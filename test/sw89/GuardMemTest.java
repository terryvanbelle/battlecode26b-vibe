package ratbot;

import battlecode.common.*;

import static ratbot.BotTest.*;

/**
 * sw89 = sw83 + GUARD_MEM 4 (Baby.memGuard, Baby.remember, Baby.recall; called from Baby.turn's guard setup). At war a
 * baby carrying nothing and hunting no prey, with no enemy baby in view within GUARD_DIST2 at the start of its turn,
 * keeps facing the last tile of the nearest free-handed enemy baby it saw in the last GUARD_MEM rounds before this one,
 * when that tile is within GUARD_DIST2 of it and out of its view (a remembered tile in view with no enemy baby on it is
 * forgotten). sw88 (not entered) tested only Nav.guard == null, which also holds with an enemy baby in view within
 * GUARD_DIST2 when nearestEnemy skipped it (walled) or is prey further away; its memory then turned the rat away from
 * the enemy in view.
 * Evidence (replica 11-24, sw83 vs goonv23, 12 games): 28% of the grabs of our free rats came from an enemy outside the
 * victim's cone at its last turn that had been in it 2-6 turns before.
 * Uses FixTest.gatherer (a gatherer on a 30x20 map) at war, round 200.
 */
public class GuardMemTest {
    static MapLocation p(int x, int y) { return new MapLocation(x, y); }

    static RobotInfo enemy(int id, int x, int y) {
        return new RobotInfo(id, Team.B, UnitType.BABY_RAT, 100, p(x, y), Direction.SOUTH, 0, 0, null);
    }

    static RobotInfo carrier(int id, int x, int y) {
        RobotInfo held = new RobotInfo(99, Team.A, UnitType.BABY_RAT, 40, p(x, y), Direction.NORTH, 0, 0, null);
        return new RobotInfo(id, Team.B, UnitType.BABY_RAT, 100, p(x, y), Direction.SOUTH, 0, 0, held);
    }

    /** War at round rd: our rat at (10,8) facing f, health 100, nothing carried, every cooldown ready, Nav.guard unset;
     *  es placed and in this turn's view; the memory emptied when fresh. */
    static RobotInfo[] world(int rd, Direction f, boolean fresh, RobotInfo... es) {
        FixTest.gatherer();
        round = rd; me = p(10, 8); facing = f; health = 100; G.startTurn();
        G.coop = false;
        carrying = null; actCd = 0; moveCd = 0; turnCd = 0;
        Nav.avoid = 0; Nav.guard = null; GrabMicro.round = -1; GrabMicro.evalRound = -1;
        if (fresh) for (int i = 0; i < Baby.MEM_N; i++) Baby.memLoc[i] = null;
        for (RobotInfo e : es) robots.put(e.location, e);
        Baby.seen = es;
        clock(1 << 30);
        return es;
    }

    /** The memory's entry for robot id, or -1. */
    static int entry(int id) {
        for (int i = 0; i < Baby.MEM_N; i++) if (Baby.memLoc[i] != null && Baby.memId[i] == id) return i;
        return -1;
    }

    public static void main(String[] args) throws Exception {
        check(Cfg.GUARD_MEM == 4 && Cfg.GUARD_DIST2 == 13, "guard memory: GUARD_MEM 4 on GUARD_DIST2 13 (sw89 = sw83 + GUARD_MEM 4)");

        // remember: an enemy baby in view is recorded (id, tile, round); allies and kings are not
        RobotInfo ally = new RobotInfo(50, Team.A, UnitType.BABY_RAT, 100, p(9, 9), Direction.NORTH, 0, 0, null);
        RobotInfo king = new RobotInfo(51, Team.B, UnitType.RAT_KING, 500, p(14, 14), Direction.NORTH, 0, 0, null);
        RobotInfo[] es = world(200, Direction.NORTH, true, enemy(81, 10, 11), ally, king);
        Baby.remember(es);
        int e = entry(81);
        check(e >= 0 && Baby.memLoc[e].equals(p(10, 11)) && Baby.memRound[e] == 200, "guard memory: an enemy baby in view is remembered");
        check(entry(50) < 0 && entry(51) < 0, "guard memory: an ally and an enemy king are not");
        check(Baby.recall() == null, "guard memory: an enemy seen this round is not recalled (nearestEnemy guards it)");

        // the next round it is out of view; we face SOUTH, so its tile (north of us, dist2 9) is behind us: recalled
        world(201, Direction.SOUTH, false);
        check(p(10, 11).equals(Baby.recall()), "guard memory: out of view a round later, behind us: recalled");
        // facing NORTH its tile is in view and empty: forgotten
        world(201, Direction.NORTH, false);
        check(Baby.recall() == null && entry(81) < 0, "guard memory: its tile in view and empty: forgotten");
        // in view with an enemy baby on it: kept
        world(200, Direction.NORTH, true, enemy(81, 10, 11));
        Baby.remember(Baby.seen);
        world(201, Direction.NORTH, false, enemy(82, 10, 11));
        check(p(10, 11).equals(Baby.recall()), "guard memory: its tile in view with an enemy baby on it: kept");

        // age: recalled through GUARD_MEM rounds, dropped after
        world(200, Direction.NORTH, true, enemy(81, 10, 11));
        Baby.remember(Baby.seen);
        world(200 + Cfg.GUARD_MEM, Direction.SOUTH, false);
        check(p(10, 11).equals(Baby.recall()), "guard memory: " + Cfg.GUARD_MEM + " rounds old: recalled");
        world(201 + Cfg.GUARD_MEM, Direction.SOUTH, false);
        check(Baby.recall() == null && entry(81) < 0, "guard memory: " + (Cfg.GUARD_MEM + 1) + " rounds old: dropped");

        // range: a remembered tile beyond GUARD_DIST2 (dist2 16) is not guarded (and stays remembered)
        world(200, Direction.NORTH, true, enemy(83, 10, 12));
        Baby.remember(Baby.seen);
        world(201, Direction.SOUTH, false);
        check(Baby.recall() == null && entry(83) >= 0, "guard memory: a tile at dist2 16 (> GUARD_DIST2): not guarded, still remembered");
        me = p(10, 9); G.startTurn(); G.coop = false;
        check(p(10, 12).equals(Baby.recall()), "guard memory: closer (dist2 9): guarded");

        // the nearest of two remembered enemies
        world(200, Direction.NORTH, true, enemy(81, 10, 11), enemy(84, 12, 9));
        Baby.remember(Baby.seen);
        world(201, Direction.SOUTH, false);
        check(p(12, 9).equals(Baby.recall()), "guard memory: the nearer of two remembered enemies (dist2 5 before 9)");

        // an enemy carrying a robot cannot grab: not remembered, and its old entry is dropped
        world(200, Direction.NORTH, true, carrier(85, 10, 11));
        Baby.remember(Baby.seen);
        check(entry(85) < 0, "guard memory: an enemy carrying a robot is not remembered");
        world(200, Direction.NORTH, true, enemy(85, 10, 11));
        Baby.remember(Baby.seen);
        world(201, Direction.NORTH, false, carrier(85, 10, 10));
        Baby.remember(Baby.seen);
        check(entry(85) < 0, "guard memory: an enemy seen carrying drops its entry");

        // the ring: a ninth enemy replaces the oldest entry
        world(190, Direction.NORTH, true);
        for (int k = 0; k < 9; k++) {
            round = 190 + k; G.startTurn(); G.coop = false;
            Baby.remember(new RobotInfo[]{enemy(100 + k, 5 + k, 15)});
        }
        boolean ring = entry(100) < 0;
        for (int k = 1; k < 9; k++) ring &= entry(100 + k) >= 0;
        check(ring, "guard memory: 9 enemies in 8 entries: the oldest is replaced");
        // a re-sighting updates its own entry instead of taking another
        round = 199; G.startTurn(); G.coop = false;
        Baby.remember(new RobotInfo[]{enemy(101, 6, 14)});
        int u = entry(101);
        check(u >= 0 && Baby.memLoc[u].equals(p(6, 14)) && Baby.memRound[u] == 199 && entry(102) >= 0, "guard memory: a re-sighting updates its entry");

        // memGuard: no guard set and nothing carried: guards the recalled tile
        world(200, Direction.NORTH, true, enemy(81, 10, 11));
        Baby.remember(Baby.seen);
        world(201, Direction.SOUTH, false);
        Baby.memGuard(Baby.seen);
        check(p(10, 11).equals(Nav.guard), "guard memory: memGuard sets Nav.guard to the recalled tile (" + Nav.guard + ")");
        // a guard already set (an enemy in view within GUARD_DIST2) is kept
        world(201, Direction.SOUTH, false);
        Nav.guard = p(9, 6);
        Baby.memGuard(Baby.seen);
        check(p(9, 6).equals(Nav.guard), "guard memory: a guard from an enemy in view is kept");
        // carrying a rat: no memory guard
        world(201, Direction.SOUTH, false);
        carrying = new RobotInfo(70, Team.B, UnitType.BABY_RAT, 40, p(10, 8), Direction.NORTH, 0, 0, null);
        Baby.memGuard(Baby.seen);
        check(Nav.guard == null, "guard memory: carrying a rat: no memory guard");
        carrying = null;
        // an enemy baby in view within GUARD_DIST2 but no guard set (nearestEnemy was prey further away): no memory
        // guard, guardTurn faces the one in view as in sw83 (sw88 guarded the remembered tile here)
        world(200, Direction.NORTH, true, enemy(81, 10, 11));
        Baby.remember(Baby.seen);
        world(201, Direction.SOUTH, false, enemy(87, 11, 6));
        Baby.memGuard(Baby.seen);
        check(Nav.guard == null, "guard memory: an enemy baby in view within GUARD_DIST2: no memory guard (" + Nav.guard + ")");
        // one in view beyond GUARD_DIST2 (dist2 16) does not stop it
        world(201, Direction.SOUTH, false, enemy(87, 10, 4));
        Baby.memGuard(Baby.seen);
        check(p(10, 11).equals(Nav.guard), "guard memory: an enemy baby in view at dist2 16 only: the memory guards (" + Nav.guard + ")");
        // hunting prey: no memory guard (the hunt keeps its prey in view)
        world(201, Direction.SOUTH, false);
        Baby.hunting = true;
        Baby.memGuard(Baby.seen);
        check(Nav.guard == null, "guard memory: hunting prey: no memory guard");
        Baby.hunting = false;
        // memGuard records this turn's view too
        world(202, Direction.SOUTH, true, enemy(86, 10, 6));
        Baby.memGuard(Baby.seen);
        check(entry(86) >= 0 && Baby.memRound[entry(86)] == 202, "guard memory: memGuard records the view");

        // Nav.step with the memory guard: a step east strafes, keeping the remembered tile in the cone
        world(200, Direction.NORTH, true, enemy(81, 10, 11));
        Baby.remember(Baby.seen);
        world(201, Direction.SOUTH, false);
        Baby.memGuard(Baby.seen);
        boolean stepped = Nav.step(Direction.EAST);
        check(stepped && me.equals(p(11, 8)) && sensable(p(10, 11)) && moveCd == 18,
              "guard memory: a step east strafes facing the remembered tile (" + me + ", " + facing + ", cd " + moveCd + ")");

        // the whole turn: nothing in view, the enemy remembered behind us: the turn ends facing its tile
        world(200, Direction.NORTH, true, enemy(81, 10, 11));
        Baby.remember(Baby.seen);
        world(201, Direction.SOUTH, false);
        Baby.turn();
        check(sensable(p(10, 11)), "guard memory: Baby.turn ends facing the remembered tile (" + me + ", " + facing + ")");
        // with nothing remembered the same turn leaves Nav.guard unset
        world(201, Direction.SOUTH, true);
        Baby.turn();
        check(Nav.guard == null, "guard memory: Baby.turn with nothing remembered: no guard");
        // in cooperation the memory is not used (the guard is war-only)
        world(200, Direction.NORTH, true, enemy(81, 10, 11));
        Baby.remember(Baby.seen);
        world(201, Direction.SOUTH, false);
        G.coop = true;
        Baby.turn();
        check(Nav.guard == null, "guard memory: in cooperation no memory guard");

        robots.clear(); Baby.seen = new RobotInfo[0]; Nav.guard = null;
        for (int i = 0; i < Baby.MEM_N; i++) Baby.memLoc[i] = null;
        System.out.println("GuardMemTest: " + (BotTest.checks - BotTest.fails) + "/" + BotTest.checks + " passed");
        System.exit(BotTest.fails == 0 ? 0 : 1);
    }
}
