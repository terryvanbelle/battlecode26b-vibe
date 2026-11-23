"""How each side's throws end (replica games MATCH:MAP ...): the thrown rat's land row (target = hit a robot or wall,
ground = full flight), rounds in flight, the thrown rat's hp before the throw, and whether the throw killed it."""
import json, os, sys, collections
sys.path.insert(0, '/home/terryvanbelle/projects/vibe/2026b/tools')
import game_report as gr
T = collections.defaultdict(collections.Counter)
for arg in sys.argv[1:]:
    m, _, want = arg.partition(':')
    d = gr.source(os.path.expanduser(f'~/projects/vibe/bc26b-galaxy-replays/{m}'))
    gi = 0
    while os.path.exists(os.path.join(d, f'g{gi}.meta.json')):
        pre = os.path.join(d, f'g{gi}'); gi += 1
        meta = json.load(open(pre + '.meta.json'))
        if want and meta.get('map') != want: continue
        us = 1 if str(meta.get('teamA', '')).startswith('vibe26b') else 2
        side = lambda t: 'us' if str(t) == str(us) else 'them'
        thr = {}; team = {}
        rows = []
        with open(pre + '.actions.tsv') as f:
            hdr = f.readline().rstrip('\n').split('\t'); ix = {h: i for i, h in enumerate(hdr)}
            for line in f:
                c = line.rstrip('\n').split('\t'); c += [''] * (len(hdr) - len(c)); rows.append(c)
        for c in rows:
            a = c[ix['action']]; r = int(c[ix['round']])
            if a == 'throw':
                thr[c[ix['a']]] = (r, side(c[ix['team']]))
            elif a == 'land' and c[ix['a']] in thr:
                r0, s = thr[c[ix['a']]]
                victim = 'own' if side(c[ix['team']]) == s else 'enemy'
                T[s][f'{victim}: land {c[ix["b"]]} after {r - r0}'] += 1
                T[s][f'{victim}: land {c[ix["b"]]}'] += 1
            elif a == 'die' and c[ix['type']] == 'RAT' and c[ix['a']] in thr and c[ix['e']] in ('throwLand', 'throwHit'):
                r0, s = thr[c[ix['a']]]
                victim = 'own' if side(c[ix['team']]) == s else 'enemy'
                T[s][f'{victim}: died {c[ix["e"]]}'] += 1
        for c in rows:
            if c[ix['action']] == 'throw': T[side(c[ix['team']])]['throws'] += 1
for s in ('them', 'us'):
    X = T[s]
    print(f"{s}: throws {X['throws']}: " + ', '.join(f'{k} {v}' for k, v in sorted(X.items()) if k.startswith('enemy') and 'after' not in k))
    print('      enemy landings by flight rounds: ' + ', '.join(f"{k.split(': ')[1]} {v}" for k, v in sorted(X.items()) if k.startswith('enemy') and 'after' in k))
