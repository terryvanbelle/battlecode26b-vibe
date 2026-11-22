#!/usr/bin/env python3
"""Per game and team, per round window: rat deaths (by killer cause), spawns, traps laid, throws, transfers (cheese),
and baby-rat turns spent on the enemy half (closer to the enemy king start than to own). -> war.jsonl"""
import csv, json, math, os, sys

REPO = os.path.expanduser('~/projects/vibe/2026b')
sys.path.insert(0, os.path.join(REPO, 'tools'))
import game_report as GR  # noqa: E402

csv.field_size_limit(10 ** 8)
R = os.path.expanduser('~/projects/vibe/bc26b-galaxy-replays')
WIN = (0, 100, 200, 400, 800, 2001)


def w(r):
    for i in range(len(WIN) - 1):
        if WIN[i] < r <= WIN[i + 1]:
            return i
    return len(WIN) - 2


def tsv(p):
    with open(p, newline='') as f:
        yield from csv.DictReader(f, delimiter='\t')


out = open(sys.argv[1], 'a')
for mid in sys.argv[2:]:
    d = GR.dump(os.path.join(R, mid, 'replay.bc26'))
    gj = json.load(open(os.path.join(d, 'game.json')))
    for g in range(gj['games']):
        s = json.load(open(os.path.join(d, f'g{g}.summary.json')))
        n = len(WIN) - 1
        st = {t: dict(deaths=[0] * n, spawns=[0] * n, traps=[0] * n, throws=[0] * n, xfer=[0] * n, fwd=[0] * n,
                      turns=[0] * n, nearEK=[0] * n) for t in (1, 2)}
        start = {}
        for r in tsv(os.path.join(d, f'g{g}.turns.tsv')):
            if r.get('src', 'own') != 'own':
                continue
            t = int(r['team']) if r['team'].isdigit() else 0
            if t not in (1, 2):
                continue
            if r['type'] == 'KING':
                start.setdefault(t, (int(r['x']), int(r['y'])))
                continue
            if r['type'] != 'RAT' or r.get('dead') == '1' or len(start) < 2:
                continue
            i = w(int(r['round']))
            p = (int(r['x']), int(r['y']))
            do, de = math.dist(p, start[t]), math.dist(p, start[3 - t])
            st[t]['turns'][i] += 1
            if de < do:
                st[t]['fwd'][i] += 1
            if de <= 6:
                st[t]['nearEK'][i] += 1
        for a in tsv(os.path.join(d, f'g{g}.actions.tsv')):
            t = int(a['team']) if a['team'].isdigit() else 0
            if t not in (1, 2):
                continue
            i = w(int(a['round']))
            k = a['action']
            if k == 'die' and a['type'] == 'RAT':
                st[t]['deaths'][i] += 1
            elif k == 'spawn':
                st[t]['spawns'][i] += 1
            elif k == 'ratTrap':
                st[t]['traps'][i] += 1
            elif k == 'throw':
                st[t]['throws'][i] += 1
            elif k == 'transfer':
                try:
                    st[t]['xfer'][i] += int(a['b'])
                except ValueError:
                    pass
        for t, side in ((1, 'A'), (2, 'B')):
            rec = dict(match=int(mid), game=g, map=s['map'], team=s['team' + side],
                       opp=s['team' + ('B' if side == 'A' else 'A')], won=s['winner'] == side, rounds=s['rounds'],
                       winType=s['winType'], **st[t])
            out.write(json.dumps(rec) + '\n')
    out.flush()
