package dev.helm.movement.step;

import java.util.ArrayList;
import java.util.List;

import dev.helm.pathfinding.world.BlockView;
import dev.helm.pathfinding.world.block.WalkRules;

public final class StepBlocks {

    private StepBlocks() {
    }

    public static List<int[]> toBreak(BlockView world, WalkRules walk,
                                      StepKind kind, int fromX, int fromY, int fromZ,
                                      int toX, int toY, int toZ) {
        List<int[]> blocks = new ArrayList<>(6);
        for (int[] at : BreakTargets.of(kind, fromX, fromY, fromZ, toX, toY, toZ)) {
            if (!walk.through(at[0], at[1], at[2])) {
                blocks.add(at);
            }
        }
        return blocks;
    }

    public static List<int[]> walkInto(BlockView world, WalkRules walk,
                                       int fromX, int fromY, int fromZ,
                                       int toX, int toZ, boolean diagonal) {
        List<int[]> blocks = new ArrayList<>(2);
        if (diagonal) {
            addIfSolid(world, walk, blocks, fromX, fromY, toZ);
            addIfSolid(world, walk, blocks, toX, fromY, fromZ);
        }
        return blocks;
    }

    private static void addIfSolid(BlockView world, WalkRules walk, List<int[]> blocks,
                                   int x, int y, int z) {
        if (!walk.through(x, y, z)) {
            blocks.add(new int[]{x, y, z});
        }
    }

    public static int[] placeUnder(BlockView world, WalkRules walk, int x, int y, int z) {
        return walk.onTop(x, y, z) ? null : new int[]{x, y, z};
    }
}
