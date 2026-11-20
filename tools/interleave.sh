#!/bin/bash
# Interleave the champion's and a challenger's n3 (was e21) panel batches (owner 11-20): make BUILD the active replica
# submission and make sure its n3 (was e21) rows were queued once, so the drain's next hourly batch is BUILD's. siarnaq fixes a
# match's submission when the match is created, so switching between batches leaves earlier matches with their bot.
#   bash tools/interleave.sh BUILD
# The drain sends only rows tagged with the active build; the other build's rows wait ("held for other builds").
# build/n3-queued-BUILD marks a build whose panel rows were queued, so a finished panel is never queued twice
# (replica games are deterministic: a repeat is wasted).
cd "$(dirname "$0")/.." || exit 1
B=${1:?usage: interleave.sh BUILD}
C=tools/galaxy26/contest.py
active=$(grep 'active build' build/drain.log | tail -1 | sed -n 's/.*active build \([^;]*\);.*/\1/p')
if [ "$active" != "$B" ]; then
  python3 -I $C submit bots/$B -m "interleave: $B panel batch" 2>&1 | tail -1
fi
if [ ! -e build/n3-queued-$B ]; then
  bash build/block_n3.sh 2>&1 | grep -E "queued" | tail -1 && touch build/n3-queued-$B
fi
echo "rows queued for $B: $(grep -c "^$B	" research/panel/queue.tsv)"
