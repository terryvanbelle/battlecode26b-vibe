package ratbot;

import battlecode.common.*;
import java.lang.reflect.Method;
import static ratbot.BotTest.*;

/**
 * yl39's gatherer prey fix (Cfg.FIX_GATHER_PREY, 11-24, from the yl33 audit). Baby.findEnemies lets a gatherer hunt
 * prey only within dist2 8. With PREY_WEAKEST, Baby.prey ranked every weaker rat within dist2 20 by health, so a
 * weaker rat at dist2 9-20 hid a nearer weaker one and the gatherer hunted neither; yl32's nearest-first prey hunted
 * the near one. We stand at (5,5) with 100 health on an open 30x20 map: a rat with 80 health at (5,7) (dist2 4) and
 * one with 40 at (7,8) (dist2 13).
 */
public class GatherPreyTest {
    static RobotInfo rat(int id, int x, int y, int hp) {
        return new RobotInfo(id, Team.B, UnitType.BABY_RAT, hp, new MapLocation(x, y), Direction.SOUTH, 0, 0, null);
    }

    static Method find;

    /** One findEnemies call for a baby with this role among these enemy rats. */
    static void world(int role, RobotInfo... rs) throws Exception {
        reset(); install();
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, false, false, false);
        round = 300; G.startTurn(); G.coop = false;
        health = 100; Comms.numKings = 0;
        Baby.role = role; Baby.hunting = false; Baby.nearestEnemy = null;
        for (RobotInfo r : rs) robots.put(r.location, r);
        find.invoke(null, (Object) rs);
    }

    static String at() { return "hunting " + Baby.hunting + ", target " + (Baby.nearestEnemy == null ? null : Baby.nearestEnemy.location); }

    public static void main(String[] args) throws Exception {
        find = Baby.class.getDeclaredMethod("findEnemies", RobotInfo[].class);
        find.setAccessible(true);
        check(Cfg.FIX_GATHER_PREY && Cfg.PREY_WEAKEST, "gather prey: FIX_GATHER_PREY on (yl39), with PREY_WEAKEST");
        RobotInfo near = rat(81, 5, 7, 80), far = rat(82, 7, 8, 40);
        world(Baby.GATHER);
        check(Baby.prey(new RobotInfo[]{near, far}, 100, 8) == near, "gather prey: within dist2 8 the near rat is the prey");
        check(Baby.prey(new RobotInfo[]{near, far}, 100) == far, "gather prey: within dist2 20 the weaker far one (two-argument prey unchanged)");

        world(Baby.GATHER, near, far);
        check(Baby.hunting && Baby.nearestEnemy == near, "gather prey: a gatherer hunts the near weaker rat (" + at() + ")");
        world(Baby.GATHER, far, near);
        check(Baby.hunting && Baby.nearestEnemy == near, "gather prey: the same with the robots in the other order (" + at() + ")");
        world(Baby.FIGHT, near, far);
        check(Baby.hunting && Baby.nearestEnemy == far, "gather prey: a fighter still hunts the weakest across its vision (" + at() + ")");
        world(Baby.GATHER, far);
        check(!Baby.hunting && Baby.nearestEnemy == far,
              "gather prey: a gatherer does not hunt a lone weaker rat at dist2 13; it is only the nearest enemy (" + at() + ")");
        // within reach the weakest still wins: 40 health at (6,7) (dist2 5) over 80 at (5,6) (dist2 1)
        RobotInfo weak = rat(83, 6, 7, 40), close = rat(84, 5, 6, 80);
        world(Baby.GATHER, close, weak);
        check(Baby.hunting && Baby.nearestEnemy == weak, "gather prey: within dist2 8 a gatherer hunts the weakest (" + at() + ")");

        G.coop = true;
        System.out.println("GatherPreyTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
