package dev.helm.render;

import java.util.List;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;

public final class DropSprite {

    private DropSprite() {
    }

    public static List<double[]> corners(Entity dropped) {
        EntityDimensions size = dropped.getDimensions(dropped.getPose());
        double halfWidth = size.width() / 2.0D;
        double height = size.height();
        double x = dropped.getX();
        double y = dropped.getY();
        double z = dropped.getZ();
        double yaw = Math.toRadians(dropped.getYRot());
        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);
        return List.of(
                corner(x, y, z, -halfWidth, 0.0D, cos, sin),
                corner(x, y, z, halfWidth, 0.0D, cos, sin),
                corner(x, y, z, halfWidth, height, cos, sin),
                corner(x, y, z, -halfWidth, height, cos, sin));
    }

    public static LineBatch outline(LineBatch batch, List<double[]> corners) {
        for (int i = 0; i < corners.size(); i++) {
            double[] from = corners.get(i);
            double[] to = corners.get((i + 1) % corners.size());
            batch.segment(from[0], from[1], from[2], to[0], to[1], to[2]);
        }
        return batch;
    }

    private static double[] corner(double x, double y, double z, double side, double up,
                                   double cos, double sin) {
        return new double[]{x + side * cos, y + up, z - side * sin};
    }
}