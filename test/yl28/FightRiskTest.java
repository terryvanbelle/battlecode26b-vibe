package ratbot;

import battlecode.common.*;
import static ratbot.BotTest.*;

/**
 * yl28's fight trap sense (Cfg.FIGHT_RISK_TTL, 11-23). Baby.riskMask is the trap-risk part of a turn's avoid mask: a
 * free baby avoids every live TrapRisk mark (TrapRisk.TTL, as before yl28); an engaged or hunting baby, which had no
 * trap mask at all before, avoids the marks of the last FIGHT_RISK_TTL rounds only. The baby stands at (5,5) at war;
 * an enemy baby at (5,7) faces south, so its front tiles (4,6), (5,6) and (6,6) are marked.
 */
public class FightRiskTest {
    static final MapLocation ME = new MapLocation(5, 5);

    /** The avoid bits (G.DIRS order) of the neighbours of ME within Chebyshev 1 of a front tile of the south-facing
     *  enemy at (5,7). */
    static int expected() {
        int m = 0;
        for (int d = 0; d < 8; d++) {
            MapLocation n = ME.add(G.DIRS[d]);
            boolean risky = false;
            for (int fx = 4; fx <= 6; fx++) risky |= Math.abs(n.x - fx) <= 1 && Math.abs(n.y - 6) <= 1;
            if (risky) m |= 1 << d;
        }
        return m;
    }

    static void world(int round) {
        FixTest.babyWorld(); FixTest.trapReset();
        me = ME; facing = Direction.NORTH;
        FixTest.at(round);
        G.coop = false; Nav.stall = 0; Baby.hunting = false;
    }

    public static void main(String[] args) throws Exception {
        check(Cfg.FIGHT_RISK_TTL == 1, "fight risk: FIGHT_RISK_TTL 1 (" + Cfg.FIGHT_RISK_TTL + ")");
        int want = expected();
        check(Integer.bitCount(want) == 5 && (want & (1 << Direction.SOUTH.ordinal())) == 0,
              "fight risk: setup: 5 of the 8 steps lie by the enemy's front, the south ones do not (" + Integer.toBinaryString(want) + ")");

        world(10);
        TrapRisk.observe(new RobotInfo[]{ FixTest.bot(50, Team.B, UnitType.BABY_RAT, new MapLocation(5, 7), Direction.SOUTH) });
        check(Baby.riskMask(true) == want, "fight risk: engaged, the front just seen is avoided (" + Integer.toBinaryString(Baby.riskMask(true)) + ")");
        check(Baby.riskMask(false) == want && Baby.riskMask(false) == TrapRisk.mask(G.me),
              "fight risk: free, the mask is TrapRisk.mask as before");
        Baby.hunting = true;
        check(Baby.riskMask(false) == want, "fight risk: hunting takes the fight mask");
        Baby.hunting = false;

        // one round on: the mark is still within FIGHT_RISK_TTL
        FixTest.at(11); G.coop = false;
        check(Baby.riskMask(true) == want, "fight risk: a mark of the last round still counts in a fight");

        // two rounds on: the fight forgets it, a free baby keeps it (TrapRisk.TTL 150)
        FixTest.at(12); G.coop = false;
        check(Baby.riskMask(true) == 0, "fight risk: a mark 2 rounds old is past FIGHT_RISK_TTL: no fight mask");
        check(Baby.riskMask(false) == want, "fight risk: a free baby still avoids the 2-round-old mark");
        Baby.hunting = true;
        check(Baby.riskMask(false) == 0, "fight risk: hunting forgets it too");
        Baby.hunting = false;

        // no mask in cooperation or once the walk has stalled 8 turns
        FixTest.at(10); G.coop = true;
        check(Baby.riskMask(true) == 0 && Baby.riskMask(false) == 0, "fight risk: none in cooperation");
        G.coop = false; Nav.stall = 8;
        check(Baby.riskMask(true) == 0 && Baby.riskMask(false) == 0, "fight risk: none after an 8-turn stall");
        Nav.stall = 0;

        // every step risky: TrapRisk.mask gives 0 (no step is closed), in a fight too
        world(20);
        TrapRisk.observe(new RobotInfo[]{
            FixTest.bot(50, Team.B, UnitType.BABY_RAT, new MapLocation(5, 7), Direction.SOUTH),
            FixTest.bot(51, Team.B, UnitType.BABY_RAT, new MapLocation(5, 3), Direction.NORTH) });
        check(Baby.riskMask(true) == 0, "fight risk: fronts on both sides close every step: the mask opens them all (0)");

        FixTest.trapReset(); G.coop = true;
        System.out.println("FightRiskTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
