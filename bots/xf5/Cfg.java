package xf5;

/** Behaviour switches and constants. Every experiment adds a switch whose default is the incumbent. */
public final class Cfg {
    public static final int MAX_BABIES = 18, MAX_BABIES_WAR = 18;
    /** With RICH_BANK global cheese or more the baby cap rises to RICH_BABIES (0 = off): a surplus buys bodies. */
    public static final int RICH_BABIES = 40, RICH_BANK = 1000;
    /** The RICH tiers need a map of RICH_MIN_AREA tiles or more (0 = off). u2g's in-band small-map field games (W*H
     *  <= 900, 22 games, 5 won; 23 of 40 won on 901-2500): at r50 we field 23 babies to their 17.5 (the 40 cap while
     *  the bank holds 1000), by r150 the bank is 386 to their 1069 and we have picked 799 to their 1153; at r300 7.4
     *  babies to their 15.9, and the king starves (294 HP a lost game). */
    public static final int RICH_MIN_AREA = 0;
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
    /** The spawn floors, the rest-of-game upkeep reserve and the rat-trap minimums scale with our live kings (each
     *  king eats 2 a round): s5r on cheesebottles (1800) formed a second king, spawned to its 30-baby cap down to
     *  the one-king floor, and both kings starved from r650. */
    public static final boolean FLOOR_PER_KING = false;
    /** King places rat traps toward enemy rats within this squared distance once cooperation is over. */
    public static final boolean KING_TRAPS = false;
    public static final int KING_TRAP_DIST2 = 20;
    /** Babies at war grab adjacent enemies and throw them; lay rat traps toward enemies within BABY_TRAP_DIST2. */
    public static final boolean NAP = true, BABY_RAT_TRAPS = true;   // xc2 (owner #31: the field's trap war), zdy's set
    /** Bite first the adjacent enemy rat that could grab us (healthier by at most one bite): it then cannot. */
    public static final boolean BITE_GUARD = false;
    /** Bite first an adjacent enemy rat that carries one of ours (on knifefight bestsofar threw 41 of our rats, most
     *  into our king). */
    public static final boolean BITE_CARRIER = false;
    public static final int BABY_TRAP_DIST2 = 13;
    /** Baby rat traps go on a front tile next to the nearest enemy rat (Chebyshev 1, inside the trap's 3x3, so its next
     *  step there fires it), adjacent enemies included. abcdezyxwv (3292 Meow): 36 of 50 traps laid next to one of our
     *  rats, 39 fired within a round, 98% hit; our rule skipped adjacent enemies and laid toward farther ones. */
    public static final boolean TRAP_ADJ = true;
    /** Cheese (global, or raw+global for babies) below which no rat trap is placed. */
    /** xc2 (zdy's set: BABY_RAT_TRAPS + TRAP_ADJ + TRAP_CAP 25 + TRAP_SPAWN_FIRST + this 900): replica 25315
     *  wallsofparadis A vs Finals (xb8), r0-600: Finals laid 111 rat traps and our rats set off 107 (50 damage each),
     *  we laid none, and it picked 10,600 to our 5,568. The 900 floor keeps u8f's 300-floor starvation off. */
    public static final int RAT_TRAP_MIN_CHEESE = 900;
    /** Opening trap floor (off at 0): before round TRAP_OPEN_TO no baby rat trap is laid with less than
     *  TRAP_OPEN_FLOOR banked, so the opening bank buys bodies first (u0y vs outercloud, 4178: 31 traps by r100 on
     *  Excavation and 40 on dirtfulcat took the bank from 1810 to 515 and 1660 to 280; spawning then stalled and both
     *  games were lost, where u0m won them). */
    public static final int TRAP_OPEN_TO = 0, TRAP_OPEN_FLOOR = 1500;
    /** Most live rat traps we keep (engine max 25): new ones are bought only as old ones fire. */
    public static final int TRAP_CAP = 25;
    /** Trapped mines: a gatherer at its mine lays rat traps on the mine's MINE_TRAPS trap sites nearest the enemy
     *  start (offsets 0 or +-3 in each axis: eight 3x3 trigger blocks tiling the 9x9 spawn square). Our traps never
     *  fire on us, so they only hit raiders (the field rushers pick 20-90 piles on our half by r300). Only after
     *  cooperation (a fired trap is a backstab), with MINE_TRAP_CHEESE banked, while we have fewer than MINE_TRAP_CAP
     *  live traps; the combat cap rises by MINE_TRAP_CAP so these don't crowd out traps in fights. */
    public static final int MINE_TRAPS = 0, MINE_TRAP_CAP = 10, MINE_TRAP_CHEESE = 400;
    /** A raider at an enemy-half mine lays traps on its RAID_TRAPS sites nearest the enemy start, the way MINE_TRAPS
     *  does at ours, under the same caps (the field rushers pick nearly every pile on their half; we pick 0-15). */
    public static final int RAID_TRAPS = 0;
    /** No rat trap (baby or king) while the king, under the baby cap, lacks the cheese for its next spawn plus
     *  SPAWN_FLOOR and the trap: bodies before traps (1204 constrictinglattice vs AlexT101: r100-150 we laid 7 traps
     *  while the king sat under the spawn floor, and the army fell 12 -> 8). */
    public static final boolean TRAP_SPAWN_FIRST = true;
    /** Keep enemies within two tiles inside our cone (anti-ratnap) instead of moving. */
    public static final boolean FACE_ENEMY = true;
    /** xb2 (explore line): an enemy rat behind a wall (Baby.walled) is no war target, prey or face target. Stash B
     *  (25070, starved r535; 0 of 34 replica draws for zdb's line): 1-3 enemy rats parked past the wall row held
     *  16-17 of ours in standoffs inside the home room from r250 (zj3 vs atrap3 seed 155, 57 pickups to 304). */
    public static final boolean WALL_BLIND = true;
    /** xb8: WALL_BLIND traces at most this many lines a turn (0 = no cap). xb4's screen zd619 had 38 overruns, 37 on Stash
     *  seat B: an attacker stuck at the pocket wall traced every enemy behind it each turn (nearest-enemy loop and prey(),
     *  ~300 bytecodes a trace) and crossed 17,500 at tryCarry or tryDeliver (OVS 2122/809) about every other turn. */
    public static final int WALL_BLIND_CAP = 6;   // xd6: was 8. xd2's s6 screen zd636 overran 3 times on Stash A vs
    // araid (17,586) and xd5's rerun at r459 (gatherer at war, OVS 2208), both with the mark 3-30 segment at ~5,200
    /** Rushers (role ATTACK from birth) walk by BitFlow's distance fields; everyone else keeps Flow (zis, x-line port
     *  without the ledger). Stash seat B vs atrap3 (xb2 seed 156): our 7 rushers stood at (12-16, 3-6) under the pocket
     *  wall at r200, each Flow search toward the enemy-king spot taking 35-57 turns and rebuilt 5 times. zis: Stash B
     *  exits by r100 21 vs zdb's 11; its replica block won YokoOno Stash A (rate 0.35) and dropped at net -1 on
     *  chaotic cells. */
    public static final boolean RUSH_BFLOW = true;
    /** RUSH_BFLOW only on maps of at least this many tiles (zir lost the tiny rush races with it). */
    public static final int RUSH_BFLOW_AREA = 600;
    /** xc3: a rusher whose Nav stall reaches BF_GIVEUP leaves BitFlow for Flow (0 = never). averystrangespace (start
     *  corner walled by dirt) vs agrab seeds 163-166: xb3 broke out 1 of 3 (rushers at nav stall 100-478 in dirt
     *  pockets), xb2 3 of 3, xb8 1 of 6, xb8 without RUSH_BFLOW 3 of 3; replica 25325/25326 (xb8) lost both seats vs
     *  Icarus, which zj3 and xa5 won 4 of 4. Flow's rushers dig through. */
    public static final int BF_GIVEUP = 60;
    /** xc7: while a rusher's BitFlow field regrows after a restart (its downhill step ran into wall it had not seen when
     *  the waves passed), the rusher walks on in the heading of its last field step, up to 90 degrees off it, instead of
     *  greedy toward the target (Nav.momentum). Stash B vs araid (xc6 probe xbfdbg seed 180): its 12 rushers left
     *  our walled pocket at r86-205; each restarted 2-5 times on fresh stretches of the y=7 wall, spent 43-108 turns
     *  off-field waiting for a rebuild (~3 waves a turn, ~10 turns a field) and walked greedy back to the target's
     *  column meanwhile, undoing the walk along the wall (11966: east to (18,6), back west to (14,5); west to (9,6),
     *  back east to (13,4)). Replica Stash B vs YokoOno: 20 of ours in the pocket to r75, theirs out by r50. */
    public static final boolean BF_MOMENTUM = true;
    /** BitFlow.follow holds the rat still when fewer bytecodes than this are left (its 8-neighbour scan is unbudgeted). */
    public static final int BF_FOLLOW_LEFT = 3000;
    /** The king's alert counts no enemy baby behind a wall from it (Baby.behindWall), and is not raised when every
     *  enemy it sees is one (zgc/zik, x-line port without the ledger). Stash seat A vs atrap3 (xb3 seed 157, r200):
     *  4 of their attackers stood at y 21 outside our pocket wall and 19 of our babies held in the pocket answering the
     *  king's alert ('w' toward it). zgc and zik each won YokoOno Stash A on the replica (rate 0.38). */
    public static final boolean KING_WALL_THREAT = true;
    /** faceEnemy passes over an enemy at Chebyshev 2 when no tile beside both of us is open (known wall or dirt):
     *  it cannot get next to us in one move, so facing it buys nothing. u3z's 96 field games: 9460 of 13612 'f'
     *  turns in wins and 4090 of 9073 in losses faced such an enemy (RUN 5918 1337, rose 5905 678, fullofsecrets 5923
     *  554); in dh1's pipes game two 'f' rats facing enemies through the pipe wall held carriers 100-150 rounds. */
    public static final boolean FACE_REACH = false;
    /** Scored combat movement (Micro) for babies at war. */
    /** War role split: 0 everyone fights, 1 gatherers ignore non-adjacent enemies, 2 and never freeze to face. */
    public static final int WAR_GATHER = 2;
    /** The post-move throw scan runs only with this many bytecodes left (it costs up to ~6000). */
    public static final int THROW_BC = 7000;
    /** Idle non-raiding fighters hold one of our mines instead of exploring. */
    public static final boolean FIGHT_MINE = true;
    /** At war a FIGHT rat takes its mine from the contested home-half mines, those whose Chebyshev steps from the
     *  mirrored enemy start exceed those from home by at most FIGHT_POST (0 = off; none known: every home mine). In
     *  u4e's 48 field losses the enemy stood at such mines 11304 mine-rounds (r150-799) with none of ours within 4 in
     *  66% of them, and took 441 cheese a mine at margin 3-8 (wins: 3708 rounds, 46%, 63 a mine); fighters, who engage
     *  enemies within d2 20 at war, spread over every home mine by slot like gatherers. */
    public static final int FIGHT_POST = 0;
    /** Micro score bonus for a step onto cheese. */
    public static final int MICRO_CHEESE = 15;
    /** The king walks onto the nearest home-half mine within dist2 KING_MINE_R2 of its start. */
    public static final boolean KING_MINE = true;
    /** The king never steps to where its 3x3 plugs a passage (the 16 tiles around it split into two or more open
     *  arcs, walls, dirt and the map edge closed), unless where it stands is as bad, and steps out of one when it can.
     *  rose vs spsquared (1796): the king walked toward its home mine into the pocket's only exit at r~60 and sat
     *  there to r2000 with all 20 babies shut in behind it (75% of our baby turns went to mine trips that never got out). */
    public static final boolean KING_PLUG = true;
    /** KING_PLUG's ring test at a fraction of the bytecodes, same answers: the 16 ring tiles read unrolled (ring on the
     *  map) with the corner rule and the arc count in bit operations, the king's own tile counted once a move, and the
     *  test skipped (no unplug step, every step allowed) under PLUG_LEFT bytecodes left. u3z's field games: 1296 king
     *  overruns in 31 matches, 1279 of them on mercifullattice (both seats, from r22) and learn_your_lesson (seat B), all
     *  four lost; locally (ko1/ko2, u3v vs araid) the king overran every other turn there, in move(): unplug() counts
     *  the ring of its tile and all 8 neighbours (~1150 bytecodes each) whenever its own ring is split, then closer()
     *  and step() count two more per candidate step. */
    public static final boolean PLUG_FAST = true;
    public static final int PLUG_LEFT = 3000;
    public static final int KING_MINE_R2 = 400;
    /** King path (KPath): the king walks to the home mine nearest by a path for its 3x3 body (seen walls block,
     *  unseen tiles and dirt are open), searched from every seat at once with spare bytecode (KP_RESERVE kept) and
     *  searched again when a mine is listed or a seen wall cuts the way; it holds still while a search runs. Paths
     *  longer than KP_MAX king steps are not taken; a search running KP_TURNS rounds gives up, and a failed one is
     *  retried after KP_RETRY rounds or a new mine; with no path, King.homeMine's greedy walk goes on as before.
     *  King.homeMine was the first listed mine within dist2 400, walked to greedily: our king stopped under a wall
     *  row on rift (4342, 13 from (6,12) while (12,26) lay 8 away) and stayed 26 from any mine on wallsofparadis
     *  (4344-4345: deliveries 1490 to AdamTan12's 7281); on 74 maps a 3x3 path reaches a home mine the greedy walk
     *  misses on 11 (TheHeist, EscapeTheNight, RUN, clouds, knifefight, krotus, ...) and 4 more have none within 400. */
    /** xe3 (explore line): on. u1y lost its replica block narrowly (D 4, net -2) but its mechanism held (kpa: king
     *  pickups 21,980 to 3,440, 22-10 head to head). xe2's attack_on_cat B king (kf3 vs atrap3) sat at (44,7) in the
     *  pillar cage for 480 rounds (km=z: greedy walk blocked short of home mine (40,13)), and a cat later pinned it
     *  there. */
    public static final boolean KING_PATH = true;
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
    /** Moat (Moat.java): at war a king that sees an enemy rat within dist2 MOAT_D2 fills its Chebyshev-2 ring with
     *  dirt (one tile per action, helped by babies beside the ring while its alert is under MOAT_IDLE rounds old),
     *  keeping MOAT_PORT spawn tiles open, and holds still until MOAT_IDLE rounds pass without a sighting; no baby
     *  digs that ring at war. Bites reach dist2 2 and a thrown rat stops at dirt. r1v vs AlexT101/abcdezyxwv (12
     *  games, 11 king kills at r49-714): throws into our king did 84-480 a game (42-46 each, point blank, our own rats
     *  thrown in 9 of 10), bites 30-316; an enemy rat reached Chebyshev 3 of our king at r7-145 with 3-138 dirt in
     *  our stock (11-16 open ring tiles). */
    public static final boolean MOAT = false;
    public static final int MOAT_D2 = 25, MOAT_IDLE = 60, MOAT_PORT = 1;
    /** MOAT: while the alert is fresh and the team holds fewer than MOAT_DIG dirt (0 = off), a baby within Chebyshev
     *  MOAT_DIG_R of the king with nothing to fill digs a dirt tile next to it off the ring (5 cheese; not under
     *  MOAT_DIG_CHEESE). mo2's first tiny game: 13 ring tiles placed, but the stock sat at 0-4 and the ring never closed. */
    public static final int MOAT_DIG = 0, MOAT_DIG_R = 5, MOAT_DIG_CHEESE = 100;
    /** A king bound to starve before round 2000 (Econ.surplus() < 0), with no mine of its own (none within KING_MINE_R2,
     *  or another king's), walks to the nearest home-half mine no other king holds, at any distance; checked from
     *  FORAGE_FROM every FORAGE_EVERY rounds. 27 of 78 king deaths in 50 replica matches (3284-3297, 3728-3763) were
     *  starvation, 16 (10 games) with no enemy damage: where_are_you (3763 B: the second king formed at (56,56) beside the first,
     *  no mine within reach; 25k cheese lay on the map at r1000, 4 of 8 home mines never visited; both kings starved
     *  r1125), Hike, uneruesansfin (the king's start has no home mine within dist2 400). */
    public static final boolean KING_FORAGE = false;
    public static final int FORAGE_FROM = 200, FORAGE_EVERY = 50;
    /** Dirt on the king's walk to its mine: the king digs the step's leading edge (5 cheese a tile). */
    public static final boolean KING_DIG = true;
    /** King steps out of a near cat's path (King.dodge) instead of only straight away from it. */
    /** xe1 (explore line): on. An exploring cat walks a BFS path to its waypoint and scratches whatever blocks its
     *  next step (engine InternalRobot cat EXPLORE). xd9's attack_on_cat A king (kf1, vs agrab) fled 20 tiles south
     *  along the cat's x = 1 patrol and was scratched dead; dodge() with CAT_RISK_STEPS scores every step against the
     *  cat's path. */
    public static final boolean KING_DODGE = true;
    /** Footprints ahead of a cat that King.catRisk scores (2 = the old 250/75 pair). */
    public static final int CAT_RISK_STEPS = 6;
    /** xd9 (explore line): a king fleeing a cat also tries the two 90-degree steps (G.fan5) when away and its two
     *  45-degree neighbours are blocked. With the 3-way fan a king on the map edge (x = 1) with a cat on its inner
     *  side has no legal step: xc6's t_minus_five_minutes A king (25588) sat at (1,7) beside a cat r266-r403 and died
     *  to scratches while the south step was open. */
    public static final boolean KING_FLEE5 = true;
    /** The king steps off the flights of enemy babies within Chebyshev THROW_DODGE that hold one of ours (King.throwDodge;
     *  0 = off, negative = run but never act). */
    public static final int THROW_DODGE = 9;
    /** Cats within this dist2 of the king's centre make it dodge. */
    public static final int KING_DODGE_R2 = 25;
    /** A dodging king boxed in by dirt digs out (KING_DIG_MIN cheese and up). */
    public static final boolean KING_DODGE_DIG = true;
    /** xe9: the boxed-in dig scores 8 more steps; skipped below this many bytecodes left. */
    public static final int DODGE_DIG_LEFT = 5000;
    /** The king bites a cat in reach when its action is still free after spawning (plain bites, no cheese). Kings
     *  never bit cats: groveisbeingdesecrated (gr1, every side-A game): the band's cat pinned our king in the corner
     *  from r120 and scratched it dead at r190 with 90 HP of its 4000 left after our babies' bites. */
    public static final boolean KING_CAT_BITE = false;
    /** Cats walk a fixed waypoint loop and scratch whatever blocks it: the king marks every cat footprint it sees or
     *  hears squeaked as lane, its walks and digs never add lane to its 3x3, and it steps off lane when on it.
     *  bed (3091, 2086-2087): the king dug out of its start pocket into cat 3's lane at (1..2,11) and was scratched
     *  to death by r245; the field king stayed home. */
    public static final boolean CAT_LANE = true;   // xe2 (explore line): on (s9t-s9y cut king scratch damage 900 -> 760
                                                   // and 3158 -> 2308, neutral on wins)
    /** xe2: King.catRisk adds this per lane tile under the king's 3x3 (CAT_LANE). xe1's attack_on_cat A king (kf2 vs
     *  agrab) dodged from (2,42) into the 3-wide x = 0..2 corridor the cat patrols and was chased 20 tiles south. */
    public static final int LANE_RISK = 40;
    /** With a cat near our king, empty rats on the ring around its 3x3 step off it: the king needs room to dodge. */
    public static final boolean YIELD_KING = false;
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
    /** A second king: the first king names a home-half mine as the site once it has FORM_BABIES babies and
     *  FORM_CHEESE global cheese after round FORM_FROM; babies within dist2 FORM_PULL_R2 gather in its 3x3. */
    public static final boolean FORM_KING = true;
    /** xb1 (explore line, owner #31): Merlin holds one king to ~r1900 and forms its second for the end (FreeTheCheese
     *  25080: 306 babies spawned to our 175, cat damage 10.9k to 6.8k, transfer 22.6k to 13.7k; our second king from
     *  ~r300 paid ~3,400 upkeep). FORM_FROM 1200 (from 80): no mid-game king; END_KING and CASH_END still form one.
     *  xc6 restores 80: xc3's replica block (#380) vs zj3's (#378). rift vs spsquared, both seats: on one king to
     *  r1850 they formed a third at r464/r619 and won B on points 3 kings to 2 (zj3's second at r103/r106 held them at
     *  two, W +27/+33). FreeTheCheese A vs collegejunior: zj3's second at r332 won 2 kings to 1 (+41); xc3 went 2-2
     *  (their second r1479) and lost by 9. FreeTheCheese B vs Merlin, xb1's target, lost both ways (1-2, 2-2). */
    public static final int FORM_FROM = 80, FORM_BABIES = 16, FORM_CHEESE = 300, FORM_TTL = 60, FORM_RETRY = 150, FORM_PULL_R2 = 72;
    /** Kings we form up to (engine max 5 before round 1200). */
    public static final int FORM_MAX = 2;
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
    public static final int FORM_NET100 = -100;
    /** Formation v2. FORM_SURPLUS: form while Econ's projected round-2000 bank with one more king's upkeep stays at
     *  least this (Integer.MIN_VALUE = FORM_NET100's income test instead; that test ignores the bank, and s4d's long
     *  replica games sat on 2-9k cheese without a site). FORM_RING: with no home mine for a site, name a clear 3x3
     *  just off the king's body (all of it in the king's view; the mine pick needs tiles the king has seen, and its
     *  four-behind fallback is often a wall). FORM_STILL: the first king does not walk while a site is open (on
     *  cheesebottles it chased piles onto its own site, 1300 rat-turns of failed formation in a game). FORM_GAP: a
     *  formed king lets the next site open this many rounds later (0 = the FORM_TTL + FORM_RETRY wait). FORM_LATE: a
     *  lone king may form a second after round 1150 (the engine's round-1200 cutoff binds only at 2 kings; kings alive
     *  at round 2000 weigh 0.5 of war points). */
    public static final int FORM_SURPLUS = Integer.MIN_VALUE;
    public static final boolean FORM_RING = false, FORM_STILL = false, FORM_LATE = false;
    public static final int FORM_GAP = 0;
    /** A baby outside the site's 3x3 joins only while it sees fewer than FORM_JOIN allied babies nearer the site;
     *  the rest keep working (0 = every baby within FORM_PULL_R2 joins). */
    public static final int FORM_JOIN = 8;
    /** Formation v3: the site is a home-half mine itself, though the king has not seen it. It is the listed mine
     *  nearest the king, more than 4 from it, that no king holds (none within Chebyshev 4), that has not failed as a
     *  site, and with FORM_MINE_SAFE * dist2 to our start at most its dist2 to the enemy start (4 = pickFormSite's
     *  "clearly ours"); known dirt in its 3x3 ranks it after the clean ones. No mine's 3x3 holds a wall or leaves the
     *  map (415 mines on 53 maps; 59 hold dirt). The field forms its extra kings on mines across its half and ours
     *  (abcdezyxwv on superioristhmus: four, r330-563, 0-1 from a mine; spsquared three), ours form 4-10 from any
     *  mine beside the first king, as the old site needs tiles the king has seen. To r400 in s9s's field games our
     *  mines ranked 4th+ from the king hold 1-2 of our babies and lose 49-59% of their cheese unpicked. Babies within
     *  FORM_MINE_PULL_R2 of a mine site join (beyond FORM_PULL_R2) when (slot >>> 3) % FORM_MINE_SHARE is 0, attackers
     *  never. A mine site skips the income test (FORM_NET100): on where_are_you all four home mines are listed by
     *  r400, 19-30 tiles out, income stays 100-330 a hundred rounds and 2000 cheese lies at each by r1000. A formed
     *  king squeaks its old home king's tile with each spawn, and its babies use that for the half test and the
     *  mirrored enemy start. */
    public static final boolean FORM_MINE = false;
    public static final int FORM_MINE_SAFE = 2, FORM_MINE_PULL_R2 = 400, FORM_MINE_SHARE = 2;
    /** An endgame king: from round END_KING a lone king names a site for a second (a clear 3x3 off its body, else
     *  pickFormSite's) whenever 7 babies and END_KING_CHEESE global cheese are there, with no income test and no
     *  retry wait past FORM_TTL (0 = off). Kings alive at the end weigh 0.5 of the points at war, 0.3 in cooperation,
     *  so a second king at 1-1 moves 8-16 points to us; s9s's set-4 limit games (9 of 42) all ended at 1-1 or less
     *  for us, and averystrangespace (3287) was lost 24-25 with no cat damage either side. */
    public static final int END_KING = 1850, END_KING_CHEESE = 400;
    /** The endgame site is the clear 3x3 off the king's body with the most of our babies near it, not the one
     *  farthest from the enemy: on ZeroDay (ek1) u0d's site lay behind the king at the map edge and 20 babies stalled
     *  around its body from r1850 to the end. */
    public static final boolean END_KING_CROWD = false;
    /** END_KING_CROWD names its site only with this many bytecodes left (the king waits a turn otherwise). */
    public static final int END_KING_LEFT = 9000;
    /** END_KING names the formation's usual site first (the nearest clearly-ours mine more than 4 from the king, clear
     *  and kingless), the ring beside the king only without one. ZeroDay (ek1, u0d side B): the ring site (34,2) lay
     *  between the king's body and the map edge; 25 babies queued for it from r1850 (stall up to 48) and none formed. */
    public static final boolean END_KING_MINE = false;
    /** END_KING names FORM_MINE's mine site first (mineSite: the babies' listed mines, no king sight needed; a mine
     *  whose formation failed is banned). u0i's END_KING_MINE was inert: pickFormSite's clear3 wants tiles the king
     *  has seen, and on ZeroDay B the king sat in its start pocket (x 32-39, y 0-6) with three home mines out of view,
     *  so the ring site (37,1) won again, its only entrance plugged by the king's own body. */
    public static final boolean END_KING_MSITE = false;
    /** Gatherers at their mine turn in place instead of wandering. */
    public static final boolean GATHER_STILL = false;
    public static final boolean RAID = true;   // idle fighters go to enemy-half mines
    public static final int RAID_STAY = 60, RAID_SPREAD = 60;
    /** Once cooperation is over, one in WAR_STRIKE fighters (by birth round, so mirrored rats agree) turns attacker
     *  and goes for the enemy king (0 = off). In s3y's panel games against the two early rushers the enemy king took
     *  no bites while ours died by about r325 (DefaultLarge vs abcdezyxwv: kills even, kd 1.26). */
    /** xa4 (explore line, owner #31: learn the tactic that beats us): collegejunior and Merlin keep 2-10 rats at our
     *  king from r300 and throw our own rats into it; zj3 has none at theirs after the opening (near.py, 24945
     *  cheesefarm, 24988 DefaultMedium). Half the fighters lay siege to the enemy king once at war. */
    /** xc5: off. The strikers cost cat defence and add no king damage. Over xb8's replica block (70 cells shared with
     *  zj3) xb8 dealt 219k cat damage to zj3's 259k and bit cats 13.3k times to 16.9k, and did no more damage to enemy
     *  kings (10.2k to 10.9k); the cat finished the enemy king 3 times to zj3's 11. On attack_on_cat vs atrap3 (seeds
     *  170-172, both seats) every game ends with a cat scratching a king to death: xc3 won 3 of 6 with 10.7k cat
     *  damage, xc3 without strikers 4 of 6 with 20.4k, zj3 5 of 6. In xc3's B 172 the 5-6 fighters that convert at
     *  war start die at the trapper's king by r250 (A 14 to 1), and when the cat reaches our king at r450 4 fighters
     *  are left within reach (zj3 has 14 within 6 tiles and kills it). Gating the strike on no cat seen for 100
     *  rounds (xc4) never acted: no cat is in sight at war start. */
    public static final int WAR_STRIKE = 0;
    /** xa5: war strikers attack in waves (Baby.waveHold): rally WAVE_FRAC percent of the way to the enemy king spot
     *  for the first WAVE_GATHER rounds of every WAVE_P, then charge together (0 = off). */
    public static final int WAVE_P = 150, WAVE_GATHER = 100, WAVE_FRAC = 50;
    /** xb9: a striker that has seen the enemy king charges for good, and no waves when the enemy king spot is within
     *  WAVE_MIN (Chebyshev) of home: there the rally point is a few tiles from our king and holding only lets it go. */
    public static final boolean WAVE_LATCH = true;
    public static final int WAVE_MIN = 14;
    /** One in RAID_EVERY fighters is a raider: on our half it ignores enemies beyond dist2 RAID_ENGAGE. */
    public static final int RAID_EVERY = 0, RAID_ENGAGE = 8, RAID_HP = 60;
    /** Bytecodes the king's first-turn sense leaves for the rest of that turn (spawn first of all). 5000 can be short:
     *  on DefaultMedium an s7e king ended its first turn at 20001 of 20000 and missed the round-2 spawn (s7g: 7000). */
    public static final int KING_SENSE_RESERVE = 7000;
    /** King press: at war a rat that sees the enemy king squeaks it (allies relay it PRESS_HOPS times); a rat with
     *  PRESS_HP health or more walks to a king sighting at most PRESS_AGE rounds old within PRESS_D2 (gatherers
     *  carrying less than DELIVER_AT: PRESS_GATHER_D2) and bites it, ahead of standoffs and facing. */
    public static final boolean KING_PRESS = false;
    public static final int PRESS_HOPS = 2, PRESS_HP = 40, PRESS_AGE = 8, PRESS_D2 = 100, PRESS_GATHER_D2 = 20;
    /** KING_PRESS also chases the kings' shared enemy-king slot (Comms.EKING: sightings a king heard squeaked, and enemy
     *  kings a king sees itself), so a sighting reaches every rat, not only those within PRESS_HOPS squeaks. Rusher
     *  kings sit 7-15 tiles from ours for hundreds of rounds (DefaultLarge, heart, uneasy_alliance, Excavation) and
     *  were never bitten; the kings wrote the slot but no rat read it. */
    public static final boolean PRESS_SHARED = false;
    /** Fighters target the nearest enemy rat with less health than theirs (it can be ratnapped). */
    public static final boolean POUNCE = true;
    /** Health at or below which a baby near enemies retreats to its king (0 = never). */
    public static final int RETREAT_HP = 40;
    /** At war a baby with health <= AMMO_HP within Chebyshev AMMO_R of our king's centre, whose nearest enemy rat is
     *  within dist2 AMMO_D2 and has more health (so it can grab us facing it or not), steps away from the king ahead of
     *  retreat(): such rats are the enemy's ammunition. tiny (rsu1): s8u killed s7d's king with 11 of our own rats
     *  thrown point-blank into it (about 42 each); field kings died the same way to rats at Chebyshev 2-3, many <= 40 HP. */
    /** xa3 (explore line): on, unledgered (zii's switch: s6 +3, replica +1 then -2). The anchors' king-kill losses to
     *  collegejunior and Merlin come from our own rats grabbed within Chebyshev 2-5 of our king and thrown into it. */
    public static final boolean AMMO_AWAY = true;
    public static final int AMMO_HP = 50, AMMO_R = 3, AMMO_D2 = 8;
    /** At war a baby within GUARD_KING_R (Chebyshev) of our king targets, ahead of its own nearest enemy, the visible
     *  enemy rat nearest it among those within Chebyshev 2 of the king's centre (in reach to bite it), skips the
     *  standoff against it and bites such rats first (0 = off). 2239 tiny: in r16-48 our king fell 540 -> 32 to 43
     *  bites with 16-19 of our rats near it against 8-14 of theirs; ours held standoffs with their own nearest
     *  enemies and ended 288 of 624 turns with the action unused. */
    public static final int GUARD_KING_R = 0;
    /** Carrier guard: at war a baby within Chebyshev GUARD_CARRY_REACH of our king goes first (as RESCUE does) for
     *  the visible enemy baby nearest it holding one of ours within Chebyshev GUARD_CARRY_R of the king's centre:
     *  it steps to a tile next to the carrier that the carrier cannot see, walking toward one when none is a step
     *  away, and grabs it (a grabbed carrier drops ours). u2v's field games (4807-4810): our king's early deaths
     *  took 292-500 of its ~600 HP from our own rats thrown into it, 8-12 a game, grabbed at Chebyshev 2-4 of its
     *  centre and thrown 2-4 rounds later by carriers at Chebyshev 2-5 (mostly 3-4); GUARD_KING_R covers only
     *  Chebyshev 2 and no carriers. 0 = off. */
    public static final int GUARD_CARRY_R = 0, GUARD_CARRY_REACH = 8;
    /** With GUARD_CARRY_R: a carrier with less health than ours may be grabbed from any tile next to it (the engine's
     *  third grab rule, HEALTH_GRAB_THRESHOLD 0), so the guard goes for the nearest such tile, blind ones first,
     *  instead of only the tiles behind it. */
    public static final boolean GUARD_CARRY_HP = false;
    /** Fortress radius (Chebyshev, from the king) fighters hold at war; 0 = off. */
    public static final int FORT_R = 0;
    /** King rat traps laid in advance at war (no enemy in sight), kept to this many. */
    public static final int FORT_TRAPS = 10;
    /** Avoid tiles where an enemy rat could have laid a trap (TrapRisk). */
    public static final boolean TRAP_RISK = true;
    /** TRAP_RISK only on maps of RISK_MIN_AREA tiles or more (0 = every map). ab1 (u2g without TRAP_RISK, u3p, vs
     *  araid and abrk, both sides): on maps of 900 tiles or fewer 9 cells gained (bed 4, canyoudig 2, DefaultSmall,
     *  arrows 2) and 5 lost (evileye, tiny, cheesefarm, starvation (875 tiles) 2); on bigger maps 1 gained
     *  (learn_your_lesson) and 2 lost (DefaultMedium). In-band field small maps (<= 900): 5 of 22 won. */
    public static final int RISK_MIN_AREA = 0;
    /** Squeak enemy positions/facings so nearby allies share TrapRisk marks. */
    public static final boolean TRAP_SHARE = false;
    /** While chasing a ratnappable enemy, ignore suspected trap tiles. */
    public static final boolean HUNT_IGNORES_RISK = true;
    /** False: the trap-risk mask is dropped while a visible enemy rat is within dist2 20 (so we can close to bite). */
    public static final boolean RISK_IN_FIGHT = false;
    /** Gatherers keep the trap-risk mask while engaged (RISK_IN_FIGHT false then drops it for fighters only): the
     *  trap layers' front tiles are where our gatherers stepped in 50% of the enemy trap hits. */
    public static final boolean RISK_GATHER = false;
    /** Where the trap-risk mask is dropped (engaged, or hunting), still avoid the marks of the last RISK_FRESH rounds
     *  (0 = off): the front tiles of the enemies in view now or just before, where a reactive trap lands; aimCarry's
     *  steps keep to the mask too. s9s's 126 field games: the field laid 44 traps a game and our rats fired 37.8,
     *  42% of them fatal (21% two or three at once); 62% within 2 rounds of the lay, 80% on combat moves (a/f/w/T/k)
     *  with the mask dropped. RISK_IN_FIGHT (150-round marks) froze our fighters (s8h screen -8.1 pts). */
    public static final int RISK_FRESH = 0;
    /** Adaptive RISK_FRESH (0 = off): once enemy rat traps are known to be in play, an engaged or hunting rat avoids
     *  the TrapRisk marks of the last RISK_ADAPT rounds. Known: one of our rats lost health during its own turn (only
     *  a trap fires on our own move), heard an ally's SQ_BURN, or reads Comms.SYM_BURN, which a king sets in the SYM
     *  slot on hearing one. RISK_FRESH 2 cost t0b -11.5 pts at a screen whose opponents lay no traps; the field's
     *  trappers fire theirs on us 0-2 rounds after the lay and grab the stunned half-health rat (3764 superioristhmus:
     *  abcdezyxwv's 160 traps fired 159 times on us, and 137 of 359 grabs in 3765 took a rat at 40-59 health). */
    public static final int RISK_ADAPT = 0;
    /** Rounds between a burned rat's SQ_BURN squeaks while the shared bit is unset. */
    public static final int BURN_EVERY = 3;
    /** Wounded hold (0 = off): once enemy rat traps are known (the burn signal RISK_ADAPT uses), a baby at war with
     *  health <= WOUNDED_HP, which one 50-damage rat trap kills, never steps within Chebyshev 2 of a visible enemy
     *  baby (the 3x3 of a trap it lays beside itself reaches that far); an adjacent one it still bites. Set 2 and
     *  u0q's set-3 block (24 replica matches): 2689 enemy trap fires on our babies; 854 hit a rat at <= 50 health and
     *  806 of those died; of the 1794 that hit a healthier rat, 721 victims died to a later trap within 20 rounds. */
    public static final int WOUNDED_HP = 0;
    /** Face hold (off): once enemy rat traps are known (the burn signal), faceEnemy no longer steps from Chebyshev 2
     *  into contact with an enemy baby that sees us and is not weaker (unless hunting, or it is at our threatened
     *  king): it holds, facing it, and lets it come. In u0m's set-3 field games 54-70% of the enemy traps that fired
     *  on us were laid 0-1 rounds before, mostly on a tile next to both rats, and the commonest victim move code was
     *  this step (mv=f). A rat standing still never sets a trap off. */
    public static final boolean FACE_HOLD = false;
    /** Rush stand-down (0 = off): once enemy rat traps are known (the burn signal), a rusher (ATTACK role) farther
     *  than Chebyshev RUSH_BURN from the enemy start (our start mirrored by the first live symmetry) turns gatherer.
     *  4198 constrictinglattice vs AlexT101 (kings 19 apart): their reactive traps on the contact line (y 26-31, laid
     *  from r12, 45 fires on us by r60) killed all 12 rushers by r57 (traps, then grabs and throws), none nearer than
     *  Chebyshev 4 to the enemy king; we lost 35 rats by r100 to their 14. Near the enemy start the rush goes on:
     *  CS50Crab's first trap fires on us at r7-14 on thunderdome (kings 8 apart) and our rush kills its king by r24. */
    public static final int RUSH_BURN = 0;
    /** The burn signal (a trap fired on our own turn, SQ_BURN, Comms.SYM_BURN) runs for RISK_ADAPT, WOUNDED_HP,
     *  FACE_HOLD or RUSH_BURN. */
    public static final boolean BURN = RISK_ADAPT > 0 || WOUNDED_HP > 0 || FACE_HOLD || RUSH_BURN > 0;
    /** TrapRisk's marks kept one map row at a time, each row allocated on its first mark (TrapRows). The flat W*H
     *  array waits for a turn with 3*W*H + BC_OPTIONAL bytecodes left: 18800 on a 60x60 map, above a baby's 17500,
     *  so on 9 of the 45 field maps no rat ever had marks (15.2-15.5k on 50x50 and 60x40). */
    public static final boolean RISK_ROWS = false;
    /** Guard facing: with an enemy rat within this squared distance, steps strafe so we keep facing it, and a
     *  turn ends facing it (rats are grabbed from behind). 0 = off. */
    public static final int GUARD_DIST2 = 13;
    /** Guard also faces enemies seen at the start of the turn or squeaked by allies when none is in view. */
    public static final boolean GUARD_MEMORY = true;
    /** At war, a turn that ends with no enemy rat known near spends the unused turn facing back, when an enemy rat
     *  was seen or heard within GLANCE_MEM rounds (0 = off). A rat is grabbed when it cannot see the grabber: on
     *  knifefight 32 of bestsofar's 61 grabs came from behind, from enemies we never saw. */
    public static final int GLANCE_MEM = 0;
    /** A baby whose health fell since the end of its last turn, with no cat and no enemy in view, turns to face back
     *  before it senses, so a hidden attacker drives this turn's decision (a gatherer flees the cat). Field losses
     *  u4e: 61% of the cheese loaded babies lost to cats in starved losses went with babies that had no sight of the
     *  cat on any of their last 3 turns, many scratched at dist2 1-2 from behind. */
    public static final boolean CAT_BLIND = false;
    /** CAT_BLIND fires only on a health loss of CAT_BLIND_DMG or twice it since our last turn (0 = any loss). A cat's
     *  scratch is 20; a bite takes 10, a throw 10 + 4 a tile, a rat trap 50, and a pounce kills. u5e (any loss) cut
     *  cat deaths 11% but picks per rat-turn 8.5%. */
    public static final int CAT_BLIND_DMG = 0;
    /** Bite cheese (c = k*k gives +k damage), only when it makes the bite a kill: a king pays up to KING_BITE_MAX
     *  above KING_BITE_FLOOR, a baby up to FINISH_K*FINISH_K above FINISH_FLOOR. */
    public static final int KING_BITE_MAX = 100, KING_BITE_FLOOR = 300, FINISH_K = 5, FINISH_FLOOR = 200;
    /** Flank: step onto a tile next to an enemy rat that it cannot see, facing it, then grab it after moving. */
    public static final boolean FLANK = true;
    /** Rescue: an enemy baby carrying one of ours within dist2 RESCUE_D2 comes first. We keep our action for it (no
     *  bite or trap before moving), step to a tile next to it that it cannot see (any rat may grab an enemy that
     *  cannot see it, and a grabbed carrier drops what it holds) and grab it. s7d's 126 field games: enemy
     *  carriers held ours 137-244 rounds a game, with a free rat of ours within Chebyshev 2 of the carrier on 55-64%
     *  of them but behind it on 5-9%; AlexT101 and abcdezyxwv threw ours into our king 5-7 times a game for 36-41.  zih: on, ledgered (both hooks in Baby, K 9). zie (unledgered) moved 10 set-3 cells, 8 of them with 0-1 rescues
     *  (bytecode drift); its 5-6 early rescues won thunderdome A and B vs Varun at r40/r45.
     */
    public static final boolean RESCUE = true;
    /** RESCUE only for a rat within Chebyshev RESCUE_KING_R of our nearest king (both the rescue walk and tryCarry's
     *  carrier bonus). zj3: every RESCUE build (zie, zih, zil, zio) won thunderdome vs Varun, where its grabs were 2-4
     *  from our king, and lost externalsustenance B and mincemeat A, where the first changed grab was 11-19 from it. */
    public static final int RESCUE_KING_R = 7;
    public static final int RESCUE_D2 = 8;
    /** With RESCUE: when no blind tile next to the carrier is one step away, walk toward the nearest one (useful
     *  with RESCUE_D2 above 8, two or more steps out). */
    public static final boolean RESCUE_WALK = false;
    /** With RESCUE: when no blind tile next to the carrier is one step away, step into the tile in front of it
     *  (a throw needs that tile empty), before RESCUE_WALK. */
    public static final boolean RESCUE_BLOCK = false;
    /** Standoff: do not step toward an enemy rat that sees us and is not weaker; face it and hold instead. */
    public static final boolean STANDOFF = true;
    /** Throws: an enemy baby hit beyond tile 2 counts THROW_FAR_PCT percent; below a THROW_MIN score (42 = a
     *  point-blank wall or rat) the carrier holds for up to THROW_HOLD rounds after the grab. */
    public static final int THROW_FAR_PCT = 40, THROW_MIN = 84, THROW_HOLD = 6;
    /** A carrier of an enemy rat steps (or turns) to the throw whose point-blank tile holds an enemy robot worth at least
     *  AIM_MIN (84 a baby, 126 a king) and throws in the same turn when it can: our throws hit enemy robots 11 times to
     *  abcdezyxwv's 57 (r <= 400, 4 matches), because our carriers never lined a throw up. */
    public static final boolean CARRY_AIM = true;
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
    public static final int AIM_MIN = 84;
    public static final boolean MICRO = false;
    /** Squared distance within which a baby answers a king's alert. */
    public static final int ALERT_DIST2 = 225;
    /** King defense: enemy rats within KING_THREAT_D2 of a king's centre raise its alert's threat count; gatherers
     *  within DEFEND_D2 of it then fight (war movement, no standoff against the king's attackers). */
    public static final boolean DEFEND = true;
    public static final int KING_THREAT_D2 = 18, DEFEND_D2 = 100;
    /** Gatherers pick uniformly among the MINE_SPREAD nearest known mines on our half (1 = always the nearest). */
    public static final int MINE_SPREAD = 4;
    /** Gatherers (and mine-holding fighters) spread evenly over every known home-half mine: slot = a per-rat hash
     *  mod the count, re-picked whenever the team's known-mine count changes (instead of MINE_SPREAD at random, picked
     *  once: on cheesefarm 10-12 of our rats sat at one mine all game while 2 of our 5 were never visited). */
    public static final boolean MINE_BALANCE = true;
    /** A tile is on our half only when it is nearer home than its twin under every live symmetry, not just the first;
     *  MINE_BALANCE also re-picks when the symmetry mask changes. On Meow (flip, rotation live until ~r600) s9s sent 1
     *  in 5 gatherers to the enemy's mine (14,18), the twin of our (14,11) under the flip, and lost them. */
    public static final boolean HOME_ALL_SYM = false;
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
    /** Global cheese below which any rat carrying cheese delivers it before fighting or running errands (0 = off):
     *  31 of our 45 king deaths on the replica were starvation, once with 728 cheese in our rats' paws. */
    public static final int HUNGRY = 0;
    /** Rounds a cheese pile given up after a stall stays ignored, with every pile within Chebyshev 2 of it (0 = off).
     *  EscapeTheNight: five babies in the king's room chased a pile 3 tiles away through a wall for 110 rounds, each
     *  re-picking it the turn after giving up, while their 19-tile path out was held by one another. */
    public static final int CHEESE_BAN = 0;
    /** No chase of cheese in a sealed pocket (Seal.java): a pile whose flood fill over tiles not known to be walls
     *  runs out within SEALED tiles without reaching us (0 = off). learn_your_lesson: four mines sit in walled 5x5
     *  rooms; 5416 of u4e's 6051 cheese-chase turns (ly1, vs s7d) were toward piles inside them, 42% of its rat turns,
     *  and u4e lost all 4 field games there with its king starved in 3 (deliveries 2775-3032 to 6079-6575). */
    public static final int SEALED = 0;
    /** With SEALED: the fill walks only tiles seen (or, with one symmetry left, whose twin was seen) instead of counting
     *  unseen tiles as open. A region that runs out at unseen tiles without reaching us is skipped for Seal.OPEN_TTL
     *  rounds, not marked. Rats see a room's pile through its near wall long before its far wall: in pb5's
     *  learn_your_lesson game (ly3, vs s7d) 9200 of 12200 cheese-chase turns still aimed inside the rooms. */
    public static final boolean SEAL_SEEN = false;
    /** CHEESE_BAN only piles behind a wall: the last flow path toward the pile was a detour (longer than
     *  2 * Chebyshev distance + 2) or its search failed. A stall in a crowd at a mine then bans nothing. */
    public static final boolean CHEESE_BAN_WALL = false;
    /** A cheese pile whose greedy line from us (directionTo steps) crosses a known wall is not chased; a walled-off
     *  target is dropped. Stash (1210, 1211 vs abcdezyxwv): a wall row seals our base but for dug corners; our
     *  rats saw the piles round the mines through it and chased them into the wall ('C'), each pick resetting the
     *  stall that would start a flow search to the mine, so they made 9 pickups to r100 (they 52) and none r100-200. */
    public static final boolean CHEESE_LOS = false;
    /** A cheese target with a flow path searching or ready toward it is kept against a nearer pile whose greedy line
     *  crosses a known wall (z1o's switch, x-line port). Stash B vs araid (xc7 probe bf2 m1, seed 180): gatherer 10044 in
     *  our walled pocket chased piles at y 8 beyond the y=7 wall r18-136, switching 17,8 -> 18,8 -> 20,9 -> 21,8 -> 13,8
     *  -> 21,8 -> 22,8 as each came nearer; its search for 21,8 was still building r70-108 (each switch restarts it).
     *  Gatherers born r0-99 left the pocket a median 69-109 rounds after birth (YokoOno's first four: by r23). */
    public static final boolean CHEESE_STICK = true;
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
    /** A delivery heads for an anchor, the nearest king's tile when it was set, kept while that king stays within
     *  Chebyshev DELIVER_ANCHOR of it (0 = off: the king's own tile). The target then holds still, so the stall
     *  reaches FLOW_STALL and a flow path is searched and followed; with 3 or less a rat at the anchor is in transfer
     *  reach. Unlike NAV_SAME_R the flow path's target stays put. fullofsecrets (u3v mm2, u3z field): carriers with
     *  60-725 cheese stood 30-120 rounds at Chebyshev 4-7 outside the king pocket's wall, path 39 steps round. */
    public static final int DELIVER_ANCHOR = 0;
    /** A gatherer counts as at its mine within Chebyshev MINE_KING_R of it when one of our kings sits within 2 of the
     *  mine (0 = off: always 2), and wanders only to tiles off the kings' bodies. FreeTheCheese: the king's 3x3 covered
     *  the mine, the free tiles within 2 were taken, and a gatherer pathed into the king for 60 rounds. */
    public static final int MINE_KING_R = 0;
    /** A delivery that has not got closer (Chebyshev) to the nearest king for FLOW_STALL turns walks back along the
     *  rat's own trail of walked tiles from its spawn instead (Trail). Flow paths to a pocketed king were rarely of
     *  use: on fullofsecrets 131 of 490 searches failed, and no path was followed to its end in 3 games. */
    /** xa2 (explore line): zgj's delivery trail + ATK_STUCK ported; zgj pooled +8 -5 over two replica blocks. */
    public static final boolean TRAIL = true;
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
    /** TRAIL's chain arrays (2·W·H ints) are allocated at the end of the first turn with at least this many real
     *  bytecodes left past their cost (Trail.alloc). */
    public static final int TRAIL_ALLOC_LEFT = 1500;
    /** A rat already carrying cheese takes no pile that would lift it above this (0 = no cap). */
    public static final int CARRY_MAX = 0;
    /** An attacker takes no pile that would lift it above this, empty-handed or not (0 = off). Attackers run the
     *  after-move pickUp but never deliver. s9s's 126 field games from r400: attack-move turns carried 43-82 cheese
     *  on average and 10-33% of them 100+, and those moved on 31-39% of turns (1% slower per unit carried). */
    /** xd4 (x-line, owner #31: Finals never carries more than 200 raw in any replica game): a non-attacker takes at
     *  most CARRY_CAP - raw of a pile (pickUpCheese(loc, amount)), leaving the rest on the tile (0 = off). xc6's set-3
     *  block lost 123,694 cheese on dead carriers to the opponents' 87,953; its rose carriers held 660-1,997 at
     *  20x cooldown. */
    public static final int CARRY_CAP = 200;
    public static final int ATK_CARRY = 25;   // xd3 (x-line): BitFlow rushers now leave walled pockets and walk long
    // zigzags; on TheHeist (xd2 vs araid s190) 11 rushers held 3,585 raw by r400, each 1% slower a unit, never delivered
    /** An attacker carrying ATK_DELIVER or more raw cheese takes it to the nearest king before attacking again (0 =
     *  off). u2g's in-band field games: attack-move rats held 830 cheese at a time in wins and 319 in losses (every
     *  50 rounds from r100), peaking at 1188 / 666 a game; our babies died holding 1362 / 851 a game (dropped for
     *  the enemy: 975 a lost game of their pickups were drops). 4988 clockwisdom: four attackers held 2500 at
     *  Chebyshev 33-54 from our king while it starved to death (r800-860). Heavy rats crawl (1% slower a unit). */
    public static final int ATK_DELIVER = 0;
    /** A stalled attacker delivers: one holding ATK_STUCK_RAW or more raw whose Nav walk has not got closer to its
     *  target for ATK_STUCK turns takes the load home until it has none (0 = off). fullofsecrets A vs agrab (zgh,
     *  cq172): at r400 six attackers held 40-800 raw (1,870) with Nav stalls up to 54 toward the enemy king in its
     *  walled pocket, while ours starved (picked 4,840, transferred 305). */
    public static final int ATK_STUCK = 30, ATK_STUCK_RAW = 100;
    /** Babies spawned before any fighter is assigned. */
    public static final int GATHER_FIRST = 6;
    /** One in FIGHT_EVERY later babies is a cat fighter. */
    public static final int FIGHT_EVERY = 2;
    public static final boolean CAT_TRAPS = true;
    /** During cooperation, a baby next to a cat-lane tile (CatLane: under a cat on two passes CAT_LAY_GAP or more
     *  rounds apart; cats walk a fixed waypoint loop by BFS) whose cat left it CAT_LAY_AGE or more rounds ago lays a
     *  cat trap there, with CAT_LAY_FLOOR global cheese. A cat trap costs 10, deals 100 (counted as our cat damage, half
     *  the cooperation points) and only cats set it off; new ones are barred once the war starts. s9s's 126 field games:
     *  rose vs spsquared, cat damage 0 to 8080 (they laid 73 cat traps to our 6) and lost on points at r2000; rift 26 to
     *  81 and superioristhmus 50 to 80 laid, both lost on points. */
    public static final boolean CAT_LAY = true;
    /** The cat trap of an approaching cat (tryCatTrap) may also go on any tile beside the cat's body, which the cat sets
     *  off on its next step, and during cooperation needs only TRAP_FLOOR, not the projected surplus. spsquared on rose
     *  (3266): when a cat reached their king, 5-6 rats ringed it with cat traps and six fired at r479 alone (63 of 73
     *  fired; cat damage 8080 to our 0). */
    public static final boolean CAT_RING = true;
    public static final int CAT_LAY_GAP = 20, CAT_LAY_AGE = 3, CAT_LAY_FLOOR = 400;
    /** Rush: babies with spawn index in [RUSH_FROM, RUSH_TO) attack the enemy king from birth. */
    public static final boolean RUSH = true;
    public static final int RUSH_FROM = 4, RUSH_TO = 16;
    /** Rush only on near maps (0 = every map): a newborn rusher keeps ATTACK only while, under some live symmetry, the
     *  enemy start (its king's start mirrored) is within Chebyshev RUSH_DIST; otherwise it takes its non-rush role.
     *  u4e-u5m field games (kdist.py, 294 games by start distance): our king kills by r400 came in 12 of 28 games at
     *  <= 10 and 9 of 24 at 11-20, but 2 of 62 at 21-30 and 7 of 180 at 31+, where we won 47% and 37%. */
    public static final int RUSH_DIST = 0;
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
    /** An attacker keeps the mirror spot it walks to until that spot is seen empty, not the nearest live one each turn.
     *  groveisbeingdesecrated (gr1): the rushers stood at the band's wall midway between (7,2) and (52,2) and flipped
     *  targets 14-21 times by r190; every flip restarted their flow search, which never finished. */
    public static final boolean RUSH_STICK = false;
    /** An attacker passes over a mirror spot whose flow search failed (timed out or found no way) within RUSH_BAN rounds
     *  while another spot is live (0 = off). pk1 averystrangespace (u0m as A, king (3,3)): symmetry still unknown at
     *  r330; the rushers alternated between the flip spots (5,56) and (54,3), and every search toward (5,56) from the
     *  tubes around (9,20) ran 61 turns and failed (201 searches of 60+ turns in the game). */
    public static final int RUSH_BAN = 0;
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
    public static final int RUSH_LIFE = 0;
    /** Rush attackers bite and grab enemy rats during cooperation too (the first bite ends it for everyone). */
    public static final boolean RUSH_BREAK = true;
    /** From this round on (0 = never) every baby bites and grabs enemy rats during cooperation too: the field rushers
     *  open the war at r36-40 (abrk, whose attackers must first walk over, at r66). */
    public static final int BREAK_ROUND = 30;
    /** We never end cooperation: while it lasts no baby bites or grabs an enemy rat or king (RUSH_BREAK, BREAK_ROUND
     *  and the attackers' bite on the enemy king wait for the war). Only a bite, an enemy grab or a fired rat trap
     *  ends it, and the backstabber loses cat traps for 100 rounds while the victim keeps them. u2g's in-band games:
     *  big-map losses open the war at median r31-33, wins at r66-68; we opened 14 of 41 big-map losses and 5 of 17
     *  small; our attackers die 6.2 a game by r100 in losses (3.2 in wins). */
    public static final boolean PEACE = false;
    /** Gatherers spread over every known mine, not only those on our half: the field rushers pick 600-700 cheese a game
     *  on our half in rounds 100-400 (abrk 156). */
    /** xb7 (explore line, owner #31: Finals' economy): on. Replica 25067 keepout, Finals worked 10 mines in r0-500 (ours 5),
     *  our home (28,47) from r32; we picked about 4,000 to its 8,600. u3c (u2g + MINE_ANY + MINE_TIES) pooled +12 -6 over
     *  176 replica cells (Finals dirtpassageway A/B gained). xb7's chains: 35 of 40 to xb4's 30 (+5 -0). */
    public static final boolean MINE_ANY = true;
    /** MINE_ANY during cooperation only: gatherers pick among every known mine, and drop an enemy-half mine once the
     *  war starts. With MINE_MIRROR the king lists each listed mine's twin on either half. Replica, s6m's blocks: by r200
     *  the mines had produced 5199 cheese a game and both teams together had picked up 2154; through r300 mines ranked
     *  6th and beyond from our king gave us 0-11% of their spawn (DefaultLarge (32,54): 760 of 760 lying). */
    public static final boolean MINE_ANY_COOP = false;
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
    public static final boolean DIG = true;
    /** digToward before flow: one-tile dirt bands (cheesefarm's diagonal stripes) are dug through at once. */
    public static final boolean DIG_FIRST = true;
    /** Bug walks dig (off): the wall-follow sweep digs the first dirt tile it meets before any free step instead of
     *  walking around it. Stash: our pocket's exits are dirt at the ends of its wall row, behind dirt stripes; a bug
     *  walk that treats dirt as wall never gets out, so every rat waits 20-26 turns for its own flow search (vs
     *  abcdezyxwv one rat of ours left the pocket by r100, 12 of theirs by r40, digging from r11). */
    public static final boolean BUG_DIG = false;
    /** Dig-aware flow-field navigation, requested after FLOW_STALL turns without progress. */
    public static final boolean FLOW = true;
    public static final int FLOW_STALL = 4, FLOW_TTL = 200, FLOW_RESERVE = 2500, FLOW_HOLD = 3, FLOW_REJOIN = 40, FLOW_MAX_TURNS = 12, FLOW_RETRY = 30;
    /** A path tile held by a robot past FLOW_HOLD turns: keep the path and sidestep onto a free tile beside a later
     *  path tile, dropping it only after FLOW_JAM_HOLD turns. Dropping starts a fresh search, which holds the rat still
     *  for up to FLOW_MAX_TURNS turns and blocks the rats queued behind it (EscapeTheNight: 4-5 babies stuck 500+ rounds). */
    public static final boolean FLOW_JAM = false;
    /** A step along a flow path is progress: it resets Nav's stall, and a turn held still for a search does not add
     *  to it. Without it the stall counts straight-line distance only, so a detour that leads away from the target for
     *  13+ turns ends a cheese chase (25 a mine errand, 20 a raid or explore) on the way. seepastyourgreed (s4d vs araid):
     *  gatherers in the bottom corridor see the mines through the wall above; each errand held 3-11 turns for its
     *  search, walked part of the 45-tile detour, gave up, and the next errand searched again (1840 pickups to 22240). */
    public static final boolean FLOW_PROGRESS = true;
    /** xd2 (with FLOW_PROGRESS on): a turn spent walking greedy while a reverse search toward the target builds does not
     *  add to Nav's stall either (FLOW_PROGRESS only froze it for forward searches, which hold the rat still). Stash B
     *  (xc9 probes bf5, seeds 180-181, r25-150): our gatherers in the walled home pocket spent 60% of their turns with a
     *  search building and 30% with a path ready, and dropped a ready path for another errand 65-78 times a game
     *  (Flow.ends[4]; 8-13 drops for a newly seen wall): the reverse search takes 10-12+ turns there, so the cheese
     *  chase (stall > 12) and the mine errand (> 25) ended before or soon after their paths were ready; gatherer 12591
     *  went cheese 17,8 -> explore x4 -> mine 10,11 -> mine 20,11 -> mine 10,11 in r19-107 without leaving. */
    public static final boolean FLOW_REV_FREEZE = true;
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
    /** Weighted reverse search (FLOW_FAST only; 1 = off, at most 7 - DIG_COST): f = g + FLOW_W * Chebyshev, so tiles
     *  near the rat come up first. Stash (pk1, u0m as A): the first explorer searched toward (10,18) from r9 to r26 and
     *  never finished; with unseen tiles open, A* pops every tile behind the pocket's wall row whose f is under the cost
     *  through the corner exit. Offline on Stash's terrain, 5 exit targets: 419-606 pops at weight 1, 325-457 at 2,
     *  path costs the same. */
    public static final int FLOW_W = 1;
    /** A ready flow path cut by a newly seen wall is mended, not searched afresh (0 = off): a reverse search from the
     *  path tile FLOW_SPLICE - 1 past the first clear one beyond the wall back to the rat (who holds still), with the
     *  rest of the old path joined on; a detour not found within FLOW_SPLICE_TURNS turns starts the full search. pk1
     *  Stash (u0m as B): rushers in the king's pocket saw only part of the wall row; a path through an unseen wall tile
     *  was cut a few steps on and the next search toward (15,26) took 32-37 turns, three times by r168 (the rat wandered
     *  the pocket meanwhile; field 4529: 1156 rusher-turns in the pocket r50-150). */
    public static final int FLOW_SPLICE = 0, FLOW_SPLICE_TURNS = 6;
    /** Flow search steps a rat may run per round (0: as many as its bytecodes allow). Mirrored rats have a few bytecodes
     *  more or less left (Stash, t0q self-play: twin rats a turn apart from r38), so a search cut by bytecodes alone
     *  finishes a turn earlier on one side and the twins walk different paths. A quota under the bytecode cut makes both
     *  finish on the same turn. */
    public static final int FLOW_QUOTA = 0;
    /** Flow's search arrays are allocated a piece at a time (g, par, each bucket row, the path), each once its own length
     *  fits past the reserve, not all in one turn: at once they need 2*W*H + 1700 and then 9216 bytecodes past
     *  FLOW_RESERVE in a single turn (an allocation costs its length), more than a rat whose turn runs 5800 has left.
     *  pb2 probe (t0q): on superioristhmus 48 of the 87 rats that asked for a search never got one going, and 9.6% of
     *  rat-turns (Stash 25.9%) were spent waiting on a requested search; EscapeTheNight self-play: one twin stood 6
     *  turns short of the arrays while the other searched (6410 bytecodes a turn to 12810). */
    public static final boolean FLOW_ALLOC = true;
    /** Navigation by BitFlow's distance fields instead of Flow's paths: waves over row bitboards from the target, no
     *  per-tile search work; the rat walks down the field from wherever it stands. gr1 (grove, 60x60 bands): Flow's
     *  reverse A* ran ~15.5k bytecodes a turn and never finished inside FLOW_REV_TURNS. A field is grown until the
     *  rat's tile is BFLOW_EXTRA waves inside it (more as the rat leaves it). */
    public static final boolean BFLOW = false;
    public static final int BFLOW_EXTRA = 12;
    /** Our kings' 3x3 bodies count as walls for flow paths and bug walks (not when the target is at a king).
     *  EscapeTheNight: A* routed babies through the king that stood between them and the room's exit; they waited behind
     *  it, dropped the path and searched again, 500+ rounds. */
    public static final boolean KING_BLOCK = false;
    /** KING_BLOCK for flow paths only: greedy steps and bug walks still treat a king as a robot to wait for or sidestep.
     *  Without it the A* routes babies through the king's body (EscapeTheNight: babies bound for a mine stood touching
     *  the king for 1266 rounds); KING_BLOCK fixed that but lost on the replica (net -5) and in self-play (net -4). */
    public static final boolean KING_FLOW = true;
    public static final int FLOW_JAM_HOLD = 12;
    /** A flow path dropped because a robot held its next tile past FLOW_HOLD turns: keep that tile out of searches
     *  for FLOW_HELD rounds (0 = off), so the new search routes around the robot instead of rebuilding the same path.
     *  cheesebottles (da1, u4g B): three empty rats in a dead-end strip under two kings, each planning through the
     *  next rat's tile, rebuilt the same path every 6 turns for 230-270 rounds; the inner one's only exit was behind them. */
    public static final int FLOW_HELD = 0;
    /** Path cost of a dirt tile (a dig is 5 cheese and 2.5 turns of action cooldown, plus the step). */
    public static final int DIG_COST = 4;
    /** Random samples per explore target, taking the first never-seen tile. */
    public static final int EXPLORE_TRIES = 12, EXPLORE_CENTER_FROM = 0;
    /** How a scout or seeker picks its next never-seen tile of our half from the EXPLORE_TRIES samples: 0 the nearest
     *  to it, 1 the first (uniform over what is unseen), 2 the farthest. Nearest crawls outward from the king, and each
     *  new scout crawls the same ground again (where_are_you: 5 of our half's 8 mines never seen in 1400 rounds). */
    public static final int HOME_PICK = 1;
    /** A rat that finds the first mine walks it home to the king. */
    public static final boolean COURIER = true;
    /** Babies relay mine squeaks to each other while the team knows no mine. */
    public static final boolean GOSSIP = true;
    /** Gossip all game: a rat away from the king squeaks a mine it knows that the king's list lacks, and every rat
     *  notes the mines it hears, so news reaches the king with the next delivery instead of its finder. */
    public static final boolean GOSSIP_ALWAYS = true;
    /** Symmetry news (SQ_SYM) is squeaked to a king when any of its 9 tiles is in hearing range (d2 16), not only
     *  its centre: delivery spots at d2 17-18 were never heard. */
    public static final boolean SYM_HEAR_TILE = false;
    /** Babies merge SQ_SYM squeaks they hear, and one away from the kings that knows a sharper mask than the shared one
     *  squeaks it every SYM_GOSSIP_EVERY rounds, in peace or with no enemy in view, when an ally is near and no cat
     *  can hear (squeaks draw attacking cats). */
    public static final boolean SYM_GOSSIP = true;   // xe6 (x-line), with SYM_COURIER
    /** xe6 (x-line): a rat that knows a sharper symmetry mask than the shared one, with no enemy in view, walks to the
     *  nearest king within SYM_COURIER (Chebyshev; 0 = off) and report() squeaks the mask once in earshot. Local
     *  probes km1-km3 (xe2-xe4 vs agrab, 40 games): a rat of ours knew the symmetry 99-1986 rounds before our king
     *  (median ~450), and the king never learned it on knifefight B (rat r19), krotus B (r15), whatsthecatdoin B
     *  (r42) or clockwisdom A (r145: attackers on the far corridor; the king kept ROT|HOR, so the enemy mine (2,25)
     *  that it listed never mirrored to our (56,33)). */
    public static final int SYM_COURIER = 30;
    public static final int SYM_GOSSIP_EVERY = 3;
    /** The team mine list is symmetry evidence: a listed mine whose s-twin a robot saw without a mine rules out s
     *  (twins not seen yet are marked and checked when first seen). The king also rules out s when a listed mine's
     *  s-twin would sit closer than the engine's minimum mine spacing (d2 25) to a listed mine. */
    public static final boolean SYM_MINELIST = false;
    /** At birth every robot reads the map's symmetry from the engine's sense order: for a chirality-1 robot
     *  getAllLocationsWithinRadiusSquared lists y reversed (HORIZONTAL), x reversed (VERTICAL) or both (ROTATIONAL);
     *  babies inherit the king's chirality, and on 40 of the 43 bundled maps exactly one king has chirality 1. So one
     *  seat knows the symmetry on round 1 and the other learns nothing (deliberately seat-asymmetric, owner prompt 21).
     *  u2g: the mask stays ambiguous on ~40% of game-sides, median unique round 103. */
    public static final boolean CHIRAL_SYM = false;
    /** A mine as near the enemy start as ours (on a HOR/VER axis: 7 of EscapeTheNight's 11) counts as ours for the
     *  gatherers' pick and the king's home mine, instead of as theirs. */
    public static final boolean MINE_TIES = true;   // xb7, with MINE_ANY
    /** Gatherers skip a home mine one of our kings holds (a king within Chebyshev 4: the king picks up there itself,
     *  KING_ROAM, and cannot step onto babies) while they know KMINE_SKIP or more home mines (0 = off). u2g's in-band
     *  field losses (sweep and v4 sets 1-2, r50-1000): the home mine nearest the king held 8.3 of our babies whenever
     *  any was there, the 2nd-6th were covered 47/39/30/28/17% of rounds and lost 440-940 cheese each to the enemy;
     *  locally (ma2 vs araid) 10.7 of 20 targeted gatherers sat on that mine with 2.7 home mines known. */
    public static final int KMINE_SKIP = 0;
    /** Experiment only (map-specific, never a candidate): the half test takes PathOracle's offline verdict (8-way path
     *  from each start, dirt +4) for the mines where it disagrees with the Euclidean one on uneruesansfin,
     *  wallsofparadis and cheesebottles, to bound what a path-based half test could gain there. */
    public static final boolean PATH_ORACLE = false;
    /** Timed walks for the half test (0 = off; else the margin in steps): gatherers time each walk from their birth or
     *  last delivery to their mine and squeak it to a king in earshot, which keeps each listed mine's fastest walk in
     *  the spare bits of its shared slot (steps / 4). A listed mine whose walk beats its twin's by TRIP_HALF steps is
     *  ours, one TRIP_HALF slower theirs (a twin not walked yet counts its Chebyshev distance from our start, its least
     *  possible walk, so a long detour sends rats to time the twin). uneruesansfin from (4,54): (32,49) is 809 vs 2465
     *  in d2 but 137 steps to its twin's 92, and AlexT101 camps it; wallsofparadis and cheesebottles split near-equal
     *  pairs the other way. A census of 23 panel maps found no other disagreement but exact path ties. */
    public static final int TRIP_HALF = 0;
    /** Scouts: one in SCOUT_EVERY gatherers explores our half (never-seen tiles) until round SCOUT_UNTIL, then
     *  gathers (0 = off). */
    public static final int SCOUT_EVERY = 4, SCOUT_UNTIL = 150;
    /** Scouts explore by Flow's frontier search (the never-seen tile nearest by path cost, digging dirt on the way)
     *  before exploreHome's random never-seen home tile, and chase only piles in line of sight (CHEESE_LOS); with
     *  SCOUT_MINES > 0 they go on scouting until SCOUT_MINES_UNTIL while the team knows fewer than SCOUT_MINES home
     *  mines. hv1 (pb2 self-play, r1-500): no rat of ours ever saw 5 of 8 home mines on where_are_you, 4 of 6 on
     *  attack_on_cat, 3 of 4 on rose (all three behind 5 dirt tiles; (24,8) is 8 tiles from our start) and 5 of 8
     *  on seepastyourgreed; own-half shares 0.04-0.38. */
    /** xa6 (explore line): s9i's harvest bundle (SCOUT_FRONTIER, SCOUT_MINES 4, SCOUT_DRY 100, BREAKOUT_TTL 150; hb1
     *  delivered 4374 to s7d's 3067). Merlin (24945 cheesefarm, 24990 DefaultLarge) made 76-83 pickups a 100 rounds to
     *  our 51-57 with as many babies, 32-67 of them 20+ tiles from its king in r0-300 (ours 12-13). */
    public static final boolean SCOUT_FRONTIER = true;
    /** xd5 (x-line): SCOUT_FRONTIER only once the team knows a mine; before that a scout takes explore()'s far
     *  never-seen samples. mincemeat A vs agrab s190: xd3's first two scouts took frontier tiles 1-2 from home ((4,2),
     *  (2,3), (3,2), (1,3)) to r24 (the cone leaves unseen tiles behind a rat); xa5's walked to (3,22) and (10,5), saw
     *  the centre mine by r27 and the king knew it at r30 (xd3 r55); picked by r100 640 vs 270. */
    public static final boolean FRONTIER_MINE = true;
    /** xe5 (x-line): scouts fan out (0 = off). A scout's frontier search ends only at a never-seen tile of our half in
     *  its sector of home's fan: within SCOUT_FAN degrees of the bearing to the enemy start, left of that or right of
     *  it. The slot picks the first sector; a search that finds nothing moves the scout on to the next. Replaces
     *  SCOUT_FRONTIER / FRONTIER_MINE for scouts, and exploreHome's samples keep to the sector too. clockwisdom A
     *  (xe4 vs agrab s1): both scouts walked the bottom corridor and the spiral; no rat came within 4 of our mines
     *  (56,27) and (56,33), 18-24 tiles up the corridor north of the king, until r656, and the king listed one mine
     *  (theirs) all game. Replica dumps: we picked 23.5 by r300 to the field's 41 there (win 0.17). */
    public static final int SCOUT_FAN = 35;
    /** xf1: a fan scout first walks to home + FAN_WAY_PCT% of the home-to-enemy axis turned FAN_WAY_DEG toward its
     *  sector (Baby.fanWaypoint), giving up after FAN_WAY_STALL stalled turns; then the sector's frontier search. */
    /** Off: clockwisdom A's north scout bug-walked into the east-edge dead end, then waited 42 turns for Flow's path to
     *  (58,42) and walked the bottom corridor west (xfan5). */
    public static final int FAN_WAY_PCT = 0, FAN_WAY_DEG = 60, FAN_WAY_STALL = 20;
    // xe7: the kings squeak each newborn's place in its role's spawn order (SQ_START, as MINE_RR); gatherers 2, 6, 10, ... (xf1: 2 and 3, then 8, 12, ...)
    // scout, taking one flank, the other flank and then the sector ahead. xe5/xe6 on clockwisdom A: both
    // slot-hashed scouts drew the bottom (the spiral's gate and the west corridor), and the north mines were first
    // seen at r595.
    /** xa6: extra Flow reserve on a newborn's first turn while SCOUT_FRONTIER is on (RobotPlayer). */
    public static final int FRONTIER_PAD = 1500;
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
    /** Mine exploring (0 = off): until round MINE_EXPLORE_UNTIL, a gatherer that knows m mines on our half takes
     *  slot % (m + MINE_EXPLORE) and explores never-seen tiles of our half when that is m or more (with 1: half the
     *  gatherers while one mine is known, a third with two), else gathers as before. Replica s6m/s7d games (56 matches): our rats first saw the 2nd nearest home mine at r60 (median;
     *  the field's at r33), the 3rd at r147 (r71) and the 4th at r329 (r103); the field picked 2x our pickups on its
     *  own half by r300. */
    public static final int MINE_EXPLORE = 0, MINE_EXPLORE_UNTIL = 300;
    /** The per-rat hash behind the scout pick and MINE_BALANCE's mine comes from the mirror-invariant birth seed (round,
     *  unsigned offset from the king), not the robot id: mirrored rats got different ids, so different scout roles and
     *  mines, and the two sides of a symmetric game played differently (mirror_check's known asymmetry). */
    public static final boolean SYM_SLOT = true;
    /** Flow\'s search expands neighbours in the side frame (Flow.frame), so mirrored rats find mirrored paths. */
    public static final boolean SYM_FLOW = false;
    /** Map-centre goals (spawn tile, fort facing and traps, centre-ranked exploring) about the true centre, not
     *  W / 2, H / 2, which sits half a tile toward +x/+y on even-sized maps and so favours one side. */
    public static final boolean SYM_CENTRE = false;
    /** Candidate fans (a direction, then its neighbours) try the side G.hand names first, not always the left. */
    public static final boolean SYM_HAND = false;
    /** The king turns to face each spawn tile before it builds: a newborn takes its king's facing, and the maps'
     *  starting king facings are not mirrored (knifefight), so the first rats looked along unmirrored lines. */
    public static final boolean SYM_FACE = false;
    /** Exact ties in tile picks (spawn, traps, dig, rescue, guard, mine site) go by G.tieFirst, an order every twin keeps,
     *  not the loop order. superioristhmus, t0q self-play: the king's two best spawn tiles, a corner and an edge, scored
     *  the same and had the same pick bit, so the dx/dy loop took the corner on one side and the edge on the other
     *  (r27, the first move that differed). */
    public static final boolean SYM_TIE = false;
    /** A salted twin: G.hand's every tie goes the other way (mirror-consistent still). Replica games repeat exactly
     *  against most of the panel (t0f replayed s9s's set-4 block alike in 42 of 42 cells), so a plain twin of an
     *  incumbent replays its accepting luck; a flipped hand is the same play with a fresh draw of the chaos. */
    public static final boolean HAND_FLIP = false;
    /** A rat held SWAP_HOLD turns behind an ally baby on its next step carries the ally, steps into its tile and sets
     *  it down behind once it can act (a carried rat leaves the grid), so two rats pass in a 1-wide corridor (0 = off).
     *  rose: s6m's outbound and inbound gatherers met head-on at r160 in the 1-wide stem out of the king's walled
     *  pocket and held there to the end; the king starved at r1375 with 20 babies (spsquared 2/2, araid 2/2). */
    public static final int SWAP_HOLD = 0;
    /** With SWAP_HOLD: a baby does not step next to an allied baby that carries an ally, so the tile the carrier left
     *  stays free for the drop. rose (s7j as B): followers filled each carrier's old tile in the 1-wide stem, the
     *  carriers could set nobody down, and the stem held three carriers for 800 rounds (1259 grabs, king starved). */
    public static final boolean SWAP_YIELD = false;
    /** With SWAP_HOLD: only an ally coming the other way (facing within 45 degrees of our step's opposite) is carried
     *  past. rose (s7k as A): two outbound rats bound for one mine carried each other back and forth in the queue out
     *  of the pocket; 9 of 20 babies were being carried at r600 and r1200, and the king starved at r1875. */
    public static final boolean SWAP_HEADON = false;
    /** With SWAP_HOLD: an ally facing along our step (within 45 degrees) is not carried past; head-on and side-on ones
     *  are. SWAP_HEADON also refused side-on allies, and s7r starved on EscapeTheNight with 45 cheese picked all game,
     *  every rat jammed within 3 tiles of the king (s7k there: 235 ally carries, 109 head-on, 43 side-on, 54 along). */
    public static final boolean SWAP_NO_ALONG = false;
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
    /** With SEEK_IDLE: each idle spell also moves the gatherer to the next mine of its pick list (MINE_BALANCE slot + a
     *  shift), so known mines the crowd ignores get worked (pipes 3804: 29,15 seen at r62, we took 702, they 3135). */
    public static final boolean IDLE_SHIFT = true;
    /** With IDLE_SHIFT: a FIGHT_MINE fighter idle at its mine (no pickup for SEEK_IDLE turns) moves on along the pick
     *  list too, without seeking, so the guard spreads over the mines thieves work (replica losses: 28 of our-half
     *  piles a game taken by them, 5 in wins). */
    public static final boolean FIGHT_SHIFT = true;
    /** Seekers (SEEK_CROWD, SEEK_IDLE) take the never-seen tile nearest by path cost (Flow's frontier search, digging
     *  dirt on the way) before exploreHome's random one. Local wallsofparadis (sl1, u2g vs abrk): as A the king listed
     *  only (30,3) for 2000 rounds; the seekers' random never-seen targets were out on the edge bands, so nobody
     *  crossed the ring dirt to the inner mines (as B the seekers drew luckier targets and all 8 were listed by r800).
     *  Replica vs AdamTan12 (4545/4546, 4735/4736): one mine worked on wallsofparadis and jail either side while they
     *  worked all 8 and 4; we starved (1170 transferred to 7325 by r692). */
    public static final boolean SEEK_FRONTIER = false;
    /** Scouts and seekers sweep our half by cells (0 = off): the target is the centre of a SWEEP x SWEEP cell of our half
     *  this rat has not seen, the one that minimises the walk there plus its distance on to the rat's lane anchor (the
     *  map centre or a corner, by slot, in the side frame), so scouts fan out instead of drawing random tiles. A cell not
     *  reached in time is banned. Scouts go on while cells are left, to SWEEP_UNTIL. u2g's set-2 games (coverage.py):
     *  by r200 we had seen 38-58% of the map counting mirror twins against 91-99% for abcdezyxwv, AlexT101 and
     *  AdamTan12, and plateaued after r100; home mines 14-23 from our king were never seen (pipes (30,30) and (47,30),
     *  dirtpassageway (7,12), attack_on_cat 4 of 6) while they took them from r83-464. */
    public static final int SWEEP = 0, SWEEP_UNTIL = 400;
    /** Cfg.SWEEP: a cell pick scans while this many bytecodes are left, and goes on next turn below it (u2u's
     *  one-turn scan overran 157 times in 8 cv1 games: ~140 cells with a half test each). */
    public static final int SWEEP_LEFT = 8000;
    /** Once one symmetry is left the king lists the twin on our half of every listed mine (mines come in mirror pairs;
     *  raiders see enemy-half mines whose twins on our half nobody found: DefaultLarge vs abcdezyxwv left 1140 cheese
     *  on two such mines). */
    public static final boolean MINE_MIRROR = false;
    /** Babies take our first king's start, squeaked by the king in a turn it spawns with no cat in sight (cats turn
     *  to squeaks), as home for the half test and the mirrored enemy start (homeHalf, mine trap sites, raid and rush
     *  targets), not the king's tile at their birth. ua1 (uneasy_alliance vs araid): the king walked from its start
     *  (11,15) to (8,21) and with the symmetry unresolved all game the rotational half from (8,21) put 3 of our 6 home
     *  mines on the enemy half; no gatherer went there (field 2072: AlexT101 picked 1140 a game on our half). */
    public static final boolean HOME_START = false;
    /** MINE_BALANCE by spawn order: the king's start squeak (as HOME_START's) also carries the newborn's place n in
     *  its role's spawn order, and the rat takes mine n % m of our m home-half mines, sorted from our first king's
     *  start, instead of its id hash mod m. ue2 (uneasy_alliance vs araid, as A): 29 of 62 rats took (8,27) and 3 took
     *  (2,7); the id hash plus a sort from each rat's birth tile (the king walked to (7,22)) piled them on one mine. */
    public static final boolean MINE_RR = false;
    /** The first king also rules out a symmetry that mirrors its start within Chebyshev 2 of itself (the two kings'
     *  3x3 bodies would overlap), not only onto itself: uneasy_alliance's kings start 7 apart on one row.
     *  xc8 turns it on: Stash (our king (15,3)) kept the horizontal flip live, whose enemy-king spot (14,3) touches our
     *  own king; xc6/xc7 rushers walked to it r4-22 before ruling it out by sight (xbfdbg 11966: nt=14,3 to r22). */
    public static final boolean SYM_KING_GAP = true;
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
    /** Grab-aware micro (GrabMicro) in the rat war: before its bites a rat scores staying and each step, with each
     *  facing, by a grab it gets there (100 + victim hp / 10), a bite (GRAB_BITE; +15 when it ends the victim's
     *  health edge, +40 for a kill) and GRAB_THREAT per enemy that can step next to it into its blind spot (anywhere
     *  next to it when healthier), and takes the best when it beats standing still by GRAB_MARGIN; else steps worse
     *  than standing by GRAB_MARGIN are avoided. Needs GRAB_BC bytecodes left. */
    public static final boolean GRAB_MICRO = true;
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
    /** xf5 (owner #31, Merlin's wounded keep their distance): with no enemy within Chebyshev 2 but one at 3, no step ends
     *  within Chebyshev 2 of a Chebyshev-3 enemy baby with free hands that is strictly healthier than us (it would step
     *  next to us and grab us first) (false = off). 25799/25647 (Merlin DefaultLarge, sunandmoon; collegejunior tiny),
     *  war to r300: our rats at 41-70 hp spent 42-71% of their turns next to a healthier enemy, theirs 14-32%; at <= 40
     *  hp 60-77% vs 41-60%. The micro skipped Chebyshev-3 enemies, so the normal move closed to 2 unchecked. */
    public static final boolean GM_GAP = true;
    /** Throws (Baby.throwChecked): our unseen kings block flights, a hit on our king scores three times a rat's, and a
     *  carrier whose best flight hits one of ours (or turns to find one on it) holds until THROW_LATE rounds into the
     *  carry (it lasts 10; at its end the victim wriggles free or swaps onto the carrier). */
    public static final boolean THROW_CHECK = false;
    public static final int THROW_LATE = 8;
    /** No formation site at war before this round (0 = off): the merge takes 7 babies from a war just begun (z0k's
     *  switch, here in a ledger hook). z1z's replica games at war still running at r250: 2 won, 12 lost with a second
     *  king formed by then; 51 won, 42 lost without (~/diag26b/z1zb*.jsonl). z0k (u9p, drifting bytecodes) pooled
     *  D 27, net +5, t 0.96 over three blocks. */
    public static final int FORM_WAR_FROM = 300;
    /** Rich formation fixes (z2a's FORM_OFF, FORM_FILL and FORM_RICH_RETRY), only for sites opened with two or more
     *  kings (the third king and on), under the bytecode ledger so games without such a site replay u6x's exactly:
     *  the first king does not walk within Chebyshev 5 of the open site; a site with one of our kings' bodies in its
     *  3x3 is dropped at once; a rich site that times out is banned and the next opens RICH_FIX_RETRY rounds later
     *  (u6x waits FORM_RETRY 150); a joiner one tile out steps onto any free tile of the 3x3, and a rat inside steps
     *  on along its entry heading. Field r2000 losses one more king flips (block files, 732 games): superioristhmus vs
     *  spsquared 7 times, 2-3 kings to their 3-4 with 6-14k banked; local z2a vs u9p on superioristhmus formed 12 of
     *  14 bursts to u9p's 6 of 13 (kings at r1190 4/4/4/3 vs 2/2/3/3). */
    public static final boolean RICH_FIX = false;
    public static final int RICH_FIX_RETRY = 20;
    /** RICH_FIX's reach: sites opened with this many kings or more (z2m-z2u: 2). 1 covers the second king's site too:
     *  z1z on superioristhmus A vs araid sent ~3,000 baby-turns to second-king sites at r800-850 and r1000-1050, both
     *  failed, and held one king with 20k banked until END_KING's r1974.
     *  z3b: 2. z2x's one-king fixes (from RICH_FIX_FROM) starved minimaze A vs Xzibit on the replica (18124): a second
     *  king at r825 with 500 banked took 7 of 20 babies at war, the bank was 0 by r950 and both kings starved at r1114;
     *  z1z (17954) kept one king, 8-10 babies and ~200 banked, and won. */
    public static final int RICH_FIX_KINGS = 2;
    /** With RICH_FIX_KINGS 1, one-king sites get the fixes only from this round (z2w: keepout A's income-path second
     *  king at r131 took them, and the game drifted from 5 kings and 70-29 to 4 and 61-38). Two-king sites always do. */
    public static final int RICH_FIX_FROM = 600;
    /** Late rich king (0 = off): from round RICH_LATE to the r1150 cut, a team with two kings and RICH_LATE_BANK
     *  banked names a third king's site (FORM_RICH's path) while Econ.surplus(3), the bank projected to r2000 with
     *  three kings' upkeep, is RICH_LATE_SUR or more. Kings alive at r2000 weigh 0.5 of war points (2-2 is 25-25, 3-2
     *  is 30-20), and after r1200 a team with two kings cannot form another. zc6 cheesefarm vs araid A and B: kings 2-2
     *  from r200, lost 49-50 both (49.99 to 50.01 before truncation); u6x banked 1,682 at r1150 on 11.5 a round of
     *  income (sur=5514 at r1100) and ended with 8,883 unspent, so FORM_RICH_BANK 3000 came at r1300, too late. */
    public static final int RICH_LATE = 0, RICH_LATE_BANK = 600, RICH_LATE_SUR = 3000;
    /** More late kings (0 = off): from round RICH_MORE to the r1150 cut, a team with three or four kings and
     *  RICH_MORE_BANK banked names one more king's site (FORM_RICH's path) while Econ.surplus(kings + 1) is RICH_MORE_SUR
     *  or more. Field r2000 losses (624 replica games): superioristhmus vs spsquared sat at 2-3 kings to their 3-4 with
     *  3-15k banked; five kings to their four is 27-22 of the kings' 50 war points, four to four 25-25. */
    public static final int RICH_MORE = 0, RICH_MORE_BANK = 600, RICH_MORE_SUR = 3000;
    /** Rich formation gap (0 = off): when a king forms at a site opened with two or more kings, the next site may open
     *  RICH_GAP rounds later instead of after FORM_TTL + FORM_RETRY (210). Kings came 210 rounds apart (z2q,
     *  superioristhmus A vs araid: r685, r904, r1111), so RICH_MORE's fifth king had no time before the r1150 cut. */
    public static final int RICH_GAP = 0;
    /** RICH_GAP's reach: sites opened with RICH_GAP_KINGS or more kings, formed from round RICH_GAP_FROM on (z2r: 2 and
     *  0). 1 lets the third site open 40 rounds after the second king forms (z2r, superioristhmus A: second king r685,
     *  third r904); the floor keeps it off early second kings (formed before r600 they went 5-12 in the u4e field). */
    public static final int RICH_GAP_KINGS = 1, RICH_GAP_FROM = 600;
    /** Endgame cat damage (0 = off): from round END_CAT at war, a baby of any role that holds less than DELIVER_AT raw,
     *  carries no rat, is not needed at a threatened king and has no enemy rat within dist2 8 goes for the nearest seen
     *  or known cat (Baby.endCat), and the bite step bites it. At war the r2000 points are 0.5 kings, 0.3 cat damage and
     *  0.2 transfer shares. z0c (u9p + END_CAT 1650) pooled D 19, net +5, t 1.15 over two replica blocks, cat share 0.658
     *  to 0.584, and was rejected only by the transfer floor (set 1: 0.86 of the twin per round, floor 0.9). From r1850
     *  the errand covers 7.5% of a full game, so transfer can fall at most about that much. */
    public static final int END_CAT = 1850;
    /** Rescue formation (0 = off): while global cheese is under CASH_BANK (and at least CASH_FLOOR, the 50 a king costs
     *  plus a margin), a baby holding CASH_RAW+ raw cheese, Chebyshev CASH_FAR+ from every king, that sees CASH_N-1 more
     *  of our babies within Chebyshev CASH_R holding CASH_MIN raw with its own, none heavier, leads a formation on a
     *  clear 3x3 at or beside itself. It squeaks the site (SQ_CASH) and every baby that hears it within Chebyshev
     *  CASH_PULL walks into the 3x3 and relays it. The engine banks the raw cheese of every rat in the 3x3 when the
     *  centre promotes (becomeRatKing), so a stuck crowd's load reaches the bank without a walk home, and the new king
     *  stands where they were. A site lives CASH_TTL rounds from its start; then no rescue for CASH_TTL more.
     *  Replica starvation losses (435 dumped games): in 92 our last king starved, in 17 our babies held 300+ raw then.
     *  When the bank first fell under 150, a group of 7+ of ours within Chebyshev 5 held 320-935 raw in 11 games:
     *  fullofsecrets (outside the king's sealed pocket, 3-7 out), rose (the stem, 5-8 out), clockwisdom, uneasy_alliance.
     *  Local fullofsecrets (u9p vs araid A): attackers held 120-777 each around the centre mines from r400, 20 out. */
    public static final int CASH_BANK = 250, CASH_FLOOR = 60, CASH_RAW = 60, CASH_N = 3, CASH_R = 4, CASH_MIN = 250,
        CASH_FAR = 5, CASH_PULL = 8, CASH_TTL = 50;
    /** Endgame rescue (0 = off): from this round a lone king's babies run CASH's formation for a second king, with no
     *  raw cheese needed, while the bank keeps the new king alive to round 2000 (Baby.cashEnd). At r2000 kings are half
     *  of the war points (0.3 in cooperation): 1 king to their 1 splits 25-25, 2 to 1 is 33-17. u6x's END_KING needs
     *  400 banked and the king to name a site; 9 of the 12 lone-king replica points losses never had 400. */
    public static final int CASH_END = 1880;
    /** CASH's rescue runs only while the team has at most this many kings (CASH_END is lone-king anyway). With two
     *  kings the bank sits at the spawn floor (250-300) for hundreds of rounds, so "under CASH_BANK" is no sign of
     *  starvation: z2x on starvation B vs araid (zd151, ~/diag26b/zst) formed a third king by CASH at r970 with the
     *  bank at 248-307, and all three starved together at r1373 (z1z, on two kings, banked 750-960 and won). */
    public static final int CASH_KINGS = 0;
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
}
