package httyd.fx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

class RasterTest {
    private static final int RED = 0xFFFF0000;
    private static final int BLUE = 0xFF0000FF;

    private static int count(Raster r) {
        int n = 0;
        for (int y = 0; y < r.getHeight(); y++) {
            for (int x = 0; x < r.getWidth(); x++) {
                if ((r.get(x, y) >>> 24) != 0) {
                    n++;
                }
            }
        }
        return n;
    }

    @Test
    void newRasterIsTransparent() {
        assertEquals(0, count(new Raster(5, 4)));
    }

    @Test
    void sizeMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new Raster(0, 3));
    }

    @Test
    void argbConvertsColours() {
        assertEquals(RED, Raster.argb(Color.RED));
        assertEquals(BLUE, Raster.argb(Color.BLUE));
        assertEquals(0xFF123456, Raster.argb(Theme.rgb(0x123456)));
    }

    @Test
    void fillRectFillsExactlyTheRectangle() {
        Raster r = new Raster(10, 10);
        r.setColor(Color.RED);
        r.fillRect(2, 3, 4, 2);
        assertEquals(8, count(r));
        assertEquals(RED, r.get(2, 3));
        assertEquals(RED, r.get(5, 4));
        assertEquals(0, r.get(6, 4));
        assertEquals(0, r.get(2, 5));
    }

    @Test
    void drawingOutsideThePictureIsIgnored() {
        Raster r = new Raster(4, 4);
        r.setColor(Color.RED);
        r.fillRect(-3, -3, 20, 20);
        r.drawLine(-5, -5, 30, 30);
        r.fillOval(-10, -10, 40, 40);
        r.set(99, 99, RED);
        assertEquals(16, count(r));
        assertEquals(0, r.get(-1, 0));
    }

    @Test
    void lineIncludesBothEndPoints() {
        Raster r = new Raster(8, 8);
        r.setColor(Color.BLUE);
        r.drawLine(1, 1, 6, 4);
        assertEquals(BLUE, r.get(1, 1));
        assertEquals(BLUE, r.get(6, 4));
        r.drawLine(0, 7, 7, 7);
        assertEquals(BLUE, r.get(3, 7));
    }

    @Test
    void ovalFillsTheMiddleButNotTheCorners() {
        Raster r = new Raster(20, 12);
        r.setColor(Color.RED);
        r.fillOval(0, 0, 20, 12);
        assertEquals(RED, r.get(10, 6));
        assertEquals(0, r.get(0, 0));
        assertEquals(0, r.get(19, 11));
        double expected = Math.PI * 10 * 6;
        assertTrue(Math.abs(count(r) - expected) < expected * 0.1, "area " + count(r));
    }

    @Test
    void arcFillsOnlyItsSlice() {
        Raster r = new Raster(20, 20);
        r.setColor(Color.RED);
        r.fillArc(0, 0, 20, 20, 180, 180);
        assertEquals(RED, r.get(10, 15), "below the centre is inside the lower half");
        assertEquals(0, r.get(10, 4), "above the centre is outside");
    }

    @Test
    void polygonFillsTheInsideOfATriangle() {
        Raster r = new Raster(20, 20);
        r.setColor(Color.RED);
        Poly triangle = new Poly();
        triangle.addPoint(2, 2);
        triangle.addPoint(18, 2);
        triangle.addPoint(2, 18);
        r.fillPolygon(triangle);
        assertEquals(RED, r.get(4, 4));
        assertEquals(0, r.get(16, 16));
        double expected = 16 * 16 / 2.0;
        assertTrue(Math.abs(count(r) - expected) < expected * 0.1, "area " + count(r));
    }

    @Test
    void gradientGoesFromTopToBottomColour() {
        Raster r = new Raster(2, 10);
        r.fillGradient(0, 0, 2, 10, 0, Color.BLACK, 10, Color.WHITE);
        int top = r.get(0, 0) & 0xFF;
        int bottom = r.get(0, 9) & 0xFF;
        assertTrue(top < 30, "top " + top);
        assertTrue(bottom > 225, "bottom " + bottom);
        assertEquals(0xFF, r.get(1, 5) >>> 24);
    }

    @Test
    void scaledRepeatsEveryPixel() {
        Raster r = new Raster(2, 1);
        r.set(0, 0, RED);
        r.set(1, 0, BLUE);
        Raster big = r.scaled(3);
        assertEquals(6, big.getWidth());
        assertEquals(3, big.getHeight());
        assertEquals(RED, big.get(2, 2));
        assertEquals(BLUE, big.get(3, 0));
        assertThrows(IllegalArgumentException.class, () -> r.scaled(0));
    }

    @Test
    void flippedMirrorsLeftToRight() {
        Raster r = new Raster(3, 1);
        r.set(0, 0, RED);
        Raster f = r.flipped();
        assertEquals(RED, f.get(2, 0));
        assertEquals(0, f.get(0, 0));
    }

    @Test
    void outlineAddsOnePixelAroundOpaquePixels() {
        Raster r = new Raster(1, 1);
        r.set(0, 0, RED);
        Raster o = r.outlined(Color.BLUE);
        assertEquals(3, o.getWidth());
        assertEquals(RED, o.get(1, 1));
        assertEquals(BLUE, o.get(0, 1));
        assertEquals(BLUE, o.get(1, 0));
        assertEquals(0, o.get(0, 0), "corners are not outlined");
        assertEquals(5, count(o));
    }

    @Test
    void resamplingAveragesWhenShrinking() {
        Raster r = new Raster(2, 2);
        r.set(0, 0, RED);
        r.set(1, 0, RED);
        Raster small = r.resampled(1, 1);
        int p = small.get(0, 0);
        assertEquals(128, p >>> 24, 1);
        assertEquals(0xFF, (p >> 16) & 0xFF, "colour is not darkened by the transparent pixels");
        assertEquals(0, p & 0xFF);
    }

    @Test
    void resamplingEnlargesWithNearestPixel() {
        Raster r = new Raster(1, 1);
        r.set(0, 0, BLUE);
        Raster big = r.resampled(4, 3);
        assertEquals(12, count(big));
        assertEquals(BLUE, big.get(3, 2));
    }

    @Test
    void placedOnKeepsPixelsAtTheOffset() {
        Raster r = new Raster(1, 1);
        r.set(0, 0, RED);
        Raster canvas = r.placedOn(5, 5, 3, 4);
        assertEquals(RED, canvas.get(3, 4));
        assertEquals(1, count(canvas));
    }

    @Test
    void toArgbReturnsACopy() {
        Raster r = new Raster(2, 2);
        int[] copy = r.toArgb();
        copy[0] = RED;
        assertNotEquals(RED, r.get(0, 0));
    }
}
