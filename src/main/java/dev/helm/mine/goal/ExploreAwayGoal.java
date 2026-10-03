package dev.helm.mine.goal;

import dev.helm.pathfinding.cost.FallCosts;
import dev.helm.pathfinding.goal.Goal;
import dev.helm.pathfinding.goal.GoalDistances;

public final class ExploreAwayGoal implements Goal {

    private static final double AWAY_WEIGHT = 0.6D;
    private static final double LEVEL_WEIGHT = 1.5D;

    private final int fromX;
    private final int fromZ;
    private final int level;

    public ExploreAwayGoal(int fromX, int fromZ, int level) {
        this.fromX = fromX;
        this.fromZ = fromZ;
        this.level = level;
    }

    @Override
    public boolean reached(int otherX, int otherY, int otherZ) {
        return false;
    }

    @Override
    public double estimate(int otherX, int otherY, int otherZ) {
        double away = GoalDistances.flat(fromX - otherX, fromZ - otherZ);
        return -AWAY_WEIGHT * away + LEVEL_WEIGHT * levelCost(otherY);
    }

    private double levelCost(int otherY) {
        if (otherY > level) {
            return FallCosts.forDistance(2) / 2 * (otherY - level);
        }
        if (otherY < level) {
            return FallCosts.jumpOneBlock() * (level - otherY);
        }
        return 0;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ExploreAwayGoal that
                && fromX == that.fromX && fromZ == that.fromZ && level == that.level;
    }

    @Override
    public int hashCode() {
        return (fromX * 31 + fromZ) * 31 + level;
    }

    @Override
    public String toString() {
        return "away from " + fromX + " " + fromZ + " at level " + level;
    }
}