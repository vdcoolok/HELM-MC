package dev.helm.mine.goal;

import dev.helm.pathfinding.goal.Goal;
import dev.helm.pathfinding.goal.GoalDistances;
import dev.helm.pathfinding.cost.FallCosts;

public final class BelowSpotGoal implements Goal {

    private final int x;
    private final int y;
    private final int z;

    public BelowSpotGoal(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean reached(int otherX, int otherY, int otherZ) {
        return otherX == x && (otherY == y || otherY == y - 1) && otherZ == z;
    }

    @Override
    public double estimate(int otherX, int otherY, int otherZ) {
        return levelCost(otherY) + GoalDistances.flat(otherX - x, otherZ - z);
    }

    private double levelCost(int otherY) {
        if (otherY > y) {
            return FallCosts.forDistance(2) / 2 * (otherY - y);
        }
        if (otherY < y - 1) {
            return FallCosts.jumpOneBlock() * (y - 1 - otherY);
        }
        return 0;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof BelowSpotGoal that && x == that.x && y == that.y && z == that.z;
    }

    @Override
    public int hashCode() {
        return (x * 31 + y) * 31 + z;
    }

    @Override
    public String toString() {
        return "at or below " + x + " " + y + " " + z;
    }
}