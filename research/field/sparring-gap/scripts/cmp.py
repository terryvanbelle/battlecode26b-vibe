#!/usr/bin/env python3
import json, statistics as st, sys
rep = [json.loads(l) for l in open('rep.jsonl')]
loc = [json.loads(l) for l in open('loc.jsonl')]
def m(v):
    v = [x for x in v if x is not None]
    return st.mean(v) if v else float('nan')
def g(side, k):
    def f(r):
        x = r[side]
        if k == 'army': return x['turnsRat'] / max(1, r['rounds'])
        if k.startswith('dmgBy.'): return (x.get('dmgBy') or {}).get(k[6:], 0)
        if k.startswith('per100.'):
            return 100 * (x.get(k[7:]) or 0) / max(1, r['rounds'])
        if k.startswith('ratDeaths.'): return (x.get('ratDeaths') or {}).get(k[10:], 0)
        return x.get(k)
    return f
KEYS = ['army', 'spawned', 'per100.spawned', 'pickupAmt', 'per100.pickupAmt', 'transferAmt', 'per100.ratTraps', 'ratTraps', 'ratTrapHits',
        'catTraps', 'per100.throws', 'throws', 'per100.grabsEnemy', 'deathsRat', 'killsRat', 'deathsByEnemy', 'deathsByCat',
        'dmgBy.bite', 'dmgBy.throwHit', 'dmgBy.throwLand', 'dmgBy.trap', 'dmgBy.fed', 'bitesKing', 'dmgKing', 'starveDmg', 'dirtDug',
        'kingsFormed', 'dmgCat', 'squeaks', 'naps', 'forcedDrops', 'friendlyDmg', 'kingPickupAmt']
groups = [
    ('rep opp ALL', rep), ('rep opp WON', [r for r in rep if not r['won']]),
    ('sw48 local', [r for r in loc if r['opp'] == 'sw48']), ('sw35 local', [r for r in loc if r['opp'] == 'sw35']),
]
print(f"{'metric':22}" + ''.join(f'{n:>26}' for n, _ in groups))
print(f"{'games / opp win%':22}" + ''.join(f"{len(rs):>14} {100*m([not r['won'] for r in rs]):>6.0f}%     " for _, rs in groups))
print(f"{'rounds':22}" + ''.join(f"{m([r['rounds'] for r in rs]):>26.0f}" for _, rs in groups))
print(f"{'king-kill end %':22}" + ''.join(f"{100*m([r['winType']=='RATKING_DESTROYED' for r in rs]):>26.0f}" for _, rs in groups))
for k in KEYS:
    print(f'{k:22}' + ''.join(f"{m([g('them',k)(r) for r in rs]):>12.1f} / {m([g('us',k)(r) for r in rs]):<11.1f}" for _, rs in groups))
