package httyd.model.dragon;

/**
 * The four numeric stats of a dragon that can be boosted or reduced.
 */
public enum Stat {
    HP("HP"),
    ATK("Attack"),
    DEF("Defense"),
    SPD("Speed");

    private final String displayName;

    Stat(String displayName) {
        this.displayName = displayName;
    }

    /** @return the label shown in the UI */
    public String getDisplayName() {
        return displayName;
    }
}
