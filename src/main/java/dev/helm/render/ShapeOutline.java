package dev.helm.render;

import java.util.List;

public final class ShapeOutline {

    private static final int[] BOX_EDGES = {
            0, 1, 1, 2, 2, 3, 3, 0,
            4, 5, 5, 6, 6, 7, 7, 4,
            0, 4, 1, 5, 2, 6, 3, 7,
    };

    private ShapeOutline() {
    }

    public static void draw(LineBatch batch, List<double[]> corners) {
        if (corners.size() == 8) {
            box(batch, corners);
            return;
        }
        for (int i = 0; i + 4 <= corners.size(); i += 4) {
            for (int edge = 0; edge < 4; edge++) {
                double[] from = corners.get(i + edge);
                double[] to = corners.get(i + (edge + 1) % 4);
                batch.segment(from[0], from[1], from[2], to[0], to[1], to[2]);
            }
        }
    }

    private static void box(LineBatch batch, List<double[]> corners) {
        for (int i = 0; i < BOX_EDGES.length; i += 2) {
            double[] from = corners.get(BOX_EDGES[i]);
            double[] to = corners.get(BOX_EDGES[i + 1]);
            batch.segment(from[0], from[1], from[2], to[0], to[1], to[2]);
        }
    }
}