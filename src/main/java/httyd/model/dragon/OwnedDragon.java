package httyd.model.dragon;

import httyd.model.GameRules;

/**
 * A dragon that belongs to the player: one species plus its own rolled stats (IV), nature,
 * star level and Alpha flag. It is the persistent part; the temporary battle state lives in
 * {@code httyd.battle.Combatant}.
 */
public final class OwnedDragon {
    /** Lowest possible stat deviation of a roll. */
    public static final int IV_MIN = -3;
    /** Highest possible stat deviation of a roll. */
    public static final int IV_MAX = 4;
    /** Bonus given to every stat of an Alpha dragon. */
    public static final int ALPHA_BONUS = 5;

    private final int id;
    private final DragonSpecies species;
    private final int ivHp;
    private final int ivAtk;
    private final int ivDef;
    private final int ivSpd;
    private final Nature nature;
    private final boolean alpha;
    private int star;

    /**
     * Creates an owned dragon.
     *
     * @param id      unique id within the collection
     * @param species the species
     * @param ivHp    HP deviation
     * @param ivAtk   ATK deviation
     * @param ivDef   DEF deviation
     * @param ivSpd   SPD deviation
     * @param nature  the nature
     * @param alpha   true for an Alpha dragon
     * @param star    star level from 1 to {@link GameRules#MAX_STAR}
     */
    public OwnedDragon(int id, DragonSpecies species, int ivHp, int ivAtk, int ivDef, int ivSpd,
                       Nature nature, boolean alpha, int star) {
        if (star < 1 || star > GameRules.MAX_STAR) {
            throw new IllegalArgumentException("star must be 1.." + GameRules.MAX_STAR);
        }
        this.id = id;
        this.species = species;
        this.ivHp = ivHp;
        this.ivAtk = ivAtk;
        this.ivDef = ivDef;
        this.ivSpd = ivSpd;
        this.nature = nature;
        this.alpha = alpha;
        this.star = star;
    }

    /** @return the unique id */
    public int getId() {
        return id;
    }

    /** @return the species */
    public DragonSpecies getSpecies() {
        return species;
    }

    /** @return the nature */
    public Nature getNature() {
        return nature;
    }

    /** @return true if this is an Alpha dragon */
    public boolean isAlpha() {
        return alpha;
    }

    /** @return the star level */
    public int getStar() {
        return star;
    }

    /** @return true if this dragon has the 3-star Titan Wing form */
    public boolean isTitanWing() {
        return star >= GameRules.MAX_STAR;
    }

    /**
     * Raises the star level by one (dupe fusion).
     *
     * @throws IllegalStateException if already at the maximum star level
     */
    public void starUp() {
        if (star >= GameRules.MAX_STAR) {
            throw new IllegalStateException("already max star");
        }
        star++;
    }

    /**
     * Returns the stat deviation of the roll.
     *
     * @param stat the stat
     * @return the deviation from -3 to +4
     */
    public int getIv(Stat stat) {
        return switch (stat) {
            case HP -> ivHp;
            case ATK -> ivAtk;
            case DEF -> ivDef;
            case SPD -> ivSpd;
        };
    }

    /** @return the grade derived from the four deviations */
    public Grade getGrade() {
        return Grade.fromTotalDeviation(ivHp + ivAtk + ivDef + ivSpd);
    }

    /**
     * Returns the final value of a stat shown in the collection (HP before the battle scale).
     *
     * @param stat the stat
     * @return the final stat value
     */
    public int getStat(Stat stat) {
        double value = species.getBase(stat) + getIv(stat) + (alpha ? ALPHA_BONUS : 0);
        value *= nature.multiplier(stat);
        if (stat == Stat.HP && star >= 2) {
            value *= 1.05;
        }
        return (int) Math.round(value);
    }

    /** @return the HP the dragon has in a battle */
    public int getBattleHp() {
        return getStat(Stat.HP) * GameRules.HP_SCALE;
    }

    /** @return the number of Signature shots including the 2-star bonus */
    public int getShotLimit() {
        return species.getShotLimit() + (star >= 2 ? 1 : 0);
    }

    /** @return the passive strength factor (1.5 for a Titan Wing) */
    public double getPassiveStrength() {
        return isTitanWing() ? 1.5 : 1.0;
    }

    /**
     * Serialises this dragon into one line of the save file.
     *
     * @return text such as {@code 3,toothless,2,-1,4,0,FIERCE,0,1}
     */
    public String toSaveString() {
        return String.join(",", String.valueOf(id), species.getId(), String.valueOf(ivHp),
                String.valueOf(ivAtk), String.valueOf(ivDef), String.valueOf(ivSpd),
                nature.name(), alpha ? "1" : "0", String.valueOf(star));
    }

    @Override
    public String toString() {
        return species.getShortName() + " #" + id;
    }
}
