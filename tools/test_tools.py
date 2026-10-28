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
print(f"test_tools: {checks - fails}/{checks} passed")
sys.exit(1 if fails else 0)
