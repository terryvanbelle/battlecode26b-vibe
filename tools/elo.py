#!/usr/bin/env python3
"""Bradley-Terry ladder of every player in progress/games.csv: progress/ELO.md and progress/elo.png.

  elo.py [--games progress/games.csv] [--attempts progress/attempts.csv] [--out progress] [--local-field] [--quiet]

Also writes progress/ratings.csv (player, rating, se, games, field_score; read by tools/plot_progress.py).

Games against field (benchmark) bots count only from the replica, where a contestant's online scrimmages would be
played; local games count only between our builds and the stock players (examplefuncsplayer, lectureplayer).
--local-field also counts the local games against field bots (all played before 2026-10-28 08:50 PDT, when every
benchmark game moved to the replica).

Each of our builds (us:<package>) is its own player. A local game counts its cell score (tools/score.py: a win or
loss when kings decide it, the spec's points share when points do, so a 45-55 points loss is 0.45 of a win), a replica
match its game counts. Our builds were gauntleted on different map sets, so a game between one of our builds and a field bot
carries the map as a handicap key (elolib): the fit learns how hard each map is for us, and a build's rating is its
strength on an average map. Field score = expected score of a build against every field player (non-us), one game
each. elo.png needs matplotlib (tools/.venv); without it only ELO.md is written.
"""
import argparse, csv, os, sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import elolib, games as G

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def build_order(attempts_csv):
    if not os.path.exists(attempts_csv):
        return []
    with open(attempts_csv, newline='') as f:
        return [r['codename'] for r in csv.DictReader(f)]


def outcome(r):
    """(credit A, credit B) of one games.csv row: a local game's cell score, else its score (wins)."""
    if r.get('source') == 'local' and r.get('ptsA') not in (None, '') and r.get('ptsB') not in (None, ''):
        a, b = float(r['ptsA']), float(r['ptsB'])
        if a + b > 0:
            return a / (a + b), b / (a + b)
    return float(r['scoreA']), float(r['scoreB'])


def records(rows):
    """{player: [wins, losses]} counted from the rows' scores (games won, whatever the rating credits)."""
    rec = {}
    for r in rows:
        a, b = float(r['scoreA']), float(r['scoreB'])
        rec.setdefault(r['teamA'], [0, 0]); rec.setdefault(r['teamB'], [0, 0])
        rec[r['teamA']][0] += a; rec[r['teamA']][1] += b
        rec[r['teamB']][0] += b; rec[r['teamB']][1] += a
    return rec


# a stock player that is also a replica team plays as that team, so local games against it share its rating
ALIAS = {'examplefuncsplayer': 'bc26-example'}


def usable(rows, local_field=False):
    """The rows the ladder rates: replica matches, and local games unless one side is a field bot (local_field keeps
    those); stock players renamed per ALIAS."""
    def field(t):
        return not t.startswith('us:') and t not in G.STOCK
    keep = [r for r in rows if local_field or r.get('source') != 'local' or not (field(r['teamA']) or field(r['teamB']))]
    return [dict(r, teamA=ALIAS.get(r['teamA'], r['teamA']), teamB=ALIAS.get(r['teamB'], r['teamB'])) for r in keep]


def game(r):
    """elolib game tuple of one row: ours vs a field bot is keyed by the map (a replica match: 'replica', its map mix),
    with our build first."""
    a, b = r['teamA'], r['teamB']
    ca, cb = outcome(r)
    if a.startswith('us:') == b.startswith('us:'):
        return a, b, ca, cb
    k = r['map'] if r.get('source') == 'local' else 'replica'
    return (a, b, ca, cb, k) if a.startswith('us:') else (b, a, cb, ca, k)


def compute(rows, maps=False):
    """(ratings, field players, field scores) [+ {map: our handicap in Elo points} with maps=True]."""
    rt, h = elolib.fit_h([game(r) for r in rows])
    field = [p for p in rt if not p.startswith('us:')]
    fs = {p: elolib.field_score(rt, p, field) for p in rt}
    return (rt, field, fs, h) if maps else (rt, field, fs)


def render(rt, field, fs, order, nrows, srcs, rec=None, h=None, local_field=False):
    rec, h = rec or {}, h or {}
    ranked = sorted(rt, key=lambda p: -rt[p].rating)
    rank = {p: i for i, p in enumerate(ranked, 1)}
    ours = [p for p in rt if p.startswith('us:')]
    pos = {c: i for i, c in enumerate(order)}
    ours.sort(key=lambda p: (pos.get(p[3:], len(pos)), p))
    L = ['# Ladder', '',
         f'{nrows} rows of progress/games.csv ({srcs.get("local", 0)} local games, {srcs.get("replica", 0)} replica '
         f'matches), rated by a batch Bradley-Terry fit on the Elo scale (`tools/elolib.py`). A local game counts its '
         f'cell score (`tools/score.py`: a win or loss when kings decide the game, the points share when points do), a replica match its games won. A game between one of our builds '
         f'and a field bot also fits how hard its map is for us (table at the end), so builds gauntleted on different '
         f'maps compare on an average map. Each pair of players counts at most {elolib.PAIR_CAP} games, every player carries one virtual win and loss against a fixed anchor, and '
         f'ratings are relative to a mean of 1500 over all {len(rt)} players. Each of our builds is its own player. '
         f'Regenerate with `tools/elo.py`; do not edit.', '',
         f'Games against field bots are replica scrimmages only; local games are our builds against each other and the '
         f'stock players{" (and, with --local-field, the local games against field bots played before 2026-10-28 08:50 PDT)" if local_field else ""}. '
         'Record = games won and lost.', '',
         '![ratings of our builds](elo.png)', '',
         f'Our builds, in attempt order (rating +- 95%; field score = expected score against each of the {len(field)} '
         'field players, one game each):', '',
         '| build | rating | rank | games | record | field score |', '|---|---|---|---|---|---|']
    for p in ours:
        r, (w, l) = rt[p], rec.get(p, (0, 0))
        L.append(f'| {p[3:]} | {r.rating:.0f} +- {1.96 * r.se:.0f} | {rank[p]} of {len(rt)} | {r.games:g} | '
                 f'{w:g}-{l:g} | {100 * fs[p]:.1f}% |')
    L += ['', 'All players:', '', '| # | player | rating | games | record | field score |', '|---:|---|---|---|---|---|']
    for p in ranked:
        r, (w, l) = rt[p], rec.get(p, (0, 0))
        L.append(f'| {rank[p]} | {p} | {r.rating:.0f} +- {1.96 * r.se:.0f} | {r.games:g} | {w:g}-{l:g} | '
                 f'{100 * fs[p]:.1f}% |')
    if h:
        L += ['', 'Map handicap for our builds (Elo points; + = we score better there than on an average map of the '
              'mix, replica = the replica\'s map rotation):', '', '| map | handicap |', '|---|---:|']
        L += [f'| {k} | {v:+.0f} |' for k, v in sorted(h.items(), key=lambda kv: -kv[1])]
    return '\n'.join(L) + '\n', ours


def write_ratings(rt, fs, path):
    with open(path, 'w', newline='') as f:
        w = csv.writer(f)
        w.writerow(['player', 'rating', 'se', 'games', 'field_score'])
        for p in sorted(rt, key=lambda p: -rt[p].rating):
            w.writerow([p, f'{rt[p].rating:.1f}', f'{rt[p].se:.1f}', f'{rt[p].games:g}', f'{fs[p]:.4f}'])


def chart(rt, ngames, path):
    """Every player, best at the top, rating with its 95% interval; our builds (us:*) in red, the field in blue."""
    try:
        import matplotlib
        matplotlib.use('Agg')
        import matplotlib.pyplot as plt
    except ImportError:
        return False
    ps = sorted(rt, key=lambda p: -rt[p].rating)
    n = len(ps)
    fig, ax = plt.subplots(figsize=(9.6, max(4.0, 0.19 * n + 1.2)))
    for i, p in enumerate(ps):
        c = '#d62728' if p.startswith('us:') else '#1f77b4'
        ax.errorbar(rt[p].rating, n - 1 - i, xerr=1.96 * rt[p].se, fmt='o', ms=4, color=c, ecolor=c, elinewidth=1)
    ax.set_yticks(range(n))
    ax.set_yticklabels(ps[::-1], fontsize=7)
    for t in ax.get_yticklabels():
        if t.get_text().startswith('us:'):
            t.set_color('#d62728'); t.set_fontweight('bold')
    ax.set_ylim(-1, n)
    ax.grid(axis='x', color='#e0e0e0')
    ax.set_xlabel('rating (Bradley-Terry, Elo scale), 95% interval relative to the mean rating')
    ax.set_title(f'Ladder: {ngames} games; our builds in red')
    fig.tight_layout()
    fig.savefig(path, dpi=100)
    plt.close(fig)
    return True


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument('--games', default=os.path.join(REPO, 'progress', 'games.csv'))
    ap.add_argument('--attempts', default=os.path.join(REPO, 'progress', 'attempts.csv'))
    ap.add_argument('--out', default=os.path.join(REPO, 'progress'))
    ap.add_argument('--quiet', action='store_true')
    ap.add_argument('--local-field', action='store_true', help='also rate local games against field bots')
    a = ap.parse_args(argv)
    rows = usable(G.read(a.games), a.local_field)
    srcs = {}
    for r in rows:
        srcs[r['source']] = srcs.get(r['source'], 0) + 1
    rt, field, fs, h = compute(rows, maps=True)
    md, ours = render(rt, field, fs, build_order(a.attempts), len(rows), srcs, records(rows), h, a.local_field)
    open(os.path.join(a.out, 'ELO.md'), 'w').write(md)
    write_ratings(rt, fs, os.path.join(a.out, 'ratings.csv'))
    png = chart(rt, len(rows), os.path.join(a.out, 'elo.png'))
    if not a.quiet:
        best = max(ours, key=lambda p: rt[p].rating) if ours else None
        print(f'{len(rt)} players, {len(ours)} builds; best {best} {rt[best].rating:.0f}' if best else 'no builds')
    print(f'ELO.md, ratings.csv{" + elo.png" if png else " (no matplotlib: elo.png skipped)"} -> {a.out}')


if __name__ == '__main__':
    main()
