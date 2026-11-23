"""Grabs in both directions for given replica games (MATCH:MAP ...): per side, grabs made, why each was legal
(victim facing away / weaker in view), victim hp at the grab, and how many grabbed rats died on landing."""
import json, math, os, sys, collections
sys.path.insert(0, '/home/terryvanbelle/projects/vibe/2026b/tools')
import game_report as gr
DX = [0, 0, 1, 1, 1, 0, -1, -1, -1]; DY = [0, 1, 1, 0, -1, -1, -1, 0, 1]
def in_cone(vx, vy, d, gx, gy):
    if d == 0 or (vx, vy) == (gx, gy): return True
    dx, dy = gx - vx, gy - vy
    cs = (DX[d] * dx + DY[d] * dy) / math.sqrt((dx * dx + dy * dy) * (DX[d] ** 2 + DY[d] ** 2))
    return math.degrees(math.acos(max(-1, min(1, cs)))) - 1e-3 <= 45
tot = {s: collections.Counter() for s in ('us', 'them')}
for arg in sys.argv[1:]:
    m, _, want = arg.partition(':')
    d = gr.source(os.path.expanduser(f'~/projects/vibe/bc26b-galaxy-replays/{m}'))
    gi = 0
    while os.path.exists(os.path.join(d, f'g{gi}.meta.json')):
        pre = os.path.join(d, f'g{gi}'); gi += 1
        meta = json.load(open(pre + '.meta.json'))
        if want and meta.get('map') != want: continue
        us = 1 if str(meta.get('teamA', '')).startswith('vibe26b') else 2
        naps, landdie = [], {}
        with open(pre + '.actions.tsv') as f:
            hdr = f.readline().rstrip('\n').split('\t'); ix = {h: i for i, h in enumerate(hdr)}
            for line in f:
                c = line.rstrip('\n').split('\t'); c += [''] * (len(hdr) - len(c))
                a = c[ix['action']]; r = int(c[ix['round']])
                if a == 'nap' and c[ix['d']] == 'enemy':
                    naps.append((r, int(c[ix['seq']] or 0), c[ix['a']], c[ix['id']], 'us' if c[ix['team']] == str(us) else 'them'))
                elif a == 'die' and c[ix['type']] == 'RAT' and c[ix['e']] in ('throwLand', 'throwHit'):
                    landdie[c[ix['a']]] = r
        need = set()
        for n in naps: need |= {n[0], n[0] - 1}
        rows = {}
        with open(pre + '.turns.tsv') as f:
            hdr = f.readline().rstrip('\n').split('\t'); jx = {h: i for i, h in enumerate(hdr)}
            for line in f:
                c = line.rstrip('\n').split('\t'); r = int(c[0])
                if r not in need or c[jx['src']] != 'own': continue
                rows[(r, c[1])] = (int(c[jx['seq']]), int(c[jx['x']]), int(c[jx['y']]), int(c[jx['dir']]), int(c[jx['hp']]))
        for r, s, v, g, side in naps:
            T = tot[side]
            vs = rows.get((r, v)); vs = vs if vs and vs[0] < s else rows.get((r - 1, v))
            g0 = rows.get((r - 1, g)); g1 = rows.get((r, g))
            if not vs or not (g0 or g1): T['no state'] += 1; continue
            _, vx, vy, vd, vh = vs
            gpos = [p for p in (g0, g1) if p and max(abs(p[1] - vx), abs(p[2] - vy)) <= 1]
            gh = (g1 or g0)[4]
            seen = [in_cone(vx, vy, vd, p[1], p[2]) for p in gpos] if gpos else [False]
            why = 'behind' if not any(seen) else ('weaker' if vh < gh else 'other')
            T['grabs'] += 1; T[why] += 1; T['victim hp sum'] += vh
            T[f'hp<=42'] += vh <= 42
            died = v in landdie and r <= landdie[v] <= r + 12
            T['died on landing'] += died
            T[f'{why}: died'] += died
        T = tot
print('per side (grabs made BY that side):')
for side in ('them', 'us'):
    T = tot[side]; n = max(1, T['grabs'])
    print(f"  {side:4s}: grabs {T['grabs']}, behind {T['behind']} ({100*T['behind']//n}%), weaker-in-view {T['weaker']} ({100*T['weaker']//n}%), other {T['other']};"
          f" victim mean hp {T['victim hp sum']//n}, victims hp<=42 {T['hp<=42']} ({100*T['hp<=42']//n}%); died on landing {T['died on landing']} ({100*T['died on landing']//n}%)"
          f" [behind {T['behind: died']}, weaker {T['weaker: died']}]")
