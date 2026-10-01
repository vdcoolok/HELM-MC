package dev.helm.movement.step;

import dev.helm.movement.MoveState;
import dev.helm.movement.MoveTick;

public final class SinkStepExecutor implements StepExecutor {

    private static final int PATIENCE = 10;
    private static final double PATIENCE_RANGE = 0.2D;

    private int ticks;

    @Override
    public void begin() {
        ticks = 0;
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
        if (feet[0] == step.toX() && feet[1] == step.toY() && feet[2] == step.toZ()) {
            return MoveState.SUCCESS;
        }
        if (feet[0] != step.fromX() || feet[1] != step.fromY() || feet[2] != step.fromZ()) {
            return MoveState.UNREACHABLE;
        }
        double offsetX = context.player().getX() - (step.toX() + 0.5D);
        double offsetZ = context.player().getZ() - (step.toZ() + 0.5D);
        double distance = Math.sqrt(offsetX * offsetX + offsetZ * offsetZ);
        if (ticks++ < PATIENCE && distance < PATIENCE_RANGE) {
            return MoveState.RUNNING;
        }
        int[] target = step.blocksToBreak().isEmpty()
                ? new int[]{step.toX(), step.toY(), step.toZ()}
                : step.blocksToBreak().get(0);
        StepPreparation.walkTowards(context, tick, target[0], target[1], target[2]);
        return MoveState.RUNNING;
    }
}
