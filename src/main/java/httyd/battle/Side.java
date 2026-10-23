package httyd.battle;

/** The two sides of a match. */
public enum Side {
    /** The human player. */
    PLAYER,
    /** The opponent (the bot). */
    OPPONENT;

    /** @return the other side */
    public Side other() {
        return this == PLAYER ? OPPONENT : PLAYER;
    }
}
