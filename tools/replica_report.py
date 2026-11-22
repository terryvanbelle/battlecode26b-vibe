#!/usr/bin/env python3
"""Snapshots of our galaxy replica, through its public API: the ladder and our rating history.

  replica_report.py [--out progress] [--games progress/games.csv]

Writes progress/ladder.md + ladder.png (the Rankings page: galaxy's displayed rating, with each team's game W-L
from the replica rows of progress/games.csv) and progress/rating-history.md + rating-history.png (our displayed
rating after each rated match, from /match/historical_rating/: whole history and last 24 h, milestones of
progress/milestones.csv dashed; no submission bars, owner 11-22), and progress/ladder-position.png + ladder-position.csv (our rank on the ladder over
time, rebuilt from every ladder team's rating history: at each of our rated matches, 1 + the teams whose latest rating
by then is higher; owner 11-21). Regenerate, do not edit. Charts need matplotlib (tools/.venv).
"""
import argparse, datetime as dt, os, sys
from zoneinfo import ZoneInfo

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), 'galaxy26'))
import games as G
import milestones as MS

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEAM = G.TEAM
PDT = ZoneInfo('America/Vancouver')


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


def history_md(hist, subs, now, marks=()):
    L = ['# Rating history (replica)', '',
         f'Our displayed rating on the replica after each rated match, from `/api/compete/bc26/match/historical_rating/`, '
         f'over the whole history and the last 24 hours, with milestones dashed. Regenerate with `tools/replica_report.py`; data fetched '
         f'{now:%Y-%m-%d %H:%M} PDT. Times are Pacific.', '',
         '![rating history](rating-history.png)', '',
         'Our position on the ladder over time (rebuilt from every team\'s rating history; `ladder-position.csv`):', '',
         '![ladder position](ladder-position.png)', '']
    if marks:
        L += ['Milestones (dashed lines tagged M1, M2, ...; `progress/milestones.csv`):', '']
        L += [f'- {MS.tag(i)} {when:%Y-%m-%d %H:%M} PDT: {label}' for i, (when, label) in enumerate(marks)]
        L.append('')
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


def charts(teams, hist, subs, out, marks=(), now=None):
    try:
        import matplotlib
        matplotlib.use('Agg')
        import matplotlib.pyplot as plt
        import matplotlib.dates as mdates
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
    rating_chart(hist, out, marks, now)
    return True


def rating_chart(hist, out, marks=(), now=None, recent_h=24):
    """Two panels on wall-clock axes: our displayed rating over the whole history, and the last recent_h hours."""
    import matplotlib
    matplotlib.use('Agg')
    import matplotlib.pyplot as plt
    import matplotlib.dates as mdates
    if not hist:
        return
    now = now or hist[-1][0]
    t0 = now - dt.timedelta(hours=recent_h)
    fig, axs = plt.subplots(2, 1, figsize=(10, 7))
    for k, ax in enumerate(axs):
        hh = hist if k == 0 else [x for x in hist if x[0] >= t0]
        if k == 1:   # carry the rating held at the window start, and hold the last one to now
            before = [x for x in hist if x[0] < t0]
            hh = ([(t0, before[-1][1])] if before else []) + hh
            hh = hh + [(now, hh[-1][1])] if hh else hist[-1:]
        ax.plot([w for w, _ in hh], [r for _, r in hh], '-', color='#2a6fdb', lw=1.4)
        pts = hist if k == 0 else [x for x in hist if x[0] >= t0]
        ax.plot([w for w, _ in pts], [r for _, r in pts], 'o', color='#2a6fdb', ms=2.5)
        if k == 1:
            ax.set_xlim(t0, now + dt.timedelta(minutes=20))
            ax.xaxis.set_major_formatter(mdates.DateFormatter('%m-%d %H:%M', tz=PDT))
            ax.set_title(f'last {recent_h} h', fontsize=9)
        else:
            marks_on(ax, marks)
            ax.xaxis.set_major_formatter(mdates.DateFormatter('%m-%d', tz=PDT))
            ax.set_title(f'{TEAM} rating on the replica (dashed: milestones)')
        w, r = hh[-1]
        ax.annotate(f'{r:.1f}', (w, r), xytext=(-40, 8), textcoords='offset points', fontsize=8, color='#212529')
        ax.set_ylabel('displayed rating')
        ax.grid(color='#e9ecef')
    axs[1].set_xlabel('time (PDT)')
    if marks:
        fig.text(0.01, 0.005, 'Milestones: ' + MS.footnote(marks), fontsize=7, color=MS.COLOR, va='bottom', wrap=True)
        fig.tight_layout(rect=(0, 0.05, 1, 1))
    else:
        fig.tight_layout()
    fig.savefig(os.path.join(out, 'rating-history.png'), dpi=110)
    plt.close(fig)


def rank_history(s, ep, teams, hist):
    """[(time, our rank, teams rated by then)] at each of our rated matches, from every ladder team's history."""
    import bisect
    others = []
    for t in teams:
        if t['name'] == TEAM:
            continue
        raw = s.api('GET', f'/api/compete/{ep}/match/historical_rating/?team_id={t["id"]}')['team_rating']['rating_history']
        h = sorted((ts(x['timestamp']), x['rating']) for x in raw)
        if h:
            others.append(([w for w, _ in h], [r for _, r in h]))
    out = []
    for when, mine in hist:
        n, above = 1, 0
        for ws, rs in others:
            i = bisect.bisect_right(ws, when)
            if i:
                n += 1
                above += rs[i - 1] > mine
        out.append((when, above + 1, n))
    return out


def marks_on(ax, marks):
    for i, (when, label) in enumerate(marks):
        ax.axvline(when, color=MS.COLOR, ls='--', lw=1.3)
        t = ax.text(when, 0.01, MS.tag(i), transform=ax.get_xaxis_transform(), va='bottom', ha='center', fontsize=7,
                    color='white', clip_on=True, bbox=dict(boxstyle='round,pad=0.15', fc=MS.COLOR, ec='none'))
        t.set_in_layout(False)


def rank_chart(ranks, subs, out, marks=(), now=None, recent_h=24):
    """Two panels on wall-clock axes: the whole history, and the last recent_h hours."""
    import matplotlib
    matplotlib.use('Agg')
    import matplotlib.pyplot as plt
    import matplotlib.dates as mdates
    now = now or ranks[-1][0]
    t0 = now - dt.timedelta(hours=recent_h)
    fig, axs = plt.subplots(2, 1, figsize=(10, 7))
    for k, ax in enumerate(axs):
        rr = ranks if k == 0 else [x for x in ranks if x[0] >= t0] or ranks[-1:]
        if k == 1:   # carry the rank held at the window start
            before = [x for x in ranks if x[0] < t0]
            if before:
                rr = [(t0, before[-1][1], before[-1][2])] + rr
            rr = rr + [(now, rr[-1][1], rr[-1][2])]
        ax.step([w for w, _, _ in rr], [r for _, r, _ in rr], where='post', color='#2a6fdb', lw=1.6, label='our rank')
        ax.step([w for w, _, _ in rr], [n for _, _, n in rr], where='post', color='#adb5bd', lw=1, label='teams rated')
        ax.set_ylim(max(n for _, _, n in rr) + 1, 0.5)
        if k == 1:
            ax.set_xlim(t0, now + dt.timedelta(minutes=20))
            ax.xaxis.set_major_formatter(mdates.DateFormatter('%m-%d %H:%M', tz=PDT))
            ax.set_title(f'last {recent_h} h', fontsize=9)
        else:
            marks_on(ax, marks)
            ax.xaxis.set_major_formatter(mdates.DateFormatter('%m-%d', tz=PDT))
            ax.set_title(f'{TEAM} position on the replica ladder (dashed: milestones)')
        r = rr[-1]
        ax.annotate(f'#{r[1]} of {r[2]}', (r[0], r[1]), xytext=(-60, -14), textcoords='offset points', fontsize=8,
                    color='#212529')
        ax.set_ylabel('ladder position (1 = top)')
        ax.grid(color='#e9ecef')
        ax.legend(loc='lower left', fontsize=8)
    axs[1].set_xlabel('time (PDT)')
    if marks:
        fig.text(0.01, 0.005, 'Milestones: ' + MS.footnote(marks), fontsize=7, color=MS.COLOR, va='bottom', wrap=True)
        fig.tight_layout(rect=(0, 0.05, 1, 1))
    else:
        fig.tight_layout()
    fig.savefig(os.path.join(out, 'ladder-position.png'), dpi=110)
    plt.close(fig)


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
    marks = MS.read()
    open(os.path.join(a.out, 'rating-history.md'), 'w').write(history_md(hist, subs, now, marks))
    png = charts(teams, hist, subs, a.out, marks, now)
    ranks = rank_history(s, ep, teams, hist)
    with open(os.path.join(a.out, 'ladder-position.csv'), 'w') as f:
        f.write('time_pdt,rank,teams\n')
        f.writelines(f'{w:%Y-%m-%d %H:%M:%S},{r},{n}\n' for w, r, n in ranks)
    me = next((i for i, t in enumerate(teams, 1) if t['name'] == TEAM), None)
    if png and ranks:   # end the line at the live ladder (other teams move between our matches)
        rank_chart(ranks + ([(now, me, len(teams))] if me else []), subs, a.out, marks, now)
    print(f'ladder: {len(teams)} teams, {TEAM} #{me}; history: {len(hist)} rated matches, {len(subs)} submissions'
          f'{"" if png else " (no matplotlib: charts skipped)"}')


if __name__ == '__main__':
    main()
