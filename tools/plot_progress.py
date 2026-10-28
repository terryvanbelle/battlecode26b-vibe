#!/usr/bin/env python3
"""Progress charts (need matplotlib: run with tools/.venv).

  plot_progress.py [--ratings progress/ratings.csv] [--games progress/games.csv] [--attempts progress/attempts.csv]
                   [--milestones progress/milestones.csv] [--tsv research/progress.tsv] [--out progress/]

progress.png, laid out like battlecode23-vibe's: our builds in date order on the ladder of tools/elo.py
(progress/ratings.csv). Top: the Bradley-Terry rating (Elo scale) with its 95% interval. Bottom: the expected field
score (%). Builds with fewer than MIN_GAMES games, and the dev directory (ratbot), are left out. Builds adopted as the
dev base (attempts.csv outcome adopted / submitted / kept as..., or the base of a later attempt) have filled markers
and bold labels; the rest are hollow.
metrics.png: the raw gauntlet metrics of research/progress.tsv (columns version, date (PDT, YYYY-MM-DD HH:MM),
metric, value; one row per measurement), one panel per metric group (the part of the name before the first '.', e.g.
pts.top6.3m and pts.top6.new3m share 'pts'), versions in date order (see version_dates).
Milestones (progress/milestones.csv, tools/milestones.py) are dashed vertical lines between the last build dated
before them and the next: labelled in full on progress.png, tagged M1, M2, ... with a footnote on metrics.png.
"""
import argparse, collections, csv, os, re, sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import milestones as MS

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
COLORS = ['#2a6fdb', '#d9480f', '#2b8a3e', '#7048e8', '#c2255c', '#0b7285', '#5c940d', '#495057']
TITLES = {'pts': 'points share vs the six strongest field bots (%)', 'kd': 'enemy deaths per our death',
          'win': 'win rate vs examplefuncsplayer'}
RATING, SCORE, MILESTONE = '#c0392b', '#2471a3', '#ff7f0e'   # battlecode23-vibe's progress.png colors
MIN_GAMES = 20
DEV = ('ratbot', '?')


def load(path):
    versions, series = [], collections.OrderedDict()
    for line in open(path):
        if not line.strip() or line.startswith('#') or line.startswith('version\t'):
            continue
        v, _date, metric, value = line.rstrip('\n').split('\t')[:4]
        if v not in versions:
            versions.append(v)
        series.setdefault(metric, {})[v] = float(value)
    return versions, series


def version_dates(versions, tsv, attempts_csv):
    """[aware datetime (PDT) or None] per version: the earlier of its snapshot date (attempts.csv) and its first
    measurement (a version cannot be measured before it exists; a re-snapshot can carry a later date)."""
    snap, meas = {}, {}
    if os.path.exists(attempts_csv):
        with open(attempts_csv, newline='') as f:
            for r in csv.DictReader(f):
                d = MS.try_parse(r.get('date'))
                if d is not None:
                    snap[r['codename']] = d
    for line in (open(tsv) if os.path.exists(tsv) else ()):
        if not line.strip() or line.startswith('#') or line.startswith('version\t'):
            continue
        v, d = line.split('\t')[:2]
        d = MS.try_parse(d)
        if d is not None and (v not in meas or d < meas[v]):
            meas[v] = d
    return [min((x for x in (snap.get(v), meas.get(v)) if x is not None), default=None) for v in versions]


def in_time_order(versions, dates):
    """(versions, dates) sorted by date, stably; undated versions keep their relative order at the end."""
    order = sorted(range(len(versions)), key=lambda i: (dates[i] is None, dates[i].timestamp() if dates[i] else 0, i))
    return [versions[i] for i in order], [dates[i] for i in order]


def groups(series):
    g = collections.OrderedDict()
    for m, s in series.items():
        g.setdefault(m.split('.')[0], collections.OrderedDict())[m] = s
    return g


def read_ratings(path):
    """{player: {rating, se, games, field_score}} from tools/elo.py's ratings.csv; {} when missing."""
    if not os.path.exists(path):
        return {}
    with open(path, newline='') as f:
        return {r['player']: {k: float(r[k]) for k in ('rating', 'se', 'games', 'field_score')} for r in csv.DictReader(f)}


def our_builds(ratings, min_games=MIN_GAMES):
    """Codenames of our rated builds (us:<name>) with at least min_games games, the dev directory excluded."""
    return [p[3:] for p, r in ratings.items() if p.startswith('us:') and p[3:] not in DEV and r['games'] >= min_games]


def adopted(attempts_csv):
    """Codenames that became the dev base: outcome adopted / submitted / kept as (or in) dev, or a later attempt's base."""
    out = set()
    if os.path.exists(attempts_csv):
        with open(attempts_csv, newline='') as f:
            for r in csv.DictReader(f):
                o = (r.get('outcome') or '').strip().lower()
                if re.match(r'(adopted|submitted)\b', o) or re.search(r'\bkept (as|in) (the )?dev', o):
                    out.add(r['codename'])
                if (r.get('base') or '').strip():
                    out.add(r['base'].strip())
    return out


def count_rows(path):
    if not os.path.exists(path):
        return 0
    with open(path, newline='') as f:
        return sum(1 for _ in csv.DictReader(f))


def ladder(builds, ratings, path, dates=None, marks=(), accepted=(), ngames=0):
    """bc23-style progress.png: rating +- 95% (top) and expected field score % (bottom) per build, in the given order."""
    import matplotlib
    matplotlib.use('Agg')
    import matplotlib.pyplot as plt
    n = len(builds)
    xs = list(range(n))
    rs = [ratings['us:' + b] for b in builds]
    nfield = sum(1 for p in ratings if not p.startswith('us:'))
    fig, (top, bot) = plt.subplots(2, 1, figsize=(max(8.75, 0.3 * n + 2.5), 6.5), sharex=True)
    top.errorbar(xs, [r['rating'] for r in rs], yerr=[1.96 * r['se'] for r in rs], color=RATING, ecolor=RATING,
                 lw=1.5, elinewidth=1.2, capsize=4, zorder=2)
    fs = [100 * r['field_score'] for r in rs]
    bot.plot(xs, fs, '-', color=SCORE, lw=1.5, zorder=2)
    for ax, ys, c in ((top, [r['rating'] for r in rs], RATING), (bot, fs, SCORE)):
        for x, y, b in zip(xs, ys, builds):
            ax.plot(x, y, 'o', ms=6, color=c, mfc=c if b in accepted else 'white', mew=1.5, zorder=3)
    for x, y in zip(xs, fs):
        bot.annotate(f'{y:.0f}%', (x, y), textcoords='offset points', xytext=(0, 7), ha='center', fontsize=8)
    top.set_title(f'Our builds on the ladder ({ngames} games vs {nfield} rated field bots)', fontsize=12)
    top.set_ylabel('ladder rating (BT, Elo scale)')
    bot.set_ylabel('expected field score %')
    bot.set_ylim(0, 100)
    mx = MS.place(dates or [None] * n, marks)
    for ax in (top, bot):
        ax.grid(color='#e6e6e6')
        ax.set_axisbelow(True)
        ax.set_xlim(min([-0.6] + [x - 0.4 for x in mx]), max([n - 0.4] + [x + 0.6 for x in mx]))   # room for labels
    bot.set_xticks(xs)
    bot.set_xticklabels(builds, rotation=90 if n > 20 else 0, fontsize=8)
    for t, b in zip(bot.get_xticklabels(), builds):
        if b in accepted:
            t.set_fontweight('bold')
    for x, (_, label) in zip(mx, marks):
        for ax in (top, bot):
            ax.axvline(x, color=MILESTONE, ls='--', lw=1.2, zorder=1)
        bot.text(x + 0.12, 2, label.replace('->', '\u2192'), rotation=90, color=MILESTONE, fontsize=8, ha='left',
                 va='bottom', clip_on=True)
    if accepted & set(builds):
        fig.text(0.01, 0.005, 'filled, bold: adopted as the dev base; hollow: tried. Local games count their points '
                 'share; map difficulty is factored out (tools/elo.py).', fontsize=7, color='#555555', va='bottom')
        fig.tight_layout(rect=(0, 0.02, 1, 1))
    else:
        fig.tight_layout()
    fig.savefig(path, dpi=100)
    plt.close(fig)


def metrics(versions, series, path, dates=None, marks=()):
    import matplotlib
    matplotlib.use('Agg')
    import matplotlib.pyplot as plt
    gs = groups(series)
    xs = {v: i for i, v in enumerate(versions)}
    fig, axes = plt.subplots(len(gs), 1, figsize=(12, 3.2 * len(gs)), sharex=True, squeeze=False)
    for ax, (g, ss) in zip(axes[:, 0], gs.items()):
        for n, (name, s) in enumerate(ss.items()):
            pts = [(xs[v], s[v]) for v in versions if v in s]
            ax.plot([x for x, _ in pts], [y for _, y in pts], 'o-', ms=3, color=COLORS[n % len(COLORS)], label=name)
        ax.set_title(TITLES.get(g, g), fontsize=10, loc='left')
        ax.grid(color='#e9ecef')
        ax.legend(fontsize=7, loc='upper left')
    n = len(versions)
    for i, x in enumerate(MS.place(dates or [None] * n, marks)):
        for k, ax in enumerate(axes[:, 0]):
            ax.axvline(x, color=MS.COLOR, ls='--', lw=1.3)
            if k == 0:
                t = ax.text(x, 0.99, MS.tag(i), transform=ax.get_xaxis_transform(), va='top', ha='center',
                            fontsize=7, color='white', clip_on=True,
                            bbox=dict(boxstyle='round,pad=0.15', fc=MS.COLOR, ec='none'))
                t.set_in_layout(False)
    for ax in axes[:, 0]:
        ax.set_xlim(-0.7, n - 0.3)
    ax = axes[-1, 0]
    ax.set_xticks(range(len(versions)))
    ax.set_xticklabels(versions, rotation=70, fontsize=7)
    fig.suptitle('Gauntlet metrics by attempt, in date order (research/progress.tsv)', fontsize=12)
    if marks:
        # full milestone labels as a footnote: they cannot overprint or push the panels around
        fig.text(0.01, 0.005, 'Milestones (dashed): ' + MS.footnote(marks), fontsize=7, color=MS.COLOR, va='bottom',
                 wrap=True)
        fig.tight_layout(rect=(0, 0.03, 1, 1))
    else:
        fig.tight_layout()
    fig.savefig(path, dpi=110)
    plt.close(fig)


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument('--ratings', default=os.path.join(REPO, 'progress', 'ratings.csv'))
    ap.add_argument('--games', default=os.path.join(REPO, 'progress', 'games.csv'))
    ap.add_argument('--tsv', default=os.path.join(REPO, 'research', 'progress.tsv'))
    ap.add_argument('--out', default=os.path.join(REPO, 'progress'))
    ap.add_argument('--attempts', default=os.path.join(REPO, 'progress', 'attempts.csv'))
    ap.add_argument('--milestones', default=MS.CSV)
    o = ap.parse_args(argv)
    os.makedirs(o.out, exist_ok=True)
    marks = MS.read(o.milestones)
    msg = []
    ratings = read_ratings(o.ratings)
    builds = our_builds(ratings)
    if builds:
        builds, dates = in_time_order(builds, version_dates(builds, o.tsv, o.attempts))
        ladder(builds, ratings, os.path.join(o.out, 'progress.png'), dates, marks, adopted(o.attempts),
               count_rows(o.games))
        msg.append(f'progress.png: {len(builds)} builds')
    else:
        msg.append(f'progress.png skipped (no build with {MIN_GAMES}+ games in {o.ratings})')
    if os.path.exists(o.tsv):
        versions, series = load(o.tsv)
        versions, dates = in_time_order(versions, version_dates(versions, o.tsv, o.attempts))
        metrics(versions, series, os.path.join(o.out, 'metrics.png'), dates, marks)
        msg.append(f'metrics.png: {len(groups(series))} panels, {len(versions)} versions')
    print('; '.join(msg) + f'; {len(marks)} milestones -> {o.out}')


if __name__ == '__main__':
    main()
