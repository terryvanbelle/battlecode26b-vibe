#!/usr/bin/env bash
# Check the bc26 galaxy replica's guarantees on battlecode-dev; prints PASS/FAIL per check, exit 1 on any FAIL.
#   bash tools/galaxy26/verify.sh            (on the driver: runs itself on the VM over ssh; or on the VM as the operator)
# Checks: (1) egress: nftables tables loaded; as bc26replica nothing off-host, no metadata server, no other loopback
# port than siarnaq's, while siarnaq answers; the official Battlecode addresses are blocked for every user; the
# saturn unit has no network; (2) no google.* import: no google package in the venv, and every google module the
# running settings load comes from the stand-ins; (3) the installed frontend and viewer bundles fetch no outside host
# (frontend_check.py) and the site's CSP header is set; (4) services up: units active, gunicorn on 127.0.0.1:8026 only,
# no Postgres TCP listener, the site gate answers 401 without the password and 200 with it, Caddy's certificate is
# valid. The 2023 replica's units are only reported, never touched. Nothing here sends a packet to battlecode.org:
# the connection attempt to its address is rejected by this host's own nftables before it leaves.
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [ "$(hostname -s)" != battlecode-dev ]; then
  # shellcheck source=vm.sh
  source "$HERE/vm.sh"; ensure_vm
  gssh 'mkdir -p ~/bc26-galaxy-stage/tools/galaxy26'
  rsync -a -e "ssh ${SSHO[*]}" "$HERE/verify.sh" "$HERE/frontend_check.py" "$USER_NAME@$IP:bc26-galaxy-stage/tools/galaxy26/"
  exec ssh "${SSHO[@]}" "$USER_NAME@$IP" 'bash ~/bc26-galaxy-stage/tools/galaxy26/verify.sh'
fi
RUSER=bc26replica; PORT=8026; PG_PORT=5426
ETC=/etc/bc26-galaxy; WWW=/srv/bc26-galaxy; OPT=/opt/bc26-galaxy
SITE="https://galaxy26.$(cat $ETC/hostname)"; HOST=${SITE#https://}
fails=0
ok () { echo "PASS  $*"; }
bad () { echo "FAIL  $*"; fails=$((fails + 1)); }
check () { local what=$1; shift; if "$@" >/dev/null 2>&1; then ok "$what"; else bad "$what"; fi; }
refuse () { local what=$1; shift; if "$@" >/dev/null 2>&1; then bad "$what"; else ok "$what"; fi; }
as_r () { sudo -n -u "$RUSER" "$@"; }

echo "== 1. egress"
check "nft table inet bc26_replica_egress loaded" sudo -n nft list table inet bc26_replica_egress
check "nft table inet bc26_official_block loaded" sudo -n nft list table inet bc26_official_block
check "egress rule matches uid of $RUSER" sh -c "sudo -n nft list chain inet bc26_replica_egress output | grep -q 'skuid $(id -u $RUSER)'"
refuse "$RUSER cannot reach the internet (1.1.1.1:443)" as_r curl -s -m 5 -o /dev/null https://1.1.1.1/
refuse "$RUSER cannot reach the metadata server" as_r curl -s -m 5 -o /dev/null -H 'Metadata-Flavor: Google' http://169.254.169.254/
refuse "$RUSER cannot reach Caddy on loopback 443" as_r curl -s -m 5 -o /dev/null -k https://127.0.0.1/
refuse "$RUSER cannot resolve names (DNS stub)" as_r getent ahosts example.com
check "$RUSER reaches siarnaq on 127.0.0.1:$PORT" as_r curl -s -m 5 -o /dev/null http://127.0.0.1:$PORT/api/episode/e/
ips=$(sudo -n nft list set inet bc26_official_block official | tr -d ' \t\n' | sed -n 's/.*elements={\([^}]*\)}.*/\1/p')
first=${ips%%,*}
if [ -n "$first" ]; then
  refuse "official Battlecode address $first rejected for every user (operator tried)" curl -s -m 5 -o /dev/null --resolve "battlecode.org:443:$first" https://battlecode.org/
  echo "      official block set: $ips"
else bad "official block set is empty"; fi
check "saturn unit has PrivateNetwork=yes" sh -c "systemctl show -p PrivateNetwork --value bc26-galaxy-saturn | grep -qx yes"
check "services are in bc26-replica.slice (IPAddressAllow=localhost)" sh -c "systemctl show -p IPAddressDeny --value bc26-replica.slice | grep -q ."

echo "== 2. no Google Cloud code"
refuse "no google* package in $OPT/venv" sh -c "find $OPT/venv -path '*/site-packages/google*' | grep -q ."
mods=$(sudo -n bc26-galaxy-manage shell -c "
import sys, django.conf; django.conf.settings.INSTALLED_APPS
import siarnaq.gcloud, siarnaq.api.compete.managers, siarnaq.api.episodes.signals
g = sorted((m, getattr(sys.modules[m], '__file__', '') or '') for m in sys.modules if m == 'google' or m.startswith('google.'))
print('\n'.join(f'{m} {f}' for m, f in g))" 2>&1)
if [ -z "$mods" ]; then bad "settings import test produced no output"
elif echo "$mods" | grep -v "^google[a-z0-9_.]* $OPT/galaxy26/standins/" | grep -q .; then
  bad "a google module outside the stand-ins:"; echo "$mods" | grep -v "$OPT/galaxy26/standins/" | head -5
else ok "the $(echo "$mods" | wc -l) google modules siarnaq loads are all stand-ins ($OPT/galaxy26/standins)"; fi

echo "== 3. bundles"
for d in frontend viewer; do
  out=$(sudo -n python3 "$HERE/frontend_check.py" "$WWW/$d" 2>&1 | tail -1)
  if [ "$out" = "bundle check: PASS" ]; then ok "$WWW/$d: no automatic fetch of another host"; else bad "$WWW/$d: $out"; fi
done
P=$(cat ~/.bc26b-replica-password)
hdr=$(curl -s -m 10 -D - -o /dev/null -u "owner:$P" --resolve "$HOST:443:127.0.0.1" "$SITE/")
check "site sends the strict Content-Security-Policy" sh -c "echo \"\$0\" | grep -qi \"^content-security-policy: default-src 'self'; script-src 'self'\"" "$hdr"

echo "== 4. services"
for u in bc26-replica-egress bc26-galaxy-web bc26-galaxy-relay bc26-galaxy-saturn postgresql@15-bc26 bc26-replica-caddy; do
  check "$u active" systemctl is-active --quiet "$u"
done
for u in bc26-galaxy-scheduler.timer bc26-galaxy-activity.service; do
  echo "      $u: enabled=$(systemctl is-enabled $u 2>/dev/null) active=$(systemctl is-active $u 2>/dev/null)"
done
check "gunicorn listens on 127.0.0.1:$PORT only" sh -c "ss -ltnH | awk '{print \$4}' | grep -E ':$PORT\$' | grep -qvx '127.0.0.1:$PORT' && exit 1; ss -ltnH | awk '{print \$4}' | grep -qx '127.0.0.1:$PORT'"
refuse "postgres cluster bc26 has no TCP listener (port $PG_PORT)" sh -c "ss -ltnH | awk '{print \$4}' | grep -qE ':$PG_PORT\$'"
code=$(curl -s -m 10 -o /dev/null -w '%{http_code}' "$SITE/")
[ "$code" = 401 ] && ok "gate: $SITE/ without password -> 401" || bad "gate: $SITE/ without password -> $code"
code=$(curl -s -m 10 -o /dev/null -w '%{http_code}' -u "owner:$P" "$SITE/api/episode/e/bc26/")
[ "$code" = 200 ] && ok "gate: with the owner password -> 200 (episode bc26)" || bad "gate: with password -> $code"
code=$(curl -s -m 10 -o /dev/null -w '%{http_code}' "$SITE/api/episode/e/bc26/")
[ "$code" = 401 ] && ok "gate: API without password or token -> 401" || bad "gate: API without credentials -> $code"
check "TLS certificate valid for $HOST (system trust store)" curl -s -m 10 -o /dev/null "$SITE/manifest.json"
echo "      2023 replica (not touched): bc23-replica-caddy $(systemctl is-active bc23-replica-caddy 2>/dev/null), bc23-replica-egress $(systemctl is-active bc23-replica-egress 2>/dev/null)"
df -h / | awk 'NR==2 {print "      disk: " $4 " free of " $2}'
echo "verify: $([ $fails = 0 ] && echo PASS || echo "FAIL ($fails)")"
[ $fails = 0 ]
