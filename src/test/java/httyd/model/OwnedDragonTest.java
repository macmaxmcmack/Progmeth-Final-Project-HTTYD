package httyd.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import httyd.model.dragon.DragonClass;
import httyd.model.dragon.DragonSpecies;
import httyd.model.dragon.Grade;
import httyd.model.dragon.Nature;
import httyd.model.dragon.OwnedDragon;
import httyd.model.dragon.Stat;
import httyd.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OwnedDragonTest {
    private DragonSpecies species;

    @BeforeEach
    void setUp() {
        species = TestData.species("t", DragonClass.STRIKE, 80, 90, 70, 60, 0.1, 6, "NONE", "NONE", "NONE");
    }

    @Test
    void battleHpIsBaseHpTimesThree() {
        OwnedDragon d = TestData.owned(1, species);
        assertEquals(80, d.getStat(Stat.HP));
        assertEquals(240, d.getBattleHp());
    }

    @Test
    void ivAndNatureChangeStats() {
        OwnedDragon d = new OwnedDragon(1, species, 0, 4, -3, 0, Nature.FIERCE, false, 1);
        assertEquals(99, d.getStat(Stat.ATK));
        assertEquals(64, d.getStat(Stat.DEF));
    }

    @Test
    void alphaDragonGetsFivePointsOnEveryStat() {
        OwnedDragon d = new OwnedDragon(1, species, 0, 0, 0, 0, Nature.CALM, true, 1);
        assertEquals(95, d.getStat(Stat.ATK));
        assertEquals(65, d.getStat(Stat.SPD));
    }

    @Test
    void gradeDependsOnTotalDeviation() {
        assertEquals(Grade.B, new OwnedDragon(1, species, -3, 0, 0, 1, Nature.CALM, false, 1).getGrade());
        assertEquals(Grade.A, new OwnedDragon(1, species, 1, 1, 1, 1, Nature.CALM, false, 1).getGrade());
        assertEquals(Grade.SSS, new OwnedDragon(1, species, 4, 4, 2, 2, Nature.CALM, false, 1).getGrade());
    }

    @Test
    void starsAddHpShotsAndTitanWing() {
        OwnedDragon d = TestData.owned(1, species);
        assertEquals(6, d.getShotLimit());
        d.starUp();
        assertEquals(84, d.getStat(Stat.HP));
        assertEquals(7, d.getShotLimit());
        assertFalse(d.isTitanWing());
        d.starUp();
        assertTrue(d.isTitanWing());
        assertEquals(1.5, d.getPassiveStrength(), 1e-9);
        assertThrows(IllegalStateException.class, d::starUp);
    }

    @Test
    void invalidStarIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new OwnedDragon(1, species, 0, 0, 0, 0, Nature.CALM, false, 0));
    }

    @Test
    void saveStringContainsAllFields() {
        OwnedDragon d = new OwnedDragon(7, species, 1, -2, 3, 4, Nature.AGILE, true, 2);
        assertEquals("7,t,1,-2,3,4,AGILE,1,2", d.toSaveString());
    }
}
