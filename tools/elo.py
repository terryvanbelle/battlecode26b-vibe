#!/usr/bin/env python3
"""Bradley-Terry ladder of every player in progress/games.csv: progress/ELO.md and progress/elo.png.

  elo.py [--games progress/games.csv] [--attempts progress/attempts.csv] [--out progress] [--quiet]

Each of our builds (us:<package>) is its own player. Field score = expected score of a build against every
field player (non-us), one game each. elo.png needs matplotlib (tools/.venv); without it only ELO.md is written.
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


def compute(rows):
    games = [(r['teamA'], r['teamB'], float(r['scoreA']), float(r['scoreB'])) for r in rows]
    rt = elolib.fit(games)
    field = [p for p in rt if not p.startswith('us:')]
    fs = {p: elolib.field_score(rt, p, field) for p in rt}
    return rt, field, fs


def render(rt, field, fs, order, nrows, srcs):
    ranked = sorted(rt, key=lambda p: -rt[p].rating)
    rank = {p: i for i, p in enumerate(ranked, 1)}
    ours = [p for p in rt if p.startswith('us:')]
    pos = {c: i for i, c in enumerate(order)}
    ours.sort(key=lambda p: (pos.get(p[3:], len(pos)), p))
    L = ['# Ladder', '',
         f'{nrows} rows of progress/games.csv ({srcs.get("local", 0)} local games, {srcs.get("replica", 0)} replica '
         f'matches), rated by a batch Bradley-Terry fit on the Elo scale (`tools/elolib.py`): each pair of players counts '
         f'at most {elolib.PAIR_CAP} games, every player carries one virtual win and loss against a fixed anchor, and '
         f'ratings are relative to a mean of 1500 over all {len(rt)} players. Each of our builds is its own player. '
         f'Regenerate with `tools/elo.py`; do not edit.', '',
         '**Caveat**: local games are win/loss only, and most of our gauntlets are against the six strongest field bots, '
         'which we rarely beat. Builds with no wins are indistinguishable here (they sit at the prior), and one or two '
         'wins against the strongest bot lift a build far more than its play warrants. Rank attempts by the gauntlet '
         'points share in `progress/ATTEMPTS.md` first; use this ladder for cross-build and field context.', '',
         '![ratings of our builds](elo.png)', '',
         f'Our builds, in attempt order (rating +- 95%; field score = expected score against each of the {len(field)} '
         'field players, one game each):', '',
         '| build | rating | rank | games | record | field score |', '|---|---|---|---|---|---|']
    for p in ours:
        r = rt[p]
        L.append(f'| {p[3:]} | {r.rating:.0f} +- {1.96 * r.se:.0f} | {rank[p]} of {len(rt)} | {r.games:g} | '
                 f'{r.wins:g}-{r.losses:g} | {100 * fs[p]:.1f}% |')
    L += ['', 'All players:', '', '| # | player | rating | games | record | field score |', '|---:|---|---|---|---|---|']
    for p in ranked:
        r = rt[p]
        L.append(f'| {rank[p]} | {p} | {r.rating:.0f} +- {1.96 * r.se:.0f} | {r.games:g} | {r.wins:g}-{r.losses:g} | '
                 f'{100 * fs[p]:.1f}% |')
    return '\n'.join(L) + '\n', ours


def chart(rt, ours, field, path):
    try:
        import matplotlib
        matplotlib.use('Agg')
        import matplotlib.pyplot as plt
    except ImportError:
        return False
    fig, ax = plt.subplots(figsize=(12, 5))
    xs = list(range(len(ours)))
    ax.errorbar(xs, [rt[p].rating for p in ours], yerr=[1.96 * rt[p].se for p in ours], fmt='o-', color='#2a6fdb',
                ecolor='#a5c3f0', capsize=2, label='our builds (+-95%)')
    top = sorted(field, key=lambda p: -rt[p].rating)[:6]
    for k, p in enumerate(top):
        ax.axhline(rt[p].rating, color='#d9480f', alpha=0.25 + 0.1 * (k == 0), lw=1)
        ax.text(len(ours) - 0.5, rt[p].rating, p, fontsize=7, color='#d9480f', va='bottom', ha='right')
    ax.set_xticks(xs)
    ax.set_xticklabels([p[3:] for p in ours], rotation=70, fontsize=7)
    ax.set_ylabel('Bradley-Terry rating (Elo scale)')
    ax.set_title('Our builds vs the field (lines: the six best field players)')
    ax.grid(axis='y', color='#e9ecef')
    ax.legend(loc='lower right', fontsize=8)
    fig.tight_layout()
    fig.savefig(path, dpi=110)
    plt.close(fig)
    return True


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument('--games', default=os.path.join(REPO, 'progress', 'games.csv'))
    ap.add_argument('--attempts', default=os.path.join(REPO, 'progress', 'attempts.csv'))
    ap.add_argument('--out', default=os.path.join(REPO, 'progress'))
    ap.add_argument('--quiet', action='store_true')
    a = ap.parse_args(argv)
    rows = G.read(a.games)
    srcs = {}
    for r in rows:
        srcs[r['source']] = srcs.get(r['source'], 0) + 1
    rt, field, fs = compute(rows)
    md, ours = render(rt, field, fs, build_order(a.attempts), len(rows), srcs)
    open(os.path.join(a.out, 'ELO.md'), 'w').write(md)
    png = chart(rt, ours, field, os.path.join(a.out, 'elo.png'))
    if not a.quiet:
        best = max(ours, key=lambda p: rt[p].rating) if ours else None
        print(f'{len(rt)} players, {len(ours)} builds; best {best} {rt[best].rating:.0f}' if best else 'no builds')
    print(f'ELO.md{" + elo.png" if png else " (no matplotlib: elo.png skipped)"} -> {a.out}')


if __name__ == '__main__':
    main()
