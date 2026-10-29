#!/usr/bin/env python3
"""How each king fared in every game of a replay (a .bc26, or a ReplayDump tsv directory).

  king_report.py REPLAY.bc26|TSVDIR [--game N] [--team NAME] [--json]

Per game and team: the king's death round and cause (or its final HP), HP lost to starvation (a king with a bank
below 2 at its turn loses exactly 10 HP instead of eating 2) and HP lost otherwise between its records, that HP by
source (hpBy: bite, biteBoost, scratch, throw, trap from the action rows, the killing blow included; other = what
no action explains), the hits on it, the round its team's cheese first ran out, the first round an enemy rat came
within dist2 20 of it, and per 25-round step until its death the enemy and friendly rats within dist2 20 of it and
its HP. Kings are never crushed or pounced (canPounce refuses a king's tile). --team keeps only that team's kings
(by name); --game N (0-based) one game.
"""
import argparse, collections, json, os, sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import game_report  # noqa: E402

NEAR = 20
STEP = 25
BITE = 10        # GameConstants.RAT_BITE_DAMAGE (old dumps give no bite damage)
STARVE = 10      # GameConstants.RAT_KING_HEALTH_LOSS
HIT = {'bite': 'bites', 'scratch': 'scratches', 'throw': 'throwHits', 'trap': 'traps'}


def analyse(d, g):
    pre = os.path.join(d, f'g{g}')
    meta = json.load(open(pre + '.meta.json'))
    s = game_report.summary(d, g)
    names = {1: meta.get('teamA'), 2: meta.get('teamB')}
    kings = {}                                   # id -> team
    pos = collections.defaultdict(dict)          # round -> id -> (team, type, x, y, hp)
    ev = collections.defaultdict(list)           # king id -> [((round, seq, 0 hit | 1 record), value)]
    for r in game_report.own(game_report.rows(pre + '.turns.tsv')):
        t = int(r['team'])
        if t not in (1, 2) or r.get('dead') == '1':
            continue
        rid, rn = int(r['id']), int(r['round'])
        if r['type'] == 'KING':
            kings.setdefault(rid, t)
            ev[rid].append(((rn, int(r.get('seq') or 0), 1), int(r['hp'])))
        pos[rn][rid] = (t, r['type'], int(r['x']), int(r['y']), int(r['hp']))
    died = {}
    for r in game_report.rows(pre + '.actions.tsv'):
        act, rn = r['action'], int(r['round'])
        if act == 'bite':
            v, cause, amt = r['a'], 'bite', r.get('d') or BITE
        elif act == 'scratch':
            v, cause, amt = r.get('d'), 'scratch', r.get('f')
        elif act == 'throwHit':
            v, cause, amt = r['a'], 'throw', r['b']
        elif act == 'trigger':
            v, cause, amt = r.get('e'), 'trap', r.get('f')
        elif act == 'die' and r['a'] and int(r['a']) in kings:
            died[int(r['a'])] = (rn, r.get('e') or r['b'])
            continue
        else:
            continue
        if v and int(v) in kings:
            # an action belongs to the record of its seq and comes before that record's state (old dumps: no seq)
            ev[int(v)].append(((rn, int(r['seq']) if r.get('seq') else -1, 0), (cause, int(amt or 0))))
    cheese = {}                                  # round -> {team: global cheese}
    for r in game_report.rows(pre + '.rounds.tsv'):
        cheese[int(r['round'])] = {1: game_report.bank(r, 'A')[0], 2: game_report.bank(r, 'B')[0]}
    rounds = max(pos) if pos else 0
    out = []
    for k, t in sorted(kings.items()):
        hp = last = None
        starve = other = 0
        by, cause, starved = collections.Counter(), collections.Counter(), []
        bitten = False
        for (rn, _, kind), v in sorted(ev[k], key=lambda e: e[0]):
            if kind == 0:
                if last is None:             # before its first king record (a rat that formed later)
                    continue
                by[v[0]] += v[1]
                cause[HIT[v[0]]] += 1
                hp -= v[1]
                bitten |= v[0] == 'bite'
                continue
            if last is None:
                hp = last = v
                continue
            rest = hp - v                    # hp lost with no action this record: starvation, a bite's cheese boost
            st = STARVE if rest >= STARVE and min(cheese.get(rn - 1, {}).get(t, 2), cheese.get(rn, {}).get(t, 2)) < 2 else 0
            starve += st
            if st:
                starved.append(rn)
            other += max(0, last - v - st)
            if rest - st:
                by['biteBoost' if bitten and rest > st else 'other'] += rest - st
            hp = last = v
            bitten = False
        if died.get(k, (0, ''))[1] == 'starve' and last:
            starve += last                   # starved to death: no action, its last hp
        first = broke = None
        steps = []
        for rn in range(1, rounds + 1):
            me = pos[rn].get(k)
            if me is None or me[1] != 'KING':
                continue
            _, _, x, y, khp = me
            if broke is None and cheese.get(rn, {}).get(t, 1) < 2:
                broke = rn
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
            if rn % STEP == 0 or rn == died.get(k, (-1,))[0] - 1:
                steps.append((rn, foes, mates, khp))
        dr, dc = died.get(k, (None, None))
        out.append({'game': g, 'map': meta.get('map'), 'team': names[t], 'side': 'AB'[t - 1], 'king': k,
                    'died': dr, 'deathCause': dc, 'hpEnd': last, 'starveHp': starve, 'otherHp': other,
                    'hpBy': dict(by), 'broke': broke, 'firstFoe': first, 'cause': dict(cause), 'starved': starved, 'steps': steps,
                    'winner': s.get('winner'), 'winType': s.get('winType')})
    return out


def render(rows):
    lines = []
    game = None
    for o in rows:
        if o['game'] != game:
            game = o['game']
            lines.append(f"game {game} {o['map']}: winner {o.get('winner')} {o.get('winType')}")
        end = f"died r{o['died']} ({o.get('deathCause')})" if o['died'] else f"alive hp {o['hpEnd']}"
        lines.append(f"g{o['game']} {o['map']} {o['side']} {o['team']} king {o['king']}: {end}; HP lost starving "
                     f"{o['starveHp']}, otherwise {o['otherHp']} (by {o.get('hpBy', {})}); cheese out r{o['broke']}; "
                     f"first enemy rat near r{o['firstFoe']}; hits {o['cause']}")
        lines.append('   ' + ' '.join(f"r{rn}:{f}v{m}@{hp}" for rn, f, m, hp in o['steps'] if f or m or rn % 100 == 0))
    return '\n'.join(lines)


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument('src')
    ap.add_argument('--team', default=None)
    ap.add_argument('--game', type=int, default=None, help='0-based game index (match.json game = N+1); default all')
    ap.add_argument('--json', action='store_true')
    a = ap.parse_args(argv)
    d = game_report.source(a.src)
    rows = []
    for g in game_report.pick(d, a.game):
        rows += [o for o in analyse(d, g) if a.team is None or o['team'] == a.team]
    print(json.dumps(rows) if a.json else render(rows))


if __name__ == '__main__':
    main()
