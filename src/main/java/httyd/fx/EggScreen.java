package httyd.fx;

import httyd.model.GameRules;
import httyd.model.dragon.DragonSpecies;
import httyd.model.dragon.OwnedDragon;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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
    private final RetroButton multiButton = new RetroButton("");
    private final AnimationTimer timer;
    private Phase phase = Phase.IDLE;
    private long phaseStart;
    private int lastCrack;
    private List<OwnedDragon> hatched = List.of();
    private Set<DragonSpecies> newSpecies = Set.of();
    private int selectedIndex;

    /**
     * @param window  the game window
     * @param context the shared game objects
     */
    public EggScreen(GameWindow window, GameContext context) {
        super(window, context);
        detail = new DragonDetailPane(context.getRepository());
        detail.setHint("The hatched dragon will appear here");
        content.setTop(header("Hatch a Dragon Egg", "Main Menu", window::showMenu));

        openButton.setFixedSize(210, 54);
        openButton.setFont(Theme.font(16, true));
        openButton.setOnAction(e -> openEggs(1));
        multiButton.setFixedSize(210, 54);
        multiButton.setFont(Theme.font(16, true));
        multiButton.setOnAction(e -> openEggs(GameRules.MULTI_EGG_COUNT));
        HBox buttons = new HBox(10, openButton, multiButton);
        VBox right = new VBox(10, detail, buttons);

        RetroButton oddsButton = new RetroButton("Drop Rates", Theme.ACCENT_2);
        oddsButton.setFont(Theme.font(14, true));
        oddsButton.setFixedSize(150, 38);
        oddsButton.setOnAction(e -> {
            context.getSound().play(SoundPlayer.Sound.CLICK);
            window.showOdds();
        });
        StackPane.setAlignment(oddsButton, Pos.TOP_RIGHT);
        StackPane.setMargin(oddsButton, new Insets(12, 12, 0, 0));
        stage.setOnMouseClicked(e -> pickHatched(e.getX(), e.getY()));
        StackPane stageHolder = new StackPane(stage, oddsButton);
        stageHolder.setMaxSize(STAGE_W, STAGE_H);
        HBox center = new HBox(20, stageHolder, right);
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
        hatched = List.of();
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
        multiButton.setDisable(!idle || !context.getProfile().canAffordEggs(GameRules.MULTI_EGG_COUNT));
        if (!context.getProfile().canAffordEgg()) {
            openButton.setText("Top Up Coins");
            multiButton.setDisable(true);
        } else {
            openButton.setText("Open Egg  (" + GameRules.EGG_PRICE + ")");
        }
        multiButton.setText("Open " + GameRules.MULTI_EGG_COUNT + " Eggs  ("
                + GameRules.EGG_PRICE * GameRules.MULTI_EGG_COUNT + ")");
    }

    private void openEggs(int count) {
        if (!context.getProfile().canAffordEggs(count)) {
            window.showTopUp();
            return;
        }
        Set<DragonSpecies> before = new HashSet<>();
        for (OwnedDragon d : context.getProfile().getDragons()) {
            before.add(d.getSpecies());
        }
        hatched = context.getProfile().buyEggs(context.getGacha(), count);
        newSpecies = new HashSet<>();
        for (OwnedDragon d : hatched) {
            if (!before.contains(d.getSpecies())) {
                newSpecies.add(d.getSpecies());
            }
        }
        selectedIndex = bestIndex();
        context.save();
        context.getSound().play(SoundPlayer.Sound.CLICK);
        detail.setDragon(null);
        detail.setHint("Hatching...");
        phase = Phase.SHAKING;
        phaseStart = System.currentTimeMillis();
        lastCrack = 0;
        refreshButton();
    }

    /** The rarest dragon of the batch is shown first. */
    private int bestIndex() {
        int best = 0;
        for (int i = 1; i < hatched.size(); i++) {
            if (hatched.get(i).getSpecies().getTier().ordinal() > hatched.get(best).getSpecies().getTier().ordinal()) {
                best = i;
            }
        }
        return best;
    }

    // cells of the result view when several eggs were opened: three in the first row, the rest below
    private static final double CELL_W = 160;
    private static final double CELL_H = 188;

    private double cellX(int i, int count) {
        int row = i / 3;
        int inRow = Math.min(3, count - row * 3);
        return (STAGE_W - inRow * CELL_W) / 2 + (i % 3) * CELL_W;
    }

    private double cellY(int i) {
        return 120 + (i / 3) * (CELL_H + 14);
    }

    private void pickHatched(double x, double y) {
        if (phase != Phase.REVEALED || hatched.size() < 2) {
            return;
        }
        for (int i = 0; i < hatched.size(); i++) {
            double cx = cellX(i, hatched.size());
            double cy = cellY(i);
            if (x >= cx && x < cx + CELL_W - 8 && y >= cy && y < cy + CELL_H) {
                selectedIndex = i;
                context.getSound().play(SoundPlayer.Sound.CLICK);
                detail.setDragon(hatched.get(i));
                return;
            }
        }
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
                    detail.setDragon(hatched.get(selectedIndex));
                    refreshButton();
                }
            }
            default -> { }
        }
    }

    private void drawCell(GraphicsContext g, int i) {
        OwnedDragon d = hatched.get(i);
        double x = cellX(i, hatched.size());
        double y = cellY(i);
        double w = CELL_W - 8;
        boolean chosen = i == selectedIndex;
        g.setFill(chosen ? Theme.GOLD : Theme.INK);
        g.fillRect(x, y, w, CELL_H);
        g.setFill(Theme.PAPER);
        g.fillRect(x + 4, y + 4, w - 8, CELL_H - 8);
        g.setFill(Theme.rgb(d.getSpecies().getDragonClass().getColorRgb()));
        g.fillRect(x + 4, y + 4, w - 8, 6);
        Image sprite = SpriteFactory.dragon(d.getSpecies(), true, 3);
        g.drawImage(sprite, Math.round(x + (w - sprite.getWidth()) / 2), y + 14);
        g.setFill(Theme.INK);
        g.setFont(Theme.font(14, true));
        String name = d.getSpecies().getShortName();
        g.fillText(name, Math.max(x + 6, x + (w - Draw.width(g.getFont(), name)) / 2), y + 146);
        g.setFont(Theme.font(12, true));
        Draw.chip(g, "Grade " + d.getGrade().getLabel(), x + 8, y + 156, 20,
                d.getGrade() == httyd.model.dragon.Grade.SSS ? Theme.GOLD : Theme.PAPER_DARK, Theme.INK);
        if (d.isAlpha()) {
            Draw.chip(g, "Alpha", x + w - 62, y + 14, 18, Theme.rgb(0xB34CD6), Color.WHITE);
        }
        if (newSpecies.contains(d.getSpecies())) {
            Draw.chip(g, "NEW!", x + 8, y + 14, 18, Theme.rgb(0x2E9E52), Color.WHITE);
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
        } else if (hatched.size() == 1) {
            OwnedDragon one = hatched.get(0);
            Image sprite = SpriteFactory.dragon(one.getSpecies(), true, 7);
            double bob = Math.round(Math.sin(now / 250.0) * 5);
            g.drawImage(sprite, Math.round(cx - sprite.getWidth() / 2), Math.round(cy - sprite.getHeight() / 2 + bob));
            g.setFont(Theme.font(30, true));
            String name = one.getSpecies().getName() + "!";
            Draw.shadowed(g, name, cx - Draw.width(g.getFont(), name) / 2, 60, Theme.GOLD);
            if (newSpecies.contains(one.getSpecies())) {
                g.setFont(Theme.font(16, true));
                Draw.chip(g, "NEW!", cx - 30, 74, 26, Theme.rgb(0x2E9E52), Color.WHITE);
            }
        } else if (!hatched.isEmpty()) {
            g.setFont(Theme.font(26, true));
            Draw.shadowed(g, hatched.size() + " dragons hatched!", 24, 62, Theme.GOLD);
            g.setFont(Theme.font(13, false));
            g.setFill(Theme.PAPER);
            g.fillText("Click a dragon to see its details", 24, 90);
            for (int i = 0; i < hatched.size(); i++) {
                drawCell(g, i);
            }
        }
        g.setFont(Theme.font(16, true));
        g.setFill(Theme.PAPER);
        g.fillText("Your coins: " + context.getProfile().getCoins(), 20, h - 18);
        g.setFont(Theme.font(13, false));
        String hint = "Rolls random IVs, a Nature and a " + GameRules.ALPHA_CHANCE_PERCENT + "% Alpha chance";
        g.fillText(hint, w - 20 - Draw.width(g.getFont(), hint), h - 18);
    }
}
