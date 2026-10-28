#!/usr/bin/env bash
# Refresh progress/: VM run results + replica matches -> games.csv, Bradley-Terry ladder (ELO.md, elo.png),
# replica ladder and rating history, the progress chart, and the attempts table. Commit progress/ after.
# A step that fails or times out is named ("!! <step> FAILED") and the script exits 1.
cd "$(dirname "$0")/.."
PY=tools/.venv/bin/python
LOG="$(mktemp)"; trap 'rm -f "$LOG"' EXIT; FAILS=()
step () {   # step <name> <timeout s> <lines of output kept> <command ...>
  local name="$1" t="$2" n="$3" rc; shift 3
  echo "== $name"
  timeout "$t" "$@" > "$LOG" 2>&1; rc=$?
  tail -"$n" "$LOG"
  if [ "$rc" -ne 0 ]; then
    echo "!! $name FAILED (exit $rc$([ "$rc" = 124 ] && echo ", timed out after ${t} s"))"; FAILS+=("$name")
  fi
}
pull_runs () {   # every runs/*/results.jsonl on the VM -> build/vmruns/runs/
  source tools/vm.sh && ensure_vm > /dev/null &&
  rm -rf build/vmruns && mkdir -p build/vmruns &&
  gssh 'cd ~/projects/vibe/2026b && tar czf - runs/*/results.jsonl' | tar xzf - -C build/vmruns &&
  ls build/vmruns/runs | wc -l
}
echo "== start $(TZ=America/Los_Angeles date '+%H:%M PDT')"
export -f pull_runs
step "pull VM runs" 600 1 bash -c pull_runs
step "replica matches" 600 1 python3 -I tools/games.py replica
step games 300 1 python3 -I tools/games.py build
step elo 600 2 "$PY" -I tools/elo.py
step replica 600 1 "$PY" -I tools/replica_report.py
step chart 300 1 "$PY" -I tools/plot_progress.py
step attempts 300 1 "$PY" -I tools/attempts.py render
echo "== done $(TZ=America/Los_Angeles date '+%H:%M PDT')"
if [ "${#FAILS[@]}" -gt 0 ]; then echo "!! failed steps: ${FAILS[*]} (their progress/ files are stale)"; exit 1; fi
