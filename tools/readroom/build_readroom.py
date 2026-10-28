#!/usr/bin/env python3
"""Build ~/projects/vibe/reference/readroom-no2026: every .md/.txt of the prior-year repos with 2026 blocks removed.
battlecode26-vibe is never a source (clean room)."""
import os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from filter2026 import filt, TAG
V = os.path.expanduser('~/projects/vibe')
SRC = {'bc22': 'reference/battlecode22-vibe', 'bc25': 'reference/battlecode25-vibe', 'bc21': 'reference/battlecode21-vibe',
       'bc20': 'reference/battlecode20-vibe', 'bc24': 'reference/battlecode24-vibe', 'bc23': '2023', 'bc19': '2019',
       'advice': 'reference/battlecode-vibe', 'bcenv': 'reference/bcenv'}
SKIP = {'.git', '.venv', 'node_modules', 'gauntlet', 'matches', 'build', 'site-packages', '__pycache__', 'benchmarks'}
RR = sys.argv[1] if len(sys.argv) > 1 else f'{V}/reference/readroom-no2026'
tot = hits = 0
for k, s in SRC.items():
    root = f'{V}/{s}'
    for d, dirs, files in os.walk(root):
        dirs[:] = [x for x in dirs if x not in SKIP]
        for f in files:
            if not (f.endswith('.md') or f.endswith('.txt')): continue
            p = os.path.join(d, f)
            if os.path.getsize(p) > 2_000_000: continue
            out = os.path.join(RR, k, os.path.relpath(p, root))
            os.makedirs(os.path.dirname(out), exist_ok=True)
            t = filt(open(p, errors='replace').read())
            open(out, 'w').write(t); tot += 1; hits += len(TAG.findall(t))
print(f'files {tot}, residual hits {hits}')
