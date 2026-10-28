#!/usr/bin/env python3
"""Progress chart: progress/progress.png, one panel per metric group (needs matplotlib: run with tools/.venv).

  plot_progress.py [--tsv research/progress.tsv] [--out progress/]

research/progress.tsv columns: version, date (PDT, YYYY-MM-DD HH:MM), metric, value. One row per measurement.
Each metric becomes one series; a panel plots value against version order (first appearance in the file).
Metric groups: the part of the metric name before the first '.', e.g. pts.top6.3m and pts.top6.new3m share 'pts'.
"""
import argparse, collections, os

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


def groups(series):
    g = collections.OrderedDict()
    for m, s in series.items():
        g.setdefault(m.split('.')[0], collections.OrderedDict())[m] = s
    return g


def chart(versions, series, path):
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
    ax = axes[-1, 0]
    ax.set_xticks(range(len(versions)))
    ax.set_xticklabels(versions, rotation=70, fontsize=7)
    fig.suptitle('Progress by attempt (research/progress.tsv)', fontsize=12)
    fig.tight_layout()
    fig.savefig(path, dpi=110)
    plt.close(fig)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--tsv', default=os.path.join(REPO, 'research', 'progress.tsv'))
    ap.add_argument('--out', default=os.path.join(REPO, 'progress'))
    o = ap.parse_args()
    versions, series = load(o.tsv)
    os.makedirs(o.out, exist_ok=True)
    chart(versions, series, os.path.join(o.out, 'progress.png'))
    print(f'{len(groups(series))} panels, {len(versions)} versions -> {o.out}/progress.png')


if __name__ == '__main__':
    main()
