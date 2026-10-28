#!/usr/bin/env python3
"""Every game we know of, in one table: progress/games.csv.

  games.py build [--runs build/vmruns/runs] [--replica build/replica-matches.json] [--out progress/games.csv]
  games.py replica [--out build/replica-matches.json]     fetch finished replica matches (public API)

Sources:
  local  -- runs/<run>/results.jsonl of tools/arena.py matrix (pulled from the VM by tools/progress-refresh.sh),
            one row per game;
  replica -- finished matches of our galaxy replica (ours and field-vs-field), one row per match: map is the
            match's maps joined by '+', scoreA/scoreB are its game counts (galaxy does not publish per-game winners).

Columns: source,run,seq,teamA,teamB,map,scoreA,scoreB,winner,rounds,reason,seed,ptsA,ptsB. ptsA/ptsB: a local game's
cell score (tools/score.py cell(): 100/0 to the winner of a game decided by kings, else the spec's points share), a
replica match's share of its games won (%).
Players: our builds are us:<package> (a replica match's package is the submission it played), the stock
examplefuncsplayer/lectureplayer keep their names, field bots use their research/field/FIELD.tsv name.
"""
import argparse, csv, glob, json, os, sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import score  # noqa: E402

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
COLS = ['source', 'run', 'seq', 'teamA', 'teamB', 'map', 'scoreA', 'scoreB', 'winner', 'rounds', 'reason', 'seed',
        'ptsA', 'ptsB']
STOCK = ('examplefuncsplayer', 'lectureplayer')
TEAM = 'vibe26b'


def field_names(path=os.path.join(REPO, 'research', 'field', 'FIELD.tsv')):
    """class dir -> field name"""
    out = {}
    if os.path.exists(path):
        for line in open(path):
            p = line.rstrip('\n').split('\t')
            if len(p) >= 2 and not line.startswith('#'):
                out[p[1].rstrip('/')] = p[0]
    return out


def player(spec, field):
    """bots/X -> us:X (stock players keep their name); cls:DIR:PKG -> the field name of DIR."""
    if spec.startswith('cls:'):
        d = spec[4:].rsplit(':', 1)[0].rstrip('/')
        return field.get(d, os.path.basename(d))
    name = os.path.basename(os.path.normpath(spec))
    return name if name in STOCK else 'us:' + name


def local_rows(runs_dir, field):
    rows = []
    for f in sorted(glob.glob(os.path.join(runs_dir, '*', 'results.jsonl'))):
        run = os.path.basename(os.path.dirname(f))
        for seq, line in enumerate(open(f), 1):
            try:
                j = json.loads(line)
            except ValueError:
                continue
            w = j.get('winner')
            if w not in ('A', 'B') or 'a_spec' not in j:
                continue
            sc = score.cell(j)
            rows.append({'source': 'local', 'run': run, 'seq': seq,
                         'teamA': player(j['a_spec'], field), 'teamB': player(j['b_spec'], field),
                         'map': j.get('map', ''), 'scoreA': int(w == 'A'), 'scoreB': int(w == 'B'), 'winner': w,
                         'rounds': j.get('rounds', ''), 'reason': j.get('winType', ''), 'seed': j.get('seed', ''),
                         'ptsA': round(sc[0], 1), 'ptsB': round(sc[1], 1)})
    return rows


def replica_rows(matches, packages, subs=()):
    """matches: galaxy match dicts; packages: our submission id -> package; subs: our (created, package) list. The
    match API hides participants' submission ids, so a match without one played our latest accepted submission
    created before the match."""
    subs = sorted(subs)

    def ours(p, created):
        if p.get('submission') in packages:
            return packages[p['submission']]
        prior = [pkg for when, pkg in subs if when <= created]
        return prior[-1] if prior else '?'
    rows = []
    for m in sorted(matches, key=lambda m: m['id']):
        ps = sorted(m.get('participants') or [], key=lambda p: p['player_index'])
        if m.get('status') != 'OK!' or len(ps) != 2 or any(p.get('score') is None for p in ps):
            continue
        names = [('us:' + ours(p, when_utc(m['created']) if m.get('created') else '')) if p['teamname'] == TEAM else p['teamname'] for p in ps]
        a, b = ps[0]['score'], ps[1]['score']
        rows.append({'source': 'replica', 'run': 'ranked' if m.get('is_ranked') else 'unranked', 'seq': m['id'],
                     'teamA': names[0], 'teamB': names[1], 'map': '+'.join(m.get('maps') or []),
                     'scoreA': a, 'scoreB': b, 'winner': 'A' if a > b else 'B' if b > a else 'T',
                     'rounds': '', 'reason': '', 'seed': '',
                     'ptsA': round(100 * a / (a + b), 1) if a + b else 50, 'ptsB': round(100 * b / (a + b), 1) if a + b else 50})
    return rows


def when_utc(s):
    """ISO timestamp with offset -> comparable UTC ISO string."""
    import datetime as dt
    return dt.datetime.fromisoformat(s).astimezone(dt.timezone.utc).isoformat()


def read(path=os.path.join(REPO, 'progress', 'games.csv')):
    with open(path, newline='') as f:
        return list(csv.DictReader(f))


def fetch_replica(out):
    sys.path.insert(0, os.path.join(REPO, 'tools', 'galaxy26'))
    import client as cl
    c = cl.Client()
    acc = cl.load_accounts()
    s = cl.session(c, acc, TEAM)
    ep = c.episode
    raw = [x for x in s.pages(f'/api/compete/{ep}/submission/') if x.get('accepted')]
    subs = {x['id']: x['package'] or x['description'] for x in raw}
    ms = [m for m in c.pages(f'/api/compete/{ep}/match/') if m.get('status') == 'OK!']
    with open(out, 'w') as f:
        json.dump({'packages': {str(k): v for k, v in subs.items()},
                   'subs': [[when_utc(x['created']), subs[x['id']]] for x in raw], 'matches': ms}, f)
    print(f'{len(ms)} finished replica matches, {len(subs)} submissions -> {out}')


def main(argv=None):
    ap = argparse.ArgumentParser()
    sub = ap.add_subparsers(dest='cmd', required=True)
    p = sub.add_parser('build')
    p.add_argument('--runs', default=os.path.join(REPO, 'build', 'vmruns', 'runs'))
    p.add_argument('--replica', default=os.path.join(REPO, 'build', 'replica-matches.json'))
    p.add_argument('--field', default=os.path.join(REPO, 'research', 'field', 'FIELD.tsv'))
    p.add_argument('--out', default=os.path.join(REPO, 'progress', 'games.csv'))
    p = sub.add_parser('replica')
    p.add_argument('--out', default=os.path.join(REPO, 'build', 'replica-matches.json'))
    a = ap.parse_args(argv)
    if a.cmd == 'replica':
        os.makedirs(os.path.dirname(a.out), exist_ok=True)
        return fetch_replica(a.out)
    field = field_names(a.field)
    rows = local_rows(a.runs, field)
    nl = len(rows)
    if os.path.exists(a.replica):
        r = json.load(open(a.replica))
        rows += replica_rows(r['matches'], {int(k): v for k, v in r['packages'].items()},
                             [tuple(x) for x in r.get('subs', [])])
    os.makedirs(os.path.dirname(a.out), exist_ok=True)
    with open(a.out, 'w', newline='') as f:
        w = csv.DictWriter(f, COLS)
        w.writeheader()
        w.writerows(rows)
    print(f'{len(rows)} rows ({nl} local games, {len(rows) - nl} replica matches) -> {a.out}')


if __name__ == '__main__':
    main()
