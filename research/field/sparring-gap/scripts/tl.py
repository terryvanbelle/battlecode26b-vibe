"""tl.py REPLAY GAME USNAME [STEP]: per-STEP-round timeline of one game, them / us."""
import sys, os, collections
sys.path.insert(0, os.path.expanduser('~/projects/vibe/2026b/tools'))
import game_report as GR
p, g, usname = sys.argv[1], int(sys.argv[2]), sys.argv[3]
step = int(sys.argv[4]) if len(sys.argv) > 4 else 20
d = GR.dump(p); s = GR.summary(d, g)
ut = 1 if s['teamA'] == usname else 2; tt = 3 - ut
print(s['map'], s['team' + ('A' if tt == 1 else 'B')], 'rounds', s['rounds'], 'winner', s.get('winner'), 'we are', 'A' if ut == 1 else 'B')
kings = collections.defaultdict(dict); rats = collections.defaultdict(lambda: {1: [], 2: []}); kst = {}
for r in GR.rows(os.path.join(d, f'g{g}.turns.tsv')):
    if r.get('src', 'own') != 'own' or r.get('dead') == '1': continue
    t = int(r['team']); rn = int(r['round'])
    if t not in (1, 2): continue
    if r['type'] == 'KING': kings[rn].setdefault(t, []).append((int(r['x']), int(r['y']))); kst.setdefault(t, (int(r['x']), int(r['y'])))
    else: rats[rn][t].append((int(r['x']), int(r['y'])))
B = collections.defaultdict(lambda: {1: collections.Counter(), 2: collections.Counter()})
for rn in rats:
    b = (rn - 1) // step
    for t in (1, 2):
        o = 3 - t
        ek = kings[rn].get(o) or [kst[o]]
        for (x, y) in rats[rn][t]:
            B[b][t]['rt'] += 1
            dm = (x - kst[t][0]) ** 2 + (y - kst[t][1]) ** 2; de = (x - kst[o][0]) ** 2 + (y - kst[o][1]) ** 2
            if de < dm: B[b][t]['fwd'] += 1
            if min(max(abs(x - kx), abs(y - ky)) for kx, ky in ek) <= 4: B[b][t]['atK'] += 1
for a in GR.rows(os.path.join(d, f'g{g}.actions.tsv')):
    try: t = int(a['team']); rn = int(a['round'])
    except (TypeError, ValueError): continue
    if t not in (1, 2): continue
    b = (rn - 1) // step; c = B[b][t]; k = a['action']
    if k == 'ratTrap':
        x, y = map(int, a['a'].split(',')); dm = (x - kst[t][0]) ** 2 + (y - kst[t][1]) ** 2; de = (x - kst[3 - t][0]) ** 2 + (y - kst[3 - t][1]) ** 2
        c['trapF' if de < dm else 'trapH'] += 1
    elif k == 'bite' and a['b'] == 'KING': c['kbite'] += 1
    elif k == 'throw': c['throw'] += 1
    elif k == 'die' and a['type'] == 'RAT':
        c['die'] += 1
        if (a.get('e') or '') in ('trap', 'throwLand', 'throwHit'): c['die_' + a['e'][:6]] += 1
    elif k == 'spawn' and a['type'] == 'KING': c['spawn'] += 1
print(f"{'rounds':>9} | {'rats':>9} {'fwd%':>9} {'atKing':>9} {'trapFwd':>9} {'trapHome':>9} {'throws':>9} {'deaths':>9} {'d:trap':>9} {'d:thrown':>9} {'kingBites':>9}   (them/us)")
for b in sorted(B):
    T, U = B[b][tt], B[b][ut]
    n = step
    def f(k, c): return c[k]
    row = f'{b*step+1:>4}-{(b+1)*step:<4} | ' + ' '.join(f'{x:>9}' for x in [
        f"{T['rt']/n:.0f}/{U['rt']/n:.0f}", f"{100*T['fwd']/max(1,T['rt']):.0f}/{100*U['fwd']/max(1,U['rt']):.0f}",
        f"{T['atK']/n:.1f}/{U['atK']/n:.1f}", f"{T['trapF']}/{U['trapF']}", f"{T['trapH']}/{U['trapH']}", f"{T['throw']}/{U['throw']}",
        f"{T['die']}/{U['die']}", f"{T['die_trap']}/{U['die_trap']}", f"{T['die_throwL']+T['die_throwH']}/{U['die_throwL']+U['die_throwH']}", f"{T['kbite']}/{U['kbite']}"])
    print(row)
