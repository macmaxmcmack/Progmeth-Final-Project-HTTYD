package httyd.battle;

import httyd.model.field.BattlefieldType;
import httyd.model.player.BotPlayer;
import httyd.model.player.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Connects the {@link BattleEngine} with the bot. The GUI sends the player's choices here and
 * asks the session to play the bot's moves one at a time, so it can show each step slowly.
 */
public class BattleSession {
    private final BattleEngine engine;
    private final BotPlayer bot;
    private final Random random;

    /**
     * @param player    the human player
     * @param bot       the computer opponent
     * @param field     the battlefield of the match
     * @param random    the random source
     * @param time      the time source
     */
    public BattleSession(Player player, BotPlayer bot, BattlefieldType field, Random random, TimeSource time) {
        this.engine = new BattleEngine(player, bot, field, random, time);
        this.bot = bot;
        this.random = random;
    }

    /** @return the engine, for read-only queries */
    public BattleEngine getEngine() {
        return engine;
    }

    /** @return the opening events */
    public List<BattleEvent> start() {
        return engine.start();
    }

    /**
     * Performs the player's action.
     *
     * @param action the action
     * @return the events
     */
    public List<BattleEvent> playerAction(BattleAction action) {
        return engine.perform(action);
    }

    /**
     * Sends the player's replacement dragon.
     *
     * @param index index in the player's team
     * @return the events
     */
    public List<BattleEvent> playerReplacement(int index) {
        return engine.chooseReplacement(Side.PLAYER, index);
    }

    /** @return true if the player has to choose a replacement dragon */
    public boolean playerMustReplace() {
        return engine.getPhase() == BattleEngine.Phase.WAITING_REPLACEMENT
                && engine.getPendingReplacements().contains(Side.PLAYER);
    }

    /** @return true if the bot has something to decide (an action or a replacement) */
    public boolean botHasDecision() {
        if (engine.getPhase() == BattleEngine.Phase.WAITING_ACTION) {
            return engine.getCurrentSide() == Side.OPPONENT;
        }
        return engine.getPhase() == BattleEngine.Phase.WAITING_REPLACEMENT
                && engine.getPendingReplacements().contains(Side.OPPONENT);
    }

    /**
     * Lets the bot make its pending decision.
     *
     * @return the events, empty if the bot had nothing to do
     */
    public List<BattleEvent> advanceBot() {
        List<BattleEvent> events = new ArrayList<>();
        if (!botHasDecision()) {
            return events;
        }
        if (engine.getPhase() == BattleEngine.Phase.WAITING_REPLACEMENT) {
            int index = bot.getStrategy().chooseReplacement(engine, Side.OPPONENT, random);
            events.addAll(engine.chooseReplacement(Side.OPPONENT, index));
        } else {
            BattleAction action = bot.getStrategy().chooseAction(engine, Side.OPPONENT, random);
            events.addAll(engine.perform(action));
        }
        return events;
    }
}
