import json, sys, glob, os
for f in sorted(glob.glob(sys.argv[1] + '/*.log')):
    n = os.path.basename(f)[:-4]
    try: d = json.loads(open(f).read().strip().split('\n')[-1])
    except Exception: print(n, 'no result'); continue
    me = 'A' if d['a'].startswith('xj') else 'B'
    th = 'B' if me == 'A' else 'A'
    u = d[me]; t = d[th]
    print('%-18s %s r%5d rt %3d/%3d tdeath %3d/%3d exc %d xf %5d/%5d kd %s' % (n, 'W' if d['winner'] == me else 'L', d['rounds'], u['ratTraps'], t['ratTraps'], u['ratDeaths'].get('trap', 0), t['ratDeaths'].get('trap', 0), u['exceptions'], u['transferAmt'], t['transferAmt'], u['kingDeaths']))
