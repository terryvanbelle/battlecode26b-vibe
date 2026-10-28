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
# plot_progress: one svg per metric group, index lists them
pp = load('plot_progress')
with tempfile.TemporaryDirectory() as d:
    t = os.path.join(d, 'p.tsv')
    open(t, 'w').write('version\tdate\tmetric\tvalue\nv1\tx\twin.a\t0.5\nv2\tx\twin.a\t0.7\nv2\tx\twin.b\t0.2\nv2\tx\tpts.f\t40\n')
    vs, ss = pp.load(t)
    check(vs == ['v1', 'v2'] and ss['win.a'] == {'v1': 0.5, 'v2': 0.7}, f"plot load {vs} {dict(ss)}")
    subprocess.run([sys.executable, '-I', os.path.join(REPO, 'tools', 'plot_progress.py'), '--tsv', t, '--out', d], capture_output=True)
    check(os.path.exists(os.path.join(d, 'win.svg')) and os.path.exists(os.path.join(d, 'pts.svg')), "plot svgs written")
    check('polyline' in open(os.path.join(d, 'win.svg')).read(), "plot has a line")
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
