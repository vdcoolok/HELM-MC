package dev.helm.pathfinding.world.block;

import dev.helm.pathfinding.context.Tunables;
import dev.helm.pathfinding.world.BlockView;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

public final class WalkRules {

    private final BlockView world;
    private final Tunables tuning;
    private final StateVerdicts verdicts;

    public WalkRules(BlockView world, Tunables tuning) {
        this.world = world;
        this.tuning = tuning;
        this.verdicts = new StateVerdicts();
    }

    public boolean through(int x, int y, int z, BlockState state) {
        if (doNotBreak(state)) {
            return false;
        }
        return switch (verdicts.through(state)) {
            case YES -> true;
            case NO -> false;
            case MAYBE -> throughAt(x, y, z, state);
        };
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
        if (LiquidRules.any(state)) {
            if (LiquidRules.source(x, y, z, state, world::stateAt)) {
                return false;
            }
            if (tuning.assumeWalkOnWater()) {
                return false;
            }
            BlockState above = world.stateAt(x, y + 1, z);
            if (LiquidRules.any(above) || above.getBlock() == Blocks.LILY_PAD) {
                return false;
            }
            return LiquidRules.water(state);
        }
        return false;
    }

    public boolean fullyPassable(int x, int y, int z, BlockState state) {
        if (doNotBreak(state)) {
            return false;
        }
        return verdicts.fullyPassable(state) == Ternary.YES;
    }

    private boolean doNotBreak(BlockState state) {
        return world.doNotBreak().contains(state.getBlock());
    }

    public boolean fullyPassable(int x, int y, int z) {
        return fullyPassable(x, y, z, world.stateAt(x, y, z));
    }

    public boolean replaceable(int x, int y, int z, BlockState state) {
        if (state.getBlock() instanceof AirBlock) {
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
        return switch (verdicts.onTop(state)) {
            case YES -> true;
            case NO -> false;
            case MAYBE -> decidedByNeighbours(x, y, z, state);
        };
    }

    private boolean decidedByNeighbours(int x, int y, int z, BlockState state) {
        if (state.getBlock() == Blocks.MAGMA_BLOCK) {
            return tuning.magmaWalkAllowed();
        }
        if (state.getBlock() instanceof net.minecraft.world.level.block.SlabBlock) {
            if (!tuning.bottomSlabWalkAllowed()) {
                return state.getValue(net.minecraft.world.level.block.SlabBlock.TYPE)
                        != SlabType.BOTTOM;
            }
            return true;
        }
        if (LiquidRules.water(state)) {
            return onTopOver(x, y, z, state);
        }
        if (LiquidRules.lava(state)) {
            return !LiquidRules.source(x, y, z, state, world::stateAt);
        }
        return tuning.vinesWalkAllowed();
    }

    private boolean onTopOver(int x, int y, int z, BlockState state) {
        BlockState above = world.stateAt(x, y + 1, z);
        if (above.getBlock() == Blocks.LILY_PAD
                || above.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock) {
            return true;
        }
        boolean moving = LiquidRules.source(x, y, z, state, world::stateAt)
                || above.getFluidState().getType()
                        == net.minecraft.world.level.material.Fluids.FLOWING_WATER;
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
