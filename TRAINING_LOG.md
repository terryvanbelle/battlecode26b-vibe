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
