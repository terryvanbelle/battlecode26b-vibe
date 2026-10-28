"""Game scores from a result row's final aggregates (tools/arena.py rows; ReplayDump summary fields).

points(row): the spec's points (GameWorld.setWinnerIfMorePoints): weighted shares of cat damage, live rat kings and
cheese transferred, 50/30/20 in cooperation and 30/50/20 after a backstab. The replay's per-round "alive rat kings"
aggregate (kingsA/kingsB) is 10 * global cheese + kings, so the king count is its last digit.

cell(row): the cell score both screens and the ratings use (TRAINING_ALGORITHM 4): 100/0 to the winner when a game is
decided by its kings (the contest counts that win and nothing else), else the points share, which is how the engine
decided it (all cats dead in cooperation, or the round limit).
"""

DECISIVE = ('RATKING_DESTROYED', 'KILL_ALL_RAT_KINGS', 'RESIGNATION')


def kings(v):
    return int(v or 0) % 10


def points(row):
    def pct(a, b):
        return (100.0 * a / (a + b), 100.0 * b / (a + b)) if a + b > 0 else (0.0, 0.0)
    ca, cb = pct(row.get('catDmgA') or 0, row.get('catDmgB') or 0)
    ka, kb = pct(kings(row.get('kingsA')), kings(row.get('kingsB')))
    xa, xb = pct(row.get('cheeseXferA') or 0, row.get('cheeseXferB') or 0)
    w = (0.5, 0.3, 0.2) if (row.get('backstabRound') if row.get('backstabRound') is not None else -1) < 0 \
        else (0.3, 0.5, 0.2)
    # the engine truncates each team's points to an int
    return int(w[0] * ca + w[1] * ka + w[2] * xa), int(w[0] * cb + w[1] * kb + w[2] * xb)


def cell(row):
    """(score A, score B), 0-100 each, summing to 100 (0, 0 for a row with no result)."""
    w = row.get('winner')
    wt = row.get('winType')
    if wt is None and row.get('ptsA') is not None:   # a row without the replay summary: its stored points
        a, b = float(row['ptsA']), float(row['ptsB'])
        return (100.0 * a / (a + b), 100.0 * b / (a + b)) if a + b > 0 else (50.0, 50.0)
    if w not in ('A', 'B'):
        return 0.0, 0.0
    if wt is None or wt in DECISIVE:
        return (100.0, 0.0) if w == 'A' else (0.0, 100.0)
    a, b = points(row)
    return (100.0 * a / (a + b), 100.0 * b / (a + b)) if a + b > 0 else (50.0, 50.0)
