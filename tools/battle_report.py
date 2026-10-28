#!/usr/bin/env python3
"""Combat analysis of one game from a ReplayDump tsv directory (or a .bc26, dumped on the fly).

  battle_report.py REPLAY.bc26|TSVDIR [--lo R] [--hi R] [--step 10] [--json]

Sections (team 1 = A, team 2 = B; rounds lo..hi):
  front    per step rounds: rats alive, total HP, rats within dist2 20 of an enemy, mean Chebyshev distance to own king
  hploss   HP lost by rats, by cause seen that round or the round before (bite, throw, trap, combinations, other)
  grabs    ratnaps (a grab of another rat, not a thrown rat's own landing) and what became of each: thrown after N
           rounds, carrier died, released; and whether the victim was weaker than the carrier and could see it
  throws   damage the thrown rat took (42 = point-blank on the throw turn, 10 = landed on nothing)
  xfer     cheese transferred and rat pickups per step rounds
"""
import argparse, collections, json, os, sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import game_report  # noqa: E402

# the replay's direction ordinal (FlatHelpers): 0 centre, then west and counter-clockwise; not the Java enum order
DIRS = [None, (-1, 0), (-1, -1), (0, -1), (1, -1), (1, 0), (1, 1), (0, 1), (-1, 1)]


def load(path):
    d = path if os.path.isdir(path) else game_report.dump(path)
    turns = list(game_report.rows(os.path.join(d, 'g0.turns.tsv')))
    acts = list(game_report.rows(os.path.join(d, 'g0.actions.tsv')))
    return turns, acts


def sees(vx, vy, vdir, tx, ty):
    """A baby at (vx, vy) facing replay direction ordinal vdir sees (tx, ty): dot >= |cross| and dist2 <= 20."""
    try:
        fx, fy = DIRS[int(vdir)]
    except (ValueError, IndexError, TypeError):
        return True
    dx, dy = tx - vx, ty - vy
    return dx * dx + dy * dy <= 20 and dx * fx + dy * fy >= abs(dx * fy - dy * fx)


def front(turns, lo, hi, step):
    pos = collections.defaultdict(list)
    king0 = {}
    for t in turns:
        if t['type'] == 'KING' and t['team'] not in king0:
            king0[t['team']] = (int(t['x']), int(t['y']))
        if t['type'] == 'RAT':
            pos[int(t['round'])].append((t['team'], int(t['x']), int(t['y']), int(t['hp'])))
    out = []
    for r in range(lo, hi + 1, step):
        m = pos.get(r, [])
        row = {'round': r}
        for tm in '12':
            mine = [v for v in m if v[0] == tm]
            en = [v for v in m if v[0] != tm]
            kx, ky = king0.get(tm, (0, 0))
            row[tm] = dict(n=len(mine), hp=sum(v[3] for v in mine),
                           front=sum(1 for v in mine if any((v[1] - e[1]) ** 2 + (v[2] - e[2]) ** 2 <= 20 for e in en)),
                           dk=round(sum(max(abs(v[1] - kx), abs(v[2] - ky)) for v in mine) / max(1, len(mine)), 1))
        out.append(row)
    return out


def hploss(turns, acts, lo, hi):
    team = {t['id']: t['team'] for t in turns}
    hp = collections.defaultdict(dict)
    for t in turns:
        if t['type'] == 'RAT':
            hp[t['id']][int(t['round'])] = int(t['hp'])
    ev = collections.defaultdict(list)
    for a in acts:
        r = int(a['round'])
        if a['action'] == 'bite' and a['b'] == 'RAT':
            ev[(a['a'], r)].append('bite')
        elif a['action'] == 'damage':
            ev[(a['a'], r)].append('throw')
        elif a['action'] == 'trigger' and a['team'] != '0':
            ev[(a['id'], r)].append('trap')
    out = {'1': collections.Counter(), '2': collections.Counter()}
    for rid, h in hp.items():
        rs = sorted(h)
        for i in range(1, len(rs)):
            r = rs[i]
            if not lo <= r <= hi:
                continue
            dl = h[rs[i - 1]] - h[r]
            if dl > 0 and team.get(rid) in out:
                k = '+'.join(sorted(set(ev.get((rid, r), []) + ev.get((rid, r - 1), [])))) or 'other'
                out[team[rid]][k] += dl
    return {tm: dict(c) for tm, c in out.items()}


def grabs(turns, acts, lo, hi):
    hist = collections.defaultdict(dict)
    last_round = collections.defaultdict(int)
    for t in turns:
        hist[t['id']][int(t['round'])] = t
        last_round[t['id']] = max(last_round[t['id']], int(t['round']))
    by_id = collections.defaultdict(list)
    for a in acts:
        by_id[a['id']].append(a)

    def state(i, r):
        for k in range(r, r - 3, -1):
            if k in hist[i]:
                return hist[i][k]
        return None

    out = {}
    for tm in '12':
        fate, how = collections.Counter(), collections.Counter()
        for a in acts:
            if a['action'] != 'nap' or a['team'] != tm or a['a'] == a['id']:
                continue
            r0 = int(a['round'])
            if not lo <= r0 <= hi:
                continue
            res = None
            for b in by_id[a['id']]:
                rb = int(b['round'])
                if r0 < rb <= r0 + 11 and b['action'] in ('throw', 'nap'):
                    res = f"{b['action']}+{rb - r0}"
                    break
            if res is None:
                res = 'carrier died' if last_round[a['id']] <= r0 + 11 else 'released'
            fate[res] += 1
            car, vic = state(a['id'], r0), state(a['a'], r0 - 1)
            if car and vic:
                s = sees(int(vic['x']), int(vic['y']), vic['dir'], int(car['x']), int(car['y']))
                how[('front' if s else 'behind') + ('/weaker' if int(vic['hp']) < int(car['hp']) else '')] += 1
        out[tm] = {'n': sum(fate.values()), 'fate': dict(fate), 'how': dict(how)}
    return out


def throws(acts, lo, hi):
    dmg = collections.defaultdict(list)
    for a in acts:
        if a['action'] == 'damage' and a['a'] == a['id']:
            dmg[a['a']].append((int(a['round']), int(a['b'])))
    out = {'1': collections.Counter(), '2': collections.Counter()}
    for a in acts:
        if a['action'] != 'throw' or a['team'] not in out or not lo <= int(a['round']) <= hi:
            continue
        r = int(a['round'])
        hit = [d for (rr, d) in dmg[a['a']] if r <= rr <= r + 4]
        out[a['team']][hit[0] if hit else 10] += 1
    return {tm: dict(sorted(c.items())) for tm, c in out.items()}


def xfer(acts, lo, hi, step):
    x, p = collections.Counter(), collections.Counter()
    for a in acts:
        r = int(a['round'])
        if not lo <= r <= hi:
            continue
        w = r // step * step
        if a['action'] == 'transfer' and (a['b'] or '').isdigit():
            x[(w, a['team'])] += int(a['b'])
        if a['action'] == 'pickup' and a['type'] == 'RAT':
            p[(w, a['team'])] += 1
    ws = sorted(set(k[0] for k in list(x) + list(p)))
    return [dict(round=w, xfer1=x[(w, '1')], xfer2=x[(w, '2')], pick1=p[(w, '1')], pick2=p[(w, '2')]) for w in ws]


def report(path, lo=0, hi=2000, step=10):
    turns, acts = load(path)
    hi = min(hi, max(int(t['round']) for t in turns))
    return dict(front=front(turns, max(lo, 1), hi, step), hploss=hploss(turns, acts, lo, hi),
                grabs=grabs(turns, acts, lo, hi), throws=throws(acts, lo, hi), xfer=xfer(acts, lo, hi, max(step, 50)))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('replay')
    ap.add_argument('--lo', type=int, default=0)
    ap.add_argument('--hi', type=int, default=2000)
    ap.add_argument('--step', type=int, default=10)
    ap.add_argument('--json', action='store_true')
    o = ap.parse_args()
    r = report(o.replay, o.lo, o.hi, o.step)
    if o.json:
        print(json.dumps(r))
        return
    print('round   ' + '   '.join(f'T{tm}: n   hp front dK' for tm in '12'))
    for f in r['front']:
        print(f"{f['round']:5d}  " + '   '.join(f"{f[tm]['n']:5d} {f[tm]['hp']:5d} {f[tm]['front']:4d} {f[tm]['dk']:4.1f}" for tm in '12'))
    for tm in '12':
        h = r['hploss'][tm]
        print(f"T{tm} hp lost {sum(h.values())}: " + ', '.join(f'{k} {v}' for k, v in sorted(h.items(), key=lambda kv: -kv[1])))
    for tm in '12':
        g = r['grabs'][tm]
        print(f"T{tm} grabs {g['n']}: how {g['how']} fate {g['fate']}")
    for tm in '12':
        print(f"T{tm} throw damage to the thrown rat: {r['throws'][tm]}")
    for x in r['xfer']:
        print(f"{x['round']:5d} xfer {x['xfer1']:5d} {x['xfer2']:5d}  rat pickups {x['pick1']:3d} {x['pick2']:3d}")


if __name__ == '__main__':
    main()
