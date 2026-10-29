# Ladder

3946 rows of progress/games.csv (2646 local games, 1300 replica matches), rated by a batch Bradley-Terry fit on the Elo scale (`tools/elolib.py`). A local game counts its cell score (`tools/score.py`: a win or loss when kings decide the game, the points share when points do), a replica match its games won. A game between one of our builds and a field bot also fits how hard its map is for us (table at the end), so builds gauntleted on different maps compare on an average map. Each pair of players counts at most 200 games, every player carries one virtual win and loss against a fixed anchor, and ratings are relative to a mean of 1500 over all 87 players. Each of our builds is its own player. Regenerate with `tools/elo.py`; do not edit.

Games against field bots are replica scrimmages only; local games are our builds against each other and the stock players. Record = games won and lost.

![ratings of our builds](elo.png)

Our builds, in attempt order (rating +- 95%; field score = expected score against each of the 51 field players, one game each):

| build | rating | rank | games | record | field score |
|---|---|---|---|---|---|
| s0e | 1309 +- 143 | 68 of 87 | 33 | 15-18 | 35.3% |
| s0m | 1348 +- 69 | 66 of 87 | 148 | 98-50 | 37.8% |
| s0u | 1360 +- 126 | 65 of 87 | 37 | 13-24 | 38.5% |
| v0d | 1649 +- 128 | 26 of 87 | 37 | 27-10 | 57.5% |
| s2o | 1491 +- 14 | 48 of 87 | 2472 | 1108-1364 | 46.9% |
| s3e | 1454 +- 76 | 59 of 87 | 80 | 29-51 | 44.6% |
| s3f | 1513 +- 75 | 35 of 87 | 80 | 46-34 | 48.4% |
| s3g | 1454 +- 76 | 60 of 87 | 80 | 30-50 | 44.6% |
| s2o_id | 1491 +- 75 | 47 of 87 | 80 | 40-40 | 47.0% |
| s3h | 1491 +- 53 | 46 of 87 | 188 | 100-88 | 47.0% |
| s3i | 1464 +- 75 | 57 of 87 | 80 | 27-53 | 45.2% |
| s3k | 1465 +- 75 | 55 of 87 | 80 | 29-51 | 45.2% |
| s3l | 1459 +- 76 | 58 of 87 | 80 | 27-53 | 44.9% |
| s3m | 1465 +- 75 | 56 of 87 | 80 | 30-50 | 45.2% |
| s3n | 1466 +- 75 | 53 of 87 | 80 | 29-51 | 45.3% |
| s3o | 1511 +- 75 | 36 of 87 | 80 | 50-30 | 48.2% |
| s3p | 1492 +- 75 | 45 of 87 | 80 | 43-37 | 47.0% |
| s3q | 1468 +- 75 | 52 of 87 | 80 | 35-45 | 45.5% |
| s3r | 1497 +- 75 | 42 of 87 | 80 | 46-34 | 47.3% |
| s3s | 1497 +- 75 | 43 of 87 | 80 | 46-34 | 47.3% |
| s3t | 1508 +- 75 | 37 of 87 | 80 | 49-31 | 48.0% |
| s3u | 1522 +- 75 | 33 of 87 | 80 | 53-27 | 49.0% |
| s3v | 1503 +- 75 | 40 of 87 | 80 | 54-26 | 47.8% |
| s3w | 1497 +- 75 | 41 of 87 | 80 | 52-28 | 47.3% |
| s3x | 1480 +- 75 | 50 of 87 | 80 | 48-32 | 46.2% |
| s3y | 1593 +- 64 | 27 of 87 | 171 | 106-65 | 53.7% |
| s3z | 1505 +- 75 | 39 of 87 | 80 | 50-30 | 47.8% |
| s4a | 1505 +- 75 | 38 of 87 | 80 | 50-30 | 47.8% |
| s4b | 1543 +- 53 | 31 of 87 | 172 | 117-55 | 50.4% |
| s4c | 1522 +- 75 | 34 of 87 | 80 | 53-27 | 48.9% |
| s4d | 1570 +- 56 | 28 of 87 | 167 | 114-53 | 52.2% |
| arush | 1448 +- 78 | 61 of 87 | 80 | 29-51 | 44.2% |
| s4e | 1545 +- 76 | 29 of 87 | 80 | 61-19 | 50.5% |
| s4f | 1545 +- 76 | 30 of 87 | 80 | 61-19 | 50.5% |
| abrk | 1528 +- 111 | 32 of 87 | 36 | 16-20 | 49.4% |
| ratbot | 1386 +- 69 | 64 of 87 | 154 | 107-47 | 40.2% |

All players:

| # | player | rating | games | record | field score |
|---:|---|---|---|---|---|
| 1 | IvanGeffner_basic51 | 2114 +- 68 | 129 | 92-37 | 89.0% |
| 2 | uravt_Version41 | 2075 +- 72 | 120 | 83-37 | 87.0% |
| 3 | awu7_awubot_us | 2039 +- 73 | 117 | 77-40 | 84.9% |
| 4 | uravt_Version35 | 2032 +- 62 | 135 | 80-55 | 84.5% |
| 5 | abcdezyxwv_bestsofar | 1985 +- 68 | 171 | 127-44 | 81.5% |
| 6 | quesswho_bot16 | 1979 +- 62 | 144 | 83-61 | 81.1% |
| 7 | thewaxmango_PhD | 1974 +- 81 | 108 | 74-34 | 80.9% |
| 8 | nubbbbbb_testplayer | 1962 +- 75 | 105 | 69-36 | 80.0% |
| 9 | nzjt_TheComplexMerlin | 1955 +- 78 | 108 | 73-35 | 79.6% |
| 10 | quesswho_quals | 1943 +- 68 | 120 | 72-48 | 78.8% |
| 11 | daannte_v52_ThisGuy | 1938 +- 62 | 138 | 76-62 | 78.4% |
| 12 | daannte_v47_Skadoosh | 1920 +- 62 | 156 | 91-65 | 77.2% |
| 13 | battlecode-archive_testplayer | 1919 +- 63 | 144 | 88-56 | 77.1% |
| 14 | awu7_awubot_quals | 1901 +- 68 | 126 | 77-49 | 75.8% |
| 15 | TeamSPAARK_SPAARK | 1889 +- 62 | 147 | 86-61 | 75.0% |
| 16 | AlexT101_usqualsbot | 1809 +- 57 | 195 | 121-74 | 69.2% |
| 17 | AlexT101_finalsbot | 1800 +- 62 | 156 | 84-72 | 68.5% |
| 18 | bman12345678_goonv23 | 1796 +- 85 | 108 | 69-39 | 68.2% |
| 19 | AdamTan12_Icarus | 1783 +- 58 | 171 | 92-79 | 67.3% |
| 20 | thewaxmango_collegejunior | 1750 +- 69 | 126 | 73-53 | 64.9% |
| 21 | HugoIngelsson_YokoOno | 1750 +- 68 | 135 | 75-60 | 64.8% |
| 22 | AdamTan12_Finals | 1739 +- 67 | 144 | 77-67 | 64.0% |
| 23 | VarunVejalla_final4_run_away10 | 1664 +- 87 | 99 | 58-41 | 58.7% |
| 24 | spsquared_TSPAARK_QUALS | 1657 +- 77 | 114 | 64-50 | 58.2% |
| 25 | HugoIngelsson_Xzibit | 1656 +- 69 | 135 | 74-61 | 58.1% |
| 26 | us:v0d | 1649 +- 128 | 37 | 27-10 | 57.5% |
| 27 | us:s3y | 1593 +- 64 | 171 | 106-65 | 53.7% |
| 28 | us:s4d | 1570 +- 56 | 167 | 114-53 | 52.2% |
| 29 | us:s4e | 1545 +- 76 | 80 | 61-19 | 50.5% |
| 30 | us:s4f | 1545 +- 76 | 80 | 61-19 | 50.5% |
| 31 | us:s4b | 1543 +- 53 | 172 | 117-55 | 50.4% |
| 32 | us:abrk | 1528 +- 111 | 36 | 16-20 | 49.4% |
| 33 | us:s3u | 1522 +- 75 | 80 | 53-27 | 49.0% |
| 34 | us:s4c | 1522 +- 75 | 80 | 53-27 | 48.9% |
| 35 | us:s3f | 1513 +- 75 | 80 | 46-34 | 48.4% |
| 36 | us:s3o | 1511 +- 75 | 80 | 50-30 | 48.2% |
| 37 | us:s3t | 1508 +- 75 | 80 | 49-31 | 48.0% |
| 38 | us:s4a | 1505 +- 75 | 80 | 50-30 | 47.8% |
| 39 | us:s3z | 1505 +- 75 | 80 | 50-30 | 47.8% |
| 40 | us:s3v | 1503 +- 75 | 80 | 54-26 | 47.8% |
| 41 | us:s3w | 1497 +- 75 | 80 | 52-28 | 47.3% |
| 42 | us:s3r | 1497 +- 75 | 80 | 46-34 | 47.3% |
| 43 | us:s3s | 1497 +- 75 | 80 | 46-34 | 47.3% |
| 44 | outercloudstudio_v_1_18 | 1495 +- 65 | 147 | 72-75 | 47.2% |
| 45 | us:s3p | 1492 +- 75 | 80 | 43-37 | 47.0% |
| 46 | us:s3h | 1491 +- 53 | 188 | 100-88 | 47.0% |
| 47 | us:s2o_id | 1491 +- 75 | 80 | 40-40 | 47.0% |
| 48 | us:s2o | 1491 +- 14 | 2472 | 1108-1364 | 46.9% |
| 49 | CS50Crab_rapierV1 | 1482 +- 59 | 174 | 87-87 | 46.3% |
| 50 | us:s3x | 1480 +- 75 | 80 | 48-32 | 46.2% |
| 51 | CS50Crab_protoSaberV7 | 1469 +- 71 | 129 | 64-65 | 45.4% |
| 52 | us:s3q | 1468 +- 75 | 80 | 35-45 | 45.5% |
| 53 | us:s3n | 1466 +- 75 | 80 | 29-51 | 45.3% |
| 54 | amcsz_fighterrats | 1465 +- 75 | 117 | 56-61 | 45.2% |
| 55 | us:s3k | 1465 +- 75 | 80 | 29-51 | 45.2% |
| 56 | us:s3m | 1465 +- 75 | 80 | 30-50 | 45.2% |
| 57 | us:s3i | 1464 +- 75 | 80 | 27-53 | 45.2% |
| 58 | us:s3l | 1459 +- 76 | 80 | 27-53 | 44.9% |
| 59 | us:s3e | 1454 +- 76 | 80 | 29-51 | 44.6% |
| 60 | us:s3g | 1454 +- 76 | 80 | 30-50 | 44.6% |
| 61 | us:arush | 1448 +- 78 | 80 | 29-51 | 44.2% |
| 62 | PixelRifts_mushakraj_mk3 | 1424 +- 72 | 135 | 63-72 | 42.5% |
| 63 | bc26vibe_bot | 1398 +- 58 | 177 | 83-94 | 40.8% |
| 64 | us:ratbot | 1386 +- 69 | 154 | 107-47 | 40.2% |
| 65 | us:s0u | 1360 +- 126 | 37 | 13-24 | 38.5% |
| 66 | us:s0m | 1348 +- 69 | 148 | 98-50 | 37.8% |
| 67 | XSercia_genies | 1325 +- 55 | 237 | 85-152 | 36.1% |
| 68 | us:s0e | 1309 +- 143 | 33 | 15-18 | 35.3% |
| 69 | C0derTang_v6 | 1293 +- 60 | 174 | 64-110 | 34.0% |
| 70 | Naton-Cai_ratplayer | 1252 +- 75 | 114 | 49-65 | 31.3% |
| 71 | ask8000_bumchit67 | 1243 +- 59 | 195 | 72-123 | 30.8% |
| 72 | justinottesen_alfredo | 1222 +- 65 | 156 | 66-90 | 29.4% |
| 73 | bc26-example | 1204 +- 36 | 532 | 199-333 | 28.3% |
| 74 | Camleaf_mk1 | 1178 +- 73 | 126 | 52-74 | 26.7% |
| 75 | vincentwang60_javabot | 1178 +- 66 | 144 | 63-81 | 26.7% |
| 76 | bc26-example-copy | 1131 +- 66 | 132 | 52-80 | 23.8% |
| 77 | Gavdingo_v1 | 1107 +- 62 | 159 | 63-96 | 22.4% |
| 78 | justinottesen_carbonara | 1095 +- 76 | 114 | 48-66 | 21.7% |
| 79 | battlecode-archive_result_408 | 1083 +- 68 | 132 | 50-82 | 21.1% |
| 80 | GuzziFlipFlops_Hermesv8 | 1070 +- 64 | 141 | 57-84 | 20.4% |
| 81 | j2399_weighted_micro_v9 | 1056 +- 72 | 132 | 52-80 | 19.6% |
| 82 | quanticsoul4772_ratbot | 981 +- 70 | 138 | 57-81 | 15.7% |
| 83 | seru9_TOTAL_AGGRO | 979 +- 71 | 141 | 51-90 | 15.6% |
| 84 | Victor-Ramirez-Garcia_battleca | 938 +- 74 | 147 | 52-95 | 13.7% |
| 85 | anicolao_myplayer | 734 +- 83 | 144 | 35-109 | 6.7% |
| 86 | j2399_weighted_path | 633 +- 103 | 87 | 21-66 | 4.3% |
| 87 | Krak9n_robot | 500 +- 117 | 102 | 10-92 | 2.1% |

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
| sunandmoon | +17 |
| replica | +13 |
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
| Hike | -460 |
| RUN | -460 |
| clockwisdom | -618 |
| fullofsecrets | -618 |
