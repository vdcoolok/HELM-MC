package dev.helm.movement.step;

import java.util.ArrayList;
import java.util.List;

import dev.helm.pathfinding.world.BlockView;
import dev.helm.pathfinding.world.block.WalkRules;

public final class StepBlocks {

    private StepBlocks() {
    }

    public static List<int[]> toBreak(BlockView world, WalkRules walk,
                                      int fromX, int fromY, int fromZ,
                                      int toX, int toY, int toZ) {
        List<int[]> blocks = new ArrayList<>(3);
        addIfSolid(world, walk, blocks, toX, toY, toZ);
        addIfSolid(world, walk, blocks, toX, toY + 1, toZ);
        if (toY < fromY) {
            addIfSolid(world, walk, blocks, toX, toY - 1, toZ);
        } else if (toY > fromY) {
            addIfSolid(world, walk, blocks, fromX, fromY + 2, fromZ);
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
}
