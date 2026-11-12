#!/bin/bash
# chainsn.sh OUTDIR P < specs: as chains.sh ("NAME BOTA BOTB MAP SEED"), no dumps kept; logs OUTDIR/NAME.log
R=$1; P=${2:-2}; mkdir -p $R
cd ~/projects/vibe/2026b
xargs -P $P -L 1 sh -c 'nice -n 19 python3 tools/arena.py game bots/$1 bots/$2 $3 --seed $4 > '$R'/$0.log 2>&1'
