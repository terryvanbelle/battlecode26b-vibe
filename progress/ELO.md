# Ladder

6084 rows of progress/games.csv (4259 local games, 1825 replica matches), rated by a batch Bradley-Terry fit on the Elo scale (`tools/elolib.py`). A local game counts its cell score (`tools/score.py`: a win or loss when kings decide the game, the points share when points do), a replica match its games won. A game between one of our builds and a field bot also fits how hard its map is for us (table at the end), so builds gauntleted on different maps compare on an average map. Each pair of players counts at most 200 games, every player carries one virtual win and loss against a fixed anchor, and ratings are relative to a mean of 1500 over all 108 players. Each of our builds is its own player. Regenerate with `tools/elo.py`; do not edit.

Games against field bots are replica scrimmages only; local games are our builds against each other and the stock players. Record = games won and lost.

![ratings of our builds](elo.png)

Our builds, in attempt order (rating +- 95%; field score = expected score against each of the 51 field players, one game each):

| build | rating | rank | games | record | field score |
|---|---|---|---|---|---|
| s0e | 1284 +- 144 | 89 of 108 | 33 | 15-18 | 34.2% |
| s0m | 1336 +- 69 | 87 of 108 | 148 | 98-50 | 37.5% |
| s0u | 1349 +- 126 | 86 of 108 | 37 | 13-24 | 38.2% |
| v0d | 1638 +- 128 | 26 of 108 | 37 | 27-10 | 56.8% |
| s2o | 1466 +- 13 | 67 of 108 | 2851 | 1210-1641 | 45.6% |
| s3e | 1430 +- 76 | 80 of 108 | 80 | 29-51 | 43.4% |
| s3f | 1489 +- 75 | 54 of 108 | 80 | 46-34 | 47.0% |
| s3g | 1430 +- 76 | 81 of 108 | 80 | 30-50 | 43.4% |
| s2o_id | 1467 +- 75 | 66 of 108 | 80 | 40-40 | 45.7% |
| s3h | 1463 +- 54 | 68 of 108 | 188 | 100-88 | 45.4% |
| s3i | 1439 +- 75 | 78 of 108 | 80 | 27-53 | 43.9% |
| s3k | 1440 +- 75 | 76 of 108 | 80 | 29-51 | 44.0% |
| s3l | 1435 +- 76 | 79 of 108 | 80 | 27-53 | 43.7% |
| s3m | 1440 +- 75 | 77 of 108 | 80 | 30-50 | 44.0% |
| s3n | 1441 +- 75 | 75 of 108 | 80 | 29-51 | 44.1% |
| s3o | 1486 +- 75 | 56 of 108 | 80 | 50-30 | 46.9% |
| s3p | 1468 +- 75 | 65 of 108 | 80 | 43-37 | 45.7% |
| s3q | 1444 +- 75 | 74 of 108 | 80 | 35-45 | 44.2% |
| s3r | 1473 +- 75 | 63 of 108 | 80 | 46-34 | 46.0% |
| s3s | 1473 +- 75 | 64 of 108 | 80 | 46-34 | 46.0% |
| s3t | 1483 +- 75 | 57 of 108 | 80 | 49-31 | 46.7% |
| s3u | 1498 +- 76 | 51 of 108 | 80 | 53-27 | 47.6% |
| s3v | 1479 +- 75 | 61 of 108 | 80 | 54-26 | 46.4% |
| s3w | 1473 +- 75 | 62 of 108 | 80 | 52-28 | 46.0% |
| s3x | 1456 +- 75 | 72 of 108 | 80 | 48-32 | 45.0% |
| s3y | 1559 +- 52 | 34 of 108 | 235 | 134-101 | 51.6% |
| s3z | 1480 +- 75 | 60 of 108 | 80 | 50-30 | 46.5% |
| s4a | 1481 +- 75 | 59 of 108 | 80 | 50-30 | 46.5% |
| s4b | 1518 +- 53 | 41 of 108 | 172 | 117-55 | 48.9% |
| s4c | 1497 +- 76 | 52 of 108 | 80 | 53-27 | 47.6% |
| s4d | 1543 +- 34 | 37 of 108 | 462 | 287-175 | 50.5% |
| arush | 1424 +- 78 | 82 of 108 | 80 | 29-51 | 42.9% |
| s4e | 1520 +- 76 | 40 of 108 | 80 | 61-19 | 49.1% |
| s4f | 1534 +- 54 | 38 of 108 | 160 | 110-50 | 49.9% |
| abrk | 1515 +- 25 | 43 of 108 | 749 | 278-471 | 48.7% |
| s4g | 1576 +- 76 | 31 of 108 | 80 | 51-29 | 52.7% |
| s4h | 1553 +- 76 | 35 of 108 | 80 | 52-28 | 51.2% |
| s4i | 1566 +- 64 | 32 of 108 | 134 | 86-48 | 52.0% |
| s4j | 1583 +- 65 | 30 of 108 | 131 | 87-44 | 53.1% |
| s4m | 1596 +- 118 | 28 of 108 | 33 | 22-11 | 54.0% |
| s4n | 1553 +- 76 | 36 of 108 | 80 | 52-28 | 51.1% |
| s4q | 1503 +- 76 | 49 of 108 | 80 | 61-19 | 48.0% |
| s4r | 1503 +- 76 | 50 of 108 | 80 | 61-19 | 48.0% |
| s4t | 1494 +- 66 | 53 of 108 | 122 | 74-48 | 47.4% |
| s5c | 1517 +- 76 | 42 of 108 | 80 | 58-22 | 48.9% |
| s5e | 1487 +- 101 | 55 of 108 | 60 | 31-29 | 46.9% |
| s5g | 1534 +- 61 | 39 of 108 | 140 | 80-60 | 49.9% |
| s5j | 1585 +- 95 | 29 of 108 | 52 | 36-16 | 53.3% |
| araid | 1509 +- 31 | 47 of 108 | 507 | 219-288 | 48.3% |
| s5o | 1513 +- 75 | 45 of 108 | 80 | 41-39 | 48.6% |
| s5r | 1508 +- 51 | 48 of 108 | 185 | 108-77 | 48.3% |
| s5s | 1562 +- 76 | 33 of 108 | 80 | 56-24 | 51.7% |
| s5t | 1509 +- 73 | 46 of 108 | 86 | 42-44 | 48.4% |
| s5w | 1761 +- 170 | 22 of 108 | 24 | 20-4 | 65.2% |
| s5x | 1481 +- 107 | 58 of 108 | 39 | 19-20 | 46.6% |
| s5y | 1514 +- 161 | 44 of 108 | 16 | 8-8 | 48.7% |
| ratbot | 1374 +- 69 | 85 of 108 | 154 | 107-47 | 39.8% |

All players:

| # | player | rating | games | record | field score |
|---:|---|---|---|---|---|
| 1 | IvanGeffner_basic51 | 2124 +- 59 | 168 | 119-49 | 89.3% |
| 2 | uravt_Version41 | 2082 +- 63 | 153 | 103-50 | 87.1% |
| 3 | uravt_Version35 | 2020 +- 57 | 162 | 95-67 | 83.5% |
| 4 | abcdezyxwv_bestsofar | 2003 +- 55 | 264 | 194-70 | 82.4% |
| 5 | awu7_awubot_us | 2000 +- 60 | 156 | 94-62 | 82.2% |
| 6 | nzjt_TheComplexMerlin | 1974 +- 63 | 150 | 95-55 | 80.6% |
| 7 | quesswho_quals | 1973 +- 53 | 186 | 110-76 | 80.5% |
| 8 | thewaxmango_PhD | 1969 +- 62 | 159 | 98-61 | 80.2% |
| 9 | quesswho_bot16 | 1965 +- 53 | 189 | 106-83 | 80.0% |
| 10 | awu7_awubot_quals | 1951 +- 59 | 165 | 102-63 | 79.0% |
| 11 | nubbbbbb_testplayer | 1942 +- 60 | 150 | 88-62 | 78.4% |
| 12 | battlecode-archive_testplayer | 1940 +- 52 | 207 | 119-88 | 78.3% |
| 13 | daannte_v52_ThisGuy | 1934 +- 53 | 183 | 97-86 | 77.8% |
| 14 | TeamSPAARK_SPAARK | 1916 +- 54 | 189 | 111-78 | 76.6% |
| 15 | daannte_v47_Skadoosh | 1909 +- 54 | 198 | 110-88 | 76.1% |
| 16 | AlexT101_usqualsbot | 1816 +- 47 | 288 | 184-104 | 69.5% |
| 17 | AlexT101_finalsbot | 1812 +- 53 | 204 | 108-96 | 69.2% |
| 18 | bman12345678_goonv23 | 1798 +- 63 | 165 | 97-68 | 68.1% |
| 19 | AdamTan12_Finals | 1772 +- 55 | 201 | 114-87 | 66.3% |
| 20 | thewaxmango_collegejunior | 1767 +- 59 | 168 | 93-75 | 65.9% |
| 21 | AdamTan12_Icarus | 1762 +- 50 | 234 | 125-109 | 65.6% |
| 22 | us:s5w | 1761 +- 170 | 24 | 20-4 | 65.2% |
| 23 | HugoIngelsson_YokoOno | 1735 +- 57 | 180 | 95-85 | 63.6% |
| 24 | VarunVejalla_final4_run_away10 | 1696 +- 65 | 153 | 86-67 | 60.9% |
| 25 | HugoIngelsson_Xzibit | 1660 +- 59 | 177 | 97-80 | 58.4% |
| 26 | us:v0d | 1638 +- 128 | 37 | 27-10 | 56.8% |
| 27 | spsquared_TSPAARK_QUALS | 1638 +- 57 | 186 | 104-82 | 56.9% |
| 28 | us:s4m | 1596 +- 118 | 33 | 22-11 | 54.0% |
| 29 | us:s5j | 1585 +- 95 | 52 | 36-16 | 53.3% |
| 30 | us:s4j | 1583 +- 65 | 131 | 87-44 | 53.1% |
| 31 | us:s4g | 1576 +- 76 | 80 | 51-29 | 52.7% |
| 32 | us:s4i | 1566 +- 64 | 134 | 86-48 | 52.0% |
| 33 | us:s5s | 1562 +- 76 | 80 | 56-24 | 51.7% |
| 34 | us:s3y | 1559 +- 52 | 235 | 134-101 | 51.6% |
| 35 | us:s4h | 1553 +- 76 | 80 | 52-28 | 51.2% |
| 36 | us:s4n | 1553 +- 76 | 80 | 52-28 | 51.1% |
| 37 | us:s4d | 1543 +- 34 | 462 | 287-175 | 50.5% |
| 38 | us:s4f | 1534 +- 54 | 160 | 110-50 | 49.9% |
| 39 | us:s5g | 1534 +- 61 | 140 | 80-60 | 49.9% |
| 40 | us:s4e | 1520 +- 76 | 80 | 61-19 | 49.1% |
| 41 | us:s4b | 1518 +- 53 | 172 | 117-55 | 48.9% |
| 42 | us:s5c | 1517 +- 76 | 80 | 58-22 | 48.9% |
| 43 | us:abrk | 1515 +- 25 | 749 | 278-471 | 48.7% |
| 44 | us:s5y | 1514 +- 161 | 16 | 8-8 | 48.7% |
| 45 | us:s5o | 1513 +- 75 | 80 | 41-39 | 48.6% |
| 46 | us:s5t | 1509 +- 73 | 86 | 42-44 | 48.4% |
| 47 | us:araid | 1509 +- 31 | 507 | 219-288 | 48.3% |
| 48 | us:s5r | 1508 +- 51 | 185 | 108-77 | 48.3% |
| 49 | us:s4q | 1503 +- 76 | 80 | 61-19 | 48.0% |
| 50 | us:s4r | 1503 +- 76 | 80 | 61-19 | 48.0% |
| 51 | us:s3u | 1498 +- 76 | 80 | 53-27 | 47.6% |
| 52 | us:s4c | 1497 +- 76 | 80 | 53-27 | 47.6% |
| 53 | us:s4t | 1494 +- 66 | 122 | 74-48 | 47.4% |
| 54 | us:s3f | 1489 +- 75 | 80 | 46-34 | 47.0% |
| 55 | us:s5e | 1487 +- 101 | 60 | 31-29 | 46.9% |
| 56 | us:s3o | 1486 +- 75 | 80 | 50-30 | 46.9% |
| 57 | us:s3t | 1483 +- 75 | 80 | 49-31 | 46.7% |
| 58 | us:s5x | 1481 +- 107 | 39 | 19-20 | 46.6% |
| 59 | us:s4a | 1481 +- 75 | 80 | 50-30 | 46.5% |
| 60 | us:s3z | 1480 +- 75 | 80 | 50-30 | 46.5% |
| 61 | us:s3v | 1479 +- 75 | 80 | 54-26 | 46.4% |
| 62 | us:s3w | 1473 +- 75 | 80 | 52-28 | 46.0% |
| 63 | us:s3r | 1473 +- 75 | 80 | 46-34 | 46.0% |
| 64 | us:s3s | 1473 +- 75 | 80 | 46-34 | 46.0% |
| 65 | us:s3p | 1468 +- 75 | 80 | 43-37 | 45.7% |
| 66 | us:s2o_id | 1467 +- 75 | 80 | 40-40 | 45.7% |
| 67 | us:s2o | 1466 +- 13 | 2851 | 1210-1641 | 45.6% |
| 68 | us:s3h | 1463 +- 54 | 188 | 100-88 | 45.4% |
| 69 | CS50Crab_protoSaberV7 | 1462 +- 57 | 186 | 91-95 | 45.3% |
| 70 | CS50Crab_rapierV1 | 1462 +- 45 | 279 | 133-146 | 45.3% |
| 71 | amcsz_fighterrats | 1462 +- 58 | 177 | 87-90 | 45.3% |
| 72 | us:s3x | 1456 +- 75 | 80 | 48-32 | 45.0% |
| 73 | outercloudstudio_v_1_18 | 1455 +- 52 | 225 | 92-133 | 44.8% |
| 74 | us:s3q | 1444 +- 75 | 80 | 35-45 | 44.2% |
| 75 | us:s3n | 1441 +- 75 | 80 | 29-51 | 44.1% |
| 76 | us:s3k | 1440 +- 75 | 80 | 29-51 | 44.0% |
| 77 | us:s3m | 1440 +- 75 | 80 | 30-50 | 44.0% |
| 78 | us:s3i | 1439 +- 75 | 80 | 27-53 | 43.9% |
| 79 | us:s3l | 1435 +- 76 | 80 | 27-53 | 43.7% |
| 80 | us:s3e | 1430 +- 76 | 80 | 29-51 | 43.4% |
| 81 | us:s3g | 1430 +- 76 | 80 | 30-50 | 43.4% |
| 82 | us:arush | 1424 +- 78 | 80 | 29-51 | 42.9% |
| 83 | bc26vibe_bot | 1421 +- 45 | 279 | 134-145 | 42.7% |
| 84 | PixelRifts_mushakraj_mk3 | 1408 +- 57 | 195 | 92-103 | 41.8% |
| 85 | us:ratbot | 1374 +- 69 | 154 | 107-47 | 39.8% |
| 86 | us:s0u | 1349 +- 126 | 37 | 13-24 | 38.2% |
| 87 | us:s0m | 1336 +- 69 | 148 | 98-50 | 37.5% |
| 88 | XSercia_genies | 1311 +- 46 | 333 | 111-222 | 35.6% |
| 89 | us:s0e | 1284 +- 144 | 33 | 15-18 | 34.2% |
| 90 | C0derTang_v6 | 1281 +- 50 | 258 | 89-169 | 33.7% |
| 91 | Naton-Cai_ratplayer | 1245 +- 63 | 153 | 71-82 | 31.4% |
| 92 | ask8000_bumchit67 | 1240 +- 48 | 288 | 100-188 | 31.1% |
| 93 | justinottesen_alfredo | 1203 +- 54 | 207 | 93-114 | 28.7% |
| 94 | bc26-example | 1192 +- 35 | 553 | 209-344 | 28.1% |
| 95 | Camleaf_mk1 | 1169 +- 61 | 168 | 71-97 | 26.6% |
| 96 | vincentwang60_javabot | 1163 +- 60 | 171 | 72-99 | 26.3% |
| 97 | bc26-example-copy | 1132 +- 55 | 183 | 78-105 | 24.4% |
| 98 | justinottesen_carbonara | 1100 +- 61 | 159 | 72-87 | 22.5% |
| 99 | Gavdingo_v1 | 1091 +- 53 | 210 | 87-123 | 22.1% |
| 100 | battlecode-archive_result_408 | 1083 +- 58 | 174 | 78-96 | 21.6% |
| 101 | GuzziFlipFlops_Hermesv8 | 1061 +- 54 | 195 | 84-111 | 20.3% |
| 102 | j2399_weighted_micro_v9 | 1032 +- 61 | 168 | 68-100 | 18.8% |
| 103 | quanticsoul4772_ratbot | 975 +- 59 | 180 | 76-104 | 15.9% |
| 104 | seru9_TOTAL_AGGRO | 954 +- 62 | 180 | 73-107 | 14.9% |
| 105 | Victor-Ramirez-Garcia_battleca | 911 +- 63 | 189 | 71-118 | 13.0% |
| 106 | anicolao_myplayer | 693 +- 74 | 168 | 40-128 | 5.8% |
| 107 | j2399_weighted_path | 663 +- 82 | 114 | 34-80 | 5.1% |
| 108 | Krak9n_robot | 541 +- 91 | 126 | 18-108 | 2.7% |

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
| replica | +37 |
| sunandmoon | +17 |
| tornadoes | -23 |
| canyoudig | -28 |
| rose | -54 |
| TheHeist | -54 |
| groveisbeingdesecrated | -54 |
| where_are_you | -110 |
| 5t4rv4t10n_1337 | -146 |
| diamond | -152 |
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
