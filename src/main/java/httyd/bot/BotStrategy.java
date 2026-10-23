package httyd.bot;

import httyd.battle.BattleAction;
import httyd.battle.BattleEngine;
import httyd.battle.Side;
import java.util.Random;

/**
 * Strategy interface for the decisions of a computer player. Different difficulty levels are
 * different implementations of this interface.
 */
public interface BotStrategy {

    /** @return the display name of the strategy */
    String getName();

    /**
     * Chooses the action for the bot's turn. The returned action must be legal.
     *
     * @param engine the running battle (read only use)
     * @param me     the side of the bot
     * @param random the random source
     * @return a legal action
     */
    BattleAction chooseAction(BattleEngine engine, Side me, Random random);

    /**
     * Chooses which dragon to send after the active one fainted.
     *
     * @param engine the running battle
     * @param me     the side of the bot
     * @param random the random source
     * @return the index in the bot's team of a dragon that has not fainted
     */
    int chooseReplacement(BattleEngine engine, Side me, Random random);
}
