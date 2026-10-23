package httyd.gui;

import httyd.model.dragon.Grade;
import httyd.model.dragon.OwnedDragon;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 * A small card showing one owned dragon. It can be selected and can carry a number badge (the
 * place in the team).
 */
public class DragonTile extends JComponent {
    /** Width of a tile. */
    public static final int WIDTH = 160;
    /** Height of a tile. */
    public static final int HEIGHT = 142;

    private final OwnedDragon dragon;
    private boolean selected;
    private boolean hover;
    private boolean dimmed;
    private int badge;

    /**
     * @param dragon  the dragon to show
     * @param onClick called when the tile is clicked, may be {@code null}
     */
    public DragonTile(OwnedDragon dragon, Runnable onClick) {
        this.dragon = dragon;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
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
                if (onClick != null) {
                    onClick.run();
                }
            }
        });
    }

    /** @return the dragon on this tile */
    public OwnedDragon getDragon() {
        return dragon;
    }

    /** @param selected true to draw the highlighted frame */
    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    /** @param dimmed true to draw the tile faded (it cannot be chosen now) */
    public void setDimmed(boolean dimmed) {
        this.dimmed = dimmed;
        repaint();
    }

    /** @param badge the place number to show in the corner, 0 for none */
    public void setBadge(int badge) {
        this.badge = badge;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        Color classColor = new Color(dragon.getSpecies().getDragonClass().getColorRgb());
        g2.setColor(selected ? Theme.GOLD : Theme.INK);
        g2.fillRect(0, 0, w, h);
        Color face = hover ? Theme.mix(Theme.PAPER, Color.WHITE, 0.5) : Theme.PAPER;
        g2.setColor(face);
        g2.fillRect(selected ? 5 : 3, selected ? 5 : 3, w - (selected ? 10 : 6), h - (selected ? 10 : 6));
        g2.setColor(classColor);
        g2.fillRect(3, 3, w - 6, 6);

        BufferedImage sprite = SpriteFactory.dragon(dragon.getSpecies(), true, 2);
        g2.drawImage(sprite, (w - sprite.getWidth()) / 2, 12, null);

        g2.setColor(Theme.INK);
        g2.setFont(Theme.font(15, true));
        String name = dragon.getSpecies().getShortName();
        int nameWidth = g2.getFontMetrics().stringWidth(name);
        g2.drawString(name, Math.max(6, (w - nameWidth) / 2), 105);

        g2.setFont(Theme.font(12, false));
        g2.drawString(dragon.getSpecies().getDragonClass().getDisplayName(), 8, 126);
        if (dragon.getStar() > 0) {
            GuiUtil.stars(g2, 8 + g2.getFontMetrics()
                    .stringWidth(dragon.getSpecies().getDragonClass().getDisplayName()) + 6, 114, dragon.getStar(), 12, Theme.GOLD);
        }
        Grade grade = dragon.getGrade();
        g2.setFont(Theme.font(12, true));
        Color gradeColor = grade == Grade.SSS ? Theme.GOLD : grade == Grade.A ? new Color(0x8FD694) : new Color(0xB8B8B8);
        int chipW = g2.getFontMetrics().stringWidth(grade.getLabel()) + 14;
        GuiUtil.chip(g2, grade.getLabel(), w - chipW - 6, 114, 18, gradeColor, Theme.INK);
        if (dragon.isAlpha()) {
            g2.setFont(Theme.font(11, true));
            GuiUtil.chip(g2, "Alpha", w - 52, 14, 17, new Color(0xB34CD6), Color.WHITE);
        }
        if (badge > 0) {
            g2.setColor(Theme.INK);
            g2.fillOval(5, 12, 26, 26);
            g2.setColor(Theme.ACCENT);
            g2.fillOval(7, 14, 22, 22);
            g2.setColor(Color.WHITE);
            g2.setFont(Theme.font(16, true));
            String number = String.valueOf(badge);
            g2.drawString(number, 18 - g2.getFontMetrics().stringWidth(number) / 2, 31);
        }
        if (dimmed) {
            g2.setColor(new Color(40, 44, 58, 150));
            g2.fillRect(0, 0, w, h);
        }
        g2.dispose();
    }
}
