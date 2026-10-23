package httyd.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import httyd.model.dragon.DragonClass;
import httyd.model.dragon.Stat;
import httyd.model.field.Battlefield;
import httyd.model.field.BattlefieldType;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class BattlefieldTest {

    @Test
    void homeClassGetsBuffAndEnemyGetsDebuff() {
        BattlefieldType thunder = BattlefieldType.THUNDERSTORM_PEAK;
        assertEquals(1.15, thunder.multiplier(DragonClass.STRIKE, Stat.ATK), 1e-9);
        assertEquals(1.10, thunder.multiplier(DragonClass.STRIKE, Stat.SPD), 1e-9);
        assertEquals(0.90, thunder.multiplier(DragonClass.BOULDER, Stat.DEF), 1e-9);
        assertEquals(1.0, thunder.multiplier(DragonClass.TIDAL, Stat.ATK), 1e-9);
    }

    @Test
    void plainsHasNoEffect() {
        for (DragonClass c : DragonClass.values()) {
            for (Stat s : Stat.values()) {
                assertEquals(1.0, BattlefieldType.PLAINS.multiplier(c, s), 1e-9);
            }
        }
    }

    @Test
    void deepCaveBuffsBoulderAttackAndDefence() {
        assertEquals(1.15, BattlefieldType.DEEP_CAVE.multiplier(DragonClass.BOULDER, Stat.ATK), 1e-9);
        assertEquals(1.15, BattlefieldType.DEEP_CAVE.multiplier(DragonClass.BOULDER, Stat.DEF), 1e-9);
        assertEquals(0.9, BattlefieldType.DEEP_CAVE.multiplier(DragonClass.SHARP, Stat.SPD), 1e-9);
    }

    @Test
    void everyClassHasAHomeField() {
        for (DragonClass c : DragonClass.values()) {
            BattlefieldType home = BattlefieldType.homeOf(c);
            assertTrue(home.multiplier(c, Stat.ATK) > 1.0, c + " should be buffed at home");
        }
    }

    @Test
    void terrainShiftLastsTwoActionsThenReturns() {
        Battlefield field = new Battlefield(BattlefieldType.PLAINS);
        field.shiftTo(BattlefieldType.OPEN_OCEAN);
        assertSame(BattlefieldType.OPEN_OCEAN, field.getCurrentField());
        assertFalse(field.endAction());
        assertSame(BattlefieldType.OPEN_OCEAN, field.getCurrentField());
        assertTrue(field.endAction());
        assertSame(BattlefieldType.PLAINS, field.getCurrentField());
        assertFalse(field.endAction());
    }

    @Test
    void newShiftOverridesOldOne() {
        Battlefield field = new Battlefield(BattlefieldType.PLAINS);
        field.shiftTo(BattlefieldType.OPEN_OCEAN);
        field.endAction();
        field.shiftTo(BattlefieldType.DEEP_CAVE);
        assertSame(BattlefieldType.DEEP_CAVE, field.getCurrentField());
        assertEquals(2, field.getOverrideTurnsLeft());
    }

    @Test
    void drawPicksOneOfThreeCandidates() {
        List<BattlefieldType> picks = List.of(BattlefieldType.DEEP_CAVE, BattlefieldType.OPEN_OCEAN);
        for (int seed = 0; seed < 20; seed++) {
            BattlefieldType result = Battlefield.draw(picks, BattlefieldType.PLAINS, new Random(seed));
            assertTrue(picks.contains(result) || result == BattlefieldType.PLAINS);
        }
    }
}
