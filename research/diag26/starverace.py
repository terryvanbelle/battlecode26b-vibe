import sys, csv, json, glob, collections
# starverace.py GLOB: for each replica game we lost with a starved king: our death round, and each enemy king's hp
# at that round and its hp 50 rounds before (a falling hp with no enemy damage = it was starving too)
seen = set()
for f in sorted(glob.glob(sys.argv[1])):
    s = json.load(open(f)); us = 'A' if s['teamA'] == 'vibe26b' else 'B'; th = 'B' if us == 'A' else 'A'
    if s['winner'] == us or 'starve' not in (s[us].get('kingDeaths') or {}): continue
    key = (s['file'].split('/')[-2], s['map'])
    if key in seen: continue
    seen.add(key)
    p = f[:-len('.summary.json')]; ut = '1' if us == 'A' else '2'
    hp = collections.defaultdict(dict); kills = []
    for x in csv.reader(open(p + '.turns.tsv'), delimiter='\t'):
        if x[0] == 'round' or x[3] != 'KING': continue
        hp[(x[2], x[1])][int(x[0])] = int(x[7])
    dr = s['rounds']
    for x in csv.reader(open(p + '.actions.tsv'), delimiter='\t'):
        if x[0] != 'round' and x[4] == 'die' and x[7] == 'KING' and x[2] == ut and x[11] == 'starve': dr = int(x[0])
    ek = []
    for (t, i), h in hp.items():
        if t == ut: continue
        rs = sorted(h); last = max(r for r in rs if r <= dr) if any(r <= dr for r in rs) else None
        if last is None: continue
        b = max([r for r in rs if r <= dr - 50] or [rs[0]])
        ek.append('%s:%d(was %d)' % (i, h[last], h[b]))
    opp = s['team' + th][:14]
    print(key[0], opp.ljust(14), s['map'][:15].ljust(15), us, 'ourdeath r%d' % dr, 'end r%d' % s['rounds'], 'enemy kings', ' '.join(ek))
