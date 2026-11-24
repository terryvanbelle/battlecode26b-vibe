package ratbot;

import battlecode.common.*;
import static ratbot.BotTest.*;

/**
 * yl27's look before you throw (Cfg.THROW_LOOK, 11-23). A baby sees a 90-degree cone (engine UnitType.BABY_RAT), so
 * Baby.tryThrow scores most of its 8 flights blind. On: our kings' 3x3 from Comms.kings enter the throw grid, a flight
 * into our king is never thrown, and after turning to a line it could not see the baby senses again and re-checks that
 * line (Baby.lineOurs): one of our babies on it holds the throw while THROW_HOLD lasts, our king on it holds it always.
 * The mock senses every robot (no cone), so each case passes the pre-turn view to tryThrow explicitly; the re-sense
 * after the turn sees the whole mock world. The baby stands at (5,5) on a 30x20 map facing north, carrying an enemy.
 */
public class ThrowLookTest {
    static final MapLocation ME = new MapLocation(5, 5);

    static void setup(MapLocation... wallTiles) throws GameActionException {
        reset(); W = 30; H = 20; me = ME; facing = Direction.NORTH;
        install(); G.coop = false; G.round = 100; Baby.carryRound = 99; Comms.numKings = 0; Baby.looksHeld = 0;
        for (MapLocation l : wallTiles) walls.add(l);
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, walls.contains(new MapLocation(x, y)), false, false);
    }

    static RobotInfo ally(int id, int x, int y) {
        RobotInfo a = new RobotInfo(id, Team.A, UnitType.BABY_RAT, 100, new MapLocation(x, y), Direction.NORTH, 0, 0, null);
        robots.put(a.location, a);
        return a;
    }

    public static void main(String[] args) throws Exception {
        check(Cfg.THROW_LOOK && Cfg.THROW_LOOK_BC == 2500, "throw look: THROW_LOOK on, THROW_LOOK_BC 2500 (" + Cfg.THROW_LOOK_BC + ")");

        // our king 3 east (centre (8,5), its west column at the point-blank tile), unseen; a wall 6 east past it. With
        // no king in Comms the scorer takes the wall (18, the best line): the throw the engine lands in our king
        setup(new MapLocation(11, 5));
        G.round = Baby.carryRound + Cfg.THROW_HOLD;
        check(Baby.tryThrow(new RobotInfo[0]) && thrown == Direction.EAST, "throw look: setup: blind, the wall line east is the best throw (" + thrown + ")");
        setup(new MapLocation(11, 5));
        G.round = Baby.carryRound + Cfg.THROW_HOLD;
        Comms.kings[0] = new MapLocation(8, 5); Comms.numKings = 1;
        check(Baby.tryThrow(new RobotInfo[0]) && thrown != null && thrown != Direction.EAST,
              "throw look: with our king in Comms the flight east is never thrown, unseen as it is (" + thrown + ")");

        // the grid: Comms kings fill their 3x3 where nothing seen stands, clipped to the 17x17 grid, far kings skipped
        setup();
        Comms.kings[0] = new MapLocation(13, 5); Comms.kings[1] = new MapLocation(25, 15); Comms.numKings = 2;
        int st = Baby.gridFill(ME, new RobotInfo[0]);
        Baby.gridKings(ME, st);
        int marked = 0;
        for (int i = 0; i < 289; i++) if (Baby.gridAt[i] == st) marked++;
        int c = (7 + 8) + 17 * (0 + 8);
        check(marked == 6 && Baby.grid[c].team == G.us && Baby.grid[c].type == UnitType.RAT_KING,
              "throw look: a king 8 east marks the 6 tiles of its 3x3 inside the grid, a king 20 away none (" + marked + ")");

        // a seen king of ours on the only open line: the old scorer threw into it when the hold ran out; now no throw
        setup(new MapLocation(4, 4), new MapLocation(4, 5), new MapLocation(4, 6), new MapLocation(5, 4), new MapLocation(5, 6),
              new MapLocation(6, 4), new MapLocation(6, 6));
        RobotInfo ours = new RobotInfo(3, Team.A, UnitType.RAT_KING, 600, new MapLocation(8, 5), Direction.NORTH, 0, 0, null);
        robots.put(ours.location, ours);
        G.round = Baby.carryRound + Cfg.THROW_HOLD;
        check(!Baby.tryThrow(new RobotInfo[]{ours}) && thrown == null, "throw look: never into our own king, even as the only line when the hold runs out (" + thrown + ")");

        // the look: one of our babies 2 east, outside the north-facing cone; a wall 4 east (26) beats every other line
        // (10). An enemy baby a knight's move away threatens a grab, so the throw goes now: turn east, look, see ours
        setup(new MapLocation(9, 5));
        ally(20, 7, 5);
        RobotInfo grabber = enemyRat(30, 4, 7, 100);
        robots.put(grabber.location, grabber);
        check(!Baby.tryThrow(new RobotInfo[]{grabber}) && thrown == null && facing == Direction.EAST && Baby.looksHeld == 1,
              "throw look: after turning east the look finds ours on the line: no throw while the hold lasts (" + thrown + " facing " + facing + ")");

        // short of THROW_LOOK_BC bytecodes the look is skipped and the throw goes as before
        setup(new MapLocation(9, 5));
        ally(20, 7, 5);
        grabber = enemyRat(30, 4, 7, 100);
        robots.put(grabber.location, grabber);
        clock(Cfg.THROW_LOOK_BC);
        check(Baby.tryThrow(new RobotInfo[]{grabber}) && thrown == Direction.EAST && Baby.looksHeld == 0,
              "throw look: with only THROW_LOOK_BC bytecodes left there is no look (" + thrown + ")");
        clock(1 << 30);

        // the same with the hold run out: the throw goes as before (the carried rat would wriggle free or swap)
        setup(new MapLocation(9, 5));
        ally(20, 7, 5);
        G.round = Baby.carryRound + Cfg.THROW_HOLD;
        check(Baby.tryThrow(new RobotInfo[0]) && thrown == Direction.EAST && Baby.looksHeld == 0,
              "throw look: with the hold run out a baby of ours on the line takes the throw as before (" + thrown + ")");

        // our king on the turned-to line though not in Comms (stale): no throw even with the hold run out
        setup(new MapLocation(11, 5));
        RobotInfo k = new RobotInfo(3, Team.A, UnitType.RAT_KING, 600, new MapLocation(8, 5), Direction.NORTH, 0, 0, null);
        robots.put(k.location, k);
        G.round = Baby.carryRound + Cfg.THROW_HOLD;
        check(!Baby.tryThrow(new RobotInfo[0]) && thrown == null && facing == Direction.EAST && Baby.looksHeld == 1,
              "throw look: the look finds our king on the line: no throw at all (" + thrown + ")");

        // a clear line after the turn: the throw goes in the same turn
        setup(new MapLocation(9, 5));
        G.round = Baby.carryRound + Cfg.THROW_HOLD;
        check(Baby.tryThrow(new RobotInfo[0]) && thrown == Direction.EAST && Baby.looksHeld == 0, "throw look: a clear line is thrown after the look (" + thrown + ")");

        // no turn, no look: already facing the line, the scorer's view stands (a line ahead is inside the cone)
        setup(new MapLocation(9, 5));
        facing = Direction.EAST; G.facing = Direction.EAST;
        ally(20, 7, 5);
        G.round = Baby.carryRound + Cfg.THROW_HOLD;
        check(Baby.tryThrow(new RobotInfo[0]) && thrown == Direction.EAST && Baby.looksHeld == 0,
              "throw look: facing the line already, no second look (the cone covers it) (" + thrown + ")");

        // lineOurs: a wall or a robot not ours first ends the line clear
        setup(new MapLocation(7, 5));
        ally(20, 8, 5);
        st = Baby.gridFill(ME, robots.values().toArray(new RobotInfo[0]));
        check(Baby.lineOurs(ME, Direction.EAST, st) == 0, "throw look: a wall before ours stops the flight: clear");
        setup();
        ally(20, 8, 5);
        RobotInfo e = enemyRat(31, 7, 5, 100);
        robots.put(e.location, e);
        st = Baby.gridFill(ME, robots.values().toArray(new RobotInfo[0]));
        check(Baby.lineOurs(ME, Direction.EAST, st) == 0, "throw look: an enemy before ours takes the hit: clear");
        setup();
        ally(20, 8, 8);
        st = Baby.gridFill(ME, robots.values().toArray(new RobotInfo[0]));
        check(Baby.lineOurs(ME, Direction.NORTHEAST, st) == 1, "throw look: ours 3 out on the diagonal: 1");

        Comms.numKings = 0; Baby.carryRound = -100;
        System.out.println("ThrowLookTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
