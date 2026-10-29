"""Game scores from a result row's final aggregates (tools/arena.py rows; ReplayDump summary fields).

points(row): the spec's points (GameWorld.setWinnerIfMorePoints): weighted shares of cat damage, live rat kings and
cheese transferred, 50/30/20 in cooperation and 30/50/20 after a backstab. The replay's per-round "alive rat kings"
aggregate (kingsA/kingsB) is 10 * global cheese + kings, so the king count is its last digit.

cell(row): the cell score both screens and the ratings use (TRAINING_ALGORITHM 4): 100/0 to the winner when a game is
decided by its kings (the contest counts that win and nothing else), else the points share, which is how the engine
decided it (all cats dead in cooperation, or the round limit). (0, 0) when the row cannot be scored: missing(row)
says why, and callers leave such rows out and count them. A row without a usable replay summary (summary_error, or
no winType) is scored only when its server reason shows a decisive win; a points game then has no score, never a
guessed one.
"""

DECISIVE = ('RATKING_DESTROYED', 'KILL_ALL_RAT_KINGS', 'RESIGNATION')
# the server's "Reason:" line (Server.java, DominationFactor) -> ReplayDump win type
REASONS = (("destroyed all of the enemy team's rat kings", 'RATKING_DESTROYED'), ('more points', 'MORE_POINTS'),
           ('(more cheese)', 'MORE_CHEESE'), ('more robots alive', 'MORE_ROBOTS'), ('coin flip', 'COIN_FLIP'),
           ('resigned', 'RESIGNATION'))


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


def reason_type(reason):
    """The win type a server reason line names, else None."""
    for text, wt in REASONS:
        if text in (reason or ''):
            return wt
    return None


def missing(row):
    """Why cell(row) has no score ('' when it has one)."""
    if row.get('winner') not in ('A', 'B'):
        return 'no result'
    if row.get('winType') is not None and not row.get('summary_error'):
        return ''
    if reason_type(row.get('reason')) in DECISIVE:
        return ''
    if row.get('summary_error'):
        return 'summary error'
    if (row.get('ptsA') or 0) + (row.get('ptsB') or 0) > 0:
        return ''
    return 'no summary'


def cell(row):
    """(score A, score B), 0-100 each, summing to 100; (0, 0) for a row with no score (missing(row) says why)."""
    if missing(row):
        return 0.0, 0.0
    w = row['winner']
    wt = None if row.get('summary_error') else row.get('winType')
    if wt is None:
        if reason_type(row.get('reason')) not in DECISIVE:     # an old row without the summary: its stored points
            a, b = float(row['ptsA']), float(row['ptsB'])
            return 100.0 * a / (a + b), 100.0 * b / (a + b)
        wt = 'RATKING_DESTROYED'
    if wt in DECISIVE:
        return (100.0, 0.0) if w == 'A' else (0.0, 100.0)
    a, b = points(row)
    return (100.0 * a / (a + b), 100.0 * b / (a + b)) if a + b > 0 else (50.0, 50.0)
