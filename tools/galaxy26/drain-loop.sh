#!/usr/bin/env bash
# Keep sending research/panel/queue.tsv's replica requests as the hourly limit allows: one `contest.py drain` every
# DRAIN_EVERY seconds (default 300) until build/drain.stop exists. Log: build/drain.log.
cd "$(dirname "$0")/../.."
Q="${1:-research/panel/queue.tsv}"
while [ ! -e build/drain.stop ]; do
  if [ -s "$Q" ]; then
    echo "== $(TZ=America/Los_Angeles date '+%m-%d %H:%M PDT')"
    timeout 600 python3 -I tools/galaxy26/contest.py drain "$Q" 2>&1 | grep -v '^request ' | tail -3
  fi
  sleep "${DRAIN_EVERY:-300}"
done
rm -f build/drain.stop
