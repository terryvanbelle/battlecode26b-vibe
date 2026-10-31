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

## 2026-10-30 00:44 PDT: s9s's record block; where the field takes our cheese; CAT_LANE
- s9s on set 1 (record only, 3067-3070, 3077-3078, 3087-3094) against s7d's accepting block: 42 cells, D 10 (gained 7,
  lost 3), net +4, t 1.26, 24/42 wins to s7d's 20. Per opponent: CS50Crab, bc26vibe and outercloud 5/6, AlexT101 and
  spsquared 3/6, AdamTan12 2/6, abcdezyxwv 1/6. Its set-2 block (from 3101) is block 1.
- Screens: ra_s9s t +0.97, net +8, and ra_s9r t +0.33, net +6 (both pass). ag4 vs s7d: agaim 9/20 and agcap 8/20.
  So the 40/40 caps and CARRY_AIM don't carry agrab's 14/20; its rush and early war do.
- Pickups on the other team's half up to r300 (s7d's 126 reference games, steal.py):
  - The field takes 329 a game from our half (AlexT101 590, AdamTan12 505, abcdezyxwv 498). 279 of it is mine
    spawns. We take 80 from theirs.
  - In 70% of their pickups none of our rats is within dist2 20 of the pile. Our nearest rat is at dist2 195 on
    average, and the pile waited 27 rounds.
- Per home mine (steal2.py), of the 280 a game they take from our mines:
  - 110 comes from the 50 of our 460 home mines that only they worked by r300 (theirs: 12 of 460).
  - 48 comes from 27 mines they reached first.
  - 122 comes from mines we worked first.
  - Our first pickup at the 3rd-nearest home mine is at r156 on average, theirs at r105.
- The never-worked mines are mostly out of reach rather than unassigned (steal3.py lists them):
  - Only 4 of the 54 in s7d's set-1 games were ever within dist2 20 of one of our rats.
  - They sit behind dirt (dirtpassageway's nearest mine behind 4 dirt rows, heart's two outer mines behind the
    dirt column) or at the end of wall mazes (cheesebottles).
  - s9i's frontier scouting didn't change this on the field (59 such mines in its set-1 games, s9s 59).
- Cat kills: s9s's 16 first-king deaths on set 1 lost 21% of their HP to cat scratches (s7d's references: 9%).
  - On bed (3091; s7d's 2086/2087 the same) our king digs out of its sealed start pocket and walks to (2,11), on cat
    3's lane from (0,0) to (1,11) to (9,11). The cat scratches whatever blocks its path, and the king was dead by
    r245. The spsquared king stayed in its pocket.
  - In 3092 (us B) the roles reversed: our king stayed home and theirs walked down and died.
  - 16 of s7d's 126 reference games and 6 of s9s's 42 had a king take 100+ scratch damage.
- New switch CAT_LANE (off by default):
  - The king marks every cat 2x2 it sees, or hears squeaked by a baby, as lane in Sym.mem (a new CAT bit, kept
    through re-sensing).
  - Its mine walk, cheese steps and digs never add lane to its 3x3, and when it stands on lane it steps off.
  - Unit test testCatLane. Candidates s9t (@s7d @kfix + CAT_LANE) and s9u (@s9s + CAT_LANE). The @s9s snapvar
    preset reproduces bots/s9s (test_snapvar).
  - cl1 (VM): s9t vs s7d and s9u vs s9s on bed, cheesebottles, DefaultLarge, popthecork, sunandmoon,
    attack_on_cat, tornadoes and cheesefarm, both sides. Screens ra_s9t and ra_s9u are queued.

## 2026-10-30 01:23 PDT: s9s block 1 (set 2): net +4
- s9s on set 2 (3101-3102, 3107-3108, 3126-3127, 3144-3147, 3252-3255) against s7d's set-2 control block (2115-2128):
  42 cells, 34 alike, D 8 (gained 6, lost 2), net +4, t 1.41; 27/42 wins to s7d's 23.
  - It wins every AdamTan12 game (6/6, s7d 4/6), and outercloud and bc26vibe 6/6.
  - AlexT101 0/6 and abcdezyxwv 1/6, all by king kill.
  - Gained: AdamTan12 dirtpassageway A/B, CS50Crab wallsofparadis A, abcdezyxwv ultimatestarvation A, bc26vibe
    DefaultLarge B, spsquared thunderdome B. Lost: abcdezyxwv ultimatestarvation B, spsquared jail B.
- Block 1 can't accept under the new rule; t > 0 continues. Block 2 (set 3) is queued from 3256. Accepting needs a
  pooled t >= 2 over sets 2 and 3.

## 2026-10-30 01:47 PDT: CAT_LANE overrun fixed; EscapeTheNight picks nothing
- CAT_LANE's first build overran: cl1 (s9t vs s7d) gave s9t's king 244 overruns on sunandmoon as B (max 20008).
  - The indicator string showed it stuck at (16,9), with its home mine at (19,9) covered by lane. Every turn it
    re-ran offLane, closer, digTile and step, and each counted lane twice over its 3x3 with loops (move() 11136
    bytecode).
  - Fix (d7f1b25): an unrolled 3x3 count, skipped while no lane is known, with the current tile's count cached per
    turn. BotTest checks it against a plain count on every tile after 40 random marks.
  - s9t/s9u are superseded by s9x (@s7d @kfix + CAT_LANE) and s9y (@s9s + CAT_LANE).
  - cl2 (cl1's cells with s9x/s9y): 0 overruns on sunandmoon on both sides (king max 19333). Screens ra_s9x and
    ra_s9y are queued.
- s9s's set 3, EscapeTheNight vs outercloudstudio (3256 A, 3257 B): we picked 0 cheese all game on both sides, and
  the king starved at r1007 with no enemy near. s7d's references (2233/2234) starved the same way (r1002, r1529).
  - The king's room is walled in except through dirt: 5 dirt tiles west, then the 9-row dirt band (y25-33) to our
    two own-side mines (4,37) and (10,45). Their 4580 cheese was never touched.
  - Mine cheese spawns at x18-19, just across the room's east wall, in our rats' view. 75% of baby turns (s7d) chase
    those piles into the wall ('C'). Each give-up is followed by a re-pick on the next sense, so nobody explores or
    digs out: 1 tile dug all game.
  - outercloud digs through and picked 4160 (1000 at its own-side mine, 3160 at the centre column's).
  - BREAKOUT_TTL (s7z/s9e) was built for this. hb1: s9e won EscapeTheNight B 13230 to 0 but lost A. It has reached
    the field only inside the s9i/s9k bundles, which were rejected on set 1 (no EscapeTheNight there).
  - New candidate s9z = @s9s + BREAKOUT_TTL 150. bk1 (VM): s9z vs s9s on brk1's 9 dig maps both sides, and both vs
    araid on EscapeTheNight.

## 2026-10-30 02:14 PDT: s9s block 2 (set 3): net +1; pooled t 1.51, on to block 3
- s9s on set 3 (3256-3269) against s7d's set-3 control block (2233-2246): 42 cells, 39 alike, D 3 (gained 2, lost 1),
  net +1, t 0.58.
  - Gained: AlexT101 tiny B, bc26vibe uneasy_alliance A. Lost: spsquared rift A.
  - Both sides of EscapeTheNight vs outercloud are lost by starvation with 0 cheese picked (s7d the same).
- Pooled over sets 2 and 3: 84 cells, D 11 (gained 8, lost 3), net +5, t 1.51. Block 2 rejects at t <= 1 and accepts
  at t >= 2, so s9s goes on to block 3.
- Block 3 is set 1, s7d's accepting set. Its reference there is the draw that crowned it, so it is re-anchored first:
  twin t0a (@s7d @kfix from the current code; the later switches are all off) is replica submission #102 and plays
  set 1 from match 3270.
  - s9s's set-1 record block (3067-3070, 3077-3078, 3087-3094) is then scored against t0a's games as block 3.
  - Block 3 rejects at a pooled t <= 1.5 and accepts at t >= 2. Otherwise block 4 is final.
- bk1 (VM) so far: s9z (@s9s + BREAKOUT_TTL 150) beat s9s on EscapeTheNight both sides by king kill, delivering
  7969 to 225 as A (122 tiles dug to 9) and 12065 to 150 as B. The breakout works from both sides on the s9s base.
  Screen ra_s9z is queued.

## 2026-10-30 02:27 PDT: enemy traps are a top HP sink; RISK_FRESH
- Screens: ra_s9v (@s9s + AMMO_AWAY) +4.4 pts, t +0.73, net +5: pass. ra_s9t/ra_s9u (the overrun build) passed at
  t +0.17 and +1.04.
- Enemy rat traps, from s9s's 126 field games (scratch traps.py: every enemy RAT_TRAP our robots fire):
  - The field lays 44.2 traps a game; our rats fire 37.8 of them (AlexT101 80.5 a game, CS50Crab 69.5, AdamTan12
    54.1, spsquared 34.3). A trap does 50 damage and a 30-cooldown stun.
  - 42% of our triggers kill the rat. 21% are two or three traps fired by one step (overlapping 3x3 zones: 100+
    damage on a full-HP rat).
  - 62% fire within 2 rounds of the lay: the layer drops the trap on one of its three front tiles (the only tiles
    it can build on) as our rat closes in. 64% are on our half.
  - About 80% are on combat moves (attack 19%, face-enemy 18%, war 17%, aim-carry 11%, flank 8%), where TrapRisk's
    mask is dropped (an enemy within dist2 20, or hunting). 12% are on gathering moves.
  - A trapped rat is left at 50 HP or less, so any full-HP enemy can grab it (the 70% of grabs on our rats that were
    on rats with less HP than the grabber), and throw it into our king.
  - s7d, which lays traps, fired 44.9 a game and hit the field with 28.9 of its 33.6 (86%). s9s lays none.
- RISK_FRESH (new, off by default): where the mask is dropped, still avoid tiles next to the front tiles of enemies
  seen in the last RISK_FRESH rounds. aimCarry's direct steps keep to the mask too. RISK_IN_FIGHT (the 150-round
  marks) froze our fighters on the screen (s8h -8.1 pts); this keeps only the marks where reactive traps land.
  - BotTest: mask(me, ttl) keeps and drops marks by age.
  - t0b = @s9s + RISK_FRESH 2. rf1 (VM): t0b and s9s vs atrap2 (our trap layer at incumbent strength) on 8 trap-heavy
    field maps, both sides.

## 2026-10-30 02:47 PDT: s9s accepted (pooled t 2.60); twin shows s7d's crowning block was +4 lucky
- Twin t0a (@s7d @kfix from the current code, #102) played set 1 (3270-3283). Against s7d's own accepting block on
  set 1 it scores 42 cells, D 6 (+1 -5), net -4, t -1.63. The block that crowned s7d overstated it by about four
  cells: the winner's curse that TRAINING_ALGORITHM §4 re-anchors against.
- s9s block 3 = its set-1 games (3067-3070, 3077-3078, 3087-3094) against t0a: 42 cells, D 14 (+11 -3), net +8,
  t 2.14.
  - Gained: AdamTan12 tiny A/B, AlexT101 heart A and popthecork B, CS50Crab Nofreecheese A, dirtfulcat B and
    streetsofnewyork B, bc26vibe krotus A, outercloud seepastyourgreed A/B, spsquared fullofsecrets B.
  - Lost: AlexT101 uneasy_alliance A, bc26vibe wallsofparadis A, outercloud cheesebottles B.
- Pooled over the three blocks: 126 cells, 101 alike, D 25 (+19 -6), net +13, t 2.60, at or above the t >= 2 bar.
  **s9s is the new incumbent**, resubmitted to the replica as #103.
- s9s's reign: sets 1-3 all carry its accepting luck. Fresh set 4 (research/panel/v3-set4.json) is its reference
  block, from match 3284. Each older set gets an s9s twin before a candidate is scored there.
- Screens of s9s-based candidates now use ra_s9s as the control. Against it s9v (AMMO_AWAY) is -0.8 pts, t -0.27,
  net -3 (pass, weak). s9u (the overrun build) is neutral.
- bk1 finished: s9z (@s9s + BREAKOUT_TTL 150) went 10/18 vs s9s on the 9 dig maps. EscapeTheNight 2/2,
  learn_your_lesson 2/2, mincemeat 0/2; three maps were identical games. On EscapeTheNight vs araid, s9z won both
  sides and s9s lost both.
- HANDOFF.md's State section is rewritten for s9s's reign (it still named s3y).

## 2026-10-30 03:56 PDT: s9s's set-4 reference; trap marks are seen but not used in fights; king traps back on
- s9s's set-4 reference block (3284-3297): 26 of 42 games. 0/6 against abcdezyxwv (Meow, superioristhmus,
  cheesebottles), 1/6 against AlexT101. s9z (@s9s + BREAKOUT_TTL 150) went to the replica as #104 on set 4 as block 1.
- CAT_LANE is neutral. Screens: s9x vs ra_s6y +0.7 pts, net -2; s9y vs ra_s9s +0.7 pts, net 0. cl2 (8 cat maps,
  both sides): s9x 8/16 vs s7d, s9y 8/16 vs s9s, 0 overruns (the bytecode fix holds). Both dropped.
- rf1 (t0b = @s9s + RISK_FRESH 2 and s9s, each vs atrap2 on 8 trap-heavy maps, both sides): t0b 12/16, s9s 9/16.
  But atrap2's traps hit t0b 99.9 times a game and s9s 91.9.
- New scratch tool trapvis.py: for each enemy trap one of our rats fires, could that rat itself have marked the tile
  (it saw the layer, dist2 20 and 90-degree cone, with the trap tile among the layer's three front tiles)?
  - s9s's 126 field games, 3867 triggers: 54% fired on a tile the victim had marked within 2 rounds, 7% within 150,
    18% the rat saw the layer but not facing that tile, 21% never saw the layer.
  - So TrapRisk sees most traps coming; the rats walk in anyway because the mask is dropped once an enemy is within
    dist2 20 (80% of triggers are on combat moves).
  - rf1: RISK_FRESH cut fresh-mark triggers from 465 to 318. But the rats then fired older marks (138 -> 348) and
    unseen layers (425 -> 510) in atrap2's 100-400-trap fields. Attack-role rats ('a') fire the most in every class.
    Screen ra_t0b queued.
- Bug, not yet fixed: TrapRisk allocates its W*H mark array only with 3*W*H + 8000 bytecodes left at the observe
  point. That is 18800 on a 60x60 map, above a baby's 17500 limit: no rat ever has marks there (9 of 45 field maps),
  and on 50x50 or 60x40 maps it needs 15.2-15.5k left. The trigger rates barely differ by map class (29.5, 28.9 and
  36.0 a game on ok, hard and dead maps), because the fights drop the mask anyway. A row-lazy array is the fix,
  behind a switch.
- s9s's losses (field_report, 126 games): 47 of 57 are king kills (mean r384). abcdezyxwv 1-17 and AlexT101 5-13.
  Against abcdezyxwv on superioristhmus they delivered 35472 to our 11455 with five kings to our one. Our army fell
  40 -> 6 (r700-r1100) while theirs rose 48 -> 59. We lost 43 rats to traps and 78 to throws. When our king died,
  4-6 of their rats were near it and none of ours.
  - We form a second king in 26 of 126 games; the field forms one in 15, and we lost 14 of those 15.
- Trap-laying is back on two candidates. A trap does 50 damage and stuns for 30 rounds, which leaves a full-HP rat
  grabbable. That is the field's combo against us, and s7d's traps hit field rats 86% of the time.
  - t0c = @s9s + KING_TRAPS: the king's ring traps, toward the nearest enemy or pre-laid toward the centre.
  - t0d = @s9s + KING_TRAPS + BABY_RAT_TRAPS (s7d's trap behaviour).
  - kt1 (VM): both vs s9s and agrab on the 8 maps of these losses, both sides. Screens ra_t0c and ra_t0d queued.

## 2026-10-30 04:08 PDT: row-lazy trap marks, a rush time limit, and how the field out-harvests us
- RISK_ROWS (t0e = @s9s + RISK_ROWS): TrapRisk's marks go in TrapRows, one map row allocated on its first mark, so a
  rat on a 60x60 map has marks from its first turn. BotTest checks it against the flat array (80 random marks, some
  off-map; clear, risky, mask at TTL and at 4 rounds agree everywhere). Screen ra_t0e queued.
- t0f is the twin: @s9s built from the current code, to re-anchor sets 2/3/1 if s9z passes set 4.
- Our attackers (spawn index 4-15) keep the attack role all game. On maps where the enemy king can't be reached, that
  is most of their turns for nothing.
  - Attack-code share of turns before r400: uneruesansfin 0.52-0.60 (3291 picked up 60 cheese by r400),
    where_are_you 0.41, rose 0.44-0.52, EscapeTheNight and grove 0.60.
  - Field games with a high attack share won 40 of 60, low 29 of 66. That is confounded by the map, so it is no
    verdict either way.
  - RUSH_LIFE (t0g = @s9s + RUSH_LIFE 150): an attacker older than 150 rounds that has not seen the enemy king in
    the last 150 rounds becomes a gatherer. Baby.rushFailed is the pure test, with four BotTest cases. rl1 (VM): t0g
    vs s9s on 10 such maps, both sides. Screen ra_t0g queued.
- How abcdezyxwv and AlexT101 out-earn us, up to r400:
  - They pick up about twice as much per rat-turn (0.60 vs 0.36, 0.97 vs 0.50) and deliver 38-43 a transfer to
    our 23-29.
  - abcdezyxwv harvests our half's mines about as much as we do. In 3293 (superioristhmus) they took 26193 from our
    half and we took 23358; we took 341 from theirs.
  - Our far home mines go unworked, and the near ones crowd ('W', wander at mine, is 22% of gather turns).
  - The shared mine list has 16 slots. superioristhmus has 20 mines, TheHeist and grove 18, and the shared array
    is full, so this one stays on the backlog.
- s9z's screen (ra_s9z vs ra_s9s): +1.3 pts, t +0.41, net +3 (+7 -4), a pass. Its set-4 block is part done: 3314-3326
  so far: outercloud 3-0 and 2-1, bc26vibe 2-1 twice, CS50Crab 2-1 and 3-0, AlexT101 0-3 and 2-1, abcdezyxwv 0-3,
  spsquared 2-1.

## 2026-10-30 04:30 PDT: s9z rejected; our second kings never sit on a mine; FORM_MINE
- s9z (BREAKOUT_TTL 150) on set 4 against s9s's reference (3284-3297): 3314-3323, 3325-3326, 3342-3343. 42 cells, 38
  alike, D 4 (+2 -2), net 0, t 0.00: rejected at block 1. progress/ refreshed.
- Partial screens. ra_t0b (RISK_FRESH 2) at 34 of 80 cells: -12.4 pts, t -1.50, net -6, on the reject line.
  ra_t0c (KING_TRAPS) at 19 cells: +13.4 pts, t +1.23, net +2. ra_t0d at 19 cells: +2.9 pts, t +0.38.
  t0c went to the replica as #105 for set 4, block 1.
- Why we form so few second kings (new scratch tool formk.py over s9s's 126 field games). We formed in 28 games and won
  12. In 38 more the gates (babies, bank, round) held but no site was ever named: we won 18. The field formed in 17
  of our games and we won 1.
  - superioristhmus (4 games, all lost; the field formed 4-5 kings): the gates held from r80 and no baby ever got a
    'K' (formation) move. Our king at (11,6) is walled in. A site must be a home mine whose 3x3 the king itself has
    seen (it sees about 5 tiles), or a clear spot 4 tiles behind it, which is wall here.
  - t0h = @s9s + FORM_RING: the old switch that names a clear 3x3 next to the king when nothing else works. It was
    only ever tried in the formation v2 bundle (s5x, on s4d). Diag fm1 (8 no-site maps, both sides) and screen
    ra_t0h.
- Where cheese is lost, to r400 (minecov.py, latency.py, minerank.py over the same 126 games):
  - Our pickups come 1-2 rounds after the spawn, so where we stand we are fast.
  - Our gatherers crowd one or two mines near the king: 10 babies in one 9x9 spawn square on Meow, cheesebottles
    and superioristhmus. abcdezyxwv keeps 1-5 on nearly every mine, theirs and ours.
  - Our mines by distance from the king. Rank 1: 56% picked by us, 12% by them, 32% left (the king's own pickups
    are not counted). Ranks 2-3: 44-49% us, 31-38% left. Rank 4+: 14-41% us, 49-59% left, with 1-2 of our babies
    in the square.
  - On their half we pick 2-14%.
  - Only 48% of our baby turns to r400 are gatherers'; 30% are fighters and 13-25% attackers. Even our gatherers
    pick 0.25-0.43 cheese a turn; the top field bots pick 0.48-0.62 across all their babies.
- The field forms its extra kings on mines. abcdezyxwv on superioristhmus formed four (r330-563), each 0-1 tiles from
  a mine and spread across both halves; spsquared formed three; AdamTan12 and AlexT101 also formed on mines. Ours form
  4-10 tiles from any mine, beside the first king.
- FORM_MINE (off by default; t0i = @s9s + FORM_MINE, t0j = t0i + FORM_MAX 3):
  - The site is a home mine itself, the nearest one not held by a king and more than 4 tiles from ours, with
    2*dist2 to our start at most its dist2 to theirs. The king need not have seen it: no mine's 3x3 on our 53 maps
    holds a wall or leaves the map, and 59 of 415 hold dirt (those rank last).
  - A site that times out is banned.
  - Babies within dist2 400 join when their slot is drawn (1 in 2), never attackers and not with an enemy in view.
  - A formed king squeaks its old home king's tile, so its babies keep our first king's half and mirrored enemy
    start. Without that, its attackers would go to the mirror of the far mine.
  - BotTest 397/397. Diag fm2 (t0i vs s9s on 8 mine-rich maps, both sides); screens ra_t0i and ra_t0j queued.

## 2026-10-30 04:51 PDT: FORM_MINE skips the income test; our rushers idle through cooperation
- where_are_you (rl1, s9s): the king lists all four home mines by r400, but its income stays 100-330 per 100 rounds
  (bank ~960 at r100, 31 babies). The formation income test (FORM_NET100) alone kept it from forming. Under FORM_MINE
  a mine site now skips that test: a king on a far mine is the cure for a low income. t0i and t0j were rebuilt with
  it before their screens started. fm2 was stopped before its first game and restarted as fm3.
- t0b (RISK_FRESH 2) is rejected at its screen: stopped by hand at 43/80 cells, t -1.73, net -9 (sign p 0.011).
  It fired fewer traps (7.3 vs 8.1 a cell), but our rats died more (51 vs 46), killed fewer (19 vs 23) and moved
  less cheese (7.9k vs 8.4k).
- kt1 (t0c vs s9s, stopped at 16/64 for VM load): t0c won 6 of 16. Its king laid 8.8 traps a game, and 1.6 of them
  fired.
- Turn budget, rounds 400-1200 of s9s's 126 field games (whyagg.py, movestall.py). Of all baby turns, non-fighters
  walking to a mine take 18.4%, attacking 15.0%, at the mine 10.7%, delivering 10.5% and stepping to cheese 10.1%.
  Fighters: to a mine 10.1%, at it 5.8%. Delivering rats carry 68 cheese on average and move on 65% of turns
  (1.68x cooldown).
- Our attackers hoard cheese. The pickUp after the move runs for every role, and attack() comes before deliver().
  From r400, attack-move turns carry 43-82 cheese on average, 10-33% of them carry 100 or more, and those rats move
  on only 31-39% of turns. 95% of the attack turns from r400 are 16+ tiles from every enemy king.
- Cooperation games (coopatk.py): from r200, 25.4% of our baby turns during cooperation are attack moves, against
  5.5% at war. 25 of the 126 games never reached war; we won 14. On rift, rose and averystrangespace, the 12 rush
  attackers (spawn index 4-15) sat on our side of the divide from r200 to r2000.
  - averystrangespace vs bc26vibe_bot, two round-2000 losses: points 0.344 and 0.493, cat damage 0/230 and 0/0,
    cheese delivered 4.5k against 5.1k. Twelve more gatherers would likely have won both.
  - rose vs spsquared: cat damage 0 against 8080.
  - This is what RUSH_LIFE (t0g) targets; ra_t0g is next in the screen queue.

## 2026-10-30 05:12 PDT: t0c rejected; cat traps, light attackers and more kings; t0h on the replica
- t0c (KING_TRAPS) is rejected at set-4 block 1 (3355-3360, 3378-3385 vs s9s's 3284-3297): 42 cells, 37 alike, D 5
  (+2 -3), net -1, t -0.45. t0d (t0c + BABY_RAT_TRAPS) is dropped; its screen was stopped at 48 cells (t -0.20).
- Cat damage, s9s's 126 field games (catagg.py). We average 1773 a game by bite and 992 by trap (11.8 cat traps
  laid, 10.0 fired). The field averages 956 and 695. The losses on points are where a field bot lays many cat traps:
  rose (spsquared laid 73 to our 6; cat damage 0 to 8080), rift (81 to 26), superioristhmus (80 to 50).
- The engine's cat rules:
  - A cat trap costs 10, deals 100, counts as cat damage for the team that laid it, and only cats set it off.
  - At most 10 are live per team. None can be laid after the backstab, except by the backstabbed side for 100 rounds.
  - Cats walk their waypoint loop by BFS, so each lap covers the same tiles. At each waypoint they hunt rats for 8
    turns.
  - When the last cat dies, the game ends on points (TheHeist r303-416; fullofsecrets r130).
- New switches, all off by default:
  - CAT_LAY: during cooperation babies mark cat footprints (CatLane, one row at a time). A tile under a cat on two
    passes 20+ rounds apart is lane, and a baby next to it lays a cat trap there, with 400+ global cheese. t0k = @s9s +
    CAT_LAY; diag cy1 on 8 cooperation maps.
  - ATK_CARRY: an attacker takes no pile that would lift it above 25. t0l = @s9s + ATK_CARRY 25.
- fm3 (t0i so far): the second king forms on a mine (0-1 tiles off) at r85-299. t0i is 3 of 6 vs s9s
  (superioristhmus 2-0, Meow 1-1, rift 0-2). fm1 (t0h, FORM_RING): 9 of 12 vs s9s on the no-site maps, mostly on
  round-2000 points with two kings to one.
- Up to 5 kings are allowed before r1200, and 2 after. 98 of s9s's 126 field games ended with every king destroyed.
  abcdezyxwv on superioristhmus (3380) formed four extra kings at r304-539, all on mines. t0m = @s9s + FORM_MINE +
  FORM_RING + FORM_MAX 4.
- t0h goes to the replica next (#106, set 4 from match 3401) on fm1's showing. Its screen ra_t0h started at the same
  time: ra_t0c was stopped, since its candidate is decided. If ra_t0h reaches the reject line, the block is pulled.

## 2026-10-30 05:47 PDT: cy1 done; the top two win on harvest; axis mines; t0o
- cy1 (t0k = @s9s + CAT_LAY vs s9s, 8 cooperation maps, both sides): t0k 10 of 16. Cat traps laid 18 a game to 13,
  fired 14 to 12, trap cat damage 1424 to 1169. The gain is on rift, TheHeist and where_are_you; on rose,
  averystrangespace and EscapeTheNight cats never came near and nobody laid any. cy2 (t0n = t0k + CAT_RING) runs
  on the same cells.
- Partial screens vs ra_s9s: ra_t0e 45 cells t -1.14 (net -3), ra_t0g 30 cells t +1.51 (net +2), ra_t0h 25 cells
  t -1.00 (net -2). t0h's replica block so far (3401-3406, 3418-3419): 24 cells, 23 alike, net -1.
- s9s's 126 field games: abcdezyxwv 1-17, AlexT101 4-14, AdamTan12 7-11; 47 of 57 losses are king kills, 27 of
  them before r500. In the abcdezyxwv and AlexT101 games (roles.py, to r300) we pick 0.31 and 0.22 cheese a rat-turn
  to their 0.63 and 0.43; our attackers are 26-35% of the turns and pick 0.07-0.13.
  - Stash vs abcdezyxwv (3264, 3265): we picked 180 and 500 to r270, they 3720 and 2959. They reach our three mines
    first (r31-49) and keep 360-512 rat-rounds there to our 81-278; 36-38% of our turns are the rush.
  - Excavation (3265): kills were even (33 of theirs, 37 of ours; 28 of ours by throw landing), but they picked 1803
    to our 898 and kept 16 rats while ours fell from 20 to 7 between r195 and r225; then our king died at r269.
- Axis mines: a mine as near their start as ours is its own twin, so a spawn drops both piles beside it. s9s never
  targets one (MINE_TIES is off in every preset since s6j). Tie maps in the corpus: EscapeTheNight (7 of 11 mines),
  Excavation (1/3), closeup (2/8), starvation (1/3); on gridlock, canyoudig, ultimatestarvation and 5t4rv4t10n every
  mine is a tie, and with no home-half mine every mine is used anyway. Excavation 3265: the axis mine spawned 840 to
  r269; abcdezyxwv took 600 and we 240. Set 4 holds two tie maps (Excavation vs AlexT101, closeup vs AdamTan12).
  - t0o = @s9s + MINE_TIES. s5w (the same switch on s4d) was +27.2 pts vs araid on the tie maps (t +2.84) and net 0
    on replica set 3, which had one tie map. Diag mt2 (vs s9s, the 4 tie maps, both sides, seeds 0-1); screen
    ra_t0o queued.

## 2026-10-30 06:04 PDT: t0h rejected; a second king starves on evileye; t0g on the replica
- t0h (FORM_RING) on set 4 against s9s's reference (3284-3297): 3401-3406, 3418-3423, 3436-3437. 42 cells, 37 alike,
  D 5 (+2 -3), net -1, t -0.45: rejected at block 1. It gained spsquared Meow A/B and lost spsquared evileye A/B and
  CS50Crab grove B. On evileye both games formed a second king (r~100-125). The bank ran out at r374 and r550, and
  both kings starved, while spsquared's own king was starving too (100 hp from r325 in 3423). One king eats half as
  much, so s9s outlasted them. Any extra-king switch needs a starvation guard on cheese-poor maps; t0i/t0m (FORM_MINE
  sites skip the income test) carry the same risk.
- ra_t0h stopped by hand at 42 cells (t -1.17, net -2) once the replica had decided; ra_t0i took its worker.
- t0g (@s9s + RUSH_LIFE 150) goes to the replica as #107 for set 4, block 1, from match 3450. Its screen is at 30
  cells, t +1.51. Its target on set 4 is uneruesansfin vs AlexT101 and averystrangespace vs bc26vibe_bot, where
  s9s's rushers walk for hundreds of rounds without reaching the enemy king.

## 2026-10-30 06:34 PDT: t0o 11 of 16 on the tie maps; s9s is not side-symmetric; t0q
- mt2 (t0o = @s9s + MINE_TIES vs s9s, the 4 tie maps, both sides, seeds 0-1): t0o won 11 of 16 and delivered 3463 a
  game to 2602. closeup 4/4, starvation 3/4, Excavation 2/4, EscapeTheNight 2/4 (coin flips). ra_t0o and ra_t0p moved
  to the front of the screen queue.
- Partial diags against s9s:
  - fm4 (t0m: FORM_MINE + FORM_RING + FORM_MAX 4) 8 of 14. It forms 2-4 kings but delivers 13.3k to 16.6k a game; it lost
    keepout B 3207 to 21933 and cheesebottles A/B.
  - cy2 (t0n: CAT_LAY + CAT_RING) 8 of 13, trap cat damage 1342 to 972.
  - ma1 (t0p: MINE_ANY + MINE_TIES) 8 of 14.
- In ma1, Stash and cheesebottles were decided by the side, not the build. t0p played exactly as s9s there; side B
  delivered 9565 to 560 on Stash, and side A 10886 to 5698 on cheesebottles. mirror_check on the Stash pair: both teams
  diverge at r2, on the first newborn's explore target (10,18 against its twin's 12,16). With no mine known, Baby.explore
  ranks its samples by Chebyshev distance to ((W-1)/2, (H-1)/2). On a 30x30 map that point is (14,14), half a tile
  off the true centre, so mirrored samples rank differently. SYM_CENTRE fixes it, but s9s carries only SYM_SLOT of the
  five symmetry switches. SYM_FLOW, SYM_CENTRE, SYM_HAND and SYM_FACE were only ever tried inside bundles (s8c, s9k),
  and both were rejected.
  - t0q = @s9s + SYM_FLOW + SYM_CENTRE + SYM_HAND + SYM_FACE (the owner's rule: every move symmetric when the sides
    switch). Mirror run sym1: t0q and s9s each against themselves on 8 maps, each game checked with mirror_check.

## 2026-10-30 06:47 PDT: t0g rejected; t0o on the replica
- t0g (RUSH_LIFE 150) on set 4 against s9s's reference (3284-3297): 3450-3455, 3457-3460, 3463-3464, 3476-3477. 42 cells,
  38 alike, D 4 (+0 -4), net -4, t -2.00: rejected at block 1. Its target cells, uneruesansfin vs AlexT101 and
  averystrangespace vs bc26vibe_bot, stayed lost. It lost outercloud seepastyourgreed A/B, AlexT101 where_are_you B and
  AdamTan12 attack_on_cat A. seepastyourgreed is a noisy cell: s9s lost it on both sides in 3270-3271 and won both in
  its reference.
- Screens vs ra_s9s: ra_t0e done, 80 cells, -3.1 pts, t -0.75, net -3, a weak pass. ra_t0g 75 cells, t +1.87.
  ra_t0i 28 cells, t +0.37.
- Finished diags against s9s: fm4 t0m 9 of 16 (delivers 11.9k to 14.8k); cy2 t0n 11 of 16 (trap cat damage 1459 to
  1181); ma1 t0p 9 of 16.
- t0o (MINE_TIES; mt2 11 of 16 on the tie maps) goes to the replica as #108 for set 4, block 1, from match 3491. Away
  from axis mines it plays as s9s does, so most cells should repeat the reference. The test rests on Excavation vs
  AlexT101 (s9s lost both) and closeup vs AdamTan12 (s9s won both).

## 2026-10-30 07:07 PDT: two more mirror breaks, tied spawn tiles and flow search depth; t0r, t0s

- sym1 (t0q and s9s self-play, mirror_check on each game's own two sides). knifefight: t0q splits at r4, when two
  rats meet on the centre line and the one that moves first bites. Our bot can't remove that; turn order decides it.
  superioristhmus: t0q splits at r27, the same round as s9s. The king's two best spawn tiles were a ring corner
  (47,54) and an edge (47,55). They scored the same (the corner's -200 matched its centre gain) and had the same pick
  bit, so the dx/dy loop order chose, and that order isn't mirrored. Stash: the r38 split is flow. Twin rats' searches
  ended a turn apart because the search runs while bytecodes last.
- New off-by-default switches (unit tests 418/418):
  - SYM_TIE: an exact tie in a tile pick (spawn, rat and cat traps, ring dig, rescue, guard, enemy-king guess, mine
    site) goes to G.tieFirst. That is a cross product of the picker's centre offset with the tile's offset, signed by
    the side chirality, then the dot product. It is a strict total order that a rotated or reflected twin keeps.
  - FLOW_QUOTA: a rat runs at most N search steps a round. Probe pb2 (t0q with Debug.PROBE) on superioristhmus: 1018
    turns popped, 542 of them cut by bytecodes, popping p5 4, p10 20, p50 25, p90 28. A quota of 18 binds before the
    bytecode cut on ~90% of cut turns, and costs ~30% of search speed.
- t0r = t0q + SYM_TIE + FLOW_QUOTA 18. Self-mirror run sym2 is on t0q's 8 maps, and its screen is queued second.
- t0s = @s9s + DELIVER_AT 50 (economy). Each carried unit slows moves by 1%, so a 50-cheese trip home costs 1.5 walks
  to 30-cheese's 1.3 but brings two-thirds more. Diag dl1 is on ma1's 8 maps.
- Screens so far vs ra_s9s: ra_t0i 54 cells t +0.43; ra_t0o 32 cells t +1.74; ra_t0p 14 cells t +1.66. t0o's set-4
  block is 6 of 14 done, all won as s9s did.

## 2026-10-30 07:27 PDT: t0o rejected; t0i on the replica; the mirror splits left are contact

- t0o (MINE_TIES) on set 4 against s9s's reference (3284-3297): 3491-3494, 3496-3497, 3509-3516. 42 cells, 41 alike,
  D 1 (+0 -1), net -1, t -1.00: rejected at block 1. Away from axis mines it plays as s9s, and on the axis maps it
  changed nothing: Excavation vs AlexT101 still lost both, closeup vs AdamTan12 still won both. The lost cell is CS50Crab
  groveisbeingdesecrated B.
- t0i (FORM_MINE: extra kings on home mines) goes to the replica as #109, set 4 from match 3522. Its fm3 diag beat s9s
  2-0 on superioristhmus, and Meow, superioristhmus and cheesebottles are set 4's abcdezyxwv maps (s9s lost all six).
  Screen ra_t0i: 54 cells, t +0.43.
- sym1 done. Every first split left in t0q's self-play is contact, or comes after it, in the engine's turn order:
  - Meow r18: the twin attackers meet at the centre line, and A's rat, moving first, sees B's rat where it stood last
    round.
  - DefaultMedium r41 and knifefight r4: fights.
  - cheesebottles (t0r, sym2) r307: cat 3 scratched its rat and its twin, cat 4, did not. The engine's cat takes the
    last rat in its sense order as its target and turns to the first squeak it hears; neither is mirrored.
  The splits that came from our bot (superioristhmus r27 spawn tie, Stash r38 flow depth) are what t0r fixes. Stash on
  pb2 (the flow probe): cut turns popped p5 16, p10 18, p50 22, so FLOW_QUOTA 18 binds on ~90% of them there too.

## 2026-10-30 07:43 PDT: many rats never start a flow search; FLOW_ALLOC; t0v, t0w

- sym2 (t0r self-play) first splits: Stash r73 (t0q r38), superioristhmus r33 (r27), cheesebottles r307 (r74, a cat
  scratch), where_are_you r27 (a cat scratch), Meow r18 and DefaultMedium r41 (contact), knifefight r4. No overruns
  past one turn a side, no exceptions.
- EscapeTheNight r26 led to a bug, not a mirror issue. The twin attackers each had a flow search requested from r20.
  Team 2's twin spent 12810 bytecodes a turn and had a 20-tile path at r24. Team 1's spent exactly 6410 a turn, popped
  no node, and stood still. Flow.work0 allocates the search arrays only in a turn with 2*W*H + 1700 and then
  NB*RING + 1024 = 9216 bytecodes left past FLOW_RESERVE 2500, because an allocation costs its length. A rat whose turn
  runs past ~5800 bytecodes before Flow.work can never start its first search, and the mean rat turn is ~7300.
- The pb2 probe counts it (rats whose cumulative pops stay 0 while a search is building):
  - superioristhmus: 48 of the 87 rats that ever asked for a search never popped a node. 9.6% of all rat-turns waited
    on a requested search. Median wait from request to first pop 72 turns, p90 1021.
  - Stash: 8 of 86 never; 25.9% of rat-turns waiting; median 16.
  Those rats fall back on greedy and bug steps: flow was never doing what the stall fixes assumed.
- FLOW_ALLOC (off by default) allocates g, par, each of the 8 bucket rows and the path one piece at a time. Each piece
  needs only its own length past the reserve, so a busy rat has them within a few turns. BotTest 420/420.
- t0v = @s9s + FLOW_ALLOC (the fix alone); t0w = t0r + FLOW_ALLOC. Probe pb3 (t0v with Debug.PROBE) runs as pba1 on
  superioristhmus and Stash to confirm the waits are gone. Diag al1 is t0v vs s9s on the 8 rz maps, and ra_t0v
  heads the screen queue.
- ex1 (s9s vs araid on Excavation, indicators): the mines sit inside a solid dirt block. Our explorers find them at
  ~r40 and the first pickup is at r55. Attack moves take 37% of rat-turns to r70. AlexT101 dug to the centre mine by
  r25 on the replica.

## 2026-10-30 08:21 PDT: t0i rejected; t0p on the replica; rush size and deliver-at diags

- t0i (FORM_MINE, replica #109) set-4 block 1 (3522-3525, 3528-3529, 3546-3551, 3562-3563) vs the s9s reference
  3284-3297: 42 cells, 33 alike, D 9 (+3 -6), net -3, t -1.00. Rejected (block 1 rejects at t <= 0). It lost
  outercloud fullofsecrets B and seepastyourgreed B, CS50Crab grove B and knifefight A, spsquared evileye B and
  AlexT101 where_are_you B; it gained spsquared Meow on both sides and AlexT101 where_are_you A. abcdezyxwv is still
  0 of 6. Its screen was a weak pass (t -0.36) and fm3 had it 10 of 16 over s9s on mine-rich maps: a second king on
  a mine doesn't carry over to the field.
- t0p (MINE_ANY + MINE_TIES: gatherers spread over every known mine, either half and the axis) goes up as #110, with
  its set-4 block from match 3572. It has the strongest screen so far, ra_t0p at 66 of 80 cells: D 28, +13.7 pts,
  t +3.29, net +6. The araid screen predicts badly (t0o screened +2.02 and failed), so the replica decides.
- Other screens part-way: ra_t0v (FLOW_ALLOC) 30 cells t +1.41, net +3; ra_t0j (FORM_MAX 3) 40 cells t -0.11.
  Diag al1 (t0v vs s9s, 8 maps, both sides) is 5 of 10 so far: the allocation fix isn't weaker.
- Rush size (never tuned): rz1, t0t RUSH_TO 12 (8 rushers) vs s9s, won 10 of 16 (Excavation, fullofsecrets and
  evileye 2-0, superioristhmus 0-2). Screen ra_t0t is queued. rz2, t0u RUSH_TO 20 (16 rushers), won 9 of 16 and is
  parked.
- DELIVER_AT 50 (t0s): dl1 7 of 16 vs s9s, delivered 11072 to 11232. Dropped.

## 2026-10-30 08:40 PDT: the set-4 reference ran lucky; the cells every build loses; the averystrangespace pocket

- Every set-4 block so far (s9s's reference and the s9z, t0c, t0h, t0g, t0o, t0i, t0p candidates), cell by cell
  (scratch cells4.py): the reference won 25 of 42 cells, the candidates 23.3 on average. Its edge is luck on a few
  coin-flip cells: CS50Crab grove B (reference 1, candidates 0.29), outercloud fullofsecrets B and seepastyourgreed B
  (0.57 each); against it, spsquared Meow A (0 vs 0.67). Block 1 rejects at t <= 0, so a candidate as strong as s9s
  is rejected more often than not. Next replica slot: a second s9s sample (twin t0f) on set 4, so the reference cells
  average two games (contest.py paired already averages several reference games per cell).
- 14 cells are lost by every build: abcdezyxwv 6 of 6, AlexT101 Excavation and uneruesansfin 4, bc26vibe_bot
  averystrangespace 2, CS50Crab grove A, AdamTan12 averyfineline A. A build only gains by turning some of these.
- First pickups at our home mines in the reference block (scratch firsts.py) show where we never reach mines:
  - averystrangespace: 1 of 3 home mines in 2000 rounds (bc26vibe_bot reaches its other two at r266-1228).
  - where_are_you: 4 of 8 (AlexT101 has all 8 by r552).
  - uneruesansfin: our home mines first at r562-846 (AlexT101 r266-278), and it takes 8.5k there to our 0.1-0.3k.
  - cheesebottles: [24, 200, never] to abcdezyxwv's [34, 52, 156].
- averystrangespace: our king's corner is a pocket sealed by walls, and the only exit is a diagonal channel of
  ~12 dirt tiles. as1 (t0v vs s9s, both sides): neither side ever leaves; mine 10,7 gives 4520 and the other two
  home mines are never seen. A scout's reverse flow search toward a random unseen tile took 24 turns on 60x60, and
  the scout's target changed before most searches finished; scouting ends at r150.
  - t0x = @s9s + SCOUT_FRONTIER (scouts take the nearest unseen tile by path cost, dirt included).
  - t0y = t0x + FLOW_ALLOC.
  - Diag fr1 runs both against s9s on averystrangespace, cheesebottles and where_are_you, both sides.
- cb1 (t0v vs s9s, cheesebottles): the corner mine 2,2 is first picked at r232 (s9s r303-308). The team knew
  1 mine at r66, 2 at r180, 3 at r311. Our attackers' flow searches toward the enemy king never finished in s9s.
- al1 done: t0v 6 of 16 vs s9s (closeup 0-2, delivering 10.0-10.7k to 15.6-16.2k; evileye 0-2; Meow 2-0). It is not
  the next submission. Its screen is at t +1.65 after 43 cells.
- t0p's block so far: net -2 after 36 cells (lost grove B and seepastyourgreed B, the reference's lucky cells),
  and abcdezyxwv 0 of 6 again. Spreading over every mine doesn't move that matchup. In 3548 (t0i) abcdezyxwv out-picked us
  20.9k to 6.1k on cheesebottles. On Meow it won a throw war we opened at r17 (kills per death 0.76; we were down to
  7 rats by r160 and broke by r200).

## 2026-10-30 08:57 PDT: t0p rejected; a second s9s sample on set 4; rushers stuck in grove's bands

- t0p (MINE_ANY + MINE_TIES, replica #110) set-4 block 1 (3572-3577, 3581-3586, 3603-3604): 42 cells, 38 alike, D 4
  (+1 -3), net -2, t -1.00. Rejected. It lost CS50Crab grove B and outercloud seepastyourgreed B (two of the
  reference's lucky cells) and AdamTan12 closeup B, and gained averyfineline A. abcdezyxwv was 0 of 6 again. Its screen
  ended at t +3.09, net +6: the second strong screen pass (after t0o) to fail on the replica.
- t0f (the s9s twin) goes up as #111 on set 4, block from match 3610. Set-4 reference cells will average two s9s
  games (`contest.py paired 3284-3297,<t0f ids> CAND`).
- groveisbeingdesecrated, where every build loses side A: each king sits by the one exit of a 60-wide band (the
  top band's gaps are at x 57 and 59 in row 53, beside the top-right king). Our 12 rushers head straight for the
  enemy king, walk the band's length and sit packed at x 26-33 from r80 on. 3288 (A): a cat scratches our king to
  death r100-190 while 3 fighters fail to kill it and the other 17 rats are stuck in the band. 3289 (B): the same
  mirrored, and we won only because CS50Crab's king died to its cat first. The rushers' flow search should take the
  exit beside home, but on 60x60 a rat whose turn runs past ~5800 bytecodes never allocates the search arrays
  (the FLOW_ALLOC bug). Diag gr1 (t0v and t0w vs s9s on grove, both sides) checks whether the fix frees them.
- Stuck travel turns in the reference block (scratch stuck2.py: our rat within 2 tiles of the same spot 30 rounds
  later while its code is a travel code): 46% overall. averystrangespace 72-87% (attackers in the pocket),
  attack_on_cat 61-65% and ZeroDay 52-54% (gatherers queued outside a mine's 5x5, carriers queued at the king).

## 2026-10-30 09:45 PDT: replica games are deterministic; BitFlow (u0c); u0a/u0b diags; endgame king (u0d); first-turn overruns (u0e)

- The t0f block (s9s's twin, #111) so far: 24 cells, all alike to s9s's reference 3284-3297. The games themselves
  repeat exactly (same winners and rounds) against outercloud, bc26vibe_bot and AlexT101; only CS50Crab varies
  (3718/3719 differ in rounds; it must draw on randomness of its own). So a second sample of the same code adds
  nothing against deterministic opponents. A candidate's "noise" on the replica is the chaos its own change
  sets off, not seed luck, and pooling a twin's block doesn't shrink it. More maps per opponent would.
- Points at round 2000 (engine setWinnerIfMorePoints): at war, 0.3 cat damage share + 0.5 king share + 0.2 cheese
  transferred share; in cooperation, 0.5 / 0.3 / 0.2. Pickups don't count; kings alive at the end do. In s9s's
  set-4 block 9 of 42 games went to the limit, and we ended 7 of them with one king. averystrangespace 3287 was
  lost 24-25 with no cat damage on either side; a second king would have won it 29-20. The others were lost by
  45+ or won anyway.
  - u0d = @s9s + END_KING 1850: from r1850 a lone king names a site (a clear 3x3 beside it) whenever it has 7
    babies and 400 cheese. No income test; it retries after FORM_TTL. Diag ek1 (vs s9s, 8 limit-prone maps,
    both sides) is running.
- First-turn overruns: s9s's only field overruns (3292/3293 superioristhmus, one each) and the local ones are a
  newborn's first turn on a big map. One reached 17424 used before the indicator string; another crossed in the
  re-sense after moving or the flow work (OVS=999). The next turn goes to finishing the old one.
  - u0e = @s9s + SENSE2_LEFT 6000 (4000) + IND_LEFT 1000: the re-sense and the indicator need that much left.
    Diag bh1 (superioristhmus, seeds 0-3, both sides) is running.
- BitFlow (u0c, committed cae2c1b): a reverse distance field over row bitboards. Dirt tiles settle DIG_COST-1
  waves late, so a rat walks through dirt when that is the shorter way out of a pocket. Diag bf1 has 2 of 16 so
  far (grove 1-1).
- kc1 done: u0a (KING_CAT_BITE) 8 of 16 vs s9s. grove 2-0: our side-A king no longer dies to the r190 scratch.
  evileye 0-2; the rest 1-1. st1 (u0b, RUSH_STICK) 4 of 8 so far.
- My mistake: re-adding ra_t0r to screenq.txt while it ran started duplicate workers twice. I killed both
  duplicates and the original kept going. Rule: never put a running run back in screenq.txt.

## 2026-10-30 10:36 PDT: set 2 re-anchored (u0f); diags in (u0b, u0c, u0e); Meow's enemy mine (u0h); u0e submitted

- u0f (s9s with every G.hand tie flipped, #112) re-anchored set 2: 3728-3741, 18 of 42. Against s9s's own
  accepting set-2 games 2115-2128 (23 of 42): 42 cells, 31 alike, D 11 (+3 -8), net -5, t -1.51. So a neutral
  salt flips about a quarter of the cells, and s9s's set-2 draw was about 5 games lucky. Set 2's reference is now
  u0f's block.
- Diags against s9s on the set-4 maps (16 games, both sides): u0b (RUSH_STICK) 8 of 16, dropped. u0c (BitFlow)
  5 of 16, parked: overruns on where_are_you and superioristhmus, two starvations. On averystrangespace it picks
  twice s9s's cheese but transfers no more. bh1 (u0e, superioristhmus seeds 0-3): u0e 0 overruns in 4 games (max
  16012), s9s 1 in 2 of 4. Results 2 of 4.
- Field loss study, abcdezyxwv on Meow (3292 g0, lost r477). Our deliveries fell to 1.2-3.2 a round, theirs were
  8-20. From r100 they work all 4 of their mines plus our (14,11), and from r200 our (23,2) as well. We work 2-3.
  Their 50 rat traps had a mean life of 1.6 rounds (hit 98%): reactive traps on our rats' next tile.
  - The cause on our side, found in a local s9s vs abrk game on Meow from the indicator strings (mt=, sym=). The map
    is a flip, but rotation stays live until about r600. homeHalf() tests only the first live symmetry (rotation),
    and under rotation the enemy mine (14,18) is nearer our king than its twin. So it counts as ours, and
    MINE_BALANCE (slot mod 5) sent 1 in 5 gatherers there, where they died. Survivors crowd the two nearest mines:
    5-8 waiting at each, for 2.26 cheese a round per mine.
  - u0h = @s9s + HOME_ALL_SYM: a tile is on our half only when every live symmetry agrees, and MINE_BALANCE
    re-picks when the mask changes. bccmp: only new members with the switch off. BotTest 445. Diag hs1 (u0h vs s9s,
    8 maps including Meow, both sides) is running.
- cheesebottles (3292 g1): we never saw our corner mine (2,2). Its cheese mostly lands on the mine tile itself (880
  by r663), and abcdezyxwv took 840 off its twin. Not yet acted on.
- Next: u0e (#113) as a basics repair on set 4 (3742 on). Reading of the repair rule (§1.11): a repair runs two
  blocks (set 4, then the re-anchored set 2) without the block-1 reject, and accepts at pooled t >= 0.

## 2026-10-30 11:27 PDT: u0e's block in; stuck rushers on cheesebottles; u0m (basics bundle) submitted

- u0e (#113) set-4 block 1 (3742-3755) vs s9s's 3284-3297: 42 cells, 38 alike, D 4 (+1 -3), net -2, t -1.00.
  Its repair worked: 0 overruns in 42 field games (reference 2). Folded into u0m instead of a second block alone.
- Diags against s9s: ek2 (u0g, END_KING_CROWD) 8 of 12, the same per-map results as u0d. hs1 (u0h,
  HOME_ALL_SYM) 6 of 16, dropped. Screen ra_t0t (RUSH_TO 12): +4.0 pts, t +0.92, net +4, pass.
- minesee scan (set-4 reference): our home mines are often seen late or never. uneruesansfin: 32,49 first seen
  r562, and AlexT101 took 6334 there. averystrangespace: 2 of 3 never seen. cheesebottles: the corner (2,2) never
  seen. Most "never" cells on grove/knifefight are just short games.
- uneruesansfin vs AlexT101 (3290 g1): their king walked 49 tiles to its corner mine by r751 and formed a second
  king by our straight-line mine at r~900, with 45 rats at r1200. Our king never moved (KING_MINE only walks
  within dist2 400). Our deliveries were 3080 in 2000 rounds, less than the king ate (4000); our bank ran down
  2488 -> 20. Our rats step forward 99% of the time (theirs strafe 35-42%), so the losses aren't in movement.
- cheesebottles vs abcdezyxwv (3292 g1, lost r684): they kept a ~1900 bank on 16 rats to r300. We spent ours on
  30 rats by r200, delivered a third as much, and fell to 4 rats. We dealt 0 damage to their king. In r1-150,
  59% of our baby turns were attack moves.
  - The rushers never left our half. A local s9s/t0v vs abrk game on cheesebottles shows why (indicator strings):
    10-12 of the 12 sat in the walled room round our (20,13) mine from r75 to r400. Greedy and bug walks lead east
    into it. Each reverse flow search toward the king spot ran ~11k bytecodes a turn and was given up at
    FLOW_REV_TURNS 25, then retried 30 rounds later (fl builds 2-3, done 0-1, stall 50). With two symmetries live,
    the target also flipped between (56,11) and (56,8) almost every turn to r72, and each flip resets the stall.
  - flowstat (local games): s9s has a search building on 29% of rat-turns on cheesebottles (10% ZeroDay, Meow),
    and 100-230 searches a game are given up.
  - u0k = t0v + FLOW_REV_TURNS 60 (cb4 vs abrk): failed searches 3 (t0v 105, s9s 234), rushers in the room 11 at
    r100 and 2 at r200, xfer 12395 (t0v 11177). u0l (+RUSH_STICK): 0 failed, 4 in the room at r100.
- u0m = @s9s + u0e's margins + FLOW_ALLOC + FLOW_REV_TURNS 60, the basics repair bundle, submitted as #114 (set 4
  from 3756). Diag fx1 and screen ra_u0m are running. I stopped ra_u0b at 27/80 (u0b was already dropped).

## 2026-10-30 12:23 PDT: u0m's set-4 block; set 4's winner's curse; traps, endgame king and starvation diags
- u0m (#114) set-4 block (3756-3767, 3785-3786) vs s9s's 3284-3297: 42 cells, 33 alike, D 9 (+3 -6), net -3,
  t -1.00. Lost: AdamTan12 attack_on_cat B, closeup B, AlexT101 where_are_you B (our kings starved), CS50Crab grove
  B, outercloud tornadoes B (both kings starved), spsquared evileye B. Gained: AlexT101 Excavation B, spsquared
  Meow A and B. u0e had lost two of the same cells. Delivery is not the cause: over the differing games u0m
  delivers 4.09 cheese/round to s9s's 4.0 (sd 1.62), u0e 4.5 to 4.58 (sd 0.84).
- Set 4 carries s9s's luck. All 9 candidates scored there against 3284-3297 came out net <= 0 (mean -1.9). Pooled
  cell rates over those 9 draws (`research/panel/v3-set4-pool.txt`, `contest.py pool`) put s9s's draw about 1.7
  wins above the pool, and u0m at 22 of 42 vs 23.4 expected, z -0.82: no reject, so u0m goes on to block 2
  (set 2 from 3796, reference u0f 3728-3741) under the basics-repair rule (accept at pooled t >= 0 after two).
- Reactive traps: in 3290 g0 (Excavation vs AlexT101) 35 of their 39 rat traps fired on us, most within 0-1 rounds
  of being laid next to our rats, for 1622 of the 2270 damage our rats took; our 20 rats were 0 by r301. Our copy
  of the tactic, u0j (@s9s + BABY_RAT_TRAPS + TRAP_ADJ), hits with 95% of its traps but went 7 of 16 vs s9s (tj1):
  it spawns fewer and its king starved on Excavation A. u0q (u0m + the same + TRAP_SPAWN_FIRST) is no better
  (tq1 6 of 16; hits 91%, spawns fewer in 13 of 16; Meow lost both on points with more cheese delivered). Not
  submitted. Local opponents don't lay reactive traps, so this local test can't show the defensive side.
- u0i's END_KING_MINE was inert (ei1 identical to u0d's ek1 in all 16 cells): pickFormSite's clear3 wants tiles
  the king itself has seen, and on ZeroDay B the king sat in its start pocket with the home mines out of view.
  u0r = u0n (u0m + END_KING 1850) + END_KING_MSITE takes FORM_MINE's site from the babies' mine list; diag er1.
- Starvation: king_report over 50 replica matches (3284-3297, 3728-3763) finds 78 king deaths: bite 32, starve
  27, scratch 9, throwHit 7, trap 3. 16 starved with no enemy damage at all (10 games: where_are_you 5,
  uneruesansfin 2, Hike 2, ...). where_are_you 3763 B: the second king formed at (56,56) beside the first with no
  mine, 25k cheese lay on the map at r1000, our rats never came within d2 20 of 4 of the 8 home mines, income
  ~3.7/round against 4 upkeep, and cats took 42 rats. Hike 3728 A: second king at r250 on 1-6/round; both kings
  starved by r680. u0s = u0m + KING_FORAGE: a king bound to starve (projected surplus < 0, from r200, every 50
  rounds) with no mine of its own walks to the nearest home mine no other king holds. Diag fg1 (u0s vs s9s) and
  control fgc (u0m vs s9s) on the starvation maps, both sides.
- Screen ra_u0a (KING_CAT_BITE) passed neutral (t +0.46, net -2): not submitted.

## 2026-10-30 12:53 PDT: u0m accepted (basics repair); the grab-and-trap war against abcdezyxwv; RISK_ADAPT
- u0m's block 2 on set 2 (3796-3809) against u0f's 3728-3741: 42 cells, 33 alike, D 9 (+8 -1), net +7, t 2.33,
  25 of 42 to 18. Gains: spsquared jail A and B, thunderdome A, evileye A; outercloud Hike B; bc26vibe sittingducks B;
  abcdezyxwv ultimatestarvation B; AdamTan12 sunandmoon B. Loss: AdamTan12 attack_on_cat B. Pooled with block 1:
  84 cells, D 18, +11 -7, net +4, t +0.94. A basics repair accepts at pooled t >= 0 after block 2, so u0m is the
  incumbent (12:49). progress/ refreshed. u0m's reference block on set 3 (never played by it) runs from 3819.
- Per opponent over the recent blocks: abcdezyxwv 1 of 18 and AlexT101 1 of 18; the rest 10-15 of 18. Against
  abcdezyxwv (3764/3765, both sides) we lose the rat war, not the race for cheese: on superioristhmus our rats took
  10252 damage from 260 throws and 6570 from 159 rat-trap fires (theirs 5670 and 0). They grab ours 78-359 times a
  game to our 61-228. About 60% of their grabs take a rat that cannot see the grabber, about 35% one weaker than the
  grabber (the engine lets a strictly healthier rat grab one that sees it). In 60-70% of the grabs the grabber was
  already adjacent at the end of our rat's turn. The grabbed rats sit at 40-59 health most often (137 of 359 in
  3765 superioristhmus): a trap's 50, then the throw. averyfineline (3736/3737): our king died r129-131 to 9-11
  thrown rats (378-440 damage) and bites; 11 of theirs stood by our king at r50 to our 1.
- Starvation on tornadoes (3757 B vs outercloud): the second king formed at r200-299 on ~4.7 cheese/round; the merge
  took 7 of 24 babies, cats killed 15 more, we were down to 8 then 3, 3.9k cheese lay unpicked, and both kings
  starved by r1080. u0t = u0n (END_KING 1850) + FORM_NET100 200 (an early king only on ~8 cheese/round); diag ng1
  with control ngc (u0n), 10 maps both sides.
- New switch RISK_ADAPT (default 0): RISK_FRESH's fight-time trap avoidance, switched on only once enemy rat traps
  are known to be in play. A rat that loses health during its own turn has fired a trap (only a trap fires on our
  own move); it squeaks SQ_BURN every BURN_EVERY rounds; allies who hear it learn it, and a king that hears it sets
  Comms.SYM_BURN (512) in the SYM slot for everyone. RISK_FRESH cost t0b -11.5 pts at a screen whose opponents lay
  no traps; this version costs nothing against them. u0u = u0m + RISK_ADAPT 2; test against u0q (reactive
  trapper), with u0m vs u0q as control.

## 2026-10-30 13:21 PDT: gatherers crowd one home mine; SEEK_IDLE and IDLE_SHIFT
- wallsofparadis (3800 g2 vs CS50Crab, lost) is a 60x60 map of concentric walled rings with dirt seals; rotational
  symmetry. Our home half has four mines: (30,3) outside the rings, (27,28), (21,37) and (4,54). 70% of our
  rat-turns from r200 to r1200 stood within 4 of (30,3), and our rats first came near the other three at r434, r392
  and r503. The enemy reached their twins by r54-135, dug 17 times in the first 100 rounds to our 2, and took 12920
  cheese from our three ringed mines to our 770. The team list held one home mine for 400 rounds, so MINE_BALANCE
  sent every gatherer there. SEEK_CROWD 6 rarely fired (x 1-5% of decisions): it counts allies nearer the mine
  inside the rat's 90-degree cone only.
- Across set 2 (3796-3809, tools: diag26b/minecov.py and minesee.py), we work 1-2 of the 3-8 home mines on
  wallsofparadis, pipes, safelycontained, attack_on_cat, dirtpassageway, jail and sunandmoon. The field reaches our
  twins far sooner: pipes 47,30 at r167 to our r1164 and 30,30 at r54 to our r491; safelycontained 18,46 at r44 to
  our r454, and 27,47 we never saw. Known mines are under-worked too: pipes 29,15 was seen at r62, yet they took
  3135 there to our 702.
- New switches (default off): SEEK_IDLE (a gatherer that spends SEEK_IDLE turns within Chebyshev SEEK_IDLE_R of its
  mine without a pickup starts SEEK_CROWD's seeking, once per SEEK_RETRY), and IDLE_SHIFT (each idle spell also
  moves the gatherer to the next mine of its MINE_BALANCE list). A mine drops 20 cheese about every 12 rounds, so
  40 idle turns trip at about 3-4 gatherers to a mine. u0w = u0m + SEEK_IDLE 40; u0x = u0w + IDLE_SHIFT. Diag iw1
  (u0w vs s9s) and control iwc (u0m vs s9s) run on those eight maps on both sides; ix1 (u0x) queued.
- fg1 (u0s KING_FORAGE vs s9s) against fgc (u0m) on the starvation maps, 15 of 16 cells: 7 wins to 5, +3 -1,
  xfer 125k to 111k. where_are_you B: u0m starved (both kings) and lost, u0s won with 19k delivered; Hike A: u0s
  starved twice and lost where u0m won.

## 2026-10-30 13:39 PDT: u0m's set-3 reference (25 of 42); the field's trap war; u0q submitted
- u0m's set-3 reference block (3819-3822, 3836-3837, 3849-3856): 25 of 42. outercloud, bc26vibe and CS50Crab 6 of 6
  each; spsquared 4, AdamTan12 2 (wallsofparadis and keepout lost on both sides), AlexT101 1, abcdezyxwv 0.
- Rat traps across set 2 (diag26b/trapsum.py): every field bot but bc26vibe lays them, 30-310 a game, and 90-98% fire
  on our rats. CS50Crab constrictinglattice 3801: 310 laid, 305 fired, 126 of our 134 deaths were traps. AlexT101
  sittingducks 3802: 242 laid, 94 trap deaths, 38 more from thrown landings. Our side: 0 traps laid in every game.
  The engine fires a rat trap when an enemy rat enters any tile of the 3x3 around it, by walking, being dropped or
  landing from a throw, and a fired rat trap also starts the backstab. In the replays (trapmove.py) most fires come
  as our rat closes on an enemy from d2 4-20, or steps off an adjacent one; trap age 0-3 rounds. The '?' why codes at
  fire turns are rats the trap killed (no indicator, no tag).
- RISK_ADAPT (u0u) against u0q, 7 of 20 diag games: u0q's traps fired 377 times on u0u and 391 on u0m (-4%), deaths
  by enemy 220 vs 297, wins 2 vs 3. In u0u's local replays the fires come in every mode: attackers with the full
  150-round mask on, hunters, retreats. TrapRisk's flat mark array waits for 3*W*H + 8000 bytecodes, so on 60x60 maps
  no rat ever has marks.
- u0q (u0m + BABY_RAT_TRAPS + TRAP_ADJ + TRAP_SPAWN_FIRST) is submitted (#115), block 1 on set 3 against the draw
  above. Self-play can't judge it: s9s lays no traps, and our screen opponents barely do. The field's evidence: s9a
  (traps off alone on s7d) lost net -3 on the replica; s9s dropped traps only as part of agrab's bundle.

## 2026-10-30 14:37 PDT: u0q rejected on set 3; u0z (economy bundle) submitted
- u0q's block 1 (3867-3872, 3886-3889, 3904-3905, 3911-3912) against u0m's set-3 reference: 42 cells, 36 alike, D 6
  (+1 -5), net -4, t -1.63, so it is rejected. Losses: outercloud on dirtfulcat A and B (39 traps laid in r0-49, ~780
  cheese, while the opening economy was still running; the bank fell 592 -> 257 by r99 and the king starved at
  r493), spsquared on rose A and B (rose A: our cat damage 1139 against u0m's 4171; u0m won by killing the king at
  r1905, u0q lost 41-58 on points at r2000), AlexT101 on tiny B. Gain: tiny A.
- Laying traps doesn't pay against this field. The field's traps still cost us ~100 babies a game against the
  heavy trappers, so the open problem is not stepping into them.
- u0y (u0q + RAT_TRAP_MIN_CHEESE 500) answers only dirtfulcat. It waits on its diags (ty1/tyq/tym against arush).
- Submitted u0z (#116) = u0m + RICH2_BABIES 80/3000 (u0v: rb1 vs rbc +2 -0 at 7 cells) + END_KING 1850 and
  FORM_NET100 200 (u0t: ng1 vs ngc +5 -3) + KING_FORAGE (u0s: fg1 vs fgc +3 -1). Block 1 is on set 3, matches after
  3912.

## 2026-10-30 14:57 PDT: who the field's traps kill, and WOUNDED_HP
- Set 2 plus u0q's set-3 block (24 replica matches, ~70 games): 2689 enemy rat-trap fires on our babies, ~38 a game,
  and ~1540 trap deaths, ~22 a game. Of the 854 fires on a rat at <= 50 health, 806 killed it. Of the 1794 on a
  healthier rat, 721 of the victims died to a later trap within 20 rounds; 240 victim-rounds had two or more traps
  fire at once.
- Where (traphp.py, trapctx.py): AlexT101 663 fires in our half to 158 in theirs, CS50Crab 402/147, abcdezyxwv
  170/2, spsquared 260/139, AdamTan12 198/162, outercloud 2/79. The field invades and lays traps as it comes. Our
  gatherers and defenders walk into them, and so do our carriers stepping to throw ('T' in local games). A replica tag of
  '-' at a fire is a rat that died that turn: its bytecode count is 0.
- RISK_ADAPT (u0u) is done: ad1 vs adc 9 wins to 11 at 20 cells, and u0q's traps fired on it 1250 times against 1129 on u0m.
  The fresh-marks mask doesn't keep rats off reactive traps.
- New switch WOUNDED_HP, off by default. Once enemy traps are known (the burn signal, now Cfg.BURN, shared with
  RISK_ADAPT), a baby at war with <= WOUNDED_HP health never steps within Chebyshev 2 of a visible enemy baby (the
  reach of a trap laid beside it); it still bites one that comes adjacent. u1b = u0m + WOUNDED_HP 50, diag wd1 vs
  u0q on adc's 20 cells. u1c = u1a + WOUNDED_HP 50.
- Built u1a = u0z + SEEK_IDLE 40 + IDLE_SHIFT. ix1 (u0x) vs iwc (u0m) stands at 8 wins to 4 at 11 cells, delivered
  +14%. u0z's set-3 block stands at 30 cells, 29 alike, net -1 so far (lost dirtfulcat A vs outercloud).

## 2026-10-30 15:03 PDT: u0z ties u0m on set 3; u1a submitted
- u0z's block 1 (3926-3931, 3934-3939, 3943-3944) vs u0m's set-3 reference: 42 cells, 40 alike, D 2 (+1 -1), net 0,
  t 0.00, so it is rejected (block 1 rejects at t <= 0). Gained spsquared rift A, lost outercloud dirtfulcat A (u0m
  won it at r665 by kings; u0z lost at r1757). Against this panel the economy bundle (RICH2 cap, END_KING, FORM_NET100,
  KING_FORAGE) moves almost nothing.
- Over set 2, set 3 and the u0q/u0z blocks (156 games): 73 wins and 60 losses by king kills, 14-5 at the round limit,
  2-2 by cats. 54 of the 60 king losses come from three teams. AlexT101 kills by r184-452 (21 of 24 games lost),
  abcdezyxwv by r227-407 (22 of 24), AdamTan12 by r609-1283 (11 of 18). Our king takes 400-600 damage from bites and thrown rats
  (kingdmg.py, 3934/3936): throwHit 84-410 and bite 118-400 a game, from 4-13 different biters.
- Engine limits that shape kings: at most 5 kings, and after round 1200 a team with 2 or more can't form
  another. At war the points are 0.5 king share, 0.3 cat-damage share and 0.2 cheese-transferred share.
- Submitted u1a (#117) = u0z + SEEK_IDLE 40 + IDLE_SHIFT. Diags: ix1 (u0x) vs iwc 11 wins to 8 at 16 cells,
  delivered +11%; iw1 (u0w) 9 to 7 at 15, +18%. Block 1 is on set 3, matches after 3945.

## 2026-10-30 16:08 PDT: u1a rejected; the Stash pocket; who grabs whom; GRAB_MICRO
- u1a (#117) block 1 on set 3 (3951-3952, 3956-3957, 3972-3973, 3986-3993) vs u0m's reference: 42 cells, 37
  alike, D 5 (+0 -5), net -5, t -2.24, rejected. It lost where_are_you A and B (AdamTan12), rose A and B
  (spsquared) and dirtfulcat B (outercloud). u0z, the same bundle without SEEK_IDLE + IDLE_SHIFT, tied u0m on this
  set, so the mine-coverage pair is what lost. Its screen ra_u0w (u0w = SEEK_IDLE alone) passed weakly: t -0.59,
  +5 -10.
- u1b (WOUNDED_HP 50) and u1e (WOUNDED_HP + AMMO_AWAY) are both weak against u0q: 8 wins to the control's 11 on
  adc's 20 cells (wd1, wa1). Neither goes to the replica.
- Stash pocket (local stz1, u1a vs araid): our king starts in a walled pocket (wall row y=22, dirt at both ends,
  serpentine dirt stripes). None of our rats got out before r53; 12 attackers were stuck r10-100, and 40-50% of
  rat-turns were in flow 'building'. One attacker's trace: r8-14 it heads for (15,26), the VER mirror of our own
  king, a symmetry that is impossible because the twin kings would overlap (SYM_KING_GAP rules it out at round 1).
  Then its target flips between the ROT and HOR spots, and each flip resets the stall (RUSH_STICK fixes that). Its
  reverse searches take 17 turns while the rat wanders, and finished paths are dropped after FLOW_HOLD 3 turns when
  the next tile is held by a robot (the pocket is crowded). u1f = u0m + SYM_KING_GAP: diag kg1 vs kgc on the 8
  gapAlt maps. Halfway: Stash won faster (706 and 891 rounds against 826 and 1047), FreeTheCheese B lost by
  starvation.
- Flow search cost (popbench, a Stash-like harness): one 30-tile path costs 640-890 pops in reverse against ~250
  forward when the rat is in a pocket. LIFO buckets and weighted A* (w 2, 3) don't help the reverse search. In
  game, the fast reverse loop pays ~245 bytecodes a pop to the plain forward loop's ~621, so a forward search for
  unknown targets would only pay if it were unrolled too.
- The field's rat war (3934-3937 vs AlexT101 and abcdezyxwv; throwsum.py, kingdmg.py). Thrown rats do 40-70% of
  the damage to our king (throwHit 232-430 a game). On tiny vs AlexT101 our king spawns every turn and first bites
  at r34, by when it has taken 31 bites and throws to the 4 bites it gave. We lose 1.3-2.3x more rats, mostly to
  throwLand and throwHit, then traps (2-35 a game ours, 0 theirs). They grab more (116-186 to our 93-148) and throw
  a larger share (78% to 63%).
- grabopp.py over the 12 games: per our rat-turn at war near enemies, it checks whether a grab was open and whether
  an enemy could step next to us into our blind spot (or anywhere next to us, when healthier). End-of-turn
  exposure 0, 1, 2, 3+ was grabbed before the next turn 0.0-0.4%, 5-20%, 8-32% and 14-65% of the time. We left
  most grabs unused: per game 33-305 open where we stood (24-74 taken) and 33-320 one step away (8-45 taken). A
  grab needs the grabber to face its victim (the engine checks canSenseLocation), so our tryCarry, which never
  turns, misses every grab that needs a turn.
- New switch GRAB_MICRO, off by default (GrabMicro.java, 7x7 bitboards). In the war, not carrying, before the
  pre-move bite: score each end tile (stay or one step) and facing by a grab there (100 + victim hp/10), a bite
  (GRAB_BITE 20, +15 when it ends the victim's health edge, +40 a kill), and -GRAB_THREAT 25 per enemy that can step
  next to us into our blind spot (anywhere next to us when healthier). Walls, dirt and robots block the enemy's
  step. Act (move, turn, then grab or bite) when the best beats standing by GRAB_MARGIN 10. Otherwise put steps worse
  than standing by 10 into Nav.avoid. Ties are broken by a mirror-invariant key. BotTest has 11 cases plus 9000
  mirrored and rotated twins (all pick the mirrored option). bccmp: only new members with the switch off.
  u1g = u0m + GRAB_MICRO: diag gm1 vs agrab, agaim, abrk on tiny, constrictinglattice, starvation, Excavation,
  knifefight and Meow, both sides, control gmc (u0m).

## 2026-10-30 16:47 PDT: GRAB_MICRO cost; THROW_CHECK

- gm1 (u1g, the first GrabMicro): 58 turns over 17500 in one tiny game. Its bitboard loops ran 9 tiles x 8 facings x
  every enemy x 3 passes. u1h scores with tables instead: per end tile, each enemy that can reach a tile next to it
  gets an 8-bit mask of the facings that leave it a threat (SAFE[ring], NEAR, RING9, DIRK string literals: ~1
  bytecode a lookup, no static initializer). BotTest keeps the old bitboard scorer as a reference and checks 4000
  random cases against it, plus the tables against their definitions. u1i: the micro only runs with an enemy baby
  within Chebyshev 2 (no grab is possible otherwise and standing is safe) and GRAB_BC 10000.
- Diag gh1 (u1h) vs gmc (u0m), 36 cells (agrab, agaim, abrk x tiny, constrictinglattice, starvation, Excavation,
  knifefight, Meow, both sides): 23 wins to 22, but 652 overruns to 4. Note: pairdiag.py keys cells by map and side
  only, so it folded the three opponents into one; oppdiag.py (scratch, also ~/diag26b/) scores per opponent.
- Probe pb2 (u1i with Clock checkpoints in the indicator string, set again right after the micro so overrun turns
  keep it) vs agrab on tiny. Evaluated turns cost a median 4388-4735 bytecodes, but evaluate itself only ~1700:
  the rest was setup. The robot scan kept a sorted list by shifting 8 arrays, and the passable-tile mask called
  G.onMap and bit() for each of 25 tiles (~60 bytecodes a tile).
- u1j: enemies are collected unsorted (the nearest 6 by Chebyshev and key are selected only when there are more;
  enemy order doesn't change the scores), the mask is read row by row from Sym.mem, each adjacent enemy updates only
  the 3 facings that see it, and an end tile with nothing to grab or bite takes its facing straight from bit-sliced
  threat counts. Probe: evaluated turns a median 3703 (p90 5248, max 6678), acting turns a median 4749 (max 7104),
  no overruns. Diag gj1, same cells.
- Own-hit throws over 27 field games: our throws hit 0-17 of our robots a game, up to 370 damage (keepout 3992: 12
  rats), and 0-4 times our own king (3934 constrictinglattice: 7 rats + 3 king hits, 334 damage). New switch
  THROW_CHECK (Baby.throwChecked), off by default. Our kings out of sight (from Comms) block flights, a hit on our
  king scores 3x a rat's, the flight tiles are checked for our robots before the throw, and a carrier with only bad
  flights holds until THROW_LATE 8 rounds into the 10-round carry. bccmp: only new members with both switches off.
  u1k = u0m + THROW_CHECK: diag tk1, same cells.


## 2026-10-30 17:24 PDT: u0o rejected; u1j and u1k diags; trap war in the field; u0y to the replica

- gj1 (u1j, the cheaper GrabMicro) vs gmc (u0m), 36 cells: 19 wins to 22 (abrk 9-10, agaim 4-6, agrab 6-6),
  overruns 15 to 4. GRAB_MICRO is not a local gain at either cost: weak, not submitted.
- tk1 (u1k = u0m + THROW_CHECK), same cells: 23 wins to 22 (abrk 11-10, agaim 6-6, agrab 6-6; +3 -2), overruns 3
  to 4. Neutral; a replica candidate.
- King defence check (field replays): once carried enemies are left out (their turns rows hold stale positions,
  carrier != 0), our king bites or spawns on nearly every turn it has a target. Not a problem.
- Field economy, u0m's set-3 losses: the strong bots pick much of our half's cheese (keepout vs AdamTan12 6860 to our
  3930 there; Stash vs abcdezyxwv 1960-2480 to our 260-300). When they took a pile on our half, our nearest rat was
  usually 9+ tiles away. On keepout the king knew 5 home mines by r138 locally (kmine1 vs agrab) but in the field
  worked 2 under war pressure; on wallsofparadis it knew only one until r324. Kings sat at the 18-baby cap r100-350
  while the bank fell 1620 to 374, paying for replacements.
- Deaths by cause (dumps of u0m's and u0q's set-3 blocks, 6 games per opponent):
  - AlexT101: we lose 312 rats (throwLand 143, trap 110) to their 236.
  - abcdezyxwv: 327 (throwLand 186, throwHit 59) to 304.
  - AdamTan12: 269 (trap 148) to 741 (cats kill most of theirs).
  - Their traps fire on us ~92% of the time, almost all on our own moves (being thrown onto one is rare). The
    victims' smuggled codes are fight moves (a attack, f face, w war, k flank, T carry aim), or 0, which is a rat
    that died on the trap.
  - u0q, with reactive baby traps, killed 76, 80 and 63 of theirs with traps. That cut our deaths to 262, 257 and
    253, so K/D vs AlexT101 rose from 0.76 to 0.98 and vs abcdezyxwv from 0.93 to 1.08. It still lost those games,
    and it lost outercloud dirtfulcat A/B to its r0-49 trap spending.
- u0o (u0m + RUSH_TO 12, #118), set-3 block 1 4164-4177: 18 of 42 to u0m's 25. Paired: 33 alike, D 9 (+1 -8),
  net -7, t -2.33. Rejected. It lost dirtfulcat A/B, where_are_you A/B, rose A/B, rift B and tiny B.
- Next: u0y (#119) = u0q + RAT_TRAP_MIN_CHEESE 500. That is the reactive trap line with a 500 bank floor, which
  stops the dirtfulcat spend (ty1 vs arush: 14 wins to 12). Set-3 block 1 from 4178. u1k follows.

## 2026-10-30 18:05 PDT: FACE_HOLD and the zone variant fail; how the field kills us; u1p, u1q

- FACE_HOLD (new switch, off): once enemy traps are known, faceEnemy no longer steps from Chebyshev 2 into contact
  with an enemy baby that sees us and is not weaker. It holds, facing it. Diags vs u0q on adc's 20 cells (control adc =
  u0m, 11 wins; fyc = u0y, 11 wins):
  - u1l (u0m + FACE_HOLD): 8 wins. Fires on us 973 to 919, deaths 626 to 654, kills 404 to 425. Weak: holding gives
    the trapper time to lay more traps around the rat.
  - u1m (u0y + FACE_HOLD): 10 wins to u0y's 11. Fires on us 699 to 808, deaths 514 to 620, xfer 220k to 187k.
    Parked: fewer losses and more income, but no more wins.
  - u1n (u0m + WOUNDED_HP 100, every rat out of Chebyshev 2 of enemy babies): 6 wins, kills 266 to 425. Rejected.
- How the field kills us (u0m's set-3 replays):
  - After an AlexT101 trap fires on one of our rats, 82% of victims die within 15 rounds. At <= 50 HP the next trap
    kills (86 of 97). Rats that survive the first fire die mostly to a throw (68) or a second fire (57) within 30
    rounds. Their next moves are fight codes (a, w, T, k, f).
  - The losses to AlexT101 and abcdezyxwv come after r100. At r100 we have more rats (16-20 to 9-16). By r250 we
    have 5-8 to their 9-16, our bank is 0-160 to their 500-1200, and they kill our king r200-450 with bites and
    thrown rats.
  - The war is on our half. constrictinglattice vs AlexT101 (3849): 53 of our deaths on our half against 12 of theirs
    on their own. They picked 930 cheese on our half, we 200 on theirs. 72 of our 112 deaths were at the two front
    mines, mostly the home one at (7,32).
- u0y's outercloud losses (4178, Excavation A and dirtfulcat A; u0m won both): 31 traps by r100 on Excavation and
  40 on dirtfulcat (26 dirtfulcat hits by r50). They took the bank from 1810 to 515 and 1660 to 280. The king then
  spawned 0-6 per 25 rounds, and we had 1-10 rats at r250. The 500 floor doesn't bind while the opening bank is high.
- New off-by-default switch TRAP_OPEN_TO / TRAP_OPEN_FLOOR: before round TRAP_OPEN_TO a baby trap needs
  TRAP_OPEN_FLOOR banked (bodies first). Bytecode check vs s9s: only new members. BotTest 490/490, full suite exit 0.
- New snapshots:
  - u1o = u0m + RUSH_TO 20. AdamTan12 delivered 19-20k on where_are_you in u0o's games.
  - u1p = u0m + MINE_TRAPS 8. Gatherers trap the eight 3x3 trigger blocks of their mine's spawn square, to defend
    the half the war is fought on. Diag mt1 vs u0q running.
  - u1q = u0y + TRAP_OPEN_TO 150 (floor 1500), the fix for u0y's opening spend if u0y gains vs the top three.

## 2026-10-30 19:28 PDT: u0y and u1p rejected; the Stash pocket (BUG_DIG); u1o on the replica

- u0y (#119, reactive traps + 500 floor), set-3 block 1 (4178-4191): 42 cells, 36 alike, D 6 (+1 -5), net -4,
  t -1.63. Rejected. It flipped no game vs abcdezyxwv or AdamTan12, and its opening traps lost outercloud
  Excavation A and dirtfulcat A.
- u1p (#120, MINE_TRAPS 8), set-3 block 1 (4192-4203, 4248-4249): D 4 (+1 -3), net -2, t -1.00. Rejected. The
  mine traps hit well in the field (26 of 30 vs AlexT101, 21 of 26 vs abcdezyxwv over 6 games each) but were too few
  to matter. The 400 bank floor and the front-tile rule keep the volume low.
- u1q (u0y + TRAP_OPEN_TO 150): oq1 vs arush 14 to 14, traps 295 to 694, xfer +11%. Not submitted: the trap line it
  fixes didn't gain vs the top three.
- Mobility vs the field (mobility.py over the 91 replica replays on the VM: the round the 5th rat stands 10+ tiles
  from its king, ours minus theirs). We are as fast or faster on nearly every map. The exceptions are Stash
  (+44..+54 rounds), pipes (+7..+32) and jail (0..+19).
- Stash: vs abcdezyxwv only one of our rats ever leaves the walled pocket. They have 12 out by r40, digging from r11,
  and deliver 3.7-4.7k to our 0-140. Locally vs araid (u0m) the first rat is out at r53 and 11 by r100.
  - Our 12 attackers aim at the VER mirror of our own king for 60 rounds. SYM_KING_GAP (u1f) rules that out; then
    they aim at the real spots.
  - The bug walk treats dirt as wall, so the pocket has no way out without digging. Each rat waits 20-26 turns for
    its own reverse flow search (ft=15,3b0/21).
  - New switch BUG_DIG (off): the bug walk's sweep digs the first dirt tile it meets before any free step.
    u1r = u0m + BUG_DIG dug the exit open by r22 (u0m r64), first rat out r20, xfer 10.0k to 7.0k. The 1-wide
    corridor then jams: 1/2/3 rats out at r40/60/100.
  - Screen ra_u1r: t -0.02, net -4 (+7 -11), pass; dirt dug +24%.
  - u1s = u1r + s7x's corridor swap: 3/7/11 out locally, but rejected at the screen (t -1.16, net -10, sign
    p 0.02), as the swap was in s8c and s9l.
- u1o (#121) = u0m + RUSH_TO 20 is on set-3 block 1, 4261-4274. u1r (basics repair; the basics rule accepts at
  t >= 0 after block 2) is next, then u1k.

## 2026-10-30 20:01 PDT: How the top three win (traps, our own rats thrown into our king, a trap line against the rush); u1t-u1w

- u1o (#121, RUSH_TO 20) was rejected on set 3: net -3, t -1.13. u1r (#122, BUG_DIG) is now on its set-3 block.
- New VM scripts (copied to ~/diag26b/): gsum.py (per game: deaths, traps laid and fired, both sides), kingctx.py
  (army positions while our king loses HP), reach.py (each side's rats near the other king), rushers.py (our first
  24 rats: fate, closest approach to their king, move codes).
- Traps: in u1p's 12 games vs AlexT101/abcdezyxwv their traps hit us 485 times and ours hit them 47. Both kill 83%
  of the rats they hit within 15 rounds (trap, then grabbed and thrown).
  - u0y (reactive traps) closed most of the gap over 18 games vs the top three: 735 hits on us to 486 on them.
    Outside where_are_you it killed more rats than it lost (893 to 743), yet lost the same games.
  - The engine's trap stun only sets the movement cooldown (stunTime 30). A trapped rat can still bite and turn.
- We lose by king kill, not on rats. Rat deaths are near even (675 to 575 over u1p's 12 games), and neither side
  forms a second king vs AlexT101/abcdezyxwv.
  - Our king loses ~350 hp in 25 rounds (4184: 486 -> 142 in r175-200; 4186: 506 -> 146).
  - The damage is 8-11 different biters plus our own rats, grabbed next to the king and thrown into it (38-42 per
    hit, 3-14 hits a game).
  - In 4184 one enemy (10260) threw our full-health rat 10490 into our king twice in 5 rounds. It was grabbed at
    equal hp from outside its 90-degree cone.
  - Their king is never touched. Our rats within 8 of their king: 0-2 at a time. Theirs within 8 of ours: 4-9.
- Economy (4198 constrictinglattice vs AlexT101, per 100 rounds):
  - They pick their own half 500 -> 1160 unopposed and take 130-500 of ours. We take 0-60 of theirs.
  - Deliveries: theirs rise 378 -> 1170, ours fall 693 -> 160. Over the game they took 26% of the cheese spawned on
    our half.
- The rush feeds a trap line. AlexT101 lays reactive traps on the contact line (y 26-31) from r12: 45 fires on us by
  r60, mostly one round after the lay. All 12 of our rushers were dead by r57, none nearer than 4 to their king. Of
  our first 24 rats, 21 died by r59.
  - First enemy trap fire on us in u0m's reference games: AlexT101 r10-26, CS50Crab r7-34, bc26vibe r17-76,
    outercloud r33-183, abcdezyxwv r47-137, AdamTan12 r19-52 (none on where_are_you), spsquared none before r824.
  - The rush wins thunderdome (kings 8 apart) vs CS50Crab by r24, and dirtfulcat, rose, rift and where_are_you
    (u0o's 8 rushers lost those).
- Candidates:
  - u1t: u0m + MINE_TRAPS 8 at a 40-cheese floor, cap 20. u1p's 400 floor held mine traps to ~5 a game, but they hit
    26 of 30 vs AlexT101.
  - u1u: u1m + TRAP_OPEN_TO 150. u1m vs u0q: fires on us 699 to 808, deaths 514 to 620, income +18%; u0y's opening
    trap drain is fixed.
  - u1v: u0m + AMMO_AWAY. It passed two screens on older bases and has never been on the replica.
  - u1w: u0m + RUSH_BURN 6 (new switch). Once enemy traps are known, a rusher more than Chebyshev 6 from the enemy
    start turns gatherer.
  - Screens ra_u1t/u1u/u1v vs araid and diag rsb2 (u1w vs u0q) are running.

## 2026-10-30 21:15 PDT: u1r rejected; u0m's set-3 draw was lucky; the economy gap and the king's walk (KING_PATH, u1x)

- u1r (BUG_DIG) rejected on the replica (#122): set-3 block vs u0m's 3819-3822,3836-3837,3849-3856: 42 cells, D 7
  (+1 -6), net -5, t -1.89.
- u0m's own set-3 draw was lucky. New research/panel/v3-set3-pool.txt pools the eight set-3 candidate blocks since
  u0m. Scored against the pool, each wins 18-23 of 42 (expectation ~21.7): u0q 21, u1a 20, u0o 18, u0y 21, u1p 23,
  u1o 22, u1r 20. u0z wins 25, but it played 40 of 42 games exactly like u0m. So u0m's draw sits ~3 wins above its
  pooled rate, and all eight lost the same cells (outercloud dirtfulcat, spsquared rose and rift B, AdamTan12
  where_are_you). Score set-3 blocks with `contest.py pool 3819-3822,3836-3837,3849-3856
  research/panel/v3-set3-pool.txt CAND` as well as `paired`.
- Screens: u1t (mine traps at a 40 floor) net -7, t -1.30; u1u net -7, t -0.40; u1v (AMMO_AWAY) net -5, t -1.55,
  which the screen rejects. u1v was already on the replica (#123, set 3 from 4369) before its screen finished. The
  block runs on for field data. u1w (RUSH_BURN 6) went 8 wins to 11 vs u0q locally. None of the four is promising.
- The 24 games of u1r's block, cheese ledger and time use (new scratch tools ledg.py, timeuse.py, trips.py):
  - We lay 0 rat traps. The top four lay 37-156 a game.
  - Deliveries in our losses: theirs 2-20x ours (keepout 9705 to 2419, wallsofparadis 7281 to 1490, Stash 4410
    to 200).
  - Our bank runs dry by r300-400 in most losses. Three of the 12 losses to AlexT101/abcdezyxwv ended with the king
    starving.
  - keepout (AdamTan12): they picked 5660 of the cheese spawned on our half, we picked 1820.
  - wallsofparadis: our ~20 gatherers spend 35-44% of turns walking to a mine and 26-47% delivering. Our king sat
    at (3,2), 26 from the nearest mine. AdamTan12's king walked to the middle of the map, 4-5 from its mines.
  - rift (spsquared): their first king walked onto mine (47,3), and later kings formed on (48,24) and (45,15).
    Their kings picked up 2680-3320 cheese directly, ours 60-100.
  - Why our king stops short: King.pickHomeMine takes the first listed mine within dist2 400. On rift that was
    (6,12), 13 away, not (12,26), 8 away. The king walks there greedily and stopped under a wall row at (6,19) for
    2000 rounds.
- Map census of king paths (scratch KingPath.java, 3x3 BFS, walls blocking), over 74 maps:
  - On 11 maps a path reaches a home mine but the greedy walk gets stuck or picks one it can't reach: TheHeist,
    EscapeTheNight, RUN, clouds, knifefight, krotus, dirtpassageway, houseofcards, ratsareants, diamond, heart.
  - On 4 more maps no home mine lies within dist2 400: wallsofparadis 26 steps, whatsthecatdoin 22,
    ultimatestarvation 15, Nofreecheese 29.
- New switch KING_PATH (new class KPath), a basics fix to the king's movement:
  - A reverse search over the king's 3x3 body runs from every seat at once: a centre within Chebyshev 1 of a home
    mine that no other king holds. Seen walls block, unseen tiles and dirt count as open.
  - The search uses spare bytecode at the end of the king's turn (2500 kept in reserve). The king then steps
    downhill to the mine nearest by path.
  - It searches again when a new mine is listed or a newly seen wall blocks the way, and holds still while a
    search runs.
  - It never takes a path over 32 steps. With no path, the old greedy walk runs as before.
  - bccmp: the default build is unchanged (new members and the new class only). BotTest 508/508 (testKingPath).
  - Snapshot u1x = u0m + KING_PATH. Diag kpa (u1x vs u0m head-to-head, those 16 maps both sides) and screen ra_u1x
    are running on the VM.

## 2026-10-30 21:34 PDT: KING_PATH's first numbers and its overruns; u1y

- Diag kpa (u1x vs u0m head-to-head), first 15 of 32 games: 9 wins to 6.
  - Kings picked up 11200 cheese to 560. wallsofparadis: delivered 22291 to 6498 (A) and 17096 to 10699 (B).
    rift A: our king picked up 2560 to 100.
  - u1x overran in 8 of 15 games (krotus B 24 times, EscapeTheNight A 6). The bytecode charge for an array lands
    at once ("WithoutInterrupt" in the instrumenter), and KPath.tick() went on to scan the walls and seed the
    search on the same turn with no guard. EscapeTheNight B ran over at r4, the turn of its last allocation.
- u1x's EscapeTheNight B loss (r615, king starved) is not the path:
  - Neither king found a home mine within reach (all mines but four sit on the axis), so both stayed home.
  - Both armies went to war. By r300 u1x's carriers held 1395 cheese and stood next to the enemy king attacking
    (move codes f and a), while u1x's bank ran down to 0. u0m's carriers held 1060 and delivered 830.
  - Field check (u1r's 24 games): our bank goes under 60 only at the end of lost wars, with no cheese left in our
    rats' paws. The exception is where_are_you (3470 held, a win). So HUNGRY (deliver when broke), tried as s3k,
    would not change the field losses.
- KPath fix: a turn that allocates does nothing else; the wall scan stops before a row with fewer than KP_RESERVE
  bytecodes left and scans again next turn; seeding gives up the same way and is due again. BotTest 511/511 (3 new
  checks), full suite exit 0, bccmp unchanged outside KPath.
- Snapshot u1y = u0m + KING_PATH with the fix. kpb (u1y on u1x's overrun cells vs u0m) is running. u1y is the next
  replica candidate after u1v's block.
- Engine facts for later: mine pairs spawn together (20 each, mirrored spots in the 9x9 square). The chance per
  check is 1 - 0.99^(rounds since the last spawn), and both mines of a pair check every round, so a pair spawns
  about every 9 rounds. A thrown rat that lands takes 10, then any enemy trap zone it lands in fires (50).
  becomeRatKing kills every robot in the 3x3, enemies too, and the king's HP is the sum of our robots' HP there
  (cap 600).

## 2026-10-30 22:13 PDT: u1y rejected; the moat (u2b)

- u1y (u0m + KING_PATH, bytecode-guarded) on the replica, set-3 block 1 (4420-4425, 4439-4446): 23 of 42.
  - Paired vs u0m: 38 of 42 alike, net -2, t -1.00 (gained spsquared TheHeist B; lost AdamTan12 where_are_you A,
    outercloud dirtfulcat B, spsquared rose B). Pool z +0.64. Rejected (same rule as u1v).
  - AdamTan12 wallsofparadis is still lost on both sides. Self-play said KING_PATH more than tripled our deliveries
    there; against AdamTan12 the king never got there alive.
  - Self-play notes: kpa 22-10 vs u0m; kpc (u1z, KP_FRONT 50) 19-13, so the front limit is parked. krotus is a
    starvation map (both banks near 0 by r280), not a front-line death. Screen ra_u1x: -1.7 pts, t -0.42, net -6.
- What kills our king against AlexT101 and abcdezyxwv (r1v, 12 games, 11 king kills at r49-714):
  - Throws into our king: 84-480 damage a game, 42-46 per hit (point blank: thrower 1-2 tiles off). 73 of the 81
    hits were their rats throwing OUR rats into the king. Bites: 30-316 a game.
  - An enemy rat first reached Chebyshev 3 of our king at r7-145 (backstab r9-62). Our dirt stock then: 3-17 on
    constrictinglattice, starvation, Stash and tiny, 73-138 on Excavation. 11-16 of the 16 ring tiles are open.
  - The grab war has been fought with micro many times (BITE_GUARD, RESCUE, GLANCE_MEM, AMMO_AWAY, GRAB_MICRO,
    THROW_CHECK, FACE_HOLD): all neutral or worse on the replica.
- Big swing: MOAT (Moat.java, off by default).
  - Engine: a bite needs dist2 2 to a king tile; a thrown rat stops at the first impassable tile, and dirt is
    impassable; placing dirt costs no cheese, one stock dirt and 25 action cooldown. A king can place dirt anywhere
    in its ring (dist2 8). Transfers reach dist2 9 of any king tile, so every ring-3 tile can still deliver.
  - At war, a king that sees an enemy rat fills its Chebyshev-2 ring, one tile per action, nearest the enemy
    first. Babies beside the ring help while the king's war alert (Comms.ALERT) is under 60 rounds old.
  - One spawn port stays open: the ring tile farthest from the map centre that is not a wall (mirror-invariant).
    A baby fills only tiles ranked below the best ring tile it knows is open, so it never fills the king's port,
    even before it has seen the whole ring.
  - The king holds still while the moat is up (until 60 rounds pass without a sighting; it still dodges cats). No
    baby digs the alerting king's ring at war (guards in Nav, Flow and BitFlow).
  - bccmp vs s9s: new members and the new class only. BotTest 523/523 (testMoat), full suite exit 0.
- Snapshots: u2a (on u1y, dropped with its base) and u2b = u0m + MOAT. Diag mo2 (u2b on gmc's 36 war cells: agrab,
  agaim, abrk on tiny, constrictinglattice, starvation, Excavation, Meow, knifefight; control = gmc's u0m games)
  and screen ra_u2b are running.

## 2026-10-30 23:14 PDT: u2b rejected; u2g (rich kings) on the replica; why AlexT101 wins
- u2b (u0m + MOAT) on the replica, set-3 block 1 (4461-4464, 4478-4479, 4493-4498, 4506-4507): 23 of 42.
  - Paired vs u0m: 36 of 42 alike, net -2, t -0.82 (gained AlexT101 tiny A, spsquared rift A; lost AdamTan12
    where_are_you A, AlexT101 tiny B, spsquared rose A/B). Pool z +0.59. Rejected.
  - Against abcdezyxwv (0-6, as u0m) the moat moved the king damage from throws to bites, and the king starved
    (bank 0 by r250-500; Stash transfers 0-200 to their 3.5-5.2k).
- The hard cells: over the 22 pool games (u0m and ten candidates, both sides), AlexT101 constrictinglattice and
  starvation and abcdezyxwv Stash and constrictinglattice were never won; Excavation once. Tweaks do not reach them.
- AlexT101 (4493/4494, scratch trapsites.py, trapchain.py, kd.py, minepick.py on the VM):
  - Our kings die of starvation at r285-384, after only 200-260 combat damage. The bank goes 1230 (r40) -> 300
    (r120-200) -> 0, while theirs holds ~600 all game.
  - They lay 46-91 rat traps a game. Every one fires on us, ~75% within 3 rounds of the lay, mostly on fight moves
    (attack, war, face, flank, carry aim). 191 of 322 fires killed outright: traps sit in pairs, and one step fires
    both (the 3x3 trigger zones overlap), so 100 damage on a 100-health rat.
  - Where: their side of the midline in r0-50 (0.35-0.47 of the way from their start to ours; first lays at r12 on
    constrictinglattice), then our half (0.55-0.9) in r50-150. Our 12 rushers walk into it, and later rush
    replacements keep doing so (u0m has RUSH_UNTIL 0: 23% of our baby turns in r150-300 were still attack moves).
  - Economy: on constrictinglattice they picked 3790 in seven mines (three of them ours: 480 + 520 + 70); we picked
    1275 in three. In r80-280 they delivered 1580 to our 530 (2.65 a round, barely above the king's 2).
- u2g on the replica (#126): u0m + END_KING 1850 + FORM_RICH_BANK 3000 + FORM_RICH_MAX 4 (King.rich(): before r1150,
  while the bank holds 3000, the first king names a site past the formation cap, with no income test and a ring site
  when pickFormSite finds none). Targets: rift vs spsquared (we bank 6-13k on one or two kings and lose on points
  2 kings to 3), rose and where_are_you (one king each at r1850).
  - rk2 vs rk1 (abrk, rift/rose both sides): rift kings at the end 4-1 and 3-1 (u0m 2-1 and 1-2); the four games keep
    their winners. On rose both A sides starve (abrk at r790, u0m/u2g at r1630): a side effect worth a look.
- Snapshots this hour: u2h = u0m + FLOW_LIFO (equal-f ties expand deepest first; FIFO fans out over the whole
  optimal-cost diamond on open ground, ~800 tiles on a 40-tile errand). Diag lf1 = pk1's u0m cells.

## 2026-10-30 23:58 PDT: u2g passes set-3 block 1; FLOW_W; u2j mirrors until first contact
- u2g (#126, rich kings) set-3 block 1 (4516-4521, 4526-4529, 4537-4538, 4545-4546): 26 of 42 (u0m 25).
  - Paired vs u0m: 41 of 42 alike, D 1, net +1, t +1.00. The gain is spsquared rift A, a cell FORM_RICH targeted.
    Pool 26 vs 22.0 expected, z +2.12. Over the t <= 0 reject line, so block 2 (set 4) is queued from 4547.
  - Unchanged losses: abcdezyxwv 0 of 6, AlexT101 1 (tiny B), AdamTan12 keepout and wallsofparadis.
- AlexT101 constrictinglattice (4526 g0, scratch kd.py/minepick.py/visits.py on the VM): we lost 34 rats in r0-100
  (20 to traps, 12 thrown) to their 20. The bank went 2500 -> 310 by r75, mostly on replacements. From r175 we held
  8 babies at the 300 floor. Below 8 the floor drops to 100, and by r450 we had 0 rats and a starving king.
  - They delivered 2753 by r450, we delivered 1870. abcdezyxwv (4528 g2): 2975 to our 1537 by r350, with 16 babies
    and a 1.2k bank against our 8 and 300.
  - Mines: they work all four of theirs (~800 each vs abcdezyxwv) plus 140-470 from two of ours. Our far home mine
    (18,54) got 40 and 3 rat-turns.
  - Trap volume does not explain the loss on its own. CS50Crab kills as many of our rats with traps in r0-100
    (18-21 on 5t4rv4t10n), yet we beat CS50Crab 6 of 6: our rush kills their king, while AlexT101's king holds.
- FLOW_W (new switch, off): a weighted reverse search, f = g + w*h with f clamped up to the bucket being popped.
  Each entry carries its push-time g, so the stale test survives the clamp.
  - It targets the Stash pocket exit, where the first explorer searched r9-r26 without finishing.
  - Offline replay of the search on Stash's terrain, unseen = open: 419-606 pops at w 1, 325-457 at w 2, same path
    costs. BotTest wall-row pocket: 365 to 333 pops, the same 23-tile path, and the mirrored rat finds the mirrored
    path.
  - Forward search from the rat would pop 192-276 here, and bidirectional 200-300. Exiting a pocket favours searching
    from the rat; delivering into one favours the reverse search. A modest gain: kept off, no candidate yet.
  - bccmp vs s9s: new members only. BotTest 530/530, unit suite exit 0.
- u2j (u0m + SYM_FLOW/CENTRE/HAND/FACE/TIE + FLOW_QUOTA 18) mirror self-play (sym3; u0m control sym4), round of the
  first A/B divergence:
  - Stash 73 vs 2, where_are_you 27 vs 2, cheesebottles 112 vs 38, knifefight 4 vs 1, superioristhmus 33 vs 28,
    DefaultMedium 41 vs 41, Meow 18 vs 18. EscapeTheNight stays mirrored for its whole 1007 rounds (u0m splits at 23).
  - u2j's splits come at first contact (team A moves first within a round, so twins see different boards) or when a
    cat hits one twin. u0m's early splits are the king's facing at r0-r1.
  - Diag sy1 (u2j and u0m vs abrk, both sides, on the side-sensitive cells' maps) is running.

## 2026-10-31 00:35 PDT: u2g to block 3; rushers stuck on Stash and averystrangespace; FLOW_SPLICE, RUSH_BAN, u2m
- u2g (#126) set-4 block 2 (4553-4558, 4569-4574, 4576-4577): paired vs u0m 41 of 42 alike, net +1 (gained bc26vibe
  averystrangespace B), t +1.00; pool 23 of 42 vs 23.2 (z -0.09; lost outercloud tornadoes B, won in the pool 9 of 10).
  - Pooled over two blocks: 84 cells, D 2, +2 -0, t +1.41. Between the block-2 lines (reject <= 1, accept >= 2), so
    block 3 on set 1. Set 1 has no u0m reference yet: u0m resubmitted (#127) and queued there first.
  - Screen ra_u2g vs araid (control ra_u0m): 80 cells, D 15, +1.4 pts, t +3.65, net +1 (+1 -0).
- Flow-search totals (new VM scratch flowsum.sh: dump + flowend.py per game) over pk1's 8 u0m games vs abrk:
  - Rat-turns with a search building: averystrangespace 32-33%, cheesebottles 30-47%, Stash 17-19%, keepout 2%.
  - Searches that gave up: averystrangespace 151 and 199, others 0-9.
  - Path ends: held by a robot 130-1321, drifted 120-436, replaced by a new target 99-655, newly seen wall 3-120.
    "Reached the end" is 0 everywhere: goTo stops calling follow() within Chebyshev 1 of the target, so arrivals
    count as "replaced" when the next errand starts. The counter can't tell arrivals from switches.
- Stash (field 4529 and local pk1, identical early): our 12 rushers (why 'a') walk about the king's pocket for
  100+ rounds.
  - Rat 10999 locally: a path was ready by r42 but cut a few steps on by a wall tile it hadn't seen. Rats see only
    part of the y=7 wall row (90-degree cone), so A* runs through the unseen tiles.
  - Each new search toward (15,26) took 32-37 turns (r48-78, r85-120, r130-168), each again cut by a wall.
  - FLOW_SPLICE (off): on such a cut, a reverse search runs from the path tile FLOW_SPLICE - 1 past the first clear
    one beyond the wall back to the rat, which holds still. The rest of the old path is joined on. A detour not
    found within FLOW_SPLICE_TURNS (6) starts the full search.
- averystrangespace (u0m as A, king (3,3)): the symmetry was still unknown at r330. The rushers alternated between
  the flip spots (5,56) and (54,3). Every search toward (5,56) from the dirt tubes near (9,20) ran 61 turns and
  failed: 201 searches of 60+ turns in the game, 3 per rusher by r330.
  - RUSH_BAN (off): an attacker passes over a spot whose search failed within RUSH_BAN rounds while another is live.
- Snapshots on u0m: u2k (FLOW_SPLICE 2), u2l (RUSH_BAN 150), u2m (FLOW_CACHE, never tested alone since s6v, before
  FLOW_REV). Diags sp1/sp2 replay pk1's 8 cells vs abrk.
- u2i (RUSH_UNTIL 100) vs u0q, adc's 20 cells: 11 wins to u0m's 11. Neutral.
- u2j sy1 vs abrk: 8 of 12 to u0m's 6. Gained tiny B, keepout B and averystrangespace B; lost keepout A.
- VM disk was at 527 MB free. Deleted 60 more replay dumps older than 12 h that no dump list references (all
  regenerable): 1.2 GB free.

## 2026-10-31 01:22 PDT: u0m's set-1 reference; u2g block 3 queued; sp1/sp2; u2o; one mine on wallsofparadis
- u0m's set-1 reference (#127): 4588-4591, 4601-4602, 4611-4614, 4719-4722, 23 of 42. Lost AlexT101 5 of 6,
  abcdezyxwv 5 of 6, spsquared bed A/B and superioristhmus A/B, AdamTan12 DefaultSmall A and jail A/B, CS50Crab
  streetsofnewyork A. u2g resubmitted (#128); its set-1 block 3 is queued from 4723.
- Diags vs abrk on pk1's 8 cells (u0m 6 of 8):
  - u2k (FLOW_SPLICE): 7 of 8. Gained averystrangespace B; Stash king kills r589/r1085 (u0m r826/r1047).
  - u2l (RUSH_BAN): 7 of 8. Gained averystrangespace B; Stash king kills r476/r536. keepout games identical to u0m's.
  - u2m (FLOW_CACHE): 6 of 8, the same cells as u0m. Parked; its screen dropped from the queue.
- u2i (RUSH_UNTIL 100) screen vs u0m: 80 cells, D 11, +1.2 pts, t +0.43, net +2. Neutral; parked.
- u2o = u0m + FLOW_SPLICE 2 + RUSH_BAN 150 (the two mechanisms touch different code: path mending and the rush spot
  choice). Diag sp3 (pk1's cells) and screen ra_u2o queued.
- wallsofparadis vs AdamTan12 (3855/3856, 0 of 2; scratch minework.py, traj.py on the VM): 60x60, eight mines, the
  map is nested walled rings with one-tile dirt gates.
  - Our king listed one mine, (30,3), all game (local u0m vs abrk game: `ml=N3` from r100 to r1990). Up to 16 rats
    stood within 4 of it (33k rat-turns locally for 4560 cheese). A mine yields ~2.25 a round however many rats
    wait (each round both twins roll 1 - 0.99^k, k rounds since the pair's last spawn, so ~9 rounds a spawn of 20),
    and the king eats 2: one mine is break-even.
  - AdamTan12 worked all eight: ~1400-1700 from each ring mine. Their king walked to its edge mine, and a second
    king sat in the inner ring by r400. With equal babies (~20) they delivered 6.9k by r650 to our 1.2k; our king
    starved at r691/r905 with 90-200 combat damage.
  - Our scouts (1 in 4 gatherers to r150) walk the open outer strip and never dig a gate. The open corner mine
    (55,5) lies on our side's boundary (equidistant), so homeHalf() keeps everyone off it.
- keepout vs AdamTan12 (50x50, 12 mines, both lost r840/r937): on our half they picked 7500 to our 4468 (B side),
  ~1-2 rats a mine on every mine; we stacked 3-4 on 3-4. From r500 our population fell below 16, and each
  replacement spawned with 4-15 alive is a rusher ('a' 36-59% of our rat-turns from r500).
- Big maps (area >= 2500: where_are_you, wallsofparadis, uneruesansfin, superioristhmus, keepout) hold 10 of u0m's
  37 set-3/set-4 losses. The causes differ: one known mine (wallsofparadis), out-harvested on our own half
  (keepout), rushers dying full (uneruesansfin; u2n targets it), population (superioristhmus).
## 2026-10-31 02:10 PDT: u2g accepted; path vs Euclid mine halves; the oracle; TRIP_HALF (u2q, u2r)
- u2g (#126/#128, rich kings) set-1 block 3 (4723-4736) vs u0m's reference #127: 42 cells, 40 alike, D 2 (+2 -0:
  CS50Crab streetsofnewyork A, spsquared superioristhmus A), t +1.41. Pooled over its three blocks: 126 cells, D 4,
  +4 -0, t +2.02: ACCEPTED (t >= 2 with D >= 4) at 02:09 PDT, the new incumbent.
  - Its reign: 122 of its 126 cells match u0m's games, so the accepting luck is the 4 gained cells. Block 1 = set 2
    against u2g's own reference (queued from 4737), then set 3 and set 4 (u2g's blocks as reference, plus the pools),
    set 1 last after a HAND_FLIP twin of u2g. Candidates are rebased on u2g.
- Path census (scratch pathdist.py/pathcensus.sh on the VM: 8-way path from exact mirrored starts, dirt +4, over the 23
  panel maps in the dump lists). Path and the Euclidean half test disagree on three maps, all solid losses:
  - uneruesansfin, all 4 mines. From A (4,54), (32,49) is 809 vs 2465 in d2 but 137 vs 92 steps, and (53,53) is the
    reverse, 49 vs 188.
  - wallsofparadis, 4 of 8: (55,5) 52 vs 72 steps, (38,22) 50 vs 55, and their twins.
  - cheesebottles, 2 of 6: (19,2) 46 vs 42 steps.
  - Elsewhere only exact path ties (uneasy_alliance, rose). With dirt free, where_are_you joins.
- u2p (EXPERIMENT, PathOracle: hard-coded path verdicts for those 10 mines). u2p beat u0m as A on uneruesansfin, but
  against abrk it moved 4038 cheese to u0m's 5237.
  - Scratch mtgt.py (rat-turns by indicator target mt=) explains it. u2p's king listed only (53,53) until r1600, so
    every rat worked one mine. u0m worked (53,53) and then (32,49).
  - A mine drops ~2.25 cheese a round however many rats wait there, so the mines worked, not the steps walked, set
    the income. Handing (32,49) to the enemy pays only if its twin (27,10) is listed and worked.
- TRIP_HALF (new, off by default): the real version.
  - Gatherers time clean walks (birth or delivery to arrival, every turn a walk-to-mine move) and squeak them to a
    king in earshot.
  - The king keeps each listed mine's fastest walk (steps / 4) in the spare bits of its shared slot.
  - The half test calls a mine ours when its walk beats its twin's by TRIP_HALF steps, theirs when it trails. An
    untimed twin counts its Chebyshev distance from our start.
  - u2r adds King.mirrorAll: once the symmetry is known, the king lists every twin on either half.
  - Default bytecode unchanged: only new members, plus ldc -> ldc_w in six King methods where the constant pool passed
    255 entries (same instructions).
  - Diags th1 (u2q, walks only) and th2 (u2r, with twins), vs u0m and abrk on the three maps, both sides.
  - Risk on uneruesansfin: with twins listed, (6,6) (Euclid ours by a hair, 188 steps) takes rats before any walk is
    timed.
- Also: u2o (FLOW_SPLICE + RUSH_BAN) diag sp3 6 of 8 like u0m (traded averystrangespace A for B); ra_u0p screen
  neutral (80 cells, t +0.08, net -2), u0p parked.

## 2026-10-31 02:47 PDT: u2g's set-2 reference; who beats us and how; one mine vs AdamTan12; SEEK_FRONTIER (u2t); u2s submitted
- u2g's set-2 reference 4737-4750: 26 of 42 (bc26vibe 6/6, CS50Crab 6/6, outercloud 5/6, spsquared 5/6, abcdezyxwv
  2/6, AdamTan12 2/6, AlexT101 0/6). u0f's re-anchor of set 2 was 18 of 42. u2s submitted (#129), set-2 block 1
  queued (ids > 4750, waiter build/wait_u2s1.sh).
- u2g's three accepting blocks by opponent: abcdezyxwv W1 L17, AlexT101 W3 L15, AdamTan12 W8 L10, spsquared W13 L5,
  CS50Crab W16 L2, outercloud W17 L1, bc26vibe W17 L1. The two top opponents beat us by killing the king.
  - King-kill losses: vs abcdezyxwv 44 (quartiles r261/323/415), vs AlexT101 39 (r194/302/391). Both sides hold one
    king until ours dies; we rarely form a second in those games.
  - Cats are 0-7% of our king's damage overall (one exception: sunandmoon as B, ~280 of 600 from the cats).
- Side gaps in the field (our results by side): averyfineline A 2/14, B 12/14 (vs AdamTan12 A loses r109-199, B wins
  r148-162); TheHeist A 14/14, B 4/14; sunandmoon vs abcdezyxwv A 3/3, B 0/3; groveisbeingdesecrated vs CS50Crab A
  always lost at r190.
  - TheHeist: each team kills its own cat (4000 each), kings 1 and 1, so transfers decide. As B we kill our cat by
    r200 and trail 980 to 1735 when the game ends at r441. Cooperation endings on the last cat's death cost 20 field
    games (TheHeist 10 lost, 18 won; groveisbeingdesecrated 7 lost).
- Local side census (tools sidebias.py, ~15k VM games pooled): A's cat-damage share sits near 0.5 on most maps, but
  A's win share is skewed on some (houseofcards 0.64, corridorofdoom 0.66, cheesebottles 0.69, groveisbeingdesecrated
  0.34, whereisthecheese 0.37). Every local wallsofparadis game this session went to B (po1, th1, th2, wp1: 14 of
  14, whichever bot was B).
- Cat AI note (engine): in ATTACK, chirality 0 turns right and 1 turns left. Mirrored cat pairs carry 0 and 1, so on
  flip maps they move as mirror images; on rotation maps the rotated twin of a turn keeps its handedness, so the
  pair's moves are not rotated copies.
- One mine vs AdamTan12 (4545/4546 wallsofparadis and keepout, 4735/4736 jail): we worked one mine and starved while
  they worked them all. wallsofparadis as A transferred 1170 to 7325 by r692 (king starved at bank 1); keepout 4009
  to 13000; jail only (8,36) as A and (8,3) as B, never (52,35)/(52,4). On jail the early scouts went into the walled
  centre and died; nobody walked east along the open home band.
  - Local sl1 (u2g vs abrk, wallsofparadis): as A the king listed only (30,3) all game (5265 delivered); as B all 8 by
    r800 (14163). B's seekers drew more never-seen targets (x code 73-253 rat-turns per 100 rounds vs 4-88 as A).
    exploreHome takes a uniform random never-seen home tile, and A's draws sat on the edge band (e.g. (3,19)), so
    nobody crossed the ring dirt; (21,37) behind the "::" dirt diagonal stayed unseen.
- SEEK_FRONTIER (new, off): seekers (SEEK_CROWD, SEEK_IDLE) take the never-seen tile nearest by path cost (Flow's
  frontier search, digging dirt on the way) before exploreHome's random one. Only a new member in the default
  bytecode. u2t = u2g + it; diag sf1 (vs abrk on wallsofparadis, jail, keepout, where_are_you, streetsofnewyork,
  pipes, safelycontained, attack_on_cat, both sides; u2g controls from sl1, ws1 and sf1).
- u2s = u2g + the side-symmetry bundle (FLOW_QUOTA 18, SYM_FLOW, SYM_CENTRE, SYM_HAND, SYM_FACE, SYM_TIE): u2j
  rebased on the incumbent, a basics repair (accepts at t >= 0 after block 2). Diag ws1 checks its A side on
  wallsofparadis/jail/keepout and its self-play mirror.
- Closed: u2p (oracle) po1 9 of 12, u2q th1 7 of 12, u2r th2 7 of 12 (no gain; wallsofparadis is B's in all three);
  screens ra_u2k (77 cells, t +0.62, net 0) and ra_u2n (80 cells, t +0.34, net 0) neutral; u2o taken off the screen
  queue unrun.
- VM disk (98% full): deleted 143 old (>12 h), unreferenced, regenerable replay dumps from build/dumpcache (83, then
  60) and the scored th1/th2/po1 kept replays (~98 MB); ~500 MB free.

## 2026-10-31 03:40 PDT: u2s rejected; exploration is not the lever we can pull; king guards stand still; the 1000 reserve (u2v)
- u2s rejected on the replica (#129): set-2 block 1 4751-4764 vs u2g's 4737-4750, 42 cells, 35 alike, D 7 (gained
  AdamTan12 attack_on_cat B + dirtpassageway B, outercloud Hike A; lost abcd ultimatestarvation A/B, CS50Crab
  wallsofparadis B, spsquared jail A), net -1, t -0.38; 26 of 42 as u2g. AlexT101 and abcd 0 of 12 (u2g 2 of 12).
  The side-symmetry switches stay in ratbot, off; ra_u2s stopped at 68 of 80 (moot).
- Coverage (coverage.py on u2g's set-2 games): by r200 we had seen 38-58% of the map with mirror twins, against
  91-99% for abcd, AlexT101 and AdamTan12, and plateaued after r100; home mines 14-23 from our king went unseen (pipes
  (30,30)/(47,30), where they took 4600). SWEEP (new, off: scouts and seekers take our half's cells nearest
  a lane anchor, by slot) in u2u = u2s + SWEEP 5; diag cv1 (u2u and u2s as A vs abrk, 8 maps): 7 of 8 to 6 of 8,
  seen share at r200 44-86% vs 49-84% (safelycontained +14, keepout -4, dirtpassageway -10 where u2u won at r316).
  No gain: dropped. With the exploration record (s4d, s6l/s6o, s7v/s7w, s7z, s9c-s9i, t0x/t0y, u0w/u1a, u2t, u2u)
  every scout variant has been neutral or worse.
- Where the field cheese goes (minelag.py, 4757 safelycontained vs AlexT101, our king died r795): the four mid
  mines gave them ~4000 and us 215 (two never seen by us); our half's six gave us 4815 and them 2085; their half's
  five gave them 5440 and us 40. They ran 31-47 babies and 2-5 kings from r400; we fell 29 -> 19 -> 10 -> 7.
  From r400 our bank held 976 with 19 babies: under RICH_BANK 1000 the cap is 18, so nothing spawned until the bank
  crossed 1000 again (one 60-cheese rat per ~7 rounds at our 8.4/round) while ~650 cheese sat idle after a fight
  that cost us 10 rats. sittingducks (4757 g2) the same: bank 507-967 with 19-22 babies from r200 to r1140.
- King guards stand still (kdef.py/kdef2.py, 4757 evileye, our king 574 -> 182 in r98-122): 16-19 of ours within
  Chebyshev 4 of it against 7-11 of theirs; their rats bit it 27 times, ours bit 9 times. Of our rats near the king
  without an action, war standoffs ('w', not moving) 48 rat-turns and face-holds ('f', not moving) 25; most had an
  enemy within dist2 20 that was not one of the king's biters. Adjacent-but-idle cases were carried enemies (same
  tile as their carrier). GUARD_KING_R (s8d rejected on a local diag, s9w neutral screen) is this fix and never
  met the field rushers; it is the next candidate after u2v.
- Pre-registration, u2v = u2g + RICH_BANK 500 (copy of u2g, one constant). Mechanism: the cap rises to 40 at bank
  500 instead of 1000, so the reserve between 500 and 1000 buys rats (a one-time ~500-cheese push, again after
  each fight). Displaces: the 1000 guard s9s added against s9r's starvations (seepastyourgreed, cheesebottles).
  Counter: rounds with bank in [500, 1000) and 18+ babies, and spawns there (field: hundreds of rounds in the
  sittingducks/safelycontained/pipes games). Dose series locally (rb2, vs abrk, 8 maps x 2 sides): 1000 (u2g, the
  zero arm), 500 (u2v), 300 (pb2, probe). Deciding instrument: the replica set-2 block vs u2g's 4737-4750; block 1
  rejects at t <= 0. Falsifier: a starvation loss or more king kills against us (bank spent).
- VM disk hit 100% (cv1's dumps failed with ENOSPC): deleted our diag_stuck1-15 kept replays and dumps (~770 MB)
  and the kept replays of 86 finished local runs dated 10-28..10-30 (1652 MB; results.jsonl kept, list in
  ~/diag26b/oldrp_deleted.txt); 2.3 GB free.

## 2026-10-31 04:41 PDT: u2v rejected; cv1 was void (SWEEP overran); what kills our rats against the top two
- u2v (u2g + RICH_BANK 500) rejected on the replica (#130): set-2 block 1 4801-4814 vs u2g's 4737-4750, 42 cells,
  31 alike, D 11 (+3 -8), net -5, t -1.51. Falsifier hit. 4807 safelycontained: 36 babies at r100 (AlexT101 19) fell
  to 3 by r600 with the bank at 77, delivered 3615 to 10889. The extra rats didn't add delivery and soaked up losses.
  Local dose series rb2 (vs abrk, 8 maps x 2 sides): u2g 16/16, u2v 16/16, pb2 (300) 15/16 (starved cheesebottles A).
- u2w (u2g + GUARD_KING_R 5) parked: gk2 (vs agrab and arush, 8 king-kill maps x 2 sides) 19 of 32 to u2g's 22.
  Loses DefaultSmall A both and B vs arush (u2g won all three; bank at 1-3), +1 evileye, -1 starvation (that map
  is a coin flip: u2g also starved once there).
- ra_u2s stopped at 67 of 80 after the replica verdict: 66 cells, -2.5 pts, t -0.47, net -4.
- The cv1 verdict on u2u (SWEEP) is void: u2u overran 157 times in its 8 games (safelycontained 86, jail 29, keepout
  25). The one-turn cell pick scanned ~140 cells with a half test each. sweep() now scans incrementally while more
  than SWEEP_LEFT 8000 bytecodes are left and picks up next turn (BotTest 569/569, full suite exit 0, bccmp vs s9s:
  only new members, plus sweep() changed but unreachable at SWEEP 0). u2x = u2g + SWEEP 5 (snapvar @s9s). Diag cv2:
  u2x and u2g vs abrk on the cv1 maps, both sides; first 10 u2x games 0 overruns.
- Deaths against the top two (deaths2.py, u2v's 12 games vs AlexT101 4807/4808 and abcd 4809/4810). We laid 0 rat
  traps in all 12 (u2g carries s9s's KING_TRAPS and BABY_RAT_TRAPS off); they laid 1-307 a game. Our baby deaths:
  throws (throwLand + throwHit) 20-68 a game, the biggest killer in every game; traps 44/56 on safelycontained,
  49/117 on sittingducks, 24/21 on pipes (abcd) and 11/17 on evileye. They grab us more than we grab them in 11
  of 12 (e.g. 121 to 96, 141 to 114, 143 to 88). Trap laying (t0c, t0d, u0j, u0q, u0y, u1q, u1u) and the grab war
  (BITE_GUARD, RESCUE, GRAB_MICRO, CARRY_AIM, THROW_*, AMMO_AWAY) are both mined out: neutral or rejected.
- 4807 safelycontained why codes: 1071 of our baby turns in r0-99 were attack moves ('a'), 112 in r100-199. The 12
  rushers die early, as u1w found on constrictinglattice. Rush size is tuned both ways on the replica (u0o 8
  rushers net -7, u1o 16 net -3), and RUSH_LIFE (t0g) and RUSH_UNTIL (u2i) were rejected or neutral.
- VM disk at 97% (606 MB free): deleted 116 build/dumpcache dumps that no ~/diag26b dump list references and that
  were older than an hour (rb2/gk2/cv1/field analysis dumps, regenerable from kept replays; list in
  ~/diag26b/dcache_deleted.txt); 1.9 GB free.
- cv2 (u2x vs u2g, vs abrk, 8 maps x 2 sides, 0 overruns either): u2x 11 of 16 to u2g's 15, delivered 226k to 257k;
  lost attack_on_cat B, dirtpassageway A, pipes A, sunandmoon A where u2g won. SWEEP, even bounded, is a loss: u2x
  dropped. The exploration record stands.
- Field pile race (race.py/minerace.py): on safelycontained (4807) 170 of our half's 351 spawns before r400 went to
  AlexT101 with a median wait of 98 rounds; on pipes (4809) abcd took 102 of 215 after 159 rounds. Three of our
  half's mines (pipes (30,30), (47,30); safelycontained (2,48), (18,46), (27,47)) got 0-6 of our rat-turns.
  Locally vs abrk u2g works all five pipes home mines (first visits r168 and r306). In the field the enemy holds
  that ground (abcd: 1264 rat-turns at (30,30), 1512 at (29,15)), so the economy gap follows the war on our half.
- Rush payoff vs the top two (rushpay.py, u2v's 12 games): 12-27 rushers a game, 12-24 dead by r300 in 11 of 12;
  enemy-king damage by r300 0-60 of 600. The rush wins early king kills against the weaker teams (rush size is
  tuned both ways on the replica), so it is kept.
- How our king dies early (kingdmg.py, ammo.py, thrower.py; evileye x2, averyfineline x2, ultimatestarvation B):
  throw hits 292-500 of the ~600, bites 80-260 (evileye A also 100 from two traps on the king's walk). Every
  thrown rat was ours (8-12 a game, mostly 20+ rounds old). Throwers stood at Chebyshev 2-5 from the king's centre
  (mostly 3-4), held the rat 2-4 rounds, and grabbed it at Chebyshev 2-4. The set-1 block shows the same early
  kills from AlexT101, abcd, spsquared (bed) and AdamTan12 (DefaultSmall): it is the main loss mode against the
  strong teams.
- King defense on the replica: KING_BLOCK s4t (-5), RESCUE s8o (-1), AMMO_AWAY u1v (-2), MOAT u2b (-2).
  GUARD_KING_R never ran there (s8d local reject, s9w neutral screen, u2w gk2 19 to 22 vs agrab/arush, which
  don't reproduce the field's king assault).
- Pre-registration, u2w = u2g + GUARD_KING_R 5 (copy of u2g, one constant). Mechanism: babies within Chebyshev 5
  of our king take as target the visible enemy babies within 2 of its centre (its biters) and skip the standoff
  against them (4757 evileye: 16-19 of ours near the king against 7-11, our 9 bites to their 27, 48 standoff and 25
  face-hold rat-turns idle). Counter: king hp lost to bites, and our idle rat-turns near the king (kdef2.py).
  Doses: 0 (u2g, zero arm), 5. Instrument: replica set-2 block 1 vs u2g's 4737-4750; block 1 rejects at t <= 0.
  Falsifier: starved kings or lost economy games (gk2's DefaultSmall), or no fewer king kills by r300.

## 2026-10-31 05:06 PDT: carrier guard (GUARD_CARRY_R); the local grab proxies do throw ours into our king

- Correction to the entry above: agrab and arush DO reproduce the field's throw assault. ammo.py over gk2's 32 u2g
  games: throw hits on our king, nearly all with our own rats, 13-18 on evileye (vs agrab), 11-13 on averyfineline
  (agrab), 6-8 on tiny (agrab), 10-11 on DefaultSmall (arush), 2-9 on tiny (arush), 4-8 on ultimatestarvation
  (arush), 0-1 on safelycontained/starvation. That gives a local delivery check for a throw counter.
- New switch GUARD_CARRY_R (Cfg, 0 = off, with GUARD_CARRY_REACH 8): at war a baby within Chebyshev 8 of our king
  takes as rescueT the visible enemy baby nearest it holding one of ours within Chebyshev R of the king's centre,
  and rescue() steps to a tile next to it that it cannot see (walking toward one when none is a step away), then
  grabs it (a grabbed carrier drops ours; tryCarry ranks carriers of ours first). Code: Baby.kingCarrier, guarded
  statements only; bccmp vs s9s shows new members only; BotTest 575/575 (6 new checks).
- Pre-registration, u2y = u2g + GUARD_CARRY_R 5 (snapvar @s9s, u2g recipe + the switch). Mechanism: the carriers
  that throw ours into our king stand at Chebyshev 2-5 of its centre and hold the rat 2-4 rounds; a grab from
  behind in that window stops the throw. Counter: throw hits on our king (ammo.py) and our rats grabbed near the
  king, per game. Doses: 0 (u2g, gk2's games), 3 (u2z), 5 (u2y). Instrument: local delivery check on the VM, the
  32 gk2 cells (agrab and arush, 8 maps x 2 sides) per arm; then the replica set-2 block 1 vs 4737-4750 after
  u2w's block (block 1 rejects at t <= 0). Falsifier: throw hits on our king not lower than u2g's on evileye,
  averyfineline, tiny and DefaultSmall, or fewer wins/lower delivery from rats pulled off gathering.
- How the enemy grabs ours near our king (new scratch grabwhy.py; gk2's 32 u2g games vs agrab/arush, grabs within
  Chebyshev 5 of our king's centre; 0-151 a game, evileye 83-151 vs agrab): about 4 in 5 victims had less HP than
  the grabber (a grab needs that or a blind victim), split ~60/40 between facing the grabber and not; ~1 in 5 were
  healthier but blind. Victims' last moves: f (facing an enemy), w (war), r (retreat), T, M. Most were thrown later.
  So facing (glance/guardTurn) can stop at most a fifth: grabbing the carrier is the lever. Note: the replay's
  dir column is FlatHelpers order (0 CENTER, 1 W, 2 SW, 3 S, 4 SE, 5 E, 6 NE, 7 N, 8 NW), not Direction.ordinal.
- gc1 (u2y = GUARD_CARRY_R 5, u2z = 3; gk2's 32 cells vs agrab/arush; u2g control = gk2's games): wins u2y 25,
  u2z 24, u2g 22 of 32; delivered 255k / 245k / 250k. u2y vs u2g: +6 (agrab tiny A+B and constrictinglattice B;
  arush averyfineline A, starvation B, tiny B) -3 (agrab DefaultSmall B, starvation A; arush DefaultSmall B).
  Delivery (new scratch saves.py; ammo.py): carriers of ours grabbed 831 vs 734, within Chebyshev 6 of our king
  457 vs 398 (+15%); enemy grabs of ours near our king 587 vs 571; throw hits on our king 125 vs 146 (ours 107 vs
  133). The mechanism fires but only trims the assault (u2g's tryCarry/POUNCE already grab most carriers they
  meet). It passes the delivery check: u2y is the next replica candidate, after u2w's block. u2z (dose 3) parked.
  Next refinement if u2y helps: rescue() steps only to blind tiles, but a carrier with less HP than ours can be
  grabbed from any adjacent tile.

## 2026-10-31 05:39 PDT: u2w rejected; u2y on the replica; GUARD_CARRY_HP
- u2w rejected on the replica (#131): set-2 block 1 4850-4855, 4883-4890 vs u2g's 4737-4750, 42 cells, 39 alike,
  D 3 (gained spsquared evileye B; lost CS50Crab wallsofparadis A, outercloud cheesefarm B), net -1, t -0.58. 26 of
  42 (AlexT101 0/6, abcd 2/6, as u2g). King kills against us 15, as u2g's 15, with evileye at r168/r247 and
  averyfineline at r101/r111: the falsifier holds. Biting the king's biters does not stop throws.
- u2y submitted (#132), set-2 block 1 from 4898 vs 4737-4750 (6 sent, 8 queued; waiter build/wait_u2y1.sh).
- GUARD_CARRY_HP (new, off; needs GUARD_CARRY_R): a carrier with less health than ours can be grabbed from any
  side (HEALTH_GRAB_THRESHOLD 0), so Baby.rescueWeak goes for the nearest tile beside it (blind ones first) or walks
  toward it, instead of rescue()'s blind tiles only. A guarded early return in rescue(); BotTest 579/579.
- Pre-registration, u3a = u2y + GUARD_CARRY_HP (snapvar @s9s). Mechanism: guards that are healthier than the
  carrier no longer need its blind side, so more carriers are reached inside their 2-4 round hold. Counter: carriers
  of ours grabbed within Chebyshev 6 of our king (saves.py) and throw hits on our king (ammo.py). Doses: off (u2y,
  gc1's games), on. Instrument: diag gc2 (gc1's 32 u2y cells), then the replica after u2y's verdict. Falsifier: no
  more saves near the king than u2y's 457, or fewer wins than u2y's 25 of 32.
- Field room for the carrier guard (new scratch carriers.py; u2w's 4883-4886 vs AlexT101/abcd, 12 games): enemy
  carry episodes of ours with the carrier within Chebyshev 5 of our king: 19-55 a game; ended by our grab of the
  carrier 4-21, thrown into our king 1-15, thrown into something else 3-20. In most episodes a free rat of ours
  stood within Chebyshev 1 of the carrier (about half of those healthier than it), and nearly all the throws into
  our king came from carriers with ours at Chebyshev 1-2: they were in reach but did not grab (the carrier saw
  them and was not weaker, or they had other business). u2y's guard and u3a's any-side grab aim at exactly these.
- gc2 (u3a = u2y + GUARD_CARRY_HP; gc1's 32 cells; u2y control): wins 28 to 25 (+3 -0: agrab DefaultSmall B,
  averyfineline B, constrictinglattice A), delivered 248k to 255k, rounds 25.4k to 27.7k; king kills of us 3 (u2y
  5, u2g 7), by us 21 (18, 16). Delivery: carriers of ours grabbed near our king 367 to 457, enemy grabs of ours
  near our king 460 to 587, throw hits on our king 119 to 125. Saves per enemy grab near the king 0.80 (u2y 0.78,
  u2g 0.70); throw hits per enemy grab 0.26 (u2y 0.21, u2g 0.26). The pre-registered falsifier (no more saves
  than 457) is met and the wins rest on 3 discordant cells: u3a parked, not promoted on its win count.
- Why our rush does nothing against the top two (deaths.py, rushpay.py, ammo.py from their side; u2w's 4883-4886):
  we landed 0 throw hits on their king in 10 of 12 games (4 and 13 in the two ultimatestarvation wins). On evileye
  (4883, lost r247) AlexT101 laid 53 traps by r100; 24 of our rats died to traps by r200 and 30 to throwLand
  (grabbed weak or stunned, then thrown); all 28 rushers were dead by r300 with 0 damage to their king. On
  averyfineline 14 of 16 rushers were dead by r100, also with 0 damage.
- Pre-registration, u3b = u2g + RUSH_BURN 6 (u1w's switch on the incumbent; u1w was weak locally vs u0q, 8 to 11,
  and never ran on the replica). Mechanism: once an enemy trap fires on us, rushers more than Chebyshev 6 from the
  enemy start turn gatherers instead of walking into the trap line, and stop feeding the enemy grab-and-throw.
  Counter: rushers demoted, our rats killed by enemy traps and throwLand, delivered cheese. Doses: 0 (u2g), 6.
  Instrument: diag rb3 (u3b and u2g vs u0q on rsb2's 10 maps, both sides) as the delivery check; then the replica
  set-2 block 1 vs 4737-4750 when the slot is free. Falsifier: no fewer of our rats killed by traps and throws, or
  wins lost where u2g's rush kills kings early (spsquared, CS50Crab, bc26vibe).

## 2026-10-31 06:22 PDT: panel v4 recalibration (owner request #18)
- u2g's record on panel v3 (sets 1-4, games): outercloud 22/24, bc26vibe 23/24, CS50Crab 22/24 (too easy, >=90%);
  AlexT101_usqualsbot 3/24, abcd 3/24 (too hard, <=13%); spsquared 18/24 (75%), AdamTan12_Finals 11/24 (46%).
  Five of seven opponents sit outside the 25-75% band, so the panel measured little: most cells never change.
- Panel v4 keeps spsquared_TSPAARK_QUALS and AdamTan12_Finals on their v3 maps (u2g's references carry over) and
  drops the other five. Calibration sweep: u2g vs 11 field bots rated 1551-1609 (HugoIngelsson_YokoOno,
  VarunVejalla_final4_run_away10, HugoIngelsson_Xzibit, daannte_v47_Skadoosh, thewaxmango_collegejunior,
  bman12345678_goonv23, AlexT101_finalsbot, AdamTan12_Icarus, nzjt_TheComplexMerlin, daannte_v52_ThisGuy,
  thewaxmango_PhD), one match per seating on 3 random maps recorded in `research/panel/calib2.json` (22 matches,
  66 games). Bots u2g beats 25-75% join v4 (6-8 in all, at most one per author where possible); their sweep matches
  are u2g's v4 set-1 reference. v4 sets 2-4 are drawn after, with reference blocks for the new teams.
- The sweep starts when u2y's set-2 block 1 finishes (2 AdamTan12 matches wait for the hourly window).

## 2026-10-31 06:31 PDT: misallocation: five candidates aimed at bots we beat ~12% (owner prompt #19)
- 42 of u2g's 68 lost panel-v3 games were to abcdezyxwv_bestsofar (ladder 6th, 1637; 3/24) and AlexT101_usqualsbot
  (1566, just above our 1559, but 3/24: the compressed ratings hide the gap). Every loss tally this morning (throws
  into our king, carrier episodes, rushers dying to traps) was therefore their tally, and u2w, u2y, u2z, u3a and u3b
  were all built against them. Their cells are losses for any build near ours, so such a fix cannot show on the panel
  (u2y's interim block: lost abcd ultimatestarvation A and B, gained only spsquared evileye B).
- Rule added to TRAINING_ALGORITHM §3.2: diagnose losses a candidate can flip (in-band opponents, cells that differ
  by side or build); a whole-panel loss tally is the strongest bots' tally. Panel v4 drops both; the next loss
  analysis uses the sweep's in-band opponents and AdamTan12/spsquared.

## 2026-10-31 06:35 PDT: u2y rejected; u3b rejected locally; u2g back on the replica for the v4 sweep
- u2y rejected (#132): set-2 block 1 4898-4903, 4926-4931, 4960-4961 vs u2g's 4737-4750, 42 cells, 37 alike, D 5
  (gained AdamTan12 dirtpassageway B, spsquared evileye B; lost abcd ultimatestarvation A/B, CS50Crab
  wallsofparadis B), net -1, t -0.45. GUARD_CARRY_R stays in ratbot, off; u2z and u3a (its variants) are closed.
- u3b rejected locally: diag rb3 vs u0q 11 of 20 to u2g's 14 (D 9, +3 -6: lost attack_on_cat A/B, dirtpassageway
  A/B, sittingducks B, where_are_you A). RUSH_BURN demotes rushers that would have won on the trap-free maps.
- u2g resubmitted (#133). Calibration sweep queued: requests 3931-3934 sent, 18 queued for the drain loop; maps are
  drawn into research/panel/calib2.json as each opponent is sent. Waiter build/wait_sweep.sh (22 matches after 4961).
- Diagnosis against an in-band opponent (AdamTan12_Finals, u2g 11/24), per the new rule. Results are deterministic
  (the same builds repeat a game to the round), so the cells that can flip are those that differ between builds:
  dirtpassageway B (u2g L, u2s/u2v/u2y W) and attack_on_cat B (u2s W); keepout, wallsofparadis and jail (both
  sides) and dirtpassageway A are lost by every build since u2b.
  - Traps (battle_report, 4750 dirtpassageway B): they laid 91 rat traps, 89 fired on us (mean life 4 rounds: laid
    in front of our movers), killing 31 of our 64 dead; then grab and throw (51 throws). Engine: a rat trap costs 20,
    deals 50, sets movement cooldown 30 and fires on any enemy within dist2 2; our side never senses it. Our
    trap-avoidance switches (RISK_FRESH, RISK_ADAPT, WOUNDED_HP, FACE_HOLD, RISK_IN_FIGHT) were all weak vs u0q.
  - Economy (new scratch econshare.py: cheese picked within Chebyshev 4 of each mine, by mine side and team; 4545,
    4546, 4735, 4736): in our 7 lost games they picked 2345 a game on our home half, as much as we did there
    (2442), plus 1357 on the axis mines and 5762 on theirs; in our 5 wins they took 211 from our half. The war opens
    at r14-52 (they backstab). keepout A: they worked all 12 mines, we 5; the axis pair (21,47)/(28,47) gave them
    3520 and we never went near it. wallsofparadis A: we worked 1 of 8.
- Sweep under way fast: by 06:39 matches 4969-4974 are in (YokoOno, VarunVejalla, Xzibit: u2g 2 of 6 each).

## 2026-10-31 06:45 PDT: in-band losses are an economy deficit; u3c (MINE_ANY + MINE_TIES on u2g)
- Over u2g's in-band games so far (AdamTan12 4545/4546/4735/4736 and the sweep's 4969-4974: YokoOno, VarunVejalla,
  Xzibit; scratch deathsum.py, kingcount.py, minetime.py, econshare.py), lost games (19): the kill trade is even
  (our rat deaths 65 a game, theirs 60), our king loses 379 hp to bites and 233 to starvation, and they transfer
  4971 to our 1811. Neither side forms extra kings before r400; we field more babies early (22 to 17 at r100).
- To r300 (15 lost games past r300): they pick 46 cheese per 100 baby turns to our 24, and 113 per 100 turns spent
  within Chebyshev 4 of a mine to our 44, though our rats spend more turns there (54% to 41%; why codes M 23%,
  W 15%: walking to and waiting at the assigned mine). Off-mine pickups are small (36 vs 86 a game). In our wins
  their rate falls to 40 and ours stays 24.
- Engine: a mine drops 20 cheese at a random tile of its 9x9 square (and its twin at the mirrored tile) with a
  hazard rising each round since the last drop (1 - 0.99^k: about every 12 rounds), however many rats wait, and
  the cheese stays until picked. So rats parked at a mine add nothing past the first, and every unvisited mine is
  a growing reservoir: keepout's axis pair gave AdamTan12 3520 and us 0; in our losses they also took 2345 a game
  on our half against 211 in our wins.
- Pre-registration, u3c = u2g + MINE_ANY + MINE_TIES (t0p's pair on the incumbent). Mechanism: MINE_BALANCE's
  slots cover every known mine (either half and the axis), so gatherers stop crowding our nearest home mines.
  t0p screened +10.8 pts (t +3.09, net +6) and delivered more vs abcd's maps, but its replica block (set 4) had
  only 4 discordant cells, on the old panel where 5 of 7 opponents were out of band. Counter: cheese picked per
  100 baby turns, delivered, picks on axis/away mines. Doses: 0 (u2g), on. Delivery check: diag ma2 (u3c and u2g
  vs araid on keepout, wallsofparadis, jail, Stash, learn_your_lesson, starvation, DefaultMedium, arrows, both
  sides). Then the replica on panel v4 set 1. Falsifier: no rise in picks per 100 turns, or rats lost on
  enemy-half trips (deaths away from home up with delivered flat).
- u3c's diag ma2 (vs araid, 8 maps both sides): 12 of 16 to u2g's 14 (D 4, +1 -3, net -2, t -1.0: no reject).
  Mechanism delivered in part: to r300 in won games 35 picked per 100 baby turns to 30, near-mine share 45% to
  53%, waiting at a mine (W) 10% to 15%; delivered +2.5% (139.9k to 136.4k) and araid's -14% (77k to 90k). The
  three losses ended at r256-549 (king kills by the rusher). Next: the replica on panel v4 set 1.
- VM disk at 285 MB free: deleted 186 unreferenced replay dumps older than 1 h (dcache.py DEL 1; ~2 GB, all
  regenerable from kept replays). 2.3 GB free now.
- Sweep so far (u2g games won of 6): YokoOno 2, VarunVejalla 2, Xzibit 2, collegejunior 2 (in band); daannte_v47 0,
  goonv23 0 (too hard); AlexT101_finalsbot, Icarus, Merlin, ThisGuy, PhD pending. While u2g is active, its v4 set-2
  reference for the four in-band newcomers is queued behind the sweep (`research/panel/v4-set2.json`: spsquared and
  AdamTan12 on their v3 set-2 maps, newcomers drawn at send), so a candidate's block 2 needs no u2g resubmission.

## 2026-10-31 07:46 PDT: panel v4 set
- Sweep result (u2g games won of 6; 4969-5013, 5049-5050, 5069-5070): nzjt_TheComplexMerlin 4, AdamTan12_Icarus 3,
  HugoIngelsson_YokoOno 2, VarunVejalla_final4_run_away10 2, HugoIngelsson_Xzibit 2, thewaxmango_collegejunior 2 (in
  band); thewaxmango_PhD 1, daannte_v47_Skadoosh 0, bman12345678_goonv23 0, AlexT101_finalsbot 0, daannte_v52_ThisGuy
  0 (too hard). Ratings again misplace them: all 11 sit within 60 points of us.
- Panel v4 = spsquared_TSPAARK_QUALS and AdamTan12_Finals (kept, v3 maps) + the six in-band newcomers on their sweep
  maps (`research/panel/v4-set1.json`). Two authors appear twice (Hugo, AdamTan12); kept, since all eight are in
  band and every opponent adds cells. u2g on v4 set 1: 21 of 48 games (44%); per opponent 2-4 of 6. Its reference:
  4723-4736 (spsquared, AdamTan12) + 4969-4974, 4988-4989, 5010-5013. A block is 16 matches, 48 cells.
- Milestone "panel: v4" added (progress/milestones.csv, 07:46). Set 2: spsquared/AdamTan12 on v3 set-2 maps, the
  newcomers drawn (u2g's references 5071+ running; waiter build/wait_s2ref.sh). Sets 3-4 are drawn when needed.
- Next: once u2g's set-2 references are in, submit u3c (MINE_ANY + MINE_TIES) for v4 set-1 block 1.
- Pre-registration, u3d = u2g + reactive baby rat traps (u1q's set: BABY_RAT_TRAPS, TRAP_ADJ, TRAP_SPAWN_FIRST,
  RAT_TRAP_MIN_CHEESE 500, TRAP_OPEN_TO 150; u2g lays none). Mechanism: babies at war lay a trap on a front tile
  next to the nearest enemy rat (its next step fires it: 50 damage, 30 movement cooldown, then grabbable), only
  after spawning, with 500 banked, and with 1500 banked before r150. In-band losses: 17.5 of our rats a game die
  to their traps. u0q/u0y were rejected (net -4) mainly on outercloud's opening (31-40 traps by r100 emptied the
  bank), an opponent now off the panel; u1q's opening floor answers that (oq1 vs arush 14 of 16 as u0y). Counter:
  our traps laid and fired, enemy rats killed, bank at r150, delivered. Doses: 0 (u2g), on. Delivery check: diag
  ud1 (vs araid, ma2's 16 cells; control u2g's ma2 cells, deterministic). Falsifier: bank collapse in the opening
  or fewer spawns by r200, or kills not up.
- VM run name tq1 was already taken (10-30, s9s/u0q vs ...): its results.jsonl is intact but its cells.tsv was
  overwritten empty by mistake; u3d's diag runs as ud1.
- u3d's diag ud1 (vs araid, ma2's 16 cells; u2g control from ma2, deterministic): 15 of 16 to 14 (D 1, gained
  learn_your_lesson B), delivered 134.9k to 136.4k. Its traps fire (21.5 laid, 17.9 fired a won game; u2g lays
  none) but enemy rats killed are not up (49.0 a won game to 50.6) and our deaths 58.6 to 55.9: the kills half of
  the falsifier holds. Weak; it stays behind u3c for the replica.
- u2g's v4 set-2 references are in (5071-5078, 5080-5081, 5100-5101). u3c submitted (#134); its v4 set-1 block 1
  (16 matches) queued from request 4071 (waiter build/wait_u3c1.sh; reference 4723-4736, 4969-4974, 4988-4989,
  5010-5013).

## 2026-10-31 08:37 PDT: in-band losses: crowd on the king's mine, far home mines open; u3e (KMINE_SKIP 2)

- Diagnosis (u2g's 32 in-band v4 matches, 102 games, 44 W / 58 L; VM scripts minerank.py, whyloc.py, econagg.py,
  starve.py, endstate.py on inband2.txt):
  - 50 of 58 losses end in king death. King HP lost per lost game: starvation 270, bites 202, throw hits 110.
  - 33 of 58 losses go broke (bank < 20) vs 2 of 44 wins, on average 93 rounds before the end. 100 rounds before
    that our bank sits at the floor (100-300) while theirs is 1000-2000.
  - Ledger to r400 in long losses: we pick 2629 vs their 4086; they take 772 from our side. About 1600 cheese lies
    unpicked on our side by r400.
  - Mine coverage in losses (r50-1000): the home mine nearest our king holds 8.3 of our babies whenever any is
    there (covered 69% of rounds). The 2nd-6th home mines are covered 47/39/30/28/17% of rounds and the enemy takes
    440-940 cheese from each; the 3rd-6th are never visited in 25-50% of games.
  - Locally (u2g vs araid, ma2): 10.7 of 20 targeted gatherers sit on that one mine with 2.7 home mines known.
- u3e = u2g + KMINE_SKIP 2 (new switch, off in ratbot). While a gatherer knows 2+ home mines, mines with one of our
  kings within Chebyshev 4 drop out of its pick (the king picks up there itself, KING_ROAM 4, and cannot step onto
  babies). If every known home mine is king-held, it keeps them all.
  - Mechanism: frees the king's crowd to cover the 2nd-mth home mines.
  - Counters: rats on the king's mine vs other home mines (mtdist.py), coverage and first visit by home rank
    (minerank.py), king pickups, cheese picked by r400 and r1000, king bite/throw damage.
  - Doses: 0 (u2g) vs 2.
  - Falsifier: picked by r400 not up, or the king's mine loses more than the other home mines gain, or king
    bite/throw damage up.
- Delivery check: ks1 on the VM (u3e's 16 cells vs araid; u2g's control cells in ma2). Then the replica, after u3c.
- 08:47 PDT ks1 (u3e vs araid, 16 cells): 12-4 against u2g's 14-2 in ma2. DefaultMedium and starvation flipped to
  losses (t -1.46, just above the local reject at -1.5), so u3e goes to the replica after u3c.
  - Delivery was partial. Rats on the king's mine fell from 10.7-11.0 to 7.3-8.7 (r200-700, wins), but the most
    rats on one mine stayed at 12-13: with only 2-3 home mines known, the crowd moved to the next known mine.
  - Coverage of home mines 2-4 rose 3-6 points. Picked by r400 rose from 2684 to 2869 a game.
  - Mine discovery, not the pick, still bounds the spread.
- 08:53 PDT u3c block 1 (v4 set 1; 5108-5111, 5151-5152, 5168-5173, 5176-5179) vs u2g: 48 cells, 43 alike, D 5
  (+3 -2), net +1, t +0.45. That passes block 1 (rejects at t ≤ 0). Block 2 (v4 set 2) queued from 5195
  (waiter build/wait_u3c2.sh); reference 4737-4750, 5071-5078, 5080-5081, 5100-5101. u3e waits behind it.

## 2026-10-31 09:00 PDT: small maps: the opening overspends; u3f (RICH_MIN_AREA 901)

- In-band (u2g, 102 games) by map area: ≤ 900 tiles 5 W / 17 L; 901-1600 8 / 12; 1601-2500 23 / 17; > 2500 8 / 12
  (VM mapres.py). The small-map losses end at r436 (median), nearly all by king death; starvation is 294 HP of it.
- Opening (opening.py, small-map losses vs theirs):
  - babies 23.1 / 17.5 at r50, 16.4 / 15.6 at r150, 7.4 / 15.9 at r300
  - bank 1173 / 1531 at r50, 386 / 1069 at r150
- Births (deathwin.py): we spawn 30.5 in r0-49 to their 22.8. Over r0-300 they spawn more (58.8 to 56.1), from
  income kept up.
- Ledger to r150 on small maps (econagg.py with MAPS=..., 16 losses): spawn spend 2028 / 1435 (pricier spawns at
  high counts: 10 + 10 per 4 alive), picked 799 / 1153.
- Deaths over the game are even (61 / 63). The opening rush rarely lands on small maps: the enemy king lost 23 HP
  by r120 in losses, with at most 1.5 of ours within 4 of it (rushhit.py). The tiny rush wins vs AdamTan12 had 7-9.
- u3f = u2g + RICH_MIN_AREA 901 (new switch, off in ratbot). The RICH tier (cap 40 while the bank holds 1000) only
  applies on maps of 901+ tiles. The rushers (spawn index 4-15) are unaffected.
  - Mechanism: small maps keep the 18 cap from the start, so about 5 fewer babies at 60-70 each by r50, cheaper
    later spawns, and 300-600 more banked through r150 for replacements and upkeep.
  - Counters: small-map bank at r50/r150, babies at r150/r300, picked by r150, king starve HP, small-map results.
  - Doses: 0 (u2g) vs 901.
  - Falsifier: small-map bank at r150 not up by 200, or babies at r300 down, or the tiny/DefaultSmall rush wins lost.
- Local diag sm1 on the VM: u3f and u2g on 8 small maps (DefaultSmall, tiny, bed, evileye, canyoudig, arrows,
  Stash, cheesefarm) vs araid and abrk, both sides.
- 09:19 PDT u3f rejected locally. sm1: 16 of 32 to u2g's 21, 5 cells lost and none gained (abrk DefaultSmall A,
  cheesefarm B, evileye A; araid Stash B, cheesefarm A), t -2.4.
  - The tiny rush wins came 24-30 rounds later. bed and canyoudig played out identically.
  - The early bodies pay locally against the rush and raid archetypes, so the small-map gap is not the RICH
    tier alone.

## 2026-10-31 09:26 PDT: attackers hoard cheese and never deliver; u3g (ATK_DELIVER 50)

- Trap war (VM trapwhere.py, trapgeo.py, catsum.py on u2g's 102 in-band games):
  - In big-map losses, rats killed by the enemy: ours 46, theirs 31. Traps make most of the gap: 13.8 of our
    rats a game die to their traps, and none of theirs to ours (u2g lays none).
  - In losses they lay 42.5 rat traps a game and 39.3 fire on our babies, 65% on our half (home mines 42%).
  - 89% sit next to the layer, 84% toward our nearest baby, laid when that baby is at Chebyshev 2 (59%) or 1
    (17%). Lay to fire: median 1 round.
  - FACE_HOLD and WOUNDED_HP (holding off) were weak in u1l-u1n. u3d (our own reactive traps) goes to the replica
    after u3c.
- Starving with cheese in hand (brokecarry.py, hoard.py):
  - 4988 clockwisdom vs collegejunior: four attack-role rats held 200-1420 cheese each (2500 in all) at Chebyshev
    33-54 from our king while it starved from 600 HP to death (r800-860).
  - Over the in-band games, attack-move rats held 830 cheese at a time in wins and 319 in losses (sampled every
    50 rounds from r100), peaking at 1188 / 666 a game. Our babies died holding 1362 / 851 a game. In losses,
    975 of the enemy's pickups were drops.
  - Attackers never reach the delivery step (the attack branch comes first). Each unit of cheese slows a rat by
    1% (CHEESE_COOLDOWN_PENALTY), so by 500+ a hoarder barely moves.
  - t0l/u2n (ATK_CARRY 25: no pickups above 25) were neutral to positive on screen (t +1.83 / +0.34) and never
    reached the replica.
- u3g = u2g + ATK_DELIVER 50 (new switch, off in ratbot). An attacker carrying 50+ raw cheese delivers to the
  nearest king before attacking again.
  - Mechanism: the cheese attackers pick up reaches the bank while they are still fast (≤ 1.5x), instead of
    dropping to the enemy when they die or sitting unspent while the king starves.
  - Counters: cheese held by attackers (hoard.py), cheese on our babies at death, enemy pickups from drops,
    delivered, broke rounds, kills and enemy king damage.
  - Doses: 0 (u2g) vs 50.
  - Falsifier: delivered not up, or attacker-held cheese not down, or kills and enemy king damage down by more
    than 10%.
- Local diag hd1 on the VM: u3g on ma2's 16 cells vs araid (control: u2g's ma2 cells).
- 09:35: hd1 done. u3g 14 of 16, the same as u2g's 14 (D 2: +learn_your_lesson B, -starvation A, net 0).
  Delivery: attacker-held cheese per sample in wins fell from 31 to 4 (peak 259 -> 80; 200+ in 6 games -> 1), and
  cheese on babies at death from 1391 to 1165. Picked in wins about flat (11185 -> 11024). araid barely provokes
  hoarding locally (31 a sample vs 830 in the field), so the screen is weak; it passes (t 0) and goes to the
  replica after u3d.

## 2026-10-31 09:47 PDT: why in-band losses go broke; engine capability sweep (plateau step 2)
- Owner rule §3.2: in-band losses only (u2g's 32 v4 in-band matches, 102 games).
- Broke rounds (brokewhy.py: bank under 20 after r50): 33 of 58 losses go broke for 62 rounds on average, with 3.6 of
  our babies alive (often 0-1) while the enemy picks 843 cheese. Only 2 of 44 wins are ever broke. By then the army is
  already gone. Attack-role turns are 29% of our broke rat-turns; canyoudig 5075 had all 3-4 survivors attacking
  with empty paws from r175 while the king starved at (2,18), 600 -> 44 HP by r300.
- Second kings (formstat.py): formed before r200 in 8 games (2 W, 6 L). 5100 cheesefarm spent 578 rat-turns on
  formation over r50-150 (8 rats a round in r100-149); delivered there 195 vs their 690. 14 of 17 small-map losses
  never formed early, so formation is not the small-map story.
- Rat deaths caused by the enemy (catsum): the gap is traps. Big-map losses 46 vs 31 (traps 13.8 vs 0); small-map
  losses 41 vs 33 (traps 8.9 vs 0). Throws and cat deaths are even. u3d (reactive traps) is the field test of that.
- Engine check: a rat trap fires only when an enemy enters a tile in its 3x3 (move, drop, throw landing; the newly
  formed king's ring too). Damage 50, movement cooldown 30. Trap arrays are per team and MapInfo shows only our own;
  placement and removal never consult the enemy's traps. No engine leak reveals enemy traps.
- Capability sweep: 27 of 85 RobotController methods are never called. None is a new lever: own-trap removal,
  cooldown getters, occupancy and sensing helpers, indicators, disintegrate, resign. Cheese bites are already used.

## 2026-10-31 09:49 PDT: u3c passes block 2; v4 set 3 drawn; u2g's set-3 reference
- u3c (MINE_ANY + MINE_TIES) block 2 (v4 set 2, 5195-5196, 5203-5204, 5311-5322) vs u2g's 4737-4750, 5071-5078,
  5080-5081, 5100-5101: 48 cells, 41 alike, D 7 (+6 -1), net +5, t 1.89.
  - Gained AdamTan12_Finals dirtpassageway A/B, Icarus TheHeist A, spsquared evileye B, thewaxmango RUN A and
    learn_your_lesson B. Lost thewaxmango learn_your_lesson A.
  - Pooled with block 1: 94 cells, 83 alike, D 11 (+8 -3), net +5, t 1.51. That is between block 2's reject bar
    (1) and its accept bar (2), so u3c plays block 3.
- v4 set 3 (`research/panel/v4-set3.json`): spsquared and AdamTan12_Finals on their v3 set-3 maps (rose, TheHeist,
  rift; wallsofparadis, keepout, where_are_you); the six newcomers are drawn at send (YokoOno: Stash,
  5t4rv4t10n_1337, externalsustenance).
- u2g resubmitted (#135) for its set-3 reference: requests 4179-4182 sent (matches 5323+), 12 queued for the drain.
  Waiter build/wait_s3ref.sh. Then u3c is resubmitted for block 3 against it.

## 2026-10-31 09:52 PDT: the rich cap's bank threshold, the other way; u3h (RICH_BANK 1500)
- u2g's baby cap is 18, rising to 40 while the bank holds RICH_BANK 1000. In in-band losses we out-spawn the
  opponent early and are then out-banked:
  - Small maps: babies at r50 23.1 vs 17.5, bank 1173 vs 1531; bank at r150 386 vs 1069; babies at r300 7.4 vs 15.9.
  - Big maps: bank at r100 1106 in losses vs 1229 in wins (their 1378 / 1465).
- Dose evidence: u2v (RICH_BANK 500) was rejected on the replica, net -5. In its 4807 safelycontained game we had 36
  babies at r100 to their 19, fell to 3 by r600, and delivered 3615 vs 10889. 1500 has never been tried. u3f (no rich
  cap on small maps) lost the local screen (16/32 vs 21/32 against araid/abrk), so mass pays against the raider
  archetypes; the field's in-band bots trap massed babies (8.9-13.8 trap kills a lost game, ours 0).
- Pre-registration, u3h = u2g + RICH_BANK 1500 (one constant).
  - Mechanism: the opening stops spawning past 18 once the bank falls under 1500 (about r30-40) instead of under 1000
    (r50-70), so the r150 bank is higher and fewer replacements are bought at 60-110 each.
  - Counters: babies at r50/r100, bank at r150, spawn spend to r150, babies at r300, delivered.
  - Doses: 500 (u2v, rejected), 1000 (u2g), 1500.
  - Falsifier: bank at r150 not higher, or babies at r300 lower.
- Local diag rh1 on the VM: u3h on sm1's 32 small-map cells (araid, abrk) plus ma2's 16 (araid); control is u2g's
  cells in sm1 and ma2 (deterministic). The screen rejects only at t <= -1.5.
- 09:56: Grabs both ways (grabsum.py on the 102 in-band games): even. In losses they grab ours 77.9 a game and we grab
  theirs 79.9; in wins 39.6 and 51.3. 72% of grabs use the health rule (victim hp < grabber hp, threshold 0), 26%
  the blind side. 18% of grabs of our rats follow a trap hit on the victim in the 5 rounds before (theirs 0%). About
  65% of grabbed rats die within 15 rounds, half of them by throws. The one-sided exchange is the traps themselves
  (39 hits on our babies a lost game, 0 on theirs; 50 hp each, 1950 hp a game), not the grabs. Victims' decisions at
  the hit (trapwhere): carried or thrown 30%, faceEnemy 17%, attack 12%, war 11%, carry aim 10%, flank 8%.
- Watch for u3d: in losses our bank is 386-788 at r150, so its 500 trap floor may block most laying where it matters.
  If its field delivery is thin, the next dose is a lower floor (the opening stays guarded by TRAP_OPEN_TO 150).
- 09:58: Field trap census (trapcensus.py, in-band games; rat traps laid per game still running in the window):
  - Theirs in losses: r0-100 10.0, r100-200 7.2, r200-300 4.4, r300-500 9.2, r500-800 12.6, r800+ 11.7.
  - Theirs in wins: 7.6, 2.4, 1.8, 2.6, 3.0, 18.7. Their bank when laying: 1300-1500 early, 600-1000 r100-500.
  - Ours: 0 everywhere (u2g lays none).
  - u3d's opening floor (1500 before r150) sits above our early bank (1173-1464 at r50), so u3d lays almost nothing
    before r150, where the field lays 10. Read u3d's field delivery with that in mind.
- 09:59: u3c's field delivery (dirtpassageway vs AdamTan12_Finals, game 1, u2g 4749/4750 vs u3c 5203/5204):
  delivered by r150 390 -> 600 (A) and 420 -> 545 (B); the enemy's 585 -> 385 and 420 -> 240. As B the enemy's bank
  was 0 by r200. Spreading over every known mine, ties included, takes the contested mines' cheese from them.
- 10:08: Mechanism check tw1 (VM): u3d vs u2g head-to-head on ma2's 8 maps, both sides (16 games). u3d wins 9 of 16
  (Stash and wallsofparadis both sides; arrows lost both sides; the other four split by side). Over the 16 games u3d
  laid 445 rat traps and 396 hit (89%); rat kills 436 vs u2g's 342 (+27%); delivered 120k vs 102k (+17%); spawned
  1392 vs 1316. Big trap games: Stash A (87 laid, kills 53 vs 12, delivered 6930 vs 3061), wallsofparadis B (48
  laid, 13753 vs 4830). The trapper wins the exchange against a bot that lays none, which is the field's asymmetry
  against u2g, reversed. This supports u3d as the next replica candidate after u3c.
- 10:10: Two probe arms on u3d, built so the field test of u3d has its follow-ups sized, with tw2 on the VM (each vs
  u3d on ma2's 8 maps, both sides; the mirror baseline is 8 of 16):
  - u3i = u3d + FACE_HOLD (u1m's pairing; u1m on u0y vs u0q cut fires on us 808 -> 699 and deaths by enemy 620 ->
    514, and raised delivered 187k -> 220k, at 10 wins to 11).
  - u3j = u3d with RAT_TRAP_MIN_CHEESE 200 (from 500): the trap floor's dose arm, since our bank in in-band losses
    is 386-788 at r150.
- 10:15: u3h (RICH_BANK 1500) rejected at the local delivery check. rh1: 31 of 44 cells to u2g's 32 (D 7: +3 -4,
  net -1). Pooled over all games, the bank at r150 is 695 vs 556 (+139), but babies at r300 are 16.2 vs 18.3 and
  the bank at r500 is 854 vs 591. The pre-registered falsifier (babies at r300 lower) fires: the cheese saved early
  stays banked instead of buying bodies later. The rich cap's threshold now has three doses: 500 (rejected on the
  replica), 1000 (u2g), 1500 (falsified locally).
- 10:22: Rat deaths by window in the in-band field games (deathwin.py; per game, ours / theirs). In big losses
  (41 games), r0-500 we lose 39.4 rats to their 32.0. Traps account for 7.9 of that gap (2.0, 2.4, 1.1, 2.4 a
  window); bites, throws and the cat are even. Their traps: 9.2, 7.3, 4.0, 8.5 a window, ours 0. In small losses
  (17 games), r0-100 we lose 21.3 to their 17.0 (traps 2.8, bites 2.8 vs 1.0, the cat 7.1 vs 5.2; we out-throw them
  8.2 vs 10.6). In big wins the gap is smaller (traps 1.1, 0.3, 0.4, 0.4). After r500, losses or wins, they lose
  10.7-14.8 rats a game to the cat and we lose 2.5-5.8. Before r500 the rats we lose to traps are the whole
  big-map gap. That is the asymmetry u3d's own traps answer.
- 10:30: tw2 (VM, each arm vs u3d on ma2's 8 maps, both sides; the mirror baseline is 8 of 16):
  - u3i (u3d + FACE_HOLD): 7 of 16. The mechanism delivers: deaths by enemy 395 to u3d's 458 (-14%), kills 458 to
    395, 93 fewer spawns, delivered +2.5%. It doesn't turn into wins in the mirror.
  - u3j (trap floor 200): 7 of 16. It lays 19% more traps (466 to 391) with even kills (411 to 416).
  - Both are parked behind u3d's field test. u3i is the follow-up if u3d's field games show our rats stepping into
    reactive traps; u3j only if u3d lays few traps there.
- 10:31: sd1 (VM): u3d on sm1's 32 small-map cells (DefaultSmall, Stash, arrows, bed, canyoudig, cheesefarm, evileye,
  tiny; vs araid and abrk, both sides). The control is u2g's 21 of 32 on the same cells; local games are
  deterministic. This is a risk check before u3d goes to the field: small maps are where we win 5 of 22 in-band
  games, and the rush proxies punish spending on traps instead of bodies. Pre-registered falsifier: u3d 3 or more
  wins below u2g, which means its traps cost bodies against a rush on small maps. Counters: traps laid and fired,
  deaths by enemy.
- 10:36: Ablation screen ab1 (plateau step 2, "ablate everything carried"; VM, no replays kept). Six switches on in
  u2g that were never ablated, each turned off alone on u2g's recipe: u3k RAID, u3l STANDOFF, u3m FLANK, u3n POUNCE,
  u3o CARRY_AIM, u3p TRAP_RISK. Each plays u2g's 48 deterministic cells: ma2 (8 maps vs araid) and sm1 (8 small maps
  vs araid and abrk), both sides. u2g wins 35 of them. Pre-registered: 3 or more wins fewer and the switch earns its
  keep; no loss of wins makes it a removal candidate for the replica, behind the queue. The archetypes lay rat traps
  (araid and abrk laid 1247 against u2g on sm1's 32 cells, 39 a game), so the trap switches are priced here too.
- 10:35: VM disk: 346 MB free when ab1 started with replays kept (288 games, about 220 MB). I stopped it, deleted the
  replay files of 30 finished local runs from 10-30 17:00 to 10-31 02:00 PDT (gj1 ... sp3; results.jsonl and
  cells.tsv kept; local games are deterministic, so they regenerate), freeing about 1 GB (1.3 GB free), and
  restarted ab1 without replays.
- 10:42: sd1 (u3d on sm1's 32 small-map cells vs araid and abrk, both sides): 26 of 32 to u2g's 21. D 9 (+7 -2), net
  +5, t +1.67. Gains: bed A/B, canyoudig B and tiny B vs abrk, arrows B vs abrk, DefaultSmall B and canyoudig B vs
  araid. Losses: DefaultSmall B vs abrk, cheesefarm A vs araid (r913; u2g won on points at r2000). u3d laid 527 rat
  traps and 485 hit (92%); kills 1162 to u2g's 1030 (+13%), deaths by enemy 1325 to 1174, spawned 2640 to 2593.
  Delivered is lower (120.7k to 148.0k) because three r2000 points games became earlier king kills. The falsifier
  (3 or more wins fewer) doesn't fire. Small maps were the risk for spending on traps against a rush; it went the
  other way. u3d stays next in the replica queue after u3c.
- 10:47: u2g's v4 set-3 reference is in (#135, 5323-5338): 16 of 48 games. spsquared 5 of 6, Xzibit 4, run_away10 3,
  AdamTan12_Finals 2, YokoOno 1, thewaxmango 1, Icarus 0 (averyfineline, averystrangespace, mincemeat), nzjt 0
  (whatsthecatdoin, FreeTheCheese, cheesebottles). The set-3 draws are hard ones for us; per-cell pairing absorbs
  that. u3c resubmitted (#136) and its block 3 queued (requests 4203-4206 sent, 12 queued for the drain).
- 10:53: u2g's set-3 losses to the in-band pair Icarus and nzjt (0 of 12; endstate):
  - Icarus: averyfineline (king kills r206/r252, war opened r15-16) and mincemeat (r316/r317). On averystrangespace
    both sides went to r2000 in cooperation with 2 kings to their 1 and even delivery (4505 vs 4575), and both were
    lost on points. Cooperation weighs cat damage 0.5, so their cat damage share was over 60%. That makes 2 cells a
    cat-damage lever could flip.
  - nzjt: on whatsthecatdoin (40x40, kings in boxed corner rooms, two centre mines) we delivered 30 by r300 to their
    1414. Our babies walk to the centre mines (why M, 150-290 rat-turns per 25 rounds r50-175). At the centre
    (16-18, 19-21) nzjt grabbed ours 91 times to our 31 and threw them (throwLand deaths 6, 9, 10 a 50-round
    window). The king starved at r305.
  - Pooled over u2g's 102 in-band games (grabvwhy.py), our grabbed babies' last move code: faceEnemy 23%, carry aim
    (T: holding an enemy, lining up a throw) 17-18%, attack 14-15%, war 9-10%. About 13 grabs a lost game hit our
    carriers while they aim (CARRY_AIM). u3o in ab1 (CARRY_AIM off) prices that exposure locally.
- 11:00: Stalled attackers are a class (stuckatk.py, u2g's 144 field games): 39% of attack-move rat-turns stand
  within Chebyshev 3 of the rat's tile 50 rounds before, 1380-1630 a game in losses and wins alike. The worst cases
  are long cooperation games on big maze maps (averystrangespace, rose, rift, TheHeist, clockwisdom). On
  averystrangespace vs Icarus (4 field cells across sets 2 and 3, all lost on points) we never reach the cats: cat
  damage 0 to 87-289, with kings 2 to 1 and delivery even. One Icarus rat bit cat 3 23 times (r127-149, 230
  damage). Our 12 rushers stood in the dead end at (7-8, 22-26) from r500 to r1500.
- 11:00: New switch STALL_CAT (off): during cooperation, an attacker that has stayed within Chebyshev 3 of one tile
  for STALL_CAT rounds hunts cats for the rest of cooperation. It goes to a cat in view, else the cat last known,
  else the mirror-invariant centre tile. The turn's usual steps bite the cat and lay cat traps. Unit test: the
  centre tile maps to mirror images under all three symmetries on 60x60, 59x41 and 20x30.
  - Pre-registered for the snapshot u3q (STALL_CAT 100, zero arm u2g): mechanism = idle cooperation attackers
    convert to cat damage; counters = our cat damage and the attackers' 'g' turns in cooperation games; local
    check vs u2g on 8 stall maps (averystrangespace, rose, rift, TheHeist, clockwisdom, cheesebottles,
    where_are_you, uneruesansfin), both sides, against the u2g mirror on the same cells. Falsifier: our cat damage
    on averystrangespace stays 0, or the mirror's points games don't move.
- 11:05: u3q's local check runs as hc1 (16 cells, u3q vs u2g both sides) then hcc (the u2g mirror on the same 8
  maps), with replays kept. A first launch reused the name st1, which an old u0b vs s9s run already held: the
  guard only printed a warning. Those processes were killed by PID, st1's cells.tsv restored to its 16 lines, the
  new replays removed, and the stray control dir stc deleted; st1's old results are untouched. VM launches now
  test that the run name is free and stop if it is not.
- 11:11: two diagnoses on u2g's 50 in-band and set-3 field matches (150 games; VM kingkill.py, outnum.py):
  - Last-king deaths in losses (71 games): starvation 38, bites 16, throw hits 12, cat scratches 4, trap 1. Most
    starvations come with 0-3 babies alive and the bank at 0-50 forty rounds before, after the army is gone. This
    matches the 09:47 broke-round finding. Exceptions: rose vs collegejunior (r1630, 23 babies beside the king, bank
    0) and clockwisdom (r860, 17 of 19 babies 10+ away, the hoarders u3g targets).
  - Local numbers at our rats' deaths (allies vs killers within Chebyshev 4, two rounds before). In losses we lose
    41.5 rats a game to the enemy and they lose 28.8. Only 42% of our deaths come while outnumbered; 63% of
    theirs do. When our side has 2+ more rats nearby we still lose 12.1 a game (traps 5.1, throw hits 2.6, throw
    landings 2.3, bites 2.1), while they lose 3.8 a game when they have the extra rats.
  - So a "retreat when outnumbered" rule would miss most of our losses. The leak is our crowds: enemy traps and
    throws land where several of ours stand together. That points at u3c (spread over mines), u3d (traps of our
    own) and the ab1 price of TRAP_RISK, not at a force-ratio rule.

## 2026-10-31 11:19 PDT: u3c block 3 between the bars; block 4 (v4 set 4) next
- u3c (MINE_ANY + MINE_TIES) block 3 (v4 set 3; 5347-5350, 5352-5359, 5362-5365) vs u2g's set-3 reference
  5323-5338: 17 of 48 to u2g's 16. 48 cells, 45 alike, D 3 (+2 -1), net +1, t 0.58.
  - Gained YokoOno 5t4rv4t10n_1337 B and collegejunior FreeTheCheese B. Lost AdamTan12_Finals where_are_you B.
  - Icarus and nzjt stayed 0 of 6 each, as for u2g.
- Pooled over blocks 1-3: 138 cells, 124 alike, D 14 (+10 -4), net +6, t 1.60. That is above block 3's reject bar
  (1.5) and below the accept bar (2), so block 4 (final; rejects below 2) decides.
- v4 set 4 (`research/panel/v4-set4.json`): spsquared and AdamTan12_Finals on their v3 set-4 maps (evileye, Meow,
  knifefight; closeup, averyfineline, attack_on_cat); the six newcomers are drawn at send.
- u2g resubmitted (#137) for its set-4 reference. The hourly limit held all 16 requests, so they wait in the queue
  for the drain (waiter build/wait_s4ref.sh, matches 5368+). Then u3c is resubmitted for block 4.
- 11:27: hc1/hcc done. u3q (STALL_CAT 100) won 12 of 16 against u2g on the 8 stall maps; the u2g mirror (hcc) gives
  8. Against the mirror's per-side results: D 6 (+5 -1), net +4, t +1.63.
  - Gained averystrangespace B, clockwisdom A, rift A, rose A, where_are_you B. Lost rose B.
  - Delivery: on averystrangespace our cat damage went from 0 (mirror 0/0) to 1110 and 6437 against u2g's 0, both
    sides won on points. rift: 8097 / 10600 to u2g's 6410 / 4288 (mirror 4572 / 5677). clockwisdom A turned a king
    loss into a win.
  - Falsifier (cat damage on averystrangespace stays 0, points games unmoved) did not fire. u3q joins the replica
    queue after u3c's block 4, ahead of u3d: its local margin is the larger, and it targets the in-band points
    losses (Icarus averystrangespace).
