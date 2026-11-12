package httyd.fx;

import httyd.battle.BattleSession;
import httyd.battle.TimeSource;
import httyd.bot.BotFactory;
import httyd.model.field.Battlefield;
import httyd.model.field.BattlefieldType;
import httyd.model.item.Item;
import httyd.model.item.ItemType;
import httyd.model.player.BotPlayer;
import httyd.model.player.Player;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 * The player picks two of the seven battlefields, the bot picks one, and the real battlefield is
 * drawn from these three.
 */
public class FieldSelectScreen extends ScreenPane {
    private static final int PICKS = 2;
    private static final double CARD_W = 220;
    private static final double CARD_H = 200;

    private final List<FieldCard> cards = new ArrayList<>();
    private final List<BattlefieldType> picks = new ArrayList<>();
    private final Label info = new Label();
    private final RetroButton startButton = new RetroButton("Start Battle!", Theme.ACCENT);
    private MatchSetup setup;

    /**
     * @param window  the game window
     * @param context the shared game objects
     */
    public FieldSelectScreen(GameWindow window, GameContext context) {
        super(window, context);
        content.setTop(header("Choose Battlefield", "Back", window::showTeamSelect));

        GridPane cardGrid = new GridPane();
        cardGrid.setHgap(12);
        cardGrid.setVgap(12);
        cardGrid.setAlignment(Pos.TOP_CENTER);
        int i = 0;
        for (BattlefieldType type : BattlefieldType.values()) {
            FieldCard card = new FieldCard(type);
            cards.add(card);
            cardGrid.add(card, i % 4, i / 4);
            i++;
        }
        cardGrid.add(new ExplanationCard(), i % 4, i / 4);

        info.setTextFill(Theme.PAPER);
        info.setFont(Theme.font(16, false));
        startButton.setFixedSize(260, 52);
        startButton.setOnAction(e -> start());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bottom = new HBox(info, spacer, startButton);
        bottom.setAlignment(Pos.CENTER_LEFT);

        BorderPane center = new BorderPane();
        center.setPadding(new Insets(6, 30, 22, 30));
        center.setCenter(cardGrid);
        center.setBottom(bottom);
        BorderPane.setMargin(bottom, new Insets(14, 0, 0, 0));
        content.setCenter(center);
    }

    /**
     * Stores the choices made so far and clears the battlefield picks.
     *
     * @param setup the player's team, items and difficulty
     */
    public void prepare(MatchSetup setup) {
        this.setup = setup;
        picks.clear();
        refreshState();
    }

    private void toggle(BattlefieldType type) {
        context.getSound().play(SoundPlayer.Sound.CLICK);
        if (!picks.remove(type) && picks.size() < PICKS) {
            picks.add(type);
        }
        refreshState();
    }

    private void refreshState() {
        for (FieldCard card : cards) {
            card.redraw();
        }
        startButton.setDisable(picks.size() != PICKS);
        info.setText("Selected " + picks.size() + "/" + PICKS + " fields");
    }

    private void start() {
        BotPlayer bot = BotFactory.create(setup.difficulty(), context.getRepository(), context.getRandom());
        BattlefieldType botPick = BotFactory.pickField(bot, context.getRandom());
        BattlefieldType chosen = Battlefield.draw(picks, botPick, context.getRandom());
        List<Item> bag = new ArrayList<>();
        for (ItemType type : setup.items()) {
            bag.add(type.create());
        }
        Player player = new Player("You", setup.team(), bag);
        BattleSession session = new BattleSession(player, bot, chosen, context.getRandom(), TimeSource.system());
        String message = "You picked: " + picks.get(0).getDisplayName() + ", " + picks.get(1).getDisplayName()
                + "\n" + bot.getName() + " picked: " + botPick.getDisplayName()
                + "\n\nThe battlefield is: " + chosen.getDisplayName();
        context.getSound().play(SoundPlayer.Sound.EGG_OPEN);
        Dialogs.info(window.getStage(), "Battlefield Draw", message);
        window.startBattle(setup, session, chosen);
    }

    /** A clickable card with the picture of a battlefield and its rules. */
    private final class FieldCard extends Canvas {
        private final BattlefieldType type;
        private boolean hover;

        FieldCard(BattlefieldType type) {
            super(CARD_W, CARD_H);
            this.type = type;
            setCursor(Cursor.HAND);
            setOnMouseEntered(e -> {
                hover = true;
                redraw();
            });
            setOnMouseExited(e -> {
                hover = false;
                redraw();
            });
            setOnMouseClicked(e -> toggle(type));
            redraw();
        }

        void redraw() {
            GraphicsContext g = getGraphicsContext2D();
            g.setImageSmoothing(false);
            double w = CARD_W;
            double h = CARD_H;
            boolean chosen = picks.contains(type);
            g.setFill(chosen ? Theme.GOLD : Theme.INK);
            g.fillRect(0, 0, w, h);
            g.setFill(hover ? Theme.mix(Theme.PAPER, Color.WHITE, 0.5) : Theme.PAPER);
            g.fillRect(4, 4, w - 8, h - 8);
            g.drawImage(BackgroundFactory.backdrop(type), 8, 8, w - 16, 94);
            g.setStroke(Theme.INK);
            g.setLineWidth(1);
            g.strokeRect(7.5, 7.5, w - 15, 95);
            g.setFill(Theme.INK);
            g.setFont(Theme.font(15, true));
            g.fillText(type.getDisplayName(), 10, 122);
            g.setFont(Theme.font(11, false));
            Draw.wrapped(g, type.getDescription(), 10, 138, w - 20, 14);
            if (chosen) {
                g.setFont(Theme.font(13, true));
                Draw.chip(g, "Pick " + (picks.indexOf(type) + 1), w - 70, 12, 22, Theme.ACCENT, Color.WHITE);
            }
        }
    }

    /** Fills the last cell of the grid with a short explanation of how the field is drawn. */
    private static final class ExplanationCard extends Canvas {
        ExplanationCard() {
            super(CARD_W, CARD_H);
            GraphicsContext g = getGraphicsContext2D();
            g.setFill(Color.rgb(255, 255, 255, 18 / 255.0));
            g.fillRect(0, 0, CARD_W, CARD_H);
            g.setFill(Theme.PAPER);
            g.setFont(Theme.font(14, true));
            g.fillText("Choosing the field", 14, 30);
            g.setFont(Theme.font(13, false));
            Draw.wrapped(g, "You pick 2 fields and the bot picks 1. The game then draws 1 of the 3 as the real battlefield. "
                    + "Dragons whose class matches the field get a bonus; other classes may be weakened.", 14, 54, CARD_W - 28, 19);
        }
    }
}
