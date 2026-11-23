# Why the sparring bots lose locally while the teams they imitate beat us (2026-11-23)

Owner 06:49 PDT: "Something is still wrong with your sparring bots, since they're worse than the champion, and yet the bots
they imitate beat our champion. Can you see if you can figure out the behavioral difference that explains this
discrepancy?"

Sources: replays only (no external source read).
* Replica: the 18 overnight matches (54 games), 24 lost and 30 won: 36852 36963 37038 37073 37079 37118 37123 37124 37133
  37146 37270 37306 37366 37377 37402 37553 37554 37611.
* Local: `runs/sp001` (yk1 against sw35 and sw48, 7 maps, both seats, replays kept) and `runs/sp002` (yk1 against the new
  sparring bot sw51), plus the per-game totals of 1,894 tournament games with a sparring bot.

"Forward" means nearer the enemy king's start than our own. Windows are round ranges; the figures are per game that
reached the window.

## Answer in one line

The sparring bots copy a number, not a behaviour. They are our own code with one to four knobs changed, so their rats
play our game: home-half mine posts after the rush, and traps only when the bank clears the reserve or the enemy is at
our king. The teams that beat us hold our half of the map from about round 200, lay most of their traps there at 2-3x our
rate, and bite our king. No knob of our code produces that.

## What the replica teams do in the 24 games they won (them / us)

| window | rats | forward rat-turns | traps | of which forward | king bites | rats lost | cheese picked |
|---|---|---|---|---|---|---|---|
| r1-100 | 14.8 / 14.0 | 18% / 14% | 16.0 / 10.0 | 7.8 / 4.4 | 0.5 / 0.1 | 11.7 / 14.2 | 591 / 425 |
| r101-200 | 15.8 / 13.4 | 23% / 17% | 12.7 / 6.5 | 6.4 / 2.4 | 5.0 / 0.2 | 12.0 / 14.9 | 952 / 677 |
| r201-400 | 14.5 / 12.5 | 25% / 8% | 17.1 / 9.1 | 12.6 / 1.5 | 4.6 / 1.0 | 18.0 / 17.5 | 1807 / 1356 |
| r401-800 | 12.8 / 10.8 | 27% / 7% | 22.8 / 15.4 | 17.3 / 2.8 | 11.7 / 0.9 | 27.6 / 25.7 | 3075 / 2403 |
| r801+ | 21.2 / 11.3 | 28% / 6% | 116 / 42 | 100 / 6.3 | 23.1 / 4.1 | 71.6 / 80.4 | 14621 / 8519 |

In the 30 games we won, neither side holds the other's half after the rush (11-17% forward), traps are level, and
king bites are 1-2 per window each. Our wins come from the opening rush (we bite their king 6.3 times in r1-100).

How our kings died in the 24 losses: 64 bites, 5 starvations, 2 traps.

By team, per 100 rounds (them / us), all 54 games:

| opponent | games | lost | rat traps | % forward | throws | king bites |
|---|---|---|---|---|---|---|
| thewaxmango_collegejunior | 6 | 5 | 1.4 / 5.0 | 50 / 38 | 23.3 / 14.0 | 2.3 / 0.1 |
| AdamTan12_Finals | 3 | 2 | 11.0 / 5.9 | 60 / 37 | 6.7 / 6.6 | 1.7 / 0.3 |
| AlexT101_finalsbot | 3 | 2 | 24.3 / 7.3 | 76 / 29 | 12.8 / 12.1 | 0.3 / 2.4 |
| AlexT101_usqualsbot | 9 | 6 | 14.2 / 6.3 | 75 / 25 | 12.5 / 10.7 | 1.1 / 0.9 |
| VarunVejalla_final4 | 9 | 5 | 11.2 / 4.0 | 54 / 38 | 12.7 / 14.0 | 2.6 / 0.2 |
| HugoIngelsson_YokoOno | 12 | 3 | 4.2 / 7.0 | 41 / 64 | 15.9 / 16.6 | 2.1 / 1.2 |
| HugoIngelsson_Xzibit | 6 | 1 | 3.0 / 5.4 | 52 / 52 | 13.9 / 16.7 | 1.5 / 1.2 |
| spsquared_TSPAARK | 6 | 0 | 7.2 / 8.1 | 12 / 87 | 15.7 / 13.9 | 0.1 / 1.3 |

Four of the five teams that beat us lay 2-3x our traps, 54-76% of them in our half. Against the teams we beat, we are the
side trapping forward (52-87%). thewaxmango is the other style: almost no traps, 23 throws per 100 rounds to our 14.

Why our code cannot trap forward: away from our king (DEF_TRAP_DIST2 64) a baby lays a rat trap only when the bank holds
KING_RESERVE + 20 = 420 per king and the kings are not austere. In our losses the bank is under that line 49-60% of
rounds from r200. And after the rush (RUSH_UNTIL 200) none of our rats is posted in the enemy half: gatherers and
fighters hold home-half mines (FIGHT_POST picks the contested ones).

## What the sparring bots do locally (runs/sp001: 14 games each against yk1, 7 loss maps, both seats)

Each cell is sparring bot / yk1, per game in the window.

| bot | window | rats | forward rat-turns | rat-turns at the enemy king | traps | forward traps | king bites |
|---|---|---|---|---|---|---|---|
| sw35 | r1-100 | 14.5 / 14.0 | 3% / 25% | 0.6 / 23.6 | 11.6 / 7.6 | 0.5 / 7.1 | 0.0 / 0.2 |
| sw35 | r101-200 | 15.3 / 14.5 | 5% / 29% | 1.7 / 18.5 | 7.5 / 4.8 | 0.9 / 3.3 | 0.0 / 1.9 |
| sw35 | r201-400 | 17.6 / 17.4 | 9% / 15% | 6.3 / 21.1 | 10.5 / 8.2 | 3.4 / 4.6 | 1.8 / 0.3 |
| sw35 | r401-800 | 17.3 / 22.2 | 15% / 14% | 33.5 / 22.7 | 17.0 / 17.6 | 8.0 / 7.9 | 0.8 / 2.2 |
| sw48 | r1-100 | 16.9 / 13.9 | 13% / 14% | 10.7 / 6.1 | 9.9 / 9.4 | 6.3 / 2.9 | 1.6 / 0.1 |
| sw48 | r101-200 | 19.2 / 14.4 | 16% / 22% | 16.5 / 14.0 | 5.1 / 5.8 | 1.6 / 1.6 | 1.3 / 0.1 |
| sw48 | r201-400 | 18.3 / 15.6 | 14% / 14% | 11.8 / 4.6 | 7.6 / 7.6 | 3.7 / 3.9 | 0.9 / 1.5 |
| sw48 | r401-800 | 17.6 / 17.9 | 14% / 11% | 22.2 / 21.6 | 15.1 / 16.4 | 5.9 / 8.2 | 0.8 / 1.0 |

**sw35 has no rush at all: a bug.** It was meant to keep rushers all game and never give up (RUSH_UNTIL 2001,
RUSH_LIFE 0). Cfg documents RUSH_LIFE 0 as "off", but `Baby.rushFailed` returns `round - birth > life && round - seen >
life`, which with life 0 is true on a rusher's second turn. So every sw35 rusher turned gatherer at once. In the
first 200 rounds 3-5% of its rat-turns are forward against yk1's 25-29%, and it never reaches the enemy king. sw35 played
1,737 tournament games (41% won, -146 vs yk1) as a champion without its opening rush. It never imitated the siege.

**sw48 plays the champion's game with more bodies.** Raising the cap to 24 gives it 2-5 more rats, but its rats sit where
the champion's sit (13-16% forward), lay the same traps, and bite kings about once per window. The premise was wrong too.
In our replica losses the cap never binds: our army is 12-13 from r150 to r800 because we lose the trades.

So neither sparring bot ever did what the replica teams do: hold our half from r200, trap there at 2-3x our rate, and bite
our king.

## Rebuilt sparring partners

* **sw52** = yk1 with sw35's intent and the bug fixed: rushers all game (RUSH_UNTIL 2001), none gives up (RUSH_LIFE 2001),
  carriers near the enemy king hold for a king hit (CARRY_KING 10, CARRY_KING_LEFT 1), traps from 50 cheese.
* **sw51** = yk1 + new code (sparring only, the champion line is unchanged):
  * **raiders** (RAID_SHARE 2): at war, half of the FIGHT rats post on the enemy twin of their contested home mine instead
    of the mine itself, so about a quarter of the later army holds the enemy half;
  * **forward traps** (FWD_TRAP): a rat trap laid from the enemy half skips the KING_RESERVE and austerity gates.
* Retired: sw35 (broken) and sw48 (premise wrong; plays like the champion).

## Check of the rebuilt partners (runs/sp003 and sp004, 14 games each against yk1, both seats; 11-23 08:30 PDT)

Each cell is the sparring bot's value / yk1's, per game in the window (`windows-local-sw52.txt`, `windows-local-sw53.txt`).

| bot | result | window | forward rat-turns | rat-turns near the enemy king | traps | forward traps | king bites |
|---|---|---|---|---|---|---|---|
| sw52 | 6-8 | r201-400 | 20% / 11% | 146 / 20 | 6.4 / 9.5 | 3.1 / 3.8 | 1.3 / 1.9 |
| sw52 | | r401-800 | 16% / 10% | 298 / 9 | 12.8 / 16.6 | 7.2 / 6.4 | 4.2 / 1.4 |
| sw53 | 11-3 | r201-400 | 14% / 11% | 29 / 0.3 | 8.4 / 9.6 | 5.1 / 3.9 | 0.8 / 0.0 |
| sw53 | | r401-800 | 13% / 11% | 35 / 25 | 19.9 / 16.5 | 11.4 / 6.9 | 0.9 / 0.6 |
| sw53 | | r801-2000 | 13% / 9% | 28 / 11 | 36.0 / 32.2 | 24.8 / 9.8 | 1.1 / 0.7 |

* **sw52 now sieges.** Its rushers sit at the enemy king from r200 (15-30 times yk1's presence there) and bite it three
  times as often in r401-800. The sw35 bug is gone. It still loses: 6-8 here, -115 vs yk1 in the tournament.
* **sw53 lays forward traps but barely holds ground.** It lays 1.6-2.5 times yk1's traps in the enemy half from r400,
  but is forward only 13-14% of rat-turns against the replica winners' 25-28%. It beats yk1 11-3 here and leads it
  by +28 in the tournament. The trap half of the imitation matters more than the raid half, so FWD_TRAP (a rat trap
  laid from the enemy half skips the reserve and austerity gates) is the first candidate for the champion line.

## Files

* `scripts/rep.py`, `scripts/loc.py`, `scripts/cmp.py`: per-game totals, replica vs local (`summary-compare.txt`).
* `scripts/win.py`: per-window behaviour from replay dumps (`windows-replica.txt`, `windows-local-*.txt`).
  `python3 win.py replica replica MATCH...`; `python3 win.py local LABEL runs/spNNN SPARBOT`.
* `scripts/tl.py REPLAY GAME OURNAME STEP`: one game's timeline (rats, forward share, traps, throws, deaths, king bites).
