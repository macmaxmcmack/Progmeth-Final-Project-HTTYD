package httyd.model.skill;

import httyd.battle.AttackContext;

/** Restores a percentage of the user's maximum HP. */
public class HealSelfEffect implements MoveEffect {
    private final double percent;

    /** @param percent percentage of max HP to restore */
    public HealSelfEffect(double percent) {
        this.percent = percent;
    }

    @Override
    public void onUse(AttackContext ctx) {
        int amount = ctx.getAttacker().heal((int) Math.round(ctx.getAttacker().getMaxHp() * percent / 100.0));
        ctx.getLog().heal(ctx.getAttacker(), amount);
    }
}
