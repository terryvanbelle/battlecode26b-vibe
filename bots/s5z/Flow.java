package s5z;

import battlecode.common.*;

/**
 * Dig-aware path planning for stuck rats: A* from here to a target over remembered terrain
 * (unknown = open, dirt = DIG_COST, wall = blocked), built with spare bytecode over one or more
 * turns, then followed step by step (digging dirt on the path). Requested only after greedy/bug
 * navigation stalls, so open maps pay nothing. g[] entries are gen*SPAN + cost, so a new search
 * needs no reset pass; Dial buckets on f = g + chebyshev (f grows by at most DIG_COST + 1 per step).
 */
public final class Flow {
    static final int SPAN = 1 << 16, RING = 1024, RMASK = RING - 1, NB = 8, BMASK = NB - 1;
    static final int[] DX = {0, 1, 1, 1, 0, -1, -1, -1}, DY = {1, 1, 0, -1, -1, -1, 0, 1};
    /** Neighbour order of the search: DX/DY in our side frame (Cfg.SYM_FLOW), so a mirrored rat's search expands
     *  mirrored neighbours in the same order and breaks ties between equal paths the mirrored way. */
    static int[] ox = DX, oy = DY;

    /** Side frame (signs of the home king's offset from the centre), set once at birth. */
    static void frame(int fu, int fv) { if (Cfg.SYM_FLOW) setFrame(fu, fv); }

    static void setFrame(int fu, int fv) {
        ox = new int[8];
        oy = new int[8];
        for (int k = 0; k < 8; k++) { ox[k] = DX[k] * fu; oy[k] = DY[k] * fv; }
    }
    static int[] g, par;
    static int gen;
    static MapLocation tgt;
    static int tIdx, tx, ty, base, sIdx;
    static boolean building;
    /** Tile bits a path may not cross (the target tile excepted): walls, and our kings' bodies (Cfg.KING_BLOCK, KING_FLOW) unless
     *  the target is inside one. */
    static int block = Sym.WALL;
    /** Round the current path was completed, or -1. */
    static int builtRound = -1;
    static int[] path;
    static int plen, ppos;
    static int[][] bucket;
    static int[] head = new int[NB], tail = new int[NB];
    static int curF, pending, held;
    public static int builds, steps, done, lastTurns, fails;
    /** Target and tile count of the last completed path (kept after the path is dropped). */
    static MapLocation lastTgt;
    static int lastLen;
    /** Debug counts of how paths and searches ended: 0 reached the end, 1 held by a robot, 2 wall on the path,
     *  3 drifted off it, 4 replaced while ready (new target), 5 search replaced before it finished. */
    public static final int[] ends = new int[6];
    static int startRound, failRound;
    static MapLocation failTgt;
    /** Tests run outside the engine, where Clock reports 0 bytecodes left. */
    public static boolean unlimited;

    static int left() { return unlimited ? Integer.MAX_VALUE : Clock.getBytecodesLeft(); }

    /** True when a complete, fresh path toward t exists. */
    public static boolean ready(MapLocation t) {
        return builtRound >= 0 && tgt != null && G.cheb(tgt, t) <= 1 && G.round - builtRound <= Cfg.FLOW_TTL;
    }

    /** True while a search toward t is running. */
    public static boolean searching(MapLocation t) {
        if (building && G.round - startRound > Cfg.FLOW_MAX_TURNS) {
            // too slow (little spare bytecode, or a huge open search): give up so the rat moves again
            building = false;
            fails++;
            failTgt = tgt;
            failRound = G.round;
        }
        return building && tgt != null && G.cheb(tgt, t) <= 1;
    }

    /** Ask for a path toward t (no-op if one is ready or already being built toward t). */
    public static void request(MapLocation t) {
        if (tgt != null && G.cheb(tgt, t) <= 1 && (building || ready(t))) return;
        if (failTgt != null && G.cheb(failTgt, t) <= 1 && G.round - failRound < Cfg.FLOW_RETRY) return;
        if (building) ends[5]++;
        else if (tgt != null && builtRound >= 0 && G.round - builtRound <= Cfg.FLOW_TTL) ends[4]++;
        tgt = t;
        tIdx = G.idx(t);
        block = (Cfg.KING_BLOCK || Cfg.KING_FLOW) && !Nav.atKing(t) ? Sym.WALL | Sym.KING : Sym.WALL;
        tx = t.x; ty = t.y;
        builtRound = -1;
        building = true;
        gen++;
        if (gen >= (Integer.MAX_VALUE / SPAN) - 1) gen = 1;
        base = gen * SPAN;
        for (int b = 0; b < NB; b++) { head[b] = 0; tail[b] = 0; }
        pending = 0;
        builds++;
        startRound = G.round;
        if (g != null && bucket != null) seed();
    }

    static void seed() {
        int s = sIdx = G.idx(G.me);
        g[s] = base;
        curF = Math.max(Math.abs(G.me.x - tx), Math.abs(G.me.y - ty));
        int b = curF & BMASK;
        bucket[b][tail[b] & RMASK] = s;
        tail[b]++;
        pending = 1;
    }

    /** Continue the search while more than reserve bytecodes are left. */
    public static void work(int reserve) {
        if (!building) return;
        int n = G.W * G.H;
        if (g == null) {
            if (left() < 2 * n + reserve) return;
            g = new int[n];
            par = new int[n];
            if (bucket != null) seed();
        }
        if (bucket == null) {
            if (left() < NB * RING + 1024 + reserve) return;
            bucket = new int[NB][RING];
            path = new int[1024];
            seed();
        }
        int[] gg = g, pp = par, mem = Sym.mem, OX = ox, OY = oy;
        int W = G.W, H = G.H, bs = base, dig = Cfg.DIG_COST, X = tx, Y = ty, blk = block;
        while (pending > 0) {
            if (left() < reserve) return;
            int b = curF & BMASK;
            if (head[b] == tail[b]) { curF++; continue; }
            int u = bucket[b][head[b] & RMASK];
            head[b]++;
            pending--;
            int ux = u % W, uy = u / W, gu = gg[u] - bs;
            if (gu + Math.max(Math.abs(ux - X), Math.abs(uy - Y)) != curF) continue; // stale entry
            if (u == tIdx) {
                // backtrack + reverse costs ~40 bc per tile and the path has at most gu + 1 tiles: wait a turn if short
                if (left() < reserve + 40 * (gu + 2)) { head[b]--; pending++; return; }
                finish();
                return;
            }
            for (int k = 0; k < 8; k++) {
                int vx = ux + OX[k], vy = uy + OY[k];
                if (vx < 0 || vy < 0 || vx >= W || vy >= H) continue;
                int v = vx + vy * W, m = mem[v];
                if ((m & blk) != 0 && v != tIdx) continue;
                int ng = gu + ((m & Sym.DIRT) != 0 ? dig : 1);
                int gv = gg[v];
                if (gv >= bs && gv - bs <= ng) continue;
                gg[v] = bs + ng;
                pp[v] = k;
                int f = ng + Math.max(Math.abs(vx - X), Math.abs(vy - Y));
                int fb = f & BMASK;
                bucket[fb][tail[fb] & RMASK] = v;
                tail[fb]++;
                pending++;
            }
        }
        // target unreachable over what we know
        building = false;
        fails++;
        failTgt = tgt;
        failRound = G.round;
    }

    static void finish() {
        int W = G.W, s = sIdx, v = tIdx, len = 0;
        // walk parents back from the target, then reverse
        while (v != s && len < path.length - 1) {
            path[len++] = v;
            int k = par[v];
            v = v - ox[k] - oy[k] * W;
        }
        path[len++] = s;
        for (int i = 0, j = len - 1; i < j; i++, j--) { int t = path[i]; path[i] = path[j]; path[j] = t; }
        plen = len;
        ppos = 0;
        lastTgt = tgt;
        lastLen = len;
        building = false;
        builtRound = G.round;
        done++;
        lastTurns = G.round - startRound;
    }

    static void drop() { builtRound = -1; tgt = null; }

    /** The way to t is known to be long: the last path toward t was longer than 2 * Chebyshev distance + 2 tiles, or
     *  the last failed search was toward t. */
    static boolean detour(MapLocation t) {
        if (failTgt != null && G.cheb(failTgt, t) <= 1 && G.round - failRound < Cfg.FLOW_RETRY) return true;
        return lastTgt != null && G.cheb(lastTgt, t) <= 1 && lastLen - 1 > 2 * G.cheb(G.me, t) + 2;
    }

    /** A free step that lands beside one of the next few path tiles after the blocked one (the farthest wins; ties go
     *  by G.hand so mirrored rats pick mirrored sides), or null. follow() rejoins the path from there next turn. */
    static Direction sidestep(int nxi) {
        RobotController rc = G.rc;
        int hi = Math.min(plen - 1, nxi + 3), bs = -1;
        Direction best = null;
        for (Direction d : G.DIRS) {
            if ((Nav.avoid & (1 << d.ordinal())) != 0 || !rc.canMove(d)) continue;
            MapLocation l = G.me.add(d);
            int s = -1;
            for (int j = hi; j > nxi; j--) {
                int p = path[j], dx = p % G.W - l.x, dy = p / G.W - l.y;
                if (Math.abs(dx) <= 1 && Math.abs(dy) <= 1) { s = j * 4 - dx * dx - dy * dy; break; }
            }
            if (s < 0) continue;
            s = s * 2 + (G.hand(G.me, d) > 0 ? 1 : 0);
            if (s > bs) { bs = s; best = d; }
        }
        return best;
    }

    /**
     * One step along the path toward t: move to the next tile, or dig it when it is dirt.
     * Returns false (caller falls back to greedy) when no path is ready or we have left it.
     */
    public static boolean follow(MapLocation t) throws GameActionException {
        if (!ready(t)) return false;
        int me = G.idx(G.me);
        int nxi = ppos + 1;
        if (path[ppos] != me) {
            // we drifted (moved while the search ran, or sidestepped): rejoin at the farthest path tile next to us
            int hi = Math.min(plen, ppos + Cfg.FLOW_REJOIN), found = -1;
            for (int i = ppos; i < hi; i++) {
                int p = path[i];
                if (Math.abs(p % G.W - G.me.x) <= 1 && Math.abs(p / G.W - G.me.y) <= 1) found = i;
            }
            if (found < 0) { ends[3]++; drop(); request(t); return false; }
            if (path[found] == me) { ppos = found; nxi = found + 1; }
            else { ppos = Math.max(0, found - 1); nxi = found; }   // found 0: beside the start tile (ppos -1 threw every turn)
        }
        if (nxi >= plen) { ends[0]++; drop(); return false; }
        int nx = path[nxi];
        MapLocation n = new MapLocation(nx % G.W, nx / G.W);
        if ((Sym.mem[nx] & block) != 0) { ends[2]++; drop(); return false; } // newly seen wall (or a king moved) on the path
        RobotController rc = G.rc;
        Direction d = G.me.directionTo(n);
        if ((Sym.mem[nx] & Sym.DIRT) != 0) {
            if (G.globalCheese + G.rawCheese < 5 || !rc.isActionReady()) return true; // wait for the dig
            if (rc.getDirection() != d && rc.canTurn(d)) { rc.turn(d); G.facing = d; }
            if (rc.canRemoveDirt(n)) {
                rc.removeDirt(n);
                Nav.digs++;
                Sym.mem[nx] &= ~Sym.DIRT;
            }
            return true;
        }
        if ((Nav.avoid & (1 << d.ordinal())) != 0 || !rc.canMove(d)) {
            // blocked by a robot: hold briefly, then step around it on the path (FLOW_JAM) or let greedy route around it
            if (++held > Cfg.FLOW_HOLD) {
                if (Cfg.FLOW_JAM && held <= Cfg.FLOW_JAM_HOLD) {
                    Direction s = sidestep(nxi);
                    if (s != null) Nav.step(s);
                    return true;
                }
                held = 0; ends[1]++; drop(); return false;
            }
            return true;
        }
        held = 0;
        steps++;
        if (Nav.step(d)) ppos = nxi;
        return true;
    }
}
