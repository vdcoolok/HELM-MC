package dev.helm.movement;

import dev.helm.control.Control;
import dev.helm.movement.step.PlanStep;
import dev.helm.movement.step.StepContext;
import dev.helm.movement.step.StepKind;
import dev.helm.movement.step.StepRunners;
import dev.helm.pathfinding.world.block.Hazards;
import dev.helm.setting.MovementSettings;

public final class RouteWalker {

    private static final double OFF_PATH_DISTANCE = 2.0D;
    private static final double WAY_OFF_DISTANCE = 3.0D;
    private static final int MAX_TICKS_OFF_PATH = 200;
    private static final int SKIP_LOOKAHEAD = 3;
    private static final int RISE_SCAN = 3;
    private static final int WIDTH_SCAN = 2;

    private final StepRunners runners = new StepRunners();
    private final StepPricer pricer;
    private final MovementSettings settings;

    private int index;
    private int ticksAway;
    private int ticksOnStep;
    private double originalCost;
    private boolean costRecorded;
    private boolean failed;
    private boolean sprinting;
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
        ticksAway = 0;
        ticksOnStep = 0;
        costRecorded = false;
        failed = false;
        sprinting = false;
        releasedControls = false;
    }

    public void restart() {
        ticksOnStep = 0;
        releasedControls = true;
    }

    public WalkOutcome tick(Route route, MoveTick state, StepContext stepContext) {
        context = stepContext;
        if (index >= route.length()) {
            return WalkOutcome.DONE;
        }
        PlanStep step = route.at(index);
        int[] feet = stepContext.feet();
        current = step;

        int rewound = rewindTo(route, feet);
        if (rewound >= 0) {
            index = rewound;
            restart();
            return WalkOutcome.CONTINUE;
        }
        int skipped = skipTo(route, feet);
        if (skipped >= 0) {
            index = skipped - 1;
            restart();
            return WalkOutcome.CONTINUE;
        }
        if (leftBehind(route, stepContext)) {
            return WalkOutcome.ABANDONED;
        }
        if (index < route.length() - 1) {
            PlanStep next = route.at(index + 1);
            if (!stepContext.world().loaded(next.toX(), next.toZ())) {
                state.inputs().clear();
                return WalkOutcome.PAUSED;
            }
        }

        if (!costRecorded) {
            originalCost = pricer.reprice(step);
            costRecorded = true;
            if (impossibleAhead(route)) {
                abandon();
                return WalkOutcome.ABANDONED;
            }
        }
        double current = pricer.reprice(step);
        if (current >= dev.helm.pathfinding.cost.MoveCosts.IMPOSSIBLE) {
            abandon();
            return WalkOutcome.ABANDONED;
        }
        if (current - originalCost > settings.maxCostIncrease()) {
            abandon();
            return WalkOutcome.ABANDONED;
        }

        MoveState outcome = runners.advance(stepContext, state, step);
        if (outcome == MoveState.UNREACHABLE || outcome == MoveState.FAILED) {
            abandon();
            return WalkOutcome.ABANDONED;
        }
        if (outcome == MoveState.SUCCESS) {
            index++;
            restart();
            return WalkOutcome.CONTINUE;
        }
        aimWith(state);
        overrideAbilities(stepContext, state);

        sprinting = sprintNextTick(route, state);
        if (!sprinting && stepContext.player() != null) {
            stepContext.player().setSprinting(false);
        }
        ticksOnStep++;
        if (ticksOnStep > originalCost + settings.movementTimeoutTicks()) {
            abandon();
            return WalkOutcome.ABANDONED;
        }
        return WalkOutcome.CONTINUE;
    }

    private int rewindTo(Route route, int[] feet) {
        for (int earlier = 0; earlier < index && earlier < route.length(); earlier++) {
            PlanStep step = route.at(earlier);
            if (step.fromX() == feet[0] && step.fromY() == feet[1] && step.fromZ() == feet[2]) {
                return earlier;
            }
        }
        return -1;
    }

    private int skipTo(Route route, int[] feet) {
        for (int later = index + SKIP_LOOKAHEAD; later < route.length() - 1; later++) {
            PlanStep step = route.at(later);
            if (step.fromX() == feet[0] && step.fromY() == feet[1] && step.fromZ() == feet[2]) {
                return later;
            }
        }
        return -1;
    }

    private boolean leftBehind(Route route, StepContext stepContext) {
        double nearest = nearestOnRoute(route, stepContext);
        if (nearest > OFF_PATH_DISTANCE) {
            ticksAway++;
            if (ticksAway > MAX_TICKS_OFF_PATH) {
                abandon();
                return true;
            }
        } else {
            ticksAway = 0;
        }
        if (nearest > WAY_OFF_DISTANCE) {
            abandon();
            return true;
        }
        return false;
    }

    private double nearestOnRoute(Route route, StepContext stepContext) {
        if (stepContext.player() == null) {
            return 0.0D;
        }
        double best = Double.MAX_VALUE;
        for (PlanStep step : route.steps()) {
            best = Math.min(best, centreDistance(stepContext, step.toX(), step.toY(), step.toZ()));
            best = Math.min(best, centreDistance(stepContext, step.fromX(), step.fromY(), step.fromZ()));
        }
        return best;
    }

    private double centreDistance(StepContext stepContext, int x, int y, int z) {
        double dx = stepContext.player().getX() - (x + 0.5D);
        double dy = stepContext.player().getY() - (y + 0.5D);
        double dz = stepContext.player().getZ() - (z + 0.5D);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private boolean impossibleAhead(Route route) {
        int limit = Math.min(settings.costVerificationLookahead(), route.length() - index - 1);
        for (int ahead = 1; ahead < limit; ahead++) {
            if (pricer.impossible(route.at(index + ahead))) {
                return true;
            }
        }
        return false;
    }

    private void abandon() {
        failed = true;
        index = Integer.MAX_VALUE;
        releasedControls = true;
    }

    private void overrideAbilities(StepContext stepContext, MoveTick state) {
        if (stepContext.player() == null) {
            return;
        }
        stepContext.player().getAbilities().flying = false;
        PlanStep step = current;
        if (step == null) {
            return;
        }
        int[] feet = stepContext.feet();
        if (dev.helm.pathfinding.world.block.LiquidRules.any(
                stepContext.world().stateAt(feet[0], feet[1], feet[2]))
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

    private PlanStep current;

    private void aimWith(MoveTick state) {
        if (!state.intent().hasAim()) {
            dev.helm.aim.LookController.instance().clear();
            return;
        }
        dev.helm.aim.LookController.instance()
                .aimAt(state.intent().aim(), state.intent().forcesAim());
    }

    public void abort() {
        abandon();
    }

    private boolean sprintNextTick(Route route, MoveTick state) {
        boolean requested = state.inputs().isSet(Control.SPRINT);
        state.set(Control.SPRINT, false);
        if (!settings.sprintAllowed() || index >= route.length()) {
            return false;
        }
        if (requested) {
            return true;
        }
        PlanStep current = route.at(index);
        if (current.kind() == StepKind.DROP) {
            if (sprintOffDrop(route, current)) {
                return true;
            }
        }
        if (current.kind() == StepKind.STEP_UP && index > 0) {
            PlanStep previous = route.at(index - 1);
            if (previous.kind() == StepKind.DROP && continuesForward(previous, current)) {
                return true;
            }
            if (index < route.length() - 2 && previous.kind() == StepKind.STEP
                    && sprintableStepUp(previous, current, route.at(index + 1))) {
                return true;
            }
        }
        return false;
    }

    private boolean sprintOffDrop(Route route, PlanStep current) {
        if (index + 1 >= route.length()) {
            return false;
        }
        PlanStep next = route.at(index + 1);
        if (next.kind() == StepKind.STEP_UP && continuesForward(current, next)) {
            index++;
            restart();
            return true;
        }
        if (next.kind() == StepKind.DROP && continuesForward(current, next)) {
            return true;
        }
        if (next.kind() == StepKind.LEAN && settings.overshootDiagonalDescend()) {
            return true;
        }
        if (frosteableLanding(next) && frostBlocked(current)) {
            return false;
        }
        if (!context.walk().onTop(current.toX() + current.directionX(), current.toY(),
                current.toZ() + current.directionZ())) {
            return false;
        }
        return next.kind() == StepKind.STEP && continuesForward(current, next);
    }

    private boolean frosteableLanding(PlanStep next) {
        return next.kind() == StepKind.STEP || next.kind() == StepKind.BOUND;
    }

    private boolean frostBlocked(PlanStep current) {
        if (context.player() == null || !FrostWalker.wornBy(context.player())) {
            return false;
        }
        return true;
    }

    private boolean continuesForward(PlanStep first, PlanStep second) {
        return first.directionX() == second.directionX()
                && first.directionZ() == second.directionZ();
    }

    private boolean sprintableStepUp(PlanStep previous, PlanStep next, PlanStep after) {
        if (!settings.sprintAscends()) {
            return false;
        }
        if (previous.directionX() != next.directionX()
                || previous.directionZ() != next.directionZ()) {
            return false;
        }
        if (after.directionX() != next.directionX() || after.directionZ() != next.directionZ()) {
            return false;
        }
        if (!context.walk().onTop(previous.toX(), previous.toY() - 1, previous.toZ())) {
            return false;
        }
        if (!context.walk().onTop(next.toX(), next.toY() - 1, next.toZ())) {
            return false;
        }
        if (!next.blocksToBreak().isEmpty()) {
            return false;
        }
        for (int across = 0; across < WIDTH_SCAN; across++) {
            for (int up = 0; up < RISE_SCAN; up++) {
                int x = previous.fromX() + (across == 1 ? previous.directionX() : 0);
                int y = previous.fromY() + up;
                int z = previous.fromZ() + (across == 1 ? previous.directionZ() : 0);
                if (!context.walk().fullyPassable(x, y, z)) {
                    return false;
                }
            }
        }
        if (Hazards.avoidWalkingInto(
                context.world().stateAt(previous.fromX(), previous.fromY() + RISE_SCAN,
                        previous.fromZ()), settings.magmaWalkAllowed())) {
            return false;
        }
        return !Hazards.avoidWalkingInto(
                context.world().stateAt(next.toX(), next.toY() + 2, next.toZ()),
                settings.magmaWalkAllowed());
    }
}
