#!/bin/bash
# The owner's Windows box (WSL2 Ubuntu, i7-13700HX, 20 threads, 8 GB; over Tailscale, reached only from the laptop) as
# the local game machine. Owner #35/#36: it never plays replica (panel) matches; it runs every local game and analysis.
# Checkout: ~/projects/vibe/b26b (git clone of this repo; engine jar and ~/opt/jdk21 installed), its own screen queue.
#   tools/box.sh sh CMD...          run CMD in the box checkout
#   tools/box.sh send PATH...       copy repo paths (bots/NAME, runs/NAME, tools, research/...) to the box checkout
#   tools/box.sh queue RUN...       send runs/RUN (cells.tsv) and the bots its cells name, then queue it on the box
#   tools/box.sh pull RUN...        copy runs/RUN back (results.jsonl, log, DONE; replays only when KEEP)
#   tools/box.sh start [JOBS]       start the box screen queue (tools/screenq.sh; default 6 games at once)
#   tools/box.sh status             queue, running games, load, memory
cd "$(dirname "$0")/.." || exit 1
BOX=(ssh -i ~/.ssh/wsl_box -o ConnectTimeout=20 -o ServerAliveInterval=30 -o BatchMode=yes terry@100.93.26.126)
D=projects/vibe/b26b
box() { timeout "${T:-300}" "${BOX[@]}" "cd $D && $*"; }
send() { tar czf - "$@" | T=900 box "tar xzf -"; }
case $1 in
  sh) shift; box "$@" ;;
  send) shift; send "$@" ;;
  queue) shift
    for r in "$@"; do
      [ -s runs/$r/cells.tsv ] || { echo "no runs/$r/cells.tsv"; exit 1; }
      bots=$(cut -f1,2 runs/$r/cells.tsv | tr '\t' '\n' | grep '^bots/' | sort -u)
      send runs/$r $bots && box "echo $r >> runs/screenq.txt" && echo "queued $r ($(wc -l < runs/$r/cells.tsv) cells; $(echo $bots))"
    done ;;
  pull) shift
    for r in "$@"; do T=900 box "tar czf - runs/$r" | tar xzf - && echo "pulled $r: $(wc -l < runs/$r/results.jsonl 2>/dev/null) results$( [ -e runs/$r/DONE ] && echo ', DONE')"; done ;;
  start) box "rm -f build/screenq.stop; mkdir -p runs; (BC_SLOTS=${2:-6} BC_JDK=\$HOME/opt/jdk21 setsid nohup sh tools/screenq.sh ${2:-6} > /dev/null 2>&1 < /dev/null &); echo started" ;;
  status) box "echo queue: \$(cat runs/screenq.txt 2>/dev/null | tr '\n' ' '); tail -3 runs/screenq.log 2>/dev/null; pgrep -c -f battlecode.server.Main || true; uptime; free -g | sed -n 2p" ;;
  *) sed -n 2,11p "$0"; exit 2 ;;
esac
