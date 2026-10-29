#!/usr/bin/env bash
# Compile tools/java (bc26tools.*, the flatbuffers helper) into build/tools. Silent on success; idempotent:
# does nothing while the classes are newer than every source (-f forces). Atomic: compiles into a fresh
# build/.tools-<n> and repoints the build/tools symlink, so running jobs never see a half-written tree.
# Usage: tools/build-tools.sh [-f]     exit 1 (javac output on stderr) if the sources do not compile.
set -euo pipefail
REPO="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JDK="${BC_JDK:-$HOME/opt/jdk21}"; ENGINE="$REPO/engine/battlecode26-java-1.2.6.jar"
SRC="$REPO/tools/java"; B="$REPO/build"; DST="$B/tools"
mkdir -p "$B"
exec 9>"$B/.tools.lock"; flock 9
stale() {
    [ "${1:-}" = -f ] || [ ! -f "$DST/.stamp" ] || [ ! -f "$DST/bc26tools/ReplayDump.class" ] && return 0
    [ -n "$(find "$SRC" -name '*.java' -newer "$DST/.stamp" -print -quit)" ] || [ "$ENGINE" -nt "$DST/.stamp" ]
}
stale "${1:-}" || exit 0
TMP="$(mktemp -d "$B/.tools-tmp-XXXXXX")"; chmod 755 "$TMP"
trap 'rm -rf "$TMP"' EXIT
touch "$TMP/.stamp"   # before javac: a source edited during the compile stays newer than the stamp
mapfile -t FILES < <(find "$SRC" -name '*.java')
if ! LOG="$("$JDK/bin/javac" -nowarn -Xlint:none --release 21 -proc:none -cp "$ENGINE" -d "$TMP" "${FILES[@]}" 2>&1)"; then
    echo "$LOG" >&2; echo "build-tools: javac failed" >&2; exit 1
fi
N=0; while [ -e "$B/.tools-$N" ]; do N=$((N + 1)); done
mv "$TMP" "$B/.tools-$N"; trap - EXIT
ln -s ".tools-$N" "$B/.tools-link-$N"
if [ -d "$DST" ] && [ ! -L "$DST" ]; then mv "$DST" "$B/.tools-old-$$"; fi   # first run: a plain directory
mv -T "$B/.tools-link-$N" "$DST"
for d in "$B"/.tools-* ; do
    [ "$d" = "$B/.tools-$N" ] || [ "$d" = "$B/.tools.lock" ] || rm -rf "$d"
done
