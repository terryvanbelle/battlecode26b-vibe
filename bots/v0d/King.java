package v0d;

import battlecode.common.*;

/** Rat king: spawns, collects, publishes shared state, defends, steps away from cats. */
public final class King {
    static int slot = -1;
    static MapLocation start;
    public static int spawned;

    static void claimSlot() {
        for (int k = 0; k < Comms.KING_SLOTS; k++) {
            int b = Comms.KING0 + 3 * k;
            if (Comms.read(b) == 0 || !Comms.fresh(Comms.read(b + 2), 3)) { slot = k; return; }
        }
        slot = 0;
    }

    public static void turn() throws GameActionException {
        RobotController rc = G.rc;
        if (slot < 0) {
            claimSlot();
            start = G.me;
            if (G.round <= 1) initialSymmetry();
        }
        Comms.readMines();
        sense();
        readSqueaks();
        publish();

        RobotInfo[] enemies = rc.senseNearbyRobots(-1, G.them);
        RobotInfo[] cats = rc.senseNearbyRobots(-1, Team.NEUTRAL);

        pickUp();
        if (!G.coop) bite(enemies);
        spawn(cats, enemies);
        move(cats);
        publish();
    }

    /** Round 1: our start and the enemy start are mirror images; a candidate that maps us onto ourselves is impossible. */
    static void initialSymmetry() {
        int x = G.me.x, y = G.me.y;
        if (G.mirX(x, Sym.HOR) == x && G.mirY(y, Sym.HOR) == y) Sym.eliminate(Sym.HOR);
        if (G.mirX(x, Sym.VER) == x && G.mirY(y, Sym.VER) == y) Sym.eliminate(Sym.VER);
        if (G.mirX(x, Sym.ROT) == x && G.mirY(y, Sym.ROT) == y) Sym.eliminate(Sym.ROT);
    }

    static void sense() throws GameActionException {
        MapInfo[] infos = G.rc.senseNearbyMapInfos();
        for (MapInfo mi : infos) {
            MapLocation l = mi.getMapLocation();
            boolean mine = mi.hasCheeseMine();
            Sym.record(l.x, l.y, mi.isWall(), mi.isDirt(), mine);
            if (mine) Comms.addMine(l.x, l.y);
        }
    }

    static void readSqueaks() {
        // Kings act before the babies they spawned, so this round's squeaks mostly arrive after our turn:
        // read last round's too.
        Message[] ms = G.rc.readSqueaks(-1);
        int n = ms.length, used = 0;
        for (int i = 0; i < n && used < 16; i++) {
            if (ms[i].getRound() < G.round - 1) continue;
            used++;
            int m = ms[i].getBytes();
            switch (Comms.sqType(m)) {
                case Comms.SQ_MINE: Comms.addMine(Comms.sqX(m), Comms.sqY(m)); break;
                case Comms.SQ_SYM: Sym.merge(Comms.sqExtra(m) & 7); break;
                case Comms.SQ_EKING:
                    Comms.write(Comms.EKING, Comms.sqX(m) + 1);
                    Comms.write(Comms.EKING + 1, Comms.sqY(m) + 1);
                    Comms.write(Comms.EKING + 2, Comms.stamp());
                    break;
                case Comms.SQ_CAT: noteCat(Comms.sqX(m), Comms.sqY(m)); break;
                default: break;
            }
        }
    }

    static void noteCat(int x, int y) {
        // Slot 0 holds the most recent sighting; the older one shifts to slot 1 unless it is the same cat area.
        MapLocation c0 = Comms.readLoc(Comms.CAT0);
        if (c0 != null && (Math.abs(c0.x - x) > 6 || Math.abs(c0.y - y) > 6)) {
            Comms.write(Comms.CAT0 + 3, c0.x + 1);
            Comms.write(Comms.CAT0 + 4, c0.y + 1);
            Comms.write(Comms.CAT0 + 5, Comms.read(Comms.CAT0 + 2));
        }
        Comms.write(Comms.CAT0, x + 1);
        Comms.write(Comms.CAT0 + 1, y + 1);
        Comms.write(Comms.CAT0 + 2, Comms.stamp());
    }

    static void publish() {
        int b = Comms.KING0 + 3 * slot;
        Comms.write(b, G.rc.getLocation().x + 1);
        Comms.write(b + 1, G.rc.getLocation().y + 1);
        Comms.write(b + 2, Comms.stamp());
        int s = Comms.read(Comms.SYM);
        int mask = s == 0 ? Sym.mask : (s & Sym.mask);
        if (mask == 0) mask = Sym.mask;
        Sym.mask = mask;
        Comms.write(Comms.SYM, mask);
    }

    static void pickUp() throws GameActionException {
        MapLocation c = G.rc.getLocation();
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation l = c.translate(dx, dy);
                if (G.rc.canPickUpCheese(l)) G.rc.pickUpCheese(l);
            }
    }

    static void bite(RobotInfo[] enemies) throws GameActionException {
        if (!G.rc.isActionReady()) return;
        RobotInfo best = null;
        for (RobotInfo e : enemies) {
            if (!G.rc.canAttack(e.location)) continue;
            if (best == null || e.health < best.health) best = e;
        }
        if (best != null) G.rc.attack(best.location);
    }

    /** Committed reserve: upkeep for every king for a horizon, never spent on spawns. */
    static int reserve() {
        int kings = Math.max(1, Comms.numKings);
        int horizon = Math.min(Cfg.RESERVE_ROUNDS, Math.max(0, 2000 - G.round));
        return 2 * kings * horizon;
    }

    static void spawn(RobotInfo[] cats, RobotInfo[] enemies) throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.isActionReady()) return;
        int cost = rc.getCurrentRatCost();
        int cheese = rc.getGlobalCheese();
        int babies = (cost - 10) / 10 * 4; // lower bound of live babies (cost step)
        boolean cheap = cost <= 30;
        if (cheese < cost + reserve() && !(cheap && cheese >= cost + 2 * 60)) return;
        if (babies >= Cfg.MAX_BABIES) return;
        // Spawn toward the most useful direction: away from visible cats, else toward the map centre.
        MapLocation c = rc.getLocation();
        MapLocation goal = new MapLocation(G.W / 2, G.H / 2);
        MapLocation cat = null;
        for (RobotInfo r : cats) { cat = r.location; break; }
        MapLocation best = null;
        int bs = Integer.MIN_VALUE;
        for (int dx = -2; dx <= 2; dx++)
            for (int dy = -2; dy <= 2; dy++) {
                if (Math.abs(dx) < 2 && Math.abs(dy) < 2) continue;
                MapLocation l = c.translate(dx, dy);
                if (!rc.canBuildRat(l)) continue;
                int s = -l.distanceSquaredTo(goal);
                if (cat != null) s += 4 * l.distanceSquaredTo(cat);
                // ring corners are poorer delivery lanes; keep them as last choice
                if (Math.abs(dx) == 2 && Math.abs(dy) == 2) s -= 50;
                s = s * 4 + (G.hand(c, c.directionTo(l)) > 0 ? 1 : 0);
                if (s > bs) { bs = s; best = l; }
            }
        if (best != null) {
            rc.buildRat(best);
            spawned++;
        } else {
            digRing(goal);
        }
    }

    public static int digs;

    /** No spawn tile is open: dig the ring dirt tile nearest the goal (kings dig within range 8). */
    static void digRing(MapLocation goal) throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.isActionReady() || rc.getGlobalCheese() < 5 + 2 * 20) return;
        MapLocation c = rc.getLocation();
        MapLocation best = null;
        int bs = Integer.MAX_VALUE;
        for (int dx = -2; dx <= 2; dx++)
            for (int dy = -2; dy <= 2; dy++) {
                if (Math.abs(dx) < 2 && Math.abs(dy) < 2) continue;
                MapLocation l = c.translate(dx, dy);
                if (!rc.canRemoveDirt(l)) continue;
                int s = l.distanceSquaredTo(goal) * 2 + (G.hand(c, c.directionTo(l)) > 0 ? 1 : 0);
                if (s < bs) { bs = s; best = l; }
            }
        if (best != null) { rc.removeDirt(best); digs++; }
    }

    static void move(RobotInfo[] cats) throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.isMovementReady()) return;
        MapLocation c = rc.getLocation();
        for (RobotInfo cat : cats) {
            if (c.distanceSquaredTo(cat.location) <= 20) {
                Direction away = cat.location.directionTo(c);
                Direction[] cand = {away, away.rotateLeft(), away.rotateRight()};
                for (Direction d : cand) if (rc.canMove(d)) { rc.move(d); return; }
            }
        }
    }
}
