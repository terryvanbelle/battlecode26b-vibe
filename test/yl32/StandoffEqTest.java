package ratbot;

import battlecode.common.*;
import static ratbot.BotTest.*;

/**
 * yl32's standoff health test (Cfg.STANDOFF_EQ, 11-23). Baby.war's standoff held against a watching enemy baby with
 * at least our health; with STANDOFF_EQ it holds only against a healthier one, and a fighter walks to an equal one
 * (neither can grab the other from the front, HEALTH_GRAB_THRESHOLD 0). We stand at (5,5) facing north on an open
 * 30x20 map; the enemy at (5,8) faces south, so it sees us.
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
        check(Cfg.STANDOFF_EQ, "standoff eq: STANDOFF_EQ on (yl32)");
        check(!Baby.standoffFoe(100, 100) && !Baby.standoffFoe(60, 60), "standoff eq: an equal enemy is no standoff foe");
        check(Baby.standoffFoe(100, 90) && Baby.standoffFoe(41, 40), "standoff eq: a healthier enemy is");
        check(!Baby.standoffFoe(90, 100), "standoff eq: a weaker enemy is not (prey)");

        // equal health, watching: we walk to it (the standoff held here through yl25)
        world(100, 100, Direction.SOUTH);
        check(Baby.sees(Baby.nearestEnemy, me), "standoff eq: setup: the enemy sees us");
        MapLocation from = me;
        check(Baby.war() && !me.equals(from) && me.distanceSquaredTo(new MapLocation(5, 8)) < 9,
              "standoff eq: an equal watching enemy is approached (at " + me + ")");

        // (audit 11-24) an equal watcher one step away at (5,7): the walk ends adjacent and the bite on arrival lands
        world(100, 100, Direction.SOUTH);
        robots.clear();
        RobotInfo near = new RobotInfo(80, Team.B, UnitType.BABY_RAT, 100, new MapLocation(5, 7), Direction.SOUTH, 0, 0, null);
        robots.put(near.location, near);
        Baby.nearestEnemy = near;
        check(Baby.war() && me.equals(new MapLocation(5, 6)) && Baby.biteEnemies(new RobotInfo[]{near}) && bitten.contains(near.location),
              "standoff eq: an equal watcher one step away is walked to and bitten on arrival (at " + me + ")");

        // a healthier watching enemy: hold and face it, as before
        world(90, 100, Direction.SOUTH);
        from = me;
        check(Baby.war() && me.equals(from) && facing == Direction.NORTH,
              "standoff eq: a healthier watching enemy is held off, facing it (" + me + ", " + facing + ")");

        // a healthier enemy facing away does not see us: approached as before
        world(90, 100, Direction.NORTH);
        from = me;
        check(!Baby.sees(Baby.nearestEnemy, me) && Baby.war() && !me.equals(from),
              "standoff eq: a healthier enemy facing away is approached as before (at " + me + ")");

        health = 100; G.coop = true; Baby.nearestEnemy = null;
        System.out.println("StandoffEqTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
