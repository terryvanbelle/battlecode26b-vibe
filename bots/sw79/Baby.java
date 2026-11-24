package sw79;

import battlecode.common.*;

/**
 * Baby rat: gathers and delivers cheese, scouts and explores, fights cats and (at war) enemy rats, lays traps,
 * carries and throws enemies, and joins king formations. One turn is {@link #turn()}; the move is
 * {@link #moveDecision()}.
 */
public final class Baby {
    public static final int GATHER = 0, FIGHT = 1, ATTACK = 2;
    /** This rat's role (-1 until born() runs on its first turn). */
    public static int role = -1;
    static int birthRound;
    /** Cfg.RUSH_LIFE: the round this attacker last saw the enemy king. */
    static int ekSeen = -10000;
    /** Cfg.STALL_CAT: the tile this attacker has stayed near since stallSince, and whether it hunts cats now. */
    static MapLocation stallAt;
    static int stallSince;
    static boolean catHunt;
    static MapLocation home;          // spawning king's location at birth
    static MapLocation origin;        // our first king's start, squeaked by the spawning king
    static int fu = 1, fv = 1;        // side frame (signs of our king's offset from centre)

    static MapLocation cheeseTarget;  // last seen cheese tile
    /** Cfg.BREAKOUT_TTL: breaking out (no chase of walled-off cheese; explore by frontier search) until this round;
     *  wallStrikes counts chases of walled-off cheese given up on a stall, the last in round lastStrike. */
    static int breakoutUntil = -1, wallStrikes, lastStrike = -1000;
    static MapLocation mineTarget;    // sticky assigned mine
    static boolean coopAtPick;        // Cfg.FIGHT_POST: the mine was picked during cooperation
    /** Cfg.FIGHT_POST (a field so tests can set it). */
    static int postMargin = Cfg.FIGHT_POST;
    /** Cfg.LOAD_AT (a field so tests can set it). */
    static int loadAt = Cfg.LOAD_AT;
    static MapLocation exploreTarget;
    static MapLocation catSeen;       // bottom-left of nearest cat sensed this turn
    static RobotInfo cat;
    static int catSeenRound = -100;
    /** CAT_REACH: no cat branch before this round; reachTurns is Cfg.CAT_REACH (a field, so tests can set it). */
    static int catBanUntil = -1, reachTurns = Cfg.CAT_REACH;
    /** AIM_LEFT: aimCarry's bytecode floor (a field, so tests can set it). */
    static int aimLeft = Cfg.AIM_LEFT;

    /** Mines this rat has sensed or heard of (x * 64 + y), to report to the kings. */
    static int[] myMines = new int[24];
    static int myMineCount;
    /** Turns cut short for lack of bytecodes. */
    static int lowBc;

    /** Debug counts (indicator string); picked also marks pickups for idle(). */
    public static int delivered, bitesCat, bitesRat, trapsPlaced, picked;

    /** One turn: refresh team state and senses, act before moving (throw, grab, bite, trap, squeak), promote or move,
     *  then act again from the new tile. Debug.mark(k) numbers the stages for the overrun locator. */
    public static void turn() throws GameActionException {
        RobotController rc = G.rc;
        if (role < 0) born();
        // a carry we hold 12+ rounds after our last grab came from the engine's role swap: its hold starts now
        if (rc.getCarrying() != null && G.round - carryRound > 11) carryRound = G.round;
        Trail.record();
        Comms.readKings();
        Nav.markKings();
        Comms.readMines();
        int s = Comms.read(Comms.SYM);
        if (s != 0) Sym.merge(s);

        Debug.mark(1);
        sense();
        RobotInfo[] robots = rc.senseNearbyRobots(-1, null);
        seen = robots;
        RobotInfo enemyKing = scanRobots(robots);

        Debug.mark(2);
        if (role != ATTACK) pickUp();
        Nav.avoid = cat != null ? laneMask(cat) : 0;
        if ((Nav.swapHold > 0 || Cfg.SWAP_LOADED > 0) && rc.getCarrying() == null) Nav.avoid |= carrierMask(robots);
        // a newborn's first turn pays for array allocation and a full first sense: skip optional work when short
        if (!G.coop && Flow.left() > Cfg.BC_OPTIONAL) TrapRisk.observe(robots);

        Debug.mark(3);
        eKing = enemyKing;
        if (role == ATTACK) {
            if (enemyKing != null) ekSeen = G.round;
            // a rush that has not found the enemy king in RUSH_LIFE rounds has failed for this rat: it gathers
            else if (rushFailed(G.round, birthRound, ekSeen, Cfg.RUSH_LIFE)) role = GATHER;
        }
        nearestEnemy = null;
        hunting = false;
        hunter = null;
        if (!G.coop) findEnemies(robots);
        Debug.mark(30);
        // the birth turn has paid Sym.init and its allocations already: it bails at NB_BAIL
        if (Flow.left() < (G.round == birthRound ? Cfg.NB_BAIL : Cfg.BC_BAIL)) { lowBc++; return; }
        Nav.guard = null;
        if (!G.coop && nearestEnemy != null && rc.getCarrying() == null
                && G.me.distanceSquaredTo(nearestEnemy.location) <= Cfg.GUARD_DIST2) Nav.guard = nearestEnemy.location;
        boolean engaged = nearestEnemy != null && G.me.distanceSquaredTo(nearestEnemy.location) <= 20;
        if (!G.coop && Nav.stall < 8 && !hunting && !engaged) Nav.avoid |= TrapRisk.mask(G.me);
        Nav.avoid |= fightRisk(engaged);
        Debug.mark(31);
        // a carried enemy is thrown as soon as we can act
        if (rc.getCarrying() != null) { if (rc.getCarrying().team == G.us) dropAlly(); else tryThrow(robots); }
        Debug.mark(4);
        boolean hostile = hostile();
        if (!actBeforeMove(robots, enemyKing, hostile)) return;

        Debug.mark(7);
        MapLocation before = rc.getLocation();
        // the rat on a rescue site (CASH) promotes once it can, movement-ready or not (heavy rats rarely are)
        if (cashSite != null && G.me.equals(cashSite) && rc.canBecomeRatKing()) {
            rc.becomeRatKing();
            formed++;
            return;
        }
        // the rat on a formation centre promotes once it can, movement-ready or not
        if (rc.canBecomeRatKing()) {
            MapLocation fs = formSite();
            if (fs != null && G.me.equals(fs)) { rc.becomeRatKing(); formed++; return; }
        }
        if (rc.isMovementReady()) moveDecision();
        boolean moved = !rc.getLocation().equals(before);
        if (rc.getType() == UnitType.RAT_KING) return;   // just promoted: the king code runs from next turn
        // a newborn's first turn (~6.4k on initialisation) that the move left short ends here: pickUp or a throw overran
        if (G.round == birthRound && Clock.getBytecodesLeft() < Cfg.NB_LATE_LEFT) { lowBc++; return; }
        actAfterMove(hostile, moved);
    }

    /** Cfg.FIGHT_RISK's avoid bits: in a fight (engaged: an enemy baby within dist2 20, where the full TrapRisk mask is
     *  dropped), the TrapRisk marks of the last FIGHT_RISK rounds only, i.e. the tiles near the front tiles of the enemy
     *  babies seen lately (0 when off, in cooperation, hunting, after Nav.stall 8, or not engaged). */
    static int fightRisk(boolean engaged) {
        if (Cfg.FIGHT_RISK <= 0 || G.coop || Nav.stall >= 8 || hunting || !engaged) return 0;
        return TrapRisk.mask(G.me, Cfg.FIGHT_RISK);
    }

    /** Sets cat (the nearest cat by distance to its body's centre) and catSeen from robots, lays cat-lane marks during
     *  cooperation; returns the enemy king in view, or null. */
    private static RobotInfo scanRobots(RobotInfo[] robots) {
        RobotInfo enemyKing = null;
        cat = null;
        int bestCat = Integer.MAX_VALUE;
        for (RobotInfo r : robots) {
            if (r.team == Team.NEUTRAL) {
                if (G.coop) CatLane.see(r.location, G.round);
                int d = catDist4(r.location, G.me);
                if (d < bestCat) { bestCat = d; cat = r; }
            } else if (r.team == G.them) {
                if (r.type == UnitType.RAT_KING) enemyKing = r;
            }
        }
        if (cat != null) { catSeen = cat.location; catSeenRound = G.round; }
        return enemyKing;
    }

    /** At war: nearestEnemy (the nearest enemy baby not behind a wall, or a prey we can grab), hunting, nearK and
     *  rescueT for this turn. */
    private static void findEnemies(RobotInfo[] robots) {
        RobotController rc = G.rc;
        int bd = Integer.MAX_VALUE;
        for (RobotInfo r : robots) {
            if (r.team != G.them || r.type == UnitType.RAT_KING) continue;
            int d = G.me.distanceSquaredTo(r.location);
            if (d < bd && !(d > 2 && walled(G.me, r.location))) { bd = d; nearestEnemy = r; }
        }
        // a rat we can grab (lower health than ours: trapped or bitten) comes before anything else
        if (rc.getCarrying() == null) {
            RobotInfo p = prey(robots, rc.getHealth());
            // gatherers only grab prey that is close; fighters hunt it across their vision
            if (p != null && (role != GATHER || G.me.distanceSquaredTo(p.location) <= 8)) {
                nearestEnemy = p;
                hunting = true;
            }
        }
        // Cfg.PREY_TRAP (yl34): a healthier enemy coming for us is the rat trap's first target (after prey(), so
        // its wall traces never use up prey()'s share of WALL_BLIND_CAP)
        if (Cfg.PREY_TRAP) hunter = hunterOf(robots, G.me, rc.getHealth());
        rescueT = null;
        nearK = false;
        if (rc.getCarrying() == null) {
            MapLocation kk = Comms.nearestKing(G.me);
            nearK = kk != null && G.cheb(G.me, kk) <= Cfg.RESCUE_KING_R;
            if (nearK) rescueT = rescueTarget(robots);
        }
        nearKR = G.round;
    }

    /** The actions before the move: grabs, the grab micro, traps, bites (enemy rats once cooperation is over or for
     *  attackers, then cats), a cat trap ahead of a cat, and the turn's squeak. False when the turn ends here (the
     *  micro left too few bytecodes). */
    private static boolean actBeforeMove(RobotInfo[] robots, RobotInfo enemyKing, boolean hostile) throws GameActionException {
        RobotController rc = G.rc;
        if (hostile) tryCarry(robots);
        // a rescue one step away keeps the action for the grab after the move
        boolean saving = rescueT != null && rc.isMovementReady() && rc.isActionReady();
        if (hostile && !saving) {
            if (GrabMicro.act(robots)) saving = true;
            if (Clock.getBytecodesLeft() < (GrabMicro.evalRound == G.round && GrabMicro.round != G.round ? Cfg.GM_BAIL : Cfg.BC_BAIL)) { lowBc++; return false; }
        }
        if (!G.coop && trapOk && !saving) tryRatTrap();
        Debug.mark(5);
        if (hostile && !saving) biteEnemies(robots);
        // attackers open the war themselves, but only on the enemy king
        else if (role == ATTACK && enemyKing != null && !saving) biteEnemies(new RobotInfo[]{enemyKing});
        if (cat != null && !saving) tryBiteCat(cat);
        if (cat != null && !saving) tryCatTrap(cat);

        Debug.mark(6);
        if (!cashTick()) report();
        return true;
    }

    /** The actions after the move, from the new tile: pick up, drop or throw a carried rat, grab and bite again, cat
     *  bites and lane traps, deliver, face the nearest threat, and re-sense. */
    private static void actAfterMove(boolean hostile, boolean moved) throws GameActionException {
        RobotController rc = G.rc;
        Debug.mark(20);
        pickUp();
        if (rc.getCarrying() != null && moved) {
            if (rc.getCarrying().team == G.us) dropAlly();
            else if (Clock.getBytecodesLeft() > Cfg.THROW_BC) tryThrow(rc.senseNearbyRobots(-1, null));
        }
        Debug.mark(21);
        if (hostile) tryCarry(rc.senseNearbyRobots(2, G.them));
        Debug.mark(22);
        // the grab micro took this turn's action already
        if (GrabMicro.round != G.round) {
            if (hostile) biteEnemies(rc.senseNearbyRobots(2, G.them));
            else if (role == ATTACK) {
                RobotInfo[] near = rc.senseNearbyRobots(8, G.them);
                for (RobotInfo r : near) if (r.type == UnitType.RAT_KING) biteEnemies(new RobotInfo[]{r});
            }
        }
        if (cat != null) tryBiteCat(cat);
        if (G.coop) layLane();
        Debug.mark(8);
        tryDeliver();
        guardTurn();
        Debug.mark(9);
        sense2();
    }

    /** Whether this rat bites and grabs enemy rats now: after cooperation, or an attacker at any time. */
    static boolean hostile() {
        return !G.coop || role == ATTACK || G.round >= Cfg.BREAK_ROUND;
    }

    /** End the turn facing the nearest enemy rat close enough to grab us next round. */
    static void guardTurn() throws GameActionException {
        RobotController rc = G.rc;
        if (G.coop || !rc.isTurningReady() || rc.getCarrying() != null) return;
        RobotInfo best = null;
        int bd = Integer.MAX_VALUE;
        for (RobotInfo r : rc.senseNearbyRobots(Cfg.GUARD_DIST2, G.them)) {
            if (r.team != G.them || r.type != UnitType.BABY_RAT) continue;
            int d = G.me.distanceSquaredTo(r.location);
            if (d < bd) { bd = d; best = r; }
        }
        MapLocation t = best != null ? best.location : null;
        if (t == null) {
            // nothing in view: an enemy we saw at the start of the turn may be behind us
            for (RobotInfo r : seen) {
                if (r.team != G.them || r.type != UnitType.BABY_RAT) continue;
                int d = G.me.distanceSquaredTo(r.location);
                if (d <= Cfg.GUARD_DIST2 && d < bd) { bd = d; t = r.location; }
            }
        }
        if (t == null) t = Nav.guard;
        if (t == null) return;
        Direction to = G.me.directionTo(t);
        if (to != Direction.CENTER && rc.getDirection() != to && rc.canTurn(to)) { rc.turn(to); G.facing = to; }
    }

    /** Cfg.STALL_CAT: once this attacker has stayed within Chebyshev 3 of one tile for STALL_CAT rounds it hunts cats:
     *  it walks to a cat in view, else the cat last known, else the centre (centreTile). False while it has not. */
    static boolean stallCat() throws GameActionException {
        if (!catHunt) {
            if (stallAt == null || G.cheb(G.me, stallAt) > 3) { stallAt = G.me; stallSince = G.round; return false; }
            if (G.round - stallSince < Cfg.STALL_CAT) return false;
            catHunt = true;
        }
        if (cat != null && approachCat(cat)) return true;
        MapLocation ct = knownCat();
        if (ct == null) ct = centreTile(home, G.cx2, G.cy2);
        Nav.goTo(ct);
        return true;
    }

    /** The map's centre tile on the side of each centre line that holds h (doubled centre cx2, cy2): mirror images of h
     *  get mirror-image tiles under every symmetry. */
    static MapLocation centreTile(MapLocation h, int cx2, int cy2) {
        return new MapLocation((cx2 + (2 * h.x < cx2 ? 0 : 1)) / 2, (cy2 + (2 * h.y < cy2 ? 0 : 1)) / 2);
    }

    /** Cfg.RUSH_LIFE: an attacker born at birth that last saw the enemy king at seen has rushed for life rounds in vain. */
    static boolean rushFailed(int round, int birth, int seen, int life) {
        return round - birth > life && round - seen > life;
    }

    /** This rat was born a rusher and walks by BitFlow (Nav.goTo); its sense loop keeps the rows. */
    public static boolean bfOn;

    /** First-turn setup: role, home king, side frame, seed, rat-trap permission and the scout assignment. */
    static void born() {
        RobotController rc = G.rc;
        birthRound = G.round;
        int cost = rc.getCurrentRatCost();
        // approximate spawn index from the cost step. Our first turn is the round after our spawn (spawn order), so the
        // cost may include a later spawn and the round's parity is not the king's; the opening was tuned on this
        int idx = (cost - 10) / 10 * 4;
        role = GATHER;
        if (idx >= Cfg.GATHER_FIRST && (G.round % Cfg.FIGHT_EVERY) == 0) role = FIGHT;
        if (rusher(idx, G.round)) role = ATTACK;
        Comms.readKings();
        home = Comms.nearestKing(G.me);
        if (home == null) home = G.me;
        if (role == ATTACK && rc.getMapWidth() * rc.getMapHeight() >= Cfg.RUSH_BFLOW_AREA) { bfOn = true; BitFlow.init(); }
        trapOk = trapsAllowed(home, Comms.read(Comms.SYM), Cfg.TRAP_DIST);
        origin = heardStart(rc.readSqueaks(-1));
        int px = 2 * home.x - G.cx2, py = 2 * home.y - G.cy2;
        fu = px >= 0 ? 1 : -1;
        fv = py >= 0 ? 1 : -1;
        // mirror-invariant seed: round and the unsigned offset from the king
        int seed = birthSeed(G.round, G.me, home);
        G.seed(seed);
        slot = (seed * 0x9E3779B1) >>> 8;
        scout = (slot >>> 4) % Cfg.SCOUT_EVERY == 0;
        // SCOUT_FAN: with our place in the king's gatherer order heard (rrSlot), gatherers 2 and 3 scout the two
        // flanks, then every SCOUT_EVERY-th from 8 (the middle, then the flanks again), so two scouts never share a
        // flank. Unheard: no scout (slot-hashed scouts added too many on maps with few gatherers).
        if (role == GATHER) {
            int k = rrSlot < 2 ? -1 : rrSlot <= 3 ? rrSlot - 2 : rrSlot >= 8 && rrSlot % Cfg.SCOUT_EVERY == 0 ? 2 + (rrSlot - 8) / Cfg.SCOUT_EVERY : -1;
            scout = k >= 0;
            fanBase = k;
        }
    }

    /** The role born() gives a rat with idx live babies in round r. */
    static int roleFor(int idx, int r) {
        if (rusher(idx, r)) return ATTACK;
        return idx >= Cfg.GATHER_FIRST && (r % Cfg.FIGHT_EVERY) == 0 ? FIGHT : GATHER;
    }

    /** Rat traps allowed for this baby (Cfg.TRAP_DIST), fixed at birth. */
    static boolean trapOk = true;

    /** Cfg.TRAP_DIST's test: true when d is 0 or no live symmetry (the kings' slot) mirrors home within Chebyshev d. */
    static boolean trapsAllowed(MapLocation home, int slot, int d) { return d == 0 || !rushNear(home, liveSyms(slot), d); }

    /** The symmetries the kings' SYM slot still allows (our own mask when the slot is empty). */
    static int liveSyms(int slot) {
        int m = slot & Sym.mask;
        return m == 0 ? Sym.mask : m;
    }

    /** Some symmetry in mask puts the enemy start (home mirrored) within Chebyshev d of home. */
    static boolean rushNear(MapLocation home, int mask, int d) {
        for (int s = Sym.ROT; s <= Sym.VER; s <<= 1)
            if ((mask & s) != 0 && G.cheb(home, G.mirror(home, s)) <= d) return true;
        return false;
    }

    /** A baby born with idx live babies in round r rushes the enemy king. */
    static boolean rusher(int idx, int r) {
        return idx >= Cfg.RUSH_FROM && idx < Cfg.RUSH_TO && r < Cfg.RUSH_UNTIL;
    }

    /** A rat's birth seed: its round and unsigned offset from the king that spawned it, the same for its mirror twin. */
    static int birthSeed(int round, MapLocation me, MapLocation home) {
        return round * 977 + Math.abs(me.x - home.x) * 31 + Math.abs(me.y - home.y) * 7 + 1;
    }

    /** 4 x squared distance from a tile to a cat's 2x2 centre. */
    static int catDist4(MapLocation catBL, MapLocation p) {
        int dx = 2 * p.x - (2 * catBL.x + 1), dy = 2 * p.y - (2 * catBL.y + 1);
        return dx * dx + dy * dy;
    }

    /** p is on the 2x2 cat body whose lower-left tile is catBL. */
    static boolean inCatBody(MapLocation catBL, MapLocation p) {
        return p.x >= catBL.x && p.x <= catBL.x + 1 && p.y >= catBL.y && p.y <= catBL.y + 1;
    }

    /** The directions whose tile is next to an allied baby carrying an ally (its drop tile). */
    static int carrierMask(RobotInfo[] robots) {
        int mask = 0;
        for (RobotInfo r : robots) {
            if (r.team != G.us || r.carryingRobot == null || r.carryingRobot.team != G.us) continue;
            for (int i = 0; i < 8; i++) if (G.cheb(G.me.add(G.DIRS[i]), r.location) <= 1) mask |= 1 << i;
        }
        return mask;
    }

    /** Avoid-mask bits for our neighbours on cat c's body or its next two steps. */
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

    /** p is on cat c's next two steps (its body moved one and two tiles along its facing). */
    static boolean inLane(RobotInfo c, MapLocation p) {
        MapLocation n1 = c.location.add(c.direction), n2 = n1.add(c.direction);
        return inCatBody(n1, p) || inCatBody(n2, p);
    }

    /** The piles seen this sense, judged once every wall in view is recorded. */
    static final MapLocation[] piles = new MapLocation[24];

    /** Record every tile in view (map memory, symmetry, BitFlow rows, mines) and retarget the nearest cheese pile
     *  that no known wall hides. */
    static void sense() throws GameActionException {
        MapInfo[] infos = G.rc.senseNearbyMapInfos();
        MapLocation bestCheese = null;
        int bd = Integer.MAX_VALUE, np = 0;
        for (MapInfo mi : infos) {
            MapLocation l = mi.getMapLocation();
            boolean mine = mi.hasCheeseMine(), wall = mi.isWall(), dirt = mi.isDirt();
            Sym.record(l.x, l.y, wall, dirt, mine);
            // a rusher (bfOn): BitFlow's rows take every wall and dirt tile in view (Sym.record clears a dug one)
            if (bfOn && (wall || dirt)) BitFlow.mark(l.x, l.y, (wall ? Sym.WALL : 0) | (dirt ? Sym.DIRT : 0));
            if (mine) noteMine(l.x, l.y);
            if (mi.getCheeseAmount() > 0) {
                if (np < piles.length) piles[np++] = l;
            } else if (cheeseTarget != null && cheeseTarget.equals(l)) {
                cheeseTarget = null;
            }
        }
        for (int i = 0; i < np; i++) {
            MapLocation l = piles[i];
            int d = G.me.distanceSquaredTo(l);
            if (d < bd && !walledOff(G.me, l)) { bd = d; bestCheese = l; }
        }
        if (cheeseTarget != null && walledOff(G.me, cheeseTarget)) cheeseTarget = null;
        if (bestCheese != null) {
            if ((cheeseTarget == null || G.me.distanceSquaredTo(cheeseTarget) > bd) && !keepTarget(bestCheese)) cheeseTarget = bestCheese;
        }
    }

    /** The current cheese target has a flow path searching or ready and the nearer pile n lies behind
     *  a wall, so the target stays (a switch would drop the path). */
    static boolean keepTarget(MapLocation n) {
        return cheeseTarget != null && (Flow.ready(cheeseTarget) || Flow.searching(cheeseTarget))
                && walledOff(G.me, n);
    }

    /** The greedy line from a to b (directionTo steps, as a straight walk would take) crosses a known wall. */
    static boolean walledOff(MapLocation a, MapLocation b) {
        int[] mem = Sym.mem;
        MapLocation c = a;
        while (true) {
            c = c.add(c.directionTo(b));
            if (c.equals(b)) return false;
            if ((mem[G.idx(c)] & Sym.WALL) != 0) return true;
        }
    }

    /** Re-sense after moving (new cone) only to refresh cheese; terrain is caught next turn. */
    static void sense2() throws GameActionException {
        if (Clock.getBytecodesLeft() < Cfg.SENSE2_LEFT) return;
        sense();
    }

    /** A mine we know that the king's list lacks (rotating through them by round), else -1. */
    static int unreported() {
        // a full list takes no more mines, so none is news
        if (Comms.numMines >= Comms.MINE_SLOTS) return -1;
        for (int j = 0; j < myMineCount; j++) {
            int key = myMines[(G.round + j) % myMineCount];
            if (!Comms.hasMine(key / 64, key % 64)) return key;
        }
        return -1;
    }

    /** Add mine (x, y) to myMines if new and there is room. */
    static void noteMine(int x, int y) {
        int key = x * 64 + y;
        for (int i = 0; i < myMineCount; i++) if (myMines[i] == key) return;
        if (myMineCount < myMines.length) myMines[myMineCount++] = key;
    }

    /** Pick up cheese on our tile and the three tiles in front of us. */
    static void pickUp() throws GameActionException {
        RobotController rc = G.rc;
        MapLocation c = rc.getLocation();
        take(c);
        for (Direction d : G.fan(c, rc.getDirection())) take(c.add(d));
        G.rawCheese = rc.getRawCheese();
    }

    /** Pick up the pile at l if canTake allows it; a non-attacker takes only what keeps it at or under CARRY_CAP. */
    static void take(MapLocation l) throws GameActionException {
        RobotController rc = G.rc;
        if (!canTake(l)) return;
        if (role != ATTACK) {
            int room = Cfg.CARRY_CAP - rc.getRawCheese();
            if (room <= 0) return;
            if (rc.senseMapInfo(l).getCheeseAmount() > room) { rc.pickUpCheese(l, room); picked++; return; }
        }
        rc.pickUpCheese(l);
        picked++;
    }

    /** A pile we may take: any, except that an attacker carries at most ATK_CARRY. */
    static boolean canTake(MapLocation l) throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.canPickUpCheese(l)) return false;
        if (role == ATTACK) return atkTake(rc.getRawCheese(), rc.senseMapInfo(l).getCheeseAmount(), Cfg.ATK_CARRY);
        return true;
    }

    /** Cfg.ATK_CARRY: an attacker's take, capped even when empty-handed. */
    static boolean atkTake(int raw, int pile, int cap) { return raw + pile <= cap; }

    // ---------------------------------------------------------------- grabs and throws

    /** This turn's enemy king in view and nearest enemy rat (see findEnemies), and the robots sensed at its start. */
    static RobotInfo eKing, nearestEnemy;
    static RobotInfo[] seen = new RobotInfo[0];
    /** Debug counts: grabs, throws and rat traps laid. */
    public static int naps, throwsDone, ratTraps;
    /** The round of our last grab (a carry lasts GameConstants.MAX_CARRY_DURATION). */
    static int carryRound = -100;

    /** Grab an adjacent enemy baby rat (the engine allows it when it cannot see us or is weaker): the healthiest,
     *  or first one carrying one of ours (it drops it). */
    static boolean tryCarry(RobotInfo[] robots) throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.isActionReady() || rc.getCarrying() != null) return false;
        RobotInfo best = null;
        int bs = Integer.MIN_VALUE;
        for (RobotInfo r : robots) {
            if (r.team != G.them || r.type != UnitType.BABY_RAT) continue;
            if (G.me.distanceSquaredTo(r.location) > 2) continue;
            if (!rc.canCarryRat(r.location)) continue;
            int s = r.health;
            if (nearK && nearKR == G.round && holdsOurs(r)) s += 1000;
            if (s > bs) { bs = s; best = r; }
        }
        if (best == null) return false;
        rc.carryRat(best.location);
        carryRound = G.round;
        naps++;
        return true;
    }

    /** The robots in view by offset from us, 17x17 (index (ox + 8) + 17 * (oy + 8)); an entry is current while its
     *  gridAt equals the stamp of the last gridFill. */
    static final RobotInfo[] grid = new RobotInfo[289];
    static final int[] gridAt = new int[289];
    /** Damage when the flight ends at tile k (2 = point-blank 42, then 38, 34, ...). */
    static final int[] FLIGHT_DMG = new int[9];
    // a thrown rat younger than its thrower loses a tick: 8 less at tiles 3-6, and it lands (10) after 6
    static { for (int k = 2; k <= 8; k++) FLIGHT_DMG[k] = flightDmg(k); }

    /** The conservative damage when the flight ends at tile k. */
    static int flightDmg(int k) {
        int d = 10 + 4 * (2 * (4 - (k - 1) / 2) + ((k & 1) == 1 ? 1 : 0));
        return k <= 2 ? d : k <= 6 ? d - 8 : 10;
    }
    static int gridStamp;

    /** Put r at offset (ox, oy) of the grid, if within 8. */
    static void gridPut(int ox, int oy, RobotInfo r, int stamp) {
        if (ox < -8 || ox > 8 || oy < -8 || oy > 8) return;
        int i = (ox + 8) + 17 * (oy + 8);
        grid[i] = r;
        gridAt[i] = stamp;
    }

    /** The robots we see by offset from me (kings cover their 3x3, cats their 2x2), so flight scans never sense tile
     *  by tile; returns the stamp that marks this fill. */
    static int gridFill(MapLocation me, RobotInfo[] robots) {
        int stamp = ++gridStamp;
        for (RobotInfo r : robots) {
            int ox = r.location.x - me.x, oy = r.location.y - me.y;
            if (r.type == UnitType.RAT_KING) {
                for (int ex = -1; ex <= 1; ex++) for (int ey = -1; ey <= 1; ey++) gridPut(ox + ex, oy + ey, r, stamp);
            } else if (r.team == Team.NEUTRAL) {
                for (int ex = 0; ex <= 1; ex++) for (int ey = 0; ey <= 1; ey++) gridPut(ox + ex, oy + ey, r, stamp);
            } else gridPut(ox, oy, r, stamp);
        }
        return stamp;
    }

    /** D3[(dx + 1) + 3 * (dy + 1)]: the direction of a unit offset (CENTER in the middle). */
    static final Direction[] D3 = {Direction.SOUTHWEST, Direction.SOUTH, Direction.SOUTHEAST, Direction.WEST,
                                   Direction.CENTER, Direction.EAST, Direction.NORTHWEST, Direction.NORTH, Direction.NORTHEAST};

    /** Carrying an enemy: from our tile or a neighbour we can step to, a throw whose point-blank tile
     *  (two out, the first one empty) holds an enemy robot: a baby 84, a king's body 126 (tryThrow's scale). Turn to
     *  it, step there (a strafe when it is not ahead) and throw now if the action is ready, else hold the aim for
     *  next turn. Only enemy tiles within Chebyshev 3 can be such targets, so the scan starts from them (a scan of
     *  every step and throw cost ~7000 bytecodes and overran). Ties: stay, then G.hand of the step, of the throw, and
     *  G.pickBit of the target tile. */
    static boolean aimCarry() throws GameActionException {
        RobotController rc = G.rc;
        if (seen == null || Flow.left() < Cfg.THROW_BC) return false;
        MapLocation me = rc.getLocation();
        int[] tx = AIM_TX, ty = AIM_TY, tv = AIM_TV;
        int n = 0;
        for (RobotInfo r : seen) {
            int ox = r.location.x - me.x, oy = r.location.y - me.y;
            if (r.team != G.them) {
                // a cat's 2x2 body point-blank feeds the carried enemy to it (CAT_FEED)
                if (r.type == UnitType.CAT && ox <= 3 && ox >= -4 && oy <= 3 && oy >= -4)
                    for (int ex = 0; ex <= 1; ex++)
                        for (int ey = 0; ey <= 1; ey++) {
                            int x = ox + ex, y = oy + ey;
                            if (x > 3 || x < -3 || y > 3 || y < -3 || n >= 24) continue;
                            tx[n] = x; ty[n] = y; tv[n++] = Cfg.CAT_FEED;
                        }
                continue;
            }
            if (r.type == UnitType.RAT_KING) {
                if (ox > 4 || ox < -4 || oy > 4 || oy < -4) continue;
                for (int ex = -1; ex <= 1; ex++)
                    for (int ey = -1; ey <= 1; ey++) {
                        int x = ox + ex, y = oy + ey;
                        if (x > 3 || x < -3 || y > 3 || y < -3 || n >= 24) continue;
                        tx[n] = x; ty[n] = y; tv[n++] = 126;
                    }
            } else if (ox <= 3 && ox >= -3 && oy <= 3 && oy >= -3 && n < 24) { tx[n] = ox; ty[n] = oy; tv[n++] = 84; }
        }
        if (n == 0) return false;
        int stamp = gridFill(me, seen);
        int[] mem = Sym.mem;
        int w = G.W, h = G.H, blk = Sym.WALL | Sym.DIRT;
        boolean canMove = rc.isMovementReady();
        int best = Integer.MIN_VALUE;
        Direction bm = null, bt = null;
        // steps into the cat's lane (its body and next two tiles) only to feed it with the throw ready
        aimLane = cat != null ? laneMask(cat) : 0;
        aimReady = rc.isActionReady();
        for (int j = 0; j < 9; j++) {
            if (aimLeft > 0 && Flow.left() <= aimLeft) break;
            int mx = j % 3 - 1, my = j / 3 - 1;
            boolean stay = mx == 0 && my == 0;
            if (!stay && !canMove) continue;
            Direction m = D3[j];
            boolean checked = stay;
            for (int i = 0; i < n; i++) {
                int vx = tx[i] - mx, vy = ty[i] - my;
                if (vx != 0 && vx != 2 && vx != -2 || vy != 0 && vy != 2 && vy != -2 || vx == 0 && vy == 0) continue;
                int fx = mx + vx / 2, fy = my + vy / 2, x = me.x + fx, y = me.y + fy;
                // the first flight tile: on the map, open terrain, no robot (the tile we leave is free)
                if (x < 0 || y < 0 || x >= w || y >= h || (mem[x + w * y] & blk) != 0
                        || (fx != 0 || fy != 0) && gridAt[(fx + 8) + 17 * (fy + 8)] == stamp) continue;
                if (!stay && (aimLane & (1 << m.ordinal())) != 0 && !(aimReady && tv[i] == Cfg.CAT_FEED)) continue;
                if (!checked) { if (!rc.canMove(m)) break; checked = true; }
                Direction d = D3[(vx / 2 + 1) + 3 * (vy / 2 + 1)];
                int key = tv[i] * 16 + (stay ? 8 : G.hand(me, m) > 0 ? 4 : 0) + (G.hand(me.add(m), d) > 0 ? 2 : 0)
                        + (G.pickBit(me, me.translate(tx[i], ty[i])) ? 1 : 0);
                if (key > best) { best = key; bm = m; bt = d; }
            }
        }
        if (bt == null) return false;
        if (rc.getDirection() != bt) { if (!rc.canTurn(bt)) return false; rc.turn(bt); G.facing = bt; }
        if (bm != Direction.CENTER) { rc.move(bm); G.me = rc.getLocation(); }
        if (rc.canThrowRat()) { rc.throwRat(); throwsDone++; }
        return true;
    }

    /** aimCarry's targets: offsets from us and value (tryThrow's scale) of up to 24 tiles. */
    static final int[] AIM_TX = new int[24], AIM_TY = new int[24], AIM_TV = new int[24];
    /** This aim's cat-lane step mask and whether the throw can go this turn. */
    static int aimLane;
    static boolean aimReady;

    /** A flight whose first obstacle is a cat at tile k: Cfg.CAT_FEED at tiles 1-2 (the throw's own turn), 60% at
     *  3-4 and 30% beyond (the cat may have moved). */
    static int feedScore(int k) {
        return k <= 2 ? Cfg.CAT_FEED : k <= 4 ? Cfg.CAT_FEED * 6 / 10 : Cfg.CAT_FEED * 3 / 10;
    }

    /** Cfg.CARRY_KING: the nearest enemy king in robots within CARRY_KING (Chebyshev) while the carry has
     *  CARRY_KING_LEFT rounds or more to run (it lasts MAX_CARRY_DURATION), else null. */
    static RobotInfo carryKing(RobotInfo[] robots) {
        if (robots == null || G.round - carryRound > GameConstants.MAX_CARRY_DURATION - Cfg.CARRY_KING_LEFT) return null;
        MapLocation me = G.rc.getLocation();
        RobotInfo best = null;
        int bd = Cfg.CARRY_KING + 1;
        for (RobotInfo r : robots) {
            if (r.team != G.them || r.type != UnitType.RAT_KING) continue;
            int d = G.cheb(me, r.location);
            if (d < bd) { bd = d; best = r; }
        }
        return best;
    }

    /** Cfg.CARRY_CAT: the nearest cat in robots within CARRY_CAT (Chebyshev, from its 2x2 body's low corner) while
     *  the carry has CARRY_KING_LEFT rounds or more to run, else null. */
    static RobotInfo carryCat(RobotInfo[] robots) {
        if (robots == null || G.round - carryRound > GameConstants.MAX_CARRY_DURATION - Cfg.CARRY_KING_LEFT) return null;
        MapLocation me = G.rc.getLocation();
        RobotInfo best = null;
        int bd = Cfg.CARRY_CAT + 1;
        for (RobotInfo r : robots) {
            if (r.type != UnitType.CAT) continue;
            int d = G.cheb(me, r.location);
            if (d < bd) { bd = d; best = r; }
        }
        return best;
    }

    /** Set down the ally carried past (Nav.swapPast) once we can act: on the tile it was taken from when that is free
     *  and beside us, else on the free tile farthest from our errand. */
    static void dropAlly() throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.isActionReady()) return;
        MapLocation me = rc.getLocation(), t = Nav.target, f = Nav.swapFrom;
        Direction best = null;
        int bs = -1;
        for (Direction d : G.DIRS) {
            if (!rc.canDropRat(d)) continue;
            MapLocation l = me.add(d);
            int s = (l.equals(f) ? 1 << 24 : 0) + 2 * (t != null ? l.distanceSquaredTo(t) : 0) + (G.hand(me, d) > 0 ? 1 : 0);
            if (s > bs) { bs = s; best = d; }
        }
        if (best != null) { rc.dropRat(best); Nav.swapFrom = null; }
    }

    /**
     * Throw the carried rat along the direction that hurts most: the flight (6-8 tiles) hits the first robot,
     * wall or dirt for 46/42 (first two tiles), 38/34, 30/26, 22/18; the victim takes the same damage as the robot hit.
     * Hitting our own rat costs its damage; an empty flight lands for 10. Holds the throw while a better one may come
     * (no threat to us and the carry has time left). True when it threw.
     */
    static boolean tryThrow(RobotInfo[] robots) throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.isActionReady()) return false;
        MapLocation me = rc.getLocation();
        if (robots == null) robots = rc.senseNearbyRobots(-1, null);
        int stamp = gridFill(me, robots);
        Direction bestD = null;
        int bs = Integer.MIN_VALUE;
        int[] mem = Sym.mem;
        int w = G.W, h = G.H, blk = Sym.WALL | Sym.DIRT;
        for (int i = 0; i < 8; i++) {
            Direction d = G.DIRS[i];
            int ddx = d.dx, ddy = d.dy;
            int x = me.x + ddx, y = me.y + ddy;
            if (x < 0 || y < 0 || x >= w || y >= h || (mem[x + w * y] & blk) != 0 || gridAt[(ddx + 8) + 17 * (ddy + 8)] == stamp) continue;
            int s2 = 10;
            for (int k = 2; k <= 8; k++) {
                x += ddx; y += ddy;
                int dmg = FLIGHT_DMG[k];
                if (x < 0 || y < 0 || x >= w || y >= h) { s2 = 10; break; }
                if ((mem[x + w * y] & blk) != 0) { s2 = dmg; break; }
                int gi = (k * ddx + 8) + 17 * (k * ddy + 8);
                if (gridAt[gi] == stamp) {
                    RobotInfo r = grid[gi];
                    // beyond tile 2 the hit lands next round: a baby has usually moved by then (kings are slow)
                    boolean far = k > 2 && r.type == UnitType.BABY_RAT;
                    if (r.team == G.them) s2 = far ? dmg * 2 * Cfg.THROW_FAR_PCT / 100 : dmg * (r.type == UnitType.RAT_KING ? 3 : 2);
                    else if (r.team == G.us) s2 = -dmg;
                    else s2 = feedScore(k); // a cat eats the rat (it dies whatever its health) and sleeps
                    break;
                }
            }
            s2 = s2 * 2 + (G.hand(me, d) > 0 ? 1 : 0);
            if (s2 > bs) { bs = s2; bestD = d; }
        }
        if (bestD == null) return false;
        // Cfg.THREAT_THROW: an enemy that could grab us next turn ends the hold (a grabbed carrier drops its rat)
        boolean threat = bs >= 0 && carryThreat(robots, me);
        // no point-blank hit (42) yet: keep holding (the carried rat is out of the fight) while the carry lasts
        if (!threat && bs / 2 < Cfg.THROW_MIN && G.round - carryRound < Cfg.THROW_HOLD) return false;
        // CARRY_KING: on the way to an enemy king, only a throw that hits a king goes before the carry runs out
        if (!threat && bs / 2 < 3 * FLIGHT_DMG[6] && carryKing(robots) != null) return false;
        // CARRY_CAT: on the way to a cat, only a feed (or a better throw) goes before the carry runs out
        if (!threat && bs / 2 < Cfg.CAT_FEED && carryCat(robots) != null) return false;
        if (rc.getDirection() != bestD) { if (!rc.canTurn(bestD)) return false; rc.turn(bestD); G.facing = bestD; }
        if (!rc.canThrowRat()) return false;
        rc.throwRat();
        throwsDone++;
        return true;
    }

    /** Cfg.THREAT_THROW: an enemy baby with free hands within Chebyshev THREAT_THROW of us (seen): it can step next
     *  to us and grab, unseen or as the healthier one. */
    static boolean carryThreat(RobotInfo[] robots, MapLocation me) {
        int c = Cfg.THREAT_THROW;
        for (RobotInfo r : robots) {
            if (r.team != G.them || r.type != UnitType.BABY_RAT || r.carryingRobot != null) continue;
            int dx = r.location.x - me.x, dy = r.location.y - me.y;
            if (dx >= -c && dx <= c && dy >= -c && dy <= c) return true;
        }
        return false;
    }

    /** Rat trap on the front tile nearest an approaching enemy rat (50 damage + stun when it enters the 3x3).
     *  Cfg.PREY_TRAP (yl34): first one in front of this turn's hunter, with no reserve or austerity gate. */
    static boolean tryRatTrap() throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.isActionReady() || rc.getNumberRatTraps() >= Cfg.TRAP_CAP) return false;
        if (rc.getAllCheese() < Econ.floor(Cfg.RAT_TRAP_MIN_CHEESE)) return false;
        if (hunter != null && ratTrapAt(hunter)) return true;
        if (nearestEnemy == null) return false;
        if ((rc.getAllCheese() < King.reserve(Cfg.KING_RESERVE) + 20 || King.austere()) && !nearKing(nearestEnemy.location, Cfg.DEF_TRAP_DIST2)
                && !(Cfg.FWD_TRAP && !homeHalf(G.me))) return false;
        return ratTrapAt(nearestEnemy);
    }

    /** A rat trap on our front tile next to enemy e (within BABY_TRAP_DIST2 of us); false when there is none. */
    static boolean ratTrapAt(RobotInfo e) throws GameActionException {
        RobotController rc = G.rc;
        int d0 = G.me.distanceSquaredTo(e.location);
        if (d0 > Cfg.BABY_TRAP_DIST2) return false;
        MapLocation best = trapTile(rc.getLocation(), rc.getDirection(), e.location, d0, true);
        if (best == null) return false;
        rc.placeRatTrap(best);
        ratTraps++;
        return true;
    }

    /** Cfg.PREY_TRAP (yl34): this turn's hunter (hunterOf), or null. */
    static RobotInfo hunter;

    /** The nearest enemy baby with free hands and more health than hp that sees me from dist2 3-8, no wall between
     *  (it can step next to us and grab us: a grab needs the target in sight and, if it sees the grabber, with less
     *  health), or null. Adjacent ones are out: they grab without a step, so no trap can catch them. */
    static RobotInfo hunterOf(RobotInfo[] robots, MapLocation me, int hp) {
        RobotInfo h = null;
        int hd = Integer.MAX_VALUE;
        for (RobotInfo r : robots) {
            if (r.team != G.them || r.type != UnitType.BABY_RAT || r.health <= hp || r.carryingRobot != null) continue;
            int d = me.distanceSquaredTo(r.location);
            if (d > 2 && d <= 8 && d < hd && sees(r, me) && !walled(me, r.location)) { hd = d; h = r; }
        }
        return h;
    }

    /** e is within d2 of our nearest king (Cfg.DEF_TRAP_DIST2; 0 = never). */
    static boolean nearKing(MapLocation e, int d2) {
        MapLocation k = d2 > 0 ? Comms.nearestKing(e) : null;
        return k != null && k.distanceSquaredTo(e) <= d2;
    }

    /** The front tile for a rat trap against an enemy at e (d0 = our dist2 to it): the one nearest e if nearer than we
     *  are, or with adj one within e's 3x3, nearest first (ties by pick bit); null if none. */
    static MapLocation trapTile(MapLocation me, Direction f, MapLocation e, int d0, boolean adj) throws GameActionException {
        RobotController rc = G.rc;
        MapLocation best = null;
        int bs = Integer.MAX_VALUE;
        for (Direction d : G.fan(me, f)) {
            MapLocation l = me.add(d);
            if (!rc.canPlaceRatTrap(l)) continue;
            int s2 = l.distanceSquaredTo(e);
            if (adj) {
                if (s2 > 2) continue;
                s2 = s2 * 2 + (G.pickBit(me, l) ? 0 : 1);
                if (s2 < bs) { bs = s2; best = l; }
            } else if (s2 < bs) { bs = s2; best = l; }
        }
        return adj || bs < d0 ? best : null;
    }

    /** Bite the best adjacent enemy: a king first, then the lowest health; turn to face it when needed. */
    static boolean biteEnemies(RobotInfo[] robots) throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.isActionReady()) return false;
        MapLocation me = rc.getLocation();
        int myHp = rc.getHealth();
        RobotInfo best = null, bestTurn = null;
        int bs = Integer.MAX_VALUE, bt = Integer.MAX_VALUE;
        for (RobotInfo r : robots) {
            if (r.team != G.them) continue;
            MapLocation t = r.location;
            // a king: its tile adjacent to us (one we can bite now, if any)
            if (r.type == UnitType.RAT_KING) t = kingTile(rc, me, t);
            if (me.distanceSquaredTo(t) > 2) continue;
            int s = r.type == UnitType.RAT_KING ? r.health : biteScore(r.health, r.cheeseAmount, Cfg.CARRY_BITE);
            if (rc.canAttack(t)) { if (s < bs) { bs = s; best = r; } }
            else if (s < bt) { bt = s; bestTurn = r; }
        }
        // turning is free of action cooldown: a better target behind us wins if we can turn this turn
        RobotInfo r = best;
        if (bestTurn != null && (best == null || bt < bs) && rc.isTurningReady()) r = bestTurn;
        if (r == null) return false;
        MapLocation t = r.location;
        if (r.type == UnitType.RAT_KING) t = kingTile(rc, me, t);
        if (!rc.canAttack(t)) {
            Direction d = me.directionTo(t);
            if (rc.getDirection() != d && rc.canTurn(d)) rc.turn(d);
            G.facing = rc.getDirection();
        }
        if (rc.canAttack(t)) {
            int c = r.type == UnitType.BABY_RAT ? finishCheese(r.health, rc.getGlobalCheese()) : 0;
            if (c > 0 && rc.canAttack(t, c)) rc.attack(t, c); else rc.attack(t);
            bitesRat++;
            return true;
        }
        return false;
    }

    /** A baby rat's bite rank (lower first): 1000 + hp, less its loot up to cap (Cfg.CARRY_BITE), never under 1001. */
    static int biteScore(int hp, int cheese, int cap) {
        int loot = Math.min(cheese, cap);
        return loot > 0 ? Math.max(1001, 1000 + hp - loot) : 1000 + hp;
    }

    /** A tile of the king centred on c we can bite now, else its tile nearest us (to turn to). */
    static MapLocation kingTile(RobotController rc, MapLocation me, MapLocation c) throws GameActionException {
        for (int ex = -1; ex <= 1; ex++)
            for (int ey = -1; ey <= 1; ey++) {
                MapLocation l = c.translate(ex, ey);
                int d = me.distanceSquaredTo(l);
                if (d >= 1 && d <= 2 && rc.canAttack(l)) return l;
            }
        int dx = Math.max(-1, Math.min(1, me.x - c.x)), dy = Math.max(-1, Math.min(1, me.y - c.y));
        return c.translate(dx, dy);
    }

    /** Cheese that turns a bite on a target with hp into a kill (k*k for +k, k <= FINISH_K), else 0. */
    static int finishCheese(int hp, int global) {
        if (hp <= 10 || global < Cfg.FINISH_FLOOR) return 0;
        int k = hp - 10;
        return k <= Cfg.FINISH_K ? k * k : 0;
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

    /** A cat trap on an adjacent cat-lane tile the cat left CAT_LAY_AGE+ rounds ago, the most recently
     *  walked first (laneKey). */
    static void layLane() throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.isActionReady() || rc.getNumberCatTraps() >= 10 || rc.getAllCheese() < Math.max(Cfg.CAT_LAY_FLOOR, King.reserve(Cfg.KING_RESERVE) + 10) || King.austere()) return;
        MapLocation me = rc.getLocation(), best = null;
        int bs = Integer.MAX_VALUE;
        for (Direction d : G.DIRS) {
            MapLocation l = me.add(d);
            if (!CatLane.lane(l.x, l.y)) continue;
            int a = CatLane.age(l.x, l.y, G.round);
            if (a < Cfg.CAT_LAY_AGE || !rc.canPlaceCatTrap(l)) continue;
            int s = laneKey(a, l, G.pickBit(me, l));
            if (s < bs) { bs = s; best = l; }
        }
        if (best != null) { rc.placeCatTrap(best); trapsPlaced++; }
    }

    /** layLane's rank: age, then dist2 from the map centre (doubled coords; both mirror-invariant), then the pick bit. */
    static int laneKey(int age, MapLocation l, boolean bit) {
        int px = 2 * l.x - G.cx2, py = 2 * l.y - G.cy2;
        return ((Math.min(age, 4000) << 15) + Math.min(px * px + py * py, 32767)) * 2 + (bit ? 0 : 1);
    }

    /** A cat trap on a front tile within Chebyshev 1 of the cat's body now or over its next three steps (the trap
     *  fires on the 3x3 around it), nearest the cat first; near a king it needs only TRAP_FLOOR cheese. */
    static void tryCatTrap(RobotInfo c) throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.isActionReady()) return;
        if (rc.getAllCheese() < Cfg.TRAP_FLOOR) return;
        // defending a king is survival: traps there need only the floor; elsewhere they need projected surplus
        MapLocation k = Comms.nearestKing(G.me);
        boolean defend = k != null && k.distanceSquaredTo(c.location) <= Cfg.TRAP_DEFEND_DIST2;
        if (!defend && (King.austere() || rc.getAllCheese() < King.reserve(Cfg.KING_RESERVE) + 10 || !G.coop && Comms.read(Comms.BUDGET) * 4 < Cfg.TRAP_SURPLUS)) return;
        if (rc.getNumberCatTraps() >= 10) return;
        int d4 = catDist4(c.location, G.me);
        if (d4 > 4 * 36) return;
        MapLocation best = null;
        int bs = Integer.MAX_VALUE;
        MapLocation me = rc.getLocation();
        MapLocation n1 = c.location.add(c.direction), n2 = n1.add(c.direction), n3 = n2.add(c.direction);
        for (Direction d : G.fan(me, rc.getDirection())) {
            MapLocation l = me.add(d);
            if (!rc.canPlaceCatTrap(l)) continue;
            // trigger zone is the 3x3 around the trap: the cat must pass within Chebyshev 1 of it
            int s = Math.min(Math.min(chebToBody(n1, l), chebToBody(n2, l)), chebToBody(n3, l));
            s = Math.min(s, chebToBody(c.location, l));
            if (s > 1) continue;
            int score = s * 100 + catDist4(c.location, l);
            if (score < bs) { bs = score; best = l; }
        }
        if (best != null) { rc.placeCatTrap(best); trapsPlaced++; }
    }

    /** Chebyshev distance from p to the 2x2 body whose lower-left tile is bl (0 on it). */
    static int chebToBody(MapLocation bl, MapLocation p) {
        int dx = p.x < bl.x ? bl.x - p.x : (p.x > bl.x + 1 ? p.x - bl.x - 1 : 0);
        int dy = p.y < bl.y ? bl.y - p.y : (p.y > bl.y + 1 ? p.y - bl.y - 1 : 0);
        return Math.max(dx, dy);
    }

    /** Hand all our raw cheese to an allied king whose body is in transfer range. */
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

    /** Squeak one fact: to a king in hearing range a mine it may not know or a sharper symmetry mask; at war, an enemy's front. */
    static void report() {
        // a cat hears squeaks within dist2 16 and an ATTACK cat turns to them
        if (cat != null && catInEarshot(cat.location, G.me)) return;
        MapLocation k = Comms.numKings == 0 ? null : Comms.nearestKing(G.me);
        boolean near = k != null && G.me.distanceSquaredTo(k) <= 16;
        if (Comms.numMines == 0) {
            // no mine known to the team yet: pass mine news rat to rat, which outruns a courier walking home
            for (Message m : G.rc.readSqueaks(-1)) {
                int c = m.getBytes();
                if (Comms.sqType(c) == Comms.SQ_MINE) noteMine(Comms.sqX(c), Comms.sqY(c));
                else if (Comms.sqType(c) == Comms.SQ_SYM) Sym.merge(Comms.sqExtra(c) & 7);
            }
            if (symGossip(near)) return;
            if (myMineCount > 0 && !near) {
                int key = myMines[G.round % myMineCount];
                G.rc.squeak(Comms.squeak(Comms.SQ_MINE, key / 64, key % 64, 0));
                return;
            }
        } else {
            // every round since the last read (a squeak stays readable 4 rounds), not only the last
            readMissed();
            if (symGossip(near)) return;
            if (!near) {
                int key = unreported();
                if (key >= 0) { G.rc.squeak(Comms.squeak(Comms.SQ_MINE, key / 64, key % 64, 0)); return; }
            }
        }
        // a squeak reaches robots with any tile within dist2 16 of us: the king's centre within 16 is always heard
        if (near) {
            int sharedSym = Comms.read(Comms.SYM);
            if (sharedSym != 0 && (Sym.mask & sharedSym) != sharedSym) {
                G.rc.squeak(Comms.squeak(Comms.SQ_SYM, 0, 0, Sym.mask));
                return;
            }
            // (none with the list full)
            for (int i = 0; i < myMineCount && Comms.numMines < Comms.MINE_SLOTS; i++) {
                int x = myMines[i] / 64, y = myMines[i] % 64;
                if (!Comms.hasMine(x, y)) { G.rc.squeak(Comms.squeak(Comms.SQ_MINE, x, y, 0)); return; }
            }
        }
        if (k != null && G.me.distanceSquaredTo(k) <= 16 && catSeen != null && G.round - catSeenRound <= 2) {
            G.rc.squeak(Comms.squeak(Comms.SQ_CAT, catSeen.x, catSeen.y, 0));
        }
    }

    /** The round of our last symmetry gossip squeak, and the last round report() read squeaks of. */
    static int symSqueaked = -100, heardTo;

    /** report()'s squeak reads, from the round after the last read (at most 4 back) to the last round. */
    static void readMissed() {
        int r = Math.max(heardTo + 1, G.round - 4);
        for (; r <= G.round - 1; r++)
            for (Message m : G.rc.readSqueaks(r)) {
                int c = m.getBytes();
                if (Comms.sqType(c) == Comms.SQ_MINE) noteMine(Comms.sqX(c), Comms.sqY(c));
                else if (Comms.sqType(c) == Comms.SQ_SYM) Sym.merge(Comms.sqExtra(c) & 7);
            }
        heardTo = G.round - 1;
    }

    /** Some tile of a cat (2x2 from catBL) is within squeak range (d2 16) of p. */
    static boolean catInEarshot(MapLocation catBL, MapLocation p) {
        int dx = Math.max(0, Math.max(catBL.x - p.x, p.x - catBL.x - 1));
        int dy = Math.max(0, Math.max(catBL.y - p.y, p.y - catBL.y - 1));
        return dx * dx + dy * dy <= 16;
    }

    /** Cfg.SYM_COURIER: walk to the nearest king within SYM_COURIER while we know a sharper symmetry mask than the
     *  shared one and no enemy is in view (report() squeaks it once in earshot). */
    static boolean symCourier() throws GameActionException {
        int shared = Comms.read(Comms.SYM);
        if (shared == 0 || Sym.mask == 0 || (Sym.mask & shared) == shared || !G.coop && nearestEnemy != null) return false;
        MapLocation k = Comms.nearestKing(G.me);
        if (k == null || G.cheb(G.me, k) > Cfg.SYM_COURIER) return false;
        Nav.goTo(k);
        return true;
    }

    /** Away from the kings, squeak a mask sharper than the shared one every SYM_GOSSIP_EVERY rounds,
     *  when an ally can hear it, no enemy is in view at war, and no cat can hear it. */
    static boolean symGossip(boolean near) {
        if (near || G.round - symSqueaked < Cfg.SYM_GOSSIP_EVERY) return false;
        int shared = Comms.read(Comms.SYM);
        if (shared == 0 || (Sym.mask & shared) == shared) return false;
        if (!G.coop && nearestEnemy != null || cat != null && catInEarshot(cat.location, G.me)) return false;
        boolean ally = false;
        for (RobotInfo r : seen) if (r.team == G.us && G.me.distanceSquaredTo(r.location) <= 16) { ally = true; break; }
        if (!ally) return false;
        G.rc.squeak(Comms.squeak(Comms.SQ_SYM, 0, 0, Sym.mask));
        symSqueaked = G.round;
        return true;
    }

    // ---------------------------------------------------------------- movement

    /** True this turn when nearestEnemy is a prey (a rat we could ratnap). */
    static boolean hunting;

    /** Which branch took the last movement decision (debug tag mv=). */
    static char why = '-';

    /** At war, before round Cfg.WAR_NO_CAT_END, fighters leave cats alone. */
    static boolean warNoCat() { return !G.coop && G.round < Cfg.WAR_NO_CAT_END; }

    /** The turn's move, by the first branch that takes it (why records which): urgent errands first (urgentMove), then
     *  a fighter's cat, a load to deliver, mine news to carry home, the cheese target, scouting, seeking, the mine,
     *  and exploring. */
    static void moveDecision() throws GameActionException {
        if (GrabMicro.round == G.round) return;
        why = '-';
        int raw = G.rc.getRawCheese();
        if (urgentMove(raw)) return;
        Debug.mark(16);
        why = 'g';
        if (cat != null && role == FIGHT && catReach() && !warNoCat()) {
            if (approachCat(cat)) return;
        }
        boolean late = G.round > 1960;
        if (loadAt > 0) {
            // full loads: no trip home with a part load; with no pile in sight the gatherer stays at its mine
            if (fullLoad(raw, late, loadAt) && deliver()) return;
        } else if ((raw >= Cfg.DELIVER_AT || (raw > 0 && (late || cheeseTarget == null && raw >= Cfg.DELIVER_IDLE))) && deliver()) return;
        if (Comms.numMines == 0 && myMineCount > 0) {
            // we found a mine nobody else knows: squeaks only reach the king from dist2 16, so carry the news home
            MapLocation k = Comms.nearestKing(G.me);
            if (k != null) { Nav.goTo(k); why = 'm'; return; }
        }
        // standing on a pile we may not take (Nav.goTo returns before counting the stall): drop it
        if (cheeseTarget != null && G.me.equals(cheeseTarget) && !canTake(cheeseTarget)) cheeseTarget = null;
        if (cheeseTarget != null) { cheeseMove(); return; }
        if (role == FIGHT) {
            MapLocation ct = catReach() && !warNoCat() ? knownCat() : null;
            if (ct != null) { Nav.goTo(ct); catStalled(ct); return; }
        }
        if (scouting()) { scoutMove(); return; }
        if (role == GATHER && G.round < seekUntil && seek()) { why = 'x'; return; }
        if ((role == GATHER || role == FIGHT) && mineMove()) return;
        why = 'E';
        explore();
    }

    /** The errands that come before gathering: a cat at our king, the endgame cat, fleeing a cat, a carried enemy (aim
     *  a throw, carry it to a king or a cat), a rescue, war safety (ammo, retreat), a king formation, flanking, facing
     *  an enemy, a stuck attacker's delivery, the attackers' cat hunt and rush, the symmetry courier and war moves.
     *  True when one took the turn. */
    private static boolean urgentMove(int raw) throws GameActionException {
        RobotController rc = G.rc;
        // Cfg.CAT_YIELD_R2 (yl43): at war the cat at our king gives way to an enemy baby close to this rat
        if (cat != null && catReach() && catAtKing(cat.location) && !catYield() && approachCat(cat)) { why = 'g'; return true; }
        boolean endGo = G.coop ? G.round >= Cfg.END_CAT_COOP || G.round < stallCatUntil
                : G.round >= Cfg.END_CAT;
        if (endGo && endCat(raw)) { why = 'e'; return true; }
        // gatherers keep out of cat reach; at war fighters do too (warNoCat), but not with an enemy rat in view, so the
        // war branches below still run; not while carrying an enemy to feed to a cat (the carry-to-cat branch below)
        if (cat != null && (role == GATHER || role == FIGHT && warNoCat() && nearestEnemy == null)
                && catDist4(cat.location, G.me) <= 4 * Cfg.GATHER_CAT_DIST && !feedingCat()) {
            // flee the cat's centre (its 2x2 centre in doubled coordinates)
            if (Nav.flee2(2 * cat.location.x + 1, 2 * cat.location.y + 1)) { why = 'c'; return true; }
        }
        if (!G.coop && rc.getCarrying() != null && rc.getCarrying().team == G.them && aimCarry()) { why = 'T'; return true; }
        if (!G.coop && rc.getCarrying() != null && rc.getCarrying().team == G.them) {
            RobotInfo k = carryKing(seen);
            if (k != null) { Nav.goTo(k.location); why = 'J'; return true; }
        }
        if (!G.coop && rc.getCarrying() != null && rc.getCarrying().team == G.them) {
            RobotInfo c = carryCat(seen);
            if (c != null) { Nav.goTo(c.location); why = 'F'; return true; }
        }
        if (rescueT != null && rescue()) { why = 'R'; return true; }
        Debug.mark(10);
        if (!G.coop && ammoAway()) { why = 'A'; return true; }
        if (!G.coop && retreat()) { why = 'r'; return true; }
        if (formKing()) { why = 'K'; return true; }
        if (cashSite != null && cashMove()) { why = 'Q'; return true; }
        Debug.mark(11);
        Debug.mark(12);
        if (!G.coop && flank(seen)) { why = 'k'; return true; }
        Debug.mark(13);
        Debug.mark(14);
        if (!G.coop && (role != GATHER || Cfg.WAR_GATHER < 2 || nearEnemy(2)) && faceEnemy()) { why = 'f'; return true; }
        if (atkStuck(raw) && deliver()) return true;
        if (role == ATTACK && G.coop && stallCat()) { why = 'g'; return true; }
        if (symCourier()) { why = 'y'; return true; }
        if (role == ATTACK && attack()) { why = 'a'; return true; }
        Debug.mark(15);
        if (!G.coop && war()) { why = 'w'; return true; }
        return false;
    }

    /** Walk to the cheese target; a pile on dirt next to us is picked up from here instead. A walk stalled 12 turns
     *  drops the target (a walled-off one counts toward a breakout). */
    private static void cheeseMove() throws GameActionException {
        RobotController rc = G.rc;
        if (G.cheb(G.me, cheeseTarget) == 1 && Nav.isDirt(cheeseTarget)) {
            // cheese on dirt: pick it up from here, facing it, instead of digging onto the tile
            Direction d = G.me.directionTo(cheeseTarget);
            if (rc.getDirection() != d && rc.canTurn(d)) { rc.turn(d); G.facing = d; }
            pickUp();
            if (!rc.canSenseLocation(cheeseTarget) || rc.senseMapInfo(cheeseTarget).getCheeseAmount() == 0) cheeseTarget = null;
            why = 'P';
            return;
        }
        why = 'C';
        Nav.goTo(cheeseTarget);
        if (Nav.stall > 12) {
            if (walledOff(G.me, cheeseTarget)) wallStrike(G.round, Cfg.BREAKOUT_TTL);
            cheeseTarget = null;
        }
    }

    /** A scout carries news the king lacks home (squeaks reach the king from dist2 16 only), else explores: its fan
     *  sector's frontier, then never-seen tiles of our half, then anywhere. */
    private static void scoutMove() throws GameActionException {
        MapLocation k = Comms.nearestKing(G.me);
        if (k != null && unreported() >= 0) {
            Nav.goTo(k); why = 's'; return;
        }
        why = 'S';
        if (fanFrontier()) return;
        if (!exploreHome()) explore();
    }

    /** A gatherer's or fighter's mine: pick one (again when the team's list changed, or for a fighter once the war
     *  starts), seek elsewhere when idle or crowded there, wander around it once there, else walk to it. False when
     *  there is no mine to go to; true when this took the turn. */
    private static boolean mineMove() throws GameActionException {
        if (mineTarget != null && Comms.numMines != minesAtPick) mineTarget = null;
        if ((postMargin > 0 && role == FIGHT || Cfg.RAID_SHARE > 0 && ((slot >>> 12) & 3) < Cfg.RAID_SHARE) && coopAtPick && !G.coop) mineTarget = null;
        // a newborn's first turn (~12.6k spent by here) without the bytecodes for pickMine (~300 per known mine) and
        // the walk after it waits: picking next turn beats overrunning into it
        if (mineTarget == null && G.round == birthRound
                && Flow.left() < Cfg.NB_PICK_PAD + Cfg.NB_PICK_PER * (Comms.numMines + myMineCount)) { lowBc++; return true; }
        if (mineTarget == null) mineTarget = pickMine();
        if (mineTarget == null) return false;
        if (idle(mineTarget)) {
            mineShift++; mineTarget = pickMine();
            if (role == GATHER && idleSeek() && seek()) { why = 'x'; return true; }
            if (mineTarget == null) mineTarget = pickMine();
        }
        // a rat that wandered at this mine in the last 3 rounds stays at it out to WANDER_R + 1 (a wander step around
        // a wall can leave the ring, and walking straight back flipped between two tiles)
        boolean held = G.round - wanderRound <= 3 && mineTarget.equals(wanderMine)
                && G.cheb(G.me, mineTarget) <= Cfg.WANDER_R + 1;
        if (held || atMine(mineTarget, Cfg.MINE_KING_R)) {
            wanderRound = G.round; wanderMine = mineTarget;
            if (role == GATHER && startSeek(mineTarget) && seek()) { why = 'x'; return true; }
            // wander around the mine, facing outward to see new cheese
            wanderAround(mineTarget);
            why = 'W';
        } else {
            why = 'M';
            Nav.goTo(mineTarget);
            if (Nav.stall > 25) {
                mineShift++;   // the re-pick moves on to the next mine
                mineTarget = null;
                if (G.coop) stallCatUntil = G.round + Cfg.MINE_STALL_CAT;
            }
        }
        return true;
    }

    /** We carry an enemy and a cat to feed it to is in reach of the carry (carryCat). */
    static boolean feedingCat() {
        RobotInfo c = G.rc.getCarrying();
        return !G.coop && c != null && c.team == G.them && carryCat(seen) != null;
    }

    /** Head for the nearest king to hand over our cheese (face it once in transfer reach); false with no king known. */
    static boolean deliver() throws GameActionException {
        MapLocation k = Comms.nearestKing(G.me);
        if (k == null) return false;
        if (inTransferReach(G.me, k)) {
            // GM_DELIVER: the grab micro weighed every transfer facing against the enemies within reach and took none:
            // keep our facing (guardTurn turns to the threat) unless the bank runs low
            if (GrabMicro.holdRound == G.round && G.globalCheese >= Cfg.DG_BANK) { why = 'G'; return true; }
            // in transfer range of the king's body (dist2 9 to its nearest tile): face it, stay off the ring
            Direction d = G.me.directionTo(k);
            if (d != Direction.CENTER && G.rc.getDirection() != d && G.rc.canTurn(d)) G.rc.turn(d);
            why = 'D';
            return true;
        }
        why = 'd';
        if (homeLeg(k)) return true;
        Nav.deliverK = true;
        try { Nav.goTo(k); } finally { Nav.deliverK = false; }
        return true;
    }

    /** homeLeg's trip: the nearest the king has been (Chebyshev), turns since that got better, the round of the last
     *  call, turns the trail has been held by robots, whether we are walking the trail, and Baby.delivered at the
     *  trip's start. */
    static int homeBest, homeStall, homeRound = -10, homeHeld;
    static boolean homing;
    static int homeDl;
    /** Cfg.TRAIL_GAP (a field so tests can set it). */
    static int trailGap = Cfg.TRAIL_GAP;

    /** After TRAIL_STALL turns of delivery without getting closer to the nearest king (a roaming king does
     *  not reset the count, unlike Nav's stall), walk the trail back until it ends; while it is held by robots, wait
     *  FLOW_HOLD turns, then let Nav route around them. True when this took the turn. */
    static boolean homeLeg(MapLocation k) throws GameActionException {
        // a new trip (a call gap, see TRAIL_GAP) or a transfer since the last call restarts the count: carried over, a
        // short trip inherited the last trip's best and walked the trail early
        if ((trailGap > 0 ? G.round - homeRound > trailGap : homeRound != G.round - 1) || delivered != homeDl) { homing = false; homeBest = Integer.MAX_VALUE; homeStall = 0; homeHeld = 0; homeDl = delivered; }
        homeRound = G.round;
        int c = G.cheb(G.me, k);
        if (c < homeBest) { homeBest = c; homeStall = 0; } else homeStall++;
        if (!homing && homeStall < Cfg.TRAIL_STALL) return false;
        homing = true;
        if (!G.rc.isMovementReady()) return true;
        int r = Trail.back();
        if (r == Trail.MOVED) { homeHeld = 0; return true; }
        if (r == Trail.END) { homing = false; homeStall = 0; homeBest = c; return false; }
        return ++homeHeld <= Cfg.FLOW_HOLD;
    }

    // ---------------------------------------------------------------- attack and war

    /** Symmetry candidates whose enemy-king spot we saw empty. */
    static int noKing;
    /** Where and when we last saw the enemy king. */
    static MapLocation lastEKing;
    static int lastEKingRound = -100;

    /** Attacker movement: the visible enemy king, else the last place we saw it (30 rounds), else the nearest enemy
     *  king start that the live symmetries allow. False with none left. */
    static boolean attack() throws GameActionException {
        if (eKing != null) { lastEKing = eKing.location; lastEKingRound = G.round; Nav.goTo(eKing.location); return true; }
        // reached the sighting and can sense it: no king there, so forget it
        if (lastEKing != null && G.me.distanceSquaredTo(lastEKing) <= 8
                && G.rc.canSenseLocation(lastEKing)) lastEKing = null;
        if (lastEKing != null && G.round - lastEKingRound < 30 && G.me.distanceSquaredTo(lastEKing) > 8) {
            Nav.goTo(lastEKing);
            return true;
        }
        MapLocation t = rushTarget();
        if (t == null) return false;
        Nav.goTo(t);
        return true;
    }

    /** The nearest mirror of home under a live symmetry whose spot we have not seen without the enemy king (ties by
     *  pick bit); null (and the seen-empty set reset) when every one was seen empty. */
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
            int d = G.me.distanceSquaredTo(c) * 2 + (G.pickBit(G.me, c) ? 1 : 0);
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

    /** e is behind a wall from a. At Chebyshev 2, no tile beside both is open (reach1:
     *  e cannot get next to a in one move); farther, the straight tile line between them crosses a known wall tile
     *  whose two neighbours across the line are wall, dirt or off the map, so the way around is long. */
    static boolean behindWall(MapLocation a, MapLocation e) {
        int dx = e.x - a.x, dy = e.y - a.y, ax = Math.abs(dx), ay = Math.abs(dy), n = Math.max(ax, ay);
        if (n <= 1) return false;
        if (n == 2) return !reach1(a, e);
        int[] mem = Sym.mem;
        int W = G.W;
        for (int i = 1; i < n; i++) {
            int x = a.x + lerp(dx, i, n), y = a.y + lerp(dy, i, n);
            if ((mem[x + y * W] & Sym.WALL) == 0) continue;
            if (ay >= ax && blocked(x - 1, y) && blocked(x + 1, y)) return true;
            if (ax >= ay && blocked(x, y - 1) && blocked(x, y + 1)) return true;
        }
        return false;
    }

    /** round(d * i / n), halves away from zero. */
    static int lerp(int d, int i, int n) {
        int v = 2 * d * i;
        return v >= 0 ? (v + n) / (2 * n) : -((n - v) / (2 * n));
    }

    /** Off the map, or a known wall or dirt tile. */
    static boolean blocked(int x, int y) {
        return x < 0 || y < 0 || x >= G.W || y >= G.H || (Sym.mem[x + y * G.W] & (Sym.WALL | Sym.DIRT)) != 0;
    }

    /** Some tile next to both a and e (on the map, not a known wall or dirt) is open, so e can get next to a in one
     *  move. */
    static boolean reach1(MapLocation a, MapLocation e) {
        int[] mem = Sym.mem;
        int W = G.W, H = G.H, x1 = Math.min(a.x, e.x) + 1, y0 = Math.max(a.y, e.y) - 1, y1 = Math.min(a.y, e.y) + 1;
        for (int x = Math.max(a.x, e.x) - 1; x <= x1; x++) {
            if (x < 0 || x >= W) continue;
            for (int y = y0; y <= y1; y++) {
                if (y < 0 || y >= H || x == a.x && y == a.y || x == e.x && y == e.y) continue;
                if ((mem[x + y * W] & (Sym.WALL | Sym.DIRT)) == 0) return true;
            }
        }
        return false;
    }

    /** War movement: close on a visible enemy king or nearby enemy rat, or answer a king's alert. */
    static boolean war() throws GameActionException {
        int raw = G.rc.getRawCheese();
        MapLocation threatened = threatenedKing();
        if (threatened != null) defends++;
        if (role == GATHER && !hunting && threatened == null) {
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
        if (nearestEnemy != null) {
            int d = G.me.distanceSquaredTo(nearestEnemy.location);
            if (d <= 2) {
                // stay and face it: biting happens before and after movement
                Direction f = G.me.directionTo(nearestEnemy.location);
                if (G.rc.getDirection() != f && G.rc.canTurn(f)) G.rc.turn(f);
                return true;
            }
            if (d <= 20 && (role == FIGHT || raw < Cfg.DELIVER_AT || d <= 8)) {
                // standoff: stepping into the front of an enemy that sees us (and is not weaker) walks onto its
                // traps and into a grab; hold, face it and let it come (flank and prey moves still engage)
                // (not against a rat at our threatened king: it is biting the king while we wait)
                boolean atKing = threatened != null && nearestEnemy.location.distanceSquaredTo(threatened) <= Cfg.KING_THREAT_D2;
                if (!hunting && !atKing && sees(nearestEnemy, G.me) && nearestEnemy.health >= G.rc.getHealth()) {
                    Direction f = G.me.directionTo(nearestEnemy.location);
                    if (G.rc.getDirection() != f && G.rc.canTurn(f)) { G.rc.turn(f); G.facing = f; }
                    return true;
                }
                Nav.goTo(nearestEnemy.location);
                return true;
            }
        }
        if (Comms.fresh(Comms.read(Comms.ALERT + 2), 3)) {
            MapLocation a = Comms.alertLoc();
            if (a != null && G.me.distanceSquaredTo(a) <= Cfg.ALERT_DIST2 && G.me.distanceSquaredTo(a) > 8) {
                Nav.goTo(a);
                return true;
            }
        }
        return false;
    }

    /** Debug count: war turns with a threatened king near us. */
    public static int defends;

    /** Our king's location when it reported enemy rats close to it this or last round and we are near enough to
     *  help (gatherers then fight instead of gathering), else null. */
    static MapLocation threatenedKing() {
        if (G.coop || !Comms.fresh(Comms.read(Comms.ALERT + 2), 2) || Comms.alertThreat() < 1) return null;
        MapLocation a = Comms.alertLoc();
        return a != null && G.me.distanceSquaredTo(a) <= Cfg.DEFEND_D2 ? a : null;
    }

    /** True when a baby rat r (facing r.direction) can see tile t: dot >= |cross| with its facing, dist2 <= 20. */
    static boolean sees(RobotInfo r, MapLocation t) {
        int dx = t.x - r.location.x, dy = t.y - r.location.y;
        Direction f = r.direction;
        if (f == null || f == Direction.CENTER) return true;
        return dx * dx + dy * dy <= 20 && dx * f.dx + dy * f.dy >= Math.abs(dx * f.dy - dy * f.dx);
    }

    /** This turn's enemy carrier of one of ours within RESCUE_D2, or null. */
    static RobotInfo rescueT;
    /** Cfg.RESCUE_KING_R: this rat was within Chebyshev RESCUE_KING_R of our nearest king at round nearKR's war setup. */
    static boolean nearK;
    static int nearKR = -1;
    /** Debug count: rescue moves made. */
    public static int rescues;

    /** r carries one of our rats. */
    static boolean holdsOurs(RobotInfo r) { return r.carryingRobot != null && r.carryingRobot.team == G.us; }

    /** The nearest enemy baby within dist2 RESCUE_D2 carrying one of ours (ties: the healthier one held, then
     *  G.pickBit), or null; none in cooperation (a grab is a backstab). */
    static RobotInfo rescueTarget(RobotInfo[] robots) {
        if (G.coop) return null;
        RobotInfo best = null;
        int bd = Integer.MAX_VALUE;
        for (RobotInfo r : robots) {
            if (r.team != G.them || r.type != UnitType.BABY_RAT || !holdsOurs(r)) continue;
            int d = G.me.distanceSquaredTo(r.location);
            if (d > Cfg.RESCUE_D2) continue;
            int s = (d * 128 + 100 - Math.min(100, r.carryingRobot.health)) * 2 + (G.pickBit(G.me, r.location) ? 0 : 1);
            if (s < bd) { bd = s; best = r; }
        }
        return best;
    }

    /** Get behind rescueT: on a tile next to it that it cannot see, turn to it (the post-move carry grabs it); else
     *  step to such a tile (turning to it first, as flank does). False when neither is possible. */
    static boolean rescue() throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.isActionReady()) return false;
        MapLocation c = rescueT.location;
        if (G.cheb(G.me, c) == 1 && !sees(rescueT, G.me)) {
            Direction f = G.me.directionTo(c);
            if (rc.getDirection() != f && rc.canTurn(f)) { rc.turn(f); G.facing = f; }
            rescues++;
            return true;
        }
        if (!rc.isMovementReady()) return false;
        Direction bestD = null;
        int bs = Integer.MAX_VALUE;
        for (int i = 0; i < 8; i++) {
            Direction d = G.DIRS[i];
            MapLocation t = G.me.add(d);
            if (G.cheb(t, c) != 1 || sees(rescueT, t)) continue;
            if ((Nav.avoid & (1 << i)) != 0 || !rc.canMove(d)) continue;
            int sc = (d == rc.getDirection() ? 0 : 1) * 2 + (G.hand(G.me, d) > 0 ? 1 : 0);
            if (sc < bs) { bs = sc; bestD = d; }
        }
        if (bestD == null) return false;
        MapLocation g = Nav.guard;
        Nav.guard = c;
        boolean moved = Nav.step(bestD);
        Nav.guard = g;
        if (moved) rescues++;
        return moved;
    }

    /** Step onto a tile adjacent to an enemy baby that it cannot see, keeping it in view; the post-move carry grabs it. */
    static boolean flank(RobotInfo[] robots) throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.isActionReady() || !rc.isMovementReady() || rc.getCarrying() != null) return false;
        RobotInfo bestE = null;
        Direction bestD = null;
        int bs = Integer.MAX_VALUE;
        for (RobotInfo e : robots) {
            if (e.team != G.them || e.type != UnitType.BABY_RAT) continue;
            if (G.me.distanceSquaredTo(e.location) > 8) continue;
            for (int i = 0; i < 8; i++) {
                Direction d = G.DIRS[i];
                MapLocation t = G.me.add(d);
                if (G.cheb(t, e.location) != 1 || sees(e, t)) continue;
                if ((Nav.avoid & (1 << i)) != 0 || !rc.canMove(d)) continue;
                int sc = e.health * 4 + (d == rc.getDirection() ? 0 : 1) * 2 + (G.hand(G.me, d) > 0 ? 1 : 0);
                if (sc < bs) { bs = sc; bestE = e; bestD = d; }
            }
        }
        if (bestE == null) return false;
        MapLocation g = Nav.guard;
        Nav.guard = bestE.location;
        boolean moved = Nav.step(bestD);
        Nav.guard = g;
        return moved;
    }

    /** b lies behind a wall from a. The segment a-b crosses a known wall, and so do both copies of it
     *  shifted one tile across (ends included, or off the map): a lone pillar leaves a way round, dirt can be dug. */
    static boolean walled(MapLocation a, MapLocation b) {
        // Cfg.WALL_BLIND_CAP: at most CAP traces a turn (~300 bytecodes each); past it an enemy counts as in the open
        if (wbRound != G.round) { wbRound = G.round; wbCalls = 0; }
        if (++wbCalls > Cfg.WALL_BLIND_CAP) return false;
        int dx = b.x - a.x, dy = b.y - a.y;
        int px = Math.abs(dx) >= Math.abs(dy) ? 0 : 1, py = 1 - px;
        return wallLine(a.x, a.y, dx, dy, 1) && wallLine(a.x + px, a.y + py, dx, dy, 0)
            && wallLine(a.x - px, a.y - py, dx, dy, 0);
    }

    /** Cfg.WALL_BLIND_CAP: the round walled() last ran and its traces that round. */
    static int wbRound = -1, wbCalls;

    /** Tiles i = from..n - from of the segment from (x0, y0) by (dx, dy) in n = Chebyshev steps: one is a known wall or
     *  off the map. */
    static boolean wallLine(int x0, int y0, int dx, int dy, int from) {
        int n = Math.max(Math.abs(dx), Math.abs(dy)), W = G.W, H = G.H, sx = dx < 0 ? -n : n, sy = dy < 0 ? -n : n;
        int[] mem = Sym.mem;
        for (int i = from, e = n - from; i <= e; i++) {
            int x = x0 + (2 * dx * i + sx) / (2 * n), y = y0 + (2 * dy * i + sy) / (2 * n);
            if (x < 0 || y < 0 || x >= W || y >= H || (mem[x + W * y] & Sym.WALL) != 0) return true;
        }
        return false;
    }

    /** The nearest enemy baby within dist2 20 that we could ratnap (health below hp) and no wall hides, or null.
     *  Cfg.PREY_WEAKEST (yl33): the weakest such baby instead, the nearest of equals. */
    static RobotInfo prey(RobotInfo[] robots, int hp) {
        RobotInfo prey = null;
        int pd = Integer.MAX_VALUE;
        for (RobotInfo r : robots) {
            if (r.team != G.them || r.type != UnitType.BABY_RAT || r.health >= hp) continue;
            int d = G.me.distanceSquaredTo(r.location);
            // Cfg.PREY_WEAKEST (yl33): the lowest health first, then the nearest (d <= 20 < 64)
            int k = Cfg.PREY_WEAKEST ? r.health * 64 + d : d;
            if (d <= 20 && k < pd && !(d > 2 && walled(G.me, r.location))) { pd = k; prey = r; }
        }
        return prey;
    }


    /** Step away from our king (or hold) while ammo(...) holds; false when it doesn't. */
    static boolean ammoAway() throws GameActionException {
        if (nearestEnemy == null) return false;
        MapLocation k = Comms.nearestKing(G.me);
        if (k == null || !ammo(G.rc.getHealth(), nearestEnemy.health, G.me.distanceSquaredTo(nearestEnemy.location), G.cheb(G.me, k))) return false;
        Nav.flee(k);
        return true;
    }

    /** True when a baby with health hp, cheb from our king's centre, is grabbable ammunition for an enemy rat with
     *  health ehp at dist2 d2. */
    static boolean ammo(int hp, int ehp, int d2, int cheb) {
        return hp <= Cfg.AMMO_HP && ehp > hp && d2 <= Cfg.AMMO_D2 && cheb <= Cfg.AMMO_R;
    }

    /** A rat with RETREAT_HP or less and an enemy within dist2 20 walks to our nearest king (unless within 2). */
    static boolean retreat() throws GameActionException {
        if (G.rc.getHealth() > Cfg.RETREAT_HP || !nearEnemy(20)) return false;
        MapLocation k = Comms.nearestKing(G.me);
        if (k == null || G.cheb(G.me, k) <= 2) return false;
        Nav.goTo(k);
        return true;
    }

    /** nearestEnemy is within dist2 d2. */
    static boolean nearEnemy(int d2) {
        return nearestEnemy != null && G.me.distanceSquaredTo(nearestEnemy.location) <= d2;
    }

    /** True when the nearest tile of a 3x3 king centred at k is within transfer range (dist2 9) of p. */
    static boolean inTransferReach(MapLocation p, MapLocation k) {
        int dx = Math.max(0, Math.abs(p.x - k.x) - 1), dy = Math.max(0, Math.abs(p.y - k.y) - 1);
        return dx * dx + dy * dy <= 9 && !(Math.abs(p.x - k.x) <= 1 && Math.abs(p.y - k.y) <= 1);
    }

    /** Cfg.END_CAT: walk to the nearest seen or known cat (the bite step bites it); false when this baby has other work
     *  (a load, a carried rat, a threatened king, an enemy in reach, a formation site to join). */
    static boolean endCat(int raw) throws GameActionException {
        if (raw >= Cfg.DELIVER_AT || G.rc.getCarrying() != null || !catReach()) return false;
        // Cfg.END_CAT_COOP: enemy rats cannot hurt us during cooperation
        if (threatenedKing() != null || !G.coop && nearEnemy(8)) return false;
        // a king formation (END_KING, CASH_END) outweighs the cat: its joiners go on to formKing and cashMove
        if (formSite() != null || cashSite != null) return false;
        // Cfg.END_CAT_COOP_FREE: in cooperation the cat share (0.5) outweighs a second king (0.3), so the lone king's
        // babies take cats until that round and only then come back for END_KING's site
        if (Comms.numKings < 2 && G.globalCheese >= endCatBar(G.round) && !(G.coop && G.round < Cfg.END_CAT_COOP_FREE)) return false;
        if (cat != null) return approachCat(cat);
        MapLocation ct = knownCat();
        // in cooperation with no cat known, look for one at the map centre
        if (ct == null && G.coop) ct = new MapLocation(G.cx2 / 2, G.cy2 / 2);
        if (ct == null) return false;
        Nav.goTo(ct);
        catStalled(ct);
        return true;
    }

    /** Cfg.END_CAT with a lone king: the bank below which no endgame king can form (END_KING's END_KING_CHEESE, CASH_END's
     *  bar), so the babies have no formation to stay for. */
    static int endCatBar(int round) {
        return Math.min(Cfg.END_KING_CHEESE, Math.max(Cfg.CASH_FLOOR, 4 * (2000 - round - 55) + 10));
    }

    /** The cat we saw in the last 10 rounds, else the nearest cat the kings reported in the last 30, else null. */
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

    /** Cfg.CAT_YIELD_R2 (yl43): at war, a cat at our king (CAT_DEFEND_R2) no longer draws a rat that has an enemy baby
     *  within dist2 CAT_YIELD_R2; the rat takes its war branches (retreat, face, fight) instead. */
    static boolean catYield() {
        return Cfg.CAT_YIELD_R2 > 0 && !G.coop && nearestEnemy != null
            && G.me.distanceSquaredTo(nearestEnemy.location) <= Cfg.CAT_YIELD_R2;
    }

    /** A cat at c is within dist2 Cfg.CAT_DEFEND_R2 of one of our live kings. */
    static boolean catAtKing(MapLocation c) {
        for (int k = 0; k < Comms.numKings; k++)
            if (c.distanceSquaredTo(Comms.kings[k]) <= Cfg.CAT_DEFEND_R2) return true;
        return false;
    }

    /** Move to a tile adjacent to the cat body, outside its forward lane (true when already there). */
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
                int d = me.distanceSquaredTo(t) * 2 + (G.pickBit(me, t) ? 1 : 0);
                if (d < bd) { bd = d; best = t; }
            }
        if (best == null) return false;
        Nav.goTo(best);
        catStalled(best);
        return true;
    }

    /** CAT_REACH: the cat branches are open (no ban running). */
    static boolean catReach() { return reachTurns == 0 || G.round >= catBanUntil; }

    /** CAT_REACH: after a step toward the cat at t, a walk stalled reachTurns turns or known to be a detour closes the
     *  cat branches for CAT_REACH_TTL rounds. */
    static void catStalled(MapLocation t) {
        if (reachTurns > 0 && (Nav.stall >= reachTurns || Flow.detour(t))) catBanUntil = G.round + Cfg.CAT_REACH_TTL;
    }

    /** This rat's mine: a mine drops 20 cheese about every 12 rounds into its 9x9 square however many rats wait
     *  there, so gatherers spread over the known mines (our half first, then by distance from home, ties by the
     *  mirror-invariant offset), each taking the one its slot and mineShift pick. A fighter at war keeps to contested
     *  home mines. Null with no mine known. */
    static MapLocation pickMine() {
        int n = 0;
        for (int i = 0; i < Comms.numMines && n < cand.length; i++) n = addCand(Comms.mines[i], n);
        for (int i = 0; i < myMineCount && n < cand.length; i++) n = addCand(new MapLocation(myMines[i] / 64, myMines[i] % 64), n);
        if (n == 0) return null;
        int ours = 0;
        for (int i = 0; i < n; i++) if (homeHalf(cand[i])) { MapLocation t = cand[ours]; cand[ours++] = cand[i]; cand[i] = t; }
        int m = n;
        long[] key = candKey;
        for (int i = 0; i < m; i++) key[i] = mineKey(cand[i], home);
        for (int i = 1; i < m; i++) {   // insertion sort of the first m by mineKey
            long k = key[i];
            MapLocation c = cand[i];
            int j = i;
            for (; j > 0 && k < key[j - 1]; j--) { key[j] = key[j - 1]; cand[j] = cand[j - 1]; }
            key[j] = k; cand[j] = c;
        }
        if (postMargin > 0 && role == FIGHT && !G.coop && m == ours) {
            // a fighter at war holds a contested home mine, where enemy thieves pick; none known: keep all
            MapLocation r = home, e = G.mirror(r, firstSym());
            int k = 0;
            for (int i = 0; i < m; i++) if (G.cheb(cand[i], e) - G.cheb(cand[i], r) <= postMargin) cand[k++] = cand[i];
            if (k > 0) m = k;
        }
        coopAtPick = G.coop;
        minesAtPick = Comms.numMines;
        MapLocation pick = cand[(slot % m + mineShift) % m];
        // Cfg.RAID_SHARE: a raider holds the enemy twin of its contested home mine
        if (raider() && homeHalf(pick)) pick = G.mirror(pick, firstSym());
        return pick;
    }

    /** Cfg.RAID_SHARE: this rat is a raider (a gatherer or fighter at war, RAID_SHARE in 4 by slot). */
    static boolean raider() {
        return Cfg.RAID_SHARE > 0 && (role == FIGHT || role == GATHER) && !G.coop && ((slot >>> 12) & 3) < Cfg.RAID_SHARE;
    }

    /** Cfg.LOAD_AT: a gatherer carrying raw cheese heads home once it holds at least the load, or late in the game. */
    static boolean fullLoad(int raw, boolean late, int load) {
        return raw > 0 && (raw >= load || late);
    }

    /** Cfg.ATK_STUCK: this attacker is taking its load home. */
    static boolean stuckHome;
    /** Cfg.ATK_STUCK: an attacker holding ATK_STUCK_RAW or more raw whose walk (Nav) has not got closer to its target
     *  for ATK_STUCK turns takes its load home (deliver(), with the trail) and keeps at it until it has none left. */
    static boolean atkStuck(int raw) {
        if (role != ATTACK || raw == 0) { stuckHome = false; return false; }
        if (!stuckHome && raw >= Cfg.ATK_STUCK_RAW && Nav.stall >= Cfg.ATK_STUCK) stuckHome = true;
        return stuckHome;
    }

    /** Distinct mines on our half that we know (the king's list and our own). */
    static int homeMineCount() {
        int n = 0;
        for (int i = 0; i < Comms.numMines && n < cand.length; i++) n = addCand(Comms.mines[i], n);
        for (int i = 0; i < myMineCount && n < cand.length; i++) n = addCand(new MapLocation(myMines[i] / 64, myMines[i] % 64), n);
        int ours = 0;
        for (int i = 0; i < n; i++) if (homeHalf(cand[i])) ours++;
        return ours;
    }

    /** A per-rat hash (the scout and mine picks), and the team's known-mine count when mineTarget was picked. */
    static int slot, minesAtPick;

    static final MapLocation[] cand = new MapLocation[40];
    static final long[] candKey = new long[40];

    /** Append l to the first n candidates if new; the new count. */
    static int addCand(MapLocation l, int n) {
        for (int i = 0; i < n; i++) if (cand[i].equals(l)) return n;
        cand[n] = l;
        return n + 1;
    }

    /** Sort key: squared distance from h, then the offset along our outward axes (same for mirrored rats). */
    static long mineKey(MapLocation m, MapLocation h) {
        return ((long) m.distanceSquaredTo(h) << 16) + ((m.x - h.x) * fu + 64) * 128 + (m.y - h.y) * fv + 64;
    }

    /** The site the king named for a second king, while its heartbeat is fresh. */
    static MapLocation formSite() {
        int t = Comms.read(Comms.FORM_T), x = Comms.read(Comms.FORM_X);
        if (t == 0 || x == 0 || !Comms.fresh(t, 2)) return null;
        return new MapLocation(x - 1, Comms.read(Comms.FORM_Y) - 1);
    }

    /** Join a king formation: walk into the site's 3x3 and hold; the rat on the centre promotes once 7 stand there. */
    static boolean formKing() throws GameActionException {
        MapLocation site = formSite();
        if (site == null) return false;
        int sd = G.me.distanceSquaredTo(site);
        if (sd > Cfg.FORM_PULL_R2) return false;
        if (Cfg.FORM_JOIN > 0 && G.cheb(G.me, site) > 1 && nearerAllies(site) >= Cfg.FORM_JOIN) return false;
        RobotController rc = G.rc;
        if (G.me.equals(site)) {
            if (rc.canBecomeRatKing()) { rc.becomeRatKing(); formed++; }
            return true;
        }
        if (G.cheb(G.me, site) <= 1) {
            // step onto the centre when it is free, else hold our ring tile facing it
            Direction d = G.me.directionTo(site);
            if (rc.canMove(d)) {
                if (rc.getDirection() != d && rc.canTurn(d)) { rc.turn(d); G.facing = d; }
                if (rc.canMove(d)) { rc.move(d); G.me = rc.getLocation(); }
            }
            // promote on the turn we step onto the centre
            if (G.me.equals(site) && rc.canBecomeRatKing()) { rc.becomeRatKing(); formed++; }
            return true;
        }
        Nav.goTo(site);
        return true;
    }

    /** Allied babies in view strictly nearer the site than we are (no id tie-break: mirrored rats decide alike). */
    static int nearerAllies(MapLocation site) {
        int d = G.me.distanceSquaredTo(site), n = 0;
        for (RobotInfo r : seen)
            if (r.team == G.us && r.type == UnitType.BABY_RAT && r.location.distanceSquaredTo(site) < d) n++;
        return n;
    }

    /** Debug count: promotions to king. */
    static int formed;

    // ---------------------------------------------------------------- rescue formation (Cfg.CASH_END)

    /** Rescue formation: the rescue site we lead or heard (null: none), its start round, the round we last heard it,
     *  the round before which no rescue runs (after one timed out), and whether we lead it. */
    static MapLocation cashSite;
    static int cashStart, cashHeard, cashBan;
    static boolean cashLead;

    /** Cfg.CASH_END: an endgame second king may run, whatever the bank holds in raw: from round CASH_END a lone king,
     *  and a bank that keeps a new king alive to round 2000 (both kings eat 2 a round; a starving king loses 10 health
     *  a round and a new one has up to 600, so it outlives 55 rounds on an empty bank). */
    static boolean cashEnd(int bank, int kings, int round) {
        return Cfg.CASH_END > 0 && round >= Cfg.CASH_END && kings == 1
            && bank >= Math.max(Cfg.CASH_FLOOR, 4 * (2000 - round - 55) + 10);
    }

    /** SQ_CASH's check bits: a squeak whose tag does not match its site is not ours. */
    static int cashTag(int x, int y) { return (x * 3 + y * 5 + 17) & 63; }

    /** Of two rescue sites the one started first wins, then the lower tile key: two leaders in one crowd merge. */
    static boolean cashFirst(int start, int key, int start2, int key2) { return start < start2 || start == start2 && key < key2; }

    /** Rescue formation, in place of report(): hear rescue sites, lead one when we qualify, and squeak ours (true when we
     *  squeaked; the squeak carries the site's start round, so relays die with it). */
    static boolean cashTick() throws GameActionException {
        RobotController rc = G.rc;
        if (!cashEnd(G.globalCheese, Comms.numKings, G.round) || G.round < cashBan) { cashSite = null; cashLead = false; return false; }
        if (cashSite != null && G.round - cashStart > Cfg.CASH_TTL) {
            cashSite = null;
            cashLead = false;
            cashBan = G.round + Cfg.CASH_TTL;
            return false;
        }
        MapLocation best = null;
        int bs = 0;
        for (Message m : rc.readSqueaks(-1)) {
            // Cfg.CASH_READ_LEFT: stop reading once the turn runs low
            if (Clock.getBytecodesLeft() < Cfg.CASH_READ_LEFT) break;
            int c = m.getBytes();
            if (Comms.sqType(c) != Comms.SQ_CASH || G.round - m.getRound() > 2) continue;
            int x = Comms.sqX(c), y = Comms.sqY(c), e = Comms.sqExtra(c);
            if ((e >>> 10) != cashTag(x, y)) continue;
            int start = G.round - ((((G.round & 1023) - (e & 1023)) + 1024) & 1023);
            if (G.round - start > Cfg.CASH_TTL) continue;
            if (best == null || cashFirst(start, x * 64 + y, bs, best.x * 64 + best.y)) { best = new MapLocation(x, y); bs = start; }
        }
        if (best != null && (cashSite == null
                || !best.equals(cashSite) && cashFirst(bs, best.x * 64 + best.y, cashStart, cashSite.x * 64 + cashSite.y))) {
            cashSite = best;
            cashStart = bs;
            cashLead = false;
        }
        if (best != null && best.equals(cashSite)) cashHeard = G.round;
        if (cashSite != null && !cashLead && G.round - cashHeard > 2) cashSite = null;
        if (cashSite == null) {
            // lead a new site: away from every king, the heaviest of our babies within CASH_R, and at least CASH_N of
            // us (no raw cheese needed: the endgame king itself scores)
            int raw = rc.getRawCheese();
            if (!cashFar(G.me)) return false;
            int n = 1, id = rc.getID();
            for (RobotInfo r : seen) {
                if (r.team != G.us || r.type != UnitType.BABY_RAT || G.cheb(r.location, G.me) > Cfg.CASH_R) continue;
                if (r.cheeseAmount > raw || r.cheeseAmount == raw && r.ID < id) return false;
                n++;
            }
            if (n < Cfg.CASH_N) return false;
            MapLocation s = cashSpot(G.me);
            if (s == null) return false;
            cashSite = s;
            cashStart = G.round;
            cashHeard = G.round;
            cashLead = true;
        }
        if (G.cheb(G.me, cashSite) > Cfg.CASH_PULL) { if (!cashLead) cashSite = null; return false; }
        if (cat != null && catInEarshot(cat.location, G.me)) return false;
        return rc.squeak(Comms.squeak(Comms.SQ_CASH, cashSite.x, cashSite.y, cashTag(cashSite.x, cashSite.y) << 10 | (cashStart & 1023)));
    }

    /** p is at least Chebyshev CASH_FAR from every king's centre. */
    static boolean cashFar(MapLocation p) {
        for (int k = 0; k < Comms.numKings; k++) if (G.cheb(Comms.kings[k], p) < Cfg.CASH_FAR) return false;
        return true;
    }

    /** A rescue site at c or beside it: its 3x3 on the map, no wall, dirt, cat or king in it (unseen tiles by memory). */
    static MapLocation cashSpot(MapLocation c) throws GameActionException {
        if (cashClear(c)) return c;
        for (Direction d : G.fan(c, G.facing)) {
            MapLocation s = c.add(d);
            if (cashClear(s)) return s;
        }
        return null;
    }

    /** The 3x3 at s is on the map with no wall, dirt, cat or king in it (unseen tiles by memory). */
    static boolean cashClear(MapLocation s) throws GameActionException {
        if (s.x < 1 || s.y < 1 || s.x > G.W - 2 || s.y > G.H - 2) return false;
        RobotController rc = G.rc;
        int[] mem = Sym.mem;
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation t = s.translate(dx, dy);
                if (rc.canSenseLocation(t)) {
                    if (!rc.senseMapInfo(t).isPassable()) return false;
                    RobotInfo r = rc.senseRobotAtLocation(t);
                    if (r != null && r.type != UnitType.BABY_RAT) return false;
                } else if ((mem[t.x + t.y * G.W] & (Sym.WALL | Sym.DIRT | Sym.KING)) != 0) return false;
            }
        return true;
    }

    /** Rescue formation: walk into the rescue site's 3x3 and hold (the rat on the centre promotes before moving). */
    static boolean cashMove() throws GameActionException {
        MapLocation site = cashSite;
        if (G.cheb(G.me, site) > Cfg.CASH_PULL) return false;
        RobotController rc = G.rc;
        if (G.me.equals(site)) return true;
        if (G.cheb(G.me, site) <= 1) {
            // step onto the centre when it is free, else hold our ring tile
            Direction d = G.me.directionTo(site);
            if (rc.canMove(d)) {
                if (rc.getDirection() != d && rc.canTurn(d)) { rc.turn(d); G.facing = d; }
                if (rc.canMove(d)) { rc.move(d); G.me = rc.getLocation(); }
            }
            // promote on the turn we step onto the centre (the site may expire before the next)
            if (G.me.equals(site) && rc.canBecomeRatKing()) { rc.becomeRatKing(); formed++; }
            return true;
        }
        Nav.goTo(site);
        return true;
    }

    /** l is closer to our king than to its mirror under the first live symmetry. */
    static boolean homeHalf(MapLocation l) {
        return nearer(l, home, firstSym());
    }

    /** Whether l is nearer h than h's twin under symmetry s (ties count). */
    static boolean nearer(MapLocation l, MapLocation h, int s) {
        return l.distanceSquaredTo(h) <= l.distanceSquaredTo(G.mirror(h, s));
    }

    /** A king start squeaked this round (Comms.SQ_START), or null; sets rrSlot from its spawn-order field. */
    static MapLocation heardStart(Message[] ms) {
        // only this round's, from the nearest sender (squeaks stay readable for rounds, and an older newborn's gave two
        // scouts one slot). A newborn spawned in round R first acts in R + 1 (spawn order), so its own SQ_START (round
        // R) is never this round's: what it hears is the next newborn's, when its king spawned again in R + 1 (it
        // takes that slot), else nothing (-1). The opening was tuned on this behaviour.
        Message best = null;
        int bd = Integer.MAX_VALUE;
        for (Message m : ms) {
            int c = m.getBytes();
            if (Comms.sqType(c) != Comms.SQ_START || m.getRound() != G.round) continue;
            int d = m.getSource().distanceSquaredTo(G.me);
            if (d < bd) { bd = d; best = m; }
        }
        if (best == null) return null;
        int c = best.getBytes();
        rrSlot = Comms.sqExtra(c) - 1;
        return new MapLocation(Comms.sqX(c), Comms.sqY(c));
    }

    /** A place in the role's spawn order from a SQ_START heard on our first turn (-1: not heard; not our own: see
     *  heardStart); born() takes the scouts from it. */
    static int rrSlot = -1;

    /** The first symmetry still live, in the order ROT, HOR, VER. */
    static int firstSym() {
        return (Sym.mask & Sym.ROT) != 0 ? Sym.ROT : (Sym.mask & Sym.HOR) != 0 ? Sym.HOR : Sym.VER;
    }
    /** Cfg.MINE_STALL_CAT: this baby takes the END_CAT errand until this round. */
    static int stallCatUntil = -1;

    /** The last round we were at our mine (wandering around it), and that mine. */
    static int wanderRound = -10;
    static MapLocation wanderMine;

    /** Walk to random tiles (in our side frame) within WANDER_R of mine m, drawing a new one on arrival, on a stall
     *  or when the old one is out of range. */
    static void wanderAround(MapLocation m) throws GameActionException {
        // Cfg.WANDER_R: the draw stays inside atMine's ring (Chebyshev 2), else a ring-3 tile flips W/M every turn
        int r = Cfg.WANDER_R > 0 ? Cfg.WANDER_R : 3, span = 2 * r + 1;
        if (exploreTarget == null || G.me.equals(exploreTarget) || G.cheb(exploreTarget, m) > r || Nav.stall > 6) {
            exploreTarget = new MapLocation(clampX(m.x + fu * (G.rand(span) - r)), clampY(m.y + fv * (G.rand(span) - r)));
            // a tile under one of our kings can never be reached: draw again (twice at most)
            for (int k = 0; k < 2 && Nav.atKing(exploreTarget); k++)
                exploreTarget = new MapLocation(clampX(m.x + fu * (G.rand(span) - r)), clampY(m.y + fv * (G.rand(span) - r)));
        }
        Nav.goTo(exploreTarget);
    }

    /** At mine m: within Chebyshev 2, or within kingR (when > 0) if one of our kings sits within 2 of m. */
    static boolean atMine(MapLocation m, int kingR) {
        int d = G.cheb(G.me, m);
        if (d <= 2) return true;
        if (kingR <= 2 || d > kingR) return false;
        for (int k = 0; k < Comms.numKings; k++) if (G.cheb(Comms.kings[k], m) <= 2) return true;
        return false;
    }

    /** Cfg.BREAKOUT_TTL is on and this rat is breaking out. */
    static boolean breakingOut() { return Cfg.BREAKOUT_TTL > 0 && G.round < breakoutUntil; }

    /** A chase of walled-off cheese given up in round r: the second within 100 rounds of the last starts a breakout. */
    static void wallStrike(int r, int ttl) {
        wallStrikes = r - lastStrike > 100 ? 1 : wallStrikes + 1;
        lastStrike = r;
        if (wallStrikes >= 2) breakoutUntil = r + ttl;
    }

    /** Breakout exploring: follow a frontier path (to the never-seen tile nearest by path cost, digging dirt on the
     *  way), holding still while its forward search runs. False when none is found (nothing unseen is reachable). */
    static boolean exploreFrontier() throws GameActionException {
        if (!Flow.frontierLive()) Flow.requestFrontier();
        if (!Flow.frontierLive()) return false;
        if (Flow.building) return true;
        exploreTarget = Flow.tgt;
        Nav.goTo(exploreTarget);
        return true;
    }

    /** The scout's walk (tile indices, walk[wpos] where it stands), the Flow.done last taken into it,
     *  and the turns a robot has held its next tile. */
    static int[] walk = new int[256];
    static int wlen, wpos, pipeDone, pipeHeld;
    /** Cfg.PIPE_HELD: the tile a robot held on the walk (Sym.HELD set; -1 none) and the round the hold ends. */
    static int pipeHeldAt = -1, pipeHeldUntil;
    /** Cfg.PIPE_WAIT: turns held at the walk's end for the search so far, and the round the pipe rests until. */
    static int pipeWait, pipeRestUntil;

    /** Scout movement: walk the frontier paths found so far, each finished one (grown from the walk's
     *  end) appended, a search running from the end while PIPE_AHEAD or fewer steps are left. False with no walk left
     *  and no search running (the last found nothing unseen). */
    static boolean pipeFrontier() throws GameActionException {
        Flow.frontierLive(); // a search past FLOW_REV_TURNS gives up
        if (G.round < pipeRestUntil) return false;
        RobotController rc = G.rc;
        int W = G.W, me = G.idx(G.me);
        if (Flow.frontier && !Flow.building && Flow.builtRound >= 0 && Flow.done != pipeDone) {
            pipeDone = Flow.done;
            int[] p = Flow.path;
            int n = Flow.plen, k = 0, r = p[0];
            Flow.drop();
            if (wpos < wlen - 1 && walk[wlen - 1] == r) for (int i = wpos; i < wlen - 1; i++) walk[k++] = walk[i];
            else if (Math.abs(r % W - G.me.x) > 1 || Math.abs(r / W - G.me.y) > 1) n = 0; // grown from where we no longer are
            if (n > 0) {
                for (int i = 0; i < n && k < walk.length; i++) walk[k++] = p[i];
                wlen = k; wpos = 0;
            }
        }
        // where we stand on the walk: on a tile of it, or beside the farthest of the next few (moved off it)
        int nxi = -1;
        if (wpos < wlen) {
            if (walk[wpos] == me) nxi = wpos + 1;
            else {
                for (int j = wpos, hi = Math.min(wlen, wpos + 12); j < hi; j++) {
                    int t = walk[j];
                    if (t == me) nxi = j + 1;
                    else if (Math.abs(t % W - G.me.x) <= 1 && Math.abs(t / W - G.me.y) <= 1) nxi = j;
                }
                if (nxi < 0) wlen = wpos = 0;
                else wpos = Math.max(0, nxi - 1); // beside walk[0]: step onto it
            }
        }
        if (nxi >= 0 && nxi < wlen) {
            int nx = walk[nxi], m = Sym.mem[nx];
            MapLocation n = new MapLocation(nx % W, nx / W);
            if ((m & Sym.WALL) != 0) { wlen = wpos = 0; nxi = -1; }
            else if ((m & Sym.DIRT) != 0) {
                if (Nav.digOk() && rc.isActionReady()) {
                    Direction d = G.me.directionTo(n);
                    if (rc.getDirection() != d && rc.canTurn(d)) { rc.turn(d); G.facing = d; }
                    if (rc.canRemoveDirt(n)) { rc.removeDirt(n); Nav.digs++; Sym.mem[nx] &= ~Sym.DIRT; }
                }
            } else if (rc.isMovementReady()) {
                Direction d = G.me.directionTo(n);
                if (Nav.free(d)) { pipeHeld = 0; if (Nav.step(d)) wpos = nxi; }
                else if (++pipeHeld > Cfg.FLOW_HOLD) {
                    // a robot on the path: step to a free tile beside the one after it, else give the walk up
                    pipeHeld = 0;
                    int a = nxi + 1 < wlen ? walk[nxi + 1] : nx, ax = a % W, ay = a / W;
                    Direction s = null;
                    for (Direction e : G.DIRS) {
                        MapLocation l = G.me.add(e);
                        if (Math.abs(l.x - ax) <= 1 && Math.abs(l.y - ay) <= 1 && !l.equals(n) && Nav.free(e)) { s = e; break; }
                    }
                    if (s != null) Nav.step(s);
                    else {
                        // Cfg.PIPE_HELD: the next searches go around the held tile a while (two scouts holding each
                        // other's next tile otherwise re-found the same paths and stood still for 100 rounds)
                        if (pipeHeldAt >= 0) Sym.mem[pipeHeldAt] &= ~Sym.HELD;
                        Sym.mem[nx] |= Sym.HELD;
                        pipeHeldAt = nx; pipeHeldUntil = G.round + Cfg.PIPE_HELD;
                        wlen = wpos = 0; nxi = -1;
                    }
                }
            }
        }
        // keep a search growing from the walk's end (here, with none) once few steps are left
        int want = wlen > 0 ? walk[wlen - 1] : G.idx(G.me);
        boolean running = Flow.building && Flow.frontier && Flow.sRoot == want;
        if (!running && (wlen == 0 || wlen - 1 - wpos <= Cfg.PIPE_AHEAD) && G.round - Flow.frontierFail >= Cfg.FLOW_RETRY) {
            Flow.nextRoot = want;
            Flow.start(G.me, true);
            if (pipeHeldAt >= 0) {
                if (G.round <= pipeHeldUntil) Flow.block |= Sym.HELD;
                else { Sym.mem[pipeHeldAt] &= ~Sym.HELD; pipeHeldAt = -1; }
            }
            running = true;
        }
        if (nxi >= 0 && nxi < wlen) { pipeWait = 0; return true; }
        if (!running) return false;
        // Cfg.PIPE_WAIT: held this long at the walk's end for a search, drop it and walk to sampled never-seen tiles of
        // the sector (exploreHome) for PIPE_REST rounds
        if (Cfg.PIPE_WAIT > 0 && ++pipeWait > Cfg.PIPE_WAIT) {
            pipeWait = 0;
            Flow.building = false;
            pipeRestUntil = G.round + Cfg.PIPE_REST;
            return false;
        }
        return true;
    }

    /** Cfg.SCOUT_FAN: sectors in fan order (Flow.fanOk's 0 ahead, 1 left, -1 right), the scouts' turns through them
     *  (each starts at its slot's), and the last failed frontier search a turn was taken for. */
    static final int[] FAN = {0, 1, -1};

    static int fanStep, fanFailSeen = -1000;
    /** Cfg.SCOUT_FAN: the scout's place among the king's scouts (from rrSlot), -1 when not heard (the slot decides). */
    static int fanBase = -1;
    /** Cfg.SCOUT_FAN: the first sector of the fanBase-th scout: both flanks, then ahead. */
    static final int[] FAN_ORDER = {1, 2, 0};

    /** Cfg.SCOUT_FAN: this scout's sector of home's fan. */
    static int fanSec() { return FAN[((fanBase >= 0 ? FAN_ORDER[fanBase % 3] : slot >>> 6) + fanStep) % 3]; }

    /** Cfg.SCOUT_FAN scout movement: Flow's frontier search, ending only at a never-seen tile of our half in this
     *  scout's sector of home's fan (from the first mine on as before it); a search that finds none moves the scout
     *  on to the next sector, and false this turn. */
    static boolean fanFrontier() throws GameActionException {
        MapLocation h = home;
        Flow.fanFrame(h, G.mirror(h, firstSym()));
        Flow.fanReq = fanSec();
        boolean ok = pipeFrontier();
        Flow.fanReq = Flow.NOFAN;
        if (!ok && Flow.frontierFail > fanFailSeen) { fanFailSeen = Flow.frontierFail; fanStep++; Flow.frontierFail = -1000; }
        return ok;
    }

    /** Walk to the nearest never-seen tile of EXPLORE_TRIES samples (in our side frame), or with no mine known by
     *  EXPLORE_CENTER_FROM the one nearest the map centre; breaking out, a frontier path first. */
    static void explore() throws GameActionException {
        if (exploreTarget != null && Flow.failTgt != null && G.cheb(Flow.failTgt, exploreTarget) <= 1
                && G.round - Flow.failRound < Cfg.FLOW_RETRY) breakoutUntil = G.round + Cfg.BREAKOUT_TTL;
        if (breakingOut() && exploreFrontier()) return;
        if (exploreTarget == null || G.cheb(G.me, exploreTarget) <= 2 || Nav.stall > 20) {
            // samples in our side frame, so mirrored rats pick mirrored targets. Once open ground is mapped, what is left
            // unseen is buried in dirt, and Flow digs to it. Mines nobody has seen are buried between the kings, and
            // shallow edge pockets do not reveal them: hence the centre ranking.
            boolean centre = Comms.numMines == 0 && G.round >= Cfg.EXPLORE_CENTER_FROM;
            int ox = centre ? (G.W - 1) / 2 : G.me.x, oy = centre ? (G.H - 1) / 2 : G.me.y;
            int x = 0, y = 0, bd = Integer.MAX_VALUE;
            for (int k = 0; k < Cfg.EXPLORE_TRIES; k++) {
                int ax = G.rand(G.W), ay = G.rand(G.H);
                int cx = fu > 0 ? ax : G.W - 1 - ax, cy = fv > 0 ? ay : G.H - 1 - ay;
                int d = Sym.known(G.idx(cx, cy)) ? 1 << 20 : Math.max(Math.abs(cx - ox), Math.abs(cy - oy));
                if (d < bd) { bd = d; x = cx; y = cy; }
            }
            exploreTarget = new MapLocation(x, y);
        }
        Nav.goTo(exploreTarget);
    }

    /** This gatherer scouts (born()). */
    static boolean scout;
    /** The most home mines this rat has known, and the round that count last grew (Cfg.SCOUT_DRY). */
    static int scoutMines = -1, scoutFound;

    /** A gatherer scout still scouting: until scoutUntil, or with Cfg.SCOUT_MINES while we know too few home mines. */
    static boolean scouting() {
        if (!scout || role != GATHER) return false;
        int until = scoutUntil(Cfg.SCOUT_AREA);
        if (G.round < until) return true;
        if (!moreScouting(Cfg.SCOUT_MINES, G.round)) return false;
        int h = homeMineCount();
        if (scoutMines < 0) { scoutMines = h; scoutFound = until; }
        else if (h > scoutMines) { scoutMines = h; scoutFound = G.round; }
        return h < Cfg.SCOUT_MINES && !scoutDry(Cfg.SCOUT_DRY, G.round, scoutFound);
    }

    /** Cfg.SCOUT_MINES is on and round r is before SCOUT_MINES_UNTIL. */
    static boolean moreScouting(int mines, int r) { return mines > 0 && r < Cfg.SCOUT_MINES_UNTIL; }

    /** No new home mine for dry rounds since round found (dry 0: never dry). */
    static boolean scoutDry(int dry, int r, int found) { return dry > 0 && r - found >= dry; }

    /** The round scouting ends: SCOUT_UNTIL, or W*H/area on a map big enough to make that later (area 0: off). */
    static int scoutUntil(int area) { return area > 0 ? Math.max(Cfg.SCOUT_UNTIL, G.W * G.H / area) : Cfg.SCOUT_UNTIL; }

    /** exploreHome's sample rank (lower wins) for a sample at Chebyshev distance d: Cfg.HOME_PICK 2 prefers far. */
    static int homeRank(int pick, int d) { return pick == 2 ? -d : d; }

    /** Scout movement: the never-seen tile of our half that Cfg.HOME_PICK takes from EXPLORE_TRIES samples; false
     *  (no move) when no sample is one. */
    static boolean exploreHome() throws GameActionException {
        if (exploreTarget == null || G.cheb(G.me, exploreTarget) <= 2 || Nav.stall > 20 || Sym.known(G.idx(exploreTarget.x, exploreTarget.y))) {
            int x = -1, y = -1, bd = Integer.MAX_VALUE;
            for (int k = 0; k < Cfg.EXPLORE_TRIES; k++) {
                int ax = G.rand(G.W), ay = G.rand(G.H);
                int cx = fu > 0 ? ax : G.W - 1 - ax, cy = fv > 0 ? ay : G.H - 1 - ay;
                if (Sym.known(G.idx(cx, cy)) || !homeHalf(new MapLocation(cx, cy))) continue;
                if (scout && !Flow.fanOk(cx, cy, fanSec())) continue;
                int d = homeRank(Cfg.HOME_PICK, Math.max(Math.abs(cx - G.me.x), Math.abs(cy - G.me.y)));
                if (d < bd) { bd = d; x = cx; y = cy; }
                if (Cfg.HOME_PICK == 1) break;
            }
            if (x < 0) return false;
            exploreTarget = new MapLocation(x, y);
        }
        Nav.goTo(exploreTarget);
        return true;
    }

    /** Mine seeking (Cfg.SEEK_CROWD) runs until round seekUntil and may start again from seekNext; seekMines is
     *  myMineCount when it started. */
    static int seekUntil, seekNext, seekMines;
    /** This seek found a mine the king lacked (and carries it home). */
    static boolean seekNews;

    /** Cfg.SEEK_IDLE: turns spent near the mine since the last pickup (idlePicked: Baby.picked then); mineShift is
     *  the offset into the pick list. */
    static int idleTurns, idlePicked, mineShift;

    /** Cfg.SEEK_IDLE: count a turn within SEEK_IDLE_R of the mine with no pickup since the last; true (and the count
     *  restarts) on the SEEK_IDLE-th. */
    static boolean idle(MapLocation mine) {
        if (picked != idlePicked) { idlePicked = picked; idleTurns = 0; }
        if (G.cheb(G.me, mine) > Cfg.SEEK_IDLE_R || ++idleTurns < Cfg.SEEK_IDLE) return false;
        idleTurns = 0;
        return true;
    }

    /** Cfg.SEEK_IDLE: an idle gatherer starts seeking unless it sought within SEEK_RETRY rounds. */
    static boolean idleSeek() {
        if (G.round < seekNext) return false;
        beginSeek();
        return true;
    }

    /** A gatherer at a crowded mine starts seeking: SEEK_CROWD allied babies nearer the mine than us. */
    static boolean startSeek(MapLocation mine) {
        if (Cfg.SEEK_CROWD <= 0 || G.round < seekNext || nearerAllies(mine) < Cfg.SEEK_CROWD) return false;
        beginSeek();
        return true;
    }

    /** Start seeking for SEEK_TTL rounds (no new seek for SEEK_RETRY) from a fresh explore target. */
    private static void beginSeek() {
        seekUntil = G.round + Cfg.SEEK_TTL;
        seekNext = G.round + Cfg.SEEK_RETRY;
        seekMines = myMineCount;
        seekNews = false;
        exploreTarget = null;
    }

    /** One seeking step; false once it is over: a mine found and reported to the king, or nothing left unseen. */
    static boolean seek() throws GameActionException {
        if (myMineCount > seekMines) {
            // squeaks reach the king from dist2 16 only: carry the news home, then gather (the new mine included)
            MapLocation k = Comms.nearestKing(G.me);
            if (k != null && unreported() >= 0) { seekNews = true; Nav.goTo(k); return true; }
            if (seekNews || k == null) { mineTarget = null; seekUntil = 0; return false; }
            seekMines = myMineCount;   // only mines the king lists, no news: seek on
        }
        if (exploreHome()) return true;
        seekUntil = 0;
        return false;
    }

    /** x, y clamped onto the map. */
    static int clampX(int x) { return Math.max(0, Math.min(G.W - 1, x)); }

    static int clampY(int y) { return Math.max(0, Math.min(G.H - 1, y)); }
}
