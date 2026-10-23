package httyd.gui;

import httyd.model.GameRules;
import httyd.model.dragon.OwnedDragon;
import httyd.model.dragon.Stat;
import httyd.model.item.ItemType;
import httyd.model.player.Difficulty;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 * Lets the player build a team of three dragons, pick up to two trainer items and choose the
 * bot's difficulty before a match.
 */
public class TeamSelectPanel extends ScreenPanel {
    private static final int COLUMNS = 3;

    private final JPanel grid = new JPanel();
    private final List<DragonTile> tiles = new ArrayList<>();
    private final List<Integer> teamIds = new ArrayList<>();
    private final TeamSlot[] slots = new TeamSlot[GameRules.DECK_SIZE];
    private final Set<ItemType> items = new LinkedHashSet<>();
    private final Map<ItemType, RetroButton> itemButtons = new EnumMap<>(ItemType.class);
    private final Map<Difficulty, RetroButton> difficultyButtons = new EnumMap<>(Difficulty.class);
    private final JLabel itemHint = new JLabel(" ");
    private final RetroButton nextButton = new RetroButton("Choose Field", Theme.ACCENT);
    private Difficulty difficulty = Difficulty.NORMAL;

    /**
     * @param window  the game window
     * @param context the shared game objects
     */
    public TeamSelectPanel(GameWindow window, GameContext context) {
        super(window, context);
        add(header("Choose Team & Items", "Main Menu", window::showMenu), BorderLayout.NORTH);

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
        JLabel hint = new JLabel("Click a dragon to add it to or remove it from your team (pick 3)");
        hint.setForeground(Theme.PAPER);
        hint.setFont(Theme.font(15, false));
        JPanel left = new JPanel(new BorderLayout(0, 8));
        left.setOpaque(false);
        left.add(hint, BorderLayout.NORTH);
        left.add(scroll, BorderLayout.CENTER);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setPreferredSize(new Dimension(410, 500));
        right.add(sectionLabel("Your team (the top dragon fights first)"));
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new TeamSlot(i);
            right.add(slots[i]);
            right.add(Box.createVerticalStrut(6));
        }
        right.add(Box.createVerticalStrut(4));
        right.add(sectionLabel("Trainer items (max " + GameRules.BAG_SIZE + ", each use costs a turn)"));
        right.add(buildItems());
        itemHint.setForeground(Theme.PAPER);
        itemHint.setFont(Theme.font(13, false));
        itemHint.setAlignmentX(LEFT_ALIGNMENT);
        right.add(itemHint);
        right.add(Box.createVerticalStrut(6));
        right.add(sectionLabel("Bot difficulty"));
        right.add(buildDifficulty());
        right.add(Box.createVerticalGlue());
        nextButton.setAlignmentX(LEFT_ALIGNMENT);
        nextButton.setMaximumSize(new Dimension(410, 52));
        nextButton.setPreferredSize(new Dimension(410, 52));
        nextButton.addActionListener(e -> next());
        right.add(nextButton);

        JPanel center = new JPanel(new BorderLayout(18, 0));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(4, 24, 18, 24));
        center.add(left, BorderLayout.CENTER);
        center.add(right, BorderLayout.EAST);
        add(center, BorderLayout.CENTER);
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Theme.GOLD);
        label.setFont(Theme.font(15, true));
        label.setAlignmentX(LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return label;
    }

    private JPanel buildItems() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.setOpaque(false);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(410, 140));
        for (ItemType type : ItemType.values()) {
            RetroButton button = new RetroButton(type.create().getDisplayName(), Theme.ACCENT_2);
            button.setFont(Theme.font(14, true));
            button.addActionListener(e -> toggleItem(type));
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    itemHint.setText(type.create().getDescription());
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    itemHint.setText(" ");
                }
            });
            itemButtons.put(type, button);
            panel.add(button);
        }
        return panel;
    }

    private JPanel buildDifficulty() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 8, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(410, 44));
        for (Difficulty d : Difficulty.values()) {
            RetroButton button = new RetroButton(d.getDisplayName(), Theme.ACCENT_2);
            button.addActionListener(e -> {
                difficulty = d;
                context.getSound().play(SoundPlayer.Sound.CLICK);
                refreshState();
            });
            difficultyButtons.put(d, button);
            panel.add(button);
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
        grid.removeAll();
        tiles.clear();
        int rows = Math.max(1, (dragons.size() + COLUMNS - 1) / COLUMNS);
        grid.setLayout(new GridLayout(rows, COLUMNS, 10, 10));
        grid.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        for (OwnedDragon dragon : dragons) {
            DragonTile tile = new DragonTile(dragon, () -> toggleDragon(dragon));
            tiles.add(tile);
            grid.add(tile);
        }
        refreshState();
        grid.revalidate();
        grid.repaint();
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
            slot.repaint();
            slot.updateButtons();
        }
        for (Map.Entry<ItemType, RetroButton> entry : itemButtons.entrySet()) {
            boolean on = items.contains(entry.getKey());
            entry.getValue().setColor(on ? Theme.ACCENT : Theme.ACCENT_2);
            entry.getValue().setEnabled(on || items.size() < GameRules.BAG_SIZE);
        }
        for (Map.Entry<Difficulty, RetroButton> entry : difficultyButtons.entrySet()) {
            entry.getValue().setColor(entry.getKey() == difficulty ? Theme.ACCENT : Theme.ACCENT_2);
        }
        boolean ready = teamIds.size() == GameRules.DECK_SIZE;
        nextButton.setEnabled(ready);
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
    private final class TeamSlot extends JComponent {
        private final int index;
        private final RetroButton up = small("Up");
        private final RetroButton down = small("Down");
        private final RetroButton remove = small("Remove");

        TeamSlot(int index) {
            this.index = index;
            setLayout(null);
            setAlignmentX(LEFT_ALIGNMENT);
            setPreferredSize(new Dimension(410, 66));
            setMaximumSize(new Dimension(410, 66));
            up.setBounds(296, 3, 52, 30);
            down.setBounds(352, 3, 52, 30);
            remove.setBounds(296, 35, 108, 28);
            up.addActionListener(e -> move(-1));
            down.addActionListener(e -> move(1));
            remove.addActionListener(e -> {
                if (index < teamIds.size()) {
                    teamIds.remove(index);
                    refreshState();
                }
            });
            add(up);
            add(down);
            add(remove);
        }

        private RetroButton small(String text) {
            RetroButton b = new RetroButton(text, Theme.ACCENT_2);
            b.setFont(Theme.font(12, true));
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

        void updateButtons() {
            boolean filled = index < teamIds.size();
            up.setVisible(filled);
            down.setVisible(filled);
            remove.setVisible(filled);
            up.setEnabled(index > 0);
            down.setEnabled(index < teamIds.size() - 1);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            OwnedDragon dragon = dragonAt(index);
            g2.setColor(Theme.INK);
            g2.fillRect(0, 0, w, h);
            g2.setColor(dragon == null ? new Color(0x3A4560) : Theme.PAPER);
            g2.fillRect(3, 3, w - 6, h - 6);
            g2.setFont(Theme.font(20, true));
            g2.setColor(dragon == null ? Theme.PAPER_DARK : Theme.ACCENT);
            g2.drawString(String.valueOf(index + 1), 12, 40);
            if (dragon == null) {
                g2.setFont(Theme.font(14, false));
                g2.setColor(Theme.PAPER_DARK);
                g2.drawString("Empty - click a dragon on the left", 44, 38);
            } else {
                BufferedImage sprite = SpriteFactory.dragon(dragon.getSpecies(), true, 1);
                g2.drawImage(sprite, 36, 12, null);
                g2.setColor(Theme.INK);
                g2.setFont(Theme.font(15, true));
                g2.drawString(dragon.getSpecies().getShortName(), 90, 26);
                g2.setFont(Theme.font(12, false));
                g2.drawString("HP " + dragon.getBattleHp() + "  ATK " + dragon.getStat(Stat.ATK)
                        + "  DEF " + dragon.getStat(Stat.DEF) + "  SPD " + dragon.getStat(Stat.SPD), 90, 46);
                g2.drawString(dragon.getSpecies().getDragonClass().getDisplayName() + "  Grade " + dragon.getGrade().getLabel(), 90, 61);
            }
            g2.dispose();
        }
    }
}
