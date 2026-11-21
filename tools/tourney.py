#!/usr/bin/env python3
"""Continuous local tournament of our own bots, ranked by Bradley-Terry (owner 11-21).

The pool is research/tourney/pool.txt (one bot per line). Every local game in runs/*/results.jsonl between two pool
bots on the 21 early maps counts (any seat, any map seed), so earlier screens and calibration runs seed the ladder.
The leader becomes the local champion once it has MIN_GAMES games and leads the champion by one combined SE; research/tourney/champion.txt holds the current one,
and a change is submitted to the replica without any gate (prod does the submit).

  tourney.py rank [--runs-root DIR]        ladder with SE, games; flags a new champion
  tourney.py plan RUN [--games N]          write runs/RUN/cells.tsv: N games chosen where the ladder is least sure
  tourney.py add BOT                       enter BOT into the pool (it is scheduled first until it has NEW_MIN games)

Pairing: bots with fewer than NEW_MIN games get half the run, weighted by their missing games, half of those games
against the champion and the rest against the current top TOP bots; the rest
goes to pairs among the top TOP, weighted toward close ratings and large standard errors (the places that decide
first place). Each game: a random map of the 21, a random map seed in 0..SEEDS-1, random seats.
"""
import json, os, random, sys, glob, math, collections
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import elolib

R = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
POOL = os.path.join(R, 'research', 'tourney', 'pool.txt')
CHAMP = os.path.join(R, 'research', 'tourney', 'champion.txt')
MAPS = open(os.path.join(R, 'tools', 'galaxy26', 'maps-early21.txt')).read().split()
MIN_GAMES, NEW_MIN, TOP, SEEDS = 200, 40, 6, 10


def pool():
    return [l.split('#')[0].strip() for l in open(POOL) if l.split('#')[0].strip()]


def games(root=R):
    P, ms = set(pool()), set(MAPS)
    out = []
    for f in glob.glob(os.path.join(root, 'runs', '*', 'results.jsonl')):
        for line in open(f):
            if not line.startswith('{'):
                continue
            try:
                g = json.loads(line)
            except ValueError:
                continue
            if g.get('map') not in ms or g['a'] not in P or g['b'] not in P or g['a'] == g['b']:
                continue
            if g.get('winner') not in ('A', 'B'):
                continue
            out.append((g['a'], g['b'], g['winner'] == 'A', g['winner'] == 'B'))
    return out


def ladder(root=R):
    gs = games(root)
    rt = elolib.fit(gs) if gs else {}
    P = pool()
    for b in P:
        if b not in rt:
            rt[b] = elolib.Rating(1500, 400, 0, 0, 0)
    return sorted(((b, rt[b]) for b in P), key=lambda x: -x[1].rating), len(gs)


def champion():
    return open(CHAMP).read().split()[0] if os.path.exists(CHAMP) else None


def cmd_rank(root):
    lad, n = ladder(root)
    print(f'{n} games among {len(lad)} pool bots')
    for i, (b, r) in enumerate(lad, 1):
        print(f'{i:2d}. {b:6s} {r.rating:6.0f} +-{r.se:3.0f}  games {r.games:5.0f}  ({r.wins:g}-{r.losses:g})')
    ch = champion()
    elig = [(b, r) for b, r in lad if r.games >= MIN_GAMES]
    rc = dict(lad).get(ch)
    if elig and elig[0][0] != ch and rc is not None:
        b, r = elig[0]
        margin = math.hypot(r.se, rc.se)   # hysteresis (11-21 10:13): lead the champion by one combined SE
        if r.rating - rc.rating > margin:
            print(f'NEW CHAMPION {b} (was {ch}): leads the bots with >= {MIN_GAMES} games by '
                  f'{r.rating - rc.rating:.0f} > {margin:.0f}')
        else:
            print(f'champion {ch} (held: {b} leads it by {r.rating - rc.rating:.0f}, needs > {margin:.0f})')
    elif elig and elig[0][0] != ch:
        print(f'NEW CHAMPION {elig[0][0]} (was {ch}): leads the bots with >= {MIN_GAMES} games')
    else:
        print(f'champion {ch}')


def cmd_plan(run, n):
    lad, _ = ladder()
    rate = {b: r for b, r in lad}
    top = [b for b, r in lad if r.games >= NEW_MIN][:TOP] or [b for b, _ in lad[:TOP]]
    new = [b for b, r in lad if r.games < NEW_MIN and b not in top]
    pairs = []
    ch = champion()
    if new:   # weight by missing games (a fresh bot outweighs a half-rated one); half its games vs the champion
        nw = [NEW_MIN - rate[b].games for b in new]
        for k in range(n // 2):
            b = random.choices(new, nw)[0]
            opp = [t for t in top if t != b] or [x for x, _ in lad if x != b]
            pairs.append((b, ch if ch in rate and ch != b and random.random() < 0.5 else random.choice(opp)))
    cand = [(a, b) for i, a in enumerate(top) for b in top[i + 1:]]
    w = [(rate[a].se + rate[b].se + 10) * math.exp(-abs(rate[a].rating - rate[b].rating) / 100) for a, b in cand]
    while len(pairs) < n and cand:
        pairs.append(random.choices(cand, w)[0])
    os.makedirs(os.path.join(R, 'runs', run), exist_ok=True)
    with open(os.path.join(R, 'runs', run, 'cells.tsv'), 'w') as f:
        for a, b in pairs:
            if random.random() < 0.5:
                a, b = b, a
            f.write(f'bots/{a}\tbots/{b}\t{random.choice(MAPS)}\t{random.randrange(SEEDS)}\n')
    c = collections.Counter(x for p in pairs for x in p)
    print(f'{run}: {len(pairs)} games; ' + ' '.join(f'{b}:{k}' for b, k in c.most_common()))


def cmd_add(bot):
    if bot in pool():
        print(f'{bot} already in pool'); return
    with open(POOL, 'a') as f:
        f.write(bot + '\n')
    print(f'added {bot}')


if __name__ == '__main__':
    a = sys.argv[1:]
    root = a[a.index('--runs-root') + 1] if '--runs-root' in a else R
    if not a or a[0] == 'rank':
        cmd_rank(root)
    elif a[0] == 'plan':
        cmd_plan(a[1], int(a[a.index('--games') + 1]) if '--games' in a else 120)
    elif a[0] == 'add':
        cmd_add(a[1])
    else:
        print(__doc__)
