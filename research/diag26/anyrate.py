import json, os, sys, collections
# usage: anyrate.py MINID "IDS" -> for each cell of IDS (opp, map, seat): draws/wins over all /dev/shm/rp ids >= MINID with the same map triple (excluding IDS)
mn = int(sys.argv[1]); ids = [int(x) for x in sys.argv[2].split()]
def cells(i):
    p = '/dev/shm/rp/%d/match.json' % i
    if not os.path.exists(p): return []
    m = json.load(open(p)); out = []
    for g in m['games']:
        us = 'A' if g['team_a'] == 'vibe26b' else 'B'
        opp = g['team_b'] if us == 'A' else g['team_a']
        out.append(((opp, g['map'], us, tuple(sorted(m['maps']))), g['winner_team'] == 'vibe26b'))
    return out
R = collections.defaultdict(list)
for d in os.listdir('/dev/shm/rp'):
    if d.isdigit() and int(d) >= mn and int(d) not in ids:
        for c, w in cells(int(d)): R[c].append((int(d), w))
s = 0.0
for i in ids:
    for c, w in cells(i):
        r = R[c]; rate = sum(x for _, x in r) / max(1, len(r)); s += w - rate
        print(i, '%-14s %-22s %s %s draws %2d rate %.2f last5 %s' % (c[0].split('_', 1)[1][:14], c[1][:22], c[2], 'W' if w else 'L', len(r), rate, ''.join('W' if x else 'L' for _, x in sorted(r)[-5:])))
print('sum(win - rate) %.2f' % s)
