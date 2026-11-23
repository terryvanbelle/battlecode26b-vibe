package ye8;

import battlecode.common.*;

/**
 * Shared-array layout (kings write, everyone reads; values 0..1023) and squeak encoding.
 * Locations are stored as x+1 / y+1 so 0 means empty; stamps are round%1024 (Comms.stamp, never 0).
 */
public final class Comms {
    /** The symmetry mask still possible (Sym.ROT | HOR | VER bits). */
    public static final int SYM = 1;
    /** Open king-formation site: x+1, y+1 and stamp (0 = none). */
    public static final int FORM_X = 0, FORM_Y = 23, FORM_T = 63;
    /** King slots, 3 ints each: x+1, y+1, heartbeat stamp. */
    public static final int KING0 = 2, KING_SLOTS = 5;
    /** War alert, 3 ints: (x+1) | threat << 6 (the enemy rats within Cfg.KING_THREAT_D2 of the alerting king, at
     *  most 15), y+1, stamp. */
    public static final int ALERT = 17;
    /** The team mine list, 2 ints each (x+1, y+1), filled from slot 0. */
    public static final int MINE0 = 24, MINE_SLOTS = 16;
    /** The two latest cat sightings, 3 ints each: x+1, y+1, stamp. */
    public static final int CAT0 = 56, CAT_SLOTS = 2;
    /** Spendable surplus / 4 (the kings' Econ.surplus projection, clamped to 0..1023). */
    public static final int BUDGET = 62;

    /** Squeak types (top 4 bits): a mine, the symmetry mask, a cat, a spawn's start order (King.rrNext), and a
     *  rescue formation (Cfg.CASH_END: site x, y; extra tag << 10 | start). */
    public static final int SQ_MINE = 1, SQ_SYM = 2, SQ_CAT = 4, SQ_START = 6;
    public static final int SQ_CASH = 9;

    /** A squeak: type << 28 | extra << 12 | x << 6 | y. */
    public static int squeak(int type, int x, int y, int extra) {
        return (type << 28) | (extra << 12) | (x << 6) | y;
    }

    public static int sqType(int m) { return (m >>> 28) & 15; }

    public static int sqX(int m) { return (m >> 6) & 63; }

    public static int sqY(int m) { return m & 63; }

    public static int sqExtra(int m) { return (m >> 12) & 0xFFFF; }

    // ------------------------------------------------------------------ readers

    /** Shared-array reads and writes never throw (an index is always valid); a write that changes nothing is
     *  skipped. */
    public static int read(int i) {
        try { return G.rc.readSharedArray(i); } catch (GameActionException e) { return 0; }
    }

    public static void write(int i, int v) {
        try { if (G.rc.readSharedArray(i) != v) G.rc.writeSharedArray(i, v); } catch (GameActionException e) { }
    }

    /** A nonzero stamp at most maxAge rounds old. */
    public static boolean fresh(int stamp, int maxAge) {
        if (stamp == 0) return false;
        int age = ((G.round & 1023) - stamp + 1024) & 1023;
        return age <= maxAge;
    }

    /** This round as a stamp: round % 1024, with 0 mapped to 1023 so a stamp is never empty. */
    public static int stamp() { int s = G.round & 1023; return s == 0 ? 1024 - 1 : s; }

    /** Live king locations from the array (heartbeat within 2 rounds). */
    public static MapLocation[] kings = new MapLocation[KING_SLOTS];
    public static int numKings;

    public static void readKings() {
        numKings = 0;
        for (int k = 0; k < KING_SLOTS; k++) {
            int b = KING0 + 3 * k;
            int x = read(b);
            if (x == 0) continue;
            if (!fresh(read(b + 2), 2)) continue;
            kings[numKings++] = new MapLocation(x - 1, read(b + 1) - 1);
        }
    }

    /** The live king nearest from (dist2), or null. */
    public static MapLocation nearestKing(MapLocation from) {
        MapLocation best = null;
        int bd = Integer.MAX_VALUE;
        for (int k = 0; k < numKings; k++) {
            int d = from.distanceSquaredTo(kings[k]);
            if (d < bd) { bd = d; best = kings[k]; }
        }
        return best;
    }

    public static int numMines;
    public static MapLocation[] mines = new MapLocation[MINE_SLOTS];

    /** Read the team mine list into mines[0..numMines). */
    public static void readMines() {
        numMines = 0;
        for (int k = 0; k < MINE_SLOTS; k++) {
            int x = read(MINE0 + 2 * k);
            if (x == 0) break;
            mines[numMines++] = new MapLocation(x - 1, read(MINE0 + 2 * k + 1) - 1);
        }
    }

    public static boolean hasMine(int x, int y) {
        for (int k = 0; k < numMines; k++) if (mines[k].x == x && mines[k].y == y) return true;
        return false;
    }

    /** King only: append a mine if new. */
    public static void addMine(int x, int y) {
        if (hasMine(x, y) || numMines >= MINE_SLOTS) return;
        write(MINE0 + 2 * numMines, x + 1);
        write(MINE0 + 2 * numMines + 1, y + 1);
        mines[numMines++] = new MapLocation(x, y);
    }

    /** The alerting king's location (ALERT's x carries the threat count in its upper bits), or null. */
    public static MapLocation alertLoc() {
        int v = read(ALERT);
        if (v == 0) return null;
        return new MapLocation((v & 63) - 1, read(ALERT + 1) - 1);
    }

    public static int alertThreat() { return read(ALERT) >> 6; }

    /** The location stored at i, i+1, or null when empty. */
    public static MapLocation readLoc(int i) {
        int x = read(i);
        if (x == 0) return null;
        return new MapLocation(x - 1, read(i + 1) - 1);
    }
}
