package httyd.gui;

import httyd.data.CoinPack;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * The demo coin shop. Every pack is free: pressing a card adds the coins at once and no real
 * money is involved.
 */
public class TopUpPanel extends ScreenPanel {
    private static final NumberFormat NUMBERS = NumberFormat.getIntegerInstance(Locale.US);

    /** A "+500" that floats up from the balance after a top-up. */
    private static final class Floater {
        final String text;
        double y = 0;
        double life = 1.0;

        Floater(String text) {
            this.text = text;
        }
    }

    private final Stage stage = new Stage();
    private final Timer timer;
    private final List<Floater> floaters = new ArrayList<>();
    private double shownCoins;
    private String lastMessage = "";
    private long lastMessageAt;

    /**
     * @param window  the game window
     * @param context the shared game objects
     */
    public TopUpPanel(GameWindow window, GameContext context) {
        super(window, context);
        add(header("Top Up Coins", "Main Menu", window::showMenu), BorderLayout.NORTH);
        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.setBorder(BorderFactory.createEmptyBorder(6, 24, 24, 24));
        holder.add(stage, BorderLayout.CENTER);
        add(holder, BorderLayout.CENTER);
        timer = new Timer(33, e -> tick());
    }

    @Override
    public void onShow() {
        shownCoins = context.getProfile().getCoins();
        floaters.clear();
        lastMessage = "";
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
        stage.repaint();
    }

    /** The drawing area: balance, four pack cards and a notice. */
    private final class Stage extends JComponent {
        private static final int CARD_W = 210;
        private static final int CARD_H = 290;
        private static final int GAP = 20;
        private static final int CARD_TOP = 150;
        private int hover = -1;

        Stage() {
            setPreferredSize(new Dimension(960, 560));
            setCursor(Cursor.getDefaultCursor());
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int index = cardAt(e.getX(), e.getY());
                    if (index != hover) {
                        hover = index;
                        setCursor(Cursor.getPredefinedCursor(index >= 0 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                    }
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int index = cardAt(e.getX(), e.getY());
                    if (index >= 0) {
                        claim(CoinPack.values()[index]);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = -1;
                }
            });
        }

        private int left() {
            int total = CoinPack.values().length * CARD_W + (CoinPack.values().length - 1) * GAP;
            return (getWidth() - total) / 2;
        }

        private int cardAt(int x, int y) {
            if (y < CARD_TOP || y > CARD_TOP + CARD_H) {
                return -1;
            }
            for (int i = 0; i < CoinPack.values().length; i++) {
                int cx = left() + i * (CARD_W + GAP);
                if (x >= cx && x <= cx + CARD_W) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();

            // balance
            g2.setFont(Theme.font(16, true));
            g2.setColor(Theme.PAPER);
            String label = "Your coins";
            g2.drawString(label, (w - g2.getFontMetrics().stringWidth(label)) / 2, 30);
            String balance = NUMBERS.format(Math.round(shownCoins));
            g2.setFont(Theme.font(54, true));
            int bw = g2.getFontMetrics().stringWidth(balance);
            int bx = (w - bw) / 2;
            drawCoin(g2, bx - 52, 40, 40);
            g2.setColor(Theme.INK);
            g2.drawString(balance, bx + 3, 88 + 3);
            g2.setColor(Theme.GOLD);
            g2.drawString(balance, bx, 88);
            for (Floater f : floaters) {
                g2.setFont(Theme.font(30, true));
                int a = (int) Math.max(0, Math.min(255, f.life * 400));
                g2.setColor(new Color(0x66, 0xE0, 0x7A, a));
                g2.drawString(f.text, bx + bw + 20, (int) (84 + f.y));
            }
            if (!lastMessage.isEmpty() && System.currentTimeMillis() - lastMessageAt < 3500) {
                g2.setFont(Theme.font(16, true));
                g2.setColor(new Color(0x66, 0xE0, 0x7A));
                g2.drawString(lastMessage, (w - g2.getFontMetrics().stringWidth(lastMessage)) / 2, 122);
            } else {
                g2.setFont(Theme.font(15, false));
                g2.setColor(Theme.PAPER_DARK);
                String hint = "Pick a pack. The coins arrive right away.";
                g2.drawString(hint, (w - g2.getFontMetrics().stringWidth(hint)) / 2, 122);
            }

            // cards
            CoinPack[] packs = CoinPack.values();
            for (int i = 0; i < packs.length; i++) {
                drawCard(g2, packs[i], i, left() + i * (CARD_W + GAP), CARD_TOP);
            }

            // notice
            g2.setFont(Theme.font(14, false));
            g2.setColor(Theme.PAPER);
            String notice = "Demo shop: every pack is free. No real money is charged or needed.";
            g2.drawString(notice, (w - g2.getFontMetrics().stringWidth(notice)) / 2, CARD_TOP + CARD_H + 44);
            g2.dispose();
        }

        private void drawCard(Graphics2D g2, CoinPack pack, int index, int x, int y) {
            boolean over = hover == index;
            int lift = over ? -6 : 0;
            y += lift;
            g2.setColor(Theme.INK);
            g2.fillRect(x, y, CARD_W, CARD_H);
            g2.setColor(over ? Theme.mix(Theme.PAPER, Color.WHITE, 0.5) : Theme.PAPER);
            g2.fillRect(x + 4, y + 4, CARD_W - 8, CARD_H - 8);
            g2.setColor(Theme.mix(Theme.GOLD, Theme.PAPER, 0.55));
            g2.fillRect(x + 4, y + 4, CARD_W - 8, 120);

            // a stack of coins that grows with the pack size
            int stacks = index + 1;
            for (int s = 0; s < stacks; s++) {
                int sx = x + CARD_W / 2 - (stacks * 30) / 2 + s * 30 + 8;
                int height = 2 + Math.min(5, index + 2 + (s % 2));
                for (int c = 0; c < height; c++) {
                    drawCoin(g2, sx, y + 100 - c * 7, 28);
                }
            }

            g2.setColor(Theme.INK);
            g2.setFont(Theme.font(18, true));
            String name = pack.getDisplayName();
            g2.drawString(name, x + (CARD_W - g2.getFontMetrics().stringWidth(name)) / 2, y + 156);
            g2.setFont(Theme.font(30, true));
            String amount = "+" + NUMBERS.format(pack.getCoins());
            g2.drawString(amount, x + (CARD_W - g2.getFontMetrics().stringWidth(amount)) / 2, y + 198);
            g2.setFont(Theme.font(13, false));
            String coins = "coins";
            g2.drawString(coins, x + (CARD_W - g2.getFontMetrics().stringWidth(coins)) / 2, y + 218);

            // FREE button
            g2.setFont(Theme.font(18, true));
            int bw = 150;
            int bx = x + (CARD_W - bw) / 2;
            int by = y + CARD_H - 56;
            g2.setColor(Theme.INK);
            g2.fillRect(bx, by, bw, 40);
            g2.setColor(over ? Theme.mix(Theme.ACCENT, Color.WHITE, 0.2) : Theme.ACCENT);
            g2.fillRect(bx + 3, by + 3, bw - 6, 34);
            g2.setColor(Color.WHITE);
            String free = "GET FREE";
            g2.drawString(free, bx + (bw - g2.getFontMetrics().stringWidth(free)) / 2, by + 27);
        }

        private void drawCoin(Graphics2D g2, int x, int y, int size) {
            g2.setColor(Theme.INK);
            g2.fillOval(x, y, size, size * 2 / 3);
            g2.setColor(Theme.GOLD);
            g2.fillOval(x + 2, y + 2, size - 4, size * 2 / 3 - 4);
            g2.setColor(Theme.mix(Theme.GOLD, Color.WHITE, 0.5));
            g2.fillOval(x + size / 4, y + size / 10, size / 2, size / 6);
        }
    }
}
