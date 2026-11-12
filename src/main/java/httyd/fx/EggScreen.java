package httyd.fx;

import httyd.model.GameRules;
import httyd.model.dragon.OwnedDragon;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * The egg screen: the player pays coins, the egg shakes and cracks, and a random dragon hatches.
 */
public class EggScreen extends ScreenPane {
    private enum Phase { IDLE, SHAKING, FLASH, REVEALED }

    private static final int SHAKE_MILLIS = 2000;
    private static final int FLASH_MILLIS = 350;
    private static final double STAGE_W = 520;
    private static final double STAGE_H = 624;

    private final Canvas stage = new Canvas(STAGE_W, STAGE_H);
    private final DragonDetailPane detail;
    private final RetroButton openButton = new RetroButton("");
    private final AnimationTimer timer;
    private Phase phase = Phase.IDLE;
    private long phaseStart;
    private int lastCrack;
    private OwnedDragon hatched;

    /**
     * @param window  the game window
     * @param context the shared game objects
     */
    public EggScreen(GameWindow window, GameContext context) {
        super(window, context);
        detail = new DragonDetailPane(context.getRepository());
        detail.setHint("The hatched dragon will appear here");
        content.setTop(header("Hatch a Dragon Egg", "Main Menu", window::showMenu));

        openButton.setFixedSize(430, 54);
        openButton.setOnAction(e -> openEgg());
        VBox right = new VBox(10, detail, openButton);
        HBox center = new HBox(20, stage, right);
        center.setPadding(new Insets(6, 24, 20, 24));
        BorderPane.setMargin(center, Insets.EMPTY);
        content.setCenter(center);

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                tick();
                paint();
            }
        };
    }

    @Override
    public void onShow() {
        phase = Phase.IDLE;
        phaseStart = System.currentTimeMillis();
        hatched = null;
        detail.setHint("The hatched dragon will appear here");
        detail.setDragon(null);
        refreshButton();
        timer.start();
    }

    @Override
    public void onHide() {
        timer.stop();
    }

    private void refreshButton() {
        boolean idle = phase == Phase.IDLE || phase == Phase.REVEALED;
        openButton.setDisable(!idle);
        if (!context.getProfile().canAffordEgg()) {
            openButton.setText("Not enough coins - Top Up");
        } else {
            openButton.setText("Open Egg  (" + GameRules.EGG_PRICE + " coins)");
        }
    }

    private void openEgg() {
        if (!context.getProfile().canAffordEgg()) {
            window.showTopUp();
            return;
        }
        hatched = context.getProfile().buyEgg(context.getGacha());
        context.save();
        context.getSound().play(SoundPlayer.Sound.CLICK);
        detail.setDragon(null);
        detail.setHint("Hatching...");
        phase = Phase.SHAKING;
        phaseStart = System.currentTimeMillis();
        lastCrack = 0;
        refreshButton();
    }

    private void tick() {
        long elapsed = System.currentTimeMillis() - phaseStart;
        switch (phase) {
            case SHAKING -> {
                int crack = elapsed < 600 ? 0 : elapsed < 1100 ? 1 : elapsed < 1600 ? 2 : 3;
                if (crack != lastCrack) {
                    lastCrack = crack;
                    context.getSound().play(SoundPlayer.Sound.EGG_CRACK);
                }
                if (elapsed >= SHAKE_MILLIS) {
                    phase = Phase.FLASH;
                    phaseStart = System.currentTimeMillis();
                    context.getSound().play(SoundPlayer.Sound.EGG_OPEN);
                }
            }
            case FLASH -> {
                if (elapsed >= FLASH_MILLIS) {
                    phase = Phase.REVEALED;
                    phaseStart = System.currentTimeMillis();
                    detail.setDragon(hatched);
                    refreshButton();
                }
            }
            default -> { }
        }
    }

    private void paint() {
        GraphicsContext g = stage.getGraphicsContext2D();
        g.setImageSmoothing(false);
        double w = STAGE_W;
        double h = STAGE_H;
        g.setFill(Theme.INK);
        g.fillRect(0, 0, w, h);
        g.setFill(Theme.rgb(0x2E3850));
        g.fillRect(4, 4, w - 8, h - 8);
        long now = System.currentTimeMillis();
        long elapsed = now - phaseStart;

        double cx = w / 2;
        double cy = h / 2 - 20;
        g.setFill(Color.rgb(255, 230, 140, 8 / 255.0));
        for (int r = 220; r > 40; r -= 20) {
            g.fillOval(cx - r, cy - r, r * 2, r * 2);
        }
        g.setFill(Color.rgb(0, 0, 0, 80 / 255.0));
        g.fillOval(cx - 110, cy + 150, 220, 40);

        if (phase == Phase.IDLE || phase == Phase.SHAKING) {
            int crack = phase == Phase.SHAKING ? lastCrack : 0;
            Image egg = SpriteFactory.egg(10, crack);
            double amplitude = phase == Phase.SHAKING ? 4 + 14.0 * Math.min(1, elapsed / (double) SHAKE_MILLIS) : 0;
            double dx = Math.round(Math.sin(now / (phase == Phase.SHAKING ? 28.0 : 400.0)) * amplitude);
            double dy = phase == Phase.IDLE ? Math.round(Math.sin(now / 300.0) * 6) : 0;
            g.drawImage(egg, Math.round(cx - egg.getWidth() / 2 + dx), Math.round(cy - egg.getHeight() / 2 + dy));
        } else if (phase == Phase.FLASH) {
            double alpha = Math.max(0, 1 - elapsed / (double) FLASH_MILLIS);
            g.setFill(Color.rgb(255, 255, 255, alpha));
            g.fillRect(4, 4, w - 8, h - 8);
        } else if (hatched != null) {
            Image sprite = SpriteFactory.dragon(hatched.getSpecies(), true, 7);
            double bob = Math.round(Math.sin(now / 250.0) * 5);
            g.drawImage(sprite, Math.round(cx - sprite.getWidth() / 2), Math.round(cy - sprite.getHeight() / 2 + bob));
            g.setFont(Theme.font(30, true));
            Draw.shadowed(g, hatched.getSpecies().getName() + "!",
                    cx - Draw.width(g.getFont(), hatched.getSpecies().getName() + "!") / 2, 60, Theme.GOLD);
        }
        g.setFont(Theme.font(16, true));
        g.setFill(Theme.PAPER);
        g.fillText("Your coins: " + context.getProfile().getCoins(), 20, h - 18);
        g.setFont(Theme.font(13, false));
        String hint = "Rolls random IVs, a Nature and a " + GameRules.ALPHA_CHANCE_PERCENT + "% Alpha chance";
        g.fillText(hint, w - 20 - Draw.width(g.getFont(), hint), h - 18);
    }
}
