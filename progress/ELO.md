# Ladder

851 rows of progress/games.csv (370 local games, 481 replica matches), rated by a batch Bradley-Terry fit on the Elo scale (`tools/elolib.py`). A local game counts its cell score (`tools/score.py`: a win or loss when kings decide the game, the points share when points do), a replica match its games won. A game between one of our builds and a field bot also fits how hard its map is for us (table at the end), so builds gauntleted on different maps compare on an average map. Each pair of players counts at most 200 games, every player carries one virtual win and loss against a fixed anchor, and ratings are relative to a mean of 1500 over all 57 players. Each of our builds is its own player. Regenerate with `tools/elo.py`; do not edit.

Games against field bots are replica scrimmages only; local games are our builds against each other and the stock players. Record = games won and lost.

![ratings of our builds](elo.png)

Our builds, in attempt order (rating +- 95%; field score = expected score against each of the 51 field players, one game each):

| build | rating | rank | games | record | field score |
|---|---|---|---|---|---|
| s0e | 1249 +- 139 | 43 of 57 | 33 | 15-18 | 28.8% |
| s0m | 1368 +- 69 | 35 of 57 | 148 | 98-50 | 37.5% |
| s0u | 1375 +- 126 | 34 of 57 | 37 | 13-24 | 38.1% |
| v0d | 1664 +- 127 | 24 of 57 | 37 | 27-10 | 61.2% |
| s2o | 1356 +- 96 | 36 of 57 | 69 | 32-37 | 36.7% |
| ratbot | 1403 +- 69 | 32 of 57 | 154 | 107-47 | 40.3% |

All players:

| # | player | rating | games | record | field score |
|---:|---|---|---|---|---|
| 1 | uravt_Version41 | 2036 +- 158 | 48 | 43-5 | 88.6% |
| 2 | uravt_Version35 | 2007 +- 97 | 66 | 49-17 | 87.0% |
| 3 | IvanGeffner_basic51 | 1984 +- 107 | 60 | 45-15 | 85.7% |
| 4 | awu7_awubot_us | 1925 +- 117 | 57 | 43-14 | 81.9% |
| 5 | quesswho_quals | 1920 +- 102 | 60 | 42-18 | 81.6% |
| 6 | nzjt_TheComplexMerlin | 1897 +- 140 | 48 | 40-8 | 80.0% |
| 7 | daannte_v52_ThisGuy | 1872 +- 88 | 75 | 45-30 | 78.1% |
| 8 | abcdezyxwv_bestsofar | 1869 +- 150 | 39 | 32-7 | 78.0% |
| 9 | awu7_awubot_quals | 1862 +- 123 | 39 | 25-14 | 77.4% |
| 10 | quesswho_bot16 | 1858 +- 98 | 60 | 38-22 | 77.1% |
| 11 | battlecode-archive_testplayer | 1843 +- 110 | 54 | 37-17 | 76.0% |
| 12 | daannte_v47_Skadoosh | 1841 +- 92 | 81 | 49-32 | 75.8% |
| 13 | thewaxmango_PhD | 1837 +- 164 | 42 | 35-7 | 75.5% |
| 14 | nubbbbbb_testplayer | 1807 +- 138 | 36 | 25-11 | 73.2% |
| 15 | TeamSPAARK_SPAARK | 1758 +- 96 | 66 | 35-31 | 69.2% |
| 16 | AdamTan12_Icarus | 1753 +- 92 | 72 | 40-32 | 68.8% |
| 17 | bman12345678_goonv23 | 1736 +- 158 | 51 | 45-6 | 67.4% |
| 18 | AlexT101_finalsbot | 1709 +- 101 | 66 | 31-35 | 65.1% |
| 19 | HugoIngelsson_YokoOno | 1703 +- 123 | 51 | 34-17 | 64.6% |
| 20 | AlexT101_usqualsbot | 1701 +- 112 | 60 | 36-24 | 64.5% |
| 21 | HugoIngelsson_Xzibit | 1701 +- 136 | 48 | 37-11 | 64.5% |
| 22 | AdamTan12_Finals | 1694 +- 109 | 63 | 34-29 | 63.9% |
| 23 | thewaxmango_collegejunior | 1679 +- 116 | 54 | 34-20 | 62.7% |
| 24 | us:v0d | 1664 +- 127 | 37 | 27-10 | 61.2% |
| 25 | outercloudstudio_v_1_18 | 1647 +- 173 | 36 | 31-5 | 60.0% |
| 26 | spsquared_TSPAARK_QUALS | 1591 +- 150 | 39 | 22-17 | 55.3% |
| 27 | VarunVejalla_final4_run_away10 | 1576 +- 178 | 33 | 24-9 | 54.1% |
| 28 | amcsz_fighterrats | 1478 +- 139 | 39 | 14-25 | 46.0% |
| 29 | CS50Crab_protoSaberV7 | 1455 +- 100 | 66 | 31-35 | 44.2% |
| 30 | CS50Crab_rapierV1 | 1449 +- 100 | 69 | 33-36 | 43.7% |
| 31 | PixelRifts_mushakraj_mk3 | 1436 +- 118 | 60 | 30-30 | 42.6% |
| 32 | us:ratbot | 1403 +- 69 | 154 | 107-47 | 40.3% |
| 33 | XSercia_genies | 1391 +- 106 | 81 | 22-59 | 39.1% |
| 34 | us:s0u | 1375 +- 126 | 37 | 13-24 | 38.1% |
| 35 | us:s0m | 1368 +- 69 | 148 | 98-50 | 37.5% |
| 36 | us:s2o | 1356 +- 96 | 69 | 32-37 | 36.7% |
| 37 | Naton-Cai_ratplayer | 1353 +- 143 | 39 | 17-22 | 36.1% |
| 38 | C0derTang_v6 | 1349 +- 113 | 45 | 23-22 | 35.8% |
| 39 | bc26vibe_bot | 1310 +- 117 | 42 | 26-16 | 32.9% |
| 40 | ask8000_bumchit67 | 1294 +- 93 | 75 | 27-48 | 31.7% |
| 41 | bc26-example | 1278 +- 40 | 460 | 157-303 | 30.4% |
| 42 | justinottesen_alfredo | 1267 +- 98 | 75 | 27-48 | 29.6% |
| 43 | us:s0e | 1249 +- 139 | 33 | 15-18 | 28.8% |
| 44 | Camleaf_mk1 | 1227 +- 123 | 54 | 20-34 | 26.7% |
| 45 | justinottesen_carbonara | 1217 +- 163 | 33 | 9-24 | 26.0% |
| 46 | j2399_weighted_micro_v9 | 1202 +- 139 | 48 | 15-33 | 25.0% |
| 47 | vincentwang60_javabot | 1190 +- 135 | 48 | 16-32 | 24.2% |
| 48 | bc26-example-copy | 1182 +- 99 | 66 | 21-45 | 23.6% |
| 49 | GuzziFlipFlops_Hermesv8 | 1177 +- 112 | 54 | 21-33 | 23.3% |
| 50 | Gavdingo_v1 | 1168 +- 111 | 60 | 17-43 | 22.7% |
| 51 | seru9_TOTAL_AGGRO | 1150 +- 123 | 54 | 15-39 | 21.5% |
| 52 | battlecode-archive_result_408 | 1144 +- 146 | 45 | 7-38 | 21.1% |
| 53 | Victor-Ramirez-Garcia_battleca | 1019 +- 117 | 66 | 12-54 | 13.9% |
| 54 | quanticsoul4772_ratbot | 995 +- 168 | 42 | 5-37 | 12.7% |
| 55 | j2399_weighted_path | 914 +- 281 | 21 | 1-20 | 9.1% |
| 56 | anicolao_myplayer | 851 +- 182 | 60 | 3-57 | 6.9% |
| 57 | Krak9n_robot | 788 +- 250 | 42 | 1-41 | 5.0% |

Map handicap for our builds (Elo points; + = we score better there than on an average map of the mix, replica = the replica's map rotation):

| map | handicap |
|---|---:|
| DefaultLarge | +234 |
| DefaultMedium | +234 |
| DefaultSmall | +234 |
| EscapeTheNight | +234 |
| Meow | +234 |
| averystrangespace | +234 |
| constrictinglattice | +234 |
| corridorofdoomanddespair | +234 |
| dirtpassageway | +234 |
| hatefullattice | +234 |
| keepout | +234 |
| mercifullattice | +234 |
| minimaze | +234 |
| peaceinourtime | +234 |
| pipes | +234 |
| ratsareants | +234 |
| rift | +234 |
| safelycontained | +234 |
| sittingducks | +234 |
| streetsofnewyork | +234 |
| superioristhmus | +234 |
| trapped | +234 |
| Excavation | +204 |
| uneasy_alliance | +204 |
| wallsofparadis | +204 |
| externalsustenance | +176 |
| jail | +176 |
| Nofreecheese | +151 |
| cheesefarm | +151 |
| evileye | +151 |
| starvation | +151 |
| ZeroDay | +127 |
| bed | +127 |
| closeup | +127 |
| clouds | +127 |
| dirtfulcat | +127 |
| gridlock | +127 |
| knifefight | +127 |
| thunderdome | +127 |
| toomuchcheese | +127 |
| FreeTheCheese | +110 |
| cheeseguardians | +110 |
| replica | +105 |
| houseofcards | +92 |
| sunandmoon | +69 |
| tornadoes | +30 |
| canyoudig | +25 |
| TheHeist | -0 |
| groveisbeingdesecrated | -0 |
| rose | -0 |
| where_are_you | -57 |
| 5t4rv4t10n_1337 | -93 |
| diamond | -98 |
| arrows | -137 |
| uneruesansfin | -182 |
| attack_on_cat | -197 |
| heart | -197 |
| seepastyourgreed | -197 |
| learn_your_lesson | -206 |
| popthecork | -206 |
| krotus | -219 |
| Stash | -245 |
| UncomfortablyClose | -245 |
| cheesebottles | -295 |
| mincemeat | -295 |
| averyfineline | -325 |
| t_minus_five_minutes | -346 |
| tiny | -346 |
| ultimatestarvation | -346 |
| whatsthecatdoin | -346 |
| whereisthecheese | -346 |
| Hike | -404 |
| RUN | -404 |
| clockwisdom | -561 |
| fullofsecrets | -561 |
