package pb6;

import battlecode.common.Clock;
import battlecode.common.MapLocation;

/** Cfg.SEALED: cheese in a pocket no rat can walk into. A flood fill from the pile over every tile not known to be a
 *  wall (unseen tiles count as open, dirt is dug; with one symmetry left a wall's twin counts as a wall) that runs out
 *  within cap tiles without reaching us has found a sealed pocket. Its tiles, and their twins under a known symmetry,
 *  get Sym.SEAL, and no rat chases cheese there. learn_your_lesson: four mines sit in 5x5 rooms walled all round, and
 *  rats see their cheese through the walls; 5416 of u4e's 6051 cheese-chase turns against s7d (ly1) went to piles in
 *  those rooms, 42% of all its rat turns. A fill costs up to ~9k bytecodes (a 5x5 room), so it stops whenever fewer
 *  than RESERVE are left and resumes on a later turn. */
final class Seal {
    /** Cfg.SEALED (a field, so tests can set it before the first fill). */
    static int cap = Cfg.SEALED;
    /** Cfg.SEAL_SEEN (a field, so tests can set it). */
    static boolean seenOnly = Cfg.SEAL_SEEN;
    static int[] seen, queue;
    static int stamp;
    /** Verdicts (tile index + 1 open, -(tile index + 1) unknown under seenOnly, and the round), trusted OPEN_TTL
     *  rounds: walls seen later can still close the pocket. */
    static int[] openT, openR;
    static int openN;
    /** The fill left unfinished (start tile index + 1, 0 = none), its queue head and tail, and the round it stopped. */
    static int pend, pendH, pendT, pendRound = -1;
    static boolean pendLeak;
    static final int OPEN_TTL = 20, CACHE = 8, RESERVE = 10000;
    static int fills, found, paused, unknown;
    /** Tests: bytecodes left, counting down one per check (-1 = ask the clock). */
    static int fakeLeft = -1;

    static int left() {
        if (fakeLeft >= 0) return fakeLeft--;
        return Flow.unlimited ? Integer.MAX_VALUE : Clock.getBytecodesLeft();
    }

    /** seenOnly: the pile at tile index s has a fresh unknown verdict (not worth a walk for now). */
    static boolean skipped(int s) {
        if (openT == null) return false;
        for (int k = 0; k < CACHE; k++) if (openT[k] == -(s + 1) && G.round - openR[k] < OPEN_TTL) return true;
        return false;
    }

    /** Cheese at l cannot be reached (see the class comment), or under seenOnly not through tiles seen so far; false
     *  when unsure or short of bytecodes. */
    static boolean sealed(MapLocation l) {
        int[] mem = Sym.mem;
        int W = G.W, H = G.H, s = l.x + l.y * W;
        if ((mem[s] & Sym.SEAL) != 0) return true;
        if (seen == null) { seen = new int[W * H]; queue = new int[cap + 1]; openT = new int[CACHE]; openR = new int[CACHE]; }
        for (int k = 0; k < CACHE; k++) {
            if (G.round - openR[k] >= OPEN_TTL) continue;
            if (openT[k] == s + 1) return false;
            if (openT[k] == -(s + 1)) return true;
        }
        // one fill a turn once one has paused (a second would throw away its queue)
        if (pendRound == G.round) return false;
        int h, t, st;
        boolean leak = false;
        if (pend == s + 1) { h = pendH; t = pendT; st = stamp; leak = pendLeak; }
        else {
            if (left() < RESERVE) return false;
            fills++;
            st = ++stamp;
            seen[s] = st; queue[0] = s;
            h = 0; t = 1;
            pend = s + 1;
        }
        boolean uni = Sym.unique();
        int sym = Sym.mask, me = G.me.x + G.me.y * W, W1 = W - 1, H1 = H - 1;
        boolean fx = sym != Sym.HOR, fy = sym != Sym.VER;
        while (h < t) {
            if (left() < RESERVE) { pendH = h; pendT = t; pendLeak = leak; pendRound = G.round; paused++; return false; }
            int c = queue[h++], cx = c % W, cy = c / W;
            for (int nx = cx - 1; nx <= cx + 1; nx++) {
                if (nx < 0 || nx > W1) continue;
                int tx = fx ? W1 - nx : nx;
                for (int ny = cy - 1; ny <= cy + 1; ny++) {
                    if (ny < 0 || ny > H1) continue;
                    int n = nx + ny * W;
                    if (seen[n] == st) continue;
                    seen[n] = st;
                    int m = uni ? mem[n] | mem[tx + (fy ? H1 - ny : ny) * W] : mem[n];
                    if ((m & Sym.WALL) != 0) continue;
                    if (seenOnly && (m & Sym.SEEN) == 0) { leak = true; continue; }
                    if (n == me || t >= cap) { pend = 0; openT[openN] = s + 1; openR[openN] = G.round; openN = (openN + 1) % CACHE; return false; }
                    queue[t++] = n;
                }
            }
        }
        pend = 0;
        if (leak) { unknown++; openT[openN] = -(s + 1); openR[openN] = G.round; openN = (openN + 1) % CACHE; return true; }
        found++;
        for (int i = 0; i < t; i++) {
            int c = queue[i];
            mem[c] |= Sym.SEAL;
            if (uni) mem[(fx ? W1 - c % W : c % W) + (fy ? H1 - c / W : c / W) * W] |= Sym.SEAL;
        }
        return true;
    }
}
