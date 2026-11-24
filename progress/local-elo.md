# Local tournament Elo

Bradley-Terry ratings of our own bots in the local tournament (`tools/tourney.py`), all 62721 games in one fit as of 2026-11-24 00:47 PDT, so every point is on the same scale; earlier champions are shown at their current rating. Regenerate with `tools/tourney.py chart`.

![local elo](local-elo.png)

| Champion from (PDT) | Bot | Change on base | Submission | Rating +- SE | Games |
|---|---|---|---:|---:|---:|
| 2026-11-21 08:20 | xk6 | starting champion (tournament began) |  | 1417 +- 11 | 1082 |
| 2026-11-21 09:13 | xr49 | MAX_BABIES 14 on xk6 | #432 | 1439 +- 10 | 1209 |
| 2026-11-21 11:12 | xs2 | KING_THREAT_D2 26 on xr49 | #433 | 1456 +- 8 | 1848 |
| 2026-11-21 13:27 | xt2 | MAX_BABIES 15 on xs2 | #434 | 1455 +- 9 | 1689 |
| 2026-11-21 15:07 | xu2 | KING_THREAT_D2 30 on xt2 | #435 | 1453 +- 8 | 2084 |
| 2026-11-21 17:50 | xw6 | THROW_HOLD 10 on xu2 | #436 | 1488 +- 9 | 1747 |
| 2026-11-21 19:55 | xw9 | MAX_BABIES 16 on xw6 | #437 | 1488 +- 8 | 1914 |
| 2026-11-21 21:55 | xx9 | GRAB_THREAT 15 on xw9 | #438 | 1478 +- 9 | 1666 |
| 2026-11-21 23:15 | xz2 | LOW_FLOOR 200 on xx9 | #439 | 1486 +- 10 | 1373 |
| 2026-11-22 00:55 | ya0 | KING_RESERVE 400 on xz2 | #440 | 1497 +- 8 | 2129 |
| 2026-11-22 03:55 | ya9 | GATHER_CAT_DIST 32 on ya0 | #441 | 1508 +- 10 | 1176 |
| 2026-11-22 04:55 | yb7 | CAT_DEFEND_R2 64 on ya9 | #442 | 1497 +- 11 | 1175 |
| 2026-11-22 06:17 | yb9 | throw dodge: dodge any carrying enemy rat on yb7 | #443 | 1485 +- 7 | 2782 |
| 2026-11-22 10:36 | sw08 | forward hold: FIGHT_POST 8 + WAR_GATHER 0 (big swing) on yb9 | #444 | 1497 +- 9 | 1424 |
| 2026-11-22 12:56 | yd1 | FIGHT_POST 12 on sw08 | #445 | 1515 +- 10 | 1252 |
| 2026-11-22 14:56 | sw19 | FIGHT_POST 12 + cat endgame END_CAT 1600 END_CAT_COOP 1200 (big swing) on sw08 | #446 | 1526 +- 10 | 1188 |
| 2026-11-22 16:57 | ye4 | END_CAT 1400 on sw19 | #447 | 1522 +- 9 | 1530 |
| 2026-11-22 20:56 | yf2 | DEFEND_D2 225 on ye4 | #448 | 1520 +- 11 | 1035 |
| 2026-11-22 22:36 | sw38 | RUSH_UNTIL 200 + END_CAT_COOP 1000 on yf2 | #449 | 1512 +- 11 | 986 |
| 2026-11-23 00:36 | yh3 | INCOME_TRUST 50 on sw38 | #450 | 1534 +- 13 | 714 |
| 2026-11-23 02:16 | yh6 | INCOME_TRUST 30 on sw38 | #451 | 1577 +- 9 | 1606 |
| 2026-11-23 06:36 | yk1 | FIGHT_POST 10 on yh6 | #452 | 1550 +- 9 | 1378 |
| 2026-11-23 09:14 | sw53 | RAID_SHARE 2 + FWD_TRAP (raiders + forward traps; sparring partner crowned) on yk1 | #453 | 1577 +- 11 | 944 |
| 2026-11-23 10:34 | sw54 | MAX_BABIES_WAR 20 + RICH_BANK 600 + CORE_BABIES 20 + KING_RESERVE 300 (sw50's bank on bodies on the raider line; big swing) on sw53 | #454 | 1622 +- 9 | 1525 |
| 2026-11-23 13:06 | sw58 | FINISH_K 8 + FINISH_FLOOR 100 (sw56's kill finishing ported to sw54; big swing) on sw54 | #470 | 1635 +- 8 | 1862 |
| 2026-11-23 14:03 | yl19 | FINISH_FLOOR 100 alone (from 200; sw58's floor without its FINISH_K 8) on sw54 | #474 | 1644 +- 8 | 2055 |
| 2026-11-23 18:08 | yl25 | RETREAT_HP 0 (from 40: wounded babies no longer walk back to our king; incremental) on yl19 | #475 | 1683 +- 8 | 1811 |
| 2026-11-24 00:05 | yl32 | STANDOFF_EQ (the standoff holds only against a healthier enemy; an equal one is walked to and bitten first; incremental) on yl25 | #476 | 1708 +- 18 | 374 |
