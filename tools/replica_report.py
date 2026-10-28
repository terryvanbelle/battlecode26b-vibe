#!/usr/bin/env python3
"""Snapshots of our galaxy replica, through its public API: the ladder and our rating history.

  replica_report.py [--out progress] [--games progress/games.csv]

Writes progress/ladder.md + ladder.png (the Rankings page: galaxy's displayed rating, with each team's game W-L
from the replica rows of progress/games.csv) and progress/rating-history.md + rating-history.png (our displayed
rating after each rated match, from /match/historical_rating/, with our submissions marked). Regenerate, do not
edit. Charts need matplotlib (tools/.venv).
"""
import argparse, datetime as dt, os, sys
from zoneinfo import ZoneInfo

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), 'galaxy26'))
import games as G

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEAM = G.TEAM
PDT = ZoneInfo('America/Los_Angeles')


def ts(s):
    return dt.datetime.fromisoformat(s).astimezone(PDT)


def wl_from_games(rows):
    """team -> [games won, games lost] over replica rows; our builds fold into TEAM."""
    out = {}
    for r in rows:
        if r['source'] != 'replica':
            continue
        a, b = (TEAM if x.startswith('us:') else x for x in (r['teamA'], r['teamB']))
        sa, sb = int(r['scoreA']), int(r['scoreB'])
        out.setdefault(a, [0, 0]); out.setdefault(b, [0, 0])
        out[a][0] += sa; out[a][1] += sb
        out[b][0] += sb; out[b][1] += sa
    return out


def ladder_md(teams, wl, now):
    L = ['# Galaxy replica ladder', '',
         f'Snapshot of the Rankings page of our private galaxy replica, taken {now:%b %d %H:%M} PDT. Written by '
         '`tools/replica_report.py`: regenerate it, do not edit it.', '',
         "**Rating** is the replica's displayed rating (galaxy's penalized Elo, which starts every team low and climbs "
         'over its first rated matches); it is not the Bradley-Terry fit of `progress/ELO.md`. Games W-L counts the '
         f'replica games in `progress/games.csv`. Our team is **{TEAM}**.', '',
         '![Galaxy replica ladder](ladder.png)', '',
         '| # | Team | Rating | Games W-L | Submission |', '|---:|---|---:|---|---|']
    for i, t in enumerate(teams, 1):
        w, l = wl.get(t['name'], [0, 0])
        name = f'**{t["name"]}**' if t['name'] == TEAM else t['name'].replace('_', '\\_')
        r = '-' if t['rating'] is None else f'{t["rating"]:.1f}'
        L.append(f'| {i} | {name} | {r} | {w}-{l} | {"accepted" if t["active"] else "none"} |')
    return '\n'.join(L) + '\n'


def history_md(hist, subs, now):
    L = ['# Rating history (replica)', '',
         f'Our displayed rating on the replica after each rated match, from `/api/compete/bc26/match/historical_rating/`, '
         f'with each submission marked at its upload. Regenerate with `tools/replica_report.py`; data fetched '
         f'{now:%Y-%m-%d %H:%M} PDT. Times are Pacific.', '',
         '![rating history](rating-history.png)', '']
    if hist:
        L.append(f'{len(hist)} rated matches; latest rating {hist[-1][1]:,.0f} ({hist[-1][0]:%Y-%m-%d %H:%M} PDT).')
        L.append('')
    L += ['| Submission | Build | Uploaded (PDT) | Accepted | Rating at upload | Rated matches since |',
          '|---:|---|---|---|---:|---:|']
    for i, s in enumerate(subs):
        t = ts(s['created'])
        nxt = ts(subs[i + 1]['created']) if i + 1 < len(subs) else None
        before = [r for when, r in hist if when <= t]
        since = [r for when, r in hist if when > t and (nxt is None or when <= nxt)]
        L.append(f'| #{s["id"]} | {s["package"] or "?"} | {t:%Y-%m-%d %H:%M} | {"yes" if s["accepted"] else "no"} | '
                 f'{(before[-1] if before else 0):,.0f} | {len(since)} |')
    return '\n'.join(L) + '\n'


def charts(teams, hist, subs, out):
    try:
        import matplotlib
        matplotlib.use('Agg')
        import matplotlib.pyplot as plt
    except ImportError:
        return False
    rated = [t for t in teams if t['rating'] is not None]
    fig, ax = plt.subplots(figsize=(8, max(3, 0.22 * len(rated))))
    ys = list(range(len(rated)))[::-1]
    ax.barh(ys, [t['rating'] for t in rated], color=['#d9480f' if t['name'] == TEAM else '#2a6fdb' for t in rated])
    ax.set_yticks(ys)
    ax.set_yticklabels([t['name'] for t in rated], fontsize=7)
    ax.set_xlim(min(t['rating'] for t in rated) * 0.9 if rated else 0, None)
    ax.set_xlabel('displayed rating')
    ax.set_title(f'Replica ladder ({TEAM} in orange)')
    fig.tight_layout()
    fig.savefig(os.path.join(out, 'ladder.png'), dpi=110)
    plt.close(fig)
    fig, ax = plt.subplots(figsize=(10, 4))
    if hist:
        ax.plot([w for w, _ in hist], [r for _, r in hist], 'o-', color='#2a6fdb', ms=3)
    for s in subs:
        t = ts(s['created'])
        ax.axvline(t, color='#d9480f', alpha=0.4, lw=1)
        ax.text(t, ax.get_ylim()[1], f' #{s["id"]} {s["package"] or ""}', rotation=90, va='top', fontsize=7,
                color='#d9480f')
    ax.set_ylabel('displayed rating')
    ax.set_title(f'{TEAM} rating on the replica (lines: submissions)')
    ax.grid(color='#e9ecef')
    fig.autofmt_xdate()
    fig.tight_layout()
    fig.savefig(os.path.join(out, 'rating-history.png'), dpi=110)
    plt.close(fig)
    return True


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument('--out', default=os.path.join(REPO, 'progress'))
    ap.add_argument('--games', default=os.path.join(REPO, 'progress', 'games.csv'))
    a = ap.parse_args(argv)
    import client as cl
    c = cl.Client()
    acc = cl.load_accounts()
    s = cl.session(c, acc, TEAM)
    ep = c.episode
    tid = acc[TEAM]['team_id']
    teams = cl.rankings(c)
    raw = s.api('GET', f'/api/compete/{ep}/match/historical_rating/?team_id={tid}')['team_rating']['rating_history']
    hist = sorted((ts(h['timestamp']), h['rating']) for h in raw)
    subs = sorted(s.pages(f'/api/compete/{ep}/submission/'), key=lambda x: x['created'])
    now = dt.datetime.now(PDT)
    wl = wl_from_games(G.read(a.games)) if os.path.exists(a.games) else {}
    open(os.path.join(a.out, 'ladder.md'), 'w').write(ladder_md(teams, wl, now))
    open(os.path.join(a.out, 'rating-history.md'), 'w').write(history_md(hist, subs, now))
    png = charts(teams, hist, subs, a.out)
    me = next((i for i, t in enumerate(teams, 1) if t['name'] == TEAM), None)
    print(f'ladder: {len(teams)} teams, {TEAM} #{me}; history: {len(hist)} rated matches, {len(subs)} submissions'
          f'{"" if png else " (no matplotlib: charts skipped)"}')


if __name__ == '__main__':
    main()
