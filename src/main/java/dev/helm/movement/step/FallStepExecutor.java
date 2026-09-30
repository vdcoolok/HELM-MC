package dev.helm.movement.step;

import dev.helm.movement.MoveState;
import dev.helm.movement.MoveTick;

public final class FallStepExecutor implements StepExecutor {

    @Override
    public MoveState advance(StepContext context, MoveTick tick, PlanStep step) {
        if (!StepPreparation.ready(context, tick, step)) {
            return MoveState.PREPPING;
        }
        StepPreparation.advanceStatus(tick);
        if (tick.state() != MoveState.RUNNING) {
            return tick.state();
        }
        int[] feet = context.feet();
        if (feet[0] == step.toX() && feet[1] == step.toY() && feet[2] == step.toZ()) {
            return MoveState.SUCCESS;
        }
        StepPreparation.walkTowards(context, tick, step.toX(), step.toY(), step.toZ());
        return MoveState.RUNNING;
    }
}
