package httyd.fx;

import httyd.model.dragon.DragonSpecies;
import httyd.model.field.BattlefieldType;
import java.util.List;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * The main menu: a title, three flying dragons and the main buttons.
 */
public final class MenuScreen extends ScreenPane {
    private static final double[] XS = {150, 860, 512};
    private static final double[] YS = {260, 300, 18};

    private final RetroButton soundButton = new RetroButton("Sound: On", Theme.ACCENT_2);
    private final Label info = new Label();
    private final Pane flyers = new Pane();
    private final ImageView[] dragons = new ImageView[3];
    private final AnimationTimer animation;
    private List<DragonSpecies> showcase;

    /**
     * @param window  the game window
     * @param context the shared game objects
     */
    public MenuScreen(GameWindow window, GameContext context) {
        super(window, context, false);

        ImageView backdrop = new ImageView(BackgroundFactory.backdrop(BattlefieldType.THUNDERSTORM_PEAK));
        backdrop.setFitWidth(GameWindow.WIDTH);
        backdrop.setFitHeight(GameWindow.HEIGHT);
        backdrop.setSmooth(false);
        Rectangle dim = new Rectangle(GameWindow.WIDTH, GameWindow.HEIGHT, Color.rgb(10, 14, 30, 120 / 255.0));

        flyers.setMouseTransparent(true);
        flyers.setMinSize(GameWindow.WIDTH, GameWindow.HEIGHT);
        flyers.setPrefSize(GameWindow.WIDTH, GameWindow.HEIGHT);
        flyers.setMaxSize(GameWindow.WIDTH, GameWindow.HEIGHT);

        Label title = new Label("DRAGON BATTLE");
        title.setFont(Theme.font(92, true));
        title.setTextFill(Theme.GOLD);
        title.setEffect(new DropShadow(0, 6, 6, Theme.INK));
        Label subtitle = new Label("A HOW TO TRAIN YOUR DRAGON GAME");
        subtitle.setFont(Theme.mono(28));
        subtitle.setTextFill(Color.WHITE);
        info.setFont(Theme.font(16, true));
        info.setTextFill(Theme.PAPER);
        VBox heading = new VBox(0, title, subtitle, info);
        heading.setAlignment(Pos.TOP_CENTER);
        heading.setPickOnBounds(false);
        heading.setPadding(new Insets(150, 0, 0, 0));

        VBox buttons = new VBox(0,
                button("Battle a Bot", Theme.ACCENT, window::showTeamSelect),
                button("Hatch a Dragon Egg", Theme.GOLD.darker(), window::showEgg),
                button("Dragon Collection", Theme.ACCENT_2, window::showCollection),
                button("Top Up Coins", Theme.rgb(0x3E9B5A), window::showTopUp),
                button("How to Play", Theme.rgb(0x5A6FD6), window::showHowTo));
        soundButton.setFixedSize(300, 44);
        soundButton.setOnAction(e -> toggleSound());
        VBox.setMargin(soundButton, new Insets(10, 0, 10, 0));
        buttons.getChildren().add(soundButton);
        buttons.getChildren().add(button("Quit", Theme.rgb(0x7A4A4A), window::quit));
        buttons.setAlignment(Pos.BOTTOM_CENTER);
        buttons.setPickOnBounds(false);
        buttons.setPadding(new Insets(0, 0, 40, 0));

        StackPane layers = new StackPane(backdrop, dim, flyers, heading, buttons);
        layers.setAlignment(Pos.TOP_LEFT);
        StackPane.setAlignment(heading, Pos.TOP_CENTER);
        StackPane.setAlignment(buttons, Pos.BOTTOM_CENTER);
        content.setCenter(layers);

        animation = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double tick = now / 1_000_000_000.0 * 1.5;
                for (int i = 0; i < dragons.length; i++) {
                    if (dragons[i] != null) {
                        dragons[i].setTranslateY(Math.round(Math.sin(tick + i * 1.7) * 10));
                    }
                }
            }
        };
    }

    private RetroButton button(String text, Color color, Runnable action) {
        RetroButton b = new RetroButton(text, color);
        b.setFixedSize(300, 44);
        b.setOnAction(e -> {
            context.getSound().play(SoundPlayer.Sound.CLICK);
            action.run();
        });
        return b;
    }

    private void toggleSound() {
        SoundPlayer sound = context.getSound();
        sound.setMuted(!sound.isMuted());
        if (!sound.isMuted()) {
            sound.startMusic();
            sound.play(SoundPlayer.Sound.CLICK);
        }
        refreshTexts();
    }

    private void refreshTexts() {
        soundButton.setText(context.getSound().isMuted() ? "Sound: Off" : "Sound: On");
        var profile = context.getProfile();
        info.setText("Coins " + profile.getCoins() + "   |   Wins " + profile.getWins()
                + "  Losses " + profile.getLosses() + "  Draws " + profile.getDraws()
                + "   |   Dragons " + profile.getDragons().size());
    }

    @Override
    public void onShow() {
        refreshTexts();
        if (showcase == null) {
            showcase = List.of(context.getRepository().byId("toothless"),
                    context.getRepository().byId("bewilderbeast"),
                    context.getRepository().byId("stormfly"));
            for (int i = 0; i < showcase.size(); i++) {
                Image img = SpriteFactory.dragon(showcase.get(i), i != 1, i == 2 ? 3 : 6);
                ImageView view = new ImageView(img);
                view.setSmooth(false);
                view.setLayoutX(XS[i] - img.getWidth() / 2);
                view.setLayoutY(YS[i]);
                dragons[i] = view;
                flyers.getChildren().add(view);
            }
        }
        animation.start();
    }

    @Override
    public void onHide() {
        animation.stop();
    }
}
