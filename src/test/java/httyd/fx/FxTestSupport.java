package httyd.fx;

import javafx.application.Platform;
import org.junit.jupiter.api.Assumptions;

/** Starts the JavaFX toolkit for tests that create images or canvases; skips the test if it cannot start. */
final class FxTestSupport {
    private FxTestSupport() {
    }

    static void start() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException alreadyRunning) {
            // fine: the toolkit was started by an earlier test
        } catch (Throwable cannotStart) {
            Assumptions.assumeTrue(false, "JavaFX toolkit is not available here: " + cannotStart);
        }
    }
}
