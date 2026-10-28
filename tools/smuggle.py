#!/usr/bin/env python3
"""Decode the bytecode-count tag our bot leaves in every turn (Debug.tag in bots/ratbot/Debug.java).

  smuggle.py REPLAY.bc26|TSVDIR [--game N]     per team: tagged or not; per role, the decision-code counts

Replays (the replica's included) keep each turn's bytecode count but drop indicator strings. A tagged build pads
the end of each turn so that bc mod 128 is a 7-bit tag:
  bits 0-4  a baby's move decision (index in WHY) or a king's spawn decision (index in KWHY); 31 = unknown
  bit 5     a baby fighter / a king with enemies in view after cooperation
  bit 6     even parity of bits 0-5
A team is tagged when nearly all its turns pass the parity check (an untagged build passes about half).
Rows for carried or thrown rats repeat the rat's last count; decode() returns the same tag for them.
"""
import argparse, collections, csv, os, sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

WHY = "-CDEHKMPSWacdfghkmprswyz"
KWHY = "-nsfcbd"
TAGGED_FRAC = 0.98   # parity pass rate above which a team is taken to be a tagged build
MIN_TURNS = 50


def encode(code, high, king):
    """The tag Debug.tagValue() writes for decision code `code` (a char) and flag `high`."""
    w = (KWHY if king else WHY).find(code)
    v = (w if w >= 0 else 31) | (32 if high else 0)
    return v if bin(v).count('1') % 2 == 0 else v | 64


def parity_ok(bc):
    return bin(int(bc) & 127).count('1') % 2 == 0


def decode(bc, king):
    """(code char or '?', high flag) from a turn's bytecode count; None when the parity check fails."""
    t = int(bc) & 127
    if not parity_ok(t):
        return None
    w, alpha = t & 31, KWHY if king else WHY
    return (alpha[w] if w < len(alpha) else '?', bool(t & 32))


def tagged_teams(turn_rows):
    """{team: (tagged, parity pass rate, turns)} over turns.tsv rows of teams 1 and 2."""
    acc = collections.defaultdict(lambda: [0, 0])
    for r in turn_rows:
        if r['team'] in ('1', '2'):
            acc[r['team']][0] += parity_ok(r['bc'])
            acc[r['team']][1] += 1
    return {t: (n >= MIN_TURNS and ok / n >= TAGGED_FRAC, ok / n if n else 0.0, n) for t, (ok, n) in acc.items()}


def label(code, high, king):
    """Readable form: 'F:m' for a fighter's micro move, 'K:cE' for a king at the cap with enemies in view."""
    if king:
        return 'K:' + code + ('E' if high else '')
    return ('F:' if high else 'G:') + code


def summarize(turn_rows):
    """{team: Counter(label)} for the tagged teams (one count per robot-round)."""
    rows = list(turn_rows)
    tagged = {t for t, (ok, _, _) in tagged_teams(rows).items() if ok}
    out = {t: collections.Counter() for t in tagged}
    seen = set()
    for r in rows:
        if r['team'] not in tagged or (r['round'], r['id']) in seen:
            continue
        seen.add((r['round'], r['id']))
        king = r['type'] == 'KING'
        d = decode(r['bc'], king)
        if d:
            out[r['team']][label(d[0], d[1], king)] += 1
    return out


def main(argv=None):
    import game_report as GR
    ap = argparse.ArgumentParser()
    ap.add_argument('src')
    ap.add_argument('--game', type=int, default=0)
    a = ap.parse_args(argv)
    d = a.src
    if os.path.isfile(d):
        d = GR.dump(d)
        import atexit, shutil
        atexit.register(shutil.rmtree, d, True)
    with open(os.path.join(d, f'g{a.game}.turns.tsv')) as f:
        rows = list(csv.DictReader(f, delimiter='\t'))
    for t, (ok, rate, n) in sorted(tagged_teams(rows).items()):
        print(f"team {'AB'[int(t) - 1]}: {'tagged' if ok else 'not tagged'} (parity {100 * rate:.1f}% of {n} turns)")
    for t, c in sorted(summarize(rows).items()):
        tot = sum(c.values())
        print(f"team {'AB'[int(t) - 1]} decisions:", ', '.join(f'{k} {100 * v / tot:.1f}%' for k, v in c.most_common(20)))


if __name__ == '__main__':
    main()
