package dev.helm.render;

import java.util.List;

import net.minecraft.world.phys.AABB;

public final class ShapeOutline {

    private static final AABB NOWHERE = new AABB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);

    private ShapeOutline() {
    }

    public static void draw(LineBatch batch, List<double[]> corners) {
        if (corners.size() == 4) {
            each(batch, corners);
            return;
        }
        batch.box(bounds(corners));
    }

    public static void each(LineBatch batch, List<double[]> corners) {
        for (int start = 0; start + 4 <= corners.size(); start += 4) {
            for (int edge = 0; edge < 4; edge++) {
                double[] from = corners.get(start + edge);
                double[] to = corners.get(start + (edge + 1) % 4);
                batch.segment(from[0], from[1], from[2], to[0], to[1], to[2]);
            }
        }
    }

    public static AABB bounds(List<double[]> corners) {
        AABB found = NOWHERE;
        for (double[] corner : corners) {
            found = new AABB(Math.min(found.minX, corner[0]), Math.min(found.minY, corner[1]),
                    Math.min(found.minZ, corner[2]), Math.max(found.maxX, corner[0]),
                    Math.max(found.maxY, corner[1]), Math.max(found.maxZ, corner[2]));
        }
        return found;
    }

    public static double lowest(List<double[]> corners) {
        double floor = Double.MAX_VALUE;
        for (double[] corner : corners) {
            floor = Math.min(floor, corner[1]);
        }
        return floor == Double.MAX_VALUE ? 0.0D : floor;
    }
}