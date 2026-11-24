package ratbot;

import battlecode.common.*;
import java.lang.reflect.Method;
import static ratbot.BotTest.*;

/**
 * yl43's cat yield at war (Cfg.CAT_YIELD_R2 20, 11-24). A cat within dist2 CAT_DEFEND_R2 of one of our kings draws
 * every rat that sees it (Baby.urgentMove's first branch, mv=g). At war, a rat with an enemy baby within dist2
 * CAT_YIELD_R2 now skips that branch and takes its war branches instead. Replica vs AdamTan12_Icarus 39741, 39836 and
 * 39883: by r200 our babies bit the cat 92-187 times to Icarus's 20-23, mostly with the cat at our king, while Icarus
 * bit our rats. We stand at (5,5) with 100 health on an open 30x20 map; our king is at (5,9) and the cat's 2x2 body
 * at (8..9, 5..6), dist2 25 from the king.
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
        check(Cfg.CAT_YIELD_R2 == 20, "cat yield: CAT_YIELD_R2 20 (yl43)");
        check(Cfg.CAT_DEFEND_R2 >= 25, "cat yield: setup: the cat at dist2 25 from our king is at our king (CAT_DEFEND_R2 " + Cfg.CAT_DEFEND_R2 + ")");

        world(false);
        check(Baby.catAtKing(theCat.location) && !Baby.catYield(), "cat yield: at war with no enemy in view: no yield");
        check(move() == 'g', "cat yield: at war with no enemy in view the cat at our king draws the rat (mv=" + Baby.why + ")");

        world(false, rat(81, 5, 7, 100));
        check(Baby.catYield(), "cat yield: at war, an enemy baby at dist2 4: the rat yields");
        char m = move();
        check(m != 'g', "cat yield: ... and takes another branch than the cat (mv=" + m + ")");

        world(false, rat(82, 7, 9, 100));
        check(Baby.catYield(), "cat yield: an enemy baby at dist2 20 (the bound) still yields");
        world(false, rat(83, 10, 6, 100));
        Baby.nearestEnemy = rat(83, 10, 6, 100);
        check(!Baby.catYield(), "cat yield: an enemy baby at dist2 26: no yield");

        world(true, rat(84, 5, 7, 100));
        Baby.nearestEnemy = rat(84, 5, 7, 100);
        check(!Baby.catYield(), "cat yield: in cooperation an enemy baby in view never makes the rat yield");
        check(move() == 'g', "cat yield: ... so the cat at our king still draws it (mv=" + Baby.why + ")");

        // the cat away from our king: catAtKing false whatever the yield
        world(false);
        Comms.kings[0] = new MapLocation(25, 15);
        check(!Baby.catAtKing(theCat.location), "cat yield: setup: with our king at (25,15) the cat is not at our king");

        // (audit 11-24, sw71 crowned) a gatherer yields too
        world(false, rat(85, 5, 7, 100));
        Baby.role = Baby.GATHER;
        check(Baby.catYield(), "cat yield: a gatherer with an enemy baby at dist2 4 yields too");
        m = move();
        check(m != 'g', "cat yield: ... and the cat at our king does not draw it (mv=" + m + ")");

        // (audit 11-24) an enemy baby behind a wall is not Baby.nearestEnemy (findEnemies skips walled rats past dist2 2),
        // so it is no reason to yield: the cat at our king still draws the rat
        Method find = Baby.class.getDeclaredMethod("findEnemies", RobotInfo[].class);
        find.setAccessible(true);
        world(false);
        for (int x = 3; x <= 7; x++) Sym.record(x, 6, true, false, false);
        RobotInfo hid = rat(86, 5, 7, 100);
        robots.put(hid.location, hid);
        Baby.wbRound = -1;
        check(Baby.walled(G.me, hid.location), "cat yield: setup: the wall row y=6 hides (5,7)");
        find.invoke(null, (Object) new RobotInfo[]{hid, theCat});
        check(Baby.nearestEnemy == null && !Baby.catYield(),
              "cat yield: an enemy baby behind a wall at dist2 4 is no reason to yield (nearest " + Baby.nearestEnemy + ")");
        m = move();
        check(m == 'g', "cat yield: ... so the cat at our king draws the rat (mv=" + m + ")");

        G.coop = true; Comms.numKings = 0; Baby.cat = null; Baby.nearestEnemy = null;
        System.out.println("CatYieldTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
