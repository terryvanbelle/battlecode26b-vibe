package ratbot;

import battlecode.common.*;

import static ratbot.BotTest.*;

/** sw54 (champion 11-23) = sw53 + sw50's bank on bodies: MAX_BABIES_WAR 20, RICH_BANK 600, CORE_BABIES 20 and
 *  KING_RESERVE 300 (sw53: 16, 1000, 18, 400). This pins how King.spawn applies them (audit of sw54,
 *  research/audit/2026-11-23-sw54.md). Every expected value is computed from Cfg, so other pool bots run it too.
 *  - Steps of 4: the engine's rat cost rises 10 for every 4 baby rats, so the babies estimate is a multiple of 4,
 *    less one for each king past the first. With one king, a core count or cap between two multiples of 4 acts as
 *    the multiple above it: CORE_BABIES 18 and 20 both keep 20 babies.
 *  - The war cap is a siege cap: spawn() passes its own war (an enemy in the king's view at war, and the war floor
 *    paid) to babyCap, so MAX_BABIES_WAR holds only then; at war with no enemy in view the cap is MAX_BABIES.
 *  - The cap comes after the core boot: a core count above the cap holds only where the cap allows it (siege, or
 *    the rich tier from RICH_FROM).
 *  The audit's fix newcomers change the last two, and this test reads their switches by name (absent = off):
 *  CORE_OVER_CAP (yl15: the core boot is not cut by the cap) and WAR_CAP_ALL (yl16: MAX_BABIES_WAR through the whole
 *  war). */
public class BankTest {
    static MapLocation p(int x, int y) { return new MapLocation(x, y); }

    /** A boolean Cfg switch by name; false when this bot does not have it. */
    static boolean flag(String name) {
        try { return Cfg.class.getField(name).getBoolean(null); } catch (ReflectiveOperationException e) { return false; }
    }

    /** n rounded up to a multiple of 4: where a test "estimate < n" stops with one king. */
    static int up4(int n) { return (n + 3) / 4 * 4; }

    /** One King.spawn at round r, 30x30 map (above CORE_SMALL_AREA: the core is CORE_BABIES), with n live babies and
     *  the given kings, bank g, no income history (so the surplus test fails: only the core boot or the siege spawn
     *  can build); enemy: an enemy rat in the king's view. Returns spawnWhy (b built, s no surplus, c at the cap). */
    static char spawn(int n, int kings, int g, boolean coop, boolean enemy, int r) throws GameActionException {
        reset(); W = 30; H = 30; install();
        type = UnitType.RAT_KING; me = p(15, 15); round = r; G.startTurn(); G.coop = coop;
        ratCost = 10 + 10 * (n / 4); global = g;
        Comms.numKings = kings;
        shared[20] = kings - 1;   // Comms.FORMED on bots that count promotions there (yl5's line); unused otherwise
        Econ.n = 0; Econ.sum = 0;
        RobotInfo[] en = enemy ? new RobotInfo[] {enemyRat(77, 18, 18, 100)} : new RobotInfo[0];
        King.spawn(new RobotInfo[0], en);
        Comms.numKings = 0; shared[20] = 0;
        return King.spawnWhy;
    }

    /** The bank the core boot needs with k kings at n babies (core floor at least KING_RESERVE, per king). */
    static int bootBank(int n, int k) { return 10 + 10 * (n / 4) + k * Math.max(Cfg.CORE_FLOOR, Cfg.KING_RESERVE); }

    public static void main(String[] args) throws Exception {
        int core = Cfg.CORE_BABIES, cap = Cfg.MAX_BABIES, warCap = Cfg.MAX_BABIES_WAR;
        int late = Math.max(400, Cfg.RICH_FROM);   // the rich tier is open
        boolean coreOver = flag("CORE_OVER_CAP"), warAll = flag("WAR_CAP_ALL");
        if (Cfg.RICH_BABIES > up4(core) && Cfg.RICH2_BANK > Cfg.RICH_BANK + 500) {
            // steps of 4, one king: in peace with a rich bank (cap RICH_BABIES) only the core boot builds
            int n = up4(core) - 4, g = Math.max(Cfg.RICH_BANK, bootBank(n + 4, 1)) + 50;
            char w = spawn(n, 1, g, true, false, late);
            check(w == 'b', "bank: one king, " + n + " babies under CORE_BABIES " + core + ": the core boots a spawn (" + w + ")");
            w = spawn(up4(core), 1, g, true, false, late);
            check(w == 's', "bank: one king, " + up4(core) + " babies: CORE_BABIES " + core + " acts as " + up4(core) + ", no boot (" + w + ")");
            // two kings: the estimate is one less, so a core count on a multiple of 4 keeps 4 more babies
            g = Math.max(Cfg.RICH_BANK, bootBank(up4(core), 2)) + 50;
            w = spawn(up4(core), 2, g, true, false, late);
            check(w == (core % 4 == 0 ? 'b' : 's'), "bank: two kings, " + up4(core) + " babies (estimate " + (up4(core) - 1)
                  + "): CORE_BABIES " + core + (core % 4 == 0 ? " boots" : " does not boot") + " (" + w + ")");
        }
        if (core > up4(cap) && Cfg.RICH_BANK > bootBank(up4(cap), 1)) {
            // the cap comes after the boot: at war with no enemy in view and a bank under RICH_BANK, MAX_BABIES holds
            // (WAR_CAP_ALL: MAX_BABIES_WAR; CORE_OVER_CAP: the core boot builds past either)
            int n = up4(cap), g = bootBank(n, 1) + 10;
            char w = spawn(n, 1, g, false, false, late);
            int atWar = warAll ? warCap : cap;
            boolean build = coreOver || n < atWar;
            check(w == (build ? 'b' : 'c'), "bank: at war, no enemy in view, " + n + " babies, core " + core + ": "
                  + (coreOver ? "CORE_OVER_CAP boots past the cap" : (warAll ? "WAR_CAP_ALL: " : "") + "the cap " + atWar + (build ? " lets it build" : " cuts the core")) + " (" + w + ")");
            // siege: an enemy in the king's view and the war floor paid: MAX_BABIES_WAR is the cap
            int warBank = 10 + 10 * (n / 4) + Math.max(Cfg.WAR_FLOOR, Cfg.KING_RESERVE);
            if (warBank <= g) {
                w = spawn(n, 1, g, false, true, late);
                build = coreOver || warCap > n;
                check(w == (build ? 'b' : 'c'), "bank: siege, " + n + " babies: MAX_BABIES_WAR " + warCap + (build ? " lets it build" : " holds") + " (" + w + ")");
                w = spawn(n, 1, g, true, true, late);
                check(w == (coreOver ? 'b' : 'c'), "bank: in coop an enemy in view is no siege: MAX_BABIES " + cap + (coreOver ? ", and CORE_OVER_CAP boots past it" : " holds") + " (" + w + ")");
            }
        }
        if (warCap > cap) {
            int n = up4(warCap), g = bootBank(n, 1) + 10;
            if (g < Cfg.RICH_BANK) {
                char w = spawn(n, 1, g, false, true, late);
                check(w != 'b', "bank: siege, " + n + " babies: MAX_BABIES_WAR " + warCap + " acts as " + up4(warCap) + ", no spawn (" + w + ")");
            }
        }
        // KING_RESERVE is the least spawn floor: sw54's 300 equals CORE_FLOOR, WAR_FLOOR and SPAWN_FLOOR, so every
        // spawn floor is its own value
        for (int f : new int[] {Cfg.CORE_FLOOR, Cfg.WAR_FLOOR, Cfg.SPAWN_FLOOR})
            check(King.spawnFloor(f, false, 0) == Math.max(f, Cfg.KING_RESERVE), "bank: a spawn floor " + f + " is at least KING_RESERVE " + Cfg.KING_RESERVE);
        System.out.println("BankTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
