package dev.helm.movement.step;

import dev.helm.control.Control;
import dev.helm.movement.MoveTick;
import dev.helm.pathfinding.world.block.Climbable;
import dev.helm.pathfinding.world.block.LiquidRules;

public final class PillarPreparation {

    private PillarPreparation() {
    }

    public static void apply(StepContext context, MoveTick tick, PlanStep step) {
        int[] feet = context.feet();
        boolean atColumn = feet[0] == step.fromX() && feet[1] == step.fromY()
                && feet[2] == step.fromZ();
        boolean underColumn = feet[0] == step.fromX() && feet[1] == step.fromY() - 1
                && feet[2] == step.fromZ();
        if (!atColumn && !underColumn) {
            return;
        }
        if (Climbable.is(context.world().stateAt(step.fromX(), step.fromY() - 1,
                step.fromZ()).getBlock())) {
            tick.press(Control.SNEAK);
        }
    }

    public static boolean alreadySwimming(StepContext context, PlanStep step) {
        return LiquidRules.water(context.world().stateAt(step.toX(), step.toY() + 1, step.toZ()));
    }
}
