package ratbot;

import battlecode.common.*;

import static ratbot.BotTest.*;

/**
 * sw83 = sw82 + SENTRY_LOOK 3; this file is sw82's SentryTest (sw82 = yl54 + SENTRY: Cfg.SENTRY 1, SENTRY_R 1; Baby.sentry and Baby.sweep, read in Baby.mineMove's at-mine branch).
 * At a mine no king of ours holds (a king within Chebyshev 2 of it), with no enemy rat in view (nearestEnemy,
 * Nav.guard) and nothing carried, a rat in the at-mine state walks to within SENTRY_R of the mine and then holds and
 * turns 90 degrees a round (why 'Y'), instead of wandering (why 'W'). A post walk stalled past 6 turns sets sentryOff,
 * and the rat wanders at that mine for 100 rounds.
 * Evidence: replica 11-24 (174 games of 66 matches since 11-23 14:04): only 34% of our home mines' spawns lay in one of
 * our cones at spawn; seen piles were picked 95% of the time within 3.3 rounds, unseen ones after 25.9; 80% of the
 * piles raiders took from our half were piles we never saw.
 * Uses FixTest.gatherer (a cooperating gatherer on a 30x20 map at round 100, home (3,3), rotational symmetry).
 */
public class SentryTest {
    static MapLocation p(int x, int y) { return new MapLocation(x, y); }

    /** The gatherer at (x, y) facing f, with mine m listed and targeted, and no sentry or Nav history. */
    static void world(int x, int y, Direction f, MapLocation m) {
        FixTest.gatherer();
        me = p(x, y); facing = f; G.startTurn();
        Comms.numMines = 0; Comms.mines[Comms.numMines++] = m;
        Baby.mineTarget = m; Baby.minesAtPick = Comms.numMines; Baby.coopAtPick = G.coop;
        Baby.sentryOff = null; Baby.sentryOffRound = 0; Baby.pickRound = -100;
        Nav.target = null; Nav.stall = 0;
        clock(1 << 30);
    }

    /** One more round for the same rat: the cooldowns cool by 10 and the round advances. */
    static void nextRound() {
        round++; moveCd = Math.max(0, moveCd - 10); turnCd = Math.max(0, turnCd - 10); actCd = Math.max(0, actCd - 10);
        G.startTurn();
    }

    public static void main(String[] args) throws Exception {
        check(Cfg.SENTRY == 1 && Cfg.SENTRY_R == 1, "sentry: SENTRY 1, SENTRY_R 1 (sw82 = yl54 + SENTRY)");
        MapLocation m = p(10, 8);

        // at the mine centre: hold and turn 90 degrees clockwise a round, four turns face every quadrant
        world(10, 8, Direction.NORTH, m);
        Baby.moveDecision();
        check(Baby.why == 'Y' && me.equals(p(10, 8)) && facing == Direction.EAST, "sentry: at the mine, holds and turns N -> E (" + Baby.why + ", " + me + ", " + facing + ")");
        Direction[] want = {Direction.SOUTH, Direction.WEST, Direction.NORTH};
        for (Direction w : want) {
            nextRound();
            Baby.moveDecision();
            check(Baby.why == 'Y' && me.equals(p(10, 8)) && facing == w, "sentry: next round turns to " + w + " (" + facing + ", " + me + ")");
        }
        // a diagonal facing sweeps the diagonals
        world(10, 8, Direction.NORTHEAST, m);
        Baby.moveDecision();
        check(facing == Direction.SOUTHEAST, "sentry: NE -> SE (" + facing + ")");
        // the turn not ready: still holds, no turn, no move
        world(10, 8, Direction.NORTH, m);
        turnCd = 10;
        Baby.moveDecision();
        check(Baby.why == 'Y' && facing == Direction.NORTH && me.equals(p(10, 8)), "sentry: turn not ready: holds facing N (" + facing + ")");
        // within SENTRY_R (Chebyshev 1): holds there
        world(11, 9, Direction.WEST, m);
        Baby.moveDecision();
        check(Baby.why == 'Y' && me.equals(p(11, 9)) && facing == Direction.NORTH, "sentry: at Chebyshev 1, holds and turns W -> N (" + me + ", " + facing + ")");
        // at Chebyshev 2 (at the mine, outside SENTRY_R): walks one step in, no turn by the sentry
        world(12, 8, Direction.WEST, m);
        Baby.moveDecision();
        check(Baby.why == 'Y' && G.cheb(me, m) == 1, "sentry: at Chebyshev 2, walks in to Chebyshev 1 (" + me + ", " + Baby.why + ")");

        // the cases that keep the wander (why 'W')
        // a king of ours within Chebyshev 2 of the mine: its own view covers the square
        world(10, 8, Direction.NORTH, m);
        Comms.kings[0] = p(11, 10); Comms.numKings = 1;
        Baby.moveDecision();
        check(Baby.why == 'W', "sentry: a king of ours within Chebyshev 2 of the mine: wander (" + Baby.why + ")");
        world(10, 8, Direction.NORTH, m);
        Comms.kings[0] = p(13, 10); Comms.numKings = 1;
        Baby.moveDecision();
        check(Baby.why == 'Y', "sentry: our king at Chebyshev 3 of the mine: the sentry holds (" + Baby.why + ")");
        Comms.numKings = 0;
        // an enemy rat in view (nearestEnemy) or a guard tile: wander, the war branches own the facing
        world(10, 8, Direction.NORTH, m);
        Baby.nearestEnemy = new RobotInfo(77, Team.B, UnitType.BABY_RAT, 100, p(10, 12), Direction.SOUTH, 0, 0, null);
        check(!Baby.sentry(m), "sentry: an enemy rat in view: no sentry");
        Baby.nearestEnemy = null;
        world(10, 8, Direction.NORTH, m);
        Nav.guard = p(9, 9);
        check(!Baby.sentry(m), "sentry: a guard tile set: no sentry");
        Nav.guard = null;
        // carrying a rat: no sentry
        world(10, 8, Direction.NORTH, m);
        carrying = new RobotInfo(78, Team.B, UnitType.BABY_RAT, 40, me, Direction.NORTH, 0, 0, null);
        check(!Baby.sentry(m), "sentry: carrying a rat: no sentry");
        carrying = null;

        // a post walk stalled past 6 turns: sentryOff, and the rat wanders at that mine for 100 rounds
        world(12, 8, Direction.WEST, m);
        Nav.target = m; Nav.stall = 7;
        Baby.moveDecision();
        check(Baby.why == 'W' && m.equals(Baby.sentryOff) && Baby.sentryOffRound == round, "sentry: stalled post walk: sentryOff, wander (" + Baby.why + ")");
        int off = round;
        Nav.stall = 0; Nav.target = null;
        me = p(10, 8); round = off + 99; G.startTurn(); Baby.wanderRound = -10;
        check(!Baby.sentry(m), "sentry: 99 rounds after the stall: still off at that mine");
        round = off + 100; G.startTurn();
        check(Baby.sentry(m), "sentry: 100 rounds after the stall: on again");
        // the stall of another errand (Nav.target elsewhere) does not set sentryOff: the walk re-targets the mine
        world(12, 8, Direction.WEST, m);
        Nav.target = p(20, 15); Nav.stall = 9;
        Baby.moveDecision();
        check(Baby.why == 'Y' && Baby.sentryOff == null && m.equals(Nav.target), "sentry: another errand's stall: walks to the post (" + Baby.why + ", " + Nav.target + ")");
        // another mine is unaffected by sentryOff
        world(10, 8, Direction.NORTH, m);
        Baby.sentryOff = p(20, 15); Baby.sentryOffRound = round;
        check(Baby.sentry(m), "sentry: sentryOff names another mine: the sentry holds here");

        // the sweep's cone: after the turn sense2 sees the new quadrant's pile
        world(10, 8, Direction.NORTH, m);
        cheeseAt.put(p(13, 8), 20);      // east of the post, dist2 9: outside the north cone, inside the east one
        Baby.sense();
        check(Baby.cheeseTarget == null, "sentry: facing N, the pile to the east is unseen");
        Baby.moveDecision();
        Baby.sense();
        check(facing == Direction.EAST && p(13, 8).equals(Baby.cheeseTarget), "sentry: after the turn to E the pile is the target (" + Baby.cheeseTarget + ")");
        // with the target set, the next round walks to it (cheeseMove, before mineMove)
        nextRound();
        Baby.moveDecision();
        check(Baby.why == 'C' || Baby.why == 'P', "sentry: the next round walks to the pile (" + Baby.why + ")");

        // SENTRY off would wander: the branch reads Cfg.SENTRY (checked by value above); sweep() alone turns 90 degrees
        world(10, 8, Direction.SOUTHWEST, m);
        Baby.sweep();
        check(facing == Direction.NORTHWEST && turnCd == 10, "sweep: SW -> NW, one turn's cooldown (" + facing + ")");
        Comms.numMines = 0;
        System.out.println("SentryTest: " + (BotTest.checks - BotTest.fails) + "/" + BotTest.checks + " passed");
        System.exit(BotTest.fails == 0 ? 0 : 1);
    }
}
