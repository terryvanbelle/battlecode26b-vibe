package s0z;

import battlecode.common.*;

/** Baby rat: gather and deliver cheese, fight cats, lay cat traps, explore. */
public final class Baby {
    public static final int GATHER = 0, FIGHT = 1, ATTACK = 2;
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
        seen = robots;
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

        if (role != ATTACK) pickUp();
        Nav.avoid = cat != null ? laneMask(cat) : 0;
        if (!G.coop && Cfg.TRAP_RISK) {
            TrapRisk.observe(robots);
            if (Nav.stall < 8) Nav.avoid |= TrapRisk.mask(G.me);
        }

        eKing = enemyKing;
        nearestEnemy = null;
        if (!G.coop) {
            int bd = Integer.MAX_VALUE;
            for (RobotInfo r : robots) {
                if (r.team != G.them || r.type == UnitType.RAT_KING) continue;
                int d = G.me.distanceSquaredTo(r.location);
                if (d < bd) { bd = d; nearestEnemy = r; }
            }
            // fighters close on a rat they can grab (lower health than ours: trapped or bitten) before anything else
            if (Cfg.POUNCE && role != GATHER && rc.getCarrying() == null) {
                RobotInfo p = prey(robots, rc.getHealth());
                if (p != null) nearestEnemy = p;
            }
        }
        // 0. a carried enemy is thrown as soon as we can act
        if (rc.getCarrying() != null) tryThrow(robots);
        // 1. bites: enemy rats once cooperation is over (they shoot back; cats wait), then cats.
        //    Attackers open the war themselves, but only on the enemy king.
        if (!G.coop && Cfg.NAP) tryCarry(robots);
        if (!G.coop && Cfg.BABY_RAT_TRAPS) tryRatTrap();
        if (!G.coop) biteEnemies(robots);
        else if (role == ATTACK && enemyKing != null) biteEnemies(new RobotInfo[]{enemyKing});
        if (cat != null) tryBiteCat(cat);
        // 2. cat trap ahead of an approaching cat
        if (cat != null && Cfg.CAT_TRAPS) tryCatTrap(cat);

        report();

        // 3. movement
        if (rc.isMovementReady()) moveDecision();
        pickUp();
        if (!G.coop && Cfg.NAP) tryCarry(rc.senseNearbyRobots(2, G.them));
        if (!G.coop) biteEnemies(rc.senseNearbyRobots(2, G.them));
        else if (role == ATTACK) {
            RobotInfo[] near = rc.senseNearbyRobots(8, G.them);
            for (RobotInfo r : near) if (r.type == UnitType.RAT_KING) biteEnemies(new RobotInfo[]{r});
        }
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
        if (Cfg.RUSH && idx >= Cfg.RUSH_FROM && idx < Cfg.RUSH_TO) role = ATTACK;
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

    static RobotInfo eKing, nearestEnemy;
    static RobotInfo[] seen = new RobotInfo[0];
    public static int naps, throwsDone, ratTraps;

    /** Grab an adjacent enemy baby rat (the engine allows it when it cannot see us or is weaker). */
    static boolean tryCarry(RobotInfo[] robots) throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.isActionReady() || rc.getCarrying() != null) return false;
        RobotInfo best = null;
        for (RobotInfo r : robots) {
            if (r.team != G.them || r.type != UnitType.BABY_RAT) continue;
            if (G.me.distanceSquaredTo(r.location) > 2) continue;
            if (!rc.canCarryRat(r.location)) continue;
            if (best == null || r.health > best.health) best = r;
        }
        if (best == null) return false;
        rc.carryRat(best.location);
        naps++;
        return true;
    }

    /**
     * Throw the carried rat along the direction that hurts most: the flight (6-8 tiles) hits the first robot,
     * wall or dirt for 46/42 (first two tiles), 38/34, 30/26, 22/18; the victim takes the same damage as the robot hit.
     * Hitting our own rat costs its damage; an empty flight lands for 10.
     */
    static boolean tryThrow(RobotInfo[] robots) throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.isActionReady()) return false;
        MapLocation me = rc.getLocation();
        Direction bestD = null;
        int bs = Integer.MIN_VALUE;
        for (int i = 0; i < 8; i++) {
            Direction d = G.DIRS[i];
            MapLocation t1 = me.add(d);
            if (!G.onMap(t1.x, t1.y) || Nav.terrainBlocked(t1) || rc.canSenseLocation(t1) && rc.isLocationOccupied(t1)) continue;
            int s2 = 10;
            MapLocation t = t1;
            for (int k = 2; k <= 8; k++) {
                t = t.add(d);
                int dmg = 10 + 4 * (2 * (4 - (k - 1) / 2) + ((k & 1) == 1 ? 1 : 0));
                if (!G.onMap(t.x, t.y)) { s2 = 10; break; }
                if (Nav.terrainBlocked(t)) { s2 = dmg; break; }
                if (rc.canSenseLocation(t)) {
                    RobotInfo r = rc.senseRobotAtLocation(t);
                    if (r != null) {
                        if (r.team == G.them) s2 = dmg * (r.type == UnitType.RAT_KING ? 3 : 2);
                        else if (r.team == G.us) s2 = -dmg;
                        else s2 = -50; // a cat eats the rat: no damage, it sleeps
                        break;
                    }
                }
            }
            s2 = s2 * 2 + (G.hand(me, d) > 0 ? 1 : 0);
            if (s2 > bs) { bs = s2; bestD = d; }
        }
        if (bestD == null) return false;
        if (rc.getDirection() != bestD) { if (!rc.canTurn(bestD)) return false; rc.turn(bestD); G.facing = bestD; }
        if (!rc.canThrowRat()) return false;
        rc.throwRat();
        throwsDone++;
        return true;
    }

    /** Rat trap on the front tile nearest an approaching enemy rat (50 damage + stun when it enters the 3x3). */
    static boolean tryRatTrap() throws GameActionException {
        RobotController rc = G.rc;
        if (nearestEnemy == null || !rc.isActionReady() || rc.getNumberRatTraps() >= 25) return false;
        if (rc.getAllCheese() < Cfg.RAT_TRAP_MIN_CHEESE) return false;
        int d0 = G.me.distanceSquaredTo(nearestEnemy.location);
        if (d0 <= 2 || d0 > Cfg.BABY_TRAP_DIST2) return false;
        MapLocation me = rc.getLocation();
        Direction f = rc.getDirection();
        Direction[] fr = {f, f.rotateLeft(), f.rotateRight()};
        MapLocation best = null;
        int bs = Integer.MAX_VALUE;
        for (Direction d : fr) {
            MapLocation l = me.add(d);
            if (!rc.canPlaceRatTrap(l)) continue;
            int s2 = l.distanceSquaredTo(nearestEnemy.location);
            if (s2 < bs) { bs = s2; best = l; }
        }
        if (best == null || bs >= d0) return false;
        rc.placeRatTrap(best);
        ratTraps++;
        return true;
    }

    /** Bite the best adjacent enemy: a king first, then the lowest health; turn to face it when needed. */
    static boolean biteEnemies(RobotInfo[] robots) throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.isActionReady()) return false;
        MapLocation me = rc.getLocation();
        RobotInfo best = null, bestTurn = null;
        int bs = Integer.MAX_VALUE, bt = Integer.MAX_VALUE;
        for (RobotInfo r : robots) {
            if (r.team != G.them) continue;
            MapLocation t = r.location;
            if (r.type == UnitType.RAT_KING) {
                // any king tile adjacent to us
                int dx = Math.max(-1, Math.min(1, me.x - t.x)), dy = Math.max(-1, Math.min(1, me.y - t.y));
                t = t.translate(dx, dy);
            }
            if (me.distanceSquaredTo(t) > 2) continue;
            int s = (r.type == UnitType.RAT_KING ? 0 : 1000) + r.health;
            if (rc.canAttack(t)) { if (s < bs) { bs = s; best = r; } }
            else if (s < bt) { bt = s; bestTurn = r; }
        }
        // turning is free of action cooldown: a better target behind us wins if we can turn this turn
        RobotInfo r = best;
        if (bestTurn != null && (best == null || bt < bs) && rc.isTurningReady()) r = bestTurn;
        if (r == null) return false;
        MapLocation t = r.location;
        if (r.type == UnitType.RAT_KING) {
            int dx = Math.max(-1, Math.min(1, me.x - t.x)), dy = Math.max(-1, Math.min(1, me.y - t.y));
            t = t.translate(dx, dy);
        }
        if (!rc.canAttack(t)) {
            Direction d = me.directionTo(t);
            if (rc.getDirection() != d && rc.canTurn(d)) rc.turn(d);
            G.facing = rc.getDirection();
        }
        if (rc.canAttack(t)) { rc.attack(t); bitesRat++; return true; }
        return false;
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
                int cheese = Comms.read(Comms.BUDGET) * 4 >= Cfg.CHEESE_BITE_SURPLUS ? 1 : 0;
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
        if (rc.getAllCheese() < Cfg.TRAP_FLOOR) return;
        // defending a king is survival: traps there need only the floor; elsewhere they need projected surplus
        MapLocation k = Comms.nearestKing(G.me);
        boolean defend = k != null && k.distanceSquaredTo(c.location) <= Cfg.TRAP_DEFEND_DIST2;
        if (!defend && Comms.read(Comms.BUDGET) * 4 < Cfg.TRAP_SURPLUS) return;
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

    /** Which branch took the last movement decision (debug tag mv=). */
    static char why = '-';

    static void moveDecision() throws GameActionException {
        why = '-';
        RobotController rc = G.rc;
        int raw = rc.getRawCheese();
        boolean late = G.round > 1960;

        // keep gatherers out of cat reach
        if (cat != null && role == GATHER && catDist4(cat.location, G.me) <= 4 * Cfg.GATHER_CAT_DIST) {
            MapLocation cc = new MapLocation(cat.location.x, cat.location.y);
            if (Nav.flee(cc)) { why = 'c'; return; }
        }
        if (!G.coop && retreat()) { why = 'r'; return; }
        if (!G.coop && Cfg.MICRO && role != ATTACK && Micro.act(seen)) { why = 'm'; return; }
        if (!G.coop && Cfg.FACE_ENEMY && (role != GATHER || Cfg.WAR_GATHER < 2 || nearEnemy(2)) && faceEnemy()) { why = 'f'; return; }
        if (role == ATTACK && attack()) { why = 'a'; return; }
        if (!G.coop && war()) { why = 'w'; return; }
        if (!G.coop && role == FIGHT && Cfg.FORT_R > 0 && fort()) { why = 'h'; return; }
        why = 'g';
        if (cat != null && role == FIGHT) {
            if (approachCat(cat)) return;
        }

        if (raw >= Cfg.DELIVER_AT || (raw > 0 && (late || cheeseTarget == null && raw >= 10))) {
            MapLocation k = Comms.nearestKing(G.me);
            if (k != null) {
                if (inTransferReach(G.me, k)) {
                    // in transfer range of the king's body (dist2 9 to its nearest tile): face it, stay off the ring
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

    static int noKing;               // symmetry candidates whose enemy-king spot we saw empty
    static MapLocation lastEKing;
    static int lastEKingRound = -100;

    /** Attacker movement: the visible enemy king, else the last place we saw it, else the nearest live mirror spot. */
    static boolean attack() throws GameActionException {
        if (eKing != null) { lastEKing = eKing.location; lastEKingRound = G.round; Nav.goTo(eKing.location); return true; }
        if (lastEKing != null && G.round - lastEKingRound < 30 && G.me.distanceSquaredTo(lastEKing) > 8) {
            Nav.goTo(lastEKing);
            return true;
        }
        MapLocation t = rushTarget();
        if (t == null) return false;
        Nav.goTo(t);
        return true;
    }

    static MapLocation rushTarget() throws GameActionException {
        RobotController rc = G.rc;
        MapLocation best = null;
        int bd = Integer.MAX_VALUE;
        for (int s = 1; s <= 4; s <<= 1) {
            if ((Sym.mask & s) == 0 || (noKing & s) != 0) continue;
            MapLocation c = G.mirror(home, s);
            if (G.me.distanceSquaredTo(c) <= 13 && rc.canSenseLocation(c)) {
                RobotInfo r = rc.senseRobotAtLocation(c);
                if (r == null || r.type != UnitType.RAT_KING || r.team != G.them) { noKing |= s; continue; }
            }
            int d = G.me.distanceSquaredTo(c) * 2 + (G.hand(G.me, G.me.directionTo(c)) > 0 ? 1 : 0);
            if (d < bd) { bd = d; best = c; }
        }
        if (best == null && noKing != 0) { noKing = 0; return null; }
        return best;
    }

    /**
     * Anti-ratnap stance: an enemy within two tiles must stay in our cone (a rat can only be grabbed by an enemy it
     * cannot see, or by a healthier one). Turn to face it instead of moving; once facing, advance only forward.
     */
    static boolean faceEnemy() throws GameActionException {
        if (nearestEnemy == null) return false;
        RobotController rc = G.rc;
        MapLocation e = nearestEnemy.location;
        int d = G.me.distanceSquaredTo(e);
        if (d > 8) return false;
        Direction to = G.me.directionTo(e);
        if (!rc.canSenseLocation(e) || (d > 2 && rc.getDirection() != to)) {
            if (rc.getDirection() != to && rc.canTurn(to)) { rc.turn(to); G.facing = to; }
            return true;
        }
        if (d > 2 && rc.canMove(to) && (Nav.avoid & (1 << to.ordinal())) == 0) {
            rc.move(to);
            G.me = rc.getLocation();
        }
        return true;
    }

    /** War movement: close on a visible enemy king or nearby enemy rat, or answer a king's alert. */
    static boolean war() throws GameActionException {
        int raw = G.rc.getRawCheese();
        if (role == GATHER && Cfg.WAR_GATHER > 0) {
            // gatherers keep the economy going: only an adjacent enemy stops them
            if (!nearEnemy(2)) return false;
            Direction f = G.me.directionTo(nearestEnemy.location);
            if (G.rc.getDirection() != f && G.rc.canTurn(f)) { G.rc.turn(f); G.facing = f; }
            return true;
        }
        if (eKing != null && (role == FIGHT || raw < Cfg.DELIVER_AT)) {
            Nav.goTo(eKing.location);
            return true;
        }
        if (nearestEnemy != null && !(role == FIGHT && Cfg.FORT_R > 0 && !nearHome(nearestEnemy.location, Cfg.FORT_R + 3))) {
            int d = G.me.distanceSquaredTo(nearestEnemy.location);
            if (d <= 2) {
                // stay and face it: biting happens before and after movement
                Direction f = G.me.directionTo(nearestEnemy.location);
                if (G.rc.getDirection() != f && G.rc.canTurn(f)) G.rc.turn(f);
                return true;
            }
            if (d <= 20 && (role == FIGHT || raw < Cfg.DELIVER_AT || d <= 8)) {
                Nav.goTo(nearestEnemy.location);
                return true;
            }
        }
        if (Comms.fresh(Comms.read(Comms.ALERT + 2), 3)) {
            MapLocation a = Comms.readLoc(Comms.ALERT);
            if (a != null && G.me.distanceSquaredTo(a) <= Cfg.ALERT_DIST2 && G.me.distanceSquaredTo(a) > 8) {
                Nav.goTo(a);
                return true;
            }
        }
        return false;
    }

    /** Nearest enemy baby within dist2 20 that we could ratnap (strictly less health than ours), or null. */
    static RobotInfo prey(RobotInfo[] robots, int hp) {
        RobotInfo prey = null;
        int pd = Integer.MAX_VALUE;
        for (RobotInfo r : robots) {
            if (r.team != G.them || r.type != UnitType.BABY_RAT || r.health >= hp) continue;
            int d = G.me.distanceSquaredTo(r.location);
            if (d <= 20 && d < pd) { pd = d; prey = r; }
        }
        return prey;
    }

    /** A wounded rat near enemies falls back to its king instead of being finished off. */
    static boolean retreat() throws GameActionException {
        if (G.rc.getHealth() > Cfg.RETREAT_HP || !nearEnemy(20)) return false;
        MapLocation k = Comms.nearestKing(G.me);
        if (k == null || G.cheb(G.me, k) <= 2) return false;
        Nav.goTo(k);
        return true;
    }

    /** True when l is within Chebyshev r of our nearest king. */
    static boolean nearHome(MapLocation l, int r) {
        MapLocation k = Comms.nearestKing(l);
        return k != null && G.cheb(k, l) <= r;
    }

    /**
     * Fortress: at war, fighters hold a ring around the king (where its traps are) instead of meeting the enemy
     * in the open; war() engages anything that comes within FORT_R + 3 of the king.
     */
    static boolean fort() throws GameActionException {
        MapLocation k = Comms.nearestKing(G.me);
        if (k == null) return false;
        if (cat != null && nearHome(cat.location, Cfg.FORT_R + 5)) return false; // defend against a cat at home
        if (G.cheb(G.me, k) > Cfg.FORT_R) { Nav.goTo(k); return true; }
        // on station: face outward, toward the map centre
        MapLocation mid = new MapLocation(G.W / 2, G.H / 2);
        Direction f = k.directionTo(mid);
        if (f != Direction.CENTER && G.rc.getDirection() != f && G.rc.canTurn(f)) { G.rc.turn(f); G.facing = f; }
        return true;
    }

    static boolean nearEnemy(int d2) {
        return nearestEnemy != null && G.me.distanceSquaredTo(nearestEnemy.location) <= d2;
    }

    /** True when the nearest tile of a 3x3 king centred at k is within transfer range (dist2 9) of p. */
    static boolean inTransferReach(MapLocation p, MapLocation k) {
        int dx = Math.max(0, Math.abs(p.x - k.x) - 1), dy = Math.max(0, Math.abs(p.y - k.y) - 1);
        return dx * dx + dy * dy <= 9 && !(Math.abs(p.x - k.x) <= 1 && Math.abs(p.y - k.y) <= 1);
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
