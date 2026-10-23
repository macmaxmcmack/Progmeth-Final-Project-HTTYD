package httyd.model.skill;

import httyd.model.dragon.Stat;

/**
 * Creates a fresh {@link PassiveSkill} object for one battle from the compact specification in
 * the dragon database, e.g. {@code REFLECT_DMG:15}. Passives keep their own counters, so every
 * dragon in every battle must get its own instance.
 */
public final class PassiveFactory {

    private PassiveFactory() {
    }

    /**
     * Builds a passive.
     *
     * @param spec        the specification text
     * @param name        English name
     * @param description description
     * @param strength    1.0 normally, 1.5 for a 3-star dragon
     * @return a new passive instance
     * @throws IllegalArgumentException if the code is unknown
     */
    public static PassiveSkill create(String spec, String name, String description, double strength) {
        if (spec == null || spec.isBlank() || spec.equalsIgnoreCase("NONE")) {
            return new Passives.NoPassive();
        }
        String[] p = spec.split(":");
        String n = name;
        String d = description;
        double s = strength;
        return switch (p[0].toUpperCase()) {
            case "DODGE" -> new Passives.DodgeUp(n, d, s, num(p, 1), (int) num(p, 2));
            case "REFLECT_DMG" -> new Passives.ReflectDamage(n, d, s, num(p, 1));
            case "REFLECT_ATK" -> new Passives.ReflectAttack(n, d, s, num(p, 1));
            case "CONDUCTOR" -> new Passives.Conductor(n, d, s, (int) num(p, 1));
            case "CRIT_RESIST" -> new Passives.CritResist(n, d, s, num(p, 1));
            case "AURA" -> new Passives.Aura(n, d, s, Stat.valueOf(p[1].toUpperCase()), num(p, 2));
            case "SPARKS" -> new Passives.SparksJet(n, d, s, (int) num(p, 1), (int) num(p, 2));
            case "DEF_STACK" -> new Passives.DefStack(n, d, s, (int) num(p, 1), (int) num(p, 2));
            case "REDUCE_ALL" -> new Passives.DamageReduction(n, d, s, num(p, 1));
            case "LAZY" -> new Passives.LazySlumber(n, d, s, num(p, 1));
            case "NEVER_DODGED" -> new Passives.NeverDodged(n, d, s);
            case "LOWHP_DEF" -> new Passives.LowHpDefense(n, d, s, (int) num(p, 1), num(p, 2));
            case "FASTER_ATK" -> new Passives.FasterAttack(n, d, s, (int) num(p, 1));
            case "TYPELESS_CRIT" -> new Passives.TypelessCrit(n, d, s, num(p, 1));
            case "IMMUNE" -> new Passives.DebuffImmunity(n, d, s, Stat.valueOf(p[1].toUpperCase()));
            case "POISON_SIG" -> new Passives.PoisonSignature(n, d, s, num(p, 1), (int) num(p, 2));
            case "FLAT" -> new Passives.FlatBonus(n, d, s, Stat.valueOf(p[1].toUpperCase()), (int) num(p, 2));
            case "DMG_UP" -> new Passives.DamageUp(n, d, s, num(p, 1));
            case "SURVIVE" -> new Passives.SurviveOnce(n, d, s);
            default -> throw new IllegalArgumentException("Unknown passive: " + spec);
        };
    }

    private static double num(String[] parts, int index) {
        return index < parts.length ? Double.parseDouble(parts[index]) : 0;
    }
}
