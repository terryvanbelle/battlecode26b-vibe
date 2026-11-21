"""Per-build mean of every numeric local-game metric (own side 'me.*', opponent side 'op.*') vs n3 total; r and LOO-stable r."""
import json, os, math, statistics as S, random
from data import R, MAPS, RUNS, n3_totals
t = n3_totals()
B = [b for b in RUNS if b in t and b != 'xr76']
def flat(d, pre):
    out = {}
    for k, v in d.items():
        if isinstance(v, dict): out.update(flat(v, pre + k + '.'))
        elif isinstance(v, (int, float)) and not isinstance(v, bool): out[pre + k] = v
    return out
def games(b):
    for line in open(f'{R}/runs/{RUNS[b]}/results.jsonl'):
        if not line.startswith('{'): continue
        g = json.loads(line)
        if g['map'] not in MAPS or b not in (g['a'], g['b']): continue
        me = 'A' if g['a'] == b else 'B'; op = 'B' if me == 'A' else 'A'
        f = {'win': int(g['winner'] == me), 'rounds': g['rounds'], 'pts.me': g.get('pts' + me, 0), 'pts.op': g.get('pts' + op, 0)}
        for k in ('cheeseXfer', 'catDmg', 'kings', 'babies', 'numKings', 'bank'):
            f[k + '.me'] = g.get(k + me, 0); f[k + '.op'] = g.get(k + op, 0)
        f.update(flat(g.get(me, {}), 'me.')); f.update(flat(g.get(op, {}), 'op.'))
        f['wonEarly'] = int(f['win'] and g['rounds'] < 500)
        yield f
F = {}
for b in B:
    gs = list(games(b)); keys = set().union(*gs)
    F[b] = {k: S.mean(g.get(k, 0) for g in gs) for k in keys}
keys = sorted(set.intersection(*[set(v) for v in F.values()]))
Y = [t[b][0] for b in B]
def r(x, y):
    mx, my = S.mean(x), S.mean(y); sx = math.sqrt(sum((a-mx)**2 for a in x)); sy = math.sqrt(sum((c-my)**2 for c in y))
    return sum((a-mx)*(c-my) for a, c in zip(x, y)) / (sx*sy) if sx > 1e-12 and sy else 0
res = []
for k in keys:
    X = [F[b][k] for b in B]
    rr = r(X, Y)
    loo = [r([x for j, x in enumerate(X) if j != i], [y for j, y in enumerate(Y) if j != i]) for i in range(len(B))]
    res.append((abs(rr), rr, min(loo, key=abs), k))
# null: max |r| over all features under shuffled targets
random.seed(1); mx = []
for _ in range(300):
    Ys = Y[:]; random.shuffle(Ys)
    mx.append(max(abs(r([F[b][k] for b in B], Ys)) for k in keys))
mx.sort()
print(f'{len(B)} builds, {len(keys)} metrics; max |r| under shuffled targets: median {mx[150]:.2f}, 95th pct {mx[285]:.2f}')
for a, rr, lo, k in sorted(res, reverse=True)[:20]:
    print(f'{k:28s} r {rr:+.2f}  worst-LOO {lo:+.2f}')
