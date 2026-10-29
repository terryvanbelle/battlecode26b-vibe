#!/usr/bin/env python3
"""Print result rows (results.jsonl) as a compact table: rows.py FILE...

pts is the spec's points; NOSCORE (score.missing: a summary error, or a points game with no summary) marks a row the
screens leave out. Per side: rat deaths, terminations (a runtime exception kills the robot and shows only as one;
'exc' counts player load failures), overruns and near90 (bytecode turns at >= 100% / 90% of the limit) with the max
bytecodes (rat/king). v1 marks a row summarized by dumpVersion 1 (deaths include rats merged into a king); TOOLS a
row summarized by the classes of an earlier tools build because the rebuild failed (arena's tools_warning).
"""
import json, os, sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import score  # noqa: E402


def side(t):
    return (f"{t.get('deathsRat')} t{t.get('terminated', '-')} x{t.get('exceptions')}"
            f" o{t.get('overruns')}/{t.get('near90')} {t.get('maxBcRat')}/{t.get('maxBcKing')}")


def line(r):
    A, B = r.get('A') or {}, r.get('B') or {}
    if 'winner' not in r:
        return f"{r['map'][:22]:22} ERROR {str(r.get('error'))[:80]!r}"
    why = score.missing(r)
    head = (f"{r['a'][:10]:10} {r['b'][:12]:12} {r['map'][:22]:22} s{r['seed']:<3} {r['winner']} r{r['rounds']:<5}"
            f" {str(r.get('winType') or score.reason_type(r.get('reason')))[:18]:18}")
    if why:
        return f"{head} NOSCORE {why}: {str(r.get('summary_error') or '')[:80]!r}"
    v = ('' if r.get('dumpVersion') else ' v1') + (' TOOLS' if r.get('tools_warning') else '')
    return (f"{head} pts {r.get('ptsA')}-{r.get('ptsB')} xfer {r.get('cheeseXferA')}-{r.get('cheeseXferB')}"
            f" cat {r.get('catDmgA')}-{r.get('catDmgB')} bs {r.get('backstabRound')}"
            f" | die/term/exc/ovr/bc A {side(A)} B {side(B)}{v}")


def main(paths):
    for p in paths:
        for l in open(p):
            if l.strip():
                print(line(json.loads(l)))


if __name__ == '__main__':
    main(sys.argv[1:])
