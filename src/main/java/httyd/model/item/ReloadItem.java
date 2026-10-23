package httyd.model.item;

import httyd.battle.BattleLog;
import httyd.battle.Combatant;

/** Refills Signature shots. */
public class ReloadItem extends Item {
    private final int shots;

    /**
     * @param name     English name
     * @param displayName display name
     * @param shots    number of shots added
     */
    public ReloadItem(String name, String displayName, int shots) {
        super(name, displayName, "Reloads +" + shots + " shots instantly");
        this.shots = shots;
    }

    @Override
    public boolean isUseful(Combatant target) {
        return target.getShots() < target.getMaxShots();
    }

    @Override
    public boolean use(Combatant target, BattleLog log) {
        int added = target.addShots(shots);
        if (added <= 0) {
            return false;
        }
        log.status(target, target.getName() + " gains +" + added + " shots");
        return true;
    }
}
