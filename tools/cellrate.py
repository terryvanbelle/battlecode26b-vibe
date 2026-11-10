#!/usr/bin/env python3
"""cellrate.py MINID [EXCL_RANGES] [CAND_RANGES]: replica set-3 cells (opponent, map, our seat) over every
/dev/shm/rp/<id>/match.json with id >= MINID against a set-3 opponent on its map triple (since 11-09 these are zdb
derivatives, each a perturbed draw of zdb's cells): draws, our wins, base rate, and zdb's anchor result (21016-21023,
21036-21043). EXCL_RANGES (a candidate under test) stay out of the rates. With CAND_RANGES: the candidate's wins minus
the base rates of its cells, with a binomial sd and z, and its net against the anchor draw.
Why (2026-11-10): zdb's anchor draw wins 28 cells where the derivatives' rates expect 26.6, so a candidate that
re-rolls every cell expects net -1.4 against the anchors; the rate score removes that bias.
Example: python3 tools/cellrate.py 22000 23984-24013 23984-23987,24000-24013"""
import json, os, sys, collections
ANC = list(range(21016, 21024)) + list(range(21036, 21044))
excl = set()
for part in (sys.argv[2].split(',') if len(sys.argv) > 2 else []):
    a, _, b = part.partition('-'); excl.update(range(int(a), int(b or a) + 1))
def cells(i):
    p = '/dev/shm/rp/%d/match.json' % i
    if not os.path.exists(p): return []
    m = json.load(open(p)); out = []
    for g in m['games']:
        us = 'A' if g['team_a'] == 'vibe26b' else 'B'
        opp = g['team_b'] if us == 'A' else g['team_a']
        out.append(((opp, g['map'], us), g['winner_team'] == 'vibe26b', tuple(sorted(m['maps']))))
    return out
anc = {}; trip = {}
for i in ANC:
    for c, w, t in cells(i): anc[c] = w; trip[c[0]] = t
R = collections.defaultdict(list)
for d in os.listdir('/dev/shm/rp'):
    if not d.isdigit() or int(d) < int(sys.argv[1]) or int(d) in excl: continue
    for c, w, t in cells(int(d)):
        if c in anc and trip.get(c[0]) == t: R[c].append(w)
for c in sorted(anc, key=lambda c: (sum(R[c]) / max(1, len(R[c])))):
    n = len(R[c]); w = sum(R[c])
    print('%-28s %-26s %s anchor %s draws %2d wins %2d rate %.2f' % (c[0][:28], c[1][:26], c[2], 'W' if anc[c] else 'L', n, w, w / max(1, n)))
# with CAND_RANGES as argv[3]: the candidate's wins minus the cells' base rates (excluding its own draws), and z
if len(sys.argv) > 3:
    ids = []
    for part in sys.argv[3].split(','):
        a, _, b = part.partition('-'); ids += range(int(a), int(b or a) + 1)
    s = v = 0.0; n = 0; aw = 0
    for i in ids:
        for c, w, t in cells(i):
            if c not in anc: continue
            r = sum(R[c]) / max(1, len(R[c])); s += w - r; v += r * (1 - r); n += 1; aw += w - anc[c]
            if abs(w - r) > 0.5: print('  surprise %-26s %-24s %s %s rate %.2f' % (c[0][:26], c[1][:24], c[2], 'W' if w else 'L', r))
    print('candidate cells %d: net vs anchor %+d, wins - rate %+.2f, sd %.2f, z %+.2f' % (n, aw, s, v ** 0.5, s / max(1e-9, v ** 0.5)))
    tot = sum(sum(R[c]) / max(1, len(R[c])) for c in anc); print('anchor wins %d, rate-expected wins %.2f' % (sum(anc.values()), tot))
