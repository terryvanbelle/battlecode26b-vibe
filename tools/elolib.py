"""Bradley-Terry ratings on the Elo scale from game counts (standard library only).

fit(games) takes (playerA, playerB, winsA, winsB) tuples and returns {player: Rating}. The fit is the MM
iteration of Hunter (2004) for P(i beats j) = r_i / (r_i + r_j), with a weak prior: every player gets one virtual
win and one virtual loss against an anchor fixed at strength 1, so players with all wins or all losses stay finite.
A pair's games are capped at PAIR_CAP (scaled down keeping its win share), so one heavily repeated pairing cannot
dominate. Ratings are 400*log10(r), shifted so the mean over all players is 1500. se is the standard error from
the diagonal of the information matrix (each other rating taken as known), so it is a little optimistic.
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
    """{(i, j): [wins_i, wins_j]} with i < j, capped at PAIR_CAP games per pair."""
    t = collections.defaultdict(lambda: [0.0, 0.0])
    for a, b, wa, wb in games:
        if a == b:
            continue
        if a < b:
            t[(a, b)][0] += wa; t[(a, b)][1] += wb
        else:
            t[(b, a)][0] += wb; t[(b, a)][1] += wa
    for k, (x, y) in t.items():
        n = x + y
        if n > PAIR_CAP:
            t[k] = [x * PAIR_CAP / n, y * PAIR_CAP / n]
    return t


def fit(games, iters=2000, tol=1e-9):
    t = pair_table(games)
    opp = collections.defaultdict(list)     # player -> [(other, n_games)]
    wins = collections.defaultdict(float)
    raw = collections.defaultdict(lambda: [0.0, 0.0])
    for (i, j), (wi, wj) in t.items():
        n = wi + wj
        opp[i].append((j, n)); opp[j].append((i, n))
        wins[i] += wi; wins[j] += wj
    for a, b, wa, wb in games:
        if a != b:
            raw[a][0] += wa; raw[a][1] += wb
            raw[b][0] += wb; raw[b][1] += wa
    players = sorted(opp)
    r = {p: 1.0 for p in players}
    for _ in range(iters):
        delta = 0.0
        for p in players:
            # prior: 1 win + 1 loss vs the anchor (strength 1) -> +1 to wins, 2/(r+1) to the denominator
            den = 2.0 / (r[p] + 1.0) + sum(n / (r[p] + r[q]) for q, n in opp[p])
            new = (wins[p] + 1.0) / den
            delta = max(delta, abs(math.log(new / r[p])))
            r[p] = new
        if delta < tol:
            break
    logs = {p: math.log(r[p]) for p in players}
    mean = sum(logs.values()) / max(1, len(logs))
    out = {}
    for p in players:
        info = 2.0 * r[p] / (r[p] + 1.0) ** 2 + sum(n * r[p] * r[q] / (r[p] + r[q]) ** 2 for q, n in opp[p])
        w, l = raw[p]
        out[p] = Rating(1500 + SCALE * (logs[p] - mean), SCALE / math.sqrt(info), w + l, w, l)
    return out


def expected(ra, rb):
    """P(a beats b) for Elo-scale ratings."""
    return 1.0 / (1.0 + 10 ** ((rb - ra) / 400.0))


def field_score(ratings, player, field):
    """Mean expected score of player against each field player (one game each)."""
    xs = [expected(ratings[player].rating, ratings[f].rating) for f in field if f in ratings and f != player]
    return sum(xs) / len(xs) if xs else float('nan')
