package httyd.model.dragon;

/**
 * Quality grade of one rolled dragon, derived from how lucky its stat rolls (IV) were.
 */
public enum Grade {
    B("B", "Common"),
    A("A", "Good"),
    SSS("SSS", "Perfect");

    private final String label;
    private final String displayName;

    Grade(String label, String displayName) {
        this.label = label;
        this.displayName = displayName;
    }

    /** @return the grade label, e.g. "SSS" */
    public String getLabel() {
        return label;
    }

    /** @return the description of the grade */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Converts the sum of the four stat deviations into a grade.
     *
     * @param totalDeviation sum of the HP, ATK, DEF and SPD deviations
     * @return the grade
     */
    public static Grade fromTotalDeviation(int totalDeviation) {
        if (totalDeviation >= 10) {
            return SSS;
        }
        if (totalDeviation >= 1) {
            return A;
        }
        return B;
    }
}
