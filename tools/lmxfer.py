# (run from the repo dir on the VM) # lmxfer.py CTRLRUN CTRLBOT RUN BOT: transfer per round, candidate vs control, pooled and on cells whose length ratio
# (cand/ctrl rounds) is within 0.8-1.25x; overruns and exceptions of the candidate
import json, sys, os
C, CB, R, B = sys.argv[1:5]
def load(run, bot):
    out = {}
    for l in open(os.path.join('runs', run, 'results.jsonl')):
        j = json.loads(l); side = 'A' if j['a'] == bot else 'B' if j['b'] == bot else None
        if side is None: continue
        other = j['b'] if side == 'A' else j['a']
        out[(j['map'], side, other, j['seed'])] = (j['cheeseXfer' + side], j['rounds'], j[side]['overruns'], j[side]['exceptions'])
    return out
c, r = load(C, CB), load(R, B)
keys = sorted(set(c) & set(r)); ov = sum(r[k][2] for k in r); ex = sum(r[k][3] for k in r)
def rate(ks, d): return sum(d[k][0] for k in ks) / max(1, sum(d[k][1] for k in ks))
lm = [k for k in keys if 0.8 <= r[k][1] / c[k][1] <= 1.25]
print('cells %d overruns %d exceptions %d | pooled xfer/round %.3fx | length-matched %d cells %.3fx' % (
    len(keys), ov, ex, rate(keys, r) / rate(keys, c), len(lm), rate(lm, r) / rate(lm, c)))
