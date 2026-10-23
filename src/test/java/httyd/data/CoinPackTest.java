package httyd.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

class CoinPackTest {

    @Test
    void packsGetBiggerAndAreAllPositive() {
        int previous = 0;
        for (CoinPack pack : CoinPack.values()) {
            assertTrue(pack.getCoins() > previous, pack.name());
            previous = pack.getCoins();
        }
    }

    @Test
    void claimingAddsExactlyThePackAmount() {
        DragonRepository repo = DragonRepository.loadDefault();
        Profile profile = Profile.createNew(new GachaService(repo, new Random(1)));
        int before = profile.getCoins();
        int added = CoinPack.CHEST.claim(profile);
        assertEquals(CoinPack.CHEST.getCoins(), added);
        assertEquals(before + CoinPack.CHEST.getCoins(), profile.getCoins());
    }
}
