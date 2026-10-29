#!/usr/bin/env python3
"""Combat analysis of each game in a ReplayDump tsv directory (or a .bc26, dumped on the fly; dumpVersion >= 2).

  battle_report.py REPLAY.bc26|TSVDIR|MATCHDIR [--game N] [--lo R] [--hi R] [--step 10] [--json]

MATCHDIR: a directory holding replay.bc26 (a replica match), dumped; a directory with neither exits 1. Every game by
default (a header per game: map, rounds, teams, winner; 'match.json reversed' from the match.json beside the replay);
--game N reports only game N. --json prints one line per game. Team 1 = A, 2 = B; rounds lo..hi (an empty window
prints only the header). A robot's state in a round is its last record that round, whatever its src
(split/thrown/carried records repeat a robot); dead records are skipped; carried rats count as alive but not at
the front.
  front    per step rounds: baby rats alive at the end of the round (the dump's rebuilt babiesA/B), and over the same
           rats (last records less later deaths, plus spawns): their HP, rats within dist2 20 of an enemy rat,
           carried rats, mean Chebyshev distance to the nearest own king alive that round
  hploss   HP lost by baby rats (kingloss: by kings) by cause, killing blows included (bite, scratch, crush, pounce,
           trap, throwHit, throwLand, fed, form, starve; other = an hp drop no row explains); hpby: the same by
           source (enemy, cat, own = this team's own throws, starve, other). Matches the dump's summary accounting
  deaths   baby-rat and king deaths by cause, by killer (enemy, cat, own, other), rats merged into an own king,
           kills (enemy rats/kings, cats); kd = enemy rats this team killed / own rats the enemy killed (cat kills,
           merges and terminations count for neither)
  grabs    grabs of another rat (enemy, ally) and what freed the victim first: throw (a point-blank fatal throw
           too), drop:<why>, release:<how>, died (while carried), held (to the end); mean rounds held per fate;
           whether the victim was weaker than the carrier and could see it the round before
  throws   landing damage the thrown rat took (42 = point-blank on the throw turn, 10 = landed on nothing);
           throwOut: thrown rats by side, landing (ground, target, eaten, fed, none), fatal landings, robots hit
           (hitEnemy, hitAlly; hitKing: the enemy king, a subset of hitEnemy), enemy rats (kills) and kings
           (killsKing) killed by throws (the hp is in the enemy's hploss/kingloss throwLand/throwHit)
  traps    per owner: rat/cat traps placed in the window and what became of those by the game's end (hit = fired,
           removed, live: placed = hit + removed + live), hitRate = hit / placed, meanLife (rounds placed->fired);
           triggers in the window (fired, by kind), their damage, kills, rat-trap victims by team; the window's cat
           damage split into traps and bites (catDmg minus traps); byKing: placed by kings; ratLiveMax: the most
           rat traps live at once, ratLiveAtMax: window rounds that ended at that count (a binding cap shows as many)
  xfer     per max(step, 50) rounds: cheese delivered to kings, kings' own pickups, rat pickups (count, amount)
"""
import argparse, collections, json, os, sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import game_report  # noqa: E402

# the replay's direction ordinal (FlatHelpers): 0 centre, then west and counter-clockwise; not the Java enum order
DIRS = [None, (-1, 0), (-1, -1), (0, -1), (1, -1), (1, 0), (1, 1), (0, 1), (-1, 1)]
CAT_KILL = ('scratch', 'crush', 'pounce')
NO_HIT = ('crush', 'pounce', 'form', 'fed', 'starve')   # die causes with no damage row: the blow is the hp left


def games(d):
    return game_report.games(d)                        # every gN.meta.json, the same set source() accepted


def load(path, game=0):
    d = path if os.path.isdir(path) else game_report.dump(path)
    turns = list(game_report.rows(os.path.join(d, f'g{game}.turns.tsv')))
    acts = list(game_report.rows(os.path.join(d, f'g{game}.actions.tsv')))
    if turns and 'src' not in turns[0]:
        raise SystemExit(f'battle_report: {d} is a dumpVersion 1 dump; redump the replay')
    return turns, acts


def last_rows(turns):
    """(round, id) -> the robot's last record that round."""
    return {(int(t['round']), t['id']): t for t in turns}


def formed(acts):
    """king id -> round its formation ran (that round's row still says RAT)."""
    return {a['a']: int(a['round']) for a in acts if a['action'] == 'upgradeKing'}


def sees(vx, vy, vdir, tx, ty):
    """A baby at (vx, vy) facing replay direction ordinal vdir sees (tx, ty): dot >= |cross| and dist2 <= 20."""
    try:
        fx, fy = DIRS[int(vdir)]
    except (ValueError, IndexError, TypeError):
        return True
    dx, dy = tx - vx, ty - vy
    return dx * dx + dy * dy <= 20 and dx * fx + dy * fy >= abs(dx * fy - dy * fx)


def front(turns, acts, lo, hi, step, rounds=(), meta=None):
    """n: the dump's rebuilt end-of-round baby count (rounds.tsv babiesA/B); the other columns from the robots alive
    at the end of the round: last records less robots that died later that round, plus this round's spawns (no
    record yet: full hp at the spawn tile)."""
    fk = formed(acts)
    health = (meta or {}).get('types', {}).get('RAT', {}).get('health', 0)
    died, born = collections.defaultdict(set), collections.defaultdict(list)
    for a in acts:
        if a['action'] == 'die':
            died[int(a['round'])].add(a['a'])
        elif a['action'] == 'spawn' and a['b'] == 'RAT':
            x, y = a['c'].split(',')
            born[int(a['round'])].append((a['a'], a['team'], int(x), int(y)))
    rats, kings, seen = collections.defaultdict(list), collections.defaultdict(list), set()
    for (r, i), t in last_rows(turns).items():
        seen.add((r, i))
        if t['dead'] == '1' or t['team'] not in ('1', '2') or i in died[r]:
            continue
        if t['type'] == 'KING' or fk.get(i, 10 ** 9) <= r:
            kings[r].append((t['team'], int(t['x']), int(t['y'])))
        elif t['type'] == 'RAT':
            rats[r].append((t['team'], int(t['x']), int(t['y']), int(t['hp']), t['carrier'] != '0'))
    for r, b in born.items():
        rats[r] += [(tm, x, y, health, False) for i, tm, x, y in b if (r, i) not in seen and i not in died[r]]
    babies = {int(x['round']): {'1': int(x['babiesA']), '2': int(x['babiesB'])} for x in rounds if x.get('babiesA', '') != ''}
    out = []
    for r in range(lo, hi + 1, step):
        m = rats.get(r, [])
        row = {'round': r}
        for tm in '12':
            mine = [v for v in m if v[0] == tm]
            free = [v for v in mine if not v[4]]
            en = [v for v in m if v[0] != tm and not v[4]]
            ks = [k for k in kings.get(r, []) if k[0] == tm]
            dk = [min(max(abs(v[1] - kx), abs(v[2] - ky)) for _, kx, ky in ks) for v in free] if ks else []
            row[tm] = dict(n=babies[r][tm] if r in babies else len(mine), hp=sum(v[3] for v in mine),
                           carried=len(mine) - len(free),
                           front=sum(1 for v in free if any((v[1] - e[1]) ** 2 + (v[2] - e[2]) ** 2 <= 20 for e in en)),
                           dk=round(sum(dk) / len(dk), 1) if dk else None)
        out.append(row)
    return out


def hits(turns, acts, meta):
    """Replays the hp of every robot in emission order (each record's actions, then the record); yields
    (round, victim team, victim type, cause, source, hp) for every hp loss, as ReplayDump's summary credits it."""
    health = {k: v['health'] for k, v in meta.get('types', {}).items()}
    hp, typ, team, bitten = {}, {}, {}, {}
    for b in meta.get('bodies', []):
        i = str(b['id'])
        typ[i], team[i], hp[i] = b['type'], str(b['team']), health.get(b['type'], 0)
    recs = collections.defaultdict(list)
    for a in acts:
        recs[(int(a['round']), int(a['seq']) if a['seq'] != '-1' else 10 ** 6)].append(a)
    for t in turns:
        recs[(int(t['round']), int(t['seq']))].append(t)

    def lose(r, v, cause, atk, amt):
        if amt <= 0 or team.get(v) not in ('1', '2'):
            return None
        src = 'cat' if atk == '0' else 'starve' if cause == 'starve' else 'other' if atk is None \
            else 'own' if atk == team[v] else 'enemy'
        return (r, team[v], typ.get(v), cause, src, amt)

    for (r, _), rs in sorted(recs.items()):
        for a in rs:
            ev = None
            k = a.get('action')
            if k is None:                                   # a turn record: hp lost with no row
                i, h = a['id'], int(a['hp'])
                if i not in hp:
                    hp[i], typ[i], team[i] = max(h, 0), a['type'], a['team']
                rest = max(0, hp[i]) - max(0, h)
                if rest >= 10 and typ.get(i) == 'KING' and a['src'] == 'own':
                    ev = lose(r, i, 'starve', None, 10)
                    if ev:
                        yield ev
                    ev, rest = None, rest - 10
                if rest > 0:
                    ev = lose(r, i, 'bite' if bitten.get(i) else 'other', bitten.get(i), rest)
                bitten.pop(i, None)
                hp[i] = h
            elif k == 'spawn':
                i = a['a']
                typ[i], team[i], hp[i] = a['b'], a['team'], health.get(a['b'], 0)
            elif k == 'upgradeKing':
                typ[a['a']] = 'KING'
            elif k == 'bite':
                v, amt = a['a'], int(a['d'])
                ev = lose(r, v, 'bite', a['team'], amt)
                hp[v] = 0 if a['e'] == '1' else hp.get(v, 0) - amt
                if a['e'] != '1' and a['b'] != 'CAT':
                    bitten[v] = a['team']
            elif k == 'scratch' and a['d'] not in ('', '0'):
                v, amt = a['d'], int(a['f'])
                ev = lose(r, v, 'scratch', '0', amt)
                hp[v] = hp.get(v, 0) - amt
            elif k == 'trigger' and a['e'] not in ('', '0'):
                v, amt = a['e'], int(a['f'])
                ev = lose(r, v, 'trap', a['d'], amt)
                hp[v] = hp.get(v, 0) - amt
            elif k == 'damage' and a['d'] != 'eaten':      # after the landing tile's traps: they took the rest
                v, n = a['a'], int(a['b'])
                ev = lose(r, v, 'throwLand', a['f'], min(n, max(0, hp.get(v, 0))))
                hp[v] = hp.get(v, 0) - n
            elif k == 'throwHit':
                v, amt = a['a'], int(a['b'])
                ev = lose(r, v, 'throwHit', a['g'], amt)
                hp[v] = hp.get(v, 0) - amt
            elif k == 'die' and a['b'] != 'terminated':
                v, cause = a['a'], a['e']
                if cause in NO_HIT:
                    ev = lose(r, v, cause, '0' if cause in CAT_KILL else None if cause == 'starve' else a['g'],
                              max(0, hp.get(v, 0)))
                if cause in NO_HIT or cause == 'merge':
                    hp[v] = min(hp.get(v, 0), 0)
            if ev:
                yield ev


def hploss(turns, acts, lo, hi, meta=None):
    out = {k: {'1': collections.Counter(), '2': collections.Counter()} for k in ('hploss', 'kingloss', 'hpby')}
    for r, tm, ty, cause, src, amt in hits(turns, acts, meta or {}):
        if lo <= r <= hi:
            out['kingloss' if ty == 'KING' else 'hploss'][tm][cause] += amt
            out['hpby'][tm][src] += amt
    return {k: {tm: dict(c) for tm, c in v.items()} for k, v in out.items()}


def deaths(acts, lo, hi):
    out = {tm: dict(rat=collections.Counter(), king=collections.Counter(), by=collections.Counter(), merged=0,
                    killsRat=0, killsKing=0, catsKilled=0) for tm in '12'}
    seen = set()
    for a in acts:
        if a['action'] != 'die' or a['a'] in seen:
            continue
        seen.add(a['a'])
        if not lo <= int(a['round']) <= hi:
            continue
        vt, cause, kt = a['d'], a['e'], a['g']
        if a['c'] == 'CAT':
            if kt in out:
                out[kt]['catsKilled'] += 1
            continue
        if vt not in out:
            continue
        if cause == 'merge':
            out[vt]['merged'] += 1
            continue
        out[vt]['king' if a['c'] == 'KING' else 'rat'][cause] += 1
        if a['c'] != 'RAT':
            if kt in out and kt != vt:
                out[kt]['killsKing'] += 1
            continue
        by = 'enemy' if kt in out and kt != vt else 'own' if kt == vt else 'cat' if cause in CAT_KILL else 'other'
        out[vt]['by'][by] += 1
        if by == 'enemy':
            out[kt]['killsRat'] += 1
    for tm, o in out.items():
        en = o['by']['enemy']
        o['kd'] = round(o['killsRat'] / en, 2) if en else None
    for o in out.values():
        for k in ('rat', 'king', 'by'):
            o[k] = dict(o[k])
    return out


def grabs(turns, acts, lo, hi):
    hist = last_rows(turns)
    ev = collections.defaultdict(list)                 # victim id -> [(index, round, fate)] in emission order
    for n, a in enumerate(acts):
        k = a['action']
        if k == 'throw':
            ev[a['a']].append((n, 'throw'))
        elif k == 'drop':
            ev[a['a']].append((n, 'drop:' + a['c']))
        elif k == 'release':
            ev[a['id']].append((n, 'release:' + a['c']))
        elif k == 'die':
            # dying in its own thrown record is a point-blank fatal throw: that record precedes the thrower's throw row
            ev[a['a']].append((n, 'throw' if a['src'] == 'thrown' and a['id'] == a['a'] else 'died'))

    def state(i, r):
        for k in range(r, r - 3, -1):
            if (k, i) in hist and hist[(k, i)]['dead'] == '0':
                return hist[(k, i)]
        return None

    out = {}
    for tm in '12':
        fate, how, held, side = collections.Counter(), collections.Counter(), collections.Counter(), collections.Counter()
        for n, a in enumerate(acts):
            if a['action'] != 'nap' or a['team'] != tm:
                continue
            r0 = int(a['round'])
            if not lo <= r0 <= hi:
                continue
            side[a['d']] += 1
            nxt = next(((m, f) for m, f in ev[a['a']] if m > n), None)
            f = nxt[1] if nxt else 'held'
            fate[f] += 1
            held[f] += (int(acts[nxt[0]]['round']) if nxt else int(acts[-1]['round'])) - r0
            car, vic = state(a['id'], r0), state(a['a'], r0 - 1)
            if car and vic:
                s = sees(int(vic['x']), int(vic['y']), vic['dir'], int(car['x']), int(car['y']))
                how[('front' if s else 'behind') + ('/weaker' if int(vic['hp']) < int(car['hp']) else '')] += 1
        out[tm] = {'n': sum(fate.values()), 'enemy': side['enemy'], 'ally': side['ally'], 'fate': dict(fate),
                   'held': {f: round(held[f] / c, 1) for f, c in fate.items()}, 'how': dict(how)}
    return out


def throws(turns, acts, lo, hi):
    land, dmg = collections.defaultdict(list), {}
    for n, a in enumerate(acts):
        if a['action'] in ('land', 'feed'):            # feed: thrown point-blank into a cat, no land row
            land[a['a']].append(n)
        elif a['action'] == 'damage':
            dmg[(a['round'], a['seq'], a['a'])] = int(a['b'])
    vteam = {t['id']: t['team'] for t in turns}
    hurt = {(a['round'], a['seq'], a['e']): a for a in acts if a['action'] == 'throwHit'}
    out = {'1': collections.Counter(), '2': collections.Counter()}
    res = {tm: collections.Counter() for tm in '12'}
    used = set()
    for n, a in enumerate(acts):
        tm = a['team']
        if a['action'] != 'throw' or tm not in out or not lo <= int(a['round']) <= hi:
            continue
        o, r = res[tm], int(a['round'])
        o['n'] += 1
        o['ownRat' if vteam.get(a['a']) == tm else 'enemyRat'] += 1
        # the point-blank landing is in the thrown record, which precedes the thrower's own (the throw row)
        m = next((m for m in land[a['a']] if m not in used and int(acts[m]['round']) >= r), None)
        if m is None:
            o['none'] += 1
            continue
        used.add(m)
        lr = acts[m]
        fed = lr['action'] == 'feed'
        o['fed' if fed else lr['b']] += 1
        o['fatal'] += fed or lr['c'] == '1'
        key = (lr['round'], lr['seq'], a['a'])
        if not fed and lr['b'] != 'eaten' and key in dmg:
            out[tm][dmg[key]] += 1
        h = hurt.get(key)
        if h:
            o['hitEnemy' if h['d'] != tm else 'hitAlly'] += 1
            if h['c'] == 'KING' and h['d'] != tm:
                o['hitKing'] += 1
    for a in acts:
        r, k = int(a['round']), a['action']
        if not lo <= r <= hi:
            continue
        if k == 'die' and a['e'] in ('throwHit', 'throwLand', 'fed') and a['g'] in res and a['d'] in res and a['d'] != a['g']:
            res[a['g']]['kills' if a['c'] == 'RAT' else 'killsKing'] += 1
    return {tm: dict(sorted(c.items())) for tm, c in out.items()}, {tm: dict(c) for tm, c in res.items()}


def traps(acts, rounds, lo, hi):
    live = {}                                          # (tile, owner) -> (kind, round placed)
    out = {tm: dict(placed=collections.Counter(), hit=0, removed=0, live=0, fired=collections.Counter(), life=[],
                    dmg=collections.Counter(), kills=collections.Counter(), victims=collections.Counter(), wasted=0,
                    byKing=0)
           for tm in '12'}
    delta = collections.Counter()                      # (owner, round) -> change in its live rat-trap count
    for a in acts:
        r, k = int(a['round']), a['action']
        if k in ('ratTrap', 'catTrap'):
            live[(a['a'], a['b'])] = (k[:3], r)
            if k == 'ratTrap':
                delta[(a['b'], r)] += 1
            if a['b'] in out and lo <= r <= hi:
                out[a['b']]['placed'][k[:3]] += 1
                out[a['b']]['byKing'] += a['type'] == 'KING'
        elif k == 'removeTrap':
            p = live.pop((a['a'], a['b']), None)
            if p and p[0] == 'rat':
                delta[(a['b'], r)] -= 1
            if p and a['b'] in out and lo <= p[1] <= hi:
                out[a['b']]['removed'] += 1
        elif k == 'trigger':
            p = live.pop((a['a'], a['d']), None)
            if p and p[0] == 'rat':
                delta[(a['d'], r)] -= 1
            o = out.get(a['d'])
            if o is None:
                continue
            if p and lo <= p[1] <= hi:
                o['hit'] += 1
                o['life'].append(r - p[1])
            if not lo <= r <= hi:
                continue
            o['fired'][a['g']] += 1
            o['dmg'][a['g']] += int(a['f'] or 0)
            o['wasted'] += a['f'] == '0'
            if a['g'] == 'rat':
                o['victims'][a['b']] += 1
        elif k == 'die' and a['e'] == 'trap' and a['g'] in out and lo <= r <= hi:
            out[a['g']]['kills'][a['c']] += 1
    for (_, tm), p in live.items():
        if tm in out and lo <= p[1] <= hi:
            out[tm]['live'] += 1
    for tm, o in out.items():                          # the live rat-trap count at each round's end over the window
        n, peak, at = 0, 0, 0
        for r in range(0, hi + 1):
            n += delta.get((tm, r), 0)
            if r < lo:
                continue
            if n > peak:
                peak, at = n, 0
            at += n == peak
        o['ratLiveMax'], o['ratLiveAtMax'] = peak, at if peak else 0
    cat = sorted((int(r['round']), int(r['catDmgA']), int(r['catDmgB'])) for r in rounds)

    def cum(r):                                        # cumulative cat damage at the end of round r
        return next((c[1:] for c in reversed(cat) if c[0] <= r), (0, 0))
    for i, tm in enumerate('12'):
        o = out[tm]
        placed = sum(o['placed'].values())
        o['hitRate'] = round(o['hit'] / placed, 2) if placed else None
        o['meanLife'] = round(sum(o['life']) / len(o['life']), 1) if o['life'] else None
        del o['life']
        o['catDmg'] = cum(hi)[i] - cum(lo - 1)[i] if lo <= hi else 0
        o['catDmgTrap'] = o['dmg']['cat']
        o['catDmgBite'] = o['catDmg'] - o['catDmgTrap']
        for k in ('placed', 'fired', 'dmg', 'kills', 'victims'):
            o[k] = dict(o[k])
    return out


def xfer(acts, lo, hi, step):
    x, kp, p, pa = collections.Counter(), collections.Counter(), collections.Counter(), collections.Counter()
    for a in acts:
        r = int(a['round'])
        if not lo <= r <= hi:
            continue
        w, k = (r // step * step, a['team']), a['action']
        if k == 'transfer':
            x[w] += int(a['b'])
        elif k == 'kingPickup':
            kp[w] += int(a['b'])
        elif k == 'pickup' and a['type'] == 'RAT':
            p[w] += 1
            pa[w] += int(a['b'] or 0)
    ws = sorted(set(k[0] for k in list(x) + list(kp) + list(p)))
    return [dict(round=w, xfer1=x[(w, '1')], xfer2=x[(w, '2')], kpick1=kp[(w, '1')], kpick2=kp[(w, '2')],
                 pick1=p[(w, '1')], pick2=p[(w, '2')], pickAmt1=pa[(w, '1')], pickAmt2=pa[(w, '2')]) for w in ws]


def header(d, game, replay=None):
    """Map, teams, result; `reversed` from the match.json beside the replay (replica matches: the side swap is not in
    the replay): `replay` (the .bc26 reported), else the dump's own meta file path when it is absolute."""
    pre = os.path.join(d, f'g{game}')
    meta = json.load(open(pre + '.meta.json'))
    h = dict(game=game, map=meta.get('map'), teamA=meta.get('teamA'), teamB=meta.get('teamB'))
    if os.path.exists(pre + '.summary.json'):
        s = json.load(open(pre + '.summary.json'))
        h.update(rounds=s.get('rounds'), winner=s.get('winner'), winType=s.get('winType'))
    f = os.path.abspath(replay) if replay else meta.get('file') or ''
    mj = os.path.join(os.path.dirname(f), 'match.json')
    if os.path.isabs(f) and os.path.exists(mj):
        g = json.load(open(mj)).get('games', [])
        if game < len(g) and g[game].get('map') == h['map']:
            h['reversed'] = g[game].get('reversed')
    return h, meta


def report(path, lo=0, hi=2000, step=10, game=0, replay=None):
    """One game's sections; `replay`: the .bc26 the dump directory `path` was made from (for match.json)."""
    d, replay = (path, replay) if os.path.isdir(path) else (game_report.dump(path), path)
    turns, acts = load(d, game)
    h, meta = header(d, game, replay)
    rounds = list(game_report.rows(os.path.join(d, f'g{game}.rounds.tsv'), need=('catDmgA', 'catDmgB', 'babiesA', 'babiesB')))
    hi = min(hi, max(int(t['round']) for t in turns))
    th, to = throws(turns, acts, lo, hi)
    return dict(h, lo=lo, hi=hi, front=front(turns, acts, max(lo, 1), hi, step, rounds, meta), **hploss(turns, acts, lo, hi, meta),
                deaths=deaths(acts, lo, hi), grabs=grabs(turns, acts, lo, hi), throws=th, throwOut=to,
                traps=traps(acts, rounds, lo, hi), xfer=xfer(acts, lo, hi, max(step, 50)))


def show(r):
    print(f"== g{r['game']} {r['map']}  rounds {r.get('rounds')}  A={r['teamA']}  B={r['teamB']}  winner {r.get('winner')}"
          f" {r.get('winType')}" + ('  (match.json reversed)' if r.get('reversed') else '') + f"  [rounds {r['lo']}..{r['hi']}]")
    if r['lo'] > r['hi']:
        print('  no rounds in the window')
        return
    print('round   ' + '   '.join(f'T{tm}: n   hp front carr dK' for tm in '12'))
    for f in r['front']:
        print(f"{f['round']:5d}  " + '   '.join(f"{f[tm]['n']:5d} {f[tm]['hp']:5d} {f[tm]['front']:4d} {f[tm]['carried']:4d} "
                                               + ('   -' if f[tm]['dk'] is None else f"{f[tm]['dk']:4.1f}") for tm in '12'))
    srt = lambda h: ', '.join(f'{k} {v}' for k, v in sorted(h.items(), key=lambda kv: -kv[1]))  # noqa: E731
    for tm in '12':
        h, k, b = r['hploss'][tm], r['kingloss'][tm], r['hpby'][tm]
        print(f"T{tm} rat hp lost {sum(h.values())}: {srt(h)} | king hp lost {sum(k.values())}: {srt(k)} | by {srt(b)}")
    for tm in '12':
        x = r['deaths'][tm]
        print(f"T{tm} rat deaths {sum(x['rat'].values())}: {srt(x['rat'])} | by {srt(x['by'])} | king deaths {x['king']}"
              f" merged {x['merged']} | kills rats {x['killsRat']} kings {x['killsKing']} cats {x['catsKilled']} kd {x['kd']}")
    for tm in '12':
        g = r['grabs'][tm]
        print(f"T{tm} grabs {g['n']} (enemy {g['enemy']} ally {g['ally']}): fate {g['fate']} held {g['held']} how {g['how']}")
    for tm in '12':
        print(f"T{tm} throws {r['throwOut'][tm]} | thrown rat's landing damage {r['throws'][tm]}")
    for tm in '12':
        t = r['traps'][tm]
        print(f"T{tm} traps placed {t['placed']} (by kings {t['byKing']}): hit {t['hit']} removed {t['removed']} live {t['live']} hitRate {t['hitRate']}"
              f" meanLife {t['meanLife']} | triggers {t['fired']} (on dead {t['wasted']}) dmg {t['dmg']} kills {t['kills']}"
              f" rat-trap victims {t['victims']} | live rat traps peak {t['ratLiveMax']} for {t['ratLiveAtMax']} rounds"
              f" | cat dmg {t['catDmg']} = traps {t['catDmgTrap']} + bites {t['catDmgBite']}")
    for x in r['xfer']:
        print(f"{x['round']:5d} delivered {x['xfer1']:5d} {x['xfer2']:5d}  king pickups {x['kpick1']:4d} {x['kpick2']:4d}"
              f"  rat pickups {x['pick1']:3d} {x['pick2']:3d} ({x['pickAmt1']} {x['pickAmt2']})")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('replay')
    ap.add_argument('--game', type=int, default=None, help='only this game (default: every game)')
    ap.add_argument('--lo', type=int, default=0)
    ap.add_argument('--hi', type=int, default=2000)
    ap.add_argument('--step', type=int, default=10)
    ap.add_argument('--json', action='store_true')
    o = ap.parse_args()
    d = game_report.source(o.replay)                   # a dir with no games: its replay.bc26 (else exit 1)
    rp = None if d == o.replay else o.replay           # the replay dumped: its match.json
    if rp and os.path.isdir(rp):
        rp = os.path.join(rp, 'replay.bc26')
    gs = games(d)
    if o.game is not None:
        if o.game not in gs:
            print(f'battle_report: no game {o.game} in {o.replay} (games {gs})', file=sys.stderr)
            sys.exit(2)
        gs = [o.game]
    for g in gs:
        r = report(d, o.lo, o.hi, o.step, g, rp)
        if o.json:
            print(json.dumps(r))
        else:
            show(r)


if __name__ == '__main__':
    main()
