package dev.helm.navigate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import dev.helm.movement.step.StepContext;
import dev.helm.pathfinding.world.block.WalkRules;

public final class PathStart {

    private static final double REACH_LIMIT = 0.8D;
    private static final int CONSIDERED = 4;

    private PathStart() {
    }

    public static int[] whereTheWalkBegins(StepContext context) {
        int[] feet = context.feet();
        WalkRules walk = context.walk();
        if (walk.onTop(feet[0], feet[1] - 1, feet[2])) {
            return feet;
        }
        if (context.player() != null && context.player().onGround()) {
            return besideTheEdge(context, feet, walk);
        }
        if (walk.onTop(feet[0], feet[1] - 2, feet[2])) {
            return new int[]{feet[0], feet[1] - 1, feet[2]};
        }
        return feet;
    }

    private static int[] besideTheEdge(StepContext context, int[] feet, WalkRules walk) {
        double x = context.player().getX();
        double z = context.player().getZ();
        List<int[]> nearest = new ArrayList<>(9);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                nearest.add(new int[]{feet[0] + dx, feet[1], feet[2] + dz});
            }
        }
        nearest.sort(Comparator.comparingDouble(support -> flatGap(support, x, z)));
        for (int taken = 0; taken < Math.min(CONSIDERED, nearest.size()); taken++) {
            int[] support = nearest.get(taken);
            double gapX = Math.abs(support[0] + 0.5D - x);
            double gapZ = Math.abs(support[2] + 0.5D - z);
            if (gapX > REACH_LIMIT && gapZ > REACH_LIMIT) {
                continue;
            }
            if (standable(walk, support)) {
                return support;
            }
        }
        return feet;
    }

    private static boolean standable(WalkRules walk, int[] support) {
        return walk.onTop(support[0], support[1] - 1, support[2])
                && walk.through(support[0], support[1], support[2])
                && walk.through(support[0], support[1] + 1, support[2]);
    }

    private static double flatGap(int[] support, double x, double z) {
        double dx = support[0] + 0.5D - x;
        double dz = support[2] + 0.5D - z;
        return dx * dx + dz * dz;
    }
}
