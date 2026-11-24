package ratbot;

import battlecode.common.*;

import static ratbot.BotTest.*;

/**
 * yl46 = sw72 with Cfg.STANDOFF_EQ off (11-24). Baby.war's standoff holds again against a watching enemy baby with at
 * least our health, as through yl25 and in sw71: an equal watcher is held off and faced, not walked to. The champion
 * sw72's test/ratbot/StandoffEqTest checks the opposite on purpose. We stand at (5,5) facing north on an open 30x20
 * map; the enemy at (5,8) faces south, so it sees us.
 */
public class StandoffEqTest {
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
        check(!Cfg.STANDOFF_EQ, "standoff eq off: STANDOFF_EQ off (yl46; sw72 has it on)");
        check(Baby.standoffFoe(100, 100) && Baby.standoffFoe(60, 60), "standoff eq off: an equal enemy is a standoff foe again (as yl25)");
        check(Baby.standoffFoe(100, 90) && Baby.standoffFoe(41, 40), "standoff eq off: a healthier enemy is");
        check(!Baby.standoffFoe(90, 100), "standoff eq off: a weaker enemy is not (prey)");

        // equal health, watching: held off and faced (sw72 walks to it)
        world(100, 100, Direction.SOUTH);
        check(Baby.sees(Baby.nearestEnemy, me), "standoff eq off: setup: the enemy sees us");
        MapLocation from = me;
        check(Baby.war() && me.equals(from) && facing == Direction.NORTH,
              "standoff eq off: an equal watching enemy is held off, facing it (" + me + ", " + facing + ")");

        // a healthier watching enemy: hold and face it
        world(90, 100, Direction.SOUTH);
        from = me;
        check(Baby.war() && me.equals(from) && facing == Direction.NORTH,
              "standoff eq off: a healthier watching enemy is held off, facing it (" + me + ", " + facing + ")");

        // a healthier enemy facing away does not see us: approached
        world(90, 100, Direction.NORTH);
        from = me;
        check(!Baby.sees(Baby.nearestEnemy, me) && Baby.war() && !me.equals(from),
              "standoff eq off: a healthier enemy facing away is approached (at " + me + ")");

        health = 100; G.coop = true; Baby.nearestEnemy = null;
        System.out.println("StandoffEqTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
