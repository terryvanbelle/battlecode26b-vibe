# Ladder

3323 rows of progress/games.csv (2892 local games, 431 replica matches), rated by a batch Bradley-Terry fit on the Elo scale (`tools/elolib.py`). A local game counts its points share (a 23-77 loss is 0.23 of a win), a replica match its games won. A game between one of our builds and a field bot also fits how hard its map is for us (table at the end), so builds gauntleted on different maps compare on an average map. Each pair of players counts at most 200 games, every player carries one virtual win and loss against a fixed anchor, and ratings are relative to a mean of 1500 over all 115 players. Each of our builds is its own player. Regenerate with `tools/elo.py`; do not edit.

**Caveat**: most of our gauntlets are against the six strongest field bots on a few maps, so a build is rated mostly on those maps; field-vs-field games are few, so the field order rests largely on its games against us. Record = games won and lost.

![ratings of our builds](elo.png)

Our builds, in attempt order (rating +- 95%; field score = expected score against each of the 60 field players, one game each):

| build | rating | rank | games | record | field score |
|---|---|---|---|---|---|
| s0e | 1611 +- 84 | 28 of 115 | 80 | 0-80 | 61.3% |
| s0g | 1522 +- 109 | 59 of 115 | 52 | 0-52 | 53.2% |
| s0h | 1486 +- 120 | 80 of 115 | 47 | 0-47 | 50.0% |
| s0i | 1501 +- 228 | 72 of 115 | 12 | 0-12 | 51.4% |
| s0j | 1489 +- 210 | 77 of 115 | 14 | 1-13 | 50.2% |
| s0m | 1577 +- 66 | 32 of 115 | 183 | 98-85 | 58.3% |
| s0n | 1505 +- 137 | 68 of 115 | 35 | 1-34 | 51.7% |
| s0o | 1569 +- 103 | 36 of 115 | 54 | 0-54 | 57.5% |
| s0p | 1568 +- 130 | 37 of 115 | 34 | 0-34 | 57.4% |
| s0q | 1561 +- 131 | 43 of 115 | 34 | 0-34 | 56.8% |
| s0r | 1567 +- 50 | 38 of 115 | 270 | 91-179 | 57.3% |
| s0s | 1559 +- 110 | 44 of 115 | 48 | 0-48 | 56.6% |
| s0t | 1566 +- 110 | 40 of 115 | 48 | 0-48 | 57.2% |
| s0u | 1534 +- 87 | 52 of 115 | 85 | 13-72 | 54.3% |
| v0d | 1612 +- 149 | 27 of 115 | 37 | 27-10 | 61.5% |
| s0y | 1445 +- 124 | 86 of 115 | 48 | 0-48 | 46.3% |
| s0z | 1553 +- 111 | 48 of 115 | 48 | 0-48 | 56.0% |
| s1a | 1516 +- 166 | 63 of 115 | 24 | 0-24 | 52.7% |
| s1b | 1493 +- 172 | 74 of 115 | 24 | 0-24 | 50.7% |
| s1c | 1510 +- 168 | 66 of 115 | 24 | 0-24 | 52.2% |
| s1d | 1527 +- 164 | 57 of 115 | 24 | 0-24 | 53.7% |
| s1e | 1463 +- 181 | 84 of 115 | 24 | 0-24 | 48.0% |
| s1f | 1489 +- 173 | 76 of 115 | 24 | 0-24 | 50.3% |
| s1g | 1550 +- 114 | 50 of 115 | 46 | 0-46 | 55.8% |
| s1h | 1580 +- 375 | 31 of 115 | 2 | 0-2 | 58.5% |
| s1l | 1510 +- 141 | 67 of 115 | 36 | 1-35 | 52.2% |
| s1n | 1533 +- 136 | 53 of 115 | 36 | 0-36 | 54.2% |
| s1q | 1510 +- 156 | 65 of 115 | 29 | 0-29 | 52.2% |
| s1s | 1493 +- 145 | 75 of 115 | 36 | 0-36 | 50.7% |
| s1w | 1521 +- 139 | 60 of 115 | 36 | 0-36 | 53.1% |
| s1x | 1496 +- 144 | 73 of 115 | 36 | 0-36 | 50.9% |
| s1y | 1502 +- 143 | 69 of 115 | 36 | 0-36 | 51.4% |
| s1z | 1488 +- 146 | 79 of 115 | 36 | 0-36 | 50.2% |
| s2c | 1485 +- 147 | 81 of 115 | 36 | 0-36 | 49.9% |
| s2e | 1516 +- 140 | 62 of 115 | 36 | 0-36 | 52.7% |
| s2f | 1522 +- 139 | 58 of 115 | 36 | 0-36 | 53.2% |
| s2i | 1502 +- 143 | 70 of 115 | 36 | 0-36 | 51.4% |
| s2j | 1468 +- 151 | 82 of 115 | 36 | 0-36 | 48.4% |
| s2k | 1488 +- 155 | 78 of 115 | 32 | 0-32 | 50.2% |
| s2l | 1531 +- 383 | 55 of 115 | 2 | 0-2 | 54.0% |
| s2m | 1551 +- 133 | 49 of 115 | 36 | 0-36 | 55.9% |
| s2n | 1431 +- 162 | 88 of 115 | 36 | 0-36 | 45.0% |
| s2o | 1591 +- 108 | 30 of 115 | 57 | 12-45 | 59.5% |
| s2p | 1515 +- 140 | 64 of 115 | 36 | 0-36 | 52.6% |
| s2q | 1540 +- 135 | 51 of 115 | 36 | 0-36 | 54.9% |
| s2r | 1532 +- 137 | 54 of 115 | 36 | 0-36 | 54.2% |
| s2s | 1572 +- 94 | 34 of 115 | 72 | 2-70 | 57.8% |
| s2t | 1576 +- 53 | 33 of 115 | 244 | 9-235 | 58.1% |
| s2u | 1528 +- 137 | 56 of 115 | 36 | 0-36 | 53.8% |
| s2v | 1553 +- 133 | 47 of 115 | 36 | 0-36 | 56.1% |
| s2w | 1553 +- 133 | 46 of 115 | 36 | 0-36 | 56.1% |
| s2y | 1520 +- 92 | 61 of 115 | 96 | 3-93 | 53.1% |
| s2z | 1566 +- 85 | 39 of 115 | 96 | 6-90 | 57.2% |
| s3a | 1569 +- 85 | 35 of 115 | 96 | 2-94 | 57.5% |
| ratbot | 1561 +- 65 | 42 of 115 | 187 | 122-65 | 56.8% |

All players:

| # | player | rating | games | record | field score |
|---:|---|---|---|---|---|
| 1 | uravt_Version35 | 1925 +- 89 | 80 | 65-15 | 86.6% |
| 2 | IvanGeffner_basic51 | 1902 +- 103 | 61 | 46-15 | 85.2% |
| 3 | awu7_awubot_us | 1834 +- 111 | 58 | 45-13 | 80.5% |
| 4 | daannte_v52_ThisGuy | 1811 +- 40 | 459 | 435-24 | 78.8% |
| 5 | abcdezyxwv_bestsofar | 1809 +- 141 | 34 | 27-7 | 78.6% |
| 6 | uravt_Version41 | 1808 +- 32 | 752 | 738-14 | 78.5% |
| 7 | battlecode-archive_testplayer | 1806 +- 107 | 55 | 39-16 | 78.4% |
| 8 | nzjt_TheComplexMerlin | 1801 +- 137 | 46 | 38-8 | 78.0% |
| 9 | awu7_awubot_quals | 1800 +- 114 | 43 | 29-14 | 78.0% |
| 10 | quesswho_bot16 | 1790 +- 96 | 61 | 41-20 | 77.1% |
| 11 | thewaxmango_PhD | 1788 +- 142 | 46 | 39-7 | 77.0% |
| 12 | nubbbbbb_testplayer | 1774 +- 128 | 37 | 26-11 | 75.8% |
| 13 | daannte_v47_Skadoosh | 1771 +- 81 | 96 | 67-29 | 75.6% |
| 14 | AdamTan12_Icarus | 1764 +- 45 | 345 | 304-41 | 75.0% |
| 15 | AlexT101_finalsbot | 1736 +- 39 | 436 | 398-38 | 72.7% |
| 16 | quesswho_quals | 1728 +- 45 | 314 | 296-18 | 72.0% |
| 17 | TeamSPAARK_SPAARK | 1724 +- 95 | 64 | 38-26 | 71.7% |
| 18 | HugoIngelsson_Xzibit | 1715 +- 137 | 49 | 41-8 | 70.9% |
| 19 | AdamTan12_Finals | 1687 +- 42 | 346 | 315-31 | 68.4% |
| 20 | thewaxmango_collegejunior | 1682 +- 124 | 49 | 36-13 | 67.9% |
| 21 | TeamSPAARK_TSPAARKJAN25 | 1669 +- 293 | 4 | 4-0 | 66.8% |
| 22 | bman12345678_goonv23 | 1665 +- 160 | 43 | 38-5 | 66.5% |
| 23 | AlexT101_usqualsbot | 1664 +- 105 | 56 | 35-21 | 66.3% |
| 24 | HugoIngelsson_YokoOno | 1651 +- 116 | 49 | 32-17 | 65.2% |
| 25 | outercloudstudio_v_1_18 | 1648 +- 157 | 37 | 32-5 | 64.9% |
| 26 | VarunVejalla_refactored_final3 | 1623 +- 284 | 4 | 4-0 | 62.6% |
| 27 | us:v0d | 1612 +- 149 | 37 | 27-10 | 61.5% |
| 28 | us:s0e | 1611 +- 84 | 80 | 0-80 | 61.3% |
| 29 | VarunVejalla_final4_run_away10 | 1591 +- 155 | 37 | 28-9 | 59.7% |
| 30 | us:s2o | 1591 +- 108 | 57 | 12-45 | 59.5% |
| 31 | us:s1h | 1580 +- 375 | 2 | 0-2 | 58.5% |
| 32 | us:s0m | 1577 +- 66 | 183 | 98-85 | 58.3% |
| 33 | us:s2t | 1576 +- 53 | 244 | 9-235 | 58.1% |
| 34 | us:s2s | 1572 +- 94 | 72 | 2-70 | 57.8% |
| 35 | us:s3a | 1569 +- 85 | 96 | 2-94 | 57.5% |
| 36 | us:s0o | 1569 +- 103 | 54 | 0-54 | 57.5% |
| 37 | us:s0p | 1568 +- 130 | 34 | 0-34 | 57.4% |
| 38 | us:s0r | 1567 +- 50 | 270 | 91-179 | 57.3% |
| 39 | us:s2z | 1566 +- 85 | 96 | 6-90 | 57.2% |
| 40 | us:s0t | 1566 +- 110 | 48 | 0-48 | 57.2% |
| 41 | spsquared_TSPAARK_QUALS | 1565 +- 139 | 37 | 21-16 | 57.3% |
| 42 | us:ratbot | 1561 +- 65 | 187 | 122-65 | 56.8% |
| 43 | us:s0q | 1561 +- 131 | 34 | 0-34 | 56.8% |
| 44 | us:s0s | 1559 +- 110 | 48 | 0-48 | 56.6% |
| 45 | StuxnetStudios_skaven | 1559 +- 280 | 4 | 0-4 | 56.7% |
| 46 | us:s2w | 1553 +- 133 | 36 | 0-36 | 56.1% |
| 47 | us:s2v | 1553 +- 133 | 36 | 0-36 | 56.1% |
| 48 | us:s0z | 1553 +- 111 | 48 | 0-48 | 56.0% |
| 49 | us:s2m | 1551 +- 133 | 36 | 0-36 | 55.9% |
| 50 | us:s1g | 1550 +- 114 | 46 | 0-46 | 55.8% |
| 51 | us:s2q | 1540 +- 135 | 36 | 0-36 | 54.9% |
| 52 | us:s0u | 1534 +- 87 | 85 | 13-72 | 54.3% |
| 53 | us:s1n | 1533 +- 136 | 36 | 0-36 | 54.2% |
| 54 | us:s2r | 1532 +- 137 | 36 | 0-36 | 54.2% |
| 55 | us:s2l | 1531 +- 383 | 2 | 0-2 | 54.0% |
| 56 | us:s2u | 1528 +- 137 | 36 | 0-36 | 53.8% |
| 57 | us:s1d | 1527 +- 164 | 24 | 0-24 | 53.7% |
| 58 | us:s2f | 1522 +- 139 | 36 | 0-36 | 53.2% |
| 59 | us:s0g | 1522 +- 109 | 52 | 0-52 | 53.2% |
| 60 | us:s1w | 1521 +- 139 | 36 | 0-36 | 53.1% |
| 61 | us:s2y | 1520 +- 92 | 96 | 3-93 | 53.1% |
| 62 | us:s2e | 1516 +- 140 | 36 | 0-36 | 52.7% |
| 63 | us:s1a | 1516 +- 166 | 24 | 0-24 | 52.7% |
| 64 | us:s2p | 1515 +- 140 | 36 | 0-36 | 52.6% |
| 65 | us:s1q | 1510 +- 156 | 29 | 0-29 | 52.2% |
| 66 | us:s1c | 1510 +- 168 | 24 | 0-24 | 52.2% |
| 67 | us:s1l | 1510 +- 141 | 36 | 1-35 | 52.2% |
| 68 | us:s0n | 1505 +- 137 | 35 | 1-34 | 51.7% |
| 69 | us:s1y | 1502 +- 143 | 36 | 0-36 | 51.4% |
| 70 | us:s2i | 1502 +- 143 | 36 | 0-36 | 51.4% |
| 71 | amcsz_fighterrats | 1502 +- 133 | 37 | 17-20 | 51.4% |
| 72 | us:s0i | 1501 +- 228 | 12 | 0-12 | 51.4% |
| 73 | us:s1x | 1496 +- 144 | 36 | 0-36 | 50.9% |
| 74 | us:s1b | 1493 +- 172 | 24 | 0-24 | 50.7% |
| 75 | us:s1s | 1493 +- 145 | 36 | 0-36 | 50.7% |
| 76 | us:s1f | 1489 +- 173 | 24 | 0-24 | 50.3% |
| 77 | us:s0j | 1489 +- 210 | 14 | 1-13 | 50.2% |
| 78 | us:s2k | 1488 +- 155 | 32 | 0-32 | 50.2% |
| 79 | us:s1z | 1488 +- 146 | 36 | 0-36 | 50.2% |
| 80 | us:s0h | 1486 +- 120 | 47 | 0-47 | 50.0% |
| 81 | us:s2c | 1485 +- 147 | 36 | 0-36 | 49.9% |
| 82 | us:s2j | 1468 +- 151 | 36 | 0-36 | 48.4% |
| 83 | CS50Crab_rapierV1 | 1466 +- 109 | 58 | 33-25 | 48.2% |
| 84 | us:s1e | 1463 +- 181 | 24 | 0-24 | 48.0% |
| 85 | OmarAyman33_pre_nav_2 | 1457 +- 291 | 4 | 0-4 | 47.4% |
| 86 | us:s0y | 1445 +- 124 | 48 | 0-48 | 46.3% |
| 87 | PixelRifts_mushakraj_mk3 | 1434 +- 126 | 55 | 28-27 | 45.3% |
| 88 | us:s2n | 1431 +- 162 | 36 | 0-36 | 45.0% |
| 89 | CS50Crab_protoSaberV7 | 1405 +- 110 | 58 | 25-33 | 42.7% |
| 90 | TheEvergreenStateCollege_immortal_chariot | 1364 +- 323 | 4 | 0-4 | 39.0% |
| 91 | XSercia_genies | 1353 +- 102 | 85 | 22-63 | 38.0% |
| 92 | Naton-Cai_ratplayer | 1346 +- 133 | 43 | 17-26 | 37.5% |
| 93 | C0derTang_v6 | 1345 +- 113 | 46 | 21-25 | 37.3% |
| 94 | Victor-Ramirez-Garcia_battlecats | 1336 +- 337 | 4 | 0-4 | 36.6% |
| 95 | bc26vibe_bot | 1328 +- 124 | 36 | 22-14 | 35.9% |
| 96 | ask8000_bumchit67 | 1281 +- 107 | 58 | 21-37 | 31.9% |
| 97 | winkelmantanner_genetic1 | 1276 +- 374 | 4 | 0-4 | 31.5% |
| 98 | LL-OV_Astro | 1276 +- 374 | 4 | 0-4 | 31.5% |
| 99 | examplefuncsplayer | 1273 +- 48 | 370 | 131-239 | 31.2% |
| 100 | bc26-example | 1235 +- 92 | 90 | 26-64 | 28.2% |
| 101 | justinottesen_alfredo | 1234 +- 109 | 64 | 22-42 | 28.1% |
| 102 | justinottesen_carbonara | 1230 +- 152 | 37 | 9-28 | 27.7% |
| 103 | vincentwang60_javabot | 1214 +- 138 | 46 | 13-33 | 26.6% |
| 104 | Camleaf_mk1 | 1211 +- 121 | 55 | 20-35 | 26.3% |
| 105 | j2399_weighted_micro_v9 | 1209 +- 138 | 43 | 15-28 | 26.1% |
| 106 | GuzziFlipFlops_Hermesv8 | 1201 +- 117 | 52 | 19-33 | 25.5% |
| 107 | bc26-example-copy | 1157 +- 103 | 60 | 20-40 | 22.3% |
| 108 | battlecode-archive_result_408 | 1156 +- 139 | 49 | 7-42 | 22.3% |
| 109 | Gavdingo_v1 | 1136 +- 114 | 61 | 16-45 | 20.8% |
| 110 | seru9_TOTAL_AGGRO | 1124 +- 121 | 58 | 15-43 | 20.0% |
| 111 | quanticsoul4772_ratbot | 1040 +- 159 | 46 | 6-40 | 14.9% |
| 112 | Victor-Ramirez-Garcia_battleca | 987 +- 126 | 60 | 10-50 | 12.1% |
| 113 | j2399_weighted_path | 921 +- 272 | 25 | 1-24 | 9.2% |
| 114 | anicolao_myplayer | 855 +- 184 | 58 | 3-55 | 6.8% |
| 115 | Krak9n_robot | 787 +- 249 | 46 | 1-45 | 4.8% |

Map handicap for our builds (Elo points; + = we score better there than on an average map of the mix, replica = the replica's map rotation):

| map | handicap |
|---|---:|
| rift | +271 |
| knifefight | +223 |
| minimaze | +135 |
| UncomfortablyClose | +131 |
| TheHeist | +125 |
| constrictinglattice | +123 |
| DefaultLarge | +111 |
| uneruesansfin | +92 |
| sittingducks | +81 |
| mercifullattice | +80 |
| averystrangespace | +78 |
| cheeseguardians | +71 |
| superioristhmus | +67 |
| dirtpassageway | +65 |
| hatefullattice | +65 |
| ratsareants | +64 |
| streetsofnewyork | +57 |
| keepout | +56 |
| corridorofdoomanddespair | +53 |
| jail | +45 |
| safelycontained | +40 |
| thunderdome | +36 |
| uneasy_alliance | +33 |
| evileye | +29 |
| tornadoes | +25 |
| DefaultMedium | +25 |
| peaceinourtime | +21 |
| clouds | +17 |
| arrows | +15 |
| DefaultSmall | +13 |
| sunandmoon | +8 |
| dirtfulcat | +5 |
| houseofcards | +3 |
| starvation | +3 |
| groveisbeingdesecrated | -3 |
| ZeroDay | -6 |
| attack_on_cat | -6 |
| heart | -8 |
| rose | -9 |
| where_are_you | -10 |
| diamond | -11 |
| cheesebottles | -12 |
| seepastyourgreed | -12 |
| FreeTheCheese | -13 |
| learn_your_lesson | -29 |
| t_minus_five_minutes | -32 |
| Nofreecheese | -37 |
| trapped | -37 |
| Meow | -45 |
| krotus | -45 |
| popthecork | -50 |
| tiny | -52 |
| bed | -58 |
| Hike | -61 |
| toomuchcheese | -63 |
| fullofsecrets | -64 |
| whereisthecheese | -66 |
| mincemeat | -67 |
| averyfineline | -68 |
| 5t4rv4t10n_1337 | -70 |
| EscapeTheNight | -71 |
| externalsustenance | -71 |
| pipes | -72 |
| ultimatestarvation | -73 |
| canyoudig | -74 |
| wallsofparadis | -76 |
| RUN | -78 |
| gridlock | -86 |
| Excavation | -87 |
| whatsthecatdoin | -88 |
| closeup | -95 |
| cheesefarm | -100 |
| Stash | -115 |
| replica | -152 |
| clockwisdom | -304 |
