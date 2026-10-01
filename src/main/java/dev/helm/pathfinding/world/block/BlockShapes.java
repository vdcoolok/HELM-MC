package dev.helm.pathfinding.world.block;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.pathfinder.PathComputationType;

public final class BlockShapes {

    private static final Map<BlockState, Boolean> FULL_CUBE = new ConcurrentHashMap<>();
    private static final Map<BlockState, Boolean> LAND_BOUND = new ConcurrentHashMap<>();

    private BlockShapes() {
    }

    public static boolean fullCube(BlockState state) {
        if (state.getBlock() == Blocks.AIR) {
            return false;
        }
        return FULL_CUBE.computeIfAbsent(state, BlockShapes::computeFullCube);
    }

    private static boolean computeFullCube(BlockState state) {
        if (excluded(state.getBlock())) {
            return false;
        }
        return collisionIsFullCube(state);
    }

    private static boolean excluded(Block block) {
        return block == Blocks.BAMBOO
                || block == Blocks.POTTED_BAMBOO
                || block == Blocks.MOVING_PISTON
                || block == Blocks.SCAFFOLDING
                || block == Blocks.SHULKER_BOX
                || block == Blocks.POINTED_DRIPSTONE
                || block == Blocks.AMETHYST_CLUSTER;
    }

    private static boolean collisionIsFullCube(BlockState state) {
        try {
            return Block.isShapeFullBlock(state.getCollisionShape(null, null));
        } catch (RuntimeException unavailable) {
            return false;
        }
    }

    public static boolean landBound(BlockState state) {
        return LAND_BOUND.computeIfAbsent(state,
                value -> value.isPathfindable(PathComputationType.LAND));
    }

    public static boolean bottomSlab(BlockState state) {
        return state.getBlock() instanceof net.minecraft.world.level.block.SlabBlock
                && state.getValue(net.minecraft.world.level.block.SlabBlock.TYPE) == SlabType.BOTTOM;
    }

    public static boolean halfSlab(BlockState state) {
        return state.getBlock() instanceof net.minecraft.world.level.block.SlabBlock
                && state.getValue(net.minecraft.world.level.block.SlabBlock.TYPE) != SlabType.DOUBLE;
    }
}