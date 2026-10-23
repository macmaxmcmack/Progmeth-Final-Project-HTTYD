package httyd.model.skill;

import httyd.battle.AttackContext;
import httyd.battle.DotType;

/** Applies a damage-over-time status (Burn or Poison) to the target. */
public class DotEffect implements MoveEffect {
    private final DotType type;
    private final double percentOfMaxHp;
    private final int turns;

    /**
     * @param type           Burn or Poison
     * @param percentOfMaxHp damage per turn as a percentage of max HP
     * @param turns          number of turns of the victim
     */
    public DotEffect(DotType type, double percentOfMaxHp, int turns) {
        this.type = type;
        this.percentOfMaxHp = percentOfMaxHp;
        this.turns = turns;
    }

    @Override
    public void afterHit(AttackContext ctx) {
        ctx.getDefender().applyDot(type, percentOfMaxHp / 100.0, turns);
        ctx.getLog().status(ctx.getDefender(),
                ctx.getDefender().getName() + " is " + type.getDisplayName());
    }
}
