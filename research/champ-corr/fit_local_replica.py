"""Owner 11-23 14:33 PDT: map local tournament Elo to replica Elo, and project when the local tournament's champion
would rank #15, #10 or #1 on the replica ladder.

  python3 -I research/champ-corr/fit_local_replica.py --fetch     # fetch the replica data into ladder_data.json
                                                                   # (a 3.7 MB cache, not committed)
  tools/.venv/bin/python research/champ-corr/fit_local_replica.py  # fit, project, chart

Data:
- local: progress/local-elo.md (every champion since the tournament began, on one Bradley-Terry fit: crowned time,
  bot, submission, rating +- SE);
- study: results21.tsv (10 champions x 21 games vs AlexT101_usqualsbot, our bot as team A, the e21 maps);
- replica (ladder_data.json): our ranked matches since 11-21 08:00 PDT (time, games won by each side, the opponent's
  rating before the match), our submissions (id, time, description = bot), and every ladder team's rating history.

The replica rating is plain Elo on games (siarnaq teams/models.py: K 24, scale 400; a match's score is the share of its
games won), so a per-game win rate p against a team rated R puts a bot at R + 400 log10(p / (1 - p)).

Fits (per game, maximum likelihood, L0 = 1550 local):
- A (study): P(win vs AlexT101_usqualsbot) = 1 / (1 + 10^(-(a + b (L - L0)) / 400)); replica rating = R_Alex + a +
  b (L - L0).
- B (ladder): P(win a ranked game) = 1 / (1 + 10^(-(alpha + beta (L - L0) - R_opp) / 400)), with L the local rating of
  the champion that was the active submission when the match was created and R_opp the opponent's rating then.
Projection: the champions' local ratings against crowning time (least squares), mapped through each fit, against the
ratings now held in ladder places 15, 10 and 1 (each moving at its drift over the last 48 hours); Monte Carlo over the
fit and trend uncertainties.
"""
import bisect, datetime as dt, json, math, os, re, sys

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(os.path.dirname(HERE))
DATA = os.path.join(HERE, 'ladder_data.json')
PDT = dt.timezone(dt.timedelta(hours=-7))
START = dt.datetime(2026, 11, 21, 8, 0, tzinfo=PDT)
TEAM, YARD = 'vibe26b', 'AlexT101_usqualsbot'
PLACES = (15, 10, 1)
L0 = 1550.0
K = math.log(10) / 400


def t_of(s):
    return dt.datetime.fromisoformat(s).astimezone(PDT)


def fetch():
    sys.path.insert(0, os.path.join(REPO, 'tools', 'galaxy26'))
    import client as cl
    c = cl.Client(None)
    acc = cl.load_accounts()
    s = cl.session(c, acc, TEAM)
    ep = c.episode
    me = acc[TEAM]['team_id']
    ranked = []
    allm = c.pages(f'/api/compete/{ep}/match/scrimmage/?team_id={me}',
                   stop=lambda res: any(t_of(m['created']) < START for m in res))
    for m in allm:
        if t_of(m['created']) < START or not m['is_ranked'] or m['status'] != 'OK!':
            continue
        us = next(p for p in m['participants'] if p['team'] == me)
        op = next(p for p in m['participants'] if p['team'] != me)
        ranked.append({'id': m['id'], 'created': m['created'], 'maps': m['maps'], 'won': us['score'], 'lost': op['score'],
                       'opp': op['teamname'], 'opp_old': op['old_rating'], 'our_old': us['old_rating'], 'our_new': us['rating']})
    subs = [{'id': x['id'], 'created': x['created'], 'ok': x.get('accepted') and x.get('status') == 'OK!',
             'description': x.get('description') or ''} for x in s.pages(f'/api/compete/{ep}/submission/')]
    teams = cl.rankings(c)
    hist = {}
    for tm in teams:
        raw = s.api('GET', f'/api/compete/{ep}/match/historical_rating/?team_id={tm["id"]}')['team_rating']['rating_history']
        hist[tm['name']] = sorted((x['timestamp'], x['rating']) for x in raw)
    now = dt.datetime.now(PDT)
    json.dump({'fetched': now.isoformat(), 'ranked': ranked, 'subs': subs,
               'ladder': [(tm['name'], tm['rating']) for tm in teams], 'hist': hist}, open(DATA, 'w'))
    print(f'{len(ranked)} ranked matches since {START:%m-%d %H:%M} PDT, {len(subs)} submissions, {len(teams)} teams -> {DATA}')


def local_champions():
    """[(crowned, bot, submission, rating, se)] from progress/local-elo.md."""
    out = []
    pat = re.compile(r'^\| (\d{4}-\d\d-\d\d \d\d:\d\d) \| (\w+) \| .*? \| #?(\d*) \| (\d+) \+- (\d+) \| \d+ \|$')
    for ln in open(os.path.join(REPO, 'progress', 'local-elo.md')):
        m = pat.match(ln.strip())
        if m:
            t = dt.datetime.strptime(m.group(1), '%Y-%m-%d %H:%M').replace(tzinfo=PDT)
            out.append((t, m.group(2), int(m.group(3)) if m.group(3) else None, float(m.group(4)), float(m.group(5))))
    return out


def study():
    rows = []
    for ln in open(os.path.join(HERE, 'results21.tsv')):
        f = ln.rstrip('\n').split('\t')
        if f[0] == 'bot' or not f[0]:
            continue
        h = dict(zip(HDR, f))
        rows.append((h['bot'], float(h['bt_rating']), float(h['bt_se']), int(h['replica_w']), int(h['replica_n'])))
    return rows


HDR = open(os.path.join(HERE, 'results21.tsv')).readline().rstrip('\n').split('\t')


def logistic_fit(rows):
    """rows: (x, offset, wins, n). Per game P = 1 / (1 + exp(-K (c + d x - offset))). Newton's method; returns
    (c, d), covariance."""
    import numpy as np
    th = np.array([sum(off * n for _, off, _, n in rows) / sum(n for *_, n in rows), 0.0])   # start at the mean offset
    for _ in range(100):
        g = np.zeros(2); H = np.zeros((2, 2))
        for x, off, w, n in rows:
            z = K * (th[0] + th[1] * x - off)
            p = 1 / (1 + math.exp(-z))
            v = np.array([1.0, x]) * K
            g += (w - n * p) * v
            H -= n * p * (1 - p) * np.outer(v, v)
        step = np.linalg.solve(H, g)
        th -= step
        if abs(step).max() < 1e-9:
            break
    return th, np.linalg.inv(-H)


def drift(hist_rows, now, hours=48.0):
    """Least-squares slope (rating per day) of a step series [(time, rating)] sampled every 15 min over the last hours."""
    import numpy as np
    xs, ys = [], []
    ts = [t for t, _ in hist_rows]
    t = now - dt.timedelta(hours=hours)
    while t <= now:
        i = bisect.bisect_right(ts, t)
        if i:
            xs.append((t - now).total_seconds() / 86400); ys.append(hist_rows[i - 1][1])
        t += dt.timedelta(minutes=15)
    if len(xs) < 3:
        return 0.0
    return float(np.polyfit(xs, ys, 1)[0])


def place_series(hist, k, now, hours=50.0, exclude=TEAM):
    """[(time, rating held in place k among the teams other than ours)] every 15 minutes over the last hours."""
    series = [([t_of(t) for t, _ in h], [r for _, r in h]) for n, h in hist.items() if n != exclude and h]
    out, t = [], now - dt.timedelta(hours=hours)
    while t <= now:
        cur = sorted((rs[i - 1] for ts, rs in series for i in [bisect.bisect_right(ts, t)] if i), reverse=True)
        if len(cur) >= k:
            out.append((t, cur[k - 1]))
        t += dt.timedelta(minutes=15)
    return out


def main():
    if '--fetch' in sys.argv:
        fetch()
        return
    import numpy as np
    D = json.load(open(DATA))
    now = t_of(D['fetched'])
    champs = local_champions()
    rating = {b: (r, se) for _, b, _, r, se in champs}

    # ---- fit A: the study (10 champions x 21 games vs AlexT101_usqualsbot)
    srows = study()
    thA, covA = logistic_fit([(L - L0, 0.0, w, n) for _, L, _, w, n in srows])
    ladder = dict(D['ladder'])
    R_yard = ladder[YARD]

    # ---- fit B: our ranked matches, each game against the opponent's rating then
    subs = sorted((t_of(x['created']), x['id'], x['description']) for x in D['subs'] if x['ok'])
    sub_bot = {}
    for _, sid, desc in subs:
        names = [b for b in rating if re.search(r'\b' + b + r'\b', desc)]
        if names:
            sub_bot[sid] = names[0]
    branked, unmatched = [], 0
    for m in D['ranked']:
        t = t_of(m['created'])
        i = bisect.bisect_right([s[0] for s in subs], t)
        sid = subs[i - 1][1] if i else None
        bot = sub_bot.get(sid)
        if bot is None:
            unmatched += 1
            continue
        branked.append((bot, t, m['won'], m['won'] + m['lost'], m['opp_old'], m['opp']))
    thB, covB = logistic_fit([(rating[b][0] - L0, R, w, n) for b, _, w, n, R, _ in branked])
    # joint: the study's games against the yardstick's rating now, and the ladder games against the opponents' then
    thJ, covJ = logistic_fit([(L - L0, R_yard, w, n) for _, L, _, w, n in srows] +
                             [(rating[b][0] - L0, R, w, n) for b, _, w, n, R, _ in branked])
    per_bot = {}
    for b, t, w, n, R, _ in branked:
        per_bot.setdefault(b, []).append((w, n, R))

    def perf(games):
        """Performance rating (MLE) of one bot from [(wins, n, R_opp)], with SE."""
        P = sum(R for _, _, R in games) / len(games)
        W = sum(w for w, _, _ in games)
        N = sum(n for _, n, _ in games)
        if W == 0 or W == N:
            return None
        for _ in range(100):
            g = h = 0.0
            for w, n, R in games:
                p = 1 / (1 + math.exp(-K * (P - R)))
                g += (w - n * p) * K
                h -= n * p * (1 - p) * K * K
            P -= g / h
            if abs(g / h) < 1e-9:
                break
        return P, 1 / math.sqrt(-h), W, N

    # ---- local pace: the champions' rating against crowning time (least squares), every champion and the last 24 h
    hrs = np.array([(t - now).total_seconds() / 3600 for t, *_ in champs])
    Ls = np.array([r for *_, r, _ in champs])
    paces = {}
    for key, mask in (('recent', hrs >= -24), ('all', hrs == hrs)):
        (sl, _), cv = np.polyfit(hrs[mask], Ls[mask], 1, cov=True)
        paces[key] = (float(sl * 24), float(math.sqrt(cv[0, 0]) * 24), int(mask.sum()))   # a day, its SE, champions
    champ = max(champs, key=lambda c: c[0])
    L_now, L_se = champ[3], champ[4]

    # ---- targets: the rating held now in places 15, 10, 1 by the teams other than ours, and its 48-hour drift
    others = sorted((r for n, r in D['ladder'] if n != TEAM), reverse=True)
    targets = {k: (others[k - 1], drift(place_series(D['hist'], k, now), now)) for k in PLACES}
    d_yard = drift([(t_of(t), r) for t, r in D['hist'][YARD]], now)
    ours = [(t_of(t), r) for t, r in D['hist'].get(TEAM, [])]
    ours_now = dict(D['ladder']).get(TEAM)
    our_place = 1 + sum(r > ours_now for r in others) if ours_now else None
    # every fit as (replica rating at L0, replica points per local point); A is placed at the yardstick's rating now
    fits = {'A': (np.array([R_yard + thA[0], thA[1]]), covA), 'B': (np.array(thB), covB), 'J': (np.array(thJ), covJ)}
    fit_games = {'A': sum(n for *_, n in srows), 'B': sum(n for _, _, _, n, _, _ in branked)}
    fit_games['J'] = fit_games['A'] + fit_games['B']
    pred = {k: th[0] + th[1] * (L_now - L0) for k, (th, _) in fits.items()}

    out = {'now': now.isoformat(), 'L0': L0, 'yardstick': {'team': YARD, 'rating': R_yard, 'drift_per_day': d_yard},
           'fits': {k: {'intercept': th[0], 'slope': th[1], 'se_intercept': math.sqrt(cv[0, 0]),
                        'se_slope': math.sqrt(cv[1, 1]), 'games': fit_games[k]} for k, (th, cv) in fits.items()},
           'ladder_matches': len(branked), 'unmatched_matches': unmatched,
           'paces': {k: {'per_day': v, 'se_per_day': se, 'champions': n} for k, (v, se, n) in paces.items()},
           'champion': {'bot': champ[1], 'local': L_now, 'local_se': L_se, 'predicted': pred},
           'ours': {'rating': ours_now, 'place': our_place},
           'targets': {str(k): {'rating': r, 'drift_per_day': d} for k, (r, d) in targets.items()}}
    print(f"now {now:%m-%d %H:%M} PDT; {YARD} {R_yard:.1f} (drift {d_yard:+.1f}/day); {TEAM} {ours_now:.1f} (#{our_place})")
    names = {'A': f'study, games vs {YARD}', 'B': f'ladder, ranked games in {len(branked)} matches', 'J': 'joint'}
    for k, (th, cv) in fits.items():
        print(f"fit {k} ({names[k]}, {fit_games[k]} games): replica = {th[0]:.1f} +- {math.sqrt(cv[0, 0]):.1f} "
              f"+ {th[1]:.2f} +- {math.sqrt(cv[1, 1]):.2f} x (local - {L0:.0f})")
    print('local pace: ' + '; '.join(f"{k} {v:+.0f} +- {se:.0f} a day ({n} champions)" for k, (v, se, n) in paces.items())
          + f"; champion {champ[1]} {L_now:.0f} +- {L_se:.0f}, predicted on the replica at "
          + ', '.join(f"{k} {x:.0f}" for k, x in pred.items()))
    print('per champion: local, study estimate (vs ' + YARD + '), ladder performance (games)')
    est = {}
    for t, b, sid, L, se in champs:
        st = next(((w, n) for bb, _, _, w, n in srows if bb == b), None)
        sA = None
        if st:
            p = (st[0] + 0.5) / (st[1] + 1)
            sA = R_yard + 400 * math.log10(p / (1 - p))
        pb = perf(per_bot[b]) if b in per_bot else None
        est[b] = {'crowned': t.isoformat(), 'local': L, 'local_se': se, 'study': sA, 'study_wl': st,
                  'ladder': pb[0] if pb else None, 'ladder_se': pb[1] if pb else None, 'ladder_games': pb[3] if pb else 0,
                  'ladder_wins': pb[2] if pb else None}
        print(f"  {b:5s} {L:6.0f}  {('%6.0f' % sA) if sA else '     -'}  {('%6.0f +- %3.0f (%d g)' % (pb[0], pb[1], pb[3])) if pb else '-'}")
    out['champions'] = est

    # ---- projection (Monte Carlo over the fit, the champion's rating now and the pace): the hours until the
    # champion's predicted replica rating reaches the rating of place k, held at today's value ('fixed') or moving at
    # its 48-hour drift less the yardstick's ('drift': our own rating is taken to drift like AlexT101_usqualsbot's)
    rng = np.random.default_rng(1)
    S = 20000
    Lnow_s = rng.normal(L_now, L_se, S)
    pace_s = {pk: rng.normal(pv, pse, S) / 24 for pk, (pv, pse, _) in paces.items()}   # local rating an hour
    proj, fan = {}, {}
    for name, (th, cov) in fits.items():
        ths = rng.multivariate_normal(th, cov, S)
        base = ths[:, 0] + ths[:, 1] * (Lnow_s - L0)
        if name == 'J':
            dd = np.linspace(0, 14, 141)
            for pk, vs in pace_s.items():
                path = base[None, :] + (ths[:, 1] * vs)[None, :] * dd[:, None] * 24
                fan[pk] = (dd, np.quantile(path, [0.1, 0.5, 0.9], axis=1))
        proj[name] = {}
        for k, (T, dk) in targets.items():
            cell = {'local_needed_now': float(L0 + (T - th[0]) / th[1]), 'reached_now_share': float((base >= T).mean())}
            for pk, vs in pace_s.items():
                for scen, rel in (('fixed', 0.0), ('drift', dk - d_yard)):
                    slope = ths[:, 1] * vs - rel / 24
                    tau = np.where(base >= T, 0.0, np.where(slope > 0, (T - base) / np.where(slope > 0, slope, 1.0), np.inf))
                    cell[f'{pk}/{scen}'] = [float(x) for x in np.quantile(np.minimum(tau, 1e7), [0.1, 0.5, 0.9])]
            proj[name][k] = cell
    out['projection'] = {n: {str(k): c for k, c in d.items()} for n, d in proj.items()}
    for k, (T, dk) in targets.items():
        cJ = proj['J'][k]
        print(f"place {k:2d} ({T:.0f} now, drift {dk:+.1f}/day, {dk - d_yard:+.1f} vs {YARD}): local "
              f"{cJ['local_needed_now']:.0f} needed (A {proj['A'][k]['local_needed_now']:.0f}, B {proj['B'][k]['local_needed_now']:.0f}); "
              f"there already in {100 * cJ['reached_now_share']:.0f}% of draws")
        for sk, label in SCEN:
            q = cJ[sk]
            print(f"   {label:34s} {when(now, q[1]):>11s}  (80%: {when(now, q[0])} to {when(now, q[2])});  "
                  f"A {when(now, proj['A'][k][sk][1])}, B {when(now, proj['B'][k][sk][1])}")
    json.dump(out, open(os.path.join(HERE, 'fit_local_replica.json'), 'w'), indent=1, default=float)
    chart(out, est, fits, fan, proj, targets, R_yard, now, champs, ours, d_yard)
    write_md(out, est, proj, targets, now, d_yard)


SCEN = (('recent/fixed', 'last-24-h pace, places held'), ('recent/drift', 'last-24-h pace, places drifting'),
        ('all/fixed', 'whole-run pace, places held'), ('all/drift', 'whole-run pace, places drifting'))
CAP_H = 60 * 24


def when(now, h, fmt='%m-%d %H:%M'):
    if h <= 0:
        return 'now'
    if h > CAP_H:
        return '> 60 days'
    return f"{now + dt.timedelta(hours=float(h)):{fmt}}"


def chart(out, est, fits, fan, proj, targets, R_yard, now, champs, ours, d_yard):
    import numpy as np
    import matplotlib
    matplotlib.use('Agg')
    import matplotlib.pyplot as plt
    import matplotlib.patheffects as pe
    import matplotlib.transforms as mtrans
    SURF, INK, INK2, MUTED, GRID, AXIS = '#fcfcfb', '#1f1f1f', '#4a4a4a', '#7a7a7a', '#e6e6e3', '#bdbdb8'
    COL = {'A': '#2a6fdb', 'B': '#d9822b', 'J': INK}
    PACE = {'recent': (INK, '-'), 'all': ('#8a8a8a', '--')}
    halo = [pe.withStroke(linewidth=3, foreground=SURF)]
    fig = plt.figure(figsize=(13.5, 11.2))
    fig.patch.set_facecolor(SURF)
    gs = fig.add_gridspec(2, 2, width_ratios=[1, 1.15], height_ratios=[1.5, 1], hspace=0.32, wspace=0.16)
    ax1, ax2, ax3 = fig.add_subplot(gs[0, 0]), fig.add_subplot(gs[0, 1]), fig.add_subplot(gs[1, :])
    for ax in (ax1, ax2, ax3):
        ax.set_facecolor(SURF)
        ax.grid(color=GRID, lw=0.6, zorder=0)
        for sp in ax.spines.values():
            sp.set_color(AXIS)
        ax.tick_params(colors=MUTED, labelsize=9)
    thJ = fits['J'][0]
    J = lambda L: thJ[0] + thJ[1] * (L - L0)
    ch = out['champion']
    # ---- panel 1: the mapping
    Lg = np.linspace(1400, 1800, 200)
    X = np.stack([np.ones_like(Lg), Lg - L0], 1)
    for k, (th, cov) in fits.items():
        mean = th[0] + th[1] * (Lg - L0)
        if k == 'J':
            sd = np.sqrt(np.einsum('ij,jk,ik->i', X, cov, X))
            ax1.fill_between(Lg, mean - 1.96 * sd, mean + 1.96 * sd, color=INK, alpha=0.08, lw=0, zorder=1)
        ax1.plot(Lg, mean, color=COL[k], lw=2.4 if k == 'J' else 1.3, zorder=3)
    for b, e in est.items():
        if e['study'] is not None:
            ax1.scatter([e['local']], [e['study']], s=42, color=COL['A'], edgecolors=SURF, linewidths=1.5, zorder=4)
        if e['ladder'] is not None and e['ladder_games'] >= 9:
            ax1.scatter([e['local']], [e['ladder']], s=34, marker='s', color=COL['B'], edgecolors=SURF, linewidths=1.2, zorder=4)
    for k, (T, _) in targets.items():
        ax1.axhline(T, color=MUTED, lw=0.8, ls=':', zorder=1)
        need = proj['J'][k]['local_needed_now']
        ax1.text(1404, T + 6, f'#{k} now' + (f' (needs local {need:.0f})' if need > 1800 else ''), color=INK2,
                 fontsize=8.5, path_effects=halo)
    ax1.axvline(ch['local'], color=AXIS, lw=0.9, ls='--', zorder=1)
    ax1.text(ch['local'] - 4, 1562, ch['bot'], color=INK2, fontsize=8.5, ha='right', path_effects=halo)
    ax1.set_xlim(1400, 1800)
    ax1.set_ylim(1550, 2250)
    ax1.set_xlabel('local tournament rating (Bradley-Terry)', color=INK2, fontsize=9.5)
    ax1.set_ylabel('replica rating (Elo)', color=INK2, fontsize=9.5)
    ax1.set_title('The mapping (band: 95% of the joint fit)', loc='left', color=INK, fontsize=10.5)
    # ---- panel 2: the champion's predicted replica rating, past and projected
    X0, X1 = -2.5, 10
    past = sorted(((t - now).total_seconds() / 86400, J(L)) for t, _, _, L, _ in champs)
    ax2.step([x for x, _ in past] + [0], [y for _, y in past] + [past[-1][1]], where='post', color=INK, lw=1.4, zorder=3)
    if ours:
        oh = [((t - now).total_seconds() / 86400, r) for t, r in ours if (t - now).total_seconds() > (X0 - 0.1) * 86400]
        if oh:
            ax2.step([x for x, _ in oh] + [0], [r for _, r in oh] + [oh[-1][1]], where='post', color=COL['B'], lw=1.1, zorder=2)
    J0 = J(ch['local'])
    for pk in ('all', 'recent'):
        dd, q = fan[pk]
        ax2.fill_between(dd, q[0], q[2], color=PACE[pk][0], alpha=0.07, lw=0, zorder=1)
        ax2.plot(dd, J0 + thJ[1] * out['paces'][pk]['per_day'] * dd, color=PACE[pk][0], ls=PACE[pk][1], lw=2, zorder=3)
    for k, (T, dk) in targets.items():
        rel = dk - d_yard
        ax2.axhline(T, color=AXIS, lw=0.9, zorder=1)
        dd = np.linspace(0, X1, 50)
        ax2.plot(dd, T + rel * dd, color=MUTED, lw=0.9, ls=':', zorder=1)
        ax2.text(X1 - 0.1, T - 7, f'#{k}', color=INK2, fontsize=8.5, ha='right', va='top', path_effects=halo)
    ax2.axvline(0, color=AXIS, lw=0.9, zorder=1)
    ax2.set_xlim(X0, X1)
    ax2.set_ylim(1600, 2300)
    ax2.set_xlabel(f'days from now ({now:%m-%d %H:%M} PDT)', color=INK2, fontsize=9.5)
    ax2.set_title('The champion on the replica: predicted, displayed, projected', loc='left', color=INK, fontsize=10.5)
    # ---- panel 3: when (median and 80% interval), joint fit
    D1 = 14
    rows, y = [], 0
    for k in PLACES:
        for sk, label in SCEN:
            rows.append((y, k, sk, label))
            y -= 1
        y -= 0.8
    for y, k, sk, label in rows:
        p10, p50, p90 = proj['J'][k][sk]
        col = PACE[sk.split('/')[0]][0]
        lo, hi = p10 / 24, min(p90 / 24, D1)
        ax3.plot([lo, hi], [y, y], color=col, lw=2, solid_capstyle='butt', zorder=3)
        if p90 / 24 > D1:
            ax3.plot([D1], [y], marker='>', ms=6, color=col, zorder=3)
        filled = sk.endswith('fixed')
        xm = min(p50 / 24, D1)
        ax3.plot([xm], [y], marker='o' if p50 / 24 <= D1 else '>', ms=8, color=col, mfc=col if filled else SURF,
                 mew=1.6, zorder=4)
        ax3.text(xm + 0.15 if xm < D1 - 1.6 else xm - 0.25, y + 0.32, when(now, p50, '%m-%d %H:%M').replace('> 60 days', 'beyond 60 days'),
                 color=INK2, fontsize=8, ha='left' if xm < D1 - 1.6 else 'right', va='center', path_effects=halo, zorder=5)
    ax3.set_yticks([r[0] for r in rows])
    ax3.set_yticklabels([r[3] for r in rows], fontsize=8.5, color=INK2)
    trans = mtrans.blended_transform_factory(fig.transFigure, ax3.transData)
    for k in PLACES:
        ys = [r[0] for r in rows if r[1] == k]
        T = targets[k][0]
        ax3.text(0.05, (max(ys) + min(ys)) / 2, f'#{k}\n({T:.0f} now)', transform=trans, ha='center', va='center',
                 fontsize=10.5, fontweight='bold', color=INK, linespacing=1.4)
    first_mid = (24 - now.hour - now.minute / 60) / 24
    ticks = [0] + [first_mid + i for i in range(D1 + 1) if 0.5 <= first_mid + i <= D1]
    ax3.set_xticks(ticks)
    ax3.set_xticklabels(['now'] + [f"{(now + dt.timedelta(days=t)):%m-%d}" for t in ticks[1:]], fontsize=8.5)
    ax3.set_xlim(-0.2, D1 + 0.2)
    ax3.set_ylim(rows[-1][0] - 0.7, 0.7)
    ax3.set_xlabel('date (PDT midnight ticks): median and 80% interval, joint fit; filled = places held at today\'s rating, '
                   'hollow = places drifting', color=INK2, fontsize=9)
    ax3.set_title('When would the local champion reach #15, #10, #1?', loc='left', color=INK, fontsize=10.5)
    # ---- titles
    fig.suptitle('Local tournament rating to replica rating', x=0.012, y=0.992, ha='left', color=INK, fontsize=13,
                 fontweight='bold')
    f, pc = out['fits'], out['paces']
    fig.text(0.012, 0.962,
             f"Mapping: A (blue) = {f['A']['games']} study games vs {YARD} (dots), placed at its rating {R_yard:.0f}; "
             f"B (orange) = {f['B']['games']} ranked ladder games of the champions while submitted (squares: 9+ games);\n"
             f"black = both: replica = {f['J']['intercept']:.0f} + {f['J']['slope']:.2f} x (local - {L0:.0f}), slope +- "
             f"{f['J']['se_slope']:.2f}. Projection from {ch['bot']} ({ch['local']:.0f}) at the champions' pace: solid = last "
             f"24 h ({pc['recent']['per_day']:+.0f} a day), dashed = whole run ({pc['all']['per_day']:+.0f} a day).\n"
             f"Grey lines: ladder places at today's rating; dotted: moving at their 48-hour drift less {YARD}'s. "
             f"Black step: the champions' predicted rating; orange step: our displayed rating.",
             color=INK2, fontsize=9, ha='left', va='top', linespacing=1.5)
    fig.subplots_adjust(left=0.06, right=0.985, top=0.875, bottom=0.06)
    p3 = ax3.get_position()
    ax3.set_position([0.255, p3.y0, 0.985 - 0.255, p3.height])
    fig.canvas.draw()
    for k, dy, x in (('A', 6, 1795), ('B', -14, 1795), ('J', 7, 1745)):
        th = fits[k][0]
        ax1.annotate({'A': 'A', 'B': 'B', 'J': 'joint'}[k], (x, th[0] + th[1] * (x - L0)), xytext=(-4, dy),
                     textcoords='offset points', ha='right', color=COL[k], fontsize=9.5, fontweight='bold', path_effects=halo)
    # direct labels: the two steps (just before now) and the two pace lines
    xp = -0.45
    yp = next(y for x, y in reversed(past) if x <= xp)
    ax2.annotate('predicted', (xp, yp), xytext=(-3, 7), textcoords='offset points', ha='right', color=INK, fontsize=8.5,
                 path_effects=halo)
    if ours and oh:
        xo = -0.75
        yo = next((r for x, r in reversed(oh) if x <= xo), oh[0][1])
        ax2.annotate('displayed', (xo, yo), xytext=(0, -13), textcoords='offset points', ha='center', color=COL['B'],
                     fontsize=8.5, path_effects=halo)
    for pk, xd, dx, dy, ha in (('recent', 2.2, -6, 2, 'right'), ('all', 6.0, 6, -12, 'left')):
        yd = J0 + thJ[1] * out['paces'][pk]['per_day'] * xd
        ax2.annotate({'recent': 'last-24-h pace', 'all': 'whole-run pace'}[pk], (xd, yd), xytext=(dx, dy),
                     textcoords='offset points', ha=ha, color=PACE[pk][0], fontsize=8.5, path_effects=halo)
    for path in (os.path.join(HERE, 'fit-local-replica.png'), os.path.join(REPO, 'progress', 'local-to-replica.png')):
        fig.savefig(path, dpi=150, facecolor=SURF)
    print('wrote fit-local-replica.png and progress/local-to-replica.png')


def write_md(out, est, proj, targets, now, d_yard):
    f, pc, ch = out['fits'], out['paces'], out['champion']
    L = [f'# Local tournament rating to replica rating', '',
         f'Fitted {now:%Y-%m-%d %H:%M} PDT by `research/champ-corr/fit_local_replica.py` (owner 11-23 14:33: map local Elo '
         'to replica Elo and predict when the local tournament produces a bot that ranks 15th, 10th or 1st). Chart: '
         '[local-to-replica.png](local-to-replica.png).', '',
         '## The mapping', '',
         'Each game is a coin flip with the Elo odds of the replica (siarnaq: scale 400, a match scores its share of games '
         f'won), so a bot\'s replica rating R wins a game against a team rated R_opp with probability 1 / (1 + 10^((R_opp - R) / 400)), '
         f'and R = intercept + slope x (local - {L0:.0f}). Fitted by maximum likelihood:', '',
         '| fit | games | replica rating at local 1550 | replica points per local point | the champion ' + ch['bot'] + ' |',
         '|---|---|---|---|---|']
    desc = {'A': f'A: the study, 10 champions x 21 maps vs {YARD} (placed at its rating now, {out["yardstick"]["rating"]:.0f})',
            'B': f'B: our ranked ladder games since 11-21 08:20 ({out["ladder_matches"]} matches), each with its active champion and the opponent\'s rating then',
            'J': 'joint: A and B together'}
    for k in ('A', 'B', 'J'):
        x = f[k]
        L.append(f"| {desc[k]} | {x['games']} | {x['intercept']:.0f} +- {x['se_intercept']:.0f} | {x['slope']:.2f} +- "
                 f"{x['se_slope']:.2f} | {ch['predicted'][k]:.0f} |")
    L += ['', f"- **Read:** a local point is worth about {f['J']['slope']:.1f} of a replica point (one SE: "
          f"{f['J']['slope'] - f['J']['se_slope']:.2f} to {f['J']['slope'] + f['J']['se_slope']:.2f}). "
          f"The champion {ch['bot']} (local {ch['local']:.0f} +- {ch['local_se']:.0f}) should settle near {ch['predicted']['J']:.0f} "
          f"on the replica; it shows {out['ours']['rating']:.0f} (#{out['ours']['place']}) now.",
          '- **Range measured:** local 1448 to ' + f"{max(e['local'] for e in est.values()):.0f}" + '. Anything above it is an extrapolation.', '',
          '## When', '',
          f"From {ch['bot']} at the champions' pace: the least-squares slope of the champions' local rating against "
          f"their crowning time, over the last 24 h ({pc['recent']['per_day']:+.0f} +- {pc['recent']['se_per_day']:.0f} a day, "
          f"{pc['recent']['champions']} champions) or the whole run ({pc['all']['per_day']:+.0f} +- {pc['all']['se_per_day']:.0f} "
          f"a day, {pc['all']['champions']} champions). The ladder places either hold today's rating or move at their "
          f"48-hour drift less {YARD}'s ({out['yardstick']['drift_per_day']:+.0f} a day; our own rating is taken to drift "
          'like it). Median and 80% interval over 20000 draws of the joint fit, the champion\'s rating and the pace:', '',
          '| place | rating now | drift a day (vs ' + YARD + ') | local needed | ' + ' | '.join(lab for _, lab in SCEN) + ' |',
          '|---|---|---|---|' + '---|' * len(SCEN)]
    for k, (T, dk) in targets.items():
        c = proj['J'][k]
        cells = []
        for sk, _ in SCEN:
            q = c[sk]
            cells.append(f"**{when(now, q[1])}** ({when(now, q[0])} to {when(now, q[2])})")
        L.append(f"| #{k} | {T:.0f} | {dk:+.0f} ({dk - d_yard:+.0f}) | {c['local_needed_now']:.0f} | " + ' | '.join(cells) + ' |')
    L += ['', 'Medians of the single fits, for comparison (last-24-h pace, places held): ' +
          '; '.join(f"#{k}: A {when(now, proj['A'][k]['recent/fixed'][1])}, B {when(now, proj['B'][k]['recent/fixed'][1])}" for k in PLACES) + '.',
          '', '## Per champion', '',
          f'| champion | crowned (PDT) | local | study vs {YARD} (W-L of 21) | its rating from the study | ladder performance +- SE (games) |',
          '|---|---|---|---|---|---|']
    for b, e in est.items():
        wl = f"{e['study_wl'][0]}-{e['study_wl'][1] - e['study_wl'][0]}" if e['study_wl'] else ''
        st = f"{e['study']:.0f}" if e['study'] is not None else ''
        ld = f"{e['ladder']:.0f} +- {e['ladder_se']:.0f} ({e['ladder_games']})" if e['ladder'] is not None else ''
        L.append(f"| {b} | {e['crowned'][5:16].replace('T', ' ')} | {e['local']:.0f} | {wl} | {st} | {ld} |")
    L += ['', '## Caveats', '',
          f"- **The slope is the weak link.** {f['J']['slope']:.2f} +- {f['J']['se_slope']:.2f}: the local range measured is "
          "narrow and the replica games are few. The per-game binomial model also treats the replica's games as independent, "
          "while the replica is deterministic (one seed per map, 9 of 21 study maps decided the same way for every champion).",
          '- **#1 is an extrapolation far past the data.** It needs a local rating around '
          f"{proj['J'][1]['local_needed_now']:.0f}, about {proj['J'][1]['local_needed_now'] - ch['local']:.0f} above {ch['bot']}: "
          'a bot that beats today\'s champion nearly every game. Nothing says local gains keep converting at the same rate that far out.',
          '- **The targets move.** The top of the ladder still climbs as the replica\'s ratings spread (place 1: about +63 a day '
          'two days ago, +34 in the last 24 h). The two target columns bound this.',
          '- **The local scale moves.** Champions\' ratings shift by tens of points as games accumulate (sw58 crowned +26 '
          'over sw54, now below it), and the pace has varied: about +60 a day on the first day, +160 in the last.',
          '- **Our displayed rating lags.** A new submission needs a few dozen ranked games to settle (K 24).']
    open(os.path.join(REPO, 'progress', 'local-to-replica.md'), 'w').write('\n'.join(L) + '\n')
    print('wrote progress/local-to-replica.md')


if __name__ == '__main__':
    main()
