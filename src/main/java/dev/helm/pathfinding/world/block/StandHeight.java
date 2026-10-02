package dev.helm.pathfinding.world.block;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;

import dev.helm.pathfinding.world.BlockView;

public final class StandHeight {

    private static final double CLEARANCE = 0.1251D;

    private StandHeight() {
    }

    public static int[] of(BlockView world, double x, double y, double z) {
        int blockX = floor(x);
        int blockZ = floor(z);
        int blockY = floor(y + CLEARANCE);
        if (risesOnto(world, blockX, blockY, blockZ)) {
            blockY++;
        }
        return new int[]{blockX, blockY, blockZ};
    }

    private static boolean risesOnto(BlockView world, int x, int y, int z) {
        BlockState state = world.stateAtOrNull(x, y, z);
        return state != null && state.getBlock() instanceof SlabBlock;
    }

    private static int floor(double value) {
        int truncated = (int) value;
        return value < truncated ? truncated - 1 : truncated;
    }
}
