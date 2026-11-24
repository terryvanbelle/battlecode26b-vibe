package ratbot;

import battlecode.common.*;
import static ratbot.BotTest.*;

/**
 * yl31's standoff switch (Cfg.STANDOFF false, 11-23). In Baby.war a fighter within dist2 20 of a watching enemy baby
 * that is not weaker used to hold and face it; with STANDOFF off it walks to it. We stand at (5,5) facing north on an
 * open 30x20 map; the enemy at (5,8) faces south (it sees us) with our health.
 */
public class StandoffTest {
    static void world(int ehp, Direction edir) {
        reset(); install();
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, false, false, false);
        round = 300; G.startTurn();
        G.coop = false;
        Baby.role = Baby.FIGHT; Baby.hunting = false; Baby.eKing = null; Comms.numKings = 0;
        RobotInfo e = new RobotInfo(80, Team.B, UnitType.BABY_RAT, ehp, new MapLocation(5, 8), edir, 0, 0, null);
        robots.put(e.location, e);
        Baby.nearestEnemy = e;
    }

    public static void main(String[] args) throws Exception {
        check(!Cfg.STANDOFF, "standoff: STANDOFF off (yl31)");

        // a watching enemy with our health, 3 tiles north: we walk to it (the standoff held here)
        world(100, Direction.SOUTH);
        check(Baby.sees(Baby.nearestEnemy, me), "standoff: setup: the enemy sees us");
        MapLocation from = me;
        boolean acted = Baby.war();
        check(acted && !me.equals(from) && me.distanceSquaredTo(new MapLocation(5, 8)) < 9,
              "standoff: off: we step toward a watching, not weaker enemy (at " + me + ")");

        // a stronger watching enemy: the same walk (the standoff covered health >= ours)
        world(140, Direction.SOUTH);
        from = me;
        check(Baby.war() && !me.equals(from), "standoff: off: we step toward a stronger watching enemy too (at " + me + ")");

        // an enemy facing away was always approached: unchanged
        world(100, Direction.NORTH);
        from = me;
        check(!Baby.sees(Baby.nearestEnemy, me) && Baby.war() && !me.equals(from), "standoff: an enemy facing away is approached as before (at " + me + ")");

        // adjacent: stay and face it, as before (no step into it)
        reset(); install();
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, false, false, false);
        round = 300; G.startTurn(); G.coop = false;
        Baby.role = Baby.FIGHT; Baby.hunting = false; Baby.eKing = null; Comms.numKings = 0;
        RobotInfo adj = new RobotInfo(81, Team.B, UnitType.BABY_RAT, 100, new MapLocation(6, 6), Direction.SOUTHWEST, 0, 0, null);
        robots.put(adj.location, adj); Baby.nearestEnemy = adj;
        from = me;
        check(Baby.war() && me.equals(from) && facing == Direction.NORTHEAST, "standoff: an adjacent enemy: stay and face it (" + me + ", " + facing + ")");

        G.coop = true; Baby.nearestEnemy = null;
        System.out.println("StandoffTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
