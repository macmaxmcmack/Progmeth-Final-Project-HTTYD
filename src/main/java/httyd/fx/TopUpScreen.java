package httyd.fx;

import httyd.data.CoinPack;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * The demo coin shop. Every pack is free: pressing a card adds the coins at once and no real money is
 * involved.
 */
public class TopUpScreen extends ScreenPane {
    private static final NumberFormat NUMBERS = NumberFormat.getIntegerInstance(Locale.US);
    private static final double STAGE_W = 960;

    /** A "+500" that floats up from the balance after a top-up. */
    private static final class Floater {
        final String text;
        double y = 0;
        double life = 1.0;

        Floater(String text) {
            this.text = text;
        }
    }

    private final Canvas balance = new Canvas(STAGE_W, 140);
    private final List<Floater> floaters = new ArrayList<>();
    private final AnimationTimer timer;
    private double shownCoins;
    private long lastFrame;
    private String lastMessage = "";
    private long lastMessageAt;

    /**
     * @param window  the game window
     * @param context the shared game objects
     */
    public TopUpScreen(GameWindow window, GameContext context) {
        super(window, context);
        content.setTop(header("Top Up Coins", "Main Menu", window::showMenu));

        HBox cards = new HBox(20);
        cards.setAlignment(Pos.CENTER);
        CoinPack[] packs = CoinPack.values();
        for (int i = 0; i < packs.length; i++) {
            cards.getChildren().add(new PackCard(packs[i], i));
        }
        Label notice = new Label("Demo shop: every pack is free. No real money is charged or needed.");
        notice.setFont(Theme.font(14, false));
        notice.setTextFill(Theme.PAPER);
        VBox box = new VBox(10, balance, cards, notice);
        box.setAlignment(Pos.TOP_CENTER);
        box.setPadding(new Insets(6, 24, 24, 24));
        content.setCenter(box);

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastFrame >= 30_000_000L) {
                    lastFrame = now;
                    tick();
                }
            }
        };
    }

    @Override
    public void onShow() {
        shownCoins = context.getProfile().getCoins();
        floaters.clear();
        lastMessage = "";
        paintBalance();
        timer.start();
    }

    @Override
    public void onHide() {
        timer.stop();
    }

    private void claim(CoinPack pack) {
        int added = pack.claim(context.getProfile());
        context.save();
        if (added > 0) {
            context.getSound().play(SoundPlayer.Sound.HEAL);
            floaters.add(new Floater("+" + NUMBERS.format(added)));
            lastMessage = NUMBERS.format(added) + " coins added to your balance!";
        } else {
            context.getSound().play(SoundPlayer.Sound.CLICK);
            lastMessage = "Your coin purse is full.";
        }
        lastMessageAt = System.currentTimeMillis();
    }

    private void tick() {
        double target = context.getProfile().getCoins();
        double step = Math.max(2, Math.abs(target - shownCoins) * 0.12);
        if (shownCoins < target) {
            shownCoins = Math.min(target, shownCoins + step);
        } else if (shownCoins > target) {
            shownCoins = Math.max(target, shownCoins - step);
        }
        Iterator<Floater> it = floaters.iterator();
        while (it.hasNext()) {
            Floater f = it.next();
            f.life -= 0.033 / 1.3;
            f.y -= 1.4;
            if (f.life <= 0) {
                it.remove();
            }
        }
        paintBalance();
    }

    private void paintBalance() {
        GraphicsContext g = balance.getGraphicsContext2D();
        g.clearRect(0, 0, STAGE_W, 140);
        double mid = STAGE_W / 2;
        g.setFill(Theme.PAPER);
        g.setFont(Theme.font(16, true));
        Draw.centered(g, "Your coins", mid, 30);
        String text = NUMBERS.format(Math.round(shownCoins));
        g.setFont(Theme.font(54, true));
        double bw = Draw.width(g.getFont(), text);
        double bx = (STAGE_W - bw) / 2;
        Draw.coin(g, bx - 52, 40, 40);
        Draw.shadowed(g, text, bx, 88, Theme.GOLD);
        for (Floater f : floaters) {
            g.setFont(Theme.font(30, true));
            g.setFill(Color.rgb(0x66, 0xE0, 0x7A, Math.max(0, Math.min(1, f.life * 400 / 255.0))));
            g.fillText(f.text, bx + bw + 20, 84 + f.y);
        }
        if (!lastMessage.isEmpty() && System.currentTimeMillis() - lastMessageAt < 3500) {
            g.setFont(Theme.font(16, true));
            g.setFill(Color.rgb(0x66, 0xE0, 0x7A));
            Draw.centered(g, lastMessage, mid, 122);
        } else {
            g.setFont(Theme.font(15, false));
            g.setFill(Theme.PAPER_DARK);
            Draw.centered(g, "Pick a pack. The coins arrive right away.", mid, 122);
        }
    }

    /** One clickable pack card. */
    private final class PackCard extends Canvas {
        private static final double CARD_W = 210;
        private static final double CARD_H = 290;
        private final CoinPack pack;
        private final int index;

        PackCard(CoinPack pack, int index) {
            super(CARD_W, CARD_H);
            this.pack = pack;
            this.index = index;
            setCursor(Cursor.HAND);
            setOnMouseEntered(e -> {
                setTranslateY(-6);
                draw(true);
            });
            setOnMouseExited(e -> {
                setTranslateY(0);
                draw(false);
            });
            setOnMouseClicked(e -> claim(pack));
            draw(false);
        }

        private void draw(boolean over) {
            GraphicsContext g = getGraphicsContext2D();
            g.clearRect(0, 0, CARD_W, CARD_H);
            g.setFill(Theme.INK);
            g.fillRect(0, 0, CARD_W, CARD_H);
            g.setFill(over ? Theme.mix(Theme.PAPER, Color.WHITE, 0.5) : Theme.PAPER);
            g.fillRect(4, 4, CARD_W - 8, CARD_H - 8);
            g.setFill(Theme.mix(Theme.GOLD, Theme.PAPER, 0.55));
            g.fillRect(4, 4, CARD_W - 8, 120);

            // a stack of coins that grows with the pack size
            int stacks = index + 1;
            for (int s = 0; s < stacks; s++) {
                double sx = CARD_W / 2 - (stacks * 30) / 2.0 + s * 30 + 8;
                int height = 2 + Math.min(5, index + 2 + (s % 2));
                for (int c = 0; c < height; c++) {
                    Draw.coin(g, sx, 100 - c * 7, 28);
                }
            }
            g.setFill(Theme.INK);
            g.setFont(Theme.font(18, true));
            Draw.centered(g, pack.getDisplayName(), CARD_W / 2, 156);
            g.setFont(Theme.font(30, true));
            Draw.centered(g, "+" + NUMBERS.format(pack.getCoins()), CARD_W / 2, 198);
            g.setFont(Theme.font(13, false));
            Draw.centered(g, "coins", CARD_W / 2, 218);

            double bw = 150;
            double bx = (CARD_W - bw) / 2;
            double by = CARD_H - 56;
            g.setFill(Theme.INK);
            g.fillRect(bx, by, bw, 40);
            g.setFill(over ? Theme.mix(Theme.ACCENT, Color.WHITE, 0.2) : Theme.ACCENT);
            g.fillRect(bx + 3, by + 3, bw - 6, 34);
            g.setFill(Color.WHITE);
            g.setFont(Theme.font(18, true));
            Draw.centered(g, "GET FREE", CARD_W / 2, by + 27);
        }
    }
}
