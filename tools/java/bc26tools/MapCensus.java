package bc26tools;

import battlecode.common.*;
import battlecode.world.*;
import java.io.File;
import java.util.*;

/** Map census: one TSV row per .map26 file (or an ASCII board with --ascii NAME). The dig columns come from team A's
 *  king (0-1 search, walls blocked, a dirt tile costs one dig): freeTiles = tiles reached without digging, minesFree =
 *  mines among them, digNear = fewest digs to any mine, digHome = fewest digs to a mine nearer king A than king B.
 *  featDiff = per wrong symmetry (R rotational, H horizontal: y flipped, V vertical: x flipped) the tiles whose wall,
 *  dirt or mine differs from their twin's, the only sightings that can rule it out (R0 = never); gapAlt = the wrong symmetries whose twin of
 *  king A lies within Chebyshev 2 of it (the kings' 3x3 bodies would overlap: ruled out at round 1). pocket = the open
 *  tiles of the ring around king A's start 3x3 (its spawn tiles) from which, with that 3x3 as wall (dirt open), under half
 *  of freeTiles can be reached, as k/n:S (k pocket tiles of n open, S the largest pocket's size; a trailing * when the
 *  open ring tile nearest the map centre is one of them), or - with none.
 *  Usage: java -cp engine.jar:classes bc26tools.MapCensus MAPDIR [--ascii NAME] */
public class MapCensus {
    public static void main(String[] args) throws Exception {
        File dir = new File(args[0]);
        String ascii = args.length > 2 && args[1].equals("--ascii") ? args[2] : null;
        if (ascii == null)
            System.out.println("map\tw\th\tsym\tkingA\tkingB\tkingDist2\tcats\twallPct\tdirtPct\tmines\tcheese0\tseed\twaypoints\tfreeTiles\tminesFree\tdigNear\tdigHome\tfeatDiff\tgapAlt\tpocket");
        File[] files = dir.listFiles((d, n) -> n.endsWith(".map26"));
        Arrays.sort(files);
        for (File f : files) {
            String name = f.getName().replace(".map26", "");
            if (ascii != null && !ascii.equals(name)) continue;
            LiveMap m = GameMapIO.loadMap(name, dir, false);
            int w = m.getWidth(), h = m.getHeight();
            boolean[] wall = m.getWallArray(), dirt = m.getDirtArray(), mine = m.getCheeseMineArray();
            int[] cheese = m.getCheeseArray();
            int walls = 0, dirts = 0, mines = 0, ch = 0;
            for (int i = 0; i < w * h; i++) { if (wall[i]) walls++; if (dirt[i]) dirts++; if (mine[i]) mines++; ch += cheese[i]; }
            MapLocation ka = null, kb = null; int cats = 0;
            List<MapLocation> catLocs = new ArrayList<>();
            for (RobotInfo r : m.getInitialBodies()) {
                if (r.getType() == UnitType.CAT) { cats++; catLocs.add(r.getLocation()); }
                else if (r.getType() == UnitType.RAT_KING) { if (r.getTeam() == Team.A) ka = r.getLocation(); else kb = r.getLocation(); }
            }
            if (ascii != null) {
                char[][] g = new char[h][w];
                for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
                    int i = y * w + x;
                    g[y][x] = wall[i] ? '#' : mine[i] ? 'M' : dirt[i] ? '%' : cheese[i] > 0 ? 'c' : '.';
                }
                for (RobotInfo r : m.getInitialBodies()) {
                    MapLocation l = r.getLocation();
                    g[l.y][l.x] = r.getType() == UnitType.CAT ? 'C' : r.getTeam() == Team.A ? 'A' : 'B';
                }
                System.out.println(name + " " + w + "x" + h + " " + m.getSymmetry());
                for (int y = h - 1; y >= 0; y--) System.out.println(new String(g[y]));
                continue;
            }
            int wp = 0; for (int[] a : m.getCatWaypoints()) wp += a.length;
            int[] dg = digs(w, h, wall, dirt, ka);
            int free = 0, minesFree = 0, digNear = -1, digHome = -1;
            for (int i = 0; i < w * h; i++) {
                if (dg[i] == 0) free++;
                if (!mine[i] || dg[i] < 0) continue;
                if (dg[i] == 0) minesFree++;
                if (digNear < 0 || dg[i] < digNear) digNear = dg[i];
                MapLocation l = new MapLocation(i % w, i / w);
                if (kb != null && l.distanceSquaredTo(ka) < l.distanceSquaredTo(kb) && (digHome < 0 || dg[i] < digHome)) digHome = dg[i];
            }
            String featDiff = "", gapAlt = "";
            for (char c : "RHV".toCharArray()) {
                if (c == "RHV".charAt(m.getSymmetry().ordinal())) continue;
                int diff = 0;
                for (int i = 0; i < w * h; i++) {
                    int j = twin(c, i % w, i / w, w, h);
                    if (wall[i] != wall[j] || dirt[i] != dirt[j] || mine[i] != mine[j]) diff++;
                }
                featDiff += (featDiff.isEmpty() ? "" : " ") + c + diff;
                if (ka != null) {
                    int j = twin(c, ka.x, ka.y, w, h);
                    if (Math.max(Math.abs(j % w - ka.x), Math.abs(j / w - ka.y)) <= 2) gapAlt += c;
                }
            }
            System.out.printf("%s\t%d\t%d\t%s\t%s\t%s\t%d\t%d\t%.1f\t%.1f\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%s\t%s\t%s%n", name, w, h, m.getSymmetry(),
                    ka == null ? "-" : ka.x + "," + ka.y, kb == null ? "-" : kb.x + "," + kb.y,
                    ka == null || kb == null ? -1 : ka.distanceSquaredTo(kb), cats,
                    100.0 * walls / (w * h), 100.0 * dirts / (w * h), mines, ch, m.getSeed(), wp, free, minesFree, digNear, digHome,
                    featDiff, gapAlt.isEmpty() ? "-" : gapAlt, pocket(w, h, wall, dirt, ka, free));
        }
    }

    static final int[] RX = {-2, -1, 0, 1, 2, 2, 2, 2, 2, 1, 0, -1, -2, -2, -2, -2},
                       RY = {-2, -2, -2, -2, -2, -1, 0, 1, 2, 2, 2, 2, 2, 1, 0, -1};

    /** The pocket column: ring tiles of the 3x3 at k whose region (k's 3x3 walled, dirt open) holds under free / 2 tiles. */
    static String pocket(int w, int h, boolean[] wall, boolean[] dirt, MapLocation k, int free) {
        if (k == null) return "-";
        boolean[] blk = wall.clone();
        for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) {
            int x = k.x + dx, y = k.y + dy;
            if (x >= 0 && y >= 0 && x < w && y < h) blk[y * w + x] = true;
        }
        int open = 0, pk = 0, big = 0, bestD = Integer.MAX_VALUE;
        boolean nearIn = false;
        for (int r = 0; r < 16; r++) {
            int x = k.x + RX[r], y = k.y + RY[r];
            if (x < 0 || y < 0 || x >= w || y >= h || blk[y * w + x] || dirt[y * w + x]) continue;
            open++;
            int n = region(w, h, blk, y * w + x);
            boolean in = 2 * n < free;
            if (in) { pk++; big = Math.max(big, n); }
            int d = (2 * x + 1 - w) * (2 * x + 1 - w) + (2 * y + 1 - h) * (2 * y + 1 - h);
            if (d < bestD) { bestD = d; nearIn = in; }
        }
        return pk == 0 ? "-" : pk + "/" + open + ":" + big + (nearIn ? "*" : "");
    }

    /** Tiles 8-connected to tile s through tiles not in blk. */
    static int region(int w, int h, boolean[] blk, int s) {
        boolean[] seen = new boolean[w * h];
        ArrayDeque<Integer> q = new ArrayDeque<>();
        seen[s] = true; q.add(s);
        int n = 0;
        while (!q.isEmpty()) {
            int i = q.poll(), x = i % w, y = i / w;
            n++;
            for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx, ny = y + dy;
                if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue;
                int j = ny * w + nx;
                if (blk[j] || seen[j]) continue;
                seen[j] = true; q.add(j);
            }
        }
        return n;
    }

    /** Index of the twin of (x, y) under symmetry c (MapSymmetry order R, H, V: H keeps x, V keeps y). */
    static int twin(char c, int x, int y, int w, int h) {
        int tx = c == 'H' ? x : w - 1 - x, ty = c == 'V' ? y : h - 1 - y;
        return ty * w + tx;
    }

    /** Fewest dirt tiles dug to reach each tile from the 3x3 king at k (-1 = walled off or no king). */
    static int[] digs(int w, int h, boolean[] wall, boolean[] dirt, MapLocation k) {
        int[] d = new int[w * h];
        Arrays.fill(d, -1);
        if (k == null) return d;
        ArrayDeque<Integer> q = new ArrayDeque<>();
        for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) {
            int x = k.x + dx, y = k.y + dy;
            if (x >= 0 && y >= 0 && x < w && y < h && !wall[y * w + x]) { d[y * w + x] = 0; q.add(y * w + x); }
        }
        while (!q.isEmpty()) {
            int i = q.pollFirst(), x = i % w, y = i / w;
            for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx, ny = y + dy;
                if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue;
                int j = ny * w + nx;
                if (wall[j]) continue;
                int c = d[i] + (dirt[j] ? 1 : 0);
                if (d[j] >= 0 && d[j] <= c) continue;
                d[j] = c;
                if (dirt[j]) q.addLast(j); else q.addFirst(j);
            }
        }
        return d;
    }
}
