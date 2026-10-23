package httyd.model.skill;

import httyd.battle.AttackContext;

/** Gives the user extra dodge chance for a number of its own turns. */
public class DodgeBuffEffect implements MoveEffect {
    private final double percent;
    private final int turns;

    /**
     * @param percent extra dodge chance in percent
     * @param turns   duration in the user's own turns
     */
    public DodgeBuffEffect(double percent, int turns) {
        this.percent = percent;
        this.turns = turns;
    }

    @Override
    public void onUse(AttackContext ctx) {
        ctx.getAttacker().addDodgeBonus(percent, turns);
        ctx.getLog().status(ctx.getAttacker(), ctx.getAttacker().getName() + " becomes harder to hit");
    }
}
