# Ladder

6705 rows of progress/games.csv (4739 local games, 1966 replica matches), rated by a batch Bradley-Terry fit on the Elo scale (`tools/elolib.py`). A local game counts its cell score (`tools/score.py`: a win or loss when kings decide the game, the points share when points do), a replica match its games won. A game between one of our builds and a field bot also fits how hard its map is for us (table at the end), so builds gauntleted on different maps compare on an average map. Each pair of players counts at most 200 games, every player carries one virtual win and loss against a fixed anchor, and ratings are relative to a mean of 1500 over all 116 players. Each of our builds is its own player. Regenerate with `tools/elo.py`; do not edit.

Games against field bots are replica scrimmages only; local games are our builds against each other and the stock players. Record = games won and lost.

![ratings of our builds](elo.png)

Our builds, in attempt order (rating +- 95%; field score = expected score against each of the 51 field players, one game each):

| build | rating | rank | games | record | field score |
|---|---|---|---|---|---|
| s0e | 1280 +- 144 | 97 of 116 | 33 | 15-18 | 34.3% |
| s0m | 1338 +- 69 | 94 of 116 | 148 | 98-50 | 37.8% |
| s0u | 1350 +- 126 | 93 of 116 | 37 | 13-24 | 38.6% |
| v0d | 1640 +- 128 | 26 of 116 | 37 | 27-10 | 56.7% |
| s2o | 1467 +- 13 | 75 of 116 | 2868 | 1214-1654 | 45.7% |
| s3e | 1432 +- 76 | 87 of 116 | 80 | 29-51 | 43.5% |
| s3f | 1490 +- 75 | 61 of 116 | 80 | 46-34 | 47.1% |
| s3g | 1432 +- 76 | 88 of 116 | 80 | 30-50 | 43.5% |
| s2o_id | 1468 +- 75 | 73 of 116 | 80 | 40-40 | 45.8% |
| s3h | 1464 +- 54 | 77 of 116 | 188 | 100-88 | 45.5% |
| s3i | 1441 +- 75 | 85 of 116 | 80 | 27-53 | 44.1% |
| s3k | 1442 +- 75 | 83 of 116 | 80 | 29-51 | 44.2% |
| s3l | 1437 +- 76 | 86 of 116 | 80 | 27-53 | 43.9% |
| s3m | 1442 +- 75 | 84 of 116 | 80 | 30-50 | 44.2% |
| s3n | 1443 +- 75 | 82 of 116 | 80 | 29-51 | 44.2% |
| s3o | 1488 +- 75 | 63 of 116 | 80 | 50-30 | 47.0% |
| s3p | 1469 +- 75 | 72 of 116 | 80 | 43-37 | 45.9% |
| s3q | 1445 +- 75 | 81 of 116 | 80 | 35-45 | 44.4% |
| s3r | 1474 +- 75 | 69 of 116 | 80 | 46-34 | 46.2% |
| s3s | 1474 +- 75 | 70 of 116 | 80 | 46-34 | 46.2% |
| s3t | 1485 +- 75 | 64 of 116 | 80 | 49-31 | 46.8% |
| s3u | 1499 +- 76 | 58 of 116 | 80 | 53-27 | 47.7% |
| s3v | 1481 +- 75 | 67 of 116 | 80 | 54-26 | 46.6% |
| s3w | 1474 +- 75 | 68 of 116 | 80 | 52-28 | 46.2% |
| s3x | 1457 +- 75 | 80 of 116 | 80 | 48-32 | 45.1% |
| s3y | 1560 +- 52 | 34 of 116 | 235 | 134-101 | 51.6% |
| s3z | 1482 +- 75 | 66 of 116 | 80 | 50-30 | 46.6% |
| s4a | 1482 +- 75 | 65 of 116 | 80 | 50-30 | 46.7% |
| s4b | 1519 +- 53 | 47 of 116 | 172 | 117-55 | 49.0% |
| s4c | 1499 +- 76 | 59 of 116 | 80 | 53-27 | 47.7% |
| s4d | 1546 +- 34 | 38 of 116 | 462 | 287-175 | 50.6% |
| arush | 1425 +- 78 | 89 of 116 | 80 | 29-51 | 43.1% |
| s4e | 1522 +- 76 | 46 of 116 | 80 | 61-19 | 49.1% |
| s4f | 1536 +- 54 | 42 of 116 | 160 | 110-50 | 50.0% |
| abrk | 1517 +- 25 | 50 of 116 | 749 | 278-471 | 48.8% |
| s4g | 1578 +- 76 | 31 of 116 | 80 | 51-29 | 52.7% |
| s4h | 1555 +- 76 | 35 of 116 | 80 | 52-28 | 51.2% |
| s4i | 1568 +- 64 | 32 of 116 | 134 | 86-48 | 52.0% |
| s4j | 1585 +- 65 | 30 of 116 | 131 | 87-44 | 53.1% |
| s4m | 1598 +- 118 | 28 of 116 | 33 | 22-11 | 53.9% |
| s4n | 1555 +- 76 | 36 of 116 | 80 | 52-28 | 51.2% |
| s4q | 1505 +- 76 | 56 of 116 | 80 | 61-19 | 48.1% |
| s4r | 1505 +- 76 | 57 of 116 | 80 | 61-19 | 48.1% |
| s4t | 1496 +- 66 | 60 of 116 | 122 | 74-48 | 47.5% |
| s5c | 1518 +- 76 | 49 of 116 | 80 | 58-22 | 48.9% |
| s5e | 1488 +- 102 | 62 of 116 | 60 | 31-29 | 47.0% |
| s5g | 1535 +- 62 | 43 of 116 | 140 | 80-60 | 50.0% |
| s5j | 1588 +- 95 | 29 of 116 | 52 | 36-16 | 53.3% |
| araid | 1512 +- 22 | 53 of 116 | 970 | 421-549 | 48.5% |
| s5o | 1516 +- 75 | 51 of 116 | 80 | 41-39 | 48.8% |
| s5r | 1511 +- 49 | 54 of 116 | 202 | 121-81 | 48.4% |
| s5s | 1565 +- 76 | 33 of 116 | 80 | 56-24 | 51.8% |
| s5t | 1513 +- 73 | 52 of 116 | 86 | 42-44 | 48.6% |
| s5w | 1657 +- 91 | 25 of 116 | 72 | 45-27 | 57.8% |
| s5x | 1505 +- 90 | 55 of 116 | 55 | 30-25 | 48.1% |
| s5y | 1551 +- 76 | 37 of 116 | 80 | 53-27 | 51.0% |
| s6c | 1318 +- 393 | 95 of 116 | 2 | 0-2 | 36.6% |
| s6e | 1526 +- 75 | 44 of 116 | 80 | 51-29 | 49.4% |
| s6f | 1519 +- 75 | 48 of 116 | 80 | 41-39 | 49.0% |
| s6g | 1537 +- 75 | 41 of 116 | 80 | 41-39 | 50.1% |
| s6j | 1542 +- 118 | 39 of 116 | 42 | 18-24 | 50.4% |
| s6m | 1539 +- 67 | 40 of 116 | 113 | 56-57 | 50.2% |
| s6o | 1472 +- 99 | 71 of 116 | 46 | 22-24 | 46.1% |
| s6p | 1526 +- 134 | 45 of 116 | 24 | 15-9 | 49.4% |
| ratbot | 1375 +- 69 | 92 of 116 | 154 | 107-47 | 40.1% |

All players:

| # | player | rating | games | record | field score |
|---:|---|---|---|---|---|
| 1 | IvanGeffner_basic51 | 2141 +- 58 | 177 | 125-52 | 89.7% |
| 2 | uravt_Version41 | 2098 +- 59 | 168 | 112-56 | 87.5% |
| 3 | uravt_Version35 | 2045 +- 54 | 177 | 106-71 | 84.5% |
| 4 | awu7_awubot_us | 2020 +- 58 | 168 | 102-66 | 83.0% |
| 5 | abcdezyxwv_bestsofar | 2020 +- 54 | 288 | 215-73 | 83.0% |
| 6 | nzjt_TheComplexMerlin | 1984 +- 63 | 153 | 97-56 | 80.7% |
| 7 | thewaxmango_PhD | 1978 +- 59 | 174 | 105-69 | 80.3% |
| 8 | quesswho_bot16 | 1973 +- 52 | 198 | 110-88 | 80.0% |
| 9 | quesswho_quals | 1970 +- 52 | 195 | 112-83 | 79.8% |
| 10 | awu7_awubot_quals | 1956 +- 57 | 174 | 105-69 | 78.9% |
| 11 | daannte_v52_ThisGuy | 1949 +- 52 | 192 | 103-89 | 78.4% |
| 12 | nubbbbbb_testplayer | 1943 +- 59 | 156 | 90-66 | 78.0% |
| 13 | battlecode-archive_testplayer | 1942 +- 49 | 225 | 127-98 | 77.9% |
| 14 | TeamSPAARK_SPAARK | 1918 +- 53 | 198 | 113-85 | 76.3% |
| 15 | daannte_v47_Skadoosh | 1916 +- 52 | 213 | 118-95 | 76.1% |
| 16 | AlexT101_finalsbot | 1824 +- 52 | 216 | 115-101 | 69.6% |
| 17 | AlexT101_usqualsbot | 1824 +- 45 | 315 | 203-112 | 69.6% |
| 18 | bman12345678_goonv23 | 1794 +- 61 | 177 | 101-76 | 67.5% |
| 19 | AdamTan12_Icarus | 1777 +- 48 | 246 | 133-113 | 66.2% |
| 20 | thewaxmango_collegejunior | 1773 +- 58 | 174 | 96-78 | 66.0% |
| 21 | AdamTan12_Finals | 1770 +- 50 | 240 | 137-103 | 65.8% |
| 22 | HugoIngelsson_YokoOno | 1741 +- 57 | 186 | 99-87 | 63.7% |
| 23 | VarunVejalla_final4_run_away10 | 1710 +- 63 | 159 | 90-69 | 61.5% |
| 24 | HugoIngelsson_Xzibit | 1663 +- 59 | 180 | 98-82 | 58.3% |
| 25 | us:s5w | 1657 +- 91 | 72 | 45-27 | 57.8% |
| 26 | us:v0d | 1640 +- 128 | 37 | 27-10 | 56.7% |
| 27 | spsquared_TSPAARK_QUALS | 1629 +- 54 | 207 | 112-95 | 56.1% |
| 28 | us:s4m | 1598 +- 118 | 33 | 22-11 | 53.9% |
| 29 | us:s5j | 1588 +- 95 | 52 | 36-16 | 53.3% |
| 30 | us:s4j | 1585 +- 65 | 131 | 87-44 | 53.1% |
| 31 | us:s4g | 1578 +- 76 | 80 | 51-29 | 52.7% |
| 32 | us:s4i | 1568 +- 64 | 134 | 86-48 | 52.0% |
| 33 | us:s5s | 1565 +- 76 | 80 | 56-24 | 51.8% |
| 34 | us:s3y | 1560 +- 52 | 235 | 134-101 | 51.6% |
| 35 | us:s4h | 1555 +- 76 | 80 | 52-28 | 51.2% |
| 36 | us:s4n | 1555 +- 76 | 80 | 52-28 | 51.2% |
| 37 | us:s5y | 1551 +- 76 | 80 | 53-27 | 51.0% |
| 38 | us:s4d | 1546 +- 34 | 462 | 287-175 | 50.6% |
| 39 | us:s6j | 1542 +- 118 | 42 | 18-24 | 50.4% |
| 40 | us:s6m | 1539 +- 67 | 113 | 56-57 | 50.2% |
| 41 | us:s6g | 1537 +- 75 | 80 | 41-39 | 50.1% |
| 42 | us:s4f | 1536 +- 54 | 160 | 110-50 | 50.0% |
| 43 | us:s5g | 1535 +- 62 | 140 | 80-60 | 50.0% |
| 44 | us:s6e | 1526 +- 75 | 80 | 51-29 | 49.4% |
| 45 | us:s6p | 1526 +- 134 | 24 | 15-9 | 49.4% |
| 46 | us:s4e | 1522 +- 76 | 80 | 61-19 | 49.1% |
| 47 | us:s4b | 1519 +- 53 | 172 | 117-55 | 49.0% |
| 48 | us:s6f | 1519 +- 75 | 80 | 41-39 | 49.0% |
| 49 | us:s5c | 1518 +- 76 | 80 | 58-22 | 48.9% |
| 50 | us:abrk | 1517 +- 25 | 749 | 278-471 | 48.8% |
| 51 | us:s5o | 1516 +- 75 | 80 | 41-39 | 48.8% |
| 52 | us:s5t | 1513 +- 73 | 86 | 42-44 | 48.6% |
| 53 | us:araid | 1512 +- 22 | 970 | 421-549 | 48.5% |
| 54 | us:s5r | 1511 +- 49 | 202 | 121-81 | 48.4% |
| 55 | us:s5x | 1505 +- 90 | 55 | 30-25 | 48.1% |
| 56 | us:s4q | 1505 +- 76 | 80 | 61-19 | 48.1% |
| 57 | us:s4r | 1505 +- 76 | 80 | 61-19 | 48.1% |
| 58 | us:s3u | 1499 +- 76 | 80 | 53-27 | 47.7% |
| 59 | us:s4c | 1499 +- 76 | 80 | 53-27 | 47.7% |
| 60 | us:s4t | 1496 +- 66 | 122 | 74-48 | 47.5% |
| 61 | us:s3f | 1490 +- 75 | 80 | 46-34 | 47.1% |
| 62 | us:s5e | 1488 +- 102 | 60 | 31-29 | 47.0% |
| 63 | us:s3o | 1488 +- 75 | 80 | 50-30 | 47.0% |
| 64 | us:s3t | 1485 +- 75 | 80 | 49-31 | 46.8% |
| 65 | us:s4a | 1482 +- 75 | 80 | 50-30 | 46.7% |
| 66 | us:s3z | 1482 +- 75 | 80 | 50-30 | 46.6% |
| 67 | us:s3v | 1481 +- 75 | 80 | 54-26 | 46.6% |
| 68 | us:s3w | 1474 +- 75 | 80 | 52-28 | 46.2% |
| 69 | us:s3r | 1474 +- 75 | 80 | 46-34 | 46.2% |
| 70 | us:s3s | 1474 +- 75 | 80 | 46-34 | 46.2% |
| 71 | us:s6o | 1472 +- 99 | 46 | 22-24 | 46.1% |
| 72 | us:s3p | 1469 +- 75 | 80 | 43-37 | 45.9% |
| 73 | us:s2o_id | 1468 +- 75 | 80 | 40-40 | 45.8% |
| 74 | amcsz_fighterrats | 1468 +- 57 | 183 | 92-91 | 45.7% |
| 75 | us:s2o | 1467 +- 13 | 2868 | 1214-1654 | 45.7% |
| 76 | CS50Crab_protoSaberV7 | 1467 +- 56 | 192 | 96-96 | 45.7% |
| 77 | us:s3h | 1464 +- 54 | 188 | 100-88 | 45.5% |
| 78 | CS50Crab_rapierV1 | 1460 +- 44 | 303 | 142-161 | 45.2% |
| 79 | outercloudstudio_v_1_18 | 1459 +- 49 | 252 | 103-149 | 45.1% |
| 80 | us:s3x | 1457 +- 75 | 80 | 48-32 | 45.1% |
| 81 | us:s3q | 1445 +- 75 | 80 | 35-45 | 44.4% |
| 82 | us:s3n | 1443 +- 75 | 80 | 29-51 | 44.2% |
| 83 | us:s3k | 1442 +- 75 | 80 | 29-51 | 44.2% |
| 84 | us:s3m | 1442 +- 75 | 80 | 30-50 | 44.2% |
| 85 | us:s3i | 1441 +- 75 | 80 | 27-53 | 44.1% |
| 86 | us:s3l | 1437 +- 76 | 80 | 27-53 | 43.9% |
| 87 | us:s3e | 1432 +- 76 | 80 | 29-51 | 43.5% |
| 88 | us:s3g | 1432 +- 76 | 80 | 30-50 | 43.5% |
| 89 | us:arush | 1425 +- 78 | 80 | 29-51 | 43.1% |
| 90 | bc26vibe_bot | 1414 +- 43 | 312 | 145-167 | 42.3% |
| 91 | PixelRifts_mushakraj_mk3 | 1406 +- 54 | 219 | 102-117 | 41.8% |
| 92 | us:ratbot | 1375 +- 69 | 154 | 107-47 | 40.1% |
| 93 | us:s0u | 1350 +- 126 | 37 | 13-24 | 38.6% |
| 94 | us:s0m | 1338 +- 69 | 148 | 98-50 | 37.8% |
| 95 | us:s6c | 1318 +- 393 | 2 | 0-2 | 36.6% |
| 96 | XSercia_genies | 1307 +- 45 | 342 | 113-229 | 35.6% |
| 97 | us:s0e | 1280 +- 144 | 33 | 15-18 | 34.3% |
| 98 | C0derTang_v6 | 1273 +- 49 | 267 | 92-175 | 33.5% |
| 99 | ask8000_bumchit67 | 1238 +- 47 | 306 | 111-195 | 31.4% |
| 100 | Naton-Cai_ratplayer | 1234 +- 57 | 183 | 85-98 | 31.1% |
| 101 | justinottesen_alfredo | 1202 +- 53 | 219 | 101-118 | 29.2% |
| 102 | bc26-example | 1194 +- 35 | 562 | 216-346 | 28.6% |
| 103 | Camleaf_mk1 | 1163 +- 56 | 195 | 87-108 | 26.8% |
| 104 | vincentwang60_javabot | 1158 +- 57 | 186 | 80-106 | 26.5% |
| 105 | bc26-example-copy | 1131 +- 52 | 201 | 88-113 | 24.9% |
| 106 | Gavdingo_v1 | 1080 +- 52 | 219 | 92-127 | 22.0% |
| 107 | justinottesen_carbonara | 1077 +- 59 | 171 | 75-96 | 21.8% |
| 108 | battlecode-archive_result_408 | 1047 +- 57 | 186 | 78-108 | 20.2% |
| 109 | GuzziFlipFlops_Hermesv8 | 1029 +- 51 | 213 | 90-123 | 19.2% |
| 110 | j2399_weighted_micro_v9 | 1015 +- 61 | 171 | 71-100 | 18.5% |
| 111 | seru9_TOTAL_AGGRO | 954 +- 58 | 198 | 84-114 | 15.4% |
| 112 | quanticsoul4772_ratbot | 937 +- 57 | 195 | 81-114 | 14.6% |
| 113 | Victor-Ramirez-Garcia_battleca | 894 +- 61 | 198 | 75-123 | 12.8% |
| 114 | anicolao_myplayer | 688 +- 72 | 171 | 43-128 | 5.9% |
| 115 | j2399_weighted_path | 647 +- 76 | 129 | 38-91 | 4.9% |
| 116 | Krak9n_robot | 548 +- 83 | 138 | 23-115 | 3.0% |

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
| FreeTheCheese | +57 |
| cheeseguardians | +57 |
| houseofcards | +38 |
| replica | +35 |
| sunandmoon | +17 |
| tornadoes | -23 |
| canyoudig | -28 |
| rose | -54 |
| TheHeist | -54 |
| groveisbeingdesecrated | -54 |
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
| clockwisdom | -618 |
| fullofsecrets | -618 |
