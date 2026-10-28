#!/usr/bin/env python3
"""The field on the bc26 replica: test/practice bots as ordinary contestant teams, and their scrimmages in spare VM
capacity. Every action goes through the site's public API as the team's own user (tools/galaxy26/client.py). Standard
library only. Re-runnable: a step already done is skipped. Our own team (vibe26b) is not handled here:
tools/galaxy26/contest.py submits and plays for it.

  python3 tools/galaxy26/field.py plan                 the entrants seed would register (no network)
  python3 tools/galaxy26/field.py seed [--only NAME]   register + upload one team per row of tools/galaxy26/field-teams.tsv
                                                       (team, Java package directory, about). A team whose uploaded
                                                       source is unchanged (content hash) is not re-uploaded.
  python3 tools/galaxy26/field.py status               one line per team: rating, latest submission status
  python3 tools/galaxy26/field.py queue                the replica queue: matches waiting (NEW/QUE/TRY), by status, running
  python3 tools/galaxy26/field.py activity [--once] [--poll 60] [--low 2]
        spare-cycle play (bc26-galaxy-activity.service, installed DISABLED): while fewer than --low matches wait in the
        queue, a random field team challenges a team near its rating: one of the 3 active teams rated closest at or
        above it, as a RANKED request (galaxy's rule: ranked only upward; 3 random maps chosen by galaxy, alternating
        order); the top team instead challenges one of the 3 closest below it, unranked, on 3 random maps. Any team
        can be the opponent, ours included. A 429 (hourly limit) rests the challenger for an hour, a 409 for 10
        minutes. saturn runs at most GALAXY_EXECUTE_SLOTS matches at once whatever the queue holds.
  python3 tools/galaxy26/field.py boost --teams A,B [--n 5] [--dry-run]
        extra ranked matches for chosen field teams whose rating has not settled: each challenges --n opponents that
        have played, at or above its rating, spread from the top of the ladder down (boost_opponents).
  python3 tools/galaxy26/field.py rate [--team vibe26b] [--n 6] [--dry-run]
        ranked matches for our team's new build: the n field teams rated closest at or below it each challenge it,
        ranked (galaxy takes ranked requests only upward, so a team at the top cannot ask for them itself).

Accounts: $GALAXY26_CONF/accounts.json (default ~/.bc26-galaxy/accounts.json, mode 600). The activity unit on the VM
reads ~/.bc26-galaxy-field/accounts.json (README "Timers": copy the driver's file there to enable it).
"""
import argparse
import json
import os
import random
import signal
import sys
import time

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import client as cl  # noqa: E402

FIELD_TSV = os.environ.get('GALAXY26_FIELD') or os.path.join(HERE, 'field-teams.tsv')
OUR_TEAMS = ('vibe26b',)           # never a challenger here (contest.py plays for it)


def log(*a):
    print(time.strftime('%Y-%m-%d %H:%M:%S'), *a, flush=True)


def entrants(only=None, path=FIELD_TSV):
    """[{'team', 'entrant', 'path', 'about', 'note'}] from the field table (tab-separated: team, package dir with ~
    expanded, about; # comments). path is None (with a note) when the directory has no .java file."""
    rows = []
    with open(path) as fh:
        for line in fh:
            line = line.rstrip('\n')
            if not line.strip() or line.lstrip().startswith('#'):
                continue
            team, pkg, about = (line.split('\t') + ['', ''])[:3]
            team, pkg = cl.team_name(team.strip()), os.path.expanduser(pkg.strip())
            if team in OUR_TEAMS:
                continue
            ok = os.path.isdir(pkg) and any(f.endswith('.java') for f in os.listdir(pkg))
            rows.append({'team': team, 'entrant': os.path.basename(pkg.rstrip('/')), 'path': pkg if ok else None,
                         'about': about.strip(), 'note': '' if ok else f'no Java package at {pkg}'})
    if only:
        rows = [r for r in rows if r['team'] == only or r['entrant'] == only]
    return rows


def seed(c, acc, rows):
    save = lambda: cl.save_accounts(acc)  # noqa: E731
    done = 0
    for r in rows:
        if not r['path']:
            log(f'skip {r["entrant"]}: {r["note"]}')
            continue
        try:
            s = cl.ensure_team(c, acc, r['team'], r['about'] or f'{r["entrant"]}: a Battlecode 2026 bot.', save)
            _, _, digest = cl.bot_zip(r['path'])
            a = acc[r['team']]
            subs = s.pages(f'/api/compete/{c.episode}/submission/', limit=1)
            pending = any(x['status'] not in cl.TERMINAL for x in subs)
            if a.get('digest') == digest and (pending or any(x['accepted'] for x in subs)):
                continue
            x = cl.submit(s, r['path'], r['entrant'], c.episode)
            a['digest'], a['entrant'] = digest, r['entrant']
            a.setdefault('submissions', []).append(x['id'])
            save()
            done += 1
            log(f'{r["team"]}: uploaded submission {x["id"]} ({r["entrant"]}, {digest})')
        except cl.ApiError as e:
            log(f'{r["team"]}: API error {e}')
    return done


def status(c, acc):
    ladder = {t['id']: t for t in cl.rankings(c)}
    for team, a in sorted(acc.items()):
        if not a.get('team_id'):
            continue
        t = ladder.get(a['team_id'], {})
        try:
            subs = cl.Session(c, a['user'], a['password']).pages(f'/api/compete/{c.episode}/submission/', limit=1)
        except cl.ApiError as e:
            subs = []
            log(f'{team}: {e}')
        last = subs[0] if subs else {}
        r = t.get('rating')
        print(f'{team:32} id {a["team_id"]:>4} rating {"-" if r is None else f"{r:.0f}":>6} '
              f'latest submission {last.get("id", "-")} {last.get("status", "-")} accepted={last.get("accepted", "-")}')


# ---------------------------------------------------------------- spare-cycle play
def queue_backlog(c, pages=5):
    """Matches not started yet or retrying (the public match list, newest first)."""
    n = 0
    for page in range(1, pages + 1):
        r = c.request('GET', f'/api/compete/{c.episode}/match/?page={page}')
        ms = r.get('results', [])
        n += sum(1 for m in ms if m['status'] in cl.WAITING)
        if not r.get('next'):
            break
    return n


def queue_census(matches):
    """{status: n}, waiting (NEW/QUE/TRY), waiting ranked, and the (id, teams) of matches running or retrying."""
    by = {}
    for m in matches:
        by[m['status']] = by.get(m['status'], 0) + 1
    waiting = [m for m in matches if m['status'] in cl.WAITING]
    live = [(m['id'], m['status'], [p['teamname'] for p in m['participants']])
            for m in matches if m['status'] in ('RUN', 'TRY')]
    return {'by_status': by, 'waiting': len(waiting), 'ranked': sum(1 for m in waiting if m.get('is_ranked')),
            'live': sorted(live)}


def all_matches(c):
    ms, page = [], 1
    while True:
        r = c.request('GET', f'/api/compete/{c.episode}/match/?page={page}')
        ms += r.get('results', [])
        if not r.get('next'):
            return ms
        page += 1


def pick_challenge(ladder, ids, rnd, band=3):
    """(challenger name, opponent team dict, ranked) or None. ids: {name: team id} of the teams that may challenge
    now. The challenger is uniform over them; the opponent is one of the `band` active teams rated closest at or above
    it (ranked), or, for a team with nobody above, one of the `band` closest below (unranked)."""
    active = {t['id'] for t in ladder if t['active'] and t['status'] == 'R'}
    pool = sorted(n for n, i in ids.items() if i in active)
    if not pool:
        return None
    ch = rnd.choice(pool)
    above, below = cl.neighbours(ladder, ids[ch], band)
    if above:
        return ch, rnd.choice(above), True
    if below:
        return ch, rnd.choice(below), False
    return None


def boost_opponents(ladder, my_id, n):
    """n opponents for a team that needs games: active teams that have played (rating above 0) and are rated at
    least as high as it (galaxy: ranked only upward), spread evenly over that list from the top down, the top team
    first; repeated in turn when there are fewer than n."""
    me = next((t for t in ladder if t['id'] == my_id), None)
    if me is None:
        return []
    mine = me['rating'] or 0
    pool = sorted((t for t in ladder if t['id'] != my_id and t['active'] and t['status'] == 'R'
                   and (t['rating'] or 0) > 0 and t['rating'] >= mine), key=lambda t: (-t['rating'], t['id']))
    if not pool:
        return []
    return [pool[i * len(pool) // n] if n <= len(pool) else pool[i % len(pool)] for i in range(n)]


def boost(c, acc, names, n, dry_run=False):
    """Ranked requests: each named team (galaxy name) challenges boost_opponents. Returns the requests made."""
    ladder = cl.rankings(c)
    by_name = {t['name']: t for t in ladder}
    by_id = {a['team_id']: (t, a) for t, a in acc.items() if a.get('team_id')}
    made = 0
    for name in names:
        t = by_name.get(name)
        if t is None or t['id'] not in by_id or by_id[t['id']][0] in OUR_TEAMS:
            log(f'boost: {name}: not a field team of ours on the ladder; skipped')
            continue
        s = None if dry_run else cl.Session(c, by_id[t['id']][1]['user'], by_id[t['id']][1]['password'])
        for opp in boost_opponents(ladder, t['id'], n):
            if dry_run:
                log(f'boost (dry run): {name} -> {opp["name"]} (rating {opp["rating"]:.0f})')
                continue
            try:
                r = cl.request(s, opp['id'], True, [], '?', c.episode)
                made += 1
                log(f'boost: request {r["id"]}: {name} -> {opp["name"]} (ranked)')
            except cl.ApiError as e:
                log(f'boost: refused: {name} -> {opp["name"]}: {e}')
                break
    return made


def rate_challengers(ladder, team_id, n):
    """The n active teams that have played (rating above 0) closest at or below team_id's rating, nearest first
    (galaxy: a ranked request goes only to a team rated at or above the challenger); [] when team_id is missing or
    unrated."""
    me = next((t for t in ladder if t['id'] == team_id), None)
    if me is None or me['rating'] is None:
        return []
    pool = sorted((t for t in ladder if t['id'] != team_id and t['active'] and t['status'] == 'R'
                   and (t['rating'] or 0) > 0 and t['rating'] <= me['rating']),
                  key=lambda t: (me['rating'] - t['rating'], t['id']))
    return pool[:n]


def rate(c, acc, team, n, dry_run=False):
    """Ranked requests to our team `team` from rate_challengers among the field teams we hold accounts for. Returns
    the requests made."""
    ladder = cl.rankings(c)
    me = next((t for t in ladder if t['name'] == team), None)
    if me is None:
        log(f'rate: {team}: not on the ladder; nothing requested')
        return 0
    by_id = {a['team_id']: a for t, a in acc.items() if a.get('team_id') and t not in OUR_TEAMS}
    made = 0
    for ch in rate_challengers([t for t in ladder if t['id'] == me['id'] or t['id'] in by_id], me['id'], n):
        if dry_run:
            log(f'rate (dry run): {ch["name"]} (rating {ch["rating"]:.0f}) -> {team} (rating {me["rating"]:.0f})')
            continue
        a = by_id[ch['id']]
        try:
            r = cl.request(cl.Session(c, a['user'], a['password']), me['id'], True, [], '?', c.episode)
            made += 1
            log(f'rate: request {r["id"]}: {ch["name"]} -> {team} (ranked)')
        except cl.ApiError as e:
            log(f'rate: refused: {ch["name"]} -> {team}: {e}')
    return made


def activity(c, acc, poll=60, once=False, seed_=None, n_maps=3, low=2):
    rnd = random.Random(seed_)
    ids, sess, rest, stop = {}, {}, {}, {'now': False}

    def join(acc):                              # teams seed registered since the last poll
        for t, a in acc.items():
            if a.get('team_id') and t not in OUR_TEAMS and t not in ids:
                ids[t], sess[t] = a['team_id'], cl.Session(c, a['user'], a['password'])
    join(acc)

    def on_signal(signum, frame):
        stop['now'] = True
    signal.signal(signal.SIGTERM, on_signal)
    signal.signal(signal.SIGINT, on_signal)
    maps = cl.public_maps(c)
    made = 0
    log(f'activity: {len(ids)} field teams, keep {low} match(es) waiting, poll {poll}s')
    while not stop['now']:
        try:
            n0 = len(ids)
            join(cl.load_accounts())
            if len(ids) > n0:
                log(f'activity: {len(ids) - n0} new field team(s), {len(ids)} in all')
            want = low - queue_backlog(c)
            if want > 0:
                ladder = cl.rankings(c)
                for _ in range(want):
                    now = time.time()
                    choice = pick_challenge(ladder, {n: i for n, i in ids.items() if rest.get(n, 0) <= now}, rnd)
                    if not choice:
                        break
                    name, opp, ranked = choice
                    try:
                        r = cl.request(sess[name], opp['id'], ranked, [] if ranked else rnd.sample(maps, n_maps), '?',
                                       c.episode)
                        made += 1
                        log(f'request {r["id"]}: {name} -> {opp["name"]} ({"ranked" if ranked else "unranked"})')
                    except cl.ApiError as e:
                        rest[name] = time.time() + (3600 if e.code == 429 else 600)
                        log(f'refused: {name} -> {opp["name"]}: {e}')
        except cl.ApiError as e:
            log(f'API error: {e}')
        except Exception as e:                  # a transient failure must not end the loop
            log(f'error: {type(e).__name__}: {e}')
            if once:
                raise
        if once:
            break
        for _ in range(poll):
            if stop['now']:
                break
            time.sleep(1)
    return made


def main(argv=None):
    ap = argparse.ArgumentParser(description=__doc__.split('\n\n')[0])
    ap.add_argument('--site', default=None)
    sub = ap.add_subparsers(dest='cmd', required=True)
    sub.add_parser('plan')
    p = sub.add_parser('seed'); p.add_argument('--only')
    sub.add_parser('status')
    sub.add_parser('queue')
    p = sub.add_parser('activity'); p.add_argument('--once', action='store_true')
    p.add_argument('--poll', type=int, default=60); p.add_argument('--low', type=int, default=2)
    p = sub.add_parser('boost'); p.add_argument('--teams', required=True); p.add_argument('--n', type=int, default=5)
    p.add_argument('--dry-run', action='store_true')
    p = sub.add_parser('rate'); p.add_argument('--team', default=OUR_TEAMS[0]); p.add_argument('--n', type=int, default=6)
    p.add_argument('--dry-run', action='store_true')
    a = ap.parse_args(argv)
    if a.cmd == 'plan':
        rows = entrants()
        for r in rows:
            print(f'{r["team"]:32} {r["entrant"]:40} {r["path"] or "-"} {r["note"]}')
        print(f'{len(rows)} entrants ({sum(1 for r in rows if r["path"])} uploadable)')
        return 0
    c = cl.Client(a.site)
    if a.cmd == 'queue':
        q = queue_census(all_matches(c))
        print(f"waiting {q['waiting']} (ranked {q['ranked']}); by status {q['by_status']}")
        for mid, st, teams in q['live']:
            print(f'  {mid} {st} {" vs ".join(teams)}')
        return 0
    acc = cl.load_accounts()
    if a.cmd == 'seed':
        rows = entrants(a.only)
        n = seed(c, acc, rows)
        log(f'seed: {n} upload(s)')
    elif a.cmd == 'status':
        status(c, acc)
    elif a.cmd == 'activity':
        activity(c, acc, a.poll, a.once, low=a.low)
    elif a.cmd == 'boost':
        log(f'boost: {boost(c, acc, [x for x in a.teams.split(",") if x], a.n, a.dry_run)} request(s)')
    elif a.cmd == 'rate':
        log(f'rate: {rate(c, acc, a.team, a.n, a.dry_run)} request(s)')
    return 0


if __name__ == '__main__':
    sys.exit(main())
