#!/usr/bin/env python3
"""Every bot attempt and how far it got: progress/attempts.csv (hand-kept) -> progress/ATTEMPTS.md (generated).

  attempts.py init                       add a row for every bots/<snapshot> not yet listed (date and base from
                                         bots/<snapshot>/SNAPSHOT, else git; description seeded from TRAINING_LOG;
                                         edit with `set`) and fill in missing dates of listed ones
  attempts.py set CODENAME FIELD=VALUE [FIELD=VALUE ...]
  attempts.py render                     write progress/ATTEMPTS.md

attempts.csv columns: codename,date (PDT),base,description,outcome. The rendered table adds, per attempt, its
gauntlet metrics from research/progress.tsv and its Bradley-Terry rating from progress/games.csv (tools/elo.py).
"""
import argparse, csv, os, re, subprocess, sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import milestones as MS
REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CSV = os.path.join(REPO, 'progress', 'attempts.csv')
COLS = ['codename', 'date', 'base', 'description', 'outcome']
METRICS = [('pts.top6.3m', 'top6 3 maps'), ('pts.top6.new3m', 'top6 new maps'), ('kd.top6.3m', 'k/d'),
           ('pts.top6', 'top6 (older panels)'), ('win.example', 'vs example')]
STOCK = ('examplefuncsplayer', 'lectureplayer', 'ratbot')


def read(path=CSV):
    if not os.path.exists(path):
        return []
    with open(path, newline='') as f:
        return list(csv.DictReader(f))


def write(rows, path=CSV):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', newline='') as f:
        w = csv.DictWriter(f, COLS)
        w.writeheader()
        w.writerows(rows)


def snapshots(repo=REPO):
    bots = os.path.join(repo, 'bots')
    return sorted(d for d in os.listdir(bots) if os.path.isdir(os.path.join(bots, d)) and d not in STOCK)


def git_date(repo, path):
    out = subprocess.run(['git', '-C', repo, 'log', '--diff-filter=A', '--format=%at', '--', path],
                         capture_output=True, text=True).stdout.split()
    if not out:
        return ''
    env = dict(os.environ, TZ='America/Los_Angeles')
    return subprocess.run(['date', '-d', '@' + out[-1], '+%Y-%m-%d %H:%M'], capture_output=True, text=True,
                          env=env).stdout.strip()


def snapshot_info(repo, name):
    """(date 'YYYY-MM-DD HH:MM' PDT or '', base snapshot or '') from bots/NAME/SNAPSHOT (written by snapshot.sh)."""
    f = os.path.join(repo, 'bots', name, 'SNAPSHOT')
    kv = {}
    if os.path.exists(f):
        for line in open(f):
            if '=' in line:
                k, v = line.rstrip('\n').split('=', 1)
                kv[k] = v
    date = re.sub(r'\s+[A-Z]{3}$', '', kv.get('created', '').strip())
    src = os.path.basename(kv.get('source', '').rstrip('/'))
    return date, ('' if src in ('', 'ratbot') else src)


def seed_description(log_lines, name):
    pat = re.compile(r'(?<![\w/])' + re.escape(name) + r'(?![\w])')
    for line in log_lines:
        if pat.search(line):
            s = re.sub(r'^\s*[-*]\s*', '', line.strip())
            return s if len(s) <= 220 else s[:217] + '...'
    return ''


def init(repo=REPO, path=CSV):
    rows = read(path)
    have = {r['codename'] for r in rows}
    log = os.path.join(repo, 'TRAINING_LOG.md')
    lines = open(log).read().splitlines() if os.path.exists(log) else []
    new = []
    for r in rows:
        if not r['date']:
            r['date'] = snapshot_info(repo, r['codename'])[0] or git_date(repo, os.path.join('bots', r['codename']))
    for s in snapshots(repo):
        if s not in have:
            date, base = snapshot_info(repo, s)
            new.append({'codename': s, 'date': date or git_date(repo, os.path.join('bots', s)), 'base': base,
                        'description': seed_description(lines, s), 'outcome': ''})
    rows += new
    rows.sort(key=lambda r: (r['date'] or '9999', r['codename']))
    write(rows, path)
    return len(new)


def set_fields(name, pairs, path=CSV):
    rows = read(path)
    for r in rows:
        if r['codename'] == name:
            for p in pairs:
                k, v = p.split('=', 1)
                if k not in COLS:
                    raise SystemExit(f'unknown field {k} (one of {COLS})')
                if k == 'date' and v and MS.try_parse(v) is None:
                    raise SystemExit(f'date must be YYYY-MM-DD HH:MM (PDT), not {v!r}')
                r[k] = v
            write(rows, path)
            return
    raise SystemExit(f'no attempt {name}')


def metrics(tsv=os.path.join(REPO, 'research', 'progress.tsv')):
    out = {}
    if os.path.exists(tsv):
        for line in open(tsv):
            if line.startswith('#') or line.startswith('version\t') or not line.strip():
                continue
            v, _d, m, x = line.rstrip('\n').split('\t')[:4]
            out.setdefault(v, {})[m] = float(x)
    return out


def ratings(games_csv=os.path.join(REPO, 'progress', 'games.csv')):
    if not os.path.exists(games_csv):
        return {}
    import elo, games
    rt, _f, fs = elo.compute(games.read(games_csv))
    return {p[3:]: (rt[p], fs[p]) for p in rt if p.startswith('us:')}


def cell(x):
    return str(x).replace('|', '\\|').replace('\n', ' ')


def render(rows, ms, rt, marks=()):
    L = ['# Attempts', '',
         'Every bot attempt (a snapshot under bots/), oldest first, with its score at each stage of the acceptance '
         'funnel. Generated by `tools/attempts.py render` from `progress/attempts.csv`; update a row with '
         '`tools/attempts.py set`.', '',
         '**The funnel**:',
         '1. **Unit tests and diagnostics**: `tools/unit-tests.sh` passes; single diagnostic games (driver) show no '
         'overruns, exceptions or stuck rats.',
         '2. **Gauntlet** (VM, `tools/arena.py matrix`): mean points share against the six strongest field bots on '
         'DefaultSmall, DefaultMedium and cheesefarm, both sides (36 games, SE about 2 points; *top6 3 maps*), on '
         'Excavation, Stash and UncomfortablyClose (*top6 new maps*), and enemy deaths per our death (*k/d*). Older '
         'attempts were scored on 4-8 map panels (*top6 (older panels)*) or against examplefuncsplayer (*vs example*, '
         'win rate).',
         '3. **Rating**: Bradley-Terry rating over every game in progress/games.csv (`progress/ELO.md`) and its field '
         'score (expected score against each field player).',
         '4. **Replica**: the best build is submitted to the galaxy replica and scrimmages the ladder '
         '(`progress/ladder.md`, `progress/rating-history.md`).', '',
         '**Milestone** rows (`progress/milestones.csv`) sit where they fall in time, between the attempts made before '
         'and after them.', '',
         '| attempt | date (PDT) | base | description | ' + ' | '.join(h for _, h in METRICS) +
         ' | rating | field score | outcome |',
         '|---|---|---|---|' + '---:|' * len(METRICS) + '---:|---:|---|']
    ncol = 4 + len(METRICS) + 3
    pending = list(marks)

    def milestone_rows(before):
        while pending and (before is None or pending[0][0] <= before):
            when, label = pending.pop(0)
            tag = MS.tag(len(marks) - len(pending) - 1)
            L.append(f'| **milestone {tag}** | {when:%Y-%m-%d %H:%M} | | **{cell(label)}** |' + ' |' * (ncol - 4))
    # date order (undated last, file order kept among equals) so each milestone row falls between the right attempts
    dated = [(MS.try_parse(r.get('date')), i, r) for i, r in enumerate(rows)]
    dated.sort(key=lambda t: (t[0] is None, t[0].timestamp() if t[0] else 0, t[1]))
    for d, _i, r in dated:
        if d is not None:
            milestone_rows(d)
        m = ms.get(r['codename'], {})
        vals = []
        for k, _h in METRICS:
            x = m.get(k)
            vals.append('' if x is None else (f'{x:.2f}' if k.startswith('kd') else f'{x:.3g}' if k.startswith('win')
                                               else f'{x:.1f}'))
        rr = rt.get(r['codename'])
        rating = f'{rr[0].rating:.0f} +- {1.96 * rr[0].se:.0f}' if rr else ''
        fs = f'{100 * rr[1]:.1f}%' if rr else ''
        L.append('| ' + ' | '.join([r['codename'], r['date'], r['base'], cell(r['description'])] + vals +
                                   [rating, fs, cell(r['outcome'])]) + ' |')
    milestone_rows(None)
    return '\n'.join(L) + '\n'


def main(argv=None):
    ap = argparse.ArgumentParser()
    sub = ap.add_subparsers(dest='cmd', required=True)
    sub.add_parser('init')
    p = sub.add_parser('set'); p.add_argument('codename'); p.add_argument('pairs', nargs='+')
    p = sub.add_parser('render'); p.add_argument('--out', default=os.path.join(REPO, 'progress', 'ATTEMPTS.md'))
    a = ap.parse_args(argv)
    if a.cmd == 'init':
        print(f'{init()} attempts added -> {CSV}')
    elif a.cmd == 'set':
        set_fields(a.codename, a.pairs)
    else:
        rows = read()
        open(a.out, 'w').write(render(rows, metrics(), ratings(), MS.read()))
        print(f'{len(rows)} attempts -> {a.out}')


if __name__ == '__main__':
    main()
