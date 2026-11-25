package ratbot;

import battlecode.common.*;

import static ratbot.BotTest.*;

/**
 * sw84 = yl54 + KILL_BITE (11-24): a baby at war bites an adjacent enemy baby that one bite kills now (Baby.biteKills:
 * health at most 10, or a FINISH_K finishing bite paid from a FINISH_FLOOR bank) instead of grabbing it. Three sites:
 * Baby.grabOrKill before tryCarry (ahead of and after the move), and GrabMicro.execute when the micro's plan grabs
 * such a rat. Evidence: replica 11-24 (yl54, 33 games, r1-400): next to an enemy of 10 health or less in our cone with
 * the action ready we grabbed it 67% of the time and bit it under 3% (the field 38% / 24%); 27% of those carries ended
 * with our carrier grabbed and 13% of the rats were alive 12 rounds on.
 * We stand at (5,5) facing NORTH (dy +1) with 100 health; the cone holds (4,6), (5,6), (6,6).
 */
public class KillBiteTest {
    static RobotInfo rat(int id, Team t, int x, int y, int hp, RobotInfo carrying) {
        return new RobotInfo(id, t, UnitType.BABY_RAT, hp, new MapLocation(x, y), Direction.SOUTH, 0, 0, carrying);
    }

    static RobotInfo enemy(int id, int x, int y, int hp) { return rat(id, Team.B, x, y, hp, null); }

    static void world() {
        reset(); install();
        round = 300; G.startTurn(); G.coop = false;
        Baby.killBites = 0; Baby.bitesRat = 0;
    }

    /** world() with every tile known open (findEnemies' wall traces read Sym.mem), no kings known, at war. */
    static void open() {
        world();
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, false, false, false);
        Comms.numKings = 0; Baby.role = Baby.GATHER; Baby.hunter = null; Baby.rescueT = null; Nav.guard = null;
    }

    static RobotInfo[] put(RobotInfo... rs) {
        for (RobotInfo r : rs) robots.put(r.location, r);
        return rs;
    }

    public static void main(String[] args) throws Exception {
        check(Cfg.KILL_BITE, "kill bite: KILL_BITE on (sw84)");
        check(Cfg.FINISH_K == 5 && Cfg.FINISH_FLOOR == 100, "kill bite: FINISH_K 5, FINISH_FLOOR 100 as on yl54 ("
              + Cfg.FINISH_K + ", " + Cfg.FINISH_FLOOR + ")");

        // biteKills: a plain bite kills at 10 or less; 11-15 with a paid finishing bite from a 100 bank
        check(Baby.biteKills(10, 0) && Baby.biteKills(1, 0), "kill bite: 1 and 10 health die to a plain bite, no bank needed");
        check(Baby.biteKills(11, 1000) && Baby.biteKills(15, 100), "kill bite: 11 (bank 1000) and 15 (bank 100) die to a paid bite");
        check(!Baby.biteKills(16, 1000), "kill bite: 16 health is past FINISH_K");
        check(!Baby.biteKills(11, 99), "kill bite: 11 health with a 99 bank (under FINISH_FLOOR): no paid kill");

        // a 10-health enemy in front: bitten, true, counted
        world();
        RobotInfo[] rs = put(enemy(61, 5, 6, 10));
        boolean b = Baby.killBite(rs);
        check(b && bitten.equals(java.util.List.of(new MapLocation(5, 6))) && Baby.killBites == 1 && Baby.bitesRat == 1,
              "kill bite: a 10-health enemy in front is bitten (" + bitten + ", kb " + Baby.killBites + ")");
        // 12 health, bank 1000: the paid kill
        world();
        rs = put(enemy(62, 5, 6, 12));
        check(Baby.killBite(rs) && bitten.size() == 1, "kill bite: a 12-health enemy with a 1000 bank: paid kill (" + bitten + ")");
        // 16 health: no kill, no bite
        world();
        rs = put(enemy(63, 5, 6, 16));
        check(!Baby.killBite(rs) && bitten.isEmpty() && Baby.killBites == 0, "kill bite: 16 health is not killed");
        // 12 health, bank 50: no kill
        world(); global = 50;
        rs = put(enemy(64, 5, 6, 12));
        check(!Baby.killBite(rs) && bitten.isEmpty(), "kill bite: 12 health with a 50 bank is not killed");
        // behind us, turn ready: turn to face it and bite
        world();
        rs = put(enemy(65, 5, 4, 10));
        check(Baby.killBite(rs) && bitten.equals(java.util.List.of(new MapLocation(5, 4))) && facing == Direction.SOUTH && G.facing == Direction.SOUTH,
              "kill bite: a 10-health enemy behind us: we turn south and bite (" + bitten + " " + facing + ")");
        // behind us, turn not ready: nothing
        world(); turnCd = 10;
        rs = put(enemy(66, 5, 4, 10));
        check(!Baby.killBite(rs) && bitten.isEmpty() && facing == Direction.NORTH, "kill bite: behind us with the turn cooling: no bite, no turn");
        // action not ready
        world(); actCd = 10;
        rs = put(enemy(67, 5, 6, 10));
        check(!Baby.killBite(rs) && bitten.isEmpty(), "kill bite: action cooling: no bite");
        // two in the cone: the lowest health
        world();
        rs = put(enemy(68, 4, 6, 8), enemy(69, 6, 6, 5));
        check(Baby.killBite(rs) && bitten.equals(java.util.List.of(new MapLocation(6, 6))), "kill bite: two killable in the cone: the 5-health one (" + bitten + ")");
        // one in the cone beats a weaker one behind us (no turn)
        world();
        rs = put(enemy(70, 5, 6, 8), enemy(71, 5, 4, 3));
        check(Baby.killBite(rs) && bitten.equals(java.util.List.of(new MapLocation(5, 6))) && facing == Direction.NORTH,
              "kill bite: the one in the cone first, no turn (" + bitten + " " + facing + ")");
        // a killable carrier of one of ours behind us beats a weaker one in front
        world();
        RobotInfo ours = rat(90, Team.A, 5, 4, 100, null);
        rs = put(rat(72, Team.B, 5, 4, 10, ours), enemy(73, 5, 6, 5));
        check(Baby.killBite(rs) && bitten.equals(java.util.List.of(new MapLocation(5, 4))) && facing == Direction.SOUTH,
              "kill bite: the carrier of ours first, turning to it (" + bitten + " " + facing + ")");
        // allies, kings and enemies two tiles away are not kill-bite targets
        world();
        RobotInfo king = new RobotInfo(74, Team.B, UnitType.RAT_KING, 5, new MapLocation(6, 7), Direction.NORTH, 0, 0, null);
        rs = put(rat(75, Team.A, 5, 6, 5, null), enemy(76, 5, 7, 5));
        RobotInfo[] all = {rs[0], rs[1], king};
        check(!Baby.killBite(all) && bitten.isEmpty(), "kill bite: an ally, a king at 5 health and an enemy at dist2 4 are left to the other code");

        // grabOrKill: the kill comes before tryCarry's grab of the healthiest grabbable rat
        world();
        rs = put(enemy(77, 5, 6, 10), enemy(78, 4, 6, 50));
        Baby.grabOrKill(rs);
        check(bitten.equals(java.util.List.of(new MapLocation(5, 6))) && carrying == null,
              "kill bite: grabOrKill bites the 10-health rat instead of grabbing the 50-health one (" + bitten + " " + carrying + ")");
        // grabOrKill with no killable rat: the grab as before
        world();
        rs = put(enemy(79, 4, 6, 50));
        Baby.grabOrKill(rs);
        check(bitten.isEmpty() && carrying != null && carrying.ID == 79, "kill bite: grabOrKill grabs the 50-health rat as yl54 did");

        // GrabMicro.execute: a grab plan on a rat a bite kills bites it instead (stay, current facing)
        check(G.DIRS[Direction.NORTH.ordinal()] == Direction.NORTH, "kill bite: G.DIRS is in Direction order");
        world();
        RobotInfo weak = enemy(80, 5, 6, 10);
        put(weak);
        GrabMicro.er[0] = weak; GrabMicro.grabI = 0; GrabMicro.biteI = -1; GrabMicro.bestD = 8; GrabMicro.bestF = Direction.NORTH.ordinal();
        GrabMicro.execute();
        check(bitten.equals(java.util.List.of(new MapLocation(5, 6))) && carrying == null && Baby.killBites == 1,
              "kill bite: the micro's grab of a 10-health rat becomes a bite (" + bitten + " " + carrying + ")");
        world();
        RobotInfo mid = enemy(81, 5, 6, 50);
        put(mid);
        GrabMicro.er[0] = mid; GrabMicro.grabI = 0; GrabMicro.biteI = -1; GrabMicro.bestD = 8; GrabMicro.bestF = Direction.NORTH.ordinal();
        GrabMicro.execute();
        check(bitten.isEmpty() && carrying != null && carrying.ID == 81 && Baby.killBites == 0, "kill bite: the micro still grabs a 50-health rat");
        // the micro's kill is stamped for afterKill
        world(); Baby.killRound = -1; Baby.killedId = -1;
        weak = enemy(82, 5, 6, 10);
        put(weak);
        GrabMicro.er[0] = weak; GrabMicro.grabI = 0; GrabMicro.biteI = -1; GrabMicro.bestD = 8; GrabMicro.bestF = Direction.NORTH.ordinal();
        GrabMicro.execute();
        check(Baby.killedId == 82 && Baby.killRound == G.round, "kill bite: the micro's kill stamps killedId and killRound ("
              + Baby.killedId + ", " + Baby.killRound + ")");

        // killBite and grabOrKill stamp and report the kill
        world(); Baby.killRound = -1; Baby.killedId = -1;
        rs = put(enemy(83, 5, 6, 10));
        check(Baby.grabOrKill(rs) && Baby.killedId == 83 && Baby.killRound == G.round, "kill bite: grabOrKill reports the kill and stamps it");
        world();
        rs = put(enemy(84, 4, 6, 50));
        check(!Baby.grabOrKill(rs) && carrying != null, "kill bite: grabOrKill reports a grab as no kill");

        // afterKill: the turn's robots without the dead rat; at war the enemy picture is found again from them
        open();
        RobotInfo dead = enemy(85, 5, 6, 8), other = enemy(86, 7, 7, 100), ally = rat(87, Team.A, 4, 4, 100, null);
        RobotInfo[] turn = put(dead, other, ally);
        Baby.seen = turn; Baby.nearestEnemy = dead; Baby.hunting = true; Nav.guard = dead.location;
        RobotInfo[] left = Baby.afterKill(turn, 85);
        check(left.length == 2 && Baby.seen == left && java.util.Arrays.stream(left).noneMatch(r -> r.ID == 85),
              "kill bite: afterKill drops the dead rat from the turn's robots and seen (" + left.length + ")");
        check(Baby.nearestEnemy != null && Baby.nearestEnemy.ID == 86 && !Baby.hunting,
              "kill bite: afterKill finds the next enemy (86, healthier: no hunt) (" + Baby.nearestEnemy + " " + Baby.hunting + ")");
        check(new MapLocation(7, 7).equals(Nav.guard), "kill bite: afterKill guards the next enemy within GUARD_DIST2 (" + Nav.guard + ")");
        // a second weak rat becomes the prey
        open();
        dead = enemy(88, 5, 6, 8);
        RobotInfo prey2 = enemy(89, 6, 7, 30);
        turn = put(dead, prey2, other);
        Baby.seen = turn; Baby.nearestEnemy = dead; Baby.hunting = true;
        Baby.afterKill(turn, 88);
        check(Baby.nearestEnemy != null && Baby.nearestEnemy.ID == 89 && Baby.hunting,
              "kill bite: afterKill hunts the next weaker rat (" + Baby.nearestEnemy + " " + Baby.hunting + ")");
        // no enemy left: no target, no guard
        open();
        dead = enemy(90, 5, 6, 8);
        turn = put(dead, ally);
        Baby.seen = turn; Baby.nearestEnemy = dead; Baby.hunting = true; Nav.guard = dead.location;
        Baby.afterKill(turn, 90);
        check(Baby.nearestEnemy == null && !Baby.hunting && Nav.guard == null, "kill bite: afterKill with no enemy left: no target, no guard");
        // under KILL_REFRESH bytecodes the picture only forgets the dead rat
        open();
        dead = enemy(91, 5, 6, 8);
        turn = put(dead, other);
        Baby.seen = turn; Baby.nearestEnemy = dead; Baby.hunting = true; Baby.hunter = null;
        clock(Cfg.KILL_REFRESH - 1);
        Baby.afterKill(turn, 91);
        clock(1 << 30);
        check(Baby.nearestEnemy == null && !Baby.hunting && Baby.seen.length == 1,
              "kill bite: under KILL_REFRESH bytecodes afterKill only forgets the dead rat (" + Baby.nearestEnemy + ")");
        // in cooperation only seen changes (no enemy picture is kept there)
        open(); G.coop = true;
        dead = enemy(92, 5, 6, 8);
        turn = put(dead, other);
        Baby.seen = turn; Baby.nearestEnemy = null;
        left = Baby.afterKill(turn, 92);
        check(left.length == 1 && Baby.seen == left && Baby.nearestEnemy == null, "kill bite: in cooperation afterKill only drops the rat from seen");
        G.coop = false;
        // a list without that rat comes back as it was
        open();
        turn = put(other);
        Baby.seen = turn;
        check(Baby.afterKill(turn, 999) == turn && Baby.seen == turn, "kill bite: afterKill of a list without the rat returns it unchanged");
        check(Cfg.KILL_REFRESH == 9000, "kill bite: KILL_REFRESH 9000 (" + Cfg.KILL_REFRESH + ")");

        G.coop = true;
        System.out.println("KillBiteTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
