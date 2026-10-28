#!/usr/bin/env bash
# One command for every unit test: the bot's logic tests (fake RobotController) and the tool tests.
set -euo pipefail
REPO="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JDK="${BC_JDK:-$HOME/opt/jdk21}"; ENGINE="$REPO/engine/battlecode26-java-1.2.6.jar"
OUT="$REPO/build/tests"; rm -rf "$OUT"; mkdir -p "$OUT"
"$JDK/bin/javac" -nowarn --release 21 -proc:none -d "$OUT" -cp "$ENGINE" "$REPO"/bots/ratbot/*.java "$REPO"/test/ratbot/*.java
for t in "$REPO"/test/ratbot/*Test.java; do "$JDK/bin/java" -cp "$OUT:$ENGINE" "ratbot.$(basename "$t" .java)"; done
PY=python3; [ -x "$REPO/tools/.venv/bin/python" ] && PY="$REPO/tools/.venv/bin/python"
"$PY" -I "$REPO/tools/test_tools.py"
# per-tool test modules (one file per tool, so tools can gain tests independently)
for t in "$REPO"/tools/tests/test_*.py; do [ -e "$t" ] && "$PY" -I "$t"; done
