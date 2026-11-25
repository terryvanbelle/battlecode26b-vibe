package ratbot;

import battlecode.common.*;

import static ratbot.BotTest.*;

/**
 * sw92 = sw83 + HIT_RUN (GrabMicro.evaluate, GrabMicro.execute). The grab micro also scores, for each end tile and
 * facing that takes no action there (no grab or bite at the end tile, no hand-over), the bite it can make first from
 * where it stands: the best enemy baby next to us inside our cone now, valued as the micro values bites (KILL, FLIP,
 * BITE), with that enemy's threat counted at its health after the bite. When such an option wins, execute bites before
 * it turns or steps (Baby.bitesRat and GrabMicro.hitRuns count it).
 * Evidence (replica 11-25, 8 matches vs AdamTan12_Icarus, bman12345678_goonv23, thewaxmango_collegejunior and
 * daannte_v47_Skadoosh): 19-24% of the bites of three of those teams came before a step away from the target; 7 of
 * sw83's 1,784 did (sw83's micro ties its action to the end tile).
 * Uses FixTest.gatherer (a gatherer on a 30x20 map) at war, round 200; GrabMicro.act is called directly.
 */
public class HitRunTest {
    static MapLocation p(int x, int y) { return new MapLocation(x, y); }

    static RobotInfo enemy(int id, int x, int y, int hp, Direction f) {
        return new RobotInfo(id, Team.B, UnitType.BABY_RAT, hp, p(x, y), f, 0, 0, null);
    }

    /** War at round 200: our rat at (10,8) facing f with health hp, nothing carried, every cooldown ready; es placed
     *  (all in view). Returns es. */
    static RobotInfo[] world(Direction f, int hp, RobotInfo... es) {
        FixTest.gatherer();
        round = 200; me = p(10, 8); facing = f; health = hp; G.startTurn();
        G.coop = false;
        carrying = null; actCd = 0; moveCd = 0; turnCd = 0; carryOk = true; raw = 0;
        Nav.avoid = 0; GrabMicro.round = -1; GrabMicro.evalRound = -1;
        bitten.clear();
        for (RobotInfo e : es) robots.put(e.location, e);
        Baby.seen = es;
        clock(1 << 30);
        return es;
    }

    static final int NW = 1 << 7, NE = 1 << 1;

    public static void main(String[] args) throws Exception {
        check(Cfg.HIT_RUN, "hit and run: HIT_RUN on (sw92 = sw83 + HIT_RUN)");
        check(GrabMicro.BITE == Cfg.GRAB_BITE && Cfg.GRAB_THREAT == 15 && Cfg.GRAB_MARGIN == 10,
              "hit and run: the test's scores assume GRAB_BITE 20, GRAB_THREAT 15, GRAB_MARGIN 10");

        // P (healthier, in our cone to the north, facing us) and Q (healthier, two tiles north): both can grab us
        // wherever we face. Staying and biting P scores BITE - 2 threats = -10; a step south (or SW, SE) leaves only P
        // a threat (Q at Chebyshev 3), -15 without a bite: sw83 stays and bites. With the bite first the step scores
        // BITE - 15 = 5, a MARGIN better: bite P from here, then step away. (The side steps NW and NE, where P cannot
        // see us and a grab of P wins, are kept out with Nav.avoid.)
        RobotInfo[] es = world(Direction.NORTH, 60, enemy(81, 10, 9, 100, Direction.SOUTH), enemy(82, 10, 10, 100, Direction.SOUTH));
        Nav.avoid = NW | NE;
        int hr = GrabMicro.hitRuns, br = Baby.bitesRat;
        boolean acted = GrabMicro.act(es);
        check(acted && GrabMicro.round == 200 && Baby.why == 'm', "hit and run: the micro acts (" + acted + ", " + Baby.why + ")");
        check(bitten.size() == 1 && bitten.get(0).equals(p(10, 9)), "hit and run: bites P (" + bitten + ")");
        check(me.y == 7 && me.x >= 9 && me.x <= 11, "hit and run: then steps away from P, out of Q's reach (" + me + ")");
        check(GrabMicro.hitRuns == hr + 1 && Baby.bitesRat == br + 1, "hit and run: hitRuns and bitesRat count the bite");
        check(GrabMicro.preI >= 0 && GrabMicro.grabI < 0 && GrabMicro.biteI < 0, "hit and run: the plan bites first, nothing at the end tile");
        check(GrabMicro.bestS - GrabMicro.BITE < GrabMicro.curS + Cfg.GRAB_MARGIN && GrabMicro.bestS >= GrabMicro.curS + Cfg.GRAB_MARGIN,
              "hit and run: only the bite first makes the step beat staying (" + GrabMicro.bestS + " vs " + GrabMicro.curS + ")");

        // the action not ready: no bite first; the step alone beats staying without a bite (-15 against -30)
        es = world(Direction.NORTH, 60, enemy(81, 10, 9, 100, Direction.SOUTH), enemy(82, 10, 10, 100, Direction.SOUTH));
        Nav.avoid = NW | NE; actCd = 10;
        hr = GrabMicro.hitRuns;
        acted = GrabMicro.act(es);
        check(acted && me.y == 7 && GrabMicro.preI < 0 && GrabMicro.hitRuns == hr && bitten.isEmpty(),
              "hit and run: action not ready: steps away without a bite (" + me + ", " + bitten + ")");

        // a grab at the end tile (NW: P cannot see us there) beats biting first: the plan grabs, no bite first
        es = world(Direction.NORTH, 60, enemy(81, 10, 9, 100, Direction.SOUTH), enemy(82, 10, 10, 100, Direction.SOUTH));
        hr = GrabMicro.hitRuns;
        acted = GrabMicro.act(es);
        check(acted && carrying != null && carrying.ID == 81 && bitten.isEmpty() && GrabMicro.hitRuns == hr && GrabMicro.preI < 0,
              "hit and run: a grab at the end tile is taken without a bite first (" + carrying + ", " + bitten + ")");
        carrying = null;

        // P outside our cone (we face EAST, P is north): no bite first. The micro turns to a facing that sees P and
        // bites it in place (-10) rather than step south (-15); with a bite first offered for P the step would have
        // won (5)
        es = world(Direction.EAST, 60, enemy(81, 10, 9, 100, Direction.SOUTH), enemy(82, 10, 10, 100, Direction.SOUTH));
        Nav.avoid = NW | NE;
        hr = GrabMicro.hitRuns;
        acted = GrabMicro.act(es);
        boolean seesP = facing == Direction.NORTHWEST || facing == Direction.NORTH || facing == Direction.NORTHEAST;
        check(acted && me.equals(p(10, 8)) && seesP && bitten.size() == 1 && GrabMicro.hitRuns == hr && GrabMicro.biteI >= 0,
              "hit and run: an enemy outside the cone gets no bite first; turn and bite in place (" + me + ", " + facing + ", " + bitten + ")");

        // bite then turn (movement not ready), all at health 10: P to the north sees us (no grab) and a bite kills it
        // (KILL); Q two tiles west is a threat unless we face WEST, whose cone covers every tile next to us it can
        // reach. Facing WEST does not see P: kill P from here first, then turn WEST (60), better than turning NW and
        // biting there (45) or staying (45)
        es = world(Direction.NORTH, 10, enemy(81, 10, 9, 10, Direction.SOUTH), enemy(82, 8, 8, 10, Direction.EAST));
        moveCd = 10;
        hr = GrabMicro.hitRuns;
        acted = GrabMicro.act(es);
        check(acted && bitten.size() == 1 && bitten.get(0).equals(p(10, 9)) && facing == Direction.WEST && me.equals(p(10, 8)),
              "hit and run: bite then turn: kills P, then faces WEST (" + bitten + ", " + facing + ", " + me + ")");
        check(GrabMicro.hitRuns == hr + 1 && GrabMicro.bestS == GrabMicro.KILL && GrabMicro.curS == GrabMicro.KILL - Cfg.GRAB_THREAT,
              "hit and run: bite then turn scores KILL against KILL - THREAT for staying (" + GrabMicro.bestS + ", " + GrabMicro.curS + ")");

        // the same without a better facing (turn not ready either): the stay-and-bite option is sw83's; the micro
        // does not act and leaves the bite to biteEnemies
        es = world(Direction.NORTH, 10, enemy(81, 10, 9, 10, Direction.SOUTH), enemy(82, 8, 8, 10, Direction.EAST));
        moveCd = 10; turnCd = 10;
        acted = GrabMicro.act(es);
        check(!acted && bitten.isEmpty() && GrabMicro.preI < 0, "hit and run: nothing better than staying: no act, no bite (" + acted + ")");

        // a bite first never goes with a hand-over: with cheese held and our king in reach after the step south, the
        // facings that hand over score GM_DELIVER and take no bite first
        es = world(Direction.NORTH, 60, enemy(81, 10, 9, 100, Direction.SOUTH), enemy(82, 10, 10, 100, Direction.SOUTH));
        RobotInfo ours = new RobotInfo(60, Team.A, UnitType.RAT_KING, 500, p(10, 5), Direction.NORTH, 0, 0, null);
        for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) robots.put(p(10 + dx, 5 + dy), ours);
        Nav.avoid = NW | NE; raw = 5;
        hr = GrabMicro.hitRuns;
        acted = GrabMicro.act(new RobotInfo[]{es[0], es[1], ours});
        boolean deliverFacing = GrabMicro.dlv && (GrabMicro.DM[GrabMicro.bestD] >> GrabMicro.bestF & 1) != 0;
        check(acted && GrabMicro.dlv && deliverFacing && GrabMicro.preI < 0 && bitten.isEmpty() && GrabMicro.hitRuns == hr && me.y == 7,
              "hit and run: a hand-over facing never bites first: step south, face the king, no bite (preI " + GrabMicro.preI
              + ", dlv " + GrabMicro.dlv + ", d " + GrabMicro.bestD + ", f " + GrabMicro.bestF + ", " + me + ", " + bitten + ")");
        raw = 0;

        robots.clear(); carryOk = true; carrying = null; G.coop = true; Nav.avoid = 0;
        System.out.println("HitRunTest: " + (BotTest.checks - BotTest.fails) + "/" + BotTest.checks + " passed");
        System.exit(BotTest.fails == 0 ? 0 : 1);
    }
}
