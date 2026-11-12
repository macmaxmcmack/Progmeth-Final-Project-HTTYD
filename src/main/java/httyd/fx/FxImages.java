package httyd.fx;

import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

/**
 * Converts between {@link Raster} (our pixel pictures) and JavaFX {@link Image}s.
 */
public final class FxImages {
    private FxImages() {
    }

    /**
     * Makes a JavaFX image from a pixel picture.
     *
     * @param raster the picture
     * @return the image
     */
    public static WritableImage toImage(Raster raster) {
        WritableImage image = new WritableImage(raster.getWidth(), raster.getHeight());
        image.getPixelWriter().setPixels(0, 0, raster.getWidth(), raster.getHeight(),
                PixelFormat.getIntArgbInstance(), raster.toArgb(), 0, raster.getWidth());
        return image;
    }

    /**
     * Copies the pixels of a loaded JavaFX image.
     *
     * @param image the image, which must be fully loaded
     * @return the pixel picture, or {@code null} if the image failed to load
     */
    public static Raster toRaster(Image image) {
        if (image == null || image.isError() || image.getWidth() < 1 || image.getHeight() < 1) {
            return null;
        }
        int w = (int) image.getWidth();
        int h = (int) image.getHeight();
        PixelReader reader = image.getPixelReader();
        if (reader == null) {
            return null;
        }
        int[] argb = new int[w * h];
        reader.getPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), argb, 0, w);
        return Raster.of(w, h, argb);
    }
}
