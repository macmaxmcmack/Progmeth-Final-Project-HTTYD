package httyd.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import httyd.model.dragon.DragonSpecies;
import httyd.model.dragon.Nature;
import httyd.model.dragon.OwnedDragon;
import httyd.model.dragon.Stat;
import httyd.model.dragon.Tier;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GachaServiceTest {
    private DragonRepository repo;
    private GachaService gacha;

    @BeforeEach
    void setUp() {
        repo = DragonRepository.loadDefault();
        gacha = new GachaService(repo, new Random(7));
    }

    @Test
    void ivStaysBetweenMinusThreeAndFour() {
        for (int i = 0; i < 2000; i++) {
            OwnedDragon d = gacha.rollDragon(i);
            for (Stat s : Stat.values()) {
                assertTrue(d.getIv(s) >= -3 && d.getIv(s) <= 4);
            }
            assertEquals(1, d.getStar());
        }
    }

    @Test
    void alphaDragonsAreRoughlyFivePercent() {
        int alpha = 0;
        int n = 10_000;
        for (int i = 0; i < n; i++) {
            if (gacha.rollDragon(i).isAlpha()) {
                alpha++;
            }
        }
        assertTrue(alpha > n * 0.03 && alpha < n * 0.07, "alpha count " + alpha);
    }

    @Test
    void allNaturesCanAppear() {
        Set<Nature> seen = EnumSet.noneOf(Nature.class);
        for (int i = 0; i < 500; i++) {
            seen.add(gacha.rollDragon(i).getNature());
        }
        assertEquals(Nature.values().length, seen.size());
    }

    @Test
    void tierRatesFollowTheDropWeights() {
        int s = 0;
        int c = 0;
        int n = 20_000;
        for (int i = 0; i < n; i++) {
            Tier tier = gacha.pickSpecies(100).getTier();
            if (tier == Tier.S) {
                s++;
            }
            if (tier == Tier.C) {
                c++;
            }
        }
        assertTrue(s > n * 0.05 && s < n * 0.09, "S share " + s);
        assertTrue(c > n * 0.40 && c < n * 0.50, "C share " + c);
    }

    @Test
    void higherRarityPercentFavoursRareTiers() {
        int normal = 0;
        int boosted = 0;
        for (int i = 0; i < 10_000; i++) {
            if (gacha.pickSpecies(100).getTier() == Tier.S) {
                normal++;
            }
            if (gacha.pickSpecies(200).getTier() == Tier.S) {
                boosted++;
            }
        }
        assertTrue(boosted > normal * 1.5);
    }

    @Test
    void chosenSpeciesIsKept() {
        DragonSpecies skrill = repo.byId("skrill");
        assertEquals(skrill, gacha.rollDragon(5, skrill).getSpecies());
    }
}
