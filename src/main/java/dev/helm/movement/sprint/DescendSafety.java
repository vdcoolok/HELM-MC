package dev.helm.movement.sprint;

import dev.helm.movement.FrostWalker;
import dev.helm.movement.step.PlanStep;
import dev.helm.movement.step.StepBlocks;
import dev.helm.movement.step.StepContext;
import dev.helm.pathfinding.world.block.Hazards;

public final class DescendSafety {

    private DescendSafety() {
    }

    private static int intoX(PlanStep step) {
        return step.toX() * 2 - step.fromX();
    }

    private static int intoY(PlanStep step) {
        return step.toY() * 2 - step.fromY() + 1;
    }

    private static int intoZ(PlanStep step) {
        return step.toZ() * 2 - step.fromZ();
    }

    public static boolean overshootsIntoAir(StepContext context, PlanStep step) {
        int x = intoX(step);
        int y = intoY(step);
        int z = intoZ(step);
        return !context.walk().through(x, y, z)
                && context.walk().through(x, y + 1, z)
                && context.walk().through(x, y + 2, z);
    }

    public static boolean needed(StepContext context, PlanStep step) {
        if (overshootsIntoAir(context, step)) {
            return true;
        }
        boolean magma = context.movement().magmaWalkAllowed();
        for (int level = 0; level <= 2; level++) {
            if (Hazards.avoidWalkingInto(
                    context.world().stateAt(intoX(step), intoY(step) + level, intoZ(step)),
                    magma)) {
                return true;
            }
        }
        return false;
    }

    public static boolean frostedLanding(StepContext context, PlanStep next) {
        return FrostWalker.wornBy(context.player())
                && context.walk().frostWalkerTurns(
                        context.world().stateAt(next.toX(), next.toY() - 1, next.toZ()));
    }

    public static boolean blocksAhead(StepContext context, PlanStep step) {
        return !step.blocksToBreak().isEmpty();
    }
}
