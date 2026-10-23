package httyd.model.dragon;

/**
 * A personality trait that nudges the stats of a dragon by a few percent.
 */
public enum Nature {
    FIERCE("Fierce", 0.05, -0.05, 0, 0, "+Atk / -Def"),
    STALWART("Stalwart", -0.05, 0.05, 0, 0, "+Def / -Atk"),
    AGILE("Agile", 0, 0, 0.05, -0.05, "+Spd / -HP"),
    HARDY("Hardy", 0, 0, -0.05, 0.05, "+HP / -Spd"),
    ALPHA_SPIRIT("Alpha Spirit", 0.04, 0.04, 0.04, 0.04, "All stats +4%"),
    CALM("Calm", 0, 0, 0, 0, "No effect");

    private final String displayName;
    private final double atk;
    private final double def;
    private final double spd;
    private final double hp;
    private final String summary;

    Nature(String displayName, double atk, double def, double spd, double hp, String summary) {
        this.displayName = displayName;
        this.atk = atk;
        this.def = def;
        this.spd = spd;
        this.hp = hp;
        this.summary = summary;
    }

    /** @return the display name of this nature */
    public String getDisplayName() {
        return displayName;
    }

    /** @return a short description of the effect, e.g. "+Atk / -Def" */
    public String getSummary() {
        return summary;
    }

    /**
     * Returns the multiplier this nature applies to a stat.
     *
     * @param stat the stat
     * @return a multiplier such as 1.05 or 0.95
     */
    public double multiplier(Stat stat) {
        return 1.0 + switch (stat) {
            case ATK -> atk;
            case DEF -> def;
            case SPD -> spd;
            case HP -> hp;
        };
    }
}
