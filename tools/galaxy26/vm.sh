#!/usr/bin/env bash
# Helpers for the VM battlecode-dev (sourced):  source tools/galaxy26/vm.sh && ensure_vm && gssh 'cmd'
# Same VM and SSH settings as the earlier projects' tools/vm.sh (us-west1-b, reached on its internal IP).
VM=battlecode-dev; ZONE=us-west1-b; PROJECT=tvanbelle-vibecode
SSHO=(-i "$HOME/.ssh/google_compute_engine" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null
      -o ConnectTimeout=20 -o ServerAliveInterval=20 -o ServerAliveCountMax=3 -o LogLevel=ERROR)
USER_NAME="${BC_SSH_USER:-$(whoami)}"
IP="${IP:-10.138.0.3}"
gssh () { ssh "${SSHO[@]}" "$USER_NAME@$IP" "$@"; }
vm_ip () { gcloud compute instances describe "$VM" --zone="$ZONE" --project="$PROJECT" --format='value(networkInterfaces[0].networkIP)' 2>/dev/null; }
ensure_vm () {
  gssh -o ConnectTimeout=8 true 2>/dev/null && return 0
  local state; state=$(gcloud compute instances describe "$VM" --zone="$ZONE" --project="$PROJECT" --format='value(status)' 2>/dev/null || true)
  [ "$state" = RUNNING ] || { echo "  starting $VM ..." >&2; gcloud compute instances start "$VM" --zone="$ZONE" --project="$PROJECT" >/dev/null; }
  IP="$(vm_ip)"; [ -n "$IP" ] || { echo "!! no IP for $VM" >&2; return 1; }
  local i; for i in $(seq 1 40); do gssh true 2>/dev/null && return 0; sleep 8; done; return 1
}
