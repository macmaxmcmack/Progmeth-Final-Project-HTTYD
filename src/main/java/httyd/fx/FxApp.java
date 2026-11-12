package httyd.fx;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * JavaFX application class. Step 1 of the port: a window that proves the toolchain works.
 */
public final class FxApp extends Application {

    /** Logical size of the game window (the old Swing window was the same size). */
    public static final double WIDTH = 1024;
    /** Logical height of the game window. */
    public static final double HEIGHT = 720;

    @Override
    public void start(Stage stage) {
        Label hello = new Label("HTTYD Dragon Battle - JavaFX " + System.getProperty("javafx.runtime.version"));
        hello.setStyle("-fx-font-size: 28px; -fx-text-fill: white;");
        StackPane root = new StackPane(hello);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1d2233;");

        stage.setTitle("HTTYD Dragon Battle");
        stage.setScene(new Scene(root, WIDTH, HEIGHT));
        stage.setResizable(false);
        stage.show();

        if (getParameters().getRaw().contains("--smoke")) {
            PauseTransition wait = new PauseTransition(Duration.millis(600));
            wait.setOnFinished(e -> {
                WritableImage shot = root.snapshot(null, null);
                boolean ok = shot.getWidth() > 0 && shot.getHeight() > 0;
                System.out.println("smoke test " + (ok ? "OK" : "FAILED") + " " + shot.getWidth() + "x" + shot.getHeight());
                Platform.exit();
                System.exit(ok ? 0 : 1);
            });
            wait.play();
        }
    }
}
