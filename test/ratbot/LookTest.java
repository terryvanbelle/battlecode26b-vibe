package ratbot;

import battlecode.common.*;

import static ratbot.BotTest.*;

/**
 * sw83 = sw82 (yl54 + SENTRY) + SENTRY_LOOK 3 (Baby.look, read in Baby.moveDecision's delivery branch; Baby.pickRound,
 * set by Baby.take). A rat on our half holding a part load (DELIVER_IDLE..DELIVER_AT-1 raw) with no pile in sight, no
 * enemy rat in view, nothing carried and before round 1961 sweeps in place (why 'L': a 90-degree turn, no move) in the
 * first SENTRY_LOOK rounds after its last pickup, instead of heading home; then it delivers as before.
 * Evidence: local games (sw81 vs yl54, 8 games): 81% of baby transfers are a single 20-pile; after 22% of the own-half
 * pickups that leave 10-29 raw another pile lay within dist2 20 outside the rat's cone.
 * Uses FixTest.gatherer (a cooperating gatherer on a 30x20 map at round 100, home (3,3), rotational symmetry).
 */
public class LookTest {
    static MapLocation p(int x, int y) { return new MapLocation(x, y); }

    /** The gatherer at (x, y) facing NORTH with raw cheese r, its last pickup ago rounds back, our king at (3,3). */
    static void world(int x, int y, int r, int ago) {
        FixTest.gatherer();
        me = p(x, y); facing = Direction.NORTH; G.startTurn();
        raw = r; G.rawCheese = r;
        Comms.kings[0] = p(3, 3); Comms.numKings = 1;
        Comms.numMines = 0;
        Baby.sentryOff = null; Baby.pickRound = G.round - ago;
        Nav.target = null; Nav.stall = 0;
        clock(1 << 30);
    }

    public static void main(String[] args) throws Exception {
        check(Cfg.SENTRY_LOOK == 3 && Cfg.SENTRY == 1, "look: SENTRY_LOOK 3 on SENTRY 1 (sw83 = sw82 + SENTRY_LOOK 3)");
        check(Cfg.DELIVER_IDLE == 10 && Cfg.DELIVER_AT == 30, "look: the part-load band is DELIVER_IDLE 10 .. DELIVER_AT 30");
        // the pickup round and the next two: look (turn 90 degrees, no move)
        Direction[] after = {Direction.EAST, Direction.EAST, Direction.EAST};
        for (int ago = 0; ago < 3; ago++) {
            world(10, 8, 20, ago);
            Baby.moveDecision();
            check(Baby.why == 'L' && me.equals(p(10, 8)) && facing == after[ago], "look: " + ago + " rounds after the pickup, 20 raw: looks (" + Baby.why + ", " + facing + ")");
        }
        // the third round after: the trip home as before
        world(10, 8, 20, 3);
        Baby.moveDecision();
        check(Baby.why == 'd' || Baby.why == 'D', "look: 3 rounds after the pickup: delivers (" + Baby.why + ")");
        // the band: 10 raw looks, 9 raw (under DELIVER_IDLE) does not deliver either way, 30 raw (DELIVER_AT) delivers
        world(10, 8, 10, 0);
        Baby.moveDecision();
        check(Baby.why == 'L', "look: 10 raw: looks (" + Baby.why + ")");
        world(10, 8, 30, 0);
        Baby.moveDecision();
        check(Baby.why == 'd' || Baby.why == 'D', "look: 30 raw (DELIVER_AT): delivers at once (" + Baby.why + ")");
        world(10, 8, 9, 0);
        Baby.moveDecision();
        check(Baby.why != 'L' && Baby.why != 'd' && Baby.why != 'D', "look: 9 raw: no trip, no look (" + Baby.why + ")");
        // a pile in sight: no look (the cheese target is walked to)
        world(10, 8, 20, 0);
        Baby.cheeseTarget = p(10, 11);
        check(!Baby.look(20, false), "look: a pile in sight: no look");
        Baby.cheeseTarget = null;
        // the enemy half: delivers
        world(22, 14, 20, 0);
        check(!Baby.homeHalf(me), "look: (22,14) is on the enemy half");
        Baby.moveDecision();
        check(Baby.why == 'd' || Baby.why == 'D', "look: on the enemy half: delivers (" + Baby.why + ")");
        // late (after round 1960): delivers
        world(10, 8, 20, 0);
        round = 1961; G.startTurn(); Baby.pickRound = round;
        Baby.moveDecision();
        check(Baby.why == 'd' || Baby.why == 'D', "look: round 1961: delivers (" + Baby.why + ")");
        // an enemy rat in view, a guard tile, a carried rat: no look
        world(10, 8, 20, 0);
        Baby.nearestEnemy = new RobotInfo(77, Team.B, UnitType.BABY_RAT, 100, p(10, 12), Direction.SOUTH, 0, 0, null);
        check(!Baby.look(20, false), "look: an enemy rat in view: no look");
        Baby.nearestEnemy = null;
        world(10, 8, 20, 0);
        Nav.guard = p(9, 9);
        check(!Baby.look(20, false), "look: a guard tile: no look");
        Nav.guard = null;
        world(10, 8, 20, 0);
        carrying = new RobotInfo(78, Team.B, UnitType.BABY_RAT, 40, me, Direction.NORTH, 0, 0, null);
        check(!Baby.look(20, false), "look: carrying a rat: no look");
        carrying = null;
        // the look finds a pile outside the first cone: after the turn, sense sees it and the next round walks to it
        world(10, 8, 20, 0);
        cheeseAt.put(p(13, 8), 20);
        Baby.sense();
        check(Baby.cheeseTarget == null, "look: facing N, the pile to the east is unseen");
        Baby.moveDecision();
        Baby.sense();
        check(Baby.why == 'L' && p(13, 8).equals(Baby.cheeseTarget), "look: after the turn to E the pile is the target (" + Baby.cheeseTarget + ")");
        round++; turnCd = 0; G.startTurn();
        Baby.moveDecision();
        check(Baby.why == 'C' || Baby.why == 'P', "look: the next round walks to the pile, no trip home (" + Baby.why + ")");
        cheeseAt.clear();
        // take() records the pickup round
        world(10, 8, 0, 50);
        cheeseAt.put(p(10, 9), 20);
        Baby.pickUp();
        check(Baby.pickRound == round, "look: a pickup sets pickRound to this round (" + Baby.pickRound + ", " + round + ")");
        cheeseAt.clear();
        Comms.numKings = 0;
        System.out.println("LookTest: " + (BotTest.checks - BotTest.fails) + "/" + BotTest.checks + " passed");
        System.exit(BotTest.fails == 0 ? 0 : 1);
    }
}
