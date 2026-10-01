package dev.helm.movement.step;

import net.minecraft.world.level.block.Blocks;

import dev.helm.aim.Aiming;
import dev.helm.control.Control;
import dev.helm.movement.MoveState;
import dev.helm.movement.MoveTick;
import dev.helm.movement.sprint.DescendSafety;
import dev.helm.pathfinding.world.block.LiquidRules;

public final class DropStepExecutor implements StepExecutor {

    private static final int APPROACH_TICKS = 20;
    private static final double APPROACH_RANGE = 1.25D;
    private static final double SETTLED = 0.25D;
    private static final double SETTLED_HEIGHT = 0.5D;
    private static final double SOURCE_SHARE = 0.17D;
    private static final double TARGET_SHARE = 0.83D;

    private boolean safeLanding;
    private int ticks;

    @Override
    public void begin() {
        ticks = 0;
        clearSafeLanding();
    }

    @Override
    public MoveState advance(StepContext context, MoveTick tick, PlanStep step) {
        if (!StepPreparation.ready(context, tick, step)) {
            return MoveState.PREPPING;
        }
        StepPreparation.advanceStatus(tick);
        if (tick.state() != MoveState.RUNNING) {
            return tick.state();
        }

        int[] feet = context.feet();
        if (landed(context, step)) {
            return MoveState.SUCCESS;
        }
        if (safeLanding) {
            creepDown(context, tick, step);
            return MoveState.RUNNING;
        }
        if (DescendSafety.needed(context, step)) {
            creepDown(context, tick, step);
            return MoveState.RUNNING;
        }

        double offsetX = context.player().getX() - (step.toX() + 0.5D);
        double offsetZ = context.player().getZ() - (step.toZ() + 0.5D);
        double distanceToDest = Math.sqrt(offsetX * offsetX + offsetZ * offsetZ);
        double fromX = context.player().getX() - (step.fromX() + 0.5D);
        double fromZ = context.player().getZ() - (step.fromZ() + 0.5D);
        double distanceFromStart = Math.sqrt(fromX * fromX + fromZ * fromZ);

        tick.set(Control.SNEAK, context.movement().magmaWalkAllowed()
                && context.world().stateAt(feet[0], feet[1] - 1, feet[2]).getBlock()
                        == Blocks.MAGMA_BLOCK);

        if (feet[0] != step.toX() || feet[1] != step.toY() || feet[2] != step.toZ()
                || distanceToDest > SETTLED) {
            if (ticks++ < APPROACH_TICKS && distanceFromStart < APPROACH_RANGE) {
                StepPreparation.walkTowards(context, tick,
                        step.toX() * 2 - step.fromX(), step.toY(), step.toZ() * 2 - step.fromZ());
            } else {
                StepPreparation.walkTowards(context, tick, step.toX(), step.toY(), step.toZ());
            }
        }
        return MoveState.RUNNING;
    }

    private boolean landed(StepContext context, PlanStep step) {
        int[] feet = context.feet();
        boolean atDest = feet[0] == step.toX() && feet[1] == step.toY() && feet[2] == step.toZ();
        boolean atFakeDest = feet[0] == step.toX() * 2 - step.fromX()
                && feet[1] == step.toY()
                && feet[2] == step.toZ() * 2 - step.fromZ();
        if (!atDest && !atFakeDest) {
            return false;
        }
        boolean inLiquid = LiquidRules.any(context.world().stateAt(step.toX(), step.toY(), step.toZ()));
        return inLiquid || context.player().getY() - step.toY() < SETTLED_HEIGHT;
    }

    private void creepDown(StepContext context, MoveTick tick, PlanStep step) {
        double targetX = (step.fromX() + 0.5D) * SOURCE_SHARE + (step.toX() + 0.5D) * TARGET_SHARE;
        double targetZ = (step.fromZ() + 0.5D) * SOURCE_SHARE + (step.toZ() + 0.5D) * TARGET_SHARE;
        var eyes = dev.helm.aim.BlockReach.eyePosition(context.player(), false);
        var aim = Aiming.lookFrom(eyes.x, eyes.y, eyes.z, targetX, step.toY(), targetZ)
                .withPitch(context.player().getXRot());
        tick.intent().aimedAt(aim, false);
        tick.press(Control.MOVE_FORWARD);
    }

    public void forceSafeLanding() {
        safeLanding = true;
    }

    public boolean isSafeLanding() {
        return safeLanding;
    }

    public void clearSafeLanding() {
        safeLanding = false;
        ticks = 0;
    }
}
