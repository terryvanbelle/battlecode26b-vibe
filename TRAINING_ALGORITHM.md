# TRAINING_ALGORITHM.md

How this project converts compute into playing strength. Written for any season: no unit, resource, score, map or tool
of a particular year appears here; `HANDOFF.md` maps each step onto this year's tools, numbers and paths. Each rule was
paid for by a season that lacked it; where sources conflict, the later season and the measured result win.

## 0. The objective and the instruments

**Objective: absolute strength against an unseen field**, as the contest ladder measures it. Beating past builds is a
means, never the goal: local wins often exploit the incumbent's own habits, and the field reverses them often enough
that self-play may reject a change but never accept one. When instruments disagree, that is the finding.

**Terms.** The *incumbent* is the last accepted build: the active submission and every candidate's baseline. A *cell* is
an (opponent, map, side, seed) slot both play (ladder cells have no seed). Per cell, d = the candidate's mean cell score
(§4) minus the incumbent's, each cell counted once; D counts cells with d ≠ 0; t = Σd / √Σd² (win/loss: net / √D). A
*block* is cells scored once, after the last game; the incumbent's on the same cells is the *control block*.

| instrument | answers | blind to |
|---|---|---|
| rules digest (engine-checked, each fact cited to file and line) | what is possible; what each action costs | what wins |
| replay reader + the bot's decision counters | how one game unfolded; whether a mechanism fired | frequency, value |
| paired self-play (vs the incumbent on seeded cells, identity-controlled) | regressions, crashes, a change's cost | what both builds share; changes meant for the field |
| local field games (downloaded field bots run here on seeded cells) | the ladder's opponents, cheaply, replays in hand | contest conditions (submission, map draw, current versions) |
| sparring archetypes (our re-implementations of field styles, each checked against the bot it models) | survival against one known style | everything else |
| frozen roster (every accepted build) | does each build still beat its ancestors (non-transitivity) | the field |
| ladder replica (downloaded field bots, contest rules, batch ratings) | strength; what the field punishes | why |

## 1. Foundation first

No strategy work until every item exists and has passed a test with a known answer; every past instrument failed on
first use, and a strategy tried on a broken base measures the defect.

1. **Rules digest** from engine source, not spec: facts cited to file and line, spec/engine disagreements listed, each
   action's legality checks and cost order (hidden limits and free actions live there), the order of execution within a
   round. Tally how starter-versus-starter games end (win condition or tiebreak): the graded score must track that.
2. **Map corpus**: every map (generated ones if few) with size, symmetry, spawn geometry, resources, hazards and key
   distances; what holds on every map is a constant, never sensed.
3. **Runner**: headless, parallel, wall-clock bounded, no network for bots, each game checked to have run as configured
   (bad settings fall back silently). One row per game keyed by both code hashes, map, side and seed, labelled from what
   played: winner, ending, rounds, graded scores, exceptions, overruns. Measure the concurrency knee and per-map cost.
4. **Determinism**: every randomness source controlled, an explicit seed per game (add a seed axis if missing), residual
   noise stated as the floor. **Identity control**: each verdict path scores the incumbent renamed (an *identity copy*)
   at D = 0 or the floor, again after a tool change on it or a streak of rejects (§8).
5. **Replay reader**: per-round team aggregates, event windows, unit lives, our logs; compute maxima, first-turn cost,
   overruns per unit type; navigation stats; losses by cause, place and attacker; the first round teams stop mirroring;
   a census row per team-game. Fixtures pin it to the engine; new counts are reconciled with independent ones.
6. **Bot architecture**, layered (turn loop, globals, one constants file, communication schema, map memory, navigation,
   a handler per role), each behaviour behind a switch defaulting to the incumbent's, with greppable decision tags per
   stage. **Moves stay symmetric when sides switch**: per-unit random streams seeded from side-invariant values; ties
   broken relative to the unit's own surroundings, never by fixed direction order, sensor order, id or team. From the
   first commit, a compute monitor counting overruns, near-limit turns and caught exceptions by stage.
7. **Snapshot tool**: each build frozen under its own name before its first gated game and kept as an opponent with a
   one-line purpose; a check that the working bot, switches at default, repeats the incumbent's actions on fixed cells.
8. **Field bots**: found, downloaded, security-scanned and compiled by script, source unread; one smoke game each.
9. **Private replica of the contest ladder**, isolated so nothing (accounts, uploads, requests, load) reaches the
   official contest, played **as a contestant** through its API: submit, scrimmage field bots, fetch results and
   replays. In spare capacity field bots challenge each other so ratings settle.
10. **Tooling**: one command regenerates ratings, charts, the attempts table and the handoff's state block, failing
    loudly. Each process change (agent model or effort, method, gate, panel version) is a dated milestone on every
    time-axis chart. One command runs comprehensive unit tests (bot logic, every tool) after every change; each change
    updates its tests; optimisations are pinned to slow references; each check fails once before it is trusted.
11. **Basics battery**: absolute bars as scheduled jobs with stored rows, run on every block and on the incumbent as
    submitted; while a row fails, candidates come from it first.
    - economy: no idle producer or gatherer, no stockpile above a stated cap, no producer starved or sealed in;
    - movement without getting stuck: no unit idle, stalled N turns or latched in one state without a logged reason; no
      oscillation or unreachable target; every multi-turn computation capped with a fallback;
    - exploration and map symmetry: enemy start positions and fixed structures known exactly once observations allow; no
      candidate symmetry dropped unrefuted; shared facts timed to the units that act on them, rare reports first;
    - combat: focused fire, no friendly fire, no fight begun at a local disadvantage, no unforced death or idle contact;
    - **zero compute overruns and exceptions** on each unit's worst and first turn, on the largest map and in crowded
      fights, worst turns within the headroom bar (§8). Against a deterministic compute count every overrun is a defect
      wherever it occurs; against a wall clock the bar binds the incumbent under contest-like load;
    - play symmetry: self-mirror games on symmetric maps stay mirror images until the engine's execution order first
      intervenes (that round reported per map), and a unit test expects each decision to mirror in a mirrored world (a
      win split proves nothing). Fix an asymmetry by giving both sides the better side's behaviour.

**Foundation bar**: from either side on every map the build beats the starter, except losses traced to causes outside
the bot's control (a battery row that must not grow); it clears the battery; it is on the replica, calibrated against
the whole field the day it is submitted. A build with no defence can sweep the starter, so the field is the real
calibration. That build is the first incumbent. Then one correctness audit (§6.1) before the loop starts.

## 2. The loop

Each idea passes a **funnel**, cheapest stage first, dying at the first that refutes it: engine read, a count in games
on disk (our random tie-breaks are free experiments), one-map probe, delivery block, local screen, ladder panel.

1. **Target**: the next source in §3, failing battery rows first.
2. **Trace** the motivating game with the reader, confirm the symptom against a second opponent, count its trigger in
   recent games (one replay is an outlier until counted), check we trail on that term, list mechanisms.
3. **Pre-register** before coding, in the training log (else noise-level changes stay in): mechanism and what it
   displaces; the counter showing it fires, and its expected rate; the changed branch reachable at observed values, with
   more than one outcome; deciding instrument, bar and smallest detectable effect (if above the expected effect, do not
   run it); falsifier; for a number, a dose series with a zero arm that plays like the incumbent.
4. **One change per candidate, behind a switch**, so a verdict has one cause. Tests pass; at its default the build
   replays the incumbent, switched on it differs exactly where intended. Freeze it as its own build before its first
   gated game, so verdict and code hash belong to it.
5. **Delivery**: one logged game where the mechanism can act, diffed against the incumbent's game on that seed; then a
   short paired block whose counters, printed beside the screen's verdict, show it firing in most games and changing the
   end result its chain aims at. An arm that does not deliver is never gated and closes as inert. Delivery proves
   behaviour changed, not that it pays: component metrics have risen while wins fell.
6. **Local screen** (rejects only): head-to-head with the incumbent on random (map, seed) cells from both sides (d = the
   candidate's mean minus an even share), plus archetypes, frozen roster and local field games. Reject at paired t ≤
   −1.5 on the cell score (if graded, also at a one-sided sign test p < 0.05 on per-cell win/loss), at a new exception,
   at a basics failure or battery rate worse than the incumbent's beyond noise, or, on a deterministic compute count, at
   any new overrun. Simulate the joint false-reject rate at the D in use (the t bar alone swings 3-13% with D) and hold
   it near one neutral in eight; a stricter bar costs more real gains than the panel time it saves.
7. **Ladder panel** (the only gate that accepts): an unranked block on fresh cells against the panel (§5), each cell
   from both sides (moving first can outweigh the change), scored by §4 against the control block. If a match plays
   whatever build is active at creation, the candidate is active only then; automatic play that could pair us with a
   candidate is paused, stray matches kept out of ratings.
8. **Accept**, by one script so no step lapses: promote the frozen candidate; make it the active submission; play it
   against the roster; refresh progress; archive a replay; commit and push within the hour; mine the new games. Refresh
   again at its first ladder games and after every scored block, so the progress graphs stay current.
9. **Reject**: delete the arm, or keep it behind a switch whose default provably replays the incumbent (pruned at
   audits). The closed-directions ledger records the closing number, closure kind (refuted; below resolution, |t| < 1 at
   the final block; blocked by a prerequisite; inert) and a checkable reopening condition, powered for the effect.

A *near-miss* is a delivered candidate rejected at t > 0. Near-misses with independent mechanisms can be combined into
one new arm; changes merely not worse (t ≈ 0) never are, as such combinations have read clearly negative.

## 3. Where candidates come from

Cycle through these sources, listed by past yield, so none is neglected; mix **incremental tweaks**, which repair what
is broken, with **big swings**, which escape a local optimum.

1. **Own degeneracies** (no opponent needed): battery symptoms (§1.11) even below their bars, deaths by cause, a counter
   that never fires (count units passing each precondition to see where they drop out). On the cadence (§8), play the
   whole corpus from both sides against one strong bot and sort maps by graded score: the worst show basics failures the
   panel hides. Trace each resource to who took it, where and when: a lost race looks like slow gathering in aggregate.
2. **The field**: what wins for the bots just above us. Contrast behaviour rates (patterns in context) of bots that beat
   us with bots we beat; act where we differ from the first and match the second (run field-vs-field games here if need
   be). Per opponent and map, find the first round from which a lead in a metric predicts the winner; work on that
   window. A metric that tracks the result is usually its consequence: it earns a diagnostic, never an allocation.
   Rebuild an offence that beats us from its games (never its code), enter it on the replica, build the counter.
3. **Capability gaps**: every engine action and rule we never use; every free action, idle cooldown or spare compute
   gets a job. Sweep on the cadence, not only when stuck: past sweeps always found something material.
4. **Ablation**, on the cadence and after any lineage change: once a switch's firing rate is counted, turn it off in a
   candidate against the incumbent, which also prices the branch taken instead. If the incumbent loses ground to the
   roster, ablate in pairs chosen by ancestry; some features matter only together.
5. **Structural swings**: a different economy, opening, doctrine or architecture, or a field style adopted whole (traits
   lifted singly rarely carry its strength), designed on paper from census numbers with a budget per funnel stage and a
   kill criterion. A rewrite keeping proven infrastructure is allowed whenever the architecture caps progress.

Balance: at least one attempt in four is structural; three straight rejects in a functional area (economy, production
mix, movement, exploration, communication, combat, defence, map adaptation) send the next attempt elsewhere. Repairs and
cap removals carried over to the field in every past season, reallocations between unit types and constant tuning
rarely: price a reallocation against what it displaces; give freed capacity a job. Never tune by self-play tournament.

## 4. Statistics

- **Cell score**: the graded objective where the season has one, else win/loss, fixed per panel version. A graded margin
  carries several times a win's information and, when we rarely beat the panel, is the only signal; compute it from
  replays if the site reports only wins. The contest counts wins, so each panel version checks that the graded score
  predicts them, every verdict prints the win/loss net, and a graded accept with a negative net does not accept.
- **A repeated cell is not new evidence**: deterministic or lopsided cells (most, in past seasons) repeat their result.
  Pooling repeated blocks as independent multiplies t by about √blocks while reusing the few cells that changed, passing
  neutral candidates 17-30% of the time, not the nominal 7%. Random opponents add mixed-sign cells: less power, no bias.
- **Paired fresh-cell rule.** Each panel version draws numbered map sets once (three maps per opponent, both sides),
  shared by every candidate; a candidate plays sets 1-4 in order, never one twice. The reference on a set is the
  incumbent's own games there in this version (accepting or control blocks), played once per reign before any candidate
  is scored there; never another build's, an earlier version's or the ladder's games. t pools the candidate's blocks:
  any block accepts at t ≥ 2 (which needs D ≥ 4); blocks 1, 2, 3 reject at t ≤ 0, 1, 1.5; block 4 is final and rejects
  below 2 (below resolution if |t| < 1). A basics repair (a battery row turned to pass) instead accepts at t ≥ 0 after
  block 2, its false accepts simulated apart. A real but narrow gain usually closes below resolution, not refuted (§8).
- **Simulate every bar before relying on it**, results in `HANDOFF.md`: the screen's false-reject rate and how well
  screen t predicts panel t; each panel design's false-accept rate (≈ 5%) and power at the effects we actually produce
  (one to two cells in twenty), across shares of cells changed and opponent noise. A rule that cannot pass a real gain
  is as defective as one that passes noise. Each pre-gate gets a null arm, a best-of-several statistic a simulated null.
- **Narrow switches need targeted cells**, not more blocks where they idle. One acting only on maps known before play
  replays the incumbent elsewhere, then plays the panel's opponents on its maps in fixed rotation (control first, same
  rule, design simulated), judged per map and in the mean; its value is the effect times those maps' pool share.
- **Local reads** add cells until D reaches the target (§8), since a switch that changes no decision yields identical
  games (count them by diffing replays); D has no direction, so stopping on it does not bias t. Diagnostics compare with
  the incumbent's games on the same cells, not the opponent in the same game, where side and noise pass for mechanisms.
- **Cells that inspired a candidate cannot also judge it**: list them at pre-registration; require t ≥ 0 on the rest.
- **No rule applies backwards**: rules are logged before their block; a candidate rejected under an older rule reopens
  only on fresh blocks. Never read a running block or match, move a bar after seeing data or reinterpret a null.
- **Ratings** come from one Bradley-Terry fit over every ladder game (each build its own player, repeated cells once,
  games capped per pair, a weak prior, convergence checked, never compared across fits), reported as rating ± interval,
  rank and expected score against the field. They never accept: builds meet different opponents, and an incumbent's
  accepting blocks lift its own rating. The contest site's rating serves seeding only.
- One game can filter an idea but never decide it, and first looks regress toward zero. A difference concentrated in one
  direction on one map or side is real; one spread thin with mixed signs is noise.

## 5. Using the ladder like a contestant

- Local self-play screens; replica scrimmages decide. Only accepted builds play ranked games; a candidate gets short
  unranked panels and is parked once its matches exist. Trials overlap; local work never waits on the ladder.
- Panel opponents come from the band around our strength (bots that recently beat or split with us, not the rating list
  alone), since a panel of only the strongest yields few discordant cells; stronger bots rotate in as the band is
  beaten. Routine scrimmages of the incumbent meet bots above and below us on random maps and sides.
- **Version the panel** when it saturates (an opponent's cell scores have no spread; on win/loss, an opponent at ≤ 10%
  or ≥ 90%, or the panel above ~80%), turns map-narrow, or has mostly inspired candidates. Each version is its own
  series, marked on every chart, so a panel change never reads as progress. Never quote standing from panel games alone.
- Each downloaded match gets a brief write-up (what each side did, where the game turned) and a census row.
- **Endgame**: no candidate trials within the endgame window (§8) of a deadline or seeding cutoff; the best accepted
  build active; ranked games spent early enough to converge; floor over ceiling (collapsing on a fifth of maps loses
  multi-game matches); nothing untested shipped late.

## 6. When nothing is accepted

Enter at the plateau trigger (§8). Being stuck is the signal to try something new; stopping is never an outcome.
Escalate in order, skipping nothing, sharing attempts with §3 (a structural swing still comes every fourth).

1. **Correctness audit** of bot and instruments, one independent lens at a time: code never reached; rules and API,
   including what the engine leaks; shared state, messaging and stale data; what replays show units doing; the
   measurement pipeline. Each finding carries evidence, a fix, a regression test and a reviewer trying to refute it.
   Repair measurement first, proven by an identity control; one broken basic is one instance of a class.
2. Ablate everything carried (§3.4), then sweep engine and API for unused capabilities (§3.3).
3. Re-watch field games (§3.2) for what earlier reads ignored; build and submit the archetype that beats us.
4. Revisit principles from other years: the cross-year advice, the prior-season digest, `LEARNINGS.md`, and for anything
   not yet digested the permitted prior-season sources (the handoff names them and what is off limits).
5. Test near-misses in pairs, in case two pay off only together.
6. A structural swing (§3.5); if the architecture is the ceiling, a rewrite. Rejection is normal.

A closure condemns one implementation, not the idea; only an arithmetic or engine ceiling licenses abandoning a line.
When every lever prices below resolution, change the design, not the dose.

## 7. Operations

- **Never idle**: at least three jobs queued, plus an idle filler playing the incumbent on new cells so charts stay
  current, capped per pair so it cannot swamp the record. A watcher, re-armed every agent turn, wakes the session when
  work finishes, a component stalls or crashes (checked by its own lock or API, not a process name), disk runs low or
  the heartbeat fires. Never wait inside an agent turn for a job, an idea or an approval.
- **Rules live in tools that refuse**; prose is forgotten: no paired read on mismatched cells, no score of an unfinished
  block, no result row without code-hash provenance, a screen printing its own verdict, validated inputs and outputs (a
  tested refusal beats a plausible result); output channels tested at size limits; exit statuses never masked.
- **Process hygiene**: one semaphore (or a fixed split of the knee) for all game-starting paths per machine; private
  build trees and temp files; check and act under one lock; kill by pid, not pattern; never edit a running script; check
  disk before each block; one agent per working tree; delegate reading, not deciding; commit explicit paths.
- **Owner**: every owner prompt verbatim in `PROMPTS.md`, timestamped from the system clock in the owner's zone; push
  after every commit so progress is visible; any owner question, however short, triggers an audit and is answered from
  data; declining a request on a self-imposed rule states that rule's measured cost.
- **Records** hold measurements, not stories; nothing stale stays (fix it that day; annotate the superseded). Training
  log, append-only, per attempt: target, trace, pre-registration, counters, run ids, gate numbers, decision, closures,
  next step. Closed-directions ledger: grepped before each attempt, reopened only on a new premise. `LEARNINGS.md`: each
  lesson with measurement and source, once confirmed. Attempts table: furthest funnel stage. `HANDOFF.md`, hand-kept,
  dated: step-to-tool map (gaps listed), season values, simulations, closures, live run ids.

## 8. Defaults

Defaults, set by simulation; `HANDOFF.md` holds the season's, plus stall N, stockpile cap, endgame window, D target.

| parameter | default |
|---|---|
| compute | 0 overruns, 0 exceptions in the incumbent; headroom: each unit's worst turn ≤ 90% of the per-turn limit |
| delivery block | 24 paired cells, extended once to 48 if delivery is still undecided |
| local screen | 40 paired cells, both sides, extended until D reaches the target; joint false reject ≈ 1 neutral in 8 |
| ladder panel block | 6-8 field bots in our band × 3 maps per numbered set × both sides |
| panel rule | §4 bars; ≈ 5% neutral false accept; past simulations: power ≈ 13 / 22 / 44 / 65% when 5 / 10 / 20 / 30% of cells change (3 in 4 of them won) |
| rating pair cap | 200 games |
| balance | area switch after 3 straight rejects in one area; ≥ 1 structural attempt in 4 |
| plateau, cadence | plateau after 8 straight rejects or 24 h without an accept; identity control re-run after 4 straight rejects; corpus run, capability sweep and ablation each at least once per 10 attempts |
| operations | measured knee (≈ cores − 1) shared by all game-starting paths; one diagnostic game at a time on the session machine; watcher heartbeat ≤ 50 minutes |
