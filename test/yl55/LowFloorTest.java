package ratbot;

import battlecode.common.*;

import static ratbot.BotTest.*;

/**
 * yl55 = yl54 + LOW_BABIES 16 (11-24; from 12): the LOW_FLOOR 20 band widens to 12-15 babies (rat cost 40). Under
 * LOW_BABIES (16) live babies every spawn floor (core boot, war, the hard floor) is LOW_FLOOR per king; with 0-3
 * babies (rat cost 10) LAST_FLOOR holds instead, so LOW_FLOOR is the floor of the 4-15 band (rat cost 20-40).
 * Evidence: the 11-22 profile of 171 replica games (research/field/top-profile): in our losses we kept 12 babies at r1000 to their 21, and they
 * spawned 39 to our 30 in r401-800.
 * New in yl55: LOW_BABIES 16 is above CORE_SMALL 12, so on maps of at most CORE_SMALL_AREA tiles 12-15 babies are
 * low but not core: the core boot does not build there, and only the war spawn (an enemy in the king's view) and the
 * hard floor read LOW_FLOOR. yl54's last check (LOW_BABIES at most the smallest core count) is replaced by checks of
 * that case on a 20x20 map.
 * Uses BankTest.spawn: one King.spawn on a 30x30 map with no income history, so only the core boot or the war spawn
 * can build; spawnSmall is the same on a 20x20 map.
 */
public class LowFloorTest {
    /** BankTest.spawn at war (no cooperation) on a 20x20 map (400 tiles, at most CORE_SMALL_AREA: the core is
     *  CORE_SMALL); enemy: an enemy rat in the king's view. */
    static char spawnSmall(int n, int kings, int g, boolean enemy, int r) throws GameActionException {
        reset(); W = 20; H = 20; install();
        type = UnitType.RAT_KING; me = new MapLocation(10, 10); round = r; G.startTurn(); G.coop = false;
        ratCost = 10 + 10 * (n / 4); global = g;
        Comms.numKings = kings;
        shared[20] = kings - 1;
        Econ.n = 0; Econ.sum = 0;
        RobotInfo[] en = enemy ? new RobotInfo[] {enemyRat(77, 13, 13, 100)} : new RobotInfo[0];
        King.spawn(new RobotInfo[0], en);
        Comms.numKings = 0; shared[20] = 0;
        return King.spawnWhy;
    }

    public static void main(String[] args) throws Exception {
        check(Cfg.LOW_FLOOR == 20 && Cfg.LOW_BABIES == 16, "low floor: LOW_FLOOR 20, LOW_BABIES 16 (yl55 = yl54 + LOW_BABIES 16, from 12; " + Cfg.LOW_BABIES + ")");
        int f = Cfg.LOW_FLOOR, r = 300;
        // one king, 4 babies (cost 20): the boot builds from 20 + LOW_FLOOR
        char w = BankTest.spawn(4, 1, 20 + f, false, false, r);
        check(w == 'b', "low floor: 4 babies, bank " + (20 + f) + ": builds (" + w + ")");
        w = BankTest.spawn(4, 1, 20 + f - 1, false, false, r);
        check(w != 'b', "low floor: 4 babies, bank " + (20 + f - 1) + ": under the floor, no build (" + w + ")");
        w = BankTest.spawn(7, 1, 150, false, false, r);
        check(w == 'b', "low floor: 7 babies, bank 150 (under yl32's 220 bar): builds (" + w + ")");
        // yl54: 8 babies (cost 30) are low
        w = BankTest.spawn(8, 1, 30 + f, false, false, r);
        check(w == 'b', "low floor: 8 babies, bank " + (30 + f) + ": low, builds (" + w + ")");
        w = BankTest.spawn(11, 1, 30 + f - 1, false, false, r);
        check(w != 'b', "low floor: 11 babies, bank " + (30 + f - 1) + ": under the floor, no build (" + w + ")");
        // yl55: 12 babies (cost 40) are low now: the boot builds from 40 + LOW_FLOOR
        w = BankTest.spawn(12, 1, 40 + f, false, false, r);
        check(w == 'b', "low floor: 12 babies, bank " + (40 + f) + ": low now, builds (" + w + ")");
        w = BankTest.spawn(15, 1, 40 + f - 1, false, false, r);
        check(w != 'b', "low floor: 15 babies, bank " + (40 + f - 1) + ": under the floor, no build (" + w + ")");
        w = BankTest.spawn(12, 1, 40 + f, false, true, r);
        check(w == 'b', "low floor: at war, 12 babies, bank " + (40 + f) + ": builds (" + w + ")");
        // 16 babies (cost 50): not low; the core floor (at least KING_RESERVE) holds as before. LOW_BABIES 16 is the
        // peace cap MAX_BABIES 16, so only with an enemy in the king's view (the war cap MAX_BABIES_WAR) or a rich bank
        // does a 16th baby build: the full floor binds only there
        check(Cfg.LOW_BABIES == Cfg.MAX_BABIES && Cfg.MAX_BABIES_WAR > Cfg.LOW_BABIES, "low floor: LOW_BABIES " + Cfg.LOW_BABIES
              + " = MAX_BABIES " + Cfg.MAX_BABIES + " < MAX_BABIES_WAR " + Cfg.MAX_BABIES_WAR);
        int hi = 50 + Math.max(Cfg.CORE_FLOOR, Cfg.KING_RESERVE);
        w = BankTest.spawn(16, 1, hi - 1, false, true, r);
        check(w != 'b', "low floor: at war, 16 babies, bank " + (hi - 1) + ": the full floor holds (" + w + ")");
        w = BankTest.spawn(16, 1, hi, false, true, r);
        check(w == 'b', "low floor: at war, 16 babies, bank " + hi + ": builds (" + w + ")");
        w = BankTest.spawn(16, 1, hi, false, false, r);
        check(w == 'c', "low floor: 16 babies, no enemy in view, bank " + hi + ": the peace cap " + Cfg.MAX_BABIES + " stops it (" + w + ")");
        // two kings: the floor is per king (the estimate 4 - 1 = 3 is still low)
        w = BankTest.spawn(4, 2, 20 + 2 * f, false, false, r);
        check(w == 'b', "low floor: two kings, bank " + (20 + 2 * f) + ": builds (" + w + ")");
        w = BankTest.spawn(4, 2, 20 + 2 * f - 1, false, false, r);
        check(w != 'b', "low floor: two kings, bank " + (20 + 2 * f - 1) + ": no build (" + w + ")");
        // two kings at 16 (estimate 16 - 1 = 15): still low, per king
        w = BankTest.spawn(16, 2, 50 + 2 * f, false, false, r);
        check(w == 'b', "low floor: two kings, 16 (estimate 15), bank " + (50 + 2 * f) + ": low, builds (" + w + ")");
        // 0-3 babies: LAST_FLOOR, not LOW_FLOOR
        w = BankTest.spawn(2, 1, 10 + Cfg.LAST_FLOOR, false, false, r);
        check(w == 'b', "low floor: 2 babies (cost 10): LAST_FLOOR " + Cfg.LAST_FLOOR + " holds (" + w + ")");
        // at war, an enemy rat in the king's view: the war spawn reads the same floor
        w = BankTest.spawn(4, 1, 20 + f, false, true, r);
        check(w == 'b', "low floor: at war, 4 babies, bank " + (20 + f) + ": builds (" + w + ")");
        w = BankTest.spawn(4, 1, 20 + f - 1, false, true, r);
        check(w != 'b', "low floor: at war, 4 babies, bank " + (20 + f - 1) + ": no build (" + w + ")");
        // yl55: LOW_BABIES is above CORE_SMALL and at most CORE_BABIES, so the small-map band 12-15 is low but not core
        check(Cfg.LOW_BABIES > Cfg.CORE_SMALL && Cfg.LOW_BABIES <= Cfg.CORE_BABIES && Cfg.LOW_BABIES <= Cfg.AUSTERE_CORE,
              "low floor: CORE_SMALL " + Cfg.CORE_SMALL + " < LOW_BABIES " + Cfg.LOW_BABIES + " <= CORE_BABIES " + Cfg.CORE_BABIES
              + " and AUSTERE_CORE " + Cfg.AUSTERE_CORE);
        check(20 * 20 <= Cfg.CORE_SMALL_AREA, "low floor: the 20x20 test map is a small map (CORE_SMALL_AREA " + Cfg.CORE_SMALL_AREA + ")");
        // small map, 12 babies, an enemy in view: the war spawn builds down to LOW_FLOOR (yl54: the 300 war floor)
        w = spawnSmall(12, 1, 40 + f, true, r);
        check(w == 'b', "low floor: small map, at war, 12 babies, bank " + (40 + f) + ": the war spawn builds (" + w + ")");
        w = spawnSmall(12, 1, 40 + f - 1, true, r);
        check(w != 'b', "low floor: small map, at war, 12 babies, bank " + (40 + f - 1) + ": under the floor, no build (" + w + ")");
        // small map, 12 babies, no enemy in view: not core, so no boot; with no income the surplus test stops it
        w = spawnSmall(12, 1, 1000, false, r);
        check(w == 's', "low floor: small map, 12 babies, no enemy, bank 1000: not core, no boot (" + w + ")");
        // small map, 8 babies: core and low, so the boot builds from 30 + LOW_FLOOR as on a large map
        w = spawnSmall(8, 1, 30 + f, false, r);
        check(w == 'b', "low floor: small map, 8 babies, bank " + (30 + f) + ": core and low, the boot builds (" + w + ")");
        System.out.println("LowFloorTest: " + (BotTest.checks - BotTest.fails) + "/" + BotTest.checks + " passed");
        System.exit(BotTest.fails == 0 ? 0 : 1);
    }
}
