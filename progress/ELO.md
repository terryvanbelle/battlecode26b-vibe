# Ladder

5654 rows of progress/games.csv (3910 local games, 1744 replica matches), rated by a batch Bradley-Terry fit on the Elo scale (`tools/elolib.py`). A local game counts its cell score (`tools/score.py`: a win or loss when kings decide the game, the points share when points do), a replica match its games won. A game between one of our builds and a field bot also fits how hard its map is for us (table at the end), so builds gauntleted on different maps compare on an average map. Each pair of players counts at most 200 games, every player carries one virtual win and loss against a fixed anchor, and ratings are relative to a mean of 1500 over all 105 players. Each of our builds is its own player. Regenerate with `tools/elo.py`; do not edit.

Games against field bots are replica scrimmages only; local games are our builds against each other and the stock players. Record = games won and lost.

![ratings of our builds](elo.png)

Our builds, in attempt order (rating +- 95%; field score = expected score against each of the 51 field players, one game each):

| build | rating | rank | games | record | field score |
|---|---|---|---|---|---|
| s0e | 1286 +- 144 | 86 of 105 | 33 | 15-18 | 34.2% |
| s0m | 1336 +- 69 | 84 of 105 | 148 | 98-50 | 37.3% |
| s0u | 1348 +- 126 | 83 of 105 | 37 | 13-24 | 38.1% |
| v0d | 1638 +- 128 | 26 of 105 | 37 | 27-10 | 56.6% |
| s2o | 1470 +- 13 | 65 of 105 | 2788 | 1191-1597 | 45.8% |
| s3e | 1434 +- 76 | 77 of 105 | 80 | 29-51 | 43.5% |
| s3f | 1493 +- 75 | 52 of 105 | 80 | 46-34 | 47.2% |
| s3g | 1434 +- 76 | 78 of 105 | 80 | 30-50 | 43.5% |
| s2o_id | 1471 +- 75 | 64 of 105 | 80 | 40-40 | 45.8% |
| s3h | 1467 +- 54 | 67 of 105 | 188 | 100-88 | 45.6% |
| s3i | 1443 +- 75 | 75 of 105 | 80 | 27-53 | 44.1% |
| s3k | 1445 +- 75 | 73 of 105 | 80 | 29-51 | 44.2% |
| s3l | 1439 +- 76 | 76 of 105 | 80 | 27-53 | 43.8% |
| s3m | 1445 +- 75 | 74 of 105 | 80 | 30-50 | 44.2% |
| s3n | 1446 +- 75 | 72 of 105 | 80 | 29-51 | 44.2% |
| s3o | 1491 +- 75 | 54 of 105 | 80 | 50-30 | 47.1% |
| s3p | 1472 +- 75 | 62 of 105 | 80 | 43-37 | 45.9% |
| s3q | 1448 +- 75 | 71 of 105 | 80 | 35-45 | 44.4% |
| s3r | 1477 +- 75 | 60 of 105 | 80 | 46-34 | 46.2% |
| s3s | 1477 +- 75 | 61 of 105 | 80 | 46-34 | 46.2% |
| s3t | 1488 +- 75 | 55 of 105 | 80 | 49-31 | 46.9% |
| s3u | 1502 +- 76 | 49 of 105 | 80 | 53-27 | 47.8% |
| s3v | 1483 +- 75 | 58 of 105 | 80 | 54-26 | 46.6% |
| s3w | 1477 +- 75 | 59 of 105 | 80 | 52-28 | 46.2% |
| s3x | 1460 +- 75 | 69 of 105 | 80 | 48-32 | 45.1% |
| s3y | 1554 +- 59 | 35 of 105 | 193 | 115-78 | 51.1% |
| s3z | 1485 +- 75 | 57 of 105 | 80 | 50-30 | 46.7% |
| s4a | 1485 +- 75 | 56 of 105 | 80 | 50-30 | 46.7% |
| s4b | 1522 +- 53 | 43 of 105 | 172 | 117-55 | 49.1% |
| s4c | 1501 +- 76 | 50 of 105 | 80 | 53-27 | 47.8% |
| s4d | 1551 +- 36 | 36 of 105 | 424 | 266-158 | 51.0% |
| arush | 1427 +- 78 | 79 of 105 | 80 | 29-51 | 43.1% |
| s4e | 1525 +- 76 | 42 of 105 | 80 | 61-19 | 49.2% |
| s4f | 1539 +- 54 | 37 of 105 | 160 | 110-50 | 50.2% |
| abrk | 1521 +- 25 | 45 of 105 | 749 | 278-471 | 49.0% |
| s4g | 1582 +- 76 | 30 of 105 | 80 | 51-29 | 52.9% |
| s4h | 1559 +- 76 | 33 of 105 | 80 | 52-28 | 51.5% |
| s4i | 1571 +- 64 | 31 of 105 | 134 | 86-48 | 52.2% |
| s4j | 1588 +- 65 | 29 of 105 | 131 | 87-44 | 53.4% |
| s4m | 1602 +- 118 | 27 of 105 | 33 | 22-11 | 54.2% |
| s4n | 1559 +- 76 | 34 of 105 | 80 | 52-28 | 51.4% |
| s4q | 1507 +- 76 | 46 of 105 | 80 | 61-19 | 48.1% |
| s4r | 1507 +- 76 | 47 of 105 | 80 | 61-19 | 48.1% |
| s4t | 1499 +- 66 | 51 of 105 | 122 | 74-48 | 47.6% |
| s5c | 1521 +- 76 | 44 of 105 | 80 | 58-22 | 49.0% |
| s5e | 1491 +- 101 | 53 of 105 | 60 | 31-29 | 47.1% |
| s5g | 1539 +- 61 | 38 of 105 | 140 | 80-60 | 50.1% |
| s5j | 1591 +- 95 | 28 of 105 | 52 | 36-16 | 53.5% |
| araid | 1527 +- 44 | 41 of 105 | 235 | 111-124 | 49.4% |
| s5o | 1530 +- 75 | 40 of 105 | 80 | 41-39 | 49.6% |
| s5r | 1533 +- 103 | 39 of 105 | 42 | 25-17 | 49.8% |
| s5s | 1505 +- 189 | 48 of 105 | 11 | 6-5 | 48.0% |
| s5t | 1564 +- 200 | 32 of 105 | 10 | 6-4 | 51.8% |
| ratbot | 1373 +- 69 | 82 of 105 | 154 | 107-47 | 39.7% |

All players:

| # | player | rating | games | record | field score |
|---:|---|---|---|---|---|
| 1 | IvanGeffner_basic51 | 2118 +- 61 | 159 | 112-47 | 88.9% |
| 2 | uravt_Version41 | 2092 +- 65 | 147 | 102-45 | 87.6% |
| 3 | uravt_Version35 | 2025 +- 57 | 159 | 94-65 | 83.8% |
| 4 | abcdezyxwv_bestsofar | 2006 +- 56 | 252 | 186-66 | 82.6% |
| 5 | awu7_awubot_us | 2002 +- 60 | 156 | 94-62 | 82.3% |
| 6 | thewaxmango_PhD | 1980 +- 63 | 156 | 98-58 | 80.9% |
| 7 | quesswho_quals | 1974 +- 53 | 186 | 110-76 | 80.5% |
| 8 | nzjt_TheComplexMerlin | 1971 +- 65 | 144 | 91-53 | 80.3% |
| 9 | quesswho_bot16 | 1961 +- 55 | 177 | 97-80 | 79.6% |
| 10 | nubbbbbb_testplayer | 1950 +- 63 | 141 | 84-57 | 78.9% |
| 11 | awu7_awubot_quals | 1947 +- 59 | 162 | 100-62 | 78.7% |
| 12 | battlecode-archive_testplayer | 1941 +- 52 | 204 | 118-86 | 78.3% |
| 13 | daannte_v52_ThisGuy | 1935 +- 54 | 177 | 94-83 | 77.9% |
| 14 | daannte_v47_Skadoosh | 1913 +- 55 | 189 | 105-84 | 76.3% |
| 15 | TeamSPAARK_SPAARK | 1902 +- 56 | 180 | 103-77 | 75.5% |
| 16 | AlexT101_usqualsbot | 1819 +- 49 | 264 | 170-94 | 69.6% |
| 17 | AlexT101_finalsbot | 1816 +- 55 | 195 | 104-91 | 69.3% |
| 18 | bman12345678_goonv23 | 1794 +- 66 | 156 | 93-63 | 67.8% |
| 19 | thewaxmango_collegejunior | 1775 +- 62 | 153 | 89-64 | 66.4% |
| 20 | AdamTan12_Finals | 1762 +- 61 | 168 | 90-78 | 65.5% |
| 21 | AdamTan12_Icarus | 1760 +- 51 | 225 | 120-105 | 65.3% |
| 22 | HugoIngelsson_YokoOno | 1746 +- 58 | 174 | 95-79 | 64.3% |
| 23 | VarunVejalla_final4_run_away10 | 1687 +- 67 | 144 | 79-65 | 60.1% |
| 24 | HugoIngelsson_Xzibit | 1668 +- 60 | 174 | 97-77 | 58.8% |
| 25 | spsquared_TSPAARK_QUALS | 1642 +- 64 | 156 | 86-70 | 57.0% |
| 26 | us:v0d | 1638 +- 128 | 37 | 27-10 | 56.6% |
| 27 | us:s4m | 1602 +- 118 | 33 | 22-11 | 54.2% |
| 28 | us:s5j | 1591 +- 95 | 52 | 36-16 | 53.5% |
| 29 | us:s4j | 1588 +- 65 | 131 | 87-44 | 53.4% |
| 30 | us:s4g | 1582 +- 76 | 80 | 51-29 | 52.9% |
| 31 | us:s4i | 1571 +- 64 | 134 | 86-48 | 52.2% |
| 32 | us:s5t | 1564 +- 200 | 10 | 6-4 | 51.8% |
| 33 | us:s4h | 1559 +- 76 | 80 | 52-28 | 51.5% |
| 34 | us:s4n | 1559 +- 76 | 80 | 52-28 | 51.4% |
| 35 | us:s3y | 1554 +- 59 | 193 | 115-78 | 51.1% |
| 36 | us:s4d | 1551 +- 36 | 424 | 266-158 | 51.0% |
| 37 | us:s4f | 1539 +- 54 | 160 | 110-50 | 50.2% |
| 38 | us:s5g | 1539 +- 61 | 140 | 80-60 | 50.1% |
| 39 | us:s5r | 1533 +- 103 | 42 | 25-17 | 49.8% |
| 40 | us:s5o | 1530 +- 75 | 80 | 41-39 | 49.6% |
| 41 | us:araid | 1527 +- 44 | 235 | 111-124 | 49.4% |
| 42 | us:s4e | 1525 +- 76 | 80 | 61-19 | 49.2% |
| 43 | us:s4b | 1522 +- 53 | 172 | 117-55 | 49.1% |
| 44 | us:s5c | 1521 +- 76 | 80 | 58-22 | 49.0% |
| 45 | us:abrk | 1521 +- 25 | 749 | 278-471 | 49.0% |
| 46 | us:s4q | 1507 +- 76 | 80 | 61-19 | 48.1% |
| 47 | us:s4r | 1507 +- 76 | 80 | 61-19 | 48.1% |
| 48 | us:s5s | 1505 +- 189 | 11 | 6-5 | 48.0% |
| 49 | us:s3u | 1502 +- 76 | 80 | 53-27 | 47.8% |
| 50 | us:s4c | 1501 +- 76 | 80 | 53-27 | 47.8% |
| 51 | us:s4t | 1499 +- 66 | 122 | 74-48 | 47.6% |
| 52 | us:s3f | 1493 +- 75 | 80 | 46-34 | 47.2% |
| 53 | us:s5e | 1491 +- 101 | 60 | 31-29 | 47.1% |
| 54 | us:s3o | 1491 +- 75 | 80 | 50-30 | 47.1% |
| 55 | us:s3t | 1488 +- 75 | 80 | 49-31 | 46.9% |
| 56 | us:s4a | 1485 +- 75 | 80 | 50-30 | 46.7% |
| 57 | us:s3z | 1485 +- 75 | 80 | 50-30 | 46.7% |
| 58 | us:s3v | 1483 +- 75 | 80 | 54-26 | 46.6% |
| 59 | us:s3w | 1477 +- 75 | 80 | 52-28 | 46.2% |
| 60 | us:s3r | 1477 +- 75 | 80 | 46-34 | 46.2% |
| 61 | us:s3s | 1477 +- 75 | 80 | 46-34 | 46.2% |
| 62 | us:s3p | 1472 +- 75 | 80 | 43-37 | 45.9% |
| 63 | amcsz_fighterrats | 1472 +- 60 | 168 | 85-83 | 45.8% |
| 64 | us:s2o_id | 1471 +- 75 | 80 | 40-40 | 45.8% |
| 65 | us:s2o | 1470 +- 13 | 2788 | 1191-1597 | 45.8% |
| 66 | CS50Crab_protoSaberV7 | 1468 +- 59 | 177 | 89-88 | 45.6% |
| 67 | us:s3h | 1467 +- 54 | 188 | 100-88 | 45.6% |
| 68 | CS50Crab_rapierV1 | 1463 +- 46 | 270 | 129-141 | 45.3% |
| 69 | us:s3x | 1460 +- 75 | 80 | 48-32 | 45.1% |
| 70 | outercloudstudio_v_1_18 | 1452 +- 53 | 216 | 88-128 | 44.6% |
| 71 | us:s3q | 1448 +- 75 | 80 | 35-45 | 44.4% |
| 72 | us:s3n | 1446 +- 75 | 80 | 29-51 | 44.2% |
| 73 | us:s3k | 1445 +- 75 | 80 | 29-51 | 44.2% |
| 74 | us:s3m | 1445 +- 75 | 80 | 30-50 | 44.2% |
| 75 | us:s3i | 1443 +- 75 | 80 | 27-53 | 44.1% |
| 76 | us:s3l | 1439 +- 76 | 80 | 27-53 | 43.8% |
| 77 | us:s3e | 1434 +- 76 | 80 | 29-51 | 43.5% |
| 78 | us:s3g | 1434 +- 76 | 80 | 30-50 | 43.5% |
| 79 | us:arush | 1427 +- 78 | 80 | 29-51 | 43.1% |
| 80 | bc26vibe_bot | 1420 +- 46 | 267 | 127-140 | 42.5% |
| 81 | PixelRifts_mushakraj_mk3 | 1408 +- 59 | 189 | 88-101 | 41.7% |
| 82 | us:ratbot | 1373 +- 69 | 154 | 107-47 | 39.7% |
| 83 | us:s0u | 1348 +- 126 | 37 | 13-24 | 38.1% |
| 84 | us:s0m | 1336 +- 69 | 148 | 98-50 | 37.3% |
| 85 | XSercia_genies | 1313 +- 46 | 330 | 110-220 | 35.6% |
| 86 | us:s0e | 1286 +- 144 | 33 | 15-18 | 34.2% |
| 87 | C0derTang_v6 | 1282 +- 50 | 255 | 87-168 | 33.7% |
| 88 | Naton-Cai_ratplayer | 1250 +- 65 | 147 | 69-78 | 31.7% |
| 89 | ask8000_bumchit67 | 1248 +- 49 | 279 | 98-181 | 31.5% |
| 90 | justinottesen_alfredo | 1204 +- 55 | 204 | 91-113 | 28.7% |
| 91 | bc26-example | 1191 +- 35 | 550 | 207-343 | 27.9% |
| 92 | vincentwang60_javabot | 1166 +- 61 | 168 | 71-97 | 26.4% |
| 93 | Camleaf_mk1 | 1165 +- 61 | 165 | 69-96 | 26.3% |
| 94 | bc26-example-copy | 1126 +- 56 | 174 | 72-102 | 24.0% |
| 95 | justinottesen_carbonara | 1097 +- 64 | 150 | 67-83 | 22.3% |
| 96 | Gavdingo_v1 | 1096 +- 54 | 201 | 83-118 | 22.3% |
| 97 | battlecode-archive_result_408 | 1075 +- 60 | 165 | 71-94 | 21.1% |
| 98 | GuzziFlipFlops_Hermesv8 | 1062 +- 53 | 195 | 84-111 | 20.4% |
| 99 | j2399_weighted_micro_v9 | 1040 +- 64 | 159 | 66-93 | 19.2% |
| 100 | quanticsoul4772_ratbot | 976 +- 59 | 180 | 76-104 | 15.9% |
| 101 | seru9_TOTAL_AGGRO | 952 +- 63 | 177 | 71-106 | 14.8% |
| 102 | Victor-Ramirez-Garcia_battleca | 923 +- 65 | 180 | 67-113 | 13.5% |
| 103 | anicolao_myplayer | 692 +- 78 | 159 | 37-122 | 5.9% |
| 104 | j2399_weighted_path | 636 +- 88 | 105 | 30-75 | 4.6% |
| 105 | Krak9n_robot | 514 +- 96 | 123 | 16-107 | 2.4% |

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
| houseofcards | +38 |
| replica | +34 |
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
