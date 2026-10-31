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
    It takes a .bc26, a dump directory or a replica match directory (MATCHDIR, dumped from its replay.bc26; side
    reversal read from the match.json beside it). Trap keys: `placed` = `hit` + `removed` + `live` for traps placed
    in the window, `fired` = triggers in the window; throwOut `kills` counts rats, `killsKing` kings. An empty
    window prints only the header.
  - `tools/king_report.py [--json] REPLAY` (per king: died, deathCause, starveHp, hpBy incl. `ownThrow`/`ownThrowHits`,
    firstFoe, broke). `game_report.py` rows carry `bc`; `tags` is null for an untagged team; the timeline has kings
    columns.
  - `tools/java/bc26tools/ReplayDump.java`, dumpVersion 3 (2026-10-28 audit round 2): trap victims by the engine's
    footprint rule, starvation booked only when the bank is surely below 2 (else the hp goes to the biter as boosted
    bite), summary mode exits 1 on a parse failure, meta `file` absolute. Known gap: a king that starves on the turn
    it forms is not booked (never seen).
  - `tools/stuck_report.py SRC... [--team NAME] [--span 30]`: stuck babies from the tag: travel code (d, M, C) on
    every turn inside a 3x3 box for 30+ rounds; lost rounds by code, longest episodes with the distance to the nearest
    own king (2 = touching its body). s4d baseline on runs/diag_stuck1 (5 maps vs s2o): 105 episodes, 7295 rounds.
    `arena.py game A B MAP --keep --tsv --out DIR` keeps indicator strings (fl=builds:done:turns:steps).
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
- **Cell score** (`tools/score.py`, used by gauntlet/games/elo): 100/0 to the winner when kings decide a game, else
  the spec's points share. Before 2026-10-28 08:28 (milestone M3) screens used `arena.points`, whose "kings" share
  read the replay's kingsA/kingsB aggregate, which is 10 * global cheese + kings: a king-kill loss scored ~25-45 for
  cat damage and leftover cheese. Every score is now recomputed from the raw fields, old runs included.
- **Field bots play only on the replica** (owner, PROMPTS #8; milestone M4, 2026-10-28 08:50): every game against a
  downloaded bot is a replica scrimmage under its hourly limits (40 unranked + 20 ranked requests), as for a contestant.
  Local games (VM `arena.py matrix`, driver diagnostics) are our builds against each other, archetypes and the stock
  players only. Before M4 every local screen (g1-g7 top-6, b1 band) and diagnostic (dm*, cf*) ran field bots locally;
  `tools/elo.py` now ignores those games (`--local-field` brings them back) and progress.png shows accepted builds only.
  Field-bot diagnostics now come from downloaded scrimmage replays (`contest.py fetch`, then `game_report.py` /
  `econ_report.py` / `battle_report.py` on the .bc26).
- **Request queue**: `contest.py scrim ... --queue research/panel/queue.tsv` keeps what the limit refuses, tagged with
  the active build; `tools/galaxy26/drain-loop.sh` (nohup on the driver, log `build/drain.log`, stop with
  `touch build/drain.stop`) runs `contest.py drain` every 5 min and sends only rows of the active build.
- **Local screen (rejects only)**: paired self-play vs the incumbent, `research/screen/self1-maps.txt` (40 random maps)
  x both sides, seed 0: `runs/spN_NAME` on the VM (cells `build/spN_NAME.tsv`), scored by `gauntlet.py selfplay
  RUN/results.jsonl --base s2o`: candidate's cell score minus 50, paired t; reject at t <= -1.5. The identity
  control `s2o_id` came out exactly 0 (identical code replays the same game with sides swapped), so no control run.
- **Bytecode tag (replays without indicator strings)**: since 13:20 on 10-28 every build pads its turn so that the
  turn's bytecode count mod 128 is a 7-bit tag (Debug.tag, TAG_K 12 calibrated: 0 misses in 14954 local turns).
  Bits 0-4 are the baby's move code (Baby.why) or the king's spawn code (King.spawnWhy: n s f c b d), bit 5 a
  fighter or a king with enemies in view, bit 6 parity. `tools/smuggle.py REPLAY [--game N]` tells tagged teams from
  untagged ones (parity pass rate) and counts decisions per team. Builds before s3y carry no tag.
- Economy report: `tools/econ_report.py TSVDIR|REPLAY [--hi R]` (each cheese pile from spawn to pickup: per mine its
  side, cheese spawned, taken by each team, wait, rat-rounds within 4, left on the ground; per team own/enemy/middle/
  drops and cheese lost on its own side). `--team vibe26b --hi 200 R1.bc26 R2.bc26 ...` sums every game by mine side
  (own / enemy / middle) for field-wide comparisons on replica replays.
- **Ladder panel (accepts)**: replica, `tools/galaxy26/contest.py`. Panel v2 = 8 field bots s2o beat 25-75% in
  calibration (`tools/panel_pick.py s2o` on games.csv; plus C0derTang_v6 5/6 and bestsofar 1/6 at the edges), 3 maps
  each, both sides: set 1 `research/panel/v2-set1.json` (s2o's reference matches `v2-set1-ref-s2o.txt`, from the
  calibration and v1 games), set 2 `v2-set2.json` (maps drawn as the requests go out; s2o's control queued). 48 games
  a set. Candidate: submit it, `contest.py scrim --vs NAME@+,NAME@-,... --panel research/panel/v2-setN.json --queue
  research/panel/queue.tsv`, re-submit the incumbent; `contest.py fetch --new --pages 30`, then `contest.py paired
  REF_IDS CAND_IDS` (win/loss per cell; accept at t >= 2 with D >= 4, blocks 1-3 reject at t <= 0 / 1 / 1.5).
  On every accept (owner rule, PROMPTS.md #16): `attempts.py set NAME "outcome=accepted ..."`, then
  `tools/progress-refresh.sh`, then commit progress/ and push, before the next candidate goes out.
  `contest.py paired --graded` scores each game by its graded score instead of won (field_report: 1/0 when kings
  decide, else our points share), cached in `<id>/graded.json` with the dumper hash and score version.
  `field_report --builds` names the build per match; rows.py marks rows whose tools changed (`tools_warning`, TOOLS);
  mirror_check exits 2 when a dump fails (cats not counted). GAP: autoscrim can pair us while a candidate is active.
- Ratings and progress: `tools/progress-refresh.sh` (see above); milestones `tools/milestones.py add LABEL`.
- Records: `TRAINING_LOG.md` (per attempt), `progress/attempts.csv` (`attempts.py set NAME outcome=...`),
  `research/progress.tsv` (gauntlet metrics), `PROMPTS.md`. GAP: closed-directions ledger is the attempts outcomes.

## State (see TRAINING_LOG.md for the history)
- Incumbent on the replica: **u0m** (#114), accepted 2026-10-30 12:49 PDT over s9s as a basics repair (accept at
  pooled t >= 0 after block 2): set 4 (3756-3767, 3785-3786) net -3 vs s9s 3284-3297, set 2 (3796-3809) net +7 vs
  u0f 3728-3741 (25 of 42 to 18); pooled 84 cells, D 18, +11 -7, net +4, t +0.94. u0m = `snapvar.py @s9s` +
  'SENSE2_LEFT = 4000, IND_LEFT = 0=>SENSE2_LEFT = 6000, IND_LEFT = 1000' 'FLOW_ALLOC = false=>FLOW_ALLOC = true'
  'FLOW_REV_TURNS = 25=>FLOW_REV_TURNS = 60'. Previous: s9s (#101/#103), accepted 2026-10-30 02:44 PDT over s7d.
- u0m's reign: set 2 carries its accepting luck (+7) and needs a salted twin before anyone is scored there. Set 3
  reference: u0m's block 3819-3822, 3836-3837, 3849-3856, 25 of 42 (outercloud, bc26vibe, CS50Crab 6 of 6 each;
  AlexT101 1, abcdezyxwv 0, spsquared 4, AdamTan12 2). Candidates' block 1 = set 3 (reference that draw),
  block 2 = set 4 (reference 3756-3767, 3785-3786, an unlucky draw, so also score with `contest.py pool`), then set 1
  after a u0m reference block there, set 2 last after a twin.
- Per opponent over the last blocks: abcdezyxwv 1 of 18, AlexT101 1 of 18; outercloud 15, CS50Crab 13, bc26vibe 12,
  spsquared 11, AdamTan12 10. Against abcdezyxwv the rat war is lost: 3764 superioristhmus our rats took 10.3k damage
  from throws and 6.6k from 159 trap fires (theirs 5.7k and 0); their grabs of ours 78-359 a game to our 61-228,
  ~60% from behind, ~35% by the health rule (our rat weaker); the grabber was already adjacent at the end of our
  rat's turn in 60-70%. Their bank holds ~2k all game while they run 3-5 kings and 50-127 babies.
- Replica: u1o (#121) = u0m + RUSH_TO 20 is on its set-3 block 1, 4261-4274; score it with `contest.py paired
  3819-3822,3836-3837,3849-3856 4261-4274`. Next: u1r (u0m + BUG_DIG, a basics repair for pocket starts; screen
  t -0.02 pass), then u1k (THROW_CHECK, neutral). Rejected on set 3 today: u0y (#119, t -1.63), u1p (#120,
  MINE_TRAPS 8, t -1.00), u0o (#118, t -2.33). Rejected locally: FACE_HOLD (u1l, u1m parked, u1n), u1s (corridor
  swap, screen t -1.16). u1q (u0y + TRAP_OPEN_TO 150) is parked: the trap line didn't gain vs the top three.
- Stash pocket (TRAINING_LOG 2026-10-30 19:28): the bug walk treated dirt as wall, so rats waited 20-26 turns each
  for a reverse flow search; BUG_DIG digs through in the sweep (exit open r22 locally). The 1-wide corridor still
  jams afterwards; the corridor swap fixes that locally but loses elsewhere. ~/diag26b/mobility.py REPLAY... compares
  how fast each side's rats leave home.
- Trap war (TRAINING_LOG 2026-10-30 17:24): the top three's traps fire on us ~92% of the time, on our own fight moves.
  u0q's reactive traps lifted K/D vs AlexT101 from 0.76 to 0.98 and vs abcdezyxwv from 0.93 to 1.08 in the field.
  Scripts on the VM: ~/diag26b/kdsum.py LABEL DUMPDIR... (deaths by cause, both sides), trapsrc.py, trapind.py
  (local games: the victim's mv= code at each fire).
- u0q (#115, u0m + BABY_RAT_TRAPS + TRAP_ADJ + TRAP_SPAWN_FIRST) was rejected on set 3, t -1.63 (D 6, +1 -5): 39 traps
  in r0-49 on dirtfulcat starved its king, and on rose it lost on points where u0m killed the king. Every field bot but
  bc26vibe lays rat traps, 30-310 a game, ~95% fire on us (set 2: 3801 constrictinglattice 305 fires, 126 of our
  deaths), mostly as our rat closes from d2 4-20 or steps off an adjacent enemy. Laying our own does not pay; the open
  problem is not walking into theirs. RISK_ADAPT (u0u) cut fires by 4% only (ad1 377 vs adc 391, 7 games), and
  TrapRisk's mark array is never allocated on 60x60 maps (needs 18.8k bytecodes left).
- Mine coverage: we work 1-2 of 3-8 home mines on the big walled maps; the field finds their twins 5-10x sooner
  (wallsofparadis 3800: rings first seen r392-503, field r54-135). u0w = u0m + SEEK_IDLE 40, u0x = u0w + IDLE_SHIFT;
  diags iw1/ix1 vs control iwc (8 maps, both sides, vs s9s), screen ra_u0w queued.
- Submit a candidate only after a block finishes (`contest.py submit bots/NAME -m TEXT`, then
  `contest.py scrim --vs ... --panel research/panel/v3-setN.json --queue research/panel/queue.tsv`; the drain loop
  sends the queue under the hourly limit). Score: `contest.py paired REF CAND`.
- Candidates on the s9s base (screens use `gauntlet.py selfplay RUN/results.jsonl --base araid --control
  build/vmruns/runs/ra_s9s/results.jsonl`):
  - Replica games are deterministic against most of the panel: t0f (s9s's twin, #111) replayed set 4
    (3610-3613, 3718-3727) 42 of 42 cells alike, 38 of 42 games identical in rounds. A candidate's differing cells
    are its own change's chaos plus its real effect, never seed luck; a twin on the same set adds nothing.
  - Set 2 is re-anchored by u0f (s9s + HAND_FLIP, #112): 3728-3741, 18 of 42 (s9s's set-2 draw 2115-2128 was 23 of
    42: D 11, +3 -8, t -1.51). Score a candidate's set-2 block against 3728-3741.
  - On the replica: u0m (#114) = @s9s + u0e's margins (SENSE2_LEFT 6000, IND_LEFT 1000) + FLOW_ALLOC (t0v) +
    FLOW_REV_TURNS 60 (u0k), the basics repair bundle. Block 1 = set 4 (3756-3767, 3785-3786) vs 3284-3297: net -3,
    t -1.00 (D 9, +3 -6); pooled over the 9 set-4 draws 22 of 42 vs 23.4 expected, z -0.82. Block 2 = set 2 from
    3796 (reference u0f 3728-3741). Reading of the repair rule: two blocks without the block-1 reject, then accept
    at pooled t >= 0. Why: on cheesebottles s9s's 12 rushers sit in the walled room by our (20,13) mine from r75 to
    r400, their reverse searches dropped at 25 turns (234 given up a game; u0k 3). fx1 (u0m vs s9s) 7 of 16.
  - Set 4 carries s9s's luck: all 9 candidates scored there against 3284-3297 came out net <= 0 (mean -1.9), and
    s9s's own draw sits ~1.7 wins above the pooled cell rates. Score set-4 blocks with
    `contest.py pool 3284-3297 research/panel/v3-set4-pool.txt CAND` as well as `paired`.
  - u0e (#113) alone: set-4 block net -2, t -1.00 (D 4), 0 overruns in 42 (reference 2); folded into u0m.
  - Next after u0m (all built on u0m): u0n = END_KING 1850 (u0d; ek1 11 of 16 vs s9s), u0r = u0n + END_KING_MSITE
    (u0i's END_KING_MINE was inert: clear3 wants king-seen tiles; er1 running), u0s = KING_FORAGE (27 of 78 king
    deaths in 50 replica matches were starvation, 16 with no enemy damage; fg1 vs fgc running on the starvation
    maps), u0o = RUSH_TO 12 (t0t), u0p = RUSH_STICK. u0q = BABY_RAT_TRAPS + TRAP_ADJ + TRAP_SPAWN_FIRST: weak
    (tq1 6 of 16; traps hit 91% but it spawns fewer). u0j (reactive traps on s9s) weak, tj1 7 of 16.
  - Dropped: u0h (HOME_ALL_SYM, hs1 6 of 16), u0b (RUSH_STICK, st1 8 of 16). Parked: u0c (BitFlow, bf1 5 of 16).
    u0l = u0k + RUSH_STICK (rushers stop flipping between the two king spots; cb4: 4 in the room at r100, u0k 11).
  - Rejected: t0p (MINE_ANY + MINE_TIES) on set 4, net -2, t -1.00.
  - Rejected: t0i (FORM_MINE) on set 4, net -3, t -1.00; t0o (MINE_TIES) on set 4, net -1, t -1.00; t0g (RUSH_LIFE 150) on set 4, net -4, t -2.00; t0h (FORM_RING) on set 4, net -1, t -0.45 (evileye: two kings starved); t0c (KING_TRAPS) on set 4,
    net -1, t -0.45; s9z (BREAKOUT_TTL 150) on set 4, net 0; t0b (RISK_FRESH 2)
    at its screen, t -1.73. Dropped: t0d (t0c + BABY_RAT_TRAPS).
  - Screens (runs/screenq.txt; ra_t0p, ra_t0j, ra_t0v running), then ra_t0r, ra_t0t, ra_t0n, ra_t0l, ra_t0m:
    - t0e = @s9s + RISK_ROWS (screen done: 80 cells, t -0.75, a weak pass).
    - t0i = @s9s + FORM_MINE (extra kings on home mines, no income test for mine sites; fm3 10 of 16 vs s9s).
      t0j = t0i + FORM_MAX 3. t0m = @s9s + FORM_MINE + FORM_RING + FORM_MAX 4 (fm4 9 of 16, delivers 11.9k to 14.8k).
    - t0k = @s9s + CAT_LAY (cat traps on the cats' lanes in cooperation; cy1 10 of 16 vs s9s, trap damage 1424 to
      1169). t0n = t0k + CAT_RING (cy2 11 of 16).
    - t0l = @s9s + ATK_CARRY 25 (attackers stay light).
    - t0p = @s9s + MINE_ANY + MINE_TIES (gatherers over every known mine, either half; ma1 9 of 16).
    - t0q = @s9s + SYM_FLOW + SYM_CENTRE + SYM_HAND + SYM_FACE (side symmetry; s9s diverges at r2 on Stash; run sym1).
    - t0r = t0q + SYM_TIE + FLOW_QUOTA 18 (t0q still split at r27 superioristhmus on a tied spawn tile and r38 Stash
      on flow search depth; self-mirror run sym2, screen queued). Mirror checks: `tools/mirror_check.py F F` on a
      self-play replay; knifefight splits at r4 on turn order (first to move bites), which no bot change removes.
    - t0s = @s9s + DELIVER_AT 50: dropped (dl1 7 of 16 vs s9s).
    - t0t = @s9s + RUSH_TO 12 (8 rushers; rz1 10 of 16 vs s9s, screen queued). t0u = RUSH_TO 20: parked (rz2 9 of 16).
    - t0v = @s9s + FLOW_ALLOC (a bug fix: flow arrays allocated a piece at a time, so busy rats start their searches;
      al1 vs s9s 5 of 10 so far, screen ra_t0v t +1.41 at 30 cells). t0w = t0r + FLOW_ALLOC. Likely next submission.
  - t0f = twin of s9s from current code, for re-anchoring sets 2/3/1.
- Owner rules: assume every map is symmetric in all aspects (PROMPTS.md #14); refresh and commit progress/ after
  every acceptance and every scored replica block (#16).
- Local machine: 2 GB RAM, 2 cores; one replay analysis at a time. Volume games run on the VM (arena.py matrix,
  screen queue runs/screenq.txt). Field bots play only on the replica.
