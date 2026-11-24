package ratbot;

import battlecode.common.*;

import static ratbot.BotTest.*;

/**
 * sw74's local odds at war (Cfg.ODDS 1, ODDS_R2 8; 11-24). Baby.war's standoff also holds (we face the nearest enemy
 * baby instead of walking to it) when the enemy babies we see within dist2 ODDS_R2 of it, itself included, outnumber us
 * and the babies of ours we see there by ODDS or more (Baby.outnumbered). We stand at (5,5) facing north on an open
 * 30x20 map with 100 health. The nearest enemy is at (5,8) facing north, away from us, with our health: it does not see
 * us, so sw71's standoff alone walks to it.
 */
public class OddsTest {
    static RobotInfo rat(int id, Team t, int x, int y, int hp, Direction d) {
        return new RobotInfo(id, t, UnitType.BABY_RAT, hp, new MapLocation(x, y), d, 0, 0, null);
    }

    /** A FIGHT rat at war with the nearest enemy at (5,8) facing north and these other rats in view. */
    static void world(RobotInfo... rs) throws Exception {
        reset(); install();
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, false, false, false);
        round = 300; G.startTurn();
        G.coop = false;
        health = 100;
        Baby.role = Baby.FIGHT; Baby.hunting = false; Baby.eKing = null; Comms.numKings = 0;
        RobotInfo e = rat(80, Team.B, 5, 8, 100, Direction.NORTH);
        robots.put(e.location, e);
        for (RobotInfo r : rs) robots.put(r.location, r);
        Baby.nearestEnemy = e;
        Baby.seen = G.rc.senseNearbyRobots(-1, null);
    }

    /** Whether war() held us in place facing the nearest enemy (true) or walked us toward it (false). */
    static boolean held() throws Exception {
        MapLocation from = me;
        boolean acted = Baby.war();
        check(acted, "odds: setup: war() acts with an enemy at dist2 9");
        return me.equals(from) && facing == Direction.NORTH;
    }

    public static void main(String[] args) throws Exception {
        check(Cfg.ODDS == 1 && Cfg.ODDS_R2 == 8, "odds: ODDS 1, ODDS_R2 8 (sw74)");

        // one enemy, facing away: not outnumbered, so we walk to it (as sw71)
        world();
        check(!Baby.sees(Baby.nearestEnemy, me), "odds: setup: the enemy at (5,8) faces away and does not see us");
        check(!Baby.outnumbered(), "odds: one enemy against us alone is not outnumbered");
        check(!held(), "odds: one enemy facing away is walked to (at " + me + ")");

        // two enemies together against us alone: hold
        world(rat(81, Team.B, 6, 9, 100, Direction.NORTH));
        check(Baby.outnumbered(), "odds: two enemies within dist2 8 of each other outnumber us alone");
        check(held(), "odds: ... so we hold and face the nearest (at " + me + ", " + facing + ")");

        // two enemies and one of ours beside them: even, walk in
        world(rat(81, Team.B, 6, 9, 100, Direction.NORTH), rat(90, Team.A, 4, 7, 100, Direction.NORTH));
        check(!Baby.outnumbered(), "odds: two enemies against us and one of ours near them is even");
        check(!held(), "odds: ... so we walk in (at " + me + ")");

        // three enemies, one of ours near them: hold
        world(rat(81, Team.B, 6, 9, 100, Direction.NORTH), rat(82, Team.B, 4, 9, 100, Direction.NORTH),
              rat(90, Team.A, 4, 7, 100, Direction.NORTH));
        check(Baby.outnumbered(), "odds: three enemies against us and one of ours is outnumbered");
        check(held(), "odds: ... so we hold (at " + me + ")");

        // an enemy beyond ODDS_R2 of the nearest one does not count: (5,11) is dist2 9 from (5,8)
        world(rat(81, Team.B, 5, 11, 100, Direction.NORTH));
        check(!Baby.outnumbered(), "odds: an enemy at dist2 9 from the nearest is not in its group");
        check(!held(), "odds: ... so we walk in (at " + me + ")");

        // an ally of ours outside ODDS_R2 of the nearest enemy does not count: (5,4) is dist2 16 from (5,8)
        world(rat(81, Team.B, 6, 9, 100, Direction.NORTH), rat(90, Team.A, 5, 4, 100, Direction.NORTH));
        check(Baby.outnumbered(), "odds: our rat behind us (dist2 16 from the nearest enemy) is not in the fight");

        // a cat or an enemy king near the nearest enemy is not a baby: not counted
        world(new RobotInfo(900, Team.NEUTRAL, UnitType.CAT, 3000, new MapLocation(6, 9), Direction.WEST, 0, 0, null));
        check(!Baby.outnumbered(), "odds: a cat beside the nearest enemy is not counted");

        // while hunting prey the odds do not hold us (prey moves still engage)
        world(rat(81, Team.B, 6, 9, 100, Direction.NORTH));
        Baby.hunting = true;
        check(!held(), "odds: a hunter is not held by the odds (at " + me + ")");

        health = 100; G.coop = true; Baby.nearestEnemy = null; Baby.hunting = false; Baby.seen = new RobotInfo[0];
        System.out.println("OddsTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
