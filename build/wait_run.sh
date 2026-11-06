#!/bin/bash
# wait until runs/$1/DONE exists on the VM
cd ~/projects/vibe/2026b
source tools/vm.sh
ensure_vm >/dev/null 2>&1
while :; do
  n=$(gssh "test -e ~/projects/vibe/2026b/runs/$1/DONE && echo yes || echo no" < /dev/null 2>/dev/null | tail -n 1)
  [ "$n" = "yes" ] && break
  sleep 120
done
echo "$1 done"
