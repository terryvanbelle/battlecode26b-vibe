#!/usr/bin/env python3
"""Pick a ladder panel: field bots one of our builds beats a middling share of the time on the replica.

  panel_pick.py BUILD [--games progress/games.csv] [--lo 25] [--hi 75] [--min 6]

From the replica rows of games.csv (one row per match; scoreA/scoreB = games won), per field opponent of us:BUILD:
games won and played, and the win rate. The panel is every opponent with at least --min games whose rate lies in
[lo, hi] percent, nearest 50% first; the rest are listed as too easy / too hard / too few games.
"""
import argparse, collections, csv, os

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def rates(rows, build):
    """{opponent: [won, played]} for us:BUILD over replica match rows."""
    me = 'us:' + build
    acc = collections.defaultdict(lambda: [0, 0])
    for r in rows:
        if r['source'] != 'replica' or me not in (r['teamA'], r['teamB']):
            continue
        we_a = r['teamA'] == me
        opp = r['teamB'] if we_a else r['teamA']
        if opp.startswith('us:'):
            continue
        a, b = int(r['scoreA'] or 0), int(r['scoreB'] or 0)
        acc[opp][0] += a if we_a else b
        acc[opp][1] += a + b
    return dict(acc)


def pick(acc, lo, hi, least):
    """(panel, easy, hard, few): opponent names; the panel nearest 50% first."""
    panel, easy, hard, few = [], [], [], []
    for o, (w, n) in acc.items():
        if n < least:
            few.append(o)
            continue
        p = 100.0 * w / n
        (panel if lo <= p <= hi else easy if p > hi else hard).append(o)
    rate = lambda o: acc[o][0] / acc[o][1] if acc[o][1] else 0
    panel.sort(key=lambda o: (abs(rate(o) - 0.5), o))
    return panel, sorted(easy, key=rate), sorted(hard, key=rate), sorted(few)


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument('build')
    ap.add_argument('--games', default=os.path.join(REPO, 'progress', 'games.csv'))
    ap.add_argument('--lo', type=float, default=25)
    ap.add_argument('--hi', type=float, default=75)
    ap.add_argument('--min', type=int, default=6)
    a = ap.parse_args(argv)
    with open(a.games) as f:
        acc = rates(list(csv.DictReader(f)), a.build)
    panel, easy, hard, few = pick(acc, a.lo, a.hi, a.min)
    show = lambda o: f'{o} {acc[o][0]}/{acc[o][1]}'
    print(f'panel ({len(panel)}, {a.lo:g}-{a.hi:g}%):', ', '.join(map(show, panel)))
    print('too easy:', ', '.join(map(show, easy)))
    print('too hard:', ', '.join(map(show, hard)))
    print(f'fewer than {a.min} games:', ', '.join(map(show, few)))


if __name__ == '__main__':
    main()
