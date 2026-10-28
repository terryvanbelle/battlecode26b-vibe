#!/usr/bin/env bash
# Freeze the development bot as an immutable snapshot: snapshot.sh NAME [SRC=bots/ratbot]
# Copies SRC to bots/NAME, renames the package, refuses to overwrite, records the source hash in bots/NAME/SNAPSHOT.
set -euo pipefail
REPO="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
NAME="$1"; SRC="${2:-$REPO/bots/ratbot}"
[[ "$NAME" =~ ^[a-z][a-z0-9_]*$ ]] || { echo "snapshot: bad package name $NAME" >&2; exit 1; }
DST="$REPO/bots/$NAME"
[ -e "$DST" ] && { echo "snapshot: $DST exists" >&2; exit 1; }
[ -d "$SRC" ] && ls "$SRC"/*.java >/dev/null 2>&1 || { echo "snapshot: no java sources in $SRC" >&2; exit 1; }
OLD="$(basename "$SRC")"
mkdir -p "$DST"
for f in "$SRC"/*.java; do sed -e "s/^package $OLD;/package $NAME;/" -e "s/\b$OLD\./$NAME./g" "$f" > "$DST/$(basename "$f")"; done
grep -L "^package $NAME;" "$DST"/*.java | grep -q . && { echo "snapshot: package rename failed" >&2; rm -rf "$DST"; exit 1; }
SRCHASH=$(cd "$SRC" && cat $(ls *.java | sort) | sha256sum | cut -c1-12)
printf 'name=%s\nsource=%s\nsource_sha=%s\ncommit=%s\ncreated=%s\n' "$NAME" "${SRC#$REPO/}" "$SRCHASH" \
  "$(git -C "$REPO" rev-parse --short HEAD)" "$(TZ=America/Los_Angeles date '+%Y-%m-%d %H:%M %Z')" > "$DST/SNAPSHOT"
"${BC_JDK:-$HOME/opt/jdk21}/bin/javac" -nowarn --release 21 -proc:none -d "$(mktemp -d)" -cp "$REPO/engine/battlecode26-java-1.2.6.jar" "$DST"/*.java
echo "snapshot: bots/$NAME"
