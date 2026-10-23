package httyd.battle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DamageCalculatorTest {

    @Test
    void baseDamageUsesDiminishingReturnFormula() {
        assertEquals(50.0, DamageCalculator.baseDamage(100, 100), 1e-9);
        assertEquals(100.0, DamageCalculator.baseDamage(100, 0), 1e-9);
        assertEquals(48.469, DamageCalculator.baseDamage(95, 96), 0.001);
    }

    @Test
    void highDefenceNeverMakesDragonImmortal() {
        assertEquals(1, DamageCalculator.toDamage(DamageCalculator.baseDamage(1, 10_000)));
    }

    @Test
    void dodgeComesFromSpeedDifferenceAndIsCapped() {
        assertEquals(0.0, DamageCalculator.speedDodgePercent(100, 90), 1e-9);
        assertEquals(5.0, DamageCalculator.speedDodgePercent(80, 100), 1e-9);
        assertEquals(25.0, DamageCalculator.speedDodgePercent(10, 500), 1e-9);
    }

    @Test
    void totalDodgeIsCappedAtForty() {
        assertEquals(40.0, DamageCalculator.totalDodgePercent(25, 30), 1e-9);
        assertEquals(10.0, DamageCalculator.totalDodgePercent(0, 10), 1e-9);
    }

    @Test
    void damageIsAtLeastOne() {
        assertEquals(1, DamageCalculator.toDamage(0.2));
        assertEquals(48, DamageCalculator.toDamage(47.6));
    }
}
