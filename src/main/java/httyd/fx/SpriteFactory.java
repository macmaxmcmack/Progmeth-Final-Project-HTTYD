package httyd.fx;

import httyd.model.dragon.DragonClass;
import httyd.model.dragon.DragonSpecies;
import httyd.model.dragon.Stat;
import static httyd.fx.Theme.darker;
import static httyd.fx.Theme.lighter;
import static httyd.fx.Theme.rgb;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Makes the pixel-art pictures of the dragons. Every species gets its own look from a small
 * recipe (colour, size, wings, horns, markings) that depends on its class and id, so the 28
 * dragons are different without any image files. These are placeholders: to use real pictures
 * just put {@code /sprites/<species id>.png} in the resources and it will be loaded instead.
 */
public final class SpriteFactory {
    private static final int W = 48;
    private static final int H = 36;
    private static final Map<String, Image> CACHE = new HashMap<>();

    private SpriteFactory() {
    }

    /**
     * Returns the picture of a dragon facing left or right.
     *
     * @param species   the species
     * @param faceRight true if the dragon looks to the right
     * @param scale     pixel scale factor
     * @return the picture
     */
    public static synchronized Image dragon(DragonSpecies species, boolean faceRight, int scale) {
        String key = species.getId() + faceRight + scale;
        return CACHE.computeIfAbsent(key, k -> {
            Raster file = loadFile(species.getId());
            if (file != null) {
                return FxImages.toImage(fitFile(file, faceRight, scale));
            }
            Raster base = draw(species);
            if (!faceRight) {
                base = base.flipped();
            }
            return FxImages.toImage(base.scaled(scale));
        });
    }

    private static Color hsb(double hue01, double saturation, double brightness) {
        return Color.hsb(hue01 * 360.0, saturation, brightness);
    }

    private static Raster loadFile(String id) {
        try (InputStream in = SpriteFactory.class.getResourceAsStream("/sprites/" + id + ".png")) {
            return in == null ? null : FxImages.toRaster(new Image(in));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Fits a picture from a file into the same box the generated sprites use (48 x 36 pixels times
     * {@code scale}): big pictures are shrunk smoothly, and the dragon stands on the bottom edge.
     */
    private static Raster fitFile(Raster file, boolean faceRight, int scale) {
        Raster src = faceRight ? file : file.flipped();
        int boxW = W * scale;
        int boxH = (H - 4) * scale;
        double k = Math.min((double) boxW / src.getWidth(), (double) boxH / src.getHeight());
        int w = Math.max(1, (int) Math.round(src.getWidth() * k));
        int h = Math.max(1, (int) Math.round(src.getHeight() * k));
        Raster fitted = src.resampled(w, h);
        return fitted.placedOn(boxW, H * scale, (boxW - w) / 2, (H - 2) * scale - h);
    }

    private static Raster draw(DragonSpecies s) {
        Random r = new Random(s.getId().hashCode() * 31L + 7);
        DragonClass cls = s.getDragonClass();
        Color classColor = rgb(cls.getColorRgb());
        double classHue = classColor.getHue() / 360.0;
        double classSat = classColor.getSaturation();
        double classBri = classColor.getBrightness();
        double hue = (classHue + (r.nextFloat() - 0.5) * 0.07 + 1.0) % 1.0;
        Color body = hsb(hue, Math.min(1.0, classSat * 0.85), Math.max(0.45, classBri * 0.8));
        Color bodyDark = darker(body, 0.28);
        Color belly = lighter(hsb(hue, 0.25, 0.9), 0.2);
        Color wing = darker(hsb((hue + 0.06) % 1.0, Math.min(1.0, classSat), Math.max(0.5, classBri * 0.75)), 0.1);
        Color eye = s.getTier().ordinal() == 3 ? Theme.GOLD : (cls == DragonClass.MYSTERY ? rgb(0xFF5FD0) : Color.WHITE);

        double size = 0.8 + (s.getBase(Stat.HP) - 60) / 39.0 * 0.55;
        int bw = (int) Math.round(9 * size);
        int bh = (int) Math.round(5.5 * size);
        int cx = 22;
        int cy = 21;
        int neck = 3 + r.nextInt(4);
        int tail = 9 + r.nextInt(6);
        int wingStyle = r.nextInt(3);
        int horns = 1 + r.nextInt(3);

        Raster g = new Raster(W, H);

        // tail
        g.setColor(body);
        Poly tailShape = new Poly();
        tailShape.addPoint(cx - bw + 2, cy - 2);
        tailShape.addPoint(cx - bw - tail, cy + 3 + r.nextInt(3));
        tailShape.addPoint(cx - bw + 1, cy + 4);
        g.fillPolygon(tailShape);
        g.setColor(bodyDark);
        g.fillRect(cx - bw - tail, cy + 3, 2, 2);

        // far wing
        g.setColor(darker(wing, 0.2));
        wingShape(g, cx - 2, cy - bh + 1, wingStyle, size, -3);

        // legs
        g.setColor(bodyDark);
        g.fillRect(cx - bw / 2, cy + bh - 2, 3, 5);
        g.fillRect(cx + bw / 2 - 2, cy + bh - 2, 3, 5);
        g.setColor(darker(body, 0.4));
        g.fillRect(cx - bw / 2, cy + bh + 2, 4, 1);
        g.fillRect(cx + bw / 2 - 2, cy + bh + 2, 4, 1);

        // body and belly
        g.setColor(body);
        g.fillOval(cx - bw, cy - bh, bw * 2, bh * 2);
        g.setColor(belly);
        g.fillArc(cx - bw + 2, cy - bh + 3, bw * 2 - 4, bh * 2 - 3, 200, 140);

        // neck and head
        int hx = cx + bw + neck - 2;
        int hy = cy - bh - neck / 2;
        g.setColor(body);
        Poly neckShape = new Poly();
        neckShape.addPoint(cx + bw - 5, cy - bh + 2);
        neckShape.addPoint(hx - 2, hy - 1);
        neckShape.addPoint(hx + 2, hy + 6);
        neckShape.addPoint(cx + bw - 1, cy + 1);
        g.fillPolygon(neckShape);
        g.fillOval(hx - 4, hy - 4, 10, 9);
        g.fillRect(hx + 3, hy - 1, 6, 4);
        g.setColor(belly);
        g.fillRect(hx + 3, hy + 2, 6, 1);

        // horns and spikes
        g.setColor(lighter(bodyDark, 0.6));
        for (int i = 0; i < horns; i++) {
            Poly horn = new Poly();
            horn.addPoint(hx - 3 + i * 2, hy - 3);
            horn.addPoint(hx - 6 + i * 2, hy - 7 - i);
            horn.addPoint(hx - 1 + i * 2, hy - 4);
            g.fillPolygon(horn);
        }
        g.setColor(bodyDark);
        for (int x = cx - bw + 3; x < cx + bw - 4; x += 4) {
            Poly spike = new Poly();
            spike.addPoint(x, cy - bh + 1);
            spike.addPoint(x + 1, cy - bh - 3);
            spike.addPoint(x + 3, cy - bh + 1);
            g.fillPolygon(spike);
        }

        // near wing
        g.setColor(wing);
        wingShape(g, cx + 1, cy - bh + 2, wingStyle, size, 0);

        motif(g, cls, r, cx, cy, bw, bh, body, bodyDark);

        // eye
        g.setColor(eye);
        g.fillRect(hx + 2, hy - 2, 2, 2);
        g.setColor(Color.BLACK);
        g.fillRect(hx + 3, hy - 2, 1, 1);

        return g.outlined(Theme.INK);
    }

    private static void wingShape(Raster g, int x, int y, int style, double size, int shift) {
        int span = (int) Math.round(13 * size);
        Poly wing = new Poly();
        wing.addPoint(x, y);
        switch (style) {
            case 0 -> {
                wing.addPoint(x - span, y - span + shift);
                wing.addPoint(x - span / 3, y - 1);
                wing.addPoint(x - span / 2 - 2, y + 4);
                wing.addPoint(x + 2, y + 3);
            }
            case 1 -> {
                wing.addPoint(x - span + 2, y - span / 2 + shift);
                wing.addPoint(x - span - 2, y + 1);
                wing.addPoint(x - span / 2, y + 2);
                wing.addPoint(x - 4, y + 6);
                wing.addPoint(x + 3, y + 4);
            }
            default -> {
                wing.addPoint(x - 3, y - span + shift);
                wing.addPoint(x - span, y - span / 2 + shift);
                wing.addPoint(x - span / 2, y + 1);
                wing.addPoint(x + 2, y + 3);
            }
        }
        g.fillPolygon(wing);
    }

    private static void motif(Raster g, DragonClass cls, Random r, int cx, int cy, int bw, int bh,
                              Color body, Color dark) {
        switch (cls) {
            case STRIKE -> {
                g.setColor(Theme.GOLD);
                for (int i = 0; i < 3; i++) {
                    int x = cx - bw / 2 + i * 5;
                    g.drawLine(x, cy - bh + 3, x + 2, cy - 1);
                    g.drawLine(x + 2, cy - 1, x, cy + 1);
                }
            }
            case STOKER -> {
                g.setColor(rgb(0xFF8A2A));
                g.fillRect(cx + bw + 5, cy - bh - 3, 3, 2);
                g.setColor(rgb(0xFFD24A));
                g.fillRect(cx + bw + 8, cy - bh - 3, 2, 2);
                g.setColor(rgb(0xFF6A2A));
                g.fillRect(cx - bw - 8, cy + 3, 3, 3);
            }
            case BOULDER -> {
                g.setColor(darker(body, 0.35));
                for (int i = 0; i < 4; i++) {
                    g.fillRect(cx - bw + 4 + i * 4, cy - 2 + (i % 2) * 3, 3, 3);
                }
            }
            case TRACKER -> {
                g.setColor(dark);
                for (int i = 0; i < 4; i++) {
                    g.drawLine(cx - bw + 4 + i * 4, cy - bh + 2, cx - bw + 2 + i * 4, cy + 1);
                }
            }
            case SHARP -> {
                g.setColor(rgb(0xE8EDF2));
                g.drawLine(cx - bw + 2, cy - 1, cx + bw - 4, cy - 1);
                g.drawLine(cx - bw + 4, cy + 1, cx + bw - 6, cy + 1);
            }
            case TIDAL -> {
                g.setColor(rgb(0x9FD8F5));
                for (int i = 0; i < 3; i++) {
                    g.fillRect(cx - bw + 5 + i * 5, cy + bh - 5, 3, 2);
                }
                g.fillRect(cx - bw - 6, cy + 5, 3, 3);
            }
            case MYSTERY -> {
                g.setColor(rgb(0xD9A5FF));
                for (int i = 0; i < 5; i++) {
                    g.fillRect(cx - bw + 3 + r.nextInt(bw * 2 - 6), cy - bh + 3 + r.nextInt(bh * 2 - 6), 2, 2);
                }
            }
            default -> { }
        }
    }

    /**
     * Returns the picture of a dragon egg.
     *
     * @param scale pixel scale factor
     * @param crack how cracked it is, 0 (whole) to 3 (about to hatch)
     * @return the picture
     */
    public static synchronized Image egg(int scale, int crack) {
        String key = "egg" + scale + crack;
        return CACHE.computeIfAbsent(key, k -> {
            Raster g = new Raster(22, 28);
            g.setColor(rgb(0xE9DEC0));
            g.fillOval(2, 1, 17, 24);
            g.setColor(rgb(0x7FB6A0));
            g.fillArc(2, 1, 17, 24, 200, 140);
            g.setColor(rgb(0xD7C79B));
            g.fillRect(5, 8, 3, 3);
            g.fillRect(12, 14, 3, 3);
            g.fillRect(7, 18, 3, 3);
            g.setColor(Theme.INK);
            if (crack >= 1) {
                g.drawLine(10, 2, 12, 7);
                g.drawLine(12, 7, 9, 10);
            }
            if (crack >= 2) {
                g.drawLine(9, 10, 13, 14);
                g.drawLine(13, 14, 10, 18);
            }
            if (crack >= 3) {
                g.drawLine(6, 8, 9, 10);
                g.drawLine(14, 10, 17, 14);
                g.drawLine(10, 18, 14, 22);
            }
            return FxImages.toImage(g.outlined(Theme.INK).scaled(scale));
        });
    }
}
