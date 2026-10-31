package u2j;

import battlecode.common.*;

/** Baby rat: gather and deliver cheese, fight cats, lay cat traps, explore. */
public final class Baby {
    public static final int GATHER = 0, FIGHT = 1, ATTACK = 2;
    public static int role = -1;
    static int birthRound;
    /** Cfg.RUSH_LIFE: the round this attacker last saw the enemy king, and the attackers demoted to gatherers. */
    static int ekSeen = -10000, demoted;
    static MapLocation home;          // spawning king's location at birth
    static MapLocation origin;        // our first king's start, squeaked by the spawning king (Cfg.HOME_START)
    static int fu = 1, fv = 1;        // side frame (signs of our king's offset from centre)

    static MapLocation cheeseTarget;  // last seen cheese tile
    /** Cheese given up after a stall (Cfg.CHEESE_BAN): a ring of the latest, with the round each was banned. */
    static final MapLocation[] banned = new MapLocation[4];
    static final int[] bannedAt = new int[4];
    static int banHead;
    /** Cfg.BREAKOUT_TTL: breaking out (no chase of walled-off cheese; explore by frontier search) until this round;
     *  wallStrikes counts chases of walled-off cheese given up on a stall, the last in round lastStrike. */
    static int breakoutUntil = -1, wallStrikes, lastStrike = -1000;
    static MapLocation mineTarget;    // sticky assigned mine
    static MapLocation exploreTarget;
    static MapLocation catSeen;       // bottom-left of nearest cat sensed this turn
    static RobotInfo cat;
    static int catSeenRound = -100;
    static MapLocation lastCatTarget;

    // mines this rat has sensed (to report to kings)
    static int[] myMines = new int[24];
    static int lowBc;   // turns cut short for lack of bytecodes
    static int myMineCount;
    static int reportedSym = 7;

    public static int delivered, bitesCat, bitesRat, trapsPlaced, picked;

    public static void turn() throws GameActionException {
        RobotController rc = G.rc;
        if (role < 0) born();
        if (Cfg.WAR_STRIKE > 0 && !G.coop && striker(role, birthRound, Cfg.WAR_STRIKE)) role = ATTACK;
        if (Cfg.TRAIL) Trail.record();
        Comms.readKings();
        if (Cfg.KING_BLOCK || Cfg.KING_FLOW) Nav.markKings();
        Comms.readMines();
        int s = Comms.read(Comms.SYM);
        if (s != 0) Sym.merge(s);

        Debug.mark(1);
        sense();
        if (Cfg.SYM_MINELIST && Flow.left() > Cfg.BC_OPTIONAL) Sym.listCheck(Comms.mines, Comms.numMines, false);
        RobotInfo[] robots = rc.senseNearbyRobots(-1, null);
        seen = robots;
        RobotInfo enemyKing = null, enemy = null;
        cat = null;
        int bestCat = Integer.MAX_VALUE;
        for (RobotInfo r : robots) {
            if (r.team == Team.NEUTRAL) {
                if (Cfg.CAT_LAY && G.coop) CatLane.see(r.location, G.round);
                int d = catDist4(r.location, G.me);
                if (d < bestCat) { bestCat = d; cat = r; }
            } else if (r.team == G.them) {
                if (r.type == UnitType.RAT_KING) enemyKing = r;
                else if (enemy == null || r.health < enemy.health) enemy = r;
            }
        }
        if (cat != null) { catSeen = cat.location; catSeenRound = G.round; }
        if (enemy != null) threatRound = G.round;

        Debug.mark(2);
        if (role != ATTACK) pickUp();
        Nav.avoid = cat != null ? laneMask(cat) : 0;
        if (Cfg.SWAP_YIELD && Nav.swapHold > 0 && rc.getCarrying() == null) Nav.avoid |= carrierMask(robots);
        // a newborn's first turn pays for array allocation and a full first sense: skip optional work when short
        if (!G.coop && Cfg.TRAP_RISK && Flow.left() > Cfg.BC_OPTIONAL) {
            TrapRisk.observe(robots);
        }

        Debug.mark(3);
        eKing = enemyKing;
        if (Cfg.RUSH_LIFE > 0 && role == ATTACK) {
            if (enemyKing != null) ekSeen = G.round;
            // a rush that has not found the enemy king in RUSH_LIFE rounds has failed for this rat: it gathers
            else if (rushFailed(G.round, birthRound, ekSeen, Cfg.RUSH_LIFE)) { role = GATHER; demoted++; }
        }
        if (Cfg.RUSH_BURN > 0 && role == ATTACK && trapsKnown(burnRound, Comms.read(Comms.SYM))
                && rushStands(G.me, ref(), Cfg.RUSH_BURN)) { role = GATHER; demoted++; }
        if (enemyKing != null && Cfg.KING_PRESS) { lastEKing = enemyKing.location; lastEKingRound = G.round; ekHops = 0; }
        nearestEnemy = null;
        hunting = false;
        if (!G.coop) {
            int bd = Integer.MAX_VALUE;
            for (RobotInfo r : robots) {
                if (r.team != G.them || r.type == UnitType.RAT_KING) continue;
                int d = G.me.distanceSquaredTo(r.location);
                if (d < bd) { bd = d; nearestEnemy = r; }
            }
            // fighters close on a rat they can grab (lower health than ours: trapped or bitten) before anything else
            if (Cfg.POUNCE && rc.getCarrying() == null) {
                RobotInfo p = prey(robots, rc.getHealth());
                // gatherers only grab prey that is close; fighters hunt it across their vision
                if (p != null && (role != GATHER || G.me.distanceSquaredTo(p.location) <= 8)) {
                    nearestEnemy = p;
                    hunting = true;
                }
            }
            guardKing = null;
            if (Cfg.GUARD_KING_R > 0 && rc.getCarrying() == null) {
                RobotInfo b = kingBiter(robots, Cfg.GUARD_KING_R);
                if (b != null) { nearestEnemy = b; hunting = true; guards++; }
            }
            rescueT = Cfg.RESCUE && rc.getCarrying() == null ? rescueTarget(robots) : null;
        }
        Debug.mark(30);
        if (Flow.left() < Cfg.BC_BAIL) { lowBc++; return; }
        Nav.guard = null;
        if (!G.coop && Cfg.GUARD_DIST2 > 0 && nearestEnemy != null && rc.getCarrying() == null
                && G.me.distanceSquaredTo(nearestEnemy.location) <= Cfg.GUARD_DIST2) Nav.guard = nearestEnemy.location;
        boolean engaged = nearestEnemy != null && G.me.distanceSquaredTo(nearestEnemy.location) <= 20;
        if (Cfg.RISK_ADAPT > 0) adaptRisk = false;
        if (!G.coop && Cfg.TRAP_RISK && Nav.stall < 8) {
            if (!(hunting && Cfg.HUNT_IGNORES_RISK) && (Cfg.RISK_IN_FIGHT || !engaged || Cfg.RISK_GATHER && role == GATHER))
                Nav.avoid |= TrapRisk.mask(G.me);
            else if (Cfg.RISK_FRESH > 0) Nav.avoid |= TrapRisk.mask(G.me, Cfg.RISK_FRESH);
            else if (Cfg.RISK_ADAPT > 0 && (adaptRisk = trapsKnown(burnRound, Comms.read(Comms.SYM))))
                Nav.avoid |= TrapRisk.mask(G.me, Cfg.RISK_ADAPT);
        }
        if (Cfg.WOUNDED_HP > 0 && !G.coop && rc.getHealth() <= Cfg.WOUNDED_HP && trapsKnown(burnRound, Comms.read(Comms.SYM))) {
            Nav.avoid |= woundMask(G.me, robots);
            wounded++;
        }
        Debug.mark(31);
        // 0. a carried enemy is thrown as soon as we can act
        if (rc.getCarrying() != null) { if (rc.getCarrying().team == G.us) dropAlly(); else tryThrow(robots); }
        // 1. bites: enemy rats once cooperation is over (they shoot back; cats wait), then cats.
        //    Attackers open the war themselves, but only on the enemy king.
        Debug.mark(4);
        boolean hostile = hostile();
        if (hostile && Cfg.NAP) tryCarry(robots);
        // a rescue one step away keeps the action for the grab after the move
        boolean saving = rescueT != null && rc.isMovementReady() && rc.isActionReady();
        if (Cfg.GRAB_MICRO && hostile && !saving && GrabMicro.act(robots)) saving = true;
        if (!G.coop && Cfg.BABY_RAT_TRAPS && !saving) tryRatTrap();
        Debug.mark(5);
        if (hostile && !saving) biteEnemies(robots);
        else if (role == ATTACK && enemyKing != null) biteEnemies(new RobotInfo[]{enemyKing});
        if (cat != null) tryBiteCat(cat);
        // 2. cat trap ahead of an approaching cat
        if (cat != null && Cfg.CAT_TRAPS) tryCatTrap(cat);

        Debug.mark(6);
        report();

        // 3. movement
        Debug.mark(7);
        MapLocation before = rc.getLocation();
        if (rc.isMovementReady()) moveDecision();
        boolean moved = !rc.getLocation().equals(before);
        if (rc.getType() == UnitType.RAT_KING) return;   // just promoted: the king code runs from next turn
        Debug.mark(20);
        pickUp();
        if (rc.getCarrying() != null && moved) {
            if (rc.getCarrying().team == G.us) dropAlly();
            else if (Clock.getBytecodesLeft() > Cfg.THROW_BC) tryThrow(rc.senseNearbyRobots(-1, null));
        }
        Debug.mark(21);
        if (hostile && Cfg.NAP) tryCarry(rc.senseNearbyRobots(2, G.them));
        Debug.mark(22);
        if (Cfg.GRAB_MICRO && GrabMicro.round == G.round) { }
        else if (hostile) biteEnemies(rc.senseNearbyRobots(2, G.them));
        else if (role == ATTACK) {
            RobotInfo[] near = rc.senseNearbyRobots(8, G.them);
            for (RobotInfo r : near) if (r.type == UnitType.RAT_KING) biteEnemies(new RobotInfo[]{r});
        }
        if (cat != null) tryBiteCat(cat);
        if (Cfg.CAT_LAY && G.coop) layLane();
        Debug.mark(8);
        tryDeliver();
        guardTurn();
        if (Cfg.MOAT && !G.coop) Moat.baby(nearestEnemy != null ? nearestEnemy.location : null);
        Debug.mark(9);
        sense2();
    }

    static int threatRound = -1000, glances;

    /** Whether a turn with no enemy known near should end facing back: an enemy was seen or heard within mem rounds. */
    static boolean glanceDue(int round, int threat, int mem) { return mem > 0 && round - threat <= mem; }

    /** Face the opposite way with the turn this turn left unused (the next step turns back at no movement cost). */
    static void glance() throws GameActionException {
        Direction back = G.rc.getDirection().opposite();
        if (back != Direction.CENTER && G.rc.canTurn(back)) { G.rc.turn(back); G.facing = back; glances++; }
    }

    /** Cfg.RISK_ADAPT: health at the start of our turn; the round an enemy trap fired on us or we heard of one (0 = never);
     *  our last SQ_BURN; whether this turn's fresh mask is on. */
    static int hpStart, burnRound, burnSqueaked;
    static boolean adaptRisk;

    /** Cfg.RISK_ADAPT: enemy rat traps are known to be in play (our own burn or a heard one, or the kings' shared bit). */
    static boolean trapsKnown(int burn, int sym) { return burn > 0 || (sym & Comms.SYM_BURN) != 0; }

    /** Cfg.RUSH_BURN: a rusher at me farther than Chebyshev r from the enemy start (our start home mirrored by the
     *  first live symmetry) stands down once enemy traps are known. */
    static boolean rushStands(MapLocation me, MapLocation home, int r) {
        return home != null && G.cheb(me, G.mirror(home, firstSym())) > r;
    }

    /** Cfg.WOUNDED_HP: turns held to the wound mask. */
    static int wounded;

    /** Cfg.WOUNDED_HP: Nav.avoid bits of the steps from me that end within Chebyshev 2 of a visible enemy baby. */
    static int woundMask(MapLocation me, RobotInfo[] robots) {
        int m = 0;
        for (RobotInfo r : robots) {
            if (r.team != G.them || r.type != UnitType.BABY_RAT) continue;
            int ex = r.location.x - me.x, ey = r.location.y - me.y;
            if (ex > 3 || ex < -3 || ey > 3 || ey < -3) continue;
            for (int i = 0; i < 8; i++) {
                int dx = ex - G.DIRS[i].dx, dy = ey - G.DIRS[i].dy;
                if (dx <= 2 && dx >= -2 && dy <= 2 && dy >= -2) m |= 1 << i;
            }
        }
        return m;
    }

    /** Whether this rat bites and grabs enemy rats now: after cooperation, or a RUSH_BREAK attacker at any time. */
    static boolean hostile() { return !G.coop || Cfg.RUSH_BREAK && role == ATTACK || Cfg.BREAK_ROUND > 0 && G.round >= Cfg.BREAK_ROUND; }

    /** End the turn facing the nearest enemy rat close enough to grab us next round. */
    static void guardTurn() throws GameActionException {
        RobotController rc = G.rc;
        if (G.coop || Cfg.GUARD_DIST2 == 0 || !rc.isTurningReady() || rc.getCarrying() != null) return;
        RobotInfo best = null;
        int bd = Integer.MAX_VALUE;
        for (RobotInfo r : rc.senseNearbyRobots(Cfg.GUARD_DIST2, G.them)) {
            if (r.team != G.them || r.type != UnitType.BABY_RAT) continue;
            int d = G.me.distanceSquaredTo(r.location);
            if (d < bd) { bd = d; best = r; }
        }
        MapLocation t = best != null ? best.location : null;
        if (t == null && Cfg.GUARD_MEMORY) {
            // nothing in view: an enemy we saw at the start of the turn, or one an ally squeaked, may be behind us
            for (RobotInfo r : seen) {
                if (r.team != G.them || r.type != UnitType.BABY_RAT) continue;
                int d = G.me.distanceSquaredTo(r.location);
                if (d <= Cfg.GUARD_DIST2 && d < bd) { bd = d; t = r.location; }
            }
            for (int i = 0; i < TrapRisk.nHeard; i++) {
                int d = G.me.distanceSquaredTo(TrapRisk.heardE[i]);
                if (d <= 8 && d < bd) { bd = d; t = TrapRisk.heardE[i]; }
            }
        }
        if (t == null) t = Nav.guard;
        if (TrapRisk.nHeard > 0) threatRound = G.round;
        if (t == null) { if (glanceDue(G.round, threatRound, Cfg.GLANCE_MEM)) glance(); return; }
        Direction to = G.me.directionTo(t);
        if (to != Direction.CENTER && rc.getDirection() != to && rc.canTurn(to)) { rc.turn(to); G.facing = to; }
    }

    /** Cfg.RUSH_LIFE: an attacker born at birth that last saw the enemy king at seen has rushed for life rounds in vain. */
    static boolean rushFailed(int round, int birth, int seen, int life) {
        return round - birth > life && round - seen > life;
    }

    static void born() {
        RobotController rc = G.rc;
        birthRound = G.round;
        int cost = rc.getCurrentRatCost();
        int idx = (cost - 10) / 10 * 4; // approximate spawn index from the cost step
        role = GATHER;
        if (idx >= Cfg.GATHER_FIRST && (G.round % Cfg.FIGHT_EVERY) == 0) role = FIGHT;
        raider = role == FIGHT && Cfg.RAID_EVERY > 0 && (G.round / Cfg.FIGHT_EVERY) % Cfg.RAID_EVERY == 0;
        if (rusher(idx, G.round)) role = ATTACK;
        Comms.readKings();
        home = Comms.nearestKing(G.me);
        if (home == null) home = G.me;
        if (Cfg.HOME_START || Cfg.MINE_RR || Cfg.FORM_MINE) origin = heardStart(rc.readSqueaks(-1));
        int px = 2 * home.x - G.cx2, py = 2 * home.y - G.cy2;
        fu = px >= 0 ? 1 : -1;
        fv = py >= 0 ? 1 : -1;
        Flow.frame(fu, fv);
        G.frame(home);
        // mirror-invariant seed: round and the unsigned offset from the king
        int seed = birthSeed(G.round, G.me, home);
        G.seed(seed);
        slot = ((Cfg.SYM_SLOT ? seed : rc.getID()) * 0x9E3779B1) >>> 8;
        scout = Cfg.SCOUT_EVERY > 0 && (slot >>> 4) % Cfg.SCOUT_EVERY == 0;
    }

    /** Cfg.RUSH: a baby born with idx live babies in round r attacks the enemy king. */
    /** The role born() gives a rat with idx live babies in round r. */
    static int roleFor(int idx, int r) {
        if (rusher(idx, r)) return ATTACK;
        return idx >= Cfg.GATHER_FIRST && (r % Cfg.FIGHT_EVERY) == 0 ? FIGHT : GATHER;
    }

    static boolean rusher(int idx, int r) {
        return Cfg.RUSH && idx >= Cfg.RUSH_FROM && idx < Cfg.RUSH_TO && (Cfg.RUSH_UNTIL == 0 || r < Cfg.RUSH_UNTIL);
    }

    /** A rat's birth seed: its round and unsigned offset from the king that spawned it, the same for its mirror twin. */
    static int birthSeed(int round, MapLocation me, MapLocation home) {
        return round * 977 + Math.abs(me.x - home.x) * 31 + Math.abs(me.y - home.y) * 7 + 1;
    }

    /** Cfg.WAR_STRIKE k: whether a rat of this role born in this round is one of the 1 in k fighters that strike at
     *  the enemy king once at war (k 0: none). */
    static boolean striker(int role, int birth, int k) {
        return k > 0 && role == FIGHT && (birth / Cfg.FIGHT_EVERY) % k == 0;
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
    /** Cfg.SWAP_YIELD: the directions whose tile is next to an allied baby carrying an ally (its drop tile). */
    static int carrierMask(RobotInfo[] robots) {
        int mask = 0;
        for (RobotInfo r : robots) {
            if (r.team != G.us || r.carryingRobot == null || r.carryingRobot.team != G.us) continue;
            for (int i = 0; i < 8; i++) if (G.cheb(G.me.add(G.DIRS[i]), r.location) <= 1) mask |= 1 << i;
        }
        return mask;
    }

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

    /** Cfg.CHEESE_LOS: the piles seen this sense, judged once every wall in view is recorded. */
    static final MapLocation[] piles = new MapLocation[24];

    static void sense() throws GameActionException {
        MapInfo[] infos = G.rc.senseNearbyMapInfos();
        MapLocation bestCheese = null;
        int bd = Integer.MAX_VALUE, np = 0;
        boolean los = Cfg.CHEESE_LOS || breakingOut() || Cfg.SCOUT_FRONTIER && scouting();
        for (MapInfo mi : infos) {
            MapLocation l = mi.getMapLocation();
            boolean mine = mi.hasCheeseMine();
            if (Sym.record(l.x, l.y, mi.isWall(), mi.isDirt(), mine)) { }
            if (mine) noteMine(l.x, l.y);
            if (mi.getCheeseAmount() > 0) {
                if (los) { if (np < piles.length) piles[np++] = l; continue; }
                int d = G.me.distanceSquaredTo(l);
                if (d < bd && (Cfg.CHEESE_BAN == 0 || !bannedCheese(l, Cfg.CHEESE_BAN))) { bd = d; bestCheese = l; }
            } else if (cheeseTarget != null && cheeseTarget.equals(l)) {
                cheeseTarget = null;
            }
        }
        if (los) {
            for (int i = 0; i < np; i++) {
                MapLocation l = piles[i];
                int d = G.me.distanceSquaredTo(l);
                if (d < bd && (Cfg.CHEESE_BAN == 0 || !bannedCheese(l, Cfg.CHEESE_BAN)) && !walledOff(G.me, l)) { bd = d; bestCheese = l; }
            }
            if (cheeseTarget != null && walledOff(G.me, cheeseTarget)) cheeseTarget = null;
        }
        if (bestCheese != null) {
            if (cheeseTarget == null || G.me.distanceSquaredTo(cheeseTarget) > bd) cheeseTarget = bestCheese;
        }
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

    /** Give up on cheese at l for Cfg.CHEESE_BAN rounds, its Chebyshev-2 neighbourhood with it. */
    static void banCheese(MapLocation l) {
        banned[banHead] = l;
        bannedAt[banHead] = G.round;
        banHead = (banHead + 1) & 3;
    }

    /** Cheese at l lies within Chebyshev 2 of a pile banned less than ttl rounds ago. */
    static boolean bannedCheese(MapLocation l, int ttl) {
        for (int i = 0; i < 4; i++) {
            MapLocation b = banned[i];
            if (b != null && G.round - bannedAt[i] < ttl && G.cheb(b, l) <= 2) return true;
        }
        return false;
    }

    /** Re-sense after moving (new cone) only to refresh cheese; terrain is caught next turn. */
    static void sense2() throws GameActionException {
        if (Clock.getBytecodesLeft() < Cfg.SENSE2_LEFT) return;
        sense();
    }

    /** A mine we know that the king's list lacks (rotating through them by round), else -1. */
    static int unreported() {
        for (int j = 0; j < myMineCount; j++) {
            int key = myMines[(G.round + j) % myMineCount];
            if (!Comms.hasMine(key / 64, key % 64)) return key;
        }
        return -1;
    }

    static void noteMine(int x, int y) {
        int key = x * 64 + y;
        for (int i = 0; i < myMineCount; i++) if (myMines[i] == key) return;
        if (myMineCount < myMines.length) myMines[myMineCount++] = key;
    }

    static void pickUp() throws GameActionException {
        RobotController rc = G.rc;
        MapLocation c = rc.getLocation();
        if (canTake(c)) { rc.pickUpCheese(c); picked++; }
        Direction f = rc.getDirection();
        Direction[] fr = G.fan(c, f);
        for (Direction d : fr) {
            MapLocation l = c.add(d);
            if (canTake(l)) { rc.pickUpCheese(l); picked++; }
        }
        G.rawCheese = rc.getRawCheese();
    }

    /** A pile we may take: any when empty-handed, else only one that keeps us within CARRY_MAX (each unit carried
     *  slows moves and actions 1%: on fullofsecrets rats hauled 400-700 home at a fifth of their speed). */
    static boolean canTake(MapLocation l) throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.canPickUpCheese(l)) return false;
        if (Cfg.ATK_CARRY > 0 && role == ATTACK) return atkTake(rc.getRawCheese(), rc.senseMapInfo(l).getCheeseAmount(), Cfg.ATK_CARRY);
        if (Cfg.CARRY_MAX <= 0) return true;
        int raw = rc.getRawCheese();
        return raw == 0 || takeOk(raw, rc.senseMapInfo(l).getCheeseAmount(), Cfg.CARRY_MAX);
    }

    static boolean takeOk(int raw, int pile, int cap) { return cap <= 0 || raw == 0 || raw + pile <= cap; }

    /** Cfg.ATK_CARRY: an attacker's take, capped even when empty-handed. */
    static boolean atkTake(int raw, int pile, int cap) { return raw + pile <= cap; }

    static RobotInfo eKing, nearestEnemy;
    static RobotInfo[] seen = new RobotInfo[0];
    public static int naps, throwsDone, ratTraps, mineTraps;
    static int carryRound = -100;

    /** Grab an adjacent enemy baby rat (the engine allows it when it cannot see us or is weaker): the healthiest,
     *  or under Cfg.RESCUE first one carrying one of ours (it drops it). */
    static boolean tryCarry(RobotInfo[] robots) throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.isActionReady() || rc.getCarrying() != null) return false;
        RobotInfo best = null;
        int bs = Integer.MIN_VALUE;
        for (RobotInfo r : robots) {
            if (r.team != G.them || r.type != UnitType.BABY_RAT) continue;
            if (G.me.distanceSquaredTo(r.location) > 2) continue;
            if (!rc.canCarryRat(r.location)) continue;
            int s = r.health + (Cfg.RESCUE && holdsOurs(r) ? 1000 : 0);
            if (s > bs) { bs = s; best = r; }
        }
        if (best == null) return false;
        rc.carryRat(best.location);
        carryRound = G.round;
        naps++;
        return true;
    }

    /**
     * Throw the carried rat along the direction that hurts most: the flight (6-8 tiles) hits the first robot,
     * wall or dirt for 46/42 (first two tiles), 38/34, 30/26, 22/18; the victim takes the same damage as the robot hit.
     * Hitting our own rat costs its damage; an empty flight lands for 10.
     */
    static final RobotInfo[] grid = new RobotInfo[289];
    static final int[] gridAt = new int[289];
    /** Damage when the flight ends at tile k (2 = point-blank 42, then 38, 34, ...). */
    static final int[] FLIGHT_DMG = new int[9];
    static { for (int k = 2; k <= 8; k++) FLIGHT_DMG[k] = 10 + 4 * (2 * (4 - (k - 1) / 2) + ((k & 1) == 1 ? 1 : 0)); }
    static int gridStamp;

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

    /** Carrying an enemy (Cfg.CARRY_AIM): from our tile or a neighbour we can step to, a throw whose point-blank tile
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
            if (r.team != G.them) continue;
            int ox = r.location.x - me.x, oy = r.location.y - me.y;
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
        for (int j = 0; j < 9; j++) {
            int mx = j % 3 - 1, my = j / 3 - 1;
            boolean stay = mx == 0 && my == 0;
            if (!stay && !canMove) continue;
            Direction m = D3[j];
            if (!stay && Cfg.RISK_FRESH > 0 && (Nav.avoid & (1 << m.ordinal())) != 0) continue;
            if (Cfg.RISK_ADAPT > 0 && adaptRisk && !stay && (Nav.avoid & (1 << m.ordinal())) != 0) continue;
            boolean checked = stay;
            for (int i = 0; i < n; i++) {
                int vx = tx[i] - mx, vy = ty[i] - my;
                if (vx != 0 && vx != 2 && vx != -2 || vy != 0 && vy != 2 && vy != -2 || vx == 0 && vy == 0) continue;
                int fx = mx + vx / 2, fy = my + vy / 2, x = me.x + fx, y = me.y + fy;
                // the first flight tile: on the map, open terrain, no robot (the tile we leave is free)
                if (x < 0 || y < 0 || x >= w || y >= h || (mem[x + w * y] & blk) != 0
                        || (fx != 0 || fy != 0) && gridAt[(fx + 8) + 17 * (fy + 8)] == stamp) continue;
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
        if (rc.canThrowRat()) { rc.throwRat(); throwsDone++; aimThrows++; }
        return true;
    }

    static final int[] AIM_TX = new int[24], AIM_TY = new int[24], AIM_TV = new int[24];

    static int aimThrows;

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

    static boolean tryThrow(RobotInfo[] robots) throws GameActionException {
        if (Cfg.THROW_CHECK) return throwChecked(robots);
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
                    else s2 = -50; // a cat eats the rat: no damage, it sleeps
                    break;
                }
            }
            s2 = s2 * 2 + (G.hand(me, d) > 0 ? 1 : 0);
            if (s2 > bs) { bs = s2; bestD = d; }
        }
        if (bestD == null) return false;
        // no point-blank hit (42) yet: keep holding (the carried rat is out of the fight) while the carry lasts
        if (bs / 2 < Cfg.THROW_MIN && G.round - carryRound < Cfg.THROW_HOLD) return false;
        if (rc.getDirection() != bestD) { if (!rc.canTurn(bestD)) return false; rc.turn(bestD); G.facing = bestD; }
        if (!rc.canThrowRat()) return false;
        rc.throwRat();
        throwsDone++;
        return true;
    }

    static RobotInfo ownKing;
    public static int throwAborts;

    /** Cfg.THROW_CHECK: tryThrow, with our unseen kings (the king channel) on the flights, a hit on our king scored
     *  three times a rat's, no throw whose best flight hits one of ours until THROW_LATE rounds into the carry, and
     *  the flight re-checked once the turn brings it into view. Field games (3934-3937, 3986-3992) threw into our own
     *  robots 0-17 times a game, up to 370 damage and 4 hits on our own king. */
    static boolean throwChecked(RobotInfo[] robots) throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.isActionReady()) return false;
        MapLocation me = rc.getLocation();
        if (robots == null) robots = rc.senseNearbyRobots(-1, null);
        int stamp = gridFill(me, robots);
        // our kings we cannot see stand where the king channel last put them
        for (int q = 0; q < Comms.numKings; q++) {
            MapLocation k = Comms.kings[q];
            int ox = k.x - me.x, oy = k.y - me.y;
            if (ox < -9 || ox > 9 || oy < -9 || oy > 9) continue;
            if (ownKing == null) ownKing = new RobotInfo(0, G.us, UnitType.RAT_KING, 600, k, Direction.NORTH, 0, 0, null);
            for (int ex = -1; ex <= 1; ex++) for (int ey = -1; ey <= 1; ey++) {
                int px = ox + ex, py = oy + ey;
                if (px < -8 || px > 8 || py < -8 || py > 8 || gridAt[(px + 8) + 17 * (py + 8)] == stamp) continue;
                gridPut(px, py, ownKing, stamp);
            }
        }
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
                    else if (r.team == G.us) s2 = r.type == UnitType.RAT_KING ? -3 * dmg : -dmg;
                    else s2 = -50; // a cat eats the rat: no damage, it sleeps
                    break;
                }
            }
            s2 = s2 * 2 + (G.hand(me, d) > 0 ? 1 : 0);
            if (s2 > bs) { bs = s2; bestD = d; }
        }
        if (bestD == null) return false;
        // no point-blank hit (42) yet: keep holding (the carried rat is out of the fight) while the carry lasts
        if (bs / 2 < Cfg.THROW_MIN && G.round - carryRound < Cfg.THROW_HOLD) return false;
        // every flight hits one of ours: hold while the carry lasts (at its end the victim wriggles free)
        boolean late = G.round - carryRound >= Cfg.THROW_LATE;
        if (bs < 0 && !late) return false;
        if (rc.getDirection() != bestD) {
            if (!rc.canTurn(bestD)) return false;
            rc.turn(bestD);
            G.facing = bestD;
            // the flight is in view now: one of ours on it, unseen before the turn, stops the throw
            if (!late) {
                MapLocation l = me.add(bestD);
                for (int k = 2; k <= 8; k++) {
                    l = l.add(bestD);
                    if (!rc.onTheMap(l) || !rc.canSenseLocation(l) || (mem[l.x + w * l.y] & blk) != 0) break;
                    RobotInfo r = rc.senseRobotAtLocation(l);
                    if (r == null) continue;
                    if (r.team == G.us) { throwAborts++; return false; }
                    break;
                }
            }
        }
        if (!rc.canThrowRat()) return false;
        rc.throwRat();
        throwsDone++;
        return true;
    }

    /** Rat trap on the front tile nearest an approaching enemy rat (50 damage + stun when it enters the 3x3). */
    static boolean tryRatTrap() throws GameActionException {
        RobotController rc = G.rc;
        if (nearestEnemy == null || !rc.isActionReady() || rc.getNumberRatTraps() >= trapCap()) return false;
        if (rc.getAllCheese() < Econ.floor(Cfg.RAT_TRAP_MIN_CHEESE) || King.trapStarvesSpawn(rc)) return false;
        if (Cfg.TRAP_OPEN_TO > 0 && openTrapBlocked(rc.getRoundNum(), rc.getAllCheese(), Cfg.TRAP_OPEN_TO, Econ.floor(Cfg.TRAP_OPEN_FLOOR))) return false;
        int d0 = G.me.distanceSquaredTo(nearestEnemy.location);
        if (Cfg.TRAP_ADJ ? d0 > Cfg.BABY_TRAP_DIST2 : d0 <= 2 || d0 > Cfg.BABY_TRAP_DIST2) return false;
        MapLocation best = trapTile(rc.getLocation(), rc.getDirection(), nearestEnemy.location, d0, Cfg.TRAP_ADJ);
        if (best == null) return false;
        rc.placeRatTrap(best);
        ratTraps++;
        return true;
    }

    /** Cfg.TRAP_OPEN_TO: before round `to` a rat trap needs `floor` banked (bodies first in the opening). */
    static boolean openTrapBlocked(int round, int bank, int to, int floor) { return round < to && bank < floor; }

    /** The front tile for a rat trap against an enemy at e (d0 = our dist2 to it): the one nearest e if nearer than we
     *  are, or with adj (Cfg.TRAP_ADJ) one within e's 3x3, nearest first (ties by pick bit, SYM_TIE); null if none. */
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
                if (s2 < bs || Cfg.SYM_TIE && s2 == bs && G.tieFirst(me, l, best)) { bs = s2; best = l; }
            } else if (s2 < bs) { bs = s2; best = l; }
        }
        return adj || bs < d0 ? best : null;
    }

    /** The live rat traps past which no combat trap is laid (TRAP_CAP, plus MINE_TRAP_CAP with trapped mines). */
    static int trapCap() { return Cfg.MINE_TRAPS > 0 || Cfg.RAID_TRAPS > 0 ? Math.min(25, Cfg.TRAP_CAP + Cfg.MINE_TRAP_CAP) : Cfg.TRAP_CAP; }

    /** Trap sites around a mine in our side frame (offsets times fu, fv); the order breaks distance ties. */
    static final int[][] MINE_SITES = {{-3, -3}, {-3, 0}, {0, -3}, {-3, 3}, {3, -3}, {0, 3}, {3, 0}, {3, 3}};

    /** l's rank among mine m's eight trap sites by distance to ek (ties in MINE_SITES order), or 99 if l is no site. */
    static int mineSiteRank(MapLocation m, MapLocation l, MapLocation ek) {
        int ox = l.x - m.x, oy = l.y - m.y;
        if (ox % 3 != 0 || oy % 3 != 0 || ox < -3 || ox > 3 || oy < -3 || oy > 3 || ox == 0 && oy == 0) return 99;
        int dl = l.distanceSquaredTo(ek), rank = 0;
        boolean seen = false;
        for (int[] s : MINE_SITES) {
            int sx = s[0] * fu, sy = s[1] * fv;
            if (sx == ox && sy == oy) { seen = true; continue; }
            int d = m.translate(sx, sy).distanceSquaredTo(ek);
            if (d < dl || d == dl && !seen) rank++;
        }
        return rank;
    }

    /** A rat trap on a front tile that is one of mine m's first `sites` trap sites (MINE_TRAPS, RAID_TRAPS). */
    static boolean mineTrap(MapLocation m, int sites) throws GameActionException {
        RobotController rc = G.rc;
        if (G.coop || !rc.isActionReady() || rc.getNumberRatTraps() >= Cfg.MINE_TRAP_CAP) return false;
        if (rc.getAllCheese() < Econ.floor(Cfg.MINE_TRAP_CHEESE) || King.trapStarvesSpawn(rc)) return false;
        MapLocation ek = G.mirror(ref(), firstSym()), me = rc.getLocation();
        for (Direction d : G.fan(me, rc.getDirection())) {
            MapLocation l = me.add(d);
            if (mineSiteRank(m, l, ek) < sites && rc.canPlaceRatTrap(l)) {
                rc.placeRatTrap(l);
                ratTraps++; mineTraps++;
                return true;
            }
        }
        return false;
    }

    /** Bite the best adjacent enemy: a king first, then the lowest health (BITE_CARRIER: a rat carrying one of ours
     *  first among rats; BITE_GUARD: a rat our bite leaves no healthier than us, and so unable to grab us, right after
     *  the kills); turn to face it when needed. */
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
            if (r.type == UnitType.RAT_KING) {
                // any king tile adjacent to us
                int dx = Math.max(-1, Math.min(1, me.x - t.x)), dy = Math.max(-1, Math.min(1, me.y - t.y));
                t = t.translate(dx, dy);
            }
            if (me.distanceSquaredTo(t) > 2) continue;
            int s = (r.type == UnitType.RAT_KING ? 0 : 1000) + r.health;
            if (Cfg.BITE_GUARD && r.type == UnitType.BABY_RAT && guardBite(r.health, myHp)) s = 1011;
            // a rat carrying one of ours throws it next (often into our king): bite it before any other rat
            if (Cfg.BITE_CARRIER && r.carryingRobot != null && r.carryingRobot.team == G.us) s = 1000;
            // a rat in reach of our king bites it next: after an enemy king, before any other rat
            if (guardKing != null && r.type == UnitType.BABY_RAT && G.cheb(r.location, guardKing) <= 2) s = 650 + r.health;
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
        if (rc.canAttack(t)) {
            int c = r.type == UnitType.BABY_RAT ? finishCheese(r.health, rc.getGlobalCheese()) : 0;
            if (c > 0 && rc.canAttack(t, c)) rc.attack(t, c); else rc.attack(t);
            bitesRat++;
            return true;
        }
        return false;
    }

    /** An enemy rat with hp can grab us (strictly healthier) and one bite (10) takes that away; not a kill. */
    static boolean guardBite(int hp, int myHp) { return hp > myHp && hp <= myHp + 10 && hp > 10; }

    /** Cheese that turns a bite on a target with hp into a kill (k*k for +k, k <= FINISH_K), else 0. */
    static int finishCheese(int hp, int global) {
        if (hp <= 10 || global < Cfg.FINISH_FLOOR) return 0;
        int k = hp - 10;
        return k <= Cfg.FINISH_K ? k * k : 0;
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
    /** Cfg.CAT_LAY: a cat trap on an adjacent cat-lane tile the cat left CAT_LAY_AGE+ rounds ago, the most recently
     *  walked first (laneKey). */
    static void layLane() throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.isActionReady() || rc.getNumberCatTraps() >= 10 || rc.getAllCheese() < Cfg.CAT_LAY_FLOOR) return;
        MapLocation me = rc.getLocation(), best = null;
        int bs = Integer.MAX_VALUE;
        for (Direction d : G.DIRS) {
            MapLocation l = me.add(d);
            if (!CatLane.lane(l.x, l.y)) continue;
            int a = CatLane.age(l.x, l.y, G.round);
            if (a < Cfg.CAT_LAY_AGE || !rc.canPlaceCatTrap(l)) continue;
            int s = laneKey(a, l, G.pickBit(me, l));
            if (s < bs || Cfg.SYM_TIE && s == bs && G.tieFirst(me, l, best)) { bs = s; best = l; }
        }
        if (best != null) { rc.placeCatTrap(best); trapsPlaced++; }
    }

    /** layLane's rank: age, then dist2 from the map centre (doubled coords; both mirror-invariant), then the pick bit. */
    static int laneKey(int age, MapLocation l, boolean bit) {
        int px = 2 * l.x - G.cx2, py = 2 * l.y - G.cy2;
        return ((Math.min(age, 4000) << 15) + Math.min(px * px + py * py, 32767)) * 2 + (bit ? 0 : 1);
    }

    static void tryCatTrap(RobotInfo c) throws GameActionException {
        RobotController rc = G.rc;
        if (!rc.isActionReady()) return;
        if (rc.getAllCheese() < Cfg.TRAP_FLOOR) return;
        // defending a king is survival: traps there need only the floor; elsewhere they need projected surplus
        MapLocation k = Comms.nearestKing(G.me);
        boolean defend = k != null && k.distanceSquaredTo(c.location) <= Cfg.TRAP_DEFEND_DIST2;
        if (!defend && !(Cfg.CAT_RING && G.coop) && Comms.read(Comms.BUDGET) * 4 < Cfg.TRAP_SURPLUS) return;
        if (rc.getNumberCatTraps() >= 10) return;
        int d4 = catDist4(c.location, G.me);
        if (d4 > 4 * 36) return;
        MapLocation best = null;
        int bs = Integer.MAX_VALUE;
        MapLocation me = rc.getLocation();
        Direction f = rc.getDirection();
        Direction[] fr = G.fan(me, f);
        MapLocation n1 = c.location.add(c.direction), n2 = n1.add(c.direction), n3 = n2.add(c.direction);
        for (Direction d : fr) {
            MapLocation l = me.add(d);
            if (!rc.canPlaceCatTrap(l)) continue;
            // trigger zone is the 3x3 around the trap: the cat must pass within Chebyshev 1 of it
            int s = Math.min(Math.min(chebToBody(n1, l), chebToBody(n2, l)), chebToBody(n3, l));
            if (Cfg.CAT_RING) s = Math.min(s, chebToBody(c.location, l));
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

    /** Squeak one fact: to a king in hearing range a mine it may not know or a sharper symmetry mask; at war, an enemy's front. */
    static void report() {
        MapLocation k = Comms.numKings == 0 ? null : Comms.nearestKing(G.me);
        boolean near = k != null && (Cfg.SYM_HEAR_TILE ? kingInEarshot(G.me) : G.me.distanceSquaredTo(k) <= 16);
        // RISK_ADAPT: news of an enemy trap goes rat to rat and to the kings until the shared bit is set
        if (Cfg.BURN && burnRound > 0 && G.round - burnSqueaked >= Cfg.BURN_EVERY
                && (Comms.read(Comms.SYM) & Comms.SYM_BURN) == 0) {
            G.rc.squeak(Comms.squeak(Comms.SQ_BURN, G.me.x, G.me.y, 0));
            burnSqueaked = G.round;
            return;
        }
        if (Cfg.GOSSIP && Comms.numMines == 0) {
            // no mine known to the team yet: pass mine news rat to rat, which outruns a courier walking home
            for (Message m : G.rc.readSqueaks(-1)) {
                int c = m.getBytes();
                if (Comms.sqType(c) == Comms.SQ_MINE) noteMine(Comms.sqX(c), Comms.sqY(c));
                else if (Cfg.SYM_GOSSIP && Comms.sqType(c) == Comms.SQ_SYM) Sym.merge(Comms.sqExtra(c) & 7);
            }
            if (symGossip(Cfg.SYM_GOSSIP, near)) return;
            if (myMineCount > 0 && !near) {
                int key = myMines[G.round % myMineCount];
                G.rc.squeak(Comms.squeak(Comms.SQ_MINE, key / 64, key % 64, 0));
                return;
            }
        } else if (Cfg.GOSSIP_ALWAYS) {
            for (Message m : G.rc.readSqueaks(G.round - 1)) {
                int c = m.getBytes();
                if (Comms.sqType(c) == Comms.SQ_MINE) noteMine(Comms.sqX(c), Comms.sqY(c));
                else if (Cfg.SYM_GOSSIP && Comms.sqType(c) == Comms.SQ_SYM) Sym.merge(Comms.sqExtra(c) & 7);
            }
            if (symGossip(Cfg.SYM_GOSSIP, near)) return;
            if (!near) {
                int key = unreported();
                if (key >= 0) { G.rc.squeak(Comms.squeak(Comms.SQ_MINE, key / 64, key % 64, 0)); return; }
            }
        }
        // a squeak reaches robots with any tile within dist2 16 of us: the king's centre within 16 is always heard
        if (near) {
            int sharedSym = Cfg.BURN ? Comms.read(Comms.SYM) & 7 : Comms.read(Comms.SYM);
            if (sharedSym != 0 && (Sym.mask & sharedSym) != sharedSym) {
                G.rc.squeak(Comms.squeak(Comms.SQ_SYM, 0, 0, Sym.mask));
                return;
            }
            for (int i = 0; i < myMineCount; i++) {
                int x = myMines[i] / 64, y = myMines[i] % 64;
                if (!Comms.hasMine(x, y)) { G.rc.squeak(Comms.squeak(Comms.SQ_MINE, x, y, 0)); return; }
            }
        }
        // at war: the enemy king first (seen, or news to relay once per sighting), then an enemy's front
        if (!G.coop && Cfg.KING_PRESS && lastEKing != null && G.round - lastEKingRound <= 2
                && (ekHops == 0 && eKing != null || ekHops < Cfg.PRESS_HOPS && ekRelayed != lastEKingRound)) {
            int hops = eKing != null ? 0 : ekHops + 1;
            G.rc.squeak(Comms.squeak(Comms.SQ_EKING, lastEKing.x, lastEKing.y, hops | (lastEKingRound & 1023) << 2));
            ekRelayed = lastEKingRound;
            return;
        }
        if (!G.coop && Cfg.TRAP_SHARE && nearestEnemy != null && nearestEnemy.direction != null) {
            int d = nearestEnemy.direction.ordinal();
            G.rc.squeak(Comms.squeak(Comms.SQ_TRAP, nearestEnemy.location.x, nearestEnemy.location.y, d));
            return;
        }
        if (k != null && G.me.distanceSquaredTo(k) <= 16 && catSeen != null && G.round - catSeenRound <= 2) {
            G.rc.squeak(Comms.squeak(Comms.SQ_CAT, catSeen.x, catSeen.y, 0));
        }
    }

    static int symSqueaked = -100;

    /** Some tile of one of our kings (3x3) is within squeak range (d2 16) of p. */
    static boolean kingInEarshot(MapLocation p) {
        for (int i = 0; i < Comms.numKings; i++) {
            MapLocation k = Comms.kings[i];
            int dx = Math.max(0, Math.abs(p.x - k.x) - 1), dy = Math.max(0, Math.abs(p.y - k.y) - 1);
            if (dx * dx + dy * dy <= 16) return true;
        }
        return false;
    }

    /** Some tile of a cat (2x2 from catBL) is within squeak range (d2 16) of p. */
    static boolean catInEarshot(MapLocation catBL, MapLocation p) {
        int dx = Math.max(0, Math.max(catBL.x - p.x, p.x - catBL.x - 1));
        int dy = Math.max(0, Math.max(catBL.y - p.y, p.y - catBL.y - 1));
        return dx * dx + dy * dy <= 16;
    }

    /** SYM_GOSSIP: away from the kings, squeak a mask sharper than the shared one every SYM_GOSSIP_EVERY rounds,
     *  when an ally can hear it, no enemy is in view at war, and no cat can hear it. */
    static boolean symGossip(boolean on, boolean near) {
        if (!on || near || G.round - symSqueaked < Cfg.SYM_GOSSIP_EVERY) return false;
        int shared = Cfg.BURN ? Comms.read(Comms.SYM) & 7 : Comms.read(Comms.SYM);
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

    static void moveDecision() throws GameActionException {
        if (Cfg.GRAB_MICRO && GrabMicro.round == G.round) return;
        why = '-';
        RobotController rc = G.rc;
        int raw = rc.getRawCheese();
        boolean late = G.round > 1960;

        if (Cfg.CAT_DEFEND_R2 > 0 && cat != null && catAtKing(cat.location) && approachCat(cat)) { why = 'g'; return; }
        // keep gatherers out of cat reach
        if (cat != null && role == GATHER && catDist4(cat.location, G.me) <= 4 * Cfg.GATHER_CAT_DIST) {
            // flee the cat's centre (its location is the 2x2 body's low corner, which mirrors to the far corner)
            if (Cfg.SYM_CENTRE ? Nav.flee2(2 * cat.location.x + 1, 2 * cat.location.y + 1) : Nav.flee(cat.location)) { why = 'c'; return; }
        }
        if (Cfg.YIELD_KING && cat != null && raw == 0 && yieldKing()) { why = 'y'; return; }
        if (Cfg.CARRY_AIM && !G.coop && rc.getCarrying() != null && rc.getCarrying().team == G.them && aimCarry()) { why = 'T'; return; }
        if (rescueT != null && rescue()) { why = 'R'; return; }
        Debug.mark(10);
        if (Cfg.AMMO_AWAY && !G.coop && ammoAway()) { why = 'A'; return; }
        if (!G.coop && retreat()) { why = 'r'; return; }
        // the bank is nearly empty and the king starves at 0: whatever we carry goes home before any fight or errand
        if (raw > 0 && G.globalCheese < Cfg.HUNGRY && deliver()) { why = 'H'; return; }
        if (Cfg.FORM_KING && formKing()) { why = 'K'; return; }
        Debug.mark(11);
        if (!G.coop && raider && raw == 0 && rc.getHealth() >= Cfg.RAID_HP && homeHalf(G.me)
                && (nearestEnemy == null || G.me.distanceSquaredTo(nearestEnemy.location) > Cfg.RAID_ENGAGE) && raid()) { why = 'z'; return; }
        Debug.mark(12);
        if (!G.coop && Cfg.FLANK && Cfg.NAP && flank(seen)) { why = 'k'; return; }
        if (!G.coop && Cfg.KING_PRESS && pressKing()) { why = 'p'; return; }
        Debug.mark(13);
        if (!G.coop && Cfg.MICRO && role != ATTACK && Micro.act(seen)) { why = 'm'; return; }
        Debug.mark(14);
        if (!G.coop && Cfg.FACE_ENEMY && (role != GATHER || Cfg.WAR_GATHER < 2 || nearEnemy(2)) && faceEnemy()) { why = 'f'; return; }
        if (role == ATTACK && attack()) { why = 'a'; return; }
        Debug.mark(15);
        if (!G.coop && war()) { why = 'w'; return; }
        if (!G.coop && role == FIGHT && Cfg.FORT_R > 0 && fort()) { why = 'h'; return; }
        Debug.mark(16);
        why = 'g';
        if (cat != null && role == FIGHT) {
            if (approachCat(cat)) return;
        }

        if ((raw >= Cfg.DELIVER_AT || (raw > 0 && (late || cheeseTarget == null && raw >= Cfg.DELIVER_IDLE))) && deliver()) return;
        if (Cfg.COURIER && Comms.numMines == 0 && myMineCount > 0) {
            // we found a mine nobody else knows: squeaks only reach the king from dist2 16, so carry the news home
            MapLocation k = Comms.nearestKing(G.me);
            if (k != null) { Nav.goTo(k); why = 'm'; return; }
        }
        if (cheeseTarget != null) {
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
                if (Cfg.CHEESE_BAN > 0 && (!Cfg.CHEESE_BAN_WALL || Flow.detour(cheeseTarget))) banCheese(cheeseTarget);
                if (Cfg.BREAKOUT_TTL > 0 && walledOff(G.me, cheeseTarget)) wallStrike(G.round, Cfg.BREAKOUT_TTL);
                cheeseTarget = null;
            }
            return;
        }
        if (role == FIGHT) {
            MapLocation ct = knownCat();
            if (ct != null) { Nav.goTo(ct); return; }
            if (Cfg.RAID && raider && rc.getHealth() >= Cfg.RAID_HP && raid()) { why = 'z'; return; }
        }
        if (scouting()) {
            // a scout with news the king lacks carries it home (squeaks reach the king from dist2 16 only)
            MapLocation k = Comms.nearestKing(G.me);
            if (k != null && unreported() >= 0) { Nav.goTo(k); why = 's'; return; }
            why = 'S';
            if (Cfg.SCOUT_FRONTIER && exploreFrontier()) return;
            if (!exploreHome()) explore();
            return;
        }
        if (role == GATHER && G.round < seekUntil && seek()) { why = 'x'; return; }
        if (Cfg.MINE_EXPLORE > 0 && role == GATHER && G.round < Cfg.MINE_EXPLORE_UNTIL
                && explorer(slot, homeMineCount(), Cfg.MINE_EXPLORE) && exploreHome()) { mineTarget = null; why = 'x'; return; }
        if (role == GATHER || role == FIGHT && Cfg.FIGHT_MINE) {
            if (Cfg.MINE_BALANCE && mineTarget != null && (Cfg.HOME_ALL_SYM ? Comms.numMines != minesAtPick || Sym.mask != maskAtPick : Comms.numMines != minesAtPick)) mineTarget = null;
            if (Cfg.MINE_ANY_COOP && !G.coop && mineTarget != null && !homeHalf(mineTarget)) mineTarget = null;
            if (mineTarget == null) mineTarget = pickMine();
            if (mineTarget != null) {
                if (Cfg.SEEK_IDLE > 0 && role == GATHER && idle(mineTarget)) {
                    if (Cfg.IDLE_SHIFT) { mineShift++; mineTarget = pickMine(); }
                    if (idleSeek() && seek()) { why = 'x'; return; }
                    if (mineTarget == null) mineTarget = pickMine();
                }
                if (atMine(mineTarget, Cfg.MINE_KING_R)) {
                    if (role == GATHER && startSeek(mineTarget) && seek()) { why = 'x'; return; }
                    if (Cfg.GATHER_STILL) {
                        // hold still (moves set off traps laid at our mines) and sweep the cone around the spawn square
                        Direction t = G.facing.rotateRight().rotateRight();
                        if (rc.canTurn(t)) { rc.turn(t); G.facing = t; }
                        if (Cfg.MINE_TRAPS > 0) mineTrap(mineTarget, Cfg.MINE_TRAPS);
                        return;
                    }
                    // wander inside the spawn square, facing outward to see new cheese
                    wanderAround(mineTarget);
                    if (Cfg.MINE_TRAPS > 0) mineTrap(mineTarget, Cfg.MINE_TRAPS);
                    why = 'W';
                } else {
                    why = 'M';
                    Nav.goTo(mineTarget);
                    if (Nav.stall > 25) mineTarget = null;
                }
                return;
            }
        }
        why = 'E';
        explore();
    }

    /** Head for the nearest king to hand over our cheese (face it once in transfer reach); false with no king known. */
    static boolean deliver() throws GameActionException {
        MapLocation k = Comms.nearestKing(G.me);
        if (k == null) return false;
        if (inTransferReach(G.me, k)) {
            // in transfer range of the king's body (dist2 9 to its nearest tile): face it, stay off the ring
            Direction d = G.me.directionTo(k);
            if (d != Direction.CENTER && G.rc.getDirection() != d && G.rc.canTurn(d)) G.rc.turn(d);
            why = 'D';
            return true;
        }
        why = 'd';
        if (Cfg.TRAIL && homeLeg(k)) return true;
        Nav.goTo(k);
        return true;
    }

    static int homeBest, homeStall, homeRound = -10, homeHeld;
    static boolean homing;

    /** Cfg.TRAIL: after FLOW_STALL turns of delivery without getting closer to the nearest king (a roaming king does
     *  not reset the count, unlike Nav's stall), walk the trail back until it ends; while it is held by robots, wait
     *  FLOW_HOLD turns, then let Nav route around them. True when this took the turn. */
    static boolean homeLeg(MapLocation k) throws GameActionException {
        if (homeRound != G.round - 1) { homing = false; homeBest = Integer.MAX_VALUE; homeStall = 0; homeHeld = 0; }
        homeRound = G.round;
        int c = G.cheb(G.me, k);
        if (c < homeBest) { homeBest = c; homeStall = 0; } else homeStall++;
        if (!homing && homeStall < Cfg.FLOW_STALL) return false;
        homing = true;
        if (!G.rc.isMovementReady()) return true;
        int r = Trail.back();
        if (r == Trail.MOVED) { homeHeld = 0; return true; }
        if (r == Trail.END) { homing = false; homeStall = 0; homeBest = c; return false; }
        return ++homeHeld <= Cfg.FLOW_HOLD;
    }

    static int noKing;               // symmetry candidates whose enemy-king spot we saw empty
    static MapLocation lastEKing;
    static int lastEKingRound = -100;
    /** Relay hops of lastEKing's news (0 = seen ourselves), and the sighting round we last relayed. */
    static int ekHops, ekRelayed = -100;
    public static int presses;

    /** An SQ_EKING squeak: extra = hops | (sighting round % 1024) << 2. A newer sighting (or the same one in fewer
     *  hops) replaces ours. */
    static void heardEKing(int c) {
        int e = Comms.sqExtra(c), s = (e >> 2) & 1023;
        int seenRound = G.round - (((G.round & 1023) - s + 1024) & 1023);
        int hops = e & 3;
        if (seenRound > lastEKingRound || seenRound == lastEKingRound && hops < ekHops) {
            lastEKing = new MapLocation(Comms.sqX(c), Comms.sqY(c));
            lastEKingRound = seenRound;
            ekHops = hops;
        }
    }

    /** King press movement (Cfg.KING_PRESS): walk to a fresh enemy-king sighting in range and stay against it. */
    static boolean pressKing() throws GameActionException {
        RobotController rc = G.rc;
        MapLocation ek = eKing != null ? eKing.location : G.round - lastEKingRound <= Cfg.PRESS_AGE ? lastEKing : null;
        if (ek == null && Cfg.PRESS_SHARED) ek = sharedEKing();
        if (ek == null || rc.getHealth() < Cfg.PRESS_HP) return false;
        if (role == GATHER && rc.getRawCheese() >= Cfg.DELIVER_AT) return false;
        int d = G.me.distanceSquaredTo(ek);
        if (d > (role == GATHER ? Cfg.PRESS_GATHER_D2 : Cfg.PRESS_D2)) return false;
        if (eKing == null && d <= 8 && rc.canSenseLocation(ek)) {
            RobotInfo r = rc.senseRobotAtLocation(ek);
            if (r == null || r.type != UnitType.RAT_KING || r.team != G.them) { lastEKing = null; return false; }
        }
        presses++;
        if (G.cheb(G.me, ek) <= 2) {
            // touching the king's 3x3: face its nearest tile and bite (bites run before and after movement)
            int dx = Math.max(-1, Math.min(1, G.me.x - ek.x)), dy = Math.max(-1, Math.min(1, G.me.y - ek.y));
            Direction f = G.me.directionTo(ek.translate(dx, dy));
            if (f != Direction.CENTER && rc.getDirection() != f && rc.canTurn(f)) { rc.turn(f); G.facing = f; }
            return true;
        }
        Nav.goTo(ek);
        return true;
    }

    /** The kings' shared enemy-king sighting (Comms.EKING) when at most PRESS_AGE rounds old, else null. */
    static MapLocation sharedEKing() {
        int x = Comms.read(Comms.EKING);
        if (x == 0 || !Comms.fresh(Comms.read(Comms.EKING + 2), Cfg.PRESS_AGE)) return null;
        return new MapLocation(x - 1, Comms.read(Comms.EKING + 1) - 1);
    }

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
        if (Cfg.RUSH_STICK) return rushStick();
        RobotController rc = G.rc;
        MapLocation best = null;
        int bd = Integer.MAX_VALUE;
        for (int s = 1; s <= 4; s <<= 1) {
            if ((Sym.mask & s) == 0 || (noKing & s) != 0) continue;
            MapLocation c = G.mirror(ref(), s);
            if (G.me.distanceSquaredTo(c) <= 13 && rc.canSenseLocation(c)) {
                RobotInfo r = rc.senseRobotAtLocation(c);
                if (r == null || r.type != UnitType.RAT_KING || r.team != G.them) { noKing |= s; continue; }
            }
            int d = G.me.distanceSquaredTo(c) * 2 + (G.pickBit(G.me, c) ? 1 : 0);
            if (d < bd || Cfg.SYM_TIE && d == bd && G.tieFirst(G.me, c, best)) { bd = d; best = c; }
        }
        if (best == null && noKing != 0) { noKing = 0; return null; }
        return best;
    }

    /** Cfg.RUSH_STICK: the symmetry whose enemy-king spot this attacker walks to (0 = none yet). */
    static int rushSym;

    /** rushTarget with Cfg.RUSH_STICK: the nearest live mirror spot is picked once and kept while it stays live. */
    static MapLocation rushStick() throws GameActionException {
        RobotController rc = G.rc;
        MapLocation best = null;
        int bd = Integer.MAX_VALUE, bs = 0, live = 0;
        for (int s = 1; s <= 4; s <<= 1) {
            if ((Sym.mask & s) == 0 || (noKing & s) != 0) continue;
            MapLocation c = G.mirror(ref(), s);
            if (G.me.distanceSquaredTo(c) <= 13 && rc.canSenseLocation(c)) {
                RobotInfo r = rc.senseRobotAtLocation(c);
                if (r == null || r.type != UnitType.RAT_KING || r.team != G.them) { noKing |= s; continue; }
            }
            live |= s;
            int d = G.me.distanceSquaredTo(c) * 2 + (G.pickBit(G.me, c) ? 1 : 0);
            if (d < bd || Cfg.SYM_TIE && d == bd && G.tieFirst(G.me, c, best)) { bd = d; best = c; bs = s; }
        }
        if (best == null && noKing != 0) { noKing = 0; rushSym = 0; return null; }
        rushSym = stickSym(live, rushSym, bs);
        return rushSym == bs ? best : G.mirror(ref(), rushSym);
    }

    /** The symmetry an attacker walks to: its current one while still live, else this turn's nearest. */
    static int stickSym(int live, int cur, int best) {
        return (live & cur) != 0 ? cur : best;
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
            if (Cfg.FACE_HOLD && faceHold()) { faceHolds++; return true; }
            rc.move(to);
            G.me = rc.getLocation();
        }
        return true;
    }

    public static int faceHolds;

    /** Cfg.FACE_HOLD: hold at Chebyshev 2 instead of stepping into contact with nearestEnemy (see Cfg). */
    static boolean faceHold() {
        if (hunting || nearestEnemy.health < G.rc.getHealth() || !sees(nearestEnemy, G.me)) return false;
        if (!trapsKnown(burnRound, Comms.read(Comms.SYM))) return false;
        MapLocation th = threatenedKing();
        return th == null || nearestEnemy.location.distanceSquaredTo(th) > Cfg.KING_THREAT_D2;
    }

    /** War movement: close on a visible enemy king or nearby enemy rat, or answer a king's alert. */
    static boolean war() throws GameActionException {
        int raw = G.rc.getRawCheese();
        MapLocation threatened = threatenedKing();
        if (threatened != null) defends++;
        if (role == GATHER && Cfg.WAR_GATHER > 0 && !hunting && threatened == null) {
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
                // standoff: stepping into the front of an enemy that sees us (and is not weaker) walks onto its
                // traps and into a grab; hold, face it and let it come (flank and prey moves still engage)
                // (not against a rat at our threatened king: it is biting the king while we wait)
                boolean atKing = threatened != null && nearestEnemy.location.distanceSquaredTo(threatened) <= Cfg.KING_THREAT_D2;
                if (Cfg.STANDOFF && !hunting && !atKing && sees(nearestEnemy, G.me) && nearestEnemy.health >= G.rc.getHealth()) {
                    Direction f = G.me.directionTo(nearestEnemy.location);
                    if (G.rc.getDirection() != f && G.rc.canTurn(f)) { G.rc.turn(f); G.facing = f; }
                    standoffs++;
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

    public static int defends;

    /** Our king's location when it reported enemy rats close to it this or last round and we are near enough to
     *  help (gatherers then fight instead of gathering), else null. */
    static MapLocation threatenedKing() {
        if (!Cfg.DEFEND || G.coop || !Comms.fresh(Comms.read(Comms.ALERT + 2), 2) || Comms.alertThreat() < 1) return null;
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

    /** Cfg.RESCUE: this turn's enemy carrier of one of ours within RESCUE_D2, or null. */
    static RobotInfo rescueT;
    public static int rescues;

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
            if (s < bd || Cfg.SYM_TIE && s == bd && G.tieFirst(G.me, r.location, best.location)) { bd = s; best = r; }
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
        if (bestD == null) return Cfg.RESCUE_BLOCK && blockMuzzle(c) || Cfg.RESCUE_WALK && walkBehind(c);
        MapLocation g = Nav.guard;
        Nav.guard = c;
        boolean moved = Nav.step(bestD);
        Nav.guard = g;
        if (moved) rescues++;
        return moved;
    }

    /** Cfg.RESCUE_BLOCK: a throw needs the tile in front of the carrier empty; step into it, facing the carrier. */
    static boolean blockMuzzle(MapLocation c) throws GameActionException {
        MapLocation m = c.add(rescueT.direction);
        if (G.cheb(G.me, m) != 1) return false;
        Direction d = G.me.directionTo(m);
        if ((Nav.avoid & (1 << d.ordinal())) != 0 || !G.rc.canMove(d)) return false;
        MapLocation g = Nav.guard;
        Nav.guard = c;
        boolean moved = Nav.step(d);
        Nav.guard = g;
        if (moved) blocks++;
        return moved;
    }

    public static int blocks;

    /** Cfg.RESCUE_WALK: no blind tile next to rescueT is one step away; walk toward the nearest open one. */
    static boolean walkBehind(MapLocation c) throws GameActionException {
        MapLocation best = null;
        int bd = Integer.MAX_VALUE;
        for (int i = 0; i < 8; i++) {
            MapLocation t = c.add(G.DIRS[i]);
            if (sees(rescueT, t) || !G.rc.onTheMap(t) || G.rc.canSenseLocation(t) && !G.rc.sensePassability(t)) continue;
            int d = (G.me.distanceSquaredTo(t) * 2) + (G.pickBit(G.me, t) ? 0 : 1);
            if (d < bd || Cfg.SYM_TIE && d == bd && G.tieFirst(G.me, t, best)) { bd = d; best = t; }
        }
        if (best == null) return false;
        Nav.goTo(best);
        rescues++;
        return true;
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
        if (moved) flanks++;
        return moved;
    }

    public static int flanks, standoffs;

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
    /** An empty rat on the ring around our king's 3x3 (Chebyshev 2 from its centre) while the cat we see is near
     *  that king: step to a tile off the ring and out of the cat's lane, farthest from the king. */
    static boolean yieldKing() throws GameActionException {
        MapLocation k = Comms.nearestKing(G.me);
        if (k == null || G.cheb(G.me, k) != 2 || King.catGap(k, cat.location) > 3) return false;
        RobotController rc = G.rc;
        if (!rc.isMovementReady()) return false;
        int lane = laneMask(cat), best = -1, bd = -1;
        for (int i = 0; i < 8; i++) {
            if ((lane & (1 << i)) != 0 || !rc.canMove(G.DIRS[i])) continue;
            MapLocation t = G.me.add(G.DIRS[i]);
            if (G.cheb(t, k) < 3) continue;
            int d = 2 * t.distanceSquaredTo(k) + (G.hand(G.me, G.DIRS[i]) > 0 ? 1 : 0);
            if (d > bd) { bd = d; best = i; }
        }
        return best >= 0 && Nav.step(G.DIRS[best]);
    }

    /** Our king's centre while this turn's nearestEnemy is a rat in reach of it (Cfg.GUARD_KING_R), else null. */
    static MapLocation guardKing;
    public static int guards;

    /** Cfg.GUARD_KING_R: when we are within Chebyshev reach of our nearest king, the visible enemy baby nearest us among
     *  those within Chebyshev 2 of its centre (they can bite it), else null. Ties: lower health, then G.pickBit. */
    static RobotInfo kingBiter(RobotInfo[] robots, int reach) {
        MapLocation k = Comms.nearestKing(G.me);
        if (k == null || G.cheb(G.me, k) > reach) return null;
        RobotInfo best = null;
        int bk = Integer.MAX_VALUE;
        for (RobotInfo r : robots) {
            if (r.team != G.them || r.type != UnitType.BABY_RAT || G.cheb(r.location, k) > 2) continue;
            int key = (G.me.distanceSquaredTo(r.location) * 128 + r.health) * 2 + (G.pickBit(G.me, r.location) ? 0 : 1);
            if (key < bk || Cfg.SYM_TIE && key == bk && G.tieFirst(G.me, r.location, best.location)) { bk = key; best = r; }
        }
        if (best != null) guardKing = k;
        return best;
    }

    /** Cfg.AMMO_AWAY: step away from our king (or hold) while ammo(...) holds; false when it doesn't. */
    static boolean ammoAway() throws GameActionException {
        if (nearestEnemy == null) return false;
        MapLocation k = Comms.nearestKing(G.me);
        if (k == null || !ammo(G.rc.getHealth(), nearestEnemy.health, G.me.distanceSquaredTo(nearestEnemy.location), G.cheb(G.me, k))) return false;
        Nav.flee(k);
        return true;
    }

    /** True when a baby with health hp, cheb from our king's centre, is grabbable ammunition for an enemy rat with
     *  health ehp at dist2 d2 (Cfg.AMMO_AWAY). */
    static boolean ammo(int hp, int ehp, int d2, int cheb) {
        return hp <= Cfg.AMMO_HP && ehp > hp && d2 <= Cfg.AMMO_D2 && cheb <= Cfg.AMMO_R;
    }

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
        Direction f = Cfg.SYM_CENTRE ? G.toCentre(k) : k.directionTo(mid);
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
    /** A cat at c is within dist2 Cfg.CAT_DEFEND_R2 of one of our live kings. */
    static boolean catAtKing(MapLocation c) {
        for (int k = 0; k < Comms.numKings; k++)
            if (c.distanceSquaredTo(Comms.kings[k]) <= Cfg.CAT_DEFEND_R2) return true;
        return false;
    }

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
                if (d < bd || Cfg.SYM_TIE && d == bd && G.tieFirst(me, t, best)) { bd = d; best = t; }
            }
        if (best == null) return false;
        Nav.goTo(best);
        return true;
    }

    static MapLocation pickMine() {
        // A mine drops 20 cheese about every 12 rounds into its 9x9 square, however many rats wait there, so
        // gatherers spread over the MINE_SPREAD nearest known mines on our half (cheesefarm: 4 mines, and every rat
        // used to crowd the nearest). Order: distance from home, ties by the mirror-invariant offset.
        int n = 0;
        for (int i = 0; i < Comms.numMines && n < cand.length; i++) n = addCand(Comms.mines[i], n);
        for (int i = 0; i < myMineCount && n < cand.length; i++) n = addCand(new MapLocation(myMines[i] / 64, myMines[i] % 64), n);
        if (n == 0) return null;
        int ours = 0;
        for (int i = 0; i < n; i++) if (homeHalf(cand[i])) { MapLocation t = cand[ours]; cand[ours++] = cand[i]; cand[i] = t; }
        int m = ours > 0 && !(Cfg.MINE_ANY || Cfg.MINE_ANY_COOP && G.coop) ? ours : n;
        long[] key = candKey;
        boolean rr = Cfg.MINE_RR && rrSlot >= 0 && origin != null;
        MapLocation kh = rr ? origin : home;
        for (int i = 0; i < m; i++) key[i] = mineKey(cand[i], kh);
        for (int i = 1; i < m; i++) {   // insertion sort of the first m by mineKey
            long k = key[i];
            MapLocation c = cand[i];
            int j = i;
            for (; j > 0 && k < key[j - 1]; j--) { key[j] = key[j - 1]; cand[j] = cand[j - 1]; }
            key[j] = k; cand[j] = c;
        }
        minesAtPick = Comms.numMines;
        if (Cfg.HOME_ALL_SYM) maskAtPick = Sym.mask;
        if (rr) return cand[rrSlot % m];
        if (Cfg.MINE_BALANCE) return Cfg.IDLE_SHIFT ? cand[(slot % m + mineShift) % m] : cand[slot % m];
        return cand[G.rand(Math.min(m, Cfg.MINE_SPREAD))];
    }

    /** Cfg.MINE_EXPLORE: the rat with hash `slot` explores while it knows m mines on our half (extra exploring slots). */
    static boolean explorer(int slot, int m, int extra) {
        return slot % (m + extra) >= m;
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

    /** A per-rat hash (the scout pick, Cfg.MINE_BALANCE), and the team's known-mine count when mineTarget was picked. */
    static int slot, minesAtPick, maskAtPick;

    static final MapLocation[] cand = new MapLocation[40];
    static final long[] candKey = new long[40];

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
        if (sd > Cfg.FORM_PULL_R2 && !(Cfg.FORM_MINE && nearestEnemy == null && minePull(sd, role, slot))) return false;
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
            return true;
        }
        Nav.goTo(site);
        return true;
    }

    /** Cfg.FORM_MINE: a baby beyond FORM_PULL_R2 (d2 from the site) still joins within FORM_MINE_PULL_R2 when its
     *  slot is drawn; attackers never. */
    static boolean minePull(int d2, int role, int slot) {
        return d2 <= Cfg.FORM_MINE_PULL_R2 && role != ATTACK && (slot >>> 3) % Cfg.FORM_MINE_SHARE == 0;
    }

    /** Allied babies in view strictly nearer the site than we are (no id tie-break: mirrored rats decide alike). */
    static int nearerAllies(MapLocation site) {
        int d = G.me.distanceSquaredTo(site), n = 0;
        for (RobotInfo r : seen)
            if (r.team == G.us && r.type == UnitType.BABY_RAT && r.location.distanceSquaredTo(site) < d) n++;
        return n;
    }

    static int formed;
    static MapLocation raidTarget;
    static boolean raider;           // a fighter that leaves our half to contest theirs

    /** l is closer to our king than to its mirror under the first live symmetry. */
    static boolean homeHalf(MapLocation l) { return Cfg.HOME_ALL_SYM ? nearerAll(l, ref(), Sym.mask) : nearer(l, ref(), firstSym()); }

    /** Cfg.HOME_ALL_SYM: l is nearer h than h's twin under every symmetry in mask (the first live one if none is). */
    static boolean nearerAll(MapLocation l, MapLocation h, int mask) {
        if (mask == 0) return nearer(l, h, firstSym());
        for (int s = Sym.ROT; s <= Sym.VER; s <<= 1) if ((mask & s) != 0 && !nearer(l, h, s)) return false;
        return true;
    }

    /** Whether l is nearer h than h's twin under symmetry s (ties count with Cfg.MINE_TIES). */
    static boolean nearer(MapLocation l, MapLocation h, int s) {
        int a = l.distanceSquaredTo(h), b = l.distanceSquaredTo(G.mirror(h, s));
        return a < b || Cfg.MINE_TIES && a == b;
    }

    /** The king start squeaked to us (Comms.SQ_START), or null; sets rrSlot from its spawn-order field. */
    static MapLocation heardStart(Message[] ms) {
        for (Message m : ms) {
            int c = m.getBytes();
            if (Comms.sqType(c) == Comms.SQ_START) {
                rrSlot = Comms.sqExtra(c) - 1;
                return new MapLocation(Comms.sqX(c), Comms.sqY(c));
            }
        }
        return null;
    }

    /** Cfg.MINE_RR: our place in our role's spawn order, squeaked by the king that spawned us (-1: not heard). */
    static int rrSlot = -1;

    /** Home for the half test and mirrors: our first king's start with Cfg.HOME_START once heard, else home. */
    static MapLocation ref() { return (Cfg.HOME_START || Cfg.FORM_MINE) && origin != null ? origin : home; }

    static int firstSym() {
        return (Sym.mask & Sym.ROT) != 0 ? Sym.ROT : (Sym.mask & Sym.HOR) != 0 ? Sym.HOR : Sym.VER;
    }
    static int raidSince = -1;

    /** Idle fighter: contest the enemy half at a mine closer to the enemy king than to ours; wander there a while. */
    static boolean raid() throws GameActionException {
        if (raidTarget != null && (Nav.stall > 20 || raidSince >= 0 && G.round - raidSince > Cfg.RAID_STAY)) {
            raidTarget = null;
            raidSince = -1;
        }
        if (raidTarget == null) raidTarget = pickRaid();
        if (raidTarget == null) return false;
        Nav.guard = null;
        if (G.cheb(G.me, raidTarget) <= 2) {
            if (raidSince < 0) raidSince = G.round;
            wanderAround(raidTarget);
            if (Cfg.RAID_TRAPS > 0) mineTrap(raidTarget, Cfg.RAID_TRAPS);
        } else {
            Nav.goTo(raidTarget);
        }
        return true;
    }

    /** A mine on the enemy half: known mines and the mirrors of our own, under the first live symmetry. */
    static MapLocation pickRaid() {
        int s = firstSym();
        MapLocation ek = G.mirror(ref(), s);
        MapLocation best = null;
        int bd = Integer.MAX_VALUE;
        for (int i = 0; i < Comms.numMines; i++) {
            MapLocation m = Comms.mines[i];
            for (int k = 0; k < 2; k++) {
                MapLocation c = k == 0 ? m : G.mirror(m, s);
                if (c.distanceSquaredTo(ek) >= c.distanceSquaredTo(home)) continue;
                int d = G.me.distanceSquaredTo(c) + G.rand(Cfg.RAID_SPREAD);
                if (d < bd) { bd = d; best = c; }
            }
        }
        return best != null ? best : new MapLocation((home.x + ek.x) / 2, (home.y + ek.y) / 2);
    }

    static void wanderAround(MapLocation m) throws GameActionException {
        if (exploreTarget == null || G.me.equals(exploreTarget) || G.cheb(exploreTarget, m) > 3 || Nav.stall > 6) {
            exploreTarget = new MapLocation(clampX(m.x + fu * (G.rand(7) - 3)), clampY(m.y + fv * (G.rand(7) - 3)));
            // a tile under one of our kings can never be reached: draw again (twice at most)
            for (int k = 0; k < 2 && Cfg.MINE_KING_R > 0 && Nav.atKing(exploreTarget); k++)
                exploreTarget = new MapLocation(clampX(m.x + fu * (G.rand(7) - 3)), clampY(m.y + fv * (G.rand(7) - 3)));
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

    static void explore() throws GameActionException {
        if (Cfg.BREAKOUT_TTL > 0) {
            if (exploreTarget != null && Flow.failTgt != null && G.cheb(Flow.failTgt, exploreTarget) <= 1
                    && G.round - Flow.failRound < Cfg.FLOW_RETRY) breakoutUntil = G.round + Cfg.BREAKOUT_TTL;
            if (breakingOut() && exploreFrontier()) return;
        }
        if (exploreTarget == null || G.cheb(G.me, exploreTarget) <= 2 || Nav.stall > 20) {
            // target in our side frame: mirrored rats pick mirrored targets. Take the nearest never-seen
            // sample: once open ground is mapped, what is left unseen is buried in dirt, and Flow digs to it.
            // With no mine known by EXPLORE_CENTER_FROM, rank by closeness to the map centre instead: mines
            // that nobody has seen are buried between the kings, and shallow edge pockets do not reveal them.
            boolean centre = Comms.numMines == 0 && G.round >= Cfg.EXPLORE_CENTER_FROM;
            int ox = centre ? (G.W - 1) / 2 : G.me.x, oy = centre ? (G.H - 1) / 2 : G.me.y;
            int x = 0, y = 0, bd = Integer.MAX_VALUE;
            for (int k = 0; k < Cfg.EXPLORE_TRIES; k++) {
                int ax = G.rand(G.W), ay = G.rand(G.H);
                int cx = fu > 0 ? ax : G.W - 1 - ax, cy = fv > 0 ? ay : G.H - 1 - ay;
                int d = Sym.known(G.idx(cx, cy)) ? 1 << 20 : Cfg.SYM_CENTRE && centre ? G.centreCheb2(cx, cy)
                    : Math.max(Math.abs(cx - ox), Math.abs(cy - oy));
                if (d < bd) { bd = d; x = cx; y = cy; }
            }
            exploreTarget = new MapLocation(x, y);
        }
        Nav.goTo(exploreTarget);
    }

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

    /** Cfg.SEEK_IDLE: turns spent near the mine since the last pickup (idlePicked: Baby.picked then); mineShift is
     *  Cfg.IDLE_SHIFT's offset into the pick list. */
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
        seekUntil = G.round + Cfg.SEEK_TTL;
        seekNext = G.round + Cfg.SEEK_RETRY;
        seekMines = myMineCount;
        exploreTarget = null;
        return true;
    }

    /** A gatherer at a crowded mine starts seeking: SEEK_CROWD allied babies nearer the mine than us. */
    static boolean startSeek(MapLocation mine) {
        if (Cfg.SEEK_CROWD <= 0 || G.round < seekNext || nearerAllies(mine) < Cfg.SEEK_CROWD) return false;
        seekUntil = G.round + Cfg.SEEK_TTL;
        seekNext = G.round + Cfg.SEEK_RETRY;
        seekMines = myMineCount;
        exploreTarget = null;
        return true;
    }

    /** One seeking step; false once it is over: a mine found and reported to the king, or nothing left unseen. */
    static boolean seek() throws GameActionException {
        if (myMineCount > seekMines) {
            // squeaks reach the king from dist2 16 only: carry the news home, then gather (the new mine included)
            MapLocation k = Comms.nearestKing(G.me);
            if (k != null && unreported() >= 0) { Nav.goTo(k); return true; }
            mineTarget = null;
        } else if (exploreHome()) return true;
        seekUntil = 0;
        return false;
    }

    static int clampX(int x) { return Math.max(0, Math.min(G.W - 1, x)); }

    static int clampY(int y) { return Math.max(0, Math.min(G.H - 1, y)); }
}
