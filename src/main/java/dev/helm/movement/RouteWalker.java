package dev.helm.movement;

import dev.helm.control.Control;
import dev.helm.diag.WalkTrace;
import dev.helm.movement.step.PlanStep;
import dev.helm.movement.step.StepContext;
import dev.helm.movement.step.StepRunners;
import dev.helm.movement.sprint.FallSprint;
import dev.helm.movement.sprint.SprintChoice;
import dev.helm.movement.sprint.SprintPlanner;
import dev.helm.movement.sprint.SprintStamina;
import dev.helm.pathfinding.world.block.LiquidRules;
import dev.helm.setting.MovementSettings;

public final class RouteWalker {

    private static final double OFF_PATH_DISTANCE = 2.0D;
    private static final double WAY_OFF_DISTANCE = 3.0D;
    private static final int MAX_TICKS_OFF_PATH = 200;

    private final StepRunners runners = new StepRunners();
    private final StepPricer pricer;
    private final MovementSettings settings;

    private int index;
    private int enteredStep = -1;
    private int ticksAway;
    private int ticksOnStep;
    private StepBudget budget;
    private boolean failed;
    private boolean sprinting;
    private boolean moved;
    private boolean releasedControls;
    private StepContext context;

    public RouteWalker(StepPricer pricer, MovementSettings settings) {
        this.pricer = pricer;
        this.settings = settings;
    }

    public void bind(StepContext stepContext) {
        this.context = stepContext;
    }

    public int index() {
        return index;
    }

    public boolean failed() {
        return failed;
    }

    public boolean sprinting() {
        return sprinting;
    }

    public boolean releasedControls() {
        boolean released = releasedControls;
        releasedControls = false;
        return released;
    }

    public void begin(int length) {
        index = 0;
        enteredStep = -1;
        ticksAway = 0;
        ticksOnStep = 0;
        budget = null;
        failed = false;
        sprinting = false;
        releasedControls = false;
    }

    public void restart(MoveTick state) {
        ticksOnStep = 0;
        budget = null;
        releasedControls = true;
        state.state(MoveState.PREPPING);
        state.inputs().clear();
    }

    public WalkOutcome tick(Route route, MoveTick state, StepContext stepContext) {
        context = stepContext;
        int guard = route.length() + 2;
        while (guard > 0) {
            guard--;
            moved = false;
            WalkOutcome outcome = attempt(route, state, stepContext);
            if (!moved) {
                return outcome;
            }
        }
        return WalkOutcome.CONTINUE;
    }

    public void abort() {
        failed = true;
        index = Integer.MAX_VALUE;
        releasedControls = true;
    }

    private WalkOutcome attempt(Route route, MoveTick state, StepContext stepContext) {
        if (index >= route.length()) {
            WalkTrace.finished(route, index, stepContext.feet());
            return WalkOutcome.DONE;
        }
        PlanStep step = route.at(index);
        int[] feet = stepContext.feet();
        if (index != enteredStep) {
            enteredStep = index;
            runners.begin(step);
        }

        if (!RouteProximity.standingOn(step, feet)) {
            int rewound = RouteResync.earlier(route, index, feet[0], feet[1], feet[2]);
            if (rewound >= 0) {
                WalkTrace.rewind(route, index, rewound, feet);
                index = rewound;
                restart(state);
                moved = true;
                return WalkOutcome.CONTINUE;
            }
            int skipped = RouteResync.later(route, index, feet[0], feet[1], feet[2]);
            if (skipped >= 0) {
                WalkTrace.skip(route, index, skipped, feet);
                index = skipped - 1;
                restart(state);
                moved = true;
                return WalkOutcome.CONTINUE;
            }
        }
        double offRoute = RouteProximity.distanceFrom(route, stepContext);
        if (leftBehind(route, offRoute, step, stepContext, feet)) {
            return WalkOutcome.ABANDONED;
        }
        if (index < route.length() - 1) {
            PlanStep next = route.at(index + 1);
            if (!stepContext.world().loaded(next.toX(), next.toZ())) {
                WalkTrace.paused(route, index, new int[]{next.toX(), next.toY(), next.toZ()});
                state.inputs().clear();
                sprinting = false;
                return WalkOutcome.PAUSED;
            }
        }

        if (budget == null) {
            budget = new StepBudget(pricer, settings, index, step);
        }
        budget.refresh();

        MoveState outcome = runners.advance(stepContext, state, step);
        boolean cancellable = runners.safeToCancel(stepContext, step, state);
        if (outcome == MoveState.UNREACHABLE || outcome == MoveState.FAILED) {
            abandon(route, step, feet, "step executor returned " + outcome);
            return WalkOutcome.ABANDONED;
        }
        if (cancellable && budget.impossible()) {
            abandon(route, step, feet, "this step is no longer possible");
            return WalkOutcome.ABANDONED;
        }
        if (cancellable && budget.roseTooFar()) {
            abandon(route, step, feet, "cost rose past the allowed increase");
            return WalkOutcome.ABANDONED;
        }
        if (cancellable && budget.anyAheadImpossible(route)) {
            abandon(route, step, feet, "a following step is no longer possible");
            return WalkOutcome.ABANDONED;
        }

        WalkTrace.step(route, index, ticksOnStep, budget.original(), budget.live(),
                stepContext, state, offRoute, toTarget(stepContext, step));
        if (outcome == MoveState.SUCCESS) {
            index++;
            restart(state);
            moved = true;
            return WalkOutcome.CONTINUE;
        }
        aimWith(state);
        overrideAbilities(stepContext, state, step);

        SprintChoice choice = decide(route, state);
        if (choice.skips()) {
            index = choice.skipTo();
            restart(state);
            moved = true;
            return WalkOutcome.CONTINUE;
        }
        FallSprint.Glide glide = FallSprint.extension(route, index, context);
        if (glide != null) {
            if (glide.landedOn(context)) {
                index = glide.landingIndex();
                restart(state);
                moved = true;
                return WalkOutcome.CONTINUE;
            }
            glide(context, state, glide);
            sprinting = true;
            return WalkOutcome.CONTINUE;
        }
        sprinting = choice.sprint();
        WalkTrace.sprinting(sprinting);
        if (!sprinting && stepContext.player() != null) {
            stepContext.player().setSprinting(false);
        }
        ticksOnStep++;
        if (ticksOnStep > budget.original() + settings.movementTimeoutTicks()) {
            abandon(route, step, feet, "ran out of ticks on this step");
            return WalkOutcome.ABANDONED;
        }
        return WalkOutcome.CONTINUE;
    }

    private boolean leftBehind(Route route, double offRoute, PlanStep step,
                              StepContext stepContext, int[] feet) {
        if (beyond(offRoute, step, stepContext, OFF_PATH_DISTANCE)) {
            ticksAway++;
            if (ticksAway > MAX_TICKS_OFF_PATH) {
                abandon(route, step, feet, "strayed from the route for " + ticksAway + " ticks");
                return true;
            }
        } else {
            ticksAway = 0;
        }
        if (beyond(offRoute, step, stepContext, WAY_OFF_DISTANCE)) {
            abandon(route, step, feet, "strayed " + Math.round(offRoute * 100.0D) / 100.0D
                    + " blocks from the route");
            return true;
        }
        return false;
    }

    private double toTarget(StepContext stepContext, PlanStep step) {
        if (stepContext.player() == null) {
            return Double.MAX_VALUE;
        }
        return step.footprint().nearestTo(stepContext.player().getX(),
                stepContext.player().getY(), stepContext.player().getZ());
    }

    private boolean beyond(double nearest, PlanStep step, StepContext stepContext, double leniency) {
        if (nearest <= leniency) {
            return false;
        }
        if (RouteProximity.falling(step)) {
            return RouteProximity.distanceFromFalling(step, stepContext) >= leniency;
        }
        return true;
    }

    private void abandon(Route route, PlanStep step, int[] feet, String reason) {
        if (!failed) {
            WalkTrace.abandon(index, route.length(), step, reason, feet);
        }
        failed = true;
        index = Integer.MAX_VALUE;
        releasedControls = true;
    }

    private void glide(StepContext stepContext, MoveTick state, FallSprint.Glide glide) {
        state.inputs().clear();
        state.intent().aimedAt(glide.aimFrom(stepContext), false);
        state.press(Control.MOVE_FORWARD);
    }

    private void aimWith(MoveTick state) {
        if (!state.intent().hasAim()) {
            dev.helm.aim.LookController.instance().clear();
            return;
        }
        dev.helm.aim.LookController.instance()
                .aimAt(state.intent().aim(), state.intent().forcesAim());
    }

    private void overrideAbilities(StepContext stepContext, MoveTick state, PlanStep step) {
        if (stepContext.player() == null) {
            return;
        }
        stepContext.player().getAbilities().flying = false;
        int[] feet = stepContext.feet();
        if (LiquidRules.any(stepContext.world().stateAt(feet[0], feet[1], feet[2]))
                && stepContext.player().getY() < step.toY() + 0.6D) {
            state.set(Control.JUMP, true);
        }
        if (stepContext.player().isInWall()) {
            for (int[] position : step.blocksToBreak()) {
                dev.helm.tools.ToolChooser.forBlock(new net.minecraft.core.BlockPos(
                        position[0], position[1], position[2]));
                break;
            }
            state.press(Control.ATTACK);
        }
    }

    private SprintChoice decide(Route route, MoveTick state) {
        boolean asked = state.inputs().isSet(Control.SPRINT);
        state.set(Control.SPRINT, false);
        if (index >= route.length() || !SprintStamina.available(context)) {
            return SprintChoice.hold();
        }
        SprintChoice choice = SprintPlanner.decide(route, index, context, asked);
        if (choice.safeDescend()) {
            runners.drop().forceSafeLanding();
        }
        if (choice.releaseJump()) {
            state.set(Control.JUMP, false);
        }
        return choice;
    }
}
