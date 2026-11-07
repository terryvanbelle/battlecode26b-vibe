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

## State (2026-11-06 16:53 PST; TRAINING_LOG.md has the history, older states are in this file's git history)
- Incumbent: **z1z** (accepted 2026-11-06 11:46 PST over u6x; replica blocks 1-4 pooled D 46, net +14, t 2.06; u9p + SEEK_IDLE
  20/IDLE_SHIFT/FIGHT_SHIFT + RICH2_BABIES 80). Screen control: **zd136** (z1z). Ledgered z1z for new switches: bots/zl0
  (scratch, untracked; `cp -r bots/zl0 bots/NEW`, sed the package; it is already ledgered, do not rerun ledger.py).
- **Replica: z3s (#245, submitted 16:36) on set-3 block 1** = z1z + ledger + FORM_WAR_FROM 300 + CASH_END 1880 + END_CAT
  1850. 2 sent (15385-15386), 14 in the drain queue; waiter build/wait_z3sb1.sh (IDs > 18502). Score `contest.py fetch
  --new --pages 3`, `paired 17946-17961 IDS` (block 1 rejects at t <= 0); then block 2 on set 1 (`build/block_v4s1.sh`,
  `paired 17962-17977 IDS`, rejects at pooled t <= 1); `git checkout research/panel/` after each block script.
  Transfer floor: rsync the IDs to VM ~/diag26b/rp, `tools/field_report.py games --dir ~/diag26b/rp --ids IDS >
  ~/diag26b/NAME.jsonl`, `python3 blockcheck.py CAND.jsonl,TWIN.jsonl CAND_IDS TWIN_IDS` (twin rows z1zb1..b4.jsonl).
- Rejected today on the replica: z2x, z3b, z3c (CAT_HUSH), z3j, **z3q** (block 2: 48 of 48 alike; pooled D 1, t 1.00).
  Ledgered switches flip one or two cells a block, so the accept bar (pooled t >= 2, D >= 4) needs bundles: each
  candidate after z3s should carry every switch that has passed its screen and mechanism checks.
- Passed, not submitted alone: z3p (CASH_END, in z3q/z3s), z3r (END_CAT, in z3s), z3t (LAST_FLOOR 20: zd167 inert).
  Dropped: z3u (KING_CALL; falsifier 2, 8 blind ring bites vs a bar of 24).
- **Lead: z3v = z1z + ledger + WALL_HOLD** (16:50; bots/z3v/SNAPSHOT). Replica dumps: 68k of 138k held w/f baby turns
  faced an enemy behind a wall (detour 6+; corridorofdoom vs Varun 15% of our baby turns, minimaze 8%, Stash 8%).
  Screen **zd170** queued behind **zd169** (z3s's combined screen); mechanism games ~/diag26b/wh (40 games, 5 maze
  maps vs araid/abrk, both seats, z1z controls; table: `python3 ~/diag26b/walled2.py DIR z3v` style, US = package).
  If both pass, the next replica candidate is z3s + LAST_FLOOR + WALL_HOLD.
- Other census results (16:50-16:53, scratch stillcen.py): 'd' still turns (8% of baby turns) are cheese-weight
  movement cooldown, not decisions; 'M' (3.7%) and 'a' (2.6%) stalls are real blocks (rose, TheHeist, minimaze;
  attackers on Stash push at the wall toward the mirror king spot).
- Screen queue after zd170 is on closed lines (zd141, zd132, ...): add a candidate before zd170 finishes.
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
