package dev.helm.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.phys.AABB;

public final class ShapeOutline {

    private static final double PRECISION = 0.0005D;
    private static final double FLATNESS = 0.25D;
    private static final AABB NOWHERE = new AABB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);

    private ShapeOutline() {
    }

    public static void draw(LineBatch batch, List<double[]> corners) {
        List<double[]> face = flatFace(corners);
        if (!face.isEmpty()) {
            each(batch, face);
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

    public static List<double[]> flatFace(List<double[]> corners) {
        AABB box = bounds(corners);
        double longest = Math.max(box.getXsize(), Math.max(box.getYsize(), box.getZsize()));
        if (longest <= 0.0D) {
            return List.of();
        }
        double[] sizes = {box.getXsize(), box.getYsize(), box.getZsize()};
        int thinnest = 0;
        for (int axis = 1; axis < sizes.length; axis++) {
            if (sizes[axis] < sizes[thinnest]) {
                thinnest = axis;
            }
        }
        if (sizes[thinnest] > longest * FLATNESS) {
            return List.of();
        }
        double edge = corners.get(0)[thinnest];
        for (double[] corner : corners) {
            edge = Math.min(edge, corner[thinnest]);
        }
        List<double[]> face = new ArrayList<>(4);
        for (double[] corner : corners) {
            if (Math.abs(corner[thinnest] - edge) < PRECISION) {
                face.add(corner);
            }
        }
        return face.size() == 4 ? face : List.of();
    }

    public static AABB bounds(List<double[]> corners) {
        if (corners.isEmpty()) {
            return NOWHERE;
        }
        double[] first = corners.get(0);
        AABB found = new AABB(first[0], first[1], first[2], first[0], first[1], first[2]);
        for (int i = 1; i < corners.size(); i++) {
            double[] corner = corners.get(i);
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