package ratbot;

import battlecode.common.*;
import java.lang.reflect.Method;
import static ratbot.BotTest.*;

/**
 * yl56 = yl54 + CAT_YIELD_R2 0 turns the champion's cat yield off (from 20; 11-24; yl52's change, which tied yl54 at 17:15), as yl46 plays (yl46 is sw71
 * without the yield and led sw71 by +11 on 896 games at 11:28 PDT). So a cat within dist2 CAT_DEFEND_R2 of one of our
 * kings draws every rat that sees it (Baby.urgentMove's first branch, mv=g), at war too, whatever enemy babies are near.
 * Overrides test/ratbot/CatYieldTest (yl54's yield at 20). We stand at (5,5) with 100 health on an open 30x20 map; our
 * king is at (5,9) and the cat's 2x2 body at (8..9, 5..6), dist2 25 from the king.
 */
public class CatYieldTest {
    static RobotInfo rat(int id, int x, int y, int hp) {
        return new RobotInfo(id, Team.B, UnitType.BABY_RAT, hp, new MapLocation(x, y), Direction.SOUTH, 0, 0, null);
    }

    static Method urgent;
    static RobotInfo theCat = new RobotInfo(900, Team.NEUTRAL, UnitType.CAT, 3000, new MapLocation(8, 5), Direction.WEST, 0, 0, null);

    /** A FIGHT rat at war (or in cooperation) with the cat at our king and these enemy rats in view. */
    static void world(boolean coop, RobotInfo... rs) {
        reset(); install();
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, false, false, false);
        round = 300; G.startTurn(); G.coop = coop;
        health = 100;
        Comms.kings[0] = new MapLocation(5, 9); Comms.numKings = 1;
        Baby.role = Baby.FIGHT; Baby.hunting = false; Baby.nearestEnemy = null; Baby.rescueT = null;
        Baby.cat = theCat; robots.put(theCat.location, theCat);
        for (RobotInfo r : rs) { robots.put(r.location, r); if (Baby.nearestEnemy == null && !coop) Baby.nearestEnemy = r; }
    }

    /** urgentMove's branch tag (Baby.why) for this turn, '-' when it took no branch. */
    static char move() throws Exception {
        Baby.why = '-';
        urgent.invoke(null, 0);
        return Baby.why;
    }

    public static void main(String[] args) throws Exception {
        urgent = Baby.class.getDeclaredMethod("urgentMove", int.class);
        urgent.setAccessible(true);
        check(Cfg.CAT_YIELD_R2 == 0, "cat yield off: CAT_YIELD_R2 0 (yl56, as yl52)");
        check(Cfg.CAT_DEFEND_R2 >= 25, "cat yield off: setup: the cat at dist2 25 from our king is at our king (CAT_DEFEND_R2 " + Cfg.CAT_DEFEND_R2 + ")");

        world(false);
        check(Baby.catAtKing(theCat.location) && !Baby.catYield(), "cat yield off: at war with no enemy in view: no yield");
        check(move() == 'g', "cat yield off: at war with no enemy in view the cat at our king draws the rat (mv=" + Baby.why + ")");

        // yl54 yields here (an enemy baby at dist2 4); yl56 does not
        world(false, rat(81, 5, 7, 100));
        check(!Baby.catYield(), "cat yield off: at war, an enemy baby at dist2 4 is no reason to yield");
        char m = move();
        check(m == 'g', "cat yield off: ... so the cat at our king still draws the rat (mv=" + m + ")");

        world(false, rat(85, 5, 7, 100));
        Baby.role = Baby.GATHER;
        check(!Baby.catYield(), "cat yield off: a gatherer with an enemy baby at dist2 4 does not yield either");
        m = move();
        check(m == 'g', "cat yield off: ... and the cat at our king draws it (mv=" + m + ")");

        world(true, rat(84, 5, 7, 100));
        Baby.nearestEnemy = rat(84, 5, 7, 100);
        check(!Baby.catYield() && move() == 'g', "cat yield off: in cooperation the cat at our king draws the rat (mv=" + Baby.why + ")");

        G.coop = true; Comms.numKings = 0; Baby.cat = null; Baby.nearestEnemy = null;
        System.out.println("CatYieldTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
