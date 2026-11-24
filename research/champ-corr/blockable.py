"""sw63 (KING_BLOCK) upper bound: of the enemy throws that hit our king in replica losses, how many came from a carrier
3-6 tiles (Chebyshev) from our king's centre that our king could see (dist2 <= 25) the round before, so that a
newborn on the ring tile the flight crossed would have taken the hit; and our king's cheese (turns.tsv) then.
Usage: python blockable.py [MATCH:MAP ...]  (default: the 40 losses on the four maps every champion loses to
AlexT101_usqualsbot)."""
import json, os, sys, collections
sys.path.insert(0, '/home/terryvanbelle/projects/vibe/2026b/tools')
import game_report as gr
REPO = '/home/terryvanbelle/projects/vibe/2026b'
if len(sys.argv) > 1:
    want = {(int(a.split(':')[0]), a.split(':')[1]) for a in sys.argv[1:]}
else:
    recs = json.load(open(f'{REPO}/research/champ-corr/full_maps.json'))
    want = {(r['match'], r['map']) for r in recs if not r['won'] and r['map'] in {'cheesefarm', 'evileye', 'starvation', 'DefaultSmall'}}
T = collections.Counter(); bank = collections.Counter(); ng = 0
cheb = lambda a, b: max(abs(a[0] - b[0]), abs(a[1] - b[1]))
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
        king, kcheese, pos = {}, {}, {}
        with open(pre + '.turns.tsv') as f:
            h = f.readline().rstrip('\n').split('\t'); jx = {x: i for i, x in enumerate(h)}
            for l in f:
                c = l.rstrip('\n').split('\t')
                if c[jx['src']] != 'own': continue
                r = int(c[0]); xy = (int(c[jx['x']]), int(c[jx['y']]))
                if c[jx['type']] == 'KING' and c[jx['team']] == us and r not in king:
                    king[r] = xy; kcheese[r] = int(c[jx['cheese']] or 0)
                pos[(r, c[jx['id']])] = xy
        throws = {}
        rows = []
        with open(pre + '.actions.tsv') as f:
            h = f.readline().rstrip('\n').split('\t'); ix = {x: i for i, x in enumerate(h)}
            for l in f:
                c = l.rstrip('\n').split('\t'); c += [''] * (len(h) - len(c)); rows.append(c)
        for c in rows:
            if c[ix['action']] == 'throw':
                bx, by = map(int, c[ix['b']].split(','))
                throws[c[ix['a']]] = (int(c[ix['round']]), c[ix['id']], (bx, by))
        for c in rows:
            if not (c[ix['action']] == 'throwHit' and c[ix['c']] == 'KING' and c[ix['d']] == us and c[ix['g']] != us): continue
            T['enemy throws hitting our king'] += 1
            t = throws.get(c[ix['e']])
            if not t: T['  no throw row'] += 1; continue
            r, thr, b = t
            p = pos.get((r, thr)); kc = king.get(r) or king.get(r - 1)
            if not p or not kc: T['  no position'] += 1; continue
            dv = (b[0] - p[0], b[1] - p[1])
            k = cheb(p, kc)
            T[f'  thrown from Chebyshev {k if k <= 6 else "7+"}'] += 1
            if not 3 <= k <= 6: continue
            q = pos.get((r - 1, thr), p)
            if (q[0] - kc[0]) ** 2 + (q[1] - kc[1]) ** 2 > 25: T['  3-6 out, unseen by our king the round before'] += 1; continue
            T['  3-6 out and in our king\'s view the round before (blockable)'] += 1
            bank[min(kcheese.get(r, kcheese.get(r - 1, 0)) // 50 * 50, 500)] += 1
print(ng, 'games')
for k, v in T.items(): print(f'{k}: {v} ({v / max(1, ng):.1f}/game)')
print('our king\'s cheese at the blockable throws (turns.tsv, 50s):', dict(sorted(bank.items())))
