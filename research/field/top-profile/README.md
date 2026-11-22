# Top-team behaviour profile (replays only), 2026-11-22

This profile compares how strong replica-ladder teams play with how we play (vibe26b, rank about 18-19, rating about 1731). It ends with
build hypotheses. The sources are replays and match metadata only. No external bot source was read and no field bot was run.

## What the data is, and what it is not

* **The top 5 (Ivan basic51, awubot_us, Version35, Version41, awubot_quals) have no usable recent replays.** Their 11-20 matches return 404
  (pruned), and field-vs-field replays are not served to us. For these teams, only the old pre-M4 local result rows exist
  (build/vmruns, 10-28). Those rows are top bots against our weak early builds, with small samples (4 to 20 games, except Version41 at 786).
  See table A.
* **The main sample is 57 recent replica matches (171 games) of ours, against 10 proxy teams ranked 8-25**, mostly ranks 14-25.
  Our builds in the sample range from xj6 to yb7, and ya0 is the most frequent. The match ids are in `match-ids.txt`.
  Merlin (#8) appears only in 12 old matches (26807-27716), so its rows mostly describe our older builds.
* Galaxy metadata since 11-20, record against ranks 6-25:
  * Ivan 29-1 (97%); awu_us, V35 and V41 about 72-73%; quals 63%.
  * Us 33%. Our worst pairings are 0-27 vs quesswho_bot16, 1-23 vs thewaxmango_PhD, 2-25 vs nubbbbbb and 2-22 vs daannte_v52.
* **140 of 171 games end in a king kill** (median round about 390, with a quarter before r275). Only 31 games go to r2000 on points.
  The battle for the king decides almost every game, so the points formula rarely matters.
* Per-team samples are 6-36 games. Any per-team difference smaller than about 15 percentage points is noise.

## A. Top 5 (old local data, pre-M4, rates per 100 rounds; "us" means our early builds)

| bot | games | their win % | mean rounds | backstab r | throws /100r (them/us) | grabs /100r | rat traps /100r | dirt dug /100r | kings formed | spawns /100r (them/us) |
|---|---|---|---|---|---|---|---|---|---|---|
| IvanGeffner_basic51 (#1) | 4 | 100 | 237 | 19 | 25.7 / 6.5 | 38.8 / 36.1 | 11.4 / 21.9 | 12.0 | 0.00 | 20.7 / 19.6 |
| awu7_awubot_us (#2) | 4 | 100 | 174 | 17 | 35.7 / 9.4 | 55.7 / 51.9 | 16.1 / 34.5 | 8.4 | 0.00 | 27.9 / 26.1 |
| uravt_Version35 (#3) | 20 | 100 | 434 | 31 | 21.5 / 1.5 | 25.0 / 23.6 | 10.6 / 3.8 | 18.5 | 0.20 | 18.2 / 17.4 |
| uravt_Version41 (#4) | 786 | 99 | 432 | 58 | 15.6 / 6.2 | 24.8 / 16.4 | 11.9 / 13.4 | 19.7 | 0.49 | 20.8 / 16.0 |
| awu7_awubot_quals (#5) | 16 | 88 | 598 | 216 | 24.2 / 10.9 | 37.2 / 26.9 | 3.0 / 17.0 | 15.7 | 0.12 | 19.7 / 17.1 |
| AlexT101_finalsbot (#14) | 406 | 98 | 445 | 61 | 13.0 / 5.4 | 24.2 / 15.2 | 19.5 / 10.2 | 22.3 | 0.63 | 17.9 / 14.6 |
| AdamTan12_Finals (#17) | 310 | 99 | 410 | 39 | 10.5 / 12.5 | 24.6 / 26.7 | 17.9 / 15.4 | 14.7 | 0.59 | 21.4 / 17.0 |
| AdamTan12_Icarus (#20) | 309 | 95 | 508 | 36 | 8.3 / 11.6 | 17.0 / 20.3 | 10.4 / 8.6 | 6.7 | 0.76 | 18.5 / 11.6 |
| daannte_v52_ThisGuy (#11) | 420 | 100 | 396 | 54 | 22.0 / 8.4 | 33.1 / 26.4 | 12.0 / 16.7 | 18.8 | 1.42 | 24.8 / 19.4 |
| quesswho_quals (#13) | 281 | 99 | 419 | 28 | 14.9 / 9.4 | 21.7 / 18.0 | 15.0 / 11.7 | 18.7 | 0.55 | 16.7 / 14.5 |

Takeaways from table A:
* The top 5 win fast: Ivan by king kill around r237, awu_us around r175.
* They backstab at r17-31 (awubot_quals at r216).
* They throw 2.5 to 14 times as often as we did.
* They almost never form a second king (0-0.2 kings formed).
* They lay traps at rates similar to the rest of the field.

## B. Recent replica games against proxies (171 games), with them / us in the same games

The columns are defined in `scripts/prof.py`, and the raw aggregates are in `agg.json`.
* "king-1 moves" and "max dist" track the first king's tile changes and its farthest distance from the start.
* "starve HP" is damage the team's own kings took from starvation.

**Ends, backstab, kings** (each cell: them / us in the same games)

| opponent (rank) | games | win % | king-kill wins | mean rounds | backstab round | stabbed first % | max kings | 2+ kings % | round of 2nd king |
|---|---|---|---|---|---|---|---|---|---|
| nzjt_TheComplexMerlin (#8) | 36 | 81 / 19 | 27 / 5 | 595 / 595 | 52 / 52 | 47 / 53 | 1.2 / 1.2 | 25 / 17 | 771 / 1001 |
| AlexT101_finalsbot (#14) | 12 | 75 / 25 | 9 / 3 | 704 / 704 | 29 / 29 | 92 / 8 | 1.8 / 1.1 | 50 / 8 | 593 / 341 |
| AdamTan12_Finals (#17) | 15 | 60 / 40 | 7 / 4 | 1040 / 1040 | 66 / 66 | 60 / 40 | 1.6 / 1.5 | 40 / 40 | 660 / 953 |
| AdamTan12_Icarus (#20) | 9 | 56 / 44 | 3 / 3 | 967 / 967 | 104 / 104 | 67 / 33 | 1.7 / 1.6 | 56 / 33 | 446 / 796 |
| AlexT101_usqualsbot (#21) | 21 | 67 / 33 | 12 / 5 | 922 / 922 | 56 / 56 | 71 / 29 | 1.5 / 1.4 | 38 / 38 | 808 / 796 |
| VarunVejalla_final4_run_away10 (#22) | 21 | 43 / 57 | 7 / 10 | 867 / 867 | 52 / 52 | 90 / 10 | 1 / 1.3 | 0 / 29 | - / 771 |
| thewaxmango_collegejunior (#23) | 30 | 27 / 73 | 7 / 15 | 1180 / 1180 | 79 / 79 | 60 / 40 | 1.3 / 1.6 | 23 / 47 | 640 / 754 |
| HugoIngelsson_YokoOno (#24) | 18 | 11 / 89 | 2 / 14 | 496 / 496 | 57 / 57 | 33 / 67 | 1.1 / 1.4 | 6 / 33 | 330 / 614 |
| HugoIngelsson_Xzibit (#25) | 6 | 33 / 67 | 2 / 3 | 853 / 853 | 82 / 82 | 33 / 67 | 1.5 / 1.7 | 33 / 50 | 872 / 317 |
| *all games we lost* | 87 | 100 / 0 | 76 / 0 | 800 / 800 | 42 / 42 | 61 / 39 | 1.4 / 1.3 | 36 / 22 | 740 / 775 |
| *all games we won* | 84 | 0 / 100 | 0 / 64 | 881 / 881 | 80 / 80 | 62 / 38 | 1.2 / 1.5 | 15 / 42 | 521 / 773 |

**Bodies and economy** (each cell: them / us in the same games)

| opponent (rank) | games | babies r200 | r500 | r1000 | peak | spawned | bank r200 | bank r500 | bank r1000 | cheese picked | cheese transferred | xfer share % | cat dmg share % |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| nzjt_TheComplexMerlin (#8) | 36 | 17 / 9.4 | 20 / 16 | 21 / 9.8 | 25 / 21 | 92 / 72 | 561 / 501 | 1333 / 687 | 1243 / 525 | 7181 / 2981 | 4810 / 2225 | 71 / 29 | 50 / 50 |
| AlexT101_finalsbot (#14) | 12 | 16 / 13 | 22 / 12 | 21 / 8 | 29 / 19 | 105 / 97 | 826 / 825 | 915 / 568 | 1887 / 416 | 11121 / 3757 | 8622 / 3058 | 75 / 25 | 57 / 43 |
| AdamTan12_Finals (#17) | 15 | 14 / 12 | 14 / 17 | 21 / 23 | 26 / 25 | 123 / 150 | 813 / 1137 | 865 / 736 | 1336 / 1136 | 13325 / 10616 | 10558 / 8890 | 58 / 42 | 47 / 53 |
| AdamTan12_Icarus (#20) | 9 | 17 / 13 | 19 / 22 | 21 / 23 | 25 / 23 | 116 / 111 | 958 / 1212 | 1142 / 908 | 1419 / 952 | 11961 / 9687 | 9340 / 7893 | 70 / 30 | 78 / 22 |
| AlexT101_usqualsbot (#21) | 21 | 14 / 13 | 15 / 18 | 15 / 17 | 27 / 22 | 117 / 120 | 614 / 900 | 864 / 844 | 970 / 902 | 12005 / 7577 | 9863 / 5724 | 61 / 39 | 49 / 51 |
| VarunVejalla_final4_run_away10 (#22) | 21 | 18 / 13 | 23 / 16 | 24 / 19 | 30 / 22 | 114 / 118 | 526 / 859 | 330 / 739 | 380 / 980 | 10377 / 7663 | 7792 / 6141 | 58 / 42 | 34 / 66 |
| thewaxmango_collegejunior (#23) | 30 | 16 / 15 | 18 / 22 | 21 / 25 | 26 / 29 | 128 / 136 | 1447 / 1185 | 1179 / 840 | 1265 / 1263 | 11240 / 13189 | 7323 / 10765 | 42 / 58 | 31 / 69 |
| HugoIngelsson_YokoOno (#24) | 18 | 14 / 15 | 15 / 23 | 29 / 39 | 24 / 21 | 81 / 69 | 585 / 1117 | 814 / 976 | 2192 / 1711 | 4166 / 5540 | 3031 / 4445 | 22 / 78 | 62 / 38 |
| HugoIngelsson_Xzibit (#25) | 6 | 15 / 14 | 15 / 20 | 22 / 31 | 21 / 27 | 119 / 86 | 1478 / 1455 | 1384 / 1081 | 3162 / 1711 | 10053 / 9472 | 6905 / 7544 | 40 / 60 | 64 / 36 |
| *all games we lost* | 87 | 16 / 12 | 19 / 14 | 21 / 12 | 28 / 21 | 116 / 103 | 730 / 768 | 951 / 631 | 1236 / 606 | 10920 / 5331 | 8380 / 4149 | 69 / 31 | 51 / 49 |
| *all games we won* | 84 | 16 / 14 | 17 / 22 | 21 / 28 | 25 / 25 | 102 / 110 | 951 / 1120 | 992 / 963 | 1350 / 1402 | 8625 / 9897 | 5953 / 8037 | 42 / 58 | 47 / 53 |

**Combat, dirt, king movement** (each cell: them / us in the same games)

| opponent (rank) | games | rat deaths | rats killed | rat traps laid | throws | grabs | cat traps | bites on kings | dmg to kings | own king starve HP | dirt dug | dirt placed | king-1 moves | king-1 max dist |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| nzjt_TheComplexMerlin (#8) | 36 | 73 / 65 | 49 / 39 | 4.7 / 14 | 94 / 57 | 119 / 76 | 10 / 12 | 0 / 3.4 | 273 / 76 | 74 / 182 | 96 / 49 | 0 / 0 | 45 / 53 | 17 / 13 |
| AlexT101_finalsbot (#14) | 12 | 79 / 93 | 77 / 42 | 122 / 36 | 81 / 63 | 139 / 98 | 0 / 8.5 | 8.8 / 7.2 | 323 / 187 | 0 / 197 | 68 / 27 | 4.9 / 0 | 152 / 47 | 14 / 8.9 |
| AdamTan12_Finals (#17) | 15 | 100 / 136 | 108 / 75 | 147 / 72 | 85 / 100 | 110 / 136 | 1 / 14 | 25 / 7.1 | 270 / 254 | 71 / 93 | 55 / 31 | 8.5 / 0 | 183 / 80 | 15 / 11 |
| AdamTan12_Icarus (#20) | 9 | 92 / 95 | 75 / 53 | 94 / 40 | 66 / 83 | 88 / 107 | 4.8 / 13 | 1.6 / 7 | 63 / 229 | 59 / 124 | 33 / 43 | 2.1 / 0 | 36 / 114 | 16 / 13 |
| AlexT101_usqualsbot (#21) | 21 | 96 / 110 | 81 / 57 | 116 / 40 | 99 / 87 | 140 / 119 | 0 / 9.5 | 15 / 7.9 | 378 / 229 | 25 / 109 | 96 / 50 | 4.1 / 0 | 201 / 80 | 20 / 13 |
| VarunVejalla_final4_run_away10 (#22) | 21 | 95 / 105 | 88 / 67 | 114 / 49 | 99 / 100 | 139 / 136 | 4.4 / 14 | 17 / 6.2 | 186 / 181 | 186 / 67 | 50 / 28 | 13 / 0 | 33 / 50 | 10 / 9.0 |
| thewaxmango_collegejunior (#23) | 30 | 107 / 110 | 76 / 82 | 8.2 / 54 | 197 / 131 | 227 / 175 | 0 / 12 | 17 / 1.9 | 314 / 109 | 272 / 22 | 45 / 71 | 0 / 0 | 162 / 78 | 11 / 12 |
| HugoIngelsson_YokoOno (#24) | 18 | 66 / 49 | 33 / 54 | 25 / 36 | 75 / 88 | 97 / 111 | 7.3 / 9.5 | 3.7 / 5.2 | 54 / 334 | 154 / 16 | 41 / 41 | 0.2 / 0 | 27 / 46 | 8.2 / 9.2 |
| HugoIngelsson_Xzibit (#25) | 6 | 99 / 67 | 34 / 58 | 29 / 36 | 70 / 101 | 91 / 122 | 8.2 / 11 | 15 / 4.3 | 167 / 363 | 0 / 221 | 37 / 34 | 1.2 / 0 | 56 / 102 | 14 / 13 |
| *all games we lost* | 87 | 92 / 96 | 77 / 55 | 78 / 35 | 106 / 82 | 139 / 113 | 4.3 / 8.5 | 18 / 3.3 | 407 / 121 | 37 / 182 | 71 / 37 | 4.2 / 0 | 100 / 71 | 16 / 12 |
| *all games we won* | 84 | 86 / 88 | 61 / 62 | 46 / 44 | 109 / 97 | 139 / 127 | 3.9 / 14 | 3.4 / 7 | 74 / 252 | 201 / 18 | 59 / 55 | 2.4 / 0 | 103 / 64 | 12 / 10 |

## C. Round windows (`windows.txt`, full per-opponent tables)

Each column pair is us / them, as means per game in the window.
* "Fwd%" is the share of baby-rat turns spent nearer the enemy king's start than our own.
* "NrEK" counts rat-turns within 6 tiles of the enemy start.

Our **losses** (87 games):

| window | deaths | spawns | traps laid | throws | cheese xfer | Fwd% | NrEK |
|---|---|---|---|---|---|---|---|
| 1-100 | 13.6 / 12.3 | 33 / 31 | 9.5 / 14.6 | 17 / 22 | 226 / 286 | 18 / 15 | 80 / 28 |
| 101-200 | 13.2 / 11.8 | 13 / 15 | 5.5 / 9.2 | 15 / 23 | 369 / 616 | 20 / 26 | 49 / 31 |
| 201-400 | 14.5 / 12.9 | 18 / 19 | 5.8 / 12.2 | 17 / 24 | 738 / 1505 | 20 / **31** | 76 / 44 |
| 401-800 | 23.8 / 22.9 | 30 / 39 | 11 / **31** | 29 / 33 | 1863 / 3344 | **6.8 / 32** | 11 / 92 |
| 801-2000 | 46.5 / 44.8 | 64 / 85 | 24 / **68** | 50 / 59 | 3843 / 9854 | **4.7 / 34** | 6 / 187 |

Our **wins** (84 games): the opponent's Fwd% stays at 13-17% in every window, ours is 11-21%, and transfers favour us from r200.

Win rate by the opponent's Fwd% in r101-200 (145 games that lasted past r200):
* below 12%: 60% (n=53)
* 12-20%: 68% (n=25)
* 20-30%: 30% (n=27)
* 30% or more: 28% (n=40)

Our own Fwd% does not predict the result: 49 / 40 / 59 / 37%. **Losing the midline after the rush, not a weak rush, is what separates
losses from wins.** Rat deaths are nearly even in every window, so we do not lose the fights outright. We lose ground, then income,
then the king.

## D. Traps (trap lifecycle; `scripts/trap.py` over 15 trap-team matches)

| | placed / game | fired % | median life (rounds) | placed in OUR half % | victim grabbed within 3 rounds % | fired on a thrown rat % |
|---|---|---|---|---|---|---|
| finalsbot, Finals, usquals, Varun, Icarus | 92-165 | 93-99 | 1 | 61-77 | 22-36 | about 5 |
| vibe26b in the same games | 47-52 | - | - | 61-69 (31-39 in the enemy half; 17-20% within 8 tiles of our own king) | - | - |

* Field traps are **reactive point-blank weapons**: placed next to an enemy rat and sprung the next round, with only 2-8 live at a time.
  They are not minefields.
* They supply 50-57% of these teams' damage; for us it is 25%.
* They kill 35-41 of our rats per game.
* Against the trap teams we win 37% of games (67 games); against the others we win 55% (78 games).
* Win rate by trap gap (their traps minus ours, games past r200):
  * we laid more: 50%
  * they laid 0-10 more: 65%
  * 10-25 more: 45%
  * 25 or more: **26%**
* Their traps in r401-800 of our losses: 31 vs our 11. The trap gap opens after r400, together with the territory gap.

## E. Kings

* Our dead kings in losses (59 kings):
  * cause: starvation 29, bite 12, thrown-rat hit 11, cat scratch 4, trap 3
  * the bank broke first in 61%
  * first enemy contact at a median of r115
* Starvation is what kills the king after territory and income are lost. In losses we pick up 5.3k cheese vs their 10.9k, and at r1000
  our bank is 606 vs 1236 and we have 12 babies vs 21.
* Against Merlin (27 old games, `econ_report`):
  * Merlin picks up about 6300 cheese per game vs our 2860, and takes 1527 per game from mines on our side.
* Second kings:
  * Teams that beat us form a second king in 36% of games (around r740). We do in 22%.
  * When we win, we form one in 42%.
  * Our second king correlates with winning, but it is an effect of a rich position rather than a cause. finalsbot gets 2 kings in
    50% of games at r593; the top 5 almost never do (table A).
* Opponent kings that beat us move more than ours: finalsbot, Finals and usquals make 150-200 king moves with a max distance of 14-20,
  vs our 47-80 and 9-13. Varun, Icarus and Merlin do not, so the evidence is weak.

## Hypotheses, ranked by expected gain

**H1. Hold a forward front after the rush (midline army), biggest expected gain.**
* Evidence:
  * After r200, the opponent's Fwd% in our losses is 31-34% while ours drops to 5-7% (our rush ends at RUSH_UNTIL 300).
  * In our wins, the opponent stays at about 15%.
  * Our win rate falls from about 62% to 29% when the opponent holds more than 20% forward early.
  * Then the income gap opens: xfer 69/31, picked 10.9k vs 5.3k, and Merlin takes 1527 per game from our-side mines.
  * Then the king starves: 29 of 59 dead kings.
* Example matches:
  * 33949 g2 (Icarus)
  * 35005 g0 (Finals)
  * 34263 g0 and 35169 g0 (usquals)
  * 34487 g0 (finalsbot)
  * 27623 g2 (Merlin)
* Build:
  * a persistent fighter group that takes and holds the contested midline mines after the rush (`Baby.roleFor`/`rusher`/`attack`;
    turn `FIGHT_POST` on; `WAR_GATHER`)
  * the rush ending into a hold rather than a recall (`RUSH_UNTIL`)
  * King.spawn biased toward the front
* Risk: Fwd% may be partly a consequence of winning the fight. Test it on the n3 panel.

**H2. Trap-first close combat.**
* Evidence:
  * The 5 trap teams get 50-57% of their damage from traps vs our 25%.
  * They lay 2-3x our count and spring them in 1 round.
  * We win 37% against them vs 55% against the others, and 26% when the trap gap is 25 or more.
* Build:
  * in `Baby`: also trap in `actAfterMove`; target the tile the enemy will step on (`trapTile`)
  * lower `RAT_TRAP_MIN_CHEESE` and widen `BABY_TRAP_DIST2`
  * drop the `KING_RESERVE+20` gate when local enemies outnumber us
  * follow a trap hit with a grab (a 50-HP victim is grabbable)
* Caveat: earlier trap lines (u4m, u5o, u8f, zdy) were neutral, and u8f starved its king. So pair this with H1's income, or cap trap
  spending by income.

**H3. Keep the army growing after r400.**
* Evidence:
  * In losses our babies fall 14 → 12 at r500-1000 while theirs rise to 21.
  * Their spawns in r401-800 are 39 vs our 30.
* Build:
  * spawn on deficit, with no reserve gates that shrink the army (LEARNINGS #17)
  * `babyCap`/`CORE_BABIES`/`KING_RESERVE` in King
* This is likely downstream of H1, so build it together with H1.

**H4. Faster decisive early fight (top-5 style).**
* Evidence: the top 5 kill kings at r175-237, backstab at r17-31 and throw 2.5-14x more.
* Our r1-100 NrEK is already 80-97. We reach the enemy king, but we do not finish it.
* Build:
  * more carry/throw onto the king (`CARRY_KING`, `tryThrow`, `aimCarry`)
* The evidence is old and comes from small samples, so this is lower confidence.

**Not supported:**
* A multi-king empire (sw01): the top 5 form 0-0.2 kings, and a second king follows winning rather than causing it.
* A mobile king: the evidence is mixed.

## Next data step

Request a few scrims against the top 5 and fetch the replays within 1 day, before they are pruned. That would replace table A with
real current data. The scripts in `scripts/` rerun on any match ids:
`BC_DUMPCACHE=... python3 -I scripts/prof.py out.jsonl IDS...`, then `agg.py`, `war.py`, `waragg.py` and `trap.py`.
