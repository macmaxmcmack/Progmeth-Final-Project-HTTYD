package httyd.model.skill;

/**
 * The three kinds of attack a dragon can use.
 */
public enum MoveKind {
    /** Plain physical attack, no element, unlimited use. */
    TYPELESS("Basic Attack"),
    /** Elemental attack that costs one shot. */
    SIGNATURE("Signature"),
    /** Powerful attack that needs a full charge. */
    ULTIMATE("Ultimate");

    private final String displayName;

    MoveKind(String displayName) {
        this.displayName = displayName;
    }

    /** @return the label shown on the command button */
    public String getDisplayName() {
        return displayName;
    }
}
