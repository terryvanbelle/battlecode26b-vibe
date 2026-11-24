package ratbot;

import battlecode.common.*;

import static ratbot.BotTest.*;

/**
 * yl51 = sw71 + Cfg.CARRY_BITE 60 (11-24; u5p's value from 11-02, never in the local tournament). Baby.biteEnemies
 * ranks an adjacent enemy baby by Baby.biteScore: 1000 + health, less the raw cheese it carries up to CARRY_BITE,
 * never under 1001 (lowest first). So a loaded gatherer is bitten before a lightly wounded empty-handed rat: its
 * cheese drops where it dies. Replica 40515 sittingducks vs AdamTan12_Icarus: Icarus picked 130-1140 cheese per 100
 * rounds on our half (we took 0-160 on theirs) and our king starved at r954. We stand at (5,5) facing north on an
 * open 30x20 map; both enemies are adjacent and in front of us.
 */
public class CarryBiteTest {
    static RobotInfo rat(int id, int x, int y, int hp, int cheese) {
        return new RobotInfo(id, Team.B, UnitType.BABY_RAT, hp, new MapLocation(x, y), Direction.SOUTH, 0, cheese, null);
    }

    static void world(RobotInfo... rs) {
        reset(); install();
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, false, false, false);
        round = 300; G.startTurn();
        G.coop = false;
        health = 100;
        for (RobotInfo r : rs) robots.put(r.location, r);
    }

    public static void main(String[] args) throws Exception {
        check(Cfg.CARRY_BITE == 60, "carry bite: CARRY_BITE 60 (yl51)");
        check(Baby.biteScore(100, 0, Cfg.CARRY_BITE) == 1100, "carry bite: an empty-handed rat at 100 ranks 1100");
        check(Baby.biteScore(100, 40, Cfg.CARRY_BITE) == 1060, "carry bite: 40 cheese ranks it 40 weaker (1060)");
        check(Baby.biteScore(100, 80, Cfg.CARRY_BITE) == 1040, "carry bite: the loot counts up to 60 (1040 with 80 cheese)");
        check(Baby.biteScore(30, 80, Cfg.CARRY_BITE) == 1001, "carry bite: never under 1001 (30 health, 80 cheese)");
        check(Baby.biteScore(30, 80, 0) == 1030, "carry bite: with the switch off (cap 0) the loot does not count");

        // an empty-handed rat at 80 health and a loaded one (60 cheese) at 100: the loaded one is bitten first
        RobotInfo plain = rat(81, 5, 6, 80, 0), loaded = rat(82, 6, 6, 100, 60);
        world(plain, loaded);
        check(Baby.biteEnemies(G.rc.senseNearbyRobots(-1, null)), "carry bite: setup: a bite is made");
        check(bitten.size() == 1 && bitten.get(0).equals(loaded.location),
              "carry bite: the loaded rat (1040) goes before the wounded empty-handed one (1080) (bitten " + bitten + ")");

        // the loot never outranks a much weaker rat: 30 health beats a loaded rat at 100 (1030 vs 1040)
        RobotInfo weak = rat(83, 5, 6, 30, 0);
        world(weak, loaded);
        Baby.biteEnemies(G.rc.senseNearbyRobots(-1, null));
        check(bitten.size() == 1 && bitten.get(0).equals(weak.location),
              "carry bite: a rat at 30 health still goes before a loaded one at 100 (bitten " + bitten + ")");

        G.coop = true;
        System.out.println("CarryBiteTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
