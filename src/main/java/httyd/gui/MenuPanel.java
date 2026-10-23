package httyd.gui;

import httyd.model.dragon.DragonSpecies;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * The main menu: a title, three flying dragons and the main buttons.
 */
public class MenuPanel extends ScreenPanel {
    private final RetroButton soundButton = new RetroButton("Sound: On", Theme.ACCENT_2);
    private final Timer animation;
    private double tick;
    private List<DragonSpecies> showcase;

    /**
     * @param window  the game window
     * @param context the shared game objects
     */
    public MenuPanel(GameWindow window, GameContext context) {
        super(window, context);
        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));
        buttons.add(button("Battle a Bot", Theme.ACCENT, window::showTeamSelect));
        buttons.add(button("Hatch a Dragon Egg", Theme.GOLD.darker(), window::showEgg));
        buttons.add(button("Dragon Collection", Theme.ACCENT_2, window::showCollection));
        buttons.add(button("Top Up Coins", new Color(0x3E9B5A), window::showTopUp));
        buttons.add(button("How to Play", new Color(0x5A6FD6), window::showHowTo));
        soundButton.setPreferredSize(new Dimension(300, 44));
        soundButton.setMaximumSize(new Dimension(300, 44));
        soundButton.setAlignmentX(CENTER_ALIGNMENT);
        soundButton.addActionListener(e -> toggleSound());
        buttons.add(Box.createVerticalStrut(10));
        buttons.add(soundButton);
        buttons.add(Box.createVerticalStrut(10));
        buttons.add(button("Quit", new Color(0x7A4A4A), () -> System.exit(0)));
        add(buttons, BorderLayout.SOUTH);
        animation = new Timer(40, e -> {
            tick += 0.06;
            repaint();
        });
    }

    private RetroButton button(String text, Color color, Runnable action) {
        RetroButton b = new RetroButton(text, color);
        b.setPreferredSize(new Dimension(300, 44));
        b.setMaximumSize(new Dimension(300, 44));
        b.setAlignmentX(CENTER_ALIGNMENT);
        b.addActionListener(e -> {
            context.getSound().play(SoundPlayer.Sound.CLICK);
            action.run();
        });
        buttonsGap(b);
        return b;
    }

    private void buttonsGap(RetroButton b) {
        b.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    }

    private void toggleSound() {
        SoundPlayer sound = context.getSound();
        sound.setMuted(!sound.isMuted());
        if (!sound.isMuted()) {
            sound.startMusic();
            sound.play(SoundPlayer.Sound.CLICK);
        }
        refreshSoundText();
    }

    private void refreshSoundText() {
        soundButton.setText(context.getSound().isMuted() ? "Sound: Off" : "Sound: On");
    }

    @Override
    public void onShow() {
        refreshSoundText();
        if (showcase == null) {
            showcase = List.of(context.getRepository().byId("toothless"),
                    context.getRepository().byId("bewilderbeast"),
                    context.getRepository().byId("stormfly"));
        }
        animation.start();
    }

    @Override
    public void removeNotify() {
        animation.stop();
        super.removeNotify();
    }

    @Override
    public void onHide() {
        animation.stop();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        BackgroundFactory.draw(g2, httyd.model.field.BattlefieldType.THUNDERSTORM_PEAK, getWidth(), getHeight());
        g2.setColor(new Color(10, 14, 30, 120));
        g2.fillRect(0, 0, getWidth(), getHeight());

        if (showcase != null) {
            int[] xs = {150, 860, 512};
            int[] ys = {260, 300, 18};
            for (int i = 0; i < showcase.size(); i++) {
                BufferedImage img = SpriteFactory.dragon(showcase.get(i), i != 1, i == 2 ? 3 : 6);
                int bob = (int) Math.round(Math.sin(tick + i * 1.7) * 10);
                g2.drawImage(img, xs[i] - img.getWidth() / 2, ys[i] + bob, null);
            }
        }
        String title = "DRAGON BATTLE";
        g2.setFont(Theme.font(92, true));
        FontMetrics fm = g2.getFontMetrics();
        int tx = (getWidth() - fm.stringWidth(title)) / 2;
        g2.setColor(Theme.INK);
        g2.drawString(title, tx + 6, 244 + 6);
        g2.setColor(Theme.GOLD);
        g2.drawString(title, tx, 244);
        String sub = "A HOW TO TRAIN YOUR DRAGON GAME";
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
        fm = g2.getFontMetrics();
        g2.setColor(Color.WHITE);
        g2.drawString(sub, (getWidth() - fm.stringWidth(sub)) / 2, 290);

        g2.setFont(Theme.font(16, true));
        g2.setColor(Theme.PAPER);
        String info = "Coins " + context.getProfile().getCoins() + "   |   Wins " + context.getProfile().getWins()
                + "  Losses " + context.getProfile().getLosses() + "  Draws " + context.getProfile().getDraws()
                + "   |   Dragons " + context.getProfile().getDragons().size();
        fm = g2.getFontMetrics();
        g2.drawString(info, (getWidth() - fm.stringWidth(info)) / 2, 326);
        g2.dispose();
    }
}
