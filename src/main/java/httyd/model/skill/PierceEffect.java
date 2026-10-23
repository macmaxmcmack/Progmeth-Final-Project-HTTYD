package httyd.model.skill;

import httyd.battle.AttackContext;

/** Ignores a fraction of the target's defence. */
public class PierceEffect implements MoveEffect {
    private final double fraction;

    /** @param fraction fraction of DEF that is ignored, from 0 to 1 */
    public PierceEffect(double fraction) {
        this.fraction = fraction;
    }

    @Override
    public void modifyAttack(AttackContext ctx) {
        ctx.setPierce(Math.max(ctx.getPierce(), fraction));
    }
}
