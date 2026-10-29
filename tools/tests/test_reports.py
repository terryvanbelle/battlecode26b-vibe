#!/usr/bin/env python3
"""The replay reports (game_report, smuggle, king_report, econ_report) and game_report's dump cache, on synthetic tsv
dirs and on the dumps of test/fixtures/throws.bc26 and king.bc26 (made through the cache in a temp BC_DUMPCACHE)."""
import collections, contextlib, io, json, os, shutil, subprocess, sys, tempfile, time
REPO = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
sys.path.insert(0, os.path.join(REPO, 'tools'))
CACHE = tempfile.mkdtemp(prefix='test-reports-')
os.environ['BC_DUMPCACHE'] = CACHE
import game_report as GR, smuggle as SG, king_report as KR, econ_report as ER, score  # noqa: E402
FX = os.path.join(REPO, 'test', 'fixtures', 'throws.bc26')
KING = os.path.join(REPO, 'test', 'fixtures', 'king.bc26')
fails = checks = 0


def check(ok, what):
    global fails, checks
    checks += 1
    if not ok:
        fails += 1; print('FAIL', what)


def write(d, name, head, rows):
    with open(os.path.join(d, name), 'w') as f:
        f.write('\t'.join(head.split()) + '\n' + ''.join('\t'.join(str(v) for v in r) + '\n' for r in rows))


def run(mod, *args):
    """(exit code, stdout, stderr) of a report's main()."""
    out, err = io.StringIO(), io.StringIO()
    code = 0
    with contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
        try:
            code = mod.main(list(args)) or 0
        except SystemExit as e:
            code = e.code if isinstance(e.code, int) else 1
            if not isinstance(e.code, int):
                err.write(str(e.code))
    return code, out.getvalue(), err.getvalue()


def entries():
    return sorted(os.listdir(CACHE))


try:
    # ---- the dump cache: content key, reuse, read-only, invalidation by the dumper hash, errors, races
    d = GR.dump(FX)
    check(os.path.dirname(d) == CACHE and os.path.basename(d).startswith(GR.file_sha(FX)[:20] + '-')
          and os.path.exists(os.path.join(d, '.stamp')) and os.path.exists(os.path.join(d, 'g0.summary.json')),
          f'dump: a complete dir keyed on the replay sha {d} {os.listdir(d)}')
    ino = os.stat(d).st_ino
    with tempfile.TemporaryDirectory() as t:
        cp = os.path.join(t, 'copy.bc26'); shutil.copy(FX, cp)
        check(GR.dump(cp) == d and GR.dump(FX) == d and os.stat(d).st_ino == ino and entries() == [os.path.basename(d)],
              f'dump: same content -> the same dir, reused, nothing else made {entries()}')
    shutil.rmtree(d, ignore_errors=True)
    check(os.path.exists(os.path.join(d, 'g0.turns.tsv')), 'dump: a caller rmtree(ignore_errors) removes nothing')
    d = GR.dump(FX)
    real = GR.dumper_hash
    GR.dumper_hash = lambda tools: 'f' * 40           # a rebuilt dumper
    try:
        d2 = GR.dump(FX)
        check(d2 != d and os.path.isdir(d) and d2.endswith('-' + 'f' * 12), f'dump: a new dumper hash -> a new dir {d2}')
        os.utime(d, (time.time() - 2 * GR.STALE_AGE,) * 2)
        GR.dump(FX)
        check(not os.path.exists(d) and os.path.isdir(d2), 'dump: an older dumper\'s dump is swept after STALE_AGE')
    finally:
        GR.dumper_hash = real
    d = GR.dump(FX)
    check(len(GR.dumper_hash(os.path.realpath(GR.TOOLS))) == 40 and os.path.basename(d).split('-')[1]
          == GR.dumper_hash(os.path.realpath(GR.TOOLS))[:12], 'dump: key = replay sha + ReplayDump class hash')
    with tempfile.TemporaryDirectory() as t:
        bad = os.path.join(t, 'bad.bc26')
        open(bad, 'wb').write(b'not a replay')
        before = entries()
        try:
            GR.dump(bad); msg = ''
        except GR.DumpError as e:
            msg = str(e)
        check('ReplayDump failed' in msg and ('Exception' in msg or 'Error' in msg) and entries() == before,
              f'dump: a failure raises with ReplayDump\'s stderr and leaves nothing {msg[:200]!r} {entries()}')
        code, out, err = run(GR, bad)
        check(code == 1 and 'ReplayDump failed' in err and entries() == before, f'game_report: exit 1 + stderr {code} {err[:100]!r}')
        code, out, err = run(GR, os.path.join(t, 'missing.bc26'))
        check(code == 1 and 'missing.bc26' in err, f'game_report: a missing replay {code} {err!r}')
        # two processes dump the same new replay at once: one dir, no temp dir left
        cp = os.path.join(t, 'race.bc26'); shutil.copy(KING, cp)
        code = f'import sys; sys.path.insert(0, {os.path.join(REPO, "tools")!r}); import game_report as G; print(G.dump({cp!r}))'
        ps = [subprocess.Popen([sys.executable, '-I', '-c', code], stdout=subprocess.PIPE, text=True) for _ in range(2)]
        outs = [p.communicate()[0].strip() for p in ps]
        check(all(p.returncode == 0 for p in ps) and outs[0] == outs[1] and os.path.isdir(outs[0])
              and not [e for e in entries() if e.startswith('.tmp-')], f'dump: concurrent dumps agree {outs} {entries()}')
    dk = GR.dump(KING)
    check(GR.source(d) == d, 'source: a directory is used as it is')

    # ---- rows / own / bank / pick
    with tempfile.TemporaryDirectory() as t:
        write(t, 'x.tsv', 'a b', [('1', 'x\ry'), ('2', 'z')])
        rs = list(GR.rows(os.path.join(t, 'x.tsv')))
        check(rs == [{'a': '1', 'b': 'x\ry'}, {'a': '2', 'b': 'z'}], f'rows: a \\r does not end a row {rs}')
        try:
            list(GR.rows(os.path.join(t, 'x.tsv'), need=('a', 'c'))); ok = False
        except ValueError as e:
            ok = 'c' in str(e)
        check(ok, 'rows: a missing required column raises')
    t2 = [{'round': '5', 'id': '9', 'src': 'own', 'bc': '1'}, {'round': '5', 'id': '9', 'src': 'carried', 'bc': '2'},
          {'round': '5', 'id': '8', 'src': 'split', 'bc': '3'}, {'round': '5', 'id': '8', 'src': 'own', 'bc': '4'}]
    check([r['bc'] for r in GR.own(t2)] == ['1', '4'], 'own: the src=own record, wherever it is')
    check([r['bc'] for r in GR.own([{k: v for k, v in r.items() if k != 'src'} for r in t2])] == ['2', '4'],
          'own: an old dump -> the last row of each (round, id)')
    check(GR.bank({'kingsA': '2051'}, 'A') == (205, 1) and GR.bank({'kingsA': '2051', 'bankA': '7', 'numKingsA': '2'}, 'A') == (7, 2),
          'bank: new columns, else decoded')

    # ---- game_report on the fixtures: summary result, own records, deaths by cause, merges excluded
    s = GR.summary(d, 0)
    g = GR.analyse(d, 0)
    check(g['winner'] == s['winner'] == 'B' and g['winType'] == 'RATKING_DESTROYED' and g['pts'] == list(score.points(s))
          and g['backstabRound'] == s['backstabRound'], f'game_report: the summary result {g["winner"]} {g["pts"]}')
    for dd in (d, dk):
        s, g = GR.summary(dd, 0), GR.analyse(dd, 0)
        for n in 'AB':
            rat = sum(v for k, v in g[n]['deaths'].items() if k.startswith('RAT:'))
            check(g[n]['ratTurns'] == s[n]['turnsRat'] and rat == s[n]['deathsRat']
                  and sum(v for k, v in g[n]['deaths'].items() if k.startswith('KING:')) == s[n]['deathsKing']
                  and not any(k.endswith(':merge') for k in g[n]['deaths']),
                  f'game_report {os.path.basename(dd)} {n}: rat turns = own records, deaths = summary {g[n]["ratTurns"]} {rat} {s[n]}')
    tl = GR.timeline(d, 40)
    own0 = GR.own(GR.rows(os.path.join(d, 'g0.turns.tsv')))
    check(tl[0][1] == sum(1 for r in own0 if r['round'] == '40' and r['team'] == '1' and r['type'] == 'RAT'), f'timeline own rows {tl[0]}')
    # every game of a multi-game dir; --game picks one; a missing game exits 2
    with tempfile.TemporaryDirectory() as t:
        for f in os.listdir(d):
            if f.startswith('g0.'):
                shutil.copy(os.path.join(d, f), os.path.join(t, f))
                shutil.copy(os.path.join(dk, f), os.path.join(t, 'g1.' + f[3:]))
        code, out, _ = run(GR, t)
        check(code == 0 and out.count('\ngame ') + out.startswith('game ') == 2 and 'game 1 cheesefarm' in out
              and 'winner B' in out, f'game_report: all games {out[:300]!r}')
        code, out, _ = run(GR, t, '--game', '1', '--json')
        check(code == 0 and [json.loads(l)['game'] for l in out.splitlines()] == [1], 'game_report --game 1 --json')
        code, out, err = run(GR, t, '--game', '3')
        check(code == 2 and 'games are [0, 1]' in err, f'game_report --game 3 {code} {err!r}')
        code, out, _ = run(GR, t, '--timeline', '50')
        check(code == 0 and out.count('round  ratsA') == 2, 'game_report --timeline: every game')
        code, out, _ = run(SG, t)
        check(code == 0 and 'game 0 (DefaultSmall)' in out and 'game 1 (cheesefarm)' in out and 'winner B' in out, f'smuggle all games {out[:200]!r}')
        code, out, _ = run(KR, t, '--game', '1')
        check(code == 0 and 'game 1 cheesefarm: winner B' in out and 'g0 ' not in out, f'king_report --game {out[:200]!r}')
        code, out, _ = run(ER, t)
        check(code == 0 and 'game 0 DefaultSmall' in out and 'game 1 cheesefarm' in out and 'ledger' in out, 'econ_report: all games')
        code, out, err = run(ER, t, '--game', '4')
        check(code == 2, 'econ_report --game 4 exits 2')

    # ---- smuggle: the own record is the real one even when it is not the last row
    rr = [{'round': '1', 'id': '5', 'team': '1', 'type': 'RAT', 'bc': '100', 'src': 'own', 'dead': '0'},
          {'round': '1', 'id': '5', 'team': '1', 'type': 'RAT', 'bc': '100', 'src': 'carried', 'dead': '0'},
          {'round': '1', 'id': '6', 'team': '2', 'type': 'RAT', 'bc': '7', 'src': 'own', 'dead': '1'}]
    check(SG.robot_rounds(rr) == [rr[0], rr[2]] and SG.usable(SG.robot_rounds(rr)) == [rr[0]],
          'smuggle: src=own rows; a dead own row is not a tag')
    check(len(SG.robot_rounds(GR.rows(os.path.join(d, 'g0.turns.tsv'))))
          == sum(1 for r in own0 if r['team'] in ('1', '2')), 'smuggle: one row per own record on the fixture')

    # ---- king_report: hp by source; starvation is exactly 10 a starving turn; fixtures agree with the summary
    for dd in (d, dk):
        s, ks = GR.summary(dd, 0), KR.analyse(dd, 0)
        for n, op in (('A', 'B'), ('B', 'A')):
            mine = [o for o in ks if o['side'] == n]
            enemy = sum(v for o in mine for k, v in o['hpBy'].items() if k in ('bite', 'biteBoost', 'throw', 'trap'))
            check(all(o['hpBy'].get('other', 0) == 0 for o in mine) and enemy == s[op]['dmgKing']
                  and sum(o['starveHp'] for o in mine) == s[n]['starveDmg'],
                  f'king_report {os.path.basename(dd)} {n}: hp by enemy sources = summary dmgKing {enemy} {s[op]["dmgKing"]}')
    ko = {o['side']: o for o in KR.analyse(d, 0)}
    check(ko['A']['died'] == 138 and ko['A']['deathCause'] == 'bite' and ko['A']['winner'] == 'B' and ko['B']['hpEnd'] == 600,
          f'king_report fixture {ko["A"]["died"]} {ko["A"]["deathCause"]}')
    with tempfile.TemporaryDirectory() as t:
        json.dump({'map': 'M', 'teamA': 'a', 'teamB': 'b'}, open(os.path.join(t, 'g0.meta.json'), 'w'))
        write(t, 'g0.rounds.tsv', 'round kingsA kingsB bankA bankB numKingsA numKingsB',
              [(1, 101, 101, 10, 10, 1, 1)] + [(rn, 1, 101, 0, 10, 1, 1) for rn in range(2, 6)])
        tt = 'round id team type x y hp seq src dead'
        write(t, 'g0.turns.tsv', tt, [(1, 1, 1, 'KING', 0, 0, 600, 0, 'own', 0), (1, 2, 2, 'KING', 9, 9, 600, 1, 'own', 0),
                                      (2, 7, 2, 'RAT', 1, 1, 100, 0, 'own', 0), (2, 1, 1, 'KING', 0, 0, 570, 1, 'own', 0),
                                      (2, 2, 2, 'KING', 9, 9, 582, 2, 'own', 0), (3, 1, 1, 'KING', 0, 0, 550, 0, 'own', 0),
                                      (4, 1, 1, 'KING', 0, 0, 527, 0, 'own', 0), (5, 1, 1, 'KING', 0, 0, 0, 0, 'own', 1)])
        write(t, 'g0.actions.tsv', 'round id team type action a b c seq src d e f g',
              [(2, 7, 2, 'RAT', 'bite', 1, 'KING', 1, 0, 'own', 10, 0, '', ''),        # before king 1's record (seq 1)
               (2, 7, 2, 'RAT', 'throwHit', 1, 10, 'KING', 0, 'own', 1, 12, 7, 2),
               (2, 2, 2, 'KING', 'trigger', '9,9', 2, '', 2, 'own', 1, 2, 15, 'rat'),   # A's trap on B's king
               (3, 4, 0, 'CAT', 'scratch', '0,0', '', '', 0, 'own', 1, 1, 20, ''),
               (4, 7, 2, 'RAT', 'bite', 1, 'KING', 1, 0, 'own', 10, 0, '', ''),
               (5, 1, 1, 'KING', 'die', 1, 'damage', 'KING', 0, 'own', 1, 'starve', 0, 0)])
        ko = {o['side']: o for o in KR.analyse(t, 0)}
        a, b = ko['A'], ko['B']
        # A r2: 600 -> 570 = bite 10 + throw 10 + starvation 10; r3: scratch 20, no starvation (rest 0) although the
        # bank is 0; r4: 550 -> 527 = bite 10 + starvation 10 + the bite's cheese boost 3; r5 starved to death from 527
        # (its dead record is skipped)
        check(a['hpBy'] == {'bite': 20, 'throw': 10, 'scratch': 20, 'biteBoost': 3} and a['starveHp'] == 20 + 527
              and a['otherHp'] == 20 + 20 + 13 and a['cause'] == {'bites': 2, 'throwHits': 1, 'scratches': 1}
              and a['starved'] == [2, 4] and a['died'] == 5 and a['deathCause'] == 'starve', f'king_report attribution {a}')
        # B: 600 -> 582: trap 15 + 3 hp nobody explains (bank 10: no starvation)
        check(b['hpBy'] == {'trap': 15, 'other': 3} and b['starveHp'] == 0 and b['otherHp'] == 18, f'king_report trap/other {b}')

    # ---- econ_report: v2 rows (kind, amounts), partial pickups, the old tile model, the mirror check, the ledger
    with tempfile.TemporaryDirectory() as t:
        meta = {'map': 'M', 'teamA': 'a', 'teamB': 'b', 'width': 20, 'height': 10, 'symmetry': 2,
                'mines': [[3, 5], [16, 5]], 'bodies': [{'id': 1, 'team': 1, 'type': 'KING', 'x': 1, 'y': 5},
                                                      {'id': 2, 'team': 2, 'type': 'KING', 'x': 18, 'y': 5}]}
        json.dump(meta, open(os.path.join(t, 'g0.meta.json'), 'w'))
        write(t, 'g0.turns.tsv', 'round id team type x y src dead', [(5, 9, 1, 'RAT', 4, 5, 'own', 0), (5, 9, 1, 'RAT', 4, 5, 'carried', 0)])
        AH = 'round id team type action a b c seq src d e f g'
        v2 = [(3, 0, 0, 'NONE', 'cheesespawn', '4,5', 20, '', 0, 'own', 'mine', 20, 20, ''),
              (3, 0, 0, 'NONE', 'cheesespawn', '15,5', 20, '', 0, 'own', 'mine', 20, 20, ''),
              (4, 0, 0, 'NONE', 'cheesespawn', '4,5', 40, '', 0, 'own', 'mine', 40, 20, ''),
              (6, 9, 1, 'RAT', 'pickup', '4,5', 15, '', 3, 'own', '', '', '', ''),
              (6, 9, 1, 'RAT', 'cheesespawn', '4,5', 25, '', 3, 'own', 'remainder', 25, -15, ''),
              (8, 2, 2, 'KING', 'kingPickup', 2, 25, '', 1, 'own', '', '', '', ''),
              (8, 2, 2, 'KING', 'pickup', '4,5', 25, '', 1, 'own', '', '', '', ''),
              (9, 8, 2, 'RAT', 'cheesespawn', '10,2', 7, '', 2, 'own', 'drop', 7, 7, ''),
              (9, 8, 2, 'RAT', 'die', 8, 'damage', 'RAT', 2, 'own', 2, 'bite', 9, 1),
              (10, 9, 1, 'RAT', 'pickup', '10,2', 7, '', 0, 'own', '', '', '', '')]
        write(t, 'g0.actions.tsv', AH, v2)
        err = io.StringIO()
        with contextlib.redirect_stderr(err):
            o = ER.analyse(t)
        m1, m2 = o['mines']
        check(err.getvalue() == '' and m1['spawned'] == 40 and m1['spawns'] == 2 and m1['gotA'] == 15 and m1['gotB'] == 25
              and m1['left'] == 0 and m2['left'] == 20, f'econ v2: partial pickup takes its amount, the rest stays {m1} {m2}')
        check(o['A']['picked'] == 22 and o['A']['drops'] == 7 and o['B']['picked'] == 25 and o['B']['pickups'] == 1
              and o['ledger'] is None and (m1['ratsA'], m2['ratsA']) == (1, 0),
              f'econ v2: kingPickup not counted twice; own records only {o["A"]} {o["B"]}')
        # the same game as an old dump: no kinds, no amounts (a mine row gives the tile total, a drop its amount, a
        # partial pickup the remainder right after it); the remainder is that pickup's, not a new pile
        write(t, 'g0.actions.tsv', 'round id team type action a b c', [
            (3, 1, 0, 'CAT', 'cheesespawn', '4,5', 20, ''), (3, 1, 0, 'CAT', 'cheesespawn', '15,5', 20, ''),
            (4, 1, 0, 'CAT', 'cheesespawn', '4,5', 40, ''), (6, 9, 1, 'RAT', 'pickup', '4,5', '', ''),
            (6, 9, 1, 'RAT', 'cheesespawn', '4,5', 25, ''), (8, 2, 2, 'KING', 'transfer', 2, 25, ''),
            (8, 2, 2, 'KING', 'pickup', '4,5', '', ''), (9, 8, 2, 'RAT', 'cheesespawn', '10,2', 7, ''),
            (9, 8, 2, 'RAT', 'die', 8, 'damage', 'RAT'), (10, 9, 1, 'RAT', 'pickup', '10,2', '', '')])
        o2 = ER.analyse(t)
        check([(m['spawned'], m['gotA'], m['gotB'], m['left']) for m in o2['mines']] == [(40, 15, 25, 0), (20, 0, 0, 20)]
              and o2['A']['picked'] == 22 and o2['B']['picked'] == 25, f'econ old dump: rebuilt tile model {o2["mines"]} {o2["A"]}')
        meta['mines'] = [[3, 5]]
        json.dump(meta, open(os.path.join(t, 'g0.meta.json'), 'w'))
        with contextlib.redirect_stderr(err):
            o3 = ER.analyse(t)
        check('without their symmetric partner' in err.getvalue() and [m['mine'] for m in o3['mines']] == [[3, 5], [16, 5]],
              'econ: a lone mine is reported and its partner added')
    # the ledger on a hand-made game, one window a round: r2 A spawns (4 babies: cost 20), B's king starves (no
    # upkeep), a B rat dies dropping 30; r3 A forms a king (-50, its turn eats 2) and takes 15 from a B rat; r4 an A rat
    # picks 20 and builds a rat trap, A's king digs, A has 2 kings
    with tempfile.TemporaryDirectory() as t:
        json.dump(meta, open(os.path.join(t, 'g0.meta.json'), 'w'))
        write(t, 'g0.rounds.tsv', 'round bankA bankB numKingsA numKingsB rawCheeseA rawCheeseB pickedA pickedB engBabiesA engBabiesB',
              [(1, 100, 1, 1, 1, 0, 45, 0, 0, 4, 2), (2, 78, 1, 1, 1, 0, 15, 0, 0, 5, 1), (3, 39, 1, 2, 1, 0, 0, 0, 0, 5, 0),
               (4, 30, 1, 2, 1, 0, 0, 20, 0, 5, 0)])
        K = lambda rn, i, tm, hp, seq: (rn, i, tm, 'KING', 1, 1, hp, 0, seq, 'own', 0)
        Rt = lambda rn, i, tm, c, seq: (rn, i, tm, 'RAT', 3, 3, 100, c, seq, 'own', 0)
        write(t, 'g0.turns.tsv', 'round id team type x y hp cheese seq src dead',
              [K(1, 1, 1, 600, 0), K(1, 2, 2, 600, 1), Rt(1, 6, 2, 30, 2), Rt(1, 7, 2, 15, 3), Rt(1, 5, 1, 0, 4),
               K(2, 1, 1, 600, 0), K(2, 2, 2, 590, 1), Rt(2, 7, 2, 15, 2), Rt(2, 5, 1, 0, 3),
               K(3, 1, 1, 600, 0), K(3, 2, 2, 580, 1), Rt(3, 5, 1, 0, 2),
               K(4, 1, 1, 600, 0), K(4, 2, 2, 570, 1), K(4, 5, 1, 600, 2), Rt(4, 8, 1, 0, 3)])
        A = lambda rn, i, tm, ty, act, a, b, c, seq, *x: (rn, i, tm, ty, act, a, b, c, seq, 'own') + tuple(x) + ('',) * (4 - len(x))
        write(t, 'g0.actions.tsv', AH, [
            A(2, 1, 1, 'KING', 'spawn', 8, 'RAT', '1,1', 0), A(2, 5, 1, 'RAT', 'bite', 6, 'RAT', 2, 3, 10, 1),
            A(2, 5, 1, 'RAT', 'cheesespawn', '3,3', 30, '', 3, 'drop', 30, 30), A(2, 5, 1, 'RAT', 'die', 6, 'damage', 'RAT', 3, 2, 'bite', 5, 1),
            A(3, 5, 1, 'RAT', 'die', 7, 'damage', 'RAT', 2, 2, 'form', 5, 1), A(3, 5, 1, 'RAT', 'upgradeKing', 5, '', '', 2, 0, 1),
            A(4, 1, 1, 'KING', 'dig', '2,2', '', '', 0), A(4, 8, 1, 'RAT', 'pickup', '3,3', 20, '', 3),
            A(4, 8, 1, 'RAT', 'ratTrap', '4,5', 1, '', 3)])
        led = {w['lo']: w for w in ER.ledger(t, 0, None, 1)}
        check(all(led[rn][n]['rest'] == 0 for rn in (2, 3, 4) for n in 'AB'), f'ledger: every term explained {led}')
        pick = lambda rn, n, *ks: tuple(led[rn][n][k] for k in ks)
        check(pick(2, 'A', 'spawns', 'spawnCost', 'upkeep') == (1, 20, 2) and pick(2, 'B', 'dropped', 'upkeep') == (30, 0)
              and pick(3, 'A', 'absorbed', 'formations', 'upkeep') == (15, 50, 4) and pick(3, 'B', 'lostToForm', 'upkeep') == (15, 0)
              and pick(4, 'A', 'picked', 'traps', 'digs', 'upkeep') == (20, 20, 5, 4), f'ledger terms {led}')
    for dd in (d, dk):
        s = GR.summary(dd, 0)
        o = ER.analyse(dd, 0, None, 10)
        rnd = list(GR.rows(os.path.join(dd, 'g0.rounds.tsv')))
        for n in 'AB':
            check(o[n]['picked'] == s[n]['pickupAmt'] == sum(int(r['picked' + n]) for r in rnd)
                  and sum(m['got' + n] for m in o['mines']) + o[n]['drops'] == o[n]['picked'],
                  f'econ {os.path.basename(dd)} {n}: picked = summary pickupAmt = rounds picked, all sourced')
        bites = collections.Counter(((int(r['round']) - 1) // 10, r['team']) for r in GR.rows(os.path.join(dd, 'g0.actions.tsv'))
                                    if r['action'] == 'bite')
        led = o['ledger']
        check(led and led[0]['A']['start'] == ER.START and led[-1]['hi'] == s['rounds'] and all(
            w[n]['rest'] == 0 or w[n]['rest'] < 0 and bites[((w['lo'] - 1) // 10, t)]
            for w in led for n, t in (('A', '1'), ('B', '2'))),
            f'econ ledger {os.path.basename(dd)}: rest only where the team bit (cheese boost) {led}')
        check(all(led[i]['A']['end'] == led[i + 1]['A']['start'] for i in range(len(led) - 1)), 'econ ledger windows chain')
    led = ER.analyse(dk, 0)['ledger'][0]['B']
    check(led['formations'] == 50 and led['spawns'] == GR.summary(dk, 0)['B']['spawned'], f'econ ledger formation/spawns {led}')
    code, out, _ = run(ER, '--team', 'Version41', FX, KING)
    check(code == 0 and '2 games' in out and 'ledger, summed' in out, f'econ --team {out[:200]!r}')
finally:
    for e in os.listdir(CACHE):
        GR._rm(os.path.join(CACHE, e))
    shutil.rmtree(CACHE, ignore_errors=True)

print(f'reports: {checks - fails}/{checks} passed')
sys.exit(1 if fails else 0)
