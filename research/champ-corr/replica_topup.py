"""Top up the champion study's replica games (plan.json topup_maps): for each champion short of 5 games against
AlexT101_usqualsbot, make its bot the active submission, request one unranked match on its top-up maps (we are team A,
player order '+'), and wait until siarnaq has created the match (it fixes the match's submission then) before the next
switch. sw54 (the champion, already active) goes first without a submission; at the end sw54 is submitted again so the
local champion is the active submission. Any failure stops the switching and restores sw54.
  python3 -I research/champ-corr/replica_topup.py   (from the repo root; log: research/champ-corr/topup.log)"""
import datetime, json, os, sys, time
HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(os.path.dirname(HERE))
sys.path.insert(0, os.path.join(REPO, 'tools/galaxy26'))
import client as cl

CHAMP = 'sw54'
LOG = open(os.path.join(HERE, 'topup.log'), 'a')


def log(msg):
    line = f'{datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=-7))):%m-%d %H:%M:%S} PDT {msg}'
    print(line, flush=True); LOG.write(line + '\n'); LOG.flush()


def main():
    plan = json.load(open(os.path.join(HERE, 'plan.json')))
    opp = plan['opponent']
    c = cl.Client(None); acc = cl.load_accounts(); s = cl.session(c, acc, 'vibe26b')
    my = acc['vibe26b']['team_id']; ep = c.episode
    oid = cl.team_id_by_name(c, opp)
    out_path = os.path.join(HERE, 'topup.json')
    done = json.load(open(out_path)) if os.path.exists(out_path) else []
    todo = [p for p in plan['plan'] if p['topup_maps'] and p['bot'] not in [d['bot'] for d in done]]
    todo.sort(key=lambda p: p['bot'] != CHAMP)          # the active champion first: no switch needed
    active = CHAMP
    try:
        for p in todo:
            bot, maps = p['bot'], p['topup_maps']
            sid = None
            if bot != active:
                x = cl.submit(s, os.path.join(REPO, 'bots', bot), f'champ-corr study: {bot} re-run', ep)
                x = cl.wait_submission(s, x['id'], ep)
                if not x.get('accepted'):
                    raise RuntimeError(f'{bot}: submission {x["id"]} not accepted ({x.get("status")})')
                sid, active = x['id'], bot
                log(f'{bot}: submission {sid} accepted, active')
            before = {m['id'] for m in cl.matches(c, my, 1)}
            t_req = datetime.datetime.now(datetime.timezone.utc)
            r = cl.request(s, oid, False, maps, '+', ep)
            log(f'{bot}: request {r.get("id")} vs {opp} on {maps} (status {r.get("status")})')
            mid = None
            for _ in range(90):                          # up to 15 minutes for the match to be created
                for m in cl.matches(c, my, 1):
                    them = [q for q in m['participants'] if q['team'] != my]
                    if (m['id'] not in before and them and them[0]['teamname'] == opp
                            and sorted(m['maps'] or []) == sorted(maps)):
                        mid = m['id']; created = m['created']
                        break
                if mid:
                    break
                time.sleep(10)
            if not mid:
                raise RuntimeError(f'{bot}: no match created within 15 min of request {r.get("id")}')
            log(f'{bot}: match {mid} created {created}')
            done.append({'bot': bot, 'submission': sid, 'request': r.get('id'), 'match': mid, 'created': created,
                         'maps': maps, 'requested_utc': t_req.isoformat()})
            json.dump(done, open(out_path, 'w'), indent=1)
    except Exception as e:
        log(f'STOPPED: {e!r}')
    finally:
        if active != CHAMP:
            x = cl.submit(s, os.path.join(REPO, 'bots', CHAMP), f'local tournament champion {CHAMP} (restored after the champ-corr study)', ep)
            x = cl.wait_submission(s, x['id'], ep)
            log(f'restored {CHAMP}: submission {x["id"]} accepted={x.get("accepted")}')
    log(f'done: {len(done)} top-up matches')


if __name__ == '__main__':
    main()
