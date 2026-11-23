package ratbot;

import battlecode.common.*;
import static ratbot.BotTest.*;

/**
 * yl24's lethal throw (Cfg.THROW_KILL, 11-23): a flight that kills the carried enemy rat scores THROW_KILL more, so the
 * carrier throws it now instead of holding for a robot hit (THROW_MIN 84, THROW_HOLD 10). In yl19's replica games the
 * enemy's throws hit something the round they are thrown 2.3x as often as ours (36-38% of throws against 14-17%).
 * The carrier stands at (5,5), one round into its hold (G.round 50, the grab at 49).
 */
public class ThrowKillTest {
    /** A fresh carrier at (5,5) holding an enemy baby with health hp, with walls at the given tiles. */
    static void setup(int hp, MapLocation... wall) throws GameActionException {
        reset(); install();
        for (MapLocation w : wall) walls.add(w);
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, walls.contains(new MapLocation(x, y)), false, false);
        carrying = enemyRat(90, 5, 5, hp);
        G.round = 50; Baby.carryRound = 49;
    }

    public static void main(String[] args) throws Exception {
        check(Cfg.THROW_KILL == 84, "throw kill: THROW_KILL is 84 (" + Cfg.THROW_KILL + ")");
        // a point-blank wall two tiles east (42): it kills a rat at 42 or less now, and only holds a healthier one
        setup(40, new MapLocation(7, 5));
        check(Baby.tryThrow(new RobotInfo[0]) && thrown == Direction.EAST, "throw kill: a 40-health rat goes into the point-blank wall now (" + thrown + ")");
        setup(42, new MapLocation(7, 5));
        check(Baby.tryThrow(new RobotInfo[0]) && thrown == Direction.EAST, "throw kill: 42 health dies on the 42 wall: thrown now (" + thrown + ")");
        setup(43, new MapLocation(7, 5));
        check(!Baby.tryThrow(new RobotInfo[0]) && thrown == null, "throw kill: a 43-health rat survives the wall: the hold goes on");
        // no wall or robot anywhere: an empty flight lands for 10
        setup(10);
        check(Baby.tryThrow(new RobotInfo[0]) && thrown != null, "throw kill: a 10-health rat dies on any landing: thrown now (" + thrown + ")");
        setup(11);
        check(!Baby.tryThrow(new RobotInfo[0]) && thrown == null, "throw kill: an 11-health rat survives a landing: the hold goes on");
        // a hit on our own rat is never a kill worth taking
        setup(30);
        RobotInfo ally = new RobotInfo(91, G.us, UnitType.BABY_RAT, 100, new MapLocation(3, 5), Direction.NORTH, 0, 0, null);
        robots.put(ally.location, ally);
        check(!Baby.tryThrow(new RobotInfo[]{ally}) && thrown == null, "throw kill: a lethal flight into our own rat is no reason to throw");
        // an enemy baby beyond tile 2 may move: it counts as a landing (10), so it kills no 30-health rat
        setup(30);
        RobotInfo far = enemyRat(92, 2, 5, 100);
        robots.put(far.location, far);
        check(!Baby.tryThrow(new RobotInfo[]{far}) && thrown == null, "throw kill: a baby three tiles off is no sure kill: the hold goes on");
        // two lethal flights: the one that also hurts an enemy rat (west, point-blank) beats the wall (east)
        setup(30, new MapLocation(7, 5));
        RobotInfo e = enemyRat(93, 3, 5, 100);
        robots.put(e.location, e);
        check(Baby.tryThrow(new RobotInfo[]{e}) && thrown == Direction.WEST, "throw kill: a lethal hit on an enemy rat beats a lethal wall (" + thrown + ")");
        // CARRY_KING: with an enemy king within reach (off every flight line) the carrier keeps walking to it
        setup(30, new MapLocation(7, 5));
        RobotInfo k = new RobotInfo(94, G.them, UnitType.RAT_KING, 600, new MapLocation(8, 11), Direction.NORTH, 0, 0, null);
        robots.put(k.location, k);
        check(Baby.carryKing(new RobotInfo[]{k}) != null, "throw kill: the king at (8,11) is in CARRY_KING reach");
        check(!Baby.tryThrow(new RobotInfo[]{k}) && thrown == null, "throw kill: a lethal wall does not end the walk to an enemy king");
        // with no captive known the throw is as before (yl19's BotTest cases run with carrying null)
        setup(40, new MapLocation(7, 5));
        carrying = null;
        check(!Baby.tryThrow(new RobotInfo[0]) && thrown == null, "throw kill: no carried rat known: the hold goes on as before");
        Baby.carryRound = -100;
        System.out.println("ThrowKillTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
