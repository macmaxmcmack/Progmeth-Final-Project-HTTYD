package httyd.model.player;

import httyd.bot.BotStrategy;
import httyd.model.dragon.OwnedDragon;
import httyd.model.item.Item;
import java.util.List;

/**
 * A computer opponent. It is a {@link Player} whose decisions come from a {@link BotStrategy}.
 * The bot does not use the turn clock.
 */
public class BotPlayer extends Player {
    private final BotStrategy strategy;
    private final Difficulty difficulty;

    /**
     * @param name       display name
     * @param team       the bot's dragons
     * @param bag        the bot's items
     * @param strategy   how the bot decides
     * @param difficulty the difficulty it was created with
     */
    public BotPlayer(String name, List<OwnedDragon> team, List<Item> bag,
                     BotStrategy strategy, Difficulty difficulty) {
        super(name, team, bag);
        this.strategy = strategy;
        this.difficulty = difficulty;
    }

    /** @return the decision strategy */
    public BotStrategy getStrategy() {
        return strategy;
    }

    /** @return the difficulty */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    @Override
    public boolean isTimed() {
        return false;
    }

    @Override
    public boolean isBot() {
        return true;
    }
}
