package ratbot;

import battlecode.common.*;

import static ratbot.BotTest.*;

/**
 * sw87 = sw83 + CARRY_MICRO 1 (GrabMicro.act, GrabMicro.carrier; Baby.aimCarry). A rat carrying an enemy whose throw
 * cannot go this turn (action cooldown 10 or more: a grab's THROW_RAT_COOLDOWN 20 covers the carry's first two rounds)
 * steers by GrabMicro's threat scoring, with no grab or bite (why 'p'); with the throw ready, or an ally carried,
 * GrabMicro stays out as in sw83. With the throw not ready, Baby.aimCarry skips the steps in Nav.avoid (where GrabMicro
 * marks the steps clearly worse than staying).
 * Evidence (replica 11-24, sw83 vs goonv23, 6 games): 191 grabs of our carriers, 180 of them before the carrier's
 * throw could go; the engine drops a grabbed carrier's rat, free.
 */
public class CarryMicroTest {
    static MapLocation p(int x, int y) { return new MapLocation(x, y); }

    static RobotInfo enemy(int id, int x, int y, int hp, Direction f) {
        return new RobotInfo(id, Team.B, UnitType.BABY_RAT, hp, p(x, y), f, 0, 0, null);
    }

    /** A carried rat of team t (40 health). */
    static RobotInfo held(Team t) { return new RobotInfo(70, t, UnitType.BABY_RAT, 40, p(10, 8), Direction.NORTH, 0, 0, null); }

    /** War on the 30x20 map at round 200: our rat at (10,8) facing NORTH with health hp, carrying c (null: nothing),
     *  action cooldown acd, movement and turn ready; the enemies es placed (all in its view). Returns es. */
    static RobotInfo[] world(int hp, RobotInfo c, int acd, RobotInfo... es) {
        FixTest.gatherer();
        round = 200; me = p(10, 8); facing = Direction.NORTH; health = hp; G.startTurn();
        G.coop = false;
        carrying = c; actCd = acd; moveCd = 0; turnCd = 0;
        Nav.avoid = 0; GrabMicro.round = -1; GrabMicro.evalRound = -1; GrabMicro.carrier = false;
        bitten.clear(); thrown = null;
        for (RobotInfo e : es) robots.put(e.location, e);
        Baby.seen = es;
        clock(1 << 30);
        return es;
    }

    public static void main(String[] args) throws Exception {
        check(Cfg.CARRY_MICRO == 1, "carry micro: CARRY_MICRO 1 (sw87 = sw83 + CARRY_MICRO 1)");
        check(Debug.WHY.indexOf('p') == 18 && Debug.WHY.indexOf('m') == 17, "carry micro: 'p' has a tag slot (18), next to 'm' (17)");

        // a healthier free-handed enemy at Chebyshev 2 (it can step next to us and grab us from any side): the carrier
        // whose throw cannot go steps out of its reach (Chebyshev 3), why 'p', and keeps its rat (no grab, no bite)
        RobotInfo[] es = world(60, held(Team.B), 10, enemy(81, 11, 10, 100, Direction.SOUTH));
        boolean acted = GrabMicro.act(es);
        check(acted && Baby.why == 'p' && GrabMicro.round == 200, "carry micro: not ready, healthier enemy at 2: acts (" + acted + ", " + Baby.why + ")");
        check(G.cheb(me, p(11, 10)) >= 3 && me.y == 7, "carry micro: steps out of the enemy's reach (" + me + ")");
        check(carrying != null && carrying.team == Team.B && bitten.isEmpty() && thrown == null, "carry micro: keeps the carried rat, no bite, no throw");

        // the throw ready (action cooldown under 10): GrabMicro stays out, as sw83 (tryThrow and aimCarry decide)
        es = world(60, held(Team.B), 0, enemy(81, 11, 10, 100, Direction.SOUTH));
        acted = GrabMicro.act(es);
        check(!acted && me.equals(p(10, 8)) && GrabMicro.round != 200 && GrabMicro.evalRound != 200, "carry micro: throw ready: no micro (" + acted + ", " + me + ")");
        // action cooldown 9 is ready, 10 is not
        es = world(60, held(Team.B), 9, enemy(81, 11, 10, 100, Direction.SOUTH));
        check(!GrabMicro.act(es), "carry micro: action cooldown 9 (ready): no micro");

        // an ally carried (a corridor swap): no micro, as sw83
        es = world(60, held(Team.A), 10, enemy(81, 11, 10, 100, Direction.SOUTH));
        acted = GrabMicro.act(es);
        check(!acted && me.equals(p(10, 8)), "carry micro: an ally carried: no micro (" + acted + ")");

        // not carrying: the micro as sw83, tagged 'm'
        es = world(60, null, 10, enemy(81, 11, 10, 100, Direction.SOUTH));
        acted = GrabMicro.act(es);
        check(acted && Baby.why == 'm' && G.cheb(me, p(11, 10)) >= 3, "carry micro: not carrying: the sw83 micro, why 'm' (" + Baby.why + ", " + me + ")");

        // no enemy within Chebyshev 2: no micro
        es = world(60, held(Team.B), 10, enemy(81, 10, 12, 100, Direction.SOUTH));
        check(!GrabMicro.act(es) && me.equals(p(10, 8)), "carry micro: enemy at Chebyshev 4: no micro");

        // an enemy we see that is not healthier: it cannot grab us while in our cone, staying is safe: no micro
        es = world(60, held(Team.B), 10, enemy(81, 10, 10, 60, Direction.SOUTH));
        check(!GrabMicro.act(es) && me.equals(p(10, 8)) && facing == Direction.NORTH, "carry micro: an equal enemy in view: no micro");

        // too few bytecodes (GRAB_BC): no micro
        es = world(60, held(Team.B), 10, enemy(81, 11, 10, 100, Direction.SOUTH));
        clock(Cfg.GRAB_BC - 1);
        check(!GrabMicro.act(es) && me.equals(p(10, 8)), "carry micro: under GRAB_BC bytecodes: no micro");
        clock(1 << 30);

        // the Chebyshev-3 case: a healthier enemy at 3; the steps to Chebyshev 2 of it go into Nav.avoid
        es = world(60, held(Team.B), 10, enemy(81, 10, 11, 100, Direction.SOUTH));
        acted = GrabMicro.act(es);
        int north = 1 << Direction.NORTH.ordinal(), ne = 1 << Direction.NORTHEAST.ordinal(), south = 1 << Direction.SOUTH.ordinal();
        check(!acted && (Nav.avoid & north) != 0 && (Nav.avoid & ne) != 0 && (Nav.avoid & south) == 0,
              "carry micro: healthier enemy at Chebyshev 3: the steps toward it avoided (" + Integer.toBinaryString(Nav.avoid) + ")");

        // aimCarry: a target enemy at (10,11), 3 north of us: the only aim is the step north (target 2 ahead)
        es = world(100, held(Team.B), 10, enemy(82, 10, 11, 100, Direction.NORTH));
        Nav.avoid = north;
        boolean aimed = Baby.aimCarry();
        check(!aimed && me.equals(p(10, 8)) && facing == Direction.NORTH && thrown == null,
              "carry micro: aimCarry, throw not ready: no step into Nav.avoid (" + aimed + ", " + me + ")");
        es = world(100, held(Team.B), 10, enemy(82, 10, 11, 100, Direction.NORTH));
        Nav.avoid = 0;
        aimed = Baby.aimCarry();
        check(aimed && me.equals(p(10, 9)) && thrown == null, "carry micro: aimCarry, throw not ready, step free: lines up (" + aimed + ", " + me + ")");
        es = world(100, held(Team.B), 0, enemy(82, 10, 11, 100, Direction.NORTH));
        Nav.avoid = north;
        aimed = Baby.aimCarry();
        check(aimed && me.equals(p(10, 9)) && thrown == Direction.NORTH,
              "carry micro: aimCarry, throw ready: steps and throws as sw83 whatever Nav.avoid (" + aimed + ", " + me + ", " + thrown + ")");

        // the whole turn: a carrier whose throw cannot go, a healthier enemy at Chebyshev 2: the micro's step, why 'p'
        es = world(60, held(Team.B), 10, enemy(81, 11, 10, 100, Direction.SOUTH));
        Baby.carryRound = round - 1;
        Baby.turn();
        check(Baby.why == 'p' && G.cheb(me, p(11, 10)) >= 3 && carrying != null, "carry micro: Baby.turn: the carrier steps away (" + Baby.why + ", " + me + ")");

        carrying = null; robots.clear(); Baby.seen = new RobotInfo[0]; Nav.avoid = 0;
        System.out.println("CarryMicroTest: " + (BotTest.checks - BotTest.fails) + "/" + BotTest.checks + " passed");
        System.exit(BotTest.fails == 0 ? 0 : 1);
    }
}
