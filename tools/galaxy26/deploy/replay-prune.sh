#!/bin/sh
# Prune the bc26 replica's replay storage when the VM root disk runs low (owner 11-21, after the 11-21 disk-full outage).
# Installed as /usr/local/sbin/bc26-replay-prune, run hourly by /etc/cron.d/bc26-replay-prune (root).
# Only acts when free space on / is under MIN_FREE_KB; then deletes replays older than one day (the running hourly
# block's replays are minutes old), logging each deleted file to /var/log/bc26-replay-prune.log.
R=/srv/bc26-galaxy/storage/bc26-replica-secure/episode/bc26/replays
L=/var/log/bc26-replay-prune.log
MIN_FREE_KB=4000000
free=$(df -Pk / | awk 'NR==2 {print $4}')
[ "$free" -ge "$MIN_FREE_KB" ] && exit 0
echo "$(date -u '+%F %T') free ${free} KB < ${MIN_FREE_KB}: pruning replays older than 1 day" >> "$L"
find "$R" -type f -mtime +0 -printf '%T+ %s %p\n' -delete >> "$L" 2>&1
echo "$(date -u '+%F %T') done: free $(df -Pk / | awk 'NR==2 {print $4}') KB" >> "$L"
