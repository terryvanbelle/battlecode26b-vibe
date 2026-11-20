"""Per build: replica games on the panel cells (v4-set3, v4-set4, starve1; opponent, maps, our seat) shared with xk6,
won by the build vs by xk6 (latest match of each on the cell; the replica uses one seed per map)."""
import json, sys, collections, bisect
from datetime import datetime
d = json.load(open('build/replica-matches.json'))
subs = sorted((datetime.fromisoformat(t).timestamp(), b) for t, b in d['subs'])
st = [t for t, _ in subs]
def build_at(created):
    i = bisect.bisect_right(st, datetime.fromisoformat(created).timestamp()) - 1
    return subs[i][1] if i >= 0 else None
cells = {}
for p in ['v4-set3', 'v4-set4', 'starve1']:
    for o, m in json.load(open(f'research/panel/{p}.json')).items(): cells[(o, ','.join(m))] = p
best = collections.defaultdict(dict)   # build -> cell -> (id, wins)
for m in d['matches']:
    if m['status'] != 'OK!': continue
    ps = m['participants']
    us = [p for p in ps if p['teamname'] == 'vibe26b']
    if len(us) != 1: continue
    them = [p for p in ps if p['teamname'] != 'vibe26b'][0]
    key = (them['teamname'], ','.join(m['maps']))
    if key not in cells: continue
    b = build_at(m['created'])
    if not b: continue
    seat = 'A' if us[0]['player_index'] == 0 else 'B'
    c = key + (seat,)
    if c not in best[b] or best[b][c][0] < m['id']: best[b][c] = (m['id'], us[0]['score'])
ref = best['xk6']
rows = []
for b, cs in best.items():
    if b == 'xk6': continue
    sh = [c for c in cs if c in ref]
    if len(sh) < 8: continue
    w = sum(cs[c][1] for c in sh); r = sum(ref[c][1] for c in sh)
    rows.append((b, len(sh), w, r))
for b, n, w, r in sorted(rows, key=lambda x: -x[1]):
    print(f'{b:6s} shared matches {n:3d}  games {3*n:3d}  build {w:3d}  xk6 {r:3d}  net {w-r:+d}  per100 {100*(w-r)/(3*n):+.1f}')
