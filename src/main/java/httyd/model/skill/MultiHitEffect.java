package httyd.model.skill;

import httyd.battle.AttackContext;

/** Splits the move into several hits. The dodge check is rolled once for the whole move. */
public class MultiHitEffect implements MoveEffect {
    private final int hits;

    /** @param hits number of hits, at least 1 */
    public MultiHitEffect(int hits) {
        this.hits = Math.max(1, hits);
    }

    @Override
    public void modifyAttack(AttackContext ctx) {
        ctx.setHits(hits);
    }
}
