package httyd.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import httyd.battle.BattleLog;
import httyd.battle.Combatant;
import httyd.battle.Side;
import httyd.model.dragon.DragonClass;
import httyd.model.dragon.DragonSpecies;
import httyd.model.dragon.Stat;
import httyd.model.skill.MoveKind;
import httyd.model.skill.PassiveFactory;
import httyd.model.skill.PassiveSkill;
import httyd.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PassiveTest {
    private BattleLog log;
    private Combatant foe;

    @BeforeEach
    void setUp() {
        log = new BattleLog();
        foe = new Combatant(Side.OPPONENT, TestData.owned(9, TestData.basic("foe", DragonClass.TIDAL)));
    }

    private Combatant with(String passive) {
        DragonSpecies s = TestData.species("p", DragonClass.STRIKE, 100, 100, 100, 100, 0, 3, "NONE", "NONE", passive);
        return new Combatant(Side.PLAYER, TestData.owned(1, s));
    }

    @Test
    void reflectDamageSendsBackAPercentage() {
        Combatant self = with("REFLECT_DMG:15");
        self.getPassive().onAttacked(self, foe, 100, MoveKind.TYPELESS, log);
        assertEquals(15, 300 - foe.getCurrentHp());
    }

    @Test
    void reflectAttackOnlyWorksAgainstPlainAttacks() {
        Combatant self = with("REFLECT_ATK:15");
        self.getPassive().onAttacked(self, foe, 50, MoveKind.SIGNATURE, log);
        assertEquals(300, foe.getCurrentHp());
        self.getPassive().onAttacked(self, foe, 50, MoveKind.TYPELESS, log);
        assertEquals(15, 300 - foe.getCurrentHp());
    }

    @Test
    void critResistOnlyReducesCriticalHits() {
        PassiveSkill p = with("CRIT_RESIST:20").getPassive();
        assertEquals(0.8, p.incomingDamageMultiplier(MoveKind.TYPELESS, true), 1e-9);
        assertEquals(1.0, p.incomingDamageMultiplier(MoveKind.TYPELESS, false), 1e-9);
    }

    @Test
    void auraLowersTheEnemyDefenceOnly() {
        PassiveSkill p = with("AURA:DEF:10").getPassive();
        assertEquals(0.9, p.statMultiplierOnOpponent(Stat.DEF), 1e-9);
        assertEquals(1.0, p.statMultiplierOnOpponent(Stat.ATK), 1e-9);
    }

    @Test
    void sparksJetStopsAtThreeStacks() {
        Combatant self = with("SPARKS:5:3");
        for (int i = 0; i < 5; i++) {
            self.getPassive().onMoveUsed(self, MoveKind.SIGNATURE, log);
        }
        assertEquals(115.0, self.effectiveStat(Stat.SPD, null, null), 1e-9);
        self.getPassive().onMoveUsed(self, MoveKind.TYPELESS, log);
        assertEquals(115.0, self.effectiveStat(Stat.SPD, null, null), 1e-9);
    }

    @Test
    void defStackStopsNearTheLimit() {
        Combatant self = with("DEF_STACK:2:10");
        for (int i = 0; i < 20; i++) {
            self.getPassive().onAttacked(self, foe, 1, MoveKind.TYPELESS, log);
        }
        double def = self.effectiveStat(Stat.DEF, null, null);
        assertTrue(def >= 110.0 && def <= 112.0, "def was " + def);
    }

    @Test
    void lazySlumberHealsOnlyAfterATurnWithoutSignature() {
        Combatant self = with("LAZY:5");
        self.takeDamage(100);
        self.getPassive().onTurnStart(self, log);
        assertEquals(200, self.getCurrentHp());
        self.getPassive().onMoveUsed(self, MoveKind.SIGNATURE, log);
        self.getPassive().onTurnStart(self, log);
        assertEquals(200, self.getCurrentHp());
        self.getPassive().onTurnStart(self, log);
        assertEquals(215, self.getCurrentHp());
    }

    @Test
    void debuffImmunityBlocksOnlyThatStat() {
        Combatant self = with("IMMUNE:SPD");
        assertFalse(self.addModifier(Stat.SPD, -0.3, 0, 3, false));
        assertTrue(self.addModifier(Stat.ATK, -0.3, 0, 3, false));
    }

    @Test
    void surviveOnceSavesFromOneLethalHit() {
        Combatant self = with("SURVIVE");
        self.takeDamage(999);
        assertEquals(1, self.getCurrentHp());
        self.takeDamage(999);
        assertTrue(self.isFainted());
    }

    @Test
    void lowHpDefenceAppearsBelowThreshold() {
        Combatant self = with("LOWHP_DEF:15:40");
        assertEquals(100.0, self.effectiveStat(Stat.DEF, foe, null), 1e-9);
        self.takeDamage(200);
        assertEquals(115.0, self.effectiveStat(Stat.DEF, foe, null), 1e-9);
    }

    @Test
    void fasterAttackNeedsHigherSpeed() {
        Combatant self = with("FASTER_ATK:8");
        assertEquals(100.0, self.effectiveStat(Stat.ATK, foe, null), 1e-9);
        Combatant slow = new Combatant(Side.OPPONENT, TestData.owned(8,
                TestData.species("slow", DragonClass.TIDAL, 100, 100, 100, 50, 0, 3, "NONE", "NONE", "NONE")));
        assertEquals(108.0, self.effectiveStat(Stat.ATK, slow, null), 1e-9);
    }

    @Test
    void dodgeBonusCanBeLimitedToFirstTurn() {
        Combatant self = with("DODGE:10:1");
        assertEquals(10.0, self.getDodgeBonus(), 1e-9);
        self.endOwnTurn(log);
        assertEquals(0.0, self.getDodgeBonus(), 1e-9);
        assertEquals(8.0, with("DODGE:8:0").getDodgeBonus(), 1e-9);
    }

    @Test
    void titanWingMakesPassiveFiftyPercentStronger() {
        PassiveSkill normal = PassiveFactory.create("REDUCE_ALL:10", "n", "d", 1.0);
        PassiveSkill titan = PassiveFactory.create("REDUCE_ALL:10", "n", "d", 1.5);
        assertEquals(0.90, normal.incomingDamageMultiplier(MoveKind.TYPELESS, false), 1e-9);
        assertEquals(0.85, titan.incomingDamageMultiplier(MoveKind.TYPELESS, false), 1e-9);
    }

    @Test
    void unknownPassiveThrowsAndNoneIsHarmless() {
        assertThrows(IllegalArgumentException.class, () -> PassiveFactory.create("FLY", "n", "d", 1.0));
        PassiveSkill none = PassiveFactory.create("NONE", "n", "d", 1.0);
        assertEquals(0.0, none.dodgeBonus(foe), 1e-9);
    }
}
