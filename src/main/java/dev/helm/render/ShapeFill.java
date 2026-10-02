package dev.helm.render;

import java.util.List;

public final class ShapeFill {

    private static final int[][] FACES = {
            {0, 1, 3, 2}, {4, 5, 7, 6}, {0, 1, 5, 4},
            {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 3, 7, 5},
    };

    private ShapeFill() {
    }

    public static void corners(FillBatch batch, List<double[]> points) {
        if (points.size() == 4) {
            face(batch, points.get(0), points.get(1), points.get(2), points.get(3));
            return;
        }
        if (points.size() == 8) {
            box(batch, points);
        }
    }

    private static void box(FillBatch batch, List<double[]> points) {
        for (int[] face : FACES) {
            face(batch, points.get(face[0]), points.get(face[1]), points.get(face[2]),
                    points.get(face[3]));
        }
    }

    static void face(FillBatch batch, double[] a, double[] b, double[] c, double[] d) {
        batch.triangle(a, b, c);
        batch.triangle(a, c, d);
        batch.triangle(a, c, b);
        batch.triangle(a, d, c);
    }
}