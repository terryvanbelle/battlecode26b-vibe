#!/usr/bin/env python3
"""score/arena/gauntlet/games/rows (unscorable rows are flagged, never mis-scored; one ReplayDump run per game and no
stdout leak), field_report on downloaded-match directories built from the fixtures (a 2-game merge with a reversed
game, a king formation and a trap backstab), and mirror_check on synthetic mirrored dumps for every symmetry (plus
build/diag/fos2 vs fos3 when present). ReplayDump is built with tools/build-tools.sh (fails, never skips).
Usage: test_scoring.py"""
import collections, contextlib, importlib.util, io, json, os, shutil, subprocess, sys, tempfile
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
RM = load('replay_merge', os.path.join(TOOLS, 'galaxy26'))
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
        (s1, e1), (s2, e2), (s3, e3) = json.load(open(os.path.join(tmp, 'sum.json')))
        check(e1 is None and (s1['winner'], s1['winType'], s1['rounds'], s1['backstabRound']) == ('B', 'RATKING_DESTROYED', 138, 16)
              and s1['dumpVersion'] >= 2, f'arena summary of throws.bc26 {e1} {str(s1)[:200]}')
        check(e2 is None and s2 == s1 and os.path.exists(os.path.join(out, 'g0.turns.tsv'))
              and json.load(open(os.path.join(out, 'g0.summary.json'))) == s1, f'arena tsv run: same summary, files {e2}')
        check(s3 is None and 'ReplayDump' in (e3 or ''), f'arena broken replay -> error {e3}')


def gauntlet_games_rows_tests(tmp):
    res = os.path.join(tmp, 'r.jsonl')
    base = dict(a_hash='O', b_hash='H1', a_spec='bots/x', b_spec='cls:/c/1:p1', a='x', b='p1', map='M', seed=0, rounds=100)
    rows = [dict(base, winner='A', winType='RATKING_DESTROYED', dumpVersion=2, A={'deathsByEnemy': 2, 'deathsRat': 9},
                 B={'deathsByEnemy': 6, 'deathsRat': 7}),
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
          and 'NOSCORE' not in lines[0] and not lines[0].endswith(' v1') and 'o' in lines[0], f'rows.py lines {lines}')
    check(rw.line(dict(base, winner='A', winType='MORE_POINTS', ptsA=1, ptsB=2, A={}, B={})).endswith(' v1'), 'rows.py v1 mark')


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
    recs, errs = FR.collect(FR.matches(root), root, team, quiet=True)
    check(list(errs) == [8] and 'disagrees' in errs[8], f'field_report: match.json disagreeing with the replay is an error {errs}')
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
        for t, n in ((1, 'A'), (2, 'B')):
            x = b[t]
            check(sum(v['over'] for v in x.values()) == s[n]['overruns'] and sum(v['near90'] for v in x.values()) == s[n]['near90']
                  and x['RAT']['max'] == s[n]['maxBcRat'] and x['KING']['max'] == s[n]['maxBcKing']
                  and x['RAT']['mean'] == s[n]['meanBcRat'] and x['RAT']['p99'] <= x['RAT']['max']
                  and len(x['RAT']['first']) == min(5, x['RAT']['over']),
                  f'field_report bytecode g{g} {n} = summary {x} {s[n]}')
    check(r0['ovrUs'] == 2 and r0['firstOvrUs'] == min(f[0] for v in FR.bytecode(d7, 0)[2].values() for f in v['first']),
          f'field_report first overrun {r0["firstOvrUs"]}')
    a = FR.aggregate(recs)
    check(a['games'] == 3 and a['wins'] == 3 and a['ends'] == {'W kings': {'n': 3, 'medianRound': 138}}
          and a['side'] == {'A': (1, 1), 'B': (2, 2)} and a['backstab']['byUs'] == 3 and a['ovrUs'] == 2 + 2 + 13
          and a['ovrGames'] == 3 and a['k2Us'] == 1 and a['at'][100]['n'] == 3, f'field_report aggregate {a}')
    txt = '\n'.join(FR.report(recs))
    check('== by opp' in txt and 's1g' in txt and '== by build' in txt and 'overruns 17 in 3 games' in txt,
          f'field_report text {txt[:600]}')
    gr = FR.write_graded(7, root, team)
    j = json.load(open(os.path.join(root, '7', 'graded.json')))
    check([(x['side'], x['score'], x['won']) for x in gr] == [('B', 1.0, True), ('A', 1.0, True)] and j['games'] == gr
          and j['replay_sha1'] == GR.file_sha(os.path.join(root, '7', 'replay.bc26')), f'field_report graded {gr}')
    run = lambda *a: subprocess.run([sys.executable, '-I', os.path.join(TOOLS, 'field_report.py'), *a], capture_output=True, text=True)
    p = run('--dir', root, '--team', team, '--ids', '7,9')
    check(p.returncode == 0 and '== all ==' in p.stdout, f'field_report CLI {p.returncode} {p.stderr[-300:]}')
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


TH = 'round id team type x y dir hp cheese mcd tcd acd bc coop ind seq src dead carrier'.split()


def mirror_tests(tmp):
    rot = {d: (d + 3) % 8 + 1 for d in range(1, 9)}
    check(all(MC.mirror_dir(d, 0) == rot[d] for d in rot) and MC.mirror_dir(0, 0) == 0, 'mirror_dir rotation = (d+3)%8+1')
    check([MC.mirror_dir(d, 1) for d in range(9)] == [0, 1, 8, 7, 6, 5, 4, 3, 2], 'mirror_dir horizontal (y flipped)')
    check([MC.mirror_dir(d, 2) for d in range(9)] == [0, 5, 4, 3, 2, 1, 8, 7, 6], 'mirror_dir vertical (x flipped)')
    check(all(MC.mirror_dir(MC.mirror_dir(d, s), s) == d for s in (0, 1, 2) for d in range(9)), 'mirror_dir is an involution')
    W, H = 9, 7
    for sym in (0, 1, 2):
        def game(d, swap, poke=None):
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
                cx = rn if not (poke and poke == (rn, 0, 0)) else rn + 1
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
        res, _ = MC.check(x, game(os.path.join(tmp, f'm{sym}w'), True, poke=(4, 0, 0)))
        check(list(res.values()) == [None, None, 4], f'mirror sym {sym}: cats compared by id, unmirrored {res}')
    try:
        MC.check(os.path.join(tmp, 'm0x'), os.path.join(tmp, 'm1y'))
        check(False, 'mirror: different symmetry refused')
    except ValueError:
        pass
    a, b = (os.path.join(REPO, 'build', 'diag', d) for d in ('fos2', 'fos3'))
    if os.path.isdir(a) and os.path.isdir(b):
        fa, fb = (os.path.join(d, sorted(f for f in os.listdir(d) if f.endswith('.bc26'))[0]) for d in (a, b))
        res, lines = MC.check(GR.dump(fa), GR.dump(fb))
        check(list(res.values()) == [24, 106, 54], f'mirror fos2/fos3: ours r24, opponent r106, cats r54 {res}')
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
