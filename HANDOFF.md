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

## State (2026-11-06 05:12 PST; TRAINING_LOG.md has the history, older states are in this file's git history)
- Incumbent: **u6x** (accepted 2026-11-03 09:21 PST over u6a; pooled 93 cells D 5, net +5, t +2.24). The u9p line is
  u6x + CAT_LAY + CAT_RING + TRAP_SHARE false (rejected at block 3, pooled t 1.18); most candidates add one switch to
  it, screened against u9p's zd61.
- **On the replica: z1l** (u9p + SWAP_LOADED 30, SWAP_HOME 10), submission 237, set-3 block 1 sent after match 17649
  (the hourly limit leaves the rest to the drain loop, tools/galaxy26/drain-loop.sh, build/drain.log). Waiter
  build/wait_z1lb1.sh: 16 OK after 17649. Score with `paired 13248-13291 IDS --crown 12130-12163`. Block 1 rejects at
  t <= 0. Then set 1 (build/block_v4s1.sh), set 2, set 4.
- **Replica order after z1l:** **z0k** (FORM_WAR_FROM 300, reopened: its screen rejection was a per-rat-turn
  denominator artifact; falsifiers_v2 in its SNAPSHOT), **u7z** (u6x + SEEK_IDLE 20 + IDLE_SHIFT + FIGHT_SHIFT,
  revived: screen vs u6x t +2.46; on the incumbent base), **u9t** (u9p + THROW_ADAPT + SEEK_CROWD 3, revived),
  **z0r** (u9p + CAT_HUSH, passed zd110). Held: z0d (STAB_LAY; per-round picks z -1.76). Demoted: z0e (per-round
  picks z -2.63). Submit: `contest.py submit bots/NAME -m TEXT`, `bash build/block_v4s3.sh`, `git checkout research/panel/`.
- **Screen gates (method change 05:06):** s6 also rejects at per-round picks z <= -2 (scratch ppr.py, copy in
  ~/diag26b: `python3 ~/diag26b/ppr.py zd61 u9p RUN BOT` on the VM); a switch that changes the number of formations
  is judged per round only. ~/diag26b/sweep.py lists every zd run against zd61 with all the paired measures.
- Closed this session (05:00-05:12): z0q (block 3, pooled 144 cells t 0.78), v0a (dg chain: cat damage on our
  kings not lower). Diagnoses: enemy trap deaths near our king are the opening brawl against rushers (young,
  never-carried rats walking out among 4+ enemies); DefaultMedium 0 of 10 (early-war merges, army collapse);
  thunderdome vs nzjt is a throw race (they throw 2-3x as often; king hits by thrown rats).
- Mechanism chain still running on the VM: **~/diag26b/cs** (z1o CHEESE_STICK, z1p + FLOW_PROGRESS vs u9p on Stash,
  seepastyourgreed, rift, pipes vs abrk; falsifiers in their SNAPSHOTs: Stash first-middle rounds (firstexit.py)
  10+ earlier in both seats, r300 transfer, wins). On DONE: if one passes, cells zdNNN + gates + queue.
- Screening (VM runs/screenq.txt): **zd128 z1q** (u9p + u7z's three switches; inert below per-round pickupAmt ratio
  1.01), zd120 z1e (running), zd112 z0t, zd113 z0u, zd114 z0v, zd107 z0p, zd108 z0o, then the old sweep. zd127 z1l
  passed (t -0.57, transfer 0.991).
- Cat-bite cheese (03:20-04:02, closed): a bite with c cheese does 10 + ceil(sqrt c). z1h (attackers dump their
  load) was inert; z1i, z1j (cooperation, 2 or 5 a bite) and z1k (war, 2 a bite at bank 800+) all fired but cut summed
  cat damage: the cheese bites come with fewer cat-trap hits (10 cheese buys 100 damage). Per-bite damage = summary
  catDmgBite / bitesCat (actions.tsv bite rows carry a lethal flag, not the cheese).
- Replica losses (04:02 entry): king kills dominate (240 recent games: ending <500 52, <1200 49). Early ones: starve
  28, bite 12, thrown rats 10; reserve and anti-throw doctrines are closed (u6e, u9i, u1k, u7b, u7i). The rose stem
  jam (king starves with 600-1,040 raw within 10) is **z1l** (u9p + SWAP_LOADED 30, SWAP_HOME 10): mechanism games
  in VM ~/diag26b/sl (from 04:01), falsifiers in bots/z1l/SNAPSHOT; screen zd127 has its cells made. FreeTheCheese vs
  collegejunior is the same stem shape (one delivery tile at the stem mouth), but there the army had shrunk first.
- Mechanism-chain tables (scratch, copies in ~/diag26b): sltab.py DIR, jamchk.py, firstexit.py, mvtally.py,
  atkjump.py, dgcheck.py DIR (cat damage on our kings, wins, exceptions per local game dir), trapctx.py, race.py.
  Done: sl2, mz, wc, fw, dg.
- Diagnoses 04:35-04:50 (TRAINING_LOG): we field more babies than in-band opponents early but pick 0.18-0.26 per
  baby-round to their 0.36-0.50 (12 of the first 16 are rushers; the rush family is closed). Cats killed our king in
  10 of 201 replica losses (bed vs spsquared B: the king fled ahead of a cat into a corner on the cat's waypoint path).
  Stash: gatherers in the walled home strip switch cheese targets every few rounds as piles beyond the wall come
  nearer, dropping flow paths the round they are ready; attackers lose paths at blockers in the 1-wide comb.
  uneasy_alliance vs Icarus: they lay ~54 rat traps on our approach tiles by r250 (trap-risk family closed).
- VM disk: 2.4 GB free (05:03; prune build/dumpcache entries older than 3 h when it nears 2 GB: dirs are read-only,
  chmod u+w first; regenerable from ~/diag26b/rp). Replica rows: ~/diag26b/{z0qb1,z0qb2,z0qb3,twin_s1,twin_s2,z0gb1,
  z0gb2}.jsonl. Each candidate's falsifiers are in its bots/NAME/SNAPSHOT; the screen maps hold no lone-king map.
- King lever (02:14-03:30 in the log): one more king at r2000 swings the war points margin by 16.7 and flips 17 of
  38 war points losses. Passed their mechanism tests and queued: **z1a** (FORM_PACK: ring rats make room so joiners
  reach a corner site's far tiles; refusals 535 -> 164), **z1c** (FORM_RING_FROM 400: a ring site beside a corner
  king; clouds second king r647, but evileye lost faster with both kings side by side), **z1e** (END_PULL_R2 10000:
  from r1850 babies join from any distance; uneruesansfin second king r1913). **z1g** (END_KING_DYN_FROM 1890: from r1890
  END_KING's bar min(400, 80 + 4 * max(0, R - 55)); a king formed with 55 rounds left lives to r2000 on an empty bank;
  met in all 12 lost lone-king replica points games, 9 of which never had 400) was dropped at its mechanism games
  (two kings in 1 of 3 bar-met games; gaps: king survival after forming, no site on narrow maps). Those that pass their screens can go as one hand-merged candidate (they act on
  different code; tools/bundle.sh NAME BASE CAND... appends Cfg lines and patches the rest strictly, tested on z1a-z1d).
- Replica scoring (twin-anchored; pass explicit match IDS, others' matches sit in the ranges):
  set 3 `contest.py paired 13248-13291 IDS --crown 12130-12163`; set 1 `paired 12771-12813 IDS --crown 11549-11677`;
  set 2 `paired 12838-12899 IDS --crown 11678-11702`; set 4 `paired 16848-16863 IDS` (u6x reference, 28 of 48).
  Order: sets 3, 1, 2, 4. Block scripts `build/block_v4sN.sh` (they touch research/panel/*.json: `git checkout
  research/panel/` after). IDS: `contest.py matches --pages 8 | awk '$1>LAST && $2=="OK!" && $3=="unranked" &&
  ($4=="vibe26b"||$7=="vibe26b")'`, then `contest.py fetch --new --pages 8`. Transfer floor (u9p family): field_report
  xUs per game >= 0.9x the twin (set 3: 3,968; set 1: 4,133): rsync the 16 match dirs to the VM's ~/diag26b/rp/
  and run `tools/field_report.py games --ids LO-HI --csv` there (never locally: the laptop disk filled at 100%).
- Closed 2026-11-06: z0z (FORM_KEEP + END_KING_SAVE: inert on the lk maps), z1b (z0z + END_PULL: mechanism bar), z1d (FORM_NET100_COOP: mechanism), z1f (END_KING_DYN with a health condition: never fired), z0h (FORM_RICH_BANK 1500: mechanism run zd98, king deaths and wins), z0x and z0y (formation refusal bar), z0g (PEACE_FROM 100: block 3), z0s (RAW_RAID: transfer 0.950), z0j (LAY_MAX: mechanism), z0k (FORM_WAR_FROM 300: picks z -3.26), z0l (SIEGE_STAY 20:
  transfer gate 0.952), z0m (RETREAT_HP 20: wins gate), z0n (END_CAT without gatherers: transfer 0.946). Closed 2026-11-05/06 on the u9p base: u9q (RETREAT_HP 0: transfer floor), w0b and z0b (inert), w0c (kings
  falsifier after block 2; its replica games replayed u9p's), z0a (RETREAT_CARRY: transfer gate), z0f (END_CAT +
  STAB_LAY: picks z -2.46). The replica is deterministic: a switch that never fires replays its parent's games.
- Local screens (VM): `runs/xt2/cells.tsv` with bots/u5n replaced, `arena.py matrix ... --jobs 3` (~35 min with the
  replica sharing the 8 cores). Score with `build/score_screen.sh RUN BOT [CTRL_RUN CTRL_BOT]`. Controls: zc6 (u6x,
  32 of 44, picks/100 35.56), zd61 (u9p, 38), zd62 (u9q, 40). Basics gate (14:27 rule): overruns/exceptions, picks/100
  below 33.8, D 0. Queue: VM `tools/screenq.sh 3` pops run names from runs/screenq.txt (insert at the front with
  `flock runs/screenq.lock sed -i '1i zdNN' runs/screenq.txt`; cells first: `sed 's#bots/u9q\b#bots/NEW#g'
  runs/zd62/cells.tsv > runs/zdNN/cells.tsv`). Gates: research/screens/gates.tsv, rsynced to VM runs/gates.tsv, read
  every minute by `tools/abort_watch.py runs/gates.tsv --follow` (stops a screen once its wins gate is unreachable).
  Score with `bash tools/score_screen.sh RUN BOT zd61 u9p`. Waiters: `tools/wait_run.sh RUN`; kill by pid only (a
  pkill/pgrep -f once matched the ssh shell).
- VM disk: the replica's replay store grows ~2.4 GB a day; prune replays older than ~36 h by hand (`find ... -mmin
  +2160`, list to /root/bc26-replays-pruned-*.txt) whenever `df -h /` shows under 2 GB free. An hourly root cron for
  this was not installed (the harness denied the persistence; the owner decides).
- Recent evidence (TRAINING_LOG 2026-11-05): u9p's 59 king-kill losses: the last king starved in 34 (after the army
  collapsed), bite 16, throw hits 7; enemy rat traps killed 805 of ours, ours 0. u9q's set-3 fast losses are
  grab-and-throw wars on small maps (tiny vs waxmango: their grabs 108 to our 67, 39% of our grabs ended with our
  carrier grabbed: v1a GRAB_SAFE) and reactive-trap fields (wallsofparadis vs AdamTan12: 74 traps, 72 fired,
  mean life 3.1 rounds, 26 of our rats). Merlin out-farms us 2-3x on FreeTheCheese/cheesebottles.
- Closed on this base: SEEK_CROWD and THROW_ADAPT (u9r, u9s, u9t: picks bar), FORM_RICH_MAX 5 (w0a inert).
  Long closed: PEACE (u3w, u3z, u4l, u6h, u6w), RUSH_TO, RISK_FRESH/RISK_ADAPT, FACE_HOLD, WOUNDED_HP.
- Owner rules: never ask permission to try an idea, never stop trying (PROMPTS #27); never leave the VM idle while
  the replica plays (#28); maps symmetric (#14); refresh and commit progress/ after every accept (#16).
- Local machine: 2 cores, 1 GB RAM, ~1 GB disk free: replay analysis runs on the VM. Volume games run on the VM;
  field bots play only on the replica.
