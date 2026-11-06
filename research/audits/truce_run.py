#!/usr/bin/env python3
"""Mechanism run for a truce switch (z0g's zd96): candidate and control in one run, against the same opponents.
  truce_run.py RESULTS.jsonl CAND CTRL [FROM=100]
Per (opponent, map, side): each bot's result and who opened the war when; then wins, and the candidate's openings at
round FROM or later (its falsifier: any)."""
import json, sys

res, cand, ctrl = sys.argv[1], sys.argv[2], sys.argv[3]
frm = int(sys.argv[4]) if len(sys.argv) > 4 else 100
cell = {}
for line in open(res):
    r = json.loads(line)
    for me in (cand, ctrl):
        if me not in (r['a'], r['b']):
            continue
        side = 'A' if r['a'] == me else 'B'
        opp = r['b'] if side == 'A' else r['a']
        by = r.get('backstabBy') or 0
        who = '-' if (r.get('backstabRound') or -1) < 0 else 'us' if ' AB'[by] == side else 'them'
        cell.setdefault((opp, r['map'], side), {})[me] = (r['winner'] == side, r.get('backstabRound'), who, r.get('rounds'))
wins = {cand: 0, ctrl: 0}
late = 0
n = 0
for k in sorted(cell):
    v = cell[k]
    if cand not in v or ctrl not in v:
        continue
    n += 1
    for me in (cand, ctrl):
        wins[me] += v[me][0]
    if v[cand][2] == 'us' and v[cand][1] >= frm:
        late += 1
    f = lambda t: '%s r%s bs %s %s' % ('W' if t[0] else 'L', t[3], t[1], t[2])
    print('%-6s %-18s %s | %-6s %s | %-6s %s' % (k[0], k[1][:18], k[2], cand, f(v[cand]), ctrl, f(v[ctrl])))
print('cells', n, 'wins', cand, wins[cand], ctrl, wins[ctrl], '| %s opened the war at r%d+ in %d' % (cand, frm, late))
