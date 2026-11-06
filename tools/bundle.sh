#!/bin/bash
# tools/bundle.sh NAME BASE CAND...: bots/NAME = BASE + each CAND's changes against BASE. Cfg.java: each candidate's added
# lines go before the class's closing brace (they only add constants); other files: its diff, applied strictly.
# Fuzzy patching put a Cfg hunk inside a doc comment (2026-11-06), hence the append; check the merged switches by eye.
set -e
cd ~/projects/vibe/2026b
N=$1; B=$2; shift 2
S=$(mktemp -d)

bash tools/snapshot.sh $N bots/$B >/dev/null
cp -r bots/$B $S/base; sed -i "s/^package $B;/package P;/" $S/base/*.java; rm -f $S/base/SNAPSHOT
for c in "$@"; do
  rm -rf $S/c; cp -r bots/$c $S/c; sed -i "s/^package $c;/package P;/" $S/c/*.java; rm -f $S/c/SNAPSHOT
  if diff $S/base/Cfg.java $S/c/Cfg.java | grep -q '^<'; then echo "$c removes Cfg lines: merge by hand"; exit 1; fi
  diff $S/base/Cfg.java $S/c/Cfg.java | sed -n 's/^> //p' > $S/cfgadd || true
  python3 - "$S/cfgadd" bots/$N/Cfg.java <<'PY'
import sys
add = open(sys.argv[1]).read(); p = sys.argv[2]; s = open(p).read()
i = s.rstrip().rfind('}')
s = s[:i] + add + s[i:]
open(p, 'w').write(s)
PY
  cp $S/c/Cfg.java $S/c/Cfg.java.keep; cp $S/base/Cfg.java $S/c/Cfg.java
  (cd $S && diff -ruN base c -x '*.keep' > $c.diff || true)
  sed -i "s/^package $N;/package P;/" bots/$N/*.java
  (cd bots/$N && patch -p1 -F0 --no-backup-if-mismatch -s < $S/$c.diff)
  sed -i "s/^package P;/package $N;/" bots/$N/*.java
  echo "applied $c ($(grep -c '^[+-][^+-]' $S/$c.diff) lines + $(wc -l < $S/cfgadd) Cfg lines)"
done
rm -rf $S
