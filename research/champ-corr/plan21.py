"""The firmed-up champion study (owner 11-23 12:45: "firm up the game count"): every sampled champion on all 21 e21
maps (tools/galaxy26/maps-early21.txt), on both sides.
  - Local: vs bots/xk6 on every e21 map in both seats (seed 1), 42 games a champion. cs001's 5 maps as team A are
    reused; the rest go to runs cs002-cs004 (balanced, round robin).
  - Replica: vs AlexT101_usqualsbot as team A on every e21 map, 21 games a champion. The replica is deterministic
    (one seed per map; repeated matches identical), so a cell already played as team A (the 11:54 top-ups) is reused
    instead of replayed. Requests take at most 10 maps (server rule), so each champion's maps are split evenly.
Writes plan21.json and runs/cs00[2-4]/cells.tsv.  python3 research/champ-corr/plan21.py  (from the repo root)"""
import json, os
HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(os.path.dirname(HERE))
E21 = open(os.path.join(REPO, 'tools/galaxy26/maps-early21.txt')).read().split()
assert len(E21) == 21
plan = json.load(open(os.path.join(HERE, 'plan.json')))
topup = json.load(open(os.path.join(HERE, 'topup.json')))
played_a = {}                       # bot -> maps already played as team A on the replica (top-ups)
for d in topup:
    played_a.setdefault(d['bot'], set()).update(d['maps'])
local_a = set()                     # (bot, map) already played as team A vs xk6 (cs001)
for line in open(os.path.join(REPO, 'runs/cs001/cells.tsv')):
    a, b, m, seed = line.rstrip('\n').split('\t')
    assert b == 'bots/xk6' and seed == '1'
    local_a.add((a[5:], m))
out, cells = [], []
for p in plan['plan']:
    bot = p['bot']
    need = [m for m in E21 if m not in played_a.get(bot, set())]
    k = -(-len(need) // 10)                      # requests of at most 10 maps, sizes as even as possible
    chunks = [need[i * len(need) // k:(i + 1) * len(need) // k] for i in range(k)] if need else []
    out.append({'bot': bot, 'crowned_pdt': p['crowned_pdt'], 'replica_reused_a': sorted(played_a.get(bot, set())),
                'replica_requests': chunks})
    for m in E21:
        if (bot, m) not in local_a:
            cells.append((f'bots/{bot}', 'bots/xk6', m, '1'))
        cells.append(('bots/xk6', f'bots/{bot}', m, '1'))
runs = ['cs002', 'cs003', 'cs004']
for i, r in enumerate(runs):
    os.makedirs(os.path.join(REPO, 'runs', r), exist_ok=True)
    with open(os.path.join(REPO, 'runs', r, 'cells.tsv'), 'w') as f:
        for c in cells[i::len(runs)]:
            f.write('\t'.join(c) + '\n')
json.dump({'opponent': plan['opponent'], 'local_opponent': 'xk6', 'maps': E21, 'local_runs': ['cs001'] + runs,
           'plan': out}, open(os.path.join(HERE, 'plan21.json'), 'w'), indent=1)
print(f'{len(cells)} new local cells in {runs}; replica requests', sum(len(o['replica_requests']) for o in out),
      'maps', sum(len(c) for o in out for c in o['replica_requests']))
for o in out:
    print(o['bot'], 'reuse', len(o['replica_reused_a']), 'requests', [len(c) for c in o['replica_requests']])
