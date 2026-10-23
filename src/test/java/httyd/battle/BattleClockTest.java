package httyd.battle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import httyd.support.FakeTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BattleClockTest {
    private FakeTime time;
    private BattleClock clock;

    @BeforeEach
    void setUp() {
        time = new FakeTime();
        clock = new BattleClock(time);
    }

    @Test
    void turnExpiresAfterSixtySeconds() {
        clock.start(Side.PLAYER);
        time.advanceSeconds(59);
        assertFalse(clock.isExpired(Side.PLAYER));
        time.advanceSeconds(2);
        assertTrue(clock.isExpired(Side.PLAYER));
    }

    @Test
    void usedTimeIsSubtractedFromTotal() {
        clock.start(Side.PLAYER);
        time.advanceSeconds(50);
        clock.stop();
        assertEquals(190_000L, clock.totalLeftMillis(Side.PLAYER));
        assertEquals(240_000L, clock.totalLeftMillis(Side.OPPONENT));
    }

    @Test
    void totalClockExpiresAfterManyShortTurns() {
        for (int i = 0; i < 5; i++) {
            clock.start(Side.PLAYER);
            time.advanceSeconds(50);
            clock.stop();
        }
        clock.start(Side.PLAYER);
        assertTrue(clock.isExpired(Side.PLAYER));
    }

    @Test
    void stoppedClockNeverExpires() {
        time.advanceSeconds(1000);
        assertFalse(clock.isExpired(Side.PLAYER));
    }

    @Test
    void startingOtherSideStopsTheFirst() {
        clock.start(Side.PLAYER);
        time.advanceSeconds(10);
        clock.start(Side.OPPONENT);
        assertEquals(Side.OPPONENT, clock.getRunning());
        assertEquals(230_000L, clock.totalLeftMillis(Side.PLAYER));
    }
}
