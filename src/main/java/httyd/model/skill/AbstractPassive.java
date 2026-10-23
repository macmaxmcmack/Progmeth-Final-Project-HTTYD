package httyd.model.skill;

/**
 * Base class of all passives. It stores the name, the description and the strength factor
 * (1.0 normally, 1.5 for a 3-star "Titan Wing" dragon).
 */
public abstract class AbstractPassive implements PassiveSkill {
    private final String name;
    private final String description;
    private final double strength;

    /**
     * @param name        English name
     * @param description description
     * @param strength    multiplier applied to the main number of the passive
     */
    protected AbstractPassive(String name, String description, double strength) {
        this.name = name;
        this.description = description;
        this.strength = strength;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    /** @return the strength factor of this passive */
    protected double getStrength() {
        return strength;
    }
}
