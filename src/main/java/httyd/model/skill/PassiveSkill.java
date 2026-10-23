package httyd.model.skill;

import httyd.battle.BattleLog;
import httyd.battle.Combatant;
import httyd.model.dragon.Stat;
import java.util.List;

/**
 * Strategy interface for the unique passive ability of a dragon. All hooks have neutral
 * default implementations so a concrete passive only overrides what it needs.
 */
public interface PassiveSkill {

    /** @return the English name of the passive */
    String getName();

    /** @return the description of the passive */
    String getDescription();

    /**
     * Called when the dragon enters the battlefield.
     *
     * @param self the dragon owning the passive
     * @param log  the battle log
     */
    default void onSwitchIn(Combatant self, BattleLog log) {
    }

    /**
     * Called at the start of the owner's turn.
     *
     * @param self the dragon owning the passive
     * @param log  the battle log
     */
    default void onTurnStart(Combatant self, BattleLog log) {
    }

    /** @return extra dodge chance in percent */
    default double dodgeBonus(Combatant self) {
        return 0;
    }

    /**
     * Flat bonus added to one of the owner's stats.
     *
     * @param self the owner
     * @param foe  the opposing dragon
     * @param stat the stat being calculated
     * @return the flat bonus
     */
    default double flatStatBonus(Combatant self, Combatant foe, Stat stat) {
        return 0;
    }

    /**
     * Multiplier this passive applies to a stat of the opposing dragon (an aura).
     *
     * @param stat the stat of the opponent
     * @return a multiplier, 1.0 for none
     */
    default double statMultiplierOnOpponent(Stat stat) {
        return 1.0;
    }

    /**
     * @param kind the kind of move the owner uses
     * @return damage multiplier for the owner's attacks
     */
    default double outgoingDamageMultiplier(MoveKind kind) {
        return 1.0;
    }

    /**
     * @param kind the kind of move hitting the owner
     * @param crit true if the hit is critical
     * @return damage multiplier for the damage the owner receives
     */
    default double incomingDamageMultiplier(MoveKind kind, boolean crit) {
        return 1.0;
    }

    /**
     * @param kind the kind of move the owner uses
     * @return extra critical chance from 0 to 1
     */
    default double critChanceBonus(MoveKind kind) {
        return 0;
    }

    /** @return true if the owner's attacks can never be dodged */
    default boolean attacksIgnoreDodge() {
        return false;
    }

    /**
     * @param stat the stat that would be reduced
     * @return true if the owner cannot be debuffed on that stat
     */
    default boolean immuneToDebuff(Stat stat) {
        return false;
    }

    /**
     * Extra effects added to every move of a kind.
     *
     * @param kind the kind of move the owner uses
     * @return additional effects, possibly empty
     */
    default List<MoveEffect> extraEffects(MoveKind kind) {
        return List.of();
    }

    /**
     * Called after the owner used a move.
     *
     * @param self the owner
     * @param kind the move kind
     * @param log  the battle log
     */
    default void onMoveUsed(Combatant self, MoveKind kind, BattleLog log) {
    }

    /**
     * Called after the owner received damage.
     *
     * @param self     the owner
     * @param attacker the dragon that attacked
     * @param damage   the damage received
     * @param kind     the kind of move
     * @param log      the battle log
     */
    default void onAttacked(Combatant self, Combatant attacker, int damage, MoveKind kind, BattleLog log) {
    }

    /**
     * Asks whether the owner survives a lethal hit with 1 HP.
     *
     * @param self the owner
     * @return true if the owner is saved (and the passive is consumed)
     */
    default boolean preventFaint(Combatant self) {
        return false;
    }
}
