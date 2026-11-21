"""Local per-archetype results vs n3 panel totals for every build with a complete n3 panel."""
import json, bisect, glob, os, collections
from datetime import datetime
R = os.path.expanduser('~/projects/vibe/2026b')
MAPS = set(open(f'{R}/tools/galaxy26/maps-early21.txt').read().split())
RUNS = {'xk6':'zd730','xr19':'zd751','xr25':'zd774','xr39':'zd788','xj9':'zd723','xr45':'zd794','xr47':'zd796',
        'xr49':'zd798','xr53':'zd802','xr59':'zd807','xr75':'zd821','xr55':'zd804','xr76':'zd822'}

def n3_totals():
    d = json.load(open(f'{R}/build/replica-matches.json'))
    subs = sorted((datetime.fromisoformat(t).timestamp(), b) for t, b in d['subs']); st = [t for t, _ in subs]
    cells = set()
    for f in glob.glob(f'{R}/research/panel/n3-[0-9]*.json'):
        for o, m in json.load(open(f)).items(): cells.add((o, ','.join(m)))
    res = collections.defaultdict(dict)
    for m in d['matches']:
        if m['status'] != 'OK!': continue
        ps = m['participants']; us = [p for p in ps if p['teamname'] == 'vibe26b']
        if len(us) != 1: continue
        th = [p for p in ps if p['teamname'] != 'vibe26b'][0]['teamname']
        key = (th, ','.join(m['maps']))
        if key not in cells: continue
        i = bisect.bisect_right(st, datetime.fromisoformat(m['created']).timestamp()) - 1
        b = subs[i][1]; c = key + (us[0]['player_index'],)
        if c not in res[b] or res[b][c][0] < m['id']: res[b][c] = (m['id'], us[0]['score'], th)
    out = {}
    for b, cs in res.items():
        if len(cs) == 18:
            per = collections.Counter()
            for _, s, th in cs.values(): per[th] += s
            out[b] = (sum(s for _, s, _ in cs.values()), dict(per))
    return out

def local(build, run):
    """(archetype, map, seat) -> 1 if build won; plus per-game extras."""
    out = {}
    for line in open(f'{R}/runs/{run}/results.jsonl'):
        if not line.startswith('{'): continue
        g = json.loads(line)
        if g['map'] not in MAPS: continue
        if g['a'] == build: opp, seat, me = g['b'], 'A', 'A'
        elif g['b'] == build: opp, seat, me = g['a'], 'B', 'B'
        else: continue
        out[(opp, g['map'], seat)] = dict(win=int(g['winner'] == me), rounds=g.get('rounds', 0),
                                          xfer_me=g.get('cheeseXfer' + me, 0), xfer_them=g.get('cheeseXfer' + ('B' if me == 'A' else 'A'), 0),
                                          wintype=g.get('winType'))
    return out
