package s3y;

/** Behaviour switches and constants. Every experiment adds a switch whose default is the incumbent. */
public final class Cfg {
    public static final int MAX_BABIES = 18, MAX_BABIES_WAR = 18;
    /** With RICH_BANK global cheese or more the baby cap rises to RICH_BABIES (0 = off): a surplus buys bodies. */
    public static final int RICH_BABIES = 0, RICH_BANK = 1000;
    /** Cheese kept when spawning under attack. */
    public static final int WAR_FLOOR = 300;
    /** Hard floor under every spawn (king upkeep). */
    public static final int SPAWN_FLOOR = 300;
    /** With fewer than LOW_BABIES babies every floor drops to LOW_FLOOR: a king without rats has no income. */
    public static final int LOW_BABIES = 8, LOW_FLOOR = 100;
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
    /** Cheese (global, or raw+global for babies) below which no rat trap is placed. */
    public static final int RAT_TRAP_MIN_CHEESE = 200;
    /** Most live rat traps we keep (engine max 25): new ones are bought only as old ones fire. */
    public static final int TRAP_CAP = 10;
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
    public static final int KING_MINE_R2 = 400;
    /** Dirt on the king's walk to its mine: the king digs the step's leading edge (5 cheese a tile). */
    public static final boolean KING_DIG = true;
    /** King steps out of a near cat's path (King.dodge) instead of only straight away from it. */
    public static final boolean KING_DODGE = false;
    /** Cats within this dist2 of the king's centre make it dodge. */
    public static final int KING_DODGE_R2 = 25;
    /** A dodging king boxed in by dirt digs out (KING_DIG_MIN cheese and up). */
    public static final boolean KING_DODGE_DIG = true;
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
    /** Gatherers at their mine turn in place instead of wandering. */
    public static final boolean GATHER_STILL = false;
    public static final boolean RAID = true;   // idle fighters go to enemy-half mines
    public static final int RAID_STAY = 60, RAID_SPREAD = 60;
    /** One in RAID_EVERY fighters is a raider: on our half it ignores enemies beyond dist2 RAID_ENGAGE. */
    public static final int RAID_EVERY = 0, RAID_ENGAGE = 8, RAID_HP = 60;
    /** Bytecodes the king's first-turn sense leaves for the rest of that turn (spawn first of all). */
    public static final int KING_SENSE_RESERVE = 5000;
    /** King press: at war a rat that sees the enemy king squeaks it (allies relay it PRESS_HOPS times); a rat with
     *  PRESS_HP health or more walks to a king sighting at most PRESS_AGE rounds old within PRESS_D2 (gatherers
     *  carrying less than DELIVER_AT: PRESS_GATHER_D2) and bites it, ahead of standoffs and facing. */
    public static final boolean KING_PRESS = false;
    public static final int PRESS_HOPS = 2, PRESS_HP = 40, PRESS_AGE = 8, PRESS_D2 = 100, PRESS_GATHER_D2 = 20;
    /** Fighters target the nearest enemy rat with less health than theirs (it can be ratnapped). */
    public static final boolean POUNCE = true;
    /** Health at or below which a baby near enemies retreats to its king (0 = never). */
    public static final int RETREAT_HP = 40;
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
    /** Guard facing: with an enemy rat within this squared distance, steps strafe so we keep facing it, and a
     *  turn ends facing it (rats are grabbed from behind). 0 = off. */
    public static final int GUARD_DIST2 = 13;
    /** Guard also faces enemies seen at the start of the turn or squeaked by allies when none is in view. */
    public static final boolean GUARD_MEMORY = true;
    /** Bite cheese (c = k*k gives +k damage), only when it makes the bite a kill: a king pays up to KING_BITE_MAX
     *  above KING_BITE_FLOOR, a baby up to FINISH_K*FINISH_K above FINISH_FLOOR. */
    public static final int KING_BITE_MAX = 100, KING_BITE_FLOOR = 300, FINISH_K = 5, FINISH_FLOOR = 200;
    /** Flank: step onto a tile next to an enemy rat that it cannot see, facing it, then grab it after moving. */
    public static final boolean FLANK = true;
    /** Standoff: do not step toward an enemy rat that sees us and is not weaker; face it and hold instead. */
    public static final boolean STANDOFF = true;
    /** Throws: an enemy baby hit beyond tile 2 counts THROW_FAR_PCT percent; below a THROW_MIN score (42 = a
     *  point-blank wall or rat) the carrier holds for up to THROW_HOLD rounds after the grab. */
    public static final int THROW_FAR_PCT = 40, THROW_MIN = 42, THROW_HOLD = 6;
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
    /** Raw cheese at which a gatherer heads home. */
    public static final int DELIVER_AT = 30;
    /** Global cheese below which any rat carrying cheese delivers it before fighting or running errands (0 = off):
     *  31 of our 45 king deaths on the replica were starvation, once with 728 cheese in our rats' paws. */
    public static final int HUNGRY = 0;
    /** A rat already carrying cheese takes no pile that would lift it above this (0 = no cap). */
    public static final int CARRY_MAX = 0;
    /** Babies spawned before any fighter is assigned. */
    public static final int GATHER_FIRST = 6;
    /** One in FIGHT_EVERY later babies is a cat fighter. */
    public static final int FIGHT_EVERY = 2;
    public static final boolean CAT_TRAPS = true;
    /** Rush: babies with spawn index in [RUSH_FROM, RUSH_TO) attack the enemy king from birth. */
    public static final boolean RUSH = false;
    public static final int RUSH_FROM = 4, RUSH_TO = 16;
    /** Squared distance (to the cat's 2x2 centre, doubled coords /4) a gatherer keeps from cats. */
    public static final int GATHER_CAT_DIST = 20;
    public static final boolean DIG = true;
    /** digToward before flow: one-tile dirt bands (cheesefarm's diagonal stripes) are dug through at once. */
    public static final boolean DIG_FIRST = true;
    /** Dig-aware flow-field navigation, requested after FLOW_STALL turns without progress. */
    public static final boolean FLOW = true;
    public static final int FLOW_STALL = 4, FLOW_TTL = 200, FLOW_RESERVE = 2500, FLOW_HOLD = 3, FLOW_REJOIN = 40, FLOW_MAX_TURNS = 12, FLOW_RETRY = 30;
    /** Path cost of a dirt tile (a dig is 5 cheese and 2.5 turns of action cooldown, plus the step). */
    public static final int DIG_COST = 4;
    /** Random samples per explore target, taking the first never-seen tile. */
    public static final int EXPLORE_TRIES = 12, EXPLORE_CENTER_FROM = 0;
    /** A rat that finds the first mine walks it home to the king. */
    public static final boolean COURIER = true;
    /** Babies relay mine squeaks to each other while the team knows no mine. */
    public static final boolean GOSSIP = true;
    /** Gossip all game: a rat away from the king squeaks a mine it knows that the king's list lacks, and every rat
     *  notes the mines it hears, so news reaches the king with the next delivery instead of its finder. */
    public static final boolean GOSSIP_ALWAYS = true;
    /** Scouts: one in SCOUT_EVERY gatherers explores our half (never-seen tiles) until round SCOUT_UNTIL, then
     *  gathers (0 = off). */
    public static final int SCOUT_EVERY = 4, SCOUT_UNTIL = 150;
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
}
