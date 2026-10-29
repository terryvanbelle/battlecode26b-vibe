#!/usr/bin/env python3
"""battle_report.py: a synthetic two-game tsv dump (duplicate/carried/dead records, a moving king, killing blows in
another robot's turn, a bite boost, starvation, traps, cat kills, a grab), the CLI over every game, and the
fixtures throws.bc26 and king.bc26 (built with tools/build-tools.sh; fails, never skips) against the dump's
summary. Usage: test_battle.py"""
import importlib.util, json, os, subprocess, sys, tempfile
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
    rec(6, 10, 1, 'RAT', 9, 9, 100, [('drop', 20, 2, 'voluntary', 10, 1)])
    rec(6, 20, 2, 'RAT', 9, 8, 85)
    meta = dict(map='Synth', teamA='a', teamB='b', types={'RAT': {'health': 100}, 'KING': {'health': 500}, 'CAT': {'health': 1000}},
                bodies=[dict(id=1, team=1, type='KING'), dict(id=2, team=2, type='KING'), dict(id=3, team=0, type='CAT')])
    for g in range(games):
        p = os.path.join(d, f'g{g}.')
        json.dump(dict(meta, map=f'Synth{g}', file=''), open(p + 'meta.json', 'w'))
        json.dump(dict(rounds=6, winner='B', winType='MORE_POINTS'), open(p + 'summary.json', 'w'))
        for name, h, rows in (('turns', TH, T), ('actions', AH, A)):
            with open(p + name + '.tsv', 'w') as f:
                f.write('\t'.join(h) + '\n' + ''.join('\t'.join(map(str, r)) + '\n' for r in rows))
        with open(p + 'rounds.tsv', 'w') as f:
            f.write('round\tcatDmgA\tcatDmgB\n' + ''.join(f'{r}\t0\t{130 if r >= 4 else 0}\n' for r in range(1, 7)))


with tempfile.TemporaryDirectory() as d:
    synth(d)
    check(br.games(d) == [0, 1], 'games')
    r = br.report(d, 0, 2000, 1, 0)
    check(r['hi'] == 6 and r['map'] == 'Synth0' and r['winner'] == 'B', f"header {r['hi']} {r['map']}")
    f = {x['round']: x for x in r['front']}
    check(f[2]['2'] == dict(n=2, hp=200, carried=1, front=1, dk=11.0), f"front: last record, carried not at the front {f[2]['2']}")
    check(f[2]['1']['front'] == 2 and f[2]['1']['dk'] == 3.5, f"front: enemy by its last record {f[2]['1']}")
    check(f[3]['1'] == dict(n=1, hp=100, carried=0, front=1, dk=6.0), f"front: dead rat gone, dK from the moved king {f[3]['1']}")
    check(f[4]['2']['n'] == 1, f"front: dead record skipped {f[4]['2']}")
    check(r['hploss']['2'] == {'bite': 65, 'trap': 50} and r['kingloss']['2'] == {'starve': 10}
          and r['hpby']['2'] == {'enemy': 115, 'starve': 10}, f"hploss: boost, killing bite, starvation {r['hploss']['2']} {r['kingloss']['2']} {r['hpby']['2']}")
    check(r['hploss']['1'] == {'pounce': 100} and r['hpby']['1'] == {'cat': 100}, f"hploss: pounce takes the hp left {r['hploss']['1']}")
    D = r['deaths']
    check(D['1']['rat'] == {'pounce': 1} and D['1']['by'] == {'cat': 1} and D['1']['kd'] is None, f"deaths: a cat kill is no enemy kill {D['1']}")
    check(D['2']['rat'] == {'bite': 1} and D['2']['by'] == {'enemy': 1} and D['1']['killsRat'] == 1 and D['2']['kd'] == 0.0,
          f"deaths: kd {D}")
    t1, t2 = r['traps']['1'], r['traps']['2']
    check(t1['placed'] == {'rat': 3} and t1['fired'] == {'rat': 1} and t1['removed'] == 1 and t1['live'] == 1
          and t1['hitRate'] == 0.33 and t1['meanLife'] == 1.0 and t1['victims'] == {'2': 1} and t1['dmg'] == {'rat': 50}, f"traps A {t1}")
    check(t2['placed'] == {'cat': 1} and t2['catDmg'] == 130 and t2['catDmgTrap'] == 100 and t2['catDmgBite'] == 30, f"traps B {t2}")
    x = r['xfer'][0]
    check((x['xfer2'], x['kpick2'], x['pick2'], x['pickAmt2']) == (5, 7, 1, 5), f"xfer: king pickups apart {x}")
    g = r['grabs']['1']
    check(g['n'] == 1 and g['enemy'] == 1 and g['fate'] == {'drop:voluntary': 1} and g['held'] == {'drop:voluntary': 1.0}, f"grabs {g}")
    check(br.report(d, 4, 2000, 1, 0)['deaths']['1']['rat'] == {} and br.report(d, 4, 4, 1, 0)['traps']['1']['catDmg'] == 0,
          'window lo..hi')
    run = lambda *a: subprocess.run([sys.executable, '-I', os.path.join(REPO, 'tools', 'battle_report.py'), d, *a],  # noqa: E731
                                    capture_output=True, text=True)
    p = run()
    check(p.returncode == 0 and [l.split()[1] for l in p.stdout.splitlines() if l.startswith('== ')] == ['g0', 'g1'],
          f'cli: every game {p.stderr[-300:]}')
    p = run('--game', '1', '--json')
    js = [json.loads(l) for l in p.stdout.splitlines()]
    check(p.returncode == 0 and len(js) == 1 and js[0]['game'] == 1 and js[0]['map'] == 'Synth1', 'cli: --game N --json')
    check(run('--game', '2').returncode != 0, 'cli: a missing game fails')

# fixtures: every section agrees with the dump's own summary
b = subprocess.run([os.path.join(REPO, 'tools', 'build-tools.sh')], capture_output=True, text=True)
check(b.returncode == 0, f'build-tools: {b.stderr[-500:]}')
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
        if tag == 'throws.bc26':
            check((r['grabs']['1']['n'], r['grabs']['2']['n']) == (11, 35) and 42 in r['throws']['2'], f'{tag} grabs 11/35')
            check(r['throwOut']['2'].get('fed') == 1 and r['throwOut']['2'].get('none', 0) == 0, f'{tag} a throw into a cat')
        else:
            check(r['deaths']['2']['merged'] == 6, f'{tag} formation merges are not deaths')

print(f'battle: {checks - fails}/{checks} passed')
sys.exit(1 if fails else 0)
