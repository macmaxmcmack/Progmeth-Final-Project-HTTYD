package httyd.fx;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.ToDoubleFunction;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Small drawing helpers shared by the screens that paint onto a {@link javafx.scene.canvas.Canvas}.
 */
public final class Draw {
    private Draw() {
    }

    /**
     * Splits text into lines that fit a width, breaking at the allowed line break points
     * (spaces and hyphens) and at newline characters. The measuring function is a parameter so the
     * method can be tested without a JavaFX toolkit.
     *
     * @param text    the text
     * @param measure gives the width in pixels of a string
     * @param width   the maximum line width in pixels
     * @return the lines
     */
    public static List<String> wrap(String text, ToDoubleFunction<String> measure, double width) {
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\n", -1)) {
            BreakIterator it = BreakIterator.getLineInstance(Locale.ENGLISH);
            it.setText(paragraph);
            StringBuilder line = new StringBuilder();
            int start = it.first();
            for (int end = it.next(); end != BreakIterator.DONE; start = end, end = it.next()) {
                String piece = paragraph.substring(start, end);
                if (line.length() > 0 && measure.applyAsDouble(line + piece.stripTrailing()) > width) {
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
     * Measures the width of a string in a font.
     *
     * @param font the font
     * @param text the string
     * @return the width in pixels
     */
    public static double width(Font font, String text) {
        Text t = new Text(text);
        t.setFont(font);
        return t.getLayoutBounds().getWidth();
    }

    /**
     * Draws wrapped text with the current font and fill of the context.
     *
     * @param g          the graphics context
     * @param text       the text
     * @param x          left edge
     * @param y          baseline of the first line
     * @param width      maximum line width
     * @param lineHeight distance between baselines
     * @return the y of the baseline after the last line
     */
    public static double wrapped(GraphicsContext g, String text, double x, double y, double width, double lineHeight) {
        Font font = g.getFont();
        double cursor = y;
        for (String line : wrap(text, s -> width(font, s), width)) {
            g.fillText(line, x, cursor);
            cursor += lineHeight;
        }
        return cursor;
    }

    /**
     * Draws text centred on an x position.
     *
     * @param g    the graphics context
     * @param text the text
     * @param cx   the centre
     * @param y    the baseline
     */
    public static void centered(GraphicsContext g, String text, double cx, double y) {
        g.fillText(text, cx - width(g.getFont(), text) / 2, y);
    }

    /**
     * Draws text with a hard dark shadow.
     *
     * @param g      the graphics context
     * @param text   the text
     * @param x      left edge
     * @param y      baseline
     * @param color  the text colour
     */
    public static void shadowed(GraphicsContext g, String text, double x, double y, Color color) {
        g.setFill(Theme.INK);
        g.fillText(text, x + 3, y + 3);
        g.setFill(color);
        g.fillText(text, x, y);
    }

    /**
     * Draws a row of 5-point stars.
     *
     * @param g     the graphics context
     * @param x     left edge
     * @param y     top edge
     * @param count the number of stars
     * @param size  the width of one star
     * @param color the colour
     */
    public static void stars(GraphicsContext g, double x, double y, int count, double size, Color color) {
        for (int i = 0; i < count; i++) {
            double cx = x + i * (size + 2) + size / 2.0;
            double cy = y + size / 2.0;
            g.setFill(Theme.INK);
            polygon(g, cx, cy, size / 2.0, size / 4.2);
            g.setFill(color);
            polygon(g, cx, cy, size / 2.0 - 1.5, size / 4.2 - 0.5);
        }
    }

    private static void polygon(GraphicsContext g, double cx, double cy, double outer, double inner) {
        double[] xs = new double[10];
        double[] ys = new double[10];
        for (int k = 0; k < 10; k++) {
            double radius = k % 2 == 0 ? outer : inner;
            double angle = -Math.PI / 2 + k * Math.PI / 5;
            xs[k] = cx + Math.cos(angle) * radius;
            ys[k] = cy + Math.sin(angle) * radius;
        }
        g.fillPolygon(xs, ys, 10);
    }

    /**
     * Draws a small label with a coloured background, using the current font.
     *
     * @param g      the graphics context
     * @param text   the label
     * @param x      left edge
     * @param y      top edge
     * @param height the height of the chip
     * @param face   the background colour
     * @param ink    the text colour
     * @return the width of the chip
     */
    public static double chip(GraphicsContext g, String text, double x, double y, double height, Color face, Color ink) {
        double w = width(g.getFont(), text) + 14;
        g.setFill(Theme.INK);
        g.fillRect(x, y, w, height);
        g.setFill(face);
        g.fillRect(x + 2, y + 2, w - 4, height - 4);
        g.setFill(ink);
        // the baseline sits about 72% of the way down the text line
        g.fillText(text, x + 7, y + height / 2 + g.getFont().getSize() * 0.36);
        return w;
    }

    /**
     * Draws a horizontal bar with a dark frame.
     *
     * @param g     the graphics context
     * @param x     left edge
     * @param y     top edge
     * @param w     width
     * @param h     height
     * @param ratio how full the bar is, 0 to 1
     * @param color the fill colour
     */
    public static void bar(GraphicsContext g, double x, double y, double w, double h, double ratio, Color color) {
        g.setFill(Theme.INK);
        g.fillRect(x, y, w, h);
        g.setFill(Theme.rgb(0x39425A));
        g.fillRect(x + 2, y + 2, w - 4, h - 4);
        double fill = Math.round((w - 4) * Math.max(0, Math.min(1, ratio)));
        if (fill > 0) {
            g.setFill(color);
            g.fillRect(x + 2, y + 2, fill, h - 4);
            g.setFill(Theme.mix(color, Color.WHITE, 0.35));
            g.fillRect(x + 2, y + 2, fill, Math.max(1, (h - 4) / 3));
        }
    }

    /**
     * Draws a stack-able gold coin seen from a slight angle.
     *
     * @param g    the graphics context
     * @param x    left edge
     * @param y    top edge
     * @param size the width of the coin
     */
    public static void coin(GraphicsContext g, double x, double y, double size) {
        double h = size * 2 / 3;
        g.setFill(Theme.INK);
        g.fillOval(x, y, size, h);
        g.setFill(Theme.GOLD);
        g.fillOval(x + 2, y + 2, size - 4, h - 4);
        g.setFill(Theme.mix(Theme.GOLD, Color.WHITE, 0.5));
        g.fillOval(x + size / 4, y + size / 10, size / 2, size / 6);
    }
}
