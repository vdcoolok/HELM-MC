package dev.helm.pathfinding.path;

import java.util.List;
import dev.helm.pathfinding.world.BlockView;

public final class PathCutoff {

    private PathCutoff() {
    }

    public static NodePath atLoadBoundary(NodePath path, BlockView world) {
        int stop = firstUnloadedPosition(path, world);
        return stop < 0 ? path : path.truncatedTo(stop);
    }

    public static NodePath shortOfGoal(NodePath path, int minimumLength, double factor) {
        int length = path.length();
        if (length < minimumLength) {
            return path;
        }
        int shortened = (int) ((length - minimumLength) * factor) + minimumLength - 1;
        if (shortened >= length) {
            return path;
        }
        return path.truncatedTo(shortened);
    }

    public static NodePath apply(NodePath path, BlockView world, boolean cutoffAtLoadBoundary) {
        if (path == null || path.isEmpty() || !cutoffAtLoadBoundary) {
            return path;
        }
        return atLoadBoundary(path, world);
    }

    private static int firstUnloadedPosition(NodePath path, BlockView world) {
        List<int[]> positions = path.positions();
        for (int index = 0; index < positions.size(); index++) {
            int[] position = positions.get(index);
            if (!world.residentChunk(position[0], position[2])) {
                return index;
            }
        }
        return -1;
    }
}
