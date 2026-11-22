#!/usr/bin/env bash
# Refresh the progress/ time charts (owner 11-21: graphs on a wall-clock axis, kept up to date at every prod):
#   local-elo.png/.md        local tournament champions and candidates (tools/tourney.py chart)
#   rating-history.png/.md   our displayed replica rating over time     (tools/replica_report.py)
#   ladder-position.png/.csv our rank on the replica ladder over time   (tools/replica_report.py; also ladder.md/.png)
# Pull the tournament runs first. Commits and pushes just these files when they changed.
cd "$(dirname "$0")/.." || exit 1
PY=tools/.venv/bin/python
timeout 300 "$PY" tools/tourney.py chart || echo "!! tourney chart FAILED"
timeout 900 "$PY" -I tools/replica_report.py || echo "!! replica_report FAILED"
F="progress/local-elo.png progress/local-elo.md progress/rating-history.png progress/rating-history.md
   progress/ladder-position.png progress/ladder-position.csv progress/ladder.md progress/ladder.png research/tourney/champions.csv"
git add $F
if ! git diff --cached --quiet -- $F; then
  git commit -q -m "progress charts $(TZ=America/Vancouver date '+%m-%d %H:%M') PDT

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01SzziPV5YmmvGU24CuZFoPc" -- $F && git push -q && echo "charts committed"
fi
grep -E 'Snapshot|vibe26b' progress/ladder.md | cut -c1-100
