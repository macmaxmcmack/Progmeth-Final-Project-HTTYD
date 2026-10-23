package httyd.battle;

/** Damage-over-time statuses. */
public enum DotType {
    /** Fire damage. */
    BURN("Burning"),
    /** Venom damage. */
    POISON("Poisoned");

    private final String displayName;

    DotType(String displayName) {
        this.displayName = displayName;
    }

    /** @return the display label */
    public String getDisplayName() {
        return displayName;
    }
}
