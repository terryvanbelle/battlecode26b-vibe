#!/bin/bash
# score_screen.sh RUN BOT [CTRL_RUN CTRL_BOT]: the screen gate numbers for runs/RUN (bots/BOT) against a control run
# (default zc6/u6x): changed cells, funnel, picks/100, overruns, exceptions, totals on the shared cells.
R=$1; B=$2; C=${3:-zc6}; CB=${4:-u6x}
cd ~/projects/vibe/2026b
source tools/vm.sh; ensure_vm >/dev/null 2>&1
gssh "cd projects/vibe/2026b && python3 -I tools/screen_t.py $C $CB $R $B; echo \"\$(wc -l < runs/$R/results.jsonl) games \$(test -e runs/$R/DONE && echo DONE)\"; python3 -I ~/diag26b/cmpruns.py runs/$C/results.jsonl runs/$R/results.jsonl $CB $B | grep -E ' [+-]\$|^cells'; python3 -I ~/diag26b/funnel.py runs/$R/results.jsonl $B | tail -1; python3 -I -c \"
import json
def load(p, me):
    out = {}
    for l in open(p):
        d = json.loads(l); s = 'A' if d['a'] == me else 'B'; o = 'B' if s == 'A' else 'A'
        out[(d['a'] if s == 'B' else d['b'], d['map'], s)] = (d[s], d[o])
    return out
a = load('runs/$C/results.jsonl', '$CB'); b = load('runs/$R/results.jsonl', '$B')
k = sorted(set(a) & set(b))
print('shared', len(k), '| ctrl', '$CB', 'cand', '$B')
for f in ('overruns', 'exceptions', 'turnsRat', 'pickups', 'pickupAmt', 'spawned', 'deathsRat', 'killsRat', 'catDmgTrap', 'transferAmt', 'kingsFormed'):
    print(f, sum(a[x][0].get(f, 0) for x in k), sum(b[x][0].get(f, 0) for x in k))
print('opp ratTrapHits', sum(a[x][1].get('ratTrapHits', 0) for x in k), sum(b[x][1].get('ratTrapHits', 0) for x in k))
\"" < /dev/null
