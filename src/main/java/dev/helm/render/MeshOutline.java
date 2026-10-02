package dev.helm.render;

import java.util.List;

import net.minecraft.client.resources.model.geometry.BakedQuad;

public final class MeshOutline {

    private static final int CORNERS_PER_FACE = BakedQuad.VERTEX_COUNT;

    private MeshOutline() {
    }

    public static void draw(LineBatch batch, List<double[]> corners) {
        for (int i = 0; i + CORNERS_PER_FACE <= corners.size(); i += CORNERS_PER_FACE) {
            for (int edge = 0; edge < CORNERS_PER_FACE; edge++) {
                double[] from = corners.get(i + edge);
                double[] to = corners.get(i + (edge + 1) % CORNERS_PER_FACE);
                batch.segment(from[0], from[1], from[2], to[0], to[1], to[2]);
            }
        }
    }
}