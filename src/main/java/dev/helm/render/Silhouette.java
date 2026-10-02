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
        List<double[]> at = new ArrayList<>(points.size());
        for (double[] point : points) {
            at.add(new double[]{
                    x + point[0] * cos + point[2] * sin,
                    y + point[1],
                    z - point[0] * sin + point[2] * cos});
        }
        return toView(at, view);
    }

    public static List<double[]> at(int x, int y, int z, List<double[]> points,
                                    ViewOffset view) {
        List<double[]> world = new ArrayList<>(points.size());
        for (double[] point : points) {
            world.add(new double[]{point[0] + x, point[1] + y, point[2] + z});
        }
        return toView(world, view);
    }

    private static List<double[]> toView(List<double[]> points, ViewOffset view) {
        List<double[]> moved = new ArrayList<>(points.size());
        for (double[] point : points) {
            moved.add(new double[]{view.applyX(point[0]), view.applyY(point[1]),
                    view.applyZ(point[2])});
        }
        return moved;
    }
}