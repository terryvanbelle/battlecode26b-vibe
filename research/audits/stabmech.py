import json, sys
def load(p, me):
    out = {}
    for l in open(p):
        d = json.loads(l); s = 'A' if d['a'].split('/')[-1] == me else 'B'
        out[((d['b'] if s == 'A' else d['a']).split('/')[-1], d['map'], s)] = (d, s)
    return out
a = load(sys.argv[1], sys.argv[2]); b = load(sys.argv[3], sys.argv[4])
n = ta = tb = ca = cb = 0
for k in set(a) & set(b):
    (da, sa), (db, sb) = a[k], b[k]
    # the opponent backstabbed in the control game or the candidate game (backstabBy is 1 for A, 2 for B)
    them = lambda d, s: d.get('backstabBy') == (2 if s == 'A' else 1)
    if them(da, sa) or them(db, sb):
        n += 1; ta += da[sa]['catTraps']; tb += db[sb]['catTraps']; ca += da[sa]['catDmgTrap']; cb += db[sb]['catDmgTrap']
print('games the opponent backstabbed: %d; our catTraps ctrl %d cand %d; our catDmgTrap ctrl %d cand %d' % (n, ta, tb, ca, cb))
