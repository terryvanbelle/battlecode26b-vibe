package s2q;

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
        Comms.readKings();
        Econ.startTurn();
        Comms.readMines();
        sense();
        readSqueaks();
        publish();

        RobotInfo[] enemies = rc.senseNearbyRobots(-1, G.them);
        RobotInfo[] cats = rc.senseNearbyRobots(-1, Team.NEUTRAL);

        int before = rc.getGlobalCheese();
        pickUp();
        Econ.received(rc.getGlobalCheese() - before);
        // a fresh rat (100 HP, bites every turn) is worth far more than one king bite: spawn first
        spawn(cats, enemies);
        if (!G.coop) bite(enemies);
        if (!G.coop && enemies.length > 0) {
            Comms.write(Comms.ALERT, G.me.x + 1);
            Comms.write(Comms.ALERT + 1, G.me.y + 1);
            Comms.write(Comms.ALERT + 2, Comms.stamp());
        }
        if (Cfg.KING_TRAPS && !G.coop) ratTrap(enemies);
        if (Cfg.FORM_KING) formation();
        move(cats, enemies);
        publish();
        Comms.write(Comms.BUDGET, Math.max(0, Math.min(1023, Econ.surplus() / 4)));
        Econ.endTurn();
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
        if (best == null) return;
        int c = biteCheese(best.health, G.rc.getGlobalCheese());
        if (c > 0 && G.rc.canAttack(best.location, c)) G.rc.attack(best.location, c);
        else G.rc.attack(best.location);
    }

    /** Cheese for a king bite on a target with hp: the least that kills now (at most KING_BITE_MAX), else 0. */
    static int biteCheese(int hp, int global) {
        int budget = Math.min(Cfg.KING_BITE_MAX, global - Cfg.KING_BITE_FLOOR);
        if (budget <= 0 || hp <= 10) return 0;
        int k = hp - 10;
        return k * k <= budget ? k * k : 0;
    }

    static void spawn(RobotInfo[] cats, RobotInfo[] enemies) throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.isActionReady()) return;
        int cost = rc.getCurrentRatCost();
        int babies = (cost - 10) / 10 * 4; // lower bound of live babies (cost step)
        // a few babies always (they pay for themselves on any map with cheese); beyond that the projected
        // end-of-game cheese at the measured income must stay positive after paying for this one
        // a core population always (rats pay for themselves and are the army); beyond it, the income test
        boolean low = babies < Cfg.LOW_BABIES;
        boolean boot = babies < Cfg.CORE_BABIES && rc.getGlobalCheese() >= cost + (low ? Cfg.LOW_FLOOR : Cfg.CORE_FLOOR);
        // at war with enemies in sight, bodies now beat cheese later: spawn down to a short upkeep floor
        boolean war = !G.coop && enemies.length > 0 && rc.getGlobalCheese() >= cost + (low ? Cfg.LOW_FLOOR : Cfg.WAR_FLOOR);
        if (!boot && !war && Econ.surplus() < cost + Cfg.SPAWN_MARGIN) return;
        // never spend the king's upkeep: SPAWN_FLOOR cheese (or what the rest of the game needs) stays
        if (rc.getGlobalCheese() < cost + Math.min(low ? Cfg.LOW_FLOOR : Cfg.SPAWN_FLOOR, 2 * (2000 - G.round))) return;
        if (babies >= (war ? Cfg.MAX_BABIES_WAR : Cfg.MAX_BABIES)) return;
        // Spawn toward the most useful direction: away from visible cats, else toward the map centre.
        MapLocation c = rc.getLocation();
        MapLocation goal = new MapLocation(G.W / 2, G.H / 2);
        MapLocation cat = null;
        for (RobotInfo r : cats) { cat = r.location; break; }
        if (!G.coop && enemies.length > 0) goal = enemies[0].location; // fresh rats join the fight at once
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

    public static int traps;

    /** War defense: a rat trap on the reachable tile nearest an approaching enemy rat (50 damage + stun). */
    static void ratTrap(RobotInfo[] enemies) throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.isActionReady() || rc.getGlobalCheese() < Cfg.RAT_TRAP_MIN_CHEESE || rc.getNumberRatTraps() >= Cfg.TRAP_CAP) return;
        MapLocation c = rc.getLocation();
        MapLocation foe = null;
        int fd = Integer.MAX_VALUE;
        for (RobotInfo e : enemies) {
            if (e.type == UnitType.RAT_KING) continue;
            int d = c.distanceSquaredTo(e.location);
            if (d < fd) { fd = d; foe = e.location; }
        }
        if (foe == null || fd > Cfg.KING_TRAP_DIST2) {
            // fortress: no one close yet, so pre-lay a few traps on the side facing the map centre
            if (Cfg.FORT_TRAPS <= 0 || rc.getNumberRatTraps() >= Cfg.FORT_TRAPS) return;
            foe = new MapLocation(G.W / 2, G.H / 2);
        }
        MapLocation best = null;
        int bs = Integer.MAX_VALUE;
        for (int dx = -2; dx <= 2; dx++)
            for (int dy = -2; dy <= 2; dy++) {
                if (Math.abs(dx) < 2 && Math.abs(dy) < 2) continue;
                MapLocation l = c.translate(dx, dy);
                if (!rc.canPlaceRatTrap(l)) continue;
                // the trap fires when the enemy enters its 3x3: covering the tile next to the foe's line in
                int s2 = l.distanceSquaredTo(foe) * 2 + (G.hand(c, c.directionTo(l)) > 0 ? 1 : 0);
                if (s2 < bs) { bs = s2; best = l; }
            }
        if (best != null) { rc.placeRatTrap(best); traps++; }
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

    static void move(RobotInfo[] cats, RobotInfo[] enemies) throws GameActionException {
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
        if (!Cfg.KING_MINE) return;
        if (enemies.length > 0 || cats.length > 0) { mineWhy = enemies.length > 0 ? 'e' : 'c'; return; }
        if (homeMine == null) homeMine = pickHomeMine(start);
        if (homeMine == null) { mineWhy = 'n'; return; }
        if (G.cheb(c, homeMine) <= 1) { mineWhy = 'a'; return; }
        mineWhy = 'm';
        Direction to = c.directionTo(homeMine);
        Direction[] cand = {to, to.rotateLeft(), to.rotateRight()};
        for (Direction d : cand) if (rc.canMove(d)) { rc.move(d); G.me = rc.getLocation(); return; }
    }

    static MapLocation formSite;
    static int formStart = -1000;

    /** The first king names a formation site and keeps it fresh until a second king exists or it times out. */
    static void formation() {
        if (slot != 0) return;
        if (Comms.numKings >= 2) { formSite = null; Comms.write(Comms.FORM_T, 0); return; }
        if (formSite != null) {
            if (G.round - formStart > Cfg.FORM_TTL) { formSite = null; Comms.write(Comms.FORM_T, 0); return; }
            Comms.write(Comms.FORM_T, Comms.stamp());
            return;
        }
        int babies = (G.rc.getCurrentRatCost() - 10) / 10 * 4;
        if (G.round < Cfg.FORM_FROM || G.round > 1150 || G.round - formStart < Cfg.FORM_TTL + Cfg.FORM_RETRY
                || babies < Cfg.FORM_BABIES || G.rc.getGlobalCheese() < Cfg.FORM_CHEESE) return;
        formSite = pickFormSite(G.me);
        if (formSite == null) return;
        formStart = G.round;
        Comms.write(Comms.FORM_X, formSite.x + 1);
        Comms.write(Comms.FORM_Y, formSite.y + 1);
        Comms.write(Comms.FORM_T, Comms.stamp());
    }

    /** The nearest clearly-ours mine more than 4 tiles from us with a clear 3x3, else a clear spot behind us. */
    static MapLocation pickFormSite(MapLocation from) {
        int s = (Sym.mask & Sym.ROT) != 0 ? Sym.ROT : (Sym.mask & Sym.HOR) != 0 ? Sym.HOR : Sym.VER;
        MapLocation ek = G.mirror(start, s);
        MapLocation best = null;
        int bd = Integer.MAX_VALUE;
        for (int i = 0; i < Comms.numMines; i++) {
            MapLocation m = Comms.mines[i];
            // clearly ours: at most half as far from our start as from theirs
            if (G.cheb(m, from) <= 4 || 4 * m.distanceSquaredTo(start) > m.distanceSquaredTo(ek) || !clear3(m)) continue;
            int d = from.distanceSquaredTo(m);
            if (d < bd) { bd = d; best = m; }
        }
        if (best != null) return best;
        // else a clear spot four tiles behind us, away from the enemy start
        Direction back = ek.directionTo(from);
        MapLocation b = from.translate(4 * back.dx, 4 * back.dy);
        return clear3(b) ? b : null;
    }

    /** The 3x3 around c is on the map and known free of walls and dirt. */
    static boolean clear3(MapLocation c) {
        if (c.x < 1 || c.y < 1 || c.x > G.W - 2 || c.y > G.H - 2) return false;
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++) {
                int m = Sym.mem[(c.x + dx) + G.W * (c.y + dy)];
                if ((m & Sym.SEEN) == 0 || (m & (Sym.WALL | Sym.DIRT)) != 0) return false;
            }
        return true;
    }

    static MapLocation homeMine;
    static char mineWhy = '-';

    /** The known mine nearest our start, within KING_MINE_R2 and on our half: the king sits on it. */
    static MapLocation pickHomeMine(MapLocation from) {
        int s = (Sym.mask & Sym.ROT) != 0 ? Sym.ROT : (Sym.mask & Sym.HOR) != 0 ? Sym.HOR : Sym.VER;
        MapLocation ek = G.mirror(from, s);
        MapLocation best = null;
        int bd = Cfg.KING_MINE_R2 + 1;
        for (int i = 0; i < Comms.numMines; i++) {
            MapLocation m = Comms.mines[i];
            int d = from.distanceSquaredTo(m);
            if (d < bd && d < m.distanceSquaredTo(ek)) { bd = d; best = m; }
        }
        return best;
    }
}
