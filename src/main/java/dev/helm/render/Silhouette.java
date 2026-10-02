package dev.helm.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.Entity;

public final class Silhouette {

    private Silhouette() {
    }

    public static List<double[]> placed(Entity owner, List<double[]> points, ViewOffset view) {
        double yaw = Math.toRadians(owner.getYRot());
        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);
        double x = owner.getX();
        double y = owner.getY();
        double z = owner.getZ();
        List<double[]> placed = new ArrayList<>(points.size());
        for (double[] point : points) {
            double px = x + point[0] * cos + point[2] * sin;
            double py = y + point[1];
            double pz = z - point[0] * sin + point[2] * cos;
            placed.add(new double[]{view.applyX(px), view.applyY(py), view.applyZ(pz)});
        }
        return placed;
    }
}