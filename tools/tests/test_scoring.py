#!/usr/bin/env python3
"""score/arena/gauntlet/games/rows (unscorable rows are flagged, never mis-scored; one ReplayDump run per game and no
stdout leak), field_report on downloaded-match directories built from the fixtures (a 2-game merge with a reversed
game, a king formation and a trap backstab), and mirror_check on synthetic mirrored dumps for every symmetry (plus
build/diag/fos2 vs fos3 when present). ReplayDump is built with tools/build-tools.sh (fails, never skips).
contest.py paired --graded on the field-test matches and synthetic ones. Usage: test_scoring.py"""
import collections, contextlib, csv, importlib.util, io, json, os, shutil, subprocess, sys, tempfile, types
REPO = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
TOOLS = os.path.join(REPO, 'tools')
FX = {f: os.path.join(REPO, 'test', 'fixtures', f + '.bc26') for f in ('throws', 'king')}
fails = checks = 0


def check(ok, what):
    global fails, checks
    checks += 1
    if not ok:
        fails += 1; print('FAIL', what)


def load(name, path=TOOLS):
    spec = importlib.util.spec_from_file_location(name, os.path.join(path, name + '.py'))
    m = importlib.util.module_from_spec(spec); spec.loader.exec_module(m)
    return m


sys.path.insert(0, TOOLS)
sc, arena, gt, gm, rw = load('score'), load('arena'), load('gauntlet'), load('games'), load('rows')
GR, FR, MC = load('game_report'), load('field_report'), load('mirror_check')
RM, CT = load('replay_merge', os.path.join(TOOLS, 'galaxy26')), load('contest', os.path.join(TOOLS, 'galaxy26'))
REASON = {'kings': "The winning team destroyed all of the enemy team's rat kings.",
          'points': 'The winning team had more points at the end of the game.'}
P = subprocess.CompletedProcess


def score_tests():
    for text, wt in ((REASON['kings'], 'RATKING_DESTROYED'), (REASON['points'], 'MORE_POINTS'),
                     ('The winning team won on tiebreakers (more cheese)', 'MORE_CHEESE'),
                     ('The winning team won on tiebreakers (more robots alive).', 'MORE_ROBOTS'),
                     ('The winning team won arbitrarily (coin flip).', 'COIN_FLIP'),
                     ('Other team has resigned. Congrats on scaring them I guess...', 'RESIGNATION'), (None, None)):
        check(sc.reason_type(text) == wt, f'reason_type {text} -> {sc.reason_type(text)}')
    cases = [({'winner': None}, 'no result', (0.0, 0.0)),
             ({'winner': 'A', 'summary_error': 'x', 'reason': REASON['points']}, 'summary error', (0.0, 0.0)),
             ({'winner': 'B', 'summary_error': 'x', 'reason': REASON['kings']}, '', (0.0, 100.0)),
             ({'winner': 'A', 'reason': REASON['points']}, 'no summary', (0.0, 0.0)),
             ({'winner': 'A', 'reason': REASON['points'], 'ptsA': 0, 'ptsB': 0}, 'no summary', (0.0, 0.0)),  # old error-JSON row
             ({'winner': 'A', 'reason': REASON['kings'], 'ptsA': 0, 'ptsB': 0}, '', (100.0, 0.0)),
             ({'winner': 'A', 'ptsA': 30, 'ptsB': 10}, '', (75.0, 25.0)),
             ({'winner': 'A', 'winType': 'MORE_POINTS', 'catDmgA': 10, 'catDmgB': 30, 'kingsA': 1, 'kingsB': 1,
               'backstabRound': -1}, '', (100 * 27 / 79, 100 * 52 / 79))]
    for row, why, cell in cases:
        c = sc.cell(row)
        check(sc.missing(row) == why and abs(c[0] - cell[0]) < 1e-9 and abs(c[1] - cell[1]) < 1e-9,
              f'score {row}: {sc.missing(row)!r} {c}')


def arena_tests(tmp):
    good = json.dumps({'game': 0, 'winner': 'A', 'winType': 'MORE_POINTS', 'catDmgA': 3, 'catDmgB': 1, 'kingsA': 1,
                       'kingsB': 1, 'backstabRound': -1, 'dumpVersion': 2, 'A': {}, 'B': {}})
    for p, ok in ((P([], 1, '', 'boom'), 'exit 1'), (P([], 0, '', ''), 'no summary'),
                  (P([], 0, '{"file":"f","error":"java.io.EOFException"}\n', ''), 'EOFException'),
                  (P([], 0, good + '\n{"file":"f","error":"late"}\n', ''), 'late'),
                  (P([], 0, '{"game":0,"winner":"A"}\n', ''), 'winType'), (P([], 0, 'junk\n', ''), 'not JSON')):
        s, e = arena.parse_summary(p)
        check(s is None and ok in (e or ''), f'arena parse_summary error {ok}: {e}')
    s, e = arena.parse_summary(P([], 0, good + '\n', ''))
    row = arena.add_summary({'winner': 'A', 'reason': REASON['points']}, s, e)
    check(e is None and (row['ptsA'], row['ptsB']) == (52, 27) and row['dumpVersion'] == 2 and sc.missing(row) == '', f'arena add_summary {row}')
    row = arena.add_summary({'winner': 'A', 'reason': REASON['points']}, None, 'ReplayDump: x')
    check('ptsA' not in row and 'winType' not in row and sc.missing(row) == 'summary error' and sc.cell(row) == (0.0, 0.0),
          f'arena add_summary error {row}')
    # real runs: summary mode, tsv mode (same summary, files written), a broken replay; nothing on stdout
    out = os.path.join(tmp, 'tsv')
    bad = os.path.join(tmp, 'bad.bc26'); open(bad, 'wb').write(b'\x1f\x8b not a replay')
    code = ('import json, sys; sys.path.insert(0, %r); import arena\n'
            's1 = arena.summarize(%r); s2 = arena.summarize(%r, %r); s3 = arena.summarize(%r)\n'
            'open(%r, "w").write(json.dumps([s1, s2, s3]))') % (TOOLS, FX['throws'], FX['throws'], out, bad,
                                                               os.path.join(tmp, 'sum.json'))
    p = subprocess.run([sys.executable, '-I', '-c', code], capture_output=True, text=True)
    check(p.returncode == 0 and p.stdout == '', f'arena summarize leaves stdout empty: {p.stdout[:200]!r} {p.stderr[-500:]}')
    if p.returncode == 0:
        (s1, e1, w1), (s2, e2, _), (s3, e3, _) = json.load(open(os.path.join(tmp, 'sum.json')))
        check(e1 is None and w1 is None and (s1['winner'], s1['winType'], s1['rounds'], s1['backstabRound'])
              == ('B', 'RATKING_DESTROYED', 138, 16) and s1['dumpVersion'] >= 2, f'arena summary of throws.bc26 {e1} {str(s1)[:200]}')
        check(e2 is None and s2 == s1 and os.path.exists(os.path.join(out, 'g0.turns.tsv'))
              and json.load(open(os.path.join(out, 'g0.summary.json'))) == s1, f'arena tsv run: same summary, files {e2}')
        check(s3 is None and 'ReplayDump' in (e3 or ''), f'arena broken replay -> error {e3}')
    arena_build_tests(tmp)
    arena_run_game_tests(tmp)


@contextlib.contextmanager
def fake_run(calls, build_rc=0):
    """subprocess.run recording every call: build-tools.sh exits build_rc, a game server writes throws.bc26 as its
    replay (no real game); everything else (ReplayDump, MapReseed) runs for real."""
    real = subprocess.run

    def run(cmd, *a, **k):
        calls.append((list(cmd), k))
        if os.path.basename(cmd[0]) == 'build-tools.sh':
            return P(cmd, build_rc, '', '' if build_rc == 0 else 'ReplayDump.java:12: error: boom')
        if 'battlecode.server.Main' in cmd:
            shutil.copy(FX['throws'], next(x.split('=', 1)[1] for x in cmd if x.startswith('-Dbc.server.save-file=')))
            return P(cmd, 0, f"[server] Version41 (B) wins (round 138)\n[server] Reason: {REASON['kings']}\n", '')
        return real(cmd, *a, **k)
    subprocess.run = run
    try:
        yield
    finally:
        subprocess.run = real


def arena_build_tests(tmp):
    """A failed tools build: not cached, retried after TOOLS_RETRY; the existing classes are used with a warning."""
    builds = lambda: sum(os.path.basename(c[0]) == 'build-tools.sh' for c, _ in calls)
    arena._tools.update(ok=False, err=None, at=None)
    calls = []
    with fake_run(calls, 1):
        s, e, w = arena.summarize(FX['throws'])
        row = arena.add_summary({'winner': 'B', 'reason': REASON['kings']}, s, e, w)
        check(e is None and 'build-tools.sh failed' in (w or '') and 'error: boom' in row.get('tools_warning', '')
              and row.get('winType') == 'RATKING_DESTROYED' and sc.missing(row) == '' and builds() == 1,
              f'arena: a failed build runs the existing classes, tools_warning in the row {e} {w} {row}')
        check(arena.build_tools() and builds() == 1, 'arena: a failed build is retried only after TOOLS_RETRY')
        old_cp, arena.TOOLS_CP = arena.TOOLS_CP, os.path.join(tmp, 'notools')
        arena._tools['at'] = 0
        s, e, w = arena.summarize(FX['throws'])
        check(s is None and 'build-tools.sh failed' in (e or '') and w is None and builds() == 2,
              f'arena: a failed build with no classes is the summary error {e} {w}')
        old = {k: getattr(arena, k) for k in ('REPO', 'MAPS')}
        arena.REPO, arena.MAPS = os.path.join(tmp, 'repo'), os.path.join(tmp, 'repo', 'maps')
        os.makedirs(os.path.join(arena.REPO, 'build')); os.makedirs(arena.MAPS)
        shutil.copy(os.path.join(REPO, 'maps', 'DefaultSmall.map26'), arena.MAPS)
        try:
            arena._tools['at'] = 0
            arena.map_dir(7)
            check(False, 'arena map_dir: a failed build with no classes raises')
        except RuntimeError as x:
            check('build-tools.sh failed' in str(x), f'arena map_dir raises RuntimeError (not SystemExit) {x}')
        arena.TOOLS_CP = old_cp
        err = io.StringIO()
        with contextlib.redirect_stderr(err):
            d = arena.map_dir(7)
        check(os.listdir(d) == ['DefaultSmall.map26'] and 'existing build/tools classes' in err.getvalue(),
              f'arena map_dir: MapReseed falls back to the existing classes {os.listdir(d)} {err.getvalue()}')
        open(os.path.join(arena.MAPS, 'Broken.map26'), 'wb').write(b'not a map')
        try:
            with contextlib.redirect_stderr(io.StringIO()):
                arena.map_dir(8)
            check(False, 'arena map_dir: a MapReseed failure raises')
        except RuntimeError as x:
            check('MapReseed failed' in str(x) and not os.path.exists(os.path.join(arena.REPO, 'build', 'maps', 's8')),
                  f'arena map_dir: MapReseed failure is a RuntimeError {x}')
        for k, v in old.items():
            setattr(arena, k, v)
    calls = []
    with fake_run(calls, 0):
        arena._tools['at'] = 0
        e1, e2 = arena.build_tools(), arena.build_tools()
        check(e1 is None and e2 is None and arena.tools_ready('bc26tools.ReplayDump') == (None, None) and builds() == 1,
              f'arena: the build succeeds on retry and is then not run again {e1} {e2} {builds()}')
    # matrix resolves every seed's map dir in the main thread before any game
    order, real_md, real_rg = [], arena.map_dir, arena.run_game
    cf = os.path.join(tmp, 'cells.tsv')
    open(cf, 'w').write('cls:/c/1:p1\tcls:/c/2:p2\tM\t0\ncls:/c/1:p1\tcls:/c/2:p2\tM\t5\ncls:/c/2:p2\tcls:/c/1:p1\tM\t5\n')

    def md(seed):
        order.append(('map', seed))
        if seed == 5 and fail[0]:
            raise RuntimeError('MapReseed failed: boom')
        return tmp
    arena.map_dir, arena.run_game = md, lambda *c, **k: order.append(('game', c[3])) or {'a_hash': c[0].hash}
    try:
        fail = [True]
        try:
            arena.matrix(cf, os.path.join(tmp, 'run1'), 2)
            check(False, 'arena matrix: a map dir failure raises')
        except RuntimeError:
            check(order == [('map', 0), ('map', 5)] and not os.path.exists(os.path.join(tmp, 'run1', 'DONE')),
                  f'arena matrix: a map failure stops the run before any game {order}')
        fail[0], order[:] = False, []
        with contextlib.redirect_stdout(io.StringIO()):
            arena.matrix(cf, os.path.join(tmp, 'run2'), 2)
        check(order[:2] == [('map', 0), ('map', 5)] and sorted(order[2:]) == [('game', 0), ('game', 5), ('game', 5)],
              f'arena matrix: map dirs first, then the games {order}')
    finally:
        arena.map_dir, arena.run_game = real_md, real_rg


def arena_run_game_tests(tmp):
    """run_game with no real game: one ReplayDump run per game, its stdout captured (nothing leaks to ours)."""
    bot = lambda n: types.SimpleNamespace(name=n, hash=n * 12, spec='bots/' + n, pkg=n, cls='/nonexistent')
    old_slots, arena.SLOTS_DIR = arena.SLOTS_DIR, os.path.join(tmp, 'slots')
    try:
        for tsv in (False, True):
            calls, out = [], io.StringIO()
            with fake_run(calls), contextlib.redirect_stdout(out):
                row = arena.run_game(bot('a'), bot('b'), 'M', out=os.path.join(tmp, f'game{tsv}'), tsv=tsv)
            rd = [k for c, k in calls if 'bc26tools.ReplayDump' in c]
            check(len(rd) == 1 and all(k.get('capture_output') or k.get('stdout') == subprocess.PIPE for _, k in calls)
                  and out.getvalue() == '', f'arena run_game (tsv {tsv}): one ReplayDump run, all output captured {calls}')
            check((row.get('winner'), row.get('rounds'), row.get('winType'), row.get('backstabRound')) == ('B', 138, 'RATKING_DESTROYED', 16)
                  and 'tools_warning' not in row and 'summary_error' not in row and (('replay' in row) == tsv),
                  f'arena run_game (tsv {tsv}) row {row}')
    finally:
        arena.SLOTS_DIR = old_slots


def gauntlet_games_rows_tests(tmp):
    res = os.path.join(tmp, 'r.jsonl')
    base = dict(a_hash='O', b_hash='H1', a_spec='bots/x', b_spec='cls:/c/1:p1', a='x', b='p1', map='M', seed=0, rounds=100)
    rows = [dict(base, winner='A', winType='RATKING_DESTROYED', dumpVersion=2,
                 A={'deathsByEnemy': 2, 'deathsRat': 9, 'overruns': 3, 'near90': 41},
                 B={'deathsByEnemy': 6, 'deathsRat': 7, 'overruns': 0, 'near90': 5}),
            dict(base, winner='B', reason=REASON['points'], summary_error='ReplayDump: x'),     # unscored
            dict(base, winner='A', reason=REASON['points'], ptsA=0, ptsB=0),                    # old error-JSON row
            dict(base, map='M2', error='crash')]                                                # no result
    open(res, 'w').write(''.join(json.dumps(r) + '\n' for r in rows))
    fld = os.path.join(tmp, 'F.tsv'); open(fld, 'w').write('opp1\t/c/1\tp1\n')
    sk = collections.Counter()
    t = gt.table(res, 'O', path=fld, skipped=sk)
    check(t == [('opp1', 2, 3, 100.0)] and sk == {'summary error': 1, 'no summary': 1, 'no result': 1},
          f'gauntlet table: unscored games count in W/G, not in points {t} {sk}')
    sk = collections.Counter()
    sm = gt.summary(res, sk)
    check(sm == [('x', 2, 3, 100.0, 3.0, 100.0)] and sum(sk.values()) == 3,
          f'gauntlet summary: kd from deathsByEnemy (v2) {sm} {sk}')
    sk = collections.Counter()
    cs = gt.cell_scores([res], sk)
    check(cs == {('cls:/c/1:p1', 'M', 'A', 0): (1.0, 1.0)} and sum(sk.values()) == 3, f'gauntlet cell_scores {cs} {sk}')
    err = io.StringIO()
    with contextlib.redirect_stderr(err):
        gt.report(sk)
    check('1 no summary' in err.getvalue() and '1 summary error' in err.getvalue(), f'gauntlet report {err.getvalue()}')
    os.makedirs(os.path.join(tmp, 'runs', 'r1'))
    shutil.copy(res, os.path.join(tmp, 'runs', 'r1', 'results.jsonl'))
    un = []
    lr = gm.local_rows(os.path.join(tmp, 'runs'), {'/c/1': 'opp1'}, un)
    check(len(lr) == 1 and (lr[0]['ptsA'], lr[0]['ptsB']) == (100.0, 0.0) and len(un) == 2 and un[0].endswith(':2'),
          f'games local_rows leaves unscored games out, counted {lr} {un}')
    lines = [rw.line(r) for r in rows]
    check('NOSCORE summary error' in lines[1] and 'NOSCORE no summary' in lines[2] and 'ERROR' in lines[3]
          and 'NOSCORE' not in lines[0] and not lines[0].endswith(' v1') and ' o3/41 ' in lines[0].split(' B ')[0]
          and ' o0/5 ' in lines[0].split(' B ')[1], f'rows.py lines {lines}')
    check(rw.line(dict(base, winner='A', winType='MORE_POINTS', ptsA=1, ptsB=2, A={}, B={})).endswith(' v1'), 'rows.py v1 mark')
    check(rw.line(dict(rows[0], tools_warning='build-tools.sh failed')).endswith(' TOOLS') and 'TOOLS' not in lines[0],
          'rows.py tools_warning mark')


def match_dir(root, mid, raw, games):
    os.makedirs(os.path.join(root, str(mid)))
    open(os.path.join(root, str(mid), 'replay.bc26'), 'wb').write(raw)
    json.dump({'match': mid, 'games': games}, open(os.path.join(root, str(mid), 'match.json'), 'w'))


def field_tests(tmp):
    root = os.path.join(tmp, 'replays')
    th, kg = open(FX['throws'], 'rb').read(), open(FX['king'], 'rb').read()
    sth, skg = GR.summary(GR.dump(FX['throws']), 0), GR.summary(GR.dump(FX['king']), 0)
    team = 'Version41'          # team B of both fixtures
    G = lambda s, i, rev: {'game': i + 1, 'map': s['map'], 'team_a': s['teamA'], 'team_b': s['teamB'], 'reversed': rev,
                           'winner_team': s['team' + s['winner']], 'rounds': s['rounds'], 'reason': s['winType']}
    match_dir(root, 7, RM.merge([th, kg]), [G(sth, 0, False), G(skg, 1, True)])
    match_dir(root, 8, kg, [dict(G(skg, 0, False), map='elsewhere')])               # disagrees with the replay
    match_dir(root, 9, th, [G(sth, 0, False)])
    check(FR.matches(root) == [7, 8, 9] and FR.matches(root, '8-9') == [8, 9] and FR.matches(root, last=1) == [9]
          and FR.ids('3-5,9') == [3, 4, 5, 9], 'field_report match selection')
    bf = os.path.join(tmp, 'replica-matches.json')       # games.py replica's file: match 7 by build Version41, 9 by s9x
    json.dump({'packages': {'5': 'Version41', '6': 's9x', '4': 's1x'}, 'subs': [['2026-10-01T00:00:00+00:00', 's1x']],
               'matches': [{'id': i, 'created': '2026-10-02T00:00:00+00:00', 'participants': [{'teamname': team, 'submission': sb}]}
                           for i, sb in ((7, 5), (8, None), (9, 6))]}, open(bf, 'w'))
    recs, errs = FR.collect(FR.matches(root), root, team, quiet=True, builds_from=bf)
    check(list(errs) == [8] and 'disagrees' in errs[8], f'field_report: match.json disagreeing with the replay is an error {errs}')
    check([(r['build'], r['pkg'], r['buildOk']) for r in recs] == [('Version41', 'Version41', True)] * 2 + [('s9x', 'Version41', False)]
          and FR.build_fits('s0e', 'ratbot') and not FR.build_fits('ratbot', 's2o') and FR.build_fits('?', 's2o') is None
          and FR.build_fits('s2o', None) is None, f'field_report build attribution against the replay package {recs}')
    check([(r['match'], r['game'], r['opp'], r['us'], r['side'], r['won'], r['end'], r['rounds']) for r in recs] ==
          [(7, 0, 's1g', 'B', 'B', True, 'kings', 138), (7, 1, 's1g', 'B', 'A', True, 'kings', 349),
           (9, 0, 's1g', 'B', 'B', True, 'kings', 138)], f'field_report records (a merged replay keeps the first game\'s team names) {recs}')
    r0, r1 = recs[0], recs[1]
    check(r0['cell'] == 100.0 and (r0['ptsUs'], r0['ptsThem']) == sc.points(sth)[::-1]
          and r0['catUs'] == sth['catDmgB'] and r0['xThem'] == sth['cheeseXferA'] and r0['kUs'] == sth['kingsB'] % 10
          and r0['bankUs'] == sth['kingsB'] // 10 and r0['bs'] == 16 and r0['bsBy'] == 'us' and r0['bsCause'] == 'bite',
          f'field_report record fields {r0}')
    check(r1['bsBy'] == 'us' and r1['bsCause'] == 'trap' and r1['kUs'] == 2 and r1['kMaxUs'] == 2 and r1['k2Us'] is not None
          and r1['k2Them'] is None and r1['kingsAt'][100] == [1, 1] and 349 not in r1['kingsAt'] and 500 not in r1['kingsAt'],
          f'field_report king formation / trap backstab {r1}')
    d7 = GR.dump(os.path.join(root, '7', 'replay.bc26'))
    for g, s in ((0, sth), (1, skg)):
        b = FR.bytecode(d7, g)
        with open(os.path.join(d7, f'g{g}.turns.tsv'), newline='\n') as f:
            raw = list(csv.DictReader(f, delimiter='\t', quoting=csv.QUOTE_NONE))
        for t, n in ((1, 'A'), (2, 'B')):
            x = b[t]
            bc = sorted(int(r['bc']) for r in raw if r['src'] == 'own' and r['team'] == str(t) and r['type'] == 'RAT')
            p99 = bc[(99 * (len(bc) - 1)) // 100]          # nearest rank below, as bytecode() documents
            check(sum(v['over'] for v in x.values()) == s[n]['overruns'] and sum(v['near90'] for v in x.values()) == s[n]['near90']
                  and x['RAT']['max'] == s[n]['maxBcRat'] and x['KING']['max'] == s[n]['maxBcKing']
                  and x['RAT']['mean'] == s[n]['meanBcRat'] and x['RAT']['turns'] == len(bc) == s[n]['turnsRat']
                  and x['RAT']['p99'] == p99 and len(x['RAT']['first']) == min(5, x['RAT']['over']),
                  f'field_report bytecode g{g} {n} = summary {x} {s[n]} p99 {p99}')
    check(r0['ovrUs'] == 2 and r0['firstOvrUs'] == min(f[0] for v in FR.bytecode(d7, 0)[2].values() for f in v['first']),
          f'field_report first overrun {r0["firstOvrUs"]}')
    a = FR.aggregate(recs)
    check(a['games'] == 3 and a['wins'] == 3 and a['ends'] == {'W kings': {'n': 3, 'medianRound': 138}}
          and a['side'] == {'A': (1, 1), 'B': (2, 2)} and a['backstab']['byUs'] == 3 and a['ovrUs'] == 2 + 2 + 13
          and a['ovrGames'] == 3 and a['k2Us'] == 1 and a['at'][100]['n'] == 3 and a['buildMismatch'] == 1,
          f'field_report aggregate {a}')
    txt = '\n'.join(FR.report(recs))
    check('== by opp' in txt and 's1g' in txt and '== by build' in txt and 'overruns 17 in 3 games' in txt
          and txt.count('BUILD MISMATCH in 1 games') == 3, f'field_report text {txt[:600]}')
    # how a points game ends: both last kings dead in one round (GameWorld.checkWin) is a kings end, not cats
    d9 = os.path.join(tmp, 'd9')
    shutil.copytree(GR.dump(os.path.join(root, '9', 'replay.bc26')), d9)
    os.chmod(d9, 0o755)
    ends = []
    for rounds, ka, kb in ((300, 0, 0), (300, 1, 1), (300, 0, 1), (2000, 1, 1), (2000, 0, 0)):
        json.dump(dict(sth, winType='MORE_POINTS', rounds=rounds, numKingsA=ka, numKingsB=kb, maxRounds=2000),
                  open(os.path.join(d9, 'g0.summary.json'), 'w'))
        ends.append(FR.record(d9, 0, {}, team)['end'])
    check(ends == ['kings', 'cats', 'cats', 'limit', 'kings'], f'field_report points-game ends {ends}')
    gr = FR.write_graded(7, root, team)
    gp = os.path.join(root, '7', 'graded.json')
    j = json.load(open(gp))
    dh = FR.dumper()
    check([(x['side'], x['score'], x['won']) for x in gr] == [('B', 1.0, True), ('A', 1.0, True)] and j['games'] == gr
          and j['replay_sha1'] == GR.file_sha(os.path.join(root, '7', 'replay.bc26')) and j['dumper'] == dh
          and d7.endswith('-' + dh) and j['score_version'] == sc.VERSION, f'field_report graded {gr} {j}')
    check(FR.stored_graded(7, root, team, dh) == gr and FR.stored_graded(7, root, team) == gr
          and FR.stored_graded(7, root, 'other', dh) is None and FR.stored_graded(9, root, team, dh) is None,
          'field_report stored graded.json is current')
    for k, v in (('dumper', '0' * 12), ('score_version', sc.VERSION - 1), ('replay_sha1', '0' * 40)):
        json.dump(dict(j, **{k: v}), open(gp, 'w'))
        stale, err = FR.stored_graded(7, root, team, dh), io.StringIO()
        with contextlib.redirect_stderr(err):
            gc = FR.graded_cached(7, root, team, dh)
        check(stale is None and gc == gr and json.load(open(gp)) == j and 'grading match 7' in err.getvalue(),
              f'field_report graded.json with another {k} is rebuilt {err.getvalue()}')
    json.dump(dict(j, games=[dict(x, score=0.25) for x in gr]), open(gp, 'w'))
    err = io.StringIO()
    with contextlib.redirect_stderr(err):
        gc = FR.graded_cached(7, root, team, dh)
    check([x['score'] for x in gc] == [0.25, 0.25] and not err.getvalue(), 'field_report a current graded.json is used as stored')
    os.remove(gp)
    run = lambda *a: subprocess.run([sys.executable, '-I', os.path.join(TOOLS, 'field_report.py'), *a], capture_output=True, text=True)
    p = run('--dir', root, '--team', team, '--ids', '7,9', '--builds', bf)
    check(p.returncode == 0 and '== all ==' in p.stdout and "match 9 game 0: our build by time is s9x, but the replay's "
          "package is Version41" in p.stderr and 'match 7 game' not in p.stderr, f'field_report CLI {p.returncode} {p.stderr[-300:]}')
    p = run('--dir', root, '--team', team)
    check(p.returncode == 1 and 'match 8 skipped' in p.stderr, f'field_report CLI reports a bad match {p.returncode} {p.stderr[-300:]}')
    p = run('games', '--dir', root, '--team', team, '--ids', '9')
    check(p.returncode == 0 and [json.loads(x)['rounds'] for x in p.stdout.splitlines()] == [138], f'field_report games {p.stdout[:200]}')
    p = run('games', '--csv', '--dir', root, '--team', team, '--ids', '7')
    check(p.returncode == 0 and len(p.stdout.splitlines()) == 3 and 'kingsAt' not in p.stdout.splitlines()[0], 'field_report games --csv')
    p = run('graded', '7,10', '--dir', root, '--team', team)
    check(p.returncode == 1 and len(p.stdout.splitlines()) == 2 and 'not downloaded: [10]' in p.stderr, f'field_report graded CLI {p.stderr}')
    p = run('bc', FX['throws'])
    check(p.returncode == 0 and 'B RAT' in p.stdout and 'n/a (no indicator strings)' not in p.stdout, f'field_report bc {p.stdout[:300]}')
    contest_tests(tmp, root, team)


def contest_tests(tmp, root, team):
    """contest.py paired --graded: graded scores from the replays (graded.json written, then reused), and paired_cmd
    on synthetic matches with current graded.json files."""
    bad = {}
    cells = CT.graded_cells([7, 9, 10], root, team, bad)
    check(cells == [(o, m, sd, 1.0) for o, m, sd, w in CT.block_cells_all([7, 9], root, team)] and len(cells) == 3 and not bad
          and all(FR.stored_graded(i, root, team) for i in (7, 9)), f'contest graded_cells {cells} {bad}')
    gp = os.path.join(root, '9', 'graded.json')
    j = json.load(open(gp))
    json.dump(dict(j, games=[dict(j['games'][0], side='A')]), open(gp, 'w'))       # disagrees with match.json
    check(CT.graded_cells([9], root, team, bad) == [] and 'match.json' in bad.get(9, ''), f'contest graded_cells cross-check {bad}')
    os.remove(gp)
    croot, dh = os.path.join(tmp, 'contest'), FR.dumper()

    def mk(mid, results, graded=True):
        os.makedirs(os.path.join(croot, str(mid)))
        rp = os.path.join(croot, str(mid), 'replay.bc26')
        open(rp, 'wb').write(b'not a replay %d' % mid)
        gs = [{'game': i, 'map': m, 'team_a': CT.TEAM, 'team_b': 'opp', 'reversed': False,
               'winner_team': CT.TEAM if sc_ >= 0.5 else 'opp', 'rounds': 2000, 'reason': 'MORE_POINTS'}
              for i, (m, sc_) in enumerate(results)]
        json.dump({'match': mid, 'games': gs}, open(os.path.join(croot, str(mid), 'match.json'), 'w'))
        if graded:
            json.dump({'match': mid, 'team': CT.TEAM, 'replay_sha1': GR.file_sha(rp), 'dumper': dh, 'score_version': sc.VERSION,
                       'games': [{'game': i, 'map': m, 'side': 'A', 'won': sc_ >= 0.5, 'score': sc_}
                                 for i, (m, sc_) in enumerate(results)]},
                      open(os.path.join(croot, str(mid), 'graded.json'), 'w'))
    mk(21, [('M1', 0.6), ('M2', 0.3)])
    mk(22, [('M1', 0.8), ('M2', 0.45)])
    mk(23, [('M1', 0.9)], graded=False)                     # no graded.json and no replay: cannot be graded
    os.remove(os.path.join(croot, '23', 'replay.bc26'))
    run = lambda *blocks, **k: _paired(types.SimpleNamespace(**dict(dict(ref='21', blocks=list(blocks), partial=False,
                                                                          dir=croot, studied=None), **k)))
    rc, out = run('22', graded=True)
    check(rc == 0 and 'champion 0.60/1  cand 0.80/1  d +0.20' in out and 'champion 0.30/1  cand 0.45/1  d +0.15' in out
          and '~ paired (graded): 2 cells, 0 alike; D 2 (gained 2, lost 0), net +0.35, t 1.40' in out, f'contest paired --graded {out}')
    for kw in ({'graded': False}, {}):
        rc, out = run('22', **kw)
        check(rc == 0 and out == '~ paired: 2 cells, 2 alike; D 0 (gained 0, lost 0), net +0.00, t 0.00\n', f'contest paired {kw} {out}')
    rc, out = run('22', '23', graded=True)
    check(rc == 3 and '~ candidate matches not graded: [23]' in out and 'refused' in out, f'contest paired --graded refuses {out}')
    rc, out = run('22', '23', graded=True, partial=True)
    check(rc == 0 and '~ paired (graded): 2 cells' in out, f'contest paired --graded --partial {out}')
    rc, out = run('22', ref='21,23', graded=True)
    check(rc == 0 and '~ reference matches not graded (left out): [23]' in out and '~ paired (graded): 2 cells, 0 alike' in out,
          f'contest paired --graded leaves an ungraded reference match out {out}')
    real, bad = CT.field_report, {}
    CT.field_report = lambda: (_ for _ in ()).throw(GR.DumpError('tools/build-tools.sh failed:\nboom'))
    try:
        cells = CT.graded_cells([21, 22], croot, bad=bad)
    finally:
        CT.field_report = real
    check(cells == [] and bad == {21: 'tools/build-tools.sh failed:', 22: 'tools/build-tools.sh failed:'},
          f'contest graded_cells: a dumper that does not build grades nothing {cells} {bad}')


def _paired(a):
    out = io.StringIO()
    with contextlib.redirect_stdout(out):
        rc = CT.paired_cmd(a)
    return rc, out.getvalue()


TH = 'round id team type x y dir hp cheese mcd tcd acd bc coop ind seq src dead carrier'.split()


def mirror_tests(tmp):
    rot = {d: (d + 3) % 8 + 1 for d in range(1, 9)}
    check(all(MC.mirror_dir(d, 0) == rot[d] for d in rot) and MC.mirror_dir(0, 0) == 0, 'mirror_dir rotation = (d+3)%8+1')
    check([MC.mirror_dir(d, 1) for d in range(9)] == [0, 1, 8, 7, 6, 5, 4, 3, 2], 'mirror_dir horizontal (y flipped)')
    check([MC.mirror_dir(d, 2) for d in range(9)] == [0, 5, 4, 3, 2, 1, 8, 7, 6], 'mirror_dir vertical (x flipped)')
    check(all(MC.mirror_dir(MC.mirror_dir(d, s), s) == d for s in (0, 1, 2) for d in range(9)), 'mirror_dir is an involution')
    W, H = 9, 7
    for sym in (0, 1, 2):
        def game(d, swap, poke=None, cat=None):
            os.makedirs(d)
            json.dump({'map': 'M', 'width': W, 'height': H, 'symmetry': sym, 'teamA': 'q' if swap else 'p',
                       'teamB': 'p' if swap else 'q'}, open(os.path.join(d, 'g0.meta.json'), 'w'))
            rows = []
            for rn in range(1, 6):
                seq = 0
                for tm, ids, base in ((1, (11, 12), (1, 1)), (2, (21, 22), (6, 4))):
                    for k, i in enumerate(ids):
                        x, y, dr, team = base[0] + k, base[1] + (rn % 2), 1 + (rn + k) % 8, tm
                        if swap:
                            (x, y), dr, team = MC.mirror_xy(x, y, sym, W, H), MC.mirror_dir(dr, sym), 3 - tm
                        if poke and (rn, tm, k) == poke:
                            dr = dr % 8 + 1
                        rows.append([rn, i + 100 * swap, team, 'RAT', x, y, dr, 100 - rn, 0, 0, 0, 0, 1000, 1, '', seq, 'own', 0, 0])
                        seq += 1
                cx = rn + (rn == cat)
                rows.append([rn, 1, 0, 'CAT', cx, 2, 3, 4000, 0, 0, 0, 0, 0, 1, '', seq, 'own', 0, 0])
                rows.append([rn, 11 + 100 * swap, team, 'RAT', 0, 0, 0, 5, 0, 0, 0, 0, 0, 1, '', seq + 1, 'split', 0, 0])
            with open(os.path.join(d, 'g0.turns.tsv'), 'w') as f:
                f.write('\t'.join(TH) + '\n' + ''.join('\t'.join(map(str, r)) + '\n' for r in rows))
            return d
        x = game(os.path.join(tmp, f'm{sym}x'), False)
        res, lines = MC.check(x, game(os.path.join(tmp, f'm{sym}y'), True))
        check(all(v is None for v in res.values()) and len(res) == 3, f'mirror sym {sym}: a mirrored pair mirrors {res} {lines}')
        res, lines = MC.check(x, game(os.path.join(tmp, f'm{sym}z'), True, poke=(3, 1, 1)))
        check(list(res.values()) == [3, None, None] and any('first divergence r3' in l for l in lines)
              and any('turn   1' in l for l in lines), f'mirror sym {sym}: a turned rat at r3 {res} {lines}')
        w = game(os.path.join(tmp, f'm{sym}w'), True, cat=4)
        res, lines = MC.check(x, w)
        check(list(res.values()) == [None, None, 4] and not any('note:' in l for l in lines),
              f'mirror sym {sym}: cats compared by id, unmirrored {res}')
        res, lines = MC.check(x, game(os.path.join(tmp, f'm{sym}v'), True, poke=(3, 2, 0), cat=2))
        check(list(res.values()) == [None, 3, 2] and [l for l in lines if 'note:' in l]
              == ['  note: q (X B ~ Y A) diverges after the cats (r2): that may come from them (the cats do not count in '
                  'the exit status)'], f'mirror sym {sym}: a note when the cats diverge before a team {res} {lines}')
        res, lines = MC.check(x, game(os.path.join(tmp, f'm{sym}u'), True, poke=(3, 1, 1), cat=4))
        check(list(res.values()) == [3, None, 4] and not any('note:' in l for l in lines),
              f'mirror sym {sym}: no note for a team that diverges before the cats {res} {lines}')
        if sym == 0:
            for args, code in (((x, w), 0), ((x, os.path.join(tmp, 'm0z')), 1), ((x, os.path.join(tmp, 'm0y')), 0)):
                with contextlib.redirect_stdout(io.StringIO()):
                    rc = MC.main(list(args))
                check(rc == code, f'mirror_check exit {rc} for {args}: the teams decide, not the cats')
    try:
        MC.check(os.path.join(tmp, 'm0x'), os.path.join(tmp, 'm1y'))
        check(False, 'mirror: different symmetry refused')
    except ValueError:
        pass
    bad = os.path.join(tmp, 'bad2.bc26'); open(bad, 'wb').write(b'\x1f\x8b not a replay')
    p = subprocess.run([sys.executable, '-I', os.path.join(TOOLS, 'mirror_check.py'), bad, os.path.join(tmp, 'm0x')],
                       capture_output=True, text=True)
    check(p.returncode == 2 and 'ReplayDump failed' in p.stderr, f'mirror_check: a failed dump exits 2 {p.returncode} {p.stderr[-300:]}')
    a, b = (os.path.join(REPO, 'build', 'diag', d) for d in ('fos2', 'fos3'))
    if os.path.isdir(a) and os.path.isdir(b):
        fa, fb = (os.path.join(d, sorted(f for f in os.listdir(d) if f.endswith('.bc26'))[0]) for d in (a, b))
        res, lines = MC.check(GR.dump(fa), GR.dump(fb))
        notes = [l.split(':')[1] for l in lines if 'note:' in l]
        check(list(res.values()) == [24, 106, 54] and notes == [' s2o (X B ~ Y A) diverges after the cats (r54)'],
              f'mirror fos2/fos3: ours r24, opponent r106 (after the cats: a note), cats r54 {res} {notes}')
    else:
        print('SKIP mirror fos2/fos3 (build/diag not here)')


def main():
    b = subprocess.run([os.path.join(TOOLS, 'build-tools.sh')], capture_output=True, text=True)
    if b.returncode != 0:
        print(f'FAIL build-tools.sh: {b.stderr[-2000:]}')
        print('scoring: 0/1 passed'); sys.exit(1)
    with tempfile.TemporaryDirectory() as tmp:
        score_tests()
        arena_tests(tmp)
        gauntlet_games_rows_tests(tmp)
        field_tests(tmp)
        mirror_tests(tmp)
    print(f'scoring: {checks - fails}/{checks} passed')
    sys.exit(1 if fails else 0)


if __name__ == '__main__':
    main()
