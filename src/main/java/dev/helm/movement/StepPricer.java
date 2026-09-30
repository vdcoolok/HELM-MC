package dev.helm.movement;

import dev.helm.pathfinding.cost.MoveCosts;
import dev.helm.pathfinding.move.MoveExpander;
import dev.helm.pathfinding.move.MoveTarget;
import dev.helm.movement.step.PlanStep;

public final class StepPricer {

    private final MoveExpander[] expanders;
    private final MoveTarget scratch = new MoveTarget();

    public StepPricer(MoveExpander[] expanders) {
        this.expanders = expanders;
    }

    public double reprice(PlanStep step) {
        scratch.clear();
        expanders[step.move().ordinal()].expand(step.fromX(), step.fromY(), step.fromZ(), scratch);
        return scratch.cost;
    }

    public boolean impossible(PlanStep step) {
        return reprice(step) >= MoveCosts.IMPOSSIBLE;
    }
}
