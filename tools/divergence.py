"""Where does a candidate play differently from the champion? Local games are deterministic per cell, so a cell whose
outcome fingerprint (winner, rounds, every per-team stat) matches the champion's on the same cell was almost surely
played identically. Per map: share of shared local cells that diverge. With --panel, lists each panel match
(opponent, maps, seat) and whether any of its maps diverged locally: a match on maps with no local divergence is
predicted to repeat the champion's replica result, so its replica games can be skipped.
usage: divergence.py CTRL_RUN CTRL_BOT RUN BOT [--panel]   (runs/RUN/results.jsonl)"""
import json, sys, collections, re
SKIP = {'a', 'b', 'a_hash', 'b_hash', 'a_spec', 'b_spec', 'wall', 'seed'}
BC = re.compile(r'(meanBc|maxBc|near90|overrun|Bc)')   # bytecode use changes with any code edit; not play
def clean(r):
    if isinstance(r, dict): return {k: clean(v) for k, v in r.items() if k not in SKIP and not BC.search(k)}
    return r
def load(run, bot):
    d = {}
    for l in open(f'runs/{run}/results.jsonl'):
        r = json.loads(l); me = 'A' if r['a'] == bot else 'B'; op = r['b'] if me == 'A' else r['a']
        fp = json.dumps(clean(r), sort_keys=True)
        d[(op.split('/')[-1], r['map'], me)] = fp
    return d
def per_map(cr, cb, r, b):
    c, x = load(cr, cb), load(r, b)
    m = collections.defaultdict(lambda: [0, 0])
    for k in x:
        if k in c:
            m[k[1]][0] += 1; m[k[1]][1] += x[k] != c[k]
    return m
if __name__ == '__main__':
    args = [a for a in sys.argv[1:] if not a.startswith('--')]
    m = per_map(*args)
    tot = sum(v[0] for v in m.values()); dv = sum(v[1] for v in m.values())
    print(f'{args[3]} vs {args[1]}: {dv}/{tot} cells diverge; maps with any divergence {sum(1 for v in m.values() if v[1])}/{len(m)}')
    if '--panel' in sys.argv:
        for f in ('v4-set3', 'v4-set4', 'starve1'):
            p = json.load(open(f'research/panel/{f}.json')); n = live = 0
            for opp, maps in p.items():
                d = [mp for mp in maps if m.get(mp, [0, 0])[1]]
                n += 1; live += bool(d)
                print(f'  {f:8s} {opp:34s} {",".join(maps):40s} {"DIVERGES on " + ",".join(d) if d else "same"}')
            print(f'  {f}: {live}/{n} opponents have a diverging map (x2 seats)')
