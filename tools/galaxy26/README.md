# tools/galaxy26: the private Battlecode 2026 contest site

The bc26 replica is a private copy of the official judging site, galaxy (siarnaq backend and React frontend), plus a
local stand-in for saturn, the game runner. It runs on the VM `battlecode-dev` and plays only the pinned Battlecode
2026 Java engine (`engine/battlecode26-java-1.2.6.jar`, sha256 in `engine/SHA256SUMS`) on the 74 maps in `maps/`.
It is ported from our earlier replicas (2023: `~/projects/vibe/2023/tools/galaxy`; 2019:
`~/projects/vibe/2019/tools/galaxy19`). It shares no files, units, users, database or nftables table with them.
`ACCESS.md` at the repository root explains how the owner opens it.

- Site: **https://galaxy26.136-86-167-127.sslip.io/** (the host name follows the VM's external IP; see "Host name").
- Galaxy source: commit `f343088` of github.com/battlecode/galaxy (MIT; the commit the earlier replicas used, from
  the 2026 season). `deploy.sh` makes a `git archive` of it from `~/projects/vibe/reference/galaxy`, and
  `galaxy-setup.sh src` refuses any archive that is not of that commit. siarnaq runs unmodified.
- Episode `bc26` ("Battlecode 2026", language `java21`): 74 public maps; ranked scrimmages are on; the hourly limits
  are 20 ranked and 40 unranked requests (raised as on the earlier replicas); autoscrim runs `0 */8 * * *` UTC while
  the scheduler timer is on. The superuser is `owner`.

## Isolation

- **No Google Cloud and no battlecode.org.** `standins/google` shadows the `google.*` packages siarnaq imports.
  `replica_gcp` provides local Pub/Sub, Cloud Tasks, Cloud Scheduler, Secret Manager, GCS and OIDC: spool
  directories, files under `/srv/bc26-galaxy/storage/<bucket>`, and HMAC tokens. Email, OAuth and Sentry are off.
  `replica_settings.py` refuses to start if a `google` module comes from anywhere but the stand-ins. The venv is
  built from the hash-locked `requirements.txt` with `--require-hashes --no-deps`, and setup fails if any google
  package lands in it.
- **Egress.** The nftables table `inet bc26_replica_egress` limits uid `bc26replica`, which runs every service, to
  loopback port 8026 (siarnaq). It cannot reach the internet, the DNS stub, Caddy, or the 2023 replica, and the GCE
  metadata server is dropped. All units sit in `bc26-replica.slice` (`IPAddressDeny=any`, `IPAddressAllow=localhost`).
  saturn also has `PrivateNetwork=yes`. Host-wide, the table `inet bc26_official_block` rejects the addresses of
  battlecode.org, www/api/play/releases.battlecode.org (resolved at setup, plus the ones recorded on 2026-10-21) for
  every user. Loaded at boot by `bc26-replica-egress.service`.
- **Frontend.** galaxy's own frontend is built on the VM (`frontend/build.sh`) from the pinned commit with
  `replica.patch` applied:
  - same-origin backend (`VITE_BACKEND_URL` empty instead of api.battlecode.org);
  - the site URL;
  - a bc26 Replay! link to `/viewer/index.html?gameSource=...`;
  - self-hosted fonts;
  - a CSP meta tag;
  - email verification off.

  It installs with `npm ci --ignore-scripts`. `frontend_check.py` audits each build and each install: no URL of
  another host is fetched without a click, battlecode.org appears only as hyperlinks, and the CSP is present.
- **Viewer.** The official Battlecode 2026 web client (battlecode26 repo `client/` + `schema/`, commit `103abf6`,
  GPL-3.0) is built on the VM by `viewer/build.sh` from a `git archive` of that commit. The build removes the Google
  Fonts link and the desktop-only git dependency `where-is-it`, and adds the CSP meta tag. It also runs
  `npm ci --ignore-scripts` (so Electron and Tauri binaries are not fetched) and the same bundle audit. One reviewed
  exception: speedscope's "flamegraph" link opens through a click listener.
- **Caddy.** This replica runs its own Caddy (`bc26-replica-caddy.service`, user `bc26caddy`, `/etc/bc26-galaxy/Caddyfile`,
  data in `/var/lib/bc26-caddy`) with Let's Encrypt, a basic-auth gate (user `owner`), and a strict
  Content-Security-Policy (`connect-src 'self'` etc.). The binary `/usr/local/bin/caddy` is shared and only executed.
  **Ports 80/443 are also those of the paused 2023 replica's `bc23-replica-caddy.service`; only one can run.** That
  unit is still *enabled*, so after a VM reboot whichever starts first holds the ports. This replica never stops the
  2023 one. If the site is down after a reboot, see "Host name and reboots".
- **Bot code.** Every compile and every game runs under `sandbox.sh` (`unshare --user --net --mount`): no network
  at all, and a read-only view of everything but its own work directory. The sandbox fails closed if any reachable
  writable mount is left. Each game is its own JVM with a heap cap and a wall limit.

## saturn.py (the game runner)

It keeps saturn's message and report protocol, as the earlier replicas' saturn.py did: Pub/Sub spool → slot leases
→ task recipe with step logs → reports through the relay's unix socket → siarnaq. Galaxy itself has no changes.

- **Compile** (as `saturn/pkg/run/java.go`):
  1. Unzip the submission (zip-slip check, 64 MiB limit).
  2. `javac --release 21 -proc:none` against the engine jar.
  3. Run the engine's `battlecode.instrumenter.Verifier <package> <classes>`.
  4. Upload the class tree as the binary.

  The user's own failures are reported `OK!` with accepted=false and the log tail; infrastructure failures are
  retried. Java only: Python and Scala sources are reported and not built.
- **Execute.** It sets the same engine properties as the official `gradlew run`:
  - `-PshowIndicators=false -PoutputVerbose=false`: indicators off, robot output not sent to stdout;
  - `validate-maps=true`, profiler and debug methods off;
  - team names = siarnaq team names, each team with its own package and class directory.

  Winner and score use saturn's regex `(?m)^\[server\]\s*.*\(([AB])\) wins \(round N\)$`. The score is games won.
- **Deviation: one engine run per map.** The official saturn runs all of a match's maps in one engine run. Here each
  map is its own run, so a finished game survives a saturn restart or a failed attempt: it is kept in
  `$GALAXY_HOME/saturn/resume/<key>` until the message is acked, at most 3 days. For an alternate-order match,
  games 2, 4, ... play a copy of the map with teams reversed (`runtime/maps-reversed`, written by
  `java/Bc26Tool reverse-maps` with the engine's own `GameMapIO.loadMap(name, dir, teamsReversed=true)`). That is
  exactly what the engine's multi-map run does. Checked on 2026-10-28: per-map games equal the native 3-map
  alternate-order run game for game (rounds, winner, win type), and the reversed map changes the outcome where it
  should.
- **One replay per match.** `replay_merge.py` merges the per-game replays into one `.bc26` file (one GameHeader, every
  game in order, the GameFooter of a game the overall winner won), uploaded where siarnaq expects the replay. The
  official client opens it like an engine-written multi-game file.
- **Limits** (`/etc/bc26-galaxy/galaxy.env`; override in `/etc/bc26-galaxy/local.env`, then restart saturn):

  | variable | default | meaning |
  |---|---|---|
  | `GALAXY_EXECUTE_SLOTS` | 4 | concurrent matches (cap 8) |
  | `GALAXY_GAME_TIMEOUT` | 3600 | wall seconds per game |
  | `GALAXY_GAME_HEAP_MB` | 2048 | `-Xmx` per game JVM |

  The unit also has `MemoryMax=22G` and `CPUQuota=800%`. The engine meters bytecodes, not wall time, so
  contention slows games but changes none.

## Parts

| file | role |
|---|---|
| `saturn.py`, `sandbox.sh`, `replay_merge.py`, `java/Bc26Tool.java` | game runner (above). `Bc26Tool`: `reverse-maps`, `check-maps` (every map loads both ways and passes `LiveMap.assertIsValid`), `replay-info`. |
| `relay.py` | relay saturn → siarnaq, and the Cloud Tasks / Scheduler stand-in (`fire-jobs`, run each minute by `bc26-galaxy-scheduler.timer` when enabled). |
| `replica_settings.py`, `replica_gcp/`, `standins/`, `py310compat.py` | siarnaq's settings (`Replica` configuration) and the Google Cloud stand-ins. |
| `bootstrap.py`, `maps.txt` | episode `bc26` + 74 maps, service user, superuser `owner`; `status`, `audit`, `purge-teams PREFIX`. |
| `client.py` | API client + CLI as a contestant: register, submit, request, matches, fetch. Accounts in `~/.bc26-galaxy/accounts.json` (600). |
| `contest.py` | our team `vibe26b` (below). |
| `field.py`, `field-teams.tsv` | field teams: `seed`, `status`, `queue`, `activity`, `boost`, `rate`. |
| `browser_check.py` | headless-Chromium end-to-end walk through the site, through the replay in the viewer (on the VM). |
| `verify.sh` | the isolation and health checks (egress, no Google code, bundles, services, gate). |
| `frontend/`, `viewer/`, `frontend_check.py` | frontend and viewer builds + bundle audit. |
| `deploy/deploy.sh`, `deploy/galaxy-setup.sh`, `vm.sh` | deployment from the driver (idempotent, per step). |
| `rating_unstick.py`, `spool_bump.py` | operator repairs carried over from the 2019 replica (their headers). |

## Operating it

```
tools/galaxy26/deploy/deploy.sh            # full (re)deploy; or deploy.sh STEP: status | app | runtime | env | units |
                                           # start | stop | viewer | frontend | site | scheduler on|off | activity on|off
bash tools/galaxy26/verify.sh              # isolation + health checks (runs on the VM over ssh)

# rebuild the frontend / viewer on the VM (as the operator), then install them
gssh 'bash ~/bc26-galaxy-stage/tools/galaxy26/frontend/install-node.sh'      # once (node v24.21.0, sha256-pinned)
gssh 'bash ~/bc26-galaxy-stage/tools/galaxy26/frontend/build.sh'
gssh 'bash ~/bc26-galaxy-stage/tools/galaxy26/viewer/build.sh'
tools/galaxy26/deploy/deploy.sh frontend; tools/galaxy26/deploy/deploy.sh viewer

# our team (credentials ~/.bc26b-galaxy-team, 600)
python3 tools/galaxy26/contest.py register
python3 tools/galaxy26/contest.py submit [bots/ratbot] [-m TEXT]       # zip <package>/*.java, upload, wait for compile
python3 tools/galaxy26/contest.py ladder
python3 tools/galaxy26/contest.py scrim --n 4                          # ranked above, unranked below
python3 tools/galaxy26/contest.py scrim --vs bc26-example,bc26-example-copy@+   # @+ / @-: fixed side, unranked
python3 tools/galaxy26/contest.py matches [--all]
python3 tools/galaxy26/contest.py fetch --new        # ~/projects/vibe/bc26b-galaxy-replays/<id>/{replay.bc26,match.json}
python3 tools/galaxy26/contest.py results
python3 tools/galaxy26/contest.py compare 10-15 20-25  # panel blocks, cell by cell (also: pool, paired)

# field
python3 tools/galaxy26/field.py seed                   # teams of field-teams.tsv (examplefuncsplayer x2 now)
python3 tools/galaxy26/field.py status | queue
python3 tools/galaxy26/client.py request bc26-example bc26-example-copy --maps Excavation,DefaultMedium,DefaultLarge
python3 tools/galaxy26/client.py request bc26-example-copy bc26-example --ranked

# end-to-end in a real browser (on the VM; Debian's chromium is installed there), then clean up
gssh 'python3 ~/bc26-galaxy-stage/tools/galaxy26/browser_check.py --out ~/bc26-galaxy-build/browser'
gssh 'sudo bc26-galaxy-manage bootstrap purge-teams e2e-'
```

`match.json` (written by `contest.py fetch`) has one record per game, read from the replay: map, team A/B,
`reversed` (game i of an alternate-order match, i even counting from 1), winner, rounds, win type. In panel
comparisons, a "side" is the start position (A or B of the map) our team played from.

## Timers (installed DISABLED)

- **Autoscrim** (galaxy's ranked round over every team, `0 */8 * * *` UTC): `tools/galaxy26/deploy/deploy.sh
  scheduler on` enables `bc26-galaxy-scheduler.timer`. Each minute it runs `relay.py fire-jobs`, which posts the due
  autoscrim job to siarnaq. The replica-only throttle `GALAXY_SCHED_MAX_WAITING` (30) holds a due autoscrim while
  30 or more matches wait. `GALAXY_SCHED_MIN_HOURS` (0) can space autoscrims further apart.
  `deploy.sh scheduler off` disables it.
- **Field activity** (field teams challenge teams near their rating while fewer than 2 matches wait): the unit runs
  `field.py activity` as the operator on the VM. It reads accounts from `~/.bc26-galaxy-field/accounts.json` there.
  To enable it:

  ```
  gssh 'mkdir -m 700 -p ~/.bc26-galaxy-field'
  python3 -c "import json,os; a=json.load(open(os.path.expanduser('~/.bc26-galaxy/accounts.json'))); \
    print(json.dumps({k:v for k,v in a.items() if k != 'vibe26b'}))" | gssh 'umask 077; cat > ~/.bc26-galaxy-field/accounts.json'
  tools/galaxy26/deploy/deploy.sh activity on          # off: deploy.sh activity off
  ```

## Deploying a change to saturn or the runtime

`deploy.sh app` (code) and `deploy.sh runtime` (JDK, engine, maps, reversed maps) swap files under the running
services. saturn picks them up only on `sudo systemctl restart bc26-galaxy-saturn`. Matches in flight go back to the
queue and resume from their first unfinished game. Afterwards, watch `journalctl -u bc26-galaxy-saturn` for nacks.

## Host name and reboots

`bc26-replica-hostname.service` reads the external IP from the metadata server at boot and writes
`/etc/bc26-galaxy/hostname`. It re-renders the Caddyfile and restarts siarnaq if the IP changed. `deploy.sh` also
stores the URL in `~/.bc26-galaxy/site` on the driver. After a reboot, check
`tools/galaxy26/deploy/deploy.sh status`. If `bc23-replica-caddy` came up first and holds 443, this replica's Caddy
fails to bind. Freeing the ports means stopping the 2023 unit, and only the owner may decide that.

## Units and paths on battlecode-dev

Units: `bc26-galaxy-web` (gunicorn 127.0.0.1:8026), `bc26-galaxy-relay`, `bc26-galaxy-saturn`,
`bc26-replica-egress`, `postgresql@15-bc26` (unix socket only, port 5426, database `siarnaq26`; `pg_hba` host lines set
to reject), `bc26-replica-caddy`, `bc26-replica-hostname`, plus `bc26-galaxy-scheduler.timer` and
`bc26-galaxy-activity` (both disabled).

Paths:
- `/opt/bc26-galaxy`: `src/` (galaxy), `venv/`, `galaxy26/` (this directory), `runtime/` (JDK 21, engine, maps,
  maps-reversed, tool).
- `/srv/bc26-galaxy`: frontend, viewer, static, storage.
- `/home/bc26replica/galaxy`: secrets, spool, saturn work and resume.
- `/etc/bc26-galaxy`: env, local.env, Caddyfile, egress.nft, owner password (root 600), owner.bcrypt, hostname,
  audit outputs.
- CLI: `sudo bc26-galaxy-manage <manage.py command> | bootstrap ... | relay status | saturn status`.

Secrets live only on the VM and in the driver's home: `~/.bc26b-replica-password` (gate + `owner`),
`~/.bc26b-galaxy-team` (our team), `~/.bc26-galaxy/accounts.json` (all team accounts). None is in git.
