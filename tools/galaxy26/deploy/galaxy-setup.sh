#!/usr/bin/env bash
# Root-side setup of the bc26 galaxy replica on battlecode-dev (Debian 12). Standalone: nothing of the 2023 replica
# (bc23-* units, /opt/bc23-*, /srv/bc23-*, /etc/bc23-*, user bcreplica, nftables table bc23_replica_egress, its Caddy and
# Caddyfile) is read, changed, stopped or removed. Every step is idempotent. Run by tools/galaxy26/deploy/deploy.sh from
# the driver; doc: tools/galaxy26/README.md.
#
#   sudo galaxy-setup.sh all STAGE      every step below; STAGE = the pushed stage (~/bc26-galaxy-stage: tools/galaxy26,
#                                       engine/, maps/, galaxy.tar = git archive of the pinned galaxy commit)
#   sudo galaxy-setup.sh user | egress | postgres | src STAGE | venv STAGE | app STAGE | runtime STAGE | dirs | secrets
#   sudo galaxy-setup.sh password | env | units | cli | migrate | bootstrap | static | viewer | frontend | hostname
#   sudo galaxy-setup.sh milestones STAGE   STAGE/milestones.json -> the rating charts (no rebuild)
#   sudo galaxy-setup.sh caddy | site | start | stop | status | teardown [--purge-data] | scheduler on|off | activity on|off
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOC=/home/terryvanbelle/projects/vibe/2026b/tools/galaxy26/README.md
OPERATOR=terryvanbelle
GALAXY_COMMIT=f343088f4d471b0664b2ff41129f0435a8dca1c2   # github.com/battlecode/galaxy (MIT), pinned
ENGINE_JAR=battlecode26-java-1.2.6.jar
ENGINE_SHA256=ea722532f6d523241ff4f708078563de2a010a39d7d524c66ae1890ab85c04ec
JDK_SRC=/home/$OPERATOR/opt/jdk21         # Temurin 21 (the operator's; copied into the runtime)
RUSER=bc26replica                         # runs every bc26 service; loopback-only network (nftables + slice)
CUSER=bc26caddy                           # runs this replica's own Caddy
OPT=/opt/bc26-galaxy                      # root-owned: src/ (siarnaq), venv/, galaxy26/ (our code), runtime/ (JDK, engine, maps)
WWW=/srv/bc26-galaxy                      # root:bc26caddy: frontend/ viewer/ static/ storage/
HOMED=/home/$RUSER/galaxy                 # bc26replica: secrets/ spool/ storage-meta/ saturn/ run/ frontend-dist/
ETC=/etc/bc26-galaxy
ENVF=$ETC/galaxy.env                      # rewritten by every deploy
LOCALENV=$ETC/local.env                   # operator overrides (e.g. GALAXY_EXECUTE_SLOTS=6), never rewritten
LIB=/usr/local/lib/bc26-galaxy
CLI=/usr/local/bin/bc26-galaxy-manage
PORT=8026                                 # siarnaq (gunicorn) on 127.0.0.1 only
PG_CLUSTER=bc26; PG_PORT=5426; DB=siarnaq26
NFT_TABLE=bc26_replica_egress
NFT_OFFICIAL=bc26_official_block          # host-wide: no process on this VM may reach the official contest's hosts
OFFICIAL_HOSTS="battlecode.org www.battlecode.org api.battlecode.org play.battlecode.org releases.battlecode.org"
OFFICIAL_IPS_KNOWN="34.160.107.126 185.199.108.153 185.199.109.153 185.199.110.153 185.199.111.153"  # 2026-10-21
SLICE=bc26-replica.slice
U_EGRESS=bc26-replica-egress.service
U_WEB=bc26-galaxy-web.service; U_RELAY=bc26-galaxy-relay.service; U_SATURN=bc26-galaxy-saturn.service
U_ACT=bc26-galaxy-activity.service       # field.py activity (operator): field teams challenge near-rated teams
U_SCHED=bc26-galaxy-scheduler.service; T_SCHED=bc26-galaxy-scheduler.timer   # Cloud Scheduler stand-in (autoscrim)
U_CADDY=bc26-replica-caddy.service; U_HOST=bc26-replica-hostname.service
BC23_CADDY=bc23-replica-caddy.service    # only ever queried (is-active), never touched
CADDY_BIN=/usr/local/bin/caddy           # the static Caddy binary already on the VM (executed, never modified)
CADDYFILE=$ETC/Caddyfile
CADDY_DATA=/var/lib/bc26-caddy; CADDY_LOG=/var/log/bc26-caddy
PASSWORD_FILE=$ETC/owner-password         # root 600: basic-auth gate + siarnaq superuser "owner"
SITE_PREFIX=galaxy26.
CSP="default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data: blob:; font-src 'self' data:; connect-src 'self'; frame-src 'self'; worker-src 'self' blob:; manifest-src 'self'; object-src 'none'; base-uri 'self'; form-action 'self'; frame-ancestors 'self'"
VIEWER_DIST=/home/$OPERATOR/bc26-galaxy-build/client-dist     # tools/galaxy26/viewer/build.sh output
die () { echo "galaxy-setup: $*" >&2; exit 1; }
say () { echo "== $*"; }
[ "$(id -u)" = 0 ] || die "run as root (sudo)"
[ "$(hostname -s)" = battlecode-dev ] || die "this is $(hostname -s), not battlecode-dev"
as_r () { (cd / && runuser -u "$RUSER" -- "$@"); }
swap_in () {  # $1 new dir, $2 target: replace target by new with two renames
  rm -rf "$2.old"; [ -e "$2" ] && mv "$2" "$2.old"; mv "$1" "$2"; rm -rf "$2.old"
}

user_step () {
  say "users $RUSER (services) and $CUSER (Caddy): system, locked, nologin"
  id -u "$RUSER" >/dev/null 2>&1 || useradd --system --user-group --home-dir "/home/$RUSER" --create-home \
    --shell /usr/sbin/nologin --comment "bc26 galaxy replica services" "$RUSER"
  passwd -l "$RUSER" >/dev/null 2>&1 || true
  chmod 0750 "/home/$RUSER"
  id -u "$CUSER" >/dev/null 2>&1 || useradd --system --user-group --home-dir "$CADDY_DATA" --create-home \
    --shell /usr/sbin/nologin --comment "bc26 galaxy replica Caddy web front" "$CUSER"
  passwd -l "$CUSER" >/dev/null 2>&1 || true
  install -d -o "$CUSER" -g "$CUSER" -m 0700 "$CADDY_DATA"
  install -d -o "$CUSER" -g "$CUSER" -m 0750 "$CADDY_LOG"
}

egress_step () {
  local uid; uid=$(id -u "$RUSER")
  say "egress table inet $NFT_TABLE: uid $uid may use loopback port $PORT only; slice $SLICE"
  install -d -m 0755 "$ETC"
  cat > "$ETC/egress.nft.new" <<EOF
#!/usr/sbin/nft -f
# Egress lockdown for uid $uid ($RUSER), the bc26 galaxy replica. Generated by $LIB/galaxy-setup.sh; loaded at boot by
# $U_EGRESS. Touches only this table and $NFT_OFFICIAL. Doc: $DOC
table inet $NFT_TABLE
delete table inet $NFT_TABLE
table inet $NFT_TABLE {
	chain output {
		type filter hook output priority filter - 10; policy accept;
		meta skuid $uid jump replica comment "$RUSER"
	}
	chain replica {
		ip daddr 169.254.169.254 counter drop comment "GCE metadata server"
		ip6 daddr fd20:ce::254 counter drop comment "GCE metadata server (IPv6)"
		oifname != "lo" limit rate 6/minute burst 5 packets log prefix "bc26-replica-egress-drop: " level info
		oifname != "lo" counter drop comment "no off-host egress"
		ct state established,related accept comment "replies on loopback"
		tcp dport $PORT accept comment "bc26 siarnaq"
		counter drop comment "no other loopback service (DNS stub, MTA, Caddy admin, other replicas)"
	}
}
EOF
  # host-wide block of the official contest (every user): the addresses its host names resolve to now plus the ones
  # recorded on 2026-10-21 (battlecode.org is GitHub Pages: those 4 addresses also serve other *.github.io sites)
  local ips h
  ips="$OFFICIAL_IPS_KNOWN"
  for h in $OFFICIAL_HOSTS; do ips="$ips $(getent ahostsv4 "$h" 2>/dev/null | awk '{print $1}' | sort -u | tr '\n' ' ')"; done
  ips=$(echo $ips | tr ' ' '\n' | grep -E '^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$' | sort -u | paste -sd, -)
  cat >> "$ETC/egress.nft.new" <<EOF
# Every process on this VM: no packet to the official Battlecode hosts ($OFFICIAL_HOSTS).
table inet $NFT_OFFICIAL
delete table inet $NFT_OFFICIAL
table inet $NFT_OFFICIAL {
	set official {
		type ipv4_addr
		elements = { $ips }
	}
	chain output {
		type filter hook output priority filter - 20; policy accept;
		ip daddr @official limit rate 6/minute burst 5 packets log prefix "bc26-official-block: " level info
		ip daddr @official counter reject comment "official Battlecode hosts"
	}
}
EOF
  mv -f "$ETC/egress.nft.new" "$ETC/egress.nft"
  cat > "/etc/systemd/system/$U_EGRESS" <<EOF
[Unit]
Description=bc26 replica: egress lockdown for user $RUSER (loopback port $PORT only, metadata server dropped)
Documentation=file://$DOC
DefaultDependencies=no
Wants=network-pre.target
Before=network-pre.target shutdown.target
After=local-fs.target nftables.service
Conflicts=shutdown.target
PartOf=nftables.service

[Service]
Type=oneshot
RemainAfterExit=yes
ExecStart=/usr/sbin/nft -f $ETC/egress.nft
ExecReload=/usr/sbin/nft -f $ETC/egress.nft
ExecStop=-/usr/sbin/nft delete table inet $NFT_TABLE
ExecStop=-/usr/sbin/nft delete table inet $NFT_OFFICIAL

[Install]
WantedBy=sysinit.target
EOF
  cat > "/etc/systemd/system/$SLICE" <<EOF
[Unit]
Description=bc26 replica services (IP traffic limited to localhost; second layer behind $U_EGRESS)
Documentation=file://$DOC

[Slice]
IPAddressDeny=any
IPAddressAllow=localhost
EOF
  systemctl daemon-reload
  systemctl enable --quiet "$U_EGRESS"
  systemctl restart "$U_EGRESS"
  nft list table inet "$NFT_TABLE" >/dev/null
  nft list table inet "$NFT_OFFICIAL" >/dev/null
}

postgres_step () {
  say "postgres cluster 15/$PG_CLUSTER on unix socket port $PG_PORT (no TCP), role $RUSER, database $DB"
  if ! pg_lsclusters -h | awk '{print $1"/"$2}' | grep -qx "15/$PG_CLUSTER"; then
    pg_createcluster 15 "$PG_CLUSTER" --port "$PG_PORT" -- --auth-local=peer >/dev/null
  fi
  # no TCP listener at all; the stock pg_hba "host ... trust" lines are turned into reject as a second guard
  local hba=/etc/postgresql/15/$PG_CLUSTER/pg_hba.conf
  if grep -qE '^host.*[[:space:]]trust$' "$hba"; then
    sed -i -E 's/^(host.*[[:space:]])trust$/\1reject/' "$hba"; systemctl reload "postgresql@15-$PG_CLUSTER" 2>/dev/null || true
  fi
  local conf=/etc/postgresql/15/$PG_CLUSTER/conf.d/bc26-galaxy.conf
  printf '%s\n' "# bc26 galaxy replica: no TCP listener; small footprint" "listen_addresses = ''" \
    "shared_buffers = 128MB" "max_connections = 40" > "$conf.new"
  if ! cmp -s "$conf.new" "$conf"; then mv -f "$conf.new" "$conf"; systemctl restart "postgresql@15-$PG_CLUSTER"; else rm -f "$conf.new"; fi
  systemctl enable --quiet "postgresql@15-$PG_CLUSTER"
  systemctl is-active --quiet "postgresql@15-$PG_CLUSTER" || systemctl start "postgresql@15-$PG_CLUSTER"
  local psql=(runuser -u postgres -- psql -p "$PG_PORT" -tAq)
  [ "$("${psql[@]}" -c "SELECT 1 FROM pg_roles WHERE rolname='$RUSER'")" = 1 ] || "${psql[@]}" -c "CREATE ROLE $RUSER LOGIN"
  [ "$("${psql[@]}" -c "SELECT 1 FROM pg_database WHERE datname='$DB'")" = 1 ] || \
    "${psql[@]}" -c "CREATE DATABASE $DB OWNER $RUSER ENCODING 'UTF8' TEMPLATE template0"
  if ss -ltnH | awk '{print $4}' | grep -qE ":$PG_PORT\$"; then die "postgres listens on TCP $PG_PORT"; fi
}

src_step () {  # $1 stage: galaxy.tar = git archive of $GALAXY_COMMIT
  local tar="${1:?stage}/galaxy.tar" s=$OPT/src
  say "galaxy source $GALAXY_COMMIT -> $s"
  [ -s "$tar" ] || die "no $tar"
  local c; c=$(python3 -c 'import sys, tarfile; print(tarfile.open(sys.argv[1]).pax_headers.get("comment", ""))' "$tar")
  [ "$c" = "$GALAXY_COMMIT" ] || die "tarball is a git archive of '$c', not $GALAXY_COMMIT"
  install -d -o root -g root -m 0755 "$OPT"
  rm -rf "$s.new"; install -d -o root -g root -m 0755 "$s.new"
  tar -xf "$tar" -C "$s.new" --no-same-owner --no-same-permissions backend saturn LICENSE
  [ -f "$s.new/backend/manage.py" ] || die "tarball has no backend/manage.py"
  printf 'galaxy commit %s, git archive sha256 %s, installed %s\n' "$GALAXY_COMMIT" "$(sha256sum "$tar" | cut -c1-64)" \
    "$(date -u +%FT%TZ)" > "$s.new/REPLICA_SOURCE"
  chown -R root:root "$s.new"; chmod -R go-w,a+rX "$s.new"
  swap_in "$s.new" "$s"
}

venv_step () {  # $1 stage: tools/galaxy26/requirements.txt (hash-locked, PyPI)
  local reqs="${1:?stage}/tools/galaxy26/requirements.txt" v=$OPT/venv
  say "venv $v from requirements.txt (pip as $OPERATOR, --require-hashes, then root-owned)"
  [ -s "$reqs" ] || die "no $reqs"
  install -d -o root -g root -m 0755 "$OPT"
  rm -rf "$v.new"; install -d -o "$OPERATOR" -g "$OPERATOR" -m 0755 "$v.new"
  local tmpreq; tmpreq=$(mktemp /tmp/bc26-galaxy-req.XXXXXX); cp "$reqs" "$tmpreq"; chmod 0644 "$tmpreq"
  runuser -u "$OPERATOR" -- python3 -m venv "$v.new"
  runuser -u "$OPERATOR" -- "$v.new/bin/python" -m pip install -q --disable-pip-version-check --no-input \
    --require-hashes --no-deps --no-build-isolation -r "$tmpreq"
  runuser -u "$OPERATOR" -- "$v.new/bin/python" -m pip check
  rm -f "$tmpreq"
  if find "$v.new" -maxdepth 6 -path '*/site-packages/google*' | grep -q .; then die "a google package got into the venv"; fi
  chown -R root:root "$v.new"; chmod -R go-w "$v.new"
  # the venv was created at $v.new: fix the recorded paths (pyvenv.cfg is path-free; scripts' shebangs are not)
  grep -rlI --exclude-dir=site-packages "$v.new" "$v.new/bin" 2>/dev/null | xargs -r sed -i "s#$v.new#$v#g"
  swap_in "$v.new" "$v"
  "$v/bin/python" -m pip freeze --disable-pip-version-check | wc -l | xargs echo "packages:"
}

app_step () {  # $1 stage dir
  local stage="${1:?stage dir}"
  say "app: $stage/tools/galaxy26 -> $OPT/galaxy26 (root, read-only)"
  [ -f "$stage/tools/galaxy26/saturn.py" ] || die "no $stage/tools/galaxy26"
  rm -rf "$OPT/galaxy26.new"; cp -r "$stage/tools/galaxy26" "$OPT/galaxy26.new"
  find "$OPT/galaxy26.new" -name __pycache__ -prune -exec rm -rf {} +
  chown -R root:root "$OPT/galaxy26.new"; chmod -R go-w,a+rX "$OPT/galaxy26.new"
  swap_in "$OPT/galaxy26.new" "$OPT/galaxy26"
  install -d -m 0755 "$LIB"; install -m 0755 "$HERE/galaxy-setup.sh" "$LIB/galaxy-setup.sh"
}

runtime_step () {  # $1 stage dir: engine/$ENGINE_JAR, maps/*.map26, tools/galaxy26/java/Bc26Tool.java
  local stage="${1:?stage dir}" rt="$OPT/runtime"
  say "runtime: JDK 21, engine $ENGINE_JAR, $(ls "$stage/maps" | grep -c '\.map26$') maps (+ teams-reversed copies)"
  [ -f "$stage/engine/$ENGINE_JAR" ] || die "no $stage/engine/$ENGINE_JAR"
  echo "$ENGINE_SHA256  $stage/engine/$ENGINE_JAR" | sha256sum -c --quiet - || die "engine jar sha256 mismatch"
  local jdk; jdk=$(readlink -f "$JDK_SRC"); [ -x "$jdk/bin/javac" ] || die "no JDK at $JDK_SRC"
  rm -rf "$rt.new"; install -d -m 0755 "$rt.new" "$rt.new/engine" "$rt.new/maps" "$rt.new/tool"
  cp -a "$jdk" "$rt.new/jdk"
  install -m 0644 "$stage/engine/$ENGINE_JAR" "$rt.new/engine/"
  install -m 0644 "$stage"/maps/*.map26 "$rt.new/maps/"
  "$rt.new/jdk/bin/javac" -J-XX:-UsePerfData --release 21 -proc:none -nowarn -cp "$rt.new/engine/$ENGINE_JAR" \
    -d "$rt.new/tool" "$stage/tools/galaxy26/java/Bc26Tool.java"
  # the reversed maps are written by the engine's own GameMapIO, run unprivileged in a scratch directory
  local tmp; tmp=$(mktemp -d /tmp/bc26-maps.XXXXXX); chown "$RUSER" "$tmp"
  as_r "$rt.new/jdk/bin/java" -XX:-UsePerfData -cp "$rt.new/engine/$ENGINE_JAR:$rt.new/tool" Bc26Tool reverse-maps \
    "$rt.new/maps" "$tmp/rev"
  install -d -m 0755 "$rt.new/maps-reversed"; install -m 0644 "$tmp"/rev/*.map26 "$rt.new/maps-reversed/"; rm -rf "$tmp"
  [ "$(ls "$rt.new/maps" | wc -l)" = "$(ls "$rt.new/maps-reversed" | wc -l)" ] || die "reversed map count differs"
  chown -R root:root "$rt.new"; chmod -R go-w,a+rX "$rt.new"
  swap_in "$rt.new" "$rt"
}

dirs_step () {
  say "directories: $HOMED ($RUSER), $WWW (root:$CUSER)"
  install -d -o "$RUSER" -g "$RUSER" -m 2750 "$HOMED"
  local d
  for d in spool storage-meta saturn run logs; do install -d -o "$RUSER" -g "$RUSER" -m 2750 "$HOMED/$d"; done
  install -d -o "$RUSER" -g "$RUSER" -m 0700 "$HOMED/secrets"
  install -d -o root -g "$CUSER" -m 0751 "$WWW"
  install -d -o "$RUSER" -g "$CUSER" -m 2750 "$WWW/storage" "$WWW/static"
  for d in bc26-replica-public bc26-replica-secure bc26-replica-ephemeral; do install -d -o "$RUSER" -g "$CUSER" -m 2750 "$WWW/storage/$d"; done
}

secrets_step () {
  say "secrets in $HOMED/secrets (generated here, never printed)"
  as_r python3 - "$HOMED/secrets" <<'PY'
import os, secrets, sys
d = sys.argv[1]
os.umask(0o077)
for name, make in (('django-secret-key', lambda: secrets.token_urlsafe(48)),
                   ('admin-password', lambda: secrets.token_urlsafe(24)),
                   ('oidc-hmac.key', lambda: secrets.token_hex(32))):
    p = os.path.join(d, name)
    if os.path.exists(p):
        continue
    fd = os.open(p, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
    with os.fdopen(fd, 'w') as fh:
        fh.write(make() + '\n')
    print('created', name)
PY
}

password_step () {  # the owner password: basic-auth gate and siarnaq superuser "owner"
  install -d -m 0755 "$ETC"
  if [ ! -s "$PASSWORD_FILE" ]; then
    (umask 077; python3 -c 'import secrets; print(secrets.token_urlsafe(18))' > "$PASSWORD_FILE")
    say "generated $PASSWORD_FILE (root 600)"
  fi
  chown root:root "$PASSWORD_FILE"; chmod 0600 "$PASSWORD_FILE"
  # the operator's copy for the contestant tools (client.py reads ~/.bc26b-replica-password)
  install -o "$OPERATOR" -g "$OPERATOR" -m 0600 "$PASSWORD_FILE" "/home/$OPERATOR/.bc26b-replica-password"
  "$CADDY_BIN" hash-password --plaintext "$(cat "$PASSWORD_FILE")" > "$ETC/owner.bcrypt.new"
  chmod 0644 "$ETC/owner.bcrypt.new"; mv -f "$ETC/owner.bcrypt.new" "$ETC/owner.bcrypt"
}

env_step () {
  say "environment $ENVF (+ operator overrides in $LOCALENV)"
  cat > "$ENVF.new" <<EOF
# Environment of the bc26 galaxy units and $CLI. Generated by $LIB/galaxy-setup.sh env (rewritten on every deploy:
# change it in tools/galaxy26/deploy/galaxy-setup.sh, or override a value in $LOCALENV). No secrets. Doc: $DOC
GALAXY_HOME=$HOMED
GALAXY_STORAGE_ROOT=$WWW/storage
GALAXY_STATIC_ROOT=$WWW/static
GALAXY_HOSTNAME_FILE=$ETC/hostname
GALAXY_RUNTIME=$OPT/runtime
GALAXY_DB_NAME=$DB
GALAXY_DB_USER=$RUSER
GALAXY_DB_PORT=$PG_PORT
PYTHONPATH=$OPT/galaxy26/standins:$OPT/galaxy26:$OPT/src/backend
DJANGO_SETTINGS_MODULE=replica_settings
DJANGO_CONFIGURATION=Replica
SIARNAQ_REVISION=galaxy-f343088-bc26-replica
GALAXY_COMPILE_SLOTS=1
GALAXY_EXECUTE_SLOTS=4
GALAXY_GAME_TIMEOUT=3600
GALAXY_GAME_HEAP_MB=2048
GALAXY_SCHED_MAX_WAITING=30
GALAXY_SCHED_MIN_HOURS=0
GALAXY_NACK_DELAY=10
GALAXY_GUNICORN_WORKERS=3
PYTHONDONTWRITEBYTECODE=1
PYTHONUNBUFFERED=1
LANG=C.UTF-8
EOF
  chmod 0644 "$ENVF.new"; mv -f "$ENVF.new" "$ENVF"
  if [ ! -e "$LOCALENV" ]; then
    printf '%s\n' "# Operator overrides of $ENVF for the bc26 galaxy units (kept across deploys). Restart the unit after a change." \
      "# e.g. GALAXY_EXECUTE_SLOTS=6   (concurrent matches, 0..8; then: systemctl restart $U_SATURN)" > "$LOCALENV"
    chmod 0644 "$LOCALENV"
  fi
}

unit_common () {  # $1 extra InaccessiblePaths, $2 ReadWritePaths
  cat <<EOF
User=$RUSER
Group=$RUSER
Slice=$SLICE
UMask=0027
EnvironmentFile=$ENVF
EnvironmentFile=-$LOCALENV
InaccessiblePaths=-/run/systemd/resolve -/run/dbus/system_bus_socket -/home/bcreplica -/home/$OPERATOR -/srv/bc23-galaxy -/etc/bc23-galaxy -/etc/bc23-replica$1
NoNewPrivileges=yes
PrivateTmp=yes
PrivateDevices=yes
ProtectSystem=strict
ProtectHome=read-only
ReadWritePaths=$2
ProtectKernelTunables=yes
ProtectKernelModules=yes
ProtectControlGroups=yes
RestrictAddressFamilies=AF_UNIX AF_INET AF_INET6
LockPersonality=yes
EOF
}

units_step () {
  say "units: $U_WEB $U_RELAY $U_SATURN (+ $T_SCHED by 'scheduler on', $U_ACT by 'activity on': both installed disabled)"
  local head="Documentation=file://$DOC
Requires=$U_EGRESS
After=$U_EGRESS network.target"
  local nftcheck="ExecStartPre=+/bin/sh -c 'exec /usr/sbin/nft list table inet $NFT_TABLE >/dev/null'"
  cat > "/etc/systemd/system/$U_WEB" <<EOF
[Unit]
Description=bc26 galaxy replica: siarnaq (galaxy f343088, Django + gunicorn) on 127.0.0.1:$PORT
$head postgresql@15-$PG_CLUSTER.service $U_HOST
Wants=postgresql@15-$PG_CLUSTER.service

[Service]
Type=simple
$(unit_common "" "$HOMED $WWW/storage")
WorkingDirectory=$OPT/src/backend
$nftcheck
ExecStart=$OPT/venv/bin/python -m gunicorn siarnaq.wsgi:application --bind 127.0.0.1:$PORT --workers \${GALAXY_GUNICORN_WORKERS} --timeout 120 --access-logfile - --forwarded-allow-ips 127.0.0.1
MemoryMax=2G
Restart=on-failure
RestartSec=5s

[Install]
WantedBy=multi-user.target
EOF
  cat > "/etc/systemd/system/$U_RELAY" <<EOF
[Unit]
Description=bc26 galaxy replica: loopback relay (saturn reports, Cloud Tasks) to siarnaq on 127.0.0.1:$PORT
$head

[Service]
Type=simple
$(unit_common "" "$HOMED")
WorkingDirectory=$HOMED
$nftcheck
ExecStart=/usr/bin/python3 $OPT/galaxy26/relay.py serve
Restart=on-failure
RestartSec=5s

[Install]
WantedBy=multi-user.target
EOF
  cat > "/etc/systemd/system/$U_SATURN" <<EOF
[Unit]
Description=bc26 galaxy replica: saturn (compile submissions, run matches with the bc26 Java engine; no network)
$head $U_RELAY
Wants=$U_RELAY

[Service]
Type=simple
$(unit_common " $HOMED/secrets" "$HOMED $WWW/storage")
WorkingDirectory=$HOMED
# no network at all (a namespace holding only lo); each compile/game also runs in sandbox.sh (user + net + mount ns)
PrivateNetwork=yes
ExecStart=/usr/bin/python3 $OPT/galaxy26/saturn.py run
KillMode=mixed
TimeoutStopSec=60
# GALAXY_EXECUTE_SLOTS matches at once (default 4, at most 8), each one engine JVM with a GALAXY_GAME_HEAP_MB heap
# (2 GB) plus JVM overhead, and one javac/Verifier compile. The memory cap is a backstop for the whole service.
MemoryMax=22G
CPUQuota=800%
Restart=on-failure
RestartSec=30s

[Install]
WantedBy=multi-user.target
EOF
  cat > "/etc/systemd/system/$U_ACT" <<EOF
[Unit]
Description=bc26 galaxy replica: field activity (field.py activity: field teams challenge near-rated teams in spare capacity)
Documentation=file://$DOC
After=$U_WEB $U_CADDY

[Service]
Type=simple
# the field teams' accounts (~/.bc26-galaxy-field/accounts.json, mode 600, copied from the driver's
# ~/.bc26-galaxy/accounts.json) and the site gate (~/.bc26b-replica-password); it
# talks only to the site, through Caddy on 127.0.0.1:443, as contestants do (the public API)
User=$OPERATOR
Environment=GALAXY26_CONNECT=127.0.0.1 GALAXY26_CONF=/home/$OPERATOR/.bc26-galaxy-field
ExecStart=/usr/bin/python3 $OPT/galaxy26/field.py activity
IPAddressDeny=any
IPAddressAllow=localhost
NoNewPrivileges=yes
ProtectSystem=strict
ProtectHome=read-only
ReadWritePaths=-/home/$OPERATOR/.bc26-galaxy-field
PrivateTmp=yes
Restart=on-failure
RestartSec=60s

[Install]
WantedBy=multi-user.target
EOF
  cat > "/etc/systemd/system/$U_SCHED" <<EOF
[Unit]
Description=bc26 galaxy replica: Cloud Scheduler stand-in (fire recorded jobs due this minute: the autoscrim job)
$head

[Service]
Type=oneshot
$(unit_common "" "$HOMED")
WorkingDirectory=$HOMED
ExecStart=/usr/bin/python3 $OPT/galaxy26/relay.py fire-jobs
EOF
  cat > "/etc/systemd/system/$T_SCHED" <<EOF
[Unit]
Description=bc26 galaxy replica: Cloud Scheduler stand-in, every minute (autoscrim job: episode autoscrim_schedule)
Documentation=file://$DOC

[Timer]
OnCalendar=*-*-* *:*:00
AccuracySec=1s
Persistent=false
Unit=$U_SCHED

[Install]
WantedBy=timers.target
EOF
  systemctl daemon-reload
  systemctl enable --quiet "$U_WEB" "$U_RELAY" "$U_SATURN"
}

cli_step () {
  cat > "$CLI.new" <<EOF
#!/usr/bin/env bash
# siarnaq's manage.py and the bc26 replica tools as $RUSER with the units' environment. Doc: $DOC
#   bc26-galaxy-manage <manage.py command ...> | bootstrap <cmd> | relay status|fire-jobs | saturn status
set -euo pipefail
ENV_FILE=$ENVF; LOCAL_ENV=$LOCALENV; RUN_AS=$RUSER; NFT_TABLE=$NFT_TABLE; PY=$OPT/venv/bin/python; BACKEND=$OPT/src/backend; APP=$OPT/galaxy26
EOF
  cat >> "$CLI.new" <<'EOF'
sudo -n /usr/sbin/nft list table inet "$NFT_TABLE" >/dev/null 2>&1 || { echo "egress table $NFT_TABLE not loaded; refusing" >&2; exit 1; }
mapfile -t envs < <(cat "$ENV_FILE" "$LOCAL_ENV" 2>/dev/null | grep -E '^[A-Za-z_][A-Za-z0-9_]*=')
case "${1:-}" in
  bootstrap) shift; set -- "$APP/bootstrap.py" "$@" ;;
  relay) shift; PY=/usr/bin/python3; set -- "$APP/relay.py" "$@" ;;
  saturn) shift; PY=/usr/bin/python3; set -- "$APP/saturn.py" "$@" ;;
  '') echo "usage: bc26-galaxy-manage <manage.py command> | bootstrap ... | relay ... | saturn ..." >&2; exit 2 ;;
  *) set -- "$BACKEND/manage.py" "$@" ;;
esac
cd "$BACKEND"
exec sudo -u "$RUN_AS" env -i PATH=/usr/local/bin:/usr/bin:/bin HOME="/home/$RUN_AS" "${envs[@]}" "$PY" "$@"
EOF
  chmod 0755 "$CLI.new"; mv -f "$CLI.new" "$CLI"
  say "installed $CLI"
}

migrate_step () { say "migrate"; "$CLI" migrate --noinput | tail -3; }
bootstrap_step () { say "bootstrap: episode bc26 + maps, service user, superuser owner"; "$CLI" bootstrap all < "$PASSWORD_FILE"; }
static_step () { say "collectstatic"; "$CLI" collectstatic --noinput --clear -v 0; }

placeholder () {  # $1 dir, $2 title, $3 text
  install -d -m 0755 "$1"
  printf '<!doctype html><meta charset="utf-8"><title>%s</title><p>%s</p>\n' "$2" "$3" > "$1/index.html"
}

viewer_step () {
  say "viewer: $WWW/viewer (the Battlecode 2026 web client, built from the battlecode26 repository by viewer/build.sh)"
  local v="$WWW/viewer"
  rm -rf "$v.new"
  if [ -f "$VIEWER_DIST/index.html" ]; then
    cp -r "$VIEWER_DIST" "$v.new"
    python3 "$OPT/galaxy26/frontend_check.py" "$v.new" > "$ETC/viewer-check.txt" || { tail -5 "$ETC/viewer-check.txt"; rm -rf "$v.new"; die "viewer check failed ($ETC/viewer-check.txt)"; }
    tail -2 "$ETC/viewer-check.txt"
  elif [ -f "$v/index.html" ]; then
    say "viewer: no build at $VIEWER_DIST; keeping the installed one"; return 0
  else
    placeholder "$v.new" "Viewer" "The Battlecode 2026 client is not installed yet (tools/galaxy26/viewer/build.sh)."
  fi
  chown -R root:"$CUSER" "$v.new"; chmod -R u=rwX,g=rX,o= "$v.new"
  swap_in "$v.new" "$v"
}

milestones_step () {  # $1 stage: its milestones.json -> $ETC (kept across frontend builds) and the live frontend
  local src="${1:?stage}/milestones.json"
  [ -f "$src" ] || die "milestones: no $src (deploy.sh writes it from progress/milestones.csv)"
  python3 -I - "$src" <<'PY' || die "milestones: $src is malformed"
import json, sys
d = json.load(open(sys.argv[1]))
assert isinstance(d, list) and len(d) <= 200
for m in d:
    assert isinstance(m, dict) and set(m) == {'tag', 'label', 't'}
    assert isinstance(m['tag'], str) and isinstance(m['label'], str) and isinstance(m['t'], int)
    assert len(m['tag']) <= 8 and len(m['label']) <= 200 and not set('<>') & set(m['tag'] + m['label'])
PY
  install -d -m 0755 "$ETC"
  install -m 0644 "$src" "$ETC/milestones.json.new"; mv -f "$ETC/milestones.json.new" "$ETC/milestones.json"
  if [ -d "$WWW/frontend" ]; then
    install -o root -g "$CUSER" -m 0640 "$ETC/milestones.json" "$WWW/frontend/milestones.json.new"
    mv -f "$WWW/frontend/milestones.json.new" "$WWW/frontend/milestones.json"
  fi
  say "milestones: $(python3 -c 'import json,sys; print(len(json.load(open(sys.argv[1]))))' "$ETC/milestones.json") on the rating charts"
}

frontend_step () {
  say "frontend: $HOMED/frontend-dist (galaxy's frontend built by frontend/build.sh) -> $WWW/frontend, after the bundle audit"
  local src="$HOMED/frontend-dist" dst="$WWW/frontend"
  rm -rf "$dst.new"
  if [ -f "$src/index.html" ]; then
    cp -r "$src" "$dst.new"
    find "$dst.new" -name '*.map' -delete
    python3 "$OPT/galaxy26/frontend_check.py" "$dst.new" > "$ETC/frontend-check.txt" || { tail -5 "$ETC/frontend-check.txt"; rm -rf "$dst.new"; die "frontend check failed ($ETC/frontend-check.txt)"; }
    tail -3 "$ETC/frontend-check.txt"
    [ -f "$ETC/milestones.json" ] && cp "$ETC/milestones.json" "$dst.new/milestones.json"   # milestones_step's data
  elif [ -f "$dst/index.html" ]; then
    say "frontend: no build at $src; keeping the installed one"; return 0
  else
    placeholder "$dst.new" "Battlecode" "The frontend is not built yet (tools/galaxy26/frontend/build.sh)."
    printf '{"name":"Battlecode","short_name":"Battlecode"}\n' > "$dst.new/manifest.json"
  fi
  chown -R root:"$CUSER" "$dst.new"; chmod -R u=rwX,g=rX,o= "$dst.new"
  swap_in "$dst.new" "$dst"
}

hostname_step () {
  local ip; ip=$(curl -s -m 5 -H 'Metadata-Flavor: Google' \
    http://169.254.169.254/computeMetadata/v1/instance/network-interfaces/0/access-configs/0/external-ip || true)
  [[ $ip =~ ^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$ ]] || die "no external IP from the metadata server"
  install -d -m 0755 "$ETC"
  echo "${ip//./-}.sslip.io" > "$ETC/hostname.new"; chmod 0644 "$ETC/hostname.new"
  if ! cmp -s "$ETC/hostname.new" "$ETC/hostname"; then mv -f "$ETC/hostname.new" "$ETC/hostname"; say "host: $(cat "$ETC/hostname")"; return 10
  else rm -f "$ETC/hostname.new"; fi
}

print_caddyfile () {  # $1 host, $2 bcrypt hash
  cat <<EOF
# The bc26 galaxy replica's own Caddy. Generated by $LIB/galaxy-setup.sh site (host from $ETC/hostname, hash from
# $ETC/owner.bcrypt). Doc: $DOC
{
	admin unix//run/bc26-caddy/admin.sock
	servers {
		protocols h1 h2
	}
}

$1 {
	log {
		output file $CADDY_LOG/access.log {
			roll_size 10MiB
			roll_keep 3
		}
	}
	header {
		-Server
		Strict-Transport-Security "max-age=31536000"
		X-Content-Type-Options nosniff
		X-Frame-Options SAMEORIGIN
		Referrer-Policy same-origin
		Content-Security-Policy "$CSP"
	}
	encode zstd gzip
	@bearer_api {
		path /api/*
		not path /api/token/* /api/user/password_reset/*
		header_regexp Authorization ^Bearer\s+[A-Za-z0-9._~+/=-]+$
	}
	# siarnaq's 401 for an expired token goes out as a 403: Chrome counts a 401 to a request it had also given the
	# cached gate login (the JWT header replaced it) as that login rejected, and drops it (2023/2019 replicas).
	handle @bearer_api {
		reverse_proxy 127.0.0.1:$PORT {
			@siarnaq_401 status 401
			handle_response @siarnaq_401 {
				header -WWW-Authenticate
				copy_response 403
			}
		}
	}
	handle /manifest.json {
		root * $WWW/frontend
		file_server
	}
	handle {
		basic_auth {
			owner $2
		}
		@backend path /api/* /admin /admin/*
		handle @backend {
			reverse_proxy 127.0.0.1:$PORT {
				header_up -Authorization
				@siarnaq_401 status 401
				handle_response @siarnaq_401 {
					header -WWW-Authenticate
					copy_response 403
				}
			}
		}
		handle_path /static/* {
			root * $WWW/static
			file_server
		}
		handle_path /storage/* {
			root * $WWW/storage
			header Cache-Control "private, no-cache"
			file_server {
				hide .tmp-*
			}
		}
		handle_path /viewer/* {
			root * $WWW/viewer
			file_server
		}
		handle {
			root * $WWW/frontend
			try_files {path} /index.html
			file_server
		}
	}
}
EOF
}

as_caddy () { (cd "$CADDY_DATA" && runuser -u "$CUSER" -- env HOME="$CADDY_DATA" XDG_DATA_HOME="$CADDY_DATA/data" XDG_CONFIG_HOME="$CADDY_DATA/config" "$CADDY_BIN" "$@"); }

render_caddyfile () {  # writes $CADDYFILE from the hostname and the hash; returns 0 if it changed
  local host="$SITE_PREFIX$(cat "$ETC/hostname")"
  print_caddyfile "$host" "$(cat "$ETC/owner.bcrypt")" > "$CADDYFILE.new"
  chown root:"$CUSER" "$CADDYFILE.new"; chmod 0640 "$CADDYFILE.new"
  local out; out=$(as_caddy validate --config "$CADDYFILE.new" --adapter caddyfile 2>&1) || { rm -f "$CADDYFILE.new"; die "caddy validate failed: $out"; }
  if cmp -s "$CADDYFILE.new" "$CADDYFILE"; then rm -f "$CADDYFILE.new"; return 1; fi
  mv -f "$CADDYFILE.new" "$CADDYFILE"; return 0
}

caddy_step () {  # the units of this replica's own Caddy and its boot-time host name refresh
  say "caddy: $U_CADDY (user $CUSER, $CADDYFILE, data $CADDY_DATA) and $U_HOST"
  [ -x "$CADDY_BIN" ] || die "no $CADDY_BIN"
  cat > "/etc/systemd/system/$U_HOST" <<EOF
[Unit]
Description=bc26 replica: point the Caddy site at this boot's external IP (galaxy26.<ip-with-dashes>.sslip.io)
Documentation=file://$DOC
After=network-online.target
Wants=network-online.target
Before=$U_CADDY $U_WEB

[Service]
Type=oneshot
ExecStart=$LIB/galaxy-setup.sh boot-hostname
TimeoutStartSec=120

[Install]
WantedBy=multi-user.target
EOF
  cat > "/etc/systemd/system/$U_CADDY" <<EOF
[Unit]
Description=bc26 replica: Caddy HTTPS front (basic auth gate, CSP, Let's Encrypt) for siarnaq on 127.0.0.1:$PORT
Documentation=file://$DOC
# Ports 80/443 are also those of $BC23_CADDY (the paused 2023 replica). Both cannot run at once: this unit fails
# to bind while that one runs, and never stops it.
After=network-online.target $U_HOST $BC23_CADDY
Wants=network-online.target
ConditionPathExists=$CADDYFILE

[Service]
Type=notify
User=$CUSER
Group=$CUSER
Environment=HOME=$CADDY_DATA XDG_DATA_HOME=$CADDY_DATA/data XDG_CONFIG_HOME=$CADDY_DATA/config
ExecStart=$CADDY_BIN run --config $CADDYFILE --adapter caddyfile
ExecReload=$CADDY_BIN reload --config $CADDYFILE --adapter caddyfile --address unix//run/bc26-caddy/admin.sock --force
TimeoutStopSec=5s
LimitNOFILE=1048576
Restart=on-failure
RestartSec=10s
RuntimeDirectory=bc26-caddy
RuntimeDirectoryMode=0750
PrivateTmp=true
PrivateDevices=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=$CADDY_DATA $CADDY_LOG
NoNewPrivileges=true
AmbientCapabilities=CAP_NET_BIND_SERVICE
CapabilityBoundingSet=CAP_NET_BIND_SERVICE

[Install]
WantedBy=multi-user.target
EOF
  systemctl daemon-reload
  systemctl enable --quiet "$U_HOST" "$U_CADDY"
}

site_step () {
  [ -s "$ETC/hostname" ] || hostname_step || true
  local host="$SITE_PREFIX$(cat "$ETC/hostname")"
  say "Caddy site https://$host/"
  render_caddyfile || say "Caddyfile unchanged"
  if systemctl is-active --quiet "$U_CADDY"; then systemctl reload "$U_CADDY"
  elif systemctl is-active --quiet "$BC23_CADDY"; then
    say "WARNING: $BC23_CADDY is running and holds ports 80/443; $U_CADDY is not started (stop the 2023 one by hand only if the owner agrees)"
  else systemctl start "$U_CADDY"
  fi
}

boot_hostname () {  # from $U_HOST at boot: refresh the host name, re-render; restart the web unit if it changed
  local rc=0; hostname_step || rc=$?
  [ "$rc" = 0 ] || [ "$rc" = 10 ] || exit "$rc"
  [ -s "$ETC/owner.bcrypt" ] && render_caddyfile || true
  if [ "$rc" = 10 ]; then
    systemctl --no-block try-restart "$U_WEB" || true
    systemctl --no-block try-reload-or-restart "$U_CADDY" || true
  fi
}

start_step () {
  say "start $U_RELAY $U_WEB $U_SATURN"
  systemctl restart "$U_RELAY" "$U_WEB"
  systemctl restart "$U_SATURN"
  sleep 3
  local u; for u in "$U_RELAY" "$U_WEB" "$U_SATURN"; do systemctl is-active --quiet "$u" || die "$u did not start (journalctl -u $u)"; done
}

stop_step () { systemctl stop "$U_ACT" "$T_SCHED" "$U_SATURN" "$U_WEB" "$U_RELAY" 2>/dev/null || true; }

status_step () {
  local u
  for u in "$U_EGRESS" "$U_WEB" "$U_RELAY" "$U_SATURN" "$T_SCHED" "$U_ACT" "postgresql@15-$PG_CLUSTER" "$U_CADDY" "$U_HOST" "$BC23_CADDY"; do
    printf '%-34s enabled=%-9s active=%s\n' "$u" "$(systemctl is-enabled "$u" 2>/dev/null || true)" "$(systemctl is-active "$u" 2>/dev/null || true)"
  done
  [ -s "$ETC/hostname" ] && echo "url: https://$SITE_PREFIX$(cat "$ETC/hostname")/"
  ss -ltnH "sport = :$PORT" | awk '{print "listen", $4}'
  [ -x "$CLI" ] && "$CLI" saturn status || true
  du -sh "$OPT" "$WWW" "$HOMED" 2>/dev/null || true
  df -h / | tail -1
}

teardown_step () {
  say "teardown: bc26 units, CLI, Caddy (2023 untouched)"
  systemctl disable --now "$U_ACT" "$T_SCHED" "$U_SATURN" "$U_WEB" "$U_RELAY" "$U_CADDY" "$U_HOST" 2>/dev/null || true
  rm -f "/etc/systemd/system/"{$U_WEB,$U_RELAY,$U_SATURN,$U_ACT,$U_SCHED,$T_SCHED,$U_CADDY,$U_HOST} "$CLI"
  systemctl daemon-reload
  if [ "${1:-}" = --purge-data ]; then
    systemctl disable --now "$U_EGRESS" 2>/dev/null || true; rm -rf "/etc/systemd/system/$U_EGRESS" "/etc/systemd/system/$SLICE"
    pg_dropcluster --stop 15 "$PG_CLUSTER" || true
    rm -rf "$HOMED" "$WWW" "$OPT" "$ETC" "$LIB" "$CADDY_DATA" "$CADDY_LOG"; userdel -r "$RUSER" 2>/dev/null || true; userdel "$CUSER" 2>/dev/null || true
    systemctl daemon-reload
  fi
}

step="${1:-}"; shift || true
case "$step" in
  all)
    stage="${1:?stage dir}"
    user_step; egress_step; postgres_step; src_step "$stage"; [ -x "$OPT/venv/bin/python" ] || venv_step "$stage"
    app_step "$stage"; runtime_step "$stage"; dirs_step; secrets_step
    { hostname_step || true; }   # before siarnaq first runs: its ALLOWED_HOSTS and recorded job URLs use the name
    password_step; env_step; units_step; cli_step; migrate_step; bootstrap_step; static_step; viewer_step
    [ -f "$stage/milestones.json" ] && milestones_step "$stage"; frontend_step
    caddy_step; site_step; start_step; status_step ;;
  user) user_step ;; egress) egress_step ;; postgres) postgres_step ;;
  src) src_step "${1:?stage}" ;; venv) venv_step "${1:?stage}" ;;
  app) app_step "${1:?stage}" ;; runtime) runtime_step "${1:?stage}" ;; dirs) dirs_step ;; secrets) secrets_step ;;
  password) password_step ;; env) env_step ;; units) units_step ;; cli) cli_step ;; migrate) migrate_step ;;
  bootstrap) bootstrap_step ;; static) static_step ;; viewer) viewer_step ;; frontend) frontend_step ;;
  milestones) milestones_step "${1:?stage}" ;;
  hostname) hostname_step || true ;; caddy) caddy_step ;; site) site_step ;; boot-hostname) boot_hostname ;;
  start) start_step ;; stop) stop_step ;; status) status_step ;;
  activity) case "${1:-}" in on) systemctl enable --now "$U_ACT" ;; off) systemctl disable --now "$U_ACT" ;; *) die "activity on|off" ;; esac ;;
  scheduler) case "${1:-}" in on) systemctl enable --now "$T_SCHED" ;; off) systemctl disable --now "$T_SCHED" ;; *) die "scheduler on|off" ;; esac ;;
  teardown) teardown_step "${1:-}" ;;
  *) sed -n '2,11p' "$0" >&2; exit 2 ;;
esac
