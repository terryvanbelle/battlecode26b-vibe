# TRAINING_ALGORITHM.md

How this project turns compute into a stronger bot. Year-agnostic: no unit, rule or tool of a particular season is
named here (`HANDOFF.md` maps each step onto this repository). Distilled from eight practice seasons
(`research/prior/DIGEST.md`); where seasons disagreed, the later one and the measured result win.

## 0. The objective and the instruments

**Objective: absolute strength against an unseen field**, as a contest ladder would measure it. Beating our own past
builds is a tool, never the goal; the field reverses self-play verdicts often enough that self-play may reject but
never accept.

| instrument | answers | blind to |
|---|---|---|
| rules digest, engine-verified, every fact cited to source | what is possible and what it costs | what wins |
| replay reader + the bot's own decision counters | why one game went as it did; did a mechanism fire | frequency, value |
| paired local games (candidate vs incumbent on identical cells) | regressions, crashes, cost of a change | what both builds share; the field |
| sparring archetypes (our copies of field styles) | survival against one known style | everything else |
| frozen roster (every accepted build) | is the lineage walking downhill (non-transitivity) | the field |
| ladder replica (field bots, contest rules) | strength; what the field punishes | why |

When two instruments disagree, that disagreement is the finding: consult a third and report it; never pick the
convenient one.

## 1. Foundation first

No strategy work until each item exists and has passed a test whose answer is known.

1. **Rules digest** from the engine source. Play the starter bot, read how the game ends, tally how games are decided.
2. **Map corpus census**: size, symmetry, spawn geometry, resources, hazards. Facts true on every map are constants.
3. **Runner**: headless, parallel, bounded wall clock, bot code without network, a private compiled tree per run,
   one result row per game keyed by (code hash A, code hash B, map, side, seed) with winner, reason, rounds,
   graded score terms, exceptions, compute overruns. Measure the machine's concurrency knee once.
4. **Determinism**: identical inputs give identical games; every residual randomness listed. Provide a seed axis if
   the engine lacks one. **Identity control** on every verdict path: a byte-identical copy reads zero discordant cells.
5. **Replay reader**: per-round team aggregates, one robot's life, the board at a round, an event window, our own log
   lines, compute per unit type, navigation statistics, a census row per team per game. Fixture tests pin its numbers
   to the engine's own aggregates.
6. **Bot skeleton**: layered (turn loop, globals, constants, comms schema, map memory, navigation, one handler per
   role); every behaviour behind a switch whose default is the incumbent; decision counters with greppable tags;
   a compute monitor (overruns, near misses, caught exceptions) from the first commit; tie-breaks from the unit's own
   geometry, never compass order, sensing order, team or identity.
7. **Field**: discovered and fetched by script, security-scanned, compiled blind, never read; smoke-tested.
8. **Ladder replica**: the contest's own site, played as a contestant through its own interface; field bots play each
   other in spare capacity.
9. **One command** regenerates charts, ratings and the handoff, failing loudly; one command runs every unit test
   (bot logic and every tool) and runs after every change.
10. **Basics battery**, absolute bars, run on every block:
    - economy: no idle producer or gatherer, no resource floating beyond a stated band, no starved producer;
    - movement: no unit stalled N turns, no oscillation, no unreachable target held;
    - exploration and symmetry: decided exactly as soon as observations allow, never wrong;
    - combat: focus fire, no fight begun at a local disadvantage, no unforced deaths;
    - zero compute overruns, zero exceptions;
    - play symmetry: the build against its own copy splits evenly on every map from both sides.

**Foundation bar**: beats the starter bot from both sides on every map, passes the battery, is on the ladder with a
calibration block. Then one correctness audit (§6.1) before the loop starts.

## 2. The loop

1. **Target**: take the next source from §3.
2. **Trace** the motivating game with the reader; confirm the symptom in a second game against a different opponent;
   count how often the trigger occurs across recent games; list the mechanisms that could cause it before picking one.
3. **Pre-register** in the log: mechanism, the counter that shows it fires, that its branch is reachable at observed
   values with a choice set larger than one, expected firing rate, what it displaces, deciding instrument and gate,
   falsifier, and for a number a dose ladder with a zero arm.
4. **Implement behind a switch**. Tests pass. With the switch at its default the build plays identically to the
   incumbent (checked by replay comparison); with it on, the arm differs exactly where intended.
5. **Delivery**: one logged game where the mechanism can fire; then a small paired block whose counters show it fires
   in most games and moves its signature. An arm that does not deliver never reaches a gate.
6. **Local screen** (rejects only): paired cells against the incumbent, the archetypes and the roster; reject on a
   clearly negative paired statistic, on a new exception or overrun, or on a basics failure.
7. **Ladder panel** (accepts): unranked, fresh cells, field bots near our strength, every cell from both sides,
   scored against the champion's own games on the same cells (§4).
8. **Accept**: freeze a snapshot, make it the active submission, play the roster, refresh progress, commit and push
   in the same hour, mine the new games for the next target.
9. **Reject**: restore the incumbent exactly; record the closing number, the kind of closure (refuted, below
   resolution, blocked, inert) and a checkable reopening condition.

Positive near-misses with independent mechanisms may be stacked and tested as a new arm; never stack the merely
not-worse.

## 3. Where candidates come from

Rotate so none dries up, and keep a deliberate mix of **small repairs and big swings**.

1. **Own degeneracies** (highest yield, no opponent needed): idle or latched units, floating resources, starved
   producers, oscillation, overruns, exceptions, avoidable deaths, a counter that never fires.
2. **The field**: how bots just above us win; contrast behaviour rates of bots that beat us with bots we beat (keep
   rates where we differ from the first group and match the second); compare action *patterns in context*, not counts;
   copy a whole style that beats us, then build the defence against our own copy.
3. **Capability gaps**: every engine action and rule we never use; every free action, idle cooldown or spare compute
   gets a job.
4. **Ablation** of what we carry, periodically and after any lineage change.
5. **Structural swings**: a different economy, opening, doctrine or architecture, designed on paper from census
   numbers, with a stage budget and a kill criterion; a full rewrite that keeps the proven plumbing is allowed.

Balance: at least one structural attempt in every four; three consecutive rejects in one functional area move the
next attempt elsewhere. Repairs and cap removals transfer to the field; reallocations between unit types and
constant ladders rarely do — price them accordingly.

## 4. Evidence

- The unit of evidence is the **paired cell** (opponent, map, side, seed); identical outcomes carry no information.
  With per-cell differences d (graded result where the game has one, else win/loss), t = Σd / √Σd², D = cells
  with d ≠ 0.
- **A repeated cell is not new evidence**: deterministic or lopsided cells repeat; every block draws fresh cells.
- **Champion reference**: a new champion plays its own control block on each cell set before any candidate is scored
  there; never compare with another build's games or the champion's history.
- **Decision rule fixed before the block** and simulated for false-accept rate and for power at the effect sizes we
  actually produce (one to two cells in twenty). A rule that cannot pass a real gain is a defect, like one that
  passes noise. Never read a running block; never move a bar after seeing a number; no rule applies backwards.
- **Held-out check**: cells a candidate was designed from are listed; it must also be non-negative on the others.
- **Graded objective**: where the season scores margins, test the per-cell difference in that score alongside wins.
- **Ratings** (batch Bradley-Terry over all ladder games, each build its own player, repeated cells counted once, a
  cap per pair) watch the field and report standing with an interval; they never accept a candidate.
- A single game is a filter, never a verdict. Read the shape of a difference: one-directional on one map or side is
  real; scattered and mixed is noise. First looks regress toward zero.

## 5. Using the ladder like a contestant

- Only validated builds play ranked games; candidates play short unranked panels and are parked as soon as their
  matches exist (a match keeps the build that was active when it was created).
- Panel opponents: the band around our strength, especially bots that recently beat or split with us. Maps drawn at
  random per opponent and kept for that block; never chosen for advantage; both sides always.
- Version the panel when it saturates (an opponent at 0-10% or 90-100%); mark every version on the charts.
- Every downloaded match becomes a short report (both teams' tactics, the turning point) and a census row. The
  ladder is asynchronous: local work never waits for it.

## 6. When nothing is accepted

Escalate in order; skip nothing.
1. **Correctness audit** of bot and instruments through independent lenses (dead code and unreachable branches; rules
   and API, including what the engine gives away; shared state and staleness; behaviour seen in replays; the
   measurement pipeline), each finding with evidence, a fix, a regression test and an adversarial reviewer. Prove the
   instruments with an identity control first. One broken basic is a sample of a class.
2. Ablate everything carried.
3. Sweep the engine and API for unused mechanics.
4. Re-read field games for what we were not looking for; build the archetype that beats us.
5. Re-read the cross-year principles and `LEARNINGS.md`.
6. Look for a jointly necessary pair among near-misses.
7. A structural swing, then a rewrite. A rejected swing is an ordinary outcome; only an arithmetic or engine ceiling
   licenses abandoning a line.

## 7. Operations

- **Never idle**: the compute machine always holds at least three queued jobs plus an idle filler; a watcher wakes the
  session on finished work, a stuck component, low disk, or a heartbeat (under an hour), re-armed every turn. Never
  stop to wait for an idea or an approval.
- **Rules live in tools that refuse**: no paired read on mismatched cells, no score of an unfinished block, no result
  row without code-hash provenance, the screen prints its own verdict.
- **Process hygiene**: one semaphore for every path that starts games; private build trees; kill by process id, never
  by a pattern that can match the caller; never edit a running script; disk checked before every block.
- **Records**: append-only training log (one entry per attempt, with run ids); closed-directions ledger;
  `LEARNINGS.md` (each lesson with its measurement, added the day it is confirmed); attempts table; `HANDOFF.md` as a
  closure map; charts that never go stale. Record measurements, not stories.
- **Owner**: every prompt verbatim in `PROMPTS.md`; push after every commit; times in the owner's zone from the clock;
  a short owner question is an audit trigger, answered from data.
- Kill ideas cheaply, in this order: engine read, census of games on disk, one-map probe, small paired block, local
  screen, ladder panel.

## 8. Defaults

| parameter | default |
|---|---|
| near-miss compute bar | 90% of the per-turn limit |
| delivery block | 24 paired cells, extended to 48 |
| local screen | 40 paired cells, both sides; reject at t ≤ −1.5, a new exception or overrun, a basics failure |
| ladder panel block | 6-8 field bots near our strength × 3 fresh maps × both sides |
| panel rule | block 1 rejects at t ≤ 0; any block accepts at t ≥ 2 with D ≥ 4; block 2 rejects at t ≤ 1, block 3 at t ≤ 1.5; repair of a basics failure may pass at t ≥ 0 after two blocks |
| rating pair cap | 200 games |
| area switch | 3 consecutive rejects |
| structural share | ≥ 1 in 4 attempts |
| concurrency | measured knee (≈ cores − 1); one diagnostic game at a time on the session machine |
| watcher heartbeat | ≤ 50 minutes |
