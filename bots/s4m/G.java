package s4m;

import battlecode.common.*;

/** Globals shared by every layer. Refreshed once per turn by {@link #startTurn()}. */
public final class G {
    public static RobotController rc;
    public static Team us, them;
    public static int W, H, myId;
    public static int round;
    public static MapLocation me;
    public static Direction facing;
    public static boolean king;
    public static boolean coop;
    public static int globalCheese, rawCheese;

    /** Map centre in doubled coordinates (so half-integer centres stay integral). */
    public static int cx2, cy2;

    /** The eight compass directions, enum order. Never used for tie-breaks on its own. */
    public static final Direction[] DIRS = {
        Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
        Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST };

    public static void init(RobotController r) {
        rc = r;
        us = r.getTeam();
        them = us.opponent();
        W = r.getMapWidth();
        H = r.getMapHeight();
        myId = r.getID();
        cx2 = W - 1;
        cy2 = H - 1;
    }

    public static void startTurn() {
        round = rc.getRoundNum();
        me = rc.getLocation();
        facing = rc.getDirection();
        king = rc.getType() == UnitType.RAT_KING;
        coop = rc.isCooperation();
        globalCheese = rc.getGlobalCheese();
        rawCheese = rc.getRawCheese();
    }

    // ---------------------------------------------------------------- geometry

    public static int idx(int x, int y) { return x + y * W; }

    public static int idx(MapLocation l) { return l.x + l.y * W; }

    public static boolean onMap(int x, int y) { return x >= 0 && y >= 0 && x < W && y < H; }

    public static int cheb(MapLocation a, MapLocation b) {
        return Math.max(Math.abs(a.x - b.x), Math.abs(a.y - b.y));
    }

    /**
     * Side-symmetric handedness for a robot at {@code from} heading along {@code d}: +1 or -1.
     * cross(from - centre, d) is invariant under 180-degree rotation and flips sign under a reflection,
     * so mirrored robots on the two sides make mirrored choices. Exact zero falls back to the
     * parity of the doubled offset, which is also mirror-consistent for rotation.
     */
    public static int hand(MapLocation from, Direction d) {
        int px = 2 * from.x - cx2, py = 2 * from.y - cy2;
        int c = px * d.dy - py * d.dx;
        if (c > 0) return 1;
        if (c < 0) return -1;
        int dot = px * d.dx + py * d.dy;
        return dot >= 0 ? 1 : -1;
    }

    /** Mirror of a location under symmetry s (Sym.ROT/HOR/VER bit). */
    public static int mirX(int x, int s) { return s == Sym.HOR ? x : W - 1 - x; }

    public static int mirY(int y, int s) { return s == Sym.VER ? y : H - 1 - y; }

    public static MapLocation mirror(MapLocation l, int s) { return new MapLocation(mirX(l.x, s), mirY(l.y, s)); }

    /** Deterministic, side-symmetric pseudo-random stream (seeded from mirror-invariant integers). */
    public static int rngState = 1;

    public static void seed(int s) { rngState = s * 0x9E3779B1 + 0x7F4A7C15; if (rngState == 0) rngState = 1; }

    public static int rand(int n) {
        int x = rngState;
        x ^= x << 13; x ^= x >>> 17; x ^= x << 5;
        rngState = x;
        return (x >>> 1) % n;
    }
}
