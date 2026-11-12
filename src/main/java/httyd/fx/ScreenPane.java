package httyd.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * Base class of the full-window screens: a dark patterned background, a content area and a title bar.
 */
public abstract class ScreenPane extends StackPane {
    /** The game window used for navigation. */
    protected final GameWindow window;
    /** The shared game objects. */
    protected final GameContext context;
    /** The area where a screen puts its own nodes (above the background). */
    protected final BorderPane content = new BorderPane();

    /**
     * Creates a screen with the dark striped background.
     *
     * @param window  the game window
     * @param context the shared game objects
     */
    protected ScreenPane(GameWindow window, GameContext context) {
        this(window, context, true);
    }

    /**
     * @param window    the game window
     * @param context   the shared game objects
     * @param patterned false if the screen paints its own background
     */
    protected ScreenPane(GameWindow window, GameContext context, boolean patterned) {
        this.window = window;
        this.context = context;
        setMinSize(GameWindow.WIDTH, GameWindow.HEIGHT);
        setPrefSize(GameWindow.WIDTH, GameWindow.HEIGHT);
        setMaxSize(GameWindow.WIDTH, GameWindow.HEIGHT);
        if (patterned) {
            getChildren().add(stripes());
        }
        getChildren().add(content);
    }

    /** Called every time the screen is shown, to refresh its data. */
    public void onShow() {
    }

    /** Called when another screen replaces this one, to stop animation timers. */
    public void onHide() {
    }

    private static Canvas stripes() {
        Canvas canvas = new Canvas(GameWindow.WIDTH, GameWindow.HEIGHT);
        canvas.setMouseTransparent(true);
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Theme.BACKGROUND);
        g.fillRect(0, 0, GameWindow.WIDTH, GameWindow.HEIGHT);
        g.setStroke(Color.rgb(255, 255, 255, 10 / 255.0));
        g.setLineWidth(2);
        for (double i = -GameWindow.HEIGHT; i < GameWindow.WIDTH; i += 36) {
            g.strokeLine(i, 0, i + GameWindow.HEIGHT, GameWindow.HEIGHT);
        }
        return canvas;
    }

    /**
     * Builds the title bar.
     *
     * @param title    the title text
     * @param backText the label of the back button, or {@code null} for none
     * @param back     what the back button does
     * @return the bar
     */
    protected HBox header(String title, String backText, Runnable back) {
        Label label = new Label(title);
        label.setTextFill(Color.WHITE);
        label.setFont(Theme.font(30, true));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bar = new HBox(label, spacer);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 20, 6, 20));
        if (backText != null) {
            RetroButton button = new RetroButton(backText, Theme.ACCENT_2);
            button.setFixedSize(170, 42);
            button.setOnAction(e -> {
                context.getSound().play(SoundPlayer.Sound.CLICK);
                back.run();
            });
            bar.getChildren().add(button);
        }
        return bar;
    }
}
