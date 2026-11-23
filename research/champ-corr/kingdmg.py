"""Damage to each king by cause over the 21-map study's replica games (vs AlexT101_usqualsbot), from actions.tsv
alone (fast split): bites, thrown rats (throwHit), rat traps triggered by the king, cats. Also the trap's age (rounds
from ratTrap placement to the king's trigger). Usage: python kingdmg.py"""
import json, os, sys, collections
sys.path.insert(0, '/home/terryvanbelle/projects/vibe/2026b/tools')
import game_report as gr
REPO = '/home/terryvanbelle/projects/vibe/2026b'
recs = json.load(open(f'{REPO}/research/champ-corr/full_maps.json'))
result = {(r['match'], r['map']): r['won'] for r in recs}
LOST = {'cheesefarm', 'evileye', 'starvation', 'DefaultSmall'}
tot = {k: collections.Counter() for k in ('won', 'lost', 'lost4')}
ng = collections.Counter()
age = collections.Counter()
seen = set()
for m in sorted({r['match'] for r in recs}):
    try:
        d = gr.source(os.path.expanduser(f'~/projects/vibe/bc26b-galaxy-replays/{m}'))
    except Exception:
        continue
    gi = 0
    while os.path.exists(os.path.join(d, f'g{gi}.meta.json')):
        pre = os.path.join(d, f'g{gi}')
        meta = json.load(open(pre + '.meta.json'))
        mp = meta.get('map'); gi += 1
        if (m, mp) not in result:
            continue
        us = 1 if str(meta.get('teamA', '')).startswith('vibe26b') else 2
        keys = ['won' if result[(m, mp)] else 'lost'] + (['lost4'] if mp in LOST else [])
        for k in keys: ng[k] += 1
        kings, placed = {}, {}
        with open(pre + '.actions.tsv') as f:
            hdr = f.readline().rstrip('\n').split('\t')
            ix = {h: i for i, h in enumerate(hdr)}
            for line in f:
                c = line.rstrip('\n').split('\t')
                c += [''] * (len(hdr) - len(c))
                a = c[ix['action']]
                if c[ix['type']] == 'KING':
                    kings[c[ix['id']]] = int(c[ix['team']])
                if a == 'ratTrap':
                    placed[c[ix['a']]] = int(c[ix['round']])
                    continue
                if a == 'bite' and c[ix['b']] == 'KING':
                    vt, dmg, cause = int(c[ix['c']]), int(c[ix['d']] or 0), 'bite'
                elif a == 'throwHit' and c[ix['c']] == 'KING':
                    vt, dmg = int(c[ix['d']]), int(c[ix['b']] or 0)
                    cause = 'thrown ' + ('own rat' if int(c[ix['team']]) == vt else 'their rat')
                elif a == 'trigger' and c[ix['id']] in kings and c[ix['g']] == 'rat':
                    vt, dmg, cause = kings[c[ix['id']]], int(c[ix['f']] or 0), 'trap'
                    if vt == us and keys[0] == 'lost':
                        age[min(int(c[ix['round']]) - placed.get(c[ix['a']], -99), 99)] += 1
                elif a == 'scratch':
                    continue
                else:
                    continue
                who = 'our king' if vt == us else 'their king'
                for k in keys: tot[k][(who, cause)] += dmg
for k in ('lost4', 'lost', 'won'):
    n = ng[k]
    print(f'== {k}: {n} games; king damage per game by cause')
    for who in ('our king', 'their king'):
        items = sorted(((cause, v) for (w, cause), v in tot[k].items() if w == who), key=lambda kv: -kv[1])
        print(f'  {who}: ' + ', '.join(f'{cause} {v / n:.0f}' for cause, v in items) + f'  (total {sum(v for _, v in items) / n:.0f})')
print('trap age (rounds from placing to our king triggering it, lost games):', sorted(age.items())[:20])
