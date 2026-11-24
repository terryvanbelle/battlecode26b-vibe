package ratbot;

import battlecode.common.*;

import static ratbot.BotTest.*;

/**
 * yl34's hunted rat's trap (Cfg.PREY_TRAP, 11-23). Baby.hunterOf finds a healthier enemy baby with free hands that
 * sees us from dist2 3-8 (it can step next to us and grab us); tryRatTrap lays its trap in front of that hunter
 * first, and that trap skips the KING_RESERVE and austerity gates (the RAT_TRAP_MIN_CHEESE floor stays). Our rat
 * stands at (5,5) facing north with 60 health on an open 30x30 map, in its home half, our king far away at (25,25).
 */
public class PreyTrapTest {
    static MapLocation p(int x, int y) { return new MapLocation(x, y); }

    static RobotInfo rat(int id, Team t, int hp, int x, int y, Direction d, RobotInfo carrying) {
        return new RobotInfo(id, t, UnitType.BABY_RAT, hp, p(x, y), d, 0, 0, carrying);
    }

    static final int RESERVE = Math.min(Cfg.KING_RESERVE, 2 * (2000 - 300)) + 20;

    /** A fresh war turn at round 300 with a bank of cheese and the budget word (0 = austere from AUSTERE_FROM). */
    static void world(int cheese, int budget) {
        reset(); W = 30; H = 30; install();
        trapsLaid.clear();
        health = 60;
        round = 300; G.startTurn(); G.coop = false;
        Baby.trapOk = true;
        global = cheese; shared[Comms.BUDGET] = budget;
        Baby.home = p(5, 5);
        Comms.numKings = 1; Comms.kings[0] = p(25, 25);
        Baby.hunter = null; Baby.nearestEnemy = null;
    }

    public static void main(String[] args) throws Exception {
        check(Cfg.PREY_TRAP, "prey trap: PREY_TRAP on (yl34)");
        world(RESERVE + 500, 5);
        RobotInfo h = rat(50, Team.B, 100, 5, 7, Direction.SOUTH, null);
        check(Baby.hunterOf(new RobotInfo[]{h}, G.me, 60) == h, "prey trap: a healthier enemy watching us from dist2 4 is a hunter");
        check(Baby.hunterOf(new RobotInfo[]{rat(51, Team.B, 100, 5, 7, Direction.NORTH, null)}, G.me, 60) == null,
              "prey trap: one facing away cannot see us: no hunter");
        check(Baby.hunterOf(new RobotInfo[]{rat(52, Team.B, 60, 5, 7, Direction.SOUTH, null)}, G.me, 60) == null,
              "prey trap: an equal one cannot grab a rat that sees it: no hunter");
        check(Baby.hunterOf(new RobotInfo[]{rat(53, Team.B, 100, 5, 6, Direction.SOUTH, null)}, G.me, 60) == null,
              "prey trap: an adjacent one grabs without a step: no hunter");
        check(Baby.hunterOf(new RobotInfo[]{rat(54, Team.B, 100, 5, 8, Direction.SOUTH, null)}, G.me, 60) == null,
              "prey trap: one at dist2 9 is past the range");
        check(Baby.hunterOf(new RobotInfo[]{rat(55, Team.B, 100, 5, 7, Direction.SOUTH, rat(9, Team.A, 30, 5, 7, Direction.SOUTH, null))}, G.me, 60) == null,
              "prey trap: a carrier's hands are full: no hunter");
        check(Baby.hunterOf(new RobotInfo[]{rat(56, Team.A, 100, 5, 7, Direction.SOUTH, null)}, G.me, 60) == null,
              "prey trap: an ally is no hunter");
        RobotInfo far = rat(57, Team.B, 100, 7, 7, Direction.SOUTHWEST, null);
        check(Baby.hunterOf(new RobotInfo[]{far, h}, G.me, 60) == h, "prey trap: the nearer of two hunters");
        check(Baby.hunterOf(new RobotInfo[]{far}, G.me, 60) == far, "prey trap: dist2 8 still counts");

        // under the reserve, not austere, home half: yl25's gate holds without a hunter, the hunter's trap skips it
        world(RESERVE - 1, 5);
        Baby.nearestEnemy = h;
        check(!Baby.tryRatTrap() && trapsLaid.isEmpty(), "prey trap: under the reserve, no hunter: the gate holds (as yl25)");
        world(RESERVE - 1, 5);
        Baby.nearestEnemy = h; Baby.hunter = Baby.hunterOf(new RobotInfo[]{h}, G.me, 60);
        boolean laid = Baby.tryRatTrap();
        check(laid && trapsLaid.size() == 1 && trapsLaid.get(0).distanceSquaredTo(h.location) <= 2 && trapsLaid.get(0).isAdjacentTo(p(5, 5)),
              "prey trap: under the reserve, a trap goes next to the hunter (" + trapsLaid + ")");
        // austere with a rich bank: the hunter's trap skips austerity too
        world(RESERVE + 500, 0);
        Baby.nearestEnemy = h; Baby.hunter = h;
        check(Baby.tryRatTrap() && trapsLaid.size() == 1, "prey trap: austere, the hunter's trap is laid");
        // under the RAT_TRAP_MIN_CHEESE floor: no trap even for a hunter
        world(Econ.floor(Cfg.RAT_TRAP_MIN_CHEESE) - 1, 5);
        Baby.nearestEnemy = h; Baby.hunter = h;
        check(!Baby.tryRatTrap() && trapsLaid.isEmpty(), "prey trap: under the cheese floor, no trap for a hunter either");
        // hunting a weaker prey to the east while a hunter comes from the north: the trap goes in front of the hunter
        world(RESERVE + 500, 5);
        RobotInfo prey = rat(58, Team.B, 30, 8, 5, Direction.WEST, null);
        Baby.nearestEnemy = prey;
        check(!Baby.tryRatTrap(), "prey trap: no hunter, the trap aims at the eastern prey: no front tile next to it (as yl25)");
        world(RESERVE + 500, 5);
        Baby.nearestEnemy = prey; Baby.hunter = h;
        check(Baby.tryRatTrap() && trapsLaid.size() == 1 && trapsLaid.get(0).distanceSquaredTo(h.location) <= 2,
              "prey trap: with the hunter, the trap goes in front of it instead (" + trapsLaid + ")");
        // the action is spent: no second trap this turn
        check(!Baby.tryRatTrap() && trapsLaid.size() == 1, "prey trap: one trap a turn (the action is spent)");

        Baby.hunter = null; Baby.nearestEnemy = null; Comms.numKings = 0; health = 100;
        System.out.println("PreyTrapTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
