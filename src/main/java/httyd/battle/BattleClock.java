package httyd.battle;

import httyd.model.GameRules;
import java.util.EnumMap;
import java.util.Map;

/**
 * The two game clocks: a short clock for the current turn and a total clock for each side.
 * Only the side that is thinking uses time.
 */
public class BattleClock {
    private final TimeSource time;
    private final long turnLimitMillis;
    private final Map<Side, Long> totalLeft = new EnumMap<>(Side.class);
    private Side running;
    private long runningSince;

    /**
     * Creates a clock with the default limits of the game rules.
     *
     * @param time the time source
     */
    public BattleClock(TimeSource time) {
        this(time, GameRules.TURN_SECONDS * 1000L, GameRules.TOTAL_SECONDS * 1000L);
    }

    /**
     * @param time            the time source
     * @param turnLimitMillis limit of one turn
     * @param totalMillis     total time of each side
     */
    public BattleClock(TimeSource time, long turnLimitMillis, long totalMillis) {
        this.time = time;
        this.turnLimitMillis = turnLimitMillis;
        for (Side s : Side.values()) {
            totalLeft.put(s, totalMillis);
        }
    }

    /**
     * Starts the clock of a side.
     *
     * @param side the side that must now decide
     */
    public void start(Side side) {
        stop();
        running = side;
        runningSince = time.nowMillis();
    }

    /** Stops the running clock and subtracts the used time from the total. */
    public void stop() {
        if (running != null) {
            long used = time.nowMillis() - runningSince;
            totalLeft.merge(running, -used, Long::sum);
            running = null;
        }
    }

    /**
     * Stops the running clock without charging the elapsed time. The GUI uses this while it
     * plays the animation of a turn, because watching an animation is not thinking time.
     */
    public void discard() {
        running = null;
    }

    /**
     * Starts the clock of a side from now, without charging any earlier time.
     *
     * @param side the side that must now decide
     */
    public void startFresh(Side side) {
        running = side;
        runningSince = time.nowMillis();
    }

    /** @return the side whose clock is running, or {@code null} */
    public Side getRunning() {
        return running;
    }

    /**
     * @param side the side
     * @return milliseconds left in the current turn, or the full limit if not running
     */
    public long turnLeftMillis(Side side) {
        if (running != side) {
            return turnLimitMillis;
        }
        return turnLimitMillis - (time.nowMillis() - runningSince);
    }

    /**
     * @param side the side
     * @return milliseconds left in the total clock
     */
    public long totalLeftMillis(Side side) {
        long left = totalLeft.get(side);
        if (running == side) {
            left -= time.nowMillis() - runningSince;
        }
        return left;
    }

    /**
     * @param side the side
     * @return true if the short clock or the total clock of the running side ran out
     */
    public boolean isExpired(Side side) {
        return running == side && (turnLeftMillis(side) <= 0 || totalLeftMillis(side) <= 0);
    }
}
