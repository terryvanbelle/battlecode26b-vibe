"""yl25 mechanism check (RETREAT_HP 0): in local games, the first bot's (team 1) wounded babies near its own king,
its own babies thrown into its king, and grabs of its babies near its king. Usage: python retreat_check.py REPLAY..."""
import json, os, sys, collections
sys.path.insert(0, '/home/terryvanbelle/projects/vibe/2026b/tools')
import game_report as gr
T = collections.defaultdict(collections.Counter)
for p in sys.argv[1:]:
    bot = os.path.basename(p).split('-')[0]
    pre = os.path.join(gr.source(p), 'g0')
    X = T[bot]; X['games'] += 1
    king, rats = {}, collections.defaultdict(list)       # round -> king centre; round -> [(team, id, x, y, hp)]
    pos = {}                                               # (round, id) -> (x, y, hp)
    with open(pre + '.turns.tsv') as f:
        h = f.readline().rstrip('\n').split('\t'); jx = {x: i for i, x in enumerate(h)}
        for l in f:
            c = l.rstrip('\n').split('\t')
            if c[jx['src']] != 'own' or c[jx['coop']] == '1': continue
            r, tm, ty = int(c[0]), c[jx['team']], c[jx['type']]
            x, y, hp = int(c[jx['x']]), int(c[jx['y']]), int(c[jx['hp']])
            if ty == 'KING' and tm == '1': king.setdefault(r, (x, y))
            elif ty == 'RAT':
                rats[r].append((tm, c[jx['id']], x, y, hp)); pos[(r, c[jx['id']])] = (x, y, hp)
    for r, L in rats.items():
        if r not in king: continue
        kx, ky = king[r]
        foes = [(x, y) for tm, _, x, y, _ in L if tm == '2']
        for tm, _, x, y, hp in L:
            if tm != '1' or max(abs(x - kx), abs(y - ky)) > 4: continue
            X['own rat-turns within 4 of own king'] += 1
            if hp <= 40 and any((x - a) ** 2 + (y - b) ** 2 <= 20 for a, b in foes):
                X['  of them wounded (<=40) with an enemy within dist2 20'] += 1
    with open(pre + '.actions.tsv') as f:
        h = f.readline().rstrip('\n').split('\t'); ix = {x: i for i, x in enumerate(h)}
        for l in f:
            c = l.rstrip('\n').split('\t'); c += [''] * (len(h) - len(c))
            a, r = c[ix['action']], int(c[ix['round']])
            if a == 'throwHit' and c[ix['c']] == 'KING' and c[ix['d']] == '1' and c[ix['g']] == '2':
                X['enemy throws hitting our king'] += 1; X['  their damage'] += int(c[ix['b']] or 0)
            elif a == 'nap' and c[ix['b']] == '1' and c[ix['team']] == '2' and r in king:
                X['our babies grabbed'] += 1
                v = pos.get((r, c[ix['a']])) or pos.get((r - 1, c[ix['a']]))
                if v and max(abs(v[0] - king[r][0]), abs(v[1] - king[r][1])) <= 4:
                    X['  within 4 of our king'] += 1
                    if v[2] <= 40: X['    at 40 health or less'] += 1
for b, X in T.items():
    print(f'== {b} (team 1, {X["games"]} games)')
    for k, v in X.items():
        if k != 'games': print(f'  {k}: {v} ({v / X["games"]:.1f}/game)')
