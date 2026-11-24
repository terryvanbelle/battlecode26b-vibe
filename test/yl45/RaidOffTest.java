package ratbot;

import battlecode.common.*;

import static ratbot.BotTest.*;

/** yl45 = sw69 + RAID_SHARE 0 (11-24): raiders off. Replica 40046 (sw69 vs AlexT101_finalsbot, dirtfulcat): 15 of our
 *  22 trap deaths by r200 were on the enemy half, 7 within 3 of (20,17), the enemy twin of our mine (9,17) that sw69's
 *  raiders take. With the share at 0 no gatherer or fighter at war raids, whatever its slot bits 12-13, and every one
 *  keeps its home-half mine. RaidTest skips its pick checks at share 0; this covers them. */
public class RaidOffTest {
    public static void main(String[] args) throws Exception {
        check(RaidTest.share() == 0, "raid off: Cfg.RAID_SHARE 0 (yl45; sw69 has 2)");
        MapLocation back = RaidTest.p(5, 5);
        for (int bits = 0; bits < 4; bits++)
            for (int role : new int[] {Baby.GATHER, Baby.FIGHT}) {
                MapLocation got = RaidTest.pick(role, bits, false, Sym.ROT, 0, back);
                check(got.equals(back), "raid off: at war, role " + role + ", slot bits " + bits + ": keeps its home mine (" + got + ")");
                check(!RaidTest.raider(), "raid off: at war, role " + role + ", slot bits " + bits + ": no raider");
            }
        G.coop = true;
        System.out.println("RaidOffTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
