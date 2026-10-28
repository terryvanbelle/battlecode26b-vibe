# Ladder

1402 rows of progress/games.csv (790 local games, 612 replica matches), rated by a batch Bradley-Terry fit on the Elo scale (`tools/elolib.py`). A local game counts its cell score (`tools/score.py`: a win or loss when kings decide the game, the points share when points do), a replica match its games won. A game between one of our builds and a field bot also fits how hard its map is for us (table at the end), so builds gauntleted on different maps compare on an average map. Each pair of players counts at most 200 games, every player carries one virtual win and loss against a fixed anchor, and ratings are relative to a mean of 1500 over all 63 players. Each of our builds is its own player. Regenerate with `tools/elo.py`; do not edit.

Games against field bots are replica scrimmages only; local games are our builds against each other and the stock players. Record = games won and lost.

![ratings of our builds](elo.png)

Our builds, in attempt order (rating +- 95%; field score = expected score against each of the 51 field players, one game each):

| build | rating | rank | games | record | field score |
|---|---|---|---|---|---|
| s0e | 1341 +- 138 | 45 of 63 | 33 | 15-18 | 35.9% |
| s0m | 1385 +- 69 | 41 of 63 | 148 | 98-50 | 39.1% |
| s0u | 1394 +- 126 | 40 of 63 | 37 | 13-24 | 39.7% |
| v0d | 1683 +- 128 | 23 of 63 | 37 | 27-10 | 62.1% |
| s2o | 1474 +- 30 | 31 of 63 | 558 | 293-265 | 45.8% |
| s3e | 1438 +- 76 | 35 of 63 | 80 | 29-51 | 43.1% |
| s3f | 1496 +- 75 | 28 of 63 | 80 | 46-34 | 47.5% |
| s3g | 1438 +- 76 | 36 of 63 | 80 | 30-50 | 43.1% |
| s2o_id | 1474 +- 75 | 30 of 63 | 80 | 40-40 | 45.8% |
| s3h | 1481 +- 75 | 29 of 63 | 80 | 43-37 | 46.3% |
| s3i | 1467 +- 145 | 33 of 63 | 20 | 6-14 | 45.2% |
| ratbot | 1422 +- 69 | 39 of 63 | 154 | 107-47 | 41.9% |

All players:

| # | player | rating | games | record | field score |
|---:|---|---|---|---|---|
| 1 | uravt_Version35 | 2047 +- 89 | 78 | 57-21 | 88.2% |
| 2 | uravt_Version41 | 2043 +- 125 | 60 | 50-10 | 88.0% |
| 3 | IvanGeffner_basic51 | 2026 +- 96 | 72 | 53-19 | 87.1% |
| 4 | awu7_awubot_us | 1975 +- 104 | 69 | 52-17 | 84.1% |
| 5 | quesswho_quals | 1941 +- 92 | 72 | 49-23 | 81.9% |
| 6 | quesswho_bot16 | 1909 +- 91 | 72 | 48-24 | 79.7% |
| 7 | daannte_v52_ThisGuy | 1898 +- 82 | 87 | 51-36 | 78.9% |
| 8 | nzjt_TheComplexMerlin | 1895 +- 116 | 60 | 44-16 | 78.7% |
| 9 | daannte_v47_Skadoosh | 1881 +- 86 | 96 | 61-35 | 77.7% |
| 10 | awu7_awubot_quals | 1878 +- 106 | 57 | 39-18 | 77.5% |
| 11 | battlecode-archive_testplayer | 1867 +- 97 | 69 | 47-22 | 76.7% |
| 12 | abcdezyxwv_bestsofar | 1843 +- 114 | 63 | 48-15 | 74.9% |
| 13 | thewaxmango_PhD | 1841 +- 133 | 54 | 39-15 | 74.8% |
| 14 | nubbbbbb_testplayer | 1835 +- 116 | 48 | 32-16 | 74.3% |
| 15 | TeamSPAARK_SPAARK | 1800 +- 91 | 78 | 46-32 | 71.6% |
| 16 | bman12345678_goonv23 | 1787 +- 135 | 63 | 51-12 | 70.6% |
| 17 | AdamTan12_Icarus | 1770 +- 87 | 84 | 49-35 | 69.2% |
| 18 | AlexT101_finalsbot | 1741 +- 93 | 81 | 43-38 | 66.9% |
| 19 | AlexT101_usqualsbot | 1731 +- 94 | 78 | 45-33 | 66.2% |
| 20 | AdamTan12_Finals | 1701 +- 97 | 78 | 39-39 | 63.7% |
| 21 | HugoIngelsson_YokoOno | 1700 +- 108 | 63 | 39-24 | 63.7% |
| 22 | thewaxmango_collegejunior | 1688 +- 99 | 72 | 45-27 | 62.7% |
| 23 | us:v0d | 1683 +- 128 | 37 | 27-10 | 62.1% |
| 24 | HugoIngelsson_Xzibit | 1672 +- 111 | 63 | 41-22 | 61.4% |
| 25 | spsquared_TSPAARK_QUALS | 1582 +- 127 | 54 | 30-24 | 54.2% |
| 26 | outercloudstudio_v_1_18 | 1578 +- 123 | 54 | 35-19 | 53.9% |
| 27 | VarunVejalla_final4_run_away10 | 1576 +- 146 | 45 | 28-17 | 53.7% |
| 28 | us:s3f | 1496 +- 75 | 80 | 46-34 | 47.5% |
| 29 | us:s3h | 1481 +- 75 | 80 | 43-37 | 46.3% |
| 30 | us:s2o_id | 1474 +- 75 | 80 | 40-40 | 45.8% |
| 31 | us:s2o | 1474 +- 30 | 558 | 293-265 | 45.8% |
| 32 | amcsz_fighterrats | 1469 +- 119 | 51 | 21-30 | 45.3% |
| 33 | us:s3i | 1467 +- 145 | 20 | 6-14 | 45.2% |
| 34 | CS50Crab_protoSaberV7 | 1466 +- 90 | 78 | 40-38 | 45.1% |
| 35 | us:s3e | 1438 +- 76 | 80 | 29-51 | 43.1% |
| 36 | us:s3g | 1438 +- 76 | 80 | 30-50 | 43.1% |
| 37 | CS50Crab_rapierV1 | 1436 +- 91 | 81 | 37-44 | 42.8% |
| 38 | PixelRifts_mushakraj_mk3 | 1431 +- 106 | 75 | 34-41 | 42.4% |
| 39 | us:ratbot | 1422 +- 69 | 154 | 107-47 | 41.9% |
| 40 | us:s0u | 1394 +- 126 | 37 | 13-24 | 39.7% |
| 41 | us:s0m | 1385 +- 69 | 148 | 98-50 | 39.1% |
| 42 | XSercia_genies | 1384 +- 97 | 99 | 34-65 | 38.8% |
| 43 | bc26vibe_bot | 1362 +- 100 | 63 | 35-28 | 37.2% |
| 44 | C0derTang_v6 | 1347 +- 102 | 57 | 25-32 | 36.0% |
| 45 | us:s0e | 1341 +- 138 | 33 | 15-18 | 35.9% |
| 46 | Naton-Cai_ratplayer | 1321 +- 123 | 51 | 19-32 | 34.1% |
| 47 | ask8000_bumchit67 | 1292 +- 84 | 90 | 36-54 | 31.9% |
| 48 | justinottesen_alfredo | 1276 +- 88 | 93 | 37-56 | 30.8% |
| 49 | bc26-example | 1243 +- 39 | 472 | 164-308 | 28.4% |
| 50 | vincentwang60_javabot | 1230 +- 111 | 60 | 24-36 | 27.5% |
| 51 | justinottesen_carbonara | 1216 +- 127 | 45 | 14-31 | 26.6% |
| 52 | Camleaf_mk1 | 1203 +- 103 | 72 | 26-46 | 25.7% |
| 53 | j2399_weighted_micro_v9 | 1188 +- 121 | 60 | 22-38 | 24.7% |
| 54 | bc26-example-copy | 1171 +- 89 | 78 | 27-51 | 23.5% |
| 55 | Gavdingo_v1 | 1137 +- 98 | 78 | 22-56 | 21.4% |
| 56 | battlecode-archive_result_408 | 1134 +- 118 | 57 | 14-43 | 21.2% |
| 57 | GuzziFlipFlops_Hermesv8 | 1115 +- 95 | 75 | 24-51 | 20.0% |
| 58 | seru9_TOTAL_AGGRO | 1112 +- 108 | 66 | 20-46 | 19.8% |
| 59 | quanticsoul4772_ratbot | 1042 +- 133 | 54 | 14-40 | 16.0% |
| 60 | Victor-Ramirez-Garcia_battleca | 1035 +- 102 | 81 | 21-60 | 15.6% |
| 61 | anicolao_myplayer | 808 +- 158 | 72 | 6-66 | 6.6% |
| 62 | j2399_weighted_path | 775 +- 256 | 33 | 1-32 | 5.7% |
| 63 | Krak9n_robot | 611 +- 257 | 54 | 1-53 | 2.5% |

Map handicap for our builds (Elo points; + = we score better there than on an average map of the mix, replica = the replica's map rotation):

| map | handicap |
|---|---:|
| DefaultLarge | +184 |
| DefaultMedium | +184 |
| DefaultSmall | +184 |
| EscapeTheNight | +184 |
| Meow | +184 |
| averystrangespace | +184 |
| constrictinglattice | +184 |
| corridorofdoomanddespair | +184 |
| dirtpassageway | +184 |
| hatefullattice | +184 |
| keepout | +184 |
| mercifullattice | +184 |
| minimaze | +184 |
| peaceinourtime | +184 |
| pipes | +184 |
| ratsareants | +184 |
| rift | +184 |
| safelycontained | +184 |
| sittingducks | +184 |
| streetsofnewyork | +184 |
| superioristhmus | +184 |
| trapped | +184 |
| Excavation | +154 |
| uneasy_alliance | +154 |
| wallsofparadis | +154 |
| externalsustenance | +126 |
| jail | +126 |
| Nofreecheese | +100 |
| cheesefarm | +100 |
| evileye | +100 |
| starvation | +100 |
| ZeroDay | +76 |
| bed | +76 |
| closeup | +76 |
| clouds | +76 |
| dirtfulcat | +76 |
| gridlock | +76 |
| knifefight | +76 |
| thunderdome | +76 |
| toomuchcheese | +76 |
| FreeTheCheese | +59 |
| cheeseguardians | +59 |
| houseofcards | +41 |
| sunandmoon | +19 |
| replica | +12 |
| tornadoes | -21 |
| canyoudig | -26 |
| TheHeist | -52 |
| groveisbeingdesecrated | -52 |
| rose | -52 |
| where_are_you | -108 |
| 5t4rv4t10n_1337 | -144 |
| diamond | -150 |
| arrows | -189 |
| uneruesansfin | -234 |
| attack_on_cat | -249 |
| heart | -249 |
| seepastyourgreed | -249 |
| learn_your_lesson | -258 |
| popthecork | -258 |
| krotus | -271 |
| Stash | -297 |
| UncomfortablyClose | -297 |
| cheesebottles | -348 |
| mincemeat | -348 |
| averyfineline | -378 |
| t_minus_five_minutes | -398 |
| tiny | -398 |
| ultimatestarvation | -398 |
| whatsthecatdoin | -398 |
| whereisthecheese | -398 |
| Hike | -456 |
| RUN | -456 |
| clockwisdom | -614 |
| fullofsecrets | -614 |
