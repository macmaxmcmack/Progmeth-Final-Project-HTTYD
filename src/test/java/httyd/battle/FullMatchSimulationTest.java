package httyd.battle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import httyd.bot.BotFactory;
import httyd.data.DragonRepository;
import httyd.model.GameRules;
import httyd.model.field.BattlefieldType;
import httyd.model.player.BotPlayer;
import httyd.model.player.Difficulty;
import httyd.model.player.Player;
import httyd.model.skill.MoveKind;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Plays whole matches with random legal moves for the "human" side against the three bots, to be
 * sure that the engine never gets stuck, never throws and always ends with an outcome.
 */
class FullMatchSimulationTest {
    private static final DragonRepository REPO = DragonRepository.loadDefault();

    @Test
    void randomPlayAlwaysEndsWithAnOutcome() {
        for (Difficulty difficulty : Difficulty.values()) {
            for (int seed = 0; seed < 40; seed++) {
                Random random = new Random(seed * 31L + difficulty.ordinal());
                BotPlayer bot = BotFactory.create(difficulty, REPO, random);
                BotPlayer stand = BotFactory.create(Difficulty.EASY, REPO, random);
                Player human = new Player("Player", stand.getTeam(), stand.getBag());
                BattlefieldType field = BattlefieldType.values()[random.nextInt(BattlefieldType.values().length)];
                BattleSession session = new BattleSession(human, bot, field, random, TimeSource.system());
                session.start();
                int guard = 0;
                while (session.getEngine().getPhase() != BattleEngine.Phase.FINISHED) {
                    assertTrue(guard++ < 1000, "match did not end, seed " + seed);
                    playOneStep(session, random);
                }
                assertNotNull(session.getEngine().getOutcome());
                assertNotNull(session.getEngine().getEndReason());
                assertTrue(session.getEngine().getActionCount() <= GameRules.MAX_ACTIONS + 2);
            }
        }
    }

    private void playOneStep(BattleSession session, Random random) {
        if (session.playerMustReplace()) {
            List<Combatant> team = session.getEngine().getTeam(Side.PLAYER);
            for (int i = 0; i < team.size(); i++) {
                if (!team.get(i).isFainted()) {
                    session.playerReplacement(i);
                    return;
                }
            }
        } else if (session.botHasDecision()) {
            session.advanceBot();
        } else {
            List<BattleAction> legal = legalActions(session.getEngine());
            session.playerAction(legal.get(random.nextInt(legal.size())));
        }
    }

    private List<BattleAction> legalActions(BattleEngine engine) {
        List<BattleAction> result = new ArrayList<>();
        for (MoveKind kind : MoveKind.values()) {
            result.add(new BattleAction.Attack(kind));
        }
        result.add(new BattleAction.Rest());
        int bag = engine.getPlayer(Side.PLAYER).getBag().size();
        for (int i = 0; i < bag; i++) {
            result.add(new BattleAction.UseItem(i));
        }
        result.removeIf(action -> !engine.canPerform(Side.PLAYER, action));
        assertTrue(!result.isEmpty(), "the typeless attack is always legal");
        return result;
    }

    @Test
    void outcomeMatchesTheRemainingDragons() {
        Random random = new Random(99);
        BotPlayer bot = BotFactory.create(Difficulty.HARD, REPO, random);
        BotPlayer stand = BotFactory.create(Difficulty.EASY, REPO, random);
        Player human = new Player("Player", stand.getTeam(), stand.getBag());
        BattleSession session = new BattleSession(human, bot, BattlefieldType.PLAINS, random, TimeSource.system());
        session.start();
        while (session.getEngine().getPhase() != BattleEngine.Phase.FINISHED) {
            playOneStep(session, random);
        }
        BattleEngine engine = session.getEngine();
        if (engine.getEndReason() == BattleEngine.EndReason.KNOCKOUT) {
            int mine = engine.remaining(Side.PLAYER);
            int theirs = engine.remaining(Side.OPPONENT);
            if (engine.getOutcome() == BattleEngine.Outcome.PLAYER_WIN) {
                assertEquals(0, theirs);
            } else if (engine.getOutcome() == BattleEngine.Outcome.OPPONENT_WIN) {
                assertEquals(0, mine);
            }
        }
    }
}
