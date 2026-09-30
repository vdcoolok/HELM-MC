package dev.helm.pathfinding.world.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class Passability {

    private Passability() {
    }

    public static boolean neverWalk(Block block) {
        return block instanceof net.minecraft.world.level.block.BaseFireBlock
                || block == Blocks.COBWEB
                || block == Blocks.END_PORTAL
                || block == Blocks.COCOA
                || block instanceof net.minecraft.world.level.block.AbstractSkullBlock
                || block == Blocks.BUBBLE_COLUMN
                || block instanceof net.minecraft.world.level.block.ShulkerBoxBlock
                || block instanceof net.minecraft.world.level.block.SlabBlock
                || block instanceof net.minecraft.world.level.block.TrapDoorBlock
                || block == Blocks.HONEY_BLOCK
                || block == Blocks.END_ROD
                || block == Blocks.SWEET_BERRY_BUSH
                || block == Blocks.POINTED_DRIPSTONE
                || block instanceof net.minecraft.world.level.block.AmethystClusterBlock
                || block instanceof net.minecraft.world.level.block.AzaleaBlock
                || block == Blocks.BIG_DRIPLEAF
                || block == Blocks.POWDER_SNOW
                || block instanceof net.minecraft.world.level.block.CauldronBlock;
    }

    public static boolean alwaysWalk(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof net.minecraft.world.level.block.AirBlock) {
            return true;
        }
        if (block instanceof net.minecraft.world.level.block.DoorBlock) {
            return block != Blocks.IRON_DOOR;
        }
        if (block instanceof net.minecraft.world.level.block.FenceGateBlock) {
            return true;
        }
        if (block instanceof net.minecraft.world.level.block.SnowLayerBlock) {
            return false;
        }
        return false;
    }

    public static boolean maybeWalk(BlockState state) {
        return state.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.SnowLayerBlock;
    }

    public static boolean neverFullyPass(Block block) {
        return block instanceof net.minecraft.world.level.block.BaseFireBlock
                || block == Blocks.TRIPWIRE
                || block == Blocks.COBWEB
                || block == Blocks.VINE
                || block == Blocks.LADDER
                || block == Blocks.COCOA
                || block instanceof net.minecraft.world.level.block.AzaleaBlock
                || block instanceof net.minecraft.world.level.block.DoorBlock
                || block instanceof net.minecraft.world.level.block.FenceGateBlock
                || block instanceof net.minecraft.world.level.block.SnowLayerBlock
                || block instanceof net.minecraft.world.level.block.TrapDoorBlock
                || block == Blocks.END_PORTAL
                || block instanceof net.minecraft.world.level.block.SkullBlock
                || block instanceof net.minecraft.world.level.block.ShulkerBoxBlock;
    }

    public static boolean neverReplaceable(BlockState state) {
        return state.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.SnowLayerBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.DoorBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.FenceGateBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.SlabBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.StairBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.TrapDoorBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.LadderBlock
                || state.getBlock() == Blocks.VINE
                || state.getBlock() instanceof net.minecraft.world.level.block.WallBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.FlowerBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.SaplingBlock;
    }

    public static boolean replaceableNow(BlockState state) {
        return state.getBlock() instanceof net.minecraft.world.level.block.AirBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.SnowLayerBlock
                || state.getBlock() == Blocks.LARGE_FERN
                || state.getBlock() == Blocks.TALL_GRASS
                || state.canBeReplaced();
    }

    public static boolean placeableAgainst(BlockState state) {
        return BlockShapes.fullCube(state)
                || state.getBlock() == Blocks.GLASS
                || state.getBlock() instanceof net.minecraft.world.level.block.StainedGlassBlock;
    }
}