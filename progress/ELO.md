# Ladder

4443 rows of progress/games.csv (3003 local games, 1440 replica matches), rated by a batch Bradley-Terry fit on the Elo scale (`tools/elolib.py`). A local game counts its cell score (`tools/score.py`: a win or loss when kings decide the game, the points share when points do), a replica match its games won. A game between one of our builds and a field bot also fits how hard its map is for us (table at the end), so builds gauntleted on different maps compare on an average map. Each pair of players counts at most 200 games, every player carries one virtual win and loss against a fixed anchor, and ratings are relative to a mean of 1500 over all 90 players. Each of our builds is its own player. Regenerate with `tools/elo.py`; do not edit.

Games against field bots are replica scrimmages only; local games are our builds against each other and the stock players. Record = games won and lost.

![ratings of our builds](elo.png)

Our builds, in attempt order (rating +- 95%; field score = expected score against each of the 51 field players, one game each):

| build | rating | rank | games | record | field score |
|---|---|---|---|---|---|
| s0e | 1291 +- 144 | 71 of 90 | 33 | 15-18 | 34.7% |
| s0m | 1340 +- 69 | 69 of 90 | 148 | 98-50 | 37.7% |
| s0u | 1352 +- 126 | 68 of 90 | 37 | 13-24 | 38.5% |
| v0d | 1642 +- 128 | 26 of 90 | 37 | 27-10 | 56.8% |
| s2o | 1482 +- 14 | 50 of 90 | 2478 | 1110-1368 | 46.6% |
| s3e | 1446 +- 76 | 62 of 90 | 80 | 29-51 | 44.3% |
| s3f | 1505 +- 75 | 38 of 90 | 80 | 46-34 | 48.0% |
| s3g | 1446 +- 76 | 63 of 90 | 80 | 30-50 | 44.3% |
| s2o_id | 1483 +- 75 | 49 of 90 | 80 | 40-40 | 46.6% |
| s3h | 1478 +- 54 | 51 of 90 | 188 | 100-88 | 46.3% |
| s3i | 1456 +- 75 | 60 of 90 | 80 | 27-53 | 44.9% |
| s3k | 1457 +- 75 | 57 of 90 | 80 | 29-51 | 45.0% |
| s3l | 1451 +- 76 | 61 of 90 | 80 | 27-53 | 44.6% |
| s3m | 1457 +- 75 | 58 of 90 | 80 | 30-50 | 45.0% |
| s3n | 1458 +- 75 | 56 of 90 | 80 | 29-51 | 45.0% |
| s3o | 1503 +- 75 | 39 of 90 | 80 | 50-30 | 47.8% |
| s3p | 1484 +- 75 | 48 of 90 | 80 | 43-37 | 46.7% |
| s3q | 1460 +- 75 | 55 of 90 | 80 | 35-45 | 45.2% |
| s3r | 1489 +- 75 | 46 of 90 | 80 | 46-34 | 47.0% |
| s3s | 1489 +- 75 | 47 of 90 | 80 | 46-34 | 47.0% |
| s3t | 1500 +- 75 | 40 of 90 | 80 | 49-31 | 47.7% |
| s3u | 1514 +- 75 | 36 of 90 | 80 | 53-27 | 48.5% |
| s3v | 1495 +- 75 | 43 of 90 | 80 | 54-26 | 47.4% |
| s3w | 1489 +- 75 | 45 of 90 | 80 | 52-28 | 47.0% |
| s3x | 1472 +- 75 | 52 of 90 | 80 | 48-32 | 45.9% |
| s3y | 1579 +- 65 | 29 of 90 | 171 | 106-65 | 52.7% |
| s3z | 1497 +- 75 | 42 of 90 | 80 | 50-30 | 47.5% |
| s4a | 1497 +- 75 | 41 of 90 | 80 | 50-30 | 47.5% |
| s4b | 1535 +- 53 | 34 of 90 | 172 | 117-55 | 49.9% |
| s4c | 1514 +- 75 | 37 of 90 | 80 | 53-27 | 48.5% |
| s4d | 1565 +- 43 | 31 of 90 | 316 | 209-107 | 51.8% |
| arush | 1440 +- 78 | 64 of 90 | 80 | 29-51 | 43.9% |
| s4e | 1537 +- 76 | 33 of 90 | 80 | 61-19 | 50.0% |
| s4f | 1549 +- 54 | 32 of 90 | 160 | 110-50 | 50.8% |
| abrk | 1528 +- 35 | 35 of 90 | 393 | 145-248 | 49.5% |
| s4g | 1590 +- 76 | 27 of 90 | 80 | 51-29 | 53.3% |
| s4h | 1567 +- 76 | 30 of 90 | 80 | 52-28 | 51.9% |
| s4i | 1583 +- 80 | 28 of 90 | 73 | 48-25 | 52.9% |
| ratbot | 1378 +- 69 | 67 of 90 | 154 | 107-47 | 40.1% |

All players:

| # | player | rating | games | record | field score |
|---:|---|---|---|---|---|
| 1 | IvanGeffner_basic51 | 2133 +- 66 | 138 | 98-40 | 89.2% |
| 2 | uravt_Version41 | 2109 +- 68 | 135 | 95-40 | 88.0% |
| 3 | awu7_awubot_us | 2047 +- 69 | 126 | 81-45 | 84.6% |
| 4 | uravt_Version35 | 2045 +- 60 | 144 | 85-59 | 84.5% |
| 5 | abcdezyxwv_bestsofar | 2008 +- 63 | 201 | 147-54 | 82.2% |
| 6 | nzjt_TheComplexMerlin | 1996 +- 75 | 117 | 81-36 | 81.5% |
| 7 | quesswho_bot16 | 1979 +- 58 | 159 | 88-71 | 80.3% |
| 8 | thewaxmango_PhD | 1979 +- 71 | 132 | 84-48 | 80.3% |
| 9 | nubbbbbb_testplayer | 1966 +- 69 | 120 | 75-45 | 79.5% |
| 10 | quesswho_quals | 1963 +- 61 | 144 | 86-58 | 79.3% |
| 11 | daannte_v52_ThisGuy | 1948 +- 61 | 144 | 79-65 | 78.2% |
| 12 | battlecode-archive_testplayer | 1940 +- 59 | 165 | 100-65 | 77.7% |
| 13 | daannte_v47_Skadoosh | 1933 +- 59 | 168 | 96-72 | 77.2% |
| 14 | awu7_awubot_quals | 1922 +- 65 | 135 | 82-53 | 76.5% |
| 15 | TeamSPAARK_SPAARK | 1903 +- 61 | 153 | 88-65 | 75.2% |
| 16 | AlexT101_usqualsbot | 1824 +- 54 | 225 | 141-84 | 69.6% |
| 17 | AlexT101_finalsbot | 1821 +- 57 | 180 | 97-83 | 69.3% |
| 18 | AdamTan12_Icarus | 1789 +- 57 | 177 | 94-83 | 67.1% |
| 19 | bman12345678_goonv23 | 1787 +- 74 | 132 | 81-51 | 66.9% |
| 20 | AdamTan12_Finals | 1768 +- 64 | 156 | 86-70 | 65.6% |
| 21 | thewaxmango_collegejunior | 1764 +- 65 | 141 | 81-60 | 65.3% |
| 22 | HugoIngelsson_YokoOno | 1758 +- 64 | 150 | 83-67 | 64.9% |
| 23 | VarunVejalla_final4_run_away10 | 1674 +- 81 | 111 | 63-48 | 59.0% |
| 24 | HugoIngelsson_Xzibit | 1666 +- 67 | 144 | 81-63 | 58.5% |
| 25 | spsquared_TSPAARK_QUALS | 1650 +- 71 | 135 | 74-61 | 57.4% |
| 26 | us:v0d | 1642 +- 128 | 37 | 27-10 | 56.8% |
| 27 | us:s4g | 1590 +- 76 | 80 | 51-29 | 53.3% |
| 28 | us:s4i | 1583 +- 80 | 73 | 48-25 | 52.9% |
| 29 | us:s3y | 1579 +- 65 | 171 | 106-65 | 52.7% |
| 30 | us:s4h | 1567 +- 76 | 80 | 52-28 | 51.9% |
| 31 | us:s4d | 1565 +- 43 | 316 | 209-107 | 51.8% |
| 32 | us:s4f | 1549 +- 54 | 160 | 110-50 | 50.8% |
| 33 | us:s4e | 1537 +- 76 | 80 | 61-19 | 50.0% |
| 34 | us:s4b | 1535 +- 53 | 172 | 117-55 | 49.9% |
| 35 | us:abrk | 1528 +- 35 | 393 | 145-248 | 49.5% |
| 36 | us:s3u | 1514 +- 75 | 80 | 53-27 | 48.5% |
| 37 | us:s4c | 1514 +- 75 | 80 | 53-27 | 48.5% |
| 38 | us:s3f | 1505 +- 75 | 80 | 46-34 | 48.0% |
| 39 | us:s3o | 1503 +- 75 | 80 | 50-30 | 47.8% |
| 40 | us:s3t | 1500 +- 75 | 80 | 49-31 | 47.7% |
| 41 | us:s4a | 1497 +- 75 | 80 | 50-30 | 47.5% |
| 42 | us:s3z | 1497 +- 75 | 80 | 50-30 | 47.5% |
| 43 | us:s3v | 1495 +- 75 | 80 | 54-26 | 47.4% |
| 44 | outercloudstudio_v_1_18 | 1491 +- 62 | 162 | 78-84 | 47.1% |
| 45 | us:s3w | 1489 +- 75 | 80 | 52-28 | 47.0% |
| 46 | us:s3r | 1489 +- 75 | 80 | 46-34 | 47.0% |
| 47 | us:s3s | 1489 +- 75 | 80 | 46-34 | 47.0% |
| 48 | us:s3p | 1484 +- 75 | 80 | 43-37 | 46.7% |
| 49 | us:s2o_id | 1483 +- 75 | 80 | 40-40 | 46.6% |
| 50 | us:s2o | 1482 +- 14 | 2478 | 1110-1368 | 46.6% |
| 51 | us:s3h | 1478 +- 54 | 188 | 100-88 | 46.3% |
| 52 | us:s3x | 1472 +- 75 | 80 | 48-32 | 45.9% |
| 53 | amcsz_fighterrats | 1468 +- 67 | 141 | 71-70 | 45.6% |
| 54 | CS50Crab_protoSaberV7 | 1462 +- 67 | 144 | 72-72 | 45.2% |
| 55 | us:s3q | 1460 +- 75 | 80 | 35-45 | 45.2% |
| 56 | us:s3n | 1458 +- 75 | 80 | 29-51 | 45.0% |
| 57 | us:s3k | 1457 +- 75 | 80 | 29-51 | 45.0% |
| 58 | us:s3m | 1457 +- 75 | 80 | 30-50 | 45.0% |
| 59 | CS50Crab_rapierV1 | 1456 +- 53 | 210 | 96-114 | 44.8% |
| 60 | us:s3i | 1456 +- 75 | 80 | 27-53 | 44.9% |
| 61 | us:s3l | 1451 +- 76 | 80 | 27-53 | 44.6% |
| 62 | us:s3e | 1446 +- 76 | 80 | 29-51 | 44.3% |
| 63 | us:s3g | 1446 +- 76 | 80 | 30-50 | 44.3% |
| 64 | us:arush | 1440 +- 78 | 80 | 29-51 | 43.9% |
| 65 | bc26vibe_bot | 1394 +- 55 | 195 | 90-105 | 40.9% |
| 66 | PixelRifts_mushakraj_mk3 | 1391 +- 67 | 153 | 68-85 | 40.7% |
| 67 | us:ratbot | 1378 +- 69 | 154 | 107-47 | 40.1% |
| 68 | us:s0u | 1352 +- 126 | 37 | 13-24 | 38.5% |
| 69 | us:s0m | 1340 +- 69 | 148 | 98-50 | 37.7% |
| 70 | XSercia_genies | 1312 +- 53 | 261 | 90-171 | 35.7% |
| 71 | us:s0e | 1291 +- 144 | 33 | 15-18 | 34.7% |
| 72 | C0derTang_v6 | 1267 +- 58 | 192 | 66-126 | 32.8% |
| 73 | Naton-Cai_ratplayer | 1232 +- 72 | 123 | 54-69 | 30.7% |
| 74 | ask8000_bumchit67 | 1228 +- 56 | 219 | 77-142 | 30.4% |
| 75 | justinottesen_alfredo | 1213 +- 62 | 168 | 73-95 | 29.4% |
| 76 | bc26-example | 1196 +- 36 | 538 | 203-335 | 28.4% |
| 77 | Camleaf_mk1 | 1174 +- 68 | 141 | 61-80 | 27.0% |
| 78 | vincentwang60_javabot | 1168 +- 64 | 150 | 66-84 | 26.7% |
| 79 | bc26-example-copy | 1121 +- 63 | 144 | 57-87 | 23.8% |
| 80 | Gavdingo_v1 | 1097 +- 59 | 171 | 69-102 | 22.4% |
| 81 | justinottesen_carbonara | 1093 +- 72 | 123 | 53-70 | 22.2% |
| 82 | battlecode-archive_result_408 | 1069 +- 65 | 141 | 55-86 | 20.9% |
| 83 | GuzziFlipFlops_Hermesv8 | 1057 +- 60 | 159 | 65-94 | 20.2% |
| 84 | j2399_weighted_micro_v9 | 1041 +- 68 | 144 | 58-86 | 19.3% |
| 85 | seru9_TOTAL_AGGRO | 970 +- 70 | 147 | 55-92 | 15.7% |
| 86 | quanticsoul4772_ratbot | 968 +- 67 | 150 | 62-88 | 15.6% |
| 87 | Victor-Ramirez-Garcia_battleca | 925 +- 71 | 156 | 55-101 | 13.7% |
| 88 | anicolao_myplayer | 709 +- 82 | 147 | 35-112 | 6.3% |
| 89 | j2399_weighted_path | 641 +- 97 | 93 | 24-69 | 4.7% |
| 90 | Krak9n_robot | 493 +- 117 | 102 | 10-92 | 2.2% |

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
| houseofcards | +38 |
| replica | +20 |
| sunandmoon | +17 |
| tornadoes | -23 |
| canyoudig | -29 |
| rose | -54 |
| TheHeist | -54 |
| groveisbeingdesecrated | -54 |
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
