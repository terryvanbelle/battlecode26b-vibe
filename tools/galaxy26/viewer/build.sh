#!/usr/bin/env bash
# Build the Battlecode 2026 web client (the replay viewer the galaxy frontend's "Replay!" links open as
# /viewer/index.html?gameSource=/storage/...) from the official battlecode26 repository (GPL-3.0), on battlecode-dev.
#   bash tools/galaxy26/viewer/build.sh           (as the operator user; node from frontend/install-node.sh)
# Input: $VIEWER_TAR (default ~/bc26-galaxy-stage/viewer-src.tar) = `git archive <commit> client schema` of
# github.com/battlecode/battlecode26, made by deploy.sh viewer-src from the driver's reference checkout; its pax comment
# names the commit, which must be $CLIENT_COMMIT.
# Changes made to the copy before building (nothing else):
#   - package.json / package-lock.json: drop the desktop-only dependency where-is-it (a git+ssh GitHub URL, used by the
#     Electron app's Java finder, never by the web build);
#   - index.html: drop the Google Fonts stylesheet (the UI falls back to the system monospace font), add the replica's
#     Content-Security-Policy meta tag (same as the site's header).
# Then npm ci --ignore-scripts in schema/ and client/ (registry.npmjs.org only, lockfile integrity; no install script: Electron and Tauri
# binaries are not downloaded), webpack production build, frontend_check.py audit of dist/ (no automatic contact with
# another host), output in $OUT (default ~/bc26-galaxy-build/client-dist) for `galaxy-setup.sh viewer`.
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CLIENT_COMMIT=103abf6b67a2cf544e6344dddef9318af9ae9193      # battlecode26 main, 2026-02-09 (client 1.0.x)
VIEWER_TAR="${VIEWER_TAR:-$HOME/bc26-galaxy-stage/viewer-src.tar}"
NODE_HOME="${NODE_HOME:-$HOME/opt/node}"
WORK="${WORK:-$HOME/bc26-galaxy-build/client}"
OUT="${OUT:-$HOME/bc26-galaxy-build/client-dist}"
CSP="default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data: blob:; font-src 'self' data:; connect-src 'self'; frame-src 'self'; worker-src 'self' blob:; manifest-src 'self'; object-src 'none'; base-uri 'self'; form-action 'self'"
[ -x "$NODE_HOME/bin/node" ] || { echo "viewer build: no node in $NODE_HOME (run tools/galaxy26/frontend/install-node.sh)" >&2; exit 1; }
export PATH="$NODE_HOME/bin:$PATH"
[ -s "$VIEWER_TAR" ] || { echo "viewer build: no $VIEWER_TAR (deploy.sh viewer-src)" >&2; exit 1; }
c=$(python3 -c 'import sys, tarfile; print(tarfile.open(sys.argv[1]).pax_headers.get("comment", ""))' "$VIEWER_TAR")
[ "$c" = "$CLIENT_COMMIT" ] || { echo "viewer build: $VIEWER_TAR is a git archive of '$c', not $CLIENT_COMMIT" >&2; exit 1; }

rm -rf "$WORK"; mkdir -p "$WORK"
tar -x -C "$WORK" --no-same-owner -f "$VIEWER_TAR" client schema
cd "$WORK/client"
python3 - "$CSP" <<'PY'
import json, re, sys
csp = sys.argv[1]
pj = json.load(open('package.json'))
pj['dependencies'].pop('where-is-it')
json.dump(pj, open('package.json', 'w'), indent=4)
lock = json.load(open('package-lock.json'))
lock['packages'][''].get('dependencies', {}).pop('where-is-it')
lock['packages'].pop('node_modules/where-is-it')
json.dump(lock, open('package-lock.json', 'w'), indent=4)
html = open('index.html').read()
html, n = re.subn(r'\s*<link href="https://fonts\.googleapis\.com/[^"]*" rel="stylesheet" />', '', html)
assert n == 1, 'Google Fonts link not found'
html = html.replace('<meta charset="UTF-8">',
                    '<meta charset="UTF-8">\n    <meta http-equiv="Content-Security-Policy" content="%s">' % csp, 1)
assert 'Content-Security-Policy' in html
open('index.html', 'w').write(html)
print('viewer build: patched package.json, package-lock.json, index.html')
PY
if grep -rq 'git+ssh\|"resolved": "git' package-lock.json; then echo "viewer build: lockfile still has a git dependency" >&2; exit 1; fi
echo "viewer build: node $(node --version), npm $(npm --version)"
export NODE_ENV=production
# the schema package's own dependencies (flatbuffers) first, from its own lockfile: upstream's `npm run fix-schema`
(cd ../schema && nice -n 19 npm ci --ignore-scripts --no-audit --no-fund --loglevel=error --omit=dev)
nice -n 19 npm ci --ignore-scripts --no-audit --no-fund --loglevel=error --include=dev
nice -n 19 node node_modules/webpack/bin/webpack.js build --config webpack.config.js --mode production
DIST="$WORK/client/dist"
[ -f "$DIST/index.html" ] || { echo "viewer build: no $DIST/index.html" >&2; exit 1; }
find "$DIST" -name '*.map' -delete
python3 "$HERE/../frontend_check.py" "$DIST" > "$WORK/bundle-urls.txt" \
  || { grep -E '^FAIL|^bundle' "$WORK/bundle-urls.txt"; echo "viewer build: bundle check FAILED; not installed" >&2; exit 1; }
tail -2 "$WORK/bundle-urls.txt"
echo "battlecode26 client $CLIENT_COMMIT (patched by tools/galaxy26/viewer/build.sh), node $(node --version), built $(date -u +%FT%TZ)" > "$DIST/REPLICA_BUILD.txt"
rm -rf "$OUT.new"; cp -r "$DIST" "$OUT.new"; rm -rf "$OUT"; mv "$OUT.new" "$OUT"
[ "${KEEP_NODE_MODULES:-0}" = 1 ] || rm -rf "$WORK/client/node_modules" "$WORK/schema/node_modules"
echo "viewer build: $OUT ($(find "$OUT" -type f | wc -l) files, $(du -sh "$OUT" | cut -f1))"
