#!/usr/bin/env bash
# Keep sending research/panel/queue.tsv's replica requests as the hourly limit allows: one `contest.py drain` every
# DRAIN_EVERY seconds (default 300) until build/drain.stop exists. Log: build/drain.log.
# Each pass also moves our team's queued matches ahead of the field's on the VM (spool_bump.py --team), so an
# autoscrim round (60+ ranked matches every 8 hours) never sits in front of a panel block: the field's matches
# against each other get the spare cycles. BUMP_TEAM= (empty) turns that off.
cd "$(dirname "$0")/../.."
Q="${1:-research/panel/queue.tsv}"
TEAM="${BUMP_TEAM-vibe26b}"
while [ ! -e build/drain.stop ]; do
  if [ -s "$Q" ]; then
    echo "== $(TZ=America/Los_Angeles date '+%m-%d %H:%M PDT')"
    timeout 600 python3 -I tools/galaxy26/contest.py drain "$Q" 2>&1 | grep -v '^request ' | tail -3
  fi
  if [ -n "$TEAM" ]; then
    n=$( (source tools/vm.sh && ensure_vm >/dev/null && timeout 120 ssh "${SSHO[@]}" "$USER_NAME@$IP" \
          "cd /opt/bc26-galaxy/galaxy26 && sudo -u bc26replica python3 -I spool_bump.py --team $TEAM" < /dev/null) \
          2>/dev/null | grep -c ' -> ')
    [ "${n:-0}" -gt 0 ] && echo "== $(TZ=America/Los_Angeles date '+%m-%d %H:%M PDT') bumped $n of our queued matches"
  fi
  sleep "${DRAIN_EVERY:-300}"
done
rm -f build/drain.stop
