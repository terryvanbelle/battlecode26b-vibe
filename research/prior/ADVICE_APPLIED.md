# Cross-year guides applied to this ruleset

Source: the filtered cross-year guides (`readroom-no2026/advice/`: ADVICE, ECONOMY, NAVIGATION, EXPLORATION, COMBAT,
SYMMETRY). Mapping onto this year's rules is inference until verified against the engine (RULES.md).

## Economy
- King upkeep is committed spending: keep a reserve `kings * 2 * horizon` that bites, digs and traps never touch.
- Spawn cost `10 + 10*floor(babies/4)` rises with live population: compute payback (delivered cheese per rat-round)
  against rounds left / expected life; it may invert into a free census of live babies.
- Merging (7 rats + 50 cheese -> king) buys a producer, resilience, and the living-kings score share, for 2/round.
  Model 1/2/3 kings offline before trying it.
- Mines regrow with time since last spawn: one rat per mine on rotation beats a swarm. Ghost cheese: remember mine
  locations (static), treat cheese as transient, delete lazily.
- Never float: committed reserve vs discretionary; sink order spawn (if payback) > score sinks > merge.
- Spawn ring (16 tiles around a 3x3 king) is infrastructure: no parking there; leave delivery gaps.

## Navigation
- Facing is navigation: forward 10, strafe 18, turn 10 (separate cooldowns?) — verify; make `stepToward` facing-aware.
- Greedy + bug escape after a measured stuck condition; per-robot handedness from own geometry; stall counter must
  survive retargeting; dig dirt when the detour is long and cheese is above reserve.
- Treat cat reach as a soft hazard; stuck detection by position history; never target the unreachable.
- Local unrolled BFS only after nav statistics show a gap.

## Exploration and comms
- 90-degree cone: exploration and facing are one decision. Random far target re-picked on arrival is a strong default.
- One int per tile map memory; per-facing cone offset tables; incremental sensing.
- Only kings write the 64x10-bit array; babies squeak (radius^2 16, 5 rounds) — hub and spoke: report near kings.
  Squeaks attract cats in attack mode: a priced action.
- Sector encodings to fit 10 bits; one writer per slot; staleness rule; adoption when a king dies.

## Combat
- Melee (adjacent bites): decide the duel regime from the engine (move-then-bite? who acts first?).
- Facing gives an exploitable gap: approach from outside the target's cone; ratnap rules depend on facing/HP.
- 9-move micro kernel scored lexicographically: not cat-lethal > can bite > less danger > unseen > support > progress.
- Every rat-on-rat attack, ratnap or trap trigger is a backstab trigger: one guard function, explicit policy.
- Against cats damage dealt is the score: maximise damage per cheese and per rat-life; cat traps (100 dmg) are
  likely the cheapest cat damage; place on waypoint paths.
- Losing all kings loses the game: the king is the centre of gravity; burying an enemy king's spawn ring with dirt.

## Symmetry
- ROT / HREF / VREF only; define by formula. Initial king pair may decide it at round 1; else eliminate by static
  features (walls, mines, initial dirt before any dig). Babies squeak eliminations to kings, kings publish.
- Uses: enemy king location, mirrored mines, map fill-in, cat waypoint mirroring.

## Failure modes to pre-empt
Babies writing the shared array (throws); king starvation spiral; blocked spawn/delivery ring; side bias from facing
and direction order; hidden strafe penalty; accidental backstab; squeak spam drawing cats; kiting logic against
unkillable scored cats; ghost cheese; stall counters reset by retargeting; first-turn bytecode spikes; cumulative
counters as census; merges that never fire; untimed emergency modes; mirror-only evaluation of defences.
