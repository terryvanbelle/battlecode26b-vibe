import json, sys
def load(p, me):
    out = {}
    for l in open(p):
        d = json.loads(l); s = 'A' if d['a'].endswith('/' + me) else 'B'
        out[(d['b'] if s == 'A' else d['a'], d['map'], s)] = (d['rounds'], d[s]['transferAmt'], d[s]['turnsRat'], d['winner'] == s)
    return out
a = load(sys.argv[1], sys.argv[2]); b = load(sys.argv[3], sys.argv[4]); lim = int(sys.argv[5])
same = diff = 0; early_same = early_diff = 0
for k in a:
    if k not in b: continue
    eq = a[k] == b[k]
    same += eq; diff += not eq
    if a[k][0] < lim:
        early_same += eq; early_diff += not eq
print('cells', same + diff, 'identical', same, 'differ', diff, '| ctrl games ending before r%d: identical %d differ %d' % (lim, early_same, early_diff))
