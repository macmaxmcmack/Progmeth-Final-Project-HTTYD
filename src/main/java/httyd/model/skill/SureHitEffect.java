package httyd.model.skill;

import httyd.battle.AttackContext;

/** The move can never be dodged. */
public class SureHitEffect implements MoveEffect {
    @Override
    public void modifyAttack(AttackContext ctx) {
        ctx.setSureHit(true);
    }
}
