package xg4;

import battlecode.common.*;

/**
 * Turn loop. Compute monitor: an overrun shows as the round advancing during our own turn;
 * a near miss is a turn using more than 90% of the limit. Both are counted and shown in the
 * indicator string with greppable tags (OVR=, NEAR=, EXC=).
 */
public strictfp class RobotPlayer {
    public static int overruns, nearMiss, exceptions, maxBc;

    public static void run(RobotController rc) {
        G.debt = 0;
        G.init(rc);
        Sym.init();
        if (Cfg.CHIRAL_SYM) {
            try {
                int cs = Sym.chiral(rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 2));
                if (cs != 0) Sym.merge(cs);
            } catch (Exception e) { exceptions++; }
        }
        while (true) {
            int startRound = rc.getRoundNum();
            Debug.begin(startRound);
            try {
                G.startTurn();
                if (G.king) King.turn();
                else {
                    if (Cfg.BURN) Baby.hpStart = rc.getHealth();
                    Baby.turn();
                    // only an enemy trap takes health during our own turn (it fires on our move)
                    if (Cfg.BURN && rc.getHealth() < Baby.hpStart && Baby.burnRound == 0) Baby.burnRound = G.round;
                }
            } catch (Exception e) {
                exceptions++;
                Debug.exception(e);
                if (Debug.ON) e.printStackTrace();
            }
            try {
                // xa6: frontier scouting's search overshot the reserve on newborns' first turns (superioristhmus, 3 overruns)
                if (Cfg.FLOW && !G.king && rc.getRoundNum() == startRound) Flow.work(Cfg.FLOW_RESERVE + (Cfg.SCOUT_FRONTIER ? Cfg.FRONTIER_PAD : 0));
                // xb3: BitFlow rushers overran by up to 90 bytecodes at the reserve alone (Stash, 9-23 a game): same pad
                if ((Cfg.BFLOW || Baby.bfOn) && !G.king && rc.getRoundNum() == startRound) BitFlow.work(Cfg.FLOW_RESERVE + Cfg.FRONTIER_PAD);
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
            int b1 = Debug.PROBE ? Clock.getBytecodeNum() : 0;
            if (Cfg.IND_LEFT == 0 || Clock.getBytecodesLeft() >= (G.king ? Cfg.IND_LEFT_K : Cfg.IND_LEFT)) Debug.indicator();
            if (Debug.PROBE) Debug.indBc = Clock.getBytecodeNum() - b1;
            G.debt = -2;   // ledger: the next turn starts with no extra bytecodes (Debug.mark(99) above adds to it; the tag pads after)
            if (Debug.TAG && rc.getRoundNum() == startRound && Clock.getBytecodesLeft() >= Debug.TAG_MIN_LEFT) Debug.tag();
            if (Cfg.TRAIL && !G.king && rc.getRoundNum() == startRound) Trail.alloc();
            Clock.yield();
        }
    }
}
