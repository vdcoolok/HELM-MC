package dev.helm.movement;

import dev.helm.movement.step.PlanStep;
import dev.helm.movement.step.StepContext;
import dev.helm.movement.step.StepKind;

public final class RouteProximity {

    private RouteProximity() {
    }

    public static double distanceFrom(Route route, StepContext context) {
        double nearest = Double.MAX_VALUE;
        for (PlanStep step : route.steps()) {
            nearest = Math.min(nearest, step.footprint().nearestTo(
                    context.player().getX(), context.player().getY(), context.player().getZ()));
        }
        return nearest;
    }

    public static double distanceFromFalling(PlanStep fall, StepContext context) {
        double x = context.player().getX() - (fall.toX() + 0.5D);
        double z = context.player().getZ() - (fall.toZ() + 0.5D);
        return Math.sqrt(x * x + z * z);
    }

    public static boolean standingOn(PlanStep step, int[] feet) {
        return step.footprint().contains(feet[0], feet[1], feet[2]);
    }

    public static boolean falling(PlanStep step) {
        return step.kind() == StepKind.FALL;
    }
}
