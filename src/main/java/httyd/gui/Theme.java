package httyd.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Colours and fonts shared by every screen, so the whole game has one retro look.
 */
public final class Theme {
    /** Dark navy used for panels and outlines. */
    public static final Color INK = new Color(0x1B2230);
    /** Cream used for text boxes. */
    public static final Color PAPER = new Color(0xF6EFD9);
    /** Slightly darker cream for borders of boxes. */
    public static final Color PAPER_DARK = new Color(0xC9BC93);
    /** Main accent colour. */
    public static final Color ACCENT = new Color(0xE8603C);
    /** Secondary accent colour. */
    public static final Color ACCENT_2 = new Color(0x2F8F9D);
    /** Gold used for rare things. */
    public static final Color GOLD = new Color(0xF2B531);
    /** Green HP. */
    public static final Color HP_GOOD = new Color(0x48C75A);
    /** Yellow HP. */
    public static final Color HP_MID = new Color(0xF0C030);
    /** Red HP. */
    public static final Color HP_LOW = new Color(0xE0463C);
    /** Screen background behind the cards. */
    public static final Color BACKGROUND = new Color(0x232B3B);

    private static final String[] FONT_CANDIDATES = {
        "Trebuchet MS", "Segoe UI", "Helvetica Neue", "Arial", "DejaVu Sans", "Dialog"
    };
    private static String family;

    private Theme() {
    }

    /**
     * Returns the game font (the first installed family of a short preference list).
     *
     * @param size  the size in points
     * @param bold  true for bold
     * @return the font
     */
    public static Font font(float size, boolean bold) {
        return new Font(resolveFamily(), bold ? Font.BOLD : Font.PLAIN, Math.round(size));
    }

    private static synchronized String resolveFamily() {
        if (family == null) {
            Set<String> installed = new HashSet<>(Arrays.asList(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
            family = "Dialog";
            for (String candidate : FONT_CANDIDATES) {
                if (installed.contains(candidate) && new Font(candidate, Font.PLAIN, 12).canDisplay('A')) {
                    family = candidate;
                    break;
                }
            }
        }
        return family;
    }

    /**
     * Mixes two colours.
     *
     * @param a      first colour
     * @param b      second colour
     * @param amount 0 gives {@code a}, 1 gives {@code b}
     * @return the mixed colour
     */
    public static Color mix(Color a, Color b, double amount) {
        int r = (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * amount);
        int g = (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * amount);
        int bl = (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * amount);
        return new Color(r, g, bl);
    }

    /**
     * Picks the colour of an HP bar.
     *
     * @param ratio HP divided by max HP
     * @return green, yellow or red
     */
    public static Color hpColor(double ratio) {
        if (ratio > 0.5) {
            return HP_GOOD;
        }
        return ratio > 0.2 ? HP_MID : HP_LOW;
    }
}
