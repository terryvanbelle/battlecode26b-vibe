"""Our king under siege in the 21-map study's replica losses on the four maps every champion loses (vs
AlexT101_usqualsbot): per siege round (an enemy baby within dist2 30 of our king's centre), our babies on the king's
16-tile ring (dist2 <= 8 of its centre, the tiles enemies bite it from and the king spawns into), within Chebyshev 4,
and enemy babies on the ring; and how far from our king's centre the enemy stood when its throw hit our king.
Usage: python siege.py"""
import json, os, sys, collections
sys.path.insert(0, '/home/terryvanbelle/projects/vibe/2026b/tools')
import game_report as gr
REPO = '/home/terryvanbelle/projects/vibe/2026b'
recs = json.load(open(f'{REPO}/research/champ-corr/full_maps.json'))
LOST = {'cheesefarm', 'evileye', 'starvation', 'DefaultSmall'}
want = {(r['match'], r['map']) for r in recs if not r['won'] and r['map'] in LOST}
T = collections.Counter(); H = collections.defaultdict(collections.Counter); ng = 0
for m in sorted({k[0] for k in want}):
    try: d = gr.source(os.path.expanduser(f'~/projects/vibe/bc26b-galaxy-replays/{m}'))
    except Exception: continue
    gi = 0
    while os.path.exists(os.path.join(d, f'g{gi}.meta.json')):
        pre = os.path.join(d, f'g{gi}'); gi += 1
        meta = json.load(open(pre + '.meta.json'))
        if (m, meta.get('map')) not in want: continue
        ng += 1
        us = '1' if str(meta.get('teamA', '')).startswith('vibe26b') else '2'
        king = {}; rats = collections.defaultdict(list); pos = {}
        with open(pre + '.turns.tsv') as f:
            h = f.readline().rstrip('\n').split('\t'); jx = {x: i for i, x in enumerate(h)}
            for l in f:
                c = l.rstrip('\n').split('\t')
                if c[jx['src']] != 'own' or c[jx['coop']] == '1': continue
                r, tm, ty = int(c[0]), c[jx['team']], c[jx['type']]
                x, y = int(c[jx['x']]), int(c[jx['y']])
                if ty == 'KING' and tm == us: king.setdefault(r, (x, y))
                elif ty == 'RAT':
                    rats[r].append((tm == us, x, y, c[jx['carrier']])); pos[(r, c[jx['id']])] = (x, y)
        for r, L in rats.items():
            if r not in king: continue
            kx, ky = king[r]
            d2 = lambda x, y: (x - kx) ** 2 + (y - ky) ** 2
            foes = [(x, y) for own, x, y, _ in L if not own]
            if not any(d2(x, y) <= 30 for x, y in foes): continue
            T['siege rounds'] += 1
            ring = sum(1 for own, x, y, car in L if own and d2(x, y) <= 8 and max(abs(x - kx), abs(y - ky)) == 2)
            near = sum(1 for own, x, y, car in L if own and max(abs(x - kx), abs(y - ky)) <= 4)
            ering = sum(1 for x, y in foes if d2(x, y) <= 8 and max(abs(x - kx), abs(y - ky)) == 2)
            H['our babies on the ring'][min(ring, 16)] += 1
            H['our babies within 4'][min(near // 2 * 2, 16)] += 1
            H['enemy babies on the ring'][min(ering, 8)] += 1
        with open(pre + '.actions.tsv') as f:
            h = f.readline().rstrip('\n').split('\t'); ix = {x: i for i, x in enumerate(h)}
            for l in f:
                c = l.rstrip('\n').split('\t'); c += [''] * (len(h) - len(c))
                if c[ix['action']] == 'throwHit' and c[ix['c']] == 'KING' and c[ix['d']] == us and c[ix['g']] != us:
                    r = int(c[ix['round']]); p = pos.get((r, c[ix['f']])) or pos.get((r - 1, c[ix['f']]))
                    if p and r in king:
                        H['thrower Chebyshev distance from our king centre'][max(abs(p[0] - king[r][0]), abs(p[1] - king[r][1]))] += 1
print(ng, 'games;', dict(T))
for k, C in H.items():
    n = sum(C.values())
    print(f'{k}: ' + ', '.join(f'{v}: {100 * C[v] / n:.0f}%' for v in sorted(C)))
