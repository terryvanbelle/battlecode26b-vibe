#!/usr/bin/env bash
# Deploy (or redeploy) the bc26 galaxy replica on battlecode-dev from the driver. Idempotent. Doc: tools/galaxy26/README.md.
#   tools/galaxy26/deploy/deploy.sh              push the stage, then galaxy-setup.sh all
#   tools/galaxy26/deploy/deploy.sh STEP [ARG]   push the stage, then one galaxy-setup.sh step (status, app, runtime,
#                                                units, start, viewer, frontend, site, scheduler on|off, activity on|off ...)
# The stage on the VM (~/bc26-galaxy-stage) holds tools/galaxy26, engine/ (the pinned 1.2.6 jar), maps/ (the 74 .map26),
# galaxy.tar (git archive of the pinned galaxy commit, from the reference clone) and viewer-src.tar (git archive of
# client/ and schema/ of the pinned battlecode26 commit, for viewer/build.sh). Nothing is downloaded from battlecode.org.
# Afterwards the driver has the site gate password (~/.bc26b-replica-password, 600) and the site URL (~/.bc26-galaxy/site).
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"; REPO="$(cd "$HERE/../../.." && pwd)"
# shellcheck source=../vm.sh
source "$REPO/tools/galaxy26/vm.sh"; ensure_vm
[ "$(hostname -s)" != battlecode-dev ] || { echo "deploy: run this on the driver" >&2; exit 1; }
STAGE=bc26-galaxy-stage
GALAXY_REF="${GALAXY_REF:-$HOME/projects/vibe/reference/galaxy}"
GALAXY_COMMIT=f343088f4d471b0664b2ff41129f0435a8dca1c2
BC26_REF="${BC26_REF:-$HOME/projects/vibe/reference/battlecode26}"
CLIENT_COMMIT=103abf6b67a2cf544e6344dddef9318af9ae9193
RS=(rsync -a --delete --exclude __pycache__ -e "ssh ${SSHO[*]}")
TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT

echo "== stage ~/$STAGE on $VM"
gssh "mkdir -p ~/$STAGE/tools"
"${RS[@]}" "$REPO/tools/galaxy26/" "$USER_NAME@$IP:$STAGE/tools/galaxy26/"
"${RS[@]}" "$REPO/engine/" "$USER_NAME@$IP:$STAGE/engine/"
"${RS[@]}" --include '*.map26' --exclude '*' "$REPO/maps/" "$USER_NAME@$IP:$STAGE/maps/"
git -C "$GALAXY_REF" archive --format=tar -o "$TMP/galaxy.tar" "$GALAXY_COMMIT"
git -C "$BC26_REF" archive --format=tar -o "$TMP/viewer-src.tar" "$CLIENT_COMMIT" client schema
rsync -a --checksum -e "ssh ${SSHO[*]}" "$TMP/galaxy.tar" "$TMP/viewer-src.tar" "$USER_NAME@$IP:$STAGE/"

SETUP="sudo bash ~/$STAGE/tools/galaxy26/deploy/galaxy-setup.sh"
if [ $# -gt 0 ]; then
  case "$1" in
    stage) exit 0 ;;
    all|app|runtime|src|venv) gssh "$SETUP $1 ~/$STAGE" ;;
    *) gssh "$SETUP $*" ;;
  esac
else
  gssh "$SETUP all ~/$STAGE"
fi

echo "== driver: gate password and site URL"
umask 077
gssh 'cat ~/.bc26b-replica-password 2>/dev/null' > "$HOME/.bc26b-replica-password.new" || true
if [ -s "$HOME/.bc26b-replica-password.new" ]; then mv -f "$HOME/.bc26b-replica-password.new" "$HOME/.bc26b-replica-password"
else rm -f "$HOME/.bc26b-replica-password.new"; fi
chmod 600 "$HOME/.bc26b-replica-password" 2>/dev/null || true
host=$(gssh 'cat /etc/bc26-galaxy/hostname 2>/dev/null' || true)
if [ -n "$host" ]; then
  mkdir -p "$HOME/.bc26-galaxy"; chmod 700 "$HOME/.bc26-galaxy"
  echo "https://galaxy26.$host" > "$HOME/.bc26-galaxy/site"
  echo "site: https://galaxy26.$host/"
fi
