package bc26tools;

import battlecode.world.*;
import java.io.File;

/** Write copies of maps with a different random seed (cheese spawns, robot ids and other engine randomness derive
 *  from the map seed). Seed 0 means "keep the map's own seed".
 *  Usage: MapReseed SRC_MAPDIR DST_MAPDIR SEED MAP... */
public class MapReseed {
    public static void main(String[] a) throws Exception {
        File src = new File(a[0]), dst = new File(a[1]);
        int seed = Integer.parseInt(a[2]);
        dst.mkdirs();
        for (int i = 3; i < a.length; i++) {
            LiveMap m = GameMapIO.loadMap(a[i], src, false);
            int s = seed == 0 ? m.getSeed() : seed;
            LiveMap n = new LiveMap(m.getWidth(), m.getHeight(), m.getOrigin(), s, m.getRounds(), m.getMapName(),
                    m.getSymmetry(), m.getWallArray(), m.getDirtArray(), m.getCheeseMineArray(), m.getCheeseArray(),
                    m.getCatWaypointIDs(), m.getCatWaypoints(), m.getInitialBodies());
            GameMapIO.writeMap(n, dst);
        }
    }
}
