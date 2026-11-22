#!/usr/bin/env python3
"""King fates per match (king_report library) -> kd.jsonl: one line per king, with its team and opponent."""
import json, os, sys

REPO = os.path.expanduser('~/projects/vibe/2026b')
sys.path.insert(0, os.path.join(REPO, 'tools'))
import game_report as GR  # noqa: E402
import king_report as KR  # noqa: E402

R = os.path.expanduser('~/projects/vibe/bc26b-galaxy-replays')
out = open(sys.argv[1], 'a')
for mid in sys.argv[2:]:
    d = GR.dump(os.path.join(R, mid, 'replay.bc26'))
    gj = json.load(open(os.path.join(d, 'game.json')))
    for g in range(gj['games']):
        try:
            recs = KR.analyse(d, g)
        except Exception as e:
            print(mid, g, e, file=sys.stderr)
            continue
        for k in recs:
                opp = gj['teamB'] if k['team'] == gj['teamA'] else gj['teamA']
                k.pop('steps', None); k.pop('starved', None)
                k.update(match=int(mid), opp=opp)
                out.write(json.dumps(k) + '\n')
    out.flush()
