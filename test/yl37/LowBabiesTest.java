package ratbot;

/**
 * yl37's low-floor threshold (Cfg.LOW_BABIES 10, from 8; 11-24). King.spawn counts babies from the rat cost, a
 * multiple of 4 less one per extra king, and every spawn floor drops to LOW_FLOOR (200 per king) while that estimate
 * is under LOW_BABIES. With one king 8-11 live babies (cost 30, estimate 8) were not low under yl32, so a spawn needed
 * 30 + KING_RESERVE 300 = 330; with yl37 it needs 230. Replica 39577 wallsofparadis vs AdamTan12_Icarus: 8 babies from
 * r550 to r800 with a bank of 253-328 and no spawn, then the army died and the king starved (r936).
 * Uses BankTest.spawn: one King.spawn on a 30x30 map with no income history, so only the core boot can build.
 */
public class LowBabiesTest {
    public static void main(String[] args) throws Exception {
        BotTest.check(Cfg.LOW_BABIES == 10, "low babies: LOW_BABIES 10 (yl37; " + Cfg.LOW_BABIES + ")");
        int f = Cfg.LOW_FLOOR, full = Math.max(Cfg.CORE_FLOOR, Cfg.KING_RESERVE), r = 600;
        // one king, 8 and 11 babies (cost 30, estimate 8 < 10): low, the bar is 30 + LOW_FLOOR
        char w = BankTest.spawn(8, 1, 30 + f, false, false, r);
        BotTest.check(w == 'b', "low babies: one king, 8 babies, bank " + (30 + f) + ": builds (" + w + ")");
        w = BankTest.spawn(8, 1, 30 + f - 1, false, false, r);
        BotTest.check(w != 'b', "low babies: one king, 8 babies, bank " + (30 + f - 1) + ": no build (" + w + ")");
        w = BankTest.spawn(8, 1, 293, false, false, r);
        BotTest.check(w == 'b', "low babies: one king, 8 babies, bank 293 (39577 r600): builds (" + w + ")");
        w = BankTest.spawn(11, 1, 30 + f, false, false, r);
        BotTest.check(w == 'b', "low babies: one king, 11 babies (estimate 8): builds at " + (30 + f) + " (" + w + ")");
        // 12 babies (cost 40, estimate 12): not low, the full floor holds
        w = BankTest.spawn(12, 1, 40 + full - 1, false, false, r);
        BotTest.check(w != 'b', "low babies: one king, 12 babies, bank " + (40 + full - 1) + ": the full floor holds (" + w + ")");
        w = BankTest.spawn(12, 1, 40 + full, false, false, r);
        BotTest.check(w == 'b', "low babies: one king, 12 babies, bank " + (40 + full) + ": builds (" + w + ")");
        // two kings, 12 babies (estimate 11 >= 10): not low, the floor is per king
        w = BankTest.spawn(12, 2, 40 + 2 * full - 1, false, false, r);
        BotTest.check(w != 'b', "low babies: two kings, 12 babies (estimate 11): bank " + (40 + 2 * full - 1) + ": no build (" + w + ")");
        // two kings, 8 babies (estimate 7): low, as under yl32
        w = BankTest.spawn(8, 2, 30 + 2 * f, false, false, r);
        BotTest.check(w == 'b', "low babies: two kings, 8 babies (estimate 7): builds at " + (30 + 2 * f) + " (" + w + ")");
        System.out.println("LowBabiesTest: " + (BotTest.checks - BotTest.fails) + "/" + BotTest.checks + " passed");
        System.exit(BotTest.fails == 0 ? 0 : 1);
    }
}
