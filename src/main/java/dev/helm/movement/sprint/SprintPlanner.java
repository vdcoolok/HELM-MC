package dev.helm.movement.sprint;

import dev.helm.movement.Route;
import dev.helm.movement.step.PlanStep;
import dev.helm.movement.step.StepContext;
import dev.helm.movement.step.StepKind;

public final class SprintPlanner {

    private static final double FARM_TOLERANCE = 0.07D;

    private SprintPlanner() {
    }

    public static SprintChoice decide(Route route, int index, StepContext context, boolean asked) {
        if (index >= route.length()) {
            return SprintChoice.hold();
        }
        SprintChoice shortcut = StepUpSprint.shortcut(route, index, context);
        if (shortcut != null) {
            return shortcut;
        }
        if (asked) {
            return SprintChoice.charge();
        }
        PlanStep current = route.at(index);
        if (current.kind() == StepKind.DROP) {
            return DescendSprint.decide(route, index, context);
        }
        if (current.kind() == StepKind.STEP_UP) {
            return afterStepUp(route, index, context);
        }
        return SprintChoice.hold();
    }

    private static SprintChoice afterStepUp(Route route, int index, StepContext context) {
        if (landedFromDescend(route, index, context)) {
            return SprintChoice.sprintReleasingJump();
        }
        if (index == 0 || index > route.length() - 2) {
            return SprintChoice.hold();
        }
        PlanStep previous = route.at(index - 1);
        if (previous.kind() == StepKind.STEP
                && StepUpSprint.usable(context, previous, route.at(index), route.at(index + 1))) {
            return SprintChoice.charge();
        }
        return SprintChoice.hold();
    }

    private static boolean landedFromDescend(Route route, int index, StepContext context) {
        if (index == 0 || index >= route.length()) {
            return false;
        }
        PlanStep current = route.at(index);
        PlanStep previous = route.at(index - 1);
        return previous.kind() == StepKind.DROP
                && StepUpSprint.sameHeading(previous, current)
                && context.player().getY() >= current.fromY() + 1 - FARM_TOLERANCE;
    }
}
