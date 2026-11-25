package ratbot;

import battlecode.common.*;

import static ratbot.BotTest.*;

/**
 * sw93 = sw83 + BITE_FIRST (Baby.actBeforeMove, Baby.bitableRat). At war, a baby that can bite an enemy baby now
 * (next to it, inside its cone, the action ready) lays no rat trap that turn, so biteEnemies takes the action;
 * Baby.trapSkips counts those turns.
 * Evidence (replica 11-25, 8 matches vs AdamTan12_Icarus, bman12345678_goonv23, thewaxmango_collegejunior and
 * daannte_v47_Skadoosh; lone duels): on the ready turns after a bite with the bitten rat next to us and our health
 * equal to or below its own, sw83 laid a rat trap 26% of the time (the target in its cone every time) and the target
 * grabbed our rat within 3 rounds after 69% of those turns; collegejunior and goonv23 bit 84% and 79% there and laid
 * no trap.
 * Uses FixTest.gatherer (a gatherer on a 30x20 map) at war, round 200, rich and not austere (no cheese gate on traps),
 * with the fake clock between BC_BAIL and GRAB_BC: the turn runs its actions and the grab micro does not score.
 */
public class BiteFirstTest {
    static MapLocation p(int x, int y) { return new MapLocation(x, y); }

    static RobotInfo baby(int id, Team t, int x, int y, int hp, Direction d) {
        return new RobotInfo(id, t, UnitType.BABY_RAT, hp, p(x, y), d, 0, 0, null);
    }

    /** War at round 200: our gatherer at (10,8) facing f, health 100, every cooldown ready, nothing carried, 5000
     *  cheese in the bank and a positive budget; rat traps allowed; rs placed. No grab (carryOk false): the fake's
     *  canCarryRat ignores health, and the engine refuses a grab of an enemy that sees us and is not weaker. */
    static void world(Direction f, RobotInfo... rs) {
        FixTest.gatherer();
        round = 200; me = p(10, 8); facing = f; health = 100; G.startTurn();
        G.coop = false;
        carrying = null; actCd = 0; moveCd = 0; turnCd = 0; carryOk = false;
        Nav.avoid = 0; Nav.guard = null; GrabMicro.round = -1; GrabMicro.evalRound = -1;
        global = 5000; raw = 0; shared[Comms.BUDGET] = 5;
        Baby.trapOk = true;
        for (RobotInfo r : rs) robots.put(r.location, r);
        bitten.clear(); trapsLaid.clear();
    }

    static final int LEFT = (Cfg.BC_BAIL + Cfg.GM_BAIL) / 2;

    /** One Baby.turn with LEFT bytecodes left throughout. */
    static void turn() throws GameActionException {
        clock(LEFT);
        Baby.turn();
        clock(1 << 30);
    }

    public static void main(String[] args) throws Exception {
        check(Cfg.BITE_FIRST, "bite first: BITE_FIRST on (sw93 = sw83 + BITE_FIRST)");
        check(Cfg.TRAP_CAP > 0 && Cfg.BABY_TRAP_DIST2 >= 4, "bite first: babies lay rat traps (TRAP_CAP " + Cfg.TRAP_CAP + ")");
        check(Cfg.BC_BAIL < LEFT && LEFT < Cfg.GRAB_BC, "bite first: the test's clock " + LEFT + " runs the actions without the grab micro");

        // an equal enemy baby north of us, facing us (no grab: it sees us and is not weaker). sw83 lays a trap on a front
        // tile beside it (shown by tryRatTrap alone); sw93 bites it instead
        RobotInfo e = baby(81, Team.B, 10, 9, 100, Direction.SOUTH);
        world(Direction.NORTH, e);
        Baby.nearestEnemy = e;
        check(Baby.tryRatTrap() && trapsLaid.size() == 1 && trapsLaid.get(0).isAdjacentTo(p(10, 9)),
              "bite first: tryRatTrap alone lays a trap beside the enemy (" + trapsLaid + ")");
        world(Direction.NORTH, e);
        int skips = Baby.trapSkips, br = Baby.bitesRat;
        turn();
        check(trapsLaid.isEmpty() && carrying == null, "bite first: no rat trap when an enemy baby can be bitten (" + trapsLaid + ", " + carrying + ")");
        check(bitten.size() == 1 && bitten.get(0).equals(p(10, 9)), "bite first: the bite takes the action (" + bitten + ")");
        check(Baby.trapSkips == skips + 1 && Baby.bitesRat == br + 1, "bite first: trapSkips and bitesRat count the turn");

        // a weaker enemy that sees us: the same (the bite, not the trap)
        world(Direction.NORTH, baby(81, Team.B, 10, 9, 100, Direction.SOUTH));
        health = 80;
        turn();
        check(trapsLaid.isEmpty() && bitten.size() == 1, "bite first: weaker than the enemy, the bite too (" + trapsLaid + ", " + bitten + ")");
        health = 100;

        // the enemy beside us outside the cone (east; NORTH's cone holds NW, N, NE): no bite now, the trap goes on the
        // front tile next to it (NE), and the action is spent
        world(Direction.NORTH, baby(81, Team.B, 11, 8, 100, Direction.WEST));
        skips = Baby.trapSkips;
        turn();
        check(trapsLaid.size() == 1 && trapsLaid.get(0).equals(p(11, 9)) && bitten.isEmpty() && Baby.trapSkips == skips,
              "bite first: an enemy outside the cone: the trap as before (" + trapsLaid + ", " + bitten + ")");

        // the enemy two tiles away: out of reach, the trap as before (the front tile between us)
        world(Direction.NORTH, baby(81, Team.B, 10, 10, 100, Direction.SOUTH));
        turn();
        check(trapsLaid.size() == 1 && trapsLaid.get(0).equals(p(10, 9)) && bitten.isEmpty() && Baby.trapSkips == skips,
              "bite first: an enemy two tiles away: the trap as before (" + trapsLaid + ", " + bitten + ")");

        // an ally in front of us and the enemy two tiles away: the ally is no reason to skip the trap
        world(Direction.NORTH, baby(41, Team.A, 10, 9, 100, Direction.NORTH), baby(81, Team.B, 10, 10, 100, Direction.SOUTH));
        turn();
        check(trapsLaid.size() == 1 && bitten.isEmpty() && Baby.trapSkips == skips,
              "bite first: an ally next to us does not skip the trap (" + trapsLaid + ", " + bitten + ")");

        // the action not ready: neither, and no skip counted
        world(Direction.NORTH, baby(81, Team.B, 10, 9, 100, Direction.SOUTH));
        actCd = 10;
        turn();
        check(trapsLaid.isEmpty() && bitten.isEmpty() && Baby.trapSkips == skips, "bite first: action not ready: no trap, no bite, no skip");

        // bitableRat alone: an enemy king tile next to us is not a baby; an enemy baby in the cone is
        RobotInfo king = new RobotInfo(90, Team.B, UnitType.RAT_KING, 500, p(10, 10), Direction.SOUTH, 0, 0, null);
        world(Direction.NORTH);
        for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) robots.put(p(10 + dx, 10 + dy), king);
        check(!Baby.bitableRat(new RobotInfo[]{king}) && Baby.trapSkips == skips, "bite first: an enemy king is no baby to bite");
        RobotInfo b2 = baby(82, Team.B, 9, 9, 100, Direction.SOUTH);
        robots.put(b2.location, b2);
        check(Baby.bitableRat(new RobotInfo[]{king, b2}) && Baby.trapSkips == skips + 1, "bite first: an enemy baby NW (in the cone) can be bitten");

        robots.clear(); G.coop = true; Nav.avoid = 0; global = 0; shared[Comms.BUDGET] = 0; carryOk = true;
        System.out.println("BiteFirstTest: " + (BotTest.checks - BotTest.fails) + "/" + BotTest.checks + " passed");
        System.exit(BotTest.fails == 0 ? 0 : 1);
    }
}
