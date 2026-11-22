# TRAINING_ALGORITHM.md

How this project converts compute into playing strength. This document is written for any season, so no unit,
resource, map or tool of a particular year appears here. `HANDOFF.md` maps each step onto this year's tools and paths.
Where a rule carries a number, it is the measured default (§8).

**The method in one paragraph.** A continuous local tournament of our own builds, rated by Bradley-Terry, decides
which build is the champion. Every new build is one change, or one coherent strategy, made on the champion. About half
are small tunings and half are big swings. A build that clearly out-rates the champion replaces it. The new champion is
submitted to the contest ladder at once, with no further gate. Field games on the ladder replica supply ideas and
measure progress, but they never accept or reject a build. Local games are cheap, so the tournament can afford
high-risk ideas: a failed swing costs a few hundred games and nothing else.

## 0. The objective and the instruments

**Objective: absolute strength against an unseen field, as the contest ladder measures it.** The local tournament is
how we get there. It is not the goal, so the ladder stays the scoreboard (§5).

**Why the local tournament decides.** We used to accept a build only through a paired gate on the ladder. Three
lessons ended that gate:
- No local measure predicted gate success. On twelve builds, no local metric did better than chance, and our
  re-implementations of field styles saturated (93-97% wins).
- The gate spent hours of scarce ladder capacity per candidate.
- The field's verdict on a narrow change was mostly cell noise.

Replacing the gate with a fast local search gave 12 new champions in its first 22 hours. The ladder rating rose with
them, with dips along the way, though the ladder rank held.

**Terms.**
- The *champion* is the tournament leader that holds the title. It is also the active submission, and the base of
  every new build.
- A *newcomer* is a build that is still being rated.
- The *pool* is the set of builds currently being scheduled.
- A *run* is a batch of tournament games played as one job.

| instrument | answers | blind to |
|---|---|---|
| rules digest (from engine source, each fact cited to a file and line) | what is possible; what each action costs | what wins |
| replay reader + the bot's decision counters | how one game unfolded; whether a mechanism fired | frequency, value |
| **local tournament** (our builds only, Bradley-Terry over every game) | which build is strongest against our own lineage | what the field punishes that we never do |
| ladder replica (downloaded field bots, played under contest rules and request limits; the only place field bots play) | strength against the field; what the field does that we don't | why |
| replay studies of field games (statistics across many replays, delegated) | which behaviours separate our wins from our losses | cause, until a build tests it |

## 1. Foundation first

No strategy work starts until each of these exists and has passed a test with a known answer. Every past instrument
failed on its first use, and a strategy tried on a broken base only measures the defect.

1. **Rules digest from engine source, not the spec.**
   - Cite each fact to a file and line, and list every disagreement between spec and engine.
   - For each action, record its legality checks and the order in which it charges costs. Hidden limits and free
     actions live there.
   - Record the order of execution within a round.
   - Tally how starter-versus-starter games end, by win condition or tiebreak.
2. **Map corpus**: size, symmetry, spawn geometry, resources and key distances for every map. Anything that holds on
   every map is a constant, never something to sense.
3. **Runner**.
   - It is headless, parallel, bounded in wall-clock time, gives bots no network, and checks that each game ran as
     configured.
   - It writes one row per game: both builds, the map, the seats, the seed, the winner, how the game ended, the
     rounds, exceptions, and per-side counters.
   - Measure the concurrency knee and the cost of each map.
4. **Determinism**: control every source of randomness and give each game an explicit seed, so a game can be replayed
   exactly.
5. **Replay reader**.
   - Per-round team aggregates, event windows, unit lives and our own logs.
   - Losses by cause, place and attacker.
   - Fixtures pin it to the engine.
6. **Bot architecture**.
   - Layered: a turn loop, globals, one constants file, a communication schema, map memory, navigation, and a handler
     per role.
   - Every tunable is a named constant, so a new build is a short edit of one file (§2.3).
   - Moves stay symmetric when sides switch, unless a seat-dependent engine behaviour gives one seat an edge worth
     taking.
   - A compute monitor counts overruns and exceptions per stage from the first commit.
7. **Snapshot tool**: freeze each build under its own name with a reason line, kept forever as a possible opponent. A
   variant tool takes the champion plus a list of constant edits, so a new build costs one command.
8. **Field bots**: found, downloaded, security-scanned and compiled by script. Never read their source.
9. **Private ladder replica**: isolated from the official contest and played **as a contestant** through its API.
   Ranked matches run continuously against the field, and replays can be downloaded.
10. **Tooling**.
    - One command per job: rank, plan a run, add a bot, chart, refresh progress.
    - Each tool fails loudly.
    - Each process change is a dated milestone on every time-axis chart.
    - Unit tests run after every tool change.
11. **Basics battery** (absolute bars, run on the champion): no idle producer, no stuck unit, no unforced death, zero
    compute overruns and exceptions. While a row fails, candidates come from it first.

**Foundation bar**: the build beats the starter from either side on every map, clears the battery, and is on the
replica. That build is the first champion.

## 2. The loop: a continuous local tournament

### 2.1 The pool

- About **10 bots**: the champion plus about 9 newcomers, half incremental and half big swings (§3). When a bot
  retires, replace it with the kind that is short.
- A pool file lists the active bots. Retired bots stay listed as comments: they are no longer scheduled, but their games
  still count in the fit, where they anchor the ratings.
- **Naming** tells the two kinds apart at a glance. Incremental builds and big swings use different name prefixes,
  numbered in order (this season: `yNN` and `swNN`). A table of swings, with each strategy and its fate, sits beside
  the pool file.

### 2.2 Ratings and the champion

- **One Bradley-Terry fit over every local game** played between bots that are in the pool or retired from it.
  - Every game counts on the contest's map set, from any seat and any seed, so older runs seed the ladder.
  - Each pairing is capped at a fixed number of games, so one heavily repeated pair cannot dominate.
  - A weak prior keeps unplayed bots finite.
  - Each bot reports its rating ± standard error and its games played.
- **The champion changes when a bot with at least MIN_GAMES games leads the champion by more than one combined SE**,
  √(SE₁² + SE₂²). The rank command prints the lead each bot needs and the games it still needs, and announces a new
  champion. The margin is hysteresis: without it, two near-equal bots would trade the title on noise.
- **Early ratings mean little.** Some measured swings:

  | build | early | at 100-300 games |
  |---|---|---|
  | forward hold | +70 at 25 games, then −43 at 63 | +17 at 284 |
  | king finisher | −190 at 12 games | +22 at 177 |

  Never retire a bot, or get excited about one, before it has about 50 games.

### 2.3 Building candidates

- **Every build is made on the current champion**, so its rating difference has one cause. That cause is one change,
  or one strategy when the build is a swing.
- Make the build with the variant tool: the champion plus explicit constant edits, frozen under the new name. Diff its
  constants against the champion's to confirm the edit and nothing else.
- Enter it into the pool, and log it in the attempts table and the training log with its change and its motivation.

### 2.4 Scheduling games

- A run plans a fixed number of games, each with a random map, a random seed and random seats.
- **Newcomers** with fewer than NEW_MIN games get half of each run, weighted by how many games they are short. Half of
  those games are against the champion, the rest against the top bots.
- **The other half** goes to pairs among the top TOP bots, weighted toward close ratings and large SEs. Those pairs
  decide who is first.
- **Re-plan queued runs whenever the pool changes.** Take the run off the queue first, so it cannot start while it is
  being replaced.

### 2.5 Crowning a champion

Do all of the following in the same turn:
1. Submit the new champion to the ladder.
2. Record it in the champion file and the champion history (time, bot, base, change, submission id).
3. Make the working bot a copy of it.
4. Log the change, mark it accepted in the attempts table, and refresh and commit progress.

There is no ladder gate. Expect the ladder rating to dip sometimes: the valleys are part of reaching higher ground, and
the tournament, not the ladder, judges the build.

### 2.6 Retiring bots

- Retire a bot that sits far below the champion after enough games, normally 200 or more.
- Retire earlier when a bot is more than about 2 SE below the champion and has 50 or more games: swings that collapse
  rarely recover. Keep a bot that is level with the champion while the pool has room.
- Comment the bot out with its reason. A failed swing gets a second version before its strategy is dropped (§3.2).

## 3. Where candidates come from

Aim for **half incremental tunings and half big swings**. Tunings repair and polish. Swings escape the local optimum,
which is where a human team's edge has always been.

### 3.1 Incremental builds (one constant or one branch)

1. **Field study findings**: a behaviour that separates the champion's ladder wins from its losses, turned into one
   change (for example, keeping the rush going longer when presence in the middle collapses after it).
2. **Own degeneracies**: battery symptoms below their bars, deaths by cause, a counter that never fires.
3. **Re-tests on the new base**: a change that lost on an old champion can win on a new one, so search the attempts
   table first. Every repeat is labelled as one.
4. **Ablation**: turn off a carried switch, which also prices the branch the bot takes instead.

### 3.2 Big swings (a coherent strategy change)

- **A swing is a strategy, not a dose**: a different economy, opening, army doctrine, target priority or style adopted
  whole. It is usually a bundle of constants that only make sense together, or a new mechanism.
- **Design swings from field evidence.** Watch what the bots ranked just above us do differently. Delegate a study of
  many replays (a hundred or more games) that compares behaviour rates in our wins and our losses, then turn each
  hypothesis into a swing.
- **Price the economy before building.** A swing that grows the army must still afford it. Bigger armies starved in
  45 of 83 losses of one blitz and in 15 of 30 of one empire.
- **A losing swing gets a second version** that keeps its idea and fixes its first visible failure (starvation, early
  merges, an over-extended cap). Drop the strategy only after two or three versions fail, and record why.
- **Combine swings that hold level with the champion.** Two independent strategies that each match the champion are
  the best bet for a pair. Bundling neutral tunings is not: those combinations read negative.
- **Try opposites.** When an all-in style fails twice, enter its mirror (a greed build after a failed blitz). The
  failure tells you which side of the trade-off the champion sits on.

### 3.3 Balance

- Keep at least one strategy-level swing in the pool at all times.
- Three straight losers in one functional area (economy, production mix, movement, combat, defence, scouting) send the
  next build elsewhere.
- Repairs and cap removals have carried over to the field. Pure reallocations and constant tuning carry over less
  often, but the tournament is cheap enough to try them anyway.

## 4. Statistics

- **Bradley-Terry** with a weak prior and a per-pair game cap. Ratings are compared only within one fit, never across
  fits.
- **Use the lead over the champion with a combined SE, not the rank.** A bot can rank first and still not be champion.
  A lead under one combined SE is a tie, and a tie keeps the champion.
- **The tournament is self-play.** It finds builds that beat our own lineage, and the field may disagree. That
  disagreement is accepted risk: it shows up as a ladder valley and is corrected by the next champions. The field
  study keeps candidates aimed at what the field actually does.
- **Repeated games**: games are deterministic per (pair, map, seed, seats). The random seed range times maps times
  seats is large compared with the pair cap, so repeats are rare. Do not shrink the seed range.
- **One game filters an idea but never decides it.** A replay shows that a mechanism fired, not that it pays.
- **Ladder ratings** are zero-sum, with the field mean pinned. Ratings above the median drift upward as the season
  spreads, so judge our progress by rank, or by the gap to reference places (#10, #15, #20), not by our raw rating.

## 5. Using the ladder like a contestant

- **Only the champion plays the ladder**, through continuous ranked matches. Field bots play only on the replica,
  within its request limits, as they would for a contestant. Locally, only our own builds play.
- **The ladder supplies ideas and a progress measure, never a verdict.**
  - Check the champion's new matches at every cycle, and look closely at losses to the teams just above us.
  - Use extra challenges only when a specific idea needs field evidence that ranked matches do not provide.
- **Read progress as rank and as the gap to reference places**, on wall-clock charts that cover the whole history plus
  a recent panel. Mark process changes as milestones.
- **Keep the replica healthy**: its disk must stay above the floor, so automatic pruning of old replays runs when free
  space drops. A full disk takes the replica down.
- **Endgame**: near a deadline, stop crowning untested strategies, keep the best-rated champion active, and spend
  ranked games early enough to converge.

## 6. When the champion stops changing

Enter at the plateau trigger (§8). A stuck champion is the signal to try something new; stopping is never an outcome.
Escalate in this order:

1. **More and bigger swings.** Raise the swing share, and design them from a fresh field study.
2. **Correctness audit** of the bot and the instruments, one lens at a time: code never reached; rules and API; shared
   state and stale data; what replays show units doing; the tournament pipeline itself. Check that a renamed copy of
   the champion rates level with it.
3. **Ablate everything carried**, then sweep the engine and API for capabilities we never use.
4. **Revisit principles from other years**, through the permitted sources only (the handoff names them, and what is
   off limits).
5. **A rewrite**, keeping proven infrastructure, if the architecture is the ceiling.

A failed build condemns one implementation, not the idea.

## 7. Operations

### 7.1 The prod cycle (about every 20 minutes)

1. Check the game machine: what is queued, what is running, and what has finished.
2. Pull every finished or partial run, and rank.
3. On a new champion, crown it (§2.5).
4. Keep at least 5 runs queued past the next cycle, and de-duplicate the queue.
5. Retire bots and add newcomers to keep about 10 bots, half incremental and half swings.
6. Check the champion's new ladder matches for ideas.
7. Log, commit and push.
8. Refresh the charts, and check the replica's disk.
9. **Report**:
   - the tournament's size: games played, games added since the last cycle, active, retired and entered bots;
   - one table of the whole pool sorted by rating, the champion marked in place: each bot's kind, base, change,
     rating ± SE, games, lead over the champion, the lead it needs, and the games it still needs (no separate top-5
     table: it only repeated the head of this one);
   - what was retired or added since the last cycle, and why;
   - ladder rank, rating and time;
   - replica disk and machine state.

All times are in the owner's zone.

### 7.2 Machines

- The game machine plays local games only, and the replica machine runs the ladder only.
- If either sits idle for long stretches, tell the owner.
- Never wait inside a turn for a job, an idea or an approval.

### 7.3 Hygiene

- Rules live in tools that refuse; prose is forgotten.
- Kill processes by pid, not by pattern.
- Never edit a running script.
- Take a run off the queue before re-planning it.
- Stage and commit explicit paths, never everything.
- Delegate reading, not deciding.

### 7.4 The owner

- Record every owner prompt verbatim in `PROMPTS.md`, timestamped from the system clock in the owner's zone.
- Turn standing instructions into memory and into this document.
- Push after every commit, so progress is visible.
- Answer any question from data.
- Make procedure decisions without asking, and log each one with its reason.

### 7.5 Records

- **Training log**, append-only: every cycle's numbers, every newcomer and retirement, every owner directive.
- **Attempts table**: one row per build, with base, change, motivation and fate (newcomer, retired with numbers, or
  accepted as champion).
- **Champion history**, **pool file**, **swing table**, and the field-study write-ups.
- Records hold measurements, not stories, and nothing stale stays.

## 8. Defaults

`HANDOFF.md` holds the season's values where they differ.

| parameter | default |
|---|---|
| pool size | about 10 bots: the champion + about 9 newcomers, half incremental, half swings |
| champion rule | at least MIN_GAMES = 200 games, and a lead over the champion of more than √(SE₁² + SE₂²) |
| newcomer share | bots under NEW_MIN = 40 games get half of each run; half of that against the champion |
| top pairs | the other half among the top TOP = 6, weighted toward close ratings and large SEs |
| run | 120 games, random map from the contest set, random seed in 0-9, random seats |
| fit | Bradley-Terry, weak prior, at most 200 games per pair; retired bots' games kept |
| retire | far below the champion after 200 or more games; or more than about 2 SE below after 50 or more; swings get a second version first |
| queue | at least 5 runs queued past the next cycle |
| cycle | about every 20 minutes: pull, rank, crown, queue, build, log, chart, report |
| plateau | champion unchanged for 12 hours, or 10 straight newcomers below the champion: escalate (§6) |
| compute | 0 overruns and 0 exceptions in the champion; each unit's worst turn at most 90% of the per-turn limit |
| replica disk | at least 2 GB free; automatic pruning of old replays below 4 GB |
