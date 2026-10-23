package httyd.battle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import httyd.model.GameRules;
import httyd.model.dragon.DragonClass;
import httyd.model.dragon.Stat;
import httyd.model.field.Battlefield;
import httyd.model.field.BattlefieldType;
import httyd.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CombatantTest {
    private Combatant strike;
    private Combatant boulder;
    private BattleLog log;

    @BeforeEach
    void setUp() {
        strike = new Combatant(Side.PLAYER, TestData.owned(1, TestData.basic("strike", DragonClass.STRIKE)));
        boulder = new Combatant(Side.OPPONENT, TestData.owned(2, TestData.basic("boulder", DragonClass.BOULDER)));
        log = new BattleLog();
    }

    @Test
    void startsWithFullHpShotsAndEmptyCharge() {
        assertEquals(300, strike.getMaxHp());
        assertEquals(300, strike.getCurrentHp());
        assertEquals(3, strike.getShots());
        assertEquals(0, strike.getUltimateCharge());
        assertFalse(strike.isFainted());
    }

    @Test
    void battlefieldChangesEffectiveStats() {
        Battlefield thunder = new Battlefield(BattlefieldType.THUNDERSTORM_PEAK);
        assertEquals(115.0, strike.effectiveStat(Stat.ATK, boulder, thunder), 1e-9);
        assertEquals(90.0, boulder.effectiveStat(Stat.DEF, strike, thunder), 1e-9);
        assertEquals(100.0, strike.effectiveStat(Stat.DEF, boulder, thunder), 1e-9);
    }

    @Test
    void percentAndFlatModifiersAreAdded() {
        strike.addModifier(Stat.ATK, 0.2, 10, -1, true);
        assertEquals(132.0, strike.effectiveStat(Stat.ATK, boulder, null), 1e-9);
    }

    @Test
    void newDebuffReplacesOldOneInsteadOfStacking() {
        boulder.addModifier(Stat.SPD, -0.25, 0, 3, false);
        boulder.addModifier(Stat.SPD, -0.15, 0, 3, false);
        assertEquals(85.0, boulder.effectiveStat(Stat.SPD, strike, null), 1e-9);
    }

    @Test
    void selfBuffCoversTheNextTwoTurns() {
        strike.addModifier(Stat.ATK, 0.2, 0, 2, true);
        strike.endOwnTurn(log);
        assertEquals(120.0, strike.effectiveStat(Stat.ATK, null, null), 1e-9);
        strike.endOwnTurn(log);
        assertEquals(120.0, strike.effectiveStat(Stat.ATK, null, null), 1e-9);
        strike.endOwnTurn(log);
        assertEquals(100.0, strike.effectiveStat(Stat.ATK, null, null), 1e-9);
    }

    @Test
    void debuffFromEnemyLastsExactlyItsTurns() {
        boulder.addModifier(Stat.DEF, -0.3, 0, 2, false);
        boulder.endOwnTurn(log);
        assertEquals(70.0, boulder.effectiveStat(Stat.DEF, null, null), 1e-9);
        boulder.endOwnTurn(log);
        assertEquals(100.0, boulder.effectiveStat(Stat.DEF, null, null), 1e-9);
    }

    @Test
    void burnTicksOnOwnTurnsThenEnds() {
        strike.applyDot(DotType.BURN, 0.10, 2);
        assertTrue(strike.hasDot(DotType.BURN));
        strike.endOwnTurn(log);
        assertEquals(270, strike.getCurrentHp());
        strike.endOwnTurn(log);
        assertEquals(240, strike.getCurrentHp());
        assertFalse(strike.hasDot(DotType.BURN));
        strike.endOwnTurn(log);
        assertEquals(240, strike.getCurrentHp());
    }

    @Test
    void sameDotDoesNotStack() {
        strike.applyDot(DotType.POISON, 0.05, 2);
        strike.applyDot(DotType.POISON, 0.05, 2);
        strike.endOwnTurn(log);
        assertEquals(285, strike.getCurrentHp());
    }

    @Test
    void cleanseRemovesDotsAndDebuffsButKeepsBuffs() {
        strike.applyDot(DotType.BURN, 0.1, 2);
        strike.addModifier(Stat.ATK, -0.2, 0, 3, false);
        strike.addModifier(Stat.DEF, 0.25, 0, 2, true);
        assertTrue(strike.hasCleansableStatus());
        assertTrue(strike.cleanse());
        assertFalse(strike.hasCleansableStatus());
        assertEquals(100.0, strike.effectiveStat(Stat.ATK, null, null), 1e-9);
        assertEquals(125.0, strike.effectiveStat(Stat.DEF, null, null), 1e-9);
        assertFalse(strike.cleanse());
    }

    @Test
    void damageAndHealStayInsideBounds() {
        assertEquals(100, strike.takeDamage(100));
        assertEquals(50, strike.heal(50));
        assertEquals(50, strike.heal(1000));
        assertEquals(300, strike.getCurrentHp());
        assertEquals(300, strike.takeDamage(1000));
        assertTrue(strike.isFainted());
        assertEquals(0, strike.heal(10));
        assertThrows(IllegalArgumentException.class, () -> strike.takeDamage(-1));
    }

    @Test
    void shotsAreSpentAndRefilledUpToMaximum() {
        strike.spendShot();
        strike.spendShot();
        assertEquals(1, strike.getShots());
        assertEquals(2, strike.addShots(5));
        assertEquals(3, strike.getShots());
        strike.spendShot();
        strike.spendShot();
        strike.spendShot();
        assertThrows(IllegalStateException.class, strike::spendShot);
    }

    @Test
    void ultimateChargeFillsAndResets() {
        for (int i = 0; i < GameRules.ULTIMATE_CHARGE_NEEDED; i++) {
            assertFalse(strike.isUltimateReady());
            strike.gainCharge();
        }
        assertTrue(strike.isUltimateReady());
        strike.gainCharge();
        assertEquals(GameRules.ULTIMATE_CHARGE_NEEDED, strike.getUltimateCharge());
        strike.resetCharge();
        assertFalse(strike.isUltimateReady());
    }

    @Test
    void dodgeBonusLastsItsTurns() {
        strike.addDodgeBonus(25, 2);
        assertEquals(25.0, strike.getDodgeBonus(), 1e-9);
        strike.endOwnTurn(log);
        strike.endOwnTurn(log);
        assertEquals(25.0, strike.getDodgeBonus(), 1e-9);
        strike.endOwnTurn(log);
        assertEquals(0.0, strike.getDodgeBonus(), 1e-9);
    }
}
