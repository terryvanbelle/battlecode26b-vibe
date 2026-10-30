#!/usr/bin/env python3
"""Snapshot the development bot with Cfg.java text replacements, then restore its Cfg.

  snapvar.py NAME [@PRESET ...] ['old=>new' ...]

Each 'old=>new' must match exactly once in bots/ratbot/Cfg.java (after the presets before it). @PRESET expands to a
named list of replacements: @s4d is the switch set of s4d, @s6m s6m's, @s7d the incumbent's (accepted 2026-10-29),
@s8w the rush line's, @s9s replica #101's and @kfix the king's first-turn margin, so a
candidate is `snapvar.py s7l @s7d 'X = 0=>X = 1'`. tools/snapshot.sh does the copy, rename and compile check.
"""
import os, subprocess, sys

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PRESETS = {
    's4d': ['FORM_KING = false=>FORM_KING = true', 'FORM_NET100 = Integer.MIN_VALUE=>FORM_NET100 = -100',
            'FORM_JOIN = 0=>FORM_JOIN = 8', 'MINE_BALANCE = false=>MINE_BALANCE = true',
            'CAT_DEFEND_R2 = 0=>CAT_DEFEND_R2 = 36', 'SEEK_CROWD = 0,=>SEEK_CROWD = 6,'],
}
PRESETS['s6m'] = PRESETS['s4d'] + ['HOME_PICK = 0=>HOME_PICK = 1']
PRESETS['s7d'] = PRESETS['s6m'] + ['FLOW_REV = false=>FLOW_REV = true', 'FLOW_FAST = false=>FLOW_FAST = true']
# the king's first-turn margin: the dev code since s7d overruns 20000 on 60x60 maps with 5000
PRESETS['kfix'] = ['KING_SENSE_RESERVE = 5000=>KING_SENSE_RESERVE = 7000']
# the rush line (s8u/s8v/s8w): we open the war, rush spawns until r150
PRESETS['s8w'] = PRESETS['s7d'] + ['RUSH = false=>RUSH = true', 'RUSH_BREAK = false=>RUSH_BREAK = true',
                                   'BREAK_ROUND = 0=>BREAK_ROUND = 36', 'RUSH_UNTIL = 0=>RUSH_UNTIL = 150'] + PRESETS['kfix']
# s9s (replica #101): agrab's rush switches on the incumbent, the baby cap rising to 40 while the bank holds 1000
PRESETS['s9s'] = PRESETS['s7d'] + PRESETS['kfix'] + [
    'RICH_BABIES = 0,=>RICH_BABIES = 40,', 'KING_TRAPS = true=>KING_TRAPS = false', 'BABY_RAT_TRAPS = true=>BABY_RAT_TRAPS = false',
    'THROW_MIN = 42,=>THROW_MIN = 84,', 'CARRY_AIM = false=>CARRY_AIM = true', 'RUSH = false=>RUSH = true',
    'RUSH_BREAK = false=>RUSH_BREAK = true', 'BREAK_ROUND = 0=>BREAK_ROUND = 30', 'SYM_SLOT = false=>SYM_SLOT = true']


def expand(args):
    """The replacement list for args: @PRESET names expanded in place, unknown ones refused."""
    out = []
    for a in args:
        if a.startswith('@'):
            if a[1:] not in PRESETS:
                raise SystemExit(f'snapvar: unknown preset {a} (known: {", ".join(sorted(PRESETS))})')
            out += PRESETS[a[1:]]
        else:
            if '=>' not in a:
                raise SystemExit(f"snapvar: not 'old=>new': {a}")
            out.append(a)
    return out


def apply(text, reps):
    """text with each replacement applied in order; each old text must occur exactly once when its turn comes."""
    for rep in reps:
        a, b = rep.split('=>', 1)
        n = text.count(a)
        if n != 1:
            raise SystemExit(f'snapvar: {a!r} occurs {n} times')
        text = text.replace(a, b)
    return text


def main(argv):
    if len(argv) < 2:
        raise SystemExit(__doc__)
    p = os.path.join(REPO, 'bots', 'ratbot', 'Cfg.java')
    orig = open(p).read()
    s = apply(orig, expand(argv[2:]))
    try:
        open(p, 'w').write(s)
        subprocess.run([os.path.join(REPO, 'tools', 'snapshot.sh'), argv[1]], check=True)
    finally:
        open(p, 'w').write(orig)


if __name__ == '__main__':
    main(sys.argv)
