package httyd.fx;

import java.util.ArrayList;
import java.util.List;

/**
 * A polygon made of integer points, filled by {@link Raster#fillPolygon(Poly)}.
 */
public final class Poly {
    private final List<int[]> points = new ArrayList<>();

    /** Creates an empty polygon; add corners with {@link #addPoint(int, int)}. */
    public Poly() {
    }

    /**
     * Creates a polygon from arrays of coordinates.
     *
     * @param xs the x coordinates
     * @param ys the y coordinates
     * @param n  how many points to use
     */
    public Poly(int[] xs, int[] ys, int n) {
        for (int i = 0; i < n; i++) {
            addPoint(xs[i], ys[i]);
        }
    }

    /**
     * Adds a corner.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public void addPoint(int x, int y) {
        points.add(new int[] {x, y});
    }

    /** @return the number of corners */
    public int size() {
        return points.size();
    }

    /**
     * @param i the index of the corner
     * @return its x coordinate
     */
    public int x(int i) {
        return points.get(i)[0];
    }

    /**
     * @param i the index of the corner
     * @return its y coordinate
     */
    public int y(int i) {
        return points.get(i)[1];
    }
}
