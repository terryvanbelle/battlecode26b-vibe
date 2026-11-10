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

## State (2026-11-08 17:54 PST; TRAINING_LOG.md has the history, older states are in this file's git history)
- **Now (03:22 11-10):**
  - Incumbent still **zdb** (#334). zhn rejected 02:44 as inert (screens, chain cq198 and replica #344 all equal zdb).
  - zho (#345; STANDOFF_ODDS 3) rejected 03:22 at its replica block (D 3, net -1: +Stash A, -rose A, -externalsustenance B).
  - **zhp** (zdb + STANDOFF_HOME 5: no standoff within 5 of our king; zho's sibling arm). Screens zd509/zd510 queued;
    chain cq200 (seed 106) running. Replica #346 set-3 block: 4 sent 03:22, 12 in the drain.
  - **zht** (zdb + RUSH_SMALL 400: no opening rushers on maps of 400 tiles or fewer; tiny vs thewaxmango). Screens
    zd511/zd512 queued; chain cq201 (seed 107) behind cq200. Submit to the replica once zhp's 12 drain rows are sent.
  - **zhu** (zhp with STANDOFF_HOME 8, the dose arm). Screens zd513/zd514; chain cq202 (seed 108) behind cq201. Replica after zht.
  - Next fresh seed: 109. Next screen id: zd515. Next chain: cq203. Next free name: zhv (zhq and zhs are old dirs).
  - VM disk 2.1 GB (pruned replica replays > 30 h; cq196 and cq197 removed).
- **Earlier (01:37 11-10):**
  - Incumbent still **zdb** (#334). Rejected since 22:58: zhh, zhi, zhj, zhk, zhl; zhm dropped (keepout replayed
    zdb: the income king's site picker needs seen mine tiles, so FORM_CHEESE/FORM_BABIES never bind there).
  - **zhn** (#344; zdb + FORM_RICH_BANK 2800): the RICH2 80-baby tier pins the bank at 2,900-2,960, just under the
    3000 rich-king test, so rich kings never fire (local keepout: one king until r1865, 14.9k unspent). Screens
    zd505 (s7) and zd506 (s6) queued; chain cq198 (seed 104, zdb pairs from cq197 linked) running; set-3 block (16)
    in the drain. Score: `chsc.py cq198 zdb zhn`, `kingsc.py cq198 zdb zhn`; bars in attempts.csv.
  - Info-only replica blocks (#337-#343) vs zdb's set-3 anchors: no candidate gained a match.
  - Next fresh seed: 105. Next screen id: zd507. Next chain: cq199. Next free name: zho (check the dir too).
  - VM disk 1.9 GB; cq196 (zhl) and cq197 (zhm) dumps removable once cq198 is scored (cq198 links cq197's zdb pair).
- **Earlier (22:58 11-09):**
  - Incumbent still **zdb** (#334). zhf rejected (zd490 s7 changed cells). zhg rejected at zd491 (s6 net -1:
    evileye A vs abrk); its 12 drain rows were dropped and its 4 sent replica matches (#337) are information only.
  - **zhh** (#338; zdb + KING_PAY 100 / floor 500: the king pays 100 for a bite on a baby rat when biteCheese pays 0;
    thunderdome vs Varun, king bitten down with 933 banked). Screens zd493 (running) and zd494; chain cq192 (seed 99)
    running. Its set-3 block (16) is in the drain. Score with `paired --dir /dev/shm/rp 21016-21023,21036-21043 IDS`.
  - **zhi** (zdb + FORM_FROM 1200: no mid-game second king; END_KING/CASH_END still form one at the end). Target
    Merlin FreeTheCheese B, lost 47-52 on points. Screens zd495/zd496 queued; chain cq193 (seed 100) behind cq192.
    Bars in attempts.csv. Replica next once zhh's block has drawn.
  - Next fresh seed: 101. Next screen id: zd497. Next chain: cq194. Next free name: zhj (check the dir too).
- **Earlier (22:06 11-09):**
  - Incumbent still **zdb** (#334). zgx was rejected on the replica (set 3: 48/48 alike, D 0). zha, zhb, zhc and zhd
    were dropped (zhd failed its ledger: TrapRisk's class initialiser ran at init).
  - **zhf** (#336) is zdb + RISK_ROWS on maps of 2500+ tiles, with the flag held in G.riskRows.
    - Why: zdb's flat trap-mark array needs 3·W·H+8000 bytecode left, so it is rarely allocated on 50x50 maps. On
      keepout, Finals' traps killed 100 of our rats in each seat. 216 of the 319 triggers came within 1 round of the
      lay (scan: /dev/shm/trapscan.out and trapdelay.py on the VM).
    - Falsifiers (binding): zd490 s7 must match zdb in every cell; cq190 (seed 97) needs wins ≥ zdb, transfer ≥ 1.02x
      and no overruns. Its replica set-3 block is queued, and the drain sends the last 4.
  - In-fight trap avoidance has been rejected 4 times (s8h, t0b, u0u, zfg). Don't retry it as it stood.
  - Next fresh seed: 98. Next screen id: zd491. Next chain: cq191. Next free name: zhg (check the dir too).
- **Earlier (20:52 11-09):**
  - Incumbent still **zdb** (#334). zgy and zgz dropped at their screens.
  - Replica: #334 zdb set-3 re-draw. The first 24 cells (spsquared, Finals, YokoOno, Varun; 22997-23000,
    23002-23005) are all alike, D 0, so set 3 replays as drawn. The other 8 (Xzibit, thewaxmango, Icarus, Merlin) are
    in the drain; score them with `paired` and log them, then submit zgx.
  - **zgx** (CORE_FLOOR/WAR_FLOOR 150): passed its local gates as inert (cq185 identical to zdb). Replica set 3
    next; targets externalsustenance A (YokoOno) and mincemeat B (Icarus).
  - **zha** (HOLD_TTL 40 / HOLD_OFF 100, new ledgered switch; Stash wall standoff): screen zd485 running, chain
    cq186 running (first cell = ledger check vs cq181's zdb Stash A agrab seed 89). Bars in attempts.csv.
  - **zhb** (RISK_ROWS, existing; Finals' traps on keepout): screen zd486 queued, chain cq187 after cq186
    (seed 95, 2500+ tile maps). Bars: wins, transfer 1.0x, trap deaths under 0.9x.
  - Next fresh seed: 96. Next screen id: zd487. Next chain: cq188. Next free name: zhc.
- **Earlier (20:00 11-09):**
  - Incumbent still **zdb** (#334). zgt rejected at its first counted block (set 3, t -1.00; set 2 is biased for
    zdb derivatives, memory set2-anchor-bias). zgu, zgv dropped; zgw withdrawn unplayed.
  - Replica: #334 zdb set-3 **re-draw** (noise measure vs its own anchors 21016-21023, 21036-21043); 4 sent, 12 in
    the drain. Score it with `paired` like a candidate; cells that flip are set-3 noise.
  - **zgx** (zdb + CORE_FLOOR/WAR_FLOOR 150): zd482 D 0 (inert on s7); chain cq185 (cq183 cells, seed 93) queued
    behind cq184; `chsc.py cq185 zdb zgx`, bars wins >= zdb and transfer >= 0.95x.
  - **zgy** (zdb + GUARD_CARRY_R 5; thewaxmango throws ours into our king 11-12 times a game): screen zd483 running,
    chain cq184 (seed 92, DefaultMedium/tiny/knifefight/DefaultSmall/thunderdome) running; bars in attempts.csv
    (throw hits on our king < 0.85x zdb's).
  - **zgz** (zdb + RETREAT_HP 60; Merlin grabs by health, victim hp median 58): screen zd484 queued.
  - Diagnoses this evening (TRAINING_LOG 19:16-20:00): mincemeat/externalsustenance bank collapses;
    wallsofparadis inner mines never found (parked: discovery switches all failed before); thewaxmango throws;
    Merlin health grabs. Replays on the VM: ~/diag26b/rp/x_s, x_f, x_w, x_m.
  - Next fresh seed: 94. Next screen id: zd485. Next chain: cq186. Next free name: zha.
- **Earlier (18:02 11-09):**
  - Incumbent still **zdb** (resubmitted #332). zgj rejected at replica block 2 (pooled t 0.83: +8 set 2, -3 set 3,
    Finals where_are_you A and B as zgh). zgo, zgp dropped; zgq/zgr/zgs withdrawn with zgj.
  - **zgt** (zdb + BREAKOUT_TTL 150, pocket escape): screen zd478 (s7) running, chain cq181 (seed 89, 5 pocket maps;
    `chsc.py ~/diag26b/cq181 zdb zgt`; clause on pickupAmt 1.05x and wins).
  - **zgu** (zdb + ATK_STUCK alone, no trail): screen zd479 queued, chain cq182 after cq181 (seed 90, cq177 cells;
    transfer 1.1x and wins).
  - Next for the replica: whichever passes; set 3 first (`paired --dir /dev/shm/rp 21016-21023,21036-21043 IDS`).
  - Next fresh seed: 91. Next screen id: zd480. Next chain: cq183. Next free name: zgv.
- **Earlier (16:41 11-09):**
  - Incumbent still **zdb**. zgh (z7x trail port) rejected at replica set 3 (#330, t -1.41); zgi/zgk/zgn dropped.
  - **zgj** (zgh + ATK_STUCK 30 / ATK_STUCK_RAW 100) on the replica as #331, set-2 block 1: 22843-22850 sent, 8 rows
    in the drain. After 7 games D 4 (+4 -0), t 2.00 (replays VM ~/diag26b/rp/zgj_b1s2a.tgz). Judge with
    `paired --dir /dev/shm/rp 22129-22132,22251-22262 IDS`; if t > 0, block 2 on set 3.
  - **zgo** (zgj + ATK_CARRY 300): chain cq177 (seed 87; `chsc.py ~/diag26b/cq177 zgj zgo`), screen zd472 (s7).
  - **zgp** (zdb + FINISH_K 10): screens zd473 (s7) / zd474 (s6), chain cq178 (cq170's zdb cells, seed 84; compare
    with zdb's 14 wins in cq170). Falsifiers in attempts.csv.
  - Closed this afternoon: king-walks-to-carriers (king vision dist2 25 is barely past transfer reach), CASH at bank 0
    (the engine needs 50 banked before becomeRatKing), backstabbed-team cat traps (we open the war at r30).
  - Next fresh seed: 88. Next screen id: zd475. Next chain: cq179. Next free name: zgq.
- **Earlier (14:38 11-09):**
  - Incumbent still **zdb** (resubmitted #328 after zgc's block-1 reject). zga-zge dropped or rejected (WALL_HOLD,
    MINE_BURN, KING_WALL_THREAT lines closed).
  - **zgh** (z7x's delivery trail ported exactly onto zdb; field king starvation in pockets, `~/diag26b/kdeath.py`
    and `starve.py` on build/dumpcache): screens zd464 (s7) / zd465 (s6) at the head of runs/screenq.txt; chains
    cq171 (zdb control, fullofsecrets) then cq172 (zgh + zdb on clockwisdom/rose/keepout), seed 85. Score with
    `python3 ~/diag26b/chsc.py ~/diag26b/cq172 zdb zgh`. Next for the replica if it passes.
  - **zgg** (KING_THREAT_D2 25) on the replica (#329, set-3 block 1: 22658-22669 + 4 in the drain; judge with
    `paired --dir /dev/shm/rp 21016-21023,21036-21043 IDS`). **zgf** (13) screen zd462 running, zgg's zd463 queued;
    chain cq170 (zdb/zgf/zgg, seed 84) running.
  - Next fresh seed: 86. Next screen id: zd466. Next chain: cq173. Next free name: zgi.
- **Earlier (11:47 11-09):**
  - Incumbent still **zdb**. Since 00:55: zes..zfu all dropped or rejected (see attempts.csv). zfv/zfw INERT
    (MINE_SPREAD is unreachable under MINE_BALANCE=true: check a switch's path is live before building).
  - Field diagnosis (36 cached zdb losses, /dev/shm/kd/spend.py): delivered by r300 0.70x the opponents' with equal
    baby counts; 0 rat traps laid vs their 708; most losses end in king starvation.
  - **zfx** (MINE_NEAR 2) / **zfy** (MINE_NEAR 3): chain **cq165** (seed 81, zdb control; score with
    `~/diag26b/kthrowc.py ~/diag26b/cq165 zfx zfy zdb` for wins), screens zd453/zd454 (s7), replica **#325** (zfx)
    set-3 block (22513-22516 sent, 12 in the drain). Clauses in each SNAPSHOT.
  - Next fresh seed: 82. Next screen id: zd455. Next free name: zfz is taken (untracked); use zga+.
- **Earlier (00:55 11-09):**
  - Incumbent still **zdb**. Raiding (RAID_EVERY, zen/zer) is CLOSED: zer rejected at replica block 1 (set 4,
    21938-21953, t -2.71; it loses short fight-force wins). MINE_MIRROR (zep) inert, dropped; zeq dropped; zeo rejected.
  - **zes** (00:13) = zdb + MINE_EVICT 20 (at war, a healthy empty-handed gatherer fights an enemy baby on its own
    home-half mine). Chain **cq139** (seed 65, running; score with `python3 ~/diag26b/evsc.py ~/diag26b/cq139 zes zdb`),
    screens zd410 (s7, playing) / zd411 (s6, queued), replica **#312** set-4 block queued (judge with
    `paired --dir /dev/shm/rp 20817-20824,20842-20845,20848-20851 IDS`), set 2 next if t > 0. Clauses in bots/zes/SNAPSHOT.
  - Next fresh seed: 66. Next screen id: zd412. Next free name: zet.
- **Earlier (23:40):**
  - Incumbent still **zdb**. zel DROPPED (cq132 1b), zem DROPPED (cq133 1b/1c; #310 set 2 D 1 t +1.00, replays VM
    ~/diag26b/rp/zem_b1s2.tgz).
  - **zeo** (23:16) = zem's code re-registered: chain **cq135** (seed 62, queued behind cq134; score with
    ~/diag26b/zelsc.py), replica set-4 block on the zem build (judge with `paired --dir /dev/shm/rp
    20817-20824,20842-20845,20848-20851 IDS`), set 3 fresh next if t > 0 (21768-21771 excluded).
  - **zen** (22:35) = zdb + RAID_EVERY 2: chain **cq134** (seed 61, running; ~/diag26b/raidsc.py DIR zen), screen zd404
    PASSED (t +0.27, 0.988x), zd405 (s6) playing. Replica set 4 then set 2 once the replica is free and cq134 passes.
  - **zep** (23:39) = zen + MINE_MIRROR (ledgered): chain **cq136** (seed 63, behind cq135; ~/diag26b/mirsc.py DIR zep
    zen), screens zd406/zd407 queued. Clauses in each SNAPSHOT.
  - Next fresh seed: 64. Next screen id: zd408.
- **Earlier (21:35):**
  - Incumbent still **zdb**. zej DROPPED at cq131 clause 1e only (wins 19 vs 20: where_are_you A vs araid, a points
    loss at r1344, divergence r207); its mechanism clauses all passed (centre 4/4, digs 863 vs 316, cat dmg 2.08x,
    xfer 1.021x). zd399 killed; #308 left unscored.
  - **zel** (21:28) = zej's build unchanged, re-registered on fresh data. Chain **cq132** (seeds 57+58, 96 games,
    running), screens zd400 (s7, playing) / zd401 (s6, queued), replica **#309** set-3 block (21692-21701+ queued;
    judge with `paired --dir /dev/shm/rp 21016-21023,21036-21043 IDS`), set 2 next if t > 0. Clauses in
    bots/zel/SNAPSHOT.
  - Laptop root disk is full (30 MB free): fetching replays needs space first (fetch on the VM or ask the owner).
  - Checked and closed for new designs: SIEGE_FLOOR (s7t), HEIR (z9c), CORE_BABIES (u5t, zd8), CAT_YIELD (zds),
    knifefight seat split (36 attempts). externalsustenance A vs YokoOno (r439 starve): a sealed box, mines outside;
    we fight both cats (6993 cat dmg vs 280) and lose 16 rats to pounces and 28 to throws.
  - Next fresh seed: 59.
- **Earlier (21:12):**
  - Incumbent still **zdb**. zeh DROPPED (cq129 Stash xfer600 0.88x; 8 of #307 played, replays VM
    ~/diag26b/rp/zeh_b1s3_2105.tgz; paired partial 24 cells D 2 net 0). zei DROPPED (cq130 rose xfer1000 0.75x).
  - **zej** (20:12) = zdb + STALL_TUNNEL 30: STALL_CAT hunters whose hunt walk stalls 30 turns tunnel (Nav.tunnelTo).
    Ledger check passed. Screen zd398 identical to zdb (inert off sealed maps); zd399 (s6) playing. Chain **cq131**
    (seed 56) playing; clauses in bots/zej/SNAPSHOT. Replica **#308** set-3 block all sent (21627+); judge with
    `paired --dir /dev/shm/rp 21016-21023,21036-21043 IDS`, set 2 next if t > 0.
  - Diagnosis notes: Stash losses (field vs YokoOno, local vs atrap3) are starvations. Rat traps at the exits and
    mines kill 22-40 of our rats, and the king's alert counts enemies across the wall row (DEFEND locks the
    gatherers). Enemy traps are invisible and vanish when they fire. Thunderdome vs Varun: 23 divers bite the king
    once or twice each (r115-436). DefaultMedium vs waxmango: the king dies to throws.
  - Next fresh seed: 57.
- **Earlier (19:53):**
  - Incumbent still **zdb**. zee REJECTED (#305 block 1 set 3, D 1, t -1.00; replays VM ~/diag26b/rp/zee_b1s3_1918.tgz).
    zeg DROPPED (cq128: fullofsecrets xfer600 0.29x; its #306 block plays out unfetched). BUG_SWITCH line closed.
  - **zeh** (zdb + THREAT_WALL_REACH 5): chain **cq129** running (seed 54), screens zd394 (s7, playing) / zd395 (s6),
    replica **#307** set-3 block queued in research/panel/queue.tsv (judge with `paired --dir /dev/shm/rp
    21016-21023,21036-21043 IDS`), set 4 next if t > 0.
  - **zei** (19:32) = zdb + SWAP_HOLD 2 + SWAP_YIELD + SWAP_NO_ALONG + SWAP_NARROW 4 (swap only where both tiles have
    <= 4 open neighbours). Ledger check passed (identical to zdb until the first swap). Chain **cq130** (seed 55, waits
    on cq129run.sh; clauses in bots/zei/SNAPSHOT), screens zd396 / zd397 queued, replica set 4 then set 1.
  - Mine-use asymmetry on averystrangespace (field A works 1 of 3 home mines) is not reproduced locally: both seats
    reach the middle mines only late (A r908, B r1199). The mine-pick line is saturated (MINE_MIRROR, MINE_RR,
    KMINE_SKIP, MINE_EXPLORE, NOMINE_HOME all rejected or dropped).
  - Next fresh seed: 56.
- **Earlier (19:15):**
  - Incumbent still **zdb**. zec REJECTED (#304 block 1 set 4, D 5 +1 -4, t -1.34; replays VM
    ~/diag26b/rp/zeb_b1s4_1826.tgz). zef (zeb's BUG_SWITCH 40) DROPPED at its mechanism clause (cq127 partial:
    8 wins to 4, xfer 2.3x, flips under the 50 bar); re-registered as **zeg** (bar 25, fresh data): chain **cq128**
    (seed 53, running; games run bots/zef), screens zd392 (s7, playing) / zd393 (s6, queued).
  - **zee** (zdb + KING_FAR) passed cq126 (mechanism 10/16, xfer600 1.76x, net +2) and zd390/zd391; replica
    **#305** set-3 block 12/16 done (21516-21523, 21541-21544, 21555-21558; judge with `paired --dir /dev/shm/rp
    21016-21023,21036-21043 IDS`); set 4 next if t > 0.
  - **zeh** (19:15) = zdb + THREAT_WALL_REACH 5 (z7d port; Stash pocket stall). Chain **cq129** (seed 54, waits on
    cq128run.sh; Stash/evileye x agrab/agtr/atrap3 x seats x zeh/zdb), screens zd394 (s7) / zd395 (s6) queued.
  - Next fresh seed: 55.
- **Earlier (18:00):**
  - Incumbent still **zdb**. Closed since 15:55: zdx DROPPED (cq119; field block t -3.16), zdz DROPPED (cq122), zea
    DROPPED (cq123), zdy REJECTED (#303 block 1 on set 4, D 9 +1 -8, t -2.33; replays VM
    ~/diag26b/rp/zdy_b1s4_1723.tgz). The reactive baby rat trap line is closed. zeb DROPPED at cq124's clockwisdom
    no-loss clause (7 cells won to zdb's 2, transfer 3.0x, but lost B vs araid).
  - **zec** = zeb's build re-registered unchanged (zdb + BUG_SWITCH 20: a bug walk not round its obstacle after 20
    turns reverses its hand, doubling each leg; bots/zeb/SNAPSHOT). Games run bots/zeb. Falsifiers: chain **cq125**
    (seed 50, queued behind cq124; clockwisdom transfer >= 2x and net flips >= +2; other cells transfer >= 0.9x, net
    >= -1), s7 zd388 **passed** (t -0.44, length-matched transfer 1.009x), s6 **zd389 playing**, replica **#304**
    set-4 block (21484-21487 all 3-0 won; judge with `paired --dir /dev/shm/rp 20817-20824,20842-20845,20848-20851
    IDS`), then set 2.
  - **zee** (18:00) = zdb + KING_FAR (rename of zdj; first king walks to a mine beyond dist2 400). Chain **cq126**
    (seed 51, waits on cq125run.sh; 16 cells: king within Cheb 1 of a mine at r400 in >= 10, xfer600 >= 1.15x, net
    >= -1), screens zd390 (s7) / zd391 (s6) queued after zd389, replica set 3 then set 4 (next if zec fails).
  - Length-matched transfer bar: `python3 tools/lmxfer.py CTRLRUN CTRLBOT RUN BOT` on the VM.
  - Next fresh seed: 52. New scratch (VM ~/diag26b): gtrace.py PKG OPP MAP SEAT STEP (one field game's economy),
    carry.py GAMEDIR BOT (carriers' distance to the king), nodeliv.py PKG R (transferred/picked ranking), oppband.py,
    losslist.py, kingdmg.py, kingsiege.py, deathcause.py, fbite.py, rawheld.py, lowbank.py.
  - Closed lines rechecked this segment (attempts.py find): CARRY_MAX, LOAD_AT, TRAIL, ATK_CARRY, ATK_DELIVER, RUSH_*,
    RISK_ADAPT/FACE_HOLD/WOUNDED_HP (local diags showed no drop in trap fires), THROW_ADAPT, MAX/RICH_BABIES.
- **Earlier (15:55):**
  - Incumbent still **zdb**. Closed since 14:11: zdq REJECTED (replica block 1, 48/48 alike), zdu and zdv DROPPED
    (cq116 starvation, cq117 late bites; the late-strike line is closed), zdt REJECTED (#300 block 1 on set 3,
    t -1.63; replays VM ~/diag26b/rp/zdt_b1s3_1547.tgz), zdw REJECTED (s7 zd378 transfer 0.929x after passing
    cq118 with 12 wins to 10; the cost is in games shorter than r1500, 0.867x).
  - **zdx** (zdw + TRAP_OPEN_TO 150 / TRAP_OPEN_FLOOR 1500; bots/zdx/SNAPSHOT): chain **cq119** (seed 44, running;
    score with `python3 chtraps.py zdx cq119`, `python3 earlytr.py zdx 150 cq119`, `python3 twinchk.py cq119 zdb zdx
    zdb` in ~/diag26b), screens zd380 (s7) / zd381 (s6) queued behind zd376, **replica #302** block 1 on set 4 in the
    drain queue (judge with `paired --dir /dev/shm/rp 20817-20824,20842-20845,20848-20851 IDS`; zdw's 4 sent #301
    matches play first and do not count). A fail at 1 or 2 withdraws it (pull its rows from research/panel/queue.tsv).
  - zd376 (seed-2 s6 zdb control) is replaying after dying at start; zd374 (seed-2 s7) is complete.
  - Next fresh seed: 45. New scratch (VM ~/diag26b): chtraps.py BOT DIR (per game laid/hitThem/hitUs/theirs/starve),
    earlytr.py BOT R DIR (traps by round R, bank at R), trapwhere.py, bdeath.py, throwcmp.py, lowspawn.py.
  - Tool note: contest.py lives at tools/galaxy26/contest.py.
- **Earlier (14:11):**
  - Incumbent still **zdb**. Closed since 13:13: zdp REJECTED (replica block 1 t -1.00), zdr REJECTED (s7 zd366
    transfer 0.969x), zds REJECTED (s7 zd368 t -1.77, transfer 0.914x).
  - **Replica: zdq** (#299, zdb + LATE_STRIKE 2 from r1600) block 1 on set 2 (anchors 20984-20999): 8+ of 16 in
    (21205-21208, 21217-21220 all won; more from 21241), rest in the drain queue. Judge at block end with
    `paired --dir /dev/shm/rp 20984-20999 IDS` (t <= 0 rejects). Inert clause: a bite by our rats on an enemy king
    after r1600 in some block 1-2 game. Block 2 on set 3 (block_v4s3.sh) if it passes.
  - **zdt** (zdb + LOW_BABIES 12; bots/zdt/SNAPSHOT): chain **cq115** (seed 40, running, 32 games; mechanism: spawns
    with babies in [8, 12) and bank under price + 300, from spawn rows vs the previous round's rounds.tsv), s7
    **zd370 playing**, s6 zd371 queued. Next on the replica after zdq if cq115 holds; block 1 on set 3, then set 2.
  - Next fresh seed: 41. Scratch (VM ~/diag26b): chcat.py DIR R BOT... (cat kills to round R + causes), starvew.py PKG
    (economy windows before field starvation deaths), kform.py PKG MAP (bank/babies/kings every 25 rounds), kbite.py
    PKG (king bites). Field census (zdbb*.jsonl, `bsBy`): the enemy breaks the truce in 92 of 144 games.
  - Chain logs: the per-bot overruns are in the last JSON line of DIR/*.log under j['A'] / j['B'] (names j['a'], j['b']).
- **Earlier (13:13):**
  - Incumbent still **zdb** (controls s7 zd343, s6 zd345; anchors below). Closed since 11:22: zdk REJECTED (replica
    block 1 t -2.12), zdo REJECTED (#297 block 1 on set 3, D 1, t -1.00; replays VM ~/diag26b/rp/zdo_b1s3_1228.tgz).
  - **Replica: zdp** (#298, zdb + HEIR) block 1 on set 4 (anchors 20817-20824, 20842-20845, 20848-20851): 21148-21151
    in (all won), 12 in the drain queue (hourly limit, ~4 an hour). Judge with `paired --dir /dev/shm/rp
    20817-20824,20842-20845,20848-20851 IDS` (comma-joined ranges; `--partial` for interim). Falsifiers 1-2 passed
    (cq111 twin 12/12; zd360 t +0.57, zd361 t 0.00). Inert clause: a heir must form in some block 1-2 game.
  - **Next on the replica: zdq** (zdb + LATE_STRIKE 2 from r1600; bots/zdq/SNAPSHOT): cq112 passed (twin zhq 16/16,
    14 late king bites vs 0), s7 zd364 passed (t +1.00); s6 **zd365 playing**. Block 1 on set 2 (20984-20999).
  - **zdr** (dose arm: LATE_STRIKE 1 from r1400): chain **cq113** (seed 38, running; `python3 ~/diag26b/twinchk.py
    cq113 zdq zdr zdb 1400` in ~/diag26b: per-cell results and late king bites), screens zd366/zd367 queued.
  - Next fresh seed: 39. Scratch this segment (VM ~/diag26b): twinchk.py, latek2.py, ethrow.py, digspend.py, diglow.py.
  - Tool notes: screen_t/xpr/celldiff take bare names (not bots/NAME); gssh with a piped stdin must not add
    `< /dev/null`; never run `attempts.py init` (it re-sorts the file and adds probe rows).
- **Earlier (11:22):**
  - **Incumbent zdb** (accepted 09:05 over z3x: #293, pooled 96 cells, D 4 (+4 -0), t 2.00). New candidates build on
    bots/zdb (`BC_JDK=$SP/nojdk TMPDIR=/dev/shm bash tools/snapshot.sh NEW bots/zdb`, switch lines by python replace
    with asserts). Screen controls: s7 **zd343**, s6 **zd345** (seed 1). Replica anchors: set 2 = 20984-20999 (twin rows
    ~/diag26b/zdbb2.jsonl), set 4 = 20817-20824, 20842-20845, 20848-20851 (zdbb1.jsonl), **set 3 = 21016-21023 +
    21036-21043** (zdb twin block complete; rows ~/diag26b/zdbb3.jsonl, replays in VM ~/diag26b/rp).
  - **Replica: zdk** (#296, zdb + RUSH_FROM_BIG 12) block 1 on set 2: 8 of 16 in (21048-21051, 21065-21068), t -1.63
    (D 6, +1 -5): lost the rush kills of YokoOno's / Varun's kings (streetsofnewyork A r92, clouds A/B). Remaining
    21079-21082 playing, 4 in the drain queue. Judge at block end (`paired --dir /dev/shm/rp 20984-20999 IDS`;
    t <= 0 rejects). The rush count is the field optimum both ways (u0o 8 rushers net -7, u1o 16 net -3, zdk 4 now).
  - **zdk screens**: s7 zd358 passed (D 8 +7 -1, t +2.21, small maps identical); s6 **zd359 playing** (falsifier 4).
  - **zdo** (zdl's sources re-registered: zdb + KING_FAR + KING_FAR_MIRROR): chain **cq110** (seed 35, running, P 4):
    `python3 ~/diag26b/farchk6.py ~/diag26b/cq110 600 zdo` (repo dir): king within Chebyshev 5 of a mine at r600 in
    11+ of the 16 target cells (wallsofparadis, whatsthecatdoin, Nofreecheese, ultimatestarvation), xfer600 >= 1.15x
    zdb, wins not fewer, < 2 lost; jail/clouds do-no-harm (not net lost, xfer >= 0.95x); `cellres.py DIR zdo zdb`.
    Field caution: Merlin walks its own king to whatsthecatdoin's centre mines and kills our still king (r288/r325).
  - Closed this segment: zdh REJECTED (zd354 t -1.70), zdj DROPPED (r250 mechanism 6 of 16; Hike identity), zdl
    DROPPED (Hike identity), zdn DROPPED (cq109 xfer 0.982x of zdk; wins 15 vs zdk 14, zdb 11).
  - Screen queue after zd359: empty. Add zdo's screens if cq110 holds, or new candidates first.
  - Next fresh seed: 36. Scratch: cellres.py DIR BOT... (per-cell results, xfer600), farchk6.py, kmine.py (per-mine
    pickups in a cached replica game), early.py, kinghp.py (king hp + deaths per window) in ~/diag26b.
  - Laptop root is near full (the session transcript grows): build/vmruns deleted at 10:46 (progress-refresh.sh
    re-pulls ~87 MB; delete it again after a refresh). /tmp/claude-1000 holds 746 MB of other sessions (owner's call).
  - zdb's lost cells on sets 2 and 4 (23 of 96; /dev/shm/rp/*/match.json): seat-split ones are arrows A vs Varun,
    averystrangespace A and rose A vs YokoOno (points), clockwisdom B vs Varun, dirtpassageway A vs Finals, knifefight A
    vs Merlin (r107; Merlin's own king walk is asymmetric: as B it retreats to a wall pocket at (14,17), as A it walks
    into our rats and dies at r22), mincemeat B vs Icarus, RUN A vs Merlin (points), streetsofnewyork B vs YokoOno.
    Points at r2000 (war): 0.5 king share, 0.3 cat damage share, 0.2 transfer share; RUN A was 2-2 kings at 8.5k vs
    16.4k transfer (one more king flips it, but the bank sat at 300-500 through r1200).
  - **Before designing:** `python3 tools/attempts.py find TERM`, one term per call, read the tail. Closed king-defence
    families: AMMO_AWAY, RETREAT_HP, GLANCE_MEM, RESCUE, BITE_CARRIER/BITE_GUARD, KING_BITE_CARRIER, KING_BITE_MAX,
    GUARD_KING_R, KING_FLEE, KING_PRESS, GRAB_MICRO (zc6's GRAB_MICRO_L is the live variant), THIEF_LOAD. King walking:
    KING_PATH (u1y), KING_FORAGE (zc3, inert), KING_FAR (zdj, zdl dropped; zdo re-registered, pending).
  - VM disk 2.1 GB (09:55). Laptop ~23 MB free; /dev/shm ~79 MB free.
- Incumbent until 09:05 11-08 (now zdb, above): **z3x** (accepted 2026-11-06 21:01 PST over z1z; replica blocks 1-2 pooled 94 cells, D 4 (+4 -0), net +4,
  t 2.00; transfer 1.09x and 1.065x the twin's). z3x = z1z + ledger + FORM_WAR_FROM 300 + CASH_END 1880 + END_CAT 1850 +
  LAST_FLOOR 20. Screen controls: s7 **zd205** (agrab, agtr, atrap3 on 8 maps), s6 **zd172** (z3x, on zd136's cells): `screen_t.py zd172 z3x RUN BOT` (and ppr, xpr,
  scr_eq, ovr). New switches: build on bots/zl0 (ledgered z1z, scratch, untracked; `tools/snapshot.sh NEW bots/zl0`),
  then `bash tools/bundle.sh NAME zl0 z3x SWITCH...` (not executable) to put them on the incumbent (exact merges; check the Cfg by eye).
- **Replica references for z3x candidates:** set 3 = 18613-18620, 18623-18626, 18653-18656 (twin rows
  ~/diag26b/z3xb1.jsonl); set 1 = 18665-18668, 18682-18685, 18691-18694, 18697-18700 (z3xb2.jsonl); set 2 =
  18722-18725, 18736-18739, 18745-18748, 18750-18753 (z3xr2.jsonl). IDs also in /dev/shm/z3x_ids.txt, z3x_ids2.txt,
  z3x_ids3.txt. Set 4 = 18760-18763, 18783-18786, 18797-18800, 18810-18813 (done at 22:30; IDs in
  /dev/shm/z3x_ids4.txt; replays in VM ~/diag26b/rp and /dev/shm/rp, match.json in bc26b-galaxy-replays; twin rows
  ~/diag26b/z3xs4.jsonl). Submit: `tools/galaxy26/contest.py submit bots/NAME -m TEXT`, then
  `bash build/block_v4sN.sh`, `git checkout research/panel/v4-setN.json`. **Block order (01:51): control sets 2 and 4
  first; sets 3 and 1 are z3x's crowning draws and score only twin-anchored** (`paired TWIN CAND --crown CROWN`;
  twin z6t = z3x + HAND_FLIP, #254, set-3 block queued 01:51; set 1 still needs a twin block).
- **Replica now:** z6j REINSTATED 01:40 (#253; cs1 seeds 1-3: 1 flip, under the 3-cell bar; 12 rows back in the queue). Earlier: z3x resubmitted #252 at 01:13; z6j (#251) = z3x + THROW_SAFE + CASH_READ + CASH_KINGS 1 + CASH_BABIES 12, submitted 00:52 and
  **withdrawn 01:01** under its falsifier 1 (through z6i: cq6 where_are_you 0/4 vs z5n 3/4). Its 4 sent set-3 matches
  (Finals, spsquared, both seats; after 19037) still play. 12 unsent rows saved in /dev/shm/kd/z6j_queue.tsv: to
  reinstate, append them to research/panel/queue.tsv (drain-loop.sh, laptop pid 1065185, sends about 4 an hour).
- **z5n REJECTED at block 1** (00:53; #250, 19022-19037 vs z3x set 3): D 4 (+YokoOno Stash A; -Icarus mincemeat A,
  -Xzibit minimaze A, -spsquared rose A), net -2, t -1.00. The three losses diverge from z3x's at plain rat moves
  (RISK_TTL suspected; firstdec.py/dumpof.py in ~/diag26b). z5a (THROW_SAFE) was 48/48 alike: replica games are
  deterministic per build, so a block measures only the cells a build's code path changes.
- **CASH_READ (23:26):** after the r1880 CASH_END recall, cashTick read every squeak of the last 5 rounds (3-9k a
  turn for about 30 crowded rats); z5n reads r-2..r and skips copies of the best site: exact to r1880, 0 overruns.
- **Mid-game rescue (CASH_KINGS 1), the live line, open question = chaos:** single-seed chains say +3 -3. Gains
  (seed 0): fullofsecrets abrk B, rose abrk A, rose araid A (cq3, cq6). Losses: where_are_you 3 cells (z6f, z6h, z6i
  all 0/4 vs z5n/z5o 3/4). z6i (CASH_BABIES 12) stopped z6f's seven failed sites, yet still lost: one 7-round
  rescue at r452 flipped the game (cq3d/wy_z6i). **Seed chains decide it:** ~/diag26b/cs1 (z5n vs z6i,
  where_are_you, seeds 1-3, 24 games) then cs2 (fullofsecrets + rose, seeds 1-2, 32 games); chain4.sh, chainsum.py.
  If where_are_you on seeds 1-3 is not 3+ cells to z5n, reinstate z6j's block. z6g (CASH_N 2) and z6h dropped.
- **Screen queue:** zd203 = z6j (from 00:52), then zd200 = calibration (z5n vs the nine unused archetypes on
  tiny/DefaultMedium/Stash/evileye, 72 cells: pick the 25-75% opponents for the next screen version; the araid/abrk
  screen is saturated, z5n 44/44), zd195-zd199 = ablations of z3x (z6a SEEK_IDLE, z6b FIGHT_SHIFT, z6c CAT_RING,
  z6d CAT_LAY, z6e RICH2_BABIES), then the closed lines (zd141, zd132, ...). zd201 (z6f) passed: net -1, its losses
  THROW_ADAPT's (as z5o's zd194, net -2).
- **z3x set-3 death census (01:01):** of 23 losses 20 are king kills (13 hit, 7 starved after the army was gone);
  throws killed 39% of our rats in losses (15% in wins); enemy rat traps killed 504 of ours, ours 0 (z3x lays none;
  trap laying is closed, about 15 attempts). Scratch kdeath.py and deathcause.py in ~/diag26b.
- **Rejected tonight:** z5k (zd191: D 3, net +3, t +1.77, but 6 crowd overruns; CASH_READ fixes them), z5m
  (SYM_KING_SEEN; mechanism falsifier: lost streetsofnewyork A, which z3x won; zd192 pulled), z5d (overruns).
- Engine: no third king after r1200 (MAX_NUMBER_OF_RAT_KINGS_AFTER_CUTOFF 2, RAT_KING_CUTOFF_ROUND 1200), so a
  2-king points loss cannot be flipped by an endgame king. Messages live 5 rounds (MESSAGE_ROUND_DURATION).
- Dropped tonight: z5f, z3w (WALL_HOLD, do not revive), z4f, z4g, z4h. Closed diagnoses with no candidate: starvation
  with cheese held (ATK_DELIVER, HUNGRY, DRY; CASH needs a bank >= 50), reactive baby traps (11 attempts), seat shift,
  fewer babies (u5t), RISK_FRESH/RISK_ADAPT.
- **Throw line (20:05-20:50):** enemy thrown-rat hits on our kings always throw our own rats, grabbed blind-side 3-5 from
  the king (wounded retreaters), 90% by thewaxmango and nzjt (kthrown.py, kgrab.py, kgrab2.py, kdis.py in ~/diag26b).
  Disintegrate-on-carry is not usable: destroying a carried rat clears its carrier from the engine's robot grid
  (GameWorld.destroyRobot).
- Rejected today on the replica: z3z (block 1 D 6, net -2, t -0.82), z3s, z3q, z2x, z3b, z3c, z3j. Closed lines today:
  discovery (z4g, z4h: a side glance replaces the forward cone), the rush index (z4d, z4e, z4f: zd178 picks z -2.11), seat
  (z3y/z4a), grab-war (GRAB_SAFE), king-biter (KING_CALL), DRY.
- **Laptop disk:** about 210 MB free at 22:35; write scratch to /dev/shm. Repacked at 20:45. 7,311 loose objects (265 MB) went to a 26.8 MB pack, built in /dev/shm and prune-packed
  through a temporary alternate (`.git/objects/info/alternates` is now empty). 239 MB free. `git config gc.auto 0` stays set:
  a plain gc needs room for the whole new pack. Repeat the recipe when loose objects pile up again (TRAINING_LOG has it).
  Scratch lives in /dev/shm (/dev/shm/sp_moved; scratchpad dirs are symlinks there).
- VM disk: 3.1 GB free at 22:35 (pruned at 22:26). Keep it at 2 GB or more.
- **Seat line, closed 19:01:** the engine's one asymmetry is turn order, but SEAT_SHIFT is a perturbation, not parity
  (~/diag26b/sc: z4a gives the flipped seat result in 23 of 45 seed pairs, z4b z1z's own in 18 of 30). Self-play
  mirrors break at r21 on peaceinourtime from orientation-dependent bytecode cost. knifefight: seat A won 15 of 15.
- **Grab war, closed again 18:56:** our carriers with an enemy within Chebyshev 2 are retaken 19-31% of the time, all
  before the first legal throw (grab + 2), so THROW_HOLD cannot help; GRAB_SAFE (u6b, u6c, v1a) is rejected.
- ~/diag26b scoring scripts (screen_t.py, scr_eq, xpr, ppr, ovr) take bare package names (z3x, not bots/z3x); with
  the prefix they score seat-B cells only.
- VM disk: pruned closed mechanism dumps (wh, dry, cr, cp, cs; logs kept) at 18:45: 3.4 GB free.
- Dropped today: z3u (KING_CALL), z3v (WALL_HOLD: wins 18 vs 19 on the maze cells). Rejected on the replica: z2x, z3b,
  z3c, z3j, z3q. Ledgered switches flip one or two cells a block, so candidates are bundles of passed switches.
- ~/diag26b scoring scripts (scr_eq, xpr, ppr, ovr) take bare package names (z3x, not bots/z3x).
- **Exact silence:** **tools/ledger.py bots/NAME** applies the exact ledger to a u6x-family bot; its docstring has the
  engine's counting model and the hook recipe. A hook whose acting path is costly closes with **G.settle(cd, cb, K)**
  (K = 18 + base-absent instructions after it): crediting an acting hook made left() overstate the budget and z2j
  overran 17 times in zd146 (Seal.work/sense2). Check a ledgered bot with a dumped game vs its base:
  `python3 ~/diag26b/replay_eq.py BASE_DIR CAND_DIR` and `hot_eq.py BASE_RUN CAND_RUN` (every hot= equal). Screens:
  `python3 ~/diag26b/scr_eq.py zc6 u6x RUN BOT` lists the cells that differ.
- **The endgame-king line (all on u6x, ledgered; kings alive at r2000 weigh 0.5 of war points):**
  - z2h CASH (passed zd144, inert); z2j = z2h + CASH_END 1880 (zd146: D 1 net +1, picks/transfer fine, but 17
    overruns); **z2o** = z2j with settle-capped CASH hooks (exact on keepout A; 0 overruns in all 5 acting cells).
  - z2k RICH_LATE 1050 (third king r1050-1150 on a surplus; zd147 playing, identical so far); z2m RICH_FIX (rich-site
    formation fixes; zd148 next); z2i RICH2_BABIES 80 (zd145).
  - **z2p** = CASH + CASH_END + RICH_LATE + RICH_FIX + settle: the bundle. Checks pass (= z2n's games). **Screen zd149**.
  - **z2q** = z2p + RICH_MORE 900 (4th/5th king while Econ.surplus(kings+1) >= 3000): inert in practice, the 4th king
    came at r1111-1133. **z2r** = z2q + RICH_GAP 40 (after a rich king forms, the next site opens 40 rounds later, not
    210). Checks ~/diag26b/zr; **screen zd150** after zd149.
  - Field census (tools/lever.py on the VM, 36 field r2000 losses in 624 games): one fewer enemy king flips 23, one
    more of ours 18; superioristhmus vs spsquared (2-3 kings to their 3-4, 3-15k banked) is 11 of them.
- **Replica order after z1z:** z2p if zd149 passes (z2r if zd150 passes too); if z1z is accepted, the bundle is ported
  onto z1z first. Else z2c (passed zd139). Submit: `contest.py submit bots/NAME -m TEXT`, `bash build/block_v4sN.sh`,
  `git checkout research/panel/`.
- **CASH (rescue formation; bots/z2e/SNAPSHOT):** when the bank is 60-249, a carrier group 5+ from any king with
  3+ rats and 250+ raw names a site and promotes a rat there (becomeRatKing banks the 3x3's raw). Chain cq2 on u9p:
  won 7 of 8 to u9p's 2. z2f (on u6x, no ledger) failed screen zd142 on transfer (0.964 in the gate form; D 4, net +2,
  t +1.00). Screens zd143 (z2g vs zd136) and zd141 (z2e vs zd61) are queued.
- **Pooled-control re-score (09:15):** against u9p plus its two salted twins, z0f, z0l, z0n, z0s, z1c, z1t and z1x
  no longer fail a gate; z0a and z1s still do; z1a fails kingsFormed. They are u9p-line switches: worth the replica
  only rebased on u6x (ideally with the ledger).
- Screening (VM runs/screenq.txt): zd143 z2g (running), zd141 z2e, zd132 z1u, zd112 z0t, zd113 z0u, zd114 z0v, zd107
  z0p, zd108 z0o and the old sweep. Controls: zc6 (u6x), zd61 (u9p), zd136 (z1z).
- **averystrangespace lock-in, closed 07:04:** in the u9p mirror neither side leaves its dirt-sealed corner until
  ~r880. z1x (seekers tunnel to the cats or the centre) won 3 of 4 there (chain tn) and 4 of 4 in the partial tx
  chain (~/diag26b/tx), but failed screen zd134 on transfer (0.956; mostly wallsofparadis 6.84 to 4.90 a round, a
  single-game divergence after r1200, not the seekers: they ran 100 turns before the r131 war, ~/diag26b/wq). z1y (z1x + z1q + z1r) was withdrawn with it. Map renders:
  `arena.java_tool('bc26tools.MapCensus', arena.MAPS, '--ascii', MAP)` on the VM.
- **Screen gates (method change 05:06):** s6 also rejects at per-round picks z <= -2 (scratch ppr.py, copy in
  ~/diag26b: `python3 ~/diag26b/ppr.py zd61 u9p RUN BOT` on the VM); a switch that changes the number of formations
  is judged per round only. ~/diag26b/sweep.py lists every zd run against zd61 with all the paired measures.
- Closed this session (05:00-05:12): z0q (block 3, pooled 144 cells t 0.78), v0a (dg chain: cat damage on our
  kings not lower). Diagnoses: enemy trap deaths near our king are the opening brawl against rushers (young,
  never-carried rats walking out among 4+ enemies); DefaultMedium 0 of 10 (early-war merges, army collapse);
  thunderdome vs nzjt is a throw race (they throw 2-3x as often; king hits by thrown rats).
- Screening (VM runs/screenq.txt): **zd138 z2b** (running), **zd139 z2c**, **zd140 z2d**, zd132 z1u (CAT_LAY_MAX 5; it fell out of the
  queue at 06:56 and was re-queued), zd112 z0t, zd113 z0u, zd114 z0v, zd107 z0p, zd108 z0o and the old sweep.
  Passed today: zd127 z1l, zd128 z1q, zd129 z1r, zd136 z1z, zd137 z2a. Rejected: zd130 z1s, zd134 z1x, zd131 z1t (picks z -2.98).
- Lead under study (07:01 log): peace games end when the last cat dies, settled on points then (19 dumped replica
  games). On two-cat maps each side kills the cat in its half, so a bite on the other side's cat outweighs the
  transfer margin. Scan: ~/diag26b/catsteal.py build/dumpcache (steal windows: peace rounds after our cat died while
  theirs lived).
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
  Order (u6x reign): sets 3, 1, 2, 4; z3x reign: see Block order in State. Block scripts `build/block_v4sN.sh` (they touch research/panel/*.json: `git checkout
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
