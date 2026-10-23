package httyd.model.skill;

import httyd.battle.AttackContext;

/** Raises the critical chance of the move, or guarantees a critical hit. */
public class CritEffect implements MoveEffect {
    private final boolean guaranteed;
    private final double bonus;

    /**
     * @param guaranteed true if every hit is critical
     * @param bonus      extra critical chance from 0 to 1 when not guaranteed
     */
    public CritEffect(boolean guaranteed, double bonus) {
        this.guaranteed = guaranteed;
        this.bonus = bonus;
    }

    @Override
    public void modifyAttack(AttackContext ctx) {
        if (guaranteed) {
            ctx.setForcedCrit(true);
        } else {
            ctx.setCritBonus(ctx.getCritBonus() + bonus);
        }
    }
}
