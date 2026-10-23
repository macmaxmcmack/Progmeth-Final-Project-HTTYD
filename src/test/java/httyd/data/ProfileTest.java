package httyd.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import httyd.battle.BattleEngine;
import httyd.model.GameRules;
import httyd.model.dragon.Nature;
import httyd.model.dragon.OwnedDragon;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProfileTest {
    private DragonRepository repo;
    private GachaService gacha;
    private Profile profile;

    @BeforeEach
    void setUp() {
        repo = DragonRepository.loadDefault();
        gacha = new GachaService(repo, new Random(3));
        profile = Profile.createNew(gacha);
    }

    private OwnedDragon copyOf(String speciesId, int id) {
        return new OwnedDragon(id, repo.byId(speciesId), 0, 0, 0, 0, Nature.CALM, false, 1);
    }

    @Test
    void newProfileHasCoinsAndStarterDragons() {
        assertEquals(Profile.STARTING_COINS, profile.getCoins());
        assertEquals(Profile.STARTER_DRAGONS, profile.getDragons().size());
    }

    @Test
    void buyingAnEggCostsCoinsAndAddsADragon() {
        int before = profile.getDragons().size();
        OwnedDragon d = profile.buyEgg(gacha);
        assertEquals(Profile.STARTING_COINS - GameRules.EGG_PRICE, profile.getCoins());
        assertEquals(before + 1, profile.getDragons().size());
        assertTrue(profile.find(d.getId()).isPresent());
    }

    @Test
    void cannotBuyWithoutCoins() {
        profile.setCoins(10);
        assertFalse(profile.canAffordEgg());
        assertThrows(IllegalStateException.class, () -> profile.buyEgg(gacha));
    }

    @Test
    void idsNeverRepeat() {
        profile.setCoins(1000);
        int a = profile.buyEgg(gacha).getId();
        int b = profile.buyEgg(gacha).getId();
        assertTrue(a != b);
    }

    @Test
    void fusionConsumesTheDuplicateAndAddsAStar() {
        Profile p = new Profile();
        p.add(copyOf("skrill", 1));
        p.add(copyOf("skrill", 2));
        assertTrue(p.canFuse(1, 2));
        p.fuse(1, 2);
        assertEquals(2, p.find(1).orElseThrow().getStar());
        assertTrue(p.find(2).isEmpty());
    }

    @Test
    void fusionRulesAreChecked() {
        Profile p = new Profile();
        p.add(copyOf("skrill", 1));
        p.add(copyOf("toothless", 2));
        p.add(copyOf("skrill", 3));
        assertFalse(p.canFuse(1, 2));
        assertFalse(p.canFuse(1, 1));
        assertFalse(p.canFuse(1, 99));
        assertThrows(IllegalStateException.class, () -> p.fuse(1, 2));
        p.fuse(1, 3);
        p.add(copyOf("skrill", 4));
        p.fuse(1, 4);
        assertTrue(p.find(1).orElseThrow().isTitanWing());
        p.add(copyOf("skrill", 5));
        assertFalse(p.canFuse(1, 5));
    }

    @Test
    void resultsPayCoinsAndCountRecord() {
        profile.setCoins(0);
        assertEquals(60, profile.recordResult(BattleEngine.Outcome.PLAYER_WIN));
        assertEquals(20, profile.recordResult(BattleEngine.Outcome.OPPONENT_WIN));
        assertEquals(25, profile.recordResult(BattleEngine.Outcome.DRAW));
        assertEquals(105, profile.getCoins());
        assertEquals(1, profile.getWins());
        assertEquals(1, profile.getLosses());
        assertEquals(1, profile.getDraws());
    }

    @Test
    void addCoinsIncreasesTheBalance() {
        int before = profile.getCoins();
        assertEquals(500, profile.addCoins(500));
        assertEquals(before + 500, profile.getCoins());
    }

    @Test
    void addCoinsRejectsZeroAndNegativeAmounts() {
        assertThrows(IllegalArgumentException.class, () -> profile.addCoins(0));
        assertThrows(IllegalArgumentException.class, () -> profile.addCoins(-5));
    }

    @Test
    void addCoinsStopsAtTheCap() {
        profile.setCoins(GameRules.MAX_COINS - 10);
        assertEquals(10, profile.addCoins(5_000));
        assertEquals(GameRules.MAX_COINS, profile.getCoins());
        assertEquals(0, profile.addCoins(100));
    }

    @Test
    void topUpMakesEggsAffordableAgain() {
        profile.setCoins(0);
        assertFalse(profile.canAffordEgg());
        CoinPack.POUCH.claim(profile);
        assertTrue(profile.canAffordEgg());
    }
}
