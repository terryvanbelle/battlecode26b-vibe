package ratbot;

import battlecode.common.*;

import static ratbot.BotTest.*;

/**
 * sw71's standoff (audit 11-24). sw71 was built on yl25's line and has no Cfg.STANDOFF_EQ. So Baby.war's standoff holds
 * against a watching enemy baby with at least our health, as through yl25 (and in yl46, which turns STANDOFF_EQ off).
 * The previous champion sw72 held it only against a healthier one; its StandoffEqTest is now in test/sw72. We stand at
 * (5,5) facing north on an open 30x20 map. The enemy is at (5,8); facing south, it sees us.
 */
public class StandoffTest {
    static void world(int myHp, int ehp, Direction edir) {
        reset(); install();
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, false, false, false);
        round = 300; G.startTurn();
        G.coop = false;
        health = myHp;
        Baby.role = Baby.FIGHT; Baby.hunting = false; Baby.eKing = null; Comms.numKings = 0;
        RobotInfo e = new RobotInfo(80, Team.B, UnitType.BABY_RAT, ehp, new MapLocation(5, 8), edir, 0, 0, null);
        robots.put(e.location, e);
        Baby.nearestEnemy = e;
    }

    public static void main(String[] args) throws Exception {
        // equal health, watching: held off and faced (sw72 walked to it)
        world(100, 100, Direction.SOUTH);
        check(Baby.sees(Baby.nearestEnemy, me), "standoff: setup: the enemy sees us");
        MapLocation from = me;
        check(Baby.war() && me.equals(from) && facing == Direction.NORTH,
              "standoff: an equal watching enemy is held off, facing it (" + me + ", " + facing + ")");
        world(60, 60, Direction.SOUTH);
        from = me;
        check(Baby.war() && me.equals(from) && facing == Direction.NORTH,
              "standoff: an equal watcher at 60 health each is held off too (" + me + ", " + facing + ")");

        // a healthier watching enemy: held off
        world(90, 100, Direction.SOUTH);
        from = me;
        check(Baby.war() && me.equals(from) && facing == Direction.NORTH,
              "standoff: a healthier watching enemy is held off, facing it (" + me + ", " + facing + ")");

        // a healthier enemy facing away does not see us: approached
        world(90, 100, Direction.NORTH);
        from = me;
        check(!Baby.sees(Baby.nearestEnemy, me) && Baby.war() && !me.equals(from),
              "standoff: a healthier enemy facing away is approached (at " + me + ")");

        // a weaker watching enemy cannot grab us: no standoff, we walk in
        world(100, 90, Direction.SOUTH);
        from = me;
        check(Baby.war() && !me.equals(from), "standoff: a weaker watching enemy is walked to (at " + me + ")");

        health = 100; G.coop = true; Baby.nearestEnemy = null;
        System.out.println("StandoffTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
