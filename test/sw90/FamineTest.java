package ratbot;

import battlecode.common.*;

/**
 * sw90 = sw83 + FAMINE 200 (King.starved, King.countStarved, King.famine; read by King.spawnFloor). A king counts its
 * starved turns: turns that ended (Econ.endTurn, after every spend) with the bank under RAT_KING_CHEESE_CONSUMPTION 2,
 * where the engine takes 10 health instead of 2 cheese. Under LOW_BABIES babies every spawn floor (core boot, war,
 * the hard floor) then holds FAMINE percent of 2 cheese per starved turn on top of LOW_FLOOR, per king; LAST_FLOOR's
 * band (rat cost 10) and the 12+ band are unchanged. With no starved turn the floors are sw83's.
 * Evidence (local tournament, sw83's games to 11-25 00:34 PDT): starvation killed the loser's king in 412 of 770 king
 * kills; in 8 replayed sw83 starvation losses, 6 kings kept buying low-band babies after their first starved turn.
 * Uses BankTest.spawn (one King.spawn, 30x30, no income history: only the core boot can build).
 */
public class FamineTest {
    public static void main(String[] args) throws Exception {
        BotTest.check(Cfg.FAMINE == 200, "famine: FAMINE 200 (sw90 = sw83 + FAMINE 200)");
        int f = Cfg.LOW_FLOOR, r = 300;

        // no starved turn: the floors are sw83's
        King.starved = 0;
        BotTest.check(King.famine() == 0, "famine: no starved turn, no brake (" + King.famine() + ")");
        BotTest.check(King.spawnFloor(1, true, 0) == f, "famine: no starved turn: the low band floor is LOW_FLOOR");
        char w = BankTest.spawn(4, 1, 20 + f, false, false, r);
        BotTest.check(w == 'b', "famine: no starved turn, 4 babies, bank " + (20 + f) + ": builds as sw83 (" + w + ")");

        // 10 starved turns: 2 x 2 x 10 = 40 more in the low band
        King.starved = 10;
        int fam = Cfg.FAMINE * 2 * 10 / 100;
        BotTest.check(King.famine() == fam && fam == 40, "famine: 10 starved turns hold " + fam + " (" + King.famine() + ")");
        BotTest.check(King.spawnFloor(1, true, 0) == f + fam, "famine: 10 starved turns: the low band floor is LOW_FLOOR + " + fam);
        BotTest.check(King.spawnFloor(1, false, 0) == Math.max(1, Cfg.KING_RESERVE), "famine: the 12+ band floor is unchanged");
        BotTest.check(King.spawnFloor(5000, false, 0) == 5000, "famine: a higher 12+ floor stands");
        BotTest.check(King.spawnFloor(1, true, Cfg.LAST_FLOOR) == Cfg.LAST_FLOOR, "famine: LAST_FLOOR's band (siege argument) is unchanged");
        King.starved = 10;
        w = BankTest.spawn(4, 1, 20 + f + fam, false, false, r);
        BotTest.check(w == 'b', "famine: 10 starved turns, 4 babies, bank " + (20 + f + fam) + ": builds (" + w + ")");
        King.starved = 10;
        w = BankTest.spawn(4, 1, 20 + f + fam - 1, false, false, r);
        BotTest.check(w != 'b', "famine: 10 starved turns, 4 babies, bank " + (20 + f + fam - 1) + ": held for upkeep (" + w + ")");
        King.starved = 10;
        w = BankTest.spawn(8, 1, 30 + f + fam - 1, false, false, r);
        BotTest.check(w != 'b', "famine: 10 starved turns, 8 babies (cost 30), bank " + (30 + f + fam - 1) + ": held (" + w + ")");
        // at war with an enemy in the king's view the war spawn reads the same floor
        King.starved = 10;
        w = BankTest.spawn(4, 1, 20 + f + fam - 1, false, true, r);
        BotTest.check(w != 'b', "famine: at war, 10 starved turns, bank " + (20 + f + fam - 1) + ": held (" + w + ")");
        King.starved = 10;
        w = BankTest.spawn(4, 1, 20 + f + fam, false, true, r);
        BotTest.check(w == 'b', "famine: at war, 10 starved turns, bank " + (20 + f + fam) + ": builds (" + w + ")");
        // two kings: the floor (brake included) is per king
        King.starved = 10;
        w = BankTest.spawn(4, 2, 20 + 2 * (f + fam), false, false, r);
        BotTest.check(w == 'b', "famine: two kings, bank " + (20 + 2 * (f + fam)) + ": builds (" + w + ")");
        King.starved = 10;
        w = BankTest.spawn(4, 2, 20 + 2 * (f + fam) - 1, false, false, r);
        BotTest.check(w != 'b', "famine: two kings, bank " + (20 + 2 * (f + fam) - 1) + ": held (" + w + ")");
        // 0-3 babies (cost 10): LAST_FLOOR, no brake (a king with no rats has no income)
        King.starved = 10;
        w = BankTest.spawn(2, 1, 10 + Cfg.LAST_FLOOR, false, false, r);
        BotTest.check(w == 'b', "famine: 2 babies (cost 10), bank " + (10 + Cfg.LAST_FLOOR) + ": LAST_FLOOR holds, builds (" + w + ")");
        // 12 babies (cost 40): the full floor, unchanged
        int hi = 40 + Math.max(Cfg.CORE_FLOOR, Cfg.KING_RESERVE);
        King.starved = 10;
        w = BankTest.spawn(12, 1, hi, false, false, r);
        BotTest.check(w == 'b', "famine: 12 babies, bank " + hi + ": the 12+ floor is unchanged, builds (" + w + ")");

        // countStarved: a turn that ended with the bank under 2 is counted at the next turn's start
        BotTest.reset(); BotTest.install();
        BotTest.type = UnitType.RAT_KING; BotTest.round = 300; G.startTurn();
        King.starved = 0; Econ.prevEnd = -1;
        King.countStarved();
        BotTest.check(King.starved == 0, "famine: before the first turn ends nothing is counted");
        BotTest.global = 1; Econ.endTurn();
        King.countStarved();
        BotTest.check(King.starved == 1, "famine: a turn that ended with 1 banked is starved (" + King.starved + ")");
        BotTest.global = 0; Econ.endTurn();
        King.countStarved();
        BotTest.check(King.starved == 2, "famine: a turn that ended with 0 banked is starved (" + King.starved + ")");
        BotTest.global = GameConstants.RAT_KING_CHEESE_CONSUMPTION; Econ.endTurn();
        King.countStarved();
        BotTest.check(King.starved == 2, "famine: a turn that ended with the upkeep (2) banked is not starved (" + King.starved + ")");
        BotTest.global = 500; Econ.endTurn();
        King.countStarved();
        BotTest.check(King.starved == 2, "famine: the count never falls (" + King.starved + ")");

        King.starved = 0; Econ.prevEnd = -1;
        System.out.println("FamineTest: " + (BotTest.checks - BotTest.fails) + "/" + BotTest.checks + " passed");
        System.exit(BotTest.fails == 0 ? 0 : 1);
    }
}
