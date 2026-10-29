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

## 2026-10-29 00:10 PDT: s4j rejected on the replica after block 1
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

## 2026-10-29 00:00 PDT: starvation is mostly lost income; stuck babies are a navigation bug
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
