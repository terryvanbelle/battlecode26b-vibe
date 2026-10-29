# Ladder

5024 rows of progress/games.csv (3414 local games, 1610 replica matches), rated by a batch Bradley-Terry fit on the Elo scale (`tools/elolib.py`). A local game counts its cell score (`tools/score.py`: a win or loss when kings decide the game, the points share when points do), a replica match its games won. A game between one of our builds and a field bot also fits how hard its map is for us (table at the end), so builds gauntleted on different maps compare on an average map. Each pair of players counts at most 200 games, every player carries one virtual win and loss against a fixed anchor, and ratings are relative to a mean of 1500 over all 96 players. Each of our builds is its own player. Regenerate with `tools/elo.py`; do not edit.

Games against field bots are replica scrimmages only; local games are our builds against each other and the stock players. Record = games won and lost.

![ratings of our builds](elo.png)

Our builds, in attempt order (rating +- 95%; field score = expected score against each of the 51 field players, one game each):

| build | rating | rank | games | record | field score |
|---|---|---|---|---|---|
| s0e | 1287 +- 143 | 77 of 96 | 33 | 15-18 | 34.3% |
| s0m | 1343 +- 69 | 75 of 96 | 148 | 98-50 | 37.8% |
| s0u | 1355 +- 126 | 74 of 96 | 37 | 13-24 | 38.6% |
| v0d | 1645 +- 128 | 25 of 96 | 37 | 27-10 | 57.1% |
| s2o | 1475 +- 13 | 56 of 96 | 2700 | 1168-1532 | 46.1% |
| s3e | 1439 +- 76 | 68 of 96 | 80 | 29-51 | 43.8% |
| s3f | 1497 +- 75 | 44 of 96 | 80 | 46-34 | 47.5% |
| s3g | 1439 +- 76 | 69 of 96 | 80 | 30-50 | 43.8% |
| s2o_id | 1475 +- 75 | 55 of 96 | 80 | 40-40 | 46.1% |
| s3h | 1471 +- 54 | 58 of 96 | 188 | 100-88 | 45.9% |
| s3i | 1448 +- 75 | 66 of 96 | 80 | 27-53 | 44.4% |
| s3k | 1449 +- 75 | 64 of 96 | 80 | 29-51 | 44.5% |
| s3l | 1444 +- 76 | 67 of 96 | 80 | 27-53 | 44.1% |
| s3m | 1449 +- 75 | 65 of 96 | 80 | 30-50 | 44.5% |
| s3n | 1450 +- 75 | 63 of 96 | 80 | 29-51 | 44.5% |
| s3o | 1495 +- 75 | 45 of 96 | 80 | 50-30 | 47.4% |
| s3p | 1477 +- 75 | 54 of 96 | 80 | 43-37 | 46.2% |
| s3q | 1452 +- 75 | 62 of 96 | 80 | 35-45 | 44.7% |
| s3r | 1481 +- 75 | 52 of 96 | 80 | 46-34 | 46.5% |
| s3s | 1481 +- 75 | 53 of 96 | 80 | 46-34 | 46.5% |
| s3t | 1492 +- 75 | 46 of 96 | 80 | 49-31 | 47.2% |
| s3u | 1506 +- 76 | 41 of 96 | 80 | 53-27 | 48.1% |
| s3v | 1488 +- 75 | 49 of 96 | 80 | 54-26 | 46.9% |
| s3w | 1481 +- 75 | 51 of 96 | 80 | 52-28 | 46.5% |
| s3x | 1464 +- 75 | 61 of 96 | 80 | 48-32 | 45.4% |
| s3y | 1575 +- 64 | 31 of 96 | 174 | 108-66 | 52.5% |
| s3z | 1489 +- 75 | 48 of 96 | 80 | 50-30 | 47.0% |
| s4a | 1489 +- 75 | 47 of 96 | 80 | 50-30 | 47.0% |
| s4b | 1527 +- 53 | 38 of 96 | 172 | 117-55 | 49.4% |
| s4c | 1506 +- 75 | 42 of 96 | 80 | 53-27 | 48.1% |
| s4d | 1556 +- 42 | 34 of 96 | 324 | 211-113 | 51.3% |
| arush | 1432 +- 78 | 70 of 96 | 80 | 29-51 | 43.4% |
| s4e | 1529 +- 76 | 37 of 96 | 80 | 61-19 | 49.5% |
| s4f | 1547 +- 54 | 35 of 96 | 160 | 110-50 | 50.7% |
| abrk | 1531 +- 29 | 36 of 96 | 576 | 211-365 | 49.7% |
| s4g | 1592 +- 76 | 29 of 96 | 80 | 51-29 | 53.7% |
| s4h | 1570 +- 76 | 32 of 96 | 80 | 52-28 | 52.2% |
| s4i | 1579 +- 64 | 30 of 96 | 134 | 86-48 | 52.8% |
| s4j | 1597 +- 65 | 28 of 96 | 131 | 87-44 | 54.0% |
| s4m | 1633 +- 226 | 27 of 96 | 8 | 5-3 | 56.3% |
| s4n | 1569 +- 76 | 33 of 96 | 80 | 52-28 | 52.1% |
| s4q | 1512 +- 76 | 39 of 96 | 80 | 61-19 | 48.4% |
| s4r | 1512 +- 76 | 40 of 96 | 80 | 61-19 | 48.4% |
| s4t | 1503 +- 66 | 43 of 96 | 122 | 74-48 | 47.9% |
| ratbot | 1381 +- 69 | 73 of 96 | 154 | 107-47 | 40.2% |

All players:

| # | player | rating | games | record | field score |
|---:|---|---|---|---|---|
| 1 | IvanGeffner_basic51 | 2123 +- 63 | 150 | 106-44 | 89.1% |
| 2 | uravt_Version41 | 2104 +- 67 | 141 | 100-41 | 88.2% |
| 3 | uravt_Version35 | 2030 +- 59 | 150 | 89-61 | 84.1% |
| 4 | awu7_awubot_us | 2011 +- 64 | 141 | 86-55 | 82.9% |
| 5 | abcdezyxwv_bestsofar | 1998 +- 59 | 228 | 167-61 | 82.0% |
| 6 | nzjt_TheComplexMerlin | 1992 +- 72 | 126 | 85-41 | 81.7% |
| 7 | thewaxmango_PhD | 1973 +- 67 | 141 | 89-52 | 80.4% |
| 8 | quesswho_bot16 | 1971 +- 56 | 168 | 93-75 | 80.3% |
| 9 | nubbbbbb_testplayer | 1957 +- 66 | 129 | 79-50 | 79.3% |
| 10 | quesswho_quals | 1956 +- 57 | 162 | 95-67 | 79.3% |
| 11 | daannte_v52_ThisGuy | 1949 +- 58 | 159 | 89-70 | 78.8% |
| 12 | battlecode-archive_testplayer | 1941 +- 55 | 183 | 110-73 | 78.3% |
| 13 | awu7_awubot_quals | 1922 +- 64 | 141 | 86-55 | 77.0% |
| 14 | daannte_v47_Skadoosh | 1910 +- 56 | 183 | 101-82 | 76.2% |
| 15 | TeamSPAARK_SPAARK | 1890 +- 59 | 165 | 93-72 | 74.7% |
| 16 | AlexT101_usqualsbot | 1822 +- 51 | 249 | 159-90 | 69.8% |
| 17 | AlexT101_finalsbot | 1809 +- 56 | 186 | 99-87 | 68.9% |
| 18 | bman12345678_goonv23 | 1785 +- 69 | 144 | 87-57 | 67.1% |
| 19 | thewaxmango_collegejunior | 1771 +- 63 | 150 | 88-62 | 66.2% |
| 20 | AdamTan12_Finals | 1766 +- 62 | 165 | 89-76 | 65.8% |
| 21 | AdamTan12_Icarus | 1759 +- 51 | 219 | 117-102 | 65.3% |
| 22 | HugoIngelsson_YokoOno | 1744 +- 60 | 168 | 92-76 | 64.2% |
| 23 | VarunVejalla_final4_run_away10 | 1686 +- 70 | 135 | 74-61 | 60.1% |
| 24 | HugoIngelsson_Xzibit | 1665 +- 62 | 162 | 89-73 | 58.7% |
| 25 | us:v0d | 1645 +- 128 | 37 | 27-10 | 57.1% |
| 26 | spsquared_TSPAARK_QUALS | 1638 +- 66 | 150 | 82-68 | 56.8% |
| 27 | us:s4m | 1633 +- 226 | 8 | 5-3 | 56.3% |
| 28 | us:s4j | 1597 +- 65 | 131 | 87-44 | 54.0% |
| 29 | us:s4g | 1592 +- 76 | 80 | 51-29 | 53.7% |
| 30 | us:s4i | 1579 +- 64 | 134 | 86-48 | 52.8% |
| 31 | us:s3y | 1575 +- 64 | 174 | 108-66 | 52.5% |
| 32 | us:s4h | 1570 +- 76 | 80 | 52-28 | 52.2% |
| 33 | us:s4n | 1569 +- 76 | 80 | 52-28 | 52.1% |
| 34 | us:s4d | 1556 +- 42 | 324 | 211-113 | 51.3% |
| 35 | us:s4f | 1547 +- 54 | 160 | 110-50 | 50.7% |
| 36 | us:abrk | 1531 +- 29 | 576 | 211-365 | 49.7% |
| 37 | us:s4e | 1529 +- 76 | 80 | 61-19 | 49.5% |
| 38 | us:s4b | 1527 +- 53 | 172 | 117-55 | 49.4% |
| 39 | us:s4q | 1512 +- 76 | 80 | 61-19 | 48.4% |
| 40 | us:s4r | 1512 +- 76 | 80 | 61-19 | 48.4% |
| 41 | us:s3u | 1506 +- 76 | 80 | 53-27 | 48.1% |
| 42 | us:s4c | 1506 +- 75 | 80 | 53-27 | 48.1% |
| 43 | us:s4t | 1503 +- 66 | 122 | 74-48 | 47.9% |
| 44 | us:s3f | 1497 +- 75 | 80 | 46-34 | 47.5% |
| 45 | us:s3o | 1495 +- 75 | 80 | 50-30 | 47.4% |
| 46 | us:s3t | 1492 +- 75 | 80 | 49-31 | 47.2% |
| 47 | us:s4a | 1489 +- 75 | 80 | 50-30 | 47.0% |
| 48 | us:s3z | 1489 +- 75 | 80 | 50-30 | 47.0% |
| 49 | us:s3v | 1488 +- 75 | 80 | 54-26 | 46.9% |
| 50 | outercloudstudio_v_1_18 | 1484 +- 57 | 189 | 85-104 | 46.6% |
| 51 | us:s3w | 1481 +- 75 | 80 | 52-28 | 46.5% |
| 52 | us:s3r | 1481 +- 75 | 80 | 46-34 | 46.5% |
| 53 | us:s3s | 1481 +- 75 | 80 | 46-34 | 46.5% |
| 54 | us:s3p | 1477 +- 75 | 80 | 43-37 | 46.2% |
| 55 | us:s2o_id | 1475 +- 75 | 80 | 40-40 | 46.1% |
| 56 | us:s2o | 1475 +- 13 | 2700 | 1168-1532 | 46.1% |
| 57 | CS50Crab_protoSaberV7 | 1471 +- 60 | 171 | 87-84 | 45.8% |
| 58 | us:s3h | 1471 +- 54 | 188 | 100-88 | 45.9% |
| 59 | amcsz_fighterrats | 1470 +- 63 | 156 | 78-78 | 45.7% |
| 60 | CS50Crab_rapierV1 | 1469 +- 50 | 240 | 114-126 | 45.6% |
| 61 | us:s3x | 1464 +- 75 | 80 | 48-32 | 45.4% |
| 62 | us:s3q | 1452 +- 75 | 80 | 35-45 | 44.7% |
| 63 | us:s3n | 1450 +- 75 | 80 | 29-51 | 44.5% |
| 64 | us:s3k | 1449 +- 75 | 80 | 29-51 | 44.5% |
| 65 | us:s3m | 1449 +- 75 | 80 | 30-50 | 44.5% |
| 66 | us:s3i | 1448 +- 75 | 80 | 27-53 | 44.4% |
| 67 | us:s3l | 1444 +- 76 | 80 | 27-53 | 44.1% |
| 68 | us:s3e | 1439 +- 76 | 80 | 29-51 | 43.8% |
| 69 | us:s3g | 1439 +- 76 | 80 | 30-50 | 43.8% |
| 70 | us:arush | 1432 +- 78 | 80 | 29-51 | 43.4% |
| 71 | bc26vibe_bot | 1404 +- 50 | 234 | 107-127 | 41.5% |
| 72 | PixelRifts_mushakraj_mk3 | 1399 +- 60 | 183 | 84-99 | 41.2% |
| 73 | us:ratbot | 1381 +- 69 | 154 | 107-47 | 40.2% |
| 74 | us:s0u | 1355 +- 126 | 37 | 13-24 | 38.6% |
| 75 | us:s0m | 1343 +- 69 | 148 | 98-50 | 37.8% |
| 76 | XSercia_genies | 1311 +- 49 | 303 | 101-202 | 35.5% |
| 77 | us:s0e | 1287 +- 143 | 33 | 15-18 | 34.3% |
| 78 | C0derTang_v6 | 1281 +- 53 | 228 | 79-149 | 33.6% |
| 79 | Naton-Cai_ratplayer | 1240 +- 68 | 135 | 60-75 | 31.0% |
| 80 | ask8000_bumchit67 | 1236 +- 53 | 252 | 86-166 | 30.8% |
| 81 | justinottesen_alfredo | 1201 +- 57 | 192 | 83-109 | 28.6% |
| 82 | bc26-example | 1199 +- 36 | 538 | 203-335 | 28.5% |
| 83 | Camleaf_mk1 | 1180 +- 63 | 156 | 68-88 | 27.3% |
| 84 | vincentwang60_javabot | 1166 +- 63 | 156 | 67-89 | 26.4% |
| 85 | bc26-example-copy | 1131 +- 57 | 171 | 71-100 | 24.3% |
| 86 | justinottesen_carbonara | 1096 +- 65 | 144 | 63-81 | 22.3% |
| 87 | Gavdingo_v1 | 1095 +- 55 | 192 | 78-114 | 22.2% |
| 88 | battlecode-archive_result_408 | 1071 +- 63 | 150 | 60-90 | 20.9% |
| 89 | GuzziFlipFlops_Hermesv8 | 1060 +- 58 | 171 | 72-99 | 20.3% |
| 90 | j2399_weighted_micro_v9 | 1037 +- 67 | 147 | 59-88 | 19.0% |
| 91 | quanticsoul4772_ratbot | 976 +- 62 | 165 | 70-95 | 15.9% |
| 92 | seru9_TOTAL_AGGRO | 950 +- 65 | 168 | 64-104 | 14.7% |
| 93 | Victor-Ramirez-Garcia_battleca | 937 +- 68 | 165 | 62-103 | 14.1% |
| 94 | anicolao_myplayer | 700 +- 79 | 156 | 37-119 | 6.0% |
| 95 | j2399_weighted_path | 640 +- 91 | 99 | 28-71 | 4.6% |
| 96 | Krak9n_robot | 512 +- 102 | 114 | 14-100 | 2.4% |

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
| externalsustenance | +125 |
| jail | +125 |
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
| houseofcards | +39 |
| replica | +31 |
| sunandmoon | +17 |
| tornadoes | -23 |
| canyoudig | -28 |
| TheHeist | -54 |
| groveisbeingdesecrated | -54 |
| rose | -54 |
| where_are_you | -109 |
| 5t4rv4t10n_1337 | -146 |
| diamond | -152 |
| arrows | -192 |
| uneruesansfin | -235 |
| attack_on_cat | -252 |
| heart | -252 |
| seepastyourgreed | -252 |
| learn_your_lesson | -262 |
| popthecork | -262 |
| krotus | -274 |
| Stash | -301 |
| UncomfortablyClose | -301 |
| cheesebottles | -352 |
| mincemeat | -352 |
| averyfineline | -381 |
| t_minus_five_minutes | -400 |
| tiny | -400 |
| ultimatestarvation | -400 |
| whatsthecatdoin | -400 |
| whereisthecheese | -400 |
| Hike | -460 |
| RUN | -460 |
| clockwisdom | -618 |
| fullofsecrets | -618 |
