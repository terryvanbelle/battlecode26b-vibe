package yb7;

import battlecode.common.MapLocation;

/**
 * Map memory (one int per tile) and symmetry inference.
 * Symmetry candidates are eliminated only by static features (walls, mines); dirt changes during play.
 */
public final class Sym {
    public static final int ROT = 1, HOR = 2, VER = 4;
    public static final int SEEN = 1, WALL = 2, DIRT = 4, MINE = 8;
    /** Expectation bits (s << 4) on an unseen tile: it is the s-twin of a listed mine, so seeing it mineless rules out s. */
    public static final int EXP = 112;
    /** A tile under one of our kings' 3x3 bodies (Nav.markKings): flow paths go around it. */
    public static final int KING = 128;
    /** A tile some cat's 2x2 was seen on (King.markLane): cats patrol fixed waypoint loops. */
    public static final int CAT = 256;
    /** A tile a robot held on our flow path (Baby's scout walk, PIPE_HELD): searches go around it for a while. */
    public static final int HELD = 512;

    public static int[] mem;
    public static int mask = ROT | HOR | VER;
    /** Count of eliminations rejected because they would have emptied the mask (must stay 0). */
    public static int contradictions;

    public static void init() { mem = new int[G.W * G.H]; }

    public static boolean known(int i) { return (mem[i] & SEEN) != 0; }

    /** Record a sensed tile, and rule out the symmetries its mirror tiles (or an expected mine) contradict. */
    public static void record(int x, int y, boolean wall, boolean dirt, boolean mine) {
        int i = x + y * G.W;
        int v = SEEN | (wall ? WALL : 0) | (dirt ? DIRT : 0) | (mine ? MINE : 0);
        int old = mem[i];
        mem[i] = v | (old & (EXP | KING | CAT | HELD));
        if (Baby.bfOn && ((old ^ v) & (WALL | DIRT)) != 0) BitFlow.mark(x, y, v);
        if ((old & SEEN) != 0 || mask == ROT || mask == HOR || mask == VER) return;
        if (!mine && (old & EXP) != 0) { int e = (old >> 4) & mask; if (e != 0) eliminate(e); }
        int stat = v & (WALL | MINE);
        if ((mask & ROT) != 0) check(stat, G.W - 1 - x, G.H - 1 - y, ROT);
        if ((mask & HOR) != 0) check(stat, x, G.H - 1 - y, HOR);
        if ((mask & VER) != 0) check(stat, G.W - 1 - x, y, VER);
    }

    private static void check(int stat, int mx, int my, int s) {
        int m = mem[mx + my * G.W];
        if ((m & SEEN) == 0) return;
        if ((m & (WALL | MINE)) != stat) eliminate(s);
    }

    public static void eliminate(int s) {
        int next = mask & ~s;
        if (next == 0) { contradictions++; return; }
        mask = next;
    }

    /** Apply a mask learned from a teammate. */
    public static void merge(int m) {
        int next = mask & m;
        if (next == 0) { contradictions++; return; }
        mask = next;
    }
}
