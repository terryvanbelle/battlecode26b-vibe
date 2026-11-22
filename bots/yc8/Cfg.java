package yc8;

/**
 * Behaviour constants, grouped by subsystem. A switch documented "0 = off" is off at that value. Distances are
 * Chebyshev unless named *_D2 / *_R2 / *DIST2 (squared Euclidean); cheese amounts are global cheese unless stated.
 * Several switches are copied into a non-final field of the class that uses them, so tests can change them.
 */
public final class Cfg {

    // ---- spawning: how many babies the king keeps

    /** Baby cap in peace and at war. */
    public static final int MAX_BABIES = 16, MAX_BABIES_WAR = 16;
    /** With RICH_BANK or more banked the cap rises to RICH_BABIES (0 = off): a surplus buys bodies. */
    public static final int RICH_BABIES = 40, RICH_BANK = 1000;
    /** The RICH tiers open at this round (0 = from the start). */
    public static final int RICH_FROM = 300;
    /** RICH_FROM applies only on maps of at least this many tiles (0: every map). */
    public static final int RICH_FROM_AREA = 0;
    /** A second tier: with RICH2_BANK or more banked the cap rises to RICH2_BABIES (0 = off). Babies cost only their
     *  spawn price, so a big bank is best turned into rats. */
    public static final int RICH2_BABIES = 80, RICH2_BANK = 3000;
    /** Live babies kept unconditionally (while CORE_FLOOR cheese remain) before the income test applies. */
    public static final int CORE_BABIES = 18, CORE_FLOOR = 300;
    /** On maps of CORE_SMALL_AREA tiles or fewer (0 = off) the core count is CORE_SMALL: small maps starved the king
     *  paying for 18 babies at a rising price. */
    public static final int CORE_SMALL = 12, CORE_SMALL_AREA = 441;
    /** Projected end-of-game cheese a peacetime spawn past the core must leave. */
    public static final int SPAWN_MARGIN = 100;
    /** Babies spawned before any fighter is assigned. */
    public static final int GATHER_FIRST = 6;
    /** One in FIGHT_EVERY later babies is a cat fighter. */
    public static final int FIGHT_EVERY = 2;
    /** Babies with spawn index in [RUSH_FROM, RUSH_TO) are attackers (rushers). */
    public static final int RUSH_FROM = 4, RUSH_TO = 16;
    /** Rush spawns stop at this round (0 = never): the spawn index is the live-baby count, so without it every
     *  replacement would be an attacker all game. */
    public static final int RUSH_UNTIL = 300;
    /** The king turns to face each spawn tile before it builds (newborns take its facing). */
    public static final boolean SYM_FACE = false;
    /** Bytecodes the king's first-turn sense leaves for the rest of that turn (5000 missed the round-2 spawn). */
    public static final int KING_SENSE_RESERVE = 7000;

    // ---- spawn floors: cheese a spawn must leave in the bank

    /** Cheese kept when spawning under attack. */
    public static final int WAR_FLOOR = 300;
    /** Hard floor under every spawn (king upkeep). */
    public static final int SPAWN_FLOOR = 300;
    /** With fewer than LOW_BABIES babies every floor drops to LOW_FLOOR: a king without rats has no income. */
    public static final int LOW_BABIES = 8, LOW_FLOOR = 200;
    /** At war with enemies in the king's sight every spawn floor drops to SIEGE_FLOOR (0 = off). */
    public static final int SIEGE_FLOOR = 0;
    /** Last stand (0 = off): at the base baby cost (3 or fewer live babies) every spawn floor drops to LAST_FLOOR, so
     *  a king without income spends its reserve on rats instead of starving on it. */
    public static final int LAST_FLOOR = 20;

    // ---- economy: income estimate, reserves, austerity

    /** Fraction (percent) of measured income trusted when projecting forward. */
    public static final int INCOME_TRUST = 75;
    /** Rounds of income history in the rate estimate. */
    public static final int INCOME_WINDOW = 100;
    /** Every optional spend (spawns over the floors, babies' traps away from a king) leaves KING_RESERVE per king,
     *  never more than the upkeep to round 2000 (0 = off); digs leave DIG_RESERVE. Starvation was the top loss cause. */
    public static final int KING_RESERVE = 400, DIG_RESERVE = 300;
    /** Austerity from round AUSTERE_FROM (0 = off) while the kings' projection (Comms.BUDGET) is 0 or less: babies lay
     *  no rat traps and no cat traps away from a king, and the king's core count drops to AUSTERE_CORE (99: no cut). */
    public static final int AUSTERE_FROM = 60, AUSTERE_CORE = 99;
    /** Projected surplus (cheese) a baby needs before placing a cat trap / biting with cheese. */
    public static final int TRAP_SURPLUS = 200, CHEESE_BITE_SURPLUS = 600;
    /** Cheese below which no cat trap is placed; cats within TRAP_DEFEND_DIST2 of a king are trapped on this floor
     *  alone. */
    public static final int TRAP_FLOOR = 600, TRAP_DEFEND_DIST2 = 100;

    // ---- king: seat, movement, dodging

    /** The king sits on the known mine nearest its start within this dist2 and on our half. */
    public static final int KING_MINE_R2 = 400;
    /** King.pickHomeMine skips a mine whose front value 100 (e - d) / L2 is under KING_FRONT (0 = off): d, e = dist2
     *  from the mine to our start and the enemy's, L2 = dist2 between the starts (100 at our start, 0 on the
     *  bisector). A king seated near the bisector gets bitten to death. */
    public static final int KING_FRONT = 20;
    /** King path (KPath): at most KP_MAX steps searched; the search needs KP_RESERVE bytecodes left, gives up after
     *  KP_TURNS turns and retries KP_RETRY rounds after a failure (or when the mine list grows). */
    public static final int KP_MAX = 32, KP_RESERVE = 2500, KP_TURNS = 40, KP_RETRY = 30;
    /** No centre on the king's path or seat nearer the bisector than KP_FRONT percent of the start's own distance
     *  from it (0 = off). */
    public static final int KP_FRONT = 20;
    /** When a search at KP_FRONT finds no path, the next one (at once) uses KP_FRONT_LOW percent (-1 = off); a failed
     *  low search hands back to KP_FRONT. Kings that never left their start lost on points. */
    public static final int KP_FRONT_LOW = 1;
    /** Footprints ahead of a cat that King.catRisk scores. */
    public static final int CAT_RISK_STEPS = 6;
    /** King.catRisk adds this per cat-lane tile under the king's 3x3. */
    public static final int LANE_RISK = 40;
    /** Cats within this dist2 of the king's centre make it dodge. */
    public static final int KING_DODGE_R2 = 25;
    /** The boxed-in dig scores 8 more steps; skipped below this many bytecodes left. */
    public static final int DODGE_DIG_LEFT = 5000;
    /** The king steps off the flights of enemy babies within THROW_DODGE that hold one of ours (King.throwDodge;
     *  0 = off, negative = compute but never act). */
    public static final int THROW_DODGE = 9;
    /** The king digs only with this much cheese banked. */
    public static final int KING_DIG_MIN = 100;
    /** With no enemy or cat in view the king walks to cheese within KING_ROAM of its mine (0 = off). */
    public static final int KING_ROAM = 4;
    /** The king's walks to a pile and to its home mine take only strictly closer steps (King.closer), no sidesteps;
     *  a king stepping back and forth reset its carriers' stall counts. */
    public static final boolean KING_STRICT = false;
    /** The passage-plug checks (King.unplugged / unplug) run only with this many bytecodes left. */
    public static final int PLUG_LEFT = 3000;
    /** A king bites with cheese only to make a kill: up to KING_BITE_MAX above KING_BITE_FLOOR banked. A baby pays up
     *  to FINISH_K * FINISH_K above FINISH_FLOOR. */
    public static final int KING_BITE_MAX = 100, KING_BITE_FLOOR = 300, FINISH_K = 5, FINISH_FLOOR = 200;
    /** Enemy rats within KING_THREAT_D2 of a king count as its threat (Comms.ALERT); a baby within DEFEND_D2 of an
     *  alerting king defends it. */
    public static final int KING_THREAT_D2 = 30, DEFEND_D2 = 100;
    /** Squared distance within which a baby answers a king's alert. */
    public static final int ALERT_DIST2 = 225;

    // ---- king formation: more kings, endgame kings, rescue formation

    /** Formation: from round FORM_FROM, with FORM_BABIES babies and FORM_CHEESE banked, the first king names a site
     *  that lives FORM_TTL rounds, then waits FORM_RETRY; babies within dist2 FORM_PULL_R2 walk to it. */
    public static final int FORM_FROM = 80, FORM_BABIES = 16, FORM_CHEESE = 300, FORM_TTL = 60, FORM_RETRY = 150, FORM_PULL_R2 = 72;
    /** Kings we form up to (engine max 5 before round 1200). */
    public static final int FORM_MAX = 3;
    /** A site for a third or later king opens only while the bank holds this much (0 = off): an unfed third king
     *  starved all three. */
    public static final int FORM3_BANK = 1200;
    /** At war: kings we form up to (0 = FORM_MAX). */
    public static final int FORM_MAX_WAR = 0;
    /** Rich kings (0 = off): up to round 1150, while the bank holds FORM_RICH_BANK, the first king names sites past
     *  the cap, up to FORM_RICH_MAX kings, with no income test. */
    public static final int FORM_RICH_BANK = 3000, FORM_RICH_MAX = 4;
    /** Form only while the trusted income (x100 per round) beats every king's upkeep, the new one's included, by
     *  FORM_NET100 (Integer.MIN_VALUE = no income test). */
    public static final int FORM_NET100 = 100;
    /** A baby outside the site's 3x3 joins only while it sees fewer than FORM_JOIN allied babies nearer the site
     *  (0 = every baby within FORM_PULL_R2 joins). */
    public static final int FORM_JOIN = 8;
    /** No formation site at war before this round (0 = off): the merge takes 7 babies from a war just begun. */
    public static final int FORM_WAR_FROM = 300;
    /** Endgame king (0 = off): from round END_KING a lone king names a site for a second whenever 7 babies and
     *  END_KING_CHEESE are there, with no income test. Living kings weigh 0.5 of the war points at round 2000. */
    public static final int END_KING = 1850, END_KING_CHEESE = 400;
    /** Rescue formation (run only as CASH_END's endgame rescue): a baby CASH_FAR+ from every king, that sees
     *  CASH_N - 1 more of ours within CASH_R, none heavier, leads a formation on a clear 3x3 at or beside itself and squeaks it (SQ_CASH); babies within CASH_PULL walk in. The
     *  promotion banks every rat's raw cheese in the 3x3. A site lives CASH_TTL rounds, then no rescue for CASH_TTL
     *  more. CASH_FLOOR: the least bank the endgame rescue needs. */
    public static final int CASH_FLOOR = 60, CASH_N = 3, CASH_R = 4,
        CASH_FAR = 5, CASH_PULL = 8, CASH_TTL = 50;
    /** Endgame rescue (0 = off): from this round a lone king's babies run the rescue formation for a second king, with
     *  no raw cheese needed, while the bank keeps the new king alive to round 2000 (Baby.cashEnd). */
    public static final int CASH_END = 1880;
    /** cashTick stops reading squeaks once fewer than this many bytecodes are left (1 = never): a crowd on a rescue
     *  site squeaks every turn, and reading them all overran. */
    public static final int CASH_READ_LEFT = 6000;

    // ---- attackers (rushers)

    /** Rushers use BitFlow only on maps of at least this many tiles. */
    public static final int RUSH_BFLOW_AREA = 600;
    /** A rusher whose Nav stall reaches BF_GIVEUP leaves BitFlow for Flow, which digs through (0 = never). */
    public static final int BF_GIVEUP = 60;
    /** BitFlow.follow holds the rat still when fewer bytecodes than this are left (its 8-neighbour scan is unbudgeted). */
    public static final int BF_FOLLOW_LEFT = 3000;
    /** An attacker older than RUSH_LIFE rounds that has not seen the enemy king in the last RUSH_LIFE rounds turns
     *  gatherer (0 = off). */
    public static final int RUSH_LIFE = 150;
    /** From this round on (0 = never) every baby bites and grabs enemy rats during cooperation too (field rushers open
     *  the war at r36-40). */
    public static final int BREAK_ROUND = 30;
    /** An attacker carries at most this much raw cheese (pickups are capped). */
    public static final int ATK_CARRY = 25;
    /** A stalled attacker delivers: one holding ATK_STUCK_RAW+ raw whose Nav walk has not got closer for ATK_STUCK
     *  turns takes the load home until it has none (0 = off). */
    public static final int ATK_STUCK = 30, ATK_STUCK_RAW = 100;
    /** Stalled attackers hunt cats (0 = off): in cooperation an attacker within 3 of one tile for STALL_CAT rounds
     *  goes for cats for the rest of cooperation (cooperation points weigh cat damage 0.5). */
    public static final int STALL_CAT = 100;

    // ---- combat: fighting enemy rats

    /** War role split: 0 everyone fights, 1 gatherers ignore non-adjacent enemies, 2 and never freeze to face. */
    public static final int WAR_GATHER = 2;
    /** Health at or below which a baby near enemies retreats to its king (0 = never). */
    public static final int RETREAT_HP = 40;
    /** A baby with AMMO_HP health or less, within AMMO_R of our king and dist2 AMMO_D2 of a healthier enemy rat, is
     *  ammunition the enemy could throw at our king: it moves away. */
    public static final int AMMO_HP = 50, AMMO_R = 3, AMMO_D2 = 8;
    /** Guard facing: with an enemy rat within this dist2, steps strafe to keep facing it and a turn ends facing it
     *  (rats are grabbed from behind; 0 = off). */
    public static final int GUARD_DIST2 = 13;
    /** Rescue only for a rat within RESCUE_KING_R of our nearest king (the rescue walk and tryCarry's carrier bonus);
     *  the enemy carrier must be within dist2 RESCUE_D2. */
    public static final int RESCUE_KING_R = 7;
    public static final int RESCUE_D2 = 8;
    /** Bite order counts loot (0 = off): an enemy baby carrying raw cheese ranks as if up to CARRY_BITE health weaker. */
    public static final int CARRY_BITE = 0;
    /** walled() traces at most this many lines a turn (~300 bytecodes each; 0 = no cap); past it an enemy counts as in
     *  the open. */
    public static final int WALL_BLIND_CAP = 6;
    /** Grab micro (GrabMicro) score weights: GRAB_THREAT per enemy that can grab us, GRAB_BITE for a bite; it acts
     *  only when the best option beats standing still by GRAB_MARGIN, and runs only with GRAB_BC bytecodes left. */
    public static final int GRAB_THREAT = 15, GRAB_BITE = 40, GRAB_MARGIN = 10, GRAB_BC = 12000;
    /** With an enemy within 2 the grab micro also scores tiles and facings that hand our cheese to the king
     *  (+GM_DELIVER; 0 = off); with none worth it Baby.deliver keeps its facing (why 'G') while the bank holds DG_BANK.
     *  The micro's scoring stops with fewer than GM_LEFT bytecodes left. */
    public static final int GM_DELIVER = 20, DG_BANK = 150, GM_LEFT = 1500;

    // ---- carrying and throwing enemy rats

    /** The post-move throw scan runs only with this many bytecodes left (it costs up to ~6000). */
    public static final int THROW_BC = 7000;
    /** Throws: an enemy baby hit beyond tile 2 counts THROW_FAR_PCT percent; below a THROW_MIN score (42 = a
     *  point-blank wall or rat) the carrier holds for up to THROW_HOLD rounds after the grab. */
    public static final int THROW_FAR_PCT = 40, THROW_MIN = 84, THROW_HOLD = 10;
    /** A thrown rat that hits a cat dies outright: a cat first on the flight scores CAT_FEED at tiles 1-2, 60% at 3-4
     *  and 30% beyond (Baby.feedScore; 0 = the cat scores -50). */
    public static final int CAT_FEED = 150;
    /** A carrier of an enemy rat that sees a cat within CARRY_CAT (0 = off) and no enemy king walks to it, holding every
     *  throw below a point-blank feed until CARRY_KING_LEFT rounds of the carry are left. */
    public static final int CARRY_CAT = 6;
    /** A carrier of an enemy rat that sees an enemy king within CARRY_KING (0 = off) walks to it and holds every throw
     *  that would not hit a king until CARRY_KING_LEFT rounds of the carry are left. */
    public static final int CARRY_KING = 8, CARRY_KING_LEFT = 2;
    /** A holding carrier throws now when an enemy baby with free hands is within THREAT_THROW (0 = off) and no flight
     *  hits one of ours: a grabbed carrier drops its rat. */
    public static final int THREAT_THROW = 2;
    /** The carry aim stops searching at AIM_LEFT or fewer bytecodes left and takes the best throw found so far. */
    public static final int AIM_LEFT = 3000;

    // ---- traps

    /** A baby lays a rat trap only against an enemy within this dist2. */
    public static final int BABY_TRAP_DIST2 = 13;
    /** Cheese (raw + global, per king) below which a baby places no rat trap. */
    public static final int RAT_TRAP_MIN_CHEESE = 100;
    /** Most live rat traps we keep (engine max 25). */
    public static final int TRAP_CAP = 25;
    /** A rat trap against an enemy within this dist2 of our nearest king skips the KING_RESERVE and austerity gates
     *  (0 = never). */
    public static final int DEF_TRAP_DIST2 = 64;
    /** Baby rat traps only on far maps (0 = every map): only when no live symmetry puts the enemy start within
     *  TRAP_DIST. */
    public static final int TRAP_DIST = 0;
    /** Cat lanes (CatLane): a tile a cat covers on two passes CAT_LAY_GAP+ rounds apart is lane; a baby lays a cat trap
     *  on a lane tile the cat left CAT_LAY_AGE+ rounds ago, with CAT_LAY_FLOOR cheese or more. */
    public static final int CAT_LAY_GAP = 20, CAT_LAY_AGE = 3, CAT_LAY_FLOOR = 400;

    // ---- cats

    /** Squared distance (to the cat's 2x2 centre, doubled coords / 4) a gatherer keeps from cats. */
    public static final int GATHER_CAT_DIST = 32;
    /** A cat within dist2 CAT_DEFEND_R2 of one of our kings draws every rat that sees it, gatherers included (0 = off). */
    public static final int CAT_DEFEND_R2 = 64;
    /** A rat whose walk to a cat has stalled CAT_REACH turns, or whose way there is a known detour, leaves cats alone
     *  for CAT_REACH_TTL rounds (0 = off). */
    public static final int CAT_REACH = 0, CAT_REACH_TTL = 100;
    /** Endgame cat damage (0 = off): from round END_CAT at war a baby that is not needed elsewhere goes for the nearest
     *  seen or known cat (Baby.endCat). */
    public static final int END_CAT = 1850;
    /** END_CAT during cooperation, from this round (0 = off): cat damage weighs 0.5 of cooperation points. */
    public static final int END_CAT_COOP = 1500;
    /** A lone king's babies take END_CAT_COOP too, until this round (0 = off); with no cat known they walk to the map
     *  centre. */
    public static final int END_CAT_COOP_FREE = 1750;
    /** In cooperation a gatherer whose walk to its mine stalls past 25 turns takes the END_CAT errand for this many
     *  rounds (0 = off). */
    public static final int MINE_STALL_CAT = 100;
    /** At war fighters leave cats alone until this round (Baby.warNoCat). */
    public static final int WAR_NO_CAT_END = 1200;

    // ---- gathering and delivery

    /** Raw cheese at which a gatherer heads home. */
    public static final int DELIVER_AT = 30;
    /** Raw cheese at which a gatherer with no pile in sight heads home (at DELIVER_AT it goes anyway). */
    public static final int DELIVER_IDLE = 10;
    /** Full loads (0 = off): a gatherer heads home only once it carries LOAD_AT raw (or late), replacing the
     *  DELIVER_AT / DELIVER_IDLE rule. */
    public static final int LOAD_AT = 0;
    /** A non-attacker takes at most CARRY_CAP - raw of a pile, leaving the rest (0 = off): carried cheese slows moves
     *  and is lost when the carrier dies. */
    public static final int CARRY_CAP = 200;
    /** A gatherer counts as at its mine within MINE_KING_R of it when one of our kings sits within 2 of the mine
     *  (0 = off: always 2). */
    public static final int MINE_KING_R = 0;
    /** Baby.wanderAround draws its tile within WANDER_R of the mine (0 = 3); atMine holds at 2, so a ring-3 draw sent
     *  the rat out and back. */
    public static final int WANDER_R = 2;
    /** A FIGHT rat at war takes its mine from the contested home-half mines: those whose steps from the mirrored
     *  enemy start exceed those from home by at most FIGHT_POST (0 = off). */
    public static final int FIGHT_POST = 0;
    /** Trail home: the stall count and homing carry over a gap of up to TRAIL_GAP rounds between delivery turns (a
     *  loaded carrier skips rounds, so 0 restarted the count every skip). */
    public static final int TRAIL_GAP = 12;
    /** Delivery turns without getting closer to the nearest king before the trail home is walked. */
    public static final int TRAIL_STALL = 20;
    /** Mine seeking (0 = off): a gatherer at its mine that sees SEEK_CROWD or more allied babies nearer that mine
     *  explores never-seen tiles of our half for SEEK_TTL rounds, at most once per SEEK_RETRY rounds. */
    public static final int SEEK_CROWD = 6, SEEK_TTL = 150, SEEK_RETRY = 200;
    /** Idle seeking (0 = off): a gatherer that spends SEEK_IDLE turns within SEEK_IDLE_R of its mine without a pickup
     *  seeks as SEEK_CROWD does. */
    public static final int SEEK_IDLE = 20, SEEK_IDLE_R = 4;
    /** A baby digs only with this much raw + global cheese. */
    public static final int DIG_MIN_CHEESE = 60;

    // ---- exploration, scouting, symmetry

    /** Random samples per explore target, taking the first never-seen tile; with no mine known from round
     *  EXPLORE_CENTER_FROM, the explore pick ranks by closeness to the map centre. */
    public static final int EXPLORE_TRIES = 12, EXPLORE_CENTER_FROM = 0;
    /** How a scout or seeker picks its next never-seen tile of our half from the EXPLORE_TRIES samples: 0 the nearest,
     *  1 the first (uniform over what is unseen), 2 the farthest. */
    public static final int HOME_PICK = 1;
    /** Scouts: one in SCOUT_EVERY gatherers explores our half until round SCOUT_UNTIL, then gathers (0 = off). */
    public static final int SCOUT_EVERY = 4, SCOUT_UNTIL = 150;
    /** Scouts fan out (0 = off): a scout's frontier search ends only at a never-seen tile of our half in its sector,
     *  within SCOUT_FAN degrees of the bearing to the enemy start, left of that or right of it. */
    public static final int SCOUT_FAN = 35;
    /** Scouting continues while the team knows fewer than SCOUT_MINES home mines, until SCOUT_MINES_UNTIL (0 = off). */
    public static final int SCOUT_MINES = 4, SCOUT_MINES_UNTIL = 600;
    /** SCOUT_MINES scouting also stops once the known home mines have not grown for SCOUT_DRY rounds (0 = off). */
    public static final int SCOUT_DRY = 100;
    /** Scouts keep going until round W*H/SCOUT_AREA when that is later than SCOUT_UNTIL (0 = off). */
    public static final int SCOUT_AREA = 0;
    /** Breakout (0 = off): a baby that gives up two chases of walled-off cheese within 100 rounds, or whose explore
     *  search fails, explores toward the never-seen tile nearest by path cost for BREAKOUT_TTL rounds, digging. */
    public static final int BREAKOUT_TTL = 150;
    /** A scout's walk searches again from its end while PIPE_AHEAD or fewer steps are left. */
    public static final int PIPE_AHEAD = 4;
    /** A robot holding the walk's next tile past FLOW_HOLD turns keeps the next scout searches off that tile for
     *  PIPE_HELD rounds (0 = off). */
    public static final int PIPE_HELD = 20;
    /** A scout held PIPE_WAIT turns at its walk's end for a search drops it and walks to sampled never-seen tiles of
     *  its sector for PIPE_REST rounds (0 = off). */
    public static final int PIPE_WAIT = 5, PIPE_REST = 12;
    /** A rat that knows a sharper symmetry mask than the shared one, with no enemy in view, walks to the nearest king
     *  within SYM_COURIER (0 = off) and squeaks the mask once in earshot. */
    public static final int SYM_COURIER = 30;
    /** Away from the kings a rat squeaks a sharper mask every SYM_GOSSIP_EVERY rounds. */
    public static final int SYM_GOSSIP_EVERY = 3;

    // ---- navigation: Nav, Flow, BitFlow, swaps

    /** A goTo target within NAV_SAME_R of the last one keeps the stall count and bug state (0 = only an equal tile). */
    public static final int NAV_SAME_R = 0;
    /** digToward before flow: one-tile dirt bands are dug through at once. */
    public static final boolean DIG_FIRST = true;
    /** DIG_FIRST also runs while a reverse flow search builds, not only with no search. */
    public static final boolean DIG_REV = true;
    /** Dig-aware flow-field navigation, requested after FLOW_STALL turns without progress. */
    public static final boolean FLOW = true;
    /** Flow: FLOW_STALL turns without progress request a search; a path lives FLOW_TTL rounds; FLOW_RESERVE bytecodes
     *  are left by the end-of-turn work; a robot holding the path is waited out FLOW_HOLD turns; a lost rat rejoins the
     *  path within FLOW_REJOIN steps ahead; a forward search gives up after FLOW_MAX_TURNS turns; a failed target is not
     *  searched again for FLOW_RETRY rounds. */
    public static final int FLOW_STALL = 4, FLOW_TTL = 200, FLOW_RESERVE = 2500, FLOW_HOLD = 3, FLOW_REJOIN = 40, FLOW_MAX_TURNS = 12, FLOW_RETRY = 30;
    /** A step along a flow path counts as progress (resets Nav's stall), so a long detour does not end an errand. */
    public static final boolean FLOW_PROGRESS = true;
    /** Keep the last completed flow paths for reuse within FLOW_CACHE_TTL rounds. */
    public static final boolean FLOW_CACHE = false;
    public static final int FLOW_CACHE_TTL = 600;
    /** Flow search buckets as stacks: among tiles of equal f the deepest is expanded first. */
    public static final boolean FLOW_LIFO = false;
    /** Reverse flow search (target to rat): the rat walks on while it runs; it gives up after FLOW_REV_TURNS. */
    public static final boolean FLOW_REV = true;
    public static final int FLOW_REV_TURNS = 60;
    /** FLOW_REV's search with the neighbour loop unrolled. */
    public static final boolean FLOW_FAST = true;
    /** BitFlow keeps searching this many waves past the rat's tile. */
    public static final int BFLOW_EXTRA = 12;
    /** Path cost of a dirt tile (a dig is 5 cheese and 2.5 turns of action cooldown, plus the step). */
    public static final int DIG_COST = 4;
    /** Extra Flow reserve at the end of a baby's turn (RobotPlayer): frontier searches overshot FLOW_RESERVE alone. */
    public static final int FRONTIER_PAD = 1500;
    /** Loaded swap (0 = off): a baby with SWAP_LOADED+ cheese within SWAP_HOME of our nearest king, blocked on its next
     *  step by an allied baby carrying under 10, carries that ally past (Nav.swapPast). */
    public static final int SWAP_LOADED = 10, SWAP_HOME = 10;
    /** A rat held SWAP_HOLD turns behind an ally on its next step carries it past (0 = off). */
    public static final int SWAP_HOLD = 0;
    /** With SWAP_HOLD: no ally is carried past twice within SWAP_PAIR rounds by the same rat (0 = off). */
    public static final int SWAP_PAIR = 0;

    // ---- bytecode budget: per-turn gates against overruns

    /** Optional per-turn work (trap-risk observation) needs this many bytecodes left. */
    public static final int BC_OPTIONAL = 8000;
    /** Below this many bytecodes left after sensing, end the turn rather than overrun. */
    public static final int BC_BAIL = 3500;
    /** A turn the grab micro scored without acting ends under GM_BAIL bytecodes left (else BC_BAIL). */
    public static final int GM_BAIL = 6500;
    /** The re-sense after moving (Baby.sense2) needs SENSE2_LEFT bytecodes left; a baby's indicator string is built
     *  only with IND_LEFT left (0 = always). */
    public static final int SENSE2_LEFT = 6000, IND_LEFT = 1000;
    /** A king's indicator (two characters a mine) is built only with IND_LEFT_K left; a newborn's first turn ends
     *  after its move below NB_LATE_LEFT. */
    public static final int IND_LEFT_K = 2500, NB_LATE_LEFT = 1500;
    /** A newborn's first turn bails under NB_BAIL (not BC_BAIL): it already paid Sym.init and its allocations. */
    public static final int NB_BAIL = 6500;
    /** A newborn's first turn defers pickMine unless NB_PICK_PER bytecodes a known mine (4.1k with 14 mines) plus
     *  NB_PICK_PAD (the Nav.goTo after it) are left. */
    public static final int NB_PICK_PER = 320, NB_PICK_PAD = 1600;
}
