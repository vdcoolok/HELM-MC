package dev.helm.pathfinding.world.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.pathfinder.PathComputationType;

public final class BlockShapes {

    private BlockShapes() {
    }

    public static boolean fullCube(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.BAMBOO
                || block == Blocks.POTTED_BAMBOO
                || block == Blocks.MOVING_PISTON
                || block == Blocks.SCAFFOLDING
                || block == Blocks.SHULKER_BOX
                || block == Blocks.POINTED_DRIPSTONE
                || block == Blocks.AMETHYST_CLUSTER) {
            return false;
        }
        try {
            return Block.isShapeFullBlock(state.getCollisionShape(null, null));
        } catch (RuntimeException unavailable) {
            return false;
        }
    }

    public static boolean bottomSlab(BlockState state) {
        return state.getBlock() instanceof net.minecraft.world.level.block.SlabBlock
                && state.getValue(net.minecraft.world.level.block.SlabBlock.TYPE) == SlabType.BOTTOM;
    }

    public static boolean halfSlab(BlockState state) {
        return state.getBlock() instanceof net.minecraft.world.level.block.SlabBlock
                && state.getValue(net.minecraft.world.level.block.SlabBlock.TYPE) != SlabType.DOUBLE;
    }

    public static boolean landBound(BlockState state) {
        return state.isPathfindable(PathComputationType.LAND);
    }
}