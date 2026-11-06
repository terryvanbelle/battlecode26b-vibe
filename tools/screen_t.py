#!/usr/bin/env python3
"""screen_t.py CTRL_RUN CTRL_BOT RUN BOT (on the VM, repo root): the TRAINING_ALGORITHM s6 screen numbers on the cells
the run shares with its control: paired t on per-cell win/loss (d in {-1, 0, +1}) and picks/100 rat-turns (cheese
picked up per 100 baby turns), pooled and as a per-cell paired difference with its standard error and z."""
import json, math, sys

def load(path, me):
    out = {}
    for line in open(path):
        r = json.loads(line)
        side = 'A' if r['a'] == me else 'B'
        opp = r['b'] if side == 'A' else r['a']
        s = r[side]
        out[(opp, r['map'], side)] = ((r['winner'] == 'A') == (side == 'A'), s.get('pickupAmt', 0), s.get('turnsRat', 0))
    return out

c = load('runs/%s/results.jsonl' % sys.argv[1], sys.argv[2])
d = load('runs/%s/results.jsonl' % sys.argv[3], sys.argv[4])
k = sorted(set(c) & set(d))
n = len(k)
x = [int(d[i][0]) - int(c[i][0]) for i in k]
D = sum(1 for v in x if v)
m = sum(x) / n
var = (D - n * m * m) / (n - 1) if n > 1 else 0
t = m / math.sqrt(var / n) if var > 0 else 0.0
pc = 100.0 * sum(c[i][1] for i in k) / max(1, sum(c[i][2] for i in k))
pd = 100.0 * sum(d[i][1] for i in k) / max(1, sum(d[i][2] for i in k))
# per-cell picks/100 difference: its mean and standard error
dd = [100.0 * d[i][1] / max(1, d[i][2]) - 100.0 * c[i][1] / max(1, c[i][2]) for i in k]
mm = sum(dd) / n
se = math.sqrt(sum((v - mm) ** 2 for v in dd) / (n - 1) / n)
print('%s vs %s: n %d D %d net %+d t %+.2f | picks/100 %.2f vs %.2f (pooled diff %+.2f; per-cell mean diff %+.2f se %.2f, z %+.2f)'
      % (sys.argv[4], sys.argv[2], n, D, sum(x), t, pd, pc, pd - pc, mm, se, mm / se if se else 0))
