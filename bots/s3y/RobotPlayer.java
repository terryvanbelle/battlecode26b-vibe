package s3y;

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
                if (G.king) King.turn(); else Baby.turn();
            } catch (Exception e) {
                exceptions++;
                Debug.exception(e);
                if (Debug.ON) e.printStackTrace();
            }
            try {
                if (Cfg.FLOW && !G.king && rc.getRoundNum() == startRound) Flow.work(Cfg.FLOW_RESERVE);
            } catch (Exception e) {
                exceptions++;
                Debug.exception(e);
                if (Debug.ON) e.printStackTrace();
            }
            int bc = Clock.getBytecodeNum();
            int limit = rc.getType() == UnitType.RAT_KING ? 20000 : 17500;
            Debug.mark(99);
            if (rc.getRoundNum() != startRound) overruns++;
            else {
                if (bc > maxBc) maxBc = bc;
                if (bc > limit * 9 / 10) nearMiss++;
            }
            Debug.indicator();
            if (Debug.TAG && rc.getRoundNum() == startRound && Clock.getBytecodesLeft() >= Debug.TAG_MIN_LEFT) Debug.tag();
            Clock.yield();
        }
    }
}
