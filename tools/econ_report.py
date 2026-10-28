#!/usr/bin/env python3
"""Economy report for one game: where each team's cheese came from, from a ReplayDump tsv directory (or a .bc26).

  econ_report.py REPLAY.bc26|TSVDIR [--game N] [--hi R] [--json]
  econ_report.py --team NAME [--hi R] REPLAY.bc26|TSVDIR ...    every game of every replay, summed by mine side

Every cheese pile is followed from its spawn (a mine's cheesespawn, or a dying rat's drop) to the pickup that takes it.
Per mine: its side (the team whose king starts nearer; '-' when equal), cheese spawned, cheese each team picked up
there, cheese still lying there at the end, and each team's mean wait (rounds from spawn to pickup, weighted by
amount). Per team: cheese picked up from own-side / enemy-side / middle mines and from drops, pickups, and the cheese
lost on its own side (spawned on its side and taken by the enemy or never taken). Per mine also each team's
rat-rounds within Chebyshev R of it (crowding). --hi R stops the trace at round R (cheese then still lying is left).
With --team, the per-mine rows of all games are summed by side as seen from that team (own / enemy / middle mines):
rat-rounds, cheese spawned, picked up by the team and by its opponent, left.
"""
import argparse, collections, json, os, shutil, sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import game_report  # noqa: E402  (dump, rows)

R = 4   # GameConstants.SQ_CHEESE_SPAWN_RADIUS: a mine spawns within Chebyshev R of itself


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


def analyse(d, game=0, hi=None):
    pre = os.path.join(d, f'g{game}')
    meta = json.load(open(pre + '.meta.json'))
    mines = [tuple(m) for m in meta.get('mines') or []]
    # the dump lists one mine of each symmetric pair; add the partners
    w, h, sym = meta['width'], meta['height'], meta.get('symmetry', 0)
    def mirror(p):
        x, y = p
        # battlecode.world.MapSymmetry: 0 ROTATIONAL, 1 HORIZONTAL (y mirrored), 2 VERTICAL (x mirrored)
        return (w - 1 - x, h - 1 - y) if sym == 0 else (x, h - 1 - y) if sym == 1 else (w - 1 - x, y)
    for m in list(mines):
        if mirror(m) not in mines:
            mines.append(mirror(m))
    kings = {}
    for b in meta.get('bodies') or []:
        if b.get('type') == 'KING' and b.get('team') in (1, 2) and b['team'] not in kings:
            kings[b['team']] = (b['x'], b['y'])
    if len(kings) < 2:
        for r in game_report.rows(pre + '.turns.tsv'):
            t = int(r['team'])
            if r['type'] == 'KING' and t in (1, 2) and t not in kings:
                kings[t] = (int(r['x']), int(r['y']))
            if len(kings) == 2:
                break
    piles = collections.defaultdict(list)   # tile -> [[round, amount, source]] not yet picked up; source = mine or None
    tile = collections.Counter()             # tile -> cheese on it
    per_mine = {m: {'spawned': 0, 'spawns': 0, 'got': {1: 0, 2: 0}, 'wait': {1: 0, 2: 0}, 'first': {1: None, 2: None}}
                for m in mines}
    team = {t: {'pickups': 0, 'picked': 0, 'drops': 0, 'byside': collections.Counter()} for t in (1, 2)}
    rounds = 0
    for r in game_report.rows(pre + '.actions.tsv'):
        rn = int(r['round'])
        if hi is not None and rn > hi:
            break
        rounds = max(rounds, rn)
        act = r['action']
        if act == 'cheesespawn':
            loc, total = xy(r['a']), int(r['b'] or 0)
            add = total - tile[loc]
            m = mine_of(loc, mines)
            if add == 20 and m is not None:
                src = m
                per_mine[m]['spawned'] += add
                per_mine[m]['spawns'] += 1
            else:
                # a death drop records only the rat's cheese; a partial pickup records the remainder
                src, add = None, (total if add <= 0 else add)
                if total < tile[loc]:
                    add = 0
            if add > 0:
                piles[loc].append([rn, add, src])
                tile[loc] += add
        elif act == 'pickup':
            t = int(r['team'])
            loc = xy(r['a'])
            if t not in (1, 2):
                continue
            team[t]['pickups'] += 1
            for prn, amt, src in piles.pop(loc, []):
                team[t]['picked'] += amt
                if src is None:
                    team[t]['drops'] += amt
                    continue
                pm = per_mine[src]
                pm['got'][t] += amt
                pm['wait'][t] += amt * (rn - prn)
                if pm['first'][t] is None:
                    pm['first'][t] = rn
                s = side(src, kings)
                team[t]['byside']['own' if s == 'AB'[t - 1] else 'mid' if s == '-' else 'enemy'] += amt
            tile[loc] = 0
    rats = collections.Counter()             # (mine, team) -> rat-rounds within R of the mine
    for r in game_report.rows(pre + '.turns.tsv'):
        t = int(r['team'])
        if r['type'] != 'RAT' or t not in (1, 2) or hi is not None and int(r['round']) > hi:
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
    out = {'map': meta.get('map'), 'rounds': rounds, 'teams': {'A': meta.get('teamA'), 'B': meta.get('teamB')},
           'mines': [], 'A': {}, 'B': {}}
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
    return out


def render(o):
    lines = [f"{o['map']}: {o['teams']['A']} (A) vs {o['teams']['B']} (B), {o['rounds']} rounds",
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
    return '\n'.join(lines)


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


def render_sides(tot, n, name):
    lines = [f"{name}, {n} games: per game, by mine side ({' / '.join(KEYS)})"]
    for s, t in tot.items():
        if n:
            lines.append(f"  {s:6s} " + '  '.join(f"{k} {t[k] / n:.0f}" for k in KEYS))
    return '\n'.join(lines)


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument('src', nargs='+')
    ap.add_argument('--game', type=int, default=0)
    ap.add_argument('--hi', type=int, default=None)
    ap.add_argument('--team', default=None)
    ap.add_argument('--json', action='store_true')
    a = ap.parse_args(argv)
    if a.team is None:
        d = game_report.dump(a.src[0]) if a.src[0].endswith('.bc26') else a.src[0]
        o = analyse(d, a.game, a.hi)
        if a.src[0].endswith('.bc26'):
            shutil.rmtree(d, ignore_errors=True)
        print(json.dumps(o) if a.json else render(o))
        return
    outs = []
    for src in a.src:
        d = game_report.dump(src) if src.endswith('.bc26') else src
        g = 0
        while os.path.exists(os.path.join(d, f'g{g}.meta.json')):
            outs.append(analyse(d, g, a.hi))
            g += 1
        if src.endswith('.bc26'):
            shutil.rmtree(d, ignore_errors=True)
    tot, n = by_side(outs, a.team)
    print(json.dumps({'games': n, 'sides': tot}) if a.json else render_sides(tot, n, a.team))


if __name__ == '__main__':
    main()
