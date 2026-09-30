package dev.helm.movement.step;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

import dev.helm.control.Control;
import dev.helm.movement.MoveState;
import dev.helm.movement.MoveTick;
import dev.helm.pathfinding.world.block.Climbable;

public final class BoundStepExecutor implements StepExecutor {

    private static final double LILY_PAD_TOLERANCE = 0.094D;
    private static final double BACK_UP_RANGE = 0.7D;
    private static final double AIRBORNE = 0.0001D;
    private static final int LONG_JUMP = 3;

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
        if (feet[1] < step.fromY()) {
            return MoveState.UNREACHABLE;
        }
        int reach = Math.abs(step.toX() - step.fromX()) + Math.abs(step.toZ() - step.fromZ());
        boolean climbed = step.toY() > step.fromY();
        if (reach >= 4 || climbed) {
            tick.press(Control.SPRINT);
        }
        if (context.movement().magmaWalkAllowed()
                && context.world().stateAt(feet[0], feet[1] - 1, feet[2]).getBlock()
                        == Blocks.MAGMA_BLOCK) {
            tick.press(Control.SNEAK);
        }
        StepPreparation.walkTowards(context, tick, step.toX(), step.toY(), step.toZ());

        boolean atDest = feet[0] == step.toX() && feet[1] == step.toY() && feet[2] == step.toZ();
        if (atDest) {
            BlockPos landed = new BlockPos(step.toX(), step.toY(), step.toZ());
            if (Climbable.is(context.world().stateAt(step.toX(), step.toY(), step.toZ()).getBlock())) {
                return MoveState.SUCCESS;
            }
            if (context.player().getY() - step.toY() < LILY_PAD_TOLERANCE) {
                return MoveState.SUCCESS;
            }
            return MoveState.RUNNING;
        }
        if (atSource(context, step)) {
            return MoveState.RUNNING;
        }

        boolean launched = feet[0] == step.fromX() + step.directionX()
                && feet[1] == step.fromY()
                && feet[2] == step.fromZ() + step.directionZ();
        boolean airborne = context.player().getY() - step.fromY() > AIRBORNE;
        if (launched || airborne) {
            if (context.movement().allowPlace()
                    && !context.walk().onTop(step.toX(), step.toY() - 1, step.toZ())
                    && !context.player().onGround()
                    && BlockPlacer.attempt(context, tick, step.toX(), step.toY() - 1, step.toZ(),
                            true, false) == Placement.READY) {
                tick.press(Control.USE);
            }
            if (reach == LONG_JUMP && !climbed && started(context, step)) {
                return MoveState.RUNNING;
            }
            tick.press(Control.JUMP);
            return MoveState.RUNNING;
        }
        if (!atBehind(context, step)) {
            tick.set(Control.SPRINT, false);
            if (atStartBehind(context, step)) {
                StepPreparation.walkTowards(context, tick, step.fromX(), step.fromY(), step.fromZ());
            } else {
                StepPreparation.walkTowards(context, tick,
                        step.fromX() - step.directionX(), step.fromY(),
                        step.fromZ() - step.directionZ());
            }
        }
        return MoveState.RUNNING;
    }

    private boolean atSource(StepContext context, PlanStep step) {
        int[] feet = context.feet();
        return feet[0] == step.fromX() && feet[1] == step.fromY() && feet[2] == step.fromZ();
    }

    private boolean atBehind(StepContext context, PlanStep step) {
        int[] feet = context.feet();
        return feet[0] == step.toX() - step.directionX()
                && feet[1] == step.toY()
                && feet[2] == step.toZ() - step.directionZ();
    }

    private boolean atStartBehind(StepContext context, PlanStep step) {
        int[] feet = context.feet();
        return feet[0] == step.fromX() - step.directionX()
                && feet[1] == step.fromY()
                && feet[2] == step.fromZ() - step.directionZ();
    }

    private boolean started(StepContext context, PlanStep step) {
        double xGap = (step.fromX() + 0.5D) - context.player().getX();
        double zGap = (step.fromZ() + 0.5D) - context.player().getZ();
        return Math.max(Math.abs(xGap), Math.abs(zGap)) < BACK_UP_RANGE;
    }
}
