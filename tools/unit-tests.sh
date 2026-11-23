#!/usr/bin/env bash
# One command for every unit test: the bot's logic tests (fake RobotController) and the tool tests.
#
#   bash tools/unit-tests.sh          bots/ratbot (the champion) with test/ratbot/*.java, then the tool tests
#   bash tools/unit-tests.sh BOT      bots/BOT, compiled under package ratbot, with test/ratbot/*.java and
#                                     test/BOT/*.java when that directory exists: tests for code only BOT has (such
#                                     as an uncrowned candidate's fixes), and a test/BOT file replaces the
#                                     test/ratbot file of the same name (a check whose expected value BOT changes);
#                                     the tool tests are skipped
#
# Every test class runs; the exit status is 1 when any failed. When BOT is crowned (bots/ratbot = BOT), move
# test/BOT/*.java into test/ratbot/ (replacing same-named files) so the default run keeps them.
set -euo pipefail
REPO="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JDK="${BC_JDK:-$HOME/opt/jdk21}"; ENGINE="$REPO/engine/battlecode26-java-1.2.6.jar"
BOT="${1:-ratbot}"
[ -d "$REPO/bots/$BOT" ] || { echo "unit-tests: no bots/$BOT" >&2; exit 2; }
OUT="$REPO/build/tests"; rm -rf "$OUT"; mkdir -p "$OUT/classes"
SRC="$REPO/bots/$BOT"
if [ "$BOT" != ratbot ]; then
    # the tests are package ratbot: compile a renamed copy of the bot
    mkdir -p "$OUT/src"
    for f in "$SRC"/*.java; do sed "s/^package $BOT;/package ratbot;/" "$f" > "$OUT/src/$(basename "$f")"; done
    SRC="$OUT/src"
fi
TESTS=()
for t in "$REPO"/test/ratbot/*.java; do
    o="$REPO/test/$BOT/$(basename "$t")"
    if [ "$BOT" != ratbot ] && [ -f "$o" ]; then TESTS+=("$o"); else TESTS+=("$t"); fi
done
if [ "$BOT" != ratbot ] && [ -d "$REPO/test/$BOT" ]; then
    # an empty test/BOT (its tests moved to test/ratbot at a crowning) leaves the glob unexpanded: skip it
    for t in "$REPO"/test/"$BOT"/*.java; do [ -f "$t" ] || continue; [ -f "$REPO/test/ratbot/$(basename "$t")" ] || TESTS+=("$t"); done
fi
"$JDK/bin/javac" -nowarn --release 21 -proc:none -d "$OUT/classes" -cp "$ENGINE" "$SRC"/*.java "${TESTS[@]}"
echo "unit-tests: bots/$BOT"
FAILED=()
for t in "${TESTS[@]}"; do
    case "$t" in
        *Test.java) "$JDK/bin/java" -cp "$OUT/classes:$ENGINE" "ratbot.$(basename "$t" .java)" || FAILED+=("$(basename "$t" .java)");;
    esac
done
if [ ${#FAILED[@]} -gt 0 ]; then echo "unit-tests: FAILED ${FAILED[*]}" >&2; exit 1; fi
[ "$BOT" != ratbot ] && exit 0
PY=python3; [ -x "$REPO/tools/.venv/bin/python" ] && PY="$REPO/tools/.venv/bin/python"
"$REPO/tools/build-tools.sh"      # the tool tests run the dumper: a compile error stops here, with javac's output
"$PY" -I "$REPO/tools/test_tools.py"
# per-tool test modules (one file per tool, so tools can gain tests independently)
for t in "$REPO"/tools/tests/test_*.py; do [ -e "$t" ] && "$PY" -I "$t"; done
