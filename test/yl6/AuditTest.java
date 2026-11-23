package ratbot;

import battlecode.common.*;
import java.util.*;

import static ratbot.BotTest.*;

/**
 * Tests for the 11-23 audit fixes in yl5 and yl6 (yl5 with WAR_GATHER 1; research/audit/2026-11-23-yk1.md). Run with
 * `bash tools/unit-tests.sh yl5`; when yl5 (or a descendant) is crowned, move this file to test/ratbot/.
 *
 * The latent fixes make each documented special value of a Cfg switch do what its doc says (yk1's values never use
 * them; sw35 did: RUSH_LIFE 0 gave it no rush). The active ones: FIGHT_POST filters a fighter's war mine to the
 * contested home-half mines whatever else the team knows; WAR_GATHER 0 sends war gatherers at enemies in reach;
 * the spawn's babies estimate subtracts every formed king; the coop endgame centre tile is mirrored;
 * the rescue bar counts the promotion; cat distances and the side frame are mirror-invariant; the throw dodge breaks
 * ties away from the carrier; TrapRisk allocates at the engine's price; a late scout seeks. Plain main; exit code 1
 * on failure. (Bite cheese: test/yl5/CheeseTest.java.)
 */
public class AuditTest {

    static MapLocation p(int x, int y) { return new MapLocation(x, y); }

    static void at(int r) { round = r; G.startTurn(); }

    // ------------------------------------------------------------------ latent: documented special values

    static void testRushSwitches() {
        reset(); install();
        check(!Baby.rushFailed(500, 0, -10000, 0) && !Baby.rushFailed(2, 0, -10000, 0),
              "RUSH_LIFE 0: no rusher ever gives up (yk1: every rusher gave up on its second turn)");
        check(Baby.rushFailed(200, 0, -10000, 150), "RUSH_LIFE 150: gives up after 150 rounds without the king");
        check(Baby.rushes(4, 1999, 4, 16, 0) && Baby.rushes(15, 5, 4, 16, 0), "RUSH_UNTIL 0: rush spawns never stop");
        check(!Baby.rushes(4, 200, 4, 16, 200) && Baby.rushes(4, 199, 4, 16, 200), "RUSH_UNTIL 200: rushers before round 200");
        check(!Baby.rushes(3, 5, 4, 16, 0) && !Baby.rushes(16, 5, 4, 16, 0), "rushes: only RUSH_FROM <= idx < RUSH_TO");
    }

    static void testBreakRound() {
        int g = Baby.GATHER, a = Baby.ATTACK;
        check(!Baby.hostile(true, g, 1999, 0), "BREAK_ROUND 0: a gatherer never bites during cooperation");
        check(Baby.hostile(true, a, 5, 0) && Baby.hostile(false, g, 5, 0), "BREAK_ROUND 0: attackers and war still bite");
        check(Baby.hostile(true, g, 30, 30) && !Baby.hostile(true, g, 29, 30), "BREAK_ROUND 30: from round 30");
    }

    static void testEndCatSwitches() {
        check(!Baby.endGo(false, 1999, -1, 0, 1000), "END_CAT 0: no war cat errand");
        check(!Baby.endGo(true, 1999, -1, 1400, 0), "END_CAT_COOP 0: no cooperation cat errand");
        check(Baby.endGo(true, 5, 100, 1400, 0), "END_CAT_COOP 0: a stalled gatherer's MINE_STALL_CAT errand still runs");
        check(Baby.endGo(false, 1400, -1, 1400, 0) && !Baby.endGo(false, 1399, -1, 1400, 0), "END_CAT 1400: from round 1400");
        check(Baby.endGo(true, 1000, -1, 0, 1000) && !Baby.endGo(true, 999, -1, 0, 1000), "END_CAT_COOP 1000: from round 1000");
    }

    static void testScoutSwitch() {
        boolean none = true;
        for (int rr = -1; rr < 40; rr++) none &= Baby.scoutPlace(rr, 0) == -1;
        check(none, "SCOUT_EVERY 0: no scouts (yk1: % 0 threw at every gatherer's birth)");
        check(Baby.scoutPlace(1, 4) == -1 && Baby.scoutPlace(2, 4) == 0 && Baby.scoutPlace(3, 4) == 1,
              "SCOUT_EVERY 4: gatherers 2 and 3 take the flanks");
        check(Baby.scoutPlace(8, 4) == 2 && Baby.scoutPlace(12, 4) == 3 && Baby.scoutPlace(9, 4) == -1
              && Baby.scoutPlace(4, 4) == -1, "SCOUT_EVERY 4: then every 4th from 8");
    }

    static void testStallCatSwitch() throws GameActionException {
        reset(); install();
        Baby.role = Baby.ATTACK; Baby.catHunt = false; Baby.stallAt = null; Baby.cat = null;
        int keep = Baby.stallCatRounds;
        Baby.stallCatRounds = 0;
        boolean any = false;
        for (int r = 10; r < 400; r += 10) { at(r); any |= Baby.stallCat(); }
        check(!any && !Baby.catHunt, "STALL_CAT 0: an attacker standing still never turns cat hunter");
        Baby.stallCatRounds = keep;
    }

    static void testAtkStuckSwitch() {
        reset(); install();
        Baby.role = Baby.ATTACK; Baby.stuckHome = false; Nav.stall = 1000;
        check(!Baby.atkStuck(500, 0, 100), "ATK_STUCK 0: a stalled attacker with a load keeps attacking");
        check(Baby.atkStuck(500, 30, 100), "ATK_STUCK 30: it delivers");
        Baby.stuckHome = false; Nav.stall = 0;
    }

    static void testWallBlindCap() {
        reset(); W = 30; H = 30; install(); at(50);
        for (int x = 0; x < 30; x++) Sym.record(x, 8, true, false, false);   // a wall row between (5,5) and (5,12)
        int keep = Baby.wallBlindCap;
        Baby.wallBlindCap = 1;
        boolean first = Baby.walled(p(5, 5), p(5, 12)), second = Baby.walled(p(5, 5), p(5, 12));
        check(first && !second, "WALL_BLIND_CAP 1: the second trace in a round counts as open");
        Baby.wallBlindCap = 0;
        boolean all = true;
        for (int i = 0; i < 20; i++) all &= Baby.walled(p(5, 5), p(5, 12));
        check(all, "WALL_BLIND_CAP 0: no cap (yk1: every trace counted as open)");
        Baby.wallBlindCap = keep;
    }

    static void testCarryCap() {
        check(Baby.carryRoom(50, 200) == 150 && Baby.carryRoom(200, 200) == 0, "CARRY_CAP 200: room is the cap less raw");
        check(Baby.carryRoom(5000, 0) == Integer.MAX_VALUE, "CARRY_CAP 0: no cap (yk1: non-attackers never picked up)");
    }

    static void testSeekIdleSwitch() {
        reset(); W = 30; H = 30; install();
        me = p(10, 10); at(200);
        Baby.idleTurns = 0; Baby.idlePicked = Baby.picked;
        boolean any = false;
        for (int i = 0; i < 100; i++) any |= Baby.idle(p(11, 10), 0, 4);
        check(!any, "SEEK_IDLE 0: never idle (yk1: idle on every turn near the mine)");
        boolean twentieth = false;
        for (int i = 1; i <= 20; i++) twentieth = Baby.idle(p(11, 10), 20, 4);
        check(twentieth, "SEEK_IDLE 20: the 20th turn near the mine is idle");
    }

    static void testKingSwitches() {
        reset(); install();
        shared[Comms.BUDGET] = 0; at(500);
        check(!King.austere(0) && King.austere(60), "AUSTERE_FROM 0: never austere (yk1: austere from round 0)");
        global = 100000; Comms.numKings = 1; at(100);
        check(!King.rich(0, 4), "FORM_RICH_BANK 0: no rich kings (yk1: rich kings from any bank)");
        check(King.rich(3000, 4), "FORM_RICH_BANK 3000: a rich bank names another site");
        int cheese = Math.max(Cfg.END_KING_CHEESE, 1);
        check(!King.endKing(1950, 1, 9, cheese, -10000, 0), "END_KING 0: no endgame king (yk1: from round 0)");
        check(King.endKing(1950, 1, 9, cheese, -10000, 1850), "END_KING 1850: a lone king's endgame site");
        Comms.numKings = 0;
    }

    static void testWanderR() {
        check(Baby.wanderR() == (Cfg.WANDER_R > 0 ? Cfg.WANDER_R : 3), "WANDER_R: 0 means 3 in both places that use it");
    }

    // ------------------------------------------------------------------ active: FIGHT_POST

    /** Picks for slots 0-7 of a FIGHT or GATHER rat at war (or in cooperation) from the listed mines; home (3,3) on a
     *  30x30 rotation map, the enemy start (26,26). */
    static Set<MapLocation> picks(int role, boolean coop, MapLocation... mines) {
        Set<MapLocation> out = new HashSet<>();
        for (int s = 0; s < 8; s++) {
            reset(); W = 30; H = 30; install(); at(400);
            Sym.mask = Sym.ROT; G.coop = coop;
            Baby.home = p(3, 3); Baby.role = role; Baby.postMargin = Cfg.FIGHT_POST;
            Baby.myMineCount = 0; Baby.mineShift = 0; Baby.slot = s;
            Comms.numMines = 0;
            for (MapLocation m : mines) Comms.mines[Comms.numMines++] = m;
            out.add(Baby.pickMine());
        }
        Comms.numMines = 0;
        return out;
    }

    static void testFightPost() {
        if (Cfg.FIGHT_POST <= 0) return;
        // ours: (5,5) 19 steps behind the front, (12,12) and (14,10) contested (5 steps); their twins on the far half
        MapLocation back = p(5, 5), c1 = p(12, 12), c2 = p(14, 10);
        MapLocation[] all = {back, c1, c2, G.mirror(back, Sym.ROT), G.mirror(c1, Sym.ROT), G.mirror(c2, Sym.ROT)};
        Set<MapLocation> f = picks(Baby.FIGHT, false, all);
        check(f.equals(new HashSet<>(Arrays.asList(c1, c2))),
              "FIGHT_POST: with the twins listed, fighters at war still take only contested home mines " + f
              + " (yk1: any of the six)");
        Set<MapLocation> g = picks(Baby.GATHER, false, all);
        check(g.size() >= 4, "FIGHT_POST: gatherers still spread over every mine " + g);
        Set<MapLocation> peace = picks(Baby.FIGHT, true, all);
        check(peace.size() >= 4, "FIGHT_POST: fighters in cooperation spread over every mine " + peace);
        Set<MapLocation> noContest = picks(Baby.FIGHT, false, back, G.mirror(back, Sym.ROT));
        check(noContest.equals(Collections.singleton(back)), "FIGHT_POST: no contested home mine: any home mine " + noContest);
        Set<MapLocation> noHome = picks(Baby.FIGHT, false, G.mirror(back, Sym.ROT), G.mirror(c1, Sym.ROT));
        check(noHome.size() == 2, "FIGHT_POST: no home mine known: any mine " + noHome);
    }


    // ------------------------------------------------------------------ active: the rest

    static void testLiveBabies() {
        reset(); install();
        shared[Comms.FORMED] = 0;
        check(King.liveBabies(10) == 0 && King.liveBabies(40) == 12, "liveBabies: 4 per cost step");
        shared[Comms.FORMED] = 2; Comms.numKings = 1;   // two formed kings, both dead: their phantoms stay
        check(King.liveBabies(40) == 10, "liveBabies: a dead formed king's phantom stays subtracted (yk1: 12)");
        check(King.liveBabies(20) == 2 && King.liveBabies(10) == 0, "liveBabies: never below 0");
        shared[Comms.FORMED] = 0; Comms.numKings = 0;
    }

    static void testFormedCount() throws GameActionException {
        reset(); W = 30; H = 30; install();
        type = UnitType.RAT_KING; me = p(10, 10);
        King.slot = -1; round = 300; G.startTurn();
        try { King.turn(); } catch (RuntimeException e) { /* the fake may lack a later call: the count comes first */ }
        check(shared[Comms.FORMED] == 1, "FORMED: a king's first turn after round 1 counts a promotion");
        King.slot = -1; shared[Comms.FORMED] = 0;
    }

    static void testCashBar() {
        int r = 1880, plain = 4 * (2000 - r - 55) + 10;
        check(Baby.cashBar(r) == Math.max(Cfg.CASH_FLOOR, plain + 50), "cashBar: both kings' upkeep plus the promotion's 50");
    }

    /** f(x, y) == f(mirror) for every cat corner near the king, under each symmetry (W x H = 30 x 24). */
    static void testCatDistMirror() {
        reset(); W = 30; H = 24; install();
        int[] syms = {Sym.ROT, Sym.HOR, Sym.VER};
        boolean atKing = true, body = true;
        MapLocation king = p(10, 9);
        for (int s : syms) {
            MapLocation mk = G.mirror(king, s);
            for (int x = 0; x < W - 1; x++)
                for (int y = 0; y < H - 1; y++) {
                    MapLocation c = p(x, y);
                    // the twin cat's body is the mirror of {x, x+1} x {y, y+1}: its corner is the low corner of that
                    MapLocation a = G.mirror(c, s), b = G.mirror(p(x + 1, y + 1), s);
                    MapLocation mc = p(Math.min(a.x, b.x), Math.min(a.y, b.y));
                    Comms.numKings = 1; Comms.kings[0] = king;
                    boolean here = Baby.catAtKing(c);
                    Comms.kings[0] = mk;
                    atKing &= here == Baby.catAtKing(mc);
                    body &= Baby.chebToBody(c, king) == Baby.chebToBody(mc, mk)
                            && Baby.catDist4(c, king) == Baby.catDist4(mc, mk);
                }
        }
        Comms.numKings = 0;
        check(atKing, "catAtKing: the twin cat at the twin king gives the same answer (yk1: not at the dist2 edge)");
        check(body, "cat body distances: mirror-invariant under every symmetry");
    }

    static void testFrame() {
        reset(); W = 45; H = 35; install();   // thunderdome: kings (18,17) and (26,17), rotation
        int[] f = new int[4];
        for (int side = 0; side < 2; side++) {
            MapLocation h = side == 0 ? p(18, 17) : p(26, 17);
            Baby.framePx = 2 * h.x - G.cx2; Baby.framePy = 2 * h.y - G.cy2;
            Sym.mask = Sym.ROT; Baby.frame();
            f[2 * side] = Baby.fu; f[2 * side + 1] = Baby.fv;
        }
        check(f[0] == -f[2] && f[1] == -f[3], "frame: thunderdome's twins get opposite signs on both axes under rotation (yk1: fv +1 for both)");
        Baby.framePx = 2 * 18 - G.cx2; Baby.framePy = 0;
        Sym.mask = Sym.VER; Baby.frame(); int va = Baby.fv;
        Baby.framePx = 2 * 26 - G.cx2; Baby.frame();
        check(va == Baby.fv, "frame: under the reflection that keeps the middle row, both keep fv");
        Sym.mask = Sym.ROT | Sym.VER; Baby.framePx = 2 * 18 - G.cx2; Baby.frame();
        check(Baby.fv == 1 && Baby.frameMask == (Sym.ROT | Sym.VER), "frame: unknown symmetry: +1, set again when it resolves");
        Baby.framePx = 2 * 10 - G.cx2; Baby.framePy = 2 * 5 - G.cy2; Sym.mask = Sym.ROT; Baby.frame();
        check(Baby.fu == -1 && Baby.fv == -1, "frame: off the centre lines, the signs of the offset as before");
        Sym.mask = Sym.ROT | Sym.HOR | Sym.VER;
    }

    static void testThrowDodgeTies() throws GameActionException {
        if (Cfg.THROW_DODGE < 5) return;
        Direction[] got = new Direction[2];
        for (int side = 0; side < 2; side++) {
            reset(); W = 30; H = 30; install();
            type = UnitType.RAT_KING; me = p(10, 10); at(300); G.coop = false;
            int ox = side == 0 ? 1 : -1;   // a carrier 3 north and 1 east, then its mirror 1 west
            MapLocation cl = p(10 + ox, 13);
            RobotInfo carrier = new RobotInfo(8, Team.B, UnitType.BABY_RAT, 100, cl, Direction.SOUTH, 0, 0,
                                              new RobotInfo(7, Team.A, UnitType.BABY_RAT, 100, cl, Direction.SOUTH, 0, 0, null));
            robots.put(cl, carrier);
            got[side] = King.throwDodge(me, new RobotInfo[] {carrier});
        }
        check(got[0] == Direction.SOUTHWEST, "throw dodge: among equal steps, the one farthest from the carrier (" + got[0] + "; yk1: EAST)");
        check(got[1] == Direction.SOUTHEAST, "throw dodge: the mirrored carrier gets the mirrored step (" + got[1] + ")");
    }

    static void testTrapRiskAlloc() {
        reset(); W = 60; H = 60; install(); at(400); G.coop = false;
        TrapRisk.mark = null;
        clock(W * H + Cfg.BC_OPTIONAL + 200);
        TrapRisk.observe(new RobotInfo[0]);
        check(TrapRisk.mark != null, "TrapRisk: a 60x60 map's marks fit a baby's turn (yk1 asked 3 per tile: 18,800 > 17,500)");
        clock(Integer.MAX_VALUE / 2);
    }

    static void testFeedOff() {
        check(Baby.feedScore(1, 0) == -50 && Baby.feedScore(6, 0) == -50, "CAT_FEED 0: a cat on the flight scores -50");
        check(Baby.feedScore(1, 150) == 150 && Baby.feedScore(3, 150) == 90 && Baby.feedScore(6, 150) == 45, "feedScore: 100/60/30%");
    }

    static void testLateScoutSeeks() throws GameActionException {
        if (Cfg.SCOUT_FAN <= 0) return;
        reset(); W = 30; H = 30; install();
        me = p(6, 6); at(400);
        Sym.mask = Sym.ROT;
        Baby.home = p(3, 3); Baby.role = Baby.GATHER; Baby.scout = true; Baby.fanBase = 0; Baby.fanStep = 0;
        Baby.exploreTarget = null;
        Flow.fhx = Flow.fhy = Flow.fex = Flow.fey = Flow.fax = Flow.fay = 0;   // fanFrontier never ran
        boolean moved = Baby.exploreHome();
        check(moved && Baby.exploreTarget != null && Baby.homeHalf(Baby.exploreTarget),
              "exploreHome: a scout that never scouted still finds a home-half target (yk1: none, so it never sought)");
        Baby.scout = false; Sym.mask = Sym.ROT | Sym.HOR | Sym.VER;
    }


    /** WAR_GATHER (active in yk1: 0 played as 1): a gatherer at war with an enemy rat 2 tiles off, which sees it. */
    static void testWarGather() throws GameActionException {
        boolean[] engaged = new boolean[2];
        for (int wg = 0; wg <= 1; wg++) {
            reset(); W = 30; H = 30; install();
            me = p(10, 10); at(400); G.coop = false; health = 100;
            Baby.role = Baby.GATHER; Baby.hunting = false; Baby.eKing = null; Baby.home = p(3, 3);
            RobotInfo e = new RobotInfo(9, Team.B, UnitType.BABY_RAT, 100, p(12, 12), Direction.SOUTHWEST, 0, 0, null);
            robots.put(e.location, e);
            Baby.nearestEnemy = e;
            Baby.warGather = wg;
            engaged[wg] = Baby.war();
        }
        Baby.warGather = Cfg.WAR_GATHER; Baby.nearestEnemy = null;
        check(engaged[0], "WAR_GATHER 0: a gatherer at war engages an enemy 2 tiles off (yk1: kept gathering)");
        check(!engaged[1], "WAR_GATHER 1: a gatherer keeps gathering unless the enemy is adjacent");
    }

    /** The coop endgame cat target: twin homes get mirrored centre tiles, even and odd sides. */
    static void testCentreTile() {
        boolean ok = true;
        for (int[] wh : new int[][] {{30, 24}, {31, 25}, {45, 35}}) {
            reset(); W = wh[0]; H = wh[1]; install();
            for (int s : new int[] {Sym.ROT, Sym.HOR, Sym.VER})
                for (MapLocation h : new MapLocation[] {p(3, 4), p(W - 5, 2), p(7, H - 3)}) {
                    MapLocation t = Baby.centreTile(h, G.cx2, G.cy2), u = Baby.centreTile(G.mirror(h, s), G.cx2, G.cy2);
                    ok &= G.mirror(t, s).equals(u);
                }
        }
        check(ok, "centreTile: twin homes get mirrored centre tiles on even and odd sides (yk1: (cx2/2, cy2/2) for both)");
    }

    public static void main(String[] args) throws Exception {
        testRushSwitches();
        testBreakRound();
        testEndCatSwitches();
        testScoutSwitch();
        testStallCatSwitch();
        testAtkStuckSwitch();
        testWallBlindCap();
        testCarryCap();
        testSeekIdleSwitch();
        testKingSwitches();
        testWanderR();
        testFightPost();
        testLiveBabies();
        testFormedCount();
        testCashBar();
        testCatDistMirror();
        testFrame();
        testThrowDodgeTies();
        testTrapRiskAlloc();
        testFeedOff();
        testLateScoutSeeks();
        testWarGather();
        testCentreTile();
        System.out.println("AuditTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
