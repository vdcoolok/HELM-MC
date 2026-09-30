package dev.helm.movement.step;

import dev.helm.movement.MoveState;
import dev.helm.movement.MoveTick;

public interface StepExecutor {

    MoveState advance(StepContext context, MoveTick tick, PlanStep step);
}
