package httyd.model.field;

import httyd.model.dragon.DragonClass;
import httyd.model.dragon.Stat;
import java.util.EnumSet;
import java.util.Set;

/**
 * The seven battlefields. Each one buffs some dragon classes and debuffs others.
 */
public enum BattlefieldType {
    PLAINS("Open Plains", "Plains",
            "No buffs or debuffs. A pure test of skill",
            none(), 0, 0, 0, none(), Stat.ATK, 0),
    THUNDERSTORM_PEAK("Thunderstorm Peak", "Thunderstorm Peak",
            "Strike: Atk +15%, Spd +10% | Boulder: Def -10%",
            EnumSet.of(DragonClass.STRIKE), 0.15, 0, 0.10,
            EnumSet.of(DragonClass.BOULDER), Stat.DEF, -0.10),
    VOLCANO_CRATER("Volcano Crater", "Volcano Crater",
            "Stoker: Atk +15%, Spd +10% | Tidal: Def -10%",
            EnumSet.of(DragonClass.STOKER), 0.15, 0, 0.10,
            EnumSet.of(DragonClass.TIDAL), Stat.DEF, -0.10),
    DEEP_CAVE("Deep Cave", "Deep Cave",
            "Boulder: Atk +15%, Def +15% | Sharp: Spd -10%",
            EnumSet.of(DragonClass.BOULDER), 0.15, 0.15, 0,
            EnumSet.of(DragonClass.SHARP), Stat.SPD, -0.10),
    DEEP_FOREST("Deep Forest", "Deep Forest",
            "Tracker & Sharp: Atk +15%, Spd +10% | Boulder: Spd -10%",
            EnumSet.of(DragonClass.TRACKER, DragonClass.SHARP), 0.15, 0, 0.10,
            EnumSet.of(DragonClass.BOULDER), Stat.SPD, -0.10),
    TOXIC_RUINS("Toxic Ruins", "Toxic Ruins",
            "Mystery: Atk +15%, Spd +10% | Sharp: Def -10%",
            EnumSet.of(DragonClass.MYSTERY), 0.15, 0, 0.10,
            EnumSet.of(DragonClass.SHARP), Stat.DEF, -0.10),
    OPEN_OCEAN("Open Ocean", "Open Ocean",
            "Tidal: Atk +15%, Spd +10% | Stoker: Atk -10%",
            EnumSet.of(DragonClass.TIDAL), 0.15, 0, 0.10,
            EnumSet.of(DragonClass.STOKER), Stat.ATK, -0.10);

    private final String displayName;
    private final String englishName;
    private final String description;
    private final Set<DragonClass> buffed;
    private final double buffAtk;
    private final double buffDef;
    private final double buffSpd;
    private final Set<DragonClass> debuffed;
    private final Stat debuffStat;
    private final double debuffValue;

    BattlefieldType(String displayName, String englishName, String description,
                    Set<DragonClass> buffed, double buffAtk, double buffDef, double buffSpd,
                    Set<DragonClass> debuffed, Stat debuffStat, double debuffValue) {
        this.displayName = displayName;
        this.englishName = englishName;
        this.description = description;
        this.buffed = buffed;
        this.buffAtk = buffAtk;
        this.buffDef = buffDef;
        this.buffSpd = buffSpd;
        this.debuffed = debuffed;
        this.debuffStat = debuffStat;
        this.debuffValue = debuffValue;
    }

    private static Set<DragonClass> none() {
        return EnumSet.noneOf(DragonClass.class);
    }

    /** @return display name */
    public String getDisplayName() {
        return displayName;
    }

    /** @return English name */
    public String getEnglishName() {
        return englishName;
    }

    /** @return description of the buffs and debuffs */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the multiplier this battlefield applies to a stat of a dragon class.
     *
     * @param dragonClass the class of the dragon
     * @param stat        the stat (HP is never changed)
     * @return a multiplier such as 1.15 or 0.9; 1.0 when unaffected
     */
    public double multiplier(DragonClass dragonClass, Stat stat) {
        double bonus = 0;
        if (buffed.contains(dragonClass)) {
            bonus += switch (stat) {
                case ATK -> buffAtk;
                case DEF -> buffDef;
                case SPD -> buffSpd;
                case HP -> 0;
            };
        }
        if (debuffed.contains(dragonClass) && debuffStat == stat) {
            bonus += debuffValue;
        }
        return 1.0 + bonus;
    }

    /**
     * Returns the home battlefield of a dragon class, used by the Ultimate terrain shift.
     *
     * @param dragonClass the class
     * @return the home battlefield
     */
    public static BattlefieldType homeOf(DragonClass dragonClass) {
        return switch (dragonClass) {
            case STRIKE -> THUNDERSTORM_PEAK;
            case STOKER -> VOLCANO_CRATER;
            case BOULDER -> DEEP_CAVE;
            case TRACKER, SHARP -> DEEP_FOREST;
            case MYSTERY -> TOXIC_RUINS;
            case TIDAL -> OPEN_OCEAN;
        };
    }
}
