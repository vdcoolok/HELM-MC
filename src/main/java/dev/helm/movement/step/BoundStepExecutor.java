package dev.helm.movement.step;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

import dev.helm.control.Control;
import dev.helm.movement.MoveState;
import dev.helm.movement.MoveTick;
import dev.helm.pathfinding.world.block.Climbable;

public final class BoundStepExecutor implements StepExecutor {

    private static final double SETTLED_HEIGHT = 0.094D;
    private static final double BACK_UP_RANGE = 0.7D;
    private static final double LIFTED = 0.0001D;
    private static final int SPRINT_SPAN = 4;
    private static final int GUARDED_SPAN = 3;

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
        BoundSpan span = BoundSpan.of(step);
        if (span.sprints() || span.climbs()) {
            tick.press(Control.SPRINT);
        }
        if (onMagma(context, feet)) {
            tick.press(Control.SNEAK);
        }
        StepPreparation.walkTowards(context, tick, step.toX(), step.toY(), step.toZ());

        if (feet[0] == step.toX() && feet[1] == step.toY() && feet[2] == step.toZ()) {
            return landed(context, step);
        }
        if (feet[0] == step.fromX() && feet[1] == step.fromY() && feet[2] == step.fromZ()) {
            return MoveState.RUNNING;
        }
        if (hasLeft(context, step, feet)) {
            return takeOff(context, tick, step, span, feet);
        }
        if (feet[0] == step.toX() - step.directionX()
                && feet[1] == step.toY()
                && feet[2] == step.toZ() - step.directionZ()) {
            return MoveState.RUNNING;
        }
        return lineUp(context, tick, step, feet);
    }

    private static MoveState landed(StepContext context, PlanStep step) {
        if (Climbable.is(context.world().stateAt(step.toX(), step.toY(), step.toZ()).getBlock())) {
            return MoveState.SUCCESS;
        }
        return context.player().getY() - step.toY() < SETTLED_HEIGHT
                ? MoveState.SUCCESS
                : MoveState.RUNNING;
    }

    private static MoveState takeOff(StepContext context, MoveTick tick, PlanStep step,
                                     BoundSpan span, int[] feet) {
        if (allowLandingBlock(context, tick, step)) {
            tick.press(Control.USE);
        }
        if (span.guarded() && nearSource(context, step)) {
            return MoveState.RUNNING;
        }
        tick.press(Control.JUMP);
        return MoveState.RUNNING;
    }

    private static MoveState lineUp(StepContext context, MoveTick tick, PlanStep step,
                                    int[] feet) {
        tick.set(Control.SPRINT, false);
        boolean oneBehindSource = feet[0] == step.fromX() - step.directionX()
                && feet[1] == step.fromY()
                && feet[2] == step.fromZ() - step.directionZ();
        if (oneBehindSource) {
            StepPreparation.walkTowards(context, tick, step.fromX(), step.fromY(), step.fromZ());
        } else {
            StepPreparation.walkTowards(context, tick, step.fromX() - step.directionX(),
                    step.fromY(), step.fromZ() - step.directionZ());
        }
        return MoveState.RUNNING;
    }

    private static boolean hasLeft(StepContext context, PlanStep step, int[] feet) {
        boolean oneAlong = feet[0] == step.fromX() + step.directionX()
                && feet[1] == step.fromY()
                && feet[2] == step.fromZ() + step.directionZ();
        return oneAlong || context.player().getY() - step.fromY() > LIFTED;
    }

    private static boolean allowLandingBlock(StepContext context, MoveTick tick, PlanStep step) {
        return context.movement().allowPlace()
                && !context.walk().onTop(step.toX(), step.toY() - 1, step.toZ())
                && !context.player().onGround()
                && BlockPlacer.attempt(context, tick, step.toX(), step.toY() - 1, step.toZ(),
                        true, false) == Placement.READY;
    }

    private static boolean nearSource(StepContext context, PlanStep step) {
        double xGap = (step.fromX() + 0.5D) - context.player().getX();
        double zGap = (step.fromZ() + 0.5D) - context.player().getZ();
        return Math.max(Math.abs(xGap), Math.abs(zGap)) < BACK_UP_RANGE;
    }

    private static boolean onMagma(StepContext context, int[] feet) {
        return context.movement().magmaWalkAllowed()
                && context.world().stateAt(feet[0], feet[1] - 1, feet[2]).getBlock()
                        == Blocks.MAGMA_BLOCK;
    }

    private record BoundSpan(int blocks, boolean climbs) {

        static BoundSpan of(PlanStep step) {
            return new BoundSpan(
                    Math.abs(step.toX() - step.fromX()) + Math.abs(step.toZ() - step.fromZ()),
                    step.toY() > step.fromY());
        }

        boolean sprints() {
            return blocks >= SPRINT_SPAN;
        }

        boolean guarded() {
            return blocks == GUARDED_SPAN;
        }
    }
}