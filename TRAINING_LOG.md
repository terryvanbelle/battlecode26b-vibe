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
