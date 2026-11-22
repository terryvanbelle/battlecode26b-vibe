#!/usr/bin/env python3
"""Per-game, per-team behaviour profile from replica replays (via tools/game_report.dump).

  prof.py OUT.jsonl ID [ID ...]      appends one JSON line per (match, game, team); skips (match) already in OUT
"""
import csv, json, math, os, sys

REPO = os.path.expanduser('~/projects/vibe/2026b')
sys.path.insert(0, os.path.join(REPO, 'tools'))
import game_report as GR  # noqa: E402
import score  # noqa: E402

REPLAYS = os.path.expanduser('~/projects/vibe/bc26b-galaxy-replays')
csv.field_size_limit(10 ** 8)
AT = (100, 200, 300, 500, 750, 1000, 1500, 2000)


def tsv(p):
    with open(p, newline='') as f:
        yield from csv.DictReader(f, delimiter='\t')


def num(x, d=None):
    try:
        return int(x)
    except (TypeError, ValueError):
        try:
            return float(x)
        except (TypeError, ValueError):
            return d


def game(d, g, mid):
    s = json.load(open(os.path.join(d, f'g{g}.summary.json')))
    meta = json.load(open(os.path.join(d, f'g{g}.meta.json')))
    W, H = meta.get('width'), meta.get('height')
    rounds = list(tsv(os.path.join(d, f'g{g}.rounds.tsv')))
    byr = {int(r['round']): r for r in rounds}
    last = max(byr)
    pa, pb = score.points(s)
    out = []
    # kings from turns
    kings = {1: {}, 2: {}}
    for r in tsv(os.path.join(d, f'g{g}.turns.tsv')):
        if r['type'] != 'KING' or r.get('src', 'own') != 'own':
            continue
        t = int(r['team'])
        if t not in kings:
            continue
        k = kings[t].setdefault(r['id'], [])
        k.append((int(r['round']), int(r['x']), int(r['y']), int(r['hp']), r.get('dead') == '1'))
    acts = {1: {}, 2: {}}
    firsts = {1: {}, 2: {}}
    perwin = {1: {}, 2: {}}
    digpos = {1: [], 2: []}
    for a in tsv(os.path.join(d, f'g{g}.actions.tsv')):
        t = num(a['team'])
        if t not in (1, 2):
            continue
        k = a['action']
        rr = int(a['round'])
        acts[t][k] = acts[t].get(k, 0) + 1
        firsts[t].setdefault(k, rr)
        w = 'e' if rr <= 200 else ('m' if rr <= 1000 else 'l')
        perwin[t].setdefault(k, {}).setdefault(w, 0)
        perwin[t][k][w] += 1
        if k == 'bite' and a['b'] == 'KING':
            firsts[t].setdefault('biteKing', rr)
            acts[t]['biteKing'] = acts[t].get('biteKing', 0) + 1
        if k in ('dig', 'placeDirt'):
            try:
                x, y = map(int, a['a'].split(','))
                digpos[t].append((k, rr, x, y))
            except ValueError:
                pass
        if k == 'transfer':
            amt = num(a['b'], 0) or 0
            acts[t]['xferAmt_' + w] = acts[t].get('xferAmt_' + w, 0) + amt
    starts = {}
    for t in (1, 2):
        ks = sorted(kings[t].values(), key=lambda v: v[0][0])
        if ks:
            starts[t] = ks[0][0][1:3]
    for t, side in ((1, 'A'), (2, 'B')):
        o = 3 - t
        S = s[side]
        team = s['team' + side]
        rec = dict(match=mid, game=g, map=s['map'], W=W, H=H, team=team, opp=s['team' + ('B' if side == 'A' else 'A')],
                   side=side, rounds=s['rounds'], winType=s['winType'], won=s['winner'] == side,
                   pts=pa if side == 'A' else pb, ptsOpp=pb if side == 'A' else pa,
                   backstab=s.get('backstabRound'), bsBy=('self' if s.get('backstabBy') == t else
                                                         'opp' if s.get('backstabBy') == o else None),
                   bsCause=s.get('backstabCause'),
                   xfer=s['cheeseXfer' + side], xferOpp=s['cheeseXfer' + ('B' if side == 'A' else 'A')],
                   catDmg=s['catDmg' + side], catDmgOpp=s['catDmg' + ('B' if side == 'A' else 'A')])
        for kk in ('spawned', 'kingsFormed', 'deathsRat', 'deathsKing', 'throws', 'naps', 'grabsEnemy', 'ratTraps',
                   'catTraps', 'ratTrapHits', 'catTrapHits', 'dirtDug', 'dirtPlaced', 'pickups', 'pickupAmt',
                   'killsRat', 'bitesRat', 'bitesKing', 'bitesCat', 'dmgKing', 'starveDmg', 'squeaks', 'deathsByCat',
                   'deathsByEnemy', 'turnsRat', 'kingPickupAmt', 'friendlyDmg', 'overruns'):
            rec[kk] = S.get(kk)
        rec['dmgBy'] = S.get('dmgBy')
        rec['ratDeaths'] = S.get('ratDeaths')
        rec['kingDeaths'] = S.get('kingDeaths')
        # timelines
        tl = {}
        for r0 in AT:
            if r0 > last:
                tl[r0] = None
                continue
            r = byr.get(r0)
            tl[r0] = dict(b=num(r['babies' + side]), k=num(r['numKings' + side]), bank=num(r['bank' + side]),
                          x=num(r['cheeseXfer' + side]), cd=num(r['catDmg' + side]), rt=num(r['ratTraps' + side]),
                          ct=num(r['catTraps' + side]), dirt=num(r['dirt' + side]), pk=num(r['picked' + side]))
        rec['tl'] = tl
        bab = [(int(r['round']), num(r['babies' + side], 0)) for r in rounds]
        rec['babPeak'], rec['babPeakR'] = max(((b, rr) for rr, b in bab), default=(0, 0))
        rec['bankPeak'] = max((num(r['bank' + side], 0) for r in rounds), default=0)
        nk = [(int(r['round']), num(r['numKings' + side], 0)) for r in rounds]
        rec['maxKings'] = max((k for _, k in nk), default=0)
        rec['kingsReach'] = {}
        for rr, k in nk:
            for j in range(2, k + 1):
                rec['kingsReach'].setdefault(j, rr)
        rec['kingRounds2'] = sum(1 for _, k in nk if k >= 2)
        # kings: movement
        ks = []
        for kid, v in kings[t].items():
            v.sort()
            r0, x0, y0 = v[0][0], v[0][1], v[0][2]
            sx, sy = starts.get(t, (x0, y0))
            ox, oy = starts.get(o, (None, None))
            dmax = max(math.dist((x, y), (x0, y0)) for _, x, y, _, _ in v)
            moves = sum(1 for i in range(1, len(v)) if v[i][1:3] != v[i - 1][1:3])
            pos = {}
            for rr in (100, 300, 500, 1000, 1500):
                c = [p for p in v if p[0] <= rr]
                if c and v[-1][0] >= rr:
                    pos[rr] = round(math.dist(c[-1][1:3], (x0, y0)), 1)
            alive_to = v[-1][0]
            dead = v[-1][4] or alive_to < s['rounds']
            fe = round(math.dist((x0, y0), (ox, oy)), 1) if ox is not None else None
            fs = round(math.dist((x0, y0), (sx, sy)), 1)
            ks.append(dict(r0=r0, at=(x0, y0), fromStart=fs, toEnemyStart=fe, dmax=round(dmax, 1), moves=moves,
                           disp=pos, last=alive_to, dead=dead, hpEnd=v[-1][3]))
        rec['kings'] = sorted(ks, key=lambda k: k['r0'])
        if t in starts and o in starts:
            rec['startDist'] = round(math.dist(starts[t], starts[o]), 1)
        rec['acts'] = acts[t]
        rec['first'] = {k: firsts[t].get(k) for k in ('ratTrap', 'catTrap', 'dig', 'placeDirt', 'throw', 'nap', 'bite',
                                                       'biteKing', 'transfer')}
        rec['win'] = {k: perwin[t].get(k) for k in ('ratTrap', 'catTrap', 'dig', 'placeDirt', 'throw', 'spawn')}
        # dirt placed near own king start (fortification) vs elsewhere
        if t in starts:
            near = sum(1 for k, rr, x, y in digpos[t] if k == 'placeDirt' and math.dist((x, y), starts[t]) <= 6)
            rec['placeNearHome'] = near
            rec['digNearHome'] = sum(1 for k, rr, x, y in digpos[t] if k == 'dig' and math.dist((x, y), starts[t]) <= 6)
        out.append(rec)
    return out


def main():
    outp = sys.argv[1]
    done = set()
    if os.path.exists(outp):
        for l in open(outp):
            done.add(json.loads(l)['match'])
    with open(outp, 'a') as fo:
        for mid in sys.argv[2:]:
            mid = int(mid)
            if mid in done:
                continue
            rp = os.path.join(REPLAYS, str(mid), 'replay.bc26')
            if not os.path.exists(rp):
                print(mid, 'missing', file=sys.stderr)
                continue
            try:
                d = GR.dump(rp)
            except Exception as e:
                print(mid, 'dump failed', e, file=sys.stderr)
                continue
            n = json.load(open(os.path.join(d, 'game.json')))['games']
            for g in range(n):
                try:
                    for rec in game(d, g, mid):
                        fo.write(json.dumps(rec) + '\n')
                except Exception as e:
                    print(mid, g, 'failed', repr(e), file=sys.stderr)
            fo.flush()
            print(mid, 'ok', file=sys.stderr)


if __name__ == '__main__':
    main()
