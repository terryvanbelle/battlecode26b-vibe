# Ladder

4651 rows of progress/games.csv (3154 local games, 1497 replica matches), rated by a batch Bradley-Terry fit on the Elo scale (`tools/elolib.py`). A local game counts its cell score (`tools/score.py`: a win or loss when kings decide the game, the points share when points do), a replica match its games won. A game between one of our builds and a field bot also fits how hard its map is for us (table at the end), so builds gauntleted on different maps compare on an average map. Each pair of players counts at most 200 games, every player carries one virtual win and loss against a fixed anchor, and ratings are relative to a mean of 1500 over all 93 players. Each of our builds is its own player. Regenerate with `tools/elo.py`; do not edit.

Games against field bots are replica scrimmages only; local games are our builds against each other and the stock players. Record = games won and lost.

![ratings of our builds](elo.png)

Our builds, in attempt order (rating +- 95%; field score = expected score against each of the 51 field players, one game each):

| build | rating | rank | games | record | field score |
|---|---|---|---|---|---|
| s0e | 1284 +- 143 | 74 of 93 | 33 | 15-18 | 34.2% |
| s0m | 1341 +- 69 | 72 of 93 | 148 | 98-50 | 37.8% |
| s0u | 1353 +- 126 | 71 of 93 | 37 | 13-24 | 38.5% |
| v0d | 1643 +- 128 | 26 of 93 | 37 | 27-10 | 56.9% |
| s2o | 1476 +- 14 | 53 of 93 | 2520 | 1119-1401 | 46.2% |
| s3e | 1440 +- 76 | 65 of 93 | 80 | 29-51 | 44.0% |
| s3f | 1499 +- 75 | 41 of 93 | 80 | 46-34 | 47.6% |
| s3g | 1440 +- 76 | 66 of 93 | 80 | 30-50 | 44.0% |
| s2o_id | 1477 +- 75 | 52 of 93 | 80 | 40-40 | 46.3% |
| s3h | 1471 +- 54 | 54 of 93 | 188 | 100-88 | 45.9% |
| s3i | 1450 +- 75 | 63 of 93 | 80 | 27-53 | 44.5% |
| s3k | 1451 +- 75 | 61 of 93 | 80 | 29-51 | 44.6% |
| s3l | 1445 +- 76 | 64 of 93 | 80 | 27-53 | 44.3% |
| s3m | 1451 +- 75 | 62 of 93 | 80 | 30-50 | 44.6% |
| s3n | 1452 +- 75 | 60 of 93 | 80 | 29-51 | 44.7% |
| s3o | 1497 +- 75 | 42 of 93 | 80 | 50-30 | 47.5% |
| s3p | 1478 +- 75 | 51 of 93 | 80 | 43-37 | 46.3% |
| s3q | 1454 +- 75 | 59 of 93 | 80 | 35-45 | 44.8% |
| s3r | 1483 +- 75 | 49 of 93 | 80 | 46-34 | 46.6% |
| s3s | 1483 +- 75 | 50 of 93 | 80 | 46-34 | 46.6% |
| s3t | 1494 +- 75 | 43 of 93 | 80 | 49-31 | 47.3% |
| s3u | 1508 +- 76 | 39 of 93 | 80 | 53-27 | 48.2% |
| s3v | 1489 +- 75 | 46 of 93 | 80 | 54-26 | 47.0% |
| s3w | 1483 +- 75 | 48 of 93 | 80 | 52-28 | 46.6% |
| s3x | 1466 +- 75 | 55 of 93 | 80 | 48-32 | 45.6% |
| s3y | 1570 +- 65 | 31 of 93 | 171 | 106-65 | 52.1% |
| s3z | 1491 +- 75 | 45 of 93 | 80 | 50-30 | 47.1% |
| s4a | 1491 +- 75 | 44 of 93 | 80 | 50-30 | 47.1% |
| s4b | 1528 +- 53 | 37 of 93 | 172 | 117-55 | 49.5% |
| s4c | 1508 +- 75 | 40 of 93 | 80 | 53-27 | 48.2% |
| s4d | 1555 +- 42 | 33 of 93 | 324 | 211-113 | 51.2% |
| arush | 1434 +- 78 | 67 of 93 | 80 | 29-51 | 43.6% |
| s4e | 1531 +- 76 | 36 of 93 | 80 | 61-19 | 49.6% |
| s4f | 1547 +- 54 | 34 of 93 | 160 | 110-50 | 50.7% |
| abrk | 1531 +- 31 | 35 of 93 | 496 | 183-313 | 49.6% |
| s4g | 1592 +- 76 | 29 of 93 | 80 | 51-29 | 53.6% |
| s4h | 1569 +- 76 | 32 of 93 | 80 | 52-28 | 52.1% |
| s4i | 1577 +- 76 | 30 of 93 | 80 | 53-27 | 52.6% |
| s4j | 1596 +- 65 | 28 of 93 | 131 | 87-44 | 53.8% |
| s4m | 1632 +- 226 | 27 of 93 | 8 | 5-3 | 56.2% |
| s4q | 1514 +- 97 | 38 of 93 | 48 | 37-11 | 48.6% |
| ratbot | 1379 +- 69 | 70 of 93 | 154 | 107-47 | 40.1% |

All players:

| # | player | rating | games | record | field score |
|---:|---|---|---|---|---|
| 1 | IvanGeffner_basic51 | 2123 +- 65 | 141 | 99-42 | 88.9% |
| 2 | uravt_Version41 | 2106 +- 67 | 138 | 97-41 | 88.1% |
| 3 | uravt_Version35 | 2033 +- 60 | 147 | 86-61 | 84.0% |
| 4 | awu7_awubot_us | 2029 +- 67 | 132 | 83-49 | 83.8% |
| 5 | abcdezyxwv_bestsofar | 2007 +- 62 | 207 | 153-54 | 82.4% |
| 6 | nzjt_TheComplexMerlin | 1992 +- 75 | 117 | 81-36 | 81.4% |
| 7 | thewaxmango_PhD | 1977 +- 69 | 135 | 86-49 | 80.5% |
| 8 | quesswho_bot16 | 1972 +- 58 | 162 | 89-73 | 80.1% |
| 9 | nubbbbbb_testplayer | 1967 +- 68 | 123 | 77-46 | 79.8% |
| 10 | quesswho_quals | 1957 +- 61 | 147 | 87-60 | 79.1% |
| 11 | daannte_v52_ThisGuy | 1950 +- 60 | 150 | 84-66 | 78.7% |
| 12 | battlecode-archive_testplayer | 1939 +- 58 | 168 | 102-66 | 77.9% |
| 13 | daannte_v47_Skadoosh | 1927 +- 59 | 168 | 96-72 | 77.1% |
| 14 | awu7_awubot_quals | 1922 +- 65 | 138 | 84-54 | 76.7% |
| 15 | TeamSPAARK_SPAARK | 1893 +- 60 | 159 | 90-69 | 74.7% |
| 16 | AlexT101_usqualsbot | 1819 +- 53 | 231 | 145-86 | 69.4% |
| 17 | AlexT101_finalsbot | 1814 +- 57 | 183 | 99-84 | 69.1% |
| 18 | bman12345678_goonv23 | 1787 +- 70 | 141 | 85-56 | 67.1% |
| 19 | AdamTan12_Finals | 1769 +- 63 | 159 | 88-71 | 65.8% |
| 20 | AdamTan12_Icarus | 1762 +- 55 | 192 | 99-93 | 65.3% |
| 21 | thewaxmango_collegejunior | 1760 +- 65 | 141 | 81-60 | 65.2% |
| 22 | HugoIngelsson_YokoOno | 1747 +- 62 | 156 | 85-71 | 64.2% |
| 23 | VarunVejalla_final4_run_away10 | 1707 +- 73 | 126 | 73-53 | 61.5% |
| 24 | HugoIngelsson_Xzibit | 1668 +- 66 | 150 | 84-66 | 58.8% |
| 25 | spsquared_TSPAARK_QUALS | 1654 +- 68 | 144 | 81-63 | 57.8% |
| 26 | us:v0d | 1643 +- 128 | 37 | 27-10 | 56.9% |
| 27 | us:s4m | 1632 +- 226 | 8 | 5-3 | 56.2% |
| 28 | us:s4j | 1596 +- 65 | 131 | 87-44 | 53.8% |
| 29 | us:s4g | 1592 +- 76 | 80 | 51-29 | 53.6% |
| 30 | us:s4i | 1577 +- 76 | 80 | 53-27 | 52.6% |
| 31 | us:s3y | 1570 +- 65 | 171 | 106-65 | 52.1% |
| 32 | us:s4h | 1569 +- 76 | 80 | 52-28 | 52.1% |
| 33 | us:s4d | 1555 +- 42 | 324 | 211-113 | 51.2% |
| 34 | us:s4f | 1547 +- 54 | 160 | 110-50 | 50.7% |
| 35 | us:abrk | 1531 +- 31 | 496 | 183-313 | 49.6% |
| 36 | us:s4e | 1531 +- 76 | 80 | 61-19 | 49.6% |
| 37 | us:s4b | 1528 +- 53 | 172 | 117-55 | 49.5% |
| 38 | us:s4q | 1514 +- 97 | 48 | 37-11 | 48.6% |
| 39 | us:s3u | 1508 +- 76 | 80 | 53-27 | 48.2% |
| 40 | us:s4c | 1508 +- 75 | 80 | 53-27 | 48.2% |
| 41 | us:s3f | 1499 +- 75 | 80 | 46-34 | 47.6% |
| 42 | us:s3o | 1497 +- 75 | 80 | 50-30 | 47.5% |
| 43 | us:s3t | 1494 +- 75 | 80 | 49-31 | 47.3% |
| 44 | us:s4a | 1491 +- 75 | 80 | 50-30 | 47.1% |
| 45 | us:s3z | 1491 +- 75 | 80 | 50-30 | 47.1% |
| 46 | us:s3v | 1489 +- 75 | 80 | 54-26 | 47.0% |
| 47 | outercloudstudio_v_1_18 | 1485 +- 59 | 174 | 81-93 | 46.7% |
| 48 | us:s3w | 1483 +- 75 | 80 | 52-28 | 46.6% |
| 49 | us:s3r | 1483 +- 75 | 80 | 46-34 | 46.6% |
| 50 | us:s3s | 1483 +- 75 | 80 | 46-34 | 46.6% |
| 51 | us:s3p | 1478 +- 75 | 80 | 43-37 | 46.3% |
| 52 | us:s2o_id | 1477 +- 75 | 80 | 40-40 | 46.3% |
| 53 | us:s2o | 1476 +- 14 | 2520 | 1119-1401 | 46.2% |
| 54 | us:s3h | 1471 +- 54 | 188 | 100-88 | 45.9% |
| 55 | us:s3x | 1466 +- 75 | 80 | 48-32 | 45.6% |
| 56 | amcsz_fighterrats | 1460 +- 66 | 147 | 73-74 | 45.1% |
| 57 | CS50Crab_protoSaberV7 | 1458 +- 64 | 153 | 75-78 | 44.9% |
| 58 | CS50Crab_rapierV1 | 1455 +- 53 | 216 | 98-118 | 44.8% |
| 59 | us:s3q | 1454 +- 75 | 80 | 35-45 | 44.8% |
| 60 | us:s3n | 1452 +- 75 | 80 | 29-51 | 44.7% |
| 61 | us:s3k | 1451 +- 75 | 80 | 29-51 | 44.6% |
| 62 | us:s3m | 1451 +- 75 | 80 | 30-50 | 44.6% |
| 63 | us:s3i | 1450 +- 75 | 80 | 27-53 | 44.5% |
| 64 | us:s3l | 1445 +- 76 | 80 | 27-53 | 44.3% |
| 65 | us:s3e | 1440 +- 76 | 80 | 29-51 | 44.0% |
| 66 | us:s3g | 1440 +- 76 | 80 | 30-50 | 44.0% |
| 67 | us:arush | 1434 +- 78 | 80 | 29-51 | 43.6% |
| 68 | PixelRifts_mushakraj_mk3 | 1411 +- 65 | 162 | 75-87 | 42.0% |
| 69 | bc26vibe_bot | 1397 +- 53 | 207 | 94-113 | 41.1% |
| 70 | us:ratbot | 1379 +- 69 | 154 | 107-47 | 40.1% |
| 71 | us:s0u | 1353 +- 126 | 37 | 13-24 | 38.5% |
| 72 | us:s0m | 1341 +- 69 | 148 | 98-50 | 37.8% |
| 73 | XSercia_genies | 1310 +- 51 | 276 | 93-183 | 35.5% |
| 74 | us:s0e | 1284 +- 143 | 33 | 15-18 | 34.2% |
| 75 | C0derTang_v6 | 1261 +- 57 | 201 | 67-134 | 32.4% |
| 76 | ask8000_bumchit67 | 1233 +- 55 | 228 | 80-148 | 30.7% |
| 77 | Naton-Cai_ratplayer | 1232 +- 72 | 123 | 54-69 | 30.6% |
| 78 | justinottesen_alfredo | 1216 +- 60 | 174 | 76-98 | 29.6% |
| 79 | bc26-example | 1197 +- 36 | 538 | 203-335 | 28.4% |
| 80 | Camleaf_mk1 | 1176 +- 68 | 141 | 61-80 | 27.1% |
| 81 | vincentwang60_javabot | 1166 +- 63 | 153 | 67-86 | 26.5% |
| 82 | bc26-example-copy | 1126 +- 61 | 150 | 61-89 | 24.1% |
| 83 | Gavdingo_v1 | 1097 +- 56 | 186 | 76-110 | 22.4% |
| 84 | justinottesen_carbonara | 1094 +- 66 | 141 | 62-79 | 22.2% |
| 85 | battlecode-archive_result_408 | 1066 +- 64 | 144 | 56-88 | 20.7% |
| 86 | GuzziFlipFlops_Hermesv8 | 1059 +- 58 | 168 | 70-98 | 20.2% |
| 87 | j2399_weighted_micro_v9 | 1042 +- 68 | 144 | 58-86 | 19.3% |
| 88 | seru9_TOTAL_AGGRO | 967 +- 69 | 150 | 57-93 | 15.5% |
| 89 | quanticsoul4772_ratbot | 967 +- 66 | 153 | 63-90 | 15.5% |
| 90 | Victor-Ramirez-Garcia_battleca | 930 +- 70 | 159 | 58-101 | 13.8% |
| 91 | anicolao_myplayer | 701 +- 78 | 156 | 37-119 | 6.0% |
| 92 | j2399_weighted_path | 644 +- 93 | 96 | 26-70 | 4.7% |
| 93 | Krak9n_robot | 517 +- 105 | 108 | 13-95 | 2.4% |

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
| replica | +28 |
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
| Hike | -460 |
| RUN | -460 |
| clockwisdom | -618 |
| fullofsecrets | -618 |
