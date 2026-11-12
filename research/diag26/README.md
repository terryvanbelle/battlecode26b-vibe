# diag26: replica-loss analysis scripts (2026-11-12 x-line, starvation focus)

Copied from laptop scratch (/dev/shm/kd) at shutdown. The VM copies lived in /dev/shm/kx and /dev/shm/dcx and were lost
when logind emptied the VM's /dev/shm (LEARNINGS 20). Paths inside the scripts point at those scratch dirs; re-create
them (or edit the paths) before use.

Replica dumps come from `~/diag26b/dumpone.py REPLAY` on the VM (BC_DUMPCACHE=/dev/shm/dcx). Each dump writes
gN.{meta,summary}.json and gN.{rounds,turns,actions}.tsv. Replays are archived in VM ~/diag26b/rp/*.tgz.

| script | where | use |
|---|---|---|
| losscount.py | VM | classify every dumped replica loss by cause (starve, bite, points, throwHit, scratch) |
| starve.py, starve*.py | VM | spend and bank breakdown of each starvation loss |
| lc2.py "IDS" | VM | per-game line: map, seat, result, rounds, backstab round, king-death causes, spawns, transfers |
| aust.py "IDS" | VM | rounds with at most 4 babies and a bank of 700+ (the austerity/reserve hold) |
| catdist.py TURNS MINROUND | VM | rats' distance to cats |
| kmsum.py DIR | VM | one line per local probe game in a chains.sh run dir (traps, trap deaths, transfer, king deaths) |
| chains.sh, chainsn.sh, chainscore.py, kmcmp.py | VM | local probe chains: `bash chains.sh DIR P < specs` (spec lines "NAME BOTA BOTB MAP SEED"), compare two runs |
| panelcmp.py "IDS" | laptop | each replica cell's result vs its earlier history, FLIP+/FLIP- tags |
| anyrate.py MINID "IDS" | laptop | anyrate (wins minus base rate) of a set of matches |
| gen_fetch.py, fetch_xj6a.sh | laptop | fetch replica replays, tar them to VM ~/diag26b/rp/NAME.tgz, verify, then delete the local copies |
