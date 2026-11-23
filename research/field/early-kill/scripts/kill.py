#!/usr/bin/env python3
"""Early king-kill reconstruction from replica replays (replays only, via tools/game_report.dump).

  kill.py MATCH:GAME [MATCH:GAME ...]   one JSON line per game on stdout, plus a text timeline on stderr

For the game's last killed king of ours (vibe26b), over the WIN rounds before it dies:
  timeline per 10 rounds: enemy rats / our rats within R of our king (dist2 <= R2 from its centre), its hp,
  enemy king distance; damage to our king by source (bite by rat / bite by king / throwHit split by the thrown
  rat's team / rat trap / cat / starvation); enemy grabs of our rats and throws of our rats (and what they hit);
  where the attackers came from; spawns and bank per team.
"""
import collections, json, math, os, sys

REPO = os.path.expanduser('~/projects/vibe/2026b')
sys.path.insert(0, os.path.join(REPO, 'tools'))
import game_report as GR  # noqa: E402

REPLAYS = os.path.expanduser('~/projects/vibe/bc26b-galaxy-replays')
US = 'vibe26b'
WIN = 100
R2 = 25          # "near the king": within 5 tiles of its centre
R2_DEF = 64      # defenders' death zone: within 8


def num(x, d=0):
    try:
        return int(x)
    except (TypeError, ValueError):
        return d


def xy(s):
    try:
        a, b = s.split(',')
        return int(a), int(b)
    except (ValueError, AttributeError):
        return None


def d2(a, b):
    return (a[0] - b[0]) ** 2 + (a[1] - b[1]) ** 2


def analyse(mid, g):
    d = GR.dump(os.path.join(REPLAYS, str(mid), 'replay.bc26'))
    pre = os.path.join(d, f'g{g}')
    meta = json.load(open(pre + '.meta.json'))
    summ = json.load(open(pre + '.summary.json'))
    us = 1 if meta['teamA'] == US else 2
    them = 3 - us
    opp = meta['teamB'] if us == 1 else meta['teamA']
    sU, sT = ('A', 'B') if us == 1 else ('B', 'A')

    # positions: own end-of-turn records per round
    pos = collections.defaultdict(dict)        # round -> id -> (team, type, x, y, hp)
    kings = {1: {}, 2: {}}                     # team -> id -> [(round, x, y, hp)]
    first = {}
    cheeseAt = {}
    for r in GR.rows(pre + '.turns.tsv'):
        if r['src'] != 'own':
            continue
        t, ty = num(r['team']), r['type']
        rr = int(r['round'])
        if r['dead'] == '1':
            continue
        p = (t, ty, int(r['x']), int(r['y']), num(r['hp']))
        pos[rr][r['id']] = p
        if ty == 'RAT':
            cheeseAt[(rr, r['id'])] = num(r['cheese'])
        first.setdefault(r['id'], (rr, p[2], p[3]))
        if ty == 'KING' and t in kings:
            kings[t].setdefault(r['id'], []).append((rr, p[2], p[3], p[4]))
    rounds = {int(r['round']): r for r in GR.rows(pre + '.rounds.tsv')}
    last = max(rounds)
    acts = list(GR.rows(pre + '.actions.tsv'))

    # our king that died last
    kdeath = {}
    spawnR = {}
    spawnAt = {}
    for a in acts:
        if a['action'] == 'die' and a['c'] == 'KING' and num(a['d']) == us:
            kdeath[a['a']] = (int(a['round']), a['e'])
        if a['action'] == 'spawn' and a['b'] == 'RAT':
            spawnR[a['a']] = int(a['round'])
            spawnAt[a['a']] = (num(a['team']), xy(a['c']))
    if not kdeath:
        return None
    kid = max(kdeath, key=lambda k: kdeath[k][0])
    D, cause = kdeath[kid]
    W0 = max(1, D - WIN)
    ktrack = {rr: (x, y, hp) for rr, x, y, hp in kings[us].get(kid, [])}
    kstart = kings[us][kid][0][1:3] if kings[us].get(kid) else None
    ekings = kings[them]
    ek0 = None
    if ekings:
        e1 = min(ekings.values(), key=lambda v: v[0][0])
        ek0 = e1[0][1:3]

    def kpos(rr):
        while rr > 0 and rr not in ktrack:
            rr -= 1
        return ktrack.get(rr)

    def ekpos(rr, kp):
        best = None
        for v in ekings.values():
            c = [p for p in v if p[0] <= rr]
            if c and v[-1][0] >= rr - 1:
                if best is None or (kp and d2(c[-1][1:3], kp[:2]) < d2(best, kp[:2])):
                    best = c[-1][1:3]
        return best

    # per-round counts over the WHOLE game (for first contact) and the window
    tl = []
    firstNear = None
    rounds_ge3 = 0
    arrive = {}             # enemy rat id -> first round within R2 (any time)
    for rr in sorted(pos):
        kp = kpos(rr)
        if not kp:
            continue
        nE = nU = 0
        for i, (t, ty, x, y, hp) in pos[rr].items():
            if ty != 'RAT':
                continue
            if d2((x, y), kp[:2]) <= R2:
                if t == them:
                    nE += 1
                    arrive.setdefault(i, rr)
                elif t == us:
                    nU += 1
        if nE and firstNear is None:
            firstNear = rr
        if nE >= 3:
            rounds_ge3 += 1
        if W0 <= rr <= D:
            ekp = ekpos(rr, kp)
            tl.append(dict(r=rr, nE=nE, nU=nU, hp=kp[2], kx=kp[0], ky=kp[1],
                           ekd=round(math.dist(ekp, kp[:2]), 1) if ekp else None,
                           ekmv=round(math.dist(ekp, ek0), 1) if ekp and ek0 else None,
                           bankU=num(rounds.get(rr, {}).get('bank' + sU)), bankT=num(rounds.get(rr, {}).get('bank' + sT)),
                           babU=num(rounds.get(rr, {}).get('babies' + sU)), babT=num(rounds.get(rr, {}).get('babies' + sT))))

    # damage to our king(s), whole game and window
    ourKings = set(kings[us])
    dmg = collections.Counter()
    dmgW = collections.Counter()
    hitsW = collections.Counter()
    throwsW = collections.Counter()
    grabsW = collections.Counter()
    ourDeathsW = collections.Counter()
    enemyDeathsW = collections.Counter()
    trapsW = collections.Counter()
    spawnsW = collections.Counter()
    biters = collections.Counter()
    kingHitRounds = []
    thrownBy = {}           # thrown rat id -> (round, thrower id, thrower team)
    napsByRat = {}          # victim -> (round, grabber, victim's distance to our king)
    throwHitD, napLag, napD, napVictimCheese, kingHitDmg, dmgEv = [], [], [], [], [], []
    napVictimAge, napVictimHp, napGrabberHp = [], [], []
    for a in acts:
        rr = int(a['round'])
        act = a['action']
        inW = W0 <= rr <= D
        rowteam = num(a['team'])
        if act == 'bite' and a['a'] in ourKings:
            src = 'biteKing' if a['type'] == 'KING' else 'biteRat'
            dmg[src] += num(a['d'])
            if inW:
                dmgW[src] += num(a['d'])
                dmgEv.append((rr, src, num(a['d'])))
                biters[a['id']] += 1
                kingHitRounds.append(rr)
        elif act == 'throwHit' and a['a'] in ourKings:
            thrownTeam = None
            sp = spawnAt.get(a['e'])
            if sp:
                thrownTeam = sp[0]
            src = 'throwHit_' + ('ourRat' if thrownTeam == us else 'theirRat' if thrownTeam == them else '?')
            if num(a['g']) == us:
                src += '_byUs'
            dmg[src] += num(a['b'])
            if inW:
                dmgW[src] += num(a['b'])
                dmgEv.append((rr, src, num(a['b'])))
                hitsW[src] += 1
                kingHitRounds.append(rr)
                kingHitDmg.append(num(a['b']))
                kp = kpos(rr)
                tp = pos.get(rr - 1, {}).get(a['f'])
                if kp and tp:
                    throwHitD.append(round(math.dist(tp[2:4], kp[:2]), 1))
                nr = napsByRat.get(a['e'])
                if nr and rr - nr[0] <= 12:
                    napLag.append(rr - nr[0])
                    if nr[2] is not None:
                        napD.append(nr[2])
        elif act == 'trigger' and a['e'] in ourKings:
            src = 'trap' if a['g'] == 'rat' else 'catTrap'
            dmg[src] += num(a['f'])
            if inW:
                dmgW[src] += num(a['f'])
        elif act == 'scratch' and a['d'] in ourKings:
            dmg['cat'] += num(a['f'])
            if inW:
                dmgW['cat'] += num(a['f'])
        elif act == 'throw' and inW:
            sp = spawnAt.get(a['a'])
            tteam = sp[0] if sp else None
            if rowteam == them:
                kp = kpos(rr)
                me = pos.get(rr, {}).get(a['id']) or pos.get(rr - 1, {}).get(a['id'])
                near = kp and me and d2(me[2:4], kp[:2]) <= 64
                throwsW['them_' + ('ourRat' if tteam == us else 'ownRat') + ('_near' if near else '')] += 1
            elif rowteam == us:
                throwsW['us_' + ('theirRat' if tteam == them else 'ownRat')] += 1
        elif act == 'nap' and inW:
            if rowteam == them and a['d'] == 'enemy':
                kp = kpos(rr)
                vp = pos.get(rr - 1, {}).get(a['a'])
                napsByRat[a['a']] = (rr, a['id'], round(math.dist(vp[2:4], kp[:2]), 1) if vp and kp else None)
                if vp and kp and d2(vp[2:4], kp[:2]) <= 64:
                    napVictimCheese.append(cheeseAt.get((rr - 1, a['a']), 0))
                    napVictimAge.append(rr - spawnR.get(a['a'], 0))
                    napVictimHp.append(vp[4])
                    gp = pos.get(rr - 1, {}).get(a['id'])
                    if gp:
                        napGrabberHp.append(gp[4])
                me = pos.get(rr, {}).get(a['id']) or pos.get(rr - 1, {}).get(a['id'])
                near = kp and me and d2(me[2:4], kp[:2]) <= 64
                grabsW['them' + ('_near' if near else '')] += 1
            elif rowteam == us and a['d'] == 'enemy':
                grabsW['us'] += 1
        elif act == 'die' and inW and a['c'] == 'RAT':
            vt = num(a['d'])
            kp = kpos(rr)
            vp = pos.get(rr - 1, {}).get(a['a'])
            near = kp and vp and d2(vp[2:4], kp[:2]) <= R2_DEF
            if vt == us:
                ourDeathsW[a['e'] + ('_near' if near else '')] += 1
            elif vt == them:
                enemyDeathsW[a['e'] + ('_near' if near else '')] += 1
        elif act == 'ratTrap' and inW:
            trapsW['them' if num(a['b']) == them else 'us'] += 1
        elif act == 'spawn' and inW and a['b'] == 'RAT':
            spawnsW['them' if rowteam == them else 'us'] += 1
    # starvation: rounds in window where our bank < 2 while the king lived (10 hp each)
    stW = sum(10 for rr in range(W0, D + 1) if rr in rounds and num(rounds[rr]['bank' + sU]) < 2)
    if stW:
        dmgW['starve~'] = stW

    # attackers: enemy rats within R2 of our king during the window
    att = {}
    for row in tl:
        rr = row['r']
        kp = (row['kx'], row['ky'])
        for i, (t, ty, x, y, hp) in pos[rr].items():
            if t == them and ty == 'RAT' and d2((x, y), kp) <= R2:
                att.setdefault(i, rr)
    attInfo = []
    for i, ar in att.items():
        sr = spawnR.get(i, first.get(i, (0,))[0])
        kp = kpos(ar)
        # where was it 20 and 40 rounds before arriving
        back = {}
        for lag in (20, 40):
            p = None
            for q in range(ar - lag, ar - lag - 6, -1):
                p = pos.get(q, {}).get(i)
                if p:
                    break
            if p and kp:
                back[lag] = (round(math.dist(p[2:4], kp[:2]), 1),
                             round(math.dist(p[2:4], ek0), 1) if ek0 else None)
        sl = spawnAt.get(i, (None, None))[1]
        attInfo.append(dict(id=i, spawn=sr, arrive=ar, firstEver=arrive.get(i), age=ar - sr, back=back,
                            spawnD=round(math.dist(sl, kp[:2]), 1) if sl and kp else None))
    # spawn distance between kings
    kd0 = round(math.dist(kstart, ek0), 1) if kstart and ek0 else None
    hpW0 = tl[0]['hp'] if tl else None
    # first round king hp < full
    full = kings[us][kid][0][3]
    firstDmg = next((rr for rr, x, y, hp in kings[us][kid] if hp < full), None)
    ekmvMax = max((r['ekmv'] or 0 for r in tl), default=None)
    return dict(match=mid, game=g, map=meta['map'], W=meta['width'], H=meta['height'], opp=opp, us='A' if us == 1 else 'B',
                death=D, cause=cause, nKings=len(ourKings), kingStart=kstart, kingAtDeath=(tl[-1]['kx'], tl[-1]['ky']) if tl else None,
                kingStartDist=kd0, firstNear=firstNear, rounds_ge3=rounds_ge3, firstDmg=firstDmg, hpW0=hpW0,
                dmg=dict(dmg), dmgW=dict(dmgW), hitsW=dict(hitsW), throwsW=dict(throwsW), grabsW=dict(grabsW),
                ourDeathsW=dict(ourDeathsW), enemyDeathsW=dict(enemyDeathsW), trapsW=dict(trapsW), spawnsW=dict(spawnsW),
                nBiters=len(biters), throwHitD=throwHitD, napLag=napLag, napVictimCheese=napVictimCheese, kingHitDmg=kingHitDmg, dmgEv=dmgEv, napVictimAge=napVictimAge, napVictimHp=napVictimHp, napGrabberHp=napGrabberHp, napD=napD,
                kmv=round(math.dist(kstart, (tl[-1]['kx'], tl[-1]['ky'])), 1) if tl and kstart else None, attackers=attInfo, ekmvMax=ekmvMax, tl=tl,
                summStarve=summ[sU].get('starveDmg'), kingDeaths=summ[sU].get('kingDeaths'))


def show(res):
    o = sys.stderr
    print(f"\n== {res['match']} g{res['game']} {res['map']} {res['W']}x{res['H']} vs {res['opp']} us={res['us']} "
          f"king died r{res['death']} by {res['cause']}; kings {res['nKings']}; start dist {res['kingStartDist']}; "
          f"first enemy near r{res['firstNear']}; first dmg r{res['firstDmg']}; hp@W0 {res['hpW0']}; our king moved {res['kmv']}", file=o)
    print('   king-hit throw dists', res['throwHitD'], 'grab->hit lag', res['napLag'], 'grab dist', res['napD'], file=o)
    print('   dmg game', res['dmg'], '| window', res['dmgW'], file=o)
    print('   throws', res['throwsW'], 'grabs', res['grabsW'], 'traps', res['trapsW'], 'spawns', res['spawnsW'], file=o)
    print('   our deaths', res['ourDeathsW'], '| their deaths', res['enemyDeathsW'], file=o)
    print('    r  nE nU  hp   king   ekd ekmv bankU bankT babU babT', file=o)
    for row in res['tl']:
        if (res['death'] - row['r']) % 10 == 0 or row['r'] == res['death']:
            print(f"  {row['r']:4d} {row['nE']:2d} {row['nU']:2d} {row['hp']:4d} {row['kx']:2d},{row['ky']:<2d} {row['ekd']!s:>5} "
                  f"{row['ekmv']!s:>4} {row['bankU']:5d} {row['bankT']:5d} {row['babU']:4d} {row['babT']:4d}", file=o)
    ats = sorted(res['attackers'], key=lambda a: a['arrive'])
    print('   attackers (spawn->arrive, age, dist to our king/their start 20 and 40 before):', file=o)
    print('    ' + '; '.join(f"{a['spawn']}->{a['arrive']} a{a['age']} {a['back'].get(20)} {a['back'].get(40)}" for a in ats), file=o)


def main():
    for spec in sys.argv[1:]:
        m, g = spec.split(':')
        res = analyse(int(m), int(g))
        if res is None:
            print(f'{spec}: no king death of ours', file=sys.stderr)
            continue
        show(res)
        print(json.dumps(res))


if __name__ == '__main__':
    main()
