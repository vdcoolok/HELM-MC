package dev.helm.pathfinding.world.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.pathfinder.PathComputationType;

public final class BlockShapes {

    private static final byte UNKNOWN = 0;
    private static final byte NO = 1;
    private static final byte YES = 2;

    private static final byte[] FULL_CUBE = new byte[Block.BLOCK_STATE_REGISTRY.size()];
    private static final byte[] LAND_BOUND = new byte[Block.BLOCK_STATE_REGISTRY.size()];

    private BlockShapes() {
    }

    public static boolean fullCube(BlockState state) {
        if (state.getBlock() == Blocks.AIR) {
            return false;
        }
        int id = Block.BLOCK_STATE_REGISTRY.getId(state);
        byte known = FULL_CUBE[id];
        if (known == UNKNOWN) {
            known = computeFullCube(state) ? YES : NO;
            FULL_CUBE[id] = known;
        }
        return known == YES;
    }

    private static boolean computeFullCube(BlockState state) {
        if (excluded(state.getBlock())) {
            return false;
        }
        try {
            return Block.isShapeFullBlock(state.getCollisionShape(null, null));
        } catch (RuntimeException unavailable) {
            return false;
        }
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

    public static boolean landBound(BlockState state) {
        int id = Block.BLOCK_STATE_REGISTRY.getId(state);
        byte known = LAND_BOUND[id];
        if (known == UNKNOWN) {
            known = state.isPathfindable(PathComputationType.LAND) ? YES : NO;
            LAND_BOUND[id] = known;
        }
        return known == YES;
    }

    public static boolean bottomSlab(BlockState state) {
        return state.getBlock() instanceof SlabBlock
                && state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
    }

    public static boolean halfSlab(BlockState state) {
        return state.getBlock() instanceof SlabBlock
                && state.getValue(SlabBlock.TYPE) != SlabType.DOUBLE;
    }
}
