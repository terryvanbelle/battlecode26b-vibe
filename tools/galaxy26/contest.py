#!/usr/bin/env python3
"""Our team (vibe26b) as a contestant on the bc26 replica, through the site's public API only (tools/galaxy26/client.py),
as a team on the official site would act. Standard library only; run on the driver (or the VM).

  python3 tools/galaxy26/contest.py register              sign up user vibe26b + team vibe26b (auto-accept on); writes
                                                           ~/.bc26b-galaxy-team (line 1 user, line 2 password; mode 600)
  python3 tools/galaxy26/contest.py submit [PKG] [-m TEXT] [--no-wait]
        zip the Java package directory PKG (default bots/ratbot; package = its name) as the scaffold's zipForSubmit
        does (<package>/*.java) and upload it; waits for the replica's compile (javac 21 + the engine's Verifier:
        accepted / rejected, with the compile log's tail) unless --no-wait
  python3 tools/galaxy26/contest.py ladder               rankings, our team marked
  python3 tools/galaxy26/contest.py scrim [--n 1] [--band 3] [--maps 3] [--vs NAME,...] [--dry-run]
        request N scrimmages against teams rated just above us (ranked: galaxy picks 3 random maps, alternating
        order) and just below us (unranked on --maps random maps: galaxy refuses ranked requests downward),
        alternating above/below, nearest first; skips opponents we already have a match waiting against.
        --vs: one scrimmage against each named team instead (exact name or a unique substring); NAME@+ makes us team
        A in every game, NAME@- team B (galaxy's player order, unranked only; otherwise '?': random, sides alternating)
        --panel FILE: unranked, on the maps FILE holds for that opponent; an opponent not in FILE gets a random draw
        that is written there, so a candidate's panel and the incumbent's control play the same maps in the same order
        --queue FILE: requests the hourly limit refuses (HTTP 429) are appended to FILE, tagged with the build that is
        the active submission now, for `drain`
  python3 tools/galaxy26/contest.py drain FILE
        send FILE's queued requests (lines: build, NAME@ORDER, panel file, maps) in order until the hourly limit
        refuses one; only lines tagged with the active submission's build are sent (a block must be played by the
        build it measures); sent lines leave FILE
  python3 tools/galaxy26/contest.py matches [--pages 1] [--all]   our scrimmages (or everyone's), newest first
  python3 tools/galaxy26/contest.py fetch [ID ...] [--new] [--out DIR]
        download match replays: DIR/<id>/replay.bc26 (every game of the match; open it in the client) + match.json
        (per game: map, team A/B, reversed, winner, rounds, win type, read from the replay) (default
        ~/projects/vibe/bc26b-galaxy-replays); --new = every finished match of ours not downloaded yet
  python3 tools/galaxy26/contest.py results [--pages 3]  per-opponent W-L over our finished matches (games)
  python3 tools/galaxy26/contest.py compare CTRL CAND [--dir DIR]
        two downloaded panel blocks (match ids, e.g. 420-425 and 432-437) cell by cell (opponent, map): our result,
        start side (A/B), rounds, win type in each; gained / lost / net and t = net / sqrt(D). Local files only
  python3 tools/galaxy26/contest.py pool [--partial] [--k 2] CONTROL POOLFILE BLOCK [BLOCK...]
        candidate blocks scored against the pooled history of earlier blocks (CONTROL + POOLFILE's blocks, one per
        line, '#' comments; a block also named as a candidate is left out of the pool): wins - E over sqrt(V), side-
        blind (cell = opponent, map) and side-aware (cell = opponent, map, our side). A candidate block with a match
        not downloaded (finished) is refused (exit 3) unless --partial. Local files only
  python3 tools/galaxy26/contest.py paired [--partial] [--graded] [--studied ...] REF BLOCK [BLOCK...]
        candidate blocks against the champion's own games on the same cells (REF: match ids, or a file of them one
        block a line, '#' comments), each (opponent, map, side) cell once: d = candidate mean - champion mean, t = net
        / sqrt(sum d^2) (TRAINING_ALGORITHM, paired fresh-cell rule). Refused (exit 3) while a candidate match is
        not downloaded or a candidate cell has no reference game, unless --partial. --graded: a game's result is its
        graded score (tools/field_report.py: 1/0 for a game decided by kings, else our points share) instead of won,
        from <dir>/<id>/graded.json while it is current, else graded from the replay and stored; a candidate match
        that cannot be graded is refused like one not downloaded. Local files only
A "side" here is the start position our team plays from in a game: A or B of the map (in the odd games of an
alternate-order match the engine reverses the map, so team A starts at B's position).
"""
import argparse
import functools
import json
import math
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(os.path.dirname(HERE))
sys.path.insert(0, HERE)
import client as cl  # noqa: E402
import replay_merge  # noqa: E402

TEAM = os.environ.get('GALAXY26_TEAM', 'vibe26b')
TEAM_FILE = os.path.expanduser(os.environ.get('GALAXY26_TEAM_FILE', '~/.bc26b-galaxy-team'))
REPLAYS = os.path.expanduser(os.environ.get('GALAXY26_REPLAYS', '~/projects/vibe/bc26b-galaxy-replays'))
ABOUT = 'battlecode26b-vibe (our team).'
DEFAULT_BOT = os.path.join(REPO, 'bots', 'ratbot')


def write_team_file(acc, path=TEAM_FILE):
    a = acc[TEAM]
    fd = os.open(path + '.tmp', os.O_WRONLY | os.O_CREAT | os.O_TRUNC, 0o600)
    with os.fdopen(fd, 'w') as fh:
        fh.write(f'{a["user"]}\n{a["password"]}\n')
    os.replace(path + '.tmp', path)


def match_json(m, raw):
    """The per-game record of a downloaded match: siarnaq's match (participants in player order: A then B, maps,
    alternate_order) + what the replay says of each game (replay_merge.describe)."""
    d = replay_merge.describe(raw)
    ps = sorted(m.get('participants') or [], key=lambda p: p.get('player_index', 0))
    names = [p.get('teamname') for p in ps] + [None, None]
    games = []
    for i, g in enumerate(d['games']):
        games.append({'game': i + 1, 'map': g['map'], 'team_a': names[0], 'team_b': names[1],
                      'reversed': bool(m.get('alternate_order')) and i % 2 == 1,
                      'winner_team': d['teams'].get(g['winner']) or {1: names[0], 2: names[1]}.get(g['winner']),
                      'rounds': g['rounds'], 'reason': g['win_type']})
    return {'match': m['id'], 'is_ranked': m.get('is_ranked'), 'alternate_order': m.get('alternate_order'),
            'maps': m.get('maps'), 'teams': names[:2], 'games': games}


def our_view(g, team):
    """(opponent, our start side 'A'/'B', won) of one game record."""
    we_a = g['team_a'] == team
    side = 'A' if we_a != g['reversed'] else 'B'
    return (g['team_b'] if we_a else g['team_a']), side, g['winner_team'] == team


def plan_scrims(ladder, my_id, n, band, waiting_ids=()):
    """[(team, ranked)] for n requests: above (ranked) and below (unranked) alternating, nearest first, skipping
    teams in waiting_ids; when one side runs out the other continues."""
    above, below = cl.neighbours(ladder, my_id, band)
    above = [t for t in above if t['id'] not in waiting_ids]
    below = [t for t in below if t['id'] not in waiting_ids]
    out, i = [], 0
    while len(out) < n and (above or below):
        side = above if (i % 2 == 0 and above) or not below else below
        t = side.pop(0)
        out.append((t, side is above))
        i += 1
    return out


def active_build(s, ep):
    """The build (snapshot codename) of our newest accepted submission, which plays every new match."""
    sys.path.insert(0, os.path.join(REPO, 'tools'))
    import games
    for x in s.pages(f'/api/compete/{ep}/submission/', limit=1):
        if x.get('accepted'):
            return games.codename(x.get('package'), x.get('description'))
    return None


def read_queue(path):
    """[(build, spec, panel file, maps)] of a queue file."""
    out = []
    if os.path.exists(path):
        for line in open(path):
            p = line.rstrip('\n').split('\t')
            if len(p) == 4 and not line.startswith('#'):
                out.append((p[0], p[1], p[2], int(p[3])))
    return out


def write_queue(path, rows):
    with open(path + '.tmp', 'w') as f:
        for r in rows:
            f.write('\t'.join(map(str, r)) + '\n')
    os.replace(path + '.tmp', path)


def send(c, s, ep, my_id, specs, panel_path, nmaps, dry_run=False):
    """Request one scrimmage per spec (NAME, NAME@+ or NAME@-; with a panel file, on its maps for that opponent).
    Returns the specs the hourly limit refused (HTTP 429), in order: every spec after the first refusal is kept too."""
    ladder = cl.rankings(c)
    pairs = [split_order(v) for v in specs]
    plan = named(ladder, my_id, [n for n, _ in pairs])
    maps = cl.public_maps(c)
    import random
    rnd = random.Random()
    panel = {}
    if panel_path and os.path.exists(panel_path):
        with open(panel_path) as fh:
            panel = json.load(fh)
    left = []
    for spec, (t, ranked), (_, order) in zip(specs, plan, pairs):
        if left:
            left.append(spec)
            continue
        tag = t['name'] + ('' if order == '?' else '@' + order)
        if order != '?':
            ranked = False      # galaxy: "Ranked matches must use shuffled order"
        if panel_path:
            ranked, mp = False, panel_maps(panel, t['name'], maps, nmaps, rnd)
        else:
            mp = [] if ranked else rnd.sample(maps, nmaps)
        if dry_run:
            print(f'would request {"ranked" if ranked else "unranked"} vs {tag} (rated {t["rating"] or 0:.0f}) {mp}')
            continue
        try:
            r = cl.request(s, t['id'], ranked, mp, order, ep)
            print(f'request {r["id"]}: vs {tag} ({"ranked" if ranked else "unranked"}, rated {t["rating"] or 0:.0f})')
        except cl.ApiError as e:
            if e.code == 429:
                print(f'hourly limit: vs {tag} and later ones not sent')
                left.append(spec)
            else:
                print(f'refused: vs {tag}: {e}')
    if panel_path and not dry_run:
        with open(panel_path, 'w') as f:
            json.dump(panel, f, indent=1)
            f.write('\n')   # as committed: a block leaves the panel file unchanged
    return left


def drain_rows(q, build, send_group):
    """(queue, rows left, number sent): the rows tagged `build` go to send_group(specs, panel file, maps) one panel file
    at a time, in queue order, until a group comes back with specs the hourly limit refused; other builds' rows wait."""
    mine = [r for r in q if r[0] == build]
    left_rows = []
    for pf in dict.fromkeys(r[2] for r in mine):   # panel files in queue order
        rows = [r for r in mine if r[2] == pf]
        if left_rows:
            left_rows += rows
            continue
        left = send_group([r[1] for r in rows], pf, rows[0][3])
        if left:
            left_rows += rows[len(rows) - len(left):]   # send() keeps the tail from its first refusal on
    return q, [r for r in q if r[0] != build] + left_rows, len(mine) - len(left_rows)


def named(ladder, my_id, names):
    """[(team, ranked)] for the teams named (exact name, else a unique substring); ranked only when rated above us."""
    me = next(t for t in ladder if t['id'] == my_id)
    mine = me['rating'] or 0
    out = []
    for n in names:
        hits = [t for t in ladder if t['name'] == n] or [t for t in ladder if n in t['name'] and t['id'] != my_id]
        if len(hits) != 1:
            raise SystemExit(f'--vs {n!r}: {len(hits)} teams match')
        t = hits[0]
        out.append((t, (t['rating'] or 0) > mine))
    return out


def split_order(spec):
    """'NAME@+' -> ('NAME', '+'), 'NAME@-' -> ('NAME', '-'), 'NAME' -> ('NAME', '?')."""
    return (spec[:-2], spec[-1]) if spec[-2:] in ('@+', '@-') else (spec, '?')


def panel_maps(panel, name, maps, n, rnd):
    """The maps a panel plays against `name`: the recorded draw, else a random draw of n, recorded."""
    if name not in panel:
        panel[name] = rnd.sample(maps, n)
    return panel[name]


def block_ids(spec):
    """'420-425' or '420,421,430-431' -> [420, ..., 425]."""
    out = []
    for part in spec.split(','):
        a, _, b = part.partition('-')
        out += range(int(a), int(b or a) + 1)
    return out


def block_cells(ids, root=REPLAYS, team=TEAM):
    """({(opponent, map): (won, side, rounds, reason, match id)}, [ids not downloaded]) over the matches' games."""
    cells, missing = {}, []
    for i in ids:
        f = os.path.join(root, str(i), 'match.json')
        if not os.path.exists(f):
            missing.append(i)
            continue
        with open(f) as fh:
            games = json.load(fh)['games']
        for g in games:
            opp, side, won = our_view(g, team)
            cells[(opp, g['map'])] = (won, side, g['rounds'], g['reason'], i)
    return cells, missing


def block_cells_all(ids, root=REPLAYS, team=TEAM):
    """Every game of the matches as a cell (opponent, map, our side, won), so a block that plays a map on both sides
    keeps both."""
    out = []
    for i in ids:
        f = os.path.join(root, str(i), 'match.json')
        if os.path.exists(f):
            with open(f) as fh:
                gs = json.load(fh)['games']
            for g in gs:
                opp, side, won = our_view(g, team)
                out.append((opp, g['map'], side, int(won)))
    return out


@functools.lru_cache(maxsize=None)
def field_report():
    """(tools/field_report.py, the current dumper hash), once per process: imported here, as only --graded needs the
    dump tools."""
    sys.path.insert(1, os.path.join(REPO, 'tools'))
    import field_report as fr
    return fr, fr.dumper()


def graded_cells(ids, root=REPLAYS, team=TEAM, bad=None):
    """block_cells_all with each game's graded score in place of won (tools/field_report.py graded_cached: its stored
    graded.json while the replay, the dumper and the score version are the same, else graded now). A match that
    cannot be graded adds no cells and goes into `bad` ({id: error})."""
    bad = {} if bad is None else bad
    try:
        fr, dh = field_report()
    except RuntimeError as e:     # the dumper does not build (field_report's GR.DumpError)
        bad.update((i, str(e).strip().splitlines()[0][:200]) for i in ids)
        return []
    out = []
    for i in ids:
        f = os.path.join(root, str(i), 'match.json')
        if not os.path.exists(f):
            continue
        try:
            with open(f) as fh:
                gs = json.load(fh)['games']
            sc = {x['game']: x for x in fr.graded_cached(i, root, team, dh)}
            cells = []
            for j, g in enumerate(gs):
                opp, side, won = our_view(g, team)
                x = sc[j]
                if (x['map'], x['side'], x['won']) != (g['map'], side, won):
                    raise ValueError(f'game {j}: graded {x["map"]} {x["side"]} won {x["won"]}, match.json {g["map"]} '
                                     f'{side} won {won}')
                cells.append((opp, g['map'], side, x['score']))
            out += cells
        except (fr.GR.DumpError, OSError, ValueError, KeyError) as e:
            bad[i] = str(e).strip().splitlines()[0][:200]
    return out


def compare_blocks(ctrl, cand):
    """Cells of both blocks in order, each (key, ctrl cell or None, cand cell or None, '+' / '-' / ''); then gained,
    lost and t = net / sqrt(gained + lost) (0 when no cell differs)."""
    rows, gained, lost = [], 0, 0
    for k in sorted(set(ctrl) | set(cand)):
        a, b = ctrl.get(k), cand.get(k)
        mark = ''
        if a and b and a[0] != b[0]:
            mark = '+' if b[0] else '-'
            gained += b[0]
            lost += a[0]
        rows.append((k, a, b, mark))
    d = gained + lost
    return rows, gained, lost, (gained - lost) / d ** 0.5 if d else 0.0


def compare_cmd(a):
    ctrl, m1 = block_cells(block_ids(a.ctrl), a.dir)
    cand, m2 = block_cells(block_ids(a.cand), a.dir)
    if m1 or m2:
        print(f'not downloaded (contest.py fetch): {m1 + m2}')
    cell = lambda c: f"{'W' if c[0] else 'L'} {c[1]} {c[2]:4} {c[3][:12]:12} m{c[4]}" if c else '-'
    rows, g, l, t = compare_blocks(ctrl, cand)
    for (opp, mp), x, y, mark in rows:
        print(f'{opp[:32]:32} {mp:24} ctrl {cell(x):30} cand {cell(y):30} {mark}')
    print(f'gained {g} lost {l} D {g + l} net {g - l} t {t:.2f}; ctrl wins {sum(c[0] for c in ctrl.values())}/{len(ctrl)}, '
          f'cand wins {sum(c[0] for c in cand.values())}/{len(cand)}')
    return 0


def pool_score(prior, cand, k=2.0):
    """Candidate cells scored against prior blocks' cells; each cell (opponent, map, side, won). Side-blind p per
    (opponent, map) = (w + .5) / (n + 1); side-aware p per (opponent, map, side) = (w_s + k p) / (n_s + k), the side's
    own record shrunk toward the side-blind p while it is thin (a map's two start positions need not be equally
    good, and '?' scrims draw each block's sides at random). Returns rows (opponent, map, side, w, n, w_s, n_s,
    cand wins, cand games, p, p_s) and {'blind': (W, N, E, z), 'side': (W, N, E, z)}."""
    def tally(cells, key):
        t = {}
        for c in cells:
            x = t.setdefault(key(c), [0, 0])
            x[0] += c[3]
            x[1] += 1
        return t
    cell, side = (lambda c: c[:2]), (lambda c: c[:3])
    pb, ps, cs = tally(prior, cell), tally(prior, side), tally(cand, side)
    rows, acc = [], {'blind': [0, 0, 0.0, 0.0], 'side': [0, 0, 0.0, 0.0]}
    for key in sorted(cs):
        cw, cn = cs[key]
        w, n = pb.get(key[:2], (0, 0))
        ws, ns = ps.get(key, (0, 0))
        p = (w + 0.5) / (n + 1)
        q = (ws + k * p) / (ns + k)
        rows.append((*key, w, n, ws, ns, cw, cn, p, q))
        for name, x in (('blind', p), ('side', q)):
            a = acc[name]
            a[0] += cw; a[1] += cn; a[2] += cn * x; a[3] += cn * x * (1 - x)
    out = {name: (W, N, E, (W - E) / V ** 0.5 if V > 0 else 0.0) for name, (W, N, E, V) in acc.items()}
    return rows, out


def effect(rows, zc=1.96):
    """The candidate's effect as one logit shift d over the pool's side-aware cell rates q (logit p = logit q + d: a
    Bradley-Terry gain with every (opponent, map, side) cell its own opponent), on the Elo scale (400 / ln 10 a logit).
    The estimate solves sum cn p = wins; the 95% interval inverts the side-aware score z at +-zc (that z is this
    model's score test at d = 0). Returns (elo, lo, hi); an end is +-inf when the wins sit at 0 or N."""
    cells = [(cw, cn, math.log(min(max(q, 1e-9), 1 - 1e-9) / (1 - min(max(q, 1e-9), 1 - 1e-9))))
             for *_, cw, cn, p, q in rows if cn]
    W, N = sum(c[0] for c in cells), sum(c[1] for c in cells)

    def score(d):   # (wins - expected) / sd at shift d
        ps = [(cn, 1 / (1 + math.exp(-(l + d)))) for _, cn, l in cells]
        v = sum(cn * x * (1 - x) for cn, x in ps)
        return (W - sum(cn * x for cn, x in ps)) / v ** 0.5 if v > 0 else 0.0

    def solve(t, lim=12.0):   # the shift where score = t (score falls as d grows); +-inf past +-lim
        if score(-lim) < t:
            return -math.inf
        if score(lim) > t:
            return math.inf
        lo, hi = -lim, lim
        for _ in range(60):
            mid = (lo + hi) / 2
            lo, hi = (mid, hi) if score(mid) > t else (lo, mid)
        return (lo + hi) / 2
    if not N:
        return 0.0, -math.inf, math.inf
    elo = 400 / math.log(10)
    return tuple(x * elo for x in (solve(0.0), solve(zc), solve(-zc)))


def pool_cmd(a):
    games = lambda spec: block_cells_all(block_ids(spec), a.dir)
    missing = lambda spec: [i for i in block_ids(spec) if not os.path.exists(os.path.join(a.dir, str(i), 'match.json'))]
    prior, miss = games(a.ctrl), missing(a.ctrl)
    with open(a.poolfile) as f:
        lines = f.read().splitlines()
    for line in lines:
        b = line.split('#')[0].strip()
        if b and b not in a.blocks:
            prior += games(b)
            miss += missing(b)
    if miss:
        print(f'~ pool matches not downloaded: {miss}')
    todo = [i for b in a.blocks for i in missing(b)]
    if todo and not a.partial:
        print(f'~ PARTIAL: matches not finished or not downloaded: {todo}; refused (--partial scores the rest)')
        return 3
    cand = [g for b in a.blocks for g in games(b)]
    rows, s = pool_score(prior, cand, a.k)
    for opp, mp, sd, w, n, ws, ns, cw, cn, p, q in rows:
        flag = '  <- won a rarely-won cell' if cw and q < 0.25 else '  <- lost a usually-won cell' if cw < cn and q > 0.75 else ''
        print(f'{opp[:40]:40} {mp:24} {sd:2}  pool {w:2}/{n:<2} {sd} {ws:2}/{ns:<2}  cand {cw}/{cn}{flag}')
    W, N, E, z = s['blind']
    print(f'~ cand wins {W} of {N}, pooled expectation {E:.1f}, z {z:.2f}')
    W, N, E, z = s['side']
    print(f'~ side-aware (k {a.k:g}): expectation {E:.1f}, z {z:.2f}')
    d, lo, hi = effect(rows)
    print(f'~ side-aware effect: {d:+.0f} Elo (95% {lo:+.0f} to {hi:+.0f}): one logit shift over the pool\'s cell rates')
    if a.studied:
        # a candidate designed from some cells must also hold its own (z >= 0) on the others.
        marks = [x.split(':', 1) for x in a.studied.split(',')]
        hit = lambda g: any(g[0].startswith(o) and g[1] == m for o, m in marks)
        for name, part in (('studied', [g for g in cand if hit(g)]), ('other', [g for g in cand if not hit(g)])):
            W, N, E, z = pool_score(prior, part, a.k)[1]['side']
            print(f'~ {name} cells: wins {W} of {N}, side-aware expectation {E:.1f}, z {z:.2f}')
    return 0


def paired_score(ref, cand):
    """The candidate's cells (opponent, map, our side, won) against the champion's reference games on the same cells,
    each cell counted once however many games it has (TRAINING_ALGORITHM §4, paired fresh-cell rule): d = the
    candidate's mean result there minus the champion's (won is 0/1, or a graded score in [0, 1]). Returns rows
    (opponent, map, side, ref wins, ref games, cand wins, cand games, d), the candidate's cells with no reference
    game, and {'cells', 'D' (cells with d != 0), 'gained', 'lost', 'net' (sum of d), 't' (net / sqrt(sum of d^2), 0
    when no cell differs)}."""
    def tally(games):
        t = {}
        for o, m, s, w in games:
            x = t.setdefault((o, m, s), [0, 0])
            x[0] += w
            x[1] += 1
        return t
    r, c = tally(ref), tally(cand)
    rows = [(*k, *r[k], *c[k], c[k][0] / c[k][1] - r[k][0] / r[k][1]) for k in sorted(c) if k in r]
    ds = [x[-1] for x in rows]
    net, s2 = sum(ds), sum(d * d for d in ds)
    return rows, [k for k in sorted(c) if k not in r], {
        'cells': len(rows), 'D': sum(1 for d in ds if d), 'gained': sum(1 for d in ds if d > 0),
        'lost': sum(1 for d in ds if d < 0), 'net': net, 't': net / s2 ** 0.5 if s2 > 0 else 0.0}


def paired_cmd(a):
    bad, graded = {}, getattr(a, 'graded', False)
    if graded:
        games = lambda spec: graded_cells(block_ids(spec), a.dir, bad=bad)
    else:
        games = lambda spec: block_cells_all(block_ids(spec), a.dir)
    missing = lambda spec: [i for i in block_ids(spec) if not os.path.exists(os.path.join(a.dir, str(i), 'match.json'))]
    specs = [a.ref]
    if os.path.isfile(a.ref):
        with open(a.ref) as f:
            specs = [b for b in (line.split('#')[0].strip() for line in f) if b]
    ref = [g for s in specs for g in games(s)]
    miss = [i for s in specs for i in missing(s)]
    if miss:
        print(f'~ reference matches not downloaded: {miss}')
    if bad:
        print(f'~ reference matches not graded (left out): {sorted(bad)}: {bad[min(bad)]}')
    todo = [i for b in a.blocks for i in missing(b)]
    if todo and not a.partial:
        print(f'~ PARTIAL: matches not finished or not downloaded: {todo}; refused (--partial scores the rest)')
        return 3
    bad.clear()
    cand = [g for b in a.blocks for g in games(b)]
    if bad:
        print(f'~ candidate matches not graded: {sorted(bad)}: {bad[min(bad)]}'
              + ('' if a.partial else '; refused (--partial scores the rest)'))
        if not a.partial:
            return 3
    rows, unref, s = paired_score(ref, cand)
    if unref and not a.partial:
        print(f'~ NO REFERENCE: the champion has no game on {len(unref)} cells (play its control block on that set '
              f'first): {unref[:6]}; refused')
        return 3
    res = lambda w, n: f'{w:.2f}/{n}' if isinstance(w, float) else f'{w}/{n}'
    for opp, mp, sd, rw, rn, cw, cn, d in rows:
        if d:
            print(f'{opp[:40]:40} {mp:24} {sd:2}  champion {res(rw, rn)}  cand {res(cw, cn)}  d {d:+.2f}')
    print(f'~ paired{" (graded)" if graded else ""}: {s["cells"]} cells, '
          f'{s["cells"] - s["D"]} alike; D {s["D"]} (gained {s["gained"]}, lost {s["lost"]}), net {s["net"]:+.2f}, '
          f't {s["t"]:.2f}')
    if a.studied:
        # A candidate designed from some cells must also hold its own (t >= 0) on the others.
        marks = [x.split(':', 1) for x in a.studied.split(',')]
        hit = lambda g: any(g[0].startswith(o) and g[1] == m for o, m in marks)
        for name, part in (('studied', [g for g in cand if hit(g)]), ('other', [g for g in cand if not hit(g)])):
            x = paired_score(ref, part)[2]
            print(f'~ {name} cells: {x["cells"]}, D {x["D"]} (gained {x["gained"]}, lost {x["lost"]}), '
                  f'net {x["net"]:+.2f}, t {x["t"]:.2f}')
    return 0


def opponent(m, my_id):
    ps = m.get('participants') or []
    me = next((p for p in ps if p.get('team') == my_id), None)
    other = next((p for p in ps if p.get('team') != my_id), None)
    return me, other


def main(argv=None):
    ap = argparse.ArgumentParser(description=__doc__.split('\n\n')[0])
    ap.add_argument('--site', default=None)
    sub = ap.add_subparsers(dest='cmd', required=True)
    sub.add_parser('register')
    p = sub.add_parser('submit'); p.add_argument('bot', nargs='?', default=DEFAULT_BOT)
    p.add_argument('-m', default=''); p.add_argument('--no-wait', action='store_true')
    sub.add_parser('ladder')
    p = sub.add_parser('scrim'); p.add_argument('--n', type=int, default=1); p.add_argument('--band', type=int, default=3)
    p.add_argument('--maps', type=int, default=3); p.add_argument('--dry-run', action='store_true')
    p.add_argument('--vs', default=''); p.add_argument('--panel', default='')
    p.add_argument('--queue', default='', help='append requests the hourly limit refuses to this file')
    p = sub.add_parser('drain'); p.add_argument('file'); p.add_argument('--dry-run', action='store_true')
    p = sub.add_parser('matches'); p.add_argument('--pages', type=int, default=1); p.add_argument('--all', action='store_true')
    p = sub.add_parser('fetch'); p.add_argument('ids', type=int, nargs='*'); p.add_argument('--new', action='store_true')
    p.add_argument('--out', default=REPLAYS); p.add_argument('--pages', type=int, default=3)
    p = sub.add_parser('results'); p.add_argument('--pages', type=int, default=3)
    p = sub.add_parser('compare'); p.add_argument('ctrl'); p.add_argument('cand'); p.add_argument('--dir', default=REPLAYS)
    p = sub.add_parser('pool'); p.add_argument('ctrl'); p.add_argument('poolfile'); p.add_argument('blocks', nargs='+')
    p.add_argument('--partial', action='store_true'); p.add_argument('--k', type=float, default=2.0)
    p.add_argument('--dir', default=REPLAYS)
    p.add_argument('--studied', help='OPPONENT_PREFIX:MAP,... the cells the candidate was designed from')
    p = sub.add_parser('paired'); p.add_argument('ref'); p.add_argument('blocks', nargs='+')
    p.add_argument('--partial', action='store_true'); p.add_argument('--dir', default=REPLAYS)
    p.add_argument('--graded', action='store_true', help="a game's graded score (field_report) instead of won")
    p.add_argument('--studied', help='OPPONENT_PREFIX:MAP,... the cells the candidate was designed from')
    a = ap.parse_args(argv)
    if a.cmd == 'compare':
        return compare_cmd(a)
    if a.cmd == 'paired':
        return paired_cmd(a)
    if a.cmd == 'pool':
        return pool_cmd(a)
    c = cl.Client(a.site)
    acc = cl.load_accounts()
    ep = c.episode
    if a.cmd == 'register':
        cl.ensure_team(c, acc, TEAM, ABOUT)
        write_team_file(acc)
        print(f'team {TEAM}: id {acc[TEAM]["team_id"]}, user {acc[TEAM]["user"]} (password in {TEAM_FILE}, line 2)')
        return 0
    s = cl.session(c, acc, TEAM)
    my_id = acc[TEAM]['team_id']
    if a.cmd == 'submit':
        desc = a.m or os.path.basename(os.path.normpath(a.bot))
        x = cl.submit(s, a.bot, desc, ep)
        print(f'submission {x["id"]} ({x["_digest"]}): {x["status"]}')
        if not a.no_wait:
            x = cl.wait_submission(s, x['id'], ep)
            print(f'submission {x["id"]}: {x["status"]} accepted={x["accepted"]}')
            if not x['accepted']:
                print('\n'.join((x.get('logs') or '').splitlines()[-20:]))
                return 1
    elif a.cmd == 'ladder':
        for i, t in enumerate(cl.rankings(c), 1):
            r = '-' if t['rating'] is None else f'{t["rating"]:.0f}'
            print(f'{i:>3} {r:>6} {t["name"]}{"   <== us" if t["id"] == my_id else ""}'
                  f'{"" if t["active"] else "  (no accepted submission)"}')
    elif a.cmd == 'scrim':
        waiting = set()
        for m in cl.matches(c, my_id, 2):
            if m['status'] in cl.WAITING + ('RUN',):
                _, o = opponent(m, my_id)
                if o:
                    waiting.add(o.get('team'))
        if a.vs:
            specs = a.vs.split(',')
        else:
            plan = plan_scrims(cl.rankings(c), my_id, a.n, a.band, waiting)
            if not plan:
                print('no rated opponent near us (or all have a match waiting)')
                return 1
            specs = [t['name'] for t, _ in plan]
        left = send(c, s, ep, my_id, specs, a.panel, a.maps, a.dry_run)
        if left and a.queue:
            b = active_build(s, ep)
            write_queue(a.queue, read_queue(a.queue) + [(b, x, a.panel, a.maps) for x in left])
            print(f'{len(left)} queued in {a.queue} for build {b}')
    elif a.cmd == 'drain':
        b = active_build(s, ep)
        q, rest, nsent = drain_rows(read_queue(a.file), b,
                                    lambda specs, pf, n: send(c, s, ep, my_id, specs, pf, n, a.dry_run))
        if not a.dry_run:
            write_queue(a.file, rest)
        print(f'drain: active build {b}; {nsent} sent, {sum(1 for r in rest if r[0] == b)} still queued for it, '
              f'{sum(1 for r in rest if r[0] != b)} held for other builds')
    elif a.cmd == 'matches':
        for m in cl.matches(c, None if a.all else my_id, a.pages):
            print(cl.fmt_match(m))
    elif a.cmd == 'fetch':
        # as our team: galaxy shows a match's replay_url only to its participants (and staff)
        ms = [s.api('GET', f'/api/compete/{ep}/match/{i}/') for i in a.ids]
        if a.new:
            ms += [m for m in s.pages(f'/api/compete/{ep}/match/scrimmage/?team_id={my_id}', limit=a.pages)
                   if m['status'] == 'OK!' and not os.path.isdir(os.path.join(a.out, str(m['id'])))]
        for m in ms:
            if m.get('status') != 'OK!' or not m.get('replay_url'):
                print(f'{m["id"]}: no replay (status {m.get("status")})')
                continue
            d = os.path.join(a.out, str(m['id']))
            p = cl.fetch(c, m, d)              # d/<id>.bc26
            rp = os.path.join(d, 'replay.bc26')
            os.replace(p, rp)
            with open(rp, 'rb') as fh:
                info = match_json(m, fh.read())
            with open(os.path.join(d, 'match.json'), 'w') as fh:
                json.dump(info, fh, indent=1)
            res = ' '.join(f'{g["map"]}:{"W" if g["winner_team"] == TEAM else "L"}' for g in info['games'])
            print(f'{m["id"]}: {rp} ({res})')
    elif a.cmd == 'results':
        rec = {}
        for m in cl.matches(c, my_id, a.pages):
            if m['status'] != 'OK!':
                continue
            me, o = opponent(m, my_id)
            if not me or not o or me.get('score') is None:
                continue
            r = rec.setdefault(o.get('teamname'), [0, 0, 0])
            r[0] += me['score']; r[1] += o['score']; r[2] += 1
        for name, (w, l, n) in sorted(rec.items(), key=lambda kv: -(kv[1][0] - kv[1][1])):
            print(f'{name:32} games {w:>3}-{l:<3} in {n} match(es)')
        print(json.dumps({'opponents': len(rec), 'games_won': sum(r[0] for r in rec.values()),
                          'games_lost': sum(r[1] for r in rec.values())}))
    return 0


if __name__ == '__main__':
    sys.exit(main())
