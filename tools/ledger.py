#!/usr/bin/env python3
"""Exact bytecode ledger for a u6x-family bot: `python3 tools/ledger.py bots/NAME` edits NAME in place.

Why: u6x spends whatever a turn leaves on Flow.work(FLOW_RESERVE), and other budget checks read the clock, so a
candidate whose new code costs even a few bytecodes a turn drifts from its base's games after a few hundred rounds,
whether or not the new code ever acts (z2f, TRAINING_LOG 2026-11-06 09:34). With the ledger, G.debt holds the
bytecodes this turn spent on code the base does not run, minus 2, and every budget read adds it back, so the base's
budget checks see the base's counts and the game replays the base's exactly until the new code acts (z2h: keepout A
identical for 2000 rounds and every hot= reading equal, 09:52).

How the engine counts (instrumenter/bytecode/InstrumentingMethodVisitor.java): 1 an instruction, flushed into the
count only at a basic block's end (a jump, label, return, throw, or a call of our own code; the call itself costs
0). Engine API calls add their MethodCosts.txt cost without ending the block, and a Clock read is no block end, so
an inline read sees the count as of the last block end.

This tool makes the shared edits:
- G.debt and G.bc() (Clock.getBytecodeNum() in a method of its own, so the call ends the caller's block);
- Flow/BitFlow/KPath/Seal left(): Clock.getBytecodesLeft() + G.debt (the same 3 counted instructions as the base's
  `unlimited ? MAX : Clock.getBytecodesLeft()`, whose getstatic and ifeq are counted before its read: the minus 2);
- the direct reads (King's floor and END_KING_LEFT, Baby's THROW_BC, SENSE2_LEFT, SWEEP_LEFT) and Debug.mark:
  `Clock.getBytecodesLeft() + (G.debt += 8) - 6` / `getBytecodeNum() - (G.debt += 8) + 6` (8 extra instructions);
- G.debt = 0 before G.init and -2 at the end of every turn, after Debug.mark(99) and before the replay tag.

The candidate's own code is then wrapped by hand, as in bots/z2h/Baby.java:
    int cd = G.debt, cb = G.bc();
    ... new code ...
    G.debt = cd + G.bc() - cb + K;
K = 9 plus the counted instructions after the second read that the base does not run (2 for `if (flag) ...` on a
boolean the hook set). Static fields of new code must not have initialisers (no <clinit> cost). Check the result with
a dumped game against the base: ~/diag26b/replay_eq.py (state) and ~/diag26b/hot_eq.py (every hot= equal).
The tool fails if a pattern is missing, and lists every clock read left in the bot for review."""
import re
import sys

LEFT = 'static int left() { return Clock.getBytecodesLeft() + G.debt; }'
DIRECT = 'Clock.getBytecodesLeft() + (G.debt += 8) - 6'
EDITS = [
    ('Flow.java', 'static int left() { return unlimited ? Integer.MAX_VALUE : Clock.getBytecodesLeft(); }', LEFT),
    ('BitFlow.java', 'static int left() { return Flow.unlimited ? Integer.MAX_VALUE : Clock.getBytecodesLeft(); }', LEFT),
    ('KPath.java', 'static int left() { return unlimited ? fakeLeft : Clock.getBytecodesLeft(); }', LEFT),
    ('Seal.java', 'return Flow.unlimited ? Integer.MAX_VALUE : Clock.getBytecodesLeft();',
     'return Clock.getBytecodesLeft() + G.debt;'),
    ('King.java', 'if (floor > 0 && Clock.getBytecodesLeft() < floor) break;',
     'if (floor > 0 && ' + DIRECT + ' < floor) break;'),
    ('King.java', 'if (Cfg.END_KING_CROWD && Clock.getBytecodesLeft() < Cfg.END_KING_LEFT) return;',
     'if (Cfg.END_KING_CROWD && ' + DIRECT + ' < Cfg.END_KING_LEFT) return;'),
    ('Baby.java', 'else if (Clock.getBytecodesLeft() > Cfg.THROW_BC) tryThrow', 'else if (' + DIRECT + ' > Cfg.THROW_BC) tryThrow'),
    ('Baby.java', 'if (Clock.getBytecodesLeft() < Cfg.SENSE2_LEFT) return;', 'if (' + DIRECT + ' < Cfg.SENSE2_LEFT) return;'),
    ('Baby.java', 'if (cap == 0 && Clock.getBytecodesLeft() < Cfg.SWEEP_LEFT) break;',
     'if (cap == 0 && ' + DIRECT + ' < Cfg.SWEEP_LEFT) break;'),
    ('Debug.java', '        int bc = battlecode.common.Clock.getBytecodeNum();\n        if (!crossed',
     '        // ledger: the base\'s count at this point; the hot-stage branch below costs bytecodes, so it must branch as the base\'s\n'
     '        int bc = battlecode.common.Clock.getBytecodeNum() - (G.debt += 8) + 6;\n        if (!crossed'),
    ('RobotPlayer.java', '    public static void run(RobotController rc) {\n        G.init(rc);',
     '    public static void run(RobotController rc) {\n        G.debt = 0;\n        G.init(rc);'),
    ('RobotPlayer.java',
     '            if (Debug.TAG && rc.getRoundNum() == startRound && Clock.getBytecodesLeft() >= Debug.TAG_MIN_LEFT) Debug.tag();\n',
     '            G.debt = -2;   // ledger: the next turn starts with no extra bytecodes (Debug.mark(99) above adds to it; the tag pads after)\n'
     '            if (Debug.TAG && rc.getRoundNum() == startRound && Clock.getBytecodesLeft() >= Debug.TAG_MIN_LEFT) Debug.tag();\n'),
]
G_FIELDS = '''
    /** Bytecode ledger (tools/ledger.py): the bytecodes this robot spent this turn on code its base does not run, minus
     *  2. Every budget read adds it back, so the base's budget checks (Flow.work above all, which spends whatever a turn
     *  leaves) see the base's counts, and games replay the base's until the new code acts. */
    public static int debt;

    /** The bytecode count, read in a method of its own: the engine counts a basic block's instructions only at its end
     *  (a jump, label, return or call of our own code), and a Clock read is no block end, so a read inline would see a
     *  count that depends on javac's line labels. The call to bc() ends the caller's block; its own return counts 1. */
    static int bc() { return Clock.getBytecodeNum(); }
'''


def main():
    d = sys.argv[1].rstrip('/') + '/'
    for f, old, new in EDITS:
        s = open(d + f).read()
        if s.count(old) != 1:
            sys.exit('%s: pattern found %d times: %s' % (f, s.count(old), old[:70]))
        open(d + f, 'w').write(s.replace(old, new))
    s = open(d + 'G.java').read()
    if 'static int debt' in s:
        sys.exit('G.java already has a ledger')
    m = re.search(r'public final class G[^{]*\{', s)
    s = s[:m.end()] + G_FIELDS + s[m.end():]
    open(d + 'G.java', 'w').write(s)
    import glob
    print('clock reads left (each must be a ledger read, a hook read, or after Flow.work):')
    for f in sorted(glob.glob(d + '*.java')):
        for i, line in enumerate(open(f), 1):
            if 'getBytecodesLeft' in line or 'getBytecodeNum' in line:
                print('  %s:%d %s' % (f, i, line.strip()[:110]))


if __name__ == '__main__':
    main()
