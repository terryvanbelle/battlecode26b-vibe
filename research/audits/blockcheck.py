#!/usr/bin/env python3
"""Replica block falsifiers from field_report games rows (JSONL), candidate block against the twin's set.
  blockcheck.py ROWS.jsonl[,ROWS2.jsonl] CAND_IDS TWIN_IDS
IDS: comma-separated match ids or LO-HI ranges. Rows are deduped by (match, game). Prints per side of the comparison:
games, transfer a game (xUs; the 0.9x floor), our cat-damage share in the r2000 points games (z0c's falsifier: summed
over the games; the mean of per-game shares beside it), kings alive at the end of those games, and wins by king kill
(z0l's)."""
import json, sys


def ids(spec):
    out = set()
    for part in spec.split(','):
        if '-' in part:
            lo, hi = part.split('-')
            out.update(range(int(lo), int(hi) + 1))
        elif part:
            out.add(int(part))
    return out


def stats(rows):
    n = len(rows)
    pts = [r for r in rows if r.get('type') == 'MORE_POINTS' and (r.get('rounds') or 0) >= 2000]
    share = [r['catUs'] / (r['catUs'] + r['catThem']) for r in pts if (r.get('catUs') or 0) + (r.get('catThem') or 0)]
    return {
        'games': n,
        'won': sum(1 for r in rows if r.get('won')),
        'xUs/game': round(sum(r.get('xUs') or 0 for r in rows) / max(1, n)),
        'ptsGames': len(pts),
        'catShare': round(sum(r['catUs'] for r in pts) / max(1, sum(r['catUs'] + r['catThem'] for r in pts)), 3),
        'catShareMean': round(sum(share) / max(1, len(share)), 3),
        'kingsEndPts': round(sum(r.get('kUs') or 0 for r in pts) / max(1, len(pts)), 2),
        'kingKillWins': sum(1 for r in rows if r.get('won') and r.get('type') == 'RATKING_DESTROYED'),
    }


def main():
    paths, cand, twin = sys.argv[1].split(','), ids(sys.argv[2]), ids(sys.argv[3])
    seen, sel = set(), {'cand': [], 'twin': []}
    for line in (l for p in paths for l in open(p)):
        r = json.loads(line)
        k = (r['match'], r['game'])
        if k in seen:
            continue
        seen.add(k)
        if r['match'] in cand:
            sel['cand'].append(r)
        elif r['match'] in twin:
            sel['twin'].append(r)
    for side in ('cand', 'twin'):
        print(side, ' '.join('%s %s' % kv for kv in stats(sel[side]).items()))


if __name__ == '__main__':
    main()
