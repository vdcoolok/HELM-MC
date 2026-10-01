package dev.helm.movement.step;

import dev.helm.movement.MoveState;
import dev.helm.movement.MoveTick;

public final class StepRunners {

    private final FlatStepExecutor flat = new FlatStepExecutor();
    private final StepUpExecutor stepUp = new StepUpExecutor();
    private final DropStepExecutor drop = new DropStepExecutor();
    private final LeanStepExecutor lean = new LeanStepExecutor();
    private final RaiseStepExecutor raise = new RaiseStepExecutor();
    private final SinkStepExecutor sink = new SinkStepExecutor();
    private final BoundStepExecutor bound = new BoundStepExecutor();
    private final FallStepExecutor fall = new FallStepExecutor();

    public MoveState advance(StepContext context, MoveTick tick, PlanStep step) {
        return runnerFor(step).advance(context, tick, step);
    }

    public void begin(PlanStep step) {
        runnerFor(step).begin();
    }

    public DropStepExecutor drop() {
        return drop;
    }

    private StepExecutor runnerFor(PlanStep step) {
        return switch (step.kind()) {
            case STEP -> flat;
            case STEP_UP -> stepUp;
            case DROP -> drop;
            case FALL -> fall;
            case LEAN -> lean;
            case RAISE -> raise;
            case SINK -> sink;
            case BOUND -> bound;
        };
    }
}
