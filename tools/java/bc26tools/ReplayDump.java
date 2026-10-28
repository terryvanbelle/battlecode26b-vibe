package bc26tools;

import battlecode.schema.*;
import com.google.flatbuffers.Table;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Battlecode 2026 replay reader. A .bc26 file is a gzipped flatbuffer GameWrapper holding one or more games
 * ("matches" in the schema).
 *
 * Usage:
 *   ReplayDump summary FILE...        one JSON line per game (volume runs; fast)
 *   ReplayDump tsv FILE OUTDIR        per game g: OUTDIR/g<i>.meta.json, .rounds.tsv, .turns.tsv, .actions.tsv
 *
 * Team ids in the replay: 1 = A, 2 = B, 0 = neutral (cats). Locations are packed as x + y * width.
 */
public class ReplayDump {
    static final String[] TYPE = {"NONE", "RAT", "KING", "CAT"};
    static final String[] WIN = {"RESIGNATION", "RATKING_DESTROYED", "BACKSTAB_RATKING_DESTROYED", "MORE_POINTS",
            "MORE_ROBOTS", "MORE_CHEESE", "TIE", "COIN_FLIP"};

    public static void main(String[] args) throws Exception {
        if (args.length < 2) { System.err.println("usage: ReplayDump summary|tsv FILE... [OUTDIR]"); System.exit(2); }
        if (args[0].equals("summary")) {
            for (int i = 1; i < args.length; i++) {
                try { new ReplayDump(args[i], null).run(); }
                catch (Exception e) { System.out.println("{\"file\":" + q(args[i]) + ",\"error\":" + q(e.toString()) + "}"); }
            }
        } else if (args[0].equals("tsv")) {
            new ReplayDump(args[1], args[2]).run();
        } else { System.err.println("unknown mode " + args[0]); System.exit(2); }
    }

    final String file; final String outDir;
    ReplayDump(String file, String outDir) { this.file = file; this.outDir = outDir; }

    // ---- per-file state
    String[] teamName = new String[3];
    int[] bcLimit = new int[4];
    // ---- per-game state
    int game = -1, width, height, symmetry;
    String mapName;
    Map<Integer, int[]> robots;   // id -> {team, type, spawnRound}
    TeamStats[] ts;
    int backstabRound, backstabTurnRobot, prevTurnRobot, lastRound;
    boolean prevCoop;
    int[] lastRoundAgg;           // final round team aggregates: cheeseXfer A,B, catDmg A,B, kings A,B, babies A,B
    PrintWriter rounds, turns, actions;

    static class TeamStats {
        int spawned, kingsFormed, deathsRat, deathsKing, exceptions, overruns, near90;
        int maxBcRat, maxBcKing; long sumBcRat, turnsRat, sumBcKing, turnsKing;
        int bitesRat, bitesKing, bitesCat, dmgDealtCat, dmgDealtRat, pickups, transfers, transferAmt;
        int ratTraps, catTraps, trapTriggers, dirtDug, dirtPlaced, squeaks, naps, throwsN, feeds;
        String json() {
            return String.format("{\"spawned\":%d,\"kingsFormed\":%d,\"deathsRat\":%d,\"deathsKing\":%d,\"exceptions\":%d,"
                    + "\"overruns\":%d,\"near90\":%d,\"maxBcRat\":%d,\"maxBcKing\":%d,\"meanBcRat\":%.0f,\"meanBcKing\":%.0f,"
                    + "\"bitesRat\":%d,\"bitesKing\":%d,\"bitesCat\":%d,\"dmgCat\":%d,\"dmgRat\":%d,\"pickups\":%d,"
                    + "\"transfers\":%d,\"transferAmt\":%d,\"ratTraps\":%d,\"catTraps\":%d,\"trapTriggers\":%d,"
                    + "\"dirtDug\":%d,\"dirtPlaced\":%d,\"squeaks\":%d,\"naps\":%d,\"throws\":%d,\"feeds\":%d}",
                    spawned, kingsFormed, deathsRat, deathsKing, exceptions, overruns, near90, maxBcRat, maxBcKing,
                    turnsRat == 0 ? 0.0 : (double) sumBcRat / turnsRat, turnsKing == 0 ? 0.0 : (double) sumBcKing / turnsKing,
                    bitesRat, bitesKing, bitesCat, dmgDealtCat, dmgDealtRat, pickups, transfers, transferAmt,
                    ratTraps, catTraps, trapTriggers, dirtDug, dirtPlaced, squeaks, naps, throwsN, feeds);
        }
    }

    static String q(String s) {
        if (s == null) return "null";
        StringBuilder b = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            if (c == '"' || c == '\\') b.append('\\').append(c);
            else if (c < 32) b.append(String.format("\\u%04x", (int) c));
            else b.append(c);
        }
        return b.append('"').toString();
    }

    static byte[] readAll(String f) throws IOException {
        byte[] raw = Files.readAllBytes(Paths.get(f));
        if (raw.length > 2 && (raw[0] & 0xff) == 0x1f && (raw[1] & 0xff) == 0x8b) {
            try (InputStream in = new GZIPInputStream(new ByteArrayInputStream(raw))) { return in.readAllBytes(); }
        }
        return raw;
    }

    /** Union members here are structs: compute the struct's position from the union vector slot. */
    static <T extends com.google.flatbuffers.Struct> T at(Turn t, int j, T s) {
        int pos = com.google.flatbuffers.UnionStructs.pos(t, 26, j);
        try { s.getClass().getMethod("__assign", int.class, java.nio.ByteBuffer.class).invoke(s, pos, com.google.flatbuffers.UnionStructs.bb(t)); }
        catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
        return s;
    }

    String loc(int packed) { return width == 0 ? String.valueOf(packed) : (packed % width) + "," + (packed / width); }

    void run() throws Exception {
        GameWrapper gw = GameWrapper.getRootAsGameWrapper(ByteBuffer.wrap(readAll(file)));
        EventWrapper ew = new EventWrapper();
        for (int i = 0; i < gw.eventsLength(); i++) {
            gw.events(ew, i);
            switch (ew.eType()) {
                case battlecode.schema.Event.GameHeader: header((GameHeader) ew.e(new GameHeader())); break;
                case battlecode.schema.Event.MatchHeader: matchHeader((MatchHeader) ew.e(new MatchHeader())); break;
                case battlecode.schema.Event.Round: round((Round) ew.e(new Round())); break;
                case battlecode.schema.Event.MatchFooter: matchFooter((MatchFooter) ew.e(new MatchFooter())); break;
                default: break;
            }
        }
    }

    void header(GameHeader h) {
        for (int i = 0; i < h.teamsLength(); i++) {
            TeamData t = h.teams(i);
            if (t.teamId() >= 0 && t.teamId() < 3) teamName[t.teamId()] = t.name();
        }
        for (int i = 0; i < h.robotTypeMetadataLength(); i++) {
            RobotTypeMetadata m = h.robotTypeMetadata(i);
            if (m.type() >= 0 && m.type() < 4) bcLimit[m.type()] = m.bytecodeLimit();
        }
    }

    void matchHeader(MatchHeader mh) throws IOException {
        game++;
        GameMap m = mh.map();
        mapName = m.name(); width = m.size().x(); height = m.size().y(); symmetry = m.symmetry();
        robots = new HashMap<>();
        ts = new TeamStats[]{new TeamStats(), new TeamStats(), new TeamStats()};
        backstabRound = -1; backstabTurnRobot = -1; prevTurnRobot = -1; prevCoop = true; lastRound = 0;
        lastRoundAgg = new int[8];
        InitialBodyTable ib = m.initialBodies();
        SpawnAction sa = new SpawnAction();
        for (int i = 0; ib != null && i < ib.spawnActionsLength(); i++) {
            ib.spawnActions(sa, i);
            robots.put(sa.id(), new int[]{sa.team(), sa.robotType(), 0});
        }
        if (outDir != null) {
            new File(outDir).mkdirs();
            String p = outDir + "/g" + game;
            try (PrintWriter meta = new PrintWriter(p + ".meta.json")) {
                StringBuilder walls = new StringBuilder(), dirt = new StringBuilder();
                for (int i = 0; i < m.wallsLength(); i++) walls.append(m.walls(i) ? '1' : '0');
                for (int i = 0; i < m.dirtLength(); i++) dirt.append(m.dirt(i) ? '1' : '0');
                StringBuilder mines = new StringBuilder("[");
                VecTable mv = m.cheeseMines();
                for (int i = 0; mv != null && i < mv.xsLength(); i++) mines.append(i > 0 ? "," : "").append("[").append(mv.xs(i)).append(",").append(mv.ys(i)).append("]");
                mines.append("]");
                StringBuilder bodies = new StringBuilder("[");
                for (int i = 0; ib != null && i < ib.spawnActionsLength(); i++) {
                    ib.spawnActions(sa, i);
                    bodies.append(i > 0 ? "," : "").append(String.format("{\"id\":%d,\"team\":%d,\"type\":\"%s\",\"x\":%d,\"y\":%d,\"dir\":%d}",
                            sa.id(), sa.team(), TYPE[sa.robotType()], sa.x(), sa.y(), sa.dir()));
                }
                bodies.append("]");
                meta.printf("{\"file\":%s,\"game\":%d,\"map\":%s,\"width\":%d,\"height\":%d,\"symmetry\":%d,\"teamA\":%s,\"teamB\":%s,"
                                + "\"seed\":%d,\"walls\":\"%s\",\"dirt\":\"%s\",\"mines\":%s,\"bodies\":%s}%n",
                        q(file), game, q(mapName), width, height, symmetry, q(teamName[1]), q(teamName[2]), m.randomSeed(),
                        walls, dirt, mines, bodies);
            }
            rounds = new PrintWriter(new BufferedWriter(new FileWriter(p + ".rounds.tsv")));
            rounds.println("round\tcheeseXferA\tcheeseXferB\tcatDmgA\tcatDmgB\tkingsA\tkingsB\tbabiesA\tbabiesB\tratTrapsA\tratTrapsB\tcatTrapsA\tcatTrapsB\tdirtA\tdirtB\tdied");
            turns = new PrintWriter(new BufferedWriter(new FileWriter(p + ".turns.tsv")));
            turns.println("round\tid\tteam\ttype\tx\ty\tdir\thp\tcheese\tmcd\ttcd\tacd\tbc\tcoop\tind");
            actions = new PrintWriter(new BufferedWriter(new FileWriter(p + ".actions.tsv")));
            actions.println("round\tid\tteam\ttype\taction\ta\tb\tc");
        }
    }

    int teamOf(int id) { int[] r = robots.get(id); return r == null ? 0 : r[0]; }
    int typeOf(int id) { int[] r = robots.get(id); return r == null ? 0 : r[1]; }

    void act(int round, int id, String name, Object a, Object b, Object c) {
        if (actions != null) actions.printf("%d\t%d\t%d\t%s\t%s\t%s\t%s\t%s%n", round, id, teamOf(id), TYPE[typeOf(id)], name, a, b, c);
    }

    void round(Round r) {
        int rn = r.roundId();
        lastRound = rn;
        if (r.teamCheeseTransferredLength() >= 2) {
            // team arrays are indexed by position in teamIds
            int ia = 0, ib = 1;
            for (int i = 0; i < r.teamIdsLength(); i++) { if (r.teamIds(i) == 1) ia = i; else if (r.teamIds(i) == 2) ib = i; }
            lastRoundAgg = new int[]{r.teamCheeseTransferred(ia), r.teamCheeseTransferred(ib), r.teamCatDamage(ia), r.teamCatDamage(ib),
                    r.teamAliveRatKings(ia), r.teamAliveRatKings(ib), r.teamAliveBabyRats(ia), r.teamAliveBabyRats(ib)};
            if (rounds != null) {
                StringBuilder died = new StringBuilder();
                for (int i = 0; i < r.diedIdsLength(); i++) died.append(i > 0 ? "," : "").append(r.diedIds(i));
                rounds.printf("%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%s%n", rn,
                        r.teamCheeseTransferred(ia), r.teamCheeseTransferred(ib), r.teamCatDamage(ia), r.teamCatDamage(ib),
                        r.teamAliveRatKings(ia), r.teamAliveRatKings(ib), r.teamAliveBabyRats(ia), r.teamAliveBabyRats(ib),
                        r.teamRatTrapCount(ia), r.teamRatTrapCount(ib), r.teamCatTrapCount(ia), r.teamCatTrapCount(ib),
                        r.teamDirtAmounts(ia), r.teamDirtAmounts(ib), died);
            }
        }
        for (int i = 0; i < r.diedIdsLength(); i++) death(r.diedIds(i), false);
        Turn t = new Turn();
        for (int i = 0; i < r.turnsLength(); i++) {
            r.turns(t, i);
            turn(rn, t);
        }
    }

    void death(int id, boolean exception) {
        int team = teamOf(id), type = typeOf(id);
        if (team < 1 || team > 2) return;
        if (exception) ts[team].exceptions++;
        if (type == 2) ts[team].deathsKing++; else if (type == 1) ts[team].deathsRat++;
    }

    void turn(int rn, Turn t) {
        int id = t.robotId(), team = teamOf(id), type = typeOf(id);
        boolean coop = t.isCooperation();
        if (backstabRound < 0 && prevCoop && !coop) { backstabRound = rn; backstabTurnRobot = id; }
        prevCoop = coop;
        StringBuilder ind = null;
        TeamStats s = team >= 1 && team <= 2 ? ts[team] : null;
        if (s != null) {
            int bc = t.bytecodesUsed(), lim = bcLimit[type];
            if (type == 1) { s.maxBcRat = Math.max(s.maxBcRat, bc); s.sumBcRat += bc; s.turnsRat++; }
            else if (type == 2) { s.maxBcKing = Math.max(s.maxBcKing, bc); s.sumBcKing += bc; s.turnsKing++; }
            if (lim > 0 && bc >= lim) s.overruns++;
            else if (lim > 0 && bc >= 0.9 * lim) s.near90++;
        }
        Table tab = new Table();
        for (int j = 0; j < t.actionsLength(); j++) {
            byte at = t.actionsType(j);
            switch (at) {
                case Action.SpawnAction: {
                    SpawnAction a = ((SpawnAction) at(t, j, new SpawnAction()));
                    robots.put(a.id(), new int[]{a.team(), a.robotType(), rn});
                    if (a.team() >= 1 && a.team() <= 2) { if (a.robotType() == 1) ts[a.team()].spawned++; }
                    act(rn, id, "spawn", a.id(), TYPE[a.robotType()], a.x() + "," + a.y()); break;
                }
                case Action.DieAction: {
                    DieAction a = ((DieAction) at(t, j, new DieAction()));
                    death(a.id(), a.dieType() == DieType.EXCEPTION);
                    act(rn, id, "die", a.id(), a.dieType() == DieType.EXCEPTION ? "EXCEPTION" : "damage", TYPE[typeOf(a.id())]); break;
                }
                case Action.RatAttack: {
                    RatAttack a = ((RatAttack) at(t, j, new RatAttack()));
                    int vt = typeOf(a.id());
                    if (s != null) { if (vt == 3) s.bitesCat++; else if (vt == 2) s.bitesKing++; else s.bitesRat++; }
                    act(rn, id, "bite", a.id(), TYPE[vt], teamOf(a.id())); break;
                }
                case Action.DamageAction: {
                    DamageAction a = ((DamageAction) at(t, j, new DamageAction()));
                    int vt = typeOf(a.id());
                    if (s != null) { if (vt == 3) s.dmgDealtCat += a.damage(); else s.dmgDealtRat += a.damage(); }
                    act(rn, id, "damage", a.id(), a.damage(), TYPE[vt]); break;
                }
                case Action.CheesePickup: {
                    CheesePickup a = ((CheesePickup) at(t, j, new CheesePickup()));
                    if (s != null) s.pickups++;
                    act(rn, id, "pickup", loc(a.loc()), "", ""); break;
                }
                case Action.CheeseTransfer: {
                    CheeseTransfer a = ((CheeseTransfer) at(t, j, new CheeseTransfer()));
                    if (s != null) { s.transfers++; s.transferAmt += a.amount(); }
                    act(rn, id, "transfer", a.id(), a.amount(), ""); break;
                }
                case Action.CheeseSpawn: {
                    CheeseSpawn a = ((CheeseSpawn) at(t, j, new CheeseSpawn()));
                    act(rn, id, "cheesespawn", loc(a.loc()), a.amount(), ""); break;
                }
                case Action.PlaceTrap: {
                    PlaceTrap a = ((PlaceTrap) at(t, j, new PlaceTrap()));
                    if (s != null) { if (a.isRatTrapType()) s.ratTraps++; else s.catTraps++; }
                    act(rn, id, a.isRatTrapType() ? "ratTrap" : "catTrap", loc(a.loc()), a.team(), ""); break;
                }
                case Action.TriggerTrap: {
                    TriggerTrap a = ((TriggerTrap) at(t, j, new TriggerTrap()));
                    if (a.team() >= 1 && a.team() <= 2) ts[a.team()].trapTriggers++;
                    act(rn, id, "trigger", loc(a.loc()), a.team(), ""); break;
                }
                case Action.RemoveTrap: {
                    RemoveTrap a = ((RemoveTrap) at(t, j, new RemoveTrap()));
                    act(rn, id, "removeTrap", loc(a.loc()), a.team(), ""); break;
                }
                case Action.BreakDirt: {
                    BreakDirt a = ((BreakDirt) at(t, j, new BreakDirt()));
                    if (s != null) s.dirtDug++;
                    act(rn, id, "dig", loc(a.loc()), "", ""); break;
                }
                case Action.PlaceDirt: {
                    PlaceDirt a = ((PlaceDirt) at(t, j, new PlaceDirt()));
                    if (s != null) s.dirtPlaced++;
                    act(rn, id, "placeDirt", loc(a.loc()), "", ""); break;
                }
                case Action.RatSqueak: {
                    RatSqueak a = ((RatSqueak) at(t, j, new RatSqueak()));
                    if (s != null) s.squeaks++;
                    act(rn, id, "squeak", loc(a.loc()), "", ""); break;
                }
                case Action.RatNap: {
                    RatNap a = ((RatNap) at(t, j, new RatNap()));
                    if (s != null) s.naps++;
                    act(rn, id, "nap", a.id(), teamOf(a.id()), ""); break;
                }
                case Action.ThrowRat: {
                    ThrowRat a = ((ThrowRat) at(t, j, new ThrowRat()));
                    if (s != null) s.throwsN++;
                    act(rn, id, "throw", a.id(), loc(a.loc()), ""); break;
                }
                case Action.CatFeed: {
                    CatFeed a = ((CatFeed) at(t, j, new CatFeed()));
                    if (s != null) s.feeds++;
                    act(rn, id, "feed", a.id(), "", ""); break;
                }
                case Action.UpgradeToRatKing: {
                    UpgradeToRatKing a = ((UpgradeToRatKing) at(t, j, new UpgradeToRatKing()));
                    if (s != null) s.kingsFormed++;
                    int[] rr = robots.get(id); if (rr != null) rr[1] = 2;
                    act(rn, id, "upgradeKing", a.phantom(), "", ""); break;
                }
                case Action.CatScratch: {
                    CatScratch a = ((CatScratch) at(t, j, new CatScratch()));
                    act(rn, id, "scratch", loc(a.loc()), "", ""); break;
                }
                case Action.CatPounce: {
                    CatPounce a = ((CatPounce) at(t, j, new CatPounce()));
                    act(rn, id, "pounce", loc(a.startLoc()), loc(a.endLoc()), ""); break;
                }
                case Action.StunAction: {
                    StunAction a = ((StunAction) at(t, j, new StunAction()));
                    act(rn, id, "stun", a.id(), a.cooldown(), ""); break;
                }
                case Action.RatCollision: {
                    RatCollision a = ((RatCollision) at(t, j, new RatCollision()));
                    act(rn, id, "collision", loc(a.loc()), "", ""); break;
                }
                case Action.IndicatorStringAction: {
                    IndicatorStringAction a = ((IndicatorStringAction) t.actions(new IndicatorStringAction(), j));
                    if (ind == null) ind = new StringBuilder(); else ind.append(" | ");
                    ind.append(a.value()); break;
                }
                default: break;
            }
        }
        if (turns != null)
            turns.printf("%d\t%d\t%d\t%s\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%s%n", rn, id, team, TYPE[type], t.x(), t.y(), t.dir(),
                    t.health(), t.cheese(), t.moveCooldown(), t.turningCooldown(), t.actionCooldown(), t.bytecodesUsed(),
                    coop ? 1 : 0, ind == null ? "" : ind.toString().replace('\t', ' ').replace('\n', ' '));
        prevTurnRobot = id;
    }

    void matchFooter(MatchFooter f) {
        int[] g = lastRoundAgg;
        String winner = f.winner() == 1 ? "A" : f.winner() == 2 ? "B" : "?";
        System.out.printf("{\"file\":%s,\"game\":%d,\"map\":%s,\"w\":%d,\"h\":%d,\"teamA\":%s,\"teamB\":%s,\"winner\":\"%s\","
                        + "\"winType\":\"%s\",\"rounds\":%d,\"backstabRound\":%d,\"backstabBy\":%d,"
                        + "\"cheeseXferA\":%d,\"cheeseXferB\":%d,\"catDmgA\":%d,\"catDmgB\":%d,\"kingsA\":%d,\"kingsB\":%d,"
                        + "\"babiesA\":%d,\"babiesB\":%d,\"A\":%s,\"B\":%s}%n",
                q(file), game, q(mapName), width, height, q(teamName[1]), q(teamName[2]), winner,
                f.winType() >= 0 && f.winType() < WIN.length ? WIN[f.winType()] : "?", f.totalRounds(), backstabRound,
                backstabTurnRobot < 0 ? 0 : teamOf(backstabTurnRobot), g[0], g[1], g[2], g[3], g[4], g[5], g[6], g[7],
                ts[1].json(), ts[2].json());
        if (rounds != null) { rounds.close(); turns.close(); actions.close(); rounds = turns = actions = null; }
    }
}
