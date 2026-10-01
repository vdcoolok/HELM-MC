package dev.helm.movement.step;

import dev.helm.movement.MoveState;
import dev.helm.movement.MoveTick;

public interface StepExecutor {

    MoveState advance(StepContext context, MoveTick tick, PlanStep step);

    default void begin() {
    }

    default boolean safeToCancel(StepContext context, PlanStep step, MoveTick tick) {
        return true;
    }
}
