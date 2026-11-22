#!/usr/bin/env python3
"""Per-window means from war.jsonl: us in wins / losses, and the opponent in the same games, per game with
the window reached (windows past the game end are skipped)."""
import json, sys, collections

recs = [json.loads(l) for l in open(sys.argv[1])]
WIN = ('1-100', '101-200', '201-400', '401-800', '801-2000')
LO = (0, 100, 200, 400, 800)
US = 'vibe26b'
by = {}
for r in recs:
    by[(r['match'], r['game'], r['team'] == US)] = r


def show(label, pairs):
    print(f'== {label}: {len(pairs)} games')
    print(f"{'window':10} {'n':>3} | {'usDie':>6} {'opDie':>6} | {'usSpn':>6} {'opSpn':>6} | {'usTrp':>6} {'opTrp':>6} | "
          f"{'usThr':>6} {'opThr':>6} | {'usXfr':>6} {'opXfr':>6} | {'usFwd%':>6} {'opFwd%':>6} | {'usNrEK':>6} {'opNrEK':>6}")
    for i, w in enumerate(WIN):
        ps = [(u, o) for u, o in pairs if u['rounds'] > LO[i]]
        if not ps:
            continue
        m = lambda f: sum(f(u, o) for u, o in ps) / len(ps)
        fw = lambda x: 100 * x['fwd'][i] / max(1, x['turns'][i])
        print(f"{w:10} {len(ps):>3} | {m(lambda u, o: u['deaths'][i]):6.1f} {m(lambda u, o: o['deaths'][i]):6.1f} | "
              f"{m(lambda u, o: u['spawns'][i]):6.1f} {m(lambda u, o: o['spawns'][i]):6.1f} | "
              f"{m(lambda u, o: u['traps'][i]):6.1f} {m(lambda u, o: o['traps'][i]):6.1f} | "
              f"{m(lambda u, o: u['throws'][i]):6.1f} {m(lambda u, o: o['throws'][i]):6.1f} | "
              f"{m(lambda u, o: u['xfer'][i]):6.0f} {m(lambda u, o: o['xfer'][i]):6.0f} | "
              f"{m(lambda u, o: fw(u)):6.1f} {m(lambda u, o: fw(o)):6.1f} | "
              f"{m(lambda u, o: u['nearEK'][i]):6.0f} {m(lambda u, o: o['nearEK'][i]):6.0f}")


pairs = []
for (mid, g, us), r in by.items():
    if us and (mid, g, False) in by:
        pairs.append((r, by[(mid, g, False)]))
show('all', pairs)
show('our losses', [p for p in pairs if not p[0]['won']])
show('our wins', [p for p in pairs if p[0]['won']])
groups = collections.defaultdict(list)
for p in pairs:
    groups[p[1]['team']].append(p)
for t, ps in sorted(groups.items(), key=lambda x: -len(x[1])):
    show('vs ' + t, ps)
