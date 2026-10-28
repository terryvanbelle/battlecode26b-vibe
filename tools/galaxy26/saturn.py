#!/usr/bin/env python3
"""The bc26 replica's saturn: compiles submissions and runs matches for siarnaq, with saturn's message and report
formats, around the pinned Battlecode 2026 Java engine (battlecode26-java 1.2.6) and JDK 21.

Runs as bc26replica in bc26-galaxy-saturn.service, inside a private network namespace (PrivateNetwork=yes: only `lo`).
Its only way out is the relay's unix socket ($GALAXY_HOME/run/relay.sock); the relay (relay.py) forwards the reports to
siarnaq on 127.0.0.1:8026 with an ID token. Every compile and every game additionally runs under sandbox.sh
(`unshare --user --net --mount`): no network at all, and everything but the task's own work directory read-only.

  python3 tools/galaxy26/saturn.py run [--compile-slots 1] [--execute-slots N]     (the service; GALAXY_EXECUTE_SLOTS)
  python3 tools/galaxy26/saturn.py status                                          (queue counts)

Pub/Sub, the task protocol and the step logs are the 2023 replica's (galaxy saturn pkg/saturn): a slot leases the
oldest spooled message; invalid JSON -> ack (dropped); finished OK!/aborted -> ack; errored or interrupted -> nack.

Compile recipe (galaxy saturn pkg/run/java.go at f343088, episode language java21): Hello world; Prepare scaffold;
Download source code (zip of src/: <package>/RobotPlayer.java ..., saturn's zip-slip check, 64 MiB expansion limit);
Build source code (javac --release 21 -proc:none against the engine jar, which is what the scaffold's `gradlew
verify` compiles with, then the engine's battlecode.instrumenter.Verifier <package> <classes>, the task `verify`
runs); Upload binary (zip of the class tree, as saturn's PutArchive of build/classes); Compile succeeded.
Failures of the user's code are OK! with accepted=false; infrastructure failures are TRY. Scala and Python sources are
not built (the replica compiles Java only).

Execute recipe: Hello world; Prepare scaffold; Download binary (A and B); Run match; Upload replay; Determine scores.
saturn runs `gradlew run` once over all maps with -PshowIndicators=false -PoutputVerbose=false
-PalternateOrder=<request> (validate-maps true, no profiler, no debug methods). Here each map is one engine run with
the same properties, so a finished game survives a saturn restart or a failed attempt (kept in
$GALAXY_HOME/saturn/resume until the message is acked; RESUME_DAYS): game i of an alternate-order match with i odd
plays the map loaded with teams reversed, which is exactly what the engine's own multi-map run does
(Server.runMatch(teamsReversed); the runtime holds those reversed maps, built by Bc26Tool reverse-maps with the
engine's GameMapIO). The per-game replays are merged into one .bc26 file (replay_merge.py: one GameHeader, the games
in order, one GameFooter with the engine's winner rule), uploaded at siarnaq's replay path. Scores are counted with
saturn's regex: [games A won, games B won].
"""
import argparse
import base64
import hashlib
import http.client
import io
import json
import logging
import os
import re
import shutil
import signal
import socket
import subprocess
import sys
import threading
import time
import traceback
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
if HERE not in sys.path:
    sys.path.insert(0, HERE)

import replay_merge  # noqa: E402
from replica_gcp import config, spool, storage  # noqa: E402

MAX_EXTRACTED = 64 * 1024 * 1024
# Concurrent matches (GALAXY_EXECUTE_SLOTS, default 4) are capped at the VM's 8 vCPUs. The Java engine meters robots
# in bytecodes, not wall time, so contention changes no game; it only makes them slower.
MAX_EXECUTE_SLOTS = 8
DEFAULT_EXECUTE_SLOTS = 4
# Java heap per game (both teams share one engine JVM). A runaway bot ends its game (the match retries, then errors)
# instead of driving the VM into swap.
GAME_HEAP_MB = int(os.environ.get('GALAXY_GAME_HEAP_MB', '2048'))
COMPILE_HEAP_MB = 1024
# A match's finished games survive a failed or interrupted attempt: the retry plays only the games still missing.
RESUME_DAYS = 3
REVISION = 'bc26-replica tools/galaxy26/saturn.py'
JAVA_WINNER_REGEX = re.compile(r'(?m)^\[server\]\s*.*\(([AB])\) wins \(round ([0-9]+)\)$')   # saturn java.go:16
REASON_REGEX = re.compile(r'(?m)^\[server\]\s*Reason: (.*)$')
MAP_RE = re.compile(r'^[A-Za-z0-9_-]{1,64}$')
PACKAGE_RE = re.compile(r'^[A-Za-z_][A-Za-z0-9_]*(\.[A-Za-z_][A-Za-z0-9_]*)*$')
ADD_OPENS = ['--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED', '--add-opens=java.base/jdk.internal.math=ALL-UNNAMED',
             '--add-opens=java.base/jdk.internal.util=ALL-UNNAMED', '--add-opens=java.base/jdk.internal.access=ALL-UNNAMED',
             '--add-opens=java.base/sun.security.action=ALL-UNNAMED']     # the scaffold's runJavaLocal jvmArgs
LOG_TAIL = 12000     # characters of a command's output kept in the task logs

log = logging.getLogger('saturn')

RUNNING, COMPLETED, ABORTED, ERRORED, INTERRUPTED = range(5)
STATUS_STRING = {RUNNING: 'RUN', COMPLETED: 'OK!', ERRORED: 'TRY', INTERRUPTED: 'TRY', ABORTED: 'ABT'}


def game_timeout():
    """Wall-clock limit per game (one map), seconds. A 3-game match may take up to 3x this."""
    return int(os.environ.get('GALAXY_GAME_TIMEOUT') or 3600)


SLOT_RETRY = 30   # seconds a slot waits after an error outside its task before it leases again


def nack_delay():
    return float(os.environ.get('GALAXY_NACK_DELAY') or 10)


def resume_dir(payload):
    """Where an execute message's finished games wait for its next attempt: one directory per (report url, details)."""
    key = json.dumps([(payload.get('metadata') or {}).get('report-url'), payload.get('details')], sort_keys=True)
    return config.galaxy_home('saturn', 'resume', hashlib.sha256(key.encode()).hexdigest()[:24])


def forget_games(payload):
    shutil.rmtree(resume_dir(payload), ignore_errors=True)


def prune_resume(now=None, days=RESUME_DAYS):
    root, n = config.galaxy_home('saturn', 'resume'), 0
    for f in os.listdir(root) if os.path.isdir(root) else []:
        p = os.path.join(root, f)
        if os.path.getmtime(p) < (now or time.time()) - days * 86400:
            shutil.rmtree(p, ignore_errors=True)
            n += 1
    return n


def kept_game(gdir, map_name, reversed_):
    """The row of a game an earlier attempt finished in gdir (the same map and orientation), or None."""
    try:
        with open(os.path.join(gdir, 'row.json')) as fh:
            row = json.load(fh)
    except (OSError, ValueError):
        return None
    if (row.get('map'), row.get('reversed')) != (map_name, reversed_) or row.get('winner') not in ('A', 'B') or \
            not os.path.isfile(os.path.join(gdir, 'replay.bc26')):
        return None
    return row


def keep_game(gdir, row, replay):
    """Store a finished game for later attempts: written beside gdir, then renamed into place."""
    tmp = gdir + '.tmp'
    shutil.rmtree(tmp, ignore_errors=True)
    os.makedirs(tmp)
    shutil.copyfile(replay, os.path.join(tmp, 'replay.bc26'))
    with open(os.path.join(tmp, 'row.json'), 'w') as fh:
        json.dump(row, fh)
    shutil.rmtree(gdir, ignore_errors=True)
    os.replace(tmp, gdir)
    return os.path.join(gdir, 'replay.bc26')


class TaskFinished(Exception):
    """saturn's panic(taskFinished{}): unwinds the recipe once the task has its final status."""


class ReportError(Exception):
    pass


class Interrupted(Exception):
    pass


# ---------------------------------------------------------------- the relay client (HTTP over a unix socket)
class _UnixHTTPConnection(http.client.HTTPConnection):
    def __init__(self, path, timeout):
        super().__init__('relay', timeout=timeout)
        self._path = path

    def connect(self):
        s = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
        s.settimeout(self.timeout)
        s.connect(self._path)
        self.sock = s


def relay_post(path, obj, sock_path=None, timeout=150):
    conn = _UnixHTTPConnection(sock_path or config.relay_socket(), timeout)
    try:
        conn.request('POST', path, body=json.dumps(obj).encode(), headers={'Content-Type': 'application/json'})
        resp = conn.getresponse()
        body = resp.read()
        try:
            return resp.status, json.loads(body or b'{}')
        except ValueError:
            return resp.status, {'raw': body[:500].decode('utf-8', 'replace')}
    finally:
        conn.close()


class RelayReporter:
    def __init__(self, sock_path=None, post=relay_post):
        self.sock_path, self.post = sock_path, post

    def report(self, task):
        payload = build_report(task)
        meta = task.payload.get('metadata') or {}
        req = {'url': meta.get('report-url'), 'task_type': meta.get('task-type'), 'payload': payload}
        try:
            status, body = self.post('/v1/report', req, self.sock_path)
        except (OSError, http.client.HTTPException) as e:
            raise ReportError(f'relay unreachable: {e}') from None
        if status != 200:
            raise ReportError(f'relay refused the report: {status} {body}')
        code = int(body.get('status', 0))
        if code == 409:
            task.finish(ABORTED)
        if not 200 <= code < 300:
            raise ReportError(f'bad status code: {code}')


def build_report(task):
    payload = dict(task.details or {})
    logs, task.logs = ''.join(task.logs), []
    payload['invocation'] = {'status': STATUS_STRING[task.status], 'logs': logs,
                             'interrupted': task.status == INTERRUPTED}
    return payload


# ---------------------------------------------------------------- tasks and recipes
class Task:
    def __init__(self, payload, reporter, stop=None):
        self.payload, self.reporter = payload, reporter
        self.stop = stop or threading.Event()
        self.status, self.details, self.logs = RUNNING, None, []

    def debug(self, msg):
        self.logs.append(f'{msg}\n')
        log.debug('%s', msg.rstrip()[:300])

    def finish(self, status, details=None):
        if self.status != RUNNING:
            return
        self.status, self.details = status, details
        raise TaskFinished()

    def run(self, runner):
        """True = acknowledge the message, False = nack it for redelivery."""
        err = None
        try:
            self.reporter.report(self)
            runner(self)
        except TaskFinished:
            pass
        except Exception as e:
            err = e
            log.error('task error: %s\n%s', e, traceback.format_exc())
        if self.status == RUNNING:
            self.status = ERRORED
        try:
            self.finalize_report()
        except TaskFinished:
            pass
        except Exception as e:
            err = e if err is None else Exception(f'{err}, t.FinalizeReport: {e}')
            log.error('final report failed: %s', e)
        if self.status in (ERRORED, INTERRUPTED):
            return False
        return err is None

    def finalize_report(self):
        if self.status == ABORTED:
            return
        if self.stop.is_set() and self.status != COMPLETED:
            self.status = INTERRUPTED
        self.reporter.report(self)


def run_recipe(task, steps):
    n = len(steps)
    for i, (name, fn) in enumerate(steps, 1):
        task.debug(f'>>> Starting step {i}/{n}: {name}')
        try:
            fn(task)
        except (TaskFinished, Interrupted):
            raise
        except Exception as e:
            task.debug(f'Step returned with an error: {e}.')
            raise
        finally:
            task.debug(f'>>> Ending step {i}/{n}\n')


def step_hello(task):
    task.debug('Welcome to Saturn!')
    task.debug(f'Node: {socket.gethostname()}')
    task.debug(f'Revision: {REVISION}')


def run_cmd(task, cmd, timeout, cwd=None, env=None):
    """Run a command in its own process group; stop() kills the group. Returns (exit code, stdout, stderr)."""
    task.debug('Running command: ' + ' '.join(os.path.basename(c) if c.startswith('/') and i == 0 else c
                                              for i, c in enumerate(cmd)))
    proc = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, stdin=subprocess.DEVNULL,
                            text=True, errors='replace', cwd=cwd, env=env, start_new_session=True)
    deadline = time.time() + timeout
    while True:
        try:
            out, err = proc.communicate(timeout=1)
            return proc.returncode, out or '', err or ''
        except subprocess.TimeoutExpired:
            if task.stop.is_set() or time.time() > deadline:
                _kill_group(proc)
                out, err = proc.communicate()
                if task.stop.is_set():
                    raise Interrupted('stopped while running ' + os.path.basename(cmd[0]))
                return 124, out or '', err or ''


def _kill_group(proc):
    try:
        os.killpg(proc.pid, signal.SIGTERM)
    except ProcessLookupError:
        return
    try:
        proc.wait(timeout=15)
    except subprocess.TimeoutExpired:
        try:
            os.killpg(proc.pid, signal.SIGKILL)
        except ProcessLookupError:
            pass


# ---------------------------------------------------------------- storage
def file_spec(d, what):
    if not isinstance(d, dict) or not isinstance(d.get('bucket'), str) or not isinstance(d.get('name'), str):
        raise ValueError(f'bad file specification for {what}: {d!r}')
    storage.object_path(d['bucket'], d['name'])
    return d['bucket'], d['name']


def safe_extract(data, root, limit=MAX_EXTRACTED):
    """Extract zip bytes under root. Returns None on success or an error message (malformed, illegal path, too big,
    symlink)."""
    root = os.path.abspath(root)
    try:
        zf = zipfile.ZipFile(io.BytesIO(data))
        infos = zf.infolist()
    except (zipfile.BadZipFile, ValueError, OSError) as e:
        return f'Archive is malformed: {e}'
    total = 0
    for info in infos:
        local = os.path.abspath(os.path.join(root, info.filename))
        if not local.startswith(root + os.sep):
            return f'Archive contains illegal path: {info.filename}'
        if (info.external_attr >> 16) & 0o170000 == 0o120000:
            return f'Archive contains a symbolic link: {info.filename}'
        total += info.file_size
    if total > limit:
        return f'Archive expands to {total} bytes (limit {limit}).'
    os.makedirs(root, exist_ok=True)
    try:
        with zf:
            for info in infos:
                local = os.path.abspath(os.path.join(root, info.filename))
                if info.is_dir():
                    os.makedirs(local, exist_ok=True)
                    continue
                os.makedirs(os.path.dirname(local), exist_ok=True)
                with zf.open(info) as r, open(local, 'wb') as w:
                    shutil.copyfileobj(r, w)
    except (zipfile.BadZipFile, ValueError, OSError, EOFError) as e:
        return f'Archive is malformed: {e}'
    return None
def find_sources(root):
    """(java files, other kinds found) under root, skipping hidden directories and __MACOSX."""
    java, other = [], set()
    for dirpath, dirnames, filenames in os.walk(root):
        dirnames[:] = sorted(d for d in dirnames if not d.startswith('.') and d != '__MACOSX')
        for f in sorted(filenames):
            if f.startswith('.'):
                continue
            if f.endswith('.java'):
                java.append(os.path.join(dirpath, f))
            elif f.endswith('.scala'):
                other.add('scala')
            elif f.endswith('.py'):
                other.add('python')
    return java, other


# ---------------------------------------------------------------- the runtime (JDK 21, the engine, maps)
class Runtime:
    """Where the pinned engine lives: $GALAXY_RUNTIME (default /opt/bc26-galaxy/runtime) holds jdk/ (Temurin 21),
    engine/battlecode26-java-<v>.jar, maps/ (the episode's .map26 files), maps-reversed/ (the same maps with teams
    reversed) and tool/Bc26Tool.class (installed by galaxy-setup.sh runtime)."""

    def __init__(self, root=None, sandbox=None):
        self.root = root or os.environ.get('GALAXY_RUNTIME') or '/opt/bc26-galaxy/runtime'
        self.java = os.path.join(self.root, 'jdk', 'bin', 'java')
        self.javac = os.path.join(self.root, 'jdk', 'bin', 'javac')
        jars = sorted(f for f in os.listdir(os.path.join(self.root, 'engine'))
                      if re.match(r'^battlecode26-java-[0-9.]+\.jar$', f)) if os.path.isdir(os.path.join(self.root, 'engine')) else []
        self.jar = os.path.join(self.root, 'engine', jars[-1]) if jars else os.path.join(self.root, 'engine', 'missing.jar')
        self.maps = os.path.join(self.root, 'maps')
        self.maps_reversed = os.path.join(self.root, 'maps-reversed')
        # sandbox: the wrapper that puts a command in its own user + network + mount namespace with a read-only view
        # of everything but the work directory (sandbox.sh). Tests pass [] to run without it.
        self.sandbox = ['/bin/sh', os.path.join(HERE, 'sandbox.sh')] if sandbox is None else sandbox

    def describe(self):
        return f'engine {os.path.basename(self.jar)}, java {self.java}, sandbox {"on" if self.sandbox else "OFF"}'

    def map_dir(self, name, reversed_):
        d = self.maps_reversed if reversed_ else self.maps
        if not os.path.isfile(os.path.join(d, name + '.map26')):
            raise RuntimeError(f'map {name} is not installed in {d}')
        return d

    def jvm(self, workdir, heap_mb):
        return [self.java, f'-Xmx{heap_mb}m', '-XX:+ExitOnOutOfMemoryError', '-XX:-UsePerfData',
                f'-Djava.io.tmpdir={workdir}']

    def game_argv(self, gdir, names, urls, packages, map_name, map_dir, replay):
        """The scaffold's runJavaLocal properties as saturn passes them (java.go RunMatch: outputVerbose=false,
        showIndicators=false, validateMaps=true, alternateOrder handled by the caller, one map)."""
        return self.jvm(gdir, GAME_HEAP_MB) + ADD_OPENS + [
            '-cp', self.jar,
            '-Dbc.server.wait-for-client=false', '-Dbc.server.websocket=false',
            '-Dbc.server.mode=headless', f'-Dbc.game.map-path={map_dir}',
            '-Dbc.server.robot-player-to-system-out=false', '-Dbc.server.debug=false',
            '-Dbc.engine.debug-methods=false', '-Dbc.engine.enable-profiler=false', '-Dbc.engine.show-indicators=false',
            f'-Dbc.game.team-a={names[0]}', f'-Dbc.game.team-b={names[1]}',
            '-Dbc.game.team-a.language=java', '-Dbc.game.team-b.language=java',
            f'-Dbc.game.team-a.url={urls[0]}', f'-Dbc.game.team-b.url={urls[1]}',
            f'-Dbc.game.team-a.package={packages[0]}', f'-Dbc.game.team-b.package={packages[1]}',
            f'-Dbc.game.maps={map_name}', '-Dbc.server.validate-maps=true', '-Dbc.server.alternate-order=false',
            f'-Dbc.server.save-file={replay}', 'battlecode.server.Main', '-c=-']

    def cmd(self, workdir, argv):
        return [*self.sandbox, workdir, *argv] if self.sandbox else list(argv)


def game_env():
    return {'PATH': '/usr/bin:/bin', 'LANG': 'C.UTF-8', 'HOME': '/nonexistent'}


def parse_maps(maps):
    if not isinstance(maps, list) or not maps or not all(isinstance(m, str) and MAP_RE.match(m) for m in maps):
        raise ValueError(f'bad map list {maps!r}')
    return maps


def parse_game_output(out):
    """(winner 'A'|'B', rounds, reason) of a one-map engine run, by saturn's winner regex; exactly one line."""
    wins = JAVA_WINNER_REGEX.findall(out)
    if len(wins) != 1:
        raise RuntimeError(f'could not determine winner: {len(wins)} winner lines for 1 map')
    reasons = REASON_REGEX.findall(out)
    return wins[0][0], int(wins[0][1]), (reasons[0].strip() if reasons else '')


def tally(rows):
    s = [0, 0]
    for r in rows:
        if r.get('winner') == 'A':
            s[0] += 1
        elif r.get('winner') == 'B':
            s[1] += 1
        else:
            raise RuntimeError(f'unknown winner: {r.get("winner")!r}')
    return s


def _tail(s, n=LOG_TAIL):
    return s if len(s) <= n else '[... ' + str(len(s) - n) + ' characters cut ...]\n' + s[-n:]


class Runners:
    def __init__(self, workdir, runtime, game_timeout_s=None):
        self.workdir, self.rt = workdir, runtime
        self.game_timeout = game_timeout_s or game_timeout()

    def _prepare(self, task):
        task.debug('Flushing build directory.')
        shutil.rmtree(self.workdir, ignore_errors=True)
        os.makedirs(self.workdir)
        task.debug(f'Using {self.rt.describe()}.')

    # ---- compile
    def compile(self, task):
        d = task.payload.get('details') or {}
        src_root = os.path.join(self.workdir, 'src')
        classes = os.path.join(self.workdir, 'build', 'classes')

        def download(t):
            bucket, name = file_spec(d.get('source'), 'source')
            data = storage.read_bytes(bucket, name)
            t.debug(f'File retrieved, total {len(data)} bytes.')
            err = safe_extract(data, src_root)
            if err:
                t.debug(err)
                t.finish(COMPLETED, {'accepted': False})

        def build(t):
            pkg = d.get('package') or ''
            if pkg == '':
                t.debug('Package name must not be empty.')
                t.finish(COMPLETED, {'accepted': False})
            if not PACKAGE_RE.match(pkg):
                t.debug(f'Package name {pkg!r} is not a Java package name.')
                t.finish(COMPLETED, {'accepted': False})
            java, other = find_sources(src_root)
            if not java:
                t.debug('No Java sources found.' + (f' This replica compiles Java only (found {", ".join(sorted(other))}).'
                                                    if other else ''))
                t.finish(COMPLETED, {'accepted': False})
            if other:
                t.debug(f'Note: {", ".join(sorted(other))} files are ignored (this replica compiles Java only).')
            os.makedirs(classes, exist_ok=True)
            argfile = os.path.join(self.workdir, 'sources.txt')
            with open(argfile, 'w') as fh:
                for p in java:
                    fh.write('"' + p.replace('\\', '\\\\').replace('"', '\\"') + '"\n')
            t.debug(f'Compiling {len(java)} Java file(s).')
            argv = [self.rt.javac, f'-J-Xmx{COMPILE_HEAP_MB}m', '-J-XX:-UsePerfData', '--release', '21', '-proc:none',
                    '-nowarn', '-encoding', 'UTF-8', '-cp', self.rt.jar, '-d', classes, '@' + argfile]
            rc, out, err = run_cmd(t, self.rt.cmd(self.workdir, argv), timeout=600, env=game_env())
            t.debug(_tail(out + err))
            if rc != 0:
                t.debug(f'Build failed (javac exit {rc}).')
                t.finish(COMPLETED, {'accepted': False})
            argv = self.rt.jvm(self.workdir, COMPILE_HEAP_MB) + ['-cp', self.rt.jar, 'battlecode.instrumenter.Verifier',
                                                                  pkg, classes]
            rc, out, err = run_cmd(t, self.rt.cmd(self.workdir, argv), timeout=600, env=game_env())
            t.debug(_tail(out + err))
            if rc != 0:
                t.debug(f'Verification failed (exit {rc}).')
                t.finish(COMPLETED, {'accepted': False})

        def upload(t):
            bucket, name = file_spec(d.get('binary'), 'binary')
            tmp = os.path.join(self.workdir, 'binary.zip')
            with zipfile.ZipFile(tmp, 'w', zipfile.ZIP_DEFLATED) as zw:
                for dirpath, dirnames, filenames in os.walk(classes):
                    dirnames.sort()
                    for f in sorted(filenames):
                        p = os.path.join(dirpath, f)
                        zw.write(p, os.path.relpath(p, classes))
            storage.write_file(bucket, name, tmp, 'application/zip', 'projectPrivate')

        def succeeded(t):
            t.finish(COMPLETED, {'accepted': True})

        run_recipe(task, [('Hello world', step_hello), ('Prepare scaffold', self._prepare),
                          ('Download source code', download), ('Build source code', build),
                          ('Upload binary', upload), ('Compile succeeded', succeeded)])

    # ---- execute
    def execute(self, task):
        d = task.payload.get('details') or {}
        data = os.path.join(self.workdir, 'data')
        a, b = d.get('a') or {}, d.get('b') or {}
        maps = parse_maps(d.get('maps'))
        alternate = bool(d.get('alternate-order'))
        names = [str(a.get('team-name')), str(b.get('team-name'))]
        packages = [str(a.get('package')), str(b.get('package'))]
        for p in packages:
            if not PACKAGE_RE.match(p):
                raise ValueError(f'bad package name {p!r}')
        urls = [os.path.join(data, 'A'), os.path.join(data, 'B')]
        results = []

        def download(t):
            for key, sub, url in (('A', a, urls[0]), ('B', b, urls[1])):
                bucket, name = file_spec(sub.get('binary'), f'binary {key}')
                err = safe_extract(storage.read_bytes(bucket, name), url)
                if err:
                    raise RuntimeError(f'binary {key} unusable: {err}')

        def run_match(t):
            kept = resume_dir(task.payload)
            for i, m in enumerate(maps):
                rev = alternate and i % 2 == 1
                kdir = os.path.join(kept, f'game-{i + 1:02d}')
                row = kept_game(kdir, m, rev)
                if row is not None:
                    results.append((row, os.path.join(kdir, 'replay.bc26')))
                    t.debug(f'Game {i + 1}/{len(maps)}: {m}, finished by an earlier attempt -> {row["winner"]} wins '
                            f'(round {row["rounds"]})')
                    continue
                gdir = os.path.join(data, f'game-{i + 1:02d}')
                os.makedirs(gdir)
                replay = os.path.join(gdir, 'replay.bc26')
                argv = self.rt.game_argv(gdir, names, urls, packages, m, self.rt.map_dir(m, rev), replay)
                t.debug(f'Game {i + 1}/{len(maps)}: {m}' + (' (teams reversed: alternate order)' if rev else ''))
                t0 = time.time()
                rc, out, err = run_cmd(t, self.rt.cmd(gdir, argv), timeout=self.game_timeout, env=game_env())
                t.debug(_tail(out + err, 4000))
                if rc != 0 or not os.path.isfile(replay):
                    raise RuntimeError(f'game {i + 1} failed: exit status {rc}' +
                                       (f' (time limit {self.game_timeout} s)' if rc == 124 else ''))
                winner, rounds, reason = parse_game_output(out)
                row = {'game': i + 1, 'map': m, 'reversed': rev, 'winner': winner, 'winner_team': names['AB'.index(winner)],
                       'rounds': rounds, 'reason': reason, 'wall_s': round(time.time() - t0, 1)}
                results.append((row, keep_game(kdir, row, replay)))
                shutil.rmtree(gdir, ignore_errors=True)

        def upload_replay(t):
            bucket, name = file_spec(d.get('replay'), 'replay')
            raws = []
            for _, p in results:
                with open(p, 'rb') as fh:
                    raws.append(fh.read())
            s = tally([r for r, _ in results])
            merged = replay_merge.merge(raws, 'A' if s[0] >= s[1] else 'B')
            out = os.path.join(self.workdir, 'replay.bc26')
            with open(out, 'wb') as fh:
                fh.write(merged)
            t.debug(f'Replay: {len(results)} game(s), {len(merged)} bytes.')
            storage.write_file(bucket, name, out, 'application/octet-stream', 'publicRead')

        def scores(t):
            s = tally([r for r, _ in results])
            if sum(s) != len(maps):
                raise RuntimeError(f'could not determine winner: {sum(s)} winners for {len(maps)} maps')
            t.finish(COMPLETED, {'scores': s})

        run_recipe(task, [('Hello world', step_hello), ('Prepare scaffold', self._prepare),
                          ('Download binary', download), ('Run match', run_match), ('Upload replay', upload_replay),
                          ('Determine scores', scores)])


# ---------------------------------------------------------------- the subscriber
def parse_message(msg):
    try:
        payload = json.loads(base64.b64decode(msg['data'], validate=True))
    except (KeyError, TypeError, ValueError):
        return None
    if not isinstance(payload, dict):
        return None
    for k, t in (('episode', dict), ('metadata', dict), ('details', dict)):
        if k in payload and not isinstance(payload[k], t):
            return None
    return payload


class Executor:
    def __init__(self, compile_slots=1, execute_slots=4, runtime=None, reporter=None, poll=1.0):
        self.compile_slots, self.execute_slots = compile_slots, execute_slots
        self.runtime = runtime or Runtime()
        self.reporter = reporter or RelayReporter()
        self.poll = poll
        self.stop = threading.Event()

    def handle(self, payload, workdir):
        runners = Runners(workdir, self.runtime)
        kind = (payload.get('metadata') or {}).get('task-type')
        runner = {'compile': runners.compile, 'execute': runners.execute}.get(kind)
        if runner is None:
            log.error('no such task type: %r', kind)
            return False
        return Task(payload, self.reporter, self.stop).run(runner)

    def slot(self, topic, k):
        """One consumer. An error outside the task's own handler (a full disk failing the spool's writes: on
        2026-10-24 five of eight slots died that way and their matches sat at RUN until a restart) returns the leased
        message unchanged and restarts the loop after SLOT_RETRY seconds."""
        q = spool.topic_queue(topic).ensure()
        while not self.stop.is_set():
            held = []
            try:
                self.consume(q, topic, k, held)
            except Exception:
                log.exception('slot %s-%d: crashed outside the task; returning %s, restarting in %ds', topic, k,
                              held or 'nothing', SLOT_RETRY)
                for msg_id in held:
                    try:
                        q.release(msg_id)
                    except OSError:
                        log.exception('slot %s-%d: could not return %s (the next start recovers it)', topic, k, msg_id)
                self.stop.wait(SLOT_RETRY)

    def consume(self, q, topic, k, held):
        workdir = config.galaxy_home('saturn', f'{topic}-{k}')
        while not self.stop.is_set():
            item = q.lease()
            if item is None:
                self.stop.wait(self.poll)
                continue
            msg_id, msg = item
            held[:] = [msg_id]
            payload = parse_message(msg)
            if payload is None:
                log.error('slot %s-%d: invalid message %s; dropped', topic, k, msg_id)
                q.ack(msg_id)
                continue
            meta = payload.get('metadata') or {}
            log.info('slot %s-%d: starting %s %s', topic, k, meta.get('task-type'), meta.get('report-url'))
            t0 = time.time()
            try:
                ok = self.handle(payload, workdir)
            except Exception:
                log.exception('slot %s-%d: handler crashed', topic, k)
                ok = False
            if ok:
                q.ack(msg_id)
                shutil.rmtree(workdir, ignore_errors=True)     # disk is scarce: keep nothing once reported
                forget_games(payload)
            else:
                msg = dict(msg, delivery_attempt=int(msg.get('delivery_attempt') or 0) + 1)
                q.nack(msg_id, msg, delay=0 if self.stop.is_set() else nack_delay())
            held.clear()
            log.info('slot %s-%d: %s %s in %.0fs', topic, k, 'acked' if ok else 'nacked', msg_id, time.time() - t0)

    def run(self):
        for topic in (config.TOPIC_COMPILE, config.TOPIC_EXECUTE):
            n = spool.topic_queue(topic).ensure().recover()
            if n:
                log.info('returned %d unacknowledged %s message(s) to the queue', n, topic)
        n = prune_resume()
        if n:
            log.info('removed %d finished-game director(ies) older than %d days', n, RESUME_DAYS)
        log.info('saturn: %d compile + %d execute slots; %s; relay %s', self.compile_slots, self.execute_slots,
                 self.runtime.describe(), config.relay_socket())
        threads = [threading.Thread(target=self.slot, args=(config.TOPIC_COMPILE, k), daemon=True)
                   for k in range(self.compile_slots)]
        threads += [threading.Thread(target=self.slot, args=(config.TOPIC_EXECUTE, k), daemon=True)
                    for k in range(self.execute_slots)]
        for t in threads:
            t.start()
        while any(t.is_alive() for t in threads):
            time.sleep(0.5)
        log.info('saturn stopped')


def main(argv=None):
    ap = argparse.ArgumentParser(description=__doc__.split('\n\n')[0])
    sub = ap.add_subparsers(dest='cmd', required=True)
    r = sub.add_parser('run')
    r.add_argument('--compile-slots', type=int, default=int(os.environ.get('GALAXY_COMPILE_SLOTS') or 1))
    r.add_argument('--execute-slots', type=int, default=int(os.environ.get('GALAXY_EXECUTE_SLOTS') or DEFAULT_EXECUTE_SLOTS))
    sub.add_parser('status')
    a = ap.parse_args(argv)
    logging.basicConfig(level=logging.INFO, format='%(asctime)s %(levelname)s %(name)s %(message)s')
    if a.cmd == 'status':
        for topic in (config.TOPIC_COMPILE, config.TOPIC_EXECUTE):
            print(topic, spool.topic_queue(topic).counts())
        return 0
    # at most one match (one engine JVM) per vCPU on the shared VM
    if not (0 <= a.compile_slots <= 1 and 0 <= a.execute_slots <= MAX_EXECUTE_SLOTS):
        ap.error(f'slots: compile 0..1, execute 0..{MAX_EXECUTE_SLOTS} (at most {MAX_EXECUTE_SLOTS} games at once on the shared VM)')
    ex = Executor(a.compile_slots, a.execute_slots)

    def on_signal(signum, frame):
        log.info('signal %d: stopping (running tasks are reported as interrupted)', signum)
        ex.stop.set()
    signal.signal(signal.SIGTERM, on_signal)
    signal.signal(signal.SIGINT, on_signal)
    ex.run()
    return 0


if __name__ == '__main__':
    sys.exit(main())
