# Local tournament files

- `pool.txt`: active bots, one per line; `# NAME (reason)` lines are retired bots (tools/tourney.py reads the second
  word of every `#` line as a retired name, so no other comments go there).
- `champion.txt`: the current local champion (bots/ratbot is a copy of it); `champions.csv`: every champion. A bot with
  its own unit tests has them in test/NAME/ (`bash tools/unit-tests.sh NAME`; same-named files replace test/ratbot's):
  when it is crowned, move test/NAME/*.java into test/ratbot/ so the default `bash tools/unit-tests.sh` keeps them.

## Candidate names (owner 11-22)

- **yNN** (yb9, yc2, yc3, yc4 ...): incremental candidates, one change on the champion.
- **Imitations** of a tactic replica opponents beat us with (owner 11-22) are built as swings (swNN). Build them from replay evidence of what the opponents' units do, and check the imitation's behaviour in replays before trusting it: sw35 never rushed (a RUSH_LIFE 0 bug) and sw48 played the champion's game (research/field/sparring-gap). Until 11-23 they formed a separate "sparring" kind (sw35, sw48, sw52, sw53), exempt from retirement. The owner dropped the category on 11-23 at about 10:00 PDT. An imitation weaker than the champion does not reproduce what beats us. A stronger one wins the tournament, as sw53 did, and every later bot inherits the tactic. So imitations now retire like any swing.
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
| sw09 | trap-first: rat traps at dist2 40, from 40 cheese, reserve gate skipped within dist2 400 of our king (retired: -34) | yb9 |
| sw10 | king finisher: carriers seek a king within 16 and hold for a king hit to the last round, THROW_MIN 60 (retired: -11 vs sw08, settled) | yb9 |
| sw11 | forward hold + trap-first (sw08 + sw09) (retired: -28 vs sw08) | yb9 |
| sw12 | pressure: forward hold + king finisher (sw08 + sw10) (retired: -20 vs sw08, settled) | yb9 |
| sw13 | greed: 10 gatherers first, 1 in 4 fights cats, 6 rushers (retired: -102) | yb9 |
| sw14 | late surge: richer tiers open at bank 500 / 1500, war cap 20 (retired: -25 vs sw08) | yb9 |
| sw15 | points endgame: endgame kings from r1500, endgame cat damage from r1600, rescue from r1700 (retired: -62) | sw08 |
| sw16 | cat endgame (points endgame v2): endgame cat damage from r1600 at war, r1200 in cooperation (retired: settled level, +1 vs yd1) | sw08 |
| sw17 | forward king: king seat and path guards 20 -> 5 (may sit nearer the bisector) (retired: -25 vs yd1; ported as sw20) | sw08 |
| sw18 | cat feeding: CARRY_CAT 12, CAT_FEED 300 (retired: -59 vs sw08) | sw08 |
| sw19 | yd1 + sw16: posts at FIGHT_POST 12 + cat endgame | sw08 |
| sw20 | forward king (sw17) on yd1 | yd1 |
| sw21 | siege economy: SIEGE_FLOOR 50 + DEF_TRAP_DIST2 400 | yd1 |
| sw22 | throw doctrine: far hits 70%, THROW_MIN 60, hold 5 | yd1 |
| sw23 | active king: roam 10, home mine within dist2 900 | yd1 |
| sw24 | king defence radius: threat dist2 64, defend dist2 225 | sw19 |
| sw25 | no opening rush: 0 rushers (from 12), forward hold carries forward presence | sw19 |
| sw26 | ye4 + sw24: END_CAT 1400 with the wider king defence | sw19 |
| sw27 | conservative opening: rush ends r150, king reserve 600 | sw19 |
| sw28 | economy opening: 8 gatherers first, 8 rushers, rush ends r200 | ye4 |
| sw29 | insurance king: second king from 12 babies, at war from r150 | ye4 |
| sw30 | ye5 + ye8 on ye4: rush ends r200, coop cat push r1000 | ye4 |
| sw31 | full loads: gatherers go home only with 40 raw | ye4 |
| sw32 | guard posture: face enemies within dist2 25, ammo distance 13 | ye4 |
| sw33 | austerity core: a broke king keeps 12 core babies, not 18 | ye4 |
| sw34 | ablation: austerity off | ye4 |
| sw35 | SPARRING: imitation of the opponents' grab-and-throw siege (rush all game, carry to the king); exempt from retirement | ye4 |
| sw36 | combination: yf2 DEFEND_D2 225 + yf4 INCOME_TRUST 60 | ye4 |
| sw37 | stack: sw30 (RUSH_UNTIL 200, END_CAT_COOP 1000) + yf2 DEFEND_D2 225 + yf3 WAR_FLOOR 450 | ye4 |
| sw38 | yf2 + RUSH_UNTIL 200 + END_CAT_COOP 1000 (sw30 on the new champion) | yf2 |
| sw39 | yf2 + RUSH_UNTIL 200 + END_CAT_COOP 1000 + INCOME_TRUST 60 (sw38 + sw36 stacked) | yf2 |
| sw40 | KING_RESERVE 250 on yf2 (spend down further for bodies) | yf2 |
| sw41 | KING_RESERVE 250 on sw38 | sw38 |
| sw42 | CORE_SMALL 16 on sw38 (more core rats on small maps) | sw38 |
| sw43 | sw38 + INCOME_TRUST 50 + KING_RESERVE 250 (yh3 + sw41 stacked) | sw38 |
| sw44 | CORE_BABIES 22 on yh3 | yh3 |
| sw45 | yh3 + INCOME_TRUST 30 + INCOME_WINDOW 150 (yh6 + yi2 stacked) | yh3 |
| sw46 | RUSH_TO 20 on yh6 (16 opening rushers, not 12) | yh6 |
| sw47 | FINISH_K 8 + FINISH_FLOOR 100 on yh6 (babies pay more cheese to finish kills) | yh6 |
| sw48 | SPARRING: MAX_BABIES 24/24 on yh6 (imitates the replica opponents' 20-30 rat armies) | yh6 |
| sw49 | spend the bank on bodies: RICH_BANK 600, MAX_BABIES_WAR 20, CORE_BABIES 20, KING_RESERVE 300 on yh6 | yh6 |
| sw50 | sw49 on yk1: RICH_BANK 600, MAX_BABIES_WAR 20, CORE_BABIES 20, KING_RESERVE 300 | yk1 |
| sw51 | SPARRING (diagnostic only, not entered): raiders from FIGHT rats + forward traps (code) | yk1 |
| sw52 | imitation: sw35's siege with its bug fixed (RUSH_UNTIL 2001, RUSH_LIFE 2001, CARRY_KING 10/1, traps from 50) (retired 11-23: -94 vs sw53 on 499) | yk1 |
| sw53 | imitation: raiders (half the gatherers and fighters hold enemy-half mines at war) + forward traps skip the reserve gate (code) (CHAMPION 11-23 09:14) | sw51 |
| sw54 | sw53's raiders and forward traps with sw50's bank on bodies | sw53 |
| sw55 | all forward at war: RAID_SHARE 4 (every gatherer and fighter raids) | sw53 |
| sw56 | kill finishing on the raider line: FINISH_K 8, FINISH_FLOOR 100 | sw53 |
| sw57 | all cheese into rats: no rat traps (TRAP_CAP 0) + sw50's bank on bodies | sw53 |
