package dev.helm.pathfinding.goal;

import net.minecraft.core.BlockPos;

public record DroppedGoal(BlockPos pos) implements Goal {

    @Override
    public boolean reached(int otherX, int otherY, int otherZ) {
        return otherX == pos.getX() && otherY == pos.getY() && otherZ == pos.getZ();
    }

    @Override
    public double estimate(int otherX, int otherY, int otherZ) {
        return GoalDistances.level(pos.getY(), otherY)
                + GoalDistances.flat(otherX - pos.getX(), otherZ - pos.getZ());
    }

    @Override
    public String toString() {
        return "dropped item at " + pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }
}