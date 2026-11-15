# Lessons from bc26a0 for breaking the 2026b plateau

2026-11-15. Written by the Claude session in battlecode26-alpha0 (bc26a0) at the owner's request, for the Claude working
in this repo. It is advice: TRAINING_ALGORITHM.md and the owner's prompts win where they conflict. Sources are listed
at the end; every number below comes from one of them.

## Context

- bc26a0 (github terryvanbelle/battlecode26-alpha0; on the driver at ~/projects/vibe/bc26a0) imitation-trained a
  neural bot on this repo's zdb (accepted 2026-11-08 09:05 PST), then ran RL and ES from it.
- Its copy of zdb, zdp (java/bots/zdp), routes every zdb choice through one decision layer,
  `D.pick(point, options, features)`, with points such as BABY_MOVE, BABY_ROLE and KING_ACT.
- Two differences from this repo's rules: bc26a0 played field bots locally, and ran with bytecode limits off. Its
  "field" numbers are local games, not replica games. Use them as evidence about methods, not as standings.

## Diagnosis: two walls

### Wall 1: the panel no longer resolves the changes it accepts

- z3x (2026-11-06, 94 cells), zdb (2026-11-08, 96 cells) and zj3 (2026-11-10, 96 cells) were each accepted at D = 4,
  +4 -0, t = 2.00, the bar's minimum. 90-92 cells of each accepting panel came out alike.
- Bradley-Terry ratings (progress/ELO.md): z3x 1615 +- 37, zdb 1629 +- 43, zj3 1621 +- 78, so the three cannot be told
  apart. Top field bots on the same scale: #1 IvanGeffner_basic51 2180 +- 24, #5 abcdezyxwv_bestsofar 2007 +- 15,
  i.e. 386-559 above zj3.
- TRAINING_ALGORITHM.md §8 simulates panel power of 13% when 5% of cells change, with about 5% neutral false
  accepts. If a share pi of candidates are real gains, P(real | accept) = 0.13 pi / (0.13 pi + 0.05 (1 - pi)), which
  is 0.5 at pi = 0.28. Unless more than about 28% of candidates are real gains, a D = 4 accept is more likely noise.
- Cells needed: a change that flips a share p = 0.05 of cells and wins q = 3/4 of those has, per cell in win/loss
  units, E[d] = p (2q - 1) = 0.025 and E[d^2] = p = 0.05, so E[t] = 0.112 sqrt(n). E[t] = 2 needs n = 320 cells; 80%
  power at the t >= 2 bar needs n = 650. Two blocks are about 96.
- progress/attempts.csv logs 1,080 attempts from 2026-10-27 to 2026-11-12; 15 builds were accepted through the
  panel before the x-line began on 2026-11-11.

bc26a0's es-m2 run looked the same before it was stopped: each update moved coordinates on noise, and the centre
random-walked.

### Wall 2: the incumbent sits on a sharp local maximum

- PPO (bc26a0 rl-m2: PPO with a learned value baseline and GAE, 39 iterations, about 5,000 games) stayed flat. Its
  32-game gates against zdpf005 / zdb (a frozen net imitation of fixed zdp, and zdb itself) were 17/16 at v0 and
  17/16 at v39; v33-v39 pooled 96/192 = 0.50.
- ES on decision biases (about 3,200 games). es-m3 drew joint directions over 168 bias and temperature coordinates,
  with sigma calibrated to flip the top choice at each point 1-16% of the time. Its candidates scored 92/224 (0.41)
  against their start in iterations 1-7, about 9 points below even; a larger step at iteration 0 scored 8/32.
  Split-half gradient cosines were 0.03-0.10 in iterations 5-7: no consistent direction. es-m2 (per-point blocks)
  scored 263/576 (0.46) against its start.
- Reading: within "re-weight zdb's own decisions", zdb is a sharp peak in self-play. A tuned bot's parts are
  co-adapted, so a single change is punished even when it would help once its partners are re-tuned. Gains have to
  come from behaviour the current decisions cannot express, or from many changes each too small to see alone.

## Recommendations, by expected value

### R1. Behaviour-exact cleanup, then a multi-reviewer audit; port bc26a0's 16 zdb fixes

Evidence (bc26a0 docs/decision-points.md, pass 2):

- 2A (commit 0ac0a97): 43% of zdb was dead under its own Cfg values, 10,403 -> 5,948 lines. Removed: 7 whole files
  (KPath, BitFlow, GrabMicro, Seal, TrapRows, Trail, PathOracle), about 1.1k lines of Baby, 380 of King, 580 of Flow
  and 227 unused Cfg constants. Check: 60/60 games identical to zdb.
- 2B (commit 0bdf161): six area reviewers raised 21 suspected bugs; 16 survived adversarial verification. Each sat
  behind its own Cfg.FIX_* flag (false = zdb's behaviour); all false replayed zdb 24/24.
- All 16 on: 40/64 against zdb (0.625, 95% CI 0.50-0.73), 8 maps x 4 seeds x 2 sides, no limits.
- Field caveat: on a local held-out field the fixed build scored 114/192, the same as zdb. That panel was mostly
  lopsided (zdb's copy on a related 80-game panel: 16/16, 16/16, 5/16, 3/16, 1/16 per bot), so it could not resolve
  a gain of this size. The field value is unknown.

The 16 fixes:

- Babies: bite any reachable tile of an enemy king, not just the nearest tile; throw enemies into cats, which kills
  them; THROW_HOLD after a carry swap; forget a stale enemy-king sighting; wander inside the mine box that `atMine`
  counts; change mine when a stalled one is abandoned; end a seek only on real news; FORM_KING centre rats promote as
  soon as they are action-ready.
- Kings: pickup income reaches the income estimate; skip piles under the king; bite enemy kings on a reachable
  tile; the shared alert keeps the most-threatened king's alert.
- Navigation and traps: DIG_FIRST applies only to an old target; the robot-blocked counter resets on a new search;
  trap marks fix the arrival round and ignore carried rats.

First steps:

1. For each fix, find its site in bots/ratbot and check whether the bug is still there (bots/zdb is the 2026-11-08
   source; zj3 and the x-line changed code since). The 0bdf161 diff shows each flag's code. zdp's files and methods
   differ from zdb's after the decision-layer refactor, so match by behaviour, not by line.
2. Port each fix that still applies behind its own Cfg switch, defaulting to the incumbent. Prove that the default
   replays the incumbent.
3. Check compute per fix with the compute monitor (bc26a0 ran without limits).
4. Gate all ported fixes as one candidate (R2). Below t = 2 at the final block means below resolution, not refuted.
5. Repeat the method on the current incumbent: delete code dead under the current switch values, checked by replay
   identity. Then run reviewers by area, and verify each finding adversarially before fixing it.

### R2. Bundle audited fixes that are individually below resolution

- Each of the 16 fixes was worth about 0.8 points on average (12.5 / 16). None was measurable alone; together they
  were 12.5 points. In this repo, z1z, the largest rating step between accepts (u6x 1499 -> z1z 1604, +105), was a
  bundle: z1q + z1r on u9p, a line rejected at replica block 3.
- Respect §2's measured rule: combinations of changes merely not worse (t near 0) have read clearly negative. So
  bundle only changes with independent evidence of value: audited bug fixes, and delivered near-misses with
  independent mechanisms.
- Keep every member behind its own switch so that leave-one-out ablation (§3.4) can follow acceptance.

### R3. More games for the accepting gate

- The replica runs 2 slots (/etc/bc26-galaxy/local.env). The z1z note of 2026-11-06 says it ran about 4 matches an
  hour. The request limits (40 unranked + 20 ranked per hour) are far above that, so acceptance is bound by slots,
  not quota.
- Options: give the replica more of the VM's 8 cores (local screens use BC_SLOTS up to 6), or add the owner's Windows
  box as remote replica workers. Box facts from bc26a0: WSL2 capped at 8 GB, i7-13700HX, about 2.7x faster per game
  than the VM. Game JVMs reach 1.0-1.4 GB RSS at -Xmx1200m, so 5-6 can run at once. bc26a0 uses the box: ask the
  owner before taking it.
- Use a round margin as the screen's graded score where kings decide. bc26a0's 'rounds' fitness (es_update.py `_pts`)
  is score = 0.5 + 0.5 ((1 - a) o + a o m), with o = +1 for a win and -1 for a loss. m = 1 - 0.5 rounds / 2000 for a
  game decided early, 0.5 at the limit; a = 0.5. Any win still outscores any loss, a quick win beats a slow one, and a
  long loss beats a short one.
- On a known handicap (a bot scoring 17% against its reference) it raised the paired t from 6.17 to 6.59, worth
  about 14% more games (t grows with the square root of the games); other variants gave 15-30%.
- Per §4: check that the graded score predicts wins on each panel version, simulate the bars for it, and keep the
  accept on the win/loss net.

### R4. Make local games predictive, so the replica confirms rather than searches

- LEARNINGS 12 and 13: peaceful self-play never goes to war (80/80 identical games for a war-only switch), and rush
  sizes 8 and 16 looked neutral or positive locally, then lost on the replica.
- Exploiter league: local bots written to beat the incumbent, with full read access to its source, which is ours.
  They are not field bots, so they play locally without quota.
  - Start with the king. LEARNINGS 7: strong bots backstab by round 15-35 and kill the king within 20-300 rounds.
    HANDOFF state, 2026-11-12: most losses are now kings bitten or thrown at.
  - Freeze each exploiter that beats the incumbent into the roster with a one-line purpose.
- Track predictiveness. For each candidate that reaches the replica, record every local instrument's verdict
  (self-play screen, archetypes, exploiters) beside the panel verdict. After about 10 candidates, screen with the
  instruments that predict; §4 already asks how well screen t predicts panel t.
- Do not clone field bots from replays at the action level.
  - bc26a0's action-level student (per-primitive imitation of zdb, with full state access) scored 1/32 against zdb.
    On a local field it scored 0.18, where zdb scored 0.33.
  - Decision-point imitation matched zdb (16/32; field 40/80 against zdb's 41/80), but only because zdb's internal
    decisions were available.
  - Hand-built archetypes from replays (§3.2) remain the right form.

### R5. A win-probability model on replica replays, to locate where losses turn

- bc26a0's learned value model, as game-level 5-fold out-of-fold Brier / AUC:
  - near-mirror self-play: 0.227 / 0.656 overall, with AUC 0.55 before round 200 and 0.73 after round 1500;
  - M1 games, where the learner started deliberately handicapped: 0.172 / 0.822 overall, AUC 0.74 before round 200.

  Losses to top bots are lopsided, so expect something nearer the better case.
- In near-mirror games (5,036 rl-m2 games), the cheese-transfer leader at rounds 500 / 1000 / 1500 matched the final
  winner 58% / 66% / 71% of the time. A single-metric lead is a weak predictor in close games.
- Build it as follows:
  - features: per (game, team, 10-round bin), from ReplayDump aggregates for both teams (babies alive, bank, king HP,
    traps placed and fired, deaths by cause, cheese transferred);
  - target: win;
  - model: gradient boosting or a small MLP, with folds split by game, never by row, so a game's outcome never trains
    its own prediction;
  - calibration: a fitted temperature.
- Use: in each loss to an in-band or top bot, find the bins with the largest drop in P(win) and trace there first.
  Aggregate the windows per opponent and map. This extends §3.2's "first round from which a lead predicts the
  winner" to all metrics at once.
- Never use it as an acceptance score: candidates would learn its blind spots.

### R6. Give structural swings a tuning budget before judging them

- If small random changes cost a tuned bot about 9 points, a raw structural change is judged far below its own tuned
  version. The x-line (from 2026-11-11) already has the right shape.
- Proposed rule: a structural candidate becomes its own line, with a pre-registered budget (repairs or days) of local
  fixes. Then compare the line's tip with the incumbent on fresh panel sets, against a kill criterion registered up
  front.
- Candidates: the rush doctrine adopted whole (§3.5: traits lifted singly rarely carry a field style's strength), or
  early detection of the opponent's style choosing between doctrines.

### R7. Decision-level instruments

One decision layer (zdp's `D.pick`) gives three tools:

- A census per point: how often it opens, which options are allowed and which is chosen, on every game. On the
  replica this extends the why-tags (Debug.tag pads the bytecode count mod 128; tools/smuggle.py decodes it).
- Forced-option ablation: a switch that takes the next-best allowed option at one point prices that point, locally.
- Deterministic forks:
  - replay a lost local game exactly to (round, unit, point), force the alternative and play out;
  - the control fork changes only a tie-break salt at the same moment;
  - a flip rate well above the control's marks a real blunder.

Forks against field bots would cost replica quota, so run them against own builds, archetypes and exploiters.

### R8. Bytecode headroom (hypothesis, unmeasured)

bc26a0 proves refactors and optimisations behaviour-identical with a per-game hash of the action trajectory
(`traj_hash` in its result.json), limits off. Here:

1. Optimise hot paths under the same check.
2. Measure overruns and near-limit turns with limits on.
3. Spend the freed budget on combat search.

## Do not repeat

- Tuning constants or option frequencies by self-play: bc26a0's PPO and ES found nothing, matching "Never tune by
  self-play tournament".
- Action-level cloning of other bots.
- Reading one flipped cell as a mechanism (LEARNINGS 19).

## Suggested order

1. R1 + R2: cleanup, audit, port the 16 fixes, gated as one bundle.
2. R3: more replica slots and the round-margin score, with the bars re-simulated.
3. R5: the win-probability model on the archived replica replays (VM ~/diag26b/rp/*.tgz).
4. R4: the exploiter league, starting with king defence; then R6 for a rush line.

## Sources

This repo:

- progress/attempts.csv: rows z3x, zdb, zj3 (the D = 4 accepts), z1z, u9p.
- progress/ELO.md: Bradley-Terry ratings of builds and field bots.
- progress/ladder.md: replica ladder snapshot, 2026-11-11 00:05 PDT.
- TRAINING_ALGORITHM.md: §2 (combination rule), §3.5, §4, §8 (power simulation).
- HANDOFF.md: replica slots and request limits, why-tags, state of 2026-11-12.
- LEARNINGS.md: 7, 12, 13, 19.

bc26a0 (github terryvanbelle/battlecode26-alpha0; ~/projects/vibe/bc26a0):

- docs/decision-points.md: the decision layer, pass 2A/2B, the 16 fixes and their checks.
- Commits 0ac0a97 (cleanup) and 0bdf161 (fixes behind Cfg.FIX_* flags; the flags were later removed, keeping the
  fixed code).
- docs/imitation-results.md: action-level against decision-point imitation.
- docs/rl-plan.md, "Critic quality": value-model Brier / AUC by round.
- docs/rl-es-plan.md §9 and scripts/es_update.py `_pts`: the margin fitness and its measured gain.
- PROGRESS.md: rl-m2, es-m2 and es-m3 results.
