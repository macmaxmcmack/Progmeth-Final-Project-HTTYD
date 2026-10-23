package httyd.model;

/**
 * Central place for the tunable numbers of the game. Keeping them in one class makes the
 * balance easy to change and easy to explain in the report.
 */
public final class GameRules {

    /** In-game HP = base HP (60-99) multiplied by this value. */
    public static final int HP_SCALE = 3;
    /** Critical hit damage multiplier. */
    public static final double CRIT_MULTIPLIER = 1.5;
    /** Maximum dodge chance coming from the speed difference formula, in percent. */
    public static final double MAX_SPEED_DODGE = 25.0;
    /** Hard cap of the total dodge chance including bonuses, in percent. */
    public static final double MAX_TOTAL_DODGE = 40.0;
    /** Power multipliers of the three attack types. */
    public static final double TYPELESS_POWER = 1.0;
    /** Default power of a Signature move that has no status effect. */
    public static final double SIGNATURE_POWER_PLAIN = 1.4;
    /** Default power of a Signature move that also applies an effect. */
    public static final double SIGNATURE_POWER_EFFECT = 1.2;
    /** Default power of an Ultimate move. */
    public static final double ULTIMATE_POWER = 2.0;
    /** Attack attempts (and hits taken) needed before the Ultimate is ready. */
    public static final int ULTIMATE_CHARGE_NEEDED = 4;
    /** Shots restored when a dragon rests for one turn. */
    public static final int REST_RELOAD_SHOTS = 2;
    /** Default duration of a stat debuff, counted in the victim's own turns. */
    public static final int DEFAULT_DEBUFF_TURNS = 3;
    /** Number of dragons in a deck. */
    public static final int DECK_SIZE = 3;
    /** Maximum number of trainer items per match. */
    public static final int BAG_SIZE = 2;
    /** Seconds allowed for a single turn. */
    public static final int TURN_SECONDS = 60;
    /** Seconds allowed for all the turns of one player. */
    public static final int TOTAL_SECONDS = 240;
    /** After this many actions the match is decided by remaining HP. */
    public static final int MAX_ACTIONS = 140;
    /** Number of turns an Ultimate keeps the terrain shifted. */
    public static final int TERRAIN_SHIFT_TURNS = 2;
    /** Price of one dragon egg in coins. */
    public static final int EGG_PRICE = 50;
    /** The most coins a player can hold. */
    public static final int MAX_COINS = 999_999;
    /** Chance of an Alpha (shiny) dragon, in percent. */
    public static final int ALPHA_CHANCE_PERCENT = 5;
    /** Highest star level of a dragon. */
    public static final int MAX_STAR = 3;

    private GameRules() {
    }
}
