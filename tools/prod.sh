#!/bin/bash
# Idle check for the periodic prod (owner 11-19: never sit idle). Prints the box queue, whether games are running,
# finished-but-unpulled screens, and the replica drain backlog; the last line is a verdict: IDLE (start work now)
# or BUSY.
cd "$(dirname "$0")/.." || exit 1
st=$(bash tools/box.sh status 2>/dev/null)
q=$(echo "$st" | sed -n 's/^queue: *//p')
load=$(echo "$st" | sed -n 's/.*load average: \([0-9.]*\).*/\1/p')
echo "now: $(TZ=America/Vancouver date '+%m-%d %H:%M PDT')"
echo "box queue: ${q:-<empty>}   load: ${load:-?}"
# box log stamps are UTC "MM-DD HH:MM"; show them in PDT (UTC-7, BC's year-round time) (owner 11-19: report all times as PDT)
echo "$st" | grep -E ' (start|end) ' | tail -2 | while read -r d t rest; do
  echo "  $(TZ=America/Vancouver date -d "$(date -u +%Y)-$d $t UTC" '+%m-%d %H:%M PDT') $rest"; done
done=$(bash tools/box.sh sh 'for d in runs/*/DONE; do echo ${d%/DONE}; done' 2>/dev/null | sed 's#runs/##' | sort)
unpulled=$(for r in $done; do [ -f runs/$r/DONE ] || echo -n "$r "; done)
echo "finished on box, not pulled: ${unpulled:-none}"
[ -f research/panel/queue.tsv ] && echo "replica rows queued for the drain: $(grep -vc '^#' research/panel/queue.tsv)"
# screens started but not ended (screenq takes a run off the queue when it starts it)
running=$(bash tools/box.sh sh 'awk "\$3==\"start\"{s[\$4]=1} \$3==\"end\"{delete s[\$4]} END{for(r in s) printf r\" \"}" runs/screenq.log' 2>/dev/null)
echo "running on box: ${running:-none}"
idle=0; [ -z "$q" ] && [ -z "$running" ] && idle=1
[ $idle = 1 ] && echo "VERDICT: IDLE - box has no queue and no games running" || echo "VERDICT: BUSY"
