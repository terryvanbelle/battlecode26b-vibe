package ratbot;

import battlecode.common.*;
import java.util.*;

import static ratbot.BotTest.*;

/**
 * Tests for the champion's mechanisms that had no direct test when the local tournament began (11-21; the tests were
 * last ported at xk6): roles and the rush give-up rule, the king's throw dodge (any carrier since yb9), the baby cap,
 * spawn floors, the reserve and austerity, the rat-trap gates, the carry threat, the cat-feed score, the stuck
 * attacker, the idle count and the FIGHT_POST mine filter. Each test states the rule from Cfg's doc and checks it at
 * the values in force, so a retune keeps it valid. Plain main; exit code 1 on failure.
 */
public class MechTest {

    static MapLocation p(int x, int y) { return new MapLocation(x, y); }

    static RobotInfo baby(int id, Team t, MapLocation l, RobotInfo held) {
        return new RobotInfo(id, t, UnitType.BABY_RAT, 100, l, Direction.SOUTH, 0, 0, held);
    }

    static void at(int r) { round = r; G.startTurn(); }

    // ------------------------------------------------------------------ roles

    /** born()/roleFor: RUSH_FROM <= idx < RUSH_TO before RUSH_UNTIL rushes; otherwise the first GATHER_FIRST gather
     *  and later ones fight on rounds divisible by FIGHT_EVERY. */
    static void testRoles() {
        reset(); install();
        int a = Cfg.RUSH_FROM, b = Cfg.RUSH_TO, u = Cfg.RUSH_UNTIL;
        check(Baby.rusher(a, 0) && Baby.rusher(b - 1, 0) && (u <= 1 || Baby.rusher(a, u - 1)),
              "roles: RUSH_FROM <= idx < RUSH_TO rushes before RUSH_UNTIL");
        check(!Baby.rusher(a - 1, 0) && !Baby.rusher(b, 0), "roles: idx outside [RUSH_FROM, RUSH_TO) never rushes");
        if (u > 0) check(!Baby.rusher(a, u), "roles: no rusher from round RUSH_UNTIL");
        int fe = Cfg.FIGHT_EVERY, idx = Math.max(Cfg.GATHER_FIRST, b), r = 1000 / fe * fe;
        check(Baby.roleFor(idx, r) == Baby.FIGHT, "roles: past GATHER_FIRST a rat born on a FIGHT_EVERY round fights");
        if (fe > 1) check(Baby.roleFor(idx, r + 1) == Baby.GATHER, "roles: born on any other round it gathers");
        if (Cfg.GATHER_FIRST > 0 && !Baby.rusher(0, r)) check(Baby.roleFor(0, r) == Baby.GATHER, "roles: the first rats gather");
        if (u > 0 && a < b) check(Baby.roleFor(a, u - 1) == Baby.ATTACK, "roles: a rusher's role is ATTACK");
    }

    /** RUSH_LIFE: an attacker older than RUSH_LIFE rounds that has not seen the enemy king in the last RUSH_LIFE rounds
     *  turns gatherer (born() starts ekSeen at -10000). */
    static void testRushLife() {
        int L = Cfg.RUSH_LIFE;
        if (L <= 0) return;
        check(!Baby.rushFailed(L, 0, -10000, L), "rush life: still rushing at exactly RUSH_LIFE rounds old");
        check(Baby.rushFailed(L + 1, 0, -10000, L), "rush life: never saw the enemy king: gives up after RUSH_LIFE rounds");
        check(!Baby.rushFailed(L + 50, 0, 60, L), "rush life: saw the king within RUSH_LIFE rounds: keeps rushing");
        check(Baby.rushFailed(2 * L + 61, 0, 60, L), "rush life: RUSH_LIFE rounds since it last saw the king: gives up");
    }

    // ------------------------------------------------------------------ king

    /** THROW_DODGE: the king steps off the flights of enemy babies within THROW_DODGE carrying a rat, whoever's rat it
     *  is (yb9: carriers of their own rats throw them too); no carrier, no dodge. */
    static void testThrowDodge() throws GameActionException {
        if (Cfg.THROW_DODGE < 5) return;
        for (Team heldTeam : new Team[] {Team.A, Team.B}) {
            reset(); W = 30; H = 30; install();
            type = UnitType.RAT_KING; me = p(10, 10); at(300); G.coop = false;
            // a carrier 5 north and 1 east: its south flight runs down x = 11, our 3x3's east column
            RobotInfo held = baby(7, heldTeam, p(11, 15), null), carrier = baby(8, Team.B, p(11, 15), held);
            robots.put(carrier.location, carrier);
            Direction d = King.throwDodge(me, new RobotInfo[] {carrier});
            check(d != null && d.dx == -1 && d.dy <= 0, "throw dodge: steps west off the x = 11 flight (holding "
                  + (heldTeam == Team.A ? "ours" : "theirs") + "): " + d);
            RobotInfo empty = baby(8, Team.B, p(11, 15), null);
            check(King.throwDodge(me, new RobotInfo[] {empty}) == null, "throw dodge: an empty-handed enemy is no flight");
        }
        // a carrier at offset (3, 8): none of its 8 flights enters our 5x5 (the diagonal toward us passes (-2, 3)
        // and (-3, 2)): no dodge
        reset(); W = 30; H = 30; install();
        type = UnitType.RAT_KING; me = p(10, 10); at(300); G.coop = false;
        RobotInfo far = baby(8, Team.B, p(13, 18), baby(7, Team.A, p(13, 18), null));
        check(King.throwDodge(me, new RobotInfo[] {far}) == null, "throw dodge: no flight reaches the 3x3: stay");
    }

    /** babyCap: MAX_BABIES(_WAR), raised to RICH_BABIES from RICH_BANK and RICH2_BABIES from RICH2_BANK, from round
     *  RICH_FROM. */
    static void testBabyCap() {
        reset(); install();
        at(Math.max(Cfg.RICH_FROM, 1) + 10); G.coop = false;
        int cap = Cfg.MAX_BABIES_WAR;
        check(King.babyCap(true, 0) == cap, "baby cap: war cap with an empty bank");
        if (Cfg.RICH_BABIES > cap && Cfg.RICH_BANK < Cfg.RICH2_BANK)
            check(King.babyCap(true, Cfg.RICH_BANK) == Cfg.RICH_BABIES && King.babyCap(true, Cfg.RICH_BANK - 1) == cap,
                  "baby cap: RICH_BABIES from RICH_BANK");
        if (Cfg.RICH2_BABIES > 0)
            check(King.babyCap(true, Cfg.RICH2_BANK) == Math.max(Math.max(cap, Cfg.RICH_BABIES), Cfg.RICH2_BABIES),
                  "baby cap: RICH2_BABIES from RICH2_BANK");
        if (Cfg.RICH_FROM > 1) {
            at(Cfg.RICH_FROM - 1);   // cooperation: the base cap however rich
            check(King.babyCap(false, 1 << 20) == Cfg.MAX_BABIES, "baby cap: no rich tier before RICH_FROM");
        }
    }

    /** spawnFloor: LOW_FLOOR under LOW_BABIES babies, else at least KING_RESERVE; a positive siege floor caps it. */
    static void testSpawnFloors() {
        check(King.spawnFloor(1, true, 0) == Cfg.LOW_FLOOR, "spawn floor: few babies: LOW_FLOOR");
        check(King.spawnFloor(1, false, 0) == Math.max(1, Cfg.KING_RESERVE), "spawn floor: at least KING_RESERVE");
        check(King.spawnFloor(5000, false, 0) == 5000, "spawn floor: a higher floor stands");
        check(King.spawnFloor(5000, false, 7) == 7, "spawn floor: a siege floor caps it");
    }

    /** reserve(r): r per live king, at most the upkeep left to round 2000 (2 a round); floor(f) = f per king (>= 1). */
    static void testReserve() {
        reset(); install();
        Comms.numKings = 0; at(1000);
        check(Econ.floor(100) == 100 && King.reserve(400) == 400, "reserve: no king known counts one");
        Comms.numKings = 2;
        check(Econ.floor(100) == 200 && King.reserve(400) == 800, "reserve: per live king");
        at(1900);
        check(King.reserve(400) == 400, "reserve: late, the upkeep left to round 2000 (200 a king)");
        at(2000);
        check(King.reserve(400) == 0, "reserve: none at round 2000");
        Comms.numKings = 0;
    }

    /** AUSTERE_FROM: austerity from that round while the kings' projection (Comms.BUDGET) is 0. */
    static void testAustere() {
        reset(); install();
        int from = Math.max(Cfg.AUSTERE_FROM, 1);
        shared[Comms.BUDGET] = 0; at(from);
        check(Cfg.AUSTERE_FROM <= 0 || King.austere(), "austere: budget 0 from AUSTERE_FROM");
        shared[Comms.BUDGET] = 3;
        check(!King.austere(), "austere: a positive budget is not austere");
        if (Cfg.AUSTERE_FROM > 1) {
            shared[Comms.BUDGET] = 0; at(Cfg.AUSTERE_FROM - 1);
            check(!King.austere(), "austere: not before AUSTERE_FROM");
        }
    }

    // ------------------------------------------------------------------ baby

    /** tryRatTrap's gates: RAT_TRAP_MIN_CHEESE per king always; away from our king (DEF_TRAP_DIST2) also KING_RESERVE
     *  per king + 20 and no austerity; the enemy within BABY_TRAP_DIST2. */
    static void testTrapGates() throws GameActionException {
        int reserve = Math.min(Cfg.KING_RESERVE, 2 * (2000 - 300)) + 20;
        // {global cheese, budget, king near the enemy, enemy dist (y offset), expect a trap}
        int[][] cases = {
            {reserve - 1, 5, 0, 2, 0},
            {reserve, 5, 0, 2, 1},
            {reserve + 500, 0, 0, 2, Cfg.AUSTERE_FROM > 0 ? 0 : 1},
            {Math.max(Cfg.RAT_TRAP_MIN_CHEESE, 1), 0, 1, 2, Cfg.DEF_TRAP_DIST2 > 0 ? 1 : 0},
            {Cfg.RAT_TRAP_MIN_CHEESE - 1, 5, 1, 2, 0},
        };
        for (int[] c : cases) {
            reset(); W = 30; H = 30; install();
            at(300); G.coop = false;
            Baby.trapOk = true; Baby.home = p(5, 5);   // the rat (5, 5) in its home half (FWD_TRAP, yl7: skips the gates in the enemy half)
            global = c[0]; shared[Comms.BUDGET] = c[1];
            MapLocation e = p(5, 5 + c[3]);
            Baby.nearestEnemy = baby(50, Team.B, e, null);
            Comms.numKings = 1; Comms.kings[0] = c[2] == 1 ? p(5, 9) : p(25, 25);
            boolean laid = Baby.tryRatTrap();
            int want = Cfg.TRAP_CAP > 0 ? c[4] : 0;   // TRAP_CAP 0 (sw57, 11-23): no rat traps at all
            check(laid == (want == 1) && trapsLaid.size() == want, "trap gates: cheese " + c[0] + " budget " + c[1]
                  + (c[2] == 1 ? " near our king" : "") + ": " + (laid ? "trap at " + trapsLaid : "none"));
        }
        // too far for a trap
        reset(); W = 30; H = 30; install(); at(300); G.coop = false; Baby.home = p(5, 5);
        global = 5000; shared[Comms.BUDGET] = 5; Comms.numKings = 1; Comms.kings[0] = p(25, 25);
        int far = 1;
        while (far * far <= Cfg.BABY_TRAP_DIST2) far++;
        Baby.nearestEnemy = baby(50, Team.B, p(5, 5 + far), null);
        check(!Baby.tryRatTrap(), "trap gates: no trap against an enemy beyond BABY_TRAP_DIST2");
        Comms.numKings = 0; Baby.nearestEnemy = null;
    }

    /** THREAT_THROW: an enemy baby with free hands within Chebyshev THREAT_THROW threatens a holding carrier. */
    static void testCarryThreat() {
        reset(); install();
        int c = Cfg.THREAT_THROW;
        if (c <= 0) return;
        MapLocation m = p(10, 10);
        check(Baby.carryThreat(new RobotInfo[] {baby(1, Team.B, p(10 + c, 10 - c), null)}, m), "carry threat: free hands at THREAT_THROW");
        check(!Baby.carryThreat(new RobotInfo[] {baby(1, Team.B, p(10 + c + 1, 10), null)}, m), "carry threat: one tile further is none");
        check(!Baby.carryThreat(new RobotInfo[] {baby(1, Team.B, p(10 + c, 10), baby(2, Team.A, p(10 + c, 10), null))}, m),
              "carry threat: an enemy already carrying cannot grab");
        check(!Baby.carryThreat(new RobotInfo[] {baby(1, Team.A, p(10 + c, 10), null)}, m), "carry threat: an ally is none");
    }

    /** CAT_FEED: a cat first on the flight scores CAT_FEED at tiles 1-2, 60% at 3-4 and 30% beyond. */
    static void testFeedScore() {
        int f = Cfg.CAT_FEED;
        check(Baby.feedScore(1) == f && Baby.feedScore(2) == f, "feed score: point-blank is CAT_FEED");
        check(Baby.feedScore(3) == f * 6 / 10 && Baby.feedScore(4) == f * 6 / 10, "feed score: 60% at 3-4");
        check(Baby.feedScore(5) == f * 3 / 10 && Baby.feedScore(8) == f * 3 / 10, "feed score: 30% beyond");
    }

    /** ATK_STUCK: an attacker holding ATK_STUCK_RAW raw whose walk stalled ATK_STUCK turns delivers until it has none. */
    static void testAtkStuck() {
        if (Cfg.ATK_STUCK <= 0 || Cfg.ATK_STUCK_RAW <= 0) return;
        reset(); install();
        Baby.role = Baby.ATTACK; Baby.stuckHome = false;
        Nav.stall = Cfg.ATK_STUCK - 1;
        check(!Baby.atkStuck(Cfg.ATK_STUCK_RAW), "atk stuck: not before ATK_STUCK stalled turns");
        Nav.stall = Cfg.ATK_STUCK;
        check(!Baby.atkStuck(Cfg.ATK_STUCK_RAW - 1), "atk stuck: not with less than ATK_STUCK_RAW raw");
        check(Baby.atkStuck(Cfg.ATK_STUCK_RAW), "atk stuck: stalled with a load: deliver");
        Nav.stall = 0;
        check(Baby.atkStuck(1), "atk stuck: keeps delivering while it holds any");
        check(!Baby.atkStuck(0) && !Baby.stuckHome, "atk stuck: done once it holds none");
        Baby.role = Baby.GATHER; Nav.stall = Cfg.ATK_STUCK;
        check(!Baby.atkStuck(Cfg.ATK_STUCK_RAW * 2), "atk stuck: attackers only");
        Nav.stall = 0;
    }

    /** SEEK_IDLE: a turn within SEEK_IDLE_R of the mine with no pickup since the last counts; the SEEK_IDLE-th is
     *  idle (and the count restarts); a pickup restarts it; turns further off do not count. */
    static void testIdle() {
        if (Cfg.SEEK_IDLE <= 0) return;
        reset(); W = 30; H = 30; install();
        MapLocation mine = p(12, 10);
        me = p(10, 10); at(200);
        Baby.idleTurns = 0; Baby.idlePicked = Baby.picked;
        boolean early = false;
        for (int i = 1; i < Cfg.SEEK_IDLE; i++) early |= Baby.idle(mine);
        check(!early && Baby.idle(mine) && Baby.idleTurns == 0, "idle: the SEEK_IDLE-th turn near the mine is idle");
        for (int i = 1; i < Cfg.SEEK_IDLE; i++) Baby.idle(mine);
        Baby.picked++;
        check(!Baby.idle(mine) && Baby.idleTurns == 1, "idle: a pickup restarts the count");
        me = p(12 + Cfg.SEEK_IDLE_R + 1, 10); at(201);
        int n = Baby.idleTurns;
        check(!Baby.idle(mine) && Baby.idleTurns == n, "idle: a turn beyond SEEK_IDLE_R does not count");
    }

    /** FIGHT_POST: at war a FIGHT rat takes its mine from the contested home-half mines (steps from the mirrored enemy
     *  start exceed those from home by at most FIGHT_POST); gatherers and peacetime picks are unfiltered. Only home-half
     *  mines are known here (the case every version handles). */
    static void testFightPostHome() {
        if (Cfg.FIGHT_POST <= 0) return;
        // home (3,3) on a 30x30 rotation map: the enemy start is (26,26). (5,5) is ours but 19 steps behind the
        // front; (12,12) and (14,10) are ours and contested (5 steps)
        MapLocation back = p(5, 5);
        Set<MapLocation> fight = new HashSet<>(), gather = new HashSet<>();
        for (int s = 0; s < 6; s++) {
            for (int role = 0; role < 2; role++) {
                reset(); W = 30; H = 30; install(); at(400);
                Sym.mask = Sym.ROT; G.coop = false;
                Baby.home = p(3, 3); Baby.role = role == 0 ? Baby.FIGHT : Baby.GATHER; Baby.postMargin = Cfg.FIGHT_POST;
                // slot bits 12-13 = 3: no raider (Cfg.RAID_SHARE, sw53 champion 11-23). With slot = s (bits 0) every
                // rat here raided under RAID_SHARE 2 and took the enemy twin of its mine, on purpose: RaidTest covers
                // raiders. s | 3 << 12 keeps the picks (12288 is a multiple of 2 and 3)
                Baby.myMineCount = 0; Baby.mineShift = 0; Baby.slot = s | 3 << 12;
                Comms.numMines = 0;
                for (MapLocation m : new MapLocation[] {back, p(12, 12), p(14, 10)}) Comms.mines[Comms.numMines++] = m;
                MapLocation pick = Baby.pickMine();
                (role == 0 ? fight : gather).add(pick);
            }
        }
        check(!fight.contains(back) && fight.size() == 2, "fight post: fighters at war take only the contested home mines " + fight);
        // RAID_SHARE 4 (sw55) leaves no slot that does not raid: every gatherer at war takes a twin, so skip
        if (RaidTest.share() < 4) check(gather.contains(back), "fight post: gatherers spread over every mine " + gather);
        Comms.numMines = 0;
    }

    public static void main(String[] args) throws Exception {
        testRoles();
        testRushLife();
        testThrowDodge();
        testBabyCap();
        testSpawnFloors();
        testReserve();
        testAustere();
        testTrapGates();
        testCarryThreat();
        testFeedScore();
        testAtkStuck();
        testIdle();
        testFightPostHome();
        System.out.println("MechTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
