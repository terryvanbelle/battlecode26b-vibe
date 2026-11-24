package ratbot;

import battlecode.common.*;
import static ratbot.BotTest.*;

/**
 * sw64's king trap sense (Cfg.KING_TRAP_TTL, 11-23). Enemy rat traps are invisible. A moving king rechecks all nine
 * tiles of its 3x3 (engine: RobotControllerImpl.move runs processTrapsAtLocation on every part), so a step fires every
 * enemy trap within Chebyshev 2 of the new centre; standing still fires none. The king marks the three front tiles of
 * every enemy baby it sees (King.observeTraps, in Sym.mem's TMARK bits) and no king step brings a mark younger than
 * KING_TRAP_TTL within Chebyshev 2 of its new centre (King.trapMask, King.canStep). On the replica (38862 wallsofparadis) the throw
 * dodge stepped our king onto three enemy traps (168 to 18 hp) and a walk onto a fourth (dead).
 * Our king stands at (15,15) on a 30x30 map, at war.
 */
public class KingTrapTest {
    static final MapLocation C = new MapLocation(15, 15);

    static void setup() throws GameActionException {
        reset(); W = 30; H = 30; type = UnitType.RAT_KING; me = C; facing = Direction.NORTH;
        install(); G.coop = false; G.round = 500;
        King.trapLast = null; King.trapObs = 0; King.trapArrived = -1; King.trapAvoid = 0;
    }

    static RobotInfo enemy(int id, int x, int y, Direction f) {
        RobotInfo e = new RobotInfo(id, Team.B, UnitType.BABY_RAT, 100, new MapLocation(x, y), f, 0, 0, null);
        robots.put(e.location, e);
        return e;
    }

    static int bits(Direction... ds) { int m = 0; for (Direction d : ds) m |= 1 << d.ordinal(); return m; }

    public static void main(String[] args) throws Exception {
        check(Cfg.KING_TRAP_TTL == 30 && Cfg.KING_TRAP_LEFT == 4500, "king traps: KING_TRAP_TTL 30, KING_TRAP_LEFT 4500 ("
              + Cfg.KING_TRAP_TTL + ", " + Cfg.KING_TRAP_LEFT + ")");

        // the literal: for each offset of a mark from the old centre, the steps whose new centre is within Chebyshev 2
        boolean ok = TrapRisk.KNEAR.length == 49;
        for (int dy = -3; dy <= 3; dy++)
            for (int dx = -3; dx <= 3; dx++) {
                int b = 0;
                for (int i = 0; i < 8; i++)
                    if (Math.abs(dx - G.DIRS[i].dx) <= 2 && Math.abs(dy - G.DIRS[i].dy) <= 2) b |= 1 << i;
                ok &= TrapRisk.KNEAR[(dx + 3) + 7 * (dy + 3)] == b;
            }
        check(ok, "king traps: TrapRisk.KNEAR literal = the king steps within Chebyshev 2 of each 7x7 offset");
        ok = true;
        for (int i = 0; i < 8; i++) ok &= G.DIRS[i].ordinal() == i;
        check(ok, "king traps: G.DIRS is in Direction.ordinal() order (canStep's bit)");

        // an enemy baby 4 tiles east facing west marks (18,14..16): only the eastward steps bring them within 2
        setup();
        RobotInfo e = enemy(1, 19, 15, Direction.WEST);
        King.observeTraps(new RobotInfo[]{e});
        int m = King.trapMask(C, Cfg.KING_TRAP_TTL);
        check(m == bits(Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST), "king traps: a baby facing us 4 east blocks the 3 eastward steps (" + m + ")");
        King.trapAvoid = m;
        check(!King.canStep(Direction.EAST) && King.canStep(Direction.WEST) && King.canStep(Direction.NORTH),
              "king traps: canStep refuses east, allows west and north");

        // the throw dodge (MechTest's case): a carrier 5 north and 1 east, its south flight down our east column; unmarked
        // the king steps west, with every step marked it stays, and it never picks a marked step
        setup();
        RobotInfo ours = new RobotInfo(600, Team.A, UnitType.BABY_RAT, 40, new MapLocation(16, 20), Direction.NORTH, 0, 0, null);
        RobotInfo cr = new RobotInfo(2, Team.B, UnitType.BABY_RAT, 100, new MapLocation(16, 20), Direction.SOUTH, 0, 0, ours);
        robots.put(cr.location, cr);
        King.trapAvoid = 0;
        Direction td = King.throwDodge(C, new RobotInfo[]{cr});
        check(td != null && td.dx == -1, "king traps: unmarked, the throw dodge steps west off the flight (" + td + ")");
        King.trapAvoid = 0xFF;
        check(King.throwDodge(C, new RobotInfo[]{cr}) == null, "king traps: with every step marked the throw dodge stays (a throw is 46 at most, a trap 50 and 3 rounds frozen)");
        King.trapAvoid = bits(Direction.WEST, Direction.NORTHWEST, Direction.SOUTHWEST);
        td = King.throwDodge(C, new RobotInfo[]{cr});
        check(td == null || td.dx != -1, "king traps: with the west steps marked the dodge takes none of them (" + td + ")");

        // the walk: the closer and sidestep moves toward a mine east skip marked steps
        setup();
        MapLocation far = new MapLocation(25, 15);
        King.trapAvoid = bits(Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST);
        check(!King.closer(C, far) && me.equals(C), "king traps: no strictly closer step is unmarked, so the king holds (at " + me + ")");
        King.trapAvoid = bits(Direction.EAST, Direction.SOUTHEAST);
        check(King.closer(C, far) && me.equals(new MapLocation(16, 16)), "king traps: the unmarked closer step is taken (at " + me + ")");

        // arrival clears marks within Chebyshev 2 that predate it (a trap there would have fired); same-round marks stay
        setup();
        G.round = 499; King.observeTraps(new RobotInfo[]{enemy(1, 19, 15, Direction.WEST)});
        robots.clear();
        G.round = 500; King.observeTraps(new RobotInfo[0]);
        me = new MapLocation(16, 15);                       // the king steps east after its round-500 observe
        G.round = 501; King.observeTraps(new RobotInfo[0]);
        m = King.trapMask(me, Cfg.KING_TRAP_TTL);
        check(m == 0, "king traps: marks within 2 of the new centre made before the step are cleared (" + m + ")");
        setup();
        G.round = 500; King.observeTraps(new RobotInfo[]{enemy(1, 19, 15, Direction.WEST)});
        robots.clear();
        me = new MapLocation(16, 15);                       // stepped the same round the marks were made
        G.round = 501; King.observeTraps(new RobotInfo[0]);
        m = King.trapMask(me, Cfg.KING_TRAP_TTL);
        check(m != 0, "king traps: marks from the observe just before the step stay (the baby lays after our king moves) (" + m + ")");

        // marks 3 tiles out are not cleared by the arrival
        setup();
        G.round = 499; King.observeTraps(new RobotInfo[]{enemy(1, 20, 18, Direction.WEST)});   // marks (19,17..19)
        robots.clear();
        G.round = 500; King.observeTraps(new RobotInfo[0]);
        me = new MapLocation(16, 15);
        G.round = 501; King.observeTraps(new RobotInfo[0]);
        m = King.trapMask(me, Cfg.KING_TRAP_TTL);
        check(m == bits(Direction.NORTHEAST, Direction.EAST), "king traps: marks 3 out survive the arrival and block NE and E (" + m + ")");

        // TTL: a mark 30 rounds old counts, 31 does not
        setup();
        G.round = 470; King.observeTraps(new RobotInfo[]{enemy(1, 19, 15, Direction.WEST)});
        robots.clear();
        G.round = 500;
        check(King.trapMask(C, Cfg.KING_TRAP_TTL) != 0, "king traps: a mark 30 rounds old still counts");
        G.round = 501;
        check(King.trapMask(C, Cfg.KING_TRAP_TTL) == 0, "king traps: a mark 31 rounds old is dropped");

        // the marks live in Sym.mem's TMARK bits: re-sensing the tile keeps them, and the terrain bits are untouched
        setup();
        King.observeTraps(new RobotInfo[]{enemy(1, 19, 15, Direction.WEST)});
        int i = 18 + W * 15, before = Sym.mem[i];
        Sym.record(18, 15, false, true, false);
        check((Sym.mem[i] & Sym.TMARK) == (before & Sym.TMARK) && (Sym.mem[i] >>> Sym.TSHIFT & 0xFFF) == 501
              && (Sym.mem[i] & Sym.DIRT) != 0, "king traps: Sym.record keeps the mark (round 500 + 1) and records the dirt");
        check((Sym.TMARK & (Sym.SEEN | Sym.WALL | Sym.DIRT | Sym.MINE | Sym.EXP | Sym.KING | Sym.CAT | Sym.HELD)) == 0
              && (2000 + 1) <= 0xFFF, "king traps: TMARK overlaps no other Sym bit and holds round 2000");
        // an enemy king and our own babies mark nothing
        setup();
        RobotInfo ek = new RobotInfo(9, Team.B, UnitType.RAT_KING, 600, new MapLocation(19, 15), Direction.WEST, 0, 0, null);
        King.observeTraps(new RobotInfo[]{ek});
        check(King.trapMask(C, Cfg.KING_TRAP_TTL) == 0, "king traps: an enemy king lays no traps, so marks nothing");

        System.out.println("KingTrapTest: " + (fails == 0 ? "all passed" : fails + " failed"));
        System.exit(fails == 0 ? 0 : 1);
    }
}
