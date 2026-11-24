"""sw63 mechanism check in local games (team 1 = the first bot): the king's blocks (Debug.KWHY 'B', index 7 of the
bytecode tag), enemy throws hitting our king and their damage. Usage: python block_check.py REPLAY..."""
import json, os, sys, collections
sys.path.insert(0, '/home/terryvanbelle/projects/vibe/2026b/tools')
import game_report as gr
T = collections.defaultdict(collections.Counter)
for p in sys.argv[1:]:
    bot = os.path.basename(p).split('-')[0]
    pre = os.path.join(gr.source(p), 'g0'); X = T[bot]; X['games'] += 1
    with open(pre + '.turns.tsv') as f:
        h = f.readline().rstrip('\n').split('\t'); jx = {x: i for i, x in enumerate(h)}
        for l in f:
            c = l.rstrip('\n').split('\t')
            if c[jx['src']] != 'own' or c[jx['type']] != 'KING' or c[jx['team']] != '1' or not c[jx['bc']]: continue
            if (int(c[jx['bc']]) % 128) & 31 == 7: X['king blocks (tag B)'] += 1
    with open(pre + '.actions.tsv') as f:
        h = f.readline().rstrip('\n').split('\t'); ix = {x: i for i, x in enumerate(h)}
        for l in f:
            c = l.rstrip('\n').split('\t'); c += [''] * (len(h) - len(c))
            if c[ix['action']] == 'throwHit' and c[ix['d']] == '1' and c[ix['g']] == '2':
                if c[ix['c']] == 'KING': X['enemy throws hitting our king'] += 1; X['  their damage to our king'] += int(c[ix['b']] or 0)
                elif c[ix['c']] == 'RAT': X['enemy throws hitting our babies'] += 1
for b, X in T.items():
    print(f'== {b} (team 1, {X["games"]} games)')
    for k, v in X.items():
        if k != 'games': print(f'  {k}: {v} ({v / X["games"]:.1f}/game)')
