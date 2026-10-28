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
    static Map<MapLocation, RobotInfo> robots = new HashMap<>();
    static int[] shared = new int[64];
    static MapLocation me = new MapLocation(5, 5);
    static Direction facing = Direction.NORTH;
    static int round = 1, global = 1000, raw = 0, moveCd = 0, turnCd = 0, actCd = 0;
    static UnitType type = UnitType.BABY_RAT;
    static List<Integer> squeaks = new ArrayList<>();
    static int digsDone;

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
        walls.clear(); dirt.clear(); robots.clear(); Arrays.fill(shared, 0); squeaks.clear();
        me = new MapLocation(5, 5); facing = Direction.NORTH; round = 1; global = 1000; raw = 0;
        moveCd = turnCd = actCd = 0; type = UnitType.BABY_RAT; digsDone = 0;
        Sym.mask = Sym.ROT | Sym.HOR | Sym.VER; Sym.contradictions = 0;
        Nav.target = null; Nav.bugging = false; Nav.avoid = 0; Nav.stall = 0; Nav.digs = 0;
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
                    case "senseNearbyRobots": return robots.values().toArray(new RobotInfo[0]);
                    case "squeak": squeaks.add((Integer) a[0]); return true;
                    case "readSqueaks": return new Message[0];
                    case "getCurrentRatCost": return 10;
                    case "setIndicatorString": return null;
                    default: throw new UnsupportedOperationException("fakeRc: " + m.getName());
                }
            });
    }

    static void install() {
        G.init(fakeRc());
        Sym.init();
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
    }

    // ------------------------------------------------------------------ tests

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

    static void testKingNeverWritesOutOfRange() throws GameActionException {
        reset(); install();
        type = UnitType.RAT_KING; me = new MapLocation(59 % W, 19); G.startTurn();
        King.slot = 0;
        King.publish();
        boolean ok = true;
        for (int v : shared) if (v < 0 || v > 1023) ok = false;
        check(ok, "king publish stays in 0..1023");
    }

    public static void main(String[] args) throws Exception {
        testHandMirror();
        testSymElimination();
        testSqueakCodec();
        testFresh();
        testCatGeometry();
        testNavAroundWall();
        testNavDigsDirt();
        testNavUShape();
        testKingNeverWritesOutOfRange();
        System.out.println("BotTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
