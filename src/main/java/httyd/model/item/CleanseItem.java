package httyd.model.item;

import httyd.battle.BattleLog;
import httyd.battle.Combatant;

/** Removes Burn, Poison and all stat reductions. */
public class CleanseItem extends Item {

    /**
     * @param name     English name
     * @param displayName display name
     */
    public CleanseItem(String name, String displayName) {
        super(name, displayName, "Removes burning, poison and all stat debuffs");
    }

    @Override
    public boolean isUseful(Combatant target) {
        return target.hasCleansableStatus();
    }

    @Override
    public boolean use(Combatant target, BattleLog log) {
        if (!target.cleanse()) {
            return false;
        }
        log.status(target, target.getName() + " is cleansed of all status effects");
        return true;
    }
}
