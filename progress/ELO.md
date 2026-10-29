# Ladder

5294 rows of progress/games.csv (3625 local games, 1669 replica matches), rated by a batch Bradley-Terry fit on the Elo scale (`tools/elolib.py`). A local game counts its cell score (`tools/score.py`: a win or loss when kings decide the game, the points share when points do), a replica match its games won. A game between one of our builds and a field bot also fits how hard its map is for us (table at the end), so builds gauntleted on different maps compare on an average map. Each pair of players counts at most 200 games, every player carries one virtual win and loss against a fixed anchor, and ratings are relative to a mean of 1500 over all 101 players. Each of our builds is its own player. Regenerate with `tools/elo.py`; do not edit.

Games against field bots are replica scrimmages only; local games are our builds against each other and the stock players. Record = games won and lost.

![ratings of our builds](elo.png)

Our builds, in attempt order (rating +- 95%; field score = expected score against each of the 51 field players, one game each):

| build | rating | rank | games | record | field score |
|---|---|---|---|---|---|
| s0e | 1278 +- 144 | 83 of 101 | 33 | 15-18 | 33.7% |
| s0m | 1331 +- 69 | 80 of 101 | 148 | 98-50 | 37.0% |
| s0u | 1344 +- 126 | 79 of 101 | 37 | 13-24 | 37.8% |
| v0d | 1633 +- 128 | 27 of 101 | 37 | 27-10 | 56.4% |
| s2o | 1466 +- 13 | 64 of 101 | 2780 | 1190-1590 | 45.5% |
| s3e | 1430 +- 76 | 73 of 101 | 80 | 29-51 | 43.2% |
| s3f | 1489 +- 75 | 49 of 101 | 80 | 46-34 | 46.9% |
| s3g | 1430 +- 76 | 74 of 101 | 80 | 30-50 | 43.2% |
| s2o_id | 1467 +- 75 | 63 of 101 | 80 | 40-40 | 45.6% |
| s3h | 1463 +- 54 | 65 of 101 | 188 | 100-88 | 45.3% |
| s3i | 1440 +- 75 | 71 of 101 | 80 | 27-53 | 43.8% |
| s3k | 1441 +- 75 | 69 of 101 | 80 | 29-51 | 43.9% |
| s3l | 1435 +- 76 | 72 of 101 | 80 | 27-53 | 43.6% |
| s3m | 1441 +- 75 | 70 of 101 | 80 | 30-50 | 43.9% |
| s3n | 1442 +- 75 | 68 of 101 | 80 | 29-51 | 44.0% |
| s3o | 1487 +- 75 | 50 of 101 | 80 | 50-30 | 46.8% |
| s3p | 1468 +- 75 | 62 of 101 | 80 | 43-37 | 45.6% |
| s3q | 1444 +- 75 | 67 of 101 | 80 | 35-45 | 44.1% |
| s3r | 1473 +- 75 | 58 of 101 | 80 | 46-34 | 45.9% |
| s3s | 1473 +- 75 | 59 of 101 | 80 | 46-34 | 45.9% |
| s3t | 1484 +- 75 | 51 of 101 | 80 | 49-31 | 46.6% |
| s3u | 1498 +- 76 | 46 of 101 | 80 | 53-27 | 47.5% |
| s3v | 1479 +- 75 | 54 of 101 | 80 | 54-26 | 46.3% |
| s3w | 1473 +- 75 | 57 of 101 | 80 | 52-28 | 45.9% |
| s3x | 1456 +- 75 | 66 of 101 | 80 | 48-32 | 44.9% |
| s3y | 1562 +- 62 | 33 of 101 | 180 | 110-70 | 51.6% |
| s3z | 1481 +- 75 | 53 of 101 | 80 | 50-30 | 46.4% |
| s4a | 1481 +- 75 | 52 of 101 | 80 | 50-30 | 46.4% |
| s4b | 1519 +- 53 | 41 of 101 | 172 | 117-55 | 48.8% |
| s4c | 1498 +- 76 | 47 of 101 | 80 | 53-27 | 47.5% |
| s4d | 1542 +- 40 | 37 of 101 | 340 | 218-122 | 50.4% |
| arush | 1424 +- 78 | 75 of 101 | 80 | 29-51 | 42.8% |
| s4e | 1521 +- 76 | 39 of 101 | 80 | 61-19 | 49.0% |
| s4f | 1537 +- 54 | 38 of 101 | 160 | 110-50 | 50.0% |
| abrk | 1520 +- 26 | 40 of 101 | 704 | 264-440 | 48.9% |
| s4g | 1581 +- 76 | 31 of 101 | 80 | 51-29 | 52.9% |
| s4h | 1558 +- 76 | 34 of 101 | 80 | 52-28 | 51.4% |
| s4i | 1569 +- 63 | 32 of 101 | 134 | 86-48 | 52.1% |
| s4j | 1586 +- 65 | 30 of 101 | 131 | 87-44 | 53.3% |
| s4m | 1593 +- 186 | 29 of 101 | 12 | 7-5 | 53.7% |
| s4n | 1558 +- 76 | 35 of 101 | 80 | 52-28 | 51.4% |
| s4q | 1503 +- 76 | 44 of 101 | 80 | 61-19 | 47.9% |
| s4r | 1503 +- 76 | 45 of 101 | 80 | 61-19 | 47.9% |
| s4t | 1495 +- 66 | 48 of 101 | 122 | 74-48 | 47.3% |
| s5c | 1517 +- 76 | 42 of 101 | 80 | 58-22 | 48.8% |
| s5e | 1514 +- 117 | 43 of 101 | 48 | 28-20 | 48.6% |
| s5g | 1553 +- 71 | 36 of 101 | 92 | 53-39 | 51.0% |
| s5j | 1618 +- 129 | 28 of 101 | 28 | 20-8 | 55.4% |
| araid | 1782 +- 381 | 19 of 101 | 3 | 3-0 | 66.7% |
| ratbot | 1368 +- 69 | 78 of 101 | 154 | 107-47 | 39.3% |

All players:

| # | player | rating | games | record | field score |
|---:|---|---|---|---|---|
| 1 | IvanGeffner_basic51 | 2111 +- 62 | 153 | 107-46 | 88.7% |
| 2 | uravt_Version41 | 2094 +- 67 | 138 | 97-41 | 87.8% |
| 3 | uravt_Version35 | 2023 +- 59 | 150 | 89-61 | 83.8% |
| 4 | awu7_awubot_us | 1999 +- 63 | 144 | 87-57 | 82.3% |
| 5 | abcdezyxwv_bestsofar | 1996 +- 57 | 240 | 176-64 | 82.1% |
| 6 | nzjt_TheComplexMerlin | 1975 +- 68 | 135 | 88-47 | 80.8% |
| 7 | thewaxmango_PhD | 1971 +- 65 | 147 | 93-54 | 80.5% |
| 8 | quesswho_quals | 1962 +- 55 | 174 | 103-71 | 79.9% |
| 9 | quesswho_bot16 | 1961 +- 55 | 174 | 96-78 | 79.8% |
| 10 | battlecode-archive_testplayer | 1943 +- 54 | 192 | 115-77 | 78.6% |
| 11 | nubbbbbb_testplayer | 1940 +- 63 | 138 | 81-57 | 78.4% |
| 12 | daannte_v52_ThisGuy | 1939 +- 56 | 168 | 92-76 | 78.3% |
| 13 | awu7_awubot_quals | 1934 +- 61 | 153 | 94-59 | 77.9% |
| 14 | daannte_v47_Skadoosh | 1904 +- 55 | 186 | 102-84 | 75.9% |
| 15 | TeamSPAARK_SPAARK | 1894 +- 56 | 177 | 100-77 | 75.2% |
| 16 | AlexT101_usqualsbot | 1816 +- 49 | 258 | 164-94 | 69.5% |
| 17 | AlexT101_finalsbot | 1814 +- 55 | 195 | 104-91 | 69.3% |
| 18 | bman12345678_goonv23 | 1795 +- 68 | 147 | 90-57 | 67.9% |
| 19 | us:araid | 1782 +- 381 | 3 | 3-0 | 66.7% |
| 20 | thewaxmango_collegejunior | 1774 +- 62 | 153 | 89-64 | 66.4% |
| 21 | AdamTan12_Finals | 1769 +- 62 | 162 | 89-73 | 66.1% |
| 22 | AdamTan12_Icarus | 1760 +- 51 | 219 | 117-102 | 65.4% |
| 23 | HugoIngelsson_YokoOno | 1744 +- 59 | 171 | 93-78 | 64.3% |
| 24 | VarunVejalla_final4_run_away10 | 1687 +- 70 | 135 | 74-61 | 60.2% |
| 25 | HugoIngelsson_Xzibit | 1666 +- 62 | 162 | 89-73 | 58.8% |
| 26 | spsquared_TSPAARK_QUALS | 1639 +- 66 | 150 | 82-68 | 56.9% |
| 27 | us:v0d | 1633 +- 128 | 37 | 27-10 | 56.4% |
| 28 | us:s5j | 1618 +- 129 | 28 | 20-8 | 55.4% |
| 29 | us:s4m | 1593 +- 186 | 12 | 7-5 | 53.7% |
| 30 | us:s4j | 1586 +- 65 | 131 | 87-44 | 53.3% |
| 31 | us:s4g | 1581 +- 76 | 80 | 51-29 | 52.9% |
| 32 | us:s4i | 1569 +- 63 | 134 | 86-48 | 52.1% |
| 33 | us:s3y | 1562 +- 62 | 180 | 110-70 | 51.6% |
| 34 | us:s4h | 1558 +- 76 | 80 | 52-28 | 51.4% |
| 35 | us:s4n | 1558 +- 76 | 80 | 52-28 | 51.4% |
| 36 | us:s5g | 1553 +- 71 | 92 | 53-39 | 51.0% |
| 37 | us:s4d | 1542 +- 40 | 340 | 218-122 | 50.4% |
| 38 | us:s4f | 1537 +- 54 | 160 | 110-50 | 50.0% |
| 39 | us:s4e | 1521 +- 76 | 80 | 61-19 | 49.0% |
| 40 | us:abrk | 1520 +- 26 | 704 | 264-440 | 48.9% |
| 41 | us:s4b | 1519 +- 53 | 172 | 117-55 | 48.8% |
| 42 | us:s5c | 1517 +- 76 | 80 | 58-22 | 48.8% |
| 43 | us:s5e | 1514 +- 117 | 48 | 28-20 | 48.6% |
| 44 | us:s4q | 1503 +- 76 | 80 | 61-19 | 47.9% |
| 45 | us:s4r | 1503 +- 76 | 80 | 61-19 | 47.9% |
| 46 | us:s3u | 1498 +- 76 | 80 | 53-27 | 47.5% |
| 47 | us:s4c | 1498 +- 76 | 80 | 53-27 | 47.5% |
| 48 | us:s4t | 1495 +- 66 | 122 | 74-48 | 47.3% |
| 49 | us:s3f | 1489 +- 75 | 80 | 46-34 | 46.9% |
| 50 | us:s3o | 1487 +- 75 | 80 | 50-30 | 46.8% |
| 51 | us:s3t | 1484 +- 75 | 80 | 49-31 | 46.6% |
| 52 | us:s4a | 1481 +- 75 | 80 | 50-30 | 46.4% |
| 53 | us:s3z | 1481 +- 75 | 80 | 50-30 | 46.4% |
| 54 | us:s3v | 1479 +- 75 | 80 | 54-26 | 46.3% |
| 55 | CS50Crab_rapierV1 | 1476 +- 48 | 252 | 122-130 | 46.0% |
| 56 | CS50Crab_protoSaberV7 | 1473 +- 60 | 171 | 87-84 | 45.9% |
| 57 | us:s3w | 1473 +- 75 | 80 | 52-28 | 45.9% |
| 58 | us:s3r | 1473 +- 75 | 80 | 46-34 | 45.9% |
| 59 | us:s3s | 1473 +- 75 | 80 | 46-34 | 45.9% |
| 60 | amcsz_fighterrats | 1472 +- 62 | 159 | 79-80 | 45.8% |
| 61 | outercloudstudio_v_1_18 | 1469 +- 55 | 201 | 86-115 | 45.6% |
| 62 | us:s3p | 1468 +- 75 | 80 | 43-37 | 45.6% |
| 63 | us:s2o_id | 1467 +- 75 | 80 | 40-40 | 45.6% |
| 64 | us:s2o | 1466 +- 13 | 2780 | 1190-1590 | 45.5% |
| 65 | us:s3h | 1463 +- 54 | 188 | 100-88 | 45.3% |
| 66 | us:s3x | 1456 +- 75 | 80 | 48-32 | 44.9% |
| 67 | us:s3q | 1444 +- 75 | 80 | 35-45 | 44.1% |
| 68 | us:s3n | 1442 +- 75 | 80 | 29-51 | 44.0% |
| 69 | us:s3k | 1441 +- 75 | 80 | 29-51 | 43.9% |
| 70 | us:s3m | 1441 +- 75 | 80 | 30-50 | 43.9% |
| 71 | us:s3i | 1440 +- 75 | 80 | 27-53 | 43.8% |
| 72 | us:s3l | 1435 +- 76 | 80 | 27-53 | 43.6% |
| 73 | us:s3e | 1430 +- 76 | 80 | 29-51 | 43.2% |
| 74 | us:s3g | 1430 +- 76 | 80 | 30-50 | 43.2% |
| 75 | us:arush | 1424 +- 78 | 80 | 29-51 | 42.8% |
| 76 | bc26vibe_bot | 1420 +- 48 | 252 | 119-133 | 42.4% |
| 77 | PixelRifts_mushakraj_mk3 | 1403 +- 60 | 183 | 84-99 | 41.3% |
| 78 | us:ratbot | 1368 +- 69 | 154 | 107-47 | 39.3% |
| 79 | us:s0u | 1344 +- 126 | 37 | 13-24 | 37.8% |
| 80 | us:s0m | 1331 +- 69 | 148 | 98-50 | 37.0% |
| 81 | XSercia_genies | 1315 +- 47 | 318 | 107-211 | 35.7% |
| 82 | C0derTang_v6 | 1279 +- 52 | 240 | 81-159 | 33.4% |
| 83 | us:s0e | 1278 +- 144 | 33 | 15-18 | 33.7% |
| 84 | ask8000_bumchit67 | 1247 +- 50 | 270 | 95-175 | 31.4% |
| 85 | Naton-Cai_ratplayer | 1240 +- 67 | 138 | 62-76 | 30.9% |
| 86 | justinottesen_alfredo | 1203 +- 57 | 192 | 83-109 | 28.6% |
| 87 | bc26-example | 1186 +- 36 | 544 | 203-341 | 27.6% |
| 88 | Camleaf_mk1 | 1181 +- 63 | 156 | 68-88 | 27.3% |
| 89 | vincentwang60_javabot | 1169 +- 63 | 159 | 68-91 | 26.5% |
| 90 | bc26-example-copy | 1130 +- 57 | 171 | 71-100 | 24.2% |
| 91 | justinottesen_carbonara | 1098 +- 64 | 150 | 67-83 | 22.4% |
| 92 | Gavdingo_v1 | 1094 +- 54 | 198 | 81-117 | 22.1% |
| 93 | battlecode-archive_result_408 | 1067 +- 62 | 156 | 63-93 | 20.6% |
| 94 | GuzziFlipFlops_Hermesv8 | 1058 +- 54 | 192 | 81-111 | 20.1% |
| 95 | j2399_weighted_micro_v9 | 1048 +- 65 | 153 | 64-89 | 19.6% |
| 96 | quanticsoul4772_ratbot | 983 +- 61 | 171 | 74-97 | 16.2% |
| 97 | seru9_TOTAL_AGGRO | 950 +- 65 | 168 | 64-104 | 14.7% |
| 98 | Victor-Ramirez-Garcia_battleca | 929 +- 65 | 177 | 65-112 | 13.7% |
| 99 | anicolao_myplayer | 701 +- 79 | 156 | 37-119 | 6.0% |
| 100 | j2399_weighted_path | 641 +- 91 | 99 | 28-71 | 4.6% |
| 101 | Krak9n_robot | 512 +- 102 | 114 | 14-100 | 2.4% |

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
| cheeseguardians | +56 |
| FreeTheCheese | +56 |
| replica | +42 |
| houseofcards | +38 |
| sunandmoon | +17 |
| tornadoes | -23 |
| canyoudig | -29 |
| TheHeist | -55 |
| groveisbeingdesecrated | -55 |
| rose | -55 |
| where_are_you | -110 |
| 5t4rv4t10n_1337 | -146 |
| diamond | -153 |
| arrows | -193 |
| uneruesansfin | -236 |
| attack_on_cat | -253 |
| heart | -253 |
| seepastyourgreed | -253 |
| learn_your_lesson | -263 |
| popthecork | -263 |
| krotus | -276 |
| Stash | -302 |
| UncomfortablyClose | -302 |
| cheesebottles | -353 |
| mincemeat | -353 |
| averyfineline | -383 |
| t_minus_five_minutes | -400 |
| tiny | -400 |
| ultimatestarvation | -400 |
| whatsthecatdoin | -400 |
| whereisthecheese | -400 |
| Hike | -461 |
| RUN | -461 |
| clockwisdom | -619 |
| fullofsecrets | -619 |
