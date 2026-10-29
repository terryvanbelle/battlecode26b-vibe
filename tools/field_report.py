#!/usr/bin/env python3
"""Our replica (field) games, from their replays: how they end, the points at the end, kings over time, cat damage
and bytecode overruns, per opponent and per our build.

  field_report.py [--dir DIR] [--ids SPEC] [--last N] [--by opp,build] [--json]
      aggregate over the downloaded matches (DIR/<id>/replay.bc26 + match.json; default
      ~/projects/vibe/bc26b-galaxy-replays); SPEC '420-425,430'; --last N: the N highest match ids
  field_report.py games [--dir DIR] [--ids SPEC] [--last N] [--csv]
      one record per game (JSON lines, or CSV of the scalar fields): see record()
  field_report.py graded SPEC [--dir DIR] [--write]
      per game: opponent, map, our start side, won, graded score (score.cell / 100: 1/0 for a game decided by kings,
      else our points share); --write also stores DIR/<id>/graded.json (for contest.py paired)
  field_report.py bc REPLAY.bc26|TSVDIR [--game N]
      bytecode use per team and unit type (bytecode())

Replays are dumped once through game_report.dump (cached under build/dumpcache; the first run over many matches takes
a while). Our team is games.TEAM; our build is the submission that played (games.our_build over
build/replica-matches.json from games.py replica; '?' for a match not in it). Our start side is the map position we
played from (contest.py our_view: team A starts at B's position in a reversed game). How a game ends (end):
kings (a king destroyed or starved: the win the contest counts), cats (points before the round limit: every cat
died in cooperation), limit (points at the round limit), other (tiebreaks, coin flip). Exceptions are not in
replays (a runtime exception only shows as a termination), and replica replays have no indicator strings.
"""
import argparse, collections, csv, json, os, sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import game_report as GR  # noqa: E402
import games  # noqa: E402
import score  # noqa: E402

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
REPLAYS = os.path.expanduser(os.environ.get('GALAXY26_REPLAYS', '~/projects/vibe/bc26b-galaxy-replays'))
MATCHES = os.path.join(REPO, 'build', 'replica-matches.json')
AT = (100, 250, 500, 1000, 1500, 2000)       # kings / rats checkpoints
LIMITS = {'RAT': 17500, 'KING': 20000}       # old dumps' meta has no types block
END = {'RATKING_DESTROYED': 'kings', 'KILL_ALL_RAT_KINGS': 'kings', 'RESIGNATION': 'kings'}


def ids(spec):
    """'420-425,430' -> [420, ..., 425, 430]"""
    out = []
    for part in spec.split(','):
        if part.strip():
            a, _, b = part.partition('-')
            out += range(int(a), int(b or a) + 1)
    return out


def matches(root=REPLAYS, spec=None, last=None):
    """Downloaded match ids (replay.bc26 and match.json present), ascending."""
    have = sorted(int(x) for x in os.listdir(root) if x.isdigit()
                  and all(os.path.exists(os.path.join(root, x, f)) for f in ('replay.bc26', 'match.json')))
    if spec:
        want = set(ids(spec))
        have = [i for i in have if i in want]
    return have[-last:] if last else have


def builds(path=MATCHES, team=games.TEAM):
    """{match id: our build} from games.py replica's file ({} when it is missing)."""
    if not os.path.exists(path):
        return {}
    with open(path) as f:
        r = json.load(f)
    pk = {int(k): v for k, v in r['packages'].items()}
    subs = sorted(tuple(x) for x in r.get('subs', []))
    out = {}
    for m in r['matches']:
        p = next((p for p in m.get('participants') or [] if p.get('teamname') == team), None)
        if p and m.get('created'):
            out[m['id']] = games.our_build(p, games.when_utc(m['created']), pk, subs)
    return out


def limits(meta):
    t = meta.get('types') or {}
    return {k: v['bytecodeLimit'] for k, v in t.items() if v.get('bytecodeLimit')} or LIMITS


def bytecode(d, game):
    """{team 1/2: {type: {turns, max, mean, p99, over (>= the type's limit), near90 (>= 90%, under the limit), limit,
    first [(round, id)] of the first 5 overruns}}} from the src=own turn records (type at the start of the turn)."""
    pre = os.path.join(d, f'g{game}')
    with open(pre + '.meta.json') as f:
        lim = limits(json.load(f))
    bcs = collections.defaultdict(list)
    first = collections.defaultdict(list)
    for r in GR.own(GR.rows(pre + '.turns.tsv', ('round', 'id', 'team', 'type', 'bc'))):
        t = int(r['team'])
        if t not in (1, 2):
            continue
        k, bc = (t, r['type']), int(r['bc'])
        bcs[k].append(bc)
        if bc >= lim.get(r['type'], 10 ** 9) and len(first[k]) < 5:
            first[k].append((int(r['round']), int(r['id'])))
    out = {1: {}, 2: {}}
    for (t, ty), v in sorted(bcs.items()):
        v.sort()
        L = lim.get(ty, 0)
        out[t][ty] = {'turns': len(v), 'max': v[-1], 'mean': round(sum(v) / len(v)), 'p99': v[int(0.99 * (len(v) - 1))],
                      'over': sum(1 for b in v if L and b >= L), 'near90': sum(1 for b in v if L and 0.9 * L <= b < L),
                      'limit': L, 'first': first[(t, ty)]}
    return out


def first_at(series, n):
    """First round whose value is >= n, else None."""
    return next((r for r, v in series if v >= n), None)


def record(d, g, mg, team=games.TEAM, build='?', match=None):
    """One game of ours as a flat dict, or None when neither replay team is `team`. mg: the game's match.json entry.
    Pairs are (us, them) unless the key ends in Us/Them."""
    s = GR.summary(d, g)
    if not s:
        raise GR.DumpError(f'{d}: no g{g}.summary.json (dump from an old ReplayDump)')
    us = 'A' if s['teamA'] == team else 'B' if s['teamB'] == team else None
    if us is None:
        return None
    th = 'B' if us == 'A' else 'A'
    SU, ST = s.get(us) or {}, s.get(th) or {}
    side = 'A' if (mg.get('team_a') == team) != bool(mg.get('reversed')) else 'B'
    wt, rounds = s.get('winType'), s.get('rounds')
    end = END.get(wt) or ('other' if wt != 'MORE_POINTS' else 'limit' if rounds >= (s.get('maxRounds') or 2000) else 'cats')
    pts = score.points(s)
    c = score.cell(s)
    bs, bsby = s.get('backstabRound', -1), s.get('backstabBy') or 0
    ks, rats = {'A': [], 'B': []}, {}
    for r in GR.rows(os.path.join(d, f'g{g}.rounds.tsv')):
        rn = int(r['round'])
        for t in 'AB':
            ks[t].append((rn, GR.bank(r, t)[1]))
        if rn in AT:
            rats[rn] = [int(r['babies' + us]), int(r['babies' + th])]
    kat = {rn: [k, ks[th][i][1]] for i, (rn, k) in enumerate(ks[us]) if rn in AT}
    rec = {'match': match, 'game': g, 'map': s.get('map'), 'opp': s['team' + th], 'build': build, 'us': us, 'side': side,
           'won': s.get('winner') == us, 'type': wt, 'end': end, 'rounds': rounds, 'cell': c[0 if us == 'A' else 1],
           'ptsUs': pts[0 if us == 'A' else 1], 'ptsThem': pts[1 if us == 'A' else 0],
           'bs': bs, 'bsBy': '' if bs is None or bs < 0 else 'us' if bsby == ' AB'.index(us) else 'them',
           'bsCause': s.get('backstabCause') or '',
           'catUs': s.get('catDmg' + us), 'catThem': s.get('catDmg' + th), 'catTrapUs': SU.get('catDmgTrap'),
           'xUs': s.get('cheeseXfer' + us), 'xThem': s.get('cheeseXfer' + th),
           'kUs': score.kings(s.get('kings' + us)), 'kThem': score.kings(s.get('kings' + th)),
           'bankUs': int(s.get('kings' + us) or 0) // 10, 'bankThem': int(s.get('kings' + th) or 0) // 10,
           'kMaxUs': max((k for _, k in ks[us]), default=0), 'kMaxThem': max((k for _, k in ks[th]), default=0),
           'k2Us': first_at(ks[us], 2), 'k2Them': first_at(ks[th], 2), 'kingsAt': kat, 'ratsAt': rats,
           'deathsCatUs': SU.get('deathsByCat'), 'deathsEnemyUs': SU.get('deathsByEnemy'), 'termUs': SU.get('terminated'),
           'ovrUs': SU.get('overruns'), 'ovrThem': ST.get('overruns'), 'near90Us': SU.get('near90'),
           'near90Them': ST.get('near90'), 'maxBcKingUs': SU.get('maxBcKing'), 'maxBcRatUs': SU.get('maxBcRat'),
           'meanBcRatUs': SU.get('meanBcRat'), 'meanBcRatThem': ST.get('meanBcRat'), 'firstOvrUs': None}
    if mg.get('map') not in (None, rec['map']) or (mg.get('winner_team') and (mg['winner_team'] == team) != rec['won']):
        raise GR.DumpError(f'match {match} game {g}: match.json ({mg.get("map")}, {mg.get("winner_team")}) disagrees '
                           f'with the replay ({rec["map"]}, winner {s.get("winner")})')
    if SU.get('overruns'):
        b = bytecode(d, g)[' AB'.index(us)]
        rec['firstOvrUs'] = min((f[0][0] for f in (x['first'] for x in b.values()) if f), default=None)
    return rec


def match_records(mid, root=REPLAYS, team=games.TEAM, build='?'):
    """The records of every game of ours in a downloaded match (raises GR.DumpError / OSError)."""
    with open(os.path.join(root, str(mid), 'match.json')) as f:
        mj = json.load(f)
    d = GR.dump(os.path.join(root, str(mid), 'replay.bc26'))
    gs = GR.games(d)
    if len(gs) != len(mj['games']):
        raise GR.DumpError(f'match {mid}: {len(gs)} games in the replay, {len(mj["games"])} in match.json')
    return [r for r in (record(d, g, mj['games'][g], team, build, mid) for g in gs) if r]


def collect(mids, root=REPLAYS, team=games.TEAM, quiet=False):
    """(records, {match id: error}) over the matches, in order."""
    bl = builds(team=team)
    recs, errs = [], {}
    for n, mid in enumerate(mids, 1):
        if not quiet:
            print(f'\r{n}/{len(mids)} match {mid}', end='', file=sys.stderr, flush=True)
        try:
            recs += match_records(mid, root, team, bl.get(mid, '?'))
        except (GR.DumpError, OSError, ValueError, KeyError) as e:
            errs[mid] = str(e).strip().splitlines()[0][:300]
    if not quiet and mids:
        print(file=sys.stderr)
    return recs, errs


def mean(v):
    v = [x for x in v if x is not None]
    return sum(v) / len(v) if v else None


def share(a, b):
    return None if a is None or b is None or a + b == 0 else 100.0 * a / (a + b)


def median(v):
    v = sorted(v)
    return v[len(v) // 2] if v else None


def aggregate(recs):
    """One group's numbers (means over its games; kings/rats at a checkpoint over the games still running)."""
    ends = collections.defaultdict(list)
    for r in recs:
        ends[('W' if r['won'] else 'L', r['end'])].append(r['rounds'])
    side = collections.defaultdict(lambda: [0, 0])
    for r in recs:
        side[r['side']][0] += r['won']
        side[r['side']][1] += 1
    at = {}
    for rn in AT:
        k = [r['kingsAt'][rn] for r in recs if rn in r['kingsAt']]
        rt = [r['ratsAt'][rn] for r in recs if rn in r['ratsAt']]
        if k:
            at[rn] = {'n': len(k), 'kUs': mean(x[0] for x in k), 'kThem': mean(x[1] for x in k),
                      'ratsUs': mean(x[0] for x in rt), 'ratsThem': mean(x[1] for x in rt)}
    bs = [r for r in recs if r['bsBy']]
    ov = [r for r in recs if r['ovrUs']]
    return {
        'games': len(recs), 'wins': sum(r['won'] for r in recs), 'cell': mean(r['cell'] for r in recs),
        'side': {s: tuple(v) for s, v in sorted(side.items())},
        'ends': {f'{w} {e}': {'n': len(v), 'medianRound': median(v)} for (w, e), v in sorted(ends.items())},
        'backstab': {'games': len(bs), 'byUs': sum(r['bsBy'] == 'us' for r in bs), 'meanRound': mean(r['bs'] for r in bs)},
        'ptsUs': mean(r['ptsUs'] for r in recs), 'ptsThem': mean(r['ptsThem'] for r in recs),
        'catShare': mean(share(r['catUs'], r['catThem']) for r in recs),
        'catTrapPct': mean(None if not r['catUs'] else 100.0 * (r['catTrapUs'] or 0) / r['catUs'] for r in recs),
        'xferShare': mean(share(r['xUs'], r['xThem']) for r in recs),
        'kUs': mean(r['kUs'] for r in recs), 'kThem': mean(r['kThem'] for r in recs),
        'bankUs': mean(r['bankUs'] for r in recs), 'bankThem': mean(r['bankThem'] for r in recs),
        'k2Us': sum(r['k2Us'] is not None for r in recs), 'k2Them': sum(r['k2Them'] is not None for r in recs),
        'k2ThemMedian': median([r['k2Them'] for r in recs if r['k2Them'] is not None]),
        'kMaxThem': max((r['kMaxThem'] for r in recs), default=0), 'at': at,
        'deathsCat': mean(r['deathsCatUs'] for r in recs), 'deathsEnemy': mean(r['deathsEnemyUs'] for r in recs),
        'ovrGames': len(ov), 'ovrUs': sum(r['ovrUs'] or 0 for r in recs), 'ovrThem': sum(r['ovrThem'] or 0 for r in recs),
        'near90Us': sum(r['near90Us'] or 0 for r in recs), 'firstOvr': min((r['firstOvrUs'] for r in ov if r['firstOvrUs']), default=None),
        'maxBcKing': max((r['maxBcKingUs'] or 0 for r in recs), default=0),
        'maxBcRat': max((r['maxBcRatUs'] or 0 for r in recs), default=0),
        'meanBcRatUs': mean(r['meanBcRatUs'] for r in recs), 'meanBcRatThem': mean(r['meanBcRatThem'] for r in recs),
        'terminated': sum(r['termUs'] or 0 for r in recs),
    }


def groups(recs, by):
    g = collections.defaultdict(list)
    for r in recs:
        g[r[by]].append(r)
    return dict(sorted(g.items(), key=lambda kv: (-len(kv[1]), str(kv[0]))))


def f0(v, spec='.0f'):
    return '-' if v is None else format(v, spec)


def text(name, a):
    """The lines of one group."""
    ends = '  '.join(f"{k} {v['n']} (r{v['medianRound']})" for k, v in a['ends'].items())
    sides = ' '.join(f'{s} {w}-{n - w}' for s, (w, n) in a['side'].items())
    b = a['backstab']
    at = '  '.join(f"r{rn} {f0(x['kUs'], '.1f')}-{f0(x['kThem'], '.1f')}/{f0(x['ratsUs'])}-{f0(x['ratsThem'])} ({x['n']})"
                   for rn, x in a['at'].items())
    return [f"{str(name)[:40]:40} {a['games']:3d} games  W-L {a['wins']}-{a['games'] - a['wins']}  cell {f0(a['cell'], '.1f')}"
            f"  start side {sides}",
            f"  ends     {ends}  | backstab {b['games']} (by us {b['byUs']}, mean r{f0(b['meanRound'])})",
            f"  at end   pts {f0(a['ptsUs'])}-{f0(a['ptsThem'])}  cat share {f0(a['catShare'])}% (traps {f0(a['catTrapPct'])}% of ours)"
            f"  xfer share {f0(a['xferShare'])}%  kings {f0(a['kUs'], '.2f')}-{f0(a['kThem'], '.2f')}"
            f"  bank {f0(a['bankUs'])}-{f0(a['bankThem'])}  our deaths by cat {f0(a['deathsCat'], '.1f')} by enemy {f0(a['deathsEnemy'], '.1f')}",
            f"  kings/rats us-them  {at}  | 2+ kings: us {a['k2Us']} them {a['k2Them']} (median r{f0(a['k2ThemMedian'])}, max {a['kMaxThem']})",
            f"  bytecode overruns {a['ovrUs']} in {a['ovrGames']} games (first r{f0(a['firstOvr'])}), near90 {a['near90Us']},"
            f" max king {a['maxBcKing']} rat {a['maxBcRat']}, mean rat {f0(a['meanBcRatUs'])} vs {f0(a['meanBcRatThem'])};"
            f" them {a['ovrThem']} overruns; our terminations {a['terminated']}"]


def report(recs, by=('opp', 'build')):
    out = []
    for key in by:
        gs = groups(recs, key)
        out.append(f'== by {key} ({len(recs)} games, {len(gs)} groups) ==')
        for name, rs in gs.items():
            out += text(name, aggregate(rs))
    out.append('== all ==')
    out += text('all', aggregate(recs))
    return out


def graded(mid, root=REPLAYS, team=games.TEAM):
    """Per game of a downloaded match: {game, map, opp, side, won, type, rounds, cell (ours, 0-100), score (cell/100)}."""
    return [{'game': r['game'], 'map': r['map'], 'opp': r['opp'], 'side': r['side'], 'won': r['won'], 'type': r['type'],
             'rounds': r['rounds'], 'cell': r['cell'], 'score': r['cell'] / 100.0}
            for r in match_records(mid, root, team)]


def write_graded(mid, root=REPLAYS, team=games.TEAM):
    gs = graded(mid, root, team)
    p = os.path.join(root, str(mid), 'graded.json')
    with open(p + '.tmp', 'w') as f:
        json.dump({'match': mid, 'team': team, 'replay_sha1': GR.file_sha(os.path.join(root, str(mid), 'replay.bc26')),
                   'games': gs}, f, indent=1)
    os.replace(p + '.tmp', p)
    return gs


def bc_text(d, g):
    b = bytecode(d, g)
    s = GR.summary(d, g)
    meta = json.load(open(os.path.join(d, f'g{g}.meta.json')))
    ind = any(r.get('ind') for r in GR.rows(os.path.join(d, f'g{g}.turns.tsv')))
    out = [f"game {g} {meta.get('map')}  A={meta.get('teamA')}  B={meta.get('teamB')}"]
    for t in (1, 2):
        for ty, x in b[t].items():
            out.append(f"  {' AB'[t]} {ty:5} turns {x['turns']:6d}  max {x['max']:6d}  mean {x['mean']:6d}  p99 {x['p99']:6d}"
                       f"  >= limit {x['limit']}: {x['over']}  >= 90%: {x['near90']}"
                       + (f"  first overruns (round, id) {x['first']}" if x['first'] else ''))
    term = '/'.join(str((s.get(t) or {}).get('terminated', '?')) for t in 'AB')
    out.append(f"  indicator tags (OVR/EXC/NEAR): {'see game_report' if ind else 'n/a (no indicator strings)'};"
               f" runtime exceptions are not in replays (terminations A/B {term})")
    return out


def main(argv=None):
    argv = sys.argv[1:] if argv is None else argv
    cmd = argv[0] if argv and argv[0] in ('games', 'graded', 'bc') else 'report'
    ap = argparse.ArgumentParser(prog='field_report.py' + ('' if cmd == 'report' else ' ' + cmd))
    if cmd == 'bc':
        ap.add_argument('src'); ap.add_argument('--game', type=int, default=None)
        o = ap.parse_args(argv[1:])
        d = GR.source(o.src)
        for g in GR.pick(d, o.game):
            print('\n'.join(bc_text(d, g)))
        return 0
    ap.add_argument('--dir', default=REPLAYS); ap.add_argument('--team', default=games.TEAM)
    if cmd == 'graded':
        ap.add_argument('spec'); ap.add_argument('--write', action='store_true')
        o = ap.parse_args(argv[1:])
        bad = 0
        for mid in matches(o.dir, o.spec):
            try:
                gs = (write_graded if o.write else graded)(mid, o.dir, o.team)
            except (GR.DumpError, OSError, ValueError, KeyError) as e:
                print(f'match {mid}: {e}', file=sys.stderr); bad += 1
                continue
            for x in gs:
                print(json.dumps(dict(match=mid, **x)))
        missing = [i for i in ids(o.spec) if i not in matches(o.dir, o.spec)]
        if missing:
            print(f'not downloaded: {missing}', file=sys.stderr)
        return 1 if bad or missing else 0
    ap.add_argument('--ids', default=None); ap.add_argument('--last', type=int, default=None)
    if cmd == 'games':
        ap.add_argument('--csv', action='store_true')
    else:
        ap.add_argument('--by', default='opp,build'); ap.add_argument('--json', action='store_true')
    o = ap.parse_args(argv[1:] if cmd == 'games' else argv)
    recs, errs = collect(matches(o.dir, o.ids, o.last), o.dir, o.team)
    if cmd == 'games':
        if o.csv:
            cols = [k for k, v in recs[0].items() if not isinstance(v, dict)] if recs else []
            w = csv.DictWriter(sys.stdout, cols, extrasaction='ignore')
            w.writeheader(); w.writerows(recs)
        else:
            for r in recs:
                print(json.dumps(r))
    elif o.json:
        by = [k for k in o.by.split(',') if k]
        print(json.dumps({'all': aggregate(recs), **{k: {str(n): aggregate(rs) for n, rs in groups(recs, k).items()}
                                                      for k in by}}, default=str))
    else:
        print('\n'.join(report(recs, [k for k in o.by.split(',') if k])))
    for mid, e in errs.items():
        print(f'match {mid} skipped: {e}', file=sys.stderr)
    return 1 if errs else 0


if __name__ == '__main__':
    sys.exit(main())
