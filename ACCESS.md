# ACCESS.md: how to look at the bc26 scrimmage ladder

Our practice contest runs on a private copy of the Battlecode website on our VM `battlecode-dev`: galaxy's own
frontend and backend, the same pages as play.battlecode.org, with the Battlecode 2026 engine 1.2.6. Nothing there
touches the real play.battlecode.org. How it is built and run: `tools/galaxy26/README.md`.

## Open the site

1. Go to **https://galaxy26.136-86-167-127.sslip.io/**
2. The browser first asks for the site password. This gate sits in front of everything. User: **`owner`**.
   Password: run `! cat ~/.bc26b-replica-password` in this Claude Code session. The password is not in this
   repository, because the repository is public.
3. Then log in on galaxy's own login page (Log In, top right) as **`owner`** with the same password. `owner` is the
   site's superuser. It sees every team, match and replay, and `/admin/` (Django admin; read there, change nothing).

## To watch our games (replays)

Galaxy shows Replay buttons only on a team's own pages, so watch as our team, not as `owner`:

1. If you are logged in to galaxy as `owner`, log out (top right). Then log in as **`vibe26b`**. Password: run
   `! sed -n 2p ~/.bc26b-galaxy-team`.
2. In the left sidebar, open **Scrimmaging**, then **Scrim History**. It lists our matches, newest first. Press
   **Replay!** on a match to open the Battlecode 2026 client (`/viewer/`) on it. One file holds every game of the
   match; the client's game picker switches between them.
3. Replay files are also on disk:
   - `python3 tools/galaxy26/contest.py fetch --new` downloads our finished matches to
     `~/projects/vibe/bc26b-galaxy-replays/<match id>/replay.bc26`, with a `match.json` per-game summary. Open
     `replay.bc26` in any Battlecode 2026 client.
   - On the VM, they are under `/srv/bc26-galaxy/storage/bc26-replica-secure/episode/bc26/replays/`.

**Queue** (sidebar) lists every match on the ladder, with teams, scores and rating changes. As on
play.battlecode.org, it has no replays, because galaxy hides other teams' scrimmage replays from contestants.

## What is there (episode bc26, galaxy's own pages)

- **Rankings** (`/bc26/rankings`): every team and its rating. The rating is galaxy's displayed rating, a penalized
  Elo: every team starts at 0 and climbs over its first ranked matches.
  - Our team is **vibe26b**.
  - The test/field teams are **bc26-example** and **bc26-example-copy**, both the official examplefuncsplayer.
- **Queue** (`/bc26/queue`): every match, newest first, with status, score and rating change.
- **Team pages** (click a team name): profile, members, rating history.
- Every rating chart (Home, team pages, My Team) has a dashed line per project milestone, e.g. **M1** = the switch
  from Medium to Ultracode effort (2026-10-28 06:38 PDT). The list is `progress/milestones.csv`.
- **Scrimmaging**, **Submissions**, **My Team**: the logged-in team's own pages.
  - Submissions take a zip of the Java package folder, `<package>/RobotPlayer.java ...`, which is what the
    scaffold's `zipForSubmit` makes. The package name goes in the form.
  - The site compiles it with JDK 21 against engine 1.2.6 and checks it with the engine's Verifier.
- A match is played on the maps galaxy picks, usually 3. The score is games won.

Automatic play is **off** for now:
- galaxy's 8-hourly ranked round (autoscrim);
- the field teams' spare-time challenges.

Matches happen when a team requests them. Turning those on is described in the README, under "Timers".

## If the address stops working

The host name is built from the VM's external IP, which changes if the VM is stopped and started. A boot script
picks up the new IP and the certificate is renewed. `~/.bc26-galaxy/site` on the driver holds the current URL after
`tools/galaxy26/deploy/deploy.sh status`.

The 2023 replica's web server (paused) uses the same ports 80/443. If it starts first after a reboot, this site is
unreachable until it is stopped again, which only the owner should decide. If the site is unreachable, ask me.
