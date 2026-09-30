package dev.helm.pathfinding.world.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;

public final class Hazards {

    private Hazards() {
    }

    public static boolean avoidWalkingInto(BlockState state) {
        Block block = state.getBlock();
        return !state.getFluidState().isEmpty()
                || block == Blocks.MAGMA_BLOCK
                || block == Blocks.CACTUS
                || block == Blocks.SWEET_BERRY_BUSH
                || block instanceof net.minecraft.world.level.block.BaseFireBlock
                || block == Blocks.END_PORTAL
                || block == Blocks.COBWEB
                || block == Blocks.BUBBLE_COLUMN;
    }

    public static boolean avoidWalkingInto(BlockState state, boolean magmaWalkAllowed) {
        return blockIsMagmaAndForbidden(state, magmaWalkAllowed) || avoidWalkingIntoWithoutMagma(state);
    }

    private static boolean avoidWalkingIntoWithoutMagma(BlockState state) {
        Block block = state.getBlock();
        return !state.getFluidState().isEmpty()
                || block == Blocks.CACTUS
                || block == Blocks.SWEET_BERRY_BUSH
                || block instanceof net.minecraft.world.level.block.BaseFireBlock
                || block == Blocks.END_PORTAL
                || block == Blocks.COBWEB
                || block == Blocks.BUBBLE_COLUMN;
    }

    private static boolean blockIsMagmaAndForbidden(BlockState state, boolean magmaWalkAllowed) {
        return state.getBlock() == Blocks.MAGMA_BLOCK && !magmaWalkAllowed;
    }
}