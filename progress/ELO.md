# Ladder

7066 rows of progress/games.csv (5023 local games, 2043 replica matches), rated by a batch Bradley-Terry fit on the Elo scale (`tools/elolib.py`). A local game counts its cell score (`tools/score.py`: a win or loss when kings decide the game, the points share when points do), a replica match its games won. A game between one of our builds and a field bot also fits how hard its map is for us (table at the end), so builds gauntleted on different maps compare on an average map. Each pair of players counts at most 200 games, every player carries one virtual win and loss against a fixed anchor, and ratings are relative to a mean of 1500 over all 122 players. Each of our builds is its own player. Regenerate with `tools/elo.py`; do not edit.

Games against field bots are replica scrimmages only; local games are our builds against each other and the stock players. Record = games won and lost.

![ratings of our builds](elo.png)

Our builds, in attempt order (rating +- 95%; field score = expected score against each of the 51 field players, one game each):

| build | rating | rank | games | record | field score |
|---|---|---|---|---|---|
| s0e | 1275 +- 144 | 103 of 122 | 33 | 15-18 | 34.2% |
| s0m | 1337 +- 69 | 100 of 122 | 148 | 98-50 | 37.9% |
| s0u | 1349 +- 126 | 99 of 122 | 37 | 13-24 | 38.7% |
| v0d | 1639 +- 128 | 27 of 122 | 37 | 27-10 | 56.5% |
| s2o | 1461 +- 13 | 80 of 122 | 2868 | 1214-1654 | 45.4% |
| s3e | 1425 +- 76 | 93 of 122 | 80 | 29-51 | 43.3% |
| s3f | 1484 +- 75 | 67 of 122 | 80 | 46-34 | 46.8% |
| s3g | 1425 +- 76 | 94 of 122 | 80 | 30-50 | 43.3% |
| s2o_id | 1462 +- 75 | 79 of 122 | 80 | 40-40 | 45.5% |
| s3h | 1456 +- 54 | 83 of 122 | 188 | 100-88 | 45.2% |
| s3i | 1434 +- 75 | 91 of 122 | 80 | 27-53 | 43.8% |
| s3k | 1436 +- 75 | 89 of 122 | 80 | 29-51 | 43.9% |
| s3l | 1430 +- 76 | 92 of 122 | 80 | 27-53 | 43.6% |
| s3m | 1436 +- 75 | 90 of 122 | 80 | 30-50 | 43.9% |
| s3n | 1437 +- 75 | 88 of 122 | 80 | 29-51 | 44.0% |
| s3o | 1482 +- 75 | 68 of 122 | 80 | 50-30 | 46.7% |
| s3p | 1463 +- 75 | 78 of 122 | 80 | 43-37 | 45.6% |
| s3q | 1439 +- 75 | 87 of 122 | 80 | 35-45 | 44.1% |
| s3r | 1468 +- 75 | 76 of 122 | 80 | 46-34 | 45.9% |
| s3s | 1468 +- 75 | 77 of 122 | 80 | 46-34 | 45.9% |
| s3t | 1479 +- 75 | 70 of 122 | 80 | 49-31 | 46.5% |
| s3u | 1493 +- 76 | 63 of 122 | 80 | 53-27 | 47.4% |
| s3v | 1474 +- 75 | 73 of 122 | 80 | 54-26 | 46.3% |
| s3w | 1468 +- 75 | 75 of 122 | 80 | 52-28 | 45.9% |
| s3x | 1451 +- 75 | 85 of 122 | 80 | 48-32 | 44.8% |
| s3y | 1554 +- 53 | 36 of 122 | 235 | 134-101 | 51.2% |
| s3z | 1476 +- 75 | 72 of 122 | 80 | 50-30 | 46.3% |
| s4a | 1476 +- 75 | 71 of 122 | 80 | 50-30 | 46.3% |
| s4b | 1513 +- 53 | 51 of 122 | 172 | 117-55 | 48.6% |
| s4c | 1493 +- 76 | 64 of 122 | 80 | 53-27 | 47.4% |
| s4d | 1537 +- 34 | 41 of 122 | 462 | 287-175 | 50.1% |
| arush | 1418 +- 78 | 95 of 122 | 80 | 29-51 | 42.9% |
| s4e | 1516 +- 76 | 49 of 122 | 80 | 61-19 | 48.8% |
| s4f | 1529 +- 54 | 45 of 122 | 160 | 110-50 | 49.6% |
| abrk | 1509 +- 25 | 54 of 122 | 749 | 278-471 | 48.4% |
| s4g | 1571 +- 76 | 34 of 122 | 80 | 51-29 | 52.2% |
| s4h | 1548 +- 76 | 38 of 122 | 80 | 52-28 | 50.8% |
| s4i | 1560 +- 64 | 35 of 122 | 134 | 86-48 | 51.6% |
| s4j | 1578 +- 65 | 33 of 122 | 131 | 87-44 | 52.6% |
| s4m | 1591 +- 118 | 29 of 122 | 33 | 22-11 | 53.5% |
| s4n | 1548 +- 76 | 39 of 122 | 80 | 52-28 | 50.8% |
| s4q | 1498 +- 76 | 60 of 122 | 80 | 61-19 | 47.7% |
| s4r | 1498 +- 76 | 61 of 122 | 80 | 61-19 | 47.7% |
| s4t | 1489 +- 66 | 65 of 122 | 122 | 74-48 | 47.2% |
| s5c | 1512 +- 76 | 52 of 122 | 80 | 58-22 | 48.6% |
| s5e | 1481 +- 103 | 69 of 122 | 60 | 31-29 | 46.7% |
| s5g | 1528 +- 62 | 46 of 122 | 140 | 80-60 | 49.5% |
| s5j | 1580 +- 95 | 32 of 122 | 52 | 36-16 | 52.8% |
| araid | 1501 +- 19 | 59 of 122 | 1250 | 541-709 | 47.9% |
| s5o | 1505 +- 75 | 56 of 122 | 80 | 41-39 | 48.1% |
| s5r | 1502 +- 49 | 57 of 122 | 202 | 121-81 | 48.0% |
| s5s | 1553 +- 76 | 37 of 122 | 80 | 56-24 | 51.1% |
| s5t | 1501 +- 73 | 58 of 122 | 86 | 42-44 | 47.9% |
| s5w | 1650 +- 91 | 26 of 122 | 72 | 45-27 | 57.2% |
| s5x | 1494 +- 90 | 62 of 122 | 55 | 30-25 | 47.5% |
| s5y | 1540 +- 76 | 40 of 122 | 80 | 53-27 | 50.3% |
| s6c | 1312 +- 393 | 101 of 122 | 2 | 0-2 | 36.4% |
| s6e | 1515 +- 75 | 50 of 122 | 80 | 51-29 | 48.7% |
| s6f | 1508 +- 75 | 55 of 122 | 80 | 41-39 | 48.3% |
| s6g | 1525 +- 75 | 47 of 122 | 80 | 41-39 | 49.4% |
| s6j | 1537 +- 119 | 42 of 122 | 42 | 18-24 | 50.1% |
| s6m | 1519 +- 51 | 48 of 122 | 212 | 97-115 | 49.0% |
| s6o | 1474 +- 75 | 74 of 122 | 80 | 39-41 | 46.3% |
| s6p | 1534 +- 76 | 43 of 122 | 80 | 47-33 | 49.9% |
| s6s | 1484 +- 75 | 66 of 122 | 80 | 40-40 | 46.9% |
| s6v | 1533 +- 88 | 44 of 122 | 58 | 35-23 | 49.9% |
| s6y | 1590 +- 124 | 31 of 122 | 30 | 22-8 | 53.4% |
| s6z | 1720 +- 273 | 23 of 122 | 7 | 6-1 | 61.8% |
| s7d | 1510 +- 278 | 53 of 122 | 4 | 2-2 | 48.5% |
| s7f | 1591 +- 351 | 30 of 122 | 2 | 1-1 | 53.4% |
| ratbot | 1375 +- 69 | 98 of 122 | 154 | 107-47 | 40.2% |

All players:

| # | player | rating | games | record | field score |
|---:|---|---|---|---|---|
| 1 | IvanGeffner_basic51 | 2152 +- 56 | 189 | 133-56 | 89.9% |
| 2 | uravt_Version41 | 2114 +- 57 | 183 | 122-61 | 88.0% |
| 3 | uravt_Version35 | 2053 +- 54 | 180 | 107-73 | 84.7% |
| 4 | awu7_awubot_us | 2034 +- 57 | 174 | 106-68 | 83.5% |
| 5 | abcdezyxwv_bestsofar | 2015 +- 52 | 309 | 227-82 | 82.4% |
| 6 | thewaxmango_PhD | 1991 +- 56 | 189 | 115-74 | 80.9% |
| 7 | nzjt_TheComplexMerlin | 1990 +- 63 | 153 | 97-56 | 80.8% |
| 8 | quesswho_bot16 | 1977 +- 51 | 207 | 114-93 | 79.9% |
| 9 | quesswho_quals | 1966 +- 50 | 207 | 115-92 | 79.2% |
| 10 | daannte_v52_ThisGuy | 1960 +- 49 | 213 | 116-97 | 78.8% |
| 11 | awu7_awubot_quals | 1960 +- 57 | 174 | 105-69 | 78.8% |
| 12 | battlecode-archive_testplayer | 1944 +- 49 | 228 | 128-100 | 77.7% |
| 13 | nubbbbbb_testplayer | 1944 +- 57 | 165 | 94-71 | 77.7% |
| 14 | TeamSPAARK_SPAARK | 1925 +- 53 | 201 | 115-86 | 76.4% |
| 15 | daannte_v47_Skadoosh | 1916 +- 51 | 219 | 120-99 | 75.8% |
| 16 | AlexT101_finalsbot | 1833 +- 51 | 219 | 117-102 | 70.0% |
| 17 | AlexT101_usqualsbot | 1830 +- 44 | 330 | 216-114 | 69.8% |
| 18 | bman12345678_goonv23 | 1804 +- 59 | 183 | 105-78 | 67.9% |
| 19 | thewaxmango_collegejunior | 1785 +- 57 | 177 | 99-78 | 66.5% |
| 20 | AdamTan12_Icarus | 1780 +- 48 | 255 | 136-119 | 66.2% |
| 21 | AdamTan12_Finals | 1779 +- 49 | 252 | 148-104 | 66.1% |
| 22 | HugoIngelsson_YokoOno | 1744 +- 55 | 198 | 106-92 | 63.7% |
| 23 | us:s6z | 1720 +- 273 | 7 | 6-1 | 61.8% |
| 24 | VarunVejalla_final4_run_away10 | 1712 +- 63 | 162 | 91-71 | 61.5% |
| 25 | HugoIngelsson_Xzibit | 1671 +- 58 | 186 | 103-83 | 58.7% |
| 26 | us:s5w | 1650 +- 91 | 72 | 45-27 | 57.2% |
| 27 | us:v0d | 1639 +- 128 | 37 | 27-10 | 56.5% |
| 28 | spsquared_TSPAARK_QUALS | 1624 +- 51 | 231 | 125-106 | 55.7% |
| 29 | us:s4m | 1591 +- 118 | 33 | 22-11 | 53.5% |
| 30 | us:s7f | 1591 +- 351 | 2 | 1-1 | 53.4% |
| 31 | us:s6y | 1590 +- 124 | 30 | 22-8 | 53.4% |
| 32 | us:s5j | 1580 +- 95 | 52 | 36-16 | 52.8% |
| 33 | us:s4j | 1578 +- 65 | 131 | 87-44 | 52.6% |
| 34 | us:s4g | 1571 +- 76 | 80 | 51-29 | 52.2% |
| 35 | us:s4i | 1560 +- 64 | 134 | 86-48 | 51.6% |
| 36 | us:s3y | 1554 +- 53 | 235 | 134-101 | 51.2% |
| 37 | us:s5s | 1553 +- 76 | 80 | 56-24 | 51.1% |
| 38 | us:s4h | 1548 +- 76 | 80 | 52-28 | 50.8% |
| 39 | us:s4n | 1548 +- 76 | 80 | 52-28 | 50.8% |
| 40 | us:s5y | 1540 +- 76 | 80 | 53-27 | 50.3% |
| 41 | us:s4d | 1537 +- 34 | 462 | 287-175 | 50.1% |
| 42 | us:s6j | 1537 +- 119 | 42 | 18-24 | 50.1% |
| 43 | us:s6p | 1534 +- 76 | 80 | 47-33 | 49.9% |
| 44 | us:s6v | 1533 +- 88 | 58 | 35-23 | 49.9% |
| 45 | us:s4f | 1529 +- 54 | 160 | 110-50 | 49.6% |
| 46 | us:s5g | 1528 +- 62 | 140 | 80-60 | 49.5% |
| 47 | us:s6g | 1525 +- 75 | 80 | 41-39 | 49.4% |
| 48 | us:s6m | 1519 +- 51 | 212 | 97-115 | 49.0% |
| 49 | us:s4e | 1516 +- 76 | 80 | 61-19 | 48.8% |
| 50 | us:s6e | 1515 +- 75 | 80 | 51-29 | 48.7% |
| 51 | us:s4b | 1513 +- 53 | 172 | 117-55 | 48.6% |
| 52 | us:s5c | 1512 +- 76 | 80 | 58-22 | 48.6% |
| 53 | us:s7d | 1510 +- 278 | 4 | 2-2 | 48.5% |
| 54 | us:abrk | 1509 +- 25 | 749 | 278-471 | 48.4% |
| 55 | us:s6f | 1508 +- 75 | 80 | 41-39 | 48.3% |
| 56 | us:s5o | 1505 +- 75 | 80 | 41-39 | 48.1% |
| 57 | us:s5r | 1502 +- 49 | 202 | 121-81 | 48.0% |
| 58 | us:s5t | 1501 +- 73 | 86 | 42-44 | 47.9% |
| 59 | us:araid | 1501 +- 19 | 1250 | 541-709 | 47.9% |
| 60 | us:s4q | 1498 +- 76 | 80 | 61-19 | 47.7% |
| 61 | us:s4r | 1498 +- 76 | 80 | 61-19 | 47.7% |
| 62 | us:s5x | 1494 +- 90 | 55 | 30-25 | 47.5% |
| 63 | us:s3u | 1493 +- 76 | 80 | 53-27 | 47.4% |
| 64 | us:s4c | 1493 +- 76 | 80 | 53-27 | 47.4% |
| 65 | us:s4t | 1489 +- 66 | 122 | 74-48 | 47.2% |
| 66 | us:s6s | 1484 +- 75 | 80 | 40-40 | 46.9% |
| 67 | us:s3f | 1484 +- 75 | 80 | 46-34 | 46.8% |
| 68 | us:s3o | 1482 +- 75 | 80 | 50-30 | 46.7% |
| 69 | us:s5e | 1481 +- 103 | 60 | 31-29 | 46.7% |
| 70 | us:s3t | 1479 +- 75 | 80 | 49-31 | 46.5% |
| 71 | us:s4a | 1476 +- 75 | 80 | 50-30 | 46.3% |
| 72 | us:s3z | 1476 +- 75 | 80 | 50-30 | 46.3% |
| 73 | us:s3v | 1474 +- 75 | 80 | 54-26 | 46.3% |
| 74 | us:s6o | 1474 +- 75 | 80 | 39-41 | 46.3% |
| 75 | us:s3w | 1468 +- 75 | 80 | 52-28 | 45.9% |
| 76 | us:s3r | 1468 +- 75 | 80 | 46-34 | 45.9% |
| 77 | us:s3s | 1468 +- 75 | 80 | 46-34 | 45.9% |
| 78 | us:s3p | 1463 +- 75 | 80 | 43-37 | 45.6% |
| 79 | us:s2o_id | 1462 +- 75 | 80 | 40-40 | 45.5% |
| 80 | us:s2o | 1461 +- 13 | 2868 | 1214-1654 | 45.4% |
| 81 | amcsz_fighterrats | 1461 +- 57 | 189 | 93-96 | 45.4% |
| 82 | CS50Crab_protoSaberV7 | 1459 +- 56 | 198 | 98-100 | 45.3% |
| 83 | us:s3h | 1456 +- 54 | 188 | 100-88 | 45.2% |
| 84 | CS50Crab_rapierV1 | 1454 +- 43 | 318 | 147-171 | 44.9% |
| 85 | us:s3x | 1451 +- 75 | 80 | 48-32 | 44.8% |
| 86 | outercloudstudio_v_1_18 | 1450 +- 47 | 267 | 108-159 | 44.7% |
| 87 | us:s3q | 1439 +- 75 | 80 | 35-45 | 44.1% |
| 88 | us:s3n | 1437 +- 75 | 80 | 29-51 | 44.0% |
| 89 | us:s3k | 1436 +- 75 | 80 | 29-51 | 43.9% |
| 90 | us:s3m | 1436 +- 75 | 80 | 30-50 | 43.9% |
| 91 | us:s3i | 1434 +- 75 | 80 | 27-53 | 43.8% |
| 92 | us:s3l | 1430 +- 76 | 80 | 27-53 | 43.6% |
| 93 | us:s3e | 1425 +- 76 | 80 | 29-51 | 43.3% |
| 94 | us:s3g | 1425 +- 76 | 80 | 30-50 | 43.3% |
| 95 | us:arush | 1418 +- 78 | 80 | 29-51 | 42.9% |
| 96 | bc26vibe_bot | 1403 +- 42 | 327 | 148-179 | 41.8% |
| 97 | PixelRifts_mushakraj_mk3 | 1402 +- 53 | 225 | 106-119 | 41.7% |
| 98 | us:ratbot | 1375 +- 69 | 154 | 107-47 | 40.2% |
| 99 | us:s0u | 1349 +- 126 | 37 | 13-24 | 38.7% |
| 100 | us:s0m | 1337 +- 69 | 148 | 98-50 | 37.9% |
| 101 | us:s6c | 1312 +- 393 | 2 | 0-2 | 36.4% |
| 102 | XSercia_genies | 1302 +- 45 | 342 | 113-229 | 35.5% |
| 103 | us:s0e | 1275 +- 144 | 33 | 15-18 | 34.2% |
| 104 | C0derTang_v6 | 1256 +- 48 | 276 | 94-182 | 32.7% |
| 105 | ask8000_bumchit67 | 1234 +- 46 | 309 | 113-196 | 31.3% |
| 106 | Naton-Cai_ratplayer | 1230 +- 57 | 183 | 85-98 | 31.0% |
| 107 | justinottesen_alfredo | 1198 +- 52 | 222 | 102-120 | 29.1% |
| 108 | bc26-example | 1193 +- 35 | 562 | 216-346 | 28.8% |
| 109 | Camleaf_mk1 | 1161 +- 56 | 195 | 87-108 | 26.9% |
| 110 | vincentwang60_javabot | 1142 +- 55 | 195 | 82-113 | 25.7% |
| 111 | bc26-example-copy | 1138 +- 51 | 207 | 93-114 | 25.5% |
| 112 | Gavdingo_v1 | 1090 +- 50 | 228 | 99-129 | 22.7% |
| 113 | justinottesen_carbonara | 1075 +- 58 | 174 | 76-98 | 21.9% |
| 114 | battlecode-archive_result_408 | 1050 +- 55 | 192 | 82-110 | 20.5% |
| 115 | GuzziFlipFlops_Hermesv8 | 1030 +- 51 | 216 | 92-124 | 19.4% |
| 116 | j2399_weighted_micro_v9 | 1013 +- 61 | 174 | 72-102 | 18.5% |
| 117 | seru9_TOTAL_AGGRO | 952 +- 58 | 198 | 84-114 | 15.5% |
| 118 | quanticsoul4772_ratbot | 936 +- 56 | 207 | 90-117 | 14.8% |
| 119 | Victor-Ramirez-Garcia_battleca | 893 +- 61 | 201 | 78-123 | 12.9% |
| 120 | anicolao_myplayer | 667 +- 71 | 180 | 43-137 | 5.6% |
| 121 | j2399_weighted_path | 630 +- 76 | 132 | 38-94 | 4.7% |
| 122 | Krak9n_robot | 533 +- 83 | 138 | 23-115 | 2.9% |

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
| cheeseguardians | +57 |
| FreeTheCheese | +57 |
| houseofcards | +38 |
| replica | +37 |
| sunandmoon | +17 |
| tornadoes | -23 |
| canyoudig | -28 |
| TheHeist | -54 |
| groveisbeingdesecrated | -54 |
| rose | -54 |
| where_are_you | -110 |
| 5t4rv4t10n_1337 | -146 |
| diamond | -152 |
| arrows | -192 |
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
