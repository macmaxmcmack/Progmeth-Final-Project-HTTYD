package httyd.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

/**
 * A chunky button with a flat colour, a thick dark border and a drop shadow, drawn by hand so it
 * looks the same on every operating system.
 */
public class RetroButton extends JButton {
    private static final int SHADOW = 4;
    private Color baseColor;
    private boolean hover;
    private boolean pressed;

    /**
     * @param text  the label
     * @param color the face colour
     */
    public RetroButton(String text, Color color) {
        super(text);
        this.baseColor = color;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setForeground(Color.WHITE);
        setFont(Theme.font(17, true));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(200, 46));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                pressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
                repaint();
            }
        });
    }

    /**
     * Changes the face colour (used by toggle buttons).
     *
     * @param color the new face colour
     */
    public void setColor(Color color) {
        this.baseColor = color;
        repaint();
    }

    /** Creates a button with the default accent colour. */
    public RetroButton(String text) {
        this(text, Theme.ACCENT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int bodyHeight = getHeight() - SHADOW;
        int dy = pressed ? SHADOW - 1 : 0;
        Color face = !isEnabled() ? new Color(0x6B7280)
                : hover ? Theme.mix(baseColor, Color.WHITE, 0.18) : baseColor;
        // drop shadow under the button
        g2.setColor(Theme.INK);
        g2.fillRect(2, SHADOW, w - 4, bodyHeight);
        // dark border, face and a light strip along the top edge
        g2.fillRect(0, dy, w, bodyHeight);
        g2.setColor(face);
        g2.fillRect(3, dy + 3, w - 6, bodyHeight - 6);
        g2.setColor(Theme.mix(face, Color.WHITE, 0.35));
        g2.fillRect(3, dy + 3, w - 6, 3);
        // label
        g2.setColor(isEnabled() ? getForeground() : new Color(0xC9CDD4));
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(getText())) / 2;
        int ty = dy + (bodyHeight - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), tx, ty);
        g2.dispose();
    }
}
