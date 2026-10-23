package httyd.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Small helpers for drawing pixel art: scaling without blur, outlines and colour tweaks.
 */
public final class Pixels {
    private Pixels() {
    }

    /**
     * Scales an image up with nearest-neighbour sampling so pixels stay sharp.
     *
     * @param src   the source image
     * @param scale integer scale factor
     * @return the scaled image
     */
    public static BufferedImage scale(BufferedImage src, int scale) {
        BufferedImage out = new BufferedImage(src.getWidth() * scale, src.getHeight() * scale, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(src, 0, 0, out.getWidth(), out.getHeight(), null);
        g.dispose();
        return out;
    }

    /**
     * Mirrors an image left to right.
     *
     * @param src the source image
     * @return the mirrored copy
     */
    public static BufferedImage flip(BufferedImage src) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(src, src.getWidth(), 0, -src.getWidth(), src.getHeight(), null);
        g.dispose();
        return out;
    }

    /**
     * Adds a one pixel outline around all opaque pixels.
     *
     * @param src     the source image
     * @param outline the outline colour
     * @return a new image that is 2 pixels larger in each direction
     */
    public static BufferedImage outline(BufferedImage src, Color outline) {
        int w = src.getWidth() + 2;
        int h = src.getHeight() + 2;
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int rgb = outline.getRGB();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int sx = x - 1;
                int sy = y - 1;
                int self = alphaAt(src, sx, sy);
                if (self > 0) {
                    out.setRGB(x, y, src.getRGB(sx, sy));
                } else if (alphaAt(src, sx - 1, sy) > 0 || alphaAt(src, sx + 1, sy) > 0
                        || alphaAt(src, sx, sy - 1) > 0 || alphaAt(src, sx, sy + 1) > 0) {
                    out.setRGB(x, y, rgb);
                }
            }
        }
        return out;
    }

    private static int alphaAt(BufferedImage img, int x, int y) {
        if (x < 0 || y < 0 || x >= img.getWidth() || y >= img.getHeight()) {
            return 0;
        }
        return (img.getRGB(x, y) >>> 24) & 0xFF;
    }

    /**
     * Makes a colour lighter.
     *
     * @param c      the colour
     * @param amount 0 to 1
     * @return the lighter colour
     */
    public static Color lighter(Color c, double amount) {
        return Theme.mix(c, Color.WHITE, amount);
    }

    /**
     * Makes a colour darker.
     *
     * @param c      the colour
     * @param amount 0 to 1
     * @return the darker colour
     */
    public static Color darker(Color c, double amount) {
        return Theme.mix(c, Color.BLACK, amount);
    }
}
