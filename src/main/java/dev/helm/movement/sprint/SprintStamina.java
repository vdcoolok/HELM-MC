package dev.helm.movement.sprint;

import dev.helm.movement.step.StepContext;

public final class SprintStamina {

    private static final int MIN_FOOD = 6;

    private SprintStamina() {
    }

    public static boolean available(StepContext context) {
        return context.movement().sprintAllowed()
                && context.player() != null
                && context.player().getFoodData().getFoodLevel() > MIN_FOOD;
    }
}
