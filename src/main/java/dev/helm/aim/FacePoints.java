package dev.helm.aim;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public final class FacePoints {

    private static final double[] SPREAD = {0.1D, 0.5D, 0.9D};

    private FacePoints() {
    }

    public static List<Vec3> on(BlockPos pos, Direction face, Vec3 eye) {
        Direction.Axis flat = face.getAxis();
        Direction.Axis along = across(flat);
        Direction.Axis across = beside(flat);
        double edge = face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0D : 0.0D;

        double[] offsets = new double[3];
        offsets[axis(flat)] = edge;
        List<Vec3> points = new ArrayList<>(SPREAD.length * SPREAD.length);
        for (double first : SPREAD) {
            offsets[axis(along)] = first;
            for (double second : SPREAD) {
                offsets[axis(across)] = second;
                points.add(new Vec3(pos.getX() + offsets[0], pos.getY() + offsets[1],
                        pos.getZ() + offsets[2]));
            }
        }
        points.sort(Comparator.comparingDouble(point -> point.distanceToSqr(eye)));
        return points;
    }

    private static Direction.Axis across(Direction.Axis flat) {
        return switch (flat) {
            case X -> Direction.Axis.Y;
            case Y -> Direction.Axis.X;
            case Z -> Direction.Axis.X;
        };
    }

    private static Direction.Axis beside(Direction.Axis flat) {
        return switch (flat) {
            case X, Y -> Direction.Axis.Z;
            case Z -> Direction.Axis.Y;
        };
    }

    private static int axis(Direction.Axis axis) {
        return switch (axis) {
            case X -> 0;
            case Y -> 1;
            case Z -> 2;
        };
    }
}
