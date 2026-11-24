package ratbot;

import battlecode.common.*;

import static ratbot.BotTest.*;

/**
 * sw78's trap risk in a fight for gatherers (Cfg.FIGHT_RISK_GATHER 3; 11-24): sw75's FIGHT_RISK for role GATHER only.
 * With an enemy baby within dist2 20 (Baby's `engaged`) the champion yl50 drops the TrapRisk avoid mask entirely; sw78
 * keeps the marks of the last FIGHT_RISK_GATHER rounds for a gatherer (Baby.fightRisk), so it does not step within
 * Chebyshev 1 of the front tiles of an enemy baby it saw lately. Fighters and attackers are not masked: sw75 masked
 * every role and is -173 vs yl50 on 160 games. Replica 40715 and 40515: 103 of our rats died on traps in six games.
 * We stand at (5,5) facing north (+y) on an open 30x20 map with 100 health.
 */
public class FightRiskTest {
    static RobotInfo rat(int id, int x, int y, int hp, Direction d) {
        return new RobotInfo(id, Team.B, UnitType.BABY_RAT, hp, new MapLocation(x, y), d, 0, 0, null);
    }

    static int bits(Direction... ds) {
        int m = 0;
        for (Direction d : ds) m |= 1 << d.ordinal();
        return m;
    }

    static void world(int r) {
        reset(); install();
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, false, false, false);
        round = r; G.startTurn();
        G.coop = false;
        health = 100;
        Baby.role = Baby.GATHER; Baby.hunting = false; Baby.eKing = null; Comms.numKings = 0;
        Nav.stall = 0; Nav.avoid = 0;
        TrapRisk.mark = null; TrapRisk.last = null; TrapRisk.arrived = -1; TrapRisk.lastObs = 0;
    }

    /** Observe e at round r (it marks its three front tiles) with us at (5,5). */
    static void see(int r, RobotInfo e) throws Exception {
        world(r);
        robots.put(e.location, e);
        TrapRisk.observe(G.rc.senseNearbyRobots(-1, null));
    }

    /** Round r of the same turn state; G.startTurn re-reads cooperation from the mock, so war is set again. */
    static void at(int r) { round = r; G.startTurn(); G.coop = false; }

    public static void main(String[] args) throws Exception {
        check(Cfg.FIGHT_RISK_GATHER == 3, "fight risk: FIGHT_RISK_GATHER 3 (sw78)");
        int front = bits(Direction.NORTHWEST, Direction.NORTH, Direction.NORTHEAST);

        // an enemy at (5,8) facing us marks (4..6, 7): our three northern neighbours (y 6) are within Chebyshev 1
        see(300, rat(80, 5, 8, 100, Direction.SOUTH));
        check(TrapRisk.mark != null && TrapRisk.mark[G.idx(new MapLocation(5, 7))] != 0, "fight risk: setup: (5,7) is marked");
        check(Baby.fightRisk(true) == front, "fight risk: engaged, the tiles toward a facing enemy are avoided (mask "
              + Integer.toBinaryString(Baby.fightRisk(true)) + ", want " + Integer.toBinaryString(front) + ")");
        check(Baby.fightRisk(false) == 0, "fight risk: not engaged, fightRisk adds nothing (the full TrapRisk mask applies)");
        check(TrapRisk.mask(G.me) == front, "fight risk: setup: the full mask is the same here");

        // the marks last FIGHT_RISK_GATHER rounds: laid at 300, still avoided at 303, gone at 304
        at(303);
        check(Baby.fightRisk(true) == front, "fight risk: a mark 3 rounds old is still avoided");
        at(304);
        check(!G.coop && Baby.fightRisk(true) == 0, "fight risk: a mark 4 rounds old is not (FIGHT_RISK_GATHER 3)");
        check(TrapRisk.mask(G.me) == front, "fight risk: ... while the full mask (150 rounds) still holds it");

        // an enemy facing away marks (4..6, 9): nothing near us
        see(300, rat(81, 5, 8, 100, Direction.NORTH));
        check(Baby.fightRisk(true) == 0, "fight risk: an enemy facing away taints none of our neighbours");

        // the exemptions: in cooperation, hunting prey, stalled 8 turns
        see(300, rat(80, 5, 8, 100, Direction.SOUTH));
        G.coop = true;
        check(Baby.fightRisk(true) == 0, "fight risk: off in cooperation");
        G.coop = false; Baby.hunting = true;
        check(Baby.fightRisk(true) == 0, "fight risk: off while hunting prey");
        Baby.hunting = false; Nav.stall = 8;
        check(Baby.fightRisk(true) == 0, "fight risk: off after Nav.stall 8");
        Nav.stall = 7;
        check(Baby.fightRisk(true) == front, "fight risk: on at Nav.stall 7");
        Baby.role = Baby.FIGHT;
        check(Baby.fightRisk(true) == 0, "fight risk: a fighter is not masked (sw78: gatherers only)");
        Baby.role = Baby.ATTACK;
        check(Baby.fightRisk(true) == 0, "fight risk: an attacker is not masked");
        Baby.role = Baby.GATHER;
        check(Baby.fightRisk(true) == front, "fight risk: a gatherer is masked");
        Nav.stall = 0;

        // in war(): a weaker enemy that faces us is not held off (the standoff needs one at least as healthy), so
        // yl50 walks straight at it, onto (5,6) next to its front tiles; a rat with the mask does not step into the band
        see(300, rat(82, 5, 8, 60, Direction.SOUTH));
        Baby.nearestEnemy = robots.get(new MapLocation(5, 8));
        Baby.seen = G.rc.senseNearbyRobots(-1, null);
        Nav.avoid = Baby.fightRisk(true);
        MapLocation from = me;
        Baby.role = Baby.FIGHT;   // war() is a fighter's step; Nav.avoid holds the mask fightRisk gave the gatherer
        Baby.war();
        check(me.y <= 5, "fight risk: with a gatherer's mask the step toward a weaker facing enemy keeps out of its front band (from " + from + " to " + me + ")");
        // sw78: a fighter is not masked, so war() walks into the band as yl50 does (sw75 held it off)
        see(300, rat(82, 5, 8, 60, Direction.SOUTH));
        Baby.nearestEnemy = robots.get(new MapLocation(5, 8));
        Baby.seen = G.rc.senseNearbyRobots(-1, null);
        Baby.role = Baby.FIGHT;
        Nav.avoid = Baby.fightRisk(true);
        Baby.war();
        check(Nav.avoid == 0 && me.y == 6, "fight risk: a fighter's war() still walks at a weaker facing enemy (to " + me + ")");
        // with the mask off it walks in (sw71's behaviour)
        see(300, rat(82, 5, 8, 60, Direction.SOUTH));
        Baby.nearestEnemy = robots.get(new MapLocation(5, 8));
        Baby.seen = G.rc.senseNearbyRobots(-1, null);
        Nav.avoid = 0;
        Baby.role = Baby.FIGHT;
        Baby.war();
        check(me.y == 6, "fight risk: setup: without the mask war() steps toward it into the band (to " + me + ")");

        G.coop = true; Baby.nearestEnemy = null; Baby.seen = new RobotInfo[0]; Nav.avoid = 0;
        System.out.println("FightRiskTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
