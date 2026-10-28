package s0h;

/** Behaviour switches and constants. Every experiment adds a switch whose default is the incumbent. */
public final class Cfg {
    public static final int MAX_BABIES = 30, MAX_BABIES_WAR = 60;
    /** Cheese kept when spawning under attack. */
    public static final int WAR_FLOOR = 150;
    /** Squared distance within which a baby answers a king's alert. */
    public static final int ALERT_DIST2 = 225;
    /** Raw cheese at which a gatherer heads home. */
    public static final int DELIVER_AT = 30;
    /** Babies spawned before any fighter is assigned. */
    public static final int GATHER_FIRST = 6;
    /** One in FIGHT_EVERY later babies is a cat fighter. */
    public static final int FIGHT_EVERY = 2;
    public static final boolean CAT_TRAPS = true;
    /** Rush: babies with spawn index in [RUSH_FROM, RUSH_TO) attack the enemy king from birth. */
    public static final boolean RUSH = true;
    public static final int RUSH_FROM = 4, RUSH_TO = 16;
    /** Squared distance (to the cat's 2x2 centre, doubled coords /4) a gatherer keeps from cats. */
    public static final int GATHER_CAT_DIST = 20;
    public static final boolean DIG = true;
    public static final int DIG_MIN_CHEESE = 60;
    /** Babies spawned unconditionally (while 600 cheese remain) before the income test applies. */
    public static final int BOOT_BABIES = 8;
    /** Projected end-of-game cheese a spawn must leave. */
    public static final int SPAWN_MARGIN = 100;
    /** Fraction (percent) of measured income trusted when projecting forward. */
    public static final int INCOME_TRUST = 75;
    /** Rounds of income history in the rate estimate. */
    public static final int INCOME_WINDOW = 100;
    /** Projected surplus (cheese) a baby needs before placing a cat trap / biting with cheese. */
    public static final int TRAP_SURPLUS = 200, CHEESE_BITE_SURPLUS = 600;
    /** Cheese below which no trap is placed; cats within this squared distance of a king are trapped on the floor alone. */
    public static final int TRAP_FLOOR = 300, TRAP_DEFEND_DIST2 = 100;
}
