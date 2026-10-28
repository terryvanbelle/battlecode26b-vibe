#!/usr/bin/env python3
"""Milestones: dated events (e.g. the owner changing the agent's effort level) marked on every chart with a time axis,
so a change in performance can be lined up against them.

  milestones.py add LABEL [--when 'YYYY-MM-DD HH:MM']    append one (default: now, PDT)
  milestones.py list

progress/milestones.csv columns: date (PDT, YYYY-MM-DD HH:MM), label. Hand-kept. Read by tools/plot_progress.py
(progress.png), tools/replica_report.py (rating-history.png) and tools/attempts.py (ATTEMPTS.md rows).
"""
import argparse, csv, datetime as dt, os, sys
from zoneinfo import ZoneInfo

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CSV = os.path.join(REPO, 'progress', 'milestones.csv')
PDT = ZoneInfo('America/Los_Angeles')
COLOR = '#343a40'   # not in any chart's series palette
FMT = '%Y-%m-%d %H:%M'


def parse(s):
    """'YYYY-MM-DD HH:MM' (PDT) -> aware datetime."""
    return dt.datetime.strptime(s.strip(), FMT).replace(tzinfo=PDT)


def try_parse(s):
    """parse(), or None for a missing or malformed date."""
    try:
        return parse(s or '')
    except ValueError:
        return None


def read(path=CSV):
    """[(aware datetime, label)] oldest first; [] when the file is missing. Malformed rows are skipped with a warning."""
    if not os.path.exists(path):
        return []
    out = []
    with open(path, newline='', encoding='utf-8-sig') as f:
        for r in csv.DictReader(f):
            d = (r.get('date') or '').strip()
            if not d:
                continue
            when = try_parse(d)
            if when is None:
                print(f'milestones: skipping bad date {d!r} in {path}', file=sys.stderr)
                continue
            out.append((when, (r.get('label') or '').strip()))
    return sorted(out)


def tag(i):
    """Short on-chart tag of the i-th milestone (0-based, oldest first); the full label goes in a footnote."""
    return f'M{i + 1}'


def footnote(marks):
    return '   '.join(f'{tag(i)} {when:%m-%d %H:%M} {label}' for i, (when, label) in enumerate(marks))


def add(label, when=None, path=CSV):
    when = when or dt.datetime.now(PDT).strftime(FMT)
    parse(when)
    new = not os.path.exists(path)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'a', newline='') as f:
        w = csv.writer(f)
        if new:
            w.writerow(['date', 'label'])
        w.writerow([when, label])
    return when


def index_x(dates, when):
    """Position of a milestone on an axis of items 0..n-1 in time order (dates: aware datetime or None per item):
    halfway between the last item dated before it and the next item."""
    before = [i for i, d in enumerate(dates) if d is not None and d < when]
    return (max(before) if before else -1) + 0.5


def place(dates, marks):
    """[x] per milestone on an axis of items in time order: milestones that fall in the same gap between two items
    are spread evenly across it instead of overprinting."""
    gaps = [index_x(dates, when) for when, _ in marks]
    xs = []
    for i, g in enumerate(gaps):
        same = [j for j, h in enumerate(gaps) if h == g]
        k, m = same.index(i), len(same)
        xs.append(g - 0.5 + (k + 1) / (m + 1))
    return xs


def main(argv=None):
    ap = argparse.ArgumentParser()
    sub = ap.add_subparsers(dest='cmd', required=True)
    p = sub.add_parser('add'); p.add_argument('label'); p.add_argument('--when')
    sub.add_parser('list')
    a = ap.parse_args(argv)
    if a.cmd == 'add':
        print(f'{add(a.label, a.when)} {a.label} -> {CSV}')
    else:
        for when, label in read():
            print(f'{when:{FMT}} PDT  {label}')


if __name__ == '__main__':
    main()
