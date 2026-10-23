package httyd.model.item;

import httyd.battle.BattleLog;
import httyd.battle.Combatant;

/** Restores a percentage of the maximum HP. */
public class HealItem extends Item {
    private final double percent;

    /**
     * @param name     English name
     * @param displayName display name
     * @param percent  percentage of max HP restored
     */
    public HealItem(String name, String displayName, double percent) {
        super(name, displayName, "Restores " + Math.round(percent) + "% of max HP");
        this.percent = percent;
    }

    @Override
    public boolean isUseful(Combatant target) {
        return target.getCurrentHp() < target.getMaxHp();
    }

    @Override
    public boolean use(Combatant target, BattleLog log) {
        int healed = target.heal((int) Math.round(target.getMaxHp() * percent / 100.0));
        if (healed <= 0) {
            return false;
        }
        log.heal(target, healed);
        return true;
    }
}
