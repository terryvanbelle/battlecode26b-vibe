#!/usr/bin/env python3
"""MapCensus over the map corpus: one row per map with the header's columns, and the dig columns on maps whose
answers are known by hand (DefaultSmall: every mine walkable; EscapeTheNight: the king's walled box opens only
through 12 dirt tiles; closeup: the king's own 3x3 is ringed by dirt)."""
import os, subprocess, sys
REPO = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
fails = checks = 0


def check(ok, what):
    global fails, checks
    checks += 1
    if not ok:
        fails += 1; print('FAIL', what)


java = os.path.join(os.environ.get('BC_JDK', os.path.expanduser('~/opt/jdk21')), 'bin', 'java')
cp = os.path.join(REPO, 'build', 'tools') + ':' + os.path.join(REPO, 'engine', 'battlecode26-java-1.2.6.jar')
out = subprocess.run([java, '-cp', cp, 'bc26tools.MapCensus', os.path.join(REPO, 'maps')], capture_output=True,
                     text=True, timeout=300)
check(out.returncode == 0, f'MapCensus exit {out.returncode}: {out.stderr[-300:]}')
lines = out.stdout.splitlines()
head = lines[0].split('\t') if lines else []
rows = {}
for l in lines[1:]:
    f = l.split('\t')
    check(len(f) == len(head), f'{f[0]}: {len(f)} fields for {len(head)} columns')
    rows[f[0]] = dict(zip(head, f))
nmaps = len([n for n in os.listdir(os.path.join(REPO, 'maps')) if n.endswith('.map26')])
check(len(rows) == nmaps, f'{len(rows)} rows for {nmaps} maps')
for c in ('freeTiles', 'minesFree', 'digNear', 'digHome', 'featDiff', 'gapAlt'):
    check(c in head, f'column {c}')


def col(m, c):
    return int(rows.get(m, {}).get(c, -99))


check(col('DefaultSmall', 'digNear') == 0 and col('DefaultSmall', 'minesFree') == col('DefaultSmall', 'mines'),
      'DefaultSmall: every mine walkable')
check(col('EscapeTheNight', 'digNear') == 12 and col('EscapeTheNight', 'digHome') == 12, 'EscapeTheNight: 12 digs')
check(col('EscapeTheNight', 'minesFree') == 0 and col('EscapeTheNight', 'freeTiles') == 51, 'EscapeTheNight box')
check(col('closeup', 'freeTiles') == 9, 'closeup: the king 3x3 alone')
for m, r in rows.items():
    check(int(r['minesFree']) <= int(r['mines']), f'{m}: minesFree <= mines')
    check((int(r['minesFree']) > 0) == (int(r['digNear']) == 0), f'{m}: minesFree > 0 iff digNear 0')
# symmetry columns: uneasy_alliance (vertical, kings (11,15) and (18,15)) differs from its rotational and horizontal
# twins on 4 tiles only, and its horizontal twin of king A, (11,14), would overlap A's body
check(rows.get('uneasy_alliance', {}).get('featDiff') == 'R4 H4', 'uneasy_alliance: 4 tiles tell the symmetries apart')
check(rows.get('uneasy_alliance', {}).get('gapAlt') == 'H', 'uneasy_alliance: the horizontal twin overlaps king A')
check(rows.get('DefaultSmall', {}).get('gapAlt') == '-' and rows.get('DefaultSmall', {}).get('featDiff', '').startswith('H'),
      'DefaultSmall (rotational): no gap elimination, wrong twins listed H and V')
print(f'mapcensus: {checks - fails}/{checks} passed')
sys.exit(1 if fails else 0)
