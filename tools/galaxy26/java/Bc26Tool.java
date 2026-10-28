// Replica helper around the pinned Battlecode 2026 engine (compiled against its jar by galaxy-setup.sh runtime).
//   java -cp ENGINE_JAR:DIR Bc26Tool reverse-maps IN_DIR OUT_DIR   every IN_DIR/*.map26, loaded the way the engine
//        loads the odd games of an alternate-order match (GameMapIO.loadMap(name, dir, teamsReversed=true)), written
//        to OUT_DIR under the same name. saturn.py plays such a game as a one-map run on the reversed copy, which is
//        the engine's own alternate-order game (Server.runMatch with teamsReversed).
//   java -cp ENGINE_JAR:DIR Bc26Tool check-maps DIR               every DIR/*.map26 loaded both ways and checked with
//        LiveMap.assertIsValid (what -Dbc.server.validate-maps=true does): one line per map, "OK name" or "BAD name: why";
//        exit 1 if any is bad.
//   java -cp ENGINE_JAR:DIR Bc26Tool replay-info FILE.bc26         one JSON line: teams, and per match the map,
//        winner (A/B), win type and rounds, read with the engine's own flatbuffer classes (battlecode.schema).
// Doc: tools/galaxy26/README.md.
import battlecode.schema.*;
import battlecode.world.GameMapIO;
import battlecode.world.LiveMap;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;

public class Bc26Tool {
    public static void main(String[] args) throws Exception {
        if (args.length == 3 && args[0].equals("reverse-maps")) {
            File in = new File(args[1]), out = new File(args[2]);
            out.mkdirs();
            File[] files = in.listFiles((d, n) -> n.endsWith(GameMapIO.MAP_EXTENSION));
            if (files == null) throw new IOException("no directory " + in);
            int n = 0;
            for (File f : files) {
                String name = f.getName().substring(0, f.getName().length() - GameMapIO.MAP_EXTENSION.length());
                LiveMap m = GameMapIO.loadMap(name, in, true);
                if (!m.getMapName().equals(name)) throw new IOException(f + " holds map " + m.getMapName());
                GameMapIO.writeMap(m, out);
                n++;
            }
            System.out.println("reversed " + n + " maps into " + out);
        } else if (args.length == 2 && args[0].equals("check-maps")) {
            File in = new File(args[1]);
            File[] files = in.listFiles((d, n) -> n.endsWith(GameMapIO.MAP_EXTENSION));
            if (files == null) throw new IOException("no directory " + in);
            java.util.Arrays.sort(files);
            int bad = 0;
            for (File f : files) {
                String name = f.getName().substring(0, f.getName().length() - GameMapIO.MAP_EXTENSION.length());
                try {
                    for (boolean rev : new boolean[] {false, true}) GameMapIO.loadMap(name, in, rev).assertIsValid();
                    System.out.println("OK " + name);
                } catch (Throwable t) {
                    Throwable c = t;
                    while (c.getCause() != null) c = c.getCause();
                    System.out.println("BAD " + name + ": " + String.valueOf(c).replace('\n', ' '));
                    bad++;
                }
            }
            System.exit(bad == 0 ? 0 : 1);
        } else if (args.length == 2 && args[0].equals("replay-info")) {
            byte[] raw = Files.readAllBytes(new File(args[1]).toPath());
            if (raw.length > 2 && (raw[0] & 0xff) == 0x1f && (raw[1] & 0xff) == 0x8b) {
                try (GZIPInputStream z = new GZIPInputStream(new ByteArrayInputStream(raw))) { raw = z.readAllBytes(); }
            }
            GameWrapper g = GameWrapper.getRootAsGameWrapper(ByteBuffer.wrap(raw));
            StringBuilder sb = new StringBuilder("{\"events\":").append(g.eventsLength());
            EventWrapper first = g.events(0), last = g.events(g.eventsLength() - 1);
            sb.append(",\"first\":").append(first.eType()).append(",\"last\":").append(last.eType());
            if (first.eType() == Event.GameHeader) {
                GameHeader h = (GameHeader) first.e(new GameHeader());
                sb.append(",\"spec\":\"").append(h.specVersion()).append("\",\"teams\":[");
                for (int i = 0; i < h.teamsLength(); i++) {
                    TeamData t = h.teams(i);
                    sb.append(i > 0 ? "," : "").append("{\"id\":").append(t.teamId()).append(",\"name\":\"").append(t.name())
                      .append("\",\"package\":\"").append(t.packageName()).append("\"}");
                }
                sb.append("]");
            }
            if (last.eType() == Event.GameFooter) sb.append(",\"game_winner\":").append(((GameFooter) last.e(new GameFooter())).winner());
            sb.append(",\"matches\":[");
            for (int i = 0; i < g.matchHeadersLength(); i++) {
                EventWrapper hw = g.events(g.matchHeaders(i));
                if (hw.eType() != Event.MatchHeader) throw new IOException("matchHeaders[" + i + "] is event type " + hw.eType());
                MatchHeader mh = (MatchHeader) hw.e(new MatchHeader());
                sb.append(i > 0 ? "," : "").append("{\"map\":\"").append(mh.map().name()).append("\"");
                if (i < g.matchFootersLength()) {
                    EventWrapper fw = g.events(g.matchFooters(i));
                    if (fw.eType() != Event.MatchFooter) throw new IOException("matchFooters[" + i + "] is event type " + fw.eType());
                    MatchFooter mf = (MatchFooter) fw.e(new MatchFooter());
                    int rounds = 0;
                    for (int k = g.matchHeaders(i) + 1; k < g.matchFooters(i); k++) if (g.events(k).eType() == Event.Round) rounds++;
                    sb.append(",\"winner\":").append(mf.winner()).append(",\"win_type\":").append(mf.winType())
                      .append(",\"total_rounds\":").append(mf.totalRounds()).append(",\"round_events\":").append(rounds);
                }
                sb.append("}");
            }
            System.out.println(sb.append("]}"));
        } else {
            System.err.println("usage: Bc26Tool reverse-maps IN_DIR OUT_DIR | replay-info FILE");
            System.exit(2);
        }
    }
}
