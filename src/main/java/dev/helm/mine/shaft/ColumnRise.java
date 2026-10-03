package dev.helm.mine.shaft;

import net.minecraft.core.BlockPos;

import dev.helm.pathfinding.goal.BlockGoal;
import dev.helm.pathfinding.goal.Goal;

public record ColumnRise(BlockPos target, int x, int y, int z) {

    public Goal stand() {
        return new BlockGoal(x, y, z);
    }
}