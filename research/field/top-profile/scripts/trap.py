#!/usr/bin/env python3
"""Rat-trap lifecycle per team: placed, fired, life (rounds), trigger mode (victim walked in vs landed thrown/dropped),
damage of the trigger, where placed (own half / enemy half), and whether the victim was then grabbed within 3 rounds.
  trap.py MATCH_ID ...   prints one line per team-name aggregate"""
import csv, json, math, os, sys, collections, statistics as st

REPO = os.path.expanduser('~/projects/vibe/2026b')
sys.path.insert(0, os.path.join(REPO, 'tools'))
import game_report as GR  # noqa: E402

csv.field_size_limit(10 ** 8)
R = os.path.expanduser('~/projects/vibe/bc26b-galaxy-replays')
agg = collections.defaultdict(lambda: collections.Counter())
lives = collections.defaultdict(list)


def tsv(p):
    with open(p, newline='') as f:
        yield from csv.DictReader(f, delimiter='\t')


for mid in sys.argv[1:]:
    d = GR.dump(os.path.join(R, mid, 'replay.bc26'))
    gj = json.load(open(os.path.join(d, 'game.json')))
    for g in range(gj['games']):
        s = json.load(open(os.path.join(d, f'g{g}.summary.json')))
        names = {1: s['teamA'], 2: s['teamB']}
        start = {}
        for r in tsv(os.path.join(d, f'g{g}.turns.tsv')):
            if r['type'] == 'KING' and r['team'] in ('1', '2'):
                start.setdefault(int(r['team']), (int(r['x']), int(r['y'])))
            if len(start) == 2:
                break
        live = {}       # (owner, pos) -> placed round
        naps = collections.defaultdict(list)   # victim id -> rounds grabbed
        trig = []
        for a in tsv(os.path.join(d, f'g{g}.actions.tsv')):
            k = a['action']
            rr = int(a['round'])
            if k == 'ratTrap':
                t = int(a['team'])
                live[(t, a['a'])] = rr
                agg[names[t]]['placed'] += 1
                x, y = map(int, a['a'].split(','))
                if len(start) == 2 and math.dist((x, y), start[3 - t]) < math.dist((x, y), start[t]):
                    agg[names[t]]['placedEnemyHalf'] += 1
                if len(start) == 2 and math.dist((x, y), start[t]) <= 8:
                    agg[names[t]]['placedNearOwnKing'] += 1
            elif k == 'trigger' and a.get('g') == 'rat':
                owner = int(a['d']) if a['d'].isdigit() else None
                if owner not in (1, 2):
                    continue
                key = (owner, a['a'])
                p = live.pop(key, None)
                agg[names[owner]]['fired'] += 1
                if a['src'] != 'own':
                    agg[names[owner]]['firedOnThrown'] += 1
                if p is not None:
                    lives[names[owner]].append(rr - p)
                trig.append((owner, int(a['e']) if a['e'].isdigit() else None, rr))
            elif k == 'nap' and a.get('d') == 'enemy':
                naps[a['a']].append(rr)
        for owner, vid, rr in trig:
            if vid is not None and any(rr <= x <= rr + 3 for x in naps.get(str(vid), [])):
                agg[names[owner]]['victimGrabbed3'] += 1
        for t in (1, 2):
            agg[names[t]]['games'] += 1

print(f"{'team':32} games placed/g fired% firedOnThrown% lifeMed life<=2% enemyHalf% nearOwnKing% victimGrabbed<=3r%")
for t, c in sorted(agg.items(), key=lambda x: -x[1]['placed'] / max(1, x[1]['games'])):
    n = c['games']
    lv = lives[t]
    print(f"{t:32} {n:5} {c['placed'] / n:8.1f} {100 * c['fired'] / max(1, c['placed']):6.0f} "
          f"{100 * c['firedOnThrown'] / max(1, c['fired']):14.0f} {st.median(lv) if lv else '-':>7} "
          f"{100 * sum(1 for x in lv if x <= 2) / max(1, len(lv)):8.0f} {100 * c['placedEnemyHalf'] / max(1, c['placed']):10.0f} "
          f"{100 * c['placedNearOwnKing'] / max(1, c['placed']):12.0f} {100 * c['victimGrabbed3'] / max(1, c['fired']):16.0f}")
