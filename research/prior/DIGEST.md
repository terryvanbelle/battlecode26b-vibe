# Digest of prior practice seasons (clean room)

Read 2026-10-27 PDT from the filtered reading room `~/projects/vibe/reference/readroom-no2026/` (built by
`tools/readroom/build_readroom.py`: every block tagged 2026, a 2026 team, or 2026 game vocabulary is dropped).
battlecode26-vibe was never opened. Order of attempts: 22, (26), 25, 21, 20, 24, 23, 19 — later weighs more.

## Outcomes
| season | lineage result | method at the end |
|---|---|---|
| 2019 (latest) | v12, BT 1832, rank 17/221, 88.7% expected vs field | replica panel, paired fresh-cell rule; local screens reject-only |
| 2023 | c_gd1, BT 2046, rank 11/140 | 20-game h2h + screen + 100-game replica panel (t = net/sqrt(D)) |
| 2024 | 2113, rank 6/88 | seeded paired band test, delivery gate, three-way verdicts, shipping rule with simulated power |
| 2020 | plateau 1740-1752 | paired mirror, Bradley-Terry ladder, control blocks |
| 2021 | rank 3/21 | SPRT mirror |
| 2025 | 42% -> 56% vs best external bot | deterministic engine: error bar over maps; McNemar on 450-key paired roster |
| 2022 | 0/20 vs best vendored bot | win-rate gate vs own snapshots |

## What produced strength (every season agrees)
1. **Correctness audits** of bot and instruments (2024: two of six promotions directly, +185 Elo; dead symmetry code,
   an always-on alert, freezing on enemies behind walls, unseeded test harness).
2. **Repairs of own degeneracies and removal of caps** (stall counter reset by retargeting: one line, biggest gain of
   2020; "danger = any enemy" froze the economy in 2021; HQ-aura deaths 1227 -> 35 in 2023).
3. **Copying a whole field style** that beats us (2023 swarm +7/+9; 2019 church chains made 108/168 kills).
   Copying a single trait or a symptom failed (forcing early soldiers worsened 2025).
4. **Ablating what we carry** (2023 cohesion rule removal +20).
5. **Stacking delivered near-misses with independent mechanisms** (2019 h35, 2024 g_iter5).
6. **Bug escape that fires only after a measured stuck condition** beat full bug nav replacing greedy (2025).

## What did not
- Self-play / own-family acceptance (never tracked the field: 2019 v1-v6 below v0 on BT; 2023 t 3.18 local, 0.82 panel;
  2025 lineages lost 3/4 to the external benchmark while fighting over 3 points among themselves).
- Reallocations between unit types, raising counts/caps/ratios, constant-tuning ladders, Swiss parameter races.
- Underpowered gates (a 17%-power shipping rule held a real gain 45 h); over-permissive ones (net +1..+3 accepted 44%
  of nulls; pooled z on lopsided repeated cells accepted 17-30% of nulls).
- Arms that never fired (sed matched nothing; dead branch; choice set of one). Always prove delivery first.

## Method elements kept (with their reason)
- Unit of evidence: paired cell (opponent, map, side, seed); only discordant pairs inform; t = net/sqrt(D).
- Identity control on every verdict path; key every result by code hash.
- Delivery before value: one logged game + mini-block showing the mechanism fires.
- Pre-registration: mechanism, counter, reachability, firing rate, price, gate, falsifier, dose ladder with zero arm.
- Local screens only reject once a field ladder exists; the field panel accepts. Fresh cells per block; both sides.
- Champion plays its own control block before candidates are scored (winner's curse).
- Batch Bradley-Terry over all ladder games, pair cap 200, repeated cells once; never accept on ratings.
- At least one structural attempt in four; three rejects in one area -> switch area; plateau protocol starts with audit.
- Never idle: >= 3 jobs queued, idle filler; watcher wakes the session (<= 50 min heartbeat).
- Rules live in tools that refuse. Nothing stale stays. Push every commit. PDT timestamps from `date`.

## Bot architecture lessons
- Canonical frame / play symmetry: tie-breaks from own geometry, never compass order, first-sensed order, team, or
  ID-correlated choices; RNG seeded per robot but never used for side-correlated choices; mirror self-play must split
  evenly on every map from both sides (2023 audit: 144/144 mirrored games diverged from ID-keyed decisions).
- Every change behind a `static final` switch defaulting to the incumbent; defaults build == incumbent byte-for-byte
  in behaviour (no-op replay check); javac folds dead branches, so test both settings.
- Bytecode monitor in the turn loop from commit 1 (round before vs after = overrun; > 90% = near miss); report via
  indicator string; any overrun voids an arm. First-turn allocation spikes are common. `java.util` is metered.
- Symmetry: candidate bitmask never emptied; eliminate by geometry, image sightings, terrain memory, engine give-aways;
  act on all live candidates; "guessing what can be known is a bug". Basics bar: symWrong = 0.
- Comms: one writer and one purpose per slot, round stamps, clearing condition seen to fire, unit-tested pack/unpack.
- Sticky targets beat nearest-recomputed; gate on the engine's own predicate, not a proxy.
- World model must mirror the engine's bookkeeping (costs charged at action time).

## Tooling map (steal list)
| need | source | note |
|---|---|---|
| bare-JVM runner, results parse | bc24 `tools/lib.sh`, bc25 `tools/gauntlet.sh` | 2026 jvm flags = scaffold runJavaLocal |
| parallel gauntlet, private class trees, re-exec from copy | bc24 `tools/gauntlet.sh` | semaphore; never recompile under live games |
| paired/SPRT/compare | bc24 `tools/paired.sh, sprt.py, compare.py`; 2023 `paired.py` | |
| Bradley-Terry | 2023 `tools/elolib.py, elo.py` (58 tests) | |
| RobotController mock | bc24 `test/bot/BotTest.java` (reflection Proxy) | Clock reads 0 outside engine |
| unit-test runner, deadcode | bc24/2023 `tools/unit-tests.sh`, `deadcode.py` | flock |
| blind bench compile, scan | 2023 `tools/bench-*.{sh,py}`, 2019 `bench-scan.sh` | never read their source |
| galaxy replica | 2023 `tools/galaxy/`, `tools/replica/deploy/`; 2019 `tools/galaxy19/` | GCP stand-ins, saturn.py, nftables egress, Caddy+basic auth |
| contestant client, ladder policy | 2023 `tools/contest.py`, `ladder_policy.py`; 2019 `contest19.py` | |
| watcher heartbeat | 2023 `tools/watch_events.py` | exits to wake the session |
| VM queue | 2023 `tools/vm-*.sh`, 2019 `tools/jobq.sh` | setsid nohup; kill by pgid |
| charts | 2023 `field_score.py`, `rating_history.py`; bc22 `plot_progress.py` | bc25 top-level plot tools are TAINTED (ported from bc26): never use |

## Infrastructure gotchas
- Driver (e2-small, 2 GB): one game at most; the session holds ~450 MB; OOM reaps background shells. VM
  (e2-standard-8, 31 GB, 20 GB disk): knee ~7-8 concurrent games; keep total games <= vCPUs; disk fills -> prune.
- `pgrep -f`/`pkill -f` match their own shell: bracket a character or use PIDs. Never edit a running bash script.
- Unique run ids (atomic mkdir); timeouts on every game; a match on a galaxy ladder plays the submission active at its
  creation; siarnaq rates in creation order so one stuck match freezes all ratings; Postgres not SQLite; frontend
  `.env.production` points at api.battlecode.org and must be overridden; sslip.io DNS flakes -> retry GETs only.
- 2023 telemetry trick: replica replays had indicators off, so state was encoded in bytecode counts. Check 2026.
