package httyd.model.skill;

import httyd.battle.AttackContext;
import httyd.model.dragon.Stat;

/** Raises a stat of the user, either permanently (turns &lt; 0) or for a number of turns. */
public class SelfBuffEffect implements MoveEffect {
    private final Stat stat;
    private final int flat;
    private final double percent;
    private final int turns;

    /**
     * @param stat    the stat to raise
     * @param flat    flat amount added to the stat
     * @param percent percentage added to the stat
     * @param turns   duration in the user's own turns, or -1 for the rest of the battle
     */
    public SelfBuffEffect(Stat stat, int flat, double percent, int turns) {
        this.stat = stat;
        this.flat = flat;
        this.percent = percent;
        this.turns = turns;
    }

    @Override
    public void onUse(AttackContext ctx) {
        ctx.getAttacker().addModifier(stat, percent / 100.0, flat, turns, true);
        ctx.getLog().status(ctx.getAttacker(), ctx.getAttacker().getName() + " "
                + stat.getDisplayName() + " increased");
    }
}
