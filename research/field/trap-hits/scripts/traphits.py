"""For each replica game: enemy trap triggers on our rats, by who laid the trap (king/baby), the distance from the
enemy king, the age of the trap, and whether one of our rats had seen the layer when it laid the trap."""
import csv, sys, os, json, collections
sys.path.insert(0, 'tools')
import game_report as gr
tot = collections.Counter(); per = []
for m in sys.argv[1:]:
    d = gr.source(os.path.expanduser(f'~/projects/vibe/bc26b-galaxy-replays/{m}'))
    for gi in range(3):
        pre = os.path.join(d, f'g{gi}')
        if not os.path.exists(pre + '.meta.json'): continue
        meta = json.load(open(pre + '.meta.json'))
        us = 1 if str(meta['teamA']).startswith('vibe26b') else 2
        them = 3 - us
        kpos = collections.defaultdict(dict)
        for r in csv.DictReader(open(pre + '.turns.tsv'), delimiter='\t'):
            if r['type'] == 'KING' and r['dead'] == '0':
                kpos[int(r['team'])].setdefault(int(r['round']), []).append((int(r['x']), int(r['y'])))
        def kings(t, rnd):
            d = kpos[t]
            for k in range(rnd, max(0, rnd - 5), -1):
                if k in d: return d[k]
            return []
        laid = {}   # tile -> (round, type, team)
        c = collections.Counter(); won = None
        for r in csv.DictReader(open(pre + '.actions.tsv'), delimiter='\t'):
            if r['action'] == 'ratTrap':
                laid[r['a']] = (int(r['round']), r['type'], int(r['team']))
            elif r['action'] == 'trigger' and r['g'] == 'rat' and r['b'] == str(us) and r['d'] == str(them):
                rnd = int(r['round']); x, y = map(int, r['a'].split(','))
                L = laid.get(r['a'])
                who = L[1] if L and L[2] == them else '?'
                age = rnd - L[0] if L and L[2] == them else -1
                ks = kings(them, rnd)
                dk = min((max(abs(x - a), abs(y - b)) for a, b in ks), default=99)
                ko = min((max(abs(x - a), abs(y - b)) for a, b in kings(us, rnd)), default=99)
                c['hits'] += 1; c['by ' + who] += 1
                c['enemy king <=3' if dk <= 3 else ('enemy king 4-8' if dk <= 8 else ('our king <=8' if ko <= 8 else 'elsewhere'))] += 1
                c['age <=150' if 0 <= age <= 150 else ('age >150' if age > 150 else 'age ?')] += 1
        summ = json.load(open(pre + '.summary.json')) if os.path.exists(pre + '.summary.json') else {}
        per.append((m, gi, meta['map'], dict(c)))
        tot.update(c)
for p in per: print(p[0], p[1], p[2][:14].ljust(14), {k: v for k, v in sorted(p[3].items())})
print('ALL', dict(sorted(tot.items())))
