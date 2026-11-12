# starveraw.py IDSPEC...: z3x starvation losses: babies' raw cheese (rounds.tsv rawCheese) and bank 100/50/0 rounds before the end, live babies
import sys, os, json, glob, csv
ROOT = os.path.expanduser('~/projects/vibe/2026b/build/dumpcache')
ids = set()
for a in sys.argv[1:]:
    for p in a.split(','):
        lo, _, hi = p.partition('-'); ids.update(range(int(lo), int(hi or lo) + 1))
seen = set()
for gj in sorted(glob.glob(ROOT + '/*/game.json')):
    for f in sorted(glob.glob(os.path.dirname(gj) + '/g*.summary.json')):
        s = json.load(open(f))
        try: m = int(s['file'].split('/')[-2])
        except Exception: continue
        if m not in ids: continue
        U = 'A' if s['teamA'] == 'vibe26b' else 'B'
        if s['winner'] == U or 'starve' not in s[U].get('kingDeaths', {}): continue
        key = (s['map'], s['team' + ('B' if U == 'A' else 'A')], U, s['rounds'])
        if key in seen: continue
        seen.add(key)
        rows = list(csv.DictReader(open(f.replace('summary.json', 'rounds.tsv')), delimiter='\t'))
        end = len(rows)
        def at(back):
            r = rows[max(0, end - 1 - back)]
            return '%s/%s/%s' % (r['bank' + U], r['rawCheese' + U], r['babies' + U])
        print('%-22s %-30s %s r%-5d bank/raw/babies at -200 %-14s -100 %-14s -50 %-14s -10 %-14s' % (key[0], key[1], U, key[3], at(200), at(100), at(50), at(10)))
