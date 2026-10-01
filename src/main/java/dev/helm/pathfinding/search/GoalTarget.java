package dev.helm.pathfinding.search;

import dev.helm.diag.Trace;
import dev.helm.pathfinding.goal.BlockGoal;
import dev.helm.pathfinding.goal.ColumnGoal;
import dev.helm.pathfinding.goal.Goal;
import dev.helm.pathfinding.world.BlockView;
import dev.helm.setting.PathSettings;

public final class GoalTarget {

    private GoalTarget() {
    }

    public static Goal reachable(Goal wanted, BlockView world, PathSettings settings) {
        if (!(wanted instanceof BlockGoal block)) {
            return wanted;
        }
        if (!settings.simplifyUnloadedGoal()) {
            return wanted;
        }
        if (world.residentChunk(block.x(), block.z())) {
            return wanted;
        }
        Trace.instance().event("search", "the goal at " + block.x() + " " + block.y() + " "
                + block.z() + " is not in a loaded chunk, so aiming for that column at any "
                + "height instead");
        return new ColumnGoal(block.x(), block.z());
    }
}
