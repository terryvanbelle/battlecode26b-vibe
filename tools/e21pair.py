#!/usr/bin/env python3
"""Interleaved e21 panels (owner 11-20): champion vs challenger, cell by cell, on cells (opponent, 3 maps, our seat)
both builds played. Replica games are deterministic, so a shared cell compares exactly.

  e21pair.py CHAMP CHAL [--panel e21|n3] [--json build/replica-matches.json]
Refresh the match file first: python3 tools/games.py replica.  Builds are attributed by submission time (d['subs']).
Prints per-opponent and total games won on shared cells, cells each build still lacks, and the challenger's verdict:
ACCEPT when it ties or beats the champion with every cell played, REJECT once winning all its remaining cells (all
their games) cannot reach a tie.
"""
import bisect, collections, json, os, sys
from datetime import datetime

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
args = sys.argv[1:]
path = os.path.join(REPO, 'build', 'replica-matches.json')
if '--json' in args:
    i = args.index('--json'); path = args[i + 1]; del args[i:i + 2]
panel = 'e21'
if '--panel' in args:
    i = args.index('--panel'); panel = args[i + 1]; del args[i:i + 2]
champ, chal = args[:2]
d = json.load(open(path))
subs = sorted((datetime.fromisoformat(t).timestamp(), b) for t, b in d['subs'])
st = [t for t, _ in subs]


def build_at(created):
    i = bisect.bisect_right(st, datetime.fromisoformat(created).timestamp()) - 1
    return subs[i][1] if i >= 0 else None


cells = set()
import glob
for f in sorted(glob.glob(os.path.join(REPO, 'research', 'panel', f'{panel}-[0-9]*.json'))):
    for o, m in json.load(open(f)).items():
        cells.add((o, ','.join(m)))
nmaps = {(o, m): len(m.split(',')) for o, m in cells}
res = collections.defaultdict(dict)   # build -> (opp, maps, seat) -> (id, games won)
for m in d['matches']:
    if m['status'] != 'OK!':
        continue
    ps = m['participants']
    us = [p for p in ps if p['teamname'] == 'vibe26b']
    if len(us) != 1:
        continue
    them = [p for p in ps if p['teamname'] != 'vibe26b'][0]
    key = (them['teamname'], ','.join(m['maps']))
    if key not in cells:
        continue
    b = build_at(m['created'])
    if b not in (champ, chal):
        continue
    c = key + ('A' if us[0]['player_index'] == 0 else 'B',)
    if c not in res[b] or res[b][c][0] < m['id']:
        res[b][c] = (m['id'], us[0]['score'])
A, B = res[champ], res[chal]
shared = sorted(set(A) & set(B))
byopp = collections.defaultdict(lambda: [0, 0, 0])
for c in shared:
    o = byopp[c[0]]; o[0] += 1; o[1] += A[c][1]; o[2] += B[c][1]
for opp, (n, a, b) in sorted(byopp.items()):
    print(f'{opp:32s} cells {n:2d}  {champ} {a:2d}  {chal} {b:2d}  {b - a:+d}')
wa = sum(A[c][1] for c in shared); wb = sum(B[c][1] for c in shared)
total = 2 * len(cells)
left_b = total - len(B)
print(f'{champ} played {len(A)}/{total} cells, {chal} {len(B)}/{total}; shared {len(shared)} '
      f'({sum(nmaps[c[:2]] for c in shared)} games): {champ} {wa}  {chal} {wb}  net {wb - wa:+d}')
# Upper bound on the final net (challenger minus champion over every cell): a cell only the challenger played adds at
# most its wins (champion >= 0); one only the champion played at most its games - champion; one neither, all its games.
allc = {(o, m, s) for o, m in cells for s in 'AB'}
best = sum((B[c][1] if c in B else nmaps[c[:2]]) - (A[c][1] if c in A else 0) for c in allc)
if len(A) == total and len(B) == total:
    print('VERDICT', 'ACCEPT' if wb >= wa else 'REJECT', '(complete)')
elif best < 0:
    print(f'VERDICT REJECT (best possible {best:+d})')
else:
    print(f'VERDICT open (best possible {best:+d})')
