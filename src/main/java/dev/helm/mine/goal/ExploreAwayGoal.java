package dev.helm.mine.goal;

import dev.helm.pathfinding.goal.Goal;
import dev.helm.pathfinding.goal.GoalDistances;

public final class ExploreAwayGoal implements Goal {

    private static final double AWAY_WEIGHT = 1.0D;

    private final int fromX;
    private final int fromZ;

    public ExploreAwayGoal(int fromX, int fromZ) {
        this.fromX = fromX;
        this.fromZ = fromZ;
    }

    @Override
    public boolean reached(int otherX, int otherY, int otherZ) {
        return false;
    }

    @Override
    public double estimate(int otherX, int otherY, int otherZ) {
        double away = GoalDistances.flat(fromX - otherX, fromZ - otherZ);
        return -AWAY_WEIGHT * away;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ExploreAwayGoal that
                && fromX == that.fromX && fromZ == that.fromZ;
    }

    @Override
    public int hashCode() {
        return fromX * 31 + fromZ;
    }

    @Override
    public String toString() {
        return "away from " + fromX + " " + fromZ;
    }
}