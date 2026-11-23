package ratbot;

import battlecode.common.*;
import java.util.*;

import static ratbot.BotTest.*;

/**
 * Tests for the audited fixes (ids from research/audit/findings-*.md and the bc26a0 pass 2B list). Each test drives
 * the helper the fix lives in through BotTest's fake RobotController. Plain main; exit code 1 on failure.
 */
public class FixTest {

    static MapLocation p(int x, int y) { return new MapLocation(x, y); }

    static RobotInfo bot(int id, Team t, UnitType ut, MapLocation l, Direction d) {
        return new RobotInfo(id, t, ut, ut == UnitType.RAT_KING ? 500 : 100, l, d, 0, 0, null);
    }

    static void kingWorld() {
        reset(); install();
        type = UnitType.RAT_KING; G.startTurn();
    }

    static void at(int r) { round = r; G.startTurn(); }

    // ------------------------------------------------------------------ king and shared state

    /** K3/S1: stamps alias after 1024 rounds; King.sweep zeroes stale KING/ALERT/CAT/FORM_T stamps. */
    static void testK3Sweep() {
        kingWorld();
        at(5);
        check(Comms.stamp() == 5, "K3: stamp is round & 1023");
        at(1024);
        check(Comms.stamp() == 1023, "K3: round 1024 stamps 1023, never 0 (" + Comms.stamp() + ")");
        // the aliasing the sweep exists for: a round-5 stamp reads fresh again at 1029
        at(1029);
        check(Comms.fresh(5, 2), "K3: a stamp 1024 rounds old reads fresh (the aliasing)");
        at(7);
        check(Comms.fresh(5, 2) && !Comms.fresh(0, 1000), "K3: fresh within maxAge; 0 never fresh");
        at(8);
        check(!Comms.fresh(5, 2), "K3: older than maxAge is stale");

        // a dead king in slot 0 (stamp 5), a live one in slot 1, stale ALERT / CAT / FORM_T
        kingWorld();
        at(5);
        int s0 = Comms.KING0, s1 = Comms.KING0 + 3;
        shared[s0] = 4; shared[s0 + 1] = 4; shared[s0 + 2] = Comms.stamp();
        shared[Comms.ALERT] = 4; shared[Comms.ALERT + 1] = 4; shared[Comms.ALERT + 2] = Comms.stamp();
        shared[Comms.CAT0 + 2] = Comms.stamp(); shared[Comms.CAT0 + 5] = Comms.stamp(); shared[Comms.FORM_T] = Comms.stamp();
        at(5 + 63);
        shared[s1] = 9; shared[s1 + 1] = 9; shared[s1 + 2] = Comms.stamp();
        King.sweep();
        check(shared[s0 + 2] == 5 && shared[Comms.FORM_T] == 5, "K3: a stamp exactly 63 rounds old stays");
        at(5 + 64);
        King.sweep();
        check(shared[s0 + 2] == 0 && shared[Comms.ALERT + 2] == 0 && shared[Comms.CAT0 + 2] == 0 && shared[Comms.CAT0 + 5] == 0
              && shared[Comms.FORM_T] == 0, "K3: stamps 64 rounds old are zeroed");
        check(shared[s1 + 2] == 5 + 63 && shared[s0] == 4, "K3: fresh stamps and the location words stay");
        // 1024 rounds on, the dead king no longer comes back
        at(5 + 1024);
        shared[s1 + 2] = Comms.stamp();
        Comms.readKings();
        check(Comms.numKings == 1 && Comms.kings[0].equals(p(8, 8)), "K3: after the sweep a dead slot never reads live again ("
              + Comms.numKings + ")");
        // without the sweep it would have (the aliasing, shown on an unswept copy)
        shared[s0 + 2] = 5;
        Comms.readKings();
        check(Comms.numKings == 2, "K3: an unswept round-5 stamp is a phantom king at 1029");
        // a zeroed slot is free for a new king
        shared[s0 + 2] = 0;
        King.slot = -1; King.claimSlot();
        check(King.slot == 0, "K3: a swept slot is claimed again");
        King.slot = -1;
    }

    /** K2: the lowest slot with a fresh heartbeat leads the formation (slot 0 dead: the next one). */
    static void testK2LeadSlot() throws GameActionException {
        kingWorld();
        at(200);
        int s0 = Comms.KING0, s1 = Comms.KING0 + 3;
        King.slot = 2;
        shared[s0] = 3; shared[s0 + 1] = 3; shared[s0 + 2] = 150;   // dead
        shared[s1] = 9; shared[s1 + 1] = 9; shared[s1 + 2] = 199;   // live
        check(King.leadSlot() == 1, "K2: slot 0 dead, slot 1 live: slot 1 leads (" + King.leadSlot() + ")");
        shared[s1 + 2] = 190;
        check(King.leadSlot() == 2, "K2: every lower slot dead: we lead");
        shared[s0 + 2] = 200;
        check(King.leadSlot() == 0, "K2: a live slot 0 leads");
        shared[s0] = 0;
        check(King.leadSlot() == 2, "K2: an empty slot does not lead");
        // formation() runs for the leader only: at war before FORM_WAR_FROM it clears FORM_T (observable)
        King.slot = 1; G.coop = false;
        G.round = Math.max(1, Cfg.FORM_WAR_FROM - 1);
        Comms.numKings = 2;
        shared[s0] = 3; shared[s0 + 2] = 0;    // slot 0 dead (swept)
        shared[s1] = 9; shared[s1 + 1] = 9; shared[s1 + 2] = Comms.stamp();
        shared[Comms.FORM_T] = 77;
        King.formation();
        check(Cfg.FORM_WAR_FROM <= 1 || shared[Comms.FORM_T] == 0, "K2: slot 1 runs formation() once slot 0 is dead");
        shared[s0 + 2] = Comms.stamp();
        shared[Comms.FORM_T] = 77;
        King.formation();
        check(shared[Comms.FORM_T] == 77, "K2: slot 1 leaves formation() to a live slot 0");
        King.slot = -1;
    }

    /** K1: KPath.held skips our own turn-start centre (Comms.kings is read before the king moves). */
    static void testK1Held() {
        reset(); W = 30; H = 30; install();
        Comms.kings[0] = p(10, 10); Comms.kings[1] = p(20, 10); Comms.numKings = 2;
        King.turnC = p(10, 10);
        MapLocation moved = p(11, 10);
        check(!KPath.held(p(12, 10), moved), "K1: our old centre does not hold a mine near it after we moved");
        check(KPath.held(p(19, 11), moved), "K1: another king still holds its mine");
        check(!KPath.held(p(15, 15), moved), "K1: a mine no king is near is free");
        // a king that did not move is skipped as before (me equals its slot)
        King.turnC = p(20, 10);
        check(!KPath.held(p(19, 11), p(20, 10)) && KPath.held(p(12, 10), p(20, 10)), "K1: from the other king's side");
        Comms.numKings = 0;
    }

    /** K7: the spawn's babies estimate subtracts promotions: two kings at the core count boot one more. yk1 counts
     *  them as numKings - 1; from yl5 as the shared FORMED counter (index 20, unused before yl5), set here too. */
    static void testK7Babies() throws GameActionException {
        for (int kings = 1; kings <= 2; kings++) {
            reset(); W = 20; H = 20; install();   // 400 tiles: core = CORE_SMALL
            type = UnitType.RAT_KING; me = p(10, 10); round = 2; G.startTurn(); G.coop = true;
            ratCost = 10 + 10 * Cfg.CORE_SMALL / 4;   // babies estimate = CORE_SMALL with one king
            global = ratCost + kings * Math.max(Cfg.CORE_FLOOR, Cfg.KING_RESERVE) + 10;
            Comms.numKings = kings;
            shared[20] = kings - 1;   // Comms.FORMED from yl5: the promotions so far
            Econ.n = 0; Econ.sum = 0;
            King.spawn(new RobotInfo[0], new RobotInfo[0]);
            if (kings == 1) check(built.isEmpty(), "K7: one king at the core count does not boot a spawn (" + King.spawnWhy + ")");
            else check(built.size() == 1, "K7: two kings: one baby promoted, the core boot spawns (" + King.spawnWhy + ")");
        }
        Comms.numKings = 0;
    }

    /** KING_NEAR_CHEESE: the king's nearCheese skips piles under its own 3x3. */
    static void testKingNearCheese() throws GameActionException {
        kingWorld();
        King.start = me; King.homeMine = null;
        cheeseAt.put(p(5, 6), 5);
        cheeseAt.put(p(7, 7), 5);
        King.sense();
        check(p(7, 7).equals(King.nearCheese), "KING_NEAR_CHEESE: the pile under the 3x3 is skipped for one 2 away (" + King.nearCheese + ")");
        cheeseAt.remove(p(7, 7));
        King.sense();
        check(King.nearCheese == null, "KING_NEAR_CHEESE: only a pile under the 3x3: none");
    }

    /** KING_PICKUP_INCOME: every pickUp counts its cheese as Econ income. */
    static void testKingPickupIncome() throws GameActionException {
        kingWorld();
        cheeseAt.put(p(4, 4), 7); cheeseAt.put(p(6, 5), 3); cheeseAt.put(p(8, 5), 50);
        Econ.pending = 0;
        King.pickUp();
        check(Econ.pending == 10 && pickedUp.size() == 2, "KING_PICKUP_INCOME: the 3x3's piles count as income (" + Econ.pending + ")");
    }

    /** KING_BITE_KING: the king bites an enemy king on a body tile in reach, not only its centre. */
    static void testKingBiteKing() throws GameActionException {
        kingWorld();
        G.coop = false;
        RobotInfo ek = bot(77, Team.B, UnitType.RAT_KING, p(7, 5), Direction.WEST);
        for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) robots.put(p(7 + dx, 5 + dy), ek);
        King.bite(new RobotInfo[]{ ek });
        check(bitten.size() == 1 && G.cheb(bitten.get(0), ek.location) == 1, "KING_BITE_KING: a body tile is bitten (" + bitten + ")");
    }

    /** ALERT_MAX lives in King.turn (whole king turn): not unit-tested. FLOW_HELD: Flow.start resets held. */
    static void testFlowHeldReset() {
        reset(); install();
        Flow.held = 2;
        Flow.start(p(20, 10), false);
        check(Flow.held == 0, "FLOW_HELD: a new search starts with held 0");
        Flow.building = false; Flow.tgt = null;
    }

    // ------------------------------------------------------------------ baby economy / reporting

    static void babyWorld() {
        reset(); install();
        Baby.myMineCount = 0; Baby.cat = null; Baby.catSeen = null; Baby.seen = new RobotInfo[0];
        Baby.heardTo = 0; Comms.numMines = 0; Comms.numKings = 0; G.coop = true;
    }

    static void listMines(int n) {
        Comms.numMines = 0;
        for (int i = 0; i < n; i++) Comms.mines[Comms.numMines++] = p(20 + i % 8, 2 + i / 8);
    }

    /** K5/S5/E3: with the mine list full, no mine is unreported, and report() squeaks no SQ_MINE. */
    static void testK5Full() {
        babyWorld();
        Baby.myMineCount = 0; Baby.noteMine(3, 3);
        listMines(Comms.MINE_SLOTS - 1);
        check(Baby.unreported() == 3 * 64 + 3, "K5: a list with room: our mine is news");
        listMines(Comms.MINE_SLOTS);
        check(Baby.unreported() == -1, "K5: a full list: nothing unreported");
        // report() beside a king: the near loop squeaks nothing either
        Comms.kings[0] = p(6, 6); Comms.numKings = 1;
        at(20);
        Baby.report();
        check(squeaks.isEmpty(), "K5: a full list: report() beside the king squeaks nothing (" + squeaks + ")");
        listMines(Comms.MINE_SLOTS - 1);
        Baby.report();
        check(squeaks.size() == 1 && Comms.sqType(squeaks.get(0)) == Comms.SQ_MINE && Comms.sqX(squeaks.get(0)) == 3,
              "K5: with room, report() beside the king squeaks the mine");
        // away from the king: the unreported() squeak
        squeaks.clear(); Comms.kings[0] = p(25, 15);
        listMines(Comms.MINE_SLOTS);
        Baby.report();
        check(squeaks.isEmpty(), "K5: a full list: report() away from the king squeaks nothing");
        Comms.numKings = 0; Comms.numMines = 0;
    }

    /** C4: report() squeaks nothing while a cat (2x2 from its low corner) is within dist2 16. */
    static void testC4CatEarshot() {
        babyWorld();
        Baby.noteMine(3, 3);
        listMines(4);
        Comms.kings[0] = p(25, 15); Comms.numKings = 1;
        at(20);
        Baby.cat = bot(900, Team.NEUTRAL, UnitType.CAT, p(9, 5), Direction.WEST);   // nearest cat tile (9,5): dist2 16
        check(Baby.catInEarshot(p(9, 5), me) && !Baby.catInEarshot(p(10, 5), me) && Baby.catInEarshot(p(2, 2), me) && !Baby.catInEarshot(p(1, 1), me),
              "C4: earshot is dist2 16 to the cat's nearest tile");
        Baby.report();
        check(squeaks.isEmpty() && heardAsked.isEmpty(), "C4: a cat in earshot: report() returns before squeaking or reading");
        Baby.cat = bot(900, Team.NEUTRAL, UnitType.CAT, p(10, 5), Direction.WEST);
        Baby.report();
        check(squeaks.size() == 1 && Comms.sqType(squeaks.get(0)) == Comms.SQ_MINE, "C4: the cat out of earshot: the squeak goes");
        Comms.numKings = 0; Comms.numMines = 0; Baby.cat = null;
    }

    /** E6: report() reads every round's squeaks since its last read (at most 4 back), not only the last round's. */
    static void testE6ReadMissed() {
        babyWorld();
        heard.put(10, List.of(new Message(Comms.squeak(Comms.SQ_MINE, 12, 13, 0), 5, 10, p(6, 6))));
        heard.put(11, List.of(new Message(Comms.squeak(Comms.SQ_MINE, 14, 15, 0), 5, 11, p(6, 6))));
        Baby.heardTo = 9;
        at(13);
        Baby.readMissed();
        check(heardAsked.equals(List.of(10, 11, 12)), "E6: rounds 10-12 read after a read through 9 (" + heardAsked + ")");
        check(Baby.myMineCount == 2 && Baby.heardTo == 12, "E6: both rounds' mines noted (" + Baby.myMineCount + ")");
        heardAsked.clear();
        at(14);
        Baby.readMissed();
        check(heardAsked.equals(List.of(13)), "E6: the next turn reads only the new round");
        heardAsked.clear();
        at(40);
        Baby.readMissed();
        check(heardAsked.equals(List.of(36, 37, 38, 39)), "E6: at most 4 rounds back (" + heardAsked + ")");
        // through report(): a list with mines reads the missed rounds
        heardAsked.clear(); listMines(2); Baby.heardTo = 37; at(41);
        Baby.report();
        check(heardAsked.equals(List.of(38, 39, 40)), "E6: report() catches up (" + heardAsked + ")");
        Comms.numMines = 0;
    }

    /** E2: wanderAround redraws a target under one of our kings (twice at most): ~36% of draws -> ~5%. */
    static void testE2Wander() throws GameActionException {
        babyWorld();
        W = 20; H = 20; install();
        MapLocation m = p(10, 10);
        Comms.kings[0] = p(11, 10); Comms.numKings = 1;
        me = p(4, 4); G.startTurn(); moveCd = 100;   // goTo never moves
        G.seed(12345);
        int under = 0, n = 400;
        for (int i = 0; i < n; i++) {
            Baby.exploreTarget = null;
            Baby.wanderAround(m);
            if (Nav.atKing(Baby.exploreTarget)) under++;
            check(G.cheb(Baby.exploreTarget, m) <= Cfg.WANDER_R, "E2: the draw stays within WANDER_R of the mine");
        }
        check(under * 100 < 12 * n, "E2: few targets under the king after the redraws (" + under + "/" + n + ")");
        Comms.numKings = 0;
    }

    /** E4: a rat stepping onto its rescue (CASH) centre promotes on that turn. FORM_PROMOTE: likewise on a formation. */
    static void testPromoteOnStep() throws GameActionException {
        babyWorld();
        Baby.cashSite = p(6, 5); canPromote = true; facing = Direction.EAST; G.startTurn();
        Baby.cashMove();
        check(me.equals(p(6, 5)) && promoted, "E4: stepped onto the rescue centre and promoted at once");
        Baby.cashSite = null;
        babyWorld();
        at(100);
        shared[Comms.FORM_X] = 7; shared[Comms.FORM_Y] = 6; shared[Comms.FORM_T] = Comms.stamp();   // site (6,5)
        canPromote = true; facing = Direction.EAST;
        check(Baby.formKing() && me.equals(p(6, 5)) && promoted, "FORM_PROMOTE: stepped onto the formation centre and promoted");
    }


    // ------------------------------------------------------------------ Baby.moveDecision branches

    /** A gatherer in cooperation at (5,5) on a 30x20 map with nothing in view, for Baby.moveDecision. */
    static void gatherer() {
        babyWorld();
        round = 100; G.startTurn();
        Baby.role = Baby.GATHER; Baby.home = p(3, 3); Baby.origin = null; Baby.nearestEnemy = null; Baby.rescueT = null;
        Baby.cashSite = null; Baby.scout = false; Baby.mineTarget = null; Baby.cheeseTarget = null;
        Baby.seekUntil = 0; Baby.stallCatUntil = -1; Baby.bfOn = false; Baby.idleTurns = 0; Baby.wanderRound = -10;
        Baby.wanderMine = null; Baby.birthRound = 0; Baby.exploreTarget = null; Baby.mineShift = 0;
        GrabMicro.round = -1;
        Sym.mask = Sym.ROT;
    }

    /** O2: a newborn's first turn without the bytecodes for pickMine (and the walk after it) waits a turn. */
    static void testO2BirthPick() throws GameActionException {
        int need = Cfg.NB_PICK_PAD + Cfg.NB_PICK_PER * 4;
        for (int k = 0; k < 3; k++) {
            gatherer();
            listMines(0);
            for (int i = 0; i < 4; i++) Comms.mines[Comms.numMines++] = p(8 + i, 6);
            if (k < 2) Baby.birthRound = G.round;
            int low = Baby.lowBc;
            clock(k == 1 ? need : need - 1);
            Baby.moveDecision();
            clock(1 << 30);
            if (k == 0) check(Baby.mineTarget == null && Baby.lowBc == low + 1, "O2: birth turn short of pickMine's bytecodes waits");
            if (k == 1) check(Baby.mineTarget != null && Baby.lowBc == low, "O2: birth turn with the bytecodes picks (" + Baby.why + ")");
            if (k == 2) check(Baby.mineTarget != null, "O2: a later turn picks whatever is left");
        }
        Comms.numMines = 0;
    }

    /** E5: the held wander chain survives up to 3 rounds between wanders. */
    static void testE5WanderChain() throws GameActionException {
        for (int gap = 3; gap <= 4; gap++) {
            gatherer();
            MapLocation m = p(10, 5);
            listMines(0); Comms.mines[Comms.numMines++] = m;
            Baby.mineTarget = m; Baby.minesAtPick = Comms.numMines;
            me = p(10 - Cfg.WANDER_R - 1, 5); G.startTurn();   // one past atMine's ring
            Baby.wanderMine = m; Baby.wanderRound = G.round - gap;
            Baby.moveDecision();
            if (gap == 3) check(Baby.why == 'W' && Baby.wanderRound == G.round, "E5: a wander 3 rounds back holds the chain (" + Baby.why + ")");
            else check(Baby.why == 'M', "E5: 4 rounds back the rat walks back to the mine (" + Baby.why + ")");
        }
        Comms.numMines = 0;
    }

    /** STALL_SHIFT: a mine walk stalled past 25 turns bumps mineShift, so the re-pick is a different mine. */
    static void testStallShift() throws GameActionException {
        gatherer();
        MapLocation m = p(20, 5);
        listMines(0); Comms.mines[Comms.numMines++] = m;
        Baby.mineTarget = m; Baby.minesAtPick = Comms.numMines;
        Nav.target = m; Nav.bestDist = 0; Nav.stall = 30; moveCd = 100;
        Baby.moveDecision();
        check(Baby.why == 'M' && Baby.mineTarget == null && Baby.mineShift == 1, "STALL_SHIFT: the stalled walk drops the mine and shifts (" + Baby.why + ")");
        Comms.numMines = 0;
    }

    /** E7: a rat standing on a pile it cannot take drops the cheese target. */
    static void testE7Pile() throws GameActionException {
        gatherer();
        Baby.cheeseTarget = me;
        Baby.moveDecision();
        check(Baby.cheeseTarget == null, "E7: standing on a pile we cannot take: target dropped (" + Baby.why + ")");
        gatherer();
        cheeseAt.put(p(8, 8), 5);
        Baby.cheeseTarget = p(8, 8);
        Baby.moveDecision();
        check(p(8, 8).equals(Baby.cheeseTarget) && Baby.why == 'C', "E7: a pile elsewhere is still walked to");
    }

    /** C3: a gatherer flees the cat's 2x2 centre, not its low corner (here the two pick different diagonals). */
    static void testC3CatCentre() throws GameActionException {
        gatherer();
        Baby.cat = bot(900, Team.NEUTRAL, UnitType.CAT, p(5, 3), Direction.WEST);   // body (5..6, 3..4), centre (5.5, 3.5)
        Baby.moveDecision();
        check(Baby.why == 'c' && me.equals(p(4, 6)), "C3: fled north-west, away from the centre (" + me + ", " + Baby.why + ")");
        Baby.cat = null;
    }

    /** C1: at war, a carrier of an enemy with a cat to feed does not flee that cat. */
    static void testC1NoFlee() throws GameActionException {
        for (int carry = 0; carry < 2; carry++) {
            gatherer();
            G.coop = false;
            Baby.cat = bot(900, Team.NEUTRAL, UnitType.CAT, p(5, 3), Direction.WEST);
            Baby.seen = new RobotInfo[]{ Baby.cat };
            if (carry == 1) { carrying = bot(40, Team.B, UnitType.BABY_RAT, me, Direction.NORTH); Baby.carryRound = G.round; }
            Baby.moveDecision();
            if (carry == 0) check(Baby.why == 'c', "C1: an empty gatherer flees the cat (" + Baby.why + ")");
            else check(Baby.why != 'c', "C1: carrying an enemy to feed it, no flee (" + Baby.why + ")");
        }
        Baby.cat = null; carrying = null; Baby.seen = new RobotInfo[0];
    }


    // ------------------------------------------------------------------ Baby.turn (bailing early on the fake clock)

    /** O1: a newborn's first turn bails under NB_BAIL bytecodes (not BC_BAIL): it already paid Sym.init. */
    static void testO1BirthBail() throws GameActionException {
        for (int k = 0; k < 2; k++) {
            gatherer();
            Baby.birthRound = k == 0 ? G.round : G.round - 1;
            int low = Baby.lowBc;
            clock((Cfg.BC_BAIL + Cfg.NB_BAIL) / 2);
            Baby.turn();
            clock(1 << 30);
            if (k == 0) check(Baby.lowBc == low + 1, "O1: the birth turn bails between BC_BAIL and NB_BAIL");
            else check(Baby.lowBc == low, "O1: a later turn with the same bytecodes goes on (" + Baby.why + ")");
        }
    }

    /** SWAP_HOLD: a carry held 12+ rounds after our last grab came from a role swap: its hold restarts now. */
    static void testSwapHold() throws GameActionException {
        for (int k = 0; k < 2; k++) {
            gatherer();
            carrying = bot(40, Team.B, UnitType.BABY_RAT, me, Direction.NORTH);
            Baby.carryRound = G.round - (k == 0 ? 12 : 11);
            clock(100);   // the turn bails at BC_BAIL, after the check
            Baby.turn();
            clock(1 << 30);
            if (k == 0) check(Baby.carryRound == G.round, "SWAP_HOLD: a carry 12 rounds after the last grab restarts carryRound");
            else check(Baby.carryRound == G.round - 11, "SWAP_HOLD: one 11 rounds after is our own grab");
        }
        carrying = null;
    }


    /** EKING_FORGET: an attacker that reaches its last enemy-king sighting and senses no king there forgets it. */
    static void testEKingForget() throws GameActionException {
        babyWorld();
        at(200); G.coop = false;
        Baby.role = Baby.ATTACK; Baby.eKing = null; Baby.home = p(3, 3); Sym.mask = Sym.ROT;
        facing = Direction.NORTH; G.startTurn();
        Baby.lastEKing = p(5, 7); Baby.lastEKingRound = 195;
        Baby.attack();
        check(Baby.lastEKing == null, "EKING_FORGET: the sighting 2 ahead, sensed empty, is forgotten");
        babyWorld();
        at(200); G.coop = false;
        Baby.role = Baby.ATTACK; Baby.eKing = null; facing = Direction.NORTH; G.startTurn();
        Baby.lastEKing = p(5, 15); Baby.lastEKingRound = 195;
        Baby.attack();
        check(p(5, 15).equals(Baby.lastEKing) && me.y == 6, "EKING_FORGET: a far sighting is kept and walked to (" + me + ")");
        Baby.role = Baby.GATHER; Baby.lastEKing = null;
    }

    /** SEEK_NEWS: a seek ends only on real news (a mine the king lacked), not on sensing a listed mine. */
    static void testSeekNews() throws GameActionException {
        gatherer();
        Comms.kings[0] = p(3, 3); Comms.numKings = 1;
        listMines(0); Comms.mines[Comms.numMines++] = p(12, 12);
        Baby.noteMine(12, 12);                 // sensed a mine the king lists already
        Baby.seekMines = 0; Baby.seekUntil = G.round + 50; Baby.seekNews = false; Baby.mineTarget = p(20, 15);
        Baby.seek();
        check(Baby.seekMines == 1 && Baby.mineTarget != null, "SEEK_NEWS: a listed mine is no news: the seek goes on");
        Baby.noteMine(14, 14);                 // real news: carry it home
        boolean on = Baby.seek();
        check(on && Baby.seekNews && Nav.target != null && Nav.target.equals(p(3, 3)), "SEEK_NEWS: news is carried to the king");
        Comms.mines[Comms.numMines++] = p(14, 14);   // the king listed it
        on = Baby.seek();
        check(!on && Baby.seekUntil == 0 && Baby.mineTarget == null, "SEEK_NEWS: reported news ends the seek");
        Comms.numKings = 0; Comms.numMines = 0;
    }

    /** N6: BitFlow momentum only while its field is toward this target (a finished one keeps building set). */
    static void testN6Momentum() throws GameActionException {
        for (int k = 0; k < 2; k++) {
            babyWorld();
            Baby.bfOn = true;
            BitFlow.building = true; BitFlow.seeded = false; BitFlow.heading = Direction.NORTH;
            BitFlow.tgt = k == 0 ? p(25, 18) : p(15, 5); BitFlow.startRound = G.round;
            facing = Direction.EAST; G.startTurn();
            MapLocation t = p(15, 5);
            Nav.goTo(t);
            if (k == 0) check(me.equals(p(6, 5)), "N6: a field toward another target gives no momentum: greedy east (" + me + ")");
            else check(me.equals(p(5, 6)), "N6: a field toward t still building: momentum north (" + me + ")");
            BitFlow.building = false; BitFlow.tgt = null; Baby.bfOn = false;
        }
    }

    // ------------------------------------------------------------------ baby combat

    /** C5: the throw damage table assumes the thrown rat may act after the thrower. */
    static void testC5FlightDmg() {
        int[] want = {0, 0, 42, 30, 26, 22, 18, 10, 10};
        boolean ok = true;
        for (int k = 2; k <= 8; k++) ok &= Baby.flightDmg(k) == want[k] && Baby.FLIGHT_DMG[k] == want[k];
        check(ok, "C5: flight damage 42 point-blank, 8 less at tiles 3-6, a landing (10) beyond ("
              + Arrays.toString(Baby.FLIGHT_DMG) + ")");
    }

    /** C1: a carrier of an enemy with a cat in carry range is feeding it (the cat flee stands down). */
    static void testC1Feeding() {
        babyWorld();
        at(300); G.coop = false;
        RobotInfo enemy = bot(40, Team.B, UnitType.BABY_RAT, me, Direction.NORTH);
        carrying = enemy; Baby.carryRound = 299;
        Baby.seen = new RobotInfo[]{ bot(900, Team.NEUTRAL, UnitType.CAT, p(9, 7), Direction.WEST) };
        check(Baby.feedingCat(), "C1: carrying an enemy with a cat within CARRY_CAT: feeding");
        Baby.seen = new RobotInfo[]{ bot(900, Team.NEUTRAL, UnitType.CAT, p(5 + Cfg.CARRY_CAT + 1, 5), Direction.WEST) };
        check(!Baby.feedingCat(), "C1: the cat beyond CARRY_CAT: not feeding");
        Baby.seen = new RobotInfo[]{ bot(900, Team.NEUTRAL, UnitType.CAT, p(9, 7), Direction.WEST) };
        carrying = bot(41, Team.A, UnitType.BABY_RAT, me, Direction.NORTH);
        check(!Baby.feedingCat(), "C1: carrying an ally: not feeding");
        carrying = enemy; G.coop = true;   // (G.startTurn reads the fake's cooperation)
        check(!Baby.feedingCat(), "C1: in cooperation: not feeding");
        carrying = null; Baby.seen = new RobotInfo[0];
    }

    /** C6: aimCarry's step keeps out of the cat's lane (body and next two tiles) unless the throw is ready and feeds it. */
    static void testC6AimLane() throws GameActionException {
        for (int k = 0; k < 2; k++) {
            reset(); install();
            for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, false, false, false);
            G.coop = false;
            RobotInfo e = bot(80, Team.B, UnitType.BABY_RAT, p(5, 8), Direction.NORTH);
            robots.put(e.location, e);
            RobotInfo c = bot(900, Team.NEUTRAL, UnitType.CAT, p(6, 8), Direction.SOUTHWEST);   // lane (4..7, 6..9)
            Baby.cat = k == 1 ? c : null;
            Baby.seen = new RobotInfo[]{ e };   // the cat is out of the scan (only its lane matters here)
            actCd = 20;                          // the throw is not ready: hold the aim
            boolean aimed = Baby.aimCarry();
            boolean inLane = Baby.inLane(c, me) || Baby.inCatBody(c.location, me);
            if (k == 0) check(aimed && me.equals(p(5, 6)), "C6: no cat: step north onto the throw line (" + me + ")");
            else check(!inLane, "C6: with the cat's lane on that step, the aim keeps out of it (" + me + ", " + aimed + ")");
        }
        Baby.cat = null; Baby.seen = new RobotInfo[0];
    }

    /** C7b: a GrabMicro grab that fails (the target turned or moved) bites it instead. */
    static void testC7bGrabBite() throws GameActionException {
        for (int k = 0; k < 2; k++) {
            babyWorld();
            G.coop = false; facing = Direction.NORTH; G.startTurn();
            RobotInfo e = bot(80, Team.B, UnitType.BABY_RAT, p(5, 6), Direction.NORTH);
            robots.put(e.location, e);
            GrabMicro.er[0] = e; GrabMicro.grabI = 0; GrabMicro.biteI = -1; GrabMicro.bestD = 8;
            GrabMicro.bestF = Direction.NORTH.ordinal();
            carryOk = k == 1;
            GrabMicro.execute();
            carryOk = true;
            if (k == 0) check(carrying == null && bitten.equals(List.of(p(5, 6))), "C7b: the grab fails: bitten instead (" + bitten + ")");
            else check(carrying == e && bitten.isEmpty(), "C7b: the grab works: no bite");
        }
        carrying = null;
    }

    /** BITE_KING_TILE: a baby bites any enemy-king tile it can attack, not only the nearest one. */
    static void testBiteKingTile() throws GameActionException {
        babyWorld();
        facing = Direction.NORTH; G.startTurn();
        RobotInfo ek = bot(77, Team.B, UnitType.RAT_KING, p(7, 6), Direction.WEST);
        for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) robots.put(p(7 + dx, 6 + dy), ek);
        // (6,5) is the tile nearest us but east is outside a north-facing cone; (6,6) is in it
        MapLocation t = Baby.kingTile(G.rc, me, ek.location);
        check(t.equals(p(6, 6)), "BITE_KING_TILE: the bitable body tile (" + t + ")");
        facing = Direction.SOUTH; G.startTurn();
        t = Baby.kingTile(G.rc, me, ek.location);
        check(t.equals(p(6, 5)), "BITE_KING_TILE: none bitable: the nearest tile, to turn to (" + t + ")");
    }

    // ------------------------------------------------------------------ navigation

    /** N3: digOk is raw 5 or the bank over the dig reserve (per king, capped by the upkeep left). */
    static void testN3DigOk() throws GameActionException {
        babyWorld();
        Comms.numKings = 1; at(100);
        int res = Cfg.DIG_RESERVE;
        raw = 5; global = 0; G.startTurn();
        check(Nav.digOk(), "N3: 5 raw digs");
        raw = 4; global = res + 4; G.startTurn();
        check(!Nav.digOk(), "N3: 4 raw and the bank at the reserve + 4: no dig");
        global = res + 5; G.startTurn();
        check(Nav.digOk(), "N3: the bank at the reserve + 5 digs");
        Comms.numKings = 2; global = 2 * res + 4; G.startTurn();
        check(!Nav.digOk(), "N3: two kings double the reserve");
        Comms.numKings = 1; round = 1990; global = 2 * 10 + 5; raw = 0; G.startTurn();
        check(Nav.digOk(), "N3: the reserve is capped by the upkeep left");
        // tryDig: stalled, dirt straight ahead, 60+ cheese but under the reserve: no dig
        babyWorld();
        Comms.numKings = 1; at(100);
        dirt.add(p(5, 6)); Sym.record(5, 6, false, true, false);
        Nav.stall = 6; raw = 0; global = res; G.startTurn();
        Nav.tryDig(p(5, 15));
        check(digsDone == 0, "N3: tryDig keeps the dig reserve");
        global = res + 5; G.startTurn();
        Nav.tryDig(p(5, 15));
        check(digsDone == 1, "N3: tryDig digs above it");
        Comms.numKings = 0;
    }

    /** N7: flee2 prefers steps outside Nav.avoid, then any. Fleeing a threat 2 west, a diagonal gains most. */
    static void testN7Flee() throws GameActionException {
        int ne = 1 << Direction.NORTHEAST.ordinal(), se = 1 << Direction.SOUTHEAST.ordinal();
        int[] masks = { 0, ne | se, 0xFF };
        for (int k = 0; k < 3; k++) {
            babyWorld();
            me = p(10, 10); facing = Direction.EAST; G.startTurn();
            Nav.avoid = masks[k];
            boolean moved = Nav.flee2(2 * 8, 2 * 10);
            boolean diag = me.equals(p(11, 11)) || me.equals(p(11, 9));
            if (k == 0) check(moved && diag, "N7: no avoid: the diagonal gains most (" + me + ")");
            if (k == 1) check(moved && me.equals(p(11, 10)), "N7: both diagonals avoided: the straight step outside the mask (" + me + ")");
            if (k == 2) check(moved && diag, "N7: every step avoided: the best one anyway (" + me + ")");
        }
        Nav.avoid = 0;
    }

    /** DIG_FIRST_TGT: dig-first is blocked by a search toward this target only, not a stale one. */
    static void testDigFirstTgt() throws GameActionException {
        for (int stale = 0; stale < 2; stale++) {
            babyWorld();
            Comms.numKings = 1; at(100);
            Baby.bfOn = false; Flow.rev = false; global = 1000; G.startTurn();
            for (int x = 4; x <= 6; x++) { MapLocation l = p(x, 6); if (x == 5) dirt.add(l); else walls.add(l); Sym.record(x, 6, x != 5, x == 5, false); }
            MapLocation t = p(5, 12);
            Flow.building = true; Flow.tgt = stale == 1 ? p(25, 2) : t; Flow.builtRound = -1; Flow.startRound = G.round;
            Nav.goTo(t);
            if (stale == 1) check(digsDone == 1, "DIG_FIRST_TGT: a search toward an old target does not block the dig");
            else check(digsDone == 0, "DIG_FIRST_TGT: a search toward this target blocks the dig");
            Flow.building = false; Flow.tgt = null;
        }
        Comms.numKings = 0;
    }

    /** Flow's path is a walk of adjacent tiles from a to b; null when it is, else why not. */
    static String walk(MapLocation a, MapLocation b) {
        if (Flow.plen < 2 || Flow.path[0] != G.idx(a) || Flow.path[Flow.plen - 1] != G.idx(b)) return "ends";
        for (int i = 1; i < Flow.plen; i++) {
            int u = Flow.path[i - 1], v = Flow.path[i];
            if (Math.abs(u % W - v % W) > 1 || Math.abs(u / W - v / W) > 1) return "gap at " + i;
        }
        return null;
    }

    /** A W x H open map with full-height dirt columns x0..x1; the rat at a; a reverse search toward t allocated and
     *  seeded (not grown). */
    static void revSetup(int w, int h, int x0, int x1, MapLocation a, MapLocation t, boolean fast) {
        reset(); W = w; H = h; install();
        for (int x = x0; x <= x1; x++) for (int y = 0; y < H; y++) { dirt.add(p(x, y)); Sym.record(x, y, false, true, false); }
        Flow.rev = true; Flow.fast = fast; Flow.lifo = false; Flow.cache = false;
        me = a; G.startTurn();
        Flow.request(t);
        if (Flow.bucket == null) Flow.alloc(W * H, 0); else Flow.seed();
    }

    /** N1: depth (path tiles) in par's high bits; the finish reserve counts tiles, not path cost (dirt DIG_COST). */
    static void testN1Depth() {
        for (int f = 0; f < 2; f++) {
            boolean fast = f == 1;
            MapLocation a = p(2, 3), t = p(27, 3);
            revSetup(30, 7, 8, 13, a, t, fast);   // 25 steps, 6 of them onto dirt: cost 25 + 6 * (DIG_COST - 1)
            // the finish pops a tile beside the rat 24 steps from t: it needs 40 * (24 + 3) bytecodes
            clock(40 * 27 - 1);
            Flow.work(0);
            check(!Flow.ready(t) && Flow.building, "N1 " + f + ": one bytecode short of 40 * (tiles + 3): the finish waits");
            clock(40 * 27);
            Flow.work(0);
            String bad = Flow.ready(t) ? walk(a, t) : "not ready";
            check(bad == null && Flow.plen == 26, "N1 " + f + ": 40 * (tiles + 3) finishes the dirt path (" + bad + ", " + Flow.plen + ")");
            boolean depth = true;
            for (int i = 1; i < Flow.plen; i++) depth &= Flow.par[Flow.path[i]] >>> 3 == Flow.plen - 1 - i;
            check(depth, "N1 " + f + ": par >>> 3 is each path tile's step count to the target");
            clock(1 << 30);
        }
    }

    /** N2: the rat walked into the region the reverse search already closed: the path starts from its own tile. */
    static void testN2Closed() {
        for (int f = 0; f < 2; f++) {
            boolean fast = f == 1;
            MapLocation a = p(2, 3), t = p(27, 3), m = p(22, 3);
            revSetup(30, 7, 12, 13, a, t, fast);
            clock(500);                 // grows to the rat's neighbours, too short to finish there
            Flow.work(0);
            check(Flow.building && !Flow.ready(t), "N2 " + f + ": the search stops at the rat, unfinished");
            me = m; G.startTurn();      // the rat walked on, into tiles the search closed (before the dirt)
            clock(1 << 30);
            Flow.work(0);
            String bad = Flow.ready(t) ? walk(m, t) : "not ready";
            check(bad == null && Flow.plen == 6, "N2 " + f + ": the path runs from the rat's new tile (" + bad + ", " + Flow.plen + ")");
        }
    }

    /** N4: a path whose next tile is dirt we may not dig is held FLOW_HOLD turns, then dropped (no retry for a while). */
    static void testN4Hold() throws GameActionException {
        babyWorld();
        W = 12; H = 5; install();
        Comms.numKings = 1;
        for (int y = 0; y < H; y++) { dirt.add(p(5, y)); Sym.record(5, y, false, true, false); }
        MapLocation t = p(9, 2);
        me = p(4, 2); facing = Direction.EAST; round = 100; global = 1000; G.startTurn();
        Flow.request(t); Flow.work(0);
        check(Flow.ready(t) && (Sym.mem[Flow.path[1]] & Sym.DIRT) != 0, "N4: a path whose next tile is dirt");
        global = Cfg.DIG_RESERVE; raw = 0; G.startTurn();   // under the dig reserve
        boolean held = true;
        for (int i = 0; i < Cfg.FLOW_HOLD; i++) { held &= Flow.follow(t); round++; G.startTurn(); }
        check(held && me.equals(p(4, 2)) && digsDone == 0, "N4: held FLOW_HOLD turns without digging");
        check(!Flow.follow(t) && !Flow.ready(t) && t.equals(Flow.failTgt), "N4: then dropped, the target failed");
        Flow.request(t);
        check(!Flow.building && !Flow.ready(t), "N4: not searched again at once");
        // with the cheese the same path digs
        babyWorld();
        W = 12; H = 5; install();
        Comms.numKings = 1;
        for (int y = 0; y < H; y++) { dirt.add(p(5, y)); Sym.record(5, y, false, true, false); }
        me = p(4, 2); facing = Direction.EAST; round = 100; global = 1000; G.startTurn();
        Flow.request(t); Flow.work(0);
        check(Flow.follow(t) && digsDone == 1, "N3/N4: above the reserve the path's dirt is dug");
        Comms.numKings = 0;
    }

    /** N5: a failed BitFlow field clears its target, so active() no longer blocks requests. */
    static void testN5BitFlowFail() {
        reset(); W = 12; H = 8; install();
        BitFlow.init();
        BitFlow.reach = null; BitFlow.tgt = null; BitFlow.building = false; BitFlow.seeded = false; BitFlow.failTgt = null;
        BitFlow.lo = 0; BitFlow.hi = -1; BitFlow.held = 0;
        for (int y = 0; y < H; y++) { Sym.record(6, y, true, false, false); BitFlow.mark(6, y, Sym.mem[6 + y * W]); }
        me = p(2, 4); G.startTurn();
        MapLocation t = p(10, 4);
        BitFlow.request(t);
        BitFlow.work(0);
        check(!BitFlow.building && BitFlow.tgt == null && !BitFlow.active(t) && t.equals(BitFlow.failTgt),
              "N5: a failed field has no target and is not active");
    }

    // ------------------------------------------------------------------ shared state

    static void trapReset() { TrapRisk.mark = null; TrapRisk.last = null; TrapRisk.arrived = -1; TrapRisk.lastObs = 0; }

    /** S2 (and TRAP_ARRIVED): arrival dates to the last observe, so a skipped observe keeps marks made after it. */
    static void testS2Arrival() {
        babyWorld(); trapReset();
        RobotInfo old = bot(50, Team.B, UnitType.BABY_RAT, p(9, 3), Direction.NORTH);   // marks (8..10, 4)
        RobotInfo now = bot(51, Team.B, UnitType.BABY_RAT, p(11, 7), Direction.SOUTH);  // marks (10..12, 6)
        me = p(3, 3); at(9);
        TrapRisk.observe(new RobotInfo[]{ old });
        at(10);
        TrapRisk.observe(new RobotInfo[]{ now });
        // round 10's move went to (10,5); round 11's observe was skipped (budget); round 12 observes there
        me = p(10, 5); at(12);
        TrapRisk.observe(new RobotInfo[0]);
        check(TrapRisk.arrived == 10, "S2: arrival dates to the last observe (" + TrapRisk.arrived + ")");
        check(TrapRisk.mark[G.idx(p(10, 6))] != 0 && TrapRisk.mark[G.idx(p(11, 6))] != 0,
              "S2: marks seen at the observe before the move stay (a trap laid after it never fired on us)");
        check(TrapRisk.mark[G.idx(p(9, 4))] == 0 && TrapRisk.mark[G.idx(p(10, 4))] == 0, "S2: older marks around us are cleared");
        check(TrapRisk.mask(me) != 0, "S2: the kept marks make the avoid mask");
    }

    /** TRAP_CARRIED: a carried or thrown rat neither moves its arrival nor clears marks. */
    static void testTrapCarried() {
        babyWorld(); trapReset();
        RobotInfo e = bot(50, Team.B, UnitType.BABY_RAT, p(9, 3), Direction.NORTH);   // marks (8..10, 4)
        me = p(3, 3); at(9);
        TrapRisk.observe(new RobotInfo[]{ e });
        me = p(9, 5); at(20); beingCarried = true;
        TrapRisk.observe(new RobotInfo[0]);
        check(TrapRisk.last.equals(p(3, 3)) && TrapRisk.mark[G.idx(p(9, 4))] != 0, "TRAP_CARRIED: carried: no arrival, marks kept");
        beingCarried = false; beingThrown = true; at(21);
        TrapRisk.observe(new RobotInfo[0]);
        check(TrapRisk.mark[G.idx(p(9, 4))] != 0, "TRAP_CARRIED: thrown: marks kept");
        beingThrown = false; at(22);
        TrapRisk.observe(new RobotInfo[0]);
        check(TrapRisk.last.equals(p(9, 5)) && TrapRisk.mark[G.idx(p(9, 4))] == 0, "TRAP_CARRIED: grounded again: arrival and clearing");
    }

    public static void main(String[] args) throws Exception {
        testK3Sweep();
        testK2LeadSlot();
        testK1Held();
        testK7Babies();
        testKingNearCheese();
        testKingPickupIncome();
        testKingBiteKing();
        testFlowHeldReset();
        testK5Full();
        testC4CatEarshot();
        testE6ReadMissed();
        testE2Wander();
        testPromoteOnStep();
        testC5FlightDmg();
        testC1Feeding();
        testBiteKingTile();
        testN3DigOk();
        testN7Flee();
        testDigFirstTgt();
        testN1Depth();
        testN2Closed();
        testN4Hold();
        testN5BitFlowFail();
        testS2Arrival();
        testTrapCarried();
        testO2BirthPick();
        testE5WanderChain();
        testStallShift();
        testE7Pile();
        testC3CatCentre();
        testC1NoFlee();
        testO1BirthBail();
        testSwapHold();
        testC6AimLane();
        testC7bGrabBite();
        testEKingForget();
        testSeekNews();
        testN6Momentum();
        System.out.println("FixTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
