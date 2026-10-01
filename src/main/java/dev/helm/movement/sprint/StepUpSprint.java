package dev.helm.movement.sprint;

import dev.helm.movement.Route;
import dev.helm.movement.step.PlanStep;
import dev.helm.movement.step.StepContext;
import dev.helm.movement.step.StepKind;
import dev.helm.pathfinding.world.block.Hazards;

public final class StepUpSprint {

    private static final int WIDTH_SCAN = 2;
    private static final int RISE_SCAN = 3;
    private static final double CENTRED_TOLERANCE = 0.1D;
    private static final double HEAD_BONK_TOLERANCE = 0.8D;

    private StepUpSprint() {
    }

    public static SprintChoice shortcut(Route route, int index, StepContext context) {
        if (index > route.length() - 3) {
            return null;
        }
        PlanStep flat = route.at(index);
        if (flat.kind() != StepKind.STEP) {
            return null;
        }
        PlanStep up = route.at(index + 1);
        if (up.kind() != StepKind.STEP_UP) {
            return null;
        }
        if (!usable(context, flat, up, route.at(index + 2))) {
            return null;
        }
        if (!centredEnough(context, flat)) {
            return null;
        }
        return SprintChoice.sprint(index + 1);
    }

    public static boolean usable(StepContext context, PlanStep flat, PlanStep up, PlanStep after) {
        if (!context.movement().sprintAscends()) {
            return false;
        }
        if (!sameHeading(flat, up) || !sameHeading(up, after)) {
            return false;
        }
        if (!context.walk().onTop(flat.toX(), flat.toY() - 1, flat.toZ())) {
            return false;
        }
        if (!context.walk().onTop(up.toX(), up.toY() - 1, up.toZ())) {
            return false;
        }
        if (DescendSafety.blocksAhead(context, up)) {
            return false;
        }
        return headroom(context, flat) && clearLanding(context, up);
    }

    public static boolean sameHeading(PlanStep first, PlanStep second) {
        return first.directionX() == second.directionX()
                && first.directionZ() == second.directionZ();
    }

    private static boolean headroom(StepContext context, PlanStep flat) {
        for (int across = 0; across < WIDTH_SCAN; across++) {
            int x = flat.fromX() + (across == 1 ? flat.directionX() : 0);
            int z = flat.fromZ() + (across == 1 ? flat.directionZ() : 0);
            for (int rise = 0; rise < RISE_SCAN; rise++) {
                if (!context.walk().fullyPassable(x, flat.fromY() + rise, z)) {
                    return false;
                }
            }
        }
        return !Hazards.avoidWalkingInto(context.world()
                .stateAt(flat.fromX(), flat.fromY() + RISE_SCAN, flat.fromZ()),
                context.movement().magmaWalkAllowed());
    }

    private static boolean clearLanding(StepContext context, PlanStep up) {
        return !Hazards.avoidWalkingInto(
                context.world().stateAt(up.toX(), up.toY() + 2, up.toZ()),
                context.movement().magmaWalkAllowed());
    }

    private static boolean centredEnough(StepContext context, PlanStep flat) {
        double offTarget = Math.abs(flat.directionX()
                * (flat.fromZ() + 0.5D - context.player().getZ()))
                + Math.abs(flat.directionZ()
                * (flat.fromX() + 0.5D - context.player().getX()));
        if (offTarget > CENTRED_TOLERANCE) {
            return false;
        }
        int bonkX = flat.fromX() - flat.directionX();
        int bonkZ = flat.fromZ() - flat.directionZ();
        if (context.walk().fullyPassable(bonkX, flat.fromY() + 2, bonkZ)) {
            return true;
        }
        double gap = Math.abs(flat.directionX() * (bonkX + 0.5D - context.player().getX()))
                + Math.abs(flat.directionZ() * (bonkZ + 0.5D - context.player().getZ()));
        return gap > HEAD_BONK_TOLERANCE;
    }
}
