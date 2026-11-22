#!/usr/bin/env python3
"""Aggregate prof.jsonl: one row per team (as subject) and our rows split by opponent."""
import json, statistics as st, sys, collections

recs = [json.loads(l) for l in open(sys.argv[1])]
US = 'vibe26b'


def mean(v):
    v = [x for x in v if x is not None]
    return round(st.mean(v), 1) if v else None


def tl(r, at, k):
    t = r['tl'].get(str(at))
    return None if t is None else t.get(k)


def row(rs):
    n = len(rs)
    kills = [r for r in rs if r['winType'] == 'RATKING_DESTROYED']
    o = collections.OrderedDict()
    o['n'] = n
    o['win%'] = round(100 * sum(r['won'] for r in rs) / n)
    o['killW'] = sum(1 for r in kills if r['won'])
    o['killL'] = sum(1 for r in kills if not r['won'])
    o['rounds'] = mean([r['rounds'] for r in rs])
    o['bsR'] = mean([r['backstab'] for r in rs if r['backstab'] is not None and r['backstab'] >= 0])
    o['bsSelf%'] = round(100 * sum(1 for r in rs if r['bsBy'] == 'self') / max(1, sum(1 for r in rs if r['bsBy'])))
    o['bsCause'] = collections.Counter(r['bsCause'] for r in rs if r['bsBy'] == 'self').most_common(2)
    o['maxK'] = mean([r['maxKings'] for r in rs])
    o['2K%'] = round(100 * sum(1 for r in rs if r['maxKings'] >= 2) / n)
    o['r2K'] = mean([r['kingsReach'].get('2') for r in rs])
    o['kFormed'] = mean([r['kingsFormed'] for r in rs])
    for at in (100, 200, 500, 1000):
        o[f'bab{at}'] = mean([tl(r, at, 'b') for r in rs])
    o['babPk'] = mean([r['babPeak'] for r in rs])
    for at in (200, 500, 1000):
        o[f'bank{at}'] = mean([tl(r, at, 'bank') for r in rs])
    o['bankPk'] = mean([r['bankPeak'] for r in rs])
    o['spawned'] = mean([r['spawned'] for r in rs])
    o['xfer'] = mean([r['xfer'] for r in rs])
    o['xfer200'] = mean([tl(r, 200, 'x') for r in rs])
    o['xfer500'] = mean([tl(r, 500, 'x') for r in rs])
    o['xferSh%'] = mean([100 * r['xfer'] / (r['xfer'] + r['xferOpp']) for r in rs if r['xfer'] + r['xferOpp']])
    o['catSh%'] = mean([100 * r['catDmg'] / (r['catDmg'] + r['catDmgOpp']) for r in rs if r['catDmg'] + r['catDmgOpp']])
    o['catDmg'] = mean([r['catDmg'] for r in rs])
    o['ratDeaths'] = mean([r['deathsRat'] for r in rs])
    o['kills'] = mean([r['killsRat'] for r in rs])
    o['throws'] = mean([r['throws'] for r in rs])
    o['grabs'] = mean([r['grabsEnemy'] for r in rs])
    o['ratTrapsLaid'] = mean([r['acts'].get('ratTrap', 0) for r in rs])
    o['rtHits'] = mean([r['ratTrapHits'] for r in rs])
    o['catTraps'] = mean([r['acts'].get('catTrap', 0) for r in rs])
    o['dig'] = mean([r['dirtDug'] for r in rs])
    o['place'] = mean([r['dirtPlaced'] for r in rs])
    o['placeHome'] = mean([r.get('placeNearHome') for r in rs])
    o['bitesKing'] = mean([r['bitesKing'] for r in rs])
    o['dmgKing'] = mean([r['dmgKing'] for r in rs])
    o['starve'] = mean([r['starveDmg'] for r in rs])
    o['k0dmax'] = mean([r['kings'][0]['dmax'] for r in rs if r['kings']])
    o['k0moves'] = mean([r['kings'][0]['moves'] for r in rs if r['kings']])
    o['k0disp500'] = mean([r['kings'][0]['disp'].get('500') for r in rs if r['kings']])
    o['firstTrap'] = mean([r['first'].get('ratTrap') for r in rs])
    o['firstThrow'] = mean([r['first'].get('throw') for r in rs])
    o['firstBiteK'] = mean([r['first'].get('biteKing') for r in rs])
    o['catDeaths'] = mean([r['deathsByCat'] for r in rs])
    o['pickAmt'] = mean([r['pickupAmt'] for r in rs])
    return o


def main():
    by = collections.defaultdict(list)
    usby = collections.defaultdict(list)
    for r in recs:
        if r['team'] == US:
            usby[r['opp']].append(r)
            by[US].append(r)
        else:
            by[r['team']].append(r)
    out = {}
    for t, rs in sorted(by.items(), key=lambda x: -len(x[1])):
        out[t] = row(rs)
    for t, rs in usby.items():
        out['us_vs_' + t] = row(rs)
    # wins vs losses split for us and for opps
    out['us_WIN'] = row([r for r in by[US] if r['won']])
    out['us_LOSS'] = row([r for r in by[US] if not r['won']])
    opp = [r for r in recs if r['team'] != US]
    out['opp_WIN'] = row([r for r in opp if r['won']])
    out['opp_LOSS'] = row([r for r in opp if not r['won']])
    json.dump(out, open(sys.argv[2], 'w'), indent=1)
    keys = list(next(iter(out.values())).keys())
    for k in keys:
        print(f'{k:12}', ' '.join(f'{str(v[k])[:9]:>9}' for v in out.values()))
    print(' ' * 12, ' '.join(f'{t[:9]:>9}' for t in out))


main()
