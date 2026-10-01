package dev.helm.movement.step;

import java.util.ArrayList;
import java.util.List;

public final class BreakTargets {

    private BreakTargets() {
    }

    public static List<int[]> of(StepKind kind, int fromX, int fromY, int fromZ,
                                 int toX, int toY, int toZ) {
        List<int[]> at = new ArrayList<>(6);
        switch (kind) {
            case STEP -> {
                at.add(block(toX, toY + 1, toZ));
                at.add(block(toX, toY, toZ));
            }
            case STEP_UP -> {
                at.add(block(toX, toY, toZ));
                at.add(block(fromX, fromY + 2, fromZ));
                at.add(block(toX, toY + 1, toZ));
            }
            case DROP -> {
                at.add(block(toX, toY + 2, toZ));
                at.add(block(toX, toY + 1, toZ));
                at.add(block(toX, toY, toZ));
            }
            case FALL -> {
                for (int level = fromY + 1; level >= toY - 1; level--) {
                    at.add(block(toX, level, toZ));
                }
            }
            case LEAN -> {
                at.add(block(fromX, fromY, toZ));
                at.add(block(fromX, fromY + 1, toZ));
                at.add(block(toX, fromY, fromZ));
                at.add(block(toX, fromY + 1, fromZ));
                at.add(block(toX, toY, toZ));
                at.add(block(toX, toY + 1, toZ));
            }
            case RAISE -> at.add(block(fromX, fromY + 2, fromZ));
            case SINK -> at.add(block(toX, toY, toZ));
            case BOUND -> {
            }
        }
        return at;
    }

    private static int[] block(int x, int y, int z) {
        return new int[]{x, y, z};
    }
}
