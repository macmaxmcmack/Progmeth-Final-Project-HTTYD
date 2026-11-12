package httyd.fx;

import java.io.InputStream;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Colours and fonts shared by every screen, so the whole game has one retro look. The fonts are
 * bundled in the jar (DejaVu Sans, see {@code fonts/LICENSE-DejaVu.txt}) so the game looks the same on
 * every operating system.
 */
public final class Theme {
    /** Dark navy used for panels and outlines. */
    public static final Color INK = rgb(0x1B2230);
    /** Cream used for text boxes. */
    public static final Color PAPER = rgb(0xF6EFD9);
    /** Slightly darker cream for borders of boxes. */
    public static final Color PAPER_DARK = rgb(0xC9BC93);
    /** Main accent colour. */
    public static final Color ACCENT = rgb(0xE8603C);
    /** Secondary accent colour. */
    public static final Color ACCENT_2 = rgb(0x2F8F9D);
    /** Gold used for rare things. */
    public static final Color GOLD = rgb(0xF2B531);
    /** Green HP. */
    public static final Color HP_GOOD = rgb(0x48C75A);
    /** Yellow HP. */
    public static final Color HP_MID = rgb(0xF0C030);
    /** Red HP. */
    public static final Color HP_LOW = rgb(0xE0463C);
    /** Screen background behind the cards. */
    public static final Color BACKGROUND = rgb(0x232B3B);

    private static final String SANS = "DejaVu Sans";
    private static final String MONO = "DejaVu Sans Mono";
    private static boolean loaded;

    private Theme() {
    }

    /**
     * Makes a colour from a 24-bit RGB number such as {@code 0x6EC1F0}.
     *
     * @param rgb the colour as a number
     * @return the colour
     */
    public static Color rgb(int rgb) {
        return Color.rgb((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }

    /**
     * Returns the game font.
     *
     * @param size the size in pixels
     * @param bold true for bold
     * @return the font
     */
    public static Font font(double size, boolean bold) {
        loadFonts();
        return Font.font(SANS, bold ? FontWeight.BOLD : FontWeight.NORMAL, size);
    }

    /**
     * Returns the bold monospaced font.
     *
     * @param size the size in pixels
     * @return the font
     */
    public static Font mono(double size) {
        loadFonts();
        return Font.font(MONO, FontWeight.BOLD, size);
    }

    private static synchronized void loadFonts() {
        if (loaded) {
            return;
        }
        loaded = true;
        for (String file : new String[] {"DejaVuSans.ttf", "DejaVuSans-Bold.ttf", "DejaVuSansMono-Bold.ttf"}) {
            try (InputStream in = Theme.class.getResourceAsStream("/fonts/" + file)) {
                if (in != null) {
                    Font.loadFont(in, 12);
                }
            } catch (java.io.IOException e) {
                System.err.println("Could not load font " + file);
            }
        }
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
        return a.interpolate(b, amount);
    }

    /**
     * Makes a colour lighter.
     *
     * @param c      the colour
     * @param amount 0 to 1
     * @return the lighter colour
     */
    public static Color lighter(Color c, double amount) {
        return mix(c, Color.WHITE, amount);
    }

    /**
     * Makes a colour darker.
     *
     * @param c      the colour
     * @param amount 0 to 1
     * @return the darker colour
     */
    public static Color darker(Color c, double amount) {
        return mix(c, Color.BLACK, amount);
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
