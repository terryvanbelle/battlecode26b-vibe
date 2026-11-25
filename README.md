# battlecode26b-vibe

**Status (2026-11-25 07:55 PDT): shut down by the owner.** The local tournament is stopped and the replica ladder is
deleted; the final champion is sw96 (`bots/ratbot`); the last replica rank was #16 of 54. See `HANDOFF.md`.

Practice season for MIT Battlecode 2026 ("Uneasy Alliances"), built from scratch (clean room: the earlier
battlecode26-vibe effort and 2026 post-mortems are never read).

- `PROMPTS.md` owner prompts; `TRAINING_ALGORITHM.md` method; `RULES.md` engine-checked rules digest.
- `TRAINING_LOG.md` attempts; `LEARNINGS.md` measured lessons.
- `bots/ratbot` development bot; `bots/<name>` frozen snapshots (`tools/snapshot.sh`).
- `tools/arena.py` runner (`game`, `matrix`), `tools/cells.py`, `tools/rows.py`, `tools/game_report.py`,
  `tools/java/bc26tools` (ReplayDump, MapCensus, MapReseed), `tools/unit-tests.sh` (run after every change),
  `tools/vm.sh` compute VM helpers, `tools/galaxy26` private ladder replica (see ACCESS.md).
