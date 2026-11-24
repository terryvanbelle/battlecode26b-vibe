package ratbot;

import battlecode.common.*;
import static ratbot.BotTest.*;

/**
 * sw65's king dirt block (Cfg.KING_DIRT_R, 11-23): an enemy baby in view carrying a rat, 3 to KING_DIRT_R tiles from
 * our centre, whose straight throw would hit us, gets a tile of dirt on the free ring tile its flight crosses just
 * before us (sw63's flight geometry; engine: a thrown rat stops at the first robot or impassable tile, dirt included;
 * placing dirt costs the team one dug dirt and DIG_COOLDOWN 25, no cheese). Our king stands at (15,15) on a 30x30
 * map, at war, with 3 dirt in the team's stock.
 */
public class DirtBlockTest {
    static final MapLocation C = new MapLocation(15, 15);

    static void setup() throws GameActionException {
        reset(); W = 30; H = 30; type = UnitType.RAT_KING; me = C; facing = Direction.NORTH;
        install(); G.coop = false; G.round = 500; King.blocks = 0; King.blockDir = null; dirtStock = 3;
    }

    /** An enemy baby at (x,y) facing f, carrying one of ours (put in view). */
    static RobotInfo carrier(int id, int x, int y, Direction f) {
        RobotInfo ours = new RobotInfo(id + 500, Team.A, UnitType.BABY_RAT, 40, new MapLocation(x, y), Direction.NORTH, 0, 0, null);
        RobotInfo e = new RobotInfo(id, Team.B, UnitType.BABY_RAT, 100, new MapLocation(x, y), f, 0, 0, ours);
        robots.put(e.location, e);
        return e;
    }

    /** t lies on the flight from e toward d, on our ring, with our 3x3 the next tile. */
    static boolean onFlight(RobotInfo e, Direction d, MapLocation t) {
        if (t == null || d == null || G.cheb(t, C) != 2 || G.cheb(t.add(d), C) > 1) return false;
        MapLocation p = e.location;
        for (int k = 0; k < 8; k++) { p = p.add(d); if (p.equals(t)) return true; }
        return false;
    }

    public static void main(String[] args) throws Exception {
        check(Cfg.KING_DIRT_R == 6, "dirt block: KING_DIRT_R 6 (" + Cfg.KING_DIRT_R + ")");
        check(Debug.KWHY.indexOf('B') == 7, "dirt block: KWHY 'B' is code 7 (block_check.py counts it) (" + Debug.KWHY + ")");

        // a carrier 4 tiles east facing us: dirt on (17,15), the tile its flight crosses just before our 3x3
        setup();
        RobotInfo e = carrier(1, 19, 15, Direction.WEST);
        check(new MapLocation(17, 15).equals(King.blockTile(new RobotInfo[]{e})) && King.blockDir == Direction.WEST,
              "dirt block: a carrier facing us is blocked at (17,15) (" + King.blockTile(new RobotInfo[]{e}) + " " + King.blockDir + ")");
        int cheese = global;
        check(King.dirtBlock(new RobotInfo[]{e}), "dirt block: the king places the dirt");
        check(dirt.contains(new MapLocation(17, 15)) && dirtStock == 2 && King.blocks == 1 && global == cheese && actCd >= 25,
              "dirt block: dirt on (17,15), one dirt spent, no cheese, the action cooldown 25 (" + dirt + " stock " + dirtStock + " cd " + actCd + ")");
        check((Sym.mem[17 + W * 15] & Sym.DIRT) != 0, "dirt block: the king's map records the dirt");
        check(!King.dirtBlock(new RobotInfo[]{e}) && dirtStock == 2, "dirt block: the action is spent: no second dirt this turn");
        // with the dirt in place the line is shut: no further block on it
        actCd = 0;
        check(King.blockTile(new RobotInfo[]{e}) == null || !King.blockTile(new RobotInfo[]{e}).equals(new MapLocation(17, 15)),
              "dirt block: the shut line needs no second dirt (" + King.blockTile(new RobotInfo[]{e}) + ")");

        // facing away: the flight toward us is still a threat (a carrier turns and throws in one turn)
        setup();
        e = carrier(1, 19, 15, Direction.NORTH);
        check(new MapLocation(17, 15).equals(King.blockTile(new RobotInfo[]{e})), "dirt block: a carrier facing away is blocked on its way to us (" + King.blockTile(new RobotInfo[]{e}) + ")");

        // a diagonal carrier: the flight enters our corner past the ring corner (17,17), within the king's dist2 8
        setup();
        e = carrier(1, 19, 19, Direction.SOUTHWEST);
        check(new MapLocation(17, 17).equals(King.blockTile(new RobotInfo[]{e})) && King.dirtBlock(new RobotInfo[]{e}) && dirt.contains(new MapLocation(17, 17)),
              "dirt block: a diagonal flight is blocked with dirt at the ring corner (" + dirt + ")");

        // off the axes, facing away: some flight toward us is blocked, on our ring, the 3x3 next
        setup();
        e = carrier(1, 19, 17, Direction.NORTH);
        MapLocation t = King.blockTile(new RobotInfo[]{e});
        check(onFlight(e, King.blockDir, t), "dirt block: an off-axis carrier's flight is blocked on the ring (" + t + " " + King.blockDir + ")");

        // a wall on the flight stops it first: no threat on that line, and the side lines miss us
        setup();
        walls.add(new MapLocation(18, 15));
        e = carrier(1, 19, 15, Direction.WEST);
        check(King.blockTile(new RobotInfo[]{e}) == null && !King.dirtBlock(new RobotInfo[]{e}) && dirt.isEmpty() && dirtStock == 3,
              "dirt block: a walled flight needs no dirt");

        // one of ours already on the line takes the hit: no dirt
        setup();
        robots.put(new MapLocation(17, 15), new RobotInfo(9, Team.A, UnitType.BABY_RAT, 100, new MapLocation(17, 15), Direction.EAST, 0, 0, null));
        e = carrier(1, 19, 15, Direction.WEST);
        check(King.blockTile(new RobotInfo[]{e}) == null, "dirt block: a line already held by one of ours needs no dirt");

        // too close (on our ring: no tile between), too far (7 tiles), or carrying nothing: no block
        setup();
        e = carrier(1, 17, 15, Direction.WEST);
        check(King.blockTile(new RobotInfo[]{e}) == null, "dirt block: a carrier on our ring cannot be blocked");
        setup();
        e = carrier(1, 22, 15, Direction.WEST);
        check(King.blockTile(new RobotInfo[]{e}) == null, "dirt block: a carrier 7 tiles out is beyond KING_DIRT_R");
        setup();
        RobotInfo free = enemyRat(1, 19, 15, 100);
        robots.put(free.location, free);
        check(King.blockTile(new RobotInfo[]{free}) == null && !King.dirtBlock(new RobotInfo[]{free}), "dirt block: an enemy carrying nothing is no throw threat");

        // two carriers: the nearer one is blocked
        setup();
        RobotInfo near = carrier(1, 19, 15, Direction.WEST), far = carrier(2, 15, 20, Direction.SOUTH);
        check(new MapLocation(17, 15).equals(King.blockTile(new RobotInfo[]{far, near})), "dirt block: the nearer carrier is blocked first (" + King.blockTile(new RobotInfo[]{far, near}) + ")");
        setup();
        far = carrier(2, 15, 20, Direction.SOUTH);
        check(new MapLocation(15, 17).equals(King.blockTile(new RobotInfo[]{far})), "dirt block: a carrier 5 tiles north is blocked at (15,17) (" + King.blockTile(new RobotInfo[]{far}) + ")");

        // no dirt in the team's stock: nothing placed (the king spawns as before)
        setup();
        dirtStock = 0;
        e = carrier(1, 19, 15, Direction.WEST);
        check(!King.dirtBlock(new RobotInfo[]{e}) && dirt.isEmpty() && King.blocks == 0, "dirt block: no dirt in stock, no block");

        System.out.println("DirtBlockTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
