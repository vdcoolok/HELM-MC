package dev.helm.movement.sprint;

import dev.helm.movement.Route;
import dev.helm.movement.step.PlanStep;
import dev.helm.movement.step.StepContext;
import dev.helm.movement.step.StepKind;

public final class DescendSprint {

    private DescendSprint() {
    }

    public static SprintChoice decide(Route route, int index, StepContext context) {
        PlanStep current = route.at(index);
        boolean forced = onFrostedLanding(context, route, index, current);
        boolean cautious = forced || DescendSafety.needed(context, current);
        if (cautious && !DescendSafety.overshootsIntoAir(context, current)) {
            return forced ? SprintChoice.holdSteadyDescend() : SprintChoice.hold();
        }
        if (index >= route.length() - 1) {
            return SprintChoice.hold();
        }
        PlanStep next = route.at(index + 1);
        if (next.kind() == StepKind.STEP_UP && StepUpSprint.sameHeading(current, next)) {
            return SprintChoice.sprint(index + 1);
        }
        if (!carriesInto(context, current, next)) {
            return SprintChoice.hold();
        }
        if (index <= route.length() - 3 && next.kind() == StepKind.DROP) {
            PlanStep after = route.at(index + 2);
            if (after.kind() == StepKind.DROP && !carriesInto(context, next, after)) {
                return SprintChoice.hold();
            }
        }
        return SprintChoice.charge();
    }

    private static boolean onFrostedLanding(StepContext context, Route route, int index,
                                            PlanStep current) {
        if (index >= route.length() - 1) {
            return false;
        }
        PlanStep next = route.at(index + 1);
        if (next.kind() != StepKind.STEP && next.kind() != StepKind.BOUND) {
            return false;
        }
        boolean couldPlaceInstead = context.movement().allowPlace()
                && next.kind() == StepKind.BOUND;
        return !couldPlaceInstead
                && StepUpSprint.sameHeading(current, next)
                && DescendSafety.frostedLanding(context, next);
    }

    private static boolean carriesInto(StepContext context, PlanStep current, PlanStep next) {
        if (next.kind() == StepKind.DROP && StepUpSprint.sameHeading(current, next)) {
            return true;
        }
        if (!context.walk().onTop(current.toX() + current.directionX(), current.toY(),
                current.toZ() + current.directionZ())) {
            return false;
        }
        return next.kind() == StepKind.LEAN && context.movement().overshootDiagonalDescend();
    }
}
