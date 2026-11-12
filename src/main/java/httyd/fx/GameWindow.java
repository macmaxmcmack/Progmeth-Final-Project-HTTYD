package httyd.fx;

import httyd.battle.BattleSession;
import httyd.model.field.BattlefieldType;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * The main window. The game is laid out in a fixed 1024 x 720 area that is scaled to fit the window,
 * so it fits small laptop screens and stays sharp on big ones. All screens live in a map and are
 * swapped into that area.
 */
public final class GameWindow {
    /** Logical width of the game area. */
    public static final double WIDTH = 1024;
    /** Logical height of the game area. */
    public static final double HEIGHT = 720;

    private final Stage stage;
    private final GameContext context;
    private final StackPane area = new StackPane();
    private final Map<String, ScreenPane> screens = new HashMap<>();
    private ScreenPane current;

    /**
     * @param stage   the JavaFX stage
     * @param context the shared game objects
     */
    public GameWindow(Stage stage, GameContext context) {
        this.stage = stage;
        this.context = context;

        area.setMinSize(WIDTH, HEIGHT);
        area.setPrefSize(WIDTH, HEIGHT);
        area.setMaxSize(WIDTH, HEIGHT);
        StackPane viewport = new StackPane(area);
        viewport.setAlignment(Pos.CENTER);
        viewport.setBackground(new Background(new BackgroundFill(Theme.INK, CornerRadii.EMPTY, null)));

        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        double fit = Math.min(1.0, Math.min((screen.getHeight() - 70) / HEIGHT, (screen.getWidth() - 20) / WIDTH));
        Scene scene = new Scene(viewport, WIDTH * fit, HEIGHT * fit);
        scene.getStylesheets().add(GameWindow.class.getResource("/fx/retro.css").toExternalForm());
        Runnable rescale = () -> {
            double s = Math.min(scene.getWidth() / WIDTH, scene.getHeight() / HEIGHT);
            area.setScaleX(s);
            area.setScaleY(s);
        };
        scene.widthProperty().addListener((o, a, b) -> rescale.run());
        scene.heightProperty().addListener((o, a, b) -> rescale.run());
        rescale.run();

        stage.setTitle("HTTYD Dragon Battle");
        stage.setScene(scene);
        stage.setMinWidth(480);
        stage.setMinHeight(340);

        register("menu", new MenuScreen(this, context));
        register("egg", new EggScreen(this, context));
        register("collection", new CollectionScreen(this, context));
        register("team", new TeamSelectScreen(this, context));
        register("field", new FieldSelectScreen(this, context));
        register("dex", new DexScreen(this, context, "All Dragons", "Every dragon in the game. Click one for details.",
                "Collection", this::showCollection));
        register("odds", new DexScreen(this, context, "Drop Rates", null, "Back to Eggs", this::showEgg));
        register("battle", new BattleScreen(this, context));
        register("topup", new TopUpScreen(this, context));
        register("howto", new HowToScreen(this, context));
    }

    /**
     * Adds (or replaces) a screen.
     *
     * @param name   the name used by {@link #show(String)}
     * @param screen the screen
     */
    public void register(String name, ScreenPane screen) {
        screens.put(name, screen);
    }

    /**
     * Swaps a screen into the game area.
     *
     * @param name the name of the screen
     */
    public void show(String name) {
        ScreenPane next = screens.get(name);
        if (next == null) {
            throw new IllegalArgumentException("unknown screen " + name);
        }
        if (current != null) {
            current.onHide();
        }
        current = next;
        area.getChildren().setAll(next);
        next.onShow();
    }

    /** Shows the main menu and starts the music. */
    public void showMenu() {
        show("menu");
        context.getSound().startMusic();
    }

    /** Shows the egg hatching screen. */
    public void showEgg() {
        show("egg");
    }

    /** Shows the index of every dragon in the game. */
    public void showDex() {
        show("dex");
    }

    /** Shows the drop rates of the egg. */
    public void showOdds() {
        show("odds");
    }

    /** Shows the collection screen. */
    public void showCollection() {
        show("collection");
    }

    /** Shows the team selection screen. */
    public void showTeamSelect() {
        show("team");
    }

    /**
     * Shows the battlefield selection screen.
     *
     * @param setup the team, items and difficulty chosen so far
     */
    public void showFieldSelect(MatchSetup setup) {
        ((FieldSelectScreen) screens.get("field")).prepare(setup);
        show("field");
    }

    /**
     * Starts a battle.
     *
     * @param setup   the team, items and difficulty
     * @param session the prepared battle
     * @param field   the battlefield that was drawn
     */
    public void startBattle(MatchSetup setup, BattleSession session, BattlefieldType field) {
        BattleScreen battle = (BattleScreen) screens.get("battle");
        battle.prepare(setup, session, field);
        show("battle");
        battle.begin();
    }

    /** Shows the free demo coin shop. */
    public void showTopUp() {
        show("topup");
    }

    /** Shows the how-to-play screen. */
    public void showHowTo() {
        show("howto");
    }

    /** Closes the game. */
    public void quit() {
        context.getSound().stopMusic();
        Platform.exit();
    }

    /** @return the stage */
    public Stage getStage() {
        return stage;
    }
}
