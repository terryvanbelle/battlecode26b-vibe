package ratbot;

import battlecode.common.*;
import static ratbot.BotTest.*;

/**
 * sw61's king's eyes (Cfg.KING_EYES, 11-23): the king that writes the war ALERT also writes its nearest open enemy
 * babies as offsets (Comms.EYE0, 3 ints), and a baby's end-of-turn guard facing (Baby.guardTurn) counts the ones it
 * cannot see itself (the enemy grabs our babies from behind their 90-degree cone and throws them into our king).
 */
public class EyesTest {
    static RobotInfo baby(int id, Team t, int x, int y) {
        return new RobotInfo(id, t, UnitType.BABY_RAT, 100, new MapLocation(x, y), Direction.NORTH, 0, 0, null);
    }

    public static void main(String[] args) throws Exception {
        // encoding: every offset within 5 reads back this round and next, not after; a value fits the shared array
        MapLocation a = new MapLocation(20, 20);
        boolean trip = true, range = true;
        for (int r = 8; r < 16; r++)
            for (int dx = -5; dx <= 5; dx++)
                for (int dy = -5; dy <= 5; dy++) {
                    int v = Comms.eye(dx, dy, r);
                    range &= v > 0 && v <= 1023;
                    MapLocation l = a.translate(dx, dy);
                    trip &= l.equals(Comms.eyeAt(v, a, r)) && l.equals(Comms.eyeAt(v, a, r + 1))
                            && Comms.eyeAt(v, a, r + 2) == null && Comms.eyeAt(v, a, r - 1) == null;
                }
        check(range, "eyes: every eye fits a shared-array int (1..1023)");
        check(trip, "eyes: every offset within 5 reads back this round and the next, and is stale after");
        check(Comms.eyeAt(0, a, 10) == null, "eyes: 0 is no eye");
        check(Comms.EYE0 >= Comms.ALERT + 3 && Comms.EYE0 + Comms.EYE_SLOTS <= Comms.FORM_Y && Cfg.KING_EYES <= Comms.EYE_SLOTS,
              "eyes: slots 20-22 lie between the ALERT and FORM_Y slots");

        // the king: raiseAlert writes its three nearest open enemy babies with the alert, nearest first
        java.lang.reflect.Method raise = King.class.getDeclaredMethod("raiseAlert", RobotInfo[].class);
        raise.setAccessible(true);
        reset(); W = 30; H = 30; type = UnitType.RAT_KING; me = new MapLocation(15, 15); round = 300; install(); G.coop = false;
        RobotInfo[] enemies = {
            baby(1, Team.B, 19, 19),     // d2 32: beyond the threat range, inside the eyes' 5
            baby(2, Team.B, 15, 12),     // d2 9
            baby(3, Team.B, 16, 15),     // d2 1
            new RobotInfo(4, Team.B, UnitType.RAT_KING, 600, new MapLocation(15, 20), Direction.NORTH, 0, 0, null),
            baby(5, Team.B, 13, 13),     // d2 8
        };
        raise.invoke(null, (Object) enemies);
        MapLocation[] want = {new MapLocation(16, 15), new MapLocation(13, 13), new MapLocation(15, 12)};
        boolean order = Comms.alertLoc() != null && Comms.alertLoc().equals(me);
        for (int i = 0; i < 3; i++) order &= want[i].equals(Comms.eyeAt(shared[Comms.EYE0 + i], me, G.round));
        check(order, "eyes: the alerting king writes its 3 nearest enemy babies, nearest first (kings and the 4th left out)");
        // next round, two babies: the third slot is cleared (in the same round a lower threat keeps the slot)
        round = 301; G.startTurn(); G.coop = false;
        raise.invoke(null, (Object) new RobotInfo[]{baby(3, Team.B, 16, 15), baby(2, Team.B, 15, 12)});
        check(shared[Comms.EYE0 + 2] == 0 && new MapLocation(15, 12).equals(Comms.eyeAt(shared[Comms.EYE0 + 1], me, G.round)),
              "eyes: fewer babies clear the rest");
        // a king that wrote the alert earlier this round with a higher threat keeps the slot and its eyes
        reset(); W = 30; H = 30; type = UnitType.RAT_KING; me = new MapLocation(15, 15); round = 300; install(); G.coop = false;
        shared[Comms.ALERT] = (3 + 1) | (15 << 6); shared[Comms.ALERT + 1] = 3 + 1; shared[Comms.ALERT + 2] = Comms.stamp();
        raise.invoke(null, (Object) enemies);
        check(shared[Comms.EYE0] == 0 && new MapLocation(3, 3).equals(Comms.alertLoc()), "eyes: a king that keeps no alert writes no eyes");

        // the baby: an enemy behind us that the king saw turns our guard facing round
        MapLocation king = new MapLocation(15, 18);
        for (int c = 0; c < 6; c++) {
            reset(); W = 30; H = 30; me = new MapLocation(15, 15); facing = Direction.NORTH; round = 300; install(); G.coop = false;
            Baby.seen = new RobotInfo[0]; Nav.guard = null;
            shared[Comms.ALERT] = (king.x + 1) | (2 << 6); shared[Comms.ALERT + 1] = king.y + 1; shared[Comms.ALERT + 2] = Comms.stamp();
            MapLocation behind = new MapLocation(15, 13);                     // d2 4, south: outside our cone
            shared[Comms.EYE0] = Comms.eye(behind.x - king.x, behind.y - king.y, G.round);
            String what;
            Direction exp;
            switch (c) {
                case 0: what = "an unseen enemy the king saw within GUARD_DIST2 is faced"; exp = Direction.SOUTH; break;
                case 1: // a visible enemy nearer than the eye keeps our facing on it
                    robots.put(new MapLocation(16, 16), baby(9, Team.B, 16, 16));
                    what = "a nearer enemy in view comes first"; exp = Direction.NORTHEAST; break;
                case 2: // an eye on a tile we see holds no enemy now
                    shared[Comms.EYE0] = Comms.eye(0, -1, G.round);                // (15, 17): north, in view, empty
                    what = "an eye on a tile in view with no enemy is ignored"; exp = Direction.NORTH; break;
                case 3: // an eye two rounds old
                    shared[Comms.EYE0] = Comms.eye(behind.x - king.x, behind.y - king.y, G.round - 2);
                    what = "a stale eye is ignored"; exp = Direction.NORTH; break;
                case 4: // beyond GUARD_DIST2: alert centre (15, 15), eye (0, -5) = (15, 10), d2 25
                    shared[Comms.ALERT + 1] = 15 + 1;
                    shared[Comms.EYE0] = Comms.eye(0, -5, G.round);
                    what = "an eye beyond GUARD_DIST2 is ignored"; exp = Direction.NORTH; break;
                default: // a stale alert
                    shared[Comms.ALERT + 2] = Comms.stamp() - 2;
                    what = "eyes under a stale alert are ignored"; exp = Direction.NORTH; break;
            }
            Baby.guardTurn();
            check(facing == exp, "eyes: " + what + " (facing " + facing + ", want " + exp + ")");
        }
        System.out.println("EyesTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
