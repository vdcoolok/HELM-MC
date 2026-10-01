package dev.helm.navigate;

import net.minecraft.client.Minecraft;

import dev.helm.aim.LookController;
import dev.helm.control.Control;
import dev.helm.control.ControlState;
import dev.helm.diag.RouteTrace;
import dev.helm.diag.Trace;
import dev.helm.interaction.BlockBreaker;
import dev.helm.interaction.BlockPlacer;
import dev.helm.movement.MoveState;
import dev.helm.movement.MoveTick;
import dev.helm.movement.Route;
import dev.helm.movement.RouteWalker;
import dev.helm.movement.WalkOutcome;
import dev.helm.movement.step.StepContext;
import dev.helm.pathfinding.search.SearchJob;
import dev.helm.setting.ClientNotice;

public final class Pilot {

    private final ControlState controls = new ControlState();
    private final BlockBreaker breaker = new BlockBreaker();
    private final BlockPlacer placer = new BlockPlacer();
    private final RouteWalker walker;
    private final LookController look = LookController.instance();

    private Route route = Route.empty();
    private MoveTick tick = new MoveTick(MoveState.PREPPING);
    private StepContext context;
    private SearchJob pending;
    private Destination destination;
    private boolean pendingAnnouncement;
    private boolean active;

    public Pilot(RouteWalker walker) {
        this.walker = walker;
    }

    public ControlState controls() {
        return controls;
    }

    public Route route() {
        return route;
    }

    public int stepIndex() {
        return walker.index();
    }

    public boolean active() {
        return active;
    }

    public boolean isWalking() {
        return active && !route.steps().isEmpty();
    }

    public void setContext(StepContext stepContext) {
        this.context = stepContext;
        walker.bind(stepContext);
    }

    public void travel(Route next) {
        this.route = next;
        this.active = true;
        this.tick = new MoveTick(MoveState.PREPPING);
        walker.begin(next.length());
    }

    public void await(SearchJob job, Destination where) {
        this.pending = job;
        this.destination = where;
        this.pendingAnnouncement = true;
    }

    public boolean searching() {
        return pending != null;
    }

    public void halt() {
        this.active = false;
        this.pending = null;
        this.route = Route.empty();
        this.tick = new MoveTick(MoveState.PREPPING);
        controls.clear();
        breaker.stop();
        look.clear();
    }

    public void forgetDestination() {
        this.destination = null;
    }

    public void tick() {
        collect();
        if (context == null) {
            return;
        }
        boolean wantsBreak = controls.isDown(Control.ATTACK);
        boolean wantsPlace = controls.isDown(Control.USE);
        release();

        breaker.tick(wantsBreak);
        placer.tick(wantsPlace);
        look.tick();

        if (!active) {
            return;
        }
        tick.reset();
        WalkOutcome outcome = walker.tick(route, tick, context);
        if (walker.releasedControls() || outcome != WalkOutcome.CONTINUE) {
            controls.clear();
        }
        tick.inputs().drain().forEach(controls::set);
        if (walker.sprinting()) {
            controls.set(Control.SPRINT, true);
        }
        if (outcome == WalkOutcome.DONE || walker.failed()) {
            finish(outcome);
        }
    }

    private void release() {
        controls.set(Control.ATTACK, false);
        controls.set(Control.USE, false);
        controls.set(Control.SPRINT, false);
        controls.set(Control.MOVE_FORWARD, false);
        controls.set(Control.MOVE_BACK, false);
        controls.set(Control.MOVE_LEFT, false);
        controls.set(Control.MOVE_RIGHT, false);
        controls.set(Control.JUMP, false);
        controls.set(Control.SNEAK, false);
    }

    private void collect() {
        SearchJob job = pending;
        if (job == null || !job.done()) {
            return;
        }
        pending = null;
        boolean announce = pendingAnnouncement;
        pendingAnnouncement = false;
        Destination target = destination;
        if (target == null) {
            return;
        }
        NavigatorAgent agent = NavigatorAgent.instance();
        Journey.Result result = Journey.collect(job.search(), agent.navigator().blocks(),
                agent.navigator().walk());
        Trace.instance().event("goto", "search finished after " + job.millis() + "ms");
        if (result.arrived()) {
            Trace.instance().event("goto", "already standing on the goal");
            halt();
            ClientNotice.warn("Already at " + target.describe() + ".");
            return;
        }
        if (!result.usable()) {
            Trace.instance().event("goto", "unusable result, nothing drawn");
            ClientNotice.warn("No path to " + target.describe() + ".");
            return;
        }
        RouteTrace.describe(result.route());
        travel(result.route());
        if (announce) {
            ClientNotice.warn((result.reached() ? "Path found: " : "Partial path: ")
                    + result.route().length() + " steps.");
        }
    }

    private void finish(WalkOutcome outcome) {
        halt();
        Destination target = destination;
        if (target == null) {
            return;
        }
        if (standingOn(target)) {
            Trace.instance().event("walk", "arrived at " + target.describe());
            ClientNotice.warn("Arrived at " + target.describe() + ".");
            return;
        }
        Trace.instance().event("walk", "route " + (outcome == WalkOutcome.ABANDONED
                ? "abandoned" : "finished") + " short of " + target.describe()
                + ", searching again from here");
        replan(target);
    }

    private void replan(Destination target) {
        NavigatorAgent agent = NavigatorAgent.instance();
        if (!agent.navigator().ready()) {
            return;
        }
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        var feet = player.blockPosition();
        SearchJob job = agent.navigator()
                .searchFor(target.goal(), feet.getX(), feet.getY(), feet.getZ());
        if (job != null) {
            await(job, target);
            pendingAnnouncement = false;
        }
    }

    private boolean standingOn(Destination target) {
        var player = Minecraft.getInstance().player;
        return player != null && target.reachedBy(player.blockPosition());
    }
}
