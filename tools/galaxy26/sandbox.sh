#!/bin/sh
# Run a command with no network and a read-only view of every file system except one work directory.
#   sandbox.sh <workdir> <command> [args...]
# A new user namespace (the caller's uid maps to root inside, no privilege outside), network namespace (only a down
# `lo`) and mount namespace; every writable mount is remounted read-only, then <workdir> is bound back read-write.
# It fails closed: when a mount other than <workdir> and the kernel's /proc, /sys and /dev trees is still writable
# after the remounts, it prints the mount points and exits 3 without running the command.
# Used by tools/galaxy26/saturn.py for every compile and every game (CLAUDE.md rule 6).
set -eu
if [ "${1:-}" != --inside ]; then
  [ $# -ge 2 ] || { echo "usage: sandbox.sh <workdir> <command> [args...]" >&2; exit 2; }
  work=$(realpath "$1"); shift
  [ -d "$work" ] || { echo "sandbox.sh: no work directory $work" >&2; exit 2; }
  exec unshare --user --map-root-user --net --mount --propagation private /bin/sh "$0" --inside "$work" "$@"
fi
shift
w=$1; shift
exempt() { [ "$1" = "$w" ] && return 0; case "$1" in /proc|/proc/*|/sys|/sys/*|/dev|/dev/pts|/dev/mqueue) return 0 ;; esac; return 1; }
# The read-write mounts: mount id, mount point, per-mount options (mountinfo fields 1, 5, 6). A mount point may appear
# more than once: a mount propagated into systemd's namespace after the unit started can sit under another mount
# (e.g. a login's /run/user/<uid> beneath ProtectHome's /run/user), where no path reaches it.
writable() { awk '$6 ~ /(^|,)rw(,|$)/ {print $1 "\t" $5 "\t" $6}' /proc/self/mountinfo | sed 's/\\040/ /g'; }
# The id of the mount a path reaches (its top mount); "unreachable" when the path cannot even be searched (then
# nothing below it can be reached either); "?" (counted as reachable) when it is searchable but cannot be opened.
top_id() {
  [ -x "$1" ] || { echo unreachable; return; }
  ( exec 3<"$1" && awk '$1 == "mnt_id:" {print $2}' /proc/self/fdinfo/3 ) 2>/dev/null || echo '?'
}
mount --bind "$w" "$w"   # its own mount first: a bind made after the remounts would inherit read-only
tab=$(printf '\t')
writable | while IFS="$tab" read -r _id mp opts; do
  exempt "$mp" && continue
  [ "$mp" = "${SANDBOX_TEST_SKIP:-}" ] && continue   # unit test of the refusal: a mount left writable on purpose
  # Keep the mount's other flags: a user namespace may not clear the nosuid / nodev / noexec it inherited.
  ro=$(echo "$opts" | sed 's/\(^\|,\)rw\(,\|$\)/\1ro\2/')
  mount -o "remount,bind,$ro" "$mp" 2>/dev/null || mount -o remount,bind,ro "$mp" 2>/dev/null || true
done
# Fail closed on any read-write mount still reachable: one that is the top mount of its path.
left=$(writable | while IFS="$tab" read -r id mp _opts; do
  exempt "$mp" && continue
  t=$(top_id "$mp")
  { [ "$t" = "$id" ] || [ "$t" = '?' ]; } && echo "$mp"
done)
if [ -n "$left" ]; then
  echo "sandbox.sh: still writable after the remounts, refusing to run:" $left >&2
  exit 3
fi
cd "$w"
exec "$@"
