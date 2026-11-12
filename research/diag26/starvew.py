# starvew.py PKG: cached field losses of PKG ending by our king starving: windows before the end (bank, babies, raw held,
# transfer and pickups in the window, ours/theirs), to see whether starving games lack income or hold it undelivered
import os, sys, json, glob, csv
ROOT = os.path.expanduser('~/projects/vibe/2026b/build/dumpcache'); PKG = sys.argv[1]
seen = set()
for f in sorted(glob.glob(ROOT + '/*/g*.summary.json')):
    s = json.load(open(f))
    if 'vibe26b' not in (s['teamA'], s['teamB']): continue
    key = (s['file'].split('/')[-2], s['game'])
    if key in seen: continue
    m = json.load(open(f.replace('.summary.json', '.meta.json')))
    U = 'A' if s['teamA'] == 'vibe26b' else 'B'; T = 'B' if U == 'A' else 'A'
    if m.get('package' + U) != PKG or s['winner'] == U: continue
    seen.add(key); tu = '1' if U == 'A' else '2'
    starve = False
    for r in csv.DictReader(open(f.replace('.summary.json', '.actions.tsv')), delimiter='\t'):
        if r['action'] == 'die' and r['c'] == 'KING' and r['d'] == tu and r['e'] == 'starve': starve = True
    if not starve: continue
    R = {}
    for r in csv.DictReader(open(f.replace('.summary.json', '.rounds.tsv')), delimiter='\t'): R[int(r['round'])] = r
    E = max(R); pts = [E - 300, E - 200, E - 120, E - 60, E - 1]
    out = []
    for i, p in enumerate(pts):
        if p < 1: continue
        r = R[p]; q = R[max(1, pts[i - 1])] if i else R[max(1, p - 100)]
        out.append('r%d bk %d/%d bb %s/%s raw %s/%s x+%d/%d pk+%d/%d' % (p, int(r['bank' + U]), int(r['bank' + T]), r['babies' + U], r['babies' + T], r['rawCheese' + U], r['rawCheese' + T],
            int(r['cheeseXfer' + U]) - int(q['cheeseXfer' + U]), int(r['cheeseXfer' + T]) - int(q['cheeseXfer' + T]), int(r['picked' + U]) - int(q['picked' + U]), int(r['picked' + T]) - int(q['picked' + T])))
    print(s['team' + T][:10], s['map'][:14], U, 'E', E, 'mapCheese', R[E]['mapCheese']); print('   ' + ' | '.join(out))
