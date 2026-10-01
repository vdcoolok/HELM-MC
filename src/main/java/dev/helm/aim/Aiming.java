package dev.helm.aim;

import net.minecraft.util.Mth;

public final class Aiming {

    private static final double TO_DEGREES = 180.0D / Math.PI;

    private Aiming() {
    }

    public static Aim lookFrom(double fromX, double fromY, double fromZ,
                               double toX, double toY, double toZ) {
        double dx = fromX - toX;
        double dy = fromY - toY;
        double dz = fromZ - toZ;
        double yaw = Mth.atan2(dx, -dz);
        double flat = Math.sqrt(dx * dx + dz * dz);
        double pitch = Mth.atan2(dy, flat);
        return new Aim(yaw * TO_DEGREES, pitch * TO_DEGREES);
    }

    public static Aim towardCentre(int fromX, int fromY, int fromZ, int toX, int toY, int toZ) {
        return lookFrom(fromX, fromY, fromZ, toX + 0.5D, toY + 0.5D, toZ + 0.5D);
    }

    public static Aim towardBlock(net.minecraft.world.entity.Entity viewer,
                                  net.minecraft.core.BlockPos pos) {
        return lookFrom(viewer.getX(), viewer.getEyeY(), viewer.getZ(),
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
    }

    public static Aim shortestFrom(Aim current, Aim target) {
        if (current.yawNear(target)) {
            return new Aim(current.yaw(), target.pitch());
        }
        return new Aim(current.yaw() + AimAngles.wrapYaw(target.yaw() - current.yaw()),
                target.pitch());
    }
}
