package xk5;

/** Behaviour switches and constants. Every experiment adds a switch whose default is the incumbent. */
public final class Cfg {
    public static final int MAX_BABIES = 18, MAX_BABIES_WAR = 18;
    /** With RICH_BANK global cheese or more the baby cap rises to RICH_BABIES (0 = off): a surplus buys bodies. */
    public static final int RICH_BABIES = 40, RICH_BANK = 1000;
    /** xf9 (explore line, owner #31): the RICH tiers open at round RICH_FROM (0 = off), Merlin's economy. Block #385's
     *  Merlin losses: at r50 we fielded 29-31 babies on DefaultLarge (bank 885-900) to their 16-18 (1764-1808), and
     *  33 to 24 on cheesebottles at r150 (bank 900 to 1210); our army then fell to 7-10 by r150-200 while theirs held
     *  16-25, and their bank stayed 1000-2700 all game (second kings by r300-400). zft (RICH_FROM 150 on zdb) went 33
     *  to 33 over two local chains (+3 -0 small maps, +2 -5 larger). */
    public static final int RICH_FROM = 300;
    /** xg2 (x-line): RICH_FROM only on maps of at least RICH_FROM_AREA tiles. xf9's s6 screen lost tiny A (abrk,
     *  araid), tiny B (araid) and DefaultSmall A (abrk) to xf8 and won none (zd665, t -2.07); Merlin's early-RICH
     *  wins were on 1180-3600 tile maps (cheesebottles, whatsthecatdoin, FreeTheCheese, DefaultLarge). */
    public static final int RICH_FROM_AREA = 0;   // xj5: RICH_FROM on every map (see KING_RESERVE)
    /** xj4 (x-line): austerity. From round AUSTERE_FROM (0 = off), while the kings' projection (Comms.BUDGET, Econ
     *  surplus at round 2000) is 0 or less, babies lay no rat traps and no cat traps away from a king (lane or ring), and
     *  the king's core count drops to AUSTERE_CORE (war spawns at the king stay). Replica externalsustenance vs
     *  YokoOno (lost A 6 of 7 since 26097, B 5 of 6): every mine is outside the walled box both kings start in; both
     *  kings starve, ours first in A (26636: 47 spawns, 31 rat traps, 7 cat traps, bank 1 at r226, starved r270;
     *  theirs 193) and theirs first in B (26637, r369). tiny vs collegejunior (26611): both banks 0-80 from r140. */
    public static final int AUSTERE_FROM = 60, AUSTERE_CORE = 99;
    /* xj7 (owner #32, starvation): AUSTERE_CORE 99 (was 4, no core cut). xj6's starvation panel: on Stash B vs YokoOno
     * (26804) the king held 4 babies from r140 to r500 on a 1300-1500 bank (income 40; the projection was below 0, so
     * BUDGET 0 cut the core to 4) and starved at r782; xi9 (26637) kept 20 babies, spent the bank to ~200 and won at
     * r818. The same 4-babies-on-700+ hold: Meow A 268 rounds, Meow B 189 (both lost); none in 18 xi9 #392 games.
     * KING_RESERVE already keeps the upkeep. */
    /** xj5 (x-line, owner #32: one common losing behaviour): every optional spend leaves KING_RESERVE global cheese per
     *  king (0 = off; never more than the upkeep to round 2000): spawns (over LOW_FLOOR, SPAWN_FLOOR, CORE_FLOOR,
     *  WAR_FLOOR; LAST_FLOOR's lone 10-cheese baby and SIEGE stay), babies' rat traps and cat traps away from a king; digs
     *  keep DIG_RESERVE. The king starved in 32 of 85 lost dumped replica games since 26000 (the top cause; bites 20,
     *  points 20): mincemeat vs Icarus 7, tiny 3, t_minus 2, externalsustenance 2, closeup, averyfineline, evileye,
     *  sunandmoon, DefaultLarge, whatsthecatdoin, Stash, Meow 2. In the war ones our bank at r50 was 755-1585 to their
     *  1024-2010 (RICH spawning at r0), at r150 0-400 to 46-1000, and at r300 0-100 while theirs held 55-470 in 6. */
    public static final int KING_RESERVE = 600, DIG_RESERVE = 300;
    /** xj9 (owner #32): a combat rat trap against an enemy within DEF_TRAP_DIST2 of our nearest king skips the
     *  KING_RESERVE and austerity gates (0 = never). Replica averyfineline vs Finals (20x20, war from r17): xi9 laid 36
     *  rat traps (A, 26661) and 31 (B, 26662) and won both; xj6 laid 21 (26805) and 7 (26806) and lost both, king
     *  bitten at r110 and r122 (Finals lost 8 and 3 rats to our traps, against 13 and 10). */
    public static final int DEF_TRAP_DIST2 = 64;
    /* xj6: FORM_NET100 100 (from -100): a king forms only while the trusted income beats every king's upkeep, the new
     * one's included, by 1 a round. 9 of the 38 starved kings in 97 lost dumped replica games had formed a king that
     * the income could not feed: cheesebottles A vs Merlin (26657) formed at ~r150 on 3.3/round trusted (4 upkeep),
     * bank 1924 at r201 to 84 at r1501, starved r1557 (theirs 4018); also 26658, 26055, 26081 RUN, 26120, 26153, Meow
     * 26466/26602, 26635. RICH_LATE off (was 950): km82 cheesebottles B vs agrab (xj6 with it on) formed its third king
     * at r1144 on a 1220 bank (Econ.surplus(3) past 2000); the merge took 13 of 19 babies, transfer fell from 680 to
     * 280 a 100 rounds, and all three kings starved by r1964. */
    /** A second tier: with RICH2_BANK or more the cap rises to RICH2_BABIES (0 = off). u0m's set-2 superioristhmus
     *  games (3796/3797 vs outercloud) ended with 22-31k cheese banked and 40 rats (the RICH_BABIES cap) from r500 on,
     *  while they ran 53-66; abcdezyxwv runs 50-127 with ~2k banked. Babies eat nothing: only the spawn price. */
    public static final int RICH2_BABIES = 80, RICH2_BANK = 3000;
    /** Cheese kept when spawning under attack. */
    public static final int WAR_FLOOR = 300;
    /** Hard floor under every spawn (king upkeep). */
    public static final int SPAWN_FLOOR = 300;
    /** With fewer than LOW_BABIES babies every floor drops to LOW_FLOOR: a king without rats has no income. */
    public static final int LOW_BABIES = 8, LOW_FLOOR = 100;
    /** At war with enemies in the king's sight every spawn floor drops to SIEGE_FLOOR (0 = off): a king killed holding
     *  its upkeep reserve wasted it. 2094 DefaultSmall vs AdamTan12: from r60 our bank sat at 308 then 148-108 (the
     *  300 floor, 8 babies) while theirs went 802 -> 12 into spawns; at r100 their 9 rats round our king met our 8,
     *  and it died at r117 holding 102. */
    public static final int SIEGE_FLOOR = 0;
    public static final int BABY_TRAP_DIST2 = 13;
    /** Cheese (global, or raw+global for babies) below which no rat trap is placed. */
    /** xc2 (zdy's set: BABY_RAT_TRAPS + TRAP_ADJ + TRAP_CAP 25 + TRAP_SPAWN_FIRST + this 900): replica 25315
     *  wallsofparadis A vs Finals (xb8), r0-600: Finals laid 111 rat traps and our rats set off 107 (50 damage each),
     *  we laid none, and it picked 10,600 to our 5,568. The 900 floor keeps u8f's 300-floor starvation off.
     *  xh1 (x-line, owner #31): 100, with TRAP_SPAWN_FIRST off. Field dumps (block #385/#386 era): the opponents lay
     *  24-32 rat traps a game (95% fire; 50 damage and a 30-round stun each) to our 8-10. final4 on arrows (26052 g0,
     *  us B) laid 80 on a bank of 30-210, 62 at Chebyshev 1 of our nearest rat, 44 of them by r200; ours sat at
     *  21-295, under the 900 floor all game, and we laid none. */
    public static final int RAT_TRAP_MIN_CHEESE = 100;
    /** Most live rat traps we keep (engine max 25): new ones are bought only as old ones fire. */
    public static final int TRAP_CAP = 25;
    /** xb8: WALL_BLIND traces at most this many lines a turn (0 = no cap). xb4's screen zd619 had 38 overruns, 37 on Stash
     *  seat B: an attacker stuck at the pocket wall traced every enemy behind it each turn (nearest-enemy loop and prey(),
     *  ~300 bytecodes a trace) and crossed 17,500 at tryCarry or tryDeliver (OVS 2122/809) about every other turn. */
    public static final int WALL_BLIND_CAP = 6;   // xd6: was 8. xd2's s6 screen zd636 overran 3 times on Stash A vs
    /** RUSH_BFLOW only on maps of at least this many tiles (zir lost the tiny rush races with it). */
    public static final int RUSH_BFLOW_AREA = 600;
    /** xc3: a rusher whose Nav stall reaches BF_GIVEUP leaves BitFlow for Flow (0 = never). averystrangespace (start
     *  corner walled by dirt) vs agrab seeds 163-166: xb3 broke out 1 of 3 (rushers at nav stall 100-478 in dirt
     *  pockets), xb2 3 of 3, xb8 1 of 6, xb8 without RUSH_BFLOW 3 of 3; replica 25325/25326 (xb8) lost both seats vs
     *  Icarus, which zj3 and xa5 won 4 of 4. Flow's rushers dig through. */
    public static final int BF_GIVEUP = 60;
    /** BitFlow.follow holds the rat still when fewer bytecodes than this are left (its 8-neighbour scan is unbudgeted). */
    public static final int BF_FOLLOW_LEFT = 3000;
    /** Scored combat movement (Micro) for babies at war. */
    /** War role split: 0 everyone fights, 1 gatherers ignore non-adjacent enemies, 2 and never freeze to face. */
    public static final int WAR_GATHER = 2;
    /** The post-move throw scan runs only with this many bytecodes left (it costs up to ~6000). */
    public static final int THROW_BC = 7000;
    /** At war a FIGHT rat takes its mine from the contested home-half mines, those whose Chebyshev steps from the
     *  mirrored enemy start exceed those from home by at most FIGHT_POST (0 = off; none known: every home mine). In
     *  u4e's 48 field losses the enemy stood at such mines 11304 mine-rounds (r150-799) with none of ours within 4 in
     *  66% of them, and took 441 cheese a mine at margin 3-8 (wins: 3708 rounds, 46%, 63 a mine); fighters, who engage
     *  enemies within d2 20 at war, spread over every home mine by slot like gatherers. */
    public static final int FIGHT_POST = 0;
    public static final int PLUG_LEFT = 3000;
    public static final int KING_MINE_R2 = 400;
    public static final int KP_MAX = 32, KP_RESERVE = 2500, KP_TURNS = 40, KP_RETRY = 30;
    /** KING_PATH: no centre on the path or seat nearer the bisector between our start and the enemy's than KP_FRONT
     *  percent of the start's own distance from it (0 = off). kpa krotus (both mines in dirt pockets walled on the
     *  edge side): u1x's king went in from the centre side, 5-7 from the axis, and was bitten to death there in both
     *  games (A r283, B r392); u0m's greedy king stuck at the edge and lived. */
    public static final int KP_FRONT = 20;   // xe3: as the x-line's KING_FRONT 20
    /** xe4 (x-line, owner #31): when a KING_PATH search at KP_FRONT finds no path, the next search (at once) keeps
     *  centres only KP_FRONT_LOW percent of the start's distance on our side of the bisector (-1 = off); a failed low
     *  search hands back to KP_FRONT. kingmove.py over the cached replica dumps (king's Chebyshev displacement from
     *  its start by r300, ours vs theirs): whatsthecatdoin 0.1 vs 16.2 (win 0.12, Merlin), wallsofparadis 0.0 vs 13.8
     *  (Finals), Nofreecheese 0 vs 13.6 (win 0.20, collegejunior), clockwisdom 0 vs 10.2 (win 0.17, Xzibit). xe3's
     *  km2 Nofreecheese king stayed at (6,5) with km=n all game: its home mines (36,21), (42,15) and (50,8) all lie
     *  under 20 percent (about 5) from the bisector, so no seat passes. krotus keeps KP_FRONT's edge-side approach. */
    public static final int KP_FRONT_LOW = 1;
    /** King.pickHomeMine skips a mine whose front value 100 (e - d) / L2 is under KING_FRONT (0 = off): d, e = dist2
     *  from the mine to our start and the enemy's, L2 = dist2 between the starts; 100 at our start, 0 on the bisector.
     *  The king takes the first such mine anyone lists and keeps it. dirtpassageway (kings (3,26) and (46,26) in an open
     *  middle band): the centre mine (20,26), f 20, 9 tiles from its twin, is listed before (7,39) and (7,12) (f 81,
     *  behind dirt); u4e's king sat there from r150 and was bitten to death (6060: hp 562 at r150, 4 at r500), the
     *  enemy's king damage 1108-1170 to our 68-114, and u4e and u3z lost all 8 field games there. u3z's 96 field games:
     *  a king past halfway to the bisector at r200 won 5 of 15 (10 died), any other 46 of 81. */
    /** xa1 (explore line): KING_FRONT 20 on every map (zjd/zjf's switch, unledgered). zjd's king lived 160-310
     *  rounds longer on DefaultMedium vs final4 and collegejunior. */
    public static final int KING_FRONT = 20;
    /** Footprints ahead of a cat that King.catRisk scores (2 = the old 250/75 pair). */
    public static final int CAT_RISK_STEPS = 6;
    /** The king steps off the flights of enemy babies within Chebyshev THROW_DODGE that hold one of ours (King.throwDodge;
     *  0 = off, negative = run but never act). */
    public static final int THROW_DODGE = 9;
    /** Cats within this dist2 of the king's centre make it dodge. */
    public static final int KING_DODGE_R2 = 25;
    /** xe9: the boxed-in dig scores 8 more steps; skipped below this many bytecodes left. */
    public static final int DODGE_DIG_LEFT = 5000;
                                                   // and 3158 -> 2308, neutral on wins)
    /** xe2: King.catRisk adds this per lane tile under the king's 3x3 (CAT_LANE). xe1's attack_on_cat A king (kf2 vs
     *  agrab) dodged from (2,42) into the 3-wide x = 0..2 corridor the cat patrols and was chased 20 tiles south. */
    public static final int LANE_RISK = 40;
    public static final int KING_DIG_MIN = 100;
    /** With no enemy or cat in view the king walks to cheese within Chebyshev KING_ROAM of its mine (0 = off). */
    public static final int KING_ROAM = 4;
    /** The king's walks to a pile (KING_ROAM) and to its home mine take only steps that bring it strictly closer
     *  (King.closer), not the sidesteps King.step allows (also not the mine walk's fallback when no dig opens the
     *  way, mode 'z'). fullofsecrets (local u4e vs s7d, field 6134 B): the king saw piles of mine (2,26)
     *  through its pocket's south wall and stepped (2,32)-(3,32)-(2,32) every 4 rounds all game (97 of 123 moves; 6134 B
     *  121 of 275). Each step was a new delivery target, so carriers outside the wall never reached FLOW_STALL 4 and
     *  transferred 0 of 4630 picked; the king starved at r545. */
    public static final boolean KING_STRICT = false;
    /** xb1 (explore line, owner #31): Merlin holds one king to ~r1900 and forms its second for the end (FreeTheCheese
     *  25080: 306 babies spawned to our 175, cat damage 10.9k to 6.8k, transfer 22.6k to 13.7k; our second king from
     *  ~r300 paid ~3,400 upkeep). FORM_FROM 1200 (from 80): no mid-game king; END_KING and CASH_END still form one.
     *  xc6 restores 80: xc3's replica block (#380) vs zj3's (#378). rift vs spsquared, both seats: on one king to
     *  r1850 they formed a third at r464/r619 and won B on points 3 kings to 2 (zj3's second at r103/r106 held them at
     *  two, W +27/+33). FreeTheCheese A vs collegejunior: zj3's second at r332 won 2 kings to 1 (+41); xc3 went 2-2
     *  (their second r1479) and lost by 9. FreeTheCheese B vs Merlin, xb1's target, lost both ways (1-2, 2-2). */
    public static final int FORM_FROM = 80, FORM_BABIES = 16, FORM_CHEESE = 300, FORM_TTL = 60, FORM_RETRY = 150, FORM_PULL_R2 = 72;
    /** Kings we form up to (engine max 5 before round 1200). */
    public static final int FORM_MAX = 3;   // xh7 (was 2): a third king, kept fed by FLOOR_PER_KING
    /** xh8: a site for a third or later king opens only while the bank holds this much (0 = off). km58 cheesebottles A
     *  vs agrab (xh7): kings 2 and 3 formed at r325 and r370 on a 1.5k bank and merged 8 babies each (20 -> 11); the
     *  third king's 900 floor (FLOOR_PER_KING) then held the bank at 560 with no respawn, babies fell to 6, the bank
     *  hit 0 at r600 and all three kings starved by r700. Finals holds 1.2-1.9k while it forms its 3rd and 4th kings. */
    public static final int FORM3_BANK = 1200;
    /** At war: kings we form up to (0 = FORM_MAX). After a backstab living kings weigh 0.5 of round-2000 points. */
    public static final int FORM_MAX_WAR = 0;
    /** Rich kings (0 = off): up to round 1150, while the bank holds FORM_RICH_BANK or more, the first king names sites
     *  past the cap, up to FORM_RICH_MAX kings, with no income test and a ring site beside it when pickFormSite has
     *  none. Kings alive at round 2000 weigh 0.5 of war points and only a second can form after round 1200. rift vs
     *  spsquared (4342, 4343, 4443): at the 39-40 baby cap we banked 3.5k by r600 and 6-8k by r1050 on one or two kings
     *  (13k at the end) while they held three, and lost each on points 2 kings to 3 with more cat damage and transfer. */
    public static final int FORM_RICH_BANK = 3000, FORM_RICH_MAX = 4;
    /** Form only while the trusted income (x100 per round) beats every king's upkeep, the new one's included, by
     *  FORM_NET100 (Integer.MIN_VALUE = no income test). */
    public static final int FORM_NET100 = 100;   // xj6 (owner #32, starvation): was -100; see KING_RESERVE
    /** A baby outside the site's 3x3 joins only while it sees fewer than FORM_JOIN allied babies nearer the site;
     *  the rest keep working (0 = every baby within FORM_PULL_R2 joins). */
    public static final int FORM_JOIN = 8;
    /** An endgame king: from round END_KING a lone king names a site for a second (a clear 3x3 off its body, else
     *  pickFormSite's) whenever 7 babies and END_KING_CHEESE global cheese are there, with no income test and no
     *  retry wait past FORM_TTL (0 = off). Kings alive at the end weigh 0.5 of the points at war, 0.3 in cooperation,
     *  so a second king at 1-1 moves 8-16 points to us; s9s's set-4 limit games (9 of 42) all ended at 1-1 or less
     *  for us, and averystrangespace (3287) was lost 24-25 with no cat damage either side. */
    public static final int END_KING = 1850, END_KING_CHEESE = 400;
    /** xa5: war strikers attack in waves (Baby.waveHold): rally WAVE_FRAC percent of the way to the enemy king spot
     *  for the first WAVE_GATHER rounds of every WAVE_P, then charge together (0 = off). */
    public static final int WAVE_P = 150, WAVE_GATHER = 100, WAVE_FRAC = 50;
    public static final int WAVE_MIN = 14;
    /** Bytecodes the king's first-turn sense leaves for the rest of that turn (spawn first of all). 5000 can be short:
     *  on DefaultMedium an s7e king ended its first turn at 20001 of 20000 and missed the round-2 spawn (s7g: 7000). */
    public static final int KING_SENSE_RESERVE = 7000;
    /** Health at or below which a baby near enemies retreats to its king (0 = never). */
    public static final int RETREAT_HP = 40;
    public static final int AMMO_HP = 50, AMMO_R = 3, AMMO_D2 = 8;
    /** Guard facing: with an enemy rat within this squared distance, steps strafe so we keep facing it, and a
     *  turn ends facing it (rats are grabbed from behind). 0 = off. */
    public static final int GUARD_DIST2 = 13;
    /** Bite cheese (c = k*k gives +k damage), only when it makes the bite a kill: a king pays up to KING_BITE_MAX
     *  above KING_BITE_FLOOR, a baby up to FINISH_K*FINISH_K above FINISH_FLOOR. */
    public static final int KING_BITE_MAX = 100, KING_BITE_FLOOR = 300, FINISH_K = 5, FINISH_FLOOR = 200;
    /** RESCUE only for a rat within Chebyshev RESCUE_KING_R of our nearest king (both the rescue walk and tryCarry's
     *  carrier bonus). zj3: every RESCUE build (zie, zih, zil, zio) won thunderdome vs Varun, where its grabs were 2-4
     *  from our king, and lost externalsustenance B and mincemeat A, where the first changed grab was 11-19 from it. */
    public static final int RESCUE_KING_R = 7;
    public static final int RESCUE_D2 = 8;
    /** Throws: an enemy baby hit beyond tile 2 counts THROW_FAR_PCT percent; below a THROW_MIN score (42 = a
     *  point-blank wall or rat) the carrier holds for up to THROW_HOLD rounds after the grab. */
    public static final int THROW_FAR_PCT = 40, THROW_MIN = 84, THROW_HOLD = 6;
    /** xg8 (x-line, owner #31): a carried enemy thrown into a cat dies outright, whatever its health, and the cat
     *  sleeps 2 rounds (InternalRobot.travelFlying). The field fed our rats to cats 1.6-3.1 times a game (VM dumps of
     *  block #385/#386 era: collegejunior 310 in 99 games, final4 178 in 72, Icarus 132 in 72, YokoOno 112 in 69) to
     *  our 0.1-0.3, as tryThrow scored the cat -50. A cat first on the flight scores CAT_FEED at tiles 1-2, 60% at 3-4
     *  and 30% beyond (Baby.feedScore; 0 = the old -50), and aimCarry lines up point-blank cats as it does enemies
     *  (a baby 84, a king 126). zc7 (z3x + a softer score, 11-08) fed 53 times locally, dropped at a strict falsifier. */
    public static final int CAT_FEED = 150;
    /** xg9 (x-line, owner #31): a carrier of an enemy rat that sees a cat within CARRY_CAT (Chebyshev; 0 = off) and no
     *  enemy king (CARRY_KING goes first) walks to it, holding every throw below a point-blank feed until
     *  CARRY_KING_LEFT rounds of the carry are left; aimCarry lines the feed up once it is a step away. final4 on arrows
     *  (26052 g0, us B: 18 of our rats fed) grabbed 1-6 tiles from a cat, carried 2-7 rounds and threw from 2-4. */
    public static final int CARRY_CAT = 6;
    /** xe8 (x-line, owner #31: Merlin's and collegejunior's throws onto our king): a carrier of an enemy rat that sees
     *  an enemy king within CARRY_KING (Chebyshev; 0 = off) walks to it (CARRY_AIM lines the point-blank throw up once
     *  it is a step away) and holds every throw that would not hit a king, until CARRY_KING_LEFT rounds of the carry
     *  are left. Replica dumps: Merlin hit our king 344 times with our own rats in 87 games (we hit theirs 101), from
     *  grabs 2-6 tiles from our king carried to Chebyshev 2 in 2-7 rounds (25799 DefaultLarge: 6 hits, our king dead
     *  r294). Our grabs 5-8 tiles from an enemy king hit that king 241 times in 7140 (0-4 tiles: 1095 in 2803). */
    public static final int CARRY_KING = 8, CARRY_KING_LEFT = 2;
    /** xf3: a carrier that holds its throw (THROW_HOLD, CARRY_KING) throws now when an enemy baby with free hands is
     *  within Chebyshev THREAT_THROW (0 = off) and no flight hits one of ours: the engine drops a grabbed carrier's
     *  rat, and km14/km15 dumps had carriers (mv T) grabbed at 8.6-11.9x the rate of all our war turns. */
    public static final int THREAT_THROW = 2;
    /** The carry aim stops its search once AIM_LEFT or fewer bytecodes are left this turn and takes the best throw found
     *  so far. Stash (local u4t vs araid, seed 0, A): a carrier's aimCarry spent 6623 of its last 7000 bytecodes, found no
     *  throw, and the turn ran over in the pick-up stage that came after (r136 and r317, OVS 2021/2122). */
    public static final int AIM_LEFT = 3000;
    /** Squared distance within which a baby answers a king's alert. */
    public static final int ALERT_DIST2 = 225;
    public static final int KING_THREAT_D2 = 18, DEFEND_D2 = 100;
    /** Raw cheese at which a gatherer heads home. */
    public static final int DELIVER_AT = 30;
    /** Raw cheese at which a gatherer with no pile in sight heads home (at or above DELIVER_AT it goes anyway). The
     *  top field bots carry 33-41 cheese a transfer to our 24-31 (pipes, safelycontained, sittingducks vs AlexT101 and
     *  abcdezyxwv); our gatherers spend 29-39% of their turns walking to a mine ('M') and 11-19% delivering. */
    public static final int DELIVER_IDLE = 10;
    /** Full loads (0 = off): a gatherer heads home only once it carries LOAD_AT raw cheese (or late), replacing the
     *  DELIVER_AT / DELIVER_IDLE rule of the trip home; with a part load and no pile in sight it stays at its mine.
     *  u4e's field games: our transfers average 27.5 in losses (77% at 10-29) to the opponents' 45.8, our cycle is
     *  27 cheese per 63 rounds to their 44 per 72, and both sides leave half of a worked mine's cheese unpicked. */
    public static final int LOAD_AT = 0;
    /** Breakout (0 = off): a baby that gives up two chases of walled-off cheese within 100 rounds, or whose explore
     *  search fails, spends BREAKOUT_TTL rounds with CHEESE_LOS on and exploring toward the never-seen tile nearest by
     *  path cost (Flow's frontier search; dirt on the way is dug). EscapeTheNight 2233 and local etn1: the king's
     *  room opens only through 12 dirt tiles; all 20 babies chased piles seen through its wall ('C' 52-85%) and
     *  explore searches toward the map centre timed out (25 turns), so the team dug 3 tiles in 1000 rounds. */
    public static final int BREAKOUT_TTL = 150;   // xa6: harvest bundle (s9i)
    /** A goTo target within Chebyshev NAV_SAME_R of the last one keeps the stall count and bug state (0 = only an
     *  equal tile does). fullofsecrets: carriers with up to 490 cheese sat 76 rounds outside the wall of the king's
     *  pocket; each step of the roaming king was a new target, so the stall never reached FLOW_STALL. */
    public static final int NAV_SAME_R = 0;
    /** A gatherer counts as at its mine within Chebyshev MINE_KING_R of it when one of our kings sits within 2 of the
     *  mine (0 = off: always 2), and wanders only to tiles off the kings' bodies. FreeTheCheese: the king's 3x3 covered
     *  the mine, the free tiles within 2 were taken, and a gatherer pathed into the king for 60 rounds. */
    public static final int MINE_KING_R = 0;
    /** xh2 (x-line): Baby.wanderAround draws its tile within Chebyshev WANDER_R of the mine (0 = the old 3). atMine
     *  holds at 2, so a ring-3 draw sent the rat out (W), then back in (M) the next turn: 2-11% of our rat turns in
     *  km43 (arrows 5-11%) and km45 (xg6 Stash A vs araid: gatherer 11461 two-tile flips for 10-20 rounds, 8 times
     *  r326-579, at 15-17k bytecodes a turn and 2 overruns). The draw alone left 3.3% (km46: arrows B (2,10) behind
     *  a wall, the way to a ring-2 tile ran through ring 3), so a rat that wandered at the mine last turn also stays at
     *  it out to WANDER_R + 1. */
    public static final int WANDER_R = 2;
    /** TRAIL's stall count and homing carry over a gap of up to TRAIL_GAP rounds between delivery turns (0: only from
     *  the round before). The move decision runs on movement-ready rounds only, and carried cheese slows moves 1% a
     *  unit, so from 20 cheese on a carrier skips rounds; with 0 the count restarted at each skip and never reached
     *  FLOW_STALL, so TRAIL never fired (pb4 on clockwisdom: carrier 11605 walked from 47 to 128 steps out, its count
     *  0-1 every turn, with a 46-tile trail home). */
    public static final int TRAIL_GAP = 12;
    /** TRAIL's trigger: delivery turns without getting closer to the nearest king before the trail is walked (was
     *  FLOW_STALL 4). fullofsecrets A vs araid (z3x, local cq7): gatherer 11964 held 40-350 cheese at (6-9, 27-32) from
     *  r160 to r700, outside the pocket wall at x 7 and y 29 round our king at (1-3, 32), while Nav's own stall stayed
     *  0-3 (each step of the roaming king is a new target). u4o (TRAIL at 4, unledgered) lost rose A/B on the replica. */
    public static final int TRAIL_STALL = 20;
    /** An attacker takes no pile that would lift it above this, empty-handed or not (0 = off). Attackers run the
     *  after-move pickUp but never deliver. s9s's 126 field games from r400: attack-move turns carried 43-82 cheese
     *  on average and 10-33% of them 100+, and those moved on 31-39% of turns (1% slower per unit carried). */
    /** xd4 (x-line, owner #31: Finals never carries more than 200 raw in any replica game): a non-attacker takes at
     *  most CARRY_CAP - raw of a pile (pickUpCheese(loc, amount)), leaving the rest on the tile (0 = off). xc6's set-3
     *  block lost 123,694 cheese on dead carriers to the opponents' 87,953; its rose carriers held 660-1,997 at
     *  20x cooldown. */
    public static final int CARRY_CAP = 200;
    public static final int ATK_CARRY = 25;   // xd3 (x-line): BitFlow rushers now leave walled pockets and walk long
    /** A stalled attacker delivers: one holding ATK_STUCK_RAW or more raw whose Nav walk has not got closer to its
     *  target for ATK_STUCK turns takes the load home until it has none (0 = off). fullofsecrets A vs agrab (zgh,
     *  cq172): at r400 six attackers held 40-800 raw (1,870) with Nav stalls up to 54 toward the enemy king in its
     *  walled pocket, while ours starved (picked 4,840, transferred 305). */
    public static final int ATK_STUCK = 30, ATK_STUCK_RAW = 100;
    /** Babies spawned before any fighter is assigned. */
    public static final int GATHER_FIRST = 6;
    /** One in FIGHT_EVERY later babies is a cat fighter. */
    public static final int FIGHT_EVERY = 2;
    public static final int CAT_LAY_GAP = 20, CAT_LAY_AGE = 3, CAT_LAY_FLOOR = 400;
    public static final int RUSH_FROM = 4, RUSH_TO = 16;
    /** Baby rat traps (BABY_RAT_TRAPS) only on far maps (0 = every map): a newborn may lay them only when no live
     *  symmetry puts the enemy start (its king's start mirrored) within Chebyshev TRAP_DIST. u4m's trap set changed 11
     *  of 48 field cells: it lost the near maps (tiny, uneasy_alliance, knifefight, DefaultSmall: the opening cheese
     *  goes to traps while the fight is already on) and gained the far ones (Stash, arrows, DefaultMedium,
     *  superioristhmus). In u4s's in-band losses 12.1 of our rats a game die to enemy traps, theirs 0 to ours. */
    public static final int TRAP_DIST = 0;
    /** Bite order counts loot (0 = off): an enemy baby carrying raw cheese ranks as if up to CARRY_BITE health weaker
     *  (never ahead of a rat carrying one of ours), since a rat killed drops its cheese where it dies. u4s's in-band
     *  losses: by r400 the enemy picked 872 cheese a game in our half (127 in wins), we 1899 there of ~2770. */
    public static final int CARRY_BITE = 0;
    /** Stalled attackers hunt cats (0 = off): during cooperation an attacker that has stayed within Chebyshev 3 of one
     *  tile for STALL_CAT rounds walks, for the rest of cooperation, to a cat in view (approachCat), else the cat last
     *  known, else the centre tile on its own side of each centre line; the turn's usual steps bite the cat and lay its
     *  cat trap. Cooperation points weigh cat damage 0.5. u2g's 144 field games: 39% of attack-move turns were stalled
     *  (within 3 tiles of the rat's tile 50 rounds before), 1380-1630 a game. averystrangespace vs AdamTan12_Icarus
     *  (5080/5081, 5335/5336): cooperation to r2000, kings 2 to 1, delivered even (4505 vs 4575), cat damage 0 to
     *  87-289, lost on points on both sides; one Icarus rat bit a cat 23 times (r127-149), while our 12 rushers stood
     *  in the dead end at (7-8, 22-26) from r500 to r1500. */
    public static final int STALL_CAT = 100;
    /** Rush spawns stop at this round (0 = never): the spawn index is the live-baby count, so without it every
     *  replacement born with 4-15 babies alive is an attacker all game (rsu1: s8u lost both DefaultSmall games to s7d
     *  in a long war, 7 rats to 13-15 at r150). */
    public static final int RUSH_UNTIL = 300;
    /** An attacker older than RUSH_LIFE rounds that has not seen the enemy king in the last RUSH_LIFE rounds turns
     *  gatherer (0 = off). s9s's field games: on maps where the rush cannot reach the enemy king its attackers walk
     *  all game (uneruesansfin vs AlexT101: 60% of our baby turns to r400 were attack moves, 60 cheese picked in 400
     *  rounds, war opened by them at r472; where_are_you 41%, rose 44-52%). */
    /** xf7 (explore line): 150. km24 (xf3 on clockwisdom vs agrab/atrap3): 12 attackers stayed 45-49 from the enemy king
     *  for 900 rounds of truce (7.5-8.3k attack moves, 12 pickups), while Xzibit, which beats every build there on the
     *  replica (25825/25826), out-picked us 75 to 27. t0g/u5v (150) were net -4 and net 0 on the old replica gate. */
    public static final int RUSH_LIFE = 150;
    /** From this round on (0 = never) every baby bites and grabs enemy rats during cooperation too: the field rushers
     *  open the war at r36-40 (abrk, whose attackers must first walk over, at r66). */
    public static final int BREAK_ROUND = 30;
    /** Gatherers spread over every known mine, not only those on our half: the field rushers pick 600-700 cheese a game
     *  on our half in rounds 100-400 (abrk 156). */
    /** Squared distance (to the cat's 2x2 centre, doubled coords /4) a gatherer keeps from cats. */
    public static final int GATHER_CAT_DIST = 20;
    /** A cat within dist2 CAT_DEFEND_R2 of one of our kings draws every rat that sees it, gatherers included
     *  (0 = off): attack_on_cat, a lone cat scratched our king to death in 113 rounds while 17 gatherers kept away. */
    public static final int CAT_DEFEND_R2 = 36;
    /** A rat on a cat branch (CAT_DEFEND, a FIGHT rat's seen or known cat) whose walk to the cat has stalled CAT_REACH
     *  turns (Nav.stall), or whose way there is a known detour (Flow.detour), leaves cats alone for CAT_REACH_TTL
     *  rounds (0 = off). fullofsecrets 6134 B: a cat at 410 hp pounced over the wall row above our king at r110 and sat
     *  beside our nearest mine to r2000; 10908 of our 38592 rat turns went to the cat branch (5169 stalled under the
     *  wall) and the jam in the king's pocket stalled 3909 delivery turns; we delivered 3865 to their 9575. */
    public static final int CAT_REACH = 0, CAT_REACH_TTL = 100;
    /** digToward before flow: one-tile dirt bands (cheesefarm's diagonal stripes) are dug through at once. */
    public static final boolean DIG_FIRST = true;
    /** xj1 (x-line): DIG_FIRST also runs while a reverse flow search builds (the rat walks on greedy/bug meanwhile), not
     *  only with no search. averystrangespace B vs Icarus (26562, xi1): both cats in the dirt-ringed centre all game,
     *  cat damage 96 to our 0, lost on points. Local seed 452 (km73, xi1 vs agrab): searches toward the centre or the
     *  far mine (10,7) ran 40-60 turns and many never finished (fl=3:0), so the rats bug-walked the east bands
     *  r750-2000 without digging; the first reached the ring at r1950. One Icarus rat dug straight from (4,24) to
     *  (30,44) in r125-196 (a dig every 2.5 rounds). */
    public static final boolean DIG_REV = true;
    /** Dig-aware flow-field navigation, requested after FLOW_STALL turns without progress. */
    public static final boolean FLOW = true;
    public static final int FLOW_STALL = 4, FLOW_TTL = 200, FLOW_RESERVE = 2500, FLOW_HOLD = 3, FLOW_REJOIN = 40, FLOW_MAX_TURNS = 12, FLOW_RETRY = 30;
    /** A step along a flow path is progress: it resets Nav's stall, and a turn held still for a search does not add
     *  to it. Without it the stall counts straight-line distance only, so a detour that leads away from the target for
     *  13+ turns ends a cheese chase (25 a mine errand, 20 a raid or explore) on the way. seepastyourgreed (s4d vs araid):
     *  gatherers in the bottom corridor see the mines through the wall above; each errand held 3-11 turns for its
     *  search, walked part of the 45-tile detour, gave up, and the next errand searched again (1840 pickups to 22240). */
    public static final boolean FLOW_PROGRESS = true;
    /** Keep the last 4 completed flow paths: a request toward one's target (or its start) from beside it walks the rest at
     *  once instead of searching. where_are_you: 16% of rat-turns stood still for searches (6 turns each), 57% of them
     *  toward a target the rat had found a path to before; paths averaged 3.5 steps before they were dropped. */
    public static final boolean FLOW_CACHE = false;
    public static final int FLOW_CACHE_TTL = 600;
    /** Flow search buckets as stacks: among tiles of equal f (g + Chebyshev) the deepest is expanded first. Chebyshev is
     *  exact on open ground, so FIFO ties fan out over every tile of the optimal cost; the path cost is the same. */
    public static final boolean FLOW_LIFO = false;
    /** Reverse flow search (target to rat): the rat walks on (greedy/bug) while it runs instead of standing still, and the
     *  path starts wherever the rat is when a tile beside it is reached. Hike: 35% of rat-turns stood still in
     *  searches of ~10 turns (621 bytecodes a node, ~15 nodes a turn). Searches give up after FLOW_REV_TURNS. */
    public static final boolean FLOW_REV = true;
    public static final int FLOW_REV_TURNS = 60;
    /** FLOW_REV's search with the neighbour loop unrolled (621 bytecodes a node measured for the loop). */
    public static final boolean FLOW_FAST = true;
    public static final int BFLOW_EXTRA = 12;
    /** Path cost of a dirt tile (a dig is 5 cheese and 2.5 turns of action cooldown, plus the step). */
    public static final int DIG_COST = 4;
    /** Random samples per explore target, taking the first never-seen tile. */
    public static final int EXPLORE_TRIES = 12, EXPLORE_CENTER_FROM = 0;
    /** How a scout or seeker picks its next never-seen tile of our half from the EXPLORE_TRIES samples: 0 the nearest
     *  to it, 1 the first (uniform over what is unseen), 2 the farthest. Nearest crawls outward from the king, and each
     *  new scout crawls the same ground again (where_are_you: 5 of our half's 8 mines never seen in 1400 rounds). */
    public static final int HOME_PICK = 1;
    /** xe6 (x-line): a rat that knows a sharper symmetry mask than the shared one, with no enemy in view, walks to the
     *  nearest king within SYM_COURIER (Chebyshev; 0 = off) and report() squeaks the mask once in earshot. Local
     *  probes km1-km3 (xe2-xe4 vs agrab, 40 games): a rat of ours knew the symmetry 99-1986 rounds before our king
     *  (median ~450), and the king never learned it on knifefight B (rat r19), krotus B (r15), whatsthecatdoin B
     *  (r42) or clockwisdom A (r145: attackers on the far corridor; the king kept ROT|HOR, so the enemy mine (2,25)
     *  that it listed never mirrored to our (56,33)). */
    public static final int SYM_COURIER = 30;
    public static final int SYM_GOSSIP_EVERY = 3;
    /** Scouts: one in SCOUT_EVERY gatherers explores our half (never-seen tiles) until round SCOUT_UNTIL, then
     *  gathers (0 = off). */
    public static final int SCOUT_EVERY = 4, SCOUT_UNTIL = 150;
    /** xe5 (x-line): scouts fan out (0 = off). A scout's frontier search ends only at a never-seen tile of our half in
     *  its sector of home's fan: within SCOUT_FAN degrees of the bearing to the enemy start, left of that or right of
     *  it. The slot picks the first sector; a search that finds nothing moves the scout on to the next. Replaces
     *  SCOUT_FRONTIER / FRONTIER_MINE for scouts, and exploreHome's samples keep to the sector too. clockwisdom A
     *  (xe4 vs agrab s1): both scouts walked the bottom corridor and the spiral; no rat came within 4 of our mines
     *  (56,27) and (56,33), 18-24 tiles up the corridor north of the king, until r656, and the king listed one mine
     *  (theirs) all game. Replica dumps: we picked 23.5 by r300 to the field's 41 there (win 0.17). */
    public static final int SCOUT_FAN = 35;
    // xe7: the kings squeak each newborn's place in its role's spawn order (SQ_START, as MINE_RR); gatherers 2, 6, 10, ... (xf1: 2 and 3, then 8, 12, ...)
    // scout, taking one flank, the other flank and then the sector ahead. xe5/xe6 on clockwisdom A: both
    // slot-hashed scouts drew the bottom (the spiral's gate and the west corridor), and the north mines were first
    // seen at r595.
    /** xa6: extra Flow reserve on a newborn's first turn while SCOUT_FRONTIER is on (RobotPlayer). */
    public static final int FRONTIER_PAD = 1500;
    public static final int PIPE_AHEAD = 4;
    /** xg3: a robot holding the walk's next tile past FLOW_HOLD turns (no free step beside it) keeps the next scout
     *  searches off that tile for PIPE_HELD rounds (0: off; searches ignore robots and found the same path again). */
    public static final int PIPE_HELD = 20;
    /** xg4: a scout held PIPE_WAIT turns at its walk's end for a search drops it and walks to exploreHome's sampled
     *  never-seen tiles of its sector for PIPE_REST rounds (0: off). With the sector's near part seen a search expands
     *  most of the map: xg3's whatsthecatdoin A scout held 56, 37, 24 and 20 turns (km34). */
    public static final int PIPE_WAIT = 5, PIPE_REST = 12;
    /** Loaded swap (0 = off; z7u's, unledgered on the x-line): a baby with SWAP_LOADED or more cheese within Chebyshev
     *  SWAP_HOME of our nearest king, blocked on its next step by an allied baby carrying under 10, carries that ally
     *  past (Nav.swapPast) and sets it down on the tile it left. rose vs YokoOno (block #386, 26049 g1 / 26050 g1):
     *  xf7's carriers stood 20+ turns with cheese for 2707 and 1810 rat-turns (xf3: 513 and 237; both seats lost, xf3
     *  won both). (10,2) is the one stem tile in transfer range of the king at (14,1-3); a rat that delivers there steps
     *  into the (9,0)-(9,2) dead end, and from r216 empties on (9,0)-(10,2) faced loaded carriers on (9,3)-(8,8).
     *  xi8: 10 (was 30). rose B vs araid (km69, xi7): from r180 to the end (r1047, transfer 180) the stem held
     *  empties on (49,2) and the dead end (50,0)-(50,2) against a carrier with 20 on (50,3) and 40s behind it; the 20
     *  carrier, the only one beside an empty, was under 30, so no swap fired (0 naps after r200). */
    public static final int SWAP_LOADED = 10, SWAP_HOME = 10;
    public static final int SCOUT_MINES = 4, SCOUT_MINES_UNTIL = 600;
    /** SCOUT_MINES scouting also stops once the team's known home mines have not grown for SCOUT_DRY rounds (counted
     *  from SCOUT_UNTIL; 0 = off). hb1, s9c (SCOUT_MINES 4) vs s7d: knifefight, cheesebottles and Stash hold 3 home
     *  mines each, so a quarter of s9c's gatherers scouted to r600 there; it delivered 935-1045 to 1679-1810 on
     *  knifefight and 3910 to 8404 on cheesebottles B. */
    public static final int SCOUT_DRY = 100;
    /** Scouts keep going until round W*H/SCOUT_AREA when that is later than SCOUT_UNTIL (0 = off): on where_are_you
     *  (60x60) the king knew 3 of our half's 8 mines until r850, and five mines 25-50 tiles out dropped 5900 cheese
     *  by r300 that nobody picked. */
    public static final int SCOUT_AREA = 0;
    /** The king turns to face each spawn tile before it builds: a newborn takes its king's facing, and the maps'
     *  starting king facings are not mirrored (knifefight), so the first rats looked along unmirrored lines. */
    public static final boolean SYM_FACE = false;
    /** A rat held SWAP_HOLD turns behind an ally baby on its next step carries the ally, steps into its tile and sets
     *  it down behind once it can act (a carried rat leaves the grid), so two rats pass in a 1-wide corridor (0 = off).
     *  rose: s6m's outbound and inbound gatherers met head-on at r160 in the 1-wide stem out of the king's walled
     *  pocket and held there to the end; the king starved at r1375 with 20 babies (spsquared 2/2, araid 2/2). */
    public static final int SWAP_HOLD = 0;
    /** With SWAP_HOLD: no ally is carried past twice within SWAP_PAIR rounds by the same rat (0 = off), which ends a
     *  back-and-forth between two rats after one exchange each. */
    public static final int SWAP_PAIR = 0;
    /** Mine seeking (0 = off): a gatherer at its mine that sees SEEK_CROWD or more allied babies nearer that mine
     *  explores never-seen tiles of our half for SEEK_TTL rounds, at most once per SEEK_RETRY rounds, and reports what
     *  it finds (streetsofnewyork: two of our three mines sat behind 5-deep dirt bands, unseen all game, while
     *  17846 rat-rounds crowded the third). */
    public static final int SEEK_CROWD = 6, SEEK_TTL = 150, SEEK_RETRY = 200;
    /** Idle seeking (0 = off): a gatherer that spends SEEK_IDLE turns within Chebyshev SEEK_IDLE_R of its mine without
     *  a pickup seeks as SEEK_CROWD does (that test counts allies in the 90-degree cone only). wallsofparadis (3800):
     *  70% of our rat-turns crowded one mine while our three other home mines, behind dirt rings, went unseen to
     *  r392-503; the field reached their twins by r54-135 and took 12920 from them to our 770. */
    public static final int SEEK_IDLE = 20, SEEK_IDLE_R = 4;
    public static final int DIG_MIN_CHEESE = 60;
    /** Live babies kept unconditionally (while CORE_FLOOR cheese remain) before the income test applies. */
    public static final int CORE_BABIES = 18, CORE_FLOOR = 300;
    /** On maps of CORE_SMALL_AREA tiles or fewer (0 = off) the core count is CORE_SMALL, not CORE_BABIES: past it
     *  every peacetime spawn passes the income test. Replica canyoudig vs Xzibit (8 z3x-line games, all lost at
     *  r288-339; in both z3x games the king starved): 19-20 babies at r50 to their 15-16, bank at r100 305-410 to
     *  800-895; z3x's spawns by r100 cost 1550-1870 at 40 each to their 1280-1360 at 32 (the price rises 10 a 4
     *  babies), deaths even (49-53 to 56-58 by r300); from r100 our bank sits under CORE_FLOOR + cost and the army
     *  falls to 0-2 by r300 while theirs holds 8-12. u5t (CORE_BABIES 12 on every map) went net -1 on the replica:
     *  of its 6 lost cells 5 were on maps of 900+ tiles (t_minus_five_minutes B the other); it gained tiny A. */
    public static final int CORE_SMALL = 12, CORE_SMALL_AREA = 441;
    /** Projected end-of-game cheese a spawn must leave. */
    public static final int SPAWN_MARGIN = 100;
    /** Fraction (percent) of measured income trusted when projecting forward. */
    public static final int INCOME_TRUST = 75;
    /** Rounds of income history in the rate estimate. */
    public static final int INCOME_WINDOW = 100;
    /** Projected surplus (cheese) a baby needs before placing a cat trap / biting with cheese. */
    public static final int TRAP_SURPLUS = 200, CHEESE_BITE_SURPLUS = 600;
    /** Cheese below which no trap is placed; cats within this squared distance of a king are trapped on the floor alone. */
    public static final int TRAP_FLOOR = 600, TRAP_DEFEND_DIST2 = 100;
    /** Optional per-turn work (trap-risk observation) needs this many bytecodes left. */
    public static final int BC_OPTIONAL = 8000;
    /** Below this many bytecodes left after sensing, end the turn rather than overrun. */
    public static final int BC_BAIL = 3500;
    /** The re-sense after moving (Baby.sense2) needs this many bytecodes left. IND_LEFT: the indicator string is
     *  built only with this many left (0 = always). Both are for a newborn's first turn on a big map: s9s overran
     *  by 3-11 there on superioristhmus (rz1, al1; field 3292, 3293), once at 17424 used before the indicator, once
     *  in the re-sense or flow work, and the next turn went to finishing it. Replica replays drop indicators. */
    public static final int SENSE2_LEFT = 6000, IND_LEFT = 1000;
    /** xe9: a king's indicator (two characters a listed mine) costs over 1000: wallsofparadis A r634 (km6, xe7) ran
     *  out inside it and lost r635. NB_LATE_LEFT: a newborn's first turn ends after its move below this. */
    public static final int IND_LEFT_K = 2500, NB_LATE_LEFT = 1500;
    public static final int GRAB_THREAT = 25, GRAB_BITE = 20, GRAB_MARGIN = 10, GRAB_BC = 12000;
    /** xf2 (owner #31: collegejunior and Merlin grab our deliverers, 18-27 a game right after a turn facing our king,
     *  and throw them onto it): with an enemy within Chebyshev 2, the grab micro also scores the end tiles and facings
     *  that can hand our cheese to the king (+GM_DELIVER, under one THREAT so it never buys a transfer with exposure);
     *  when none is worth taking, Baby.deliver keeps its facing (why 'G') while the bank holds DG_BANK. GM_LEFT: the micro's
     *  scoring stops with fewer bytecodes left (keepout overran past GRAB_BC). 0 turns the deliver part off. */
    public static final int GM_DELIVER = 20, DG_BANK = 150, GM_LEFT = 1500;
    /** xf3: a turn the grab micro scored without acting ends under GM_BAIL bytecodes left (else BC_BAIL):
     *  an attacker in a 40-robot crowd (km15 wallsofparadis, 94 overruns at OVS 809/999) spent ~8k in the micro and
     *  overran in the rest of the turn. */
    public static final int GM_BAIL = 6500;
    /** No formation site at war before this round (0 = off): the merge takes 7 babies from a war just begun (z0k's
     *  switch, here in a ledger hook). z1z's replica games at war still running at r250: 2 won, 12 lost with a second
     *  king formed by then; 51 won, 42 lost without (~/diag26b/z1zb*.jsonl). z0k (u9p, drifting bytecodes) pooled
     *  D 27, net +5, t 0.96 over three blocks. */
    public static final int FORM_WAR_FROM = 300;
    /** Endgame cat damage (0 = off): from round END_CAT at war, a baby of any role that holds less than DELIVER_AT raw,
     *  carries no rat, is not needed at a threatened king and has no enemy rat within dist2 8 goes for the nearest seen
     *  or known cat (Baby.endCat), and the bite step bites it. At war the r2000 points are 0.5 kings, 0.3 cat damage and
     *  0.2 transfer shares. z0c (u9p + END_CAT 1650) pooled D 19, net +5, t 1.15 over two replica blocks, cat share 0.658
     *  to 0.584, and was rejected only by the transfer floor (set 1: 0.86 of the twin per round, floor 0.9). From r1850
     *  the errand covers 7.5% of a full game, so transfer can fall at most about that much. */
    public static final int END_CAT = 1850;
    /** xh4 (x-line, owner #31): END_CAT during cooperation, from this round (0 = off). Cooperation points weigh cat
     *  damage 0.5 (war 0.3), kings 0.3, transfer 0.2, and the game ends when the last cat dies. Block #387's r2000
     *  points losses: clockwisdom A vs Xzibit (26157) cat damage 500 to 1150 with kings 2 to 1 and transfer 3855 to
     *  3400 (46 to 54); rose B vs YokoOno (26154) 400 to 1350 (39 to 60); FreeTheCheese B vs Merlin 8054 to 10292.
     *  A cat has 4000 health and a bite deals 10. */
    public static final int END_CAT_COOP = 1500;
    /** xi1 (x-line): END_CAT_COOP for a lone king's babies too, until this round (0 = off), and with no cat known they
     *  walk to the map centre (END_CAT_CENTRE). #389 (xh4) lost averystrangespace vs Icarus in both seats of both sets
     *  (26362/26363/26401/26402, base rate 0.94-0.96), all in cooperation to r2000: Icarus did 224-230 cat damage by
     *  r200 and we did 0 (0.5 of the points), with one king and 600-900 banked from r400, so endCatBar held every baby
     *  for END_KING and none had seen a cat (32 babies, none carrying, cheese piling on the map). xh1's win there
     *  (26346) found the cats at r700-1000 and did 5.9k. */
    public static final int END_CAT_COOP_FREE = 1750;
    /** xj2 (x-line): in cooperation a gatherer whose walk to its mine stalls past 25 turns (the pick is dropped) takes
     *  the END_CAT errand (endCat: the nearest seen or known cat, else the map centre) for this many rounds (0 = off).
     *  averystrangespace seed 452 (km73 xi1 vs agrab, B): r750-1500 nearly all 33 babies walked toward the enemy home
     *  mine (10,7) (MINE_BALANCE + IDLE_SHIFT once our one home mine idled them) at stalls 25-90, the pick dropped
     *  and re-made every turn; transfer 560 a 250 rounds, cat damage 0. With xj1's DIG_REV the r1500 errand reached
     *  the centre's cats and mines at once (46 cat traps r1600-1900, pickups 30 -> 88 a 250 rounds). */
    public static final int MINE_STALL_CAT = 100;
    /** xh6 (x-line): WAR_NO_CAT ends at this round. km55 keepout vs atrap3 A/B: xh5 kept 40% fewer cat deaths but
     *  lost both at r2000 on cat share (ours 5627 vs 8000 A, 6002 vs 8000 B) with cats still alive; xh4 won both 8000/8000. */
    public static final int WAR_NO_CAT_END = 1200;
    /** Rescue formation (run only as CASH_END's endgame rescue): a baby holding CASH_RAW+ raw cheese, Chebyshev CASH_FAR+ from every king, that sees CASH_N-1 more
     *  of our babies within Chebyshev CASH_R holding CASH_MIN raw with its own, none heavier, leads a formation on a
     *  clear 3x3 at or beside itself. It squeaks the site (SQ_CASH) and every baby that hears it within Chebyshev
     *  CASH_PULL walks into the 3x3 and relays it. The engine banks the raw cheese of every rat in the 3x3 when the
     *  centre promotes (becomeRatKing), so a stuck crowd's load reaches the bank without a walk home, and the new king
     *  stands where they were. A site lives CASH_TTL rounds from its start; then no rescue for CASH_TTL more.
     *  Replica starvation losses (435 dumped games): in 92 our last king starved, in 17 our babies held 300+ raw then.
     *  When the bank first fell under 150, a group of 7+ of ours within Chebyshev 5 held 320-935 raw in 11 games:
     *  fullofsecrets (outside the king's sealed pocket, 3-7 out), rose (the stem, 5-8 out), clockwisdom, uneasy_alliance.
     *  Local fullofsecrets (u9p vs araid A): attackers held 120-777 each around the centre mines from r400, 20 out. */
    public static final int CASH_FLOOR = 60, CASH_RAW = 60, CASH_N = 3, CASH_R = 4, CASH_MIN = 250,
        CASH_FAR = 5, CASH_PULL = 8, CASH_TTL = 50;
    /** Endgame rescue (0 = off): from this round a lone king's babies run CASH's formation for a second king, with no
     *  raw cheese needed, while the bank keeps the new king alive to round 2000 (Baby.cashEnd). At r2000 kings are half
     *  of the war points (0.3 in cooperation): 1 king to their 1 splits 25-25, 2 to 1 is 33-17. u6x's END_KING needs
     *  400 banked and the king to name a site; 9 of the 12 lone-king replica points losses never had 400. */
    public static final int CASH_END = 1880;
    /** Last stand (0 = off): with 3 or fewer live babies (the baby cost at its base 10) every spawn floor drops to
     *  LAST_FLOOR, so a king without income spends its upkeep reserve on rats instead of starving on it. Replica dumps
     *  (lowstand.py, 27 games with 10+ rounds of <= 2 babies and >= 20 banked): lost 25; the bank then was 22-104, the
     *  LOW_FLOOR 100 plus a spawn's cost. averyfineline vs Icarus (18352, A): 2 rats and 100 banked at r120, 0 rats
     *  from r140 with 60, no spawn after r111, the king starved at r257. u6u (LOW_FLOOR 30 under 8 babies) went D 3
     *  (+2 -1) at screen zc3 and was dropped on its starved-king count (6 to 3). */
    public static final int LAST_FLOOR = 20;
    /** cashTick stops reading squeaks once fewer than this many bytecodes are left (1 = never: the read alone), z7u's
     *  line. A crowd on a rescue site squeaks it every turn, and z3x read them all: zc4 (z3x + RAW_TRAPS) overran 4 times
     *  on zd312, and on ov4 the overrun is z3x's own cashTick in the r1880 CASH_END recall crowd (rat 13481, r1884-1891,
     *  stage 6 to 7 costing 11k; no trap laid that turn). z7m's rats overran the same way on arrows (z7u's evidence). */
    public static final int CASH_READ_LEFT = 6000;

    /** A newborn's first turn bails under NB_BAIL (not BC_BAIL): it already paid Sym.init and its allocations. */
    public static final int NB_BAIL = 6500;
    /** A newborn's first turn defers pickMine (Baby.moveDecision) unless NB_PICK_PER bytecodes a known mine (its
     *  dedupe, home-half test and sort: 4.1k with 14 mines) plus NB_PICK_PAD (the Nav.goTo after it, ~1k) are left;
     *  the NB_BAIL gate let ~170 newborns a game on 60x60 maps overrun there and lose their second turn too. */
    public static final int NB_PICK_PER = 320, NB_PICK_PAD = 1600;
}
