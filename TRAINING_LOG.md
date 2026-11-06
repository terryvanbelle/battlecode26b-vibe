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
- 12:07: STALL_CAT dose arm and side-effect screen (VM hq3, hq2, hq4).
  - hq3: u3r (STALL_CAT 50) vs u2g on the 8 stall maps, 10 of 16. Against the mirror (hcc): D 6 (+4 -2), net +2.
    Gained averystrangespace B, rift A, rose A, where_are_you B; lost uneruesansfin B, where_are_you A. Cat damage
    on averystrangespace 4545 / 1742 to 0. The 100-round dose (u3q, net +4) is the stronger arm.
  - hq2: both doses vs araid on ma2's 8 maps, 12 of 16 each to u2g's 14. The two lost cells are the same for both
    (jail B: u2g's king kill at r440 became a points loss at r2000; learn_your_lesson A).
  - hq4 (those cells replayed): u3q's jail game has no cat-hunt turns at all, so that cell diverged from the extra
    bytecode the stall bookkeeping costs (chaos), not from the hunt. learn_your_lesson's hunt turns ('g', shared
    with the FIGHT_EVERY cat fighters) are close to u2g's (171 vs 160 in r100-199).
  - u3q's local total: D 8 (+5 -3), net +2. It stays in the replica queue after u3c; u3r is parked as the weaker
    dose (reopen if u3q passes block 1 and a dose sweep is wanted).
- 12:15: u2g's v4 set-4 reference done (5387-5388, 5395-5396, 5402-5405, 5407-5414): 25 of 48. The newcomers'
  draws: YokoOno rose, tornadoes, Meow; run_away10 groveisbeingdesecrated, sunandmoon, arrows; Xzibit clockwisdom,
  dirtpassageway, Hike; collegejunior Nofreecheese, t_minus_five_minutes, cheesebottles; Icarus averystrangespace,
  mincemeat, tiny; nzjt sunandmoon, DefaultLarge, RUN. u3c resubmitted (#138) for block 4 (requests from 4286; 12
  queued for the drain; waiter build/wait_u3cb4.sh, matches 5415+).
- 12:16: head-to-head screen hh1 armed on the VM; it starts when ab1 ends, to keep the replica's load down (VM load
  12 on 8 cores). The queued candidates u3d, u3g, u3e and u3q each play u2g on 8 set-4 maps both sides, and u3d, u3g,
  u3e also on the 8 stall maps. Control: the u2g mirror (hcc, plus hhc for the set-4 maps). It ranks the replica
  queue after u3c by local margin against the incumbent itself, a second view beside the archetype screens.

## 2026-10-31 12:33 PDT: ablation screen ab1 priced; u3s (RISK_MIN_AREA 901)
- ab1 done: each ablation on u2g's 48 cells vs araid/abrk (ma2 + sm1; 44 distinct cells, u2g wins 32).
  - u3k (RAID off): 32, identical games (transfer equal to the unit). RAID never fires locally, so the screen
    cannot price it.
  - u3l (STANDOFF off): 31 (+4 -5), transfer -9%. Inconclusive.
  - u3m (FLANK off): 28 (+5 -9). Earns its keep.
  - u3n (POUNCE off): 32 (+6 -6), transfer -6%. No loss: a removal candidate (bytecode only).
  - u3o (CARRY_AIM off): 24 (+5 -13). Earns its keep.
  - u3p (TRAP_RISK off): 35 (+10 -7). By map area: on maps of 900 tiles or fewer +9 -5 (bed 4, canyoudig 2,
    DefaultSmall, arrows 2 vs evileye, tiny, cheesefarm, starvation (875 tiles) A and B); on bigger maps +1 -2
    (learn_your_lesson B vs DefaultMedium A and B).
  - Each ablation moves 9-18 cells of 44 with the transfer within 10%, so the local cells are chaotic. A margin
    of 3-4 is about one standard deviation there.
- u3s = u2g + RISK_MIN_AREA 901 (new switch, off in ratbot). TRAP_RISK's mask and its observations apply only on
  maps of 901+ tiles. 900 is the in-band small-map line, fixed before the ablation (5 of 22 won there).
  - Mechanism: on small maps the mask (tiles within 1 of the front tiles of every enemy baby seen in the last 150
    rounds) covers much of the board, so our rats detour or stall where the trap risk it prices is lower than the
    tempo it costs.
  - Counters: small-map results; our rats' trap deaths and the enemy's deaths on small maps.
  - Doses: 0 (u2g) vs 901.
  - Falsifier: the rma screen loses small-map cells on balance, or our small-map trap deaths rise by more than the
    enemy deaths we gain.
- Local screen rma on the VM: u3s on ab1's 48 cells (control: u2g's ma2 + sm1).
- Process slip: snapvar edits bots/ratbot/Cfg.java in place while it snapshots, and I ran it during a full
  unit-test run. The run compiled the altered Cfg and failed 11 config-dependent tests (rush, roles, throws).
  Cfg was restored afterwards; the quick BotTest had already passed (1737/1737), and the repeated full run
  passed (exit 0). Never run snapvar while the unit tests or a progress refresh are running.
- 12:40: field evidence for u3s's mechanism (VM movert.py on u2g's 150 in-band and set-3 games). After cooperation
  ends, the share of baby rat-turns on which the rat changed tile:
  - Small maps (<= 900 tiles), losses (23 games): ours 64.0%, theirs 77.4%. With no enemy rat within Chebyshev 3:
    ours 69.4%, theirs 82.1%.
  - Big maps, losses (56): ours 76.5%, theirs 73.5%; with no enemy within 3, 80.5% vs 74.9%. Wins are alike.
  - So only on small maps do our rats stand still more than theirs, and by 13 points even with no enemy near. This
    fits a mask that marks most of a small board, though crowding jams could do it too. Counter for rma and the
    field: the same moved share on small maps.
- 13:00: rma done. u3s (RISK_MIN_AREA 901) won 36 of 44 to u2g's 32: D 14 (+9 -5), net +4, t +1.07.
  - Every changed cell is on a map of 900 tiles or fewer. Gained bed A/B against both archetypes, canyoudig B
    against both, DefaultSmall A, arrows A (araid) and B (abrk). Lost evileye B, tiny A, arrows A (abrk),
    starvation A/B.
  - The bigger maps replay u2g's games (DefaultMedium's two u3p losses are gone).
  - It passes the screen (rejects only at t <= -1.5) and joins the replica queue after u3c.

## 2026-10-31 13:06 PDT: u3c rejected at block 4 (near-miss); plateau trigger; u3q on the replica
- u3c block 4 (v4 set 4; 5428-5431, 5451-5452, 5464-5465, 5473-5476, 5479-5482) vs u2g's set-4 reference
  (5387-5388, 5395-5396, 5402-5405, 5407-5414): 48 cells, 43 alike, D 5 (+3 -2), net +1, t 0.45.
  - Gained AdamTan12_Finals closeup B, run_away10 arrows A, spsquared evileye B. Lost AdamTan12_Finals
    averyfineline B, nzjt RUN A.
- Pooled over the four blocks: 176 distinct cells, 158 alike, D 18 (+12 -6), net +6, t 1.41. Block 4 is final and
  rejects below 2: u3c is rejected. With t > 0 it is a near-miss (§2), eligible for a pairing at plateau step 5.
  Closure: refuted at t 1.41 (above the |t| < 1 resolution line). Reopening condition: a pairing with another
  near-miss of an independent mechanism, or a block design powered for 1-2 cells in 20.
- That is 8 straight rejects (u2s, u2v, u2w, u2y, u3b, u3f, u3h, u3c): the plateau trigger. Step 2 (ablation and
  capability sweep) is done (ab1; 09:47). Step 1, the correctness audit, starts now, one lens at a time.
- u3q (STALL_CAT 100) submitted (#139) for block 1 on v4 set 1 against u2g's references. Requests from 4352; 12
  queued for the drain; waiter build/wait_u3qb1.sh (matches 5483+). Queue after it: u3s, u3d, u3g, u3e (hh1, the
  head-to-head screen, will rank them).
- The set-4 reference replays (5387-5414) had not been fetched before the first paired read, which refused to score
  (contest.py: "NO REFERENCE"). They are fetched now.

## 2026-10-31 13:22 PDT: audit lens 4 (what replays show): the king plugs rose's pocket; u3t (KING_PLUG)
- Lens 3 (shared state and stale data): the Comms layout is consistent and the stamps wrap correctly. One minor
  finding: MINE_SLOTS 16 caps the team mine list, which truncates it on 6 of 74 maps (TheHeist 18,
  cheeseguardians 18, clockwisdom 20, externalsustenance 24, groveisbeingdesecrated 18, superioristhmus 20).
  Each gatherer's own myMines list covers the overflow (pickMine merges both), so no fix for now.
- Lens 4: stuck_report on u2g's set-4 field games. Stuck rats used 2.0% of baby turns overall, but on rose vs
  YokoOno (as A) they used 29.1%: rats at (13-15,2-4) walking to a mine ('M') for ~600 rounds, touching our king.
  averystrangespace (785-1205 rounds on 'M', far from the king) and cheesebottles (5.9%) also rose.
- The u2g vs u2g mirror on rose (hcc) reproduces it: 12.3% of 91738 baby turns lost, the same tiles on both sides.
  - The stuck rats target mine (2,7) (indicator nt=2,7). The king, also bound for (2,7) (hm=2,7), started at
    (16,1) and walked (16,2) r52, (15,3) r58, (14,3) r62, (14,2) r75, (13,1) r84. It stays at (13,1) to r2000 (km=z:
    no step closer, no dirt to dig), because its 3x3 cannot pass the 1-wide corridor at x=9.
  - At (13,1) its body fills x 12-14, y 0-2. That is the only way out of the start pocket (x 13-20, y 0-5).
  - All 20 babies spawned in r1-99 were born in the pocket. Over the game, 42% of side A's 39932 rat-turns were
    spent in the box x 15-20, y 0-5. Spawns after r84 still pick the pocket side (it is nearest the centre): 11 of
    15 in r300-499.
- This is the same geometry as s6r's KING_PLUG note (field 1796 vs spsquared). s6r was never screened: its base
  (S4D) was superseded. Its rose diagnostic against araid lost both sides, with the unplugged pocket's rats jamming
  the 1-wide corridor head-on.
- MapCensus gains a pocket column: open spawn-ring tiles of king A's start 3x3 that reach under half of freeTiles
  with that 3x3 walled. No map has one at the start, so on rose the plug comes from the king's walk alone.
- u3t = u2g + KING_PLUG (existing switch; snapvar with the u2g recipe; diff vs u3s is the two switches only).
  Pre-registration:
  - Mechanism: the king stops short of a ring-splitting tile, so the pocket's rats can reach the mines.
  - Counter: rose's pocket rat-turns and stuck share.
  - Dose: on or off (the zero arm is u2g).
  - Falsifier: rose pl1 (u3t vs u2g, both sides) shows no fall in pocket rat-turns, or the corridor jam that s6r
    saw returns.
- 13:26: audit lens 2 (rules and API against the engine source) is back from a read-only reviewer. Six findings.
  I re-read A, B and D in the engine myself and they hold. C, E and F I spot-checked at the cited bot lines.
  - A. The engine leaks the map's symmetry on round 1, to one seat only. For a robot with chirality 1,
    GameWorld.java:767-781 reverses the sense enumeration (senseNearbyMapInfos, senseNearbyRobots,
    getAllLocationsWithinRadiusSquared): y for HORIZONTAL, x for VERTICAL, both for ROTATIONAL. Babies inherit
    the king's chirality, and on 40 of the 43 bundled maps exactly one king has chirality 1.
    - Not used. Only the chirality-1 seat would learn the symmetry early, so our play would differ by seat. The
      owner's rule is that every move mirrors when the sides switch. Flagged for the owner.
  - B. Throw flight depends on execution order. The bot's FLIGHT_DMG table (Baby.java:530-534) assumes the 8-tile
    flight. The engine (InternalRobot.java:1141-1158, flight steps in processBeginningOfTurn, hasTraveledIDs
    cleared at round end) flies a victim that runs after its thrower only 6 tiles: 42/30/26/22/18 at tiles 2-6,
    then a ground landing for 10. The bot cannot tell which case it is in, so a king 7-8 tiles away scores 54-66
    and gets thrown at for nothing about half the time. Every hit from tile 3 on is also overrated by 8.
  - C. TrapRisk (TrapRisk.java:36-43) marks only enemy babies' front tiles. Enemy kings place rat traps on the 16
    ring tiles around their 3x3 (RobotControllerImpl.java:339-345), as ours do.
  - D. Bites overpay cheese. The engine adds ceil(sqrt(cheese)) damage (InternalRobot.java:609-612), so +k costs
    (k-1)^2+1, not k^2: +3 costs 5 (not 9), the king's +10 costs 82 (not 100). Affects Baby.finishCheese and
    King.biteCheese.
  - E. The king never bites an adjacent enemy king. King.bite filters on canAttack(centre), but the enemy centre
    is at dist2 >= 9 when the bodies touch, while its body tiles at dist2 4-8 are biteable. The babies already
    clamp to a body tile; the king does not.
  - F. SQ_CAT squeaks near our king (Baby.java:1092) reach cats. During ATTACK a cat turns toward the first
    squeak it hears at each new waypoint.
  - Ranked by expected effect: B, then C, then D/E/F. D, E and F are each below what a panel block can detect,
    so they wait to ride with a pairing (§6 step 5) rather than take replica blocks alone.
- 13:31: pl1 (u3t vs u2g on rose, both sides). The same seats won as in the u2g mirror (B both times), so u3t
  does not deliver alone and is parked.
  - u3t's king stopped at (14,2)/(14,3) instead of (13,1), but the pocket rats still stood touching it: their A*
    routes through the king's body. Stuck share 11.0% of baby turns vs 12.5% in the mirror; box rat-turns 38% vs
    42%.
  - Field census (stuck_report on u2g's 150 in-band and reference field games, stucksum.py): 3.5% of baby turns
    are stuck. Of the listed stuck rounds, 73% (35199) are rats walking to a mine within 3 of our king: rose 21142,
    EscapeTheNight 6696, rift 2079, fullofsecrets 1869. That class is Flow's A* routing through our own king.
  - u3u = u2g + KING_FLOW (existing switch: our kings' bodies are walls for flow paths, target at a king
    excepted). s5e tried it on an older base and lost at block 1 (net -4, t -1.07). Pre-registration:
    - Counter: stuck rounds within 3 of our king on rose, EscapeTheNight, rift and fullofsecrets.
    - Dose: on or off.
    - Falsifier: kf1 (those 4 maps, both sides, plus u2g mirrors) shows no fall in king-near stuck rounds.
    - If rose still jams (the king plugs at (13,1), so no flow path exists), the pair u3t+u3u is next.
- 13:46: kf1 done (u3u = u2g + KING_FLOW vs u2g on rose, EscapeTheNight, rift and fullofsecrets, both sides,
  plus u2g mirrors). Mirror winners: B on all four maps.
  - u3u gained EscapeTheNight A and fullofsecrets A, lost EscapeTheNight B: net +1 of 8 cells.
  - The counter (listed stuck rounds within 3 of our king, stuckside.py) is mixed. u3u vs the two u2g games on
    the same side:
    - Down on rift A (599 vs 1779/2394), rose A (528 vs 2845/4007) and EscapeTheNight B (906 vs 1040/1311).
    - Up on rift B (3548 vs 692/711).
    - Mixed on EscapeTheNight A (1014 vs 145/3953) and rose B (2311 vs 1446/3029).
  - Rift B shows the cause KING_FLOW cannot reach. From r941 to r2000 four rats sit at (53-54, 12-13), boxed in
    by the wall row y=14, the wall at x=52 and three of our kings' bodies ((53,10), (56,12), (57,9)). Those kings
    stand in state 'z' (no step closer, no dirt to dig), walking greedily toward home mines (46,15) and (53,17)
    across the wall row. The rats target (53,17).
  - So the stuck-near-king class has one root: a king's greedy walk toward a mine its 3x3 cannot reach ends at a
    wall or corridor mouth, and parks its body where it shuts our babies in. Rose (one king, the corridor mouth)
    and rift (three kings, a corner) are both instances. KING_FLOW only fixes the A* half (EscapeTheNight).
  - Basics battery (§1.11): the movement row (no unit stalled N turns) fails, with rats stalled 600-1200 rounds,
    so candidates from it go first. u3u is a basics repair. It joins the replica queue ahead of the strategy
    candidates once hh1 has ranked them, and accepts at t >= 0 after block 2.
- 13:50: hh1 scored (h2h.py: each candidate vs u2g on the 8 set-4 maps, plus the 8 stall maps for u3d/u3e/u3g,
  both sides, against the u2g mirrors hhc and hcc).
  - u3d: 17 of 32, D 11 (+6 -5), net +1, t +0.30.
  - u3e: 16 of 32, D 4 (+2 -2), net 0.
  - u3g: 15 of 32, D 5 (+2 -3), net -1.
  - u3q: 6 of 16 on the set-4 maps, D 2 (+0 -2), net -2 (its stall-map hc1 was +4).
  - Self-play head-to-head is weak evidence; none is near the -1.5 screen reject.
  - Replica queue after u3q, failing battery row first: u3u (movement repair), then u3s (screen t +1.07), u3d,
    u3e, u3g.
- 14:00: kv1 (u3v = u2g + KING_FLOW + KING_PLUG vs u2g on rose, rift and EscapeTheNight, both sides). The pair
  delivers on the counter where u3u alone did not.
  - Listed stuck rounds within 3 of our king: 0 on rose and rift, both sides (u2g in the same games: 3158/5017
    on rose, 3077/1419 on rift).
  - Stuck share: rose A 4.6% (u2g 26.0%, mirror 12.5%), rose B 10.3%, rift A 0.4%, rift B 0.0%.
  - EscapeTheNight replays u3u's games (KING_PLUG does not act there).
  - Results vs the mirror: gained rose A and EscapeTheNight A, lost rift B (u2g won on points as A) and
    EscapeTheNight B. Net 0 of 6.
  - So u3v replaces u3u as the movement repair. kfs (u3u on rma's 48 cells vs araid/abrk) was stopped at 18 of 48
    and the same cells restarted as kvs with u3v, scored against u2g's control results (ctl_ab1).

## 2026-10-31 14:02 PDT: u3q rejected at block 1; u3d to the replica
- u3q (STALL_CAT 100) block 1 on v4 set 1 (5494-5497, 5504-5507, 5521-5522, 5533-5534, 5546-5549) vs u2g's set-1
  reference: 48 cells, 45 alike, D 3 (+1 -2), net -1, t -0.58. Block 1 rejects at t <= 0, so u3q is rejected.
  - Gained spsquared superioristhmus B. Lost spsquared superioristhmus A and collegejunior clockwisdom B.
  - The averystrangespace cat-hunt gain seen locally (hc1) has no cell on set 1. Reopening condition: a set
    whose cooperation-to-r2000 maps (averystrangespace, rose) hold enough cells to see it.
- That is 9 straight rejects. The plateau ladder goes on: the step-1 audit gave u3v (movement repair, local
  screen kvs running).
- The replica would sit idle until kvs finishes, so u3d (reactive baby rat traps; sd1 +5, hh1 +1) goes first,
  for block 1 on v4 set 1. It targets the largest measured asymmetry: in u2g's in-band games we lay no rat traps
  at all, while the opponents lay 9-32 per window and their traps kill 9-14 of our rats per lost game (none of
  theirs die to traps).

## 2026-10-31 14:19 PDT: u3v passes its screen; how in-band losses lose the economy
- kvs (u3v = u2g + KING_FLOW + KING_PLUG on rma's 48 cells vs araid/abrk), scored against u2g's control ctl_ab1:
  30 of 44 cells to 32, D 16 (+7 -9), net -2, t -0.50. The screen rejects only at t <= -1.5, so u3v passes.
  - Gained bed A/B vs abrk, and bed B, DefaultSmall A/B, arrows A and learn_your_lesson B vs araid.
  - Lost DefaultSmall A, arrows A and evileye A vs abrk, and DefaultMedium A/B, arrows B, learn_your_lesson A,
    starvation B and tiny B vs araid. Delivered 295.4k to 285.4k.
  - u3v is a basics (movement) repair, so it accepts at t >= 0 after block 2. It goes to the replica next, after
    u3d's block 1.
- How in-band losses end (howend.py over inband2, 102 games): 50 of 58 losses are king kills, and in 24 of those
  the last king starved (kingkill.py). Our bank at R-40 is 0-100 in most of them.
- Cheese budget (budget.py, per game, ours/theirs):
  - By r100 in big-map losses we spawn 33 babies for ~1490 against their 25 for ~930, on equal income (243/211).
  - By r300 in small-map losses they have delivered 2018 to our 1164.
- Who picks at which mine (pickctx.py, r50-1000, cheese a game near mines):
  - Losses: they pick 1742 a game at our home mines (wins: 421). 75% of that is picked with none of our rats
    within Chebyshev 4 of the mine; 35% by a lone enemy rat.
  - Meanwhile 34% of our home picks (wins: 47%) come with 4+ of our rats at one mine and no enemy.
  - At their own mines they pick 4560 a lost game, almost all with none of ours near; we pick 340 there.
- 5100 cheesefarm vs nzjt (minework.py, r50-400): the two mines equidistant from both starts, (26,12) and (26,17),
  gave them 2220 and us 0 (no rat of ours went within 4). u2g counts tied mines as theirs (MINE_TIES off).
  - That is u3c's mechanism (MINE_ANY + MINE_TIES): the nearest miss of the plateau (176 cells, D 18 (+12 -6),
    net +6, t 1.41 at the final block).
- 14:27: raidctx.py (inband2, r50-1000): the enemy's uncontested picks at our home mines are quick, small hits.
  - Losses: 1456 a game (wins 400), 26 visits a game (wins 7). 38% of visits leave within 5 rounds. 55% of the
    picks come with a teammate within 4 of the picker.
  - Our nearest baby is more than 12 tiles away for 51% of that cheese, and none of ours is within 8 for 71%.
  - By mine rank from our first king: 2nd 29%, 3rd 17%, 4th 14%, 5th+ 32% (wins: 70% at the 5th+).
  - So these raids follow from a thin army (losses are at 1-8 babies by r300), not from a defence that is there and
    fails to react. A raid-response switch would have few rats to send.
- VM disk was down to 106 MB free. I deleted the replays of 20 finished local runs (rb2, rb3, gk2, gc1, gc2, sl1,
  cv1, cv2, tw1, tw2, rh1, sf1, ws1, hc1, hcc, hd1, ks1, sd1, ra_u2o, hq4; ~900 MB). Their results.jsonl files stay,
  and the games are deterministic, so they can be regenerated. Now 1005 MB free.
- 14:29: u3c's delivery on the field (pickctx.py, its sets 1-2 blocks, 32 matches, vs u2g's 52 reference matches
  on the same sets).
  - In wins u3c picks more everywhere: home 4569 a game vs 3842, axis 906 vs 626, away 748 vs 545.
  - In losses it picks about as u2g (home 2794 vs 2975, away 484 vs 313), and the enemy still takes 1612 a game at
    our home mines (u2g 1519). So its gain came from widening wins, not from turning losses.
- Roles (roles.py: tag bit 5 = fighter, any 'a' move = attacker): in in-band losses gatherers die 20.8 of 24.1 born
  a game (wins 12.4 of 24.2). Live at r300: 9.0 gatherers, 6.3 fighters, 3.1 attackers (wins 9.8 / 8.2 / 5.5).
  Attackers are not the bulk of the army after r100, so trimming the rush is not the lever (u0o RUSH_TO 12 lost net -7).
- Deaths (outnum.py over u2g's 90 field losses): ours 41.5 a game, theirs 28.8. Traps make 11.3 of our deaths and
  none of theirs, so traps are most of the 12.7 gap. The rest is bites (6.5 vs 3.9); throw deaths are even (18.2+4.8 vs 19.2+5.6).
  Only 25% of our trap deaths come while outnumbered. Their reactive traps hit us when we press at even numbers or
  better. u3d, now on the replica, lays our own.
- 14:33: when is the game decided (leadpred.py, inband2)? The side ahead on rat kills by r50 wins 70%; in losses we
  are 2 kills down by r50 and 3 by r100. A baby-count lead predicts nothing (we lead it by 5-8 in wins and losses
  alike through r300). The bank always trails (-180 to -720).
  - Early deaths (earlydeath.py, r1-100, in-band losses): ours 11.0 a game, theirs 8.1. Of ours, 6.2 are attackers
    (wins: 3.2), 4.6 die within 8 of our king and 2.8 on their half. Theirs die mostly near our king (3.8).
- u3d's partial block 1 (12 of 16 matches, 36 cells): D 2 (+1 -1), net 0. Field delivery (deathwin.py on those
  12 matches):
  - It lays 8.9 traps a game by r100 in big-map losses and 10.6 on small maps (u2g 0). Our traps kill 2.6 and 3.4
    enemy rats there (u2g 0), about what theirs kill of ours (1.8 and 2.8).
  - r0-100 deaths in big-map losses are 9.6 ours to 10.1 theirs (u2g's in-band: 11.1 to 8.2).
  - So the trap gap closes, but no result has turned yet.

## 2026-10-31 14:45 PDT: u3d passes block 1; u3v to the replica; u3w (PEACE) pre-registered
- u3d (reactive baby rat traps) block 1 on v4 set 1 (5557-5560, 5563-5566, 5573-5576, 5598-5599, 5618-5619) vs
  u2g's set-1 reference: 48 cells, 45 alike, D 3 (+2 -1), net +1, t +0.58. Block 1 rejects at t <= 0, so u3d goes
  on to block 2.
  - Gained AdamTan12_Icarus uneasy_alliance B and VarunVejalla DefaultMedium A. Lost spsquared superioristhmus A.
- u3v (movement repair, screen pass) is submission 141 and goes first, for its block 1 on v4 set 1 (16 queued).
  u3d's block 2 follows. Waiter build/wait_u3vb1.sh (matches 5620+).
- The plateau trigger is active (9 straight rejects) and a structural attempt is overdue (§3). u3w is one: a
  doctrine change, not a dose.
  - Change: u2g + PEACE (new switch). We never end cooperation. While it lasts, no baby of ours bites or grabs an
    enemy rat or king. RUSH_BREAK, BREAK_ROUND 30 and the attackers' king bites wait for the war. Only the opponent
    can start it.
  - Mechanism, from the engine: a bite on an enemy rat or king, a grab of an enemy, or a fired rat trap ends
    cooperation (the trap's owner counts as the backstabber). The backstabber then loses cat traps for 100 rounds,
    while the victim keeps them. Cooperation also scores differently: cat damage 0.5, kings 0.3, transfer 0.2,
    and the game ends when the last cat dies.
  - Census (whostab.py, u2g's in-band games, 102 of them): in big-map losses the opponent opened the war in 20
    (median r33), we did in 14 (r31), and nobody in 7. In big-map wins it was 20 (r68), 10 (r66) and 9. On small
    maps: losses 11 by them (r20) and 5 by us (r15); wins 1, 3 and 1. Games nobody opens were won 11 of 18. Our
    r1-100 deaths in losses are 11.0 a game to their 8.1, and 6.2 of ours are attackers (3.2 in wins).
  - Counters: who opens the war, and when; our r0-100 deaths against theirs; the kill lead at r50 and r100.
  - Doses: the zero arm is u2g. A middle arm (RUSH_BREAK off, keeping BREAK_ROUND 30) is held back unless u3w
    delivers but nets near zero.
  - Falsifier: we still open the war in any field game (a delivery bug), or our r0-100 deaths in big-map losses
    stay 2+ above theirs, or the war start in the cells u2g opened does not move later.
  - Instruments: the local screen pw1 (kvs's 48 cells vs araid/abrk, control ctl_ab1) rejects only at
    t <= -1.5. araid and abrk break the truce themselves at r22-36, so PEACE only changes the first ~30 rounds
    there. The truce check pw2 (vs u2g and s7d, 8 maps x 2 sides) must show backstabBy never on u3w's side. The
    replica block protocol decides, after u3v and u3d's block 2.
- Build check: u3w differs from a same-code sibling (pb2, u2g's switch set) in the PEACE line only. BotTest
  1737/1737 with PEACE on and off; the unit tests pass.
- 14:48: VM disk was at 422 MB free (the replica's own storage is 6.7 GB and grows with the ladder). I deleted the
  39 replay dumps in build/dumpcache that no list in ~/diag26b references (587 MB; all can be rebuilt from kept
  replays). Now 1008 MB free. progress/ refreshed after u3d's block 1.

## 2026-10-31 15:22 PDT: owner prompt 21 (break symmetry for an edge); u3w passes its screen; u3x built
- Owner prompt 21: "If you can get an advantage by breaking symmetry, then do so." This answers finding A of audit
  lens 2, the chirality leak, parked at 13:26 because of the side-symmetry rule. TRAINING_ALGORITHM §6 now keeps
  side-symmetric play as the default (so mirror self-play still exposes bugs) and breaks it on purpose, behind a
  switch, where a seat-dependent engine behaviour gives one seat information or tempo.
- u3w (PEACE) local screen pw1 (kvs's 44 scored cells vs araid/abrk, control u2g ctl_ab1): 36 of 44 to 32, D 8
  (+6 -2), net +4, t +1.41. That passes (the screen rejects only at t <= -1.5).
  - Gained: bed A/B and canyoudig B vs abrk; bed B, canyoudig B and learn_your_lesson B vs araid. Lost:
    DefaultSmall B vs abrk and tiny B vs araid.
  - Truce check pw2: u3w broke the truce in 0 of 80 local games. araid (32), abrk (16) and u2g (16) broke it
    themselves (median r26-36), and s7d never did.
  - Head-to-head vs u2g: 12 of 16.
  - vs s7d: 11 of 16, against u2g's 12 of 16 (control pw3, where u2g breaks the truce at r28 every game). Peace
    games end when the last cat dies (r77-932) and are decided on points, so xfer falls (69.7k vs 149.6k). In a
    peace game our 12 rushers wait at the enemy king doing nothing.
  - Delivery is as designed. u3w goes to the replica right after u3v's block 1. It is ahead of u3d's block 2,
    since it is the overdue structural attempt and has the stronger local signal.
- u3x = u2g + CHIRAL_SYM (new switch), the chirality leak used on purpose.
  - At birth every robot calls getAllLocationsWithinRadiusSquared(own tile, 2). The engine lists x ascending,
    then y ascending, and for a chirality-1 robot it reverses y on HORIZONTAL maps, x on VERTICAL maps and both
    on ROTATIONAL ones (GameWorld.java:767-781). Sym.chiral reads that order (any visible subsequence keeps it)
    and merges the one symmetry it shows. A robot that sees the natural order learns nothing.
  - Babies inherit the king's chirality, and exactly one king has chirality 1 on 40 of the 43 bundled maps. So
    one seat knows the symmetry on round 1. On u2g's field game-sides the mask stays ambiguous ~40% of the time,
    and the median round it becomes unique is 103.
  - Every reader of the mask gains: the rushers' enemy-king target, the king's enemy-mine and raid picks, and the
    gatherers' mine halves.
  - Counters: the round the mask becomes unique, per seat; attacker turns to the first enemy-king sighting.
  - Dose: on or off (zero arm u2g).
  - Falsifier: on the chirality-1 seat the mask is not unique by round 2 in real games (a reading bug), or the
    attackers' time to first sight the enemy king does not fall.
  - BotTest 1744/1744, now with 7 Sym.chiral checks on engine-order arrays (natural, each symmetry, edge
    clipping, one row or column, a filtered subsequence); the unit tests pass. u3x differs from a same-code
    sibling in the CHIRAL_SYM line only. Engine check cx1 (u3x vs u2g on cheesefarm HOR, tiny VER and evileye ROT,
    both seats, with tsv dumps) is running on the VM.
- 15:24: u3v (movement repair) block 1 on v4 set 1 (5639-5642, 5659-5666, 5671-5674) vs u2g's set-1 reference:
  48 cells, 36 alike, D 12 (+7 -5), net +2, t +0.58. Block 1 rejects at t <= 0, so it passes.
  - Gained: AdamTan12_Icarus TheHeist A and uneasy_alliance A/B; VarunVejalla knifefight A and
    t_minus_five_minutes B; spsquared bed A; collegejunior rose A (rose is the parked-king stuck map u3v targets).
  - Lost: Xzibit attack_on_cat B, nzjt peaceinourtime A, spsquared fullofsecrets A, collegejunior clockwisdom B.
  - A basics repair accepts at t >= 0 after block 2, so its block 2 (v4 set 2, vs u2g's set-2 reference) goes
    next, ahead of u3w. If u3v is accepted it becomes the base, and u3w, u3x and u3d are rebuilt on it (u3v + one
    switch) and re-screened before their replica blocks. Waiter build/wait_u3vb2.sh (matches 5675+).
- 15:30: engine check cx1 (u3x vs u2g, both seats, tsv dumps, 'sym=' in the king's indicator). The reading is
  right on all three symmetry types. On cheesefarm (HORIZONTAL), tiny (VERTICAL) and evileye (ROTATIONAL), seat B
  holds chirality 1. With u3x on seat B the king's round-1 mask was 2, 4 and 1 respectively, the true symmetry.
  - u2g on seat B first had a unique mask at r82, r34 and r8.
  - u3x on seat A learned nothing early (r40, r24, r7, the same as u2g on seat A), as expected.
  - Results (6 games, too few to read): cheesefarm and tiny went by seat; on evileye u2g won both.
  - The cx1 replays and dumps were deleted afterwards (98 MB; rows kept).
- u3y = u3w + STALL_CAT 20, pre-registered (attempts.csv). u3w's 16 peace games vs s7d end when the last cat
  dies, and cat damage weighs 0.5 there. In the 5 losses we did 2224-4060 cat damage to their 3940-5776, while our
  rushers waited at their king. yc1 (vs s7d) and yc2 (screen cells) are running.
- 15:45: u3x (CHIRAL_SYM) screen cx2 (kvs's 44 scored cells vs araid/abrk, control u2g ctl_ab1): 35 of 44 to
  32, D 9 (+6 -3), net +3, t +1.00. That passes.
  - Every changed cell is on seat B, the chirality-1 seat on these maps. The seat-A cells play exactly as u2g's,
    so the switch acts only where the engine leaks.
  - Gained: bed B and canyoudig B vs abrk; DefaultSmall B, bed B, canyoudig B and learn_your_lesson B vs araid.
    Lost: DefaultSmall B vs abrk; arrows B and starvation B vs araid.
  - Replica order: u3v block 2 (running), then u3w, then u3x, then u3d block 2. If u3v is accepted, each of them is
    rebuilt on u3v first.
- 15:58: u3y (u3w + STALL_CAT 20) is parked; its falsifier hit.
  - yc1 (vs s7d, pw2's 16 peace cells, control u3w): 10 of 16 to 11, losing Stash A. Our share of cat damage went
    from 52.6% to 53.6% (71.7k to 62.1k, against 71.2k to 64.1k), not a real rise.
  - yc2 (screen cells, control u3w pw1): 35 of 44 to 36, losing evileye B vs abrk. Net -2 against its base.
  - The peace losses are close games at even cat damage. canyoudig ends at r73-85 with xfer 10-20 to 80-120, so
    the idle rushers are not what loses them. What remains in peace is transfer and kings (s7d's king points run
    1.3-2x ours: it forms kings that we don't).

## 2026-10-31 16:20 PDT: u3v accepted (movement repair); candidates rebased on it
- u3v block 2 on v4 set 2 (5691-5692, 5704-5705, 5717-5720, 5740-5747) vs u2g's set-2 reference: 48 cells, 40
  alike, D 8 (+5 -3), net +2, t +0.71.
  - Gained: AdamTan12_Finals attack_on_cat B, AdamTan12_Icarus TheHeist A, YokoOno Hike A, spsquared evileye B,
    collegejunior RUN A.
  - Lost: YokoOno Hike B, nzjt fullofsecrets A, collegejunior learn_your_lesson A.
- Pooled over blocks 1-2: 94 cells, 75 alike, D 19 (+11 -8), net +3, t +0.69. A basics repair accepts at pooled
  t >= 0 after block 2, so **u3v is accepted** (replica #141). It is u2g + KING_FLOW + KING_PLUG: the king stays
  off corridor mouths, and flow paths treat our kings' bodies as walls.
  - That ends the run of 9 straight rejects.
  - Its own blocks are the new reference for sets 1 (5639-5642, 5659-5666, 5671-5674) and 2 (5691-5692,
    5704-5705, 5717-5720, 5740-5747).
- Rebase: u3w (PEACE) and u3x (CHIRAL_SYM) are rebuilt on u3v as u3z and u4a. Each is one switch on top of u3v,
  with the same pre-registrations. Each gets a fresh screen on kvs's cells, where kvs is u3v itself (the control).
  - u3z goes to the replica at once, for block 1 on set 1 vs u3v's set-1 games, with its screen run alongside. A
    screen reject (t <= -1.5) would withdraw it.
  - u3d's block 2 waits for a rebase onto u3v too.
- progress/ refreshed for the acceptance (owner rule).
- 16:24: rebuilt on u3v. The u3v recipe is @s9s + R1-R5 + KING_FLOW + KING_PLUG; a same-code sibling (pb2)
  matches u3v in every switch, and each new build differs from pb2 only in its own lines.
  - u3z = u3v + PEACE. It is submission 142, with block 1 on v4 set 1 queued (scored vs u3v's set-1 games,
    5639-5642, 5659-5666, 5671-5674). Waiter build/wait_u3zb1.sh (matches 5748+).
  - u4a = u3v + CHIRAL_SYM is next on the replica. u4b = u3v + u3d's trap set comes after it.
  - Screens on kvs's cells, control kvs (u3v): pz1 (u3z), pz2 (u4a) and pz3 (u4b) are running.
- 16:56: rebased screens (kvs's 44 scored cells vs araid/abrk, control kvs = u3v).
  - u3z (PEACE): 32 of 44 to 30, D 12 (+7 -5), net +2, t +0.58. That passes. It broke the truce in 0 of 48 games.
  - u4a (CHIRAL_SYM): 33 of 44 to 30, D 9 (+6 -3), net +3, t +1.00. That passes. 8 of the 9 changed cells are on
    seat B. The seat-A change (cheesefarm A vs araid) comes from the birth-turn sense call's bytecode: the work
    Flow and the optional stages do depends on the bytecode left, so seat A's play shifts a little too.
  - u4b's screen (pz3) is still running.
- 17:07: u4b (u3v + u3d's trap set) screen pz3: 34 of 44 to 30, D 16 (+10 -6), net +4, t +1.00. That passes.
  The replica queue is u3z (block 1 running, 12 of 16 matches in), then u4a, then u4b.
- 17:22: u3z (u3v + PEACE) block 1 on v4 set 1 (5764-5769, 5782-5783, 5793-5796, 5904-5907) vs u3v's set-1
  games: 48 cells, 40 alike, D 8 (+5 -3), net +2, t +0.71. Block 1 rejects at t <= 0, so it passes; block 2
  (set 2) is queued.
  - Gained: AdamTan12_Finals tiny A, nzjt peaceinourtime A, spsquared bed B and superioristhmus B, collegejunior
    clockwisdom B.
  - Lost: AdamTan12_Icarus rose B, nzjt peaceinourtime B, spsquared superioristhmus A.
- Field delivery (whostab.py on the block's 48 games). The first version counted the first hostile bite or grab
  and showed "us" in 2 games, both on uneasy_alliance. stabwhy.py shows that in both, the opponent's rat trap
  fired on our rat at r14, and the engine charges the trap's owner with the backstab; our bites came at r15-16,
  in war. whostab.py now reads the engine's backstabBy from the summary. On that count u3z broke the truce in
  **0 of 48** games: them 30 (median r13-68), nobody 8.
- This corrects the 14:45 census. By the engine's count, u2g's in-band games break down as follows:
  - Big-map losses: them 26 (median r31), us 8 (r51), nobody 7.
  - Big-map wins: them 26 (r71), us 5 (r65), nobody 8.
  - Small-map losses: them 13 (r20), us 3 (r11), nobody 1.
  - Small-map wins: them 2, us 2, nobody 1.
  - So u2g broke the truce in 18 of 102 games (11 losses), not 24 of 99. The opponents' early reactive traps,
    not our bites, end most truces.
- 17:45: hz1/hz2 head-to-heads vs u3v on 8 maps (DefaultMedium, DefaultSmall, Stash, arrows, canyoudig, cheesefarm,
  evileye, keepout), both seats, with the u3v mirror (hz2) as the seat baseline. Seat A wins the mirror on
  DefaultMedium, canyoudig and Stash; seat B wins the other five. A u3v clone scores 8 of 16 against it.
  - u3z: 11 of 16, +4 -1 against the mirror's seat outcomes.
  - u4a: 7 of 16, +1 -2.
  - u4b: 10 of 16, +3 -1.
  - None of them looks broken. These are 16 deterministic games each, a sanity check only; the replica decides.
- clockwisdom. u3z's block-1 losses ended by king kill in 20 of 23 (kingkill.py): 9 by starvation, the rest
  by bites, throws or a scratch.
  - One starvation was clockwisdom A vs collegejunior (5795). The truce was never broken, and our king starved
    at r930 with 19 babies alive and 2460 cheese in their paws. We had delivered 40 cheese in the whole game.
  - On clockwisdom we deliver 0-40 a game on both seats in every replica game (4988/4989, 5575/5576,
    5795/5796, 5073/5074), and 0-340 locally (ck1: u3v vs araid and the u3v mirror).
  - The map is a spiral (mapbfs.py). By walking distance our nearest mines are (2,25) and (2,31), 59 and 65
    steps away on the enemy's Euclidean half. The Euclidean home mines are 110-315 steps away.
  - Symmetry stays ROT|HOR to r400 and beyond, since no rat sees the far half. The rushers flip between the two
    king spots (2,49) and (56,49) every 10-30 rounds (trace1.py): the nearest by dist2 changes at x = 29, and
    the bug walk along the ring wall carries them across it. Their flow searches stall, and they pick up cheese
    they never bring home.
  - The king lists no mine by r400; the gatherers explore toward far targets.
  - Alive, 5795 A would have won on points: cat damage 20 to 0 is a 100% share, worth 0.5 of the score.
- Starvation census (starvecarry.py) over 49 dumped field starvation losses (u3z, u3d, u2g sets). In 9 of them,
  babies held 100+ raw cheese 60 rounds before the death, nearly all of it in attackers' paws: clockwisdom
  2460, Hike 820 and 1245, uneasy_alliance 940 at R-100. The bank fell under 300 100-300 rounds before the death.
- u4c pre-registered: u3v + HUNGRY 300. With this existing switch, any baby carrying cheese, attackers included,
  delivers it while the bank is under 300.
  - s3k's HUNGRY 150 was neutral in old self-play and never reached the field.
  - Counter: bank and raw cheese at R-60 in starvation losses, and the 'H' share.
  - Falsifier: screen t <= -1.5, or no delivery gain on clockwisdom.
  - Instruments: hu1 (kvs's 48 cells, control kvs = u3v) and hu2 (clockwisdom vs araid and u3v, both seats).
  - The rusher flip itself is RUSH_STICK's class (u0b, u0p and u2f were neutral locally, never on the
    replica). It is the next clockwisdom candidate if u4c leaves the rushers stuck.
- 17:51: u4c's clockwisdom check (hu2: vs araid and u3v, both seats) is identical to u3v's ck1 games: xfer
  0/100, 80/340, 0/340 and 0/340. Seat A starves at r795-810 in a truce that is never broken.
  - The hungry mode does fire, from about r700, with 480 'H' turns a game. But the carriers hold 200-1680
    cheese each, and every unit carried adds 1% to both the movement and the action cooldown
    (InternalRobot.addMovementCooldownTurns / addActionCooldownTurns). A rat with 1320 cheese moves 14x
    slower, and none of them gets home before the king dies.
  - One gatherer had found the spiral's mines and carried 1680 from r150 on without getting home.
  - **u4c's pre-registered falsifier is hit** (no delivery gain on clockwisdom). Its screen hu1 runs to the
    end for the record only.
- Probe pb3 = u3v + HUNGRY 300 + CARRY_MAX 100 + ATK_CARRY 25 on clockwisdom (cq1), to bound what carry caps can
  do there. Seat B delivered 1080 vs u3v (u3v as B: 340) and 440 as B vs u3v A, but it lost seat B to araid
  (araid A won r807). Every game still ended with a king starving at r807-952. Clockwisdom needs mine
  finding along the spiral, not carry rules. Parked: it is about 1 game in 48 on the replica.
- Field load census (loads.py, u3z block 1, 48 games):
  - Attackers carry 205 cheese on average, and 19.4% of attack-move turns carry 300+ (4x cooldown on moves and
    bites). Gatherers average 16.
  - Games with a 300+ load: 9 of 25 wins, 6 of 23 losses. That is no loss signal, so attacker caps stay off.
- Engine note: the carried-robot multiplier is cast `(int) 1.5` = 1 in both cooldown functions. Carrying a rat
  adds no cooldown.
- VM disk was down to 542 MB free (the replica's own storage on it grows with every match). I deleted the kept
  replays of six finished local runs: kf1, ma2, sm1, ud1, kv1 and pl1, 195 files, about 177 MB. Their
  results.jsonl files stay. The disk is now at 715 MB free. The dump cache (3.1 GB, 372 of 374 dumps referenced
  by ~/diag26b lists) is untouched.
- Map census vs the v4 panel's in-band teams, since match 4700 (replica match.json):
  - Lost on both seats every time: averystrangespace 0/20 (Icarus, YokoOno), Stash 0/16 (YokoOno), starvation
    0/12 (Xzibit), mincemeat 0/8 (Icarus).
  - Nearly always lost: DefaultMedium 1/16, arrows 1/16, learn_your_lesson 2/18.
  - u3z broke the averystrangespace run in block 2: 5912 A vs YokoOno, won at r2000 on points.
- 18:01: **u3z block 2** (v4 set 2, 5908-5923) vs u3v's set-2 games: 48 cells, 41 alike, D 7 (+5 -2), net +3,
  t 1.13. Gains: AdamTan12_Icarus averystrangespace A and B, YokoOno Hike B and averystrangespace A,
  run_away10 ratsareants B. Losses: spsquared jail B, collegejunior RUN A.
  - Pooled with block 1: 96 cells, D 15 (+10 -5), net +5, t 1.29. That is between the block-2 bars (reject at
    t <= 1, accept at t >= 2), so u3z goes to block 3 on v4 set 3.
  - u3v has no set-3 games yet, so I resubmitted it as the reference (#143) and queued its 16 set-3 scrims.
    u3z block 3 follows, then u4a and u4b. The set-3 reference serves their block 3s as well.
  - Three of the five gains are on averystrangespace, the map we had lost 0/20 to the in-band pair. PEACE (we
    never end cooperation) fits it: long truces there are decided on points.
- 18:09: **u4c's screen hu1** (kvs cells vs araid/abrk, control kvs = u3v): 38 of 44 to 30, D 10 (+9 -1), net +8,
  t +2.53. Gains: DefaultSmall A, arrows A and B, canyoudig B and evileye A vs abrk; DefaultMedium A and B, bed A
  and learn_your_lesson A vs araid. Loss: DefaultSmall B vs araid.
  - u4c is u3v plus one constant, HUNGRY 300. A game can only diverge once the branch fires (bank under 300 with
    a carrier holding cheese), so the flips sit in the wars that drain the bank. That matches u3z's in-band
    losses: in 18 of 20 king-kill losses (kingkill.py, block 1) our bank was under 300 at R-40. Our babies were few
    (0-9), and the opponent's bank held 755-1355 at the same time (econtl: starvation, thunderdome).
  - Its pre-registered design case (clockwisdom) stays falsified. I re-registered u4c for the replica with the
    low-bank war mechanism, and recorded in attempts.csv that this was done AFTER seeing hu1. The screen is
    therefore not independent evidence; the replica blocks are.
  - Queue: u3v set-3 reference (#143, running), u3z block 3, then u4c (strongest screen: t 2.53 against 1.00 for
    u4a and u4b), then u4a, then u4b.
- averystrangespace (u3z block 2, per mine, permine3.py and minereach.py): 15-18 of our gatherers crowd the home
  mine all game (3200-4950 rat-turns per 250 rounds within 6 of it, which yields about 4.5k for the game). Twelve
  rushers stay in the dirt band south of the centre until r700. We reach the centre mine at r650 and never touch the
  nearer corner mine, (53,4) as A or (6,55) as B: 0 picked in all 4 games. YokoOno reaches its centre mine at r213
  and its corner at r263. Every mine except home needs digging (dig distance 170-342).
  - The games went to r2000 on war points. Our END_KING second king gives kings 2 to 1 (33.3 points), so we won 3
    of 4. The loss (5913) was on cat damage, 350 to 6930. With PEACE, the map is no longer a 0/20 hole, so it is
    not the next target.
- 18:15: **u4d** (u3v + HUNGRY 600, the dose arm of u4c's HUNGRY 300) built and screened as hg1.
- 18:38: **u4d's screen hg1**: 30 of 44 to 36, D 14 (+10 -4), net +6, t +1.60. That is below u4c's +8 (t 2.53),
  so by its falsifier the dose arm stops here and only u4c goes to the replica. Its losses (DefaultSmall A and B,
  cheesefarm A vs araid, bed A vs abrk) are cells u4c kept: a 600 bar sends carriers home in games the bank could
  carry.
- 18:38: **King bytecode regression: KING_PLUG overruns on lattice maps.** u3z's 31 block 1-2 field matches
  (overruns_vm.py over replica replays) hold 1296 king overruns. 1279 of them are in four games, all lost:
  mercifullattice B 381 and A 247, learn_your_lesson B 380 and 271. ko1 (u3v vs araid on those maps, both seats)
  reproduces it locally; the king overruns about every other turn from r22 on mercifullattice.
  - pb4 (a probe: u3v plus per-stage king bytecode counts in the indicator) in ko2 puts the rest of the turn at
    7-10k and the overrun inside move(). KING_PLUG's ring test costs ~1150 bytecodes a call. Where the king's own
    ring is split, unplug() counts its tile and all 8 neighbours, then closer() and step() count two more per
    candidate step.
  - **u4e** (u3v + PLUG_FAST) is a basics repair. It reads the 16 ring tiles unrolled, counts the corner rule and the
    arcs in bit operations, keeps the king's own count for the move, and skips the test under 3000 bytecodes left.
    The answers equal ringArcs's on all 65536 ring masks and on random maps, edges included (3 new BotTests,
    1747/1747; unit tests exit 0). Overrun check pf1 (u4e vs araid on mercifullattice and learn_your_lesson, both
    seats) is running. It goes on the replica ahead of u4c if pf1 holds; as a basics repair it accepts at t >= 0
    after block 2.
- 18:43: **u4e's overrun check pf1** (u4e vs araid on mercifullattice and learn_your_lesson, both seats): 0 king
  overruns in all four games. ko1's u3v had 915 on mercifullattice and about 790 on learn_your_lesson as seat B (0
  as A). u4e's king turns run 10-13k on mercifullattice with a peak of 16640. The 3000-left floor, which cuts in at
  17000 used, therefore did not fire at the sampled rounds: the saving is the cheaper test itself.
  - Results are as in ko1. The seat-A learn_your_lesson game, where u3v never overran, came out the same game (lost
    at r1322), as expected when the fast test gives the same answers. The falsifier passed. Screen pf2 (kvs cells)
    is running.
  - Field overruns are all the king's: in the staged field replays (overruns_vm.py) 1296 of 1296 are KING, none
    are baby rats.
  - **VM disk:** I deleted 12 unreferenced dump-cache dirs (198 MB, the ko1, ko2 and pf1 dumps, logged in
    ~/diag26b/dcache_deleted.txt). They are our own and can be regenerated from the kept replays. VM free went from
    256 MB to 455 MB. I added ~/diag26b/u3z_b2.txt so the u3z block-2 dumps (5908-5923) stay referenced.
- 18:53: **u3v's set-3 reference** (#143) is complete: 5924-5931, 5934-5937, 5939-5940 and 5954-5955 (16 matches,
  fetched), 18 of 48 games. The set-3 panel has nzjt, Icarus and collegejunior at 0 of 6 or close to it. I submitted
  u3z as #144 for block 3 and queued its 16 v4 set-3 scrims; the drain loop sends them as the hourly limit allows.
  Pooled bars after block 3: reject at t <= 1.5, accept at t >= 2.
- 19:00: **u4e's screen pf2** (kvs cells, control kvs = u3v): 30 of 44 to 30, D 0 (no cell changed), xfer 292.8k to
  295.4k. Pass. The screen's maps hold no lattice case where u3v lost to its overruns. u4e goes to the replica
  after u3z's block 3, ahead of u4c.
- 19:00: **Where in-band losses lose the cheese** (u3z's 96 field games, new scripts cheeseflow, pickrate,
  minehalf, theft, homemines; all on the VM over the u3z_b1/u3z_b2 lists):
  - Delivery is not it. We transfer about two thirds of what we pick in wins and in losses (4780 of 7721, 2967 of
    4477), as the enemy does. A rat pays dig and trap costs out of the cheese it carries first (engine
    addCheese), and cheese drops where it dies (571 a lost game).
  - Picking is. In losses the enemy picks 8443 a game to our 4477, although we have more babies until r300 (19-21
    to 15-17): they get about 51 per baby per 100 rounds, we get 30-33, at more mines (4.5-5.1 a window to 3.6).
    Our share of gathering turns is higher in losses than in wins (r100-299 56% to 47%; whyshare).
  - The enemy takes 1835 a lost game at mines on our half (787 in wins), and 72-83% of it with none of our rats
    within 4. 69 of the 179 home-half mines in losses were never reached by us, and they picked 26.9k there.
  - For 14 home mines in losses (21 in wins) we had seen the mirror twin 50+ rounds before we saw or reached the
    mine itself (median delay 722 rounds). The enemy picked 12960 there, we 2700. The maps are clockwisdom,
    fullofsecrets, rose, RUN and learn_your_lesson.
  - **u4f** = u3v + MINE_MIRROR (s4e's switch, never on the replica alone): with one symmetry left the king lists the
    our-half twin of every listed mine. Diag mm2 (u4f and u3v vs araid on those five maps, both seats, replays
    kept) checks that the king lists the hidden twins earlier and that our picks there rise.
- 19:12: **u4f's diag mm2** (u4f and u3v vs araid on clockwisdom, fullofsecrets, RUN, learn_your_lesson and rose,
  both seats; mineknow.py on the kings' ml= lists). The mechanism fires. On RUN as B the king listed (22,11) at
  r112 and (4,5) at r323, home mines no rat of ours ever saw, and gatherers went there (520 and 447 rat-rounds).
  On fullofsecrets as A it listed (4,5) at r321 (362 rat-rounds). u3v never listed any of these.
  - Six of ten pairs are the same game: araid's king falls early on clockwisdom, so locally there is little of
    the mid-game theft seen in the field. RUN B transferred 11039 to 9803, fullofsecrets A 788 to 600 (still
    lost), and RUN A was won by a king kill at r853 instead of on points. No outcome flipped. Screen mm3 is
    running.
  - jail (5766, our A side): the enemy reached our home mine (52,35), 26 tiles east of our king on open ground,
    at r58. Our rats spent 7 turns east of x 40, all exploring in r0-99 (max x 47), and none after that. The seekers
    and scouts never went back east. This is an exploration gap MINE_MIRROR cannot close: no rat saw the twin
    either. It is next on the list.
  - **VM disk:** I deleted the 4 mm2 dump-cache dirs (50 MB, regenerable from the kept replays) and 189 old dump
    tables (*.tsv other than cells.tsv, 529 MB) inside runs/ dirs more than 48 h old. Those are our own local-game
    dumps from the s- and early t-series (mc_s5k, mc_s5m, mc_s5p, mr1, rv1, pb1, ...); the list is in
    ~/diag26b/runs_tsv_deleted.txt. Results files, cells and replays stay. VM free went from 316 MB to 905 MB.
    /srv/bc26-galaxy (the replica's storage, not ours to prune) holds 7.0 GB.

## 2026-10-31 19:28 PDT: starvation losses decomposed; delivery anchor u4g
- 19:16: u3z's 96 field games: our last king starved in 26 of 41 king-kill losses (10 bites, 5 unknown). starve.py: at R-300 bank 581, 14.8 babies to their 19.0; R-150 bank 159, 8.7 babies; R-50 bank 15, 3.8 babies to 24.8. starvewar.py: 25 of the 26 followed an enemy backstab (r13-r322) with our babies wiped (0-4 left to 8-39): lost wars, not an economy fault. The odd ones out hold cheese: clockwisdom 5795 A (no backstab; 6 attackers holding 440-720 each at Chebyshev 50, all 'a', 0 transferred r600-930: the known frozen-attacker case, u4c's target) and fullofsecrets 5764 A, 5922 A, 5923 B (9-16 babies holding 1385-3550).
- 19:20: fullofsecrets 5764 A (hoard.py, kingbox.py, kingreach.py): 8 deliverers ('d') stood r350-650 at (8-10,28-31), Chebyshev 5 from the king at (2-3,32) behind the pocket's east wall; the path round is 39 steps and clear (no dirt; the enemy's 17 placed dirt tiles are round its own king). Local mm2 u3v fullofsecrets (indicators): rat 12568 r396-470 nav target hops (1,32)/(2,32)/(3,32) with stall 0-2 and a stale flow path (9,31): the roaming king's every step is a new goTo target, so the stall never reaches FLOW_STALL 4 and no path is asked for. s4w's NAV_SAME_R (2026-10-28) saw the same and kept the stall, but the target still moved and 12-turn forward searches failed.
- 19:24: built u4g = u3v + DELIVER_ANCHOR 3 (new switch, Baby.anchorFor; BotTest testAnchorFor, 1792/1792; unit tests exit 0): the delivery target is an anchor kept while the king stays within 3 of it, so the stall builds and a path to a fixed target is followed. Pre-registered as a basics repair; diag da1 (6 pocket/corridor maps vs araid both seats, u3v control, replays kept) running on the VM.
- 19:26: stuck sweep (stuck_report on all 96 u3z field games): stuck rounds by code d 7973, C 3677, M 36758; TheHeist M 22858 in 4 games (won all 4), EscapeTheNight M 7046 in 2 (won both), rose M 5119 + d 4754 in 4 (lost 5905 B on points 44-55). rose 5905 B r430-470: a mine-bound rat holds the choke tile (49,2) into the king's room for 40+ rounds while 4-8 deliverers queue head-on in the 1-wide x=50 corridor: the corridor jam the SWAP_HOLD family (s7j-s7y, s8c, u1s) fixed locally but lost at screen/replica. Not taken up again yet.

## 2026-10-31 19:40 PDT: u3z rejected at block 3; u4e on the replica; enemy traps in melee
- 19:33: u4f's screen mm3 (kvs cells, control kvs = u3v): 30 of 44 to 30, D 0 (no cell changed; xfer 295.4k to 295.7k): pass. u4f joins the replica queue after u4e and u4c.
- 19:36: **u3z block 3** (#144, v4 set 3: 5964-5967, 5980-5983, 6002-6005, 6008-6011) vs u3v's set-3 games: 48 cells, 40 alike, D 8 (+5: AdamTan12_Finals where_are_you B, Icarus averystrangespace A + B, spsquared TheHeist A, collegejunior FreeTheCheese B; -3: Finals where_are_you A, spsquared rift B, collegejunior FreeTheCheese A), net +2, t +0.71. Pooled over blocks 1-3: 144 cells, D 23 (+15 -8), net +7, t 1.46, at or below the block-3 bar of 1.5: **u3z (PEACE) rejected**. Every block was positive (+2, +3, +2), so PEACE may be worth a little, but not enough to clear the bar; u3v stays the incumbent.
- 19:38: u4e (PLUG_FAST, basics repair of the king's overruns) submitted as #145 for block 1 on v4 set 1 against u3v's set-1 games; 2 scrims sent, 14 queued for the drain (waiter build/wait_u4eb1.sh, matches above 6011). Queue after it: u4g (if da1 passes), u4c, u4f, u4a, u4b.
- 19:39: **Enemy rat traps in melee** (u3z's 96 field games; traps.py, traps2.py, trapmove.py on the VM):
  - Enemy rat traps fire on our rats 35.6 times in a lost game (22.4 in a win); 22.7 of those victims die within 20 rounds (13.0 in wins). Our own traps kill none of theirs (u3v lays none). Engine: a rat trap does 50 damage (a bite does 10) and sets movement cooldown 30, costs 20, can be laid only on a free tile within dist2 2 of the builder, and fires for an enemy robot that moves (or lands) into a tile within dist2 2 of it.
  - In our half: 26.1 laid and 24.8 fired a lost game (13.1 and 12.5 in wins), mostly 0-4 from our mines, and the median gap from laying to firing is 1 round (q25 0, q75 3). These are melee traps laid next to the fight, not minefields.
  - How our victims got there, per lost game: own move 22.4 (16.5 of them with an enemy rat within Chebyshev 2 the round before), no own turn that round 10.6 (carried or dropped), thrown 2.6. Victim codes on own moves: faceEnemy 4.6, war 4.1, attack 3.9, carry-aim 2.9, flank 1.8, to-mine 1.6, chase 1.5.
  - Death causes per lost game, r<300 then later: ours throw-landing 11.8/10.9, trap 5.1/6.6; theirs throw-landing 11.8/9.7, trap 0/0. Against AdamTan12's losses our trap deaths are 13.2/12.4.
  - The defensive side was tried before: FACE_HOLD (u1l weak; u1m parked, fires on us 699 to 808 but wins 10 to 11), u1n's zone hold WOUNDED_HP 100 (rejected at diag, 6 wins to 11), u3i (parked). The offensive side is u4b (u3d's reactive trap set, which kills 2.6-3.4 rats a game), already queued.

## 2026-10-31 19:59 PDT: u4g diag da1 mixed; held path tiles u4h

- 19:45: da1 done (u3v vs u4g, each vs araid on six maps, both seats). u4g wins 10 of 12 to u3v's 9 (cheesebottles B
  now won). fullofsecrets is still lost both ways (lost wars, backstab r445), but later, with xfer 160/600 up to
  2777/1560. Delivery stalls ('d' rounds, all 12 games) 2435 to 1342.
- 19:48: Falsifier taken literally: fullofsecrets 'd' 434 to 657 and all stuck rounds 12222 to 15650, so both clauses
  hit. Read on: the targeted stand (carriers at king Chebyshev 4-7 behind the pocket wall) is gone. u4g's
  fullofsecrets 'd' is one carrier with 1400-1820 cheese, 27-31 from the king, crawling under carry cooldown in a game
  2.2x longer. The stuck rise is a cheesebottles B M jam (4579 rounds). Sent to the screen (sg1) as a mixed result; the
  screen and the replica decide.
- 19:52: The jam: three empty M rats at (12-14,15), a dead-end strip under two side-by-side kings (FORM_RICH). The
  inner rat's only exit runs through the other two, and those two plan east through its tile. Each drops its path after
  holding 3 turns behind a robot (indicator fe 2nd field +1 every 6 turns), searches again, gets the same path (searches
  ignore robots), and repeats for 230-270 rounds. TheHeist's field M jam (22858 rounds) fits the same picture.
- 19:56: Robot-hold drops are common: u3v averages about 235 a game in da1 (2832 of about 12500 searches).
- 19:58: FLOW_HELD (new switch): after a robot-hold drop, the held tile is kept out of the next searches for N rounds
  (one tile per rat, never beside the target), so the rat routes round or backs out of a dead end. This differs from
  FLOW_JAM (sidestep and keep the path, s4s/s4u failed) and SWAP_HOLD (carry the ally past). BotTest testFlowHeld
  passes (1796/1796), and the unit tests exit 0.
- 19:59: u4h = u3v + FLOW_HELD 20. Diag dh1 started: u4h vs araid on da1's six maps (the da1 u3v games are the
  control; local games are deterministic, as mm2 and da1 gave identical u3v numbers), plus TheHeist and rose for both
  builds; jobs 1, BC_KEEP. u4e's replica block 1 is at 4 of 16.
- 20:03: VM disk deletion: free space had fallen to 382 MB (da1's 24 replay dumps, 372 MB). I retired six
  analysis lists for settled candidates to ~/diag26b/retired/ (rh1_u3h, u3c_12, hd1_u3g, ud1_u3d, ks1_u3e, ma2_u3c),
  then deleted the dumps that were now unreferenced and over 3 h old: 140 dump dirs, about 1.25 GB, all regenerable from
  their replays (names in ~/diag26b/dcache_deleted.txt). VM free is now 1.6 GB.
- 20:03: Xzibit (3-9, worst in-band record): all 9 losses are king kills, each map lost in both seats (canyoudig r285-321,
  starvation r482-556, learn_your_lesson, ZeroDay). In r0-50 we spend 1638 (30.5 spawns, about 41 each, plus 198 on
  defensive cat traps) to their 782 (20 spawns, about 28 each). The early throw exchange is even, but our refills cost
  more because our live count is higher. From r200 we bring in 180-240 per 100 rounds with 13-19 babies to their
  360-450 with 11-12, and the king's 200 per 100 rounds empties the bank. Rush limits were all tried before (RUSH_UNTIL,
  RUSH_LIFE, RUSH_BURN, RUSH_BAN, RUSH_TO 12).
- 20:17: u4g passes the screen sg1 (kvs cells vs araid/abrk, control u3v): 32 of 44 to 30, D 12 (+7 -5), net +2,
  t +0.58. It joins the replica queue after u4e.
- 20:17: Stash vs YokoOno (5769, lost as B): from r30 to r170 only 0-3 of our 20-30 rats are outside the pocket
  (y >= 7). The why-codes are a 37%, M 14-35%, C 9-40%, w+f 9-32%. Of the w/f turns, 370 of 406 have the nearest
  enemy across a known wall (greedy line). Attackers and mine-runners mill at y 4-6, right under the mines at y 11 that
  YokoOno farms. Locally (u3v B vs araid) the same digs happen until a stripe tile, (5,6), is dug at r62; from r90 the
  rats stream out (7, 11, 15, 19, 24 outside). In the field that tile waits until r133, and the rats leave from r170.
- 20:22: Walled-enemy census over u3z's 32 field matches (96 games; ~/diag26b/enemywall2.py). It counts our rat-turns
  whose nearest enemy (dist2 3-20) is across a wall on the greedy line, with an 8-way BFS detour over 2*cheb+4 ("long").
  Among w/f turns, long-walled ones are 8364 of 26858 in wins (31%) and 3903 of 33095 in losses (12%): EscapeTheNight
  5918 W alone has 5523, RUN 5918 L 2244, Stash 5769 L 637. Facing walled-off enemies is common and not
  loss-specific, so no ENEMY_LOS switch on this evidence.
- 20:22: Carry-load census (same games; carryload.py). Engine: carrying a robot adds no cooldown ((int) 1.5 = 1), and a
  carried rat's cheese doesn't slow its carrier. So ferrying a rat that holds C cheese over D tiles costs about 2D
  rat-turns, against D(1 + C/100) walking. In wins we spend about 1620 baby-turns a game holding 300+ cheese, mostly
  6-20 from our king (rich maps). In losses it's about 140 a game: their loads are light (1-29: 1580, 30-99: 1080 turns
  a game). A ferry would speed rich wins, not flip losses. Logged as an idea, not built.

## 2026-10-31 20:31 PDT: u4e block 1 passes; u4h diag mixed, to the screen

- 20:31: u4e (PLUG_FAST, a bytecode basics repair) block 1 on the replica (#145, set 1, 6021-6080): 48 cells, 47 the
  same as u3v's, D 1 (gained Xzibit attack_on_cat B), net +1, t +1.00. Block 1 rejects only at t <= 0, so block 2
  (set 2) is queued; a basics repair accepts at pooled t >= 0 after block 2.
- 20:31: u4h (FLOW_HELD 20) diag dh1 vs u3v, each vs araid: wins 9 = 9 of the 12 da1 cells and 2 = 2 on
  TheHeist/rose (gained rose B, lost TheHeist A by 865 to 900 xfer at r411). Robot-hold drops 6577 to 3139 (rose 3729
  to 467). Stuck rounds 22864 to 15384 (rose 10485 to 316). On the 12 da1 cells stuck rounds rose from 12222 to 14764:
  pipes went 1398 to 4511, nearly all delivery, because a held tile in a 1-wide pipe sends the next search on a long
  detour. Xfer 149.8k to 145.0k (-3%, mostly EscapeTheNight 19.1k to 13.0k, which also fell under u4g). By the letter,
  the fewer-xfer clause of the falsifier is hit. The aimed-at jam (rose's corridor) is gone, so it goes to the screen
  qh1 as mixed. If it reaches the replica and a later fix is needed: skip the hold when the held tile has no free
  side neighbour (a pipe).
- 20:38: The pipes stall under u4h (dh1, araid_vs_u4h pipes, r1081-1739): our carriers ($60-255) are held in the
  2-wide pipe x 24-25 by two of our rats on code f. Those rats face enemies at x 27-28 through the pipe wall (x 26);
  a FLOW_HELD hold on one column leaves the other blocked too. Field census (facereach.py, u3z's 96 games): 'f' turns
  where the enemy at Chebyshev 2 has no open tile beside both of us (can't get adjacent in one move) are 9460 of 13612
  in wins and 4090 of 9073 in losses. In the two local pipes games it's 4173 of 4296 and 5119 of 5166, about half
  beside one of our travellers.
- 20:38: u4i = u3v + FACE_REACH (new switch; Baby.reach1, a test in BotTest, 1798/1798; unit tests exit 0).
  Pre-registered in attempts.csv. Diag fq1 (22 cells: u4i vs araid on EscapeTheNight, rose, fullofsecrets, RUN,
  averystrangespace, evileye, Stash, pipes, both seats; u3v on the last three new maps) is running, replays kept.
- 20:42: How u3z's 45 field losses ended (howend/kingkill): 40 by king kill, of which 21 starvation (bank about 0 at
  R-40, 0-1 babies left), 15 bites, 3 throw hits, 1 cat scratch. Opening economy (openecon.py, rounds.tsv engBabies
  and bank; medians by opponent, us/them): the bots we lose to keep far bigger banks. Xzibit (3-9) r200 476/1278 and
  r400 523/1321; TheComplexMerlin (4-8) r200 735/1380 and r400 296/1305; collegejunior (6-6) r400 882/1525. They run 16
  babies to our 20. In collegejunior's learn_your_lesson win over us (5918 g2), they spawn nothing until about r100
  (bank 2300) and keep 1400-2000 all game. Our bank sits at 250-350 from r400, under SPAWN_FLOOR's reach, so dead
  babies aren't replaced (20 to 1 by r900). A bank gap that tracks the result is likely a consequence (the r0-50 rush
  spend: 30 spawns to Xzibit's 20). Rush size is already bracketed on the replica (RUSH_TO 12 net -7, RUSH_TO 20
  net -3), so no tuning candidate; kept as context for an economy swing.
- 20:50: **u4j pre-registered** (structural): u3v + PEACE + MINE_ANY + MINE_TIES, built by combining the two replica near-misses (TRAINING_ALGORITHM §3).
  - u3z (PEACE) stopped at block 3 with pooled t 1.46 (net +7 over 144 cells). u3c (MINE_ANY + MINE_TIES on u2g) ended block 4 at pooled t 1.41 (net +6 over 176).
  - Their gains fall on different cells, and the mechanisms meet. PEACE keeps cooperation going, and during it the enemy-half and axis mines are not fought over. So the spread can turn the longer peace into cheese (in-band losses: they picked 46 per 100 rat-turns to our 24).
  - Falsifier: the replica block bars, or a screen t <= -1.5. Also falsified if, in the field blocks, we open the war in any game, or pickups before the war are no higher than u3z's.
  - Its screen pm1 (kvs cells, control u3v) starts when qh1 frees the VM. bots/u4h was never committed; it goes in now with u4j.
  - Replica queue after u4e: u4g, u4c, u4f, u4a, u4b (the order set at 19:38), then u4j, u4h and u4i as their local checks pass.
- 20:54: **u4h's screen qh1** (kvs cells, control kvs = u3v): 36 of 44 to 30, D 12 (+9 -3), net +6, t +1.73: pass. Xfer 295.4k to 299.5k.
  - Gains: abrk canyoudig B, evileye A, tiny B; araid DefaultMedium A, arrows B, bed A, learn_your_lesson A, starvation B, tiny B.
  - Losses: abrk bed B; araid bed B, keepout A.
  - u4h joins the replica queue as a basics repair. Queue after u4e: u4g, u4c, u4h, then u4j (if pm1 passes), u4a, u4b, u4f and u4i (if fq1 passes). u4h moves ahead of u4a and u4b on the stronger screen (t +1.73 to their +1.00), and u4f goes last (its screen changed no cell).
  - u4j's screen pm1 started when qh1 finished (jobs 2).
- 20:54: Baseline for u4j's counter (prewar.py on u3z's 96 field games):
  - Peace lasts 253 rounds a game (losses 146, wins 348). Nobody broke the truce in 10 games.
  - Picked before the war, a game: us 2155, them 1465. In losses 802 to 962 (550 to 660 per 100 peace rounds); in wins 3349 to 1909.
  - fullofsecrets 5764 (L): 568 peace rounds, we picked 5880 to their 1900. Yet at r481 our babies held 1965 while we had delivered 260, and the king starved. That is the pocket-delivery failure u4g (DELIVER_ANCHOR) is aimed at.
- 21:00: In-band loss in u4e's block 2: YokoOno 6099, averystrangespace B. The game was peaceful (no backstab) and lost on points at r2000: cat damage 0 to 6400, xfer 4450 to 11520, babies 30 to 49.
  - minepick.py: we picked only from our home mine (49,52), 4520 in total, about 1100 every 500 rounds. YokoOno took the same from theirs, plus 5245 from their centre mine, 2625 from OUR centre mine (34,33), 4140 from their far corner and 440 from ours.
  - heat.py: our babies never leave the corner pocket. The open floor is a walled dead-end corridor (x51-52, y33-46). The way out runs through dirt bands, and we dug 35 tiles all game. The attackers ('a', 33-41% of baby-turns all game) sit at the corridor's foot for 2000 rounds.
  - Reproduced locally (fq1, u4i vs araid on averystrangespace, xfer 4480/4490). It is the mirror picture: the attackers' pit is at x7-8, y20-26, and their target is (4,56), a wrong mirror spot, since symmetry is still ambiguous at r600 (sym=7). Their flow searches end in timeouts or failures (fe[6] 5-6), at 15.5k bytecodes a turn.
  - All ~22 gatherers have mt=10,7: the one mine we know. The 'x' explorers' searches to far never-seen tiles take 8-25 turns and find nothing.
  - So the loss comes from knowing one mine and from Flow's search speed on a 60x60 dirt maze (~344 bytecodes a node). The same class was tried as BREAKOUT (s9z, net 0), SEEK_FRONTIER (u2t), SWEEP (u2u/u2x) and BFLOW (u0c). BFLOW picked 2x here but transferred no more: carriers' fields toward a moving king never finished.
  - Two queued candidates touch it: u4a (CHIRAL_SYM: on this map the chirality-1 seat B would know the rotation at r1, so the attackers would stop targeting a wrong mirror spot) and u4j (PEACE: u3z gained averystrangespace A/B vs Icarus and A vs YokoOno). A faster search (BFLOW v2 with fields reused across king moves) stays the structural option for this map class.
- 21:14: **u4i's diag fq1** (FACE_REACH; u4i vs araid on 8 maps, both seats). Controls: u3v's da1/dh1 games and fq1's own u3v cells.
  - Wins 13 = 13 of 16 (no cell changed).
  - Xfer 120.5k to 157.3k (+31%). EscapeTheNight went 19.1k to 50.7k, won on points at r2000 instead of by king kills at r840. Stash A went 6.8k to 13.7k.
  - Counter (facereach.py): 'f' turns facing an enemy that no open tile lets reach us fell 15128 to 395 (-97%). Those beside an own traveller fell 4209 to 11.
  - averystrangespace, rose and fullofsecrets were identical games.
  - Falsifier not hit. Screen fq2 (kvs cells, control u3v) is running at jobs 1, beside pm1 at jobs 2. VM free 840 MB after 32 new dumps (lists fq1_u4i.txt, fq1_u3v.txt).
- 21:15: **u4j's screen pm1** (kvs cells, control kvs = u3v): 36 of 44 to 30, D 16 (+11 -5), net +6, t +1.50: pass. u3z, PEACE alone, scored net +2 and t +0.58 on the same cells.
  - Gains: abrk DefaultSmall A, arrows B, canyoudig B, evileye A; araid DefaultMedium A/B, bed A, canyoudig B, learn_your_lesson A, starvation B, tiny B.
  - Losses: araid arrows A, cheesefarm A, evileye B, starvation A, tiny A.
  - Xfer 295.4k to 262.5k (-11%; peace games end at the last cat). Truce broken by u4j in 0 of 48 (araid median r36, abrk r30).
  - **Queue re-ordered** after u4e: u4g (promised next; a basics repair), u4j, u4c, u4h, u4a, u4b, u4i (if fq2 passes), u4f. u4j moves up because both of its parts already have field evidence (pooled t 1.46 and 1.41 over 144 and 176 cells), which no other queued candidate has. u4f (screen D 0) goes last.
- 21:17: Stall census (flowfail.py, u4i's 16 fq1 games): 11% of baby-turns are stalled 50+ turns (79.9k of 720.8k). Half of those come from a rat whose Flow search failed (37.1k).
  - averystrangespace: 24-27%, nearly all attackers with failed searches (14.5k and 16.5k). RUN: 2.5-4.4%, attackers. Both point at a target nobody can reach.
  - EscapeTheNight: 21-22% stalled, nearly all 'M' (heading to a mine), but with searches that succeeded (~550 with a failure). rose: 10-12%, M and a.
  - So a "BitFlow when A* fails" fallback would mostly move pitted attackers. It would not fix EscapeTheNight's mine queue, nor the one-known-mine economy on averystrangespace. Not built for now. The census is the baseline if it comes back.

## 2026-10-31 21:20 PDT: u4e accepted (basics repair, king bytecodes); reign plan

- 21:20: **u4e accepted over u3v** (replica #145).
  - Block 2 (v4 set 2: 6088-6091, 6098-6101, 6114-6117, 6131-6134) vs u3v's set-2 games: 48 cells, 47 alike, D 1 (lost AdamTan12_Finals attack_on_cat B), net -1, t -1.00.
  - Pooled over blocks 1-2: 94 cells, 92 alike, D 2 (+1 -1), net 0, t 0.00. A basics repair accepts at pooled t >= 0 after block 2.
  - Field check of the mechanism (kingovr_all.py): 0 king overruns on mercifullattice A/B and learn_your_lesson A/B, max 18.3k bytecodes over 12 games. u3z's games had ~1279 on those maps.
  - progress/ refresh started (owner rule).
- Reign plan for u4e:
  - References: u4e's own set-1 games (6021-6022, 6025-6026, 6039-6042, 6058-6061, 6077-6080) and set-2 games (the block above). These are its accepting sets, but it was crowned at net 0 with 92 of 94 cells the same as u3v's, so the winner's curse is negligible there. Set 3 needs a u4e reference before any candidate is scored on it.
  - Queued candidates were built on u3v without PLUG_FAST, so each is rebased on u4e under a new codename before it is submitted. PLUG_FAST changed no local screen cell (pf2 D 0), so their screens carry over.
  - First up: u4g's DELIVER_ANCHOR 3 on u4e, then u4j's PEACE + MINE_ANY + MINE_TIES on u4e.
- 21:27: **u4k submitted** (replica #146, u4g's DELIVER_ANCHOR 3 rebased on u4e). Block 1 runs on v4 set 1, against u4e's set-1 games (6021-6022, 6025-6026, 6039-6042, 6058-6061, 6077-6080). The hourly limit holds all 16 scrims in the queue, so the drain loop sends them. **u4l** (u4j rebased on u4e) is built and goes next. Then u4c, u4h, u4a, u4b, u4i (if fq2 passes) and u4f, each rebased on u4e first.
- 21:27: **hx1** (u4a and u4j vs araid on averystrangespace, both seats): all four games are wins, as before.
  - u4a (CHIRAL_SYM): xfer 4480 (A) and 4520 (B), against u3v's 4480 and 4450. Cat damage 0, as before.
  - u4j (PEACE + MINE_ANY): xfer 5905 (A, cat damage 3806) and 4465 (B).
  - The mechanism works. In u4a's seat-B game every baby has sym=1 (rotation) from r1, and all 12 attackers aim at (3,3). u3v's attackers split between the wrong mirrors (3,56) and (56,3) under sym=7.
  - The outcome does not change. All 12 attackers stay in our quadrant (x 36-59, y 33-59), with stall counters at 300-1100 by r400-1200: their reverse searches across the 60x60 dirt maze never finish inside FLOW_REV_TURNS 60.
  - So the map's limit is search bytecodes, not symmetry. BitFlow is no cheaper on a full 60x60 front: a wave costs ~100 bytecodes for each row in range, and the field needs ~150+ waves here, ~1M bytecodes against A*'s ~0.9M. Only shared work (a king-computed route, posted as waypoints) would change the order of magnitude. Not built: the census puts failed searches on one map of the eight.

## 2026-10-31 21:42 PDT: u4e's field losses: starvation after the army falls, and enemy rat traps

- 21:42: **How u4e loses** (its 32 set-1/2 field matches, 96 games, 46 won). Each replay was dumped to a temporary cache and deleted after use (scripts kk_batch.sh/fx_batch.sh on the VM; the dumps are not kept).
  - Ends: 43 losses by king kill, 7 on points.
  - Points losses: 4 of the 7 are on averystrangespace (YokoOno A/B, Icarus A/B), all in peace to r2000. Against Icarus (6131/6132) we had 2 kings to 1 and even deliveries (4480/4415, 4450/4650). We lost on cat damage alone, 0 to 420 and 0 to 87: in cooperation cat damage is half the score, and 100 damage would have won either game. u3z (PEACE) won 5921 there with cat damage 587 to 87. So u4l covers this class.
  - kingkill.py on the 43 king-kill losses: our last king starved in 23, was bitten to death in 14, thrown into in 4, scratched in 2. Of the 23 starvations, 17 have 0-3 of our babies alive 30 rounds before: the army was killed first. Only 6 (fullofsecrets x2, Stash, clockwisdom x2, Hike) starve with 5-17 babies alive.
  - So most losses are combat losses, whatever the final cause.
- 21:42: **Combat census** (fightx.py, rounds 1-300, per game; W 46, L 50):
  - Enemy rat traps: they lay 13.5 a game in our wins and 19.0 in our losses, and our rats fire 12.1 and 17.6 of them (about 92%). We lay 0 (BABY_RAT_TRAPS is off in u4e). Each trap does 50 damage and stuns 30 rounds.
  - By opponent: AdamTan12_Finals lays 46.2 and we fire 44.8; VarunVejalla 31.7 / 27.0; spsquared 20.1 / 18.8; Icarus 11.9 / 11.2; Xzibit 10.3 / 8.6; YokoOno 8.0 / 6.5; TheComplexMerlin 2.9 / 2.9; collegejunior 0.
  - Throws: we throw 23.6 (W) and 33.7 (L) times, they throw 15.7 and 32.6. Our thrown rats hit a robot 14.4 and 19.0 times (61%, 56%), theirs 5.7 and 14.4 (36%, 44%). Our throws are the better ones. (A throwHit row belongs to the thrown rat's team. This line first had the two sides swapped.)
  - trapwhere.py (6 dumped matches, 11 losses): 49.0 traps fired on us a lost game. 22.4 fired the round they were laid and 9.7 a round later: reactive traps, laid on a front tile next to the placer as our rat steps in. 94% fired in our rat's own move; 4% on landing from a throw.
  - The victims were mostly fighting: attack 5.6, war 5.9, face 5.4, carry-aim 4.0, flank 3.2 a game; gatherers' errands about 6.
  - **Dodging has been tried five times** and lost each time: RISK_FRESH (t0b), RISK_IN_FIGHT (s8h), FACE_HOLD (u1l), WOUNDED_HP (u1b), RISK_ADAPT (u0u). A detour round fresh enemy fronts costs fights more than it saves, and holding at Chebyshev 2 gives the trapper time.
  - So the lever is laying our own: u4b, u3d's reactive set (u3d on u2g passed replica block 1, +1, t +0.58).
- **Queue re-ordered**: u4k (running), u4l, u4b, u4h, u4a, u4i (if fq2 passes), u4c, u4f.
  - u4b moves up on this census.
  - u4c moves down: its design case was falsified (hu2).

## 2026-10-31 21:59 PDT: u4i passes its screen (u4n), Merlin's throws, and enemy traps can't be seen

- 21:59: **fq2** (u4i = u3v + FACE_REACH, kvs cells, control kvs = u3v): 44 distinct cells, 30 wins to 33 (net +3), xfer 295.4k to 289.5k (-2%), 0 exceptions. Pass (the screen rejects only at t <= -1.5). **u4n** is u4i rebased on u4e (the u4e recipe + FACE_REACH; only newer off switches differ).
- Queue: u4k (block 1 running), u4l, u4m, u4h, u4a, u4n, u4c, u4f.
- 21:59: **TheComplexMerlin's throws.** Merlin is 4-8 against u4e, inside the 25-75% band.
  - 3 of its 8 wins are king kills by throws: thunderdome in both seats (6079 r139, 6080 r120) and knifefight (6133 r90). The fourth throw kill in u4e's 96 games is AdamTan12_Finals on tiny (6025 r133).
  - 6079 thunderdome: 13 throw hits on our king, r101-139, all 42 damage, all with our own rats as the ammunition. Example: Merlin's rat grabbed our rat 12359 two tiles from our king at r95, carried it 6 rounds and threw it at r101. Another Merlin rat grabbed the same rat at r104 and threw it again at r106, killing it.
  - Engine: a throw hits for 10 + 4 x (2 x remaining + 1 on the first step), so 42 means a hit on the second tile. The thrower stands two tiles off the king's 3x3. The thrown rat takes the same 42.
  - An enemy can grab our rat when our rat cannot see it, or when our rat has less health. Our king cannot run: its movement cooldown is 40 (a move every 4 rounds), and a carrier's is 15.
  - This class has been attacked six ways: RESCUE (s8n/s8o/s8q), the carrier guard (u2y/u2z/u3a), AMMO_AWAY (s8y/s9v/u1v), GUARD_KING_R (s8d/s9w/u2w), BITE_CARRIER (s4i). All were rejected or parked, at |t| < 1 against the old panel.
  - With 4 games of 96 at stake, a re-test cannot reach t 2 on its own. Not built now.
- 21:59: **Can our rats see enemy traps being laid?** (trapcheese.py on u3z's 32 dumped field matches)
  - The idea: a baby pays a trap's 20 cheese from its carried cheese first (InternalRobot.addCheese), and RobotInfo shows cheeseAmount, so a 20-cheese drop on an enemy rat would reveal a trap on one of its front tiles.
  - Of the 2982 enemy traps with placer positions, 1184 (40%) came with a 20-cheese drop. 1746 (59%) were laid by placers carrying 0 cheese and paid from the bank, which shows nothing.
  - 2673 of these traps fired on our rats (88%). In only 376 (14%) did the victim have the placer in its vision cone both one round before and at the lay, and only 117 of those were drop-20 traps.
  - So inference from cheese would flag about 4% of the traps that hit us. Not built.
  - The engine was also checked: traps are stored per team, so canPlaceRatTrap and canRemoveRatTrap reveal nothing about the enemy's traps.
- 22:04: **u4k rejected** (replica #146, DELIVER_ANCHOR 3 on u4e). v4 set-1 block 1 (6148-6151, 6162-6165, 6171-6172, 6180-6181, 6189-6192) against u4e's set-1 games: 48 cells, 42 alike, D 6 (+1 -5), net -4, t -1.63. Block 1 rejects at t <= 0.
  - Gained spsquared bed B. Lost AdamTan12_Finals DefaultSmall B, Icarus TheHeist A and uneasy_alliance A, Merlin peaceinourtime B, collegejunior rose A.
  - Merlin's thunderdome went the same way in both seats again (lost).
- 22:04: **u4l submitted** (replica #147: u4e + PEACE + MINE_ANY + MINE_TIES). Block 1 runs on v4 set 1 against u4e's set-1 games; 16 scrims are queued for the drain loop. progress/ refreshed after the scored block.

## 2026-10-31 22:21 PDT: clockwisdom, where u4e delivers nothing

- 22:21: **Why u4e starves on clockwisdom** (field 6060 A: picked 5440, delivered 0, king starved r910; 6061 B: picked 15960, delivered 920, starved r1285; 6100/6101 vs Varun delivered 0 and were won only by king kills).
  - collegejunior delivers 93% of its pickups on this map. xferratio.py: on every other map our first delivery comes by r60-223; on clockwisdom it comes at r635 or never.
  - The failure reproduces locally (cw1, u4e vs s7d): xfer 40 as A (lost r930), 340 as B (won r775 when s7d starved first).
  - The map is a 59x59 rotational spiral of ring walls. homedist.py (BFS from our king at (56,9), walls blocked): the outer band runs from the king along the bottom, up the left side and across the top. The right-hand band beside the king (x 53-58, y 20-49) is 300+ steps away, and the inner rings are 110-260.
  - Mines by path from the king: (2,25) 59 steps and (2,31) 65 along the outer band, then (38,47) 103 and (43,44) 108. (56,27), at Chebyshev 18, is 309 steps away.
  - Our king knows no mine all game (km=n0, empty mine list). It reaches the 20-baby cap by r50, so it never spawns again.
  - 12 of the 20 babies are RUSH attackers. They walk the spiral toward (2,49) or the wrong mirror (56,49), picking up cheese on the way. They hold 220-4400 each (5 carriers above 1000), and attackers never deliver (ATK_DELIVER 0).
  - Gatherers with no mine ('E') roam the bottom corridor for 400 rounds. Their explore targets are inside walled rings, and every flow search fails (fe[5] reaches 17).
  - The one real carrier, 11605, starts delivering at (9,15) on r100, 47 steps from home, and walks the wrong way round the spiral. By r425 it is 128 steps out, holding 1240 cheese. The greedy/bug walk east hits the ring wall and follows it upward; a flow search never completes.
- 22:21: **The carry cap does not fix it** (cw2, against s7d's 300-1496). pb2 (CARRY_MAX 150) delivered 140 as A (lost r1005) and 300 as B (lost r985). pb3 (CARRY_MAX 150 + ATK_CARRY 25) delivered 0 as A (won r770, s7d starved first) and 420 as B (won r955). Every game ended in a starvation.
- 22:21: **Next probe: TRAIL** (s5d's switch, withdrawn at s5c because EscapeTheNight's mine-bound babies jammed in the king's box). A carrier whose delivery stalls walks its own loop-free trail back from its spawn, which is a walked, wall-free path whatever the maze.
  - cw3 runs pb2 (u4e + TRAIL) and pb3 (u4e + TRAIL + ATK_DELIVER 50) against s7d on clockwisdom, EscapeTheNight (the old jam), fullofsecrets and pipes (TRAIL's old gains, carrier stalls 1339 to 155 and 913 to 121), both seats, with u4e controls.
- 22:34: **TRAIL has never fired since the move decision became movement-ready only.** pb4 (pb2 with Trail.n, homing and homeStall in the indicator) on clockwisdom: carrier 11605 had a 46-tile trail home at r100 but homeStall stayed at 0-1 every turn.
  - homeLeg restarts its count unless it ran on the round before. A carrier with 20+ cheese skips movement rounds (1% cooldown a unit), so the count never reaches FLOW_STALL.
  - New switch TRAIL_GAP (off): the count survives a gap of up to TRAIL_GAP rounds. The gated BotTest home run (extra move cooldown, decision only on ready rounds) takes 43 turns with gap 12. Gap 0 takes 65, the same as nav alone. BotTest 1800/1800, unit tests pass (commit 78b16db).
  - pb4 is now u4e + TRAIL + TRAIL_GAP 12; cw4 runs it on the same 4 maps against s7d, both seats.
- 22:34: **cw3** (against s7d, both seats; u4e on clockwisdom from cw1). pb2's TRAIL was inert, so pb2 is u4e plus Trail.record's bytecodes: it won 6 of 8, as u4e.
  - pb3 (u4e + ATK_DELIVER 50, trail inert): clockwisdom 2 of 2 with real deliveries (940 as A, 1800 as B; u4e 40 and 340, 1 of 2). EscapeTheNight 2 of 2, in r792-850 (u4e went to r2000). fullofsecrets 0 of 2, as u4e. pipes A lost r432 (u4e won it).
  - u4e loses fullofsecrets in both seats against s7d. A: we broke the truce at r385, delivered 540 to 2619, and the king died r712. B: we broke it at r505 and lost on points, 4475 to 10798. This is a local reproduction of the field's fullofsecrets losses (6021 A, 6133 A, 6134 B).
- 22:34: **averystrangespace is decided by cat damage, and PEACE is what changes it.** u4e lost all 4 set-2 games, 6098/6099 against YokoOno and 6131/6132 against Icarus, at r2000 on points. u3v lost the same 4 (5717/5718/5744/5745). u3z (u3v + PEACE) won 3 of the 4 (5912 A, 5920 A, 5921 B).
  - Transfers against Icarus are even (u4e 4480 to 4415 as A). Cat damage decides it: u4e 0/87 to their 420/87; u3z 1065 to 296 and 587 to 87.
  - u4e picks 4520 in every game: only mine (10,7), in its pocket. It digs 22-27 dirt tiles; u3z dug 163-239 and picked 8527-10969.
  - digmin.py from (3,3): every other mine is 30+ dirt tiles away; the cheapest is (53,4) along the y=2 dirt corridor (50 steps, 30 digs).
  - u4l (u4e + PEACE + MINE_ANY + MINE_TIES) is on the replica now; its set-2 block would test this.
- 22:34: **Delivery-stall census** (dstall.py on u3z's 96 field games; a 'd' round is stalled when the BFS path length to the king has not dropped in 20 rounds): 34878 of 200138 'd' rounds were stalled.
  - Far from the king (31+ steps): TheHeist 4812, fullofsecrets 3560, peaceinourtime 2288, averystrangespace 815, clockwisdom 739, Hike 389. This is TRAIL's class.
  - Near the king (4-10 steps): rose 12089, a queue. u4e won all 4 set-1 rose games, so it is not a flippable loss.
- 22:38: **cw4: pb4 (u4e + TRAIL + TRAIL_GAP 12) won 6 of 8 against s7d; u4e won 5 of the same 8.**
  - fullofsecrets 2 of 2, both at r2000 (u4e lost both). EscapeTheNight 2 of 2, at r692 and r1169: no king-box jam, which was s5d's reason for withdrawal. pipes 2 of 2. clockwisdom 0 of 2 (u4e 1 of 2).
  - On clockwisdom the trail now works: gatherers' delivery spells end at the king (e.g. 43 steps to 4 in 100 rounds). The cheese, though, is on the attackers, who held 760-2700 each and never deliver. pb3's ATK_DELIVER 50 is what fixed that map in cw3.
- 22:38: **u4o built** (= pb4: u4e + TRAIL + TRAIL_GAP 12; basics repair, pre-registered in attempts.csv). Its local screen is hw1 (kvs cells, control kvs = u3v).
  - I first named the screen run tr1, which was taken by an Oct 29 s7d-vs-atrap run (6 results, KEEP file). I overwrote that run's cells.tsv, then rebuilt it from its results.jsonl (all 6 cells) and killed the queued start before it ran. Nothing else there changed.
  - It goes to the replica after u4l, ahead of u4m: a basics repair comes first.
- 22:49: **u4e's 50 losses in sets 1-2** (96 games), by opponent: Xzibit 8, YokoOno 8, Merlin 8, Finals 7, Varun 6, collegejunior 6, Icarus 4, spsquared 3. By map: dirtpassageway 4, learn_your_lesson 4, averystrangespace 4, fullofsecrets 3, clockwisdom 2, and pairs on a dozen others.
- 22:49: **dirtpassageway: our king dies on the centre mine.** Kings start at (3,26) and (46,26) in an open middle band (dirt bands at y 16-20 and 32-35). The mine (20,26) is 9 tiles from its twin (29,26). The mines behind the dirt, (7,39) and (7,12), are listed later.
  - 6060 g1 (u4e vs collegejunior): our king walked (KING_MINE) onto (20,26) by r150 and was bitten from then on, hp 562 at r150 and 4 at r500, then died. A second king formed at (11,30) and lived to r783. Pickups were even (5319 to 5540), but their king damage was 1108-1170 to our 68-114.
  - Against Finals (6090/6091): we picked 2500 to their 4480, they laid 82 rat traps, and they bit our king 57-58 times.
  - u3z lost all 4 of its dirtpassageway games too. Its king was at the centre by r200, and was bitten to death in 3 of them (r171, r239, r378).
- 22:49: **King front census** (kingfront.py on u3z's 96 field games: the first king's position at r200, projected onto the line between the starts). A king past halfway to the bisector won 5 of 15, and 10 of those 15 died. Any other king won 46 of 81. The first king died by bites in 35 games (12 won).
  - FrontScan (scratch, all 74 maps): on 16 maps a home-half mine within dist2 400 has front value under 25 (100 at our start, 0 on the bisector). King.pickHomeMine takes the first listed qualifying mine and keeps it.
  - No earlier attempt guarded KING_MINE's pick. KP_FRONT (u1z) did it for KING_PATH only, which is off; u1z was parked because its motivating map, krotus, was a starvation map.
- 22:49: **u4p built** (u4e + KING_FRONT 25, new switch, BotTest testKingFront; commits 81a29af, 7fa3c9f; pre-registered in attempts.csv). Its screen runs after hw1, and its replica blocks after u4o's. A local probe on dirtpassageway is running (u4p vs arush).
- 22:49: VM disk: I deleted 4 temporary full dumps in ~/diag26b/tmpdc that I had made for this analysis: the first copy of 2cfbf29a54101a99707d (field 6060, re-made since), 94408d9c966ec9e1099d (cw3 pb2 clockwisdom, 7.5 MB), f9d17203551ca7b7a24f (pj1 pb4, 7.6 MB) and 65fe3295d5de87063c41 (cw4 pb4 clockwisdom). They can all be regenerated from their replays.
- 22:56: **u4l rejected after replica block 1** (v4 set 1: 6218-6221, 6247-6250, 6259-6264, 6268-6269, against u4e's set-1 games). 48 cells, 40 alike. D 8: gained 3, lost 5; net -2, t -0.71, and block 1 rejects at t ≤ 0.
  - Gained: peaceinourtime A (Merlin), superioristhmus B (spsquared), clockwisdom B (collegejunior).
  - Lost: rose B and uneasy_alliance A (Icarus), knifefight A and t_minus_five_minutes A (Varun), superioristhmus A (spsquared).
  - PEACE's case, averystrangespace, is in set 2, which this block did not reach.
  - u4l's own dirtpassageway game (6261 g1, seat A) repeats u4e's: the king reached (20,26) by r200 (570 HP), was down to 83 HP by r600, and died before r700. The second king, at (9,34), lived.
- 22:56: **hw1 (u4o's screen) passes**: 44 distinct cells, u4o 32 wins to the u3v control's 30 (the screen rejects only at t ≤ -1.5). u4o is on the replica as submission 148, with 16 v4 set-1 scrims queued.
- 22:56: **Local probe kf1** (u4p vs arush, dirtpassageway, seat A): won at r844. Our king stayed at (3,26) to r100, then went for (7,12) and stopped in the dirt band at (5,17). It kept 600 HP all game. u4p's screen is kf2 (kvs cells, jobs 3).
- 22:56: **u4q built**: u4e + ATK_DELIVER 50, u3g's switch rebased on u4e (u3g never reached the replica). Pre-registered in attempts.csv, with pb3's cw3 clockwisdom result as its evidence.
- 22:56: VM disk: I deleted 4 more temporary dumps in ~/diag26b/tmpdc (35 MB): 2cfbf29a54101a99707d (field 6060), d9403612809ccfad6319 (cw1 u4e vs s7d, clockwisdom), 73375a29f82af082ebc8 (field 6261) and 06cc2d0adeb8909a0ebb (kf1). I also deleted rp/kf1.bc26, my copy of the local kf1 replay. All can be regenerated.
- 23:09: **Xzibit**: u4e won attack_on_cat in all 4 set-1 games and lost learn_your_lesson and starvation in all 4. So did u4l (6259/6260).
  - starvation (35x25; mines (5,11) ours, (17,11) on the axis, (29,11) theirs): they pick 2800-3130 to our 1770-1840. About 1400 of theirs comes from around the axis mine, against about 600 of ours, since without MINE_TIES our gatherers leave it alone. In 6058 we also formed a second king at r115 (6 rats merged), and both starved by r482 (starveDmg 930). In 6059 their bites killed our king (57 bites, 600 damage, r556).
  - learn_your_lesson: our king starves (r947, r1119) while we deal 3-6 times their cat damage. We pick 4388-4971 to their 8616-10190. 9763 cheese was lying on the map by r900.
- 23:09: **learn_your_lesson: half our rat turns chase cheese no one can reach.** Four of its 10 mines, (17,36), (9,23), (30,16) and (22,3), sit in 5x5 rooms walled all round. SealScan (scratch, all 74 maps): such sealed mines also exist on diamond, t_minus_five_minutes, peaceinourtime and externalsustenance, in larger regions.
  - The engine drops a mine's cheese on a random tile of its 9x9 square, or on the mine when that tile is a wall. Most of a room mine's cheese lands inside the room, and rats see it through the walls.
  - 6058 (field, full dump; cspell.py): 320 cheese-chase ('C') spells, 6212 rounds, half of all our rat turns. Only 101 of the spells ended with a pickup. 27 of the 48 cat kills of our rats were on 'C' rats hovering at the rooms, where a cat patrols. A cat's pounce kills from dist2 13.
  - Local reproduction ly1 (u4e vs s7d, seat A): u4e lost at r1455, delivering 3855 to 7872. Its indicator strings show 5416 of its 6051 'C' turns aimed at piles inside the rooms (42% of all rat turns). 350 more were gatherers walking to the room mines, and its king's home mine (17,36) is one of them.
- 23:09: **New switch SEALED** (Seal.java, off; BotTest testSeal). A bounded flood fill from the best new pile over tiles not known to be walls decides whether it is sealed. Unseen tiles count as open. With one symmetry left, a wall's twin counts as a wall. The fill is sealed if it runs out within SEALED tiles without reaching us. Its tiles and their twins get Sym.SEAL, and sense() never picks cheese there. Open verdicts are cached for 20 rounds, and no fill runs with under 11000 bytecodes left.
  - Probe pb5 = u4e + SEALED 30 is replaying ly1.
- 23:33: **pb5's first run overran 10 times; the fill now pauses.** In ly2 (pb5 vs s7d, seat A, r2000) pb5 won on points, delivering 13287 to 10032. But it overran 10 times, where u4e had none. The turns after each overrun showed about 12.6k bytecodes spent between the marks around sense().
  - The fill for a 5x5 room costs about 9k bytecodes. It had checked only once, at its start, that 11k were left, so it could use up the rest of the turn.
  - Seal.sealed now checks before each tile, pauses below RESERVE 10000 and resumes on a later turn. No second fill starts in a turn after a pause. The twin index is now inline arithmetic instead of two calls. The pause and resume have BotTest checks (1817/1817).
  - The rerun ly3 (same pairing) has 0 overruns, maxBc 16181, and won on points r2000, delivering 13560 to 10229 (u4e in ly1: lost r1455, 3855 to 7872, king starved).
- 23:33: **But SEALED barely stops the chase.** By the map's own reachability (scratch SealTiles: 100 sealed tiles in four 25-tile rooms), 9200 of pb5's 12200 'C' turns in ly3 still aimed at sealed tiles (u4e ly1: 5416 of 6051). pb5's 'C' share fell from 47% to 24%, and its 'M' share rose to 32%, 5425 of those turns walking to the four room mines.
  - Cause: a rat sees a room's pile through the near wall long before it sees the far wall, and an unseen tile counts as open, so the fill escapes through the unseen far side.
  - The ly2/ly3 wins are two games on one seat and may be chaos, not mechanism.
- 23:33: **New switch SEAL_SEEN** (with SEALED; BotTest +7 checks, 1824/1824; a mutation that ignores it fails 3). The fill walks only tiles seen, or with one symmetry left tiles whose twin was seen.
  - A region that runs out at unseen tiles without reaching us is skipped for OPEN_TTL 20 rounds and not marked. The sense loop passes over it, so the next-nearest pile is chosen.
  - Vision is not blocked by walls and a cone is convex, so a pile with a clear straight line to the rat always has a seen path and is never skipped.
  - Probe pb6 = u4e + SEALED 30 + SEAL_SEEN. VM matrix sq1 is running learn_your_lesson only: u4e, pb5 and pb6, each vs s7d on seeds 1-3 and vs araid on seed 1, both seats (24 games). Local ly4 is running pb6 vs s7d, seat A, seed 0, with a dump.
- 23:33: **u4q screen kq1 passes**: 44 cells, 34 wins to 30, D 10 (+7 -3). The losses are araid bed B, cheesefarm B and starvation A. u4q goes to the replica after u4o and u4p. u4o's block 1 was at 8 of 16 at 23:20.
- 23:50: **u4o rejected at replica block 1** (#148, v4 set 1, 6281-6284, 6298-6301, 6317-6320, 6332-6335, vs u4e's set-1 reference): 48 cells, 35 alike, D 13 (+3 -10), net -7, t -1.94. Gained YokoOno Stash A, Merlin peaceinourtime A and spsquared superioristhmus B. Lost Finals DefaultSmall B, Icarus TheHeist A, rose A/B and uneasy_alliance A, YokoOno clouds B, Varun t_minus_five_minutes B, Merlin corridorofdoomanddespair A and collegejunior rose A/B. progress/ refreshed.
  - **u4p** (KING_FRONT 25) submitted as #149; its 16 set-1 scrims are queued (14 held by the hourly limit, sent by the drain loop).
- 23:50: **averystrangespace** (v4 set 2; u4e lost all four cells on points at r2000): our cat damage was 0 in every game, against Icarus's 87 and 420 and YokoOno's 2790 and 6400.
  - Our home pocket holds 253 open tiles and one mine, (10,7). It is sealed by dirt: every other mine and both cats' waypoints are 30+ digs away (scratch digpath.py; the centre region of 642 tiles, with mines (25,26) and (34,33), is 31 digs).
  - YokoOno digs out and delivers 11520-14960 to our 4450-4490. Icarus stays home like us (4415-4650) but scores the cats.
  - Local as1 (u4e vs s7d, seat A; VM dump): u4e won on points with kings 2 to 1, cat damage 0 to 0, 26 tiles dug. Attack moves ('a') took 35% of rat turns, walks to the one mine ('M') 30%, waits at the mine ('W') 27%. At r300 and r800 the attackers stood in the dead end at (7-8, 22-26), aimed at the flip spots (4-6,56) and (53,3) with the symmetry unresolved (sym=7).
  - **u4s = u4e + STALL_CAT 100** (u3q rebased; u3q's block 1 on set 1 had 45 of 48 cells alike) is pre-registered for the screen. The four averystrangespace cells are its inspiring cells.
- 23:50: VM disk was 99% full (282 MB free) while the replica stores ~2 MB a match. **Deleted 180 of our own replay copies in VM ~/diag26b/rp (ids below 6000, 252 MB)**; the originals are in the local replay store. VM free is now 530 MB.

## 2026-11-01 00:04 PDT: the end-of-turn seal fill, u4r, and the throw-rush losses

- 00:04: **SEALED's fill moved to the end of the turn** (af65f1e). pb6 (seen-only, run inside sense) still overran once at r1768: a fighter's turn, stage -1. Now sense() only reads verdicts and names one pile to judge (Seal.want). Seal.work runs the fill just before sense2, on the bytecodes above RESERVE = SENSE2_LEFT + 500. It pauses there and resumes next turn, and drops a fill still unfinished after PEND_MAX 10 rounds. BotTest 1828/1828. A mutation that drops the leak line fails 3 checks.
- 00:04: **sq1** (VM, learn_your_lesson seeds 1-3 vs s7d, both seats): u4e won 1 of 6 (delivered 4252-11902). pb5 won 6 of 6 (12113-16781). pb6 won 6 of 6 (12839-16428). Against araid (seed 1) all three won 2 of 2. No overruns anywhere.
  - **u4r = u4e + SEALED 30** (end-of-turn fill, SEAL_SEEN off) is pre-registered as a basics repair: the battery row "movement: no unreachable target". Its inspiring cells are Xzibit learn_your_lesson A/B. Its screen kr1 and a learn_your_lesson check sq2 are running on the VM. SEAL_SEEN stays a possible follow-up.
- 00:04: **Loss census, u4e's 49 lost field games** (sets 1-2): king starved about 24, bitten about 14, early throw rush 4 (knifefight A r90, thunderdome A/B r139/r120, tiny A r133), points 7 (averystrangespace x4, fullofsecrets B, learn_your_lesson B, superioristhmus B).
- 00:04: **Thunderdome throw rush** (6079 g2, u4e as A vs Merlin, lost r139; full dump; scratch throwrush.py, whyb.py). The kings start 8 apart.
  - Both sides grab enemy rats and throw them at 42 a hit. We threw 37 times: 5 hits on their king in r22-41 (600 to 364 HP), 27 on rats. They threw 80 times: 15 hits on our king in r74-139, which killed it, and 56 on rats.
  - All 15 hits on our king were our own rats, grabbed beside it and thrown into it.
  - Our attackers (spawn index 4-15) had 11 rats within Chebyshev 4 of their king at r20-39. Their king walked off to (31,13), and by r60-79 none of ours were within 8 of it. The attackers were dead (52 of our rats died to their 23), and the rest gathered near home.
  - The defences for this class have all been tried: AMMO_AWAY (s8y, s9v, u1v: rejected on the replica) and GUARD_KING_R (s8d, s9w, u2w: rejected). No new candidate from it yet.
- 00:04: The clockwisdom repro cwk1 (VM, u4e vs s7d) was a repeat of 22:21's cw1. u4e delivered 40 to 1160 and lost to starvation at r930. That class is u4q's (ATK_DELIVER 50).
- 00:04: Disk deletions since 23:50, all our own regenerable data:
  - VM: the temporary full dumps tmpdc/ff523301c9a011ccf01d (field 6058, 6.2 MB), 9aac59a7c43e84eba14a (field 6079, 4.1 MB) and 2cfbf29a54101a99707d (field 6060, 9.9 MB); rp/6058.bc26 (my copy of that replay); and ~/diag26b/cwk1 (8.7 MB).
  - Local: build/kf1 (916K), build/ly2 and build/ly3 (22 MB): dumps of local probe games.
- 00:08: **Field census of u4e's 96 games (sets 1-2; scratch econcensus.py and a summary scan of the lite dumps).**
  - How games end. Our 46 wins: 20 by killing their king (median r226), 15 by their king starving (median r639), 11 on points. Our 50 losses: 24 by our king starving (median r683), 19 by it being killed (median r690), 7 on points. The early king kill is our main win, which fits the rejected rush cuts (u0o, u1o, t0g).
  - Economy. We hold 20-22 babies to their 16-17 from r25 to r500, and our bank sits 250-600 below theirs from r50 on. In wins both sides deliver about 15 per 100 baby-rounds. In losses their rate pulls away from r150: 19.3 to our 13.8, then 23.8 to 15.5 at r200 and 29.5 to 17.9 at r300.
  - Combat, per king-kill loss (43 games): enemy rats killed 45.6 of ours to our 32.7 of theirs. Trap deaths: ours 12.8, theirs 0. They lay 41.7 rat traps a game and hit our rats 38.6 times; we lay 0. **Their traps make up the whole kill gap.** Throws and bites are about even (throwLand 20.6/22.0, throwHit 5.7/6.7, bite 5.8/4.0). In wins we lose 5.2 rats a game to traps.
  - So **u4m (u4e + u3d's reactive baby rat trap set) moves to the front of the queue, right after u4p.** u3d passed replica block 1 on u2g (t +0.58), and five trap-dodging attempts have lost. Then u4q, u4r, u4s.
- 00:08: **Stash vs YokoOno** (6040, u4e as B, delivered 20 by r400). After the dirt gap (1,7) was dug at r25, 85-95% of our rat turns stayed in the home pocket. Attackers and mine-runners pressed against the wall under the centre mines at y 11, and the few rats that went out died to cat pounces, throws and traps while YokoOno held the centre with 17-23 rats.
  - Locally u4e vs s7d (VM st1/st2): it won as B at r328 (delivered 2702 to 765) and lost as A at r933 (4409 to 7200). So this loss is to the centre-holding opponent, not to navigation; no candidate.
  - VM: deleted the st1/st2 game dirs (11 MB, regenerable).
- 00:09: **u4r's learn_your_lesson check sq2** (VM, vs s7d, seeds 1-3, both seats): 6 of 6, all on points at r2000. It delivered 12966-17245 (mean 14992), with 0 overruns and at most 16010 bytecodes in a rat turn. u4e in sq1 won 1 of 6 (4252-11902). This matches pb5 and pb6, so the end-of-turn fill keeps the effect without the overrun. The screen kr1 is at 13 of 48.
- 00:14: **Where field opponents lay rat traps** (scratch trapgeo.py over u4e's 96 lite games). Per game laid / fired on us: Finals 55.4/54.2, Varun 45.9/37.2, spsquared 37.9/35.4, Icarus 33.5/32.0, Xzibit 30.1/26.9, YokoOno 20.2/17.8, collegejunior 10.7/8.3, Merlin 3.7/3.7.
  - The median trap fires 0-3 rounds after it is laid. 53-80% are laid within Chebyshev 4 of a mine, and for Finals, Icarus, Xzibit and spsquared about 70% are on our half. These are reactive traps laid in front of our rats at the mines, not minefields.
  - Engine (TrapType): a rat trap costs 20 cheese, deals 50 damage and stuns for 30 turns, fires on an enemy entering its 3x3, and a team may hold 25.
  - dirtpassageway vs Finals (6090/6091 g1, both lost): they laid 82 traps a game, and 29-30 of our rats died to traps.
  - This is u4m's case, which goes next. A fuller trap doctrine (lower cheese floors, traps at contested mines) waits for u4m's field result.
- 00:14: **Digging** (lite dumps). Opponents dig 2-13 times more than we do on the dirt-heavy maps: superioristhmus 552/game to our 93, ratsareants 365/189, averystrangespace 365/28, streetsofnewyork 191/94. We won 2 of those 10 games.
  - On averystrangespace (6098 g1, scratch digmap.py) YokoOno dug 348 tiles in 800 rounds, tunnels the length of the dirt corridors from its pocket to the centre mines. We dug 23.
  - DIG_COST (Flow's dirt tile cost, 4) was only ever probed in 8-game local diags (u2d, u2e). Probes pb7 = u4e + DIG_COST 2 and pb8 = u4e + DIG_COST 1 are queued as VM run dg1: u4e, pb7 and pb8 vs s7d on superioristhmus, ratsareants, averystrangespace, streetsofnewyork, dirtpassageway and cheesefarm, both seats, 36 games. It starts when kr1 ends.
- 00:14: The structural slot is overdue: u4m to u4s are seven incremental attempts since u4l. The next structural design is a dig doctrine or a trap doctrine, whichever dg1 and u4m's block 1 support.
- 00:15: Trap deaths per game in u4e's 96 field games: 25 of 50 losses had 10 or more of our rats killed by traps and 14 had 20 or more; of 46 wins, 9 had 10 or more and none had 20. The worst were ZeroDay (Xzibit, 39 dead, 114 traps laid), ratsareants (Varun, 36 and 33), mercifullattice (Icarus, 34 and 27), superioristhmus (spsquared, 31), and jail and dirtpassageway (Finals, 23-30).
- 00:25: **u4r's screen kr1 passes**: 44 cells, 36 wins to the control's 30 (D 14: +10, -4), xfer 305253 to 295383. Plus cells: abrk on DefaultSmall A, arrows B, canyoudig B and evileye A; araid on DefaultMedium A, arrows B, bed A, canyoudig B, learn_your_lesson A and tiny B. Minus cells: abrk on Stash B, bed B and cheesefarm A; araid on evileye A.
- 00:25: **kr1's 3 overruns, all on tiny A** (2 at 17502 bytecodes, 1 at 17545). The same tiny cells in kvs, kq1 and kf2 had 0 overruns as A.
  - First guess: the fill allocated its arrays (W·H ints) on its first call, before it checked the bytecode reserve, and the engine charges an array's length in one go without an interrupt (InstrumentingMethodVisitor NEWARRAY). I moved the allocation after the check (ratbot and u4r) and added a BotTest check, "no arrays are allocated short of bytecodes", which fails on the old code. BotTest 1829/1829, unit tests exit 0.
- 00:28: **That guess was wrong.** Recheck kr2 (VM, the four tiny cells with the fix) gave the same games and the same overruns, to the bytecode.
  - Dump ov1 (u4r vs abrk, tiny): the 17545 turn is rat 11834 at r37, biting the enemy king in the melee. OVS=2208 means it crossed the limit between mark 22 and mark 8, in biteEnemies / tryBiteCat / layLane. The bite went through, and the rest of the turn (1812 bytecodes) ran in r38.
  - In the same cell u4e peaked at 17113, so melee turns on tiny already run within about 400 bytecodes of the limit. u4r's games diverge from r11, so this is a different melee, not the seal fill's cost.
  - Field: u4e's 96 replica games had 0 overruns (the highest rat turn was 17194, 6078 g2). Not worth a candidate. I kept the allocation order fix; it does no harm.
- 00:37: **u4p block 1** (replica #149, KING_FRONT 25; v4 set 1, 6343-6346, 6356-6359, 6378-6381, 6391-6394) against u4e's set-1 games: 48 cells, 45 alike, D 3 (+2 -1), net +1, t +0.58. It gained AdamTan12_Finals tiny A and spsquared bed B and lost spsquared bed A. Block 1 rejects at t <= 0, so u4p goes on: block 2 on v4 set 2 is queued (16 scrims; waiter build/wait_u4pb2.sh). u4m waits behind it. progress/ refreshed.
- 00:40: **Where the cheese goes** (scratch spend.py over u4e's 96 lite games). In the 43 king-dead losses (starved or killed, mean r670) we delivered 2234 to their 4651 and spent 2932 on spawns (69 rats) to their 3545 (78). So we don't overspend; we under-earn. In the 35 king-kill wins we delivered 2385 to their 1261.
  - By half (scratch halfshare.py): in losses they picked 2422 a game on our half (we picked 4091 there) and our dead rats dropped 768 there; in wins they took 506 and we dropped 296. Their own half gives them 6638 (L) and 5908 (W).
- 00:40: **fullofsecrets B vs Merlin** (6134 g1, the most enemy picks on our half: 11610 to our 6435; lost on points at r2000, delivered 3865 to 9575).
  - A cat came into our king's pocket at r55-105 and our rats bit it from 4000 to 510. At r110 it pounced over the wall row y=15 into the 5-wide corridor above, beside our nearest mine (37,18), and sat there at 410 hp until r2000. That corridor opens only at its far end, through dirt Merlin dug.
  - CAT_DEFEND (r2 36 of a king) and the FIGHT cat branches drew our rats to it. 10908 of our 38592 rat turns had why 'g' (the cat branch), 5169 of them standing still under the wall. The jam in the king's pocket stalled 3909 delivery ('d') turns. The mine took 4709 of our rat turns and gave us 840.
- 00:40: **Stalled-turn census** (scratch gstuck.py; each of u4e's 31 field matches dumped, measured and deleted again on the VM). A stalled turn is a rat on the same tile as its last turn with no bite, pickup, transfer, dig, throw or nap. Over 90 games, 26.8% of our rat turns were stalled (wins 27.8%, losses 25.6%).
  - Outside 6134 g1 the cat branch is small: 9622 stalled 'g' turns over 90 games, against 5169 in that one game. A cat-reach switch (CAT_REACH: a rat whose walk to the cat stalls or is a known detour leaves cats alone for a while) is coded in ratbot, off. It would fix a rare case.
  - The big stall counts are on the attack branch 'a' (44% of 324k turns), the mine walk 'M' (26% of 489k) and delivery 'd' (41% of 197k), from each game's top four letters.
  - Engine (InternalRobot.addMovementCooldownTurns): a baby's move costs 10 cooldown forward and 18 in any other direction (MOVE_STRAFE_COOLDOWN), times 1 + 0.01 per cheese carried. A rat can move only below 10. So a strafe every turn moves about every other turn. Nav.step turns first when it can, but turning has its own cooldown, and any turn spent earlier in the turn (facing an enemy, sweeping a cone) makes the step a strafe. Next: measure strafe moves and cooldown-blocked turns in the 6134 dump.
- 00:45: **Strafes are not the stall** (scratch strafe.py on the 6134 dump). We strafe on 0-10% of moves on the economy maps (fullofsecrets 40 of 23581), so Nav.step's turn-then-step works. The waits come from cheese weight: delivery 'd' turns are cooldown-blocked 39%. With a load of c the trip home takes L(1 + 0.01c), so throughput c / (L(2 + 0.01c)) still grows with c, and bigger loads stay right.
- 00:45: **u4t registered** (u4e + CAT_REACH 30: a rat on a cat branch whose walk to the cat stalls 30 turns or is a known detour leaves cats alone for 100 rounds; basics repair). BotTest 1836/1836 with a new testCatReach. A mutation that drops the detour clause fails 2 checks. Unit tests exit 0. Screen cr1 (kvs cells) runs after ks2. Targeted check cr2 (u4e and u4t vs s7d on fullofsecrets, both seats, seeds 0-2) runs after dg1. Replica: at the end of the queue.
- 00:45: **Attackers hoard cheese.** In 6134 g1 our attack-branch turns ('a', 4481) carried 280 cheese on average. 1964 of them were cooldown-blocked by the weight, and our king ended on 12 hp. This may be u4q's case (ATK_DELIVER 50, from clockwisdom). Census gstuck2.py (per why letter: moved / cooldown-blocked / still / busy, cheese carried, turns near their king) is running over the other 30 field matches.
- 00:49: **Per-letter census** (scratch gstuck2.py and gs2agg.py over u4e's 31 field matches, 93 games). Shares of our rat turns, wins / losses: mine walk 'M' 31.9 / 26.3%, attack 'a' 16.4 / 22.8%, wander at the mine 'W' 11.0 / 17.8%, cheese chase 'C' 15.9 / 8.7%, delivery 'd' 14.6 / 8.5%.
  - In losses 40% of attack turns stood still with the rat ready to move, and only 1% were within Chebyshev 4 of their king. Nearly all of it is averystrangespace (6131, 6098, 6132, 6099 g1, all lost on points), where 22-28% of all our rat turns were attackers standing in the dead end. That is u4s's case (STALL_CAT 100, screen ks2 nearly done). Elsewhere it is 10% at most (Stash 6040).
  - Attackers' carried cheese, per round of the game: TheHeist wins 5100-5800 held. Losses: clockwisdom 6061 3077, fullofsecrets 6021 1811, peaceinourtime 6079 1373, clockwisdom 6060 685, fullofsecrets 6134 628. Mean 638 in wins and 176 in losses. Hoarding is real but does not mark losses in general; it is u4q's case on clockwisdom, fullofsecrets and peaceinourtime. The queue order stands: u4p block 2, u4m, u4q, u4r, u4s, then the rest, u4t last.
- 00:51: **u4s's screen ks2 passes** (STALL_CAT 100 on u4e; kvs cells): 44 cells, 32 wins to the control's 30, D 2 (+2 -0: abrk evileye A, araid learn_your_lesson A). Delivered 274006 to 295383 (-7%). 1 overrun (tiny B, as the control). It goes to the replica after u4r. cr1 (u4t's screen) starts now.
- 00:55: **Where the cheese was when our king starved** (u4e's 31 field matches, light dumps). In 30 games our bank fell under 2 (29 losses). In 24 of them our rats held almost nothing at that point (0-280 raw, often with 0-3 babies left): the economy was already gone. In the other 6 they held plenty: clockwisdom 6061 7280 and 6060 2080, fullofsecrets 6021 4358 and 6133 825, ratsareants 6100 1405, rose 6078 1280 (a win). This matches the 3198 finding and u4c's falsified check: HUNGRY fires, but carriers this heavy crawl and never reach the king. So it is u4q's case (attackers delivering at 50) and a carry cap, not a later trigger. Nothing new to build here.
- 00:59: **Our losses are mostly the opponent out-harvesting its own half** (scratch halfgame.py, u4e's 49 lost field games). Cheese spawns in mirrored pairs (GameWorld.spawnCheese: each mine and its twin get 20 at once), so both halves yield the same. Yet in most long losses the opponent picks 1.5-3x on its half what we pick on ours: ratsareants 6100 10027 to 5549, mercifullattice 6131 13414 to 7550, learn_your_lesson 6117 15166 to 8148, averystrangespace 6098 12920 to 4520. Raids on our half are smaller (they took 0-2100 on most maps, more only on ZeroDay, mercifullattice, fullofsecrets 6134 and superioristhmus). We often have more babies at r300 (20 to 16) and fewer by r1000.
- 00:59: **dg1** (dirt path cost probe; u4e, pb7 = DIG_COST 2, pb8 = DIG_COST 1, vs s7d on 6 dirt maps, both seats, seed 0): u4e won 12 of 12, pb7 10, pb8 10.
  - On averystrangespace pb7 dug 222/229 tiles to u4e's 26/22, did 1230/2495 cat damage to 0/0, and scored 79/80 points to 29/29. pb8 dug 267/346 and scored 63/73.
  - pb7 lost ratsareants A (r362, bitten in an early war; u4e won that cell at r292) and streetsofnewyork A (king starved at r1424, delivered 5425 to u4e's 17159). pb8 lost ratsareants B and streetsofnewyork B on points at r2000.
  - **u4u registered** (u4e + DIG_COST 2, the same Cfg as pb7). Inspiring cells are set 2's averystrangespace x4, which u4e lost on cat damage (0 to 87, 420, 2790, 6400). The kvs screen has no dirt maps, so it gets two VM runs after cr2: screen dz1 (kvs cells) and check dz2 (u4e and u4u vs s7d on ratsareants, streetsofnewyork and averystrangespace, both seats, seeds 1-2).
- 01:10: **cr2** (u4e and u4t vs s7d on fullofsecrets, both seats, seeds 0-2): u4e lost all 6 and u4t lost all 6. u4e delivered 0-540 per game to s7d's 960-2820 when its king starved (r545-712), and 4475 to 10798 in the one game that went to r2000. So CAT_REACH does not touch this map's problem. u4t keeps its screen (cr1, running) but has no case left here.
- 01:10: **Gatherers by mine** (scratch minepair.py on light dumps, minework.py on the full dump of field match 6131). Each mine and its mirror twin spawn the same cheese. On mercifullattice 6131 (Icarus) our rat-turns at the three home mines 12-14 steps from our king were 4359, 3712 and 2109 (2248 of them idle 'W'), and we took 60-85% of what they spawned. The two home mines 22 steps out got 506 and 214 turns: we took 1780 and 500, Icarus 940 and 2140 of the second. Icarus took 2300-3100 at each of 8 mines and formed a second king on the twin of our nearest mine at r200. MINE_BALANCE gives every known home mine the same share of gatherers, so near mines crowd and far ones starve. Past mine-coverage attempts (s3t, s4e, s7v/s7w, s9b, u0w/u0x/u1a, u4f) never weighted the share by trip length. Noted as a structural candidate.
- 01:10: **The king's pile walk jams delivery on fullofsecrets** (local u4e vs s7d, seed 2, as A, full dump build/fs1). u4e picked 4630 and transferred **0** in 545 rounds. Carriers stood at (6-9,27-30), Chebyshev 4-5 from the king, outside its pocket's south wall. Their target was the king's tile with Nav stall 0-2. The king (km=p, King.move) saw piles of mine (2,26) through that wall and walked to them with King.step, which allows sidesteps that keep the distance. It went (2,32)-(3,32)-(2,32) every 4 rounds, the king's move cooldown, all game: 97 of its 123 moves went back to the tile it had just left. Each step is a new goTo target and resets Nav's stall, so the carriers never reached FLOW_STALL 4. Field 6134 B shows the same: 121 of the king's 275 moves (scratch kingosc.py). This is the mechanism behind the earlier fullofsecrets 'd' stands (s4w's NAV_SAME_R and u4g's DELIVER_ANCHOR treated the carriers' side).
  - New switch KING_PILE_STRICT (off): the pile walk takes only strictly closer steps (King.closer, already used for the mine walk). New BotTest testKingPile (1839/1839). A mutation that ignores the switch fails 2 checks.
  - u4v = u4e + KING_PILE_STRICT (basics repair, navigation) is built. Local check on fullofsecrets vs s7d is running.
  - Our attackers broke the truce at r354 by grabbing an enemy rat (RUSH_BREAK, by design). Several carried 160-440 cheese toward the enemy king meanwhile (u4q's case).
  - Local deletion: build/fs1 (the 4.9 MB u4e dump above; regenerable with arena.py game).

## 2026-11-01 01:31 PDT: u4p rejected at block 2; the king's sidesteps (u4v); u4m on the replica
- 01:12: The first u4v (pile walk only) still lost seed 2 A at r637 with 3 transfers. The king had switched to its home-mine walk (km=z): no dig opens the way through the wall, so it fell back to King.step and went back and forth on 80 of 112 moves. The switch now covers both walks and is renamed **KING_STRICT** (King.walkStep: closer under the switch, else step; used by the pile walk, the no-dig mine walk and the 'z' fallback). BotTest testKingPile tests walkStep (1839/1839). The mutation still fails 2 checks. Unit tests exit 0.
- 01:20: **u4v = u4e + KING_STRICT, local check vs s7d on fullofsecrets** (u4e lost all 6 cr2 games there):
  - seed 2 A: won at r947, king killed. 18 transfers, 2670 delivered to 1020, 8780 picked. The king moved twice in the game (u4e: 123 moves).
  - seed 0 A: won on points at r2000, 56 to 43. 46 transfers, 5370 delivered (u4e lost this one at r712 with 540).
  - seed 0 B: lost at r971. Delivered 4514 to 1234, but the king starved during the war we started at r414 (u4e lost it on points, 4475 to 10798).
  - Local deletion: build/fs2 (38 MB of these dumps, regenerable), to make room for the replica fetch.
- 01:25: **u4p block 2** (v4 set 2: 6402-6405, 6415-6418, 6427-6430, 6535-6538) vs u4e's set-2 games: 48 cells, 48 alike, D 0, t 0.00. Pooled with block 1: 94 cells, 91 alike, D 3 (+2 -1), net +1, t +0.58, at or below block 2's bar of 1: **u4p (KING_FRONT) rejected**.
- 01:27: **u4m submitted** (#150: u4e + u3d's reactive baby rat trap set) for block 1 on v4 set 1. 4 scrims sent, 12 queued for the drain. Waiter build/wait_u4mb1.sh (matches above 6538).
- 01:28: On the VM, u4v's screen **vs1** (kvs cells, control kvs = u3v) is running. I paused cr1 (u4t's screen) to free the slot before noticing it was at 43 of 48. I killed its one orphaned game (and deleted that game's partial replay) and resumed it for the last 5 cells.
- 01:30: Queue: u4m (on the replica), then **u4v** if vs1 passes. KING_STRICT is a basics repair, so it goes ahead of u4q, u4r, u4s and the rest. u4u (DIG_COST 2) waits on dz1 and dz2.
- 01:46: **cr1 (u4t's screen, kvs cells, control kvs = u3v) passes**: 44 cells, 33 wins to 30, D 5 (+4: abrk DefaultSmall A, abrk arrows B, araid arrows B, araid bed A; -1: araid starvation A), xfer -2.5%. But u4t overran twice in each of the two Stash A cells (0 for the control and for u4s there). That has to be fixed before u4t goes to the replica. It stays at the end of the queue.
- 01:46: **Home mines saturate** (local u4e vs s7d on mercifullattice, full dump, since deleted). Every home mine gave 4140-4880 cheese, whether 1 or 10 of our gatherers worked it. Spawn, not labour, limits each mine, so a gatherer share weighted by trip length would not raise the harvest. Dropped as a candidate. What the field games show instead is that we lose mines on our side to raiders and don't take any on theirs.
- 01:46: **King count census** (u4e's 96 field games, light dumps): even. At r600 in losses we had 1.12 kings to the opponent's 1.16.
- 01:46: **Combat census** (same games): in losses, enemy rat traps killed 12.2 of our rats per game and ours killed 0 of theirs. They laid 39.7 rat traps per lost game (36.8 fired) and we laid none. Our deaths to the enemy were 42.5 per lost game to their 30.2. This is u4m's case (u3d's reactive trap set, on the replica now).
- 01:46: **The king's back-and-forth in the field** (scratch kingosc.py on full dumps of u4e's 31 field matches, 93 games; each dump deleted after use, except 6134's). A back-and-forth move returns the king to the tile it left 2 moves earlier. It is everywhere, in wins too: 2508 of 4509 king moves in 45 wins and 1721 of 3806 in 48 losses. On rose the king went back and forth on 95-98% of its moves (runs of 106-263 in a row) and we won all 4. It hurts only where carriers can't follow the king. That is fullofsecrets, where u4e transferred 0. On dirtpassageway (3 losses, runs of 71-119 in a row), transfers kept pace with pickups (6060: 4524 of 5319 picked by r800). Those losses are the king dying on the centre mine (22:49). So KING_STRICT's case is one map, and its screen (vs1) has to show it does no harm elsewhere.
- 01:46: Local deletions (both regenerable): build/ml1 (27 MB, the mercifullattice dump above) and build/th1 (3 MB, a TheHeist dump).
- 01:57: **u4u (DIG_COST 2) rejected at screen.** dz1 (kvs cells, control kvs = u3v): 44 cells, 25 wins to 30, D 13 (+4 -9), t -1.39, xfer -11%. None of these cells is on its inspiring map (averystrangespace), and off the inspiring cells t >= 0 is required. The losses spread over cheesefarm (3), bed (2), DefaultSmall, evileye, starvation and wallsofparadis. In the dirt-map check dz2 (vs s7d on ratsareants, streetsofnewyork and averystrangespace, both seats, seeds 1-2), u4u and u4e each won 10 of 12. u4u scored more points (810 to 593; on averystrangespace 310 to 206, from cat damage 4192 to 2632) but delivered 12% less and dug only 5% more. So a cheaper dirt cost moves rats toward cats more than it opens the map. A dig doctrine would have to be something other than this parameter.
- 01:57: **u4t's overruns**: rerun locally (u4t vs araid, Stash, seed 0, as A; dump build/ov2). Two overruns, both by rat 11306 at (12,8) on r317 and r319 (17504 and 17500 bytecodes). It was carrying an enemy rat, action ready, and threw nothing for 5 turns (mv=f). The turn crossed the limit between marks 21 and 22 (OVS=2122), a stage that costs almost nothing, so the stages before it had already spent the budget. The costliest of them was 7 to 10 (YIELD_KING, CARRY_AIM's aimCarry, rescue) at 6630. A probe copy (bots/pb9, untracked) that traced only r312-322 shifted the game (it ran to r790 without that overrun). The probe now keeps a per-stage trace of every turn whose own stages pass 12000 bytecodes before Flow.work, and prints it in the next indicator. It is running on Stash vs araid and abrk.

## 2026-11-01 01:14 PST: u4m rejected at block 1; u4v on the replica (daylight saving time ended at 02:00 PDT, so times from here are PST)
- 01:14: **u4v's screen vs1 passes** (kvs cells, control kvs = u3v): 44 cells, 30 wins to 30, D 12 (+6: abrk DefaultSmall A, arrows A, evileye A; araid bed A, learn_your_lesson A, starvation B; -6: abrk DefaultSmall B, bed B; araid Stash A, arrows A, cheesefarm B, jail B), t 0.00, xfer -6.6%, 1 overrun (tiny B, as the control). Its inspiring cells (fullofsecrets) are not in the screen, and t >= 0 holds on the rest.
- 01:14: **u4m block 1** (v4 set 1: 6539-6554) vs u4e's set-1 games: 48 cells, 37 alike, D 11 (+5: Finals tiny A, YokoOno Stash A and arrows B, Varun DefaultMedium B, spsquared superioristhmus B; -6: Finals DefaultSmall B and tiny B, Icarus uneasy_alliance A/B, Xzibit attack_on_cat B, Varun knifefight A), net -1, t -0.30: **u4m (u3d's reactive baby rat trap set) rejected** (block 1 rejects at t <= 0).
- 01:14: **What u4m's traps did** (scratch trapcmp.py on light dumps of the 16 matches against u4e's 16 on the same set-1 cells). Per game, u4m laid 21.5 rat traps and 20.8 fired (u4e: 0). Our rat kills rose from 24.6 to 29.5 and our deaths went from 28.8 to 30.3. The opponents spawned 80.0 rats a game instead of 69.9 and delivered 15% more (4071 to 4700), while ours rose 3.5% (3333 to 3451). In losses we delivered 1379 a game (u4e 2445). The traps cost about 430 cheese a game, about 88 cheese per extra kill, and the opponent rebuilt the rats. So matching the field's trap count is an even trade at best. The field's trap edge over us is not the whole story of the kill gap: the trap-dodging attempts (u1b, u1l, u1n, u3i) and now the trap-laying one have all come out even.
- 01:14: **u4v submitted** (#151) for block 1 on v4 set 1 (16 scrims queued; waiter build/wait_u4vb1.sh, matches above 6554). Queue after u4v: u4r (basics repair; ahead of u4q now), u4q, u4s, then the rebases (u4h, u4a, u4n, u4c, u4f), u4t last (its overrun first).
- 01:14: Local deletions (regenerable): build/ly1 (11 MB) and build/ly4 (22 MB), learn_your_lesson probe dumps from u4r's diagnosis, to make room for the 16 replica replays (local free space had fallen to 77 MB). VM: the 16 replays copied to ~/diag26b/rp/6539-6554 and light dumps in ~/diag26b/lite/6539-6554.
- 01:18: **u4t's overrun found.** A stage probe (bots/pb9, untracked: u4t with per-stage bytecode counts kept for every turn whose stages pass 12000 before Flow.work, printed in the next indicator) ran Stash vs araid (both seats, seeds 0-1) and vs abrk. It caught one overrun, rat 10806 on r136, carrying an enemy rat (mv=f). Its trace: aimCarry (CARRY_AIM) spent 6623 bytecodes and found no throw. Its entry guard only asks for THROW_BC = 7000 left, so the turn ran over in the pick-up stage after it (OVS 2021; u4t's rat 11306 at r317 crossed in the next stage, 2122). New switch **AIM_LEFT** (default 0): aimCarry stops its direction loop once 3000 or fewer bytecodes are left and takes the best throw so far. BotTest testCarryAim has a floor case (1840/1840); the mutation fails it; unit tests exit 0.
  - The probe also shows how often the turn itself is heavy. 5081 baby turns in the four games spent more than 12000 bytecodes before Flow.work. The big stages were 2 to 3 (2493 on average), 1 to 2 (2199), 12 to 13 (flank and pressKing, 1484) and 0 to 1 (1380). The traces were cut by the indicator's length cap, so these are lower bounds.
- 01:18: **u4w = u4t + AIM_LEFT 3000** (pre-registered; supersedes u4t for the replica). Screen wz1 (kvs cells, jobs 3) is running on the VM.
- 01:28: **Damage to our kings in u4e's king-kill losses** (scratch kingdmg.py, light dumps of 43 such field games): bites 247 a game, thrown rats 109, the cat 49, traps 2. The sieges are long. On arrows the first bite came at r22 and the king died at r690. Their kings bite only in war, after their spawns. We already boost our bites with cheese. No cheap lever here: the king dies to rat bites over hundreds of rounds, so it is lost by whoever has more rats.
- 01:28: **Mine census** (scratch minepair.py + minefirst.py over u4e's 93 field games, mines on our half by Chebyshev steps from our first king, each paired with its mirror twin). In losses, per home mine we pick less than they pick at the twin in every band: up to 10 steps, 1681 vs 2094; 11-20, 1019 vs 1543; 21-30, 640 vs 1475 (18 of 43 such mines we never picked, against their 12); over 30, 126 vs 760. In losses they also pick at OUR mines: 331, 539, 1009 and 786 per mine by band, about 2600 a game, some 40% of what our half yields. In wins we take about 3000 a game from theirs. We arrive late. The median first pick in losses is r104 for us against r64 for them at 11-20 steps, and r180 against r152 at 21-30. Opponents first pick at 22-step mines by r25-31 (mercifullattice r25-27, ratsareants r29-31, streetsofnewyork r31, cheesefarm r24; on jail r39 at a 26-step mine we never picked, from which they took 1980). Their first babies must walk straight to the mine. No API call lists mine locations (king vision r² 25, baby r² 20 in a 90° cone), so either they scout fast or they know the maps. Our first picks at such mines: dirtpassageway r110-238, mercifullattice r196/254, streetsofnewyork r103-132.
  - Past attempts on mine discovery (attempts.csv): s3f scouts (adopted), s6m HOME_PICK 1 (accepted), s7v/s7w MINE_EXPLORE (neutral), s9c-s9j SCOUT_FRONTIER/SCOUT_MINES (rejected), s4d SEEK_CROWD (rejected), s6l MINE_MIRROR (dropped). The field replays carry no indicator strings, so they can't tell us when our king learned each mine. To tell late discovery from late allocation, mz1 is running on the VM (u4e vs s7d, seed 0, on mercifullattice, ratsareants, streetsofnewyork, cheesefarm, jail, with dumps). Scratch minesee.py then puts, for each mine, the round it first appears in the king's mine list (ml=) next to the round of the first pick.
- 01:28: Local deletions (regenerable probe dumps from u4t's overrun hunt): build/ov2 (6.1 MB), build/ov3 and build/ov4 (30 MB).
- 01:44: **u4w's screen wz1 passes** (kvs cells, control kvs = u3v): 44 cells, 33 wins to 30, D 5 (+4: abrk DefaultSmall A, abrk arrows B, araid arrows B, araid bed A; -1: araid starvation A), t +1.34, xfer -2.3%, **0 overruns** (u4t's cr1 had 5: Stash A 2+2, tiny B 1). Against u4t's cr1: the same 33 wins. The overrun fix holds; u4w takes u4t's place, last in the replica queue.
- 01:44: **Late discovery, not late allocation** (mz1 on the VM: u4e vs s7d, seed 0, five maps, full dumps; scratch minesee.py). For our seat (A) the first pick at each home mine comes 3-40 rounds BEFORE the mine enters the king's list: a rat finds a pile by the mine first, and the king hears later. So first picks wait on discovery. Mines 21-25 steps out: known r58-113 (mercifullattice, ratsareants, streetsofnewyork, cheesefarm), but jail (52,35) 26 steps was first picked r204 and listed r436, and streetsofnewyork (22,54) 24 steps r805/r1032. s7d as B found jail's twin by r49. Field opponents first pick at such mines by r25-31.
- 01:44: **But the early economy is even** (scratch mineera.py, u4e's 96 field games, cheese per game by round bucket). In losses, at own-half mines r0-149: us 605, them 653. r150-399: 1181 vs 1325. r400-799: 1164 vs 1832. They also pick at our mines: 227 a game in r0-149, 644 in r150-399, 780 in r400-799, 1127 from r800 (we take 104 / 132 / 246 / 127 at theirs). So the gap opens after r150 and grows with time. It is a fight for mid-board mines once their army is bigger, not a discovery race. This matches the record: every scout variant (s4d, s6l/s6o, s7v/s7w, s7z, s9c-s9i, t0x/t0y, u2t, u2u/u2x) was neutral or worse, and s7w matched the field's discovery pace and still lost its block.
- 01:44: **Deaths by cause** (u4e's field games, per game). Losses: ours thrown-and-landed 19.2, enemy traps 12.2, cat pounce 9.7, bite 5.5, crush 5.4, throw hit 5.0; theirs: thrown 20.3, pounce 11.7, crush 7.8, throw hit 6.3, bite 3.8, traps 0. Grabs 77.3 by us to 77.8 by them. When they backstab first (29 of 50 losses, median r28) they spawn 92.9 to our 76.0. The trade is even apart from their traps; traps are the asymmetric loss. Wins: our deaths by trap 4.7.
- 01:44: **Split cells** (u4e's sets 1-2, won one seat and lost the other on the same map): 10. Five are fast king kills: Finals DefaultSmall (A lost r201, B won r150), Finals tiny (A lost r133, B won r218), Merlin knifefight (A lost r90, B won r23), YokoOno streetsofnewyork (A won r89, B lost r911), spsquared bed. knifefight is a throwing war: as B our thrown rats did 806 throw-hit damage and killed their king at r23; as A their 70 throws to our 41 killed ours at r90.
- 01:44: VM deletion: runs/mz1 (101 MB, the five dumped games; regenerable, results above) when the VM's free space fell to 60 MB.
- 01:55: **Why their grabs land** (scratch grabwhy.py on full dumps of 31 of u4e's field matches, one dump at a time and each deleted after use; the engine allows a grab when the target cannot sense the grabber or has less health). In losses they grabbed ours 3687 times: 46% from outside the victim's cone (side 1010, behind 692) and 48% on a weaker victim. We grabbed theirs 3643 times: 58% from outside the cone, 41% weaker. Even. Facing is not the gap.
- 01:55: **Bank against babies** (rounds.tsv, u4e's 96 field games). In losses we field more babies early (21.0 to 16.1 a round in r50-149) and bank less (1007 to 1389); by r500-799 they have more of both (20.3 babies to 17.0, bank 1460 to 536). This is u3h's case (RICH_BANK 1500, rejected locally), so not a fresh lever. Our low bank also throttles u4m's traps, whose floor is 500 banked.
- 01:55: **Do our trap victims see the layer?** (scratch trapsee.py, same 30 matches; ratTrap rows give the layer, trigger rows the victim.) In losses 1794 enemy rat traps fired on our babies. Of the 1632 we could measure, the victim saw the layer at the lay in 1091 (67%). It stood Chebyshev 2 from the layer in 853 (52%) and 3 in 320. 992 (61%) fired within a round of the lay. Of the 541 blind victims, another of our rats saw the layer in 348. So mostly our rat sees the enemy two tiles off, the enemy lays next to itself toward us, and our next step in sets it off. The engine leaks nothing: a build is refused only on a tile holding our own team's trap. u1n (no step within Chebyshev 2 of a visible enemy, every rat) did not cut fires and gave up the fight.
- 01:55: **u4x pre-registered (structural: trap doctrine) = u4m + FACE_HOLD**, u3i's pairing rebased on u4e. It brings u3d's reactive baby rat traps (u4m: block 1 t -0.30, kills 24.6 to 29.5 a game) and FACE_HOLD (once enemy traps are known, faceEnemy holds at Chebyshev 2 facing a watching, not weaker enemy instead of stepping into contact; on u2g, u3i cut deaths by enemy 14% and raised kills 16%). Its Cfg differs from u4m only in FACE_HOLD (BURN follows it). Falsifier: in trap check xt1 (u4x and u4m vs u3d on dirtpassageway, Stash, jail, arrows, DefaultMedium, starvation, learn_your_lesson, cheesefarm, both seats, seed 0), enemy trap fires on u4x are not at least 10% below u4m's, or its kills fall below u4m's. VM: screen xs1 (kvs cells, jobs 2) and xt1 (jobs 1) are running. Structural attempts are overdue, so u4x goes to the replica next if both pass.
- 01:55: u4v block 1: 12 of 16 matches played (6555-6568); the last four (6582-6585) are queued.
- 02:01: **u4v (KING_STRICT) rejected at block 1** (v4 set 1: 6555-6562, 6565-6568, 6582-6585) vs u4e's set-1 games: 48 cells, 36 alike, D 12 (+3: Finals tiny A, spsquared bed B, spsquared fullofsecrets A, its inspiring map; -9: Finals DefaultSmall B, Icarus rose A/B, Icarus uneasy_alliance A, YokoOno clouds B, Merlin peaceinourtime B, spsquared superioristhmus A, collegejunior rose A/B), net -6, t -1.73. u4e won all four rose games. As A, u4v's king **starved at r797 in peace against both opponents with identical numbers** (430 delivered, 31 spawned, starve damage 600, no contact). The strict walk strands the king out of its carriers' reach there: a KING_STRICT bug, not chance. A local repro (u4v vs s7d, rose, A) follows. The VM got copies of 6567 and 6582 in ~/diag26b/rp for this.
- 02:01: **u4r (SEALED 30, basics repair) submitted** (#152): block 1 on v4 set 1, 16 scrims queued (waiter build/wait_u4rb1.sh, matches above 6585). u4x (structural) goes next if its screen and trap check pass. After it: u4q, u4s, the rebases (u4h, u4a, u4n, u4c, u4f), then u4w. progress/ refreshed.
- 02:07: **u4v's rose starvation reproduced** (local, u4v vs s7d, rose, seed 0, A: lost r827, 490 delivered; dump build/rs1). Its king walked to (14,3) by r80 and stayed there to the end in mode 'z' (the mine walk's no-dig fallback), knowing one mine, (2,7), all game. The other three home mines, (24,8), (24,16) and (24,24), sit behind dirt and were never seen; that is the old rose finding. u4e (build/rs2, same seed: won r1837) parks in the same spot with the same single mine, but rocks between (14,2) and (14,3). Its rats picked 10580 and delivered 3690; u4v's picked 7550 and delivered 490. So the parked strict king left its carriers loaded but unable to hand over, and the back-and-forth is what kept delivery working there. The king's parking tile needs a reachable hand-over tile. That is a possible later repair; KING_STRICT itself is dropped (even without rose its block was +3 -5). Both dumps deleted (local, regenerable, 8.6 MB each).
- 02:31: **u4x's screen xs1 passes** (kvs cells, control kvs = u3v): 44 cells, 36 wins to 30, D 18 (+12 -6), t +1.41, xfer -11%, 0 overruns. Per game it laid 22.2 rat traps (20.5 fired), kills rose from 31.6 to 37.5 and deaths by enemy fell from 34.4 to 31.7. u4b, the same traps on u3v, had 34 wins, D 16, net +4.
- 02:31: **u4x falsified at its trap check** (xt1: u4x and u4m vs u3d, 8 maps, both seats, seed 0). Enemy trap fires on us were 29.9 a game to u4m's 32.5, only -8% against the pre-registered bar of -10%. Our trap deaths went from 11.5 to 11.0, kills from 29.8 to 31.8, deaths by enemy from 37.2 to 33.2, delivered from 4827 to 5016; wins 9 to 8; 0 overruns. Per the pre-registration it does not go to the replica. The hold works on one step (faceEnemy), and most trap victims evidently step in some other way. Two dumped games (xw1 on the VM: u4x on cheesefarm and u4m on arrows, as A vs u3d) are running to read each victim's move code on the firing turn (scratch trapwhy.py).
- 02:31: VM deletions (to free space: 58 MB free): ~/diag26b/tmpdc emptied (the two rose loss dumps 6567/6582 and 6134's, analyses done) and `dcache.py DEL 6` removed 33 unreferenced dumpcache entries older than 6 hours (about 470 MB, all regenerable from their replays). VM free: 557 MB.
- 02:42: **How our trap victims step in** (xw1: u4m on arrows and u4x on cheesefarm, as A vs u3d, seed 0, full dumps; scratch trapwhy.py reads the victim's indicator on the firing turn).
  - arrows (u4m): 185 fires on us. Move codes: 108 no indicator that turn (heavy turns skip it), 53 'a' (attack moves by ATTACK-role rats), 10 'f' (faceEnemy), 9 'T', 4 'k', 1 'w'. 87 of the traps were more than 5 rounds old.
  - cheesefarm (u4x): 116 fires. Codes: 64 none, 17 'w', 8 'd', 6 'M', 5 'f', 5 'W', 4 'C', 3 'T'; roles G 30, F 17, A 5. 51 traps were more than 5 rounds old, 42 a round or less.
  - So in local games against u3d the faceEnemy step is a small share. Attackers, gatherers and war moves walk into standing traps, which is why FACE_HOLD moved fires only 8%.
- 02:42: **The lay signal, re-measured** (scratch laycheese.py over the VM's 288 cached field dumps). An enemy pays a trap's 20 cheese from its carried cheese first, and that is visible on RobotInfo. 24% of enemy lays showed the 20 drop (4919 of 20213) and 70% came from rats carrying none. All of Xzibit's and YokoOno's lays showed it, and most of collegejunior's. This repeats 2026-10-31 21:59 (40% then, on u3z's games), where the victim saw the layer both rounds for only 14% of hits, so the signal would flag about 4% of the traps that hit us. Not built.
- 02:42: VM deletion: runs/xw1 (46 MB, the two dumped games above; regenerable). VM free: 545 MB.
- 02:48: **u4r (SEALED 30) rejected at block 1** (v4 set 1: 6598-6601, 6612-6615, 6637-6640, 6654-6657) vs u4e's set-1 games: 48 cells, 35 alike, D 13 (+5: Finals tiny A, Merlin peaceinourtime A, spsquared bed B and fullofsecrets A, collegejunior clockwisdom B; -8: Finals tiny B, Icarus TheHeist B, rose A/B and uneasy_alliance A, Xzibit attack_on_cat B, spsquared bed A, collegejunior rose A), net -3, t -0.83. Its inspiring map learn_your_lesson was lost in both seats to Xzibit, as by u4e. rose again went against the candidate: u4e won all four rose games, u4v and u4r three of four each lost (u4r: Icarus r953 king destroyed and r2000 on points, collegejunior r983). progress/ refreshed.
- 02:48: **u4q (ATK_DELIVER 50) submitted** (#153): block 1 on v4 set 1, 16 scrims queued (waiter build/wait_u4qb1.sh, matches above 6657). Queue after it: u4s, the rebases (u4h, u4a, u4n, u4c, u4f), then u4w.
- 02:48: Local deletion, to free space (93 MB free on the shared root disk): the replay.bc26 files of replica matches 1000-2999 (555 files, about 695 MB; their match.json files stay, so `contest.py paired` and the progress tools are unaffected, and any of them can be fetched again from the replica). Local free: 758 MB.
- 02:48: **Kings in u4e's field games** (scratch kingcount.py, king2.py, kingbite.py on the light dumps of its 96 games).
  - From r800 we average 1.88 kings in wins and 1.19 in losses; they hold 1.22 and 1.50.
  - Second kings formed before r600 came in 17 games, of which 12 were lost (cheesefarm vs Merlin r95 and r143, DefaultMedium vs Varun r112, dirtpassageway vs Finals r233 and r259 and vs collegejunior r132 and r187). The merge takes 5-12 babies at the time the army war is decided (Varun DefaultMedium 20 to 11, Finals dirtpassageway 20 to 8). Formed r600-999 (at the 40 rich cap): 7 of 7 won. With no second king: 31 wins, 33 losses.
  - The record on timing is mixed: s5y (FORM_FROM 600) passed its screen (net +9) but went to the replica bundled as s6j (with MINE_TIES) and was rejected at t -0.30; s4q and s5o (earlier forming) were rejected. Formation's own gates are 16 babies team-wide (the rat cost) against a cap of 18 (40 with 1000 banked), so it fires at the ordinary cap.
  - In losses our king bites 27 enemy rats a game, 23 of them in the last 300 rounds (the siege), while theirs bite 1.5. They spawn 80.2 a game to our 69.0 in losses; 51.2 to 55.7 in wins.
- 02:59: **Who takes whose cheese** (scratch minedef.py and minedef2.py, full dumps of 31 of u4e's field matches made one at a time and deleted after use). A mine and its twin spawn together (GameWorld.spawnCheese, CheeseMine.generationProbability), so both halves yield the same. Per loss game (48): on our half we picked 3921 and they 2373; on theirs they picked 6228 and we 550. Per win (43): our half 4041 to their 246; their half 3654 to our 1189. So the economy gap is the share of the other half's output each side takes.
  - Of their 2373 a loss game on our half: 238 at mines up to 10 steps from our first king, 1011 at 11-20, 835 at 21-30, 289 beyond. By round: 149 before r150, 410 to r400, 652 to r800, 1162 after (much of it the end of a lost game). 1527 came at mines we had already picked at, and 82% of all of it with none of our babies within Chebyshev 4.
- 02:59: **Home-mine coverage, r150-799** (scratch minecover.py and coversum.py, same matches). Cover is the share of rounds with a baby of ours within Chebyshev 4 of the mine.
  - Losses (176 home mines): up to 10 steps, cover 0.81 with 6.1 babies on average (the crowd round the king); 11-20 steps, cover 0.52 (2.3 babies), their take 261 a mine; 21+ steps, cover 0.12 (0.2 babies), their take 368 a mine, 425 at the 42 of 53 with cover under 0.2.
  - Wins: the 21+ mines have the same low cover (0.09) but they take only 128 a mine.
  - This is u4f's case (MINE_MIRROR: its row already holds u3z's theft numbers, 72-83% uncontested), so no new switch.
- 02:59: **Rebased on u4e** (the u4e recipe + one switch; only newer off switches differ, checked by diffing Cfg): u4y = u4h (FLOW_HELD 20), u4z = u4a (CHIRAL_SYM), u5a = u4c (HUNGRY 300), u5b = u4f (MINE_MIRROR). Their u3v screens carry over, as u4n's did. The originals are marked superseded. Replica order after u4q, by screen evidence: u5a (hu1 t +2.53), u4y (qh1 +1.73, basics repair), u4n, u4z (+1.00), u4s, u5b (D 0), u4w (an overrun fix; u4e had 0 overruns in its 96 field games, so it is not urgent).
- 03:29: **What our rats do, and who stands at our contested mines** (u4e's 93 tagged field games; scratch tags_all.sh and theftzone.py).
  - Tag shares of our robot-rounds, losses: gatherers (G) 62.7%, fighters (F) 30.3%, kings 5.2%. Top labels: G:a (attack) 21.5%, G:M (walk to the mine) 13.8%, F:M 11.0%, G:W (wander at the mine) 9.2%, F:W 7.6%, G:d 5.6%, G:C 5.1%, F:C 3.1%, F:w (war) 1.9%. Wins: G:a 15.5%, G:M 17.4%, F:M 12.7%, F:w 0.7%. Fighters spend their time gathering, and war moves are rare.
  - Contested home mines (enemy steps minus ours at most 8), r150-799. Losses: an enemy within Chebyshev 4 in 11304 mine-rounds over 48 games, with none of ours there in 66% of them; their picks there 32147 cheese, 78% with none of ours within 4. Our rats there: G:a 15%, G:d 9%, G:C 9%, G:M 8%, F:w 8%, F:f 7%. Wins: 3708 mine-rounds, none of ours 46%, their picks 4040. Deeper home mines (margin 9+), losses: enemy present 10209 mine-rounds, none of ours 29%.
- 03:29: **u5c pre-registered** (structural; one switch, FIGHT_POST 8 on the u4e recipe): at war a FIGHT rat takes its mine from the contested home-half mines (none known: every home mine), re-picked once when cooperation ends. BotTest 1845/1845, with five new checks, each failed once under a mutation (the margin test reversed, coopAtPick never set, the coop test dropped, the empty-set fallback dropped; the mine-balance mode also passes). Falsifier, inspiring cells and kill criterion are in its row. Running on the VM: screen fp1 (u5c on the 48 kvs cells) and the control fp2 (u4e on its 32 araid cells), both keeping replays for the theft count (scratch postdiag.py).
- 03:34: **How our kings die, by game length** (scratch earlyking.py over the light dumps: u4e's and u4m's 144 field games). 68 of 75 losses are king kills. Damage to our king in losses: early (under r350, 18 games) bites 4296 and our own rats thrown into it 3516; mid (r350-999, 38) bites 10253, own rats 2198; late (12) bites 2192, own rats 1268. In our 22 early wins their own rats thrown into their king did 5994 and our bites 1756, so the early war is a throwing war both ways.
  - **thunderdome vs nzjt is lost 28 of 28** (every build since 5012; as B 11 of 14 at exactly r120). The kings start Chebyshev 8 apart, and in all four dumped games our king took 588-600 of its damage from our own rats, thrown into it. In 6080 g2 (full dump, scratch throwgeo.py, deleted after use) the 15 such hits came from rats grabbed 2-6 tiles from our king, carried 2-7 rounds and thrown from 2-4 tiles. They grabbed ours 114 times, 52 of them within 3 of our king (38 of those on a weaker victim). The victims were doing everything (G:-, F:f, G:f, G:w ...). knifefight vs nzjt (6133, kings 5 apart) is the same. This is the AMMO_AWAY / BITE_CARRIER / RESCUE family (s8n, s8y, s9v, u1v, all rejected or neutral), so it is not a fresh lever. It is a lost cell, not a band target.
- 03:36: **u4q (ATK_DELIVER 50) rejected at block 1** (replica #153, v4 set 1: 6672-6675, 6693-6696, 6719-6722, 6743-6746) vs u4e's set-1 games: 48 cells, 43 alike, D 5 (+2: Xzibit learn_your_lesson A, spsquared bed B; -3: Icarus uneasy_alliance A, Merlin peaceinourtime B, spsquared superioristhmus A), net -1, t -0.45. thunderdome lost both seats again (30 of 30 now).
- 03:36: **u5a (HUNGRY 300) submitted** (#154), block 1 on v4 set 1, 16 scrims queued (waiter build/wait_u5ab1.sh, matches above 6746). It is a cousin of u4q (attackers deliver), but its trigger is the bank, not the load. u5c follows if its screen passes (structural, overdue). progress/ refresh running.
- 03:39: **averystrangespace re-checked** (6131 g1 full dump, scratch catnear.py, deleted after use): our nearest rat stays 7+ from both cats all game; 12 attackers stand in 'a' every round (1200 attack turns a hundred rounds); fighters gather (F:M, F:W) and never take a cat branch; their first cat bite r127, ours none. Points game: kings 2 to 1, xfer 4480 to 4415, cat 0 to 420, lost. This is u4s's pre-registered case (its row has it: STALL_CAT), which I had re-derived; the row should have been read first. Plan for u4s's turn: u3q idled through set 1 (D 3), so per §3 ("narrow switches need targeted cells") u4s gets a targeted design, not a plain set-1 block: a panel file putting averystrangespace and the stall maps (clockwisdom, rose, where_are_you) against the panel's opponents, the u4e control played first, judged per map and in the mean, with set 2's four averystrangespace cells (its inspiring cells) excluded.
- 03:43: **u5c acts as built** (local u5c vs araid, cheesefarm, seed 0, A, with dumps; won r869, xfer 9147 to 5758). From r30 to r800, 6525 of the fighters' 6929 mine-target turns ('mt=' in the indicator) name the three contested home mines, (14,9) margin 2, (20,2) margin 3 and (26,12) margin 0. Gatherers still spread over all five. Local deletion: build/fpc (7.8 MB, that game's dump). Reach: by u4e's set 1-2 field maps, 25 of 32 have at least one home mine at margin 8 or less (none on DefaultSmall, EscapeTheNight, clouds, corridorofdoomanddespair, dirtpassageway, ratsareants, starvation), so the switch is broad, not map-narrow.
- 04:06: **u5c (FIGHT_POST 8) killed by its falsifier.** Screen fp1 passes (44 cells, 31 wins to kvs's 30, D 11 (+6 -5), t +0.30, xfer -5%, 1 overrun as u4e). But against the control fp2 (u4e on the same 32 araid cells, 28 games dumped each): araid's picks at our margin<=8 home mines in r150-799 were 5659 vs 5835 (-3%; the bar was -30%), and our transferred cheese 180.7k vs 205.4k (-12%; the bar was -5%). Head to head on the araid cells: 18 wins to 20 (D 8, +3 -5). The fighters did stand there (rounds with araid at those mines and none of ours: 19% vs 23%), and the picking went on. So being present at the contested mines does not stop the picking, and fighters there gather less than at the near mines. The theft gap is about the army balance in the middle, not about where we stand. VM deletion: runs/fp1/replays and runs/fp2/replays (79 MB; results kept).
- 04:20: **How our kings die, re-cut (u4e's 96 field games + u4m's 48, light dumps; scripts kingcause/ledger/kingend).** The long "siege" in 6060 g1 (dirtpassageway, collegejunior) was not bites: our king lost 526 of ~730 HP to our own rats thrown into it, and it spent most siege rounds at the baby cap (K:c), so spawns did not pre-empt its bites. 35 of 75 losses end with the bank empty: kings eat 2 a round and lose 10 HP a round under 2 banked (InternalRobot.processEndOfTurn), so these are starvation deaths. 33 are combat kills and 7 go to r2000. In the starved losses we pick 654 a 100 rounds to their 1055 and deliver 271 to their 716 (41% of picks vs 68%); u5a (HUNGRY 300) is the live test of that gap. Per opponent, u4e is 33-75% against all 8 panel bots, and nearly every map is 2-0 or 0-2 (the same result from both seats).
- 04:20: **Clouds (6100 g1 vs Varun, starved r489): a never-seen mine.** 50x50, VER; our half's mines (3,21), (2,37) and axis (8,47). We worked only (3,21) (850 picked); Varun worked 5 mines, including our (2,37) (800) and (8,47) (400). No rat of ours came within 4 of (2,37) all game: it sits beyond a diagonal dirt band (y 28-35, x 0-13), and our scouts went north through x 17-30 toward the centre. Fifteen rats sat on mine duty (M/W) at the one mine. Over all 96 u4e games we work 74% of home mines in losses and 67% in wins, so coverage is not the general gap; exploration is already well mined (s6m accepted; s7v/s7w, s9c-s9i, u0w/u1a, u2t/u2u/u2x, t0x not adopted).
- 04:20: **Theft, measured as loaded carriers.** By r600 they pick 930 a lost game at our home mines (21% of their picks; wins 300, 12%), and in losses they work 77 of our 182 home mines (wins 22 of 148). Full dumps: Stash 6040 g0 had 1537 rat-rounds of an enemy on our half carrying 20+ cheese; one of ours was within dist2 20 in 661, and 96 loaded trips got home with 3070. cheesefarm 6133 g0: adjacent (dist2 <= 2) in 308 rat-rounds, yet 55 trips left with 1555 and 5 died. The rats nearest a loaded thief are mostly G:a, G:f/F:f (standoff facing) and F:w. A rat that dies drops its cheese on its tile (GameWorld.destroyRobot), so a killed thief on our half gives the cheese back. No attempt has ever targeted loaded carriers (u5c moved fighters to the contested mines, but its fighters did not engage).
- 04:20: **u5a (HUNGRY 300) passes replica block 1** (6755-6758, 6767-6770, 6786-6789, 6810-6813 vs u4e's set-1 refs): 48 cells, 42 alike, D 6 (+4 -2), net +2, t +0.82. Gained Finals tiny A, Xzibit learn_your_lesson A, spsquared bed B and superioristhmus B; lost Finals tiny B, nzjt peaceinourtime B. Block 2 on v4 set 2 queued (16 scrims, matches above 6813; refs 6088-6091, 6098-6101, 6114-6117, 6131-6134).
- 04:40: **The economy census behind u5d** (u4e's 31 dumped set-1/2 matches, 92 games; scratch kdeath/cflow/harvest/saturate/perbaby/xsize/trips/bphase). King deaths in our 68 pre-r2000 losses: starvation 39, bites 27, our own rats thrown 9, the cat 8 (summary kingDeaths). In wins their kings starve too (27 of 56). A mine and its twin drop a 20-pile about every 6-7 rounds each (CheeseMine: p = 1 - 0.99^k), about 3 cheese a round per mine; even worked mines keep half their cheese (we pick 30% in losses and 46% in wins; they pick 44-45%). The binding stage is gatherer time.
  - Picks per 100 baby-rounds in losses: 26.6 / 31.5 / 23.2 (r<200, 200-599, 600+) to their 44.2 / 54.2 / 45.2.
  - Cheese ledger in losses: we pick 3733 a game and deliver 55%; 14% dies with the carrier, 13% is held at the end. They pick 6548 and deliver 69%, with 4% lost on deaths.
  - Transfers: ours average 27.3 in losses (77% at 10-29), theirs 45.8. Every in-band opponent averages 43-54.
  - Haul cycle (first pickup, transfer, next pickup): ours in losses is pick 2.4 rounds, home 12.0 (6.4 tiles), back 66.0, so 27.3 cheese per cycle, 0.34 a cycle-round. Theirs is 14.4 / 27.2 / 44.2 with 45.7 per cycle (0.53). In wins we load 37.8 and make 0.49 to their 0.41.
  - DELIVER_IDLE 10 (home with 10+ when no pile is in sight) sets most of our trips. On arrows (6040 g1) the back phase covers 49% of all our baby turns: G:W 46% (wandering at the mine), G:M 44% (walking to it).
  - Rat deaths per 1000 rounds in losses: traps 14.2 ours to 2.6 theirs, which is still the whole kill gap (LEARNINGS 15); throws even.
  - Dropped ideas: loot-hunting thieves (a loaded trip averages ~30 cheese and needs ~10 bites, little upside); new exploration (well mined, see 04:20).
- 04:40: **u5d pre-registered (structural: economy doctrine, the field's haul cycle) = u4e + LOAD_AT 40.** LOAD_AT is a new switch, off in ratbot. With it a gatherer heads home only once it carries 40 raw cheese (or after r1960), instead of at DELIVER_AT 30 or at DELIVER_IDLE 10 with no pile in sight. With a part load it stays at its mine. DELIVER_AT keeps its combat uses (war, pressKing). Test testFullLoad (5 checks; four mutations each failed); BotTest 1850/1850; unit tests exit=0.
  - Local budget, screen ld1 (kvs cells; control pf2 = u4e: mean transfer 29.7, picks 38.74 per 100 rat-turns, delivered 285254, 6 starved kings, 33 wins): (1) mean transfer >= 38; (2) picks >= 40.7 per 100 rat-turns; (3) delivered >= 271k; (4) starved kings <= 9.
  - Kill: (1) or (3) fails, or screen t <= -1.5.
  - Field budget: mean transfer >= 40 and picks per 100 baby-rounds +15% vs u4e on the same cells.
  - Inspired by aggregate set-1/2 statistics, not by single cells; set 3 is the clean test.
  - Screen ld1 running on the VM (jobs 2). u5d goes to the replica after u5a's verdict and ahead of the queued basics repairs, since a structural attempt is overdue.
- 04:50: where enemy rat traps hit us (trapvic.py over u4e's 31 field matches, every trigger row with an enemy-owned trap and one of our rats as the victim).
  - Losses: 47.2 fires a game (38 games). Wins: 24.4 (31 games).
  - Place. In losses, 56% of fires are on our half, 19% at the midline and 25% on theirs. 89% are more than Chebyshev 10 from the enemy start. In wins, 28% are within 5 of the enemy start (rushers dying at their base) and only 33% are on our half.
  - Age. 55% of fired traps were laid that round or the round before, and 11% were 11-50 rounds old. These are reactive traps laid in contact, not minefields.
  - Activity. About half the victims were fighting: G:a 12%, G:w 7%, G:f 7%, F:f 7%, F:w 6%, G:T 6%, G:k 4%. Gathering labels (M, W, C, d) cover about 11%, and 32% carry no decodable tag. 24% of victims carried cheese.
  - Reading. In losses the field's fighters bring reactive traps to our half and spend them on our fighters in contact. This is the same mechanism as u0j/u3d's TRAP_ADJ. The counter-measures already tried (RISK_FRESH, WOUNDED_HP, FACE_HOLD, TRAP_RISK, RUSH_BURN, MOAT, our own reactive traps u3d/u4b/u4m/u4x) were all neutral or rejected. That makes three or more straight rejects in the trap area, so the next attempt goes elsewhere (the economy: u5a HUNGRY, u5d LOAD_AT).
- 04:55: unpicked cheese (piles.py, piles2.py, piles3.py, idlemine.py, stalemap.py over u4e's light dumps; a pile is tracked from its cheesespawn row to its pickup, and dirt is updated by dig and placeDirt rows).
  - Cheese lies stale on both halves all game. On our half at r700: losses 1424 a game (1346 of it older than 50 rounds), wins 1025. The enemy half holds the same amount. Losses differ only at the midline: 125 lies there against 1402 in wins, because in losses they take the axis mines.
  - Reach (flood fill from our start). Most stale cheese on our half is reachable over open tiles: losses 813 a game (19 piles) at r700, wins 943. Behind current dirt: 372 / 34. Sealed: 161 / 0.
  - Where. At r700 the reachable stale cheese is mostly at home mines none of ours picked at in the last 100 rounds: losses 585 a game against 227 at mines we worked, wins 776 against 166. In wins we leave more home mines idle (r400: 1.5 of 4.0 idle in wins, 0.8 of 4.1 in losses).
  - Example: clockwisdom 6060 g0 has 10 home mines; 6 are idle at r700 with 1200-1740 each, and our 12 picks in 700 rounds at (35,17) leave 31 stale piles in its square. learn_your_lesson (17,36) sits in a walled room, so its 660-cheese mine-tile pile is out of reach.
  - Reading. Supply is not what binds us: unpicked cheese is plentiful in wins and losses alike. The gap is our pick rate per baby, 26 against their 45 per 100 baby-rounds in losses. Idle home mines don't separate wins from losses (we leave more of them idle in wins). Coverage tries (u0w SEEK_IDLE, u0x IDLE_SHIFT, rejected inside u1a at t -2.24) also argue against another coverage attempt. The trip cycle (u5d LOAD_AT) stays the lever under test.
- 05:00: the per-rat pick rate is a war tax, not a crowding artefact (u4e light summaries). In losses we keep 19.6 rats alive on average and they keep 21.8, so they are not picking more simply by having fewer rats to share each mine. Our rate per 100 rat-turns is 28.6 against their 49.1. In wins ours is 39.3 against their 43.7. We spawn 69 rats a game in losses against 56 in wins. So our rate collapses in exactly the games where our half is contested (deaths, fights, theft), and a gatherer-routine change can only win back part of that gap.
- 05:01: u5d (LOAD_AT 40) killed at the local screen by its pre-registered falsifier.
  - ld1: 48 kvs cells, control pf2 = u4e.
  - Budget results:
    - (1) mean transfer 40.0 (u4e 29.7; bar >= 38): pass.
    - (2) picks 38.20 per 100 rat-turns (u4e 38.74; bar >= 40.7): fail.
    - (3) delivered 264,956 (u4e 285,254; bar >= 271k): fail, so kill.
    - (4) starved kings 2 (u4e 6; bar <= 9): pass.
  - Screen: 33 wins to 30 on 44 matched cells, D 9 (+6 -3), t +1.0.
  - Why delivered fell: per 100 rounds it is flat (647.5 vs 645.8). The total dropped because u5d won some games sooner (jail B r467, starvation A r781, cheesefarm B r728, all r2000 for u4e). So (3) was confounded by game length. The kill stands anyway: the bar does not move after the data, and stage (2) shows the mechanism itself did not appear (full loads, but no more cheese picked per rat-turn).
  - Method lesson: from now on, write economy budgets as rates (per 100 rounds or per rat-turn), not game totals. A candidate that wins sooner delivers less in total.
  - Its +3 is noted as a near-miss for possible combination; the mechanism behind it is unknown (fewer starved kings, 2 vs 6?).
  - The replica queue after u5a is now u4y (FLOW_HELD 20, basics repair), then u4n, u4z, u4s, u5b, u4w.
- 05:11: **u5a (HUNGRY 300) block 2** (#154, v4 set 2: 6835-6838, 6851-6854, 6866-6869, 6879-6882) against u4e's set-2 reference.
  - Block 2: 48 cells, 46 alike, D 2 (+2 -0), t +1.41. Gains: AdamTan12_Finals dirtpassageway B and YokoOno Hike B.
  - Pooled with block 1: 94 cells, 86 alike, D 8 (+6 -2), net +4, t +1.41. That is between the block-2 bars (reject at t <= 1, accept at t >= 2), so u5a goes to block 3 on v4 set 3.
  - u4e has no set-3 games, so I'm resubmitting it as the reference and queuing its 16 set-3 scrims. u5a's block 3 follows. The set-3 reference will also serve later candidates' block 3s.
- 05:20: **Cheese by carrier class** (carryrole.py over u4e's 32 replica matches, 96 games, full dumps). A rat's class comes from its decoded tags: FIGHT if the fight bit is set on half its turns or more, ATK if attack moves ('a') make up 30%+ of its other turns, GATHER otherwise.
  - Wins (46): we pick 8307 a game. Attackers pick 30% of it and deliver 2%; 68% dies with them (cats 28%) and 28% is still held at the end. Gatherers deliver 86%, fighters 91%.
  - Starved losses (23): we pick 4127. Attackers pick 40% and deliver 2% (died 54%, cats 29%, end 43%). Gatherers deliver 68% (died holding 25%, cats 16%). Fighters deliver 64% (died 33%, cats 25%).
  - Other losses (27): gatherers deliver 90%, fighters 83%, and attackers pick only 15%.
  - Reading. "We deliver 55% of what we pick" is mostly the attackers: a third of our pickups are attackers stripping piles they never bring home. The economy gap specific to starved losses is cats killing loaded fighters and gatherers. HUNGRY (u5a, now on the replica) makes attackers deliver when the bank is low, the starved-loss case. Cat avoidance has never been tried. The gatherer flee (GATHER_CAT_DIST) covers only role GATHER, and CAT_DEFEND's approach (a cat at our king draws every rat that sees it) runs first, whatever the load.
  - Tool bug found and fixed. cflow.py booked deaths by the die row's x[1]. That is the killer's id: a cat's, or the rat itself on 'terminated'. The victim is x[5]. So cflow's "died" undercounted kills by cats and enemies, and its "rest" (14% in losses) was mostly those deaths. carryrole.py uses x[5]. Earlier cflow figures in this log carry the bias; the delivered and picked shares are unaffected.
- 05:34: whether loaded cat victims saw the cat (catvis + catjoin, u4e's 96 field games, victims holding ≥ 50 cheese, their last 3 turns).
  - Starved losses: 73 victims holding 19,586. 34 of them (12,043 cheese, 61%) never had the cat in their 90° vision cone. 46 did not see it on the first of the 3 turns. Many sat at dist2 1-2 from the cat (adjacent, behind us) for all 3 turns.
  - By role in starved losses: gatherers 25 victims / 4,922 cheese, only 1,470 seen; fighters 26 / 4,033, 1,495 seen; attackers 22 / 10,631, 4,578 seen. Gatherers already flee a cat they see (GATHER_CAT_DIST), so most of their losses are to cats they never saw.
  - Wins: 91 victims / 36,860, 56 seen. Most of it is attackers' hoards (31,140), which they rarely deliver.
  - Concentration: one map dominates the unseen losses (clockwisdom, collegejunior: 6061 g0 4,680, 6060 g0 4,000, 6261 g0 900), then fullofsecrets (6021 g1, 6133 g1), Hike (6099 g0) and learn_your_lesson (6059 g1, 6116 g2).
  - So a flee rule for seen cats would reach only about a third of the deliverable cheese lost (gatherers + fighters: ~270 per starved loss unseen, ~135 seen). The missing piece is sensing. Cat avoidance was never tried as a candidate (attempts.csv), so the area is open.
- 05:35: pre-registration, u5e = u4e + CAT_BLIND (one change, switch off by default).
  - Mechanism: a baby whose health fell since the end of its last turn, with no cat and no enemy robot in view, turns to face back before it senses. A hidden cat then drives this turn's decision: a gatherer flees it, a fighter takes it on as before. It costs the turn action, so the flee step is a strafe (cooldown 18, not 10).
  - Inspiring cells (field, u4e): collegejunior clockwisdom (6059-6061, 6261), spsquared fullofsecrets (6021), Merlin fullofsecrets (6133), YokoOno Hike (6099), learn_your_lesson (6059, 6116).
  - Budgets are rates on the local screen (run bl1, the 48 kvs cells, control pf2 = u4e):
    - (1) mechanism: cat-caused rat deaths (pounce + crush + scratch) per 1000 rat-turns must fall from u4e's 1.538 to ≤ 1.46 (-5%). If not, kill.
    - (2) cost: picks per 100 rat-turns must stay ≥ 37.6 (u4e 38.74, -3%). If not, kill.
    - (3) overruns ≤ 3 (u4e 1).
    - (4) the screen rejects at t ≤ -1.5, as always.
  - Expected effect: small. The best case saves part of ~270 deliverable cheese per starved loss, and dropped cheese can be picked up again. It is cheap: one extra sense on hurt turns only.
- 05:43: **VM disk deletion** (free space had fallen to 182 MB; the replica writes its replays to the same disk). Deleted 176 of 288 dirs in build/dumpcache on the VM (1364 MB; 2.5 GB to 1.2 GB; free now 1.6 GB). These are derived full dumps, rebuildable from their replays with dumpone.py, referenced only by finished analysis lists from the u2g/u3d era and earlier (u2g_*, rhc_u2g, sm1_u2g, s3ref_u2g, ma2_u2g, inband*, ref_dumps, r1r/r1v/y/p/sweep/alex/tdumps, s3_icnz, one4970, po1_une, flowsum_pk1, fq_test, u3d_b1). Kept: dumps referenced by keep_dumps.txt, u3z_*, fq1_*, l6099 and u4e_ovr. Names in ~/diag26b/dcache_deleted.txt (scratch dkeep.py).
- 05:43: two checks before the next idea (u4e's 96 field games, light dumps).
  - King deaths, re-counted (scratch kdeath.py): of 50 losses, 22 starve, 2 bite + starve, 12 bite, 4 throwHit, 1 bite + throwHit, 2 cat scratch, 7 on points. In the bite/throw losses our bank 100 rounds before the end is 40-450 to their 280-7100, and our babies 1-20 to their 8-38: the king kill follows an economic collapse. This repeats the 03:34/04:20/04:40 entries, which I should have read first.
  - Pile age at pickup (scratch pileage.py: rounds from a mine's 20-pile landing to its pickup). At our own mines we are faster than they are at theirs: losses, median 2 rounds (r<400) and 1 (later), to their 7 and 9; wins 3 and 8, to their 8 and 12. Their thefts on our half wait a median 12-13 rounds. So detection at worked mines is not the gap. The gap is how many piles each side reaches (in losses, their home pickups 12,809 to our 8,478). Economy has three straight rejects (u4q, u5c, u5d), so the next attempts go elsewhere (§3).
- 06:03: **u4e's v4 set-3 reference is in** (#155): 6899-6902, 6919-6922, 6940-6943, 6954-6957; u4e won 18 of 48 games (both seats lost vs Icarus and Merlin, 1-5 vs collegejunior and Finals, 2-4 vs YokoOno; 5-1 vs spsquared and Xzibit, 4-2 vs Varun). **u5a resubmitted** (#156) for block 3; 16 set-3 scrims queued (waiter build/wait_u5ab3.sh, matches above 6957). Block 3 rejects at pooled t <= 1.5 and accepts at t >= 2.
- 06:05: **u4e's lost set-3 games, first look** (8 matches fetched: 6901-6902, 6942-6943, 6954-6957; light dumps made on the VM into ~/diag26b/lite, full dumps deleted; scratch s3lite.sh, s3sum.py). These are the block-3 maps, so anything built from them must list them as inspiring cells.
  - keepout, wallsofparadis (Finals): we pick 1.5-4.8k to their 9.6-13.7k; 23-50 of our rats die to traps a game (theirs 0). Kings killed at r690-990 after the bank ran out.
  - FreeTheCheese (Merlin): they spawn 152-174 to our 52-63 and pick 16.7-18.9k to our 5.5k, though cats kill 90-115 of their rats. cheesebottles: bank 4.1-4.4k to our 0.6-1.0k by r1000.
  - whatsthecatdoin (Merlin; 40x40, four mines, two in sealed-off corners): we pick 376 and 680 and deliver 30 and 144; 36-41 of our rats die to throws (throwLand + throwHit); starved by r292-332.
  - mincemeat (Icarus; 31x31 dirt maze, three mines): we pick 1025 and 1630 but deliver 325 and 250 (their 1290 and 1400); cats cause 31-36 of our ~50 deaths; the bank goes from 1075 at r100 to 0 by r300 on spawns that die before they deliver.
  - averystrangespace (Icarus): no war at all (no backstab); transfers 3.9k to 4.0-4.2k and kings 2 to 1, lost on points through the cat share (u4s's case). averyfineline: both sides broke by r100 (bank 105 to 1), we starve by r211-238.
  - So set 3 repeats sets 1-2: an economic collapse, then the king falls. Two map-level signals are new: delivery failure on mincemeat and whatsthecatdoin (we deliver 15-20% of what we pick), and Merlin's 150+ spawns on FreeTheCheese.
- 06:06: **u5e (CAT_BLIND) killed at the screen by falsifier (2).** bl1 (48 kvs cells) vs pf2 (u4e): 44 matched cells, D 17 (+9 -8), net +1, t +0.24; 33 wins of 48, as u4e.
  - (1) The mechanism is there: cat-caused rat deaths fell to 1.366 per 1000 rat-turns (u4e 1.538, -11%; bar 1.46).
  - (2) Failed: picks fell to 35.45 per 100 rat-turns (u4e 38.74, -8.5%; bar 37.6). Delivered 255k to 285k (-10%).
  - (3) 0 overruns.
  - Reading: the turn back fires on every hurt turn with nothing in view, so bites, throws and trap hits pay for it too, and the next step is a strafe (cooldown 18). The damage tells the causes apart (GameConstants, TrapType): a scratch is exactly 20 (CAT_SCRATCH_DAMAGE), a bite 10, a throw 10 + 4 a tile, a rat trap 50, and a pounce kills. So u5f keeps the mechanism and fires only on a loss of 20 or 40. That is a new candidate with its own bars, not a moved bar for u5e. VM: runs/bl1/replays removed (empty).
- 06:08: **pre-registration, u5f = u4e + CAT_BLIND + CAT_BLIND_DMG 20** (u5e's mechanism with a narrower trigger; the step is a new switch, 0 = any loss). The turn back fires only on a health loss of exactly 20 or 40 since the end of our last turn, with no cat or enemy in view. Two bites (10 + 10) also match, and then the biter is behind us, which guardTurn would want to face anyway. BotTest 1855/1855; the two new checks each failed once under a mutation (twice-the-step dropped; any multiple of the step; any loss including none at step 0).
  - Inspiring evidence: as u5e (field u4e 96 games), plus u5e's bl1 screen (cat deaths -11%, picks -8.5%). Seen on set 3 before this: mincemeat (Icarus; cats cause 31-36 of our ~50 deaths), so mincemeat cells are not clean evidence for u5f.
  - Bars on the local screen (48 kvs cells, control pf2 = u4e): (1) cat-caused rat deaths per 1000 rat-turns ≤ 1.46 (u4e 1.538); (2) picks per 100 rat-turns ≥ 37.6 (u4e 38.74); (3) overruns ≤ 3; (4) t ≤ -1.5 rejects. Kill if (1), (2) or (3) fails.
- 06:13: the set-3 "delivery failure" games are army wipe-outs (light dumps; summary + rawCheese in rounds.tsv). On mincemeat (6954/6955 g2), whatsthecatdoin (6956/6957 g2) and averyfineline (6954/6955 g0) every rat we spawned died (46-58 spawned, 45-58 died) by r211-375. Cats killed 31-36 a game on mincemeat; enemy rats killed 36-51 on the other two. Our rats never held more than 40-415 cheese at once, so the picks were lost one carrier at a time, not hoarded. A spawn costs 10 + 10 per 4 live babies (RobotControllerImpl.getCurrentRatCost), so those 50 rats cost ~2000 of the bank. On mincemeat their rats die to cats as fast as ours (crush + pounce 27-41), but they deliver 1290-1400 to our 250-325.
- 06:49: **u5a (HUNGRY 300) rejected at replica block 3.** Block 3 (6972-6975, 6987-6990, 7007-7010, 7023-7026) vs u4e's set-3 reference: 48 cells, 43 alike, D 5 (+1 -4), net -3, t -1.34. Pooled over blocks 1-3: 138 cells, 125 alike, D 13 (+7 -6), net +1, t +0.28, under the block-3 bar of 1.5. The +4 of blocks 1-2 did not hold; u5a won 15 games of 48 on set 3 to u4e's 18. Economy has now lost four in a row (u4q, u5c, u5d, u5a).
- 06:49: **u4y (FLOW_HELD 20, basics repair) submitted** (#157), block 1 on v4 set 1, 16 scrims queued (waiter build/wait_u4yb1.sh, matches above 7026). Remaining queue: u4n, u4z, u4s, u5b, u4w; u5f if its screen passes.
- 07:41: **u4y (FLOW_HELD 20) rejected at replica block 1** (#157; 7041-7044, 7061-7064, 7085-7088, 7103-7106) vs u4e's set-1 refs: 48 cells, 42 alike, D 6 (+1 -5), net -4, t -1.63. **u4n (FACE_REACH) submitted** (#158), block 1 on set 1, 16 scrims queued (waiter build/wait_u4nb1.sh, matches above 7106).
- 07:41: **u5f (CAT_BLIND + CAT_BLIND_DMG 20) killed at the screen by falsifier (1), by a hair.** bl2 vs pf2: cat-caused rat deaths 1.462 per 1000 rat-turns, against a bar of 1.46 (u4e 1.538, u5e 1.366). (2) passed (picks 38.45 per 100 rat-turns, bar 37.6); 1 overrun. The screen leaned positive (44 cells, D 15 (+9 -6), t about +0.8; 37 wins of 48 to u4e's 33), but transfers fell 8% (270.0k to 292.8k). The bar was pre-registered, so it stands. Narrowing the trigger kept the picks but lost most of the cat-death cut. So most of u5e's cut came from turns after non-scratch hits: two-cat or pounce-adjacent situations, or simply rats that turned and so saw cats sooner. The cat-sensing area has two kills now. VM: runs/bl2/replays removed.
- 07:41: **pre-registration, u5g = u4e + GLANCE_MEM 20** (an existing switch, never tried alone on the replica; snapshot only). At war, a rat ending its turn with no enemy known near spends its unused turn facing back, when an enemy rat was seen or heard within 20 rounds. Rats are grabbed from behind by grabbers they cannot see. History: s4g passed the s4d rush run (t +1.52, king kills 14 to 17); it reached the replica only bundled as s4j (with MAX_BABIES_WAR 30), rejected at t -0.33. Unlike u5e it turns at the END of the turn, so the next step turns back for free and pays no strafe cost.
  - Inspiring cells: set-3 whatsthecatdoin (Merlin 6956/6957 g2: throws kill 36-41 of our rats), and the 03:34 throwing-war entry (thunderdome, knifefight, tiny).
  - Bars on the local screen (48 kvs cells, control pf2 = u4e):
    - (1) mechanism: enemy grabs of our rats per 1000 rat-turns ≤ 3.38 (u4e 3.562, -5%);
    - (2) cost: picks per 100 rat-turns ≥ 37.6 (u4e 38.74);
    - (3) overruns ≤ 3;
    - (4) t ≤ -1.5 rejects.
  - Kill if (1), (2) or (3) fails.
- 08:27: **u4n (FACE_REACH) rejected at replica block 1** (#158; 7121-7124, 7135-7138, 7248-7255) vs u4e's set-1 refs: 48 cells, 45 alike, D 3 (+1 Finals tiny A; -2 Icarus uneasy_alliance A/B), net -1, t -0.58. u4y and u4n both lost both uneasy_alliance seats that u4e's reference won, so that cell may be a lucky reference rather than two regressions. **u4z (CHIRAL_SYM, symmetry breaking) submitted** (#159), block 1 on set 1, 16 scrims queued (waiter build/wait_u4zb1.sh, matches above 7255).
- 08:30: **u5g (GLANCE_MEM 20) killed at the screen by falsifier (2), by a hair.** gl1 vs pf2: picks 37.41 per 100 rat-turns (bar 37.6; u4e 38.74). (1) passed: enemy grabs of our rats 3.355 per 1000 rat-turns (bar 3.38; u4e 3.562). (3) 0 overruns. The screen leaned positive: 44 cells, D 15 (+9 -6), net +3, t about +0.8, 35 wins of 48 to 33; delivered 281.0k (u4e 285.3k).
  - **Method lesson: the cost bars were tighter than the noise.** Over four screens of near-neutral switches the picks rate ranged 37.4-38.7 (u5d 38.20, u5f 38.45, u5g 37.41) against u4e's 38.74. Any change re-rolls every game's chaos, so a -3% bar is about one run-to-run spread, and two candidates in a row (u5f, u5g) died on it with positive screens. From now on, set a cost bar outside the spread of past screens: picks ≥ 36.5 per 100 rat-turns (-6%), or name a mechanism-specific cost. Both kills stand; neither bar is moved after the fact.
  - Next: two near-misses with independent mechanisms (u5f: cat-scratch turn-back, cat deaths -5%; u5g: end-of-turn glance, grabs -6%) may be combined (§3). The combination is pre-registered fresh as u5h.
- 08:31: **pre-registration, u5h = u4e + CAT_BLIND + CAT_BLIND_DMG 20 + GLANCE_MEM 20** (u5f + u5g, two near-misses with independent triggers: a hidden scratch at the start of the turn, an unseen grabber at the end of it). Snapshot only.
  - Inspiring evidence: as u5f and u5g (field u4e 96 games; set-3 mincemeat and whatsthecatdoin seen before this).
  - Bars on the local screen (48 kvs cells, control pf2 = u4e), set outside the screen's spread:
    - (1) mechanisms: not both of cat-caused deaths ≥ 1.538 per 1000 rat-turns and enemy grabs ≥ 3.562 per 1000 (u4e's values);
    - (2) cost: picks ≥ 36.5 per 100 rat-turns;
    - (3) overruns ≤ 3;
    - (4) t ≤ -1.5 rejects.
  - Kill if (1), (2) or (3) fails. If it passes, it goes to the replica queue by screen evidence.
- 09:14: **u4z (CHIRAL_SYM) rejected at replica block 1** (#159; 7256-7271) vs u4e's set-1 refs: 48 cells, 41 alike, D 7 (+3 -4), net -1, t -0.38. Four queued replica candidates have now failed block 1 in a row (u4y, u4n, u4z) or later (u5a). u4e's set-1 reference looks lucky on Icarus uneasy_alliance B (lost by u4y, u4n and u4z).
- 09:14: **u4s's targeted design, as planned at 03:39** (STALL_CAT acts only where attackers stall, so a plain set block hardly tests it: u3q left 45 of 48 cells alike). Panel research/panel/stall-u4s.json: averystrangespace against all 8 panel opponents, plus two of the stall maps (rose, where_are_you, clockwisdom) each. The u4e control plays it first (#160, 16 scrims, waiter build/wait_u4est.sh, matches above 7271), then u4s.
  - Judging, fixed now: pooled t over the 48 cells with the usual block-1 bars (reject at t ≤ 0). Separately, a mechanism check on the averystrangespace cells: our cat damage there must be > 0 in at least half of u4s's 16 games (u4e: 0 in all of set 2's).
- 09:16: **u5h passes its screen** (gl2 vs pf2): 44 cells, D 19 (+12 -7), net +5, t about +1.15; 39 wins of 48 to u4e's 33. (1) cat deaths 1.522 (u4e 1.538) passes, though enemy grabs rose to 4.079 (3.562); (2) picks 38.74 (bar 36.5); (3) 1 overrun. Delivered 269.5k (-6%). This is the best local screen since u4e, but the grab counter moved the wrong way, so the glance is not doing what u5g's did. Replica order: u4s's targeted test (u4e control running), then u5h, then u5b and u4w. VM: runs/gl2/replays removed.
- 10:09: u4e's stall-panel control is in (#160: 7280-7283, 7305-7308, 7320-7323, 7336-7339; u4e won 16 of 48 games). **u4s submitted** (#161) on the same panel, 16 scrims queued (waiter build/wait_u4sst.sh, matches above 7339).
- 10:56: **u4s (STALL_CAT 100) passes its targeted block** (#161; 7353-7356, 7366-7369, 7388-7391, 7402-7405 vs u4e's control 7280-7283, 7305-7308, 7320-7323, 7336-7339 on stall-u4s.json): 48 cells, 33 alike, D 15 (+12 -3), net +9, t +2.32. Gained averystrangespace in 6 cells (Icarus A/B, Xzibit B, YokoOno B, Varun B, collegejunior B) and where_are_you in 4 (Xzibit A/B, YokoOno A, Varun B); lost Icarus clockwisdom B and rose B, and Varun clockwisdom B. Block 1 never accepts. The mechanism check (our cat damage on averystrangespace) is running on the VM light dumps.
  - **Block 2, fixed now:** v4 set 2 (16 scrims queued, matches above 7405; waiter build/wait_u4sb2.sh) against u4e's set-2 refs 6088-6091, 6098-6101, 6114-6117, 6131-6134. Pooled with the targeted block: reject at t ≤ 1, accept at t ≥ 2. Set 2 carries four averystrangespace cells, all lost by u4e on points.

## 2026-11-01 11:41 PST: u4s accepted (STALL_CAT 100); the new incumbent

- 11:41: **u4s ACCEPTED** (u4e + STALL_CAT 100): pooled over the targeted stall block and v4 set 2, 90 cells, 71 alike, D 19 (+16 -3), net +13, t +2.98 (block 2 accepts at t ≥ 2). Block 2 alone (7420-7423, 7440-7443, 7459-7462, 7488-7491 vs u4e's set-2 refs): D 8 (+7 -1), t +2.12, gaining both averystrangespace cells vs YokoOno and Icarus that u4e lost on points, and both Merlin fullofsecrets seats. Mechanism confirmed: our cat damage on averystrangespace > 0 in 14 of 16 games (40-1380 a game; bar 8), u4e 0 of 16. During cooperation an attacker stalled within Chebyshev 3 of one tile for 100 rounds now hunts cats. The cat share weighs 0.5 in cooperation scoring, and u4e's stalled rushers had been standing idle.
  - The lesson behind it is logged at 03:39 (§3): a narrow switch needs targeted cells. u3q, the same switch, idled through a plain set-1 block (45 of 48 alike) and was rejected. With a panel built around the maps where it acts, it showed its effect in one block.
  - New incumbent recipe: the u4e snapvar recipe + 'STALL_CAT = 0;=>STALL_CAT = 100;'. Queued candidates get rebased: u5h (CAT_BLIND + CAT_BLIND_DMG 20 + GLANCE_MEM 20) becomes u5i, and u5b and u4w follow.
  - u4s's own references: the stall panel and set 2 exist; the set-1 reference is queued now (16 scrims, matches above 7491; waiter build/wait_u4sr1.sh), then u5i's block 1 on set 1.
- 11:46: Queue rebased on u4s: **u5i** = u4s + CAT_BLIND + CAT_BLIND_DMG 20 + GLANCE_MEM 20 (was u5h), **u5j** = u4s + MINE_MIRROR (was u5b), **u5k** = u4s + CAT_REACH 30 + AIM_LEFT 3000 (was u4w). All three compile, and their Cfg diffs against u4s show only the listed switches. The pf2 control is u4e, so a u4s local control (VM run cb2, the 48 kvs cells) is running now. u5i's screen xw2 follows it on the same cells. u5i keeps the pre-registered u5h falsifiers, now measured against cb2: picks ≥ −6% per 100 rat-turns and wins not below the control's minus 3. Replica order: the u4s set-1 reference (queued), then u5i, u5j, u5k.
- 12:26: **u4s set-1 reference done** (7506-7509, 7527-7530, 7543-7546, 7553-7556). Against u4e's set-1 refs (6021-6080): 48 cells, 43 alike, D 5 (+2: Merlin peaceinourtime A, spsquared bed B; −3: Icarus rose B and uneasy_alliance A, Merlin peaceinourtime B), net −1. This is the expected near-wash: STALL_CAT acts only on stalled cooperation maps, and set 1 has few. Icarus uneasy_alliance has now been lost by u4y, u4n, u4z and u4s after u4e's reference won it, so u4e's reference there was probably lucky. These games are the reference for u4s-based candidates on set 1. **u5i submitted** (#162), block 1 on set 1: 16 scrims queued (waiter build/wait_u5ib1.sh, matches above 7556).
- 13:17: **u5i (CAT_BLIND + CAT_BLIND_DMG 20 + GLANCE_MEM 20 on u4s) rejected at replica block 1** (#162; 7580-7583, 7599-7602, 7630-7633, 7649-7652 vs u4s's set-1 refs): 48 cells, 38 alike, D 10 (+4 −6), net −2, t −0.63. The gains and losses are spread over six opponents with no pattern: the scratch-to-look-back reflex did not show up on the field. This is the cat-awareness line's third miss (u5e, u5f, u5g were killed locally; u5h passed locally, and its rebase u5i failed on the field). Cat-victim cheese stays a loss channel, but the next attempt goes to another area.
  - u4s local control **cb2** (the 48 kvs cells, VM): 35 of 48 wins, picks 38.80 per 100 rat-turns, delivered 272,665, 5 starved kings, 1 overrun. On the 44 cells it shares with pf2 (u4e), 32 wins to 30, xfer 274,006 vs 292,783. In local coop games STALL_CAT pulls stalled attackers onto the cats, and that shows as less transfer. This is the new screen baseline.
  - **u5j (MINE_MIRROR on u4s) submitted** (#163), block 1 on set 1: 16 scrims queued (waiter build/wait_u5jb1.sh, matches above 7652). The local screen of **u5k** (CAT_REACH 30 + AIM_LEFT 3000 on u4s) runs as VM run xw2 against cb2. Pre-registered as a basics repair: overruns not above cb2's 1; cat deaths not above cb2's +10%; wins not below cb2's minus 3.
- 14:08: **u5j (MINE_MIRROR on u4s) rejected at replica block 1** (#163; 7667-7670, 7684-7687, 7700-7703, 7725-7728): 48 cells, 46 alike, D 2 (+1 collegejunior clockwisdom B, −1 Varun knifefight A), net 0, t 0. On the field the mirrored home-mine list hardly changes a game, as u4f's local D 0 had already suggested. That makes five economy rejects in a row. **u5k (CAT_REACH 30 + AIM_LEFT 3000 on u4s, a basics repair) submitted** (#164), block 1 on set 1, 16 scrims queued (waiter build/wait_u5kb1.sh, matches above 7728). Its local screen xw2 keeps running as a check. The queue is now empty after u5k: the next candidate must be new work, and the area rules leave economy and cat-awareness out.
- 14:11: **Loss scan of the u4s family on set 1** (u4s ref, u5i, u5j: three near-copies of each cell; scratch cellsum.py; s3sum.py over light dumps of 7508/7509, 7527-7530, 7543/7544, 7555/7556). The replica is nearly deterministic: u4s and u5j played identical games in most cells. Losses that repeat: Xzibit learn_your_lesson and starvation in both seats, YokoOno Stash and arrows in both seats (both opponents 5 of 18), collegejunior dirtpassageway and clockwisdom, Varun DefaultMedium A/B, Finals DefaultSmall A and jail A/B, Merlin thunderdome A/B. In the long Xzibit/YokoOno losses their babies hold 12-16 from r100 to the end while ours fall from 20-26 to 0-5. Their pickups run 1.6-10x ours. Twice (Stash 7527 g0, starvation 7543 g2) we held two kings at r300-530 with the bank under 100, and on Stash both kings starved.
  - Already explored, so not new levers: thunderdome (the AMMO_AWAY / GUARD_KING_R family; the 00:04 entry), trap deaths (6-25 a game; u0q, u0y, u3d rejected), and the pickup gap (economy, five rejects in a row).
  - **pre-registration, u5l = u4s + FORM_FROM 600** (an existing switch, snapshot only; the king-formation area). It allows no second king before round 600. The rich path (bank ≥ 3000) and END_KING are gated by FORM_FROM too, so those first kings come at r600 or later. Evidence: the 02:59 census (second kings formed before r600: 5 W 12 L; r600-999: 7 of 7 W; none: 31 W 33 L). s5y passed its local screen with this switch (net +9) but reached the replica only bundled as s6j, which was rejected (t −0.30).
    - Inspiring cells: YokoOno Stash A (7527 g0), Xzibit starvation A (7543 g2), Varun DefaultMedium (the 02:59 entry: babies 20 to 11 at the r112 merge), Finals and collegejunior dirtpassageway.
    - Local screen md2 (48 kvs cells vs cb2): (1) mechanism: second kings formed before r600 = 0 (checked from light dumps of a sample); (2) wins not below cb2's 35 minus 3; (3) picks per 100 rat-turns ≥ 36.5 (cost bar outside screen spread).
    - Replica: the usual block protocol on set 1 against u4s's set-1 refs, after u5k.
- 14:54: **u5k (CAT_REACH 30 + AIM_LEFT 3000 on u4s) rejected at replica block 1** (#164; 7740-7743, 7754-7757, 7783-7786, 7801-7804): 48 cells, 43 alike, D 5 (+2 −3), net −1, t −0.44. Its local screen xw2 had passed every bar against cb2 (38 wins of 48 to 35; D 8 (+5 −3) on 44 cells; 0 overruns to 1; picks 38.00). The repair is real locally but does not move field cells. Four straight block-1 rejects on u4s (u5i, u5j, u5k, plus the near-wash reference) say set 1 is mostly settled for small switches: about 43-46 of 48 cells repeat exactly.
  - **u5l (FORM_FROM 600) submitted** (#165), block 1 on set 1 (waiter build/wait_u5lb1.sh, matches above 7804). Its local screen md2 runs alongside on the VM instead of first, so the replica is not left idle. Its pre-registered bars are unchanged, and if md2 fails one I stop the block.
- 15:45: **u5l (FORM_FROM 600) passes replica block 1** (#165; 7821-7824, 7839-7842, 7859-7862, 7885-7888 vs u4s's set-1 refs): 48 cells, 46 alike, D 2 (+2: Varun DefaultMedium A, an inspiring cell, and collegejunior clockwisdom B; −0), net +2, t +1.40.
  - **Local screen md2 passes all three pre-registered bars** (48 kvs cells vs cb2):
    - (1) No second king formed in any game that ended before r600 (cb2: 6 such games). Formations 19 to 22, rats merged 116 to 132.
    - (2) 41 wins of 48 to 35 (bar 32).
    - (3) 38.64 picks per 100 rat-turns (bar 36.5).
    - On the 44 cells shared with pf2's layout: D 9 (+7 −2), t ≈ +1.64, xfer 288,549 to 274,006. The gains are against abrk and araid on DefaultSmall, arrows, DefaultMedium (both seats), bed and starvation B. The losses are araid cheesefarm B and starvation A. 1 overrun (cb2 also 1).
  - Block 2 on v4 set 2 is queued (16 scrims, matches above 7888, waiter build/wait_u5lb2.sh; refs u4s's 7420-7423, 7440-7443, 7459-7462, 7488-7491).

## 2026-11-01 19:25 PST: replica outage (VM disk full) fixed; replica replays pruned

- 19:25: **Replica outage.** The owner reported that the site said "That episode was not found". The VM's root disk was 100% full (0 bytes free), so the replica's Postgres cluster (15/bc26) stopped taking connections. Every API call failed with psycopg2 OperationalError, so the frontend found no episode. 10 of u5l's 16 block-2 scrims ended in ERR (8028, 8029, 8031, 8032, 8034, 8035, 8065-8068); 6 finished (7902-7905, 8030, 8033). The disk had filled with replica replays (9.4 GB in /srv/bc26-galaxy/storage/bc26-replica-secure/episode/bc26/replays, about 1.2 MB per match, many matches an hour counting the field ladder).
  - **Deletions on the VM (all our own data), in order:**
    - ~/diag26b/rp: my 104 copies of replica replays (193 MB; every one also kept locally).
    - 82 build/dumpcache dirs older than a day (719 MB; derived from replays and seeded games of finished analyses; names in ~/diag26b/dcache_deleted2.txt).
    - Then, once the owner pointed out that no rule protects it (the "not ours to prune" note was my own caution, not theirs): **4,321 replica replays older than two days** (about 5 GB; matches before about 2026-10-31 03:20 UTC, mostly field-vs-field ladder games; file list in /root/bc26-replays-pruned-2026-11-02.txt on the VM). The site's Replay links for those old matches no longer work. Their results stay in the database, and our local match.json files are untouched.
  - Postgres recovered by itself at 03:22 UTC once space was free. The site and API answer 200 again, and the VM now has 5.8 GB free (70%).
  - The 10 failed u5l scrims are re-queued on the same seats and maps. u5l's block 2 counts the 6 finished plus the 10 reruns.
  - Prevention: keep VM root free space at 2 GB or more. Prune old replica replays (older than two days) whenever it drops below that, and check it at every block.
- 19:41: **u5l block 2** (set 2; 7902-7905, 8030, 8033 plus the 10 reruns 8098-8107) vs u4s's set-2 refs: 48 cells, 46 alike, D 2 (+2: Finals dirtpassageway B, YokoOno streetsofnewyork B; −0). **Pooled with block 1: 96 cells, D 4 (+4 −0), net +4, t +1.99** (sd ≈ √(D/(n−1)), as in every earlier decision). That is under the accept bar of 2 and over block 2's reject bar of 1, so the bar holds and u5l goes to block 3 on set 3. u4s has no set-3 games, so its set-3 reference comes first: u4s resubmitted (#166), 16 scrims (waiter build/wait_u4sr3.sh, matches above 8107). Then u5l is resubmitted for block 3 (block 3 rejects at pooled t ≤ 1.5 and accepts at ≥ 2). VM disk 5.7 GB free.
- 20:38: **u4s set-3 reference done** (#166; 8108-8117, 8171-8176; all 16 OK, fetched). **u5l resubmitted for block 3** (#167) on v4 set 3: 16 scrims (waiter build/wait_u5lb3.sh, matches above 8176). Local disk 232 MB free, VM 5.7 GB.
- 21:34: **u5l (FORM_FROM 600) rejected at replica block 3** (#167; set 3: 8186-8189, 8192-8201, 8272-8273 vs u4s's set-3 refs): block 3 D 5 (+2 −3), net −1. Pooled over 144 cells: D 9 (+6 −3), net +3, t +1.00 (block 3 rejects at ≤ 1.5). After +4 −0 in blocks 1-2, set 3 gave back a little. The effect is small and positive (locally +7 −2): late formation is probably right, but too few field cells turn on it. It is logged as a near-miss with an independent mechanism, so it can be combined with another near-miss later. Of the four candidates tried on u4s (u5i, u5j, u5k, u5l) it did best, and it is still not enough.
- 21:34: **u5m = u4s + FORM_FROM 600 + HUNGRY 300** combines two near-misses whose mechanisms are independent: u5l (formation timing, pooled t +1.00) and u5a (delivery threshold, pooled t +0.28 on u4e). Submitted (#168), block 1 on set 1 (waiter build/wait_u5mb1.sh, matches above 8273). Local screen xs2 against cb2 runs alongside. Bars: wins ≥ 32 of 48, picks ≥ 36.5 per 100 rat-turns. The next new idea must be structural: none of the last four attempts was.
- 21:58: **u5m passes replica block 1** (#168; 8283-8286, 8292-8295, 8301-8308 vs u4s's set-1 refs): 48 cells, 40 alike, D 8 (+5 −3), net +2, t +0.70. Gained three of u5l's inspiring cells: YokoOno Stash A, Xzibit learn_your_lesson A and Varun DefaultMedium A. Also gained Finals tiny A and collegejunior clockwisdom B. Lost Finals tiny B, Xzibit attack_on_cat B and spsquared bed B. **Local screen xs2 passes**: 41 wins of 48 (bar 32); on 44 cells D 8 (+7 −1), t ≈ +2.10; picks 38.22 (bar 36.5); 0 starved kings (cb2 5); 1 overrun. Block 2 on set 2 is queued (waiter build/wait_u5mb2.sh, matches above 8308).
  - **Local deletion** (disk at 179 MB free): 281 of the 286 build/dumpcache entries, all older than two days (about 2.5 GB; the 5 newer ones kept). They are dump caches of earlier local analyses, rebuilt on demand by the dumper; all 286 names are listed in build/dumpcache_deleted_2026-11-01.txt. Local free space is now 2.7 GB.
- 22:52: **u5m block 2** (set 2; 8318-8319, 8375-8376, 8396-8407) vs u4s's set-2 refs: 48 cells, 46 alike, D 2 (+2 −0: Finals dirtpassageway B and YokoOno streetsofnewyork B, the same two cells u5l gained, so they come from FORM_FROM). Pooled with block 1: 96 cells, D 10 (+7 −3), net +4, t +1.26, which is between the block-2 bars (reject ≤ 1, accept ≥ 2). Block 3 on set 3 is queued (waiter build/wait_u5mb3.sh, matches above 8407; refs u4s's 8108-8176).
- 23:53: **u5m (FORM_FROM 600 + HUNGRY 300) rejected at replica block 3** (#168; set 3: 8413-8416, 8422-8423, 8476-8477, 8491-8498): block 3 D 5 (+2 −3), net −1. Pooled over 144 cells: D 15 (+9 −6), net +3, t +0.77 (block 3 rejects at ≤ 1.5). It went the way u5l did: positive on sets 1 and 2, then set 3 gave some back. Both lost Finals where_are_you A, so late formation probably costs there: on that map where_are_you's far mines want a second king early (the FORM_MINE note). Both local screens were strongly positive (41 wins of 48 to 35), but the field cells that turn on formation timing are few. The two-switch economy line is done for now. The next attempt is structural, as the one-in-four rule requires.

## 2026-11-02 00:03 PST: u5n (RUSH_DIST, structural) on the replica

- 00:03: **King-distance census** (scratch kdist.py over the light dumps of 294 field games of u4e-u5m; Chebyshev distance between the starting kings): ≤ 10: 28 games, 46% won, our early king kills (r ≤ 400) 12, theirs 14; 11-20: 24 games, 67%, 9 and 6; 21-30: 62 games, 47%, 2 and 6; 31+: 180 games, 37%, 7 and 8. Most field games are on far maps, where we do worst. There the rush (babies 4-15 attack from birth) almost never kills a king, while the attack tag is the largest share of our rat-rounds in losses (21.5%, the 03:29 entry). Earlier stand-downs worked by round or age (RUSH_UNTIL, RUSH_LIFE, RUSH_BURN: s8v, u2i, t0g, u1w, u3b). None used map geometry.
- 00:03: **u5n = u4s + RUSH_DIST 20** (new switch, structural: roles by map geometry). A newborn rusher keeps ATTACK only while some live symmetry (the kings' SYM slot narrowed by its own mask) puts the enemy start, its king's position mirrored, within Chebyshev 20. Otherwise it takes the role it would have had without the rush. Both sides compute the same distance, so it is side-symmetric. BotTest 1860/1860 with 5 new checks. Four mutations were run, and each check fails under at least one of them. A fifth mutation showed that the ROT|HOR|VER mask was redundant (Sym.mask holds only those bits), so that line is now `slot & Sym.mask`. Unit tests exit 0.
  - Pre-registered bars on the local screen xt2 (48 kvs cells vs cb2, running alongside the replica): (1) on far-map cells (start distance > 20), picks per 100 rat-turns at or above cb2's on the same cells; (2) wins ≥ 32 of 48. Replica: the usual block protocol on set 1 against u4s's refs. Inspiring cells: far-map losses (Xzibit learn_your_lesson, YokoOno arrows, collegejunior clockwisdom and dirtpassageway).
  - Submitted (#169), block 1 on set 1 (waiter build/wait_u5nb1.sh, matches above 8498).
- 00:49: **u5n (RUSH_DIST 20) rejected at replica block 1** (#169; 8620-8635): 48 cells, 36 alike, D 12 (+4 −8), net −4, t −1.14. The mechanism did appear on the field: it gained the far-map inspiring cells Xzibit learn_your_lesson A/B and YokoOno arrows A, plus spsquared superioristhmus B. It lost rose three times (Icarus A, collegejunior A/B), Icarus TheHeist B, YokoOno clouds B (u4s's rush kills that king by r92-100, with the kings 47 apart), Merlin peaceinourtime A, and spsquared fullofsecrets B and superioristhmus A. Start distances per map (scratch mapdist.py): gains at 25, 37 and 47, losses at 27-47, interleaved, so no RUSH_DIST threshold separates them. The rush pays against some opponents on far maps (YokoOno clouds) and not against others. Locally (xt2 vs cb2) it won 37 of 48 to 35 and delivered 19% more (336k to 274k). Our local archetypes reward an economy turn more than the field does.
  - Lesson: the census grouped games by geometry, but the rush's payoff depends on the opponent's early defence. A rush switch would have to read the opponent: for example, stand down once the rush has met a defence, as RUSH_LIFE and RUSH_BURN tried in their ways.
- 00:51: The replica would otherwise sit idle, so **u4s's set-4 reference** is queued (u4s resubmitted as #170; 16 scrims, matches above 8635; waiter build/wait_u4sr4.sh). Any candidate that reaches block 4 needs it.
- 01:03: **King-death census of u4s's references** (48 matches, 126 games; lite dumps on the VM; scratch kdie.py). Losses (64) lost 81 kings: 43 starved, 14 bitten, 7 thrown, 3 to cats. Every starvation came after the army war was lost: 100 rounds before it the median bank was ~80, our babies 0-8 against their 12-45. So starvation is the end state, not the cause.
  - Over time (losses vs wins): deliveries are level to r100 and the gap opens from r150. Our babies fall 21.7 (r50) to 13.6 (r600) while theirs rise 16.7 to 22.7. Our bank sits at the 300 spawn floor (median 300-450 from r200) while theirs holds 1000-1400.
  - Rat-vs-rat deaths in a lost game: ours 45.1 (60.7 less 15.6 to cats), theirs 32.5 (58.3 less 25.8). Enemy traps kill 12.1 of ours. We lay none, so they lose 0 to ours. The traps cover ~12 of the ~13-death gap. In wins the gap is small (17.7 to 15.5, our trap deaths 5.0).
  - Lever: u4m (u3d's reactive baby trap set on u4e) changed 11 of 48 set-1 cells. It lost the near maps (tiny B, DefaultSmall B, uneasy_alliance A/B, knifefight A, attack_on_cat B) and gained the far ones (Stash A, arrows B, DefaultMedium B, superioristhmus B; tiny A the exception).
- 01:03: **u5o pre-registered (structural: trap doctrine by map geometry) = u4s + u4m's trap set + TRAP_DIST 20** (new switch). A newborn may lay rat traps only when no live symmetry puts the enemy start within Chebyshev 20 of its home king. Bars as usual. Falsifier: in its field games our traps kill fewer than 2 enemy rats a game on far maps.
- 01:24: **u5o local screen xt3** (48 cells vs cb2, u4s's control): 41 wins to 35. Of the cells both runs completed, D 9 (+7 −2): it gained abrk DefaultSmall A and arrows A/B, and araid arrows B, bed A, canyoudig B and starvation B. It lost araid cheesefarm B and starvation A. Starved kings 1 to 5, delivered 262k to 273k (−4%, cheese spent on traps). The local screen passes (it rejects only at t ≤ −1.5). The replica follows the u4s set-4 reference block.
- 01:31: **u4s set-4 reference** (#170): 8636-8649 are done; the Merlin pair 8673/8674 is still running as u4s scrims (queued before the next submission). **u5o submitted** (#171). Block 1 (16 scrims, v4 set 1) is queued; the waiter counts matches above 8674.
- 01:36: **u4s set-4 reference complete**: 8636-8649 + 8673/8674 (queued before #171, so u4s games). 27 of 48 won: Finals 4/6, Icarus 4/6, Xzibit 3/6, YokoOno 2/6, Varun 3/6, Merlin 1/6, spsquared 6/6, collegejunior 4/6.
- 02:13: **u5o passes replica block 1** (#171; v4 set 1: 8690-8693, 8704-8707, 8731-8738) vs u4s's set-1 refs: 48 cells, 41 alike, D 7 (+4 −3), net +1, t +0.38 (block 1 rejects at t ≤ 0). Gained Finals jail B, Icarus rose B, YokoOno arrows B (a far-map inspiring cell) and collegejunior clockwisdom B. Lost Xzibit attack_on_cat B, YokoOno clouds A and Merlin corridorofdoomanddespair B. Block 2 (v4 set 2) is queued: matches above 8738, against u4s's set-2 refs 7420-7491. The trap falsifier census on the block-1 replays is running on the VM.
- 02:14: **u5o's trap falsifier passes** (block-1 replays, lite dumps; scratch trapkill.py). On the 36 far-map games (king start distance > 20) we laid 20.2 rat traps a game, and they killed 7.9 enemy rats a game (bar: 2). Enemy traps killed 6.9 of ours, the same as on near maps. On the 12 near-map games no traps were laid, as gated. DefaultSmall (21) and Stash (23) also laid none: some live symmetry still put the enemy start within 20 there. Biggest users: superioristhmus 124/298 laid (48/125 kills), attack_on_cat 37/40 (12/16).
- 03:13: **u5o block 2** (set 2; 8745-8746, 8779-8780, 8797-8800, 8813-8816, 8832-8835) vs u4s's set-2 refs (7420-7491): 48 cells, 40 alike, D 8 (+4 −4), t 0.00. Gained Finals attack_on_cat B, Finals dirtpassageway A, YokoOno streetsofnewyork B and collegejunior learn_your_lesson B. Lost Finals attack_on_cat A, YokoOno Hike A/B and spsquared jail B. **Pooled with block 1: 96 cells, D 15 (+8 −7), net +1, t +0.26, at or below block 2's bar of 1: u5o (TRAP_DIST) rejected.**
  - Reading: the traps work as a mechanism (7.9 enemy rats killed a far-map game) but don't turn games. The trap line now has u0q, u0y, u3d, u4m, u4x and u5o, all neutral or rejected. Rats killed by traps are replaced by spawns while the traps' cheese is gone. Three rejects in a row in war/role areas (u5n rush, u5o traps), so the next attempt goes elsewhere.
- 03:22: **Where the losses' economy goes** (u4s's 48 reference matches, games of 400+ rounds, pickups by r400 split by the half each lies in). In losses the enemy picked 2720 at home and **872 in our half**; we picked 1899 in our half and 263 in theirs. In wins: enemy 1671 at home and 127 in ours; we 1777 in ours and 238 in theirs. So our half yields about the same (~2770) either way, but in losses a third of it walks away in enemy pockets. Total pickups by r400 in losses are 155 for them against 97 for us (wins 92 to 89).
- 03:22: **u5p pre-registered = u4s + CARRY_BITE 60** (new switch; incremental: bite targeting). A rat that dies drops its raw cheese where it falls (GameWorld.destroyRobot), so an enemy baby carrying cheese ranks in our bite order as if up to 60 health weaker. It never ranks ahead of a rat carrying one of ours (rank floor 1001). Submitted as #172; block 1 (v4 set 1) is above 8861, with the local screen xc1 vs cb2 alongside. Bars as usual. Falsifier: enemy pickups in our half by r400 in its field losses not below u4s's 37.1 a game.
- 03:41: **u5p local screen xc1** (48 cells vs cb2): 37 wins to 35, delivered 298k to 273k (+9%), starved kings 1 to 5. Of the cells both runs completed, D 7 (+4 −3): it gained abrk DefaultSmall A and arrows A, and araid arrows B and bed A. It lost araid cheesefarm A/B and starvation A. Caveat: abrk DefaultSmall A, arrows A and araid bed A are the same games, round for round, as u5o's xt3 gains. Something both snapshots share and cb2 (bots/u4s) lacks moves those cells. Likely candidate: bytecode drift from the dev source's new inactive code (RUSH_DIST, TRAP_DIST helpers). So the local screen overstates any snapshot taken from today's source; the replica's paired protocol against u4s's refs is unaffected in kind but carries the same drift. The screen passes (rejects only at t ≤ −1.5).
- 03:57: **u5p passes replica block 1** (#172; v4 set 1: 8862-8867, 8874-8875, 8891-8894, 8907-8910) vs u4s's set-1 refs: 48 cells, 45 alike, D 3 (+2: Finals tiny A, collegejunior clockwisdom B; −1: Finals DefaultSmall B), net +1, t +0.58. Block 2 (v4 set 2) queued above 8910.
- 04:00: **Source drift check** (null snapshot nul1 = today's source + exactly u4s's switches, local only and not committed; run xn1, the same 48 cells). Against cb2 (bots/u4s) it reproduces u5p's xc1 almost game for game: 37 wins, D 7 (+4 −3, the same seven cells), delivered 300k to 274k. u5p against nul1: 44 cells, D 0, delivered 298k to 299k. So all of the xt3/xc1 difference from cb2 is drift in the dev source, not the candidates' switches.
  - The dev source differs from bots/u4s in code that is meant to be inert when off: CAT_BLIND's per-turn hpEnd read, the catReach()/catStalled calls, LOAD_AT, FIGHT_POST and KING_STRICT wrappers, the RUSH_DIST/TRAP_DIST helpers and biteScore. Seal.java has a real fix (arrays allocated after the bytecode check). Extra bytecodes shift the bot's Clock-gated choices (SENSE2_LEFT, Flow.left()), which is enough to change a few deterministic games.
  - Consequences. (1) Local screens now use xn1 (nul1) as their control instead of cb2. (2) On the replica the drift is part of every candidate built since u4s (u5i-u5p), but it moved only 3-12 cells a block and the paired protocol scores it like any other change. A candidate accepted with it would carry it too. No re-scoring needed.
- 04:48: **u5p block 2** (set 2; 8934-8937, 8962-8967, 8980-8981, 8998-9001) vs u4s's set-2 refs: 48 cells, 46 alike, D 2 (+0 −2: YokoOno Hike A/B), t −1.41. **Pooled with block 1: 96 cells, D 5 (+2 −3), net −1, t −0.44: u5p (CARRY_BITE) rejected** (block 2 rejects at t ≤ 1).
  - YokoOno Hike A/B were also lost by u5o's block 2. CARRY_BITE and TRAP_DIST share nothing but the source drift (04:00 entry), so the drift itself likely costs Hike both seats against YokoOno. Before the next candidate goes to the replica, the drift needs isolating: nul1 itself on set 2 tells us whether it is the drift.
- 04:50: **Drift probe on the replica**: nul1 submitted as #173 (diagnostic, not a candidate), with 16 scrims on v4 set 2 above 9001, against u4s's set-2 refs. If it loses Hike A/B too, the drift is the cause and gets isolated and repaired before the next candidate.
- 04:53: **u5q pre-registered = u4s + FIGHT_EVERY 3** (existing switch, dose from 2; incremental: role split). One in three later babies is a cat fighter instead of one in two, so more babies gather. In u4s's in-band losses we pick 97 piles by r400 to the enemy's 155 (wins 89 to 92). FIGHT_EVERY 3 was screened on s7d (s8r: net +5) and reached the replica only inside the s8x bundle, which TRAP_CAP 25 sank. Local screen xq1 vs nul1 (same 48 cells). Replica after the nul1 drift probe, with the usual bars. Falsifier: our pickups by r400 in its field losses not above u4s's 97.
- 05:12: **u5q local screen xq1** (vs nul1 xn1, 48 cells): 38 wins to 37. Of the cells both runs completed, D 16 (+9 −7): it gained araid DefaultMedium A/B and cheesefarm A/B, among others, and lost araid learn_your_lesson A/B and jail A, among others. Picks per 100 rat-turns 40.56 to 39.10, but delivered 279k to 300k (−7%), with 4 overruns to 1. It passes (the screen rejects only at t ≤ −1.5). The replica follows the drift probe.
- 05:34: **Drift probe result** (nul1 #173 on v4 set 2: 9015-9018, 9036-9039, 9060-9065, 9074-9075) vs u4s's set-2 refs: 48 cells, 46 alike, D 2 (+0 −2: YokoOno Hike A/B), t −1.41. That is the very pair u5o and u5p lost. So their block-2 losses were the drift, not their switches.
  - Cause. Every source change since u4s is inert with its switch off (walkStep → step, catReach() → true, biteScore → the old ranks, LOAD_AT's else branch). Only bytecodes differ: born() now always evaluates Comms.read(SYM) for trapsAllowed, and the per-turn hpEnd read and the extra wrapper calls add more. Clock-gated choices (SENSE2_LEFT, Flow.left()) branch differently in a few deterministic games, and Hike's game first splits at r108 with a pickup one tile over. Any code change does this, so it is zero-mean noise per change, but it lands on whichever cells it lands on.
  - Method change. A candidate from today's source is paired against **references built from the same source** (nul1: today's source + exactly u4s's switches), so the drift cancels and the pair measures the switch alone. nul1's set-2 games above are its set-2 reference. Its set-1 reference is queued now (above 9075). u5q's blocks follow against nul1's refs. The incumbent stays u4s. A candidate accepted against nul1 ships with the drift, whose measured cost is −2 on set 2 and is part of the noise every change carries.
- 06:31: **nul1 set-1 reference** (9093-9096, 9113-9116, 9141-9144, 9165-9168) vs u4s's: 48 cells, 47 alike, D 1 (+1: collegejunior clockwisdom B, which u5o and u5p also "gained"). So the drift is +1 on set 1 and −2 on set 2. **u5q submitted** (#174). Block 1 (v4 set 1) is above 9175 and is paired against nul1's set-1 refs.
- 07:17: **u5q (FIGHT_EVERY 3) rejected at replica block 1** (#174; v4 set 1: 9176-9177, 9181-9182, 9199-9202, 9212-9215, 9239-9242) vs nul1's set-1 refs: 48 cells, 37 alike, D 11 (+4: Finals DefaultSmall A and tiny A, Icarus rose B and uneasy_alliance A; −7: Finals DefaultSmall B, Icarus uneasy_alliance B, Xzibit attack_on_cat B, Varun knifefight A, spsquared bed B and superioristhmus A, collegejunior clockwisdom B), net −3, t −0.90. Fewer cat fighters cost more than the extra gatherers bring, mostly on small maps. The role-split area now has s8r, s8s, s8x and u5q.
- 07:24: **u5r pre-registered: THIEF_LOAD 20** (bite-the-thief, the gap the 04:20 theft census found: no attempt has ever targeted loaded carriers). At war, a rat that is carrying no robot and holds fewer than DELIVER_AT raw hunts (a POUNCE-style hunt that overrides WAR_GATHER and STANDOFF) the nearest visible enemy baby within dist2 20 that carries 20+ raw cheese, is no healthier than it, and stands on our half (nearer our reference than its mirror). POUNCE prey still comes first. A killed thief drops its load on our half. Area: theft defence (u5c, u4f/u5j before; the previous three rejects were rush, traps and bite rank, role split). Built on u4s's recipe and paired against nul1's same-source refs. Falsifier: in its field losses, enemy pickups in our half by r400 must fall below u4s's 872 cheese a lost game; otherwise the mechanism did not work, whatever the score.
- 07:45: **u5r local screen xr1** vs nul1's xn1: 44 cells, D 9 (+4: abrk arrows B and canyoudig B, araid canyoudig B and starvation A; −5: araid DefaultSmall A, arrows A/B, canyoudig A and wallsofparadis A), net −1, t −0.31. It passes the screen. Four of the five losses are against araid, the raider archetype. Wins 34 to nul1's 37, delivered 287k to 300k, and overruns 5 to 1 (the thief scan runs over every visible robot; see if the field shows any). **u5r submitted** (#175). Block 1 (v4 set 1) is above 9242 and is paired against nul1's set-1 refs.
- 08:02: **u5r (THIEF_LOAD 20) rejected at replica block 1** (#175; v4 set 1: 9297-9308, 9312-9315) vs nul1's set-1 refs: 48 cells, 42 alike, D 6 (+2: Finals DefaultSmall A and tiny A; −4: Finals DefaultSmall B and tiny B, Xzibit attack_on_cat B, spsquared bed B), net −2, t −0.82. The Finals flips are mirrored A-gain/B-loss pairs on two tiny rush maps, so they say little. The rest are losses. The mechanism count (pickups in our half by r400, u5r vs nul1, same cells) follows.
  - Mechanism (pickup counts by r400 on the same 48 cells, halfpick.py): enemy pickups in our half 17.8 a game for u5r vs 17.1 for nul1 (in losses 27.6 vs 28.7), and ours in our half 71.2 vs 73.4. The hunts did not cut theft and cost our own gathering a little, so the falsifier holds against the idea, not just the score. A hunter at dist2 ≤ 20 rarely catches a carrier that is already walking home. Theft defence now has u5c, u4f/u5j and u5r, so the next attempt goes elsewhere and is structural.
- 08:14: **Economy census, the 96 same-source games** (nul1 set 1 + u5r block 1, the 64 that reached r400; econcum.py). In losses the enemy out-picks us from the start with fewer rats: cumulative picked by r100 458 to their 535, r400 2345 to 3035, with 23 babies to their 16 at r100. That is 20 cheese a baby to their 33. They also carry 238 raw at r100 to our 82, so they make fewer trips home. Our bank falls 1147 → 567 by r400 while theirs holds about 1300: our extra spawns cost 50-60 each and don't pay back. In wins we pick 2576 to 2017.
- 08:14: **u5s pre-registered (structural: economy doctrine, the field's haul cycle) = u4s + LOAD_AT 40.** This is u5d's switch retried. u5d was killed at its local screen by falsifier (3), "delivered ≥ 271k", which was confounded: won games ended sooner, delivered per 100 rounds was flat (647.5 vs 645.8), and its screen was +3 (D 9, +6 −3, t +1.0). It never reached the replica. Built from nul1's source with only LOAD_AT changed, so there is no drift and the u5r thief code is absent. Falsifiers, all rate-based. Local screen vs xn1: (1) mean transfer ≥ 38 (nul1 30.4). (2) Picks per 100 rat-turns ≥ 37.1 (nul1 39.10, −5%). Field, on the paired cells: (3) our cumulative picked by r300 in games of 300+ rounds is at least nul1's +5%. Kill at the screen only if (1) fails or t ≤ −1.5. (3) is the mechanism check at block 1.
- 08:33: **u5s local screen xl1** vs xn1: 44 cells, D 9 (+4: araid DefaultMedium A/B, cheesefarm A/B; −5: abrk DefaultSmall A and bed A, araid arrows A/B and keepout A), net −1, t −0.31. Falsifiers: (1) mean transfer 38.9 (≥ 38) passes; (2) picks per 100 rat-turns 38.48 (≥ 37.1) passes. Delivered 261k to 300k, but with the same 34-vs-37 game-length confound as u5d. Overruns 3. **u5s submitted** (#176). Block 1 (v4 set 1) is above 9315 and is paired against nul1's set-1 refs.
- 08:59: **u5s (LOAD_AT 40) rejected at replica block 1** (#176; v4 set 1: 9427-9442) vs nul1's set-1 refs: 48 cells, 42 alike, D 6 (+2: Icarus rose B, Merlin peaceinourtime B; −4: run_away10 t_minus_five_minutes B, spsquared bed A and superioristhmus A, collegejunior clockwisdom B), net −2, t −0.82. Falsifier (3) fails: our cumulative picked by r300, pooled over the games that reached r300, was about 1627 a game to nul1's 1662 (−2%, bar +5%). In losses we carry 342 raw at r300 to nul1's 395, but pick only 1884 to 1756 against the same enemy's 2105 to 2031. Full loads make the trips longer without picking more: the trip home is not what limits our rate. The haul-cycle line is closed (s6t/s6u unrun, t0s, u5d, u5s).
- 09:05: **Diagnosis of the always-lost cells** (lost in all three same-source set-1 runs, nul1/u5r/u5s: 20 of 48 cells; every panel opponent is in band, 28-72%). Losses are map-specific. thunderdome vs Merlin: the kings start 4 apart, 90 throws of our rats in 120 rounds. DefaultMedium vs run_away10: 58-68 enemy rat traps, which our rats fire 50-62 times by r300, mostly 1 round after the lay as they step in to bite (the RISK_FRESH/RISK_ADAPT line t0b/u0u/u1b already showed that avoidance costs more fights than it saves). starvation vs Xzibit (both seats): the bank goes 2488 → 1383 (r51) → 788 (r101) → the 300 floor by r200 with 20-24 babies, against their 1798 → 1453 → 1013 with 16. Our babies then die unreplaced (8 by r300, 1-3 at the end) and the king starves.
- 09:05: **u5t pre-registered (spawn doctrine) = u4s + CORE_BABIES 12** (from 18). Up to CORE_BABIES babies the king spawns down to CORE_FLOOR 300 whatever its income. Past it, every spawn must pass Econ.surplus() (the projected end-of-game cheese at the measured income). So on a poor map we stop at 12 and keep the bank, and on a rich map the income test still lets the count climb. No attempt has lowered the core count (s4h, s5r and s5z raised caps). Built from nul1's source with only that constant changed. Falsifiers: local screen vs xn1, kill only at t ≤ −1.5. Field block 1, same cells as nul1: (1) our bank at r200, pooled over games that reach r200, is at least nul1's +100; (2) starved king deaths in losses do not rise. If (1) holds and the score falls, rats are worth more than cheese early, and the line closes.
- 09:20: **u5t local screen xk1** vs xn1: 44 cells, D 14 (+7: abrk arrows B and canyoudig B, araid DefaultMedium B, canyoudig B, cheesefarm A/B and starvation B; −7: abrk Stash A and arrows A, araid DefaultSmall A/B, arrows B, jail B and learn_your_lesson A), net 0, t 0. It passes. Delivered 210k to 300k (fewer gatherers on rich maps), starved kings 5 to 3, wins 36 to 37. **u5t submitted** (#177). Block 1 (v4 set 1) is paired against nul1's set-1 refs.
- 09:56: **u5t (CORE_BABIES 12) rejected at replica block 1** (#177; v4 set 1: 9462-9465, 9483-9486, 9508-9515) vs nul1's set-1 refs: 48 cells, 37 alike, D 11 (+5: Finals DefaultSmall A and tiny A, YokoOno arrows A, Merlin thunderdome A, collegejunior clockwisdom A; −6: Icarus TheHeist B and uneasy_alliance B, run_away10 t_minus_five_minutes B, Merlin peaceinourtime A, spsquared superioristhmus A, collegejunior clockwisdom B), net −1, t −0.30. Falsifier (1) holds: bank at r200 about 1110 a game to nul1's 867 (+243). The starvation cells stay lost, and in losses the enemy still out-picks us (1561 to 1204 by r200). So per the pre-registration, rats are worth more than cheese early, and the core-count line closes: the starvation losses are lost in the fight, not to the spawn bill.
- 10:01: **Where theft comes from** (theftage.py, the 32 same-source set-1 matches of nul1 and u5t, to r400). In losses the enemy's pickups on our half (1648) take cheese a median 10 rounds old, with our nearest live baby a median 7 tiles off (within 4 in only 29%). 85% lie in the outer half of our half (≥ 0.375 of the king-to-king distance from our king). They grow through the game (283, 383, 498, 484 per 100 rounds), at a median 12 live babies of ours, against 20 at our own pickups. So theft is uncovered mines after we lose babies, a consequence of the rat war, which is why hunting thieves (u5r) and posting fighters (u5c) did nothing.
- 10:01: **Plateau.** Straight rejects since u4s's acceptance (2026-11-01 11:41) far exceed the trigger of 8. Steps 1-4 ran in the last plateau and on the cadence since. This is **step 5: near-misses in pairs**. **u5u pre-registered = u4s + u5o's trap set** (BABY_RAT_TRAPS, TRAP_ADJ, TRAP_SPAWN_FIRST, RAT_TRAP_MIN_CHEESE 500, TRAP_OPEN_TO 150, TRAP_DIST 20) **+ u5p's CARRY_BITE 60.** These are the two best since u4s. u5o pooled t +0.26 over 2 blocks. u5p, corrected for the measured drift (its block-2 −2 was exactly nul1's Hike A/B), is D 3 (+2 −1) against nul1's refs. Both kill enemy rats: traps on far maps (7.9 enemy trap deaths a far game) and bites first on loaded carriers, whose cheese drops on our side. Built from nul1's source. Falsifiers: local screen vs xn1, kill only at t ≤ −1.5; field, enemy rat deaths a game on the paired cells ≥ nul1's +3.
- 10:19: **u5u local screen xu1** vs xn1: 44 cells, D 4 (+4: abrk arrows B, araid canyoudig B, cheesefarm A and starvation B; −0), net +4. Wins 41 to 37, starved kings 1 to 3, picks per 100 rat-turns 40.88 to 39.10, delivered 260k to 300k (shorter wins). **u5u submitted** (#178). Block 1 (v4 set 1) is paired against nul1's set-1 refs.
- 10:55: **u5u (u5o traps + u5p CARRY_BITE) rejected at replica block 1** (#178; v4 set 1: 9555-9558, 9565-9568, 9583-9586, 9608-9611) vs nul1's set-1 refs: 48 cells, 40 alike, D 8 (+4: Finals jail B and tiny A, Icarus rose B, YokoOno arrows B; −4: Finals DefaultSmall B, Xzibit attack_on_cat B, YokoOno clouds A, Merlin corridorofdoomanddespair B), net 0, t 0 (block-1 bar is t > 0). The local +4 −0 did not carry: araid and abrk are no model of the panel's fights. The pair adds nothing over its halves. Plateau step 5 is spent on these two. Next is step 6, a structural swing.
- 11:00: **Early labour census** (earlymix.py, nul1's 48 set-1 games to r150): 31% of our rat-turns in losses (37% in wins) are attack moves (G:a, the rushers). Death causes to r400 (deathcause.py, nul1 + u5u, 96 games): in losses ours 38.4 a game to theirs 35.1. The gap is traps (5.8 to 1.1). Throws kill both ways about equally (12.9 to 14.6). u5n showed the rush pays against some opponents on far maps (YokoOno clouds, rose) and not others (learn_your_lesson, arrows, superioristhmus), with start distances interleaved.
- 11:00: **u5v pre-registered (production mix by outcome) = u4s + RUSH_LIFE 150.** This is t0g's switch retried. t0g was rejected at s9s's set-4 block 1 (D 4, −4), where 2 of the 4 were outercloud seepastyourgreed A/B, which s9s itself flipped both ways. An attacker older than 150 rounds that has not seen the enemy king in the last 150 turns gatherer. So the rush keeps going where it reaches the king (clouds, rose: kills at r92-100) and stops where it never arrives: the rule by outcome that no RUSH_DIST threshold could give. Built from nul1's source with only that constant changed. Falsifiers: local screen vs xn1, kill only at t ≤ −1.5. Field block 1 on the same cells as nul1: (1) our share of attack moves in r150-400 rat-turns falls by at least a third; (2) no more than 1 of the 6 rose/clouds cells lost.
- 11:20: **u5v local screen xv1** vs xn1: 44 cells, D 2 (+1: araid canyoudig B; −1: araid arrows A), net 0. Wins 36 to 37, delivered 284k to 300k, picks per 100 rat-turns 36.71 to 39.10. It passes (locally the rushers mostly reach the archetypes' kings, so the switch seldom fires). **u5v submitted** (#179). Block 1 (v4 set 1) is paired against nul1's set-1 refs.
- 11:41: **u5v (RUSH_LIFE 150) rejected at replica block 1** (#179; v4 set 1: 9657-9664, 9667-9670, 9680-9683) vs nul1's set-1 refs: 48 cells, 40 alike, D 8 (+4: Icarus rose B, Xzibit learn_your_lesson A, YokoOno Stash A, spsquared superioristhmus B; −4: Icarus rose A, Xzibit attack_on_cat B, collegejunior clockwisdom B and rose A), net 0, t 0. Falsifier (2) fails (2 rose cells lost). Like u5n, it gained three always-lost far-map cells and lost rose. Why rose: our rats first see the enemy king there at r484-544 (clouds: r67-70), and the first rat-on-rat hostile act comes at r459, r989 or never (learn_your_lesson and Stash: r35-66). So on rose the game is a long cooperation, won by stalled rushers hunting cats (STALL_CAT; cat damage weighs in the cooperation score), and RUSH_LIFE turned them into gatherers first. Next: demote only at war.
- 11:45: **u5w pre-registered (production mix by outcome) = u4s + RUSH_LIFE 150 + RUSH_LIFE_WAR** (new switch, off in ratbot; rushDemote(), BotTest checks mutation-verified). A rusher that has not seen the enemy king in 150 rounds turns gatherer only once cooperation is over. On the war maps it does what u5v did (u5v's gains). On cooperation maps like rose the stalled rushers keep hunting cats (u5v's losses). Built from nul1's source plus the patch. Falsifiers: local screen vs xn1, kill only at t ≤ −1.5. Field block 1 on the same cells: (1) at most 1 of Icarus/collegejunior rose A/B lost against nul1; (2) at least 2 of the always-lost war-map cells (learn_your_lesson, Stash, arrows, superioristhmus, both seats) gained.
- 12:02: **u5w local screen xw5** vs xn1: 44 cells, D 4 (+2: araid canyoudig B, cheesefarm B; −2: araid arrows A, jail B), net 0. Delivered 317k to 300k, wins 36 to 37. It passes. **u5w submitted** (#180). Block 1 (v4 set 1) is paired against nul1's set-1 refs.
- 12:35: **u5w (RUSH_LIFE 150 + RUSH_LIFE_WAR) rejected at replica block 1** (#180; v4 set 1: 9718-9721, 9745-9752, 9757-9760) vs nul1's set-1 refs: 48 cells, 40 alike, D 8 (+4: Icarus rose B, Xzibit learn_your_lesson A, YokoOno Stash A, spsquared superioristhmus B; −4: Icarus TheHeist A, run_away10 t_minus_five_minutes B, Merlin peaceinourtime A, collegejunior clockwisdom B), net 0, t 0. Both falsifiers held (no rose cell lost; 3 always-lost war-map cells gained), so the mechanism works as designed, but the losses moved elsewhere. **Family census:** nul1, u5r, u5s, u5t, u5u, u5v and u5w are all one-switch variants of the same source; on the 48 set-1 cells each wins 22-24, each is within t ±0.65 of the leave-one-out family mean, and 28 of the 48 cells flip within the family. So most set-1 cells are coin flips for this source, and a one-switch tweak needs an effect of several cells to show. The rush-life line is closed for now (2 tries, both net 0). The next attempt is a structural swing (plateau step 6).
- 13:07: **How our losses end** (scratch klose.py; nul1, u5v and u5w set-1 games, 67 losses by king kill). Our last king **starved in 32** (median r867), was bitten to death in 25 (median r676), killed by a throw in 7 (median r133: thunderdome, tiny) and by a cat in 3. In 16 of the losses we lost two kings. Of the starvations traced (starvepath.py, nul1's set 1), two are hoards: on fullofsecrets and clockwisdom our rats carried 3-5k raw cheese while the bank ran to 0 with no deliveries, mostly attackers (tag a) with 1.3-2k each (hoardwhy.py). The rest are collapses: our babies die out (Stash 21 → 9, dirtpassageway 20 → 1) while the enemy keeps 16-37.
- 13:07: **Stash: our rats never leave the home pocket.** Stash is 30x30 ROT. A full wall row seals each home (rows 7 and 22), with a dirt gate of two tiles at each end, and dirt combs inside the pocket. As B vs YokoOno (9114, 9746) we picked 9 and 28 piles by r300 to their 161-182. 10-12 attackers (tag a) and the gatherers (C, M) stay at home to r300, while YokoOno digs both gates by r19 and fills the field by r25. Local mirror st1 (nul1 vs nul1, indicators) shows the mechanism. (1) The mask stays 7 to r150 on A: the terrain is symmetric under all three candidates, and VER puts the enemy king on our own king's body. (2) Flow plans "unknown = open", so every attacker routes through the enemy's unseen wall row, sights it a few steps in (Flow end 2, wall on path) and rebuilds. Each build takes 15-27 turns at ~15k bytecodes, and bug-walks meanwhile. A picked 41 piles to B's 200 and starved at r388.
- 13:07: **u5x pre-registered (basics repair: navigation/symmetry) = u4s + SYM_KING_GAP + SYM_FILL 200.** SYM_FILL is a new switch, off in ratbot: a seen wall or dirt tile is copied onto each unseen twin on which every live symmetry agrees (not marked seen; sensing overwrites it), and a baby re-scans 200 remembered tiles a turn after the mask shrinks (bytecode-guarded). SYM_KING_GAP (u1f's, never on the replica alone) rules out at round 1 a symmetry whose twin of our start overlaps our 3x3; without it VER blocks every fill on Stash. One mechanism: terrain known from symmetry. BotTest 1887/1887 (testSymFill, mutation-verified except the bytecode guard, which tests can't reach). Local: u5x mirror on Stash, 0 overruns (a first build without the guard overran 10-14 times), picks 229/324 to nul1's 41/200; head to head on Stash the seat decides (A loses both ways, picks 44/175 and 55/162), so Stash alone is not the evidence. Bars: screen xy1 passes (t > −1.5) with 0 overruns; falsifiers on block 1: (1) any overrun or exception; (2) picks by r300 summed over the block below nul1's refs. Basics repair: accepts at t ≥ 0 after block 2.
- 13:07: **Side asymmetry on Stash, re-found.** mirror_check on nul1's mirror game diverges at r2 (explore targets 10,18 vs 12,16: the first newborns face unmirrored ways), as at 2026-10-30. The side-symmetry bundle (SYM_FLOW, SYM_CENTRE, SYM_HAND, SYM_FACE, SYM_TIE; u2s, rejected at block 1 by net −1, t −0.38) moves the first divergence to r68 (scratch pb2), where twin attackers with identical state bug-walk opposite ways. With mask 7 one handedness can't mirror both a rotation and a reflection. Queued as the next candidate after u5x, rebased on today's source.
- 13:46: **u5x's first screen xy1** vs xn1: 44 cells, D 11 (+6 −5), wins 34 to 33, delivered 271k to 300k, but **8 overruns** to the control's 1 (tiny ×3, cheesefarm ×4, arrows ×1). That fails the bar. Located on tiny (local u5x vs nul1, OVS 2021): a fight turn near the limit crossed in pickUp/throw after paying fill() inside sensing. **Fix:** sensing now only flags a newly seen wall or dirt tile (Sym.fillDirty), and all filling runs in the end-of-turn backfill, which is bytecode-guarded and starts a new pass when the map is dirty or the mask shrank. BotTest 1890/1890 (3 new checks, mutation-verified). Reruns: tiny 1-2 overruns a game for nul1 too (a baseline fight-turn issue), Stash and cheesefarm mirrors 0. Re-screen xy3.
- 13:46: **u5y's screen xy2** vs xn1: 44 cells, D 15 (+7 −8), net −1, wins 32 to 33, delivered 248k to 300k (−17%), overruns 2 to 1. A weak pass (t > −1.5). It goes to the replica after u5x.
- 14:15: u5x xy3 (after moving fills out of sensing): D 16 (+9 -7), net +2, 35 wins vs ctrl 33, delivered 271k vs 299k, overruns 5 (tiny x3, a baseline nul1 also shows; jail B x2). Jail reproduced locally (araid vs u5x, OVS 999 = end-of-turn backfill): the `(i & 15)` guard let a pass resuming mid-block run up to 15 fills unchecked. Fix: check on entry and every 8 tiles counted from the call's first tile. BotTest 1890/1890; jail rerun 0 overruns. Confirming screen xy4 running.
- 14:30: u5x xy4 (guard fix): D 12 (+4 -8), net -4, 29 wins vs ctrl 33, delivered 281k vs 299k; overruns 3, all tiny vs abrk (ctrl: tiny 1). The jail overruns are gone. Local tiny repro: OVS 720 (moveDecision) and 2021 (pickUp/throw), fight-turn sites nul1 shares, not the fill. The two screens pooled: D 28 (+13 -15), net -2, so neutral, and no screen reached the reject bar (t <= -1.5). One flag: Stash vs abrk A lost with delivered 200, but Stash is the map this change targets. Submitted as submission 181; block 1 (v4 set 1) queued after match 9760.
- 14:36: The xy4 Stash loss (u5x A vs abrk) is deterministic locally. u5x delivered 200, all by r150; nul1 delivered 3512 by r400. At r150 three abrk rats sit at our wall-row gates (y 21, next to the y 18 mines). u5x's gatherers end up at y 23-26 with df= 73-79 (defend turns), while nul1's are on the mines with no defends; in nul1's game no abrk rat reaches y >= 19 by r150. Both runs read the symmetry as ROT (sym=1). This is abrk's rush landing on a different timing, not the fill. Not acted on; it is one cell, and the replica block decides. If u5x's block shows the same effect, the follow-up is a cap on how long gatherers defend.
- 14:38: u5z pre-registered = u4s + ATK_CARRY 25 (same-source copy of nul1 with that one constant changed). The target is starvation losses: per klose, 32 of 67 king-kill losses were starvations, with attackers holding 1.3-2k raw while the bank emptied. t0l and u2n screened +1.83 and +0.34 on older bases and never reached the replica. Falsifiers: (1) the screen delivers less than control (xn1, 299k); (2) the replica block's starvation losses are no fewer than u4s's. Screen xy5 is running; queued after u5x and u5y.
- 14:45: **u5x REJECTED at replica block 1** (submission 181, matches 9948-9963, paired with nul1's set-1 refs): 48 cells, 39 alike, D 9 (+2 -7), net -5, t -1.67, past block 1's t <= 0 reject bar. Losses: AdamTan12 rose A and uneasy_alliance B, HugoIngelsson clouds B, nzjt corridor A, spsquared bed A and superioristhmus A, thewax rose A. Gains: spsquared superioristhmus B, thewax clockwisdom A. Terrain from symmetry plus the king-gap elimination did not carry over from the local Stash/jail checks. Three symmetry-area attempts are now in a row (u5w, u5x, u5y pending); if u5y fails, the next attempt moves out of the symmetry area. u5y is submitted (182); block 1 is queued after match 9963. Disk: cleared 122 old local diag dumps from build/diag (regenerable; kept the test fixtures cf4, cf6, fos2, fos3; list in build/diag-deleted-1102.txt) plus scratch build/st*, cf*, jl*. Local free space went 1.4 to 2.4 GB; VM has 3.2 GB.
- 14:53: **u5z REJECTED on the local screen** (xy5 vs xn1): 44 cells, D 8 (+4 -4), net 0, 33 wins each. Delivered was 261k against the control's 299k (-13%), which hits pre-registered falsifier 1. Starved kings: 4. Capping attacker pickups at 25 cost deliveries without flipping any losses. Its third screen on a third base (t0l, u2n, u5z) is again neutral, so the switch is retired.
- 15:08: Starvation diagnosis of u5x's block (klose 9948-9963): 16 of 27 last-king deaths were starvations (median r720), 9 bites, 2 throws. The early ones (DefaultMedium vs Varun r282-304, uneasy_alliance vs AdamTan r331-355, thunderdome vs nzjt r141) share one path. By r51 our bank is 413-983 against their 1450-1550. We then lose the fight between r100 and r200, the bank cannot rebuild, and the king starves. Opening ledger (new scratch openspend.py, r <= 50, 48 games): we spawn 1339 to their 1042. On thunderdome we spawn 44-50 into a losing throw war. **Spend cap ruled out** (openwin.py over about 300 games: 9093-9168, 9948-9963, 9015-9075, 7506-7556, 7420-7491). Raw: in openings with a fight, win rate falls as spawn excess grows (<=0: 14/24; 1-5: 19/38; 6-12: 17/57; >12: 11/35). But stratified by our deaths by r50, with live-baby excess net of replacements, more bodies wins in every stratum: d10+ net >5 21/27 vs net 1-5 8/23; d0-3 18/37 vs 2/18; quiet 20/25 vs 32/50. The raw trend is the respawn confound, so a spend throttle would cut what wins. The lever is keeping bodies alive (fights), not spending less.
- 15:12: Grab census (scratch grabkind.py, 9948-9963, r <= 300; engine HEALTH_GRAB_THRESHOLD 0, so a healthier rat can grab, and so can any rat in the victim's blind spot): our grabs 2042 (1500 health, 542 blind) to the field's 1822 (1324, 498). Thunderdome vs nzjt is the exception: they out-grab us about 2:1, mostly on health. Not a general lever.
- 15:12: Basics repair: nul1's tiny overruns. Local nul1 on tiny vs abrk, araid and agrab: one overrun vs araid, rat 12455 at r38-42. The turn spent 6984 bytecodes in moveDecision on aimCarry (move reason T), which is gated only on 7000 left at entry, and ran over at the end-of-turn section (OVS 999). AIM_LEFT (stop the aim search with N left) existed but reached the replica only bundled with CAT_REACH in u5k (rejected, net -1). **u6a pre-registered** = u4s + AIM_LEFT 3000 alone, as a basics repair (accepts at t >= 0 after block 2). Local recheck: araid vs u6a on tiny, 0 overruns. Falsifiers: screen overruns not below the control's; throws per game down more than 10%. Screen xy6 running.
- 15:30: u6a screen xy6: identical to the control in every cell (33 wins each, D 0). Delivered 300205 vs 300185, throws 3261 vs 3260, overruns 0 vs 1 (control: tiny araid B). AIM_LEFT changes nothing except the overrun turn. Passes; it goes to the replica after u5y's block 1, which has 4 of 16 matches done.
- 15:47: **u5y REJECTED at replica block 1** (submission 182, matches 9976-10044, paired with nul1's set-1 refs): 48 cells, 40 alike, D 8 (+1 -7), net -6, t -2.12. Lost: spsquared bed A and B, fullofsecrets B, superioristhmus A; AdamTan12 Finals DefaultSmall B; Icarus uneasy_alliance B; Varun t_minus_five B. Gained: thewax clockwisdom A. Side symmetry by itself does not win: the bundle cost cells on both seats. That makes three rejects in a row in the symmetry area (u5w, u5x, u5y), so the area is closed for now. u6a (AIM_LEFT basics repair) is submitted (183); block 1 is queued after match 10044. VM disk 3.1 GB free.
- 16:42: u6a replica block 1 (submission 183, matches 10059-10190, paired with nul1's set-1 refs): 48 cells, **48 alike**, D 0, t 0. The repair is inert on the field panel; no overrun turn changed a game. A strict reading of block 1's bar (reject at t <= 0) would reject it. But §3 lets a basics repair accept at t >= 0 after block 2, and that rule only has a purpose if a tie survives block 1. So block 1 counts as a pass for a repair, and block 2 (v4 set 2, vs nul1's set-2 refs 9015-9075) is queued after match 10190. This reading is logged here before block 2 is played.
- 16:55: rose starvation (9961 g1 vs AdamTan12_Icarus, a set-1 loss for u5x; king starved r890 with 27 babies). hoardwhy: from r600 about 8 carriers stood 5-8 tiles from the king for 200+ rounds, tag d (delivering), loads 20-240. Local nul1 as B on rose (vs araid and vs abrk; our side plays the same game in both): delivered 4065 in 2000 rounds, with 6-12 carriers on d every 100 rounds. Map at r700: the king sits at (45,2) in a walled pocket whose only exit is the diagonal gap (49,2)-(48,1). Outbound gatherers bound for the one mine (57,7), on M with stalls of 24-59, meet the inbound carriers head-on there. The carriers' flow paths are ready, 6-11 steps. This is the same class as s6m and s7j-s7y on 10-29. Probe pb3 (scratch, not committed) = nul1 + s7x's swap (SWAP_HOLD 2 + SWAP_YIELD + SWAP_NO_ALONG): delivered 4450 (+9%); cheese held on d drops to 120-240 from 340-650. Only a small gain: rose's economy is bound by one far mine as much as by the jam. With u1s's broad screen loss (net -10) on record, a narrow-gated swap is not worth a slot. Dropped.

## 2026-11-02 17:43 PST: u6a accepted (AIM_LEFT 3000, basics repair); the new incumbent

- 17:43: **u6a ACCEPTED** (u4s + AIM_LEFT 3000) under the basics-repair rule (accept at pooled t >= 0 after block 2). Block 2 (submission 183, v4 set 2, matches 10191-10246, paired with nul1's set-2 refs 9015-9075): 48 cells, 48 alike, D 0. Pooled over blocks 1 and 2: 96 cells, all alike, t 0. The repair changes no field game; it removes the aimCarry overrun (local screen xy6: overruns 0 vs nul1's 1, cells and throws otherwise identical). It is a bytecode-safety floor, not a strength gain, and the progress charts should be read that way.
  - Reference bookkeeping: u6a's set-1 and set-2 games are identical to nul1's, so nul1's refs (set 1: 9093-9168, set 2: 9015-9075) stay the paired references. From now on candidates are same-source copies of **bots/u6a** (copy, rewrite the package line, change one Cfg constant).
  - Since u4s: 13 candidates (u4t-u6a) went through screens or blocks; this is the first acceptance.
- 17:53: **Grab-war diagnosis** (field, our set-1 refs 9093-9168 + 9948-9963, r <= 300). Scratch grabvictim.py: enemy grabs of our babies 3520 (2244 on health, 1276 blind) to our 3937. The victim's smuggled tag before the grab is most often f/a (fight, attack) or T/TF (carrying a prisoner and aiming, 692). Scratch holdend.py: of 3976 holds of an enemy, 2893 ended in our throw, 891 with our carrier grabbed (its prisoner freed), 127 with our carrier killed, 65 dropped. By hold length: no throw is possible in rounds 0-1, because the grab's cooldown leaves the action not ready until round 2. Yet 583 carriers were grabbed in those two rounds (219 at 0, 364 at 1). A shorter THROW_HOLD cannot help that window; only not grabbing into a counter-grab can.
- 17:53: **u6b pre-registered** = u6a + GRAB_SAFE (new switch, ratbot Baby.tryCarry + grabRisk, BotTest testGrabSafe with 9 checks; mutations of each condition fail a check; the redundant target exclusion was dropped because a target that sees us with more health cannot be grabbed anyway). Rule: no grab while another enemy baby adjacent to us, carrying nobody, has us in its vision cone and strictly more health (HEALTH_GRAB_THRESHOLD 0), since it can grab our carrier before the throw is ready. Blind-side grabbers cannot be seen, so they are not gated. Falsifiers: (1) the screen rejects (t <= -1.5) or wins fall below the control's minus 3; (2) on the replica block, carriers grabbed in hold rounds 0-1, per hold, are not at least 25% below nul1's set-1 refs (583 of 3976 = 14.7%). Screen xy7 running.
- 18:12: u6b screen xy7 vs xn1: 44 cells, D 9 (+6 -3), net +3; wins 36 vs 33 (games 40 of 48), delivered 280k vs 299k (-6%), starved kings 0, overruns 0. Gained: abrk canyoudig B and tiny B, araid DefaultMedium A, cheesefarm A and B, starvation B. Lost: abrk bed B, araid DefaultSmall B and canyoudig A. Falsifier 1 holds. Submitted (184); block 1 (v4 set 1, paired with nul1's set-1 refs) is queued after match 10246. Disk: VM 2.8 GB, local 1.8 GB free.
- 18:28: **u6b REJECTED at replica block 1** (submission 184, matches 10297-10312, paired with nul1's set-1 refs): 48 cells, 44 alike, D 4 (+1 -3), net -2, t -1.00. Gained AdamTan12 Finals tiny A; lost Finals DefaultSmall B, Icarus uneasy_alliance B, spsquared bed A. **Falsifier 2 also fails:** carriers grabbed in hold rounds 0-1 were 276 of 1938 holds (14.2%), against nul1's 14.7%, where the bar was a 25% cut. The gate barely fires where it matters. The counter-grabs come from enemies we cannot see (the blind side) or that step beside the carrier after the grab, not from a healthier neighbour already in view. The grab-war lever stays open, but this form is refuted. The local screen gain (net +3) did not carry to the field.
- 18:38: **Counter-grab geometry** (scratch countergrab.py, set-1 refs + 9948-9963, r <= 300; 583 of our carriers grabbed within a round of their own grab). The grabber is never the prisoner (0 of 583). On our grab round it stood within Chebyshev 2 in 556 cases (cheb 0: 211, i.e. it stepped into the tile our carrier left; cheb 1: 118; cheb 2: 215) and inside our carrier's vision cone in every case. It had more health in 325. Our carrier had moved in 345 (aimCarry lines up the throw by stepping) and was followed. So u6b's gate (adjacent and already facing us) checked the wrong set.
- 18:38: **u6c pre-registered** = u6b + GRAB_SAFE_R 2 (new int switch, ratbot Baby.grabRisk with a radius; 5 BotTest checks; every mutation fails a check). The gate counts any healthier enemy baby within Chebyshev 2, whatever its facing, and after a move it also checks the turn's first sensing, because the post-move call passes only adjacent robots. Second attempt in the grab area. Falsifiers: (1) screen rejects (t <= -1.5) or wins below the control's minus 3; (2) on the replica, carriers grabbed in hold rounds 0-1 per hold not 25% below nul1's 14.7%. Screen xy8 running.
- 18:58: u6c screen xy8 vs xn1: 44 cells, D 13 (+8 -5), net +3; wins 36 vs 33 (games 40 of 48), delivered 298k vs 299k, starved kings 4, overruns 0. Gained: abrk arrows B, canyoudig B, tiny B; araid canyoudig B, cheesefarm A and B, starvation A, tiny B. Lost: abrk DefaultSmall B, arrows A, cheesefarm A; araid bed A and B. Falsifier 1 holds. Submitted (185); block 1 (v4 set 1, paired with nul1's set-1 refs) is queued after match 10312.
- 19:27: u6c REJECTED at replica block 1 (submission 185, matches 10363-10399): 48 cells, 42 alike, D 6 (+1 −5), net −4, t −1.63 (rule: block 1 rejects at t ≤ 0). Gained Icarus rose B; lost Finals tiny B, Xzibit attack_on_cat B, YokoOno clouds B, spsquared bed B, superioristhmus A. Falsifier 2 PASSED: carriers grabbed in hold rounds 0-1 fell to 9.1% of holds (141/1554) from 14.7% (583/3976), so the gate does what it says. The skipped grabs cost more than the saved carriers. That is the grab area's second reject (u6b, u6c). The next attempt leaves the grab area.
- 19:48: **Where the in-band losses bleed** (scratch deathcause.py and traptag.py over u6a/nul1's 192 replica games: 9015-9075, 9093-9168, 10059-10246). In-band opponents: YokoOno 8/24, Xzibit 8/24, Finals 10/24, run_away10 10/24, Merlin 12/24. Our last king starved in 48 of 84 losses, was bitten in 26 and thrown in 8. Baby deaths in losses: thrown 21.6 ours vs 22.1 theirs (even), cats roughly even, but **enemy rat traps 11.5 of ours a game vs 0 of theirs** (wins: 4.6), and bites 6.2 vs 1.5. Smuggled tags on each trap victim's last turn: almost every fire came with an enemy rat within dist2 20, where Baby drops the TrapRisk mask, and about 60% of victims were non-fighters (tag G:a, attackers on their way, the largest share at 198 of 1058; then G:f, G:T, G:w, G:r, G:M). RISK_GATHER (gatherers only) was never scored on the field (s5u and s8g stopped at weak screens, 2026-10-29). RISK_IN_FIGHT froze fighters (s8h), so fighters stay out of it.
- 19:48: **u6d pre-registered = u6a + RISK_NONFIGHT** (new switch; ratbot Baby.keepRisk, 10 BotTest checks, all 4 mutations caught). Gatherers and attackers keep the trap-risk mask while engaged; fighters still drop it. This is the trap area (last tried u5u, 10:55), not grab. Not structural: the next attempt must be, since the last structural one was u5s (08:14). Falsifiers: (1) screen rejects (t ≤ −1.5) or wins below the control's minus 3; (2) on the replica our trap deaths per game do not fall 25% below u6a's 7.9 (to ≤ 5.9). Screen xy9 is running.
- 20:07: **u6d REJECTED on the local screen** (xy9 vs xn1): 44 cells, D 16 (+6 −10), net −4; wins 29 vs 33, below the control's minus 3 (falsifier 1). Delivered 272k vs 299k, starved kings 4, overruns 0. Gained: araid DefaultMedium B, canyoudig B, cheesefarm B, starvation A and B, tiny B. Lost: abrk DefaultSmall A, bed B, tiny A; araid DefaultSmall A and B, arrows A and B, bed B, evileye A, learn_your_lesson B. Seven of the ten losses are against araid, the raider, and they come shorter (arrows A r1075 → r572). Gatherers that keep the mask near raiders give up ground and cheese. The archetypes lay no rat traps, so the screen sees the cost and not the benefit. The field benefit is untested, but the price is too high for one change. The trap area now has u5u and u6d rejected.
- 20:12: **King damage both ways** (scratch kingdmg2.py, the same 192 games): in losses their rats bite our kings for 222 a game and throw our own rats into them for 97, while we do 12 and 45 to theirs. In wins we do 45 by bites and 106 by throws, and take 28 and 11. Our throws already weight kings 3x (tryThrow). The levers for king safety (KING_FRONT, GUARD_KING_R, DEFEND, KING_DODGE, thunderdome's AMMO_AWAY) were all tried. **Starvation run-ups** (starvepath.py, u6a set 2, 10191-10246): the opponents bank 1000-1500 through the mid-game, and we hover at the 300 floor (or the 100 LOW_FLOOR once under 8 babies). When the army collapses, the bank runs out in 50-150 rounds (canyoudig vs Xzibit r285-305, clouds vs run_away10 r499, ratsareants r628, mercifullattice vs Icarus r906). Field dose-response: u2v (RICH_BANK 500, more spending) lost net −5 on the replica. No reserve raise has ever reached the field; u3h's RICH_BANK 1500 stopped at a local −1, and the archetypes don't punish a thin bank. Also seen: clockwisdom vs run_away10 (10198 g0) is still the old maze hoard (two rats at 2200 raw each, 21x slowed by the 1%-per-unit cooldown; u4o/TRAIL was rejected).
- 20:12: **u6e pre-registered (structural: bank reserve doctrine) = u6a + RESERVE_ROUNDS 300** (new switch; ratbot King.reserve, 8 BotTest checks). Every spawn floor (core, war, spawn, and the LOW_FLOOR collapse) holds at least our live kings' upkeep for 300 more rounds, capped at the game's end: 600 for one king, 1200 for two. Same-source copy of u6a; King.java gains only reserve(). Falsifiers: (1) screen rejects (t ≤ −1.5) or wins below the control's minus 3; (2) on the replica, starvation still ends 40%+ of the block's losses (u6a: 48 of 84, 57%) and the median starvation round is not later than u6a's r839. Screen xz1 is running.
- 20:31: **u6e REJECTED on the local screen** (xz1 vs xn1): 44 cells, D 13 (+1 −12), net −11; wins 22 vs 33 (falsifier 1). Delivered 277k vs 299k, starved kings 1 (control 4), overruns 0. The only gain was araid DefaultMedium B. Losses: abrk DefaultSmall A and B, bed B, evileye A and B; araid DefaultSmall A and B, arrows B, bed A, evileye A and B, learn_your_lesson B. Holding 600 instead of 300 costs about two bodies through the opening and mid-game, and those fights are lost (arrows B r803 → r326). It does cut starvation (1 vs 4), but the army is worth more than the reserve. Read with u2v (RICH_BANK 500, field −5): the spawn level is near a local optimum both ways. A reserve that bites only once the army has collapsed is a different hypothesis.
- 20:34: **u6f pre-registered (basics repair, navigation) = u6a + FLOW_UNSEEN 3** (new switch; ratbot Flow.unseen). In Flow's target searches (forward, reverse, reverse-fast and the weighted variant) a never-seen tile costs 3 instead of 1 (dirt stays 4, under the Dial bucket bound). Plans then keep to walked corridors instead of guessing through unseen space; the frontier search is unchanged. Evidence: clockwisdom vs run_away10 (10198 g0). Two carriers on deliver/gather held 2200 raw each from r600 to r950, slowed 21x by the 1%-per-unit cooldown, and walked away from the king (4,2): 11777 went from (14,23) to (25,39). This is the old maze class: the 2026-10-31 notes say flow searches fail there because unknown counts as open. The far delivery stalls in the u3z census were TheHeist, fullofsecrets, peaceinourtime, averystrangespace, clockwisdom and Hike. TRAIL (u4o) went after the same class with a different tool and was rejected. Test: testFlowUnseen (8 checks over rev x fast; mutating each live cost line fails a check, and the FLOW_W > 1 variant is off). Falsifiers: (1) screen rejects (t ≤ −1.5) or wins below the control's minus 3; (2) a maze diag (clockwisdom, TheHeist, fullofsecrets, peaceinourtime vs araid, both seats) delivers no more than u6a. Screen xz2 is running (it has no maze maps; the diag follows it).
- 20:53: u6f screen xz2 vs xn1: 44 cells, D 17 (+9 −8), net +1; wins 34 vs 33 (games 38 of 48). Delivered 269k vs 299k (−10%), starved kings 1, overruns 0. Gained: abrk arrows B, canyoudig B, tiny B; araid DefaultMedium B, canyoudig B, cheesefarm A and B, starvation A and B. Lost: abrk bed A, cheesefarm A, tiny A; araid DefaultSmall B, evileye A, learn_your_lesson A and B, tiny A. Falsifier 1 holds. learn_your_lesson (mines in walled rooms) lost in both seats: watch it. Maze diag xz3 (16 games, u6f and u6a vs araid on clockwisdom, TheHeist, fullofsecrets, peaceinourtime, both seats) is running.
- 20:59: **u6f REJECTED at the maze diag** (xz3, 16 games vs araid, both seats): it delivered 6405 to u6a's 9405, which hits falsifier 2 ("delivers no more than u6a"); wins 3 vs 4. Per cell (u6f / u6a): clockwisdom A 520 W / 0 W, B 0 L / 0 W; TheHeist A 880 L / 840 L, B 925 L / 4980 W; fullofsecrets A 700 L / 905 L, B 210 L / 350 L; peaceinourtime A 1630 W / 1100 L, B 1540 W / 1230 W. Without TheHeist B, u6f is ahead (5525 vs 4425), so the effect is mixed, but the pre-registered bar decides. The screen gain (net +1) came with −10% delivered and both learn_your_lesson cells lost. The maze/delivery navigation class stays open (u4o TRAIL and u6f rejected).
- 21:05: **u6g pre-registered = u6a + s8o's rescue** (RESCUE, RESCUE_D2 18, RESCUE_WALK; existing switches, one recipe, testRescue in BotTest). An enemy baby carrying one of ours within dist2 18 comes first: step or walk to a tile beside it that it cannot see and grab it; a grabbed carrier drops what it holds. s8o screened +6.0 points (t +1.42) on s7d on 2026-10-29 and never went to the field. Field now (192 games): thrown rats kill 27.2 of ours a lost game (throwLand 21.6, throwHit 5.6) and 9.5 a won one, about 18.0 a game, and their throws of our own rats hit our king for 97 a lost game. Grab area: its third attempt after u6b and u6c; a reject closes it. Falsifiers: (1) screen rejects (t ≤ −1.5), wins below the control's minus 3, or overruns above the control's 1 by more than 2; (2) on the replica, our thrown-rat deaths per game do not fall 10% below u6a's 18.0 (to ≤ 16.2). Screen xz4 is running.
- 21:16: u6g screen xz4 vs xn1: 44 cells, D 13 (+8 −5), net +3; wins 36 vs 33 (games 39 of 48). Delivered 256k vs 299k, starved kings 4, overruns 0. Gained: abrk arrows B, canyoudig B, tiny B; araid DefaultMedium A, canyoudig B, cheesefarm A, starvation B, tiny B. Lost: abrk bed A and B; araid arrows B, learn_your_lesson B, wallsofparadis A. Falsifier 1 holds. **Submitted** (186). Block 1 (v4 set 1, paired with nul1's set-1 refs) is queued after match 10399.
- 21:32: **u6g REJECTED at replica block 1** (submission 186, matches 10567-10582) vs nul1's set-1 refs: 48 cells, 41 alike, D 7 (+3 −4), net −1, t −0.38 (block 1 rejects at t ≤ 0). Gained: Merlin thunderdome A and B (the throwing-war map, lost in every earlier same-source run), Finals tiny A. Lost: Finals DefaultSmall B, Xzibit attack_on_cat A, YokoOno clouds B, spsquared bed A. Thrown-rat deaths of ours: 20.8 a lost game and 12.6 a won one, about 16.7 a game against u6a's 18.0. That misses falsifier 2's 16.2 narrowly. Rescue does what it says where throws decide the game (thunderdome flips both seats), but it costs elsewhere. With u6b and u6c, this is the grab area's third reject, so the area is closed. A rescue limited to map geometry like thunderdome's (kings 4 apart) is a narrower follow-up, but that waits until another area has had its turn.
- 21:39: **Disk:** local root fell to 1.1 GB free. I deleted 128 build/dumpcache entries untouched for 6+ hours (about 920 MB; they are replay dumps that regenerate from the replays on demand). The list is in build/dumpcache_deleted_2026-11-02.txt. Now 2.0 GB free. The replays themselves are untouched.
- 21:39: **Truce timing** (summary.json backstab fields, u6a/nul1's 192 replica games): with the war open before r100 we won 56 of 132 (42%), and 44 of 60 otherwise (73%). We opened 38 of the early wars (won 18) and they opened 94 (won 38). **u6h pre-registered (structural, doctrine) = u6a + PEACE** (existing switch: we never end cooperation; while it lasts no bite or grab on an enemy rat or king). It is u3z's near-miss (pooled t +1.46 at block 3, under the 1.5 bar, 2026-10-31), retried alone; u4l bundled it with MINE_ANY and MINE_TIES. Falsifiers: (1) screen rejects (t ≤ −1.5) or wins below the control's minus 3; (2) on the replica, wars we open before r100 stay above 5 of the block's games (u6a: 38 of 192, ~10 a block). Screen xz5 is running.
- 21:53: u6h screen xz5 vs xn1: 44 cells, D 9 (+4 −5), net −1; wins 32 vs 33 (games 34 of 48). Delivered 277k vs 299k, starved kings 3, overruns 0. Gained: abrk canyoudig B; araid canyoudig B, cheesefarm A, starvation A. Lost: abrk DefaultSmall A; araid arrows A and B, evileye B, tiny A. Falsifier 1 holds; the archetypes break the truce themselves, so the screen can barely see PEACE. **Submitted** (187). Block 1 (v4 set 1, paired with nul1's set-1 refs) is queued after match 10582.
- 22:33: u6h (PEACE) replica block 1 (sub 187, matches 10624-10674): 48 paired cells, 41 alike, D 7 (+4 −3), net +1, t +0.38 → continues to block 2. Gained Finals tiny A, Icarus rose B, Icarus uneasy_alliance A, collegejunior clockwisdom A; lost spsquared fullofsecrets B, collegejunior clockwisdom B, collegejunior rose A. Falsifier 2 holds: we opened 0 truce breaks (bar ≤ 5; the u6a/nul1 field had 38 of 132). Opponents broke early in 34 of 48 games (we won 16), broke late in 6 (we won 4), and never broke in 8 (we won 5). Queuing block 2 on set 2.
- 22:42: u6i pre-registered (combat; the area is open: u6d's trap reject was a different mechanism): u6a + KING_PRESS + PRESS_SHARED with s8a's reach (PRESS_D2 225, PRESS_AGE 16, PRESS_HOPS 3). Rationale: in lost field games they bite our king for 222 and we bite theirs for 12. s8a ran this recipe on s7d (block 1 t −0.45) but it has never run on the u-series base. Falsifiers: (1) screen xz6 wins < 31 of 44 or D-net < −2 rejects; (2) if it reaches the replica, the enemy-king bite damage per game must rise from 12/45 (L/W) to ≥ 30/70, otherwise the mechanism did not fire. Screen xz6 is on the VM; it waits on u6h's block 2 before it can go to the replica.
- 22:50: u6i screen xz6 passed falsifier 1. It differs from xn1 in 12 cells (+7 −5) and wins 35 of 44 cells vs 33. Funnel: 38/48 wins vs 37, our overruns 0 (the 22 in the run are araid/abrk's). It gains araid cheesefarm A+B and starvation A+B, where the control lost on points or late, and it kills the araid king by r584-1137. It loses abrk DefaultSmall/bed/evileye and araid DefaultSmall/arrows. Cost to watch: delivered cheese 229k vs 300k (−24%), because pressing rats stop gathering. If falsifier (2) misses on the replica, this cost is the likely cause. u6i is next in line for the replica after u6h's block 2.
- 23:03: Diagnosis of the starved losses in u6h block 1 (starvepath, hoardwhy). In 4 of 11 losses where our last king starved, our babies carried ≥1500 raw cheese while delivering 0 per 50 rounds. Example: 10670 g0 clockwisdom, from r505 to the king's death at r905, 4000-5900 cheese sat in our paws. The carriers are ATTACK-role rats (tag 'a') holding 940-2240 each at Chebyshev 10-44 from the king. Each carried unit adds 1% to cooldown, so they crawl about 2 tiles per 100 rounds. u6j pre-registered (economy/basics repair: frozen carriers): u6a + ATK_DELIVER 200. u4q's 50 interrupted fights too often (block 1 t −0.45); 200 means 3× cooldown and catches only the crawlers. Falsifiers: (1) screen xz7 wins < 31 or D-net < −2; (2) on the replica, starved losses with ≥1500 carried and 0 xfer must fall from 4 to ≤ 1 per block.
- 23:10: u6j screen xz7 passed falsifier 1: D 2 (+2 −0: araid DefaultMedium B, canyoudig B), 35 vs 33 wins, delivered 301,667 vs 298,702, overruns 0. A quiet screen was expected: the archetypes rarely leave our attackers holding 200+. Queue for the replica: u6h block 2 first, then u6i, then u6j.
- 23:36: u6h REJECTED at block 2. Set 2 (10686-10689, 10719-10722, 10756-10763) vs nul1's set-2 refs 9015-9075: 48 cells, 39 alike, D 9 (+5: Finals attack_on_cat B, YokoOno Hike A+B, YokoOno streetsofnewyork B, run_away10 clockwisdom B; −4: Finals attack_on_cat A, YokoOno averystrangespace B, Merlin fullofsecrets A+B), net +1, t +0.33. Pooled over blocks 1+2: D 16, net +2, t +0.50, at or under the block-2 bar of 1. PEACE removed our early truce breaks (0 vs 38/132) but the field breaks early anyway (34 of 48 block-1 games), so most of the gain it could buy never materialised. Rejects since the last accept: u6b-u6h makes 7, one short of the plateau trigger. Submitted u6i (sub 188); block 1 queued on set 1 after 10763, paired against refs 9093-9168.
- 00:32: u6i REJECTED at block 1. Set 1 (10780-10931, 15 matches) vs refs 9093-9168: 45 cells, 38 alike, D 7 (+2: Finals tiny A, Icarus rose B; −5: Finals DefaultSmall B, Icarus uneasy_alliance B, YokoOno clouds A+B, collegejunior clockwisdom B), net −3, t −1.13. Falsifier 2 missed: our bites on their king were 26/42 per lost/won game (bars 30/70; base 12/45), while their bites on ours stayed at 223 in losses. The press reaches their king a little more often, but it does not stop the damage to ours, and pulling rats off gathering costs us. Combat (king press) area: 1 reject. This is reject 8 in a row since the last acceptance, so the plateau trigger fires. u6j (already screened) goes to the replica now as sub 189, block 1 after 10931. The next new attempt must be a structural big swing.
- 00:41: Plateau procedure, step 1 (correctness audit, lens "what replays show units doing"): stuck.py over u6i block 1 (45 games). Baby turns motionless for 30+ rounds: 2.5% in wins, 0.5% in losses, mostly tag 'M' (waiting at a mine). Carrying cases: 538 rat-turns with 200+ cheese, already u6j's target. No new basics bug. Step 2 (ablate everything carried): eight switches that are on in u6a and have never been ablated, each turned off alone: u6k NAP, u6l FACE_ENEMY, u6m FIGHT_MINE, u6n CAT_TRAPS, u6o RUSH_BREAK, u6p COURIER, u6q GUARD_MEMORY, u6r HUNT_IGNORES_RISK. They run as screens zb1-zb8, sequentially on the VM (the run name ab1 was already taken). A switch whose removal scores at or above u6a on the screen (wins ≥ 33, D-net ≥ 0) goes to the replica as a simplification.
- 01:17: u6j REJECTED at block 1. Set 1 (10932-10947, 16 matches) vs refs 9093-9168: 48 cells, 44 alike, D 4 (+1: Icarus rose B; −3: Icarus TheHeist A, Merlin peaceinourtime A, collegejunior clockwisdom B), net −2, t −1.00. Falsifier 2 also missed: 3 starved losses still held ≥1500 carried with 0 xfer (bar ≤ 1). In 10942 g0 clockwisdom our rats held up to 10,760. The carriers were now tagged 'd' (delivering: the switch fired) but they still crawled. The real mechanism is a pickup feedback loop, not attackers. 10942 g0 is a truce game (3 bites all game). Rat 10004 made 68 pickups of 20 cheese while walking home through clockwisdom's cheese field and reached 1460-1840; each unit slows it 1%, so it arrives later and passes more piles. Reject 9 in a row since the last acceptance.
- 01:17: u6s pre-registered (basics repair, economy: the carrier feedback loop): u6a + CARRY_MAX 100. A rat that already carries takes no pile that lifts it above 100 (at most 2× cooldown); empty-handed pickup is unchanged. CARRY_MAX was tested only in self-play (s3l, 120, neutral), where this situation never arose. Falsifiers: (1) screen zc1 wins < 31 or delivered < 285k (−5% vs 298,702); (2) on the replica, starved losses with ≥1500 carried and 0 xfer ≤ 1 per block. As a basics repair it accepts at t ≥ 0 after block 2. Screen zc1 is chained after the ablations zb1-zb8 on the VM.
- 02:44: VM root disk was down to 1.9 GB (below the 2 GB floor). I pruned 434 replica replays older than 2 days (537 MB), listed in /root/bc26-replays-pruned-2026-11-03.txt on the VM; it now has 2.5 GB free. Our local replay copies are untouched.
- 02:44: Ablation screens zb1-zb7, each vs xn1 (nul1: 33 wins, delivered 298,702). Overruns 0 in all.
  - u6k −NAP: D 21 (+4 −17), 20 wins. NAP is essential; keep it.
  - u6l −FACE_ENEMY: D 16 (+8 −8), 33 wins, delivered 272k. Keep it.
  - u6m −FIGHT_MINE: +3 −4, 32 wins, 295k. Neutral.
  - u6n −CAT_TRAPS: +2 −3, 32 wins, 306k. Neutral.
  - u6o −RUSH_BREAK: +4 −4, 33 wins, 307k. Neutral.
  - u6p −COURIER: +5 −4, 34 wins, 317k (+6%). Replica candidate: simpler and delivers more.
  - u6q −GUARD_MEMORY: D 14 (+9 −5), 37 wins, delivered 271k (−9%). Replica candidate: the best screen of the set.
  - zb8 (−HUNT_IGNORES_RISK) is still running. The waiter timed out at 2 h because the VM's load is about 12.
  Replica order: u6s (basics repair) once its screen zc1 passes, then u6q, then u6p.
- 02:47: Submitted u6q (−GUARD_MEMORY) to the replica as sub 190 while u6s's screen waits for the VM; block 1 is queued on set 1 after 10947. Falsifier (pre-registered): the replica block must not lose more than 3 cells where we are in-band (the 25-75% band). u6s goes next once its screen passes.
- 02:53: Ablation zb8, u6r −HUNT_IGNORES_RISK: D 0 (all 44 cells alike), 33 wins, delivered 300k. The switch is inert against the archetypes, so the screen gives no signal; I'm not spending a replica block on it. u6s's screen zc1 has started (3 games in).
- 02:58: u6q REJECTED at block 1. Set 1 (11067-11082) vs refs 9093-9168: 48 cells, 42 alike, D 6 (+2: Icarus rose B, spsquared superioristhmus B; −4: Finals DefaultSmall B, Xzibit attack_on_cat B, YokoOno clouds B, spsquared bed B), net −2, t −0.82. Its 37-of-44 screen did not carry to the field. Reject 10 in a row. Submitted u6p (−COURIER) as sub 191 while u6s's screen zc1 runs; block 1 after 11082.
- 03:01: Local disk was at 1.6 GB free. Deleted 53 build/dumpcache entries older than 6 h (348M, regenerable replay parses; list in build/dumpcache_deleted_2026-11-03.txt). Local replays are untouched.
- 03:07: u6s REJECTED at screen zc1 (falsifier 1, delivery). D 6 (+3: araid DefaultMedium B, cheesefarm A, starvation A; −3: abrk cheesefarm A, araid arrows A, learn_your_lesson B), 33 vs 33 wins (bar 31: pass), but delivered 264,495 vs 298,702 (−11%, bar 285k: fail). A 100 cap also stops productive loads. u6t pre-registered as the same basics repair with the cap set above normal hauling: u6a + CARRY_MAX 300. The runaway loads were 940-10,760. Falsifiers unchanged: screen zc2 wins ≥ 31 and delivered ≥ 285k; on the replica, at most 1 starved loss per block with ≥1500 carried and 0 xfer. Screen zc2 is running on the VM.
- 03:25: u6t screen zc2 passed falsifier 1: D 1 (+1: araid DefaultMedium B), 34 vs 33 wins, delivered 297,032 (bar 285k), overruns 0. The 300 cap leaves normal hauling alone, as intended. It goes to the replica next, after u6p's block 1; as a basics repair it accepts at t ≥ 0 after block 2.
- 03:54: u6p REJECTED at block 1. Set 1 (11092-11177) vs refs 9093-9168: 48 cells, 40 alike, D 8 (+2: Finals DefaultSmall A, tiny A; −6: Finals DefaultSmall B, Xzibit attack_on_cat B, run_away10 t_minus_five_minutes A, Merlin peaceinourtime A, spsquared bed B, collegejunior rose A), net −4, t −1.41. COURIER stays. Reject 12 in a row (u6b-u6s, counting the u6s screen reject). Submitted u6t (CARRY_MAX 300) as sub 192; block 1 after 11177.
- 04:00: Plateau step 3 (re-watch field games). I pooled every u6a-lineage replica game (nul1/u6a refs plus the u6h-u6t blocks; the bots play near-identically) per opponent×map×side; build/cellrates.txt.
  - Opponent rates: Icarus 73%, spsquared 71%, run_away10 54%, collegejunior 50%, Merlin 42%, Finals 40%, YokoOno 31%, Xzibit 29%.
  - 18 in-band cells are lost every time (n ≥ 5). Eight opponent-map pairs are 0/12 on BOTH sides: Finals jail, Xzibit learn_your_lesson, Xzibit starvation, YokoOno Stash, YokoOno arrows, run_away10 DefaultMedium, Merlin thunderdome, collegejunior dirtpassageway.
  - These deterministic losses are where the next structural attempt should aim; another 18 in-band cells are coin flips. Next: how our king dies in each.
- 04:13: How our last king dies in the eight 0/12 pairs (klose over the pooled games; build/syslost.txt):
  - Stash vs YokoOno: starved 11/12, median r508. A second king forms in only 4 of the 12, so that is not the general cause.
  - learn_your_lesson vs Xzibit: starved 12/12, median r991.
  - starvation vs Xzibit: bitten 8, starved 4.
  - DefaultMedium vs run_away10: bitten 6, starved 6, median r245.
  - thunderdome vs Merlin: thrown rats 8/10, median r139.
  - jail vs Finals: bitten 12/12. arrows vs YokoOno: bitten 12/12.
  - dirtpassageway vs collegejunior: mixed.
  learn_your_lesson is a death spiral (11076 g1). From r667 our babies fall 7 → 0 while Xzibit holds 20, and 7.7k-12.3k cheese lies unpicked on the map. Our bank sits at 26-90, under LOW_FLOOR 100, while a spawn costs 10-20 under 8 babies. So the king never respawns a gatherer and starves next to a full map.
- 04:13: u6u pre-registered (basics repair, economy: low-army spawn lock): u6a + LOW_FLOOR 30. LOW_FLOOR has never been tuned. Falsifiers:
  (1) screen zc3 wins ≥ 31 and starved kings ≤ 3 (control 3);
  (2) on the replica, in learn_your_lesson/Stash losses our babies must not reach 0 while the bank is ≥ 30, and learn_your_lesson vs Xzibit must win at least 1 game of 2-4 within the blocks.
  It accepts at t ≥ 0 after block 2. Screen zc3 is on the VM; it goes to the replica after u6t.
- 04:30: u6u REJECTED at screen zc3 (falsifier 1, starvation): D 3 (+2: abrk arrows B, canyoudig B; −1: araid arrows A), 34 vs 33 wins (pass), but starved kings 6 vs 3 (bar ≤ 3: fail). Delivered 298k. A 30 floor lets the king spend itself to death elsewhere. The spawn lock in the learn_your_lesson spiral is real, but a flat floor cut is the wrong fix; a spawn that pays back (gatherer, rich map, a mine in reach) would need a new switch. Reject 13.
- 04:48: u6v pre-registered (STRUCTURAL, combat/king safety; new switch). At war, King.move() returned on any enemy in view, so the king stood at its mine while enemy rats bit it (222 a lost game). jail vs Finals and arrows vs YokoOno are 0/12, every king death by bites. Defensive baby tweaks (BITE_GUARD, GUARD_KING_R, GUARD_CARRY, AMMO_AWAY, KING_TRAPS) were all rejected; the king itself never moved.
  - New: KING_FLEE (dist2, 0 = off). With more enemy rats than our babies within it, the king steps from the enemies' centroid toward our babies' (away from the enemies when none of ours is near); enemy kings don't count. King.flee is a pure helper, with 9 BotTest checks, including mirrored inputs giving mirrored steps; 4/4 mutants caught; BotTest 1939/1939.
  - u6v = u6a + ratbot King.java (RESERVE_ROUNDS off) + KING_FLEE 20.
  - Falsifiers: (1) screen zc4 wins ≥ 31, overruns 0; (2) on the replica, enemy bite damage on our king per lost game must fall below 180 (from 222), otherwise the mechanism did not help.
  Screen zc4 is on the VM.
- 04:54: u6v screen zc4 passed falsifier 1: D 1 (+1: abrk canyoudig B), 34 vs 33 wins, delivered 299k, overruns 0. KING_FLEE rarely fires against the archetypes (our babies outnumber their rushers at our king), so the replica is the real test. It goes after u6t's block. Full unit suite: exit 0, 0 FAIL (first full run since King.reserve, Flow.unseen and King.flee).
- 04:56: u6t REJECTED at block 1. Set 1 (11184-11264) vs refs 9093-9168: 48 cells, 45 alike, D 3 (+1: spsquared superioristhmus B; −2: Merlin peaceinourtime A, collegejunior clockwisdom B), net −1, t −0.58. Falsifier 2 passed: 1 starved loss of 12 held ≥1500 carried with 0 xfer (bar ≤ 1, u6h/u6j had 3-4), and the clockwisdom peak load fell from 10,760 to 1,660. But clockwisdom is still lost to starvation, so the frozen carriers were a symptom, not the deciding cause. Reject 14. Submitted u6v (KING_FLEE 20) as sub 193; block 1 after 11264.
- 05:01: Local disk at 1.4 GB free. Deleted 48 build/dumpcache entries older than 2 h (372M, regenerable replay parses; list in build/dumpcache_deleted_2026-11-03b.txt). Replays untouched.
- 05:53: u6v REJECTED at block 1. Set 1 (11268-11352) vs refs 9093-9168: 48 cells, 47 alike, D 1 (−1: Finals DefaultSmall B), net −1, t −1.00. Falsifier 2 failed: enemy bites on our king were 262 per lost game (bar < 180, base 222). With an enemy rat within dist2 20 our king moved on 405 turns and stood on 4,677. The trigger (more enemy rats than our babies within 20) seldom fires because our babies crowd the king too, and when it does the king does not escape. King-safety area: u6v plus the older guard/ammo rejects. Reject 15 in a row.
- 05:54: u6w pre-registered (plateau step 5, near-misses in pairs): u6a + PEACE + CARRY_MAX 300. u6h (PEACE) came closest (pooled t +0.50 over two blocks); u6t's cap fixed its mechanism without moving the score. Hypothesis: PEACE keeps more truce games, and those are the cheese-rich games where loads run away, so the pair may pay where neither did alone. Falsifiers: (1) screen zc5 wins ≥ 31 and delivered ≥ 285k; (2) on the replica, block 1 t > 0. Screen zc5 is on the VM.
- 06:12: u6w REJECTED at screen zc5 (falsifier 1, delivery): D 12 (+5 −7), 31 vs 33 wins (bar 31: pass), delivered 278,567 (bar 285k: fail). The pair is worse than either half: PEACE's longer truces plus the cap cost hauling on the araid maps (arrows A+B, evileye B, learn_your_lesson A, wallsofparadis A lost). Reject 16. Next: plateau step 4, revisit principles from other years.
- 06:14: Plateau step 4 (principles from other years and our LEARNINGS). Lesson 15 (enemy traps are the rat-death gap; we lay none): trap laying has 18 attempts (latest u5u, net 0), so that area is saturated.
  Baby deaths in the Xzibit/YokoOno always-lost matches (24 matches): in losses we lose 62.9 rats a game to their 69.6. Theirs die mostly to cats (pounce 24.5); ours die to throw landings (25.5 vs 20.5) and their traps (7.8 vs 0). So the gap is economic. In the learn_your_lesson spiral they pick ~2× our cheese with 20 babies to our 7 and refill; most of the map's cheese lies unpicked.
  While I plan the next structural swing, the replica takes u6o (−RUSH_BREAK, the neutral ablation with the best delivery: 318k vs 299k, 33 vs 33 wins) as sub 194; block 1 after . RUSH_BREAK lets our rushers bite during the truce; this is a lighter cousin of PEACE. Falsifier: block 1 t > 0, and our early truce breaks (< r100) fall to ≤ 5.
- 06:31: u6o (−RUSH_BREAK) block 1 (sub 194, 11397-11412) vs refs 9093-9168: 48 cells, 43 alike, D 5 (+3: Finals tiny A, Icarus rose B, Icarus uneasy_alliance A; −2: Merlin peaceinourtime A, spsquared fullofsecrets B), net +1, t +0.45 → block 2. Falsifier holds: our early truce breaks 3 (bar ≤ 5); opponents broke early in 31 of 48 games. Block 2 is queued on set 2 after 11412, paired against set-2 refs 9015-9075.
- 07:26: u6o REJECTED at block 2. Set 2 (11447-11508) vs refs 9015-9075: 48 cells, 42 alike, D 6 (+3: Finals attack_on_cat B, YokoOno Hike B, YokoOno streetsofnewyork B; −3: Merlin fullofsecrets A+B, spsquared jail A), net 0, t 0.00. Pooled over blocks 1+2: D 11, net +1, t +0.30 (bar 1). As with u6h (PEACE), keeping the truce costs Merlin fullofsecrets on both sides. The truce-timing area now has two rejects (u6h, u6o). Reject 17.
- 07:28: VM root disk was at 1.9 GB. I pruned 2,334 replica replays older than 48 h (2.9 GB), listed in /root/bc26-replays-pruned-2026-11-03b.txt on the VM; it now has 4.7 GB free. The 02:44 prune used find -mtime +2, which means ≥ 72 h, so it caught only 434. The site's Replay links for those matches no longer work; their results stay in the database, and our local copies are untouched.
- 07:28: u6x pre-registered (economy/roles): u6a + RUSH_UNTIL 300. The rush index is the live-baby count, so with RUSH_UNTIL 0 every replacement born with 4-15 babies alive is an attacker all game, including in the learn_your_lesson spiral and after any heavy loss. s8v (150) and u2i (100) were neutral screens on older bases and never reached the replica. Falsifiers: (1) screen zc6 wins ≥ 31 and delivered ≥ 285k; (2) on the replica, block 1 t > 0, and our pick rate in losses after r300 must rise. Screen zc6 is on the VM.
- 07:49: u6x screen zc6 passed falsifier 1: D 7 (+3: abrk arrows B, araid DefaultMedium A, starvation A; −4: araid arrows A+B, evileye A+B), 32 vs 33 wins (bar 31), delivered 354,802 vs 298,702 (+19%), overruns 0. The delivery jump confirms the mechanism: late replacements gather instead of rushing. Submitted as sub 195; block 1 queued on set 1 after 11508.
- 08:25: u6x block 1 (sub 195, 11549-11677, 15 matches; the spsquared seat-A match never ran) vs refs 9093-9168: 45 cells, 42 alike, D 3 (+3 −0: Xzibit learn_your_lesson A+B, YokoOno Stash A), net +3, t +1.73 → block 2. All three gains are always-lost cells from plateau step 3: learn_your_lesson vs Xzibit was 0/12 and Stash vs YokoOno 0/12. That is the diagnosis's target, flipped. Block 2 is queued on set 2 after 11677, paired against refs 9015-9075.
- 09:21: **u6x ACCEPTED** (u6a + RUSH_UNTIL 300). Block 2 (sub 195, set 2, matches 11678-11702) vs refs 9015-9075: 48 cells, 46 alike, D 2 (+2: YokoOno Hike B, collegejunior learn_your_lesson B), net +2, t +1.41. Pooled over blocks 1 and 2: 93 cells, D 5, gained 5, lost 0, net +5, t +2.24 (accepts at ≥ 2 from block 2).
  - The gains are the plateau step 3 targets: learn_your_lesson vs Xzibit A+B (0/12 before), Stash vs YokoOno A (0/12), plus YokoOno Hike B and collegejunior learn_your_lesson B.
  - Mechanism, confirmed on the screen: delivered +19% (355k vs 299k). With RUSH_UNTIL 0 the rush index is the live-baby count, so every replacement born with 4-15 babies alive was an attacker all game. After a long war or in a spiral, the king spent its last cheese on rushers instead of gatherers.
  - Lesson: the death-spiral diagnosis was right (u6u), but the lever was the spawned role, not the floor.
  This ends 17 straight rejects (u6b-u6w). The new incumbent is bots/u6x (the replica's active submission, 195). Paired refs for u6x: set 1 = 11549-11677 (15 matches; the spsquared seat-A match never ran), set 2 = 11678-11702. Candidates now copy bots/u6x.
- 09:27: Follow-up on u6x (incremental): bracket RUSH_UNTIL with u6y (200) and u6z (500), each u6x with one constant changed. Screens zc7 and zc8 run sequentially on the VM, controlled against u6x's own screen zc6 (32 wins, delivered 354,802). Falsifier: wins ≥ 31 and delivered ≥ 340k (−4%); the better one goes to the replica.
- 09:27: Local disk at 1.4 GB free. Deleted 180 build/dumpcache entries older than 2 h (1.3G, regenerable replay parses; list in build/dumpcache_deleted_2026-11-03c.txt). Replays untouched.
- 10:07: Bracket screens vs u6x's zc6 (32 wins, 352,884 xfer). u6z (500): D 4 (+0 −4), 28 wins (bar 31): REJECTED; later rush spawns cost the abrk evileye/arrows long games. u6y (200): D 5 (+2 −3), 31 wins (bar 31: pass), delivered 349,950 (bar 340k: pass), starved kings 2, overruns 0. That is flat locally, so the replica decides: submitted as sub 196, block 1 after 11702, paired against u6x's set-1 games 11549-11677.
- 10:09: u7a = u6x + GATHER_FIRST 9 (roles lever not tried alone on the replica; RUSH_LIFE was rejected twice, t0g and u5w). Screen zc9 vs zc6; falsifier: wins < 31 or delivered < 340k rejects.
- 10:27: u6y (RUSH_UNTIL 200) REJECTED at replica block 1: 45 cells, 44 alike, D 1 (+0 -1), t -1.00 (lost spsquared bed B). RUSH_UNTIL bracket done: 300 stays.
- 10:28: u7a screen zc9 passed (31 wins vs 32, D 1 lost araid DefaultMedium A, delivered 351k). Submitted #197, block 1 on set 1 after 11786.
- 10:37: u6x in-band field losses (set 1+2 refs): king damage in 12 losses was 203 from bites and 209 from our own rats thrown by the enemy (knifefight A, 90 rounds: 405 throw damage, nothing else). Merlin: 53 throw hits in 9 games, 40 each; victims 2-4 tiles from our king, often full health (grabbed blind), 38% juggled. The king spawns before it bites and its bite ignores carriers. u7b = u6x + KING_BITE_CARRIER (new switch, ratbot too, BotTest 1948/1948, 4/4 mutants caught): bite a carrier of ours first, before spawning. Screen zd1; falsifiers: wins < 31 or delivered < 340k; on the replica, Merlin throw hits not below 5.9/game.
- 11:00: Owner noticed our replica rating sat at exactly 1561 for ~2 days. Ranked play never stopped (44 ranked of the last 400 matches); siarnaq's rating chain had stalled: 7,553 of 23,552 participations unrated, the first at ranked match 7928 (the 2026-11-01 16:00 PST autoscrim round, ERR), whose rating-update task never fired, so every later match of its teams waited. Ran tools/galaxy26/rating_unstick.py (snapshot /var/tmp/bc26-rating-snapshot.json): 7,553 -> 10 unrated (9 heads RUN/QUE, normal). vibe26b now 1622, rank 18 (was 1561, 25). progress-refresh.sh now runs the unstick step before reading the replica, so a stall cannot outlast a block again. Panel picks by rating were made on frozen ratings since 11-01.
- 11:00: u7b screen zd1 passed: 33 wins vs 32, D 5 (+3 -2: canyoudig B, tiny B x2 gained; tiny A, starvation A lost), delivered 353k, 0 overruns. Queued for the replica after u7a's block 1.
- 11:19: u7a (GATHER_FIRST 9) REJECTED at replica block 1: 45 cells, 44 alike, D 1 (+0 -1: YokoOno Stash A), t -1.00. u7b (KING_BITE_CARRIER) submitted #198, block 1 on set 1 after 11868. VM disk 4.3 GB free.
- 11:25: u7c = u6x + KING_BITE_MAX 400 (never tuned; king bites decide the 12 in-band losses' other half, 203 bite damage on our king vs 13 on theirs). Screen zd2; falsifier: wins < 31 or delivered < 340k.
- 11:43: u7c (KING_BITE_MAX 400) REJECTED at screen zd2 by its falsifier: delivered 329k < 340k (wins 33 vs 32, D 3 +2 -1). 15k of the 23k drop is araid cheesefarm B won at r723 instead of drawn to r2000: the delivered bar is confounded by game length; next screen falsifiers state delivered per 1000 rounds instead.
- 11:44: u7d = u6x + RETREAT_HP 0: retreat() walks babies at <= 40 hp to our king, where Merlin grabs them (victims 2-4 tiles out, 51% at <= 50 hp) and throws them into it. s8b tried it once (weak screen pass, parked; only bundled on the replica). Screen zd3; falsifiers: wins < 31 or any overrun (delivered dropped as a bar: game length confounds it, u7c).
- 12:05: u7d (RETREAT_HP 0) screen zd3 passed: 35 wins vs 32, D 11 (+7 -4; gained araid cheesefarm A/B, evileye A, DefaultMedium B, canyoudig B, abrk canyoudig B, tiny B; lost abrk DefaultSmall B, bed A, araid DefaultMedium A, canyoudig A), 0 overruns, 0 starved kings. Queued for the replica after u7b.
- 12:22: u7b (KING_BITE_CARRIER) REJECTED at replica block 1: 45 cells, 43 alike, D 2 (+1 -1), t 0. Merlin throw hits 5.2/game vs 5.9; thewaxmango 6.2/game, YokoOno/Xzibit 1.5. The king seldom has a carrier in bite reach before the throw: the throw lands from range. u7d (RETREAT_HP 0) submitted #199, block 1 after 11960.
- 12:27: u7e = u6x + RETREAT_HP 20 (bracket of u7d). Screen zd4; falsifiers: wins < 31 or any overrun; to the replica only if it beats u7d's 35 or u7d is rejected.
- 12:46: u7e (RETREAT_HP 20) REJECTED at screen zd4: 30 wins < 31 (u6x 32, u7d 35). Retreat is all-or-nothing: the half setting keeps the ammo walking home.
- 12:46: u7f = u6x + GATHER_FIRST 4 (other side of u7a). Screen zd5; falsifiers: wins < 31 or any overrun.
- 13:08: u7f (GATHER_FIRST 4) REJECTED at screen zd5 as a no-op: identical to zc6 to the unit (32 wins, 352,884 transfer); rushers take spawn indices 4-15 before the gather/fight split, so GATHER_FIRST < 6 is dead. u7d's block 1 is 12 of 16 matches in (4 behind the hourly limit).
- 13:21: u7d (RETREAT_HP 0) replica block 1: 45 cells, 36 alike, D 9 (+5 -4), t +0.33: continue. Throw hits on our king/game: Merlin 2.0 (u6x 5.9, u7b 5.2), YokoOno+Xzibit 1.1 (1.5 under u7b), thewaxmango 7.3 (6.2). The mechanism works on Merlin; thewaxmango's throws come from elsewhere. Block 2 on set 2 queued after 12043.
- 13:26: u7d block-1 king damage: 21 losses took 268 bite and 113 thrown-rat damage (12.8 and 5.4 per loss; u6x 17 and 17.4). Bites are now the killer. u7c (KING_BITE_MAX 400) passed its screen on wins (33 vs 32, 0 overruns) and failed only the delivered bar that game length confounds; reinstated, before any of its replica games, as the next replica candidate after u7d. Replica falsifier: bite damage on our king per loss not below u6x's 17.
- 13:42: **u7d block 2** (set 2; 12056-12075 vs u6x's 11678-11702): 48 cells, 42 alike, D 6 (+5 -1: Finals attack_on_cat B, dirtpassageway B, Xzibit canyoudig A, YokoOno Hike A, Merlin knifefight A; lost spsquared jail A), t +1.63. Pooled over blocks 1-2: 91 cells, D 15 (+10 -5), net +5, t +1.29: between block 2's bars (reject <= 1, accept >= 2), so block 3 on set 3. u6x has no set-3 games: resubmitted u6x (#200), 16 set-3 scrims after 12075 (build/wait_u6xr3.sh); u7d is resubmitted after. VM disk 4.0 GB.
- 13:47: u7g = u7d + KING_BITE_MAX 400 (u7c's change rebased, ready if u7d is accepted). Screen zd6 vs u7d's zd3; falsifiers: wins < 33 or any overrun; on the replica, bite damage on our king per loss not below u7d's 12.8.
- 14:08: u7g (u7d + KING_BITE_MAX 400) screen zd6: 33 wins vs u7d's 35 (bar 33), D 2 (+0 -2: abrk evileye B, tiny B), 0 overruns: a bare pass. Replica only if u7d is accepted. u6x set-3 reference: 5 of 16 matches in.
- 14:32: u6x set-3 reference done (#200; 12130-12133, 12152-12163, all 16 OK, fetched). u7d resubmitted (#201) for block 3 on set 3: 16 scrims after 12163 (build/wait_u7db3.sh). Block 3 rejects at pooled t <= 1.5, accepts at >= 2.
- 15:34: **u7d (RETREAT_HP 0) rejected at replica block 3** (#201; set 3, 12171-12260 vs u6x's 12130-12163): block 3 D 4 (+2 -2: YokoOno externalsustenance A, collegejunior FreeTheCheese B gained; Xzibit minimaze B, spsquared rose B lost), t 0. Pooled over 135 cells: D 19 (+12 -7), net +5, t +1.15, under block 3's 1.5. A near-miss with a working mechanism (Merlin throw hits 5.9 -> 2.0/game). u7g (u7d + KING_BITE_MAX 400) is dropped with it.
- 15:34: **Plateau trigger**: 8 straight rejects since u6x's acceptance (u6z, u6y, u7a, u7b, u7c, u7e, u7f, u7d). Steps 1-4 ran at the last plateau (11-02/03: audits, ablations zb1-zb8, field re-watch -> u6x). Step 5 now: near-misses in pairs. u7h = u6x + RETREAT_HP 0 (u7d, +12 -7) + FORM_FROM 600 (u5l, +6 -3 on u4s; independent mechanism: second-king timing). Screen zd7 vs u7d's zd3; falsifiers: wins < 33 or any overrun. Step 6 (structural swing) follows if the pair fails.
- 15:55: u7h screen zd7 passed: 36 wins (u7d 35, u6x 32), delivered 343k, 0 overruns, 0 starved kings. Submitted #202, block 1 on set 1 after 12276 (vs u6x's set-1 refs).
- 16:36: **u7h (RETREAT_HP 0 + FORM_FROM 600) rejected at replica block 1**: 45 cells, 37 alike, D 8 (+4 -4), t 0. The changed cells are u7d's block-1 cells less superioristhmus B, so FORM_FROM adds nothing. No-retreat gains against throwers (Finals, Icarus, Merlin) and loses against Xzibit, YokoOno, Varun, spsquared (throw hits 0.2-1.1/game). Plateau step 6, structural (doctrine adapts to the opponent): u7i = u6x + THROW_ADAPT, a baby that has seen an enemy carrying one of our rats never retreats from then on.
- 16:37: u7i built: THROW_ADAPT (Baby.sawThrower sets a per-rat flag on sensing an enemy that carries one of ours; Baby.mayRetreat refuses retreat once it is set). The shared array has no free slot (all 64 used), so the flag is per rat, not broadcast. ratbot has it off; BotTest 1958/1958, 4/4 mutants caught. Screen zd8 vs zc6; falsifiers: wins < 31 or any overrun; on the replica, Merlin throw hits not below 4/game, or the four non-thrower cells u7d lost lost again.
- 16:59: u7i (THROW_ADAPT) screen zd8 passed: 33 wins (u6x 32, u7d 35), 0 overruns, 0 starved kings. Submitted #203, block 1 on set 1 after 12413. VM disk 3.6 GB.
- 17:19: **u7i (THROW_ADAPT) rejected at replica block 1**: 45 cells, 40 alike, D 5 (+2 -3), t -0.45. Merlin throw hits 1.2/game (u6x 5.9), so the switch works on throwers, but YokoOno clouds B and Varun knifefight A were lost again (falsifier): they grab our rats too, in the field, so the flag fires on them. u7j = u7i + THROW_ADAPT_R 4: a carrier counts only within Chebyshev 4 of our king (Merlin's victims were 2-4 tiles out).
- 17:22: u7j built (ratbot gets THROW_ADAPT_R 0; BotTest 1962/1962, 4/4 mutants caught). Screen zd9 vs zc6; falsifiers: wins < 31 or any overrun; on the replica, Merlin throw hits not below 4/game, or YokoOno clouds B / Varun knifefight A lost again.
- 17:40: u7j screen zd9 passed: 32 wins (u6x 32), 0 overruns, 2 starved kings. Submitted #204, block 1 on set 1 after 12429. VM disk 3.5 GB.
- 18:13: **u7j (THROW_ADAPT_R 4) rejected at replica block 1**: 45 cells, 41 alike, D 4 (+1 -3), t -1.00; Merlin throw hits back to 4.0/game (u7i 1.2). The retreat line (u7d, u7e, u7h, u7i, u7j) closes for now: no-retreat helps against throwers and every gating we tried either misses Merlin or catches Varun/YokoOno. Varun knifefight A is lost by every variant, including u7j where retreat mostly stays on.
- 18:13: u7c (KING_BITE_MAX 400, reinstated 13:26) submitted #205, block 1 on set 1 after 12514 (bites are the king killer now: 12.8-17 per loss).
- 18:18: u7k = u6x + KING_BITE_MAX 900 (bracket of u7c). Screen zd10; falsifiers: wins < 31 or any overrun; to the replica only if it beats u7c's 33 screen wins or u7c fails.
- 18:37: u7k (KING_BITE_MAX 900) screen zd10 identical to u7c's zd2 (33 wins, 330,021 transfer): the bite cheese cap is not binding past 400. Parked; u7c's replica block decides the lever.
- 18:38: Varun DefaultMedium (u6x, 11554-11555, both lost ~r300-356): our king took 760 bite damage in 2 games. kingbiters on 11554: 54 bites on our king by 1-2 touching rats, 46 of them with none of ours adjacent, while 4-8 of ours stood within 4 (343 rat-rounds: war 122, forming a king 91, gather 65). u7l = u6x + GUARD_KING_R 5 (existing switch, last tried on u2g vs the old top-heavy panel). Screen zd11; falsifiers: wins < 31 or any overrun; replica: bite damage on our king per loss not below 17.
- 19:00: u7l (GUARD_KING_R 5) REJECTED at screen zd11: 28 wins < 31, D 8 (+2 -6: lost araid DefaultSmall A/B, jail B, starvation A, abrk bed B, evileye A). The king guard draws defenders off the raid fights; two closures now (u2w, u7l).
- 19:03: u7m = u6x + FORM_SAFE 1 (new switch, ratbot off; pure King.formHold + 5 BotTest checks, 3/3 mutants caught, 1967/1967): while any enemy rat is within KING_THREAT_D2 of our first king it starts no second-king formation and calls off the running one, freeing the 91 formation rat-rounds seen near the bitten king in Varun 11554. Screen zd12 vs zc6; falsifiers: wins < 31 or any overrun; replica: bite damage on our king per loss not below 17. PROMPTS #24-#25 recorded.
- 19:17: u7c (KING_BITE_MAX 400) REJECTED at replica block 1 (12529-12607 vs set-1 refs 11549-11677): 45 paired cells, 45 alike, t 0.00. Bite damage on our king per loss 298/21 = 14.2 (falsifier would pass) and throw damage 172/21 = 8.2, but no game outcome changed: the bite-cheese cap is not binding on the field. Bite-cheese line closed (u7c, u7k). Replica idle until u7m clears screen zd12.
- 19:33: u7m (FORM_SAFE) passed screen zd12: 32 wins = zc6's 32, no cell flipped, 0 overruns; submitted (206), block 1 on set 1 queued after 12607.
- 19:33: Finals DefaultSmall loss (12531 g0, our king dead r201): 59 of 59 bites on our king unanswered. Our war-mode rats near it (28 of 37 rat-rounds at hp <= 40) shuttled between two tiles beside the king for ~15 rounds. Reproduced locally (u6x vs araid DefaultSmall, indicator strings kept): those rats had no enemy in view and followed war()'s ALERT branch, whose target is the alerting king's own centre (nt=13,17): they stall against its 3x3 while the biter on the far side stays unseen. u7n = u6x + ALERT_SIDE (new switch; the ALERT+1 spare bits 6-9 carry the direction to the king's nearest threatening rat; Comms.sideTarget is pure, 21 BotTest checks, 4/4 mutants caught, 1989/1989): such rats go to the tile two steps out on that side. Screen zd13 vs zc6; falsifiers: wins < 31 or any overrun; replica: bite damage on our king per loss not below 17.
- 19:54: u7n (ALERT_SIDE) PASSED screen zd13: 37 wins vs zc6's 32 (+9 -4: araid arrows A/B, evileye A/B, cheesefarm A, starvation B, tiny B, DefaultMedium B, abrk tiny B; lost araid DefaultSmall A/B, DefaultMedium A, abrk bed B), 0 overruns, transfer 357k vs 353k. Best screen since u6x's acceptance (u7d 35). Goes to the replica once u7m's block 1 (10 of 16 in) finishes.
- 20:08: u7m (FORM_SAFE) REJECTED at replica block 1 (12630-12677 vs set-1 refs): 45 cells, 44 alike, D 1 (lost Varun knifefight A), t -1.00. Formation suppression under threat changes almost nothing on the field. u7n (ALERT_SIDE, screen 37 vs 32) submitted (207); block 1 on set 1 after 12677. Supporting field evidence: Varun DefaultMedium 11554 g0 has 122 war-mode rat-rounds within 4 of our bitten king that never reach the biter, 49 of them with it out of view.
- 20:26: Why u7n lost araid DefaultSmall locally: r100-200 its rats spent ~630 rat-rounds on a king formation (code K) that never completed. At r160, 6 of the site's 9 tiles were held; the 3 free ones lay against the map edge, and 5 rats one ring out walked at the occupied centre and queued (Nav sidesteps robots only after stalls). Field check (formstall.py, 93 u6x replica games): 203 K rat-rounds a game; 10 games with a formation hitting its 61-round TTL; several with 1000-3300 K rat-rounds. u7o = u6x + FORM_FILL (basics repair, new switch): joiners from beyond the 3x3 walk to the nearest free ring tile (side-frame ties; fillKey pure, 7 BotTest checks, 4/4 mutants caught, 1995/1995). Screen zd14 vs zc6; falsifiers: wins < 31 or any overrun; replica: formation rat-rounds a game not below 203.
- 20:45: u7o (FORM_FILL) passed screen zd14 at the bar: 31 wins vs zc6's 32 (+1 -2: won araid starvation B; lost araid DefaultMedium A, starvation A), 0 overruns, transfer 338k vs 353k. Queued for the replica behind u7n (a basics repair: accepts at t >= 0 after block 2). Local mechanism check vs araid DefaultSmall running.
- 20:56: u7n (ALERT_SIDE) REJECTED at replica block 1 (12687-12756 vs set-1 refs): 45 cells, 42 alike, D 3 (+1 Finals tiny A; -2 Finals tiny B, YokoOno Stash A), t -0.58. Screen +9 -4 did not carry.
- 20:56: **Measurement repair: u6x's crowning sets were never re-anchored.** u6x was accepted on set 1 (11549-11677, +3 -0) and set 2 (11678-11702, +2 -0), and every candidate since (u6y-u7n, 15 of them) was paired against those crowning draws. TRAINING_ALGORITHM §5 says an accepting set gets a twin before anyone is scored on it: the winner's curse. Candidates have lost exactly the cells u6x flipped (u7n lost YokoOno Stash A, one of u6x's three set-1 gains). u7p = u6x + HAND_FLIP (the existing salted-twin switch, last used for u2g) submitted (208): set 1 after 12759, then set 2. Its games become the references there; u7n and the other set-1 rejects get rescored against it. u7o (FORM_FILL) waits until set 1 is re-anchored. Local check of u7o's mechanism vs araid DefaultSmall: second king formed 2 rounds after the first joiner (41 K rat-rounds; u6x 82, u7n's stalled formation 630), won r703 with 3905 transfer (u6x 2245).
- 21:04: thewaxmango losses (12602-12603; dirtpassageway, clockwisdom): our king took 496 thrown-rat damage in 3 losses against 263 from bites; 44 throw hits in 6 games (7.3 a game), 57% of them juggled. First grabs: full-health rats (hp 100 in 47%) 2-3 tiles from our king, mostly walking to a mine (code M), so grabbed from behind; the king (full view) saw the grabbers. u7q = u6x + KING_EYES (new switch, structural: the king shares its view): the king squeaks the nearest enemy baby within 18 as SQ_TRAP (after SQ_START, no cat in view; one message a turn); a baby with a heard enemy within dist2 8 strafes facing it. Pure King.eyesTarget and Baby.nearestHeard, 11 BotTest checks, 7/7 valid mutants caught, 2006/2006. Screen zd15 vs zc6; falsifiers: wins < 31 or any overrun; replica: thrown-rat hits on our king a game vs thewaxmango and Merlin not below 7.3 and 5.9.
- 21:22: u7q (KING_EYES) passed screen zd15: 34 wins vs zc6's 32 (+5: abrk canyoudig B, araid DefaultMedium B, canyoudig B, evileye B, starvation B; -3: abrk bed B, araid bed B, starvation A), 0 overruns, transfer 345k. Replica order: the u7p twin finishes set 1 (8 of 16 in); candidates then take block 1 on set 3 (u6x's post-acceptance refs 12130-12163, not a crowning draw), block 2 on twin-anchored set 1; the twin plays set 2 only if a candidate reaches block 3. u7o first, then u7q.
- 21:30: u7r built (exploratory, not a replica candidate): u7n + FORM_FILL + KING_EYES, the three screened singles stacked (ALERT_SIDE 37, FORM_FILL 31, KING_EYES 34 vs zc6 32). Screen zd16 vs zc6; falsifier: wins <= 37 (no gain over the best single) or any overrun means the bundle adds nothing. Full unit suite after the u7o/u7q ratbot changes: exit 0, 0 FAIL.
- 21:30: The two 0/6 field cells, re-read on u6x-era replays. DefaultMedium vs run_away10 (12580-12726, our king dead r251-356): every game the same; ~5 of our rats die to enemy rat traps by r50 and 13-18 by r100 (50 fires in 12726 g0, 34 of them within a round of the lay, victims mostly G:a attackers on their side of the map), then the stunned rats are thrown. That is the trap war closed after five dodging rejects (RISK_FRESH, RISK_IN_FIGHT, FACE_HOLD, WOUNDED_HP, RISK_ADAPT): no new attempt there. dirtpassageway vs thewaxmango (626-1217 rounds): our king's damage is almost all our own rats thrown by them (throwOwn 30-144 per 50 rounds, all game), which is u7q's KING_EYES target; it stays next in the replica order after u7o.
- 21:45: **Twin u7p set 1 done** (#208, 12771-12813, 16/16). Against u6x's crowning draw (11549-11677): 45 cells, 37 alike, D 8 (+2 -6), net -4, t -1.41. The crowning set overstated u6x by about four cells: winner's curse confirmed.
  - Plain rescoring against the twin is biased the other way. A candidate that barely changes play replays u6x's lucky games: u7c (45/45 alike with the crowning draw) scores +8 -2, t +1.90 vs the twin.
  - Identity-aware rescoring (scratch idpaired.py): a cell whose candidate game repeats the crowning game (same winner and round count) scores 0, any other cell is a fresh draw scored against the twin. Set-1 blocks: u7d +8 -3 (t +1.51; 31 fresh cells), u7h +8 -3 (+1.51), u7i +6 -2 (+1.41), u7n +4 -2 (+0.82), u7b +3 -1 (+1.00), u7c +2 -0, u6y/u7a/u7m +2 -1, u7j +2 -2. Only 7 of the twin's 48 games repeat the crowning game exactly, so HAND_FLIP re-rolls nearly everything.
  - The retreat line (u7d, u7h, u7i) is positive on every unbiased read. u7d reached block 3 (rejected at pooled t +1.15 on crowning-referenced blocks 1-2). With block 1 rescored and block 2 still unanchored (+5 -1 vs the crowning set 2), its pooled t is 1.96.
  - So the twin plays set 2 now (the 21:22 rule: only once a candidate reaches block 3): 16 scrims queued for u7p after 12821 (waiter build/wait_u7pr2.sh).
  - Pre-registered: u7d's three blocks are rescored identity-aware against the twins on sets 1-2, and plainly on set 3 (u6x's post-acceptance refs). Pooled t ≥ 2 accepts u7d (block 3's bar); otherwise the rejection stands. u7o and u7q wait behind this.
- 21:45: u7r (u7n + FORM_FILL + KING_EYES) screen zd16: 37 wins vs zc6's 32 (+10 -5), 0 overruns, 5 starved kings. That equals u7n's 37, so the falsifier fired: no stacking. Not submitted.
- 21:47: The identity-aware score is now a tool: `contest.py paired TWIN CAND --crown CROWN` (contest.identity_ref). A candidate game that repeats the crowning game on its cell (same winner and round count) takes the crowning game as its reference there (d 0); every other cell is scored against the twin. It reproduces the 21:45 scratch numbers: u7d +8 -3, u7c +2 -0, the twin against itself 0. tools/tests/test_scoring.py has 114/114 checks, the identity ones included, and 2/2 mutants were caught (3 and 5 FAILs): comparing winners only, and no identity at all. TRAINING_ALGORITHM §4's twin rule now carries the exception.
- 21:51: Prepared rebases in case the re-anchored rescore accepts u7d: u7s = u7d + KING_EYES (u7q + RETREAT_HP 0) and u7t = u7d + FORM_FILL (u7o + RETREAT_HP 0). Screens zd17 and zd18 run back to back on the otherwise idle VM, controlled against u7d's zd3 (35 wins). Falsifiers: wins < 34 or any overrun. Neither is submitted unless u7d becomes the incumbent; otherwise u7o and then u7q go on u6x as planned.
- 21:51: How our last king died in current losses (scratch lastking.py over the twin's set 1 and u6x's set 3: 56 losses): starved 24, destroyed 22 (no die row: killed in the final round), points 7, bitten 2, trapped 1. Starvation is still the largest single cause. Icarus starves us earliest: averyfineline r211-238 and mincemeat r318-375. On averyfineline (20x20, 12160 g0) our bank went 2488 → 635 (r60) → 105 (r100) while our babies died 20 → 0 by r140 to traps and throws. That is the combat bleed again, not a gathering fault.
- 22:28: Rebase screens vs u7d's zd3 (35 wins, 0 overruns). u7s (u7d + KING_EYES), zd17: 36 wins, D 7 (+4: abrk DefaultSmall B, bed A, araid canyoudig A, starvation B; −3: abrk canyoudig B, tiny B, araid evileye A), 0 overruns, 1 starved king. u7t (u7d + FORM_FILL), zd18: 36 wins, D 1 (+1: araid DefaultMedium A), 0 overruns, transfer 346k vs 334k. Both pass; they wait on the u7d verdict.
- 22:40: **Twin u7p set 2 done** (12838-12899, 16/16). Against u6x's crowning set 2 (11678-11702): 48 cells, 33 alike, D 15 (+7 -8), net -1, t -0.26. Set 2's crowning draw was not inflated; set 1's was (-4).
- 22:40: **u7d re-anchored verdict: the rejection stands.** Identity-aware against the twins: set 1 +8 -3, set 2 +3 -3 (Xzibit canyoudig A, YokoOno streetsofnewyork A, spsquared evileye B gained; Varun ratsareants A+B, spsquared jail A lost); set 3 plain against u6x's 12130-12163: +2 -2. Pooled D 21 (+13 -8), net +5, t +1.09, under block 3's 1.5. The mechanism is real (Merlin throw hits 5.9 → 2.0/game) but its cells net out. The prepared rebases u7s and u7t are shelved with it.
- 22:40: Measurement from here on: set 1 and set 2 are scored against the twin's games with `--crown` (set 1 --crown 11549-11677 vs 12771-12813; set 2 --crown 11678-11702 vs 12838-12899), set 3 plainly against 12130-12163. A candidate's block order: set 3, set 1, set 2, then set 4 (u6x needs a reference block there first). u7o (FORM_FILL, basics repair) submitted (#209) and block 1 on set 3 queued after 12899 (waiter build/wait_u7ob1.sh). u7q follows.
- 22:44: How games end now (twin set 1 + u6x set 3, 96 games). In 40 wins the enemy's last king was starved 15 times, outscored at r2000 18 times, destroyed 5 times and bitten 2 times. In 56 losses ours was starved 24 times, destroyed 22 times, outscored 7 times, bitten 2 times and trapped once. So the cheese race decides about 40% of all games, and we almost never kill a king by force.
  - In averyfineline vs Icarus (12160 g0, 20x20) both banks hit zero by r100. We had spawned 58 rats and dug 25 tiles; they had spawned 43 and laid 50 rat traps (1,000 cheese) to our 0. Their traps killed our babies (20 → 8 by r100, 0 by r140) while theirs kept 12-16 and recovered the bank. This is the trap war again, which is closed.
- 22:44: API sweep (§6.2), RobotController methods u6x never calls: removeRatTrap (own traps only), isBeingThrown/isBeingCarried, the cooldown getters, senseRobot, and MapInfo.flyingRobot(), which shows rats in flight.
  - Thrown-rat hits on our king (195 in the thewaxmango/Merlin games) by damage: 88 at 42 (carrier 2 tiles from the king's edge, impact on the throw turn), 26 at 38 and 9 at 34 (one turn of flight), 52 at 30 and 7 at 26 (two turns). So 55% arrive a turn or two after the throw.
  - The king can't use that warning: its movement cooldown is 40 against the baby's 10, about one step per four turns, and a 3x3 body needs two steps to clear a line through its centre. A shield of babies on the line would trade baby health for king health, the guard trade that u7l (GUARD_KING_R) already lost. Not pursued.
- 22:45: Replica queue under the re-anchored measurement. Block 1 rejects only at t ≤ 0, and on the identity-aware rescore several set-1 rejects were above 0, so they continue rather than restart:
  1. u7o (FORM_FILL), block 1 on set 3, now playing;
  2. u7q (KING_EYES), block 1 on set 3;
  3. u7n (ALERT_SIDE), block 2 on set 3 (block-1 identity t +0.82);
  4. u7i (THROW_ADAPT), block 2 on set 3 (+1.41).
  u7h (u7d + FORM_FROM 600) drops, since its block-1 cells were u7d's and u7d is now decided.
- 22:45: u7u = u7n + RETREAT_HP 0 (plateau step 5, the two near-misses paired; both move war rats near our king, from different triggers). Screen zd19 vs zc6. Falsifiers: wins < 37 (u7n's own screen) or any overrun.
- 23:06: u7u (u7n + RETREAT_HP 0) REJECTED at screen zd19: 33 wins vs the 37 bar (zc6 32, u7n 37), D 17 (+9 -8), 0 overruns, 4 starved kings, transfer 325k (-8%). The pair is worse than ALERT_SIDE alone: no-retreat sends wounded rats into the side fights ALERT_SIDE starts. u7o's set-3 block is 8 of 16 in.
- 23:32: u7o (FORM_FILL) REJECTED at replica block 1 on set 3 (12913-12964 vs u6x's 12130-12163): 48 cells, 45 alike, D 3 (lost Finals where_are_you A/B, spsquared rift A), t -1.73. Its mechanism held on the field (formation rat-rounds 183 a game vs u6x's 203), but no game turned on it. u7q (KING_EYES) submitted (#210), block 1 on set 3 queued after 12964 (waiter build/wait_u7qb1.sh).
- 23:32: u7v = u6x + MICRO (the existing combat micro, off since s0o's 0/54 on the s0 base; plateau step 4, cross-year principle that micro pays most) REJECTED at screen zd20: 28 wins vs zc6's 32 (bar 31), D 14 (+4 -10), 0 overruns. The generic step scorer, which ignores facing, grabs and traps, loses fights that faceEnemy wins.
- 23:36: **Where the cheese race is lost (u6x set 3, 12130-12163, 48 games; new scratch tools pickrate2, minecontest, theft4, mineocc2, catdeaths).** Over r1-300 we pick 1319 a game to their 1933 with more babies alive (18.5 vs 15.7): 23.8 picks per 100 baby-rounds to their 41.0 (wins 21.3 vs 32.6, losses 25.8 vs 46.5). Mine output is exogenous (each mine and its twin drop 20 together, about every 12.5 rounds), so the gap is who claims the piles:
  - Theft. By r400 they take 19 piles a game spawned on our half (28 in losses, 5 in wins); in losses 15 of the 28 go with no rat of ours within Chebyshev 8, mostly at our 2nd and 3rd mines (6.9 and 7.9).
  - Staffing. By r400 we hold 1774 rat-rounds a game at our nearest home mine against their 700 at theirs; in losses our mines ranked 3-5 get 221 against their 424, and we stand at enemy-half mines 195 to their 548 at ours. Locally (u6x vs araid on FreeTheCheese, indicator strings kept) the nearest mine holds 2.5 rats waiting (W) all game, half of them FIGHT_MINE fighters, while two home mines 29-33 away are never seen before r1192.
  - Idle supply. Piles on our half lying unpicked 100+ rounds (to r400): 37 a game, 30 of them wall-reachable (dirt counts), 17 in losses, mostly 20+ steps from our king; the opponent leaves as many on its half. externalsustenance's 24 mines lie outside the walled start box (unreachable for both).
  - Combat. By r300 we lose 17.3 babies to the enemy and kill 13.2 (losses 23.1 vs 17.1); cats take 8.9 of ours and 8.1 of theirs, so cat fighting is not the leak.
- 23:36: u7w = u6x + SEEK_IDLE 40 + IDLE_SHIFT (the existing coverage pair, on the replica only inside bundle u1a on u0z): an idle gatherer (40 turns at its mine, no pickup) seeks never-seen home tiles and moves to the next mine of its list. Screen zd21 vs zc6; falsifiers: wins < 31 or any overrun; replica: nearest-mine rat-rounds to r400 not 15% below 1774, or ranks 3-5 in losses not above 221.
- 23:56: u7w (SEEK_IDLE 40 + IDLE_SHIFT) PASSED screen zd21: 32 wins = zc6's 32 (+5: abrk canyoudig B, araid cheesefarm A/B, evileye A, starvation B; -5: abrk DefaultSmall A, arrows B, evileye A, araid DefaultSmall B, starvation A), 0 overruns, picks per 100 rat-turns 37.1 vs 35.6, delivered 361k vs 355k. Local FreeTheCheese (u7w vs araid, as A): rat-rounds within 4 of our nearest mine over the game 13.7k vs u6x's 27.9k; the far mine (48,33) first worked at r945 vs r1192, the other (8,32) still never; before r400 nothing changes (the seekers start at the crowded nearest mine and spend their time in the dirt corridors of our own base). Replica order now: u7q (playing), u7i block 2 on set 3 (identity t +1.41), u7w block 1 on set 3, u7n block 2 on set 3. Bracket u7x (SEEK_IDLE 20) on screen zd22.

## 2026-11-04 00:15 PST: u7q rejected; the idle-shift bracket screens 37
- 00:15: u7q (KING_EYES) REJECTED at replica block 1 on set 3 (#210, 12979-13132 vs 12130-12163): 48 cells, 36 alike, D 12 (+4: Xzibit minimaze A, uneruesansfin A, collegejunior FreeTheCheese A/B; -8: Finals where_are_you A/B, Xzibit clouds A, uneruesansfin B, YokoOno 5t4rv4t10n A/B, Stash A, spsquared TheHeist A), t -1.15. Falsifier failed against thewaxmango: thrown-rat hits on our king 11.2 a game (bar: below 7.3); Merlin 3.8 (bar 5.9). The king's squeak and the strafe do not stop the throws. Finals where_are_you A/B is now lost by u7o and u7q alike, after u6x won it twice: those two reference cells look like lucky draws for u6x. u7i (THROW_ADAPT) submitted (#211) for block 2 on set 3 after 13132 (waiter build/wait_u7ib2.sh).
- 00:15: u7x (SEEK_IDLE 20 + IDLE_SHIFT, bracket of u7w's 40) screen zd22: 37 wins vs zc6's 32 (+8: abrk canyoudig B, araid DefaultMedium B, arrows A/B, cheesefarm A/B, evileye B, starvation B; -3: abrk evileye B, araid DefaultSmall B, starvation A), 0 overruns, picks per 100 rat-turns 39.1 (zc6 35.6, u7w 37.1), delivered 348k vs 355k. Tied with u7n for the best screen since u6x's acceptance; its own falsifier keeps it over u7w, so u7x takes u7w's replica slot (next after u7i). u7y (SEEK_IDLE 10) brackets below on screen zd23.
- 00:36: u7y (SEEK_IDLE 10 + IDLE_SHIFT) screen zd23: 36 wins vs zc6's 32 and u7x's 37 (vs u7x: +3 abrk evileye B, araid DefaultSmall B, starvation A; -4 abrk DefaultSmall B, canyoudig B, araid DefaultMedium A, evileye B), 0 overruns, picks per 100 rat-turns 37.0 (u7x 39.1). Its falsifier (more than 37 wins) fails: SUPERSEDED, u7x keeps the slot; SEEK_IDLE 20 is the bracket's peak. Local FreeTheCheese u7x vs araid (as A, won 75-24, xfer 13.7k vs u7w's 14.3k): (48,33) still unworked (2 picks, araid took 47), (8,32) first reached r388 but picked 5 of 60 spawns; the nearest mine held 20.4k rat-rounds. Nearly every discovery switch (SWEEP, SCOUT_FRONTIER, SCOUT_MINES, MINE_EXPLORE, MINE_MIRROR) was tried and is off, so the next screen extends the idle shift to fighters instead: u7z (FIGHT_SHIFT) on screen zd24.
- 00:43: Where the turns go (local FreeTheCheese, u7x vs araid as A): gatherers 35% walking home to deliver, 31% walking to a mine, 13% to a pile, 11% seeking, 2% waiting at the mine; fighters hold more rat-turns than gatherers (20.2k vs 17.1k) and pick on 2% of them. (48,33) and (8,32) never reach the king's list: the gatherers know one mine each (mm=1) and use the team list; the only rat that saw (8,32) was an attacker, which does not come home. Seekers bound for the west band walk the top wall row (y=48) toward unseen tiles for 40+ turns before Flow sends them back to the only corridor, because unseen tiles count as open; pricing unseen tiles would not fix it (the band is unseen on either route), only wall knowledge would. averyfineline vs Icarus (12160, lost r211): both banks under 110 by r100 (we spent 2230 on 58 spawns), they picked 36 times to our 13 and took 240 from our own mine. u8a pre-registered (plateau step 5, near-misses in pairs): u7x + ALERT_SIDE (u7n), both 37 on screen through independent mechanisms. Screen zd25 follows zd24 automatically (build/chain_zd25.sh). Falsifiers: wins below 37 or any overrun; picks/100 rat-turns below 39.1.
- 00:55: u7z (u7x + FIGHT_SHIFT) REJECTED at screen zd24 on its pre-registered falsifier 2: picks per 100 rat-turns 37.3 against the 39.1 bar. Falsifier 1 passed with the best screen since u6x's acceptance: 39 wins vs zc6's 32 and u7x's 37 (vs u7x: +3 abrk evileye B, araid evileye A, starvation A; -1 araid evileye B), 0 overruns, 1 starved king. The mechanism fired: total picks 477.5k vs u7x's 449.2k (+6%), delivered 378.8k vs 348.4k (+9%). The bar was mis-specified: a per-rat-turn rate falls when a richer economy keeps more rats alive, and the rate's own noise across zd21-zd24 is 37.0-39.1, so a bar at the bracket's peak tested noise. Lesson: put screen bars on totals (picked, delivered), set from the spread of earlier screens, never at one screen's point value. The registered verdict stands. A closure condemns one implementation, not the idea, so the dose series continues on fresh data: u8b = u7x + FIGHT_SHIFT with fighters' own idle threshold FIGHT_IDLE 40 (u7z: 20). Bars: screen wins ≥ 37, overruns 0, delivered ≥ 360k (u7x 348k, u7w's 361k the highest of zd21-zd23).
- 00:56: u8a falsifier 2 amended before any zd25 result exists (results.jsonl not yet written): "picks/100 rat-turns below 39.1" becomes "delivered below 348k (u7x's zd22 total)", per the 00:55 lesson. Falsifier 1 is unchanged: wins below 37 or any overrun. u8b's screen zd26 follows zd25 automatically (build/chain.sh zd25 zd26 u8b).

## 2026-11-04 01:02 PST: u7i rejected on its falsifier at pooled t +2.14
- 01:02: u7i (THROW_ADAPT) block 2 on set 3 (#211, 13133-13148, 16/16) vs u6x's set-3 refs 12130-12163: 48 cells, 42 alike, D 6 (+5: Xzibit clouds B, minimaze A, YokoOno externalsustenance A, Varun corridorofdoomanddespair B, collegejunior FreeTheCheese A; -1: YokoOno Stash A), net +4, t +1.63. Pooled with the identity-aware block 1 on set 1 (+6 -2): D 14, net +8, t +2.14, above the block-2 accept bar. REJECTED on pre-registered falsifier 1: Merlin's throw hits on our king were 5.3 a game (bar: below 4; u6x on the same set-3 maps 6.7), and both Merlin matches were lost 0-3. None of the five gains is the mechanism's target. The pre-registered falsifier decides, as it did for u7z at 00:55: a t above the bar with the named mechanism missing reads as noise or as an unregistered effect. The retreat line still reads positive off-target (u7d, u7h, u7i: unbiased set-1 reads +8 -3, +8 -3, +6 -2; u7i's set-3 gains are against grabbers, not throwers). A candidate claiming that general effect must be registered as such and judged on fresh blocks. u6x stays the incumbent.
- 01:02: Replica: u7x (SEEK_IDLE 20 + IDLE_SHIFT) submitted (#212) for block 1 on set 3, 16 scrims after 13148 (waiter build/wait_u7xb1.sh). Queue after it: u7n block 2 on set 3. Screens: zd25 (u8a) running, zd26 (u8b) chained.
- 01:15: u8a (u7x + ALERT_SIDE) REJECTED at screen zd25 on both falsifiers: 35 wins vs the 37 bar (zc6 32; vs u7x: +4 abrk tiny B, araid DefaultSmall B, evileye A, tiny B; -6 abrk bed A/B, araid DefaultMedium A, DefaultSmall A, arrows B, evileye B), delivered 336.8k vs the 348k bar, 0 overruns, 3 starved kings. The pair is worse than either half. ALERT_SIDE pulls war rats to the king's threatened side, and the idle shift spreads gatherers further out; together they lose the small araid and abrk maps, where the king's defence and the pick both need the same rats. The near-miss-pair step now has u7u (u7n + RETREAT_HP 0) and u8a, both worse than their halves.
- 01:16: Local disk at 910 MB free (97%): deleted the 132 build/dumpcache entries older than 2 h (1.07 GB of regenerable replay dumps, chmod -R u+w first), list in build/dumpcache_deleted_20261104-0116.txt; now 1.9 GB free. Local game outputs (build/ftc*, build/rose) and replays kept. Screen zd26 (u8b, FIGHT_IDLE 40) launched by the chain.
- 01:29: Home-mine census of u7i's set-3 block (48 games, econ_report over 13133-13148; scratch neverworked.py). Losses (25 games): opponents took 49.3k from our half against our own 53.2k; 15 of our 85 home mines never had one pickup of ours, and of the 28.5k they spawned the opponent took 18.1k, mostly starting early (keepout (21,47) and (28,47) from r31-34, Stash (15,11) r47, uneruesansfin (6,6) r47, cheesebottles (19,2) r88, wallsofparadis (27,28) r167, FreeTheCheese (48,33) r295). Wins (23 games): opponents took 15.1k from our half against our 160.9k, and 2.5k from never-worked mines. FreeTheCheese vs Merlin (13147 g0): Merlin picked 24.4k to our 7.0k, including 2860 from our unseen (48,33) and 7285 from dropped cheese. Locally, keepout as A vs araid (u7x): all six home mines are listed by r200 and 99% picked (symmetry ROT|VER all game, so firstSym is ROT, under which they are all ours). So the replica's never-worked mines are mostly taken from us, not undiscovered: guarding contested home mines is the gap, which is what FIGHT_SHIFT (u7z/u8b) moves toward.
- 01:38: u8b (FIGHT_IDLE 40) REJECTED at screen zd26 on falsifier 3: delivered 345.4k vs the 360k bar; wins 37 (bar 37, pass), 0 overruns, 3 starved kings (vs u7z: +1 araid DefaultSmall B, -3 abrk evileye B, araid evileye A, starvation A). Dose series so far: fighters' idle count 40 gives 345k delivered and 37 wins, 20 (u7z) gives 379k and 39. Delivered across zd21-zd26 ranges 337-379k, so u7z's total is the high outlier: either the dose matters steeply or u7z drew well. u8c (FIGHT_IDLE 10) is the next arm on screen zd27, with u8b's bars: wins ≥ 37, overruns 0, delivered ≥ 360k.
- 01:58: u7x (SEEK_IDLE 20 + IDLE_SHIFT) block 1 on set 3 (#212, 13149-13165, 16/16) vs u6x's set-3 refs 12130-12163: 48 cells, 40 alike, D 8 (+5: Icarus averyfineline A, Xzibit minimaze A, uneruesansfin A, Varun corridorofdoomanddespair B, collegejunior FreeTheCheese A; -3: Finals where_are_you A, YokoOno Stash A, spsquared TheHeist A), net +2, t +0.71: continues to block 2 (set 1, identity-aware against the twin). The set-3 reference shares its noise across candidates: minimaze A, corridorofdoomanddespair B and FreeTheCheese A were also gained by u7i and/or u7q, and where_are_you A, Stash A and TheHeist A also lost by u7i, u7o or u7q. Those six cells look like unlucky and lucky single draws of u6x, so set-3 blocks are partly reading the reference. The replica falsifier carried from u7w's registration (nearest-mine rat-rounds to r400 at least 15% below 1774; ranks 3-5 in losses above 221) is running (mineocc2 on 13149-13165 and on u6x's refs). u7n (ALERT_SIDE) submitted (#213) for block 2 on set 3 after 13170 (waiter build/wait_u7nb2.sh); u7x's set-1 block follows.
- 02:08: **u8c (FIGHT_IDLE 10) REJECTED at screen zd27 (falsifier 3, delivery)**: 38 wins vs zc6's 32 (+8: abrk canyoudig B, araid DefaultMedium B, arrows A/B, cheesefarm A/B, evileye B, starvation B; -2: abrk evileye B, araid DefaultMedium A), 0 overruns, but delivered 359,056 (bar 360k). The FIGHT_IDLE dose series is flat within screen noise: idle turns 10/20/40 gave 38/39/37 wins and delivered 359k/379k/345k, against u7x's 37 wins and 348k with no fighter shift. The FIGHT_SHIFT line closes.
- 02:08: **u7x REJECTED on its replica mechanism falsifier** (the bar carried from u7w's registration at 01:58, before the occupancy data were in; mineocc2 to r400 on 13149-13165 vs u6x's 12130-12163). Nearest home mine: 1541 rat-rounds a game against 1774, -13.1% (bar: at least -15%, at most 1508): FAIL. Ranks 3-5 in losses: 351 against 221: pass. The idle shift moves some rats off the crowded mine, but less than its registration claimed, and the block-1 cells it gained (+5 -3) are mostly the set-3 cells that other candidates gain too. The u7x lineage (u7y, u7z, u8a, u8b, u8c) is closed.
- 02:08: **How we lose, u6x's reference blocks** (set 3 12130-12163, set 1 twin 12771-12813, set 2 12838-12899; 144 games): 77 losses, 66 of them with every king of ours destroyed (22 before r400, 44 later), and 11 on points at r2000. Every opponent is in band (Merlin 5-13, YokoOno 7-11, Xzibit 8-10, Icarus 8-10, Finals 9-9, collegejunior 9-9, Varun 10-8, spsquared 11-7). On set 3 (klose), our last king starved in 15 of 26 such losses (median r499), was bitten in 6 (r425) and hit by thrown rats in 5 (r1071). Starvation follows a lost rat war: averyfineline vs Icarus (12160 g0, lost r211) cost us 58 babies by r121 (2230 cheese of spawns against 260 picked), and Icarus laid 50 rat traps, 47 of which fired on our rats (2098 HP, 13 kills; mean life 2.0 rounds, so they are laid in front of a rat about to step). Our kd was 0.55 to their 1.82. hpcause (scratch) is summing HP lost and deaths by cause over set 3.
- 02:08: Replica order: u7n block 2 on set 3 (playing), then a **u6x twin on set 3** (measurement repair, plateau step 1). Set 3 is the only set without a twin: every candidate since 11-03 is read against one draw of u6x there, and six of its cells were gained or lost by several unrelated candidates (01:58). After the twin, set-3 blocks get identity-aware scores, as sets 1 and 2 do.
- 02:21: **Why the rat war is lost** (u6x set 3, 48 games; scratch hpcause.py and ecotraj.py over battle_report/econ_report):
  - Kills are even in losses: 60 of our babies die a game and 60 of theirs. Their rat traps are the one weapon we lack. In losses they lay 35 a game and 33 fire on us, 1519 HP and 10 kills a game. Finals lays 131 (43 kills a game), Varun 59, Icarus 36, YokoOno 25. We lay none (BABY_RAT_TRAPS off). Engine (TrapType, GameWorld): a rat trap costs 20, hits for 50, freezes movement 3 turns, and fires on an enemy that moves within dist2 2 of it. Enemy traps are invisible to us, and a fired rat trap counts as a backstab by its owner. Our own trap line (u0q, u0y, u3d, u4m, u4x, u5o) is all neutral or rejected.
  - Bodies and income, in losses: r1-100 we spawn 34 (1340 cheese) to their 27 (869) and bank 1066 to 1471. From r200 we hold 15-18 babies to their 17-26, and pick about 500 a 100 rounds to their 950-1570, while our bank idles at about 550 and theirs climbs 1430 to 2950. In wins we field 24-25 to their 20 and pick as much or more.
  - Stash vs YokoOno (12153 g1, last king starved r452): we picked 280 cheese all game to their 5915. They took 2680 of the 2860 spawned at our three mines, with 1534-2284 rat-rounds at each to our 76-189. The map's two mine rows are 7 apart, and our babies (39 spawned) never hold them.
  - Ladder (replica): we are 24th at 1576; IvanGeffner_basic51 tops it at 1917, and our in-band panel sits at 1560-1694. Against abcdezyxwv_bestsofar (6th; 164 matches with older builds, 48 of 492 games won), on pipes they grow from 15 babies at r100 to 38-48 from r300 with a 2000-2900 bank, spending 1000-1600 per 100 rounds on spawns and 140-200 on traps, and pick 1400-1900 per 100 rounds to our 450-790 at 18-21 babies.
  - The earlier bank and cap doses (u2v RICH_BANK 500, field -5; u3h 1500, local reject; u6e reserve, local -11) say bodies bought below the bank threshold die without adding delivery. So what the field adds over us is per-rat effect (traps, gathering), not the count.
- 02:21: Capability sweep (RobotController methods the bot never calls): adjacentLocation, canMoveForward, canSenseRobot(AtLocation), disintegrate, get*CooldownTurns, getAllPartLocations, getBackstabbingTeam, getMapWidth/Height (we use G), getTeam, isBeingCarried/Thrown, isLocationOccupied, moveForward, resign, senseRobot, indicator and timeline calls. Every game action is used, so the sweep finds no new capability.
- 02:21: The VM was idle after zd27, so two field-style retests on u6x, both existing switches last tried on far older bases:
  - **u8d = u6x + RICH2_BABIES 80** (cap 80 at a 3000 bank; abcdezyxwv's mass army). It reached the replica only inside u0z's bundle. Screen zd28; falsifiers: wins below 31 or any overrun; on the replica, starvation as our last king's death in more than 18 of a block's king-destroyed losses (u6x: 15 of 26).
  - **u8e = u6x + FORM_MINE** (structural: the second king forms on a home-half mine; abcdezyxwv's extra kings on mines). Last tried as t0i on s9s (net -3, one block). Screen zd29, chained; falsifiers: wins below 31 or any overrun; on the replica, fewer than half of its second kings within Chebyshev 2 of a mine.
- 02:42: u8d (RICH2_BABIES 80) PASSED screen zd28: 33 wins vs zc6's 32 (D 1: +1 araid cheesefarm A), 0 overruns, delivered 364.6k vs 354.8k, 3 starved kings. As expected it is nearly inert against the archetypes: only cheesefarm banks 3000. It joins the replica queue after the u6x twin's set-3 block. zd29 (u8e, FORM_MINE) is chained.
- 02:45: **u7n (ALERT_SIDE) REJECTED at replica block 2** (#213, set 3: 13179-13232, 16/16) vs u6x's 12130-12163: 48 cells, 44 alike, D 4 (+1: Varun corridorofdoomanddespair B; -3: Xzibit minimaze B, YokoOno 5t4rv4t10n_1337 B and Stash A), net -2, t -1.00. Pooled with its identity-aware block 1 (+4 -2): D 10, net 0, t 0, under block 2's bar of 1.
- 02:45: **Twin u7p submitted (#214) for the set-3 reference** (measurement repair): 16 scrims queued after 13232 (waiter build/wait_u7pr3.sh). Replica queue after it: u8d (RICH2_BABIES 80, screen passed), then u8e and u8f if their screens pass.
- 02:45: **u8f pre-registered (structural: combat doctrine, the field's trap style) = u6x + BABY_RAT_TRAPS + TRAP_ADJ + TRAP_SPAWN_FIRST + TRAP_CAP 25 + RAT_TRAP_MIN_CHEESE 300**, with no opening floor. u4m had the same switches with a 500 floor, a 1500 opening floor to r150 and cap 10 (block 1 t -0.30), so it laid little where the field lays most. Screen zd30, chained after zd29. Falsifiers: wins below 31 or any overrun; on the replica, fewer than 10 of our traps fired a game, or starvation ending more than 18 of a block's king-destroyed losses.
- 03:01: **u8e (FORM_MINE) REJECTED at screen zd29 (falsifier 1)**: 29 wins vs zc6's 32 (bar 31), D 9 (+3: araid DefaultMedium B, cheesefarm A/B; -6: abrk evileye B, araid DefaultMedium A, DefaultSmall A, jail A, starvation A, wallsofparadis A), 0 overruns, delivered 304.6k vs 354.8k (-14%), 7 starved kings (zc6 3). A second king on a far mine starves itself and splits the army, as t0i did on s9s: abcdezyxwv's extra kings come out of a 2000+ bank, ours out of a few hundred. zd30 (u8f, traps) follows on the VM.
- 03:17: **u8f (field-dose reactive traps) PASSED screen zd30**: 33 wins vs zc6's 32, D 11 (+6: abrk canyoudig B, tiny B, araid canyoudig B, cheesefarm A, starvation B, tiny B; -5: abrk arrows B, cheesefarm A, araid DefaultSmall B, bed B, starvation A), 0 overruns. Delivered 281.4k vs 354.8k (-21%), picked 362k. Much of that is shorter games: wins that end sooner (abrk cheesefarm B r1092 vs r2000, araid DefaultMedium A r288 vs r892, Stash A r645 vs r1062). Part is real: keepout A lost 8.7k and wallsofparadis B 7.6k over full 2000-round games. Its delivery bar sits on the replica (the starvation falsifier). Being structural, it goes first in the replica queue after the twin, then u8d.
- 03:17: **u8g = u8f + TRAP_NO_GATHER** (new one-line gate in Baby: gatherers lay no rat traps, so their actions stay on pickups). Screen zd31; falsifiers: wins below 31 or any overrun; against zd30 it is kept only with delivered above 300k.
- 03:31: **Twin u7p set 3 done** (#214, 13248-13291, 16/16). Against u6x's set-3 draw 12130-12163: 48 cells, 39 alike, D 9 (+3: Xzibit clouds B, minimaze A, collegejunior FreeTheCheese A; -6: Finals where_are_you A, Icarus averystrangespace A, YokoOno 5t4rv4t10n_1337 B, spsquared TheHeist A/B, rose B), net -3, t -1.00. u6x's set-3 draw ran about three cells lucky. The cells unrelated candidates kept "gaining" (minimaze A, FreeTheCheese A) and "losing" (where_are_you A, TheHeist A) are the twin's own flips (01:58). From now on set 3 is scored `paired 13248-13291 CAND --crown 12130-12163`, like sets 1 and 2.
  - Re-anchored earlier set-3 blocks: u7n block 2 +1 -3 (unchanged, t -1.00), u7x block 1 +5 -2 (t +1.13, was +0.71), u7i block 2 +3 -1 (t +1.00, was +1.63). u7i pooled with its set-1 block (+6 -2) comes to D 12, net +6, t +1.73: under block 2's accept bar of 2, so its rejection stands without the falsifier. u7x's mechanism falsifier still decides it.
- 03:31: **u8f submitted (#215), block 1 on set 3** (twin-anchored): 16 scrims queued after 13291 (waiter build/wait_u8fb1.sh). u8d follows.
- 03:36: u8g (u8f + TRAP_NO_GATHER) passed screen zd31 on both bars, barely: 31 wins vs zc6's 32 (bar 31), D 17 (+9 -8), 0 overruns, delivered 312.7k (bar 300k; u8f 281.4k, zc6 354.8k), 1 starved king. Barring gatherers from traps recovers about half the delivery and costs two wins against u8f's 33. It waits as the follow-up arm. If u8f's field block shows its traps firing (falsifier: at least 10 a game) and starvation is up, u8g takes the next slot; otherwise u8d does.
- 03:46: Local check of u8f's traps (build/u8fk: u8f as A vs araid on keepout, won 50-49 on points at r2000): we laid 30 rat traps and 22 fired (hit rate 0.73, mean life 11.9 rounds), 878 HP and 9 kills, against araid's 39 laid and 29 fired (their mean life 2.5). Pickups 1127 to araid's 1175; 0 overruns, max 15.9k bytecode on a rat. The trap code works as registered; its replica block 1 (#215) is playing. Next diagnostic: how much cheese our dead rats drop and who picks it up (scratch drops.py over the u6x twin's set-3 block).
- 03:54: **Dropped cheese is not the gap** (u6x twin, set 3, 13248-13291, 48 games; scratch drops.py over econ_report). Losses (32 games), per game: we pick 4352 cheese to their 9825, 509 of ours and 2073 of theirs from drops. Our dead rats drop 929 and theirs 2018, so even if they took every pile we dropped, drops explain under a fifth of the 5.5k pick gap. The rest is mines. The biggest gaps are Finals (7.2k to 21.6k a lost game) and nzjt (5.5k to 17.7k). Even in our 16 wins they pick more (11.2k to 10.0k), so we win on cat damage and kings, not cheese. A drop-pickup or deliver-sooner change has little room; the next lever has to be mine income.
- 03:56: **u8h = u6x + RUSH_BURN 6** (existing switch, never on the replica alone; doctrine: the production mix follows the outcome). Once enemy rat traps fire on us, a rusher more than Chebyshev 6 from the enemy start turns gatherer. Mechanism: the king-loss census of the u6x twin's set-3 block (scratch klose.py, 13248-13291) has our last king starving in 15 of 23 king-destroyed losses (median r437; bites 4, thrown rats 4). The fastest, averyfineline vs Icarus (13288 g0, starved r209): by r100 we spawned 53 babies for 2110 cheese and picked 220, while Icarus spent 910 on traps (about 45). Until r300 every replacement born with 4-15 babies alive is a rusher, so the rush keeps feeding the trap line. Locally (u1w, u3b) the switch was weak against u0q, whose traps are not the field's. Screen zd32 launched. Falsifiers: (1) zd32 wins below 31 or any overrun; (2) on the replica, our cheese picked by r400 a game not above u6x's same-set reference; (3) starvation ending more than 18 of a block's king-destroyed losses.
- 04:19: u8h (RUSH_BURN 6) PASSED screen zd32: 36 wins vs zc6's 32, D 10 (+7: abrk canyoudig B, araid DefaultMedium B, canyoudig B, cheesefarm B, evileye B, starvation B, tiny B; -3: abrk DefaultSmall A, araid DefaultSmall B, starvation A), 0 overruns of ours (the 10 in the run are the archetypes'), delivered 358.1k vs 354.8k, 2 starved kings. The archetypes lay few traps, so the stand-down seldom fires here; its test is the field. It joins the replica queue.
- 04:19: **u8f (field-dose reactive traps) block 1 on set 3** (#215, 13304-13361, 16/16, twin-anchored: `paired 13248-13291 --crown 12130-12163`): 48 cells, 43 alike, D 5 (+3: Varun corridorofdoomanddespair B, thunderdome A and B; -2: YokoOno 5t4rv4t10n_1337 A, Stash A), net +1, t +0.45. It continues. Falsifiers, both held:
  - Our rat traps fired 38 a lost game (39 laid) and 21 a won game, against the bar of 10. They did 1547 HP and 13 kills a lost game to the opponent. Enemy traps did 1055 HP to us a lost game, against 1519 for u6x's set-3 losses, and laid 25 against 35 (scratch hpcause.py).
  - Starvation took our last king in 17 of 25 king-destroyed losses, against the bar of 18 (median r336; the twin had 15 of 23 at r437).
  - Block 2 on set 1 is queued after 13361 (4 sent, 12 behind the hourly limit; waiter build/wait_u8fb2.sh).
- 04:19: whatsthecatdoin vs Merlin (twin 13290 g2, starved r342): we picked 423 to Merlin's 2688. Of our two home mines, (36,2) was never worked by us (Merlin took 440 from it, first at r54). At (14,20) Merlin had 1175 rat-rounds to our 782 and took 520 to our 280. Merlin spent 1135 on digs by r250. A local u6x vs araid game on the map is running on the VM (build/wtc1) to see whether our gatherers can reach (36,2) at all.
- 04:24: u6x's replica results by dig need (scratch digmaps.py; u6x's six reference blocks, 360 games; build/census.tsv minesFree/digHome): maps with a home mine reachable without digging 101-109 (0.48); one dig 20-15; FreeTheCheese (two digs) 1-7. The maps where the census reaches no mine at all go 3-13: canyoudig 0-4, externalsustenance 0-4, mincemeat 0-4. On externalsustenance (24 mines on the border, 12k of our half's cheese never picked by either side) both sides pick little. The split is the opening spend: r1-100 we spent 1840 on 46 spawns and 210 on cat traps against YokoOno's 1410 and 80, so at r100 we banked 455 to their 1015, drained at about 1.5 a round, and starved at r460 (13263 g2). Locally whatsthecatdoin's far home mine (36,2), 33 from our start and 34 from theirs, is never worked by u6x or by araid's twin (build/wtc1 on the VM); on the replica Merlin digs to it by r54.
- 04:24: **u8i = u6x + LEAN_DEBT 1200 / LEAN_FLOOR 800 / LEAN_MIN 4** (new switch, structural: spending doctrine). Once the king's spawn spending runs more than 1200 ahead of the income it has measured since round 1 (Econ.total, new), a spawn must leave 800 banked (4 or more babies alive). Rats that die before paying for themselves are not replaced out of the king's upkeep, and spawning resumes as income catches up. Unlike u6e's flat reserve (screen -11), it fires only where spending outruns income: u6x's in-band losses average 1340 spent to 301 picked by r100 (debt about 1040), wins 1232 to 338, against 1605 on externalsustenance and 1430 on averyfineline by r50. Screen zd33 launched. Falsifiers: (1) wins below 31 or any overrun; (2) on the replica, starvation ending more than 12 of a block's king-destroyed losses (u6x twin 15 of 23); (3) our median bank at r150 in losses not above u6x's same-set reference.
- 04:43: **u8i (LEAN_DEBT) REJECTED at screen zd33 (falsifier 1)**: 30 wins vs zc6's 32 (bar 31), D 8 (+3: araid DefaultMedium B, canyoudig B, starvation B; -5: abrk bed A/B, araid bed A, DefaultSmall B, starvation A), 0 overruns, delivered 366.0k vs 352.9k, 1 starved king. The losses are short rush games where the king held 800 with the enemy at its ring.
- 04:43: **u8j = u8i + LEAN_SIEGE** (new gate): the lean floor stands aside at war with enemies in the king's sight, as WAR_FLOOR does. The field starvations it targets (externalsustenance, averyfineline, canyoudig) overspend in the opening with no enemy at our king. Screen zd34 launched, with u8i's falsifiers.
- 04:48: **The rush is a main win source; the mid-game is where we lose** (u6x's six reference blocks, 285 unranked games against the in-band panel, by match.json):
  - Wins (136): 34 are king kills before r300, 28 at r300-999, 15 later, and 59 on points. The early kills are spread across the panel: Varun 8, Finals 7, Xzibit 7, YokoOno 7.
  - Losses (149): 25 are kills before r300, 73 at r300-999 (mostly starvation), 27 later, and 24 on points.
  - Stash vs YokoOno (13264 g1, we are B): r1-100 YokoOno picked 1000 to our 300 with 27 spawns to our 32, and spent 600 on spawns r101-200 while banking 1970. By r414 they had 1037, 1139 and 716 rat-rounds at our three mines to our 628, 786 and 998, and they took 1360 there. Locally (VM build/st1, st2) u6x beats araid on Stash in both seats, picking 400-600 by r100 and 11-16k in the game.
  - So the opening's 12 rushers (spawn index 4-15) buy the early kills and cost the opening economy. The cut-the-rush lines (RUSH_TO 12, net -7; RUSH_UNTIL 200) lost. The lever left is recovery after a failed rush: u8h (stand down under traps) and u8j (lean spending).
- 05:05: u8j (u8i + LEAN_SIEGE) PASSED screen zd34: 34 wins vs zc6's 32 (u8i 30), D 10 (+6: abrk canyoudig B, araid DefaultMedium B, arrows B, evileye A/B, starvation B; -4: abrk bed B, evileye B, araid DefaultSmall A, starvation A), 0 overruns, delivered 369.5k vs 352.9k, 1 starved king. The siege exemption recovers the rush maps u8i lost. It joins the replica queue.
- 05:05: u8k = u8j with LEAN_DEBT 800 (dose arm). At 1200 the lean starts too late for canyoudig vs Xzibit (12877 g1): we spent 1670 on spawns by r100 against 445 picked, so the debt passed 1200 only near r95 with 415 banked. Screen zd35 launched. Falsifiers: wins below 31 or any overrun; kept over u8j only with at least 34 wins.
- 05:10: **u8f (field-dose reactive traps) REJECTED at replica block 2** (#215, set 1: 13376-13442, 16/16, twin-anchored `paired 12771-12813 --crown 11549-11677`): 48 cells, 42 alike, D 6 (+2: Icarus rose B, YokoOno clouds B; -4: Finals tiny A and B, YokoOno Stash A, spsquared bed B), net -2, t -0.82. Pooled with block 1 (+3 -2): D 11, net -1, t -0.30, under block 2's bar of 1. Our traps fire (38 a lost game on set 3) and enemy trap damage fell by a third, but the cheese they cost (39 a lost game, about 780) comes out of the same bank that starves. The trap line (u0q, u0y, u3d, u4m, u4x, u5o, u8f) stays closed; u8g (u8f without gatherer traps) is dropped with it.
- 05:10: **u8j submitted (#216), block 1 on set 3** (twin-anchored `paired 13248-13291 --crown 12130-12163`): 16 scrims queued after 13442 (4 sent, 12 behind the hourly limit; waiter build/wait_u8jb1.sh). Replica queue after it: u8h (RUSH_BURN), then u8d (RICH2_BABIES 80).
- 05:18: Local disk at 1.1 GB free (97%): deleted the 72 build/dumpcache entries older than 2 h (680 MB of regenerable replay dumps, chmod -R u+w first), list in build/dumpcache_deleted_20261104-0517.txt; now 1.7 GB free. Local game outputs and replays kept.
- 05:28: u8k (u8j with LEAN_DEBT 800) SUPERSEDED at screen zd35 (falsifier 2): 31 wins vs zc6's 32 and u8j's 34 (vs u8j: +2 abrk evileye B, araid cheesefarm B; -5 abrk arrows B, araid DefaultMedium A/B, arrows B, evileye B), 0 overruns, delivered 346.1k vs u8j's 369.5k, 3 starved kings. At 800 the lean fires in ordinary openings on the medium maps. The dose stays at 1200 (u8j, on the replica).

## 2026-11-04 06:13 PST: u8j rejected on its starvation falsifier; u8h on the replica

- 06:13: **u8j (u6x + LEAN_DEBT 1200 + LEAN_SIEGE) REJECTED at replica block 1 on falsifier 2** (#216, set 3: 13450-13508, 16/16, twin-anchored `paired 13248-13291 --crown 12130-12163`): 48 cells, 43 alike, D 5 (+3: Icarus averyfineline A, YokoOno 5t4rv4t10n_1337 B, Varun corridorofdoomanddespair B; -2: Xzibit clouds B, YokoOno Stash A), net +1, t +0.45, which clears block 1's bar. But starvation was our last king's death in 15 of the block's 26 king-destroyed losses (bar: 12 or fewer; u6x's twin: 15 of 23), median starve round r779 (u6x r437): the lean delays starvation, it does not prevent it. Falsifier 3 held: median bank at r150 in losses 870 vs u6x's 844.
- 06:13: Diagnosis while u8j played:
  - Stash (13264 g1 vs YokoOno; local debug build zdbg vs agrab on the VM, build/stdbg1): at r100, 18 of our 25 rats are still inside the walled home pocket, while YokoOno has 14 of 21 on its own mines. Both pocket exits were dug open by r25. A flow path is dropped when a robot holds its next tile past FLOW_HOLD or a newly seen wall cuts it, and each re-search takes about 20 turns while the rat cycles a 4-tile loop (attacker 14045: searches r22-31, r37-57, r65-73; out at r78). Attack targets also flip between (14,26) and (15,26), the ROT and HOR mirrors of our king (both still alive), and each flip resets the bug walk (NAV_SAME_R 0). King formation pulled 17 rats (attackers too) to a site in the pocket at r100 locally, but it is only about 1% of rat-turns in the field game (tag channel, tools/smuggle.py).
  - Per-map record over 11549-13442 against the expectation from our overall rate vs each opponent met there (replica games are deterministic, so these are fixed cells): DefaultMedium 0-44 (22.2 expected: Varun 0-26, thewaxmango 0-18), starvation 0-26 (Xzibit, 15.3), thunderdome 11-39 (23.0), dirtpassageway 2-30 (thewaxmango 0-26), arrows 0-26 (YokoOno), mincemeat 0-18 and averyfineline 1-17 (Icarus), jail 0-26, keepout 0-18 and wallsofparadis 0-18 (Finals), cheesebottles, whatsthecatdoin and FreeTheCheese 0-18 (nzjt). Best: corridorofdoomanddespair +20, rose +18, TheHeist +16, sunandmoon +14.
  - DefaultMedium vs Varun (13396 g0) was a king race: our king died at r178 with theirs at 2 HP (we killed 44 rats to their 37). Enemy rats camp at our king from about r50-100 in these losses (8-11 of theirs against 0-3 of ours).
  - Economics: a mine spawns 20 cheese with chance 1 - 0.99^k (k = rounds since its last spawn), about every 12.6 rounds; mincemeat measured 2.3-2.6 cheese a round per mine. A king eats 2 a round, so on one- or two-mine maps the 2500 starting bank is the main resource. averyfineline (13288 g0): we spawned 58 babies by r125 for 2210 cheese, picked 260 and lost the rat war (kd 0.64 to their 1.56); their king also starved for 26 rounds. mincemeat (13289 g2): Icarus kept 700-900 banked all game and took two thirds of the middle mines' cheese; u8j's debt (spawn spend minus income) never passed 1040 there.
  - 86% of our losses in 11549-13442 are king kills (527 of 616), spread over the game: 173 by r350, 88 in r350-500, 184 in r750-1500. Our king-kill wins are early (93 of 326 before r150).
- 06:13: **u8h submitted (#217), block 1 on set 3** (twin-anchored `paired 13248-13291 --crown 12130-12163`): 8 scrims sent after 13520, 8 queued behind the hourly limit (waiter build/wait_u8hb1.sh). Replica queue after it: u8d (RICH2_BABIES 80).
- 06:29: **Kings that die holding cheese** (scratch kingrace.py, deathbank.py). 13396 DefaultMedium vs Varun (0-26 for us on that cell pair) was lost by about five rounds: our king stood at (17,16) with 3-4 enemy biters on its ring from r135 and died at r178 (56 bites), holding 160-320 with 8 babies, because the 300 spawn floor blocked every spawn; Varun spent its bank to 0 by r155 on 18 babies and its king, hit by 12 of our throws, was starving at 2 HP. Over u6x's set-3 twin (13248-13291), 7 of 23 king-destroyed losses died with cheese banked: DefaultMedium and tiny vs thewaxmango with 100-330 and 6-10 babies (the floor), thunderdome vs Varun with 550-840 and 19-20 babies (the 18 cap) while our king was ground from 456 HP to 0 over r240-412 with the enemy king untouched. u8l = u6x + SIEGE_HURT 10 / SIEGE_HURT_FLOOR 20 / SIEGE_HURT_CAP 40 (new switch): for 10 rounds after our king's HP fell with an enemy baby within dist2 18, at war with enemies in sight, spawn floors drop to 20 and the cap rises to 40. Falsifiers: screen zd36 wins below 31 or any overrun; replica: median bank 20 rounds before a non-starvation king death not below u6x's same-set reference; starvation the last king's death in more than 18 of a block's king-destroyed losses. Screen zd36 launched (waiter build/wait_zd36.sh).
- 06:35: **VM replay prune** (owner rule from prompt #23): the replica VM's root fell to 2.1 GB free, so I deleted the 2001 replica replay files older than two days under /srv/bc26-galaxy/storage/bc26-replica-secure/episode/bc26/replays (`find -type f -mtime +1 -mmin +2880`). The list is in /root/bc26-replays-pruned-2026-11-04.txt and the deletion log in /root/bc26-replays-pruned-2026-11-04.log on the VM. Root went from 2.1 GB to 4.5 GB free. Our own fetched copies under bc26b-galaxy-replays/ are untouched.
- 06:57: **u8l (SIEGE_HURT) passed screen zd36**: 44 cells, 36 wins to zc6's 32 (+6: abrk canyoudig B and tiny B, araid DefaultMedium B, cheesefarm B, evileye A and tiny B; -2: araid DefaultMedium A, starvation A), transfer 353,211 to 352,884, 0 overruns, so falsifier 1 held. Replica queue: u8h block 2, u8d, then u8l.
- 07:02: **u8h (RUSH_BURN 6) block 1 on set 3** (#217, 13548-13585, 16/16, twin-anchored `paired 13248-13291 --crown 12130-12163`): 48 cells, 36 alike, D 12 (+7: Icarus mincemeat A and B, Xzibit uneruesansfin A, Varun corridorofdoomanddespair B, spsquared TheHeist A and B, collegejunior FreeTheCheese B; -5: Xzibit minimaze A, YokoOno 5t4rv4t10n_1337 A and Stash A, spsquared rift B, collegejunior FreeTheCheese A), net +2, t +0.58: it continues. Falsifiers held: cheese picked by r400 2214 a game against the twin's 2187 (losses 2301 vs 2054, the mechanism: rushers turned gatherers); starvation was our last king's death in 15 of 25 king-destroyed losses (bar 18; twin 15 of 23), median round r541 (twin r437). Block 2 on set 1 queued after 13585 (4 sent, 12 behind the hourly limit; waiter build/wait_u8hb2.sh). Screen zd37 launched for u8m (u8l with SIEGE_HURT 30, the dose bracket).
- 07:26: **Who takes each home mine's cheese** (u6x twin, set 3, 13248-13291; scratch minesplit.py, mineleft2.py, rank2.py over econ_report, mines ranked by Chebyshev from each king's start). Losses (32 games, 892 rounds on average): of our half's cheese we pick 40%, the opponent 22%, and 38% is never picked; of theirs they pick 68% and we 5%. Wins: 57% / 5% / 38% on our half, the same on theirs. By rank in losses, the opponent takes 44% of our second-nearest home mine (2313 a game) and we take 35%; on their own rank 2 they take 70% and we 12%. They first pick at our rank 2 at a median r107, we at r163; on their own rank 2 they start at r55 (in wins we start ours at r436). Rank 2 is never visited by us on cheesebottles, whatsthecatdoin and FreeTheCheese vs nzjt and uneruesansfin vs Xzibit, where they work it from r50-85. cheesebottles (39,17) sits in a walled bottle whose only way in winds from (33,14), about as far by path from either king; by r200 their rats were inside and ours had stood at its mouth. On u7x's block (SEEK_IDLE 20 + IDLE_SHIFT, 13149-13165) the rank-2 take on keepout went from 660 and 160 to 2400 and 2980 and on FreeTheCheese to 1000-3780, but the bottle mines stayed unworked. The arrows loss in 13393 was u8f's (its traps cost 1100 by r100), not u6x's: u6x's own arrows losses spend 1510 on 34 babies by r50, and from r150 the bank sits at the 300 floor with 8 rats against YokoOno's 12-16.
- 07:26: **u8m (SIEGE_HURT 30) screen zd37: 37 wins** against zc6's 32 and u8l's 36 (vs zc6 +7 -2), delivered 361,489, 4 starved kings (u8l 11), 0 overruns. Under u8m's registration it takes u8l's replica slot and u8l is superseded. Replica queue: u8h block 2 (playing), u8d, u8m. u8n (SIEGE_HURT 60, the next dose) is on screen zd38; it replaces u8m only with more than 37 wins and at least 361k delivered.
- 07:31: **Replica queue reordered**: u8h block 2 (playing), then u8m (screen 37 wins vs 32; it targets the kings that die holding cheese), then u8d (screen 33 vs 32; RICH2 fires only with 3000+ banked, which our losses rarely reach: median bank at r400 in losses 517-718). The cheese never picked at each team's nearest home mine is about equal (leftpiles.py, losses: ours 439 a mine older than the last 150 rounds, theirs 339), so it is not our gap; the gap is the theft at our rank 2 and the unworked far mines. **u8o = u6x + SCOUT_EVERY 2** (one gatherer in two scouts our half to r150, from one in four; the dose was never tuned since s3f on Oct 28) chained on screen zd39 after zd38. Falsifiers: screen wins below 31 or any overrun; replica: our median first pickup at our rank-2 home mine in a block's losses not earlier than the twin's r163.5; cheese picked by r400 below 2100 a game (twin 2187).
- 07:33: **u8h (RUSH_BURN 6) REJECTED at replica block 2** (#217, set 1: 13628-13666, 16/16, twin-anchored `paired 12771-12813 --crown 11549-11677`): 48 cells, 36 alike, D 12 (+6: Finals DefaultSmall B, Icarus rose B, Varun knifefight B, spsquared bed A, fullofsecrets B, superioristhmus A; -6: Icarus TheHeist A, YokoOno Stash A, Varun t_minus_five_minutes B, nzjt peaceinourtime A, collegejunior clockwisdom A and B), net 0, t 0.00. Pooled with block 1 (+7 -5): D 24, net +2, t +0.41, under block 2's bar of 1. The burn moved cheese (losses picked 12% more by r400 on set 3) but not results. **u8m submitted (#218), block 1 on set 3** (16 scrims queued after 13666, waiter build/wait_u8mb1.sh); u8d follows.
- 07:48: **u8n (SIEGE_HURT 60) SUPERSEDED at screen zd38**: 37 wins vs zc6's 32 (+7 -2), the same as u8m. Its bar was more than 37 wins and at least 361k delivered; it delivered 347.4k (u8m 361.5k), with 5 starved kings and 0 overruns. The hurt-window dose stays at u8m's 30, and zd39 (u8o) follows on the VM.
- 07:54: **Our rank-2 home mine is late because nobody sees it, not because nobody is sent** (u6x twin, set 3, 13248-13291; scratch rank2see.py adds first sighting to rank2.py: baby r2 20 in a 90-degree cone, king r2 25):
  - Over the 38 games with a rank-2 home mine, we first see ours at median r249 and they see theirs at r51. By r100 we have seen it in 14 games, they in 25. In losses the medians are r165.5 to r41.5.
  - Sighting and arrival coincide (near-see lag median 0), and the first pickup follows within a few rounds.
  - Losses: our rank-2 mines gave us 19,295 and them 24,565. Theirs gave them 39,020 and us 6,680.
  - Worst gaps (us vs them on mirrored ground): cheesebottles r1702/never vs r67/r78; averystrangespace r1443 vs r126; FreeTheCheese (Merlin) r479/never vs r29/r26; keepout r129/r304 vs r7/r42; thunderdome r87/r67 vs r19/r26; sunandmoon and clouds never vs r32-r54.
- 07:54: **Why: the opening runs to the centre** (keepout 13250 g0 vs AdamTan12_Finals, scratch seenmap.py and a first-babies trace):
  - All 16 of our first babies went west toward the enemy. Babies 1-4 are gatherers that know no mine, and explore() then ranks samples by closeness to the map centre (EXPLORE_CENTER_FROM 0, s3c's rule from Excavation). Babies 5-16 are rushers.
  - We first saw a mine at r35. By r60 we had seen 1037 tiles to their 1475. Their first babies fanned out north, south and east over their half, found 7 of their mines by r35, and one of them reached our rank-2 mine (46,13) at r51, before we did.
  - Census (scratch KingSight.java over the 54 panel maps): the king sees no mine at round 1 on every map but superioristhmus. So this is how every game opens: the first gatherers and the 12 rushers run the centre corridor, and our half's flanks wait for the 1-in-4 scouts. Every earlier scout variant (SCOUT_*, SWEEP, FRONTIER, SEEK_*) changed later scouting, not this no-mine fallback.
  - Tie-mine census (scratch TieCensus.java): mines equidistant from both kings sit on 6 panel maps: 5t4rv4t10n_1337 1/1, EscapeTheNight 7/11, canyoudig 2/2, closeup 2/8, mincemeat 3/3, starvation 1/3. MINE_TIES matters only where non-tie home mines also exist (starvation, EscapeTheNight, closeup).
- 08:06: **u8o (SCOUT_EVERY 2) REJECTED at screen zd39 (falsifier 1)**: 28 wins vs zc6's 32 (bar 31), D 12 (+4 -8), delivered 311.5k vs 354.8k (-12%), 8 starved kings (zc6 3), 0 overruns. Sending one gatherer in two to scout until r150 costs more income than its finds return, so the dose stays at 4. u8p changes only the no-mine fallback (before any mine is known), so it does not take gatherers off a known mine. Screen zd40 (u8p) is running.
- 08:06: **u8p registered** (bots/u8p/SNAPSHOT, 07:55, before its first game) = u6x + NOMINE_HOME: a gatherer that knows no mine scouts our half (exploreHome) instead of heading for the centre. Local keepout check (u8p vs araid, A, seed 0; seenmap.py): our rank-2 mine (46,13) seen at r26 (field u6x: r129/r304), first mine r26 (field r35), 1091 tiles seen by r60 (field 1037).
  - Two of the four opening gatherers drew home-half tiles near the middle and went west with the rushers: with our king near the east edge, most of our half lies toward the centre.
  - It won 62-37 but delivered 21.4k to araid's 25.6k, where u6x in zc6's same cell delivered 31.6k to 20.0k. A local u6x replay of that cell is running to compare the openings.
- 08:28: **u8p (NOMINE_HOME) passed screen zd40 at its bar**: 31 wins vs zc6's 32 (bar 31), D 17 (+8 -9), 0 overruns, 0 exceptions.
  - Every gain is on side B (8 of 8). The losses are 7 on side A (DefaultSmall twice, bed, tiny twice, Stash, jail) plus bed B and jail B. The archetypes' own side asymmetries decide which way tiny and bed fall.
  - Delivered 301.8k vs 354.8k (-15%), picks per 100 rat-turns 31.6 vs 35.6 (scratch xferdiff.py, per cell). Part of the drop is shorter wins: DefaultMedium A won at r271 (u6x r892), DefaultMedium B became a king kill at r127 (u6x lost at r871), learn_your_lesson A won at r1477. Among full 2000-round games, keepout A/B lost 10.3k/9.4k and wallsofparadis A/B 4.1k/4.7k, still winning all four.
  - Local keepout pair (u6x replay reproduces zc6's cell exactly): u6x saw one mine by r60 (r59), u8p four (r25-r51, the rank-2 mine at r26). But u8p's gatherers balanced over those near mines (12-17k rat-rounds at each for about 4.7k cheese), and araid took the two contested north mines (21,47) and (28,47) first (r139 and r357, about 9k). u6x's centre-first opening had found and held them (r322 and r499, 9.1k).
  - The field census has the same crowding: in losses we spend 7,912 rat-rounds a game at our rank-1 mine for a 64% share, they 1,784 at theirs for 76%.
  - u8p goes to the replica next, after u8m's block and ahead of u8d (RICH2 is nearly inert, screen D 1). Its falsifiers 2-3 test the mechanism (rank-2 sighting) and the opening's income (picked by r400).
- 08:30: **u8m (SIEGE_HURT 30) REJECTED at replica block 1** (#218, set 3: 13676-13818, 16/16, twin-anchored `paired 13248-13291 --crown 12130-12163`): 48 cells, 46 alike, D 2 (+1 Varun corridorofdoomanddespair B, -1 Xzibit minimaze A), net 0, t 0.00. Block 1 rejects at t <= 0.
  - 29 of its 48 cells replay u6x's crowning game exactly: the hurt-king spending window seldom fires against the field. Its 37-win screen came from the archetypes' sieges, which the field does not reproduce.
  - The SIEGE_HURT line (u8l, u8m, u8n) closes.
- 08:30: **u8p submitted (#219), block 1 on set 3** (twin-anchored; 12 scrims queued after 13818, the rest under the hourly limit; waiter build/wait_u8pb1.sh). Falsifiers to score:
  - rank2see.py median first sighting of our rank-2 home mine, against the twin's r249;
  - bankat.py picked by r400, against the bar of 2100.
  - u8d (RICH2_BABIES 80) waits behind it.
- 08:37: **u8q = u6x + SEEK_CROWD 3** (from 6; registered in bots/u8q/SNAPSHOT before its first game). It targets the crowding: 66% of our home-mine rat-rounds sit at the nearest mine (twin set 3, 412k of 625k over 42 games). A gatherer whose cone shows 3+ allies nearer its mine seeks never-seen home ground for up to 150 rounds and reports what it finds. At 6 the test seldom fires. Alone it was never screened against the archetypes or sent to the replica (s7q bundled it with MINE_ANY_COOP, t 0.53). Screen zd41 is running on the VM.
  - Falsifiers: wins below 31 or any overrun; on the replica, rank 1's share of our home-mine rat-rounds at 59% or more (twin 66%), or picked by r400 below 2100.
- 08:53: **Who stands at our nearest home mine** (u6x twin, set 3, 13248-13291; scratch r1roles.py decodes our turn tags within Chebyshev 4 of the rank-1 home mine). In losses, fighters doing mine work make up 34.5% of those rat-rounds (F:M 18.0%, F:W 16.5%) and gatherers 37.3% (G:M 19.6%, G:W 17.7%); wins are alike. Fighters and gatherers share one pick (cand[slot % m] over the known home mines), so the crowd means few mines are known, not a role mix. Our king stands within 4 of that mine half its rounds.
- 08:53: **Death causes are not the gap** (scratch deathcause.py, same block): in losses the opponent loses 66.3 babies a game to our 50.2, and 37.2 of theirs die to cats (pounce 26.5, crush 10.7) against 13.5 of ours. Our largest causes are thrown-rat landings (14.2), enemy rat traps (8.3) and bites (5.6).
- 08:53: **Points-decided losses turn on cat damage** (field_report games, same block). 9 of the 32 losses were decided on points. Holding everything else fixed, 6 of them flip with 25-1400 more cat damage, against 550-4850 more transfer (or none):
  - averystrangespace A vs Icarus +25 (cooperation to r1511);
  - rose B vs spsquared +300 (to r415): one cat trap stood all game and never fired, with the bank at 1100-1800, while spsquared's 2-3 traps took about 800;
  - corridorofdoom B vs Varun +625;
  - TheHeist A/B vs spsquared +700/+600 (no backstab; the game ended when the last cat died, 4000 damage each);
  - uneruesansfin A vs Xzibit +1400 (to r566).
  - The twin's cat-trap damage is 826 a game (median 200), and 1935 in the 10 games with 300+ rounds of cooperation.
  - CAT_LAY (traps on learned cat lanes) and CAT_RING (a reactive trap beside the cat's body, on the floor alone in cooperation) were tried only locally on s9s on Oct 30: 10 of 16 and 11 of 16 against s9s; screen +2.5 pts, t +0.64. They never reached the replica.
- 08:53: **u8r registered** (bots/u8r/SNAPSHOT, 08:52, before its first game) = u6x + CAT_LAY + CAT_RING, one doctrine: trap cats during cooperation. Screen zd42 is chained after zd41 on the VM.
  - Falsifiers: (1) screen wins below 31 or any overrun; (2) on the replica, cat-trap damage a game not above 1.25x the twin's same-set value (set 3: 826); (3) transfer a game below 0.9x the twin's (set 3: 4409).
- 08:57: **u8q (SEEK_CROWD 3) passed screen zd41**: 44 cells, 36 wins to zc6's 32, D 8 (+6: araid arrows A and B, cheesefarm A and B, evileye A, starvation B; -2: abrk arrows B, araid DefaultSmall B), 0 overruns, 0 exceptions. Delivered 348.4k to 354.8k (-1.8%; funnel over 48), picks per 100 rat-turns 36.94 to 35.56.
  - Per cell (xferdiff.py), most of the drop is wins that end sooner: araid cheesefarm B r920 vs r2000, evileye A r862, DefaultMedium A r455 vs r892.
  - Among full 2000-round games, delivery is about even: cheesefarm +1.1k to +2.5k, arrows A +1.0k, wallsofparadis A +1.2k; keepout A/B -2.0k/-1.4k, wallsofparadis B -0.8k.
  - Replica queue: u8p (block 1 playing), then u8q, then u8d. u8r's screen zd42 is running.
- 09:18: **u8r (CAT_LAY + CAT_RING) passed screen zd42**: 44 cells, 34 wins to zc6's 32, D 6 (+4: abrk canyoudig B and tiny B, araid canyoudig B and evileye B; -2: araid DefaultSmall B and bed A), 0 overruns, 0 exceptions.
  - The economy cost is real: delivered 316.3k to 354.8k (-11%), picks per 100 rat-turns 32.0 to 35.6. Full-length wins lost delivery: wallsofparadis B -8.2k, keepout B -6.8k and A -2.6k, learn_your_lesson B -2.4k.
  - Over 48 games, cat-trap damage was 44.5k to 16.7k and bite damage 124.6k to 158.1k, so total cat damage barely moved (169k to 175k). Rats lost to cats fell 25% (1,301 to 1,744), spawns fell 8%, and picks fell 10% on the same rat-turns. One reading: stunned cats linger near our rats and gatherers keep away from them.
  - Cooperation lasts about 40 rounds a game against the archetypes. So most of the extra traps here came from CAT_RING's beside-the-body option in war, laid in the window the backstabbed side keeps (100 rounds), not from CAT_LAY, which acts only in cooperation.
  - u8r joins the replica queue behind u8q. Its replica falsifier 3 (transfer at least 0.9x the twin's) tests this cost on the field.
- 09:18: **u8r's pair split** to find the costly half: u8s = u6x + CAT_LAY (screen zd43, running) and u8t = u6x + CAT_RING (zd44, chained after it). Both were registered before their first games, with a delivery bar of 340k added to the screen falsifier.
  - If either passes while u8r has not yet started on the replica, the one with more screen trap damage takes u8r's slot. If both pass, u8r stays.
- 09:18: Baseline for u8p's rank-2 falsifier on set 1 (rank2see.py over the twin 12771-12813): our rank-2 home mine is first seen at a median r28, with 25 of 38 games by r100. Set 3's twin: r249, 14 of 38. On set 1 u8p must beat r28.
- 09:36: **u8p (NOMINE_HOME) passes replica block 1** (#219, set 3: 13819-13834, 16/16, twin-anchored `paired 13248-13291 --crown 12130-12163`): 48 cells, 38 alike, D 10, net +2, t +0.63. It clears block 1's bar; block 1 never accepts.
  - Gained 6: spsquared TheHeist A and B, Varun corridorofdoomanddespair B, Xzibit uneruesansfin A, YokoOno 5t4rv4t10n_1337 B and externalsustenance B.
  - Lost 4: Xzibit minimaze B and uneruesansfin B, YokoOno 5t4rv4t10n_1337 A and Stash A.
  - Four of the gains are twin losses decided on points or by a narrow race. Both TheHeist games ended sooner, when the last cat died (r303 and r277 against the twin's r457 and r651), with our transfer just ahead (620 to 610, 629 to 460).
  - Falsifier 2 holds: our rank-2 home mine is first seen at a median r98 over 38 games (20 by r100), against the twin's r249 (14). In losses it moved from r165.5 to r91.5. The opponent's take from it fell from 24,565 to 13,740, and ours rose from 19,295 to 26,720.
  - Falsifier 3 holds: 2,313 picked by r400 a game (28 games reached r400) against the bar of 2,100 (twin 2,187).
  - Block 2 runs on set 1 (8 scrims sent after 13834, 8 queued under the hourly limit; waiter build/wait_u8pb2.sh). Its rank-2 bar there is the set-1 twin's r28.
- 09:42: **u8s (CAT_LAY alone) REJECTED at screen zd43** (falsifier 1, delivery): 44 cells, 34 wins to zc6's 32, D 4 (+3: abrk tiny B, araid canyoudig B and evileye A; -1: abrk evileye B), 0 overruns, but delivered 331.6k (bar 340k; zc6 354.8k).
  - The loss sits in a few long-cooperation araid cells, as in u8r: wallsofparadis B -8.6k (a 2000-round win, 14.3k to 5.7k) and keepout A -2.6k; Stash A and learn_your_lesson A were wins that ended sooner. 27 of 44 cells did not change.
  - Cat-trap damage was 24.5k to 16.7k and rats lost to cats 1,533 to 1,744. The lane traps cost delivery even where cooperation is short.
  - u8t (CAT_RING alone) runs as zd44. **u8u = u8p + SEEK_CROWD 3** (u8q's switch rebased on u8p, registered 09:39 before its first game) is chained as zd45. If u8p is accepted, u8u is u8q's rebase; if not, it is dropped.
- 09:42: Progress refreshed after u8p's block 1 (no warnings).
- 10:08: **u8t (CAT_RING alone) REJECTED at screen zd44** (falsifier 1, delivery): 44 cells, 34 wins to zc6's 32, D 6 (+4: abrk canyoudig B, araid DefaultMedium B, evileye A and B; -2: araid DefaultSmall B, bed A), 0 overruns. But it delivered 334.6k, under the 340k bar (zc6 354.8k).
  - The delivery loss is again in araid's long cells: wallsofparadis B -8.2k and A -6.0k (both still won), and Stash B -5.2k and A -4.2k (wins at r535 and r767 instead of r861 and r1062).
  - Cat traps 489 to 192, trap damage 42.8k to 16.7k, rats lost to cats 1,321 to 1,744, picked 419.7k to 442.1k.
  - Neither half of u8r's pair passed alone, so u8r keeps its replica slot, as registered. Its replica falsifier 3 (transfer at least 0.9x the twin's) tests the delivery cost on the field.
- 10:08: **Mine census of u8p's block 1** (13819-13834; scratch allsee.py and theft1.py over econ_report; home mines ranked by Chebyshev from each king's start):
  - Losses, per game: the opponent picks 5,798 on its own half and steals 1,766 from ours. We pick 3,487 on ours and steal 574 from theirs. They harvest 79/76/81/51% of their rank 1/2/3/4+ mines' spawn; we harvest 57/55/44/25% of ours. They take 27% of our rank-1 mine's spawn, where our king stands half its rounds.
  - Sighting medians in losses (never = 2000): our rank 1 at r35 vs their r18.5, rank 2 r91.5 vs r38, rank 3 r84 vs r35, rank 4+ r119.5 vs r105.
  - Theft by opponent in losses, per game: AdamTan12_Finals takes 6,525 from our side's mines while we take 5,540 there. On keepout A (13821 g0) they took 11,860 from our side; two of our six home mines, (28,47) and (46,42), we never worked; they took all 4,800 of (28,47) from r34. HugoIngelsson_YokoOno on Stash B (13824 g1): 3,080 stolen from r28, and we picked 80 on our side. nzjt's first steals come at r51-88, during cooperation.
  - Rat placement (scratch ratplace.py, rats within Chebyshev 4 of a home mine at r50-300): we have as many rats at our home mines as they do at theirs (losses r100: 5.8 vs 5.1; 20.1 rats to 17.0). Their rats harvest about twice as much per rat-round, on more mines.
- 10:08: **The opening has two gatherers, not four** (keepout 13821 g0 trace, tools/smuggle.py tags). Babies 1-2 gather and the rush starts with the 3rd baby (born r3, first turn r4). A newborn takes its first turn the round after its spawn, after the king has spawned the next baby, so the cost step it reads counts itself and that next baby. Baby k reads k+1, so idx 0 (designed as "0-3 babies alive") covers babies 1-2 only. On keepout our nearest home mine (47,30), 7 from our king, was first seen at r243, while Finals had worked it from r149.
- 10:08: **u8v = u6x + OPEN_IDX** (new switch, basics repair: economy and exploration). While the cost step still matches a spawn every round since round 1 (round 24 at most), a newborn's idx comes from the count before its own spawn, round - 2. Babies 1-4 then gather, 5-16 rush (still 12) and 17-20 fight or gather. Two more opening gatherers; the rush starts two rounds later. Registration in bots/u8v/SNAPSHOT; a local trace of the opening is running.
- 10:15: **u8p (NOMINE_HOME) REJECTED at its block 2** (set 1, 13852-13893, against the set-1 twin 12771-12813, crown 11549-11677): 48 cells, 37 alike, D 11 (+5, -6), net -1, block t -0.30. Pooled with block 1 (net +2, D 10): net +1, D 21, t 0.22, under block 2's bar of 1.
  - Gains: HugoIngelsson clouds B, VarunVejalla knifefight B, spsquared fullofsecrets A and B, superioristhmus A. Losses: AdamTan12_Finals tiny A, HugoIngelsson Stash A, nzjt peaceinourtime A, spsquared bed B, thewaxmango clockwisdom A and B.
  - u8u (u8p + SEEK_CROWD 3, screen zd45) is dropped, as registered. **u8q (u6x + SEEK_CROWD 3) takes the replica**: submission 220, block 1 on set 3 queued after 13893.
- 10:22: Screen zd45 (u8u = u8p + SEEK_CROWD 3, dropped with u8p, run for the record): 44 cells, 32 wins to zc6's 32, D 20 (+10 -10), 0 overruns, delivered 290.6k to 354.8k (-18%). With NOMINE_HOME under it, the crowd rule costs a fifth of the delivery; alone (u8q, zd41) it cost 2%. zd46 (u8v) is running.
  - u8p block 2's census, for the record: our rank-2 home mine first seen at median r70 in losses and r34 in wins (set-1 twin r28), so falsifier 2 fails too; picked by r400 averaged 2,417 in losses and 2,508 in wins (bar 2,100).
- 10:38: **u8v (OPEN_IDX) REJECTED at screen zd46** (falsifier 1, delivery): 44 cells, 36 wins to zc6's 32, D 10 (+7: abrk canyoudig B, tiny B; araid DefaultMedium B, canyoudig B, evileye B, starvation B, tiny B; -3: abrk DefaultSmall A, araid jail B, starvation A), funnel 38/48 to 34/48, 0 overruns, 0 exceptions. It delivered 319.2k, under the 340k bar (zc6 354.8k).
  - The drop comes from faster wins, not a weaker economy. The six largest drops are wins that came sooner: abrk Stash B r1235 to r457 (-9.6k), araid Stash A r1062 to r497 (-7.8k), jail A r2000 to r590 (-6.0k), DefaultSmall B r692 to r321. Picks per 100 rat-turns are 35.43 to 35.56.
  - **New rule from 10:38**: a screen's delivery bar is delivered per 100 rounds played over the cells (or picks per 100 rat-turns), never the screen's total, which shrinks when wins come sooner. It applies to candidates registered from now on.
  - Under "no rule applies backwards", OPEN_IDX reopens only on fresh blocks: as **u8w** (u8v's code under a new package), on the replica panel after u8q, with u8v's replica falsifiers 2 and 3 unchanged.
- 11:07: **u8q (SEEK_CROWD 3) block 1** (set 3, 13904-13940, 16/16, twin-anchored `paired 13248-13291 --crown 12130-12163`): 48 cells, 43 alike, D 5 (+4: Varun corridorofdoomanddespair B, spsquared TheHeist A and B, thewaxmango_collegejunior FreeTheCheese B; -1: spsquared rift A), net +3, t +1.34. Block 1 never accepts and rejects at t <= 0, so it plays on.
  - Falsifier 2 holds: the nearest home mine has 55% of our home-mine rat-rounds (ranks 1-6, both results; bar 59%, twin 66%). The crowd dispersed.
  - Falsifier 3 holds: picked by r400 averages 2,277 a game (17 losses 2,214, 17 wins 2,339; bar 2,100, twin 2,187).
  - Block 2 (set 1) is queued after 13971.
- 11:07: **How our kings die** (scratch kingdeath.py over king_report, 192 replica games: twin set 3 and set 1, u8p's two blocks; 115 losses, 77 wins).
  - The last king to fall in a loss: starvation 46, bites 29, thrown rats 19, cat scratches 6.
  - HP our kings lost in losses: starvation 31,570, bites 22,449, thrown rats 13,858, scratches 6,750.
  - In our wins, the opponent's last king died of starvation 24 times, thrown rats 9, scratches 5, bites 5. Whoever runs out of cheese first loses.
  - Early starvation with no enemy damage at all: mincemeat vs Icarus (4 games, r375-486), whatsthecatdoin vs nzjt (4, r310-342), averyfineline vs Icarus (4, r195-231, plus throws).
  - The ledger (econ_report) shows the shape. averyfineline 13831 g0, two mines: we spawned 58 babies for 2,220 cheese by r125 (37 of them in r1-50 under the 40 cap while the bank held 1,000) and picked 315. Icarus spawned 43 for 1,480, laid traps for 830, and picked 890. Our babies died to their traps (14), throws (22), bites (12) and cats (10).
  - mincemeat 13831 g2, three middle mines: we spawned 43 by r100 for 1,750 and picked 420; Icarus spawned 29 for 920 and picked 595. Cats crushed 13 of ours in r51-100 (7 of theirs).
  - whatsthecatdoin 13834 g2: 42 spawned by r150 for 1,700, 499 picked all game. nzjt threw 29 of our babies to death, picked 2,575, and took 460 of our far mine's 760 (we never picked it).
  - The spending fixes already tried only delay this (u8j: the starved kings' median death moved from r437 to r779, net +1). It is a fight and harvest loss on small maps, with the spending as the multiplier.
- 11:48: **u8q (SEEK_CROWD 3) REJECTED at its block 2** (set 1, 13973-14034, against the set-1 twin 12771-12813, crown 11549-11677): 48 cells, 42 alike, D 6 (+3 -3), net 0, block t 0.00. Pooled with block 1 (net +3, D 5): 96 cells, D 11 (+7 -4), net +3, t 0.90, under block 2's bar of 1.
  - Gains: AdamTan12_Finals DefaultSmall B, spsquared bed A and superioristhmus A. Losses: AdamTan12_Finals tiny A, VarunVejalla knifefight A, nzjt peaceinourtime A.
  - The crowd did disperse in block 1 (rank-1 share 66% to 55%), but the record did not move. Spreading the crowd within our half is now 0 for 4 on the replica (u3e never played; u8p NOMINE_HOME, u8q SEEK_CROWD 3 rejected; KMINE_SKIP neutral head-to-head).
  - **u8w (u6x + OPEN_IDX) takes the replica**: submission 221, block 1 on set 3 queued after 14036 (12 of 16 queued for the drain; waiter build/wait_u8wb1.sh). Queue after it: u8r, u8d.
- 12:06: **Where our baby-turns go** (u6x twin, sets 3 and 1: 96 replica games, rounds 51-400; scratch idlep.py, idletag.py, mking.py, yieldloc.py, pileinfo.py, nearking.py, firstsee.py on the VM).
  - Our turns within Chebyshev 4 of our own king are 36.1% of the total in losses (opponents 19.5%) and pick 12.8 per 100 turns (theirs 27.4). Our turns at home mines ranked 3 and 4+ pick 84.8 and 128.6 per 100. Over all turns, opponents pick 53.6 per 100 baby-turns in our losses and 32.6 in our wins; we pick 29.6 and 25.2. We lose when their economy runs, not when ours stalls.
  - The smuggled decision tags explain the near-king turns: 18.9% of all our loss baby-turns (16.5% in wins) are rats tagged M (walking to their mine) or W (wandering at it) within 4 of the king. They are a queue, not transit: 76% of those M turns and 90% of the W turns are still within 4 of the king 10 rounds later. The king's 3x3 sits on its mine, so a rat targeting it never gets within Chebyshev 2 and never runs the crowd test (u8q's SEEK_CROWD fires only at the mine). Idle turns: 15.2% of ours in losses vs 10.1% of theirs, half of ours at the king.
  - Theft: in losses they take 36.8 piles a game from our mines, we take 8.5 from theirs. 65% of the thefts come with none of our babies within 5 of the mine at the spawn. Our rank-2 and rank-3+ mines spawn 36% and 30% of their piles unattended, and their thieves take 77% and 52% of those. Their own unattended shares are 19% and 24%.
  - Discovery: in losses our rank-2 home mine is first seen at median r87 and first worked at r185; theirs at r38 and r61. In 25 of 75 game-sides they work their rank-2 mine 50+ rounds before we work ours, mostly because we never see ours by r600 (sunandmoon, clouds, cheesebottles, FreeTheCheese, whatsthecatdoin, thunderdome, minimaze). Ten earlier exploration switches were neutral or rejected (u8o, u8p last).
  - Opening losses (deaths to r150, deathcause.py): ours 22.3 a loss game vs theirs 16.8. The gap is their traps (3.8 a game of our deaths) and their bites (2.6 vs our 0.6).
- 12:06: **u8x = u6x + KMINE_SKIP 2 + SEEK_CROWD 3** (structural: who harvests the king's mine; registered in bots/u8x/SNAPSHOT before its first game). While a rat knows 2+ home mines it skips mines one of our kings holds (the king walks to every pile within 4 of its mine and banks it directly); any other mine with 3+ allies nearer sends it to seek new home mines. KMINE_SKIP alone moved the crowd to the next mine (u3e locally, never on the replica); SEEK_CROWD alone could not reach the king's queue (u8q, rejected at pooled t 0.90).
  - Funnel budget: M+W turns within 4 of our king from 17.9% to 10% or less; thefts per game to 0.85 of the twin's or less; picked by r400 up 5% or more.
  - Falsifiers: screen zd47 wins below 31, any overrun, or picks per 100 rat-turns below 33.8 (zc6 35.56); on the replica, the allocation stage failing, or the harvest stage failing while the allocation holds (kill criterion), or king HP lost to bites and thrown rats above 1.25 times the twin's per loss.
  - Screen zd47 runs on the VM (48 cells, chained after zd46; waiter build/wait_zd47.sh).
- 12:22: **u8y = u6x + KMINE_SKIP 2** (the attribution arm of u8x, without SEEK_CROWD 3) was registered at 12:08 in bots/u8y/SNAPSHOT. Screen zd48 runs after zd47 with u8x's bars. u8x goes to the replica if it passes its screen; u8y goes only if u8x fails and u8y passes.
- 12:22: **u8x mechanism check** (local, keepout vs araid, seed 0, as A: build/kx1 u8x, build/kx2 u6x; both won on points at r2000, 0 overruns; scratch kqueue.py and tagwin.py).
  - Rounds 51-400: M/W tags within 4 of the king are 10.5% of u8x's tagged baby-turns (u8v 41.3% on the same game, build/oi1). u8x's rats work its rank-2 and rank-4+ home mines (24.6% and 6.7% of baby-turns; u8v none). The allocation stage happens.
  - Picked per 100 baby-turns, by 400-round window: u8x 48.4, 60.2, 41.7, 37.8, 36.3; u6x 42.0, 63.9, 44.0, 44.7, 42.5. u8x leads the opening by 15% and trails from r401 by 6-15%. Its seek tag (x) holds 4-6% of the turns in every late window (u6x 0-1%), and its W turns fall from u6x's 35-37% to 22-27%. It lost 86 babies to cats (u6x 59) and picked 30.5k to 33.5k over the game.
  - One game only; the screen's picks per 100 rat-turns (falsifier 1) judges it. If u8x passes and then fails late on the replica, a successor would end KMINE_SKIP and the crowd seek once every home mine is known (not registered).
- 12:26: **u8x passed screen zd47** (falsifier 1): 44 cells, 33 wins to zc6's 32, D 7 (+4: araid arrows A and B, cheesefarm B, starvation B; -3: abrk DefaultSmall B and evileye A, araid DefaultSmall B), 0 overruns, funnel 37/48 (zc6 34/48). Picks per 100 rat-turns 35.07 (bar 33.8, zc6 35.56); delivered 341.8k.
  - u8x goes to the replica once u8w's blocks end, ahead of u8r and u8d: the census names the king's queue as our largest leak, and u8r (cat traps) and u8d (baby cap) work other areas. Screen zd48 (u8y) runs for attribution only.
  - attempts.csv: the empty base of u7a-u8u filled in from their descriptions (46 rows).
- 12:36: **How our rank-2 and rank-3 home mines get found** (u6x twin, sets 3 and 1, 96 replica games; scratch minelag.py on the VM). In losses our rank-2 home mine is first seen at median r87 and first worked at r185. In 13 of 45 loss game-sides none of ours sees it by r600: cheesebottles (both seats), fullofsecrets, whatsthecatdoin, clouds, thunderdome, uneruesansfin, FreeTheCheese, TheHeist, averystrangespace and dirtpassageway. Where it is seen, a rat works it before any seer reaches the king: work comes a median 16 rounds before the report. So the lag is sight, not allocation. The first seers are cheese chasers (C, 13 of 32 seen) and rushers (a, 9); scouts (S) are 3. Rank 3: seen r74, worked r103, 5 of 33 never seen.
- 12:36: **Baby deaths over whole games** (same 96 games; scratch deathcause.py).
  - In losses, 55.3 of ours die a game: thrown and landed 17.0, their rat traps 9.4, cat pounce 9.0, their bites 6.3, cat crush 4.8, throw hits 4.7, king formation 2.2.
  - 66.3 of theirs die: cat pounce 20.4, thrown by us 19.7, cat crush 10.9, throw hits 6.6, formation 2.9, our king's bites 2.6, our rats' bites 1.4.
  - Our rat kills come from throws (26.3 a loss game). Theirs come from throws (21.7), traps (9.4) and bites (6.3).
  - In wins, 35.4 of ours and 36.4 of theirs die.
- 12:44: **Local keepout trio, A side vs araid** (kx1 u8x, kx2 u6x, kx3 u8y; one game each; scratch tagwin.py).
  - Picked by babies plus kings: u6x 33.5k + 2.6k, u8x 30.5k + 2.8k, u8y 25.2k + 4.3k. Cat deaths 59 / 86 / 48. Points 67-32 / 52-47 / 63-36.
  - Picks per 100 baby-turns by 400-round window: u8y 48.7, 33.4, 33.4, 35.2, 35.8. That is lower than u8x (48.4, 60.2, 41.7, 37.8, 36.3) and u6x (42.0, 63.9, 44.0, 44.7, 42.5).
  - u8y does not have SEEK_CROWD 3, so the lower picks are not from that change. One game on one cell is noise here. Attribution between u8x's two parts waits for screen zd48 (40 of 48 games played).
- 12:48: **u8y (KMINE_SKIP 2 alone) passed screen zd48**: 33 wins vs zc6's 32, D 7 (+4: araid arrows A and B, cheesefarm A and B; -3: abrk arrows A, araid DefaultMedium A, DefaultSmall B), picks per 100 rat-turns 36.94 (u8x 35.07, zc6 35.56), delivered 354.3k, 0 overruns, 2 starved kings. Against the archetypes SEEK_CROWD 3 adds nothing measurable on top of KMINE_SKIP. This was the attribution arm only: u8x keeps its replica slot as registered.
- 12:52: **u8w (OPEN_IDX) REJECTED at replica block 1 on falsifier 1** (#221, set 3: 14043-14115, 16/16, twin-anchored `paired 13248-13291 --crown 12130-12163`).
  - Paired: 48 cells, 40 alike, D 8 (+5: Finals wallsofparadis A, Icarus averyfineline A, YokoOno 5t4rv4t10n_1337 B, Varun thunderdome A, spsquared TheHeist B; -3: Xzibit minimaze A, YokoOno Stash A, spsquared rift B), net +2, t +0.71. Without the falsifier this would continue to block 2.
  - Falsifier 1 (bankat.py, mean picked by r400 over games reaching r400): 2,295 a game (19 losses 2,292, 10 wins 2,300) against the bar of 2,300. The twin computes to 2,187 the same way (24 losses 2,054, 11 wins 2,476). That is +4.9% where +5% was registered, so it fails by 5 cheese a game. The falsifier rejects regardless of t.
  - Falsifier 2 holds: 4 king-kill wins before r150 (Varun 2, Xzibit 2) against the twin's 3 on these cells (bar 2); no early losses (twin 1).
  - OPEN_IDX moved the opening income about as far as registered and the block was positive. Under "no rule applies backwards" it can reopen only as a new candidate on fresh blocks.
- 12:52: **Ablation sweep (cadence; none since zb8 on 11-03, 50+ attempts ago).** Eight switches that are on in u6x and were never switched off, one screen each, chained on the VM (build/chain_abl.sh): u8z -RUSH (zd49), u9a -FORM_KING (zd50), u9b -TRAP_SHARE (zd51), u9c -GOSSIP_ALWAYS (zd52), u9d -KING_FLOW (zd53), u9e -KING_PLUG (zd54), u9f -DIG_FIRST (zd55), u9g -KING_DIG (zd56).
  - Pre-registered in each SNAPSHOT: D 0 means the switch is inert against the archetypes (no replica block). Wins below 31, any overrun, or picks per 100 rat-turns below 33.8 means it earns its keep. Otherwise the ablation is neutral or better and queues for the replica as a simplification.
- 12:55: **u8x submitted** (#222, u6x + KMINE_SKIP 2 + SEEK_CROWD 3): block 1 on set 3 after 14115 (8 of 16 sent, the rest queued for the drain; waiter build/wait_u8xb1.sh). Its replica falsifiers are in bots/u8x/SNAPSHOT (allocation stage via mking.py, picked by r400 at least 2,296 on set 3, king HP lost to bites and thrown rats per loss at most 1.25 x 316). Queue after it: u8r, u8d.
- 13:06: **How the twin's games end** (sets 3 and 1, 96 replica games, match.json): 46 of 59 losses are king kills (median r431) and 13 are on points; 21 of 37 wins are king kills (median r467). King-kill wins come early or late: 7 before r150, 2 at r150-299, 9 after r600. King-kill losses: 2 before r150, 9 at r150-299, 14 at r300-449, 18 after r600. The early losses are both seats of small maps: DefaultSmall vs Finals, averyfineline vs Icarus, DefaultMedium vs Varun and collegejunior, thunderdome vs nzjt, tiny vs collegejunior.
- 13:06: **Our baby deaths to r300** (same 96 games; scratch deathtag.py: the victim's last smuggled tag, age, place, cause).
  - In losses 33.3 of ours die by r300 (we spawn 48.2, they 44.0); in wins 18.0 (39.0 spawned, theirs 36.2).
  - Half die young: 16.6 a loss game within 50 rounds of birth, 12.7 at 51-150, 3.9 older.
  - Where: the middle third of the start-to-start line 16.2, the enemy third 11.5, our third 5.6.
  - Last tags: rusher attacking 5.3, fighter idle 3.8, retreating 3.7, carrying an enemy to throw 2.9, facing an enemy 2.6. No single branch dominates.
  - Causes: thrown by them 10.7, cat pounce 5.4, their traps 5.0, their bites 3.9, hit by a thrown rat 2.9, cat crush 2.8.
- 13:06: **How the field lays rat traps** (same games; scratch trapstyle.py over the actions dumps). Every opponent's traps are reactive and nearly all fire on us: 88-97% fire in our losses, 60-85% within 3 rounds of being laid (median 1-2 rounds).
  - Finals lays 84 a lost game (81 fire), 78% of them in the third nearest our start. Varun lays 59 (54 fire), Icarus 35 (32), spsquared 29 (28, 70% after r800), YokoOno 22, Xzibit 23, nzjt 4, collegejunior 4.
  - At 20 cheese a trap, Finals pays about 1,690 for 4,050 damage a lost game. u8f's traps fired at the same rate (38 of 39 a lost game) but were paid out of the bank that starves. The trap line stays closed; what differs is their income (Finals picks 21.6k a lost game to our 7.2k, set 3).
- 13:08: **Field style against us** (same 96 games; scratch fieldstyle.py; means per game, opponents' numbers from our losses).
  - Babies alive at r100: we have 22.9 in losses (24.2 in wins), opponents 14-20. At r400 we have 18.1 to their 15-24. They field fewer early and keep them alive.
  - Picks per 100 baby-turns, r1-400: ours 27.1 in losses (22.5 in wins); Finals 56.6, Icarus 56.1, Varun 52.7, spsquared 50.3, nzjt 47.8, Xzibit 38.7, YokoOno 36.5, collegejunior 34.9.
  - Baby-turns in the other team's half (nearer the other start): ours 9.2% in losses, 11.1% in wins; theirs 20-33% in our losses (Varun 33, Finals 30, Xzibit 28, YokoOno 24, Icarus 22, nzjt 21), except spsquared 8 and collegejunior 15.
  - Baby-turns within Chebyshev 4 of their own king: ours 37.3% in losses, 31.6% in wins; theirs 13-28%.
  - Kings 3 and more tiles from a mine are the rule on both sides: Chebyshev distance from the king to its nearest mine averages 6.8 at r100 for us and 5.5-11 for them.
  - RAID is dead code in u6x: raider needs RAID_EVERY > 0, and RAID_EVERY is 0 (the 10-31 ablation found it inert for this reason). Raiding was last tried on s7d (s8i, s8j; rejected at replica block 2 on 10-29).
  - Their own home mines are harvested at 76-81% (u8p's census), so raiders of ours would find few piles there. The gap is attendance at our own mines: they hold more rat-rounds at our mines than we do (Stash vs YokoOno: 1037/1139/716 to our 628/786/998 by r414). u8x (on the replica) targets that.
- 13:10: **What our babies near our king are doing** (same 96 games, rounds 51-400; scratch nearktag.py). In losses, 36.1% of our tagged baby-turns are within Chebyshev 4 of one of our kings (29.0% in wins).
  - Walking to a mine (M): 11.5% of all turns (gatherers 6.9, fighters 4.6). Wandering at a mine (W): 7.7% (4.7 and 3.0). Together 19.2% in losses, 16.6% in wins; they carry no cheese and a quarter of those turns stand still.
  - War near the king: 4.6%. Rushers just out of the spawn ring: 2.6%. Walking to visible cheese: 3.6%. Delivering: only 0.5%.
  - So the near-king time is the mine queue (u8x's target, on the replica now), not delivery.
  - The 19:48 (11-03) trap-trigger census found almost every enemy trap fired with an enemy rat within dist2 20, where Baby drops the TrapRisk mask. Every fight-time avoidance variant since (RISK_GATHER, RISK_FRESH, RISK_ADAPT, WOUNDED_HP, FACE_HOLD, AMMO_AWAY) was weak locally against our own trap builds and never reached the replica.
- 13:19: **u8z (u6x without RUSH) fails its ablation screen on falsifier 2** (zd49 vs zc6, both u6x-lineage, 48 cells vs the archetypes, jobs 3). Wins rose: 38 of 44 compared cells to 32 (D 10, +8 -2; funnel 41 of 48 to 34). But picks per 100 rat-turns fell to 29.41, under the registered bar of 33.8 (control 35.56). Delivered fell to 334.8k from 354.8k (-5.6%) and picked to 412.2k from 442.1k (-6.8%). No overruns.
  - The registered rule: "wins below 31, any overrun, or picks per 100 rat-turns below 33.8: the switch earns its keep, keep it, no replica block". So RUSH stays and u8z gets no replica block.
  - The gains were 7 of 8 on seat B, and 7 of 8 against araid (tiny B against both araid and abrk at r58-61, now wins at r82-84). Against the raid archetypes a home without rushers holds better.
  - The field history agrees with the rule: every rush cut lost on the replica (u0o RUSH_TO 12 net -7, u1o RUSH_TO 20 net -3, u5n RUSH_DIST 20 net -4, t0g RUSH_LIFE 150 net -4). The archetypes reward a home guard, while the field games reward harassment.
- 13:21: **Corpus run registered** (TRAINING_ALGORITHM §3.1 and the §8 cadence: "play the whole corpus from both sides against one strong bot (queued replica scrimmages) and sort maps by graded score: the worst show basics failures the panel hides"). It is due once per 10 attempts and has never been logged.
  - Build: u6x, the incumbent (u7p is its byte-identical twin). It is submitted after u8x's replica run ends: after block 1 if that rejects, else after u8x's last block. The next candidate waits for the corpus.
  - Opponent: VarunVejalla_final4_run_away10. It is the in-band bot nearest 50%: 6-6 against the twin on sets 1 and 3.
  - Maps: 20 of the 74 corpus maps are on no panel set (Excavation, UncomfortablyClose, cheeseguardians, constrictinglattice, diamond, dirtfulcat, gridlock, hatefullattice, heart, houseofcards, krotus, pipes, popthecork, safelycontained, seepastyourgreed, sittingducks, toomuchcheese, trapped, ultimatestarvation, whereisthecheese).
  - 9 maps already have u6x/u7p games against Varun on sets 1-3 and are reused.
  - The other 65 maps, plus thunderdome as a repeat, go in 22 triples (research/panel/corpus-01..22.json, never-paneled maps first), each played from both seats: 44 matches, about 4-5 h of the replica's hourly limit.
  - Analysis, fixed now:
    - Per map: the graded score (field_report.py graded: 1/0 for a game decided by kings, else our points share), averaged over the seats.
    - Maps sorted by that score. The worst 10 get replay diagnosis for basics failures: navigation stalls, starvation, symmetry still unknown late, overruns, delivery against theirs, king-kill rounds.
    - Seat splits (A won, B lost or the reverse) are listed as symmetry suspects.
    - Repairs that come out of it are new candidates with their own registrations.
- 13:36: **u8x (KMINE_SKIP 2 + SEEK_CROWD 3) passes replica block 1** (#222, set 3, 14129-14159, 16/16; twin-anchored `paired 13248-13291 --crown 12130-12163`): 48 cells, 41 alike, D 7 (+4 -3), net +1, t +0.38. Block 1 rejects only at t <= 0.
  - Gained: Varun corridorofdoomanddespair B, spsquared TheHeist A and B, collegejunior FreeTheCheese B. Lost: Finals where_are_you B, YokoOno Stash A, spsquared rift A.
  - Falsifier 2 holds: stage 1, M+W baby-turns within Chebyshev 4 of our king, measured exactly (scratch mwexact.py, nearktag's criteria, r51-400) is 9.69% (L 6.19%, W 13.72%) against the 10% bar. The twin on the same set has 20.09%, and on sets 3 and 1 together 18.07% (the registered 17.9%).
  - Falsifier 3 holds: picked by r400 is 2,378 a game (bankat: L 2,324 over 19 games, W 2,447 over 15) against the 2,296 bar (the twin's set-3 2,187 plus 5%).
  - Falsifier 4 holds: our kings lost 229 HP a loss to bites and enemy-thrown rats (bites 4,173, throws 2,694, 30 losses) against 395 (1.25 x the twin's 316).
  - The last king to fall in a loss: starvation 16, bites 6, throws 2, scratches 2.
  - Block 2 on set 1: 16 matches queued after 14159 (all held by the hourly limit for the drain). Waiter build/wait_u8xb2.sh.
  - The corpus run moves behind u8x's last block.
- 13:36: **Replacement rushers** (twin, sets 1 and 3, 96 games; scratch lateRush.py). A baby counts as an attacker if it ever carries the smuggled 'a' tag.
  - Attackers born in r60-300 (replacements, since the rush index is the live-baby count): 3.1 a loss game, 1.1 a win.
  - In the 18 losses whose last king fell before r400, attackers hold 36.7% of our baby-turns in the final 60 rounds, replacements 28.4%, at a median Chebyshev 8.8 from our king. Our babies within 4 of the king average 3.2.
  - Extremes: thunderdome vs nzjt has 26 and 32 attackers born before r60 (a death spiral: every replacement in the 4-15 band rushes). DefaultMedium vs Varun 12787: our last babies rushed (59 of 175 baby-turns in r151-200 tagged 'a') while 1-4 enemy rats bit our lone king to death.
  - Not a candidate by itself: RUSH_UNTIL doses 100-200 were neutral (u2i, s8v, u6y), and a 3-rat effect per loss is small.
- 13:40: **u9a (u6x without FORM_KING) fails its ablation screen** (falsifier 2): FORM_KING earns its keep.
  - Screen zd50 against zc6 (u6x): 26 wins of 44 cells against 32 (D 12, +3 -9); funnel 28 of 48 against 34.
  - Picks per 100 rat-turns 31.69 against the 33.8 bar (control 35.56); delivered 338.4k against 354.8k; 0 overruns.
  - 6 of the 9 lost cells are against abrk (DefaultSmall twice, bed, cheesefarm twice, evileye), the archetype that rushes and forms its own second king.
  - No replica block. Next in the sweep: zd51 (u9b, without TRAP_SHARE), launched.
- 13:52: **Starvation census** (scratch starve.py; u8x block 1 plus the twin's sets 1 and 3, 89 losses). Our last king starved in 39 losses (44%); 36 have a usable ledger.
  - In 29 of the 36, spawn cost beat picked minus upkeep over the 200 rounds before the bank first ended a 50-round window under 100. Means over those 200 rounds: picked 663, spawn cost 806 (22 spawns), upkeep 467, digs 62, traps 29, dropped 308.
  - Two shapes:
    - The early grinder: 10 games, collapse in r101-250, 45-58 spawns costing 1,760-2,240 by r150-200 while we picked 240-775. Maps: averyfineline and mincemeat vs Icarus, 5t4rv4t10n vs YokoOno, whatsthecatdoin vs Merlin, DefaultMedium vs Varun, uneasy_alliance vs Icarus.
    - The late squeeze: collapse in r450-1350, income about equal to upkeep (keepout, FreeTheCheese and Stash with 2 kings; wallsofparadis, externalsustenance with one), where 3-18 cheap replacements tip the bank under the floor.
  - A crude counterfactual (no spawn after the bank first ends a window under 600) keeps the bank positive to the death round in all 36. But it ignores that fewer spawns mean fewer pickers and fighters: u6e (RESERVE_ROUNDS 300) lost 11 cells net on its screen ("the thinner army loses fights"); u5t (CORE_BABIES 12) went -1 on the replica, and u6u (LOW_FLOOR 30) was rejected. The reserve levers are priced, so this is no candidate by itself.
  - Next: the death census on the grinder games (who dies, where, to what), to tell our own replacement rush from their pressure.
- 13:53: **The grinder games' deaths** (deathtag.py to r200 on the 10 matches that hold them: 14156-14157, 13288-13289, 14134, 13264, 14158, 13290-13291, 12787; 25 lost games, 5 won).
  - Losses: 31.9 of our babies die by r200 (wins 7.8), 45.0 spawned (theirs 39.1). 19.5 of the deaths come within 50 rounds of birth.
  - Where: mid-map 21.7, enemy third 7.4, home 2.8.
  - Causes: enemy throws 11.4 (land 8.9, hit 2.5) plus 1.0 thrown into cats; cats 11.0 (pounce 6.5, crush 3.8, scratch 0.7); enemy traps 4.2; bites 4.2. Wins: cats 3.8, traps 1.8, throws 1.4, bites 0.8.
  - By last tag: attack moves 5.3, fighters chasing cats (F:g) 3.9, retreats 3.1, carriers aiming (T) 2.6, facing (f) 2.4.
  - So the grinder is a mid-map meat market: rushers and their replacements, and fighters drawn to cats, crossing the middle where enemy throwers and cats work.
- 13:56: **Grab census** (scratch grabvictim.py to r300; u8x block 1 plus the twin's set 1). Enemy grabs of our babies: 2,289 on health (the grabber has more hp) and 1,262 blind (we could not see it): 64% on health. Our grabs of theirs: 2,531 on health and 1,207 blind (68%).
  - Victims' last tags for health grabs: facing 379, attack move 376, carriers aiming a throw 307, retreating 184.
  - So the war turns on bite-then-grab: one bite makes a rat grabbable by any full-health enemy from any side. Facing fixes (KING_EYES, GLANCE_MEM, FACE_HOLD) reach only the blind third.
  - Fighters as cat hunters are priced: FIGHT_EVERY 3 (u5q) -3 and GATHER_FIRST 9 (u7a) -1 on the replica, so the F:g cat deaths buy cat points the field pays for.
- 14:03: **u9b's ablation screen passes** (zd51, u6x without TRAP_SHARE, vs zc6 = u6x): 37 vs 32 wins on 44 cells (+8 -3, net +5, t 1.5), funnel 40/48 vs 34, picks/100 36.14 vs 35.56, 0 overruns.
  - The gains are mostly against araid (cheesefarm both seats, arrows, evileye, DefaultMedium, canyoudig), plus abrk on canyoudig and tiny. The losses are abrk DefaultSmall, araid bed and araid starvation (all seat A).
  - Falsifier 3: neutral or better, so it queues for the replica as a simplification, behind u8x's run and the corpus run.
  - Mechanism to check on the replica: the shared trap-risk mask may cost more in detours than it saves in trap deaths. Without it, each rat still marks the enemies it sees itself.
- 14:03: **Bite damage census** (scratch biteboost.py; u8x block 1, twin sets 1 and 3; summary dmgBy.bite over rat plus king bites, killing blows capped). Damage per bite: ours 9.94; spsquared 9.90, AdamTan12_Finals 9.59, YokoOno 9.95, VarunVejalla 9.88, Xzibit 9.95, thewaxmango 10.11, Icarus 9.45, nzjt 11.74.
  - So every field bot but nzjt bites at the plain 10. The engine adds ceil(sqrt(c)) for c cheese, taken from the rat's own load first and then the bank: 1 cheese buys +1, and 2 cheese buy +2.
  - Grabs need the grabber strictly healthier (HEALTH_GRAB_THRESHOLD 0). With equal bite counts, a +1 boost leaves the enemy 1 hp lower per bite: our next grab is legal, theirs is not. A parity census (scratch parity.py) is sizing how many health grabs on us a +1 or +2 boost would have blocked.
- 14:27: **The parity census refutes the bite edge** (scratch parity.py; u8x block 1 plus the twin's sets 1 and 3). Enemy grabs of our babies in our losses: 5,949, of them 4,518 on health. A bite boost of +1/+2/+3/+4 on every bite we landed on the grabber beforehand would have made 1%/2%/3%/4% of those grabs illegal.
  - The health margins are wide: 10 hp 872, 20 hp 648, 30 hp 651, 40 hp 879, 50 hp 850, 60+ hp 1,535. In 3,758 of the 5,802 health grabs over all games, we had never bitten the grabber.
  - In losses we grab more than they do (6,285 to 5,949), so grab volume is not what separates the results.
  - u9h (u6x + BITE_EDGE 2: two cheese on every non-finishing bite of an enemy baby) was built for this and is dropped unregistered. Its mechanism is refuted, and what is left is a dose: +2 damage for 2 cheese a bite. The name u9h stays used.
- 14:27: **Hopeless cells** (all replica games since match 9000, any of our builds; map x opponent, wins of games). starvation vs Xzibit 0/76, DefaultMedium vs Varun 0/76 and vs collegejunior 0/32, dirtpassageway vs collegejunior 0/76, cheesebottles vs nzjt 0/32, whatsthecatdoin vs nzjt 0/32, keepout vs Finals 0/32, cheesefarm vs nzjt 0/16, ZeroDay vs Xzibit 0/14, mercifullattice vs Icarus 0/14; arrows vs YokoOno 2/76, jail vs Finals 1/76, thunderdome vs nzjt 6/76.
  - Poor maps (3 or fewer mines in the census: 5t4rv4t10n_1337 and ultimatestarvation 1; UncomfortablyClose, averyfineline, canyoudig, corridorofdoomanddespair, houseofcards, krotus, tiny, whereisthecheese 2; Excavation, mincemeat, starvation 3) went 178 W to 224 L. The rest went 1,203 to 1,323.
- 14:27: **Four losing traces** (scratch trace.py: banks, babies, kings, traps and pickups by nearest mine every 25 rounds).
  - averyfineline vs Icarus (14156, 14157, 13288). It is 20x20 with a dirt cross and one mine per quadrant side. Both banks crash by r100. Icarus keeps 16 babies while ours fall to 0 by r150. It lays 37-51 traps, which kill 9-19 of ours. Our king starves at r205-239.
  - starvation vs Xzibit (12806, 12807). It is 35x25 vertical, with mines at (5,11) and (29,11) and a dirt-ringed centre mine at (17,11). In 12806 we out-fight them (kills 79-58, throws 168-101), but they pick 5,770 to our 2,935 by r950. To r600 they took 2,390 from the centre mine to our 680. Their bank sits at 800-1,100 and ours near 300, and they spawn 131 to our 72. In 12807 we formed a second king near r200 on this poor map, and the bank was at 4 by r300: starvation plus bites.
  - DefaultMedium vs Varun (12787). They laid 61 traps (trap damage 2,196). Our babies fell from 23 to 2 by r175. Our king walked to (23,30) near the centre, took about 50 bites in r150-225 and starved at r243.
  - DefaultMedium vs collegejunior (14147). Our bank fell from 1,830 to 323 by r150 (30 babies at r75), while theirs held near 1,600 with 16-19 babies. Kills were even (30 to 32). Our second king formed near r150 and died by r200. Our king sat at (24,26)-(18,27) near the centre and was bitten to death at r227 (45 king bites).
  - The common shape: by r100-150 the bank is spent on babies (300-600 to the field's 1,000-1,700), and those babies die in the middle. Then the king, which has walked toward the centre, starves or is bitten.
- 14:27: **Trap engine facts** (RobotControllerImpl.processTrapsAtLocation, GameWorld.triggerTrap). Enemy traps are invisible. A trap fires when an enemy enters any tile of its 3x3, by a move or by landing from a throw, and standing still never fires it. Firing removes the trap, deals 50 damage, sets the victim's movement cooldown to 30 and calls backstab.
  - So a reactive trap laid on the tile in front of a rat in contact fires on that rat's next step in any direction except away. A rat that holds its tile cannot be trapped.
- 14:27: **The screen does not predict the replica.** From attempts.csv, 27 candidates have both a screen net and a replica net (s7h to u6b). The correlation is Pearson 0.22 and Spearman 0.07.
  - The 12 screen passes at net +3 or better went 0, -3, -1, -3, -1, +13 (s9s), 0, -4, -1, -2, -3 and -2 on the replica: a mean of -1.8 without s9s.
  - The range is restricted, since screen failures rarely reach the replica. So the screen's power to veto is untested, and its vetoes of reserve and spawn-restraint levers (u6e RESERVE_ROUNDS 300: 22 to 33 wins; u3f RICH_MIN_AREA 901) carry no field evidence. The archetypes rush and raid, which punish a thinner army, and they lay no traps.
  - From here, a screen counts as a basics gate only (overruns, delivery, crashes, inert switches). The win count stays on record but no longer vetoes a candidate whose mechanism targets field behaviour the archetypes lack (starvation, traps, forward harvesters).
- 14:27: **u9c (u6x without GOSSIP_ALWAYS) fails its ablation screen on falsifier 2** (zd52 vs zc6): 32 wins of 44 cells to 32 (D 14, +7 -7, net 0), funnel 35/48 to 34. But picks per 100 rat-turns fell to 31.07, under the 33.8 bar (control 35.56). Delivered was 324.6k to 354.8k, starved kings 4 to 3, and there were no overruns. GOSSIP_ALWAYS earns its keep (delivery), so there is no replica block. zd53 (u9d, without KING_FLOW) is running.
- 14:27: **u8x (KMINE_SKIP 2 + SEEK_CROWD 3) REJECTED at replica block 2** (#222, set 1: 14177-14219, 16/16; twin-anchored `paired 12771-12813 --crown 11549-11677`): 48 cells, 38 alike, D 10 (+4: AdamTan12_Finals DefaultSmall B, YokoOno arrows A, spsquared bed A and superioristhmus A; -6: Finals tiny A, YokoOno Stash A, Varun knifefight A and t_minus_five_minutes B, nzjt peaceinourtime A, collegejunior clockwisdom B), net -2, t -0.63.
  - Pooled with block 1 (+4 -3): D 17, net -1, t -0.24, under block 2's bar of 1.
  - Falsifier 3 also fails on set 1. Picked by r400 averaged 2,454 (losses 2,412, wins 2,503) against a bar of 2,804: the twin's 2,670 plus 5%. The mechanism held: M+W within 4 of our king fell to about 8% of baby-turns (10.2% in losses, 5.9% in wins; twin 20%), and king HP lost to bites and throws was 332 a loss against the 395 bar. So the freed turns did not turn into picks on set 1.
  - Economy now has four straight rejects (u8p, u8q, u8w, u8x), so the next new candidate goes to another area.
- 14:27: **Corpus run submitted** (#223, u6x, the registered build). Queued 22 triples from both seats against VarunVejalla_final4_run_away10: 4 sent, 40 queued for the drain. The 44 matches come after 14221; waiter build/wait_corpus.sh. Analysis as registered at 13:21 (graded score per map, the worst 10 diagnosed, seat splits listed).
- 14:37: **King-death census by map class** (kingdeath.py over twin sets 1 and 3 plus u8x blocks 1-2: 192 games, 115 losses; poor = 3 or fewer mines in build/census.tsv).
  - Poor maps with one king went 5-17, and poor maps where we formed a second king 0-2. Rich maps with one king went 40-61, and with two kings 32-35.
  - Last-king death causes in losses: starvation 49 (9 before r300, 26 in r300-800, 13 in r800-1500, 1 later), bites 25, thrown rats 14, cats 7. 20 losses were on points with a king alive (12 of them with two kings).
  - On poor maps 10 of the 17 one-king losses are starvations, 6 of them before r300.
  - Record by opponent over the same games: Xzibit 16-8, spsquared 13-11, Varun 12-12, Icarus 10-14, collegejunior 9-15, YokoOno 7-17, Finals 5-19, nzjt 5-19. The poor-map losses are almost all in-band: Icarus averyfineline 0-4 and mincemeat 0-4, Xzibit starvation 0-4, collegejunior tiny 0-4.
- 14:37: **u9i registered** (structural, production) = u6x + RESERVE_ROUNDS 300 from RESERVE_FROM 300. This is u6e's bank reserve doctrine, reopened on fresh blocks under the 14:27 screen rule and binding from r300 only, so the opening army and the rush window are u6x's.
  - From r300, every spawn floor holds the live kings' upkeep for 300 more rounds: 600 for one king, 1,200 for two.
  - Area: the spawn policy. The four straight economy rejects were all harvest allocation.
  - Falsifiers are in bots/u9i/SNAPSHOT:
    - Screen zd57 is a basics gate: inert, any overrun, or picks per 100 rat-turns under 33.8.
    - On the replica, starvation must end under 40% of our king-destroyed losses, pooled (twin 52%).
    - King bite plus throw damage must stay at or under 395 a loss.
    - Standard block rules.
  - zd57 is chained after the ablation chain (zd53-zd56). Replica order after the corpus run: u9i first, since it targets the largest loss mode. Then u8r (cooperation cat traps), u9b (a simplification) and u8d (RICH2_BABIES 80, D 1 on its screen: nearly inert).
- 14:43: **The 20 losses on points** (same 192 games; scratch ptsloss.py and ptsbank.py over rounds.tsv; engine GameWorld.setWinnerIfMorePoints). War points are 0.3 of the cat-damage share plus 0.5 of the live-king share plus 0.2 of the transfer share; cooperation uses 0.5, 0.3 and 0.2. Most of these losses are close: 46-50 points to 50-54.
  - Cat damage decides the rose type. On rose against Icarus (12811, 14217) and spsquared (13249) we held 2 kings to 1 and transfers were even, but cat damage was 595 to 2,539, 460 to 2,422 and 230 to 1,430. Also averystrangespace vs Icarus (13288: 50.0 to 50.0, cat 110 to 386) and uneruesansfin and corridorofdoom (cat 340-1,337 to 1,780-2,570). In each, 50-500 more cat damage over the game flips the result. These are u8r's target: cooperation cat traps. Cat traps are legal only in cooperation, or for 100 rounds after the other team's backstab (GameWorld.catTrapsAllowed).
  - King count and transfers decide the rich type: spsquared on superioristhmus and rift, Finals on where_are_you. At r1150 we banked 3-11k with 3-4 kings, and at the end we held 6.6-10.7k unspent while capped at 37-40 babies against their 44-74. That is u8d's target (RICH2_BABIES 80).
  - A third king for the 2-to-1 games is not available. After r1200 a team with two or more kings cannot form another (MAX_NUMBER_OF_RAT_KINGS_AFTER_CUTOFF 2), and those games banked only 450-650 at r1150 on about 2 a round of income.
- 14:43: **END_KING census** (scratch endk2.py; the 50 games that reached r1850 with one king of ours). With 400 or more banked at r1850 the second king formed in 13 of 15. With less (END_KING_CHEESE 400 is the guard) it formed in 1 of 10: 6 losses and 3 wins.
  - Three of those losses flip with a second king: 12806 (49 to 51), 13266 (46.7 to 53.3) and 14145 (48.1 to 51.9), whose banks were 76, 278 and 160 after spending at the 300 floor. u9i's reserve, 600 from r300, keeps the guard's 400 there.
  - One rich failure: 13270 had 947 banked and 25 babies, but only 0-13 babies gathered within 3 of the king at (4,54) by r2000.
  - A king has 600 hp and every king loses 10 a round while the bank is under 2, so a late king without the upkeep would starve both.
- 14:43: **u9d (u6x without KING_FLOW) passes its ablation screen** (zd53 vs zc6): 36 wins of 44 cells to 32 (D 12, +8 -4, net +4), funnel 39/48 to 34, picks per 100 rat-turns 34.42 (bar 33.8), delivered 327.5k to 354.8k, 3 starved kings, 0 overruns. Falsifier 3, neutral or better: it queues for the replica as a simplification. Gains are mostly against araid (cheesefarm both seats, arrows, evileye, starvation, DefaultMedium, tiny); losses abrk bed B and evileye A/B, araid DefaultSmall A. zd54 (u9e, without KING_PLUG) is running. VM disk 2.8 GB free.
- 14:56: **Screen order changed**: u9i's screen zd57 now runs right after zd54 (build/chain_u9i.sh: zd54, zd57, zd55, zd56). u9i is next on the replica, and the corpus run should finish before zd56 would have.
- 14:56: **How bites and throws kill our last king** (scratch kdforce.py and kthrow.py on the VM; the 39 losses in the 14:37 census whose last king died to bites or thrown rats).
  - Near the king we are not outnumbered. At 30 rounds before the death we have a median 6 babies within Chebyshev 4 of the king to their 2, and at least as many as theirs in 35 of 39 cases. Overall we are outnumbered: 8 babies alive to their 17 (medians). Our bank falls from a median 289 to 115 over the last 60 rounds.
  - Many of these kings were bitten down slowly. 15 of 39 had 200 hp or less 60 rounds before the death, with starvation at 0 (jail vs Finals: 550 bite damage over 988 rounds). Over the 115 losses our kings lose 288 hp a game to starvation, 194 to bites, 106 to throws, 54 to cats and 12 to our own throws. Their kings in the same games lose 54 to our throws and 12 to our bites.
  - Every thrown rat that hit our kings was one of ours. In the 15 throw-heavy games, they threw 218 of our babies into our kings (7,842 damage), and we threw 8 of theirs into theirs. Engine grab rule (RobotControllerImpl.assertCanCarryRat): an enemy rat can grab one of ours that cannot sense it (cone 90) or that has strictly less health.
  - Not a new lever. This class has been attacked many times and every attempt was rejected: RESCUE, the carrier guard, AMMO_AWAY, GUARD_KING_R, BITE_CARRIER, u6v (the king walks away), RETREAT_HP and BITE_EDGE (refuted by the 14:27 parity census). A king cannot outrun babies (UnitType RAT_KING movement cooldown 40, baby 10). The deaths come with the 2-to-1 army deficit, so the economy is what has to change. u9i's reserve keeps the bank, and with it the army, from collapsing first.
- 15:03: **u9e (u6x without KING_PLUG) fails its ablation screen** (zd54 vs zc6): 30 wins of 44 cells to 32 (D 6, +2 -4). Lost abrk cheesefarm B and evileye B, and araid DefaultMedium A and DefaultSmall B; won araid cheesefarm A and B. Delivered 311.9k vs 354.8k (-12%); picks per 100 rat-turns 35.29; 0 overruns. Wins are under 31, so KING_PLUG earns its keep under the 12:52 rule: it stays in, and u9e gets no replica block. zd57 (u9i) is running.
- 15:20: **Who opens the war** (scratch stab.py on the VM; game_report backstabRound and backstabBy over the 14:37 census's 192 games). We opened it in 50 games and won 18 (36%), median round 40. They opened it in 120 and we won 45 (38%), median round 36. Nobody opened it in 22, and we won 14 (64%).
  - Our openings: 35 by bites, 15 by grabs; 37 of the 50 came by r65.
  - Breaking first costs nothing measurable: 36% after our break, 38% after theirs. That fits u6h (u6a + PEACE, rejected at block 2, pooled t +0.50). Removing our breaks moves games into "they open", not into "nobody opens".
- 15:20: **Corpus run, first 16 of 44 matches** (14226-14275 vs Varun; scratch corpus.py): 24 maps, 18 of 48 games won. 11 maps lost from both seats: constrictinglattice, Excavation, dirtfulcat, gridlock, krotus, pipes, safelycontained, sittingducks, toomuchcheese, arrows, attack_on_cat. Seat splits (symmetry suspects): heart, houseofcards, whereisthecheese, averyfineline. Seven maps won from both seats.
  - Excavation, dirtfulcat and gridlock (14228/14229, 0 of 6) end by king kill at r229-343. We hold 19-20 babies to r75 and spend faster (bank at r100: 284-810 to their 580-1155). Then we fall to 0-8 babies by r150-200 while they keep 11-17. They deliver 2.2-2.7 times our cheese.
- 15:20: **Where our babies' turns go, and how they die** (scratch roleturns.py and trapgame.py, the same 48 games, rounds 1-400, per game). Classes come from the smuggled tags: ATTACK (an 'a' tag in a rat's first 20 turns), FIGHT (the FIGHT bit) or GATHER.
  - In losses we field 61 rats by r400: 23.9 gatherers (41.7 picks per 100 turns), 19.2 fighters (28.8) and 18.1 attackers (20.6). 52.5 of them die.
  - Causes of our rats' deaths: thrown by them and killed on landing 15.8, their traps 14.7, cats 10.9, hit by thrown rats 4.1, bites 2.9. Their rats die 42 a game: thrown by us 19.3, cats 13.5, hit by our thrown rats 5.0, our bites 3.1, our traps 0. We grab 73 of theirs a game, they grab 64 of ours.
  - The trap exchange is one-sided. Varun lays 50.8 rat traps per lost game by r400 (29.8 in wins); 45.2 of them hit our rats for 2,002 damage a game. Each fires a median 1 round after it is laid (reactive: laid on the tile our rat steps to next). 29.7 of our rats die within 30 rounds of a hit, 57% of our rat deaths in losses. u6x laid no rat traps at all in these games.
  - Engine: a rat trap costs 20, deals 50 and stuns for 30 rounds; it fires on any robot that ends a move within distance squared 2 of it. A baby sees a 90-degree cone, and an enemy can grab it from any adjacent tile outside the cone, or from anywhere once our rat has less health than the grabber (GameConstants HEALTH_GRAB_THRESHOLD 0). A trapped rat (50 health, stunned) can therefore be grabbed and thrown by any healthier rat.
  - The 192-game version of trapgame.py, split by opponent, is running on the VM.
- 15:21: **u9i passes its basics gate** (screen zd57 vs zc6): 28 wins of 44 cells to 32 (D 6, +1 -5). It gained araid cheesefarm B and lost abrk DefaultSmall A/B, araid DefaultMedium A, DefaultSmall B and starvation A, all by king kill at r432-668: the reserve holds back replacements in the archetypes' army wars. 0 overruns, picks per 100 rat-turns 36.34 (bar 33.8), delivered 321.7k to 354.8k, 4 starved kings (zc6 3). Under the 14:27 rule the win count is recorded but does not veto a candidate aimed at field starvation. u9i plays set 3 block 1 once the corpus run ends.
- 15:21: **The trap census over the 14:37 census's 192 games** (trapgame.py on the VM, per game to r400). In losses the opponent lays 20.2 rat traps, 18.5 hit, for 836 damage, and 11.7 of our rats die within 30 rounds of a hit. In wins: 11.1 laid, 9.4 hits, 6.0 deaths. We lay none.
  - By opponent (traps laid, hits, our follow-on deaths, a game): Finals (5-19) 41.0, 39.2, 23.7; Varun (12-12) 38.1, 32.0, 18.4; Icarus (10-14) 20.2, 19.3, 14.1; YokoOno (7-17) 14.8, 12.7, 8.0; Xzibit (16-8) 8.4, 6.8, 4.8; spsquared (13-11) 6.9, 6.0, 3.9; Merlin (5-19) 2.9; collegejunior (9-15) 0.5.
  - The three heaviest trappers are also the three highest picking rates in the 13:08 field-style census (Finals 56.6, Icarus 56.1, Varun 52.7 picks per 100 baby-turns).
- 15:41: **u9f (u6x without DIG_FIRST) fails its ablation screen** (zd55 vs zc6): 35 wins of 44 cells to 32 (D 11, +7 -4), funnel 37/48 to 34, but picks per 100 rat-turns 33.75 (bar 33.8) and delivered 293.2k vs 354.8k (-17%), 1 starved king, 0 overruns. DIG_FIRST earns its keep under the 12:52 rule (economy); no replica block. zd56 (u9g, without KING_DIG) is running.
- 15:41: **VM replay prune** (owner rule #23): the VM was down to 2.3 GB free. Deleted the 871 replica replay files older than two days (1.07 GB; list in /root/bc26-replays-pruned-20261104-2341.txt on the VM). 3.3 GB free now.
- 15:43: **Which of our moves walk into Varun's traps** (scratch trapforensic.py on the VM; the first 16 corpus matches, 48 games, r1-400). 1,862 enemy trap hits on our babies: 90% on our own moves, 7% on throw landings.
  - Timing: 460 hits came in the round the trap was laid, 552 a round later, and 362 six or more rounds later.
  - Distance: before the move, the victim stood Chebyshev 2 from the placer in 599 cases and 3 in 790 (85% of own moves); 91% of the moves ended in the placer's front zone and 78% stepped toward it.
  - Decision tags: of the 1,179 tagged moves, 74% were combat moves: attack 189, flank 169, face 198, war 185, aim a carried rat 129. Economy moves were 18%: to a mine 99, wander at a mine 47, to cheese 34, scout 27. Another 457 victims died on the move itself (no tag), already at 50 health or less.
  - So the trap layer punishes the step into contact. The avoidance switches that hold back that step (FACE_HOLD, RISK_FRESH, RISK_ADAPT, WOUNDED_HP, RISK_IN_FIGHT) all cost fights locally. What worked on the replica was laying traps ourselves: u8f.
- 15:43: **u8f against the twin, by opponent** (the same 96 games of sets 3 and 1): u8f went 41-55 to the twin's 37-59. Varun 9-3 (twin 6-6), spsquared 7-5 (4-8), Icarus 6-6 (4-8), Xzibit 8-4 and Merlin 3-9 (both unchanged), YokoOno 2-10 (4-8), Finals 2-10 (3-9), collegejunior 4-8 (5-7). Paired against twin and crown it was net -1, and starvation ended 17 of its 25 king-destroyed losses.
- 15:43: **u9j registered** (structural, combat doctrine on u9i's base): u9i + u8f's trap switches (BABY_RAT_TRAPS, TRAP_ADJ, TRAP_CAP 25, TRAP_SPAWN_FIRST, RAT_TRAP_MIN_CHEESE 300) + TRAP_RESERVE (new): from r300 no baby trap spends below u9i's reserve (600 for one king). That makes the reserve hold against traps as well as spawns. Screen zd58 is chained after zd56 as a basics gate. u9j plays on the replica only if u9i is accepted, paired against u9i's blocks. Its replica falsifiers: our traps fire at least 10 times a game, and starvation ends under 40% of king-destroyed losses.
- 15:54: **Corpus run, 24 of 44 matches** (14226-14301; scratch corpus.py): 36 maps, 72 games, 25 won (35%). Lost from both seats on 16 maps, won from both on 7. Nine maps split by seat, and in eight of them seat A won (heart, whereisthecheese, averyfineline, averystrangespace, cheesebottles, dirtpassageway, EscapeTheNight, externalsustenance; only houseofcards went to seat B). Over our replica games since 11549, seat A won 47.3% and seat B 45.2%, but some opponents lean hard one way: against Varun A 56% and B 47%, Finals 24% and 44%, YokoOno 48% and 24%, Merlin 34% and 20%.
- 15:54: **Mirror check on the corpus pairs** (scratch mirrorpairs.py, tools/mirror_check.py on each map's seat-A and seat-B games). Varun diverges from its own mirror by r1-4 on every map. Our play diverges at r2 on 13 of 36 maps. The cause is the first spawn tile: the king's two best tiles tie (offsets (1,2) and (2,1) on whereisthecheese and trapped), and the dx/dy loop takes them in opposite mirrored order. That is the known tie that SYM_TIE and SYM_CENTRE fix; the side-symmetry bundle lost on the replica every time it was tried (u5y block 1: net -6, t -2.12). Varun's r3 asymmetry means a seat split against it does not show our own fault, so these splits are not a lever.
- 15:54: **How our kings die against Varun** (scratch kingdeath.py, the 72 corpus games). In the 47 losses the last king died to bites in 22 and to starvation in 18, with 3 scratches, 1 trap and 1 thrown rat. 30 of the 47 losses end before r460. In the 25 wins, their last king starved in 14.
  - The small-map losses run one way. gridlock as A (14228): by r60 we had spawned 43 babies and banked 510, to their 21 spawned and 1,175 banked with 28 traps laid. By r200 their traps numbered 45, we had lost 45 rats to their 23, and we had 0 babies left. The king starved at r287. canyoudig (14281/14282) follows the same path: our bank sits at the 300 floor from r80, we have 0-1 babies by r180-200, and the king starves at r244-265.
- 15:54: **u8f's late traps were not what starved it** (scratch trapbank.py on u8f's 96 replica games). In losses it laid 34.5 rat traps a game by r300 and 10.3 after. Only 2.4 of the late ones came with the bank under 600. A trap floor raised to the reserve after r300 would have stopped about 48 cheese a lost game. So u9k (u8f + that floor, built and not registered) would replay u8f nearly cell for cell; deleted unrun. The same holds for u9j's TRAP_RESERVE, which leaves u9j as u9i + u8f's traps in practice. A paired economy census (u8f against the twin, cell by cell) is running on the VM.
- 16:01: **u9g (u6x without KING_DIG) fails its ablation screen** (zd56 vs zc6): 30 wins of 44 cells to 32 (D 6, +2 -4). It won araid DefaultMedium B and evileye A, and lost abrk DefaultSmall B and bed B, araid DefaultSmall A and starvation A. Funnel 32/48 to 34, picks per 100 rat-turns 34.96 (bar 33.8), delivered 338.6k to 354.8k (-4.6%), 1 starved king, 0 overruns. Wins are under 31, so KING_DIG earns its keep under the 12:52 rule: no replica block. That ends the 11-04 ablation sweep: TRAP_SHARE (u9b) and KING_FLOW (u9d) queue as simplifications; RUSH, FORM_KING, GOSSIP_ALWAYS, KING_PLUG, DIG_FIRST and KING_DIG stay. zd58 (u9j) is running.
- 16:01: **Where u8f's trap money came from** (scratch ecopair.py on the VM: u8f's blocks against the twin's games in the same 94 (opponent, map, seat) cells of sets 3 and 1, means per cell at r100/200/400).
  - Result pairs: both lost 46, both won 29, u8f won where the twin lost 12, u8f lost where the twin won 7.
  - In the 46 cells both lost, picks did not move: 968 to 922 by r200 and 1,708 to 1,782 by r400. The bank ran lower from r100 (654 to 783; 359 to 494 at r200; 271 to 357 at r400), and so did the army (babies alive 17.0 to 21.0 at r100, 9.4 to 11.5 at r400). u8f laid 36 traps by r400 and lost fewer rats (35.0 dead to 41.5) while killing more (38.3 to 35.7). It spawned 44.4 to the twin's 53.0 by r400, and these games ended 46 rounds sooner (683 to 729).
  - So the traps (about 720 cheese by r400) were paid for with about 9 fewer spawns and a lower bank. The exchange improved, from 1.16 of our deaths per enemy death to 0.91, but the army was smaller at every checkpoint.
  - The 12 cells u8f flipped to wins came from keeping rats alive, not from income: it picked 1,486 by r400 to the twin's 2,638, but lost 11.0 rats to 22.2. The 7 it flipped to losses came with income falling (823 to 1,262 by r400; babies 8.4 to 12.7 at r400).
  - That fits the 13:08 field-style census. Every one of the eight opponents picks more per baby-turn than we do (27.1 in our losses against 34.9-56.6). The heavy trappers carry traps on that income, while u8f bought them out of a 27-per-100 economy.
- 16:17: **u9j passes its basics gate.** Screen zd58 (the xt2 cells against abrk and araid, jobs 3) against u9i's zd57:
  - 27 wins of 44 cells to 28 (D 15, +7 −8; the control zc6 has 32).
  - 0 overruns. Picks per 100 rat-turns 39.08 (bar 33.8; zd57 36.34). Delivered 266.0k to zd57's 321.7k (−17%). 1 starved king (zd57 4).
  - The wins stay on record without a veto (14:27 rule: the archetypes lay no reactive traps). u9j plays only if u9i is accepted, paired against u9i's blocks.
- 16:27: **Opening census by map area** (180 replica games: twin sets 1-3 and the corpus run to 14327; scratch opening.py). Means per game, ours/theirs:
  - Small maps (900 tiles or fewer), 37 losses: babies alive 23/16 at r50 and 7/15 at r300; bank 533/1,008 at r100 and 160/580 at r300; picked by r300 1,533/2,775. The enemy laid 15 traps by r60 and 32 by r150. Our spawn spend with 18+ babies alive (the RICH tier) was 1,284 by r100 and 1,591 by r300.
  - Small maps, 17 wins: the same RICH spend (1,272 and 1,563); picked by r300 1,763/1,620.
  - 901-1,600 tiles: losses (24) RICH 958 by r100 and 1,423 by r300, picked 1,400/2,005; wins (16) 761 and 1,241, picked 1,416/1,566.
  - 1,601-2,500 tiles: losses (32) 864 and 1,465, picked 1,744/2,333; wins (24) 594 and 1,078, picked 1,813/1,600.
  - Over 2,500 tiles: losses (19) 797 and 1,508, picked 2,075/3,230; wins (11) 587 and 935, picked 1,484/1,377.
  - Reading: we are 17-37 on small maps. The RICH spend does not tell small-map wins from losses (on larger maps the wins spend less). What separates them everywhere is the opponent's picks by r300: 1.3-1.8 times ours in losses, about equal in wins. No candidate from this.
- 16:27: **Capability sweep, priced** (TRAINING_ALGORITHM §3.3). The 02:21 sweep listed the same unused methods and stopped there; this one reads the engine for each and prices the one that could matter.
  - Of the 85 RobotController methods, u6x never calls 23. Most are trivial or debug: adjacentLocation, canMoveForward, moveForward, canSenseRobot, senseRobot, canSenseRobotAtLocation, isLocationOccupied, getAllPartLocations, the three cooldown getters, resign, setIndicatorDot/Line, setTimelineMarker.
  - The ones that could change play: disintegrate, isBeingCarried, isBeingThrown, getBackstabbingTeam, and removeRatTrap/removeCatTrap (these remove only our own traps and refund nothing).
  - Engine facts from the source:
    - Every robot runs its turn while carried or thrown, but it fails isActionReady, isMovementReady and isTurningReady. disintegrate has no precondition.
    - carryRat adds 20 action cooldown, so the carrier throws two turns later at the earliest.
    - A throw that hits deals 10 + 4·(2·turns of flight left + 1 on the first step), up to 46, to the robot hit and to the thrown rat.
    - A grab needs the grabber outside the victim's 90° cone (5 of its 8 neighbours) or more health than the victim (HEALTH_GRAB_THRESHOLD 0). So a full-health rat can be grabbed only from its blind side. u6x's guard facing (GUARD_DIST2 13) already turns toward known enemies.
- 16:27: **What the enemy's grabs of our rats cost** (228 replica games, the same set; scratch disint.py, disint2.py; counts per grab, so a rat grabbed twice counts twice).
  - Per lost game (133): 76.6 grabs and 56.9 throws. The throws dealt 90 damage to our king (2.5 hits) and 702 to our babies (5.6 killed), and 69.6 to their own units. The grabbed rat died on landing 44.4 times, died later within 30 rounds 15.9 times, and was alive 30 rounds on 16.4 times.
  - Per won game (95): 36.9 grabs, 12 damage to our king, 361 to our babies.
  - By the victim's health at the grab, per lost game (grabs; damage to our king; damage to our babies, with kills; alive 30 rounds on):
    - 10 or less: 8.5; 10; 70 (0.6); 0.6.
    - 11-46: 22.6; 29; 173 (1.5); 2.8.
    - 47-60: 22.7; 19; 230 (1.8); 5.4.
    - 61+: 22.8; 32; 228 (1.7); 7.5.
  - **u9l priced and dropped** (built, never registered): u6x + DISINTEGRATE_HP, under which a carried rat at or below that health disintegrates.
    - The best dose, 46, denies 39 damage to our king and 243 to our babies (2.1 kills) per lost game. It gives up 22 damage to their own units. It loses 3.4 rats that would have lived 30 more rounds, brings forward 2.7 deaths, and drops their cheese at the carrier's feet.
    - Net: about 1.3 rats lost and 39 king damage saved per lost game, below the replica's resolution. Higher doses lose more rats than they save: the 61+ band gives up 15.6 rats for 1.7 kills.
- 16:55: **Corpus run, all 44 matches** (14226-14463 against Varun; scratch corpus.py; 150 games on 74 maps, with the other Varun games in that id range): 63 won (42%).
  - Lost from both seats on 30 maps: constrictinglattice, Excavation, dirtfulcat, gridlock, krotus, pipes, safelycontained, sittingducks, toomuchcheese, arrows, attack_on_cat, bed, canyoudig, closeup, DefaultLarge, DefaultSmall, jail, keepout, Meow, mercifullattice, mincemeat, Stash, starvation, streetsofnewyork, uneasy_alliance, wallsofparadis, whatsthecatdoin, thunderdome, DefaultMedium, clouds. Partly scored: hatefullattice 0.19, cheeseguardians 0.21, ZeroDay 0.22, cheesefarm 0.22.
  - Split by seat on 17 maps, 14 of them to seat A (FreeTheCheese, where_are_you, heart, whereisthecheese, averyfineline, averystrangespace, cheesebottles, dirtpassageway, EscapeTheNight, externalsustenance, rift, knifefight, uneruesansfin, corridorofdoomanddespair); seat B took houseofcards, groveisbeingdesecrated and Nofreecheese.
  - Won from both seats on 23 maps.
  - The fastest losses from both seats (rounds A/B): DefaultSmall 252/180, DefaultMedium 243/219, Excavation 283/229, canyoudig 265/244, gridlock 287/248, dirtfulcat 269/343, mincemeat 312/285, bed 358/257.
- 16:55: **Early losses against Varun** (scratch earlyloss.py; the 44 corpus matches, 132 games): 31 losses with our last king dead by r400 (starved 16, bitten 11, scratched 3, a thrown rat 1), 48 later losses, 53 wins. Means for early losses / later losses / wins:
  - Our spawns by r60: 32.6 / 28.6 / 26.4 (Varun 21.8 / 21.2 / 20.7). By r150: 47.3 / 41.2 / 36.9 (Varun 39.7 / 35.1 / 30.5).
  - Our bank at r60: 1,042 / 1,275 / 1,402 (Varun 1,424 / 1,561 / 1,573). At r150: 355 / 726 / 796 (Varun 481 / 761 / 870).
  - Varun's rat traps by r60: 15.4 / 8.1 / 6.6; by r150: 35.5 / 23.8 / 15.6. We lay none.
  - Our babies dead by r200: 42.8 / 27.0 / 21.7 (Varun's 31.0 / 17.8 / 16.0). Ours in early losses by cause: thrown-rat landings 12.6, traps 11.3, pounces 5.7, thrown-rat hits 4.3, crushes 3.6, bites 3.0.
  - Reading: the early losses are the most intense games. We lose about 1.4 rats per enemy rat in early losses and in wins alike, so the exchange rate does not decide them. Volume does: we replace losses faster than our income covers, Varun spends on traps and keeps more cheese, and half of these games end with our king starving.
- 16:55: **u9i is on the replica** (submission #224; set 3 block 1, 16 scrims queued after 14463). The hourly limit holds the queue; 4 went out at 16:43.
- 16:55: **Cats killing our king, and grove's top band** (traces of the corpus games where a cat scratched our last king to death):
  - 14324 groveisbeingdesecrated (seat A): the king walked east from (52,57) into the corner at (58,57) by r121. A cat sat beside it at (55,57) from r121 and scratched it from 580 hp (r101) to dead at r189; our babies bit that cat from 2,680 to 120 hp. Our 20 babies picked nothing in 180 rounds: 17 of them wandered x 21-37 in the top band at 15.4-15.8k bytecodes a turn. The band is closed by a 57-tile wall in row 53 whose only gaps (x 57 and 59, one tile wide) are beside our king, where this cat's waypoint loop runs ((47-57, 55-57)). Varun's babies left their band through the mirrored gaps and had picked 180 by r60 and 880 by r180.
  - 14284 cheesebottles (seat B): the king stood at (44,2) from r141. A cat came beside it at r241 and scratched it from 600 hp (r221) to dead at r313. None of our 33 babies attacked that cat (it kept 3,760 of 4,000 hp).
  - 14458 uneasy_alliance (seat A): the king stood at (11,24) from r221 and died at r319 with two cats beside it.
  - u6x's king steps away from a cat only within distance² 20 (G.fan away from it; KING_DODGE is off). Otherwise it holds still while any cat is in view. A king moves every 4 rounds and a cat every 2, so fleeing cannot shake a cat; a cat leaves only when its waypoint loop moves it on.
  - The grove band is the 10-30 finding again (gr1): toward a mine, Flow's reverse A* must expand nearly the whole south half before it reaches the 50+-tile way round through the gap, and FLOW_REV_TURNS 60 runs out first. BitFlow (u0c), built for this, was parked on 10-30 for overruns elsewhere.
- 16:55: **Stuck babies** (scratch stuckc.py; the 44 corpus matches, 132 games). A baby is static when it stands within Chebyshev 2 of its tile 30 rounds before; stuck when static and not within 2 of a mine or 3 of an own king.
  - Pooled over 1.72M of our baby-rounds and 2.52M of Varun's: we are static 42.6% of the time and Varun 16.1%; stuck 18.9% to 11.9%. In our losses we are static 40.3% to Varun's 13.7%; in our wins 33.8% to 19.2%.
  - So 23.7% of our baby-rounds stand still at a mine or our king, against Varun's 4.2%.
  - The worst maps by our stuck share: rift 0.37, groveisbeingdesecrated 0.30 (Varun 0.06), Hike 0.30, Nofreecheese 0.29 (0.03), pipes 0.29, gridlock 0.28 (0.09), attack_on_cat 0.25 (0.05), fullofsecrets 0.25, Stash 0.22 (0.11).
  - Stuck share does not tell losses from wins (0.151 in losses, 0.160 in wins); static share does a little (0.403 to 0.338).
  - Our king deaths in the 79 losses (every king, not only the last): bitten 40, starved 38, scratched 6, trapped 1, a thrown rat 1.
- 17:07: **What our static babies are doing** (scratch stuck2.py and stuck3.py, the same 132 games; our replica builds carry Debug.TAG, so each turn's move decision decodes from its bytecode count with tools/smuggle.py; F = role FIGHT). Each static round takes the first class that fits; shares are of all baby-rounds, ours / Varun's:
  - Carrying cheese 6.0% / 8.3%. Ours are mostly delivering (d 48%), so carriers queue at the king.
  - Within Chebyshev 4 of an enemy 9.0% / 2.1%, and 14.2% / 2.5% in our losses. Tags: war 28%, faceEnemy 18%, walking to a mine 18%, wandering at a mine 16%.
  - Within 2 of a mine with a pickup in the 30 rounds 2.4% / 0.3%; with none 8.9% / 0.6%. Of the 8.9%, 66% are tagged M (walking to the mine they target, so it is another mine) and 19% W (wandering at their mine).
  - Within 3 of our king 4.8% / 1.2% (41% W, 25% M).
  - Searching (12k+ bytecodes) 2.6% / 0.3%: in losses 64% are attackers ('a'). By map: groveisbeingdesecrated 26%, Hike 22%, seepastyourgreed 15%, TheHeist 11%, uneruesansfin 6%.
  - Anything else 8.8% / 3.3%: 62% W, i.e. gatherers wandering the 7x7 around their mine without a pickup.
  - Overall 21.5% of our baby turns run 12k+ bytecodes, Varun's 4.3%.
  - So about a fifth of our baby-rounds go to babies standing still on the way to a mine (M, about 10%) or wandering at a mine with nothing to pick (W, about 10%). Varun has almost none of either. On grove, 57% of our baby turns were attack moves: the 12 rushers walked the band west to its dead end and searched there.
- 17:20: **Stalled mine walkers** (scratch probem.py; the 132 corpus games). 217,668 of our M rounds were static.
  - 85% of them stepped that round. 65% ended exactly 2 from the nearest mine, and 36% had 3 or more of our robots beside them.
  - Only 9.5% ran a 12k+ search (moving M rounds: 29%), and enemies beside averaged 0.01.
  - So these babies are not blocked. They pace the edge of a mine square:
    - wanderAround draws targets up to 3 from the mine, but atMine counts only within 2.
    - A baby that steps out to 3 turns M and steps back. turns.tsv positions are end-of-turn, so an M round that ends at 2 began at 3.
    - The 17:07 reading that these babies target another mine does not follow. Static M and static W are one behaviour: idling at a crowded mine.
  - By map: rift 40k rounds, superioristhmus 29k, FreeTheCheese 11k (3.25 own robots beside), averystrangespace 10k (3.27), minimaze 9.5k (2.83).
- 17:20: **The strong bots' style** (scratch style.py).
  - Sources: abcdezyxwv and AlexT101 from the 32 unranked matches of 10-31 to 11-01, when we ran u2g-era builds; Varun from the u6x corpus.
  - Means a game, theirs / ours, in our losses (45 vs Alex, 42 vs abcd, 79 vs Varun):
    - Spawns by r60: Alex 20/33, abcd 25/31, Varun 21/30. By r300: 43/57, 46/51, 53/54.
    - Bank at r300: 1486/630, 1733/655, 338/387 (Varun spends; the other two hoard).
    - Picks by r300: 3408/2600, 3298/2079, 3012/1756. Picks per 100 baby-turns to r300: 63/42, 55/31, 50/32.
    - Static share to r600: 0.09/0.32, 0.12/0.35, 0.13/0.39.
    - Rat traps laid by r150 (r300): Alex 30 (48), Varun 28 (44), abcd 2 (4). Ours: 0.
    - Babies lost by r300: 26/44, 27/39, 33/43.
  - In our 53 wins against Varun the gap closes: picks by r300 2073/1980, static 0.18/0.31, babies lost 20/26.
- 17:20: **Where each side's cheese comes from** (scratch pickhalf.py; the same games).
  - Method: a tile is ours or theirs by dist2 to the two first kings' starts. Pickups come from actions.tsv pickup rows, which run about 700 a game below the rounds.tsv picks column.
  - To r300, in our losses:
    - Mine spawn in our half / theirs: Alex 3717/4172, Varun 3188/3517, abcd 2306/2123.
    - We pick 48% / 3% of the two halves' spawn against Alex, 43% / 4% against Varun, 48% / 4% against abcd. They pick 16% / 49%, 20% / 59%, 17% / 69%.
    - So by r300 a third to a half of each half's spawn has not been picked.
  - Over whole lost games they take far more from our half than we take from theirs: Varun 2621 (we take 325 of theirs), abcd 3698 (more than our own 3551), Alex 3445.
  - Trap hits on our rats to r300: Alex 42.4 (14.3 deaths), Varun 38.3 (12.0), abcd 3.6 (0.9). Over the game: 108 (40), 65 (22), 34 (12). We lay none, so they take none.
  - Our rat deaths by cause to r300 against Alex: throwLand 15.4, trap 14.3, pounce 5.0, crush 3.8, throwHit 2.9. Theirs: throwLand 9.8, pounce 5.7, crush 3.8, throwHit 2.9.
  - A census of where the unpicked cheese lies (scratch floor.py: floor cheese per mine at r150/300/600, by half and by which side covered the mine) is running on the VM.
- 17:25: **u9i rejected at block 1** (set 3, 14464-14479, #224).
  - Paired against the twin: 48 cells, 42 alike (26 replay u6x's crowning games exactly), D 6 (gained 1, lost 5), net -4, t -1.63. Block 1 rejects at t <= 0.
  - Lost: AdamTan12_Finals where_are_you B, Xzibit clouds B and minimaze B, YokoOno Stash A, collegejunior FreeTheCheese A. Gained collegejunior FreeTheCheese B.
  - Falsifiers 2 and 3 were not computed; rule 4 decided it. u9j, built on u9i, is dropped with it.
- 17:25: **VM dump cache.**
  - The VM's tools/game_report.py lacked the local cache trim, and its build/dumpcache had reached 2.3 GB (239 dumps) with 2.3 GB free on root.
  - Copied the local game_report.py there (the dump key hashes only the dumper classes and the engine, so the cache stays valid). The old copy is kept as ~/diag26b/game_report.vm-backup.py.
  - VM censuses now run with BC_DUMPCACHE_MAX_MB=2000.
- 17:25: **Where the unpicked cheese lies** (scratch floor.py; floor cheese per mine from a tile model of actions.tsv; a mine is covered when a side's rat stood within 4 of it in the 30 rounds before).
  - At r300 in our losses to Varun (63 games):
    - We covered 2.4 of our 3.6 home mines and 0.4 of their 4.1. Varun covered 2.3 of ours and 3.4 of theirs.
    - Floor in our half: 56 at mines both sides covered, 81 at mines only we covered, 117 at mines only they covered, and 488 at mines nobody covered. Their half: 17 / 36 / 211 / 489.
  - Against Alex and abcd the shape is the same, and by r600 the uncovered piles reach 1,000-1,200 a half.
  - So covered mines are picked clean, and the floor cheese sits at mines nobody works.
- 17:30: **Unworked mines** (scratch unworked.py and unworked2.py; the 132 corpus games; unworked = 200+ floor cheese at r300).
  - Of our 500 home mines, 229 (46%) were unworked. Of Varun's 556, 250 (45%).
  - Unworked against worked (ours): Chebyshev from the king's start median 23 against 13; no path without digging 66% against 30%; never reached by us by r300 47% against 2%. Varun's are the same: 66% dirt-blocked, 33% never reached.
  - By map the two sides match: externalsustenance 24/24 each, TheHeist 16/18 each, cheeseguardians 14/18 each, seepastyourgreed 14/16 and 16/16, grove 16/16 and 15/20.
  - On the maps we lose from both seats, both sides work every mine: Meow 0/8, mercifullattice 0/10, uneasy_alliance 0/12, bed 0/4, arrows 0/4.
  - So far mines behind dirt are left unworked by both sides alike. That is an edge nobody takes, but it does not explain our losses.
- 17:30: **The losses' economy** (scratch pickgroup.py: pickhalf.py on the 30 maps we lose from both seats against the rest).
  - To r300 in the 56 games on those maps, Varun picks 698 of the 2,557 cheese our half's mines spawn (27%) and we pick 1,383 (54%).
  - Of their half's 2,916 they pick 2,086 (72%) and we pick 112 (4%).
  - In our 53 wins Varun picks 5% of our half and we pick 6% of theirs.
  - Trap hits on our rats to r300: 41.8 on those maps (13.0 deaths), 17.7 in our wins.
  - So in the losses Varun takes a quarter of our home harvest at mines we cover, behind a trap screen we never answer. A pickup-race census (scratch race.py: who reaches each of our half's spawns first, from how far, and whether our nearest baby had it in its cone) is running.
- 17:30: **u8r submitted** (#225; u6x + CAT_LAY + CAT_RING, the cooperation cat-trap doctrine; never on the replica). Set 3 block 1: 4 sent, 12 queued after 14479 (waiter build/wait_u8rb1.sh). Its falsifiers are in bots/u8r/SNAPSHOT.
  - The identity re-run is satisfied by the paired tool's replay check: in every block, cells the candidate's change never touches replay u6x's crowning games exactly (26 of 48 for u9i). So the replica stays deterministic and the measuring path holds, and a renamed copy would only repeat that.
- 17:46: **Pickup races at our mines** (scratch race.py; each mine spawn on our half's tiles in r1-600 and who picks it first, with positions and cones at the end of the round before).
  - On the 30 maps we lose from both seats (10,138 spawns in 56 games against Varun): we pick 46%, Varun picks 26%, and 28% lies 60+ rounds.
  - When we pick, latency is a median 1 round and our baby was nearer 84% of the time.
  - When Varun picks, latency is a median 5. Its baby was nearer 74% of the time and within 4 tiles 75%; ours was within 4 only 34%. Our babies had the tile in a cone 16% of the time, Varun's 41%.
  - The same shape holds on our other lost maps (Varun picks 15%) and in our wins (6%).
- 17:46: **What our babies did at the races we lost** (scratch contested.py; decoded tags of our nearest two babies within Chebyshev 6).
  - 1.88 of our babies stood within 6 of the tile against 3.18 of Varun's. In 39.5% of these races none of ours was within 6.
  - The tags present: M 6.9%, C 4.7%, d+c 4.6%, a 4.5%, fF 4.4%, f 2.7%, w 2.7%, MF 2.4%, wF 2.4%, W 2.4%, T 2.2%. No single behaviour dominates; mostly we are absent.
- 17:46: **Where Varun's traps catch us** (scratch traploc.py; enemy rat-trap lays and the triggers on our rats, r1-600, by half and by distance to the nearest mine).
  - Lost-both maps (56 games): Varun lays 41.6 traps a game in our half (26.1 within 4 of a mine) and 17.6 in its own. Its traps catch our rats 38.1 times in our half (24.3 by a mine) and 14.3 in its half. Hits are 25.9 in r1-150 and 26.5 in r151-600.
  - Our other losses (23): 25.0 laid in our half (19.3 by a mine), 22.9 hits there.
  - Our wins (53): 10.6 laid in our half, 10.1 hits there.
  - On the lost maps, the hits fall a median 11 tiles from our king's start and 16 from Varun's.
  - Deaths to r300 on those maps: ours are throwLand 14.1, trap 13.0, pounce 5.1, throwHit 4.6, crush 3.1, bite 2.6 (about 45). Varun's are throwLand 16.9, pounce 6.1, throwHit 5.1, crush 4.1, bite 2.5 (about 35). The 10-death gap is the trap deaths.
  - Engine (GameWorld.placeTrap, RobotControllerImpl.processTrapsAtLocation): a rat trap arms every tile within distance² 2 of it (its 3x3), and fires only when an enemy rat moves onto one of those tiles. It deals 50, sets the movement cooldown to 30, and is consumed. A baby lays only on an adjacent tile it can sense, so on its three front tiles. A rat that stands still is never trapped.
  - So the loss-maker against Varun is trap-and-steal at our mines: it seeds our mine squares, stuns and kills our gatherers as they move, and picks what they leave. We have never answered it on the replica (all trap and mine-trap candidates were about laying our own).
- 18:02: **Could our trap mask have known the traps that catch us?** (scratch trapseen.py; the strong and corpus games; every enemy rat-trap trigger on our rats in r1-600, matched to its lay row).
  - On the 30 lost-both maps, in our losses (all opponents): the trap was a median 1 round old (99% under TrapRisk's 150-round TTL). 1% were laid during cooperation, when our babies skip TrapRisk.observe.
  - The layer stood in the victim's cone at the end of the lay round or the round before in 60% of hits, and in some baby of ours' cone in 86%.
  - The victim was engaged (an enemy baby in its cone within dist2 20) in 78%, and in 55% it was both informed and engaged: u6x drops the mask while engaged (RISK_IN_FIGHT false, RISK_GATHER false).
  - 55% of victims stood within 4 of a mine. Their tags spread over every branch: none decoded 36% (dead or carried that round), a 8.7%, wF 6.3%, M 6.2%, fF 5.4%, w 5.3%, f 4.6%, T 4.5%, k 3.8%, C 2.5%, d 2.4%.
  - Our wins and other maps look the same. So these are reactive combat traps laid beside our rats as they engage, not a seeded mine square. The fight-time masks already tried (t0b RISK_FRESH, u0u RISK_ADAPT, u1l FACE_HOLD, u1b/u1n WOUNDED_HP) all failed their local mechanism checks: fires on us did not fall.
- 18:02: **How our games end** (scratch kings.py; the same games, summary.json).
  - Against Varun: 73 of 79 losses were king kills (mean round 712) and 6 on points; of 53 wins, 35 were king kills and 18 on points.
  - Of the 6 points losses, one more king of ours would have flipped 5. Against Alex and abcd nearly every loss is a king kill.
  - The points formula (GameWorld.setWinnerIfMorePoints) weighs kings alive 0.5 after a backstab and cheese transferred only 0.2.
- 18:02: **Flippable cells** (local; match.json of all 635 u6x-era unranked matches since 11549, 268 cells of opponent, map and seat).
  - 69 cells with 3+ games have mixed results (971 games, 514 won). These are the cells a candidate can flip; replays are deterministic, so a cell is mixed only across builds.
  - In them, losses are king kills at a median r499 (359) or points at r2000 (98). Wins are king kills at a median r226 (256; Varun r78, Icarus r44, Merlin r60) or points (258).
- 18:02: **The opening rush** (scratch rush.py; 522 u6x-era games on the VM; rushers are spawn index 4-15, all born by r17).
  - It kills a king by r200 in 41 of 522 games (8%): Varun 17 of 180, Xzibit 8 of 48, YokoOno 7 of 48, Finals 5 of 48, Icarus and Merlin 2 each, spsquared and collegejunior 0.
  - When it works against Varun, 7.7 rushers reach Chebyshev 4 of the king (the first at r18) and 3.8 die on the way. When it fails, 2.8 arrive and 7.2 die on the way: thrown 2.0, trapped 1.9, cats 2.0, hit by thrown rats 0.7.
  - The rush lands only where the enemy king is near. Elsewhere 12 babies cross the map and die en route. RUSH_TO, RUSH_DIST, RUSH_LIFE and RUSH_BURN have each been rejected on the replica, so the rush's presence still pays for itself somehow.
  - A census of how our rats fire enemy traps (walked in, thrown or dropped onto them, or grabbed just before) is running.
- 18:19: **VM replay prune** (17:49; owner rule #23). The replica's replay store held 5.4 GB and the VM root was down to 2.2 GB free.
  - Pruned 217 replay files (293 MB) older than two days from /srv/bc26-galaxy/storage/bc26-replica-secure/episode/bc26/replays. Their list is in /root/bc26-replays-pruned-20261105-0149.txt.
  - The VM root now has 2.5 GB free. Our own copies of our matches under ~/projects/vibe/bc26b-galaxy-replays are untouched.
- 18:19: **How our rats fire enemy traps** (scratch thrownhit.py; the 522 u6x-era games on the VM; every enemy rat-trap trigger on our rats in r1-600).
  - Each hit is classed as thrown onto the trap (a land row that round), dropped onto it, grabbed in the 3 rounds before, or walked in.
  - Against Varun, in losses (4,651 hits): walked in 73%, thrown 8%, dropped 7%, grabbed just before 11%. In wins (1,756): 69%, 7%, 10%, 14%.
  - Against the others, walk-ins are 70-83% of hits in losses and wins alike.
  - A walk-in kills our rat that round 31-36% of the time (it was at 50 hp or less 16-32%). A throw onto a trap kills 68-94%.
  - So the traps catch our rats on their own moves, which a rat that holds still never makes. This puts "step-in discipline" (COMBAT.md §3: hold a ready attack and let the enemy step into you) on the list for a later candidate.
- 18:19: **u8r, set-3 block 1** (14481-14530, 16/16; twin-anchored paired 13248-13291 --crown 12130-12163).
  - 48 cells, 42 alike (18 replay the crowning game), D 6 (+4 -2), net +2, t 0.82. Gained Xzibit uneruesansfin A, Varun corridorofdoomanddespair B, and spsquared TheHeist A and B; lost YokoOno Stash A and spsquared rift A.
  - Falsifier 2 passes: our cat-trap damage is 1,401 a game against the bar of 1,033 (1.25 x the twin's 826).
  - Falsifier 3 passes: cheese transferred is 4,360 a game against the floor of 3,968 (0.9 x the twin's 4,409).
  - Block 1 never accepts, and t > 0 continues. Block 2 (set 1) was queued at 18:12 (waiter build/wait_u8rb2.sh).
  - All four gains are cells u8r's SNAPSHOT named in advance as points losses that more cat damage would flip: TheHeist (+600/+700), uneruesansfin (+1,400) and corridorofdoom (+625). So the mechanism is doing what it was designed to do.
- 18:50: **Would a front-zone rule stop the walk-ins?** (scratch stepin.py; the 522 u6x-era games, r1-600). A baby lays rat traps only on its three front tiles, and a trap arms the 3x3 around it, so the tiles a fresh trap of that baby can arm are its front zone (15 tiles).
  - Against Varun (4,581 walk-ins): the entered tile was in the front zone of some enemy baby in 77% of hits, of one the victim had in its cone in 65%, and of one that also faced the victim in 57%. In 51% the victim already stood inside that zone before its move. Against the others: 85%, 77%, 71%.
  - Base rates over our babies' war moves (677k against Varun): 9.6% enter the front zone of an enemy baby the mover can see, and 4.4% of those fire a trap. Outside every zone a move fires 0.19% of the time, about 23 times less.
  - TrapRisk already marks exactly these tiles (150-round marks), but u6x drops the mask while an enemy is in view, and every past fight-time mask failed its local check (fires on us did not fall). A census splitting the walk-ins by the victim's role (stepin2.py: is it a gatherer, which RISK_GATHER would keep masked?) is running.
- 18:50: **Where the rushers die** (rush.py's new per-key lines; the same 522 games).
  - Rushers that die before reaching the enemy king fall a median 13-19 tiles from it (Chebyshev), 0.50-0.68 of the way from our king. So a rally next to their king would form inside the band where they die.
  - The twin's per-set baselines (rush.py ALL): set 3 (13248-13291) arrived by r100 2.12 a game, rush kills 3 of 48; set 1 (12771-12813) 2.48 and 4; set 2 (12838-12899) 1.58 and 5.
- 18:50: **u9m = u6x + RUSH_STAGE 24** (new switch; structural, plateau step 6: the opening rush's shape, not its dose; COMBAT.md §8, "mass arrival beats per-turn kill caps"). Committed and screening (zd59, launched 18:43 after zd58).
  - An opening rusher (born by r17) heading for its rush target holds, facing it, once it is within Chebyshev 24 of the target (but farther than 6, and 3+ from its king's spawn tile). It leaves at the round the last rusher can have walked to the line: r17 + 5/4 of (Chebyshev home-target - 24) + 4.
  - Two earlier forms failed a local game (DefaultMedium vs u6x) and never reached a screen. A rally 5 from our king until r24 strung out on the way: at r44 three rushers led the other nine by 7 tiles. A walk-on-even-rounds rule for the head of the column never fired, because side-by-side rushers each counted the other as ahead and leapfrogged; distances matched u6x's through r36.
  - The staged form in the same game: nine rushers held at Chebyshev 18-22 of u6x's king through r36, and six were within 10-12 of it at r45. 0 overruns, a 52-47 points win, and no king died before r458.
  - On that map the nearest mirror spot (VERTICAL, 20 from home) lies inside the line, so the rushers first held 3 from home until r21, then retargeted to the true king and held on its line.
  - Falsifiers (bots/u9m/SNAPSHOT): the screen's basics gate; replica arrivals by r100 below 1.5x the twin's same-set value (set 3: 3.18) reject; rush kills over blocks 1-2 below the twin's 7 reject.
- 19:03: **u9m (RUSH_STAGE 24) REJECTED at its screen** (zd59 against zc6, falsifier 1: the picks bar).
  - Picks per 100 rat-turns 31.82, below the bar of 33.8 (zc6 35.56). Wins 32 of 44 cells to zc6's 32 (D 14, +7 −7). Delivered 323.8k to 354.8k, 3 starved kings, 0 overruns, 0 exceptions.
  - Combat moved: on the 44 shared cells we killed 1,861 enemy rats to u6x's 1,537 (+21%) and lost 2,562 to 2,806 (−9%).
  - The economy cost: rat-turns +4.4%, pickup amount −4.0%, spawns −5%. The surviving rushers keep the attacker role, which never picks cheese, and several wins ended sooner (Stash B against araid at r483 instead of r861), so less was delivered.
  - That is the third straight reject in the rush area (u8h RUSH_BURN, u8z no rush, u9m), so the next attempt goes elsewhere. u9n (RISK_GATHER, a trap doctrine for the economy's rats) is already screening as zd60.
  - The full unit suite passed (exit 0) after the RUSH_STAGE port to ratbot.
- 19:10: **u8r (CAT_LAY + CAT_RING) REJECTED at replica block 2** (#225; set 1: 14547-14620, 16/16, twin-anchored `paired 12771-12813 --crown 11549-11677`).
  - Paired: 48 cells, 39 alike, D 9 (+5: spsquared bed A, fullofsecrets A and B, superioristhmus B, Icarus rose B; −4: Icarus rose A, Xzibit attack_on_cat B, YokoOno Stash A, thewaxmango clockwisdom B), net +1, t 0.33. Pooled with block 1 (D 6, net +2): D 15, net +3, t 0.77, under the block-2 bar of 1.
  - Falsifier 2 failed: cat-trap damage 1,252 a game against the bar of 1,270 (1.25 × the twin's 1,016 on set 1; block 1 on set 3 had 1,401 against 1,033). Falsifier 3 failed: cheese transferred 3,941 a game against the floor of 4,133 (0.9 × the twin's 4,592). Block 1's transfer was 4,360 against the twin's 4,409 (−1%), so set 1 shows the economy cost (−14%) that set 3 hid.
  - Four of the five gains were against spsquared, as were two of block 1's points flips (rose B and TheHeist in the evidence). Cooperation cat trapping may pay against that one opponent; it does not pay across the panel.
- 19:10: **u9b submitted** (#226, u6x without TRAP_SHARE, the ablation that passed zd51 at 37 vs 32 wins). Block 1 is on set 3 after 14620: 4 sent, 12 queued for the drain; waiter build/wait_u9bb1.sh.
  - It goes ahead of u9n because zd60 had 9 of 48 cells done, so the replica would otherwise idle about 40 minutes. Queue after it: u9n (if zd60 passes its gate), u9d, u8d.

## 2026-11-05 10:55 PST: u9b passes replica block 1; u9n rejected at its screen
- 10:55: **The session stopped at 19:10 yesterday** and resumed at 10:43. u9b's block 1 finished overnight; nothing else was waiting on it (the drain had sent all 16 by about 22:30).
- 10:55: **u9b (u6x without TRAP_SHARE), set-3 block 1** (14633-14640, 14663-14666, 14679-14682, 16/16; twin-anchored `paired 13248-13291 --crown 12130-12163`).
  - 48 cells, 44 alike (15 replay the crowning game), D 4 (+3 −1), net +2, t 1.00. Gained Icarus averyfineline A, YokoOno 5t4rv4t10n_1337 B and externalsustenance A; lost YokoOno Stash A.
  - Block 1 rejects at t <= 0, so it continues. YokoOno Stash A is also lost by u8r and u9i on this set, so it reads as the twin's lucky cell.
  - Block 2 (set 1, `build/block_v4s1.sh`) was queued at 10:52.
- 10:55: **u9n (RISK_GATHER) REJECTED at screen zd60** (against zc6, falsifier 1: the archetypes' rat-trap hits).
  - The archetypes' rat-trap hits on us were 909 against zc6's 946 on the 44 shared cells (0.96×), above the 0.9× bar: the mask did not keep our gatherers off the traps.
  - Wins 30 of 44 cells to 32 (D 14, +6 −8), funnel 32 of 48 to 34. Picks/100 rat-turns 34.49 (bar 33.8), delivered 311.1k to 354.8k (−12%), 1 starved king, 0 overruns, 0 exceptions.
  - It cost the economy as well: rat-turns −7.6%, pickups −9.3%, spawns −9.1%. Our rat deaths fell 9% (2,806 to 2,544), but so did our rat kills (1,537 to 1,385).
  - This is the sixth fight-time trap mask to fail its local check (t0b, u0u, u1l, u1b/u1n, u9n). On the archetypes, keeping the trap mask on does not keep our rats off the traps.
- 10:57: **u9b (u6x without TRAP_SHARE) REJECTED at replica block 2** (#226; set 1: 16155-16170, 16/16, twin-anchored `paired 12771-12813 --crown 11549-11677`).
  - Paired: 48 cells, 41 alike, D 7 (+4: Icarus rose B, YokoOno clouds B, Varun knifefight B, spsquared bed A; −3: Xzibit attack_on_cat A, YokoOno Stash A, spsquared bed B), net +1, t 0.38.
  - Pooled with block 1 (D 4, net +2): D 11, net +3, t 0.90, under the block-2 bar of 1. TRAP_SHARE stays on in ratbot.
  - YokoOno Stash A was lost on both sets, as by u8r and u9i: every recent candidate loses that cell to the twin.
- 10:57: **u9d submitted** (#227, u6x without KING_FLOW, the ablation that passed zd53 at 36 vs 32 wins). Block 1 is on set 3 after 16170: 4 sent, 12 queued for the drain; waiter build/wait_u9db1.sh. Queue after it: u8d.
- 11:59: **u9d (u6x without KING_FLOW) REJECTED at replica block 1** (#227; set 3: 16180-16183, 16270-16281, 16/16, twin-anchored `paired 13248-13291 --crown 12130-12163`).
  - Paired: 48 cells, 40 alike (11 replay the crowning game), D 8 (+3: Varun corridorofdoomanddespair B, spsquared TheHeist A and B; −5: Finals where_are_you B, Xzibit minimaze B, YokoOno Stash A, spsquared rift B and rose A), net −2, t −0.71. Block 1 rejects at t <= 0. KING_FLOW stays on.
- 11:59: **u8d submitted** (#228, u6x + RICH2_BABIES 80 at bank 3000; screen zd28 33 vs 32 wins). Block 1 is on set 3 after 16281: 8 sent, 8 queued; waiter build/wait_u8db1.sh. Its replica falsifier is starvation as our last king's death in more than 18 of the block's king-destroyed losses (u6x set 3: 15 of 26).
  - It is the last screened build waiting for the replica. The next candidate must be built and screened while u8d runs, and not in the rush or fight-time trap-mask areas.
- 13:00: **u8d (u6x + RICH2_BABIES 80 at bank 3000) REJECTED at replica block 1** (#228; set 3: 16289-16296, 16373-16380, 16/16, twin-anchored `paired 13248-13291 --crown 12130-12163`).
  - All 48 cells replay the crowning game (D 0, net 0, t 0.00): the switch is inert against the panel. With babies at the 40 cap our bank never reaches 3,000 on these maps, so the second tier never opens (the screen zd28 had D 1 too).
  - The replica queue of screened builds is now empty.
- 13:01: **u9p = u6x + CAT_LAY + CAT_RING − TRAP_SHARE** (TRAINING_ALGORITHM §6 step 5, near-misses in pairs; we are on the plateau: 8+ straight rejects and no accept in 24 h). Built from bots/u8r with TRAP_SHARE off; Cfg.java differs from u6x in exactly those three switches, and nothing else differs.
  - Why: the two nearest misses of the last day are u8r (pooled D 15, net +3, t 0.77) and u9b (pooled D 11, net +3, t 0.90), both on sets 3 and 1. Their switches act on different mechanisms (cat traps laid in cooperation vs the shared rat-trap-risk squeaks), so if both effects are real they should add.
  - Falsifiers (bots/u9p/SNAPSHOT): the screen's basics gate (overruns or exceptions, picks/100 below 33.8, or D 0); replica cheese transferred below 0.9× the twin's same-set value (set 3: 3,968; set 1: 4,133), the falsifier u8r failed on set 1; the standard block rules.
  - Screen zd61 launched at 13:02 on the VM (chain after zd60, `runs/xt2/cells.tsv` layout); waiter build/wait_zd61.sh.
- 13:20: **u9p passes screen zd61** (against zc6 = u6x; falsifier 1 holds).
  - Wins 38 of 44 cells to zc6's 32 (D 12, +9 −3; gained abrk canyoudig B and tiny B, araid DefaultMedium B, arrows A, canyoudig B, cheesefarm A and B, evileye A, tiny B; lost araid DefaultMedium A, DefaultSmall A, starvation A). Funnel 41 of 48 games to 34.
  - Picks/100 rat-turns 34.21 (bar 33.8), delivered 352.0k to 354.8k, 2 starved kings, 0 overruns, 0 exceptions.
  - On the 44 shared cells: cat-trap damage 38,260 to 16,226 (2.4×); cheese transferred 321.6k to 324.2k (−0.8%); rat kills 1,956 to 1,537 (+27%), our rat deaths 2,862 to 2,806 (+2%). The archetypes' rat-trap hits on us rose to 1,044 from 946 (+10%): with TRAP_SHARE off our babies no longer share trap-risk marks.
  - For comparison, the two halves alone: u8r (zd42) 34 vs 32 (+4 −2), u9b (zd51) 37 vs 32 (+8 −3).
- 13:20: **u9p submitted** (#229). Set-3 block 1 after 16398: 12 sent, 4 queued; waiter build/wait_u9pb1.sh.
- 13:58: **u9p, set-3 block 1** (#229; 16422-16433, 16470-16473, 16/16; twin-anchored `paired 13248-13291 --crown 12130-12163`).
  - 48 cells, 40 alike (10 replay the crowning game), D 8 (+5 −3), net +2, t 0.71. Gained YokoOno 5t4rv4t10n_1337 B and externalsustenance B, spsquared TheHeist A and B, collegejunior FreeTheCheese B; lost Xzibit minimaze B, YokoOno 5t4rv4t10n_1337 A, spsquared rift A.
  - YokoOno Stash A is not among the changed cells: u9p keeps the twin's win there, which u8r, u9i, u9b and u9d all lost.
  - Falsifier 2 holds: cheese transferred is 4,466 a game (field_report xUs, 48 games) against the floor of 3,968 (twin 4,409, +1.3%). Cat-trap damage is 1,414 a game (u8r on this set 1,401; twin 826).
  - Block 1 rejects at t <= 0, so it continues. Block 2 (set 1) was queued at 13:58: 4 sent, 12 queued; waiter build/wait_u9pb2.sh.
- 13:58: **VM replay prune** (owner rule #23). The VM root was at 2.1 GB free with the replica's replay store at 7.5 GB (about 2,100 replay files a day, about 2.4 GB a day, now that the field plays around the clock).
  - Pruned the 1,847 files (2.3 GB) older than 48 hours, listed in /root/bc26-replays-pruned-20261105-2157.txt. The root now has 4.2 GB free. Our own copies under ~/projects/vibe/bc26b-galaxy-replays are untouched.
  - At this rate the root falls back to 2 GB in about a day, so the prune needs repeating daily (or a timer).
- 14:35: **u9p, set-1 block 2** (#229; 16486-16489, 16526-16537, 16/16; twin-anchored `paired 12771-12813 --crown 11549-11677`).
  - 48 cells, 38 alike (7 replay the crowning game), D 10 (+7 −3), net +4, t 1.26. Gained Finals DefaultSmall B, Icarus rose B, YokoOno clouds B, Varun knifefight B, spsquared bed A, fullofsecrets A and B; lost Icarus rose A, spsquared bed B, collegejunior clockwisdom B.
  - Six of the seven gains were cells one of the halves had gained on this set: u8r (fullofsecrets A and B, rose B, bed A) and u9b (rose B, clouds B, knifefight B, bed A). All three losses were lost by a half too: rose A and clockwisdom B by u8r, bed B by u9b. So the pair carries both halves' gains with fewer of their losses (u8r −4, u9b −3 here).
  - Pooled with block 1 (D 8, net +2): D 18, net +6, t 1.42. Block 2 rejects at t <= 1 and accepts at t >= 2, so it continues to block 3, which rejects at t <= 1.5.
  - Falsifier 2 holds on set 1: cheese transferred is 4,425 a game against the floor of 4,133 (twin 4,592, −3.6%; u8r had 3,941). Cat-trap damage is 1,256 a game (u8r 1,252; twin 1,016).
- 14:35: **u9p block 3 (set 2)** queued after 16537 (`build/block_v4s2.sh`, new: the set-1 script on `v4-set2.json`): the hourly limit took none at once, so all 16 wait for the drain; waiter build/wait_u9pb3.sh. It is scored against the twin's set 2 with `paired 12838-12899 --crown 11678-11702` (the 11-03 22:40 rule).
- 15:36: **u9p (u6x + CAT_LAY + CAT_RING − TRAP_SHARE) REJECTED at replica block 3** (#229; set 2: 16572-16575, 16591-16594, 16636-16643, 16/16; twin-anchored `paired 12838-12899 --crown 11678-11702`).
  - Block 3: 48 cells, 40 alike (10 replay the crowning game), D 8 (+4: YokoOno streetsofnewyork A, Varun clouds A, spsquared evileye B and jail B; −4: Varun clockwisdom B, ratsareants A and B, Merlin knifefight A), net 0, t 0.00.
  - Pooled over sets 3, 1 and 2: 144 cells, D 26 (+16 −10), net +6, t 1.18, under block 3's bar of 1.5.
  - Varun ratsareants A and B were also lost by u7d on this set (the 11-03 22:40 re-anchoring), and YokoOno streetsofnewyork A and spsquared evileye B were gained by it, so set 2's changed cells repeat across candidates.
  - It is the best pooled result of this reign's candidates (u8r: D 15, net +3 over two sets; u9b: D 11, net +3), but at about +4% of cells it is below what three blocks can resolve. A fourth set (set 4) would need a u6x reference block first (the 11-03 22:40 note).
  - The switches stay off in ratbot. The pair is closed as a pair; the near-miss-pairs step (§6 step 5) has been tried once.
- 17:28: **Owner, PROMPTS #27-28**: never ask permission to try an idea, never stop trying new ideas, and the two hours the screen machine sat idle while u9p's blocks ran were wasted. From now on a batch of candidates screens while the replica plays (screens take about 17 minutes, a replica block about an hour), so the replica always has a screened build waiting.
- 17:32: **u6x resubmitted** (#230) so the replica's ranked games play the incumbent, not the rejected u9p. Its **set-4 reference block** is queued after 16818 (`build/block_v4s4.sh`, new), so later candidates can have a fourth block; waiter build/wait_u6xr4.sh.
- 17:32: **Batch: u9p plus the reign's other near-misses** (§6 step 5, stacked). All four are u9p with only these changes:
  - u9q = u9p + RETREAT_HP 0 (u7d: reached block 3).
  - u9r = u9p + THROW_ADAPT (u7i's code: never retreat once an enemy has been seen carrying one of ours; u7i pooled t +2.14 at block 2, failed its Merlin throw-hit falsifier).
  - u9s = u9p + SEEK_CROWD 3 (u8q: block 1 D 5, +4 −1, t 1.34).
  - u9t = u9p + THROW_ADAPT + SEEK_CROWD 3.
  - Screens zd62-zd65 run one after another at 3 jobs each (runs/seq_zd62.sh on the VM). The falsifiers are in each SNAPSHOT: u9p's (the basics gate, the replica transfer floor, the block rules).
- 17:41: **Capability sweep on u9p** (§6 step 2, rerun on the current base): 15 switches that are off in u9p and have not been tried since u6x became the base, each switched on alone in a copy of u9p (Cfg.java differs by that one line). They were picked for where we lose: king survival, the rat war, the economy.
  - v0a KING_DODGE, v0b YIELD_KING, v0c KING_STRICT, v0p BITE_GUARD, v0e BITE_CARRIER, v0f RESCUE, v0g GRAB_MICRO, v0h THROW_CHECK, v0i KING_FORAGE, v0j FACE_REACH, v0k CAT_BLIND, v0l MINE_ANY, v0m GATHER_STILL, v0n BUG_DIG, v0o SEEK_FRONTIER.
  - Screens zd66-zd80 (runs/seq_zd66.sh on the VM) start when zd62-zd65 finish, one at a time at 3 jobs, about 17 minutes each, so the sweep runs about 4.5 hours while the replica plays.
  - Pre-registered (each SNAPSHOT): scored against u9p's own screen zd61 (38 wins of 44 cells). Any overrun or exception, picks/100 below 33.8, D 0, or wins below 37 rejects. The passer with the most wins goes to the replica.
  - Slip, caught at once: bots/v0d already existed (an October snapshot), so the BITE_GUARD copy is v0p. The sed on the old v0d matched nothing (that snapshot has no BITE_GUARD), and git shows it unchanged.
- 17:45: **u6x set-4 reference complete** (#230; 16848-16863, 16/16): 28 of 48 games won (spsquared 6/6, Finals 4/6, collegejunior 4/6, Icarus 4/6, Xzibit 3/6, Varun 3/6, YokoOno 2/6, Merlin 2/6). A candidate's fourth block is scored plainly with `contest.py paired 16848-16863 IDS`.
- 17:45: **Points losses with a full bank** (u9p's 96 replica games, field_report). 51 losses: 42 king kills, 9 on points at r2000.
  - In three of the points losses we banked 9,144-14,627 cheese and held one king fewer: spsquared rift (3 kings to 4, 40-59), superioristhmus (2 to 3, 46-53; 3 to 4, 46-53).
  - The points formula (GameWorld.setWinnerIfMorePoints, at war): 0.5 × our share of kings alive + 0.3 × our share of cat damage + 0.2 × our share of cheese transferred. The engine allows 5 kings to r1200 and 2 after; a king costs 50 cheese plus 2 a round. FORM_RICH stops at 4 kings, and only before r1150.
  - **Batch w0a-w0d** (all u9p plus): w0a FORM_RICH_MAX 5; w0b FORM_RICH_MAX 5 + FORM_RICH_BANK 2000; w0c FORM_LATE (a lone king may form a second until r1940); w0d FORM_RICH_MAX 5 + FORM_LATE. Screens zd81-zd84 go before the sweep (runs/seq_main.sh, which waits on zd62-zd65 by pid).
  - Falsifiers (each SNAPSHOT): the screen gate against zd61; D 0 with kingsFormed unchanged is inert and rejects; on the replica, u9p's falsifiers plus kings alive at the end of our points games not above the twin's.
- 17:45: Process slip: `pgrep -f` on the VM matched the ssh command's own shell, so `kill` ended the session that was reordering the queue (no screen was hurt; zd62 kept running). The new queue waits on its predecessor by pid (TRAINING_ALGORITHM §7: kill by pid, not pattern).
- 17:52: **u9q (u9p + RETREAT_HP 0) passes screen zd62.** Against zc6 (its registered gate): 40 wins of 44 cells to 32. Against u9p's zd61: 40 to 38 (D 8, +5: araid DefaultSmall A, arrows B, evileye B, starvation A and B; −3: abrk arrows A, araid DefaultSmall B, tiny B).
  - Funnel 44 of 48 games, picks/100 rat-turns 34.08 (bar 33.8), delivered 340.6k (u9p 352.0k), 0 starved kings (u9p 2), 0 overruns, 0 exceptions. On the shared cells transfer is −4.9% against u9p.
  - **Submitted** (#231). Set-3 block 1 after 16867: 4 sent, 12 queued; waiter build/wait_u9qb1.sh. Scoring and falsifiers as u9p's: `paired 13248-13291 --crown 12130-12163`, transfer floor 3,968 on set 3.
- 18:04: **How u9p's replica losses end** (king_report on all 48 matches, 144 games; scratch kd/). 59 losses with every king of ours destroyed. The last king starved in 34 (58%: 12 before r400, 14 in r400-1000, 8 after), was bitten in 16, hit by thrown rats in 7, scratched by a cat in 2.
  - 12 of the 34 starvations had no enemy damage on that king at all. By opponent: YokoOno and Xzibit 9, Merlin 9, Icarus 8, Finals 3, Varun 3, collegejunior 2.
  - The starvations follow the army's collapse. In whatsthecatdoin vs Merlin (16472 g3) we held 19-20 rats to r125 and Merlin 15-18; ours fell to 8 by r175 and 0 by r250 while Merlin's stayed at 17. Our bank went 1,845 (r25), 320 (r125), 1 (r250), and the king starved at r298.
  - Spawn cost is 10 + 10 per 4 live babies (RobotControllerImpl.getCurrentRatCost), so each replacement at 19 babies costs 50.
  - So a bigger bank reserve only delays these deaths (u9i's reserve from r300 was rejected at block 1). The lever is the rat war itself.
- 18:04: **x0a, x0b = u9q + FORT_R 4 / 6** (existing switch, never a candidate). At war, fighters hold a ring within Chebyshev R of our king, facing out, and engage what comes within R+3, so the rat war is fought beside the king, which bites too (dist² 8). Falsifiers (SNAPSHOT): the gate against zd62, wins below 39, or our rat deaths on the shared cells not below u9q's. Screens zd85-zd86 go after w0a-w0d and before the sweep (runs/seq_main2.sh replaced seq_main.sh, killed by pid while it waited).
- 18:07: **The rat war in u9p's king-kill losses** (battle_report r0-2000 on all 59; scratch br/). Per side in those games:
  - Grabs of enemy rats: we made 5,660, they made 5,366. Throws: ours 4,339, theirs 4,110. Their grabs of ours came front/weaker 2,056, behind/weaker 1,558, behind 1,278, front 564; ours of theirs split the same way.
  - Rat deaths: ours 3,891 (enemy 3,057, cats 788, own 46), theirs 3,813 (enemy 2,277, cats 1,452). Our kills 2,277, theirs 3,057.
  - The asymmetry is traps: their rat traps killed 805 of ours (113.7k HP, our second-largest HP loss after throw landings at 119.6k), and we killed 0 with traps (u9p lays none). Bites killed 496 of ours and 290 of theirs. Everything else is roughly even, and they lose more rats to cats than we do.
  - By opponent (our deaths, top causes): Xzibit/YokoOno 899 (throwLand 407, trap 164), Icarus 702 (bite 192, trap 144), Finals 669 (trap 270, bite 179), Varun 655 (trap 207, throwLand 205), Merlin 515 (throwLand 277), collegejunior 396 (throwLand 211).
  - The engine shows a team only its own traps (RobotControllerImpl.getMapInfo reads the trap of the sensing team), and only its own can be removed. So enemy traps can only be avoided, and six avoidance masks have failed their local checks.
- 18:08: **u9r (u9p + THROW_ADAPT) REJECTED at screen zd63** (falsifier 1, the picks bar): picks/100 rat-turns 33.31 < 33.8. Wins 35 of 44 cells (zc6 32, u9p 38; D 13 against u9p, +5 −8), funnel 38 of 48, delivered 342.7k, 1 starved king, 0 overruns. u9t (u9r + SEEK_CROWD 3) is still screened (zd65) as registered.
- 18:09: **y0a = u9q + u8f's field-dose reactive rat traps** (BABY_RAT_TRAPS, TRAP_ADJ, TRAP_CAP 25, TRAP_SPAWN_FIRST, RAT_TRAP_MIN_CHEESE 300; five lines of Cfg.java). It answers the census's asymmetry, 805 trap deaths to 0, on the new base. u8f was rejected on u6x at block 2 (pooled D 11, net −1).
  - Falsifiers (SNAPSHOT): the gate against zd62 (wins below 39 rejects); more than 2 starved kings rejects, as that is how u0q failed; on the replica, our rat-trap kills under 5 a game rejects.
  - Screen zd87 goes second in the queue (runs/seq_main4.sh; seq_main3 killed by pid while it waited).
- 18:54: **u9q set-3 block 1** (16881-16885, 16931-16942; 48 cells): `paired 13248-13291 --crown 12130-12163` D 11 (+7 −4), net +3, t 0.90, above block 1's reject line (t ≤ 0). Cheese transferred 4,358 per game (floor 3,968) and cat-trap damage 1,376 (field_report, 48 games). Block 2 (set 1) queued: 4 sent (14149-14152), 12 in the drain queue.
- 18:54: **u9s (u9p + SEEK_CROWD 3) REJECTED at screen zd64** (the picks bar): picks/100 rat-turns 32.78 < 33.8. Wins 39 of 44 (zc6 32, u9p 38; D 5 against u9p, +3 −2), 5 starved kings, 0 overruns. Rat-turns rose 5% (1.243M vs 1.187M) while pickups rose 3.5%, so the crowd-seeking scouts walk more than they pick.
- 18:54: **u9t (u9r + SEEK_CROWD 3) REJECTED at screen zd65** (the picks bar): picks/100 33.64 < 33.8. Wins 37 of 44 (u9p 38; D 5, +2 −3), 2 starved kings, 0 overruns. With u9r and u9s, THROW_ADAPT and SEEK_CROWD are closed on this base.
- 18:54: tooling. build/wait_run.sh passed `$1` to the VM inside single quotes, so it tested runs//DONE and never fired (the zd63 and zd64 waiters hung for hours); fixed with double quotes. The laptop disk hit 100% (8 MB free) after the fetch of block 1's replays: removed build/dumpcache (1.1 GB of game_report dumps from this loop's Nov 4-6 king/battle reports; rebuilt on demand), leaving 1.1 GB free. Replay analysis runs on the VM from now on, so the local cache stays empty; bc26b-galaxy-replays (4.2 GB) is the next thing to prune.
- 18:59: **screen protocol: a screen stops once its wins gate is unreachable.** The screen queue (22 runs, about 35 min each with the replica's four workers sharing the VM's 8 cores) is now the loop's bottleneck. tools/abort_watch.py (on the VM: runs/abort_watch.py runs/gates_main4.tsv, pid 1048836) watches the run in progress, and once its losses on the cells it shares with its registered control exceed shared − MIN_WINS it kills that run's arena process and its games and writes runs/RUN/ABORTED; seq_main4.sh then moves on. The gates (research/screens/gates_main4.tsv) are the registered falsifiers, checked against each SNAPSHOT: v0*, w0* vs zd61 (u9p) at 37 wins, x0a, x0b, y0a vs zd62 (u9q) at 39. Dry run on the finished screens: u9r would have stopped at its 8th loss (35 wins, 9 lost); u9s (39) and u9t (37) run to the end. An aborted screen is a rejection on the wins gate; the picks bar and D are not measured. The laptop (2 cores, 1 GB) cannot host a second queue.
- 18:59: the fast losses in u9q's set-3 block (tiny r122 vs waxmango, battle_report): the rat war there is grab-and-throw. They grabbed us 108 times to our 67 and threw 84 to 37; 26 of our 67 grabs ended when our carrier was itself grabbed (39%, theirs 17%); 47 of their grabs came from our front on a rat weaker than the grabber (engine: any strictly healthier rat may grab a watching enemy, HEALTH_GRAB_THRESHOLD 0, no healing). We had more rats (27 to 15 at r31) and lost them 46 to 25. Most such losses were opened by our rushers (bs r11-26). BITE_GUARD, GRAB_MICRO and BITE_CARRIER (v0p, v0g, v0e) test this on u9p; they are 6th-8th in the queue.
- 19:03: **v1a = u9q + GRAB_SAFE** (new switch, about 30 lines in Baby.java): a baby skips a grab (unless the victim carries one of ours) while another free enemy baby within Chebyshev 2 is healthier than it (it may then grab the carrier from the front) or can step onto a tile next to it outside its 90-degree cone (5 of the 8 neighbours; those tiles are unsensed, so walls and dirt come from Sym memory). Skips count in the indicator string (gs=). Falsifiers (SNAPSHOT): the screen zd88 vs zd62, wins below 39 (abort gate); the basics gate; our rat deaths on the shared cells not below u9q's. Queue: seq_main5.sh (pid 1055814) replaced seq_main4.sh (killed by pid 970563 while zd81's arena, pid 1026912, kept running) and puts zd88 after zd87; abort_watch restarted (pid 1055815) with zd88's gate.
- 19:05: **w0a (u9p + FORM_RICH_MAX 5) REJECTED at screen zd81 as inert** (registered: D 0 with kingsFormed unchanged): all 44 shared cells and every total (turns, pickups, deaths, kingsFormed 31) equal to u9p's zd61; 38 wins (zc6 32), picks/100 34.21. No screen game held four kings with a 3,000 bank before r1150, where rich formation acts. **w0d (w0a + FORM_LATE) dropped unscreened** (zd84 marked DONE with runs/zd84/SKIPPED): FORM_LATE only acts after r1150 and FORM_RICH_MAX only before, so w0d's screen is w0c's (zd83) game for game. w0b (FORM_RICH_BANK 2000) still engages more often and keeps its screen (zd82).
- 19:10: Merlin's economy edge (econ_report on 16941/16942; u9q lost all six): on cheesebottles we never came within 4 tiles of our home mine (19,2), which Merlin worked from r88 (2,300 cheese), and our (2,2) waited 156 rounds a pile; on whatsthecatdoin we never worked (36,2) (660 left) and picked 434 in 298 rounds against 1,750 spent on spawns and 450 on traps before the king starved. Locally (u9q vs araid, cheesebottles, indicators kept on the VM in ~/diag26b/mc1): the king's mine list reads (20,13) by r100, (19,2) by r200, (2,2) by r300, though (2,2) is 9 tiles from the king: the home mines sit in walled bottles whose mouths are far round by path. Exploration is closed ground (SCOUT_FRONTIER t0x/t0y failed on this very map, SCOUT_MINES s9c/s9i, MINE_EXPLORE s7v/s7w, SCOUT_AREA s6l/s6o, SCOUT_EVERY 2 u8o), so no candidate from this; the queue keeps the grab-war (v1a, v0p, v0g, v0e) and fort (x0a, x0b) ideas ahead.
- 19:14: **y0a (u9q + reactive rat traps) REJECTED at screen zd87**, the first screen stopped by abort_watch (03:14 UTC): 6 of 44 shared cells lost after 23 games, so 39 wins were out of reach; falsifier 2 also fired (4 starved kings, bar 2). Over the 23 games against u9q's zd62: 17 wins to 20 (D 9, +3 −6), our rat deaths 1,245 to 1,410 and kills 820 to 748, but 1,798 rats spawned to 2,021, kings formed 12 to 16, delivered 139.9k to 150.0k, cat-trap damage 15.1k to 22.4k. As in u0q, u8f and u9j, the traps win the exchange they fire in and lose the economy that pays for them. Reactive baby traps are closed on this base.
- 19:31: **v1a (u9q + GRAB_SAFE) REJECTED at screen zd88** (abort_watch, 03:29 UTC): 6 of 44 shared cells lost after 37, so 39 wins were out of reach. Against u9q's zd62 on those cells: 31 wins to 34, D 7 (+2: araid DefaultMedium A, DefaultSmall B; −5: abrk evileye A, araid evileye A and B, wallsofparadis A and B, all long games), picks/100 33.37 (also under the 33.8 bar). The mechanism hardly moved: our rat deaths 2,291 to 2,309 (−0.8%), kills 1,583 to 1,533, spawned 3,228 to 3,203. Skipping exposed grabs does not change the exchange; the carriers grabbed on tiny are not a lever on this screen.
- 19:34: **Screen-gate correction (methodology).** The gates I registered for the u9p/u9q children (wins at the parent's draw minus 1, plus the 14:27 picks bar at 33.8) are much stricter than TRAINING_ALGORITHM §6 (reject at paired t ≤ −1.5 on the cells, basics worse than the parent's beyond noise, joint false reject held near one neutral in eight). Two reasons it went wrong: the parent's screen is a selected high draw (u9q was sent up for its 40, against u9p's 38 and u6x's 32), so chaos flips of a neutral child run mostly win→loss; and the picks bar sits on the family's own level (u9q reads 33.80 picks/100 on the shared cells, u9p 34.30). New tool tools/screen_t.py (now printed first by tools/score_screen.sh): paired t on per-cell win/loss and the per-cell paired picks/100 difference with its standard error (1.1-1.4 here). Under §6 every recent screen rejection would have passed: u9r t −0.83 (picks z −1.00), u9s +0.44 (−0.41), u9t −0.44 (−0.28), v1a −1.14 at 37 cells (−0.67), y0a −1.00 at 23 cells (+1.73; y0a also fails its starved-kings falsifier, which stands). Those verdicts stand as registered. **Re-registered before any of their results were read** (falsifiers_v2 in each SNAPSHOT, 19 builds; research/screens/gates_main5.tsv): w0b, w0c, v0a-v0p, x0a, x0b now reject at paired t ≤ −1.5 against the parent's screen, at picks/100 per-cell z ≤ −2 against the parent, at any overrun or exception, or at D 0; their mechanism falsifiers are unchanged. abort_watch now takes t:T gates and stops a run only when its best final t (every unplayed cell a win) is at or below T (pid 1104465 on runs/gates_main5.tsv). The replica remains the only accepting gate; if u9q fails a block, the next submission is the best §6 passer by screen t, as a fresh registration.
- 19:47: **u9q REJECTED after replica block 2** (its falsifier, the transfer floor). Block 2 (set 1, 16957-16964, 17011-17018) vs `paired 12771-12813 --crown 11549-11677`: D 12 (+9: AdamTan12_Finals DefaultSmall A, Icarus rose B, YokoOno arrows A and clouds B, Varun knifefight B, Merlin thunderdome A and B, spsquared bed A and fullofsecrets B; −3: Icarus rose A, YokoOno Stash A, waxmango clockwisdom B), net +6, t 1.73; pooled over both blocks 96 cells, D 23, net +9, t 1.90 (block 2 would have continued: accept needs 2, reject at 1). But cheese transferred on set 1 was 3,915 a game (field_report, 48 games) against the registered floor of 4,133 (0.9 × the twin's 4,592), the falsifier that rejected u8r (3,941) on the same set; u9p held 4,425 there. Set 3 was 4,358 (floor 3,968). Cat-trap damage 1,275 a game. Reading: RETREAT_HP 0 wins rat fights (Merlin's thunderdome both sides) and costs about 11% of delivery, likely wounded gatherers dying with their cheese instead of going home. The cells stay on record; no rescue of u9q.
- 19:52: **w0b REJECTED at screen zd82 as inert** (falsifiers_v2: D 0 rejects). Against u9p's zd61: n 44, D 0, 38 wins each; picks/100 34.69 vs 34.30 (per-cell z +1.23); kingsFormed 34 vs 31, transferAmt 324.2k vs 321.6k, deaths 2,858 vs 2,862; 0 overruns, 0 exceptions, 2 starved kings. FORM_RICH_BANK 2000 forms 3 more kings in 48 games and changes no result: the points games it targets do not occur on the xt2 cells.
- 19:52: **z0a = u9p + RETREAT_CARRY** (new switch, one line in Baby.retreat): the RETREAT_HP 40 retreat applies only while carrying raw cheese; a wounded empty-handed baby keeps fighting. Target: keep u9q's rat-fight wins (pooled 96 replica cells D 23, net +9, t 1.90) without its delivery loss (set 1 3,915 a game, u9p 4,425; screen shared-cell transfer −4.9%). Falsifiers in bots/z0a/SNAPSHOT, registered before any game: screen zd89 vs u9p's zd61 under §6 (t ≤ −1.5, picks z ≤ −2, overrun or exception, D 0) plus shared-cell transferAmt below 0.97 × u9p's; replica block rules and the per-set transfer floor (set 3 3,968, set 1 4,133).
  - **Submitted** (#232) as soon as it compiled, so the replica would not sit idle after u9q's last games (19:31). Set-3 block 1 after 17018: 4 sent, 12 queued; waiter build/wait_z0ab1.sh. Its screen zd89 runs in parallel, first in the VM queue (runs/seq_main6.sh replaces seq_main5 after zd83; abort gate research/screens/gates_main6.tsv via runs/abort_watch2.py); a screen reject stops the block.
- 19:56: **VM screen queue moved to tools/screenq.sh** (runs/screenq.txt, one run a line, reorderable under flock runs/screenq.lock). Three stale idle screenq workers from 10-29 (jobs 4 each, pids 2685070, 2685085, 2842259 and their wrappers) were still polling an empty queue and would each have taken the next run; killed by pid. screenq.sh now runs nice and touches DONE after an aborted run. Order: zd89 (z0a), zd90 (z0b), then zd69 v0p, zd72 v0g, zd70 v0e, zd85 x0a, zd86 x0b and the v0 sweep. tools/abort_watch.py re-reads its gates file every pass and with --follow keeps watching; new gates go to research/screens/gates.tsv (VM runs/gates.tsv, runs/abort_watch3.py).
- 19:56: **z0b = u9p + STEP_IN 8** (new switch, Micro.score; step-in discipline, on the list since the 18:19 trap-hit census). In a fight that is ours, a tile with no enemy in bite reach but an enemy at Chebyshev 2 whose cone covers it scores −8 (approach is worth 3 a step), so the baby holds at 3 or steps in and bites, instead of ending its turn where the enemy steps in and bites first. Enemies facing away are chased as before. Falsifiers in bots/z0b/SNAPSHOT (registered before any game): §6 screen gates vs zd61 plus an inertness check (killsRat not up while deathsRat not down); replica block rules and the transfer floor. Screen zd90, second in the queue.
- 20:10: **w0c (u9p + FORM_LATE) passes screen zd83 under its falsifiers_v2** (§6 gates vs u9p's zd61): n 44, D 1 (−1: abrk evileye A, a points win at r2000 for u9p became a king-death loss at r1896), t −1.00 (bar −1.5); picks/100 34.28 vs 34.30, z −1.00 (bar −2); 0 overruns, 0 exceptions; 37 wins of 44 (u9p 38). kingsFormed 31 = 31: no late second king formed on these cells, so the screen says nothing for the mechanism, only that it costs nothing measurable. It joins the replica queue behind z0a, with its registered replica falsifier (kings alive at the end of our points games not above the twin's) on top of the block rules.
- 20:33: **z0a REJECTED at screen zd89** (its registered transfer falsifier). Against u9p's zd61: n 44, D 9 (+5 −4), net +1, t +0.33; picks/100 33.89 vs 34.30, per-cell z +0.36; 0 overruns, 0 exceptions, 0 starved kings; 39 wins of 44. But shared-cell transferAmt 302.4k against u9p's 321.6k, 0.940 of it, below the registered 0.97 (u9q read 0.951). Rat-turns were 4.7% fewer (1.131M vs 1.187M: games ended sooner), so per rat-turn the gap is −1.3%; the registered rule is on the total, and stands. The four losses swing points wins into king deaths (araid arrows A, evileye A) or back, so carry-only retreat is not u9q's wins without u9q's cost: it reproduces u9q's delivery loss and half its cells. Lesson for registration: state transfer gates per rat-turn (or on cells both sides finished at r2000), not as a total that game length moves.
  - Replica: all 16 block-1 requests had already been sent (drain 20:31) and a match is bound to the build active when it is created, so the block plays as z0a. Its results are diagnostic only (does carry-only retreat cost delivery on the replica too?); no decision rests on them. The next submission waits for z0b's screen zd90 (started 04:31 UTC), as the replica is busy with z0a's matches for ~1.5 h anyway.
- 20:46: **z0b REJECTED as inert; built on a disabled path** (my error). STEP_IN was written into Micro.score, but Micro runs only with Cfg.MICRO, false in u9p; at 26 of 48 games screen zd90 read D 0 and picks/100 identical to u9p's (32.21), and it was stopped by hand (runs/zd90/ABORTED). The idea itself is already live: Cfg.STANDOFF (on since u2g; its ablation u3l lost 4 cells and gained 5, transfer −9%) holds and faces an enemy rat that sees us and is not weaker, in Baby.war. Closed. Before building, check that the method runs (the 'why' branch is reachable with the incumbent's switches) and grep Cfg for an existing switch.
- 20:46: **z0a's replica block 1, diagnostic only** (set 3, 17040-17043, 17045-17052, 17079-17082; `paired 13248-13291 --crown 12130-12163`): D 8 (+4 −4), net 0, t 0.00; transfer 4,061 a game (field_report on the VM, 48 games; floor 3,968, u9q 4,358). It confirms the screen verdict: no gain, delivery down.
- 20:46: **w0c submitted** (#233, u9p + FORM_LATE; passed screen zd83). Set-3 block 1 after 17082: 4 sent, 12 queued; waiter build/wait_w0cb1.sh. Falsifiers as registered in its SNAPSHOT: the block rules and transfer floor, plus kings alive at the end of our points games not above the twin's.
- 20:49: **What decides our r2000 points losses** (field_report games on the VM, ~/diag26b/rp: 318 replica matches, 954 games, u-era builds; scratch ptsgames.py). 205 games went to points at the round limit: 142 won, 63 lost. At war the points are 0.5 × kings share + 0.3 × cat-damage share + 0.2 × transfer share (GameWorld.setWinnerIfMorePoints; in cooperation 0.3 / 0.5 / 0.2). In the 63 losses our mean deficit was 3.9 points on kings, **6.8 on cat damage**, 4.7 on transfer; parity on cat damage alone would have won 18, on kings 9, on transfer 8. Losses by opponent: spsquared 19 (superioristhmus 10, rift 6, rose 2), Merlin 10 (FreeTheCheese, cheesebottles), Xzibit 9, Varun 9. Many are lost with more kings than theirs, on a few hundred cat damage a side: Icarus averystrangespace 49-50 (kings 2-1, cat 110 to 386), rose 48-51 (460 to 2,422), spsquared jail 45-54 (32 to 520), YokoOno averystrangespace (0 to 2,740), Varun corridorofdoomanddespair 41-58 (332 to 1,280). Backstab: them 36, us 27 (median r145); after a backstab only the backstabbed team may lay cat traps, for 100 rounds, so war cat damage is bites (10 each).
- 20:49: **z0c = u9p + END_CAT 1650** (new switch; Baby.endCat at the head of moveDecision). From r1650 at war, a baby of any role with less than DELIVER_AT raw, no rat in its paws, no threatened king to defend and no enemy rat within dist² 8 walks to the nearest seen or known cat; the bite step already bites any cat beside it. Falsifiers (bots/z0c/SNAPSHOT, before any game): §6 screen gates vs zd61; mechanism, our dmgCat in shared-cell games reaching r1650 must rise; replica block rules, the per-set transfer floor, and our cat-damage share in the block's points games above the twin's. Screen zd91, next in the VM queue (after zd69, running).
- 20:51: **z0d = u9p + STAB_LAY** (new switch). When the enemy backstabbed, the engine lets us keep placing cat traps for 100 rounds (GameWorld.catTrapsAllowed), but our lane traps (CAT_LAY) ran only in cooperation and tryCatTrap wanted a projected surplus at war. Through that window the cooperation rules now hold; each placement is still checked by canPlaceCatTrap. Target: the 36 of 63 points losses the enemy backstabbed. Falsifiers in bots/z0d/SNAPSHOT (before any game): §6 screen gates; mechanism, our catTraps in shared-cell games they backstabbed must rise; replica block rules, transfer floor, cat share in points games. Screen zd92, after zd91.
- 21:16: **w0c replica block 1 (set 3) continues.** 17090-17093, 17095-17098, 17103-17110 vs `paired 13248-13291 --crown 12130-12163`: D 8 (+5: YokoOno 5t4rv4t10n_1337 B and externalsustenance B, spsquared TheHeist A and B, collegejunior FreeTheCheese B; −3: Xzibit minimaze B, YokoOno 5t4rv4t10n_1337 A, spsquared rift A), net +2, t 0.71 (block-1 reject at t ≤ 0). Transfer 4,462 a game (floor 3,968; twin 4,409). Kings alive at the end of our points games: 2.00 over 16 games, twin 1.87 over 15 (its registered falsifier needs above the twin: holds). Block 2 (set 1) queued after 17110 (16 queued for the drain); waiter build/wait_w0cb2.sh. Block 2 rejects at pooled t ≤ 1 and accepts at t ≥ 2 with D ≥ 4.
- 21:25: **z0c (u9p + END_CAT 1650) passes screen zd91.** Against u9p's zd61: n 44, D 1 (−1: abrk arrows A, a points win became a king death at r1555), t −1.00 (bar −1.5); picks/100 33.60 vs 34.30, per-cell z −0.39 (bar −2); 0 overruns, 0 exceptions, 2 starved kings; 37 wins of 44. Mechanism (registered: our dmgCat in shared-cell games reaching r1650 must rise): 17 such games, our dmgCat 81,172 to u9p's 74,827 (+8.5%), mean cat share 0.629 to 0.582 (+0.047, about +1.4 war points a game). Transfer 303.1k to 321.6k (no screen transfer gate was registered for it; the replica floor applies). Replica queue: after w0c.
- 21:25: **Bytecode chaos in the screens** (scratch diverge.py on the VM; seeds are fixed at 0, so a game changes only if some robot's computation changes). The one cell z0c lost ended at r1555, before END_CAT can act: of the 14 zd61 games that end before r1650, 3 differ under z0c, whose only change before then is a few bytecodes a baby turn (one short-circuited test in moveDecision). Spare-bytecode searches (KP_RESERVE, FLOW_RESERVE) make behaviour depend on the bytecode left. w0c and w0a (no code on the per-turn path) leave all 14 identical; z0a, a change in live fight code, leaves 1 of 14. So a code change that touches the per-turn path turns roughly 1 early game in 5 into a fresh draw, and its screen D includes those flips as noise. Of the totals, turnsRat −4.6% on z0c is mostly shorter games (rounds 50,635 vs 52,450), not fewer rats.
- 21:26: **z0e = u9p + END_CAT 1450** (z0c's switch 200 rounds earlier; dose test). Falsifiers in bots/z0e/SNAPSHOT: §6 screen gates vs zd61; mechanism, our dmgCat in shared-cell games reaching r1450 must exceed z0c's on the same games. Screen zd93, next after zd92.
- 21:44: **z0d (u9p + STAB_LAY) passes screen zd92.** Against u9p's zd61: n 44, D 6 (+4: araid DefaultSmall A, evileye B, starvation A and B; −2: abrk arrows A, cheesefarm B), net +2, t +0.81; picks/100 32.12 vs 34.30, per-cell z −1.90 (bar −2: passes, narrowly); 0 overruns, 0 exceptions, 0 starved kings; 40 wins of 44 (u9p 38). Mechanism (registered: our catTraps in games the opponent backstabbed must rise): 18 such games, catTraps 318 to 298, catDmgTrap 27,670 to 26,460. Transfer 301.7k to 321.6k on near-equal rat-turns (1.190M vs 1.187M); killsRat 2,083 to 1,956. Replica queue: after w0c, with z0c.
- 21:44: **z0f = u9p + END_CAT 1650 + STAB_LAY** (both cat-share switches; the replica, at 4 blocks a candidate, is the bottleneck). Falsifiers in bots/z0f/SNAPSHOT before any game: §6 screen gates; mechanism, dmgCat on the shared cells must rise; replica block rules, transfer floor, cat share in points games. Screen zd94, next after zd93 (z0e).
- 22:08: **w0c REJECTED after replica block 2** (its registered mechanism falsifier: kings alive at the end of our points games not above the twin's). Block 2 (set 1, 17145-17148, 17162-17165, 17167-17170, 17173-17176) vs `paired 12771-12813 --crown 11549-11677`: D 10 (+7: Finals DefaultSmall B, Icarus rose B, YokoOno clouds B, Varun knifefight B, spsquared bed A, fullofsecrets A and B; −3: Icarus rose A, spsquared bed B, collegejunior clockwisdom B), net +4, t 1.26; pooled 96 cells D 18, net +6, t 1.42 (block 2 would continue: reject at 1, accept at 2). Transfer 4,425 a game (floor 4,133). But kings alive at the end of our r2000 points games: set 1 2.00 over 10 games to the twin's 2.22 over 9; pooled over both blocks 2.00 (26 games) to the twin's 2.00 (24). FORM_LATE was registered to win by holding a second king at the end; it does not, so the +6 has no mechanism behind it (the screen saw kingsFormed 31 = 31 too) and is read as chance. The 4 block-3 requests already sent play as diagnostics; the 12 queued rows were removed.
- 22:08: **z0e (u9p + END_CAT 1450) passes screen zd93, but the dose buys little.** Against u9p's zd61: n 44, D 3 (+1 araid arrows B; −2 abrk arrows A at r1535, abrk evileye B at r1608), net −1, t −0.57; picks/100 33.53, z −0.43; 0 overruns, 0 exceptions. Mechanism (registered: dmgCat in games reaching r1450 above z0c's on the same games): 93,162 to z0c's 90,641 (+2.8%; 19 games); against u9p 97,305 to 88,439 and cat share 0.626 to 0.576. So most of the gain is already in z0c's last 350 rounds; 1650 stays the version to stack. abrk arrows A, a u9p points win, is lost by z0c (r1555), z0d (r1745) and z0e (r1535) alike: a fragile cell.
- 22:19: **z0f REJECTED at screen zd94** (§6 picks gate). Against u9p's zd61: n 44, D 6 (+4 −2, the same six cells as z0d's), net +2, t +0.81; but picks/100 31.97 vs 34.30, per-cell paired z −2.46 (bar −2); 0 overruns, 0 exceptions. Mechanisms: dmgCat 166.6k to 161.2k over all shared cells; catTraps in opponent-backstab games 309 to 298. STAB_LAY's picks cost (z0d alone z −1.90, pooled −2.18) buys about 65 trap damage a game; END_CAT's (z0c z −0.39) buys about 370 in late games.
- 22:19: **w0c's diagnostic set-2 matches** (17184-17187, sent before its rejection): Finals and spsquared, 10 of 12 games won; no decision rests on them.
- 22:19: **z0c submitted** (#234, u9p + END_CAT 1650; screen zd91 passed with dmgCat +8.5% in late games). Chosen over z0d (screen net +2 but picks z −1.90, and its trap mechanism adds ~65 damage a game) and z0e (the 1450 dose adds 2.8% more damage). Set-3 block 1 after 17187: 16 queued for the drain (hourly limit); waiter build/wait_z0cb1.sh. Falsifiers as registered in bots/z0c/SNAPSHOT: block rules, transfer floor per set, and our cat-damage share in the block's points games above the twin's.
- 22:21: **w0c replayed u9p on the replica** (scratch cmpsets.py over field_report games on the VM). Set 1: w0c and u9p (16486-16537) are the same 48 games: 26 won, xUs 4,425, deaths by enemy 34.5 and by cats 17.6 a game, 896 rounds; set 3 differs in one game (cat damage 3,158 vs 3,163). FORM_LATE never fired, as on the screen (kingsFormed 31 = 31), and the replica is deterministic too, so its +6 against the twin was u9p's own draw on the same cells. Its mechanism falsifier caught it. Rule for later: a candidate whose screen shows its switch never firing goes to the replica only with a mechanism falsifier that can reject it.
- 22:21: **Per-round check of the transfer floor** (same data). u9q set 1: 3,916 a game over 821 rounds = 4.77 a round; the twin 4,592 over 855 = 5.37 (u9q 0.888 of it, still under 0.9); u9p 4.94 (0.92). So u9q's rejection holds per round as well, though a per-game floor does penalize builds whose games end sooner. z0a set 3: 4,061 over 975 rounds = 4.17, twin 4.38 (0.95).
- 22:26: **VM disk at 824 MB free** (below the 2 GB floor; the replica's replay store had grown to 6.3 GB). Pruned the 1,798 replay files (2.3 GB) older than 36 hours from /srv/bc26-galaxy/storage/bc26-replica-secure/episode/bc26/replays, listed in /root/bc26-replays-pruned-20261106-0626.txt; the root now has 3.0 GB free. Our copies under ~/projects/vibe/bc26b-galaxy-replays and ~/diag26b/rp are untouched. An hourly root cron that would prune only under 2.5 GB free was not installed (the harness denied the persistent job); the prune stays a by-hand step at every block (HANDOFF state).
- 22:34: **Points losses are cat-share losses, and our late war openings hand the victim the trap window.** field_report over the replica's u/w/z-era set-3/set-4 games (156): in the 16 lost r2000 points games, our cat damage was below theirs in 14, kings in only 6-7 (r1000+). Splitting cat damage into its trap part (scratch trapsplit.py, ~/diag26b/trapsplit.py on the VM, per game from the dumps' catDmgTrap): when we open the war we won 6 of 20 in those blocks, the victim's traps out-damaging ours 1,164 to 686 a game; nzjt cheesebottles, our first bite at r135-151 (BREAK_ROUND 30 makes every baby hostile from r30): their traps 3,700-4,000 to our 200-300. Over the first 168 games of the whole ~/diag26b/rp scan (sets 1-4, still running): we opened at r100+ in 14 and won 5, they opened at r100+ in 36 and we won 21; our early (<r100) openings 8 of 25.
- 22:34: **z0g = u9p + PEACE_FROM 100** (new switch; Baby.truce()): a truce that has lasted to r100 is kept (no bite, grab or carry of an enemy rat, no attacker bite on the enemy king, while cooperation holds from r100 on); the early breaks before r100 are untouched, which is what separates it from the closed PEACE family. Falsifiers in bots/z0g/SNAPSHOT before any game: s6 screen zd95 vs zd61 (D 0 does not reject: u9p broke at r100+ in 2 of zd61's 44 games); mechanism run zd96 (z0g and u9p each vs u6h and s7d, which never break, on the 8 maps of our late openings, both sides): any z0g opening at r100+ rejects, wins below u9p's - 2 reject; replica: block rules, transfer floor, no z0g opening at r100+, and wins above the twin's in the games where the twin opened at r100+. zd95 and zd96 are at the front of the VM screen queue (behind zd72, running).
- 22:44: **A cooperation game on rift banks 9.7k unscored.** Local u9p vs u6h on rift (~/diag26b/kf; neither broke, cooperation to r2000, u6h won on points): our third king formed at r1059, once the bank passed FORM_RICH_BANK 3000 (u6h's at r795); the bank was 1,251 at r500, 2,634 at r750 and 9,721 at the end. Cat damage 4,880 to u6h's 8,206 (traps 2,900 to 5,611; traps placed 39 to 65, fired 29 to 60), though u6h has neither CAT_LAY nor CAT_RING (its other differences: PEACE, TRAP_SHARE, RUSH_UNTIL 0); one game, noted for zd96's cooperation games. The trap floors are not the limit: over set 3/4's replica games our bank sat under 600 in 13% of cooperation rounds, nearly all on where_are_you (scratch bankcoop.py).
- 22:44: **z0h = u9p + FORM_RICH_BANK 1500** (from 3000; FORM_RICH_MAX stays 4). w0b (2000 + MAX 5) died as inert on the war-heavy screen, where banks stay low; z0h is tested where banks grow. Falsifiers in bots/z0h/SNAPSHOT before any game: s6 screen zd97 (D 0 does not reject); mechanism run zd98 (z0h vs u6h and s7d on zd96's 32 cells, control u9p's zd96 games): kingsFormed not above u9p's, king deaths above u9p's + 2, or wins below u9p's - 2 reject; replica: block rules, transfer floor, kings alive at r2000 in points games above the twin's. Queue: zd95 (running), zd96, zd98, zd97, then the old sweep.
- 22:45: **The full late-break scan** (trapsplit over all of ~/diag26b/rp, 636 replica games of 12771-17176, every build there): we opened the war at r100+ in 49 games and won 20 (41%; their traps 1,475 a game to our 1,167); they opened at r100+ in 142 and we won 94 (66%); no war in 53, won 43; early openings (<r100) win alike whoever opens (ours 28 of 94, theirs 90 of 298). So z0g's premise holds on the whole store: a late war is good for us when they open it and bad when we do. The second local rift game (u9p vs s7d, no war, won on points 3 kings to 1) formed our third king at r1073 again, with the bank past 3000 by ~r800 (4,885 at r1000): the first rich attempt seems to time out (FORM_TTL 60 + FORM_RETRY 150), which zd98's kingsFormed will show for z0h.
- 22:47: **Our cat traps clog the engine's 10-trap cap.** Over the local u9p vs u6h rift game's actions (scratch trapcap.py): one baby laid 10 CAT_LAY lane traps behind a cat at r229-269 along x=19; the cat never crossed them again and we held the cap for 1,774 of 2,000 rounds (u6h: 6), so after r270 almost no cat trap of ours could go down: placed 39 and fired 29 to u6h's 65 and 60. removeCatTrap exists (rats within dist2 2, no cooldown, no refund; engine RobotControllerImpl).
- 22:47: **z0i = u9p + RECYCLE_AGE 300** (new switch; Baby.recycle()): at the cap during cooperation a baby lifts one of our cat traps within reach whose tile it has not seen a cat on for 300 rounds (or never). Falsifiers in bots/z0i/SNAPSHOT before any game: s6 screen zd99 (D 0 does not reject); mechanism run zd100 (vs u6h/s7d on zd96's cells, control u9p's zd96 games): trap hits and cat damage not above u9p's, or wins below u9p's - 2, reject; replica: block rules, transfer floor, block catDmgTrap above the twin's. A single z0i vs u6h rift game runs beside the queue as a quick mechanism look (no decision rests on it). Queue: zd95 (running), zd96, zd100, zd99, zd98, zd97.
- 22:49: **The cap clog is common on the replica** (scratch capscan.py over u9p's set-3 dumps, 16422-16473): we held 10 live cat traps in 21.5% of the rounds we could still place them (them 4.1%); fired 682 of 795 placed (them 424 of 448). The worst cells are the long cooperation games: spsquared rift (lost on points) at the cap 368 of 593 placeable rounds, traps fired 29 to their 54; TheHeist 496 of 602 and 178 of 276; AdamTan12_Finals where_are_you 752 of 900. This is z0i's target. Correction: replica dumps number their games g0-g2, and the trapsplit tallies above (22:34, 22:52) read only g1-g2; the full rerun (g0 included) is going to ~/diag26b/trapsplit_all2.txt.
- 22:55: **z0i DROPPED before screening** (diagnostic game, no decision game). z0i vs u6h on rift (~/diag26b/kf; it won, cat damage 5,999 to 5,003, but opened the war at r529): its baby-side lift removed 19 of our cat traps, most of them 1-74 rounds old on the live x=19-20 lane (a bystander baby that never saw a cat on a tile counted it as stale). The mirror game (u6h vs u9p, u9p as B) held the cap 1,617 rounds: the clog is not side-specific.
- 22:55: **z0j = u9p + LAY_MAX 6 + RECYCLE_AGE 300 (strict)**, one mechanism, cat-trap slots: lane traps only while fewer than 6 of ours are live (4 slots kept for traps in front of or beside a cat), and the lift only on tiles where that baby saw a cat 300+ rounds ago. Falsifiers in bots/z0j/SNAPSHOT before any game: s6 screen zd101; mechanism run zd102 (vs u6h/s7d on zd96's cells): trap hits and cat damage above u9p's, wins not below u9p's - 2; replica: block rules, transfer floor, block catDmgTrap above the twin's. Queue: zd95 (running), zd96, zd102, zd101, zd98, zd97.
- 22:56: **z0j's diagnostic rift game** (z0j vs u6h, ~/diag26b/kf; no decision rests on it): at the cap 333 rounds (u9p 1,774), cat traps fired 40 (u9p 29), cat damage 7,820 to u6h's 6,390 (u9p's game: 4,880 to 8,206); lost on points with kings 2 to 3 (its third king never formed after it opened the war at r594) and transfer 22,199 to 23,305. Three lifts, two of them 1-2 rounds after another baby re-laid the same stale tile (19,20): CAT_LAY has no upper bound on how long ago the cat left a lane tile, so dead lanes keep drawing traps (a possible LAY_STALE follow-up).
- 23:08: **z0g passes screen zd95** (s6, against u9p's zd61): n 44, D 1 (−1: abrk arrows A, a r1555 king loss), net −1, t −1.00 (gate −1.5); picks/100 34.13 vs 34.30 (per-cell z +0.45); 0 overruns, 0 exceptions; catDmgTrap 39,460 vs 38,260, kingsFormed 30 vs 31. As registered, the switch barely fires on these cells; mechanism run zd96 is next in the queue.
- 23:08: **z0c continues after replica block 1** (#234; set 3, 17224-17227, 17241-17244, 17251-17254, 17259-17262; `paired 13248-13291 IDS --crown 12130-12163`): 48 cells, 37 alike, D 11 (+6 −5), net +1, t +0.30 (block 1 rejects at t ≤ 0). Gained: spsquared TheHeist A and B, Hugo Xzibit uneruesansfin A, Hugo YokoOno 5t4rv4t10n B and externalsustenance B, waxmango FreeTheCheese B; lost: spsquared rift A and rose A, Hugo Xzibit minimaze B, Hugo YokoOno 5t4rv4t10n A and Stash A. Falsifiers (field_report on the VM): transfer 4,303 a game (set-3 floor 3,968), pass; cat-damage share in the block's 15 r2000 points games 0.511 against the twin's 0.444 (its 15), pass. Set-1 block 2 queued (4 sent, 12 in the drain queue; waiter build/wait_z0cb2.sh, after 17262). VM disk 2.5 GB free.
- 23:09: **v0g (u9p + GRAB_MICRO) REJECTED at screen zd72** (s6 overrun gate; scored late: it finished before zd95): 545 overruns (u9p 0), though n 44, D 10 (+6 −4), net +2, t +0.63, picks z +0.39; transfer 286,528 vs 321,602 (−11%), cat-trap damage 28,417 vs 38,260. GrabMicro's per-turn scoring still overruns on u9p's heavier turn (u1g/u1j's rewrite notwithstanding).
- 23:10: **Why the rift lane traps went dead** (u9p vs u6h dump, per-cat tallies): a cat has 4,000 HP and a new one spawns after one dies. Cat 7 (our side) took 2,900 trap and 1,002 bite damage from us and was gone by ~r260-270; the 10 lane traps were laid along its route at r229-269, and their layer (baby 12407) was killed by it at r281. Its successor, cat 9, lived r309-1927 and took 430 bite damage from us and no trap damage (our cap was full) and none from u6h, whose cats 8 and 10 took 3,011 + 2,000 of its trap damage. So the cat-damage pool is not fixed, and slots matter most after a kill: z0j's LAY_MAX keeps 4 slots whatever the lifter knows.
- 23:12: **The late-break scan with g0 included** (trapsplit over all 954 replica games of ~/diag26b/rp; the 22:45 tally read 636, missing g0): we opened the war at r100+ in 60 games and won 24 (40%); they opened at r100+ in 179 and we won 116 (65%); no war in 92, won 72 (78%); early openings (<r100) ours 61 of 170 (36%), theirs 146 of 453 (32%). Cat-trap damage a game after their opening 956-1,483 ours to 401-433 theirs; after ours 344-384 to 837-1,165. The 22:45 reading stands: z0g's target (our r100+ openings) is 6% of games at a 40% win rate, where no war wins 78%.
- 23:30: **Early-war formations lose** (scratch formscan.py over the 954 replica games of ~/diag26b/rp). After the opponent opened the war before r100 (453 games), the games where we formed a second king after the opening and before r300 won 6 of 55 (11%), those with no formation 135 of 452 (30%), those that formed at r300+ 15 of 35 (43%, which survival to r300 inflates). The 49 losses repeat across builds on a few cells: Varun DefaultMedium (formed r109-134), waxmango DefaultMedium (r120-138), Icarus uneasy_alliance (r82-124), Finals keepout (r115-207), AlexT101 safelycontained, Xzibit starvation, YokoOno Stash. Varun DefaultMedium (17164 g0, their rat trap broke the truce at r25): the merge at r109 took our babies from 20 to 13, both kings then spawned at the rising cost and the bank fell from 709 to 301 by r115; by r175 we had 9 babies to their 20, and 99 king bites killed the first king at r225. Starvation, the other face of formation, was worked through s3w-u0t (FORM_NET100 -100 is u9p's gate); this one is about the 7 babies a merge takes in a war just begun.
- 23:30: **z0k = u9p + FORM_WAR_FROM 300** (new switch, King.formation): no formation site at war before r300, and a site open when the war starts is withdrawn. Falsifiers in bots/z0k/SNAPSHOT before any game: s6 screen zd103 vs zd61 (D 0 does not reject); z0k forming at war before r300 in any screen or replica game rejects; replica block rules, transfer floor, and z0k's wins above the twin's in the block games where the twin formed at war before r300. Queue: zd96 (running), zd102, zd103, zd101, zd98, zd97, then the old sweep. A note on waiting: `pgrep -f diag26b/formscan.py` inside gssh matched its own ssh shell, so a waiter on it never ended (the HANDOFF warning, again); wait on an output marker instead.
- 23:31: **Starvation in king-kill losses is the end of an attrition loss** (scratch starvescan.py, the same 954 games): 567 of our losses ended in a king kill; 311 had starve damage on our kings (165 before r600, 101 in r600-1200, 45 later). A hundred rounds before the end our bank averaged 65-95 (under 100 in 238 of 311) with 5-9 babies, against their 16-38 at the end: the army had already lost, so no spending or reserve rule would have saved those kings (as u6e/u9i's RESERVE_ROUNDS found). Our early openings by opponent (trapsplit_all2): Finals 12 of 16 won, waxmango 0 of 38, Icarus 1 of 11, spsquared 10 of 21, Varun 16 of 33; but waxmango's are mutual-contact maps (DefaultMedium B r26 by us where A has them at r24; tiny r11-12; dirtpassageway r25-27 both sides), where they would open too, so no break-timing switch follows from them.
- 23:34: **Cells that flip between builds** (trapsplit_all2, cells with 4+ games and both outcomes): thunderdome A vs Varun 4 of 10, Stash A vs YokoOno 7 of 16, clouds B vs Xzibit 9 of 16, FreeTheCheese B vs waxmango 5 of 9, corridorofdoom B vs Varun 4 of 9, rift A vs spsquared 3 of 9. thunderdome A's wins are early king kills (u8f r40, u8w r115, u9q and z0a r77); u9p and w0c lose it at r784. Our babies' turns within dist2 20 of Varun's king (scratch nearking.py): z0a/u9q 174 in r25-49 (93 at health <= 40) and 123 in r50-74 (86); u9p 68 in r25-49 (3) and none after r50: u9p's retreat (health <= 40 with an enemy within dist2 20: go home) lifts the siege.
- 23:34: **z0l = u9p + SIEGE_STAY 20** (new switch, Baby.retreat): no retreat while the enemy king is in view within dist2 20. u9q and z0a removed retreat everywhere and lost delivery; this keeps it everywhere but at their king. Falsifiers in bots/z0l/SNAPSHOT before any game: s6 screen zd104 with z0a's transfer gate (shared-cell transferAmt >= 0.97x u9p's); replica block rules, transfer floor, our king-kill wins above the twin's on the block's cells. Queue: zd96 (running), zd102, zd103, zd104, zd101, zd98, zd97. VM dump cache trimmed 3.4 to 1.5 GB (game_report._trim, LRU; our regenerable dumps): root 3.8 GB free.
- 23:41: **z0g passes mechanism run zd96** (z0g and u9p each vs u6h and s7d, which never break, on 8 maps x 2 sides; research/audits/truce_run.py and mech_cmp.py). z0g opened the war at r100+ in 0 of 32 games (u9p in 21; the falsifier needed 0); wins 21 to u9p's 18 (+6: s7d FreeTheCheese A, where_are_you A; u6h FreeTheCheese B, averystrangespace A, uneruesansfin B; -3: s7d Stash B, uneruesansfin B, u6h cheesebottles B), so not below u9p - 2. Our cat damage 233k to 168k (+39%), cat traps placed 1,639 to 769 and fired 1,449 to 674; transfer 253k to 299k on 40.8k rounds to 49.1k (6.21 a round to 6.09: games end sooner, several z0g wins by the cat killing their king in cooperation); king deaths 5 to 10, kingsFormed 21 to 27; 0 overruns, 0 exceptions. z0g joins the replica queue (after z0c), with its registered replica falsifiers.
- 23:43: **Retreat decides several flippable cells.** By build (trapsplit_all2 with packages from the dumps): u9q (RETREAT_HP 0) won thunderdome A vs Varun (r77), clouds B vs Xzibit (points) and corridorofdoom B vs Varun (points, cat 1,722 to 1,270) where u9p lost all three; z0a (retreat only when carrying) won thunderdome A, clouds B and Stash A vs YokoOno (r1189 king kill). Both were rejected on delivery. rift A vs spsquared (3 of 9) is won only when no one breaks (u7p, u8f, u8w); since u8x spsquared breaks at r454-494 and wins every component. **z0m = u9p + RETREAT_HP 20** (dose arm; z0l is the narrowing at the enemy king): falsifiers in bots/z0m/SNAPSHOT before any game (s6 screen zd105 with the 0.97 transfer gate; replica block rules and transfer floor). Queue: zd102 (running), zd103, zd104, zd105, zd101, zd98, zd97.
- 23:55: **z0l's reach is wide** (scratch siegescan.py over u9p's 96 replica games, 16422-16537): our rat-turns at health <= 40 within dist2 20 of an enemy king reached 10+ in 39 games (won 13) and stayed under 10 in 57 (won 32). The big counts are small maps where the kings sit close (tiny vs waxmango 150-167, knifefight vs Varun 94-122, dirtpassageway 44-85, externalsustenance and 5t4rv4t10n vs YokoOno 53-78), so SIEGE_STAY will change many early fights, not only sieges; zd104 decides whether that helps.
- 00:04: **z0c REJECTED after replica block 2** (its registered transfer floor). Block 2 (set 1, 17272-17275, 17295-17298, 17314-17321) vs `paired 12771-12813 IDS --crown 11549-11677`: 48 cells, 40 alike, D 8 (+6: Finals DefaultSmall B, Icarus rose B, YokoOno clouds B, Varun knifefight B, spsquared bed A, fullofsecrets B; -2: YokoOno Stash A, spsquared bed B), net +4, t 1.41; pooled over both blocks D 19, net +5, t 1.15 (block 2 would continue). But transfer 3,988 a game against set 1's floor of 4,133 (twin 4,592; research/audits/blockcheck.py on field_report rows); per round too, 4.63 to 5.37 (0.86), and in the 9 r2000 games 14,778 to 17,279 (0.86), on equal rounds (41.3k to 41.0k). The cat falsifier held (share in the 9 points games 0.658 to 0.584). So END_CAT's late cat chase costs delivery on set 1 (block 1 read 0.976 of the twin on set 3); a cat errand that spares carriers, or starts later, would be the follow-up.
- 00:04: **z0g submitted** (#235, u9p + PEACE_FROM 100; passed screen zd95 and mechanism run zd96). Set-3 block 1 after 17415: 4 sent, 12 in the drain queue; waiter build/wait_z0gb1.sh. Falsifiers as registered in bots/z0g/SNAPSHOT: block rules, transfer floor per set, no z0g opening at r100+ in any game, and z0g's wins above the twin's in the games where the twin opened at r100+.
- 00:05: **z0j REJECTED at mechanism run zd102** (its registered falsifiers: our cat traps fired and our cat damage must exceed u9p's on zd96's 32 cells). z0j vs u9p (research/audits/mech_cmp.py): catTrapHits 636 to 674, cat damage 165.9k to 167.6k (trap 62.5k to 66.8k, bites 103.3k to 100.8k), catTraps placed 724 to 769; wins 21 to 18, king deaths 6 to 10, transfer 308.7k to 299.1k on 52.3k rounds to 49.1k; 0 overruns, 0 exceptions. LAY_MAX 6 laid fewer lane traps and the strict lift freed too few slots to make up for it: the cap clog seen on rift does not turn into hits on these cells. Its s6 screen zd101 is removed from the queue. **z0n = u9p + END_CAT 1650 with END_CAT_GATHER false** (z0c's cat errand without the gatherers, after z0c's set-1 delivery loss) is registered in bots/z0n/SNAPSHOT before any game: s6 screen zd106 with the 0.97 transfer gate and z0c's mechanism (late dmgCat above u9p's); replica block rules, transfer floor, cat share. Queue: zd103 (running), zd104, zd105, zd106, zd98, zd97, then the old sweep.
- 00:28: **z0k REJECTED at screen zd103** (s6 picks gate). z0k (u9p + FORM_WAR_FROM 300) vs u9p's zd61: n 44, D 2, net 0, t 0.00 (38 wins each); picks/100 33.14 vs 34.30, per-cell z −3.26 (gate −2); transferAmt 334,820 vs 321,602; kingsFormed 22 vs 31; 0 overruns. Holding formations back to r300 kept the wins level and gave up picks.
- 00:31: **Which field kings stand still** (scratch kingmove.py over the 546 cached replica dumps, king samples every 10 rounds to r600, share on the same tile as 30 rounds before): VarunVejalla 0.57, Icarus 0.56, nzjt 0.55, spsquared 0.53; Xzibit 0.08, YokoOno 0.05, waxmango 0.02, AdamTan12_Finals 0.01 (ours 0.36). A buried spawn ring (z0o) can only hold against the first four; our three worst in-band opponents by win rate are split (nzjt 0.23 still; Finals 0.35 and waxmango 0.32 always moving).
- 00:33: **z0o's diagnostic games vs araid** (~/diag26b/bury, indicators on): DefaultSmall 2 ring tiles placed (lost r392, as u9p), tiny 17 (won r112), Stash 0 (won r858). The registered inert falsifier (no ring dirt in any game) does not fire. Stash's 0 is the map: araid's king sat at (10,5) behind the y=7 wall row, every ring tile out of reach, and two of our attackers stood at (9,8)/(10,8) from r590 to r800 on 'f' turns facing it through the wall. stockDirt never fired (bu=n:0 on every rat): the dirt came from u9p's own digs (dg).
- 00:35: **Fighters parked through walls on pipes** (scratch parked.py: our baby samples on one tile for 50+ rounds, away from our kings, over the 546 cached replica dumps): 1.8% overall, but 23% in pipes vs VarunVejalla (14254 g1: transfer 3,793 to 20,240, a r1880 king kill), 15% corridorofdoom vs Varun, 14% attack_on_cat vs Xzibit. Snapshots of 14254 g1 (r150-500): every baby of ours inside the king's corridor (x 33-38) while Varun's carriers worked our mines (29,15) and (47,30). Local u9p vs u6h on pipes (~/diag26b/park/u6h, indicators): 323 of the parked samples are fighters on 'f' (faceEnemy) turns, all at x=27 y=9-19, facing enemies in the centre column x=24-25 through the x=26 wall. FACE_REACH (u4i/u4n) was built for this and rejected once on u4e (#158, block 1 D 3, t -0.58).
- 00:36: **Keepout vs Finals is coverage, which stays closed** (econ_report 16424 g0): Finals picked 42.5k to our 9.6k, all of (28,47), (46,42), (47,30) on our half (we never worked the first two) and 1.4-1.6k from our pocket mines (37,3), (46,13). Exploration and coverage switches are closed ground (SCOUT_FRONTIER, SCOUT_MINES, MINE_EXPLORE, SCOUT_AREA, SEEK_IDLE + IDLE_SHIFT all rejected), so no candidate from this.
- 00:37: **z0p = u9p + FACE_REACH** (the existing switch, retested on the current base with the wall-stare evidence above). Falsifiers in bots/z0p/SNAPSHOT before any z0p game: s6 screen vs zd61 (t <= -1.5, picks z <= -2, overruns/exceptions, transfer < 0.97x); mechanism: z0p vs u6h on pipes with fighter samples parked 50+ rounds on 'f' turns not below half of u9p's 323 drops it as inert; replica block rules and transfer floors. Screen zd107 queued after zd106; mechanism game in ~/diag26b/park2.
- 00:43: **z0p's mechanism holds** (z0p vs u6h on pipes, ~/diag26b/park2, against u9p's ~/diag26b/park game): fighter samples parked 50+ rounds on 'f' turns 0 (u9p 323); the parked samples left are gatherers waiting at or walking to mines (G W 137, G M 107, F M 84, F W 77). The one game: z0p lost 49-50 on points (kings 3 to u6h's 3, transfer 21,075 to 21,950), u9p won 54-45 (kings 3 to 2, transfer 20,467 to 21,858); one game a side decides nothing. Screen zd107 stays queued.
- 00:43: **Cheese-boosted cat bites are worth little on the replica's points games** (rp_games.jsonl, 59 MORE_POINTS games): all but the TheHeist cat-death endings were decided after a backstab (war weights: cat 0.3), and our end banks were mostly 60-1,000; only rift banked 9,144 (lost 40-59 to spsquared): spent at 100 a bite (+10 damage) that is about +900 cat damage, +1.2 points. The closest loss, averystrangespace vs Icarus 49-50 (13288 g1), turned on 110 cat damage to 386 after a r1511 backstab. No candidate.
- 00:43: **z0o amended before any decision game** (bots/z0o/SNAPSHOT): stockDirt now runs for attackers and idle babies (no pile in view, nothing carried), with no enemy within dist2 20, on any dirt beside them not within Chebyshev 4 of one of our kings; no rat stocked anything in the first games. Mirror check of those first games (u9p vs u9p, ~/diag26b/bury3): z0o as A flipped dirtpassageway (won on points, transfer 14,498 to 7,607; the mirror lost it) and lost DefaultSmall (r732; the mirror won r413); tiny and knifefight went to A either way. Enemy spawns in the 100 rounds after the first ring placement: 13 vs the mirror's 15 (DefaultSmall), 2 vs 3 (dirtpassageway). Re-run in ~/diag26b/bury4 (new falsifier: the stash must reach 8+ dirt).
- 00:48: **z0l REJECTED at screen zd104** (its registered shared-cell transfer gate, 0.97x). z0l (u9p + SIEGE_STAY 20) vs u9p's zd61: n 44, D 5 (+2 −3), net −1, t −0.44; picks/100 34.53 vs 34.30 (z +0.27); 0 overruns; transferAmt 306,251 vs 321,602 (0.952), kingsFormed 29 vs 31. Holding wounded rats in the siege kept the wins about level and cost delivery, as RETREAT_HP 0 (u9q) and z0a did.
- 00:50: **The shared-cell transfer gate punishes faster wins** (z0l's zd104 against u9p's zd61, per cell). On the 30 cells where the two games ended within 20 rounds of each other z0l delivered 232,981 to 233,708 (0.997); the 14 cells of changed length gave 73,270 to 87,894 (0.834), led by cheesefarm B (z0l won by king kill at r896, u9p won at r2000: 9,944 to 21,882) and arrows A (z0l lost at r1156, u9p won at r2000). Transfer per round over all shared cells: 6.62 to 6.77 (0.977). z0l stays rejected under its registered gate. From here on a candidate's screen transfer gate is registered as transfer per round on the shared cells (and on the cells whose lengths agree within 20 rounds), so an earlier win is not scored as lost delivery. Thunderdome A vs nzjt (nzjt backstabs at r3) is one cell that turns on retreat: u9q (RETREAT_HP 0) killed nzjt's king at r45 in 17017 (600 damage); w0c and z0c (u9p's line) lost it at r182 in 17175/17320.
- 00:52: **z0o's amended stocking fills the stash** (z0o vs u9p, ~/diag26b/bury4; scratch stash.py and burymech.py): stash max 26 on DefaultSmall, 128 on dirtpassageway and 14 on tiny, so the new falsifier (8+) passes and screen zd108 stays queued. Ring tiles placed 13, 10 and 1. DefaultSmall lost at r1112 (the u9p mirror won it at r413), dirtpassageway won on points (transfer 12,992 to 8,565; the mirror lost it 9,704 to 12,462), tiny won at r61 (as the mirror's A). In the 100 rounds after the first placement the enemy spawned 2 times on dirtpassageway (mirror 7) and dug 4 ring tiles (mirror 7); on DefaultSmall 7 spawns to the mirror's 5. Three games decide nothing; zd108 does.
- 01:01: **z0g continues after replica block 1** (#235; set 3, 17426-17441; `paired 13248-13291 IDS --crown 12130-12163`): 48 cells, 38 alike, D 10 (+7 −3), net +4, t 1.26 (block 1 rejects at t ≤ 0). Gained: Xzibit uneruesansfin A, YokoOno 5t4rv4t10n B and externalsustenance B, Varun corridorofdoom B, spsquared TheHeist A and B, waxmango FreeTheCheese B; lost: YokoOno 5t4rv4t10n A and Stash A, spsquared rift A. Falsifiers (field_report rows, research/audits/blockcheck.py): transfer 4,359 a game (set-3 floor 3,968; twin 4,409), pass; z0g opened the war at r100+ in 0 of 48 games (7 openings before r100, 39 by them), pass; on the 4 cells where the twin opened at r100+, z0g won 2 to the twin's 1, pass. Cat share in the 15 points games 0.55 to 0.444. Set-1 block 2 queued after 17441 (4 sent, 12 in the drain queue); waiter build/wait_z0gb2.sh.
- 01:08: **Where picked cheese goes: attacker hoards** (scratch deathcheese.py, bigdrop.py, hoard.py and rawreach.py over the cached replica dumps). Our babies died holding 1,690 raw cheese a game (40 games; theirs 1,789), a third of our 4,948 transfer. Over 120 games, 141,696 of 216,465 dropped came from 222 deaths holding 241+; over all 573 dumps, 749 such deaths, all while one of our kings lived, 548 of them in games we won, 530 at Chebyshev 20+ from our nearest king (TheHeist vs Icarus 3,875 at most; minimaze vs Xzibit, averystrangespace vs Icarus and rose vs Icarus/spsquared/waxmango 63-104 each). The hoarders are attackers (move code 'a', the rushers of spawn index 4-15, which keep the role for life and never deliver; RUSH_LIFE and RUSH_BURN are 0): on TheHeist (17318 g0, 12810 g0) three of them walked the cat's lane at y=18 from r850 on and each picked up 2,000-3,400, crawling at 1% slower per unit carried. Capping or delivering the hoard has been tried five times (ATK_CARRY t0l/u2n/u5z, ATK_DELIVER u3g/u4q/u6j, HUNGRY u4c/u5a), all rejected or neutral; spending it has not. At war, our attackers spent 164 turns a lost game (mean raw 116) and 378 a won one (mean 838) holding 20+ raw with an enemy baby within dist2 13 (150 dumps). Rat traps by team over the 573 dumps: we lay 5.9 a game (97% fire), the field 29.3 (92%; Finals 62.4, Varun 48.3).
- 01:08: **z0q = u9p + RAW_TRAPS** (new switch, Baby.rawTrap; RAW_TRAP_CAP 20): at war an attacker with 20+ raw lays a rat trap on the TRAP_ADJ tile beside the nearest enemy baby within dist2 13, paid from its raw alone (the engine spends raw first), so the bank pays nothing, unlike u8f, u9j and y0a's bank-paid traps. Falsifiers in bots/z0q/SNAPSHOT before any decision game: s6 screen zd109 (t <= -1.5, picks z <= -2, overruns, transfer per round on shared cells < 0.97x); inert if under 44 raw traps over the screen; hit rate under 0.7 drops it; replica block rules and floors. Diagnostic z0q vs araid (~/diag26b/raw): DefaultSmall 3 traps laid, 3 fired, lost r373 (u9p r378), transfer 1,363 to 1,270. Queue: zd105 (running), zd106, zd107, zd108, zd109, zd98, zd97, then the sweep; zd75 (v0j = u9p + FACE_REACH, the same build as z0p) dropped.
- 01:08: **z0m REJECTED at screen zd105** (s6 wins gate; abort_watch stopped it at 42 of 48 games once the gate was out of reach). z0m (u9p + RETREAT_HP 20) vs u9p's zd61: n 38, D 9 (+2 −7), net −5, t −1.71; picks/100 31.23 vs 33.44 (z −1.91); transferAmt 218,838 vs 242,182 on the shared cells; 0 overruns. Lost: abrk arrows A, canyoudig B, evileye A; araid DefaultMedium B, bed A, cheesefarm A, tiny B; gained araid arrows B, starvation A. Fighting on down to 20 health loses the rats retreat would have saved, as RETREAT_HP 0 (u9q) did; with z0a and z0l rejected too, the retreat dose is closed on this base.
- 01:16: **What kills our babies in lost games** (scratch killcause.py, die rows of the 621 cached replica dumps). Per lost game (353): enemy rat traps 10.7 of ours to our 2.5 of theirs; throw landings 17.8 to 19.8, throw hits 5.2 to 6.1, bites 5.8 to 4.2; the cats (pounce, crush, scratch) 16.4 of ours to 32.2 of theirs. Traps are the one weapon we lose by much, which z0q answers without the bank cost that sank y0a (y0a at zd87: kills 820 to 748 and deaths 1,245 to 1,410, but spawned 1,798 to 2,021 and 4 starved kings). z0q's laid traps vs araid all came at r30-32 right after the r29 war start (DefaultSmall, ~/diag26b/raw/ds2), 3 a game in all three games: vs the local archetypes the dose is small, the field's hoards are larger. Screen zd109 moved to the head of the queue.
- 01:16: **Our squeaks near cats** (scratch catsq.py over 150 cached replica dumps): cats in attack mode turn toward the first squeak they hear each turn. In lost games a cat heard our squeaks 157 times a game (theirs 347) and heard us first on 73 rounds (them 200); in the 5 rounds after a cat heard us first it killed 24.9 of ours and 6.0 of theirs, after hearing them first 11.2 of ours and 58.0 of theirs (proximity drives part of it: rats near a cat both squeak and die). Our drops in lost games are 1,091 cheese a game, of which they pick up about 488 (scratch droppick.py): a small lever.
- 01:16: **z0r = u9p + CAT_HUSH** (new switch, Baby.report): no baby report squeak while the nearest cat in view can hear it. Falsifiers in bots/z0r/SNAPSHOT before any decision game: s6 screen (t <= -1.5, picks z <= -2, overruns, transfer per round < 0.97x); mechanism pair z0r/u9p vs araid on arrows and evileye (~/diag26b/hush): first-heard rounds not 80%+ below u9p's drops it; screen deathsByCat above 1.1x u9p's rejects; replica block rules and floors.
- 01:23: **z0r's mechanism holds after the amendment** (z0r vs araid, arrows, ~/diag26b/hush2, against u9p's ~/diag26b/hush/u9p-arrows): rounds on which a cat heard us first 0 (u9p 20; the first z0r build 30, its squeakers not seeing the cat), our squeaks 123 (306). The game: won at r2000 as u9p; cats killed 47 of ours (u9p's game 53) and 91 of araid's (54); transfer 7,818 to 9,871, which the per-round transfer gate will weigh. Screen zd110 queued after zd109.
- 01:23: **Mine yield per rat-round** (econ_report --team vibe26b over z0g's 48 block games): our side's mines spawned 10,070 cheese a game; we picked 4,849, the enemy 1,800, and 3,420 lay unpicked; their side spawned 10,065, they picked 6,515 and we 589. Our rat-rounds within Chebyshev 4 of our mines 11,759 to theirs 7,503 at theirs: 0.41 cheese a rat-round to their 0.87. Per-mine rows (17432 g1, sunandmoon) show both teams crowding one home mine (1,198 and 918 rat-rounds for 300 cheese) and leaving the far ones unworked. Coverage switches are closed (SCOUT_*, MINE_EXPLORE, SEEK_*); MINE_ANY (v0l) waits in the sweep.
- 01:27: **z0n REJECTED at screen zd106** (its registered shared-cell transfer gate, 0.97x). z0n (u9p + END_CAT 1650, END_CAT_GATHER false) vs u9p's zd61: n 44, D 1 (−1: abrk arrows A, lost r1555 where u9p won at r2000), net −1, t −1.00; picks/100 33.84 vs 34.30 (per-cell z +0.01); 0 overruns, 0 exceptions; transferAmt 304,366 vs 321,602 (0.946; per round 6.63 to 6.77, 0.979, which would pass the per-round gate registered for later candidates). Cat damage 163.6k to 161.2k (+1.5%, bites 124.8k to 122.9k): sparing the gatherers kept most of the delivery but also took away most of z0c's late cat damage, so the errand has no dose left worth a replica slot.
- 01:27: **RAW_BITE would be a small dose** (scratch rawbite.py over 200 cached replica dumps, bites by our role and raw carried): in lost games our attackers bit the cat 18.5 times a game (3.7 holding 20+ raw, 0.7 holding 100+) and rats 14.8 (2.1, 0.4); in won games the cat 25.3 (8.3, 3.5). A raw-paid bite boost would touch about 6 bites a lost game, +5 damage each. No candidate. **A raw-trap camp at their king is no larger** (scratch campreach.py, the same dumps): our attackers stood within Chebyshev 4 of an enemy king holding 20+ raw 9.0 turns a lost game (mean raw 104) and 11.1 a won one, and only 0.4 and 0.6 of those turns had no enemy baby within dist2 13, so RAW_TRAPS already covers them.
- 01:32: **Attackers' raw at enemy-half mines** (scratch raidreach.py over 250 cached replica dumps, mines from the dump meta, enemy half by distance to the two starting kings): at war our attackers stood within Chebyshev 4 of an enemy-half mine holding 20+ raw 76.6 turns a lost game (mean raw 316, 3.7 rat-mine pairs), 36.7 of them with no enemy baby within dist2 13; in won games 391.5 turns (mean 1,087, 6.0 pairs). Enemy rats spent 5,893 rat-rounds a lost game inside those mines' 9x9 squares at war. **z0s = u9p + RAW_RAID 8** (new switch, Baby.rawRaid; cap 20 live): such an attacker, with nothing to bite beside it, lays a rat trap on an adjacent trap site of the mine (MINE_SITES, the 3x3 blocks tiling the 9x9), paid from raw alone; the bank-paid RAID_TRAPS (s6i, s8i) was rejected. Falsifiers in bots/z0s/SNAPSHOT before any z0s game: s6 screen zd111 (t <= -1.5, picks z <= -2, overruns, transfer per round on shared cells < 0.97x); inert under 44 traps over the screen; hits under 0.5x laid drops it; replica block rules and floors. Diagnostic games vs araid in ~/diag26b/rraid. Queue: zd109 (running), zd110, zd111, zd107, zd108, zd98, zd97, then the sweep.
- 01:37: **z0s's diagnostic games vs araid** (z0s as A, ~/diag26b/rraid; no decision rests on them): DefaultSmall 5 raw raid traps, 4 fired, won at r345 (u9p as A lost at r378 in zd61); arrows 2 and 2, lost at r644 (u9p won on points); evileye 1 and 1, won on points (as u9p). 8 laid in 3 games, above the inert bar's one a cell; the dose against the field's longer wars should be larger. Screen zd111 stays queued.
- 01:37: **How our kings die, and the mid-game collapse** (scratch kingdmg.py and collapse3.py over the cached replica dumps). Damage to our kings in lost king-kill games: ending before r300 (82 games) bites 232 a game, throw hits 170, starvation 175, cats 43; ending r300-999 (173) bites 216, throw hits 95, starvation 346, cats 69; r1000+ (52) bites 228, throw hits 169, starvation 472. Their kings in our king-kill wins at r300-999 (52) took starvation 488 and cats 87, our bites 9. In all 173 mid-game losses our babies fell to half of theirs or less (median r351, 135 rounds before the end): 15.4 to 18.0 a hundred rounds before, 10.0 to 18.3 at the collapse. In those 100 rounds 12.9 of ours died (throw landings 3.7, enemy traps 2.2, cat pounces 1.8) to 10.1 of theirs (traps 0.2), 5.1 of ours within Chebyshev 6 of our king. In the 200 rounds before the collapse they picked 1,901 (1,299 on their half, 542 on ours) and we 959 (807 on ours, 120 on theirs), delivering 1,293 to our 551; spawns 23 to 19. Mid-game losses are an income loss: they work their half unopposed and take 40% of the picks on ours.
- 01:46: **z0g continues after replica block 2** (#235; set 1, 17442-17457; `paired 12771-12813 IDS --crown 11549-11677`): 48 cells, 37 alike, D 11 (+6: Finals DefaultSmall B, YokoOno clouds B, Varun knifefight B, nzjt peaceinourtime B, spsquared bed A and fullofsecrets B; −5: YokoOno Stash A, nzjt peaceinourtime A, spsquared bed B, waxmango clockwisdom A and rose B), net +1, t 0.30; pooled over both blocks D 21, net +5, t 1.09 (block 2 rejects at t ≤ 1). Falsifiers (research/audits/blockcheck.py on field_report rows ~/diag26b/z0gb2.jsonl against ~/diag26b/twin_s1.jsonl): transfer 4,269 a game (set-1 floor 4,133; twin 4,592), pass; z0g opened the war in 11 games, all before r100 (latest r96, superioristhmus A), none at r100+ (twin 2 at r100+), pass; on the twin's 2 late-opening cells z0g won 1 to the twin's 0, pass. Cat share in the 9 points games 0.634 to 0.584; kings at the end of points games 2.11 to 2.22; king-kill wins 13 each. Set-2 block 3 queued (16 in the drain queue: the hourly limit sent none yet); waiter build/wait_z0gb3.sh (after 17457); set-2 twin rows built in ~/diag26b/twin_s2.jsonl (12838-12899, 48 games).
- 01:49: **Where their traps catch us, and what each mine class is worth at war** (scratch trapwhere.py over 300 cached replica dumps; mineval.py over 300 war games, r60-1000, within Chebyshev 4 of a mine classed by its distance from its half's start over the start-start distance: near < 0.25, mid < 0.4, far). Enemy rat traps fired on our rats 40.0 times a lost game (16.8 a won one); 30.3 of the 40 on our own half: 13.4 at our far mines, 8.3 at our mid mines, 2.2 at our near mines, 6.4 away from mines; ours fired on them 6.7 times. Our piles on our half (pilerace2/3.py, r60-600): in lost games we took 90.4 a game and they 43.8, 33.2 of those at war at our far mines, and their rat was nearer the pile at its spawn in 31.6 of the 43.8 (ours within Chebyshev 4 in 12.1): they hold our frontier mines, they do not outrun us. In won games 42.0 far-mine piles a game lay unpicked. Per mine class at war in lost games, our rat-rounds / cheese per rat-round / deaths per 1,000 rat-rounds / trap fires on us: our near 1,578 / 0.36 / 0.9 / 0.6; our mid 1,875 / 0.46 / 4.1 / 5.0; our far 1,432 / 0.76 / 8.7 / 7.2; their far 243 / 1.30 / 24.1 / 3.3 (their rat-rounds at our far mines 781 a game, ours at theirs 243). In won games our far mines gave 1.21 a rat-round at 6.3 deaths per 1,000 and their rats spent 139 rat-rounds there. The near mines are crowded (0.23-0.36 a rat-round) and safe; the frontier is where both the cheese and the traps are.
- 01:51: **z0q passes screen zd109** (s6 vs u9p's zd61, all registered gates). z0q (u9p + RAW_TRAPS): n 44, D 8 (+3: araid arrows B, evileye B, starvation A; −5: abrk bed A and cheesefarm B, araid DefaultMedium B, DefaultSmall B, evileye A), net −2, t −0.70 (gate −1.5); picks/100 33.68 vs 34.30 (per-cell z −1.58, gate −2); 0 overruns, 0 exceptions; transfer per round on the 44 shared cells 6.583 to 6.769 (0.9725, gate 0.97), on the 26 length-matched cells 258,026 to 259,014 (0.996). Mechanism: 141 raw traps laid over the screen (bar 44), 137 fired (0.97; bar 0.7); our kills 2,122 to 1,956 on the shared cells (+8.5%), deaths 2,810 to 2,862, spawned 3,930 to 4,031, kingsFormed 33 to 31. z0q joins the replica queue after z0g, with its registered block rules and floors.
- 01:55: **Raw near enemies by role** (scratch rawreach2.py over 250 cached replica war games): holding 20+ raw with an enemy baby within dist2 13, per lost game: attackers 153 turns in 15.6 fifteen-round windows (mean raw 124), fighters 134 turns in 19.1 windows (41), gatherers 219 turns in 36.9 windows (41); per won game 34.4, 13.8 and 21.4 windows (raw 802, 135, 150). Dying non-attackers at war held 20+ raw on their last turn 10.7 times a lost game (mean 49), 7.7 of them with an enemy near (scratch lasttrap.py), mostly thrown (3.4) or trapped (1.8): a last-gasp trap could not be timed. **z0t = z0q + RAW_ANY** (new switch: z0q's raw-paid trap for every role): falsifiers in bots/z0t/SNAPSHOT before any z0t game: s6 screen zd112 (t <= -1.5, picks z <= -2, overruns, transfer per round < 0.97x, starved kings above u9p's 2 + 2); no dose-up under 212 traps (1.5x z0q's 141); hits under 0.7x; replica block rules and floors. Queue: zd110 (running), zd111, zd112, zd107, zd108, zd98, zd97, then the sweep.
- 02:00: **Enemy trap fires cluster by mine** (scratch trapclust.py over 300 cached replica dumps): within Chebyshev 4 of a mine on our half, their traps fired on us 21.1 times a lost game, 15.3 of them within 50 rounds of an earlier fire at the same mine (17.2 within 100, 18.6 within 200); in won games 4.5 and 2.9. Our picks at those mines inside the 50-round windows after a fire: 730 a lost game of 4,445 at our-half mines (won games 273 of 7,304). **z0u = u9p + MINE_BURN 50** (new switch): a baby an enemy trap fires on within Chebyshev 4 of a known mine bans the mine for 50 rounds and squeaks it (SQ_MBURN, twice); hearers ban it too and relay it once to a king in earshot, which stamps the mine's shared slot (spare bits of the mine words); pickMine passes over banned mines while another is left. Falsifiers in bots/z0u/SNAPSHOT before any z0u game: s6 screen zd113 (t <= -1.5, picks z <= -2, overruns, transfer per round < 0.97x); the opponents' trap hits on us not below 0.9x their 1,044 on u9p drops it, our deaths above u9p's drops it; replica block rules and floors. Diagnostic games on zd61's three most-trapped cells in ~/diag26b/mburn.
- 02:03: **How the field grabs us** (scratch grabwhy.py over 200 cached replica war games, nap rows against the previous round's health): enemy grabs of our babies at war 79.6 a lost game, 54.8 by the health rule (victim weaker) and 24.8 of rats at least as healthy as the grabber (mean health 86: the facing-away rule); won games 33.8 (21.8, 12.0). The healthy victims' last moves were spread: faceEnemy 4.4, attack 3.9, carry-aim 3.8, war 3.2, walking to a mine 2.3, flank 1.8. Guard facing (GUARD_DIST2 13 with GUARD_MEMORY) already ends turns facing the nearest known enemy; GLANCE_MEM is off. No candidate yet. **z0u's diagnostic games** (z0u as A; ~/diag26b/mburn): DefaultMedium vs araid lost at r479 (u9p as A lost at r375), their trap hits 44 (u9p's game 40), 13 of our deaths by trap (13), transfer 3,003 to 1,820 (u9p 1,120 to 1,404); evileye vs abrk lost at r1081 (u9p won on points), trap hits 19 (u9p's 42), 9 trap deaths (16). One game a cell decides nothing; zd113 does.
- 02:06: **z0u's third diagnostic game** (araid vs z0u, evileye, z0u as B; ~/diag26b/mburn/evB): won on points 65-34 at r2000 (u9p as B lost at r905 by king kill); araid's trap hits on us 27 (u9p's game 42), 11 of our deaths by trap (16), transfer 6,815 to 6,320; 0 overruns, 0 exceptions. Bytecode on the two earlier games: mean 9,053 and 8,135 a rat-turn (u9p 8,520 and 8,065 on the same cells), turns near 90% of the limit 32 and 48 (u9p 8 and 54): the squeak reads cost a little on DefaultMedium. Over the three games the opponents' trap hits were 90 to u9p's 124 (0.73x), under the registered 0.9x bar, but zd113 decides it.
- 02:10: **Where our attackers stand at war** (scratch attwhy.py over 250 cached replica war games, r100-1000, babies that took move code 'a' at least once): in lost games 758 attacker turns a game, 69% on our half, 24% on theirs, 7% within Chebyshev 6 of an enemy king; won games 801 turns, 54%, 37% and 9%. Move codes: attack 0.76 (won 0.79), faceEnemy 0.11, retreat 0.05, carry-aim 0.03. Raw 20+ on 30% of the turns in lost games (40% won). In the games we lose the rushers are pulled back to fight raiders on our half, so the enemy half goes unpressed, as the collapse rows showed (they picked 1,299 on their half in the 200 rounds before the collapse to our 120).
- 02:12: **z0r passes screen zd110** (s6 vs u9p's zd61, all registered gates). z0r (u9p + CAT_HUSH): n 44, D 7 (+4: araid DefaultSmall A, arrows B, evileye B, starvation A; −3: abrk bed A, evileye A, araid bed B), net +1, t +0.37 (gate −1.5); picks/100 35.18 vs 34.30 (per-cell z +0.38, gate −2); 0 overruns, 0 exceptions; transfer per round on the 44 shared cells 6.612 to 6.769 (0.977, gate 0.97), on the 21 length-matched cells 212,457 to 218,620 (0.972); our deaths to cats 1,302 to 1,279 (1.018x, gate 1.1x). Squeaks fell 13,179 to 20,602 (−36%), but our cat deaths did not fall; the cats killed 937 of theirs to 989. Kills 2,287 to 1,956 (+17%), spawned 4,133 to 4,031, deaths 2,931 to 2,862, opponents' trap hits on us 933 to 1,044; starved kings 4 to 2. z0r joins the replica queue after z0g and z0q. **z0v = u9p + RAW_RAID 8 + RAW_RAID_HALF 1** (new switch; z0s's raw-paid attacker trap on our own mid/far mines): over 250 cached replica war games our attackers stood at those mines with 20+ raw 84 turns a lost game (63 with no enemy baby within dist2 13; mean raw 147) while enemy rats spent 2,242 rat-rounds a lost game in the mines' 9x9 squares (380 won). Falsifiers in bots/z0v/SNAPSHOT before any z0v game: s6 screen zd114 (t <= −1.5, picks z <= −2, overruns, transfer per round < 0.97x); inert under 44 traps; hits under 0.5x laid drops it; replica block rules and floors. Diagnostic games vs araid in ~/diag26b/rhome.
- 02:14: **What flips our points losses: one more king** (field rows of 444 distinct replica games: rp_games, both twins and the z0c/z0g blocks; points = 30 cat share + 50 king share + 20 transfer share, which reproduces the scored points within 1). War points losses (38): cat share 0.365, kings 1.92 to 2.16, transfer 13,142 to 20,647, gap −14.2. One more living king at r2000 would flip 17 of the 38; double cat damage 15, 1.5x cat 8, double transfer 13. We lose few kings in those games (most at a time 2.03, at the end 1.92) and end them with 2,996 cheese banked on average (theirs 2,495): we form too few, not too late. Cooperation games (39, no backstab, won 32): the cats die early and transfer decides (TheHeist vs spsquared 1,040 to 1,560 and 2,660 to 3,740; peaceinourtime vs nzjt 1,230 to 1,280 and 1,000 to 2,345). The open candidate on this lever is z0h (FORM_RICH_BANK 1500: rich kings past the 2-king cap from a 1,500 bank up to r1150); its screen zd97 moved to the head of the queue, then zd98 (its mechanism run).
- 02:17: **z0w = u9p + RAW_TRAPS + CAT_HUSH** (z0q + z0r's switch, code copied unchanged): both passed their screens and act on different turns, and the replica runs one build at a time, so they go as one candidate. Falsifiers in bots/z0w/SNAPSHOT before any z0w game: s6 screen zd115 (t <= −1.5, picks z <= −2, overruns, transfer per round < 0.97x); raw traps under 100 or squeaks above 0.8x u9p's on the shared cells drop it as a broken combination; a t below z0q's −0.70 sends z0q alone instead. Queue: zd111 (z0s, running), zd97 (z0h), zd115, zd112, zd113, zd114, zd107, zd108, zd98, then the sweep. **z0v's first diagnostic** (z0v vs araid, evileye, ~/diag26b/rhome): won on points at r2000 (as u9p), 2 home-mine raw traps laid, both fired; their trap hits on us 54.
- 02:25: **z0g REJECTED after replica block 3** (#235; set 2, 17458-17486; `paired 12838-12899 IDS --crown 11678-11702`): 48 cells, 39 alike, D 9 (+4: YokoOno streetsofnewyork A, Varun clouds A, spsquared evileye B and jail B; −5: Finals attack_on_cat A, Varun clockwisdom B, ratsareants A and B, nzjt knifefight A), net −1, t −0.33; pooled over the three blocks D 30, net +4, t 0.73, under block 3's bar of 1.5. Keeping a truce that has lasted to r100 gained on sets 3 and 1 and gave it back on set 2. **z0q takes the replica** (submission 236; u9p + RAW_TRAPS): set-3 block 1 queued after 17486 (16 in the drain queue; waiter build/wait_z0qb1.sh), with its registered block rules and transfer floors. z0w (z0q + CAT_HUSH) screens at zd115; if it passes it can follow z0q.
- 02:31: **z0s REJECTED at screen zd111** (its registered transfer gate). z0s (u9p + RAW_RAID 8: attackers' raw-paid traps at enemy-half mines) vs u9p's zd61: n 44, D 7 (+4: araid DefaultSmall A, arrows B, evileye B, starvation A; −3: abrk arrows A, araid arrows A, bed B), net +1, t +0.37; picks/100 32.86 vs 34.30 (per-cell z −1.62); 0 overruns; transfer per round on the 44 shared cells 6.430 to 6.769 (0.950, gate 0.97), on the 27 length-matched cells 227,880 to 237,852 (0.958). Mechanism: 115 raid traps laid, 76 fired (0.66; bar 0.5). Pickups fell 16,935 to 17,696 and spawns 3,889 to 4,031, though attackers never deliver: the raid traps cost the gatherers' income somewhere, which z0v's screen (the same trap at our own frontier mines) will show again or not. **z0v's second diagnostic** (arrows vs araid): won at r2000 (as u9p), 0 traps laid.
- 02:35: **Why our formations fail** (scratch formk.py and formfail.py over the cached replica dumps; formation turns are move code 'K', Baby.formKing). In points losses (23 games) our babies spent 952 K-turns a game for 0.83 kings formed, in 1.35 attempts of which 0.52 failed (550 K-turns); in points wins 542 K-turns, 0.99 kings, 0.38 failed. Failed attempts in points losses had 7.1 of ours at most near the site (7+ in 69% of them), no enemy within 6, a 3,584 bank and 35 babies; current builds fail alike (u9p 13 of 39 attempts, z0g 23 of 51). The engine's test (RobotControllerImpl.assertCanBecomeRatKing): action ready, 50 cheese, under 5 kings (2 after r1200), the 3x3 on the map, passable, with no cat or king in it, and 7+ of our babies in it, the centre rat included. A debug copy of u9p, bots/zfk (not a candidate; the refusal reason in the indicator), vs u6h (~/diag26b/fk): superioristhmus (A) refused 114 times, every one "Not enough rats in the 3x3 square" (10 rows "cats or rat kings"), with 509 ring-hold turns, and we formed 2 kings to u6h's 3 (lost on points); minimaze (B) 203 refusals and 870 hold turns, 1 king each (won on points). Board at minimaze r650: six of ours in the site's 3x3, a seventh K rat one tile out behind an occupied ring tile, three ring tiles on the far side empty: joiners walk at the occupied centre (Nav.goTo(site)) and stall behind the near side. **z0x = u9p + FORM_FILL** (u7o's switch, code ported unchanged: joiners from beyond the 3x3 walk to the nearest free ring tile). u7o on u6x was rejected at replica block 1 on D 3 (+0 −3) with the mechanism holding; this is its first test on the u9p base, with the points-loss and refusal evidence above. Falsifiers in bots/z0x/SNAPSHOT before any z0x game: s6 screen zd116 (t <= −1.5, picks z <= −2, overruns, transfer per round < 0.97x); mechanism zfk2 (z0x + the indicator) on the same two games: refusals not below 0.5x zfk's 317, or fewer than zfk's 3 kings formed, drops it; replica block rules, transfer floors, and points-game kings at r2000 above the twin's. Queue: zd97 (z0h, running), zd115 (z0w), zd116 (z0x), zd112, zd113, zd114, zd107, zd108, zd98, then the sweep.
- 02:42: **z0x DROPPED before its screen** (its registered mechanism falsifier). zfk2 (z0x + zfk's refusal indicator) vs u6h, same seats (~/diag26b/fk2): superioristhmus (A) refused 129 times (zfk 114), lost on points, 1 king formed (zfk 2); minimaze (B) 104 (zfk 203), won on points, 1 king. Sum 233 to 317 (0.73x, bar 0.5x); kings 2 to 3. Screen zd116 removed from the queue. On superioristhmus the centre rat's later refusals were "cats or rat kings": the site (50,56) was named while our king stood elsewhere, the king then walked to its home mine (48,56) and its body at (47-49, 57-59) covered the site's tile (49,57), and the king kept the site's heartbeat fresh to the timeout. So two faults: joiners that stall (FORM_FILL eases it on minimaze) and a site the king's own body or a cat makes void, which nothing re-checks. Debug runs counting refusals by reason (zfk and zfk2 on superioristhmus, where_are_you, minimaze and cheesebottles vs u6h) in ~/diag26b/fk3.
- 02:47: **z0y = z0x + FORM_KEEP** (the second formation fault). No king step in closer()/step() (the cat dodge untouched) puts the king's 3x3 on the open site's 3x3 unless the step leaves the site; every king reads the site from the array, since only the first king holds it. The first king re-checks its open site each turn and drops it when a tile of the 3x3 is off the map, impassable or under a cat or a king (our own body included), naming the next after 5 rounds instead of FORM_TTL + FORM_RETRY (210). fk3 split so far (superioristhmus, A vs u6h): zfk refusals rats 104 + king/cat 10, kings 2 (u6h 3); zfk2 rats 74 + king/cat 55, kings 1. Falsifiers in bots/z0y/SNAPSHOT before any z0y game: zfk3 (z0y + the indicator + site-drop count) on the four fk3 games; king/cat refusals not below 0.5x zfk2's drops FORM_KEEP as inert, all refusals not below 0.5x zfk's or fewer kings than zfk drops z0y; then s6 screen zd117 with the standard gates plus kingsFormed below u9p's 31.
- 02:58: **z0y DROPPED by its mechanism falsifier.** zfk3 (z0y + the indicator) on the four fk3 games vs u6h: refusals superioristhmus 97, where_are_you 103, minimaze 104, cheesebottles 26, total 330 against the bar of 268 (0.5x zfk's 535: 114, 194, 203, 24); zfk2 (z0x) 362. FORM_KEEP's own target fell as designed: "cats or rat kings" refusals 0 (zfk2 55, zfk 10), and kings formed 6 to zfk's 5 (superioristhmus 3 to 2, with u6h's 3). On where_are_you, minimaze and cheesebottles zfk3 replayed zfk2's refusals exactly (FORM_KEEP never acted there); the where_are_you game diverged after r1100 and both our kings starved at r1500 (bank 0 from r1440) where zfk2 recovered to 1,376, so the second king on that map stays a starvation risk. **z0h passes screen zd97** (s6 vs u9p's zd61, its registered gates): n 44, D 0, t 0.00; picks/100 34.84 vs 34.30 (per-cell z −0.30); 0 overruns, 0 exceptions; transfer per round 0.981 (length-matched 0.995); kingsFormed 35 to 31. Its mechanism run zd98 moved to the head of the queue and is playing.
- 02:58: **The lone-king points games** (scratch kbank.py, endk.py, endk2.py, lonek.py over the VM dump cache; field rows): in points losses we hold 1.77 kings at r1150 to their 1.94 and 1.79 at the end to their 2.17. In 17 war points losses we sat with one king, 16+ babies and a 300+ bank for 100+ rounds before r1150 and never opened a formation (clouds, corridorofdoom, uneruesansfin, rose, averystrangespace, learn_your_lesson, hatefullattice, cheeseguardians); the same in all 5 cooperation losses (TheHeist, peaceinourtime), which end when the cats die (r353-429), with nzjt's second king at r265 in two. The local u9p vs u6h on uneruesansfin reproduces it: the king's income estimate stays 0-440 a hundred rounds (the test needs 400), bank 343-1,700, one king each at the end. END_KING (r1850) should rescue the war games, but in 15 of the 19 points games we ended with one king the bank never reached END_KING_CHEESE 400 over r1850-2000 (most 39-357: the spawn floor holds it there), and where it did (learn_your_lesson, u9p and z0g A, banks 505-1,035) 18 babies queued from r1850 and every centre in reach was blocked by our king's body (sitegrid at r1950), z0y's second fault. Field rows: a second king flips 8 of the 15 lone-king points losses. **z0z = u9p + FORM_KEEP + END_KING_SAVE 1800** (new: from r1800 a lone king with 10+ babies and no enemy in view spawns only above END_KING_CHEESE + 50). Falsifiers in bots/z0z/SNAPSHOT before any z0z game: on the seven lk maps vs u6h (u9p's games the control) games ending with 2+ kings must exceed u9p's by 2, and no king may starve where u9p's did not; then s6 screen zd118 with the standard gates; replica rules, floors and points-game kings.
- 03:01: **The main formation fault, drawn** (zfk minimaze B, r330, sitegrid): the site (32,44) is in the map's corner. Six of ours hold the centre and the open side of the ring; the three corner tiles (32,45), (33,45), (33,44) touch nothing outside the 3x3, so they can be reached only through occupied tiles, and the K rats at (30,44), (30,42), (32,42) wait outside. The ring never shifts, so "Not enough rats in the 3x3" repeats (203 times that game). **z1a = u9p + FORM_PACK** (new): a ring rat with the centre taken steps to a free ring tile beside it that fewer passable (or unseen) outside tiles touch, strictly fewer, so the packing ends; the free tiles left are the ones joiners can reach. Falsifiers in bots/z1a/SNAPSHOT before any game: zfk4 (zfk + FORM_PACK + a pack-step count) on the four fk3 games, refusals below 268 (0.5x zfk's 535) and kings formed 5+ or it is dropped; then the s6 screen with the standard gates plus kingsFormed above u9p's 31. The zfk4 games and z0z's lk games are playing.
- 03:03: **END_KING fires but pulls too few** (u9p vs u6h on uneruesansfin, A, ~/diag26b/lk; z0z played the same game): from r1850 the site by our king at (4,54) drew about 4 babies (80 K turns a 20-round span) of 21-24 alive, with a 690-890 bank (the baby cap bound, so END_KING_SAVE never acted), and no king formed; FORM_PULL_R2 72 keeps the gatherers at their far mines. **z1b = z0z + END_PULL_R2 10000** (new: from r1850 babies join from any distance; FORM_JOIN 8 still turns back those that see 8 allies nearer). Falsifiers in bots/z1b/SNAPSHOT before any z1b game: on the lk maps 2+ kings at the end in 2 more games than u9p, a second king on uneruesansfin, no starved king or post-r1850 king loss that u9p did not have; then the s6 screen. Its lk games are playing beside z0z's and zfk4's.
- 03:04: VM disk at 2.1 GB free: pruned the 361 replica replay files (448 MB) older than 36 hours (list in /root/bc26-replays-pruned-20261106-1104.txt) and removed my debug dumps ~/diag26b/fk and fk2 (167 MB; the same bots and seats replay in ~/diag26b/fk3, except zfk on learn_your_lesson). The root has 2.7 GB free.
- 03:07: **A third formation fault: no site at all** (u9p vs u6h on clouds, A, ~/diag26b/lk). Our king sat at (2,8) in its corner all game; the income test passed from r400 (inc 588-960 a hundred rounds) with 20-40 babies and a ~1,000 bank, yet no baby took a formation turn before r1850: pickFormSite found no clearly-ours mine with a seen, clear 3x3 and the spot four behind the king is off the map, and only the rich branch falls back to the ring beside the king. END_KING formed the second king at r1894 (2 to 2 at the end, a points win). The replica's clouds games vs Xzibit were lost 47-52 three times on 1 king to 2. FORM_RING (the same fallback, t0h on s9s, #106) lost evileye to two starved kings after a second king at r100-125, so **z1c = u9p + FORM_RING_FROM 400** (new: the ring fallback only from r400, after the back spot). Falsifiers in bots/z1c/SNAPSHOT before any game: no second king before r1150 on clouds drops it; a starved king on evileye (A, B) where u9p's did not drops it; then the s6 screen with kingsFormed above u9p's 31. Games in ~/diag26b/rf.
- 03:09: **The cooperation loss, reproduced** (u9p vs u6h on peaceinourtime, A, ~/diag26b/lk): cats dead at r450, lost on points 1 king to 2. Our king's income estimate ran 160-340 a hundred rounds with 27-30 babies and a ~900 bank, short of the 400 the formation test needs (our 41 cat traps to u6h's 31 count against it); u6h, with the same formation settings, formed at r376 on 480 at its back spot. Replica cooperation points games (27): 21 end by r676; at 1-1 kings we won 16 and lost 3 (TheHeist vs spsquared 48-52 twice, peaceinourtime vs nzjt 49-50), at 1-2 lost 1. **z1d = u9p + FORM_NET100_COOP −200** (new: the income test one cheese a round looser during cooperation). Falsifiers in bots/z1d/SNAPSHOT before any game: a second king in 2 of the 4 peaceinourtime/TheHeist games (both seats vs u6h) where u9p formed none, no starved king and no cooperation game lost that u9p won; then the s6 screen as a no-harm check.
- 03:13: **z1a passes its mechanism test** (zfk4 = zfk + FORM_PACK + a pack counter, same four seats vs u6h as zfk/fk3, ~/diag26b/fk4). "Not enough rats" refusals fell from 535 to 164 (minimaze 203 to 3, where_are_you 194 to 62, superioristhmus 114 to 76, cheesebottles 24 to 23), under the bar of 268; formation holds fell from 2,275 to 379. Kings formed were 5 both times, so packing cuts the wasted waiting but makes no extra king on these four. where_are_you went from a points win to a points loss (one game; zfk3 lost it too). Screen **zd119** (z1a vs u9p, the zd62 cells) queued after zd115, with the standard gates plus kingsFormed above u9p's 31.
- 03:17: **z0z DROPPED as inert** (its registered mechanism bar). On all seven lk maps vs u6h (A, seed 0) it played u9p's games: the same results, kings at the end and formation rounds (2+ kings at the end in 3 games each; the bar was u9p's 3 + 2); no king starved. END_KING_SAVE never acted (the bank was above END_KING_CHEESE + 50 at r1800 in every game), and no site was voided under FORM_KEEP. **z1b DROPPED** by the same bar: clouds 2-2 (formed r1885, u9p r1894), corridorofdoom 2-2 (r1853, same), uneruesansfin **2-1 (formed r1913; u9p stayed 1-1)**, at most 4 with averystrangespace (u9p's was 2-2 already) and the three short games (cooperation and rose end before r1850, so z1b = z0z = u9p there); I stopped its last games. Transfer from r1850: −60 on uneruesansfin, +40 on clouds; no king starved, no king lost. **z1e = u9p + END_PULL_R2 10000** (new: z1b's switch alone). Falsifiers in bots/z1e/SNAPSHOT before any z1e game: no second king on uneruesansfin (A) drops it; then screen zd120 on fresh games, where kingsFormed not above u9p's 31 drops it as inert, plus the standard gates. z1d's cooperation games (~/diag26b/cp) restarted: z1d A/B and u9p B on peaceinourtime and TheHeist (u9p's A games are the lk ones).
- 03:17: **z1c passes its mechanism test** (~/diag26b/rf, vs u6h, seed 0). clouds (A): the ring site gave a second king at **r647** (u9p's only came from END_KING at r1894); 2-2 at the end, a points win again, transfer 13,236 to u9p's 13,210, and the bank at r2000 fell from 4,499 to 2,592. evileye: no king starved (the falsifier), but **z1c lost A at r1110 where u9p won at r1527, and lost B at r1114 where u9p held to r1712**: u6h's rats bit both our kings to death, the second (formed r631 A, r672 B) beside the first, so one raid reaches both. Screen **zd121** (z1c vs u9p, the zd62 cells, evileye included) queued after zd119; the standard gates plus kingsFormed above u9p's 31. The screen maps hold no lone-king map such as clouds, so the screen can only show harm; the replica decides.
- 03:22: **z0q continues after replica block 1** (#236; set 3, 17502-17505, 17517-17520, 17535-17542; `paired 13248-13291 IDS --crown 12130-12163`): 48 cells, 38 alike, D 10 (+6: Icarus averyfineline A, Xzibit uneruesansfin A, YokoOno 5t4rv4t10n B, Varun corridorofdoom B, spsquared TheHeist A and B; −4: Xzibit minimaze A, YokoOno 5t4rv4t10n A, spsquared rift A, waxmango FreeTheCheese A), net +2, t 0.63 (block 1 rejects at t ≤ 0). Falsifiers (field_report rows on the VM, research/audits/blockcheck.py against the twin's rp_games rows): transfer 4,396 a game (set-3 floor 3,968; twin 4,409), pass. Cat share in the 12 points games 0.606 to the twin's 0.444 in 15; kings alive at the end of the points games 2.0 to 1.87; wins by king kill 9 to 8. Set-1 block 2 queued after 17542 (4 sent, 12 in the drain queue); waiter build/wait_z0qb2.sh.
- 03:25: **The endgame king is cheaper than END_KING thinks** (engine 1.2.6 sources; scratch endbank.py over the VM dump cache). A king pays 2 a round from the bank or loses 10 health when it is under 2; robots update in spawn order, so the old king pays first; a new king takes its 3x3's health up to 600. So a king formed with 55 rounds left lives to r2000 on an empty bank, the bank is worth nothing at the end, and one more king of 2-3 swings the war points margin by 16.7. END_KING asks for 400 banked: in 12 lost points games we ended with one king (u9p family), 9 never had it from r1850 (clouds x4 and uneruesansfin x3 vs Xzibit, corridorofdoom vs Varun, cheesebottles vs nzjt; 50-300 banked, 12-20 babies). A bar of min(400, 80 + 4 * max(0, R − 55)) (R the rounds left; both kings paid down to the last 55) is met in all 12, 10 by r1937. **z1f = u9p + END_KING_DYN** (new; that bar while our king's health outlasts 55 rounds of starving). Falsifiers in bots/z1f/SNAPSHOT before any z1f game: on evileye vs abrk (A; u9p's zd61 cell 1-1, bank 129, won 50-49) no second king drops it; on clouds, uneruesansfin, corridorofdoom and cheesebottles vs abrk and araid (A), z1f must end with two kings in half the games where u9p ends with one and the bar is met; any king of ours starving where u9p's did not drops it; then screen zd122. Games in VM ~/diag26b/dk.
- 03:25: **z1d DROPPED** (both of its registered mechanism falsifiers; ~/diag26b/cp vs u6h, seed 0; u9p's A games are the lk ones). u9p formed no second king in TheHeist A and B and peaceinourtime A; z1d formed one only in peaceinourtime A (r321, a win where u9p lost 1-2), not in TheHeist (cats dead by r280, before any formation could gather), so 1 of 3 against the bar of 2. It also lost peaceinourtime B (formed r276, 2-2) where u9p won (formed r347, 2-2). **z1e passes its mechanism test** (~/diag26b/ep): uneruesansfin A 2-1, second king formed at r1913 (u9p 1-1), bank 74, no king starved; the same game as z1b's. Screen **zd120** (z1e vs u9p) queued after zd121.
- 03:30: **z1f DROPPED** (its registered falsifier: no second king on evileye vs abrk, A). It played u9p's game (1-1, won 50-49, bank 129): our king had 12-238 health from r1840 (abrk's rats on it) with 99-299 banked, and z1f's health condition, 10 * min(R, 55) + 50, is full health before r1945, so its bar never applied. The condition guards nothing: a king formed with 55 rounds left outlives an empty bank, so at worst the old king starves and we hold the one king we had. Spawns can take the bank to the 'f' floor 2R, which two kings eat in R/2 rounds, so the lower bar is safe only from r1890 (R/2 = 55). **z1g = u9p + END_KING_DYN_FROM 1890** (new: z1f's bar from r1890, no health condition). Falsifiers in bots/z1g/SNAPSHOT before any z1g game: no second king on evileye vs abrk (A) drops it; on the four lone-king maps vs abrk and araid (A), two kings in half the games where u9p ends with one and the bar is met; ending with fewer kings than u9p, or a king-destruction loss after r1850 that u9p did not have, drops it; then screen zd122 (cells now z1g). The dk chain restarted for z1g (VM ~/diag26b/dk/chain.sh; u9p's evileye and clouds-vs-abrk games are kept).
- 03:35: **z0h REJECTED by its mechanism run zd98** (z0h = u9p + FORM_RICH_BANK 1500; vs u6h and s7d on zd96's 32 cells, u9p's games there the control; `research/audits/mech_cmp.py runs/zd98/results.jsonl z0h runs/zd96/results.jsonl u9p`). kingsFormed 30 to u9p's 27 (pass), but king deaths 13 to 10 (bar u9p + 2 = 12) and wins 15 to 18 (bar u9p − 2 = 16): two falsifiers fire. Among the rows shown, z0h lost where u9p won on cheesebottles B (vs s7d and vs u6h), uneruesansfin B (s7d), FreeTheCheese A and Stash B (u6h), and won where u9p lost on Stash B (s7d) and superioristhmus B (u6h). Cat damage by bites 88.5k to 100.8k. The screen queue moved on to zd115 (z0w) at 03:25.
- 03:40: **z1h = u9p + COOP_DUMP 10** (new): in cooperation an attacker carrying 10+ cheese spends all of it on its next cat bite (10 + ceil(sqrt(c)) damage; the load slows it 1% a unit and attackers never deliver). Replica dumps (scratch coopraw.py, 22 u9p-family cooperation points games): our rats held 41.6k cheese when the cats died, 19.6k on attackers. Falsifiers in bots/z1h/SNAPSHOT before any z1h game: mechanism on VM ~/diag26b/cd (vs u6h, peaceinourtime and TheHeist, both seats; u9p's lk and cp games the control): mean load on attack turns not below half of u9p's drops it as inert, summed cat damage not above u9p's drops it, a cooperation game lost that u9p won drops it; then screen zd123 (cells made). u9p's control (scratch coopcmp.py): mean load 53.7 on 14,980 attack turns, cat damage 17,165 over the 4.
- 03:46: **z1h DROPPED** (its registered inert falsifier): mean load on attack turns 47.5 to u9p's 53.7 (bar: under half), cat damage 17,075 to 17,165, 1 win of 4 as u9p. In actions.tsv not one z1h cat bite spent cheese: the dump was limited to the ATTACK role (the king rushers), and cooperation cat biters are other roles. The same games show the cooperation bank idle at 900-1,100 from r100 to the end (u9p and z1h, both seats, rounds.tsv), while the BUDGET rule (projected surplus x4 >= 600) kept all 43 of u9p's peaceinourtime A cat bites at 0 cheese. A bite with c cheese does 10 + ceil(sqrt c), so c = 2 gives the most damage per unit (1 a unit). Cat damage is 0.5 of the cooperation points from a fixed pool (the cats' health) and transfer 0.2 of a pool both teams grow, so a unit spent this way is worth about twice a unit delivered. **z1i = u9p + COOP_BITE 2** (cooperation cat bites spend 2 when the bank holds 300 or the rat carries 2). **z1j = z1i + COOP_BITE_HI 5** (5 cheese, +3 damage, while the bank holds 600: the idle-bank bracket). Falsifiers in each SNAPSHOT before any game: mechanism on VM ~/diag26b/ci (as z1h's, u9p's games the control). Under half of our cat bites spending cheese drops it as inert, summed cat damage not above 17,165 drops it, a cooperation game lost that u9p won or a starved king drops it, and for z1j a bank under 300 after r100 also drops it. Then screens zd124 (z1i) and zd125 (z1j) (cells made). Measured and set aside: a war version for king bites. Across 13 local u9p games (dk, lk, cp, ep; scratch kbload.py) our rats bit enemy kings 32 times at a mean load of 12.5, none at 100+.
- 03:48: **z1k = u9p + WAR_CAT_BITE 2 (floor 800)** (new, z1i's code with COOP_BITE 0): at war a cat bite spends 2 cheese while the bank holds 800. In u9p's local war games on clouds (vs abrk, araid, u6h) our rats bit cats 391-445 times, 1-2 of them with cheese, while the bank held 900-2,000 until the cats died (r900-1300; cat damage is 0.3 of the war points). Falsifiers in bots/z1k/SNAPSHOT before any game: mechanism on VM ~/diag26b/wk (4 A-seat games, u9p's dk and lk the control; it runs after ci). Under half of the war cat bites at bank 800+ spending cheese drops it as inert; cat damage not above 16,803 drops it; a game lost that u9p won, fewer kings formed or a starved king drops it. Then screen zd126 (cells made).
- 03:49: **Correction to the z1h entry.** actions.tsv bite rows carry a lethal flag in column e, not the cheese spent (ReplayDump: the ceil(sqrt c) boost is not in the action), so "not one z1h cat bite spent cheese" was a misreading. The summary's catDmgBite / bitesCat shows the same thing. z1h's per-bite damage was 10.05-10.84 against u9p's 10.06-10.83, so its dump barely fired. The drop stands on its registered falsifier (mean attack-turn load 47.5 vs 53.7). The role explanation fits but is unverified. **z1i DROPPED** by its registered cat-damage falsifier, though its mechanism fired: 11.7-12.0 damage per cat bite (u9p 10.1-10.8). Summed cat damage was 16,052 to u9p's 17,165 (bar: above it). The difference is the trap-led peaceinourtime games: A 3,839 vs 4,140 with 29 bites to u9p's 43, and B 4,213 vs 5,025, though z1i still won B as u9p did. TheHeist gives each team 4,000 whatever the bites. Results by seat were as u9p's (W only in peaceinourtime B), with no king starved. On these maps traps, not bites, decide the cat pool, so a +2 a bite shows up only below the noise of one seed. z1j (the 5-cheese bracket) and z1k (war, bank 800+) keep their registered tests.
- 03:52: **z0w DROPPED at screen zd115 by its registered mechanism falsifier** (raw traps under 100, i.e. 0.7x z0q's 141). The screen gates all passed: n 44, D 8 (+3: araid evileye B, starvation A and B; −5: abrk DefaultSmall A, bed B, araid DefaultSmall B, bed B, evileye A), net −2, t −0.70 (gate −1.5); picks/100 34.61 vs 34.30 (per-cell z −0.80); 0 overruns, 0 exceptions; transfer per round on the 44 shared cells 6.707 to 6.769 (0.991, gate 0.97; transferAmt over rounds, the z0q measure, which reproduces z0q's 0.9725), 0.987 on the 18 length-matched cells. Raw traps over the 48 games were 97 (90 fired) against z0q's 141 (137 fired), the same count as the 141. Squeaks on the shared cells were 14,410 to u9p's 20,602 (0.70; bar 0.8, so CAT_HUSH works). The combination cuts the raw traps by a third, plausibly because hushed squeaks also carry the enemy sightings that bring attackers within trap reach. z0q stays on the replica alone, and z0r (CAT_HUSH alone, passed zd110) is the next replica candidate after it.
- 03:53: **z1j DROPPED** by two registered falsifiers. It lost peaceinourtime B, which u9p won, and its summed cat damage was 16,248 against u9p's 17,165. Its mechanism fired, at 12.2-13.0 damage a cat bite. Cat traps explain the loss of cat damage. A cat trap costs 10 and deals 100, ten times a cheese bite's rate, and the cheese bites came with fewer trap hits on peaceinourtime: u9p 38 and 34 (A, B), z1i 35 and 28, z1j 21 and 29. Bite damage rose (z1j A 2,055 to u9p's 448) but trap damage fell more (2,100 to 3,692). The cooperation cat-bite cheese family is closed; z1k (war, bank 800+, after the cats' traps are barred 100 rounds into the war) keeps its registered test.
- 04:02: **Replica losses by ending, recent u9p-family blocks** (scratch lossclass.py over z0c, z0g and z0q blocks; 240 games): king kills dominate the losses, by end round <500: 52, <1200: 49, <1850: 10, 1850+: 2; war points 16 and cooperation points 2. Mid-game king kills (kkmid.py, 500-1850) end by starvation in 31 and by bites in 19. Most starvations follow an army collapse with nothing carried (starvejam.py). The exception is a jam on rose: the king starved at r1107-1342 with 19-20 babies alive and 600-1,040 raw held within Chebyshev 10 of it for the last 200 rounds (vs thewaxmango, AdamTan12_Icarus, spsquared in two blocks), and fullofsecrets vs spsquared alike (1,030). The 17455 snapshot at r1000 (snapmap.py) shows empty rats sitting in the mouth of the 1-wide stem out of our king's pocket and the loaded ones waiting inside it: 6 loaded at 5-8 tiles, 9 empty within 4. Opponents' transfer pulls ahead from r100 to r300 (econ_early.py: AdamTan12_Finals 0.84 to 0.58, Icarus 0.86 to 0.52, YokoOno 0.93 to 0.48 of theirs), and from r100 to r300 we spawn 8-14 to their 9-19. **z1l = u9p + SWAP_LOADED 30, SWAP_HOME 10** (new): a rat carrying 30+ raw, held 2 turns behind an allied baby carrying under 10 within Chebyshev 10 of our nearest king, carries it past (u9p's dormant swapPast) and sets it down behind. Unlike u1s's general swap (rejected at screen, t −1.16), nothing else changes. Falsifiers in bots/z1l/SNAPSHOT before any game: mechanism on VM ~/diag26b/sl (rose vs araid, abrk, u6h and fullofsecrets vs abrk, both seats, u9p alongside). Under 10 ally grabs drops it as inert. Where u9p jams, z1l must not starve in at least half. Fewer wins than u9p, transfer under 0.95x, or any exception or overrun drops it. Then screen zd127 (cells made; it holds no rose cell, so it tests harm only).
- 04:02: **z1g DROPPED** by its registered falsifier (2) (VM ~/diag26b/dk, 9 A-seat games vs abrk and araid; lkcmp.py). In three games u9p ended with one king and z1g's bar was met with 7+ babies from r1890. z1g ended with two kings in only one of them, against the bar of half: corridorofdoomanddespair vs abrk (formed r1911, 2-1, bank 1 at r1995: the 55-round reserve held). It failed on evileye vs abrk: formed r1963, but one king died by r1980 to abrk's rats, so 1-1 as u9p. It failed on uneruesansfin vs araid: the bar was met from r1910 with 19 babies, but no site was named and the game is u9p's to the bank. Falsifier (1) passed (evileye formed), there was no king-destruction loss, and wins were 9 of 9 for both, with kings at the end 5 to 4. The bar works where a site exists. Its gaps are king survival after forming and finding a site on narrow maps. **z1k DROPPED** by three registered falsifiers. It lost clouds vs araid (king destroyed r1208; u9p won at r2000), formed no king there to u9p's one, and its summed cat damage was 16,554 to 16,803. Per-bite damage rose 0.2-0.5, and 568 of its 1,420 cat bites came at war with the bank at 800+.
- 04:10: **Early replica king kills (<r500) by cause** (scratch kkmid.py over the z0c, z0g and z0q blocks): starve 28, bite 12, thrown rats 10, cat scratch 2. The starvations are economy collapses: bank under 100 and 0-8 babies 100 rounds before (Icarus averyfineline and mincemeat, nzjt whatsthecatdoin, YokoOno 5t4rv4t10n and externalsustenance). The reserve doctrine (u6e, u9i) and the anti-throw doctrine (u1k, u7b, u7i, AMMO_AWAY, the carrier guard) are closed. By opponent, the in-band ones are AdamTan12_Finals 12 of 30 (13 king kills in r500-1200 on keepout, wallsofparadise and jail), YokoOno 10, collegejunior 9, Icarus 14, spsquared 17 and Varun 19. **FreeTheCheese vs collegejunior** (17537 and 17538 g1): both our kings starved at r1405 and r1770 with 16-18 babies, all of them within 4 of a king and carrying under 10 for the last 200 rounds (starvejam.py). It has rose's shape: one tile at the stem mouth, (30,12), lies within transfer reach of the king at (30,9). The army had already shrunk, though: our rats past y 19 fell from 895 rat-turns in r100-200 to 0 by r1300, against their 52 babies. **VM disk**: 3.1 GB free after pruning 76 replica replays (84 MB; list /root/bc26-replays-pruned-20261106-1203.txt), 143 old build/dumpcache dirs and 259 old ~/diag26b replays (their tsv files kept).
- 04:15: **z0q passes replica block 2** (set 1, 17551-17605 vs the twin's 12771-12813, crown 11549-11677): 48 cells, 42 alike, D 6 (+5: Icarus rose B, YokoOno clouds B, Varun knifefight B, spsquared bed A, spsquared fullofsecrets B; −1: Icarus rose A), net +4, t +1.63. Transfer 4,214 a game (floor 4,133; twin 4,592); 26 of 48 games won to the twin's 21; king-kill wins 17 to 13. Pooled with block 1 (96 cells): D 16 (+11 −5), net +6, t ≈ 1.51, between block 2's reject (t ≤ 1) and accept (t ≥ 2) bars, so **z0q goes to block 3** on set 2 (build/block_v4s2.sh: 14700-14703 sent, 12 in the drain queue; twin rows twin_s2.jsonl). **z1a REJECTED at screen zd119** by two registered gates: kingsFormed 30, not above u9p's 31, and transfer per round on the 44 shared cells 6.534 to 6.769 (0.965, gate 0.97; scratch xpr.py, which reproduces zd115's 0.991). Passed: n 44, D 1 (+1 araid starvation A), net +1, t +1.00; picks/100 33.52 vs 34.30 (per-cell z −0.45); 0 overruns, 0 exceptions. zd121 (z1c) is running.
- 04:25: **z1l passes its mechanism games** (VM ~/diag26b/sl; rose vs araid, abrk and u6h, fullofsecrets vs abrk, both seats; scratch sltab.py and jamchk.py). It won 6 of 8 to u9p's 3, with transfer 23,859 to 12,790 (1.87x), 445 ally carries, 0 exceptions and 0 overruns. u9p lost rose A to abrk and araid by starvation (r867, r817) and fullofsecrets B to abrk (r625). Two of those are jams by the registered test, with 300+ raw within Chebyshev 10 of the king 100 rounds before: rose abrk A had 940 and fullofsecrets abrk B had 760 (rose araid A had 180). z1l won rose abrk A at r2000 (4,095 delivered, two kings). It starved on fullofsecrets B exactly as u9p did: the game replayed u9p's, with no carries, so the mechanism never fired there. That is 1 of 2, which meets the half bar. z1l also won rose u6h B (u9p lost on points), and every rose game went to it. **Screen zd127 queued first** (gates line added on the VM and locally). **Stash exit** (replica 17570 g0 vs YokoOno: we delivered 80 by r100 and 100 by r300 to their 2,605): 96% of our rat-turns before r200 were in the home strip, with both corner exits dug by r40. YokoOno's rats were out by r15-20, and from r111 they trapped our exits at (1,9), (1,7) and (27,8). Locally (VM ~/diag26b/stx, u9p vs abrk A) the first gatherer's reverse search toward (10,18) ran r9-r26 without finishing (the 2026-10-30 log saw the same). The first rats left at r47-96, and two never left. **z1m = u9p + FLOW_W 2, z1n = u9p + FLOW_W 3** (the existing weighted search, never a candidate; 325-457 pops to 419-606 offline at w 2). Falsifiers are in their SNAPSHOTs: a mechanism chain on VM ~/diag26b/fw (Stash, cheesebottles, FreeTheCheese and wallsofparadis vs abrk, both seats). The median first-exit round of the first 8 rats on Stash must be 10+ earlier than u9p's in both seats; r300 transfer above u9p's; no exception or overrun; wins not below u9p's. Then the s6 screen.
- 04:41: **z1c REJECTED at screen zd121** by the registered transfer gate: transfer per round on the 44 shared cells 6.427 to u9p's 6.769 (0.949, gate 0.97; scratch xpr.py). Passed: n 44, D 5 (+1 araid evileye B; −4 abrk arrows B, abrk evileye A and B, araid evileye A), net −3, t −1.35 (gate −1.5); picks/100 36.51 vs 34.30 (per-cell z +2.10); kingsFormed 38 to 31; 0 overruns, 0 exceptions. The evileye losses (our king destroyed r1035-1430) are the risk the r400 floor was meant to hold off: a ring-site second king costs evileye. **minimaze diagnosis** (VM ~/diag26b/mz, u9p vs abrk and araid, both seats): 4 of 4 won, transfer 42,277, no jam, so the replica loss there is specific to Xzibit.
- 04:41: **Early economy by opponent** (scratch econ2.py over the z0c, z0g and z0q blocks, 246 replica games; rounds.tsv engBabies, pickedX, cheeseXfer): we field more babies than every in-band opponent at r50-r100 (20-24 to their 15-19), but pick far less per baby-round over r1-200: 0.18-0.26 against Finals 0.50, Icarus 0.37, YokoOno 0.36, Merlin 0.36, Xzibit 0.36 and Varun 0.38 (only spsquared is close, 0.35 to 0.40). Picked by r200: Finals 720 to 1,598, YokoOno 659 to 1,248, Icarus 763 to 1,164. Twelve of our first 16 babies are rushers (spawn index 4-15, RUSH_UNTIL 300), and the rush family is closed (RUSH_TO 12 net −7, RUSH_TO 20 net −3, RUSH_DIST, RUSH_LIFE). The per-gatherer rate cannot be split out of replica replays: they carry no indicator strings.
- 04:41: **Cat king kills on the replica** (scratch catking.py, 384 games): cats scratched our kings for 16,654 and theirs for 19,140, and killed 16 of our kings to 18 of theirs; 10 of our 201 losses had a cat kill our king. Recurring: bed vs spsquared B (r246 both times: cat 4 scratched our king 30 times from r154 at (18-19,1-9)), cheesebottles vs nzjt A (r1169-1340), uneasy_alliance vs Icarus (both seats). Engine (InternalRobot, cat EXPLORE): a cat walking to its waypoint scratches whatever blocks its next step, and it enters ATTACK only on reaching a waypoint, for 8 turns. In the bed game our king fled straight away from the cat (u9p's default: b.directionTo(c)), which is the cat's own heading, so it ran ahead of it up the east edge into the corner, where it blocked the cat's way to its waypoint until it died. KING_DODGE (step out of the cat's next two footprints) is u9p's existing switch for this: v0a = u9p + KING_DODGE was registered for screen zd66 in the old sweep and never ran. **zd66 moved up the queue** (after zd120), with the s6 transfer gate added to its SNAPSHOT before any game. **VM disk**: 2.9 GB free after deleting 49 build/dumpcache entries older than 3 hours (regenerable from ~/diag26b/rp).
- 04:45: **z1m and z1n DROPPED** by registered falsifier (1) (VM ~/diag26b/fw, Stash vs abrk, seed 0; scratch firstexit.py: the first round each of our first 8 rats stands in the middle band y 8-21, between the walled home strips at y 0-6 and 23-29). Medians by seat, A/B: u9p 176/141.5, z1m (FLOW_W 2) 199/106.5, z1n (FLOW_W 3) 203/141.5. The bar was 10+ rounds earlier than u9p in both seats, and seat A is later for both. Several of the first 8 never enter the middle (9999 in the lists), so first rats working home-strip mines dominate the measure. The fw chain was stopped after Stash (pids 1919891, 1951164-5). **whatsthecatdoin diagnosis** (VM ~/diag26b/wc): u9p beat abrk and araid in all 4 seats (king kills r500-905), so the nzjt loss there is specific to that opponent. **uneasy_alliance vs Icarus** (17318 g2, lost both seats in every block; kings start 8 apart on one row): by r250 Icarus laid 54 rat traps on the tiles in front of our advancing rats (x 12-17), and ours triggered 48, mostly 0-3 rounds after laying. Our deaths: 19 bites, 12 traps, 20 throws, 11 cats; theirs: 25 throws, 13 cats, 4 to our king's bites, 1 to our rats' bites. Their king never had one of ours within 4 after r50, and our second king (formed r100) walked off and died at r275. The trap-risk family is closed (TRAP_RISK on; RISK_IN_FIGHT, RISK_GATHER, RISK_FRESH, RISK_ADAPT, the wounded hold), so this is a diagnosis, not a candidate. **v0a mechanism chain dg** started (VM ~/diag26b/dg: u9p and v0a vs araid on bed, uneasy_alliance, cheesebottles and attack_on_cat, both seats). Falsifiers were appended to bots/v0a/SNAPSHOT before any game: cat scratch damage to our kings not below u9p's drops it as inert, as do fewer wins or any exception or overrun.
- 04:49: **Stash gatherers thrash between targets** (VM ~/diag26b/fw, u9p vs abrk A; per-round indicator trace of rat 13770). All six Stash mines lie in the middle band (y 8-21), and the home strips (y 0-6, 23-29) are walled off but for dirt at the corners and comb-shaped dirt columns inside. From r36 to r100 the first gatherer changed cheese target 12 times (11,16 → 19,20 → 17,21 → 12,21 → 9,21 → 12,21 → mines 9,18 and 14,18 → 17,21 → 19,18 → 17,21). Each change came when a pile beyond the wall drew nearer by straight line (sense(): any nearer pile replaces the target). Its flow toward (11,16) came ready with 21 steps at r40, the round after the switch to (19,20). It took 9 flow steps in 100 rounds and never left the strip. The 2026-11-04 debug (zdbg, replica 13264) saw the attackers' version: targets flipping between mirror candidates, paths dropped at FLOW_HOLD. CHEESE_LOS (s5t, 2026-10-29: walled piles never chased) was rejected at screen (t −1.75). **z1o = u9p + CHEESE_STICK** (new; Baby.keepTarget): the target stays while a flow toward it is searching or ready, unless the nearer pile has a clear straight line. **z1p = z1o + FLOW_PROGRESS** (existing; rejected on s4d, never on the u6x line): a step along the flow resets Nav's stall, so a detour leading away from the target no longer ends the chase at 12 turns. Falsifiers in their SNAPSHOTs before any game. Mechanism chain on VM ~/diag26b/cs (Stash, seepastyourgreed, rift and pipes vs abrk, both seats, u9p alongside): the Stash median first-middle round must be 10+ earlier than u9p's in both seats; r300 transfer above u9p's; no exception or overrun; wins not below u9p's. Then the s6 screen.
- 05:00: **v0a DROPPED** by registered falsifier (dg chain, VM ~/diag26b/dg; scratch dgcheck.py). Cat scratch on our kings: v0a 2020, u9p 1790; wins 7 and 7 of 8; no exceptions or overruns. v0a's cheesebottles A ran to r2000 while u9p's ended at r612, so its exposure was longer, but the registered rule drops it. zd66 is out of the screen queue. **Enemy trap deaths near our king, in context** (scratch traplay.py and trapctx.py over the z0q, twin and z0g replica rows; r1-500). Enemy traps are laid mostly 3-7 (Chebyshev) from our king's centre: Finals 3:120 4:208 5:195 6:171 7:111; Varun 4:123 5:164 6:149. Our trap deaths at 3-6 from our king: Finals 232 in 36 games, Varun 137, Icarus 81. Of the 232 Finals deaths:
  - 78% were rats that never carried cheese, 54% were under 30 rounds old, 53% were walking outward from the king, and 136 came in r1-100;
  - 85% had 4+ enemy babies and 68% had 7+ of ours within dist2 20.
  So these deaths come from the opening brawl at our king against a rush: fresh fighters walk out into the rusher's trap field. Against Finals they are as common in wins as in losses (6.6 and 6.3 a game). Against Varun they are not (7.5 a loss, 1.7 a win), but losing may be what puts Varun at our king. The defensive trap family (KING_TRAPS t0c, MOAT u2b, the reactive baby trap set u3d/u4m/u5o) and the movement family (RISK_FRESH, RISK_ADAPT, FACE_HOLD, WOUNDED_HP) are all closed. This is a diagnosis, not a candidate.
- 05:04: **z0q REJECTED** at replica block 3 (set 2, 17612-17649). Block 3 was 48 cells, D 10 (+4 −6), net −2, t −0.63. Pooled over blocks 1-3: 144 cells, D 26 (+15 −11), net +4, t 0.78, against the block-3 bar of 1.5. Rows are in VM ~/diag26b/z0qb3.jsonl. Block 3 changes: lost Finals attack_on_cat A, Varun clockwisdom B and ratsareants A/B, nzjt knifefight A and spsquared jail A; gained Xzibit canyoudig B, YokoOno streetsofnewyork B, spsquared evileye B and jail B. **z1l passed screen zd127** (vs u9p's zd61): 44 shared cells, D 3, net −1, t −0.57; picks/100 33.36 vs 34.30, z −0.39; transfer per round 0.991; no overruns or exceptions. It lost evileye B vs abrk and arrows A vs araid, and gained starvation A vs araid. **z1l is on the replica** as submission 237 (u9p + SWAP_LOADED 30, SWAP_HOME 10). Set-3 block 1 was sent after match 17649 (build/block_v4s3.sh; the hourly limit leaves the rest to the drain loop). The waiter is build/wait_z1lb1.sh. Score with `paired 13248-13291 IDS --crown 12130-12163`.
- 05:06: **DefaultMedium is 0 of 10 on the replica** (Varun 6, waxmango 4; z0q, twin and z0g rows). Every loss is a king kill by r583, mostly r198-388. In 7 of the 10, a second king merged in r100-199 during the war, and the army fell from ~20 to 7 within 40 rounds (scratch race.py: 17571 has n 20 at r100 and 7 at r140; 12787 dropped 23 to 10 in r80-120 without a merge). The 23:30 formscan found the same: after a war opened before r100, games with an early-war formation won 6 of 55. **z0k (u9p + FORM_WAR_FROM 300) reopened.** zd103 rejected it on the s6 picks gate, which divides pickups by rat-turns, and that denominator is biased for a formation switch. z0k formed 22 kings to u9p's 31, so it kept 7.5% more rat-turns, yet:
  - pickups 18,326 vs 17,696, pickupAmt 422,468 vs 406,978, transfer 334,820 vs 321,602, kills 2,127 vs 1,956, wins 38 = 38;
  - per game round (scratch ppr.py, per-cell z of pickupAmt/rounds): z +0.33, ratio 0.987; transfer per round ratio 0.990.
  falsifiers_v2 are in bots/z0k/SNAPSHOT before any replica game: the block rules, the transfer floor, at least the twin's wins + 1 in the block games where the twin formed at war before r300, and no z0k formation at war before r300.
  **The per-round check on other screens** (ppr.py vs zd61): z0q z −2.80, ratio 0.963 (it passed the per-rat-turn gate at −1.58 and failed the replica); z1l z −1.26 (0.977); z0r +0.71 (0.978); z0d −1.76 (0.930); z0e −2.63 (0.969). **Method change:** from now on the s6 screen also rejects at per-round picks z ≤ −2, and a switch that changes the number of formations is judged per round only. Replica order after z1l: z0k, z0r. z0e is demoted (it would fail per round) and z0d is held.
- 05:11: **Screens re-read under the paired gates** (scratch sweep.py: every runs/zd*/results.jsonl vs u9p's zd61, with per-cell z of picks per rat-turn and per round, and transfer per round). Several rejections came from pooled or absolute picks bars, not paired tests:
  - **u7z** (u6x + SEEK_IDLE 20 + IDLE_SHIFT + FIGHT_SHIFT; zd24, 2026-11-04). Its registered bar was pooled picks/100 rat-turns 37.3 < u7x's 39.1, but the per-cell z vs u7x is −0.15. Per round it is z +0.93 vs u7x, and vs the incumbent u6x z +2.53 (ratio 1.051), with transfer per round 1.040. Its screen vs u6x's zc6 is the strongest of the season: D 9, net +7, t +2.46, 39 of 44 wins. **Revived** for the replica (falsifiers_v2 in its SNAPSHOT).
  - **u9t** (u9p + THROW_ADAPT + SEEK_CROWD 3; zd65): the absolute bar was 33.8 and it scored 33.64; per-cell z −0.28, per round z −0.24, transfer 1.012, t −0.44. **Revived.** u9s (SEEK_CROWD 3 alone) passes too, but u9t carries it.
  - u9r (THROW_ADAPT alone) stays rejected: per round z −1.93, ratio 0.964.
  **z1q = u9p + SEEK_IDLE 20 + IDLE_SHIFT + FIGHT_SHIFT** (u7z's switches on the u9p line; FIGHT_SHIFT's Baby hunk patched strictly). Its falsifiers are in its SNAPSHOT, including inert below a per-round pickupAmt ratio of 1.01. Screen **zd128** is at the front of the VM queue. **Replica order:** z1l (on), z0k, u7z, u9t, z0r.
- 05:12: **z1o and z1p DROPPED** by registered falsifier (1) (cs chain, VM ~/diag26b/cs; Stash vs abrk; scratch firstexit.py, median first round in the middle band for our first 8 rats, A/B). u9p 176/141.5, z1o 144.5/148, z1p 126.5/136. The bar was 10+ rounds earlier than u9p in both seats. Seat A cleared it for both (−31.5, −49.5); seat B missed it for both (+6.5, −5.5). The chain was stopped after rift (pids 1962718, 1996991-3). **Mechanism chain fk** started (VM ~/diag26b/fk): u9p and z0k vs araid and abrk on DefaultMedium, both seats. In the zd103 screen, z0k lost araid DefaultMedium B at r1168 where u9p won at r288. These games show what the war-formation hold does on the map where the replica went 0 of 10.
- 05:15: **Rich points losses: our army stops at the cap** (replica 17551, z0q, superioristhmus vs spsquared A, lost 48-51 on points with kings 3-3; rounds.tsv). From r400 our babies sat at 38-40 (the RICH_BABIES cap) while theirs grew to 50-52. Our bank held 4.2-8.1k from r800, their 2.5-4k. Map cheese rose from 5.4k to 10.2k uncollected. Their transfer over r1000-2000 was 25.5k to our 14.5k. Every superioristhmus vs spsquared game in the recent rows is a points loss (0 of 6), each with 4.2-11.8k banked. War points are 0.3 cat share + 0.5 king share + 0.2 transfer share (GameWorld.setWinnerIfMorePoints), and the bank counts for nothing. u8d (u6x + RICH2_BABIES 80 at bank 3000) was rejected at replica block 1 only because set 3 has no rich game: all 48 cells replayed, t 0.00. Its local screen (zd28 vs u6x) was 33 to 32 wins, per-round picks z +2.27, transfer per round 1.030. Its per-rat-turn picks z −2.25 is the denominator effect again. **z1r = u9p + RICH2_BABIES 80**, screen **zd129** queued after zd128. Falsifiers are in its SNAPSHOT: the replica block order is sets 1, 2, 3, 4, and it is inert unless it out-transfers the twin in most block games where the twin banked 3000+.
- 05:17: **fk chain done** (VM ~/diag26b/fk, DefaultMedium vs araid and abrk, both seats, seed 0): u9p 2 of 4, z0k 2 of 4. The mechanism shows in abrk A: u9p merged a second king at r110 during the war, its babies fell from 18 to 6 by r350, and it lost at r475. z0k did not merge, held 19-20 babies, and won at r248. In araid A both lost (u9p merged at r113 and lost at r375; z0k lost at r314). In araid B, u9p merged at r142 and won at r288, while z0k kept 20 babies to araid's 8-9, formed at r312 and lost at r1168. Seat B vs abrk was a r55 win for both. This was diagnostic only; z0k's replica falsifiers are unchanged. **wallsofparadis vs Finals is a crowding loss** (replica 17504 g1, z0q as A; scratch snapat.py ASCII map at r300). 17 of our 20 babies stood in the bottom strip (y 0-6) around the home mine (30,3). Our other home mines, (55,5) and (38,22) inside the walled central box, had nobody. Finals' carriers worked the central box (27-41, 27-47). Over r300-700 our transfer was 635 to 1,270 and theirs 1,405 to 6,790, with 1.5-3.1k cheese on the map. Our bank stayed 900-980, under RICH_BANK 1000, so the cap stayed at 20. z1q (SEEK_IDLE + IDLE_SHIFT + FIGHT_SHIFT: idle rats move along the mine list) is aimed at exactly this; its screen zd128 is next.
- 05:21: **keepout vs Finals is attrition, not crowding** (replica 17504 g0, z0q as A, lost r1786 by king kills). They grabbed first at r22 (bsCause grab), and the war never stopped. We lost 136 rats to the enemy and 23 to the cat. Transfer was 8,922 to 34,780 and final banks 75 to 1,795. Their second king came at r315, ours at r655. Our rats counted on snapat.py maps: r100 18 (17 out of the home room), r200 11 (all 11 inside the bottom-right room x 35-51, y 0-15), r300 5. Finals had 16-19 rats every 100 rounds, spread over the whole board, most outside their own room. Unlike wallsofparadis, our rats are not standing around a mine. They die in the open and the rest fall back into the room. z1q will not touch this cell. The keepout chain (wp chain: wallsofparadis, jail, keepout, mercifullattice vs araid and abrk, u9p vs z1q, both seats) is on the VM to check that z1q changes nothing here.
- 05:23: **keepout attrition is Finals' rat traps on our mines** (17504 g0 dumped with game_report.dump). Of our 161 rat deaths, 60 were traps, 38 bites, 39 thrown and 23 cat. Finals laid 202 rat traps all game, 10-21 every 100 rounds. 167 of them were on our half, and 143 sat within Chebyshev 5 of a mine. Our rats fired 193 of the 202 (9,650 damage), spread over r0-1800, mostly round our home mines (37,3), (46,3) and (46,13). Every counter-trap doctrine is already closed on the replica: MINE_TRAPS (s6f, u1p, u1t), RAID_TRAPS (s8j), KING_TRAPS (t0c), the reactive baby traps (u0q, u4m, u5o, u5u) and TRAP_RISK with RISK_ADAPT, WOUNDED_HP and FACE_HOLD. The cell is 0 of 32 across builds, so it is not worth a candidate of its own.
- 05:30: **z1e dropped as inert.** Screen zd120 vs u9p zd61: 44 cells, D 0, net 0, t 0.00. kingsFormed was 30 to u9p's 31, and its registered bar was above 31. Picks/100 per-cell z −1.22, transfer x1.00, no overruns or exceptions.
- 05:30: **rift vs spsquared, seat A, is a peace-time grab** (16422 g1 against the crown twin's 13248 g1, both dumped). The u9p line loses this cell in all 6 games: spsquared opens the war at r494 (r539 for z0g), then out-damages the cats 8,770 to 4,330 (347 cat bites) and wins 59-40. u7p saw no war and won 54-45 on cat damage 5,640 to 4,000. At r493 their 80-hp rat stood 2 tiles in front of our 100-hp rat, which carried 713 cheese and faced NW. At r494 our rat stepped to (30,16), next to it, and their rat moved to (31,16), our blind side, and grabbed (engine: a grab needs the target unable to see the grabber, or weaker by more than the threshold). u9p turns to face enemies only at war (guardTurn and faceEnemy return in cooperation). Field rows (588 games, deduped): the opponents opened 395 wars, 158 of them with grabs, and we won 64 of those; we won 42 of the 51 games with no war. Late grabs (r300+) by spsquared, Icarus and Finals recur on rift, rose and fullofsecrets. **New candidate z1s** = u9p + COOP_GUARD: at peace, a non-rusher baby does not step next to a visible enemy baby and ends its turn facing the nearest one within Chebyshev 2. The local screen opponents all open the war before r140, so screen zd130 (queued after zd129) tests only the cost. The replica mechanism check is the count of grab-opened wars against the reference rows.
- 05:34: **Wars the opponent opens with a trap before r100 start with our first babies** (scratch trapopen.py on the VM, 25 recent replica cells, one game each). These games number 85 in the field rows, and we won 23. The firing rat was our 1st-4th baby (spawn index 0-3) in 20 of 25, a rusher (4-15) in 3 and a later baby in 2. The trap sat mid-map, Chebyshev 14-25 from their king and 7-30 from ours, fired r14-52. Most were AdamTan12 (Finals, Icarus) and Varun cells: DefaultSmall, DefaultMedium, keepout, clouds, mercifullattice, uneasy_alliance, wallsofparadis. These are the opening gatherers walking out to find mines. u8p (NOMINE_HOME: a mineless gatherer scouts our half, not the centre) was the nearest test, and it was rejected at replica block 2 (pooled net +1, t 0.22). No new candidate from this.
- 05:36: **z1s smoke games** (VM ~/diag26b/cg, z1s as A vs u9p, seed 0): rift won 2000 on points, fullofsecrets lost r637, rose lost r913. No exceptions or overruns. z1s itself opened the war by grab on rift (r587) and rose (r584). u9p's babies grab during cooperation from r30 (BREAK_ROUND 30), and facing an enemy also shows us its blind side. So z1s's SNAPSHOT now also reports wars we open by grab. This is not a gate; it was added before any screen or replica game.
- 05:36: **z1t = u9p + END_CAT 1650 + END_CAT_IDLE 20** (z0c's endgame cat switch with a gatherer gate). From r1650 at war, idle babies go for the cats. A gatherer joins only after 20 turns within Chebyshev 4 of its mine with no pickup, and then hunts to the end. z0c (every role) won replica cells (pooled 96 cells D 19, net +5, t 1.15, cat share 0.658 to 0.584) and failed only on the set-1 transfer floor (0.86 of the twin's per round). Every one of the 44 replica points losses was at war, most with our cat share at 0.06-0.35. Screen zd131 is queued after zd130. The new replica gate: transfer per round below 0.95x the twin's rejects.
