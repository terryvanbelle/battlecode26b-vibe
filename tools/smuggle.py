#!/usr/bin/env python3
"""Decode the bytecode-count tag our bot leaves in every turn (Debug.tag in bots/ratbot/Debug.java).

  smuggle.py REPLAY.bc26|TSVDIR [--game N]     per game and team: tagged or not; the decision-code shares

Replays (the replica's included) keep each turn's bytecode count but drop indicator strings. A tagged build pads
the end of each turn so that bc mod 128 is a 7-bit tag:
  bits 0-4  a baby's move decision (index in WHY) or a king's spawn decision (index in KWHY); 31 = unknown
  bit 5     a baby with role FIGHT (gatherers and attackers both 0) / a king with enemies in view after cooperation
  bit 6     even parity of bits 0-5
A team is tagged when nearly all its turns decode (an untagged build passes about half) and its counts vary.
Row rules (engine InternalRobot.throwRobot / processEndOfTurn): a robot-round can have several turns.tsv rows;
rows written for a thrower, a thrown rat or a carried rat during someone's turn repeat the PREVIOUS turn's count.
The real one is the robot's own record: src=own in a dumpVersion 2 dump, else the last row of the (round, id). bc 0
or dead=1 means the robot died in its own turn (no tag); a count within TAG_MIN_LEFT of the limit may be a turn whose
pad was skipped, and the turn after an overrun ran late. turns.tsv gives a robot's type at the start of its turn, so
a baby that becomes a king that turn decodes with WHY, as it was written.
"""
import argparse, collections, json, os, sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import game_report as GR  # noqa: E402

WHY = "-CDEHKMPSWacdfghkmprswyzxTR"
KWHY = "-nsfcbd"
TAG_MIN_LEFT = 400              # Debug.TAG_MIN_LEFT: no pad with fewer bytecodes left
LIMIT = {'KING': 20000}         # bytecode limit by type (others 17500)
TAGGED_FRAC = 0.98              # decode rate above which a team is taken to be a tagged build
MIN_TURNS, MIN_DISTINCT = 50, 16


def limit(rtype):
    return LIMIT.get(rtype, 17500)


def encode(code, high, king):
    """The tag Debug.tagValue() writes for decision code `code` (a char) and flag `high`."""
    w = (KWHY if king else WHY).find(code)
    v = (w if w >= 0 else 31) | (32 if high else 0)
    return v if bin(v).count('1') % 2 == 0 else v | 64


def parity_ok(bc):
    return bin(int(bc) & 127).count('1') % 2 == 0


def decode(bc, king):
    """(code char or '?', high flag) from a turn's bytecode count; None unless encode() could have written it."""
    t = int(bc) & 127
    alpha, w = KWHY if king else WHY, t & 31
    if not parity_ok(t) or not (w < len(alpha) or w == 31):
        return None
    return (alpha[w] if w < len(alpha) else '?', bool(t & 32))


def robot_rounds(turn_rows):
    """The real row of each robot-round of teams 1 and 2 (GR.own: src=own, or the last row), in input order."""
    return [r for r in GR.own(turn_rows) if r['team'] in ('1', '2')]


def usable(rows):
    """Robot-rounds whose count is a tag: not a death row (bc 0), not possibly skipped (within TAG_MIN_LEFT - 5 of
    the limit, overruns included), and not the turn right after an overrun."""
    out, late = [], set()
    for r in sorted(rows, key=lambda r: (r['id'], int(r['round']))):
        bc, lim = int(r['bc']), limit(r['type'])
        prev_over = r['id'] in late
        late.discard(r['id'])
        if bc >= lim:
            late.add(r['id'])
        if bc == 0 or r.get('dead') == '1' or bc >= lim - (TAG_MIN_LEFT - 5) or prev_over:
            continue
        out.append(r)
    return out


def tagged_teams(turn_rows):
    """{team: (tagged, decode rate, robot-rounds)} for teams 1 and 2."""
    acc = collections.defaultdict(lambda: [0, 0, set()])
    for r in usable(robot_rounds(turn_rows)):
        a = acc[r['team']]
        a[0] += decode(r['bc'], r['type'] == 'KING') is not None
        a[1] += 1
        a[2].add(r['bc'])
    return {t: (n >= MIN_TURNS and len(bcs) >= MIN_DISTINCT and ok / n >= TAGGED_FRAC, ok / n if n else 0.0, n)
            for t, (ok, n, bcs) in acc.items()}


def label(code, high, king):
    """Readable form: 'F:m' for a fighter's micro move, 'G:W' for a non-fighter (gatherer, or attacker under
    Cfg.RUSH), 'K:cE' for a king at the cap with enemies in view."""
    if king:
        return 'K:' + code + ('E' if high else '')
    return ('F:' if high else 'G:') + code


def summarize(turn_rows):
    """{team: Counter(label)} for the tagged teams, one count per usable robot-round."""
    rows = list(turn_rows)
    tagged = {t for t, (ok, _, _) in tagged_teams(rows).items() if ok}
    out = {t: collections.Counter() for t in tagged}
    for r in usable(robot_rounds(rows)):
        if r['team'] not in tagged:
            continue
        king = r['type'] == 'KING'
        d = decode(r['bc'], king)
        if d:
            out[r['team']][label(d[0], d[1], king)] += 1
    return out


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument('src')
    ap.add_argument('--game', type=int, default=None, help='0-based game index (match.json game = N+1); default all')
    a = ap.parse_args(argv)
    d = GR.source(a.src)
    for g in GR.pick(d, a.game):
        meta = json.load(open(os.path.join(d, f'g{g}.meta.json')))
        name = {'1': meta.get('teamA'), '2': meta.get('teamB')}
        print(f"game {g} ({meta.get('map')}): A {name['1']} vs B {name['2']}" + GR.result(GR.summary(d, g)))
        rows = list(GR.rows(os.path.join(d, f'g{g}.turns.tsv'), GR.need(d, g, 'round id team type bc src dead')))
        for t, (ok, rate, n) in sorted(tagged_teams(rows).items()):
            print(f"  {name[t]}: {'tagged' if ok else 'not tagged'} (decodes {100 * rate:.1f}% of {n} robot-rounds)")
        for t, c in sorted(summarize(rows).items()):
            tot = sum(c.values())
            print(f"  {name[t]} decisions:", ', '.join(f'{k} {100 * v / tot:.1f}%' for k, v in c.most_common(20)))


if __name__ == '__main__':
    sys.exit(main())
