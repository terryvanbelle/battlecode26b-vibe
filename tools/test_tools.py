#!/usr/bin/env python3
"""Tests for the python tools (synthetic inputs). Exit 1 on failure."""
import importlib.util, json, os, subprocess, sys, tempfile
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
# points(): cooperation weights 50/30/20, backstab 30/50/20
row = {'catDmgA': 300, 'catDmgB': 100, 'kingsA': 1, 'kingsB': 1, 'cheeseXferA': 0, 'cheeseXferB': 0, 'backstabRound': -1}
check(arena.points(row) == (52, 28), f"points coop {arena.points(row)}")
row['backstabRound'] = 500
check(arena.points(row) == (48, 32), f"points backstab {arena.points(row)}")
check(arena.points({}) == (0, 0), "points empty")
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
        subprocess.run([sys.executable, '-I', os.path.join(REPO, 'tools', 'plot_progress.py'), '--tsv', t, '--out', d], capture_output=True)
        check(os.path.getsize(os.path.join(d, 'progress.png')) > 1000, "progress.png written")
    except ImportError:
        pass
# games.py: player names, local rows, replica rows mapped to our submission by time
gm = load('games')
fld = {'/c/1': 'opp1'}
check(gm.player('cls:/c/1/:pkg', fld) == 'opp1' and gm.player('bots/s2t/', fld) == 'us:s2t'
      and gm.player('bots/examplefuncsplayer', fld) == 'examplefuncsplayer', "games player names")
with tempfile.TemporaryDirectory() as d:
    os.makedirs(os.path.join(d, 'r1'))
    open(os.path.join(d, 'r1', 'results.jsonl'), 'w').write(
        json.dumps(dict(a_spec='bots/x', b_spec='cls:/c/1:p', map='M', winner='B', rounds=200, winType='K', seed=3)) + '\n'
        + 'not json\n' + json.dumps(dict(a_spec='bots/x', b_spec='cls:/c/1:p', winner=None)) + '\n')
    lr = gm.local_rows(d, fld)
    check(len(lr) == 1 and lr[0]['teamA'] == 'us:x' and lr[0]['teamB'] == 'opp1' and lr[0]['winner'] == 'B'
          and (lr[0]['scoreA'], lr[0]['scoreB']) == (0, 1), f"games local rows {lr}")
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
sys.path.insert(0, os.path.join(REPO, 'tools'))
eo = load('elo')
rows = [{'teamA': 'us:x', 'teamB': 'f1', 'scoreA': '1', 'scoreB': '0'}, {'teamA': 'f1', 'teamB': 'us:x', 'scoreA': '1', 'scoreB': '0'},
        {'teamA': 'f1', 'teamB': 'f2', 'scoreA': '2', 'scoreB': '0'}]
rt, field, fs = eo.compute(rows)
check(sorted(field) == ['f1', 'f2'] and all(0 <= v <= 1 for v in fs.values()), f"elo compute {field} {fs}")
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
    try:
        import matplotlib  # noqa: F401
        out = os.path.join(d, 'out')
        r = subprocess.run([sys.executable, '-I', os.path.join(REPO, 'tools', 'plot_progress.py'), '--tsv', t, '--out', out,
                            '--attempts', ac, '--milestones', mp], capture_output=True, text=True)
        check(r.returncode == 0 and '2 milestones' in r.stdout and os.path.getsize(os.path.join(out, 'progress.png')) > 1000,
              f"progress.png with milestones {r.stdout} {r.stderr[-300:]}")
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
check(br.sees(5, 5, 0, 5, 6) and br.sees(5, 5, 0, 6, 6) and not br.sees(5, 5, 0, 6, 5) and not br.sees(5, 5, 0, 5, 4),
      "battle_report cone (north)")
check(br.sees(5, 5, 1, 6, 5) and br.sees(5, 5, 1, 5, 6) and not br.sees(5, 5, 1, 4, 6), "battle_report cone (northeast)")
if os.path.exists(fx) and os.path.isdir(os.path.join(REPO, 'build', 'tools', 'bc26tools')):
    rep = br.report(d, 60, 140, 20)
    check([f['round'] for f in rep['front']] == [60, 80, 100, 120, 140][:len(rep['front'])] and rep['front'][0]['1']['n'] > 0,
          f"battle_report front {rep['front'][:1]}")
    check(rep['grabs']['2']['n'] > rep['grabs']['1']['n'] > 0 and sum(rep['grabs']['2']['fate'].values()) == rep['grabs']['2']['n'],
          f"battle_report grabs {rep['grabs']}")
    check(all(v > 0 for tm in '12' for v in rep['hploss'][tm].values()) and 42 in rep['throws']['2'], "battle_report hploss/throws")
# snapshot.sh refuses a source without java files and leaves nothing behind
import subprocess as _sp
_r = _sp.run([os.path.join(REPO, 'tools', 'snapshot.sh'), 'zz_selftest', os.path.join(REPO, 'no_such_dir')], capture_output=True, text=True)
check(_r.returncode != 0 and not os.path.exists(os.path.join(REPO, 'bots', 'zz_selftest')), "snapshot.sh rejects a missing source")
print(f"test_tools: {checks - fails}/{checks} passed")
sys.exit(1 if fails else 0)
