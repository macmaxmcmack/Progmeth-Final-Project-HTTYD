package httyd.model.item;

import httyd.battle.BattleLog;
import httyd.battle.Combatant;

/**
 * Base class of all trainer items. A subclass decides what {@link #use} does; the battle engine
 * only calls {@code use} and never checks which kind of item it is (polymorphism).
 */
public abstract class Item {
    private final String name;
    private final String displayName;
    private final String description;

    /**
     * @param name        English name
     * @param displayName    display name
     * @param description description of the effect
     */
    protected Item(String name, String displayName, String description) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
    }

    /** @return the English name */
    public String getName() {
        return name;
    }

    /** @return the display name */
    public String getDisplayName() {
        return displayName;
    }

    /** @return the description of the effect */
    public String getDescription() {
        return description;
    }

    /**
     * Uses the item on the active dragon.
     *
     * @param target the dragon on the field
     * @param log    the battle log
     * @return true if the item had an effect and is consumed; false if it would do nothing, in
     *         which case the turn is not spent
     */
    public abstract boolean use(Combatant target, BattleLog log);

    /**
     * Tells whether using the item now would change anything. The engine refuses useless items so
     * a turn is never wasted by mistake.
     *
     * @param target the dragon on the field
     * @return true if the item would have an effect
     */
    public boolean isUseful(Combatant target) {
        return true;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
