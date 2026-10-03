package dev.helm.mine.goal;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.FallingBlock;

import dev.helm.mine.find.Breakable;
import dev.helm.mine.target.TargetFilter;
import dev.helm.pathfinding.goal.BlockGoal;
import dev.helm.pathfinding.goal.Goal;
import dev.helm.pathfinding.world.BlockView;
import dev.helm.pathfinding.world.block.WorkCosts;
import dev.helm.setting.MiningSettings;

public final class SpotGoals {

    private SpotGoals() {
    }

    public static Goal forPosition(BlockPos pos, List<BlockPos> known, TargetFilter filter,
                                   BlockView world, WorkCosts work, MiningSettings settings) {
        boolean shaftPossible = !(world.stateAt(pos.getX(), pos.getY() + 1,
                pos.getZ()).getBlock() instanceof FallingBlock);
        if (!settings.digIntoVein()) {
            return shaftPossible ? new ShaftSpotGoal(pos.getX(), pos.getY(), pos.getZ())
                    : new BelowSpotGoal(pos.getX(), pos.getY(), pos.getZ());
        }
        boolean upward = partOfVein(pos.above(), known, filter, world, work, settings);
        boolean downward = partOfVein(pos.below(), known, filter, world, work, settings);
        boolean twiceDown = partOfVein(pos.below(2), known, filter, world, work, settings);
        if (upward == downward) {
            if (twiceDown && shaftPossible) {
                return new ShaftSpotGoal(pos.getX(), pos.getY(), pos.getZ());
            }
            return new BelowSpotGoal(pos.getX(), pos.getY(), pos.getZ());
        }
        if (upward) {
            return new BlockGoal(pos.getX(), pos.getY(), pos.getZ());
        }
        if (twiceDown && shaftPossible) {
            return new BelowSpotGoal(pos.getX(), pos.getY() - 1, pos.getZ());
        }
        return new BlockGoal(pos.getX(), pos.getY() - 1, pos.getZ());
    }

    private static boolean partOfVein(BlockPos pos, List<BlockPos> known, TargetFilter filter,
                                      BlockView world, WorkCosts work, MiningSettings settings) {
        if (known.contains(pos)) {
            return true;
        }
        if (settings.digThroughAir()
                && world.stateAt(pos.getX(), pos.getY(), pos.getZ()).getBlock()
                    instanceof AirBlock) {
            return true;
        }
        return filter.wants(world.stateAt(pos.getX(), pos.getY(), pos.getZ()))
                && Breakable.worthMining(world, work, pos);
    }
}