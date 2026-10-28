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
