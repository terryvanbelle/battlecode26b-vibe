package ratbot;

/**
 * yl44's spawn floor with few babies (Cfg.LOW_FLOOR 20: sw69's dose arm, from 50; sw69 took it from 200; 11-24). Under LOW_BABIES (8) live babies every
 * spawn floor (core boot, war, the hard floor) is LOW_FLOOR per king; with 0-3 babies (rat cost 10) LAST_FLOOR holds
 * instead, so LOW_FLOOR is the floor of the 4-7 band (rat cost 20). Replica 39142 vs AlexT101_usqualsbot: we sat at 4
 * babies for 300 rounds with a 98-208 bank under the 220 bar, while usqualsbot spent down to about 50 and kept 8.
 * Uses BankTest.spawn: one King.spawn on a 30x30 map with no income history, so only the core boot can build.
 */
public class LowFloorTest {
    public static void main(String[] args) throws Exception {
        BotTest.check(Cfg.LOW_FLOOR == 20, "low floor: LOW_FLOOR 20 (yl50 = sw71 + yl44's dose, from 50; " + Cfg.LOW_FLOOR + ")");
        int f = Cfg.LOW_FLOOR, r = 300;
        // one king, 4 babies (cost 20): the boot builds from 20 + LOW_FLOOR
        char w = BankTest.spawn(4, 1, 20 + f, false, false, r);
        BotTest.check(w == 'b', "low floor: 4 babies, bank " + (20 + f) + ": builds (" + w + ")");
        w = BankTest.spawn(4, 1, 20 + f - 1, false, false, r);
        BotTest.check(w != 'b', "low floor: 4 babies, bank " + (20 + f - 1) + ": under the floor, no build (" + w + ")");
        w = BankTest.spawn(7, 1, 150, false, false, r);
        BotTest.check(w == 'b', "low floor: 7 babies, bank 150 (under yl32's 220 bar): builds (" + w + ")");
        // 8 babies: not low; the core floor (at least KING_RESERVE) holds as before
        int hi = 30 + Math.max(Cfg.CORE_FLOOR, Cfg.KING_RESERVE);
        w = BankTest.spawn(8, 1, hi - 1, false, false, r);
        BotTest.check(w != 'b', "low floor: 8 babies, bank " + (hi - 1) + ": the full floor holds (" + w + ")");
        w = BankTest.spawn(8, 1, hi, false, false, r);
        BotTest.check(w == 'b', "low floor: 8 babies, bank " + hi + ": builds (" + w + ")");
        // two kings: the floor is per king (the estimate 4 - 1 = 3 is still low)
        w = BankTest.spawn(4, 2, 20 + 2 * f, false, false, r);
        BotTest.check(w == 'b', "low floor: two kings, bank " + (20 + 2 * f) + ": builds (" + w + ")");
        w = BankTest.spawn(4, 2, 20 + 2 * f - 1, false, false, r);
        BotTest.check(w != 'b', "low floor: two kings, bank " + (20 + 2 * f - 1) + ": no build (" + w + ")");
        // 0-3 babies: LAST_FLOOR, not LOW_FLOOR
        w = BankTest.spawn(2, 1, 10 + Cfg.LAST_FLOOR, false, false, r);
        BotTest.check(w == 'b', "low floor: 2 babies (cost 10): LAST_FLOOR " + Cfg.LAST_FLOOR + " holds (" + w + ")");
        // (audit 11-24) at war, an enemy rat in the king's view: the war spawn reads the same floor
        w = BankTest.spawn(4, 1, 20 + f, false, true, r);
        BotTest.check(w == 'b', "low floor: at war, 4 babies, bank " + (20 + f) + ": builds (" + w + ")");
        w = BankTest.spawn(4, 1, 20 + f - 1, false, true, r);
        BotTest.check(w != 'b', "low floor: at war, 4 babies, bank " + (20 + f - 1) + ": no build (" + w + ")");
        // (audit 11-24) the core boot is the path that applies LOW_FLOOR: every low count is under every core count,
        // so the war and hard floors never apply it on their own (they would if a core count fell below LOW_BABIES)
        int coreMin = Math.min(Math.min(Cfg.CORE_SMALL, Cfg.CORE_BABIES), Cfg.AUSTERE_CORE);
        BotTest.check(Cfg.LOW_BABIES <= coreMin, "low floor: LOW_BABIES " + Cfg.LOW_BABIES + " <= the smallest core count " + coreMin
                      + " (CORE_SMALL, CORE_BABIES, AUSTERE_CORE)");
        System.out.println("LowFloorTest: " + (BotTest.checks - BotTest.fails) + "/" + BotTest.checks + " passed");
        System.exit(BotTest.fails == 0 ? 0 : 1);
    }
}
