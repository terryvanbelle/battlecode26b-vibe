# Local tournament Elo

Bradley-Terry ratings of our own bots in the local tournament (`tools/tourney.py`), all 70813 games in one fit as of 2026-11-24 08:33 PDT, so every point is on the same scale; earlier champions are shown at their current rating. Regenerate with `tools/tourney.py chart`.

![local elo](local-elo.png)

| Champion from (PDT) | Bot | Change on base | Submission | Rating +- SE | Games |
|---|---|---|---:|---:|---:|
| 2026-11-21 08:20 | xk6 | starting champion (tournament began) |  | 1410 +- 11 | 1082 |
| 2026-11-21 09:13 | xr49 | MAX_BABIES 14 on xk6 | #432 | 1434 +- 10 | 1209 |
| 2026-11-21 11:12 | xs2 | KING_THREAT_D2 26 on xr49 | #433 | 1451 +- 8 | 1848 |
| 2026-11-21 13:27 | xt2 | MAX_BABIES 15 on xs2 | #434 | 1451 +- 9 | 1689 |
| 2026-11-21 15:07 | xu2 | KING_THREAT_D2 30 on xt2 | #435 | 1449 +- 8 | 2084 |
| 2026-11-21 17:50 | xw6 | THROW_HOLD 10 on xu2 | #436 | 1483 +- 9 | 1747 |
| 2026-11-21 19:55 | xw9 | MAX_BABIES 16 on xw6 | #437 | 1484 +- 8 | 1914 |
| 2026-11-21 21:55 | xx9 | GRAB_THREAT 15 on xw9 | #438 | 1474 +- 9 | 1666 |
| 2026-11-21 23:15 | xz2 | LOW_FLOOR 200 on xx9 | #439 | 1482 +- 10 | 1373 |
| 2026-11-22 00:55 | ya0 | KING_RESERVE 400 on xz2 | #440 | 1493 +- 8 | 2129 |
| 2026-11-22 03:55 | ya9 | GATHER_CAT_DIST 32 on ya0 | #441 | 1503 +- 10 | 1176 |
| 2026-11-22 04:55 | yb7 | CAT_DEFEND_R2 64 on ya9 | #442 | 1492 +- 11 | 1175 |
| 2026-11-22 06:17 | yb9 | throw dodge: dodge any carrying enemy rat on yb7 | #443 | 1480 +- 7 | 2782 |
| 2026-11-22 10:36 | sw08 | forward hold: FIGHT_POST 8 + WAR_GATHER 0 (big swing) on yb9 | #444 | 1492 +- 9 | 1424 |
| 2026-11-22 12:56 | yd1 | FIGHT_POST 12 on sw08 | #445 | 1509 +- 10 | 1252 |
| 2026-11-22 14:56 | sw19 | FIGHT_POST 12 + cat endgame END_CAT 1600 END_CAT_COOP 1200 (big swing) on sw08 | #446 | 1520 +- 10 | 1188 |
| 2026-11-22 16:57 | ye4 | END_CAT 1400 on sw19 | #447 | 1514 +- 9 | 1530 |
| 2026-11-22 20:56 | yf2 | DEFEND_D2 225 on ye4 | #448 | 1512 +- 11 | 1035 |
| 2026-11-22 22:36 | sw38 | RUSH_UNTIL 200 + END_CAT_COOP 1000 on yf2 | #449 | 1504 +- 11 | 986 |
| 2026-11-23 00:36 | yh3 | INCOME_TRUST 50 on sw38 | #450 | 1525 +- 13 | 714 |
| 2026-11-23 02:16 | yh6 | INCOME_TRUST 30 on sw38 | #451 | 1566 +- 9 | 1606 |
| 2026-11-23 06:36 | yk1 | FIGHT_POST 10 on yh6 | #452 | 1535 +- 9 | 1378 |
| 2026-11-23 09:14 | sw53 | RAID_SHARE 2 + FWD_TRAP (raiders + forward traps; sparring partner crowned) on yk1 | #453 | 1560 +- 11 | 944 |
| 2026-11-23 10:34 | sw54 | MAX_BABIES_WAR 20 + RICH_BANK 600 + CORE_BABIES 20 + KING_RESERVE 300 (sw50's bank on bodies on the raider line; big swing) on sw53 | #454 | 1601 +- 9 | 1525 |
| 2026-11-23 13:06 | sw58 | FINISH_K 8 + FINISH_FLOOR 100 (sw56's kill finishing ported to sw54; big swing) on sw54 | #470 | 1612 +- 8 | 1862 |
| 2026-11-23 14:03 | yl19 | FINISH_FLOOR 100 alone (from 200; sw58's floor without its FINISH_K 8) on sw54 | #474 | 1620 +- 8 | 2055 |
| 2026-11-23 18:08 | yl25 | RETREAT_HP 0 (from 40: wounded babies no longer walk back to our king; incremental) on yl19 | #475 | 1658 +- 8 | 2001 |
| 2026-11-24 00:05 | yl32 | STANDOFF_EQ (the standoff holds only against a healthier enemy; an equal one is walked to and bitten first; incremental) on yl25 | #476 | 1671 +- 8 | 1892 |
| 2026-11-24 02:03 | yl33 | PREY_WEAKEST (a hunter chases the weakest weaker enemy baby within dist2 20 - the lowest health then the nearest - instead of the nearest; incremental) on yl25 | #477 | 1682 +- 8 | 1707 |
| 2026-11-24 05:04 | sw69 | LOW_FLOOR 50 (with fewer than LOW_BABIES 8 babies every spawn floor drops to 50 from 200 - usqualsbot's spend-down; on yl32 = yl25 + STANDOFF_EQ; swing) on yl32 | #478 | 1690 +- 9 | 1372 |
| 2026-11-24 07:35 | sw72 | PREY_WEAKEST + PREY_TRAP (sw68's hunt rework on sw69: hunters chase the weakest weaker enemy baby; a rat a healthier enemy hunter watches traps it first - gate-free; swing) on sw69 | #479 | 1712 +- 19 | 352 |
