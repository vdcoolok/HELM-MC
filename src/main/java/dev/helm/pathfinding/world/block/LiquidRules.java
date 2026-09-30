package dev.helm.pathfinding.world.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.FlowingFluid;

public final class LiquidRules {

    private LiquidRules() {
    }

    public static boolean water(BlockState state) {
        FluidState fluid = state.getFluidState();
        return fluid.getType() == Fluids.WATER || fluid.getType() == Fluids.FLOWING_WATER;
    }

    public static boolean lava(BlockState state) {
        FluidState fluid = state.getFluidState();
        return fluid.getType() == Fluids.LAVA || fluid.getType() == Fluids.FLOWING_LAVA;
    }

    public static boolean any(BlockState state) {
        return !state.getFluidState().isEmpty();
    }

    public static boolean source(int x, int y, int z, BlockState state, BlockViewLookup lookup) {
        FluidState fluid = state.getFluidState();
        if (!(fluid.getType() instanceof FlowingFluid)) {
            return false;
        }
        if (fluid.getType().getAmount(fluid) != 8) {
            return true;
        }
        return spreading(lookup.stateAt(x + 1, y, z))
                || spreading(lookup.stateAt(x - 1, y, z))
                || spreading(lookup.stateAt(x, y, z + 1))
                || spreading(lookup.stateAt(x, y, z - 1));
    }

    private static boolean spreading(BlockState state) {
        FluidState fluid = state.getFluidState();
        return fluid.getType() instanceof FlowingFluid && fluid.getType().getAmount(fluid) != 8;
    }

    public interface BlockViewLookup {

        BlockState stateAt(int x, int y, int z);
    }
}