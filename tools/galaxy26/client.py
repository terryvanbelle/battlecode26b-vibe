#!/usr/bin/env python3
"""Contestant client for the bc26 galaxy replica (Battlecode 2026): what a team does on the contest website, through the same HTTP API
the website uses (siarnaq). Standard library only. Talks ONLY to our replica site (never the official domain).

    python3 tools/galaxy26/client.py register <team> [--user U]       sign up a user, create the team, auto-accept on
    python3 tools/galaxy26/client.py submit <team> <package dir|src dir|zip> [--package P] [-m TEXT] [--wait]
    python3 tools/galaxy26/client.py submissions <team>               compile status of the team's submissions
    python3 tools/galaxy26/client.py teams                            the ladder (rating order)
    python3 tools/galaxy26/client.py request <team> <opponent> [--ranked] [--maps DefaultSmall,...] [--order +|-|?]
    python3 tools/galaxy26/client.py challenge <team> [--band 3]      one request to a team rated just above (ranked)
                                                                      or just below (unranked) us, alternating
    python3 tools/galaxy26/client.py matches [--team T] [--pages N]   scrimmages, newest first
    python3 tools/galaxy26/client.py wait <match id> [--timeout S]    until the match is finished
    python3 tools/galaxy26/client.py fetch <match id ...> [--out DIR]  the match's replay -> DIR/<id>.bc26 (all of its
                                                                      games in one file, as saturn uploads it)

Site: $GALAXY26_SITE (default: ~/.bc26-galaxy/site, else https://galaxy26.136-86-167-127.sslip.io). $GALAXY26_CONNECT
sends the TCP connection to another address (e.g. 127.0.0.1 on the VM) keeping the site name for TLS and Host.
The site is behind a basic-auth gate (user owner, password in ~/.bc26b-replica-password, mode 600): logged-out
requests carry it, API requests carry the team's JWT. Team accounts: ~/.bc26-galaxy/accounts.json (mode 600,
{team: {user, password, team_id, ...}}), written by `register` and tools/galaxy26/field.py; never printed.

Galaxy's rules are the server's: hourly request limits (20 ranked, 40 unranked on this replica), ranked requests only
to teams rated at least as high, ranked matches use 3 random maps; up to 10 maps per unranked request.
"""
import argparse
import base64
import hashlib
import http.client
import io
import json
import os
import random
import re
import secrets
import socket
import ssl
import sys
import time
import urllib.parse
import uuid
import zipfile

CONF_DIR = os.path.expanduser(os.environ.get('GALAXY26_CONF', '~/.bc26-galaxy'))
ACCOUNTS = os.path.join(CONF_DIR, 'accounts.json')
GATE_USER = 'owner'
GATE_FILE = os.path.expanduser(os.environ.get('GALAXY26_GATE_FILE', '~/.bc26b-replica-password'))
EPISODE = os.environ.get('GALAXY26_EPISODE', 'bc26')
OFFICIAL = 'battlecode' + '.org'          # the official contest's domain: refused
DEFAULT_SITE = 'https://galaxy26.136-86-167-127.sslip.io'
EMAIL_DOMAIN = 'team.bc26-replica.invalid'
TERMINAL = ('OK!', 'ERR', 'CAN')
WAITING = ('NEW', 'QUE', 'TRY')


def default_site():
    if os.environ.get('GALAXY26_SITE'):
        return os.environ['GALAXY26_SITE']
    try:
        with open(os.path.join(CONF_DIR, 'site')) as fh:
            s = fh.readline().strip()
            if s:
                return s
    except OSError:
        pass
    return DEFAULT_SITE


class ApiError(Exception):
    def __init__(self, code, detail):
        super().__init__(f'HTTP {code}: {detail[:300]}')
        self.code, self.detail = code, detail


def check_site(site):
    p = urllib.parse.urlparse(site)
    host = (p.hostname or '').lower()
    if p.scheme != 'https' or not host:
        raise SystemExit(f'refusing site {site!r}: an https URL of our replica is required')
    if host == OFFICIAL or host.endswith('.' + OFFICIAL):
        raise SystemExit(f'refusing site {site!r}: this client talks only to our replica')
    return p


def multipart(fields, files):
    b = uuid.uuid4().hex
    out = io.BytesIO()
    for k, v in fields.items():
        out.write(f'--{b}\r\nContent-Disposition: form-data; name="{k}"\r\n\r\n{v}\r\n'.encode())
    for k, (fname, data, ct) in files.items():
        out.write(f'--{b}\r\nContent-Disposition: form-data; name="{k}"; filename="{fname}"\r\n'
                  f'Content-Type: {ct}\r\n\r\n'.encode())
        out.write(data)
        out.write(b'\r\n')
    out.write(f'--{b}--\r\n'.encode())
    return out.getvalue(), f'multipart/form-data; boundary={b}'


class _Conn(http.client.HTTPSConnection):
    def __init__(self, host, connect_ip=None, **kw):
        super().__init__(host, **kw)
        self.connect_ip = connect_ip

    def connect(self):
        if not self.connect_ip:
            return super().connect()
        sock = socket.create_connection((self.connect_ip, self.port), self.timeout)
        self.sock = self._context.wrap_socket(sock, server_hostname=self.host)


class Client:
    def __init__(self, site=None, connect=None, gate=None, episode=EPISODE):
        self.site = (site or default_site()).rstrip('/')
        p = check_site(self.site)
        self.host, self.port = p.hostname, p.port or 443
        self.connect = connect if connect is not None else (os.environ.get('GALAXY26_CONNECT') or None)
        self.episode, self._gate = episode, gate
        self.ctx = ssl.create_default_context()

    def gate(self):
        if self._gate is None:
            with open(GATE_FILE) as fh:
                self._gate = 'Basic ' + base64.b64encode(f'{GATE_USER}:{fh.readline().strip()}'.encode()).decode()
        return self._gate

    def path_of(self, url):
        if url.startswith('/'):
            return url
        p = urllib.parse.urlparse(url)
        if (p.scheme, (p.hostname or '').lower(), p.port or 443) != ('https', self.host.lower(), self.port):
            raise SystemExit(f'refusing a request outside the replica site: {url}')
        return p.path + (f'?{p.query}' if p.query else '')

    def request(self, method, url, body=None, token=None, ctype=None, raw=False, timeout=120, retries=3):
        path = self.path_of(url)
        headers = {'Authorization': f'Bearer {token}' if token else self.gate(), 'Accept': 'application/json',
                   'User-Agent': 'bc26-replica-client'}
        data = None
        if body is not None:
            if ctype:
                data, headers['Content-Type'] = body, ctype
            else:
                data, headers['Content-Type'] = json.dumps(body).encode(), 'application/json'
        for attempt in range(retries + 1):
            conn = _Conn(self.host, connect_ip=self.connect, port=self.port, timeout=timeout, context=self.ctx)
            try:
                conn.request(method, path, body=data, headers=headers)
                r = conn.getresponse()
                status, out = r.status, r.read()
            except (OSError, http.client.HTTPException) as e:
                if method != 'GET' or attempt == retries:
                    raise ApiError(0, f'{type(e).__name__}: {e}') from None
                time.sleep(2 + 3 * attempt)
                continue
            finally:
                conn.close()
            if status in (502, 503, 504) and method == 'GET' and attempt < retries:
                time.sleep(2 + 3 * attempt)
                continue
            if status >= 400:
                raise ApiError(status, out.decode(errors='replace')[:2000])
            if raw:
                return out
            return json.loads(out) if out else None
        raise ApiError(0, 'unreachable')

    def pages(self, path, token=None, limit=None):
        out, url, n = [], path, 0
        while url and (limit is None or n < limit):
            r = self.request('GET', url, token=token)
            if isinstance(r, list):
                return r
            out += r.get('results', [])
            url = r.get('next')
            n += 1
        return out

    def login(self, username, password):
        return self.request('POST', '/api/token/', {'username': username, 'password': password})['access']


class Session:
    """One team user logged in: api() carries its JWT and logs in again once if the token is refused."""

    def __init__(self, client, username, password):
        self.c, self.username, self._pw = client, username, password
        self.token = None

    def api(self, method, path, body=None, **kw):
        if self.token is None:
            self.token = self.c.login(self.username, self._pw)
        try:
            return self.c.request(method, path, body, token=self.token, **kw)
        except ApiError as e:
            if e.code not in (401, 403) or 'token' not in e.detail.lower():
                raise
            self.token = self.c.login(self.username, self._pw)
            return self.c.request(method, path, body, token=self.token, **kw)

    def pages(self, path, limit=None):
        if self.token is None:
            self.token = self.c.login(self.username, self._pw)
        return self.c.pages(path, token=self.token, limit=limit)


# ---------------------------------------------------------------- accounts (secrets: never printed)
def load_accounts(path=None):
    path = path or ACCOUNTS
    if not os.path.exists(path):
        return {}
    if os.stat(path).st_mode & 0o077:
        raise SystemExit(f'{path} must be mode 600 (it holds team passwords)')
    with open(path) as fh:
        return json.load(fh)


def save_accounts(acc, path=None):
    path = path or ACCOUNTS
    d = os.path.dirname(path)
    os.makedirs(d, mode=0o700, exist_ok=True)
    os.chmod(d, 0o700)
    fd = os.open(path + '.tmp', os.O_WRONLY | os.O_CREAT | os.O_TRUNC, 0o600)
    with os.fdopen(fd, 'w') as fh:
        json.dump(acc, fh, indent=1, sort_keys=True)
    os.replace(path + '.tmp', path)


def user_name(team):
    """A siarnaq user name for a team (letters, digits and @.+-_ only, at most 30 characters)."""
    base = ''.join(ch if ch.isalnum() or ch in '.+-_' else '_' for ch in team)
    if len(base) > 30:
        base = base[:23] + '_' + hashlib.sha256(team.encode()).hexdigest()[:6]
    return base


def team_name(name):
    """A team name for an entrant: printable ASCII, at most 32 characters (a hash suffix keeps long ones unique)."""
    n = ''.join(ch if 32 < ord(ch) < 127 else '_' for ch in name)
    if len(n) > 32:
        n = n[:25] + '~' + hashlib.sha256(name.encode()).hexdigest()[:6]
    return n


def ensure_team(c, acc, team, about='', save=None, ep=None):
    """Sign-up flow, idempotent: user, login, team, auto-accept ranked + unranked. Returns a Session."""
    ep = ep or c.episode
    save = save or (lambda: save_accounts(acc))
    a = acc.setdefault(team, {})
    a.setdefault('user', user_name(team))
    if 'password' not in a:
        a['password'] = secrets.token_urlsafe(24)
        save()
    if not a.get('user_id'):
        try:
            r = c.request('POST', '/api/user/u/', {
                'username': a['user'], 'password': a['password'], 'email': f'{a["user"].lower()}@{EMAIL_DOMAIN}',
                'first_name': 'Team', 'last_name': team[:30],
                'profile': {'gender': '?', 'country': 'US', 'school': '', 'biography': about[:500]}})
            a['user_id'] = r['id']
        except ApiError as e:
            if e.code != 400 or 'exists' not in e.detail:
                raise
        save()
    s = Session(c, a['user'], a['password'])
    if not a.get('user_id'):
        a['user_id'] = s.api('GET', '/api/user/u/me/')['id']
    try:
        t = s.api('GET', f'/api/team/{ep}/t/me/')
    except ApiError as e:
        if e.code != 404:
            raise
        t = s.api('POST', f'/api/team/{ep}/t/', {'name': team, 'episode': ep})
    a['team_id'] = t['id']
    prof = t.get('profile') or {}
    if prof.get('auto_accept_reject_ranked') != 'A' or prof.get('auto_accept_reject_unranked') != 'A':
        s.api('PATCH', f'/api/team/{ep}/t/me/', {'profile': {
            'auto_accept_reject_ranked': 'A', 'auto_accept_reject_unranked': 'A', 'quote': '',
            'biography': about[:500]}})
    save()
    return s


def session(c, acc, team):
    a = acc.get(team)
    if not a or 'password' not in a:
        raise SystemExit(f'no account for team {team!r} in {ACCOUNTS} (register it first)')
    return Session(c, a['user'], a['password'])


# ---------------------------------------------------------------- submissions
PACKAGE_RE = re.compile(r'^[A-Za-z_][A-Za-z0-9_]*$')


def bot_zip(path, package=None):
    """The submission archive, as the scaffold's `gradlew zipForSubmit` makes it: the .java files of src/, paths
    relative to src/ (<package>/RobotPlayer.java ...). path: a package directory (src/<package>), a src/ directory
    (package required unless it holds exactly one package), or a ready .zip (package required).
    Returns (zip bytes, package, sha256 prefix of the contents)."""
    h = hashlib.sha256()
    if os.path.isfile(path):
        if not path.endswith('.zip') or not package:
            raise SystemExit('a ready-made submission must be a .zip, with --package')
        data = open(path, 'rb').read()
        h.update(data)
        return data, package, h.hexdigest()[:16]
    path = os.path.abspath(path.rstrip('/'))
    if any(f.endswith('.java') for f in os.listdir(path)):     # a package directory
        root, package = os.path.dirname(path), package or os.path.basename(path)
        tops = [os.path.basename(path)]
    else:                                                       # a src/ directory
        root = path
        tops = sorted(d for d in os.listdir(path) if os.path.isdir(os.path.join(path, d)) and not d.startswith('.'))
        if not package:
            if len(tops) != 1:
                raise SystemExit(f'{path} holds packages {tops}: name the one to play with --package')
            package = tops[0]
    if not PACKAGE_RE.match(package.split('.')[0]) or not os.path.isdir(os.path.join(root, *package.split('.'))):
        raise SystemExit(f'no package directory {package!r} under {root}')
    buf, n = io.BytesIO(), 0
    with zipfile.ZipFile(buf, 'w', zipfile.ZIP_DEFLATED) as z:
        for top in tops:
            for base, dirs, files in os.walk(os.path.join(root, top)):
                dirs[:] = sorted(d for d in dirs if not d.startswith('.'))
                for f in sorted(files):
                    if f.endswith('.java') and not f.startswith('.'):
                        full = os.path.join(base, f)
                        rel = os.path.relpath(full, root).replace(os.sep, '/')
                        data = open(full, 'rb').read()
                        h.update(rel.encode() + b'\0' + data)
                        z.writestr(rel, data)
                        n += 1
    if not n:
        raise SystemExit(f'no .java files under {path}')
    return buf.getvalue(), package, h.hexdigest()[:16]


def submit(s, path, description='', ep=EPISODE, package=None):
    data, package, digest = bot_zip(path, package)
    body, ct = multipart({'package': package, 'description': (description or os.path.basename(path.rstrip('/')))[:128]},
                         {'source_code': ('submission.zip', data, 'application/zip')})
    sub = s.api('POST', f'/api/compete/{ep}/submission/', body, ctype=ct)
    sub['_digest'] = digest
    return sub


def wait_submission(s, sid, ep=EPISODE, timeout=900):
    t0 = time.time()
    while time.time() - t0 < timeout:
        x = s.api('GET', f'/api/compete/{ep}/submission/{sid}/')
        if x['status'] in TERMINAL:
            return x
        time.sleep(5)
    raise SystemExit(f'submission {sid} not compiled after {timeout}s')


# ---------------------------------------------------------------- ladder and scrimmages
def rankings(c, ep=None):
    ep = ep or c.episode
    out = []
    for t in c.pages(f'/api/team/{ep}/t/?ordering=-rating%2Cname'):
        prof = t.get('profile') or {}
        out.append({'id': t['id'], 'name': t['name'], 'rating': prof.get('rating'), 'status': t.get('status'),
                    'active': str(t.get('has_active_submission')).lower() in ('true', '1')})
    return out


def neighbours(ladder, my_id, band):
    """(the `band` active teams rated closest at or above us, the `band` closest below), nearest first."""
    me = next((t for t in ladder if t['id'] == my_id), None)
    if me is None or me['rating'] is None:
        return [], []
    others = [t for t in ladder if t['id'] != my_id and t['active'] and t['status'] == 'R' and t['rating'] is not None]
    above = sorted((t for t in others if t['rating'] >= me['rating']), key=lambda t: (t['rating'] - me['rating'], t['id']))
    below = sorted((t for t in others if t['rating'] < me['rating']), key=lambda t: (me['rating'] - t['rating'], t['id']))
    return above[:band], below[:band]


def request(s, opponent_id, ranked=False, maps=(), order='?', ep=EPISODE):
    body = {'is_ranked': bool(ranked), 'requested_to': opponent_id, 'player_order': order, 'map_names': list(maps)}
    return s.api('POST', f'/api/compete/{ep}/request/', body)


def team_id_by_name(c, name, ep=None):
    ep = ep or c.episode
    for t in c.pages(f'/api/team/{ep}/t/?search={urllib.parse.quote(name)}', limit=5):
        if t['name'] == name:
            return t['id']
    raise SystemExit(f'no team named {name!r}')


def public_maps(c, ep=None):
    ep = ep or c.episode
    return [m['name'] for m in c.request('GET', f'/api/episode/{ep}/map/')]


def matches(c, team_id=None, pages=1, ep=None):
    ep = ep or c.episode
    if team_id:
        return c.pages(f'/api/compete/{ep}/match/scrimmage/?team_id={team_id}', limit=pages)
    return c.pages(f'/api/compete/{ep}/match/', limit=pages)     # every match (the Queue page's list)


def get_match(c, mid, ep=None):
    ep = ep or c.episode
    return c.request('GET', f'/api/compete/{ep}/match/{mid}/')


def fetch(c, match, out_dir):
    """Download the match's replay (one .bc26 with every game of the match) -> out_dir/<id>.bc26."""
    url = match.get('replay_url')
    if not url or match.get('status') != 'OK!':
        return None
    os.makedirs(out_dir, exist_ok=True)
    p = os.path.join(out_dir, f'{match["id"]}.bc26')
    if not os.path.exists(p):
        data = c.request('GET', url, raw=True, timeout=600)
        if data[:2] != b'\x1f\x8b':
            raise ApiError(0, f'match {match["id"]}: the replay is not a gzip file')
        with open(p + '.tmp', 'wb') as fh:
            fh.write(data)
        os.replace(p + '.tmp', p)
    return p


def fmt_match(m):
    ps = sorted(m.get('participants') or [], key=lambda p: p.get('player_index', 0))     # team A first
    sc = ' - '.join(f'{p.get("teamname")} {p.get("score") if p.get("score") is not None else "?"}' for p in ps)
    return (f'{m["id"]:>6} {m.get("status"):4} {"ranked  " if m.get("is_ranked") else "unranked"} {sc}  '
            f'maps={",".join(m.get("maps") or [])}')


# ---------------------------------------------------------------- CLI
def main(argv=None):
    ap = argparse.ArgumentParser(description=__doc__.split('\n\n')[0])
    ap.add_argument('--site', default=None)
    sub = ap.add_subparsers(dest='cmd', required=True)
    p = sub.add_parser('register'); p.add_argument('team'); p.add_argument('--about', default='')
    p = sub.add_parser('submit'); p.add_argument('team'); p.add_argument('path'); p.add_argument('-m', default='')
    p.add_argument('--package', default=None)
    p.add_argument('--wait', action='store_true')
    p = sub.add_parser('submissions'); p.add_argument('team')
    sub.add_parser('teams')
    p = sub.add_parser('request'); p.add_argument('team'); p.add_argument('opponent')
    p.add_argument('--ranked', action='store_true'); p.add_argument('--maps', default='')
    p.add_argument('--order', default='?', choices=['+', '-', '?'])
    p = sub.add_parser('challenge'); p.add_argument('team'); p.add_argument('--band', type=int, default=3)
    p.add_argument('--maps', type=int, default=3)
    p = sub.add_parser('matches'); p.add_argument('--team'); p.add_argument('--pages', type=int, default=1)
    p = sub.add_parser('wait'); p.add_argument('match', type=int); p.add_argument('--timeout', type=int, default=3600)
    p = sub.add_parser('fetch'); p.add_argument('match', type=int, nargs='+'); p.add_argument('--out', default='replays-galaxy26')
    a = ap.parse_args(argv)
    c = Client(a.site)
    ep = c.episode
    if a.cmd == 'register':
        acc = load_accounts()
        ensure_team(c, acc, a.team, a.about)
        print(f'team {a.team}: id {acc[a.team]["team_id"]} (user {acc[a.team]["user"]}; auto-accept on)')
    elif a.cmd == 'submit':
        s = session(c, load_accounts(), a.team)
        x = submit(s, a.path, a.m, package=a.package)
        print(f'submission {x["id"]} ({x["_digest"]}) status {x["status"]}')
        if a.wait:
            x = wait_submission(s, x['id'])
            print(f'submission {x["id"]}: {x["status"]} accepted={x["accepted"]}')
            if not x['accepted']:
                print('\n'.join((x.get('logs') or '').splitlines()[-15:]))
                return 1
    elif a.cmd == 'submissions':
        s = session(c, load_accounts(), a.team)
        for x in s.pages(f'/api/compete/{ep}/submission/', limit=2):
            print(x['id'], x['status'], 'accepted' if x['accepted'] else '-', x['created'], x.get('description', ''))
    elif a.cmd == 'teams':
        for i, t in enumerate(rankings(c), 1):
            r = '-' if t['rating'] is None else f'{t["rating"]:.0f}'
            print(f'{i:>3} {r:>6} {t["name"]} (id {t["id"]}{"" if t["active"] else ", no submission"})')
    elif a.cmd == 'request':
        s = session(c, load_accounts(), a.team)
        maps = [m for m in a.maps.split(',') if m]
        r = request(s, team_id_by_name(c, a.opponent), a.ranked, maps, a.order)
        print(f'request {r["id"]} to {a.opponent} ({"ranked" if a.ranked else "unranked"}) status {r.get("status")}')
    elif a.cmd == 'challenge':
        acc = load_accounts()
        s = session(c, acc, a.team)
        above, below = neighbours(rankings(c), acc[a.team]['team_id'], a.band)
        rnd = random.Random()
        pool = [(t, True) for t in above] + [(t, False) for t in below]
        if not pool:
            print('no rated opponent near us'); return 1
        t, ranked = rnd.choice(pool)
        maps = [] if ranked else rnd.sample(public_maps(c), a.maps)
        r = request(s, t['id'], ranked, maps, '?')
        print(f'request {r["id"]}: {a.team} -> {t["name"]} ({"ranked" if ranked else "unranked"}, rated {t["rating"]})')
    elif a.cmd == 'matches':
        tid = team_id_by_name(c, a.team) if a.team else None
        for m in matches(c, tid, a.pages):
            print(fmt_match(m))
    elif a.cmd == 'wait':
        t0 = time.time()
        while True:
            m = get_match(c, a.match)
            if m['status'] in TERMINAL:
                print(fmt_match(m)); break
            if time.time() - t0 > a.timeout:
                print('timeout:', fmt_match(m)); return 1
            time.sleep(15)
    elif a.cmd == 'fetch':
        for mid in a.match:
            m = get_match(c, mid)
            p = fetch(c, m, a.out)
            print(f'{mid}: {p or "no replay (status " + str(m.get("status")) + ")"}')
    return 0


if __name__ == '__main__':
    sys.exit(main())
