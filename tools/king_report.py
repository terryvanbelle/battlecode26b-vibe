#!/usr/bin/env python3
"""How each king fared in every game of a replay (a .bc26, or a ReplayDump tsv directory).

  king_report.py REPLAY.bc26|TSVDIR [--team NAME] [--json]

Per game and team: the king's death round (or its final HP), HP lost while its team had no cheese (starving: the
king eats 2 a round or loses 10 HP) and HP lost otherwise, bites and damage actions on it, the round its team's
cheese first ran out, the first round an enemy rat came within dist2 20 of it, and per 25-round step until its death
the enemy and friendly rats within dist2 20 of it and its HP. --team keeps only that team's kings (by name).
"""
import argparse, collections, json, os, shutil, sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import game_report  # noqa: E402

NEAR = 20
STEP = 25


def analyse(d, g):
    pre = os.path.join(d, f'g{g}')
    meta = json.load(open(pre + '.meta.json'))
    names = {1: meta.get('teamA'), 2: meta.get('teamB')}
    kings = {}                                   # id -> team
    pos = collections.defaultdict(dict)          # round -> id -> (team, type, x, y, hp)
    for r in game_report.rows(pre + '.turns.tsv'):
        t = int(r['team'])
        if t not in (1, 2):
            continue
        rid, rn = int(r['id']), int(r['round'])
        if r['type'] == 'KING':
            kings.setdefault(rid, t)
        pos[rn][rid] = (t, r['type'], int(r['x']), int(r['y']), int(r['hp']))
    cause = {k: collections.Counter() for k in kings}
    died = {}
    for r in game_report.rows(pre + '.actions.tsv'):
        act = r['action']
        if act == 'bite' and r['a'] and int(r['a']) in kings:
            cause[int(r['a'])]['bites'] += 1
        elif act == 'damage' and r['a'] and int(r['a']) in kings:
            cause[int(r['a'])]['damage:' + r['type']] += int(r['b'] or 0)
        elif act == 'die' and r['a'] and int(r['a']) in kings:
            died[int(r['a'])] = int(r['round'])
    cheese = {}                                  # round -> {team: global cheese} (kings column = 10 * cheese + kings)
    for r in game_report.rows(pre + '.rounds.tsv'):
        cheese[int(r['round'])] = {1: int(r['kingsA'] or 0) // 10, 2: int(r['kingsB'] or 0) // 10}
    rounds = max(pos) if pos else 0
    out = []
    for k, t in sorted(kings.items()):
        hp0 = last = None
        first = broke = None
        starve = other = 0
        steps = []
        for rn in range(1, rounds + 1):
            me = pos[rn].get(k)
            if me is None:
                continue
            _, _, x, y, hp = me
            hp0 = hp if hp0 is None else hp0
            c = cheese.get(rn, {}).get(t, 1)
            if broke is None and c < 2:
                broke = rn
            if last is not None and hp < last:
                if c < 2 or cheese.get(rn - 1, {}).get(t, 1) < 2:
                    starve += last - hp
                else:
                    other += last - hp
            last = hp
            foes = mates = 0
            for rid, (tt, ty, xx, yy, _) in pos[rn].items():
                if rid == k or ty != 'RAT' or (xx - x) ** 2 + (yy - y) ** 2 > NEAR:
                    continue
                if tt == t:
                    mates += 1
                else:
                    foes += 1
            if foes and first is None:
                first = rn
            if rn % STEP == 0 or rn == died.get(k, -1) - 1:
                steps.append((rn, foes, mates, hp))
        out.append({'game': g, 'map': meta.get('map'), 'team': names[t], 'side': 'AB'[t - 1], 'king': k,
                    'died': died.get(k), 'hpEnd': last, 'starveHp': starve, 'otherHp': other, 'broke': broke,
                    'firstFoe': first, 'cause': dict(cause[k]), 'steps': steps})
    return out


def render(rows):
    lines = []
    for o in rows:
        end = f"died r{o['died']}" if o['died'] else f"alive hp {o['hpEnd']}"
        lines.append(f"g{o['game']} {o['map']} {o['side']} {o['team']} king {o['king']}: {end}; HP lost starving "
                     f"{o['starveHp']}, otherwise {o['otherHp']}; cheese out r{o['broke']}; first enemy rat near "
                     f"r{o['firstFoe']}; {o['cause']}")
        lines.append('   ' + ' '.join(f"r{rn}:{f}v{m}@{hp}" for rn, f, m, hp in o['steps'] if f or m or rn % 100 == 0))
    return '\n'.join(lines)


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument('src')
    ap.add_argument('--team', default=None)
    ap.add_argument('--json', action='store_true')
    a = ap.parse_args(argv)
    d = game_report.dump(a.src) if a.src.endswith('.bc26') else a.src
    rows = []
    g = 0
    while os.path.exists(os.path.join(d, f'g{g}.meta.json')):
        rows += [o for o in analyse(d, g) if a.team is None or o['team'] == a.team]
        g += 1
    if d != a.src:
        shutil.rmtree(d, ignore_errors=True)
    print(json.dumps(rows) if a.json else render(rows))


if __name__ == '__main__':
    main()
