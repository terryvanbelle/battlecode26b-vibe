#!/usr/bin/env python3
"""Gauntlet against the benchmark field (research/field/FIELD.tsv: name<TAB>classdir<TAB>package).

  gauntlet.py cells OURS [--maps LIST|@file] [--seeds 0] [--only NAME,NAME] > cells.tsv
      every field bot vs OURS on every map, both sides
  gauntlet.py table RUN/results.jsonl [--ours HASH]
      per-opponent wins/games and points share for our bot, worst first, plus totals
  gauntlet.py summary RUN/results.jsonl
      one line per bot of ours (a bots/ spec) in a mixed run: wins/games and mean points, best first
"""
import argparse, collections, json, os, sys

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
FIELD = os.path.join(REPO, 'research', 'field', 'FIELD.tsv')
PANEL = 'DefaultSmall,DefaultMedium,DefaultLarge,cheesefarm,sittingducks,rift,knifefight,whereisthecheese'


def field(path=FIELD):
    out = []
    for line in open(path):
        if line.strip() and not line.startswith('#'):
            name, cls, pkg = line.rstrip('\n').split('\t')
            out.append((name, f'cls:{cls}:{pkg}'))
    return out


def cells(ours, maps, seeds, only=None, path=FIELD):
    rows = []
    for name, spec in field(path):
        if only and name not in only:
            continue
        for s in seeds:
            for m in maps:
                rows.append((ours, spec, m, s))
                rows.append((spec, ours, m, s))
    return rows


def table(results, ours_hash=None, path=FIELD):
    """Summarise from our bot's side. Our bot = the hash present in every row (or --ours)."""
    rows = [json.loads(l) for l in open(results) if l.strip()]
    if not rows:
        return []
    if ours_hash is None:
        c = collections.Counter()
        for r in rows:
            c[r['a_hash']] += 1; c[r['b_hash']] += 1
        ours_hash = c.most_common(1)[0][0]
    names = {}
    for name, spec in field(path):
        names[spec] = name
    agg = collections.defaultdict(lambda: [0, 0, 0.0])   # wins, games, our points
    for r in rows:
        if r['a_hash'] == ours_hash:
            side, opp = 'A', r.get('b_spec') or r['b']
        elif r['b_hash'] == ours_hash:
            side, opp = 'B', r.get('a_spec') or r['a']
        else:
            continue
        opp = names.get(opp, opp)
        a = agg[opp]
        a[1] += 1
        a[0] += 1 if r['winner'] == side else 0
        a[2] += r['ptsA' if side == 'A' else 'ptsB']
    return sorted(((o, w, g, p / g) for o, (w, g, p) in agg.items()), key=lambda t: (t[1] / t[2], t[3]))


def summary(results):
    """Per our-bot totals for a run holding several of our bots (sides with a bots/ spec) vs field bots:
    (bot, wins, games, mean points, kill ratio = enemy rat deaths / our rat deaths, mean rounds)."""
    agg = collections.defaultdict(lambda: [0, 0, 0.0, 0, 0, 0])
    for l in open(results):
        if not l.strip():
            continue
        r = json.loads(l)
        for side, spec in (('A', r.get('a_spec') or r['a']), ('B', r.get('b_spec') or r['b'])):
            if not spec.startswith('bots/'):
                continue
            other = 'B' if side == 'A' else 'A'
            a = agg[spec[5:].rstrip('/')]
            a[1] += 1
            a[0] += 1 if r['winner'] == side else 0
            a[2] += r['ptsA' if side == 'A' else 'ptsB']
            a[3] += (r.get(other) or {}).get('deathsRat', 0)
            a[4] += (r.get(side) or {}).get('deathsRat', 0)
            a[5] += r.get('rounds', 0)
    return sorted(((b, w, g, p / g, k / max(1, d), n / g) for b, (w, g, p, k, d, n) in agg.items()),
                  key=lambda t: (-t[1] / t[2], -t[3]))


def main():
    ap = argparse.ArgumentParser()
    sub = ap.add_subparsers(dest='cmd', required=True)
    c = sub.add_parser('cells'); c.add_argument('ours'); c.add_argument('--maps', default=PANEL)
    c.add_argument('--seeds', default='0'); c.add_argument('--only', default=None)
    t = sub.add_parser('table'); t.add_argument('results'); t.add_argument('--ours', default=None)
    u = sub.add_parser('summary'); u.add_argument('results')
    o = ap.parse_args()
    if o.cmd == 'summary':
        for b, w, g, p, kd, n in summary(o.results):
            print(f'{b:20s} {w:3d}/{g:<3d} pts {p:5.1f}  kills/deaths {kd:4.2f}  rounds {n:5.0f}')
        return
    if o.cmd == 'cells':
        maps = [l.strip() for l in open(o.maps[1:])] if o.maps.startswith('@') else o.maps.split(',')
        only = set(o.only.split(',')) if o.only else None
        for r in cells(o.ours, [m for m in maps if m], o.seeds.split(','), only):
            print('\t'.join(map(str, r)))
    else:
        tw = tg = 0
        for opp, w, g, p in table(o.results, o.ours):
            print(f'{opp:44s} {w:3d}/{g:<3d} pts {p:5.1f}')
            tw += w; tg += g
        print(f'{"TOTAL":44s} {tw:3d}/{tg:<3d}')


if __name__ == '__main__':
    main()
