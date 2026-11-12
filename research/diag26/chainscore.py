# chainscore.py DIR CAND CTRL FIELD...: paired chain cells; wins, cells lost/gained, and our side's summed stat FIELDs
import sys, glob, json, os
d, cand, ctrl, fields = sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4:]
def log(p):
    t = open(p).read().strip().split('\n')[-1]
    try: return json.loads(t)
    except ValueError: return None
R = {}
for p in sorted(glob.glob(d + '/*.log')):
    n = os.path.basename(p)[:-4]; r = log(p)
    if not r or 'winner' not in r: continue
    bot = n.rsplit('-', 1)[1]; us = 'A' if r['a'] == bot else 'B'
    R[n] = (r['winner'] == us, r['rounds'], r[us])
cells = sorted({n.rsplit('-', 1)[0] for n in R})
wa = wb = lost = gained = 0; S = {f: [0, 0] for f in fields}; done = 0
for c in cells:
    a, b = R.get(c + '-' + cand), R.get(c + '-' + ctrl)
    if not a or not b: print(c.ljust(26), 'incomplete'); continue
    done += 1; wa += a[0]; wb += b[0]
    for f in fields: S[f][0] += a[2].get(f, 0); S[f][1] += b[2].get(f, 0)
    tag = 'LOST-CELL' if b[0] and not a[0] else 'gained' if a[0] and not b[0] else ''
    lost += tag == 'LOST-CELL'; gained += tag == 'gained'
    print(c.ljust(26), cand, 'W' if a[0] else 'L', a[1], ' '.join(f'{f}={a[2].get(f)}' for f in fields), '|', ctrl, 'W' if b[0] else 'L', b[1], ' '.join(f'{f}={b[2].get(f)}' for f in fields), tag)
print('paired cells', done, cand, 'wins', wa, ctrl, 'wins', wb, 'lost', lost, 'gained', gained, '|', ' '.join(f'{f} {v[0]}/{v[1]}={v[0] / max(v[1], 1):.3f}' for f, v in S.items()))
