#!/usr/bin/env python3
"""Points games at the round limit (field_report games --csv): what decided them.
  ptsgames.py CSV  -> per game: opp map side won pts, kings, cat dmg, transfer; then totals by component."""
import csv, sys, collections

W = {'cat': 0.3, 'king': 0.5, 'x': 0.2}   # war weights (GameWorld.setWinnerIfMorePoints)


def share(a, b):
    return a / (a + b) if a + b else 0.0


rows = [r for r in csv.DictReader(open(sys.argv[1])) if r['end'] == 'limit']
agg = collections.defaultdict(lambda: [0, 0])
dec = collections.Counter()
print('opp map side won ptsUs ptsThem kUs kThem catUs catThem xUs xThem | dKing dCat dX (points, us - them)')
for r in sorted(rows, key=lambda r: (r['won'], r['opp'], r['map'])):
    f = lambda k: int(float(r[k] or 0))
    kU, kT, cU, cT, xU, xT = f('kUs'), f('kThem'), f('catUs'), f('catThem'), f('xUs'), f('xThem')
    dk = 100 * W['king'] * (2 * share(kU, kT) - 1)
    dc = 100 * W['cat'] * (2 * share(cU, cT) - 1)
    dx = 100 * W['x'] * (2 * share(xU, xT) - 1)
    won = r['won'] == 'True'
    print(r['opp'][:22], r['map'][:18], r['side'], 'W' if won else 'L', r['ptsUs'], r['ptsThem'], kU, kT, cU, cT, xU, xT,
          '| %+.0f %+.0f %+.0f' % (dk, dc, dx))
    for k, v in (('king', dk), ('cat', dc), ('x', dx)):
        agg[(won, k)][0] += v
        agg[(won, k)][1] += 1
    if not won:
        # the components that went against us, and whether flipping one alone (to parity) would win
        tot = dk + dc + dx
        for k, v in (('king', dk), ('cat', dc), ('x', dx)):
            if v < 0 and tot - v > 0:
                dec['parity on ' + k + ' alone wins'] += 1
        dec['lost'] += 1
print()
for won in (False, True):
    n = sum(1 for r in rows if (r['won'] == 'True') == won)
    print('won' if won else 'lost', n, ' '.join('%s %+.1f' % (k, agg[(won, k)][0] / max(1, agg[(won, k)][1]))
                                              for k in ('king', 'cat', 'x')))
for k, v in dec.items():
    print(k, v)
