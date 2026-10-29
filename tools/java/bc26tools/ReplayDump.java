package bc26tools;

import battlecode.schema.*;
import com.google.flatbuffers.UnionStructs;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Battlecode 2026 replay reader. A .bc26 file is a gzipped flatbuffer GameWrapper holding one or more games
 * ("matches" in the schema). Facts below are from engine 1.2.6 (battlecode26 reference source).
 *
 * Usage (build with tools/build-tools.sh; write $HOME, not ~, inside -cp):
 *   java -cp "$HOME/projects/vibe/2026b/build/tools:$HOME/projects/vibe/2026b/engine/battlecode26-java-1.2.6.jar" \
 *        bc26tools.ReplayDump summary FILE...     one JSON line per game on stdout (volume runs; fast)
 *        bc26tools.ReplayDump tsv FILE OUTDIR     the files below, plus the same JSON lines on stdout
 *
 * Conventions. Teams: 1 = A, 2 = B, 0 = neutral (cats). Types: RAT (baby), KING, CAT, NONE. Locations in rows are
 * "x,y" (packed x + y*width in the replay). Directions are FlatHelpers ordinals: 0 CENTER, 1 W, 2 SW, 3 S, 4 SE,
 * 5 E, 6 NE, 7 N, 8 NW. Footprints: a king's x,y is the centre of its 3x3; a cat's x,y is the SW corner of its 2x2
 * (x..x+1, y..y+1). A carried rat is reported at its carrier's tile.
 *
 * How the engine writes records. Each Round holds Turn records in emission order; actions are buffered and attach
 * to the NEXT record written, so an action's record is not always its actor's own turn. A robot can have up to 4
 * records in one round (column src):
 *   own      written by its processEndOfTurn: its real end-of-turn state and bytecodesUsed. At most one per
 *            robot-round.
 *   split    a thrower's record written inside throwRobot, mid-turn; bc is still the previous turn's value.
 *   thrown   the thrown rat's record written inside throwRobot, right after the split; holds the flight actions
 *            (point-blank landing, impact, death). The thrower's own record then carries the ThrowRat.
 *   carried  the extra record a carrier writes for the rat it carries, right after its own, at the carrier's tile
 *            (also when the carrier died in that turn: destroyRobot drops the rat but leaves the carrier's pointer).
 * A robot-round with no own record took no turn (spawned this round, or thrown and killed before its turn).
 * Mine spawns run in the round's first record; actions after a round's last record (e.g. the cheese drop of a
 * robot terminated after its turn) land in the next record, possibly next round.
 *
 * OUTDIR/g<i>.meta.json (written at the match header): file, game, map, width, height, symmetry, teamA, teamB,
 *   seed, walls ("0/1" per packed loc), dirt, mines [[x,y]] (every mine; pairs fire together), bodies [{id, team,
 *   type, x, y, dir, chirality}] (dir is derived by the engine: maps store CENTER, robots face the centre),
 *   cheese [[x,y,amount]] (initial tile cheese, sparse; zero on every map seen), catWaypoints {catId: [[x,y],...]},
 *   maxRounds, specVersion (always "1"), packageA, packageB, types {RAT|KING|CAT: {health, actionCooldown,
 *   movementCooldown, turningCooldown, visionRadiusSquared, visionAngle, messageRadiusSquared, bytecodeLimit}},
 *   dumpVersion.
 * OUTDIR/g<i>.summary.json: the game's summary line (below).
 * OUTDIR/game.json (written at the game footer): file, specVersion, teamA, teamB, packageA, packageB, games,
 *   winner (GameFooter: the series winner, "A"/"B"/"?").
 *
 * OUTDIR/g<i>.rounds.tsv, one row per round, from Round's team aggregates unless noted:
 *   round, cheeseXferA, cheeseXferB (cumulative cheese transferred: deliveries AND king pickups; the engine's points
 *   use it), catDmgA, catDmgB (cumulative cat damage), kingsA, kingsB (raw combined stat 10*bank + kings),
 *   babiesA, babiesB (alive baby rats at end of round, rebuilt from spawns/deaths/formations: the engine's field
 *   drifts +1 per king formed, see engBabies), ratTrapsA/B, catTrapsA/B (live traps), dirtA/B (dirt STOCK: dug
 *   minus placed, not dirt on the map), died (Round.diedIds: terminations, see die rows),
 *   numKingsA/B (kings%10), bankA/B (kings/10, global cheese), engBabiesA/B (the engine's baby count),
 *   rawCheeseA/B (cheese carried by alive baby rats, from their latest records), mapCheese (cheese on tiles, from
 *   the tile model), mined (cheese spawned by mines this round), dropped (cheese dropped by deaths), pickedA/B
 *   (cheese picked up this round, kings included).
 *
 * OUTDIR/g<i>.turns.tsv, one row per Turn record:
 *   round, id, team, type (type at the start of the turn: a forming rat's row says RAT; its upgradeKing action
 *   marks the formation), x, y, dir, hp, cheese (baby rats only; a king's cheese goes straight to the bank), mcd,
 *   tcd, acd (cooldowns), bc (bytecodesUsed; real only on src=own), coop (isCooperation at write time),
 *   ind (indicator string; control characters replaced by spaces; empty when indicators were off, as in every
 *   replica/online replay), seq (index of the record in the round, from 0), src (own|split|thrown|carried),
 *   dead (1 when hp <= 0), carrier (id of the rat carrying this robot, else 0).
 *   One robot-round = the row with src=own (the robot's real end-of-turn state).
 *
 * OUTDIR/g<i>.actions.tsv, one row per action (in emission order):
 *   round, id/team/type (the robot whose record holds the action; for buffered engine actions this is not the
 *   actor), action, a, b, c, seq/src (of that record), d, e, f, g. Kinds and their a..g:
 *   spawn       a new id, b type, c x,y, d dir, e chirality (both the spawning king's; team = row team)
 *   die         a victim, b damage|EXCEPTION|terminated, c victim type, d victim team, e cause, f killer id,
 *               g killer team. cause: bite, scratch (cat), crush / pounce (cat move or pounce: no action of its
 *               own; pounce when the cat moved >1 tile or its mcd rose by >=40), trap (killer = placer, its team;
 *               also a thrown rat that survived its impact and died on a trap where it landed: the impact counts
 *               first in dmgBy), throwHit (hit by a thrown rat; killer = the thrower), throwLand (died landing;
 *               killer = thrower),
 *               fed (thrown into a cat; killer = thrower), form (killed by an ENEMY king formation),
 *               merge (an own rat consumed by its team's formation: not a death in the counters), starve (king,
 *               bank < 2), exception (player failed to load; the only EXCEPTION source), terminated (Round.diedIds:
 *               run() returned, uncaught exception, disintegrate, resign, or dropped dead; id = victim, seq -1),
 *               other.
 *   bite        a victim, b victim type, c victim team, d damage (10 nominal or the victim's hp when lethal; the
 *               cheese boost ceil(sqrt(cheese)) is not in the action: the summary adds it from the victim's next
 *               record), e lethal 0/1
 *   scratch     a x,y, d victim, e victim team, f damage (20, capped at hp)
 *   damage      a thrown rat (always the row robot), b its landing damage, c type, d ground|target|eaten (eaten:
 *               the engine ran hitGround on a rat a cat already ate; no real damage), e thrower, f thrower team
 *   throwHit    a robot hit by a thrown rat, b damage (the thrown rat's, capped at hp), c its type, d its team,
 *               e thrown rat, f thrower, g thrower team. Only when a non-flying robot was on the tile hit.
 *   land        a thrown rat (row robot), b ground|target|eaten, c fatal 0/1, d thrower, e thrower team
 *   nap         a grab: a victim, b victim team, d enemy|ally (row robot is the grabber)
 *   drop        a rat leaving its carrier, b its team, c carrier (voluntary) | grabbed (the carrier was grabbed) |
 *               killed (the carrier died) | other, d carrier, e carrier team
 *   release     a rat freeing itself in its own turn, b carrier, c wriggle | swap | carrierDead
 *   swap        a the former carrier, now carried by the row robot (after a release/swap), b its team
 *   throw       a thrown rat, b first tile in the throw direction (thrower + dir), not the landing tile
 *   stun        a robot, b cooldown (10 landed on ground, 20 hit a robot/wall/dirt)
 *   pickup      a x,y, b amount (tile model: tile before minus remainder; a king's exact amount)
 *   transfer    a receiving king, b amount (a delivery by a rat)
 *   kingPickup  a king, b amount (a king picking cheese up itself: CheeseTransfer to itself)
 *   cheesespawn a x,y, b raw amount, d mine|drop|remainder, e tile total after, f tile change. mine rows have
 *               id/team 0 (runCheeseMines runs in whatever record is first) and are told by place: a pair in the
 *               round's first record, p within 4 of a mine then sym(p); drop = a dying robot's cheese (just
 *               before its die row; a terminated robot's has none); remainder = what a partial pickup left
 *   ratTrap, catTrap  a x,y, b team
 *   trigger     a trap x,y, b triggering robot's team (0 = cat, so a cat trap), d trap owner team, e victim,
 *               f damage (50 rat trap, 100 cat trap, capped at hp; 0 on a victim an earlier trap killed: the
 *               engine keeps firing the tile's traps), g rat|cat
 *   removeTrap  a x,y, b team (removes only that team's trap)
 *   dig, placeDirt, squeak   a x,y
 *   upgradeKing a king id, d own rats merged, e enemies killed
 *   catSleep    a cat id: one per turn a cat sleeps after eating (CatFeed carries the cat's id, not the rat's)
 *   feed        a rat thrown into a cat, b cat id (0 if not found), c rat team, d thrower, e thrower team
 * OUTDIR/g<i>.indicators.tsv (only when the replay has indicator dots/lines; replica replays never do):
 *   round, id, team, kind (dot|line), a x,y, b end x,y (lines), color (hex rgb), seq
 * OUTDIR/g<i>.markers.tsv (only when the footer has timeline markers; none seen in any replay):
 *   round (the client shows it at round+1), team (1/2), label, color
 *
 * Summary JSON (one line per game): file, game, map, w, h, teamA, teamB, winner, winType, rounds, backstabRound,
 *   backstabBy (team whose action started the backstab: the trap OWNER for a rat-trap backstab), cheeseXferA/B,
 *   catDmgA/B, kingsA/B (raw, as rounds.tsv), babiesA/B (rebuilt alive baby rats), A, B (team stats below),
 *   then backstabCause (bite|grab|trap|?; '' when no backstab), packageA/B, maxRounds, numKingsA/B, bankA/B, engBabiesA/B,
 *   catSleepTurns, markers, profilerFiles, cheeseModelErr (king pickups and mine rows whose tile-model amount
 *   disagreed with the engine's exact amount, which the model then takes; 0 expected), dumpVersion.
 * Team stats: spawned, kingsFormed, deathsRat/deathsKing (deaths of any cause except merge; terminations
 *   included), exceptions (load failures only: runtime exceptions show up as terminations), overruns, near90
 *   (>= 90% of the limit), maxBcRat, maxBcKing, meanBcRat, meanBcKing (all from src=own records, once per
 *   robot-round, with the limit of the type at the start of the turn), bitesRat/King/Cat, dmgCat (cat damage =
 *   catDmg), dmgRat / dmgKing (hp this team DEALT to enemy baby rats / kings, all sources, killing blows
 *   included), pickups, transfers / transferAmt (deliveries only, king pickups excluded), ratTraps, catTraps,
 *   trapTriggers, catTrapHits, ratTrapHits (traps OWNED by this team that fired), dirtDug, dirtPlaced, squeaks,
 *   naps (grabs, enemy+ally), throws, feeds (rats this team threw into cats), then ratsMerged, terminated,
 *   deathsByEnemy, deathsByCat, deathsSelf (rat deaths by killer team), ratDeaths / kingDeaths {cause: n},
 *   killsRat, killsKing, catsKilled (killing blows), dmgBy {source: hp dealt to enemy rats+kings}, throwDmgTaken
 *   (landing damage this team's thrown rats took: the old dmgRat), catDmgTaken (hp lost to cats), catDmgTrap
 *   (min(100, cat hp) per trap hit), catDmgBite (catDmg - catDmgTrap: throws never hurt cats), grabsEnemy,
 *   grabsAlly, drops (voluntary), forcedDrops (this team's carriers lost their rat), releases, pickupAmt,
 *   kingPickups, kingPickupAmt, turnsRat, turnsKing, starveDmg (kings' hp lost to starvation: 10 per starving
 *   turn), friendlyDmg (hp lost to this team's own throws: an enemy rat thrown into an own robot, an own rat's
 *   landing).
 * hp accounting: a team's hp lost (rats and kings) = the other team's dmgRat + dmgKing + this team's catDmgTaken +
 *   starveDmg + friendlyDmg (exact on every replay checked, except hp merged into a king).
 * Changed in dumpVersion 2: babiesA/B rebuilt; dmgRat/dmgCat redefined (dmgRat was throw damage taken, dmgCat was
 *   always 0); naps = grabs only (was every RatNap); transfers/transferAmt exclude king pickups; deathsRat excludes
 *   merged rats; feeds credited (was always 0); ratTrapHits/catTrapHits per (tile, team); backstabBy fixed for trap
 *   backstabs; bc stats from own records only (duplicate records inflated them); 'feed' rows renamed catSleep;
 *   RatNap rows split into nap/drop/release/swap/land; self CheeseTransfer rows renamed kingPickup.
 *
 * KNOWN GAPS (not in the replay): bite damage beyond the base 10 (inferred from the victim's next record, so a
 * bite boost and a king's starvation in the same turn are split by assuming the starvation is 10); which tiles a rat
 * passed; a cat's pounce vs move except by inference (no CatPounce or RatCollision is ever written: their branches
 * below are dead); non-fatal king starvation (no action: inferred from hp); the robot hit by a thrown rat when it
 * survives (no action: found by position, so it can be missed); runtime exceptions (only terminations are visible);
 * per-tile cheese is a model (seeded from GameMap.cheese) and cheese moved by kings' formations is not on tiles;
 * where a dropped rat landed (until its next record); robot stdout; TeamInfo.cheeseCollected; GameplayConstants
 * (empty) and RobotTypeMetadata.maxCheese (never set); WinType TIE/BACKSTAB_RATKING_DESTROYED (never written);
 * profiler files (counted only). Indicators and timeline markers exist only with show-indicators on (local runs).
 */
public class ReplayDump {
    static final String[] TYPE = {"NONE", "RAT", "KING", "CAT"};
    static final String[] WIN = {"RESIGNATION", "RATKING_DESTROYED", "BACKSTAB_RATKING_DESTROYED", "MORE_POINTS",
            "MORE_ROBOTS", "MORE_CHEESE", "TIE", "COIN_FLIP"};
    static final int DUMP_VERSION = 2, RAT = 1, KING = 2, CAT = 3;
    // engine 1.2.6 constants: GameConstants RAT_BITE_DAMAGE, CAT_SCRATCH_DAMAGE, CHEESE_SPAWN_AMOUNT; TrapType damage
    static final int BITE = 10, SCRATCH = 20, MINE = 20, MINE_R = 4, RAT_TRAP_DMG = 50, CAT_TRAP_DMG = 100;
    static final int NO_HP = Integer.MIN_VALUE;
    static final String USAGE = "usage: ReplayDump summary FILE... | ReplayDump tsv FILE OUTDIR";

    public static void main(String[] args) throws Exception {
        if (args.length < 2 || args[0].equals("tsv") && args.length != 3) { System.err.println(USAGE); System.exit(2); }
        if (args[0].equals("summary")) {
            for (int i = 1; i < args.length; i++) {
                try { new ReplayDump(args[i], null).run(); }
                catch (Exception e) { System.out.println("{\"file\":" + q(args[i]) + ",\"error\":" + q(e.toString()) + "}"); }
            }
        } else if (args[0].equals("tsv")) {
            new ReplayDump(args[1], args[2]).run();
        } else { System.err.println("unknown mode " + args[0] + "\n" + USAGE); System.exit(2); }
    }

    final String file; final String outDir;
    ReplayDump(String file, String outDir) { this.file = file; this.outDir = outDir; }

    // ---- per-file state
    String[] teamName = new String[3], pkg = new String[3];
    String spec;
    int[] bcLimit = new int[4], baseHp = {0, 100, 600, 4000};
    String typesJson = "{}";
    // ---- per-game state
    int game = -1, width, height, symmetry, maxRounds;
    String mapName;
    Map<Integer, Bot> bots;
    Map<Integer, Trap> traps;      // loc*4 + owning team -> live trap (each team has its own trap layer)
    int[] tile;                    // cheese per packed loc (model)
    int[] mineLoc;                 // packed mine locations
    int leftoverLoc = -1;          // where the last record's robot, terminated, dropped cheese into the next round
    TeamStats[] ts;
    int backstabRound, backstabBy, lastRound, catSleepTurns, cheeseModelErr, trapOrder;
    String backstabCause;
    boolean bsPending;
    int[] lastRoundAgg;            // final round: cheeseXfer A,B, catDmg A,B, kings A,B, babies A,B (engine)
    PrintWriter rounds, turns, actions, indicators;
    // ---- per-round / per-record state
    int rn, seq, mined, dropped;
    int[] picked = new int[3];
    String src = "";
    int carrierOfRec;              // the carrier writing the current src=carried record
    Bot hitVictim;                 // a robot killed by the current record's thrown-rat impact

    static final class Bot {
        final int id, team; int type, x, y, hp, cheese, mcd, carrier, thrower, tdx, tdy, exCarrier, exRound = -1, exSeq, biteTeam;
        boolean alive = true, flying;
        Bot(int id, int team, int type, int x, int y, int hp) { this.id = id; this.team = team; this.type = type; this.x = x; this.y = y; this.hp = hp; }
        /** Clear the carrier, remembering it: a carrier that died this turn still writes the carried record. */
        void uncarry(int rn, int seq) { if (carrier != 0) { exCarrier = carrier; exRound = rn; exSeq = seq; } carrier = 0; }
        boolean carriedBy(int p, int rn, int seq) { return carrier == p || exCarrier == p && exRound == rn && exSeq == seq; }
        boolean covers(int px, int py) {
            if (type == KING) return Math.abs(px - x) <= 1 && Math.abs(py - y) <= 1;
            if (type == CAT) return px >= x && px <= x + 1 && py >= y && py <= y + 1;
            return px == x && py == y;
        }
    }

    static final class Trap { final boolean rat; final int placer, order; Trap(boolean rat, int placer, int order) { this.rat = rat; this.placer = placer; this.order = order; } }

    /** One decoded action. Fields by type: id (robot), loc/loc2 (packed), n (amount, damage, cooldown, dir, dieType). */
    static final class Act {
        byte t; int id, loc, loc2, n, team, type, chir, x, y; boolean rat; String s;
        // annotations set while processing the record
        int victim, owner, placer, hp0 = NO_HP; String cause = ""; boolean remainder;   // hp0: before an impact applied early
    }

    static class TeamStats {
        int spawned, kingsFormed, deathsRat, deathsKing, exceptions, overruns, near90;
        int maxBcRat, maxBcKing; long sumBcRat, turnsRat, sumBcKing, turnsKing;
        int bitesRat, bitesKing, bitesCat, dmgCat, dmgRat, dmgKing, pickups, transfers, transferAmt;
        int ratTraps, catTraps, trapTriggers, catTrapHits, ratTrapHits, dirtDug, dirtPlaced, squeaks, naps, throwsN, feeds;
        int ratsMerged, terminated, deathsByEnemy, deathsByCat, deathsSelf, killsRat, killsKing, catsKilled;
        int throwDmgTaken, catDmgTaken, catDmgTrap, catDmgBite, grabsEnemy, grabsAlly, drops, forcedDrops, releases;
        int pickupAmt, kingPickups, kingPickupAmt, starveDmg, friendlyDmg;
        Map<String, Integer> ratDeaths = new TreeMap<>(), kingDeaths = new TreeMap<>(), dmgBy = new TreeMap<>();
        String json() {
            return String.format("{\"spawned\":%d,\"kingsFormed\":%d,\"deathsRat\":%d,\"deathsKing\":%d,\"exceptions\":%d,"
                    + "\"overruns\":%d,\"near90\":%d,\"maxBcRat\":%d,\"maxBcKing\":%d,\"meanBcRat\":%.0f,\"meanBcKing\":%.0f,"
                    + "\"bitesRat\":%d,\"bitesKing\":%d,\"bitesCat\":%d,\"dmgCat\":%d,\"dmgRat\":%d,\"pickups\":%d,"
                    + "\"transfers\":%d,\"transferAmt\":%d,\"ratTraps\":%d,\"catTraps\":%d,\"trapTriggers\":%d,\"catTrapHits\":%d,\"ratTrapHits\":%d,"
                    + "\"dirtDug\":%d,\"dirtPlaced\":%d,\"squeaks\":%d,\"naps\":%d,\"throws\":%d,\"feeds\":%d,"
                    + "\"ratsMerged\":%d,\"terminated\":%d,\"deathsByEnemy\":%d,\"deathsByCat\":%d,\"deathsSelf\":%d,"
                    + "\"ratDeaths\":%s,\"kingDeaths\":%s,\"killsRat\":%d,\"killsKing\":%d,\"catsKilled\":%d,\"dmgKing\":%d,"
                    + "\"dmgBy\":%s,\"throwDmgTaken\":%d,\"catDmgTaken\":%d,\"catDmgTrap\":%d,\"catDmgBite\":%d,"
                    + "\"grabsEnemy\":%d,\"grabsAlly\":%d,\"drops\":%d,\"forcedDrops\":%d,\"releases\":%d,"
                    + "\"pickupAmt\":%d,\"kingPickups\":%d,\"kingPickupAmt\":%d,\"turnsRat\":%d,\"turnsKing\":%d,\"starveDmg\":%d,\"friendlyDmg\":%d}",
                    spawned, kingsFormed, deathsRat, deathsKing, exceptions, overruns, near90, maxBcRat, maxBcKing,
                    turnsRat == 0 ? 0.0 : (double) sumBcRat / turnsRat, turnsKing == 0 ? 0.0 : (double) sumBcKing / turnsKing,
                    bitesRat, bitesKing, bitesCat, dmgCat, dmgRat, pickups, transfers, transferAmt,
                    ratTraps, catTraps, trapTriggers, catTrapHits, ratTrapHits, dirtDug, dirtPlaced, squeaks, naps, throwsN, feeds,
                    ratsMerged, terminated, deathsByEnemy, deathsByCat, deathsSelf, obj(ratDeaths), obj(kingDeaths),
                    killsRat, killsKing, catsKilled, dmgKing, obj(dmgBy), throwDmgTaken, catDmgTaken, catDmgTrap, catDmgBite,
                    grabsEnemy, grabsAlly, drops, forcedDrops, releases, pickupAmt, kingPickups, kingPickupAmt, turnsRat, turnsKing, starveDmg, friendlyDmg);
        }
    }

    static String obj(Map<String, Integer> m) {
        StringBuilder b = new StringBuilder("{");
        for (Map.Entry<String, Integer> e : m.entrySet()) b.append(b.length() > 1 ? "," : "").append(q(e.getKey())).append(':').append(e.getValue());
        return b.append('}').toString();
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

    static String clean(String s) {
        StringBuilder b = new StringBuilder(s.length());
        for (char c : s.toCharArray()) b.append(c < 32 || c == 127 ? ' ' : c);
        return b.toString();
    }

    static byte[] readAll(String f) throws IOException {
        byte[] raw = Files.readAllBytes(Paths.get(f));
        if (raw.length > 2 && (raw[0] & 0xff) == 0x1f && (raw[1] & 0xff) == 0x8b) {
            try (InputStream in = new GZIPInputStream(new ByteArrayInputStream(raw))) { return in.readAllBytes(); }
        }
        return raw;
    }

    /** Union members here are structs: decode each from its slot in Turn.actions (vtable offset 26). */
    static Act[] decode(Turn t) {
        int n = t.actionsLength();
        Act[] L = new Act[n];
        ByteBuffer bb = n > 0 ? UnionStructs.bb(t) : null;
        for (int j = 0; j < n; j++) {
            Act a = L[j] = new Act();
            a.t = t.actionsType(j);
            if (a.t == Action.IndicatorStringAction) { a.s = ((IndicatorStringAction) t.actions(new IndicatorStringAction(), j)).value(); continue; }
            if (a.t == Action.NONE) continue;
            int p = UnionStructs.pos(t, 26, j);
            switch (a.t) {
                case Action.CatFeed: a.id = new CatFeed().__assign(p, bb).id(); break;
                case Action.RatAttack: a.id = new RatAttack().__assign(p, bb).id(); break;
                case Action.RatNap: a.id = new RatNap().__assign(p, bb).id(); break;
                case Action.RatCollision: a.loc = new RatCollision().__assign(p, bb).loc(); break;
                case Action.PlaceDirt: a.loc = new PlaceDirt().__assign(p, bb).loc(); break;
                case Action.BreakDirt: a.loc = new BreakDirt().__assign(p, bb).loc(); break;
                case Action.CheesePickup: a.loc = new CheesePickup().__assign(p, bb).loc(); break;
                case Action.CheeseSpawn: { CheeseSpawn s = new CheeseSpawn().__assign(p, bb); a.loc = s.loc(); a.n = s.amount(); break; }
                case Action.CheeseTransfer: { CheeseTransfer s = new CheeseTransfer().__assign(p, bb); a.id = s.id(); a.n = s.amount(); break; }
                case Action.CatScratch: a.loc = new CatScratch().__assign(p, bb).loc(); break;
                case Action.CatPounce: { CatPounce s = new CatPounce().__assign(p, bb); a.loc = s.startLoc(); a.loc2 = s.endLoc(); break; }
                case Action.PlaceTrap: { PlaceTrap s = new PlaceTrap().__assign(p, bb); a.loc = s.loc(); a.team = s.team(); a.rat = s.isRatTrapType(); break; }
                case Action.RemoveTrap: { RemoveTrap s = new RemoveTrap().__assign(p, bb); a.loc = s.loc(); a.team = s.team(); break; }
                case Action.TriggerTrap: { TriggerTrap s = new TriggerTrap().__assign(p, bb); a.loc = s.loc(); a.team = s.team(); break; }
                case Action.ThrowRat: { ThrowRat s = new ThrowRat().__assign(p, bb); a.id = s.id(); a.loc = s.loc(); break; }
                case Action.UpgradeToRatKing: a.id = new UpgradeToRatKing().__assign(p, bb).phantom(); break;
                case Action.RatSqueak: a.loc = new RatSqueak().__assign(p, bb).loc(); break;
                case Action.DamageAction: { DamageAction s = new DamageAction().__assign(p, bb); a.id = s.id(); a.n = s.damage(); break; }
                case Action.StunAction: { StunAction s = new StunAction().__assign(p, bb); a.id = s.id(); a.n = s.cooldown(); break; }
                case Action.SpawnAction: { SpawnAction s = new SpawnAction().__assign(p, bb); a.id = s.id(); a.x = s.x(); a.y = s.y(); a.n = s.dir(); a.chir = s.chirality(); a.team = s.team(); a.type = s.robotType(); break; }
                case Action.DieAction: { DieAction s = new DieAction().__assign(p, bb); a.id = s.id(); a.n = s.dieType(); break; }
                case Action.IndicatorDotAction: { IndicatorDotAction s = new IndicatorDotAction().__assign(p, bb); a.loc = s.loc(); a.n = s.colorHex(); break; }
                case Action.IndicatorLineAction: { IndicatorLineAction s = new IndicatorLineAction().__assign(p, bb); a.loc = s.startLoc(); a.loc2 = s.endLoc(); a.n = s.colorHex(); break; }
                default: break;
            }
        }
        return L;
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
                case battlecode.schema.Event.GameFooter: gameFooter((GameFooter) ew.e(new GameFooter())); break;
                default: break;
            }
        }
    }

    void header(GameHeader h) {
        spec = h.specVersion();
        for (int i = 0; i < h.teamsLength(); i++) {
            TeamData t = h.teams(i);
            if (t.teamId() >= 0 && t.teamId() < 3) { teamName[t.teamId()] = t.name(); pkg[t.teamId()] = t.packageName(); }
        }
        StringBuilder ty = new StringBuilder("{");
        for (int i = 0; i < h.robotTypeMetadataLength(); i++) {
            RobotTypeMetadata m = h.robotTypeMetadata(i);
            if (m.type() < 1 || m.type() > 3) continue;
            bcLimit[m.type()] = m.bytecodeLimit();
            if (m.baseHealth() > 0) baseHp[m.type()] = m.baseHealth();
            ty.append(ty.length() > 1 ? "," : "").append(String.format("\"%s\":{\"health\":%d,\"actionCooldown\":%d,\"movementCooldown\":%d,"
                    + "\"turningCooldown\":%d,\"visionRadiusSquared\":%d,\"visionAngle\":%d,\"messageRadiusSquared\":%d,\"bytecodeLimit\":%d}",
                    TYPE[m.type()], m.baseHealth(), m.actionCooldown(), m.movementCooldown(), m.turningCooldown(),
                    m.visionConeRadiusSquared(), m.visionConeAngle(), m.messageRadiusSquared(), m.bytecodeLimit()));
        }
        typesJson = ty.append('}').toString();
    }

    void matchHeader(MatchHeader mh) throws IOException {
        game++;
        GameMap m = mh.map();
        mapName = m.name(); width = m.size().x(); height = m.size().y(); symmetry = m.symmetry(); maxRounds = mh.maxRounds();
        bots = new HashMap<>();
        traps = new HashMap<>();
        tile = new int[width * height];
        ts = new TeamStats[]{new TeamStats(), new TeamStats(), new TeamStats()};
        backstabRound = -1; backstabBy = 0; backstabCause = ""; bsPending = false; lastRound = 0;
        catSleepTurns = 0; cheeseModelErr = 0; trapOrder = 0;
        lastRoundAgg = new int[8];
        StringBuilder cheese = new StringBuilder("[");
        for (int i = 0; i < m.cheeseLength() && i < tile.length; i++) {
            tile[i] = m.cheese(i) & 0xff;
            if (tile[i] > 0) cheese.append(cheese.length() > 1 ? "," : "").append("[").append(i % width).append(",").append(i / width).append(",").append(tile[i]).append("]");
        }
        cheese.append("]");
        InitialBodyTable ib = m.initialBodies();
        SpawnAction sa = new SpawnAction();
        StringBuilder bodies = new StringBuilder("[");
        for (int i = 0; ib != null && i < ib.spawnActionsLength(); i++) {
            ib.spawnActions(sa, i);
            bots.put(sa.id(), new Bot(sa.id(), sa.team(), sa.robotType(), sa.x(), sa.y(), baseHp[sa.robotType() & 3]));
            bodies.append(i > 0 ? "," : "").append(String.format("{\"id\":%d,\"team\":%d,\"type\":\"%s\",\"x\":%d,\"y\":%d,\"dir\":%d,\"chirality\":%d}",
                    sa.id(), sa.team(), TYPE[sa.robotType()], sa.x(), sa.y(), sa.dir(), sa.chirality()));
        }
        bodies.append("]");
        VecTable mv = m.cheeseMines();
        mineLoc = new int[mv == null ? 0 : mv.xsLength()];
        for (int i = 0; i < mineLoc.length; i++) mineLoc[i] = mv.xs(i) + mv.ys(i) * width;
        leftoverLoc = -1;
        if (outDir == null) return;
        new File(outDir).mkdirs();
        String p = outDir + "/g" + game;
        Files.deleteIfExists(Paths.get(p + ".indicators.tsv"));
        Files.deleteIfExists(Paths.get(p + ".markers.tsv"));
        try (PrintWriter meta = new PrintWriter(p + ".meta.json")) {
            StringBuilder walls = new StringBuilder(), dirt = new StringBuilder();
            for (int i = 0; i < m.wallsLength(); i++) walls.append(m.walls(i) ? '1' : '0');
            for (int i = 0; i < m.dirtLength(); i++) dirt.append(m.dirt(i) ? '1' : '0');
            StringBuilder mines = new StringBuilder("[");
            for (int i = 0; mv != null && i < mv.xsLength(); i++) mines.append(i > 0 ? "," : "").append("[").append(mv.xs(i)).append(",").append(mv.ys(i)).append("]");
            mines.append("]");
            StringBuilder wp = new StringBuilder("{");
            VecTable wv = new VecTable();
            for (int i = 0; i < m.catWaypointIdsLength() && i < m.catWaypointVecsLength(); i++) {
                m.catWaypointVecs(wv, i);
                wp.append(i > 0 ? "," : "").append('"').append(m.catWaypointIds(i)).append("\":[");
                for (int k = 0; k < wv.xsLength(); k++) wp.append(k > 0 ? "," : "").append("[").append(wv.xs(k)).append(",").append(wv.ys(k)).append("]");
                wp.append("]");
            }
            wp.append("}");
            meta.printf("{\"file\":%s,\"game\":%d,\"map\":%s,\"width\":%d,\"height\":%d,\"symmetry\":%d,\"teamA\":%s,\"teamB\":%s,"
                            + "\"seed\":%d,\"walls\":\"%s\",\"dirt\":\"%s\",\"mines\":%s,\"bodies\":%s,\"cheese\":%s,\"catWaypoints\":%s,"
                            + "\"maxRounds\":%d,\"specVersion\":%s,\"packageA\":%s,\"packageB\":%s,\"types\":%s,\"dumpVersion\":%d}%n",
                    q(file), game, q(mapName), width, height, symmetry, q(teamName[1]), q(teamName[2]), m.randomSeed(),
                    walls, dirt, mines, bodies, cheese, wp, maxRounds, q(spec), q(pkg[1]), q(pkg[2]), typesJson, DUMP_VERSION);
        }
        rounds = new PrintWriter(new BufferedWriter(new FileWriter(p + ".rounds.tsv")));
        rounds.println("round\tcheeseXferA\tcheeseXferB\tcatDmgA\tcatDmgB\tkingsA\tkingsB\tbabiesA\tbabiesB\tratTrapsA\tratTrapsB\tcatTrapsA\tcatTrapsB\tdirtA\tdirtB\tdied"
                + "\tnumKingsA\tnumKingsB\tbankA\tbankB\tengBabiesA\tengBabiesB\trawCheeseA\trawCheeseB\tmapCheese\tmined\tdropped\tpickedA\tpickedB");
        turns = new PrintWriter(new BufferedWriter(new FileWriter(p + ".turns.tsv")));
        turns.println("round\tid\tteam\ttype\tx\ty\tdir\thp\tcheese\tmcd\ttcd\tacd\tbc\tcoop\tind\tseq\tsrc\tdead\tcarrier");
        actions = new PrintWriter(new BufferedWriter(new FileWriter(p + ".actions.tsv")));
        actions.println("round\tid\tteam\ttype\taction\ta\tb\tc\tseq\tsrc\td\te\tf\tg");
    }

    Bot bot(int id) {
        Bot b = bots.get(id);
        if (b == null) bots.put(id, b = new Bot(id, 0, 0, 0, 0, 0));
        return b;
    }
    int teamOf(int id) { Bot b = bots.get(id); return b == null ? 0 : b.team; }
    int typeOf(int id) { Bot b = bots.get(id); return b == null ? 0 : b.type; }
    static boolean player(int team) { return team == 1 || team == 2; }

    void act(int id, String name, Object a, Object b, Object c, Object... dg) {
        if (actions == null) return;
        actions.printf("%d\t%d\t%d\t%s\t%s\t%s\t%s\t%s\t%d\t%s", rn, id, teamOf(id), TYPE[typeOf(id)], name, a, b, c, seq, src);
        for (int i = 0; i < 4; i++) { actions.print('\t'); if (i < dg.length) actions.print(dg[i]); }
        actions.println();
    }

    /** The live robot covering (x, y), ignoring carried and flying rats and the given id. */
    Bot at(int x, int y, int except, boolean cats) {
        for (Bot b : bots.values())
            if (b.alive && b.id != except && b.carrier == 0 && !b.flying && (cats || b.type != CAT) && b.covers(x, y)) return b;
        return null;
    }

    void round(Round r) {
        rn = r.roundId();
        lastRound = rn; mined = dropped = 0; picked = new int[3];
        int n = r.turnsLength();
        Turn[] tv = new Turn[n];
        Act[][] av = new Act[n][];
        for (int i = 0; i < n; i++) { tv[i] = r.turns(new Turn(), i); av[i] = decode(tv[i]); }
        // throwRobot writes [thrower split][thrown rat] and queues ThrowRat into the thrower's own record
        String[] srcs = new String[n];
        int[] throwRec = new int[n];
        for (int i = 2; i < n; i++)
            for (Act a : av[i])
                if (a.t == Action.ThrowRat && tv[i - 1].robotId() == a.id && tv[i - 2].robotId() == tv[i].robotId()) {
                    srcs[i - 1] = "thrown"; srcs[i - 2] = "split"; throwRec[i - 1] = i;
                    Bot x = bot(a.id);
                    x.thrower = tv[i].robotId(); x.tdx = a.loc % width - tv[i - 2].x(); x.tdy = a.loc / width - tv[i - 2].y();
                }
        for (int i = 0; i < n; i++) {
            String s = srcs[i];
            if (s == null) {
                Bot b = bot(tv[i].robotId());
                s = i > 0 && "own".equals(srcs[i - 1]) && b.carriedBy(tv[i - 1].robotId(), rn, i - 1)
                        && tv[i].x() == tv[i - 1].x() && tv[i].y() == tv[i - 1].y() ? "carried" : "own";
                srcs[i] = s;
            }
            if (s.equals("thrown")) { Bot x = bot(tv[i].robotId()); x.carrier = 0; x.flying = true; }
            seq = i; src = s; carrierOfRec = s.equals("carried") ? tv[i - 1].robotId() : 0;
            record(tv[i], av[i]);
        }
        seq = -1; src = ""; carrierOfRec = 0;
        leftoverLoc = -1;
        for (int i = 0; i < r.diedIdsLength(); i++) {
            Bot v = bots.get(r.diedIds(i));
            if (v == null || !v.alive) continue;
            if (n > 0 && v.id == tv[n - 1].robotId() && v.cheese > 0) leftoverLoc = v.x + v.y * width;
            v.alive = false; v.uncarry(rn, seq); v.flying = false;
            if (player(v.team)) { death(v, "terminated", 0, 0); ts[v.team].terminated++; }
            act(v.id, "die", v.id, "terminated", TYPE[v.type], v.team, "terminated", 0, 0);
        }
        if (r.teamCheeseTransferredLength() >= 2) {
            // team arrays are indexed by position in teamIds
            int ia = 0, ib = 1;
            for (int i = 0; i < r.teamIdsLength(); i++) { if (r.teamIds(i) == 1) ia = i; else if (r.teamIds(i) == 2) ib = i; }
            int[] babies = new int[3], raw = new int[3];
            for (Bot b : bots.values()) if (b.alive && b.type == RAT && player(b.team)) { babies[b.team]++; raw[b.team] += b.cheese; }
            int ka = r.teamAliveRatKings(ia), kb = r.teamAliveRatKings(ib);
            lastRoundAgg = new int[]{r.teamCheeseTransferred(ia), r.teamCheeseTransferred(ib), r.teamCatDamage(ia), r.teamCatDamage(ib),
                    ka, kb, r.teamAliveBabyRats(ia), r.teamAliveBabyRats(ib), babies[1], babies[2]};
            if (rounds != null) {
                StringBuilder died = new StringBuilder();
                for (int i = 0; i < r.diedIdsLength(); i++) died.append(i > 0 ? "," : "").append(r.diedIds(i));
                long mapCheese = 0;
                for (int c : tile) mapCheese += c;
                rounds.printf("%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%s\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d%n", rn,
                        r.teamCheeseTransferred(ia), r.teamCheeseTransferred(ib), r.teamCatDamage(ia), r.teamCatDamage(ib),
                        ka, kb, babies[1], babies[2],
                        r.teamRatTrapCount(ia), r.teamRatTrapCount(ib), r.teamCatTrapCount(ia), r.teamCatTrapCount(ib),
                        r.teamDirtAmounts(ia), r.teamDirtAmounts(ib), died, ka % 10, kb % 10, ka / 10, kb / 10,
                        r.teamAliveBabyRats(ia), r.teamAliveBabyRats(ib), raw[1], raw[2], mapCheese, mined, dropped, picked[1], picked[2]);
            }
        }
    }

    /** Index of the next action after j that is not a death side effect (cheese drop, trap triggers or death of x). */
    static int nextMain(Act[] L, int j, int x) {
        int k = j + 1;
        while (k < L.length && (L[k].t == Action.CheeseSpawn || L[k].t == Action.TriggerTrap || L[k].t == Action.DieAction && L[k].id == x)) k++;
        return k;
    }

    /** The trap that killed x, found back from its die row at j over its cheese drop and other triggers, or null. */
    static Act trapBefore(Act[] L, int j, int x) {
        for (int k = j - 1; k >= 0 && (L[k].t == Action.CheeseSpawn || L[k].t == Action.TriggerTrap); k--)
            if (L[k].t == Action.TriggerTrap && L[k].victim == x) return L[k];
        return null;
    }

    /** hitTarget: the thrown rat x's DamageAction after j (stun 20 follows), written after the landing's traps. */
    static int impactAfter(Act[] L, int j, int x) {
        for (int k = j + 1; k < L.length; k++) if (L[k].t == Action.DamageAction && L[k].id == x) return stunAfter(L, k, x) == 20 ? k : -1;
        return -1;
    }

    int symLoc(int loc) {
        int x = loc % width, y = loc / width;   // MapSymmetry: 0 ROTATIONAL, 1 HORIZONTAL, 2 VERTICAL
        return (symmetry == 1 ? x : width - 1 - x) + (symmetry == 2 ? y : height - 1 - y) * width;
    }

    /** CheeseSpawn rows runCheeseMines wrote in a round's first record: 1 at p within MINE_R of a mine, 2 at sym(p)
     *  next. Rows in a pair cannot be a death's drop (a DieAction follows it); skipped: a remainder (after its
     *  pickup) and a terminated robot's drop buffered ahead of everything (leftoverLoc). */
    int[] mineRows(Act[] L) {
        int[] r = new int[L.length];
        int j = 0;
        while (j < L.length && L[j].t != Action.CheeseSpawn) j++;
        if (j < L.length && L[j].loc == leftoverLoc) j++;
        for (; j + 1 < L.length; j++) {
            if (!bareSpawn(L, j) || !bareSpawn(L, j + 1) || L[j + 1].loc != symLoc(L[j].loc)) continue;
            int x = L[j].loc % width, y = L[j].loc / width;
            boolean near = false;
            for (int m : mineLoc) near |= Math.abs(m % width - x) <= MINE_R && Math.abs(m / width - y) <= MINE_R;
            if (near) { r[j] = 1; r[++j] = 2; }
        }
        return r;
    }

    static boolean bareSpawn(Act[] L, int k) {
        return L[k].t == Action.CheeseSpawn && !(k > 0 && L[k - 1].t == Action.CheesePickup && L[k - 1].loc == L[k].loc);
    }

    static boolean later(Act[] L, int j, byte type, int id) {
        for (int k = j + 1; k < L.length; k++) if (L[k].t == type && L[k].id == id) return true;
        return false;
    }

    static int stunAfter(Act[] L, int j, int id) {
        for (int k = j + 1; k < L.length; k++) if (L[k].t == Action.StunAction && L[k].id == id) return L[k].n;
        return 0;
    }

    void bs(int team, String cause) {
        if (bsPending && backstabBy == 0 && player(team)) { backstabBy = team; backstabCause = cause; }
    }

    void credit(int atk, Bot v, int amt, String how) {
        if (amt <= 0 || !player(v.team)) return;
        if (atk == 0) { ts[v.team].catDmgTaken += amt; return; }
        if (atk == v.team) { ts[v.team].friendlyDmg += amt; return; }
        if (!player(atk)) return;
        TeamStats s = ts[atk];
        if (v.type == KING) s.dmgKing += amt; else s.dmgRat += amt;
        s.dmgBy.merge(how, amt, Integer::sum);
    }

    void death(Bot v, String cause, int killer, int kteam) {
        TeamStats s = ts[v.team];
        if (cause.equals("merge")) { s.ratsMerged++; return; }
        if (v.type == KING) { s.deathsKing++; s.kingDeaths.merge(cause, 1, Integer::sum); }
        else if (v.type == RAT) { s.deathsRat++; s.ratDeaths.merge(cause, 1, Integer::sum); }
        if (cause.equals("exception")) s.exceptions++;
        if (v.type != RAT) return;
        if (player(kteam) && kteam != v.team) s.deathsByEnemy++;
        else if (kteam == v.team) s.deathsSelf++;
        else if (cause.equals("scratch") || cause.equals("crush") || cause.equals("pounce")) s.deathsByCat++;
    }

    void record(Turn t, Act[] L) {
        int id = t.robotId();
        Bot me = bot(id);
        int team = me.team, type = me.type;
        boolean coop = t.isCooperation(), own = src.equals("own");
        if (backstabRound < 0 && !coop) { backstabRound = rn; bsPending = true; }
        TeamStats s = player(team) ? ts[team] : null;
        if (s != null && own) {
            int bc = t.bytecodesUsed(), lim = bcLimit[type];
            if (type == RAT) { s.maxBcRat = Math.max(s.maxBcRat, bc); s.sumBcRat += bc; s.turnsRat++; }
            else if (type == KING) { s.maxBcKing = Math.max(s.maxBcKing, bc); s.sumBcKing += bc; s.turnsKing++; }
            if (lim > 0 && bc >= lim) s.overruns++;
            else if (lim > 0 && bc >= 0.9 * lim) s.near90++;
        }
        // a cat's kills have no action: a pounce moves it >1 tile or adds its movement cooldown twice
        boolean pounce = false;
        if (type == CAT) {
            int disp = Math.max(Math.abs(t.x() - me.x), Math.abs(t.y() - me.y));
            pounce = disp > 1 || disp == 1 && t.moveCooldown() - Math.max(0, me.mcd - 10) >= 40;
        }
        hitVictim = null;
        StringBuilder ind = null;
        int arrival = 0, merged = 0, formKills = 0;
        int[] mine = seq == 0 ? mineRows(L) : new int[L.length];
        for (int j = 0; j < L.length; j++) {
            Act a = L[j];
            boolean mineRow = false;
            switch (a.t) {
                case Action.SpawnAction: {
                    Bot nb = new Bot(a.id, a.team, a.type, a.x, a.y, baseHp[a.type & 3]);
                    bots.put(a.id, nb);
                    if (player(a.team) && a.type == RAT) ts[a.team].spawned++;
                    act(id, "spawn", a.id, TYPE[a.type & 3], a.x + "," + a.y, a.n, a.chir); break;
                }
                case Action.DieAction: {
                    Bot v = bot(a.id);
                    String cause = "other"; int killer = 0, trapOwner = 0;
                    if (a.n == DieType.EXCEPTION) cause = "exception";
                    else if (type != CAT && later(L, j, Action.DamageAction, id) && !(a.id == id && trapBefore(L, j, id) != null)) {
                        // died on the thrown rat's impact (the rat itself, or the robot it hit). hitTarget writes its
                        // DamageAction last: a trap on the landing tile that killed the survivor of the impact comes
                        // before this die row (no trap fires in flight)
                        killer = me.thrower;
                        if (a.id != id) cause = "throwHit";
                        else {
                            int dmg = 0;
                            for (int k = j + 1; k < L.length; k++) if (L[k].t == Action.DamageAction && L[k].id == id) { dmg = L[k].n; break; }
                            cause = dmg == 10 && t.health() == -10 ? "fed" : "throwLand";
                        }
                    } else {
                        for (int k = j - 1; k >= 0 && cause.equals("other"); k--) {
                            Act p = L[k];
                            if (p.t == Action.RatAttack && p.id == a.id) { cause = "bite"; killer = id; }
                            else if (p.t == Action.TriggerTrap && p.victim == a.id) { cause = "trap"; killer = p.placer; trapOwner = p.owner; }
                            // skip side effects of the death: cheese drop, the carried rat's drop, its traps and death
                            else if (p.t == Action.CheeseSpawn || p.t == Action.TriggerTrap || p.t == Action.RatNap && p.id != a.id
                                    || p.t == Action.DieAction) continue;
                            else break;
                        }
                        if (!cause.equals("other")) { }
                        else if (j + 1 < L.length && L[j + 1].t == Action.CatScratch) { cause = "scratch"; killer = id; }
                        else if (type != CAT && later(L, j, Action.UpgradeToRatKing, id)) { cause = v.team == team ? "merge" : "form"; killer = id; }
                        else if (type == CAT) { cause = pounce ? "pounce" : "crush"; killer = id; }
                        else if (a.id == id && me.flying) { cause = "fed"; killer = me.thrower; }
                        else if (a.id == id && type == KING) cause = "starve";
                    }
                    int kteam = cause.equals("trap") ? trapOwner
                            : cause.equals("scratch") || cause.equals("crush") || cause.equals("pounce") ? 0 : teamOf(killer);
                    a.cause = cause;
                    if (cause.equals("merge")) merged++;
                    else if (cause.equals("form")) formKills++;
                    // causes with no damage action of their own: the killing blow is the victim's remaining hp
                    if (cause.equals("crush") || cause.equals("pounce") || cause.equals("form") || cause.equals("fed") || cause.equals("starve")) {
                        if (cause.equals("starve")) ts[v.team].starveDmg += Math.max(0, v.hp);
                        else credit(kteam, v, Math.max(0, v.hp), cause);
                        v.hp = Math.min(v.hp, 0);
                    }
                    if (cause.equals("throwHit")) hitVictim = v;
                    if (v.alive && player(v.team)) death(v, cause, killer, kteam);
                    if (v.alive && player(kteam) && kteam != v.team) {
                        if (v.type == KING) ts[kteam].killsKing++;
                        else if (v.type == RAT) ts[kteam].killsRat++;
                        else if (v.type == CAT) ts[kteam].catsKilled++;
                    }
                    if (cause.equals("fed")) {
                        if (player(teamOf(killer))) ts[teamOf(killer)].feeds++;
                        Bot cat = at(t.x() + me.tdx, t.y() + me.tdy, id, true);
                        act(id, "feed", a.id, cat != null && cat.type == CAT ? cat.id : 0, v.team, killer, teamOf(killer));
                    }
                    v.alive = false; v.uncarry(rn, seq); v.flying = false;
                    act(id, "die", a.id, a.n == DieType.EXCEPTION ? "EXCEPTION" : "damage", TYPE[v.type], v.team, cause, killer, kteam); break;
                }
                case Action.RatAttack: {
                    Bot v = bot(a.id);
                    boolean lethal = later(L, j, Action.DieAction, a.id);
                    int amt = lethal ? Math.max(0, v.hp) : Math.min(BITE, Math.max(0, v.hp));
                    v.hp = lethal ? 0 : v.hp - BITE;
                    if (!lethal && v.type != CAT) v.biteTeam = team;
                    if (s != null) { if (v.type == CAT) s.bitesCat++; else if (v.type == KING) s.bitesKing++; else s.bitesRat++; }
                    if (v.type != CAT) { credit(team, v, amt, "bite"); if (v.team != team) bs(team, "bite"); }
                    act(id, "bite", a.id, TYPE[v.type], v.team, amt, lethal ? 1 : 0); break;
                }
                case Action.DamageAction: {
                    Bot x = bot(a.id);
                    int st = stunAfter(L, j, a.id);
                    boolean eaten = a.n == 10 && t.health() == -10 && !x.alive;
                    String out = eaten ? "eaten" : st == 20 ? "target" : "ground";
                    int thr = x.thrower;
                    act(id, "damage", a.id, a.n, TYPE[x.type], out, thr, teamOf(thr));
                    if (!eaten) {
                        int amt = Math.min(a.n, Math.max(0, a.hp0 != NO_HP ? a.hp0 : x.hp));
                        if (a.hp0 == NO_HP) x.hp -= a.n;
                        if (player(x.team)) ts[x.team].throwDmgTaken += a.n;
                        credit(teamOf(thr), x, amt, "throwLand");
                    }
                    if (out.equals("target")) {
                        Bot v = hitVictim != null ? hitVictim : at(t.x() + x.tdx, t.y() + x.tdy, a.id, false);
                        if (v != null) {
                            int amt = Math.min(a.n, Math.max(0, v.hp));
                            v.hp = hitVictim != null ? 0 : v.hp - a.n;
                            credit(teamOf(thr), v, amt, "throwHit");
                            act(id, "throwHit", v.id, amt, TYPE[v.type], v.team, a.id, thr, teamOf(thr));
                        }
                        hitVictim = null;
                    }
                    break;
                }
                case Action.CheesePickup: {
                    int before = tile[a.loc], rem = -1, exact = -1;
                    if (j + 1 < L.length && L[j + 1].t == Action.CheeseSpawn && L[j + 1].loc == a.loc) { rem = L[j + 1].n; L[j + 1].remainder = true; }
                    for (int k = j + 1; k <= j + 2 && k < L.length; k++) if (type == KING && L[k].t == Action.CheeseTransfer && L[k].id == id) exact = L[k].n;
                    int amt = Math.max(0, rem >= 0 ? before - rem : before);
                    if (exact >= 0) { if (exact != amt) cheeseModelErr++; amt = exact; }
                    tile[a.loc] = Math.max(0, rem);
                    if (s != null) { s.pickups++; s.pickupAmt += amt; picked[team] += amt; }
                    act(id, "pickup", loc(a.loc), amt, ""); break;
                }
                case Action.CheeseTransfer: {
                    if (a.id == id && type == KING) {
                        if (s != null) { s.kingPickups++; s.kingPickupAmt += a.n; }
                        act(id, "kingPickup", a.id, a.n, ""); break;
                    }
                    if (s != null) { s.transfers++; s.transferAmt += a.n; }
                    act(id, "transfer", a.id, a.n, ""); break;
                }
                case Action.CheeseSpawn: {
                    String kind; int delta;
                    if (a.remainder) { kind = "remainder"; delta = 0; }
                    else if (mine[j] > 0) { kind = "mine"; delta = MINE; mineRow = true; }
                    else { kind = "drop"; delta = a.n; }
                    if (mineRow) {
                        // the engine writes 20 + the tile's cheese before the pair's additions
                        int before = a.n - MINE + (mine[j] == 2 && L[j - 1].loc == a.loc ? MINE : 0);
                        if (tile[a.loc] != before) cheeseModelErr++;
                        tile[a.loc] = before + MINE; mined += MINE;
                    }
                    else if (kind.equals("drop")) { tile[a.loc] += a.n; dropped += a.n; }
                    act(mineRow ? 0 : id, "cheesespawn", loc(a.loc), a.n, "", kind, tile[a.loc], delta); break;
                }
                case Action.PlaceTrap: {
                    if (s != null) { if (a.rat) s.ratTraps++; else s.catTraps++; }
                    if (player(a.team)) traps.put(a.loc * 4 + a.team, new Trap(a.rat, id, trapOrder++));
                    act(id, a.rat ? "ratTrap" : "catTrap", loc(a.loc), a.team, ""); break;
                }
                case Action.TriggerTrap: {
                    // TriggerTrap.team is the TRIGGERING robot's team: rat traps fire only on enemies, cat traps only on cats
                    int owner = 0;
                    Trap tr = null;
                    if (a.team != 0) { owner = 3 - a.team; tr = traps.remove(a.loc * 4 + owner); }
                    else {
                        Trap t1 = traps.get(a.loc * 4 + 1), t2 = traps.get(a.loc * 4 + 2);
                        if (t1 != null && t1.rat) t1 = null;
                        if (t2 != null && t2.rat) t2 = null;
                        // the engine walks the tile's trigger list backwards: the later trap fires first
                        owner = t1 == null && t2 == null ? 0 : t1 == null ? 2 : t2 == null ? 1 : t1.order > t2.order ? 1 : 2;
                        if (owner > 0) tr = traps.remove(a.loc * 4 + owner);
                    }
                    if (player(owner)) { ts[owner].trapTriggers++; if (a.team != 0) ts[owner].ratTrapHits++; else ts[owner].catTrapHits++; }
                    Bot v = null;
                    if (arrival != 0 && (a.team == 0 ? typeOf(arrival) == CAT : teamOf(arrival) == a.team)) v = bot(arrival);
                    else if (a.team == 0 ? type == CAT : team == a.team) v = me;
                    int amt = 0;
                    if (v != null && v.alive) {   // processTrapsAtLocation keeps firing traps on a robot it killed
                        int nom = a.team != 0 ? RAT_TRAP_DMG : CAT_TRAP_DMG, k = impactAfter(L, j, v.id);
                        // hitTarget applies the impact before the landing's traps: apply it here, the trap gets the rest
                        if (k >= 0 && L[k].hp0 == NO_HP) { L[k].hp0 = v.hp; v.hp -= L[k].n; }
                        amt = Math.min(nom, Math.max(0, v.hp));
                        v.hp -= nom;
                        if (v.type == CAT) { if (player(owner)) ts[owner].catDmgTrap += amt; }
                        else credit(owner, v, amt, "trap");
                    }
                    a.victim = v == null ? 0 : v.id; a.owner = owner; a.placer = tr == null ? 0 : tr.placer;
                    if (a.team != 0) bs(owner, "trap");
                    act(id, "trigger", loc(a.loc), a.team, "", owner, a.victim, amt, a.team != 0 ? "rat" : "cat"); break;
                }
                case Action.RemoveTrap: {
                    traps.remove(a.loc * 4 + a.team);
                    act(id, "removeTrap", loc(a.loc), a.team, ""); break;
                }
                case Action.BreakDirt: {
                    if (s != null) s.dirtDug++;
                    act(id, "dig", loc(a.loc), "", ""); break;
                }
                case Action.PlaceDirt: {
                    if (s != null) s.dirtPlaced++;
                    act(id, "placeDirt", loc(a.loc), "", ""); break;
                }
                case Action.RatSqueak: {
                    if (s != null) s.squeaks++;
                    act(id, "squeak", loc(a.loc), "", ""); break;
                }
                case Action.RatNap: {
                    // RatNap toggles the target's carried state (grab, drop, release, swap) and also marks a landing
                    Bot x = bot(a.id);
                    if (j > 0 && L[j - 1].t == Action.DamageAction && L[j - 1].id == a.id) {
                        int st = stunAfter(L, j, a.id);
                        boolean eaten = L[j - 1].n == 10 && t.health() == -10 && !x.alive;
                        x.flying = false; x.carrier = 0; arrival = a.id;
                        act(id, "land", a.id, eaten ? "eaten" : st == 20 ? "target" : "ground", x.alive ? 0 : 1, x.thrower, teamOf(x.thrower));
                    } else if (a.id == id) {
                        int c = x.carrier;
                        String why = c == 0 ? "other" : !bot(c).alive ? "carrierDead"
                                : j + 1 < L.length && L[j + 1].t == Action.RatNap && L[j + 1].id == c ? "swap" : "wriggle";
                        a.cause = why;
                        x.uncarry(rn, seq); arrival = a.id;
                        if (s != null) s.releases++;
                        act(id, "release", a.id, c, why);
                    } else if (x.carrier != 0) {
                        int c = x.carrier, k = nextMain(L, j, a.id);
                        String why = k < L.length && L[k].id == c && L[k].t == Action.RatNap ? "grabbed"
                                : k < L.length && L[k].id == c && L[k].t == Action.DieAction ? "killed" : c == id ? "carrier" : "other";
                        x.uncarry(rn, seq); arrival = a.id;
                        if (why.equals("carrier")) { if (s != null) s.drops++; }
                        else if (player(teamOf(c))) ts[teamOf(c)].forcedDrops++;
                        act(id, "drop", a.id, x.team, why, c, teamOf(c));
                    } else if (j > 0 && L[j - 1].t == Action.RatNap && L[j - 1].id == id && "swap".equals(L[j - 1].cause)) {
                        x.carrier = id;
                        act(id, "swap", a.id, x.team, "");
                    } else {
                        x.carrier = id;
                        boolean enemy = player(x.team) && x.team != team;
                        if (s != null) { s.naps++; if (enemy) s.grabsEnemy++; else s.grabsAlly++; }
                        if (enemy) bs(team, "grab");
                        act(id, "nap", a.id, x.team, "", enemy ? "enemy" : "ally");
                    }
                    break;
                }
                case Action.ThrowRat: {
                    Bot x = bot(a.id);
                    if (x.thrower != id || x.carrier != 0) {   // not paired with a thrown record: set the flight here
                        x.thrower = id; x.tdx = a.loc % width - me.x; x.tdy = a.loc / width - me.y; x.carrier = 0; x.flying = true;
                    }
                    if (s != null) s.throwsN++;
                    act(id, "throw", a.id, loc(a.loc), ""); break;
                }
                case Action.CatFeed: {
                    catSleepTurns++;
                    act(id, "catSleep", a.id, "", ""); break;
                }
                case Action.UpgradeToRatKing: {
                    if (s != null) s.kingsFormed++;
                    me.type = KING;
                    act(id, "upgradeKing", a.id, "", "", merged, formKills); break;
                }
                case Action.CatScratch: {
                    Bot v = j > 0 && L[j - 1].t == Action.DieAction && L[j - 1].cause.equals("scratch") ? bot(L[j - 1].id)
                            : at(a.loc % width, a.loc / width, id, false);
                    int amt = 0;
                    if (v != null) { amt = Math.min(SCRATCH, Math.max(0, v.hp)); v.hp -= SCRATCH; credit(0, v, amt, "scratch"); }
                    act(id, "scratch", loc(a.loc), "", "", v == null ? 0 : v.id, v == null ? 0 : v.team, amt); break;
                }
                case Action.CatPounce: {   // never written by engine 1.2.6
                    act(id, "pounce", loc(a.loc), loc(a.loc2), ""); break;
                }
                case Action.StunAction: {
                    act(id, "stun", a.id, a.n, ""); break;
                }
                case Action.RatCollision: {   // never written by engine 1.2.6
                    act(id, "collision", loc(a.loc), "", ""); break;
                }
                case Action.IndicatorStringAction: {
                    if (ind == null) ind = new StringBuilder(); else ind.append(" | ");
                    ind.append(a.s); break;
                }
                case Action.IndicatorDotAction:
                case Action.IndicatorLineAction: {
                    indicator(id, a); break;
                }
                default: break;
            }
            if (!mineRow && a.t != Action.TriggerTrap && a.t != Action.CheeseSpawn && a.t != Action.DieAction && a.t != Action.RatNap) arrival = 0;
        }
        if (bsPending) { if (backstabBy == 0) { backstabBy = team; backstabCause = "?"; } bsPending = false; }
        if (turns != null)
            turns.printf("%d\t%d\t%d\t%s\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%s\t%d\t%s\t%d\t%d%n", rn, id, team, TYPE[type], t.x(), t.y(), t.dir(),
                    t.health(), t.cheese(), t.moveCooldown(), t.turningCooldown(), t.actionCooldown(), t.bytecodesUsed(),
                    coop ? 1 : 0, ind == null ? "" : clean(ind.toString()), seq, src, t.health() <= 0 ? 1 : 0, src.equals("carried") ? carrierOfRec : me.carrier);
        // hp lost with no action: a king's starvation (10, in its own record) and the cheese boost of a bite
        int rest = Math.max(0, me.hp) - Math.max(0, t.health());
        if (rest > 0 && type == KING && own && rest >= 10 && s != null) { s.starveDmg += 10; rest -= 10; }
        if (rest > 0 && me.biteTeam != 0) credit(me.biteTeam, me, rest, "bite");
        me.biteTeam = 0;
        me.x = t.x(); me.y = t.y(); me.hp = t.health(); me.cheese = t.cheese(); me.mcd = t.moveCooldown();
    }

    void indicator(int id, Act a) throws RuntimeException {
        if (outDir == null) return;
        try {
            if (indicators == null) {
                indicators = new PrintWriter(new BufferedWriter(new FileWriter(outDir + "/g" + game + ".indicators.tsv")));
                indicators.println("round\tid\tteam\tkind\ta\tb\tcolor\tseq");
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
        boolean line = a.t == Action.IndicatorLineAction;
        indicators.printf("%d\t%d\t%d\t%s\t%s\t%s\t%06x\t%d%n", rn, id, teamOf(id), line ? "line" : "dot", loc(a.loc), line ? loc(a.loc2) : "", a.n & 0xffffff, seq);
    }

    void matchFooter(MatchFooter f) throws IOException {
        int[] g = lastRoundAgg;
        String winner = f.winner() == 1 ? "A" : f.winner() == 2 ? "B" : "?";
        for (int t = 1; t <= 2; t++) { ts[t].dmgCat = g[1 + t]; ts[t].catDmgBite = g[1 + t] - ts[t].catDmgTrap; }
        String line = String.format("{\"file\":%s,\"game\":%d,\"map\":%s,\"w\":%d,\"h\":%d,\"teamA\":%s,\"teamB\":%s,\"winner\":\"%s\","
                        + "\"winType\":\"%s\",\"rounds\":%d,\"backstabRound\":%d,\"backstabBy\":%d,"
                        + "\"cheeseXferA\":%d,\"cheeseXferB\":%d,\"catDmgA\":%d,\"catDmgB\":%d,\"kingsA\":%d,\"kingsB\":%d,"
                        + "\"babiesA\":%d,\"babiesB\":%d,\"A\":%s,\"B\":%s,\"backstabCause\":%s,\"packageA\":%s,\"packageB\":%s,"
                        + "\"maxRounds\":%d,\"numKingsA\":%d,\"numKingsB\":%d,\"bankA\":%d,\"bankB\":%d,\"engBabiesA\":%d,\"engBabiesB\":%d,"
                        + "\"catSleepTurns\":%d,\"markers\":%d,\"profilerFiles\":%d,\"cheeseModelErr\":%d,\"dumpVersion\":%d}",
                q(file), game, q(mapName), width, height, q(teamName[1]), q(teamName[2]), winner,
                f.winType() >= 0 && f.winType() < WIN.length ? WIN[f.winType()] : "?", f.totalRounds(), backstabRound,
                backstabBy, g[0], g[1], g[2], g[3], g[4], g[5], g.length > 8 ? g[8] : g[6], g.length > 8 ? g[9] : g[7],
                ts[1].json(), ts[2].json(), q(backstabCause), q(pkg[1]), q(pkg[2]), maxRounds, g[4] % 10, g[5] % 10, g[4] / 10, g[5] / 10,
                g[6], g[7], catSleepTurns, f.timelineMarkersLength(), f.profilerFilesLength(), cheeseModelErr, DUMP_VERSION);
        System.out.println(line);
        if (rounds == null) return;
        try (PrintWriter w = new PrintWriter(outDir + "/g" + game + ".summary.json")) { w.println(line); }
        if (f.timelineMarkersLength() > 0) {
            try (PrintWriter w = new PrintWriter(outDir + "/g" + game + ".markers.tsv")) {
                w.println("round\tteam\tlabel\tcolor");
                TimelineMarker tm = new TimelineMarker();
                for (int i = 0; i < f.timelineMarkersLength(); i++) {
                    f.timelineMarkers(tm, i);
                    w.printf("%d\t%d\t%s\t%06x%n", tm.round(), tm.team() + 1, clean(String.valueOf(tm.label())), tm.colorHex() & 0xffffff);
                }
            }
        }
        rounds.close(); turns.close(); actions.close();
        if (indicators != null) indicators.close();
        rounds = turns = actions = indicators = null;
    }

    void gameFooter(GameFooter f) throws IOException {
        if (outDir == null) return;
        try (PrintWriter w = new PrintWriter(outDir + "/game.json")) {
            w.printf("{\"file\":%s,\"specVersion\":%s,\"teamA\":%s,\"teamB\":%s,\"packageA\":%s,\"packageB\":%s,\"games\":%d,\"winner\":\"%s\"}%n",
                    q(file), q(spec), q(teamName[1]), q(teamName[2]), q(pkg[1]), q(pkg[2]), game + 1,
                    f.winner() == 1 ? "A" : f.winner() == 2 ? "B" : "?");
        }
    }
}
