import json, sys
def load(p, me):
    out = {}
    for l in open(p):
        d = json.loads(l)
        s = 'A' if d['a'].split('/')[-1] == me else 'B'; o = 'B' if s == 'A' else 'A'
        opp = (d['b'] if s == 'A' else d['a']).split('/')[-1]
        out[(opp, d['map'], s)] = d
    return out, None
a, _ = load(sys.argv[1], sys.argv[2]); b, _ = load(sys.argv[3], sys.argv[4]); lim = int(sys.argv[5])
def side(d, me): return 'A' if d['a'].split('/')[-1] == me else 'B'
n = 0; ca = cb = 0; pa = pb = 0; np_ = 0; ta = tb = 0; bsa = bsb = 0
for k in a:
    if k not in b: continue
    da, db = a[k], b[k]
    sa, sb = side(da, sys.argv[2]), side(db, sys.argv[4])
    if da['rounds'] >= lim or db['rounds'] >= lim:
        n += 1
        ca += da[sa]['dmgCat']; cb += db[sb]['dmgCat']
        oa, ob = ('B' if sa == 'A' else 'A'), ('B' if sb == 'A' else 'A')
        sha = da['catDmg' + sa] / max(1, da['catDmg' + sa] + da['catDmg' + oa]); shb = db['catDmg' + sb] / max(1, db['catDmg' + sb] + db['catDmg' + ob])
        pa += sha; pb += shb
    ta += da[sa].get('catTraps', 0); tb += db[sb].get('catTraps', 0)
print('shared cells', len(set(a) & set(b)), '| games reaching r%d: %d; our dmgCat ctrl %d cand %d; mean cat share ctrl %.3f cand %.3f | catTraps all cells ctrl %d cand %d' % (lim, n, ca, cb, pa / max(1, n), pb / max(1, n), ta, tb))
