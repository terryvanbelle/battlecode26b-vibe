package ratbot;

import battlecode.common.*;

import static ratbot.BotTest.*;

/** Cfg.FWD_TRAP (sw53, champion 11-23; first in sw51, rated alone as yl7): a rat trap laid from the enemy half skips
 *  the KING_RESERVE and austerity gates. Moved from test/yl7 at sw53's crowning; the switch is read by reflection so
 *  that bots without it (yk1 line) run the suite with FWD_TRAP off: the gate holds on both halves. */
public class FwdTrapTest {
    static MapLocation p(int x, int y) { return new MapLocation(x, y); }

    static boolean fwd() {
        try { return Cfg.class.getField("FWD_TRAP").getBoolean(null); } catch (ReflectiveOperationException e) { return false; }
    }

    public static void main(String[] args) throws Exception {
        boolean fwd = fwd();
        boolean traps = Cfg.TRAP_CAP > 0;   // TRAP_CAP 0 (sw57, 11-23): no rat traps at all, on either half
        int reserve = Math.min(Cfg.KING_RESERVE, 2 * (2000 - 300)) + 20;
        boolean[][] laid = new boolean[2][2];
        for (int budget = 0; budget < 2; budget++)
            for (int side = 0; side < 2; side++) {
                reset(); W = 30; H = 30; install();
                round = 300; G.startTurn(); G.coop = false;
                Baby.trapOk = true;
                // below the reserve (budget 5), or rich but austere (budget 0)
                global = budget == 1 ? reserve - 1 : reserve + 500; shared[Comms.BUDGET] = budget == 1 ? 5 : 0;
                Baby.home = side == 0 ? p(5, 5) : p(25, 25);   // the rat at (5, 5): its home half, then the enemy half
                Baby.nearestEnemy = new RobotInfo(50, Team.B, UnitType.BABY_RAT, 100, p(5, 7), Direction.SOUTH, 0, 0, null);
                Comms.numKings = 1; Comms.kings[0] = p(25, 25);   // our king far from the enemy: no defence trap
                laid[budget][side] = Baby.tryRatTrap();
            }
        check(!laid[1][0], "FWD_TRAP: below the reserve, no trap from the home half (the gate holds)");
        check(laid[1][1] == (traps && fwd), "FWD_TRAP: below the reserve, a trap from the enemy half skips the gate");
        check(laid[0][0] == (traps && !(Cfg.AUSTERE_FROM > 0)), "FWD_TRAP: austere, no trap from the home half (the gate holds)");
        check(laid[0][1] == (traps && (fwd || !(Cfg.AUSTERE_FROM > 0))), "FWD_TRAP: austere, a trap from the enemy half skips the gate");
        Comms.numKings = 0; Baby.nearestEnemy = null;
        System.out.println("FwdTrapTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
