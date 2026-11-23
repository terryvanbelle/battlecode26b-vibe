package ratbot;

import static ratbot.BotTest.*;

/**
 * yl23's one change (11-23): SIEGE_FLOOR 100. At war with enemies in the king's sight every spawn floor drops to
 * SIEGE_FLOOR (King.spawnFloor's siege argument; King.spawn passes it only then), so a besieged king spends its bank
 * on defenders down to 100 instead of holding KING_RESERVE 300 (or LOW_FLOOR 200 under LOW_BABIES babies).
 */
public class SiegeTest {
    public static void main(String[] args) {
        int sf = Cfg.SIEGE_FLOOR, res = Cfg.KING_RESERVE, low = Cfg.LOW_FLOOR;
        check(sf == 100, "siege floor: SIEGE_FLOOR is 100 (yl23)");
        // without a siege the floors are the champion's: at least KING_RESERVE, LOW_FLOOR under LOW_BABIES babies
        check(King.spawnFloor(Cfg.SPAWN_FLOOR, false, 0) == Math.max(Cfg.SPAWN_FLOOR, res)
              && King.spawnFloor(Cfg.SPAWN_FLOOR, true, 0) == low, "siege floor: no siege, no change (KING_RESERVE " + res + ", LOW_FLOOR " + low + ")");
        // under a siege every floor (core, war, spawn; low or not) is at most SIEGE_FLOOR
        boolean capped = true;
        for (int f : new int[]{Cfg.CORE_FLOOR, Cfg.WAR_FLOOR, Cfg.SPAWN_FLOOR})
            for (boolean lo : new boolean[]{false, true})
                capped &= King.spawnFloor(f, lo, sf) == Math.min(lo ? low : Math.max(f, res), sf);
        check(capped, "siege floor: every spawn floor drops to " + sf + " under a siege");
        // a floor already below the siege value stays (min, not a raise)
        check(King.spawnFloor(50, true, 1000) == Math.min(low, 1000), "siege floor: a siege never raises a floor");
        System.out.println("SiegeTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
