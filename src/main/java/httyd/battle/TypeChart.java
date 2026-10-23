package httyd.battle;

import httyd.model.dragon.DragonClass;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * The elemental advantage table. Every class beats two classes, loses to two classes and is
 * neutral against the rest, so there is no strongest class. An elemental move that hits a
 * weakness deals 25% more damage; one that hits a resistance deals 20% less.
 *
 * <pre>
 * Strike &gt; Boulder, Tidal      Tracker &gt; Boulder, Strike
 * Boulder &gt; Sharp, Mystery     Tidal &gt; Stoker, Tracker
 * Sharp &gt; Stoker, Strike       Mystery &gt; Sharp, Tidal
 * Stoker &gt; Tracker, Mystery
 * </pre>
 */
public final class TypeChart {
    /** Multiplier when the attacker's class is strong against the defender's. */
    public static final double ADVANTAGE = 1.25;
    /** Multiplier when the attacker's class is weak against the defender's. */
    public static final double DISADVANTAGE = 0.8;

    private static final Map<DragonClass, Set<DragonClass>> BEATS = new EnumMap<>(DragonClass.class);

    static {
        BEATS.put(DragonClass.STRIKE, EnumSet.of(DragonClass.BOULDER, DragonClass.TIDAL));
        BEATS.put(DragonClass.BOULDER, EnumSet.of(DragonClass.SHARP, DragonClass.MYSTERY));
        BEATS.put(DragonClass.SHARP, EnumSet.of(DragonClass.STOKER, DragonClass.STRIKE));
        BEATS.put(DragonClass.STOKER, EnumSet.of(DragonClass.TRACKER, DragonClass.MYSTERY));
        BEATS.put(DragonClass.TRACKER, EnumSet.of(DragonClass.BOULDER, DragonClass.STRIKE));
        BEATS.put(DragonClass.TIDAL, EnumSet.of(DragonClass.STOKER, DragonClass.TRACKER));
        BEATS.put(DragonClass.MYSTERY, EnumSet.of(DragonClass.SHARP, DragonClass.TIDAL));
    }

    private TypeChart() {
    }

    /**
     * Returns the damage multiplier of an elemental move.
     *
     * @param attacker the class of the attacking dragon
     * @param defender the class of the defending dragon
     * @return 1.25, 0.8 or 1.0
     */
    public static double multiplier(DragonClass attacker, DragonClass defender) {
        if (BEATS.get(attacker).contains(defender)) {
            return ADVANTAGE;
        }
        if (BEATS.get(defender).contains(attacker)) {
            return DISADVANTAGE;
        }
        return 1.0;
    }
}
