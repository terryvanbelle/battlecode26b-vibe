#!/bin/sh
# A screen queue on the VM: each worker takes the first run name from runs/screenq.txt (one a line, runs/NAME/cells.tsv
# prepared beforehand) and plays it with arena.py matrix, until build/screenq.stop exists.
#   nohup sh tools/screenq.sh JOBS > /dev/null 2>&1 < /dev/null &      (one worker; start more for more streams)
#   echo ra_s5s >> runs/screenq.txt                                       (queue a run; order is play order)
# The take is under flock, so two workers never claim the same run. Log: runs/screenq.log.
cd "$(dirname "$0")/.." || exit 1
JOBS="${1:-4}"; Q=runs/screenq.txt; L=runs/screenq.lock
touch "$Q"
while [ ! -e build/screenq.stop ]; do
  R=$(flock "$L" sh -c "R=\$(sed -n '/./{p;q}' $Q); [ -n \"\$R\" ] && sed -i '0,/./{/./d}' $Q; echo \$R")
  if [ -z "$R" ]; then sleep 60; continue; fi
  if [ ! -s "runs/$R/cells.tsv" ] || [ -e "runs/$R/DONE" ]; then
    echo "$(date -u '+%m-%d %H:%M') skip $R (no cells or already done)" >> runs/screenq.log; continue
  fi
  echo "$(date -u '+%m-%d %H:%M') start $R (jobs $JOBS)" >> runs/screenq.log
  python3 -I tools/arena.py matrix "runs/$R/cells.tsv" --run "runs/$R" --jobs "$JOBS" > "runs/$R/log" 2>&1
  echo "$(date -u '+%m-%d %H:%M') end $R" >> runs/screenq.log
done
