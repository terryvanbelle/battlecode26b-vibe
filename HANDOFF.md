# Handoff: battlecode26b-vibe

Clean-room 2026 redo (see PROMPTS.md #1 for the rules: no reading 2026 post-mortems, nothing from the original
battlecode26-vibe except running its bot as an opponent, competitor code is scanned for safety and never read).

## Where things are
- `bots/ratbot/` is the dev bot; `bots/s0*` are frozen snapshots (`tools/snapshot.sh NAME`). Behaviour switches are in `Cfg.java`.
  - `King`: spawn (a core of 12 always, more on projected income (`Econ`), freely at war), rat traps at war,
    symmetry, comms.
  - `Baby`: gather/deliver, cat fighting and cat traps, war mode (bite targeting, ratnap and throw, rat traps,
    anti-ratnap facing), optional `Micro` (scored combat movement) and rush role (`Cfg.RUSH`).
  - `Nav`: greedy plus bug, digs dirt.
- `test/ratbot/BotTest.java` holds the bot unit tests (fake RobotController). `tools/test_tools.py` tests the tooling.
  `tools/unit-tests.sh` runs both, after every change.
- Running games:
  - `tools/arena.py` (game/matrix) and `tools/cells.py`.
  - `tools/gauntlet.py` (field cells/table, from `research/field/FIELD.tsv`).
  - `tools/game_report.py` (basics report, `--timeline N`), `tools/rows.py`.
  - `tools/battle_report.py` (combat: front line, HP loss by cause, grabs and their fate, throw damage, cheese per window).
- Progress (laid out like battlecode23-vibe's `progress/`): `tools/progress-refresh.sh` pulls VM `runs/*/results.jsonl` and replica matches into `progress/games.csv`, then writes ELO.md + elo.png + ratings.csv (Bradley-Terry, `tools/elo.py`: local games count their points share, our-vs-field games carry a per-map handicap so builds gauntleted on different maps compare), ladder.md/png and rating-history.md/png (replica, `tools/replica_report.py`), progress.png (bc23 layout: our builds' rating +- 95% and expected field score in date order, adopted builds filled) and metrics.png (raw gauntlet metrics from `research/progress.tsv`) (`tools/plot_progress.py`) and ATTEMPTS.md (`tools/attempts.py render` from the hand-kept `progress/attempts.csv`; `attempts.py init` adds new snapshots, `attempts.py set NAME outcome=...` records verdicts). Run it after each gauntlet and commit progress/.
- VM: `source tools/vm.sh && ensure_vm && vm_sync`. All volume games run on battlecode-dev.
  - Local games use `BC_SLOTS` up to 6.
  - The replica uses 2 slots (`/etc/bc26-galaxy/local.env`).
- Replica ladder: `tools/galaxy26/` (ACCESS.md).
  - `contest.py submit` uploads `bots/ratbot` as team vibe26b.
  - `field.py status|queue|rate`.
  - The activity and autoscrim timers are ON.

## TRAINING_ALGORITHM.md, this season (step -> tool; gaps marked GAP)
- Rules digest: `RULES.md` (engine-cited). Compute is a deterministic bytecode count (baby 17500, king 20000 per
  turn; overrun = debt), plus a 1200 s team CPU cap per game: every local overrun is a real defect.
- Map corpus: `maps/`, `research/maps.tsv`, `bc26tools.MapCensus` (`--ascii NAME`). Runner: `tools/arena.py`
  (headless, seeded, one row per game in `runs/RUN/results.jsonl`). Replay reader: `tools/game_report.py`,
  `tools/battle_report.py`, `g0.turns.tsv` / `g0.actions.tsv` from `arena.py game --tsv`.
- Decision tags: the indicator string (`Debug.java`): babies `mv=` move reason, `dg` digs, `fl` flow, `df` defends,
  `EXC=n:type@stage`; king `km=` mine-walk reason (m walk, D dug, x cooldown, y cheese, z no dirt-only step, e/c enemy
  or cat seen, a arrived), `hm` home mine, `kd` king digs.
- Snapshot: `tools/snapshot.sh NAME` (bots/NAME + SNAPSHOT with source hash and a reason line).
- Field bots: `~/projects/vibe/bc26b-benchmarks` (scanned, source unread), `research/field/FIELD.tsv`.
- **Local screen (rejects only)**: `gauntlet.py cells bots/NAME --maps DefaultSmall,DefaultMedium,cheesefarm,
  UncomfortablyClose,Excavation,Stash --only <the six strongest>` (72 cells) and the worst-12 maps vs Version41 (24
  cells), both on the VM (`build/g7_NAME.tsv`, `build/g7_NAMEw.tsv` are the cell files); then
  `gauntlet.py paired runs/BASE/results.jsonl runs/CAND/results.jsonl` (cell score = points share; reject at t <=
  -1.5 or sign p < 0.05). GAP: no seed axis beyond seed 0 and no identity-control run yet; no D-target extension.
- **Ladder panel (accepts)**: replica, `tools/galaxy26/contest.py`. Panel v1 = 8 field bots around our rating, 3 maps
  each (`research/panel/v1-setN.json`), both sides (`--vs NAME@+,NAME@-` per opponent: 48 games a block). Control:
  the incumbent's own block on the set; candidate: submit it, create its block, re-submit the incumbent; `contest.py
  fetch --new`, then `contest.py paired CONTROL_IDS CAND_IDS` (win/loss per cell; accept at t >= 2 with D >= 4,
  blocks 1-3 reject at t <= 0 / 1 / 1.5). GAP: graded (points) score from panel replays; autoscrim can pair us while
  a candidate is active.
- Ratings and progress: `tools/progress-refresh.sh` (see above); milestones `tools/milestones.py add LABEL`.
- Records: `TRAINING_LOG.md` (per attempt), `progress/attempts.csv` (`attempts.py set NAME outcome=...`),
  `research/progress.tsv` (gauntlet metrics), `PROMPTS.md`. GAP: closed-directions ledger is the attempts outcomes.

## State (see TRAINING_LOG.md for the history)
- Incumbent on the replica: s2o (submission #62, rating ~1408, #22 of 54). Dev base: s3d line (s3a dig-first, s3b
  king defense + mine spread, s3c centre-first explore, s3d king digs to its mine). s3b passed the local screen vs
  s3a (t +0.50 top-6, -1.08 worst-12); s3c and s3d are queued on the VM (runs g7_s3c*, g7_s3d*).
- Against examplefuncsplayer (all 74 maps, both sides): v0d won 134-14. Against the six strongest field bots we
  score ~26% of points and win ~5 of 72; Version41 out-collects us ~2-3x (it tunnels to the centre from round 3 and
  walks its king to the mines).
