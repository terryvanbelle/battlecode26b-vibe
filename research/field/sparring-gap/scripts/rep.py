#!/usr/bin/env python3
"""Replica side: per (match, game) both teams' ReplayDump summaries + babies/bank timelines. Writes rep.jsonl."""
import json, os, sys
REPO = os.path.expanduser('~/projects/vibe/2026b')
sys.path.insert(0, os.path.join(REPO, 'tools'))
import game_report as GR
R = os.path.expanduser('~/projects/vibe/bc26b-galaxy-replays')
AT = (50, 100, 150, 200, 300, 400, 500, 750, 1000, 1500, 2000)
out = open('rep.jsonl', 'w')
for mid in sys.argv[1:]:
    d = GR.dump(os.path.join(R, mid, 'replay.bc26'))
    for g in GR.games(d):
        s = GR.summary(d, g)
        us = 'A' if s['teamA'] == 'vibe26b' else 'B'
        th = 'B' if us == 'A' else 'A'
        tl = {}
        for r in GR.rows(os.path.join(d, f'g{g}.rounds.tsv')):
            rn = int(r['round'])
            if rn in AT:
                tl[rn] = {'babUs': int(r['babies' + us]), 'babThem': int(r['babies' + th]),
                          'bankUs': GR.bank(r, us)[0], 'bankThem': GR.bank(r, th)[0]}
        out.write(json.dumps({'src': 'replica', 'match': mid, 'game': g, 'map': s.get('map'), 'opp': s['team' + th],
                              'won': s.get('winner') == us, 'winType': s.get('winType'), 'rounds': s.get('rounds'),
                              'backstab': s.get('backstabRound'), 'bsBy': s.get('backstabBy'),
                              'us': s.get(us), 'them': s.get(th), 'top': {k: v for k, v in s.items() if k not in ('A', 'B')},
                              'side': us, 'tl': tl}) + '\n')
    print(mid, 'ok', file=sys.stderr)
