package httyd.battle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import httyd.model.GameRules;
import httyd.model.dragon.DragonClass;
import httyd.model.dragon.DragonSpecies;
import httyd.model.field.BattlefieldType;
import httyd.model.item.ItemType;
import httyd.model.player.Player;
import httyd.model.skill.MoveKind;
import httyd.support.FakeTime;
import httyd.support.StubRandom;
import httyd.support.TestData;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BattleEngineTest {
    private static final BattleAction TYPELESS = new BattleAction.Attack(MoveKind.TYPELESS);
    private static final BattleAction SIGNATURE = new BattleAction.Attack(MoveKind.SIGNATURE);
    private static final BattleAction ULTIMATE = new BattleAction.Attack(MoveKind.ULTIMATE);

    private FakeTime time;

    @BeforeEach
    void setUp() {
        time = new FakeTime();
    }

    private BattleEngine start(Player player, Player opponent, Random random) {
        return start(player, opponent, random, BattlefieldType.PLAINS);
    }

    private BattleEngine start(Player player, Player opponent, Random random, BattlefieldType field) {
        BattleEngine engine = new BattleEngine(player, opponent, field, random, time);
        engine.start();
        return engine;
    }

    private DragonSpecies plain(String id, DragonClass cls) {
        return TestData.basic(id, cls);
    }

    @Test
    void fasterDragonMovesFirst() {
        DragonSpecies slow = TestData.species("slow", DragonClass.STRIKE, 100, 100, 100, 80, 0, 3, "NONE", "NONE", "NONE");
        DragonSpecies fast = TestData.species("fast", DragonClass.STOKER, 100, 100, 100, 120, 0, 3, "NONE", "NONE", "NONE");
        BattleEngine engine = start(TestData.player("A", slow), TestData.player("B", fast), new StubRandom());
        assertEquals(Side.OPPONENT, engine.getCurrentSide());
    }

    @Test
    void equalSpeedIsDecidedByTheRandomSource() {
        BattleEngine engine = start(TestData.player("A", plain("a", DragonClass.STRIKE)),
                TestData.player("B", plain("b", DragonClass.STOKER)), new StubRandom());
        assertEquals(Side.PLAYER, engine.getCurrentSide());
        assertEquals(BattleEngine.Phase.WAITING_ACTION, engine.getPhase());
    }

    @Test
    void plainAttackUsesFormulaA() {
        BattleEngine engine = start(TestData.player("A", plain("a", DragonClass.STRIKE)),
                TestData.player("B", plain("b", DragonClass.STOKER)), new StubRandom());
        engine.perform(TYPELESS);
        assertEquals(250, engine.getActive(Side.OPPONENT).getCurrentHp());
        assertEquals(Side.OPPONENT, engine.getCurrentSide());
    }

    @Test
    void turnsAlternate() {
        BattleEngine engine = start(TestData.player("A", plain("a", DragonClass.STRIKE)),
                TestData.player("B", plain("b", DragonClass.STOKER)), new StubRandom());
        engine.perform(TYPELESS);
        engine.perform(TYPELESS);
        assertEquals(Side.PLAYER, engine.getCurrentSide());
        assertEquals(250, engine.getActive(Side.PLAYER).getCurrentHp());
    }

    @Test
    void onlyTheCurrentSideMayAct() {
        BattleEngine engine = start(TestData.player("A", plain("a", DragonClass.STRIKE)),
                TestData.player("B", plain("b", DragonClass.STOKER)), new StubRandom());
        assertFalse(engine.canPerform(Side.OPPONENT, TYPELESS));
        assertTrue(engine.canPerform(Side.PLAYER, TYPELESS));
    }

    @Test
    void signatureCostsAShotAndNeedsOne() {
        DragonSpecies noShots = TestData.species("n", DragonClass.STRIKE, 100, 100, 100, 100, 0, 0, "NONE", "NONE", "NONE");
        BattleEngine empty = start(TestData.player("A", noShots), TestData.player("B", plain("b", DragonClass.STOKER)), new StubRandom());
        assertFalse(empty.canPerform(Side.PLAYER, SIGNATURE));
        assertThrows(IllegalStateException.class, () -> empty.perform(SIGNATURE));

        BattleEngine engine = start(TestData.player("A", plain("a", DragonClass.STRIKE)),
                TestData.player("B", plain("b", DragonClass.STOKER)), new StubRandom());
        engine.perform(SIGNATURE);
        assertEquals(2, engine.getActive(Side.PLAYER).getShots());
        assertEquals(230, engine.getActive(Side.OPPONENT).getCurrentHp());
    }

    @Test
    void elementalMoveUsesTypeAdvantage() {
        BattleEngine engine = start(TestData.player("A", plain("a", DragonClass.STRIKE)),
                TestData.player("B", plain("b", DragonClass.BOULDER)), new StubRandom());
        engine.perform(SIGNATURE);
        assertEquals(212, engine.getActive(Side.OPPONENT).getCurrentHp());
    }

    @Test
    void typelessMoveIgnoresTypeChart() {
        BattleEngine engine = start(TestData.player("A", plain("a", DragonClass.STRIKE)),
                TestData.player("B", plain("b", DragonClass.BOULDER)), new StubRandom());
        engine.perform(TYPELESS);
        assertEquals(250, engine.getActive(Side.OPPONENT).getCurrentHp());
    }

    @Test
    void criticalHitMultipliesByOnePointFive() {
        DragonSpecies crit = TestData.species("c", DragonClass.STRIKE, 100, 100, 100, 100, 0.5, 3, "NONE", "NONE", "NONE");
        BattleEngine engine = start(TestData.player("A", crit), TestData.player("B", plain("b", DragonClass.STOKER)),
                new StubRandom(0.99, 0.0));
        List<BattleEvent> events = engine.perform(TYPELESS);
        assertEquals(225, engine.getActive(Side.OPPONENT).getCurrentHp());
        assertTrue(events.stream().anyMatch(e -> e.type() == EventType.CRIT));
    }

    @Test
    void dodgeAvoidsAllDamage() {
        DragonSpecies slow = TestData.species("slow", DragonClass.STRIKE, 100, 100, 100, 100, 0, 3, "NONE", "NONE", "NONE");
        DragonSpecies fast = TestData.species("fast", DragonClass.STOKER, 100, 100, 100, 200, 0, 3, "NONE", "NONE", "NONE");
        BattleEngine engine = start(TestData.player("A", slow), TestData.player("B", fast), new StubRandom(0.99, 0.99, 0.0));
        engine.perform(TYPELESS);
        List<BattleEvent> events = engine.perform(TYPELESS);
        assertEquals(300, engine.getActive(Side.OPPONENT).getCurrentHp());
        assertTrue(events.stream().anyMatch(e -> e.type() == EventType.DODGE));
    }

    @Test
    void ultimateNeedsFullChargeFromAttackingAndBeingHit() {
        BattleEngine engine = start(TestData.player("A", plain("a", DragonClass.STRIKE)),
                TestData.player("B", plain("b", DragonClass.STOKER)), new StubRandom());
        engine.perform(TYPELESS);
        engine.perform(TYPELESS);
        assertFalse(engine.canPerform(Side.PLAYER, ULTIMATE));
        engine.perform(TYPELESS);
        engine.perform(TYPELESS);
        assertTrue(engine.canPerform(Side.PLAYER, ULTIMATE));
        engine.perform(ULTIMATE);
        assertEquals(0, engine.getActive(Side.PLAYER).getUltimateCharge());
    }

    @Test
    void elementalUltimateShiftsTerrainForTwoTurns() {
        BattleEngine engine = start(TestData.player("A", plain("a", DragonClass.STRIKE)),
                TestData.player("B", plain("b", DragonClass.STOKER)), new StubRandom());
        for (int i = 0; i < 4; i++) {
            engine.perform(TYPELESS);
        }
        engine.perform(ULTIMATE);
        assertSame(BattlefieldType.THUNDERSTORM_PEAK, engine.getBattlefield().getCurrentField());
        engine.perform(TYPELESS);
        assertSame(BattlefieldType.THUNDERSTORM_PEAK, engine.getBattlefield().getCurrentField());
        engine.perform(TYPELESS);
        assertSame(BattlefieldType.PLAINS, engine.getBattlefield().getCurrentField());
    }

    @Test
    void knockoutAsksTheLoserForAReplacement() {
        DragonSpecies weak = TestData.species("w", DragonClass.STOKER, 1, 100, 100, 100, 0, 3, "NONE", "NONE", "NONE");
        BattleEngine engine = start(TestData.player("A", plain("a", DragonClass.STRIKE)), TestData.player("B", weak), new StubRandom());
        List<BattleEvent> events = engine.perform(TYPELESS);
        assertEquals(BattleEngine.Phase.WAITING_REPLACEMENT, engine.getPhase());
        assertTrue(engine.getPendingReplacements().contains(Side.OPPONENT));
        assertTrue(events.stream().anyMatch(e -> e.type() == EventType.FAINT));
        assertThrows(IllegalStateException.class, () -> engine.perform(TYPELESS));
        engine.chooseReplacement(Side.OPPONENT, 1);
        assertEquals(BattleEngine.Phase.WAITING_ACTION, engine.getPhase());
        assertEquals(Side.OPPONENT, engine.getCurrentSide());
        assertEquals(2, engine.remaining(Side.OPPONENT));
    }

    @Test
    void cannotSendAFaintedDragon() {
        DragonSpecies weak = TestData.species("w", DragonClass.STOKER, 1, 100, 100, 100, 0, 3, "NONE", "NONE", "NONE");
        BattleEngine engine = start(TestData.player("A", plain("a", DragonClass.STRIKE)), TestData.player("B", weak), new StubRandom());
        engine.perform(TYPELESS);
        assertThrows(IllegalArgumentException.class, () -> engine.chooseReplacement(Side.OPPONENT, 0));
        assertThrows(IllegalStateException.class, () -> engine.chooseReplacement(Side.PLAYER, 1));
    }

    @Test
    void lastDragonDownEndsTheMatch() {
        DragonSpecies weak = TestData.species("w", DragonClass.STOKER, 1, 100, 100, 100, 0, 3, "NONE", "NONE", "NONE");
        BattleEngine engine = start(TestData.player("A", plain("a", DragonClass.STRIKE)), TestData.player("B", weak), new StubRandom());
        int guard = 0;
        while (engine.getPhase() != BattleEngine.Phase.FINISHED && guard++ < 50) {
            if (engine.getPhase() == BattleEngine.Phase.WAITING_REPLACEMENT) {
                int next = engine.getTeam(Side.OPPONENT).get(1).isFainted() ? 2 : 1;
                engine.chooseReplacement(Side.OPPONENT, next);
            } else {
                engine.perform(TYPELESS);
            }
        }
        assertEquals(BattleEngine.Outcome.PLAYER_WIN, engine.getOutcome());
        assertEquals(BattleEngine.EndReason.KNOCKOUT, engine.getEndReason());
        assertEquals(0, engine.remaining(Side.OPPONENT));
    }

    @Test
    void bothLastDragonsFallingTogetherIsADraw() {
        DragonSpecies thorny = TestData.species("t", DragonClass.STRIKE, 1, 100, 100, 100, 0, 3, "NONE", "NONE", "REFLECT_DMG:100");
        DragonSpecies thorny2 = TestData.species("u", DragonClass.STOKER, 1, 100, 100, 100, 0, 3, "NONE", "NONE", "REFLECT_DMG:100");
        BattleEngine engine = new BattleEngine(TestData.player("A", thorny), TestData.player("B", thorny2),
                BattlefieldType.PLAINS, new StubRandom(), time);
        engine.getTeam(Side.PLAYER).get(1).takeDamage(999);
        engine.getTeam(Side.PLAYER).get(2).takeDamage(999);
        engine.getTeam(Side.OPPONENT).get(1).takeDamage(999);
        engine.getTeam(Side.OPPONENT).get(2).takeDamage(999);
        engine.start();
        engine.perform(TYPELESS);
        assertEquals(BattleEngine.Phase.FINISHED, engine.getPhase());
        assertEquals(BattleEngine.Outcome.DRAW, engine.getOutcome());
    }

    @Test
    void runningOutOfTimeLosesImmediately() {
        BattleEngine engine = start(TestData.player("A", plain("a", DragonClass.STRIKE)),
                TestData.player("B", plain("b", DragonClass.STOKER)), new StubRandom());
        time.advanceSeconds(30);
        assertTrue(engine.checkTimeout().isEmpty());
        time.advanceSeconds(31);
        engine.checkTimeout();
        assertEquals(BattleEngine.Outcome.OPPONENT_WIN, engine.getOutcome());
        assertEquals(BattleEngine.EndReason.TIMEOUT, engine.getEndReason());
    }

    @Test
    void forfeitGivesTheWinToTheOtherSide() {
        BattleEngine engine = start(TestData.player("A", plain("a", DragonClass.STRIKE)),
                TestData.player("B", plain("b", DragonClass.STOKER)), new StubRandom());
        engine.forfeit(Side.PLAYER);
        assertEquals(BattleEngine.Outcome.OPPONENT_WIN, engine.getOutcome());
        assertEquals(BattleEngine.EndReason.FORFEIT, engine.getEndReason());
        assertFalse(engine.canPerform(Side.PLAYER, TYPELESS));
    }

    @Test
    void restRefillsTwoShots() {
        BattleEngine engine = start(TestData.player("A", plain("a", DragonClass.STRIKE)),
                TestData.player("B", plain("b", DragonClass.STOKER)), new StubRandom());
        assertFalse(engine.canPerform(Side.PLAYER, new BattleAction.Rest()));
        engine.perform(SIGNATURE);
        engine.perform(TYPELESS);
        engine.perform(SIGNATURE);
        engine.perform(TYPELESS);
        assertEquals(1, engine.getActive(Side.PLAYER).getShots());
        engine.perform(new BattleAction.Rest());
        assertEquals(3, engine.getActive(Side.PLAYER).getShots());
    }

    @Test
    void usingAnItemSpendsTheTurnAndTheItem() {
        Player a = TestData.player("A", plain("a", DragonClass.STRIKE), ItemType.ARCTIC_CHAR.create());
        BattleEngine engine = start(a, TestData.player("B", plain("b", DragonClass.STOKER)), new StubRandom());
        assertFalse(engine.canPerform(Side.PLAYER, new BattleAction.UseItem(0)));
        engine.perform(TYPELESS);
        engine.perform(TYPELESS);
        assertTrue(engine.canPerform(Side.PLAYER, new BattleAction.UseItem(0)));
        engine.perform(new BattleAction.UseItem(0));
        assertEquals(300, engine.getActive(Side.PLAYER).getCurrentHp());
        assertTrue(a.getBag().isEmpty());
        assertEquals(Side.OPPONENT, engine.getCurrentSide());
        assertFalse(engine.canPerform(Side.OPPONENT, new BattleAction.UseItem(0)));
    }

    @Test
    void pierceEffectIgnoresPartOfTheDefence() {
        DragonSpecies lance = TestData.species("l", DragonClass.STRIKE, 100, 100, 100, 100, 0, 3, "PIERCE:50", "NONE", "NONE");
        BattleEngine engine = start(TestData.player("A", lance), TestData.player("B", plain("b", DragonClass.STRIKE)), new StubRandom());
        engine.perform(SIGNATURE);
        assertEquals(300 - 93, engine.getActive(Side.OPPONENT).getCurrentHp());
    }

    @Test
    void burnDamagesTheVictimAtTheEndOfItsOwnTurn() {
        DragonSpecies fire = TestData.species("f", DragonClass.STRIKE, 100, 100, 100, 100, 0, 3, "BURN:10:2", "NONE", "NONE");
        BattleEngine engine = start(TestData.player("A", fire), TestData.player("B", plain("b", DragonClass.STRIKE)), new StubRandom());
        engine.perform(SIGNATURE);
        assertEquals(230, engine.getActive(Side.OPPONENT).getCurrentHp());
        assertTrue(engine.getActive(Side.OPPONENT).hasDot(DotType.BURN));
        engine.perform(TYPELESS);
        assertEquals(200, engine.getActive(Side.OPPONENT).getCurrentHp());
    }

    @Test
    void multiHitSplitsTheDamageButDodgesOnce() {
        DragonSpecies twin = TestData.species("t", DragonClass.STRIKE, 100, 100, 100, 100, 0, 3, "HITS:2", "NONE", "NONE");
        BattleEngine engine = start(TestData.player("A", twin), TestData.player("B", plain("b", DragonClass.STRIKE)), new StubRandom());
        List<BattleEvent> events = engine.perform(SIGNATURE);
        assertEquals(300 - 84, engine.getActive(Side.OPPONENT).getCurrentHp());
        assertEquals(2, events.stream().filter(e -> e.type() == EventType.DAMAGE).count());
    }

    @Test
    void speedDebuffLowersTheVictimsSpeedInLaterTurns() {
        DragonSpecies chill = TestData.species("c", DragonClass.STRIKE, 100, 100, 100, 100, 0, 3, "SPD_DOWN:25", "NONE", "NONE");
        BattleEngine engine = start(TestData.player("A", chill), TestData.player("B", plain("b", DragonClass.STRIKE)), new StubRandom());
        engine.perform(SIGNATURE);
        double spd = engine.getActive(Side.OPPONENT).effectiveStat(httyd.model.dragon.Stat.SPD,
                engine.getActive(Side.PLAYER), engine.getBattlefield());
        assertEquals(75.0, spd, 1e-9);
    }

    @Test
    void matchStopsAtTheActionLimitAndComparesHp() {
        DragonSpecies tank = TestData.species("t", DragonClass.STRIKE, 99, 1, 1000, 100, 0, 3, "NONE", "NONE", "NONE");
        BattleEngine engine = start(TestData.player("A", tank), TestData.player("B", tank), new StubRandom());
        while (engine.getPhase() != BattleEngine.Phase.FINISHED) {
            engine.perform(TYPELESS);
        }
        assertEquals(BattleEngine.EndReason.TURN_LIMIT, engine.getEndReason());
        assertEquals(GameRules.MAX_ACTIONS, engine.getActionCount());
        assertEquals(BattleEngine.Outcome.DRAW, engine.getOutcome());
    }

    @Test
    void cannotStartTwice() {
        BattleEngine engine = start(TestData.player("A", plain("a", DragonClass.STRIKE)),
                TestData.player("B", plain("b", DragonClass.STOKER)), new StubRandom());
        assertThrows(IllegalStateException.class, engine::start);
    }
}
