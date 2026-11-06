#!/usr/bin/env python3
"""Mechanism run, candidate against control on the cells they share (opponent, map, side).
  mech_cmp.py CAND_RESULTS.jsonl CAND CTRL_RESULTS.jsonl CTRL [FIELD ...]
FIELDs are our team's per-team stats in results.jsonl (catTrapHits, catDmgTrap, kingsFormed, deathsKing, ...); the
default set covers z0h's and z0j's falsifiers. Also printed: wins, our cat damage (catDmgA/B) and kings alive at the
end (numKingsA/B). One line a cell where any printed number differs, then the totals."""
import json, sys

DEFAULT = ['catTraps', 'catTrapHits', 'catDmgTrap', 'catDmgBite', 'kingsFormed', 'deathsKing', 'transferAmt']


def load(path, me):
    out = {}
    for line in open(path):
        r = json.loads(line)
        if me not in (r['a'], r['b']):
            continue
        side = 'A' if r['a'] == me else 'B'
        opp = r['b'] if side == 'A' else r['a']
        out[(opp, r['map'], side)] = (r, side)
    return out


def row(r, side, fields):
    t = r.get(side) or {}
    v = {f: t.get(f) or 0 for f in fields}
    v['win'] = int(r['winner'] == side)
    v['catDmg'] = r.get('catDmg' + side) or 0
    v['kingsEnd'] = r.get('numKings' + side) or 0
    v['rounds'] = r.get('rounds') or 0
    return v


def main():
    cres, cand, kres, ctrl = sys.argv[1:5]
    fields = sys.argv[5:] or DEFAULT
    a, b = load(cres, cand), load(kres, ctrl)
    keys = sorted(set(a) & set(b))
    cols = ['win', 'rounds', 'catDmg', 'kingsEnd'] + fields
    tot = {cand: dict.fromkeys(cols, 0), ctrl: dict.fromkeys(cols, 0)}
    print('cell |', ' '.join(cols))
    for k in keys:
        va, vb = row(*a[k], fields), row(*b[k], fields)
        for c in cols:
            tot[cand][c] += va[c]
            tot[ctrl][c] += vb[c]
        if va != vb:
            print('%s %s %s |' % (k[0][:8], k[1][:18], k[2]), ' '.join('%s/%s' % (va[c], vb[c]) for c in cols))
    print('cells', len(keys))
    for me in (cand, ctrl):
        print('%-6s' % me, ' '.join('%s %s' % (c, tot[me][c]) for c in cols))


if __name__ == '__main__':
    main()
