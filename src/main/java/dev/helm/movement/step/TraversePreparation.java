package dev.helm.movement.step;

import dev.helm.control.Control;
import dev.helm.movement.MoveTick;
import dev.helm.pathfinding.world.block.Climbable;

public final class TraversePreparation {

    private TraversePreparation() {
    }

    public static void apply(StepContext context, MoveTick tick, PlanStep step) {
        int[] feet = context.feet();
        boolean atSource = feet[0] == step.fromX() && feet[1] == step.fromY()
                && feet[2] == step.fromZ();
        boolean belowSource = feet[0] == step.fromX() && feet[1] == step.fromY() - 1
                && feet[2] == step.fromZ();
        if (!atSource && !belowSource) {
            return;
        }
        if (Climbable.is(context.world().stateAt(step.fromX(), step.fromY() - 1,
                step.fromZ()).getBlock())) {
            tick.press(Control.SNEAK);
        }
    }
}
