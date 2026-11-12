#!/bin/bash
# chains.sh OUTDIR P < specs: each spec line "NAME BOTA BOTB MAP SEED"; arena games P at a time, logs OUTDIR/NAME.log (dumps kept)
R=$1; P=${2:-2}; mkdir -p $R
cd ~/projects/vibe/2026b
xargs -P $P -L 1 sh -c 'nice -n 19 python3 tools/arena.py game bots/$1 bots/$2 $3 --seed $4 --keep --tsv --out '$R'/$0 > '$R'/$0.log 2>&1'
