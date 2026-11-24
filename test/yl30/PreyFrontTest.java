package ratbot;

import battlecode.common.*;
import static ratbot.BotTest.*;

/**
 * yl30's prey front (Cfg.PREY_FRONT, 11-23). Baby.preyMask is added to Nav.avoid every turn: while hunting (the
 * nearest enemy is prey, a weaker enemy baby in view) and the prey sees us, the steps within Chebyshev 1 of the prey's
 * 3 front tiles are avoided, since a rat trap can only be laid there. We stand at (5,5) at war; the prey at (5,8)
 * faces south, toward us, so its front tiles are (4,7), (5,7) and (6,7), and only our three north steps reach their
 * 3x3.
 */
public class PreyFrontTest {
    static final MapLocation ME = new MapLocation(5, 5);

    /** The avoid bits (G.DIRS order) of the steps from ME that end within Chebyshev 1 of a front tile of a baby at
     *  loc facing d. */
    static int expected(MapLocation loc, Direction d) {
        MapLocation[] fr = { loc.add(d), loc.add(d.rotateLeft()), loc.add(d.rotateRight()) };
        int m = 0;
        for (int k = 0; k < 8; k++) {
            MapLocation n = ME.add(G.DIRS[k]);
            for (MapLocation f : fr) if (Math.max(Math.abs(n.x - f.x), Math.abs(n.y - f.y)) <= 1) m |= 1 << k;
        }
        return m == 0xFF ? 0 : m;
    }

    static void world() {
        FixTest.babyWorld(); FixTest.trapReset();
        me = ME; facing = Direction.NORTH;
        FixTest.at(30);
        G.coop = false; Nav.stall = 0; Baby.hunting = false; Baby.nearestEnemy = null;
    }

    static int bits(Direction... ds) { int m = 0; for (Direction d : ds) m |= 1 << d.ordinal(); return m; }

    public static void main(String[] args) throws Exception {
        check(Cfg.PREY_FRONT, "prey front: PREY_FRONT on");
        world();
        RobotInfo prey = FixTest.bot(60, Team.B, UnitType.BABY_RAT, new MapLocation(5, 8), Direction.SOUTH);
        int want = expected(prey.location, prey.direction);
        check(want == bits(Direction.NORTHWEST, Direction.NORTH, Direction.NORTHEAST),
              "prey front: setup: the three north steps reach the front 3x3 (" + Integer.toBinaryString(want) + ")");
        check(G.DIRS[Direction.NORTH.ordinal()] == Direction.NORTH, "prey front: G.DIRS is in Direction order");
        check(Baby.preyFront(prey, ME) == want, "prey front: a facing prey 3 tiles north masks the north steps ("
              + Integer.toBinaryString(Baby.preyFront(prey, ME)) + ")");

        Baby.nearestEnemy = prey;
        check(Baby.preyMask() == 0, "prey front: not hunting: no mask");
        Baby.hunting = true;
        check(Baby.preyMask() == want, "prey front: hunting the facing prey: the mask");
        G.coop = true;
        check(Baby.preyMask() == 0, "prey front: none in cooperation");
        G.coop = false; Nav.stall = 8;
        check(Baby.preyMask() == 0, "prey front: none after an 8-turn stall");
        Nav.stall = 7;
        check(Baby.preyMask() == want, "prey front: still on at a 7-turn stall");
        Nav.stall = 0;

        // the prey faces away (north): it cannot lay a trap toward us
        RobotInfo away = FixTest.bot(61, Team.B, UnitType.BABY_RAT, new MapLocation(5, 8), Direction.NORTH);
        check(Baby.preyFront(away, ME) == 0, "prey front: a prey facing away masks nothing");
        Baby.nearestEnemy = away;
        check(Baby.preyMask() == 0, "prey front: hunting a prey that faces away: no mask");

        // a facing prey 4 tiles off: its front tiles are 3 away, beyond every step's 3x3
        RobotInfo far = FixTest.bot(62, Team.B, UnitType.BABY_RAT, new MapLocation(5, 9), Direction.SOUTH);
        check(Baby.preyFront(far, ME) == 0 && expected(far.location, far.direction) == 0, "prey front: a prey 4 tiles off masks nothing");

        // a diagonal prey at (7,7) facing southwest: fronts (6,6), (6,7), (7,6)
        RobotInfo diag = FixTest.bot(63, Team.B, UnitType.BABY_RAT, new MapLocation(7, 7), Direction.SOUTHWEST);
        int wd = expected(diag.location, diag.direction);
        check(Baby.preyFront(diag, ME) == wd && wd != 0 && (wd & bits(Direction.SOUTHWEST)) == 0,
              "prey front: a diagonal prey masks the steps by its front, not the one away (" + Integer.toBinaryString(wd) + ")");

        // an adjacent prey facing us: its front row is our row, so all 8 steps end in the front 3x3 and the mask is
        // dropped (0, as TrapRisk.mask: no step would be left)
        RobotInfo adj = FixTest.bot(64, Team.B, UnitType.BABY_RAT, new MapLocation(5, 6), Direction.SOUTH);
        check(expected(adj.location, adj.direction) == 0 && Baby.preyFront(adj, ME) == 0,
              "prey front: an adjacent facing prey covers every step: no mask (" + Integer.toBinaryString(Baby.preyFront(adj, ME)) + ")");
        // two tiles off, diagonal-adjacent fronts: the prey at (6,7) facing southwest has fronts (5,6), (5,7), (6,6)
        RobotInfo near2 = FixTest.bot(65, Team.B, UnitType.BABY_RAT, new MapLocation(6, 7), Direction.SOUTHWEST);
        int w2 = expected(near2.location, near2.direction);
        check(Baby.preyFront(near2, ME) == w2 && w2 != 0 && (w2 & bits(Direction.SOUTH, Direction.SOUTHWEST, Direction.SOUTHEAST)) == 0,
              "prey front: a prey 2 tiles off masks the steps toward it, not those away (" + Integer.toBinaryString(w2) + ")");

        check(Baby.preyFront(null, ME) == 0, "prey front: no prey, no mask");

        FixTest.trapReset(); G.coop = true; Baby.hunting = false; Baby.nearestEnemy = null;
        System.out.println("PreyFrontTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
