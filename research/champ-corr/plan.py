"""Champion correlation study (owner 11-23 11:48 PDT): 10 local champions evenly spaced over time; for each, win rate
vs xk6 (local, the box) against win rate vs AlexT101_usqualsbot (replica). Reuses every replica game a champion's code
already played against AlexT101_usqualsbot and tops up to 5 games on fixed maps.
  python3 -I research/champ-corr/plan.py MATCHES.json SUBS.json   (dumps of contest matches/submissions)
Writes research/champ-corr/plan.json."""
import csv, datetime, json, os, sys
HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(os.path.dirname(HERE))
T = lambda t: datetime.datetime.fromisoformat(t)
PDT = datetime.timezone(datetime.timedelta(hours=-7))
OPP = 'AlexT101_usqualsbot'
E21 = [l.strip() for l in open(os.path.join(REPO, 'tools/galaxy26/maps-early21.txt')) if l.strip()]
MAPS = [E21[i] for i in (0, 5, 10, 15, 20)]          # evenly spaced over the e21 list
N = 10; GAMES = 5


def champions():
    rows = list(csv.DictReader(open(os.path.join(REPO, 'research/tourney/champions.csv'))))
    out = []
    for r in rows:
        if not r['submission']:
            continue                                  # xk6 is the reference opponent, not a sample point
        t = datetime.datetime.strptime(r['when_pdt'], '%Y-%m-%d %H:%M').replace(tzinfo=PDT)
        out.append((t, r['bot'], int(r['submission'])))
    return out


def sample(ch):
    """Targets evenly spaced from the first to the last crowning; each takes the champion crowned nearest to it
    (a tie goes to the one reigning at the target); no champion twice."""
    t0, t1 = ch[0][0], ch[-1][0]
    picks = []
    for i in range(N):
        t = t0 + (t1 - t0) * i / (N - 1)
        best = min((c for c in ch if c[1] not in [p[1] for p in picks]),
                   key=lambda c: (abs((c[0] - t).total_seconds()), c[0] > t))
        picks.append(best)
    return picks


def main(mpath, spath):
    ms = json.load(open(mpath)); subs = json.load(open(spath))
    subs = sorted([x for x in subs if x['accepted']], key=lambda x: T(x['created']))
    ch = champions()
    pick = sample(ch)

    def sub_at(t):
        cur = None
        for x in subs:
            if T(x['created']) <= t:
                cur = x
        return cur
    plan = []
    for t, bot, sid in pick:
        games = []
        for m in ms:
            if m['status'] != 'OK!':
                continue
            us = [p for p in m['participants'] if p['team'] == 9]
            them = [p for p in m['participants'] if p['team'] != 9]
            if not us or not them or them[0]['teamname'] != OPP:
                continue
            x = sub_at(T(m['created']))
            if x and x['package'] == bot:             # this champion's code, crowned or as an earlier candidate
                games.append({'match': m['id'], 'sub': x['id'], 'ranked': m['is_ranked'], 'maps': m['maps'],
                              'won': us[0]['score'], 'lost': them[0]['score']})
        n = sum(g['won'] + g['lost'] for g in games)
        played = {mp for g in games for mp in g['maps']}
        need = max(0, GAMES - n)
        top = [mp for mp in MAPS if mp not in played][:need]
        plan.append({'bot': bot, 'crowned_pdt': t.strftime('%m-%d %H:%M'), 'champion_sub': sid,
                     'replica_games': n, 'replica_record': games, 'topup_maps': top})
    json.dump({'opponent': OPP, 'maps': MAPS, 'plan': plan}, open(os.path.join(HERE, 'plan.json'), 'w'), indent=1)
    for p in plan:
        w = sum(g['won'] for g in p['replica_record']); l = sum(g['lost'] for g in p['replica_record'])
        print(f"{p['bot']:5} crowned {p['crowned_pdt']}  have {w}-{l} ({p['replica_games']} games, "
              f"{len(p['replica_record'])} matches)  top-up {p['topup_maps']}")


if __name__ == '__main__':
    main(*sys.argv[1:3])
