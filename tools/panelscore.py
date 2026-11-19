#!/usr/bin/env python3
"""Running score of a panel run on the replica: games won per panel for the build's matches (ids > START), each
compared with the latest earlier match on the same cell (opponent, maps, our seat).

  panelscore.py START [PANEL.json ...] [--pages N] [--ref LO:HI]     defaults: v4-set3, v4-set4, starve1
  --ref LO:HI: compare only with earlier matches whose ids are in LO..HI (the champion's own panel, e.g. xk6 27474:27718)
"""
import json, os, re, subprocess, sys

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
args = sys.argv[1:]
rlo, rhi = 0, 10 ** 9
if '--ref' in args:
    i = args.index('--ref'); rlo, rhi = map(int, args[i + 1].split(':')); del args[i:i + 2]
pages = '40'
if '--pages' in args:
    i = args.index('--pages'); pages = args[i + 1]; del args[i:i + 2]
start = int(args[0])
panels = args[1:] or ['v4-set3', 'v4-set4', 'starve1']
cells = {}
for p in panels:
    for opp, maps in json.load(open(os.path.join(REPO, 'research', 'panel', p + '.json'))).items():
        cells[(opp, ','.join(maps))] = p
out = subprocess.run([sys.executable, '-I', os.path.join(REPO, 'tools', 'galaxy26', 'contest.py'), 'matches', '--pages', pages],
                     capture_output=True, text=True).stdout
pat = re.compile(r'^\s*(\d+) OK!\s+\w+\s+(\S+) (\d) - (\S+) (\d)\s+maps=(\S+)')
ours, ref = {}, {}
for line in out.splitlines():
    m = pat.match(line)
    if not m:
        continue
    mid, a, sa, b, sb, maps = int(m[1]), m[2], int(m[3]), m[4], int(m[5]), m[6]
    if 'vibe26b' not in (a, b):
        continue
    seat, opp, w = ('A', b, sa) if a == 'vibe26b' else ('B', a, sb)
    p = cells.get((opp, maps))
    if p is None:
        continue
    key = (p, opp, seat)
    if mid > start:
        ours[key] = (mid, w)
    elif rlo <= mid <= rhi and (key not in ref or ref[key][0] < mid):
        ref[key] = (mid, w)
tot = [0, 0, 0, 0]
for p in panels:
    keys = [k for k in ours if k[0] == p]
    w = sum(ours[k][1] for k in keys); n = 3 * len(keys)
    rk = [k for k in keys if k in ref]
    rw = sum(ref[k][1] for k in rk); cw = sum(ours[k][1] for k in rk)
    want = sum(1 for k in cells.values() if k == p) * 2
    print(f'{p:8s} matches {len(keys):2d}/{want:2d}  games {w:3d}/{n:3d}  | on {len(rk)} cells with an earlier match: now {cw} vs before {rw} ({cw - rw:+d})')
    tot = [tot[0] + w, tot[1] + n, tot[2] + cw, tot[3] + rw]
print(f'total    games {tot[0]}/{tot[1]}  | vs earlier same cells: {tot[2]} vs {tot[3]} ({tot[2] - tot[3]:+d})')
