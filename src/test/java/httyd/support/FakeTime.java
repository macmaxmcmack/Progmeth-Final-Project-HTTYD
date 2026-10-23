package httyd.support;

import httyd.battle.TimeSource;

/** A clock that only moves when the test says so. */
public class FakeTime implements TimeSource {
    private long now;

    @Override
    public long nowMillis() {
        return now;
    }

    /** @param seconds how far to move the clock forward */
    public void advanceSeconds(long seconds) {
        now += seconds * 1000L;
    }
}
