#!/usr/bin/env python3
"""Aggregate kill.py lines: agg.py kill.jsonl  -> per-game table and pooled numbers (markdown on stdout)."""
import json, statistics as st, sys

rows = [json.loads(l) for l in open(sys.argv[1]) if l.startswith('{')]


def at(tl, D, back):
    c = [r for r in tl if r['r'] <= D - back]
    return c[-1] if c else tl[0]


def med(v):
    return round(st.median(v), 1) if v else '-'


def mean(v):
    return round(sum(v) / len(v), 1) if v else '-'


SRC = ['throwHit_ourRat', 'throwHit_theirRat', 'biteRat', 'biteKing', 'trap', 'cat', 'starve~']
print('| match g | map | opp | died r | cause | 1st near | 1st dmg | dmg in last 100: thrown-our-rat / thrown-their-rat / bite / trap / cat / starve | '
      'nE @-100/-50/-20/-5 | nU @-100/-50/-20/-5 | their grabs (near) / throws of our rats (near) | our grabs / throws | '
      'spawns them/us | bank them/us @-100 | @death | babies them/us @-50 | king gap @death (their king moved / ours) |')
print('|' + '---|' * 15)
pool = {k: 0 for k in SRC}
poolGame = {k: 0 for k in SRC}
agg = dict(nE=[[], [], [], []], nU=[[], [], [], []], ages=[], fresh=0, staged=0, travel=0, natt=[], thD=[], lag=[], napD=[],
           grabsN=[], throwsN=[], ourGrabs=[], ourThrows=[], spT=[], spU=[], bankT0=[], bankU0=[], bankTD=[], bankUD=[], ekd=[],
           ekmv=[], kmv=[], first=[], fdmg=[], death=[], ourDeathsNear={}, spawnD=[])
for r in rows:
    D = r['death']
    tl = r['tl']
    pts = [at(tl, D, b) for b in (100, 50, 20, 5)]
    for i, p in enumerate(pts):
        agg['nE'][i].append(p['nE'])
        agg['nU'][i].append(p['nU'])
    dw = r['dmgW']
    for k in SRC:
        pool[k] += dw.get(k, 0)
        poolGame[k] += r['dmg'].get(k, 0)
    for a in r['attackers']:
        agg['ages'].append(a['age'])
        b20 = a['back'].get('20')
        if b20 is None:
            agg['fresh'] += 1
        elif b20[0] <= 12:
            agg['staged'] += 1
        else:
            agg['travel'] += 1
        if a.get('spawnD') is not None:
            agg['spawnD'].append(a['spawnD'])
    agg['natt'].append(len(r['attackers']))
    agg['thD'] += r['throwHitD']
    agg['lag'] += r['napLag']
    agg['napD'] += r['napD']
    g, t = r['grabsW'], r['throwsW']
    gn = g.get('them_near', 0)
    tn = t.get('them_ourRat_near', 0)
    agg['grabsN'].append(gn)
    agg['throwsN'].append(tn)
    agg['ourGrabs'].append(g.get('us', 0))
    agg['ourThrows'].append(t.get('us_theirRat', 0))
    agg['spT'].append(r['spawnsW'].get('them', 0))
    agg['spU'].append(r['spawnsW'].get('us', 0))
    agg['bankT0'].append(tl[0]['bankT'])
    agg['bankU0'].append(tl[0]['bankU'])
    agg['bankTD'].append(tl[-1]['bankT'])
    agg['bankUD'].append(tl[-1]['bankU'])
    agg['ekd'].append(tl[-1]['ekd'] or 0)
    agg['ekmv'].append(tl[-1]['ekmv'] or 0)
    agg['kmv'].append(r['kmv'] or 0)
    agg['first'].append(r['firstNear'] or D)
    agg['fdmg'].append(r['firstDmg'] or D)
    agg['death'].append(D)
    for k, v in r['ourDeathsW'].items():
        agg['ourDeathsNear'][k] = agg['ourDeathsNear'].get(k, 0) + v
    b50 = at(tl, D, 50)
    cause = r['cause']
    print(f"| {r['match']} g{r['game']} | {r['map']} {r['W']}x{r['H']} | {r['opp'].split('_')[0][:8]}_{r['opp'].split('_')[-1][:10]} | {D} | {cause} | "
          f"{r['firstNear']} | {r['firstDmg']} | "
          f"{dw.get('throwHit_ourRat', 0)} / {dw.get('throwHit_theirRat', 0)} / {dw.get('biteRat', 0) + dw.get('biteKing', 0)} / {dw.get('trap', 0)} / {dw.get('cat', 0)} / {dw.get('starve~', 0)} | "
          f"{'/'.join(str(p['nE']) for p in pts)} | {'/'.join(str(p['nU']) for p in pts)} | "
          f"{g.get('them', 0) + gn} ({gn}) / {t.get('them_ourRat', 0) + tn} ({tn}) | {g.get('us', 0)} / {t.get('us_theirRat', 0)} | "
          f"{r['spawnsW'].get('them', 0)}/{r['spawnsW'].get('us', 0)} | {tl[0]['bankT']}/{tl[0]['bankU']} | {tl[-1]['bankT']}/{tl[-1]['bankU']} | "
          f"{b50['babT']}/{b50['babU']} | {tl[-1]['ekd']} ({tl[-1]['ekmv']} / {r['kmv']}) |")
n = len(rows)
tot = sum(pool.values())
print(f'\nGames: {n}; death round median {med(agg["death"])} (range {min(agg["death"])}-{max(agg["death"])})')
print('Damage to our king in the last 100 rounds, pooled: ' + ', '.join(f'{k} {pool[k]} ({100 * pool[k] // max(tot, 1)}%)' for k in SRC))
totg = sum(poolGame.values())
print('Whole game (no starvation term): ' + ', '.join(f'{k} {poolGame[k]} ({100 * poolGame[k] // max(totg, 1)}%)' for k in SRC if k != 'starve~'))
print('Enemy rats within 5 @-100/-50/-20/-5 (mean): ' + ' / '.join(str(mean(v)) for v in agg['nE']))
print('Our rats within 5 @-100/-50/-20/-5 (mean): ' + ' / '.join(str(mean(v)) for v in agg['nU']))
na = len(agg['ages'])
print(f'Attackers per game {mean(agg["natt"])}; age at arrival median {med(agg["ages"])}; fresh (<20 r old) {agg["fresh"]}/{na}, '
      f'staged (within 12 of our king 20 r before) {agg["staged"]}/{na}, travelling {agg["travel"]}/{na}; spawn tile to our king median {med(agg["spawnD"])}')
print(f'King-hitting throws: thrower distance from our king centre median {med(agg["thD"])} (n={len(agg["thD"])}); '
      f'grab->hit lag median {med(agg["lag"])} (n={len(agg["lag"])}); victim grabbed at distance median {med(agg["napD"])}')
print(f'Their grabs of our rats near our king / game {mean(agg["grabsN"])}, their throws of our rats near our king / game {mean(agg["throwsN"])}; '
      f'ours (grabs/throws anywhere) {mean(agg["ourGrabs"])} / {mean(agg["ourThrows"])}')
print(f'Spawns in window them/us {mean(agg["spT"])} / {mean(agg["spU"])}; bank @-100 them/us {med(agg["bankT0"])} / {med(agg["bankU0"])}; '
      f'@death {med(agg["bankTD"])} / {med(agg["bankUD"])}')
print(f'Enemy king distance from our king @death median {med(agg["ekd"])}; their king displaced from start {med(agg["ekmv"])}; ours {med(agg["kmv"])}')
print(f'First enemy within 5 of our king median r{med(agg["first"])}; first king damage median r{med(agg["fdmg"])}')
print('Our rat deaths in window (near = within 8 of our king):', dict(sorted(agg['ourDeathsNear'].items(), key=lambda kv: -kv[1])))
