package u5x;

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
    /** A tile under one of our kings' 3x3 bodies (Nav.markKings, Cfg.KING_BLOCK): paths and bug walks go around it. */
    public static final int KING = 128;
    /** A tile some cat's 2x2 was seen on (King.markLane, Cfg.CAT_LANE): cats patrol fixed waypoint loops. */
    public static final int CAT = 256;
    /** A tile a robot held on our flow path (Flow.markHeld, Cfg.FLOW_HELD): searches go around it for a while. */
    public static final int HELD = 512;
    /** A tile in a sealed pocket (Seal.sealed, Cfg.SEALED): its cheese is never chased. */
    public static final int SEAL = 1024;
    /** Mine spacing the map validator enforces (GameConstants.MIN_CHEESE_MINE_SPACING_SQUARED). */
    public static final int MINE_SPACING2 = 25;

    public static int[] mem;
    public static int mask = ROT | HOR | VER;
    /** Count of eliminations rejected because they would have emptied the mask (must stay 0). */
    public static int contradictions;

    public static void init() { mem = new int[G.W * G.H]; fillMask = ROT | HOR | VER; fillAt = 0; fillDirty = false; if (Cfg.BFLOW) BitFlow.init(); }

    public static boolean known(int i) { return (mem[i] & SEEN) != 0; }

    /** Record a sensed tile; returns true when the symmetry mask shrank. */
    public static boolean record(int x, int y, boolean wall, boolean dirt, boolean mine) {
        int i = x + y * G.W;
        int v = SEEN | (wall ? WALL : 0) | (dirt ? DIRT : 0) | (mine ? MINE : 0);
        int old = mem[i];
        mem[i] = v | (old & (EXP | KING | CAT | HELD | SEAL));
        if (Cfg.BFLOW && ((old ^ v) & (WALL | DIRT)) != 0) BitFlow.mark(x, y, v);
        if (Cfg.SYM_FILL > 0 && (old & SEEN) == 0 && (v & (WALL | DIRT)) != 0) fillDirty = true;
        if ((old & SEEN) != 0 || mask == ROT || mask == HOR || mask == VER) return false;
        int before = mask;
        if (!mine && (old & EXP) != 0) { int e = (old >> 4) & mask; if (e != 0) eliminate(e); }
        int stat = v & (WALL | MINE);
        if ((mask & ROT) != 0) check(stat, G.W - 1 - x, G.H - 1 - y, ROT);
        if ((mask & HOR) != 0) check(stat, x, G.H - 1 - y, HOR);
        if ((mask & VER) != 0) check(stat, G.W - 1 - x, y, VER);
        return mask != before;
    }

    /** Cfg.SYM_FILL: a seen wall or dirt tile (x, y) with terrain bits t is copied onto each unseen twin (not marked
     *  SEEN, so sensing it later overwrites the guess) on which every live symmetry agrees: the twin's own twin under
     *  each live symmetry is a seen tile with the same terrain. Paths stop crossing the enemy half's unseen walls. */
    static void fill(int x, int y, int t) {
        int m = mask, W = G.W, H = G.H;
        for (int s = ROT; s <= VER; s <<= 1) {
            if ((m & s) == 0) continue;
            int tx = s == HOR ? x : W - 1 - x, ty = s == VER ? y : H - 1 - y, ti = tx + ty * W, tm = mem[ti];
            if ((tm & SEEN) != 0 || (tm & (WALL | DIRT)) == t) continue;
            boolean ok = true;
            for (int s2 = ROT; s2 <= VER && ok; s2 <<= 1) {
                if ((m & s2) == 0 || s2 == s) continue;
                int u = mem[(s2 == HOR ? tx : W - 1 - tx) + (s2 == VER ? ty : H - 1 - ty) * W];
                ok = (u & SEEN) != 0 && (u & (WALL | DIRT)) == t;
            }
            if (ok) mem[ti] = (tm & ~(WALL | DIRT)) | t;
        }
    }

    /** Cfg.SYM_FILL: the end-of-turn scan of remembered tiles that fills their twins, SYM_FILL tiles a turn. A pass
     *  starts when the mask shrinks or after a wall or dirt tile was first seen (fillDirty); sensing itself only sets the
     *  flag, so a turn near the bytecode limit pays nothing extra (u5x's first screen xy1 overran 8 times, in fights). */
    static int fillMask = ROT | HOR | VER, fillAt;
    static boolean fillDirty;

    public static boolean fillDue() { return fillAt < mem.length || fillDirty || mask != fillMask; }
    /** Bytecodes backfill leaves for the rest of the turn (16 tiles cost at most ~16 fills, ~2.4k). */
    static final int FILL_LEFT = 6000;

    public static void backfill(int n) {
        if (mask != fillMask) { fillMask = mask; fillAt = 0; fillDirty = false; }
        else if (fillAt >= mem.length) { if (!fillDirty) return; fillAt = 0; fillDirty = false; }
        int[] mm = mem;
        int end = Math.min(mm.length, fillAt + n), W = G.W;
        int i = fillAt;
        for (; i < end; i++) {
            if ((i & 15) == 0 && Flow.left() < FILL_LEFT) break;
            int v = mm[i];
            if ((v & SEEN) != 0 && (v & (WALL | DIRT)) != 0) fill(i % W, i / W, v & (WALL | DIRT));
        }
        fillAt = i;
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

    /** Cfg.CHIRAL_SYM: the symmetry the engine's sense order of ls shows (ROT, HOR or VER), or 0 for the natural
     *  order (chirality 0) or too few tiles in ls to tell. The natural order is x ascending, then y ascending. */
    public static int chiral(MapLocation[] ls) {
        int xr = 0, yr = 0;
        for (int i = 1; i < ls.length && (xr == 0 || yr == 0); i++) {
            MapLocation a = ls[i - 1], b = ls[i];
            if (a.x == b.x) { if (yr == 0) yr = b.y > a.y ? 1 : -1; }
            else if (xr == 0) xr = b.x > a.x ? 1 : -1;
        }
        if (xr == 0 || yr == 0) return 0;
        return xr < 0 ? (yr < 0 ? ROT : VER) : (yr < 0 ? HOR : 0);
    }

    /** Apply a mask learned from a teammate. */
    public static void merge(int m) {
        int next = mask & m;
        if (next == 0) { contradictions++; return; }
        mask = next;
    }

    /** Team-list entries already checked by listCheck (the list is append-only). */
    public static int listDone;

    /** Mine-list evidence for the entries listed since the last call: an s-twin seen without a mine rules out s;
     *  an unseen one is marked for record(). With spacing (kings), an s-twin closer than MINE_SPACING2 to a listed
     *  mine (itself included) rules out s too. */
    public static void listCheck(MapLocation[] mines, int n, boolean spacing) {
        for (; listDone < n; listDone++) {
            if (unique()) { listDone = n; return; }
            MapLocation m = mines[listDone];
            for (int s = ROT; s <= VER; s <<= 1) {
                if ((mask & s) == 0) continue;
                int tx = G.mirX(m.x, s), ty = G.mirY(m.y, s);
                if (tx == m.x && ty == m.y) continue;
                int t = tx + ty * G.W, tm = mem[t];
                if ((tm & SEEN) != 0) { if ((tm & MINE) == 0) { eliminate(s); continue; } }
                else mem[t] = tm | s << 4;
                if (!spacing) continue;
                for (int j = 0; j < n; j++) {
                    int dx = mines[j].x - tx, dy = mines[j].y - ty, d = dx * dx + dy * dy;
                    if (d > 0 && d < MINE_SPACING2) { eliminate(s); break; }
                }
            }
        }
    }

    public static boolean unique() { return mask == ROT || mask == HOR || mask == VER; }

    public static int count() { return (mask & 1) + ((mask >> 1) & 1) + ((mask >> 2) & 1); }
}
