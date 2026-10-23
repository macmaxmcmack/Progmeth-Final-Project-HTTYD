package httyd.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import javax.swing.JPanel;

/**
 * A panel with a cream face and a thick dark double border, used for text boxes and cards.
 */
public class RetroPanel extends JPanel {
    private final Color face;

    /**
     * @param layout the layout manager
     * @param face   the face colour
     */
    public RetroPanel(LayoutManager layout, Color face) {
        super(layout);
        this.face = face;
        setOpaque(false);
    }

    /** Creates a cream panel. */
    public RetroPanel(LayoutManager layout) {
        this(layout, Theme.PAPER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();
        g2.setColor(Theme.INK);
        g2.fillRect(0, 0, w, h);
        g2.setColor(face);
        g2.fillRect(4, 4, w - 8, h - 8);
        g2.setColor(Theme.mix(face, Theme.INK, 0.35));
        g2.fillRect(4, 4, w - 8, 3);
        g2.fillRect(4, 4, 3, h - 8);
        g2.setColor(Theme.INK);
        g2.drawRect(7, 7, w - 15, h - 15);
        g2.dispose();
        super.paintComponent(g);
    }
}
