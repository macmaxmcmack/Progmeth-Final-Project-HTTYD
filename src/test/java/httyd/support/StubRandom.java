package httyd.support;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

/**
 * A random source that returns prepared values, so a test can decide exactly when a critical
 * hit or a dodge happens. When the prepared values run out it returns "nothing special":
 * {@code nextDouble()} gives 0.99 (no crit, no dodge) and {@code nextInt} gives 0.
 */
@SuppressWarnings("serial")
public class StubRandom extends Random {
    private final Deque<Double> values = new ArrayDeque<>();

    /** @param prepared the values returned by {@link #nextDouble()} in order */
    public StubRandom(double... prepared) {
        for (double v : prepared) {
            values.add(v);
        }
    }

    @Override
    public double nextDouble() {
        return values.isEmpty() ? 0.99 : values.poll();
    }

    @Override
    public int nextInt(int bound) {
        return 0;
    }

    @Override
    public boolean nextBoolean() {
        return true;
    }
}
