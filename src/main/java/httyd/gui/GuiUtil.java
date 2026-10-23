package httyd.gui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Small drawing helpers shared by the screens.
 */
public final class GuiUtil {
    private static final Locale THAI = Locale.ENGLISH;

    private GuiUtil() {
    }

    /**
     * Splits text into lines that fit a width, breaking at the allowed line break points
     * (spaces and hyphens) and at newline characters.
     *
     * @param text  the text
     * @param fm    the metrics of the font that will draw it
     * @param width the maximum line width in pixels
     * @return the lines
     */
    public static List<String> wrap(String text, FontMetrics fm, int width) {
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\n", -1)) {
            BreakIterator it = BreakIterator.getLineInstance(THAI);
            it.setText(paragraph);
            StringBuilder line = new StringBuilder();
            int start = it.first();
            for (int end = it.next(); end != BreakIterator.DONE; start = end, end = it.next()) {
                String piece = paragraph.substring(start, end);
                if (line.length() > 0 && fm.stringWidth(line + piece.stripTrailing()) > width) {
                    lines.add(line.toString().stripTrailing());
                    line.setLength(0);
                }
                line.append(piece);
            }
            lines.add(line.toString().stripTrailing());
        }
        return lines;
    }

    /**
     * Draws wrapped text.
     *
     * @param g          the graphics
     * @param text       the text
     * @param x          left edge
     * @param y          baseline of the first line
     * @param width      maximum line width
     * @param lineHeight distance between baselines
     * @return the y of the baseline after the last line
     */
    public static int drawWrapped(Graphics2D g, String text, int x, int y, int width, int lineHeight) {
        int cursor = y;
        for (String line : wrap(text, g.getFontMetrics(), width)) {
            g.drawString(line, x, cursor);
            cursor += lineHeight;
        }
        return cursor;
    }

    /**
     * Draws a row of 5-point stars.
     *
     * @param g     the graphics
     * @param x     left edge
     * @param y     top edge
     * @param count the number of stars
     * @param size  the width of one star
     * @param color the colour
     */
    public static void stars(Graphics2D g, int x, int y, int count, int size, Color color) {
        for (int i = 0; i < count; i++) {
            Polygon p = new Polygon();
            double cx = x + i * (size + 2) + size / 2.0;
            double cy = y + size / 2.0;
            for (int k = 0; k < 10; k++) {
                double radius = (k % 2 == 0 ? size / 2.0 : size / 4.2);
                double angle = -Math.PI / 2 + k * Math.PI / 5;
                p.addPoint((int) Math.round(cx + Math.cos(angle) * radius),
                        (int) Math.round(cy + Math.sin(angle) * radius));
            }
            g.setColor(Theme.INK);
            g.fillPolygon(p);
            g.translate(0, 0);
            g.setColor(color);
            Polygon inner = new Polygon();
            for (int k = 0; k < p.npoints; k++) {
                double radius = (k % 2 == 0 ? size / 2.0 - 1.5 : size / 4.2 - 0.5);
                double angle = -Math.PI / 2 + k * Math.PI / 5;
                inner.addPoint((int) Math.round(cx + Math.cos(angle) * radius),
                        (int) Math.round(cy + Math.sin(angle) * radius));
            }
            g.fillPolygon(inner);
        }
    }

    /**
     * Draws a small label with a coloured background.
     *
     * @param g      the graphics (its font is used)
     * @param text   the label
     * @param x      left edge
     * @param y      top edge
     * @param height the height of the chip
     * @param face   the background colour
     * @param ink    the text colour
     * @return the width of the chip
     */
    public static int chip(Graphics2D g, String text, int x, int y, int height, Color face, Color ink) {
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(text) + 14;
        g.setColor(Theme.INK);
        g.fillRect(x, y, w, height);
        g.setColor(face);
        g.fillRect(x + 2, y + 2, w - 4, height - 4);
        g.setColor(ink);
        g.drawString(text, x + 7, y + (height - fm.getHeight()) / 2 + fm.getAscent());
        return w;
    }

    /**
     * Draws a horizontal bar with a dark frame.
     *
     * @param g      the graphics
     * @param x      left edge
     * @param y      top edge
     * @param w      width
     * @param h      height
     * @param ratio  how full the bar is, from 0 to 1
     * @param color  the fill colour
     */
    public static void bar(Graphics2D g, int x, int y, int w, int h, double ratio, Color color) {
        g.setColor(Theme.INK);
        g.fillRect(x, y, w, h);
        g.setColor(new Color(0x39425A));
        g.fillRect(x + 2, y + 2, w - 4, h - 4);
        int fill = (int) Math.round((w - 4) * Math.max(0, Math.min(1, ratio)));
        if (fill > 0) {
            g.setColor(color);
            g.fillRect(x + 2, y + 2, fill, h - 4);
            g.setColor(Theme.mix(color, Color.WHITE, 0.35));
            g.fillRect(x + 2, y + 2, fill, Math.max(1, (h - 4) / 3));
        }
    }
}
