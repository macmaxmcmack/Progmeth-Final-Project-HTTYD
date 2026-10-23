package httyd.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Base class of the full-window screens: a dark patterned background and a title bar.
 */
public abstract class ScreenPanel extends JPanel {
    /** The game window used for navigation. */
    protected final GameWindow window;
    /** The shared game objects. */
    protected final GameContext context;

    /**
     * @param window  the game window
     * @param context the shared game objects
     */
    protected ScreenPanel(GameWindow window, GameContext context) {
        this.window = window;
        this.context = context;
        setLayout(new BorderLayout());
    }

    /** Called every time the screen is shown, to refresh its data. */
    public void onShow() {
    }

    /** Called when another screen replaces this one, to stop animation timers. */
    public void onHide() {
    }

    /**
     * Builds the title bar.
     *
     * @param title    the title text
     * @param backText the label of the back button, or {@code null} for none
     * @param back     what the back button does
     * @return the bar
     */
    protected JPanel header(String title, String backText, Runnable back) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(14, 20, 6, 20));
        JLabel label = new JLabel(title);
        label.setForeground(Color.WHITE);
        label.setFont(Theme.font(30, true));
        bar.add(label, BorderLayout.WEST);
        if (backText != null) {
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            right.setOpaque(false);
            RetroButton button = new RetroButton(backText, Theme.ACCENT_2);
            button.setPreferredSize(new Dimension(170, 42));
            button.addActionListener(e -> {
                context.getSound().play(SoundPlayer.Sound.CLICK);
                back.run();
            });
            right.add(button);
            bar.add(right, BorderLayout.EAST);
        }
        return bar;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Theme.BACKGROUND);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(new Color(255, 255, 255, 10));
        for (int i = -getHeight(); i < getWidth(); i += 36) {
            g2.drawLine(i, 0, i + getHeight(), getHeight());
            g2.drawLine(i + 1, 0, i + 1 + getHeight(), getHeight());
        }
        g2.dispose();
    }
}
