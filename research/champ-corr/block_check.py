"""King-defence mechanism check in local games (sw63, sw64, sw65), both seats: each replay's two bots are named in its
file name (A-hash_vs_B-hash_MAP_sN.bc26: A is team 1, B team 2) and each bot is measured on its own team. Counts the
king's blocks (Debug.KWHY 'B', index 7 of the bytecode tag: sw63's newborn or sw65's dirt), dirt the king placed,
enemy throws hitting our king and babies (and the king's damage), enemy rat traps our king fired (sw64) and their damage.
Usage: python block_check.py REPLAY..."""
import os, re, sys, collections
sys.path.insert(0, '/home/terryvanbelle/projects/vibe/2026b/tools')
import game_report as gr

KNOWN = {'cheesespawn', 'pickup', 'transfer', 'squeak', 'nap', 'bite', 'throw', 'damage', 'land', 'stun', 'spawn', 'die',
         'trigger', 'throwHit', 'ratTrap', 'drop', 'dig', 'scratch', 'catTrap', 'release', 'swap', 'catSleep', 'feed',
         'upgradeKing'}
T = collections.defaultdict(collections.Counter)
for p in sys.argv[1:]:
    m = re.match(r'(.+?)-[0-9a-f]+_vs_(.+?)-[0-9a-f]+_', os.path.basename(p))
    names = [m.group(1), m.group(2)] if m else [os.path.basename(p).split('-')[0], '?']
    pre = os.path.join(gr.source(p), 'g0')
    for t in ('1', '2'):
        T[names[int(t) - 1]]['games'] += 1
    with open(pre + '.turns.tsv') as f:
        h = f.readline().rstrip('\n').split('\t'); jx = {x: i for i, x in enumerate(h)}
        for l in f:
            c = l.rstrip('\n').split('\t')
            if c[jx['src']] != 'own' or c[jx['type']] != 'KING' or not c[jx['bc']]: continue
            if (int(c[jx['bc']]) % 128) & 31 == 7: T[names[int(c[jx['team']]) - 1]]['king blocks (tag B)'] += 1
    with open(pre + '.actions.tsv') as f:
        h = f.readline().rstrip('\n').split('\t'); ix = {x: i for i, x in enumerate(h)}
        for l in f:
            c = l.rstrip('\n').split('\t'); c += [''] * (len(h) - len(c))
            a, team = c[ix['action']], c[ix['team']]
            if team not in ('1', '2'): continue
            X = T[names[int(team) - 1]]
            if a == 'trigger' and c[ix['type']] == 'KING' and c[ix['g']] == 'rat':
                X['enemy traps our king fired (sw64)'] += 1; X['  their damage to our king '] += int(c[ix['f']] or 0)
            if a == 'throwHit' and c[ix['d']] in ('1', '2') and c[ix['g']] in ('1', '2') and c[ix['d']] != c[ix['g']]:
                V = T[names[int(c[ix['d']]) - 1]]
                if c[ix['c']] == 'KING': V['enemy throws hitting our king'] += 1; V['  their damage to our king'] += int(c[ix['b']] or 0)
                elif c[ix['c']] == 'RAT': V['enemy throws hitting our babies'] += 1
            if c[ix['type']] == 'KING' and a not in KNOWN:
                X[f'king action {a}'] += 1
for b, X in T.items():
    print(f'== {b} ({X["games"]} games, its own team in each)')
    for k, v in X.items():
        if k != 'games': print(f'  {k}: {v} ({v / X["games"]:.1f}/game)')
