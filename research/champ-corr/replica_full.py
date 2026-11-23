"""The firmed-up study's replica games (plan21.json replica_requests): each sampled champion as team A (order '+')
against AlexT101_usqualsbot on every e21 map it has not played as team A yet, in unranked requests of at most 10 maps.
Like replica_topup.py: make the bot the active submission, request, and wait until siarnaq has created the match (that
fixes its submission) before the next request or switch.
  - The switching starts right after a ranked match of ours is created (up to 15 minutes' wait), so the old champions
    are active in the gap before the next ranked match, not in the middle of one.
  - The champion in research/tourney/champion.txt at the start goes first (it is the active submission: no switch).
  - At the end, or on any failure, the champion in champion.txt at THAT moment is submitted again, so a crowning
    during the run is restored too.
  python3 -I research/champ-corr/replica_full.py   (from the repo root; log: full.log, results: full.json)"""
import datetime, json, os, sys, time
HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(os.path.dirname(HERE))
sys.path.insert(0, os.path.join(REPO, 'tools/galaxy26'))
import client as cl

LOG = open(os.path.join(HERE, 'full.log'), 'a')
CHAMP_FILE = os.path.join(REPO, 'research/tourney/champion.txt')


def log(msg):
    line = f'{datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=-7))):%m-%d %H:%M:%S} PDT {msg}'
    print(line, flush=True); LOG.write(line + '\n'); LOG.flush()


def champion():
    return open(CHAMP_FILE).read().split()[0]


def main():
    plan = json.load(open(os.path.join(HERE, 'plan21.json')))
    opp = plan['opponent']
    c = cl.Client(None); acc = cl.load_accounts(); s = cl.session(c, acc, 'vibe26b')
    my = acc['vibe26b']['team_id']; ep = c.episode
    oid = cl.team_id_by_name(c, opp)
    out_path = os.path.join(HERE, 'full.json')
    done = json.load(open(out_path)) if os.path.exists(out_path) else []
    have = {(d['bot'], tuple(d['maps'])) for d in done}
    champ0 = champion()
    todo = [(p['bot'], ch) for p in plan['plan'] for ch in p['replica_requests'] if (p['bot'], tuple(ch)) not in have]
    todo.sort(key=lambda t: t[0] != champ0)            # the active champion first (stable: plan order after it)
    log(f'start: {len(todo)} requests, champion {champ0} active')
    # wait for a fresh ranked match, so the switching window falls in the gap before the next one
    ranked0 = {m['id'] for m in cl.matches(c, my, 1) if m['is_ranked']}
    t0 = time.time()
    while time.time() - t0 < 900:
        new = [m for m in cl.matches(c, my, 1) if m['is_ranked'] and m['id'] not in ranked0]
        if new:
            log(f'ranked match {new[0]["id"]} created {new[0]["created"]}: switching starts')
            break
        time.sleep(15)
    else:
        log('no ranked match in 15 minutes: switching starts anyway')
    active = champ0
    try:
        for bot, maps in todo:
            sid = None
            if bot != active:
                x = cl.submit(s, os.path.join(REPO, 'bots', bot), f'champ-corr study (21 maps): {bot} re-run', ep)
                x = cl.wait_submission(s, x['id'], ep)
                if not x.get('accepted'):
                    raise RuntimeError(f'{bot}: submission {x["id"]} not accepted ({x.get("status")})')
                sid, active = x['id'], bot
                log(f'{bot}: submission {sid} accepted, active')
            before = {m['id'] for m in cl.matches(c, my, 1)}
            t_req = datetime.datetime.now(datetime.timezone.utc)
            r = cl.request(s, oid, False, maps, '+', ep)
            log(f'{bot}: request {r.get("id")} vs {opp}, {len(maps)} maps (status {r.get("status")})')
            mid = None
            for _ in range(180):                         # up to 15 minutes for the match to be created
                for m in cl.matches(c, my, 1):
                    them = [q for q in m['participants'] if q['team'] != my]
                    if (m['id'] not in before and them and them[0]['teamname'] == opp
                            and sorted(m['maps'] or []) == sorted(maps)):
                        mid = m['id']; created = m['created']
                        break
                if mid:
                    break
                time.sleep(5)
            if not mid:
                raise RuntimeError(f'{bot}: no match created within 15 min of request {r.get("id")}')
            log(f'{bot}: match {mid} created {created}')
            done.append({'bot': bot, 'submission': sid, 'request': r.get('id'), 'match': mid, 'created': created,
                         'maps': maps, 'requested_utc': t_req.isoformat()})
            json.dump(done, open(out_path, 'w'), indent=1)
    except Exception as e:
        log(f'STOPPED: {e!r}')
    finally:
        champ = champion()
        if active != champ:
            x = cl.submit(s, os.path.join(REPO, 'bots', champ), f'local tournament champion {champ} (restored after the champ-corr 21-map study)', ep)
            x = cl.wait_submission(s, x['id'], ep)
            log(f'restored {champ}: submission {x["id"]} accepted={x.get("accepted")}')
    log(f'done: {len(done)} matches in full.json')


if __name__ == '__main__':
    main()
