package dev.helm.navigate;

import dev.helm.movement.Route;
import dev.helm.movement.step.StepContext;

public final class Splice {

    private static final double FALLING_FAST = -0.1D;

    private Splice() {
    }

    public static boolean ontoPlannedRoute(Route planned, StepContext context, int[] feet) {
        var player = context.player();
        if (player == null) {
            return false;
        }
        if (!player.onGround() && !inLiquid(context, feet)) {
            return false;
        }
        if (player.getDeltaMovement().y < FALLING_FAST) {
            return false;
        }
        return planned.holds(feet[0], feet[1], feet[2]);
    }

    private static boolean inLiquid(StepContext context, int[] feet) {
        return dev.helm.pathfinding.world.block.LiquidRules
                .any(context.world().stateAt(feet[0], feet[1], feet[2]));
    }
}
