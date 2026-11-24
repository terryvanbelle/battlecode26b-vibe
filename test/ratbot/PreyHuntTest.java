package ratbot;

import battlecode.common.*;
import java.lang.reflect.Method;

import static ratbot.BotTest.*;

/**
 * sw72's hunt within one turn's sensing (audit 11-24). Baby.findEnemies runs with Cfg.PREY_WEAKEST (yl33's prey by
 * health) and Cfg.PREY_TRAP (yl34's hunter). This test also covers the hunter cases PreyTrapTest leaves out: a wall
 * between us and the hunter, and a hunter behind us. We stand at (5,5) facing north on an open 30x30 map, in our
 * home half, with our king far away at (25,25).
 */
public class PreyHuntTest {
    static MapLocation p(int x, int y) { return new MapLocation(x, y); }

    static RobotInfo rat(int id, int hp, int x, int y, Direction d) {
        return new RobotInfo(id, Team.B, UnitType.BABY_RAT, hp, p(x, y), d, 0, 0, null);
    }

    static final int RESERVE = Math.min(Cfg.KING_RESERVE, 2 * (2000 - 300)) + 20;

    /** A fresh war turn at round 300 with hp health and a bank of cheese (budget word 5: not austere). */
    static void world(int hp, int cheese) {
        reset(); W = 30; H = 30; install();
        trapsLaid.clear();
        health = hp;
        round = 300; G.startTurn(); G.coop = false;
        Baby.trapOk = true;
        global = cheese; shared[Comms.BUDGET] = 5;
        Baby.home = p(5, 5);
        Comms.numKings = 1; Comms.kings[0] = p(25, 25);
        Baby.hunter = null; Baby.nearestEnemy = null; Baby.hunting = false;
        Baby.wbRound = -1;
    }

    static Method find;

    /** One findEnemies call for a baby with this role among these enemy rats (Baby.hunter is left as it was). */
    static void sense(int role, RobotInfo... rs) throws Exception {
        Baby.role = role; Baby.hunting = false; Baby.nearestEnemy = null;
        for (RobotInfo r : rs) robots.put(r.location, r);
        find.invoke(null, (Object) rs);
    }

    static String at(RobotInfo r) { return r == null ? "null" : r.location.toString(); }

    static String state() {
        return "hunting " + Baby.hunting + ", target " + at(Baby.nearestEnemy) + ", hunter " + at(Baby.hunter);
    }

    public static void main(String[] args) throws Exception {
        find = Baby.class.getDeclaredMethod("findEnemies", RobotInfo[].class);
        find.setAccessible(true);
        check(Cfg.PREY_WEAKEST && Cfg.PREY_TRAP, "prey hunt: PREY_WEAKEST and PREY_TRAP on (sw72)");

        // PREY_WEAKEST through findEnemies, with 100 health: a gatherer hunts prey within dist2 8 only
        RobotInfo near = rat(81, 80, 5, 7, Direction.NORTH), far = rat(82, 40, 7, 8, Direction.NORTH);
        world(100, RESERVE + 500);
        sense(Baby.GATHER, near);
        check(Baby.hunting && Baby.nearestEnemy == near,
              "prey hunt: a gatherer hunts a lone weaker rat at dist2 4 (" + state() + ")");
        world(100, RESERVE + 500);
        sense(Baby.GATHER, far, near);
        check(!Baby.hunting && Baby.nearestEnemy == near,
              "prey hunt: a gatherer with a weaker rat at dist2 4 and a weaker one still at dist2 13 hunts neither: its"
              + " prey is the far one, past its dist2 8 (the yl33 audit's slip; its fix yl39 was retired at -26) (" + state() + ")");
        world(100, RESERVE + 500);
        sense(Baby.FIGHT, far, near);
        check(Baby.hunting && Baby.nearestEnemy == far,
              "prey hunt: a fighter hunts the weakest one, at dist2 13 (" + state() + ")");

        // PREY_TRAP through findEnemies, with 60 health
        RobotInfo h = rat(50, 100, 5, 7, Direction.SOUTH);
        world(60, RESERVE + 500);
        sense(Baby.GATHER, h);
        check(Baby.hunter == h && Baby.nearestEnemy == h && !Baby.hunting,
              "prey hunt: findEnemies names the healthier watcher at dist2 4 the hunter (" + state() + ")");
        world(60, RESERVE + 500);
        Baby.hunter = h;   // as an earlier turn left it
        sense(Baby.GATHER, rat(59, 30, 5, 7, Direction.SOUTH));
        check(Baby.hunter == null, "prey hunt: with only a weaker enemy in view findEnemies leaves no hunter (" + state() + ")");

        // a wall row between us and the watcher: it cannot walk straight in (Baby.walled), so it is no hunter
        world(60, RESERVE + 500);
        for (int x = 3; x <= 7; x++) Sym.record(x, 6, true, false, false);
        check(Baby.walled(G.me, h.location), "prey hunt: setup: the wall row y=6 hides (5,7)");
        Baby.wbRound = -1;
        check(Baby.hunterOf(new RobotInfo[]{h}, G.me, 60) == null, "prey hunt: a healthier watcher behind a wall is no hunter");

        // a hunter behind us: no front tile is next to it, so tryRatTrap falls through to yl25's gated path
        RobotInfo back = rat(60, 100, 5, 3, Direction.NORTH);
        world(60, RESERVE - 1);
        check(Baby.hunterOf(new RobotInfo[]{back}, G.me, 60) == back, "prey hunt: a healthier watcher at dist2 4 behind us is a hunter");
        Baby.hunter = back; Baby.nearestEnemy = back;
        check(!Baby.tryRatTrap() && trapsLaid.isEmpty(),
              "prey hunt: behind us, under the reserve: no front tile next to it, and the gated path lays none (" + trapsLaid + ")");
        world(60, RESERVE + 500);
        Baby.hunter = back; Baby.nearestEnemy = back;
        check(!Baby.tryRatTrap() && trapsLaid.isEmpty(),
              "prey hunt: behind us, with a rich bank: still no front tile next to it, no trap (" + trapsLaid + ")");

        Baby.hunter = null; Baby.nearestEnemy = null; Baby.hunting = false; Comms.numKings = 0; health = 100;
        System.out.println("PreyHuntTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
