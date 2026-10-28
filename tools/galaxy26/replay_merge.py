#!/usr/bin/env python3
"""Merge one-game Battlecode 2026 replays (.bc26: gzip of a flatbuffer GameWrapper) into one multi-game replay, as
the engine itself writes for a run over several maps.

  python3 replay_merge.py OUT.bc26 GAME1.bc26 GAME2.bc26 ...   [--winner A|B: whose GameFooter closes the file]

Why: saturn.py plays each map of a match as its own engine run, so a finished game survives a restart (the
engine's own multi-map run is all or nothing). The contestants' client expects one file per match, holding
  GameHeader, (MatchHeader, Round..., MatchFooter) per game, GameFooter,
with GameWrapper.matchHeaders / matchFooters indexing the events. That is what this writes.

How (no schema compiler needed): every offset inside a flatbuffer is relative (uoffset forward from where it is
stored, soffset from a table to its vtable), so a whole input buffer copied verbatim, at an alignment it already had,
stays valid. The output is a new GameWrapper table and three vectors at the front, followed by the input buffers;
the new events vector points at the inputs' EventWrapper tables. The GameHeader is the first game's (all runs of a
match have the same teams and constants); the GameFooter is that of a game the overall winner won, so its winner is
the engine's rule for a multi-map run (A if A won at least as many games as B, else B).
Standard library only.
"""
import gzip
import struct
import sys

ALIGN = 16      # >= the largest scalar alignment in the schema (8); inputs are copied at multiples of this
EVENT_GAME_HEADER, EVENT_MATCH_HEADER, EVENT_ROUND, EVENT_MATCH_FOOTER, EVENT_GAME_FOOTER = 1, 2, 3, 4, 5


def _u32(b, p):
    return struct.unpack_from('<I', b, p)[0]


def _i32(b, p):
    return struct.unpack_from('<i', b, p)[0]


def _u16(b, p):
    return struct.unpack_from('<H', b, p)[0]


def _field(b, table, idx):
    """Absolute position of field idx of the table at `table`, or None if absent."""
    vt = table - _i32(b, table)
    vsize = _u16(b, vt)
    slot = 4 + 2 * idx
    if slot + 2 > vsize:
        return None
    off = _u16(b, vt + slot)
    return table + off if off else None


def _vector(b, table, idx):
    """(position of the first element, length) of the vector field idx, or (None, 0)."""
    p = _field(b, table, idx)
    if p is None:
        return None, 0
    v = p + _u32(b, p)
    return v + 4, _u32(b, v)


class Replay:
    """One parsed GameWrapper: absolute positions of its EventWrapper tables, their types, header/footer indices."""

    def __init__(self, raw):
        self.buf = gzip.decompress(raw) if raw[:2] == b'\x1f\x8b' else bytes(raw)
        b = self.buf
        root = _u32(b, 0)
        first, n = _vector(b, root, 0)
        if first is None or n < 2:
            raise ValueError('replay has no events')
        self.events = []
        for j in range(n):
            p = first + 4 * j
            ev = p + _u32(b, p)
            tp = _field(b, ev, 0)
            self.events.append((ev, b[tp] if tp is not None else 0))
        self.match_headers = self._ints(root, 1)
        self.match_footers = self._ints(root, 2)
        if self.events[0][1] != EVENT_GAME_HEADER or self.events[-1][1] != EVENT_GAME_FOOTER:
            raise ValueError('replay does not start with a GameHeader and end with a GameFooter')
        if len(self.match_headers) != len(self.match_footers):
            raise ValueError('replay has unequal match header and footer counts')

    def _ints(self, root, idx):
        first, n = _vector(self.buf, root, idx)
        return [] if first is None else [_i32(self.buf, first + 4 * j) for j in range(n)]

    def footer_winner(self):
        """Winner byte of the GameFooter (team id: 1 = A, 2 = B)."""
        ev = self.events[-1][0]
        u = _field(self.buf, ev, 1)
        table = u + _u32(self.buf, u)
        w = _field(self.buf, table, 0)
        return self.buf[w] if w is not None else 0


def merge(raws, winner=None):
    """raws: the gzipped (or raw) bytes of each game's replay, in game order. winner: 'A'/'B' overall (default: from
    the GameFooters, the engine's rule). Returns the gzipped merged replay."""
    reps = [Replay(r) for r in raws]
    if not reps:
        raise ValueError('nothing to merge')
    wins = {1: 0, 2: 0}
    for r in reps:
        wins[r.footer_winner()] = wins.get(r.footer_winner(), 0) + 1
    if winner is None:
        winner = 'A' if wins.get(1, 0) >= wins.get(2, 0) else 'B'
    want = 1 if winner == 'A' else 2
    footer_from = next((i for i, r in enumerate(reps) if r.footer_winner() == want), len(reps) - 1)

    # event list: (replay index, absolute position) and the new header / footer indices
    events, mh, mf = [(0, reps[0].events[0][0])], [], []
    for i, r in enumerate(reps):
        base = len(events) - 1           # this replay's event k (k >= 1) lands at base + k
        events += [(i, pos) for pos, _ in r.events[1:-1]]
        mh += [base + k for k in r.match_headers]
        mf += [base + k for k in r.match_footers]
    events.append((footer_from, reps[footer_from].events[-1][0]))

    # front part: root offset, vtable, table, vectors
    n = len(events)
    vt_pos, tbl_pos = 4, 16
    ev_vec = tbl_pos + 16
    mh_vec = ev_vec + 4 + 4 * n
    mf_vec = mh_vec + 4 + 4 * len(mh)
    front_len = mf_vec + 4 + 4 * len(mf)
    starts, pos = [], (front_len + ALIGN - 1) // ALIGN * ALIGN
    for r in reps:
        starts.append(pos)
        pos = (pos + len(r.buf) + ALIGN - 1) // ALIGN * ALIGN
    out = bytearray(pos)
    struct.pack_into('<I', out, 0, tbl_pos)
    struct.pack_into('<HHHHH', out, vt_pos, 10, 16, 4, 8, 12)        # vtable: 3 offset fields
    struct.pack_into('<i', out, tbl_pos, tbl_pos - vt_pos)
    for k, vec in enumerate((ev_vec, mh_vec, mf_vec)):
        at = tbl_pos + 4 + 4 * k
        struct.pack_into('<I', out, at, vec - at)
    struct.pack_into('<I', out, ev_vec, n)
    for j, (i, p) in enumerate(events):
        at = ev_vec + 4 + 4 * j
        struct.pack_into('<I', out, at, starts[i] + p - at)
    for vec, vals in ((mh_vec, mh), (mf_vec, mf)):
        struct.pack_into('<I', out, vec, len(vals))
        struct.pack_into(f'<{len(vals)}i', out, vec + 4, *vals)
    for s, r in zip(starts, reps):
        out[s:s + len(r.buf)] = r.buf
    return gzip.compress(bytes(out), compresslevel=6, mtime=0)


def summary(raw):
    """[(event type counts per match)] for checks: list of (rounds, winner byte of the MatchFooter)."""
    r = Replay(raw)
    res = []
    for h, f in zip(r.match_headers, r.match_footers):
        rounds = sum(1 for _, t in r.events[h + 1:f] if t == EVENT_ROUND)
        ev = r.events[f][0]
        u = _field(r.buf, ev, 1)
        table = u + _u32(r.buf, u)
        w = _field(r.buf, table, 0)
        res.append((rounds, r.buf[w] if w is not None else 0))
    return res


WIN_TYPES = ('RESIGNATION', 'RATKING_DESTROYED', 'BACKSTAB_RATKING_DESTROYED', 'MORE_POINTS', 'MORE_ROBOTS',
             'MORE_CHEESE', 'TIE', 'COIN_FLIP')      # schema/battlecode.fbs enum WinType (2026)


def _table(b, table, idx):
    """The sub-table field idx of a table, or None."""
    p = _field(b, table, idx)
    return None if p is None else p + _u32(b, p)


def _string(b, table, idx):
    p = _field(b, table, idx)
    if p is None:
        return None
    s = p + _u32(b, p)
    return b[s + 4:s + 4 + _u32(b, s)].decode('utf-8', 'replace')


def _scalar(b, table, idx, fmt, default=0):
    p = _field(b, table, idx)
    return default if p is None else struct.unpack_from('<' + fmt, b, p)[0]


def describe(raw):
    """{'teams': {team id: name}, 'games': [{'map', 'rounds', 'winner' (team id: 1 = A, 2 = B), 'win_type'}]} of a
    replay: the GameHeader's teams, each MatchHeader's map name, each MatchFooter's winner, win type and totalRounds
    (schema/battlecode.fbs of battlecode26: TeamData{name,packageName,teamId}, MatchHeader{map,maxRounds},
    GameMap{name,...}, MatchFooter{winner,winType,totalRounds,...})."""
    r = Replay(raw)
    b = r.buf
    head = _table(b, r.events[0][0], 1)
    teams = {}
    first, n = _vector(b, head, 1)
    for j in range(n if first is not None else 0):
        p = first + 4 * j
        t = p + _u32(b, p)
        teams[_scalar(b, t, 2, 'b')] = _string(b, t, 0)
    games = []
    for h, f in zip(r.match_headers, r.match_footers):
        mh = _table(b, r.events[h][0], 1)
        gm = _table(b, mh, 0)
        mf = _table(b, r.events[f][0], 1)
        wt = _scalar(b, mf, 1, 'b')
        games.append({'map': _string(b, gm, 0) if gm is not None else None, 'rounds': _scalar(b, mf, 2, 'i'),
                      'winner': _scalar(b, mf, 0, 'b'),
                      'win_type': WIN_TYPES[wt] if 0 <= wt < len(WIN_TYPES) else str(wt)})
    return {'teams': teams, 'games': games}


def main(argv):
    winner = None
    if '--winner' in argv:
        k = argv.index('--winner')
        winner = argv[k + 1]
        del argv[k:k + 2]
    if len(argv) < 2:
        print(__doc__, file=sys.stderr)
        return 2
    raws = []
    for p in argv[1:]:
        with open(p, 'rb') as fh:
            raws.append(fh.read())
    data = merge(raws, winner)
    with open(argv[0], 'wb') as fh:
        fh.write(data)
    print(f'{argv[0]}: {len(raws)} game(s), {summary(data)}')
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
