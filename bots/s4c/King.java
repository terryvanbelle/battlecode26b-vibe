package s4c;

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
        spawnWhy = '-';
        if (slot < 0) {
            claimSlot();
            start = G.me;
            if (G.round <= 1) initialSymmetry();
        }
        Debug.mark(41);
        Comms.readKings();
        Econ.startTurn();
        Comms.readMines();
        Debug.mark(42);
        sense();
        Debug.mark(43);
        readSqueaks();
        publish();
        Debug.mark(44);

        RobotInfo[] enemies = rc.senseNearbyRobots(-1, G.them);
        RobotInfo[] cats = rc.senseNearbyRobots(-1, Team.NEUTRAL);

        int before = rc.getGlobalCheese();
        pickUp();
        Econ.received(rc.getGlobalCheese() - before);
        Debug.mark(45);
        // a fresh rat (100 HP, bites every turn) is worth far more than one king bite: spawn first
        spawn(cats, enemies);
        Debug.mark(46);
        if (!G.coop) bite(enemies);
        if (!G.coop && enemies.length > 0) {
            int threat = 0;
            for (RobotInfo e : enemies)
                if (e.type != UnitType.RAT_KING && e.location.distanceSquaredTo(G.me) <= Cfg.KING_THREAT_D2) threat++;
            Comms.write(Comms.ALERT, (G.me.x + 1) | (Math.min(15, threat) << 6));
            Comms.write(Comms.ALERT + 1, G.me.y + 1);
            Comms.write(Comms.ALERT + 2, Comms.stamp());
        }
        if (Cfg.KING_TRAPS && !G.coop) ratTrap(enemies);
        if (Cfg.FORM_KING) formation();
        Debug.mark(47);
        move(cats, enemies);
        Debug.mark(48);
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

    static MapLocation nearCheese;   // nearest cheese pile within the roam leash, this turn

    static void sense() throws GameActionException {
        MapInfo[] infos = G.rc.senseNearbyMapInfos();
        MapLocation me = G.rc.getLocation(), anchor = homeMine != null ? homeMine : start;
        nearCheese = null;
        int bd = Integer.MAX_VALUE;
        // the first turn sees every tile new and pays for Sym.init too (60x60: 13.7k + 3.6k of 20k), so it stops
        // KING_SENSE_RESERVE short of the limit rather than overrun; the rest is recorded next turn
        int floor = G.round <= 1 ? Cfg.KING_SENSE_RESERVE : 0;
        for (MapInfo mi : infos) {
            if (floor > 0 && Clock.getBytecodesLeft() < floor) break;
            MapLocation l = mi.getMapLocation();
            boolean mine = mi.hasCheeseMine();
            Sym.record(l.x, l.y, mi.isWall(), mi.isDirt(), mine);
            if (mine) Comms.addMine(l.x, l.y);
            if (Cfg.KING_ROAM > 0 && anchor != null && mi.getCheeseAmount() > 0 && G.cheb(l, anchor) <= Cfg.KING_ROAM) {
                int d = me.distanceSquaredTo(l);
                if (d < bd) { bd = d; nearCheese = l; }
            }
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

    /** Most babies we keep: the war or peace cap, raised to RICH_BABIES while the bank holds RICH_BANK. */
    static int babyCap(boolean war, int bank) {
        int cap = war ? Cfg.MAX_BABIES_WAR : Cfg.MAX_BABIES;
        return Cfg.RICH_BABIES > cap && bank >= Cfg.RICH_BANK ? Cfg.RICH_BABIES : cap;
    }

    /** The last spawn() outcome (Debug.KWHY): n not action-ready, s no surplus, f under the floor, c at the cap,
     *  b built, d no free tile (dug the ring). sawEnemy: enemies in view after cooperation. */
    static char spawnWhy = '-';
    static boolean sawEnemy;

    static void spawn(RobotInfo[] cats, RobotInfo[] enemies) throws GameActionException {
        RobotController rc = G.rc;
        sawEnemy = !G.coop && enemies.length > 0;
        spawnWhy = 'n';
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
        spawnWhy = 's';
        if (!boot && !war && Econ.surplus() < cost + Cfg.SPAWN_MARGIN) return;
        // never spend the king's upkeep: SPAWN_FLOOR cheese (or what the rest of the game needs) stays
        spawnWhy = 'f';
        if (rc.getGlobalCheese() < cost + Math.min(low ? Cfg.LOW_FLOOR : Cfg.SPAWN_FLOOR, 2 * (2000 - G.round))) return;
        spawnWhy = 'c';
        if (babies >= babyCap(war, rc.getGlobalCheese())) return;
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
            spawnWhy = 'b';
        } else {
            digRing(goal);
            spawnWhy = 'd';
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
        if (Cfg.KING_DODGE) {
            if (dodge(c, cats)) { mineWhy = 'c'; return; }
        } else for (RobotInfo cat : cats) {
            if (c.distanceSquaredTo(cat.location) <= 20) {
                Direction away = cat.location.directionTo(c);
                Direction[] cand = {away, away.rotateLeft(), away.rotateRight()};
                for (Direction d : cand) if (rc.canMove(d)) { rc.move(d); return; }
            }
        }
        if (!Cfg.KING_MINE) return;
        if (enemies.length > 0 || cats.length > 0) { mineWhy = enemies.length > 0 ? 'e' : 'c'; return; }
        if (homeMine == null) homeMine = pickHomeMine(start);
        // a pile the king can reach is cheese no rat has to carry: walk until it is under our 3x3
        if (nearCheese != null && G.cheb(c, nearCheese) > 1 && step(c, nearCheese)) { mineWhy = 'p'; pickUp(); return; }
        if (homeMine == null) { mineWhy = 'n'; return; }
        if (G.cheb(c, homeMine) <= 1) { mineWhy = 'a'; return; }
        mineWhy = 'm';
        if (!Cfg.KING_DIG) {
            if (step(c, homeMine)) pickUp();
            return;
        }
        // a step that gets closer, else dig one (a sidestep that only keeps the distance jitters at a dirt face)
        if (closer(c, homeMine)) { pickUp(); return; }
        // blocked: tag why the king does not dig (x cooldown, y cheese, z no dirt-only step, q engine refused)
        if (!G.rc.isActionReady()) { mineWhy = 'x'; return; }
        if (G.rc.getGlobalCheese() < Cfg.KING_DIG_MIN) { mineWhy = 'y'; return; }
        MapLocation d = digTile(c, homeMine);
        if (d == null) { mineWhy = 'z'; if (step(c, homeMine)) pickUp(); return; }
        if (!G.rc.canRemoveDirt(d)) { mineWhy = 'q'; return; }
        G.rc.removeDirt(d); digs++; Sym.mem[G.idx(d)] &= ~Sym.DIRT; mineWhy = 'D';
    }

    /**
     * A cat walking to its waypoint scratches whatever blocks its next footprint, and stays put while it does: step the
     * 3x3 out of a near cat's next two footprints (then off its body, then away) when some step is safer than staying.
     */
    static boolean dodge(MapLocation c, RobotInfo[] cats) throws GameActionException {
        RobotController rc = G.rc;
        boolean near = false;
        for (RobotInfo cat : cats) if (c.distanceSquaredTo(cat.location) <= Cfg.KING_DODGE_R2) near = true;
        if (!near) return false;
        int best = 2 * catRisk(c, cats), bi = -1;
        for (int i = 0; i < 8; i++) {
            Direction d = G.DIRS[i];
            if (!rc.canMove(d)) continue;
            int s = 2 * catRisk(c.add(d), cats) + (G.hand(c, d) > 0 ? 1 : 0);
            if (s < best) { best = s; bi = i; }
        }
        if (bi >= 0) {
            rc.move(G.DIRS[bi]);
            G.me = rc.getLocation();
            return true;
        }
        // boxed in by dirt (a king's home is often a dirt pocket): dig the leading edge of the safest dirt-only step
        if (!Cfg.KING_DODGE_DIG || !rc.isActionReady() || rc.getGlobalCheese() < Cfg.KING_DIG_MIN) return false;
        MapLocation dig = null;
        for (int i = 0; i < 8; i++) {
            Direction d = G.DIRS[i];
            MapLocation t = dirtStep(c, d);
            if (t == null) continue;
            int s = 2 * catRisk(c.add(d), cats) + (G.hand(c, d) > 0 ? 1 : 0);
            if (s < best) { best = s; dig = t; }
        }
        if (dig == null || !rc.canRemoveDirt(dig)) return false;
        rc.removeDirt(dig);
        digs++;
        Sym.mem[G.idx(dig)] &= ~Sym.DIRT;
        mineWhy = 'C';
        return true;
    }

    /** The first dirt tile among those a king step in d adds to its 3x3, when the step adds dirt and no wall, unseen
     *  or off-map tile; else null. The added tiles are within dist2 8 of the king, its dig range. */
    static MapLocation dirtStep(MapLocation c, Direction d) {
        MapLocation nc = c.add(d), first = null;
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++) {
                int x = nc.x + dx, y = nc.y + dy;
                if (Math.abs(x - c.x) <= 1 && Math.abs(y - c.y) <= 1) continue;
                if (x < 0 || y < 0 || x >= G.W || y >= G.H) return null;
                int m = Sym.mem[x + G.W * y];
                if ((m & Sym.SEEN) == 0 || (m & Sym.WALL) != 0) return null;
                if ((m & Sym.DIRT) != 0 && first == null) first = new MapLocation(x, y);
            }
        return first;
    }

    /** Lower is safer for a king centred at k: 250 per king tile in a cat's next footprint, 75 per tile in its second
     *  (the 3x3 is wider than the 2x2 lane, so leaving it can take two steps), 100 touching its body, less up to 100
     *  for distance from the body's centre. */
    static int catRisk(MapLocation k, RobotInfo[] cats) {
        int s = 0;
        for (RobotInfo cat : cats) {
            MapLocation b = cat.location;
            if (k.distanceSquaredTo(b) > Cfg.KING_DODGE_R2 + 16) continue;
            MapLocation n1 = b.add(cat.direction), n2 = n1.add(cat.direction);
            s += 250 * catOverlap(k, n1) + 75 * catOverlap(k, n2);
            if (catGap(k, b) <= 1) s += 100;
            s -= Math.min(100, Baby.catDist4(b, k));
        }
        return s;
    }

    /** Tiles shared by a king's 3x3 centred at k and a cat's 2x2 from bottom-left b. */
    static int catOverlap(MapLocation k, MapLocation b) {
        int ox = Math.min(k.x + 1, b.x + 1) - Math.max(k.x - 1, b.x) + 1;
        int oy = Math.min(k.y + 1, b.y + 1) - Math.max(k.y - 1, b.y) + 1;
        return ox > 0 && oy > 0 ? ox * oy : 0;
    }

    /** Chebyshev gap between a king's 3x3 centred at k and a cat's 2x2 from bottom-left b: 0 overlap, 1 touching. */
    static int catGap(MapLocation k, MapLocation b) {
        int gx = Math.max(0, Math.max(b.x - (k.x + 1), (k.x - 1) - (b.x + 1)));
        int gy = Math.max(0, Math.max(b.y - (k.y + 1), (k.y - 1) - (b.y + 1)));
        return Math.max(gx, gy);
    }

    /** One king step toward t that strictly shortens the distance: straight or 45 degrees off. */
    static boolean closer(MapLocation c, MapLocation t) throws GameActionException {
        RobotController rc = G.rc;
        Direction to = c.directionTo(t);
        int d0 = c.distanceSquaredTo(t);
        Direction[] cand = {to, to.rotateLeft(), to.rotateRight()};
        for (Direction d : cand) {
            if (!rc.canMove(d) || c.add(d).distanceSquaredTo(t) >= d0) continue;
            rc.move(d); G.me = rc.getLocation(); return true;
        }
        return false;
    }

    /** Dirt blocks the king's walk to t: a dirt tile of the step toward t (or 45 degrees off it, never farther from
     *  t) with the fewest dirt tiles and no wall, unseen or off-map tile among the tiles the step adds to the 3x3.
     *  Every added tile is within dist2 8 of the king, its dig range. Null when no step is blocked only by dirt. */
    static MapLocation digTile(MapLocation c, MapLocation t) {
        Direction to = c.directionTo(t);
        if (to == Direction.CENTER) return null;
        Direction[] cand = {to, to.rotateLeft(), to.rotateRight()};
        MapLocation best = null;
        int bn = Integer.MAX_VALUE;
        for (Direction d : cand) {
            MapLocation nc = c.add(d);
            if (G.cheb(nc, t) > G.cheb(c, t)) continue;
            int n = 0;
            MapLocation first = null;
            boolean ok = true;
            for (int dx = -1; dx <= 1 && ok; dx++)
                for (int dy = -1; dy <= 1; dy++) {
                    int x = nc.x + dx, y = nc.y + dy;
                    if (Math.abs(x - c.x) <= 1 && Math.abs(y - c.y) <= 1) continue;   // already under us
                    if (x < 0 || y < 0 || x >= G.W || y >= G.H) { ok = false; break; }
                    int m = Sym.mem[x + G.W * y];
                    if ((m & Sym.SEEN) == 0 || (m & Sym.WALL) != 0) { ok = false; break; }
                    if ((m & Sym.DIRT) != 0) {
                        n++;
                        if (first == null) first = new MapLocation(x, y);
                    }
                }
            if (ok && n > 0 && n < bn) { bn = n; best = first; }
        }
        return best;
    }

    /** One king step toward t; babies crowd a king, so sidestep up to 90 degrees off the line, never farther. */
    static boolean step(MapLocation c, MapLocation t) throws GameActionException {
        RobotController rc = G.rc;
        Direction to = c.directionTo(t);
        Direction[] cand = {to, to.rotateLeft(), to.rotateRight(), to.rotateLeft().rotateLeft(), to.rotateRight().rotateRight()};
        for (Direction d : cand) {
            if (!rc.canMove(d)) continue;
            if (G.cheb(c.add(d), t) > G.cheb(c, t)) continue;
            rc.move(d); G.me = rc.getLocation(); return true;
        }
        return false;
    }

    static MapLocation formSite;
    static int formStart = -1000;

    /** The first king names a formation site and keeps it fresh until a second king exists or it times out. */
    static void formation() {
        if (slot != 0) return;
        if (Comms.numKings >= Math.min(Cfg.FORM_MAX, Comms.KING_SLOTS)) { formSite = null; Comms.write(Comms.FORM_T, 0); return; }
        if (formSite != null) {
            if (G.round - formStart > Cfg.FORM_TTL) { formSite = null; Comms.write(Comms.FORM_T, 0); return; }
            Comms.write(Comms.FORM_T, Comms.stamp());
            return;
        }
        int babies = (G.rc.getCurrentRatCost() - 10) / 10 * 4;
        if (G.round < Cfg.FORM_FROM || G.round > 1150 || G.round - formStart < Cfg.FORM_TTL + Cfg.FORM_RETRY
                || babies < Cfg.FORM_BABIES || G.rc.getGlobalCheese() < Cfg.FORM_CHEESE || !formIncome()) return;
        formSite = pickFormSite(G.me);
        if (formSite == null) return;
        formStart = G.round;
        Comms.write(Comms.FORM_X, formSite.x + 1);
        Comms.write(Comms.FORM_Y, formSite.y + 1);
        Comms.write(Comms.FORM_T, Comms.stamp());
    }

    /** The income test for one more king (Cfg.FORM_NET100). */
    static boolean formIncome() {
        if (Cfg.FORM_NET100 == Integer.MIN_VALUE) return true;
        return Econ.rate100() * Cfg.INCOME_TRUST / 100 - 200 * (Econ.kings() + 1) >= Cfg.FORM_NET100;
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
            if (nearKing(m)) continue;
            int d = from.distanceSquaredTo(m);
            if (d < bd) { bd = d; best = m; }
        }
        if (best != null) return best;
        // else a clear spot four tiles behind us, away from the enemy start
        Direction back = ek.directionTo(from);
        MapLocation b = from.translate(4 * back.dx, 4 * back.dy);
        return clear3(b) ? b : null;
    }

    /** Some live king of ours is within Chebyshev 4 of m (it holds that mine). */
    static boolean nearKing(MapLocation m) {
        for (int k = 0; k < Comms.numKings; k++)
            if (G.cheb(m, Comms.kings[k]) <= 4) return true;
        return false;
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
            if (d >= bd || d >= m.distanceSquaredTo(ek)) continue;
            // another king already holds it
            boolean held = false;
            for (int k = 0; k < Comms.numKings; k++)
                if (G.cheb(Comms.kings[k], m) <= 2 && !Comms.kings[k].equals(G.me)) held = true;
            if (!held) { bd = d; best = m; }
        }
        return best;
    }
}
