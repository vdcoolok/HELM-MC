package dev.helm.aim;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import dev.helm.setting.LookSettings;

public final class BlockReach {

    private static final double[][] FACE_CORNERS = {
            {0.5D, 0.0D, 0.5D},
            {0.5D, 1.0D, 0.5D},
            {0.5D, 0.5D, 0.0D},
            {0.5D, 0.5D, 1.0D},
            {0.0D, 0.5D, 0.5D},
            {1.0D, 0.5D, 0.5D}
    };

    private BlockReach() {
    }

    public static Aim towards(Entity viewer, BlockPos pos, LookSettings settings, boolean sneaking) {
        Aim held = held(viewer, pos, settings, sneaking);
        if (held != null) {
            return held;
        }
        Aim atCentre = probe(viewer, pos, shapeCentre(viewer, pos), settings, sneaking);
        if (atCentre != null) {
            return atCentre;
        }
        for (double[] corner : FACE_CORNERS) {
            Aim atCorner = probe(viewer, pos, cornerOf(pos, corner), settings, sneaking);
            if (atCorner != null) {
                return atCorner;
            }
        }
        return null;
    }

    private static Aim held(Entity viewer, BlockPos pos, LookSettings settings, boolean sneaking) {
        if (!settings.remainWithLookDirection() || !lookingAt(viewer, pos, settings)) {
            return null;
        }
        Aim hypothetical = current(viewer).shifted(0.0D, 0.0001D);
        if (!sneaking) {
            return hypothetical;
        }
        HitResult trace = trace(viewer, hypothetical, settings, true);
        return hits(trace, pos) ? hypothetical : null;
    }

    private static boolean lookingAt(Entity viewer, BlockPos pos, LookSettings settings) {
        HitResult trace = AimTrace.towards(viewer, new Aim(viewer.getYRot(), viewer.getXRot()),
                settings.blockReachDistance(), false);
        return hits(trace, pos);
    }

    private static boolean hits(HitResult trace, BlockPos pos) {
        return trace != null && trace.getType() == HitResult.Type.BLOCK
                && ((BlockHitResult) trace).getBlockPos().equals(pos);
    }

    private static Aim probe(Entity viewer, BlockPos pos, Vec3 point,
                             LookSettings settings, boolean sneaking) {
        Vec3 eyes = eyePosition(viewer, sneaking);
        Aim wanted = Aiming.lookFrom(eyes.x, eyes.y, eyes.z, point.x, point.y, point.z);
        HitResult trace = trace(viewer, wanted, settings, sneaking);
        if (trace == null || trace.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockPos hit = ((BlockHitResult) trace).getBlockPos();
        if (hit.equals(pos)) {
            return wanted;
        }
        if (hit.equals(pos.below())
                && viewer.level().getBlockState(pos).getBlock() instanceof BaseFireBlock) {
            return wanted;
        }
        return null;
    }

    private static Vec3 cornerOf(BlockPos pos, double[] corner) {
        double x;
        BlockState state = Minecraft.getInstance().level == null
                ? null
                : Minecraft.getInstance().level.getBlockState(pos);
        VoxelShape shape = state == null
                ? Shapes.block()
                : state.getCollisionShape(Minecraft.getInstance().level, pos);
        if (shape.isEmpty()) {
            shape = Shapes.block();
        }
        x = shape.min(Direction.Axis.X) * corner[0] + shape.max(Direction.Axis.X) * (1 - corner[0]);
        double y = shape.min(Direction.Axis.Y) * corner[1] + shape.max(Direction.Axis.Y) * (1 - corner[1]);
        double z = shape.min(Direction.Axis.Z) * corner[2] + shape.max(Direction.Axis.Z) * (1 - corner[2]);
        return new Vec3(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
    }

    public static Aim current(Entity viewer) {
        return new Aim(viewer.getYRot(), viewer.getXRot());
    }

    public static Vec3 centre(BlockPos pos) {
        return new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
    }

    private static Vec3 shapeCentre(Entity viewer, BlockPos pos) {
        var level = viewer.level();
        VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
        if (shape.isEmpty()) {
            return centre(pos);
        }
        return new Vec3(pos.getX() + (float) (shape.min(Direction.Axis.X) + shape.max(Direction.Axis.X)) / 2,
                pos.getY() + (float) (shape.min(Direction.Axis.Y) + shape.max(Direction.Axis.Y)) / 2,
                pos.getZ() + (float) (shape.min(Direction.Axis.Z) + shape.max(Direction.Axis.Z)) / 2);
    }

    public static HitResult trace(Entity viewer, Aim aim, LookSettings settings, boolean sneaking) {
        return AimTrace.towards(viewer, aim, settings.blockReachDistance(), sneaking);
    }

    public static Vec3 directionOf(Aim aim) {
        double yaw = -Math.toRadians(aim.yaw()) - Math.PI;
        double pitch = -Math.toRadians(aim.pitch());
        double flatX = Math.sin(yaw);
        double flatZ = Math.cos(yaw);
        double base = -Math.cos(pitch);
        double height = Math.sin(pitch);
        return new Vec3(flatX * base, height, flatZ * base);
    }

    public static Vec3 eyePosition(Entity viewer, boolean sneaking) {
        return sneaking ? AimTrace.crouchingEyes(viewer) : viewer.getEyePosition(1.0F);
    }
}
