package httyd.fx;

import httyd.data.GachaService;
import httyd.model.dragon.DragonSpecies;
import httyd.model.dragon.OwnedDragon;
import httyd.model.dragon.Tier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Shows every dragon species in the game with its picture, stats, moves and the chance of getting
 * it from an egg. It is used twice: as the "All Dragons" index of the collection, and as the
 * "Drop Rates" page of the egg screen.
 */
public class DexScreen extends ScreenPane {
    private final TileScroll scroll = new TileScroll();
    private final DragonDetailPane detail;
    private final Label summary = new Label();
    private final List<DexTile> tiles = new ArrayList<>();
    private final String subtitle;

    /**
     * @param window   the game window
     * @param context  the shared game objects
     * @param title    the title of the screen
     * @param subtitle the line above the grid; {@code null} to show the tier chances
     * @param backText the label of the back button
     * @param back     what the back button does
     */
    public DexScreen(GameWindow window, GameContext context, String title, String subtitle,
                     String backText, Runnable back) {
        super(window, context);
        this.subtitle = subtitle;
        detail = new DragonDetailPane(context.getRepository());
        detail.setHint("Click a dragon to see its details");
        content.setTop(header(title, backText, back));

        summary.setTextFill(Theme.PAPER);
        summary.setFont(Theme.font(15, false));
        summary.setWrapText(true);
        VBox left = new VBox(8, summary, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox center = new HBox(18, left, detail);
        center.setPadding(new Insets(4, 24, 18, 24));
        content.setCenter(center);
    }

    @Override
    public void onShow() {
        GachaService gacha = context.getGacha();
        Map<DragonSpecies, Double> odds = gacha.odds();
        Map<DragonSpecies, Integer> owned = new java.util.HashMap<>();
        for (OwnedDragon d : context.getProfile().getDragons()) {
            owned.merge(d.getSpecies(), 1, Integer::sum);
        }
        List<DragonSpecies> species = new ArrayList<>(odds.keySet());
        species.sort(Comparator.comparing(DragonSpecies::getTier).reversed().thenComparing(DragonSpecies::getName));

        tiles.clear();
        for (DragonSpecies s : species) {
            int count = owned.getOrDefault(s, 0);
            tiles.add(new DexTile(s, odds.get(s), count, () -> select(s, odds.get(s), count)));
        }
        scroll.setTiles(tiles);
        detail.setHint("Click a dragon to see its details");
        detail.setDragon(null);

        Map<Tier, Double> perTier = new EnumMap<>(Tier.class);
        odds.forEach((s, p) -> perTier.merge(s.getTier(), p, Double::sum));
        int ownedSpecies = owned.size();
        if (subtitle == null) {
            StringBuilder text = new StringBuilder("Chance per egg:  ");
            for (Tier t : new Tier[] {Tier.S, Tier.A, Tier.B, Tier.C}) {
                text.append("Tier ").append(t.getLabel()).append(' ')
                        .append(DragonDetailPane.formatPercent(perTier.getOrDefault(t, 0.0))).append("   ");
            }
            text.append("\nThe chance of a tier is shared equally by its dragons. Every dragon also rolls random IVs, "
                    + "a Nature and a 5% chance of being an Alpha.");
            summary.setText(text.toString());
        } else {
            summary.setText(subtitle + "  (you own " + ownedSpecies + " of " + species.size() + " kinds)");
        }
    }

    private void select(DragonSpecies species, double chance, int ownedCount) {
        context.getSound().play(SoundPlayer.Sound.CLICK);
        for (DexTile t : tiles) {
            t.setSelected(t.getSpecies() == species);
        }
        detail.setSpecies(species, chance, ownedCount);
    }
}
