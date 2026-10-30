package ratbot;

/** Behaviour switches and constants. Every experiment adds a switch whose default is the incumbent. */
public final class Cfg {
    public static final int MAX_BABIES = 18, MAX_BABIES_WAR = 18;
    /** With RICH_BANK global cheese or more the baby cap rises to RICH_BABIES (0 = off): a surplus buys bodies. */
    public static final int RICH_BABIES = 0, RICH_BANK = 1000;
    /** A second tier: with RICH2_BANK or more the cap rises to RICH2_BABIES (0 = off). u0m's set-2 superioristhmus
     *  games (3796/3797 vs outercloud) ended with 22-31k cheese banked and 40 rats (the RICH_BABIES cap) from r500 on,
     *  while they ran 53-66; abcdezyxwv runs 50-127 with ~2k banked. Babies eat nothing: only the spawn price. */
    public static final int RICH2_BABIES = 0, RICH2_BANK = 3000;
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
    public static final boolean KING_TRAPS = true;
    public static final int KING_TRAP_DIST2 = 20;
    /** Babies at war grab adjacent enemies and throw them; lay rat traps toward enemies within BABY_TRAP_DIST2. */
    public static final boolean NAP = true, BABY_RAT_TRAPS = true;
    /** Bite first the adjacent enemy rat that could grab us (healthier by at most one bite): it then cannot. */
    public static final boolean BITE_GUARD = false;
    /** Bite first an adjacent enemy rat that carries one of ours (on knifefight bestsofar threw 41 of our rats, most
     *  into our king). */
    public static final boolean BITE_CARRIER = false;
    public static final int BABY_TRAP_DIST2 = 13;
    /** Baby rat traps go on a front tile next to the nearest enemy rat (Chebyshev 1, inside the trap's 3x3, so its next
     *  step there fires it), adjacent enemies included. abcdezyxwv (3292 Meow): 36 of 50 traps laid next to one of our
     *  rats, 39 fired within a round, 98% hit; our rule skipped adjacent enemies and laid toward farther ones. */
    public static final boolean TRAP_ADJ = false;
    /** Cheese (global, or raw+global for babies) below which no rat trap is placed. */
    public static final int RAT_TRAP_MIN_CHEESE = 200;
    /** Most live rat traps we keep (engine max 25): new ones are bought only as old ones fire. */
    public static final int TRAP_CAP = 10;
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
    public static final boolean TRAP_SPAWN_FIRST = false;
    /** Keep enemies within two tiles inside our cone (anti-ratnap) instead of moving. */
    public static final boolean FACE_ENEMY = true;
    /** Scored combat movement (Micro) for babies at war. */
    /** War role split: 0 everyone fights, 1 gatherers ignore non-adjacent enemies, 2 and never freeze to face. */
    public static final int WAR_GATHER = 2;
    /** The post-move throw scan runs only with this many bytecodes left (it costs up to ~6000). */
    public static final int THROW_BC = 7000;
    /** Idle non-raiding fighters hold one of our mines instead of exploring. */
    public static final boolean FIGHT_MINE = true;
    /** Micro score bonus for a step onto cheese. */
    public static final int MICRO_CHEESE = 15;
    /** The king walks onto the nearest home-half mine within dist2 KING_MINE_R2 of its start. */
    public static final boolean KING_MINE = true;
    /** The king never steps to where its 3x3 plugs a passage (the 16 tiles around it split into two or more open
     *  arcs, walls, dirt and the map edge closed), unless where it stands is as bad, and steps out of one when it can.
     *  rose vs spsquared (1796): the king walked toward its home mine into the pocket's only exit at r~60 and sat
     *  there to r2000 with all 20 babies shut in behind it (75% of our baby turns went to mine trips that never got out). */
    public static final boolean KING_PLUG = false;
    public static final int KING_MINE_R2 = 400;
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
    public static final boolean KING_DODGE = false;
    /** Cats within this dist2 of the king's centre make it dodge. */
    public static final int KING_DODGE_R2 = 25;
    /** A dodging king boxed in by dirt digs out (KING_DIG_MIN cheese and up). */
    public static final boolean KING_DODGE_DIG = true;
    /** The king bites a cat in reach when its action is still free after spawning (plain bites, no cheese). Kings
     *  never bit cats: groveisbeingdesecrated (gr1, every side-A game): the band's cat pinned our king in the corner
     *  from r120 and scratched it dead at r190 with 90 HP of its 4000 left after our babies' bites. */
    public static final boolean KING_CAT_BITE = false;
    /** Cats walk a fixed waypoint loop and scratch whatever blocks it: the king marks every cat footprint it sees or
     *  hears squeaked as lane, its walks and digs never add lane to its 3x3, and it steps off lane when on it.
     *  bed (3091, 2086-2087): the king dug out of its start pocket into cat 3's lane at (1..2,11) and was scratched
     *  to death by r245; the field king stayed home. */
    public static final boolean CAT_LANE = false;
    /** With a cat near our king, empty rats on the ring around its 3x3 step off it: the king needs room to dodge. */
    public static final boolean YIELD_KING = false;
    public static final int KING_DIG_MIN = 100;
    /** With no enemy or cat in view the king walks to cheese within Chebyshev KING_ROAM of its mine (0 = off). */
    public static final int KING_ROAM = 4;
    /** A second king: the first king names a home-half mine as the site once it has FORM_BABIES babies and
     *  FORM_CHEESE global cheese after round FORM_FROM; babies within dist2 FORM_PULL_R2 gather in its 3x3. */
    public static final boolean FORM_KING = false;
    public static final int FORM_FROM = 80, FORM_BABIES = 16, FORM_CHEESE = 300, FORM_TTL = 60, FORM_RETRY = 150, FORM_PULL_R2 = 72;
    /** Kings we form up to (engine max 5 before round 1200). */
    public static final int FORM_MAX = 2;
    /** At war: kings we form up to (0 = FORM_MAX). After a backstab living kings weigh 0.5 of round-2000 points. */
    public static final int FORM_MAX_WAR = 0;
    /** Form only while the trusted income (x100 per round) beats every king's upkeep, the new one's included, by
     *  FORM_NET100 (Integer.MIN_VALUE = no income test). */
    public static final int FORM_NET100 = Integer.MIN_VALUE;
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
    public static final int FORM_JOIN = 0;
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
    public static final int END_KING = 0, END_KING_CHEESE = 400;
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
    public static final int WAR_STRIKE = 0;
    /** One in RAID_EVERY fighters is a raider: on our half it ignores enemies beyond dist2 RAID_ENGAGE. */
    public static final int RAID_EVERY = 0, RAID_ENGAGE = 8, RAID_HP = 60;
    /** Bytecodes the king's first-turn sense leaves for the rest of that turn (spawn first of all). 5000 can be short:
     *  on DefaultMedium an s7e king ended its first turn at 20001 of 20000 and missed the round-2 spawn (s7g: 7000). */
    public static final int KING_SENSE_RESERVE = 5000;
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
    public static final boolean AMMO_AWAY = false;
    public static final int AMMO_HP = 50, AMMO_R = 3, AMMO_D2 = 8;
    /** At war a baby within GUARD_KING_R (Chebyshev) of our king targets, ahead of its own nearest enemy, the visible
     *  enemy rat nearest it among those within Chebyshev 2 of the king's centre (in reach to bite it), skips the
     *  standoff against it and bites such rats first (0 = off). 2239 tiny: in r16-48 our king fell 540 -> 32 to 43
     *  bites with 16-19 of our rats near it against 8-14 of theirs; ours held standoffs with their own nearest
     *  enemies and ended 288 of 624 turns with the action unused. */
    public static final int GUARD_KING_R = 0;
    /** Fortress radius (Chebyshev, from the king) fighters hold at war; 0 = off. */
    public static final int FORT_R = 0;
    /** King rat traps laid in advance at war (no enemy in sight), kept to this many. */
    public static final int FORT_TRAPS = 10;
    /** Avoid tiles where an enemy rat could have laid a trap (TrapRisk). */
    public static final boolean TRAP_RISK = true;
    /** Squeak enemy positions/facings so nearby allies share TrapRisk marks. */
    public static final boolean TRAP_SHARE = true;
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
    /** Bite cheese (c = k*k gives +k damage), only when it makes the bite a kill: a king pays up to KING_BITE_MAX
     *  above KING_BITE_FLOOR, a baby up to FINISH_K*FINISH_K above FINISH_FLOOR. */
    public static final int KING_BITE_MAX = 100, KING_BITE_FLOOR = 300, FINISH_K = 5, FINISH_FLOOR = 200;
    /** Flank: step onto a tile next to an enemy rat that it cannot see, facing it, then grab it after moving. */
    public static final boolean FLANK = true;
    /** Rescue: an enemy baby carrying one of ours within dist2 RESCUE_D2 comes first. We keep our action for it (no
     *  bite or trap before moving), step to a tile next to it that it cannot see (any rat may grab an enemy that
     *  cannot see it, and a grabbed carrier drops what it holds) and grab it. s7d's 126 field games: enemy
     *  carriers held ours 137-244 rounds a game, with a free rat of ours within Chebyshev 2 of the carrier on 55-64%
     *  of them but behind it on 5-9%; AlexT101 and abcdezyxwv threw ours into our king 5-7 times a game for 36-41. */
    public static final boolean RESCUE = false;
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
    public static final int THROW_FAR_PCT = 40, THROW_MIN = 42, THROW_HOLD = 6;
    /** A carrier of an enemy rat steps (or turns) to the throw whose point-blank tile holds an enemy robot worth at least
     *  AIM_MIN (84 a baby, 126 a king) and throws in the same turn when it can: our throws hit enemy robots 11 times to
     *  abcdezyxwv's 57 (r <= 400, 4 matches), because our carriers never lined a throw up. */
    public static final boolean CARRY_AIM = false;
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
    public static final boolean MINE_BALANCE = false;
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
    /** Global cheese below which any rat carrying cheese delivers it before fighting or running errands (0 = off):
     *  31 of our 45 king deaths on the replica were starvation, once with 728 cheese in our rats' paws. */
    public static final int HUNGRY = 0;
    /** Rounds a cheese pile given up after a stall stays ignored, with every pile within Chebyshev 2 of it (0 = off).
     *  EscapeTheNight: five babies in the king's room chased a pile 3 tiles away through a wall for 110 rounds, each
     *  re-picking it the turn after giving up, while their 19-tile path out was held by one another. */
    public static final int CHEESE_BAN = 0;
    /** CHEESE_BAN only piles behind a wall: the last flow path toward the pile was a detour (longer than
     *  2 * Chebyshev distance + 2) or its search failed. A stall in a crowd at a mine then bans nothing. */
    public static final boolean CHEESE_BAN_WALL = false;
    /** A cheese pile whose greedy line from us (directionTo steps) crosses a known wall is not chased; a walled-off
     *  target is dropped. Stash (1210, 1211 vs abcdezyxwv): a wall row seals our base but for dug corners; our
     *  rats saw the piles round the mines through it and chased them into the wall ('C'), each pick resetting the
     *  stall that would start a flow search to the mine, so they made 9 pickups to r100 (they 52) and none r100-200. */
    public static final boolean CHEESE_LOS = false;
    /** Breakout (0 = off): a baby that gives up two chases of walled-off cheese within 100 rounds, or whose explore
     *  search fails, spends BREAKOUT_TTL rounds with CHEESE_LOS on and exploring toward the never-seen tile nearest by
     *  path cost (Flow's frontier search; dirt on the way is dug). EscapeTheNight 2233 and local etn1: the king's
     *  room opens only through 12 dirt tiles; all 20 babies chased piles seen through its wall ('C' 52-85%) and
     *  explore searches toward the map centre timed out (25 turns), so the team dug 3 tiles in 1000 rounds. */
    public static final int BREAKOUT_TTL = 0;
    /** A goTo target within Chebyshev NAV_SAME_R of the last one keeps the stall count and bug state (0 = only an
     *  equal tile does). fullofsecrets: carriers with up to 490 cheese sat 76 rounds outside the wall of the king's
     *  pocket; each step of the roaming king was a new target, so the stall never reached FLOW_STALL. */
    public static final int NAV_SAME_R = 0;
    /** A gatherer counts as at its mine within Chebyshev MINE_KING_R of it when one of our kings sits within 2 of the
     *  mine (0 = off: always 2), and wanders only to tiles off the kings' bodies. FreeTheCheese: the king's 3x3 covered
     *  the mine, the free tiles within 2 were taken, and a gatherer pathed into the king for 60 rounds. */
    public static final int MINE_KING_R = 0;
    /** A delivery that has not got closer (Chebyshev) to the nearest king for FLOW_STALL turns walks back along the
     *  rat's own trail of walked tiles from its spawn instead (Trail). Flow paths to a pocketed king were rarely of
     *  use: on fullofsecrets 131 of 490 searches failed, and no path was followed to its end in 3 games. */
    public static final boolean TRAIL = false;
    /** A rat already carrying cheese takes no pile that would lift it above this (0 = no cap). */
    public static final int CARRY_MAX = 0;
    /** An attacker takes no pile that would lift it above this, empty-handed or not (0 = off). Attackers run the
     *  after-move pickUp but never deliver. s9s's 126 field games from r400: attack-move turns carried 43-82 cheese
     *  on average and 10-33% of them 100+, and those moved on 31-39% of turns (1% slower per unit carried). */
    public static final int ATK_CARRY = 0;
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
    public static final boolean CAT_LAY = false;
    /** The cat trap of an approaching cat (tryCatTrap) may also go on any tile beside the cat's body, which the cat sets
     *  off on its next step, and during cooperation needs only TRAP_FLOOR, not the projected surplus. spsquared on rose
     *  (3266): when a cat reached their king, 5-6 rats ringed it with cat traps and six fired at r479 alone (63 of 73
     *  fired; cat damage 8080 to our 0). */
    public static final boolean CAT_RING = false;
    public static final int CAT_LAY_GAP = 20, CAT_LAY_AGE = 3, CAT_LAY_FLOOR = 400;
    /** Rush: babies with spawn index in [RUSH_FROM, RUSH_TO) attack the enemy king from birth. */
    public static final boolean RUSH = false;
    public static final int RUSH_FROM = 4, RUSH_TO = 16;
    /** An attacker keeps the mirror spot it walks to until that spot is seen empty, not the nearest live one each turn.
     *  groveisbeingdesecrated (gr1): the rushers stood at the band's wall midway between (7,2) and (52,2) and flipped
     *  targets 14-21 times by r190; every flip restarted their flow search, which never finished. */
    public static final boolean RUSH_STICK = false;
    /** Rush spawns stop at this round (0 = never): the spawn index is the live-baby count, so without it every
     *  replacement born with 4-15 babies alive is an attacker all game (rsu1: s8u lost both DefaultSmall games to s7d
     *  in a long war, 7 rats to 13-15 at r150). */
    public static final int RUSH_UNTIL = 0;
    /** An attacker older than RUSH_LIFE rounds that has not seen the enemy king in the last RUSH_LIFE rounds turns
     *  gatherer (0 = off). s9s's field games: on maps where the rush cannot reach the enemy king its attackers walk
     *  all game (uneruesansfin vs AlexT101: 60% of our baby turns to r400 were attack moves, 60 cheese picked in 400
     *  rounds, war opened by them at r472; where_are_you 41%, rose 44-52%). */
    public static final int RUSH_LIFE = 0;
    /** Rush attackers bite and grab enemy rats during cooperation too (the first bite ends it for everyone). */
    public static final boolean RUSH_BREAK = false;
    /** From this round on (0 = never) every baby bites and grabs enemy rats during cooperation too: the field rushers
     *  open the war at r36-40 (abrk, whose attackers must first walk over, at r66). */
    public static final int BREAK_ROUND = 0;
    /** Gatherers spread over every known mine, not only those on our half: the field rushers pick 600-700 cheese a game
     *  on our half in rounds 100-400 (abrk 156). */
    public static final boolean MINE_ANY = false;
    /** MINE_ANY during cooperation only: gatherers pick among every known mine, and drop an enemy-half mine once the
     *  war starts. With MINE_MIRROR the king lists each listed mine's twin on either half. Replica, s6m's blocks: by r200
     *  the mines had produced 5199 cheese a game and both teams together had picked up 2154; through r300 mines ranked
     *  6th and beyond from our king gave us 0-11% of their spawn (DefaultLarge (32,54): 760 of 760 lying). */
    public static final boolean MINE_ANY_COOP = false;
    /** Squared distance (to the cat's 2x2 centre, doubled coords /4) a gatherer keeps from cats. */
    public static final int GATHER_CAT_DIST = 20;
    /** A cat within dist2 CAT_DEFEND_R2 of one of our kings draws every rat that sees it, gatherers included
     *  (0 = off): attack_on_cat, a lone cat scratched our king to death in 113 rounds while 17 gatherers kept away. */
    public static final int CAT_DEFEND_R2 = 0;
    public static final boolean DIG = true;
    /** digToward before flow: one-tile dirt bands (cheesefarm's diagonal stripes) are dug through at once. */
    public static final boolean DIG_FIRST = true;
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
    public static final boolean FLOW_PROGRESS = false;
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
    public static final boolean FLOW_REV = false;
    public static final int FLOW_REV_TURNS = 25;
    /** FLOW_REV's search with the neighbour loop unrolled (621 bytecodes a node measured for the loop). */
    public static final boolean FLOW_FAST = false;
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
    public static final boolean FLOW_ALLOC = false;
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
    public static final boolean KING_FLOW = false;
    public static final int FLOW_JAM_HOLD = 12;
    /** Path cost of a dirt tile (a dig is 5 cheese and 2.5 turns of action cooldown, plus the step). */
    public static final int DIG_COST = 4;
    /** Random samples per explore target, taking the first never-seen tile. */
    public static final int EXPLORE_TRIES = 12, EXPLORE_CENTER_FROM = 0;
    /** How a scout or seeker picks its next never-seen tile of our half from the EXPLORE_TRIES samples: 0 the nearest
     *  to it, 1 the first (uniform over what is unseen), 2 the farthest. Nearest crawls outward from the king, and each
     *  new scout crawls the same ground again (where_are_you: 5 of our half's 8 mines never seen in 1400 rounds). */
    public static final int HOME_PICK = 0;
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
    public static final boolean SYM_GOSSIP = false;
    public static final int SYM_GOSSIP_EVERY = 3;
    /** The team mine list is symmetry evidence: a listed mine whose s-twin a robot saw without a mine rules out s
     *  (twins not seen yet are marked and checked when first seen). The king also rules out s when a listed mine's
     *  s-twin would sit closer than the engine's minimum mine spacing (d2 25) to a listed mine. */
    public static final boolean SYM_MINELIST = false;
    /** A mine as near the enemy start as ours (on a HOR/VER axis: 7 of EscapeTheNight's 11) counts as ours for the
     *  gatherers' pick and the king's home mine, instead of as theirs. */
    public static final boolean MINE_TIES = false;
    /** Scouts: one in SCOUT_EVERY gatherers explores our half (never-seen tiles) until round SCOUT_UNTIL, then
     *  gathers (0 = off). */
    public static final int SCOUT_EVERY = 4, SCOUT_UNTIL = 150;
    /** Scouts explore by Flow's frontier search (the never-seen tile nearest by path cost, digging dirt on the way)
     *  before exploreHome's random never-seen home tile, and chase only piles in line of sight (CHEESE_LOS); with
     *  SCOUT_MINES > 0 they go on scouting until SCOUT_MINES_UNTIL while the team knows fewer than SCOUT_MINES home
     *  mines. hv1 (pb2 self-play, r1-500): no rat of ours ever saw 5 of 8 home mines on where_are_you, 4 of 6 on
     *  attack_on_cat, 3 of 4 on rose (all three behind 5 dirt tiles; (24,8) is 8 tiles from our start) and 5 of 8
     *  on seepastyourgreed; own-half shares 0.04-0.38. */
    public static final boolean SCOUT_FRONTIER = false;
    public static final int SCOUT_MINES = 0, SCOUT_MINES_UNTIL = 600;
    /** SCOUT_MINES scouting also stops once the team's known home mines have not grown for SCOUT_DRY rounds (counted
     *  from SCOUT_UNTIL; 0 = off). hb1, s9c (SCOUT_MINES 4) vs s7d: knifefight, cheesebottles and Stash hold 3 home
     *  mines each, so a quarter of s9c's gatherers scouted to r600 there; it delivered 935-1045 to 1679-1810 on
     *  knifefight and 3910 to 8404 on cheesebottles B. */
    public static final int SCOUT_DRY = 0;
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
    public static final boolean SYM_SLOT = false;
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
    public static final int SEEK_CROWD = 0, SEEK_TTL = 150, SEEK_RETRY = 200;
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
     *  3x3 bodies would overlap), not only onto itself: uneasy_alliance's kings start 7 apart on one row. */
    public static final boolean SYM_KING_GAP = false;
    public static final int DIG_MIN_CHEESE = 60;
    /** Live babies kept unconditionally (while CORE_FLOOR cheese remain) before the income test applies. */
    public static final int CORE_BABIES = 18, CORE_FLOOR = 300;
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
    public static final int SENSE2_LEFT = 4000, IND_LEFT = 0;
}
