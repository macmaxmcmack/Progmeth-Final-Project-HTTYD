package httyd.gui;

import httyd.model.GameRules;
import httyd.model.dragon.OwnedDragon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * The egg screen: the player pays coins, the egg shakes and cracks, and a random dragon hatches.
 */
public class EggPanel extends ScreenPanel {
    private enum Phase { IDLE, SHAKING, FLASH, REVEALED }

    private static final int SHAKE_MILLIS = 2000;
    private static final int FLASH_MILLIS = 350;

    private final EggStage stage = new EggStage();
    private final DragonDetailPanel detail;
    private final RetroButton openButton = new RetroButton("");
    private final Timer timer;
    private Phase phase = Phase.IDLE;
    private long phaseStart;
    private int lastCrack;
    private OwnedDragon hatched;

    /**
     * @param window  the game window
     * @param context the shared game objects
     */
    public EggPanel(GameWindow window, GameContext context) {
        super(window, context);
        detail = new DragonDetailPanel(context.getRepository());
        detail.setHint("The hatched dragon will appear here");
        add(header("Hatch a Dragon Egg", "Main Menu", window::showMenu), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(20, 0));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(6, 24, 20, 24));
        center.add(stage, BorderLayout.CENTER);
        JPanel right = new JPanel(new BorderLayout(0, 10));
        right.setOpaque(false);
        right.add(detail, BorderLayout.CENTER);
        openButton.setPreferredSize(new Dimension(430, 54));
        openButton.addActionListener(e -> openEgg());
        right.add(openButton, BorderLayout.SOUTH);
        center.add(right, BorderLayout.EAST);
        add(center, BorderLayout.CENTER);

        timer = new Timer(33, e -> tick());
    }

    @Override
    public void onShow() {
        phase = Phase.IDLE;
        hatched = null;
        detail.setDragon(null);
        refreshButton();
        timer.start();
    }

    @Override
    public void onHide() {
        timer.stop();
    }

    @Override
    public void removeNotify() {
        timer.stop();
        super.removeNotify();
    }

    private void refreshButton() {
        boolean idle = phase == Phase.IDLE || phase == Phase.REVEALED;
        boolean affordable = context.getProfile().canAffordEgg();
        openButton.setEnabled(idle);
        if (!affordable) {
            openButton.setText("Not enough coins - Top Up");
        } else {
            openButton.setText("Open Egg  (" + GameRules.EGG_PRICE + " coins)");
        }
        stage.repaint();
    }

    private void openEgg() {
        if (!context.getProfile().canAffordEgg()) {
            window.showTopUp();
            return;
        }
        hatched = context.getProfile().buyEgg(context.getGacha());
        context.save();
        context.getSound().play(SoundPlayer.Sound.CLICK);
        detail.setDragon(null);
        detail.setHint("Hatching...");
        phase = Phase.SHAKING;
        phaseStart = System.currentTimeMillis();
        lastCrack = 0;
        refreshButton();
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
                    detail.setDragon(hatched);
                    refreshButton();
                }
            }
            default -> { }
        }
        stage.repaint();
    }

    /** The area that draws the egg. */
    private final class EggStage extends JComponent {
        EggStage() {
            setPreferredSize(new Dimension(500, 500));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(Theme.INK);
            g2.fillRect(0, 0, w, h);
            g2.setColor(new Color(0x2E3850));
            g2.fillRect(4, 4, w - 8, h - 8);
            long elapsed = System.currentTimeMillis() - phaseStart;

            // glow behind the egg
            int cx = w / 2;
            int cy = h / 2 - 20;
            for (int r = 220; r > 40; r -= 20) {
                g2.setColor(new Color(255, 230, 140, 8));
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);
            }
            // ground
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillOval(cx - 110, cy + 150, 220, 40);

            long now = System.currentTimeMillis();
            if (phase == Phase.IDLE || phase == Phase.SHAKING) {
                int crack = phase == Phase.SHAKING ? lastCrack : 0;
                BufferedImage egg = SpriteFactory.egg(10, crack);
                double amplitude = phase == Phase.SHAKING ? 4 + 14.0 * Math.min(1, elapsed / (double) SHAKE_MILLIS) : 0;
                int dx = (int) Math.round(Math.sin(now / (phase == Phase.SHAKING ? 28.0 : 400.0)) * amplitude);
                int dy = phase == Phase.IDLE ? (int) Math.round(Math.sin(now / 300.0) * 6) : 0;
                g2.drawImage(egg, cx - egg.getWidth() / 2 + dx, cy - egg.getHeight() / 2 + dy, null);
            } else if (phase == Phase.FLASH) {
                int alpha = (int) Math.max(0, 255 - 255 * elapsed / FLASH_MILLIS);
                g2.setColor(new Color(255, 255, 255, alpha));
                g2.fillRect(4, 4, w - 8, h - 8);
            } else if (hatched != null) {
                BufferedImage sprite = SpriteFactory.dragon(hatched.getSpecies(), true, 7);
                int bob = (int) Math.round(Math.sin(now / 250.0) * 5);
                g2.drawImage(sprite, cx - sprite.getWidth() / 2, cy - sprite.getHeight() / 2 + bob, null);
                g2.setFont(Theme.font(30, true));
                String text = hatched.getSpecies().getName() + "!";
                int tw = g2.getFontMetrics().stringWidth(text);
                g2.setColor(Theme.INK);
                g2.drawString(text, cx - tw / 2 + 3, 63);
                g2.setColor(Theme.GOLD);
                g2.drawString(text, cx - tw / 2, 60);
            }
            g2.setFont(Theme.font(16, true));
            g2.setColor(Theme.PAPER);
            String coins = "Your coins: " + context.getProfile().getCoins();
            g2.drawString(coins, 20, h - 18);
            String hint = "Each egg rolls random IVs, a Nature and a " + GameRules.ALPHA_CHANCE_PERCENT + "% chance of being an Alpha";
            g2.setFont(Theme.font(13, false));
            g2.drawString(hint, w - 20 - g2.getFontMetrics().stringWidth(hint), h - 18);
            g2.dispose();
        }
    }
}
