#!/usr/bin/env python3
"""Continuous local tournament of our own bots, ranked by Bradley-Terry (owner 11-21).

The pool is research/tourney/pool.txt (one bot per line; '# NAME (why)' = retired: unscheduled and unlisted,
but its games still count in the fit). Every local game in runs/*/results.jsonl between two pool
bots on the 21 early maps counts (any seat, any map seed), so earlier screens and calibration runs seed the ladder.
The leader becomes the local champion once it has MIN_GAMES games and leads the champion by one combined SE; research/tourney/champion.txt holds the current one,
and a change is submitted to the replica without any gate (prod does the submit).

  tourney.py rank [--runs-root DIR]        ladder with SE, games; flags a new champion
  tourney.py plan RUN [--games N]          write runs/RUN/cells.tsv: N games chosen where the ladder is least sure
  tourney.py add BOT                       enter BOT into the pool (it is scheduled first until it has NEW_MIN games)
  tourney.py chart                         progress/local-elo.png + local-elo.md: every champion and candidate on a
                                           wall-clock axis (owner 11-21: kept current at every prod)

research/tourney/champions.csv is the champion history (time PDT, bot, base, change, submission); when champion.txt
names a bot that is not its last row, chart appends it at champion.txt's modification time.

Pairing: bots with fewer than NEW_MIN games get half the run, weighted by their missing games, half of those games
against the champion and the rest against the current top TOP bots (TOP covers the ~10-bot pool); the rest
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
MIN_GAMES, NEW_MIN, TOP, SEEDS = 200, 40, 10, 10   # TOP 6 -> 10 (11-22 10:55): with a ~10-bot pool every bot keeps getting games


def pool():
    return [l.split('#')[0].strip() for l in open(POOL) if l.split('#')[0].strip()]


def retired():
    # '# NAME  (why)' lines: no longer scheduled or listed, but their games still anchor the fit (11-21 15:07)
    out = []
    for l in open(POOL):
        t = l.strip()
        if t.startswith('#') and len(t.split()) > 1:
            out.append(t.split()[1])
    return out


def games(root=R):
    P, ms = set(pool()) | set(retired()), set(MAPS)
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
    print(f'{n} games among {len(lad)} pool bots ({len(retired())} retired; {len(lad) + len(retired())} entered in all)')
    ch = champion()
    rc0 = dict(lad).get(ch)
    for i, (b, r) in enumerate(lad, 1):
        tail = ''
        if rc0 is not None and b != ch:   # vs champion: lead, and the lead needed to replace it (one combined SE)
            need = math.hypot(r.se, rc0.se)
            gl = max(0, MIN_GAMES - r.games)
            tail = f'  vs {ch} {r.rating - rc0.rating:+5.0f} (needs > +{need:.0f}' + (f', {gl:.0f} more games)' if gl else ')')
        elif b == ch:
            tail = '  CHAMPION'
        print(f'{i:2d}. {b:6s} {r.rating:6.0f} +-{r.se:3.0f}  games {r.games:5.0f}  ({r.wins:g}-{r.losses:g}){tail}')
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
        nw = [max(NEW_MIN - rate[b].games, NEW_MIN // 4) for b in new]   # floor: a bot 2 games short still plays (11-21 21:15)
        for k in range(n // 2 if len(top) >= 3 else n * 5 // 6):   # few rated bots: newcomers take most of the run
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


CHAMPS = os.path.join(R, 'research', 'tourney', 'champions.csv')


def champ_history():
    import csv, datetime as dt
    from zoneinfo import ZoneInfo
    pdt = ZoneInfo('America/Vancouver')
    rows = list(csv.DictReader(open(CHAMPS)))
    ch = champion()
    if ch and (not rows or rows[-1]['bot'] != ch):   # a promotion not yet recorded: stamp it with champion.txt's mtime
        when = dt.datetime.fromtimestamp(os.path.getmtime(CHAMP), pdt)
        rows.append({'when_pdt': f'{when:%Y-%m-%d %H:%M}', 'bot': ch, 'base': rows[-1]['bot'] if rows else '',
                     'change': '', 'submission': ''})
        with open(CHAMPS, 'a') as f:
            f.write(f"{rows[-1]['when_pdt']},{ch},{rows[-1]['base']},,\n")
    for r in rows:
        r['when'] = dt.datetime.strptime(r['when_pdt'], '%Y-%m-%d %H:%M').replace(tzinfo=pdt)
    return rows


def entry_times():
    """bot -> when its snapshot was first committed (PDT), from one git log pass; uncommitted bots: directory mtime."""
    import subprocess, datetime as dt
    from zoneinfo import ZoneInfo
    pdt = ZoneInfo('America/Vancouver')
    out, t = {}, None
    log = subprocess.run(['git', '-C', R, 'log', '--diff-filter=A', '--name-only', '--format=@%ct', '--',
                          'bots/*/SNAPSHOT'], capture_output=True, text=True).stdout
    for l in log.splitlines():
        if l.startswith('@'):
            t = dt.datetime.fromtimestamp(int(l[1:]), pdt)
        elif l.startswith('bots/') and t:
            out[l.split('/')[1]] = t   # git log is newest first: the last seen is the first add
    for b in pool() + retired():
        if b not in out and os.path.isdir(os.path.join(R, 'bots', b)):
            out[b] = dt.datetime.fromtimestamp(os.path.getmtime(os.path.join(R, 'bots', b)), pdt)
    return out


def cmd_chart(out=os.path.join(R, 'progress')):
    import datetime as dt
    from zoneinfo import ZoneInfo
    import matplotlib
    matplotlib.use('Agg')
    import matplotlib.pyplot as plt
    import matplotlib.dates as mdates
    import milestones as MS
    pdt = ZoneInfo('America/Vancouver')
    gs = games()
    rt = elolib.fit(gs) if gs else {}
    hist = champ_history()
    start, now = hist[0]['when'], dt.datetime.now(pdt)
    ent = entry_times()
    champs = {r['bot'] for r in hist}
    cand = [(ent[b], rt[b]) for b in set(pool()) | set(retired())
            if b in rt and b in ent and b not in champs and ent[b] >= start and rt[b].games >= 10]
    fig, ax = plt.subplots(figsize=(10, 5))
    ax.scatter([w for w, _ in cand], [r.rating for _, r in cand], s=[8 + r.games / 8 for _, r in cand],
               color='#adb5bd', alpha=0.7, label='candidates (at entry; size = games)', zorder=2)
    xs, ys = [], []
    for i, r in enumerate(hist):
        b = r['bot']
        if b not in rt:
            continue
        end = hist[i + 1]['when'] if i + 1 < len(hist) else now
        xs += [r['when'], end]
        ys += [rt[b].rating, rt[b].rating]
        ax.errorbar([r['when']], [rt[b].rating], yerr=[rt[b].se], fmt='o', color='#2a6fdb', ms=6, capsize=3, zorder=4)
        lab = b + (f" {r['change']}" if r['change'] else '') + (f" #{r['submission']}" if r['submission'] else '')
        ax.annotate(lab, (r['when'], rt[b].rating), xytext=(4, 6), textcoords='offset points', fontsize=7.5,
                    color='#212529')
    ax.plot(xs, ys, color='#2a6fdb', lw=1.8, label='local champion (rating +- SE, current fit)', zorder=3)
    for i, (when, label) in enumerate(m for m in MS.read() if m[0] >= start - dt.timedelta(hours=1)):
        ax.axvline(when, color=MS.COLOR, ls='--', lw=1.3)
    ax.set_xlim(start - dt.timedelta(minutes=30), now + dt.timedelta(minutes=30))
    ax.xaxis.set_major_formatter(mdates.DateFormatter('%m-%d %H:%M', tz=pdt))
    ax.set_xlabel('time (PDT)')
    ax.set_ylabel('Bradley-Terry rating (local tournament)')
    ax.set_title(f'Local tournament: champions and candidates ({len(gs)} games, {now:%m-%d %H:%M} PDT)')
    ax.grid(color='#e9ecef')
    ax.legend(loc='lower right', fontsize=8)
    fig.autofmt_xdate()
    fig.tight_layout()
    fig.savefig(os.path.join(out, 'local-elo.png'), dpi=110)
    plt.close(fig)
    L = ['# Local tournament Elo', '',
         f'Bradley-Terry ratings of our own bots in the local tournament (`tools/tourney.py`), all {len(gs)} games in '
         f'one fit as of {now:%Y-%m-%d %H:%M} PDT, so every point is on the same scale; earlier champions are '
         'shown at their current rating. Regenerate with `tools/tourney.py chart`.', '',
         '![local elo](local-elo.png)', '',
         '| Champion from (PDT) | Bot | Change on base | Submission | Rating +- SE | Games |', '|---|---|---|---:|---:|---:|']
    for r in hist:
        x = rt.get(r['bot'])
        L.append(f"| {r['when_pdt']} | {r['bot']} | {r['change'] + (' on ' + r['base'] if r['base'] else '')} | "
                 f"{('#' + r['submission']) if r['submission'] else ''} | "
                 f"{f'{x.rating:.0f} +- {x.se:.0f}' if x else '-'} | {f'{x.games:.0f}' if x else '-'} |")
    open(os.path.join(out, 'local-elo.md'), 'w').write('\n'.join(L) + '\n')
    print(f'local-elo.png: {len(hist)} champions, {len(cand)} candidates')


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
    elif a[0] == 'chart':
        cmd_chart()
    else:
        print(__doc__)
