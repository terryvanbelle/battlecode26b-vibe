#!/usr/bin/env python3
"""Self-play predictor check (owner 11-21): each n3-tested build's results vs a past champion (xi9 by default) on the
21 maps x 2 seats against its n3 panel total (and per n3 team). Prints wins, correlations, a shuffled-target null for
the win count, and the same metric scan as metrics.py.
  python3 tools/pred/selfplay.py [--opp xi9] [--runs-root DIR]"""
import json, sys, os, math, random, statistics as S
sys.path.insert(0, os.path.dirname(__file__))
from data import R, MAPS, n3_totals
def flat(d, pre):
    out = {}
    for k, v in d.items():
        if isinstance(v, dict): out.update(flat(v, pre + k + '.'))
        elif isinstance(v, (int, float)) and not isinstance(v, bool): out[pre + k] = v
    return out
args = sys.argv[1:]
opp = args[args.index('--opp') + 1] if '--opp' in args else 'xi9'
root = args[args.index('--runs-root') + 1] if '--runs-root' in args else R
RUNS = {'xk6': 'zd737', 'xr19': 'zd837', 'xr25': 'zd838', 'xr39': 'zd839', 'xj9': 'zd840', 'xr45': 'zd841', 'xr47': 'zd842',
        'xr49': 'zd843', 'xr53': 'zd844', 'xr59': 'zd845', 'xr75': 'zd846', 'xr76': 'zd847', 'xr55': 'zd848'}
t = n3_totals()
def games(b, run):
    p = f'{root}/runs/{run}/results.jsonl'
    if not os.path.exists(p): return []
    out = []
    for line in open(p):
        if not line.startswith('{'): continue
        g = json.loads(line)
        if g['map'] not in MAPS or {g['a'], g['b']} != {b, opp}: continue
        me = 'A' if g['a'] == b else 'B'; op = 'B' if me == 'A' else 'A'
        f = {'win': int(g['winner'] == me), 'rounds': g['rounds'], 'pts.me': g.get('pts' + me, 0), 'pts.op': g.get('pts' + op, 0)}
        for k in ('cheeseXfer', 'catDmg', 'kings', 'babies', 'numKings', 'bank'):
            f[k + '.me'] = g.get(k + me, 0); f[k + '.op'] = g.get(k + op, 0)
        f.update(flat(g.get(me, {}), 'me.')); f.update(flat(g.get(op, {}), 'op.'))
        out.append(f)
    return out
G = {b: games(b, r) for b, r in RUNS.items() if b in t}
B = [b for b in G if len(G[b]) == 42]
print(f'builds with all 42 games vs {opp}: {len(B)} of {len(G)}  ({", ".join(f"{b}:{len(G[b])}" for b in G if len(G[b]) < 42)})')
def r(x, y):
    mx, my = S.mean(x), S.mean(y); sx = math.sqrt(sum((a-mx)**2 for a in x)); sy = math.sqrt(sum((c-my)**2 for c in y))
    return sum((a-mx)*(c-my) for a, c in zip(x, y)) / (sx*sy) if sx > 1e-12 and sy > 1e-12 else 0
if len(B) < 4: sys.exit()
W = {b: sum(g['win'] for g in G[b]) for b in B}; Y = [t[b][0] for b in B]
for b in sorted(B, key=lambda b: -t[b][0]): print(f'  {b:5s} n3 {t[b][0]:3d}   wins vs {opp} {W[b]:2d}/42')
rw = r([W[b] for b in B], Y)
random.seed(1); null = sorted(abs(r([W[b] for b in B], random.sample(Y, len(Y)))) for _ in range(2000))
print(f'wins vs n3 total: r {rw:+.2f}  (shuffled |r| 95th pct {null[1900]:.2f}, p ~ {sum(x >= abs(rw) for x in null)/2000:.3f})')
for team in sorted({k for b in B for k in t[b][1]}):
    print(f'  vs {team:24s} r {r([W[b] for b in B], [t[b][1].get(team, 0) for b in B]):+.2f}')
F = {b: {k: S.mean(g.get(k, 0) for g in G[b]) for k in set().union(*G[b])} for b in B}
keys = sorted(set.intersection(*[set(v) for v in F.values()]))
res = sorted(((abs(r([F[b][k] for b in B], Y)), k) for k in keys), reverse=True)
nullmax = sorted(max(abs(r([F[b][k] for b in B], Ys)) for k in keys) for Ys in (random.sample(Y, len(Y)) for _ in range(200)))
print(f'{len(keys)} metrics: best |r| {res[0][0]:.2f} ({res[0][1]}); shuffled max |r| median {nullmax[100]:.2f}, 95th {nullmax[190]:.2f}')
for a, k in res[:8]: print(f'  {k:28s} r {r([F[b][k] for b in B], Y):+.2f}')
