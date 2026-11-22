package xx5;

import battlecode.common.*;

/**
 * Rat king, once a turn: read the shared array and squeaks, record what it senses, publish its heartbeat and the
 * symmetry, spawn babies, bite enemies in reach, raise the alert at war, lead king formation, then move (dodge thrown
 * rats and cats, leave cat lanes and passages, walk to its home mine) and work on its path search (KPath).
 */
public final class King {
    /** Our king slot in the shared array (-1 before the first turn), and the centre we started on. */
    static int slot = -1;
    static MapLocation start;
    public static int spawned;

    /** The last round a formation site may be opened in (and the last a rich bank buys a king in). */
    static final int FORM_LAST_ROUND = 1150;

    /** Claim the first slot that is empty or whose heartbeat is older than 3 rounds (slot 0 when none is). */

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
        // our centre in Comms.kings (KPath.held skips it after we move)
        turnC = G.me;
        // every 16 rounds the stale stamps go (round & 1023 would make them fresh again 1024 rounds on)
        if ((G.round & 15) == 0) sweep();
        Econ.startTurn();
        Comms.readMines();
        Debug.mark(42);
        sense();
        Debug.mark(43);
        readSqueaks();
        mirrorMines();
        publish();
        Debug.mark(44);

        RobotInfo[] enemies = rc.senseNearbyRobots(-1, G.them);
        RobotInfo[] cats = rc.senseNearbyRobots(-1, Team.NEUTRAL);
        for (RobotInfo k : cats) markLane(k.location);

        pickUp();
        Debug.mark(45);
        // a fresh rat (100 HP, bites every turn) is worth far more than one king bite: spawn first
        spawnAndAnnounce(rc, cats, enemies);
        Debug.mark(46);
        if (!G.coop) bite(enemies);
        if (!G.coop && enemies.length > 0) raiseAlert(enemies);
        formation();
        Debug.mark(47);
        move(cats, enemies);
        Debug.mark(48);
        publish();
        Comms.write(Comms.BUDGET, Math.max(0, Math.min(1023, Econ.surplus() / 4)));
        Econ.endTurn();
        KPath.tick();
    }

    /** Our centre at the start of this turn (as Comms.kings lists it). */
    static MapLocation turnC;

    /** spawn(), and after a spawn with no cat in view, squeak the gatherers' origin and the newborn's place in its
     *  role's spawn order (SQ_START, heard by Baby.heardStart). */
    private static void spawnAndAnnounce(RobotController rc, RobotInfo[] cats, RobotInfo[] enemies) throws GameActionException {
        int before = spawned;
        spawn(cats, enemies);
        if (spawned > before && cats.length == 0) {
            MapLocation o = Baby.origin != null ? Baby.origin : start;
            rc.squeak(Comms.squeak(Comms.SQ_START, o.x, o.y, rrNext()));
        }
    }

    /** At war with enemies in view: write the ALERT slot (our centre and the threat, the enemy babies within
     *  KING_THREAT_D2 that are not behind a wall from us, at most 15). Enemies behind a wall neither count as threat
     *  nor, when every enemy seen is one, raise the alert; a king that wrote earlier this round with at least our
     *  threat keeps the slot. */
    private static void raiseAlert(RobotInfo[] enemies) {
        int threat = 0;
        for (RobotInfo e : enemies)
            if (e.type != UnitType.RAT_KING && e.location.distanceSquaredTo(G.me) <= Cfg.KING_THREAT_D2) threat++;
        int walledThreat = 0, open = 0;
        for (RobotInfo e : enemies) {
            if (e.type != UnitType.RAT_KING && Baby.behindWall(G.me, e.location)) {
                if (e.location.distanceSquaredTo(G.me) <= Cfg.KING_THREAT_D2) walledThreat++;
            } else open++;
        }
        threat -= walledThreat;
        boolean quiet = open == 0;
        if (!quiet && !(Comms.read(Comms.ALERT + 2) == Comms.stamp()
                        && (Comms.read(Comms.ALERT) >> 6) >= Math.min(15, threat))) {
            Comms.write(Comms.ALERT, (G.me.x + 1) | (Math.min(15, threat) << 6));
            Comms.write(Comms.ALERT + 1, G.me.y + 1);
            Comms.write(Comms.ALERT + 2, Comms.stamp());
        }
    }

    /** Zero the stamps (KING, ALERT, CAT slots, FORM_T) older than 63 rounds; every reader takes 30 or less. */
    static void sweep() {
        for (int k = 0; k < Comms.KING_SLOTS; k++) stale(Comms.KING0 + 3 * k + 2);
        stale(Comms.ALERT + 2);
        for (int k = 0; k < Comms.CAT_SLOTS; k++) stale(Comms.CAT0 + 3 * k + 2);
        stale(Comms.FORM_T);
    }

    /** Zero the stamp at index i when it is older than 63 rounds. */
    static void stale(int i) {
        int t = Comms.read(i);
        if (t != 0 && !Comms.fresh(t, 63)) Comms.write(i, 0);
    }

    /** The lowest king slot with a fresh heartbeat (ours is), the formation leader. */
    static int leadSlot() {
        for (int k = 0; k < slot; k++) {
            int b = Comms.KING0 + 3 * k;
            if (Comms.read(b) != 0 && Comms.fresh(Comms.read(b + 2), 2)) return k;
        }
        return slot;
    }

    /** The mine count and symmetry mask mirrorMines last ran with. */
    static int mirroredN = -1, mirroredMask;

    /** With one symmetry left, list every listed mine's twin. Runs when the list or the symmetry changed since the
     *  last run. */
    static void mirrorMines() {
        int s = Sym.mask;
        if (Integer.bitCount(s) != 1 || Comms.numMines == mirroredN && s == mirroredMask) return;
        mirrorInto(s);
        mirroredN = Comms.numMines;
        mirroredMask = s;
    }

    /** List the twin under symmetry s of every listed mine. */
    static void mirrorInto(int s) {
        for (int k = 0, n = Comms.numMines; k < n; k++) {
            MapLocation t = G.mirror(Comms.mines[k], s);
            Comms.addMine(t.x, t.y);
        }
    }

    /** Round 1: our start and the enemy start are mirror images; a candidate that maps us onto ourselves is impossible. */
    static void initialSymmetry() {
        int bad = gapMask(G.me, 2);
        if ((bad & Sym.HOR) != 0) Sym.eliminate(Sym.HOR);
        if ((bad & Sym.VER) != 0) Sym.eliminate(Sym.VER);
        if ((bad & Sym.ROT) != 0) Sym.eliminate(Sym.ROT);
    }

    /** The symmetries that mirror a king start c within Chebyshev g of itself (g 2: the two kings' bodies overlap). */
    static int gapMask(MapLocation c, int g) {
        int bad = 0;
        for (int s = 1; s <= 4; s <<= 1)
            if (Math.abs(G.mirX(c.x, s) - c.x) <= g && Math.abs(G.mirY(c.y, s) - c.y) <= g) bad |= s;
        return bad;
    }

    /** The nearest cheese pile within KING_ROAM of the home mine (or start) and outside our 3x3, this turn. */
    static MapLocation nearCheese;
    /** Cfg.KING_STRICT (a field, so tests can switch it). */
    static boolean strict = Cfg.KING_STRICT;

    /** Record every tile in view (Sym.record), list the mines, and find nearCheese. */
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
            if (anchor != null && mi.getCheeseAmount() > 0 && G.cheb(l, anchor) <= Cfg.KING_ROAM
                && G.cheb(l, me) > 1) {
                int d = me.distanceSquaredTo(l);
                if (d < bd) { bd = d; nearCheese = l; }
            }
        }
    }

    /** Act on up to 16 squeaks from this round and the last (kings act before the babies they spawned, so this
     *  round's squeaks mostly arrive after our turn): mines, symmetry, cat sightings. */
    static void readSqueaks() {
        Message[] ms = G.rc.readSqueaks(-1);
        int n = ms.length, used = 0;
        for (int i = 0; i < n && used < 16; i++) {
            if (ms[i].getRound() < G.round - 1) continue;
            used++;
            int m = ms[i].getBytes();
            switch (Comms.sqType(m)) {
                case Comms.SQ_MINE: Comms.addMine(Comms.sqX(m), Comms.sqY(m)); break;
                case Comms.SQ_SYM: Sym.merge(Comms.sqExtra(m) & 7); break;
                case Comms.SQ_CAT:
                    noteCat(Comms.sqX(m), Comms.sqY(m));
                    markLane(new MapLocation(Comms.sqX(m), Comms.sqY(m)));
                    break;
                default:
                    break;
            }
        }
    }

    /** List a cat sighting: slot 0 holds the most recent; the older one shifts to slot 1 unless it is the same cat
     *  area (within 6 tiles on both axes). */
    static void noteCat(int x, int y) {
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

    /** Write our heartbeat (centre and stamp) and the symmetry mask, intersected with the shared one unless that
     *  would leave none. */
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

    /** Picks up every pile under our 3x3 and counts it as Econ income (every caller). */
    static void pickUp() throws GameActionException {
        int b = G.rc.getGlobalCheese();
        MapLocation c = G.rc.getLocation();
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation l = c.translate(dx, dy);
                if (G.rc.canPickUpCheese(l)) G.rc.pickUpCheese(l);
            }
        Econ.received(G.rc.getGlobalCheese() - b);
    }

    /** Bite the weakest enemy in reach (a king through any tile of its 3x3), with the cheese that kills it now when
     *  the bank allows (biteCheese). */
    static void bite(RobotInfo[] enemies) throws GameActionException {
        if (!G.rc.isActionReady()) return;
        RobotInfo best = null;
        MapLocation at = null;
        for (RobotInfo e : enemies) {
            MapLocation t = e.location;
            if (e.type == UnitType.RAT_KING) { if ((t = kingTile(t)) == null) continue; }
            else if (!G.rc.canAttack(t)) continue;
            if (best == null || e.health < best.health) { best = e; at = t; }
        }
        if (best == null) return;
        int c = biteCheese(best.health, G.rc.getGlobalCheese());
        if (c > 0 && G.rc.canAttack(at, c)) G.rc.attack(at, c);
        else G.rc.attack(at);
    }

    /** A tile of the enemy king's 3x3 (centre ctr) we can attack, or null. */
    static MapLocation kingTile(MapLocation ctr) {
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation t = ctr.translate(dx, dy);
                if (G.rc.canAttack(t)) return t;
            }
        return null;
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
        if (G.round < Cfg.RICH_FROM && (G.W * G.H >= Cfg.RICH_FROM_AREA || G.coop)) return cap;
        if (bank >= Cfg.RICH2_BANK) return Math.max(Math.max(cap, Cfg.RICH_BABIES), Cfg.RICH2_BABIES);
        return Cfg.RICH_BABIES > cap && bank >= Cfg.RICH_BANK ? Cfg.RICH_BABIES : cap;
    }

    /** A spawn floor f: LOW_FLOOR under LOW_BABIES babies, else at least KING_RESERVE; at most siege when that is
     *  positive. */
    static int spawnFloor(int f, boolean low, int siege) {
        int r = low ? Cfg.LOW_FLOOR : Math.max(f, Cfg.KING_RESERVE);
        return siege > 0 ? Math.min(r, siege) : r;
    }

    /** Cfg.KING_RESERVE (or r) for our live kings, at most the upkeep left to round 2000. */
    static int reserve(int r) { return Econ.floor(Math.min(r, 2 * (2000 - G.round))); }

    /** Cfg.AUSTERE_FROM: the kings project starvation (Comms.BUDGET 0) from that round. */
    static boolean austere() {
        return G.round >= Cfg.AUSTERE_FROM && Comms.read(Comms.BUDGET) == 0;
    }

    /** The last spawn() outcome (Debug.KWHY): n not action-ready, s no surplus, f under the floor, c at the cap,
     *  b built, d no free tile (dug the ring). sawEnemy: enemies in view after cooperation. */
    static char spawnWhy = '-';
    static boolean sawEnemy;
    /** Spawns so far by the role each newborn reads off the rat cost (rrNext). */
    static final int[] rrCount = new int[3];

    /** The newborn's place in its role's spawn order, 1-based, by the role the rat cost after this spawn gives in
     *  this round. The newborn first acts next round (spawn order) and reads its role off that round's cost and parity,
     *  so the two can differ, and it never hears this squeak (Baby.heardStart). */
    static int rrNext() throws GameActionException {
        int idx = (G.rc.getCurrentRatCost() - 10) / 10 * 4;
        int r = Baby.roleFor(idx, G.round);
        return ++rrCount[r] % 4096 + 1;
    }

    /** Build a baby when the economy allows (spawnWhy says why not), on the free ring tile nearest the goal (the
     *  first enemy in view at war, else the map centre) and away from a cat in view; dig the ring when no tile is
     *  free. */
    static void spawn(RobotInfo[] cats, RobotInfo[] enemies) throws GameActionException {
        RobotController rc = G.rc;
        sawEnemy = !G.coop && enemies.length > 0;
        spawnWhy = 'n';
        if (!rc.isActionReady()) return;
        int cost = rc.getCurrentRatCost();
        // lower bound of live babies (cost step; the engine still counts each promoted rat as a baby)
        int babies = (cost - 10) / 10 * 4 - Math.max(0, Comms.numKings - 1);
        // a core population always (rats pay for themselves and are the army); beyond it, the projected
        // end-of-game cheese at the measured income must cover this one
        boolean low = babies < Cfg.LOW_BABIES;
        boolean siege = !G.coop && enemies.length > 0;
        int sf = siege ? Cfg.SIEGE_FLOOR : 0;
        if (cost == 10) sf = Cfg.LAST_FLOOR;   // no baby alive: the cheapest rat
        int core = G.W * G.H <= Cfg.CORE_SMALL_AREA ? Cfg.CORE_SMALL : Cfg.CORE_BABIES;
        if (austere()) core = Math.min(core, Cfg.AUSTERE_CORE);
        boolean boot = babies < core && rc.getGlobalCheese() >= cost + Econ.floor(spawnFloor(Cfg.CORE_FLOOR, low, sf));
        // at war with enemies in sight, bodies now beat cheese later: spawn down to a short upkeep floor
        boolean war = siege && rc.getGlobalCheese() >= cost + Econ.floor(spawnFloor(Cfg.WAR_FLOOR, low, sf));
        spawnWhy = 's';
        if (!boot && !war && Econ.surplus() < cost + Cfg.SPAWN_MARGIN) return;
        // never spend the king's upkeep: SPAWN_FLOOR cheese (or what the rest of the game needs) stays
        spawnWhy = 'f';
        if (rc.getGlobalCheese() < cost + Econ.floor(Math.min(spawnFloor(Cfg.SPAWN_FLOOR, low, sf), 2 * (2000 - G.round)))) return;
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
                s = s * 4 + (G.pickBit(c, l) ? 1 : 0);
                if (s > bs) { bs = s; best = l; }
            }
        if (best != null) {
            // a newborn takes its king's facing: face the spawn tile, so it looks outward along a line its twin mirrors
            if (faceSpawn) { Direction f = c.directionTo(best); if (rc.getDirection() != f && rc.canTurn(f)) rc.turn(f); }
            rc.buildRat(best);
            spawned++;
            spawnWhy = 'b';
        } else {
            digRing(goal);
            spawnWhy = 'd';
        }
    }

    /** Shown in the indicator (tr=); nothing counts king traps now. */
    public static int traps;
    /** Cfg.SYM_FACE (a field, so tests can switch it). */
    static boolean faceSpawn = Cfg.SYM_FACE;
    /** Cfg.KING_FRONT (a field, so tests can set it). */
    static int kingFront = Cfg.KING_FRONT;

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
                int s = l.distanceSquaredTo(goal) * 2 + (G.pickBit(c, l) ? 1 : 0);
                if (s < bs) { bs = s; best = l; }
            }
        if (best != null) { rc.removeDirt(best); digs++; }
    }

    /** Per offset (rx, ry) from the king, rx and ry in -2..2, the king positions (bit 3 (dy + 1) + dx + 1
     *  for a step dx, dy; bit 4 = stay) whose 3x3 covers that tile. Built on first use. */
    static int[] DODGE_MASK;
    /** Cfg.THROW_DODGE: the step that puts our 3x3 on the fewest flights of the enemy babies in view holding one of
     *  ours (within Chebyshev THROW_DODGE), or null when staying is as good. A flight runs from the carrier along any of
     *  the 8 directions for up to 8 tiles and ends at the first wall, dirt or robot other than this king; it counts for
     *  a king position when it reaches that 3x3 first. (Measured: carriers at Chebyshev 2-6 threw our own rats onto a
     *  king 15-17 times a game, 520-570 of its hp.) */
    static Direction throwDodge(MapLocation c, RobotInfo[] enemies) throws GameActionException {
        RobotController rc = G.rc;
        int[] hits = null;
        for (RobotInfo e : enemies) {
            if (e.type != UnitType.BABY_RAT || !Baby.holdsOurs(e) || G.cheb(e.location, c) > Cfg.THROW_DODGE) continue;
            if (hits == null) {
                hits = new int[9];
                if (DODGE_MASK == null) {
                    DODGE_MASK = new int[25];
                    for (int rx = -2; rx <= 2; rx++)
                        for (int ry = -2; ry <= 2; ry++) {
                            int m = 0;
                            for (int dx = -1; dx <= 1; dx++)
                                for (int dy = -1; dy <= 1; dy++)
                                    if (Math.abs(rx - dx) <= 1 && Math.abs(ry - dy) <= 1) m |= 1 << (3 * (dy + 1) + dx + 1);
                            DODGE_MASK[(rx + 2) * 5 + ry + 2] = m;
                        }
                }
            }
            int ex = e.location.x, ey = e.location.y;
            if (Clock.getBytecodesLeft() < 5000) break;
            for (Direction d : G.DIRS) {
                int ddx = d.dx, ddy = d.dy, x = ex, y = ey, seen = 0, kmax = 0;
                // the last k whose tile lies in our 5x5; flights that never enter it cost no sensing
                for (int k = 1; k <= 8; k++) {
                    int rx = ex + k * ddx - c.x, ry = ey + k * ddy - c.y;
                    if (rx >= -2 && rx <= 2 && ry >= -2 && ry <= 2) kmax = k;
                }
                if (kmax == 0) continue;
                for (int k = 1; k <= kmax; k++) {
                    x += ddx; y += ddy;
                    if (x < 0 || y < 0 || x >= G.W || y >= G.H) break;
                    int rx = x - c.x, ry = y - c.y;
                    boolean zone = rx >= -2 && rx <= 2 && ry >= -2 && ry <= 2;
                    // a tile of our own 3x3 never stops the flight; elsewhere walls, dirt and robots do
                    if (!(rx >= -1 && rx <= 1 && ry >= -1 && ry <= 1)) {
                        MapLocation t = new MapLocation(x, y);
                        if (rc.canSenseLocation(t) && (!rc.sensePassability(t) || rc.senseRobotAtLocation(t) != null)) break;
                    }
                    if (zone) seen |= DODGE_MASK[(rx + 2) * 5 + ry + 2];
                }
                for (int b = 0; b < 9; b++) if ((seen >> b & 1) != 0) hits[b]++;
            }
        }
        if (hits == null || hits[4] == 0) return null;
        Direction best = null;
        int bh = hits[4];
        for (Direction d : G.DIRS) {
            int b = 3 * (d.dy + 1) + d.dx + 1;
            if (hits[b] < bh && rc.canMove(d)) { bh = hits[b]; best = d; }
        }
        return best;
    }

    /** The king's move, first that applies: dodge thrown rats (T), dodge cats (c), leave a cat lane (l), hold with
     *  enemies or cats in view (e, c), unplug a passage (u), follow the KPath search, walk to a pile in reach (p),
     *  then walk or dig toward the home mine (mineWhy says which). */
    static void move(RobotInfo[] cats, RobotInfo[] enemies) throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.isMovementReady()) return;
        MapLocation c = rc.getLocation();
        Direction td = throwDodge(c, enemies);
        if (td != null) { rc.move(td); mineWhy = 'T'; return; }
        if (dodge(c, cats)) { mineWhy = 'c'; return; }
        if (offLane(c, cats)) { mineWhy = 'l'; return; }
        if (enemies.length > 0 || cats.length > 0) { mineWhy = enemies.length > 0 ? 'e' : 'c'; return; }
        if (unplug(c)) { mineWhy = 'u'; return; }
        if (KPath.follow(c)) return;
        if (homeMine == null) homeMine = pickHomeMine(start);
        // a pile the king can reach is cheese no rat has to carry: walk until it is under our 3x3
        if (nearCheese != null && G.cheb(c, nearCheese) > 1 && walkStep(c, nearCheese)) { mineWhy = 'p'; pickUp(); return; }
        if (homeMine == null) { mineWhy = 'n'; return; }
        if (G.cheb(c, homeMine) <= 1) { mineWhy = 'a'; return; }
        mineWhy = 'm';
        // a step that gets closer, else dig one (a sidestep that only keeps the distance jitters at a dirt face)
        if (closer(c, homeMine)) { pickUp(); return; }
        // blocked: tag why the king does not dig (x cooldown, y cheese, z no dirt-only step, q engine refused)
        if (!G.rc.isActionReady()) { mineWhy = 'x'; return; }
        if (G.rc.getGlobalCheese() < Cfg.KING_DIG_MIN) { mineWhy = 'y'; return; }
        MapLocation d = digTile(c, homeMine);
        if (d == null) { mineWhy = 'z'; if (walkStep(c, homeMine)) pickUp(); return; }
        if (!G.rc.canRemoveDirt(d)) { mineWhy = 'q'; return; }
        G.rc.removeDirt(d); digs++; Sym.mem[G.idx(d)] &= ~Sym.DIRT; mineWhy = 'D';
    }

    /** Lane tiles marked so far, and the lane under the king's 3x3 where move() found it. */
    static int laneN, lane0;

    /** Mark a cat's 2x2 (bottom-left b) as lane. */
    static void markLane(MapLocation b) {
        int[] mem = Sym.mem;
        for (int dx = 0; dx <= 1; dx++)
            for (int dy = 0; dy <= 1; dy++) {
                int x = b.x + dx, y = b.y + dy;
                if (x < 0 || y < 0 || x >= G.W || y >= G.H) continue;
                int i = x + G.W * y;
                if ((mem[i] & Sym.CAT) == 0) { mem[i] |= Sym.CAT; laneN++; }
            }
    }

    /** Lane tiles under a king's 3x3 centred at k (unrolled: a stuck king asks every turn). */
    static int laneCount(MapLocation k) {
        if (laneN == 0) return 0;
        int[] mem = Sym.mem;
        int w = G.W, x = k.x, y = k.y;
        if (x < 1 || y < 1 || x + 1 >= w || y + 1 >= G.H) {
            int n = 0;
            for (int xx = Math.max(0, x - 1); xx <= Math.min(w - 1, x + 1); xx++)
                for (int yy = Math.max(0, y - 1); yy <= Math.min(G.H - 1, y + 1); yy++)
                    if ((mem[xx + w * yy] & Sym.CAT) != 0) n++;
            return n;
        }
        int i = x - 1 + w * (y - 1), n = 0;
        if ((mem[i] & Sym.CAT) != 0) n++;
        if ((mem[i + 1] & Sym.CAT) != 0) n++;
        if ((mem[i + 2] & Sym.CAT) != 0) n++;
        i += w;
        if ((mem[i] & Sym.CAT) != 0) n++;
        if ((mem[i + 1] & Sym.CAT) != 0) n++;
        if ((mem[i + 2] & Sym.CAT) != 0) n++;
        i += w;
        if ((mem[i] & Sym.CAT) != 0) n++;
        if ((mem[i + 1] & Sym.CAT) != 0) n++;
        if ((mem[i + 2] & Sym.CAT) != 0) n++;
        return n;
    }

    /** A king step in d from move()'s tile adds no lane to its 3x3, or leaves less than lane0 (always
     *  true with no lane known). */
    static boolean laneOk(MapLocation c, Direction d) {
        if (laneN == 0) return true;
        int n = laneCount(c.add(d));
        return n == 0 || n < lane0;
    }

    /** On lane: the step leaving the least lane under the 3x3 (ties: farther from the nearest seen cat, then hand). */
    static boolean offLane(MapLocation c, RobotInfo[] cats) throws GameActionException {
        int n0 = laneCount(c);
        lane0 = n0;
        if (n0 == 0) return false;
        RobotController rc = G.rc;
        int best = n0 * 1000, bi = -1;
        for (int i = 0; i < 8; i++) {
            Direction d = G.DIRS[i];
            if (!rc.canMove(d) || !unplugged(c, d)) continue;
            MapLocation nc = c.add(d);
            int nl = laneCount(nc);
            if (nl >= n0) continue;
            int s = nl * 1000 + (G.hand(c, d) > 0 ? 1 : 0);
            int cd = 1000;
            for (RobotInfo cat : cats) cd = Math.min(cd, Baby.catDist4(cat.location, nc));
            s -= Math.min(cd, 998) / 2 * 2;
            if (s < best) { best = s; bi = i; }
        }
        if (bi < 0) return false;
        rc.move(G.DIRS[bi]);
        G.me = rc.getLocation();
        lane0 = laneCount(G.me);
        return true;
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
        riskPrep(cats);
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
        if (!rc.isActionReady() || rc.getGlobalCheese() < Cfg.KING_DIG_MIN) return false;
        if (Clock.getBytecodesLeft() < Cfg.DODGE_DIG_LEFT) return false;   // the rest of the turn first
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

    /** catRisk weights for a king tile in the cat's footprints 1..6 ahead (Cfg.CAT_RISK_STEPS). */
    static final int[] RISK_W = {250, 150, 100, 60, 40, 20};

    /** catRisk's footprints: cat j's i-th next 2x2 (bottom-left) at j * CAT_RISK_STEPS + i, filled once per dodge by
     *  riskPrep (walking them with MapLocation.add on every catRisk call cost a dodging king up to 9.9k bytecodes). */
    static int[] riskX = new int[0], riskY = new int[0];
    /** Tiles a cat's 2x2 at x shares with a king's 3x3 along one axis, by x - (king - 2) for 0..3. */
    static final int[] OV = {1, 2, 2, 1};

    /** Fill riskX/riskY: each cat's next CAT_RISK_STEPS footprints along its facing. */
    static void riskPrep(RobotInfo[] cats) {
        int n = Cfg.CAT_RISK_STEPS, m = cats.length * n;
        if (riskX.length < m) { riskX = new int[m]; riskY = new int[m]; }
        for (int j = 0, o = 0; j < cats.length; j++) {
            RobotInfo cat = cats[j];
            int x = cat.location.x, y = cat.location.y, dx = cat.direction.dx, dy = cat.direction.dy;
            for (int i = 0; i < n; i++, o++) { x += dx; y += dy; riskX[o] = x; riskY[o] = y; }
        }
    }

    /** Lower is safer for a king centred at k: LANE_RISK per known lane tile under the 3x3 (a dodge should not step
     *  into a patrol corridor); per near cat, RISK_W[i] per king tile in its i-th next footprint (a cat moves a tile
     *  every 2 rounds and a king every 4, so only a step off its path stops the scratches), 100 touching its body,
     *  less up to 100 for distance from it. */
    static int catRisk(MapLocation k, RobotInfo[] cats) {
        int s = Cfg.LANE_RISK * laneCount(k);
        int kx = k.x - 2, ky = k.y - 2, n = Cfg.CAT_RISK_STEPS;
        for (int j = 0; j < cats.length; j++) {
            RobotInfo cat = cats[j];
            MapLocation b = cat.location;
            if (k.distanceSquaredTo(b) > Cfg.KING_DODGE_R2 + 16) continue;
            // the overlap of footprint and 3x3, inlined: OV per axis
            for (int i = 0, o = j * n; i < n; i++, o++) {
                int dx = riskX[o] - kx, dy = riskY[o] - ky;
                if (((dx | dy) & ~3) == 0) s += RISK_W[i] * OV[dx] * OV[dy];
            }
            if (catGap(k, b) <= 1) s += 100;
            s -= Math.min(100, Baby.catDist4(b, k));
        }
        return s;
    }

    /** Chebyshev gap between a king's 3x3 centred at k and a cat's 2x2 from bottom-left b: 0 overlap, 1 touching. */
    static int catGap(MapLocation k, MapLocation b) {
        int gx = Math.max(0, Math.max(b.x - (k.x + 1), (k.x - 1) - (b.x + 1)));
        int gy = Math.max(0, Math.max(b.y - (k.y + 1), (k.y - 1) - (b.y + 1)));
        return Math.max(gx, gy);
    }

    /** The 16 tiles around a king's 3x3 at c, in ring order (corners at 0, 4, 8, 12). */
    static final int[] RX = {-2, -1, 0, 1, 2, 2, 2, 2, 2, 1, 0, -1, -2, -2, -2, -2},
                       RY = {-2, -2, -2, -2, -2, -1, 0, 1, 2, 2, 2, 2, 2, 1, 0, -1};

    /** Open arcs of the ring around a king's 3x3 at c: a wall, dirt or off-map tile closes a ring tile,
     *  except a closed corner whose two ring neighbours are open (rats cut it diagonally). 1 with none closed, 0 with
     *  all closed; 2 or more means the body may cut one open area from another. */
    static int ringArcs(MapLocation c) {
        int[] mem = Sym.mem;
        int w = G.W, h = G.H, blk = Sym.WALL | Sym.DIRT, closedMask = 0;
        for (int i = 0; i < 16; i++) {
            int x = c.x + RX[i], y = c.y + RY[i];
            if (x < 0 || y < 0 || x >= w || y >= h || (mem[x + w * y] & blk) != 0) closedMask |= 1 << i;
        }
        for (int i = 0; i < 16; i += 4)
            if ((closedMask >> i & 1) != 0 && (closedMask >> ((i + 1) & 15) & 1) == 0 && (closedMask >> ((i + 15) & 15) & 1) == 0)
                closedMask &= ~(1 << i);
        if (closedMask == 0) return 1;
        int arcs = 0;
        for (int i = 0; i < 16; i++)
            if ((closedMask >> i & 1) == 0 && (closedMask >> ((i + 15) & 15) & 1) != 0) arcs++;
        return arcs;
    }

    /** ringArcs(c) with the ring's 16 tiles read unrolled when the ring lies on the map (ringArcs itself
     *  otherwise), the corner rule and the arc count in bit operations. Same answer as ringArcs. */
    static int ringArcsFast(MapLocation c) {
        int x = c.x, y = c.y, w = G.W;
        if (x < 2 || y < 2 || x + 2 >= w || y + 2 >= G.H) return ringArcs(c);
        int[] mem = Sym.mem;
        int blk = Sym.WALL | Sym.DIRT, w2 = w + w, w3 = w2 + w, w4 = w3 + w;
        int b = x - 2 + w * (y - 2), cm = 0;
        if ((mem[b] & blk) != 0) cm |= 1;
        if ((mem[b + 1] & blk) != 0) cm |= 2;
        if ((mem[b + 2] & blk) != 0) cm |= 4;
        if ((mem[b + 3] & blk) != 0) cm |= 8;
        if ((mem[b + 4] & blk) != 0) cm |= 16;
        if ((mem[b + 4 + w] & blk) != 0) cm |= 32;
        if ((mem[b + 4 + w2] & blk) != 0) cm |= 64;
        if ((mem[b + 4 + w3] & blk) != 0) cm |= 128;
        if ((mem[b + 4 + w4] & blk) != 0) cm |= 256;
        if ((mem[b + 3 + w4] & blk) != 0) cm |= 512;
        if ((mem[b + 2 + w4] & blk) != 0) cm |= 1024;
        if ((mem[b + 1 + w4] & blk) != 0) cm |= 2048;
        if ((mem[b + w4] & blk) != 0) cm |= 4096;
        if ((mem[b + w3] & blk) != 0) cm |= 8192;
        if ((mem[b + w2] & blk) != 0) cm |= 16384;
        if ((mem[b + w] & blk) != 0) cm |= 32768;
        return arcsOf(cm);
    }

    /** ringArcs's corner rule and arc count for a 16-bit closed mask in ring order. */
    static int arcsOf(int cm) {
        int open = ~cm & 0xFFFF;
        // a closed corner (bit 0, 4, 8, 12) with both ring neighbours open opens: bit i of nx is ring tile i+1 open,
        // of pv tile i-1 open
        int nx = (open >>> 1 | open << 15) & 0xFFFF, pv = (open << 1 | open >>> 15) & 0xFFFF;
        cm &= ~(cm & 0x1111 & nx & pv);
        if (cm == 0) return 1;
        // arcs: open tiles whose previous ring tile is closed
        int v = ~cm & (cm << 1 | cm >>> 15) & 0xFFFF;
        v = v - (v >>> 1 & 0x5555);
        v = (v & 0x3333) + (v >>> 2 & 0x3333);
        v = v + (v >>> 4) & 0x0F0F;
        return v + (v >>> 8) & 0x1F;
    }

    /** The ring arcs of the king's own tile, cached for the tile and round they were counted for. */
    static MapLocation arcsAt;
    static int arcsHere, arcsRound = -1;

    /** ringArcsFast(c), counted once per tile and round. */
    static int arcsHere(MapLocation c) {
        if (arcsRound != G.round || !c.equals(arcsAt)) { arcsAt = c; arcsRound = G.round; arcsHere = ringArcsFast(c); }
        return arcsHere;
    }

    /** A move from c by d does not plug a passage (or plugs no worse than where the king stands). */
    static boolean unplugged(MapLocation c, Direction d) {
        if (KPath.left() < Cfg.PLUG_LEFT) return true;
        int a = ringArcsFast(c.add(d));
        return a <= 1 || a <= arcsHere(c);
    }

    /** Standing where the ring splits, step to the neighbour with the fewest arcs (fewer than here). */
    static boolean unplug(MapLocation c) throws GameActionException {
        if (KPath.left() < Cfg.PLUG_LEFT) return false;
        RobotController rc = G.rc;
        int here = arcsHere(c);
        if (here <= 1) return false;
        int best = 2 * here, bi = -1;
        for (int i = 0; i < 8; i++) {
            Direction d = G.DIRS[i];
            if (!rc.canMove(d)) continue;
            int s = 2 * ringArcsFast(c.add(d)) + (G.hand(c, d) > 0 ? 1 : 0);
            if (s < best) { best = s; bi = i; }
        }
        if (bi < 0) return false;
        rc.move(G.DIRS[bi]);
        G.me = rc.getLocation();
        return true;
    }

    /** The walk to a pile or the home mine: strictly closer steps under Cfg.KING_STRICT, else step's (sidesteps allowed). */
    static boolean walkStep(MapLocation c, MapLocation t) throws GameActionException {
        return strict ? closer(c, t) : step(c, t);
    }

    /** One king step toward t that strictly shortens the distance: straight or 45 degrees off. */
    static boolean closer(MapLocation c, MapLocation t) throws GameActionException {
        RobotController rc = G.rc;
        Direction to = c.directionTo(t);
        int d0 = c.distanceSquaredTo(t);
        Direction[] cand = G.fan(c, to);
        for (Direction d : cand) {
            if (!rc.canMove(d) || c.add(d).distanceSquaredTo(t) >= d0 || !unplugged(c, d) || !laneOk(c, d)) continue;
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
        Direction[] cand = G.fan(c, to);
        MapLocation best = null;
        int bn = Integer.MAX_VALUE;
        for (Direction d : cand) {
            MapLocation nc = c.add(d);
            if (G.cheb(nc, t) > G.cheb(c, t) || !laneOk(c, d)) continue;
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
        Direction[] cand = G.fan5(c, to);
        for (Direction d : cand) {
            if (!rc.canMove(d)) continue;
            if (G.cheb(c.add(d), t) > G.cheb(c, t) || !unplugged(c, d) || !laneOk(c, d)) continue;
            rc.move(d); G.me = rc.getLocation(); return true;
        }
        return false;
    }

    /** The open formation site (null when none), the round it opened, and the king count then. */
    static MapLocation formSite;
    static int formStart = -1000, formKings;

    /** The first king names a formation site and keeps it fresh until the king cap is reached, a king forms there
     *  or it times out. */
    static void formation() throws GameActionException {
        // (the lowest live slot leads, so the role passes on when the slot-0 king dies)
        if (slot != 0 && leadSlot() != slot) return;
        if (!G.coop && G.round < Cfg.FORM_WAR_FROM) { formSite = null; Comms.write(Comms.FORM_T, 0); return; }
        if (Comms.numKings >= Math.min(formCap(G.coop), Comms.KING_SLOTS) && !rich()) {
            formSite = null; Comms.write(Comms.FORM_T, 0); return;
        }
        if (formSite != null) {
            // a king formed at the open site closes it (under the cap, a second merge there would follow)
            boolean formed = Comms.numKings > formKings;
            if (formed || G.round - formStart > Cfg.FORM_TTL) {
                formSite = null;
                Comms.write(Comms.FORM_T, 0);
                return;
            }
            Comms.write(Comms.FORM_T, Comms.stamp());
            return;
        }
        int babies = (G.rc.getCurrentRatCost() - 10) / 10 * 4 - Math.max(0, Comms.numKings - 1);
        if (endKing(G.round, Comms.numKings, babies, G.rc.getGlobalCheese(), formStart)) {
            int s = guessSym();
            formSite = ringSite(G.me, G.mirror(start, s));
            if (formSite == null) formSite = pickFormSite(G.me);
            if (formSite == null) return;
            openFormSite();
            return;
        }
        if (G.round < Cfg.FORM_FROM || G.round > FORM_LAST_ROUND || G.round - formStart < Cfg.FORM_TTL + Cfg.FORM_RETRY
                || babies < Cfg.FORM_BABIES || G.rc.getGlobalCheese() < Cfg.FORM_CHEESE) return;
        // Cfg.FORM3_BANK: a third or later king only from a bank that still feeds the respawn after the merge
        if (Comms.numKings >= 2 && G.rc.getGlobalCheese() < Cfg.FORM3_BANK) return;
        if (rich()) {
            formSite = pickFormSite(G.me);
            if (formSite == null) {
                int s = guessSym();
                formSite = ringSite(G.me, G.mirror(start, s));
            }
        }
        else if (formIncome()) formSite = pickFormSite(G.me);
        if (formSite == null) return;
        openFormSite();
    }

    /** Publish formSite as open from this round. */
    private static void openFormSite() {
        formStart = G.round;
        formKings = Comms.numKings;
        Comms.write(Comms.FORM_X, formSite.x + 1);
        Comms.write(Comms.FORM_Y, formSite.y + 1);
        Comms.write(Comms.FORM_T, Comms.stamp());
    }

    /** Kings we form up to: FORM_MAX, or FORM_MAX_WAR once cooperation is over (kings weigh 0.5 of points then, 0.3 before). */
    static int formCap(boolean coop) { return coop ? Cfg.FORM_MAX : Math.max(Cfg.FORM_MAX, Cfg.FORM_MAX_WAR); }

    /** Cfg.FORM_RICH_BANK: the bank pays for one more king before the cutoff (FORM_RICH_MAX kings at most). */
    static boolean rich() {
        return G.round <= FORM_LAST_ROUND && Comms.numKings < Math.min(Cfg.FORM_RICH_MAX, Comms.KING_SLOTS)
            && G.rc.getGlobalCheese() >= Cfg.FORM_RICH_BANK;
    }

    /** Cfg.END_KING: a lone king's endgame site is due (babies from the rat cost, last site's start formStart). */
    static boolean endKing(int round, int kings, int babies, int cheese, int formStart) {
        return round >= Cfg.END_KING && kings < 2 && babies >= 7 && cheese >= Cfg.END_KING_CHEESE
            && round - formStart > Cfg.FORM_TTL;
    }

    /** The income test for one more king (Cfg.FORM_NET100). */
    static boolean formIncome() {
        return Econ.rate100() * Cfg.INCOME_TRUST / 100 - 200 * (Econ.kings() + 1) >= Cfg.FORM_NET100;
    }

    /** The nearest clearly-ours mine more than 4 tiles from us with a clear 3x3, else a clear spot behind us. */
    static MapLocation pickFormSite(MapLocation from) throws GameActionException {
        int s = guessSym();
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

    /** Site centres 3 tiles out from the king: the site's 3x3 touches the king's body (no lane between them; 4 out
     *  would leave one, but its far tiles fail clearNow's view test), every site tile within the king's view; offsets
     *  are in the side frame, so mirrored kings scan alike. */
    static final int[][] RING = {{3, 0}, {0, 3}, {3, 1}, {1, 3}, {3, -1}, {-1, 3}, {0, -3}, {-3, 0}, {1, -3}, {-3, 1}, {-1, -3}, {-3, -1}};

    /** The clear 3x3 off our king's body farthest from the enemy start, or null. */
    static MapLocation ringSite(MapLocation c, MapLocation ek) throws GameActionException {
        int sx = 2 * c.x >= G.W - 1 ? 1 : -1, sy = 2 * c.y >= G.H - 1 ? 1 : -1;
        MapLocation best = null;
        int bd = -1;
        for (int[] o : RING) {
            MapLocation s = c.translate(o[0] * sx, o[1] * sy);
            int d = s.distanceSquaredTo(ek);
            if (d > bd && clearNow(s)) { bd = d; best = s; }
        }
        return best;
    }

    /** The 3x3 around s is on the map, in view, passable, and holds no cat or king. */
    static boolean clearNow(MapLocation s) throws GameActionException {
        if (s.x < 1 || s.y < 1 || s.x > G.W - 2 || s.y > G.H - 2) return false;
        RobotController rc = G.rc;
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation t = s.translate(dx, dy);
                if (!rc.canSenseLocation(t) || !rc.senseMapInfo(t).isPassable()) return false;
                RobotInfo r = rc.senseRobotAtLocation(t);
                if (r != null && r.type != UnitType.BABY_RAT) return false;
            }
        return true;
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

    /** The mine the king sits on (null until picked). */
    static MapLocation homeMine;
    /** Why the king's last move did what it did (indicator km=): see move(), KPath.follow and dodge. */
    static char mineWhy = '-';

    /** The symmetry to mirror our start by to find the enemy's: the first still possible of ROT, HOR, VER. */
    static int guessSym() {
        return (Sym.mask & Sym.ROT) != 0 ? Sym.ROT : (Sym.mask & Sym.HOR) != 0 ? Sym.HOR : Sym.VER;
    }

    /** The known mine nearest our start, within KING_MINE_R2 and on our half: the king sits on it. */
    static MapLocation pickHomeMine(MapLocation from) {
        int s = guessSym();
        MapLocation ek = G.mirror(from, s);
        MapLocation best = null;
        int bd = Cfg.KING_MINE_R2 + 1;
        for (int i = 0; i < Comms.numMines; i++) {
            MapLocation m = Comms.mines[i];
            int d = from.distanceSquaredTo(m);
            int e = m.distanceSquaredTo(ek);
            if (d >= bd || d > e) continue;
            // KING_FRONT: not one near the bisector
            if (kingFront > 0 && 100 * (e - d) < kingFront * from.distanceSquaredTo(ek)) continue;
            // another king already holds it
            boolean held = false;
            for (int k = 0; k < Comms.numKings; k++)
                if (G.cheb(Comms.kings[k], m) <= 2 && !Comms.kings[k].equals(G.me)) held = true;
            if (!held) { bd = d; best = m; }
        }
        return best;
    }
}
