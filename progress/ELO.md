# Ladder

3851 rows of progress/games.csv (2576 local games, 1275 replica matches), rated by a batch Bradley-Terry fit on the Elo scale (`tools/elolib.py`). A local game counts its cell score (`tools/score.py`: a win or loss when kings decide the game, the points share when points do), a replica match its games won. A game between one of our builds and a field bot also fits how hard its map is for us (table at the end), so builds gauntleted on different maps compare on an average map. Each pair of players counts at most 200 games, every player carries one virtual win and loss against a fixed anchor, and ratings are relative to a mean of 1500 over all 86 players. Each of our builds is its own player. Regenerate with `tools/elo.py`; do not edit.

Games against field bots are replica scrimmages only; local games are our builds against each other and the stock players. Record = games won and lost.

![ratings of our builds](elo.png)

Our builds, in attempt order (rating +- 95%; field score = expected score against each of the 51 field players, one game each):

| build | rating | rank | games | record | field score |
|---|---|---|---|---|---|
| s0e | 1318 +- 142 | 67 of 86 | 33 | 15-18 | 35.8% |
| s0m | 1349 +- 69 | 65 of 86 | 148 | 98-50 | 37.8% |
| s0u | 1360 +- 126 | 64 of 86 | 37 | 13-24 | 38.5% |
| v0d | 1650 +- 128 | 26 of 86 | 37 | 27-10 | 57.5% |
| s2o | 1490 +- 14 | 47 of 86 | 2438 | 1097-1341 | 46.9% |
| s3e | 1454 +- 76 | 58 of 86 | 80 | 29-51 | 44.5% |
| s3f | 1512 +- 75 | 34 of 86 | 80 | 46-34 | 48.3% |
| s3g | 1454 +- 76 | 59 of 86 | 80 | 30-50 | 44.5% |
| s2o_id | 1490 +- 75 | 46 of 86 | 80 | 40-40 | 46.9% |
| s3h | 1495 +- 53 | 44 of 86 | 188 | 100-88 | 47.2% |
| s3i | 1463 +- 75 | 56 of 86 | 80 | 27-53 | 45.1% |
| s3k | 1464 +- 75 | 54 of 86 | 80 | 29-51 | 45.2% |
| s3l | 1459 +- 76 | 57 of 86 | 80 | 27-53 | 44.8% |
| s3m | 1464 +- 75 | 55 of 86 | 80 | 30-50 | 45.2% |
| s3n | 1465 +- 75 | 53 of 86 | 80 | 29-51 | 45.2% |
| s3o | 1510 +- 75 | 35 of 86 | 80 | 50-30 | 48.2% |
| s3p | 1491 +- 75 | 45 of 86 | 80 | 43-37 | 47.0% |
| s3q | 1467 +- 75 | 51 of 86 | 80 | 35-45 | 45.4% |
| s3r | 1496 +- 75 | 42 of 86 | 80 | 46-34 | 47.3% |
| s3s | 1496 +- 75 | 43 of 86 | 80 | 46-34 | 47.3% |
| s3t | 1507 +- 75 | 37 of 86 | 80 | 49-31 | 48.0% |
| s3u | 1521 +- 75 | 32 of 86 | 80 | 53-27 | 48.9% |
| s3v | 1503 +- 75 | 40 of 86 | 80 | 54-26 | 47.7% |
| s3w | 1496 +- 75 | 41 of 86 | 80 | 52-28 | 47.3% |
| s3x | 1479 +- 75 | 48 of 86 | 80 | 48-32 | 46.2% |
| s3y | 1602 +- 64 | 27 of 86 | 171 | 106-65 | 54.3% |
| s3z | 1504 +- 75 | 39 of 86 | 80 | 50-30 | 47.8% |
| s4a | 1504 +- 75 | 38 of 86 | 80 | 50-30 | 47.8% |
| s4b | 1544 +- 53 | 29 of 86 | 172 | 117-55 | 50.4% |
| s4c | 1521 +- 75 | 33 of 86 | 80 | 53-27 | 48.9% |
| s4d | 1537 +- 75 | 31 of 86 | 83 | 61-22 | 50.0% |
| arush | 1449 +- 78 | 60 of 86 | 80 | 29-51 | 44.2% |
| s4e | 1544 +- 76 | 30 of 86 | 80 | 61-19 | 50.4% |
| s4f | 1550 +- 100 | 28 of 86 | 46 | 38-8 | 50.8% |
| ratbot | 1387 +- 69 | 63 of 86 | 154 | 107-47 | 40.2% |

All players:

| # | player | rating | games | record | field score |
|---:|---|---|---|---|---|
| 1 | IvanGeffner_basic51 | 2117 +- 69 | 129 | 92-37 | 89.1% |
| 2 | uravt_Version41 | 2079 +- 72 | 120 | 83-37 | 87.1% |
| 3 | uravt_Version35 | 2044 +- 64 | 129 | 78-51 | 85.2% |
| 4 | awu7_awubot_us | 2043 +- 73 | 117 | 77-40 | 85.1% |
| 5 | abcdezyxwv_bestsofar | 1988 +- 69 | 165 | 122-43 | 81.7% |
| 6 | quesswho_bot16 | 1980 +- 62 | 144 | 83-61 | 81.2% |
| 7 | thewaxmango_PhD | 1978 +- 83 | 105 | 73-32 | 81.1% |
| 8 | nzjt_TheComplexMerlin | 1957 +- 80 | 105 | 72-33 | 79.7% |
| 9 | nubbbbbb_testplayer | 1947 +- 76 | 102 | 66-36 | 79.0% |
| 10 | daannte_v52_ThisGuy | 1943 +- 62 | 141 | 78-63 | 78.7% |
| 11 | quesswho_quals | 1943 +- 68 | 120 | 72-48 | 78.7% |
| 12 | daannte_v47_Skadoosh | 1916 +- 63 | 153 | 88-65 | 76.8% |
| 13 | battlecode-archive_testplayer | 1915 +- 64 | 141 | 86-55 | 76.8% |
| 14 | awu7_awubot_quals | 1900 +- 68 | 126 | 77-49 | 75.7% |
| 15 | TeamSPAARK_SPAARK | 1889 +- 63 | 144 | 85-59 | 74.9% |
| 16 | AlexT101_usqualsbot | 1811 +- 59 | 186 | 115-71 | 69.3% |
| 17 | bman12345678_goonv23 | 1795 +- 86 | 108 | 69-39 | 68.1% |
| 18 | AlexT101_finalsbot | 1794 +- 63 | 159 | 86-73 | 68.0% |
| 19 | AdamTan12_Icarus | 1785 +- 59 | 165 | 90-75 | 67.4% |
| 20 | HugoIngelsson_YokoOno | 1748 +- 68 | 135 | 75-60 | 64.7% |
| 21 | thewaxmango_collegejunior | 1742 +- 73 | 117 | 67-50 | 64.2% |
| 22 | AdamTan12_Finals | 1740 +- 68 | 141 | 76-65 | 64.1% |
| 23 | VarunVejalla_final4_run_away10 | 1677 +- 88 | 96 | 58-38 | 59.6% |
| 24 | spsquared_TSPAARK_QUALS | 1657 +- 77 | 114 | 64-50 | 58.1% |
| 25 | HugoIngelsson_Xzibit | 1656 +- 70 | 132 | 73-59 | 58.1% |
| 26 | us:v0d | 1650 +- 128 | 37 | 27-10 | 57.5% |
| 27 | us:s3y | 1602 +- 64 | 171 | 106-65 | 54.3% |
| 28 | us:s4f | 1550 +- 100 | 46 | 38-8 | 50.8% |
| 29 | us:s4b | 1544 +- 53 | 172 | 117-55 | 50.4% |
| 30 | us:s4e | 1544 +- 76 | 80 | 61-19 | 50.4% |
| 31 | us:s4d | 1537 +- 75 | 83 | 61-22 | 50.0% |
| 32 | us:s3u | 1521 +- 75 | 80 | 53-27 | 48.9% |
| 33 | us:s4c | 1521 +- 75 | 80 | 53-27 | 48.9% |
| 34 | us:s3f | 1512 +- 75 | 80 | 46-34 | 48.3% |
| 35 | us:s3o | 1510 +- 75 | 80 | 50-30 | 48.2% |
| 36 | outercloudstudio_v_1_18 | 1508 +- 67 | 141 | 72-69 | 48.0% |
| 37 | us:s3t | 1507 +- 75 | 80 | 49-31 | 48.0% |
| 38 | us:s4a | 1504 +- 75 | 80 | 50-30 | 47.8% |
| 39 | us:s3z | 1504 +- 75 | 80 | 50-30 | 47.8% |
| 40 | us:s3v | 1503 +- 75 | 80 | 54-26 | 47.7% |
| 41 | us:s3w | 1496 +- 75 | 80 | 52-28 | 47.3% |
| 42 | us:s3r | 1496 +- 75 | 80 | 46-34 | 47.3% |
| 43 | us:s3s | 1496 +- 75 | 80 | 46-34 | 47.3% |
| 44 | us:s3h | 1495 +- 53 | 188 | 100-88 | 47.2% |
| 45 | us:s3p | 1491 +- 75 | 80 | 43-37 | 47.0% |
| 46 | us:s2o_id | 1490 +- 75 | 80 | 40-40 | 46.9% |
| 47 | us:s2o | 1490 +- 14 | 2438 | 1097-1341 | 46.9% |
| 48 | us:s3x | 1479 +- 75 | 80 | 48-32 | 46.2% |
| 49 | CS50Crab_rapierV1 | 1470 +- 60 | 168 | 83-85 | 45.5% |
| 50 | CS50Crab_protoSaberV7 | 1467 +- 71 | 129 | 64-65 | 45.3% |
| 51 | us:s3q | 1467 +- 75 | 80 | 35-45 | 45.4% |
| 52 | amcsz_fighterrats | 1465 +- 75 | 117 | 56-61 | 45.1% |
| 53 | us:s3n | 1465 +- 75 | 80 | 29-51 | 45.2% |
| 54 | us:s3k | 1464 +- 75 | 80 | 29-51 | 45.2% |
| 55 | us:s3m | 1464 +- 75 | 80 | 30-50 | 45.2% |
| 56 | us:s3i | 1463 +- 75 | 80 | 27-53 | 45.1% |
| 57 | us:s3l | 1459 +- 76 | 80 | 27-53 | 44.8% |
| 58 | us:s3e | 1454 +- 76 | 80 | 29-51 | 44.5% |
| 59 | us:s3g | 1454 +- 76 | 80 | 30-50 | 44.5% |
| 60 | us:arush | 1449 +- 78 | 80 | 29-51 | 44.2% |
| 61 | PixelRifts_mushakraj_mk3 | 1424 +- 72 | 135 | 63-72 | 42.4% |
| 62 | bc26vibe_bot | 1402 +- 59 | 168 | 81-87 | 41.0% |
| 63 | us:ratbot | 1387 +- 69 | 154 | 107-47 | 40.2% |
| 64 | us:s0u | 1360 +- 126 | 37 | 13-24 | 38.5% |
| 65 | us:s0m | 1349 +- 69 | 148 | 98-50 | 37.8% |
| 66 | XSercia_genies | 1320 +- 57 | 228 | 82-146 | 35.7% |
| 67 | us:s0e | 1318 +- 142 | 33 | 15-18 | 35.8% |
| 68 | C0derTang_v6 | 1296 +- 60 | 168 | 64-104 | 34.1% |
| 69 | Naton-Cai_ratplayer | 1252 +- 75 | 114 | 49-65 | 31.3% |
| 70 | ask8000_bumchit67 | 1245 +- 60 | 186 | 70-116 | 30.8% |
| 71 | justinottesen_alfredo | 1222 +- 64 | 156 | 66-90 | 29.4% |
| 72 | bc26-example | 1204 +- 36 | 532 | 199-333 | 28.3% |
| 73 | Camleaf_mk1 | 1178 +- 73 | 126 | 52-74 | 26.6% |
| 74 | vincentwang60_javabot | 1173 +- 67 | 141 | 61-80 | 26.4% |
| 75 | bc26-example-copy | 1131 +- 66 | 132 | 52-80 | 23.8% |
| 76 | Gavdingo_v1 | 1108 +- 62 | 159 | 63-96 | 22.4% |
| 77 | justinottesen_carbonara | 1096 +- 76 | 114 | 48-66 | 21.7% |
| 78 | battlecode-archive_result_408 | 1084 +- 68 | 132 | 50-82 | 21.1% |
| 79 | GuzziFlipFlops_Hermesv8 | 1071 +- 64 | 141 | 57-84 | 20.4% |
| 80 | j2399_weighted_micro_v9 | 1058 +- 72 | 132 | 52-80 | 19.6% |
| 81 | seru9_TOTAL_AGGRO | 987 +- 71 | 144 | 52-92 | 16.0% |
| 82 | quanticsoul4772_ratbot | 983 +- 70 | 138 | 57-81 | 15.8% |
| 83 | Victor-Ramirez-Garcia_battleca | 940 +- 74 | 147 | 52-95 | 13.8% |
| 84 | anicolao_myplayer | 737 +- 83 | 144 | 35-109 | 6.7% |
| 85 | j2399_weighted_path | 637 +- 103 | 87 | 21-66 | 4.3% |
| 86 | Krak9n_robot | 502 +- 117 | 102 | 10-92 | 2.1% |

Map handicap for our builds (Elo points; + = we score better there than on an average map of the mix, replica = the replica's map rotation):

| map | handicap |
|---|---:|
| DefaultLarge | +182 |
| DefaultMedium | +182 |
| DefaultSmall | +182 |
| EscapeTheNight | +182 |
| Meow | +182 |
| averystrangespace | +182 |
| constrictinglattice | +182 |
| corridorofdoomanddespair | +182 |
| dirtpassageway | +182 |
| hatefullattice | +182 |
| keepout | +182 |
| mercifullattice | +182 |
| minimaze | +182 |
| peaceinourtime | +182 |
| pipes | +182 |
| ratsareants | +182 |
| rift | +182 |
| safelycontained | +182 |
| sittingducks | +182 |
| streetsofnewyork | +182 |
| superioristhmus | +182 |
| trapped | +182 |
| Excavation | +152 |
| uneasy_alliance | +152 |
| wallsofparadis | +152 |
| externalsustenance | +124 |
| jail | +124 |
| Nofreecheese | +98 |
| cheesefarm | +98 |
| evileye | +98 |
| starvation | +98 |
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
| sunandmoon | +17 |
| replica | +4 |
| tornadoes | -23 |
| canyoudig | -29 |
| TheHeist | -54 |
| groveisbeingdesecrated | -54 |
| rose | -54 |
| where_are_you | -110 |
| 5t4rv4t10n_1337 | -147 |
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
| clockwisdom | -618 |
| fullofsecrets | -618 |
