"""Bradley-Terry ratings on the Elo scale from game counts (standard library only).

fit(games) takes (playerA, playerB, winsA, winsB) tuples and returns {player: Rating}. The fit is the MM
iteration of Hunter (2004) for P(i beats j) = r_i / (r_i + r_j), with a weak prior: every player gets one virtual
win and one virtual loss against an anchor fixed at strength 1, so players with all wins or all losses stay finite.
A pair's games are capped at PAIR_CAP (scaled down keeping its win share), so one heavily repeated pairing cannot
dominate. Ratings are 400*log10(r), shifted so the mean over all players is 1500. se is the standard error from
the diagonal of the information matrix (each other rating taken as known), so it is a little optimistic.

A game may carry a fifth element k, a handicap key (e.g. the map): playerA then plays at strength r_A * h_k, with h_k
fitted alongside the ratings (Hunter's home-advantage model, one advantage per key). fit_h(games) returns
(ratings, {k: h_k in Elo points}). When every keyed player (an A of a keyed game) meets unkeyed players only in keyed
games, the h_k are normalized to a game-weighted mean of 0 by moving the keyed players' ratings the other way (the
likelihood is unchanged), so a keyed player's rating is its strength on an average key of the mix it was fitted on.
"""
import collections, math

PAIR_CAP = 200
SCALE = 400 / math.log(10)


class Rating:
    __slots__ = ('rating', 'se', 'games', 'wins', 'losses')

    def __init__(self, rating, se, games, wins, losses):
        self.rating, self.se, self.games, self.wins, self.losses = rating, se, games, wins, losses

    def __repr__(self):
        return f'Rating({self.rating:.0f}+-{self.se:.0f}, {self.wins:g}-{self.losses:g})'


def pair_table(games):
    """{(i, j): [wins_i, wins_j]} with i < j, capped at PAIR_CAP games per pair (unkeyed games only; see key_table)."""
    return {(i, j): w for (i, j, k), w in key_table(games).items()}


def key_table(games):
    """{(i, j, k): [wins_i, wins_j]}: k None with i < j, else i the keyed player. Each pair of players (whatever the
    keys) is capped at PAIR_CAP games, every entry scaled down by the same factor."""
    t = collections.defaultdict(lambda: [0.0, 0.0])
    for g in games:
        a, b, wa, wb = g[:4]
        k = g[4] if len(g) > 4 else None
        if a == b:
            continue
        if k is not None or a < b:
            t[(a, b, k)][0] += wa; t[(a, b, k)][1] += wb
        else:
            t[(b, a, k)][0] += wb; t[(b, a, k)][1] += wa
    pair = collections.defaultdict(float)
    for (i, j, k), (x, y) in t.items():
        pair[min(i, j), max(i, j)] += x + y
    for (i, j, k), (x, y) in t.items():
        n = pair[min(i, j), max(i, j)]
        if n > PAIR_CAP:
            t[(i, j, k)] = [x * PAIR_CAP / n, y * PAIR_CAP / n]
    return dict(t)


def fit(games, iters=2000, tol=1e-9):
    return fit_h(games, iters, tol)[0]


def fit_h(games, iters=2000, tol=1e-9):
    t = key_table(games)
    opp = collections.defaultdict(list)     # player -> [(other, n_games, key, keyed side?)]
    wins = collections.defaultdict(float)
    hw, hn = collections.defaultdict(float), collections.defaultdict(float)   # key -> keyed side's wins, games
    by_key = collections.defaultdict(list)                                     # key -> [(keyed i, j, n)]
    raw = collections.defaultdict(lambda: [0.0, 0.0])
    for (i, j, k), (wi, wj) in t.items():
        n = wi + wj
        opp[i].append((j, n, k, True)); opp[j].append((i, n, k, False))
        wins[i] += wi; wins[j] += wj
        if k is not None:
            hw[k] += wi; hn[k] += n; by_key[k].append((i, j, n))
    for g in games:
        a, b, wa, wb = g[:4]
        if a != b:
            raw[a][0] += wa; raw[a][1] += wb
            raw[b][0] += wb; raw[b][1] += wa
    players = sorted(opp)
    keys = sorted(hn, key=str)
    r = {p: 1.0 for p in players}
    h = {k: 1.0 for k in keys}

    def m(k):
        return 1.0 if k is None else h[k]

    def p_win(p, q, k, keyed):   # (strength of p, strength of q) in a game keyed k, p on the keyed side or not
        return (r[p] * m(k), r[q]) if keyed else (r[p], r[q] * m(k))

    for _ in range(iters):
        delta = 0.0
        for p in players:
            # prior: 1 win + 1 loss vs the anchor (strength 1) -> +1 to wins, 2/(r+1) to the denominator
            den = 2.0 / (r[p] + 1.0)
            for q, n, k, keyed in opp[p]:
                sp, sq = p_win(p, q, k, keyed)
                den += n * (m(k) if keyed else 1.0) / (sp + sq)
            new = (wins[p] + 1.0) / den
            delta = max(delta, abs(math.log(new / r[p])))
            r[p] = new
        for k in keys:
            den = 2.0 / (h[k] + 1.0)
            for i, j, n in by_key[k]:
                den += n * r[i] / (r[i] * h[k] + r[j])
            new = (hw[k] + 1.0) / den
            delta = max(delta, abs(math.log(new / h[k])))
            h[k] = new
        if delta < tol:
            break
    keyed = {i for (i, j, k) in t if k is not None}
    if keys and not any(k is None and ((i in keyed) != (j in keyed)) for (i, j, k) in t):
        mu = sum(hn[k] * math.log(h[k]) for k in keys) / sum(hn.values())
        for k in keys:
            h[k] /= math.exp(mu)
        for p in keyed:
            r[p] *= math.exp(mu)
    logs = {p: math.log(r[p]) for p in players}
    mean = sum(logs.values()) / max(1, len(logs))
    out = {}
    for p in players:
        info = 2.0 * r[p] / (r[p] + 1.0) ** 2
        for q, n, k, kd in opp[p]:
            sp, sq = p_win(p, q, k, kd)
            info += n * sp * sq / (sp + sq) ** 2
        w, l = raw[p]
        out[p] = Rating(1500 + SCALE * (logs[p] - mean), SCALE / math.sqrt(info), w + l, w, l)
    return out, {k: SCALE * math.log(h[k]) for k in keys}


def expected(ra, rb):
    """P(a beats b) for Elo-scale ratings."""
    return 1.0 / (1.0 + 10 ** ((rb - ra) / 400.0))


def field_score(ratings, player, field):
    """Mean expected score of player against each field player (one game each)."""
    xs = [expected(ratings[player].rating, ratings[f].rating) for f in field if f in ratings and f != player]
    return sum(xs) / len(xs) if xs else float('nan')
