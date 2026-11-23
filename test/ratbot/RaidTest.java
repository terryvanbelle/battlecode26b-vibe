package ratbot;

import battlecode.common.*;

import static ratbot.BotTest.*;

/** sw53 (champion 11-23) = yk1 + Cfg.RAID_SHARE + Cfg.FWD_TRAP. RAID_SHARE: a gatherer or fighter at war whose slot
 *  bits 12-13 draw below RAID_SHARE (of 4) is a raider. A home-half mine it picks becomes that mine's enemy twin under
 *  the first live symmetry (Baby.firstSym: ROT, HOR, VER); an enemy-half pick stays; in coop nobody raids. The
 *  switch is read by reflection so that bots without it (yk1 line) still compile the suite: they skip this test. */
public class RaidTest {
    static MapLocation p(int x, int y) { return new MapLocation(x, y); }

    static int share() {
        try { return Cfg.class.getField("RAID_SHARE").getInt(null); } catch (ReflectiveOperationException e) { return -1; }
    }

    static boolean raider() throws Exception {
        return (boolean) Baby.class.getDeclaredMethod("raider").invoke(null);
    }

    /** pickMine of a rat at home (3, 3) on a 30x30 map (ROT twin of home: (26, 26)). */
    static MapLocation pick(int role, int bits, boolean coop, int symMask, int postMargin, MapLocation... mines) {
        reset(); W = 30; H = 30; install(); round = 400; G.startTurn();
        Sym.mask = symMask; G.coop = coop;
        Baby.home = p(3, 3); Baby.role = role; Baby.postMargin = postMargin;
        Baby.myMineCount = 0; Baby.mineShift = 0; Baby.slot = bits << 12;
        Comms.numMines = 0;
        for (MapLocation m : mines) Comms.mines[Comms.numMines++] = m;
        MapLocation r = Baby.pickMine();
        Comms.numMines = 0;
        return r;
    }

    public static void main(String[] args) throws Exception {
        int share = share();
        if (share < 0) {
            System.out.println("RaidTest: skipped (no Cfg.RAID_SHARE in this bot)");
            System.exit(0);
        }
        // raider(): share in 4 slot draws, gatherers and fighters at war only
        for (int bits = 0; bits < 4; bits++)
            for (int role : new int[] {Baby.GATHER, Baby.FIGHT, Baby.ATTACK}) {
                reset(); W = 30; H = 30; install(); round = 400; G.startTurn();
                Baby.role = role; Baby.slot = bits << 12 | 5;
                G.coop = false;
                boolean want = share > bits && role != Baby.ATTACK;
                check(raider() == want, "raid: at war, role " + role + ", slot bits " + bits + ": raider " + want);
                G.coop = true;
                check(!raider(), "raid: in coop nobody raids (role " + role + ", slot bits " + bits + ")");
            }
        if (share > 0) {
            MapLocation back = p(5, 5), far = p(24, 24);
            MapLocation got = pick(Baby.GATHER, 0, false, Sym.ROT, 0, back);
            check(got.equals(G.mirror(back, Sym.ROT)), "raid: a raider at war takes the enemy twin of its home mine " + got);
            got = pick(Baby.GATHER, 3, false, Sym.ROT, 0, back);
            check(share > 3 || got.equals(back), "raid: slot bits 3 (no raider under RAID_SHARE <= 3) keeps its home mine " + got);
            got = pick(Baby.GATHER, 0, true, Sym.ROT, 0, back);
            check(got.equals(back), "raid: in coop a raider's slot keeps its home mine " + got);
            got = pick(Baby.GATHER, 0, false, Sym.ROT, 0, far);
            check(got.equals(far), "raid: an enemy-half pick stays (no twin back home) " + got);
            got = pick(Baby.GATHER, 0, false, Sym.HOR, 0, back);
            check(got.equals(G.mirror(back, Sym.HOR)), "raid: only HOR live: the twin is the HOR mirror " + got);
            got = pick(Baby.GATHER, 0, false, Sym.ROT | Sym.HOR, 0, back);
            check(got.equals(G.mirror(back, Sym.ROT)), "raid: ROT and HOR live: the twin is under ROT (firstSym order) " + got);
            // a fighter raider: with every known mine ours, FIGHT_POST keeps the contested ones; the raider holds their twin
            if (Cfg.FIGHT_POST > 0) {
                java.util.Set<MapLocation> twins = new java.util.HashSet<>();
                for (MapLocation m : new MapLocation[] {p(12, 12), p(14, 10)}) twins.add(G.mirror(m, Sym.ROT));
                got = pick(Baby.FIGHT, 0, false, Sym.ROT, Cfg.FIGHT_POST, back, p(12, 12), p(14, 10));
                check(twins.contains(got), "raid: a fighter raider holds the twin of a contested home mine " + got);
            }
        }
        Sym.mask = Sym.ROT | Sym.HOR | Sym.VER;
        System.out.println("RaidTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
