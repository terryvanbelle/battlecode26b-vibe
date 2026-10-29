#!/usr/bin/env python3
"""tools/snapvar.py: presets expand in place, each replacement must match exactly once, the incumbent preset (@s6m)
reproduces bots/s6m's Cfg from the development Cfg, and unknown presets or malformed pairs are refused."""
import os, sys
REPO = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
sys.path.insert(0, os.path.join(REPO, 'tools'))
import snapvar  # noqa: E402
fails = checks = 0


def check(ok, what):
    global fails, checks
    checks += 1
    if not ok:
        fails += 1; print('FAIL', what)


def refused(f):
    try:
        f()
    except SystemExit:
        return True
    return False


reps = snapvar.expand(['@s4d', 'X = 1=>X = 2'])
check(reps[:len(snapvar.PRESETS['s4d'])] == snapvar.PRESETS['s4d'] and reps[-1] == 'X = 1=>X = 2', f'expand {reps}')
check(snapvar.PRESETS['s6m'][:-1] == snapvar.PRESETS['s4d'], 's6m extends s4d')
check(refused(lambda: snapvar.expand(['@nope'])), 'unknown preset refused')
check(refused(lambda: snapvar.expand(['no arrow'])), 'malformed pair refused')
check(snapvar.apply('a = 1; b = 2;', ['a = 1=>a = 3', 'a = 3=>a = 4']) == 'a = 4; b = 2;', 'replacements apply in order')
check(refused(lambda: snapvar.apply('a = 1; a = 1;', ['a = 1=>a = 2'])), 'two matches refused')
check(refused(lambda: snapvar.apply('a = 1;', ['b = 1=>b = 2'])), 'no match refused')
# the incumbent's switches: the development Cfg with @s6m equals bots/s6m's Cfg once the package lines are aligned
dev = open(os.path.join(REPO, 'bots', 'ratbot', 'Cfg.java')).read()
s6m_path = os.path.join(REPO, 'bots', 's6m', 'Cfg.java')
if os.path.exists(s6m_path):
    want = open(s6m_path).read().replace('package s6m;', 'package ratbot;')
    got = snapvar.apply(dev, snapvar.expand(['@s6m']))
    import re
    # switches added to dev after s6m was cut sit at their off defaults: compare the incumbent's switches only
    vals = lambda t: dict(re.findall(r'public static final \w+ (\w+) = ([^;]+);', t))
    gv, wv = vals(got), vals(want)
    diff = {k: (gv[k], wv[k]) for k in wv if k in gv and gv[k] != wv[k]}
    check(not diff, f'@s6m reproduces bots/s6m switch values: {diff}')
print(f'snapvar: {checks - fails}/{checks} passed')
sys.exit(1 if fails else 0)
