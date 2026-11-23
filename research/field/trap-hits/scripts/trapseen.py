"""Enemy trap hits on our rats: had the victim (or any of our rats) seen the layer while it laid the trap, and was
the victim thrown onto the trap (landed that round)?"""
import csv, sys, os, json, math, collections
sys.path.insert(0, 'tools')
import game_report as gr
DIRS = [(0, 1), (1, 1), (1, 0), (1, -1), (0, -1), (-1, -1), (-1, 0), (-1, 1), (0, 0)]
def sees(x, y, d, tx, ty, r2=20, theta=90):
    if (x, y) == (tx, ty): return True
    dx, dy = tx - x, ty - y
    if dx * dx + dy * dy > r2: return False
    fx, fy = DIRS[d] if 0 <= d < 9 else (0, 0)
    if (fx, fy) == (0, 0): return True
    c = (fx * dx + fy * dy) / math.sqrt((dx * dx + dy * dy) * (fx * fx + fy * fy))
    return math.degrees(math.acos(max(-1, min(1, c)))) - 1e-3 <= theta / 2
tot = collections.Counter()
for m in sys.argv[1:]:
    d = gr.source(os.path.expanduser(f'~/projects/vibe/bc26b-galaxy-replays/{m}'))
    for gi in range(3):
        pre = os.path.join(d, f'g{gi}')
        if not os.path.exists(pre + '.meta.json'): continue
        meta = json.load(open(pre + '.meta.json'))
        us = 1 if str(meta['teamA']).startswith('vibe26b') else 2
        them = 3 - us
        pos = collections.defaultdict(dict)   # round -> id -> (x, y, dir, team, type, carrier)
        for r in csv.DictReader(open(pre + '.turns.tsv'), delimiter='\t'):
            if r['dead'] != '0': continue
            pos[int(r['round'])][r['id']] = (int(r['x']), int(r['y']), int(r['dir'] or 8), int(r['team']), r['type'], r['carrier'])
        laid = {}; landed = set(); c = collections.Counter()
        acts = list(csv.DictReader(open(pre + '.actions.tsv'), delimiter='\t'))
        for r in acts:
            if r['action'] == 'land': landed.add((int(r['round']), r['id']))
        for r in acts:
            if r['action'] == 'ratTrap':
                laid[r['a']] = (int(r['round']), r['id'], int(r['team']))
            elif r['action'] == 'trigger' and r['g'] == 'rat' and r['b'] == str(us) and r['d'] == str(them):
                rnd = int(r['round']); vid = r['e']; c['hits'] += 1
                if any((k, vid) in landed for k in (rnd, rnd - 1)): c['thrown onto it'] += 1; continue
                L = laid.get(r['a'])
                if not L or L[2] != them: c['layer ?'] += 1; continue
                lr, lid, _ = L
                lp = pos[lr].get(lid) or pos[lr - 1].get(lid)
                if not lp: c['layer ?'] += 1; continue
                vp = pos[lr].get(vid) or pos[lr - 1].get(vid)
                seen_v = bool(vp) and sees(vp[0], vp[1], vp[2], lp[0], lp[1])
                seen_any = any(sees(p[0], p[1], p[2], lp[0], lp[1], 25 if p[4] == 'KING' else 20, 360 if p[4] == 'KING' else 90)
                               for k in (lr, lr - 1) for i, p in pos[k].items() if p[3] == us)
                c['victim saw the lay' if seen_v else ('another of ours saw it' if seen_any else 'nobody saw it')] += 1
                if not vp: c['victim not born yet'] += 1
        print(m, gi, meta['map'][:14].ljust(14), dict(sorted(c.items())))
        tot.update(c)
print('ALL', dict(sorted(tot.items())))
