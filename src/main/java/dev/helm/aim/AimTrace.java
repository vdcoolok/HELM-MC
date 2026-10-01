package dev.helm.aim;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class AimTrace {

    private AimTrace() {
    }

    public static HitResult towards(Entity viewer, Aim aim, double reach, boolean sneaking) {
        Vec3 start = sneaking ? crouchingEyes(viewer) : viewer.getEyePosition(1.0F);
        Vec3 direction = BlockReach.directionOf(aim);
        Vec3 end = start.add(direction.scale(reach));
        return viewer.level().clip(new ClipContext(start, end,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, viewer));
    }

    public static HitResult towards(Entity viewer, Aim aim, double reach) {
        return towards(viewer, aim, reach, false);
    }

    public static Vec3 crouchingEyes(Entity viewer) {
        return new Vec3(viewer.getX(),
                viewer.getY() + viewer.getEyeHeight(Pose.CROUCHING),
                viewer.getZ());
    }
}