from data import *
import statistics as S, math
t = n3_totals()
B = [b for b in RUNS if b in t and b != 'xr76']
y = {b: t[b][0] for b in B}
L = {b: local(b, RUNS[b]) for b in B}
common = set.intersection(*[set(v) for v in L.values()])
print('builds', len(B), 'common cells', len(common))
def r(x, yy):
    mx, my = S.mean(x), S.mean(yy)
    sx = math.sqrt(sum((a-mx)**2 for a in x)); sy = math.sqrt(sum((b-my)**2 for b in yy))
    return sum((a-mx)*(b-my) for a, b in zip(x, yy)) / (sx*sy) if sx and sy else float('nan')
Y = [y[b] for b in B]
feats = {}
feats['wins_total'] = {b: sum(L[b][c]['win'] for c in common) for b in B}
for arch in sorted({c[0] for c in common}):
    feats['wins_'+arch] = {b: sum(L[b][c]['win'] for c in common if c[0]==arch) for b in B}
feats['mean_rounds_won'] = {b: S.mean(L[b][c]['rounds'] for c in common if L[b][c]['win']) for b in B}
feats['xfer_ratio'] = {b: sum(L[b][c]['xfer_me'] for c in common)/max(1,sum(L[b][c]['xfer_them'] for c in common)) for b in B}
feats['xfer_me'] = {b: S.mean(L[b][c]['xfer_me'] for c in common) for b in B}
for k, f in feats.items():
    X = [f[b] for b in B]
    print(f'{k:22s} r {r(X, Y):+.2f}  range {min(X):.1f}-{max(X):.1f}')
print('per-arch losses:', {a: [42-feats["wins_"+a][b] for b in B] for a in sorted({c[0] for c in common})})
