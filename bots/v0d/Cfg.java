package v0d;

/** Behaviour switches and constants. Every experiment adds a switch whose default is the incumbent. */
public final class Cfg {
    public static final int MAX_BABIES = 30;
    /** Raw cheese at which a gatherer heads home. */
    public static final int DELIVER_AT = 30;
    /** Babies spawned before any fighter is assigned. */
    public static final int GATHER_FIRST = 6;
    /** One in FIGHT_EVERY later babies is a cat fighter. */
    public static final int FIGHT_EVERY = 2;
    public static final boolean CAT_TRAPS = true;
    /** Global cheese kept back from cat traps. */
    public static final int TRAP_RESERVE = 300;
    /** Squared distance (to the cat's 2x2 centre, doubled coords /4) a gatherer keeps from cats. */
    public static final int GATHER_CAT_DIST = 20;
    public static final boolean DIG = true;
    public static final int DIG_MIN_CHEESE = 60;
    /** Rounds of king upkeep the spawner never spends. */
    public static final int RESERVE_ROUNDS = 250;
}
