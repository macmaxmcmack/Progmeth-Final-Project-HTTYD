package httyd.model.skill;

import httyd.battle.AttackContext;
import httyd.model.dragon.Stat;

/** Reduces a stat of the target for a few of its turns. */
public class StatDebuffEffect implements MoveEffect {
    private final Stat stat;
    private final double percent;
    private final int turns;

    /**
     * @param stat    the stat to reduce
     * @param percent size of the reduction in percent
     * @param turns   duration counted in the target's own turns
     */
    public StatDebuffEffect(Stat stat, double percent, int turns) {
        this.stat = stat;
        this.percent = percent;
        this.turns = turns;
    }

    @Override
    public void afterHit(AttackContext ctx) {
        boolean applied = ctx.getDefender().addModifier(stat, -percent / 100.0, 0, turns, false);
        if (applied) {
            ctx.getLog().status(ctx.getDefender(), ctx.getDefender().getName() + " "
                    + stat.getDisplayName() + " reduced by " + Math.round(percent) + "%");
        } else {
            ctx.getLog().message(ctx.getDefender().getName() + " is immune to a reduction of "
                    + stat.getDisplayName());
        }
    }
}
