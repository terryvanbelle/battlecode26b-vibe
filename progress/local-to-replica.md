# Local tournament rating to replica rating

Fitted 2026-11-23 14:52 PDT by `research/champ-corr/fit_local_replica.py` (owner 11-23 14:33: map local Elo to replica Elo and predict when the local tournament produces a bot that ranks 15th, 10th or 1st). Chart: [local-to-replica.png](local-to-replica.png).

## The mapping

Each game is a coin flip with the Elo odds of the replica (siarnaq: scale 400, a match scores its share of games won), so a bot's replica rating R wins a game against a team rated R_opp with probability 1 / (1 + 10^((R_opp - R) / 400)), and R = intercept + slope x (local - 1550). Fitted by maximum likelihood:

| fit | games | replica rating at local 1550 | replica points per local point | the champion yl19 |
|---|---|---|---|---|
| A: the study, 10 champions x 21 maps vs AlexT101_usqualsbot (placed at its rating now, 1740) | 210 | 1740 +- 27 | 0.69 +- 0.43 | 1832 |
| B: our ranked ladder games since 11-21 08:20 (107 matches), each with its active champion and the opponent's rating then | 321 | 1751 +- 22 | 0.40 +- 0.37 | 1805 |
| joint: A and B together | 531 | 1747 +- 17 | 0.53 +- 0.28 | 1817 |

- **Read:** a local point is worth about 0.5 of a replica point (one SE: 0.25 to 0.80). The champion yl19 (local 1683 +- 17) should settle near 1817 on the replica; it shows 1761 (#17) now.
- **Range measured:** local 1448 to 1683. Anything above it is an extrapolation.

## When

From yl19 at the champions' pace: the least-squares slope of the champions' local rating against their crowning time, over the last 24 h (+154 +- 24 a day, 11 champions) or the whole run (+85 +- 7 a day, 26 champions). The ladder places either hold today's rating or move at their 48-hour drift less AlexT101_usqualsbot's (+10 a day; our own rating is taken to drift like it). Median and 80% interval over 20000 draws of the joint fit, the champion's rating and the pace:

| place | rating now | drift a day (vs AlexT101_usqualsbot) | local needed | last-24-h pace, places held | last-24-h pace, places drifting | whole-run pace, places held | whole-run pace, places drifting |
|---|---|---|---|---|---|---|---|
| #15 | 1774 | +12 (+2) | 1602 | **now** (now to 11-24 07:49) | **now** (now to 11-24 08:59) | **now** (now to 11-24 21:35) | **now** (now to 11-25 02:01) |
| #10 | 1855 | +27 (+17) | 1756 | **11-24 02:56** (now to 11-27 12:09) | **11-24 06:09** (now to 12-05 12:58) | **11-24 12:55** (now to 11-30 13:40) | **11-25 02:21** (now to > 60 days) |
| #1 | 2205 | +43 (+33) | 2421 | **11-28 14:01** (11-26 00:37 to 12-11 13:02) | **12-02 03:13** (11-26 18:42 to > 60 days) | **12-02 11:15** (11-28 01:14 to 12-25 11:23) | **12-28 06:25** (12-01 12:28 to > 60 days) |

Medians of the single fits, for comparison (last-24-h pace, places held): #15: A now, B now; #10: A 11-23 20:15, B 11-24 10:46; #1: A 11-27 03:47, B 11-30 05:05.

## Per champion

| champion | crowned (PDT) | local | study vs AlexT101_usqualsbot (W-L of 21) | its rating from the study | ladder performance +- SE (games) |
|---|---|---|---|---|---|
| xk6 | 11-21 08:20 | 1427 |  |  |  |
| xr49 | 11-21 09:13 | 1447 | 7-14 | 1625 | 1531 +- 152 (6) |
| xs2 | 11-21 11:12 | 1463 |  |  | 1839 +- 116 (12) |
| xt2 | 11-21 13:27 | 1463 |  |  | 1772 +- 92 (15) |
| xu2 | 11-21 15:07 | 1461 | 8-13 | 1660 | 1646 +- 102 (12) |
| xw6 | 11-21 17:50 | 1495 |  |  | 1823 +- 123 (9) |
| xw9 | 11-21 19:55 | 1495 | 10-11 | 1724 | 1788 +- 73 (24) |
| xx9 | 11-21 21:55 | 1485 |  |  |  |
| xz2 | 11-21 23:15 | 1493 |  |  | 1604 +- 123 (9) |
| ya0 | 11-22 00:55 | 1504 | 13-8 | 1820 | 1703 +- 61 (33) |
| ya9 | 11-22 03:55 | 1515 |  |  | 1769 +- 102 (12) |
| yb7 | 11-22 04:55 | 1504 |  |  | 1814 +- 150 (6) |
| yb9 | 11-22 06:17 | 1493 | 8-13 | 1660 | 1671 +- 64 (30) |
| sw08 | 11-22 10:36 | 1505 |  |  | 1826 +- 95 (15) |
| yd1 | 11-22 12:56 | 1524 | 11-10 | 1756 | 1737 +- 67 (27) |
| sw19 | 11-22 14:56 | 1536 |  |  | 1583 +- 213 (3) |
| ye4 | 11-22 16:57 | 1533 | 10-11 | 1724 | 1585 +- 87 (18) |
| yf2 | 11-22 20:56 | 1533 |  |  | 1780 +- 108 (12) |
| sw38 | 11-22 22:36 | 1526 | 9-12 | 1692 | 1746 +- 85 (18) |
| yh3 | 11-23 00:36 | 1549 |  |  | 1822 +- 106 (12) |
| yh6 | 11-23 02:16 | 1593 |  |  | 1500 +- 139 (9) |
| yk1 | 11-23 06:36 | 1573 | 11-10 | 1756 |  |
| sw53 | 11-23 09:14 | 1604 |  |  | 1609 +- 213 (3) |
| sw54 | 11-23 10:34 | 1656 | 12-9 | 1788 | 1842 +- 87 (18) |
| sw58 | 11-23 13:06 | 1649 |  |  |  |
| yl19 | 11-23 14:03 | 1683 |  |  | 1868 +- 123 (9) |

## Caveats

- **The slope is the weak link.** 0.53 +- 0.28: the local range measured is narrow and the replica games are few. The per-game binomial model also treats the replica's games as independent, while the replica is deterministic (one seed per map, 9 of 21 study maps decided the same way for every champion).
- **#1 is an extrapolation far past the data.** It needs a local rating around 2421, about 738 above yl19: a bot that beats today's champion nearly every game. Nothing says local gains keep converting at the same rate that far out.
- **The targets move.** The top of the ladder still climbs as the replica's ratings spread (place 1: about +63 a day two days ago, +34 in the last 24 h). The two target columns bound this.
- **The local scale moves.** Champions' ratings shift by tens of points as games accumulate (sw58 crowned +26 over sw54, now below it), and the pace has varied: about +60 a day on the first day, +160 in the last.
- **Our displayed rating lags.** A new submission needs a few dozen ranked games to settle (K 24).
