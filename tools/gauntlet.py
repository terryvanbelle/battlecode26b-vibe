#!/usr/bin/env python3
"""Gauntlet against the benchmark field (research/field/FIELD.tsv: name<TAB>classdir<TAB>package).

  gauntlet.py cells OURS [--maps LIST|@file] [--seeds 0] [--only NAME,NAME] > cells.tsv
      every field bot vs OURS on every map, both sides
  gauntlet.py table RUN/results.jsonl [--ours HASH]
      per-opponent wins/games and mean cell score (tools/score.py) for our bot, worst first, plus totals
  gauntlet.py summary RUN/results.jsonl
      one line per bot of ours (a bots/ spec) in a mixed run: wins/games and mean points, best first
  gauntlet.py paired BASE/results.jsonl CAND/results.jsonl [--base-also R ...] [--cand-also R ...]
      the local screen of TRAINING_ALGORITHM.md 2.6: per shared cell (opponent, map, our side, seed), d = the
      candidate's cell score (tools/score.py) minus the base's (/100); t = sum d / sqrt(sum d^2), D = cells with d != 0; win/loss net
      and a one-sided sign test on per-cell wins; per-map mean d; the verdict (reject at t <= -1.5 or sign p < 0.05)
  gauntlet.py selfplay CAND/results.jsonl [--base s2o] [--control CTRL/results.jsonl]
      the same screen for a self-play run (our candidate vs the incumbent BASE): d = the candidate's cell score minus
      the identity control's on the same cell (CTRL: an identical copy of BASE vs BASE), or minus 50 without one
Rows with no result (a crashed game) are left out; a game score.cell cannot score (a points game whose replay summary
failed) counts in wins/games but not in the points or the screens. Both are counted and reported on stderr.
"""
import argparse, collections, json, math, os, sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import score  # noqa: E402

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
FIELD = os.path.join(REPO, 'research', 'field', 'FIELD.tsv')
PANEL = 'DefaultSmall,DefaultMedium,DefaultLarge,cheesefarm,sittingducks,rift,knifefight,whereisthecheese'


def cell(r, skipped):
    """score.cell(r), or None for a row with no score (its reason counted in the Counter `skipped`)."""
    why = score.missing(r)
    if why:
        if skipped is not None:
            skipped[why] += 1
        return None
    return score.cell(r)


def report(skipped):
    if skipped:
        print('gauntlet: left out ' + ', '.join(f'{n} {why}' for why, n in sorted(skipped.items()))
              + ' (no result: not counted; summary error / no summary: not in points or screens)', file=sys.stderr)


def kills_deaths(r, side):
    """(enemy rats our side killed, our rats the enemy killed); dumpVersion 1 rows: rat deaths of any cause."""
    other = 'B' if side == 'A' else 'A'
    k = 'deathsByEnemy' if r.get('dumpVersion') else 'deathsRat'
    return (r.get(other) or {}).get(k, 0), (r.get(side) or {}).get(k, 0)


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


def table(results, ours_hash=None, path=FIELD, skipped=None):
    """Summarise from our bot's side. Our bot = the hash present in every row (or --ours). Mean points over the
    scored games (nan when none is)."""
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
    agg = collections.defaultdict(lambda: [0, 0, 0.0, 0])   # wins, games, our points, scored games
    for r in rows:
        if r['a_hash'] == ours_hash:
            side, opp = 'A', r.get('b_spec') or r['b']
        elif r['b_hash'] == ours_hash:
            side, opp = 'B', r.get('a_spec') or r['a']
        else:
            continue
        c = cell(r, skipped)
        if c is None and score.missing(r) == 'no result':
            continue
        opp = names.get(opp, opp)
        a = agg[opp]
        a[1] += 1
        a[0] += 1 if r['winner'] == side else 0
        if c is not None:
            a[2] += c[0 if side == 'A' else 1]
            a[3] += 1
    return sorted(((o, w, g, p / n if n else math.nan) for o, (w, g, p, n) in agg.items()), key=lambda t: (t[1] / t[2], t[3]))


def summary(results, skipped=None):
    """Per our-bot totals for a run holding several of our bots (sides with a bots/ spec) vs field bots:
    (bot, wins, games, mean points (scored games), kill ratio = enemy rats we killed / our rats they killed (cat and
    own-goal deaths excluded; dumpVersion 1 rows: all rat deaths), mean rounds)."""
    agg = collections.defaultdict(lambda: [0, 0, 0.0, 0, 0, 0, 0])
    for l in open(results):
        if not l.strip():
            continue
        r = json.loads(l)
        c = cell(r, skipped)
        if c is None and score.missing(r) == 'no result':
            continue
        for side, spec in (('A', r.get('a_spec') or r['a']), ('B', r.get('b_spec') or r['b'])):
            if not spec.startswith('bots/'):
                continue
            a = agg[spec[5:].rstrip('/')]
            a[1] += 1
            a[0] += 1 if r['winner'] == side else 0
            if c is not None:
                a[2] += c[0 if side == 'A' else 1]
                a[6] += 1
            k, d = kills_deaths(r, side)
            a[3] += k
            a[4] += d
            a[5] += r.get('rounds', 0)
    return sorted(((b, w, g, p / s if s else math.nan, k / max(1, d), n / g) for b, (w, g, p, k, d, n, s) in agg.items()),
                  key=lambda t: (-t[1] / t[2], -t[3]))


def cell_scores(paths, skipped=None):
    """{(opponent spec, map, our side, seed): (mean cell score / 100, mean win)} for the one bots/ bot of the runs
    (unscored games left out)."""
    acc = collections.defaultdict(lambda: [0.0, 0.0, 0])
    for path in paths:
        for l in open(path):
            if not l.strip():
                continue
            r = json.loads(l)
            a, b = r.get('a_spec') or r['a'], r.get('b_spec') or r['b']
            if a.startswith('bots/') == b.startswith('bots/'):
                continue
            sc = cell(r, skipped)
            if sc is None:
                continue
            side, opp = ('A', b) if a.startswith('bots/') else ('B', a)
            c = acc[(opp, r['map'], side, r.get('seed', 0))]
            c[0] += sc[0 if side == 'A' else 1] / 100.0
            c[1] += 1.0 if r['winner'] == side else 0.0
            c[2] += 1
    return {k: (p / n, w / n) for k, (p, w, n) in acc.items()}


def selfplay_scores(paths, base, skipped=None):
    """cell_scores for a self-play run: {(base spec, map, our side, seed): (score, win)} of the bot that is not BASE."""
    acc = collections.defaultdict(lambda: [0.0, 0.0, 0])
    for path in paths:
        for l in open(path):
            if not l.strip():
                continue
            r = json.loads(l)
            if (r['a'] == base) == (r['b'] == base):
                continue
            sc = cell(r, skipped)
            if sc is None:
                continue
            side, opp = ('A', r.get('b_spec') or r['b']) if r['b'] == base else ('B', r.get('a_spec') or r['a'])
            c = acc[(opp, r['map'], side, r.get('seed', 0))]
            c[0] += sc[0 if side == 'A' else 1] / 100.0
            c[1] += 1.0 if r['winner'] == side else 0.0
            c[2] += 1
    return {k: (p / n, w / n) for k, (p, w, n) in acc.items()}


def sign_p(worse, better):
    """One-sided sign test: P(at most `better` of worse + better fair coin flips favour the candidate)."""
    n = worse + better
    return sum(math.comb(n, i) for i in range(better + 1)) / 2 ** n if n else 1.0


def paired(base, cand):
    """Paired screen of cand vs base on their shared cells (cell_scores dicts): a dict of the numbers and a verdict."""
    keys = sorted(set(base) & set(cand), key=str)
    ds = [cand[k][0] - base[k][0] for k in keys]
    wd = [cand[k][1] - base[k][1] for k in keys]
    s, s2 = sum(ds), sum(d * d for d in ds)
    t = s / math.sqrt(s2) if s2 > 0 else 0.0
    better, worse = sum(1 for w in wd if w > 0), sum(1 for w in wd if w < 0)
    p = sign_p(worse, better)
    maps = collections.defaultdict(list)
    for k, d in zip(keys, ds):
        maps[k[1]].append(d)
    reject = t <= -1.5 or p < 0.05
    return {'cells': len(keys), 'D': sum(1 for d in ds if d != 0), 't': t, 'mean_d': s / len(keys) if keys else 0.0,
            'win_net': sum(wd), 'better': better, 'worse': worse, 'sign_p': p,
            'maps': {m: sum(v) / len(v) for m, v in maps.items()},
            'verdict': 'reject' if reject else 'pass (screen only rejects)'}


def main():
    ap = argparse.ArgumentParser()
    sub = ap.add_subparsers(dest='cmd', required=True)
    c = sub.add_parser('cells'); c.add_argument('ours'); c.add_argument('--maps', default=PANEL)
    c.add_argument('--seeds', default='0'); c.add_argument('--only', default=None)
    t = sub.add_parser('table'); t.add_argument('results'); t.add_argument('--ours', default=None)
    u = sub.add_parser('summary'); u.add_argument('results')
    q = sub.add_parser('paired'); q.add_argument('base'); q.add_argument('cand')
    q.add_argument('--base-also', nargs='*', default=[]); q.add_argument('--cand-also', nargs='*', default=[])
    v = sub.add_parser('selfplay'); v.add_argument('cand'); v.add_argument('--base', default='s2o')
    v.add_argument('--control', default=None)
    o = ap.parse_args()
    sk = collections.Counter()
    if o.cmd in ('paired', 'selfplay'):
        if o.cmd == 'paired':
            r = paired(cell_scores([o.base] + o.base_also, sk), cell_scores([o.cand] + o.cand_also, sk))
        else:
            cand = selfplay_scores([o.cand], o.base, sk)
            ctrl = selfplay_scores([o.control], o.base, sk) if o.control else {k: (0.5, 0.5) for k in cand}
            r = paired(ctrl, cand)
        print(f"{r['cells']} shared cells, D {r['D']}: mean d {100 * r['mean_d']:+.1f} pts, t {r['t']:+.2f}; "
              f"win/loss net {r['win_net']:+g} (+{r['better']} -{r['worse']} cells, sign p {r['sign_p']:.3f})")
        for m, d in sorted(r['maps'].items(), key=lambda kv: kv[1]):
            print(f'  {m:28s} {100 * d:+6.1f}')
        print('verdict:', r['verdict'])
        report(sk)
        return
    if o.cmd == 'summary':
        for b, w, g, p, kd, n in summary(o.results, sk):
            print(f'{b:20s} {w:3d}/{g:<3d} pts {p:5.1f}  kills/deaths {kd:4.2f}  rounds {n:5.0f}')
        report(sk)
        return
    if o.cmd == 'cells':
        maps = [l.strip() for l in open(o.maps[1:])] if o.maps.startswith('@') else o.maps.split(',')
        only = set(o.only.split(',')) if o.only else None
        for r in cells(o.ours, [m for m in maps if m], o.seeds.split(','), only):
            print('\t'.join(map(str, r)))
    else:
        tw = tg = 0
        for opp, w, g, p in table(o.results, o.ours, skipped=sk):
            print(f'{opp:44s} {w:3d}/{g:<3d} pts {p:5.1f}')
            tw += w; tg += g
        print(f'{"TOTAL":44s} {tw:3d}/{tg:<3d}')
        report(sk)


if __name__ == '__main__':
    main()
