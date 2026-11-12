package httyd.fx;

import httyd.model.GameRules;
import httyd.model.dragon.OwnedDragon;
import httyd.model.dragon.Stat;
import httyd.model.item.ItemType;
import httyd.model.player.Difficulty;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Lets the player build a team of three dragons, pick up to two trainer items and choose the bot's
 * difficulty before a match.
 */
public class TeamSelectScreen extends ScreenPane {
    private static final double RIGHT_W = 410;

    private final TileScroll scroll = new TileScroll();
    private final List<DragonTile> tiles = new ArrayList<>();
    private final List<Integer> teamIds = new ArrayList<>();
    private final TeamSlot[] slots = new TeamSlot[GameRules.DECK_SIZE];
    private final Set<ItemType> items = new LinkedHashSet<>();
    private final Map<ItemType, RetroButton> itemButtons = new EnumMap<>(ItemType.class);
    private final Map<Difficulty, RetroButton> difficultyButtons = new EnumMap<>(Difficulty.class);
    private final Label itemHint = new Label(" ");
    private final RetroButton nextButton = new RetroButton("Choose Field", Theme.ACCENT);
    private Difficulty difficulty = Difficulty.NORMAL;

    /**
     * @param window  the game window
     * @param context the shared game objects
     */
    public TeamSelectScreen(GameWindow window, GameContext context) {
        super(window, context);
        content.setTop(header("Choose Team & Items", "Main Menu", window::showMenu));

        Label hint = new Label("Click a dragon to add it to or remove it from your team (pick 3)");
        hint.setTextFill(Theme.PAPER);
        hint.setFont(Theme.font(15, false));
        VBox left = new VBox(8, hint, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        HBox.setHgrow(left, Priority.ALWAYS);

        VBox right = new VBox(6);
        right.setMinWidth(RIGHT_W);
        right.setPrefWidth(RIGHT_W);
        right.setMaxWidth(RIGHT_W);
        right.getChildren().add(sectionLabel("Your team (the top dragon fights first)"));
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new TeamSlot(i);
            right.getChildren().add(slots[i]);
        }
        right.getChildren().add(sectionLabel("Trainer items (max " + GameRules.BAG_SIZE + ", each use costs a turn)"));
        right.getChildren().add(buildItems());
        itemHint.setTextFill(Theme.PAPER);
        itemHint.setFont(Theme.font(13, false));
        itemHint.setMinHeight(20);
        right.getChildren().add(itemHint);
        right.getChildren().add(sectionLabel("Bot difficulty"));
        right.getChildren().add(buildDifficulty());
        Region glue = new Region();
        VBox.setVgrow(glue, Priority.ALWAYS);
        right.getChildren().add(glue);
        nextButton.setFixedSize(RIGHT_W, 52);
        nextButton.setOnAction(e -> next());
        right.getChildren().add(nextButton);

        HBox center = new HBox(18, left, right);
        center.setPadding(new Insets(4, 24, 18, 24));
        content.setCenter(center);
    }

    private Label sectionLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Theme.GOLD);
        label.setFont(Theme.font(15, true));
        label.setPadding(new Insets(4, 0, 0, 0));
        return label;
    }

    private GridPane buildItems() {
        GridPane panel = new GridPane();
        panel.setHgap(8);
        panel.setVgap(8);
        int i = 0;
        for (ItemType type : ItemType.values()) {
            RetroButton button = new RetroButton(type.create().getDisplayName(), Theme.ACCENT_2);
            button.setFont(Theme.font(14, true));
            button.setFixedSize((RIGHT_W - 8) / 2, 38);
            button.setOnAction(e -> toggleItem(type));
            button.setOnMouseEntered(e -> itemHint.setText(type.create().getDescription()));
            button.setOnMouseExited(e -> itemHint.setText(" "));
            itemButtons.put(type, button);
            panel.add(button, i % 2, i / 2);
            i++;
        }
        return panel;
    }

    private HBox buildDifficulty() {
        HBox panel = new HBox(8);
        double each = (RIGHT_W - 16) / 3;
        for (Difficulty d : Difficulty.values()) {
            RetroButton button = new RetroButton(d.getDisplayName(), Theme.ACCENT_2);
            button.setFixedSize(each, 44);
            button.setOnAction(e -> {
                difficulty = d;
                context.getSound().play(SoundPlayer.Sound.CLICK);
                refreshState();
            });
            difficultyButtons.put(d, button);
            panel.getChildren().add(button);
        }
        return panel;
    }

    @Override
    public void onShow() {
        teamIds.removeIf(id -> context.getProfile().find(id).isEmpty());
        rebuildTiles();
    }

    private void rebuildTiles() {
        List<OwnedDragon> dragons = new ArrayList<>(context.getProfile().getDragons());
        dragons.sort(Comparator
                .comparing((OwnedDragon d) -> d.getSpecies().getTier()).reversed()
                .thenComparing(d -> d.getSpecies().getName())
                .thenComparingInt(OwnedDragon::getId));
        tiles.clear();
        for (OwnedDragon dragon : dragons) {
            tiles.add(new DragonTile(dragon, () -> toggleDragon(dragon)));
        }
        scroll.setTiles(tiles);
        refreshState();
    }

    private void toggleDragon(OwnedDragon dragon) {
        context.getSound().play(SoundPlayer.Sound.CLICK);
        Integer id = dragon.getId();
        if (teamIds.contains(id)) {
            teamIds.remove(id);
        } else if (teamIds.size() < GameRules.DECK_SIZE) {
            teamIds.add(id);
        }
        refreshState();
    }

    private void toggleItem(ItemType type) {
        context.getSound().play(SoundPlayer.Sound.CLICK);
        if (!items.remove(type) && items.size() < GameRules.BAG_SIZE) {
            items.add(type);
        }
        refreshState();
    }

    private OwnedDragon dragonAt(int slot) {
        if (slot >= teamIds.size()) {
            return null;
        }
        return context.getProfile().find(teamIds.get(slot)).orElse(null);
    }

    private void refreshState() {
        for (DragonTile tile : tiles) {
            int index = teamIds.indexOf(tile.getDragon().getId());
            tile.setSelected(index >= 0);
            tile.setBadge(index + 1);
            tile.setDimmed(index < 0 && teamIds.size() >= GameRules.DECK_SIZE);
        }
        for (TeamSlot slot : slots) {
            slot.refresh();
        }
        for (Map.Entry<ItemType, RetroButton> entry : itemButtons.entrySet()) {
            boolean on = items.contains(entry.getKey());
            entry.getValue().setColor(on ? Theme.ACCENT : Theme.ACCENT_2);
            entry.getValue().setDisable(!on && items.size() >= GameRules.BAG_SIZE);
        }
        for (Map.Entry<Difficulty, RetroButton> entry : difficultyButtons.entrySet()) {
            entry.getValue().setColor(entry.getKey() == difficulty ? Theme.ACCENT : Theme.ACCENT_2);
        }
        boolean ready = teamIds.size() == GameRules.DECK_SIZE;
        nextButton.setDisable(!ready);
        nextButton.setText(ready ? "Choose Field" : "Pick 3 dragons (" + teamIds.size() + "/3)");
    }

    private void next() {
        List<OwnedDragon> team = new ArrayList<>();
        for (int i = 0; i < GameRules.DECK_SIZE; i++) {
            team.add(dragonAt(i));
        }
        window.showFieldSelect(new MatchSetup(team, List.copyOf(items), difficulty));
    }

    /** One row of the team list: picture, name, battle stats and move buttons. */
    private final class TeamSlot extends Pane {
        private static final double SLOT_H = 66;
        private final int index;
        private final Canvas canvas = new Canvas(RIGHT_W, SLOT_H);
        private final RetroButton up = small("Up", 52, 30);
        private final RetroButton down = small("Down", 52, 30);
        private final RetroButton remove = small("Remove", 108, 28);

        TeamSlot(int index) {
            this.index = index;
            setMinSize(RIGHT_W, SLOT_H);
            setPrefSize(RIGHT_W, SLOT_H);
            setMaxSize(RIGHT_W, SLOT_H);
            up.setLayoutX(296);
            up.setLayoutY(3);
            down.setLayoutX(352);
            down.setLayoutY(3);
            remove.setLayoutX(296);
            remove.setLayoutY(35);
            up.setOnAction(e -> move(-1));
            down.setOnAction(e -> move(1));
            remove.setOnAction(e -> {
                if (index < teamIds.size()) {
                    teamIds.remove(index);
                    refreshState();
                }
            });
            getChildren().addAll(canvas, up, down, remove);
        }

        private RetroButton small(String text, double w, double h) {
            RetroButton b = new RetroButton(text, Theme.ACCENT_2);
            b.setFont(Theme.font(12, true));
            b.setFixedSize(w, h);
            return b;
        }

        private void move(int delta) {
            int other = index + delta;
            if (index < teamIds.size() && other >= 0 && other < teamIds.size()) {
                Integer a = teamIds.get(index);
                teamIds.set(index, teamIds.get(other));
                teamIds.set(other, a);
                refreshState();
            }
        }

        void refresh() {
            boolean filled = index < teamIds.size();
            up.setVisible(filled);
            down.setVisible(filled);
            remove.setVisible(filled);
            up.setDisable(index <= 0);
            down.setDisable(index >= teamIds.size() - 1);
            paint();
        }

        private void paint() {
            GraphicsContext g = canvas.getGraphicsContext2D();
            g.setImageSmoothing(false);
            double w = RIGHT_W;
            double h = SLOT_H;
            OwnedDragon dragon = dragonAt(index);
            g.clearRect(0, 0, w, h);
            g.setFill(Theme.INK);
            g.fillRect(0, 0, w, h);
            g.setFill(dragon == null ? Theme.rgb(0x3A4560) : Theme.PAPER);
            g.fillRect(3, 3, w - 6, h - 6);
            g.setFont(Theme.font(20, true));
            g.setFill(dragon == null ? Theme.PAPER_DARK : Theme.ACCENT);
            g.fillText(String.valueOf(index + 1), 12, 40);
            if (dragon == null) {
                g.setFont(Theme.font(14, false));
                g.setFill(Theme.PAPER_DARK);
                g.fillText("Empty - click a dragon on the left", 44, 38);
            } else {
                Image sprite = SpriteFactory.dragon(dragon.getSpecies(), true, 1);
                g.drawImage(sprite, 36, 12);
                g.setFill(Theme.INK);
                g.setFont(Theme.font(15, true));
                g.fillText(dragon.getSpecies().getShortName(), 90, 26);
                g.setFont(Theme.font(12, false));
                g.fillText("HP " + dragon.getBattleHp() + "  ATK " + dragon.getStat(Stat.ATK)
                        + "  DEF " + dragon.getStat(Stat.DEF) + "  SPD " + dragon.getStat(Stat.SPD), 90, 46);
                g.fillText(dragon.getSpecies().getDragonClass().getDisplayName()
                        + "  Grade " + dragon.getGrade().getLabel(), 90, 61);
            }
        }
    }
}
