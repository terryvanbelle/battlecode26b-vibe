#!/usr/bin/env python3
"""Progress chart: progress/progress.png, one panel per metric group (needs matplotlib: run with tools/.venv).

  plot_progress.py [--tsv research/progress.tsv] [--out progress/]

research/progress.tsv columns: version, date (PDT, YYYY-MM-DD HH:MM), metric, value. One row per measurement.
Each metric becomes one series; a panel plots value against versions in date order (see version_dates).
Metric groups: the part of the metric name before the first '.', e.g. pts.top6.3m and pts.top6.new3m share 'pts'.
Milestones (progress/milestones.csv, tools/milestones.py) are dashed vertical lines tagged M1, M2, ... between the
last version dated before them and the next, with their full labels in a footnote.
"""
import argparse, collections, csv, os, sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import milestones as MS

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
COLORS = ['#2a6fdb', '#d9480f', '#2b8a3e', '#7048e8', '#c2255c', '#0b7285', '#5c940d', '#495057']
TITLES = {'pts': 'points share vs the six strongest field bots (%)', 'kd': 'enemy deaths per our death',
          'win': 'win rate vs examplefuncsplayer'}


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
    for line in open(tsv):
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


def chart(versions, series, path, dates=None, marks=()):
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
    fig.suptitle('Progress by attempt, in date order (research/progress.tsv)', fontsize=12)
    if marks:
        # full milestone labels as a footnote: they cannot overprint or push the panels around
        fig.text(0.01, 0.005, 'Milestones (dashed): ' + MS.footnote(marks), fontsize=7, color=MS.COLOR, va='bottom',
                 wrap=True)
        fig.tight_layout(rect=(0, 0.03, 1, 1))
    else:
        fig.tight_layout()
    fig.savefig(path, dpi=110)
    plt.close(fig)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--tsv', default=os.path.join(REPO, 'research', 'progress.tsv'))
    ap.add_argument('--out', default=os.path.join(REPO, 'progress'))
    ap.add_argument('--attempts', default=os.path.join(REPO, 'progress', 'attempts.csv'))
    ap.add_argument('--milestones', default=MS.CSV)
    o = ap.parse_args()
    versions, series = load(o.tsv)
    os.makedirs(o.out, exist_ok=True)
    marks = MS.read(o.milestones)
    versions, dates = in_time_order(versions, version_dates(versions, o.tsv, o.attempts))
    chart(versions, series, os.path.join(o.out, 'progress.png'), dates, marks)
    print(f'{len(groups(series))} panels, {len(versions)} versions, {len(marks)} milestones -> {o.out}/progress.png')


if __name__ == '__main__':
    main()
