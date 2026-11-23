"""Why the enemy can grab our babies (21-map study, vs AlexT101_usqualsbot): for each enemy nap of our baby, the
victim's state just before the grab (its own end-of-turn row: this round if it moved before the grabber, else last
round), the grabber's positions (start and end of its turn), whether the grabber was in the victim's 90-degree cone
or healthier, the distance to our nearest king, and whether that grab ended in a throw into our king.
Usage: python grabs.py"""
import json, math, os, sys, collections
sys.path.insert(0, '/home/terryvanbelle/projects/vibe/2026b/tools')
import game_report as gr
REPO = '/home/terryvanbelle/projects/vibe/2026b'
recs = json.load(open(f'{REPO}/research/champ-corr/full_maps.json'))
result = {(r['match'], r['map']): r['won'] for r in recs}
DX = [0, 0, 1, 1, 1, 0, -1, -1, -1]; DY = [0, 1, 1, 0, -1, -1, -1, 0, 1]   # dump dir: 0 centre, 1 N .. 8 NW

def in_cone(vx, vy, d, gx, gy):
    if d == 0 or (vx, vy) == (gx, gy): return True
    dx, dy = gx - vx, gy - vy
    if dx * dx + dy * dy > 20: return False
    cs = (DX[d] * dx + DY[d] * dy) / math.sqrt((dx * dx + dy * dy) * (DX[d] ** 2 + DY[d] ** 2))
    return math.degrees(math.acos(max(-1, min(1, cs)))) - 1e-3 <= 45

stats = {k: collections.Counter() for k in ('lost', 'won')}
for m in sorted({r['match'] for r in recs}):
    try:
        d = gr.source(os.path.expanduser(f'~/projects/vibe/bc26b-galaxy-replays/{m}'))
    except Exception:
        continue
    gi = 0
    while os.path.exists(os.path.join(d, f'g{gi}.meta.json')):
        pre = os.path.join(d, f'g{gi}'); gi += 1
        meta = json.load(open(pre + '.meta.json'))
        if (m, meta.get('map')) not in result: continue
        us = 1 if str(meta.get('teamA', '')).startswith('vibe26b') else 2
        key = 'won' if result[(m, meta.get('map'))] else 'lost'
        naps, throws, born = [], collections.defaultdict(list), {}       # throws: victim id -> rounds it hit our king
        with open(pre + '.actions.tsv') as f:
            hdr = f.readline().rstrip('\n').split('\t'); ix = {h: i for i, h in enumerate(hdr)}
            for line in f:
                c = line.rstrip('\n').split('\t'); c += [''] * (len(hdr) - len(c))
                a = c[ix['action']]
                if a == 'spawn': born[c[ix['a']]] = int(c[ix['round']])
                if a == 'nap' and c[ix['d']] == 'enemy' and c[ix['b']] == str(us):
                    naps.append((int(c[ix['round']]), int(c[ix['seq']] or 0), c[ix['a']], c[ix['id']]))
                elif a == 'throwHit' and c[ix['c']] == 'KING' and c[ix['d']] == str(us) and c[ix['team']] == str(us):
                    throws[c[ix['e']]].append(int(c[ix['round']]))
        need = set()
        for r, _, _, _ in naps: need |= {r, r - 1}
        rows = {}
        kings = collections.defaultdict(list)
        with open(pre + '.turns.tsv') as f:
            hdr = f.readline().rstrip('\n').split('\t'); ix = {h: i for i, h in enumerate(hdr)}
            for line in f:
                c = line.rstrip('\n').split('\t')
                r = int(c[0])
                if r not in need or c[ix['src']] != 'own': continue
                st = (int(c[ix['seq']]), int(c[ix['x']]), int(c[ix['y']]), int(c[ix['dir']]), int(c[ix['hp']]), int(c[ix['cheese']] or 0))
                rows[(r, c[1])] = st
                if c[ix['type']] == 'KING' and c[ix['team']] == str(us): kings[r].append((st[1], st[2]))
        S = stats[key]
        for r, s, v, g in naps:
            vs = rows.get((r, v)); vs = vs if vs and vs[0] < s else rows.get((r - 1, v))
            g0 = rows.get((r - 1, g)); g1 = rows.get((r, g))
            if not vs or not (g0 or g1): S['no state'] += 1; continue
            _, vx, vy, vd, vh, vc = vs
            gpos = [p for p in (g0, g1) if p and max(abs(p[1] - vx), abs(p[2] - vy)) <= 1]
            if not gpos: S['grabber not adjacent'] += 1; continue
            gh = (g1 or g0)[4]
            seen = [in_cone(vx, vy, vd, p[1], p[2]) for p in gpos]
            why = 'behind (out of cone)' if not any(seen) else ('weaker (seen)' if vh < gh else ('ambiguous' if not all(seen) else 'seen, not weaker?'))
            kd = min((max(abs(kx - vx), abs(ky - vy)) for kx, ky in kings.get(r - 1, kings.get(r, []))), default=99)
            near = 'king<=3' if kd <= 3 else ('king4-6' if kd <= 6 else 'far')
            thrown = any(r <= t <= r + 12 for t in throws.get(v, []))
            S['grabs'] += 1; S[why] += 1; S[near] += 1
            S[f'{near} {why}'] += 1
            age = r - born.get(v, -999)
            agec = 'age<=5' if age <= 5 else ('age6-20' if age <= 20 else 'age>20')
            if kd <= 6:
                S[f'near-king grab {agec}'] += 1
                S[f'near-king grab {"loaded" if vc > 0 else "empty"}'] += 1
                S[f'near-king grab hp{"<=50" if vh <= 50 else ">50"}'] += 1
            if thrown:
                S[f'thrown {agec}'] += 1; S[f'thrown {"loaded" if vc > 0 else "empty"}'] += 1
                S[f'thrown hp{"<=50" if vh <= 50 else ">50"}'] += 1
                S['thrown into king'] += 1; S[f'thrown, {why}'] += 1; S[f'thrown, {near}'] += 1
for k, S in stats.items():
    print(f'== {k}: ' + ', '.join(f'{a} {b}' for a, b in sorted(S.items()) if a.startswith(('thrown', 'near-king', 'grabs'))))
