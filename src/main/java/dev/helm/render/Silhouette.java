package dev.helm.render;

import java.util.List;

public final class Silhouette {

    private Silhouette() {
    }

    public static List<double[]> at(double x, double y, double z, List<double[]> points,
                                    ViewOffset view) {
        List<double[]> placed = new java.util.ArrayList<>(points.size());
        for (double[] point : points) {
            placed.add(new double[]{view.applyX(point[0] + x), view.applyY(point[1] + y),
                    view.applyZ(point[2] + z)});
        }
        return placed;
    }

    public static List<double[]> spun(List<double[]> points, float spin, float rise) {
        double angle = Math.toRadians(spin);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        List<double[]> placed = new java.util.ArrayList<>(points.size());
        for (double[] point : points) {
            placed.add(new double[]{point[0] * cos - point[2] * sin, point[1] + rise,
                    point[0] * sin + point[2] * cos});
        }
        return placed;
    }
}