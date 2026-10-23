package httyd.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import httyd.battle.BattleAction;
import httyd.battle.BattleEngine;
import httyd.battle.Side;
import httyd.battle.TimeSource;
import httyd.data.DragonRepository;
import httyd.model.dragon.DragonSpecies;
import httyd.model.field.BattlefieldType;
import httyd.model.player.BotPlayer;
import httyd.model.player.Difficulty;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BotTest {
    private DragonRepository repo;
    private Random random;

    @BeforeEach
    void setUp() {
        repo = DragonRepository.loadDefault();
        random = new Random(2026);
    }

    @Test
    void botDeckHasThreeDifferentDragonsAndTwoDifferentItems() {
        for (Difficulty d : Difficulty.values()) {
            for (int i = 0; i < 50; i++) {
                BotPlayer bot = BotFactory.create(d, repo, random);
                Set<String> ids = new HashSet<>();
                bot.getTeam().forEach(o -> ids.add(o.getSpecies().getId()));
                assertEquals(3, ids.size());
                assertEquals(2, bot.getBag().size());
                assertNotEquals(bot.getBag().get(0).getName(), bot.getBag().get(1).getName());
                assertFalse(bot.isTimed());
                assertTrue(bot.isBot());
                assertNotNull(bot.getStrategy());
            }
        }
    }

    private static void assertNotEquals(String a, String b) {
        assertFalse(a.equals(b));
    }

    @Test
    void hardBotsFieldRarerDragonsThanEasyBots() {
        int easyRare = 0;
        int hardRare = 0;
        for (int i = 0; i < 300; i++) {
            for (var o : BotFactory.create(Difficulty.EASY, repo, random).getTeam()) {
                if (o.getSpecies().getTier().ordinal() >= 2) {
                    easyRare++;
                }
            }
            for (var o : BotFactory.create(Difficulty.HARD, repo, random).getTeam()) {
                if (o.getSpecies().getTier().ordinal() >= 2) {
                    hardRare++;
                }
            }
        }
        assertTrue(hardRare > easyRare);
    }

    @Test
    void botFieldIsAlwaysAValidField() {
        BotPlayer bot = BotFactory.create(Difficulty.NORMAL, repo, random);
        for (int i = 0; i < 50; i++) {
            assertNotNull(BotFactory.pickField(bot, random));
        }
    }

    /** Plays a full bot-against-bot battle and returns the engine; every chosen action must be legal. */
    private BattleEngine playOut(Difficulty a, Difficulty b) {
        BotPlayer first = BotFactory.create(a, repo, random);
        BotPlayer second = BotFactory.create(b, repo, random);
        BattlefieldType field = BattlefieldType.values()[random.nextInt(BattlefieldType.values().length)];
        BattleEngine engine = new BattleEngine(first, second, field, random, TimeSource.system());
        engine.start();
        int guard = 0;
        while (engine.getPhase() != BattleEngine.Phase.FINISHED && guard++ < 1000) {
            if (engine.getPhase() == BattleEngine.Phase.WAITING_REPLACEMENT) {
                for (Side side : engine.getPendingReplacements()) {
                    BotStrategy strategy = ((BotPlayer) engine.getPlayer(side)).getStrategy();
                    int index = strategy.chooseReplacement(engine, side, random);
                    assertFalse(engine.getTeam(side).get(index).isFainted());
                    engine.chooseReplacement(side, index);
                }
            } else {
                Side side = engine.getCurrentSide();
                BotStrategy strategy = ((BotPlayer) engine.getPlayer(side)).getStrategy();
                BattleAction action = strategy.chooseAction(engine, side, random);
                assertTrue(engine.canPerform(side, action), "illegal action " + action);
                engine.perform(action);
            }
        }
        return engine;
    }

    @Test
    void everyStrategyAlwaysPlaysLegallyAndFinishesTheMatch() {
        for (Difficulty a : Difficulty.values()) {
            for (Difficulty b : Difficulty.values()) {
                for (int i = 0; i < 40; i++) {
                    BattleEngine engine = playOut(a, b);
                    assertEquals(BattleEngine.Phase.FINISHED, engine.getPhase());
                    assertNotNull(engine.getOutcome());
                }
            }
        }
    }

    @Test
    void hardBotBeatsEasyBotMostOfTheTime() {
        int hardWins = 0;
        int games = 150;
        for (int i = 0; i < games; i++) {
            if (playOut(Difficulty.HARD, Difficulty.EASY).getOutcome() == BattleEngine.Outcome.PLAYER_WIN) {
                hardWins++;
            }
        }
        assertTrue(hardWins > games * 0.7, "hard won " + hardWins + " of " + games);
    }

    @Test
    void matchesLastAboutTwentyActionsOnAverage() {
        int total = 0;
        int games = 100;
        for (int i = 0; i < games; i++) {
            total += playOut(Difficulty.NORMAL, Difficulty.NORMAL).getActionCount();
        }
        double average = total / (double) games;
        assertTrue(average > 12 && average < 40, "average actions " + average);
    }

    @Test
    void speciesPickIsNeverNull() {
        DragonSpecies s = repo.all().get(0);
        assertNotNull(s);
    }
}
