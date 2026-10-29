package ratbot;

import battlecode.common.*;
import java.util.*;

/** Pure-logic tests for the bot against a small fake RobotController. Plain main; exit code 1 on failure. */
public class BotTest {
    static int fails = 0, checks = 0;

    static void check(boolean ok, String what) {
        checks++;
        if (!ok) { fails++; System.out.println("FAIL " + what); }
    }

    // ------------------------------------------------------------------ fake world
    static int W = 30, H = 20;
    static Set<MapLocation> walls = new HashSet<>(), dirt = new HashSet<>();
    static Map<MapLocation, Integer> cheeseAt = new HashMap<>();
    static boolean canPromote, promoted;
    static Map<MapLocation, RobotInfo> robots = new HashMap<>();
    static int[] shared = new int[64];
    static MapLocation me = new MapLocation(5, 5);
    static Direction facing = Direction.NORTH;
    static int health = 100;
    static int round = 1, global = 1000, raw = 0, moveCd = 0, turnCd = 0, actCd = 0;
    static UnitType type = UnitType.BABY_RAT;
    static int ratCost = 10;
    static List<MapLocation> built = new ArrayList<>();
    static List<Integer> squeaks = new ArrayList<>();
    static int digsDone;
    static List<MapLocation> bitten = new ArrayList<>();
    static Direction thrown;

    static boolean onMap(MapLocation l) { return l.x >= 0 && l.y >= 0 && l.x < W && l.y < H; }

    static boolean passable(MapLocation l) { return onMap(l) && !walls.contains(l) && !dirt.contains(l); }

    static boolean sensable(MapLocation l) {
        if (!onMap(l)) return false;
        if (l.equals(me)) return true;
        int vr = type == UnitType.RAT_KING ? 25 : 20;
        if (me.distanceSquaredTo(l) > vr) return false;
        if (type == UnitType.RAT_KING) return true;
        double fx = facing.dx, fy = facing.dy, vx = l.x - me.x, vy = l.y - me.y;
        double cos = (fx * vx + fy * vy) / Math.sqrt((fx * fx + fy * fy) * (vx * vx + vy * vy));
        return Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, cos)))) <= 45.001;
    }

    static void reset() {
        canPromote = promoted = false;
        walls.clear(); dirt.clear(); cheeseAt.clear(); robots.clear(); Arrays.fill(shared, 0); squeaks.clear(); bitten.clear(); thrown = null;
        me = new MapLocation(5, 5); facing = Direction.NORTH; health = 100; round = 1; global = 1000; raw = 0;
        moveCd = turnCd = actCd = 0; type = UnitType.BABY_RAT; digsDone = 0; ratCost = 10; built.clear();
        Sym.mask = Sym.ROT | Sym.HOR | Sym.VER; Sym.contradictions = 0; Sym.listDone = 0;
        Nav.target = null; Nav.bugging = false; Nav.avoid = 0; Nav.stall = 0; Nav.digs = 0; Nav.kingMarkN = 0;
        Flow.building = false; Flow.tgt = null; Flow.builtRound = -1; Flow.g = null; Flow.bucket = null; Flow.failTgt = null; Flow.lastTgt = null;
    }

    static RobotController fakeRc() {
        return (RobotController) java.lang.reflect.Proxy.newProxyInstance(RobotController.class.getClassLoader(),
            new Class<?>[]{RobotController.class}, (proxy, m, a) -> {
                switch (m.getName()) {
                    case "getRoundNum": return round;
                    case "getMapWidth": return W;
                    case "getMapHeight": return H;
                    case "getID": return 10001;
                    case "getTeam": return Team.A;
                    case "getType": return type;
                    case "getLocation": return me;
                    case "getDirection": return facing;
                    case "getCarrying": return null;
                    case "isCooperation": return true;
                    case "getGlobalCheese": return global;
                    case "getRawCheese": return raw;
                    case "getAllCheese": return global + raw;
                    case "onTheMap": return onMap((MapLocation) a[0]);
                    case "canSenseLocation": return sensable((MapLocation) a[0]);
                    case "readSharedArray": return shared[(Integer) a[0]];
                    case "writeSharedArray": {
                        if (type != UnitType.RAT_KING) throw new GameActionException(GameActionExceptionType.CANT_DO_THAT, "baby write");
                        int v = (Integer) a[1];
                        if (v < 0 || v > 1023) throw new GameActionException(GameActionExceptionType.CANT_DO_THAT, "range");
                        shared[(Integer) a[0]] = v; return null;
                    }
                    case "isMovementReady": return moveCd < 10;
                    case "isTurningReady": return turnCd < 10;
                    case "isActionReady": return actCd < 10;
                    case "canTurn": return turnCd < 10 && (a == null || (a[0] != null && a[0] != Direction.CENTER));
                    case "turn": facing = (Direction) a[0]; turnCd += 10; return null;
                    case "canMove": {
                        Direction d = (Direction) a[0];
                        MapLocation t = me.add(d);
                        return moveCd < 10 && passable(t) && !robots.containsKey(t);
                    }
                    case "move": {
                        Direction d = (Direction) a[0];
                        MapLocation t = me.add(d);
                        if (moveCd >= 10 || !passable(t) || robots.containsKey(t)) throw new GameActionException(GameActionExceptionType.CANT_MOVE_THERE, "x");
                        moveCd += d == facing ? 10 : 18;
                        me = t; return null;
                    }
                    case "canRemoveDirt": {
                        MapLocation l = (MapLocation) a[0];
                        return actCd < 10 && sensable(l) && me.distanceSquaredTo(l) <= 2 && dirt.contains(l) && global + raw >= 5;
                    }
                    case "removeDirt": dirt.remove((MapLocation) a[0]); actCd += 25; global -= 5; digsDone++; return null;
                    case "senseNearbyMapInfos": {
                        List<MapInfo> out = new ArrayList<>();
                        int vr = type == UnitType.RAT_KING ? 25 : 20;
                        for (int x = me.x - 5; x <= me.x + 5; x++)
                            for (int y = me.y - 5; y <= me.y + 5; y++) {
                                MapLocation l = new MapLocation(x, y);
                                if (!sensable(l) || me.distanceSquaredTo(l) > vr) continue;
                                out.add(new MapInfo(l, passable(l), null, walls.contains(l), dirt.contains(l), 0, TrapType.NONE, false));
                            }
                        return out.toArray(new MapInfo[0]);
                    }
                    case "senseMapInfo": {
                        MapLocation l = (MapLocation) a[0];
                        return new MapInfo(l, passable(l), null, walls.contains(l), dirt.contains(l), cheeseAt.getOrDefault(l, 0), TrapType.NONE, false);
                    }
                    case "senseNearbyRobots": return robots.values().toArray(new RobotInfo[0]);
                    case "canAttack": {
                        MapLocation l = (MapLocation) a[0];
                        RobotInfo r = robots.get(l);
                        int d = me.distanceSquaredTo(l);
                        return actCd < 10 && sensable(l) && d >= 1 && d <= 2 && r != null && r.team != Team.A;
                    }
                    case "isLocationOccupied": return robots.containsKey((MapLocation) a[0]);
                    case "senseRobotAtLocation": return robots.get((MapLocation) a[0]);
                    case "canThrowRat": { MapLocation t = me.add(facing); return actCd < 10 && passable(t) && !robots.containsKey(t); }
                    case "throwRat": thrown = facing; actCd += 20; return null;
                    case "attack": bitten.add((MapLocation) a[0]); actCd += 10; return null;
                    case "squeak": squeaks.add((Integer) a[0]); return true;
                    case "readSqueaks": return new Message[0];
                    case "getCurrentRatCost": return ratCost;
                    case "canBuildRat": { MapLocation t = (MapLocation) a[0]; return actCd < 10 && passable(t) && !robots.containsKey(t) && global >= ratCost; }
                    case "buildRat": built.add((MapLocation) a[0]); global -= ratCost; actCd += 10; return null;
                    case "getHealth": return health;
                    case "canBecomeRatKing": return canPromote && actCd < 10;
                    case "becomeRatKing": promoted = true; type = UnitType.RAT_KING; return null;
                    case "setIndicatorString": return null;
                    default: throw new UnsupportedOperationException("fakeRc: " + m.getName());
                }
            });
    }

    static void install() {
        G.init(fakeRc());
        Sym.init();
        Flow.unlimited = true;
        G.startTurn();
    }

    /** One simulated turn of movement: cooldowns tick, then Nav steps toward t. */
    static void tick(MapLocation t) throws GameActionException {
        round++;
        moveCd = Math.max(0, moveCd - 10); turnCd = Math.max(0, turnCd - 10); actCd = Math.max(0, actCd - 10);
        G.startTurn();
        MapInfo[] infos = G.rc.senseNearbyMapInfos();
        for (MapInfo mi : infos) Sym.record(mi.getMapLocation().x, mi.getMapLocation().y, mi.isWall(), mi.isDirt(), false);
        Nav.goTo(t);
        Flow.work(0);
    }

    // ------------------------------------------------------------------ tests

    static void testSymMineList() {
        reset(); W = 20; H = 20; install();
        // listed mine m = (3,4); twins: ROT (16,15), HOR (3,15), VER (16,4)
        MapLocation[] ms = { new MapLocation(3, 4) };
        Sym.record(16, 15, false, false, false);
        Sym.listCheck(ms, 1, false);
        check(Sym.mask == (Sym.HOR | Sym.VER) && !Sym.known(3 + 15 * W) && (Sym.mem[3 + 15 * W] & Sym.EXP) == Sym.HOR << 4,
              "sym list: a mineless ROT twin rules out ROT; the unseen HOR twin is marked, not seen");
        Sym.record(16, 4, false, false, false);
        check(Sym.mask == Sym.HOR && Sym.contradictions == 0, "sym list: a marked twin first seen mineless rules out its symmetry");
        reset(); install();
        Sym.listCheck(ms, 1, false);
        Sym.record(16, 15, false, false, true);
        check(Sym.mask == 7 && Sym.listDone == 1, "sym list: a twin that holds a mine keeps every symmetry");
        reset(); install();
        Sym.listCheck(ms, 1, false);
        Sym.record(3, 15, false, true, false);   // its own mirrors are all unseen: only the HOR mark can fire
        check(Sym.mask == (Sym.ROT | Sym.VER) && (Sym.mem[3 + 15 * W] & (Sym.SEEN | Sym.DIRT | Sym.EXP)) == (Sym.SEEN | Sym.DIRT | Sym.HOR << 4),
              "sym list: record keeps the marks, and the HOR mark fires on the first sighting");
        Sym.record(3, 15, false, false, false);
        check(Sym.mask == (Sym.ROT | Sym.VER) && Sym.contradictions == 0, "sym list: a second sighting changes nothing");
        // spacing (kings): mirror_ROT(3,4) = (16,15) lies at d2 4 from the listed (16,13)
        reset(); install();
        MapLocation[] ms2 = { new MapLocation(3, 4), new MapLocation(16, 13) };
        Sym.listCheck(ms2, 2, false);
        check(Sym.mask == 7, "sym list: no spacing test for babies");
        reset(); install();
        Sym.listCheck(ms2, 2, true);
        check(Sym.mask == (Sym.HOR | Sym.VER) && Sym.listDone == 2, "sym list: a twin closer than the mine spacing rules out its symmetry");
        Sym.mask = Sym.HOR;
        MapLocation[] ms3 = { new MapLocation(3, 4), new MapLocation(16, 13), new MapLocation(8, 8) };
        Sym.listCheck(ms3, 3, true);
        check(Sym.mask == Sym.HOR && Sym.listDone == 3 && Sym.contradictions == 0, "sym list: a unique mask skips the rest");
    }

    static void testSymRelay() {
        reset(); W = 20; H = 20; install();
        Comms.numKings = 1; Comms.kings[0] = new MapLocation(9, 6);
        check(Baby.kingInEarshot(new MapLocation(5, 5)) && new MapLocation(5, 5).distanceSquaredTo(Comms.kings[0]) > 16
              && !Baby.kingInEarshot(new MapLocation(3, 5)) && Baby.kingInEarshot(new MapLocation(9, 11)),
              "sym relay: a king is in earshot when any of its 9 tiles is within d2 16");
        MapLocation c = new MapLocation(10, 10);
        check(Baby.catInEarshot(c, new MapLocation(6, 10)) && !Baby.catInEarshot(c, new MapLocation(5, 10))
              && Baby.catInEarshot(c, new MapLocation(15, 11)) && !Baby.catInEarshot(c, new MapLocation(16, 11)),
              "sym relay: a cat hears from any of its 4 tiles");
        G.us = Team.A; G.coop = true; G.round = 50; G.me = new MapLocation(5, 5);
        Baby.cat = null; Baby.nearestEnemy = null; Baby.symSqueaked = -100;
        RobotInfo ally = new RobotInfo(2, Team.A, UnitType.BABY_RAT, 100, new MapLocation(6, 7), Direction.NORTH, 0, 0, null);
        Baby.seen = new RobotInfo[] { ally };
        shared[Comms.SYM] = 7; Sym.mask = Sym.HOR;
        check(!Baby.symGossip(false, false) && squeaks.isEmpty(), "sym relay: off by switch");
        check(!Baby.symGossip(true, true) && squeaks.isEmpty(), "sym relay: within a king's earshot the king squeak handles it");
        check(Baby.symGossip(true, false) && squeaks.size() == 1 && Comms.sqType(squeaks.get(0)) == Comms.SQ_SYM
              && (Comms.sqExtra(squeaks.get(0)) & 7) == Sym.HOR, "sym relay: a sharper mask is squeaked to a nearby ally");
        G.round = 52;
        check(!Baby.symGossip(true, false), "sym relay: at most once per SYM_GOSSIP_EVERY rounds");
        G.round = 50 + Cfg.SYM_GOSSIP_EVERY;
        Baby.cat = new RobotInfo(3, Team.NEUTRAL, UnitType.CAT, 1000, new MapLocation(6, 8), Direction.NORTH, 0, 0, null);
        check(!Baby.symGossip(true, false), "sym relay: never with a cat in earshot");
        Baby.cat = null;
        Baby.seen = new RobotInfo[0];
        check(!Baby.symGossip(true, false), "sym relay: never with no ally to hear");
        Baby.seen = new RobotInfo[] { ally };
        shared[Comms.SYM] = Sym.HOR;
        check(!Baby.symGossip(true, false), "sym relay: nothing new against the shared mask");
        shared[Comms.SYM] = 7;
        check(Baby.symGossip(true, false) && squeaks.size() == 2, "sym relay: squeaks again after the wait");
        Baby.seen = new RobotInfo[0]; Comms.numKings = 0; squeaks.clear();
    }

    static void testHandMirror() {
        reset(); W = 31; H = 24; install();
        Random r = new Random(7);
        int bad = 0;
        for (int i = 0; i < 3000; i++) {
            MapLocation p = new MapLocation(r.nextInt(W), r.nextInt(H));
            Direction d = G.DIRS[r.nextInt(8)];
            int h = G.hand(p, d);
            // rotation: position and direction both negate -> same handedness
            MapLocation pr = G.mirror(p, Sym.ROT);
            int hr = G.hand(pr, d.opposite());
            // horizontal reflection (y flips): handedness flips
            MapLocation ph = G.mirror(p, Sym.HOR);
            Direction dh = Direction.values()[0];
            for (Direction q : G.DIRS) if (q.dx == d.dx && q.dy == -d.dy) dh = q;
            int hh = G.hand(ph, dh);
            MapLocation pv = G.mirror(p, Sym.VER);
            Direction dv = dh;
            for (Direction q : G.DIRS) if (q.dx == -d.dx && q.dy == d.dy) dv = q;
            int hv = G.hand(pv, dv);
            int px = 2 * p.x - G.cx2, py = 2 * p.y - G.cy2;
            boolean cross0 = px * d.dy - py * d.dx == 0;
            if (hr != h) bad++;
            if (!cross0 && (hh != -h || hv != -h)) bad++;
        }
        check(bad == 0, "hand(): mirrored robots choose mirrored sides (bad=" + bad + ")");
    }

    static void testSymElimination() {
        reset(); W = 20; H = 20; install();
        // wall at (2,3); its mirrors: ROT (17,16), HOR (2,16), VER (17,3)
        Sym.record(2, 3, true, false, false);
        Sym.record(17, 16, false, false, false);  // ROT mirror is open -> ROT eliminated
        check((Sym.mask & Sym.ROT) == 0, "sym: open ROT mirror of a wall eliminates ROT");
        check((Sym.mask & (Sym.HOR | Sym.VER)) == (Sym.HOR | Sym.VER), "sym: HOR/VER still live");
        Sym.record(2, 16, true, false, false);    // HOR mirror is a wall -> consistent
        check((Sym.mask & Sym.HOR) != 0, "sym: matching HOR mirror keeps HOR");
        Sym.record(17, 3, false, false, false);   // VER mirror open -> VER eliminated
        check(Sym.mask == Sym.HOR, "sym: unique HOR after eliminations, got " + Sym.mask);
        Sym.eliminate(Sym.HOR);
        check(Sym.mask == Sym.HOR && Sym.contradictions == 1, "sym: never empties; contradiction counted");
        // dirt does not eliminate
        reset(); install();
        Sym.record(4, 4, false, true, false);
        Sym.record(15, 15, false, false, false);
        check(Sym.mask == 7, "sym: dirt differences never eliminate");
        // mines eliminate like walls
        reset(); install();
        Sym.record(4, 4, false, false, true);
        Sym.record(15, 15, false, false, false);
        check((Sym.mask & Sym.ROT) == 0, "sym: mine without mirrored mine eliminates");
    }

    static void testSqueakCodec() {
        int m = Comms.squeak(Comms.SQ_MINE, 59, 42, 7);
        check(Comms.sqType(m) == Comms.SQ_MINE && Comms.sqX(m) == 59 && Comms.sqY(m) == 42 && Comms.sqExtra(m) == 7, "squeak roundtrip");
        int c = Comms.squeak(Comms.SQ_CAT, 0, 0, 0);
        check(Comms.sqType(c) == Comms.SQ_CAT, "squeak type in top bits");
    }

    static void testFresh() {
        reset(); install();
        round = 1030; G.startTurn();
        check(Comms.fresh(1030 & 1023, 2), "fresh: same round");
        check(Comms.fresh((1028) & 1023, 2), "fresh: 2 old");
        check(!Comms.fresh((1027) & 1023, 2), "fresh: 3 old is stale");
        round = 1024; G.startTurn();
        check(Comms.fresh(1023, 2), "fresh: wraps at 1024");
        check(Comms.stamp() != 0, "stamp never 0");
    }

    static void testCatGeometry() {
        MapLocation bl = new MapLocation(10, 10);
        check(Baby.inCatBody(bl, new MapLocation(11, 11)) && !Baby.inCatBody(bl, new MapLocation(12, 11)), "cat body 2x2 from bottom-left");
        check(Baby.chebToBody(bl, new MapLocation(12, 12)) == 1 && Baby.chebToBody(bl, new MapLocation(13, 10)) == 2, "cheb to cat body");
        check(Baby.catDist4(bl, new MapLocation(10, 10)) == 2, "catDist4 corner tile = 4*(0.5^2+0.5^2)");
        reset(); install();
        me = new MapLocation(12, 10); G.startTurn();
        RobotInfo cat = new RobotInfo(3, Team.NEUTRAL, UnitType.CAT, 4000, bl, Direction.EAST, 0, 0, null);
        int mask = Baby.laneMask(cat);
        // stepping NORTH from (12,10) -> (12,11): in the cat's next footprint (11..12, 10..11)
        check((mask & (1 << Direction.NORTH.ordinal())) != 0, "lane: tile ahead of an east-facing cat is avoided");
        check((mask & (1 << Direction.SOUTHEAST.ordinal())) == 0, "lane: (13,9) is outside the lane");
    }

    static void testNavAroundWall() throws GameActionException {
        reset(); W = 30; H = 20; install();
        // vertical wall x=10, y=2..17, open at the ends
        for (int y = 2; y <= 17; y++) walls.add(new MapLocation(10, y));
        me = new MapLocation(5, 10); facing = Direction.EAST;
        MapLocation t = new MapLocation(15, 10);
        int steps = 0;
        while (!me.equals(t) && steps < 120) { tick(t); steps++; }
        check(me.equals(t), "nav: walks around a wall (steps=" + steps + ", at " + me + ")");
        check(steps < 60, "nav: wall detour bounded (steps=" + steps + ")");
    }

    static void testNavDigsDirt() throws GameActionException {
        reset(); W = 30; H = 20; install();
        // full-height dirt band at x=10..11: only digging gets through
        for (int y = 0; y < H; y++) { dirt.add(new MapLocation(10, y)); dirt.add(new MapLocation(11, y)); }
        me = new MapLocation(5, 10); facing = Direction.EAST;
        MapLocation t = new MapLocation(15, 10);
        int steps = 0;
        while (!me.equals(t) && steps < 80) { tick(t); steps++; }
        check(me.equals(t), "nav: digs through a dirt band (steps=" + steps + ", at " + me + ", digs=" + digsDone + ")");
        check(digsDone <= 3, "nav: digs only the band (" + digsDone + ")");
    }

    static void testNavUShape() throws GameActionException {
        reset(); W = 30; H = 30; install();
        // U-shaped trap opening west; target east
        for (int y = 8; y <= 20; y++) walls.add(new MapLocation(16, y));
        for (int x = 8; x <= 16; x++) { walls.add(new MapLocation(x, 8)); walls.add(new MapLocation(x, 20)); }
        me = new MapLocation(12, 14); facing = Direction.EAST;
        MapLocation t = new MapLocation(24, 14);
        int steps = 0;
        while (!me.equals(t) && steps < 200) { tick(t); steps++; }
        check(me.equals(t), "nav: escapes a U trap (steps=" + steps + ", at " + me + ")");
    }

    static void testNavFlowSealedRoom() throws GameActionException {
        reset(); W = 30; H = 30; install();
        // wall across y=12 except a dirt plug at x=0..1 (the only way out): bug-walking alone circles forever
        for (int x = 2; x < W; x++) walls.add(new MapLocation(x, 12));
        dirt.add(new MapLocation(0, 12)); dirt.add(new MapLocation(1, 12));
        me = new MapLocation(15, 5); facing = Direction.SOUTH;
        MapLocation t = new MapLocation(15, 20);
        int steps = 0;
        while (!me.equals(t) && steps < 250) { tick(t); steps++; }
        check(me.equals(t), "flow: leaves a room sealed by a dirt plug (steps=" + steps + ", at " + me + ", digs=" + digsDone + ")");
        check(digsDone <= 2, "flow: digs only the plug (" + digsDone + ")");
        check(steps < 120, "flow: bounded detour (steps=" + steps + ")");
    }

    static void testKingBlock() {
        reset(); W = 20; H = 12; install();
        me = new MapLocation(5, 5); G.startTurn();
        Comms.kings[0] = new MapLocation(10, 5); Comms.numKings = 1;
        Nav.kingMarks = new MapLocation[Comms.KING_SLOTS]; Nav.kingMarkN = 0;
        Nav.markKings();
        int n = 0;
        for (int i = 0; i < W * H; i++) if ((Sym.mem[i] & Sym.KING) != 0) n++;
        check(n == 9 && (Sym.mem[G.idx(new MapLocation(11, 6))] & Sym.KING) != 0, "king block: the king's 3x3 is marked (" + n + ")");
        Sym.record(11, 6, false, false, false);
        check((Sym.mem[G.idx(new MapLocation(11, 6))] & Sym.KING) != 0 && Nav.terrainBlocked(new MapLocation(11, 6)),
              "king block: re-sensing a body tile keeps the mark; bug walks treat it as terrain");
        check(Nav.atKing(new MapLocation(10, 5)) && Nav.atKing(new MapLocation(9, 6)) && !Nav.atKing(new MapLocation(12, 5)),
              "king block: atKing covers the body only");
        // a path past the king goes around its body when the search blocks king tiles
        MapLocation t = new MapLocation(15, 5);
        Flow.request(t);
        Flow.block = Sym.WALL | Sym.KING;
        Flow.work(0);
        boolean crosses = false;
        for (int i = 0; i < Flow.plen; i++) if ((Sym.mem[Flow.path[i]] & Sym.KING) != 0) crosses = true;
        check(Flow.ready(t) && !crosses, "king block: the path goes around the king's body");
        // the king moves two tiles east: its old column is cleared, the new one marked
        Comms.kings[0] = new MapLocation(12, 5);
        Nav.markKings();
        n = 0;
        for (int i = 0; i < W * H; i++) if ((Sym.mem[i] & Sym.KING) != 0) n++;
        check(n == 9 && (Sym.mem[G.idx(new MapLocation(9, 5))] & Sym.KING) == 0 && (Sym.mem[G.idx(new MapLocation(13, 4))] & Sym.KING) != 0,
              "king block: a moved king is re-marked (" + n + ")");
        Comms.numKings = 0;
        Nav.markKings();
        n = 0;
        for (int i = 0; i < W * H; i++) if ((Sym.mem[i] & Sym.KING) != 0) n++;
        check(n == 0, "king block: marks clear when the king is gone (" + n + ")");
    }

    /** Walk toward a target that steps between three tiles every two turns, behind a wall; true if we got beside it. */
    static boolean roamingTarget(int sameR) throws GameActionException {
        reset(); W = 30; H = 20; install();
        for (int y = 3; y <= 17; y++) walls.add(new MapLocation(10, y));
        me = new MapLocation(12, 10); facing = Direction.WEST;
        Nav.sameR = sameR;
        MapLocation[] ts = { new MapLocation(7, 10), new MapLocation(7, 11), new MapLocation(6, 10) };
        boolean got = false;
        for (int i = 0; i < 90 && !got; i++) {
            MapLocation t = ts[(i / 2) % 3];
            tick(t);
            got = G.cheb(me, t) <= 1;
        }
        Nav.sameR = Cfg.NAV_SAME_R;
        return got;
    }

    static void testNavSameTarget() throws GameActionException {
        reset(); W = 30; H = 20; install();
        me = new MapLocation(5, 10);
        Nav.sameR = 2;
        Nav.target = new MapLocation(15, 10); Nav.stall = 7; Nav.bestDist = 0;
        Nav.goTo(new MapLocation(15, 12));
        check(Nav.stall >= 7, "nav same target: a target 2 tiles off keeps the stall (" + Nav.stall + ")");
        Nav.goTo(new MapLocation(15, 15));
        check(Nav.stall <= 1, "nav same target: a target 3 tiles off resets it (" + Nav.stall + ")");
        Nav.sameR = 0;
        Nav.target = new MapLocation(15, 10); Nav.stall = 7; Nav.bestDist = 0;
        Nav.goTo(new MapLocation(15, 11));
        check(Nav.stall <= 1, "nav same target: with NAV_SAME_R 0 any other tile resets it (" + Nav.stall + ")");
        Nav.sameR = Cfg.NAV_SAME_R;
        check(roamingTarget(2), "nav same target: reaches a roaming target behind a wall");
        check(!roamingTarget(0), "nav same target: without it the roaming target behind a wall is never reached");
    }

    static void testAtMine() {
        reset(); W = 30; H = 30; install();
        MapLocation m = new MapLocation(15, 15);
        Comms.numKings = 0;
        me = new MapLocation(12, 15); G.startTurn();
        check(!Baby.atMine(m, 4), "at mine: 3 tiles off with no king near the mine is not there");
        me = new MapLocation(13, 16); G.startTurn();
        check(Baby.atMine(m, 0) && Baby.atMine(m, 4), "at mine: within 2 always");
        Comms.kings[0] = new MapLocation(14, 16); Comms.numKings = 1;
        me = new MapLocation(11, 15); G.startTurn();
        check(Baby.atMine(m, 4) && !Baby.atMine(m, 0) && !Baby.atMine(m, 3 - 1), "at mine: a king on the mine widens it to kingR");
        me = new MapLocation(10, 15); G.startTurn();
        check(!Baby.atMine(m, 4), "at mine: not beyond kingR");
        Comms.kings[0] = new MapLocation(18, 15);
        me = new MapLocation(11, 15); G.startTurn();
        check(!Baby.atMine(m, 4), "at mine: a king 3 from the mine does not widen it");
        Comms.numKings = 0;
    }

    static void testCheeseBan() {
        reset(); W = 20; H = 20; install();
        Arrays.fill(Baby.banned, null); Baby.banHead = 0;
        MapLocation a = new MapLocation(18, 21 - 10);
        round = 100; G.startTurn();
        Baby.banCheese(a);
        check(Baby.bannedCheese(a, 60) && Baby.bannedCheese(new MapLocation(16, 13), 60) && !Baby.bannedCheese(new MapLocation(15, 11), 60),
              "cheese ban: the pile and its Chebyshev-2 neighbourhood, nothing farther");
        round = 159; G.startTurn();
        check(Baby.bannedCheese(a, 60), "cheese ban: still banned on its last round");
        round = 160; G.startTurn();
        check(!Baby.bannedCheese(a, 60), "cheese ban: expires after ttl rounds");
        for (int i = 0; i < 4; i++) Baby.banCheese(new MapLocation(2 + 5 * i, 2));
        check(!Baby.bannedCheese(a, 1000) && Baby.bannedCheese(new MapLocation(17, 2), 60) && Baby.bannedCheese(new MapLocation(2, 2), 60),
              "cheese ban: a ring of four, the oldest overwritten");
        Arrays.fill(Baby.banned, null); Baby.banHead = 0;
        // detour: a pile 3 tiles east through a wall whose path runs around it, against one in the open
        reset(); W = 30; H = 20; install();
        for (int y = 3; y <= 17; y++) walls.add(new MapLocation(10, y));
        for (int y = 0; y < H; y++) for (int x = 0; x < W; x++) Sym.record(x, y, walls.contains(new MapLocation(x, y)), false, false);
        me = new MapLocation(8, 10); G.startTurn();
        MapLocation far = new MapLocation(11, 10), near = new MapLocation(8, 13);
        Flow.request(far); Flow.work(0);
        check(Flow.ready(far) && Flow.detour(far), "cheese ban: a path around the wall is a detour (" + Flow.plen + " tiles)");
        Flow.request(near); Flow.work(0);
        check(Flow.ready(near) && !Flow.detour(near) && !Flow.detour(far), "cheese ban: a straight path is not; only the last path counts");
    }

    static void testFlowJam() {
        // a path straight east with an ally parked on its next tile: sidestep lands diagonally beside a later path
        // tile, never on the ally; the ROT-mirrored scene picks the mirrored side
        Direction[] got = new Direction[2];
        for (int m = 0; m < 2; m++) {
            reset(); W = 30; H = 20; install();
            MapLocation a = new MapLocation(5, 10), t = new MapLocation(15, 10);
            if (m == 1) { a = G.mirror(a, Sym.ROT); t = G.mirror(t, Sym.ROT); }
            me = a; G.startTurn();
            // a straight path, laid by hand (A* ties zigzag)
            Flow.path = new int[1024];
            int sx = Integer.signum(t.x - a.x);
            for (int i = 0; i <= 10; i++) Flow.path[i] = G.idx(new MapLocation(a.x + sx * i, a.y));
            Flow.plen = 11; Flow.ppos = 0;
            MapLocation block = new MapLocation(Flow.path[1] % W, Flow.path[1] / W);
            robots.put(block, new RobotInfo(2, Team.A, UnitType.BABY_RAT, 100, block, Direction.NORTH, 0, 0, null));
            Direction d = Flow.sidestep(1);
            got[m] = d;
            MapLocation l = d == null ? null : me.add(d);
            check(l != null && !l.equals(block) && Math.abs(l.x - Flow.path[2] % W) <= 1 && Math.abs(l.y - Flow.path[2] / W) <= 1,
                  "flow jam: sidestep lands beside the next free path tile (" + d + ")");
            // boxed in on every side: no sidestep
            for (Direction e : G.DIRS) { MapLocation q = me.add(e); robots.put(q, new RobotInfo(3, Team.A, UnitType.BABY_RAT, 100, q, Direction.NORTH, 0, 0, null)); }
            check(Flow.sidestep(1) == null, "flow jam: no sidestep when boxed in");
        }
        check(got[0] != null && got[1] != null && got[1] == got[0].opposite(), "flow jam: mirrored rats sidestep to mirrored sides (" + got[0] + ", " + got[1] + ")");
    }

    static void testFlowField() {
        reset(); W = 12; H = 8; install();
        // dirt column x=5 beside a wall column x=6 with a gap at y=0: the path digs once and uses the gap
        for (int y = 0; y < H; y++) { Sym.record(5, y, false, true, false); if (y > 0) Sym.record(6, y, true, false, false); }
        me = new MapLocation(2, 4); G.startTurn();
        MapLocation t = new MapLocation(10, 4);
        Flow.request(t);
        Flow.work(0);
        check(Flow.ready(t), "flow: search completes with budget");
        int digs = 0, gap = 0;
        for (int i = 0; i < Flow.plen; i++) {
            int x = Flow.path[i] % W, y = Flow.path[i] / W;
            if (x == 5) digs++;
            if (x == 6) gap += y == 0 ? 1 : 100;
        }
        check(Flow.path[0] == G.idx(me) && Flow.path[Flow.plen - 1] == G.idx(t), "flow: path runs from me to the target");
        check(digs == 1 && gap == 1, "flow: one dirt tile, through the gap (digs=" + digs + " gap=" + gap + ")");
        for (int i = 1; i < Flow.plen; i++) {
            int a = Flow.path[i - 1], b = Flow.path[i];
            if (Math.abs(a % W - b % W) > 1 || Math.abs(a / W - b / W) > 1) { check(false, "flow: path steps are adjacent"); break; }
        }
        // a sealed target fails cleanly
        reset(); W = 12; H = 8; install();
        for (int y = 0; y < H; y++) Sym.record(6, y, true, false, false);
        me = new MapLocation(2, 4); G.startTurn();
        int f0 = Flow.fails;
        Flow.request(t);
        Flow.work(0);
        check(!Flow.ready(t) && Flow.fails == f0 + 1 && !Flow.building, "flow: walled-off target fails");
        Flow.request(t);
        check(!Flow.building, "flow: a failed target is not retried at once");
        // a search that cannot finish in time is abandoned, so the rat stops holding
        reset(); W = 12; H = 8; install();
        me = new MapLocation(2, 4); G.startTurn();
        Flow.request(t);
        check(Flow.searching(t), "flow: searching while the build runs");
        round += Cfg.FLOW_MAX_TURNS + 1; G.startTurn();
        check(!Flow.searching(t) && !Flow.building, "flow: slow search times out");
    }

    static void testExploreUnseen() throws GameActionException {
        reset(); W = 20; H = 20; install();
        // everything seen except an 8x8 block (16%): 12 samples land in it ~88% of the time
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++)
            if (x < 6 || x > 13 || y < 6 || y > 13) Sym.record(x, y, false, false, false);
        int in = 0;
        for (int i = 0; i < 40; i++) {
            Baby.exploreTarget = null;
            Baby.explore();
            MapLocation e = Baby.exploreTarget;
            if (e.x >= 6 && e.x <= 13 && e.y >= 6 && e.y <= 13) in++;
        }
        check(in >= 28, "explore: prefers unseen tiles (" + in + "/40)");
    }

    static void testKingNeverWritesOutOfRange() throws GameActionException {
        reset(); install();
        type = UnitType.RAT_KING; me = new MapLocation(59 % W, 19); G.startTurn();
        King.slot = 0;
        King.publish();
        boolean ok = true;
        for (int v : shared) if (v < 0 || v > 1023) ok = false;
        check(ok, "king publish stays in 0..1023");
    }

    static void testTransferReach() {
        MapLocation k = new MapLocation(10, 10);
        check(Baby.inTransferReach(new MapLocation(14, 11), k), "reach: (4,1) from centre -> nearest tile dist2 9");
        check(Baby.inTransferReach(new MapLocation(13, 13), k), "reach: (3,3) -> 8");
        check(!Baby.inTransferReach(new MapLocation(14, 12), k), "reach: (4,2) -> 10 out of range");
        check(!Baby.inTransferReach(new MapLocation(11, 11), k), "reach: a king tile itself is not a standing spot");
    }

    static RobotInfo enemyRat(int id, int x, int y, int hp) {
        return new RobotInfo(id, Team.B, UnitType.BABY_RAT, hp, new MapLocation(x, y), Direction.NORTH, 0, 0, null);
    }

    static void testBiteTargets() throws GameActionException {
        reset(); install();
        // two enemies adjacent: the weaker one behind us (outside the cone) is chosen and we turn to bite it
        RobotInfo strong = enemyRat(1, 5, 6, 90), weak = enemyRat(2, 5, 4, 30);
        robots.put(strong.location, strong); robots.put(weak.location, weak);
        boolean b = Baby.biteEnemies(robots.values().toArray(new RobotInfo[0]));
        check(b && bitten.size() == 1 && bitten.get(0).equals(weak.location) && facing == Direction.SOUTH,
              "bite: lowest-health adjacent enemy, turning to face it (" + bitten + " " + facing + ")");
        // nothing adjacent: no bite, no turn
        reset(); install();
        RobotInfo far = enemyRat(3, 5, 8, 10);
        robots.put(far.location, far);
        check(!Baby.biteEnemies(robots.values().toArray(new RobotInfo[0])) && bitten.isEmpty() && facing == Direction.NORTH,
              "bite: enemy 3 tiles away is not bitten");
        // an enemy king (3x3) whose body is adjacent diagonally: bite its nearest tile
        reset(); install();
        RobotInfo k = new RobotInfo(4, Team.B, UnitType.RAT_KING, 600, new MapLocation(7, 7), Direction.NORTH, 0, 0, null);
        robots.put(new MapLocation(6, 6), k);
        b = Baby.biteEnemies(new RobotInfo[]{k});
        check(b && bitten.size() == 1 && bitten.get(0).equals(new MapLocation(6, 6)), "bite: king's nearest body tile (" + bitten + ")");
    }

    static void testThrowDirection() throws GameActionException {
        reset(); install();
        // a wall two tiles east, an enemy two tiles west, open north/south: the wall throw (42) beats nothing (10)
        // but the enemy (42, doubled for hurting them) beats the wall; a rat three tiles away may move first
        walls.add(new MapLocation(7, 5));
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) {
            MapLocation l = new MapLocation(x, y);
            Sym.record(x, y, walls.contains(l), false, false);
        }
        check(Baby.tryThrow(new RobotInfo[0]) && thrown == Direction.EAST, "throw: toward the wall behind a free tile (" + thrown + ")");
        reset(); install();
        walls.add(new MapLocation(7, 5));
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, walls.contains(new MapLocation(x, y)), false, false);
        RobotInfo e = enemyRat(9, 3, 5, 100);
        robots.put(e.location, e);
        facing = Direction.WEST; // so the enemy is sensable
        check(Baby.tryThrow(new RobotInfo[]{e}) && thrown == Direction.WEST, "throw: into an adjacent-flight enemy beats a wall (" + thrown + ")");
        reset(); install();
        walls.add(new MapLocation(7, 5));
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, walls.contains(new MapLocation(x, y)), false, false);
        e = enemyRat(9, 2, 5, 100);
        robots.put(e.location, e);
        facing = Direction.WEST;
        check(Baby.tryThrow(new RobotInfo[]{e}) && thrown == Direction.EAST, "throw: a sure wall beats a rat three tiles off (" + thrown + ")");
        // an enemy king's body: its centre three tiles south puts its edge on the point-blank tile
        reset(); install();
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, false, false, false);
        RobotInfo k = new RobotInfo(70, G.them, UnitType.RAT_KING, 600, new MapLocation(5, 2), Direction.NORTH, 0, 0, null);
        robots.put(k.location, k);
        check(Baby.tryThrow(new RobotInfo[]{k}) && thrown == Direction.SOUTH, "throw: into an enemy king's edge tile (" + thrown + ")");
        // an ally on the flight path is never the target
        reset(); install();
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, false, false, false);
        RobotInfo a = new RobotInfo(71, G.us, UnitType.BABY_RAT, 100, new MapLocation(5, 3), Direction.NORTH, 0, 0, null);
        RobotInfo e2 = enemyRat(72, 7, 5, 100);
        robots.put(a.location, a); robots.put(e2.location, e2);
        check(Baby.tryThrow(new RobotInfo[]{a, e2}) && thrown == Direction.EAST, "throw: past no ally, into the enemy (" + thrown + ")");
        // nothing point-blank: hold the rat for a while
        reset(); install();
        G.round = 50; Baby.carryRound = 49;
        check(!Baby.tryThrow(new RobotInfo[0]) && thrown == null, "throw: holds when no throw reaches 42");
        G.round = 49 + Cfg.THROW_HOLD;
        check(Baby.tryThrow(new RobotInfo[0]) && thrown != null, "throw: throws anyway when the hold runs out");
        Baby.carryRound = -100;
    }

    static void testMicroRetreatsWhenOutnumbered() throws GameActionException {
        reset(); install();
        // three healthy enemies two tiles north, we are alone: micro must not step toward them
        for (int i = 0; i < 3; i++) { RobotInfo e = enemyRat(20 + i, 4 + i, 7, 100); robots.put(e.location, e); }
        check(Micro.act(robots.values().toArray(new RobotInfo[0])), "micro: takes the decision with enemies near");
        check(me.y <= 5, "micro: outnumbered rat does not advance (at " + me + ")");
        // one weak enemy adjacent-diagonal reachable, we are healthy: micro closes / holds contact
        reset(); install();
        RobotInfo e = enemyRat(30, 6, 7, 20);
        robots.put(e.location, e);
        Micro.act(robots.values().toArray(new RobotInfo[0]));
        check(me.distanceSquaredTo(e.location) <= 2, "micro: winning fight moves into bite range (at " + me + ")");
        // outnumbered three tiles off: of the steps that keep the distance, the one onto cheese wins
        reset(); install();
        for (int i = 0; i < 3; i++) { RobotInfo f = enemyRat(40 + i, 4 + i, 9, 100); robots.put(f.location, f); }
        cheeseAt.put(new MapLocation(4, 6), 20);
        Micro.act(robots.values().toArray(new RobotInfo[0]));
        check(me.equals(new MapLocation(4, 6)), "micro: a safe step onto visible cheese (at " + me + ")");
    }

    static void testWarRoles() throws GameActionException {
        reset(); install();
        RobotInfo strong = enemyRat(40, 5, 7, 100), weak = enemyRat(41, 7, 8, 40), far = enemyRat(42, 5, 12, 10);
        RobotInfo[] rs = {strong, weak, far};
        check(Baby.prey(rs, 100) == weak, "prey: nearest weaker enemy within dist2 20");
        check(Baby.prey(rs, 40) == null, "prey: none when no enemy is strictly weaker in range");
        // a gatherer ignores an enemy two tiles away, but turns to face an adjacent one
        Baby.role = Baby.GATHER;
        Baby.nearestEnemy = strong;
        if (Cfg.WAR_GATHER > 0) check(!Baby.war(), "war: gatherer keeps gathering past a non-adjacent enemy");
        else check(Baby.war(), "war: with WAR_GATHER 0 a gatherer engages an enemy two tiles away");
        RobotInfo adj = enemyRat(43, me.x + 1, me.y, 100);
        Baby.nearestEnemy = adj;
        check(Baby.war(), "war: gatherer holds against an adjacent enemy");
        check(facing == me.directionTo(adj.location), "war: gatherer faces the adjacent enemy");
    }

    static void testRetreat() throws GameActionException {
        reset(); install();
        Baby.nearestEnemy = enemyRat(50, 5, 7, 100);
        check(!Baby.retreat(), "retreat: a healthy rat does not retreat");
        health = 30;
        Comms.kings[0] = new MapLocation(5, 0); Comms.numKings = 1;
        check(Baby.retreat(), "retreat: a wounded rat near an enemy falls back");
        check(me.y < 5, "retreat: it moved toward its king (at " + me + ")");
    }

    static void testFort() throws GameActionException {
        reset(); install();
        Baby.role = Baby.FIGHT; Baby.cat = null;
        Comms.kings[0] = new MapLocation(5, 15); Comms.numKings = 1;
        check(Baby.fort(), "fort: a fighter far from the king takes the decision");
        check(me.y > 5, "fort: it heads back toward the king (at " + me + ")");
        check(!Baby.nearHome(new MapLocation(5, 5), 4) && Baby.nearHome(new MapLocation(7, 12), 4), "fort: nearHome radius");
    }

    static void testStandoff() throws GameActionException {
        reset(); install();
        G.coop = false; Baby.role = Baby.FIGHT; Baby.hunting = false; Baby.eKing = null;
        // an enemy three tiles north facing us (south)
        RobotInfo e = new RobotInfo(64, G.them, UnitType.BABY_RAT, 100, new MapLocation(5, 8), Direction.SOUTH, 0, 0, null);
        robots.put(e.location, e);
        Baby.nearestEnemy = e;
        MapLocation start = me;
        check(Baby.war(), "standoff: war takes the turn");
        if (Cfg.STANDOFF) check(me.equals(start) && facing == Direction.NORTH, "standoff: holds and faces a watching enemy (" + me + ")");
        else check(!me.equals(start), "standoff off: closes on the enemy");
        // an enemy looking away is approached
        reset(); install(); G.coop = false; Baby.role = Baby.FIGHT; Baby.hunting = false; Baby.eKing = null;
        e = new RobotInfo(64, G.them, UnitType.BABY_RAT, 100, new MapLocation(5, 8), Direction.NORTH, 0, 0, null);
        robots.put(e.location, e);
        Baby.nearestEnemy = e;
        check(Baby.war() && !me.equals(new MapLocation(5, 5)), "standoff: closes on an enemy looking away");
    }

    static void testSpawnFloor() throws GameActionException {
        reset(); install();
        type = UnitType.RAT_KING; G.coop = false; G.round = 300; global = Cfg.SPAWN_FLOOR - 100;
        ratCost = 40; // 12+ babies: the spawn floor holds
        King.spawn(new RobotInfo[0], new RobotInfo[0]);
        check(built.isEmpty(), "spawn: a king with a dozen babies keeps its floor");
        ratCost = 20; global = Cfg.LOW_FLOOR + 100; // 4+ babies: too few to earn, the floor drops
        King.spawn(new RobotInfo[0], new RobotInfo[0]);
        check(built.size() == 1, "spawn: a king with few babies spends its reserve on rats (" + built + ")");
    }

    static void testFlank() throws GameActionException {
        reset(); install();
        G.coop = false; Nav.avoid = 0;
        // an enemy at (5,7) facing north sees (4..6, 8) but not the row below it
        RobotInfo e = new RobotInfo(62, G.them, UnitType.BABY_RAT, 100, new MapLocation(5, 7), Direction.NORTH, 0, 0, null);
        check(Baby.sees(e, new MapLocation(5, 8)) && !Baby.sees(e, new MapLocation(5, 6)) && !Baby.sees(e, new MapLocation(6, 7)),
              "flank: cone of a north-facing rat");
        RobotInfo ne = new RobotInfo(63, G.them, UnitType.BABY_RAT, 100, new MapLocation(5, 7), Direction.NORTHEAST, 0, 0, null);
        check(Baby.sees(ne, new MapLocation(6, 7)) && Baby.sees(ne, new MapLocation(5, 8)) && !Baby.sees(ne, new MapLocation(4, 7)),
              "flank: cone of a diagonal-facing rat");
        robots.put(e.location, e);
        check(Baby.flank(new RobotInfo[]{e}), "flank: a blind tile next to the enemy is taken");
        check(G.cheb(me, e.location) == 1 && !Baby.sees(e, me) && facing == me.directionTo(e.location),
              "flank: we end behind it, facing it (" + me + " " + facing + ")");
        e = new RobotInfo(62, G.them, UnitType.BABY_RAT, 100, new MapLocation(5, 7), Direction.SOUTH, 0, 0, null);
        reset(); install(); G.coop = false; robots.put(e.location, e);
        check(!Baby.flank(new RobotInfo[]{e}), "flank: none when every reachable neighbour is in its view");
    }

    static void testBiteCheese() {
        check(Baby.finishCheese(10, 1000) == 0 && Baby.finishCheese(13, 1000) == 9 && Baby.finishCheese(16, 1000) == 0,
              "bite cheese: a baby pays k*k only when it makes the kill");
        check(Baby.finishCheese(13, 100) == 0, "bite cheese: none below the floor");
        check(King.biteCheese(18, 1000) == 64, "bite cheese: the king pays the least that kills");
        check(King.biteCheese(100, 5000) == 0 && King.biteCheese(18, 350) == 0 && King.biteCheese(21, 5000) == 0,
              "bite cheese: nothing for a bite that does not kill or is over budget");
    }

    static void testRaid() throws GameActionException {
        reset(); W = 20; H = 20; install();
        Sym.mask = Sym.ROT;
        Baby.home = new MapLocation(3, 3);
        Comms.numMines = 0;
        Comms.mines[Comms.numMines++] = new MapLocation(4, 6);    // our half: its ROT mirror (15, 13) is theirs
        MapLocation t = Baby.pickRaid();
        check(t.equals(new MapLocation(15, 13)), "raid: mirror of our mine on the enemy half (" + t + ")");
        Comms.mines[Comms.numMines++] = new MapLocation(12, 17);  // enemy half
        Comms.mines[Comms.numMines++] = new MapLocation(7, 2);    // mirror (12, 17) duplicates
        int far = 0;
        for (int i = 0; i < 20; i++) {
            MapLocation c = Baby.pickRaid();
            if (c.distanceSquaredTo(new MapLocation(16, 16)) >= c.distanceSquaredTo(Baby.home)) far++;
        }
        check(far == 0, "raid: never picks a spot on our half");
        Comms.numMines = 0;
        check(Baby.pickRaid().equals(new MapLocation(9, 9)), "raid: no mines -> map centre");
        check(Baby.homeHalf(new MapLocation(5, 5)) && !Baby.homeHalf(new MapLocation(14, 14)), "raid: home half");
    }

    static void testKingMine() {
        reset(); W = 60; H = 60; install();
        Sym.mask = Sym.ROT;
        Comms.numMines = 0;
        Comms.mines[Comms.numMines++] = new MapLocation(30, 31);  // nearer the enemy start (56, 56)
        Comms.mines[Comms.numMines++] = new MapLocation(3, 25);   // ours but beyond dist2 400
        check(Cfg.KING_MINE_R2 < 484 && King.pickHomeMine(new MapLocation(3, 3)) == null, "king mine: none on our half in reach");
        Comms.mines[Comms.numMines++] = new MapLocation(7, 6);
        Comms.mines[Comms.numMines++] = new MapLocation(4, 7);
        check(new MapLocation(4, 7).equals(King.pickHomeMine(new MapLocation(3, 3))), "king mine: the nearest one on our half");
        Comms.numKings = 1; Comms.kings[0] = new MapLocation(4, 8);
        check(new MapLocation(7, 6).equals(King.pickHomeMine(new MapLocation(3, 3))), "king mine: skips a mine another king holds");
        Comms.numKings = 0;
        // VER map: a mine on the x axis is as near the enemy start (56, 3) as ours
        Sym.mask = Sym.VER;
        Comms.numMines = 0;
        Comms.mines[Comms.numMines++] = new MapLocation(29, 10);   // axis x = 29.5: d2 from (3,3) 725, from (56,3) 778
        Comms.mines[Comms.numMines++] = new MapLocation(15, 3);    // d2 144 vs 1681: ours
        W = 31; install(); Sym.mask = Sym.VER;                       // axis x = 15: (15, 3) ties
        check((King.pickHomeMine(new MapLocation(3, 3)) != null) == Cfg.MINE_TIES, "king mine: an axis tie counts as ours only with MINE_TIES");
        Baby.home = new MapLocation(3, 3);
        check(Baby.homeHalf(new MapLocation(15, 9)) == Cfg.MINE_TIES && Baby.homeHalf(new MapLocation(14, 9))
              && !Baby.homeHalf(new MapLocation(16, 9)), "home half: an axis tie is ours only with MINE_TIES");
        Comms.numMines = 0;
    }

    static void testReportPriority() {
        reset(); install();
        G.coop = false;
        Comms.numKings = 1; Comms.kings[0] = new MapLocation(7, 8);   // dist2 13 from (5, 5)
        Comms.numMines = 0; Baby.myMineCount = 0;
        Baby.noteMine(12, 3);
        Baby.nearestEnemy = enemyRat(80, 5, 7, 100);
        Baby.report();
        check(squeaks.size() == 1 && Comms.sqType(squeaks.get(0)) == Comms.SQ_MINE, "report: a king in hearing range learns the mine even at war");
        squeaks.clear();
        Comms.kings[0] = new MapLocation(15, 15);
        Baby.report();
        check(squeaks.size() == 1 && Comms.sqType(squeaks.get(0)) == Comms.SQ_MINE, "report: while the team knows no mine, relay ours even far from kings");
        squeaks.clear();
        Comms.numMines = 1;
        Comms.mines[0] = new MapLocation(30, 31);
        if (Cfg.GOSSIP_ALWAYS) {
            Baby.report();
            check(squeaks.size() == 1 && Comms.sqType(squeaks.get(0)) == Comms.SQ_MINE && Comms.sqX(squeaks.get(0)) == 12,
                  "report: gossip passes on a mine the king's list lacks, away from kings");
            squeaks.clear();
            Comms.mines[0] = new MapLocation(12, 3);
        }
        Baby.report();
        check(squeaks.size() == 1 && Comms.sqType(squeaks.get(0)) == Comms.SQ_TRAP, "report: away from kings, share the enemy front");
        Comms.numMines = 0;
        Comms.numKings = 0; Baby.myMineCount = 0; Baby.nearestEnemy = null; G.coop = true;
    }

    static void testFormation() throws GameActionException {
        reset(); W = 30; H = 30; install();
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, false, false, false);
        Sym.mask = Sym.ROT;
        King.start = new MapLocation(3, 3);
        Comms.numMines = 0;
        Comms.mines[Comms.numMines++] = new MapLocation(4, 4);    // too close to the king
        Comms.mines[Comms.numMines++] = new MapLocation(20, 22);  // enemy half
        Comms.mines[Comms.numMines++] = new MapLocation(9, 6);
        check(new MapLocation(9, 6).equals(King.pickFormSite(new MapLocation(3, 3))), "form: nearest clear home mine away from the king");
        Comms.numMines = 0;
        check(new MapLocation(6, 6).equals(King.pickFormSite(new MapLocation(10, 10))), "form: no mine -> four tiles behind the king");
        // a baby on the site promotes when it can; one beside the free centre steps onto it
        G.round = 100;
        shared[Comms.FORM_X] = 6; shared[Comms.FORM_Y] = 6; shared[Comms.FORM_T] = Comms.stamp();
        check(new MapLocation(5, 5).equals(Baby.formSite()), "form: site read from the shared array");
        canPromote = true;
        check(Baby.formKing() && promoted, "form: the centre rat becomes a king");
        reset(); W = 30; H = 30; install();
        G.round = 100;
        shared[Comms.FORM_X] = 6; shared[Comms.FORM_Y] = 7; shared[Comms.FORM_T] = Comms.stamp();
        check(Baby.formKing() && me.equals(new MapLocation(5, 6)), "form: steps onto the free centre (at " + me + ")");
        shared[Comms.FORM_T] = 0;
        check(!Baby.formKing(), "form: no site, no formation");
    }

    static void testMineSpread() {
        reset(); W = 30; H = 30; install();
        Sym.mask = Sym.HOR; Baby.home = new MapLocation(1, 5); Baby.fu = -1; Baby.fv = -1;
        Comms.numMines = 0; Baby.myMineCount = 0;
        int[][] ms = {{6, 8}, {11, 2}, {14, 9}, {20, 2}, {6, 21}, {11, 27}, {14, 20}, {20, 27}, {26, 12}};
        for (int[] m : ms) Comms.mines[Comms.numMines++] = new MapLocation(m[0], m[1]);
        java.util.Set<MapLocation> got = new java.util.HashSet<>();
        G.seed(12345);
        for (int i = 0; i < 200; i++) got.add(Baby.pickMine());
        java.util.Set<MapLocation> want = new java.util.HashSet<>(java.util.Arrays.asList(new MapLocation(6, 8),
            new MapLocation(11, 2), new MapLocation(14, 9), new MapLocation(20, 2)));
        if (!Cfg.MINE_BALANCE) check(got.equals(want), "mine spread: the 4 nearest home-half mines, never the enemy half " + got);
        Comms.numMines = 0;
        Comms.mines[Comms.numMines++] = new MapLocation(6, 21);
        check(new MapLocation(6, 21).equals(Baby.pickMine()), "mine spread: only enemy-half mines known -> take one");
        Comms.numMines = 0;
        check(Baby.pickMine() == null, "mine spread: no mine known");
        if (Cfg.MINE_BALANCE) {
            for (int[] m : ms) Comms.mines[Comms.numMines++] = new MapLocation(m[0], m[1]);
            java.util.Map<MapLocation, Integer> per = new java.util.HashMap<>();
            for (int id = 10001; id < 10001 + 500; id++) {
                Baby.slot = (id * 0x9E3779B1) >>> 8;
                per.merge(Baby.pickMine(), 1, Integer::sum);
            }
            int lo = java.util.Collections.min(per.values()), hi = java.util.Collections.max(per.values());
            check(per.size() == 5 && !per.containsKey(new MapLocation(6, 21)) && lo >= 70 && hi <= 130,
                  "mine balance: every home-half mine, evenly " + per);
            check(Baby.minesAtPick == Comms.numMines, "mine balance: remembers the mine count it picked at");
        }
    }

    static void testScout() throws GameActionException {
        reset(); W = 30; H = 30; install();
        Sym.mask = Sym.HOR; Baby.home = new MapLocation(1, 5); Baby.fu = -1; Baby.fv = -1;
        // the right half is unseen, on both sides of the mirror line y = 14.5
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) if (x < 15) Sym.mem[x + W * y] = Sym.SEEN;
        G.seed(777);
        Baby.exploreTarget = null;
        boolean went = Baby.exploreHome();
        MapLocation t = Baby.exploreTarget;
        check(went && t != null && t.x >= 15 && t.y <= 14, "scout: explores an unseen tile on our half (" + t + ")");
        // with nothing unseen on our half it says so (the scout then explores like anyone else)
        for (int x = 15; x < W; x++) for (int y = 0; y < 15; y++) Sym.mem[x + W * y] = Sym.SEEN;
        Baby.exploreTarget = null;
        check(!Baby.exploreHome() && Baby.exploreTarget == null, "scout: no target once our half is seen");
    }

    static void testMineMirror() {
        reset(); W = 30; H = 30; install();
        MapLocation home = new MapLocation(3, 3);
        Comms.numMines = 0;
        Comms.addMine(25, 20);   // enemy half under ROT: twin (4, 9) is ours
        Comms.addMine(6, 4);     // our half: its twin (23, 25) is theirs, not listed
        King.mirrorInto(home, Sym.ROT);
        check(Comms.numMines == 3 && Comms.hasMine(4, 9) && !Comms.hasMine(23, 25), "mine mirror: lists our-half twins only");
        King.mirrorInto(home, Sym.ROT);
        check(Comms.numMines == 3, "mine mirror: no duplicates");
        Comms.numMines = 0; Comms.addMine(25, 20);
        King.mirrorInto(home, Sym.VER);   // x -> 29 - x: twin (4, 20)
        check(Comms.hasMine(4, 20), "mine mirror: follows the symmetry");
        Comms.numMines = 0;
    }

    static void testSeek() throws GameActionException {
        reset(); W = 30; H = 30; install();
        Sym.mask = Sym.HOR; Baby.home = new MapLocation(1, 5); Baby.fu = -1; Baby.fv = -1;
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) if (x < 15) Sym.mem[x + W * y] = Sym.SEEN;
        G.seed(777); G.us = Team.A; G.me = new MapLocation(6, 6);
        MapLocation mine = new MapLocation(8, 6);
        RobotInfo[] crowd = new RobotInfo[12];
        for (int i = 0; i < crowd.length; i++)
            crowd[i] = new RobotInfo(100 + i, Team.A, UnitType.BABY_RAT, 100, new MapLocation(7 + i % 3, 5 + i / 3 % 3), Direction.NORTH, 0, 0, null);
        Baby.seen = crowd;
        Baby.seekNext = 0; Baby.seekUntil = 0; Baby.myMineCount = 0;
        boolean started = Baby.startSeek(mine);
        check(Cfg.SEEK_CROWD == 0 ? !started : started == (Baby.nearerAllies(mine) >= Cfg.SEEK_CROWD), "seek: starts only at a crowded mine, when on");
        // a seeker heads for an unseen tile of our half; it stops once nothing there is unseen
        Baby.seekMines = 0; Baby.exploreTarget = null; Baby.seekUntil = round + 100;
        check(Baby.seek() && Baby.exploreTarget != null && Baby.exploreTarget.x >= 15 && Baby.exploreTarget.y <= 14,
              "seek: explores our half's unseen tiles (" + Baby.exploreTarget + ")");
        for (int x = 15; x < W; x++) for (int y = 0; y < 15; y++) Sym.mem[x + W * y] = Sym.SEEN;
        Baby.exploreTarget = null;
        MapLocation keep = Baby.mineTarget = mine;
        check(!Baby.seek() && Baby.seekUntil == 0 && Baby.mineTarget == keep, "seek: over once our half is seen, mine kept");
        // a find (known to the king already) ends seeking and sends the rat to re-pick its mine
        Baby.seekUntil = round + 100; Baby.myMineCount = 0; Baby.noteMine(20, 3);
        Comms.numMines = 1; Comms.mines[0] = new MapLocation(20, 3);
        shared[Comms.MINE0] = 21; shared[Comms.MINE0 + 1] = 4;
        check(!Baby.seek() && Baby.seekUntil == 0 && Baby.mineTarget == null, "seek: a reported find ends it and re-picks");
        Baby.seen = new RobotInfo[0]; Baby.myMineCount = 0; Comms.numMines = 0; Baby.mineTarget = null;
    }

    static void testHostile() {
        int r0 = Baby.role; boolean c0 = G.coop;
        G.coop = false; Baby.role = Baby.GATHER;
        boolean war = Baby.hostile();
        G.coop = true;
        boolean gatherCoop = Baby.hostile();
        Baby.role = Baby.ATTACK;
        boolean attackCoop = Baby.hostile();
        check(war && !gatherCoop && attackCoop == Cfg.RUSH_BREAK, "hostile: after cooperation all; during it only RUSH_BREAK attackers");
        Baby.role = r0; G.coop = c0;
    }

    static void testGlance() {
        check(!Baby.glanceDue(500, 495, 0) && Baby.glanceDue(500, 495, 10) && Baby.glanceDue(500, 490, 10)
              && !Baby.glanceDue(500, 489, 10) && !Baby.glanceDue(500, -1000, 10), "glance: only within GLANCE_MEM rounds of a threat, never when 0");
    }

    static void testCarryCap() {
        check(Baby.takeOk(0, 500, 120) && Baby.takeOk(100, 20, 120) && !Baby.takeOk(100, 40, 120) && Baby.takeOk(400, 400, 0),
              "carry cap: empty-handed takes any pile; loaded only within the cap; 0 = no cap");
        check(Cfg.CARRY_MAX >= 0 && Cfg.HUNGRY >= 0, "carry cap / hungry: switches non-negative");
    }

    static void testBabyCap() {
        check(King.babyCap(false, 0) == Cfg.MAX_BABIES && King.babyCap(true, 0) == Cfg.MAX_BABIES_WAR
              && King.babyCap(false, Cfg.RICH_BANK - 1) == Cfg.MAX_BABIES, "baby cap: the base caps below RICH_BANK");
        check(King.babyCap(false, Cfg.RICH_BANK) == Math.max(Cfg.MAX_BABIES, Cfg.RICH_BABIES)
              && King.babyCap(true, 1 << 20) == Math.max(Cfg.MAX_BABIES_WAR, Cfg.RICH_BABIES), "baby cap: a rich bank raises it, never lowers it");
    }

    static void testNearKing() {
        Comms.numKings = 2; Comms.kings[0] = new MapLocation(5, 5); Comms.kings[1] = new MapLocation(20, 20);
        check(King.nearKing(new MapLocation(9, 1)) && King.nearKing(new MapLocation(16, 24)) && !King.nearKing(new MapLocation(10, 5))
              && !King.nearKing(new MapLocation(12, 12)), "near king: Chebyshev 4 of any live king");
        Comms.numKings = 1;
        check(!King.nearKing(new MapLocation(20, 20)), "near king: only live slots count");
        Comms.numKings = 0;
        check(Cfg.FORM_MAX >= 1 && Cfg.FORM_MAX <= 5, "form max: within the engine's 5");
        check(King.formCap(true) == Cfg.FORM_MAX && King.formCap(false) == Math.max(Cfg.FORM_MAX, Cfg.FORM_MAX_WAR)
              && King.formCap(false) <= 5, "form cap: FORM_MAX in peace, FORM_MAX_WAR at war when higher");
    }

    static void testNearerAllies() {
        G.us = Team.A;
        G.me = new MapLocation(10, 10);
        MapLocation site = new MapLocation(14, 10);
        Baby.seen = new RobotInfo[] {
            new RobotInfo(1, Team.A, UnitType.BABY_RAT, 100, new MapLocation(12, 10), Direction.NORTH, 0, 0, null),
            new RobotInfo(2, Team.A, UnitType.BABY_RAT, 100, new MapLocation(10, 14), Direction.NORTH, 0, 0, null),  // farther
            new RobotInfo(3, Team.B, UnitType.BABY_RAT, 100, new MapLocation(13, 10), Direction.NORTH, 0, 0, null),  // enemy
            new RobotInfo(4, Team.A, UnitType.RAT_KING, 600, new MapLocation(14, 11), Direction.NORTH, 0, 0, null),  // king
            new RobotInfo(5, Team.A, UnitType.BABY_RAT, 100, new MapLocation(15, 11), Direction.NORTH, 0, 0, null),
        };
        check(Baby.nearerAllies(site) == 2, "nearer allies: our babies strictly nearer the site only, got " + Baby.nearerAllies(site));
        Baby.seen = new RobotInfo[0];
    }

    static void testCatAtKing() {
        Comms.numKings = 1; Comms.kings[0] = new MapLocation(7, 43);
        MapLocation near = new MapLocation(4, 43), far = new MapLocation(20, 43);
        check(Cfg.CAT_DEFEND_R2 == 0 || Baby.catAtKing(near), "cat at king: the attack_on_cat cat, 3 tiles off, counts");
        check(!Baby.catAtKing(far), "cat at king: a cat 13 tiles off does not");
        Comms.numKings = 0;
        check(!Baby.catAtKing(near), "cat at king: no live king, no defence");
    }

    static void testFormIncome() {
        Comms.numKings = 1;
        Econ.n = 100; Econ.sum = 0;   // rate100() = sum
        if (Cfg.FORM_NET100 == Integer.MIN_VALUE) {
            check(King.formIncome(), "form income: no test when FORM_NET100 is off");
        } else {
            // two kings' upkeep (400 x100) plus the margin, at INCOME_TRUST percent
            int need = (400 + Cfg.FORM_NET100) * 100 / Cfg.INCOME_TRUST + 1;
            Econ.sum = need;
            check(King.formIncome(), "form income: enough for both kings plus the margin");
            Econ.sum = need * 9 / 10 - 1;
            check(400 + Cfg.FORM_NET100 < 20 || !King.formIncome(), "form income: short of the second king's upkeep");
        }
        Econ.n = 0; Econ.sum = 0; Comms.numKings = 0;
    }

    static void testKingDig() {
        reset(); W = 30; H = 30; install();
        for (int i = 0; i < W * H; i++) Sym.mem[i] = Sym.SEEN;
        MapLocation c = new MapLocation(10, 10), east = new MapLocation(20, 10);
        check(King.digTile(c, east) == null, "king dig: an open step needs no dig");
        // a dirt band at x = 12: the east step adds (12, 9..11), all dirt
        for (int y = 0; y < H; y++) Sym.mem[12 + W * y] |= Sym.DIRT;
        MapLocation d = King.digTile(c, east);
        check(d != null && d.x == 12 && Math.abs(d.y - 10) <= 1 && c.distanceSquaredTo(d) <= 8, "king dig: the leading edge, in range " + d);
        // toward (20, 20) with a wall at (12, 10): north-east and east are out; north adds (9..11, 12), dirt
        for (int x = 0; x < W; x++) Sym.mem[x + W * 12] |= Sym.DIRT;
        Sym.mem[12 + W * 10] |= Sym.WALL;
        MapLocation d2 = King.digTile(c, new MapLocation(20, 20));
        check(d2 != null && d2.y == 12 && d2.x >= 9 && d2.x <= 11 && c.distanceSquaredTo(d2) <= 8, "king dig: the step around a wall " + d2);
        // walls across the east edge: every step toward the target adds a wall
        Sym.mem[12 + W * 9] |= Sym.WALL; Sym.mem[12 + W * 11] |= Sym.WALL;
        check(King.digTile(c, east) == null, "king dig: never through a wall");
        for (int i = 0; i < W * H; i++) Sym.mem[i] = Sym.DIRT;
        check(King.digTile(c, east) == null, "king dig: not into unseen tiles");
        for (int i = 0; i < W * H; i++) Sym.mem[i] = Sym.SEEN | Sym.DIRT;
        MapLocation d3 = King.digTile(new MapLocation(1, 1), new MapLocation(0, 0));
        check(d3 == null, "king dig: not off the map " + d3);
    }

    static void testBiteGuard() {
        check(Baby.guardBite(95, 90) && Baby.guardBite(100, 90), "bite guard: healthier by up to one bite");
        check(!Baby.guardBite(90, 90) && !Baby.guardBite(101, 90), "bite guard: not equal, not out of one bite's reach");
        check(!Baby.guardBite(10, 5), "bite guard: a kill is ranked as a kill");
    }

    static void testKingDodge() {
        // the sunandmoon death: cat at (13,25) facing north, king at (14,28) in its next footprint
        RobotInfo cat = new RobotInfo(6, Team.NEUTRAL, UnitType.CAT, 4000, new MapLocation(13, 25), Direction.NORTH, 0, 0, null);
        RobotInfo[] cats = {cat};
        MapLocation k = new MapLocation(14, 28);
        check(King.catGap(k, new MapLocation(13, 26)) == 0 && King.catGap(k, cat.location) == 1, "king dodge: gap 0 in the lane, 1 touching");
        check(King.catGap(new MapLocation(17, 28), cat.location) == 2, "king dodge: gap 2 two columns off");
        int stay = King.catRisk(k, cats), east = King.catRisk(k.add(Direction.EAST), cats), west = King.catRisk(k.add(Direction.WEST), cats);
        check(King.catOverlap(k, new MapLocation(13, 26)) == 2 && King.catOverlap(k.add(Direction.EAST), new MapLocation(13, 26)) == 1
                && King.catOverlap(new MapLocation(16, 28), new MapLocation(13, 26)) == 0, "king dodge: overlap tiles 2, 1, 0");
        check(east < stay && east < west, "king dodge: a step east is half out of the lane " + stay + " " + east + " " + west);
        check(King.catRisk(new MapLocation(16, 28), cats) < east, "king dodge: two steps east is out");
        check(King.catRisk(new MapLocation(20, 28), cats) < King.catRisk(new MapLocation(17, 28), cats) || King.catRisk(new MapLocation(20, 28), cats) == 0,
                "king dodge: farther is no worse");
        check(King.catRisk(new MapLocation(40, 40), cats) == 0, "king dodge: a far cat is no risk");
        reset(); W = 30; H = 30; install();
        for (int i = 0; i < W * H; i++) Sym.mem[i] = Sym.SEEN;
        MapLocation c = new MapLocation(10, 10);
        check(King.dirtStep(c, Direction.NORTH) == null, "king dodge: an open step needs no dig");
        Sym.mem[10 + W * 12] |= Sym.DIRT;
        MapLocation dn = King.dirtStep(c, Direction.NORTH);
        check(dn != null && dn.x == 10 && dn.y == 12, "king dodge: dig the dirt the step adds " + dn);
        check(King.dirtStep(c, Direction.SOUTH) == null, "king dodge: a step away adds no dirt");
        Sym.mem[9 + W * 12] |= Sym.WALL;
        check(King.dirtStep(c, Direction.NORTH) == null, "king dodge: never into a wall");
        check(King.dirtStep(new MapLocation(1, 1), Direction.SOUTHWEST) == null, "king dodge: not off the map");
        // mirrored (rotation about the 60x40 centre): the same score
        RobotInfo mc = new RobotInfo(6, Team.NEUTRAL, UnitType.CAT, 4000, new MapLocation(59 - 14, 39 - 26), Direction.SOUTH, 0, 0, null);
        check(King.catRisk(new MapLocation(59 - 15, 39 - 28), new RobotInfo[]{mc}) == east, "king dodge: rotation-symmetric score");
    }

    static void testKingPress() throws GameActionException {
        reset(); install();
        G.round = 200; G.coop = false; G.me = me; Baby.role = Baby.FIGHT; Baby.eKing = null; Baby.lastEKing = null;
        Baby.lastEKingRound = -100; Baby.ekHops = 0;
        // codec: hops | (sighting round % 1024) << 2 in the 16-bit extra
        Baby.heardEKing(Comms.squeak(Comms.SQ_EKING, 5, 12, 1 | (198 & 1023) << 2));
        check(new MapLocation(5, 12).equals(Baby.lastEKing) && Baby.lastEKingRound == 198 && Baby.ekHops == 1,
              "king press: heard sighting decoded " + Baby.lastEKing + " r" + Baby.lastEKingRound + " h" + Baby.ekHops);
        Baby.heardEKing(Comms.squeak(Comms.SQ_EKING, 9, 9, 0 | (190 & 1023) << 2));
        check(new MapLocation(5, 12).equals(Baby.lastEKing), "king press: an older sighting is ignored");
        Baby.heardEKing(Comms.squeak(Comms.SQ_EKING, 6, 12, 0 | (198 & 1023) << 2));
        check(new MapLocation(6, 12).equals(Baby.lastEKing) && Baby.ekHops == 0, "king press: same sighting in fewer hops wins");
        G.round = 1024 + 2; Baby.lastEKingRound = -100;
        Baby.heardEKing(Comms.squeak(Comms.SQ_EKING, 6, 12, 0 | (1023 & 1023) << 2));
        check(Baby.lastEKingRound == 1023, "king press: sighting round across the 1024 wrap " + Baby.lastEKingRound);
        // a fighter walks to a fresh sighting in range
        G.round = 200; Baby.lastEKing = new MapLocation(5, 12); Baby.lastEKingRound = 198;
        MapLocation start = me;
        check(Baby.pressKing() && me.distanceSquaredTo(Baby.lastEKing) < start.distanceSquaredTo(Baby.lastEKing),
              "king press: a fighter closes on the king (" + me + ")");
        // too old, too far for a gatherer, or too weak: no press
        reset(); install(); G.coop = false; G.me = me; Baby.eKing = null; Baby.role = Baby.FIGHT;
        Baby.lastEKing = new MapLocation(5, 12); Baby.lastEKingRound = 198; G.round = 198 + Cfg.PRESS_AGE + 1;
        check(!Baby.pressKing(), "king press: a stale sighting is not chased");
        G.round = 200; Baby.role = Baby.GATHER;
        check(!Baby.pressKing(), "king press: a gatherer only presses a king within PRESS_GATHER_D2");
        Baby.role = Baby.FIGHT; health = Cfg.PRESS_HP - 1;
        check(!Baby.pressKing(), "king press: a weak rat does not press");
        // against the king's 3x3: hold and face its nearest tile
        reset(); install(); G.coop = false; G.me = me; Baby.role = Baby.FIGHT; G.round = 200; facing = Direction.EAST;
        RobotInfo k = new RobotInfo(70, G.them, UnitType.RAT_KING, 600, new MapLocation(5, 7), Direction.SOUTH, 0, 0, null);
        robots.put(k.location, k); Baby.eKing = k;
        check(Baby.pressKing() && me.equals(new MapLocation(5, 5)) && facing == Direction.NORTH,
              "king press: touching the 3x3, faces it (" + me + " " + facing + ")");
        // the sighting spot is empty when we get there: forget it
        reset(); install(); G.coop = false; G.me = me; Baby.role = Baby.FIGHT; G.round = 200; Baby.eKing = null;
        Baby.lastEKing = new MapLocation(5, 7); Baby.lastEKingRound = 199;
        check(!Baby.pressKing() && Baby.lastEKing == null, "king press: an empty sighting spot is forgotten");
        Baby.eKing = null; Baby.lastEKing = null; Baby.lastEKingRound = -100;
    }

    static void testDefend() throws GameActionException {
        reset(); W = 30; H = 30; install();
        G.round = 200; G.coop = false; G.me = me;
        // a king at (59, 3) with 5 enemy rats close: x and the threat share one 10-bit value
        shared[Comms.ALERT] = (59 + 1) | (5 << 6); shared[Comms.ALERT + 1] = 3 + 1; shared[Comms.ALERT + 2] = Comms.stamp();
        check(shared[Comms.ALERT] <= 1023 && new MapLocation(59, 3).equals(Comms.alertLoc()) && Comms.alertThreat() == 5,
              "defend: alert location and threat packed");
        shared[Comms.ALERT] = (me.x + 3 + 1) | (2 << 6); shared[Comms.ALERT + 1] = me.y + 1;
        check(me.translate(3, 0).equals(Baby.threatenedKing()), "defend: a threatened king nearby is defended");
        shared[Comms.ALERT] = me.x + 3 + 1;
        check(Baby.threatenedKing() == null, "defend: enemies seen but none close to the king: keep gathering");
        shared[Comms.ALERT] = (me.x + 3 + 1) | (2 << 6); G.round = 203;
        check(Baby.threatenedKing() == null, "defend: a stale alert is ignored");
    }

    static void testGuard() throws GameActionException {
        reset(); install();
        Nav.avoid = 0;
        Nav.guard = new MapLocation(8, 5);
        check(Nav.step(Direction.SOUTH), "guard: the step is taken");
        check(me.equals(new MapLocation(5, 4)) && facing != Direction.SOUTH, "guard: it strafes, still facing the enemy (" + facing + ")");
        Nav.guard = null;
        reset(); install();
        RobotInfo e = enemyRat(61, 7, 5, 100);
        robots.put(e.location, e); G.coop = false;
        Baby.guardTurn();
        check(facing == me.directionTo(e.location), "guard: the turn ends facing the nearest enemy rat (" + facing + ")");
        // out of view (behind us): an enemy heard from an ally's squeak
        reset(); install(); G.coop = false; turnCd = 0;
        TrapRisk.nHeard = 1; TrapRisk.heardE[0] = new MapLocation(4, 4);
        Baby.seen = new RobotInfo[0];
        Baby.guardTurn();
        check(!Cfg.GUARD_MEMORY || facing == Direction.SOUTHWEST, "guard: faces a squeaked enemy behind it (" + facing + ")");
        TrapRisk.nHeard = 0;
    }

    static void testTrapRisk() throws GameActionException {
        reset(); install();
        G.round = 100;
        TrapRisk.mark = null; TrapRisk.last = null;
        // an enemy at (5,9) facing south marks (4..6, 8); risky tiles reach one further, to row 7
        RobotInfo e = new RobotInfo(60, G.them, UnitType.BABY_RAT, 100, new MapLocation(5, 9), Direction.SOUTH, 0, 0, null);
        TrapRisk.observe(new RobotInfo[]{e});
        check(TrapRisk.risky(new MapLocation(5, 7)), "traprisk: tile next to the enemy's front is risky");
        check(!TrapRisk.risky(new MapLocation(5, 6)), "traprisk: two tiles off the front is safe");
        check(TrapRisk.mask(me) == 0, "traprisk: our neighbours (row 4..6) are not risky");
        G.round = 100 + TrapRisk.TTL + 1;
        check(!TrapRisk.risky(new MapLocation(5, 7)), "traprisk: marks expire");
        // an ally's squeak about an enemy at (12,12) facing west marks (11, 11..13)
        int c = Comms.squeak(Comms.SQ_TRAP, 12, 12, Direction.WEST.ordinal());
        TrapRisk.heard(new Message[]{new Message(c, 7, G.round, new MapLocation(10, 10))});
        check(TrapRisk.risky(new MapLocation(10, 12)), "traprisk: a squeaked enemy front marks tiles");
        check(!TrapRisk.risky(new MapLocation(14, 12)), "traprisk: behind a squeaked enemy is safe");
        // mask() agrees with risky() on every neighbour, for many tiles and random marks
        java.util.Random rnd = new java.util.Random(7);
        for (int i = 0; i < 60; i++) TrapRisk.put(new MapLocation(rnd.nextInt(W), rnd.nextInt(H)));
        int bad = 0;
        for (int x = 0; x < W; x++)
            for (int y = 0; y < H; y++) {
                MapLocation l = new MapLocation(x, y);
                int m = 0;
                for (int k = 0; k < 8; k++) if (TrapRisk.risky(l.add(G.DIRS[k]))) m |= 1 << k;
                if (m == 0xFF) m = 0;
                if (m != TrapRisk.mask(l)) bad++;
            }
        check(bad == 0, "traprisk: mask matches risky() everywhere (bad=" + bad + ")");
    }

    public static void main(String[] args) throws Exception {
        testTrapRisk();
        testMineSpread();
        testScout();
        testSeek();
        testHostile();
        testGlance();
        testMineMirror();
        testKingDig();
        testKingDodge();
        testBiteGuard();
        testCarryCap();
        testBabyCap();
        testNearKing();
        testFormIncome();
        testCatAtKing();
        testNearerAllies();
        testKingPress();
        testDefend();
        testGuard();
        testBiteCheese();
        testFlank();
        testSpawnFloor();
        testStandoff();
        testFort();
        testRetreat();
        testWarRoles();
        testMicroRetreatsWhenOutnumbered();
        testThrowDirection();
        testBiteTargets();
        testTransferReach();
        testRaid();
        testFormation();
        testReportPriority();
        testKingMine();
        testHandMirror();
        testSymElimination();
        testSymMineList();
        testSymRelay();
        testSqueakCodec();
        testFresh();
        testCatGeometry();
        testNavAroundWall();
        testNavDigsDirt();
        testNavUShape();
        testNavFlowSealedRoom();
        testFlowField();
        testFlowJam();
        testCheeseBan();
        testNavSameTarget();
        testAtMine();
        testKingBlock();
        testExploreUnseen();
        testKingNeverWritesOutOfRange();
        System.out.println("BotTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
