package httyd.battle;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects the events of a battle. The engine, the moves and the passives write to it, and the
 * GUI reads and clears it after every action.
 */
public class BattleLog {
    private final List<BattleEvent> events = new ArrayList<>();

    /** Adds a general message. */
    public void message(String text) {
        add(EventType.MESSAGE, null, text, 0);
    }

    /** Adds a status message about a dragon. */
    public void status(Combatant who, String text) {
        add(EventType.STATUS, who.getSide(), text, 0);
    }

    /** Adds a damage event for the victim. */
    public void damage(Combatant victim, int amount, boolean crit) {
        add(crit ? EventType.CRIT : EventType.DAMAGE, victim.getSide(),
                victim.getName() + " takes " + amount + " damage" + (crit ? " (critical hit!)" : ""), amount);
    }

    /** Adds a healing event. */
    public void heal(Combatant who, int amount) {
        add(EventType.HEAL, who.getSide(), who.getName() + " recovers " + amount + " HP", amount);
    }

    /** Adds a dodge event. */
    public void dodge(Combatant defender) {
        add(EventType.DODGE, defender.getSide(), defender.getName() + " dodged the attack!", 0);
    }

    /** Adds a damage-reflection event. */
    public void reflect(Combatant source, Combatant target, int amount) {
        add(EventType.REFLECT, target.getSide(),
                source.getName() + " reflects " + amount + " damage back to " + target.getName(), amount);
    }

    /** Adds an arbitrary event. */
    public void add(EventType type, Side side, String text, int value) {
        events.add(new BattleEvent(type, side, text, value));
    }

    /**
     * Removes and returns all collected events.
     *
     * @return the events in the order they happened
     */
    public List<BattleEvent> drain() {
        List<BattleEvent> copy = new ArrayList<>(events);
        events.clear();
        return copy;
    }
}
