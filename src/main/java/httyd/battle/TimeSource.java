package httyd.battle;

/** Source of time, replaceable in unit tests. */
@FunctionalInterface
public interface TimeSource {
    /** @return the current time in milliseconds */
    long nowMillis();

    /** @return a time source that reads the system clock */
    static TimeSource system() {
        return System::currentTimeMillis;
    }
}
