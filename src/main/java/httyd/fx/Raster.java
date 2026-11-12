package httyd.fx;

import javafx.scene.paint.Color;

/**
 * A small picture made of pixels that lives in memory, with the handful of drawing operations the
 * pixel-art sprites and backdrops need (rectangles, ovals, arcs, polygons, lines, gradients). It has
 * no anti-aliasing, which is exactly what pixel art wants, and no dependency on any window system, so
 * it can be tested without opening a window. {@link FxImages} turns it into a JavaFX image.
 *
 * <p>Pixels are stored as non-premultiplied ARGB integers.
 */
public final class Raster {
    private final int width;
    private final int height;
    private final int[] pixels;
    private int pen = 0xFF000000;

    /**
     * Creates a fully transparent picture.
     *
     * @param width  the width in pixels
     * @param height the height in pixels
     */
    public Raster(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("size must be positive");
        }
        this.width = width;
        this.height = height;
        this.pixels = new int[width * height];
    }

    /**
     * Wraps existing pixel data.
     *
     * @param width  the width
     * @param height the height
     * @param argb   {@code width * height} pixels, copied
     * @return the picture
     */
    public static Raster of(int width, int height, int[] argb) {
        Raster r = new Raster(width, height);
        System.arraycopy(argb, 0, r.pixels, 0, width * height);
        return r;
    }

    /** @return the width in pixels */
    public int getWidth() {
        return width;
    }

    /** @return the height in pixels */
    public int getHeight() {
        return height;
    }

    /** @return a copy of all pixels, row by row, as ARGB */
    public int[] toArgb() {
        return pixels.clone();
    }

    /**
     * Reads a pixel; positions outside the picture are transparent.
     *
     * @param x the column
     * @param y the row
     * @return the ARGB value
     */
    public int get(int x, int y) {
        return inside(x, y) ? pixels[y * width + x] : 0;
    }

    /**
     * Writes a pixel; positions outside the picture are ignored.
     *
     * @param x    the column
     * @param y    the row
     * @param argb the ARGB value
     */
    public void set(int x, int y, int argb) {
        if (inside(x, y)) {
            pixels[y * width + x] = argb;
        }
    }

    private boolean inside(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    /**
     * Converts a colour to the ARGB integer used by this class.
     *
     * @param c the colour
     * @return the ARGB value
     */
    public static int argb(Color c) {
        int a = (int) Math.round(c.getOpacity() * 255);
        int r = (int) Math.round(c.getRed() * 255);
        int g = (int) Math.round(c.getGreen() * 255);
        int b = (int) Math.round(c.getBlue() * 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Chooses the colour used by the next drawing operations.
     *
     * @param color the colour
     */
    public void setColor(Color color) {
        this.pen = argb(color);
    }

    /**
     * Fills a rectangle.
     *
     * @param x the left edge
     * @param y the top edge
     * @param w the width
     * @param h the height
     */
    public void fillRect(int x, int y, int w, int h) {
        for (int yy = Math.max(0, y); yy < Math.min(height, y + h); yy++) {
            for (int xx = Math.max(0, x); xx < Math.min(width, x + w); xx++) {
                pixels[yy * width + xx] = pen;
            }
        }
    }

    /**
     * Draws the outline of a rectangle that is {@code w + 1} by {@code h + 1} pixels, like AWT did.
     *
     * @param x the left edge
     * @param y the top edge
     * @param w the width
     * @param h the height
     */
    public void drawRect(int x, int y, int w, int h) {
        drawLine(x, y, x + w, y);
        drawLine(x, y + h, x + w, y + h);
        drawLine(x, y, x, y + h);
        drawLine(x + w, y, x + w, y + h);
    }

    /**
     * Draws a one pixel wide line (Bresenham), end points included.
     *
     * @param x1 start x
     * @param y1 start y
     * @param x2 end x
     * @param y2 end y
     */
    public void drawLine(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x2 - x1);
        int dy = -Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx + dy;
        int x = x1;
        int y = y1;
        while (true) {
            set(x, y, pen);
            if (x == x2 && y == y2) {
                return;
            }
            int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                x += sx;
            }
            if (e2 <= dx) {
                err += dx;
                y += sy;
            }
        }
    }

    /**
     * Fills the ellipse that fits in a rectangle (a pixel is filled when its centre is inside).
     *
     * @param x the left edge
     * @param y the top edge
     * @param w the width
     * @param h the height
     */
    public void fillOval(int x, int y, int w, int h) {
        fillArc(x, y, w, h, 0, 360);
    }

    /**
     * Fills a pie slice of the ellipse that fits in a rectangle.
     *
     * @param x     the left edge
     * @param y     the top edge
     * @param w     the width
     * @param h     the height
     * @param start the start angle in degrees, 0 = three o'clock, counter-clockwise
     * @param sweep how many degrees the slice covers (360 or more fills the whole ellipse)
     */
    public void fillArc(int x, int y, int w, int h, int start, int sweep) {
        if (w <= 0 || h <= 0) {
            return;
        }
        double cx = x + w / 2.0;
        double cy = y + h / 2.0;
        double rx = w / 2.0;
        double ry = h / 2.0;
        for (int yy = Math.max(0, y); yy < Math.min(height, y + h); yy++) {
            for (int xx = Math.max(0, x); xx < Math.min(width, x + w); xx++) {
                double nx = (xx + 0.5 - cx) / rx;
                double ny = (yy + 0.5 - cy) / ry;
                if (nx * nx + ny * ny > 1.0) {
                    continue;
                }
                if (sweep < 360) {
                    double angle = Math.toDegrees(Math.atan2(-ny, nx));
                    double rel = ((angle - start) % 360 + 360) % 360;
                    if (rel > sweep) {
                        continue;
                    }
                }
                pixels[yy * width + xx] = pen;
            }
        }
    }

    /**
     * Fills a polygon (even-odd rule, a pixel is filled when its centre is inside).
     *
     * @param poly the polygon
     */
    public void fillPolygon(Poly poly) {
        int n = poly.size();
        if (n < 3) {
            return;
        }
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            minY = Math.min(minY, poly.y(i));
            maxY = Math.max(maxY, poly.y(i));
        }
        for (int yy = Math.max(0, minY); yy <= Math.min(height - 1, maxY); yy++) {
            double py = yy + 0.5;
            double[] cross = new double[n];
            int count = 0;
            for (int i = 0; i < n; i++) {
                int j = (i + 1) % n;
                double y1 = poly.y(i);
                double y2 = poly.y(j);
                if ((y1 <= py && y2 > py) || (y2 <= py && y1 > py)) {
                    cross[count++] = poly.x(i) + (py - y1) / (y2 - y1) * (poly.x(j) - poly.x(i));
                }
            }
            java.util.Arrays.sort(cross, 0, count);
            for (int k = 0; k + 1 < count; k += 2) {
                int from = (int) Math.ceil(cross[k] - 0.5);
                int to = (int) Math.ceil(cross[k + 1] - 0.5) - 1;
                for (int xx = Math.max(0, from); xx <= Math.min(width - 1, to); xx++) {
                    pixels[yy * width + xx] = pen;
                }
            }
        }
    }

    /**
     * Fills a polygon given as arrays of coordinates.
     *
     * @param xs the x coordinates
     * @param ys the y coordinates
     * @param n  how many points to use
     */
    public void fillPolygon(int[] xs, int[] ys, int n) {
        fillPolygon(new Poly(xs, ys, n));
    }

    /**
     * Fills a rectangle with a vertical colour gradient. Above {@code y1} the colour is {@code top},
     * below {@code y2} it is {@code bottom}, in between it is blended.
     *
     * @param x      the left edge
     * @param y      the top edge
     * @param w      the width
     * @param h      the height
     * @param y1     where the gradient starts
     * @param top    colour at {@code y1}
     * @param y2     where the gradient ends
     * @param bottom colour at {@code y2}
     */
    public void fillGradient(int x, int y, int w, int h, int y1, Color top, int y2, Color bottom) {
        for (int yy = Math.max(0, y); yy < Math.min(height, y + h); yy++) {
            double t = y2 == y1 ? 0 : (yy + 0.5 - y1) / (y2 - y1);
            int argb = argb(top.interpolate(bottom, Math.max(0, Math.min(1, t))));
            for (int xx = Math.max(0, x); xx < Math.min(width, x + w); xx++) {
                pixels[yy * width + xx] = argb;
            }
        }
    }

    /**
     * Enlarges the picture with nearest-neighbour sampling so pixels stay sharp.
     *
     * @param factor integer scale factor
     * @return the enlarged copy
     */
    public Raster scaled(int factor) {
        if (factor < 1) {
            throw new IllegalArgumentException("factor must be at least 1");
        }
        Raster out = new Raster(width * factor, height * factor);
        for (int y = 0; y < out.height; y++) {
            for (int x = 0; x < out.width; x++) {
                out.pixels[y * out.width + x] = pixels[(y / factor) * width + (x / factor)];
            }
        }
        return out;
    }

    /** @return a copy mirrored left to right */
    public Raster flipped() {
        Raster out = new Raster(width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                out.pixels[y * width + x] = pixels[y * width + (width - 1 - x)];
            }
        }
        return out;
    }

    /**
     * Adds a one pixel outline around all opaque pixels.
     *
     * @param outline the outline colour
     * @return a new picture that is 2 pixels larger in each direction
     */
    public Raster outlined(Color outline) {
        Raster out = new Raster(width + 2, height + 2);
        int rgb = argb(outline);
        for (int y = 0; y < out.height; y++) {
            for (int x = 0; x < out.width; x++) {
                int sx = x - 1;
                int sy = y - 1;
                if (alpha(sx, sy) > 0) {
                    out.pixels[y * out.width + x] = get(sx, sy);
                } else if (alpha(sx - 1, sy) > 0 || alpha(sx + 1, sy) > 0
                        || alpha(sx, sy - 1) > 0 || alpha(sx, sy + 1) > 0) {
                    out.pixels[y * out.width + x] = rgb;
                }
            }
        }
        return out;
    }

    private int alpha(int x, int y) {
        return (get(x, y) >>> 24) & 0xFF;
    }

    /**
     * Resizes the picture to any size. Shrinking averages the covered pixels (so photos and smooth
     * artwork stay clean), enlarging copies the nearest pixel.
     *
     * @param newWidth  the new width
     * @param newHeight the new height
     * @return the resized copy
     */
    public Raster resampled(int newWidth, int newHeight) {
        Raster out = new Raster(newWidth, newHeight);
        for (int y = 0; y < newHeight; y++) {
            int y0 = (int) Math.floor((double) y * height / newHeight);
            int y1 = Math.max(y0 + 1, (int) Math.ceil((double) (y + 1) * height / newHeight));
            for (int x = 0; x < newWidth; x++) {
                int x0 = (int) Math.floor((double) x * width / newWidth);
                int x1 = Math.max(x0 + 1, (int) Math.ceil((double) (x + 1) * width / newWidth));
                long a = 0;
                long r = 0;
                long g = 0;
                long b = 0;
                int count = 0;
                for (int yy = y0; yy < Math.min(height, y1); yy++) {
                    for (int xx = x0; xx < Math.min(width, x1); xx++) {
                        int p = pixels[yy * width + xx];
                        int pa = (p >>> 24) & 0xFF;
                        a += pa;
                        r += (long) ((p >> 16) & 0xFF) * pa;
                        g += (long) ((p >> 8) & 0xFF) * pa;
                        b += (long) (p & 0xFF) * pa;
                        count++;
                    }
                }
                if (count == 0 || a == 0) {
                    continue;
                }
                int oa = (int) Math.round((double) a / count);
                int or = (int) Math.round((double) r / a);
                int og = (int) Math.round((double) g / a);
                int ob = (int) Math.round((double) b / a);
                out.pixels[y * newWidth + x] = (oa << 24) | (or << 16) | (og << 8) | ob;
            }
        }
        return out;
    }

    /**
     * Puts this picture on a bigger transparent canvas.
     *
     * @param canvasWidth  the canvas width
     * @param canvasHeight the canvas height
     * @param x            where the left edge of this picture goes
     * @param y            where the top edge of this picture goes
     * @return the canvas
     */
    public Raster placedOn(int canvasWidth, int canvasHeight, int x, int y) {
        Raster out = new Raster(canvasWidth, canvasHeight);
        for (int yy = 0; yy < height; yy++) {
            for (int xx = 0; xx < width; xx++) {
                out.set(x + xx, y + yy, pixels[yy * width + xx]);
            }
        }
        return out;
    }
}
