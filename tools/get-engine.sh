#!/usr/bin/env bash
# Fetch the official Battlecode 2026 engine 1.2.6 (read-only download from the public release host) and verify it.
# This is the only contact this project makes with battlecode.org infrastructure.
set -euo pipefail
cd "$(dirname "$0")/../engine"
B=https://releases.battlecode.org/maven/org/battlecode/battlecode26-java/1.2.6
for f in battlecode26-java-1.2.6.jar battlecode26-java-1.2.6-sources.jar battlecode26-java-1.2.6-javadoc.jar; do
  [ -f "$f" ] || curl -sfL -o "$f" "$B/$f"
done
sha256sum -c SHA256SUMS
