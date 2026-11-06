"""For field r2000 losses in the block jsonl files: which single lever flips the points result."""
import glob, json, sys
seen = {}
for f in sorted(glob.glob('/home/terryvanbelle/diag26b/*.jsonl')):
    if 'rp_games' in f or 'ctl' in f: continue
    for l in open(f):
        try: d = json.loads(l)
        except Exception: continue
        k = (d.get('match'), d.get('game'))
        seen[k] = d
def pts(cu, ct, ku, kt, xu, xt, war):
    wc, wk, wx = (0.3, 0.5, 0.2) if war else (0.5, 0.3, 0.2)
    def sh(a, b, w): return int(100 * w * a / (a + b)) if a + b > 0 else 0
    return sh(cu, ct, wc) + sh(ku, kt, wk) + sh(xu, xt, wx), sh(ct, cu, wc) + sh(kt, ku, wk) + sh(xt, xu, wx)
n = 0; tally = {}
rows = []
for k, d in seen.items():
    if d.get('rounds') != 2000 or d.get('won'): continue
    if d.get('end') not in (None, 'points') and d.get('type') not in ('MORE_POINTS',): continue
    war = d.get('bs') is not None
    cu, ct, ku, kt, xu, xt = d['catUs'], d['catThem'], d['kUs'], d['kThem'], d['xUs'], d['xThem']
    pu, pt = pts(cu, ct, ku, kt, xu, xt, war)
    n += 1
    lev = []
    def win(a, b): return a > b
    if win(*pts(cu, ct, ku + 1, kt, xu, xt, war)): lev.append('K+1')
    if kt > 0 and win(*pts(cu, ct, ku, kt - 1, xu, xt, war)): lev.append('T-1')
    if win(*pts(cu, ct, ku + 2, kt, xu, xt, war)) and 'K+1' not in lev: lev.append('K+2')
    for f in (1.1, 1.25, 1.5):
        if win(*pts(cu * f, ct, ku, kt, xu, xt, war)): lev.append('C%g' % f); break
    for f in (1.1, 1.25, 1.5):
        if win(*pts(cu, ct, ku, kt, xu * f, xt, war)): lev.append('X%g' % f); break
    for l in lev: tally[l] = tally.get(l, 0) + 1
    if not lev: tally['none'] = tally.get('none', 0) + 1
    rows.append((d['opp'][:14], d['map'][:16], d['us'], 'W' if war else 'C', pu, pt, ku, kt, d.get('kMaxUs'), d.get('kMaxThem'), d.get('bankUs'), d.get('bankThem'), round(cu / max(1, cu + ct), 2), round(xu / max(1, xu + xt), 2), ' '.join(lev), d['match'], d.get('pkg')))
print('r2000 losses', n, 'of', len(seen), 'games;', tally)
for r in sorted(rows): print(*r, sep='\t')
