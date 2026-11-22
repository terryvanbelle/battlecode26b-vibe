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
| sw02 | blitz: rushers spawn index 2-25 until r700, caps and core 26 | yb9 |
| sw03 | cat army: every baby after the first 3 fights cats, caps 22 | yb9 |
| sw04 | empire v2: yb9's early game and gates, only the caps rise (5 kings, rich 2000/5, babies 20) | yb9 |
| sw05 | home guard: every baby answers the king's alarm (ALERT_DIST2 3600), threat 50, defend 400, war cap 24 | yb9 |
| sw06 | empire v3: sw04's king caps (5, rich 2000/5) with yb9's baby caps 16 | yb9 |
