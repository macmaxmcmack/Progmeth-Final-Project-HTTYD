package httyd.gui;

import httyd.battle.BattleSession;
import httyd.battle.TimeSource;
import httyd.bot.BotFactory;
import httyd.model.field.Battlefield;
import httyd.model.field.BattlefieldType;
import httyd.model.item.Item;
import httyd.model.item.ItemType;
import httyd.model.player.BotPlayer;
import httyd.model.player.Player;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The player picks two of the seven battlefields, the bot picks one, and the real battlefield is
 * drawn from these three.
 */
public class FieldSelectPanel extends ScreenPanel {
    private static final int PICKS = 2;

    private final List<FieldCard> cards = new ArrayList<>();
    private final List<BattlefieldType> picks = new ArrayList<>();
    private final JLabel info = new JLabel();
    private final RetroButton startButton = new RetroButton("Start Battle!", Theme.ACCENT);
    private MatchSetup setup;

    /**
     * @param window  the game window
     * @param context the shared game objects
     */
    public FieldSelectPanel(GameWindow window, GameContext context) {
        super(window, context);
        add(header("Choose Battlefield", "Back", window::showTeamSelect), BorderLayout.NORTH);

        JPanel cardGrid = new JPanel(new GridLayout(2, 4, 12, 12));
        cardGrid.setOpaque(false);
        for (BattlefieldType type : BattlefieldType.values()) {
            FieldCard card = new FieldCard(type);
            cards.add(card);
            cardGrid.add(card);
        }
        // the seventh slot of the grid is filled with an explanation card
        cardGrid.add(new ExplanationCard());

        info.setForeground(Theme.PAPER);
        info.setFont(Theme.font(16, false));
        startButton.setPreferredSize(new Dimension(260, 52));
        startButton.addActionListener(e -> start());
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(info, BorderLayout.CENTER);
        JPanel buttonHolder = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonHolder.setOpaque(false);
        buttonHolder.add(startButton);
        bottom.add(buttonHolder, BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(6, 30, 22, 30));
        center.add(cardGrid, BorderLayout.CENTER);
        center.add(bottom, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
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
            card.repaint();
        }
        startButton.setEnabled(picks.size() == PICKS);
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
        Dialogs.info(this, "Battlefield Draw", message);
        window.startBattle(setup, session, chosen);
    }

    /** A clickable card with the picture of a battlefield and its rules. */
    private final class FieldCard extends JComponent {
        private final BattlefieldType type;
        private boolean hover;

        FieldCard(BattlefieldType type) {
            this.type = type;
            setPreferredSize(new Dimension(220, 200));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    toggle(type);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            boolean chosen = picks.contains(type);
            g2.setColor(chosen ? Theme.GOLD : Theme.INK);
            g2.fillRect(0, 0, w, h);
            g2.setColor(hover ? Theme.mix(Theme.PAPER, Color.WHITE, 0.5) : Theme.PAPER);
            g2.fillRect(4, 4, w - 8, h - 8);
            BufferedImage backdrop = BackgroundFactory.backdrop(type);
            g2.drawImage(backdrop, 8, 8, w - 16, 94, null);
            g2.setColor(Theme.INK);
            g2.drawRect(7, 7, w - 15, 95);
            g2.setFont(Theme.font(15, true));
            g2.drawString(type.getDisplayName(), 10, 122);
            g2.setFont(Theme.font(11, false));
            GuiUtil.drawWrapped(g2, type.getDescription(), 10, 138, w - 20, 14);
            if (chosen) {
                int order = picks.indexOf(type) + 1;
                g2.setFont(Theme.font(13, true));
                GuiUtil.chip(g2, "Pick " + order, w - 70, 12, 22, Theme.ACCENT, Color.WHITE);
            }
            g2.dispose();
        }
    }

    /** Fills the eighth cell of the grid with a short explanation of how the field is drawn. */
    private static final class ExplanationCard extends JComponent {
        ExplanationCard() {
            setPreferredSize(new Dimension(220, 200));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 18));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(Theme.PAPER);
            g2.setFont(Theme.font(14, true));
            g2.drawString("Choosing the field", 14, 30);
            g2.setFont(Theme.font(13, false).deriveFont(Font.PLAIN));
            GuiUtil.drawWrapped(g2, "You pick 2 fields and the bot picks 1. The game then draws 1 of the 3 as the real battlefield. "
                    + "Dragons whose class matches the field get a bonus; other classes may be weakened.", 14, 54, getWidth() - 28, 19);
            g2.dispose();
        }
    }
}
