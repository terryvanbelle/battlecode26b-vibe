#!/usr/bin/env python3
"""Local side: tournament rows where one side is a sparring bot (sw35, sw48). Writes loc.jsonl (same shape as rep.jsonl, 'us' = the
champion-line bot, 'them' = the sparring bot)."""
import glob, json, os
REPO = os.path.expanduser('~/projects/vibe/2026b')
SPAR = {'sw35', 'sw48'}
out = open('loc.jsonl', 'w'); n = 0
for p in sorted(glob.glob(os.path.join(REPO, 'runs/tr*/results.jsonl'))):
    for l in open(p):
        try:
            r = json.loads(l)
        except ValueError:
            continue
        a, b = r.get('a'), r.get('b')
        if (a in SPAR) == (b in SPAR) or 'A' not in r or 'winType' not in r:
            continue
        sp = 'A' if a in SPAR else 'B'; us = 'B' if sp == 'A' else 'A'
        out.write(json.dumps({'src': 'local', 'run': p.split('/')[-2], 'map': r['map'], 'opp': r[sp.lower()],
                              'usBot': r[us.lower()], 'won': r.get('winner') == us, 'winType': r['winType'],
                              'rounds': r['rounds'], 'backstab': r.get('backstabRound'), 'bsBy': r.get('backstabBy'),
                              'us': r[us], 'them': r[sp], 'side': us,
                              'top': {k: v for k, v in r.items() if k not in ('A', 'B')}}) + '\n'); n += 1
print(n)
