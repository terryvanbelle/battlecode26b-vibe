"""Champion correlation study (owner 11-23 11:48 PDT): does a local champion's win rate vs xk6 (box, local) go with its
win rate vs AlexT101_usqualsbot (replica)? Reads plan.json (sample + existing replica games), topup.json (new replica
matches; scores fetched from the replica API) and runs/cs001/results.jsonl (local games vs xk6), then writes
results.tsv, champ-corr.png and the stats block of README.md.
  tools/.venv/bin/python research/champ-corr/analyse.py      (from the repo root)"""
import json, math, os, random, sys

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(os.path.dirname(HERE))
sys.path.insert(0, os.path.join(REPO, 'tools/galaxy26'))


def wilson(w, n, z=1.96):
    if n == 0:
        return 0.0, 1.0
    p = w / n
    d = 1 + z * z / n
    c = (p + z * z / (2 * n)) / d
    h = z * math.sqrt(p * (1 - p) / n + z * z / (4 * n * n)) / d
    return max(0.0, c - h), min(1.0, c + h)


def pearson(x, y):
    n = len(x)
    mx, my = sum(x) / n, sum(y) / n
    sxy = sum((a - mx) * (b - my) for a, b in zip(x, y))
    sxx = sum((a - mx) ** 2 for a in x)
    syy = sum((b - my) ** 2 for b in y)
    return sxy / math.sqrt(sxx * syy) if sxx > 0 and syy > 0 else float('nan')


def ranks(v):
    o = sorted(range(len(v)), key=lambda i: v[i])
    r = [0.0] * len(v)
    i = 0
    while i < len(o):
        j = i
        while j + 1 < len(o) and v[o[j + 1]] == v[o[i]]:
            j += 1
        for k in range(i, j + 1):
            r[o[k]] = (i + j) / 2 + 1
        i = j + 1
    return r


def perm_p(x, y, stat, n=200000, seed=1):
    """Two-sided permutation p-value of stat(x, y) against shuffled pairings."""
    obs = abs(stat(x, y))
    rnd = random.Random(seed)
    yy = list(y)
    hit = 0
    for _ in range(n):
        rnd.shuffle(yy)
        if abs(stat(x, yy)) >= obs - 1e-12:
            hit += 1
    return (hit + 1) / (n + 1)


def replica_scores(ids):
    """{match id: (status, our score, their score)} from the replica API."""
    import client as cl
    c = cl.Client(None)
    acc = cl.load_accounts()
    s = cl.session(c, acc, 'vibe26b')
    my = acc['vibe26b']['team_id']
    out = {}
    for i in ids:
        m = s.api('GET', f'/api/compete/{c.episode}/match/{i}/')
        us = [p for p in m['participants'] if p['team'] == my]
        them = [p for p in m['participants'] if p['team'] != my]
        out[i] = (m['status'], us[0].get('score') if us else None, them[0].get('score') if them else None)
    return out


def local_games():
    out = {}
    f = os.path.join(REPO, 'runs/cs001/results.jsonl')
    if os.path.exists(f):
        for line in open(f):
            if not line.startswith('{'):
                continue
            g = json.loads(line)
            if g.get('b') == 'xk6':
                out[(g['a'], g['map'])] = g.get('winner')
    return out


def bt_ratings():
    """The local tournament's Bradley-Terry fit over every local game (tools/tourney.py): {bot: Rating}."""
    sys.path.insert(0, os.path.join(REPO, 'tools'))
    import tourney, elolib
    return elolib.fit(tourney.games())


def main():
    plan = json.load(open(os.path.join(HERE, 'plan.json')))
    tp = os.path.join(HERE, 'topup.json')
    top = json.load(open(tp)) if os.path.exists(tp) else []
    sc = replica_scores([t['match'] for t in top]) if top else {}
    loc = local_games()
    bt = bt_ratings()
    rows = []
    for p in plan['plan']:
        b = p['bot']
        rw = sum(g['won'] for g in p['replica_record'])
        rl = sum(g['lost'] for g in p['replica_record'])
        pend = 0
        for t in top:
            if t['bot'] != b:
                continue
            st, w, l = sc[t['match']]
            if st == 'OK!' and w is not None:
                rw += w; rl += l
            else:
                pend += len(t['maps'])
        lw = sum(1 for m in plan['maps'] if loc.get((b, m)) == 'A')
        ll = sum(1 for m in plan['maps'] if loc.get((b, m)) == 'B')
        lt = sum(1 for m in plan['maps'] if (b, m) in loc and loc[(b, m)] not in ('A', 'B'))
        rows.append({'bot': b, 'crowned': p['crowned_pdt'], 'lw': lw, 'll': ll, 'lt': lt,
                     'ln': lw + ll + lt, 'rw': rw, 'rl': rl, 'rn': rw + rl, 'pending': pend,
                     'existing': sum(g['won'] + g['lost'] for g in p['replica_record']),
                     'bt': bt[b].rating if b in bt else float('nan'), 'bt_se': bt[b].se if b in bt else float('nan'),
                     'bt_games': bt[b].games if b in bt else 0})
    with open(os.path.join(HERE, 'results.tsv'), 'w') as f:
        f.write('bot\tcrowned_pdt\tlocal_w\tlocal_l\tlocal_t\tlocal_rate\treplica_w\treplica_l\treplica_rate\treplica_existing\treplica_pending\tbt_rating\tbt_se\tbt_games\n')
        for r in rows:
            lr = (r['lw'] + 0.5 * r['lt']) / r['ln'] if r['ln'] else float('nan')
            rr = r['rw'] / r['rn'] if r['rn'] else float('nan')
            r['lr'], r['rr'] = lr, rr
            f.write(f"{r['bot']}\t{r['crowned']}\t{r['lw']}\t{r['ll']}\t{r['lt']}\t{lr:.3f}\t{r['rw']}\t{r['rl']}\t{rr:.3f}\t{r['existing']}\t{r['pending']}"
                    f"\t{r['bt']:.0f}\t{r['bt_se']:.0f}\t{r['bt_games']:.0f}\n")
    done = [r for r in rows if r['ln'] == len(plan['maps']) and r['rn'] >= 5 and not r['pending']]
    print(f"{'bot':5} {'crowned':11} {'local vs xk6':>13} {'replica vs AlexT101':>20}")
    for r in rows:
        print(f"{r['bot']:5} {r['crowned']:11} {r['lw']}-{r['ll']}" + (f"-{r['lt']}" if r['lt'] else '') +
              f" ({r['lr']:.0%})".rjust(8) + f"   {r['rw']}-{r['rl']} ({r['rr']:.0%})".rjust(14) +
              (f'  +{r["pending"]} pending' if r['pending'] else '') + ('' if r['ln'] == 5 else f'  local {r["ln"]}/5'))
    if len(done) < 3:
        print(f'{len(done)} champions complete: no statistics yet')
        return
    n = len(done)
    x = [r['lr'] for r in done]; y = [r['rr'] for r in done]; z = [r['bt'] for r in done]

    def corr(a, b):
        r = pearson(a, b)
        ci = [math.tanh(math.atanh(r) + k * 1.96 / math.sqrt(n - 3)) for k in (-1, 1)] if abs(r) < 1 and n > 3 else [float('nan')] * 2
        return {'r': r, 'ci95': ci, 'perm_p': perm_p(a, b, pearson), 'spearman': pearson(ranks(a), ranks(b)),
                'spearman_perm_p': perm_p(ranks(a), ranks(b), pearson)}

    # leave-one-out: how much each champion carries the correlation (10 points)
    loo = {}
    for i, r in enumerate(done):
        xi, yi, zi = x[:i] + x[i + 1:], y[:i] + y[i + 1:], z[:i] + z[i + 1:]
        loo[r['bot']] = {'local_vs_replica': pearson(xi, yi), 'rating_vs_replica': pearson(zi, yi)}
    stats = {'n': n, 'local_vs_replica': corr(x, y), 'rating_vs_replica': corr(z, y), 'rating_vs_local': corr(z, x),
             'leave_one_out': loo}
    for k, name in (('local_vs_replica', 'local rate vs xk6  ~ replica rate'), ('rating_vs_replica', 'tournament rating ~ replica rate'),
                    ('rating_vs_local', 'tournament rating ~ local rate vs xk6')):
        c = stats[k]
        print(f"{name:38s} n {n}: Pearson r {c['r']:+.2f} (95% CI {c['ci95'][0]:+.2f} to {c['ci95'][1]:+.2f}; permutation p "
              f"{c['perm_p']:.3f}), Spearman {c['spearman']:+.2f} (p {c['spearman_perm_p']:.3f})")
    for k in ('local_vs_replica', 'rating_vs_replica'):
        v = sorted((d[k], b) for b, d in loo.items())
        print(f'leave one out, {k}: r from {v[0][0]:+.2f} (without {v[0][1]}) to {v[-1][0]:+.2f} (without {v[-1][1]})')
    # per map: the champions' local record vs xk6 (cs001) and the replica top-up record vs AlexT101_usqualsbot on the
    # same maps (topup_maps.json: per-game winners read from the top-up replays, each checked to be the champion's package)
    tm = os.path.join(HERE, 'topup_maps.json')
    tg = json.load(open(tm)) if os.path.exists(tm) else []
    stats['maps'] = {}
    print(f"{'map':15s} {'local vs xk6':>13s} {'replica top-ups':>16s}")
    for m in plan['maps']:
        lw = sum(1 for r in done if loc.get((r['bot'], m)) == 'A')
        ln = sum(1 for r in done if (r['bot'], m) in loc)
        rw = sum(1 for g in tg if g['map'] == m and g['won'])
        rn = sum(1 for g in tg if g['map'] == m)
        stats['maps'][m] = {'local_w': lw, 'local_n': ln, 'replica_w': rw, 'replica_n': rn}
        print(f'{m:15s} {lw:>6d} of {ln:<4d} {rw:>8d} of {rn}')
    json.dump(stats, open(os.path.join(HERE, 'stats.json'), 'w'), indent=1)
    plot(done, stats)


def plot(done, st):
    import matplotlib
    matplotlib.use('Agg')
    import matplotlib.pyplot as plt
    import matplotlib.patheffects as pe
    import matplotlib.ticker as mt
    INK, INK2, MUTED, GRID, AXIS, SURF = '#0b0b0b', '#52514e', '#898781', '#e1e0d9', '#c3c2b7', '#fcfcfb'
    RAMP = ['#86b6ef', '#6da7ec', '#5598e7', '#3987e5', '#2a78d6', '#256abf', '#1c5cab', '#184f95', '#104281', '#0d366b']
    order = sorted(done, key=lambda r: r['crowned'])
    col = {r['bot']: RAMP[round(i * (len(RAMP) - 1) / max(1, len(order) - 1))] for i, r in enumerate(order)}
    fig, axes = plt.subplots(1, 2, figsize=(13, 6.6), sharey=True, facecolor=SURF)
    panels = ((axes[0], 'lr', 'local_vs_replica', 'win rate vs xk6 on the box (5 games each, same 5 maps)'),
              (axes[1], 'bt', 'rating_vs_replica', 'local tournament rating (Bradley-Terry, all games; whiskers ±1.96 SE)'))
    jobs = []
    for ax, key, sk, xlabel in panels:
        ax.set_facecolor(SURF)
        xs = [r[key] for r in done]
        rate = key == 'lr'
        lo_x, hi_x = (-0.05, 1.05) if rate else (min(xs) - 0.1 * (max(xs) - min(xs)), max(xs) + 0.1 * (max(xs) - min(xs)))
        # points that would overlap (one x column, replica rates within 0.05) are dodged sideways, so every champion
        # stays visible; exact twins share one label
        groups = {}
        for r in done:
            groups.setdefault((round(r[key], 3), round(r['rr'], 3)), []).append(r)
        step = 0.022 * (hi_x - lo_x)
        pos, placed = {}, []
        for r in sorted(done, key=lambda r: (r[key], r['rr'], r['crowned'])):
            for k in (0, 1, -1, 2, -2, 3, -3, 4, -4):
                px = r[key] + k * step
                if all(abs(px - qx) > 0.9 * step or abs(r['rr'] - qy) > 0.05 for qx, qy in placed):
                    break
            pos[r['bot']] = (px, r['rr'])
            placed.append((px, r['rr']))
        ax.axhline(0.5, color=AXIS, lw=0.9, zorder=1)   # above this line the champion beat AlexT101_usqualsbot
        ys = [r['rr'] for r in done]
        mx, my = sum(xs) / len(xs), sum(ys) / len(ys)
        sxx = sum((a - mx) ** 2 for a in xs)
        if sxx > 0:
            k = sum((a - mx) * (b - my) for a, b in zip(xs, ys)) / sxx
            ax.plot([lo_x, hi_x], [my + k * (lo_x - mx), my + k * (hi_x - mx)], color=MUTED, lw=1.2, ls='--', zorder=2)
        for r in done:
            px, py = pos[r['bot']]
            lo_y, hi_y = wilson(r['rw'], r['rn'])
            ax.plot([px, px], [lo_y, hi_y], color=col[r['bot']], alpha=0.35, lw=1.4, zorder=2, solid_capstyle='round')
            if not rate:
                ax.plot([px - 1.96 * r['bt_se'], px + 1.96 * r['bt_se']], [py, py], color=col[r['bot']], alpha=0.35, lw=1.4, zorder=2,
                        solid_capstyle='round')
            ax.scatter([px], [py], s=80, color=col[r['bot']], edgecolors=SURF, linewidths=2, zorder=4)
        ax.set_xlim(lo_x, hi_x); ax.set_ylim(-0.05, 1.05)
        ax.set_yticks([0, 0.2, 0.4, 0.6, 0.8, 1.0])
        ax.yaxis.set_major_formatter(mt.PercentFormatter(1.0))
        if rate:
            ax.set_xticks([0, 0.2, 0.4, 0.6, 0.8, 1.0])
            ax.xaxis.set_major_formatter(mt.PercentFormatter(1.0))
        ax.grid(color=GRID, lw=0.6, zorder=0)
        for s in ax.spines.values():
            s.set_color(AXIS)
        ax.tick_params(colors=MUTED, labelsize=9)
        ax.set_xlabel(xlabel, color=INK2, fontsize=9.5)
        c = st[sk]
        ax.set_title(f"Pearson r {c['r']:+.2f} (95% CI {c['ci95'][0]:+.2f} to {c['ci95'][1]:+.2f}), permutation p "
                     f"{c['perm_p']:.2f}; Spearman {c['spearman']:+.2f}", loc='left', color=INK, fontsize=10)
        jobs.append((ax, groups, pos))
    axes[0].set_ylabel('win rate vs AlexT101_usqualsbot on the replica (5-45 games)', color=INK2, fontsize=9.5)
    fig.suptitle(f"Do local champions do better on the replica?  {st['n']} champions, 11-21 to 11-23",
                 x=0.012, y=0.985, ha='left', color=INK, fontsize=13, fontweight='bold')
    fig.text(0.012, 0.948, 'Shade = crowning order (light = earliest, dark = latest).  Dashed = least squares.  Line at 50% = even '
             'with AlexT101.\nVertical whiskers = 95% Wilson interval of the replica rate; a 5-game local rate has one of about '
             '±35 points too.', color=INK2, fontsize=9, ha='left', va='top', linespacing=1.5)
    fig.tight_layout(rect=(0, 0, 1, 0.89))
    # labels after the layout pass (they cannot shrink the axes): bot names, first free spot beside each point
    fig.canvas.draw()
    rend = fig.canvas.get_renderer()
    for ax, groups, pos in jobs:
        tr = ax.transData
        taken = [tr.transform(p) for p in pos.values()]
        boxes = []
        ab = ax.get_window_extent()
        for (gx, gy), rs in sorted(groups.items()):
            text = ' / '.join(r['bot'] for r in sorted(rs, key=lambda r: pos[r['bot']][0]))
            cx = sum(pos[r['bot']][0] for r in rs) / len(rs)
            for dx, dy, ha, va in ((0, 12, 'center', 'bottom'), (0, -12, 'center', 'top'), (10, 0, 'left', 'center'),
                                   (-10, 0, 'right', 'center'), (10, 10, 'left', 'bottom'), (-10, 10, 'right', 'bottom'),
                                   (10, -10, 'left', 'top'), (-10, -10, 'right', 'top')):
                t = ax.annotate(text, (cx, gy), xytext=(dx, dy), textcoords='offset points', ha=ha, va=va, fontsize=9,
                                color=INK, zorder=5, path_effects=[pe.withStroke(linewidth=3, foreground=SURF)])
                bb = t.get_window_extent(rend).expanded(1.05, 1.15)
                inside = ab.x0 <= bb.x0 and bb.x1 <= ab.x1 and ab.y0 <= bb.y0 and bb.y1 <= ab.y1
                clash = any(bb.overlaps(o) for o in boxes) or any(bb.contains(px, py) for px, py in taken)
                if inside and not clash:
                    boxes.append(bb)
                    break
                t.remove()
            else:
                ax.annotate(text, (cx, gy), xytext=(0, 12), textcoords='offset points', ha='center', fontsize=9,
                            color=INK, zorder=5, path_effects=[pe.withStroke(linewidth=3, foreground=SURF)])
    out = os.path.join(HERE, 'champ-corr.png')
    fig.savefig(out, dpi=150, facecolor=SURF)
    print('wrote', out)


if __name__ == '__main__':
    main()
