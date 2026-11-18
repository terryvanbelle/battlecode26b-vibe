package xk5;

import battlecode.common.*;

/**
 * Turn loop. Compute monitor: an overrun shows as the round advancing during our own turn;
 * a near miss is a turn using more than 90% of the limit. Both are counted and shown in the
 * indicator string with greppable tags (OVR=, NEAR=, EXC=).
 */
public strictfp class RobotPlayer {
    public static int overruns, nearMiss, exceptions, maxBc;
    /** Overruns before this turn's check. */
    static int ovSeen;

    public static void run(RobotController rc) {
        G.debt = 0;
        G.init(rc);
        Sym.init();

        while (true) {
            int startRound = rc.getRoundNum();
            Debug.begin(startRound);
            try {
                G.startTurn();
                if (G.king) King.turn();
                else {
                    Baby.turn();
                }
            } catch (Exception e) {
                exceptions++;
                Debug.exception(e);
            }
            // a rat that promoted this turn is a king from here (no baby end-of-turn work)
            G.king = rc.getType() == UnitType.RAT_KING;
            try {
                // the trail arrays first (Flow.work spent to its reserve and starved them on big maps),
                // leaving Flow's reserve; nothing after the tag pad then depends on the indicator's cost
                if (!G.king && rc.getRoundNum() == startRound) Trail.alloc(Cfg.FLOW_RESERVE + Cfg.FRONTIER_PAD);
                // xa6: frontier scouting's search overshot the reserve on newborns' first turns (superioristhmus, 3 overruns)
                if (!G.king && rc.getRoundNum() == startRound) Flow.work(Cfg.FLOW_RESERVE + Cfg.FRONTIER_PAD);
                // xb3: BitFlow rushers overran by up to 90 bytecodes at the reserve alone (Stash, 9-23 a game): same pad
                if (Baby.bfOn && !G.king && rc.getRoundNum() == startRound) BitFlow.work(Cfg.FLOW_RESERVE + Cfg.FRONTIER_PAD);
            } catch (Exception e) {
                exceptions++;
                Debug.exception(e);
            }
            int bc = Clock.getBytecodeNum();
            int limit = rc.getType() == UnitType.RAT_KING ? 20000 : 17500;
            Debug.mark(99);
            ovSeen = overruns;
            if (rc.getRoundNum() != startRound) overruns++;
            else {
                if (bc > maxBc) maxBc = bc;
                if (bc > limit * 9 / 10) nearMiss++;
            }
            int b1 = 0;   // unused; its two bytecodes stay in the turn's count (the tag ledger)
            if (Clock.getBytecodesLeft() >= (G.king ? Cfg.IND_LEFT_K : Cfg.IND_LEFT)) Debug.indicator();
            // an overrun inside the indicator counts too (once)
            if (overruns == ovSeen && rc.getRoundNum() != startRound) overruns++;

            G.debt = -2;   // ledger: the next turn starts with no extra bytecodes (Debug.mark(99) above adds to it; the tag pads after)
            if (Debug.TAG && rc.getRoundNum() == startRound && Clock.getBytecodesLeft() >= Debug.TAG_MIN_LEFT) Debug.tag();
            Clock.yield();
        }
    }
}
