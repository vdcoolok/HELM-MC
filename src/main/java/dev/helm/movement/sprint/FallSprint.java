package dev.helm.movement.sprint;

import dev.helm.aim.Aim;
import dev.helm.aim.Aiming;
import dev.helm.movement.Route;
import dev.helm.movement.step.PlanStep;
import dev.helm.movement.step.StepBlocks;
import dev.helm.movement.step.StepContext;
import dev.helm.movement.step.StepKind;

public final class FallSprint {

    private static final int DEEPEST_EXTENDED_FALL = -3;
    private static final int MAX_EXTRA_STEPS = 2;
    private static final double REACH_BACK = 0.4D;

    private FallSprint() {
    }

    public record Glide(int landingIndex, int landingX, int landingY, int landingZ,
                        double targetX, double targetY, double targetZ) {

        public boolean landedOn(StepContext context) {
            int[] feet = context.feet();
            return feet[0] == landingX && feet[1] == landingY && feet[2] == landingZ;
        }

        public Aim aimFrom(StepContext context) {
            return Aiming.lookFrom(context.player().getX(), context.player().getEyeY(),
                    context.player().getZ(), targetX, targetY, targetZ);
        }
    }

    public static Glide extension(Route route, int index, StepContext context) {
        if (index >= route.length()) {
            return null;
        }
        PlanStep fall = route.at(index);
        if (fall.kind() != StepKind.FALL || fall.directionY() < DEEPEST_EXTENDED_FALL) {
            return null;
        }
        if (breakingSomething(context, fall)) {
            return null;
        }
        int reach = reach(route, index, fall, context);
        if (reach == index) {
            return null;
        }
        int extra = reach - index;
        double ahead = extra - REACH_BACK;
        return new Glide(reach,
                fall.toX() + fall.directionX() * extra,
                fall.toY(),
                fall.toZ() + fall.directionZ() * extra,
                fall.directionX() * ahead + fall.toX() + 0.5D,
                fall.toY(),
                fall.directionZ() * ahead + fall.toZ() + 0.5D);
    }

    private static int reach(Route route, int index, PlanStep fall, StepContext context) {
        int reach = index;
        for (int cursor = index + 1;
             cursor < route.length() && cursor < index + MAX_EXTRA_STEPS + 1;
             cursor++) {
            PlanStep next = route.at(cursor);
            if (!continues(context, fall, next)) {
                break;
            }
            reach = cursor;
        }
        return reach;
    }

    private static boolean continues(StepContext context, PlanStep fall, PlanStep next) {
        if (next.kind() != StepKind.STEP
                || next.directionX() != fall.directionX()
                || next.directionZ() != fall.directionZ()) {
            return false;
        }
        for (int y = next.toY(); y <= fall.fromY() + 1; y++) {
            if (!context.walk().fullyPassable(next.toX(), y, next.toZ())) {
                return false;
            }
        }
        return context.walk().onTop(next.toX(), next.toY() - 1, next.toZ());
    }

    private static boolean breakingSomething(StepContext context, PlanStep fall) {
        return !StepBlocks.toBreak(context.world(), context.walk(), fall.fromX(), fall.fromY(),
                fall.fromZ(), fall.toX(), fall.toY(), fall.toZ()).isEmpty();
    }
}
