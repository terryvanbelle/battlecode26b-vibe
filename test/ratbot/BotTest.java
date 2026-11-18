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
    /** Extra move cooldown a step (carried cheese). */
    static int moveExtra = 0;
    static UnitType type = UnitType.BABY_RAT;
    static int ratCost = 10;
    static List<MapLocation> built = new ArrayList<>();
    static List<Integer> squeaks = new ArrayList<>();
    static int digsDone, dirtStock;
    static List<MapLocation> bitten = new ArrayList<>();
    static Direction thrown;
    static RobotInfo carrying;
    /** This rat is being carried / thrown (TrapRisk's grounded test). */
    static boolean beingCarried, beingThrown;
    /** Squeaks readable by round (readSqueaks(r): round r's; readSqueaks(-1): all). */
    static Map<Integer, List<Message>> heard = new HashMap<>();
    /** Rounds readSqueaks(r) was asked for. */
    static List<Integer> heardAsked = new ArrayList<>();
    static List<MapLocation> pickedUp = new ArrayList<>();
    /** canCarryRat's answer when everything else allows it (false: the target turned to face us). */
    static boolean carryOk = true;

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
        walls.clear(); dirt.clear(); cheeseAt.clear(); robots.clear(); Arrays.fill(shared, 0); squeaks.clear(); bitten.clear(); thrown = null; carrying = null;
        beingCarried = beingThrown = false; carryOk = true; heard.clear(); heardAsked.clear(); pickedUp.clear();
        me = new MapLocation(5, 5); facing = Direction.NORTH; health = 100; round = 1; global = 1000; raw = 0;
        moveCd = turnCd = actCd = moveExtra = 0; type = UnitType.BABY_RAT; digsDone = 0; dirtStock = 0; ratCost = 10; built.clear();
        Sym.mask = Sym.ROT | Sym.HOR | Sym.VER; Sym.contradictions = 0;
        Nav.target = null; Nav.bugging = false; Nav.avoid = 0; Nav.stall = 0; Nav.digs = 0; Nav.kingMarkN = 0;
        Flow.building = false; Flow.tgt = null; Flow.builtRound = -1; Flow.g = null; Flow.bucket = null; Flow.failTgt = null; Flow.lastTgt = null;
        Flow.cpath = new int[Flow.CK][]; Flow.cnext = 0; Flow.hits = 0; Flow.cache = Cfg.FLOW_CACHE; Flow.held = 0;
        Flow.rev = Cfg.FLOW_REV; Flow.lifo = Cfg.FLOW_LIFO; Flow.fast = Cfg.FLOW_FAST; King.faceSpawn = Cfg.SYM_FACE;
        Trail.at = null; Trail.tiles = null; Trail.n = 0; Baby.homeRound = -10; Baby.homing = false; King.laneN = 0; King.lane0 = 0;
        Nav.swapHold = Cfg.SWAP_HOLD; Nav.swapFrom = null; Nav.swaps = 0; Baby.nearestEnemy = null;
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
                    case "getCarrying": return carrying;
                    case "canCarryRat": {
                        MapLocation l = (MapLocation) a[0];
                        return carryOk && actCd < 10 && carrying == null && sensable(l) && me.distanceSquaredTo(l) <= 2 && robots.containsKey(l);
                    }
                    case "carryRat": carrying = robots.remove((MapLocation) a[0]); actCd += 20; return null;
                    case "canDropRat": { MapLocation t = me.add((Direction) a[0]); return actCd < 10 && carrying != null && passable(t) && !robots.containsKey(t); }
                    case "dropRat": {
                        MapLocation t = me.add((Direction) a[0]);
                        RobotInfo c = carrying;
                        robots.put(t, new RobotInfo(c.ID, c.team, c.type, c.health, t, c.direction, c.chirality, c.cheeseAmount, null));
                        carrying = null; actCd += 20; return null;
                    }
                    case "isCooperation": return true;
                    case "getGlobalCheese": return global;
                    case "getRawCheese": return raw;
                    case "getAllCheese": return global + raw;
                    case "onTheMap": return onMap((MapLocation) a[0]);
                    case "sensePassability": return passable((MapLocation) a[0]);
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
                        moveCd += (d == facing ? 10 : 18) + moveExtra;
                        me = t; return null;
                    }
                    case "canRemoveDirt": {
                        MapLocation l = (MapLocation) a[0];
                        return actCd < 10 && sensable(l) && me.distanceSquaredTo(l) <= 2 && dirt.contains(l) && global + raw >= 5;
                    }
                    case "removeDirt": dirt.remove((MapLocation) a[0]); actCd += 25; global -= 5; digsDone++; return null;
                    case "canPlaceDirt": {
                        MapLocation l = (MapLocation) a[0];
                        return actCd < 10 && sensable(l) && me.distanceSquaredTo(l) <= (type == UnitType.RAT_KING ? 8 : 2) && dirtStock > 0
                                && !walls.contains(l) && !dirt.contains(l) && !robots.containsKey(l);
                    }
                    case "placeDirt": dirt.add((MapLocation) a[0]); dirtStock--; actCd += 25; return null;
                    case "getDirt": return dirtStock;
                    case "senseNearbyMapInfos": {
                        List<MapInfo> out = new ArrayList<>();
                        int vr = type == UnitType.RAT_KING ? 25 : 20;
                        for (int x = me.x - 5; x <= me.x + 5; x++)
                            for (int y = me.y - 5; y <= me.y + 5; y++) {
                                MapLocation l = new MapLocation(x, y);
                                if (!sensable(l) || me.distanceSquaredTo(l) > vr) continue;
                                out.add(new MapInfo(l, passable(l), null, walls.contains(l), dirt.contains(l), cheeseAt.getOrDefault(l, 0), TrapType.NONE, false));
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
                    case "readSqueaks": {
                        int r = (Integer) a[0];
                        heardAsked.add(r);
                        List<Message> out = new ArrayList<>();
                        if (r == -1) for (List<Message> l : heard.values()) out.addAll(l);
                        else out.addAll(heard.getOrDefault(r, List.of()));
                        return out.toArray(new Message[0]);
                    }
                    case "getNumberCatTraps": case "getNumberRatTraps": return 0;
                    case "isBeingCarried": return beingCarried;
                    case "isBeingThrown": return beingThrown;
                    case "canPickUpCheese": { MapLocation l = (MapLocation) a[0]; return cheeseAt.getOrDefault(l, 0) > 0; }
                    case "pickUpCheese": { MapLocation l = (MapLocation) a[0]; global += cheeseAt.remove(l); pickedUp.add(l); return null; }
                    case "getCurrentRatCost": return ratCost;
                    case "canBuildRat": { MapLocation t = (MapLocation) a[0]; return actCd < 10 && passable(t) && !robots.containsKey(t) && global >= ratCost; }
                    case "buildRat": built.add((MapLocation) a[0]); global -= ratCost; actCd += 10; return null;
                    case "getHealth": return health;
                    case "canBecomeRatKing": return canPromote && actCd < 10;
                    case "becomeRatKing": promoted = true; type = UnitType.RAT_KING; return null;
                    case "setIndicatorString": return null;
                    case "canPlaceRatTrap": { MapLocation t = (MapLocation) a[0]; return actCd < 10 && me.isAdjacentTo(t) && passable(t) && !robots.containsKey(t); }
                    default: throw new UnsupportedOperationException("fakeRc: " + m.getName());
                }
            });
    }

    /** The fake bytecode clock: Clock.getBytecodesLeft() reads RobotMonitor's private static bytecodesLeft (0 outside
     *  the engine), so tests set it (and bytecodeLimit, which getBytecodeNum subtracts from). Nothing spends it. */
    static void clock(int left) {
        try {
            Class<?> c = Class.forName("battlecode.instrumenter.inject.RobotMonitor");
            java.lang.reflect.Field f = c.getDeclaredField("bytecodesLeft"); f.setAccessible(true); f.setInt(null, left);
            f = c.getDeclaredField("bytecodeLimit"); f.setAccessible(true); f.setInt(null, 1 << 30);
        } catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
    }

    static void install() {
        G.init(fakeRc());
        Sym.init();
        clock(1 << 30);
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

    static void testStallCentre() {
        // even width: the two centre tiles split by side; odd width: the one centre tile; mirror images map to mirror images
        int[][] dims = {{60, 60}, {59, 41}, {20, 30}};
        for (int[] d : dims) {
            int cx2 = d[0] - 1, cy2 = d[1] - 1;
            for (int x = 0; x < d[0]; x += 3)
                for (int y = 0; y < d[1]; y += 4) {
                    MapLocation h = new MapLocation(x, y), c = Baby.centreTile(h, cx2, cy2);
                    MapLocation rot = Baby.centreTile(new MapLocation(cx2 - x, cy2 - y), cx2, cy2);
                    MapLocation fx = Baby.centreTile(new MapLocation(cx2 - x, y), cx2, cy2);
                    MapLocation fy = Baby.centreTile(new MapLocation(x, cy2 - y), cx2, cy2);
                    check(rot.x == cx2 - c.x && rot.y == cy2 - c.y && fx.x == cx2 - c.x && fx.y == c.y && fy.x == c.x && fy.y == cy2 - c.y,
                          "stall centre: mirror-invariant at " + h + " on " + d[0] + "x" + d[1]);
                    check(Math.abs(2 * c.x - cx2) <= 1 && Math.abs(2 * c.y - cy2) <= 1, "stall centre: a centre tile at " + h);
                }
        }
    }

    static void testWalledOff() {
        reset(); W = 30; H = 30; install();
        // Stash: a wall row y=22 with dirt at the corners; piles round the mines below it are walled off
        for (int x = 2; x < 28; x++) Sym.mem[G.idx(x, 22)] = Sym.SEEN | Sym.WALL;
        Sym.mem[G.idx(1, 22)] = Sym.SEEN | Sym.DIRT;
        MapLocation me = new MapLocation(16, 24);
        check(Baby.walledOff(me, new MapLocation(15, 20)) && Baby.walledOff(me, new MapLocation(18, 21)),
              "walled off: a pile seen through the wall row");
        check(!Baby.walledOff(me, new MapLocation(13, 26)) && !Baby.walledOff(me, new MapLocation(16, 23)) && !Baby.walledOff(me, me),
              "walled off: piles on our side, one next to the wall, our own tile");
        check(!Baby.walledOff(new MapLocation(1, 24), new MapLocation(1, 20)), "walled off: dirt is no wall (it can be dug)");
        check(!Baby.walledOff(new MapLocation(16, 21), new MapLocation(16, 22)), "walled off: the target tile itself is not crossed");
        // mirror invariance: random walls and segments, each symmetry
        Random r = new Random(5);
        int bad = 0;
        for (int i = 0; i < 300; i++) {
            reset(); W = 15 + r.nextInt(10); H = 15 + r.nextInt(10); install();
            int s = new int[]{Sym.ROT, Sym.HOR, Sym.VER}[i % 3];
            for (int k = 0; k < 30; k++) {
                MapLocation w = new MapLocation(r.nextInt(W), r.nextInt(H)), mw = G.mirror(w, s);
                Sym.mem[G.idx(w)] |= Sym.WALL; Sym.mem[G.idx(mw)] |= Sym.WALL;
            }
            for (int k = 0; k < 20; k++) {
                MapLocation a = new MapLocation(r.nextInt(W), r.nextInt(H));
                MapLocation b = new MapLocation(Math.max(0, Math.min(W - 1, a.x + r.nextInt(9) - 4)), Math.max(0, Math.min(H - 1, a.y + r.nextInt(9) - 4)));
                if (Baby.walledOff(a, b) != Baby.walledOff(G.mirror(a, s), G.mirror(b, s))) bad++;
            }
        }
        check(bad == 0, "walled off: mirrored walls and segments agree (bad=" + bad + ")");
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

    /** Cfg.FLOW_PROGRESS: a detour of 40+ tiles round a wall (seepastyourgreed's corridors) is walked with the stall
     *  kept low; without it the stall passes the cheese chase's 12 on the way. */
    static void testFlowProgress() throws GameActionException {
        int[] worst = new int[2];
        boolean[] got = new boolean[2];
        for (int k = 0; k < 2; k++) {
            reset(); W = 40; H = 14; install();
            for (int x = 5; x < W; x++) walls.add(new MapLocation(x, 5));
            me = new MapLocation(25, 3); facing = Direction.NORTH;
            Nav.progress = k == 1;
            MapLocation t = new MapLocation(25, 8);
            int steps = 0;
            while (!me.equals(t) && steps < 300) { tick(t); steps++; worst[k] = Math.max(worst[k], Nav.stall); }
            got[k] = me.equals(t);
        }
        Nav.progress = Cfg.FLOW_PROGRESS;
        check(got[1] && worst[1] <= 12, "flow progress: the detour is walked with the stall at most 12 (" + got[1] + ", " + worst[1] + ")");
        check(worst[0] > 12, "flow progress: without it the stall passes 12 on the way (" + worst[0] + ")");
    }

    /** A wall at x 15 (gap at the top) between a rat and its target: a dropped path is adopted again from the cache
     *  from beside it, walked backwards toward its start, forgotten when a wall turns up on it; off, a new search runs. */
    static void testFlowCache() throws GameActionException {
        for (int on = 0; on < 2; on++) {
            reset(); W = 30; H = 20; install();
            for (int y = 0; y < 16; y++) walls.add(new MapLocation(15, y));
            for (MapLocation w : walls) Sym.record(w.x, w.y, true, false, false);
            Flow.cache = on == 1;
            MapLocation a = new MapLocation(5, 5), t = new MapLocation(25, 5);
            me = a; G.startTurn();
            Flow.request(t); Flow.work(0);
            int b0 = Flow.builds, L = Flow.plen;
            int[] p = new int[L];
            for (int i = 0; i < L; i++) p[i] = Flow.path[i];
            check(Flow.ready(t) && L > 20, "flow cache: the first search finds the detour (" + L + ")");
            // dropped (held by a robot) a few tiles on; asked again from there
            me = new MapLocation(p[6] % W, p[6] / W); G.startTurn();
            Flow.drop(); Flow.request(t);
            if (on == 0) { check(Flow.building && Flow.builds == b0 + 1, "flow cache: off, a dropped path means a new search"); continue; }
            check(Flow.ready(t) && !Flow.building && Flow.builds == b0 && Flow.hits == 1 && Flow.path[Flow.ppos] == p[6] && Flow.plen == L,
                  "flow cache: a dropped path is taken again from the rat's tile (" + Flow.hits + ", " + Flow.ppos + ")");
            // near the target, heading back to the start: the path walked backwards
            me = new MapLocation(p[L - 3] % W + 1, p[L - 3] / W); G.startTurn();
            Flow.drop(); Flow.request(a);
            boolean back = Flow.ready(a) && Flow.hits == 2 && Flow.path[Flow.plen - 1] == p[0] && Flow.builds == b0;
            int nx = Flow.path[Flow.ppos + 1];
            check(back && Math.abs(nx % W - me.x) <= 1 && Math.abs(nx / W - me.y) <= 1,
                  "flow cache: toward the start, the path is walked backwards from beside us (" + back + ")");
            // a new wall on the cached path: following it drops and forgets the path, the next request searches
            me = new MapLocation(p[6] % W, p[6] / W); G.startTurn();
            Flow.drop(); Flow.request(t);
            int ahead = Flow.path[Flow.ppos + 1];
            Sym.mem[ahead] |= Sym.WALL;
            boolean f = Flow.follow(t);
            Flow.request(t);
            check(!f && Flow.building && Flow.builds == b0 + 1 && Flow.hits == 3, "flow cache: a wall on the path forgets it (" + f + ", " + Flow.hits + ")");
        }
    }

    /** Cfg.FLOW_HELD: a strip y 5, x 1-4, walled above and below, open to the west at x 1; its east end (5,5) is a wall
     *  the rat at (2,5) has not seen. Toward (12,5) the search goes east through the strip; with (3,5) held (an ally
     *  stuck there) it goes out west and round. A held tile beside the target is not kept out; it outlasts a re-sense
     *  and expires after FLOW_HELD rounds. Both search directions. */
    /** FACE_REACH's test: an enemy at Chebyshev 2 reaches us in one move only through an open tile beside both of us
     *  (unknown counts as open; walls and dirt block; off-map tiles don't count). */
    static void testFaceReach() throws GameActionException {
        reset(); W = 20; H = 12; install();
        MapLocation a = new MapLocation(5, 5), e = new MapLocation(7, 5);
        boolean open = Baby.reach1(a, e);
        Sym.record(6, 4, true, false, false); Sym.record(6, 5, true, false, false);
        boolean one = Baby.reach1(a, e);
        Sym.record(6, 6, false, true, false);
        boolean shut = !Baby.reach1(a, e) && !Baby.reach1(e, a);
        Sym.record(6, 6, false, false, false);
        boolean reopened = Baby.reach1(a, e);
        check(open && one && shut && reopened, "face reach: a wall line with a dirt gap is shut, an open gap is not ("
              + open + ", " + one + ", " + shut + ", " + reopened + ")");
        Sym.record(6, 6, true, false, false);
        boolean diag = !Baby.reach1(a, new MapLocation(7, 6)) && !Baby.reach1(a, new MapLocation(7, 7))
                && Baby.reach1(a, new MapLocation(5, 7)) && Baby.reach1(a, new MapLocation(6, 7));
        reset(); W = 20; H = 12; install();
        MapLocation c = new MapLocation(0, 5), f = new MapLocation(0, 7);
        Sym.record(0, 6, true, false, false);
        boolean edge = Baby.reach1(c, f);
        Sym.record(1, 6, true, false, false);
        edge = edge && !Baby.reach1(c, f) && !Baby.reach1(f, c);
        check(diag && edge, "face reach: diagonal offsets use their shared tiles; the map edge is not a way round ("
              + diag + ", " + edge + ")");
    }

    /** Cfg.HOME_START / SYM_KING_GAP on uneasy_alliance's layout (30x30, kings start (11,15) and (18,15)). */
    static void testHomeStart() throws GameActionException {
        reset(); W = 30; H = 30; install();
        MapLocation start = new MapLocation(11, 15), king = new MapLocation(8, 21);
        check(King.gapMask(start, 0) == 0, "gap: no symmetry maps the start onto itself");
        check(King.gapMask(start, 2) == Sym.HOR, "gap: only the horizontal twin (11,14) overlaps our 3x3");
        check(King.gapMask(new MapLocation(14, 14), 1) == (Sym.ROT | Sym.HOR | Sym.VER), "gap: every twin of a centre start is within 1");
        // the rotational half from the king's tile (8,21) loses the home mines (8,8), (8,2); from the start it keeps them
        MapLocation[] far = {new MapLocation(8, 8), new MapLocation(8, 2), new MapLocation(2, 7)};
        for (MapLocation m : far)
            check(Baby.nearer(m, start, Sym.ROT) && Baby.nearer(m, start, Sym.VER), "half: " + m + " on our half from the start");
        check(!Baby.nearer(far[1], king, Sym.ROT), "half: (8,2) off it from the king's tile");
        check(Baby.nearer(far[0], king, Sym.ROT), "half: (8,8), on the bisector from the king's tile, counts as ours (ties)");
        check(!Baby.nearer(new MapLocation(21, 8), start, Sym.ROT) && !Baby.nearer(new MapLocation(21, 21), start, Sym.VER),
              "half: their mines stay theirs");
        Message other = new Message(Comms.squeak(Comms.SQ_MINE, 3, 4, 0), 7, 1, start);
        Message st = new Message(Comms.squeak(Comms.SQ_START, 11, 15, 0), 7, 1, start);
        check(Baby.heardStart(new Message[]{other}) == null, "start: no start squeak");
        check(start.equals(Baby.heardStart(new Message[]{other, st})), "start: read from the king's squeak");
    }

    /** Cfg.FORM_MINE's site pick on a 30x30 map, first king at (3,3), the enemy start (26,26) under rotation. */
    static void testAtkCarry() {
        check(Baby.atkTake(0, 20, 25) && Baby.atkTake(5, 20, 25), "atk carry: piles within the cap are taken");
        check(!Baby.atkTake(0, 40, 25) && !Baby.atkTake(10, 20, 25), "atk carry: an attacker takes no pile past the cap, empty-handed or not");
    }

    static void testCatLay() {
        reset(); W = 30; H = 30; install();
        int g = Cfg.CAT_LAY_GAP;
        check((CatLane.mark(0, 100, g) & CatLane.LANE) == 0, "cat lane: one pass is not lane");
        check((CatLane.mark(CatLane.mark(0, 100, g), 101, g) & CatLane.LANE) == 0, "cat lane: a cat standing still is one pass");
        check((CatLane.mark(CatLane.mark(0, 100, g), 100 + g, g) & CatLane.LANE) != 0, "cat lane: a second pass gap rounds on is lane");
        int v = CatLane.mark(CatLane.mark(CatLane.mark(0, 100, g), 100 + g, g), 300, g);
        check((v & CatLane.LANE) != 0 && (v & CatLane.ROUND) == 301, "cat lane: lane stays and the round moves on");
        CatLane.rows = null;
        MapLocation bl = new MapLocation(10, 12);
        CatLane.see(bl, 50);
        check(!CatLane.lane(10, 12) && CatLane.age(11, 13, 55) == 5 && CatLane.age(12, 12, 55) >= 1 << 20, "cat lane: the 2x2 body is marked");
        CatLane.see(bl, 50 + g);
        check(CatLane.lane(10, 12) && CatLane.lane(11, 12) && CatLane.lane(10, 13) && CatLane.lane(11, 13) && !CatLane.lane(12, 12),
              "cat lane: the body's four tiles turn lane on the second pass");
        int used = 0;
        for (int[] row : CatLane.rows) if (row != null) used++;
        check(used == 2, "cat lane: only the two body rows are allocated (" + used + ")");
        CatLane.see(new MapLocation(-1, H - 1), 10);
        check(CatLane.age(0, H - 1, 10) == 0 && !CatLane.lane(-1, H - 1), "cat lane: off-map parts are skipped");
        MapLocation a = new MapLocation(14, 14), b = new MapLocation(20, 20);
        check(Baby.laneKey(3, b, false) < Baby.laneKey(4, a, true), "cat lane key: the more recent pass first");
        check(Baby.laneKey(5, a, false) < Baby.laneKey(5, b, true), "cat lane key: then nearer the centre");
        check(Baby.laneKey(5, a, true) < Baby.laneKey(5, a, false), "cat lane key: then the pick bit");
        // the key is the same for a tile and its mirror twin under each symmetry (bits equal)
        MapLocation r = new MapLocation(W - 1 - b.x, H - 1 - b.y), h = new MapLocation(W - 1 - b.x, b.y), u = new MapLocation(b.x, H - 1 - b.y);
        check(Baby.laneKey(7, b, true) == Baby.laneKey(7, r, true) && Baby.laneKey(7, b, true) == Baby.laneKey(7, h, true)
              && Baby.laneKey(7, b, true) == Baby.laneKey(7, u, true), "cat lane key: mirror twins rank alike");
        CatLane.rows = null;
    }

    static void testCatLane() throws GameActionException {
        reset(); W = 30; H = 30; install();
        type = UnitType.RAT_KING; me = new MapLocation(5, 6); G.startTurn();
        King.markLane(new MapLocation(5, 7));
        King.markLane(new MapLocation(29, 29));   // the 2x2 runs off the map's corner: marks (29,29) only
        check((Sym.mem[G.idx(new MapLocation(6, 8))] & Sym.CAT) != 0 && (Sym.mem[G.idx(new MapLocation(7, 8))] & Sym.CAT) == 0,
              "lane: the cat's 2x2 from its bottom-left");
        check(King.laneCount(new MapLocation(5, 6)) == 2 && King.laneCount(new MapLocation(5, 5)) == 0
              && King.laneCount(new MapLocation(5, 8)) == 4 && King.laneCount(new MapLocation(29, 29)) == 1, "lane: tiles under a 3x3");
        Sym.record(5, 7, false, false, false);
        check((Sym.mem[G.idx(new MapLocation(5, 7))] & Sym.CAT) != 0, "lane: survives a fresh sensing of the tile");
        check(!King.laneOk(me, Direction.NORTH) && King.laneOk(me, Direction.SOUTH), "lane: laneOk refuses a step adding lane to the 3x3");
        check(King.offLane(me, new RobotInfo[0]) && King.laneCount(me) == 0 && me.y == 5, "lane: steps off, south");
        check(!King.offLane(me, new RobotInfo[0]), "lane: off lane, no step");
        // the unrolled count matches a plain 3x3 count everywhere, edges included
        Random rnd = new Random(7);
        for (int k = 0; k < 40; k++) King.markLane(new MapLocation(rnd.nextInt(W), rnd.nextInt(H)));
        boolean same = true;
        for (int x = 0; x < W; x++)
            for (int y = 0; y < H; y++) {
                int n = 0;
                for (int dx = -1; dx <= 1; dx++)
                    for (int dy = -1; dy <= 1; dy++) {
                        int xx = x + dx, yy = y + dy;
                        if (xx >= 0 && yy >= 0 && xx < W && yy < H && (Sym.mem[xx + W * yy] & Sym.CAT) != 0) n++;
                    }
                if (n != King.laneCount(new MapLocation(x, y))) same = false;
            }
        check(same, "lane: unrolled count = plain count on every tile");
    }

    static RobotInfo rat(Team t, int x, int y) { return new RobotInfo(9, t, UnitType.BABY_RAT, 100, new MapLocation(x, y), Direction.NORTH, 0, 0, null); }

    static RobotInfo hrat(Team t, int hp, RobotInfo held) { return new RobotInfo(9, t, UnitType.BABY_RAT, hp, new MapLocation(3, 3), Direction.NORTH, 0, 0, held); }

    static void testFrontier() throws GameActionException {
        MapLocation[] tg = new MapLocation[2];
        for (int mir = 0; mir < 3; mir++) {
            reset(); W = 20; H = 20; install();
            for (int x = 3; x <= 9; x++)
                for (int y = 3; y <= 9; y++) {
                    int mx = mir == 1 ? W - 1 - x : x;
                    boolean ring = x == 3 || x == 9 || y == 3 || y == 9, exit = x == 9 && y == 6 && mir != 2;
                    Sym.record(mx, y, ring && !exit, exit, false);
                }
            me = new MapLocation(mir == 1 ? W - 1 - 5 : 5, 6); G.startTurn();
            Flow.drop();
            Flow.requestFrontier();
            Flow.work(0);
            if (mir == 2) {
                check(!Flow.frontierLive() && Flow.frontierFail == G.round, "frontier: a sealed room finds nothing unseen");
                continue;
            }
            MapLocation t = Flow.tgt;
            boolean viaDirt = false;
            for (int i = 0; i < Flow.plen; i++) if (Flow.path[i] == G.idx(mir == 1 ? W - 1 - 9 : 9, 6)) viaDirt = true;
            check(Flow.frontierLive() && !Flow.building && t != null && !Sym.known(G.idx(t)) && viaDirt
                  && Math.abs(t.x - (mir == 1 ? W - 1 - 10 : 10)) == 0,
                  "frontier: the nearest unseen tile is outside the dirt exit (" + t + ", " + viaDirt + ")");
            tg[mir] = t;
        }
        check(tg[0] != null && tg[1] != null, "frontier: both rooms give a target (" + tg[0] + ", " + tg[1] + ")");
        Baby.breakoutUntil = -1; Baby.wallStrikes = 0; Baby.lastStrike = -1000;
        Baby.wallStrike(100, 150);
        boolean one = Baby.breakoutUntil < 0;
        Baby.wallStrike(180, 150);
        check(one && Baby.breakoutUntil == 330, "breakout: the second strike within 100 rounds starts it (" + Baby.breakoutUntil + ")");
        Baby.breakoutUntil = -1; Baby.wallStrikes = 0; Baby.lastStrike = -1000;
        Baby.wallStrike(100, 150); Baby.wallStrike(250, 150);
        check(Baby.breakoutUntil < 0, "breakout: strikes 150 rounds apart start nothing");
    }

    /** A walk of adjacent free tiles from a to b (no walls), or why not. */
    static String pathCheck(MapLocation a, MapLocation b) {
        if (Flow.plen < 2 || Flow.path[0] != G.idx(a) || Flow.path[Flow.plen - 1] != G.idx(b)) return "ends";
        for (int i = 0; i < Flow.plen; i++) {
            int p = Flow.path[i];
            if (walls.contains(new MapLocation(p % W, p / W))) return "wall at " + i;
            if (i > 0) {
                int q = Flow.path[i - 1];
                if (Math.max(Math.abs(p % W - q % W), Math.abs(p / W - q / W)) != 1) return "gap at " + i;
            }
        }
        return null;
    }

    /** FLOW_FAST's unrolled reverse search finds the same path as the plain loop, in every side frame and with
     *  LIFO ties, on random walls and dirt, from the start tile and after the rat walked on. */
    static void testFlowFast() throws GameActionException {
        java.util.Random rnd = new java.util.Random(7);
        int same = 0, runs = 0, found = 0;
        String diff = null;
        for (int trial = 0; trial < 6; trial++) {
            List<MapLocation> wl = new ArrayList<>(), dl = new ArrayList<>();
            for (int i = 0; i < 110; i++) wl.add(new MapLocation(rnd.nextInt(30), rnd.nextInt(20)));
            for (int i = 0; i < 40; i++) dl.add(new MapLocation(rnd.nextInt(30), rnd.nextInt(20)));
            MapLocation a = new MapLocation(rnd.nextInt(30), rnd.nextInt(20)), t = new MapLocation(rnd.nextInt(30), rnd.nextInt(20));
            MapLocation moved = new MapLocation(Math.min(29, a.x + 1), a.y);
            for (int fr = 0; fr < 4; fr++)
                for (int v = 0; v < 4; v++) {
                    int[][] got = new int[2][];
                    for (int fast = 0; fast < 2; fast++) {
                        reset(); W = 30; H = 20; install();
                        for (MapLocation w : wl) if (!w.equals(a) && !w.equals(t) && !w.equals(moved)) { walls.add(w); Sym.record(w.x, w.y, true, false, false); }
                        for (MapLocation d : dl) if (!walls.contains(d)) Sym.record(d.x, d.y, false, true, false);
                        Flow.rev = true; Flow.lifo = (v & 1) != 0; Flow.fast = fast == 1;
                        me = a; G.startTurn();
                        Flow.request(t);
                        if ((v & 2) != 0) { me = moved; G.startTurn(); }
                        Flow.work(0);
                        got[fast] = Flow.ready(t) ? java.util.Arrays.copyOf(Flow.path, Flow.plen) : new int[] {-1, Flow.fails};
                    }
                    runs++;
                    if (got[1][0] >= 0) found++;
                    if (java.util.Arrays.equals(got[0], got[1])) same++;
                    else if (diff == null) diff = "trial " + trial + " frame " + fr + " v " + v + ": " + got[0].length + " vs " + got[1].length;
                }
        }
        Flow.ox = Flow.DX; Flow.oy = Flow.DY;
        check(same == runs && found * 4 > runs * 3, "flow fast: the unrolled search finds the plain loop's path (" + same + "/" + runs + ", "
              + found + " found" + (diff == null ? "" : ", " + diff) + ")");
    }

    /** The reverse search (FLOW_REV) finds a path as short as the forward one, from wherever the rat stands when it
     *  completes; with LIFO buckets as well; a walled-in rat fails it. */
    static void testFlowRev() throws GameActionException {
        int[] len = new int[4];
        for (int v = 0; v < 4; v++) {
            reset(); W = 30; H = 20; install();
            for (int y = 0; y < 16; y++) walls.add(new MapLocation(15, y));
            for (MapLocation w : walls) Sym.record(w.x, w.y, true, false, false);
            Flow.rev = v >= 1; Flow.lifo = v == 3;
            MapLocation a = new MapLocation(5, 5), t = new MapLocation(25, 5);
            me = a; G.startTurn();
            Flow.request(t);
            if (v == 2) { me = new MapLocation(9, 9); G.startTurn(); }   // walked on while the search ran
            Flow.work(0);
            String bad = Flow.ready(t) ? pathCheck(me, t) : "not ready";
            len[v] = Flow.plen;
            check(bad == null, "flow rev " + v + ": a clean path from the rat's tile to the target (" + bad + ")");
        }
        check(len[1] == len[0] && len[3] == len[0], "flow rev: as short as the forward search (" + len[0] + " " + len[1] + " " + len[3] + ")");
        check(len[2] < len[0], "flow rev: the path starts where the rat walked to (" + len[2] + ")");
        reset(); W = 20; H = 20; install();
        for (int x = 3; x <= 7; x++) for (int y = 3; y <= 7; y++) if (x == 3 || x == 7 || y == 3 || y == 7) walls.add(new MapLocation(x, y));
        for (MapLocation w : walls) Sym.record(w.x, w.y, true, false, false);
        Flow.rev = true;
        me = new MapLocation(5, 5); G.startTurn();
        MapLocation t = new MapLocation(15, 15);
        int f0 = Flow.fails;
        Flow.request(t); Flow.work(0);
        check(!Flow.ready(t) && !Flow.building && Flow.fails == f0 + 1 && Flow.searching(t) == false,
              "flow rev: a walled-in rat is unreachable: the search fails");
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

    static MapLocation p(int x, int y) { return new MapLocation(x, y); }

    /** Stand on each tile in turn (a round each) and extend the trail. */
    static void trailWalk(MapLocation... ls) {
        for (MapLocation l : ls) { round++; me = l; G.startTurn(); Trail.record(); }
    }

    static void testTrail() throws GameActionException {
        reset(); W = 30; H = 20; install();
        Trail.alloc();
        trailWalk(p(5, 5), p(6, 5), p(7, 5), p(7, 6), p(6, 6));
        check(Trail.n == 5, "trail: five tiles walked (" + Trail.n + ")");
        trailWalk(p(6, 5));
        check(Trail.n == 2 && Trail.pos(G.idx(p(7, 5))) == 0 && Trail.pos(G.idx(p(6, 6))) == 0 && Trail.pos(G.idx(p(6, 5))) == 2,
            "trail: stepping back onto it cuts the loop (" + Trail.n + ")");
        trailWalk(p(7, 5), p(7, 6), p(6, 6));
        moveCd = 0;
        check(Trail.back() == Trail.MOVED && me.equals(p(5, 5)), "trail: back cuts the corner to the earliest neighbour (" + me + ")");
        trailWalk(me);
        check(Trail.n == 1, "trail: at the start one tile is left (" + Trail.n + ")");
        moveCd = 0;
        check(Trail.back() == Trail.END && me.equals(p(5, 5)), "trail: END at its start");
        trailWalk(p(6, 5), p(7, 5), p(7, 6), p(6, 6));
        for (MapLocation l : new MapLocation[] { p(5, 5), p(6, 5), p(7, 5), p(7, 6) })
            robots.put(l, new RobotInfo(3, Team.A, UnitType.BABY_RAT, 100, l, Direction.NORTH, 0, 0, null));
        moveCd = 0;
        check(Trail.back() == Trail.HELD && me.equals(p(6, 6)), "trail: HELD when every earlier neighbour is occupied");
        robots.clear();
        Nav.avoid = 1 << Direction.SOUTHWEST.ordinal();
        moveCd = 0;
        check(Trail.back() == Trail.MOVED && me.equals(p(6, 5)), "trail: an avoided step is skipped for the next earliest (" + me + ")");
        Nav.avoid = 0;
        moveCd = 20;
        trailWalk(me);
        check(Trail.back() == Trail.HELD && me.equals(p(6, 5)), "trail: HELD while movement is not ready");
        trailWalk(p(12, 12));
        check(Trail.n == 1 && Trail.pos(G.idx(p(5, 5))) == 0 && Trail.pos(G.idx(p(12, 12))) == 1, "trail: a jump starts a new chain");
        int with = homeRun(true), without = homeRun(false);
        check(with > 0 && with <= 40, "trail: a stalled carrier walks its trail back to a king behind a wall (" + with + " turns)");
        check(with > 0 && (without < 0 || with <= without), "trail: no slower than nav alone (" + with + " vs " + without + ")");
        int gapped = homeRun(true, 6, 12), ungapped = homeRun(true, 6, 0), navOnly = homeRun(false, 6, 0);
        // the reverse, dig-aware Flow now brings the slowed carrier home without a TRAIL_STALL stall here, so the trail
        // need not fire; it must never be slower
        check(gapped > 0 && (navOnly < 0 || gapped <= navOnly), "trail gap: a slowed carrier skipping rounds gets home no slower ("
            + gapped + " turns, nav alone " + navOnly + ")");
        check(ungapped == navOnly, "trail gap: with TRAIL_GAP 0 the skipped rounds keep the trail from firing (" + ungapped + ")");
    }

    /** Turns until a carrier at (16,5) is in transfer reach of a king at (5,5) behind a wall at x=10 (y 0..16), having
     *  walked out round the wall's end; -1 if not within 150. With trail, Baby.homeLeg takes the turns it wants. */
    static int homeRun(boolean trail) throws GameActionException { return homeRun(trail, 0, -1); }

    /** homeRun with extra move cooldown a step (a carrier's cheese) and, for gap >= 0, the bot's gating: the move
     *  decision only on movement-ready rounds, Baby.trailGap = gap. */
    static int homeRun(boolean trail, int extra, int gap) throws GameActionException {
        reset(); W = 30; H = 20; install();
        moveExtra = extra;
        Trail.alloc();
        int keep = Baby.trailGap;
        if (gap >= 0) Baby.trailGap = gap;
        for (int y = 0; y <= 16; y++) walls.add(p(10, y));
        MapLocation k = p(5, 5);
        for (int x = 4; x <= 6; x++)
            for (int y = 4; y <= 6; y++) robots.put(p(x, y), new RobotInfo(1, Team.A, UnitType.RAT_KING, 600, k, Direction.NORTH, 0, 0, null));
        List<MapLocation> out = new ArrayList<>(List.of(p(7, 6), p(8, 7), p(9, 8)));
        for (int y = 9; y <= 17; y++) out.add(p(9, y));
        out.add(p(10, 17));
        for (int y = 16; y >= 6; y--) out.add(p(11, y));
        for (int x = 12; x <= 16; x++) out.add(p(x, 5));
        trailWalk(out.toArray(new MapLocation[0]));
        facing = Direction.WEST;
        for (int i = 1; i <= 150; i++) {
            round++;
            moveCd = Math.max(0, moveCd - 10); turnCd = Math.max(0, turnCd - 10); actCd = Math.max(0, actCd - 10);
            G.startTurn();
            for (MapInfo mi : G.rc.senseNearbyMapInfos()) Sym.record(mi.getMapLocation().x, mi.getMapLocation().y, mi.isWall(), mi.isDirt(), false);
            Trail.record();
            if (Baby.inTransferReach(me, k)) { Baby.trailGap = keep; return i; }
            if (gap < 0 || moveCd < 10) { if (!(trail && Baby.homeLeg(k))) Nav.goTo(k); }
            Flow.work(0);
        }
        Baby.trailGap = keep;
        return -1;
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
        round += (Flow.rev ? Cfg.FLOW_REV_TURNS : Cfg.FLOW_MAX_TURNS) + 1; G.startTurn();
        check(!Flow.searching(t) && !Flow.building, "flow: slow search times out");
    }

    /** A clean BitFlow over the installed map (its row masks follow Sym.record through bfRecord). */
    static void bfReset() {
        BitFlow.init();
        BitFlow.reach = null; BitFlow.tgt = null; BitFlow.building = false; BitFlow.seeded = false; BitFlow.failTgt = null;
        BitFlow.lo = 0; BitFlow.hi = -1; BitFlow.held = 0;
    }

    static void bfRecord(int x, int y, boolean wall, boolean dirtTile) {
        Sym.record(x, y, wall, dirtTile, false);
        BitFlow.mark(x, y, Sym.mem[x + y * W]);
    }

    /** BitFlow's field: Chebyshev distance on open ground, a dirt tile costs DIG_COST, walls are gone round, a walled-off
     *  rat fails. */
    static void testBitFlow() {
        reset(); W = 12; H = 8; install(); bfReset();
        me = new MapLocation(2, 4); G.startTurn();
        MapLocation t = new MapLocation(10, 4);
        BitFlow.request(t);
        BitFlow.work(0);
        check(BitFlow.find(2, 4, 0) == 8 && BitFlow.find(10, 4, 5) == 0 && BitFlow.find(9, 0, 3) == 4,
              "bitflow: open ground is Chebyshev distance (" + BitFlow.find(2, 4, 0) + ")");
        check(BitFlow.building && BitFlow.wave >= 8 + Cfg.BFLOW_EXTRA || !BitFlow.building, "bitflow: grown past the rat");
        // dirt column x=5 beside a wall column x=6 with a gap at y=0: 8 steps, one dig (+DIG_COST - 1)
        reset(); W = 12; H = 8; install(); bfReset();
        for (int y = 0; y < H; y++) { bfRecord(5, y, false, true); if (y > 0) bfRecord(6, y, true, false); }
        me = new MapLocation(2, 4); G.startTurn();
        BitFlow.request(t);
        BitFlow.work(0);
        int d = BitFlow.find(2, 4, 0);
        check(d == 8 + Cfg.DIG_COST - 1, "bitflow: through the gap with one dig (" + d + ")");
        check(BitFlow.find(6, 3, 0) < 0 && BitFlow.find(6, 0, 0) == 4, "bitflow: walls never settle, the gap does");
        // a sealed target fails cleanly and is not retried at once
        reset(); W = 12; H = 8; install(); bfReset();
        for (int y = 0; y < H; y++) bfRecord(6, y, true, false);
        me = new MapLocation(2, 4); G.startTurn();
        int f0 = BitFlow.fails;
        BitFlow.request(t);
        BitFlow.work(0);
        check(!BitFlow.building && BitFlow.fails == f0 + 1, "bitflow: walled-off rat fails");
        BitFlow.request(t);
        check(!BitFlow.building, "bitflow: a failed target is not retried at once");
        // the wave a neighbour sits in, from ours
        reset(); W = 12; H = 8; install(); bfReset();
        me = new MapLocation(2, 4); G.startTurn();
        BitFlow.request(t);
        BitFlow.work(0);
        check(BitFlow.near(3, 4, 8) == 7 && BitFlow.near(1, 4, 8) == -1 && BitFlow.near(2, 5, 8) == 8, "bitflow: neighbour waves");
        check(BitFlow.turns(Direction.NORTH, Direction.SOUTH) == 4 && BitFlow.turns(Direction.NORTHWEST, Direction.NORTHEAST) == 2,
              "bitflow: turns between directions");
    }

    /** One turn of a rat walking a BitFlow field toward t: sense, follow (asking for a field when off one), grow. */
    static void bfTick(MapLocation t) throws GameActionException {
        round++;
        moveCd = Math.max(0, moveCd - 10); turnCd = Math.max(0, turnCd - 10); actCd = Math.max(0, actCd - 10);
        G.startTurn();
        for (MapInfo mi : G.rc.senseNearbyMapInfos()) bfRecord(mi.getMapLocation().x, mi.getMapLocation().y, mi.isWall(), mi.isDirt());
        if (!BitFlow.follow(t)) BitFlow.request(t);
        BitFlow.work(0);
    }

    /** A room sealed by a wall with a dirt plug at one end: the field walk finds the plug as walls come into view
     *  (stale fields regrow) and digs only it. */
    static void testBitFlowWalk() throws GameActionException {
        reset(); W = 30; H = 30; install(); bfReset();
        for (int x = 2; x < W; x++) walls.add(new MapLocation(x, 12));
        dirt.add(new MapLocation(0, 12)); dirt.add(new MapLocation(1, 12));
        me = new MapLocation(15, 5); facing = Direction.SOUTH;
        MapLocation t = new MapLocation(15, 20);
        int steps = 0;
        while (G.cheb(me, t) > 1 && steps < 250) { bfTick(t); steps++; }
        check(G.cheb(me, t) <= 1, "bitflow: leaves a room sealed by a dirt plug (steps=" + steps + ", at " + me + ", digs=" + digsDone + ")");
        check(digsDone <= 2, "bitflow: digs only the plug (" + digsDone + ")");
        check(steps < 120, "bitflow: bounded detour (steps=" + steps + ")");
        // a robot on the only downhill step: the rat waits, then gives the turn to greedy
        reset(); W = 12; H = 3; install(); bfReset();
        for (int x = 0; x < W; x++) { if (x != 6) { walls.add(new MapLocation(x, 0)); walls.add(new MapLocation(x, 2)); } }
        me = new MapLocation(2, 1); facing = Direction.EAST;
        t = new MapLocation(10, 1);
        robots.put(new MapLocation(3, 1), null);
        bfTick(t); bfTick(t);
        check(me.equals(new MapLocation(2, 1)) && BitFlow.held >= 1, "bitflow: holds behind a robot on the path (" + me + ")");
        robots.clear();
        for (int i = 0; i < 12 && G.cheb(me, t) > 1; i++) bfTick(t);
        check(G.cheb(me, t) <= 1, "bitflow: goes on once it moves (" + me + ")");
    }

    /** Mirrored maps and targets grow mirrored fields: every tile's wave equals its rotated twin's. */
    static void testBitFlowMirror() {
        int[][] cells = { {4, 2, 1}, {4, 3, 1}, {4, 4, 1}, {5, 4, 1}, {6, 4, 1}, {8, 6, 2}, {9, 6, 2}, {2, 7, 1}, {3, 7, 2}, {10, 1, 1} };
        int[][] wav = new int[2][];
        for (int k = 0; k < 2; k++) {
            reset(); W = 15; H = 11; install(); bfReset();
            for (int[] c : cells) {
                int x = k == 0 ? c[0] : W - 1 - c[0], y = k == 0 ? c[1] : H - 1 - c[1];
                bfRecord(x, y, c[2] == 1, c[2] == 2);
            }
            me = k == 0 ? new MapLocation(1, 1) : new MapLocation(W - 2, H - 2); G.startTurn();
            MapLocation t = k == 0 ? new MapLocation(12, 8) : new MapLocation(W - 13, H - 9);
            BitFlow.request(t);
            BitFlow.work(0);
            while (BitFlow.building && BitFlow.grow()) { }
            wav[k] = new int[W * H];
            for (int y = 0; y < H; y++)
                for (int x = 0; x < W; x++) {
                    int xx = k == 0 ? x : W - 1 - x, yy = k == 0 ? y : H - 1 - y;
                    wav[k][x + y * W] = (BitFlow.done[yy] >>> xx & 1) != 0 ? BitFlow.find(xx, yy, 0) : -1;
                }
        }
        boolean same = true;
        for (int i = 0; i < W * H; i++) if (wav[0][i] != wav[1][i]) same = false;
        check(same && wav[0][1 + W] > 0, "bitflow: rotated map, rotated field");
    }

    static void testFlowAlloc() {
        reset(); W = 30; H = 20; install();
        int n = W * H;
        Flow.rev = true; Flow.fast = true;
        Flow.start(new MapLocation(20, 10), false);
        // earlier turns got g and one bucket row; par is stale from a smaller map
        Flow.g = new int[n]; Flow.par = null; Flow.bk = new int[Flow.NB][]; Flow.bk[0] = new int[Flow.RING]; Flow.path = null;
        boolean ok = Flow.alloc(n, 0);
        boolean rows = true;
        for (int b = 0; b < Flow.NB; b++) rows &= Flow.bucket != null && Flow.bucket[b] != null && Flow.bucket[b].length == Flow.RING;
        check(ok && rows && Flow.par.length == n && Flow.path.length == 1024 && Flow.AB != null && Flow.pending == 1,
              "flow alloc: the missing pieces are allocated and the search seeded");
        reset(); W = 30; H = 20; install();
        Flow.rev = true;
        Flow.start(new MapLocation(20, 10), false);
        Flow.par = new int[5];
        check(Flow.alloc(n, 0) && Flow.g.length == n && Flow.par.length == n, "flow alloc: a fresh g reallocates the other pieces");
    }

    static MapLocation tw(int x, int y, int sx, int sy) { return new MapLocation(sx > 0 ? x : W - 1 - x, sy > 0 ? y : H - 1 - y); }

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

    static void testCarryAim() throws GameActionException {
        // an enemy rat three tiles north: step north and throw it point-blank in the same turn
        reset(); install();
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, false, false, false);
        RobotInfo e = enemyRat(80, 5, 8, 100);
        robots.put(e.location, e);
        Baby.seen = new RobotInfo[]{e};
        check(Baby.aimCarry() && me.equals(new MapLocation(5, 6)) && thrown == Direction.NORTH,
              "carry aim: step to the point-blank line and throw (" + me + " " + thrown + ")");
        // AIM_LEFT: with the bytecodes at the floor the search stops before any step; no move, no throw
        reset(); install();
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, false, false, false);
        robots.put(e.location, e);
        Baby.seen = new RobotInfo[]{e};
        Baby.aimLeft = Integer.MAX_VALUE;
        check(!Baby.aimCarry() && me.equals(new MapLocation(5, 5)) && thrown == null,
              "carry aim: at the bytecode floor no search (" + me + " " + thrown + ")");
        Baby.aimLeft = Cfg.AIM_LEFT;
        // action not ready: take the aim and keep the rat
        reset(); install();
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, false, false, false);
        robots.put(e.location, e);
        Baby.seen = new RobotInfo[]{e};
        facing = Direction.EAST; actCd = 20;
        check(Baby.aimCarry() && me.equals(new MapLocation(5, 6)) && facing == Direction.NORTH && thrown == null,
              "carry aim: cooling down, it steps and faces the line (" + me + " " + facing + ")");
        // an enemy king's edge on the point-blank tile from here: no step, throw east
        reset(); install();
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, false, false, false);
        RobotInfo k = new RobotInfo(81, G.them, UnitType.RAT_KING, 600, new MapLocation(8, 5), Direction.NORTH, 0, 0, null);
        robots.put(k.location, k);
        Baby.seen = new RobotInfo[]{k};
        check(Baby.aimCarry() && me.equals(new MapLocation(5, 5)) && thrown == Direction.EAST,
              "carry aim: the king from where we stand (" + me + " " + thrown + ")");
        // only an ally or a wall on point-blank tiles: no aim
        reset(); install();
        walls.add(new MapLocation(5, 7));
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, walls.contains(new MapLocation(x, y)), false, false);
        RobotInfo a = new RobotInfo(82, G.us, UnitType.BABY_RAT, 100, new MapLocation(7, 5), Direction.NORTH, 0, 0, null);
        robots.put(a.location, a);
        Baby.seen = new RobotInfo[]{a};
        check(!Baby.aimCarry() && me.equals(new MapLocation(5, 5)) && thrown == null, "carry aim: no enemy in a point-blank line");
        // the first flight tile must be empty: an enemy adjacent north with another behind it is not a line
        reset(); install();
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) Sym.record(x, y, false, false, false);
        RobotInfo e1 = enemyRat(83, 5, 6, 100), e2 = enemyRat(84, 5, 7, 100);
        robots.put(e1.location, e1); robots.put(e2.location, e2);
        Baby.seen = new RobotInfo[]{e1, e2};
        moveCd = 20;
        check(!Baby.aimCarry() && thrown == null, "carry aim: a blocked first tile is no throw");
        Baby.seen = null;
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

    static void testRetreat() throws GameActionException {
        reset(); install();
        Baby.nearestEnemy = enemyRat(50, 5, 7, 100);
        check(!Baby.retreat(), "retreat: a healthy rat does not retreat");
        health = 30;
        Comms.kings[0] = new MapLocation(5, 0); Comms.numKings = 1;
        check(Baby.retreat(), "retreat: a wounded rat near an enemy falls back");
        check(me.y < 5, "retreat: it moved toward its king (at " + me + ")");
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

    static void testSpawnFace() throws GameActionException {
        reset(); install();
        type = UnitType.RAT_KING; G.coop = true; G.round = 2; global = 2000; facing = Direction.SOUTH;
        King.faceSpawn = true;
        King.spawn(new RobotInfo[0], new RobotInfo[0]);
        check(built.size() == 1 && facing == me.directionTo(built.get(0)), "spawn face: the king faces the spawn tile before it builds (" + built + " " + facing + ")");
        King.faceSpawn = Cfg.SYM_FACE;
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

    static void testCatReach() {
        reset(); W = 20; H = 20; install();
        round = 100; me = new MapLocation(5, 3); G.startTurn();
        int saved = Baby.reachTurns;
        MapLocation c = new MapLocation(5, 6);
        Baby.reachTurns = 0; Baby.catBanUntil = -1; Nav.stall = 50;
        Baby.catStalled(c);
        check(Baby.catReach() && Baby.catBanUntil == -1, "cat reach: off, a stalled walk keeps the cat");
        Baby.reachTurns = 30; Nav.stall = 29;
        Baby.catStalled(c);
        check(Baby.catReach(), "cat reach: a short stall keeps the cat");
        Nav.stall = 30;
        Baby.catStalled(c);
        check(!Baby.catReach() && Baby.catBanUntil == 100 + Cfg.CAT_REACH_TTL, "cat reach: a long stall closes the cat branches");
        round = 100 + Cfg.CAT_REACH_TTL; G.startTurn();
        check(Baby.catReach(), "cat reach: the ban runs out");
        Nav.stall = 0; Flow.failTgt = new MapLocation(5, 7); Flow.failRound = G.round;
        Baby.catStalled(c);
        check(!Baby.catReach(), "cat reach: a failed search toward the cat closes the cat branches");
        Flow.failTgt = null; Baby.catBanUntil = -1;
        Flow.lastTgt = c; Flow.lastLen = 9;
        Baby.catStalled(c);
        check(Baby.catReach(), "cat reach: a short path keeps the cat");
        Flow.lastLen = 10;
        Baby.catStalled(c);
        check(!Baby.catReach(), "cat reach: a detour closes the cat branches");
        Flow.lastTgt = null; Baby.catBanUntil = -1; Baby.reachTurns = saved; Nav.stall = 0;
    }

    static void testKingFront() {
        // dirtpassageway (50x50, x flipped): kings (3,26) and (46,26); the centre mine (20,26) has front value 20
        reset(); W = 50; H = 50; install();
        Sym.mask = Sym.VER;
        Comms.numMines = 0; Comms.numKings = 0;
        Comms.mines[Comms.numMines++] = new MapLocation(20, 26);
        MapLocation s = new MapLocation(3, 26);
        int saved = King.kingFront;
        King.kingFront = 0;
        check(new MapLocation(20, 26).equals(King.pickHomeMine(s)), "king front: off, the centre mine is ours");
        King.kingFront = 25;
        check(King.pickHomeMine(s) == null, "king front: 25 skips the centre mine (front 20)");
        King.kingFront = 20;
        check(new MapLocation(20, 26).equals(King.pickHomeMine(s)), "king front: 20 keeps it");
        Comms.mines[Comms.numMines++] = new MapLocation(7, 39);   // d2 185, front 81
        King.kingFront = 25;
        check(new MapLocation(7, 39).equals(King.pickHomeMine(s)), "king front: falls back to a mine behind the line");
        King.kingFront = saved;
    }

    static void testTrapTile() throws GameActionException {
        reset(); W = 30; H = 30; install();
        MapLocation e = new MapLocation(5, 7);
        // facing N from (5,5): front tiles (4,6), (5,6), (6,6); the enemy two ahead
        check(new MapLocation(5, 6).equals(Baby.trapTile(me, facing, e, 4, false)), "trap: the front tile nearest the enemy");
        check(new MapLocation(5, 6).equals(Baby.trapTile(me, facing, e, 4, true)), "trap adj: the front tile inside the enemy's 3x3, nearest");
        // an adjacent enemy at (6,6): the old rule needs a tile nearer it than we are; adj takes (5,6) (dist2 1)
        e = new MapLocation(6, 6);
        robots.put(e, new RobotInfo(9, Team.B, UnitType.BABY_RAT, 100, e, Direction.SOUTH, 0, 0, null));
        check(new MapLocation(5, 6).equals(Baby.trapTile(me, facing, e, 2, true)), "trap adj: next to an adjacent enemy, not on it");
        MapLocation ally = new MapLocation(5, 6);
        robots.put(ally, new RobotInfo(2, Team.A, UnitType.BABY_RAT, 100, ally, Direction.NORTH, 0, 0, null));
        check(Baby.trapTile(me, facing, e, 2, true) == null, "trap adj: (4,6) lies outside the enemy's 3x3");
        robots.clear();
        check(Baby.trapTile(me, facing, new MapLocation(5, 9), 16, true) == null, "trap adj: no front tile within 1 of a far enemy");
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
        // audit K5: every twin is listed (no own-half test; the fix kept that and made a full list report nothing)
        check(Comms.numMines == 4 && Comms.hasMine(4, 9) && Comms.hasMine(23, 25), "mine mirror: lists every twin");
        King.mirrorInto(home, Sym.ROT);
        check(Comms.numMines == 4, "mine mirror: no duplicates");
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

    /** The engine's location order (GameWorld.getAllLocationsWithinRadiusSquared): x outer, y inner, x reversed for
     *  chirality 1 on VERTICAL/ROTATIONAL maps and y reversed on HORIZONTAL/ROTATIONAL ones, clipped to x0..x1, y0..y1. */
    static MapLocation[] engineOrder(int x0, int x1, int y0, int y1, boolean rx, boolean ry) {
        MapLocation[] out = new MapLocation[(x1 - x0 + 1) * (y1 - y0 + 1)];
        int n = 0;
        for (int i = 0; i <= x1 - x0; i++)
            for (int j = 0; j <= y1 - y0; j++) out[n++] = new MapLocation(rx ? x1 - i : x0 + i, ry ? y1 - j : y0 + j);
        return out;
    }

    static void testFullLoad() {
        check(!Baby.fullLoad(0, false, 40) && !Baby.fullLoad(0, true, 40), "full load: nothing carried, no trip home");
        check(!Baby.fullLoad(39, false, 40) && !Baby.fullLoad(10, false, 40), "full load: a part load stays out");
        check(Baby.fullLoad(40, false, 40) && Baby.fullLoad(65, false, 40), "full load: at the load or more, home");
        check(Baby.fullLoad(10, true, 40), "full load: late, any load goes home");
        check(Cfg.LOAD_AT >= 0 && Baby.loadAt == Cfg.LOAD_AT, "full load: switch non-negative, the test field mirrors it");
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

    static void testKingPile() throws GameActionException {
        // the fullofsecrets pocket: a pile 4 south behind a wall row; the only open steps keep the distance
        reset(); W = 30; H = 30; install(); type = UnitType.RAT_KING;
        MapLocation c = new MapLocation(5, 5), pile = new MapLocation(5, 1);
        for (int x = 3; x <= 7; x++) walls.add(new MapLocation(x, 4));
        me = c; G.me = c; King.strict = false;
        check(King.walkStep(c, pile) && !me.equals(c) && G.cheb(me, pile) == G.cheb(c, pile), "king pile: a sidestep keeps the distance (" + me + ")");
        reset(); W = 30; H = 30; install(); type = UnitType.RAT_KING;
        for (int x = 3; x <= 7; x++) walls.add(new MapLocation(x, 4));
        me = c; G.me = c; King.strict = true;
        check(!King.walkStep(c, pile) && me.equals(c), "king pile: strict, no step that is not closer (" + me + ")");
        walls.clear();
        check(King.walkStep(c, pile) && me.distanceSquaredTo(pile) < c.distanceSquaredTo(pile), "king pile: strict, an open step closer (" + me + ")");
        King.strict = Cfg.KING_STRICT;
    }

    static void testBirthSeed() {
        // the same for a rat and its mirror twin under each symmetry (60x40 map), different for another offset or round
        MapLocation me = new MapLocation(12, 7), home = new MapLocation(10, 8);
        int s = Baby.birthSeed(33, me, home);
        boolean ok = true;
        for (int sym = 0; sym < 3; sym++) {
            MapLocation m = sym == 0 ? new MapLocation(59 - me.x, 39 - me.y) : sym == 1 ? new MapLocation(59 - me.x, me.y) : new MapLocation(me.x, 39 - me.y);
            MapLocation h = sym == 0 ? new MapLocation(59 - home.x, 39 - home.y) : sym == 1 ? new MapLocation(59 - home.x, home.y) : new MapLocation(home.x, 39 - home.y);
            if (Baby.birthSeed(33, m, h) != s) ok = false;
        }
        check(ok, "birth seed: mirror twins share it");
        check(Baby.birthSeed(33, new MapLocation(11, 7), home) != s && Baby.birthSeed(34, me, home) != s,
              "birth seed: another offset or round differs");
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

    static void testScoutUntil() {
        reset(); W = 60; H = 60; install();
        check(Baby.scoutUntil(6) == 600 && Baby.scoutUntil(0) == Cfg.SCOUT_UNTIL, "scout area: 60x60 scouts to r600; off = SCOUT_UNTIL");
        reset(); W = 30; H = 30; install();
        check(Baby.scoutUntil(6) == Math.max(Cfg.SCOUT_UNTIL, 150), "scout area: a small map keeps SCOUT_UNTIL");
    }

    static void testRingArcs() {
        reset(); W = 30; H = 30; install();
        for (int i = 0; i < W * H; i++) Sym.mem[i] = Sym.SEEN;
        check(King.ringArcs(new MapLocation(15, 15)) == 1, "ring arcs: open ground is one arc");
        check(King.ringArcs(new MapLocation(15, 1)) == 1 && King.ringArcs(new MapLocation(1, 1)) == 1, "ring arcs: a map edge or corner closes one run");
        // rose (1796): a pocket whose only exit is x=11, y 0-1, past walls at (11,2), (10..12,3)
        int[][] walls = {{11, 2}, {10, 3}, {11, 3}, {12, 3}, {10, 1}, {10, 0}};
        for (int[] w : walls) Sym.mem[G.idx(w[0], w[1])] |= Sym.WALL;
        check(King.ringArcs(new MapLocation(13, 1)) == 2, "ring arcs: a king at the pocket's exit plugs it");
        check(King.ringArcs(new MapLocation(16, 1)) == 1, "ring arcs: the king's start further in does not");
        // a closed corner between two open ring tiles is cut diagonally
        reset(); W = 30; H = 30; install();
        for (int i = 0; i < W * H; i++) Sym.mem[i] = Sym.SEEN;
        Sym.mem[G.idx(13, 13)] |= Sym.WALL; Sym.mem[G.idx(17, 17)] |= Sym.DIRT;
        check(King.ringArcs(new MapLocation(15, 15)) == 1, "ring arcs: closed corners alone do not split the ring");
        // a 3-wide corridor along x: walls above and below the king's body
        for (int x = 0; x < W; x++) { Sym.mem[G.idx(x, 13)] |= Sym.WALL; Sym.mem[G.idx(x, 17)] |= Sym.WALL; }
        check(King.ringArcs(new MapLocation(15, 15)) == 2, "ring arcs: a king filling a corridor plugs it");
        // mirror: the same count for the mirrored position of the rose pocket
        reset(); W = 30; H = 30; install();
        for (int i = 0; i < W * H; i++) Sym.mem[i] = Sym.SEEN;
        for (int[] w : walls) Sym.mem[G.idx(W - 1 - w[0], H - 1 - w[1])] |= Sym.WALL;
        check(King.ringArcs(new MapLocation(W - 1 - 13, H - 1 - 1)) == 2 && King.ringArcs(new MapLocation(W - 1 - 16, H - 1 - 1)) == 1,
              "ring arcs: the rotated pocket counts the same");
    }

    static void testRingArcsFast() {
        // arcsOf against ringArcs's loops on every 16-bit closed mask
        boolean ok = true;
        for (int m = 0; m < 65536 && ok; m++) {
            int cm = m;
            for (int i = 0; i < 16; i += 4)
                if ((cm >> i & 1) != 0 && (cm >> ((i + 1) & 15) & 1) == 0 && (cm >> ((i + 15) & 15) & 1) == 0) cm &= ~(1 << i);
            int want = 0;
            if (cm == 0) want = 1;
            else for (int i = 0; i < 16; i++) if ((cm >> i & 1) == 0 && (cm >> ((i + 15) & 15) & 1) != 0) want++;
            ok = King.arcsOf(m) == want;
        }
        check(ok, "ring arcs fast: the bit corner rule and arc count match the loops on all 65536 masks");
        // ringArcsFast against ringArcs on random walls and dirt, a map that is not square, every tile (edges included)
        java.util.Random r = new java.util.Random(26);
        ok = true;
        for (int t = 0; t < 6 && ok; t++) {
            reset(); W = 23 + t; H = 31 - t; install();
            int dens = 10 + 12 * t;
            for (int i = 0; i < W * H; i++) {
                int q = r.nextInt(100);
                Sym.mem[i] = Sym.SEEN | (q < dens / 2 ? Sym.WALL : q < dens ? Sym.DIRT : 0) | (r.nextInt(9) == 0 ? Sym.MINE : 0);
            }
            for (int y = 0; y < H && ok; y++)
                for (int x = 0; x < W && ok; x++) {
                    MapLocation c = new MapLocation(x, y);
                    ok = King.ringArcsFast(c) == King.ringArcs(c);
                }
        }
        check(ok, "ring arcs fast: the unrolled ring matches ringArcs on random maps, edges and corners included");
        // the king's own count is kept for a tile and round, and counted again on a new round
        reset(); W = 30; H = 30; install();
        for (int i = 0; i < W * H; i++) Sym.mem[i] = Sym.SEEN;
        MapLocation c = new MapLocation(15, 15);
        G.round = 300; King.arcsRound = -1;
        int open = King.arcsHere(c);
        for (int x = 0; x < W; x++) { Sym.mem[G.idx(x, 13)] |= Sym.WALL; Sym.mem[G.idx(x, 17)] |= Sym.WALL; }
        int same = King.arcsHere(c);
        G.round = 301;
        check(open == 1 && same == 1 && King.arcsHere(c) == 2, "ring arcs fast: the king's own count is kept within a round, renewed the next");
    }

    static void testTrapRiskNear() {
        boolean ok = true;
        for (int dy = -2; dy <= 2; dy++)
            for (int dx = -2; dx <= 2; dx++) {
                int b = 0;
                for (int i = 0; i < 8; i++)
                    if (Math.abs(G.DIRS[i].dx - dx) <= 1 && Math.abs(G.DIRS[i].dy - dy) <= 1) b |= 1 << i;
                ok &= TrapRisk.NEAR[(dx + 2) + 5 * (dy + 2)] == b;
            }
        check(ok && TrapRisk.NEAR.length == 25, "TrapRisk.NEAR literal = the neighbours within Chebyshev 1 of each 5x5 offset");
    }

    static void testKingPath() {
        reset(); W = 30; H = 20; install();
        int c0 = Comms.numKings; Comms.numKings = 0;
        KPath.kb = null; KPath.g = null; KPath.poolV = null; KPath.poolN = null;
        KPath.ox = KPath.DX; KPath.oy = KPath.DY; KPath.lastScan = -1;
        check(KPath.alloc(0), "king path: arrays allocated");
        for (int y = 0; y < 15; y++) Sym.record(10, y, true, false, false);
        for (int x = 0; x < W; x += 13) for (int y = 0; y < H; y += 13) KPath.scan(new MapLocation(x, y), 0);
        check(!KPath.open(9, 5) && !KPath.open(11, 14) && KPath.open(10, 16) && !KPath.open(0, 5),
              "king path: centres touching the wall or the edge are blocked");
        MapLocation k = new MapLocation(4, 5), a = new MapLocation(14, 5), b = new MapLocation(4, 17);
        MapLocation[] ms = { a, b };
        MapLocation home = k, away = new MapLocation(100, 100);   // far off: KP_FRONT's line clears the whole map
        check(KPath.start(k, ms, 2, home, away), "king path: seeds from both mines");
        KPath.work(0);
        check(KPath.ready && KPath.cost(k) == 11, "king path: the search reaches the king, 11 steps to mine B (" + KPath.cost(k) + ")");
        MapLocation c = k;
        int n = 0;
        while (KPath.cost(c) > 0 && n < 40) { Direction[] ds = KPath.downhill(c); if (ds == null) break; c = c.add(ds[0]); n++; }
        check(KPath.cost(c) == 0 && G.cheb(c, b) <= 1 && n == 11, "king path: downhill walks to B's seat in 11 steps (" + c + ", " + n + ")");
        // without B the path to A goes round the wall top
        check(KPath.start(k, new MapLocation[]{ a }, 1, home, away), "king path: seeds from A alone");
        KPath.work(0);
        int ca = KPath.cost(k);
        // (4,5) to (8,15) 10, over the top (9,16) (10,16) (11,16) (12,15) 4, down to the seat (13,6) 9
        check(KPath.ready && ca == 23, "king path: A is 23 steps round the wall's top (" + ca + ")");
        // a wall seen across the way after the search: the downhill steps avoid it
        Direction[] d0 = KPath.downhill(k);
        MapLocation nxt = k.add(d0[0]);
        Sym.record(nxt.x, nxt.y + 1, true, false, false);
        KPath.lastScan = -1; KPath.scan(k, 0);
        Direction[] d1 = KPath.downhill(k);
        check(d1 == null || !k.add(d1[0]).equals(nxt), "king path: a newly seen wall blocks the old next step");
        // a mine on the enemy's half, or farther than KP_MAX, seeds nothing
        check(!KPath.start(k, new MapLocation[]{ new MapLocation(24, 14) }, 1, home, new MapLocation(25, 14)),
              "king path: no seat on the enemy's half");
        // short of bytecodes: a scan stops before a row and leaves lastScan unset; seeding gives up without a search
        clock(100); KPath.lastScan = -1;
        check(!KPath.scan(k, 2500) && KPath.lastScan == -1, "king path: a scan short of bytecodes stops and scans again");
        check(!KPath.start(k, ms, 2, home, away) && !KPath.building && KPath.minesAt == -1,
              "king path: seeding short of bytecodes searches nothing and is due again");
        clock(1 << 30);
        // KP_FRONT's line: 2 * |home - away| times the signed distance from the bisector, |home - away|^2 at home
        KPath.frontPct = Cfg.KP_FRONT; KPath.front(new MapLocation(1, 1), new MapLocation(29, 1));
        check(KPath.fa * 1 + KPath.fb * 1 + KPath.fc == 28 * 28 && KPath.fa * 15 + KPath.fb * 7 + KPath.fc == 0
              && KPath.fa * 9 + KPath.fb * 20 + KPath.fc == 2 * 28 * 6 && KPath.fMin == 28 * 28 * Cfg.KP_FRONT / 100,
              "king path: the front line is the bisector, scaled by twice the start gap");
        KPath.fMin = Integer.MIN_VALUE;
        // the turn that allocates the last array does nothing else
        KPath.poolN = null; KPath.lastScan = -1;
        KPath.tick();
        check(KPath.poolN != null && KPath.lastScan == -1, "king path: the allocating turn neither scans nor searches");
        reset(); W = 60; H = 20; install();
        KPath.kb = null; KPath.g = null; KPath.poolV = null; KPath.poolN = null; KPath.lastScan = -1;
        KPath.alloc(0);
        check(!KPath.start(new MapLocation(2, 5), new MapLocation[]{ new MapLocation(2 + Cfg.KP_MAX + 4, 5) }, 1,
                           new MapLocation(2, 5), new MapLocation(57, 5)), "king path: a mine beyond KP_MAX seeds nothing");
        Comms.numKings = c0;
    }

    static void testHomeRank() {
        check(Baby.homeRank(0, 3) < Baby.homeRank(0, 9) && Baby.homeRank(1, 3) < Baby.homeRank(1, 9), "home pick 0/1: nearer ranks first");
        check(Baby.homeRank(2, 9) < Baby.homeRank(2, 3), "home pick 2: farther ranks first");
    }




    public static void main(String[] args) throws Exception {
        testFlowProgress();
        testFlowCache();
        testFaceReach();
        testFlowRev();
        testFlowFast();
        testTrapRiskNear();
        testCarryAim();
        testSpawnFace();
        testFrontier();
        testHomeStart();
        testCatLay();
        testAtkCarry();
        testCatLane();
        testRingArcs();
        testRingArcsFast();
        testHomeRank();
        testKingPath();
        testScoutUntil();
        testScout();
        testSeek();
        testMineMirror();
        testKingDig();
        testKingPile();
        testFullLoad();
        testWalledOff();
        testNearKing();
        testFormIncome();
        testCatAtKing();
        testNearerAllies();
        testBirthSeed();
        testDefend();
        testBiteCheese();
        testFlank();
        testSpawnFloor();
        testRetreat();
        testThrowDirection();
        testBiteTargets();
        testTransferReach();
        testRaid();
        testTrapTile();
        testKingFront();
        testCatReach();
        testSymElimination();
        testStallCentre();
        testSqueakCodec();
        testFresh();
        testCatGeometry();
        testNavAroundWall();
        testNavDigsDirt();
        testNavUShape();
        testNavFlowSealedRoom();
        testFlowField();
        testFlowAlloc();
        testBitFlow();
        testBitFlowWalk();
        testBitFlowMirror();
        testNavSameTarget();
        testAtMine();
        testTrail();
        testExploreUnseen();
        testKingNeverWritesOutOfRange();
        System.out.println("BotTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
