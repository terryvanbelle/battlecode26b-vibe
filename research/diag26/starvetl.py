# starvetl.py [PKG]: cached field losses of PKG where our king starved: at T-200/-120/-60/-1 bank, our/their babies, our raw
# held; over [T-200, T]: our spawns (cheese), traps laid, cheese delivered (xfer delta), first round the bank fell under 2
import os, sys, json, glob, csv, collections
ROOT = os.path.expanduser('~/projects/vibe/2026b/build/dumpcache'); PKG = sys.argv[1] if len(sys.argv) > 1 else 'z3x'
seen = set()
for f in sorted(glob.glob(ROOT + '/*/g*.summary.json')):
    s = json.load(open(f))
    if 'vibe26b' not in (s['teamA'], s['teamB']): continue
    key = (s['file'].split('/')[-2], s['game'])
    if key in seen: continue
    m = json.load(open(f.replace('.summary.json', '.meta.json')))
    U = 'A' if s['teamA'] == 'vibe26b' else 'B'; T = 'B' if U == 'A' else 'A'
    if m.get('package' + U) != PKG or s['winner'] == U or s['winType'] != 'RATKING_DESTROYED': continue
    tu = '1' if U == 'A' else '2'
    acts = list(csv.DictReader(open(f.replace('.summary.json', '.actions.tsv')), delimiter='\t'))
    kd = [r for r in acts if r['action'] == 'die' and r['c'] == 'KING' and r['d'] == tu]
    if not kd or kd[-1]['e'] != 'starve': continue
    seen.add(key)
    E = s['rounds']; R = {int(r['round']): r for r in csv.DictReader(open(f.replace('.summary.json', '.rounds.tsv')), delimiter='\t')}
    lo = max(1, E - 200)
    sp = sum(1 for r in acts if r['action'] == 'spawn' and r['team'] == tu and int(r['round']) >= lo)
    spc = 0
    for r in acts:
        if r['action'] == 'spawn' and r['team'] == tu and int(r['round']) >= lo:
            b = int(R.get(int(r['round']) - 1, R[int(r['round'])])['babies' + U]); spc += 10 + 10 * (b // 4)
    tr = sum(1 for r in acts if r['action'] in ('placeRatTrap', 'placeCatTrap', 'ratTrap', 'catTrap') and r['team'] == tu and int(r['round']) >= lo)
    xf = int(R[E]['cheeseXfer' + U]) - int(R[lo]['cheeseXfer' + U])
    zero = next((q for q in range(lo, E + 1) if q in R and int(R[q]['bank' + U]) < 2), None)
    g = lambda q: '%s/%s/%s/%s' % (R[q]['bank' + U], R[q]['babies' + U], R[q]['babies' + T], R[q]['rawCheese' + U]) if q in R else '-'
    print('%-12s %-16s %s r%4d | T-200 %s T-120 %s T-60 %s T-1 %s | sp %d (%d) traps %d xfer %d zero@%s' % (
        s['team' + T][:12], s['map'][:16], U, E, g(lo), g(E - 120), g(E - 60), g(E - 1), sp, spc, tr, xf, zero))
