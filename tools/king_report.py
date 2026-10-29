#!/usr/bin/env python3
"""How each king fared in every game of a replay (a .bc26, or a ReplayDump tsv directory).

  king_report.py REPLAY.bc26|TSVDIR [--game N] [--team NAME] [--json]

Per game and team: the king's death round and cause (or its final HP), HP lost to starvation and HP lost otherwise
(hits after its last record included), that HP by source (hpBy: bite, biteBoost, scratch, throw (enemy throwers),
ownThrow (its own team threw a rat into it), trap from the action rows, the killing blow included; other = what no
action explains), the hits on it, the round its team's cheese first ran out, the first round an enemy rat came
within dist2 20 of it, and per 25-round step until its death the enemy and friendly rats within dist2 20 of it and
its HP. Kings are never crushed or pounced (canPounce refuses a king's tile). --team keeps only that team's kings
(by name); --game N (0-based) one game.
Starvation, as ReplayDump books starveDmg: a king whose bank is below 2 at its end of turn loses exactly 10 HP
instead of eating 2; one that starves to death loses the HP it had left (after the hits since its last record). The
bank then is not in the replay: it is at least the round's bank minus the cheese the team received (transfer and
kingPickup rows) after the king's record, and at most the previous round's bank plus what it received up to that
record. A record that lost 10+ HP no action explains starved if that low bound is below 2 and either the high bound
is too or the king was not bitten since its last record; the rest of the loss is a bite's cheese boost (biteBoost)
if it was bitten, else other.
"""
import argparse, collections, json, os, sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import game_report  # noqa: E402

NEAR = 20
STEP = 25
BITE = 10        # GameConstants.RAT_BITE_DAMAGE (old dumps give no bite damage)
STARVE = 10      # GameConstants.RAT_KING_HEALTH_LOSS
EAT = 2          # GameConstants.RAT_KING_CHEESE_CONSUMPTION
NONE = 10 ** 9   # no bound on the bank
HIT = {'bite': 'bites', 'scratch': 'scratches', 'throw': 'throwHits', 'ownThrow': 'ownThrowHits', 'trap': 'traps'}


def analyse(d, g):
    pre = os.path.join(d, f'g{g}')
    meta = json.load(open(pre + '.meta.json'))
    s = game_report.summary(d, g)
    names = {1: meta.get('teamA'), 2: meta.get('teamB')}
    kings = {}                                   # id -> team
    pos = collections.defaultdict(dict)          # round -> id -> (team, type, x, y, hp)
    ev = collections.defaultdict(list)           # king id -> [((round, seq, 0 hit | 1 record), value)]
    need = lambda f, cols: game_report.rows(pre + f, game_report.need(d, g, cols))
    rat = {}                                     # id -> its latest RAT record (a forming rat's is its king's first)
    for r in game_report.own(need('.turns.tsv', 'round id team type x y hp seq src dead')):
        t = int(r['team'])
        if t not in (1, 2) or r.get('dead') == '1':
            continue
        rid, rn = int(r['id']), int(r['round'])
        e = ((rn, int(r.get('seq') or 0), 1), int(r['hp']))
        if r['type'] == 'KING':
            if rid not in kings and rid in rat:
                ev[rid].append(rat.pop(rid))
            kings.setdefault(rid, t)
            ev[rid].append(e)
        elif r['type'] == 'RAT':
            rat[rid] = e
        pos[rn][rid] = (t, r['type'], int(r['x']), int(r['y']), int(r['hp']))
    died = {}
    income = collections.defaultdict(list)       # (round, team) -> [(seq, cheese)] its bank received
    for r in need('.actions.tsv', 'round team action a b seq d e f g'):
        act, rn = r['action'], int(r['round'])
        seq = int(r['seq']) if r.get('seq') else -1
        if act == 'bite':
            v, cause, amt = r['a'], 'bite', r.get('d') or BITE
        elif act == 'scratch':
            v, cause, amt = r.get('d'), 'scratch', r.get('f')
        elif act == 'throwHit':          # g: the thrower's team
            v, amt = r['a'], r['b']
            cause = 'ownThrow' if v and r.get('g') == str(kings.get(int(v))) else 'throw'
        elif act == 'trigger':
            v, cause, amt = r.get('e'), 'trap', r.get('f')
        elif act == 'die' and r['a'] and int(r['a']) in kings:
            died[int(r['a'])] = (rn, r.get('e') or r['b'])
            continue
        elif act in ('transfer', 'kingPickup') and r['a']:   # a: the receiving king
            income[(rn, kings.get(int(r['a']), int(r['team'])))].append((seq, int(r['b'] or 0)))
            continue
        else:
            continue
        if v and int(v) in kings:
            # an action belongs to the record of its seq and comes before that record's state (old dumps: no seq)
            ev[int(v)].append(((rn, seq, 0), (cause, int(amt or 0))))
    cheese = {}                                  # round -> {team: global cheese}
    for r in need('.rounds.tsv', 'round bankA bankB numKingsA numKingsB'):
        cheese[int(r['round'])] = {1: game_report.bank(r, 'A')[0], 2: game_report.bank(r, 'B')[0]}

    def starving(rn, seq, t, bitten):
        """Whether a king record of team t that lost 10+ hp no action explains starved (see the module doc)."""
        inc = income[(rn, t)]
        upto = sum(c for q, c in inc if q <= seq)
        lo = cheese[rn][t] - (sum(c for _, c in inc) - upto) if rn in cheese else NONE
        hi = cheese[rn - 1][t] + upto if rn - 1 in cheese else NONE
        return lo < EAT and (hi < EAT or not bitten)

    rounds = max(pos) if pos else 0
    out = []
    for k, t in sorted(kings.items()):
        hp = last = None
        starve = other = 0
        by, cause, starved = collections.Counter(), collections.Counter(), []
        bitten = False
        for (rn, seq, kind), v in sorted(ev[k], key=lambda e: e[0]):
            if kind == 0:
                if last is None:             # before its first record as a king (a formed king's forming turn)
                    continue
                by[v[0]] += v[1]
                cause[HIT[v[0]]] += 1
                hp -= v[1]
                bitten |= v[0] == 'bite'
                continue
            if last is None:
                hp = last = v
                continue
            rest = max(0, hp) - v            # hp lost with no action this record: starvation, a bite's cheese boost
            st = STARVE if rest >= STARVE and starving(rn, seq, t, bitten) else 0
            starve += st
            if st:
                starved.append(rn)
            other += max(0, last - v - st)
            if rest - st:
                by['biteBoost' if bitten and rest > st else 'other'] += rest - st
            hp = last = v
            bitten = False
        if last is not None:
            other += last - max(0, hp)       # the hits after its last record
            if died.get(k, (0, ''))[1] == 'starve':
                starve += max(0, hp)         # starved to death: no action, the hp it had left
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
