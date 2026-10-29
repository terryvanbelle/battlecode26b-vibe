package ratbot;

import battlecode.common.*;
import battlecode.world.*;
import java.io.File;
import java.util.*;

/**
 * Offline flow-search benchmark (not a unit test): on each map, the gatherer trips of team A (each king to each mine
 * and back) searched over full terrain knowledge with unlimited bytecode. Per map: searches, tiles reached (g set in
 * the search's generation; the cost driver), and path tiles. usage: FlowBench MAPDIR [MAP...]
 */
public class FlowBench {
    public static void main(String[] a) throws Exception {
        Flow.lifo = Boolean.getBoolean("lifo");
        Flow.rev = Boolean.getBoolean("rev");
        File dir = new File(a[0]);
        List<String> names = a.length > 1 ? Arrays.asList(a).subList(1, a.length) : GameMapIO.getAvailableMaps(dir);
        long allS = 0, allR = 0, allL = 0;
        for (String n : names) {
            LiveMap m = GameMapIO.loadMap(n, dir, false);
            int W = m.getWidth(), H = m.getHeight();
            boolean[] wall = m.getWallArray(), dirt = m.getDirtArray(), mine = m.getCheeseMineArray();
            BotTest.reset();
            BotTest.W = W; BotTest.H = H;
            for (int i = 0; i < W * H; i++) {
                MapLocation l = m.indexToLocation(i);
                if (wall[i]) BotTest.walls.add(l);
                if (dirt[i]) BotTest.dirt.add(l);
            }
            BotTest.install();
            for (int i = 0; i < W * H; i++) {
                MapLocation l = m.indexToLocation(i);
                Sym.record(l.x, l.y, wall[i], dirt[i], false);
            }
            List<MapLocation> kings = new ArrayList<>(), mines = new ArrayList<>();
            for (RobotInfo r : m.getInitialBodies()) if (r.getTeam() == Team.A && r.getType() == UnitType.RAT_KING) kings.add(r.getLocation());
            for (int i = 0; i < W * H; i++) if (mine[i]) mines.add(m.indexToLocation(i));
            long s = 0, reached = 0, len = 0;
            for (MapLocation k : kings)
                for (MapLocation c : mines)
                    for (int dirn = 0; dirn < 2; dirn++) {
                        MapLocation from = dirn == 0 ? k : c, to = dirn == 0 ? c : k;
                        BotTest.me = from;
                        G.startTurn();
                        Flow.drop();
                        Flow.failTgt = null;
                        Flow.request(to);
                        Flow.work(0);
                        if (!Flow.ready(to)) continue;
                        int r = 0;
                        for (int i = 0; i < W * H; i++) if (Flow.g[i] >= Flow.base) r++;
                        s++; reached += r; len += Flow.plen;
                    }
            allS += s; allR += reached; allL += len;
            System.out.printf("%-22s %3dx%-3d searches %3d  reached/search %6.1f  path %5.1f%n", n, W, H, s,
                              s > 0 ? (double) reached / s : 0, s > 0 ? (double) len / s : 0);
        }
        System.out.printf("ALL searches %d  reached/search %.1f  path %.1f%n", allS, (double) allR / allS, (double) allL / allS);
    }
}
