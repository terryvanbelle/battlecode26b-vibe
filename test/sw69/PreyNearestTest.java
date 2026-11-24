package ratbot;

import battlecode.common.*;
import java.lang.reflect.Method;
import static ratbot.BotTest.*;

/**
 * sw69's prey (audit 11-24). sw69 is built on yl32, which has no Cfg.PREY_WEAKEST: Baby.prey is the NEAREST enemy
 * baby within dist2 20 that is weaker than us and not behind a wall (yl25's rule), where yl33, the champion before,
 * took the weakest one. Baby.findEnemies lets a gatherer hunt that prey only within dist2 8. We stand at (5,5) with
 * 100 health on an open 30x20 map.
 */
public class PreyNearestTest {
    static RobotInfo rat(int id, int x, int y, int hp) {
        return new RobotInfo(id, Team.B, UnitType.BABY_RAT, hp, new MapLocation(x, y), Direction.SOUTH, 0, 0, null);
    }

    static void world() {
        reset(); install();
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, false, false, false);
        round = 300; G.startTurn(); G.coop = false;
    }

    static Method find;

    /** One findEnemies call for a baby with this role among these enemy rats. */
    static void hunt(int role, RobotInfo... rs) throws Exception {
        world();
        health = 100; Comms.numKings = 0;
        Baby.role = role; Baby.hunting = false; Baby.nearestEnemy = null;
        for (RobotInfo r : rs) robots.put(r.location, r);
        find.invoke(null, (Object) rs);
    }

    static String at(RobotInfo p) { return p == null ? "null" : p.location.toString(); }

    static String target() { return "hunting " + Baby.hunting + ", target " + at(Baby.nearestEnemy); }

    public static void main(String[] args) throws Exception {
        boolean weakest = true;
        try { Cfg.class.getField("PREY_WEAKEST"); } catch (NoSuchFieldException e) { weakest = false; }
        check(!weakest, "prey nearest: no Cfg.PREY_WEAKEST (sw69 is on yl32)");
        world();
        RobotInfo near = rat(81, 5, 7, 80), far = rat(82, 7, 8, 40), strong = rat(83, 5, 6, 100);
        RobotInfo p = Baby.prey(new RobotInfo[]{far, near, strong}, 100);
        check(p == near, "prey nearest: the 80-health rat at dist2 4 beats the 40-health one at dist2 13 (" + at(p) + ")");
        p = Baby.prey(new RobotInfo[]{near, rat(84, 7, 5, 30)}, 100);
        check(p == near, "prey nearest: equal distance: the first sensed, whatever its health (" + at(p) + ")");
        p = Baby.prey(new RobotInfo[]{strong, rat(85, 4, 6, 100)}, 100);
        check(p == null, "prey nearest: an enemy with our health or more is never prey (" + at(p) + ")");
        p = Baby.prey(new RobotInfo[]{rat(86, 9, 9, 10)}, 100);
        check(p == null, "prey nearest: a weaker rat at dist2 32 is out of range (" + at(p) + ")");
        p = Baby.prey(new RobotInfo[]{rat(87, 5, 7, 60), rat(88, 3, 8, 30)}, 50);
        check(p != null && p.health == 30, "prey nearest: only rats below our own health count (we have 50: " + at(p) + ")");

        // a wall row y=7 from x=2 to 9 hides (5,8) (dist2 9) but not (8,6) (dist2 10)
        reset(); install(); round = 300; G.startTurn(); G.coop = false;
        for (int x = 2; x <= 9; x++) Sym.record(x, 7, true, false, false);
        RobotInfo hid = rat(89, 5, 8, 50), open = rat(90, 8, 6, 50);
        check(Baby.walled(me, hid.location) && !Baby.walled(me, open.location), "prey nearest: setup: the wall row hides (5,8) only");
        Baby.wbRound = -1;
        p = Baby.prey(new RobotInfo[]{hid, open}, 100);
        check(p == open, "prey nearest: the nearest weaker rat behind a wall is passed over for the next nearest in the open (" + at(p) + ")");

        // findEnemies: a gatherer hunts prey within dist2 8 only; a fighter across its vision
        find = Baby.class.getDeclaredMethod("findEnemies", RobotInfo[].class);
        find.setAccessible(true);
        hunt(Baby.GATHER, far, near);
        check(Baby.hunting && Baby.nearestEnemy == near, "prey nearest: a gatherer hunts the near weaker rat (" + target() + ")");
        hunt(Baby.GATHER, far);
        check(!Baby.hunting && Baby.nearestEnemy == far,
              "prey nearest: a gatherer does not hunt a lone weaker rat at dist2 13; it is only the nearest enemy (" + target() + ")");
        hunt(Baby.FIGHT, far);
        check(Baby.hunting && Baby.nearestEnemy == far, "prey nearest: a fighter hunts it (" + target() + ")");

        G.coop = true;
        System.out.println("PreyNearestTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
