package ratbot;

import battlecode.common.*;

import static ratbot.BotTest.*;

/**
 * sw77's focus fire (Cfg.FOCUS_BITE 10; 11-24; the cross-year COMBAT guide's kill-efficiency rule). Baby.biteEnemies
 * ranks an adjacent enemy baby first by Baby.focusTurns, the rounds our rats need to kill it: ceil(hp / (10 x
 * hitters)), hitters = us + our babies within dist2 2 of it + our kings within dist2 8 of it. The old rank (lowest
 * health first, Baby.biteScore) breaks ties; enemy kings keep their rank (health, always first). We stand at (5,5)
 * facing north on an open 30x20 map; enemy A at (4,6) has 40 health and no other rat of ours next to it, enemy B at
 * (6,6) has 100 health with three of our babies next to it, which are not next to A.
 */
public class FocusBiteTest {
    static RobotInfo enemy(int id, int x, int y, int hp) {
        return new RobotInfo(id, Team.B, UnitType.BABY_RAT, hp, new MapLocation(x, y), Direction.SOUTH, 0, 0, null);
    }

    static RobotInfo ally(int id, int x, int y) {
        return new RobotInfo(id, Team.A, UnitType.BABY_RAT, 100, new MapLocation(x, y), Direction.NORTH, 0, 0, null);
    }

    static void world(RobotInfo... rs) {
        reset(); install();
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, false, false, false);
        round = 300; G.startTurn();
        G.coop = false;
        health = 100;
        for (RobotInfo r : rs) robots.put(r.location, r);
    }

    static MapLocation bite() throws Exception {
        Baby.biteEnemies(G.rc.senseNearbyRobots(-1, null));
        return bitten.isEmpty() ? null : bitten.get(0);
    }

    public static void main(String[] args) throws Exception {
        check(Cfg.FOCUS_BITE == 10, "focus bite: FOCUS_BITE 10 (sw77)");
        RobotInfo a = enemy(81, 4, 6, 40), b = enemy(82, 6, 6, 100);
        RobotInfo[] helpers = {ally(91, 7, 6), ally(92, 7, 7), ally(93, 6, 7)};

        // focusTurns: A alone 4 rounds (ceil 40/10); B with 4 hitters 3 rounds (ceil 100/40)
        world(a, b, helpers[0], helpers[1], helpers[2]);
        RobotInfo[] seen = G.rc.senseNearbyRobots(-1, null);
        check(Baby.focusTurns(a, seen, me) == 4, "focus bite: A (40 health, no helper) needs 4 rounds (" + Baby.focusTurns(a, seen, me) + ")");
        check(Baby.focusTurns(b, seen, me) == 3, "focus bite: B (100 health, 3 helpers) needs 3 rounds (" + Baby.focusTurns(b, seen, me) + ")");

        // the bite goes to B, which our rats kill sooner, not to the weaker A (the old rank)
        MapLocation t = bite();
        check(b.location.equals(t), "focus bite: we bite B, which 4 of us kill in 3 rounds, not A at 40 health (bitten " + t + ")");

        // without helpers the old rank holds: A (40) before B (100)
        world(a, b);
        t = bite();
        check(a.location.equals(t), "focus bite: with no helpers the weaker A is bitten first (bitten " + t + ")");

        // a one-round kill comes first: A at 10 health dies to our bite alone
        RobotInfo a10 = enemy(83, 4, 6, 10);
        world(a10, b, helpers[0], helpers[1], helpers[2]);
        t = bite();
        check(a10.location.equals(t), "focus bite: an enemy our bite kills this round (10 health) goes first (bitten " + t + ")");

        // equal rounds to kill: the bite rank (lower health) breaks the tie. A 30 alone: 3; B 100 with 3 helpers: 3
        RobotInfo a30 = enemy(84, 4, 6, 30);
        world(a30, b, helpers[0], helpers[1], helpers[2]);
        t = bite();
        check(a30.location.equals(t), "focus bite: a tie on rounds (3 and 3) goes to the lower health (bitten " + t + ")");

        // our king within dist2 8 of B counts as a hitter; one farther away does not
        RobotInfo king = new RobotInfo(95, Team.A, UnitType.RAT_KING, 600, new MapLocation(8, 8), Direction.NORTH, 0, 0, null);
        world(a, b, king);
        seen = G.rc.senseNearbyRobots(-1, null);
        check(Baby.focusTurns(b, seen, me) == 5, "focus bite: our king at dist2 8 of B makes 2 hitters: 5 rounds (" + Baby.focusTurns(b, seen, me) + ")");
        RobotInfo farKing = new RobotInfo(96, Team.A, UnitType.RAT_KING, 600, new MapLocation(9, 8), Direction.NORTH, 0, 0, null);
        world(a, b, farKing);
        seen = G.rc.senseNearbyRobots(-1, null);
        check(Baby.focusTurns(b, seen, me) == 10, "focus bite: our king at dist2 13 of B does not count: 10 rounds (" + Baby.focusTurns(b, seen, me) + ")");

        // enemy rats and cats are never hitters
        world(a, b, enemy(85, 7, 6, 100), enemy(86, 7, 7, 100));
        seen = G.rc.senseNearbyRobots(-1, null);
        check(Baby.focusTurns(b, seen, me) == 10, "focus bite: enemy rats next to B are not hitters (" + Baby.focusTurns(b, seen, me) + ")");

        // an adjacent enemy king still comes first, whatever the babies' rounds
        // the king centred at (6,7) fills (5..7, 6..8): its tiles (5,6) and (6,6) are next to us; a10 at (4,6) is clear of it
        RobotInfo eKing = new RobotInfo(97, Team.B, UnitType.RAT_KING, 600, new MapLocation(6, 7), Direction.SOUTH, 0, 0, null);
        world(a10);
        for (int x = 5; x <= 7; x++) for (int y = 6; y <= 8; y++) robots.put(new MapLocation(x, y), eKing);
        t = bite();
        check(t != null && t.distanceSquaredTo(eKing.location) <= 2 && !t.equals(a10.location),
              "focus bite: an adjacent enemy king is bitten before a baby we could kill (bitten " + t + ")");

        G.coop = true;
        System.out.println("FocusBiteTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
