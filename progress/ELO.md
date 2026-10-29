# Ladder

6293 rows of progress/games.csv (4422 local games, 1871 replica matches), rated by a batch Bradley-Terry fit on the Elo scale (`tools/elolib.py`). A local game counts its cell score (`tools/score.py`: a win or loss when kings decide the game, the points share when points do), a replica match its games won. A game between one of our builds and a field bot also fits how hard its map is for us (table at the end), so builds gauntleted on different maps compare on an average map. Each pair of players counts at most 200 games, every player carries one virtual win and loss against a fixed anchor, and ratings are relative to a mean of 1500 over all 112 players. Each of our builds is its own player. Regenerate with `tools/elo.py`; do not edit.

Games against field bots are replica scrimmages only; local games are our builds against each other and the stock players. Record = games won and lost.

![ratings of our builds](elo.png)

Our builds, in attempt order (rating +- 95%; field score = expected score against each of the 51 field players, one game each):

| build | rating | rank | games | record | field score |
|---|---|---|---|---|---|
| s0e | 1288 +- 144 | 93 of 112 | 33 | 15-18 | 34.6% |
| s0m | 1337 +- 69 | 90 of 112 | 148 | 98-50 | 37.6% |
| s0u | 1350 +- 126 | 89 of 112 | 37 | 13-24 | 38.4% |
| v0d | 1639 +- 128 | 26 of 112 | 37 | 27-10 | 56.6% |
| s2o | 1471 +- 13 | 70 of 112 | 2868 | 1214-1654 | 45.9% |
| s3e | 1435 +- 76 | 83 of 112 | 80 | 29-51 | 43.7% |
| s3f | 1493 +- 75 | 58 of 112 | 80 | 46-34 | 47.3% |
| s3g | 1435 +- 76 | 84 of 112 | 80 | 30-50 | 43.7% |
| s2o_id | 1472 +- 75 | 69 of 112 | 80 | 40-40 | 46.0% |
| s3h | 1469 +- 54 | 71 of 112 | 188 | 100-88 | 45.8% |
| s3i | 1444 +- 75 | 81 of 112 | 80 | 27-53 | 44.2% |
| s3k | 1445 +- 75 | 79 of 112 | 80 | 29-51 | 44.3% |
| s3l | 1440 +- 76 | 82 of 112 | 80 | 27-53 | 44.0% |
| s3m | 1445 +- 75 | 80 of 112 | 80 | 30-50 | 44.3% |
| s3n | 1446 +- 75 | 78 of 112 | 80 | 29-51 | 44.4% |
| s3o | 1491 +- 75 | 59 of 112 | 80 | 50-30 | 47.2% |
| s3p | 1473 +- 75 | 68 of 112 | 80 | 43-37 | 46.0% |
| s3q | 1448 +- 75 | 77 of 112 | 80 | 35-45 | 44.5% |
| s3r | 1477 +- 75 | 66 of 112 | 80 | 46-34 | 46.3% |
| s3s | 1477 +- 75 | 67 of 112 | 80 | 46-34 | 46.3% |
| s3t | 1488 +- 75 | 60 of 112 | 80 | 49-31 | 47.0% |
| s3u | 1502 +- 76 | 53 of 112 | 80 | 53-27 | 47.9% |
| s3v | 1484 +- 75 | 64 of 112 | 80 | 54-26 | 46.7% |
| s3w | 1477 +- 75 | 65 of 112 | 80 | 52-28 | 46.3% |
| s3x | 1460 +- 75 | 75 of 112 | 80 | 48-32 | 45.3% |
| s3y | 1566 +- 52 | 33 of 112 | 235 | 134-101 | 51.9% |
| s3z | 1485 +- 75 | 62 of 112 | 80 | 50-30 | 46.8% |
| s4a | 1485 +- 75 | 61 of 112 | 80 | 50-30 | 46.8% |
| s4b | 1523 +- 53 | 43 of 112 | 172 | 117-55 | 49.2% |
| s4c | 1502 +- 76 | 54 of 112 | 80 | 53-27 | 47.9% |
| s4d | 1547 +- 34 | 37 of 112 | 462 | 287-175 | 50.7% |
| arush | 1429 +- 78 | 85 of 112 | 80 | 29-51 | 43.3% |
| s4e | 1525 +- 76 | 42 of 112 | 80 | 61-19 | 49.3% |
| s4f | 1539 +- 54 | 40 of 112 | 160 | 110-50 | 50.2% |
| abrk | 1520 +- 25 | 45 of 112 | 749 | 278-471 | 49.0% |
| s4g | 1581 +- 76 | 31 of 112 | 80 | 51-29 | 52.9% |
| s4h | 1559 +- 76 | 34 of 112 | 80 | 52-28 | 51.4% |
| s4i | 1572 +- 64 | 32 of 112 | 134 | 86-48 | 52.3% |
| s4j | 1589 +- 65 | 30 of 112 | 131 | 87-44 | 53.4% |
| s4m | 1601 +- 118 | 28 of 112 | 33 | 22-11 | 54.1% |
| s4n | 1558 +- 76 | 35 of 112 | 80 | 52-28 | 51.4% |
| s4q | 1508 +- 76 | 48 of 112 | 80 | 61-19 | 48.2% |
| s4r | 1508 +- 76 | 49 of 112 | 80 | 61-19 | 48.2% |
| s4t | 1500 +- 66 | 55 of 112 | 122 | 74-48 | 47.7% |
| s5c | 1522 +- 76 | 44 of 112 | 80 | 58-22 | 49.1% |
| s5e | 1494 +- 102 | 57 of 112 | 60 | 31-29 | 47.3% |
| s5g | 1540 +- 61 | 39 of 112 | 140 | 80-60 | 50.3% |
| s5j | 1591 +- 95 | 29 of 112 | 52 | 36-16 | 53.5% |
| araid | 1505 +- 27 | 52 of 112 | 653 | 268-385 | 48.0% |
| s5o | 1509 +- 75 | 47 of 112 | 80 | 41-39 | 48.3% |
| s5r | 1510 +- 49 | 46 of 112 | 202 | 121-81 | 48.4% |
| s5s | 1557 +- 76 | 36 of 112 | 80 | 56-24 | 51.4% |
| s5t | 1506 +- 73 | 50 of 112 | 86 | 42-44 | 48.1% |
| s5w | 1659 +- 91 | 25 of 112 | 72 | 45-27 | 57.9% |
| s5x | 1499 +- 90 | 56 of 112 | 55 | 30-25 | 47.7% |
| s5y | 1544 +- 76 | 38 of 112 | 80 | 53-27 | 50.5% |
| s6c | 1314 +- 393 | 91 of 112 | 2 | 0-2 | 36.2% |
| s6e | 1526 +- 96 | 41 of 112 | 48 | 31-17 | 49.4% |
| s6f | 1505 +- 278 | 51 of 112 | 4 | 2-2 | 48.0% |
| s6g | 1485 +- 182 | 63 of 112 | 12 | 8-4 | 46.8% |
| ratbot | 1375 +- 69 | 88 of 112 | 154 | 107-47 | 39.9% |

All players:

| # | player | rating | games | record | field score |
|---:|---|---|---|---|---|
| 1 | IvanGeffner_basic51 | 2132 +- 59 | 171 | 121-50 | 89.4% |
| 2 | uravt_Version41 | 2088 +- 62 | 156 | 104-52 | 87.1% |
| 3 | uravt_Version35 | 2029 +- 56 | 165 | 97-68 | 83.7% |
| 4 | abcdezyxwv_bestsofar | 2017 +- 54 | 273 | 203-70 | 83.0% |
| 5 | awu7_awubot_us | 2008 +- 58 | 165 | 99-66 | 82.4% |
| 6 | nzjt_TheComplexMerlin | 1979 +- 63 | 150 | 95-55 | 80.6% |
| 7 | thewaxmango_PhD | 1975 +- 61 | 165 | 101-64 | 80.3% |
| 8 | quesswho_quals | 1971 +- 52 | 192 | 111-81 | 80.0% |
| 9 | quesswho_bot16 | 1970 +- 53 | 192 | 107-85 | 79.9% |
| 10 | awu7_awubot_quals | 1959 +- 58 | 168 | 104-64 | 79.2% |
| 11 | nubbbbbb_testplayer | 1944 +- 60 | 153 | 89-64 | 78.2% |
| 12 | battlecode-archive_testplayer | 1943 +- 51 | 210 | 120-90 | 78.2% |
| 13 | daannte_v52_ThisGuy | 1939 +- 53 | 183 | 97-86 | 77.9% |
| 14 | TeamSPAARK_SPAARK | 1921 +- 54 | 189 | 111-78 | 76.6% |
| 15 | daannte_v47_Skadoosh | 1916 +- 53 | 201 | 112-89 | 76.3% |
| 16 | AlexT101_usqualsbot | 1826 +- 46 | 297 | 192-105 | 69.9% |
| 17 | AlexT101_finalsbot | 1817 +- 53 | 204 | 108-96 | 69.2% |
| 18 | bman12345678_goonv23 | 1805 +- 63 | 168 | 99-69 | 68.3% |
| 19 | AdamTan12_Finals | 1780 +- 53 | 213 | 122-91 | 66.6% |
| 20 | thewaxmango_collegejunior | 1774 +- 58 | 171 | 95-76 | 66.1% |
| 21 | AdamTan12_Icarus | 1766 +- 49 | 237 | 126-111 | 65.5% |
| 22 | HugoIngelsson_YokoOno | 1737 +- 57 | 183 | 96-87 | 63.5% |
| 23 | VarunVejalla_final4_run_away10 | 1700 +- 65 | 153 | 86-67 | 60.9% |
| 24 | HugoIngelsson_Xzibit | 1662 +- 59 | 180 | 98-82 | 58.3% |
| 25 | us:s5w | 1659 +- 91 | 72 | 45-27 | 57.9% |
| 26 | us:v0d | 1639 +- 128 | 37 | 27-10 | 56.6% |
| 27 | spsquared_TSPAARK_QUALS | 1632 +- 56 | 192 | 105-87 | 56.3% |
| 28 | us:s4m | 1601 +- 118 | 33 | 22-11 | 54.1% |
| 29 | us:s5j | 1591 +- 95 | 52 | 36-16 | 53.5% |
| 30 | us:s4j | 1589 +- 65 | 131 | 87-44 | 53.4% |
| 31 | us:s4g | 1581 +- 76 | 80 | 51-29 | 52.9% |
| 32 | us:s4i | 1572 +- 64 | 134 | 86-48 | 52.3% |
| 33 | us:s3y | 1566 +- 52 | 235 | 134-101 | 51.9% |
| 34 | us:s4h | 1559 +- 76 | 80 | 52-28 | 51.4% |
| 35 | us:s4n | 1558 +- 76 | 80 | 52-28 | 51.4% |
| 36 | us:s5s | 1557 +- 76 | 80 | 56-24 | 51.4% |
| 37 | us:s4d | 1547 +- 34 | 462 | 287-175 | 50.7% |
| 38 | us:s5y | 1544 +- 76 | 80 | 53-27 | 50.5% |
| 39 | us:s5g | 1540 +- 61 | 140 | 80-60 | 50.3% |
| 40 | us:s4f | 1539 +- 54 | 160 | 110-50 | 50.2% |
| 41 | us:s6e | 1526 +- 96 | 48 | 31-17 | 49.4% |
| 42 | us:s4e | 1525 +- 76 | 80 | 61-19 | 49.3% |
| 43 | us:s4b | 1523 +- 53 | 172 | 117-55 | 49.2% |
| 44 | us:s5c | 1522 +- 76 | 80 | 58-22 | 49.1% |
| 45 | us:abrk | 1520 +- 25 | 749 | 278-471 | 49.0% |
| 46 | us:s5r | 1510 +- 49 | 202 | 121-81 | 48.4% |
| 47 | us:s5o | 1509 +- 75 | 80 | 41-39 | 48.3% |
| 48 | us:s4q | 1508 +- 76 | 80 | 61-19 | 48.2% |
| 49 | us:s4r | 1508 +- 76 | 80 | 61-19 | 48.2% |
| 50 | us:s5t | 1506 +- 73 | 86 | 42-44 | 48.1% |
| 51 | us:s6f | 1505 +- 278 | 4 | 2-2 | 48.0% |
| 52 | us:araid | 1505 +- 27 | 653 | 268-385 | 48.0% |
| 53 | us:s3u | 1502 +- 76 | 80 | 53-27 | 47.9% |
| 54 | us:s4c | 1502 +- 76 | 80 | 53-27 | 47.9% |
| 55 | us:s4t | 1500 +- 66 | 122 | 74-48 | 47.7% |
| 56 | us:s5x | 1499 +- 90 | 55 | 30-25 | 47.7% |
| 57 | us:s5e | 1494 +- 102 | 60 | 31-29 | 47.3% |
| 58 | us:s3f | 1493 +- 75 | 80 | 46-34 | 47.3% |
| 59 | us:s3o | 1491 +- 75 | 80 | 50-30 | 47.2% |
| 60 | us:s3t | 1488 +- 75 | 80 | 49-31 | 47.0% |
| 61 | us:s4a | 1485 +- 75 | 80 | 50-30 | 46.8% |
| 62 | us:s3z | 1485 +- 75 | 80 | 50-30 | 46.8% |
| 63 | us:s6g | 1485 +- 182 | 12 | 8-4 | 46.8% |
| 64 | us:s3v | 1484 +- 75 | 80 | 54-26 | 46.7% |
| 65 | us:s3w | 1477 +- 75 | 80 | 52-28 | 46.3% |
| 66 | us:s3r | 1477 +- 75 | 80 | 46-34 | 46.3% |
| 67 | us:s3s | 1477 +- 75 | 80 | 46-34 | 46.3% |
| 68 | us:s3p | 1473 +- 75 | 80 | 43-37 | 46.0% |
| 69 | us:s2o_id | 1472 +- 75 | 80 | 40-40 | 46.0% |
| 70 | us:s2o | 1471 +- 13 | 2868 | 1214-1654 | 45.9% |
| 71 | us:s3h | 1469 +- 54 | 188 | 100-88 | 45.8% |
| 72 | amcsz_fighterrats | 1463 +- 58 | 180 | 89-91 | 45.3% |
| 73 | CS50Crab_protoSaberV7 | 1462 +- 57 | 189 | 93-96 | 45.3% |
| 74 | outercloudstudio_v_1_18 | 1462 +- 50 | 237 | 98-139 | 45.2% |
| 75 | us:s3x | 1460 +- 75 | 80 | 48-32 | 45.3% |
| 76 | CS50Crab_rapierV1 | 1458 +- 45 | 285 | 133-152 | 45.0% |
| 77 | us:s3q | 1448 +- 75 | 80 | 35-45 | 44.5% |
| 78 | us:s3n | 1446 +- 75 | 80 | 29-51 | 44.4% |
| 79 | us:s3k | 1445 +- 75 | 80 | 29-51 | 44.3% |
| 80 | us:s3m | 1445 +- 75 | 80 | 30-50 | 44.3% |
| 81 | us:s3i | 1444 +- 75 | 80 | 27-53 | 44.2% |
| 82 | us:s3l | 1440 +- 76 | 80 | 27-53 | 44.0% |
| 83 | us:s3e | 1435 +- 76 | 80 | 29-51 | 43.7% |
| 84 | us:s3g | 1435 +- 76 | 80 | 30-50 | 43.7% |
| 85 | us:arush | 1429 +- 78 | 80 | 29-51 | 43.3% |
| 86 | bc26vibe_bot | 1426 +- 45 | 288 | 139-149 | 43.0% |
| 87 | PixelRifts_mushakraj_mk3 | 1408 +- 56 | 207 | 98-109 | 41.9% |
| 88 | us:ratbot | 1375 +- 69 | 154 | 107-47 | 39.9% |
| 89 | us:s0u | 1350 +- 126 | 37 | 13-24 | 38.4% |
| 90 | us:s0m | 1337 +- 69 | 148 | 98-50 | 37.6% |
| 91 | us:s6c | 1314 +- 393 | 2 | 0-2 | 36.2% |
| 92 | XSercia_genies | 1310 +- 45 | 339 | 113-226 | 35.7% |
| 93 | us:s0e | 1288 +- 144 | 33 | 15-18 | 34.6% |
| 94 | C0derTang_v6 | 1277 +- 50 | 261 | 89-172 | 33.6% |
| 95 | Naton-Cai_ratplayer | 1242 +- 61 | 165 | 77-88 | 31.4% |
| 96 | ask8000_bumchit67 | 1237 +- 48 | 288 | 100-188 | 31.1% |
| 97 | justinottesen_alfredo | 1198 +- 54 | 207 | 93-114 | 28.7% |
| 98 | bc26-example | 1193 +- 35 | 556 | 212-344 | 28.4% |
| 99 | vincentwang60_javabot | 1164 +- 59 | 174 | 75-99 | 26.6% |
| 100 | Camleaf_mk1 | 1159 +- 59 | 174 | 74-100 | 26.3% |
| 101 | bc26-example-copy | 1128 +- 54 | 189 | 81-108 | 24.5% |
| 102 | Gavdingo_v1 | 1086 +- 52 | 213 | 89-124 | 22.1% |
| 103 | justinottesen_carbonara | 1082 +- 59 | 171 | 75-96 | 21.8% |
| 104 | battlecode-archive_result_408 | 1070 +- 58 | 177 | 78-99 | 21.2% |
| 105 | GuzziFlipFlops_Hermesv8 | 1047 +- 52 | 207 | 89-118 | 19.9% |
| 106 | j2399_weighted_micro_v9 | 1023 +- 61 | 168 | 68-100 | 18.6% |
| 107 | seru9_TOTAL_AGGRO | 958 +- 61 | 186 | 78-108 | 15.4% |
| 108 | quanticsoul4772_ratbot | 954 +- 58 | 186 | 76-110 | 15.2% |
| 109 | Victor-Ramirez-Garcia_battleca | 904 +- 63 | 189 | 71-118 | 13.0% |
| 110 | anicolao_myplayer | 685 +- 74 | 168 | 40-128 | 5.7% |
| 111 | j2399_weighted_path | 649 +- 81 | 117 | 35-82 | 4.9% |
| 112 | Krak9n_robot | 542 +- 88 | 129 | 20-109 | 2.8% |

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
| replica | +31 |
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
