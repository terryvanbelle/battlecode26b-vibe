#!/usr/bin/env python3
"""Side-symmetry check of a local game pair: the same two bots on the same symmetric map, sides swapped.

  mirror_check.py X.bc26|TSVDIR Y.bc26|TSVDIR [--game N] [--cheese] [--show N]

Our bot is meant to be side-symmetric (G.java's side-symmetric random stream, Baby.java's mirror-invariant seed), so
team A of X should play exactly the mirror image of team B of Y, and X's B the mirror of Y's A. Per round this
compares the multisets of (type, x, y, dir, hp[, cheese]) of each team's turns (src=own records), Y's mapped through
the map's symmetry (kings by their centre, like rats; directions flipped as GameWorld.flipDirBySymmetry). Cats are
the same robots in both games: they are compared by id, unmirrored. Robot ids differ between the games, so a
divergence prints both sides' robots with their turn index in their team's round, their previous record and
indicator string, then the turn orders where they differ. Known sources of asymmetry: anything derived from
rc.getID() (Baby.java's scout slot) and side-dependent bytecode counts.
Exit 0 when every group mirrors through the shorter game, 1 when one diverges, 2 on bad input.
"""
import argparse, collections, json, os, sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import game_report as GR  # noqa: E402

DELTA = {1: (-1, 0), 2: (-1, -1), 3: (0, -1), 4: (1, -1), 5: (1, 0), 6: (1, 1), 7: (0, 1), 8: (-1, 1)}
ORD = {v: k for k, v in DELTA.items()}
FLIP = {0: (-1, -1), 1: (1, -1), 2: (-1, 1)}     # MapSymmetry 0 ROTATIONAL, 1 HORIZONTAL, 2 VERTICAL: (sx, sy)


def mirror_dir(d, sym):
    if d not in DELTA:
        return d
    sx, sy = FLIP[sym]
    dx, dy = DELTA[d]
    return ORD[(sx * dx, sy * dy)]


def mirror_xy(x, y, sym, w, h):
    sx, sy = FLIP[sym]
    return (w - 1 - x if sx < 0 else x), (h - 1 - y if sy < 0 else y)


def load(d, g):
    """(meta, {round: [own turn rows in seq order]})"""
    with open(os.path.join(d, f'g{g}.meta.json')) as f:
        meta = json.load(f)
    by = collections.defaultdict(list)
    for r in GR.own(GR.rows(os.path.join(d, f'g{g}.turns.tsv'))):
        by[int(r['round'])].append(r)
    for v in by.values():
        v.sort(key=lambda r: int(r.get('seq') or 0))
    return meta, by


class Side:
    """One team's (or the cats') records in one game, as comparable states."""
    def __init__(self, by, team, mirror=None, cheese=False, ids=False):
        self.by, self.team, self.mirror, self.cheese, self.ids = by, team, mirror, cheese, ids
        self.last = {}

    def state(self, r):
        x, y, d = int(r['x']), int(r['y']), int(r['dir'])
        if self.mirror:
            sym, w, h = self.mirror
            x, y = mirror_xy(x, y, sym, w, h)
            d = mirror_dir(d, sym)
        s = (r['type'], x, y, d, int(r['hp'])) + ((int(r['cheese'] or 0),) if self.cheese else ())
        return (int(r['id']),) + s if self.ids else s

    def turns(self, rn):
        return [r for r in self.by.get(rn, []) if int(r['team']) == self.team]


def describe(side, rn, keys, show):
    """Lines for the robots of `side` whose state at round rn is in keys."""
    out = []
    for i, r in enumerate(side.turns(rn)):
        s = side.state(r)
        if s in keys and len(out) < show:
            prev = side.last.get(r['id'])
            out.append(f"      id {r['id']:>6} turn {i:3d}  {s}  prev {prev[1] if prev else '-'} (r{prev[0] if prev else '-'})"
                       + (f"  ind {r['ind'][:120]!r}" if r.get('ind') else ''))
    return out


def compare(name, sx, sy, rounds, show):
    """(first divergent round or None, divergent rounds, lines)."""
    first, n, lines = None, 0, []
    for rn in range(1, rounds + 1):
        tx, ty = sx.turns(rn), sy.turns(rn)
        cx = collections.Counter(sx.state(r) for r in tx)
        cy = collections.Counter(sy.state(r) for r in ty)
        if cx != cy:
            n += 1
            if first is None:
                first = rn
                only_x, only_y = set(cx - cy), set(cy - cx)
                lines.append(f'  {name}: first divergence r{rn} ({len(tx)} vs {len(ty)} turns)')
                lines.append(f'    X only ({sum((cx - cy).values())}):')
                lines += describe(sx, rn, only_x, show)
                lines.append(f'    Y only ({sum((cy - cx).values())}, in X\'s frame):')
                lines += describe(sy, rn, only_y, show)
                order = [(i, a, b) for i, (a, b) in enumerate(zip([sx.state(r) for r in tx], [sy.state(r) for r in ty]))
                         if a != b]
                lines.append(f'    turn order: {len(order)} of {min(len(tx), len(ty))} positions differ'
                             + ''.join(f'\n      turn {i:3d}  X {a}  Y {b}' for i, a, b in order[:show]))
        for s, ts in ((sx, tx), (sy, ty)):
            for r in ts:
                s.last[r['id']] = (rn, s.state(r))
    if first is None:
        lines.append(f'  {name}: mirrored through r{rounds}')
    else:
        lines.append(f'  {name}: {n} of {rounds} rounds differ')
    return first, n, lines


def check(dx, dy, gx=0, gy=0, cheese=False, show=8):
    """({group: first divergent round or None}, report lines)."""
    mx, bx = load(dx, gx)
    my, by = load(dy, gy)
    for k in ('map', 'width', 'height', 'symmetry'):
        if mx.get(k) != my.get(k):
            raise ValueError(f'not the same map: {k} {mx.get(k)} vs {my.get(k)}')
    sym, w, h = mx['symmetry'], mx['width'], mx['height']
    if sym not in FLIP:
        raise ValueError(f'unknown symmetry {sym}')
    rx, ry = max(bx, default=0), max(by, default=0)
    lines = [f"X {mx.get('teamA')} (A) vs {mx.get('teamB')} (B), Y {my.get('teamA')} (A) vs {my.get('teamB')} (B); "
             f"{mx['map']} {w}x{h} symmetry {('rotational', 'horizontal', 'vertical')[sym]}; rounds X {rx} Y {ry}"]
    if (mx.get('teamA'), mx.get('teamB')) != (my.get('teamB'), my.get('teamA')):
        lines.append('  warning: the team names are not swapped between the games')
    out = {}
    m = (sym, w, h)
    for name, a, b in ((f"{mx.get('teamA')} (X A ~ Y B)", 1, 2), (f"{mx.get('teamB')} (X B ~ Y A)", 2, 1)):
        out[name], _, ls = compare(name, Side(bx, a, None, cheese), Side(by, b, m, cheese), min(rx, ry), show)
        lines += ls
    out['cats'], _, ls = compare('cats (by id, unmirrored)', Side(bx, 0, ids=True), Side(by, 0, ids=True), min(rx, ry), show)
    lines += ls
    return out, lines


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument('x'); ap.add_argument('y')
    ap.add_argument('--game', type=int, default=0, help='0-based game index in both replays')
    ap.add_argument('--cheese', action='store_true', help='compare carried cheese too')
    ap.add_argument('--show', type=int, default=8, help='robots / turn positions listed per divergence')
    o = ap.parse_args(argv)
    dx, dy = GR.source(o.x), GR.source(o.y)
    for d, src in ((dx, o.x), (dy, o.y)):
        if o.game not in GR.games(d):
            print(f'{src}: no game {o.game}; games are {GR.games(d)}', file=sys.stderr)
            return 2
    try:
        res, lines = check(dx, dy, o.game, o.game, o.cheese, o.show)
    except ValueError as e:
        print(f'mirror_check: {e}', file=sys.stderr)
        return 2
    print('\n'.join(lines))
    return 1 if any(v is not None for v in res.values()) else 0


if __name__ == '__main__':
    sys.exit(main())
