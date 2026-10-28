#!/usr/bin/env bash
# Shared helpers for the compute VM. Sourced, not run:  source tools/vm.sh && ensure_vm
# battlecode-dev (us-west1-b, e2-standard-8: 8 vCPU / 31 GB / 20 GB disk) runs every game in volume and hosts the
# private galaxy replica. The driver (claude-driver, e2-small) runs the session and at most one diagnostic game.
# battlecode-dev2 belongs to the paused 2024 project and is never touched from here.
# The VM mirrors the driver layout: ~/projects/vibe/2026b, ~/projects/vibe/bc26b-benchmarks.
VM=battlecode-dev; ZONE=us-west1-b; PROJECT=tvanbelle-vibecode
REMOTE_REPO='projects/vibe/2026b'
SSHO=(-i "$HOME/.ssh/google_compute_engine" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null
      -o ConnectTimeout=20 -o ServerAliveInterval=20 -o ServerAliveCountMax=3 -o LogLevel=ERROR)
USER_NAME="${BC_SSH_USER:-$(whoami)}"
vm_ip () { gcloud compute instances describe "$VM" --zone="$ZONE" --project="$PROJECT" --format='value(networkInterfaces[0].networkIP)' 2>/dev/null; }
gssh () { ssh "${SSHO[@]}" "$USER_NAME@$IP" "$@"; }
wait_ssh () { for _ in $(seq 1 40); do gssh true 2>/dev/null && return 0; sleep 8; done; return 1; }
ensure_vm () {
  IP="${IP:-10.138.0.3}"
  gssh -o ConnectTimeout=8 true 2>/dev/null && return 0
  local state; state=$(gcloud compute instances describe "$VM" --zone="$ZONE" --project="$PROJECT" --format='value(status)' 2>/dev/null || true)
  [ "$state" = RUNNING ] || { echo "  starting $VM ..." >&2; gcloud compute instances start "$VM" --zone="$ZONE" --project="$PROJECT" >/dev/null; }
  IP="$(vm_ip)"; [ -n "$IP" ] || { echo "!! no IP for $VM" >&2; return 1; }
  wait_ssh || { echo "!! cannot reach $USER_NAME@$IP" >&2; return 1; }
}
# vm_sync: push code directories to the VM, atomically per directory (hard-link seeded staging copy + rename),
# so a running job never sees a half-copied tree. build/ is not synced: the VM compiles its own private trees.
vm_sync () {
  local here; here="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
  gssh "mkdir -p ~/$REMOTE_REPO ~/projects/vibe/bc26b-benchmarks"
  local d
  for d in bots tools maps engine snapshots; do
    [ -e "$here/$d" ] || continue
    gssh "rm -rf ~/$REMOTE_REPO/.$d.sync; [ -d ~/$REMOTE_REPO/$d ] && cp -al ~/$REMOTE_REPO/$d ~/$REMOTE_REPO/.$d.sync; true"
    rsync -a --delete -e "ssh ${SSHO[*]}" --exclude '.venv' --exclude '__pycache__' --exclude 'galaxy26' \
      "$here/$d/" "$USER_NAME@$IP:$REMOTE_REPO/.$d.sync/" && \
    gssh "rm -rf ~/$REMOTE_REPO/.$d.old; [ -e ~/$REMOTE_REPO/$d ] && mv ~/$REMOTE_REPO/$d ~/$REMOTE_REPO/.$d.old; mv ~/$REMOTE_REPO/.$d.sync ~/$REMOTE_REPO/$d; rm -rf ~/$REMOTE_REPO/.$d.old"
  done
}
# games in flight on the VM (the bracket keeps grep from matching itself)
vm_games () { gssh 'ps -eo args | grep -c "[b]attlecode.server.Main" || true'; }
