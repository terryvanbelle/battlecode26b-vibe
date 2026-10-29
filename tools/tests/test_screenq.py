#!/usr/bin/env python3
"""tools/screenq.sh: runs are taken from runs/screenq.txt in order, one with no cells or already DONE is skipped, a
run with a KEEP file gets BC_KEEP=1 (the others an empty BC_KEEP), and the worker stops at build/screenq.stop. The
matrix is a stand-in arena.py that records what it was given."""
import json, os, shutil, subprocess, sys, tempfile
REPO = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
fails = checks = 0


def check(ok, what):
    global fails, checks
    checks += 1
    if not ok:
        fails += 1; print('FAIL', what)


FAKE = '''import json, os, sys
run = sys.argv[sys.argv.index('--run') + 1]
with open(os.path.join(run, 'got.json'), 'w') as f:
    json.dump({'keep': os.environ.get('BC_KEEP'), 'argv': sys.argv[1:]}, f)
open(os.path.join(run, 'DONE'), 'w').close()
os.makedirs('build', exist_ok=True)
with open('build/played', 'a') as f:
    f.write(os.path.basename(run) + '\\n')
if open('build/played').read().split() == ['r1', 'r3']:
    open('build/screenq.stop', 'w').close()
'''

with tempfile.TemporaryDirectory() as t:
    os.makedirs(os.path.join(t, 'tools'))
    shutil.copy(os.path.join(REPO, 'tools', 'screenq.sh'), os.path.join(t, 'tools', 'screenq.sh'))
    with open(os.path.join(t, 'tools', 'arena.py'), 'w') as f:
        f.write(FAKE)
    for r, cells, keep, done in (('r1', True, False, False), ('r2', False, False, False), ('r3', True, True, False),
                                 ('r4', True, False, True), ('r5', True, False, False)):
        d = os.path.join(t, 'runs', r)
        os.makedirs(d)
        if cells:
            with open(os.path.join(d, 'cells.tsv'), 'w') as f:
                f.write('bots/a\tbots/b\tmap\t0\n')
        if keep:
            open(os.path.join(d, 'KEEP'), 'w').close()
        if done:
            open(os.path.join(d, 'DONE'), 'w').close()
    with open(os.path.join(t, 'runs', 'screenq.txt'), 'w') as f:
        f.write('r1\n\nr2\nr4\nr3\nr5\n')
    p = subprocess.run(['sh', os.path.join(t, 'tools', 'screenq.sh'), '3'], timeout=120, capture_output=True, text=True)
    check(p.returncode == 0, f'worker exit {p.returncode} {p.stderr}')
    played = open(os.path.join(t, 'build', 'played')).read().split()
    check(played == ['r1', 'r3'], f'played in queue order, skipping r2 (no cells) and r4 (done): {played}')
    g1 = json.load(open(os.path.join(t, 'runs', 'r1', 'got.json')))
    g3 = json.load(open(os.path.join(t, 'runs', 'r3', 'got.json')))
    check(g1['keep'] == '', f'no KEEP file: BC_KEEP empty {g1}')
    check(g3['keep'] == '1', f'KEEP file: BC_KEEP=1 {g3}')
    check(g1['argv'] == ['matrix', 'runs/r1/cells.tsv', '--run', 'runs/r1', '--jobs', '3'], f'matrix args {g1}')
    left = open(os.path.join(t, 'runs', 'screenq.txt')).read().split()
    check(left == ['r5'], f'r5 still queued after the stop: {left}')
    log = open(os.path.join(t, 'runs', 'screenq.log')).read()
    check('skip r2' in log and 'skip r4' in log and 'start r1 (jobs 3)' in log and 'end r3' in log, f'log {log!r}')

print(f'screenq: {checks - fails}/{checks} passed')
sys.exit(1 if fails else 0)
