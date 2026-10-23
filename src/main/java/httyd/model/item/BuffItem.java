package httyd.model.item;

import httyd.battle.BattleLog;
import httyd.battle.Combatant;
import httyd.model.dragon.Stat;

/** Raises one stat of the active dragon for a few turns. */
public class BuffItem extends Item {
    private final Stat stat;
    private final double percent;
    private final int turns;

    /**
     * @param name     English name
     * @param displayName display name
     * @param stat     the stat to raise
     * @param percent  percentage added to the stat
     * @param turns    duration in own turns
     */
    public BuffItem(String name, String displayName, Stat stat, double percent, int turns) {
        super(name, displayName, "Boosts " + stat.getDisplayName() + " +" + Math.round(percent)
                + "% for " + turns + " turns");
        this.stat = stat;
        this.percent = percent;
        this.turns = turns;
    }

    @Override
    public boolean use(Combatant target, BattleLog log) {
        target.addModifier(stat, percent / 100.0, 0, turns, true);
        log.status(target, target.getName() + " " + stat.getDisplayName() + " increased by "
                + Math.round(percent) + "%");
        return true;
    }
}
