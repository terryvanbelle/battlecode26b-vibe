#!/usr/bin/env python3
"""battle_report.py: a synthetic two-game tsv dump (duplicate/carried/dead records, a moving king, killing blows in
another robot's turn, a bite boost, starvation, traps, cat kills, a grab and drop, a grab then a fatal point-blank
throw, spawns and later-in-the-round deaths at the front, round windows, match.json lookup), the CLI over every game,
an empty window, non-dump dirs and a match dir whose cached dump was made from another copy of the replay, and the
fixtures throws.bc26 and king.bc26 (built with tools/build-tools.sh; fails, never skips) against the dump's summary
and rounds.tsv. Dumps go to temp dirs, never build/dumpcache. Usage: test_battle.py"""
import importlib.util, json, os, shutil, subprocess, sys, tempfile
REPO = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
JDK = os.environ.get('BC_JDK', os.path.expanduser('~/opt/jdk21'))
CP = os.path.join(REPO, 'build', 'tools') + ':' + os.path.join(REPO, 'engine', 'battlecode26-java-1.2.6.jar')
FIXTURES = [os.path.join(REPO, 'test', 'fixtures', f) for f in ('throws.bc26', 'king.bc26')]
fails = checks = 0


def check(ok, what):
    global fails, checks
    checks += 1
    if not ok:
        fails += 1; print('FAIL', what)


sys.path.insert(0, os.path.join(REPO, 'tools'))
spec = importlib.util.spec_from_file_location('battle_report', os.path.join(REPO, 'tools', 'battle_report.py'))
br = importlib.util.module_from_spec(spec); spec.loader.exec_module(br)

TH = 'round id team type x y dir hp cheese mcd tcd acd bc coop ind seq src dead carrier'.split()
AH = 'round id team type action a b c seq src d e f g'.split()


def synth(d, games=2):
    T, A, seq = [], [], {}

    def rec(r, i, tm, ty, x, y, hp, acts=(), src='own', carrier=0):
        s = seq[r] = seq.get(r, -1) + 1
        for a in acts:
            a = tuple(a) + ('',) * (8 - len(a))
            A.append([r, i, tm, ty, a[0], *a[1:4], s, src, *a[4:8]])
        T.append([r, i, tm, ty, x, y, 0, hp, 0, 0, 0, 0, 1000, 0, '', s, src, int(hp <= 0), carrier])

    rec(1, 1, 1, 'KING', 5, 5, 500, [('spawn', 10, 'RAT', '6,6'), ('spawn', 11, 'RAT', '6,7')])
    rec(1, 2, 2, 'KING', 20, 20, 500, [('spawn', 20, 'RAT', '19,19'), ('spawn', 21, 'RAT', '19,18')])
    rec(1, 3, 0, 'CAT', 10, 10, 1000)
    rec(2, 1, 1, 'KING', 5, 5, 500)
    rec(2, 10, 1, 'RAT', 8, 8, 100, [('ratTrap', '12,12', 1), ('ratTrap', '13,13', 1), ('ratTrap', '14,14', 1)])
    rec(2, 11, 1, 'RAT', 9, 9, 100)
    rec(2, 20, 2, 'RAT', 30, 30, 100, src='split')               # stale mid-turn record: not the robot-round
    rec(2, 20, 2, 'RAT', 10, 9, 100)
    rec(2, 21, 2, 'RAT', 10, 9, 100, src='carried', carrier=20)
    rec(2, 2, 2, 'KING', 20, 20, 500)
    rec(3, 1, 1, 'KING', 8, 2, 500, [('removeTrap', '13,13', 1)])  # the king moved: dK from this round's tile
    rec(3, 10, 1, 'RAT', 8, 8, 100, [('bite', 20, 'RAT', 2, 10, 0)])
    rec(3, 20, 2, 'RAT', 10, 9, 85)                              # 10 nominal + 5 cheese boost
    rec(3, 11, 1, 'RAT', 9, 9, 100)                              # alive in its record, killed later this round
    rec(3, 3, 0, 'CAT', 9, 8, 1000, [('die', 11, 'damage', 'RAT', 1, 'pounce', 3, 0)])  # rat 11 (100 hp) never wrote again
    rec(3, 2, 2, 'KING', 20, 20, 490)                            # starvation: no row
    rec(3, 21, 2, 'RAT', 12, 12, 50, [('trigger', '12,12', 2, '', 1, 21, 50, 'rat')])
    rec(4, 10, 1, 'RAT', 8, 8, 100, [('bite', 21, 'RAT', 2, 50, 1), ('die', 21, 'damage', 'RAT', 2, 'bite', 10, 1)])
    rec(4, 21, 2, 'RAT', 12, 12, 0, src='thrown')                # a dead record: not alive
    rec(4, 20, 2, 'RAT', 10, 9, 85, [('catTrap', '15,15', 2), ('pickup', '1,1', 5), ('transfer', 2, 5)])
    rec(4, 3, 0, 'CAT', 15, 15, 900, [('trigger', '15,15', 0, '', 2, 3, 100, 'cat')])
    rec(4, 2, 2, 'KING', 20, 20, 490, [('kingPickup', 2, 7)])
    rec(5, 10, 1, 'RAT', 9, 9, 100, [('nap', 20, 2, '', 'enemy')])
    rec(5, 20, 2, 'RAT', 9, 9, 85, carrier=10)
    rec(6, 1, 1, 'KING', 8, 2, 500, [('spawn', 12, 'RAT', '9,10')])  # no record this round: full hp at the front
    rec(6, 10, 1, 'RAT', 9, 9, 100, [('drop', 20, 2, 'carrier', 10, 1)])
    rec(6, 20, 2, 'RAT', 9, 8, 85)
    rec(7, 10, 1, 'RAT', 8, 9, 100)
    rec(7, 12, 1, 'RAT', 9, 10, 100)
    rec(7, 20, 2, 'RAT', 9, 9, 85, [('bite', 12, 'RAT', 1, 60, 0), ('nap', 12, 1, '', 'enemy')])
    # a point-blank fatal throw: the thrown record (die, damage, land) precedes the thrower's own (the throw row)
    rec(8, 12, 1, 'RAT', 9, 8, 0, [('die', 12, 'damage', 'RAT', 1, 'throwLand', 20, 2), ('damage', 12, 42, 'RAT', 'ground', 20, 2),
                                   ('land', 12, 'ground', 1, 20, 2)], src='thrown')
    rec(8, 20, 2, 'RAT', 9, 9, 85, [('throw', 12, '9,8')])
    rec(8, 10, 1, 'RAT', 8, 9, 100)
    meta = dict(map='Synth', teamA='a', teamB='b', types={'RAT': {'health': 100}, 'KING': {'health': 500}, 'CAT': {'health': 1000}},
                bodies=[dict(id=1, team=1, type='KING'), dict(id=2, team=2, type='KING'), dict(id=3, team=0, type='CAT')])
    m = os.path.join(d, 'm')                                     # game 1's replay (an absolute meta file) and match.json
    os.makedirs(m)
    json.dump({'games': [{'map': 'Synth0', 'reversed': True}, {'map': 'Synth1', 'reversed': True}]}, open(os.path.join(m, 'match.json'), 'w'))
    for g in range(games):
        p = os.path.join(d, f'g{g}.')
        json.dump(dict(meta, map=f'Synth{g}', file=os.path.join(m, 'replay.bc26') if g == 1 else ''), open(p + 'meta.json', 'w'))
        json.dump(dict(rounds=8, winner='B', winType='MORE_POINTS'), open(p + 'summary.json', 'w'))
        for name, h, rows in (('turns', TH, T), ('actions', AH, A)):
            with open(p + name + '.tsv', 'w') as f:
                f.write('\t'.join(h) + '\n' + ''.join('\t'.join(map(str, r)) + '\n' for r in rows))
        with open(p + 'rounds.tsv', 'w') as f:
            f.write('round\tcatDmgA\tcatDmgB\tbabiesA\tbabiesB\n'
                    + ''.join(f'{r}\t0\t{130 if r >= 4 else 0}\t{BABIES[r][0]}\t{BABIES[r][1]}\n' for r in range(1, 9)))


BABIES = {1: (2, 2), 2: (2, 2), 3: (1, 2), 4: (1, 1), 5: (1, 1), 6: (2, 1), 7: (2, 1), 8: (1, 1)}   # end of each round


with tempfile.TemporaryDirectory() as d:
    synth(d)
    check(br.games(d) == [0, 1], 'games')
    r = br.report(d, 0, 2000, 1, 0)
    check(r['hi'] == 8 and r['map'] == 'Synth0' and r['winner'] == 'B' and 'reversed' not in r, f"header {r['hi']} {r['map']}")
    check(br.report(d, 0, 2000, 1, 0, os.path.join(d, 'm', 'replay.bc26')).get('reversed') is True
          and br.report(d, 0, 2000, 1, 1).get('reversed') is True, "match.json beside the replay given, else the meta's absolute file")
    f = {x['round']: x for x in r['front']}
    check(f[2]['2'] == dict(n=2, hp=200, carried=1, front=1, dk=11.0), f"front: last record, carried not at the front {f[2]['2']}")
    check(f[2]['1']['front'] == 2 and f[2]['1']['dk'] == 3.5, f"front: enemy by its last record {f[2]['1']}")
    check(f[3]['1'] == dict(n=1, hp=100, carried=0, front=1, dk=6.0), f"front: dead rat gone, dK from the moved king {f[3]['1']}")
    check(f[4]['2']['n'] == 1, f"front: dead record skipped {f[4]['2']}")
    check(f[1]['1'] == dict(n=2, hp=200, carried=0, front=0, dk=1.5) and f[6]['1']['hp'] == 200, f"front: spawns {f[1]['1']} {f[6]['1']}")
    turns, acts = br.load(d, 0)
    rb = br.front(turns, acts, 1, 8, 1)
    check([(x['1']['n'], x['2']['n']) for x in rb] == [BABIES[k] for k in range(1, 9)], f'front: rebuilt alive set {rb}')
    check(br.front(turns, acts, 2, 2, 1, [dict(round='2', babiesA='5', babiesB='4')])[0]['1']['n'] == 5, 'front: n from babiesA/B')
    check(r['hploss']['2'] == {'bite': 65, 'trap': 50} and r['kingloss']['2'] == {'starve': 10}
          and r['hpby']['2'] == {'enemy': 115, 'starve': 10}, f"hploss: boost, killing bite, starvation {r['hploss']['2']} {r['kingloss']['2']} {r['hpby']['2']}")
    check(r['hploss']['1'] == {'pounce': 100, 'bite': 60, 'throwLand': 40} and r['hpby']['1'] == {'cat': 100, 'enemy': 100},
          f"hploss: pounce takes the hp left, landing the rest {r['hploss']['1']}")
    D = r['deaths']
    check(D['1']['rat'] == {'pounce': 1, 'throwLand': 1} and D['1']['by'] == {'cat': 1, 'enemy': 1} and D['1']['kd'] == 1.0,
          f"deaths: a cat kill is no enemy kill {D['1']}")
    check(D['2']['rat'] == {'bite': 1} and D['2']['by'] == {'enemy': 1} and D['1']['killsRat'] == 1 and D['2']['kd'] == 1.0,
          f"deaths: kd {D}")
    check(r['throwOut']['2'] == {'n': 1, 'enemyRat': 1, 'ground': 1, 'fatal': 1, 'kills': 1} and r['throws']['2'] == {42: 1},
          f"throws: a fatal point-blank throw {r['throwOut']['2']} {r['throws']['2']}")
    thr = [dict(round='5', id='7', team='1', type='RAT', action='throw', a='20', seq='4', src='own'),
           dict(round='5', id='20', team='2', type='RAT', action='land', a='20', b='target', c='0', seq='4', src='thrown'),
           dict(round='5', id='20', team='2', type='RAT', action='damage', a='20', b='42', seq='4', src='thrown'),
           dict(round='5', id='20', team='2', type='RAT', action='throwHit', a='30', b='42', c='KING', d='2', e='20', seq='4', src='thrown')]
    t1 = br.throws([dict(id='20', team='2')], thr, 0, 9)
    check(t1[1]['1'] == {'n': 1, 'enemyRat': 1, 'target': 1, 'fatal': 0, 'hitEnemy': 1, 'hitKing': 1} and t1[0]['1'] == {42: 1},
          f"throws: an enemy rat thrown into its own king {t1}")
    die = dict(round='5', id='9', team='1', type='RAT', action='die', b='damage', seq='3', src='thrown', d='2', e='throwHit', g='1')
    check(br.throws([], [dict(die, a='2', c='KING'), dict(die, a='25', c='RAT')], 0, 9)[1]['1'] == {'kills': 1, 'killsKing': 1},
          'throws: kings killed apart')
    t1, t2 = r['traps']['1'], r['traps']['2']
    check(t1['placed'] == {'rat': 3} and t1['fired'] == {'rat': 1} and t1['removed'] == 1 and t1['live'] == 1
          and t1['hitRate'] == 0.33 and t1['meanLife'] == 1.0 and t1['victims'] == {'2': 1} and t1['dmg'] == {'rat': 50}, f"traps A {t1}")
    check(t2['placed'] == {'cat': 1} and t2['catDmg'] == 130 and t2['catDmgTrap'] == 100 and t2['catDmgBite'] == 30, f"traps B {t2}")
    x = r['xfer'][0]
    check((x['xfer2'], x['kpick2'], x['pick2'], x['pickAmt2']) == (5, 7, 1, 5), f"xfer: king pickups apart {x}")
    g = r['grabs']['1']
    check(g['n'] == 1 and g['enemy'] == 1 and g['fate'] == {'drop:carrier': 1} and g['held'] == {'drop:carrier': 1.0}, f"grabs {g}")
    g = r['grabs']['2']
    check(g['n'] == 1 and g['fate'] == {'throw': 1} and g['held'] == {'throw': 1.0}, f"grabs: the die row before the throw row {g}")
    check(br.report(d, 4, 6, 1, 0)['deaths']['1']['rat'] == {} and br.report(d, 4, 4, 1, 0)['traps']['2']['catDmg'] == 130
          and br.report(d, 5, 6, 1, 0)['traps']['2']['catDmg'] == 0, 'window lo..hi')
    check([br.report(d, *w, 1, 0)['traps']['2']['catDmg'] for w in ((10, 2000), (5, 3))] == [0, 0], 'window past the end, lo > hi')
    t = br.report(d, 2, 2, 1, 0)['traps']['1']                  # placed in round 2: one fired in round 3, one removed
    check(t['placed'] == {'rat': 3} and (t['hit'], t['removed'], t['live'], t['hitRate'], t['meanLife']) == (1, 1, 1, 0.33, 1.0)
          and t['fired'] == {}, f'traps: hit rate over the traps placed in the window {t}')
    t = br.report(d, 3, 4, 1, 0)['traps']                       # team 1's traps were placed before: only its trigger counts
    check(t['1']['placed'] == {} and (t['1']['hit'], t['1']['removed'], t['1']['live'], t['1']['hitRate']) == (0, 0, 0, None)
          and t['1']['fired'] == {'rat': 1} and t['2']['placed'] == {'cat': 1} and t['2']['hit'] == 1, f'traps: placed before the window {t}')
    nap = dict(round='5', id='10', team='1', action='nap', a='20', src='own', d='enemy')
    hit = dict(round='6', id='12', team='1', action='die', a='20', src='thrown', d='2', e='throwHit')
    check(br.grabs([], [nap, hit], 0, 9)['1']['fate'] == {'died': 1}, "grabs: killed in another rat's thrown record: died")
    run = lambda *a, src=d, env=None: subprocess.run(  # noqa: E731
        [sys.executable, '-I', os.path.join(REPO, 'tools', 'battle_report.py'), src, *a], capture_output=True, text=True, env=env)
    p = run()
    check(p.returncode == 0 and [l.split()[1] for l in p.stdout.splitlines() if l.startswith('== ')] == ['g0', 'g1'],
          f'cli: every game {p.stderr[-300:]}')
    p = run('--game', '1', '--json')
    js = [json.loads(l) for l in p.stdout.splitlines()]
    check(p.returncode == 0 and len(js) == 1 and js[0]['game'] == 1 and js[0]['map'] == 'Synth1', 'cli: --game N --json')
    check(run('--game', '2').returncode != 0, 'cli: a missing game fails')
    p = run('--lo', '9')
    check(p.returncode == 0 and p.stdout.count('no rounds in the window') == 2 and len(p.stdout.splitlines()) == 4,
          f'cli: an empty window prints only the headers {p.stdout[-300:]}')
    os.makedirs(os.path.join(d, 'g1only'))
    for f in os.listdir(d):
        if f.startswith('g1.'):
            shutil.copy(os.path.join(d, f), os.path.join(d, 'g1only'))
    p = run(src=os.path.join(d, 'g1only'))
    check(p.returncode == 0 and [l.split()[1] for l in p.stdout.splitlines() if l.startswith('== ')] == ['g1'], 'cli: a dump without g0')
    p = run(src=os.path.join(d, 'm'))
    check(p.returncode != 0 and p.stdout == '', f'cli: a dir with no dump and no replay.bc26 fails {p.returncode}')

# fixtures: every section agrees with the dump's own summary
b = subprocess.run([os.path.join(REPO, 'tools', 'build-tools.sh')], capture_output=True, text=True)
check(b.returncode == 0, f'build-tools: {b.stderr[-500:]}')
with tempfile.TemporaryDirectory() as t:                         # a replica-style match dir, dumped into a temp cache
    m = os.path.join(t, 'm')
    os.makedirs(m)
    shutil.copy(FIXTURES[0], os.path.join(m, 'replay.bc26'))
    json.dump({'games': [{'map': 'DefaultSmall', 'reversed': True}]}, open(os.path.join(m, 'match.json'), 'w'))
    env = dict(os.environ, BC_DUMPCACHE=os.path.join(t, 'cache'))
    # dumped first from the fixture's path (no match.json there): the cached meta file is not the match dir's copy
    for src, rev in ((FIXTURES[0], None), (m, True), (os.path.join(m, 'replay.bc26'), True)):
        p = run('--json', src=src, env=env)
        js = [json.loads(l) for l in p.stdout.splitlines()] if p.returncode == 0 else []
        check(len(js) == 1 and js[0].get('reversed') is rev, f'cli: match.json beside the replay given {src} {p.stderr[-300:]}')
for fx in FIXTURES:
    with tempfile.TemporaryDirectory() as d:
        p = subprocess.run([os.path.join(JDK, 'bin', 'java'), '-cp', CP, 'bc26tools.ReplayDump', 'tsv', fx, d],
                           capture_output=True, text=True, timeout=300)
        check(p.returncode == 0 and br.games(d) == [0], f'dump {fx}: {p.stderr[-300:]}')
        if p.returncode:
            continue
        s = json.load(open(os.path.join(d, 'g0.summary.json')))
        r = br.report(d, 0, 2000, 50, 0)
        tag = os.path.basename(fx)
        for tm, n, o in (('1', 'A', 'B'), ('2', 'B', 'A')):
            lost = sum(r['hploss'][tm].values()) + sum(r['kingloss'][tm].values())
            exp = s[o]['dmgRat'] + s[o]['dmgKing'] + s[n]['catDmgTaken'] + s[n]['starveDmg'] + s[n]['friendlyDmg']
            check(lost == exp and 'other' not in r['hpby'][tm], f'{tag} {n} hp lost {lost} vs summary {exp} {r["hpby"][tm]}')
            check(r['hpby'][tm].get('cat', 0) == s[n]['catDmgTaken'] and r['hpby'][tm].get('own', 0) == s[n]['friendlyDmg'],
                  f'{tag} {n} hp by source')
            x = r['deaths'][tm]
            check(x['rat'] == s[n]['ratDeaths'] and x['king'] == s[n]['kingDeaths'] and x['merged'] == s[n]['ratsMerged']
                  and x['by'].get('enemy', 0) == s[n]['deathsByEnemy'] and x['by'].get('cat', 0) == s[n]['deathsByCat']
                  and (x['killsRat'], x['killsKing'], x['catsKilled']) == (s[n]['killsRat'], s[n]['killsKing'], s[n]['catsKilled']),
                  f'{tag} {n} deaths {x}')
            t = r['traps'][tm]
            check(t['catDmgTrap'] == s[n]['catDmgTrap'] and t['catDmgBite'] == s[n]['catDmgBite']
                  and sum(t['fired'].values()) == s[n]['trapTriggers'] and t['placed'].get('rat', 0) == s[n]['ratTraps'],
                  f'{tag} {n} traps {t}')
            xs = r['xfer']
            check(sum(v['xfer' + tm] for v in xs) == s[n]['transferAmt'] and sum(v['kpick' + tm] for v in xs) == s[n]['kingPickupAmt']
                  and sum(v['xfer' + tm] + v['kpick' + tm] for v in xs) == s['cheeseXfer' + n], f'{tag} {n} xfer')
            check(r['grabs'][tm]['n'] == s[n]['naps'] and sum(r['grabs'][tm]['fate'].values()) == s[n]['naps']
                  and r['throwOut'][tm].get('n', 0) == s[n]['throws'], f'{tag} {n} grabs/throws')
        rs = list(br.game_report.rows(os.path.join(d, 'g0.rounds.tsv')))
        rb = br.front(*br.load(d, 0), 1, len(rs), 1)
        check([(x['1']['n'], x['2']['n']) for x in rb] == [(int(x['babiesA']), int(x['babiesB'])) for x in rs],
              f'{tag} front: the rebuilt alive set is babiesA/B')
        check(all(sum(t['placed'].values()) == t['hit'] + t['removed'] + t['live'] for t in r['traps'].values()),
              f'{tag} traps placed = hit + removed + live')
        if tag == 'throws.bc26':
            check((r['grabs']['1']['n'], r['grabs']['2']['n']) == (11, 35) and 42 in r['throws']['2'], f'{tag} grabs 11/35')
            check(r['grabs']['2']['fate'].get('throw') == 32 and r['grabs']['1']['fate'].get('throw') == 8
                  and not any('died' in r['grabs'][tm]['fate'] for tm in '12'), f"{tag} grab fates {r['grabs']}")
            check(r['throwOut']['2'].get('fed') == 1 and r['throwOut']['2'].get('none', 0) == 0, f'{tag} a throw into a cat')
        else:
            check(r['deaths']['2']['merged'] == 6, f'{tag} formation merges are not deaths')

print(f'battle: {checks - fails}/{checks} passed')
sys.exit(1 if fails else 0)
