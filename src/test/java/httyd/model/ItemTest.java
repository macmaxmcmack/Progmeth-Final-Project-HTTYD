package httyd.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import httyd.battle.BattleLog;
import httyd.battle.Combatant;
import httyd.battle.DotType;
import httyd.battle.Side;
import httyd.model.dragon.DragonClass;
import httyd.model.dragon.Stat;
import httyd.model.item.Item;
import httyd.model.item.ItemType;
import httyd.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ItemTest {
    private Combatant dragon;
    private BattleLog log;

    @BeforeEach
    void setUp() {
        dragon = new Combatant(Side.PLAYER, TestData.owned(1, TestData.basic("d", DragonClass.STRIKE)));
        log = new BattleLog();
    }

    @Test
    void arcticCharHealsThirtyPercent() {
        Item char1 = ItemType.ARCTIC_CHAR.create();
        assertFalse(char1.isUseful(dragon));
        assertFalse(char1.use(dragon, log));
        dragon.takeDamage(200);
        assertTrue(char1.isUseful(dragon));
        assertTrue(char1.use(dragon, log));
        assertEquals(190, dragon.getCurrentHp());
    }

    @Test
    void fireGelAddsThreeShots() {
        Item gel = ItemType.FIRE_GEL.create();
        assertFalse(gel.isUseful(dragon));
        dragon.spendShot();
        dragon.spendShot();
        dragon.spendShot();
        assertTrue(gel.use(dragon, log));
        assertEquals(3, dragon.getShots());
    }

    @Test
    void dragonRootRaisesAttackByTwentyPercent() {
        assertTrue(ItemType.DRAGON_ROOT.create().use(dragon, log));
        assertEquals(120.0, dragon.effectiveStat(Stat.ATK, null, null), 1e-9);
    }

    @Test
    void ironDustRaisesDefenceByTwentyFivePercent() {
        assertTrue(ItemType.IRON_DUST.create().use(dragon, log));
        assertEquals(125.0, dragon.effectiveStat(Stat.DEF, null, null), 1e-9);
    }

    @Test
    void dragonNipOnlyUsefulWhenThereIsSomethingToClean() {
        Item nip = ItemType.DRAGON_NIP.create();
        assertFalse(nip.isUseful(dragon));
        dragon.applyDot(DotType.POISON, 0.05, 2);
        assertTrue(nip.isUseful(dragon));
        assertTrue(nip.use(dragon, log));
        assertFalse(dragon.hasDot(DotType.POISON));
    }

    @Test
    void catalogueHasFiveDifferentItemsWithDisplayNames() {
        assertEquals(5, ItemType.values().length);
        for (ItemType type : ItemType.values()) {
            Item item = type.create();
            assertFalse(item.getDisplayName().isBlank());
            assertFalse(item.getDescription().isBlank());
        }
    }
}
