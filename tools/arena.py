#!/usr/bin/env python3
"""Headless Battlecode 2026 runner.

Bot specs:
  <dir>/<pkg>            a source package directory (e.g. src/bot, bots/v3); compiled into a private, content-hashed
                         class tree build/bots/<pkg>-<hash12>/ (never recompiled while in use: trees are immutable)
  cls:<classdir>:<pkg>   an already compiled class tree (benchmark bots)

Commands:
  arena.py compile SPEC                  print the class dir
  arena.py game A B MAP [--seed S] [--out DIR] [--keep]   one game; prints the result row (JSON)
  arena.py matrix CELLS.tsv --run DIR [--jobs N]          cells: a<TAB>b<TAB>map<TAB>seed per line; appends to
                                                          DIR/results.jsonl, skipping cells already present
Every game runs with no network (unshare -rn), a heap cap and a wall-clock cap, and holds one machine-wide slot
(flock on $BC_SLOTS_DIR/slot<i>, i < $BC_SLOTS) so that every tool shares one concurrency limit.
"""
import argparse, fcntl, hashlib, json, os, re, shutil, subprocess, sys, tempfile, threading, time

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JDK = os.environ.get('BC_JDK', os.path.expanduser('~/opt/jdk21'))
ENGINE = os.path.join(REPO, 'engine', 'battlecode26-java-1.2.6.jar')
TOOLS_CP = os.path.join(REPO, 'build', 'tools')
MAPS = os.path.join(REPO, 'maps')
SLOTS = int(os.environ.get('BC_SLOTS', '7'))
SLOTS_DIR = os.environ.get('BC_SLOTS_DIR', '/tmp/bc26b-slots')
GAME_TIMEOUT = int(os.environ.get('BC_GAME_TIMEOUT', '1800'))
XMX = os.environ.get('BC_XMX', '1g')
ADD_OPENS = ['--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED', '--add-opens=java.base/jdk.internal.math=ALL-UNNAMED',
             '--add-opens=java.base/jdk.internal.util=ALL-UNNAMED', '--add-opens=java.base/jdk.internal.access=ALL-UNNAMED',
             '--add-opens=java.base/sun.security.action=ALL-UNNAMED']


def tree_hash(d):
    h = hashlib.sha256()
    for root, dirs, files in sorted(os.walk(d)):
        dirs.sort()
        for f in sorted(files):
            if f.endswith('.java'):
                p = os.path.join(root, f)
                h.update(os.path.relpath(p, d).encode()); h.update(b'\0'); h.update(open(p, 'rb').read())
    return h.hexdigest()


class Bot:
    """A bot: package name, class dir, content hash, display name."""
    def __init__(self, spec):
        self.spec = spec
        if spec.startswith('cls:'):
            _, self.cls, self.pkg = spec.split(':', 2)
            self.hash = 'cls-' + hashlib.sha256(self.cls.encode()).hexdigest()[:12]
            if os.path.exists(os.path.join(self.cls, '.hash')):
                self.hash = open(os.path.join(self.cls, '.hash')).read().strip()
            self.name = self.pkg
        else:
            src = os.path.abspath(spec).rstrip('/')
            self.pkg = os.path.basename(src)
            self.hash = tree_hash(src)[:12]
            self.name = self.pkg
            self.cls = os.path.join(REPO, 'build', 'bots', f'{self.pkg}-{self.hash}')
            if not os.path.isdir(self.cls):
                compile_pkg(src, self.pkg, self.cls)


def compile_pkg(src, pkg, out):
    files = [os.path.join(r, f) for r, _, fs in os.walk(src) for f in fs if f.endswith('.java')]
    if not files:
        raise SystemExit(f'no java sources in {src}')
    os.makedirs(os.path.dirname(out), exist_ok=True)
    tmp = tempfile.mkdtemp(prefix='.cmp-', dir=os.path.dirname(out))
    r = subprocess.run([os.path.join(JDK, 'bin', 'javac'), '-nowarn', '-proc:none', '-encoding', 'UTF-8', '--release', '21',
                        '-cp', ENGINE, '-d', tmp] + files, capture_output=True, text=True)
    if r.returncode != 0:
        shutil.rmtree(tmp, ignore_errors=True)
        raise SystemExit(f'compile failed for {src}:\n{r.stderr[-3000:]}')
    try:
        os.rename(tmp, out)          # atomic publish; a concurrent compile of the same hash loses harmlessly
    except OSError:
        shutil.rmtree(tmp, ignore_errors=True)


class Slot:
    """Machine-wide game slot (flock), shared by every tool that starts games."""
    def __enter__(self):
        os.makedirs(SLOTS_DIR, exist_ok=True)
        while True:
            for i in range(SLOTS):
                f = open(os.path.join(SLOTS_DIR, f'slot{i}'), 'w')
                try:
                    fcntl.flock(f, fcntl.LOCK_EX | fcntl.LOCK_NB)
                    self.f = f
                    return self
                except BlockingIOError:
                    f.close()
            time.sleep(0.5)

    def __exit__(self, *a):
        fcntl.flock(self.f, fcntl.LOCK_UN); self.f.close()


_map_lock = threading.Lock()


def map_dir(seed):
    """Seed 0 = the original maps. Otherwise copies with the map seed replaced, built once per seed."""
    if not seed:
        return MAPS
    d = os.path.join(REPO, 'build', 'maps', f's{seed}')
    with _map_lock:
        if not os.path.isdir(d):
            names = sorted(f[:-6] for f in os.listdir(MAPS) if f.endswith('.map26'))
            tmp = d + f'.tmp{os.getpid()}'
            subprocess.run([os.path.join(JDK, 'bin', 'java'), '-cp', f'{ENGINE}:{TOOLS_CP}', 'bc26tools.MapReseed',
                            MAPS, tmp, str(seed)] + names, check=True, capture_output=True)
            try: os.rename(tmp, d)
            except OSError: shutil.rmtree(tmp, ignore_errors=True)
    return d


WIN_RE = re.compile(r'\[server\]\s+(\S+) \(([AB])\) wins \(round (\d+)\)')
REASON_RE = re.compile(r'\[server\] Reason: (.*)')


def points(row):
    """Score both teams with the spec's formulas from the final aggregates (integer percentages, rounded)."""
    def pct(a, b):
        return (100.0 * a / (a + b), 100.0 * b / (a + b)) if a + b > 0 else (0.0, 0.0)
    ca, cb = pct(row.get('catDmgA', 0), row.get('catDmgB', 0))
    ka, kb = pct(row.get('kingsA', 0), row.get('kingsB', 0))
    xa, xb = pct(row.get('cheeseXferA', 0), row.get('cheeseXferB', 0))
    w = (0.5, 0.3, 0.2) if row.get('backstabRound', -1) < 0 else (0.3, 0.5, 0.2)
    return (round(w[0] * ca + w[1] * ka + w[2] * xa), round(w[0] * cb + w[1] * kb + w[2] * xb))


def run_game(a, b, mapname, seed=0, out=None, keep=False, tsv=False):
    """Play one game; return the result row. a and b are Bot objects (a plays team A)."""
    tmpdir = None
    if out is None:
        tmpdir = out = tempfile.mkdtemp(prefix='game-')
    out = os.path.abspath(out)
    os.makedirs(out, exist_ok=True)
    tag = f'{a.name}-{a.hash[:6]}_vs_{b.name}-{b.hash[:6]}_{mapname}_s{seed}'
    replay = os.path.join(out, tag + '.bc26')
    md = map_dir(seed)
    pkg_a, pkg_b = a.pkg, b.pkg
    cmd = ['timeout', str(GAME_TIMEOUT), os.path.join(JDK, 'bin', 'java'), f'-Xmx{XMX}', '-XX:+UseSerialGC', *ADD_OPENS,
           '-cp', ENGINE, '-Dbc.server.mode=headless', f'-Dbc.game.map-path={md}',
           '-Dbc.server.robot-player-to-system-out=false', '-Dbc.server.debug=false',
           '-Dbc.engine.debug-methods=false', '-Dbc.engine.enable-profiler=false', '-Dbc.engine.show-indicators=true',
           f'-Dbc.game.team-a={pkg_a}', f'-Dbc.game.team-b={pkg_b}', f'-Dbc.game.team-a.url={a.cls}',
           f'-Dbc.game.team-b.url={b.cls}', f'-Dbc.game.team-a.package={pkg_a}', f'-Dbc.game.team-b.package={pkg_b}',
           f'-Dbc.game.maps={mapname}', '-Dbc.server.validate-maps=false', f'-Dbc.server.save-file={replay}',
           'battlecode.server.Main', '-c=-']
    if shutil.which('unshare') and os.environ.get('BC_NONET', '1') == '1':
        cmd = ['unshare', '-rn'] + cmd
    t0 = time.time()
    with Slot():
        p = subprocess.run(cmd, capture_output=True, text=True, cwd=out)
    wall = time.time() - t0
    row = {'a': a.name, 'a_hash': a.hash, 'b': b.name, 'b_hash': b.hash, 'map': mapname, 'seed': seed,
           'wall': round(wall, 1), 'exit': p.returncode}
    m = WIN_RE.search(p.stdout)
    if m:
        row['winner'], row['rounds'] = m.group(2), int(m.group(3))
    r = REASON_RE.search(p.stdout)
    row['reason'] = r.group(1).strip() if r else None
    if p.returncode != 0 or not m:
        row['error'] = (p.stderr or p.stdout)[-1500:]
    if os.path.exists(replay):
        s = subprocess.run([os.path.join(JDK, 'bin', 'java'), '-cp', f'{ENGINE}:{TOOLS_CP}', 'bc26tools.ReplayDump',
                            'summary', replay], capture_output=True, text=True)
        try:
            summ = json.loads(s.stdout.strip().splitlines()[-1])
            for k in ('winType', 'backstabRound', 'backstabBy', 'cheeseXferA', 'cheeseXferB', 'catDmgA', 'catDmgB',
                      'kingsA', 'kingsB', 'babiesA', 'babiesB', 'A', 'B'):
                row[k] = summ.get(k)
            row['ptsA'], row['ptsB'] = points(row)
        except Exception as e:
            row['summary_error'] = str(e)
        if tsv:
            subprocess.run([os.path.join(JDK, 'bin', 'java'), '-cp', f'{ENGINE}:{TOOLS_CP}', 'bc26tools.ReplayDump',
                            'tsv', replay, os.path.join(out, tag)], check=False)
        if keep or tsv:
            row['replay'] = replay
        else:
            os.remove(replay)
    if tmpdir and not keep and not tsv:
        shutil.rmtree(tmpdir, ignore_errors=True)
    return row


def cell_key(a_hash, b_hash, mapname, seed):
    return f'{a_hash}|{b_hash}|{mapname}|{seed}'


def matrix(cells_file, run, jobs, keep_losses_for=None):
    os.makedirs(run, exist_ok=True)
    res = os.path.join(run, 'results.jsonl')
    done = set()
    if os.path.exists(res):
        for line in open(res):
            try:
                d = json.loads(line); done.add(cell_key(d['a_hash'], d['b_hash'], d['map'], d['seed']))
            except Exception:
                pass
    bots, cells = {}, []
    for line in open(cells_file):
        if not line.strip() or line.startswith('#'):
            continue
        sa, sb, mp, sd = line.rstrip('\n').split('\t')[:4]
        for s in (sa, sb):
            if s not in bots:
                bots[s] = Bot(s)
        if cell_key(bots[sa].hash, bots[sb].hash, mp, int(sd)) not in done:
            cells.append((bots[sa], bots[sb], mp, int(sd)))
    lock = threading.Lock()
    it = iter(cells)
    n = [0]

    def worker():
        while True:
            with lock:
                c = next(it, None)
            if c is None:
                return
            row = run_game(*c, out=os.path.join(run, 'replays'), keep=os.environ.get('BC_KEEP') == '1')
            with lock:
                with open(res, 'a') as f:
                    f.write(json.dumps(row) + '\n')
                n[0] += 1
    ts = [threading.Thread(target=worker) for _ in range(max(1, jobs))]
    for t in ts: t.start()
    for t in ts: t.join()
    open(os.path.join(run, 'DONE'), 'w').write(f'{n[0]} new games\n')
    print(f'{n[0]} games -> {res}')


def main():
    ap = argparse.ArgumentParser()
    sp = ap.add_subparsers(dest='cmd', required=True)
    c = sp.add_parser('compile'); c.add_argument('spec')
    g = sp.add_parser('game'); g.add_argument('a'); g.add_argument('b'); g.add_argument('map')
    g.add_argument('--seed', type=int, default=0); g.add_argument('--out'); g.add_argument('--keep', action='store_true')
    g.add_argument('--tsv', action='store_true')
    m = sp.add_parser('matrix'); m.add_argument('cells'); m.add_argument('--run', required=True)
    m.add_argument('--jobs', type=int, default=SLOTS)
    args = ap.parse_args()
    if args.cmd == 'compile':
        print(Bot(args.spec).cls)
    elif args.cmd == 'game':
        print(json.dumps(run_game(Bot(args.a), Bot(args.b), args.map, args.seed, args.out, args.keep, args.tsv)))
    elif args.cmd == 'matrix':
        matrix(args.cells, args.run, args.jobs)


if __name__ == '__main__':
    main()
