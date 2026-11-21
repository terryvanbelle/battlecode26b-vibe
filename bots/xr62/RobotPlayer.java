package xr62;

import battlecode.common.*;

/**
 * Turn loop. Compute monitor: an overrun shows as the round advancing during our own turn;
 * a near miss is a turn using more than 90% of the limit. Both are counted and shown in the
 * indicator string with greppable tags (OVR=, NEAR=, EXC=).
 */
public strictfp class RobotPlayer {
    public static int overruns, nearMiss, exceptions, maxBc;

    public static void run(RobotController rc) {
        G.init(rc);
        Sym.init();

        while (true) {
            int startRound = rc.getRoundNum();
            Debug.begin(startRound);
            try {
                G.startTurn();
                if (G.king) King.turn();
                else Baby.turn();
            } catch (Exception e) {
                exceptions++;
                Debug.exception(e);
            }
            // a rat that promoted this turn is a king from here (no baby end-of-turn work)
            G.king = rc.getType() == UnitType.RAT_KING;
            try {
                // spare bytecodes, a baby's only: the trail arrays first (path searches spend to their reserve and
                // would starve them on big maps), then the path searches. The reserve carries FRONTIER_PAD: frontier
                // searches and BitFlow overshot the bare FLOW_RESERVE and overran
                if (!G.king && rc.getRoundNum() == startRound) Trail.alloc(Cfg.FLOW_RESERVE + Cfg.FRONTIER_PAD);
                if (!G.king && rc.getRoundNum() == startRound) Flow.work(Cfg.FLOW_RESERVE + Cfg.FRONTIER_PAD);
                if (Baby.bfOn && !G.king && rc.getRoundNum() == startRound) BitFlow.work(Cfg.FLOW_RESERVE + Cfg.FRONTIER_PAD);
            } catch (Exception e) {
                exceptions++;
                Debug.exception(e);
            }
            int bc = Clock.getBytecodeNum();
            int limit = rc.getType() == UnitType.RAT_KING ? 20000 : 17500;
            Debug.mark(99);
            int ovSeen = overruns;
            if (rc.getRoundNum() != startRound) overruns++;
            else {
                if (bc > maxBc) maxBc = bc;
                if (bc > limit * 9 / 10) nearMiss++;
            }
            if (Clock.getBytecodesLeft() >= (G.king ? Cfg.IND_LEFT_K : Cfg.IND_LEFT)) Debug.indicator();
            // an overrun inside the indicator counts too (once)
            if (overruns == ovSeen && rc.getRoundNum() != startRound) overruns++;
            // last: pad the turn's bytecode count to its tag (tools/smuggle.py)
            if (Debug.TAG && rc.getRoundNum() == startRound && Clock.getBytecodesLeft() >= Debug.TAG_MIN_LEFT) Debug.tag();
            Clock.yield();
        }
    }
}
