#!/bin/sh
cd ~/projects/vibe/2026b
while :; do
  n=$(python3 -I tools/galaxy26/contest.py matches --pages 5 2>/dev/null | awk '$1>17486 && $2=="OK!" && $3=="unranked" && ($4=="vibe26b" || $7=="vibe26b")' | wc -l)
  [ "$n" -ge 16 ] && break
  sleep 120
done
echo "z0q set-3 block 1 done: $n"
