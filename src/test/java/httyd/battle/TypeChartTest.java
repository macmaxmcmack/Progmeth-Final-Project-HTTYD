package httyd.battle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import httyd.model.dragon.DragonClass;
import org.junit.jupiter.api.Test;

class TypeChartTest {

    @Test
    void advantageAndDisadvantageAreMirrored() {
        assertEquals(1.25, TypeChart.multiplier(DragonClass.STRIKE, DragonClass.BOULDER), 1e-9);
        assertEquals(0.8, TypeChart.multiplier(DragonClass.BOULDER, DragonClass.STRIKE), 1e-9);
    }

    @Test
    void sameClassIsNeutral() {
        for (DragonClass c : DragonClass.values()) {
            assertEquals(1.0, TypeChart.multiplier(c, c), 1e-9);
        }
    }

    @Test
    void everyClassBeatsTwoAndLosesToTwo() {
        for (DragonClass a : DragonClass.values()) {
            int wins = 0;
            int losses = 0;
            for (DragonClass b : DragonClass.values()) {
                double m = TypeChart.multiplier(a, b);
                if (m > 1.0) {
                    wins++;
                }
                if (m < 1.0) {
                    losses++;
                }
            }
            assertEquals(2, wins, a + " wins");
            assertEquals(2, losses, a + " losses");
        }
    }
}
