package httyd.battle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import httyd.bot.BotFactory;
import httyd.data.DragonRepository;
import httyd.model.field.BattlefieldType;
import httyd.model.player.BotPlayer;
import httyd.model.player.Difficulty;
import httyd.model.player.Player;
import httyd.model.skill.MoveKind;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BattleSessionTest {
    private DragonRepository repo;
    private Random random;
    private BattleSession session;

    @BeforeEach
    void setUp() {
        repo = DragonRepository.loadDefault();
        random = new Random(5);
        BotPlayer bot = BotFactory.create(Difficulty.NORMAL, repo, random);
        BotPlayer stand = BotFactory.create(Difficulty.EASY, repo, random);
        Player human = new Player("Player", stand.getTeam(), stand.getBag());
        session = new BattleSession(human, bot, BattlefieldType.PLAINS, random, TimeSource.system());
    }

    @Test
    void startGivesOpeningEvents() {
        List<BattleEvent> events = session.start();
        assertFalse(events.isEmpty());
        assertTrue(events.stream().anyMatch(e -> e.type() == EventType.SWITCH_IN));
    }

    @Test
    void botPlaysWhenItIsItsTurnAndNotBefore() {
        session.start();
        int guard = 0;
        while (guard++ < 200 && session.getEngine().getPhase() != BattleEngine.Phase.FINISHED) {
            if (session.botHasDecision()) {
                assertFalse(session.advanceBot().isEmpty());
            } else if (session.playerMustReplace()) {
                int index = firstHealthyPlayerDragon();
                session.playerReplacement(index);
            } else {
                assertEquals(Side.PLAYER, session.getEngine().getCurrentSide());
                assertTrue(session.advanceBot().isEmpty());
                session.playerAction(new BattleAction.Attack(MoveKind.TYPELESS));
            }
        }
        assertEquals(BattleEngine.Phase.FINISHED, session.getEngine().getPhase());
    }

    private int firstHealthyPlayerDragon() {
        List<Combatant> team = session.getEngine().getTeam(Side.PLAYER);
        for (int i = 0; i < team.size(); i++) {
            if (!team.get(i).isFainted()) {
                return i;
            }
        }
        throw new IllegalStateException("no dragon left");
    }
}
