# Ladder

4185 rows of progress/games.csv (2821 local games, 1364 replica matches), rated by a batch Bradley-Terry fit on the Elo scale (`tools/elolib.py`). A local game counts its cell score (`tools/score.py`: a win or loss when kings decide the game, the points share when points do), a replica match its games won. A game between one of our builds and a field bot also fits how hard its map is for us (table at the end), so builds gauntleted on different maps compare on an average map. Each pair of players counts at most 200 games, every player carries one virtual win and loss against a fixed anchor, and ratings are relative to a mean of 1500 over all 88 players. Each of our builds is its own player. Regenerate with `tools/elo.py`; do not edit.

Games against field bots are replica scrimmages only; local games are our builds against each other and the stock players. Record = games won and lost.

![ratings of our builds](elo.png)

Our builds, in attempt order (rating +- 95%; field score = expected score against each of the 51 field players, one game each):

| build | rating | rank | games | record | field score |
|---|---|---|---|---|---|
| s0e | 1302 +- 143 | 69 of 88 | 33 | 15-18 | 35.1% |
| s0m | 1344 +- 69 | 67 of 88 | 148 | 98-50 | 37.7% |
| s0u | 1355 +- 126 | 66 of 88 | 37 | 13-24 | 38.4% |
| v0d | 1645 +- 128 | 26 of 88 | 37 | 27-10 | 57.1% |
| s2o | 1488 +- 14 | 47 of 88 | 2472 | 1108-1364 | 46.8% |
| s3e | 1452 +- 76 | 60 of 88 | 80 | 29-51 | 44.5% |
| s3f | 1510 +- 75 | 36 of 88 | 80 | 46-34 | 48.2% |
| s3g | 1452 +- 76 | 61 of 88 | 80 | 30-50 | 44.5% |
| s2o_id | 1488 +- 75 | 46 of 88 | 80 | 40-40 | 46.8% |
| s3h | 1486 +- 54 | 49 of 88 | 188 | 100-88 | 46.7% |
| s3i | 1461 +- 75 | 58 of 88 | 80 | 27-53 | 45.1% |
| s3k | 1462 +- 75 | 56 of 88 | 80 | 29-51 | 45.2% |
| s3l | 1457 +- 76 | 59 of 88 | 80 | 27-53 | 44.8% |
| s3m | 1462 +- 75 | 57 of 88 | 80 | 30-50 | 45.2% |
| s3n | 1463 +- 75 | 55 of 88 | 80 | 29-51 | 45.2% |
| s3o | 1508 +- 75 | 37 of 88 | 80 | 50-30 | 48.1% |
| s3p | 1489 +- 75 | 45 of 88 | 80 | 43-37 | 46.9% |
| s3q | 1465 +- 75 | 53 of 88 | 80 | 35-45 | 45.4% |
| s3r | 1494 +- 75 | 43 of 88 | 80 | 46-34 | 47.2% |
| s3s | 1494 +- 75 | 44 of 88 | 80 | 46-34 | 47.2% |
| s3t | 1505 +- 75 | 38 of 88 | 80 | 49-31 | 47.9% |
| s3u | 1519 +- 75 | 34 of 88 | 80 | 53-27 | 48.8% |
| s3v | 1501 +- 75 | 41 of 88 | 80 | 54-26 | 47.6% |
| s3w | 1494 +- 75 | 42 of 88 | 80 | 52-28 | 47.2% |
| s3x | 1477 +- 75 | 51 of 88 | 80 | 48-32 | 46.1% |
| s3y | 1588 +- 64 | 27 of 88 | 171 | 106-65 | 53.3% |
| s3z | 1502 +- 75 | 40 of 88 | 80 | 50-30 | 47.7% |
| s4a | 1502 +- 75 | 39 of 88 | 80 | 50-30 | 47.7% |
| s4b | 1540 +- 53 | 32 of 88 | 172 | 117-55 | 50.1% |
| s4c | 1519 +- 75 | 35 of 88 | 80 | 53-27 | 48.8% |
| s4d | 1561 +- 45 | 29 of 88 | 262 | 174-88 | 51.5% |
| arush | 1445 +- 78 | 62 of 88 | 80 | 29-51 | 44.1% |
| s4e | 1542 +- 76 | 31 of 88 | 80 | 61-19 | 50.3% |
| s4f | 1557 +- 60 | 30 of 88 | 131 | 95-36 | 51.3% |
| abrk | 1526 +- 47 | 33 of 88 | 211 | 78-133 | 49.2% |
| s4g | 1587 +- 76 | 28 of 88 | 80 | 51-29 | 53.2% |
| ratbot | 1381 +- 69 | 65 of 88 | 154 | 107-47 | 40.1% |

All players:

| # | player | rating | games | record | field score |
|---:|---|---|---|---|---|
| 1 | IvanGeffner_basic51 | 2120 +- 67 | 132 | 94-38 | 89.0% |
| 2 | uravt_Version41 | 2087 +- 72 | 123 | 86-37 | 87.3% |
| 3 | uravt_Version35 | 2038 +- 62 | 138 | 82-56 | 84.5% |
| 4 | awu7_awubot_us | 2038 +- 71 | 120 | 78-42 | 84.5% |
| 5 | nzjt_TheComplexMerlin | 1992 +- 75 | 117 | 81-36 | 81.6% |
| 6 | abcdezyxwv_bestsofar | 1985 +- 66 | 183 | 134-49 | 81.2% |
| 7 | thewaxmango_PhD | 1982 +- 75 | 120 | 80-40 | 81.0% |
| 8 | quesswho_bot16 | 1975 +- 59 | 153 | 86-67 | 80.5% |
| 9 | nubbbbbb_testplayer | 1967 +- 71 | 114 | 73-41 | 80.0% |
| 10 | quesswho_quals | 1949 +- 64 | 132 | 79-53 | 78.8% |
| 11 | daannte_v52_ThisGuy | 1934 +- 62 | 141 | 76-65 | 77.8% |
| 12 | daannte_v47_Skadoosh | 1933 +- 60 | 162 | 95-67 | 77.7% |
| 13 | battlecode-archive_testplayer | 1921 +- 60 | 156 | 93-63 | 76.8% |
| 14 | awu7_awubot_quals | 1916 +- 66 | 132 | 81-51 | 76.5% |
| 15 | TeamSPAARK_SPAARK | 1898 +- 62 | 150 | 87-63 | 75.2% |
| 16 | AlexT101_usqualsbot | 1827 +- 56 | 207 | 132-75 | 70.1% |
| 17 | AlexT101_finalsbot | 1810 +- 59 | 171 | 92-79 | 68.9% |
| 18 | AdamTan12_Icarus | 1791 +- 57 | 174 | 94-80 | 67.5% |
| 19 | bman12345678_goonv23 | 1777 +- 77 | 123 | 74-49 | 66.5% |
| 20 | thewaxmango_collegejunior | 1757 +- 67 | 135 | 78-57 | 65.1% |
| 21 | AdamTan12_Finals | 1755 +- 64 | 153 | 83-70 | 64.9% |
| 22 | HugoIngelsson_YokoOno | 1743 +- 66 | 141 | 76-65 | 64.0% |
| 23 | VarunVejalla_final4_run_away10 | 1669 +- 87 | 99 | 58-41 | 58.9% |
| 24 | spsquared_TSPAARK_QUALS | 1667 +- 72 | 129 | 74-55 | 58.7% |
| 25 | HugoIngelsson_Xzibit | 1661 +- 70 | 135 | 74-61 | 58.3% |
| 26 | us:v0d | 1645 +- 128 | 37 | 27-10 | 57.1% |
| 27 | us:s3y | 1588 +- 64 | 171 | 106-65 | 53.3% |
| 28 | us:s4g | 1587 +- 76 | 80 | 51-29 | 53.2% |
| 29 | us:s4d | 1561 +- 45 | 262 | 174-88 | 51.5% |
| 30 | us:s4f | 1557 +- 60 | 131 | 95-36 | 51.3% |
| 31 | us:s4e | 1542 +- 76 | 80 | 61-19 | 50.3% |
| 32 | us:s4b | 1540 +- 53 | 172 | 117-55 | 50.1% |
| 33 | us:abrk | 1526 +- 47 | 211 | 78-133 | 49.2% |
| 34 | us:s3u | 1519 +- 75 | 80 | 53-27 | 48.8% |
| 35 | us:s4c | 1519 +- 75 | 80 | 53-27 | 48.8% |
| 36 | us:s3f | 1510 +- 75 | 80 | 46-34 | 48.2% |
| 37 | us:s3o | 1508 +- 75 | 80 | 50-30 | 48.1% |
| 38 | us:s3t | 1505 +- 75 | 80 | 49-31 | 47.9% |
| 39 | us:s4a | 1502 +- 75 | 80 | 50-30 | 47.7% |
| 40 | us:s3z | 1502 +- 75 | 80 | 50-30 | 47.7% |
| 41 | us:s3v | 1501 +- 75 | 80 | 54-26 | 47.6% |
| 42 | us:s3w | 1494 +- 75 | 80 | 52-28 | 47.2% |
| 43 | us:s3r | 1494 +- 75 | 80 | 46-34 | 47.2% |
| 44 | us:s3s | 1494 +- 75 | 80 | 46-34 | 47.2% |
| 45 | us:s3p | 1489 +- 75 | 80 | 43-37 | 46.9% |
| 46 | us:s2o_id | 1488 +- 75 | 80 | 40-40 | 46.8% |
| 47 | us:s2o | 1488 +- 14 | 2472 | 1108-1364 | 46.8% |
| 48 | outercloudstudio_v_1_18 | 1486 +- 64 | 153 | 73-80 | 46.6% |
| 49 | us:s3h | 1486 +- 54 | 188 | 100-88 | 46.7% |
| 50 | CS50Crab_rapierV1 | 1478 +- 56 | 192 | 93-99 | 46.1% |
| 51 | us:s3x | 1477 +- 75 | 80 | 48-32 | 46.1% |
| 52 | amcsz_fighterrats | 1469 +- 72 | 126 | 62-64 | 45.5% |
| 53 | us:s3q | 1465 +- 75 | 80 | 35-45 | 45.4% |
| 54 | CS50Crab_protoSaberV7 | 1463 +- 68 | 138 | 69-69 | 45.1% |
| 55 | us:s3n | 1463 +- 75 | 80 | 29-51 | 45.2% |
| 56 | us:s3k | 1462 +- 75 | 80 | 29-51 | 45.2% |
| 57 | us:s3m | 1462 +- 75 | 80 | 30-50 | 45.2% |
| 58 | us:s3i | 1461 +- 75 | 80 | 27-53 | 45.1% |
| 59 | us:s3l | 1457 +- 76 | 80 | 27-53 | 44.8% |
| 60 | us:s3e | 1452 +- 76 | 80 | 29-51 | 44.5% |
| 61 | us:s3g | 1452 +- 76 | 80 | 30-50 | 44.5% |
| 62 | us:arush | 1445 +- 78 | 80 | 29-51 | 44.1% |
| 63 | PixelRifts_mushakraj_mk3 | 1406 +- 69 | 147 | 67-80 | 41.5% |
| 64 | bc26vibe_bot | 1396 +- 57 | 183 | 85-98 | 40.8% |
| 65 | us:ratbot | 1381 +- 69 | 154 | 107-47 | 40.1% |
| 66 | us:s0u | 1355 +- 126 | 37 | 13-24 | 38.4% |
| 67 | us:s0m | 1344 +- 69 | 148 | 98-50 | 37.7% |
| 68 | XSercia_genies | 1321 +- 54 | 249 | 88-161 | 36.0% |
| 69 | us:s0e | 1302 +- 143 | 33 | 15-18 | 35.1% |
| 70 | C0derTang_v6 | 1277 +- 58 | 186 | 66-120 | 33.2% |
| 71 | Naton-Cai_ratplayer | 1253 +- 74 | 117 | 52-65 | 31.6% |
| 72 | ask8000_bumchit67 | 1236 +- 57 | 210 | 75-135 | 30.6% |
| 73 | justinottesen_alfredo | 1222 +- 63 | 162 | 70-92 | 29.7% |
| 74 | bc26-example | 1199 +- 36 | 532 | 199-333 | 28.3% |
| 75 | vincentwang60_javabot | 1172 +- 66 | 144 | 63-81 | 26.6% |
| 76 | Camleaf_mk1 | 1168 +- 72 | 129 | 53-76 | 26.3% |
| 77 | bc26-example-copy | 1126 +- 66 | 132 | 52-80 | 23.8% |
| 78 | Gavdingo_v1 | 1101 +- 61 | 162 | 64-98 | 22.4% |
| 79 | justinottesen_carbonara | 1097 +- 75 | 117 | 50-67 | 22.1% |
| 80 | battlecode-archive_result_408 | 1074 +- 67 | 135 | 51-84 | 20.8% |
| 81 | GuzziFlipFlops_Hermesv8 | 1065 +- 62 | 153 | 63-90 | 20.4% |
| 82 | j2399_weighted_micro_v9 | 1048 +- 70 | 138 | 55-83 | 19.4% |
| 83 | quanticsoul4772_ratbot | 974 +- 69 | 141 | 58-83 | 15.6% |
| 84 | seru9_TOTAL_AGGRO | 973 +- 71 | 141 | 51-90 | 15.6% |
| 85 | Victor-Ramirez-Garcia_battleca | 933 +- 73 | 150 | 53-97 | 13.7% |
| 86 | anicolao_myplayer | 729 +- 83 | 144 | 35-109 | 6.7% |
| 87 | j2399_weighted_path | 628 +- 103 | 87 | 21-66 | 4.3% |
| 88 | Krak9n_robot | 494 +- 117 | 102 | 10-92 | 2.1% |

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
| sunandmoon | +17 |
| replica | +16 |
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
