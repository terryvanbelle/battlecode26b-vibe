#!/usr/bin/env python3
"""Per-window behaviour of 'them' and 'us' from ReplayDump dirs.
win.py replica LABEL MID... | win.py local LABEL RUNDIR SPARBOT   (local: replays under RUNDIR/replays, us = the non-sparring bot)
Prints per window: them / us."""
import collections, glob, json, math, os, sys
REPO = os.path.expanduser('~/projects/vibe/2026b')
sys.path.insert(0, os.path.join(REPO, 'tools'))
import game_report as GR
W = [(1, 100), (101, 200), (201, 400), (401, 800), (801, 2000)]
def wi(rn):
    for i, (a, b) in enumerate(W):
        if a <= rn <= b: return i
def xy(s):
    x, y = s.split(','); return int(x), int(y)

def game_stats(d, g, them):
    """them: 1 or 2 (team number of the opponent / sparring bot)."""
    us = 3 - them
    st = {t: [collections.Counter() for _ in W] for t in (1, 2)}
    # positions per round
    pos = collections.defaultdict(lambda: {1: [], 2: []})
    kstart = {}
    for r in GR.rows(os.path.join(d, f'g{g}.turns.tsv')):
        if r.get('src', 'own') != 'own' or r.get('dead') == '1': continue
        t = int(r['team']); rn = int(r['round'])
        if t not in (1, 2): continue
        if r['type'] == 'KING':
            kstart.setdefault(t, (int(r['x']), int(r['y'])))
        elif r['type'] == 'RAT':
            p = (int(r['x']), int(r['y'])); pos[rn][t].append(p)
    for rn, pp in pos.items():
        i = wi(rn)
        if i is None or len(kstart) < 2: continue
        for t in (1, 2):
            o = 3 - t
            for (x, y) in pp[t]:
                st[t][i]['ratTurns'] += 1
                dm = (x - kstart[t][0]) ** 2 + (y - kstart[t][1]) ** 2
                de = (x - kstart[o][0]) ** 2 + (y - kstart[o][1]) ** 2
                if de < dm: st[t][i]['fwd'] += 1
                if de <= 36: st[t][i]['nearEK'] += 1
                if dm <= 36: st[t][i]['nearOK'] += 1
    for a in GR.rows(os.path.join(d, f'g{g}.actions.tsv')):
        try: t = int(a['team']); rn = int(a['round'])
        except (TypeError, ValueError): continue
        if t not in (1, 2): continue
        i = wi(rn)
        if i is None: continue
        c = st[t][i]; k = a['action']
        if k == 'spawn' and a['type'] == 'KING': c['spawn'] += 1
        elif k == 'ratTrap':
            c['trap'] += 1
            # distance from the trap to the nearest enemy rat this round
            tx, ty = xy(a['a']); en = pos[rn][3 - t]
            dd = min(((tx - x) ** 2 + (ty - y) ** 2 for x, y in en), default=999)
            c['trapNear2' if dd <= 2 else 'trapNear8' if dd <= 8 else 'trapFar'] += 1
            if len(kstart) == 2:
                dm = (tx - kstart[t][0]) ** 2 + (ty - kstart[t][1]) ** 2; de = (tx - kstart[3 - t][0]) ** 2 + (ty - kstart[3 - t][1]) ** 2
                if de < dm: c['trapFwd'] += 1
                if de <= 64: c['trapAtEK'] += 1
                if dm <= 64: c['trapAtOK'] += 1
        elif k == 'trigger': st[int(a['d'])][i]['trapHit'] += 1 if a.get('d') in ('1', '2') else 0
        elif k == 'bite':
            if a['b'] == 'KING': c['biteKing'] += 1; c['biteKingDmg'] += int(a.get('d') or 0); c['biteKing_' + a.get('src', '?')] += 1
            elif a['b'] == 'RAT': c['biteRat'] += 1
        elif k == 'throw': c['throw'] += 1
        elif k == 'pickup': c['pickAmt'] += int(a.get('b') or 0)
        elif k == 'die' and a['type'] == 'RAT':
            c['death'] += 1; c['death_' + (a.get('e') or a.get('b') or '?')] += 1
        elif k == 'die' and a['type'] == 'KING':
            c['kingDeath'] += 1; c['kingDeath_' + (a.get('e') or a.get('b') or '?')] += 1
    return {'them': st[them], 'us': st[us]}

def run(items):
    tot = {'them': [collections.Counter() for _ in W], 'us': [collections.Counter() for _ in W]}
    n = [0] * len(W)
    for d, g, them, rounds in items:
        s = game_stats(d, g, them)
        for i, (a, b) in enumerate(W):
            if rounds >= a: n[i] += 1
        for side in ('them', 'us'):
            for i in range(len(W)): tot[side][i].update(s[side][i])
    return tot, n

def show(label, tot, n):
    keys = ['spawn', 'death', 'death_trap', 'death_bite', 'death_throwLand', 'death_throwHit', 'trap', 'trapFwd', 'trapAtEK', 'trapAtOK', 'trapNear2', 'trapNear8',
            'trapFar', 'biteRat', 'biteKing', 'biteKing_own', 'biteKing_thrown', 'biteKingDmg', 'throw', 'pickAmt', 'fwd%', 'nearEK', 'nearOK', 'ratsMean']
    print(f'== {label}: per game in window, them / us; games reaching the window: {n}')
    print(f"{'':16}" + ''.join(f'{f"r{a}-{b}":>16}' for a, b in W))
    for k in keys:
        row = f'{k:16}'
        for i, (a, b) in enumerate(W):
            if not n[i]: row += f"{'-':>16}"; continue
            def v(side):
                c = tot[side][i]
                if k == 'fwd%': return 100 * c['fwd'] / max(1, c['ratTurns'])
                if k == 'ratsMean': return c['ratTurns'] / n[i] / (b - a + 1)
                return c[k] / n[i]
            row += f'{v("them"):>8.1f}/{v("us"):<7.1f}'
        print(row)

if __name__ == '__main__':
    mode, label = sys.argv[1], sys.argv[2]
    items_won, items_lost = [], []
    if mode == 'replica':
        R = os.path.expanduser('~/projects/vibe/bc26b-galaxy-replays')
        for mid in sys.argv[3:]:
            d = GR.dump(os.path.join(R, mid, 'replay.bc26'))
            for g in GR.games(d):
                s = GR.summary(d, g)
                them = 1 if s['teamA'] != 'vibe26b' else 2
                us = 'A' if them == 2 else 'B'
                (items_won if s.get('winner') == us else items_lost).append((d, g, them, s['rounds']))
    else:
        rundir, spar = sys.argv[3], sys.argv[4]
        for rp in sorted(glob.glob(os.path.join(rundir, 'replays', '*.bc26'))):
            d = GR.dump(rp)
            for g in GR.games(d):
                s = GR.summary(d, g)
                names = (s.get('teamA'), s.get('teamB'))
                if spar not in names: continue
                them = 1 if names[0] == spar else 2
                us = 'B' if them == 1 else 'A'
                (items_won if s.get('winner') == us else items_lost).append((d, g, them, s['rounds']))
    for lab, items in ((label + ' / all games', items_lost + items_won), (label + ' / they won', items_lost), (label + ' / we won', items_won)):
        if items:
            tot, n = run(items); show(lab, tot, n)
