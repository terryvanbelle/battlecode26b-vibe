# Training log (append-only; one entry per attempt or foundation step, newest last)

## F0 foundation, 2026-10-27 22:54 PDT
- Engine 1.2.6 pinned; RULES.md engine digest (24 spec/engine disagreements); map census (74 maps).
- Runner tools/arena.py (VM, unshare -rn, slot semaphore, code-hash keyed rows); ReplayDump; game_report.py.
- ratbot v0: G/Sym/Comms/Nav/King/Baby, cat traps, compute monitor.
- Basics battery vs examplefuncsplayer (never spawns, starves ~r1310), all 74 maps x both sides:
  - v0b (runs/v0b_vs_ex): 131-17. Losses: king starved (no deliveries: rats never faced the king; king boxed in
    dirt on EscapeTheNight and closeup; king never heard squeaks because it acts before its babies).
  - Fixes: dig toward target, face king to deliver, read previous-round squeaks, king digs blocked spawn ring,
    bug-nav start heading (unit test caught a 2x2 orbit).

## F1 economy budget + field, 2026-10-27 23:45 PDT
- v0c/v0d batteries vs example: 130-18, then 134-14 (nav orbit fix, digRing, stand-off delivery: coin flips gone).
- Remaining losses: our king died first. The example never spawns: its king just eats the starting ~2500 cheese.
  Ours either starved (spent on spawns/traps, income < upkeep on cat-heavy maps) or was killed by a cat.
- v0e Econ (spawn only while projected end cheese at 75% of measured income stays positive; traps/cheese bites
  gated on the kings' published surplus): **98-50, much worse**. Cause: early traps blocked, so cats killed our king
  before the example's. Lesson: cat damage near our king is survival, not luxury (see LEARNINGS 6).
- v0f: traps allowed on a 300-cheese floor whenever the cat is within dist2 100 of our king (battery running).
- Field: 64 repos scanned (no hostile code), 1,057 packages compiled; 56 chosen by package name (finals/quals/latest)
  -> research/field/FIELD.tsv; 53 seeded on the replica ladder (tools/galaxy26/field-teams.tsv), activity + autoscrim on.
- Gauntlet s0e vs the top 5 (partial, 80 games): **0-80**. Every loss is RATKING_DESTROYED by a rush: backstab at
  round 13-36, 20-60 king bites, rat traps 10-80, throws 20-140, our rats die one-for-one with our spawns.
  Our babies never moved toward an enemy. Combat is the missing basic.
- s0g war mode: babies bite the best adjacent enemy (king first, then lowest HP; turn to face), close on visible
  enemies once cooperation ends, answer a king's alert slot; the king spawns freely toward enemies under attack.

## F2 (2026-10-28 00:48 PDT): why we lose to the top field
- s0m (core 12, trap floor 500) 0/28 vs the top six; s0n (+rush) 1/29; s0o (+Micro) 0/54.
- Diagnostic s0m vs Version41 (DefaultSmall): kills are about even (35 vs 37) and our rat traps hit 66 times,
  but Version41 delivered 1840 cheese to our 145 (102 pickups vs 20). Cheese spawns mostly in the middle, which they
  hold. They also ratnap about 3x more (67 vs 23 in the s0r game) and bite twice as often; our carriers get grabbed
  in the 2-3 rounds before they can throw.
- s0p/s0q: gatherers ignore non-adjacent enemies in war (WAR_GATHER 1/2). s0r: s0p + POUNCE (fighters target an enemy
  rat weaker than themselves, which they can ratnap) + a carry attempt after moving. One diagnostic game each: pickups
  did not rise (20), so holding the middle is the real problem.

## F3 (2026-10-28 02:11 PDT): starvation, traps, idle fighters
- s0p/s0q 0/33, s0r 0/50 (top six); s0r vs the whole field on 2 maps: 8/89, wins only vs the two justinottesen bots.
- s0s (core 18, trap floor 1000) 0/42, s0t (no baby traps) 0/40, s0u (+retreat at <=40 HP) 0/48.
- Diagnostics: kings starve (spawning down to 200 cheese), enemy traps are the largest killer, fortress fighters idle.
- s0v fortress; s0w/s0x + hard spawn floor 600 (king lives, but the army is gone); s0y/s0z + TrapRisk.
- v0d (best vs example) wins 27 of the 37 example maps s0m lost; s0u wins 13. The conservative v0d spawner suits
  passive opponents.

## F4 (2026-10-28 03:09 PDT): economy does not matter, grabs from behind do
- s0y (fortress + TrapRisk) 0/48 pts 21, s0z (no fortress) 0/48 pts 32. The fortress is off from now on.
- Economy sweep s1a-s1f (core 14/18, floors 150/300/600, trap minimum 400/1000) vs Version41, Icarus, Finals,
  finalsbot on 3 maps: all 0/24, mean points 15-21. The spawn economy is not the lever.
- ReplayDump bug: the engine also emits RatNap for a thrown rat's own landing, so the dumped "naps" mixed grabs with
  landings. Fixed ('land' rows). Real grabs, s1g vs Version41: 14 for us, 38 for them (our rats were thrown 31 times,
  theirs 8). Vs Icarus: 32 vs 45.
- Most of their grabs take our rat from behind (outside its cone) and, against Icarus, half land on 50-HP rats just
  hit by a trap. Our Nav turns to face every step, so a retreating or gathering rat shows its back.
- s1h/s1i: guard facing. With an enemy within d2 13 (s1i: 20), steps strafe so we keep facing it, and every turn
  ends facing the nearest enemy rat.

## F5 (2026-10-28 03:22 PDT): idle in contact, starving on the floor
- s1h vs Version41, rounds 85-115: our rats were within d2 20 of an enemy for 397 rat-rounds but bit 10 times, grabbed
  9, threw 4 and laid no traps (they: 18 traps, 24 grabs, 18 throws, often throwing our rats onto traps). 200 of
  the 350 engaged rat-rounds were gatherers still gathering (WAR_GATHER 1), and TrapRisk marks every enemy's front
  tiles, so fighters could never step next to an enemy facing them.
- s1j: king bites in combat with up to 225 cheese (+15), babies pay k*k for a finishing bite.
- s1k: everyone fights (WAR_GATHER 0). s1l: also no trap-risk mask while an enemy is within d2 20. Damage dealt in
  the battle window rose from 390 to 870-1080.
- s1m: flank (step onto an enemy's blind neighbour facing it, grab after moving). Vs Icarus: grabs 75 vs 62,
  throws 47 vs 29, kills even, still lost: from r100 the king sat on its 600 spawn floor and never reinforced,
  then starved with no rats (Icarus spent down to 365 and kept 12-16 rats).
- s1n: under 8 babies every floor drops to 100. Vs Icarus still lost (transferred 860 vs 2500; mid-game they pick
  up more cheese on our side than we do). s1o: s1n with WAR_GATHER 1.
- The ghi gauntlet was stopped at 48/144 (rift games run 2000 rounds, about 300 s each); s1g: 0/46, pts 30.0.

## F6 (2026-10-28 03:51 PDT): kill ratio as the combat metric; traps win fights, the economy loses games
- gauntlet summary now reports kills/deaths (enemy rat deaths / ours) and mean rounds. Same 3-map top-six panel:
  s1g pts 21.2 kd 1.26; s1l 18.5 / 1.03; s1n 20.7 / 0.77; s1q 18.3 / 0.76; s1s 17.0 / 0.78; s1w 19.5 / 0.83;
  s1x 17.2 / 1.52. The "aggressive" changes after s1g lowered the kill ratio; single-game impressions misled.
- s1j bug (fixed in s1q): a king under attack bit with 225 cheese every turn and never spawned (knifefight: 2225 to
  450 cheese in 10 rounds). The king now spawns first and pays bite cheese only for a kill.
- s1w: throws discount targets beyond tile 2 (they move first) and hold the rat until a point-blank (42) throw.
  Our throw damage went from mixed 10-38 to mostly 42, like the top bots.
- s1x (rat traps down to 200 cheese) doubled the kill ratio; in diagnostics our traps dealt up to 2400 damage.
- Cheese tracing (who picks up each spawn): through r100 each side collects its own half; after r100 the
  opponent takes most of ours. Unopposed, our gathering rate equals Icarus's (about 690 cheese per 100 rounds).
  We lose the economy by overbuilding early (26 rats by r50 vs 16), spending heavily on traps, then sitting on the
  spawn floor while the army dies.
- New: battle_report.py; standoff (hold against a watching, not weaker enemy); TRAP_CAP; s1y-s2f variants.

## F7 — raiders (2026-10-28 04:01 PDT)
- s1y 17.8 pts kd 1.48, s1z (standoff) 16.5 kd 1.71: traps and standoffs trade well but do not move points.
- Replays: from r50 ~8 enemy rats fight on our half; our rats spend ~2% of rat-rounds on theirs, where they collect ~24 piles per 100 rounds unopposed.
- Added Cfg.RAID: one in RAID_EVERY fighters is a raider; on our half it ignores enemies beyond dist2 8 and heads to an enemy-half mine (known mines or mirrors of ours), wanders there RAID_STAY rounds. First game: raiders reach the midline and stall on trap-risk marks / fight there. s2g (every 2nd fighter), s2h (every fighter) queued (runs/g6_raid) after g6_s2.
- Bytecode: Debug.mark(k) stage markers locate overruns (OVS=, first stage reached after the round advanced) and the costliest segment (hot=). Overruns were the post-move throw scan (called with a wasted full sense) and TrapRisk.mask (72 lookups). mask is now one 5x5 pass (test: equals risky() everywhere), the post-move throw needs 7000 bytecodes left. Overruns per game vs Icarus 11-48 -> 1. s2i/s2j = s2g/s2h with the fix; they replace s2g/s2h in runs/g6_raid.
- Throw scan reads a 17x17 offset grid of the robots we see (kings 3x3, cats 2x2) instead of sensing tile by tile (+2 tests: king edge, ally on path). Vs Icarus: overruns 0, >90% turns 48 -> 16.
- Cheese is a race: no pile is left over; Icarus takes all of its half and from r100 more than half of ours (cheesefate). Our gatherers are in 'g' 87% of the time. Micro gained a cheese-step bonus (MICRO is off, so inert). s2k = s2i + idle fighters hold our mines (FIGHT_MINE); s2l = s2k + WAR_GATHER 2 (gatherers react only to adjacent enemies). Queued as runs/g6_kl after g6_raid.
- Raid gauntlet (top-6, 3 maps): s2i 17.7 pts kd 0.91, s2j 14.9 kd 0.88 vs s2e 19.1/0.91. Raiders cost points; RAID_EVERY 0 in dev.
- Found: the king knows no mine until ~r180 on DefaultMedium (km=n0 tag) - shared mine list is empty, so gatherers rely on mines they saw themselves. Investigating.
- BUG (big): after the backstab every baby seeing an enemy squeaked SQ_TRAP and returned, so mine/symmetry reports never reached the king: no shared mines until ~r180, symmetry unresolved. report() now gives king-bound facts priority when the king is within dist2 16 (hearing range; was 9). Kings now know mines by r25-40. One game each vs Icarus: DefaultMedium 29 pts (was 7-8), DefaultSmall 11 pts but 592 rounds, 1330 cheese (was ~500).
- King walks onto the nearest home-half mine within dist2 400 (KING_MINE), not while enemies or cats are in view.
- s2m = dev (comms fix, KING_MINE, FIGHT_MINE, raid off); s2n = s2m without KING_MINE.
- Bytecode: post-move throw scan only after a move; flight damage table; trap squeaks capped at 8/turn. DefaultMedium vs Icarus overruns 22 -> 20 (spread over stages: heavy turns are the sum, not one hot spot).
- Second king (FORM_KING): king 0 names a site (nearest clearly-ours mine with a clear 3x3, else 4 tiles behind it) once it has 16 babies and 300 cheese after r80; babies within dist2 72 walk into the 3x3, the centre rat promotes. First games vs Icarus: Medium formed at r100 (412 HP) on a mid-map mine and lost sooner; site rule tightened. s2p = s2m + formation (r80, 16 babies), s2q = r40, 12 babies.
- Gauntlet g6_mno: s2o 23.6 pts (best so far), s2m 22.4, s2n (no KING_MINE) 11.9. The king on a home mine is worth ~10 pts; WAR_GATHER 2 helps a little. Dev adopts WAR_GATHER 2. Weakest: cheesefarm and DefaultSmall vs Icarus/ThisGuy.
- King sidesteps babies on its way to the mine (up to 90 degrees off, never farther); kings skip a mine another king holds. s2r = dev (WAR_GATHER 2 + formation + these), s2s = s2r without formation.
- Formation gauntlet: s2p 18.9, s2q 21.4 vs s2m 22.4; games shorter (~290 vs 347 rounds). Seven rats for a king costs more than it returns against these opponents. FORM_KING off in dev.
- King roam (KING_ROAM 4): with no enemy/cat in view the king walks to cheese within Chebyshev 4 of its mine (finalsbot's king roams its mines; cheesefarm xfer 5830 vs our 1512). One game vs finalsbot cheesefarm 25 pts (s2o: 9). s2t = s2s + roam.
- g6_rs/g6_t: s2s 24.4 (best), s2t (roam) 23.7, s2r (formation) 20.6. Roam within noise (SE ~2); kept in dev.
- s2s vs Version41 DefaultMedium: our king dies r180-213 after our army is wiped (10 -> 2 rats). Trap damage taken 2372 vs dealt 802; 42 of 45 of our trap triggers are on our half (Version41 lays 47 traps there, we 24); gatherers walking at mines trigger most. s2u = s2t + TRAP_CAP 25, RAT_TRAP_MIN_CHEESE 100; s2v = s2t + RISK_IN_FIGHT. Also running s2s/s2t on Excavation/Stash/UncomfortablyClose (runs/g6_st2).
- snapshot.sh now rejects a source without java files (it had created an empty dir with a literal '*.java'); test added.
- GATHER_STILL (gatherers turn in place at their mine instead of wandering): vs Version41 DefaultMedium our trap triggers 51 -> 34 but cheese 414 -> 275, pts 31 -> 30; DefaultSmall 36 pts. Snapshot s2w for a gauntlet.
- New maps (Excavation/Stash/UncomfortablyClose, top-6, 36 games): s2s 25.6 pts (2 wins, both vs Icarus), s2t 25.3 (4 wins). First wins. Per map: UncomfortablyClose ~45, Excavation ~13, Stash ~15 -> diagnose Excavation/Stash.
- Trap variants on base maps: s2u (TRAP_CAP 25, min cheese 100) 20.1 pts, kd 1.82 (more kills, but the spend loses the economy race); s2v (RISK_IN_FIGHT) 22.3. Both below s2t 23.7 -> rejected.
- s2w (GATHER_STILL) 22.8 pts / kd 1.01 vs s2t 23.7: rejected (GATHER_STILL=false in dev).
- Excavation (39x39, 35% dirt) and Stash (30x30, each king sealed behind a wall row whose only exits are dirt corner plugs reached through a dirt serpentine): s2s digs 128 tiles on Excavation; s2x (no eager dig) delivers 0 cheese on Stash in 667 rounds. Nav was greedy + bug only. New Flow.java: dig-aware Dijkstra flow field (dirt cost 4, unknown open) over remembered terrain, built with spare bytecode after FLOW_STALL turns without progress and followed once complete; replaces eager digging.
- Flow v1 (full-map Dijkstra field) took ~27 turns per build on 30x30 (5k spare bc/turn): useless. Rewrote as A* from the rat (Dial buckets on f, unknown open, dirt cost 4), path cached and followed (digging), rat holds still while its search runs (otherwise it strands the path), rejoins from adjacent tiles. Builds now 0-13 turns.
- Excavation: all 3 mines buried mid-block, >4 tiles from open ground, so they are never seen. Explore now picks the nearest never-seen of 12 samples, and once no mine is known by r30 the never-seen sample nearest the map centre. Courier: when the team knows no mine, a rat that found one walks home to report it (squeaks only reach the king from dist2 16). Single games still poor (Excavation: king learns a mine ~r160); snapshot s2y for a gauntlet.
- All-maps census, s2t vs Version41 (74 maps x 2 sides): mean 24.0 pts, 5/148 wins (research/maps_s2t_vs_v41.tsv). Worst 12: trapped pipes Stash Excavation EscapeTheNight toomuchcheese wallsofparadis Nofreecheese Meow closeup cheesefarm FreeTheCheese . Queued s2y on these 12 (runs/g7_worst).
- s2y gauntlet: base 18.8 (s2t 23.7), new maps 23.8 (s2t 25.3); Excavation 14.5, Stash 14.4 barely moved. Rejected. Trace found rats frozen forever by "hold while the search runs" when searches never finish (little spare bc). Dev now: search timeout FLOW_MAX_TURNS 12 + retry backoff 30, mine gossip relay while the team knows no mine (king learns a mine r75 on trapped vs r175, r50 on Excavation), cheese on dirt picked up from the adjacent tile.

## 2026-10-28 06:30 PDT: s2y on the 12 worst maps; progress/ restructure
- s2y vs Version41 on the 12 worst census maps (2 games each side): mean 7.5 vs s2t 6.8. The gain is all toomuchcheese
  (26.5 vs 6.5); trapped 2.5, pipes 3.0, Stash 5.0, Excavation 4.5 are unchanged or worse. A* flow alone does not fix
  dirt maps: rats reach the mines but gatherers still fail to deliver.
- progress/ now follows battlecode23-vibe's layout (ATTEMPTS.md, attempts.csv, games.csv, ELO.md/elo.png, ladder.md/png,
  rating-history.md/png, progress.png; `tools/progress-refresh.sh`). The Bradley-Terry ladder is weak for us because
  local games are win/loss and we rarely beat the top 6; the points share is still the main signal.

## 2026-10-28 06:55 PDT: overruns fixed; trapped economy trace
- Newborn first-turn overruns (20/game on trapped, all on a rat's first turn): TrapRisk allocated one int per tile
  (~9.3k bc) on top of the first full sense. Fix: defer the allocation until a turn has 3*W*H + 8000 bc left; skip
  optional work under 8000 left; bail under 3500 left after sensing; Flow.finish() waits a turn when the backtrack
  would not fit. Rerun on trapped: overruns 20 -> 0, max rat bc 17503 -> 16043.
- New move tags inside gather: D/d deliver (at king / walking), P cheese-on-dirt pickup, C cheese target, M to mine,
  W at mine, E explore.
- trapped (50x50, our king sealed in a pocket: 5 dirt columns east, 6 dirt rows south): gatherer turns r50+ are
  mostly M (1448, 37% standing still) and d (700, 32% still); W only 411, C 72. Stalls cluster on the south dirt band
  (y 37-43). Our 51 digs are scattered over 3+ partial tunnels (east, south-east, south) versus Version41's 128 digs;
  12 transfers all game. Hypothesis: each rat plans its own A* path to its own mine from its own map memory, so
  tunnels diverge and none is finished early. Candidate: one shared tunnel per king (king plans, broadcasts waypoints).

## 2026-10-28 07:25 PDT: s2z gauntlet
- s2z (s2y + Flow search timeout/backoff, mine gossip, cheese-on-dirt pickup) vs the top 6, 72 games on 6 maps:
  base 3 maps 23.2 pts (s2s 24.4), new 3 maps 30.0 (s2s 25.6); kills/deaths 1.05. Per map: UncomfortablyClose 47.6,
  DefaultMedium 34.3, Stash 25.8, DefaultSmall 25.2, Excavation 16.8, cheesefarm 10.0.
- vs Version41 on the 12 worst census maps: 8.0 (s2y 7.5, s2t 6.8). Within noise of s2s overall (26.6 vs 25.0):
  inconclusive, kept as the dev base (the overrun fix sits on top of it). The waiter that was meant to report this
  looped on an unset VM address (no ensure_vm) and never printed; re-armed waiters now call ensure_vm first.
- Next: the shared-tunnel economy for dirt-sealed maps (the king plans one dig path to the nearest mine and broadcasts
  it), then cheesefarm (10.0, our worst base map).

## 2026-10-28 07:55 PDT: s3a gauntlet, progress chart, dev work
- s3a (s2z + bytecode overrun fix + DIG_FIRST: a blocked rat digs toward its target before the Flow search) vs the
  top 6, 72 games: base 3 maps 24.6 pts (s2z 23.2), new 3 maps 27.6 (s2z 30.0), kd 1.01, 2 wins. Per map:
  UncomfortablyClose 41.8, DefaultMedium 31.4, DefaultSmall 29.2, Stash 21.8, Excavation 19.1, cheesefarm 13.2
  (s2z 10.0). Worst-12 vs Version41: 10.6 (s2z 8.0; trapped 22.5, Nofreecheese 26.5). Inconclusive overall but
  cheesefarm and the worst-12 both moved up; DIG_FIRST stays in dev.
- progress.png redone in bc23's layout (owner prompt #7) on a map-aware Bradley-Terry ladder: local games count their
  points share and our-vs-field games carry a per-map handicap, so builds gauntleted on different maps compare. It is
  flat: v0d/s0e 1610, s2o 1591, s2s/s2t/s2z/s3a 1565-1575, all inside each other's 95% intervals. Hardest maps for
  us (ELO.md): clockwisdom -304, the replica rotation -183, Stash -164, closeup -102, cheesefarm -97, Excavation -84.
- Dev since s3a (diagnostics on cheesefarm vs Version41): king defense (the king broadcasts how many enemy rats are
  within d2 18; gatherers within d2 100 of a threatened king fight instead of gathering; cf3 king lived to r298 vs
  r138), mine spread (gatherers pick one of the 4 nearest home-half mines at random instead of all taking the
  nearest), Flow.follow fix (a path starting beside us threw ArrayIndexOutOfBounds every turn: 56 turns of one rat
  in one game), exception tags in the debug line (EXC=count:type@stage). Version41 still picks up ~2x our cheese on
  our own half of cheesefarm.

## 2026-10-28 08:30 PDT: the screen metric was wrong; why we lose
- s3c (s3b + centre-first explore) top-6 screen vs s3b: points t +0.4; worst-12 flat. s3d (+ king digs) stopped at
  10/72 when the screen changed (below).
- **Metric bug.** `arena.points` took the "kings" share from the replay's kingsA/kingsB aggregate, which is
  10 * global cheese + kings (6391 = 639 cheese + 1 king). A game our king lost scored us ~25-45 "points" for cat
  damage and leftover cheese (DefaultMedium vs Finals: king killed r193, scored 44-56). The screen therefore rewarded
  cat farming as much as winning. New `tools/score.py`: points with the king count (last digit), and the cell score
  = 100/0 when kings decide, the points share when points do (all cats dead in cooperation, round limit). gauntlet,
  games and elo recompute it from raw fields. Milestone M3.
- Re-scored, every top-6 cell of s3a/s3b/s3c is a loss: 144 games, 6 wins + 1 points win. A top-6 screen is blind on
  win/loss. New local screen v1: 12 band bots (replica ranks 11-33, the 8 ladder-panel bots left out) x 6 random maps
  x both sides (`research/screen/v1.json`). Running s2o, s3d, s3b on it (runs b1_*).
- Why the top 6 beat us (s3a+s3b, 144 games): **bites on the enemy king 0.0-1.0 a game for us vs 3.5-37.5 for them**.
  137 of 144 games end with our king destroyed; we never press theirs. Pickups 25-105 vs 133-227 a game, dirt dug
  25-32 vs 46-91, rat traps triggered by us ~27 vs ~40 by them.
- Economy trace (new `tools/econ_report.py`, DefaultMedium vs Finals): we harvested 1.5 of our 3 mines; (36,17) was
  never visited (240 cheese left lying), (34,24) first reached r92 (220 left); Finals took 380 from our side and 1240
  in all to our 460. Finals' king also walks to the middle (24,17) and is never bitten.
- Backstab: Finals lays a rat trap at r25 during cooperation; our rat steps on it at r27, which ends cooperation with
  Finals as the backstabber (the trap's owner). Our trap-risk mask is off during cooperation.

## 2026-10-28 09:10 PDT: field bots only on the replica; panel calibration; economy switches
- Owner (PROMPTS #8) asked whether every benchmark game goes through the replica. It did not: every local screen
  (g1-g7, b1) and diagnostic (dm*, cf*) ran the downloaded bots on the VM/driver directly, which a contestant cannot
  do, and TRAINING_ALGORITHM listed "local field games" as an instrument. Changed (milestone M4, 08:50): field bots
  play only as replica scrimmages, under its hourly request limits; local games are self-play, archetypes and stock
  players. The b1 band screen was stopped (s2o 127/144, s3d/s3b never started) and is not used. `tools/elo.py` drops
  local games against field bots (`--local-field` restores them) and maps examplefuncsplayer onto the replica's
  bc26-example; progress.png shows accepted builds only (s0e 1250, s2o 1355 on replica games).
- Panel v1 set 1 is badly calibrated: s2o went 6-0 vs PixelRifts_mushakraj_mk3, 8-1 vs alfredo, 0-6 vs goonv23 and
  0-9 vs Icarus, although all are rated within 50 of us. The replica's ratings are compressed (the top six beat us
  ~97% at +130). Calibration sweep queued: s2o vs ~30 ladder bots (3 maps x both sides, `research/panel/calib1.json`);
  panel v2 = bots s2o beats 25-75%. The hourly limit refused 20 of 24 requests at once, so `contest.py scrim --queue`
  / `drain` and `drain-loop.sh` feed the queue as the limit allows, sending only rows of the active build.
- Economy switches (cheesefarm vs Version41, before M4): MINE_BALANCE (even spread over all known home-half mines,
  re-picked when the count changes) pickups 1620 -> 2055; GOSSIP_ALWAYS + SCOUT_EVERY 4 moved the king's 4th mine from
  r122 to r31 but pickups stayed 2038. The real gap there is war: Version41 backstabs at r15-16, rushes our king
  (dist ~17) and lays traps at its door; trap damage is our biggest HP loss (2634 of 6083), and from r150 our rats
  thin out (20 -> 5) while theirs hold 16-20 with a 1500-2000 cheese bank. One scout overran once (r21).
- Snapshots s3e (s3d + MINE_BALANCE), s3f (s3e + gossip + scouts), s3g (s3e + KING_PRESS), s2o_id (identity control)
  in the self-play screen sp1 vs s2o (40 random maps x both sides).

## 2026-10-28 10:50 PDT: self-play screens, king overrun, starvation is how kings die
- sp1 (40 random maps x both sides vs s2o, `gauntlet.py selfplay`): s3e (+MINE_BALANCE) -5.4 pts, t -2.40 and s3g
  (+KING_PRESS, which almost never fires vs s2o) -5.4, t -2.40: rejected; s3f (+gossip, scouts) +3.2, t +1.55. The
  identity control s2o_id came out exactly 0 (identical code replays the same game with sides swapped), so self-play
  is scored against 50 with no control run.
- Bytecode: the king's first turn overran 20k on 9 of 40 maps (60x60: Sym.init 3.6k + sense 13.7k of new tiles, then
  spawn crossed the limit), losing its round-2 spawn. King.sense now stops KING_SENSE_RESERVE (5000) short on round 1;
  the rest is recorded next turn. s3h: 0 overruns in 80 games (s2o: 12).
- sp2: s3h (dev + gossip + scouts, no MINE_BALANCE) +0.9, t +0.49: pass. s3i (dev control) running; s3j dropped.
- Replica execute slots 2 -> 4 (`/etc/bc26-galaxy/local.env`): all field games now come from the replica.
- New `tools/king_report.py` (per king: HP lost starving vs otherwise, the round the bank ran dry, enemy rats near).
  Over all 42 downloaded replica matches, **31 of our 45 king deaths were starvation** (theirs: 36 of 49); in several
  no enemy rat ever came near our king. The other 14 are rushes on small maps (gridlock r55, sunandmoon r101,
  dirtfulcat r183-200).
- Where the cheese goes until the bank is empty (s2o matches): spawns 1300-1900, rat traps 560-1560 (Icarus maps,
  safelycontained), digs up to 800 (Excavation), against 250-700 delivered. On fullofsecrets and diamond our rats
  held 3665 / 1685 / 760 cheese when the king starved: s2o's nav could not bring them home (the king's strip is
  walled off at x=5). The dev line (s2y-s3d nav fixes) delivered 2790 vs s2o's 160 there and won both sides.
- New switches, off in dev: HUNGRY (global cheese below which any rat carrying cheese delivers before fighting) and
  CARRY_MAX (a loaded rat takes no pile that lifts it above the cap; carrying slows moves 1% per unit). Snapshots
  s3k (HUNGRY 150), s3l (CARRY_MAX 120), s3m (RAT_TRAP_MIN_CHEESE 400, FORT_TRAPS 4) queued as sp3.

## 2026-10-28 11:07 PDT: cats kill kings that block their path; the grab cone was decoded wrong
- Of the 14 replica king deaths that were not starvation, 9-10 had no enemy rat near the king at any time: cats
  (gridlock, sunandmoon, dirtfulcat, evileye, heart, popthecork x2, grove, uneasy_alliance). The engine's cat
  (InternalRobot, EXPLORE) walks waypoint to waypoint and, when its next footprint is blocked, scratches whatever
  blocks it, and keeps doing so. Our king on sunandmoon sat in such a path for 60 rounds (540 -> 20 HP). Its flee
  only tried away / 45 degrees off; dirt boxed it in on the west and north, and an idle fighter (approachCat, stuck
  behind the king) plus wandering gatherers boxed it in on the east.
- King.dodge (KING_DODGE): scores the 3x3 at every legal step by the tiles it shares with a near cat's next two
  footprints (250 / 75 per tile), touching its body (100) and distance; it moves when a step beats staying, and digs
  the leading dirt tile of the safest dirt-only step when boxed in. YIELD_KING: an empty rat on the ring around our
  king (Chebyshev 2) steps off it while the cat it sees is near that king. Dodge alone (one diagnostic game,
  sunandmoon, vs s2o) lost the king at r116, exactly as s2o did on the replica. Dodge + yield (s3n): the king lost 20
  HP in 2000 rounds and won on points. s3n is queued as sp4.
- Bug: replay direction ordinals are FlatHelpers' (0 centre, 1 west, then counter-clockwise: 6 north-east, 7 north),
  not the Java enum order. Checked against 66k rat moves. battle_report's vision cone used the enum order, so the F4
  finding "most of their grabs take our rat from behind" was wrong. Recomputed on the same five games: grabs on us
  are mostly from the front of a rat that is weaker than the grabber (cf5: front 45, behind 30; ex1: front 40,
  behind 19). Low HP, not facing, is what exposes a rat to a grab. Guard facing (s1h/s1i) rested on the wrong reading.

## 2026-10-28 11:35 PDT: starvation is usually a lost army; the grab war
- Self-play: s3i (the s3d line + sense guard) -4.0, t -2.04 vs s2o: reject. s3h (+ gossip + scouts) +0.9, so
  gossip + scouts is worth ~+5 (sp1: ~+8.6) and dev adopts it. s3k (HUNGRY 150) vs its base s3i: +0.2, t +0.33,
  22 of 80 cells changed. That is neutral; the bank rarely runs dry against s2o.
- From now on a switch candidate is scored against its own base, not s2o (`gauntlet.py selfplay CAND --control
  BASE/results.jsonl`).
- 57 replica starvation deaths (all builds): at the round our bank ran dry we had a median of **2 babies to their
  13**, and 14 a hundred rounds earlier. In 35 of 57 the army had been wiped (3 or fewer babies); only 17 had 10 or
  more (fullofsecrets, diamond, seepastyourgreed: delivery). Median cheese delivered by then: ours 465, theirs 905.
  Starvation is mostly what follows losing the fight. Combat is the root cause; spending is secondary.
- Knifefight vs bestsofar (match 610, our king killed r38): bestsofar backstabbed with a bite on our king at r2.
  - It grabbed 66 of our rats and threw 41, most of them into our king (its HP fell in steps of 42 and 84).
  - We grabbed 20 and threw 2; 15 of our 20 carriers died still holding (THROW_HOLD waits up to 6 rounds for a
    point-blank target).
  - Our 44 rat traps hit 36 times (1250 HP of theirs) and we had more rats (26 vs 15 at r37), yet we bit 11 times to
    their 44. Throws were 1694 of our 2624 HP lost.
  - Engine rule (assertCanCarryRat): a rat can be grabbed by any enemy outside its 90-degree, dist2 20 cone, or by a
    strictly healthier one (HEALTH_GRAB_THRESHOLD 0).
- New switches: BITE_GUARD (bite first an adjacent enemy healthier than us by at most one bite, which could grab us)
  and BITE_CARRIER (bite first an enemy carrying one of ours). Snapshots s3r (BITE_GUARD) and s3s (BITE_GUARD +
  BITE_CARRIER + THROW_HOLD 0) are queued as sp6. s3h submitted to the replica (#63); its panel v2 block (sets 1+2)
  is queued behind s2o's set-2 control.

## 2026-10-28 12:00 PDT: the fight is even; the leak is collection (we crowd one mine)
- Field-wide over s2o's replica matches (one per opponent, scratch scripts over battle_report):
  - Grab war volume is even: we grab 3092 and throw 2488; they grab 3409 and throw 2201.
  - Against the top 10 (30 games), HP lost: ours 107k, theirs 129k. Point-blank throws (42): 891 of our 1251,
    826 of their 1392. Combat is not where we lose.
- Economy, same 30 games (medians, us/them):
  - Bank: r100 1106/1468, r200 332/1230, r300 190/1268.
  - Delivered: r100 175/146, r200 410/544, r300 606/1032.
  - Rats: r300 9/16. Rat traps placed by r100: 19/2.
  - Spawns are even (36/43 by r300), and so are deaths. They can afford replacements; we stop at the spawn floor.
- Cheese accounting to r200 (25 games, means): spawns 1224/1210, rat traps 425/158, digs 148/264, upkeep 392/401.
  Residual (cheese bites, error) -43/-111.
  - The bank gap of ~600 is ~350 delivery and ~270 rat traps.
  - Picked up: 647/1344. Carry-turns: 438/836. Rat-rounds: 3461/3021.
- Per mine, to r200 (`econ_report.py --team vibe26b --hi 200` over the 30 games; it adds each listed mine's mirror
  partner, which the first scratch count missed). Per game:
  - Home mines: we spent 1501 rat-rounds there, they 217. Of 1227 cheese spawned, we picked up 374 (30%), they
    240 (20%), and 613 lay uncollected (50%).
  - Their home mines: they spent 652 rat-rounds and picked up 729 of 1236 (59%); we picked up 75.
  - Cheese per rat-round at home mines: ours 0.25, theirs 1.1. Our rats crowd the 1-2 mines nearest the king and
    leave the rest empty (rift (12,26): 3613 rat-rounds; keepout (47,30): 3383).
  - Cause, seen locally (s3h vs s2o, keepout): a gatherer picks its mine once, at birth, and keeps it. Rats born
    before the far mines are known stay at the nearest one. s3h's gossip and scouts find all 6 home mines by r103,
    yet (47,30) got 2658 rat-rounds and the others 4-73; 1220 of s3h's home cheese lay uncollected at r200. Even so,
    s3h picked up 1520 to s2o's 580 there.
- Next: gatherers must spread over every home mine. s3t = s3h + MINE_BALANCE: each rat takes the (hash mod count)-th
  known home mine and picks again whenever the team's known-mine count changes. It is queued as sp7. On the s3d
  base without gossip, MINE_BALANCE (s3e) lost 5.4 in self-play vs s2o. Self-play cannot show the field's theft
  from neglected mines.

## 2026-10-28 12:15 PDT: s3t (MINE_BALANCE) on keepout; the round-2000 points rule
- Local, keepout, vs s2o (A side, one game each):
  - s3h delivered 9025 and s3t 22246; s2o delivered 6580 in both.
  - s3t visits every home mine: 5 of 6 are emptied over the game (0-280 left each); (28,47) keeps 3480.
  - s2o leaves 20060 cheese lying on its own side. We never raid (RAID_EVERY 0 since s2e-s2j cost points against
    the top 6). The enemy's neglected mines are the next pool of cheese.
- Points at round 2000 (GameWorld.setWinnerIfMorePoints, after cooperation): 50% kings-alive share, 30% cat-damage
  share, 20% cheese-delivered share. Top bots run 2-5 kings (finalsbot and Finals 5). Only 2 of our 30 top-10 games
  reached r2000, though. Against the top, a game is decided by king deaths, not points.

## 2026-10-28 13:00 PDT: how field games end; opponents' extra kings
- All 240 replica games of our three submissions (ReplayDump summaries, `build/ends.jsonl`):
  - King killed or starved: 176 games, we won 77.
  - Points at round 2000: 41 games, we won 20.
  - Points before round 2000: 23 games, we won 16. These are cooperation games in which every cat died
    (GameWorld.setWinnerifAllCatsDead). Points then weigh 50% cat damage, 30% kings, 20% delivery. We won all 12
    in which we did all the cat damage (8000/0 on closeup, canyoudig, externalsustenance...). We lost the even ones:
    TheHeist (4000/4000, delivery 46-49%) twice, krotus and knifefight vs bc26vibe_bot (42-43%).
- Round-2000 losses: in 12 of 21, the opponent ended with 2-5 kings to our 1. A 1-2 kings split costs about
  17 points of the 50 for kings. In the 20 wins, our delivery share is often under 50%, but our cat damage share is 55-100%.
- When the field forms more kings (rounds.tsv king counts, 76 games where a side reached 2):
  - thewaxmango PhD: king 2 at r194-296 (keepout, rift), king 3 at r378-891.
  - AlexT101 finalsbot: kings 2-5 at r168-419 (EscapeTheNight) and r384-768 (seepastyourgreed).
  - daannte Skadoosh: kings 2-4 at r221-310 (safelycontained) and 2-5 at r321-611 (seepastyourgreed).
  - AdamTan12 Icarus/Finals: king 2 at r108-520, Finals reaching 5 by r943.
  - HugoIngelsson: king 2 at r333-802, king 3 at r536-1117.
  - We never have more than one king. Our single king dies later in most of these games.
- Formation rule (RobotControllerImpl.assertCanBecomeRatKing): 7 allied baby rats in the 3x3 (the former counted),
  50 cheese, no cat or king in the 3x3, every tile passable. Up to 5 kings before r1200, 2 after. The king's HP
  is the sum of the rats' HP; enemy rats in the 3x3 die too.
- Local (6 games, s3t vs s3h, alternating sides): 3-3. s3t picked up 14-141% more cheese to r300 in the 4 long
  games. The 2 short games (gridlock r84, canyoudig r96) ended when the cats died during cooperation.
- Self-play screens: s3t +1.5 vs s3h (t +0.66); s3u (RICH_BABIES 30) +2.1 vs s3t (t +1.00); s3s (grab-war bundle)
  -0.1 vs s3h. All pass.
- Next: s3v (s3t + FORM_KING) and s3w (s3t + RICH_BABIES + FORM_KING) are screening. Whichever of s3t/s3w screens
  best goes to the replica after s3h's panel block.

## 2026-10-28 14:35 PDT: s3h rejected on the panel; a bytecode tag for field replays; s3v forms kings
- s3h vs s2o on panel v2 (sets 1+2; s2o's reference `research/panel/v2-ref-s2o.txt`): 94 cells, 67 alike, D 27
  (+15 -12), net +3, t 0.58. Block 2 rejects at t <= 1, so s3h is rejected and s2o stays the incumbent.
- The bytecode tag (prompt 11):
  - Each turn now ends with a pad, so that bc mod 128 carries the move code or spawn code, a fighter/war bit and a
    parity bit (Debug.tag).
  - The instrumenter charges a basic block before the instruction that ends it, and Clock.getBytecodeNum is free.
    So the cost after the read is a constant 12 plus the pad. Calibrated on 5317 turns, then 9637 of 9637 decoded.
  - Review fixes (workflow, 15 agents):
    - Rows for throwers, thrown rats and carried rats are written mid-turn with the PREVIOUS turn's count; the
      robot's own row comes last, so decoders keep the last row per robot-round.
    - Skipped: bc 0 (died in its own turn), counts near the limit, and the turn after an overrun.
    - An opponent with a constant count passed parity, so the decoder now also requires varied counts.
  - `tools/smuggle.py` reads it. s3y = s3t + the tag is the next replica candidate.
- s3v (FORM_KING) self-play vs s2o:
  - It formed a second king in 28 of 80 games and ended with 2 kings in 24. Screen vs s3t: -0.6 (t -0.28).
  - King deaths rose from 2 to 9. Both kings died on where_are_you (both sides) and houseofcards; one king died on
    rose and ultimatestarvation without a second king ever forming. The formation probably spends the
    rats and cheese that a poor map needs (FORM_CHEESE 300). Next: a richer gate (bank and income) before forming.

## 2026-10-28 14:55 PDT: extra kings starve on poor maps; s3y on the panel
- s3w (s3t + RICH_BABIES 30 + FORM_KING) screen vs s3t: -1.6 (t -0.53), neutral. It formed 26 kings; our king
  deaths were 16 vs s3t's 2.
- The deaths are starvation. Every game where both kings died ended with bank 0-1, on maps where we delivered only
  1-2 cheese a round: houseofcards, where_are_you, corridorofdoomanddespair, minimaze, and popthecork as B.
  Two kings eat 4 a round (a starving king loses 10 HP a round). Where delivery ran 3+ a round (keepout, rift,
  pipes, safelycontained, DefaultLarge, cheeseguardians), the second king lived and those games were won.
- Local s3v vs s2o on where_are_you (rerun with a long timeout): s3v delivered 3520 to s2o's 7574 and did 0 cat
  damage; both of its kings starved by r1608 (RATKING_DESTROYED, cooperation never ended).
- Points at round 2000 (GameWorld.setWinnerIfMorePoints): without cooperation, kings are 50% of the points,
  split by king count; in cooperation they are 30%. So extra kings matter wherever we can feed them.
- New off-by-default switches in dev:
  - FORM_NET100: form only while the trusted income beats every king's upkeep, the new one's included.
  - FORM_JOIN: only babies with fewer than FORM_JOIN allied babies nearer the site join; the rest keep working.
- s3y (s3t + the tag) is replica submission #64; its panel v2 blocks are queued.

## 2026-10-28 15:20 PDT: how the field forms extra kings; screens s3x, s3n, s3r, s3m, s3o
- Extra kings in our 136 replica matches (scratch kingform.py over the replays; 120 formed by opponents, 0 by us):
  - The bots whose extra kings live form them with a bank of 1500-2800 (median), at r300-600, 8-28 tiles from
    the first king, with 20-36 babies. AlexT101_usqualsbot: 20 kings in 10 games, 5% died. daannte, thewaxmango,
    AdamTan12_Finals: none died.
  - The two whose kings die (GuzziFlipFlops 83%, seru9_TOTAL_AGGRO 75%) form at bank 350-500, 3-4 tiles from the
    first king. That is what our FORM_CHEESE 300 does.
- Screens:
  - s3x (s3w + FORM_MAX 4) rejected vs s3w (-2.5, t -1.68): 42 kings formed, 30 died, 14 games ended with none.
  - s3n (cat dodge + yield) +0.3, s3r (BITE_GUARD) -0.1, s3m (trap economy) +0.2: neutral.
  - s3o (s3h without FLOW and DIG_FIRST) +1.9 vs s3h (t +0.85): neutral, leaning positive.
- Queued on the VM: s3z (s3t + FORM_KING with the income gate FORM_NET100 -100 and FORM_JOIN 8), then s4a
  (s3z + FORM_CHEESE 1500).

## 2026-10-28 17:25 PDT: s3y between the bars; a cat defence; more screens
- s3y (s3t + the tag) vs s2o on panel v2 sets 1+2: 94 cells, 64 alike, D 30 (gained 20, lost 10), net +10, t 1.83.
  Block 2 rejects at t <= 1 and accepts at t >= 2, so s3y plays block 3. Set 3 was drawn with a fixed seed
  (20261028), three maps per opponent that it met in neither set 1 nor set 2 (`research/panel/v2-set3.json`).
  s2o (#65) was re-submitted to play its set-3 control first; s3y follows.
- attack_on_cat (s3t and s3z lost both sides; s3h won both): during cooperation one cat parked 3 tiles from our king
  at r501 and scratched it 20 HP every 3 rounds until it died at r614. 17 of our 20 rats were gatherers and kept
  GATHER_CAT_DIST away; 3 fighters could not kill a 4000-HP cat. s2o's rats had its cat at 2107 HP by r480.
  New switch CAT_DEFEND_R2: a cat within dist2 R2 of one of our kings draws every rat that sees it. s4b = s3z +
  CAT_DEFEND_R2 36 wins attack_on_cat locally (cat killed, points win at r595). Screening on the VM.
- Screens: s4a (s3z + FORM_CHEESE 1500) is s3z to within one cell, because the income gate already holds formation
  until the bank is over 1500. s3p (s3h without KING_ROAM/KING_DIG) -0.8, neutral. s3q (s3h without DEFEND,
  MINE_SPREAD and EXPLORE_CENTER) -4.3, rejected: those features earn their keep.

## 2026-10-28 17:55 PDT: s4b passes; the dumper audit lands; where the field beats us
- s4b (s3z + CAT_DEFEND_R2 36) passes the screen: +2.6 pts vs s3z (t +1.81, cells +6 -0), +2.2 vs s3t;
  attack_on_cat +75 (wins both sides). Next replica candidate after s3y. s4c (s4b + KING_DODGE + YIELD_KING) is
  screening.
- ReplayDump audit round 1 is in (dumpVersion 2, dump cache, field_report.py, mirror_check.py; f48475a). Round 2
  (trap victims, starvation booking, report leftovers, `contest.py paired --graded`) is running.
- `field_report.py --ids 898-917,976-987 --by opp` on s3y's 96 panel games:
  - We win 11-12 of 12 against the peaceful bots (C0derTang, XSercia, ask8000) and 9-3 against outercloudstudio.
  - We lose to the two that backstab around r35-50 and rush: abcdezyxwv_bestsofar 1-11 (king killed, median
    r333) and AlexT101_usqualsbot 2-10 (median r306). CS50Crab 5-7, bc26vibe 6-6 (cat points in cooperation).
  - We never backstab first ("backstab by us 0" in every group).
- Brawls with rushers (battle_report, knifefight vs abcdezyxwv, evileye vs AlexT101): kills are about even
  (kd 0.93-1.0), but they grab more (63 vs 39) and throw more (42 vs 21; thrown rats hit our king for 294 HP). We
  spend the bank on rat traps (61 traps, 1220 cheese, on knifefight; 45 on evileye), so with enemies in view the king
  then fails its war spawn (`K:sE`), while they keep spawning and outlast us.
- Mine coverage (scratch minecov.py): in 44 of the 96 games we never took cheese from at least one of our-side mines.
  streetsofnewyork vs CS50Crab (lost): two of our three mines sit behind 5-deep dirt bands and no rat of ours came
  within sight of them in 1444 rounds; 17846 rat-rounds crowded the third. On safelycontained AlexT101's rats cover
  the whole map by r400 (ours stay within ~10 tiles of the king) and pick 62592 cheese to our 6638.
- New switch SEEK_CROWD: a gatherer at its mine that sees SEEK_CROWD allied babies nearer that mine explores our
  half's unseen tiles (up to SEEK_TTL 150 rounds, once per SEEK_RETRY 200) and reports any mine it finds. s4d = s4b +
  SEEK_CROWD 6.
- New local archetype `arush` = s4b + RUSH (spawn indices 4-15 attack the enemy king from birth), so rush defence
  can be measured locally (field bots stay on the replica). s4b vs arush queued on the VM (rx1_s4b).

## 2026-10-28 18:45 PDT: s3y accepted; s4b to the replica; a stronger rush archetype
- s3y's block 3 (set 3, matches 1196-1211) against s2o's set-3 control (1168-1183): block t +1.41 (+6 -2). All three
  blocks: 142 cells, D 38 (+26 -12), net +14, t 2.27: accepted. s3y (#66) is the incumbent and its panel matches are
  the new reference (`research/panel/v2-ref-s3y.txt`). Set 3 by opponent: 6-0 against bc26vibe, CS50Crab, XSercia,
  ask8000 and C0derTang; 3-3 against outercloudstudio; 0-6 against AlexT101 and 1-5 against abcdezyxwv. Rush defence
  is still the gap.
- s4b (#67) submitted; block 1 on set 1 is going out through the drain loop.
- rx1_s4b: s4b beat arush 51-29. arush won 23 of its 29 on points and killed our king only 6 times, while the field
  rushers kill it in most of our losses. arush only bites the king during cooperation, so it never opens the war
  the way they do.
- New switch RUSH_BREAK (archetype only, default off): rush attackers bite and grab enemy rats during cooperation
  (Baby.hostile()). `bots/abrk` = s4b + RUSH + RUSH_BREAK. s4b vs abrk (rx2_s4b) is queued after the screen chain;
  one diagnostic game is running first.
- Engine check for the back-grabs: a rat can be grabbed when it cannot sense the grabber (outside its 90-degree cone)
  or when the grabber has more health (HEALTH_GRAB_THRESHOLD 0), so any bitten rat can be grabbed from the front.
  Turning has its own cooldown (10, separate from movement), and a baby moving off its facing pays 18 instead of 10.
  We already strafe to keep a seen enemy within r^2 13 in the cone (Nav.guard) and end the turn facing it
  (guardTurn). The 32 of 61 grabs from behind come from enemies we never saw.
- Owner rule (PROMPTS.md #14): every map is symmetric in all aspects. The mine sets of all 45 maps in the dump cache
  are closed under their symmetry. A read-only audit of where the bot should mirror its map knowledge (terrain and
  navigation, mines and cheese, enemy kings and cats, inference speed) is running, with a skeptic checking each
  finding against the code and the data.

## 2026-10-28 19:00 PDT: s4d passes and replaces s4b on the replica; GLANCE_MEM
- s4d (s4b + SEEK_CROWD 6) passes its screen against control sp12_s4b: +3.3 pts, t +2.48, cells +7 -3 (best:
  corridorofdoomanddespair +32, peaceinourtime +19, houseofcards +19; worst: sunandmoon -7, DefaultLarge -5).
- Diagnostics on streetsofnewyork vs s2o (VM): s4b lost 21-78 on points, delivering 4295 cheese to s2o's 8624. s4d won
  63-36, delivering 12974. s4e (s4d + MINE_MIRROR) played the identical game: there, mirroring added no mine that
  seeking had not already found.
- s4b's replica block 1 was withdrawn after 4 matches; s4d (#68) is the candidate, and its block 1 is queued.
- New switch GLANCE_MEM (default 0): at war, a turn that ends with no enemy rat known near spends the unused turn
  facing back, when an enemy rat was seen or heard in the last GLANCE_MEM rounds. s4g = s4d + GLANCE_MEM 20, screened
  as sp17_s4g and tested against abrk as rx3_s4g (vs rx2_s4d).

## 2026-10-28 19:45 PDT: the self-play screen has no war; s4e neutral; s4f untested
- s4e (s4d + MINE_MIRROR) against control sp14_s4d: only 4 of 80 games differ, -0.1 pts, t -0.38: neutral.
- s4f (s4e + RAT_TRAP_MIN_CHEESE 400 + FORT_TRAPS 4) played all 80 games exactly as s4e did. The reason: no game in
  any `sp` screen against s2o ever left cooperation (0 backstabs, 0 rat traps in sp14, sp15 and sp16), so every
  war-only switch is inert there. The old local gauntlets had a backstab in nearly every game, because the benchmark
  bots break the truce. Since protocol M4 (field bots only on the replica), the screens have been blind to war.
  Re-read as untested: s3m (trap economy), s3r (BITE_GUARD), s3s (grab-war bundle), KING_PRESS (s3g), s4f. LEARNINGS
  #12.
- Consequences: sp17_s4g (GLANCE_MEM acts only at war) is dropped from the VM chain. War changes are judged in rush
  runs against abrk (rx2_s4d is the control; rx3_s4g = GLANCE_MEM, rx4_s4f = trap economy), then on the replica.
- progress/ refreshed after s3y's acceptance (owner rule, PROMPTS.md #16): s0e -> s2o -> s3y on progress.png, about
  1320 -> 1490 -> 1600 BT, expected field score 36% -> 47% -> 54%; replica ladder #27 of 54.

## 2026-10-28 20:10 PDT: abrk is a usable rush instrument; s4d's replica block 1
- rx2_s4d (s4d vs abrk, 80 games): s4d 48-32. abrk killed our king in 17 of our 32 losses at median round 314, after
  breaking the truce at median round 36. That is close to what the field rushers do to us on the replica (king dead
  r306-333, backstab r35-50); arush managed only 6 king kills at median r440. rx2_s4d is the control for rush
  defence.
- In the king-kill losses our delivered cheese falls far behind once the war starts (EscapeTheNight 390 vs 5004,
  gridlock 320 vs 887) and we often have fewer rats spawned. At war the baby cap stays 18 (MAX_BABIES_WAR = MAX_BABIES).
- Queued rush runs against abrk: rx3_s4g (GLANCE_MEM 20), rx4_s4f (trap economy), rx_s4h (MAX_BABIES_WAR 30),
  rx_s4i (BITE_GUARD + BITE_CARRIER).
- s4d on the replica, block 1 (set 1, matches 1287-1302) vs s3y: 48 cells, D 9 (+5 -4), net +1, t 0.33: above the
  block 1 bar (t <= 0 rejects); block 2 on set 2 is going out.

## 2026-10-28 20:40 PDT: GLANCE_MEM helps against the rush
- rx3_s4g (s4g = s4d + GLANCE_MEM 20, vs abrk) against control rx2_s4d: +6.4 pts, t +1.52, cells +10 -7; 51-29 (s4d
  48-32); our king killed in 14 losses (17). Our grab count rose (3624 -> 3972) and the enemy's barely moved (3128 ->
  3267), so the gain seems to come from seeing, and then grabbing, enemies behind us rather than from dodging their
  grabs. Pass; it joins the next stack. It is inert in peace, since the turn-end code returns during cooperation.

## 2026-10-28 21:00 PDT: s4d replica block 2
- s4d block 2 (set 2, matches 1310-1313 and 1352-1363) vs s3y: D 9 (+7 -2), net +5, block t 1.67. Blocks 1-2: 94
  cells, D 18 (+12 -6), net +6, t 1.41, above the block-2 bar (t <= 1 rejects); block 3 on set 3 is going out.

## 2026-10-28 21:15 PDT: the trap budget is neutral against the rush
- rx4_s4f (s4f = s4e + RAT_TRAP_MIN_CHEESE 400 + FORT_TRAPS 4, vs abrk) against control rx2_s4d: +2.1 pts, t +0.56,
  cells +13 -12, 49-31, king-kill losses 16 (17): neutral. The intended effect did not happen: we placed 3020 rat
  traps over the 80 games against 2171 for s4d. A higher bank floor probably keeps more babies above their own trap
  threshold. Unexplained for now; trap spending is not where the rush losses come from.

## 2026-10-28 21:30 PDT: a higher war baby cap leans positive against the rush
- rx_s4h (s4h = s4d + MAX_BABIES_WAR 30, vs abrk) against control rx2_s4d: +3.1 pts, t +0.93, cells +11 -7, 52-28,
  king-kill losses 16 (17), king kills by us 21 (18). We spawned 4572 babies against 4108 and moved 718702 cheese
  against 703869. Lean positive; it joins GLANCE_MEM 20 in the next stack. It is inert in peace.

## 2026-10-28 21:50 PDT: s4d rejected on the replica; the symmetry audit
- s4d block 3 (set 3, matches 1373-1380 and 1420-1427) vs s3y lost 3 net: uneasy_alliance on both sides to bc26vibe_bot
  and Excavation to abcdezyxwv. Blocks 1-3: 142 cells, D 21 (+12 -9), net +3, t 0.65, under the block-3 bar (1.5):
  rejected. s3y is back on the replica (submission 69). s4d's +3.3 self-play points did not carry to the field. Its
  switches stay as the base of the next stacks, because nothing in the field data says they hurt.
- The symmetry audit (research/audits/symmetry.md) found nothing wrong with the sightings we already use. Its gaps:
  the team pools almost no evidence, so the king's mask never becomes unique on about 40% of our game-sides, and the
  median round where it does is 103. Its top proposal reads the symmetry from the order of
  senseNearbyMapInfos, which the engine reverses for chirality-1 robots. Only one side would learn the symmetry from
  this, so our play would differ by side. That breaks the rule that every move is symmetric when switching sides, so
  it is not implemented.
- Implemented behind switches: SYM_HEAR_TILE (a SYM squeak goes to the king when any of its 9 tiles is in earshot,
  not only its centre: the engine delivers to any occupied tile), SYM_GOSSIP (babies merge heard SYM squeaks, and
  one away from the kings squeaks a sharper mask every 3 rounds, with an ally in earshot, no cat in earshot, no
  enemy in view), and SYM_MINELIST (a listed mine's mineless s-twin rules out s; unseen twins carry expectation bits
  that fire on the first sighting; the king also applies the engine's d2-25 mine spacing). The companion is
  MINE_TIES: once VER is known on EscapeTheNight, 7 of its 11 mines sit on the axis, and the strict test would leave
  the king no home mine.
- Arms: s4k = s4d + the three plus MINE_TIES, s4l = s4k + MINE_MIRROR, on the self-play screen (sp16) vs s2o, with
  control sp14_s4d, after rx_s4j (s4d + GLANCE_MEM 20 + MAX_BABIES_WAR 30, the two rush passes stacked).

## 2026-10-28 22:15 PDT: the bite pair is the best rush arm; s4j on the replica
- rx_s4i (s4i = s4d + BITE_GUARD + BITE_CARRIER, vs abrk) against control rx2_s4d: +4.2 pts, t +1.82, cells +7 -2,
  53-27, king-kill losses 13 (17). Pass, and the best of the four rush arms. s4n stacks all three passes
  (GLANCE_MEM 20, MAX_BABIES_WAR 30, the bite pair) on s4d; its rush run goes ahead of the symmetry screens.
- The replica would otherwise sit idle, so s4j (s4d + GLANCE_MEM 20 + MAX_BABIES_WAR 30) is on it now (submission
  70, set-1 block from match 1446). Already-created matches pin their submission, so s4n cannot replace it mid-block.
  If s4j's block 1 ends at t <= 0, s4n (after its rush run) goes out instead.
- knifefight is 0-2 against abrk in every rush run. The kings start 5 tiles apart (d2 25), and on 4 maps they start
  within d2 64 (knifefight, tiny, uneasy_alliance, thunderdome). In the kept replay abrk bit our king 38 times, and
  we bit theirs 8 times. s4m = s4d + KING_PRESS (rats that see or hear of the enemy king walk to it and bite) was
  rejected only on a peaceful screen (s3g); a 16-game check against abrk on the 4 close-king maps (dx_close) is running.

## 2026-10-28 22:40 PDT: KING_PRESS wins the king races; a war-only king cap
- dx_close (vs abrk, 4 close-king maps x 2 sides): s4d 2-6, s4m (s4d + KING_PRESS) 5-3. Our bites on the enemy king
  rose from 8 and 14 to 60 and 23 on knifefight, and from 11 and 14 to 45 and 48 on tiny. Wins on knifefight A, tiny B,
  uneasy_alliance A and B, and thunderdome B; losses on knifefight B, tiny A and thunderdome A (on points). s3g's
  peace-time rejection said nothing about war. The full rush run rx_s4m is queued after rx_s4n.
- Points at round 2000 weigh living kings 0.5 after a backstab (0.3 before). In rx_s4i's points games (no king
  kill), every game where we formed one more king than abrk was a win. FORM_MAX_WAR (default 0 = FORM_MAX) lets the
  first king name sites up to that count once cooperation is over. s4o = s4d + FORM_MAX_WAR 3 is queued as rx_s4o.
  FORM_MAX 4 in peace (s3x) starved kings: 42 formed, 30 died.
- VM queue: rx_s4j (running), rx_s4n, rx_s4m, rx_s4o, sp16_s4k, sp16_s4l.

## 2026-10-28 23:00 PDT: an early second king for the cat-death endings
- Our replica losses on uneasy_alliance to bc26vibe_bot (1375, 1376) were 49-50 and 50-49 on points. The game
  ended in cooperation when the last cat died (r344, r346), with one king each. We won the cat share (8068 vs 7932)
  and lost on cheese transferred (4064 vs 4481), with 1900-3400 cheese unspent in the bank.
- 40 of the 80 games in s4d's self-play screen end the same way (r63-r383). s4d formed a second king in 23 of the
  80 games, and in none of the early-ending ones: FORM_FROM 80, FORM_BABIES 16 and FORM_CHEESE 300 come too late.
  With one king each, a second king moves the margin by about 10 points (king share 2/3 vs 1/3 at weight 0.3), and
  those games are decided by 1-10 points. Against a rusher it also means two kings to kill.
- Arms on the self-play screen (sp17 vs s2o, control sp14_s4d): s4q = s4d + FORM_FROM 30; s4r = s4q + FORM_BABIES 12
  + FORM_CHEESE 150. They go first in the VM queue, after rx_s4j.
- Field endings for our bot over all replica games: 502 of 759 end in a king kill (263 lost), 178 on points at r2000
  (52 lost), 79 on points at an early cat death (27 lost). A tally of what killed our king in the s3y/s4d-era losses
  is running.

## 2026-10-28 23:20 PDT: s4j passes the rush run
- rx_s4j (s4j = s4d + GLANCE_MEM 20 + MAX_BABIES_WAR 30, vs abrk) against control rx2_s4d: +6.2 pts, t +1.53,
  cells +14 -9, 53-27, king-kill losses 12 (17), king kills by us 23 (18). The two passes stack, with the largest
  points gain of the rush arms. It is already the replica candidate (set-1 block running). The full suite passed
  (BotTest, test_tools 109, battle 64, replaydump 489, reports 67, scoring 102).

## 2026-10-28 22:40 PDT: s4j rejected on the replica after block 1
- s4j block 1 (set 1, matches 1446-1457 and 1494-1497) vs s3y: 48 cells, 39 alike, D 9 (+4 -5), net -1, t -0.33:
  rejected (block-1 bar t <= 0). It gained where_are_you (C0derTang), dirtfulcat and streetsofnewyork (CS50Crab) and
  wallsofparadis (bc26vibe_bot). It lost Nofreecheese (CS50Crab), DefaultLarge (abcdezyxwv), fullofsecrets (ask8000),
  krotus (bc26vibe_bot) and heart (outercloud). abcdezyxwv swept DefaultLarge, knifefight and sunandmoon on both
  sides against both versions. Against abrk, our own rush archetype, war-time fixes move few panel cells. s3y is
  back (submission 71). The next candidate goes out when the early-king screens (sp17) or the stacked rush run
  (rx_s4n) are scored.

## 2026-10-28 23:05 PDT: s4q rejected; s4i to the replica; starvation tally
- sp17_s4q (s4d + FORM_FROM 30) vs s2o, control sp14_s4d: 80 cells, D 54, mean -2.6, t -1.23, net +1. It formed a
  second king in 21 of 80 games (s4d 23), so FORM_FROM is not what holds the second king back. Rejected. s4r
  (FORM_BABIES 12 + FORM_CHEESE 150 on top) is running.
- The replica was idle with s3y alone, so s4i (s4d + BITE_GUARD + BITE_CARRIER, rush run t +1.82, cells +7 -2) is
  submission 72 with a set-1 block from match 1520.
- What killed our king in the 64 s3y/s4d-era field losses: bite 25, starvation 23, throw hit 11, trap 2, scratch 2.
  The starvation deaths come from 17 replays on 11 maps (Stash, dirtfulcat, sittingducks, constrictinglattice,
  Nofreecheese, streetsofnewyork, heart, fullofsecrets, EscapeTheNight, pipes, ultimatestarvation). Dumps of
  all 17 are running to see where the cheese went before the bank hit 0. The first one, dirtfulcat in 1373
  (backstab r11), shows the pattern. The cat and the enemy killed 17 of our babies, and income stopped (20
  babies at r80, 2 by r220). We formed a second king at about r150 with the bank at 343, and its 7 babies left
  the workforce. Spawns stopped below LOW_FLOOR 100. Two kings at 4 per round ran the bank to 0 at r258, and
  both kings starved by r311. Rat traps cost 1300 of our 2488 starting cheese in the first 150 rounds (65 traps,
  all triggered, 23 enemy kills).

## 2026-10-28 23:15 PDT: starvation is mostly lost income; stuck babies are a navigation bug
- In the 150 rounds before the bank first hit 0, spawns and traps cost little in most of the 20 starvation games. Upkeep
  dominated: 300 per king, with income of 0-600, and the bank at 150-400 when the window opened. The early-war rat
  traps and spawns spent the starting 2488 long before that. In several games income was near 0 while 16-20 babies
  were alive: EscapeTheNight (1197, no cheese delivered in 150 rounds), fullofsecrets (1298), Stash. Two s4r screen
  facts: s4r played the same 80 games as s4q (FORM_BABIES and FORM_CHEESE never bind; the income test is the gate),
  rejected.
- Smuggled tags plus positions: a stuck-baby scan counts episodes where a baby whose decision is travel (deliver,
  to mine, to cheese) stays inside a 3x3 box for 30+ rounds. Over the 54 games in those 17 replays it finds 485
  episodes and 35k baby-rounds. On EscapeTheNight babies sat 300-660 rounds in a room corner chasing cheese or a mine
  across a wall. On Nofreecheese a carrier sat on open ground for 130 rounds. On FreeTheCheese one held 840 cheese
  for 175 rounds.
- Local repro (s4d vs s2o, VM, indicator strings kept): EscapeTheNight, 36 episodes, 4220 baby-rounds. Four babies
  in the corner of the king's room each finish an A* search (12 turns), find the next path tile held by another
  stuck baby, drop the path after FLOW_HOLD 3 turns, and search again, holding still during every search. Baby 11175:
  48 searches done, 23 steps in 600 rounds.
- s4s = s4d + FLOW_JAM: when a robot holds the next path tile, keep the path and sidestep onto a free tile beside one
  of the next three path tiles. The farthest wins; ties go by G.hand, so mirrored rats take mirrored sides. The path
  is dropped only after FLOW_JAM_HOLD 12 turns. Unit test testFlowJam (straight path, ally on the next tile, ROT
  mirror, boxed-in case); BotTest 196/196. Diagnostic games on the five worst maps are running, before a screen.

## 2026-10-28 23:45 PDT: KING_BLOCK, the stuck report, and cheese across a wall
- s4s (FLOW_JAM) made things worse on the five diagnostic maps: stuck rounds went from 7295 to 10335. The longer hold
  sat behind a tile that never clears, our own king's body. The A* search treated the king's 3x3 as open ground, so
  babies beside the king followed paths into it.
- KING_BLOCK (s4t = s4d + KING_BLOCK): our kings' bodies are marked in Sym.mem with a KING bit that survives
  re-sensing. Flow paths and bug walks treat it as a wall unless the target is at the king. testKingBlock covers it.
  Stuck rounds over the five maps fell from 7295 to 4755: EscapeTheNight 4220 -> 1809, FreeTheCheese 571 -> 139,
  fullofsecrets 2173 -> 1543. pipes rose from 89 to 1161; that is one game, and results there are within noise.
  Results matched s4d's (EscapeTheNight W 57-42 vs 52-47; the other four kept their winners). s4u (KING_BLOCK + FLOW_JAM)
  reached 8182, so FLOW_JAM is rejected twice.
- tools/stuck_report.py turns the scratch scan into a tested tool. It adds the distance to the nearest own king when
  an episode starts. Field games (54, tagged builds): 7.2% of baby turns are lost to stuck episodes. 58% of those
  rounds are touching our king's body, 29% within 3-5 of it, and 13% farther away (mostly carriers, code d).
- The rest of EscapeTheNight, traced with a debug build that writes the nav target, flow target and held turns to the
  indicator: five babies in the king's room target a cheese pile at (18,21), 3 tiles east through the room wall. A*
  finds the 19-tile way out, but each baby's next path tile is held by another of them. Each holds 3 turns, drops,
  and searches again (4 turns). After 12 stalled turns it gives up the cheese and picks the same pile the next turn,
  because nothing remembers the give-up. This lasted 110 rounds, until someone else took the pile.
- s4v = s4t + CHEESE_BAN 60: a pile given up after a stall is ignored, with its Chebyshev-2 neighbourhood, for 60
  rounds (a ring of 4). testCheeseBan. Diagnostic games are running. s4t's self-play screen (sp18_s4t) and rush
  run (rx_s4t) are first in the VM queue after rx_s4n.
- s4i (s4d + BITE_GUARD + BITE_CARRIER) is rejected on replica block 1 against s3y: 48 cells, 12 differ, 5 gained,
  7 lost, net -2, t -0.58. s3y was re-submitted (submission 73). Then s4t went up (submission 74) and its set-1
  block started at match 1578, in parallel with its self-play screen. The replica would otherwise sit idle, and a
  screen rejection would end the block.
- The block waiter had a bug: `contest.py matches` shows one page by default, so older block games fell off the list
  and the count could not reach 16. Waiters now pass `--pages 5`.

## 2026-10-29 00:05 PDT: CHEESE_BAN works; NAV_SAME_R backfires; flow paths are mostly wasted
- s4v (s4t + CHEESE_BAN 60) on the five diagnostic maps: 1386 stuck rounds (s4d 7295, s4t 4755). EscapeTheNight
  fell from 1809 to 83 and fullofsecrets from 1543 to 928. Results were unchanged; cheese delivered on
  EscapeTheNight went from 21029 to 26520. FreeTheCheese delivered less (17472 -> 9566, one game). A plausible cause:
  a stall in a crowd at a mine bans every pile within 2 of it for 60 rounds. CHEESE_BAN_WALL bans only piles whose
  last flow path was a detour (> 2 * Chebyshev distance + 2) or whose search failed (Flow.detour, tested).
- Carriers on fullofsecrets stall outside the wall of the king's pocket. The king roams between (3,32) and (4,32),
  every step was a new goTo target, and the stall reset before it reached FLOW_STALL. NAV_SAME_R 2 kept the stall
  for targets within 2. The unit test passes (a roaming target behind a wall is reached only with it), but in games
  it is worse: s4w 5468 stuck rounds vs s4t 4755, s4x 3558 vs s4v 1386. The search from such a carrier to the pocket
  spans most of the 40x45 map, overruns FLOW_MAX_TURNS 12 with the carrier holding still, fails, and waits
  FLOW_RETRY 30. With the stall kept, the carrier loops on that and never falls back to bug walking. One carrier
  held 1190 cheese for 365 rounds. s4w, s4x, s4y and s4z are rejected or withdrawn.
- MINE_KING_R 4: on FreeTheCheese our king's 3x3 covered a gatherer's mine. The free tiles within 2 were taken,
  so it pathed into the king for 60 rounds. Now a gatherer counts as at its mine within 4 when a king sits within
  2 of it, and wander targets avoid the kings' bodies (testAtMine). s5a = s4v + MINE_KING_R, s5b = s5a +
  CHEESE_BAN_WALL; diagnostics are running.
- Flow accounting from the final indicators of s4v's five games: 12-38% of searches fail on the maze maps
  (fullofsecrets 224 of 711, Nofreecheese 85 of 223). Completed paths are followed for about 3 steps each (fullofsecrets
  1383 steps over 487 paths), after 4+ turns of holding still to build them. Per-path end counters (fe=: reached
  the end, robot-held, wall, drift, replaced while ready, replaced while building, failed) are in the indicator
  now. dbgv (s4v + counters) is running to show where the paths go.

## 2026-10-29 00:16 PDT: s4t rejected; flow paths end at robots; a trail home
- s4t (s4d + KING_BLOCK) is rejected on replica block 1 against s3y (matches 1578-1601): 48 cells, 9 differ,
  2 gained, 7 lost, net -5, t -1.67. Most of the lost cells were war games (heart and uneasy_alliance against
  AlexT101, DefaultLarge against abcdezyxwv), plus two 49-50 cooperation losses on krotus against bc26vibe_bot. The
  self-play screen agrees: sp18_s4t was net -4 over 64 cells (+3 -7, t -0.64) when it was stopped. KING_BLOCK
  fixed stuck babies but does not pay in results. s3y is back on the ladder (submission 75).
- s5a (s4v + MINE_KING_R 4) and s5b (s5a + CHEESE_BAN_WALL) on the five diagnostic maps: 2709 and 3830 stuck
  rounds (s4v 1386). Most of it is carriers ('d') on pipes and fullofsecrets, where single games swing widely:
  s5a starved on fullofsecrets (cheese delivered 240) while s5b won it 89-10 (2520). Both are withdrawn.
- dbgv's path-end counters on EscapeTheNight / Nofreecheese / fullofsecrets: no path was followed to its end
  (0 / 0 / 0). Paths ended when a robot held the next tile (231 / 19 / 184), when the target moved (185 / 66 / 86),
  at a newly seen wall (53 / 2 / 64), or by drift (45 / 37 / 4). Searches failed 54 / 83 / 131 times. The A* is
  per request and starts from scratch each time.
- Trail (Cfg.TRAIL, off by default): each baby keeps the loop-free chain of tiles it has walked since its spawn
  beside a king. Stepping back onto the chain cuts the loop, and a jump (carried, thrown) starts a new chain. A
  delivery that has not got closer to the nearest king in Chebyshev distance for FLOW_STALL turns walks the chain
  back. Each turn it steps to the free neighbour earliest on the chain, which cuts corners. If robots hold every
  earlier neighbour it waits FLOW_HOLD turns, then lets Nav route around them. A roaming king does not reset this
  count. The chain is walked ground, so it gets through any pocket the rat came out of. It costs about 50
  bytecodes a turn and two W*H int arrays. Unit tests cover loop cuts, corner cuts, HELD, the avoid mask, jumps,
  and a carrier behind a wall.
- s5c = s4d + CHEESE_BAN 60 (no KING_BLOCK); s5d = s5c + TRAIL. Diagnostics on the five maps are running, and
  sp18/rx screens for s5c and s4v are queued after them.

## 2026-10-29 00:55 PDT: KING_FLOW, TRAIL's jam, and a war strike
- s5c (s4d + CHEESE_BAN, no KING_BLOCK): 6521 stuck rounds on the five diagnostic maps (s4d 7295). Without KING_BLOCK,
  babies bound for a mine still stand touching the king on EscapeTheNight (2394 rounds).
- KING_FLOW (off by default): our kings' bodies block flow paths only. Greedy steps and bug walks still treat a king
  as a robot to wait for or sidestep, which was KING_BLOCK's other half. s5e = s5c + KING_FLOW: 5080 stuck rounds
  (EscapeTheNight 1731, fullofsecrets 2806). Its sp18 screen is queued.
- TRAIL works for carriers (s5d vs s5c: pipes 'd' 913 -> 121, fullofsecrets 1339 -> 155), but both TRAIL builds jam
  EscapeTheNight. There, babies bound for a mine fill the king's box: s5d 13763 rounds, s5f 14883 (72% of baby
  turns, lost 15-84). The king sits in a walled box whose only way out is a dirt passage west. Every mine lies past
  a 9-row dirt band (y 25-33) that must be dug. Trail carriers deliver in the box mouth and then stay there, bound
  for a mine they cannot path to. s5d and s5f are withdrawn. A trail that helps there must not fill the only exit.
- Field rushers: in s3y's 36 panel games against AlexT101 and abcdezyxwv (2-16 each) the enemy king took no bites.
  DefaultLarge vs abcdezyxwv (match 1600): kills about even (kd 1.26), but our bank fell from 1620 to 587 between
  r75 and r125, mostly on 45 rat traps (900 cheese, 17 kills). We then could not reinforce (rats 20 -> 0, theirs
  15-24), and their rats kept gathering during the war (deliveries 695-845 per 50 rounds vs our 118-20).
- WAR_STRIKE k (off by default): once cooperation is over, one in k fighters (chosen by birth round, so mirrored rats
  agree) becomes an attacker. It walks to the enemy king (seen, last heard, or the symmetry mirror of home) and
  bites it. s5g = s4d + WAR_STRIKE 1 is on the replica (block 1 from match 1629) and on the rush run vs abrk
  (rx_s5g). s5h = s4d + WAR_STRIKE 2 follows. Almost every field bot backstabs eventually (all but bc26vibe_bot), so
  war play matters in most panel games, and the sp screens never reach war.

## 2026-10-29 01:05 PDT: how the rushers win, and the enemy-king slot no rat read
- s5g's replica block was dropped once its rush run showed the cost. In the first 6 cells vs abrk our king died
  every time; at 26 of 80 cells it is net -2 (t -1.20). s5e (s5c + KING_FLOW) took its replica slot (submission 77,
  block from match 1633).
- Field forensics on s3y's 144 panel games (replica replays, actions.tsv):
  - Rat deaths are even against both rushers (abcdezyxwv 34.4 ours vs 34.3 theirs per game; AlexT101 33.7 vs
    30.9). The games are lost at the king. Our king takes about 200 bite damage and 230 (abcdezyxwv) or 150
    (AlexT101) thrown-rat damage, mostly from our own rats that they grabbed and threw. Their king is never bitten.
  - The grab war: abcdezyxwv grabs 66.7 of our rats a game and we grab 44.1 of theirs. 69% of their grabs take a
    rat with less hp than the grabber, and 52% come from outside the victim's cone. Most victims were facing an
    enemy ('f', 16.9) or in war movement ('w', 15.8).
  - Income: in rounds 100-400 their rats pick up 600-700 cheese a game on our half, close to our own 680-850. We
    pick up almost nothing on theirs (0-114). Against peaceful bots they take 3-226 from our half.
  - Rat traps: about 1.7 hp of damage per cheese for us and 1.95 for them. We place 40 a game against abcdezyxwv,
    twice their count, and they keep a bank of about 1900 while ours drains.
  - The kings sit 7-15 tiles apart (Chebyshev) for hundreds of rounds on DefaultLarge, heart, uneasy_alliance,
    Excavation and knifefight. Both kings roam 5-18 tiles from their starts.
  - All 126 of our bytecode overruns are a newborn's first turn (array allocation), 84 of them with no enemy near.
- King-press reach: kings write squeaked enemy-king sightings into a shared slot (Comms.EKING), but no rat ever
  read it. A sighting reached only the rats within PRESS_HOPS squeaks (d2 16 each).
- PRESS_SHARED (off by default): KING_PRESS also chases a fresh shared sighting, and kings write the enemy kings
  they see themselves. Unit tests cover it.
- s5i = s4d + KING_PRESS with PRESS_D2 225, PRESS_AGE 16, PRESS_HOPS 3. s5j = s5i + PRESS_SHARED. Their rush runs
  vs abrk follow sp18_s5c on the VM. rx_s4m (plain KING_PRESS) now runs right after rx_s5g.

## 2026-10-29 01:20 PDT: a mirror-invariant rat hash; abrk is a weak stand-in for the field rushers
- sp18_s5c: neutral vs sp14_s4d (80 cells, D 57, -0.6 pts, t -0.26, net -2). rx_s5g at 55 of 80: +3.4 pts, net +1.
  It recovered from 0 of 6 at the start.
- Symmetry: Baby.slot hashed rc.getID(). It decides the scout pick and MINE_BALANCE's mine, so mirrored rats could
  get different roles and mines (mirror_check.py listed this as a known asymmetry). SYM_SLOT (off by default)
  hashes the mirror-invariant birth seed instead: round plus the unsigned offset from the king, now
  Baby.birthSeed with a mirror test. s5k = s4d + SYM_SLOT; s5l = dev + SYM_SLOT is only its partner for mirror
  checks. Mirror pairs s5k/s5l and s4d/s2o (DefaultMedium, cheesefarm) are running on the VM.
- abrk vs the field rushers (16 kept-replay games s4d vs abrk, profiled like s3y's panel games):

  | Measure | abrk | Field rushers |
  |---|---|---|
  | Backstab round | r66 | r36-40 |
  | Grabs, theirs vs ours | 39 vs 46 | 53-74 vs 42-53 |
  | Cheese picked on our half, r100-400 | 156 | 600-700 |
  | Picked on their own half | 1056 | 1285-1612 |
  | Bank at r100 | 686 | 960-1234 |
  | Bank at r200 | 454 | 809-940 |

  abrk is poorer and opens the war late, which explains why rush-run passes (s4g, s4h, s4j) did nothing against
  the real rushers on the replica (s4j 2-10 vs them, s3y 3-9).
- New archetype switches (off by default): BREAK_ROUND (every baby is hostile from that round) and MINE_ANY
  (gatherers spread over all known mines, not only our half). `bots/araid` = the s4d switch set + SYM_SLOT + RUSH
  + RUSH_BREAK + BREAK_ROUND 36 + MINE_ANY. s3y vs araid on the eight rusher maps, both sides (rxa_s3y), checks
  whether it reproduces the field rushers' profile and s3y's 2-16 against them.
- Mirror check: with SYM_SLOT the first divergence of a mirrored pair moves from r5 to r40 on DefaultMedium (s4d/s2o
  vs s5k/s5l) and from r3 to r22 on cheesefarm. The early rat-hash asymmetry is gone, but something later still
  breaks symmetry: at DefaultMedium r40 two twin gatherers at the same tile pick moves one tile apart.
- s5e (KING_FLOW) on the replica, block 1 vs the s3y reference: 48 cells, D 14 (+5 -9), net -4, t -1.07.
  Rejected.
- rx_s5g (WAR_STRIKE) vs rx2_s4d: 80 cells, D 53, +4.1 pts, t +0.77, net -2. That's neutral, but it's the best
  signal on deck and aims at the rushers' unbitten king, so s5g is on the replica now (submission 78).

## 2026-10-29 01:35 PDT: araid replaces abrk as the rush screen; an early insurance king; mirrored flow search
- rxa_s3y (s3y vs araid, 16 kept games on the eight rusher maps): s3y won 5 of 16. Its profile (`profile.py`):

  | Measure | araid | Field rushers |
  |---|---|---|
  | Backstab | r45 (94% theirs) | r36-40 |
  | Throw damage on our king | 203 | 150-230 |
  | Bites on our king | 13 | ~20 |
  | Picked on our half | 211 | 600-700 |
  | Bank at r100 | 680 | 960-1234 |

  araid is a weaker version of the same style, and far closer than abrk (s4d beat abrk 7 of 16). The rx runs vs
  abrk stopped (rx_s4m at 25 of 80: +5.4 pts, t +0.64; rx_s5j at 52 of 80: +6.3 pts, t +1.29; s4o, s5h, s5i
  and s5c's rx runs dropped). The new ra screen is the same 80 cells vs araid: ra_s4d (control), ra_s5g, ra_s5m
  on one stream; ra_s5o, ra_s5i, ra_s4m on the other.
- Grab forensics (s3y's panel games): our grabbed rats had already lost hp to rat traps (the first damage in 340
  of AlexT101's 957 grabs; victim hp peaks at 50, one trap) and to landings from earlier throws (22.5 hp per grab
  vs abcdezyxwv). Their grabbers are fresh (100 hp in about half the grabs). The engine lets a strictly
  healthier adjacent rat grab from any side, so a trapped rat is fair game for every fresh rusher.
- Kings: s3y never had a second king in 144 panel games. s5e (s4d's formation switches) formed one in 14 of 48,
  but vs the rushers only 3 of 12, at r105-228. The war starts at r36-40 and the bank starts near 2500, so
  s5o = s4d with FORM_FROM 25, FORM_BABIES 8, FORM_CHEESE 150 and no income test: a second king early, before the
  war, so the rushers have two kings to kill.
- Symmetry: the s5k/s5l split at DefaultMedium r40 is two twin rats on equal-length (17-tile) flow paths that
  disagree on the first step. The side decides it, not the build: in both games the side-A twin steps
  diagonally. Flow's A* expanded neighbours in a fixed order; SYM_FLOW (off by default) expands them in the side
  frame (the signs of the home king's offset, which flip exactly along the map's mirror axes whether or not the
  bot knows the symmetry yet). In random 15x11 symmetric layouts the fixed order splits 1 of 211 pairs and the
  framed order none; a unit test pins the splitting layout. s5m = s5k + SYM_FLOW (partner s5n) is in a
  mirror check.

## 2026-10-29 01:59 PDT: mirror check on s5p; the rushers win the war economy, not the opening
- s5p/s5q (all four SYM switches) mirror check: first divergences moved from DefaultMedium r63/r87 to r75/r93,
  cheesefarm r37/r14 to r82/r107, sunandmoon r2 to r22/r25. What is left starts at cat moves ('g' cat defend,
  'c' cat flee) beside cats whose hp differs by one scratch. SYM_CENTRE fixed the gatherer's cat flee (it aimed at
  the cat's bottom-left tile, not its centre), king spawn and trap goals (integer W/2 centre), and fort facing.
  SYM_HAND fixed the cross=0 tie (a chirality from the home king's quadrant). All four switches are off by
  default; s5p goes to a self-play screen for harm before they become defaults.
- s3y's record by panel opponent on the replica (144 games): AlexT101 2/18, abcdezyxwv 2/18, CS50Crab 11/18,
  outercloudstudio 12/18, bc26vibe 12/18, XSercia 17/18, C0derTang 17/18, ask8000 18/18. 41 of our 53 losses are
  king kills.
- The two rushers are not ahead on rats early: at r100 we average 16-20 babies to their 15-17. By r300 we are at
  10-12 and they are at 16-19. AlexT101 on constrictinglattice (1204 g0): to r75 we spawned 31 and they 31, our
  traps hit them 30 times and theirs hit us 19 times, and they lost 16 rats to our 10. But our bank went
  2500 -> 690 by r75 (spawns ~1200, 37 rat traps 740, upkeep 150) and income stopped: deliveries ('d' turns) fell
  to 4-5 per 25 rounds after r150, with the gatherers walking to mines ('M') and fleeing. From r100 the king sat at
  'f' (under the 300 spawn floor) for 100+ rounds while the army shrank 12 -> 2. AlexT101 rebuilt from 11 to 17
  on a steady ~600 bank.
- The cap: MAX_BABIES 18 is checked against the cost-step count, so we stop at 20 babies. outercloudstudio and
  bc26vibe peak at 33, and EscapeTheNight (1196) ended with 9322 cheese unspent on 20 rats. On tiny (1204 g2) our
  king died at r48 holding 1105 cheese with 20 rats to their 31. s3u (RICH_BABIES 30) passed self-play but never
  went to the replica on its own, and s4h/s4j raised only the war cap. s5r = s4d + MAX_BABIES 30 +
  MAX_BABIES_WAR 40 is on the ra screen (vs araid) and the sp screen.

## 2026-10-29 02:41 PDT: s5g rejected; Stash's through-the-wall chase (CHEESE_LOS, s5t); enemy traps on our half
- s5g (WAR_STRIKE 1) replica block 1770-1785: net -5 (gained 4, lost 9), t -1.39; graded -3.16, t -0.94. It
  took the CS50Crab cells and lost ask8000 fullofsecrets, bc26vibe krotus, AlexT101 popthecork/uneasy_alliance,
  outercloudstudio heart and abcdezyxwv DefaultLarge. Rejected. s5o (early insurance king) on the ra screen:
  -5.9 pts, t -1.19, net -3 vs ra_s4d. It is not going to the replica.
- Stash vs abcdezyxwv (1210/1211): the map has a wall row at y=22 (dirt gaps at x=0,1,28,29). Our 'M' rats stay
  on the far side and chase piles they see through the wall ('C'). Each new target resets the stall count, so
  Flow is never asked for a path. To r100 we picked up 9 piles to their 52. In r100-200 it was 0 to 63, and by
  r300 they had taken 78 piles on our half to our 16. CHEESE_LOS (Baby.walledOff walks the greedy line through
  the known map) skips such piles. s5t = s4d + CHEESE_LOS. In self-play on Stash and pipes it changes nothing
  (own-half pickups to r300 84/79 vs s4d's 89/83), because s2o never raids our half. Only the replica can test it.
  The ra and sp no-harm screens are queued.
- The same own-half count over s3y's 144 panel games: the large deficits are Stash (us 16 / them 78),
  AlexT101 safelycontained (58/96) and outercloudstudio seepastyourgreed (11/48, which we won anyway).
- Enemy traps are our biggest single killer against two panel opponents. Our rat deaths per game by cause:
  CS50Crab 59.3, of which trap 40.9; AlexT101 46.2, of which trap 18.8 (throw landings 11.8).
  CS50Crab lays 86 of its 115 traps per game on our half; AlexT101 lays 48 of 54. AlexT101 lays them early: 262
  of its traps on our half (18 games) went down in r0-100, mostly 4-12 tiles from our king. That is a ring around
  our delivery approach. On starvation (1204 g1) the trap exchange to r125 was even (39 hits each way). Our
  economy lost: pickups 16 vs 31 to r125, then 13 vs 39. Our bank sat at 130-190, under the 300 spawn floor,
  while theirs held ~550 and kept spawning. Our gatherers spent most turns as 'W' (waiting at an empty mine).
- A rat trap fires on an enemy entering any tile within r2 2 of it, and the layer must stand within r2 2 to
  place it. Our traps don't block theirs (one trap per team per tile). Enemy traps are invisible to us.
- s3y is resubmitted for a 6-match reference block (1786-1791) against spsquared_TSPAARK_QUALS,
  amcsz_fighterrats and AdamTan12_Finals (maps drawn into research/panel/v3-set1.json), so the v3 panel can
  replace the three opponents we beat 17-18/18.

## 2026-10-29 02:59 PDT: traps set up the grabs; the axis mines; v3 panel; the cap on rich maps
- Enemy trap hits on our rats in s3y's panel games (CS50Crab 1833, AlexT101 907). In 96-98% the trap sat on one of
  the placer's three front tiles, which is TrapRisk's model. 82-92% of placements happened in view of one of our
  robots, and in 50-60% of the hits the victim itself had seen the placer. 81-92% fired within 25 rounds of
  placement, and an enemy rat stood next to the trap in 70% of hits. The victims' labels were about 40% combat
  ('w', 'f') and 50% economic (C 241, M 248, W 111, d 50 vs CS50Crab). TrapRisk's mask is dropped for every rat
  with an enemy within dist2 20 (RISK_IN_FIGHT false, s2v), which is exactly when those traps are there. Only 1%
  of the hits came from traps laid during cooperation.
- Enemy grabs of our rats: abcdezyxwv 1328, AlexT101 957. 70-76% took a rat that saw the grabber but had lower hp
  (a grab succeeds on an unseen or weaker victim). Victim hp peaks at 50-59, one trap hit. Vs AlexT101, 309 grabs
  came within 10 rounds of the victim being trapped: trap (50 dmg, 3 turns stunned), then grab, then throw. Kills
  are even against the strong three (AlexT101 30.9/33.7, abcdezyxwv 34.3/34.4, CS50Crab 42.7/43.6 per game).
  Income decides those games.
- Arms vs atrap, a new archetype (araid + TRAP_CAP 25, min cheese 40, trap toward any enemy within dist2 20):
  s5u = s4d + RISK_GATHER (gatherers keep the mask when engaged) and s5v = s4d + RISK_IN_FIGHT (everyone keeps
  it), with control at_s4d. Replays are kept (screenq: a runs/NAME/KEEP file sets BC_KEEP=1).
- Axis mines: the engine pairs each mine with its mirror. A mine on the symmetry axis or at the centre is its own
  pair, so one spawn event drops both piles around it, twice a normal mine's yield. 6 of 74 maps have one:
  50% of all cheese on starvation, Excavation, mincemeat, 78% on EscapeTheNight, 100% on ultimatestarvation and
  5t4rv4t10n_1337. Our gatherers skip a mine as near the enemy start as ours (MINE_TIES off) unless no other
  mine is known. On starvation vs AlexT101 (1204 g1) the middle mine dropped 56 piles in 268 rounds: they took
  49, we took 1. Our rats spent 1288 rat-turns at our corner mine and 18 at the middle. s5w = s4d + MINE_TIES is
  on a 24-game check vs araid on the four tie maps (mt_s5w, control mt_s4d).
- v3 panel: v2 minus XSercia, C0derTang and ask8000 (s3y 17-18/18), plus spsquared_TSPAARK_QUALS (s3y 3/6) and
  AdamTan12_Finals (1/6). amcsz_fighterrats (6/6) was dropped. s3y references on the new opponents' set-2 and
  set-3 maps are running (1792-1799); research/panel/v3-ref-s3y.txt lists all three sets. A v3 block is 14
  matches.
- superioristhmus vs spsquared (1786/1787): we finished on 10904 and 8758 cheese, with 39.6k rat-turns to their
  93.7k and 109k, and lost on points 27-72 and 26-73 (xfer 19k vs 55k-58k). The 18-baby cap again. ra_s5r (caps
  30/40) is at 62/80 cells, net +7 cells, -1.9 pts, t -0.37: no harm. It goes to the replica next, on v3 set 1.

## 2026-10-29 03:31 PDT: the second king; formation v2; seepastyourgreed
- s3y has formation off, and in 41 long panel games we never had more than one king. Opponents had 2-5 in 8 of
  them, and we lost 6 of those 8. Over all 45 s3y panel games that reached round 2000 we won 32 and lost 13 on
  points. 8 of the 13 losses came with fewer kings than the opponent, and at equal kings 5 of those 8 would have
  been wins. We finished those games on 2k-24k unspent cheese. Kings alive at round 2000 weigh 0.5 of war points;
  "cheese transferred" counts baby-to-king transfers plus a king's own pickups.
- s4d's formation (on its replica games, 1287-1302 and 1770-1785): in most long games the first king never named a
  site. The income test (rate x 0.75 - every king's upkeep >= -100) ignores the bank: seepastyourgreed reached
  8819 at r1000 with no site. pickFormSite needs a home mine whose 3x3 the king has seen, which almost never
  happens because the king hardly moves. Its fallback, four tiles behind the king, is often a wall. Where sites
  were named (cheesebottles), the king chased piles onto its own site: 900-1300 'K' rat-turns per game and no
  king.
- Formation v2, five switches (off by default):
  - FORM_SURPLUS: Econ's projected round-2000 bank with one more king >= 500.
  - FORM_RING: a site just off the king's body (centre 3 out, all of it in view; side-frame order).
  - FORM_STILL: the first king stays put while a site is open.
  - FORM_GAP 30: the next site may open 30 rounds after a king forms.
  - FORM_LATE: a lone king may form a second up to r1940. The engine's r1200 cutoff binds only at 2 kings.
  - Candidates: s5x = s4d + all of it, FORM_MAX 5. s5y = s5x from r600. s5z = s5x + s5r's caps 30/40.
  - Check vs s4d (fk_s5x, 6 maps x 2 sides): 5-7. superioristhmus formed 4 kings on both sides (r82-421) and won
    both; the kings settled on four different mines. diamond formed 2. DefaultMedium formed 2-3 at r103-210 and
    lost both early (r148, r273). No site on cheesebottles, wallsofparadis, seepastyourgreed (surplus negative).
    ra_s5x and ra_s5y are queued; 40 of ra_s4d's 80 games go to round 2000.
- seepastyourgreed is a serpentine of wall rows with gaps at alternate ends. In fk_s5x neither side had income for
  500 rounds (s5x starved on both sides; s4d's bank reached 52 on one). The babies chase piles seen through the
  wall rows ('C' was 60-70% of their turns). s5t (CHEESE_LOS) vs s4d there, both sides: won both, income from
  r300-500 (bank 6060 at r500 vs 1190).
- AdamTan12 beat s3y 0-3 twice on wallsofparadis, keepout, where_are_you (1798/1799), forming 2-5 kings per
  game. Their extra kings form 6-48 tiles from the first, often at the centre or a far mine, from r120-260 on,
  with a bank of 1150-2900. On tiny and DefaultSmall (1790/1791) both sides laid 25-56 rat traps by r120, and
  nearly every one hit. Our king died to bites after our bank fell to about 100 by r70-80 (spawn replacements at
  60 apiece).
- s5r (caps 30/40) is on the replica: v3 set 1, from match 1800.

## 2026-10-29 04:38 PDT: where s3y's losses come from; trapped mines
- Screens since 03:31:
  - s5r (caps 30/40): rejected on the replica (v3 set 1, net -4, t -1.07). On cheesebottles it formed a second
    king through s4d's income test and both kings starved; on superioristhmus its cheese transfer barely moved
    (19-23k vs their 53-61k).
  - ra_s5t (CHEESE_LOS): reject, -7.6 pts, t -1.75. ra_s5s (TRAP_SPAWN_FIRST): pass, +1.2, net +12.
  - mt_s5w (MINE_TIES): strong pass on the four tie maps, +27.2 pts, t +2.84. On EscapeTheNight our pickups
    at the axis mines rose from 778 to 2620. s5w is on the replica (v3 set 3).
  - ra_s5x (formation v2) stopped at 35 of 80: -9.7 pts, t -1.92. On evileye (s5x and s6c vs araid, both
    sides) all four games were lost to kings bitten to death, not to starvation. The second king formed at
    r739/818 with the bank pinned near 300, because the surplus projection ignores spawn spending.
- FLOOR_PER_KING (off by default): spawn floors, the upkeep reserve and the trap minimums scale with live kings.
  s6e = s5x + FLOOR_PER_KING + FORM_CHEESE 1200 is on ra_s6e.
- s3y's reference record by opponent (66 replica matches): AlexT101 2/18, abcdezyxwv 2/18, AdamTan12 4/18,
  spsquared 9/18, CS50Crab 11/18, everyone else 12-18/18. 41 of the 56 lost games end with our king destroyed;
  15 are lost on points.
- Against AlexT101 the kills to r150 are even (our rats lost 428, theirs 419 over 18 games), and so is the spend
  (spawns 25.0k vs 25.2k, traps 12.0k vs 9.1k). What differs is income. Pickup events to r300
  (`halfpick.py`):
  - They take nearly every pile on their own half (50-280 a game); we take 0-15 there.
  - They also take 20-95 on ours, about 30% of it.
  - Mines spawn paired piles, so both halves produce the same cheese. They get about 1.3 halves and we get
    0.7, a 1.9x income gap. It shows as a bank of 100-300 for us at r150 against their 500-1000. Their army
    then outlasts ours, and the king falls.
- Engine facts behind traps (TrapType, GameWorld.triggerTrap):
  - A rat trap costs 20, deals 50, and adds 30 movement cooldown (3 turns rooted; the victim can still act).
  - It fires on an enemy entering any tile within dist2 2 and is used up.
  - It never fires on its own team, and enemy traps can't be sensed.
  - A fired rat trap starts a backstab by its owner, so traps go down only after cooperation.
  - The team cap is 25 live traps, and a baby places only on an adjacent tile in its cone.
  - Cheese spawns uniformly in the 9x9 square around a mine and is picked up from dist2 2.
- MINE_TRAPS (off by default): a gatherer at its mine lays rat traps on the mine's MINE_TRAPS trap sites nearest
  the enemy start.
  - The sites are offsets 0/+-3 in each axis: eight 3x3 trigger blocks that tile the spawn square, ranked by
    distance to the enemy start, with ties broken in side-frame order (a mirror unit test covers it).
  - It runs only after cooperation, with 400 banked, and while we have fewer than MINE_TRAP_CAP (10) live traps.
    The combat cap rises by 10 so these traps don't crowd out the ones laid in fights.
  - s6f = s4d + 4 sites per mine; s6g = s4d + all 8.
  - Sanity game s6g vs araid on DefaultMedium: 129 traps laid, 118 hit (4610 damage), rat kills 82-30. It still
    lost at r553: a cat scratched one king and a throw finished the other.
  - ra_s6g and ra_s6f are at the head of the VM queue.

## 2026-10-29 04:54 PDT: s5w rejected; s6j to the replica; where the babies stand; raiders again
- s5w on the replica (v3 set 3 vs the s3y reference, 1835-1840, 1847-1852, 1870-1871): 42 cells, D 6 (+3 -3),
  net 0, t 0.00. Rejected (block 1 needs t > 0). Only Excavation of set 3 is a tie map, so the block mostly
  measured noise for MINE_TIES. Its own screen (mt_s5w, +27.2 pts, t +2.84 on the tie maps) stands.
- ra_s5y (formation v2 from r600) vs araid: 80 cells, D 49, -0.8 pts, t -0.19, net +9 (+19 -10). Pass.
- s6j = s5y + MINE_TIES (submission 82) is on the replica, v3 set 1. That set has superioristhmus and jail, two
  of the points losses.
- Where babies stand in s3y's reference games, r100-300 (`posture.py`; per round: alive, at a mine of their own
  half, at a mine of the other half, carrying cheese, within dist2 36 of their own king, on the other half):

  | Opponent | Us: alive / own mine / carry / near king / other half | Them: same |
  |---|---|---|
  | AlexT101 | 11.5 / 8.7 / 1.9 / 8.1 / 0.1 | 15.4 / 6.8 / 5.9 / 3.8 / 3.9 |
  | abcdezyxwv | 16.0 / 9.0 / 2.1 / 9.3 / 0.8 | 16.8 / 6.3 / 4.8 / 3.5 / 4.2 |
  | AdamTan12 | 16.2 / 10.4 / 1.7 / 7.9 / 0.3 | 16.6 / 3.8 / 6.4 / 4.2 / 5.1 |
  | CS50Crab | 15.9 / 10.4 / 2.9 / 7.0 / 0.4 | 11.6 / 3.7 / 5.5 / 2.3 / 1.7 |

  - The top three keep 4-5 babies on our half all the time; we keep under 1 on theirs.
  - They carry cheese 2-3x as often, even the weaker bots (C0derTang 5.6 of 11).
  - 7-9 of ours stand near our own king.
- Raiders were last tried on s2i/s2j (2026-10-28 04:01), a much weaker base, and cost points in a small
  gauntlet. s6i = s4d + RAID_EVERY 2 (a quarter of the babies raid enemy-half mines) + RAID_TRAPS 8 is queued
  after the trapped-mine screens (ra_s6g running; ra_s6f, ra_s6h next). sp22_s5t and ra_s5g were dropped from
  the queue: the replica had already rejected s5t and s5g.

## 2026-10-29 05:19 PDT: big maps: our rats never see most of our own half; HOME_PICK
- where_are_you (60x60, rotational, 8 mines a half), s4d and s6l vs araid locally (wau1, wau2): the king knows 3
  mines (`km=..3`) all game; each baby's `mm` is 1-3. Reporting is not the problem. No rat of ours ever comes
  within sight (r² 20) of 5 of our half's 8 mines. Through r400 every rat-turn falls in x < 36, y < 24. Both kings
  starve in s6l's game (1556 of 7720 home-half cheese picked by r300).
- The cause is how scouts and seekers pick targets. exploreHome takes the nearest never-seen tile of 12 random
  samples. Each baby has its own memory, so every new scout re-crawls the ground next to the king. Scout 13705 was
  at (25,9) at r248, 22 tiles from where it started. SCOUT_AREA 6 (scouting until r600) changed nothing.
- HOME_PICK (off by default): 1 takes the first valid sample (uniform over what is unseen), 2 the farthest.
  Diagnostic games vs araid on where_are_you:

  | Bot | Result | Our pickups | Home mines first seen (of 8) |
  |---|---|---|---|
  | s4d (wau1) | lost r1234 | - | 3: r3, r57, r236 |
  | s6l (wau2) | lost r1406 | 5850 | 3: r3, r60, r152 |
  | s6m = s4d + HOME_PICK 1 | won on points r2000 | 12187 | 4: r3, r115, r157, r806 |
  | s6n = s4d + HOME_PICK 2 | lost on points r2000 | 10257 | 3: r3, r37, r931 |
  | s6o = s6m + SCOUT_AREA 6 | won on points r2000 | 13199 | 4: r3, r115, r157, r816 |

- The four mines still unseen are gated: (39,9), (38,20) and (9,29) sit behind 5-tile dirt bands, and (51,4) is
  at the far end of the bottom corridor (about 46 tiles from the king).
- ra_s6m and ra_s6o are at the head of the VM queue. s6n is not screened.

## 2026-10-29 06:17 PDT: unseen mines, fruitless chases, KING_PLUG, FLOW_PROGRESS, the economy gap
- Home mines never seen (replica replays, 53 maps): by r300, 69 of 193 home-half mines (36%) were never in any
  of our rats' sight. They spawned 74k of the 187k cheese on our half (39%). Most are gated by dirt or walls:
  - heart: the king sits in a walled heart whose exits are all dirt.
  - popthecork: 4 mines inside a walled bottle; the only way in is a 3-tile dirt cork.
  - TheHeist: a vault reached only by a gap at x 51-59.
  - seepastyourgreed: a wall row at y=5 that opens only at x 0-4.
- Fruitless chases: runs of 40+ rounds decoded C or M with no pickup take 12.3% of all our baby turns.
  seepastyourgreed 89%, rose 75%, TheHeist 46%, diamond 46%, cheesebottles 36%, fullofsecrets 32%.
- Grabs and throws vs the top bots:
  - abcdezyxwv makes 73.8 grabs a game to our 52.6. 54% of its throws hit our robots (5.4 king hits and 24 rat
    hits a game); 21% of ours hit theirs.
  - They throw our own rats into our king: 226 king damage a game from abcdezyxwv, 144 from AlexT101.
  - s6p (THROW_FAR_PCT 80) and s6q (THROW_MIN 60, THROW_HOLD 8) are queued.
- KING_PLUG (s6r):
  - The trigger was rose vs spsquared (1796): our king stood at (13,1) for 1937 rounds, and its 3x3 was the only
    exit from the pocket it spawned in. 20 babies were trapped behind it.
  - King.ringArcs counts the open arcs of the 16 tiles around the 3x3 (walls, dirt and the edge close a tile).
    The king never steps where that ring splits into 2+ arcs, unless where it stands is as bad, and it steps out
    of such a spot when it can.
  - Rose diag vs araid: s6r lost both sides (r1060 and r1125; its king starved). s4d won one side on points and
    lost the other at r1985.
  - Once the pocket is open, its rats meet the returning carriers head-on in the 1-wide exit corridor. In
    r200-600 that was 3635 corridor rat-turns (s4d: 334) and 2180 'd' turns, with pickups near 0.
  - spsquared's mirrored king roams (41-46, 1-3), its rats flow through the same corridor (1786 corridor turns,
    mixed traffic), and it picks 382 piles to our 199.
  - Head-on jams (an outbound rat next to a carrier, neither moving for 10+ rounds) are 0.07% of our rat-turns
    over replica matches 1700-1935, most of them on rose and fullofsecrets. A traffic system isn't worth building
    yet. ra_s6r will decide KING_PLUG.
- FLOW_PROGRESS (s6s):
  - Nav's stall counted straight-line progress only. A detour that leads away from the target ended a cheese
    chase at 13 turns, a mine errand at 25, and a raid or explore at 20, while the flow path was still being
    walked. Turns held still for a flow search counted as stall too.
  - On seepastyourgreed, gatherers in the bottom corridor see the mines through the wall above. Each errand held
    3-11 turns for its search, walked part of the 45-tile detour, gave up, and the next errand searched again.
  - Now a step along a flow path resets the stall, and a search hold doesn't add to it. In testFlowProgress
    (a 40+ tile detour) the stall peaks at 3 with it and 78 without. ra_s6s is at the head of the queue, with
    replays kept.
- Why we lose to AlexT101 and abcdezyxwv (block 1943-1946, all 12 games lost):
  - Kills trade evenly. By r150 vs abcdezyxwv, 123 of ours died to 135 of theirs; by r300 vs AlexT101, 187 to
    189. We spend 3120 on traps to their 120 (vs abcdezyxwv).
  - The gap is cheese:

    | Map (r300) | They pick on their half | We pick on ours | They pick on ours |
    |---|---|---|---|
    | pipes | 2700-3140 | 1318-1740 | 500-600 |
    | safelycontained | 3460 | 2320 | 1260 |
    | sittingducks | 3120 | 1615 | 575 |

  - Production on the two halves is identical (spawn events 162/159, 269/268, 171/171). We leave 900-1800 of our
    half's cheese unpicked by r300; they leave 260-1900 of theirs.
  - Our baby turns break down as M (to a mine) 29-39%, W (at the mine) 9-28%, d 11-19% and C 6-11%. We carry
    24-31 cheese a transfer; they carry 33-41.
  - The idle rule sends a gatherer home with only 10 cheese when no pile is in sight. Cfg.DELIVER_IDLE now names
    that threshold. s6t = s4d + DELIVER_IDLE 30 + HUNGRY 50; s6u = s4d + DELIVER_AT/IDLE 50 + HUNGRY 100. Both
    screens are queued behind ra_s6s.

## 2026-10-29 06:21 PDT: s6m accepted (HOME_PICK 1)
- s6m = s4d + HOME_PICK 1: scouts and seekers take the first unseen sample, so they no longer re-crawl the
  ground beside the king. It played v3 set 2 against s3y's reference (1915-1918, 1934-1935, 1943-1946,
  1949-1950, 1963-1964): 42 cells, D 9 (+8 -1), net +7, t 2.33. That passes the bar on block 1 (t ≥ 2,
  D ≥ 4), so s6m is accepted as replica submission #83.
  - Gained: bc26vibe_bot DefaultLarge and sittingducks (both sides), outercloudstudio cheesefarm (both sides),
    CS50Crab tornadoes, AdamTan12 attack_on_cat (A).
  - Lost: AdamTan12 attack_on_cat (B).
  - The gains are on medium and large maps, where the old nearest-unseen pick left most of our half unvisited.
  - Still 0-12 against AlexT101 and abcdezyxwv.
- New reference, research/panel/v3-ref-s6m.txt: set 2 is the accepting block. s6m's control blocks on sets 1 and
  3 are queued (24 requests, drained as the hourly limit allows). No candidate is scored on a set before s6m's row
  for it is in.
- Candidates from now on are built on S6M = S4D + HOME_PICK 1. Screens already running against ra_s4d (s6o-s6u)
  still filter. A screen pass is rebuilt on S6M before it goes to the replica, and ra_s6m becomes the screen
  control once it finishes.
- The BT rating puts s6m at 1540 ± 65 (s3y 1561 ± 51). It has played only the panel block and the strongest
  opponents, and ratings never accept.

## 2026-10-29 06:51 PDT: rats stand still for path searches (16-35% of their turns)
- ra_s6m (s6m against araid on the 80-cell screen) came in at -7.3 points, t -1.59, net -4 against ra_s4d. That
  was the build the replica just accepted, which is more evidence that the ra screen can only reject. ra_s6m is now
  the control for S6M-based screens. ra_s6f is rejected (-5.4, t -1.15, net -3).
- The M analysis on where_are_you (s6m vs araid, local replay with indicators):
  - Re-picking mines is not where M time goes.
  - 59 "wasteful" M trips (more than twice the distance) hold 36% of all M turns.
  - The longest one is a three-rat convoy in a 1-wide corridor, head-on against a returning carrier. The flow
    path holds 3 turns, drops, steps back, searches again and steps forward: 13 tiles in 194 rounds.
- A new measure, stuck_report "idle in travel": a baby with a travel code that could move (mcd < 10) but stayed on
  its tile. Replica matches 1850-1964 (99 of our games): 8.3% of all baby turns idle beside an ally, 2.6% alone.
  The worst maps:
  - rose 68%
  - Hike 36%
  - Stash 20%
  - Nofreecheese 12%
  - Most of the "beside an ally" turns (61%) are M rats within 3 tiles of our king, and 62% of them have nothing
    in the tile ahead.
- The indicators show what those turns are. Flow.request starts a multi-turn A* search, and Nav holds the rat
  still until it finishes:
  - where_are_you: 16.2% of rat-turns stand still in a search, 877 searches averaging 6 turns. Paths get 3.5 steps
    before they are dropped (343 held by a robot, 293 replaced by a new target, 159 walls).
  - Hike: 35.2% of rat-turns; 41% of all M turns and 50% of all E (explore) turns. 155 of 597 searches time out
    after 12 turns.
  - Bytecode probe (Debug.PROBE): 621 bytecodes per A* node, about 9,400 spare a turn, so about 15 nodes a turn.
    The indicator string costs 235.
  - Offline bench (test/ratbot/FlowBench, every map, king-to-mine trips, full terrain knowledge): about 530 tiles
    reached per search, paths of about 40 tiles.
- Three new switches, all off by default:
  - FLOW_CACHE: keep the last 4 paths; a request beside one is taken at once, also walked backwards toward its
    start. s6v = S6M + FLOW_CACHE.
    - where_are_you holds went from 16.2% to 14.3%, and Hike from 35.2% to 26.8% (582 cache hits).
    - The searches left are the long first-time ones.
  - FLOW_REV: the search grows from the target back toward the rat. Every tile it reaches knows its way, so the
    rat walks on (greedy/bug) instead of standing still. The path starts wherever the rat is when a tile beside
    it is reached. On the bench it is node-for-node equal to the forward search, with the same path lengths.
    s6w = S6M + FLOW_REV.
  - FLOW_LIFO: buckets as stacks, deepest tile first among equal f, so fewer nodes (0-48% fewer on the bench)
    for the same path cost. s6x = S6M + REV + LIFO + CACHE.
- A side finding: piles of 100-355 cheese build up at unattended mines. A rat takes the whole pile (pickUpCheese
  with no amount), and the cooldown penalty is 1% per cheese with no cap. Rats carrying 455-2375 cheese crawl home
  for 50-130 rounds. Not addressed yet.

## 2026-10-29 07:04 PDT: walking on while the search runs (FLOW_REV) nearly ends the idling; FLOW_FAST

- Diagnostic games vs araid (one game each, local VM):

  | build | map | points | cheese delivered | rounds lost (stuck_report) | idle in travel, beside an ally / alone |
  |---|---|---|---|---|---|
  | s6m | Hike | 79 | 2092 | 9.7% | 20.7% / 7.3% |
  | s6w (REV) | Hike | 88 | 5813 | 0.6% | 2.8% / 0.4% |
  | s6x (REV+LIFO+CACHE) | Hike | 88 | 4836 | 1.4% | 5.6% / 0.8% |
  | s6m | where_are_you | — | 10100 | 3.4% | 11.5% / 10.3% |
  | s6v (CACHE) | where_are_you | 53 | 8797 | — | — |
  | s6w (REV) | where_are_you | 55 | 12292 | 2.1% | 4.5% / 0.9% |
  | s6x (REV+LIFO+CACHE) | where_are_you | 54 | 10275 | 1.6% | 5.6% / 0.7% |

  - FLOW_REV alone gives the best economy. The cache and LIFO ties add nothing on top of it.
- FLOW_FAST: the reverse search with its eight neighbours unrolled for tiles off the map edge, the queue state in
  locals, and the heuristic read from a fixed |d| table.
  - It follows the side-frame neighbour order (ox/oy), so a mirrored rat runs the mirrored search at the same
    cost.
  - BotTest compares it with the plain loop: the same path in 96 of 96 searches (random walls and dirt, all 4
    frames, FIFO and LIFO ties, a rat that walked on).
  - Probe: 543 bytecodes per node for the plain reverse loop, 344 with FLOW_FAST (37% cheaper).
  - The first version rebuilt per-search heuristic columns at every search start (about 2,100 bytecodes). On the
    turn that allocates the queue arrays this caused one overrun in the probe game, so it was replaced by a
    129-entry table built once inside the existing guarded allocation.
- New candidates, all on S6M:
  - s6y = REV + FAST.
  - s6z = s6y + FLOW_RESERVE 1300 (from 2500; the indicator costs at most 241 and the tag 400).
  - s7a = s6y + FLOW_STALL 2: with REV a rat no longer pays for searching, so it can ask for a path sooner.

## 2026-10-29 07:19 PDT: points weights; s6m's replica losses; the throw gap

- The engine's round-2000 points: in cooperation 50% share of cat damage, 30% share of kings alive, 20% share of
  cheese delivered; at war 30/50/20. No new king after round 1200 once a team has 2 (kings formed earlier stay,
  up to 5). Formation v2 (s6j, up to 5 kings) was neutral on the replica.
- s6m's 42 replica games so far:
  - Points games: coop 6-0, war 5-7.
  - King-destroyed games at war: 8-14. Coop: 2-0.
  - All 12 games vs AlexT101_usqualsbot and abcdezyxwv_bestsofar were lost by king kill. Some end very early:
    averyfineline at round 96-113, evileye at 220-267.
- 1946 g0 (averyfineline vs abcdezyxwv, our king dead at r96):
  - Kills were even to r100 (their 31 rats, our 29).
  - They spent 1610 cheese (40 on traps) and still banked 777. We spent 2440: 41 rat traps (820), 15 cat traps,
    and 35 spawns at the 20-baby cap. Our bank was 263 at r49, when theirs was 1290.
  - From r55 they rebuilt and we could not: 16 -> 4 babies vs their 15 -> 13 by r79. Then four or more of their
    rats bit our king to death in 16 rounds while it bit back once a round.
  - We bit the cat 69 times and laid 15 cat traps in that early war.
- The grab war (grabs.py, r <= 400, 4 matches each):

  | | grabs | throws | dmg per throw | throws hitting an enemy robot | throw kills |
  |---|---|---|---|---|---|
  | us vs abcdezyxwv | 264 | 208 | 16.9 | 11 (and 9 into our own) | 88 |
  | abcdezyxwv | 349 | 252 | 26.4 | 57 (50 rats, 7 king) | 122 |
  | us vs AlexT101 | 237 | 185 | 17.8 | 8 | 68 |
  | AlexT101 | 280 | 230 | 15.2 | 48 | 100 |

  - Whole games (hitvict.py): 190 of abcdezyxwv's throws and 139 of AlexT101's hit our robots; 50 of AlexT101's
    hit our king.
  - Most hits are point-blank: in the throw round, 2-3 tiles from the thrower. They throw a grabbed rat of ours
    into our other rats or our king, which costs us twice.
  - The victims were mostly moving (140 of 186 had moved in the 3 rounds before), so dodging is not the fix.
  - Our throws mostly end on the ground (10) or a wall.
- s6p (S4D + THROW_FAR_PCT 80) is on the ra screen.

## 2026-10-29 07:30 PDT: a class's static block was the newborn overrun; symmetry switches on
- Mirror check of s6y/s6m (mr1, both seatings): the first divergence is r2 on where_are_you, where twin newborns
  spawn one tile apart, and r5 on Hike. That matches s5m/s5n before the SYM switches. All four switches (SYM_SLOT,
  SYM_FLOW, SYM_CENTRE, SYM_HAND) are still off in s6m and s6y, and s5p's self-play screen (sp21_s5p) is still
  near the end of the VM queue.
- First-turn trace probe (pb3 = s6y plus a per-stage bytecode trace of each rat's first turn, cheeseguardians vs
  araid): 52 overruns, and 51 of them are first turns.

  | Segment | Normal first turn | Overrunning first turn |
  |---|---|---|
  | G.init + Sym.init | 3650 | 3650 |
  | born, comms reads (91 to 1) | 2070 | 2070 |
  | first full sense (1 to 2) | 4280 | 4280 |
  | 30 to 31: Nav.guard, TrapRisk.mask | 51 | 6115 |
  | turn total | 15550 | past 17500 |

  The 6115 is TrapRisk's static block: it builds the 25-entry NEAR table with 200 Math.abs pairs, and it runs the
  first time a robot touches the class. TrapRisk.observe waits for a turn with room, but TrapRisk.mask runs
  whenever we're at war. Before the backstab it isn't called, so only rats born after the backstab paid.
- Fix: NEAR is now a literal, and BotTest checks it against the formula. The only other static block
  (Baby.FLIGHT_DMG) is 7 entries. s7d = s6y + the fix, with overrun probe ov1 on cheeseguardians and
  superioristhmus. s7e = s7d + all four SYM switches, with mirror self-play ms1 (where_are_you, Hike,
  DefaultMedium, cheesefarm) and screen ra_s7e queued right after ra_s6z.
- ov1: s7d vs araid on cheeseguardians had 0 overruns (pb3 had 52; s6y in cg1 had 39 and 55).
- CARRY_AIM (off by default; s7f = s7d + it): a carrier holding an enemy rat checks its own tile and the 8 it can
  step to for a throw whose point-blank tile holds an enemy robot (a baby scores 84, a king 126). It turns to the
  throw direction, steps there (a strafe if the step isn't ahead, so the turn, move and throw fit in one turn) and
  throws at once if its action is ready. Otherwise it holds the line for the next turn. WHY code 'T'. Engine
  check: the 1.5x carry movement multiplier is cast to int, so carriers move at full speed, and an enemy wriggles
  free after 10 of its turns. Diag ca1 (s7f and s7d vs araid on 4 maps) and screen ra_s7f are queued.
- Correction to the grab-war table above (grabs.py, s6m's 6 matches vs AlexT101):
  - By total damage per throw (the thrown rat plus what it hits) we're ahead: 18.7 to their 13.5. Our throws mostly
    hit walls point-blank (42 to their rat).
  - Throw kills are close: 156 ours, 186 theirs.
  - The gap is the king. 36 of their throws hit our king, and none of ours hit theirs. The fights happen beside our
    king.
- Where the rats thrown into our king come from (kingammo.py, s6m's set-2 block):

  | Opponent | Throws hitting our king | Damage | Victim within 3 tiles of our king at the grab | Rounds from grab to hit |
  |---|---|---|---|---|
  | AlexT101 | 50 | 1778 | 39 of 46 | 2-3 (35 of 46) |
  | abcdezyxwv | 21 | 848 | 20 of 21 | 2-3 (19 of 21) |

  The ammunition is our own rats in the war near our king (their last move tag was mostly 'w' war or 'f' facing
  an enemy). The carrier throws at its first ready action. The king also takes 170-570 in bites a game in those
  losses, so throws are about a third of its damage.

## 2026-10-29 07:45 PDT: s6m's control blocks; s7d on the replica
- s6m's control blocks are in: set 1 is 1973-1978, 1980-1983, 2001-2002, 2008-2009, and set 3 is 2010-2011,
  2014-2015, 2024-2025, 2032-2037, 2039-2040. They are now rows of research/panel/v3-ref-s6m.txt.
- The same blocks scored against s3y's reference:

  | Set | Cells | D | Net | t |
  |---|---|---|---|---|
  | 1 (control) | 42 | 13 (+3 -10) | -7 | -1.94 |
  | 2 (s6m's accepting block) | 42 | 9 (+8 -1) | +7 | 2.33 |
  | 3 (control) | 42 | 5 (+3 -2) | +1 | 0.45 |

  Over all 126 cells s6m is about even with s3y (net +1). Its acceptance was mostly the noise of a single 42-cell
  block, as the control blocks are there to show. Candidates are scored against s6m's own row for each set, so
  the bar stays fair. A single-block pass is weak evidence, though, and a candidate's first block is no verdict.
- s7d (s6y + the TrapRisk literal) is submission 84, with its first block on v3 set 1 (from match 2053). The ra
  screen for s6y (the same flow code) is at about 20 of 80.
- Mirror self-play of s7e (all SYM switches; ms1):
  - First divergences: Hike r28 (s6y/s6m: r5) and cheesefarm r20.
  - where_are_you still splits at r2. The kings' second spawn ties on centre distance between (3,4) and (4,3).
    G.hand gets c.directionTo(l), which puts both in NORTHEAST, so loop order decides, and loop order doesn't
    mirror. Separately, a per-candidate hand bonus flips under a reflection, so it only mirrors on rotational maps.
  - DefaultMedium splits at r3 because B's king overran its first turn by one bytecode (20001, A 19989; the
    smuggle pad and the debug strings differ by a few bytecodes). It missed the r2 spawn. KING_SENSE_RESERVE 5000
    leaves too little for spawn, publish and the debug strings on a 45x45 map.
- Fixes:
  - G.pickBit(from, l) is the tie bit for ranking candidate tiles: King spawn, the king's traps and dig, and
    Baby's two target picks. Under SYM_HAND it is the sign of the cross product of from's centre offset with the
    exact vector to l, times the side chirality, or the sign of the dot product when the cross is 0. That bit is
    the same for a rotated twin and for a reflected one. BotTest checks it over 4 map sizes x 3 symmetries, and
    checks the (4,3)/(3,4) pair.
  - Without SYM_HAND it is the old hand() of the compass direction.
- s7g = s7e + both fixes, with KING_SENSE_RESERVE 7000 set by snapvar (the default stays 5000, so the @s6m preset
  still reproduces s6m). It takes ra_s7e's place in the screen queue. Mirror self-play ms2 covers where_are_you,
  DefaultMedium, sunandmoon and knifefight.
- CARRY_AIM v1 (s7f) overran. In ca1 it had 102 overruns on DefaultMedium, 18 on cheesefarm, 8 on sittingducks
  and 4 on knifefight; s7d had 0 on all four. 67 of the 98 counted on DefaultMedium crossed in moveDecision's
  segment 7-10, where aimCarry sits, which took ~7350 bytecodes (9 steps x 8 throws x a flight check). The grab
  war there was much bigger (151 grabs, throwHit 1700, against s7d's 55 and 490), but the game was still lost.
- v2 (s7h) starts from the targets: only enemy tiles within Chebyshev 3 can be a point-blank target from here or
  a neighbour. The grid fill runs only when one exists, and the step's canMove is asked only when a line needs
  it. Diag ca2 is on the same maps, and ra_s7h takes ra_s7f's place in the queue.
- s7g mirror self-play (ms2): DefaultMedium r62 (s7e r3), where_are_you r27 (r2; at r27 the twins share a tile
  and only a cat's scratch differs, 100 vs 80 hp), sunandmoon r21. knifefight (vertical) splits at r1 on the
  kings' facing. That facing comes from the map: neither king turns, and the two sides start in directions that
  aren't mirrors of each other. The engine gives a newborn its king's direction (and chirality), so the first
  rats look along unmirrored lines. By r2 one side's rat had ruled out a symmetry (sym=5) and its twin hadn't
  (sym=7).
- SYM_FACE (off by default): the king turns to face each spawn tile before it builds, so the newborn looks
  outward along a line its twin mirrors. s7i = s7g + SYM_FACE takes ra_s7g's place in the queue (ra_s7g hadn't
  started). Mirror self-play ms3 covers knifefight, cheesefarm, sunandmoon and Hike.

## 2026-10-29 08:17 PDT: kings starve in corridors; carry an ally past it (SWAP_HOLD)
- Points in the s6m control games against spsquared and AdamTan12 (2001-2002, 2008-2009, 2036-2037, 2039-2040):
  most losses are king kills at war (12 lost, 0 won). Points games split 4-2 in coop and 0-2 at war.
- How our kings died in those 24 games (ReplayDump die rows, cause column):
  - AdamTan12: bites, in every game it won.
  - spsquared: starvation. Our last king starved in 6 games: bed 2001 and 2002, fullofsecrets 2002, rose 2036 and
    2037, and where_are_you 2040. Each had 50-64 rounds with the bank under 2. Four of them were coop games with
    13-20 babies alive.
- rose (2036 g2): our king's 3x3 body sits in a walled pocket whose only exit is a diagonal into a 1-wide stem
  (x = 9, y 0-5) that winds out to the mines. From r160 three gatherers carrying cheese home ('d') at (9,3)-(9,5)
  faced six outbound ones ('M') at (9,0)-(9,2), (10,2) and (11,0)-(11,1). Neither line moved again, and four more
  'M' rats stood in the pocket behind them. Pickups were 0 from r150, the 460 cheese the rats held was never
  delivered after r850, and 18000 cheese lay unmined. The king starved at r1375 with 20 babies.
  spsquared, in the mirrored pocket, delivered 3700-4280.
- Local: s6m starved on rose in both ra_s6m games against araid (it delivered 430 and 200 to araid's 2320 and
  1620), and in 14 of its 80 ra_s6m games overall: Hike, rose, averyfineline, Stash, krotus, bed, 5t4rv4t10n,
  whereisthecheese, minimaze, EscapeTheNight.
- Engine: carryRat takes the carried rat off the grid (getGrabbed removes it). Carrying an ally always succeeds,
  and a carried rat can't move, turn or act. So a rat can pass an ally in a 1-wide corridor: carry it, step into
  its tile, and drop it behind once the action cooldown allows. That's 20 action cooldown for the carry and 20 for
  the drop, so the drop comes two turns later.
- SWAP_HOLD (off by default):
  - A rat held SWAP_HOLD turns behind an ally baby on its next step does the swap. The step can be a flow-path tile
    or the blocked greedy step.
  - It skips the swap when an enemy rat is near, or when the ally is carrying a rat.
  - Baby.dropAlly sets the ally down on the tile it came from when that is free and beside us, else on the free tile
    farthest from our errand. A carried ally is never thrown: both tryThrow calls check the carried rat's team.
  - BotTest covers a 1-wide gap: off by default the ally holds us; with SWAP_HOLD 2 we pass and the ally lands
    behind us; an enemy rat is never carried.
- s7j = @s6m + SWAP_HOLD 2. ra_s7j is at the front of the queue. Diag sj1 runs rose and fullofsecrets on both
  sides. sv1 runs s6m and s7j against araid on Stash, minimaze, EscapeTheNight, krotus and whereisthecheese, on
  the side where s6m starved.
- Also done:
  - CARRY_AIM v2 (ca2): 0 overruns on all four maps (v1 had 102/18/8/4). Thrown rats hitting enemies did
    682/544/700/84 damage (s7d 490/658/122/0), with the same win/loss as s7d. Its screen ra_s7h is queued.
  - s7i mirror self-play (ms3): knifefight now splits at r10, not r1. There the twins chose explore targets that
    aren't mirrors (17,26 against 22,26 in X's frame). Hike r28, cheesefarm r20 and sunandmoon r21 are unchanged.
  - Screens: s6s (FLOW_PROGRESS) was rejected (-8.9, t -1.95). s6v (FLOW_CACHE) passed (+4.8, t +1.17, net +7).

## 2026-10-29 08:43 PDT: s7d accepted; SWAP_HOLD fixes most corridor starvation; half the cheese lies unpicked
- s7d (s6y = s6m + FLOW_REV + FLOW_FAST, with TrapRisk.NEAR as a literal) played v3 set 1 against s6m's
  reference (2053-2054, 2056-2057, 2070-2075, 2086-2087, 2094-2095): 42 cells, D 10 (+9 -1), net +8, t 2.53.
  That passes on block 1 (t ≥ 2, D ≥ 4): s7d is accepted, replica submission #84.
  - Gained: AlexT101 popthecork (both sides) and uneasy_alliance (A); CS50Crab Nofreecheese (B) and dirtfulcat
    (A); bc26vibe_bot krotus (A) and wallsofparadis (B); outercloudstudio heart (B); spsquared fullofsecrets (B).
  - Lost: outercloudstudio seepastyourgreed (B). Still 0-6 against AdamTan12 (DefaultSmall, tiny, jail).
  - Local agrees: ra_s6y (same behaviour, before the overrun fix) scored +10.4 points, t +2.32, net +13 (+18 -5)
    against ra_s6m.
- New reference research/panel/v3-ref-s7d.txt (set 1 = accepting block). s7d's control blocks on sets 2 and 3
  are queued (6 sent, 22 in the queue). New preset @s7d = @s6m + FLOW_REV + FLOW_FAST (snapvar test checks it
  reproduces bots/s7d). Candidates are now built on S7D, and ra_s6y is the screen control for them.
- sv1/sj1 (s7j = s6m + SWAP_HOLD 2 against araid, on the side where s6m starved):

  | Map | s6m delivered (result) | s7j delivered (result) |
  |---|---|---|
  | Stash | 380 (starved r904) | 13488 (won on points) |
  | minimaze | 3623 (starved r1299) | 11775 (won on points) |
  | EscapeTheNight | 0 (starved r777) | 11023 (lost on points at r2000) |
  | whereisthecheese | 630 (starved r423) | 870 (won: araid's king died r291) |
  | krotus | 557 (starved r363) | 588 (starved r375) |
  | rose (A) | - | 2750 (lost on points at r2000) |
  | rose (B) | - | 60 (starved r1020, 1259 ally grabs) |
  | fullofsecrets (A / B) | - | 2329 / 1790 (won both) |

- rose as B jammed in a new way: three carriers at (50,5), (51,6) and (52,5) each held an ally for hundreds of
  rounds. Each carrier's old tile, where it meant to set the ally down, was filled at once by the next rat in its
  queue, and a 1-wide stem has no other free tile. SWAP_YIELD (off by default): a baby doesn't step next to an
  allied baby carrying an ally, so that tile stays free for the drop. s7k = @s6m + SWAP_HOLD 2 + SWAP_YIELD: diag
  sk1 runs the same maps, and ra_s7k is at the front of the screen queue.
- The economy gap is per rat, not rat count (replica, s6m's three blocks, cooperation rounds only):

  | Rounds | Babies us / them | Pickups per rat-round us / them |
  |---|---|---|
  | 1-50 | 21.7 / 20.5 | 0.178 / 0.208 |
  | 51-100 | 22.2 / 20.6 | 0.265 / 0.301 |
  | 101-200 | 21.6 / 20.7 | 0.279 / 0.385 |
  | 201-400 | 23.8 / 27.9 | 0.314 / 0.405 |

- Both teams leave most mined cheese lying. By r200 the mines had produced 5199 cheese a game, but we had picked
  up 969 and the opponent 1185, leaving 3101 on the floor. At r400 it was 11385 mined, 2111 + 3024 picked, 6580
  lying. By mine rank through r300, our five nearest mines gave us 41/32/32/40/27% of their spawn, and mines
  ranked 6th and beyond gave us 0-11%. The opponents took 42/37/34/42/22/16/9% of theirs. AdamTan12 also took
  420 each from two mines on our half of keepout. Where the lying cheese sits:
  - corridor and dirt maps (Hike: 1120 of 1240 lying at our second mine by r300; EscapeTheNight: nothing picked
    by either team by r300);
  - far mines nobody visits (DefaultLarge (32,54): 760 of 760 lying).
- Where our rats' turns go (replica tag, r1-200): walking to a mine 25.8%, at a mine 14.6%, to a pile 11.4%,
  delivering 8.2%, scouting 7.9%, exploring 7.7%, cat defence 8.3%. A mine pays about 1.6 cheese a round, so
  about 3 rats at mines at a time can't cover 5+ mines. Levers: more rats at mines, larger loads from far mines
  (s6t/s6u test longer trips), and spreading over every mine the team knows of (with symmetry, the enemy half's
  twins too).

## 2026-10-29 09:15 PDT: how the top three kill our king; slow mine discovery; SIEGE_FLOOR, MINE_EXPLORE, SWAP_HEADON
- s7k's corridor diag (sk1, vs araid, 9 maps): it won rose B on points (4590 delivered), both fullofsecrets games,
  minimaze, krotus, EscapeTheNight (17155 delivered) and whereisthecheese. It lost rose A and Stash. On rose A,
  two outbound rats bound for the same mine kept carrying each other back and forth. 9 of 20 babies were being
  carried at r600 and r1200, and the king starved at r1875. SWAP_HEADON (off by default): a rat carries an ally
  past it only when the ally is coming the other way (facing within 45 degrees of the opposite of our step).
  s7r = S7D + SWAP_HOLD 2 + SWAP_HEADON, and s7s adds SWAP_YIELD. Diag sh1 is running.
- Replica results, s6m and s7d blocks (168 games): 76 losses came from a king kill after their backstab, with 26
  wins of that kind. Nearly all of those losses were against AdamTan12, AlexT101 and abcdezyxwv (18-22 lost of
  21-24 each). Their median backstab is r20, and our king dies at a median r374.
- warcmp.py (backstab +0/+40/+80/+120 rounds, means over the lost games):
  - Rat deaths are even or in our favour (+40: 5/10, 8/9 and 6/10, ours/theirs). Spawns are within 3.
  - Their bank holds 1000-1500. Ours is 950 at +40 and 400-800 by +120. The difference is traps: by +40 we have
    laid 24-27 rat traps to their 10, 15 and 1, plus 5-10 cat traps to their 0-2.
  - Over whole games against the three, they spawn 77 to our 53, pick up 6671 cheese to our 2514, deliver 4747 to
    our 1788, and dig 70 dirt to our 22.
- Two sieges:
  - tiny (2094 g2, vs AdamTan12): we laid 37 rat traps in r20-40 (bank 1650 -> 395). From r44 we couldn't spawn
    (bank 192-116 under the floors) while they spawned 14 more from 987. Their fresh wave reached our worn-down
    rats at r52-56, and the king died at r74.
  - DefaultSmall (2094 g0): we out-killed them in r55-100 (26 of theirs died to 16 of ours). But our bank sat at
    308, then 148-108 (the 300 floor with 8 babies), so we spawned 9 to their 20 in r60-100. The king died at r117
    holding 102.
  - DefaultLarge (1982 g0, vs abcdezyxwv): at r190 we had 15 rats near our king (avg 45 hp) to their 8 (avg 74).
    In r180-200 their throws killed 13 of ours (10 on landing), and the king fell 464 -> 178 by r200. The engine
    lets a rat grab any enemy that can't see it or that has strictly less hp, so worn-down rats are ammunition.
- SIEGE_FLOOR (off by default): at war, with enemies in the king's sight, every spawn floor drops to SIEGE_FLOOR.
  - s7t = S7D + SIEGE_FLOOR 20.
  - s7u = s7t + TRAP_SPAWN_FIRST. s7b is the same switch on S6Y; it passed on S5R.
  - ra_s7t and ra_s7u are at the head of the screen queue. The queue was pruned of S4D-era screens
    (s6h/i/k/q/r/t/u, and earlier s4m/s5i/s5s/s5u/s5v).
- Mine discovery (minedisc.py, the same 56 replica matches): by mine rank, the first round any of our rats saw
  each home-half mine against the field's first sight of its own.

  | Home mine by distance | Our median first sight | Field's | Seen by r50, ours / field's |
  |---|---|---|---|
  | nearest | r20 | r14 | 121 / 120 of 160 |
  | 2nd | r60 | r33 | 67 / 96 of 140 |
  | 3rd | r147 | r71 | 31 / 53 of 114 |
  | 4th | r329 | r103 | 21 / 28 of 76 |
  | 6th | never (median) | r209 | 4 / 10 of 32 |

  By r300 the three top bots make about 2x our pickups on their own half (80-212 against our 52-136 on ours),
  and they take another 0-68 on our half.
- MINE_EXPLORE (off by default): until r300, a gatherer that knows m home-half mines explores never-seen home tiles
  when its slot % (m + k) is m or more. With k = 1 that is half the gatherers while one mine is known and a third
  with two. s7v (k = 1) and s7w (k = 2) are in diag eco3 against s7d, with replays kept for the same discovery
  measure. md1 (an s7d mirror on the same maps) is the reference.

## 2026-10-29 09:39 PDT: EscapeTheNight starvation; MapCensus dig columns; eco1, s6z and s7i outcomes
- s7d's set 3 started (2233-2238). Against outercloudstudio the king starved on EscapeTheNight on both sides, 61
  rounds with the bank under 2, holding 19-20 babies.
  - 2233 (A): we picked nothing in 1000 rounds. 79% of our rat-turns were 'C', chasing cheese at x 18-20 that
    we could see through the room's east wall. The room only opens through dirt.
  - outercloudstudio cleared its dirt pocket by r150, then sank one straight shaft up through the 9-row dirt band.
    By r400 it reached the far mine.
  - 2234 (B): we got out after the r317 backstab and picked 8454, but delivered only 4447. They picked 7437 on
    our half.
  - Local etn1 (s7d vs araid, both sides): s7d delivered 45 as A, all rats at king distance 3 with mv=C on piles
    at (18-19, 12-21) for 600 rounds, then mv=E. As B it lost on points. ra_s6y (same behaviour) delivered 11403
    there, so escaping is chaotic rather than impossible. Earlier fixes (CHEESE_BAN, CHEESE_LOS) were neutral or
    rejected.
- MapCensus now reports dig columns from king A (0-1 search, a dirt tile = one dig): freeTiles, minesFree, digNear
  and digHome. research/maps.tsv was regenerated, and tools/tests/test_mapcensus.py checks known answers.
  - Maps where the nearest home mine needs 3+ digs: EscapeTheNight 12, Excavation 6, streetsofnewyork 5, trapped
    5, whereisthecheese 5, mincemeat 5 (no home mine), canyoudig 3, learn_your_lesson 3, pipes 3.
  - Replica s6m+s7d record by dig need: 11/23 (0.48) on 3+ digs, 15/27 on 1-2, and 83/184 (0.45) on none. So
    digging is not where we lose to the field on average. pipes is 0/4 and streetsofnewyork 1/5.
- eco1 (vs s7d, 12 games each on 6 maps):
  - s7m (MINE_ANY_COOP): 6 wins, -0.5 pts, pick x0.95.
  - s7n (MINE_ANY_COOP + MINE_MIRROR): 6 wins, -2.0 pts, pick x1.12.
  - s7o (DELIVER_IDLE 30): 5 wins, -1.7 pts, pick x1.05.
  - None is adopted.
- Self-play keepout is side-biased: whenever a variant plays exactly like s7d there, side A picks 21819 to B's
  18400. The SYM line (s7e/s7g/s7i) is the fix in progress.
- Screens:
  - ra_s6y passes against ra_s6m: net +13, t +2.32. Its behaviour is s7d's.
  - s6z (FLOW_RESERVE 1300) is neutral against ra_s6y: net -1, t -0.06. Not adopted.
  - s7i (SYM_SLOT + SYM_FLOW + SYM_CENTRE + SYM_HAND + pickBit + SYM_FACE + KING_SENSE_RESERVE 7000) passes
    against ra_s6y: +3.3 pts, t +0.65, net +2. The SYM switches cost nothing and make the bot's moves mirror
    across sides, so they go in the next replica bundle.

## 2026-10-29 10:02 PDT: the throw war against the top bots; RETREAT_HP parks ammunition beside our king (s8b)
- s7d's set 3 so far: 0-3 on both sides against AlexT101 (2239-2240) and abcdezyxwv (2241-2242), 3-0 and 3-0
  against CS50Crab_rapierV1 (2237-2238). In all 12 losses our king was killed between r49 and r645.
- Grabs and throws in those 12 games (throwstat.py; damage counts the thrown rat's landing plus any robot hit):

  | | us | them |
  |---|---|---|
  | enemy rats grabbed | 586 | 884 |
  | throws | 411 | 625 |
  | throws landing on an enemy robot | 80 (19%) | 278 (44%) |
  | ... on the enemy king | 0 | 70 |
  | throw damage to enemies | 17.8k | 33.1k |
  | bites on the enemy king | 2 | 259 |
  | cheese picked up (pickups) | 746 | 1707 |

  Over all 90 top-3 games the grab counts are close (4926 against 5446) and so is the damage to the thrown rat
  itself (148k each). The gap is what the throw lands on: 17% of ours hit an enemy robot to 48% of theirs, 27
  of ours hit their king to 365 of theirs on ours, and robot hits did 25k damage to 68k. Most of our throws hit
  a wall (2852 of 3860).
- Who gets grabbed (napwhy.py: the victim's smuggled decision code the turn before): both sides take mostly weaker
  victims (75%; victims average 50 hp against grabbers' 78). Our victims were in war ('w', 27%), facing an enemy
  ('f', 22%), going to a mine ('M', 12%) or retreating ('r', 6%). The rats thrown into our king were mostly 'w'
  and 'f' rats at Chebyshev 2-3 from it, many at 40 hp or less.
- In 2241 g2 every one of the 9 hits on our king was one of our own rats (6-50 hp, 1-2 tiles from the king)
  grabbed by a 10-58 hp enemy and thrown point-blank, 42 damage each: the king went 600 -> 348 in r112-r250.
- Baby.retreat() sends a rat at RETREAT_HP (40) or less that is near an enemy back to within Chebyshev 2 of our
  king. The engine has no healing, so the only effect is to put grabbable rats where a throw hits the king.
  RETREAT_HP was never tested. s8b = @s7d + RETREAT_HP 0; screen ra_s8b is queued after ra_s8a.
- CARRY_AIM v2 (s7h, screen running) addresses our own aim. BITE_CARRIER (bite first an enemy carrying one of
  ours) was rejected on the replica as part of s4i (net -2).
- battle_report.py's throws section now counts hitKing (enemy-king hits, a subset of hitEnemy); test_battle checks
  it on a synthetic throw.

## 2026-10-29 10:08 PDT: s7d's sets 2-3 done; bundle s8c on the replica
- s7d's control blocks for sets 2 (2115-2128) and 3 (2233-2246) are done and recorded in v3-ref-s7d.txt. All three
  sets against s6m's reference: 126 cells, 23 differ (gained 16, lost 7), net +9, t 1.88. progress/ refreshed:
  s7d rates about 1558 (52% expected field score).
- Screens since the last entry: ra_s7j (S6M + SWAP_HOLD 2) passes against ra_s6m, +7.4 pts, t +1.64, net +11
  (+18 -7). ra_s7h (CARRY_AIM v2) passes at 60/80 against ra_s6y, +5.7 pts, net +5. In the corridor diag sh2
  (half done) s7x (SWAP_HOLD + SWAP_YIELD + SWAP_NO_ALONG) beat araid on 7 of 8 rose/EscapeTheNight/Stash/minimaze
  games, s7l (without NO_ALONG) on 4 of 8.
- s8c bundles the parts that have passed, on @s7d:
  - s7i's symmetry switches (SYM_SLOT/FLOW/CENTRE/HAND/FACE) with KING_SENSE_RESERVE 7000;
  - s7x's corridor swap (SWAP_HOLD 2 + SWAP_YIELD + SWAP_NO_ALONG);
  - s7h's CARRY_AIM v2;
  - s7q's economy pair (SEEK_CROWD 3 + MINE_ANY_COOP; eco2 9 wins of 12 against s7d, +8.8 pts a game).
- s8c is replica submission #85. Its v3 set-1 block against s7d's reference started at 2247 (8 sent, 6 queued for
  the drain). Sanity diag sc1 (s8c vs s7d, 3 maps x 2 sides) is running: the first game had no exceptions and no
  overruns.
- Not in the bundle yet (screens or diags still running): BREAKOUT_TTL (s7z, brk1), KING_PRESS + PRESS_SHARED (s8a),
  RETREAT_HP 0 (s8b), SIEGE_FLOOR (s7t/s7u), MINE_EXPLORE (s7v/s7w, eco3).

## 2026-10-29 10:29 PDT: mine discovery against the field; eco3, sh2 and brk1 results
- minedisc.py (scratch) had the replay direction codes wrong: turns.tsv uses the schema codes (0 centre, 1 W ...
  8 NW, as in battle_report.py), not Java ordinals, so its cone test was off. Rerun on 56 replica replays
  (s6m/s7d era, 1915-2095): our rats first see the nearest home-half mine about when opponents do (median r17 vs
  r13), but the 2nd at r64 (theirs r29), the 3rd at r143 (r68) and the 4th at r335 (r90).
- eco3 (MINE_EXPLORE against s7d, 6 maps x 2 sides) closes part of that gap in self-play. s7w found the 4th and
  5th nearest home mines at r63/r48 against s7d's r110/r81, s7v (one exploring slot) at r87/r61. Results:
  s7v 5/12 wins, -0.3 pts a game, pickups x0.97; s7w 6/12, +1.2 pts, x1.03. Neutral against our own bot, which
  explores the same way. The gap it targets is against other bots, so s7w is held for a field bundle.
- sh2 (corridor maps rose, EscapeTheNight, Stash, minimaze; vs araid, 2 sides): s7x (SWAP_HOLD + SWAP_YIELD +
  SWAP_NO_ALONG) 7/8, s7y (+ SWAP_PAIR 30) 6/8, s7l (no NO_ALONG) 4/8. s7x's form is already in s8c.
- brk1 (s7z = BREAKOUT_TTL 150 vs s7d on the 9 dig maps x 2 sides): 8/18 wins, +5.7 pts a game. The mean
  rests on one 100-0 king kill on EscapeTheNight; 10 of 18 games lost, pickups x1.04. Mixed; screen ra_s7z
  decides.
- sc1 (s8c vs s7d, 3 maps x 2 sides) finished 5/6 with no exceptions or overruns. s8c's replica block is running.

## 2026-10-29 10:45 PDT: the trap war: our trap cap is full of the king's fort traps (s8e-s8h)
- s8c's set-1 block stands at 36 of 42 cells, D 7 (gained 2, lost 5), net -3: block 1 rejects at t <= 0. Two of
  the losses are Nofreecheese against CS50Crab. Both sides lose 84-94 rats to its traps there; s7d's king lived
  to r2000 and won on cat damage, s8c's starved (r1949, r1562) after CS50Crab took our mines late. Diag tp1
  (s8c, s7d, s7q, s7i vs atrap on Nofreecheese) is looking for which part does it.
- Engine (GameWorld.placeTrap/triggerTrap): a rat trap fires on an enemy rat entering any tile within dist2 2
  of it (its 3x3), deals 50, sets movement cooldown 30 and starts the backstab. A team sees only its own traps.
- Enemy rat-trap firings on our rats in s7d's 126 replica games, per game: CS50Crab 134, AlexT101 72, AdamTan12
  59, spsquared 53, abcdezyxwv 20, outercloud 9. Of the traps we fire, 43% were placed 0-1 rounds earlier and
  68% within 5; the placer stood next to it. With our nearest rat 2 away, the trap went on the placer's tile
  toward it 54% of the time and beside that tile 36%. The commonest firing (1532 of 5650) is our rat stepping
  from 2 to 1 of an enemy rat. 47% of firings were within 1 tile of one we fired in the previous 25 rounds.
- Our side, per game: babies place 17.5 rat traps and 16.9 fire (97%); kings place 16.1 and 12.1 fire; enemy
  babies place 49.7. In rounds with any trap of ours down, the live count sat at 10 in 84% of them: TRAP_CAP
  (10) is the team count, and the king's FORT_TRAPS (10, laid at war around it toward the centre) fill it, so
  babies stop laying traps until an enemy walks into the fort. On 2070 g0 we sat at 10 for 1928 of 2000 rounds
  while CS50Crab peaked at 24. battle_report's traps line now shows kings' placements and the live-count peak
  and rounds at it.
- TrapRisk (marks every visible enemy baby's three front tiles, avoids entering their 3x3) already exists, but
  RISK_IN_FIGHT and RISK_GATHER are off, so the mask is dropped exactly when an enemy rat is near. Both were
  dropped unrun with the S4D base.
- New, all @s7d: s8e = TRAP_CAP 25 (engine max), s8f = RISK_IN_FIGHT, s8g = RISK_GATHER, s8h = TRAP_CAP 25 +
  RISK_IN_FIGHT. Diag tr1 (each with s7d vs atrap, 6 high-trap maps x 2 sides); screens ra_s8e and ra_s8h are
  queued after ra_s8d.

## 2026-10-29 10:50 PDT: s8c rejected on the replica; s7h (CARRY_AIM v2 alone) submitted
- s8c's v3 set-1 block (2247-2260) against s7d's reference: 42 cells, D 8 (gained 3, lost 5), net -2, t -0.71.
  Rejected (block 1 rejects at t <= 0). progress/ refreshed.
- What did work: CARRY_AIM v2. Throws that hit an enemy robot rose from 0.18 a throw (s7d, set 1) to 0.28, and
  hits on the enemy king from 11 to 26 in as many games. Pickups per opponent by r300/r600 match s7d's, so the
  economy pair did not visibly cost income. Against AlexT101 and abcdezyxwv both versions pick about half of
  what the opponent picks by r300.
- The five lost cells: Nofreecheese A+B vs CS50Crab (king starved late), AlexT101 popthecork A (a cat scratched
  our king to death) and uneasy_alliance A (throw hits on the king), outercloud heart B (both kings starving;
  s7d won the same race at r347-360). At t -0.71 this reads as noise as much as harm.
- s7h (s7d + CARRY_AIM v2, screen +4.4 pts, net +3) is replica submission #86. Its set-1 block starts at 2261.
  The trap candidates (s8e/s8h) wait on diag tr1.

## 2026-10-29 11:05 PDT: atrap is too weak to test trap play; atrap2 and diag tr2
- tp1 (s8c, s7d, s7q, s7i vs atrap on Nofreecheese, 2 sides x 2 seeds): all 16 games won by king kill, 74-77
  pts a game, so it cannot say which part of s8c starved on Nofreecheese against CS50Crab. tr1 (the trap
  variants vs atrap) was stopped after 5 of 60 games for the same reason.
- atrap2 = s7d + TRAP_CAP 25 + RAT_TRAP_MIN_CHEESE 40 + BABY_TRAP_DIST2 20: a trap layer as strong as the
  incumbent otherwise. tr2 on Nofreecheese, constrictinglattice, wallsofparadis and evileye (2 sides): s8e vs
  s7d, and s7d, s8f (RISK_IN_FIGHT), s8h (TRAP_CAP 25 + RISK_IN_FIGHT) each vs atrap2.

## 2026-10-29 11:23 PDT: where s7d loses on the field: early king kills by the three top bots
- s7d's 42 replica matches (126 games) by opponent: bc26vibe_bot 16-2, CS50Crab 13-5, outercloud 13-5,
  spsquared 9-9, AdamTan12 6-12, AlexT101 3-15, abcdezyxwv 2-16. The panel weighs each opponent equally, so the
  room is in the last three (cell 29.9, 17.9 and 11.1 of 100).
- Those losses are king kills, and early ones: AlexT101 14 of 15 at mean r259, abcdezyxwv 16 of 16 at r340,
  AdamTan12 11 of 12 at r872. The opponent backstabs first in 95 of 126 games (mean r81; r38 for AlexT101, r32
  for abcdezyxwv); we never do. Rats at r100 are even (16-18 each), but by r250 we hold 11 to their 18, and
  abcdezyxwv runs 42 at r500 to our 16.
- On small maps the rush decides by r120: tiny (r49-218, 4 losses), knifefight (r67, r73), averyfineline (r90,
  r112), DefaultSmall (r117). On tiny vs AlexT101 (2239 g2) we won the rat trade (19 kills to 12, kd 1.58), but
  their 25 rats to our 20 went past ours and bit the king 45 times; it died at r49 with no starvation.
- A raised baby cap was already tried (s5r, rejected on the replica: it spent the bank and both kings starved),
  so the next lever is how the king is defended, not how many rats we hold. kingperim (our king's open
  16-tile bite ring vs the damage it takes) and per-opponent combat and king-HP aggregates are running.

## 2026-10-29 11:29 PDT: s7h rejected on the replica; we win the rat war but never touch their king
- s7h's v3 set-1 block (2261-2274) against s7d's reference: 42 cells, 38 alike, D 4 (gained CS50Crab
  dirtfulcat B and streetsofnewyork A, lost AlexT101 uneasy_alliance A and CS50Crab Nofreecheese A), net 0,
  t 0.00. Rejected (block 1 rejects at t <= 0): CARRY_AIM v2 is neutral on the field. ra_s7q passed its screen
  (-0.2 pts, net +1).
- First kings over s7d's 126 field games (king_report aggregated): against AlexT101 our king lost 166 HP a game
  to bites and 206 to throws, theirs 6 and 2; abcdezyxwv 223 + 192 against 7 + 11; AdamTan12 299 + 13 against
  1 + 8. Their kings lose HP mainly to cats. Our rats come within dist2 20 of AlexT101's king at r20 (theirs
  reach ours at r76), so reaching it is not the problem; biting it is.
- Combat to r250 (battle_report aggregated): rat losses are even against all three (AlexT101 32.7 to 32.5,
  abcdezyxwv 29.2 to 31.4, AdamTan12 20.7 to 21.0), but thrown rats hit a king 3.5 times a game for AlexT101
  and 3.7 for abcdezyxwv against our 0.1 and 0.3, and they hold 18 rats at r250 to our 11 after equal losses.
- kingperim (our king's open 16-tile bite ring, mean over its turns from r100): 13-16 open, 76 games, lost 50%,
  bite + throw 140 + 92 HP a game; 9-12, 27 games, 52%, 112 + 108; 8 or fewer, 15 games, 40%, 87 + 70. Weak and
  confounded by map, so not a reason to rebuild king placement.
- Next on the replica: s8a (s7d + KING_PRESS + PRESS_SHARED, wider reach): at war healthy rats go to a fresh
  enemy-king sighting and bite it. WAR_STRIKE 1 (every fighter strikes) lost on the replica as s5g; the press
  is reactive and has never met the field. Sanity games kp1 (vs araid on tiny and knifefight) first; its
  screen ra_s8a is moved to the front of the queue.

## 2026-10-29 11:34 PDT: we never leave our half; raiders s8i and s8j
- Posture over s7d's 126 field games (territory.py: share of each team's babies nearer the other king's start
  than their own, sampled every 10 rounds while both first kings live). In r100-300 our share on the enemy half
  is 1-5% (0.1-1 rat) against every opponent. Theirs on ours: AlexT101 23-30% (about 4-5 rats), abcdezyxwv
  25-32% (14.8 rats by r600), AdamTan12 24-31%, bc26vibe_bot 19-25%, CS50Crab 11-22%, outercloud 9-16%.
- That one fact fits the rest: their own mines run untouched while they take from ours (about 2x our pickups
  by r300), the fighting is always at our king, and their king is never bitten. It is the posture gap, not the
  rat trade (even) or the king's perimeter (weak), that the top three exploit.
- The raider role exists in the code (one in RAID_EVERY cat fighters goes to an enemy-half mine, a known one
  or the mirror of ours, picks its piles, and with RAID_TRAPS lays traps on its sites) but is off. Its s6i/s6k
  recipes were dropped unrun when S4D was superseded; raiders were last measured on s2i, a much weaker base.
- New, both @s7d: s8i = RAID_EVERY 2 + RAID_TRAPS 8 (a quarter of later babies raid), s8j = RAID_EVERY 1 +
  RAID_TRAPS 8 (half). RAID_TRAPS also lifts the combat trap cap to 20. Screens ra_s8i and ra_s8j are queued
  after ra_s8a; diag rd1 (each vs s7d, DefaultMedium, sunandmoon, DefaultLarge, constrictinglattice, 2 sides)
  checks that the posture actually moves.

## 2026-10-29 12:22 PDT: s8a (KING_PRESS) rejected; s8j (raiders) on the replica; replica blocks are fast
- s8a's v3 set-1 block against s7d's reference: 42 cells, 37 alike, D 5 (gained 2, lost 3), net -1, t -0.45.
  Rejected. It lost popthecork both sides to AlexT101 (an s7d win) and spsquared fullofsecrets B. Its two gains
  are the CS50Crab cells s7h also gained (dirtfulcat B, streetsofnewyork A), so the reference was probably
  unlucky there rather than either change helping.
- rd1 (s8i, s8j each vs s7d, 4 maps x 2 sides): every game stayed at peace. Raiders move the posture a little:
  s8j keeps 8-18% of its rats (1.6-3.6) on the enemy half in r100-300 to s7d's 5-10%; s8i 6-14%. Results
  neutral: s8j 5/8, +2.2 pts, pick x1.02; s8i 3/8, +0.2. The war effect needs araid or the field.
- tr2 was inert: atrap2 never breaks the truce, so all 32 games ended on points and the trap variants played
  s7d's games. A trap test needs a war-opening opponent; atrap2 is recorded as such. ra_s7z passed its screen
  (+4.7 pts, t +1.11, net 0).
- A replica block runs in under an hour (s8a's: 11:31-12:10), while an 80-game screen on the VM (load about 22)
  takes several hours. From now on candidates whose case rests on war play go to the replica directly and
  the screens run behind as a backstop. s8j is submission #88 (block from match 2310); queued after it: s7q
  (economy), s8e (trap cap 25), s8b (no retreat), s7z (breakout).

## 2026-10-29 12:23 PDT: on the small maps we buy traps instead of rats; s7u next
- AdamTan12 takes all six set-1 games from every build so far (DefaultSmall, tiny, jail). In 2308 our rats
  never came within dist2 20 of its king on DefaultSmall or jail. DefaultSmall: kills were even (36 to 37), but
  from r68 to r84 we lost 12 rats and they 8, and they held 16 by spawning while we fell from 20 to 8. Our king
  died at r117 to 60 bites.
- The ledger to the king's death: tiny (r92) traps 990 cheese for us against 440, spawns 35 against 53;
  DefaultSmall (r117) traps 1010 against 940, spawns 42 against 48. Both banks ended at 100-130.
- The priority is inverted: a baby lays a rat trap down to the RAT_TRAP_MIN_CHEESE floor (200), while the king
  stops spawning at war below WAR_FLOOR (300) plus the rat cost, so in a squeeze traps drain the 200-300 band
  and spawns stop. s7u (s7d + SIEGE_FLOOR 20 + TRAP_SPAWN_FIRST: spawn floors drop to 20 with enemies in the
  king's sight, and no baby or king rat trap while the king lacks its next spawn) was written for exactly this
  case (2094 DefaultSmall) and has never met the field. It goes to the replica after s8j.

## 2026-10-29 12:27 PDT: the r300 economy against the field: three leaks
- econ_report aggregated to r300 over s7d's 126 field games (per game). Against AlexT101 we pick 1265 to their
  2389, abcdezyxwv 1047 to 1875, AdamTan12 1050 to 1840; against the other four we are level or ahead.
- Leak 1, our own mines: we work 2.9 of 3.9 home mines against AlexT101, 2.1 of 2.8 against abcdezyxwv, 2.2
  of 4.0 against AdamTan12 (they work 3.2-3.9 of theirs), and 826, 386 and 1002 cheese lie unpicked at our
  home mines by r300. That is slow mine discovery (the field sees its 2nd/3rd home mines at r68/r90, we at
  r143/r335): s7w (MINE_EXPLORE 2) is the candidate.
- Leak 2, raiding: they pick 486, 386 and 433 on our half by r300; we pick 27-33 on theirs. s8j (raiders) is
  on the replica now.
- Leak 3, traps: we spend 940, 814 and 726 on traps by r300 against their 789, 155 and 511, while they spend
  more on spawns (AlexT101 52 spawns to our 43, abcdezyxwv 51 to 40). s7u gates traps behind the next spawn.
- Replica order: s8j (running, from 2317), s7u, s7w, then s7q, s8e, s8b.

## 2026-10-29 13:01 PDT: s8j (raiders) passes replica block 1, but not by raiding
- s8j (s7d + RAID_EVERY 1 + RAID_TRAPS 8) on v3 set 1 (2317-2322, 2326-2329, 2343-2344, 2346-2347) against
  s7d's reference: 42 cells, 34 alike, D 8 (+5 -3), net +2, t +0.71. Block 1 rejects only at t <= 0, so set 2
  is queued (from 2350).
- The AlexT101 gain (5 of 6 against s7d's 3 of 6 on the same set) is not the raiders. Their first king died
  in 5 games, 4 of them to cat scratches (490 HP a game against 398 for s7d); our bites on it stay at 15 HP a
  game in both. Our share of rats on their half barely moved: 3-14% in r100-300 against s7d's 1-5% over
  all games.
- Field posture is unchanged where it matters. Against abcdezyxwv we hold 7-9% on their half from r100 to
  r300 while they hold 26-28% on ours, and against AdamTan12 our king still dies in all 6 games (mean r250,
  565 HP a game to bites). Raiders hold their half against a passive s7d (rd1, 8-18%) but not against the top
  bots.

## 2026-10-29 13:10 PDT: against the top three we are broke for half the war; s8k
- Over s7d's 126 field games (v3 sets 1-3), the share of war rounds (after the backstab, our first king alive)
  in which our bank is under 350 with fewer than 18 babies, so the king cannot spawn at WAR_FLOOR, is 62%
  against AlexT101, 49% against abcdezyxwv and 32% against AdamTan12; theirs is 5-7%. Mean babies at war
  12.9 to 20.6 (AlexT101) and 13.8 to 23.4 (abcdezyxwv); their banks average 3675 and 1497 to our 513 and 626.
  Against the other four we are stranded 0-25% of the time.
- How it happens on the small maps (s8j vs AdamTan12, 2346; income there is negligible for both): on
  DefaultSmall the war opens at r15 (their trap). By r40 we have laid 65 rat traps (1300 cheese) and fall to
  295 cheese against their 1060. Our traps kill 22 of their rats before r49 and they kill 16 of ours, but from
  r49 they spawn 15 to our 2. Our king, at 185 cheese with 13 babies, is under WAR_FLOOR and its spawn tiles
  are taken. tiny: 47 traps by r40, bank 195 against 1065, kills even at 29-29, spawns 35 against 45.
- Jail (same match, r778) is the slow version: from r150 to r740 our bank sits at 175-250 with 8-15 babies
  and no spawns, while babies keep laying traps (51 to 67), until a fight at r740 wipes us. Their bank climbs
  past 1000 with 16 babies.
- In those king fights AdamTan12 keeps our defenders carried: 21 grabs within 5 of our king in r49-94 on
  DefaultSmall, while their carriers keep biting (a carrier can still act) and our babies bit 10 times to
  their 60 on the king.
- s7u (SIEGE_FLOOR 20 + TRAP_SPAWN_FIRST: no trap while it would cost the next spawn, and spawn floors drop
  to 20 with enemies in the king's sight) is aimed at exactly this and goes to the replica after s8j. s8k is s7u
  plus RAT_TRAP_MIN_CHEESE 600, which keeps a 600 reserve that only spawns can spend. Local check tf1 (s7d, s7u,
  s8k vs araid on DefaultSmall and tiny, both sides) is on the VM.

## 2026-10-29 13:25 PDT: who picks up our home mines' cheese
- Pickups to r400 per game over s7d's 126 field games, at each home-half mine ranked by distance from our king
  (h1 nearest, h4 = 4th and beyond), us/them. AlexT101: h1 580/69, h2 241/273, h3 295/60, h4 344/338.
  abcdezyxwv: h1 410/131, h2 339/175, h3 143/252, h4 49/125. AdamTan12: h1 519/100, h2 352/198, h3 105/123,
  h4 260/128. So they take 549-740 a game off our half, while we take 0-31 at each of their mines. Their
  own-half totals are 1634-2154 to our 941-1460. Against the other four we pick 1600-2500 at home.
- Jail (2346, s8j vs AdamTan12, r150-500) shows the mechanism. Of our two home mines, (8,36) gets every
  gatherer: 1268 waiting turns and 1080 walking turns within 4 of it. (52,35), 26 tiles from our king, is never
  worked (our rats were within 4 of it 0.1 per round), and their occasional visitors pick 540 there to our 300.
  Mines fire in mirror pairs, so its twin (52,4) gave them 615 more. Our income was about 3 a round against
  their 6, and the king logged 'no surplus' or 'under the floor' on 89% of its turns.
- Grab legality over the same games: their grabs on our rats are legal because our rat is weaker (34-53%
  weaker only, 30-37% both). Only 15-27% need us to be facing away, so GLANCE-style facing is not the lever.
  Our victims average 50-56 HP, their grabbers 82-85.
- So two levers stand out: finding and working every home mine (s7w, MINE_EXPLORE 2, held for a field bundle)
  and keeping a spawn reserve (s8k). s8k goes to the replica after s8j, then s7w. MINE_MIRROR (off in s7d)
  would let s8j's raiders list the home twin of each enemy mine they see.
- s8i's screen: ra_s8i vs ra_s6y, 80 cells, D 58, -6.2 pts, t -1.11, net -2: a weak pass, parked behind s8j.

## 2026-10-29 13:39 PDT: s8j rejected after block 2; s8k on the replica
- s8j (raiders) on v3 sets 1+2 against s7d's reference: 84 cells, 71 alike, D 13 (+7 -6), net +1, t +0.28.
  Block 2 rejects at t <= 1. Block 2 on its own netted -1: it gained CS50Crab wallsofparadis B and spsquared
  thunderdome B, and lost AdamTan12 attack_on_cat (both sides) and abcdezyxwv ultimatestarvation B. Block 1's
  AlexT101 gain was cat damage to their king, not raiding (13:01).
- Raiding as built does not shift posture against the top three. s8m (s8j + MINE_MIRROR) is parked unless the
  mm1 check shows the mirrored mine list feeding our home economy.
- s8k (s7u + RAT_TRAP_MIN_CHEESE 600) is submission #89, set-1 block from 2384. Behind it: s8l (s8k +
  MINE_EXPLORE 2), then s7w alone if s8l's block points that way.

## 2026-10-29 14:30 PDT: s8k rejected after block 1; s7w on the replica
- s8k (s7u + RAT_TRAP_MIN_CHEESE 600) on v3 set 1 against s7d's reference: 42 cells, 31 alike, D 11 (+4 -7),
  net -3, t -0.90. Block 1 rejects at t <= 0. It gained CS50Crab dirtfulcat B and streetsofnewyork A,
  abcdezyxwv sunandmoon B and spsquared bed B. It lost all three AlexT101 cells s7d had won (popthecork A/B,
  uneasy_alliance A; 0/6, every game a king kill, backstabs by r4-r47), plus CS50Crab Nofreecheese A and
  dirtfulcat A, outercloudstudio heart A and spsquared fullofsecrets B. AdamTan12 was 0/6 for both.
- So a reserve for the next spawn, kept by holding war traps back, does not change the result against the top
  three. AlexT101 swings a lot between blocks (s8j 5/6, s7d 3/6, s8k 0/6), so one block says little about it.
- s7w (s7d + MINE_EXPLORE 2, working every home mine) is submission #90, set-1 block from 2439. s8l was
  s8k + MINE_EXPLORE 2, so it waits on s7w's result.

## 2026-10-29 14:57 PDT: our rats are thrown into our king; RESCUE (s8n)
- King damage by source over s7d's 126 field games (kinghits): AlexT101 does 241 a game by throwing our own
  rats into our king and 191 by biting it. For abcdezyxwv it's 206 and 278. AdamTan12 almost only bites (345).
  Throw hits on our king (throwking): AlexT101 6.6 a game at 36 each, abcdezyxwv 5.1 at 41, outercloudstudio
  2.9 at 38, everyone else under 1. Grab to hit takes about 3 rounds. 29-36% of these victims had been thrown
  before (juggled: grab, throw, re-grab 2 rounds later, the soonest the engine allows). First-time victims were
  2-3 from our king's centre, averaging 58-68 hp, and 40% of abcdezyxwv's were at full health (grabbed from behind).
- Our rats next to an enemy at our besieged king (kingidle, carried enemies excluded): 65-73% of those turns
  our rat is itself being carried, against 38-47% for theirs. We bite on 5-10% of them, they bite on 14-29%.
- The counter in the rules: any rat may grab an enemy that cannot see it, whatever its health, and a grabbed
  carrier drops the rat it holds (InternalRobot.getGrabbed). A carrier faces its throw, so 5 of the 8 tiles
  around it are blind. In s7d's games enemy carriers held one of ours for 137-244 rounds a game. A free rat of
  ours stood within Chebyshev 2 of the carrier on 55-64% of those rounds, but on its blind side only 5-9%; we
  grabbed 5-20 such carriers a game, mostly by accident. The nearby rats were busy: f/w codes with the action
  spent (on a bite) or the move spent.
- RESCUE (off-by-default switch): an enemy baby carrying one of ours within dist2 8 comes first. No bite or
  trap before moving (the action stays free), step to a blind tile next to it while turning to face it
  (flank's move), and the post-move grab prefers carriers holding ours. Move code R; BotTest testRescue.
  s8n = s7d + RESCUE. Screen ra_s8n and rs1 (s8n vs s7d, 8 maps x 2 sides) are on the VM.
- Screens: ra_s8k +7.2 pts, t +1.54, net +5; ra_s8l +1.7, t +0.37, net +6. Both pass, but s8k lost its
  replica block and s8l is parked behind it.

## 2026-10-29 15:12 PDT: s7w rejected after block 1; s7q on the replica
- s7w (s7d + MINE_EXPLORE 2) on v3 set 1 against s7d's reference: 42 cells, 35 alike, D 7 (+3 -4), net -1,
  t -0.38. Gained CS50Crab dirtfulcat B, abcdezyxwv sunandmoon B, outercloudstudio seepastyourgreed B. Lost
  AlexT101 popthecork B, CS50Crab Nofreecheese B and dirtfulcat A, outercloudstudio heart A. Exploring for
  every home mine moved nothing on the field.
- s7q (s7d + SEEK_CROWD 3 + MINE_ANY_COOP; eco2 vs s7d 9/12, pick x1.20) is submission #91, set-1 block
  from 2491. s8n (RESCUE) waits on its local runs: rs1 vs s7d was all cooperation, since our bot never
  backstabs, so it is being rerun vs abrk on gk1's 8 king-kill maps.

## 2026-10-29 15:48 PDT: s7q passes block 1; RESCUE results so far
- s7q (s7d + SEEK_CROWD 3 + MINE_ANY_COOP) on v3 set 1 against s7d's reference: 42 cells, 32 alike, D 10
  (+6 -4), net +2, t +0.63. It gained CS50Crab dirtfulcat B and streetsofnewyork A/B, AlexT101 heart B,
  abcdezyxwv sunandmoon B and spsquared bed B. It lost AlexT101 popthecork B and uneasy_alliance A, CS50Crab
  Nofreecheese B and outercloudstudio heart A. Block 2 (v3 set 2) is queued from 2522 and rejects at t <= 1
  over both blocks.
- RESCUE locally: s8n (one step) screens +3.5 pts, t +0.91, net +5; vs abrk on gk1's 8 maps x 2 sides 11/16
  (s7d 13/16). s8o (two steps, RESCUE_WALK) vs abrk 14/16, no exceptions or overruns; its screen is running.
  Our grabs of enemy carriers rise from 13.8 to 19-21 a game against abrk.
- Our own holds (s7d's field games): 38-52% thrown after 2 rounds, 23-51% held the full THROW_HOLD 6, and the
  field takes back 13-26% of our grabs about 1.6 rounds in. s8p = s8o + THROW_HOLD 3 is queued (screen + rs4).

## 2026-10-29 16:39 PDT: s7q rejected after block 2; s8o (RESCUE) on the replica
- s7q's block 2 (v3 set 2) on its own: 42 cells, 38 alike, D 4 (+2 -2), net 0. It gained spsquared
  thunderdome A and lost spsquared evileye A. Over both blocks: 84 cells, D 14 (+8 -6), net +2, t +0.53.
  Block 2 rejects at t <= 1. Its local economy gain (eco2 pick x1.20) doesn't carry over to the field.
- s8o (s7d + RESCUE + RESCUE_D2 18 + RESCUE_WALK) is submission #92, set-1 block from 2588. Its screen was
  +6.0 pts, t +1.42, net +5 (+2.4 over s8n), and it went 14/16 vs abrk.
- Local RESCUE variants vs abrk on gk1's maps, 16 games each: s7d 13, s8n 11, s8o 14, s8p (THROW_HOLD 3) 11,
  s8q (RESCUE_BLOCK) 13. All in the noise. Screens ra_s8p and ra_s8q are on the VM.
- A background waiter (ra_s8p's scorer) was stopped by Claude Code for low local memory (2 GB machine). The
  screen itself runs on the VM; I'll score it in one foreground call once it's done.

## 2026-10-29 16:52 PDT: s8p and s8q screens; mine discovery is not the lever; s8r and s8s
- ra_s8p (s8o + THROW_HOLD 3): vs ra_s6y +3.8 pts, t +1.01, net +4; vs ra_s8o -2.2, t -0.61, net -1. Parked.
- ra_s8q (s8o + RESCUE_BLOCK): vs ra_s6y -6.2 pts, t -1.51, net -5; vs ra_s8o -12.2, t -2.37, net -10 (sign
  p 0.04). Rejected: our rat steps into the tile the carrier would throw through, and gets grabbed or thrown.
- Field mine discovery (minedisc.py) for s7w's replica block (MINE_EXPLORE 2, 2439-2480) against s7d's on the
  same set 1 (median first sighting of each home-half mine, by distance rank; field = the opponent's own):

  | Rank | s7d | s7w | field | seen by r200, s7d / s7w / field |
  |---|---|---|---|---|
  | 2nd | r47 | r50 | r32-35 | 30 / 30 / 33-35 of 38 |
  | 3rd | r154 | r117 | r91-145 | 16 / 20 / 19-22 of 30 |
  | 4th | r579 | r183 | r116-237 | 5 / 10 / 8-9 of 16 |

  s7w found our 3rd and 4th home mines at about the field's pace and still went net -1 (t -0.38) on the replica.
  So slow discovery is not why the top three out-pick us 1.9x by r300.
- s7d's field losses: 55 of 64 are king kills (AlexT101 3-15, abcdezyxwv 2-16, AdamTan12 6-12). The economy
  split was never tuned on a modern base: half of the babies after the 6th are cat fighters. New (both @s7d):
  s8r = FIGHT_EVERY 3, s8s = GATHER_FIRST 10. Screens ra_s8r and ra_s8s are queued on the VM. s8o's replica
  block 1 is running (from 2588).

## 2026-10-29 17:23 PDT: our gatherers lose home mines to a moving reference point; HOME_START (s8t)
- What our babies do over s7d's 126 field games (alloc.py, sampled every 5 rounds while both first kings live):
  at r100-300 the opponents have 5-7 babies holding cheese to our 2-4.7, against every opponent. Ours stand
  empty-handed within 4 of a mine instead: 7-11 to their 2-6.
- By mine side to r300 (econ_report --team, per game). Against AlexT101 we spend 2672 rat-rounds near our own mines
  and pick 1091 there; 826 lies unpicked and they take 486. They spend 1707 near theirs and pick 1646. Against
  abcdezyxwv: 1798 for 708, 386 left, 386 taken. So per rat-round near a mine they pick 2.3x what we do; we crowd
  the mines near the king. popthecork (2072): about 14 rats at the king's mine, and 3 of our 5 home mines never
  seen; they are inside a walled enclosure whose neck is plugged with dirt (s7z's breakout is aimed at that).
- uneasy_alliance (ua1, s7d vs araid): the king lists 6 mines from r50, but gatherers target only the three by the
  king. The symmetry mask stays 7 all game. homeHalf mirrors the king's tile at the baby's birth, and the king
  walked from its start (11,15) to (8,21). From there the rotational half puts (8,8), (8,2) on the enemy half, so
  they get 106 and 1 rat-rounds to r300; araid takes 700 on our half.
- New switches (off by default): HOME_START (the king squeaks its start in a turn it spawns with no cat in sight,
  since cats turn toward squeaks; newborns take it as the reference for homeHalf, mine trap sites and raid/rush
  targets) and SYM_KING_GAP (round 1 rules out any symmetry whose twin of our start overlaps our 3x3).
  s8t = @s7d + both. ua3 vs araid: 3/4, the same as s7d, winning both uneasy_alliance games (s7d lost side A).
  uneasy_alliance A to r300: mask 5, (8,8) gets 422 rat-rounds (106), own-half pickups 2660 (2420), araid
  takes 180 on our half (700). Screen ra_s8t is queued.
- MapCensus gained featDiff (tiles whose wall, dirt or mine differs from their twin, per wrong symmetry) and
  gapAlt (the wrong symmetries the king gap rules out). The fewest distinguishing tiles: uneasy_alliance 4,
  averyfineline 4, Stash 12, UncomfortablyClose 16, externalsustenance 28, Excavation 32, evileye 32. The gap
  rule rules out one wrong symmetry on 8 maps (uneasy_alliance, UncomfortablyClose, thunderdome, houseofcards,
  streetsofnewyork: H; Stash, FreeTheCheese, corridorofdoomanddespair: V). With the mask unresolved the bot
  defaults to rotational, which is right on rotational maps.

## 2026-10-29 17:24 PDT: s8o (RESCUE) rejected after block 1; s8t on the replica
- s8o's v3 set-1 block (2588-2593, 2606-2607, 2611-2612, 2722-2725) against s7d's reference: 42 cells, 35 alike,
  D 7 (+3 -4), net -1, t -0.38. Rejected. Gained AdamTan12 tiny A, AlexT101 uneasy_alliance B, CS50Crab dirtfulcat
  B; lost AlexT101 uneasy_alliance A, CS50Crab Nofreecheese A, abcdezyxwv sunandmoon A, spsquared fullofsecrets B.
  The rescue lifted our grabs of enemy carriers locally (13.8 -> 19-21 a game vs abrk), but it does not show on
  the field.
- s8t (HOME_START + SYM_KING_GAP) is submission #93, set-1 block from 2726. It goes ahead of its screen because
  it is a reference-point fix with a measured mechanism (ua3), and a replica block is faster than a VM screen.
  Next in line: s8e (TRAP_CAP 25, screen +2.0, net +3).

## 2026-10-29 17:40 PDT: popthecork's bottle; spending on small maps; s8u (we open the war)
- ua2 (s7z, BREAKOUT_TTL, vs araid on popthecork): A won at r927 (s7d also won A, at r930), B lost on points
  (s7d won B on points). The breakout does not get us into the bottle. popthecork's middle is one walled bottle
  holding 8 of the 10 mines, 4 per side, and its only way in is a dirt-plugged neck at the top centre. As A our rats
  never went below y 11 and never entered it (one neck tile dug). The king's shared list held 1 mine (the outside
  one at our start) for 900 rounds, and 7700 rat-rounds sat there. A rat came within vision radius of (7,9) at r35,
  but no report reached the king. As B we dug the neck late and reached (17,17) only at r1863. araid dug in and
  took 4380 at its outside mine plus 6200 inside.
- uneasy_alliance (2073 g2, s7d as B vs AlexT101, lost at r894): they picked 18722 to our 7397 and spawned 197
  rats to our 100. 5920 of their pickups came from three of our six home mines, (21,2), (21,8) and (27,7), which we
  never worked (0-52 rat-rounds against their 826-2515). This is the case HOME_START addresses (s8t, on the
  replica now).
- knifefight (2074 g1, vs abcdezyxwv, our king dead at r67): their bite at r3 opened the war. We placed 62 rat
  traps for 1240 cheese, and 60 of them fired. They died 37 to our 25, and their king fell to 346 by r40. But we
  had spent 1240 on traps against 1120 on 32 spawns, and they spent 1500 on 48 spawns and nothing on traps. Our
  bank was 185 at r50 against their 1008. From r50 to r66 they brought 12 rats to our king, our 12 defenders fell
  to 6, and it took 31 bites and 7 throw hits (310 + 290 HP).
- Local king indicators over cached dumps: the mask is still unresolved at r100 on fullofsecrets (7, 1 mine listed),
  jail (3 or 7), popthecork (5, 1-2 mines), uneasy_alliance (7), ultimatestarvation (7) and houseofcards (5). Field
  replays strip indicator strings, so field games can't be checked this way.
- New big swing: s8u = @s7d + RUSH + RUSH_BREAK + BREAK_ROUND 36. Spawn indices 4-15 head for the enemy king from
  birth and bite or grab during cooperation, and every baby fights from r36. The top three open the war in 95% of
  games and take 38 of 48 cells from s7d, and we have never opened it ourselves. Screen ra_s8u and diag rsu1 (s8u vs
  s7d, 6 maps x 2 sides) are on the VM.

## 2026-10-29 17:55 PDT: s8t rejected after block 1; s8u beats s7d 8/12 and goes to the replica
- s8t's v3 set-1 block (2726-2739) against s7d's reference: 42 cells, 38 alike, D 4 (+1 -3), net -2, t -1.00.
  Rejected. It lost AlexT101 popthecork B, AlexT101 uneasy_alliance A and CS50Crab Nofreecheese A, and gained
  CS50Crab dirtfulcat B. s7d's uneasy_alliance A win was luck: 4 of our rats to their 25 at r280, and their king
  died to 29 cat scratches at r316. s8t's game there went the same way, without the cat.
- Screens: ra_s8r (FIGHT_EVERY 3) -2.3 pts, t -0.46, net +5; ra_s8s (GATHER_FIRST 10) +2.7 pts, t +0.56, net +4.
  Both pass (the screen only rejects) and are replica candidates.
- rsu1, s8u (s7d + RUSH + RUSH_BREAK + BREAK_ROUND 36) against s7d head-to-head, 6 maps x 2 sides: s8u wins 8 of
  12. It takes both tiny (r60, r65), both knifefight (r48, r46), both DefaultLarge (on points), uneasy_alliance A
  (r89) and heart B. It loses both DefaultSmall, uneasy_alliance B and heart A. 0 exceptions, 0 overruns. No
  candidate since s7d has beaten it head-to-head this clearly, so s8u is submission #94, set-1 block from 2740,
  ahead of its screen (ra_s8u, 28/80).

## 2026-10-29 18:48 PDT: the rush line fails on the field (s8u rejected); s8w becomes the local rush proxy; s8x
- s8u's v3 set-1 block (2740-2753) against s7d's reference: 42 cells, 34 alike, D 8 (+3 -5), net -2, t -0.71.
  Rejected. It gained CS50Crab dirtfulcat B, CS50Crab streetsofnewyork B and outercloud seepastyourgreed B. It lost
  AlexT101 popthecork A and B, abcdezyxwv sunandmoon A, bc26vibe wallsofparadis A and outercloud heart A. Nothing
  moved against the top three.
  - popthecork vs AlexT101 (2746 g1): the rush has to go round the bottle. Their king kept 600 HP all game, and
    our 20 rats were all dead by r399.
  - heart vs AlexT101 (2746 g0): our bank fell 1535 -> 500 by r50 for one bite on their king. Our king died at r177
    to cat scratches (400 HP) and starvation (200).
- The local wins (rsu1 8/12, rsu2 10/20, rsv1 9/12 against s7d) came from s7d's weak defence against a rush.
  The field's kings are well defended. rsu2: s8u won both averyfineline and krotus, lost both Nofreecheese and
  superioristhmus, and jail, popthecork, cheesefarm and wallsofparadis went to side B both times.
- s8v (s8u + RUSH_UNTIL 150; new switch, off by default) fixed s8u's long-war DefaultSmall losses (9/12 against
  s7d). s8w adds KING_SENSE_RESERVE 7000: the dev code since s7d had pushed the king's first turn on 60x60 maps
  to 20000-20005 (s7d 19696), which cost the round-2 spawn. ovw1 shows 0 overruns. New snapvar presets: @kfix,
  and @s8w (the rush line).
- s8w is a much stronger local stand-in for the field's rushers than araid. s7d beats araid, but it lost 8/12
  to s8u. It kills s7d's king on tiny (r60-65), knifefight (r46-48) and uneasy_alliance (r89), the pattern the top
  three show on the field. Defence baseline dr0 (s7d vs s8w on 12 close-king maps, both sides) is running. Defence
  candidates will be measured against it.
- s8x = @s7d @kfix + GATHER_FIRST 10 + FIGHT_EVERY 3 + TRAP_CAP 25, a bundle of the three weak screen passes
  (s8s +2.7 net +4, s8r -2.3 net +5, s8e +2.0 net +3). It is submission #95, set-1 block from 2754; screen ra_s8x is
  queued.

## 2026-10-29 19:18 PDT: thrown rats; AMMO_AWAY; we harvest half our own side
- dr0 (s7d vs s8w, 12 close-king maps x 2 sides): s7d 13/24, 9 losses by king kill. On tiny (rsu1) s8u killed s7d's
  king mostly by grabbing OUR rats near it and throwing them point-blank (11 hits x about 42). The field's kills look
  the same: the thrown rats were mostly war/face rats at Chebyshev 2-3 from our king, many at <= 40 HP. The engine
  lets a rat grab any enemy that can't see it, or one with less health. RETREAT_HP 40 walks low-HP rats to the king,
  which is where they become ammunition.
- New switch AMMO_AWAY (off; tag code 'A'): at war a baby with <= AMMO_HP 50 health, within Chebyshev 3 of our
  king, whose nearest enemy rat is within dist2 8 and healthier, steps away from the king instead (Nav.flee, so
  mirror twins step alike). s8y = @s7d @kfix + AMMO_AWAY, s8z = s8y + RETREAT_HP 0. dr2 runs both against s8w on
  dr0's cells.
- dr1 so far: s8o (rescue) 16/24 against s8w (7 king kills) against s7d's 13/24. It was already rejected on the
  replica (net -1), so local defence wins against s8w don't carry over yet.
- s8x's block, 30 of 42 cells in: net -4, t -1.63. It lost both heart games to outercloud. On heart it spent
  1400-1570 on traps and spawned 23-32 rats by r300 (s7d: 780-800 and 42-45). The king starved. TRAP_CAP 25 trades
  rats for traps, and the field punishes that.
- Trap spend is not what separates us from the top two; picking is. Over s7d's 31 parsed set-1 games, r1-300:
  abcdezyxwv picks 1448 more than we do and spawns 61 rats to our 42 (traps: us 1127, them 153). AlexT101 picks 1681
  more and spawns 64 to our 47.
  - uneasy_alliance (2073 g2, us B): each half's mines spawned 4220 by r300. AlexT101 picked 95% of its half
    plus 1380 of ours; we picked 51% of ours. Three of our six mines were barely visited: (21,8), 7 tiles from our
    king, they picked from r20; (21,2) from r49; (27,7) mostly lay unpicked.
  - The paired spawns (40/38/39/27/31/36 per twin) show the map is a horizontal flip (x -> 29-x).
  - s8t's games on the same map: as A we worked five of six home mines (own side 2760). As B, (21,2) and (21,8)
    still went to them.
  - alloc (2072-2073): in r51-100 against AlexT101 we have as many rats as they do (17.9 vs 16.1). 6.5 of ours
    stand near mines and 1.1 carry cheese; 2.5 of theirs stand near mines and 4.5 carry.
  - ue1 (s7d self-play and s7d vs araid on uneasy_alliance and popthecork, kept replays) checks how much of our
    own half we harvest when the opponent can't explain it.

## 2026-10-29 19:31 PDT: s8x rejected; what kills our king; s9a (no baby traps) to the replica
- s8x's v3 set-1 block (2754-2763, 2765-2766, 2773-2774) against s7d's reference: 42 cells, 36 alike, D 6 (+1 -5),
  net -4, t -1.63. Rejected. Both heart losses to outercloud were the trap-for-rats trade described above.
- King deaths across s7d's 126 reference games (sets 1-3): the first king died in 62. HP lost in those deaths:
  bites 36%, starvation 23%, throws 22%, cat scratches 9%, traps 7%, our own throws 2%. Starvation is spread over
  many maps: EscapeTheNight, Stash, constrictinglattice, heart, pipes, rose, streetsofnewyork, tiny and wallsofparadis.
- tiny vs AdamTan12 (2094 g2): we spent 930 on 46 rat traps by r74 (babies laid 40 of them); they spent 520. Bank
  at r40: 395 to their 1235. We spawned 33 to their 48, our army fell 19 -> 9 by r56 while theirs held at 16, and
  60 bites killed our king at r74. Our traps hit 98% of the time and killed 14 rats, but the same cheese was about
  25 rats at the time.
- s9a = @s7d @kfix + BABY_RAT_TRAPS false (kings still lay their FORT_TRAPS). Submission #96, set-1 block from
  2781, ahead of its screen (ra_s9a) and its defence run (dr3, against s8w on dr0's cells).
- uneasy_alliance, locally (ue1): s7d vs araid as A left (2,7) at 160 of 800 picked, though araid never went
  there. From r151 our gatherers worked only (8,21), (8,27) and (2,22). The map's mines are symmetric under all
  three candidate symmetries, so the mask stays 7 all game and MINE_MIRROR (which waits for one symmetry) never acts.
  In self-play both sides pick about 90% of their half, so the gap opens when an opponent disturbs us.

## 2026-10-29 20:03 PDT: unseen home mines; round-robin mines (s9b); s9a passes block 1
- s9a (no baby rat traps): v3 set-1 block (2781-2782, 2786-2787, 2793-2794, 2805-2810, 2812-2813) against s7d's
  reference: 42 cells, 32 alike, D 10 (+6 -4), net +2, t +0.63. On to set 2.
- dr2/dr3 against s8w on dr0's cells: s8y (AMMO_AWAY) 14/24, s8z (+ RETREAT_HP 0) 14/24, s9a 14/24; s7d 13/24.
- Probe pb2 (@s7d with the king's mine list ml= and each rat's mine mt= in the indicators):
  - ue2, uneasy_alliance against araid as A: our king listed 5 of our 6 home mines by r47 ((8,2) only at r1731),
    yet 29 of 62 rats took (8,27) and 3 took (2,7). scratchpad picksim.py reproduces 60 of 62 picks: MINE_BALANCE's
    id hash mod m, over a list sorted from each rat's birth tile, piles rats on one mine once the king has walked
    from (11,15) to (7,22). Own-half share to r600: 0.59 to araid's 0.90.
  - hv1, self-play on seven of our worst field maps, own-half share to r500 for side A / B: seepastyourgreed
    0.04 / 0.04, rose 0.24 / 0.17, where_are_you 0.28 / 0.26, attack_on_cat 0.34 / 0.33, fullofsecrets
    0.38 / 0.49, heart 0.86 / 0.89. The losses come without an opponent: most of the unworked home mines were
    never seen by any of our rats by r500 (mineknow.py): where_are_you 5 of 8, attack_on_cat 4 of 6, rose 3 of 4
    ((24,8) is 8 tiles from our start), seepastyourgreed 5 of 8, fullofsecrets 2 of 6.
  - seepastyourgreed: the king starts in a strip (y <= 4) under a wall row whose only gap is 50 tiles west. About
    15 rats chase piles seen through the wall at (39,6)-(49,6). Each new pile or mine target restarts the A*,
    which treats unseen tiles as open, so the rats keep probing the unseen east end of the wall and never take
    the detour.
- New switch MINE_RR (off; s9b = @s7d @kfix + HOME_START + MINE_RR): the king's start squeak carries the newborn's
  place in its role's spawn order, and the rat takes home mine n % m, sorted from our first king's start. The
  king predicts the role with Baby.roleFor, the rule born() applies. ue3 (vs araid on uneasy_alliance and
  popthecork, both sides) and screen ra_s9b are running.
- Next: exploration that finds the unseen home mines (rose, where_are_you, attack_on_cat), and a cheese chase that
  doesn't restart its search on every new pile.

## 2026-10-29 20:14 PDT: dig-aware scouting (s9c-s9e); how abcdezyxwv out-harvests us
- Unseen home mines are mostly behind dirt. Scratchpad pathdist.py gives the true path cost from our start to each
  home mine. hv1 maps: rose's three central home mines need 5 digs each, where_are_you's five unseen need 1-9,
  attack_on_cat's two far ones 20. On seepastyourgreed no dig is needed, but the path out of the start strip is
  89-109 steps to mines 6-12 tiles away.
- New switches (off): SCOUT_FRONTIER makes scouts explore by Flow's frontier search (the never-seen tile nearest
  by path cost, digging on the way) and chase only piles in line of sight. SCOUT_MINES n keeps scouts scouting
  until SCOUT_MINES_UNTIL (600) while the team knows fewer than n home mines.
  - s9c = @s7d @kfix + SCOUT_FRONTIER + SCOUT_MINES 4.
  - s9d = s9c + BREAKOUT_TTL 150.
  - s9e = @s7d @kfix + BREAKOUT_TTL 150 (s7z rebased).
  - hb1 runs each against s7d on seepastyourgreed, rose, where_are_you, attack_on_cat, fullofsecrets, Stash,
    EscapeTheNight, popthecork, cheesebottles and knifefight, both sides; tallies use delivered cheese.
- s9b in ue3 (vs araid on uneasy_alliance, as A): rats per home mine 17/12/11/11/10/8 over all six. s7d had 29 on
  one mine and none on (8,2). Own-half share 0.80 (s7d 0.59).
- Field (s7d's reference games):
  - pipes vs abcdezyxwv (2123 g2), every home mine behind 3+ dirt tiles: they worked all five of theirs by r61
    plus two of ours from r100 and r153; our 2nd-4th mines first got picked at r55, r150 and r329, and the 5th
    never. Picks to r600: 9139 against our 3583; spawn spend 5600 to 2530.
  - Stash (2241 g2): they picked 1720 of our half's 2320 and we picked 540, outnumbering us at our own mines
    (628-937 rat-rounds each to our 136-451).
  - knifefight (2074 g2, lost r67): a brawl from r1. Our traps killed 23 of their rats (62 traps), but they
    grabbed 59 to our 43 and threw 43 to our 23. Their throws hit a robot 38 times in 43, ours 13 in 23, and 7 of
    theirs hit our king (290 of its 600 HP; bites did the rest).
- dr1 is complete: s7h, s8a and s8o 16/24 and s7u 14/24 against s8w (s7d 13/24); all four were already rejected
  on the replica. ra_s8z was stopped at 28/80 to free the VM (s8y carries the same AMMO_AWAY switch).

## 2026-10-29 20:40 PDT: hb1 first half: s9c neutral; SCOUT_DRY (s9g, s9h)
- hb1, s9c (SCOUT_FRONTIER + SCOUT_MINES 4) vs s7d, 20 games on 10 maps x 2 sides: 10/20 wins, delivered 3729 to
  3504 a game. It won rose A (4530 to 2295), seepastyourgreed A (6500 to 560) and B, EscapeTheNight B (7563 to 0)
  and attack_on_cat A/B. It lost both knifefight games (935-1045 to 1679-1810), cheesebottles B (3910 to 8404)
  and Stash A.
- Those three maps have 6 mines, 3 per half. SCOUT_MINES 4 never stops there, so a quarter of the gatherers
  scouted to r600 while their mates worked three mines.
- New switch SCOUT_DRY (off): after SCOUT_UNTIL, SCOUT_MINES scouting also stops once the team's known home mines
  have not grown for SCOUT_DRY rounds (clock from SCOUT_UNTIL, so it ends near r150 when nothing new turns up).
  - s9g = s9c + SCOUT_DRY 100.
  - s9h = s9f + SCOUT_DRY 100 (round-robin mines + frontier scouting).
  - hb2 (not yet started) now runs s9h, s9b and s9g against s7d; s9f was dropped unrun. Screens ra_s9h and ra_s9g
    replace ra_s9f/ra_s9c in the queue.

## 2026-10-29 21:00 PDT: s9a rejected after set 2; s9b (round-robin mines) to the replica
- s9a (no baby rat traps), v3 set-2 block (2823-2824, 2831-2832, 2848-2849, 2852-2855, 2860-2861, 2869-2870)
  against s7d's reference: 42 cells, 35 alike, D 7 (+2 -5), net -3, t -1.13. Sets 1+2: 84 cells, D 17 (+8 -9),
  net -1, t -0.24, below block 2's bar (t <= 1 rejects). Rejected.
  - Set 2 lost outercloud cheesefarm A and superioristhmus A, CS50Crab tornadoes A, AdamTan12 attack_on_cat A and
    spsquared jail B. It gained spsquared thunderdome A/B.
- Screens: ra_s9a +5.1 pts, t +1.18, net +6 (+16 -10); ra_s8y (AMMO_AWAY) +4.8 pts, t +1.34, net +2. Both pass.
- s9b (HOME_START + MINE_RR) is submission #97, with its set-1 block from 2877. The evidence: ue3's own-half share
  0.80 to s7d's 0.59 on uneasy_alliance, a map AlexT101 takes from us. ra_s9b and hb2 (s9h, s9b, s9g vs s7d) are
  still to come.

## 2026-10-29 21:16 PDT: s7d is not side-symmetric (mc1); s9d leads hb1; s9i, s9j, s9k
- hb1, s9d (s9c + BREAKOUT_TTL 150) vs s7d: 13/20 wins, delivered 4374 to 3067 a game (+43%). It loses where
  SCOUT_MINES never stops (knifefight, cheesebottles) and on fullofsecrets. New, for hb2 (now s9j, s9i, s9b vs
  s7d):
  - s9i = s9d + SCOUT_DRY 100.
  - s9j = s9i + HOME_START + MINE_RR, the harvest bundle.
- hb1 also showed s7d's results depend on its side. EscapeTheNight: as A it delivered 0 in both games (3 tiles
  dug), as B 4444-6389. seepastyourgreed: 13310-13464 as A, 560-800 as B.
- mc1 (kept replays): s7d self-play on EscapeTheNight, A picked 0 and dug 3 while B picked 605 (14408 delivered)
  and dug 104. mirror_check: the sides split at r4 on the first newborn. A's spawns at (15,20), B's at the mirror
  of (15,19); A's is a scout ('S'), B's an explorer ('E'). The spawn tile comes from the unmirrored centre and
  king facing (SYM_CENTRE, SYM_FACE) and the role from an id hash (SYM_SLOT).
- The five symmetry switches have been off since s7d. They only met the field inside the s8c bundle (with SWAP and
  CARRY_AIM, rejected at net -2). The owner's brief asks for side-symmetric play, so s9k = s9j + SYM_SLOT,
  SYM_FLOW, SYM_CENTRE, SYM_HAND and SYM_FACE. Its mirror self-play mc2 runs on EscapeTheNight, seepastyourgreed,
  knifefight, rose and DefaultMedium.
- s9b's set-1 block, 8 of 14 matches in: 24 cells, D 6 (+2 -4), net -2, t -0.82. Lost CS50Crab Nofreecheese A/B
  and dirtfulcat A.
  - Nofreecheese 2881 (us A): our king starved at r1609 with no enemy ever near it. CS50Crab picked 5060 on our
    half (own-half share: us 0.52, them 0.92) and spawned 100 rats.
  - In s7d's reference game (2070) CS50Crab spawned 42 and got 43% of its own half. Our rat-rounds at our three
    mines were 1341/1230/544 against s7d's 2919/2168/488.

## 2026-10-29 21:30 PDT: hb1 final; s9k mirrors to r41-48; s9l (corridor swap)
- hb1 final, each vs s7d on 10 maps x 2 sides (wins; cheese delivered a game, candidate to s7d):
  - s9c (frontier scouting): 10/20; 3729 to 3504.
  - s9d (s9c + BREAKOUT_TTL 150): 13/20; 4374 to 3067.
  - s9e (BREAKOUT_TTL 150 alone, as s7z): 12/20; 4025 to 2357. seepastyourgreed 8770/11120 to s7d's 500/530.
  - Breakout carries most of the gain. Every bundle since s9i has it. s7z never went to the replica.
- mc2, s9k self-play (s9j + the five symmetry switches), delivered A/B: EscapeTheNight 6603/5494,
  seepastyourgreed 20875/22635, knifefight 565/475, rose 830/830, DefaultMedium 772/532. No overruns or
  exceptions. s7d's self-play delivered 0/14408 and 410/520 on the first two.
- mirror_check on s9k: the sides now split at r48 (EscapeTheNight) and r41 (seepastyourgreed), not r4.
  - r48: a flow search took 3 turns on one side and 2 on the other. Flow spends whatever bytecodes are left, and
    the mirrored turn has a different count.
  - r41: fighters chasing cats. Cats move on the engine's shared Random(1092) (InternalRobot), so they never
    mirror each other, and no bot can keep a cat map mirrored past their first random step.
  - A deterministic per-turn search budget would remove only the first cause, so it is not worth its slower
    searches.
- s9l = s9k + SWAP_HOLD 2 + SWAP_YIELD + SWAP_NO_ALONG (s7x's corridor swap, 7/8 vs araid on corridor maps).
  rose is still a starvation map for s9k (830 a side). hb2 now runs s9k, s9l, s9i and s9b vs s7d (80 games).
- Screen ra_s9b: +0.2 pts, t +0.05, net 0 (+14 -14): pass.

## 2026-10-29 21:41 PDT: s9b rejected; s9k (symmetric harvest bundle) to the replica
- s9b (HOME_START + MINE_RR), v3 set-1 block (2877-2884, 2896-2897, 2906-2909) against s7d's reference: 42 cells,
  34 alike, D 8 (+4 -4), net 0, t 0.00. Rejected (block 1 rejects at t <= 0).
  - Lost CS50Crab Nofreecheese A/B and dirtfulcat A, and AlexT101 popthecork B.
  - Gained CS50Crab dirtfulcat B, abcdezyxwv sunandmoon B, outercloud seepastyourgreed B and spsquared bed B.
  - Spreading rats evenly over the known home mines does not move the field by itself.
- s9k is submission #98, with its set-1 block from 2920. hb2's head-to-heads (s9k, s9l, s9i, s9b vs s7d) and its
  screen are still running. The replica is the bottleneck, so it goes ahead of them on hb1's component results
  (s9d 13/20, s9e 12/20) and mc2's symmetric self-play.

## 2026-10-29 22:01 PDT: rose is a corridor jam; s9m (rich baby cap); hb2 early
- rose, s9k self-play (mc2): each king sits in a pocket whose only exit is a 1-wide corridor (x=9 then x=8, rows
  0-9 for A) out to mine (2,7). Each side worked only that mine; the three central home mines ((24,8), (24,16),
  (24,24), 5 digs each) were never worked. Both kings starved (A at r1005 with 1682 cheese still in its rats'
  paws). Each side spawned 20 rats in 1395 rounds.
  - stuck_report: 36% of baby turns lost. Outbound ('M') and delivering ('d') rats met head-on at (9,1)-(9,5)
    and held 200-580 rounds at a time.
  - This is the jam the corridor swap was built for, so s9l (s9k + SWAP_HOLD 2 + SWAP_YIELD + SWAP_NO_ALONG) is
    the rose candidate. A second king outside the pocket would serve better, but formation's income test
    (FORM_NET100 -100) fails when nothing gets delivered.
- Nofreecheese 2881 (s9b vs CS50Crab): war from r38. Their 83 rat traps hit our rats 79 times (30 kills, 3484
  damage); traps are invisible to the other team (MapInfo) and fire on the 3x3 around them.
- s9m = s9k + RICH_BABIES 30 + FLOOR_PER_KING: the baby cap rises from 18 to 30 while the bank holds 1000+.
  - The 18 cap binds once harvest works: s9k delivered 20-22k on seepastyourgreed.
  - abcdezyxwv fields 42 rats at r500 to our 16.
  - s5r's flat 30/40 caps starved both kings; this cap only rises while the bank holds 1000+.
  - Screen ra_s9m is queued.
- hb2 partial: s9b 10/20, delivered 3314 to 3538 (neutral, as on the replica). s9k 6/8, delivered 6373 to 3790
  (seepastyourgreed 20875 to 11900 as A, 9620 to 650 as B). Screen ra_s9g: +0.3 pts, net 0, pass.

## 2026-10-29 22:11 PDT: where s7d loses; s9n (s9k + AMMO_AWAY)
- s7d's 126 reference games (v3 sets 1-3) by opponent (our game wins):
  - bc26vibe_bot 16/18, CS50Crab 13/18, outercloudstudio 13/18, spsquared 9/18.
  - AdamTan12 6/18, AlexT101 3/18, abcdezyxwv 2/18.
- The top three win almost entirely by killing our king (abcdezyxwv 16 of 16 losses, AlexT101 14 of 15, AdamTan12
  11 of 12), often early: knifefight r67-73, averyfinel r90-112, tiny r49-218, evileye r155-247.
- How the rats are spent in those 12 matches (alloc, mean babies per class):
  - At r<=100 we field more rats (18.5-20 to 15.6-17) and as many fighting.
  - By r200-300 we are down to 11-12 to their 15-18; abcdezyxwv reaches 29 by r500.
- knifefight 2074 (king dead r67):
  - abcdezyxwv grabbed 56 of ours and threw 42, hitting a robot with 38 of them; 7 hit our king (290 HP).
  - Of those 56 grabs, 29 worked because our rat had less HP than theirs (no threshold: HEALTH_GRAB_THRESHOLD 0) and
    28 came from outside our 90-degree cone.
  - Our 52 rat traps hit 96% (22 kills), so traps are not where this fight is lost; grab and throw is.
- s9n = s9k + AMMO_AWAY: low-HP rats near our king step away from it rather than be thrown into it. On s7d as
  s8y it passed the screen at +4.8 pts, net +2. Screen ra_s9n is queued.
- s9k's set-1 block after 6 of 14 matches: 18 cells, 13 alike, gained 2, lost 3, net -1.

## 2026-10-29 22:19 PDT: s9k rejected after block 1; s9i to the replica; agrab
- s9k (the symmetric harvest bundle) on v3 set 1 against s7d's reference: 42 cells, 34 alike, D 8 (+3 -5),
  net -2, t -0.71. Block 1 rejects at t <= 0.
  - Gained: AdamTan12 tiny A, CS50Crab dirtfulcat B, outercloudstudio seepastyourgreed B.
  - Lost: AlexT101 popthecork B and uneasy_alliance A, CS50Crab Nofreecheese B and dirtfulcat A, bc26vibe_bot
    wallsofparadis B.
- hb2 so far, against s7d:
  - s9k 10/20, delivered 4687 to 3823 (lost knifefight, cheesebottles and Stash on both sides).
  - s9i 5/5, delivered 10187 to 4806 (rose A 4450 to 2295, where s9k delivered 830 to 1195).
  - s9i is s9k without HOME_START, MINE_RR and the symmetry switches, so it goes next: submission #99, set-1
    block from 2964. Its screen ra_s9i replaces ra_s9n.
  - s9n is dropped (its base s9k is rejected); AMMO_AWAY goes on s9i if s9i holds.
- agrab, a local archetype on s7d copying abcdezyxwv's observed play: RUSH + RUSH_BREAK + BREAK_ROUND 30,
  baby caps 40/40, no rat traps, CARRY_AIM + THROW_MIN 84. ag1 (s7d and s9k against agrab, 10 small and medium
  maps, both sides, replays kept) is on the VM. If it beats s7d the way abcdezyxwv does, it becomes the local
  test for rush defence.

## 2026-10-29 22:56 PDT: hb2 final; s9o (s9i + corridor swap); agrab's first games
- hb2 (each candidate against s7d, 10 maps x 2 sides; wins, then cheese delivered a game):

  | Candidate | Wins | Delivered (cand / s7d) |
  |---|---|---|
  | s9i | 14/20 | 4623 / 3706 |
  | s9l | 10/20 | 5544 / 4160 |
  | s9k | 10/20 | 4687 / 3823 |
  | s9b | 10/20 | 3314 / 3538 |

  - s9i won rose, seepastyourgreed, Stash, attack_on_cat and where_are_you on both sides.
  - s9l (s9k + swap) cleared rose's 1-wide stem: delivered 4590 and 2830 there, where s9k delivered 830 and 670.
    But it lost where_are_you, cheesebottles, Stash and popthecork on both sides.
  - HOME_START, MINE_RR and the symmetry switches did not help s9k in self-play either.
- s9o = s9i + SWAP_HOLD 2 + SWAP_YIELD + SWAP_NO_ALONG. hb3 plays it against s9i (same 10 maps, replays kept).
- ag1, first 4 games, all on knifefight:
  - agrab killed the king of s7d or s9k in 27-31 rounds in 3 games; s7d killed agrab's king at r43 in the fourth.
  - knifefight's kings start 5 tiles apart ((17,14) and (22,14)).
  - agrab vs s7d: agrab grabbed 39 rats to s7d's 16. Of its 30 throws, 28 hit our robots and 13 our king,
    which lost 540 HP to throws by r31. s7d's 35 traps all fired and killed 13 agrab rats.
  - This is the same pattern abcdezyxwv showed on the replica (2074 knifefight: 56 grabs, 38 of 43 throws
    hitting our robots).
- ra_s9k finished after the replica verdict: +0.8 pts, net -1 (pass).

## 2026-10-29 23:12 PDT: s9i rejected after block 1; s9r (the grab-rush archetype) to the replica
- s9i on v3 set 1 against s7d's reference: 42 cells, 31 alike, D 11 (+3 -8), net -5, t -1.51. Block 1 rejects
  at t <= 0.
  - Lost AlexT101 popthecork A/B and uneasy_alliance A, CS50Crab Nofreecheese A/B, abcdezyxwv sunandmoon A,
    bc26vibe_bot wallsofparadis B and outercloudstudio heart A.
  - hb2 had it 14/20 against s7d. Self-play against the incumbent did not predict the field again; s9b (hb2
    10/20, replica net 0) and s9k (10/20, net -2) agree with that.
  - The harvest switches (SCOUT_FRONTIER, SCOUT_MINES/DRY, BREAKOUT_TTL) are parked.
- s9r = s7d + agrab's switches (RUSH + RUSH_BREAK + BREAK_ROUND 30, baby caps 40/40, no rat traps, CARRY_AIM +
  THROW_MIN 84, SYM_SLOT). It is submission #100, set-1 block from 3012.
  - This is the big swing: copy the style that beats us. In ag1's first 25 games agrab beat s7d 8 of 11 and
    s9k 9 of 12.
  - s9q (the same switches on s9i) is dropped with its base. Screen ra_s9r is queued; ra_s9i and ra_s9q were
    stopped.
- ra_s9m passed (-0.8 pts, net +1); its base s9k is rejected, so RICH_BABIES waits for a base that holds.

## 2026-10-29 23:20 PDT: ag1 final: agrab beats s7d 14 of 20
- ag1 (10 small and medium maps x 2 sides):
  - s7d won 6 of 20 against agrab, s9k 7 of 20.
  - agrab killed s7d's king in 8 of its 14 wins (knifefight r31, tiny r70-83, averyfineline r358, DefaultSmall
    r502, Excavation r463-791, Stash r794) and won the other 6 on points at r2000 (sunandmoon,
    constrictinglattice and pipes, both sides).
  - s7d's wins: knifefight A, averyfineline B, DefaultSmall A, starvation A/B, Stash B.
- agrab's overruns: 5 in 25 games, each a rat in a crowded fight near the 17500 limit. Exceptions: 0.
- So the switch set that copies the top three beats our champion locally by about the margin abcdezyxwv and
  AlexT101 beat it on the replica. s9r (the same switches as a candidate) is on the replica. ag2 splits agrab:
  agrw (the early war and rush alone) and agtr (agrab keeping our rat traps), each against s7d on ag1's maps.

## 2026-10-29 23:44 PDT: ag2: agrab's edge is not the rush and not dropping traps
- ag2 (against s7d on ag1's 10 maps, both sides):
  - agrw (s7d + RUSH + RUSH_BREAK + BREAK_ROUND 30 only) won 6/20. The early war and rush on their own are
    worse than s7d.
  - agtr (agrab keeping our baby and king rat traps) won 14/20, the same as agrab, including constrictinglattice
    and pipes on points on both sides.
- That leaves the 40/40 baby cap and the aimed throws (CARRY_AIM + THROW_MIN 84). ag4 splits them:
  - agaim = s7d + caps 40 + aim, with no rush and traps kept.
  - agcap = s7d + caps 40 alone.
- s9r's block so far (8 of 14): CS50Crab 6/6. All three losses to outercloudstudio were starvations with no
  enemy near (cheese out r558-1280). s9s (the 40 cap only while the bank holds 1000) is in ag3 against s7d.
- ra_s9r's first start failed ('no java sources': queued before bots/s9r reached the VM); it is queued again
  behind ra_s9s. Sync the bots before queueing a screen.

## 2026-10-29 23:54 PDT: s9r rejected; s9s to the replica
- s9r's v3 set-1 block (replica #100; 3012-3017, 3021-3022, 3040-3041, 3046-3047, 3054-3055) against s7d's
  reference: 42 cells, 29 alike, D 13 (gained 6, lost 7), net -1, t -0.28. Block 1 rejects at t <= 0.
  - Gained: AdamTan12 tiny B, AlexT101 heart A, CS50Crab dirtfulcat B and streetsofnewyork A/B, outercloud
    seepastyourgreed B.
  - Lost: AlexT101 popthecork A/B and uneasy_alliance A, bc26vibe krotus B, outercloud cheesebottles A/B and
    seepastyourgreed A.
- agrab's 14/20 against s7d locally became net -1 on the field. The three outercloud losses were starvations,
  which is what the 40-rat cap invites against a bot that doesn't fight back early.
- ag3 so far (21 of 24): s9s (s9r with the 40 cap only while the bank holds 1000) beats s7d 12 of 21. It wins
  cheesebottles A, so the cap gate does stop that starvation. s9s goes to the replica now so the replica isn't
  idle; ag4 (aim vs cap alone) runs on.

## 2026-10-30 00:16 PDT: the field reference is biased; protocol change
- Games are deterministic: det1 played s7d vs agrab on tiny twice on the VM, with the same winner, round (70) and
  per-team stats. So a replica result is fixed for a pair of builds; the noise between builds is chaos (a small code
  change reshuffles the contested cells).
- So the block that crowned an incumbent is the draw that won, and it overstates the incumbent on those cells:
  - s7d's set-1 reference is its accepting block (20/42). Its sets 2 and 3 are control blocks (23 and 19).
  - s6m: set 1 control 12, set 2 accepting 21, set 3 control 20.
  - s7d's crowning +8 was on set 1 against s6m's unlucky 12. On set 3, where both references are control blocks, s7d
    is one win behind s6m (19 vs 20).
  - The 13 candidates scored against s7d on set 1 averaged net -1.5. AlexT101 popthecork A/B and uneasy_alliance A
    (all s7d wins, one of them a cat kill with one rat of ours left) were lost by 8 of them.
- Protocol change, logged before s9s's block finishes (I had seen 6 of its 14 matches in the listing, all against mid
  bots; the reasons for the change come from s6m's and s7d's references):
  - One block never accepts; from block 2 on, cumulative t >= 2 accepts. The reject bars are unchanged.
  - For s7d's reign, block 1 is on set 2 and block 2 on set 3. Set 1 comes last and is re-anchored first with a twin
    (@s7d @kfix from the current code) whose games become the set-1 reference.
  - s9s: its set-1 block (from 3067) is scored and recorded, but it neither rejects nor accepts. Its set-2 block is
    queued now as block 1 and set 3 follows as block 2.
  - Earlier rejections stand. A candidate rejected on set 1 alone reopens only on fresh blocks (sets 2 and 3).
- TRAINING_ALGORITHM.md section 4: one block never accepts, and the new "accepting block is a biased reference" rule.
