package dev.helm.navigate;

import net.minecraft.core.BlockPos;

import dev.helm.pathfinding.goal.BlockGoal;

public record Destination(int x, int y, int z) {

    public BlockGoal goal() {
        return new BlockGoal(x, y, z);
    }

    public boolean reachedBy(BlockPos feet) {
        return feet.getX() == x && feet.getY() == y && feet.getZ() == z;
    }

    public String describe() {
        return x + " " + y + " " + z;
    }
}
