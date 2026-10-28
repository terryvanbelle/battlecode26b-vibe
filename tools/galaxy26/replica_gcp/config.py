"""Names and paths shared by the replica settings (replica_settings.py), the stand-ins, saturn.py and relay.py.

Every value can be overridden through the environment (the systemd units read /etc/bc26-galaxy/galaxy.env); the
functions read the environment at call time so tests can point them at a scratch directory.
"""
import os

# --- names (the Replica configuration class copies these into siarnaq's GCLOUD_* settings)
PROJECT = 'bc26-replica'
LOCATION = 'replica-local'
BUCKET_PUBLIC = 'bc26-replica-public'
BUCKET_SECURE = 'bc26-replica-secure'
BUCKET_EPHEMERAL = 'bc26-replica-ephemeral'
BUCKETS = (BUCKET_PUBLIC, BUCKET_SECURE, BUCKET_EPHEMERAL)
TOPIC_COMPILE = 'replica-siarnaq-compile'
TOPIC_EXECUTE = 'replica-siarnaq-execute'
ORDER_COMPILE = 'compile-order'
ORDER_EXECUTE = 'execute-order'
SCHEDULER_PREFIX = 'replica'
QUEUE_BRACKET = 'replica-siarnaq-bracket'
QUEUE_RATING = 'replica-siarnaq-rating'
# Service identities. ".invalid" is reserved (RFC 2606): these addresses can never be delivered to or resolved.
SERVICE_EMAIL = 'siarnaq-agent@bc26-replica.invalid'        # Cloud Tasks / Cloud Scheduler OIDC identity
SATURN_COMPILE_EMAIL = 'saturn-compile@bc26-replica.invalid'
SATURN_EXECUTE_EMAIL = 'saturn-execute@bc26-replica.invalid'
ADMIN_EMAILS = [SERVICE_EMAIL, SATURN_COMPILE_EMAIL, SATURN_EXECUTE_EMAIL]
ADMIN_USERNAME = 'galaxy-admin'
SATURN_USER_AGENT = 'Galaxy-Saturn'          # saturn/cmd/saturn/main.go -useragent default
TASKS_USER_AGENT = 'Google-Cloud-Tasks'
SCHEDULER_USER_AGENT = 'Google-Cloud-Scheduler'
SITE_PREFIX = 'galaxy26.'                    # the bc26 site is galaxy26.<ip-with-dashes>.sslip.io
SIARNAQ_HOST = '127.0.0.1'
SIARNAQ_PORT = 8026


def _env(name, default):
    return os.environ.get(name) or default


def galaxy_home(*parts):
    """Private data of the replica (spools, metadata, secrets, work dirs): $GALAXY_HOME."""
    return os.path.join(_env('GALAXY_HOME', '/home/bc26replica/galaxy'), *parts)


def storage_root(*parts):
    """The buckets, one directory each, served read-only by Caddy under /storage/: $GALAXY_STORAGE_ROOT."""
    return os.path.join(_env('GALAXY_STORAGE_ROOT', '/srv/bc26-galaxy/storage'), *parts)


def secrets_dir(*parts):
    return galaxy_home('secrets', *parts)


def spool_dir(*parts):
    return galaxy_home('spool', *parts)


def relay_socket():
    return _env('GALAXY_RELAY_SOCKET', galaxy_home('run', 'relay.sock'))


def siarnaq_addr():
    return _env('GALAXY_SIARNAQ_HOST', SIARNAQ_HOST), int(_env('GALAXY_SIARNAQ_PORT', str(SIARNAQ_PORT)))


def base_host():
    """The replica's public host name (<ip-with-dashes>.sslip.io), kept by tools/galaxy26/deploy/galaxy-setup.sh hostname."""
    if os.environ.get('GALAXY_BASE_HOST'):
        return os.environ['GALAXY_BASE_HOST']
    path = _env('GALAXY_HOSTNAME_FILE', '/etc/bc26-galaxy/hostname')
    try:
        with open(path) as fh:
            host = fh.readline().strip()
    except OSError:
        host = ''
    return host or 'localhost'


def galaxy_host():
    """galaxy.<ip-with-dashes>.sslip.io: siarnaq's ALLOWED_HOSTS[0] and the Host header of loopback requests."""
    return os.environ.get('GALAXY_HOST') or SITE_PREFIX + base_host()
