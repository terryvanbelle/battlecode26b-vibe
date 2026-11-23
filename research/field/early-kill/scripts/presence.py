#!/usr/bin/env python3
"""Rats within 5 of the ENEMY king, per 50-round bucket, both directions: presence.py kill.jsonl"""
import json, os, sys, collections
sys.path.insert(0, os.path.expanduser('~/projects/vibe/2026b/tools'))
import game_report as GR
rows = [json.loads(l) for l in open(sys.argv[1])]
B = 50
tot = {1: collections.defaultdict(list), 2: collections.defaultdict(list)}   # 1: ours at theirs, 2: theirs at ours
for r in rows:
    d = GR.dump(os.path.expanduser(f"~/projects/vibe/bc26b-galaxy-replays/{r['match']}/replay.bc26"))
    us = 1 if r['us'] == 'A' else 2
    byr = collections.defaultdict(list)
    for t in GR.rows(f"{d}/g{r['game']}.turns.tsv"):
        if t['src'] == 'own' and t['dead'] != '1':
            byr[int(t['round'])].append((int(t['team']), t['type'], int(t['x']), int(t['y'])))
    per = {1: collections.defaultdict(list), 2: collections.defaultdict(list)}
    for rr, ts in byr.items():
        for side, (ra, kt) in ((1, (us, 3 - us)), (2, (3 - us, us))):
            ks = [(x, y) for tm, ty, x, y in ts if ty == 'KING' and tm == kt]
            if not ks:
                continue
            n = sum(1 for tm, ty, x, y in ts if ty == 'RAT' and tm == ra and min((x - a) ** 2 + (y - b) ** 2 for a, b in ks) <= 25)
            per[side][rr // B].append(n)
    line = []
    for side in (1, 2):
        for b, v in per[side].items():
            tot[side][b].append(sum(v) / len(v))
    print(r['match'], r['game'], r['map'], 'ours@theirs', [round(sum(per[1][b]) / len(per[1][b]), 1) for b in sorted(per[1])],
          'theirs@ours', [round(sum(per[2][b]) / len(per[2][b]), 1) for b in sorted(per[2])])
for side, name in ((1, 'ours at their king'), (2, 'theirs at our king')):
    print(name, ' '.join(f'r{b * B}-{b * B + B - 1}:{sum(v) / len(v):.2f}(n{len(v)})' for b, v in sorted(tot[side].items())))
