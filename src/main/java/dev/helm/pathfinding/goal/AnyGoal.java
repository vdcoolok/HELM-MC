package dev.helm.pathfinding.goal;

import java.util.Arrays;
import java.util.List;

public final class AnyGoal implements Goal {

    private final Goal[] any;

    public AnyGoal(Goal... any) {
        this.any = any;
    }

    public static AnyGoal of(List<Goal> goals) {
        return new AnyGoal(goals.toArray(new Goal[0]));
    }

    @Override
    public boolean reached(int x, int y, int z) {
        for (Goal goal : any) {
            if (goal.reached(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double estimate(int x, int y, int z) {
        double cheapest = Double.MAX_VALUE;
        for (Goal goal : any) {
            cheapest = Math.min(cheapest, goal.estimate(x, y, z));
        }
        return cheapest;
    }

    public int count() {
        return any.length;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AnyGoal that && Arrays.equals(any, that.any);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(any);
    }

    @Override
    public String toString() {
        return any.length + " targets";
    }
}