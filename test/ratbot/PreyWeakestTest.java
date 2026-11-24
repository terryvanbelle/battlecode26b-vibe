package ratbot;

import battlecode.common.*;
import static ratbot.BotTest.*;

/**
 * yl33's prey by health (Cfg.PREY_WEAKEST, 11-23). Baby.prey was the nearest enemy baby within dist2 20 weaker than
 * us; with PREY_WEAKEST it is the weakest one, the nearest of equals. We stand at (5,5) with 100 health on an open
 * 30x20 map.
 */
public class PreyWeakestTest {
    static RobotInfo rat(int id, int x, int y, int hp) {
        return new RobotInfo(id, Team.B, UnitType.BABY_RAT, hp, new MapLocation(x, y), Direction.SOUTH, 0, 0, null);
    }

    static void world() {
        reset(); install();
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, false, false, false);
        round = 300; G.startTurn(); G.coop = false;
    }

    public static void main(String[] args) throws Exception {
        check(Cfg.PREY_WEAKEST, "prey weakest: PREY_WEAKEST on (yl33)");
        world();
        RobotInfo near = rat(81, 5, 7, 80), far = rat(82, 7, 8, 40), strong = rat(83, 5, 6, 100);
        RobotInfo p = Baby.prey(new RobotInfo[]{near, far, strong}, 100);
        check(p == far, "prey weakest: the 40-health rat at dist2 13 beats the 80-health one at dist2 4 (" + (p == null ? null : p.location) + ")");
        p = Baby.prey(new RobotInfo[]{near, rat(84, 6, 7, 80)}, 100);
        check(p == near, "prey weakest: equal health: the nearer one (" + (p == null ? null : p.location) + ")");
        p = Baby.prey(new RobotInfo[]{strong, rat(85, 4, 6, 100)}, 100);
        check(p == null, "prey weakest: an enemy with our health or more is never prey");
        p = Baby.prey(new RobotInfo[]{near, rat(86, 9, 9, 10)}, 100);
        check(p == near, "prey weakest: a weaker rat past dist2 20 is out of range (dist2 32)");
        p = Baby.prey(new RobotInfo[]{rat(87, 5, 7, 60), rat(88, 3, 8, 30)}, 50);
        check(p != null && p.health == 30, "prey weakest: only rats below our own health count (we have 50)");

        // (audit 11-24) the weakest rat behind a wall is passed over for the next weakest in the open: a wall row
        // y=7 from x=2 to 9 hides (5,9) (dist2 16, 30 health) but not (7,6) (dist2 5, 70 health)
        reset(); install(); round = 300; G.startTurn(); G.coop = false;
        for (int x = 2; x <= 9; x++) Sym.record(x, 7, true, false, false);
        RobotInfo hid = rat(89, 5, 9, 30), open = rat(90, 7, 6, 70);
        check(Baby.walled(me, hid.location) && !Baby.walled(me, open.location), "prey weakest: setup: the wall row hides (5,9) only");
        Baby.wbRound = -1;
        p = Baby.prey(new RobotInfo[]{hid, open}, 100);
        check(p == open, "prey weakest: the weakest rat behind a wall is passed over for the next weakest in the open ("
              + (p == null ? null : p.location) + ")");

        G.coop = true;
        System.out.println("PreyWeakestTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
