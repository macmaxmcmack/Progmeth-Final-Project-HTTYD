package httyd.model.dragon;

/**
 * Rarity tier of a dragon species. It decides how often the species comes out of an egg.
 */
public enum Tier {
    C("C", 45),
    B("B", 30),
    A("A", 18),
    S("S", 7);

    private final String label;
    private final int dropWeight;

    Tier(String label, int dropWeight) {
        this.label = label;
        this.dropWeight = dropWeight;
    }

    /** @return the tier label such as "S" */
    public String getLabel() {
        return label;
    }

    /** @return relative chance weight used when opening an egg */
    public int getDropWeight() {
        return dropWeight;
    }
}
