package dev.helm.movement.step;

import dev.helm.aim.BlockReach;
import dev.helm.control.Control;
import dev.helm.movement.MoveState;
import dev.helm.movement.MoveTick;
import dev.helm.pathfinding.world.block.BlockShapes;

public final class StepUpExecutor implements StepExecutor {

    private static final int GIVE_UP_PLACING_AFTER = 10;
    private static final double LATERAL_TOLERANCE = 0.2D;
    private static final double FLAT_REACH = 1.2D;
    private static final double LATERAL_MOTION = 0.1D;

    private int ticksWithoutPlacement;

    @Override
    public void begin() {
        ticksWithoutPlacement = 0;
    }

    @Override
    public boolean safeToCancel(StepContext context, PlanStep step, MoveTick tick) {
        return tick.state() != MoveState.RUNNING || ticksWithoutPlacement == 0;
    }

    @Override
    public MoveState advance(StepContext context, MoveTick tick, PlanStep step) {
        int[] feet = context.feet();
        if (feet[1] < step.fromY()) {
            return MoveState.UNREACHABLE;
        }
        if (!StepPreparation.ready(context, tick, step)) {
            return MoveState.PREPPING;
        }
        StepPreparation.advanceStatus(tick);
        if (tick.state() != MoveState.RUNNING) {
            return tick.state();
        }
        if (atDest(context, step)) {
            return MoveState.SUCCESS;
        }
        int[] landing = step.placeAt() == null
                ? new int[]{step.toX(), step.toY() - 1, step.toZ()}
                : step.placeAt();
        if (!context.walk().onTop(landing[0], landing[1], landing[2])) {
            ticksWithoutPlacement++;
            if (BlockPlacer.attempt(context, tick, step.toX(), step.toY() - 1, step.toZ(),
                    false, true) == Placement.READY) {
                tick.press(Control.SNEAK);
                if (context.player().isCrouching()) {
                    tick.press(Control.USE);
                }
            }
            if (ticksWithoutPlacement > GIVE_UP_PLACING_AFTER) {
                tick.press(Control.MOVE_BACK);
            }
            return MoveState.RUNNING;
        }
        StepPreparation.walkTowards(context, tick, step.toX(), step.toY(), step.toZ());
        tick.set(Control.SNEAK, context.movement().magmaWalkAllowed()
                && context.world().stateAt(landing[0], landing[1], landing[2]).getBlock()
                        == net.minecraft.world.level.block.Blocks.MAGMA_BLOCK);
        if (BlockShapes.bottomSlab(context.world().stateAt(landing[0], landing[1], landing[2]))
                && !BlockShapes.bottomSlab(context.world().stateAt(
                        step.fromX(), step.fromY() - 1, step.fromZ()))) {
            return MoveState.RUNNING;
        }
        if (context.movement().assumeStep() || onTopOfSource(context, step)) {
            return MoveState.RUNNING;
        }
        int xAxis = Math.abs(step.fromX() - step.toX());
        int zAxis = Math.abs(step.fromZ() - step.toZ());
        double flatToNext = xAxis * Math.abs((step.toX() + 0.5D) - context.player().getX())
                + zAxis * Math.abs((step.toZ() + 0.5D) - context.player().getZ());
        double sideDist = zAxis * Math.abs((step.toX() + 0.5D) - context.player().getX())
                + xAxis * Math.abs((step.toZ() + 0.5D) - context.player().getZ());
        double lateral = xAxis * context.player().getDeltaMovement().z
                + zAxis * context.player().getDeltaMovement().x;
        if (Math.abs(lateral) > LATERAL_MOTION) {
            return MoveState.RUNNING;
        }
        if (headroom(context, step)) {
            tick.press(Control.JUMP);
            return MoveState.RUNNING;
        }
        if (flatToNext > FLAT_REACH || sideDist > LATERAL_TOLERANCE) {
            return MoveState.RUNNING;
        }
        tick.press(Control.JUMP);
        return MoveState.RUNNING;
    }

    private boolean atDest(StepContext context, PlanStep step) {
        int[] feet = context.feet();
        if (feet[0] == step.toX() && feet[1] == step.toY() && feet[2] == step.toZ()) {
            return true;
        }
        return feet[0] == step.toX() + step.directionX()
                && feet[1] == step.toY() - 1
                && feet[2] == step.toZ() + step.directionZ();
    }

    private boolean onTopOfSource(StepContext context, PlanStep step) {
        int[] feet = context.feet();
        return feet[0] == step.fromX() && feet[1] == step.fromY() + 1 && feet[2] == step.fromZ();
    }

    private boolean headroom(StepContext context, PlanStep step) {
        return context.walk().fullyPassable(step.fromX(), step.fromY() + 2, step.fromZ())
                && context.walk().fullyPassable(step.fromX() + step.directionX(), step.fromY() + 1,
                        step.fromZ() + step.directionZ());
    }
}
