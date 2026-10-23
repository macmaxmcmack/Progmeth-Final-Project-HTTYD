package httyd.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import httyd.data.DragonRepository;
import httyd.model.dragon.DragonSpecies;
import httyd.model.field.BattlefieldType;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class ImageFactoryTest {
    private static final DragonRepository REPO = DragonRepository.loadDefault();

    @Test
    void everySpeciesHasASprite() {
        for (DragonSpecies species : REPO.all()) {
            BufferedImage img = SpriteFactory.dragon(species, true, 2);
            assertNotNull(img, species.getId());
            assertTrue(img.getWidth() > 0 && img.getHeight() > 0, species.getId());
        }
    }

    @Test
    void scaleMultipliesTheSize() {
        DragonSpecies species = REPO.all().get(0);
        BufferedImage one = SpriteFactory.dragon(species, true, 1);
        BufferedImage three = SpriteFactory.dragon(species, true, 3);
        assertEquals(one.getWidth() * 3, three.getWidth());
        assertEquals(one.getHeight() * 3, three.getHeight());
    }

    @Test
    void facingLeftIsADifferentPicture() {
        DragonSpecies species = REPO.all().get(0);
        BufferedImage right = SpriteFactory.dragon(species, true, 1);
        BufferedImage left = SpriteFactory.dragon(species, false, 1);
        boolean different = false;
        for (int x = 0; x < right.getWidth() && !different; x++) {
            for (int y = 0; y < right.getHeight(); y++) {
                if (right.getRGB(x, y) != left.getRGB(x, y)) {
                    different = true;
                    break;
                }
            }
        }
        assertTrue(different);
    }

    @Test
    void spritesAreCached() {
        DragonSpecies species = REPO.all().get(1);
        assertSame(SpriteFactory.dragon(species, true, 2), SpriteFactory.dragon(species, true, 2));
    }

    @Test
    void differentSpeciesLookDifferent() {
        BufferedImage a = SpriteFactory.dragon(REPO.byId("toothless"), true, 1);
        BufferedImage b = SpriteFactory.dragon(REPO.byId("stormfly"), true, 1);
        int differences = 0;
        for (int x = 0; x < Math.min(a.getWidth(), b.getWidth()); x++) {
            for (int y = 0; y < Math.min(a.getHeight(), b.getHeight()); y++) {
                if (a.getRGB(x, y) != b.getRGB(x, y)) {
                    differences++;
                }
            }
        }
        assertNotEquals(0, differences);
    }

    @Test
    void eggFramesExist() {
        for (int crack = 0; crack <= 3; crack++) {
            assertNotNull(SpriteFactory.egg(2, crack));
        }
    }

    @Test
    void everyBattlefieldHasABackdrop() {
        for (BattlefieldType type : BattlefieldType.values()) {
            BufferedImage img = BackgroundFactory.backdrop(type);
            assertEquals(BackgroundFactory.BASE_W, img.getWidth(), type.name());
            assertEquals(BackgroundFactory.BASE_H, img.getHeight(), type.name());
        }
    }
}
