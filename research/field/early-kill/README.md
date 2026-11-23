# How strong teams kill our king early (replays only), 2026-11-23

**Sources.** Replays and match metadata only. No other team's source was read and no field bot was run.

**Sample.** 25 games that we lost by king kill at or before r460, out of 37 recent replica matches (111 games), builds xz2 to ye4:
* 6 opponent bots: AlexT101 usquals/finals (11 games), AdamTan12_Finals (4), Varun final4 (4), bman goonv23 (3), thewaxmango collegejunior (3).
* 34872 g0 was dropped because it repeats 34861 g0 exactly (the replica is deterministic).
* 36188 g2 (starvation at r552) is just past the cutoff.

**Files.**
* `kill.jsonl`: per-game reconstruction from `scripts/kill.py`
* `timelines.txt`: 10-round timelines, attackers, throws and grabs
* `agg.md`: per-game table and pooled numbers from `scripts/agg.py`
* `presence.txt`: rats within 5 of each king per 50 rounds, from `scripts/presence.py`

The window is the last 100 rounds before our last king dies. "Near" means within 5 tiles of our king's centre (dist2 ≤ 25). A rat death counts as near within 8.

## The tactic in one paragraph

On small maps (13 of 25 games are 30x30 or smaller; median death r283), the opponent keeps **a steady 1-2 rats at our king from about r35** (first contact median r36). They refill it all game:
* about a third of the attackers are fresh spawns walking straight in, under 20 rounds old, from a king about 14 tiles away
* a third were already waiting within 12 tiles
* a third came from further away

Their rats fight **at our king, not in the middle**. When a rat of ours near the king takes damage, they **grab it**:
* the victim is at median 3.2 tiles from the king's centre, so it stands next to the 3x3
* its median HP is 50, typically just hit by a trap or bitten; the grabber's median HP is 90
* the victim is young, median 13 rounds old; 42% are 10 rounds old or less, so they are our fresh spawns

They carry it a median 3 rounds and **throw it point-blank into our king** from about 4 tiles. That hit deals 26-42 damage to the king and the same to our rat.
* 30% of their throws near our king hit it; the rest smash into other rats of ours or the ground.
* So **our own defenders become the ammunition**, and the throw kills them too: 260 of our 461 deaths in the window are throwLand or throwHit.

Every free rat **bites the king**:
* about 20 bites per game from 4-6 different rats
* bites take priority whenever they are adjacent

**44% of the king damage lands in the last 20 rounds**: enemies near our king go 0.8 → 1.0 → 1.7 → 3.0 at r-100/-50/-20/-5 (up to 7), while ours go 3.6 → 3.1 → 2.2 → 1.5.

**Their king does not advance.** It ends a median 7 tiles from its start (ours 6), and the kings are about 15.8 apart when ours dies.

## Damage to our king

The last 100 rounds, 25 games pooled:

| source | hp | share |
|---|---|---|
| bites by their baby rats | 4925 | 41% |
| **our own rats thrown into it by them** | 3877 | 32% |
| starvation (bank < 2, 10/round) | 2920 | 24% |
| cat | 92 | 1% |
| their own rats thrown at it | **0** | 0% |
| traps, king bites | 50 / 0 | 0% |

* They never throw their own rats at our king. The ammunition is always ours.
* Killing blows: bite 14, starvation 6, throwHit 4, cat scratch 1.

## Three ways the games die

| mode | games | what happens |
|---|---|---|
| **grab-and-throw siege** | 14 (35096, 35148 g0/g2, 35169, 35187, 35560 g1, 35618, 35722, 36188 g0, 36254, 36283 g0/g1, 36659 g0/g2) | the description above |
| bite siege | 4 (34908, 35186, 35423, 36502 g1) | the same presence, mostly bites |
| strangled | 6 (35375, 35560 g0, 35707, 35748, 36238, 36502 g0) | Few enemies at the king. Our babies are down to 0-3 against their 16-34 and the bank hits 0, so the king starves. This is the territory and income loss from top-profile H1, not a rush. |

The grab-and-throw siege (14 games) compared with the other 11:

| | siege games (them / us) | other games (them / us) |
|---|---|---|
| map area, median | 900 | 1575 |
| spawns in the window | 16.3 / 17.4 | 10.9 / 7.4 |
| bank at r-100 | 678 / 572 | 375 / 172 |
| babies at r-100 | 11 / 10 | 15.5 / 5.9 |
| babies at death | **10.6 / 3.4** | 17.1 / 1.9 |
| grabs of the other side's rats near our king (them) / our grabs anywhere | 29 / 27 | 7 / 12 |
| throws of the other side's rats near our king (them) / our throws anywhere | 22 / 17 | 5 / 8.5 |
| rat traps laid in the window | 8.4 / 8.5 | 11.5 / 2 |

**The siege starts as an even fight:**
* bodies, spawns and banks are level at r-100
* our grab and throw counts are about the same as theirs

**The difference is where the fight happens.** Rats within 5 tiles of the other side's king, mean per 50 rounds, all 25 games:

| rounds | 0-49 | 50-99 | 100-149 | 150-199 | 200-249 | 250-299 | 300+ |
|---|---|---|---|---|---|---|---|
| theirs at our king | 0.34 | 0.98 | 1.17 | **1.56** | 0.63 | 0.94 | 1.0-1.9 |
| ours at their king | 0.61 | 0.67 | 0.42 | 0.36 | 0.52 | 0.22 | **0.0-0.15** |

* Our early rush gets there first, but it fades. Theirs builds up and stays.
* Over the whole game we deal a median of about 100 damage to their king; they deal 600 to ours.
* **Our spawns feed the grinder.** In 36254 g2 we spent 1418 → 26 cheese on 35 spawns in 100 rounds (they made 12), and our babies still fell 15 → 2. The pooled bank goes 229 → 14 for us and 475 → 411 for them. That collapse is the 24% starvation share.

An example sequence, 36283 g1 r120-169 (from `timelines.txt` and the action log):
1. A rat trap fires on our rat next to our king (50 damage).
2. An enemy rat grabs it in the same round or the next.
3. It throws it 2-4 rounds later from (8-11, 13-17) into our king at (4-6, 14-16), for 42 damage.
4. Fresh enemy rats arrive within 8-14 rounds of spawning (spawn r135-141, arrive r147-151) and bite the king at r156-164.

## Imitation in our bot (sparring partner)

Our bot already has every primitive:
* biteEnemies ranks an adjacent king first
* tryCarry / tryThrow score a king hit at 3x
* CARRY_KING walks a carried rat to a king

What is missing is a **sustained stream of rats that fight at the enemy king** rather than mid-map.

**(a) The closest imitation with existing constants** (`bots/ratbot/Cfg.java`):

```
RUSH_UNTIL = 300            =>  RUSH_UNTIL = 0              // every replacement born with 4..15 live babies rushes, all game (the stream)
RUSH_LIFE = 150             =>  RUSH_LIFE = 0               // rushers never revert to gathering
CARRY_KING = 8, CARRY_KING_LEFT = 2  =>  CARRY_KING = 10, CARRY_KING_LEFT = 1   // carry grabbed rats to the king from farther and hold for a king hit (they hit after ~3 rounds from ~4 tiles)
RAT_TRAP_MIN_CHEESE = 100   =>  RAT_TRAP_MIN_CHEESE = 50    // trap -> 50-hp victim -> grab; they lay 9.8 traps/100r in the window vs our 5.6
```

* Keep RUSH_FROM=4 and RUSH_TO=16. The spawn index is the live-baby count, so idx 0-3 stay gatherers and their economy is the opponent's: bank held at about 400 with 8-16 babies. The opponent's attackers are about 13 distinct rats per 100 rounds, with 1-3 present at a time.
* Optional, if the trap gate still blocks attackers at the enemy king: `KING_RESERVE = 400 => 250`. The rusher's trap needs the reserve + 20 unless it is near OUR king. This also loosens spawns, so test it separately.

**(b) The smallest behaviour change constants cannot express: an "ignore until the king" gate for ATTACK**

In `Baby` (the move chain), skip faceEnemy / flank / war / retreat and the GrabMicro dodge for an ATTACK rat while it is more than about 6 tiles (dist2 > 36) from a known enemy king. It walks straight in (`attack()`), and the existing trap / grab / throw / bite runs once it is at the king. Inside that radius, tryCarry should prefer enemy babies **adjacent to their king and at 50 hp or less**, the reverse of today's highest-HP pick, and throw the moment a flight hits the king (no THROW_HOLD wait).

Why this is the missing piece:
* Our presence at their king fades from 0.6 to 0.0-0.2, while our grab and throw volume matches theirs.
* Our rushers are fighting en route. faceEnemy and flank run before attack() in the move chain.
* This is a hypothesis from the presence and volume data. Rat roles are not visible in replays.

## Defences suggested by the data

1. **Don't spawn into the siege. Keep the upkeep.**
   * With 2 or more enemy babies within 5 tiles of our king and no more of ours than theirs there, stop spawns (or spawn only on the side away from the enemies), and never spend below about 100 rounds of upkeep.
   * Evidence: grabbed victims are median 13 rounds old, and 42% are 10 rounds old or less. In siege games we out-spawn them (17.4 vs 16.3) yet fall from 10 to 3.4 babies, and the bank goes from 572 to near 0. Starvation is 24% of the king damage.
   * Knobs: SIEGE_FLOOR is 0 today, so siege drops the floor. LAST_FLOOR=20 and LOW_FLOOR=200 also let the last cheese go into rats.
2. **Move weak rats away from the king (they are ammunition).**
   * Victims sit at median 3.2 tiles from the king's centre with median 50 HP. The grabber is healthier (90).
   * AMMO_HP=50 and AMMO_R=3 cover only part of this. Test `AMMO_HP = 50 => 80` and `AMMO_R = 3 => 5`.
   * Also: when a siege is on, keep the king off flights. THROW_DODGE=9 exists already; check that it fires, since 30% of their nearby throws hit the king.
   * A cheaper structural option on small maps: seat the king further back. On DefaultSmall our king walks about 6 tiles toward the middle, and the gap shrinks from 27-28 to 14-16, so fresh spawns reach us in 10-20 rounds. Raise KING_FRONT (20 →) or cap KING_MINE_R2 on maps of 900 tiles or fewer.

## Confidence and limits

* **n = 25 games from 6 opponent bots.**
* The grab-and-throw siege is clear in 14 games, with the same signature in every one. It comes from AlexT101 (both bots, 8 of 11), bman (3/3) and thewaxmango (3/3).
* AdamTan12_Finals and Varun kill by bites (4) or starvation (4) instead. So the tactic belongs to specific teams; it is not field-wide.
* Starvation is entangled. 6 of the deaths are income losses, not rushes.
* Counts are per game and noisy: the 8-114 grab range is driven by map size.
* Bite damage is the nominal 10 per bite. The cheese boost is not in the action, so the bite share is a lower bound.
* The "fresh / staged / travelling" split uses position 20 rounds before arrival. "Staged" can include rats that were simply passing by.
* The imitation constants are a best guess and untested. (b) is untested.
