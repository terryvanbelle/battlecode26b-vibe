#!/usr/bin/env python3
"""Stuck babies: travel decisions that go nowhere, read from a tagged build's bytecode-count tag (tools/smuggle.py).

  stuck_report.py REPLAY.bc26|TSVDIR... [--game N] [--team NAME] [--span R] [--top K] [--json]

An episode is a baby of a tagged team whose decoded decision is a travel code (TRAVEL: d deliver, M to a mine,
C to cheese) on every usable turn (smuggle.usable) for at least --span rounds (default 30), while it stays within
Chebyshev distance 1 of where the episode began; any other code, or a step out of that 3x3 box, ends it. Per game
and tagged team: the episodes, the rounds they cover (lost), that as a share of the team's usable baby turns, the
lost rounds by code, and the --top longest episodes: id, first-last round, box centre, codes by count, most cheese
carried, and the Chebyshev distance from the box centre to the nearest own king centre when it began (2 = touching
the king's 3x3 body; '-' = no king record that round). With several sources or games a total line per team name
follows. --team keeps one team (by name); --game N (0-based) one game.
"""
import argparse, collections, json, os, sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import game_report as GR, smuggle as SG  # noqa: E402

TRAVEL = set('dMC')
SPAN = 30
COLS = 'round id team type x y bc cheese src dead'


def cheb(a, b):
    return max(abs(a[0] - b[0]), abs(a[1] - b[1]))


def episodes(turn_rows, team, span=SPAN):
    """(episodes, usable baby turns) of team '1'/'2'. An episode: {id, start, end, rounds, x, y, codes (code ->
    turns), cheese (most carried), king (Chebyshev distance to the nearest own king centre at start, or None)}."""
    rr = SG.robot_rounds(turn_rows)
    kings = collections.defaultdict(list)        # round -> own king centres
    for r in rr:
        if r['team'] == team and r['type'] == 'KING' and r.get('dead') != '1':
            kings[int(r['round'])].append((int(r['x']), int(r['y'])))
    track = collections.defaultdict(list)        # id -> [(round, x, y, code, cheese)] in round order
    turns = 0
    for r in SG.usable(rr):
        if r['team'] != team or r['type'] != 'RAT':
            continue
        d = SG.decode(r['bc'], False)
        turns += 1
        track[r['id']].append((int(r['round']), int(r['x']), int(r['y']), d[0] if d else '?', int(r.get('cheese') or 0)))
    out = []
    for rid, t in track.items():
        i = 0
        while i < len(t):
            if t[i][3] not in TRAVEL:
                i += 1
                continue
            j = i
            while j + 1 < len(t) and t[j + 1][3] in TRAVEL and cheb(t[j + 1][1:3], t[i][1:3]) <= 1:
                j += 1
            if t[j][0] - t[i][0] >= span:
                ks = kings.get(t[i][0])
                out.append({'id': int(rid), 'start': t[i][0], 'end': t[j][0], 'rounds': t[j][0] - t[i][0],
                            'x': t[i][1], 'y': t[i][2], 'codes': dict(collections.Counter(s[3] for s in t[i:j + 1])),
                            'cheese': max(s[4] for s in t[i:j + 1]),
                            'king': min(cheb(k, t[i][1:3]) for k in ks) if ks else None})
            i = j + 1
    out.sort(key=lambda e: (-e['rounds'], e['start'], e['id']))
    return out, turns


def analyse(d, g, span=SPAN):
    """[{team, name, tagged, episodes, lost, turns, byCode}] for teams 1 and 2 of game g (untagged: no episodes)."""
    meta = json.load(open(os.path.join(d, f'g{g}.meta.json')))
    rows = list(GR.rows(os.path.join(d, f'g{g}.turns.tsv'), GR.need(d, g, COLS)))
    tagged = SG.tagged_teams(rows)
    res = []
    for t, side in (('1', 'A'), ('2', 'B')):
        o = {'team': t, 'name': meta.get('team' + side), 'tagged': bool(tagged.get(t, (False,))[0])}
        eps, n = episodes(rows, t, span) if o['tagged'] else ([], 0)
        by = collections.Counter()
        for e in eps:
            for c, k in e['codes'].items():
                by[c] += k
        o.update(episodes=eps, lost=sum(e['rounds'] for e in eps), turns=n, byCode=dict(by))
        res.append(o)
    return meta, res


def line(e):
    codes = ' '.join(f'{c}:{k}' for c, k in sorted(e['codes'].items(), key=lambda x: (-x[1], x[0])))
    return (f"#{e['id']} r{e['start']}-{e['end']} ({e['x']},{e['y']}) {codes} ${e['cheese']} "
            f"king {'-' if e['king'] is None else e['king']}")


def pct(a, b):
    return f'{100 * a / b:.1f}%' if b else 'n/a'


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument('src', nargs='+')
    ap.add_argument('--game', type=int, default=None, help='0-based game index (match.json game = N+1); default all')
    ap.add_argument('--team', default=None, help='only this team (by name)')
    ap.add_argument('--span', type=int, default=SPAN, help=f'rounds an episode must last (default {SPAN})')
    ap.add_argument('--top', type=int, default=4, help='longest episodes listed per game and team (default 4)')
    ap.add_argument('--json', action='store_true')
    a = ap.parse_args(argv)
    tot = collections.defaultdict(lambda: collections.Counter())
    for src in a.src:
        d = GR.source(src)
        for g in GR.pick(d, a.game):
            meta, res = analyse(d, g, a.span)
            res = [o for o in res if a.team is None or o['name'] == a.team]
            if a.json:
                print(json.dumps({'src': src, 'game': g, 'map': meta.get('map'), 'teams': res}))
            else:
                print(f"game {g} ({meta.get('map')}): A {meta.get('teamA')} vs B {meta.get('teamB')}"
                      + GR.result(GR.summary(d, g)))
            for o in res:
                if not o['tagged']:
                    if not a.json:
                        print(f"  {o['name']}: not tagged")
                    continue
                c = tot[o['name']]
                c['games'] += 1; c['episodes'] += len(o['episodes']); c['lost'] += o['lost']; c['turns'] += o['turns']
                if a.json:
                    continue
                by = ' '.join(f'{k} {v}' for k, v in sorted(o['byCode'].items(), key=lambda x: (-x[1], x[0])))
                print(f"  {o['name']}: {len(o['episodes'])} episodes, {o['lost']} rounds lost "
                      f"({pct(o['lost'], o['turns'])} of {o['turns']} baby turns){'; ' + by if by else ''}")
                for e in o['episodes'][:a.top]:
                    print('    ' + line(e))
    if not a.json and any(c['games'] > 1 for c in tot.values()):
        for name, c in sorted(tot.items()):
            print(f"total {name}: {c['games']} games, {c['episodes']} episodes, {c['lost']} rounds lost "
                  f"({pct(c['lost'], c['turns'])} of {c['turns']} baby turns)")
    return 0


if __name__ == '__main__':
    sys.exit(main())
