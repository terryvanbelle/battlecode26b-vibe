"""Scatter of local screen advantage vs replica advantage for candidates that had both (from progress/attempts.csv,
hand-checked 11-19). Left: z-line, s6/s7 screens (44-48 local cells matched to replica cells) vs replica block
(48 cells). Right: xr-line, full archetype screen (1628 cells) vs replica panel (132 games, vs xk6 on the same cells)."""
import matplotlib; matplotlib.use('Agg')
import matplotlib.pyplot as plt, numpy as np
z = {'z5i': (2, -2), 'zc4': (3, 4), 'zc8': (1, -1), 'zdk': (6, -6), 'zew': (3, -4), 'zgh': (1, -2), 'zgj': (1, -3),
     'zgt': (0, -1), 'zho': (-2, -1), 'zht': (2, -1), 'zhw': (2, 0), 'zii': (3, -2), 'zil': (1, -1), 'ziv': (1, -1),
     'zj3': (4, 4), 'zj8': (6, -3), 'zja': (4, -1)}
x = {'xr8': (-11, -9), 'xr13': (3, -2), 'xr16': (16, -1)}
INK, INK2, GRID, SURF = '#0b0b0b', '#52514e', '#e4e3df', '#fcfcfb'
fig, axes = plt.subplots(1, 2, figsize=(11, 4.8), facecolor=SURF)
for ax, d, col, title, xl, yl in (
        (axes[0], z, '#2a78d6', 'z-line (11-06 to 11-11)', 'local net, s6/s7 screen (44-48 cells)', 'replica net, block (48 cells)'),
        (axes[1], x, '#eb6834', 'xr-line vs xk6 (11-19)', 'local net, archetype screen (1628 cells)', 'panel net vs xk6 (132 games)')):
    ax.set_facecolor(SURF)
    pts = np.array(list(d.values()), float)
    # stack duplicates sideways a little so every candidate shows
    seen = {}
    for name, (lx, ry) in d.items():
        k = (lx, ry); n = seen.get(k, 0); seen[k] = n + 1
        px = lx + 0.18 * n
        ax.scatter(px, ry, s=70, color=col, edgecolor=SURF, linewidth=2, zorder=3)
        ax.annotate(name, (px, ry), xytext=(5, 4 - 10 * n), textcoords='offset points', fontsize=8, color=INK2)
    lo = min(pts.min(), -1) - 1.5; hi = max(pts.max(), 1) + 1.5
    ax.plot([lo, hi], [lo, hi], color=INK2, lw=1, ls='--', zorder=1)
    ax.text(hi, hi, ' y = x', color=INK2, fontsize=8, va='bottom', ha='right')
    ax.axhline(0, color=GRID, lw=1, zorder=0); ax.axvline(0, color=GRID, lw=1, zorder=0)
    r = np.corrcoef(pts[:, 0], pts[:, 1])[0, 1]
    ax.set_title(f'{title}: n {len(pts)}, r = {r:+.2f}', color=INK, fontsize=11, loc='left')
    ax.set_xlabel(xl, color=INK2, fontsize=9); ax.set_ylabel(yl, color=INK2, fontsize=9)
    for s in ax.spines.values(): s.set_color(GRID)
    ax.tick_params(colors=INK2, labelsize=8)
    pos = (pts[:, 0] > 0).sum(); posrep = ((pts[:, 0] > 0) & (pts[:, 1] > 0)).sum()
    print(title, 'n', len(pts), 'r %.2f' % r, 'local>0', pos, 'of which replica>0', posrep, 'mean replica', pts[:, 1].mean().round(2))
fig.suptitle('Local screen advantage vs replica advantage (candidates with both)', color=INK, fontsize=12, x=0.01, ha='left')
fig.tight_layout()
fig.savefig('progress/local_vs_panel.png', dpi=130, facecolor=SURF)
