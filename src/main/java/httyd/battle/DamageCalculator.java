package httyd.battle;

import httyd.model.GameRules;

/**
 * Pure functions of the combat formulas. They contain no state so they are easy to unit test.
 */
public final class DamageCalculator {

    private DamageCalculator() {
    }

    /**
     * Formula A (diminishing return): {@code atk * 100 / (100 + def)}.
     *
     * @param attack  effective attack
     * @param defense effective defence
     * @return the damage before multipliers
     */
    public static double baseDamage(double attack, double defense) {
        return attack * (100.0 / (100.0 + Math.max(0, defense)));
    }

    /**
     * Dodge chance from the speed difference: {@code clamp((defSpd - atkSpd) / 4, 0, 25)} percent.
     *
     * @param attackerSpeed effective speed of the attacker
     * @param defenderSpeed effective speed of the defender
     * @return the chance in percent
     */
    public static double speedDodgePercent(double attackerSpeed, double defenderSpeed) {
        double raw = (defenderSpeed - attackerSpeed) / 4.0;
        return Math.max(0.0, Math.min(GameRules.MAX_SPEED_DODGE, raw));
    }

    /**
     * Adds the dodge bonuses to the speed dodge and applies the overall cap.
     *
     * @param speedDodge dodge from the speed formula, in percent
     * @param bonus      dodge bonus from passives and effects, in percent
     * @return the total chance in percent
     */
    public static double totalDodgePercent(double speedDodge, double bonus) {
        return Math.max(0.0, Math.min(GameRules.MAX_TOTAL_DODGE, speedDodge + bonus));
    }

    /**
     * Rounds a damage value; every hit that connects deals at least 1 damage.
     *
     * @param value the damage as a decimal
     * @return the whole damage, at least 1
     */
    public static int toDamage(double value) {
        return Math.max(1, (int) Math.round(value));
    }
}
