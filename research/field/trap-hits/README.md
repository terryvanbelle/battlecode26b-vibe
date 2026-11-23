# Enemy rat traps on our rats (replica study, 11-23 11:50 PDT)

**Question:** sw54's replica losses (38282 vs thewaxmango_collegejunior, the 38236 cheesefarm game vs AdamTan12_Finals) raised two candidate behaviours: our army collapsing under the spawn floor (entered as yl17), and enemy traps. How much of the loss gap is traps, and is any of it avoidable?

**Data:**
- *Death causes:* 33 games from 11 matches, 37270-38282. These were played by several champions, but the trap code (TrapRisk, the avoid gate) has not changed since the u-line.
- *Trap hits:* 2,060 enemy rat-trap triggers on our rats, in the 18 games of 38236, 38203, 37611, 37554, 37553 and 37402 (opponents AdamTan12_Finals, AlexT101_usqualsbot, VarunVejalla_final4_run_away10).
- *Method:* replays dumped by tools/game_report.py. The scripts are in scripts/ (run them from the repo root):
  - traphits.py: who laid the trap, where, and its age;
  - trapseen.py: who of ours saw the lay, and whether the victim was thrown onto the trap.

## Death causes, per game

| | games | our rats lost | to enemy traps | thrown (land + hit) | their rats lost | to our traps | thrown |
|---|---|---|---|---|---|---|---|
| our losses | 17 | 130.6 | **34.3** | 64.1 | 111.4 | **22.2** | 55.6 |
| our wins | 16 | 69.0 | 13.3 | 29.0 | 85.9 | 16.2 | 39.8 |

- **Traps are the biggest gap in losses.** Enemy traps kill 12 more of our rats a game than ours kill of theirs. In wins it is the other way round, by 3.
- **The worst games:**
  - ZeroDay vs AlexT101: 151 of ours to traps, against 73 of theirs.
  - pipes vs AlexT101: 92 against 38.
  - cheesefarm vs AdamTan12_Finals: 82 against 49.
- **thewaxmango is different.** Its traps barely touch us: 0-8 a game in 38282 and 37306. It wins by throws (75-158 of our rats a game).

## Engine facts

From bc26-engine-src:
- **The rat trap:** TrapType.RAT_TRAP costs 20 cheese and does 50 damage. It stuns the victim for 30 movement-cooldown turns. At most 25 are live per team.
- **When it fires:** on entering any tile within dist2 2 of the trap (its 3x3), whether by a move, a landing or a drop. A rat already inside the 3x3 when the trap is laid does not fire it until it moves to another tile in it. The trap is removed when it fires.
- **The grab rule:** a rat can grab an enemy that cannot sense it (out of the enemy's 90° cone), or whose health is below its own (HEALTH_GRAB_THRESHOLD is 0). So one trap hit (50 damage) makes our rat grabbable from the front by any healthier enemy. Traps feed the throw deaths.

## The 2,060 enemy-trap hits on our rats

**Who laid them, and when:**
- Babies laid 92%. Kings laid 26 (1%). For 7% the layer is unknown: the trap was laid where the dump has no lay row.
- 91% fired within 150 rounds of the lay; only 35 were older. So TrapRisk's 150-round memory covers them, and a longer TTL would add nothing. Shorter TTLs (z5h 40, z5i 15) were already rejected.

**Where:**

| place | hits | share |
|---|---|---|
| far from both kings | 944 | 46% |
| within 8 of the enemy king (beyond 3) | 543 | 26% |
| within 8 of our king | 493 | 24% |
| within 3 of the enemy king | 80 | 4% |

**Who of ours saw the lay** (victim, or any of our robots in view of the layer that round, using the engine's vision cones):

| | hits | share |
|---|---|---|
| the victim saw it | 655 | 32% |
| another of our robots saw it, the victim did not | 710 | 34% |
| nobody of ours saw it | 425 | 21% |
| thrown onto the trap | 132 | 6% |
| layer unknown | 138 | 7% |

## What has been tried against this (progress/attempts.csv)

- **Avoidance during fights.** TrapRisk's avoid mask is off while hunting prey or with an enemy within dist2 20 (Baby.java: `!hunting && !engaged`). Every way of keeping it on in fights failed or never ran:
  - s1l: dropped the mask in fights (the current design);
  - RISK_GATHER (s5u, s8g, u9n) and RISK_IN_FIGHT (s5v, s8f);
  - RISK_FRESH (t0b), RISK_ADAPT (u0u, zfg), FACE_HOLD (u1l), RISK_NONFIGHT (u6d).
- **Sharing sightings.** TRAP_SHARE (shared trap-risk squeaks, u-line) was ablated by u9b, a near-miss of +2. MINE_BURN, which bans a mine where a trap fired, was inert (zgb, 0.945x trap deaths).
- **Memory.** RISK_TTL 40 and 15 were rejected (z5h, z5i).

## Conclusion (11-23)

- **The avoidable share is small.** Most hits were seen by someone. But the two remedies — keeping the mask in fights, and sharing sightings — have each failed several times, on lines with the same TrapRisk.
- **The remaining lever is our side of the exchange.** That means more and better-placed traps of our own, which make their rats grabbable first.
- **No new newcomer from this study.** The tournament already rates that lever: sw59 (trap-first: BABY_TRAP_DIST2 40, RAT_TRAP_MIN_CHEESE 40, DEF_TRAP_DIST2 400) and sw54's forward traps (FWD_TRAP). yl17 rates the spawn floors.
