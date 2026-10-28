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
print(f"test_tools: {checks - fails}/{checks} passed")
sys.exit(1 if fails else 0)
