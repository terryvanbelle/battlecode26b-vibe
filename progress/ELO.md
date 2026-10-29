# Ladder

6447 rows of progress/games.csv (4541 local games, 1906 replica matches), rated by a batch Bradley-Terry fit on the Elo scale (`tools/elolib.py`). A local game counts its cell score (`tools/score.py`: a win or loss when kings decide the game, the points share when points do), a replica match its games won. A game between one of our builds and a field bot also fits how hard its map is for us (table at the end), so builds gauntleted on different maps compare on an average map. Each pair of players counts at most 200 games, every player carries one virtual win and loss against a fixed anchor, and ratings are relative to a mean of 1500 over all 114 players. Each of our builds is its own player. Regenerate with `tools/elo.py`; do not edit.

Games against field bots are replica scrimmages only; local games are our builds against each other and the stock players. Record = games won and lost.

![ratings of our builds](elo.png)

Our builds, in attempt order (rating +- 95%; field score = expected score against each of the 51 field players, one game each):

| build | rating | rank | games | record | field score |
|---|---|---|---|---|---|
| s0e | 1285 +- 144 | 95 of 114 | 33 | 15-18 | 34.4% |
| s0m | 1337 +- 69 | 92 of 114 | 148 | 98-50 | 37.6% |
| s0u | 1349 +- 126 | 91 of 114 | 37 | 13-24 | 38.4% |
| v0d | 1638 +- 128 | 26 of 114 | 37 | 27-10 | 56.6% |
| s2o | 1468 +- 13 | 73 of 114 | 2868 | 1214-1654 | 45.7% |
| s3e | 1432 +- 76 | 85 of 114 | 80 | 29-51 | 43.5% |
| s3f | 1491 +- 75 | 61 of 114 | 80 | 46-34 | 47.1% |
| s3g | 1432 +- 76 | 86 of 114 | 80 | 30-50 | 43.5% |
| s2o_id | 1469 +- 75 | 72 of 114 | 80 | 40-40 | 45.8% |
| s3h | 1466 +- 54 | 74 of 114 | 188 | 100-88 | 45.6% |
| s3i | 1441 +- 75 | 83 of 114 | 80 | 27-53 | 44.1% |
| s3k | 1443 +- 75 | 81 of 114 | 80 | 29-51 | 44.2% |
| s3l | 1437 +- 76 | 84 of 114 | 80 | 27-53 | 43.8% |
| s3m | 1443 +- 75 | 82 of 114 | 80 | 30-50 | 44.2% |
| s3n | 1444 +- 75 | 80 of 114 | 80 | 29-51 | 44.2% |
| s3o | 1489 +- 75 | 62 of 114 | 80 | 50-30 | 47.0% |
| s3p | 1470 +- 75 | 70 of 114 | 80 | 43-37 | 45.9% |
| s3q | 1446 +- 75 | 79 of 114 | 80 | 35-45 | 44.4% |
| s3r | 1475 +- 75 | 68 of 114 | 80 | 46-34 | 46.1% |
| s3s | 1475 +- 75 | 69 of 114 | 80 | 46-34 | 46.1% |
| s3t | 1486 +- 75 | 63 of 114 | 80 | 49-31 | 46.8% |
| s3u | 1500 +- 76 | 56 of 114 | 80 | 53-27 | 47.7% |
| s3v | 1481 +- 75 | 66 of 114 | 80 | 54-26 | 46.6% |
| s3w | 1475 +- 75 | 67 of 114 | 80 | 52-28 | 46.2% |
| s3x | 1458 +- 75 | 77 of 114 | 80 | 48-32 | 45.1% |
| s3y | 1564 +- 52 | 33 of 114 | 235 | 134-101 | 51.7% |
| s3z | 1483 +- 75 | 65 of 114 | 80 | 50-30 | 46.6% |
| s4a | 1483 +- 75 | 64 of 114 | 80 | 50-30 | 46.6% |
| s4b | 1521 +- 53 | 46 of 114 | 172 | 117-55 | 49.0% |
| s4c | 1499 +- 76 | 57 of 114 | 80 | 53-27 | 47.7% |
| s4d | 1543 +- 34 | 40 of 114 | 462 | 287-175 | 50.4% |
| arush | 1426 +- 78 | 87 of 114 | 80 | 29-51 | 43.1% |
| s4e | 1523 +- 76 | 45 of 114 | 80 | 61-19 | 49.1% |
| s4f | 1536 +- 54 | 44 of 114 | 160 | 110-50 | 50.0% |
| abrk | 1517 +- 25 | 48 of 114 | 749 | 278-471 | 48.8% |
| s4g | 1578 +- 76 | 31 of 114 | 80 | 51-29 | 52.7% |
| s4h | 1556 +- 76 | 34 of 114 | 80 | 52-28 | 51.2% |
| s4i | 1569 +- 64 | 32 of 114 | 134 | 86-48 | 52.1% |
| s4j | 1586 +- 65 | 30 of 114 | 131 | 87-44 | 53.2% |
| s4m | 1598 +- 118 | 28 of 114 | 33 | 22-11 | 53.9% |
| s4n | 1555 +- 76 | 35 of 114 | 80 | 52-28 | 51.2% |
| s4q | 1505 +- 76 | 51 of 114 | 80 | 61-19 | 48.1% |
| s4r | 1505 +- 76 | 52 of 114 | 80 | 61-19 | 48.1% |
| s4t | 1497 +- 66 | 58 of 114 | 122 | 74-48 | 47.6% |
| s5c | 1519 +- 76 | 47 of 114 | 80 | 58-22 | 48.9% |
| s5e | 1491 +- 102 | 60 of 114 | 60 | 31-29 | 47.2% |
| s5g | 1537 +- 61 | 43 of 114 | 140 | 80-60 | 50.0% |
| s5j | 1588 +- 95 | 29 of 114 | 52 | 36-16 | 53.3% |
| araid | 1501 +- 25 | 55 of 114 | 772 | 312-460 | 47.8% |
| s5o | 1505 +- 75 | 53 of 114 | 80 | 41-39 | 48.0% |
| s5r | 1507 +- 49 | 50 of 114 | 202 | 121-81 | 48.2% |
| s5s | 1553 +- 76 | 36 of 114 | 80 | 56-24 | 51.1% |
| s5t | 1502 +- 73 | 54 of 114 | 86 | 42-44 | 47.8% |
| s5w | 1656 +- 91 | 25 of 114 | 72 | 45-27 | 57.7% |
| s5x | 1495 +- 90 | 59 of 114 | 55 | 30-25 | 47.4% |
| s5y | 1540 +- 76 | 41 of 114 | 80 | 53-27 | 50.2% |
| s6c | 1312 +- 393 | 93 of 114 | 2 | 0-2 | 36.1% |
| s6e | 1515 +- 75 | 49 of 114 | 80 | 51-29 | 48.6% |
| s6f | 1550 +- 104 | 37 of 114 | 42 | 28-14 | 50.9% |
| s6g | 1547 +- 89 | 38 of 114 | 57 | 35-22 | 50.7% |
| s6j | 1547 +- 118 | 39 of 114 | 42 | 18-24 | 50.7% |
| s6m | 1539 +- 280 | 42 of 114 | 4 | 2-2 | 50.2% |
| ratbot | 1374 +- 69 | 90 of 114 | 154 | 107-47 | 39.9% |

All players:

| # | player | rating | games | record | field score |
|---:|---|---|---|---|---|
| 1 | IvanGeffner_basic51 | 2134 +- 59 | 171 | 121-50 | 89.5% |
| 2 | uravt_Version41 | 2093 +- 61 | 162 | 109-53 | 87.4% |
| 3 | uravt_Version35 | 2036 +- 55 | 171 | 102-69 | 84.1% |
| 4 | abcdezyxwv_bestsofar | 2015 +- 54 | 279 | 208-71 | 82.8% |
| 5 | awu7_awubot_us | 2009 +- 58 | 165 | 99-66 | 82.5% |
| 6 | nzjt_TheComplexMerlin | 1982 +- 63 | 153 | 97-56 | 80.7% |
| 7 | thewaxmango_PhD | 1974 +- 60 | 168 | 102-66 | 80.2% |
| 8 | quesswho_bot16 | 1969 +- 52 | 195 | 108-87 | 79.8% |
| 9 | quesswho_quals | 1968 +- 52 | 195 | 112-83 | 79.8% |
| 10 | awu7_awubot_quals | 1959 +- 58 | 171 | 105-66 | 79.2% |
| 11 | battlecode-archive_testplayer | 1944 +- 51 | 213 | 122-91 | 78.2% |
| 12 | nubbbbbb_testplayer | 1944 +- 60 | 153 | 89-64 | 78.2% |
| 13 | daannte_v52_ThisGuy | 1939 +- 53 | 183 | 97-86 | 77.8% |
| 14 | TeamSPAARK_SPAARK | 1916 +- 53 | 198 | 113-85 | 76.2% |
| 15 | daannte_v47_Skadoosh | 1915 +- 52 | 207 | 115-92 | 76.1% |
| 16 | AlexT101_finalsbot | 1819 +- 53 | 207 | 110-97 | 69.4% |
| 17 | AlexT101_usqualsbot | 1819 +- 45 | 309 | 197-112 | 69.3% |
| 18 | bman12345678_goonv23 | 1805 +- 62 | 168 | 99-69 | 68.3% |
| 19 | AdamTan12_Finals | 1787 +- 52 | 225 | 132-93 | 67.1% |
| 20 | thewaxmango_collegejunior | 1774 +- 58 | 174 | 98-76 | 66.1% |
| 21 | AdamTan12_Icarus | 1768 +- 49 | 240 | 128-112 | 65.7% |
| 22 | HugoIngelsson_YokoOno | 1737 +- 57 | 183 | 96-87 | 63.5% |
| 23 | VarunVejalla_final4_run_away10 | 1700 +- 65 | 153 | 86-67 | 60.9% |
| 24 | HugoIngelsson_Xzibit | 1663 +- 59 | 180 | 98-82 | 58.4% |
| 25 | us:s5w | 1656 +- 91 | 72 | 45-27 | 57.7% |
| 26 | us:v0d | 1638 +- 128 | 37 | 27-10 | 56.6% |
| 27 | spsquared_TSPAARK_QUALS | 1631 +- 55 | 201 | 109-92 | 56.2% |
| 28 | us:s4m | 1598 +- 118 | 33 | 22-11 | 53.9% |
| 29 | us:s5j | 1588 +- 95 | 52 | 36-16 | 53.3% |
| 30 | us:s4j | 1586 +- 65 | 131 | 87-44 | 53.2% |
| 31 | us:s4g | 1578 +- 76 | 80 | 51-29 | 52.7% |
| 32 | us:s4i | 1569 +- 64 | 134 | 86-48 | 52.1% |
| 33 | us:s3y | 1564 +- 52 | 235 | 134-101 | 51.7% |
| 34 | us:s4h | 1556 +- 76 | 80 | 52-28 | 51.2% |
| 35 | us:s4n | 1555 +- 76 | 80 | 52-28 | 51.2% |
| 36 | us:s5s | 1553 +- 76 | 80 | 56-24 | 51.1% |
| 37 | us:s6f | 1550 +- 104 | 42 | 28-14 | 50.9% |
| 38 | us:s6g | 1547 +- 89 | 57 | 35-22 | 50.7% |
| 39 | us:s6j | 1547 +- 118 | 42 | 18-24 | 50.7% |
| 40 | us:s4d | 1543 +- 34 | 462 | 287-175 | 50.4% |
| 41 | us:s5y | 1540 +- 76 | 80 | 53-27 | 50.2% |
| 42 | us:s6m | 1539 +- 280 | 4 | 2-2 | 50.2% |
| 43 | us:s5g | 1537 +- 61 | 140 | 80-60 | 50.0% |
| 44 | us:s4f | 1536 +- 54 | 160 | 110-50 | 50.0% |
| 45 | us:s4e | 1523 +- 76 | 80 | 61-19 | 49.1% |
| 46 | us:s4b | 1521 +- 53 | 172 | 117-55 | 49.0% |
| 47 | us:s5c | 1519 +- 76 | 80 | 58-22 | 48.9% |
| 48 | us:abrk | 1517 +- 25 | 749 | 278-471 | 48.8% |
| 49 | us:s6e | 1515 +- 75 | 80 | 51-29 | 48.6% |
| 50 | us:s5r | 1507 +- 49 | 202 | 121-81 | 48.2% |
| 51 | us:s4q | 1505 +- 76 | 80 | 61-19 | 48.1% |
| 52 | us:s4r | 1505 +- 76 | 80 | 61-19 | 48.1% |
| 53 | us:s5o | 1505 +- 75 | 80 | 41-39 | 48.0% |
| 54 | us:s5t | 1502 +- 73 | 86 | 42-44 | 47.8% |
| 55 | us:araid | 1501 +- 25 | 772 | 312-460 | 47.8% |
| 56 | us:s3u | 1500 +- 76 | 80 | 53-27 | 47.7% |
| 57 | us:s4c | 1499 +- 76 | 80 | 53-27 | 47.7% |
| 58 | us:s4t | 1497 +- 66 | 122 | 74-48 | 47.6% |
| 59 | us:s5x | 1495 +- 90 | 55 | 30-25 | 47.4% |
| 60 | us:s5e | 1491 +- 102 | 60 | 31-29 | 47.2% |
| 61 | us:s3f | 1491 +- 75 | 80 | 46-34 | 47.1% |
| 62 | us:s3o | 1489 +- 75 | 80 | 50-30 | 47.0% |
| 63 | us:s3t | 1486 +- 75 | 80 | 49-31 | 46.8% |
| 64 | us:s4a | 1483 +- 75 | 80 | 50-30 | 46.6% |
| 65 | us:s3z | 1483 +- 75 | 80 | 50-30 | 46.6% |
| 66 | us:s3v | 1481 +- 75 | 80 | 54-26 | 46.6% |
| 67 | us:s3w | 1475 +- 75 | 80 | 52-28 | 46.2% |
| 68 | us:s3r | 1475 +- 75 | 80 | 46-34 | 46.1% |
| 69 | us:s3s | 1475 +- 75 | 80 | 46-34 | 46.1% |
| 70 | us:s3p | 1470 +- 75 | 80 | 43-37 | 45.9% |
| 71 | amcsz_fighterrats | 1469 +- 57 | 183 | 92-91 | 45.7% |
| 72 | us:s2o_id | 1469 +- 75 | 80 | 40-40 | 45.8% |
| 73 | us:s2o | 1468 +- 13 | 2868 | 1214-1654 | 45.7% |
| 74 | us:s3h | 1466 +- 54 | 188 | 100-88 | 45.6% |
| 75 | CS50Crab_protoSaberV7 | 1462 +- 57 | 189 | 93-96 | 45.3% |
| 76 | CS50Crab_rapierV1 | 1461 +- 45 | 291 | 136-155 | 45.2% |
| 77 | us:s3x | 1458 +- 75 | 80 | 48-32 | 45.1% |
| 78 | outercloudstudio_v_1_18 | 1458 +- 50 | 243 | 99-144 | 45.0% |
| 79 | us:s3q | 1446 +- 75 | 80 | 35-45 | 44.4% |
| 80 | us:s3n | 1444 +- 75 | 80 | 29-51 | 44.2% |
| 81 | us:s3k | 1443 +- 75 | 80 | 29-51 | 44.2% |
| 82 | us:s3m | 1443 +- 75 | 80 | 30-50 | 44.2% |
| 83 | us:s3i | 1441 +- 75 | 80 | 27-53 | 44.1% |
| 84 | us:s3l | 1437 +- 76 | 80 | 27-53 | 43.8% |
| 85 | us:s3e | 1432 +- 76 | 80 | 29-51 | 43.5% |
| 86 | us:s3g | 1432 +- 76 | 80 | 30-50 | 43.5% |
| 87 | us:arush | 1426 +- 78 | 80 | 29-51 | 43.1% |
| 88 | bc26vibe_bot | 1423 +- 44 | 297 | 141-156 | 42.8% |
| 89 | PixelRifts_mushakraj_mk3 | 1409 +- 56 | 207 | 98-109 | 41.9% |
| 90 | us:ratbot | 1374 +- 69 | 154 | 107-47 | 39.9% |
| 91 | us:s0u | 1349 +- 126 | 37 | 13-24 | 38.4% |
| 92 | us:s0m | 1337 +- 69 | 148 | 98-50 | 37.6% |
| 93 | us:s6c | 1312 +- 393 | 2 | 0-2 | 36.1% |
| 94 | XSercia_genies | 1311 +- 45 | 339 | 113-226 | 35.7% |
| 95 | us:s0e | 1285 +- 144 | 33 | 15-18 | 34.4% |
| 96 | C0derTang_v6 | 1277 +- 49 | 261 | 89-172 | 33.7% |
| 97 | Naton-Cai_ratplayer | 1244 +- 60 | 168 | 79-89 | 31.6% |
| 98 | ask8000_bumchit67 | 1238 +- 47 | 300 | 107-193 | 31.2% |
| 99 | justinottesen_alfredo | 1198 +- 54 | 210 | 95-115 | 28.7% |
| 100 | bc26-example | 1192 +- 35 | 556 | 212-344 | 28.4% |
| 101 | vincentwang60_javabot | 1166 +- 58 | 180 | 78-102 | 26.8% |
| 102 | Camleaf_mk1 | 1157 +- 58 | 183 | 77-106 | 26.2% |
| 103 | bc26-example-copy | 1126 +- 53 | 192 | 82-110 | 24.4% |
| 104 | Gavdingo_v1 | 1085 +- 52 | 213 | 89-124 | 22.0% |
| 105 | justinottesen_carbonara | 1081 +- 59 | 171 | 75-96 | 21.8% |
| 106 | battlecode-archive_result_408 | 1063 +- 57 | 180 | 78-102 | 20.8% |
| 107 | GuzziFlipFlops_Hermesv8 | 1044 +- 52 | 207 | 89-118 | 19.8% |
| 108 | j2399_weighted_micro_v9 | 1020 +- 62 | 168 | 68-100 | 18.5% |
| 109 | seru9_TOTAL_AGGRO | 962 +- 60 | 189 | 81-108 | 15.7% |
| 110 | quanticsoul4772_ratbot | 945 +- 58 | 189 | 76-113 | 14.8% |
| 111 | Victor-Ramirez-Garcia_battleca | 902 +- 63 | 189 | 71-118 | 13.0% |
| 112 | anicolao_myplayer | 687 +- 73 | 171 | 43-128 | 5.9% |
| 113 | j2399_weighted_path | 645 +- 81 | 117 | 35-82 | 4.9% |
| 114 | Krak9n_robot | 530 +- 87 | 132 | 20-112 | 2.7% |

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
| replica | +33 |
| sunandmoon | +17 |
| tornadoes | -23 |
| canyoudig | -28 |
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
