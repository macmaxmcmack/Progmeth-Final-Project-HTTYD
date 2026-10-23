package httyd.battle;

import httyd.model.dragon.Stat;

/**
 * A temporary or permanent change to one stat. The change is a percentage and/or a flat value.
 */
public final class StatModifier {
    private final Stat stat;
    private final double percent;
    private final int flat;
    private int turnsLeft;

    /**
     * @param stat      the stat
     * @param percent   fraction such as -0.25 or 0.2
     * @param flat      flat amount
     * @param turnsLeft own turns left, or -1 for permanent
     */
    public StatModifier(Stat stat, double percent, int flat, int turnsLeft) {
        this.stat = stat;
        this.percent = percent;
        this.flat = flat;
        this.turnsLeft = turnsLeft;
    }

    /** @return the stat */
    public Stat getStat() {
        return stat;
    }

    /** @return the percentage fraction */
    public double getPercent() {
        return percent;
    }

    /** @return the flat amount */
    public int getFlat() {
        return flat;
    }

    /** @return remaining own turns, -1 if permanent */
    public int getTurnsLeft() {
        return turnsLeft;
    }

    /** @return true if the modifier is permanent */
    public boolean isPermanent() {
        return turnsLeft < 0;
    }

    /** @return true if this modifier lowers the stat */
    public boolean isDebuff() {
        return percent < 0 || flat < 0;
    }

    /**
     * Counts down one turn.
     *
     * @return true if the modifier has expired
     */
    boolean tick() {
        if (turnsLeft < 0) {
            return false;
        }
        turnsLeft--;
        return turnsLeft <= 0;
    }
}
