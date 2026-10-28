#!/usr/bin/env python3
"""Print result rows (results.jsonl) as a compact table: arena.py rows FILE..."""
import json, sys

def main(paths):
    for p in paths:
        for line in open(p):
            r = json.loads(line)
            A, B = r.get('A') or {}, r.get('B') or {}
            if 'winner' not in r:
                print(f"{r['map'][:22]:22} ERROR {str(r.get('error'))[:80]!r}")
                continue
            print(f"{r['a'][:10]:10} {r['b'][:12]:12} {r['map'][:22]:22} s{r['seed']:<3} {r['winner']} r{r['rounds']:<5}"
                  f" {str(r.get('winType'))[:18]:18} pts {r.get('ptsA')}-{r.get('ptsB')} xfer {r.get('cheeseXferA')}-{r.get('cheeseXferB')}"
                  f" cat {r.get('catDmgA')}-{r.get('catDmgB')} die {A.get('deathsRat')}/{B.get('deathsRat')}"
                  f" exc {A.get('exceptions')}/{B.get('exceptions')} ovr {A.get('overruns')}/{B.get('overruns')}"
                  f" bs {r.get('backstabRound')}")

if __name__ == '__main__':
    main(sys.argv[1:])
