package httyd.model.skill;

import httyd.battle.DotType;
import httyd.model.GameRules;
import httyd.model.dragon.Stat;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds {@link MoveEffect} objects from the compact text used in the dragon database,
 * for example {@code BURN:6:2+ATK_DOWN:15}.
 */
public final class EffectFactory {

    private EffectFactory() {
    }

    /**
     * Parses a list of effects separated by {@code +}.
     *
     * @param spec the text; blank or {@code NONE} means no effects
     * @return the effects in order
     * @throws IllegalArgumentException if a token is unknown
     */
    public static List<MoveEffect> parseList(String spec) {
        List<MoveEffect> result = new ArrayList<>();
        if (spec == null || spec.isBlank() || spec.trim().equalsIgnoreCase("NONE")) {
            return result;
        }
        for (String token : spec.split("\\+")) {
            result.add(parseOne(token.trim()));
        }
        return result;
    }

    private static MoveEffect parseOne(String token) {
        String[] p = token.split(":");
        String code = p[0].toUpperCase();
        return switch (code) {
            case "PIERCE" -> new PierceEffect(num(p, 1, 0) / 100.0);
            case "CRIT" -> new CritEffect(true, 0);
            case "CRITBONUS" -> new CritEffect(false, num(p, 1, 0) / 100.0);
            case "HITS" -> new MultiHitEffect((int) num(p, 1, 1));
            case "SUREHIT" -> new SureHitEffect();
            case "BURN" -> new DotEffect(DotType.BURN, num(p, 1, 5), (int) num(p, 2, 2));
            case "POISON" -> new DotEffect(DotType.POISON, num(p, 1, 5), (int) num(p, 2, 2));
            case "ATK_DOWN" -> new StatDebuffEffect(Stat.ATK, num(p, 1, 15),
                    (int) num(p, 2, GameRules.DEFAULT_DEBUFF_TURNS));
            case "DEF_DOWN" -> new StatDebuffEffect(Stat.DEF, num(p, 1, 15),
                    (int) num(p, 2, GameRules.DEFAULT_DEBUFF_TURNS));
            case "SPD_DOWN" -> new StatDebuffEffect(Stat.SPD, num(p, 1, 15),
                    (int) num(p, 2, GameRules.DEFAULT_DEBUFF_TURNS));
            case "SELF_FLAT" -> new SelfBuffEffect(Stat.valueOf(p[1].toUpperCase()),
                    (int) num(p, 2, 0), 0, (int) num(p, 3, -1));
            case "SELF_PCT" -> new SelfBuffEffect(Stat.valueOf(p[1].toUpperCase()),
                    0, num(p, 2, 0), (int) num(p, 3, 2));
            case "HEAL" -> new HealSelfEffect(num(p, 1, 10));
            case "DODGE" -> new DodgeBuffEffect(num(p, 1, 10), (int) num(p, 2, 2));
            default -> throw new IllegalArgumentException("Unknown move effect: " + token);
        };
    }

    private static double num(String[] parts, int index, double fallback) {
        if (index >= parts.length || parts[index].isBlank()) {
            return fallback;
        }
        return Double.parseDouble(parts[index]);
    }
}
