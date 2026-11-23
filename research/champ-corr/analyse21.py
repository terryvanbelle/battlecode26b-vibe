"""The firmed-up champion study (owner 11-23 12:45 PDT: "firm up the game count"): the same 10 champions on all 21 e21
maps. Local: vs xk6 in both seats, 42 games each (runs cs001-cs004). Replica: vs AlexT101_usqualsbot as team A, 21 games
each (topup_maps.json, reused, and full_maps.json from fetch21.py). Writes results21.tsv, stats21.json and
champ-corr21.png.   tools/.venv/bin/python research/champ-corr/analyse21.py      (from the repo root)"""
import json, math, os, sys
HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(os.path.dirname(HERE))
sys.path.insert(0, HERE)
from analyse import wilson, pearson, ranks, perm_p, bt_ratings   # noqa: E402


def local_results(bots, maps, runs):
    """{(bot, map, seat): 'W' | 'L' | 'T'} from the champion's side, for games against xk6 on the study maps."""
    out = {}
    for r in runs:
        f = os.path.join(REPO, 'runs', r, 'results.jsonl')
        if not os.path.exists(f):
            continue
        for line in open(f):
            if not line.startswith('{'):
                continue
            g = json.loads(line)
            if g['map'] not in maps:
                continue
            if g['b'] == 'xk6' and g['a'] in bots:
                bot, seat = g['a'], 'A'
            elif g['a'] == 'xk6' and g['b'] in bots:
                bot, seat = g['b'], 'B'
            else:
                continue
            w = g.get('winner')
            out[(bot, g['map'], seat)] = 'W' if w == seat else ('L' if w in ('A', 'B') else 'T')
    return out


def replica_results():
    """{(bot, map): won} for the champions as team A against AlexT101_usqualsbot (top-ups reused + the 21-map matches)."""
    out = {}
    for name in ('topup_maps.json', 'full_maps.json'):
        f = os.path.join(HERE, name)
        if os.path.exists(f):
            for g in json.load(open(f)):
                assert g['package'] == g['bot'], g
                out[(g['bot'], g['map'])] = bool(g['won'])
    return out


def corr(a, b):
    n = len(a)
    r = pearson(a, b)
    ci = [math.tanh(math.atanh(r) + k * 1.96 / math.sqrt(n - 3)) for k in (-1, 1)] if abs(r) < 1 and n > 3 else [float('nan')] * 2
    return {'r': r, 'ci95': ci, 'perm_p': perm_p(a, b, pearson), 'spearman': pearson(ranks(a), ranks(b)),
            'spearman_perm_p': perm_p(ranks(a), ranks(b), pearson)}


def split_half(done, rate_on):
    """Odd-even split over the maps: correlation across champions of their rates on the two halves, and its
    Spearman-Brown reliability for the whole set (how consistently the maps rank the champions)."""
    a = [rate_on(r, 0) for r in done]
    b = [rate_on(r, 1) for r in done]
    h = pearson(a, b)
    return {'half_r': h, 'reliability': 2 * h / (1 + h) if h > -1 else float('nan')}


def main():
    plan = json.load(open(os.path.join(HERE, 'plan21.json')))
    maps = plan['maps']
    bots = [p['bot'] for p in plan['plan']]
    loc = local_results(set(bots), set(maps), plan['local_runs'])
    rep = replica_results()
    bt = bt_ratings()
    rows = []
    for p in plan['plan']:
        b = p['bot']
        lres = [loc[(b, m, s)] for m in maps for s in 'AB' if (b, m, s) in loc]
        rres = [rep[(b, m)] for m in maps if (b, m) in rep]
        r = {'bot': b, 'crowned': p['crowned_pdt'],
             'lw': lres.count('W'), 'll': lres.count('L'), 'lt': lres.count('T'), 'ln': len(lres),
             'law': sum(1 for m in maps if loc.get((b, m, 'A')) == 'W'), 'lbw': sum(1 for m in maps if loc.get((b, m, 'B')) == 'W'),
             'rw': sum(rres), 'rn': len(rres),
             'bt': bt[b].rating if b in bt else float('nan'), 'bt_se': bt[b].se if b in bt else float('nan'),
             'bt_games': bt[b].games if b in bt else 0}
        r['rl'] = r['rn'] - r['rw']
        r['lr'] = (r['lw'] + 0.5 * r['lt']) / r['ln'] if r['ln'] else float('nan')
        r['rr'] = r['rw'] / r['rn'] if r['rn'] else float('nan')
        rows.append(r)
    with open(os.path.join(HERE, 'results21.tsv'), 'w') as f:
        f.write('bot\tcrowned_pdt\tlocal_w\tlocal_l\tlocal_t\tlocal_n\tlocal_rate\tlocal_A_w\tlocal_B_w\treplica_w\treplica_l\t'
                'replica_n\treplica_rate\tbt_rating\tbt_se\tbt_games\n')
        for r in rows:
            f.write(f"{r['bot']}\t{r['crowned']}\t{r['lw']}\t{r['ll']}\t{r['lt']}\t{r['ln']}\t{r['lr']:.3f}\t{r['law']}\t{r['lbw']}\t"
                    f"{r['rw']}\t{r['rl']}\t{r['rn']}\t{r['rr']:.3f}\t{r['bt']:.0f}\t{r['bt_se']:.0f}\t{r['bt_games']:.0f}\n")
    full_l, full_r = 2 * len(maps), len(maps)
    print(f"{'bot':5} {'crowned':11} {'local vs xk6':>17} {'(A / B seat)':>12} {'replica vs AlexT101':>20}")
    for r in rows:
        print(f"{r['bot']:5} {r['crowned']:11} " + f"{r['lw']}-{r['ll']}" + (f"-{r['lt']}" if r['lt'] else '') +
              f" ({r['lr']:.0%})".rjust(7) + f"{r['law']:>6} / {r['lbw']:<4}" + f"{r['rw']}-{r['rl']} ({r['rr']:.0%})".rjust(15) +
              ('' if r['ln'] == full_l else f'  local {r["ln"]}/{full_l}') + ('' if r['rn'] == full_r else f'  replica {r["rn"]}/{full_r}'))
    done = [r for r in rows if r['ln'] == full_l and r['rn'] == full_r]
    if len(done) < 4:
        print(f'{len(done)} champions complete: no statistics yet')
        return
    n = len(done)
    x = [r['lr'] for r in done]; y = [r['rr'] for r in done]; z = [r['bt'] for r in done]
    loo = {}
    for i, r in enumerate(done):
        xi, yi, zi = x[:i] + x[i + 1:], y[:i] + y[i + 1:], z[:i] + z[i + 1:]
        loo[r['bot']] = {'local_vs_replica': pearson(xi, yi), 'rating_vs_replica': pearson(zi, yi)}

    def lrate(r, half):
        res = [loc[(r['bot'], m, s)] for j, m in enumerate(maps) if j % 2 == half for s in 'AB']
        return (res.count('W') + 0.5 * res.count('T')) / len(res)

    def rrate(r, half):
        res = [rep[(r['bot'], m)] for j, m in enumerate(maps) if j % 2 == half]
        return sum(res) / len(res)

    stats = {'n': n, 'local_vs_replica': corr(x, y), 'rating_vs_replica': corr(z, y), 'rating_vs_local': corr(z, x),
             'leave_one_out': loo, 'split_half': {'local': split_half(done, lrate), 'replica': split_half(done, rrate)}}
    for k, name in (('local_vs_replica', 'local rate vs xk6  ~ replica rate'), ('rating_vs_replica', 'tournament rating ~ replica rate'),
                    ('rating_vs_local', 'tournament rating ~ local rate vs xk6')):
        c = stats[k]
        print(f"{name:38s} n {n}: Pearson r {c['r']:+.2f} (95% CI {c['ci95'][0]:+.2f} to {c['ci95'][1]:+.2f}; permutation p "
              f"{c['perm_p']:.3f}), Spearman {c['spearman']:+.2f} (p {c['spearman_perm_p']:.3f})")
    for k in ('local_vs_replica', 'rating_vs_replica'):
        v = sorted((d[k], b) for b, d in loo.items())
        print(f'leave one out, {k}: r from {v[0][0]:+.2f} (without {v[0][1]}) to {v[-1][0]:+.2f} (without {v[-1][1]})')
    for k, d in stats['split_half'].items():
        print(f"split-half over maps, {k}: halves r {d['half_r']:+.2f}, reliability {d['reliability']:+.2f}")
    sa = sum(r['law'] for r in done); sb = sum(r['lbw'] for r in done)
    print(f'local seat check: champions won {sa} of {n * len(maps)} as team A and {sb} of {n * len(maps)} as team B')
    stats['local_seats'] = {'A_w': sa, 'B_w': sb, 'n_each': n * len(maps)}
    stats['maps'] = {}
    print(f"{'map':16s} {'local vs xk6':>14s} {'replica':>10s}")
    for m in maps:
        lw = sum(1 for r in done for s in 'AB' if loc.get((r['bot'], m, s)) == 'W')
        rw = sum(1 for r in done if rep.get((r['bot'], m)))
        stats['maps'][m] = {'local_w': lw, 'local_n': 2 * n, 'replica_w': rw, 'replica_n': n}
        print(f'{m:16s} {lw:>6d} of {2 * n:<5d} {rw:>4d} of {n}')
    mx = [stats['maps'][m]['local_w'] / (2 * n) for m in maps]; my = [stats['maps'][m]['replica_w'] / n for m in maps]
    stats['maps_local_vs_replica'] = corr(mx, my)
    c = stats['maps_local_vs_replica']
    print(f"per map (21 maps): local win share ~ replica win share r {c['r']:+.2f} (p {c['perm_p']:.3f})")
    json.dump(stats, open(os.path.join(HERE, 'stats21.json'), 'w'), indent=1)
    plot(done, stats, len(maps))


def plot(done, st, nmaps):
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
    panels = ((axes[0], 'lr', 'local_vs_replica', f'win rate vs xk6 on the box ({2 * nmaps} games each: {nmaps} maps, both seats)'),
              (axes[1], 'bt', 'rating_vs_replica', 'local tournament rating (Bradley-Terry, all games; whiskers ±1.96 SE)'))
    jobs = []
    for ax, key, sk, xlabel in panels:
        ax.set_facecolor(SURF)
        xs = [r[key] for r in done]
        rate = key == 'lr'
        lo_x, hi_x = (-0.05, 1.05) if rate else (min(xs) - 0.1 * (max(xs) - min(xs)), max(xs) + 0.1 * (max(xs) - min(xs)))
        groups = {}
        for r in done:
            groups.setdefault((round(r[key], 3), round(r['rr'], 3)), []).append(r)
        step = 0.022 * (hi_x - lo_x)
        pos, placed = {}, []
        for r in sorted(done, key=lambda r: (r[key], r['rr'], r['crowned'])):
            for k in (0, 1, -1, 2, -2, 3, -3, 4, -4):
                px = r[key] + k * step
                if all(abs(px - qx) > 0.9 * step or abs(r['rr'] - qy) > 0.03 for qx, qy in placed):
                    break
            pos[r['bot']] = (px, r['rr'])
            placed.append((px, r['rr']))
        ax.axhline(0.5, color=AXIS, lw=0.9, zorder=1)
        if rate:
            ax.axvline(0.5, color=AXIS, lw=0.9, zorder=1)
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
            if rate:
                lo, hi = wilson(r['lw'] + 0.5 * r['lt'], r['ln'])
                ax.plot([lo, hi], [py, py], color=col[r['bot']], alpha=0.35, lw=1.4, zorder=2, solid_capstyle='round')
            else:
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
    axes[0].set_ylabel(f'win rate vs AlexT101_usqualsbot on the replica ({nmaps} games each: {nmaps} maps as team A)',
                       color=INK2, fontsize=9.5)
    fig.suptitle(f"Do local champions do better on the replica?  {st['n']} champions on all {nmaps} early maps",
                 x=0.012, y=0.985, ha='left', color=INK, fontsize=13, fontweight='bold')
    fig.text(0.012, 0.948, 'Shade = crowning order, 11-21 to 11-23 (light = earliest, dark = latest).  Dashed = least squares.  '
             'Lines at 50% = even.\nWhiskers = 95% Wilson intervals of the win rates (both axes on the left), ±1.96 SE of the '
             'rating on the right.', color=INK2, fontsize=9, ha='left', va='top', linespacing=1.5)
    fig.tight_layout(rect=(0, 0, 1, 0.89))
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
    out = os.path.join(HERE, 'champ-corr21.png')
    fig.savefig(out, dpi=150, facecolor=SURF)
    print('wrote', out)


if __name__ == '__main__':
    main()
