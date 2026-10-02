package dev.helm.render;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.AABB;

public final class DropSprite {

    private static final double THICKNESS = 0.03D;

    private DropSprite() {
    }

    public static AABB bounds(Entity dropped) {
        EntityDimensions size = dropped.getDimensions(dropped.getPose());
        double halfWidth = size.width() / 2.0D;
        double height = size.height();
        double half = THICKNESS / 2.0D;
        double[][] corners = {
                {-halfWidth, 0.0D, -half},
                {halfWidth, 0.0D, -half},
                {halfWidth, 0.0D, half},
                {-halfWidth, 0.0D, half},
                {-halfWidth, height, -half},
                {halfWidth, height, -half},
                {halfWidth, height, half},
                {-halfWidth, height, half},
        };
        return hull(dropped, corners);
    }

    private static AABB hull(Entity dropped, double[][] corners) {
        double yaw = Math.toRadians(dropped.getYRot());
        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;
        for (double[] corner : corners) {
            double x = dropped.getX() + corner[0] * cos + corner[2] * sin;
            double z = dropped.getZ() - corner[0] * sin + corner[2] * cos;
            double y = dropped.getY() + corner[1];
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
        }
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}