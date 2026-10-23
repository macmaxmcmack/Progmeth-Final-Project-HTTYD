package httyd.model.skill;

import httyd.battle.AttackContext;

/**
 * Strategy interface for the extra behaviour of a move. Every effect overrides only the hooks
 * it needs, so the battle engine never has to know about concrete effects.
 */
public interface MoveEffect {

    /**
     * Called before the damage is calculated. Effects can change the parameters of the attack
     * (pierce, critical chance, number of hits...).
     *
     * @param ctx the attack in progress
     */
    default void modifyAttack(AttackContext ctx) {
    }

    /**
     * Called once when the move is used, whether or not it hits. Used for self effects.
     *
     * @param ctx the attack in progress
     */
    default void onUse(AttackContext ctx) {
    }

    /**
     * Called after the damage was dealt, only when the attack was not dodged.
     *
     * @param ctx the attack in progress
     */
    default void afterHit(AttackContext ctx) {
    }
}
