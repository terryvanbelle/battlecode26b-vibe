package ratbot;

import battlecode.common.*;

import static ratbot.BotTest.*;

/**
 * sw91 = sw83 + BAIL_BITE (Baby.bailBite; called where a hostile turn ends early for bytecodes: Baby.turn's BC_BAIL
 * bail before the actions, and actBeforeMove's GM_BAIL/BC_BAIL bail after the grab micro when the micro did not act).
 * Before the turn ends, a rat with its action ready and BAIL_BITE_LEFT bytecodes left bites the best enemy already in
 * reach inside its cone, without turning or moving: a king tile first, then the lowest biteScore.
 * Evidence (replica 11-25, sw83 vs AdamTan12_Icarus and thewaxmango_collegejunior, 9 games): in our ready war turns
 * with an enemy baby adjacent in our cone we bit 8% and did nothing 18.5% (they bit 40%, nothing 6%); in local
 * replays 70% of sw83's such idle turns were bytecode bails (the lb counter rose that turn).
 * Uses FixTest.gatherer (a gatherer on a 30x20 map) at war, round 200; the fake clock spends nothing.
 */
public class BailBiteTest {
    static MapLocation p(int x, int y) { return new MapLocation(x, y); }

    static RobotInfo baby(int id, int x, int y, int hp, Direction d) {
        return new RobotInfo(id, Team.B, UnitType.BABY_RAT, hp, p(x, y), d, 0, 0, null);
    }

    /** War at round 200: our gatherer at (10,8) facing f, health 100, every cooldown ready, nothing carried; es placed. */
    static void world(Direction f, RobotInfo... es) {
        FixTest.gatherer();
        round = 200; me = p(10, 8); facing = f; health = 100; G.startTurn();
        G.coop = false;
        carrying = null; actCd = 0; moveCd = 0; turnCd = 0; carryOk = true;
        Nav.avoid = 0; Nav.guard = null; GrabMicro.round = -1; GrabMicro.evalRound = -1;
        for (RobotInfo e : es) robots.put(e.location, e);
        bitten.clear(); bitePaid.clear();
    }

    /** One Baby.turn with `left` bytecodes left throughout; gm: the grab micro scored this round without acting. */
    static void turn(int left, boolean gm) throws GameActionException {
        if (gm) GrabMicro.evalRound = G.round;
        clock(left);
        Baby.turn();
        clock(1 << 30);
    }

    public static void main(String[] args) throws Exception {
        check(Cfg.BAIL_BITE && Cfg.BAIL_BITE_LEFT == 1500, "bail bite: BAIL_BITE on, BAIL_BITE_LEFT 1500 (sw91 = sw83 + BAIL_BITE)");
        // between BAIL_BITE_LEFT and BC_BAIL: the turn bails before its actions, with enough left to bite
        int early = (Cfg.BAIL_BITE_LEFT + Cfg.BC_BAIL) / 2;
        // between BC_BAIL and GM_BAIL: past the early check; bails after the micro only when the micro scored
        int mid = (Cfg.BC_BAIL + Cfg.GM_BAIL) / 2;
        check(Cfg.BAIL_BITE_LEFT < early && early < Cfg.BC_BAIL && Cfg.BC_BAIL < mid && mid < Cfg.GM_BAIL && mid < Cfg.GRAB_BC,
              "bail bite: the test's bytecode levels " + early + " and " + mid + " fall in their bands");

        // the early bail: an enemy baby in reach inside our cone is bitten, and the turn still ends there
        world(Direction.NORTH, baby(81, 10, 9, 100, Direction.SOUTH));
        int low = Baby.lowBc, bb = Baby.bailBites, br = Baby.bitesRat;
        turn(early, false);
        check(bitten.size() == 1 && bitten.get(0).equals(p(10, 9)), "bail bite: the early bail bites the enemy in reach (" + bitten + ")");
        check(Baby.lowBc == low + 1 && Baby.bailBites == bb + 1 && Baby.bitesRat == br + 1,
              "bail bite: the turn still bails; bailBites and bitesRat count the bite");
        check(me.equals(p(10, 8)) && facing == Direction.NORTH, "bail bite: no move and no turn (" + me + ", " + facing + ")");

        // under BAIL_BITE_LEFT: the turn just bails
        world(Direction.NORTH, baby(81, 10, 9, 100, Direction.SOUTH));
        low = Baby.lowBc;
        turn(Cfg.BAIL_BITE_LEFT - 1, false);
        check(bitten.isEmpty() && Baby.lowBc == low + 1, "bail bite: under BAIL_BITE_LEFT no bite, the turn bails");
        world(Direction.NORTH, baby(81, 10, 9, 100, Direction.SOUTH));
        turn(Cfg.BAIL_BITE_LEFT, false);
        check(bitten.size() == 1, "bail bite: at BAIL_BITE_LEFT it bites");

        // an enemy beside us outside the cone (west; NORTH's cone holds NW, N, NE): no bite and no turn
        world(Direction.NORTH, baby(81, 9, 8, 100, Direction.EAST));
        turn(early, false);
        check(bitten.isEmpty() && facing == Direction.NORTH, "bail bite: an enemy outside the cone: no bite, no turn (" + facing + ")");
        // one in view two tiles away: out of reach
        world(Direction.NORTH, baby(81, 10, 10, 100, Direction.SOUTH));
        turn(early, false);
        check(bitten.isEmpty(), "bail bite: an enemy two tiles away is out of reach");

        // two in the cone: the lower health first (biteScore), whichever is sensed first
        world(Direction.NORTH, baby(81, 10, 9, 100, Direction.SOUTH), baby(82, 11, 9, 60, Direction.SOUTH));
        turn(early, false);
        check(bitten.size() == 1 && bitten.get(0).equals(p(11, 9)), "bail bite: the weaker of two in the cone (" + bitten + ")");
        world(Direction.NORTH, baby(82, 11, 9, 60, Direction.SOUTH), baby(81, 10, 9, 100, Direction.SOUTH));
        turn(early, false);
        check(bitten.size() == 1 && bitten.get(0).equals(p(11, 9)), "bail bite: the weaker of two, the other order (" + bitten + ")");
        // loot counts only as biteScore counts it (CARRY_BITE; 0: not at all): a 90-health enemy carrying 40 raw
        // against a 60-health one carrying none
        world(Direction.NORTH, baby(82, 11, 9, 60, Direction.SOUTH),
              new RobotInfo(83, Team.B, UnitType.BABY_RAT, 90, p(9, 9), Direction.SOUTH, 0, 40, null));
        turn(early, false);
        MapLocation want = Baby.biteScore(90, 40, Cfg.CARRY_BITE) < Baby.biteScore(60, 0, Cfg.CARRY_BITE) ? p(9, 9) : p(11, 9);
        check(bitten.size() == 1 && bitten.get(0).equals(want),
              "bail bite: loot ranks as biteScore ranks it (CARRY_BITE " + Cfg.CARRY_BITE + "; " + bitten + ")");

        // an enemy king with a tile in reach comes before a weaker baby (the fake lists the king at its tiles)
        RobotInfo king = new RobotInfo(90, Team.B, UnitType.RAT_KING, 500, p(9, 10), Direction.SOUTH, 0, 0, null);
        world(Direction.NORTH, baby(82, 11, 9, 20, Direction.SOUTH));
        robots.put(p(9, 9), king); robots.put(p(10, 9), king);
        turn(early, false);
        check(bitten.size() == 1 && (bitten.get(0).equals(p(9, 9)) || bitten.get(0).equals(p(10, 9))),
              "bail bite: a king tile in reach first (" + bitten + ")");

        // an ally in reach is never bitten
        world(Direction.NORTH, new RobotInfo(50, Team.A, UnitType.BABY_RAT, 10, p(10, 9), Direction.SOUTH, 0, 0, null));
        turn(early, false);
        check(bitten.isEmpty(), "bail bite: an ally in reach is not bitten");

        // the action not ready: no bite
        world(Direction.NORTH, baby(81, 10, 9, 100, Direction.SOUTH));
        actCd = 10;
        turn(early, false);
        check(bitten.isEmpty(), "bail bite: the action not ready: no bite");

        // in cooperation before BREAK_ROUND a gatherer is not hostile: no bite (a bite would be a backstab)
        world(Direction.NORTH, baby(81, 10, 9, 100, Direction.SOUTH));
        round = Cfg.BREAK_ROUND - 1; G.startTurn(); G.coop = true;
        turn(early, false);
        check(bitten.isEmpty(), "bail bite: in cooperation before BREAK_ROUND a gatherer does not bite");
        // from BREAK_ROUND it is hostile (Baby.hostile), as for the pre-move bite: it bites
        world(Direction.NORTH, baby(81, 10, 9, 100, Direction.SOUTH));
        round = Cfg.BREAK_ROUND; G.startTurn(); G.coop = true;
        turn(early, false);
        check(bitten.size() == 1, "bail bite: in cooperation from BREAK_ROUND (hostile) it bites, as the pre-move bite would");

        // a newborn's first turn (NB_BAIL) bails without the bite
        world(Direction.NORTH, baby(81, 10, 9, 100, Direction.SOUTH));
        Baby.birthRound = G.round;
        low = Baby.lowBc;
        turn(early, false);
        Baby.birthRound = 0;
        check(bitten.isEmpty() && Baby.lowBc == low + 1, "bail bite: the birth turn bails without the bite");

        // the micro's bail: the micro scored without acting and fewer than GM_BAIL are left; an enemy as healthy as us
        // facing us (no grab: carryOk false) is bitten before the turn ends
        world(Direction.NORTH, baby(81, 10, 9, 100, Direction.SOUTH));
        carryOk = false;
        low = Baby.lowBc;
        turn(mid, true);
        check(bitten.size() == 1 && bitten.get(0).equals(p(10, 9)) && Baby.lowBc == low + 1,
              "bail bite: the micro's GM_BAIL bail bites first (" + bitten + ", lb +" + (Baby.lowBc - low) + ")");
        check(me.equals(p(10, 8)), "bail bite: the micro's bail does not move (" + me + ")");
        // the same with a grab possible: tryCarry grabs before the micro (no bite)
        world(Direction.NORTH, baby(81, 10, 9, 60, Direction.SOUTH));
        turn(mid, true);
        check(bitten.isEmpty() && carrying != null, "bail bite: a grab before the micro is kept (no bite)");
        carrying = null;
        // the micro not scored this round: no bail at this level, the normal pre-move bite (biteEnemies) bites
        world(Direction.NORTH, baby(81, 10, 9, 100, Direction.SOUTH));
        carryOk = false;
        low = Baby.lowBc;
        bb = Baby.bailBites;
        turn(mid, false);
        check(bitten.size() == 1 && Baby.bailBites == bb, "bail bite: no bail: biteEnemies bites, not bailBite (" + bitten + ")");

        // the finishing bite is paid as biteEnemies pays it (finishCheese): an enemy k over a plain bite's 10, with the
        // bank at FINISH_FLOOR or more, gets k*k cheese; under FINISH_FLOOR, or too healthy to finish, a plain bite
        int k = Math.min(4, Cfg.FINISH_K);
        world(Direction.NORTH, baby(81, 10, 9, 10 + k, Direction.SOUTH));
        global = Cfg.FINISH_FLOOR + 100;
        turn(early, false);
        check(bitten.size() == 1 && bitePaid.equals(java.util.List.of(k * k)),
              "bail bite: a finishing bite on a " + (10 + k) + "-health enemy pays " + k * k + " cheese (" + bitePaid + ")");
        world(Direction.NORTH, baby(81, 10, 9, 10 + k, Direction.SOUTH));
        global = Cfg.FINISH_FLOOR - 1;
        turn(early, false);
        check(bitten.size() == 1 && bitePaid.equals(java.util.List.of(0)), "bail bite: under FINISH_FLOOR a plain bite (" + bitePaid + ")");
        world(Direction.NORTH, baby(81, 10, 9, 10 + Cfg.FINISH_K + 1, Direction.SOUTH));
        global = Cfg.FINISH_FLOOR + 100;
        turn(early, false);
        check(bitten.size() == 1 && bitePaid.equals(java.util.List.of(0)), "bail bite: too healthy to finish: a plain bite (" + bitePaid + ")");

        // an attacker is hostile in cooperation too (Baby.hostile): before BREAK_ROUND it bites through the bail, as its
        // pre-move bite (biteEnemies) would
        world(Direction.NORTH, baby(81, 10, 9, 100, Direction.SOUTH));
        round = Cfg.BREAK_ROUND - 1; G.startTurn(); G.coop = true;
        Baby.role = Baby.ATTACK;
        turn(early, false);
        int roleAfter = Baby.role;
        Baby.role = Baby.GATHER;
        check(roleAfter == Baby.ATTACK && bitten.size() == 1 && bitten.get(0).equals(p(10, 9)),
              "bail bite: an attacker in cooperation before BREAK_ROUND bites (" + bitten + ")");

        robots.clear(); carryOk = true; carrying = null; G.coop = true;
        System.out.println("BailBiteTest: " + (BotTest.checks - BotTest.fails) + "/" + BotTest.checks + " passed");
        System.exit(BotTest.fails == 0 ? 0 : 1);
    }
}
