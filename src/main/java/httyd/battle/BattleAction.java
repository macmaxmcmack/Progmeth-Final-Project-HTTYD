package httyd.battle;

import httyd.model.skill.MoveKind;

/**
 * What a side can do on its turn. Only one action is allowed per turn.
 */
public sealed interface BattleAction {

    /**
     * Attack with one of the three moves.
     *
     * @param kind which move
     */
    record Attack(MoveKind kind) implements BattleAction {
    }

    /**
     * Use a trainer item from the bag. It costs the whole turn.
     *
     * @param bagIndex index in the bag
     */
    record UseItem(int bagIndex) implements BattleAction {
    }

    /** Skip the attack and reload shots. */
    record Rest() implements BattleAction {
    }
}
