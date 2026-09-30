package dev.helm.pathfinding.world.block;

import dev.helm.pathfinding.context.Tunables;
import dev.helm.pathfinding.cost.MoveCosts;
import dev.helm.tools.BreakStrength;
import dev.helm.pathfinding.world.BlockView;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class WorkCosts {

    private final BlockView world;
    private final WalkRules walk;
    private final Tunables tuning;
    private final BreakStrength breakStrength;

    public WorkCosts(BlockView world, WalkRules walk, Tunables tuning, BreakStrength breakStrength) {
        this.world = world;
        this.walk = walk;
        this.tuning = tuning;
        this.breakStrength = breakStrength;
    }

    public double breakTicks(int x, int y, int z, BlockState state, boolean includeFallingAbove) {
        if (walk.through(x, y, z, state)) {
            return 0;
        }
        if (!state.getFluidState().isEmpty()) {
            return MoveCosts.IMPOSSIBLE;
        }
        if (!tuning.breakAllowed() || avoidBreaking(x, y, z, state)) {
            return MoveCosts.IMPOSSIBLE;
        }
        double strength = breakStrength.against(state.getBlock());
        if (strength <= 0) {
            return MoveCosts.IMPOSSIBLE;
        }
        double cost = 1 / strength + tuning.breakAdditionalCost();
        if (includeFallingAbove) {
            BlockState above = world.stateAt(x, y + 1, z);
            if (above.getBlock() instanceof FallingBlock) {
                cost += breakTicks(x, y + 1, z, above, true);
            }
        }
        return cost;
    }

    public double breakTicks(int x, int y, int z, boolean includeFallingAbove) {
        return breakTicks(x, y, z, world.stateAt(x, y, z), includeFallingAbove);
    }

    public boolean avoidBreaking(int x, int y, int z, BlockState state) {
        if (!world.insideBorder(x, y, z)) {
            return true;
        }
        Block block = state.getBlock();
        if (block == Blocks.ICE || block instanceof InfestedBlock) {
            return true;
        }
        return adjacentBreakingRisk(x, y + 1, z, true)
                || adjacentBreakingRisk(x + 1, y, z, false)
                || adjacentBreakingRisk(x - 1, y, z, false)
                || adjacentBreakingRisk(x, y, z + 1, false)
                || adjacentBreakingRisk(x, y, z - 1, false);
    }

    private boolean adjacentBreakingRisk(int x, int y, int z, boolean directlyAbove) {
        BlockState state = world.stateAt(x, y, z);
        Block block = state.getBlock();
        if (!directlyAbove && block instanceof FallingBlock && FallingBlock.isFree(world.stateAt(x, y - 1, z))) {
            return true;
        }
        if (block instanceof net.minecraft.world.level.block.LiquidBlock) {
            if (directlyAbove) {
                return true;
            }
            if (state.getValue(net.minecraft.world.level.block.LiquidBlock.LEVEL) == 0) {
                return true;
            }
            return !(world.stateAt(x, y - 1, z).getBlock()
                    instanceof net.minecraft.world.level.block.LiquidBlock);
        }
        return !state.getFluidState().isEmpty();
    }

    public double placeAt(int x, int y, int z, BlockState state) {
        if (!tuning.placeAllowed() || !Passability.replaceableNow(state)) {
            return MoveCosts.IMPOSSIBLE;
        }
        return tuning.placementCost();
    }
}