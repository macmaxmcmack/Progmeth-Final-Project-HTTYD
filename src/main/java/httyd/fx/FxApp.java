package httyd.fx;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * JavaFX application class: creates the shared game objects and opens the window.
 */
public final class FxApp extends Application {

    @Override
    public void start(Stage stage) {
        GameContext context = GameContext.createDefault();
        GameWindow window = new GameWindow(stage, context);
        window.showMenu();
        stage.show();

        if (getParameters().getRaw().contains("--smoke")) {
            PauseTransition wait = new PauseTransition(Duration.millis(800));
            wait.setOnFinished(e -> {
                WritableImage shot = stage.getScene().getRoot().snapshot(null, null);
                boolean ok = shot.getWidth() > 0 && shot.getHeight() > 0;
                System.out.println("smoke test " + (ok ? "OK" : "FAILED") + " " + shot.getWidth() + "x" + shot.getHeight());
                Platform.exit();
                System.exit(ok ? 0 : 1);
            });
            wait.play();
        }
    }

    @Override
    public void stop() {
        // sound threads are not daemons, so end the program explicitly
        System.exit(0);
    }
}
