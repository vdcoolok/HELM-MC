package dev.helm.pathfinding.goal;

import dev.helm.pathfinding.cost.FallCosts;
import dev.helm.setting.Settings;

public final class GoalDistances {

    private static final double ROOT_TWO = Math.sqrt(2);

    private GoalDistances() {
    }

    public static double flat(double xDiff, double zDiff) {
        double along = Math.abs(xDiff);
        double across = Math.abs(zDiff);
        double straight;
        double diagonal;
        if (along < across) {
            straight = across - along;
            diagonal = along;
        } else {
            straight = along - across;
            diagonal = across;
        }
        return (diagonal * ROOT_TWO + straight) * Settings.holder().path().costHeuristic();
    }

    public static double level(int goalLevel, int currentLevel) {
        if (currentLevel > goalLevel) {
            return FallCosts.forDistance(2) / 2 * (currentLevel - goalLevel);
        }
        if (currentLevel < goalLevel) {
            return (goalLevel - currentLevel) * FallCosts.jumpOneBlock();
        }
        return 0;
    }
}
