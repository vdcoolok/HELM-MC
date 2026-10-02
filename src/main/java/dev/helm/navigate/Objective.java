package dev.helm.navigate;

import dev.helm.pathfinding.goal.BlockGoal;
import dev.helm.pathfinding.goal.Goal;

public record Objective(Goal goal, String label) {

    public static Objective at(int x, int y, int z) {
        return new Objective(new BlockGoal(x, y, z), x + " " + y + " " + z);
    }

    public boolean satisfiedBy(int feetX, int feetY, int feetZ) {
        return goal.reached(feetX, feetY, feetZ);
    }
}