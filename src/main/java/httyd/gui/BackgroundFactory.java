package httyd.gui;

import httyd.model.field.BattlefieldType;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Draws the pixel-art backdrops of the seven battlefields. They are drawn small (240x135) and
 * stretched without blur, which gives the old-console look.
 */
public final class BackgroundFactory {
    /** Width of the small image before stretching. */
    public static final int BASE_W = 240;
    /** Height of the small image before stretching. */
    public static final int BASE_H = 135;
    private static final Map<BattlefieldType, BufferedImage> CACHE = new HashMap<>();

    private BackgroundFactory() {
    }

    /**
     * Returns the small backdrop of a battlefield.
     *
     * @param type the battlefield
     * @return an image of {@link #BASE_W} x {@link #BASE_H}
     */
    public static synchronized BufferedImage backdrop(BattlefieldType type) {
        return CACHE.computeIfAbsent(type, BackgroundFactory::paint);
    }

    /**
     * Draws a backdrop stretched to a size.
     *
     * @param g    the graphics
     * @param type the battlefield
     * @param w    target width
     * @param h    target height
     */
    public static void draw(Graphics2D g, BattlefieldType type, int w, int h) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(backdrop(type), 0, 0, w, h, null);
    }

    private static BufferedImage paint(BattlefieldType type) {
        BufferedImage img = new BufferedImage(BASE_W, BASE_H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Random r = new Random(type.ordinal() * 99L + 5);
        switch (type) {
            case PLAINS -> plains(g, r);
            case THUNDERSTORM_PEAK -> thunder(g, r);
            case VOLCANO_CRATER -> volcano(g, r);
            case DEEP_CAVE -> cave(g, r);
            case DEEP_FOREST -> forest(g, r);
            case TOXIC_RUINS -> ruins(g, r);
            case OPEN_OCEAN -> ocean(g, r);
            default -> { }
        }
        g.dispose();
        return img;
    }

    private static void sky(Graphics2D g, Color top, Color bottom, int horizon) {
        g.setPaint(new GradientPaint(0, 0, top, 0, horizon, bottom));
        g.fillRect(0, 0, BASE_W, horizon);
    }

    private static void ground(Graphics2D g, Color top, Color bottom, int horizon) {
        g.setPaint(new GradientPaint(0, horizon, top, 0, BASE_H, bottom));
        g.fillRect(0, horizon, BASE_W, BASE_H - horizon);
        g.setColor(Pixels.darker(top, 0.25));
        g.fillRect(0, horizon, BASE_W, 1);
    }

    private static void platforms(Graphics2D g, Color top) {
        g.setColor(Pixels.darker(top, 0.35));
        g.fillOval(140, 62, 90, 20);
        g.fillOval(10, 100, 120, 28);
        g.setColor(top);
        g.fillOval(143, 62, 84, 16);
        g.fillOval(14, 100, 112, 23);
    }

    private static void cloud(Graphics2D g, int x, int y, Color c) {
        g.setColor(c);
        g.fillRect(x, y, 26, 5);
        g.fillRect(x + 4, y - 3, 14, 4);
        g.fillRect(x + 12, y + 4, 16, 3);
    }

    private static void plains(Graphics2D g, Random r) {
        sky(g, new Color(0x6EC1F0), new Color(0xD8F0FF), 70);
        cloud(g, 30, 18, Color.WHITE);
        cloud(g, 150, 30, Color.WHITE);
        g.setColor(new Color(0x8FD07A));
        g.fillOval(-30, 52, 150, 40);
        g.fillOval(100, 48, 180, 50);
        ground(g, new Color(0x6BBF5A), new Color(0x3F8E3C), 68);
        for (int i = 0; i < 40; i++) {
            g.setColor(new Color(0x4F9E49));
            g.fillRect(r.nextInt(BASE_W), 72 + r.nextInt(60), 2, 2);
        }
        platforms(g, new Color(0x93D86A));
    }

    private static void thunder(Graphics2D g, Random r) {
        sky(g, new Color(0x2A2F55), new Color(0x6F76A8), 80);
        cloud(g, 10, 20, new Color(0x4C5380));
        cloud(g, 90, 12, new Color(0x3C4270));
        cloud(g, 170, 26, new Color(0x4C5380));
        Polygon peak = new Polygon(new int[]{-10, 60, 120, 190, 250}, new int[]{82, 40, 64, 30, 82}, 5);
        g.setColor(new Color(0x4A4F66));
        g.fillPolygon(peak);
        g.setColor(new Color(0x6A708C));
        g.fillPolygon(new int[]{60, 72, 52}, new int[]{40, 56, 56}, 3);
        g.setColor(Theme.GOLD);
        for (int i = 0; i < 3; i++) {
            int x = 40 + i * 70 + r.nextInt(20);
            g.fillPolygon(new int[]{x, x + 6, x + 2, x + 8, x - 2, x + 2, x - 4},
                    new int[]{4, 4, 20, 20, 44, 26, 26}, 7);
        }
        ground(g, new Color(0x5A5F78), new Color(0x2F3347), 80);
        platforms(g, new Color(0x7A809C));
    }

    private static void volcano(Graphics2D g, Random r) {
        sky(g, new Color(0x5B1D1D), new Color(0xE8693A), 78);
        g.setColor(new Color(0x3A1D18));
        g.fillPolygon(new int[]{20, 80, 160, 220}, new int[]{86, 30, 30, 86}, 4);
        g.setColor(new Color(0xFF9A2E));
        g.fillRect(84, 28, 72, 5);
        g.setColor(new Color(0xFFD24A));
        for (int i = 0; i < 25; i++) {
            g.fillRect(70 + r.nextInt(100), 6 + r.nextInt(24), 2, 3);
        }
        ground(g, new Color(0x7A3426), new Color(0x3D1A14), 78);
        g.setColor(new Color(0xFF7A2A));
        for (int i = 0; i < 6; i++) {
            g.fillRect(r.nextInt(BASE_W), 90 + r.nextInt(40), 14 + r.nextInt(14), 2);
        }
        platforms(g, new Color(0x9A5A3A));
    }

    private static void cave(Graphics2D g, Random r) {
        g.setPaint(new GradientPaint(0, 0, new Color(0x1E1B26), 0, BASE_H, new Color(0x4A4152)));
        g.fillRect(0, 0, BASE_W, BASE_H);
        g.setColor(new Color(0x38323F));
        for (int i = 0; i < 12; i++) {
            int x = i * 22 + r.nextInt(10);
            int len = 14 + r.nextInt(26);
            g.fillPolygon(new int[]{x, x + 10, x + 5}, new int[]{0, 0, len}, 3);
        }
        g.setColor(new Color(0x6FC3FF));
        for (int i = 0; i < 9; i++) {
            g.fillRect(r.nextInt(BASE_W), 40 + r.nextInt(40), 2, 2);
        }
        ground(g, new Color(0x6B6275), new Color(0x2E2A36), 82);
        platforms(g, new Color(0x8C8497));
    }

    private static void forest(Graphics2D g, Random r) {
        sky(g, new Color(0x2E6B4A), new Color(0xA7D98A), 74);
        for (int i = 0; i < 9; i++) {
            int x = i * 28 - 6 + r.nextInt(8);
            g.setColor(new Color(0x5B3B22));
            g.fillRect(x + 8, 30, 8, 60);
            g.setColor(new Color(0x2F8A3F));
            g.fillOval(x - 4, 6 + r.nextInt(12), 32, 34);
            g.setColor(new Color(0x3FA64F));
            g.fillOval(x, 10, 20, 20);
        }
        ground(g, new Color(0x4C8C3A), new Color(0x264F22), 76);
        platforms(g, new Color(0x6FB052));
    }

    private static void ruins(Graphics2D g, Random r) {
        sky(g, new Color(0x3A2A4F), new Color(0x9AD06A), 80);
        g.setColor(new Color(0x4A3A5E));
        for (int i = 0; i < 6; i++) {
            int x = i * 40 + r.nextInt(12);
            int h = 30 + r.nextInt(30);
            g.fillRect(x, 80 - h, 14, h);
            g.fillRect(x + 4, 80 - h - 6, 6, 6);
        }
        g.setColor(new Color(0xB8E986));
        for (int i = 0; i < 5; i++) {
            g.fillRect(r.nextInt(BASE_W - 40), 60 + r.nextInt(24), 40, 3);
        }
        ground(g, new Color(0x586B3E), new Color(0x2A3320), 80);
        platforms(g, new Color(0x7B9158));
    }

    private static void ocean(Graphics2D g, Random r) {
        sky(g, new Color(0x4FA7E8), new Color(0xCDEBFF), 58);
        cloud(g, 40, 16, Color.WHITE);
        cloud(g, 160, 26, Color.WHITE);
        g.setPaint(new GradientPaint(0, 58, new Color(0x2C7FCB), 0, BASE_H, new Color(0x123F7A)));
        g.fillRect(0, 58, BASE_W, BASE_H - 58);
        g.setColor(new Color(0xCDEBFF));
        for (int i = 0; i < 40; i++) {
            g.fillRect(r.nextInt(BASE_W), 62 + r.nextInt(70), 6 + r.nextInt(6), 1);
        }
        g.setColor(new Color(0x8D7A5A));
        g.fillOval(140, 64, 90, 18);
        g.fillOval(10, 102, 120, 26);
        g.setColor(new Color(0xC8B27E));
        g.fillOval(144, 64, 82, 14);
        g.fillOval(14, 102, 112, 21);
    }
}
