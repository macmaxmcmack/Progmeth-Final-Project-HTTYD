package httyd.gui;

import httyd.model.GameRules;
import httyd.model.dragon.OwnedDragon;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 * The collection screen: shows every owned dragon and lets the player fuse duplicates to
 * raise a dragon's star level.
 */
public class CollectionPanel extends ScreenPanel {
    private static final int COLUMNS = 3;

    private final JPanel grid = new JPanel();
    private final DragonDetailPanel detail;
    private final RetroButton fuseButton = new RetroButton("Fuse", Theme.GOLD.darker());
    private final JLabel infoLabel = new JLabel();
    private final List<DragonTile> tiles = new ArrayList<>();
    private OwnedDragon selected;
    private OwnedDragon fusionTarget;

    /**
     * @param window  the game window
     * @param context the shared game objects
     */
    public CollectionPanel(GameWindow window, GameContext context) {
        super(window, context);
        detail = new DragonDetailPanel(context.getRepository());
        add(header("Dragon Collection", "Main Menu", window::showMenu), BorderLayout.NORTH);

        grid.setOpaque(false);
        JPanel gridHolder = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        gridHolder.setOpaque(false);
        gridHolder.add(grid);
        JScrollPane scroll = new JScrollPane(gridHolder, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.setPreferredSize(new Dimension(DragonTile.WIDTH * COLUMNS + 10 * (COLUMNS + 1) + 24, 500));

        JPanel left = new JPanel(new BorderLayout(0, 8));
        left.setOpaque(false);
        infoLabel.setForeground(Theme.PAPER);
        infoLabel.setFont(Theme.font(15, false));
        left.add(infoLabel, BorderLayout.NORTH);
        left.add(scroll, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(0, 10));
        right.setOpaque(false);
        right.add(detail, BorderLayout.CENTER);
        fuseButton.setPreferredSize(new Dimension(430, 50));
        fuseButton.addActionListener(e -> toggleFusion());
        right.add(fuseButton, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(18, 0));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(4, 24, 18, 24));
        center.add(left, BorderLayout.CENTER);
        center.add(right, BorderLayout.EAST);
        add(center, BorderLayout.CENTER);
    }

    @Override
    public void onShow() {
        fusionTarget = null;
        selected = null;
        detail.setDragon(null);
        rebuild();
    }

    private void rebuild() {
        List<OwnedDragon> dragons = new ArrayList<>(context.getProfile().getDragons());
        dragons.sort(Comparator
                .comparing((OwnedDragon d) -> d.getSpecies().getTier()).reversed()
                .thenComparing(d -> d.getSpecies().getName())
                .thenComparing(Comparator.comparingInt(OwnedDragon::getStar).reversed())
                .thenComparingInt(OwnedDragon::getId));
        grid.removeAll();
        tiles.clear();
        int rows = Math.max(1, (dragons.size() + COLUMNS - 1) / COLUMNS);
        grid.setLayout(new GridLayout(rows, COLUMNS, 10, 10));
        grid.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        for (OwnedDragon dragon : dragons) {
            DragonTile tile = new DragonTile(dragon, () -> onTileClicked(dragon));
            tiles.add(tile);
            grid.add(tile);
        }
        refreshState();
        grid.revalidate();
        grid.repaint();
    }

    private List<OwnedDragon> duplicatesOf(OwnedDragon target) {
        List<OwnedDragon> result = new ArrayList<>();
        for (OwnedDragon d : context.getProfile().getDragons()) {
            if (context.getProfile().canFuse(target.getId(), d.getId())) {
                result.add(d);
            }
        }
        return result;
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
            fuseButton.setEnabled(true);
        } else {
            infoLabel.setText("Dragons: " + context.getProfile().getDragons().size() + "  |  Coins: "
                    + context.getProfile().getCoins());
            if (selected == null) {
                fuseButton.setText("Fuse (select a dragon first)");
                fuseButton.setEnabled(false);
            } else if (selected.getStar() >= GameRules.MAX_STAR) {
                fuseButton.setText("Already at max stars");
                fuseButton.setEnabled(false);
            } else {
                int count = duplicatesOf(selected).size();
                fuseButton.setText(count == 0 ? "Fuse (no duplicates yet)" : "Fuse with a duplicate (" + count + " available)");
                fuseButton.setEnabled(count > 0);
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
        if (Dialogs.confirm(this, "Confirm Fusion", message, "Fuse", "Cancel")) {
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
            Dialogs.info(this, "Fusion Complete", result);
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
