package xe2;

import battlecode.common.*;

/**
 * Breadcrumbs home: the loop-free chain of tiles this baby has walked since it was spawned beside a king. Stepping
 * back onto a tile of the chain cuts off the loop after it, so the chain is always a simple, walked path from the
 * spawn tile to here: no walls on it, whatever the map. A carrier whose delivery stalls (Cfg.TRAIL) walks it back,
 * each turn to the free neighbour earliest on the chain, which also cuts corners where the chain passes close to
 * itself. A jump of more than one tile (carried, thrown) starts a new chain where the rat lands.
 *
 * The chain is recorded at the start of the turn in a ledgered hook (a record is O(1): a tile's entry carries the
 * chain generation, and a cut or a new chain only moves n or bumps gen). The arrays are allocated at the end of a
 * turn (alloc, after Flow.work and every budget read of the base), since an allocation costs its length, 2·W·H, more
 * than the ledger may credit. z7s recorded at the end of the turn and skipped the record when under 1,500 bytecodes
 * were left; on fullofsecrets a gatherer's skipped records at a mine (16,000 used) cut its chain there, and its walk
 * home ended at the mine.
 */
public final class Trail {
    /** Chain tiles in walk order (tiles[0] = start), and tile -> gen << 16 | chain position + 1. */
    static int[] tiles, at;
    public static int n, gen;

    /** Extend the chain to G.me (once a turn, at its start); nothing until alloc has run. */
    public static void record() {
        if (at != null) add(G.me.x, G.me.y);
    }

    /** Allocate the chain arrays at the end of a turn whose real bytecodes left cover them (RobotPlayer, after
     *  Flow.work: no budget read follows). */
    public static void alloc() {
        if (at != null) return;
        int c = G.W * G.H;
        if (Clock.getBytecodesLeft() < 2 * c + Cfg.TRAIL_ALLOC_LEFT) return;
        at = new int[c];
        tiles = new int[c];
        gen = 1;
    }

    static void add(int x, int y) {
        int W = G.W, i = x + y * W;
        if (n > 0) {
            int l = tiles[n - 1];
            if (l == i) return;
            if (Math.abs(l % W - x) > 1 || Math.abs(l / W - y) > 1) { n = 0; gen++; }
        }
        int p = pos(i);
        if (p > 0) n = p;
        else { tiles[n++] = i; at[i] = gen << 16 | n; }
    }

    /** Tile i's chain position + 1 (0 = not on the chain). */
    static int pos(int i) {
        int v = at[i], p = v & 0xFFFF;
        return v >>> 16 == gen && p <= n && tiles[p - 1] == i ? p : 0;
    }

    static final int MOVED = 0, HELD = 1, END = 2;

    /** One step back along the chain, to the free neighbour (Nav.avoid respected) earliest on it, if that is earlier
     *  than here: MOVED; HELD when every earlier neighbour is occupied (or movement is not ready); END when no
     *  neighbour is earlier (the start of the chain, or no chain). */
    static int back() throws GameActionException {
        if (at == null || n < 2) return END;
        RobotController rc = G.rc;
        int W = G.W, x = G.me.x, y = G.me.y, p0 = pos(x + y * W), best = p0 == 0 ? Integer.MAX_VALUE : p0;
        boolean earlier = false;
        Direction bd = null;
        for (Direction d : G.DIRS) {
            int vx = x + d.dx, vy = y + d.dy;
            if (vx < 0 || vy < 0 || vx >= W || vy >= G.H) continue;
            int p = pos(vx + vy * W);
            if (p == 0 || p >= best) continue;
            earlier = true;
            if ((Nav.avoid & (1 << d.ordinal())) != 0 || !rc.canMove(d)) continue;
            best = p;
            bd = d;
        }
        if (bd == null) return earlier ? HELD : END;
        return Nav.step(bd) ? MOVED : HELD;
    }
}
