package httyd.app;

import httyd.fx.FxApp;
import javafx.application.Application;

/**
 * Entry point of the game "HTTYD Dragon Battle".
 *
 * <p>This class deliberately does <em>not</em> extend {@link Application}: when the main class of a jar
 * extends Application, {@code java -jar} fails with "JavaFX runtime components are missing". The real
 * application class is {@link FxApp}.
 */
public final class Main {

    private Main() {
    }

    /**
     * Starts the JavaFX application.
     *
     * @param args {@code --smoke} opens the window, takes a snapshot and exits (used by the automatic tests)
     */
    public static void main(String[] args) {
        Application.launch(FxApp.class, args);
    }
}
