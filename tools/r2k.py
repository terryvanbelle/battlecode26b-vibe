import sys, json, glob, os, collections
# replica r2000 points games in dumps: components at the end (our share minus theirs, in points)
rows = []
for s in glob.glob('/dev/shm/dcx/*/g*.summary.json'):
    if '/km' in s: continue
    try: j = json.load(open(s))
    except Exception: continue
    if 'vibe26b' not in (j.get('teamA'), j.get('teamB')) or j.get('rounds') != 2000: continue
    us = 'A' if j['teamA'] == 'vibe26b' else 'B'; th = 'B' if us == 'A' else 'A'
    opp = j['team' + th]
    rt = s.replace('.summary.json', '.rounds.tsv')
    if not os.path.exists(rt): continue
    hdr = None; last = None
    for line in open(rt):
        f = line.rstrip('\n').split('\t')
        if hdr is None: hdr = f; continue
        last = f
    d = dict(zip(hdr, last))
    def sh(k):
        a, b = int(d[k + us] or 0), int(d[k + th] or 0)
        return (a / (a + b) if a + b else 0.5)
    coop = j.get('backstabRound', -1) in (-1, None, 0) and False
    bs = j.get('backstabRound')
    cw, kw = (0.3, 0.5) if bs and bs > 0 else (0.5, 0.3)
    k = (int(d['numKings' + us]), int(d['numKings' + th]))
    pts = lambda c, kk, x: int(cw * 100 * c + kw * 100 * kk + 20 * x)
    cs, ks, xs = sh('catDmg'), (k[0] / sum(k) if sum(k) else 0), sh('cheeseXfer')
    won = j['winner'] == us
    rows.append((won, opp, j['map'], us, bs, k, round(cs, 2), round(ks, 2), round(xs, 2)))
print(len(rows), 'games;', sum(r[0] for r in rows), 'won')
c = collections.Counter()
for r in sorted(rows, key=lambda r: (r[0], r[1])):
    if not r[0]:
        print('L', r[1][:22], r[2], r[3], 'bs', r[4], 'kings', r[5], 'cat', r[6], 'kshare', r[7], 'xfer', r[8])
    c[('W' if r[0] else 'L', 'kings ' + ('<' if r[5][0] < r[5][1] else '=' if r[5][0] == r[5][1] else '>'))] += 1
print(sorted(c.items()))
