# Ladder

2492 rows of progress/games.csv (1616 local games, 876 replica matches), rated by a batch Bradley-Terry fit on the Elo scale (`tools/elolib.py`). A local game counts its cell score (`tools/score.py`: a win or loss when kings decide the game, the points share when points do), a replica match its games won. A game between one of our builds and a field bot also fits how hard its map is for us (table at the end), so builds gauntleted on different maps compare on an average map. Each pair of players counts at most 200 games, every player carries one virtual win and loss against a fixed anchor, and ratings are relative to a mean of 1500 over all 73 players. Each of our builds is its own player. Regenerate with `tools/elo.py`; do not edit.

Games against field bots are replica scrimmages only; local games are our builds against each other and the stock players. Record = games won and lost.

![ratings of our builds](elo.png)

Our builds, in attempt order (rating +- 95%; field score = expected score against each of the 51 field players, one game each):

| build | rating | rank | games | record | field score |
|---|---|---|---|---|---|
| s0e | 1363 +- 140 | 53 of 73 | 33 | 15-18 | 38.2% |
| s0m | 1381 +- 69 | 52 of 73 | 148 | 98-50 | 39.4% |
| s0u | 1390 +- 126 | 51 of 73 | 37 | 13-24 | 40.0% |
| v0d | 1680 +- 128 | 23 of 73 | 37 | 27-10 | 60.6% |
| s2o | 1487 +- 18 | 37 of 73 | 1510 | 728-782 | 46.7% |
| s3e | 1450 +- 76 | 46 of 73 | 80 | 29-51 | 44.2% |
| s3f | 1509 +- 75 | 29 of 73 | 80 | 46-34 | 48.3% |
| s3g | 1450 +- 76 | 47 of 73 | 80 | 30-50 | 44.2% |
| s2o_id | 1487 +- 75 | 36 of 73 | 80 | 40-40 | 46.7% |
| s3h | 1505 +- 53 | 31 of 73 | 188 | 100-88 | 48.0% |
| s3i | 1460 +- 75 | 43 of 73 | 80 | 27-53 | 44.8% |
| s3k | 1461 +- 75 | 42 of 73 | 80 | 29-51 | 44.9% |
| s3l | 1455 +- 76 | 45 of 73 | 80 | 27-53 | 44.5% |
| s3n | 1462 +- 75 | 41 of 73 | 80 | 29-51 | 45.0% |
| s3r | 1507 +- 98 | 30 of 73 | 46 | 31-15 | 48.2% |
| s3s | 1493 +- 75 | 35 of 73 | 80 | 46-34 | 47.1% |
| s3t | 1504 +- 75 | 32 of 73 | 80 | 49-31 | 47.9% |
| s3u | 1518 +- 75 | 27 of 73 | 80 | 53-27 | 48.9% |
| s3v | 1499 +- 75 | 33 of 73 | 80 | 54-26 | 47.6% |
| s3w | 1493 +- 75 | 34 of 73 | 80 | 52-28 | 47.2% |
| s3x | 1476 +- 75 | 39 of 73 | 80 | 48-32 | 46.0% |
| ratbot | 1420 +- 69 | 49 of 73 | 154 | 107-47 | 42.1% |

All players:

| # | player | rating | games | record | field score |
|---:|---|---|---|---|---|
| 1 | uravt_Version41 | 2095 +- 101 | 78 | 62-16 | 89.2% |
| 2 | uravt_Version35 | 2061 +- 75 | 102 | 69-33 | 87.4% |
| 3 | IvanGeffner_basic51 | 2037 +- 82 | 90 | 62-28 | 86.1% |
| 4 | awu7_awubot_us | 2035 +- 90 | 87 | 63-24 | 86.0% |
| 5 | quesswho_bot16 | 1966 +- 73 | 108 | 67-41 | 81.7% |
| 6 | daannte_v52_ThisGuy | 1933 +- 72 | 108 | 61-47 | 79.6% |
| 7 | abcdezyxwv_bestsofar | 1931 +- 94 | 99 | 78-21 | 79.5% |
| 8 | quesswho_quals | 1930 +- 81 | 87 | 54-33 | 79.4% |
| 9 | nzjt_TheComplexMerlin | 1926 +- 92 | 87 | 63-24 | 79.1% |
| 10 | thewaxmango_PhD | 1921 +- 105 | 78 | 56-22 | 78.8% |
| 11 | daannte_v47_Skadoosh | 1902 +- 75 | 117 | 70-47 | 77.4% |
| 12 | nubbbbbb_testplayer | 1897 +- 96 | 69 | 48-21 | 77.0% |
| 13 | battlecode-archive_testplayer | 1889 +- 78 | 102 | 65-37 | 76.5% |
| 14 | awu7_awubot_quals | 1878 +- 81 | 90 | 58-32 | 75.8% |
| 15 | TeamSPAARK_SPAARK | 1832 +- 75 | 108 | 63-45 | 72.3% |
| 16 | AlexT101_finalsbot | 1778 +- 76 | 114 | 62-52 | 68.3% |
| 17 | AlexT101_usqualsbot | 1777 +- 74 | 123 | 74-49 | 68.2% |
| 18 | AdamTan12_Icarus | 1762 +- 72 | 117 | 62-55 | 67.1% |
| 19 | bman12345678_goonv23 | 1760 +- 110 | 78 | 57-21 | 66.9% |
| 20 | AdamTan12_Finals | 1747 +- 85 | 99 | 53-46 | 65.9% |
| 21 | thewaxmango_collegejunior | 1714 +- 93 | 81 | 50-31 | 63.4% |
| 22 | HugoIngelsson_YokoOno | 1698 +- 93 | 81 | 47-34 | 62.2% |
| 23 | us:v0d | 1680 +- 128 | 37 | 27-10 | 60.6% |
| 24 | spsquared_TSPAARK_QUALS | 1657 +- 103 | 75 | 47-28 | 59.2% |
| 25 | HugoIngelsson_Xzibit | 1646 +- 87 | 93 | 54-39 | 58.3% |
| 26 | VarunVejalla_final4_run_away10 | 1622 +- 131 | 57 | 39-18 | 56.5% |
| 27 | us:s3u | 1518 +- 75 | 80 | 53-27 | 48.9% |
| 28 | outercloudstudio_v_1_18 | 1517 +- 94 | 78 | 47-31 | 48.8% |
| 29 | us:s3f | 1509 +- 75 | 80 | 46-34 | 48.3% |
| 30 | us:s3r | 1507 +- 98 | 46 | 31-15 | 48.2% |
| 31 | us:s3h | 1505 +- 53 | 188 | 100-88 | 48.0% |
| 32 | us:s3t | 1504 +- 75 | 80 | 49-31 | 47.9% |
| 33 | us:s3v | 1499 +- 75 | 80 | 54-26 | 47.6% |
| 34 | us:s3w | 1493 +- 75 | 80 | 52-28 | 47.2% |
| 35 | us:s3s | 1493 +- 75 | 80 | 46-34 | 47.1% |
| 36 | us:s2o_id | 1487 +- 75 | 80 | 40-40 | 46.7% |
| 37 | us:s2o | 1487 +- 18 | 1510 | 728-782 | 46.7% |
| 38 | amcsz_fighterrats | 1486 +- 100 | 72 | 36-36 | 46.6% |
| 39 | us:s3x | 1476 +- 75 | 80 | 48-32 | 46.0% |
| 40 | CS50Crab_protoSaberV7 | 1475 +- 85 | 90 | 46-44 | 45.9% |
| 41 | us:s3n | 1462 +- 75 | 80 | 29-51 | 45.0% |
| 42 | us:s3k | 1461 +- 75 | 80 | 29-51 | 44.9% |
| 43 | us:s3i | 1460 +- 75 | 80 | 27-53 | 44.8% |
| 44 | CS50Crab_rapierV1 | 1458 +- 75 | 114 | 57-57 | 44.6% |
| 45 | us:s3l | 1455 +- 76 | 80 | 27-53 | 44.5% |
| 46 | us:s3e | 1450 +- 76 | 80 | 29-51 | 44.2% |
| 47 | us:s3g | 1450 +- 76 | 80 | 30-50 | 44.2% |
| 48 | PixelRifts_mushakraj_mk3 | 1428 +- 87 | 102 | 46-56 | 42.5% |
| 49 | us:ratbot | 1420 +- 69 | 154 | 107-47 | 42.1% |
| 50 | bc26vibe_bot | 1393 +- 74 | 108 | 56-52 | 40.0% |
| 51 | us:s0u | 1390 +- 126 | 37 | 13-24 | 40.0% |
| 52 | us:s0m | 1381 +- 69 | 148 | 98-50 | 39.4% |
| 53 | us:s0e | 1363 +- 140 | 33 | 15-18 | 38.2% |
| 54 | Naton-Cai_ratplayer | 1334 +- 99 | 72 | 34-38 | 35.9% |
| 55 | XSercia_genies | 1332 +- 74 | 147 | 51-96 | 35.7% |
| 56 | C0derTang_v6 | 1308 +- 83 | 90 | 36-54 | 34.1% |
| 57 | ask8000_bumchit67 | 1254 +- 70 | 132 | 49-83 | 30.4% |
| 58 | bc26-example | 1238 +- 38 | 490 | 173-317 | 29.4% |
| 59 | justinottesen_alfredo | 1227 +- 83 | 105 | 40-65 | 28.7% |
| 60 | Camleaf_mk1 | 1177 +- 97 | 81 | 30-51 | 25.4% |
| 61 | battlecode-archive_result_408 | 1173 +- 93 | 81 | 30-51 | 25.2% |
| 62 | bc26-example-copy | 1151 +- 75 | 105 | 39-66 | 23.7% |
| 63 | vincentwang60_javabot | 1149 +- 90 | 90 | 34-56 | 23.6% |
| 64 | j2399_weighted_micro_v9 | 1120 +- 93 | 87 | 32-55 | 21.9% |
| 65 | justinottesen_carbonara | 1109 +- 100 | 75 | 30-45 | 21.2% |
| 66 | Gavdingo_v1 | 1106 +- 81 | 105 | 35-70 | 21.0% |
| 67 | GuzziFlipFlops_Hermesv8 | 1101 +- 82 | 93 | 34-59 | 20.7% |
| 68 | seru9_TOTAL_AGGRO | 1052 +- 79 | 111 | 39-72 | 17.9% |
| 69 | quanticsoul4772_ratbot | 1023 +- 88 | 96 | 37-59 | 16.4% |
| 70 | Victor-Ramirez-Garcia_battleca | 962 +- 81 | 120 | 32-88 | 13.5% |
| 71 | anicolao_myplayer | 799 +- 95 | 114 | 22-92 | 7.3% |
| 72 | j2399_weighted_path | 730 +- 129 | 60 | 13-47 | 5.5% |
| 73 | Krak9n_robot | 556 +- 169 | 69 | 4-65 | 2.2% |

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
| Excavation | +153 |
| uneasy_alliance | +153 |
| wallsofparadis | +153 |
| externalsustenance | +125 |
| jail | +125 |
| Nofreecheese | +99 |
| cheesefarm | +99 |
| evileye | +99 |
| starvation | +99 |
| ZeroDay | +76 |
| bed | +76 |
| closeup | +76 |
| clouds | +76 |
| dirtfulcat | +76 |
| gridlock | +76 |
| knifefight | +76 |
| thunderdome | +76 |
| toomuchcheese | +76 |
| FreeTheCheese | +58 |
| cheeseguardians | +58 |
| houseofcards | +40 |
| sunandmoon | +18 |
| tornadoes | -22 |
| replica | -22 |
| canyoudig | -27 |
| TheHeist | -53 |
| groveisbeingdesecrated | -53 |
| rose | -53 |
| where_are_you | -109 |
| 5t4rv4t10n_1337 | -145 |
| diamond | -151 |
| arrows | -190 |
| uneruesansfin | -235 |
| attack_on_cat | -250 |
| heart | -250 |
| seepastyourgreed | -250 |
| learn_your_lesson | -260 |
| popthecork | -260 |
| krotus | -272 |
| Stash | -299 |
| UncomfortablyClose | -299 |
| cheesebottles | -349 |
| mincemeat | -349 |
| averyfineline | -379 |
| t_minus_five_minutes | -399 |
| tiny | -399 |
| ultimatestarvation | -399 |
| whatsthecatdoin | -399 |
| whereisthecheese | -399 |
| Hike | -458 |
| RUN | -458 |
| clockwisdom | -615 |
| fullofsecrets | -615 |
