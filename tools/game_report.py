#!/usr/bin/env python3
"""Basics report for every game of a replay (a .bc26, dumped through the cache below, or a ReplayDump tsv directory).

  game_report.py REPLAY.bc26|TSVDIR [--game N] [--json] [--timeline N]

Per game: map, rounds, winner and win type, backstab, points (score.points on the game's summary). Per team: global
cheese curve, starvation rounds, king HP floor (live king records: a king's dead=1 record is skipped), rat turns
(own records, dead ones included, as the summary's turnsRat) and their stall statistics (same tile for N+ own turns
while not within dist2 18 of an own king), deaths by victim type and cause, the bot's own indicator tags
(exceptions/overruns; n/a when the team has no indicator strings, as in every replica replay), bytecode use per unit
type (field_report.bytecode: max/mean/p99, turns at the limit and at 90% of it), delivery and cat damage totals.
--timeline N: every N rounds, per team baby rats and kings alive at the end of the round (babiesA/B, numKingsA/B;
an old dump: rats with a live own record that round), king HP, global cheese. --game N (0-based, match.json game =
N+1) keeps one game; --json prints one JSON line per game. SRC may also be a directory holding a replay.bc26 (a
replica match dir).

Library: dump(replay) -> tsv directory, cached under build/dumpcache/<replay sha1>-<dumper hash>/ (BC_DUMPCACHE
overrides the root). The key is the replay's content and the ReplayDump classes (built first with
tools/build-tools.sh), so a rebuilt dumper never serves an old dump. A dump is written to a temp dir and renamed into
place, so a directory that exists is complete; callers must never delete it (it is made read-only, so a stray
rmtree(ignore_errors=True) removes nothing). Every dump() removes the cache's dumps by any other dumper (of every
replay) and temp dirs of crashed dumps once they are an hour old; the current dumper's dumps are kept up to
BC_DUMPCACHE_MAX_MB (2500), least recently used first out (_trim). rows(), own(),
games(), summary(), bank() read the files; need() gives the columns a reader requires of a dumpVersion 2+ dump.
"""
import argparse, collections, fcntl, glob, hashlib, json, os, re, shutil, subprocess, sys, tempfile, time

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import score  # noqa: E402

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JDK = os.environ.get('BC_JDK', os.path.expanduser('~/opt/jdk21'))
ENGINE = os.path.join(REPO, 'engine', 'battlecode26-java-1.2.6.jar')
TOOLS = os.path.join(REPO, 'build', 'tools')
STALL = 8
STALE_AGE = 3600        # seconds before another dumper's dump (or a crashed dump's temp dir) is removed
CACHE_MAX_MB = int(os.environ.get('BC_DUMPCACHE_MAX_MB', '2500'))   # the current dumper's dumps are trimmed to this


class DumpError(RuntimeError):
    pass


_sha = {}


def file_sha(path):
    st = os.stat(path)
    k = (os.path.realpath(path), st.st_size, st.st_mtime_ns)
    if k not in _sha:
        h = hashlib.sha1()
        with open(path, 'rb') as f:
            for b in iter(lambda: f.read(1 << 20), b''):
                h.update(b)
        _sha[k] = h.hexdigest()
    return _sha[k]


def dumper_hash(tools):
    """Hash of the ReplayDump classes (inner classes and the flatbuffers helper included) and the engine jar."""
    h = hashlib.sha1()
    paths = glob.glob(os.path.join(tools, 'bc26tools', 'ReplayDump*.class'))
    paths += glob.glob(os.path.join(tools, 'com', '**', '*.class'), recursive=True)
    if not any(p.endswith('/ReplayDump.class') for p in paths):
        raise DumpError(f'no ReplayDump.class in {tools}')
    for p in sorted(paths) + [ENGINE]:
        h.update((os.path.basename(ENGINE) if p == ENGINE else os.path.relpath(p, tools)).encode() + b'\0' + file_sha(p).encode())
    return h.hexdigest()


def _rm(p):
    try:
        os.chmod(p, 0o755)
    except OSError:
        pass
    shutil.rmtree(p, ignore_errors=True)


def _sweep(root, keep):
    """Remove the temp dirs and the dumps by another dumper than `keep`'s (any replay) older than STALE_AGE."""
    now, cur = time.time(), keep.rsplit('-', 1)[1]
    for e in os.listdir(root):
        if not (e.startswith('.tmp-') or re.fullmatch(r'[0-9a-f]{20}-[0-9a-f]{12}', e) and e[21:] != cur):
            continue
        p = os.path.join(root, e)
        try:
            if now - os.stat(p).st_mtime > STALE_AGE:
                _rm(p)
        except OSError:
            pass


def _size(p):
    n = 0
    for e in os.scandir(p):
        try:
            n += e.stat(follow_symlinks=False).st_size
        except OSError:
            pass
    return n


def _trim(root, keep, cap_mb=None):
    """Evict the least recently used dumps (directory mtime, refreshed on every dump() hit) until the cache holds at
    most cap_mb (default CACHE_MAX_MB). `keep` and dumps used within STALE_AGE (a reader may be on them) stay. A full
    disk once filled the cache (568 dumps, 4 GB) and cut a dump short."""
    cap = (CACHE_MAX_MB if cap_mb is None else cap_mb) * 1024 * 1024
    now, ds = time.time(), []
    for e in os.listdir(root):
        if not re.fullmatch(r'[0-9a-f]{20}-[0-9a-f]{12}', e):
            continue
        p = os.path.join(root, e)
        try:
            ds.append((os.stat(p).st_mtime, _size(p), p))
        except OSError:
            pass
    total = sum(d[1] for d in ds)
    for mt, n, p in sorted(ds):
        if total <= cap:
            break
        if p == keep or now - mt <= STALE_AGE:
            continue
        _rm(p)
        total -= n


def dump(replay):
    """The ReplayDump tsv directory of `replay` (cached; see the module doc). Raises DumpError with ReplayDump's
    stderr when the build or the dump fails; nothing is left behind then."""
    b = subprocess.run([os.path.join(REPO, 'tools', 'build-tools.sh')], capture_output=True, text=True)
    if b.returncode != 0:
        raise DumpError(f'tools/build-tools.sh failed:\n{b.stderr.strip()}')
    root = os.environ.get('BC_DUMPCACHE', os.path.join(REPO, 'build', 'dumpcache'))
    os.makedirs(root, exist_ok=True)
    sha = file_sha(replay)
    # a shared lock on the build lock: build-tools.sh cannot swap or delete the classes while they are hashed and run
    with open(os.path.join(REPO, 'build', '.tools.lock'), 'a') as lk:
        fcntl.flock(lk, fcntl.LOCK_SH)
        tools = os.path.realpath(TOOLS)
        d = os.path.join(root, f'{sha[:20]}-{dumper_hash(tools)[:12]}')
        if not os.path.isdir(d):
            tmp = tempfile.mkdtemp(prefix='.tmp-', dir=root)
            try:
                p = subprocess.run([os.path.join(JDK, 'bin', 'java'), '-cp', f'{tools}:{ENGINE}', 'bc26tools.ReplayDump',
                                    'tsv', replay, tmp], capture_output=True, text=True)
                if p.returncode != 0:
                    raise DumpError(f'ReplayDump failed on {replay} (exit {p.returncode}):\n{p.stderr.strip()}')
                with open(os.path.join(tmp, '.stamp'), 'w') as f:
                    json.dump({'replay': os.path.abspath(replay), 'sha1': sha, 'tools': tools,
                               'made': time.strftime('%Y-%m-%dT%H:%M:%S')}, f)
                gj = os.path.join(tmp, 'game.json')
                if os.path.exists(gj) and not os.path.getsize(gj):
                    raise DumpError(f'ReplayDump left an empty game.json for {replay}')
                try:
                    os.rename(tmp, d)
                    os.chmod(d, 0o555)    # read-only: an rmtree(ignore_errors=True) by a caller removes nothing
                except OSError:       # another process finished the same dump first
                    if not os.path.isdir(d):
                        raise
            finally:
                if os.path.exists(tmp):
                    _rm(tmp)
            _trim(root, d)
        else:
            try:
                os.utime(d)   # a hit: most recently used (_trim)
            except OSError:
                pass
    _sweep(root, d)
    return d


def source(src):
    """A tsv directory for a command-line argument: a dump directory itself, else the dump of the replay (or of the
    replay.bc26 in a directory with no games, such as a replica match dir). Exit 1 on failure."""
    prog = os.path.basename(sys.argv[0])
    if os.path.isdir(src):
        if games(src):
            return src
        if not os.path.isfile(os.path.join(src, 'replay.bc26')):
            sys.exit(f'{prog}: {src} is not a ReplayDump tsv dir (no g<N>.meta.json) and holds no replay.bc26')
        src = os.path.join(src, 'replay.bc26')
    try:
        return dump(src)
    except (DumpError, OSError) as e:
        sys.exit(f'{prog}: {e}')


def version(d, game):
    """The game's dumpVersion (meta.json; 1 for a dump older than the field or a dir without meta)."""
    try:
        with open(os.path.join(d, f'g{game}.meta.json')) as f:
            return json.load(f).get('dumpVersion') or 1
    except OSError:
        return 1


def need(d, game, cols):
    """The columns `cols` (space-separated) for rows(need=) when the game's dump is dumpVersion 2 or later, else ()
    (an old dump has none of the v2 columns; the readers fall back)."""
    return tuple(cols.split()) if version(d, game) >= 2 else ()


def rows(path, need=()):
    """Dicts per tsv row. Lines end only at \\n (an old dump's indicator could hold a \\r). `need`: columns that must
    be in the header (ValueError otherwise)."""
    with open(path, newline='\n') as f:
        head = f.readline().rstrip('\n').split('\t')
        miss = [c for c in need if c not in head]
        if miss:
            raise ValueError(f'{path}: no column {", ".join(miss)}')
        for line in f:
            yield dict(zip(head, line.rstrip('\n').split('\t')))


def own(turns):
    """One row per robot-round: the src=own record (dumpVersion 2), else the last row of each (round, id) of an old
    dump (the robot's own record is written last)."""
    turns = list(turns)
    if turns and 'src' in turns[0]:
        return [r for r in turns if r['src'] == 'own']
    last = {}
    for r in turns:
        last[(r['round'], r['id'])] = r
    return list(last.values())


def bank(r, name):
    """(global cheese, kings) of team name 'A'/'B' from a rounds.tsv row (old dumps: decoded from 10*bank + kings)."""
    if r.get('bank' + name, '') != '':
        return int(r['bank' + name]), int(r['numKings' + name])
    v = int(r.get('kings' + name) or 0)
    return v // 10, v % 10


def games(d):
    return sorted(int(m.group(1)) for m in (re.match(r'g(\d+)\.meta\.json$', f) for f in os.listdir(d)) if m)


def pick(d, game):
    """The games to report: all, or [game] (exit 2 when the replay has no such game)."""
    gs = games(d)
    if game is None:
        return gs
    if game not in gs:
        print(f'no game {game} in this replay; games are {gs} (0-based, match.json game = N+1)', file=sys.stderr)
        sys.exit(2)
    return [game]


def summary(d, game):
    """The game's ReplayDump summary (g<i>.summary.json), {} for an old dump without one."""
    p = os.path.join(d, f'g{game}.summary.json')
    if not os.path.exists(p):
        return {}
    with open(p) as f:
        return json.load(f)


def result(s):
    """Winner, win type, backstab and points of a summary, or '' when there is none."""
    if not s:
        return ''
    pa, pb = score.points(s)
    bs = s.get('backstabRound', -1)
    bs = f"  backstab r{bs} by {'?AB'[s.get('backstabBy') or 0]} ({s.get('backstabCause') or '?'})" if bs is not None and bs >= 0 else ''
    return f"  winner {s.get('winner')} {s.get('winType')}{bs}  pts {pa}-{pb}"


def analyse(d, game=0):
    import field_report             # here: field_report imports this module
    pre = os.path.join(d, f'g{game}')
    meta = json.load(open(pre + '.meta.json'))
    s = summary(d, game)
    out = {'game': game, 'map': meta.get('map'), 'teams': {'A': meta.get('teamA'), 'B': meta.get('teamB')}}
    for k in ('winner', 'winType', 'backstabRound', 'backstabBy', 'backstabCause'):
        out[k] = s.get(k)
    out['pts'] = list(score.points(s)) if s else None
    curve = {1: {}, 2: {}}
    starving = {1: 0, 2: 0}
    last = None
    for r in rows(pre + '.rounds.tsv', need(d, game, 'round bankA bankB numKingsA numKingsB cheeseXferA cheeseXferB catDmgA catDmgB')):
        rn = int(r['round'])
        for t, name in ((1, 'A'), (2, 'B')):
            cheese, kings = bank(r, name)
            curve[t][rn] = cheese
            if kings > 0 and cheese < 2:
                starving[t] += 1
        last = r
    rounds = max(curve[1]) if curve[1] else 0
    kings_at = collections.defaultdict(list)       # round -> [(team, x, y)]
    king_hp = {1: 10 ** 9, 2: 10 ** 9}
    pos = {}                                        # id -> (x, y, run, team)
    stall_runs = {1: [], 2: []}
    rat_turns = {1: 0, 2: 0}
    stationary = {1: 0, 2: 0}
    tags = {1: collections.Counter(), 2: collections.Counter()}
    last_ind = {}                                   # id -> (team, last non-empty indicator string)
    team_of = {}
    turns = list(rows(pre + '.turns.tsv', need(d, game, 'round id team type x y hp ind src dead')))
    for r in turns:
        team_of[r['id']] = int(r['team'])
        if r.get('ind'):
            last_ind[r['id']] = (int(r['team']), r['ind'])
    turns = own(turns)
    for r in turns:
        if r['type'] == 'KING' and r.get('dead') != '1':
            kings_at[int(r['round'])].append((int(r['team']), int(r['x']), int(r['y'])))
    for r in turns:
        t = int(r['team'])
        if t not in (1, 2):
            continue
        rid = int(r['id'])
        if r['type'] == 'KING':
            if r.get('dead') != '1':                # the record of the turn it died in (hp <= 0)
                king_hp[t] = min(king_hp[t], int(r['hp']))
        elif r['type'] == 'RAT':
            rat_turns[t] += 1
            x, y, rn = int(r['x']), int(r['y']), int(r['round'])
            near_king = any(kt == t and (kx - x) ** 2 + (ky - y) ** 2 <= 18 for kt, kx, ky in kings_at.get(rn, []))
            p = pos.get(rid)
            if p and p[0] == x and p[1] == y and not near_king:
                run = p[2] + 1
                stationary[t] += 1
            else:
                if p and p[2] >= STALL:
                    stall_runs[t].append(p[2])
                run = 0
            pos[rid] = (x, y, run, t)
    for rid, p in pos.items():
        if p[2] >= STALL:
            stall_runs[p[3]].append(p[2])
    for rid, (t, ind) in last_ind.items():
        for tag in ('OVR', 'EXC', 'NEAR', 'SYMX'):
            m = re.search(tag + r'=(\d+)', ind)
            if m:
                tags[t][tag] += int(m.group(1))
    deaths = {1: collections.Counter(), 2: collections.Counter()}
    for r in rows(pre + '.actions.tsv', need(d, game, 'round action a b c d e')):
        if r['action'] != 'die':
            continue
        if r.get('e'):                              # dumpVersion 2: d victim team, e cause
            if r['e'] != 'merge' and r['d'] in ('1', '2'):
                deaths[int(r['d'])][r['c'] + ':' + r['e']] += 1
        elif team_of.get(r['a']) in (1, 2):         # old dump: the victim's team from its turns
            deaths[team_of[r['a']]][r['c'] + ':' + r['b']] += 1
    bc = field_report.bytecode(d, game)
    for t, name in ((1, 'A'), (2, 'B')):
        c = curve[t]
        out[name] = {
            'cheese@': {k: c.get(k) for k in (100, 250, 500, 1000, 1500, 2000) if k in c},
            'cheeseMin': min(c.values()) if c else None,
            'starvingRounds': starving[t],
            'kingHpMin': None if king_hp[t] == 10 ** 9 else king_hp[t],
            'ratTurns': rat_turns[t],
            'stationaryPct': round(100.0 * stationary[t] / rat_turns[t], 1) if rat_turns[t] else 0.0,
            'stallRuns': len(stall_runs[t]), 'stallMax': max(stall_runs[t]) if stall_runs[t] else 0,
            'deaths': dict(deaths[t]), 'bc': bc[t],
            'tags': dict(tags[t]) if any(lt == t for lt, _ in last_ind.values()) else None,   # None: no indicator strings
            'xfer': int(last[f'cheeseXfer{name}']) if last else 0, 'catDmg': int(last[f'catDmg{name}']) if last else 0,
        }
    out['rounds'] = rounds
    return out


def timeline(d, every, game=0, kings=False):
    """Rows every N rounds: (round, baby rats A, B, king HP A, B (lowest live own record), global cheese A, B), and
    with kings=True the kings A, B appended. Baby rats and kings alive at the end of the round from rounds.tsv
    (babiesA/B as rebuilt by a dumpVersion 2+ dump; an old dump's babies drift, so its rats are those with a live own
    record that round; old kings are decoded by bank())."""
    pre = os.path.join(d, f'g{game}')
    cheese, nk, babies = {}, {}, {}
    for r in rows(pre + '.rounds.tsv', need(d, game, 'round babiesA babiesB engBabiesA bankA bankB numKingsA numKingsB')):
        rn = int(r['round'])
        (ca, ka), (cb, kb) = bank(r, 'A'), bank(r, 'B')
        cheese[rn], nk[rn] = (ca, cb), (ka, kb)
        if r.get('engBabiesA', '') != '':           # babiesA/B are the rebuilt count
            babies[rn] = (int(r['babiesA']), int(r['babiesB']))
    rats = collections.defaultdict(lambda: [0, 0])
    khp = collections.defaultdict(lambda: [None, None])
    for r in own(rows(pre + '.turns.tsv', need(d, game, 'round team type hp src dead'))):
        t = int(r['team'])
        if t not in (1, 2) or r.get('dead') == '1':
            continue
        rn = int(r['round'])
        if r['type'] == 'RAT':
            rats[rn][t - 1] += 1
        elif r['type'] == 'KING':
            cur = khp[rn][t - 1]
            khp[rn][t - 1] = int(r['hp']) if cur is None else min(cur, int(r['hp']))
    out = []
    for rn in sorted(cheese):
        if rn % every == 0 or rn == max(cheese):
            ra, rb = babies.get(rn, rats[rn])
            out.append((rn, ra, rb, khp[rn][0], khp[rn][1], cheese[rn][0], cheese[rn][1]) + (nk[rn] if kings else ()))
    return out


def header(d, g):
    """One line per game: index, map, teams, rounds, and the result from the summary."""
    meta = json.load(open(os.path.join(d, f'g{g}.meta.json')))
    s = summary(d, g)
    return (f"game {g} {meta.get('map')}  rounds {s.get('rounds', '?')}  A={meta.get('teamA')}  B={meta.get('teamB')}"
            + result(s))


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument('src'); ap.add_argument('--json', action='store_true'); ap.add_argument('--timeline', type=int, default=0)
    ap.add_argument('--game', type=int, default=None, help='0-based game index (match.json game = N+1); default all')
    o = ap.parse_args(argv)
    d = source(o.src)
    for g in pick(d, o.game):
        if o.timeline:
            print(header(d, g))
            print('round  ratsA ratsB  kingsA kingsB  kingHpA kingHpB  cheeseA cheeseB')
            for t in timeline(d, o.timeline, g, kings=True):
                print('%5d  %5d %5d  %6d %6d  %7s %7s  %7d %7d' % (t[:3] + t[7:] + t[3:7]))
            continue
        rep = analyse(d, g)
        if o.json:
            print(json.dumps(rep)); continue
        print(header(d, g))
        for name in ('A', 'B'):
            s = rep[name]
            tags = 'n/a (no indicator strings)' if s['tags'] is None else s['tags']
            print(f"  {name}: cheese {s['cheese@']} min {s['cheeseMin']} starving {s['starvingRounds']} kingHpMin {s['kingHpMin']}"
                  f" | rat-turns {s['ratTurns']} stationary {s['stationaryPct']}% stalls>={STALL}: {s['stallRuns']} (max {s['stallMax']})"
                  f" | xfer {s['xfer']} cat {s['catDmg']} deaths {s['deaths']} tags: {tags}")
            for ty, x in s['bc'].items():
                print(f"     bc {ty}: {x['turns']} turns, max {x['max']} mean {x['mean']} p99 {x['p99']};"
                      f" at the limit ({x['limit']}) {x['over']}, at 90% {x['near90']}")


if __name__ == '__main__':
    main()
