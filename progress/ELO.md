# Ladder

7349 rows of progress/games.csv (5244 local games, 2105 replica matches), rated by a batch Bradley-Terry fit on the Elo scale (`tools/elolib.py`). A local game counts its cell score (`tools/score.py`: a win or loss when kings decide the game, the points share when points do), a replica match its games won. A game between one of our builds and a field bot also fits how hard its map is for us (table at the end), so builds gauntleted on different maps compare on an average map. Each pair of players counts at most 200 games, every player carries one virtual win and loss against a fixed anchor, and ratings are relative to a mean of 1500 over all 126 players. Each of our builds is its own player. Regenerate with `tools/elo.py`; do not edit.

Games against field bots are replica scrimmages only; local games are our builds against each other and the stock players. Record = games won and lost.

![ratings of our builds](elo.png)

Our builds, in attempt order (rating +- 95%; field score = expected score against each of the 51 field players, one game each):

| build | rating | rank | games | record | field score |
|---|---|---|---|---|---|
| s0e | 1274 +- 144 | 107 of 126 | 33 | 15-18 | 34.1% |
| s0m | 1335 +- 69 | 104 of 126 | 148 | 98-50 | 37.8% |
| s0u | 1348 +- 126 | 103 of 126 | 37 | 13-24 | 38.5% |
| v0d | 1637 +- 128 | 26 of 126 | 37 | 27-10 | 56.4% |
| s2o | 1460 +- 13 | 85 of 126 | 2868 | 1214-1654 | 45.4% |
| s3e | 1425 +- 76 | 97 of 126 | 80 | 29-51 | 43.2% |
| s3f | 1483 +- 75 | 71 of 126 | 80 | 46-34 | 46.8% |
| s3g | 1425 +- 76 | 98 of 126 | 80 | 30-50 | 43.2% |
| s2o_id | 1462 +- 75 | 84 of 126 | 80 | 40-40 | 45.5% |
| s3h | 1455 +- 54 | 88 of 126 | 188 | 100-88 | 45.1% |
| s3i | 1434 +- 75 | 95 of 126 | 80 | 27-53 | 43.8% |
| s3k | 1435 +- 75 | 93 of 126 | 80 | 29-51 | 43.9% |
| s3l | 1430 +- 76 | 96 of 126 | 80 | 27-53 | 43.5% |
| s3m | 1435 +- 75 | 94 of 126 | 80 | 30-50 | 43.9% |
| s3n | 1436 +- 75 | 92 of 126 | 80 | 29-51 | 43.9% |
| s3o | 1481 +- 75 | 72 of 126 | 80 | 50-30 | 46.7% |
| s3p | 1463 +- 75 | 82 of 126 | 80 | 43-37 | 45.5% |
| s3q | 1438 +- 75 | 91 of 126 | 80 | 35-45 | 44.1% |
| s3r | 1467 +- 75 | 80 of 126 | 80 | 46-34 | 45.8% |
| s3s | 1467 +- 75 | 81 of 126 | 80 | 46-34 | 45.8% |
| s3t | 1478 +- 75 | 75 of 126 | 80 | 49-31 | 46.5% |
| s3u | 1492 +- 76 | 67 of 126 | 80 | 53-27 | 47.3% |
| s3v | 1474 +- 75 | 78 of 126 | 80 | 54-26 | 46.2% |
| s3w | 1467 +- 75 | 79 of 126 | 80 | 52-28 | 45.8% |
| s3x | 1450 +- 75 | 90 of 126 | 80 | 48-32 | 44.8% |
| s3y | 1554 +- 53 | 38 of 126 | 235 | 134-101 | 51.1% |
| s3z | 1475 +- 75 | 77 of 126 | 80 | 50-30 | 46.3% |
| s4a | 1475 +- 75 | 76 of 126 | 80 | 50-30 | 46.3% |
| s4b | 1512 +- 53 | 55 of 126 | 172 | 117-55 | 48.5% |
| s4c | 1492 +- 76 | 68 of 126 | 80 | 53-27 | 47.3% |
| s4d | 1538 +- 34 | 43 of 126 | 462 | 287-175 | 50.2% |
| arush | 1418 +- 78 | 99 of 126 | 80 | 29-51 | 42.8% |
| s4e | 1515 +- 76 | 53 of 126 | 80 | 61-19 | 48.7% |
| s4f | 1529 +- 54 | 47 of 126 | 160 | 110-50 | 49.6% |
| abrk | 1509 +- 25 | 60 of 126 | 749 | 278-471 | 48.4% |
| s4g | 1571 +- 76 | 34 of 126 | 80 | 51-29 | 52.2% |
| s4h | 1548 +- 76 | 39 of 126 | 80 | 52-28 | 50.8% |
| s4i | 1560 +- 64 | 36 of 126 | 134 | 86-48 | 51.5% |
| s4j | 1577 +- 65 | 31 of 126 | 131 | 87-44 | 52.5% |
| s4m | 1591 +- 118 | 28 of 126 | 33 | 22-11 | 53.4% |
| s4n | 1547 +- 76 | 40 of 126 | 80 | 52-28 | 50.7% |
| s4q | 1498 +- 76 | 65 of 126 | 80 | 61-19 | 47.7% |
| s4r | 1498 +- 76 | 66 of 126 | 80 | 61-19 | 47.7% |
| s4t | 1488 +- 66 | 70 of 126 | 122 | 74-48 | 47.1% |
| s5c | 1512 +- 76 | 57 of 126 | 80 | 58-22 | 48.5% |
| s5e | 1480 +- 103 | 74 of 126 | 60 | 31-29 | 46.6% |
| s5g | 1527 +- 62 | 48 of 126 | 140 | 80-60 | 49.4% |
| s5j | 1580 +- 95 | 30 of 126 | 52 | 36-16 | 52.8% |
| araid | 1507 +- 18 | 62 of 126 | 1463 | 625-838 | 48.2% |
| s5o | 1511 +- 75 | 59 of 126 | 80 | 41-39 | 48.5% |
| s5r | 1505 +- 49 | 63 of 126 | 202 | 121-81 | 48.1% |
| s5s | 1560 +- 76 | 37 of 126 | 80 | 56-24 | 51.5% |
| s5t | 1507 +- 73 | 61 of 126 | 86 | 42-44 | 48.2% |
| s5w | 1652 +- 91 | 25 of 126 | 72 | 45-27 | 57.3% |
| s5x | 1499 +- 90 | 64 of 126 | 55 | 30-25 | 47.7% |
| s5y | 1546 +- 76 | 41 of 126 | 80 | 53-27 | 50.6% |
| s6c | 1315 +- 393 | 105 of 126 | 2 | 0-2 | 36.6% |
| s6e | 1521 +- 75 | 49 of 126 | 80 | 51-29 | 49.1% |
| s6f | 1514 +- 75 | 54 of 126 | 80 | 41-39 | 48.7% |
| s6g | 1531 +- 75 | 46 of 126 | 80 | 41-39 | 49.7% |
| s6j | 1537 +- 119 | 44 of 126 | 42 | 18-24 | 50.1% |
| s6m | 1511 +- 50 | 58 of 126 | 219 | 98-121 | 48.5% |
| s6o | 1480 +- 75 | 73 of 126 | 80 | 39-41 | 46.6% |
| s6p | 1540 +- 76 | 42 of 126 | 80 | 47-33 | 50.3% |
| s6s | 1490 +- 75 | 69 of 126 | 80 | 40-40 | 47.2% |
| s6v | 1534 +- 75 | 45 of 126 | 80 | 47-33 | 49.9% |
| s6y | 1573 +- 77 | 33 of 126 | 80 | 53-27 | 52.3% |
| s6z | 1573 +- 80 | 32 of 126 | 73 | 49-24 | 52.3% |
| s7d | 1569 +- 110 | 35 of 126 | 48 | 23-25 | 52.1% |
| s7f | 1517 +- 278 | 50 of 126 | 4 | 2-2 | 48.8% |
| s7h | 1516 +- 278 | 51 of 126 | 4 | 2-2 | 48.8% |
| s7i | 1588 +- 109 | 29 of 126 | 39 | 30-17 | 53.3% |
| s7j | 1512 +- 149 | 56 of 126 | 19 | 10-9 | 48.5% |
| s7k | 1516 +- 341 | 52 of 126 | 2 | 2-0 | 48.8% |
| ratbot | 1373 +- 69 | 102 of 126 | 154 | 107-47 | 40.1% |

All players:

| # | player | rating | games | record | field score |
|---:|---|---|---|---|---|
| 1 | IvanGeffner_basic51 | 2145 +- 54 | 198 | 137-61 | 89.6% |
| 2 | uravt_Version41 | 2112 +- 55 | 189 | 125-64 | 87.9% |
| 3 | uravt_Version35 | 2052 +- 53 | 183 | 108-75 | 84.6% |
| 4 | awu7_awubot_us | 2033 +- 57 | 174 | 106-68 | 83.5% |
| 5 | abcdezyxwv_bestsofar | 2020 +- 51 | 321 | 236-85 | 82.7% |
| 6 | nzjt_TheComplexMerlin | 1998 +- 61 | 159 | 102-57 | 81.3% |
| 7 | thewaxmango_PhD | 1990 +- 55 | 195 | 118-77 | 80.8% |
| 8 | quesswho_bot16 | 1981 +- 50 | 210 | 117-93 | 80.2% |
| 9 | daannte_v52_ThisGuy | 1962 +- 49 | 216 | 118-98 | 78.9% |
| 10 | quesswho_quals | 1961 +- 49 | 213 | 117-96 | 78.9% |
| 11 | awu7_awubot_quals | 1955 +- 55 | 183 | 108-75 | 78.4% |
| 12 | battlecode-archive_testplayer | 1949 +- 48 | 234 | 133-101 | 78.1% |
| 13 | nubbbbbb_testplayer | 1944 +- 56 | 171 | 97-74 | 77.7% |
| 14 | TeamSPAARK_SPAARK | 1922 +- 51 | 210 | 118-92 | 76.2% |
| 15 | daannte_v47_Skadoosh | 1918 +- 50 | 225 | 124-101 | 75.9% |
| 16 | AlexT101_finalsbot | 1825 +- 50 | 231 | 121-110 | 69.4% |
| 17 | AlexT101_usqualsbot | 1825 +- 43 | 342 | 222-120 | 69.3% |
| 18 | bman12345678_goonv23 | 1797 +- 58 | 189 | 107-82 | 67.4% |
| 19 | AdamTan12_Finals | 1786 +- 48 | 264 | 157-107 | 66.6% |
| 20 | thewaxmango_collegejunior | 1785 +- 56 | 183 | 102-81 | 66.5% |
| 21 | AdamTan12_Icarus | 1778 +- 47 | 264 | 139-125 | 66.0% |
| 22 | HugoIngelsson_YokoOno | 1745 +- 55 | 198 | 106-92 | 63.7% |
| 23 | VarunVejalla_final4_run_away10 | 1723 +- 62 | 165 | 94-71 | 62.2% |
| 24 | HugoIngelsson_Xzibit | 1673 +- 58 | 186 | 103-83 | 58.8% |
| 25 | us:s5w | 1652 +- 91 | 72 | 45-27 | 57.3% |
| 26 | us:v0d | 1637 +- 128 | 37 | 27-10 | 56.4% |
| 27 | spsquared_TSPAARK_QUALS | 1629 +- 50 | 237 | 129-108 | 55.9% |
| 28 | us:s4m | 1591 +- 118 | 33 | 22-11 | 53.4% |
| 29 | us:s7i | 1588 +- 109 | 39 | 30-17 | 53.3% |
| 30 | us:s5j | 1580 +- 95 | 52 | 36-16 | 52.8% |
| 31 | us:s4j | 1577 +- 65 | 131 | 87-44 | 52.5% |
| 32 | us:s6z | 1573 +- 80 | 73 | 49-24 | 52.3% |
| 33 | us:s6y | 1573 +- 77 | 80 | 53-27 | 52.3% |
| 34 | us:s4g | 1571 +- 76 | 80 | 51-29 | 52.2% |
| 35 | us:s7d | 1569 +- 110 | 48 | 23-25 | 52.1% |
| 36 | us:s4i | 1560 +- 64 | 134 | 86-48 | 51.5% |
| 37 | us:s5s | 1560 +- 76 | 80 | 56-24 | 51.5% |
| 38 | us:s3y | 1554 +- 53 | 235 | 134-101 | 51.1% |
| 39 | us:s4h | 1548 +- 76 | 80 | 52-28 | 50.8% |
| 40 | us:s4n | 1547 +- 76 | 80 | 52-28 | 50.7% |
| 41 | us:s5y | 1546 +- 76 | 80 | 53-27 | 50.6% |
| 42 | us:s6p | 1540 +- 76 | 80 | 47-33 | 50.3% |
| 43 | us:s4d | 1538 +- 34 | 462 | 287-175 | 50.2% |
| 44 | us:s6j | 1537 +- 119 | 42 | 18-24 | 50.1% |
| 45 | us:s6v | 1534 +- 75 | 80 | 47-33 | 49.9% |
| 46 | us:s6g | 1531 +- 75 | 80 | 41-39 | 49.7% |
| 47 | us:s4f | 1529 +- 54 | 160 | 110-50 | 49.6% |
| 48 | us:s5g | 1527 +- 62 | 140 | 80-60 | 49.4% |
| 49 | us:s6e | 1521 +- 75 | 80 | 51-29 | 49.1% |
| 50 | us:s7f | 1517 +- 278 | 4 | 2-2 | 48.8% |
| 51 | us:s7h | 1516 +- 278 | 4 | 2-2 | 48.8% |
| 52 | us:s7k | 1516 +- 341 | 2 | 2-0 | 48.8% |
| 53 | us:s4e | 1515 +- 76 | 80 | 61-19 | 48.7% |
| 54 | us:s6f | 1514 +- 75 | 80 | 41-39 | 48.7% |
| 55 | us:s4b | 1512 +- 53 | 172 | 117-55 | 48.5% |
| 56 | us:s7j | 1512 +- 149 | 19 | 10-9 | 48.5% |
| 57 | us:s5c | 1512 +- 76 | 80 | 58-22 | 48.5% |
| 58 | us:s6m | 1511 +- 50 | 219 | 98-121 | 48.5% |
| 59 | us:s5o | 1511 +- 75 | 80 | 41-39 | 48.5% |
| 60 | us:abrk | 1509 +- 25 | 749 | 278-471 | 48.4% |
| 61 | us:s5t | 1507 +- 73 | 86 | 42-44 | 48.2% |
| 62 | us:araid | 1507 +- 18 | 1463 | 625-838 | 48.2% |
| 63 | us:s5r | 1505 +- 49 | 202 | 121-81 | 48.1% |
| 64 | us:s5x | 1499 +- 90 | 55 | 30-25 | 47.7% |
| 65 | us:s4q | 1498 +- 76 | 80 | 61-19 | 47.7% |
| 66 | us:s4r | 1498 +- 76 | 80 | 61-19 | 47.7% |
| 67 | us:s3u | 1492 +- 76 | 80 | 53-27 | 47.3% |
| 68 | us:s4c | 1492 +- 76 | 80 | 53-27 | 47.3% |
| 69 | us:s6s | 1490 +- 75 | 80 | 40-40 | 47.2% |
| 70 | us:s4t | 1488 +- 66 | 122 | 74-48 | 47.1% |
| 71 | us:s3f | 1483 +- 75 | 80 | 46-34 | 46.8% |
| 72 | us:s3o | 1481 +- 75 | 80 | 50-30 | 46.7% |
| 73 | us:s6o | 1480 +- 75 | 80 | 39-41 | 46.6% |
| 74 | us:s5e | 1480 +- 103 | 60 | 31-29 | 46.6% |
| 75 | us:s3t | 1478 +- 75 | 80 | 49-31 | 46.5% |
| 76 | us:s4a | 1475 +- 75 | 80 | 50-30 | 46.3% |
| 77 | us:s3z | 1475 +- 75 | 80 | 50-30 | 46.3% |
| 78 | us:s3v | 1474 +- 75 | 80 | 54-26 | 46.2% |
| 79 | us:s3w | 1467 +- 75 | 80 | 52-28 | 45.8% |
| 80 | us:s3r | 1467 +- 75 | 80 | 46-34 | 45.8% |
| 81 | us:s3s | 1467 +- 75 | 80 | 46-34 | 45.8% |
| 82 | us:s3p | 1463 +- 75 | 80 | 43-37 | 45.5% |
| 83 | amcsz_fighterrats | 1462 +- 57 | 189 | 93-96 | 45.4% |
| 84 | us:s2o_id | 1462 +- 75 | 80 | 40-40 | 45.5% |
| 85 | us:s2o | 1460 +- 13 | 2868 | 1214-1654 | 45.4% |
| 86 | CS50Crab_rapierV1 | 1460 +- 42 | 327 | 153-174 | 45.3% |
| 87 | CS50Crab_protoSaberV7 | 1459 +- 55 | 204 | 101-103 | 45.2% |
| 88 | us:s3h | 1455 +- 54 | 188 | 100-88 | 45.1% |
| 89 | outercloudstudio_v_1_18 | 1451 +- 46 | 276 | 111-165 | 44.7% |
| 90 | us:s3x | 1450 +- 75 | 80 | 48-32 | 44.8% |
| 91 | us:s3q | 1438 +- 75 | 80 | 35-45 | 44.1% |
| 92 | us:s3n | 1436 +- 75 | 80 | 29-51 | 43.9% |
| 93 | us:s3k | 1435 +- 75 | 80 | 29-51 | 43.9% |
| 94 | us:s3m | 1435 +- 75 | 80 | 30-50 | 43.9% |
| 95 | us:s3i | 1434 +- 75 | 80 | 27-53 | 43.8% |
| 96 | us:s3l | 1430 +- 76 | 80 | 27-53 | 43.5% |
| 97 | us:s3e | 1425 +- 76 | 80 | 29-51 | 43.2% |
| 98 | us:s3g | 1425 +- 76 | 80 | 30-50 | 43.2% |
| 99 | us:arush | 1418 +- 78 | 80 | 29-51 | 42.8% |
| 100 | PixelRifts_mushakraj_mk3 | 1403 +- 53 | 225 | 106-119 | 41.7% |
| 101 | bc26vibe_bot | 1401 +- 41 | 339 | 152-187 | 41.7% |
| 102 | us:ratbot | 1373 +- 69 | 154 | 107-47 | 40.1% |
| 103 | us:s0u | 1348 +- 126 | 37 | 13-24 | 38.5% |
| 104 | us:s0m | 1335 +- 69 | 148 | 98-50 | 37.8% |
| 105 | us:s6c | 1315 +- 393 | 2 | 0-2 | 36.6% |
| 106 | XSercia_genies | 1305 +- 45 | 348 | 118-230 | 35.7% |
| 107 | us:s0e | 1274 +- 144 | 33 | 15-18 | 34.1% |
| 108 | C0derTang_v6 | 1252 +- 48 | 282 | 97-185 | 32.4% |
| 109 | ask8000_bumchit67 | 1233 +- 46 | 309 | 113-196 | 31.2% |
| 110 | Naton-Cai_ratplayer | 1229 +- 57 | 183 | 85-98 | 31.0% |
| 111 | justinottesen_alfredo | 1193 +- 52 | 225 | 102-123 | 28.8% |
| 112 | bc26-example | 1190 +- 35 | 565 | 216-349 | 28.6% |
| 113 | Camleaf_mk1 | 1153 +- 54 | 204 | 90-114 | 26.4% |
| 114 | bc26-example-copy | 1146 +- 50 | 219 | 99-120 | 25.9% |
| 115 | vincentwang60_javabot | 1136 +- 55 | 201 | 85-116 | 25.4% |
| 116 | Gavdingo_v1 | 1093 +- 49 | 237 | 103-134 | 22.9% |
| 117 | justinottesen_carbonara | 1078 +- 56 | 183 | 81-102 | 22.0% |
| 118 | battlecode-archive_result_408 | 1057 +- 52 | 210 | 93-117 | 20.9% |
| 119 | GuzziFlipFlops_Hermesv8 | 1033 +- 50 | 219 | 94-125 | 19.5% |
| 120 | j2399_weighted_micro_v9 | 1004 +- 58 | 186 | 77-109 | 18.0% |
| 121 | seru9_TOTAL_AGGRO | 953 +- 57 | 204 | 86-118 | 15.5% |
| 122 | quanticsoul4772_ratbot | 939 +- 55 | 216 | 97-119 | 14.9% |
| 123 | Victor-Ramirez-Garcia_battleca | 901 +- 58 | 216 | 87-129 | 13.2% |
| 124 | anicolao_myplayer | 659 +- 70 | 186 | 43-143 | 5.4% |
| 125 | j2399_weighted_path | 628 +- 75 | 138 | 39-99 | 4.7% |
| 126 | Krak9n_robot | 531 +- 83 | 138 | 23-115 | 2.9% |

Map handicap for our builds (Elo points; + = we score better there than on an average map of the mix, replica = the replica's map rotation):

| map | handicap |
|---|---:|
| DefaultLarge | +183 |
| DefaultMedium | +183 |
| DefaultSmall | +183 |
| EscapeTheNight | +183 |
| Meow | +183 |
| averystrangespace | +183 |
| constrictinglattice | +183 |
| corridorofdoomanddespair | +183 |
| dirtpassageway | +183 |
| hatefullattice | +183 |
| keepout | +183 |
| mercifullattice | +183 |
| minimaze | +183 |
| peaceinourtime | +183 |
| pipes | +183 |
| ratsareants | +183 |
| rift | +183 |
| safelycontained | +183 |
| sittingducks | +183 |
| streetsofnewyork | +183 |
| superioristhmus | +183 |
| trapped | +183 |
| Excavation | +152 |
| uneasy_alliance | +152 |
| wallsofparadis | +152 |
| externalsustenance | +124 |
| jail | +124 |
| Nofreecheese | +99 |
| cheesefarm | +99 |
| evileye | +99 |
| starvation | +99 |
| ZeroDay | +75 |
| bed | +75 |
| closeup | +75 |
| clouds | +75 |
| dirtfulcat | +75 |
| gridlock | +75 |
| knifefight | +75 |
| thunderdome | +75 |
| toomuchcheese | +75 |
| FreeTheCheese | +56 |
| cheeseguardians | +56 |
| replica | +39 |
| houseofcards | +38 |
| sunandmoon | +17 |
| tornadoes | -23 |
| canyoudig | -29 |
| TheHeist | -54 |
| groveisbeingdesecrated | -54 |
| rose | -54 |
| where_are_you | -110 |
| 5t4rv4t10n_1337 | -146 |
| diamond | -153 |
| arrows | -193 |
| uneruesansfin | -236 |
| attack_on_cat | -252 |
| heart | -252 |
| seepastyourgreed | -252 |
| learn_your_lesson | -262 |
| popthecork | -262 |
| krotus | -275 |
| Stash | -301 |
| UncomfortablyClose | -301 |
| cheesebottles | -352 |
| mincemeat | -352 |
| averyfineline | -382 |
| t_minus_five_minutes | -400 |
| tiny | -400 |
| ultimatestarvation | -400 |
| whatsthecatdoin | -400 |
| whereisthecheese | -400 |
| Hike | -461 |
| RUN | -461 |
| clockwisdom | -619 |
| fullofsecrets | -619 |
