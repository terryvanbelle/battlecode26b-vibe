#!/usr/bin/env python3
"""Basics report for one game from a ReplayDump tsv directory (or a .bc26, dumped on the fly).

  game_report.py REPLAY.bc26|TSVDIR [--team A|B] [--json]

Per team: global cheese curve (decoded from the round aggregate kings+10*cheese), starvation rounds,
king HP floor, rat-turn stall statistics (same tile for N+ own turns while not within dist2 18 of an own king),
deaths, exceptions/overruns from the bot's own indicator tags, delivery and cat damage totals.
"""
import argparse, collections, json, os, re, subprocess, sys, tempfile

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JDK = os.environ.get('BC_JDK', os.path.expanduser('~/opt/jdk21'))
ENGINE = os.path.join(REPO, 'engine', 'battlecode26-java-1.2.6.jar')
STALL = 8


def dump(replay):
    d = tempfile.mkdtemp(prefix='gr-')
    subprocess.run([os.path.join(JDK, 'bin', 'java'), '-cp', f"{os.path.join(REPO, 'build', 'tools')}:{ENGINE}",
                    'bc26tools.ReplayDump', 'tsv', replay, d], check=True, capture_output=True)
    return d


def rows(path):
    with open(path) as f:
        head = f.readline().rstrip('\n').split('\t')
        for line in f:
            v = line.rstrip('\n').split('\t')
            yield dict(zip(head, v))


def analyse(d, game=0):
    pre = os.path.join(d, f'g{game}')
    meta = json.load(open(pre + '.meta.json'))
    out = {'map': meta.get('map'), 'teams': {'A': meta.get('teamA'), 'B': meta.get('teamB')}}
    curve = {1: {}, 2: {}}
    starving = {1: 0, 2: 0}
    last = None
    for r in rows(pre + '.rounds.tsv'):
        rn = int(r['round'])
        for t, k in ((1, 'kingsA'), (2, 'kingsB')):
            stat = int(r[k])
            kings, cheese = stat % 10, stat // 10
            curve[t][rn] = cheese
            if kings > 0 and cheese < 2:
                starving[t] += 1
        last = r
    rounds = max(curve[1]) if curve[1] else 0
    seen = set()
    kings_at = collections.defaultdict(list)       # round -> [(team, x, y)]
    king_hp = {1: 10 ** 9, 2: 10 ** 9}
    pos = {}                                        # id -> (x, y, run, team)
    stall_runs = {1: [], 2: []}
    rat_turns = {1: 0, 2: 0}
    stationary = {1: 0, 2: 0}
    tags = {1: collections.Counter(), 2: collections.Counter()}
    last_ind = {}
    turns = list(rows(pre + '.turns.tsv'))
    for r in turns:
        if r['type'] == 'KING':
            kings_at[int(r['round'])].append((int(r['team']), int(r['x']), int(r['y'])))
    for r in turns:
        key = (r['round'], r['id'])
        if key in seen:
            continue
        seen.add(key)
        t = int(r['team'])
        if t not in (1, 2):
            continue
        rid = int(r['id'])
        if r['type'] == 'KING':
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
        last_ind[rid] = (t, r.get('ind', ''))
    for rid, p in pos.items():
        if p[2] >= STALL:
            stall_runs[p[3]].append(p[2])
    for rid, (t, ind) in last_ind.items():
        for tag in ('OVR', 'EXC', 'NEAR', 'SYMX'):
            m = re.search(tag + r'=(\d+)', ind)
            if m:
                tags[t][tag] += int(m.group(1))
    deaths = {1: collections.Counter(), 2: collections.Counter()}
    for r in rows(pre + '.actions.tsv'):
        if r['action'] == 'die':
            # the dying robot's team is not on the row; look it up via its last turn
            vid = int(r['a'])
            vt = last_ind.get(vid, (0, ''))[0]
            if vt in (1, 2):
                deaths[vt][r['c'] + ':' + r['b']] += 1
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
            'deaths': dict(deaths[t]), 'tags': dict(tags[t]),
            'xfer': int(last[f'cheeseXfer{name}']) if last else 0, 'catDmg': int(last[f'catDmg{name}']) if last else 0,
        }
    out['rounds'] = rounds
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('src'); ap.add_argument('--json', action='store_true')
    o = ap.parse_args()
    d = dump(o.src) if o.src.endswith('.bc26') else o.src
    rep = analyse(d)
    if o.json:
        print(json.dumps(rep)); return
    print(f"{rep['map']}  rounds {rep['rounds']}  A={rep['teams']['A']}  B={rep['teams']['B']}")
    for name in ('A', 'B'):
        s = rep[name]
        print(f"  {name}: cheese {s['cheese@']} min {s['cheeseMin']} starving {s['starvingRounds']} kingHpMin {s['kingHpMin']}"
              f" | rat-turns {s['ratTurns']} stationary {s['stationaryPct']}% stalls>={STALL}: {s['stallRuns']} (max {s['stallMax']})"
              f" | xfer {s['xfer']} cat {s['catDmg']} deaths {s['deaths']} tags {s['tags']}")


if __name__ == '__main__':
    main()
