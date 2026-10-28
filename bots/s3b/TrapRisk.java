package s3b;

import battlecode.common.*;

/**
 * Where enemy rat traps may be. Traps are invisible to us, fire when we enter the 3x3 around them, and a baby can
 * only place one on the three tiles in front of it. So every enemy baby we see marks its three front tiles; a tile
 * is risky when a live mark lies within Chebyshev 1 of it. Marks expire after TTL rounds, and marks around a tile
 * we have stood on since they were made are cleared (a trap there would have fired on us).
 */
public final class TrapRisk {
    static final int TTL = 150;
    static int[] mark;            // round + 1 the tile was last marked, 0 = none
    static int arrived = -1;      // round we reached our current tile
    static MapLocation last;

    static void init() {
        if (mark == null) mark = new int[G.W * G.H];
    }

    /** Enemy positions allies squeaked this round and last (for guard facing). */
    public static final MapLocation[] heardE = new MapLocation[8];
    public static int nHeard;

    /** Called once per turn with the robots we see. */
    public static void observe(RobotInfo[] robots) {
        // allocating one int per tile costs ~2.5 bytecodes each: wait for a turn with room (a newborn's first has none)
        if (mark == null && Flow.left() < 3 * G.W * G.H + Cfg.BC_OPTIONAL) return;
        init();
        nHeard = 0;
        MapLocation me = G.rc.getLocation();
        if (last == null || !last.equals(me)) { last = me; arrived = G.round; }
        for (RobotInfo r : robots) {
            if (r.team != G.them || r.type != UnitType.BABY_RAT) continue;
            Direction f = r.direction;
            if (f == null || f == Direction.CENTER) continue;
            put(r.location.add(f));
            put(r.location.add(f.rotateLeft()));
            put(r.location.add(f.rotateRight()));
        }
        if (Cfg.TRAP_SHARE) heard(G.rc.readSqueaks(G.round - 1));
        if (Cfg.TRAP_SHARE) heard(G.rc.readSqueaks(G.round));
        // clear marks we have proven safe: anything within 1 of our tile marked before we arrived
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++) {
                int x = me.x + dx, y = me.y + dy;
                if (!G.onMap(x, y)) continue;
                int i = x + G.W * y;
                if (mark[i] != 0 && mark[i] - 1 < arrived) mark[i] = 0;
            }
    }

    /** Marks from allies' SQ_TRAP squeaks: an enemy at (x, y) facing DIRS[extra]. */
    static void heard(Message[] ms) {
        for (Message m : ms) {
            if (nHeard >= heardE.length) return;   // bytecode cap: the nearest few squeaks are enough
            int c = m.getBytes();
            if (Comms.sqType(c) != Comms.SQ_TRAP) continue;
            int d = (c >> 12) & 7;
            MapLocation e = new MapLocation(Comms.sqX(c), Comms.sqY(c));
            heardE[nHeard++] = e;
            Direction f = G.DIRS[d];
            put(e.add(f));
            put(e.add(f.rotateLeft()));
            put(e.add(f.rotateRight()));
        }
    }

    static void put(MapLocation l) {
        if (G.onMap(l.x, l.y)) mark[l.x + G.W * l.y] = G.round + 1;
    }

    /** True when entering l could fire a trap we suspect. */
    public static boolean risky(MapLocation l) {
        if (mark == null) return false;
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++) {
                int x = l.x + dx, y = l.y + dy;
                if (!G.onMap(x, y)) continue;
                int m = mark[x + G.W * y];
                if (m != 0 && G.round - (m - 1) <= TTL) return true;
            }
        return false;
    }

    /** Avoid-mask bits for the risky neighbours of me (0 when every neighbour is risky). */
    public static int mask(MapLocation me) {
        if (mark == null) return 0;
        // one pass over the 5x5 around us: a live mark at offset (dx, dy) taints every neighbour within Chebyshev 1
        int m = 0, w = G.W, x0 = me.x, y0 = me.y, r = G.round - TTL;
        for (int dy = -2; dy <= 2; dy++) {
            int y = y0 + dy;
            if (y < 0 || y >= G.H) continue;
            for (int dx = -2; dx <= 2; dx++) {
                int x = x0 + dx;
                if (x < 0 || x >= w) continue;
                int k = mark[x + w * y];
                if (k == 0 || k - 1 < r) continue;
                m |= NEAR[(dx + 2) + 5 * (dy + 2)];
            }
        }
        return m == 0xFF ? 0 : m;
    }

    /** NEAR[offset] = bits of the 8 neighbour directions within Chebyshev 1 of that offset in the 5x5. */
    static final int[] NEAR = new int[25];
    static {
        for (int dy = -2; dy <= 2; dy++)
            for (int dx = -2; dx <= 2; dx++) {
                int b = 0;
                for (int i = 0; i < 8; i++) {
                    int nx = G.DIRS[i].dx, ny = G.DIRS[i].dy;
                    if (Math.abs(nx - dx) <= 1 && Math.abs(ny - dy) <= 1) b |= 1 << i;
                }
                NEAR[(dx + 2) + 5 * (dy + 2)] = b;
            }
    }
}
