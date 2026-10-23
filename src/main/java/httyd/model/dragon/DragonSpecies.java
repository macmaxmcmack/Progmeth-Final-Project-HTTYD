package httyd.model.dragon;

import httyd.model.skill.Move;
import httyd.model.skill.MoveKind;
import httyd.model.skill.PassiveFactory;
import httyd.model.skill.PassiveSkill;

/**
 * Immutable template of a dragon species: base stats, class, moves and passive. Every owned
 * dragon is a rolled variation of one species.
 */
public final class DragonSpecies {
    private final String id;
    private final String name;
    private final DragonClass dragonClass;
    private final Tier tier;
    private final int baseHp;
    private final int baseAtk;
    private final int baseDef;
    private final int baseSpd;
    private final double critRate;
    private final int shotLimit;
    private final Move typeless;
    private final Move signature;
    private final Move ultimate;
    private final String passiveName;
    private final String passiveDescription;
    private final String passiveSpec;

    /**
     * Creates a species. See the dragon database for the meaning of each value.
     */
    public DragonSpecies(String id, String name, DragonClass dragonClass, Tier tier,
                         int baseHp, int baseAtk, int baseDef, int baseSpd,
                         double critRate, int shotLimit,
                         Move typeless, Move signature, Move ultimate,
                         String passiveName, String passiveDescription, String passiveSpec) {
        this.id = id;
        this.name = name;
        this.dragonClass = dragonClass;
        this.tier = tier;
        this.baseHp = baseHp;
        this.baseAtk = baseAtk;
        this.baseDef = baseDef;
        this.baseSpd = baseSpd;
        this.critRate = critRate;
        this.shotLimit = shotLimit;
        this.typeless = typeless;
        this.signature = signature;
        this.ultimate = ultimate;
        this.passiveName = passiveName;
        this.passiveDescription = passiveDescription;
        this.passiveSpec = passiveSpec;
    }

    /** @return the short unique id, e.g. "toothless" */
    public String getId() {
        return id;
    }

    /** @return the full English name */
    public String getName() {
        return name;
    }

    /** @return the short name without the species in brackets */
    public String getShortName() {
        int bracket = name.indexOf(" (");
        return bracket > 0 ? name.substring(0, bracket) : name;
    }

    /** @return the class of the dragon */
    public DragonClass getDragonClass() {
        return dragonClass;
    }

    /** @return the rarity tier */
    public Tier getTier() {
        return tier;
    }

    /**
     * Returns the base value of a stat before any roll.
     *
     * @param stat the stat
     * @return the base value
     */
    public int getBase(Stat stat) {
        return switch (stat) {
            case HP -> baseHp;
            case ATK -> baseAtk;
            case DEF -> baseDef;
            case SPD -> baseSpd;
        };
    }

    /** @return the base critical chance from 0 to 1 */
    public double getCritRate() {
        return critRate;
    }

    /** @return the number of Signature shots */
    public int getShotLimit() {
        return shotLimit;
    }

    /**
     * Returns the move of the given kind.
     *
     * @param kind the kind
     * @return the move
     */
    public Move getMove(MoveKind kind) {
        return switch (kind) {
            case TYPELESS -> typeless;
            case SIGNATURE -> signature;
            case ULTIMATE -> ultimate;
        };
    }

    /** @return the English name of the passive */
    public String getPassiveName() {
        return passiveName;
    }

    /** @return the description of the passive */
    public String getPassiveDescription() {
        return passiveDescription;
    }

    /**
     * Creates a fresh passive object for one battle.
     *
     * @param strength 1.0 normally, 1.5 for a 3-star dragon
     * @return a new passive
     */
    public PassiveSkill createPassive(double strength) {
        return PassiveFactory.create(passiveSpec, passiveName, passiveDescription, strength);
    }

    @Override
    public String toString() {
        return name;
    }
}
