package httyd.bot;

import httyd.data.DragonRepository;
import httyd.data.GachaService;
import httyd.model.dragon.DragonSpecies;
import httyd.model.dragon.OwnedDragon;
import httyd.model.field.BattlefieldType;
import httyd.model.item.Item;
import httyd.model.item.ItemType;
import httyd.model.player.BotPlayer;
import httyd.model.player.Difficulty;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Builds a computer opponent: its deck, its items, its strategy and its favourite battlefield.
 */
public final class BotFactory {

    private BotFactory() {
    }

    /**
     * Creates a bot opponent.
     *
     * @param difficulty the difficulty
     * @param repository the species database
     * @param random     the random source
     * @return a bot with a random deck of three different dragons and two items
     */
    public static BotPlayer create(Difficulty difficulty, DragonRepository repository, Random random) {
        GachaService gacha = new GachaService(repository, random);
        List<OwnedDragon> team = new ArrayList<>();
        Set<String> used = new HashSet<>();
        int id = 1000;
        while (team.size() < 3) {
            DragonSpecies species = gacha.pickSpecies(difficulty.getRarityPercent());
            if (!used.add(species.getId())) {
                continue;
            }
            OwnedDragon rolled = gacha.rollDragon(id++, species);
            if (difficulty == Difficulty.HARD) {
                OwnedDragon second = gacha.rollDragon(id++, species);
                if (second.getGrade().ordinal() > rolled.getGrade().ordinal()) {
                    rolled = second;
                }
            }
            team.add(rolled);
        }
        List<ItemType> types = new ArrayList<>(List.of(ItemType.values()));
        List<Item> bag = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            bag.add(types.remove(random.nextInt(types.size())).create());
        }
        BotStrategy strategy = switch (difficulty) {
            case EASY -> new RandomBotStrategy();
            case NORMAL -> new GreedyBotStrategy();
            case HARD -> new SmartBotStrategy();
        };
        return new BotPlayer(strategy.getName(), team, bag, strategy, difficulty);
    }

    /**
     * Lets the bot pick one battlefield: the home field of its first dragon, or a random one.
     *
     * @param bot    the bot
     * @param random the random source
     * @return the chosen battlefield
     */
    public static BattlefieldType pickField(BotPlayer bot, Random random) {
        if (random.nextInt(100) < 60) {
            return BattlefieldType.homeOf(bot.getTeam().get(0).getSpecies().getDragonClass());
        }
        BattlefieldType[] all = BattlefieldType.values();
        return all[random.nextInt(all.length)];
    }
}
