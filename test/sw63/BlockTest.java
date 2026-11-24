package ratbot;

import battlecode.common.*;
import static ratbot.BotTest.*;

/**
 * sw63's king block (Cfg.KING_BLOCK, 11-23): an enemy baby in view carrying a rat, 3 to KING_BLOCK_R tiles from our
 * centre, whose straight throw would hit us, gets a newborn on the free ring tile its flight crosses just before us.
 * In the 40 replica losses on the four maps every champion loses to AlexT101_usqualsbot, thrown rats cost our king
 * 297 health a game, 80% of them thrown from 3-5 tiles out. Our king stands at (15,15) on a 30x30 map, at war, with 12 babies (rat cost 40).
 */
public class BlockTest {
    static final MapLocation C = new MapLocation(15, 15);

    static void setup(int bank) throws GameActionException {
        reset(); W = 30; H = 30; type = UnitType.RAT_KING; me = C; facing = Direction.NORTH;
        install(); G.coop = false; G.round = 500; global = bank; King.blocks = 0; King.blockDir = null;
        ratCost = 40; // 12 babies: the war spawn floor (WAR_FLOOR 300) holds, unlike the last stand's at cost 10
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
        check(Cfg.KING_BLOCK == 50 && Cfg.KING_BLOCK_R == 6, "king block: KING_BLOCK 50, KING_BLOCK_R 6 (" + Cfg.KING_BLOCK + ", " + Cfg.KING_BLOCK_R + ")");

        // a carrier 4 tiles east facing us: the flight crosses (17,15) just before our 3x3; the bank (100) is under every
        // other spawn floor, so only the block spawns, and the newborn faces the carrier
        setup(100);
        RobotInfo e = carrier(1, 19, 15, Direction.WEST);
        check(new MapLocation(17, 15).equals(King.blockTile(new RobotInfo[]{e})) && King.blockDir == Direction.WEST, "king block: a carrier facing us is blocked at (17,15) (" + King.blockTile(new RobotInfo[]{e}) + " " + King.blockDir + ")");
        King.spawn(new RobotInfo[0], new RobotInfo[]{e});
        check(built.size() == 1 && built.get(0).equals(new MapLocation(17, 15)) && King.spawnWhy == 'B' && King.blocks == 1,
              "king block: the king spawns on the flight line under the spawn floor (" + built + " " + King.spawnWhy + ")");
        check(facing == Direction.EAST, "king block: the newborn (our facing) faces the carrier (" + facing + ")");

        // facing away: the flight toward us is still a threat (a carrier turns and throws in one turn)
        setup(100);
        e = carrier(1, 19, 15, Direction.NORTH);
        check(new MapLocation(17, 15).equals(King.blockTile(new RobotInfo[]{e})), "king block: a carrier facing away is blocked on its way to us (" + King.blockTile(new RobotInfo[]{e}) + ")");

        // a diagonal carrier: the flight enters our corner past the ring corner (17,17)
        setup(100);
        e = carrier(1, 19, 19, Direction.SOUTHWEST);
        check(new MapLocation(17, 17).equals(King.blockTile(new RobotInfo[]{e})) && King.blockDir == Direction.SOUTHWEST, "king block: a diagonal flight is blocked at the ring corner (" + King.blockTile(new RobotInfo[]{e}) + ")");

        // off the axes, facing away: some flight toward us is blocked, on our ring, the 3x3 next
        setup(100);
        e = carrier(1, 19, 17, Direction.NORTH);
        MapLocation t = King.blockTile(new RobotInfo[]{e});
        check(onFlight(e, King.blockDir, t), "king block: an off-axis carrier's flight is blocked on the ring (" + t + " " + King.blockDir + ")");

        // a wall on the flight stops it first: no threat on that line, and the side lines miss us
        setup(100);
        walls.add(new MapLocation(18, 15));
        e = carrier(1, 19, 15, Direction.WEST);
        check(King.blockTile(new RobotInfo[]{e}) == null, "king block: a walled flight needs no block");
        King.spawn(new RobotInfo[0], new RobotInfo[]{e});
        check(built.isEmpty(), "king block: no block, and the spawn floor still holds (" + built + ")");

        // one of ours already on the line takes the hit: no newborn
        setup(100);
        robots.put(new MapLocation(17, 15), new RobotInfo(9, Team.A, UnitType.BABY_RAT, 100, new MapLocation(17, 15), Direction.EAST, 0, 0, null));
        e = carrier(1, 19, 15, Direction.WEST);
        check(King.blockTile(new RobotInfo[]{e}) == null, "king block: a line already held by one of ours needs no block");

        // too close (on our ring: no tile between), too far (7 tiles), or carrying nothing: no block
        setup(100);
        e = carrier(1, 17, 15, Direction.WEST);
        check(King.blockTile(new RobotInfo[]{e}) == null, "king block: a carrier on our ring cannot be blocked");
        setup(100);
        e = carrier(1, 22, 15, Direction.WEST);
        check(King.blockTile(new RobotInfo[]{e}) == null, "king block: a carrier 7 tiles out is beyond KING_BLOCK_R");
        setup(100);
        RobotInfo free = enemyRat(1, 19, 15, 100);
        robots.put(free.location, free);
        check(King.blockTile(new RobotInfo[]{free}) == null, "king block: an enemy carrying nothing is no throw threat");

        // two carriers: the nearer one is blocked
        setup(100);
        RobotInfo near = carrier(1, 19, 15, Direction.WEST), far = carrier(2, 15, 20, Direction.SOUTH);
        check(new MapLocation(17, 15).equals(King.blockTile(new RobotInfo[]{far, near})), "king block: the nearer carrier is blocked first (" + King.blockTile(new RobotInfo[]{far, near}) + ")");
        setup(100);
        far = carrier(2, 15, 20, Direction.SOUTH);
        check(new MapLocation(15, 17).equals(King.blockTile(new RobotInfo[]{far})), "king block: a carrier 5 tiles north is blocked at (15,17) (" + King.blockTile(new RobotInfo[]{far}) + ")");

        // the bank must keep KING_BLOCK after the rat cost; and none in cooperation
        setup(40 + Cfg.KING_BLOCK - 1);
        e = carrier(1, 19, 15, Direction.WEST);
        King.spawn(new RobotInfo[0], new RobotInfo[]{e});
        check(built.isEmpty(), "king block: a bank under cost + KING_BLOCK keeps its cheese (" + built + ")");
        setup(100);
        G.coop = true;
        e = carrier(1, 19, 15, Direction.WEST);
        King.spawn(new RobotInfo[0], new RobotInfo[]{e});
        check(!built.contains(new MapLocation(17, 15)) && King.blocks == 0, "king block: none in cooperation (" + built + ")");

        System.out.println("BlockTest: " + (checks - fails) + "/" + checks + " passed");
        System.exit(fails == 0 ? 0 : 1);
    }
}
