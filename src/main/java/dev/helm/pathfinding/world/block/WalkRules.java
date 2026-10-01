package dev.helm.pathfinding.world.block;

import dev.helm.pathfinding.context.Tunables;
import dev.helm.pathfinding.world.BlockView;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.FluidState;

public final class WalkRules {

    private final BlockView world;
    private final Tunables tuning;

    public WalkRules(BlockView world, Tunables tuning) {
        this.world = world;
        this.tuning = tuning;
    }

    public boolean through(int x, int y, int z, BlockState state) {
        if (doNotBreak(state)) {
            return false;
        }
        if (state.getBlock() instanceof AirBlock) {
            return true;
        }
        if (Passability.neverWalk(state.getBlock())) {
            return false;
        }
        if (Passability.alwaysWalk(state)) {
            return true;
        }
        if (state.getBlock() == Blocks.IRON_DOOR) {
            return false;
        }
        if (state.getBlock() instanceof net.minecraft.world.level.block.FenceGateBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.DoorBlock) {
            return true;
        }
        if (Passability.maybeWalk(state)) {
            return throughAt(x, y, z, state);
        }
        if (!state.getFluidState().isEmpty()) {
            return state.getFluidState().getType().getAmount(state.getFluidState()) == 8
                    && throughAt(x, y, z, state);
        }
        return BlockShapes.landBound(state);
    }

    public boolean through(int x, int y, int z) {
        return through(x, y, z, world.stateAt(x, y, z));
    }

    private boolean throughAt(int x, int y, int z, BlockState state) {
        if (state.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock) {
            return onTop(x, y - 1, z);
        }
        if (state.getBlock() instanceof SnowLayerBlock) {
            if (!world.loaded(x, z)) {
                return true;
            }
            if (state.getValue(SnowLayerBlock.LAYERS) >= 3) {
                return false;
            }
            return onTop(x, y - 1, z);
        }
        FluidState fluid = state.getFluidState();
        if (!fluid.isEmpty()) {
            if (LiquidRules.source(x, y, z, state, world::stateAt)) {
                return false;
            }
            if (tuning.assumeWalkOnWater()) {
                return false;
            }
            BlockState above = world.stateAt(x, y + 1, z);
            if (!above.getFluidState().isEmpty() || above.getBlock() == Blocks.LILY_PAD) {
                return false;
            }
            return LiquidRules.water(state);
        }
        return BlockShapes.landBound(state);
    }

    public boolean fullyPassable(int x, int y, int z, BlockState state) {
        if (doNotBreak(state)) {
            return false;
        }
        if (state.getBlock() instanceof AirBlock) {
            return true;
        }
        if (Passability.neverFullyPass(state.getBlock())
                || !state.getFluidState().isEmpty()) {
            return false;
        }
        return BlockShapes.landBound(state);
    }

    private boolean doNotBreak(BlockState state) {
        return world.doNotBreak().contains(state.getBlock());
    }

    public boolean fullyPassable(int x, int y, int z) {
        return fullyPassable(x, y, z, world.stateAt(x, y, z));
    }

    public boolean replaceable(int x, int y, int z, BlockState state) {
        if (state.getBlock() instanceof net.minecraft.world.level.block.AirBlock) {
            return true;
        }
        if (state.getBlock() instanceof SnowLayerBlock) {
            if (!world.loaded(x, z)) {
                return true;
            }
            return state.getValue(SnowLayerBlock.LAYERS) == 1;
        }
        if (state.getBlock() == Blocks.LARGE_FERN || state.getBlock() == Blocks.TALL_GRASS) {
            return true;
        }
        if (Passability.neverReplaceable(state)) {
            return false;
        }
        return state.canBeReplaced();
    }

    public boolean onTop(int x, int y, int z, BlockState state) {
        if (BlockShapes.fullCube(state)
                && (state.getBlock() != Blocks.MAGMA_BLOCK || tuning.magmaWalkAllowed())
                && state.getBlock() != Blocks.BUBBLE_COLUMN
                && state.getBlock() != Blocks.HONEY_BLOCK) {
            return true;
        }
        if (state.getBlock() instanceof net.minecraft.world.level.block.AzaleaBlock) {
            return true;
        }
        if (state.getBlock() == Blocks.LADDER
                || (Climbable.is(state.getBlock()) && tuning.vinesWalkAllowed())) {
            return true;
        }
        if (state.getBlock() == Blocks.FARMLAND
                || state.getBlock() == Blocks.DIRT_PATH
                || state.getBlock() == Blocks.SOUL_SAND) {
            return true;
        }
        if (state.getBlock() == Blocks.ENDER_CHEST
                || state.getBlock() == Blocks.CHEST
                || state.getBlock() == Blocks.TRAPPED_CHEST) {
            return true;
        }
        if (state.getBlock() == Blocks.GLASS
                || state.getBlock() instanceof net.minecraft.world.level.block.StainedGlassBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.StairBlock) {
            return true;
        }
        if (LiquidRules.water(state)) {
            return onTopOver(x, y, z, state);
        }
        if (LiquidRules.lava(state)) {
            return !LiquidRules.source(x, y, z, state, world::stateAt);
        }
        if (state.getBlock() instanceof net.minecraft.world.level.block.SlabBlock) {
            if (!tuning.bottomSlabWalkAllowed()) {
                return state.getValue(net.minecraft.world.level.block.SlabBlock.TYPE) != SlabType.BOTTOM;
            }
            return true;
        }
        return false;
    }

    private boolean onTopOver(int x, int y, int z, BlockState state) {
        BlockState above = world.stateAt(x, y + 1, z);
        if (above.getBlock() == Blocks.LILY_PAD
                || above.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock) {
            return true;
        }
        boolean moving = LiquidRules.source(x, y, z, state, world::stateAt)
                || above.getFluidState().getType() == net.minecraft.world.level.material.Fluids.FLOWING_WATER;
        if (moving) {
            return LiquidRules.water(above) && !tuning.assumeWalkOnWater();
        }
        return LiquidRules.water(above) != tuning.assumeWalkOnWater();
    }

    public boolean onTop(int x, int y, int z) {
        return onTop(x, y, z, world.stateAt(x, y, z));
    }

    public boolean frostWalkerTurns(BlockState state) {
        return tuning.frostWalkerLevel() != 0
                && state.getBlock() == Blocks.WATER
                && state.getValue(net.minecraft.world.level.block.LiquidBlock.LEVEL) == 0;
    }

    public boolean solidNeededToStand(int x, int y, int z, BlockState state) {
        if (Climbable.is(state.getBlock())) {
            return false;
        }
        if (!state.getFluidState().isEmpty()) {
            if (state.getBlock() instanceof net.minecraft.world.level.block.SlabBlock) {
                if (state.getValue(net.minecraft.world.level.block.SlabBlock.TYPE) != SlabType.BOTTOM) {
                    return true;
                }
            } else if (state.getBlock() instanceof net.minecraft.world.level.block.StairBlock) {
                return true;
            }
            if (tuning.assumeWalkOnWater()) {
                return false;
            }
            if (world.stateAt(x, y + 1, z).getBlock()
                    instanceof net.minecraft.world.level.block.LiquidBlock) {
                return false;
            }
        }
        return true;
    }
}