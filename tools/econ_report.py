#!/usr/bin/env python3
"""Economy report for every game of a replay: where each team's cheese came from and where it went, from a ReplayDump
tsv directory (or a .bc26, dumped through game_report's cache).

  econ_report.py REPLAY.bc26|TSVDIR [--game N] [--hi R] [--window W] [--json]
  econ_report.py --team NAME [--hi R] REPLAY.bc26|TSVDIR ...    every game of every replay, summed by mine side

Every cheese pile is followed from its spawn (a mine's cheesespawn, or a dying rat's drop) to the pickups that take
it (a partial pickup takes only its amount; the rest stays on the tile). Per mine: its side (the team whose king
starts nearer; '-' when equal), cheese spawned, cheese each team picked up there, cheese still lying there at the
end, and each team's mean wait (rounds from spawn to pickup, weighted by amount). Per team: cheese picked up (kings'
own pickups included) from own-side / enemy-side / middle mines and from drops, pickups, and the cheese lost on its
own side (spawned on its side and taken by the enemy or never taken). Per mine also each team's rat-rounds within
Chebyshev R of it (crowding). --hi R stops the trace at round R (cheese then still lying is left).
Ledger (dumpVersion 2 dumps): per team and window of W rounds (default the whole game up to --hi), the change of
bank + carried cheese = picked + enemy cheese taken by own formations - spawns - traps - digs - king upkeep -
formations - cheese dropped by deaths - own cheese taken by enemy formations + rest. rest is what no row explains
(a bite's cheese boost, a spawn cost off by a death in the same round); windows with rest != 0 are flagged '*'.
With --team, the per-mine rows of all games are summed by side as seen from that team (own / enemy / middle mines):
rat-rounds, cheese spawned, picked up by the team and by its opponent, left; and the team's ledger, summed.
--game N (0-based) keeps one game; --json prints one JSON line per game.
"""
import argparse, collections, json, os, sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import game_report  # noqa: E402  (source, rows, own, bank, summary)
import king_report  # noqa: E402  (starving king turns)

R = 4   # GameConstants.SQ_CHEESE_SPAWN_RADIUS: a mine spawns within Chebyshev R of itself
MINE = 20                                       # GameConstants.CHEESE_SPAWN_AMOUNT
START = 2500                                    # GameConstants.INITIAL_TEAM_CHEESE
SPAWN_BASE, SPAWN_STEP, SPAWN_PER = 10, 10, 4   # BUILD_ROBOT_BASE_COST, _COST_INCREASE, NUM_ROBOTS_FOR_COST_INCREASE
TRAP = {'ratTrap': 20, 'catTrap': 10}           # TrapType.buildCost
DIG, UPKEEP, FORM = 5, 2, 50                    # DIG_DIRT_CHEESE_COST, RAT_KING_CHEESE_CONSUMPTION, RAT_KING_UPGRADE_CHEESE_COST
LEDGER = ('start', 'picked', 'absorbed', 'spawns', 'spawnCost', 'traps', 'digs', 'upkeep', 'formations', 'dropped',
          'lostToForm', 'end', 'rest')
# the columns read, required of a dumpVersion 2+ dump (game_report.need)
ACTS, TURNS = 'round id team type action a b c seq d e f g', 'round id team type x y cheese seq src dead'
ROUNDS = 'round bankA bankB rawCheeseA rawCheeseB pickedA pickedB engBabiesA engBabiesB'


def side(loc, kings):
    """'A' or 'B': the team whose king starts nearer (dist2); '-' on a tie."""
    da = (loc[0] - kings[1][0]) ** 2 + (loc[1] - kings[1][1]) ** 2
    db = (loc[0] - kings[2][0]) ** 2 + (loc[1] - kings[2][1]) ** 2
    return 'A' if da < db else 'B' if db < da else '-'


def mine_of(loc, mines):
    """The nearest mine (Chebyshev, then dist2) within R of loc, else None."""
    best = None
    for m in mines:
        c = max(abs(loc[0] - m[0]), abs(loc[1] - m[1]))
        if c <= R:
            k = (c, (loc[0] - m[0]) ** 2 + (loc[1] - m[1]) ** 2)
            if best is None or k < best[0]:
                best = (k, m)
    return best and best[1]


def xy(s):
    x, y = s.split(',')
    return int(x), int(y)


def mirror(p, w, h, sym):
    # battlecode.world.MapSymmetry: 0 ROTATIONAL, 1 HORIZONTAL (y mirrored), 2 VERTICAL (x mirrored)
    x, y = p
    return (w - 1 - x, h - 1 - y) if sym == 0 else (x, h - 1 - y) if sym == 1 else (w - 1 - x, y)


def events(acts, mines, hi=None):
    """Cheese moves from actions.tsv rows: ('spawn', round, loc, amount, mine or None) and ('pickup', round, loc,
    team, amount). dumpVersion 2 rows say it (cheesespawn d mine|drop|remainder, f tile change; pickup b amount). An
    old dump's are rebuilt with a tile model: a spawn adding 20 near a mine is a mine's, a spawn right after a pickup on
    the same tile is that pickup's remainder (the pickup took the tile minus it), anything else a drop of its amount."""
    tile = collections.Counter()
    out, pend = [], None
    for r in acts:
        rn = int(r['round'])
        if hi is not None and rn > hi:
            break
        act = r['action']
        if act == 'cheesespawn' and r.get('d'):
            if r['d'] != 'remainder':
                loc = xy(r['a'])
                out.append(('spawn', rn, loc, int(r['f']), mine_of(loc, mines) if r['d'] == 'mine' else None))
        elif act == 'cheesespawn':
            loc, total = xy(r['a']), int(r['b'] or 0)
            if pend and pend[1] == loc:
                out[pend[0]] = out[pend[0]][:4] + (pend[2] - total,)
                tile[loc] = total
            elif total - tile[loc] == MINE and mine_of(loc, mines) is not None:
                out.append(('spawn', rn, loc, MINE, mine_of(loc, mines)))
                tile[loc] = total
            else:
                out.append(('spawn', rn, loc, total, None))
                tile[loc] += total
        elif act == 'pickup' and r['team'] in ('1', '2'):
            loc = xy(r['a'])
            if r.get('b'):
                out.append(('pickup', rn, loc, int(r['team']), int(r['b'])))
            else:
                out.append(('pickup', rn, loc, int(r['team']), tile[loc]))
                pend = (len(out) - 1, loc, tile[loc])
                tile[loc] = 0
                continue
        pend = None
    return out


def analyse(d, game=0, hi=None, window=0):
    pre = os.path.join(d, f'g{game}')
    meta = json.load(open(pre + '.meta.json'))
    mines = [tuple(m) for m in meta.get('mines') or []]
    # the dump lists every mine, both of each symmetric pair; a missing partner means a hand-made or broken dump
    w, h, sym = meta['width'], meta['height'], meta.get('symmetry', 0)
    lone = [m for m in mines if mirror(m, w, h, sym) not in mines]
    if lone:
        print(f'econ_report: g{game} meta lists mines {lone} without their symmetric partner; adding it',
              file=sys.stderr)
        mines += [mirror(m, w, h, sym) for m in lone]
    kings = {}
    for b in meta.get('bodies') or []:
        if b.get('type') == 'KING' and b.get('team') in (1, 2) and b['team'] not in kings:
            kings[b['team']] = (b['x'], b['y'])
    if len(kings) < 2:
        for r in game_report.rows(pre + '.turns.tsv', game_report.need(d, game, TURNS)):
            t = int(r['team'])
            if r['type'] == 'KING' and t in (1, 2) and t not in kings:
                kings[t] = (int(r['x']), int(r['y']))
            if len(kings) == 2:
                break
    piles = collections.defaultdict(list)   # tile -> [[round, amount, source]] not yet picked up; source = mine or None
    per_mine = {m: {'spawned': 0, 'spawns': 0, 'got': {1: 0, 2: 0}, 'wait': {1: 0, 2: 0}, 'first': {1: None, 2: None}}
                for m in mines}
    team = {t: {'pickups': 0, 'picked': 0, 'drops': 0, 'byside': collections.Counter()} for t in (1, 2)}
    rounds = 0
    for ev in events(game_report.rows(pre + '.actions.tsv', game_report.need(d, game, ACTS)), mines, hi):
        rounds = max(rounds, ev[1])
        if ev[0] == 'spawn':
            _, rn, loc, amt, src = ev
            if src is not None:
                per_mine[src]['spawned'] += amt
                per_mine[src]['spawns'] += 1
            if amt > 0:
                piles[loc].append([rn, amt, src])
            continue
        _, rn, loc, t, amt = ev
        team[t]['pickups'] += 1
        team[t]['picked'] += amt
        ps = piles[loc]
        while amt > 0 and ps:
            p = ps[0]
            take = min(amt, p[1])
            amt -= take
            p[1] -= take
            if p[1] == 0:
                ps.pop(0)
            if p[2] is None:
                team[t]['drops'] += take
                continue
            pm = per_mine[p[2]]
            pm['got'][t] += take
            pm['wait'][t] += take * (rn - p[0])
            if pm['first'][t] is None:
                pm['first'][t] = rn
            s = side(p[2], kings)
            team[t]['byside']['own' if s == 'AB'[t - 1] else 'mid' if s == '-' else 'enemy'] += take
    rats = collections.Counter()             # (mine, team) -> rat-rounds within R of the mine
    for r in game_report.own(game_report.rows(pre + '.turns.tsv', game_report.need(d, game, TURNS))):
        t = int(r['team'])
        if r['type'] != 'RAT' or t not in (1, 2) or r.get('dead') == '1' or hi is not None and int(r['round']) > hi:
            continue
        x, y = int(r['x']), int(r['y'])
        for m in mines:
            if max(abs(x - m[0]), abs(y - m[1])) <= R:
                rats[(m, t)] += 1
    left = collections.Counter()
    for loc, ps in piles.items():
        for prn, amt, src in ps:
            if src is not None:
                left[src] += amt
    sm = game_report.summary(d, game)
    if hi is None and sm.get('rounds'):
        rounds = sm['rounds']
    out = {'game': game, 'map': meta.get('map'), 'rounds': rounds, 'teams': {'A': meta.get('teamA'), 'B': meta.get('teamB')},
           'winner': sm.get('winner'), 'winType': sm.get('winType'), 'mines': [], 'A': {}, 'B': {}}
    lost = {1: 0, 2: 0}
    for m in sorted(mines):
        pm = per_mine[m]
        s = side(m, kings)
        row = {'mine': list(m), 'side': s, 'spawns': pm['spawns'], 'spawned': pm['spawned'], 'left': left[m]}
        for t, n in ((1, 'A'), (2, 'B')):
            g = pm['got'][t]
            row['got' + n] = g
            row['wait' + n] = round(pm['wait'][t] / g, 1) if g else None
            row['first' + n] = pm['first'][t]
            row['rats' + n] = rats[(m, t)]
        out['mines'].append(row)
        if s in 'AB' and s != '-':
            t = 1 if s == 'A' else 2
            lost[t] += pm['got'][3 - t] + left[m]
    for t, n in ((1, 'A'), (2, 'B')):
        tm = team[t]
        out[n] = {'pickups': tm['pickups'], 'picked': tm['picked'], 'drops': tm['drops'],
                  'own': tm['byside']['own'], 'enemy': tm['byside']['enemy'], 'mid': tm['byside']['mid'],
                  'lostOwnSide': lost[t]}
    out['ledger'] = ledger(d, game, hi, window)
    return out


def ledger(d, game, hi=None, window=0):
    """[{lo, hi, A: {LEDGER key: cheese}, B: ...}] per window, or None for a dump without bank columns."""
    pre = os.path.join(d, f'g{game}')
    if not os.path.exists(pre + '.rounds.tsv'):
        return None
    rnd = {int(r['round']): r for r in game_report.rows(pre + '.rounds.tsv', game_report.need(d, game, ROUNDS))}
    if not rnd or 'rawCheeseA' not in next(iter(rnd.values())):
        return None
    last = min(max(rnd), hi) if hi is not None else max(rnd)
    edges = list(range(1, last + 1, window)) if window > 0 else [1]
    win = lambda rn: (rn - 1) // window if window > 0 else 0
    acc = [{t: collections.Counter() for t in (1, 2)} for _ in edges]
    starved = collections.Counter()              # (round, team) -> king turns that starved instead of eating
    for o in king_report.analyse(d, game):
        for rn in o['starved']:
            starved[(rn, 1 if o['side'] == 'A' else 2)] += 1
    acts = list(game_report.rows(pre + '.actions.tsv', game_report.need(d, game, ACTS)))
    formed = {r['a'] for r in acts if r['action'] == 'die' and r['e'] == 'form'}
    kturns = collections.Counter()               # (round, team) -> kings' own turns
    cheese = collections.defaultdict(dict)       # id -> {(round, seq): carried cheese} of rats a formation killed
    for r in game_report.rows(pre + '.turns.tsv', game_report.need(d, game, TURNS)):
        if r['src'] == 'own' and r['type'] == 'KING' and r['team'] in ('1', '2') and r['dead'] == '0':
            kturns[(int(r['round']), int(r['team']))] += 1
        elif r['id'] in formed:
            cheese[r['id']][(int(r['round']), int(r['seq']))] = int(r['cheese'])
    babies = {1: 0, 2: 0}
    drop = rn0 = None
    for r in acts:
        rn = int(r['round'])
        if rn > last:
            break
        if rn != rn0 and rn - 1 in rnd:          # resync the engine's baby count (it decides the spawn cost)
            babies = {1: int(rnd[rn - 1]['engBabiesA']), 2: int(rnd[rn - 1]['engBabiesB'])}
        rn0 = rn
        a, act, t = acc[win(rn)], r['action'], int(r['team'])
        if act == 'spawn' and r['b'] == 'RAT' and t in (1, 2):
            a[t]['spawns'] += 1
            a[t]['spawnCost'] += SPAWN_BASE + SPAWN_STEP * (babies[t] // SPAWN_PER)
            babies[t] += 1
        elif act in TRAP and r['b'] in ('1', '2'):
            a[int(r['b'])]['traps'] += TRAP[act]
        elif act == 'dig' and t in (1, 2):
            a[t]['digs'] += DIG
        elif act == 'upgradeKing' and t in (1, 2):
            a[t]['formations'] += FORM
            a[t]['upkeep'] += UPKEEP         # the new king eats at the end of its forming turn (a RAT row)
        elif act == 'cheesespawn':
            drop = int(r['f']) if r.get('d') == 'drop' else None
            continue
        elif act == 'die' and r['d'] in ('1', '2'):
            v = int(r['d'])
            if r['c'] == 'RAT':
                babies[v] -= 1
            if drop is not None:
                a[v]['dropped'] += drop
            if r['e'] == 'form' and r['g'] in ('1', '2'):   # the forming king took the victim's carried cheese
                seq = int(r['seq'])
                c = [c for k, c in sorted(cheese.get(r['a'], {}).items()) if k < (rn, seq)]
                if c and c[-1]:
                    a[int(r['g'])]['absorbed'] += c[-1]
                    a[v]['lostToForm'] += c[-1]
        if act != 'feed':                        # a drop row comes just before its die row (a feed row between)
            drop = None
    out = []
    for i, lo in enumerate(edges):
        top = min(last, lo + window - 1) if window > 0 else last
        row = {'lo': lo, 'hi': top}
        for t, n in ((1, 'A'), (2, 'B')):
            a = acc[i][t]
            pool = lambda rn: (int(rnd[rn]['bank' + n]) + int(rnd[rn]['rawCheese' + n])) if rn in rnd else START
            a['start'], a['end'] = pool(lo - 1), pool(top)
            for rn in range(lo, top + 1):
                a['picked'] += int(rnd[rn]['picked' + n]) if rn in rnd else 0
                a['upkeep'] += UPKEEP * (kturns[(rn, t)] - starved[(rn, t)])
            a['rest'] = (a['end'] - a['start'] - a['picked'] - a['absorbed'] + a['spawnCost'] + a['traps'] + a['digs']
                         + a['upkeep'] + a['formations'] + a['dropped'] + a['lostToForm'])
            row[n] = {k: a[k] for k in LEDGER}
        out.append(row)
    return out


def own_share(o, n):
    """Team n's share of what the mines on its own half spawned, and those of its own-half mines it never picked from."""
    own = [m for m in o['mines'] if m['side'] == n]
    spawned = sum(m['spawned'] for m in own)
    idle = [m['mine'] for m in own if m['got' + n] == 0]
    return (sum(m['got' + n] for m in own) / spawned if spawned else None), idle


def render(o):
    res = f", winner {o['winner']} {o['winType']}" if o.get('winner') else ''
    lines = [f"game {o.get('game', 0)} {o['map']}: {o['teams']['A']} (A) vs {o['teams']['B']} (B), {o['rounds']} rounds{res}",
             f"{'mine':>8} side spawns spawned |   gotA  waitA firstA  ratsA |   gotB  waitB firstB  ratsB | left"]
    f = lambda v: '-' if v is None else str(v)
    for m in o['mines']:
        lines.append(f"{m['mine'][0]:>3},{m['mine'][1]:<3}  {m['side']:>3} {m['spawns']:>6} {m['spawned']:>7} | "
                     f"{m['gotA']:>6} {f(m['waitA']):>6} {f(m['firstA']):>6} {m['ratsA']:>6} | "
                     f"{m['gotB']:>6} {f(m['waitB']):>6} {f(m['firstB']):>6} {m['ratsB']:>6} | {m['left']:>4}")
    for n in 'AB':
        t = o[n]
        lines.append(f"{n} {o['teams'][n]}: picked {t['picked']} in {t['pickups']} pickups (own side {t['own']}, "
                     f"enemy side {t['enemy']}, middle {t['mid']}, drops {t['drops']}); lost on own side "
                     f"{t['lostOwnSide']}")
        share, idle = own_share(o, n)
        if share is not None:
            lines.append(f"  {n} own-half share {share:.2f}" + (' never worked: ' + ' '.join(f'{x},{y}' for x, y in idle) if idle else ''))
    if o.get('ledger'):
        lines.append('ledger (bank + carried): ' + ' '.join(LEDGER))
        for w in o['ledger']:
            for n in 'AB':
                v = w[n]
                lines.append(f"  r{w['lo']}-{w['hi']} {n} " + ' '.join(str(v[k]) for k in LEDGER) + (' *' if v['rest'] else ''))
    return '\n'.join(lines)


def team_ledger(outs, name):
    """The whole-game ledger of team `name` summed over the games it played (None if no game has one)."""
    tot = None
    for o in outs:
        me = 'A' if o['teams']['A'] == name else 'B' if o['teams']['B'] == name else None
        if me is None or not o.get('ledger'):
            continue
        tot = tot or dict.fromkeys(LEDGER, 0)
        for w in o['ledger']:
            for k in LEDGER:
                tot[k] += w[me][k]
    return tot


KEYS = ('rats', 'oppRats', 'spawned', 'got', 'oppGot', 'left')


def by_side(outs, name):
    """Sum per-mine rows of analysed games by side as seen from team `name`: {own|enemy|mid: {key: total}}, and
    the number of games it played."""
    tot = {s: dict.fromkeys(KEYS, 0) for s in ('own', 'enemy', 'mid')}
    n = 0
    for o in outs:
        me = 'A' if o['teams']['A'] == name else 'B' if o['teams']['B'] == name else None
        if me is None:
            continue
        n += 1
        op = 'B' if me == 'A' else 'A'
        for m in o['mines']:
            s = 'mid' if m['side'] == '-' else 'own' if m['side'] == me else 'enemy'
            for k, v in (('rats', m['rats' + me]), ('oppRats', m['rats' + op]), ('spawned', m['spawned']),
                         ('got', m['got' + me]), ('oppGot', m['got' + op]), ('left', m['left'])):
                tot[s][k] += v
    return tot, n


def render_sides(tot, n, name, led=None):
    lines = [f"{name}, {n} games: per game, by mine side ({' / '.join(KEYS)})"]
    for s, t in tot.items():
        if n:
            lines.append(f"  {s:6s} " + '  '.join(f"{k} {t[k] / n:.0f}" for k in KEYS))
    if led:
        lines.append('  ledger, summed: ' + '  '.join(f'{k} {v}' for k, v in led.items()))
    return '\n'.join(lines)


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument('src', nargs='+')
    ap.add_argument('--game', type=int, default=None, help='0-based game index (match.json game = N+1); default all')
    ap.add_argument('--hi', type=int, default=None)
    ap.add_argument('--window', type=int, default=0, help='ledger window in rounds (default: the whole game)')
    ap.add_argument('--team', default=None)
    ap.add_argument('--json', action='store_true')
    a = ap.parse_args(argv)
    if a.team is None:
        d = game_report.source(a.src[0])
        for g in game_report.pick(d, a.game):
            o = analyse(d, g, a.hi, a.window)
            print(json.dumps(o) if a.json else render(o))
        return
    outs = []
    for src in a.src:
        d = game_report.source(src)
        outs += [analyse(d, g, a.hi) for g in game_report.games(d)]
    tot, n = by_side(outs, a.team)
    led = team_ledger(outs, a.team)
    print(json.dumps({'games': n, 'sides': tot, 'ledger': led}) if a.json else render_sides(tot, n, a.team, led))


if __name__ == '__main__':
    main()
