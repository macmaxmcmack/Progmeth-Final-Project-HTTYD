package httyd.fx;

import httyd.model.GameRules;
import httyd.model.dragon.OwnedDragon;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * The collection screen: shows every owned dragon and lets the player fuse duplicates to raise a
 * dragon's star level.
 */
public class CollectionScreen extends ScreenPane {
    private final TileScroll scroll = new TileScroll();
    private final DragonDetailPane detail;
    private final RetroButton fuseButton = new RetroButton("Fuse", Theme.darker(Theme.GOLD, 0.25));
    private final Label infoLabel = new Label();
    private final List<DragonTile> tiles = new ArrayList<>();
    private OwnedDragon selected;
    private OwnedDragon fusionTarget;

    /**
     * @param window  the game window
     * @param context the shared game objects
     */
    public CollectionScreen(GameWindow window, GameContext context) {
        super(window, context);
        detail = new DragonDetailPane(context.getRepository());
        content.setTop(header("Dragon Collection", "Main Menu", window::showMenu));

        infoLabel.setTextFill(Theme.PAPER);
        infoLabel.setFont(Theme.font(15, false));
        RetroButton dexButton = new RetroButton("All Dragons", Theme.ACCENT_2);
        dexButton.setFont(Theme.font(14, true));
        dexButton.setFixedSize(150, 38);
        dexButton.setOnAction(e -> {
            context.getSound().play(SoundPlayer.Sound.CLICK);
            window.showDex();
        });
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox infoRow = new HBox(infoLabel, spacer, dexButton);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        infoLabel.setWrapText(true);
        infoLabel.setMaxWidth(330);
        VBox left = new VBox(8, infoRow, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        HBox.setHgrow(left, Priority.ALWAYS);

        fuseButton.setFixedSize(430, 50);
        fuseButton.setOnAction(e -> toggleFusion());
        VBox right = new VBox(10, detail, fuseButton);

        HBox center = new HBox(18, left, right);
        center.setPadding(new Insets(4, 24, 18, 24));
        content.setCenter(center);
    }

    @Override
    public void onShow() {
        fusionTarget = null;
        selected = null;
        rebuild();
    }

    private void rebuild() {
        List<OwnedDragon> dragons = new ArrayList<>(context.getProfile().getDragons());
        dragons.sort(Comparator
                .comparing((OwnedDragon d) -> d.getSpecies().getTier()).reversed()
                .thenComparing(d -> d.getSpecies().getName())
                .thenComparing(Comparator.comparingInt(OwnedDragon::getStar).reversed())
                .thenComparingInt(OwnedDragon::getId));
        tiles.clear();
        for (OwnedDragon dragon : dragons) {
            tiles.add(new DragonTile(dragon, () -> onTileClicked(dragon)));
        }
        scroll.setTiles(tiles);
        refreshState();
    }

    private int duplicateCount(OwnedDragon target) {
        int count = 0;
        for (OwnedDragon d : context.getProfile().getDragons()) {
            if (context.getProfile().canFuse(target.getId(), d.getId())) {
                count++;
            }
        }
        return count;
    }

    private void refreshState() {
        for (DragonTile tile : tiles) {
            OwnedDragon d = tile.getDragon();
            boolean isSelected = selected != null && d.getId() == selected.getId();
            tile.setSelected(isSelected || (fusionTarget != null && d.getId() == fusionTarget.getId()));
            tile.setDimmed(fusionTarget != null
                    && !context.getProfile().canFuse(fusionTarget.getId(), d.getId())
                    && d.getId() != fusionTarget.getId());
        }
        detail.setDragon(selected);
        if (fusionTarget != null) {
            infoLabel.setText("Choose a dragon of the same species to use as material (it will be consumed)");
            fuseButton.setText("Cancel Fusion");
            fuseButton.setDisable(false);
        } else {
            infoLabel.setText("Dragons: " + context.getProfile().getDragons().size() + "  |  Coins: "
                    + context.getProfile().getCoins());
            if (selected == null) {
                fuseButton.setText("Fuse (select a dragon first)");
                fuseButton.setDisable(true);
            } else if (selected.getStar() >= GameRules.MAX_STAR) {
                fuseButton.setText("Already at max stars");
                fuseButton.setDisable(true);
            } else {
                int count = duplicateCount(selected);
                fuseButton.setText(count == 0 ? "Fuse (no duplicates yet)" : "Fuse with a duplicate (" + count + " available)");
                fuseButton.setDisable(count == 0);
            }
        }
    }

    private void onTileClicked(OwnedDragon dragon) {
        context.getSound().play(SoundPlayer.Sound.CLICK);
        if (fusionTarget == null) {
            selected = dragon;
            refreshState();
            return;
        }
        if (!context.getProfile().canFuse(fusionTarget.getId(), dragon.getId())) {
            return;
        }
        String message = "Use " + dragon.getSpecies().getName() + " (grade " + dragon.getGrade().getLabel()
                + (dragon.isAlpha() ? ", Alpha" : "") + ") as fusion material?\nIt will be consumed and cannot be recovered.";
        if (Dialogs.confirm(window.getStage(), "Confirm Fusion", message, "Fuse", "Cancel")) {
            OwnedDragon target = fusionTarget;
            context.getProfile().fuse(target.getId(), dragon.getId());
            context.save();
            context.getSound().play(SoundPlayer.Sound.EGG_OPEN);
            fusionTarget = null;
            selected = target;
            rebuild();
            String result = target.isTitanWing()
                    ? target.getSpecies().getName() + " became a Titan Wing! Passive +50%"
                    : target.getSpecies().getName() + " reached " + target.getStar() + " stars";
            Dialogs.info(window.getStage(), "Fusion Complete", result);
        }
    }

    private void toggleFusion() {
        context.getSound().play(SoundPlayer.Sound.CLICK);
        if (fusionTarget != null) {
            fusionTarget = null;
        } else if (selected != null) {
            fusionTarget = selected;
        }
        refreshState();
    }
}
