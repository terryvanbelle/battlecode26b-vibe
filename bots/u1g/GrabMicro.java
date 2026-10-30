package u1g;

import battlecode.common.*;

/**
 * Cfg.GRAB_MICRO: grab-aware movement for the rat war. A rat grabs an adjacent enemy it can see (it faces it) when
 * that enemy cannot see the grabber (the grabber stands in its blind spot) or is weaker. Field replays (3934-3937)
 * showed our rats ending turns where enemies could step into their blind spots: with 0, 1, 2 and 3+ such enemies
 * 0.1%, 6-20%, 8-32% and 14-65% of them were grabbed before their next turn, while one-step grabs went unused.
 *
 * Every reachable end tile (stay or one step) and final facing is scored: a grab there (+GRAB), a bite (+BITE, more
 * when it makes the target grabbable by us or ends its health edge, or kills), and minus THREAT for each enemy that can
 * step next to that tile into our blind spot (or anywhere next to it, when it is healthier). The rat acts when the
 * best option beats standing still by MARGIN; otherwise the normal move runs with the clearly worse steps avoided.
 *
 * Tiles are bits of a 7x7 window around us: bit (ox + 3) + 8 * (oy + 3), column 7 a guard against row wrap.
 */
public final class GrabMicro {
    static final int GRAB = 100, THREAT = Cfg.GRAB_THREAT, BITE = Cfg.GRAB_BITE, FLIP = BITE + 15, KILL = BITE + 40,
                     MARGIN = Cfg.GRAB_MARGIN;
    static final int[] DX = {0, 1, 1, 1, 0, -1, -1, -1}, DY = {1, 1, 0, -1, -1, -1, 0, 1};
    static final long CBIT = 1L << 27;
    /** The 3x3 around our tile, and its 8 neighbours. */
    static final long K3 = (7L << 18) | (7L << 26) | (7L << 34), ADJ = K3 & ~CBIT;
    /** Tiles within Chebyshev 2 of us: the only ones next to an end tile. */
    static final long INNER = 0x00003E3E3E3E3E00L;
    static final int MAXE = 8;
    static final int[] ex = new int[MAXE], ey = new int[MAXE], ehp = new int[MAXE], edir = new int[MAXE];
    static final boolean[] ecan = new boolean[MAXE];
    static final RobotInfo[] er = new RobotInfo[MAXE];
    static final long[] r0 = new long[MAXE], r1 = new long[MAXE], eb = new long[MAXE], rt = new long[MAXE];
    static final boolean[] gr = new boolean[MAXE];
    /** The best option: end tile (0-7 a step in G.DIRS order, 8 stay), facing, the enemy grabbed or bitten (-1). */
    static int bestD, bestF, grabI, biteI, bestS, curS;
    /** Best score per end tile (Integer.MIN_VALUE: not reachable). */
    static final int[] dirBest = new int[9];
    /** The round the micro took this turn's movement (Baby.moveDecision then stands down). */
    public static int round = -1;
    public static int acts, steps, grabs, avoids;

    static int b(int ox, int oy) { return (ox + 3) + 8 * (oy + 3); }

    static long bit(int ox, int oy) { return 1L << b(ox, oy); }

    static long shift(long m, int ox, int oy) {
        int s = ox + 8 * oy;
        return s >= 0 ? m << s : m >>> -s;
    }

    /** The three tiles next to our tile that facing f (G.DIRS index) sees: ahead and the two front diagonals. */
    static long vis(int f) {
        int l = (f + 7) & 7, r = (f + 1) & 7;
        return bit(DX[f], DY[f]) | bit(DX[l], DY[l]) | bit(DX[r], DY[r]);
    }

    /** Whether a rat at offset (x, y) facing f (G.DIRS index, -1 for none) sees offset (tx, ty): the engine's
     *  90-degree cone of radius squared 20 (Baby.sees). */
    static boolean sees(int x, int y, int f, int tx, int ty) {
        int dx = tx - x, dy = ty - y;
        if (f < 0) return true;
        int fx = DX[f], fy = DY[f];
        return dx * dx + dy * dy <= 20 && dx * fx + dy * fy >= Math.abs(dx * fy - dy * fx);
    }

    /** Mirror-invariant tie key of offset (dx, dy) seen from a tile whose doubled centre offset is (px, py): the cross
     *  product signed by the side chirality, then the dot product (G.tieFirst's order). Larger ranks first. */
    static int key(int px, int py, int chir, int dx, int dy) {
        return (px * dy - py * dx) * chir * 4096 + px * dx + py * dy;
    }

    /**
     * Score every end tile and facing. Enemies are offsets from us with health, facing (-1 none) and whether they can
     * grab (not carrying). free: window tiles an enemy may step onto (walls, dirt, robots and our tile cleared).
     * moveMask: bit d for each legal step d (stay is always legal). Sets bestD/bestF/grabI/biteI/bestS, curS (stay,
     * current facing) and dirBest; returns bestS.
     */
    static int evaluate(int myHp, int cur, boolean turnOk, int moveMask, boolean actOk, int n, long free,
                        int px, int py, int chir) {
        for (int i = 0; i < n; i++) {
            long e = bit(ex[i], ey[i]), nb = shift(K3, ex[i], ey[i]);
            eb[i] = e;
            r0[i] = nb & (free | e) & INNER;
            r1[i] = r0[i] | (nb & CBIT);
        }
        bestS = Integer.MIN_VALUE; bestD = 8; bestF = cur; grabI = -1; biteI = -1; curS = 0;
        int bestKey = 0, bestFKey = 0;
        for (int d = 8; d >= 0; d--) {
            dirBest[d] = Integer.MIN_VALUE;
            if (d < 8 && (moveMask & (1 << d)) == 0) continue;
            int tx = d < 8 ? DX[d] : 0, ty = d < 8 ? DY[d] : 0;
            long at = shift(ADJ, tx, ty);
            for (int i = 0; i < n; i++) {
                rt[i] = (d == 8 ? r0[i] : r1[i]) & at;
                gr[i] = actOk && (eb[i] & at) != 0 && (ehp[i] < myHp || !sees(ex[i], ey[i], edir[i], tx, ty));
            }
            int tKey = d == 8 ? Integer.MAX_VALUE : key(px, py, chir, tx, ty);
            for (int f = turnOk ? 0 : cur, f1 = turnOk ? 7 : cur; f <= f1; f++) {
                long v = shift(vis(f), tx, ty);
                int g = -1, gv = 0, gk = 0, bi = -1, bv = 0, bh = 0, bk = 0;
                for (int i = 0; i < n; i++) {
                    if ((eb[i] & v) == 0) continue;
                    // equal choices fall to the mirror-invariant key of the victim's tile
                    if (gr[i]) {
                        int x = GRAB + ehp[i] / 10, k = key(px, py, chir, ex[i], ey[i]);
                        if (x > gv || x == gv && k > gk) { gv = x; g = i; gk = k; }
                    } else if (actOk) {
                        int h = ehp[i], k = key(px, py, chir, ex[i], ey[i]);
                        int x = h <= 10 ? KILL : h >= myHp && h - 10 < myHp ? FLIP : BITE;
                        if (x > bv || x == bv && (h < bh || h == bh && k > bk)) { bv = x; bi = i; bh = h; bk = k; }
                    }
                }
                if (g >= 0) { bi = -1; bv = 0; }
                int th = 0;
                for (int i = 0; i < n; i++) {
                    if (i == g || !ecan[i] || rt[i] == 0) continue;
                    int h = ehp[i] - (i == bi ? 10 : 0);
                    if (h <= 0) continue;
                    if (h > myHp || (rt[i] & ~v) != 0) th++;
                }
                int s = gv + bv - THREAT * th - (d < 8 && f != d && cur != d ? 1 : 0);
                if (d == 8 && f == cur) curS = s;
                if (s > dirBest[d]) dirBest[d] = s;
                int fKey = f == cur ? Integer.MAX_VALUE : key(px, py, chir, DX[f], DY[f]);
                if (s > bestS || s == bestS && (tKey > bestKey || tKey == bestKey && fKey > bestFKey)) {
                    bestS = s; bestD = d; bestF = f; grabI = g; biteI = bi; bestKey = tKey; bestFKey = fKey;
                }
            }
        }
        return bestS;
    }

    /**
     * Before this turn's bites: take the movement (and the action, for a grab or bite) when grab safety or a grab
     * says so; true when it acted (the caller skips its pre-move bite and Baby.moveDecision stands down). Otherwise
     * the steps clearly worse than staying are added to Nav.avoid.
     */
    static boolean act(RobotInfo[] robots) throws GameActionException {
        RobotController rc = G.rc;
        if (rc.getCarrying() != null || Flow.left() < Cfg.GRAB_BC) return false;
        MapLocation me = rc.getLocation();
        int n = 0;
        long occ = 0;
        for (RobotInfo r : robots) {
            int ox = r.location.x - me.x, oy = r.location.y - me.y;
            if (ox < -3 || ox > 3 || oy < -3 || oy > 3) {
                if (r.type == UnitType.RAT_KING && ox >= -4 && ox <= 4 && oy >= -4 && oy <= 4) occ |= shift(K3, ox, oy);
                continue;
            }
            if (r.type == UnitType.RAT_KING) {
                // an enemy king next to us is bitten, not danced around
                if (r.team == G.them && Math.max(Math.abs(ox), Math.abs(oy)) <= 2) return false;
                occ |= shift(K3, ox, oy);
            } else if (r.team == Team.NEUTRAL) {
                occ |= shift(K3 & ((3L << 27) | (3L << 35)), ox, oy);
            } else {
                occ |= bit(ox, oy);
                if (r.team == G.them && n < MAXE) {
                    ex[n] = ox; ey[n] = oy; ehp[n] = r.health; er[n] = r;
                    Direction f = r.direction;
                    edir[n] = f == null || f == Direction.CENTER ? -1 : f.ordinal();
                    ecan[n] = r.carryingRobot == null;
                    n++;
                }
            }
        }
        if (n == 0) return false;
        long free = 0;
        for (int oy = -2; oy <= 2; oy++) for (int ox = -2; ox <= 2; ox++) {
            int x = me.x + ox, y = me.y + oy;
            if (!G.onMap(x, y) || (Sym.mem[x + y * G.W] & (Sym.WALL | Sym.DIRT)) != 0) continue;
            free |= bit(ox, oy);
        }
        free &= ~occ & ~CBIT;
        boolean moveOk = rc.isMovementReady();
        int moveMask = 0;
        if (moveOk) for (int d = 0; d < 8; d++) if ((Nav.avoid & (1 << d)) == 0 && rc.canMove(G.DIRS[d])) moveMask |= 1 << d;
        int cur = rc.getDirection().ordinal();
        int px = 2 * me.x - G.cx2, py = 2 * me.y - G.cy2;
        evaluate(rc.getHealth(), cur, rc.isTurningReady(), moveMask, rc.isActionReady(), n, free, px, py, G.chir);
        if (grabI < 0 && bestS < curS + MARGIN) {
            for (int d = 0; d < 8; d++)
                if ((moveMask & (1 << d)) != 0 && dirBest[d] <= curS - MARGIN) { Nav.avoid |= 1 << d; avoids++; }
            return false;
        }
        execute();
        return true;
    }

    static void execute() throws GameActionException {
        RobotController rc = G.rc;
        Direction f = G.DIRS[bestF];
        Direction cur = rc.getDirection();
        if (bestD < 8) {
            Direction d = G.DIRS[bestD];
            if (cur != d && f == d && rc.canTurn(d)) { rc.turn(d); cur = d; }
            if (rc.canMove(d)) { rc.move(d); steps++; }
        }
        if (cur != f && rc.canTurn(f)) rc.turn(f);
        G.me = rc.getLocation();
        G.facing = rc.getDirection();
        if (grabI >= 0) {
            MapLocation l = er[grabI].location;
            if (rc.canCarryRat(l)) {
                rc.carryRat(l);
                Baby.carryRound = G.round;
                Baby.naps++;
                grabs++;
            }
        } else if (biteI >= 0) {
            MapLocation l = er[biteI].location;
            if (rc.canAttack(l)) {
                int c = Baby.finishCheese(er[biteI].health, rc.getGlobalCheese());
                if (c > 0 && rc.canAttack(l, c)) rc.attack(l, c); else rc.attack(l);
                Baby.bitesRat++;
            }
        }
        round = G.round;
        acts++;
        Baby.why = 'm';
    }
}
