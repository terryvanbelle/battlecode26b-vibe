package ratbot;

import battlecode.common.*;
import static ratbot.BotTest.*;

/**
 * yl35's first strike (Cfg.STRIKE_FIRST, 11-24). yl32's standoff holds against a watching, healthier enemy baby; with
 * STRIKE_FIRST it does not when that enemy is one step away (Chebyshev 2), its health is in our bite bucket
 * (ceil(eh / 10) <= ceil(h / 10)) and our action is ready: Baby.war takes Baby.strikeStep, a forward step that ends
 * adjacent to it with it inside our cone, and actAfterMove's bite follows. We stand at (5,5) on an open 30x20 map.
 */
public class StrikeFirstTest {
    static RobotInfo world(int myHp, int ehp, MapLocation el, Direction edir, Direction myFacing) {
        reset(); install();
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, false, false, false);
        facing = myFacing;
        round = 300; G.startTurn();
        G.coop = false;
        health = myHp;
        Baby.role = Baby.FIGHT; Baby.hunting = false; Baby.eKing = null; Comms.numKings = 0;
        RobotInfo e = new RobotInfo(80, Team.B, UnitType.BABY_RAT, ehp, el, edir, 0, 0, null);
        robots.put(e.location, e);
        Baby.nearestEnemy = e;
        return e;
    }

    static final MapLocation ME = new MapLocation(5, 5), N2 = new MapLocation(5, 7);

    public static void main(String[] args) throws Exception {
        check(Cfg.STRIKE_FIRST && Cfg.STANDOFF_EQ, "strike first: STRIKE_FIRST on (yl35, on yl32's STANDOFF_EQ)");

        // the bite bucket: ceil(eh / 10) <= ceil(h / 10)
        RobotInfo e = world(41, 50, N2, Direction.SOUTH, Direction.NORTH);
        check(Baby.strikeStep(e, 41) == Direction.NORTH, "strike first: 50 vs our 41 (bucket 5 = 5): the direct step north");
        e = world(91, 100, N2, Direction.SOUTH, Direction.NORTH);
        check(Baby.strikeStep(e, 91) == Direction.NORTH, "strike first: 100 vs our 91 (bucket 10 = 10): strike");
        e = world(90, 100, N2, Direction.SOUTH, Direction.NORTH);
        check(Baby.strikeStep(e, 90) == null, "strike first: 100 vs our 90 (bucket 10 > 9): no strike (a lead of 10 survives our bite)");
        e = world(40, 41, N2, Direction.SOUTH, Direction.NORTH);
        check(Baby.strikeStep(e, 40) == null, "strike first: 41 vs our 40 (bucket 5 > 4): no strike");

        // only one step away
        e = world(91, 100, new MapLocation(5, 8), Direction.SOUTH, Direction.NORTH);
        check(Baby.strikeStep(e, 91) == null, "strike first: Chebyshev 3: no strike");
        e = world(91, 100, new MapLocation(5, 6), Direction.SOUTH, Direction.NORTH);
        check(Baby.strikeStep(e, 91) == null, "strike first: adjacent already: no step");

        // war: a watching enemy of 100 two tiles north, we have 91: we step to (5,6) facing it and can bite it
        e = world(91, 100, N2, Direction.SOUTH, Direction.NORTH);
        check(Baby.sees(e, me), "strike first: setup: the enemy sees us");
        int s0 = Baby.strikes;
        check(Baby.war() && me.equals(new MapLocation(5, 6)) && facing == Direction.NORTH && Baby.strikes == s0 + 1,
              "strike first: war steps in (at " + me + ", facing " + facing + ")");
        check(Baby.biteEnemies(new RobotInfo[]{e}) && bitten.contains(N2), "strike first: and the bite on arrival lands");

        // the same at 90 (bucket 9 < 10): the standoff holds as in yl32
        world(90, 100, N2, Direction.SOUTH, Direction.NORTH);
        check(Baby.war() && me.equals(ME) && facing == Direction.NORTH, "strike first: 90 vs 100: held off (" + me + ")");

        // action not ready (a trap or bite before the move): hold
        world(91, 100, N2, Direction.SOUTH, Direction.NORTH);
        actCd = 10;
        check(Baby.war() && me.equals(ME), "strike first: action not ready: held off (" + me + ")");

        // the direct step blocked by an ally: its neighbours end adjacent but leave the enemy outside our cone: hold
        world(91, 100, N2, Direction.SOUTH, Direction.NORTH);
        robots.put(new MapLocation(5, 6), new RobotInfo(81, Team.A, UnitType.BABY_RAT, 100, new MapLocation(5, 6), Direction.NORTH, 0, 0, null));
        check(Baby.war() && me.equals(ME), "strike first: direct step blocked: held off (" + me + ")");

        // facing east with the turn used up: the direct step would be a strafe that leaves the enemy outside our cone
        world(91, 100, N2, Direction.SOUTH, Direction.EAST);
        turnCd = 10;
        check(Baby.strikeStep(Baby.nearestEnemy, 91) == null && Baby.war() && me.equals(ME),
              "strike first: no turn left and not facing the step: held off (" + me + ")");

        // a diagonal enemy at (7,7) facing south-west: the step north-east
        e = world(91, 100, new MapLocation(7, 7), Direction.SOUTHWEST, Direction.NORTH);
        check(Baby.sees(e, me) && Baby.war() && me.equals(new MapLocation(6, 6)) && facing == Direction.NORTHEAST,
              "strike first: diagonal: steps north-east (at " + me + ", facing " + facing + ")");
        check(Baby.biteEnemies(new RobotInfo[]{e}), "strike first: diagonal: the bite lands");

        // a knight's-move enemy at (6,7): whichever step is taken ends adjacent with the enemy in our cone
        e = world(91, 100, new MapLocation(6, 7), Direction.SOUTH, Direction.NORTH);
        check(Baby.sees(e, me) && Baby.war() && G.cheb(me, e.location) == 1 && Baby.biteEnemies(new RobotInfo[]{e}),
              "strike first: knight's move: adjacent and bitten (at " + me + ", facing " + facing + ")");

        // an equal enemy is no standoff foe (yl32): approached by the plain walk, not counted as a strike
        world(91, 91, N2, Direction.SOUTH, Direction.NORTH);
        s0 = Baby.strikes;
        check(Baby.war() && !me.equals(ME) && Baby.strikes == s0, "strike first: an equal enemy: yl32's walk, no strike");

        health = 100; G.coop = true; Baby.nearestEnemy = null;
        System.out.println("StrikeFirstTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
