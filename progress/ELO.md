# Ladder

4916 rows of progress/games.csv (3343 local games, 1573 replica matches), rated by a batch Bradley-Terry fit on the Elo scale (`tools/elolib.py`). A local game counts its cell score (`tools/score.py`: a win or loss when kings decide the game, the points share when points do), a replica match its games won. A game between one of our builds and a field bot also fits how hard its map is for us (table at the end), so builds gauntleted on different maps compare on an average map. Each pair of players counts at most 200 games, every player carries one virtual win and loss against a fixed anchor, and ratings are relative to a mean of 1500 over all 95 players. Each of our builds is its own player. Regenerate with `tools/elo.py`; do not edit.

Games against field bots are replica scrimmages only; local games are our builds against each other and the stock players. Record = games won and lost.

![ratings of our builds](elo.png)

Our builds, in attempt order (rating +- 95%; field score = expected score against each of the 51 field players, one game each):

| build | rating | rank | games | record | field score |
|---|---|---|---|---|---|
| s0e | 1290 +- 143 | 76 of 95 | 33 | 15-18 | 34.4% |
| s0m | 1346 +- 69 | 74 of 95 | 148 | 98-50 | 38.0% |
| s0u | 1358 +- 126 | 73 of 95 | 37 | 13-24 | 38.7% |
| v0d | 1647 +- 128 | 26 of 95 | 37 | 27-10 | 57.3% |
| s2o | 1476 +- 14 | 55 of 95 | 2632 | 1146-1486 | 46.2% |
| s3e | 1440 +- 76 | 67 of 95 | 80 | 29-51 | 43.9% |
| s3f | 1499 +- 75 | 43 of 95 | 80 | 46-34 | 47.6% |
| s3g | 1440 +- 76 | 68 of 95 | 80 | 30-50 | 43.9% |
| s2o_id | 1477 +- 75 | 54 of 95 | 80 | 40-40 | 46.2% |
| s3h | 1472 +- 54 | 56 of 95 | 188 | 100-88 | 45.9% |
| s3i | 1449 +- 75 | 65 of 95 | 80 | 27-53 | 44.5% |
| s3k | 1450 +- 75 | 63 of 95 | 80 | 29-51 | 44.5% |
| s3l | 1445 +- 76 | 66 of 95 | 80 | 27-53 | 44.2% |
| s3m | 1450 +- 75 | 64 of 95 | 80 | 30-50 | 44.5% |
| s3n | 1451 +- 75 | 62 of 95 | 80 | 29-51 | 44.6% |
| s3o | 1497 +- 75 | 44 of 95 | 80 | 50-30 | 47.5% |
| s3p | 1478 +- 75 | 53 of 95 | 80 | 43-37 | 46.3% |
| s3q | 1454 +- 75 | 61 of 95 | 80 | 35-45 | 44.8% |
| s3r | 1483 +- 75 | 51 of 95 | 80 | 46-34 | 46.6% |
| s3s | 1483 +- 75 | 52 of 95 | 80 | 46-34 | 46.6% |
| s3t | 1494 +- 75 | 45 of 95 | 80 | 49-31 | 47.3% |
| s3u | 1508 +- 76 | 41 of 95 | 80 | 53-27 | 48.2% |
| s3v | 1489 +- 75 | 48 of 95 | 80 | 54-26 | 47.0% |
| s3w | 1483 +- 75 | 50 of 95 | 80 | 52-28 | 46.6% |
| s3x | 1466 +- 75 | 58 of 95 | 80 | 48-32 | 45.5% |
| s3y | 1577 +- 64 | 30 of 95 | 174 | 108-66 | 52.6% |
| s3z | 1490 +- 75 | 47 of 95 | 80 | 50-30 | 47.1% |
| s4a | 1491 +- 75 | 46 of 95 | 80 | 50-30 | 47.1% |
| s4b | 1529 +- 53 | 37 of 95 | 172 | 117-55 | 49.5% |
| s4c | 1507 +- 75 | 42 of 95 | 80 | 53-27 | 48.2% |
| s4d | 1554 +- 42 | 34 of 95 | 324 | 211-113 | 51.2% |
| arush | 1434 +- 78 | 69 of 95 | 80 | 29-51 | 43.5% |
| s4e | 1530 +- 76 | 36 of 95 | 80 | 61-19 | 49.6% |
| s4f | 1544 +- 54 | 35 of 95 | 160 | 110-50 | 50.5% |
| abrk | 1524 +- 29 | 38 of 95 | 573 | 210-363 | 49.2% |
| s4g | 1585 +- 76 | 29 of 95 | 80 | 51-29 | 53.2% |
| s4h | 1563 +- 76 | 32 of 95 | 80 | 52-28 | 51.7% |
| s4i | 1559 +- 63 | 33 of 95 | 134 | 84-50 | 51.4% |
| s4j | 1593 +- 65 | 28 of 95 | 131 | 87-44 | 53.7% |
| s4m | 1627 +- 226 | 27 of 95 | 8 | 5-3 | 55.9% |
| s4n | 1564 +- 77 | 31 of 95 | 77 | 50-27 | 51.8% |
| s4q | 1513 +- 76 | 39 of 95 | 80 | 61-19 | 48.5% |
| s4r | 1513 +- 76 | 40 of 95 | 80 | 61-19 | 48.5% |
| ratbot | 1383 +- 69 | 72 of 95 | 154 | 107-47 | 40.3% |

All players:

| # | player | rating | games | record | field score |
|---:|---|---|---|---|---|
| 1 | IvanGeffner_basic51 | 2121 +- 63 | 150 | 106-44 | 89.1% |
| 2 | uravt_Version41 | 2099 +- 67 | 138 | 97-41 | 88.0% |
| 3 | uravt_Version35 | 2024 +- 60 | 147 | 86-61 | 83.7% |
| 4 | awu7_awubot_us | 2007 +- 64 | 141 | 86-55 | 82.7% |
| 5 | abcdezyxwv_bestsofar | 1998 +- 59 | 222 | 162-60 | 82.1% |
| 6 | nzjt_TheComplexMerlin | 1991 +- 71 | 126 | 85-41 | 81.7% |
| 7 | thewaxmango_PhD | 1971 +- 67 | 141 | 89-52 | 80.4% |
| 8 | quesswho_bot16 | 1968 +- 56 | 168 | 93-75 | 80.2% |
| 9 | quesswho_quals | 1957 +- 58 | 159 | 94-65 | 79.4% |
| 10 | nubbbbbb_testplayer | 1955 +- 66 | 129 | 79-50 | 79.3% |
| 11 | battlecode-archive_testplayer | 1938 +- 56 | 180 | 108-72 | 78.1% |
| 12 | daannte_v52_ThisGuy | 1936 +- 58 | 159 | 87-72 | 78.0% |
| 13 | awu7_awubot_quals | 1922 +- 64 | 141 | 86-55 | 77.0% |
| 14 | daannte_v47_Skadoosh | 1916 +- 57 | 180 | 101-79 | 76.6% |
| 15 | TeamSPAARK_SPAARK | 1887 +- 60 | 159 | 90-69 | 74.5% |
| 16 | AlexT101_usqualsbot | 1821 +- 51 | 243 | 154-89 | 69.8% |
| 17 | AlexT101_finalsbot | 1816 +- 56 | 186 | 101-85 | 69.5% |
| 18 | bman12345678_goonv23 | 1785 +- 70 | 144 | 88-56 | 67.2% |
| 19 | thewaxmango_collegejunior | 1773 +- 62 | 153 | 91-62 | 66.3% |
| 20 | AdamTan12_Finals | 1769 +- 62 | 162 | 89-73 | 66.0% |
| 21 | AdamTan12_Icarus | 1754 +- 52 | 213 | 112-101 | 65.0% |
| 22 | HugoIngelsson_YokoOno | 1742 +- 60 | 165 | 89-76 | 64.1% |
| 23 | VarunVejalla_final4_run_away10 | 1697 +- 72 | 129 | 73-56 | 60.9% |
| 24 | HugoIngelsson_Xzibit | 1666 +- 64 | 156 | 86-70 | 58.8% |
| 25 | spsquared_TSPAARK_QUALS | 1649 +- 67 | 147 | 82-65 | 57.5% |
| 26 | us:v0d | 1647 +- 128 | 37 | 27-10 | 57.3% |
| 27 | us:s4m | 1627 +- 226 | 8 | 5-3 | 55.9% |
| 28 | us:s4j | 1593 +- 65 | 131 | 87-44 | 53.7% |
| 29 | us:s4g | 1585 +- 76 | 80 | 51-29 | 53.2% |
| 30 | us:s3y | 1577 +- 64 | 174 | 108-66 | 52.6% |
| 31 | us:s4n | 1564 +- 77 | 77 | 50-27 | 51.8% |
| 32 | us:s4h | 1563 +- 76 | 80 | 52-28 | 51.7% |
| 33 | us:s4i | 1559 +- 63 | 134 | 84-50 | 51.4% |
| 34 | us:s4d | 1554 +- 42 | 324 | 211-113 | 51.2% |
| 35 | us:s4f | 1544 +- 54 | 160 | 110-50 | 50.5% |
| 36 | us:s4e | 1530 +- 76 | 80 | 61-19 | 49.6% |
| 37 | us:s4b | 1529 +- 53 | 172 | 117-55 | 49.5% |
| 38 | us:abrk | 1524 +- 29 | 573 | 210-363 | 49.2% |
| 39 | us:s4q | 1513 +- 76 | 80 | 61-19 | 48.5% |
| 40 | us:s4r | 1513 +- 76 | 80 | 61-19 | 48.5% |
| 41 | us:s3u | 1508 +- 76 | 80 | 53-27 | 48.2% |
| 42 | us:s4c | 1507 +- 75 | 80 | 53-27 | 48.2% |
| 43 | us:s3f | 1499 +- 75 | 80 | 46-34 | 47.6% |
| 44 | us:s3o | 1497 +- 75 | 80 | 50-30 | 47.5% |
| 45 | us:s3t | 1494 +- 75 | 80 | 49-31 | 47.3% |
| 46 | us:s4a | 1491 +- 75 | 80 | 50-30 | 47.1% |
| 47 | us:s3z | 1490 +- 75 | 80 | 50-30 | 47.1% |
| 48 | us:s3v | 1489 +- 75 | 80 | 54-26 | 47.0% |
| 49 | outercloudstudio_v_1_18 | 1489 +- 58 | 183 | 84-99 | 46.9% |
| 50 | us:s3w | 1483 +- 75 | 80 | 52-28 | 46.6% |
| 51 | us:s3r | 1483 +- 75 | 80 | 46-34 | 46.6% |
| 52 | us:s3s | 1483 +- 75 | 80 | 46-34 | 46.6% |
| 53 | us:s3p | 1478 +- 75 | 80 | 43-37 | 46.3% |
| 54 | us:s2o_id | 1477 +- 75 | 80 | 40-40 | 46.2% |
| 55 | us:s2o | 1476 +- 14 | 2632 | 1146-1486 | 46.2% |
| 56 | us:s3h | 1472 +- 54 | 188 | 100-88 | 45.9% |
| 57 | amcsz_fighterrats | 1470 +- 63 | 156 | 78-78 | 45.7% |
| 58 | us:s3x | 1466 +- 75 | 80 | 48-32 | 45.5% |
| 59 | CS50Crab_protoSaberV7 | 1465 +- 61 | 168 | 84-84 | 45.4% |
| 60 | CS50Crab_rapierV1 | 1464 +- 51 | 231 | 107-124 | 45.3% |
| 61 | us:s3q | 1454 +- 75 | 80 | 35-45 | 44.8% |
| 62 | us:s3n | 1451 +- 75 | 80 | 29-51 | 44.6% |
| 63 | us:s3k | 1450 +- 75 | 80 | 29-51 | 44.5% |
| 64 | us:s3m | 1450 +- 75 | 80 | 30-50 | 44.5% |
| 65 | us:s3i | 1449 +- 75 | 80 | 27-53 | 44.5% |
| 66 | us:s3l | 1445 +- 76 | 80 | 27-53 | 44.2% |
| 67 | us:s3e | 1440 +- 76 | 80 | 29-51 | 43.9% |
| 68 | us:s3g | 1440 +- 76 | 80 | 30-50 | 43.9% |
| 69 | us:arush | 1434 +- 78 | 80 | 29-51 | 43.5% |
| 70 | bc26vibe_bot | 1400 +- 52 | 219 | 99-120 | 41.2% |
| 71 | PixelRifts_mushakraj_mk3 | 1399 +- 61 | 177 | 80-97 | 41.1% |
| 72 | us:ratbot | 1383 +- 69 | 154 | 107-47 | 40.3% |
| 73 | us:s0u | 1358 +- 126 | 37 | 13-24 | 38.7% |
| 74 | us:s0m | 1346 +- 69 | 148 | 98-50 | 38.0% |
| 75 | XSercia_genies | 1313 +- 49 | 291 | 98-193 | 35.6% |
| 76 | us:s0e | 1290 +- 143 | 33 | 15-18 | 34.4% |
| 77 | C0derTang_v6 | 1266 +- 56 | 213 | 71-142 | 32.6% |
| 78 | Naton-Cai_ratplayer | 1245 +- 69 | 132 | 60-72 | 31.3% |
| 79 | ask8000_bumchit67 | 1240 +- 54 | 240 | 84-156 | 31.0% |
| 80 | bc26-example | 1202 +- 36 | 538 | 203-335 | 28.6% |
| 81 | justinottesen_alfredo | 1201 +- 57 | 189 | 81-108 | 28.5% |
| 82 | Camleaf_mk1 | 1185 +- 64 | 153 | 68-85 | 27.6% |
| 83 | vincentwang60_javabot | 1167 +- 63 | 153 | 67-86 | 26.4% |
| 84 | bc26-example-copy | 1138 +- 58 | 168 | 72-96 | 24.7% |
| 85 | Gavdingo_v1 | 1096 +- 55 | 192 | 78-114 | 22.2% |
| 86 | justinottesen_carbonara | 1094 +- 66 | 141 | 61-80 | 22.1% |
| 87 | battlecode-archive_result_408 | 1066 +- 64 | 147 | 57-90 | 20.5% |
| 88 | GuzziFlipFlops_Hermesv8 | 1060 +- 58 | 171 | 70-101 | 20.2% |
| 89 | j2399_weighted_micro_v9 | 1039 +- 67 | 147 | 59-88 | 19.1% |
| 90 | quanticsoul4772_ratbot | 976 +- 64 | 159 | 67-92 | 15.9% |
| 91 | seru9_TOTAL_AGGRO | 960 +- 66 | 165 | 63-102 | 15.1% |
| 92 | Victor-Ramirez-Garcia_battleca | 939 +- 68 | 165 | 62-103 | 14.2% |
| 93 | anicolao_myplayer | 701 +- 78 | 159 | 37-122 | 6.0% |
| 94 | j2399_weighted_path | 642 +- 93 | 96 | 26-70 | 4.6% |
| 95 | Krak9n_robot | 513 +- 105 | 111 | 13-98 | 2.4% |

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
| replica | +28 |
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
| cheesebottles | -351 |
| mincemeat | -351 |
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
