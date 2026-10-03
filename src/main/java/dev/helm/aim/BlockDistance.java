package dev.helm.aim;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockDistance {

    private BlockDistance() {
    }

    public static double nearest(Entity viewer, BlockPos pos, boolean sneaking) {
        ClientLevel level = (ClientLevel) viewer.level();
        VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
        double minX = shape.isEmpty() ? 0.0D : shape.min(Direction.Axis.X);
        double maxX = shape.isEmpty() ? 1.0D : shape.max(Direction.Axis.X);
        double minY = shape.isEmpty() ? 0.0D : shape.min(Direction.Axis.Y);
        double maxY = shape.isEmpty() ? 1.0D : shape.max(Direction.Axis.Y);
        double minZ = shape.isEmpty() ? 0.0D : shape.min(Direction.Axis.Z);
        double maxZ = shape.isEmpty() ? 1.0D : shape.max(Direction.Axis.Z);
        Vec3 eyes = eyes(viewer, sneaking);
        double x = eyes.x - clamp(eyes.x, pos.getX() + minX, pos.getX() + maxX);
        double y = eyes.y - clamp(eyes.y, pos.getY() + minY, pos.getY() + maxY);
        double z = eyes.z - clamp(eyes.z, pos.getZ() + minZ, pos.getZ() + maxZ);
        return Math.sqrt(x * x + y * y + z * z);
    }

    private static Vec3 eyes(Entity viewer, boolean sneaking) {
        return sneaking
                ? new Vec3(viewer.getX(), viewer.getY() + viewer.getEyeHeight(Pose.CROUCHING),
                        viewer.getZ())
                : viewer.getEyePosition(1.0F);
    }

    private static double clamp(double value, double low, double high) {
        return Math.max(low, Math.min(high, value));
    }
}