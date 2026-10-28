#!/usr/bin/env python3
"""Tests for the python tools (synthetic inputs). Exit 1 on failure."""
import importlib.util, json, math, os, subprocess, sys, tempfile
REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
fails = checks = 0

def check(ok, what):
    global fails, checks
    checks += 1
    if not ok:
        fails += 1; print('FAIL', what)

def load(name):
    spec = importlib.util.spec_from_file_location(name, os.path.join(REPO, 'tools', name + '.py'))
    m = importlib.util.module_from_spec(spec); spec.loader.exec_module(m); return m

arena = load('arena')
# contest.py (replica requests): queue file round trip, drain order, per-build hold
spec_ct = importlib.util.spec_from_file_location('contest', os.path.join(REPO, 'tools', 'galaxy26', 'contest.py'))
ct = importlib.util.module_from_spec(spec_ct); spec_ct.loader.exec_module(ct)
with tempfile.TemporaryDirectory() as d:
    qf = os.path.join(d, 'q.tsv')
    q0 = [('s2o', 'a@+', 'p1.json', 3), ('s2o', 'a@-', 'p1.json', 3), ('s3e', 'b@+', 'p2.json', 3),
          ('s2o', 'c@+', 'p2.json', 3), ('s2o', 'c@-', 'p2.json', 3)]
    ct.write_queue(qf, q0)
    check(ct.read_queue(qf) == q0 and ct.read_queue(qf + 'x') == [], "contest queue round trip")
    calls = []
    def fake(specs, pf, n):
        calls.append((tuple(specs), pf))
        return specs[1:] if pf == 'p2.json' else []   # the limit refuses the second p2 request and the rest
    _, rest, nsent = ct.drain_rows(q0, 's2o', fake)
    check(calls == [(('a@+', 'a@-'), 'p1.json'), (('c@+', 'c@-'), 'p2.json')] and nsent == 3
          and rest == [('s3e', 'b@+', 'p2.json', 3), ('s2o', 'c@-', 'p2.json', 3)],
          f"contest drain: own build's rows by panel file until refused, others held {calls} {rest} {nsent}")
    calls.clear()
    _, rest, nsent = ct.drain_rows(q0, 's3e', lambda specs, pf, n: [])
    check(nsent == 1 and len(rest) == 4 and all(r[0] == 's2o' for r in rest), "contest drain: only the active build's rows")
    # send(): fixed sides are unranked, panel maps recorded, the tail from the first 429 is returned
    lad = [{'id': 1, 'name': 'us', 'rating': 1400}, {'id': 2, 'name': 'hi', 'rating': 1450}, {'id': 3, 'name': 'lo', 'rating': 1300}]
    sent = []
    def req(s, tid, ranked, mp, order, ep):
        if len(sent) == 2:
            raise ct.cl.ApiError(429, 'too many')
        sent.append((tid, ranked, tuple(mp), order))
        return {'id': 100 + len(sent)}
    saved = (ct.cl.rankings, ct.cl.public_maps, ct.cl.request)
    ct.cl.rankings, ct.cl.public_maps, ct.cl.request = (lambda c: lad), (lambda c: ['m1', 'm2', 'm3', 'm4']), req
    try:
        import contextlib, io
        pf = os.path.join(d, 'panel.json')
        with contextlib.redirect_stdout(io.StringIO()):
            left = ct.send(None, None, 'bc26', 1, ['hi@+', 'hi@-', 'lo@+', 'lo'], pf, 3)
        pan = json.load(open(pf))
        check(left == ['lo@+', 'lo'] and [x[1] for x in sent] == [False, False] and [x[3] for x in sent] == ['+', '-']
              and sent[0][2] == sent[1][2] == tuple(pan['hi']) and len(pan['hi']) == 3,
              f"contest send: unranked fixed sides on the panel's maps, tail kept after the limit {left} {sent} {pan}")
    finally:
        ct.cl.rankings, ct.cl.public_maps, ct.cl.request = saved
# points(): cooperation weights 50/30/20, backstab 30/50/20
row = {'catDmgA': 300, 'catDmgB': 100, 'kingsA': 1, 'kingsB': 1, 'cheeseXferA': 0, 'cheeseXferB': 0, 'backstabRound': -1}
check(arena.points(row) == (52, 27), f"points coop (engine truncates) {arena.points(row)}")
row['backstabRound'] = 500
check(arena.points(row) == (47, 32), f"points backstab {arena.points(row)}")
check(arena.points({}) == (0, 0), "points empty")
# score: the replay's kings aggregate is 10 * cheese + kings; a game decided by kings scores 100/0
sc = load('score')
row = {'catDmgA': 2180, 'catDmgB': 70, 'kingsA': 890, 'kingsB': 4591, 'cheeseXferA': 420, 'cheeseXferB': 895,
       'backstabRound': 27}
check(sc.points(row) == (35, 64), f"score points: king count from the last digit {sc.points(row)}")
check(sc.cell(dict(row, winner='B', winType='RATKING_DESTROYED')) == (0.0, 100.0), "score cell: king kill is a loss")
c = sc.cell(dict(row, winner='B', winType='MORE_POINTS'))
check(abs(c[0] - 3500 / 99) < 1e-9 and abs(sum(c) - 100) < 1e-9, f"score cell: points share when points decide {c}")
check(sc.cell(dict(row, winner='A', winType='COIN_FLIP', catDmgA=0, catDmgB=0, cheeseXferA=0, cheeseXferB=0,
                   kingsA=1, kingsB=1)) == (50.0, 50.0), "score cell: tied points")
check(sc.cell({'winner': 'A', 'ptsA': 30, 'ptsB': 10}) == (75.0, 25.0), "score cell: stored points without a summary")
check(sc.cell({'winner': None}) == (0.0, 0.0), "score cell: no result")
check(arena.cell_key('a', 'b', 'm', 3) == arena.cell_key('a', 'b', 'm', 3), "cell key stable")
check(arena.cell_key('a', 'b', 'm', 3) != arena.cell_key('b', 'a', 'm', 3), "cell key ordered by side")
# tree_hash ignores non-java files and changes with content
with tempfile.TemporaryDirectory() as d:
    open(os.path.join(d, 'A.java'), 'w').write('class A {}')
    h1 = arena.tree_hash(d)
    open(os.path.join(d, 'notes.txt'), 'w').write('x')
    check(arena.tree_hash(d) == h1, "tree_hash ignores non-java")
    open(os.path.join(d, 'A.java'), 'w').write('class A { }')
    check(arena.tree_hash(d) != h1, "tree_hash sees java edits")
# cells.py
out = subprocess.run([sys.executable, '-I', os.path.join(REPO, 'tools', 'cells.py'), 'x', 'y', '--maps', 'M1,M2', '--seeds', '0,4', '--both'],
                     capture_output=True, text=True).stdout.splitlines()
check(len(out) == 8 and out[0] == 'x\ty\tM1\t0' and out[1] == 'y\tx\tM1\t0' and out[-1] == 'y\tx\tM2\t4', f"cells.py {out[:2]}")
# gauntlet: cells both sides, table from our side with field names
g = load('gauntlet')
with tempfile.TemporaryDirectory() as d:
    f = os.path.join(d, 'F.tsv'); open(f, 'w').write('opp1\t/c/1\tp1\nopp2\t/c/2\tp2\n')
    cs = g.cells('bots/x', ['M'], ['0'], path=f)
    check(cs == [('bots/x', 'cls:/c/1:p1', 'M', '0'), ('cls:/c/1:p1', 'bots/x', 'M', '0'),
                 ('bots/x', 'cls:/c/2:p2', 'M', '0'), ('cls:/c/2:p2', 'bots/x', 'M', '0')], f"gauntlet cells {cs}")
    res = os.path.join(d, 'r.jsonl')
    rows = [dict(a_hash='O', b_hash='H1', a_spec='bots/x', b_spec='cls:/c/1:p1', a='x', b='p1', winner='A', ptsA=70, ptsB=30),
            dict(a_hash='H1', b_hash='O', a_spec='cls:/c/1:p1', b_spec='bots/x', a='p1', b='x', winner='A', ptsA=60, ptsB=40),
            dict(a_hash='O', b_hash='H2', a_spec='bots/x', b_spec='cls:/c/2:p2', a='x', b='p2', winner='A', ptsA=80, ptsB=20)]
    open(res, 'w').write('\n'.join(json.dumps(r) for r in rows) + '\n')
    t = g.table(res, path=f)
    check(t == [('opp1', 1, 2, 55.0), ('opp2', 1, 1, 80.0)], f"gauntlet table {t}")
    open(res, 'a').write(json.dumps(dict(a_hash='Y', b_hash='H1', a_spec='bots/y', b_spec='cls:/c/1:p1', a='y', b='p1',
                                         winner='B', ptsA=10, ptsB=90, rounds=300,
                                         A={'deathsRat': 8}, B={'deathsRat': 4})) + '\n')
    sm = g.summary(res)
    check(sm == [('x', 2, 3, 190 / 3, 0.0, 0.0), ('y', 0, 1, 10.0, 0.5, 300.0)], f"gauntlet summary {sm}")
# gauntlet paired screen: shared cells only, t = sum d / sqrt(sum d^2), sign test on per-cell wins
with tempfile.TemporaryDirectory() as d:
    def run(path, rows):
        open(path, 'w').write(''.join(json.dumps(dict(a_spec=a, b_spec=b, a=a, b=b, map=m, seed=0, winner=w, ptsA=pa, ptsB=pb)) + '\n'
                                      for a, b, m, w, pa, pb in rows))
        return path
    b = run(os.path.join(d, 'b.jsonl'), [('bots/x', 'cls:o:p', 'M1', 'B', 20, 80), ('cls:o:p', 'bots/x', 'M1', 'A', 70, 30),
                                         ('bots/x', 'cls:o:p', 'M2', 'B', 40, 60), ('bots/x', 'cls:q:p', 'M2', 'A', 60, 40)])
    c = run(os.path.join(d, 'c.jsonl'), [('bots/y', 'cls:o:p', 'M1', 'B', 30, 70), ('cls:o:p', 'bots/y', 'M1', 'A', 70, 30),
                                         ('bots/y', 'cls:o:p', 'M2', 'A', 60, 40), ('bots/y', 'cls:o:p', 'M3', 'A', 90, 10)])
    cs = g.cell_scores([b])
    check(cs[('cls:o:p', 'M1', 'B', 0)] == (0.3, 0.0) and len(cs) == 4, f"gauntlet cell scores {cs}")
    pr = g.paired(g.cell_scores([b]), g.cell_scores([c]))
    check(pr['cells'] == 3 and pr['D'] == 2 and abs(pr['t'] - 0.3 / math.sqrt(0.05)) < 1e-9 and pr['win_net'] == 1
          and pr['better'] == 1 and pr['worse'] == 0 and pr['verdict'].startswith('pass'), f"gauntlet paired {pr}")
    check(abs(g.sign_p(5, 0) - 1 / 32) < 1e-12 and g.sign_p(0, 0) == 1.0 and g.sign_p(1, 1) == 0.75, "gauntlet sign test")
    pr2 = g.paired(g.cell_scores([c]), g.cell_scores([b]))
    check(pr2['t'] < 0 and pr2['win_net'] == -1, f"gauntlet paired reversed {pr2}")
    # self-play: the candidate is whichever side is not the base; rows without the base (or base vs base) are skipped
    sp = run(os.path.join(d, 'sp.jsonl'), [('bots/y', 'bots/s', 'M1', 'A', 70, 30), ('bots/s', 'bots/y', 'M1', 'A', 60, 40),
                                           ('bots/s', 'bots/s', 'M2', 'A', 50, 50), ('bots/y', 'cls:o:p', 'M2', 'A', 50, 50)])
    ss = {k: v for k, v in g.selfplay_scores([sp], 'bots/s').items()}
    check(ss == {('bots/s', 'M1', 'A', 0): (0.7, 1.0), ('bots/s', 'M1', 'B', 0): (0.4, 0.0)}, f"gauntlet selfplay scores {ss}")
    pr3 = g.paired({k: (0.5, 0.5) for k in ss}, ss)
    check(pr3['cells'] == 2 and abs(pr3['mean_d'] - 0.05) < 1e-9 and pr3['better'] == 1 and pr3['worse'] == 1,
          f"gauntlet selfplay vs 50 {pr3}")
# plot_progress: series per metric, one progress.png
pp = load('plot_progress')
with tempfile.TemporaryDirectory() as d:
    t = os.path.join(d, 'p.tsv')
    open(t, 'w').write('version\tdate\tmetric\tvalue\nv1\tx\twin.a\t0.5\nv2\tx\twin.a\t0.7\nv2\tx\twin.b\t0.2\nv2\tx\tpts.f\t40\n')
    vs, ss = pp.load(t)
    check(vs == ['v1', 'v2'] and ss['win.a'] == {'v1': 0.5, 'v2': 0.7}, f"plot load {vs} {dict(ss)}")
    check(list(pp.groups(ss)) == ['win', 'pts'] and list(pp.groups(ss)['win']) == ['win.a', 'win.b'], "plot groups")
    try:
        import matplotlib  # noqa: F401  (system python has none; unit-tests.sh runs the venv)
        r = subprocess.run([sys.executable, '-I', os.path.join(REPO, 'tools', 'plot_progress.py'), '--tsv', t, '--out', d,
                            '--ratings', os.path.join(d, 'none.csv')], capture_output=True, text=True)
        check(os.path.getsize(os.path.join(d, 'metrics.png')) > 1000 and not os.path.exists(os.path.join(d, 'progress.png'))
              and 'progress.png skipped' in r.stdout, f"metrics.png written, progress.png skipped without ratings {r.stdout}")
    except ImportError:
        pass
# games.py: player names, local rows, replica rows mapped to our submission by time
gm = load('games')
with tempfile.TemporaryDirectory() as bd:
    os.mkdir(os.path.join(bd, 's0e'))
    check([gm.codename('ratbot', 's0e econ budget', bd), gm.codename('ratbot', 'ratbot', bd), gm.codename('s2o', 's2o: x', bd),
           gm.codename('ratbot', 'zz9: not a snapshot', bd)] == ['s0e', 'ratbot', 's2o', 'ratbot'],
          "games codename: a dev-dir submission takes the snapshot its description names")
fld = {'/c/1': 'opp1'}
check(gm.player('cls:/c/1/:pkg', fld) == 'opp1' and gm.player('bots/s2t/', fld) == 'us:s2t'
      and gm.player('bots/examplefuncsplayer', fld) == 'examplefuncsplayer', "games player names")
with tempfile.TemporaryDirectory() as d:
    os.makedirs(os.path.join(d, 'r1'))
    open(os.path.join(d, 'r1', 'results.jsonl'), 'w').write(
        json.dumps(dict(a_spec='bots/x', b_spec='cls:/c/1:p', map='M', winner='B', rounds=200, winType='RATKING_DESTROYED', seed=3, ptsA=30, ptsB=60)) + '\n'
        + 'not json\n' + json.dumps(dict(a_spec='bots/x', b_spec='cls:/c/1:p', winner=None)) + '\n')
    lr = gm.local_rows(d, fld)
    check(len(lr) == 1 and lr[0]['teamA'] == 'us:x' and lr[0]['teamB'] == 'opp1' and lr[0]['winner'] == 'B'
          and (lr[0]['scoreA'], lr[0]['scoreB']) == (0, 1) and (lr[0]['ptsA'], lr[0]['ptsB']) == (0, 100), f"games local rows: cell score {lr}")
P = lambda team, idx, score, sub=None: {'teamname': team, 'player_index': idx, 'score': score, 'submission': sub}
ms = [{'id': 2, 'status': 'OK!', 'is_ranked': True, 'maps': ['M1', 'M2', 'M3'], 'created': '2026-10-28T09:00:00-04:00',
       'participants': [P('opp1', 0, 1), P(gm.TEAM, 1, 2)]},
      {'id': 1, 'status': 'OK!', 'is_ranked': False, 'maps': ['M1'], 'created': '2026-10-28T03:00:00-04:00',
       'participants': [P(gm.TEAM, 0, 0, 7), P('opp2', 1, 1)]},
      {'id': 3, 'status': 'QUE', 'participants': []}]
subs = [(gm.when_utc('2026-10-28T05:00:00-07:00'), 'new'), (gm.when_utc('2026-10-27T20:00:00-07:00'), 'old')]
rr = gm.replica_rows(ms, {7: 'sub7'}, subs)
check([(r['seq'], r['teamA'], r['teamB'], r['winner'], r['map'], r['run']) for r in rr] ==
      [(1, 'us:sub7', 'opp2', 'B', 'M1', 'unranked'), (2, 'opp1', 'us:new', 'B', 'M1+M2+M3', 'ranked')], f"games replica rows {rr}")
check([(r['ptsA'], r['ptsB']) for r in rr] == [(0.0, 100.0), (33.3, 66.7)], f"games replica rows: share of games won {rr}")
check(gm.replica_rows(ms[:1], {}, [])[0]['teamB'] == 'us:?', "games replica row without a known submission")
# elolib / elo: Bradley-Terry fit orders players by strength, field score in [0, 1]
el = load('elolib')
gs = [('a', 'b', 3, 1), ('b', 'c', 3, 1), ('a', 'c', 4, 0)]
rt = el.fit(gs)
check(rt['a'].rating > rt['b'].rating > rt['c'].rating and abs(sum(x.rating for x in rt.values()) / 3 - 1500) < 1,
      f"elolib fit order/mean {rt}")
check(rt['a'].wins == 7 and rt['c'].losses == 7, f"elolib records {rt}")
check(abs(el.expected(1500, 1500) - 0.5) < 1e-9 and el.expected(1900, 1500) > 0.9, "elolib expected")
check(el.pair_table([('a', 'b', 300, 100)])[('a', 'b')] == [150.0, 50.0], "elolib pair cap")
kt = el.key_table([('u', 'f', 150, 50, 'm1'), ('f', 'u', 100, 100)])
check(kt == {('u', 'f', 'm1'): [75.0, 25.0], ('f', 'u', None): [50.0, 50.0]}, f"elolib pair cap across keys {kt}")
# keyed fit: u0 only meets f on the easy map, u1 only on the hard one, u2 on both (without a build on both maps, map
# and build are confounded and only the prior splits them); exact expected outcomes, true u1 - u0 = +100
P = lambda d: 1 / (1 + 10 ** (-d / 400))
F, H = {'f0': -100, 'f1': 0, 'f2': 100}, {'easy': 150, 'hard': -150}
kg = [(a, b, 100 * P(F[a] - F[b]), 100 * (1 - P(F[a] - F[b]))) for a in F for b in F if a < b]
for u, du, mp in (('u0', -50, 'easy'), ('u1', 50, 'hard'), ('u2', 0, 'easy'), ('u2', 0, 'hard')):
    kg += [(u, f, 100 * P(du + H[mp] - F[f]), 100 * (1 - P(du + H[mp] - F[f])), mp) for f in F]
kr, kh = el.fit_h(kg)
nr = el.fit([g[:4] for g in kg])
check(80 < kr['u1'].rating - kr['u0'].rating < 105 and nr['u1'].rating < nr['u0'].rating and 270 < kh['easy'] - kh['hard'] < 305
      and abs(kh['easy'] + kh['hard']) < 1e-6, f"elolib map handicap recovers u1 > u0 {kr} {kh}")
sys.path.insert(0, os.path.join(REPO, 'tools'))
eo = load('elo')
rows = [{'teamA': 'us:x', 'teamB': 'f1', 'scoreA': '1', 'scoreB': '0'}, {'teamA': 'f1', 'teamB': 'us:x', 'scoreA': '1', 'scoreB': '0'},
        {'teamA': 'f1', 'teamB': 'f2', 'scoreA': '2', 'scoreB': '0'}]
rt, field, fs = eo.compute(rows)
check(sorted(field) == ['f1', 'f2'] and all(0 <= v <= 1 for v in fs.values()), f"elo compute {field} {fs}")
lg = {'source': 'local', 'teamA': 'f1', 'teamB': 'us:x', 'map': 'M', 'scoreA': '1', 'scoreB': '0', 'ptsA': '75', 'ptsB': '25'}
check(eo.game(lg) == ('us:x', 'f1', 0.25, 0.75, 'M'), f"elo game: ours first, keyed by map, points share {eo.game(lg)}")
check(eo.game(dict(lg, source='replica', ptsA='', ptsB='')) == ('us:x', 'f1', 0.0, 1.0, 'replica'), "elo game: replica key, wins")
check(eo.game(dict(lg, teamB='f2')) == ('f1', 'f2', 0.75, 0.25), "elo game: field vs field unkeyed")
check(eo.game(dict(lg, ptsA='0', ptsB='0')) == ('us:x', 'f1', 0.0, 1.0, 'M'), "elo game: no points falls back to the score")
check(eo.records(rows) == {'us:x': [1, 1], 'f1': [3, 1], 'f2': [0, 2]}, f"elo records {eo.records(rows)}")
uz = [dict(lg), dict(lg, source='replica'), dict(lg, teamA='us:y'), dict(lg, teamA='examplefuncsplayer'), dict(lg, teamB='f2')]
check(eo.usable(uz)[:2] == uz[1:3] and eo.usable(uz)[2]['teamA'] == 'bc26-example' and len(eo.usable(uz)) == 3
      and len(eo.usable(uz, True)) == 5,
      f"elo usable: local games vs field bots dropped unless --local-field {[r['teamA'] + '/' + r['teamB'] + '/' + r['source'] for r in eo.usable(uz)]}")
md, _ = eo.render(*eo.compute(rows, maps=True)[:3], [], 3, {'local': 3}, eo.records(rows), {'M': 12.0})
check('| x |' in md and '| 1-1 |' in md and '| M | +12 |' in md, "elo render: game records and map handicaps")
# attempts: description seeding, init from bots/, render cells
at = load('attempts')
check(at.seed_description(['- s2t: trap tweak', 's2tt other'], 's2t') == 's2t: trap tweak'
      and at.seed_description(['bots/s2t x', 'nothing'], 's2t') == '', "attempts seed description")
with tempfile.TemporaryDirectory() as d:
    for b in ('s1a', 'ratbot', 's1b'):
        os.makedirs(os.path.join(d, 'bots', b))
    open(os.path.join(d, 'TRAINING_LOG.md'), 'w').write('- s1b: bigger swarm\n')
    csvp = os.path.join(d, 'a.csv')
    check(at.init(d, csvp) == 2 and at.init(d, csvp) == 0, "attempts init adds snapshots once")
    at.set_fields('s1b', ['outcome=adopted'], csvp)
    rows = at.read(csvp)
    check([r['codename'] for r in rows] == ['s1a', 's1b'] and rows[1]['description'] == 's1b: bigger swarm'
          and rows[1]['outcome'] == 'adopted', f"attempts rows {rows}")
    md = at.render(rows, {'s1b': {'pts.top6.3m': 23.71, 'kd.top6.3m': 1.234}}, {})
    check('| s1b |' in md and '| 23.7 |' in md and '| 1.23 |' in md and 'adopted |' in md, "attempts render")
mst = load('milestones')
with tempfile.TemporaryDirectory() as d:
    os.makedirs(os.path.join(d, 'bots', 's9a'))
    open(os.path.join(d, 'bots', 's9a', 'SNAPSHOT'), 'w').write('name=s9a\nsource=bots/s9\ncreated=2026-10-28 05:20 PDT\n')
    check(at.snapshot_info(d, 's9a') == ('2026-10-28 05:20', 's9') and at.snapshot_info(d, 'nope') == ('', ''),
          f"attempts snapshot info {at.snapshot_info(d, 's9a')}")
rows = [{'codename': 'a1', 'date': '2026-10-28 06:00', 'base': '', 'description': '', 'outcome': ''},
        {'codename': 'a2', 'date': '2026-10-28 07:00', 'base': '', 'description': '', 'outcome': ''}]
mk2 = [(mst.parse('2026-10-28 06:38'), 'switch'), (mst.parse('2026-10-28 08:00'), 'later')]
md = at.render(rows, {}, {}, mk2).splitlines()
body = [l for l in md if l.startswith('| ') and not l.startswith('| attempt')]
check([l.split('|')[1].strip() for l in body] == ['a1', '**milestone M1**', 'a2', '**milestone M2**']
      and all(l.count('|') == md[[i for i, x in enumerate(md) if x.startswith('| attempt')][0]].count('|') for l in body)
      and '**switch**' in body[1], f"attempts milestone rows {body}")
shuffled = [rows[1], {'codename': 'a0', 'date': 'not a date', 'base': '', 'description': '', 'outcome': ''}, rows[0]]
body = [l for l in at.render(shuffled, {}, {}, mk2[:1]).splitlines() if l.startswith('| ') and not l.startswith('| attempt')]
check([l.split('|')[1].strip() for l in body] == ['a1', '**milestone M1**', 'a2', 'a0'], f"attempts sorted by date, bad date last {body}")
# replica_report: W-L from replica rows, ladder and history markdown
rp = load('replica_report')
wl = rp.wl_from_games([{'source': 'replica', 'teamA': 'us:x', 'teamB': 'o', 'scoreA': '2', 'scoreB': '1'},
                       {'source': 'local', 'teamA': 'us:x', 'teamB': 'o', 'scoreA': '0', 'scoreB': '1'}])
check(wl == {rp.TEAM: [2, 1], 'o': [1, 2]}, f"replica W-L {wl}")
import datetime as _dt
now = _dt.datetime(2026, 10, 28, 6, 0, tzinfo=rp.PDT)
lm = rp.ladder_md([{'name': 'o', 'rating': 1400.0, 'active': True}, {'name': rp.TEAM, 'rating': None, 'active': False}], wl, now)
check('| 1 | o | 1400.0 | 1-2 | accepted |' in lm and f'| 2 | **{rp.TEAM}** | - | 2-1 | none |' in lm, "replica ladder md")
hist = [(rp.ts('2026-10-28T10:00:00+00:00'), 900.0), (rp.ts('2026-10-28T12:00:00+00:00'), 1000.0)]
hm = rp.history_md(hist, [{'id': 5, 'package': 'p', 'accepted': True, 'created': '2026-10-28T11:00:00+00:00'}], now)
check('| #5 | p | 2026-10-28 04:00 | yes | 900 | 1 |' in hm and 'latest rating 1,000' in hm, "replica history md")
hm2 = rp.history_md(hist, [], now, [(mst.parse('2026-10-28 06:38'), 'effort switch')])
check('- M1 2026-10-28 06:38 PDT: effort switch' in hm2 and 'Milestones' not in hm, "replica history md milestones")
# milestones: add/read round trip, placement between time-ordered items, drawn on progress.png and rating-history.png
mst = load('milestones')
with tempfile.TemporaryDirectory() as d:
    mp = os.path.join(d, 'm.csv')
    check(mst.read(mp) == [], "milestones missing file")
    mst.add('later', '2026-10-28 07:00', mp)
    mst.add('first', '2026-10-28 06:38', mp)
    mk = mst.read(mp)
    check([l for _, l in mk] == ['first', 'later'] and mk[0][0].utcoffset().total_seconds() == -7 * 3600,
          f"milestones read {mk}")
    ds = [mst.parse('2026-10-28 06:00'), None, mst.parse('2026-10-28 06:40'), mst.parse('2026-10-28 07:30')]
    check(mst.index_x(ds, mk[0][0]) == 0.5 and mst.index_x(ds, mk[1][0]) == 2.5
          and mst.index_x(ds, mst.parse('2026-10-27 00:00')) == -0.5 and mst.index_x(ds, mst.parse('2026-10-29 00:00')) == 3.5,
          "milestones index_x")
    bad = os.path.join(d, 'bad.csv')
    open(bad, 'w', encoding='utf-8-sig').write('date,label\n2026-10-28 06:38,ok\nnot a date,skip\n\n')
    check([l for _, l in mst.read(bad)] == ['ok'], "milestones skip bad rows and a BOM")
    check(mst.place(ds, mk) == [0.5, 2.5], f"milestones place alone {mst.place(ds, mk)}")
    two = [(mst.parse('2026-10-28 06:10'), 'a'), (mst.parse('2026-10-28 06:20'), 'b'), mk[1]]
    check([round(x, 3) for x in mst.place(ds, two)] == [0.333, 0.667, 2.5], f"milestones spread in one gap {mst.place(ds, two)}")
    check(mst.tag(0) == 'M1' and mst.footnote(mk) == 'M1 10-28 06:38 first   M2 10-28 07:00 later', f"milestones footnote {mst.footnote(mk)}")
    js = json.loads(mst.to_json([(mst.parse('2026-10-28 06:38'), 'a -> <b>c</b>')] + mk[1:]))
    check(js == [{'tag': 'M1', 'label': 'a \u2192 bc/b', 't': 1793194680000}, {'tag': 'M2', 'label': 'later', 't': 1793196000000}],
          f"milestones json for the replica charts {js}")
    t = os.path.join(d, 'p.tsv'); ac = os.path.join(d, 'a.csv')
    open(t, 'w').write('version\tdate\tmetric\tvalue\nv1\t2026-10-28 06:10\twin.a\t0.5\nv2\t2026-10-28 06:50\twin.a\t0.7\n'
                       'v3\tbad date\twin.a\t0.6\n')
    open(ac, 'w').write('codename,date,base,description,outcome\nv1,2026-10-28 05:00,,,\nv2,,,,\n')
    open(t, 'a').write('v4\t2026-10-28 04:00\twin.a\t0.1\n')
    open(ac, 'a').write('v4,2026-10-28 09:00,,,\nv3,bad,,,\n')
    vd = pp.version_dates(['v1', 'v2', 'v3', 'v4'], t, ac)
    check(vd == [mst.parse('2026-10-28 05:00'), mst.parse('2026-10-28 06:50'), None, mst.parse('2026-10-28 04:00')],
          f"plot version dates: earlier of snapshot and first measurement {vd}")
    ov, od = pp.in_time_order(['v1', 'v2', 'v3', 'v4'], vd)
    check(ov == ['v4', 'v1', 'v2', 'v3'] and od[-1] is None, f"plot axis in date order {ov}")
    rc = os.path.join(d, 'ratings.csv')
    open(rc, 'w').write('player,rating,se,games,field_score\nf1,1700,40,90,0.7\nus:v1,1500,50,40,0.45\nus:v2,1520,60,25,0.5\n'
                        'us:v4,1480,80,20,0.4\nus:v3,1490,90,19,0.4\nus:ratbot,1600,30,200,0.6\n')
    rts = pp.read_ratings(rc)
    check(rts['us:v2'] == {'rating': 1520.0, 'se': 60.0, 'games': 25.0, 'field_score': 0.5} and pp.read_ratings(rc + 'x') == {},
          f"plot read ratings {rts}")
    check(pp.our_builds(rts) == ['v1', 'v2', 'v4'], f"plot builds: ours, 20+ games, no dev dir {pp.our_builds(rts)}")
    open(os.path.join(d, 'ad.csv'), 'w').write('codename,date,base,description,outcome\na,,,,adopted: base\nb,,,,rejected (kept 12 rats)\n'
                                               'c,,,,submitted to replica\nd,,,,"inconclusive; kept as dev base"\ne,,c2,,\n')
    open(os.path.join(d, 'ad.csv'), 'a').write('f,,,,"accepted: panel t 2.3"\ng,,,,not submitted\n')
    ad = pp.accepted(os.path.join(d, 'ad.csv'))
    check(ad == {'c', 'f'}, f"plot accepted builds: submitted or accepted only {ad}")
    try:
        import matplotlib  # noqa: F401
        out = os.path.join(d, 'out')
        r = subprocess.run([sys.executable, '-I', os.path.join(REPO, 'tools', 'plot_progress.py'), '--tsv', t, '--out', out,
                            '--attempts', ac, '--milestones', mp, '--ratings', rc], capture_output=True, text=True)
        check(r.returncode == 0 and 'progress.png skipped (no accepted build' in r.stdout,
              f"progress.png: candidates never accepted are left out {r.stdout}")
        open(ac, 'a').write('v1b,,,,\n')
        acs = open(ac).read().replace('v1,2026-10-28 05:00,,,', 'v1,2026-10-28 05:00,,,submitted (#1)') \
            .replace('v4,2026-10-28 09:00,,,', 'v4,2026-10-28 09:00,,,accepted: panel')
        open(ac, 'w').write(acs)
        r = subprocess.run([sys.executable, '-I', os.path.join(REPO, 'tools', 'plot_progress.py'), '--tsv', t, '--out', out,
                            '--attempts', ac, '--milestones', mp, '--ratings', rc], capture_output=True, text=True)
        check(r.returncode == 0 and '2 milestones' in r.stdout and 'progress.png: 2 builds' in r.stdout
              and os.path.getsize(os.path.join(out, 'progress.png')) > 1000 and os.path.getsize(os.path.join(out, 'metrics.png')) > 1000,
              f"progress.png + metrics.png with milestones {r.stdout} {r.stderr[-300:]}")
        rp0 = load('replica_report')
        h = [(rp0.ts('2026-10-28T12:00:00+00:00'), 900.0), (rp0.ts('2026-10-28T13:00:00+00:00'), 950.0)]
        sb = [{'id': 5, 'package': 'p', 'accepted': True, 'created': '2026-10-28T11:30:00+00:00'}]
        tm = [{'name': 'o', 'rating': 1400.0, 'active': True}, {'name': rp0.TEAM, 'rating': 950.0, 'active': True}]
        check(rp0.charts(tm, h, sb, d, mk, mst.parse('2026-10-28 07:10')) and os.path.getsize(os.path.join(d, 'rating-history.png')) > 1000,
              "rating-history.png with milestones")
    except ImportError:
        pass
# game_report.timeline on a synthetic tsv dir
gr = load('game_report')
with tempfile.TemporaryDirectory() as d:
    open(os.path.join(d, 'g0.rounds.tsv'), 'w').write('round\tkingsA\tkingsB\n1\t25001\t25001\n2\t24981\t24991\n')
    open(os.path.join(d, 'g0.turns.tsv'), 'w').write('round\tid\tteam\ttype\tx\ty\thp\n'
        '1\t3\t1\tKING\t0\t0\t600\n1\t4\t2\tKING\t9\t9\t600\n2\t3\t1\tKING\t0\t0\t590\n2\t50\t1\tRAT\t1\t1\t100\n2\t50\t1\tRAT\t1\t1\t100\n')
    tl = gr.timeline(d, 1)
    check(tl == [(1, 0, 0, 600, 600, 2500, 2500), (2, 1, 0, 590, None, 2498, 2499)], f"timeline {tl}")
# ReplayDump on a real replay with throws: a thrown rat's landing is a 'land' row, never a self 'nap'
fx = os.path.join(REPO, 'test', 'fixtures', 'throws.bc26')
if os.path.exists(fx) and os.path.isdir(os.path.join(REPO, 'build', 'tools', 'bc26tools')):
    d = gr.dump(fx)
    acts = list(gr.rows(os.path.join(d, 'g0.actions.tsv')))
    naps = [a for a in acts if a['action'] == 'nap']
    check(naps and all(a['a'] != a['id'] for a in naps), "replaydump naps are grabs of another rat")
    check(any(a['action'] == 'land' for a in acts), "replaydump logs landings")
# battle_report: vision cone, and the sections on the fixture replay
br = load('battle_report')
# replay direction ordinals: 1 west, 6 north-east, 7 north (FlatHelpers), checked against real rat moves
check(br.sees(5, 5, 7, 5, 6) and br.sees(5, 5, 7, 6, 6) and not br.sees(5, 5, 7, 6, 5) and not br.sees(5, 5, 7, 5, 4),
      "battle_report cone (north)")
check(br.sees(5, 5, 6, 6, 5) and br.sees(5, 5, 6, 5, 6) and not br.sees(5, 5, 6, 4, 6), "battle_report cone (northeast)")
check(br.sees(5, 5, 1, 4, 5) and not br.sees(5, 5, 1, 6, 5) and br.sees(5, 5, 0, 6, 5), "battle_report cone (west; centre sees all)")
if os.path.exists(fx) and os.path.isdir(os.path.join(REPO, 'build', 'tools', 'bc26tools')):
    rep = br.report(d, 60, 140, 20)
    check([f['round'] for f in rep['front']] == [60, 80, 100, 120, 140][:len(rep['front'])] and rep['front'][0]['1']['n'] > 0,
          f"battle_report front {rep['front'][:1]}")
    check(rep['grabs']['2']['n'] > rep['grabs']['1']['n'] > 0 and sum(rep['grabs']['2']['fate'].values()) == rep['grabs']['2']['n'],
          f"battle_report grabs {rep['grabs']}")
    check(all(v > 0 for tm in '12' for v in rep['hploss'][tm].values()) and 42 in rep['throws']['2'], "battle_report hploss/throws")
# snapshot.sh refuses a source without java files and leaves nothing behind
# econ_report: piles followed from spawn to pickup, mine sides, a drop, a pile never taken
er = load('econ_report')
with tempfile.TemporaryDirectory() as d:
    json.dump({'map': 'M', 'teamA': 'a', 'teamB': 'b', 'width': 20, 'height': 10, 'symmetry': 2,
               'mines': [[3, 5]], 'bodies': [{'id': 1, 'team': 1, 'type': 'KING', 'x': 1, 'y': 5},
                                             {'id': 2, 'team': 2, 'type': 'KING', 'x': 18, 'y': 5}]},
              open(os.path.join(d, 'g0.meta.json'), 'w'))
    open(os.path.join(d, 'g0.turns.tsv'), 'w').write('round\tid\tteam\ttype\tx\ty\n')
    open(os.path.join(d, 'g0.actions.tsv'), 'w').write('round\tid\tteam\ttype\taction\ta\tb\tc\n'
        '3\t1\t0\tCAT\tcheesespawn\t4,5\t20\t\n3\t1\t0\tCAT\tcheesespawn\t15,5\t20\t\n'   # (16,5) mirrored: x -> 19-x
        '7\t9\t1\tRAT\tpickup\t4,5\t\t\n9\t8\t2\tRAT\tpickup\t15,5\t\t\n'
        '10\t1\t0\tCAT\tcheesespawn\t4,6\t20\t\n10\t1\t0\tCAT\tcheesespawn\t15,6\t20\t\n'
        '14\t8\t2\tRAT\tpickup\t4,6\t\t\n'
        '20\t9\t1\tRAT\tcheesespawn\t10,2\t7\t\n21\t8\t2\tRAT\tpickup\t10,2\t\t\n')
    eo2 = er.analyse(d)
    m1, m2 = eo2['mines']
    check(m1['mine'] == [3, 5] and m1['side'] == 'A' and m1['gotA'] == 20 and m1['waitA'] == 4.0
          and m1['gotB'] == 20 and m1['waitB'] == 4.0 and m1['firstB'] == 14, f"econ mine A {m1}")
    check(m2['mine'] == [16, 5] and m2['side'] == 'B' and m2['gotB'] == 20 and m2['left'] == 20, f"econ mine B {m2}")
    check(eo2['A']['own'] == 20 and eo2['B']['enemy'] == 20 and eo2['B']['own'] == 20 and eo2['B']['drops'] == 7
          and eo2['A']['lostOwnSide'] == 20 and eo2['B']['lostOwnSide'] == 20, f"econ teams {eo2['A']} {eo2['B']}")
    check('lost on own side' in er.render(eo2), "econ render")
# panel_pick: per-opponent game rates of one build from replica rows; panel = rates in [lo, hi] with enough games
pk = load('panel_pick')
prow = lambda a, b, sa, sb, src='replica': dict(source=src, teamA=a, teamB=b, scoreA=str(sa), scoreB=str(sb))
pacc = pk.rates([prow('us:s', 'x', 2, 1), prow('x', 'us:s', 1, 2), prow('y', 'us:s', 3, 0), prow('y', 'us:s', 3, 0),
                 prow('z', 'us:s', 0, 3), prow('z', 'us:s', 0, 3), prow('us:t', 'x', 3, 0), prow('us:s', 'x', 3, 0, 'local'),
                 prow('us:s', 'us:t', 3, 0), prow('w', 'us:s', 1, 2)], 's')
check(pacc == {'x': [4, 6], 'y': [0, 6], 'z': [6, 6], 'w': [2, 3]}, f"panel_pick rates {pacc}")
check(pk.pick(pacc, 25, 75, 6) == (['x'], ['z'], ['y'], ['w']), f"panel_pick pick {pk.pick(pacc, 25, 75, 6)}")
# king_report: HP lost while the team has no cheese counts as starving, the rest as attack; enemy rats near the king
kr = load('king_report')
with tempfile.TemporaryDirectory() as d:
    json.dump({'map': 'M', 'teamA': 'a', 'teamB': 'b'}, open(os.path.join(d, 'g0.meta.json'), 'w'))
    open(os.path.join(d, 'g0.rounds.tsv'), 'w').write('round\tkingsA\tkingsB\n1\t101\t101\n2\t1\t101\n3\t1\t101\n')
    open(os.path.join(d, 'g0.turns.tsv'), 'w').write('round\tid\tteam\ttype\tx\ty\thp\n'
        '1\t1\t1\tKING\t0\t0\t600\n1\t2\t2\tKING\t9\t9\t600\n'
        '2\t1\t1\tKING\t0\t0\t590\n2\t2\t2\tKING\t9\t9\t590\n2\t7\t1\tRAT\t8\t8\t100\n'
        '3\t1\t1\tKING\t0\t0\t580\n3\t2\t2\tKING\t9\t9\t570\n3\t7\t1\tRAT\t8\t8\t100\n')
    open(os.path.join(d, 'g0.actions.tsv'), 'w').write('round\tid\tteam\ttype\taction\ta\tb\tc\n'
        '2\t7\t1\tRAT\tbite\t2\tKING\t2\n3\t7\t1\tRAT\tbite\t2\tKING\t2\n3\t7\t1\tRAT\tdie\t2\tdamage\tKING\n')
    ko = {o['team']: o for o in kr.analyse(d, 0)}
    check(ko['a']['starveHp'] == 20 and ko['a']['otherHp'] == 0 and ko['a']['broke'] == 2 and ko['a']['died'] is None,
          f"king_report starving {ko['a']}")
    check(ko['b']['starveHp'] == 0 and ko['b']['otherHp'] == 30 and ko['b']['cause'] == {'bites': 2}
          and ko['b']['died'] == 3 and ko['b']['firstFoe'] == 2, f"king_report attacked {ko['b']}")
    check('HP lost starving 20' in kr.render(list(ko.values())), "king_report render")
import subprocess as _sp
_r = _sp.run([os.path.join(REPO, 'tools', 'snapshot.sh'), 'zz_selftest', os.path.join(REPO, 'no_such_dir')], capture_output=True, text=True)
check(_r.returncode != 0 and not os.path.exists(os.path.join(REPO, 'bots', 'zz_selftest')), "snapshot.sh rejects a missing source")
print(f"test_tools: {checks - fails}/{checks} passed")
sys.exit(1 if fails else 0)
