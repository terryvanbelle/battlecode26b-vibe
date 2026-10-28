package ratbot;

import battlecode.common.*;

/** Baby rat: gather and deliver cheese, fight cats, lay cat traps, explore. */
public final class Baby {
    public static final int GATHER = 0, FIGHT = 1;
    public static int role = -1;
    static int birthRound;
    static MapLocation home;          // spawning king's location at birth
    static int fu = 1, fv = 1;        // side frame (signs of our king's offset from centre)

    static MapLocation cheeseTarget;  // last seen cheese tile
    static MapLocation mineTarget;    // sticky assigned mine
    static MapLocation exploreTarget;
    static MapLocation catSeen;       // bottom-left of nearest cat sensed this turn
    static RobotInfo cat;
    static int catSeenRound = -100;
    static MapLocation lastCatTarget;

    // mines this rat has sensed (to report to kings)
    static int[] myMines = new int[24];
    static int myMineCount;
    static int reportedSym = 7;

    public static int delivered, bitesCat, bitesRat, trapsPlaced, picked;

    public static void turn() throws GameActionException {
        RobotController rc = G.rc;
        if (role < 0) born();
        Comms.readKings();
        Comms.readMines();
        int s = Comms.read(Comms.SYM);
        if (s != 0) Sym.merge(s);

        sense();
        RobotInfo[] robots = rc.senseNearbyRobots(-1, null);
        RobotInfo enemyKing = null, enemy = null;
        cat = null;
        int bestCat = Integer.MAX_VALUE;
        for (RobotInfo r : robots) {
            if (r.team == Team.NEUTRAL) {
                int d = catDist4(r.location, G.me);
                if (d < bestCat) { bestCat = d; cat = r; }
            } else if (r.team == G.them) {
                if (r.type == UnitType.RAT_KING) enemyKing = r;
                else if (enemy == null || r.health < enemy.health) enemy = r;
            }
        }
        if (cat != null) { catSeen = cat.location; catSeenRound = G.round; }

        pickUp();
        Nav.avoid = cat != null ? laneMask(cat) : 0;

        // 1. bites: cats always; enemy rats only once cooperation is over
        if (cat != null) tryBiteCat(cat);
        if (!G.coop) {
            if (enemyKing != null) tryBite(enemyKing);
            if (enemy != null) tryBite(enemy);
        }
        // 2. cat trap ahead of an approaching cat
        if (cat != null && Cfg.CAT_TRAPS) tryCatTrap(cat);

        report();

        // 3. movement
        if (rc.isMovementReady()) moveDecision();
        pickUp();
        if (cat != null) tryBiteCat(cat);
        tryDeliver();
        sense2();
    }

    static void born() {
        RobotController rc = G.rc;
        birthRound = G.round;
        int cost = rc.getCurrentRatCost();
        int idx = (cost - 10) / 10 * 4; // approximate spawn index from the cost step
        role = GATHER;
        if (idx >= Cfg.GATHER_FIRST && (G.round % Cfg.FIGHT_EVERY) == 0) role = FIGHT;
        Comms.readKings();
        home = Comms.nearestKing(G.me);
        if (home == null) home = G.me;
        int px = 2 * home.x - G.cx2, py = 2 * home.y - G.cy2;
        fu = px >= 0 ? 1 : -1;
        fv = py >= 0 ? 1 : -1;
        // mirror-invariant seed: round and the unsigned offset from the king
        G.seed(G.round * 977 + Math.abs(G.me.x - home.x) * 31 + Math.abs(G.me.y - home.y) * 7 + 1);
    }

    /** 4 x squared distance from a tile to a cat's 2x2 centre. */
    static int catDist4(MapLocation catBL, MapLocation p) {
        int dx = 2 * p.x - (2 * catBL.x + 1), dy = 2 * p.y - (2 * catBL.y + 1);
        return dx * dx + dy * dy;
    }

    static boolean inCatBody(MapLocation catBL, MapLocation p) {
        return p.x >= catBL.x && p.x <= catBL.x + 1 && p.y >= catBL.y && p.y <= catBL.y + 1;
    }

    /** Bit d set when stepping in DIRS[d] lands in the cat's next two footprints along its facing. */
    static int laneMask(RobotInfo c) {
        Direction cd = c.direction;
        int mask = 0;
        MapLocation n1 = c.location.add(cd), n2 = n1.add(cd);
        for (int i = 0; i < 8; i++) {
            MapLocation p = G.me.add(G.DIRS[i]);
            if (inCatBody(n1, p) || inCatBody(n2, p) || inCatBody(c.location, p)) mask |= 1 << i;
        }
        return mask;
    }

    static boolean inLane(RobotInfo c, MapLocation p) {
        MapLocation n1 = c.location.add(c.direction), n2 = n1.add(c.direction);
        return inCatBody(n1, p) || inCatBody(n2, p);
    }

    static void sense() throws GameActionException {
        MapInfo[] infos = G.rc.senseNearbyMapInfos();
        MapLocation bestCheese = null;
        int bd = Integer.MAX_VALUE;
        for (MapInfo mi : infos) {
            MapLocation l = mi.getMapLocation();
            boolean mine = mi.hasCheeseMine();
            if (Sym.record(l.x, l.y, mi.isWall(), mi.isDirt(), mine)) { }
            if (mine) noteMine(l.x, l.y);
            if (mi.getCheeseAmount() > 0) {
                int d = G.me.distanceSquaredTo(l);
                if (d < bd) { bd = d; bestCheese = l; }
            } else if (cheeseTarget != null && cheeseTarget.equals(l)) {
                cheeseTarget = null;
            }
        }
        if (bestCheese != null) {
            if (cheeseTarget == null || G.me.distanceSquaredTo(cheeseTarget) > bd) cheeseTarget = bestCheese;
        }
    }

    /** Re-sense after moving (new cone) only to refresh cheese; terrain is caught next turn. */
    static void sense2() throws GameActionException {
        if (Clock.getBytecodesLeft() < 4000) return;
        sense();
    }

    static void noteMine(int x, int y) {
        int key = x * 64 + y;
        for (int i = 0; i < myMineCount; i++) if (myMines[i] == key) return;
        if (myMineCount < myMines.length) myMines[myMineCount++] = key;
    }

    static void pickUp() throws GameActionException {
        RobotController rc = G.rc;
        MapLocation c = rc.getLocation();
        if (rc.canPickUpCheese(c)) { rc.pickUpCheese(c); picked++; }
        Direction f = rc.getDirection();
        Direction[] fr = {f, f.rotateLeft(), f.rotateRight()};
        for (Direction d : fr) {
            MapLocation l = c.add(d);
            if (rc.canPickUpCheese(l)) { rc.pickUpCheese(l); picked++; }
        }
        G.rawCheese = rc.getRawCheese();
    }

    static boolean tryBite(RobotInfo r) throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.isActionReady()) return false;
        MapLocation l = r.location;
        if (G.me.distanceSquaredTo(l) > 2) return false;
        if (!rc.canAttack(l)) {
            Direction d = G.me.directionTo(l);
            if (rc.getDirection() != d && rc.canTurn(d)) rc.turn(d);
        }
        if (rc.canAttack(l)) { rc.attack(l); bitesRat++; return true; }
        return false;
    }

    /** Bite any adjacent tile of the cat's 2x2 body, turning to face it when needed. */
    static boolean tryBiteCat(RobotInfo c) throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.isActionReady()) return false;
        MapLocation me = rc.getLocation();
        MapLocation turnTo = null;
        for (int i = 0; i < 4; i++) {
            MapLocation t = c.location.translate(i & 1, i >> 1);
            if (me.distanceSquaredTo(t) > 2) continue;
            if (rc.canAttack(t)) {
                int cheese = rc.getAllCheese() > 400 ? 1 : 0;
                if (cheese > 0 && rc.canAttack(t, cheese)) rc.attack(t, cheese); else rc.attack(t);
                bitesCat++;
                return true;
            }
            if (turnTo == null) turnTo = t;
        }
        if (turnTo != null && rc.canTurn()) {
            rc.turn(me.directionTo(turnTo));
            if (rc.canAttack(turnTo)) { rc.attack(turnTo); bitesCat++; return true; }
        }
        return false;
    }

    /** Place a cat trap on a visible adjacent tile in front of an approaching cat. */
    static void tryCatTrap(RobotInfo c) throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.isActionReady()) return;
        if (rc.getAllCheese() < Cfg.TRAP_RESERVE) return;
        if (rc.getNumberCatTraps() >= 10) return;
        int d4 = catDist4(c.location, G.me);
        if (d4 > 4 * 36) return;
        MapLocation best = null;
        int bs = Integer.MAX_VALUE;
        MapLocation me = rc.getLocation();
        Direction f = rc.getDirection();
        Direction[] fr = {f, f.rotateLeft(), f.rotateRight()};
        MapLocation n1 = c.location.add(c.direction), n2 = n1.add(c.direction), n3 = n2.add(c.direction);
        for (Direction d : fr) {
            MapLocation l = me.add(d);
            if (!rc.canPlaceCatTrap(l)) continue;
            // trigger zone is the 3x3 around the trap: the cat must pass within Chebyshev 1 of it
            int s = Math.min(Math.min(chebToBody(n1, l), chebToBody(n2, l)), chebToBody(n3, l));
            if (s > 1) continue;
            int score = s * 100 + catDist4(c.location, l);
            if (score < bs) { bs = score; best = l; }
        }
        if (best != null) { rc.placeCatTrap(best); trapsPlaced++; }
    }

    static int chebToBody(MapLocation bl, MapLocation p) {
        int dx = p.x < bl.x ? bl.x - p.x : (p.x > bl.x + 1 ? p.x - bl.x - 1 : 0);
        int dy = p.y < bl.y ? bl.y - p.y : (p.y > bl.y + 1 ? p.y - bl.y - 1 : 0);
        return Math.max(dx, dy);
    }

    static void tryDeliver() throws GameActionException {
        RobotController rc = G.rc;
        int raw = rc.getRawCheese();
        if (raw <= 0 || !rc.isActionReady()) return;
        RobotInfo[] ks = rc.senseNearbyRobots(9, G.us);
        for (RobotInfo k : ks) {
            if (k.type != UnitType.RAT_KING) continue;
            // any king tile within range 9 and in our cone
            for (int dx = -1; dx <= 1; dx++)
                for (int dy = -1; dy <= 1; dy++) {
                    MapLocation t = k.location.translate(dx, dy);
                    if (rc.canTransferCheese(t, raw)) {
                        rc.transferCheese(t, raw);
                        delivered += raw;
                        G.rawCheese = 0;
                        return;
                    }
                }
        }
    }

    /** Squeak one fact to a nearby king: a mine it may not know, or a sharper symmetry mask. */
    static void report() {
        if (Comms.numKings == 0) return;
        MapLocation k = Comms.nearestKing(G.me);
        if (k == null || G.me.distanceSquaredTo(k) > 9) return;
        int sharedSym = Comms.read(Comms.SYM);
        if (sharedSym != 0 && (Sym.mask & sharedSym) != sharedSym) {
            G.rc.squeak(Comms.squeak(Comms.SQ_SYM, 0, 0, Sym.mask));
            return;
        }
        for (int i = 0; i < myMineCount; i++) {
            int x = myMines[i] / 64, y = myMines[i] % 64;
            if (!Comms.hasMine(x, y)) { G.rc.squeak(Comms.squeak(Comms.SQ_MINE, x, y, 0)); return; }
        }
        if (catSeen != null && G.round - catSeenRound <= 2) {
            G.rc.squeak(Comms.squeak(Comms.SQ_CAT, catSeen.x, catSeen.y, 0));
        }
    }

    // ---------------------------------------------------------------- movement

    static void moveDecision() throws GameActionException {
        RobotController rc = G.rc;
        int raw = rc.getRawCheese();
        boolean late = G.round > 1960;

        // keep gatherers out of cat reach
        if (cat != null && role == GATHER && catDist4(cat.location, G.me) <= 4 * Cfg.GATHER_CAT_DIST) {
            MapLocation cc = new MapLocation(cat.location.x, cat.location.y);
            if (Nav.flee(cc)) return;
        }
        if (cat != null && role == FIGHT) {
            if (approachCat(cat)) return;
        }

        if (raw >= Cfg.DELIVER_AT || (raw > 0 && (late || cheeseTarget == null && raw >= 10))) {
            MapLocation k = Comms.nearestKing(G.me);
            if (k != null) {
                if (G.me.distanceSquaredTo(k) <= 8) {
                    // in transfer range of the king's body: face it so it is inside our cone
                    Direction d = G.me.directionTo(k);
                    if (d != Direction.CENTER && rc.getDirection() != d && rc.canTurn(d)) rc.turn(d);
                    return;
                }
                Nav.goTo(k);
                return;
            }
        }
        if (cheeseTarget != null) {
            Nav.goTo(cheeseTarget);
            if (Nav.stall > 12) cheeseTarget = null;
            return;
        }
        if (role == FIGHT) {
            MapLocation ct = knownCat();
            if (ct != null) { Nav.goTo(ct); return; }
        }
        if (role == GATHER) {
            if (mineTarget == null) mineTarget = pickMine();
            if (mineTarget != null) {
                if (G.cheb(G.me, mineTarget) <= 2) {
                    // wander inside the spawn square, facing outward to see new cheese
                    wanderAround(mineTarget);
                } else {
                    Nav.goTo(mineTarget);
                    if (Nav.stall > 25) mineTarget = null;
                }
                return;
            }
        }
        explore();
    }

    static MapLocation knownCat() {
        if (catSeen != null && G.round - catSeenRound < 10) return catSeen;
        MapLocation best = null;
        int bd = Integer.MAX_VALUE;
        for (int k = 0; k < Comms.CAT_SLOTS; k++) {
            int b = Comms.CAT0 + 3 * k;
            if (!Comms.fresh(Comms.read(b + 2), 30)) continue;
            MapLocation l = Comms.readLoc(b);
            if (l == null) continue;
            int d = G.me.distanceSquaredTo(l);
            if (d < bd) { bd = d; best = l; }
        }
        return best;
    }

    /** Move to a tile adjacent to the cat body, outside its forward lane. */
    static boolean approachCat(RobotInfo c) throws GameActionException {
        MapLocation me = G.me;
        if (chebToBody(c.location, me) == 1 && !inLane(c, me)) return true; // already in place: stay
        MapLocation best = null;
        int bd = Integer.MAX_VALUE;
        for (int dx = -1; dx <= 2; dx++)
            for (int dy = -1; dy <= 2; dy++) {
                if (dx >= 0 && dx <= 1 && dy >= 0 && dy <= 1) continue;
                MapLocation t = c.location.translate(dx, dy);
                if (!G.onMap(t.x, t.y) || inLane(c, t)) continue;
                if ((Sym.mem[G.idx(t)] & (Sym.WALL | Sym.DIRT)) != 0) continue;
                int d = me.distanceSquaredTo(t) * 2 + (G.hand(me, me.directionTo(t)) > 0 ? 1 : 0);
                if (d < bd) { bd = d; best = t; }
            }
        if (best == null) return false;
        Nav.goTo(best);
        return true;
    }

    static MapLocation pickMine() {
        // nearest known mine on our half (closer to home than to its mirror); spread rats by seed
        MapLocation best = null;
        int bd = Integer.MAX_VALUE;
        int n = Comms.numMines;
        for (int i = 0; i < n; i++) {
            MapLocation m = Comms.mines[i];
            int d = m.distanceSquaredTo(home) + G.rand(40);
            if (d < bd) { bd = d; best = m; }
        }
        for (int i = 0; i < myMineCount; i++) {
            MapLocation m = new MapLocation(myMines[i] / 64, myMines[i] % 64);
            int d = m.distanceSquaredTo(home) + G.rand(40);
            if (d < bd) { bd = d; best = m; }
        }
        return best;
    }

    static void wanderAround(MapLocation m) throws GameActionException {
        if (exploreTarget == null || G.me.equals(exploreTarget) || G.cheb(exploreTarget, m) > 3 || Nav.stall > 6) {
            exploreTarget = new MapLocation(clampX(m.x + fu * (G.rand(7) - 3)), clampY(m.y + fv * (G.rand(7) - 3)));
        }
        Nav.goTo(exploreTarget);
    }

    static void explore() throws GameActionException {
        if (exploreTarget == null || G.cheb(G.me, exploreTarget) <= 2 || Nav.stall > 20) {
            // target in our side frame: mirrored rats pick mirrored targets
            int ax = G.rand(G.W), ay = G.rand(G.H);
            int x = fu > 0 ? ax : G.W - 1 - ax;
            int y = fv > 0 ? ay : G.H - 1 - ay;
            exploreTarget = new MapLocation(x, y);
        }
        Nav.goTo(exploreTarget);
    }

    static int clampX(int x) { return Math.max(0, Math.min(G.W - 1, x)); }

    static int clampY(int y) { return Math.max(0, Math.min(G.H - 1, y)); }
}
