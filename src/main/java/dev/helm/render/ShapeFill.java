package dev.helm.render;

import java.util.List;

public final class ShapeFill {

    private static final int CORNERS_PER_FACE = 4;

    private ShapeFill() {
    }

    public static void corners(FillBatch batch, List<double[]> points) {
        if (points.size() == CORNERS_PER_FACE) {
            face(batch, points.get(0), points.get(1), points.get(2), points.get(3));
            return;
        }
        for (int i = 0; i + CORNERS_PER_FACE <= points.size(); i += CORNERS_PER_FACE) {
            face(batch, points.get(i), points.get(i + 1), points.get(i + 2),
                    points.get(i + 3));
        }
    }

    static void face(FillBatch batch, double[] a, double[] b, double[] c, double[] d) {
        batch.triangle(a, b, c);
        batch.triangle(a, c, d);
        batch.triangle(a, c, b);
        batch.triangle(a, d, c);
    }
}
