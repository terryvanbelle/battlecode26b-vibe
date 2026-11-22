# Local tournament files

- `pool.txt`: active bots, one per line; `# NAME (reason)` lines are retired bots (tools/tourney.py reads the second
  word of every `#` line as a retired name, so no other comments go there).
- `champion.txt`: the current local champion (bots/ratbot is a copy of it); `champions.csv`: every champion.

## Candidate names (owner 11-22)

- **yNN** (yb9, yc2, yc3, yc4 ...): incremental candidates, one change on the champion.
- **swNN** (sw01, sw02 ...): big swings, a coherent strategy change on the champion (a bundle of settings or a new
  mechanism). High-risk by design: a losing swing gets a second version before its strategy is dropped.

| swing | strategy | built on |
|---|---|---|
| sw01 | empire: up to 5 kings, formed at war too, no income test, cheaper formation, caps 20 (retired: -197) | yb9 |
| sw02 | blitz: rushers spawn index 2-25 until r700, caps and core 26 (retired: -86, starving) | yb9 |
| sw03 | cat army: every baby after the first 3 fights cats, caps 22 (retired: -55) | yb9 |
| sw04 | empire v2: yb9's early game and gates, only the caps rise (5 kings, rich 2000/5, babies 20) (retired: -73) | yb9 |
| sw05 | home guard: every baby answers the king's alarm (ALERT_DIST2 3600), threat 50, defend 400, war cap 24 (retired: -71) | yb9 |
| sw06 | empire v3: sw04's king caps (5, rich 2000/5) with yb9's baby caps 16 (retired: -67) | yb9 |
| sw07 | blitz v2: every baby from index 2 rushes until r700, caps 16 (retired: -114) | yb9 |
| sw08 | forward hold: fighters take contested mines at war (FIGHT_POST 8), everyone fights (WAR_GATHER 0) | yb9 |
| sw09 | trap-first: rat traps at dist2 40, from 40 cheese, reserve gate skipped within dist2 400 of our king | yb9 |
| sw10 | king finisher: carriers seek a king within 16 and hold for a king hit to the last round, THROW_MIN 60 | yb9 |
| sw11 | forward hold + trap-first (sw08 + sw09) | yb9 |
| sw12 | pressure: forward hold + king finisher (sw08 + sw10) | yb9 |
