package ratbot;

import battlecode.common.*;
import java.util.*;

import static ratbot.BotTest.*;

/**
 * sw81 = yl54 + WARN 8 (11-24): warnings. At war a baby that sees an enemy baby with free hands within dist2 WARN of
 * one of our babies that cannot see it (Baby.sees: outside that ally's 90-degree cone) squeaks the enemy's tile
 * (Comms.SQ_WARN) before report()'s other news; a baby that hears a warning of this round or the last for a tile within
 * dist2 WARN of it that it cannot see turns to face it (Baby.heedWarning, at the top of Baby.turn, not while carrying
 * a rat). Evidence: in 12 replica games on 11-24, 59% of 1675 grabs of our rats came from a tile the rat could not see.
 */
public class WarnTest {
    static RobotInfo baby(int id, Team t, int x, int y, Direction d, RobotInfo carrying) {
        return new RobotInfo(id, t, UnitType.BABY_RAT, 100, new MapLocation(x, y), d, 0, 0, carrying);
    }

    /** At war on an open 30x20 map, us a baby at (10,10) facing NORTH in round 300, plenty of bytecodes. */
    static void world() throws Exception {
        reset(); W = 30; H = 20; install();
        type = UnitType.BABY_RAT; me = new MapLocation(10, 10); facing = Direction.NORTH; round = 300;
        moveCd = 0; turnCd = 0; actCd = 0;
        G.startTurn(); G.coop = false;
        Comms.numKings = 0; Comms.numMines = 0;
        Baby.cat = null;
        clock(15000);
    }

    static Message msg(int type, int x, int y, int r) {
        return new Message(Comms.squeak(type, x, y, 0), 7, r, new MapLocation(x + 1, y));
    }

    static int warnings() {
        int n = 0;
        for (int c : squeaks) if (Comms.sqType(c) == Comms.SQ_WARN) n++;
        return n;
    }

    public static void main(String[] args) throws Exception {
        check(Cfg.WARN == 8 && Cfg.WARN_LEFT == 2500, "warn: WARN 8, WARN_LEFT 2500 (sw81 = yl54 + WARN 8)");
        Set<Integer> types = new HashSet<>(List.of(Comms.SQ_MINE, Comms.SQ_SYM, Comms.SQ_CAT, Comms.SQ_START, Comms.SQ_CASH));
        check(Comms.SQ_WARN == 11 && !types.contains(Comms.SQ_WARN), "warn: SQ_WARN 11 is a new squeak type");

        // ---- the sender: warn()
        Team us = Team.A, them = Team.B;
        world();
        Baby.seen = new RobotInfo[] {baby(50, them, 12, 10, Direction.WEST, null), baby(51, us, 12, 11, Direction.NORTH, null)};
        check(!Baby.sees(Baby.seen[1], Baby.seen[0].location), "warn: setup: the ally at (12,11) facing NORTH cannot see (12,10)");
        boolean sq = Baby.warn();
        check(sq && squeaks.size() == 1 && Comms.sqType(squeaks.get(0)) == Comms.SQ_WARN && Comms.sqX(squeaks.get(0)) == 12
              && Comms.sqY(squeaks.get(0)) == 10, "warn: an enemy behind an ally (dist2 1) is squeaked (" + squeaks + ")");
        world();
        Baby.seen = new RobotInfo[] {baby(50, them, 12, 10, Direction.WEST, null), baby(51, us, 12, 11, Direction.SOUTH, null)};
        check(!Baby.warn() && squeaks.isEmpty(), "warn: the ally faces the enemy: no warning");
        world();
        Baby.seen = new RobotInfo[] {baby(50, them, 12, 10, Direction.WEST, baby(60, us, 12, 10, Direction.NORTH, null)),
                                     baby(51, us, 12, 11, Direction.NORTH, null)};
        check(!Baby.warn() && squeaks.isEmpty(), "warn: an enemy carrying a rat has no free hands: no warning");
        world();
        Baby.seen = new RobotInfo[] {baby(50, them, 12, 8, Direction.WEST, null), baby(51, us, 12, 11, Direction.NORTH, null)};
        check(!Baby.warn() && squeaks.isEmpty(), "warn: the enemy is dist2 9 > WARN from the ally: no warning");
        world();
        Baby.seen = new RobotInfo[] {baby(50, them, 12, 9, Direction.WEST, null), baby(51, us, 12, 11, Direction.NORTH, null)};
        check(Baby.warn() && Comms.sqY(squeaks.get(0)) == 9, "warn: dist2 4 (within WARN 8) is warned");
        world();
        Baby.seen = new RobotInfo[] {baby(50, them, 14, 9, Direction.WEST, null), baby(52, them, 13, 11, Direction.WEST, null),
                                     baby(51, us, 13, 12, Direction.NORTH, null), baby(53, us, 14, 11, Direction.NORTH, null)};
        // (14,9): dist2 5 to (13,12)? no: 1 + 9 = 10; dist2 4 to (14,11) behind it. (13,11): dist2 1 to (13,12) behind it
        check(Baby.warn() && Comms.sqX(squeaks.get(0)) == 13 && Comms.sqY(squeaks.get(0)) == 11,
              "warn: the closest blind pair is warned (13,11), not (14,9) (" + squeaks + ")");
        world();
        Baby.seen = new RobotInfo[] {baby(50, them, 12, 10, Direction.WEST, null), baby(51, us, 12, 11, Direction.NORTH, null)};
        clock(Cfg.WARN_LEFT - 1);
        check(!Baby.warn() && squeaks.isEmpty(), "warn: under WARN_LEFT bytecodes left: no scan");
        world();
        Baby.seen = new RobotInfo[] {baby(50, them, 12, 10, Direction.WEST, null)};
        check(!Baby.warn() && squeaks.isEmpty(), "warn: no ally in view: no warning");
        world();
        Baby.seen = new RobotInfo[] {new RobotInfo(50, them, UnitType.RAT_KING, 500, new MapLocation(12, 10), Direction.WEST, 0, 0, null),
                                     baby(51, us, 12, 11, Direction.NORTH, null)};
        check(!Baby.warn() && squeaks.isEmpty(), "warn: an enemy king is not warned of (only babies grab)");

        // report(): at war the warning comes first; in cooperation there is none
        world();
        Baby.seen = new RobotInfo[] {baby(50, them, 12, 10, Direction.WEST, null), baby(51, us, 12, 11, Direction.NORTH, null)};
        Baby.report();
        check(warnings() == 1 && squeaks.size() == 1, "warn: report() at war squeaks the warning (" + squeaks + ")");
        world();
        G.coop = true;
        Baby.seen = new RobotInfo[] {baby(50, them, 12, 10, Direction.WEST, null), baby(51, us, 12, 11, Direction.NORTH, null)};
        Baby.report();
        check(warnings() == 0, "warn: report() in cooperation does not warn");

        // ---- the receiver: heedWarning()
        world();
        heard.put(300, List.of(msg(Comms.SQ_WARN, 11, 9, 300)));
        check(!sensable(new MapLocation(11, 9)), "warn: setup: (11,9) is behind us (facing NORTH)");
        check(Baby.heedWarning() && facing == Direction.SOUTHEAST && G.facing == Direction.SOUTHEAST,
              "warn: a warning behind us (dist2 2) turns us to face it (" + facing + ")");
        world();
        heard.put(299, List.of(msg(Comms.SQ_WARN, 10, 8, 299)));
        check(Baby.heedWarning() && facing == Direction.SOUTH, "warn: last round's warning counts (" + facing + ")");
        world();
        heard.put(298, List.of(msg(Comms.SQ_WARN, 10, 8, 298)));
        check(!Baby.heedWarning() && facing == Direction.NORTH, "warn: a warning two rounds old is ignored");
        world();
        heard.put(300, List.of(msg(Comms.SQ_WARN, 11, 11, 300)));
        check(!Baby.heedWarning() && facing == Direction.NORTH, "warn: a warned tile we can see (45 degrees) needs no turn");
        world();
        heard.put(300, List.of(msg(Comms.SQ_WARN, 13, 7, 300)));
        check(!Baby.heedWarning() && facing == Direction.NORTH, "warn: a warning at dist2 18 > WARN is ignored");
        world();
        heard.put(300, List.of(msg(Comms.SQ_MINE, 11, 9, 300)));
        check(!Baby.heedWarning() && facing == Direction.NORTH, "warn: other squeak types are ignored");
        world();
        turnCd = 10;
        heard.put(300, List.of(msg(Comms.SQ_WARN, 11, 9, 300)));
        check(!Baby.heedWarning() && facing == Direction.NORTH, "warn: no turn while the turn cooldown is not ready");
        world();
        heard.put(300, List.of(msg(Comms.SQ_WARN, 8, 8, 300), msg(Comms.SQ_WARN, 9, 9, 300)));
        check(Baby.heedWarning() && facing == Direction.SOUTHWEST, "warn: the nearest of two warnings (9,9) is faced (" + facing + ")");
        world();
        heard.put(300, List.of(msg(Comms.SQ_WARN, 10, 10, 300)));
        check(!Baby.heedWarning() && facing == Direction.NORTH, "warn: a warning on our own tile is ignored");

        System.out.println("WarnTest: " + (BotTest.checks - BotTest.fails) + "/" + BotTest.checks + " passed");
        System.exit(BotTest.fails == 0 ? 0 : 1);
    }
}
