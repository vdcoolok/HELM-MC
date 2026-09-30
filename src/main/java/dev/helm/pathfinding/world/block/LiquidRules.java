package dev.helm.pathfinding.world.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
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
        if (!flowing(state.getFluidState().getType())) {
            return false;
        }
        if (flowing(state)) {
            return true;
        }
        return flowing(lookup.stateAt(x + 1, y, z))
                || flowing(lookup.stateAt(x - 1, y, z))
                || flowing(lookup.stateAt(x, y, z + 1))
                || flowing(lookup.stateAt(x, y, z - 1));
    }

    public static boolean flowing(BlockState state) {
        FluidState fluid = state.getFluidState();
        return flowing(fluid.getType()) && fluid.getType().getAmount(fluid) != 8;
    }

    private static boolean flowing(Fluid fluid) {
        return fluid instanceof FlowingFluid;
    }

    public interface BlockViewLookup {

        BlockState stateAt(int x, int y, int z);
    }
}