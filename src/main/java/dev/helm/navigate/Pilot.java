package dev.helm.navigate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

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

    private static final int ANCHOR_COOLDOWN = 10;

    private final ControlState controls = new ControlState();
    private final BlockBreaker breaker = new BlockBreaker();
    private final BlockPlacer placer = new BlockPlacer();
    private final RouteWalker walker;
    private final LookController look = LookController.instance();

    private Route route = Route.empty();
    private MoveTick tick = new MoveTick(MoveState.PREPPING);
    private StepContext context;
    private SearchJob pending;
    private Objective objective;
    private boolean pendingAnnouncement;
    private boolean active;
    private boolean held;
    private boolean outOfReach;
    private Objective anchor;
    private int anchorCooldown;

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

    public void await(SearchJob job, Objective what) {
        this.pending = job;
        this.objective = what;
        this.outOfReach = false;
        this.pendingAnnouncement = true;
    }

    public boolean searching() {
        return pending != null;
    }

    public boolean unreachable() {
        return outOfReach;
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

    public void holdStill() {
        this.held = true;
    }

    public void forgetObjective() {
        this.objective = null;
        this.anchor = null;
        this.anchorCooldown = 0;
    }

    public void anchorAt(Objective what) {
        this.anchor = what;
        this.anchorCooldown = 0;
    }

    public boolean anchored() {
        return anchor != null;
    }

    public void tick() {
        reportSearchProgress();
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

        boolean standing = held;
        held = false;
        if (standing) {
            return;
        }
        if (!active) {
            holdAnchor();
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

    private void holdAnchor() {
        Objective what = anchor;
        if (what == null || pending != null || context == null) {
            return;
        }
        if (anchorCooldown > 0) {
            anchorCooldown--;
            return;
        }
        var player = Minecraft.getInstance().player;
        if (player == null || satisfiedBy(what, player)) {
            return;
        }
        var feet = player.blockPosition();
        Trace.instance().event("goto", "anchored on " + what.label()
                + " but the player is at " + feet.getX() + " " + feet.getY() + " "
                + feet.getZ() + ", going back");
        anchorCooldown = ANCHOR_COOLDOWN;
        replan(what);
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

    private void reportSearchProgress() {
        if (pending == null) {
            return;
        }
        var search = pending.search();
        Trace.instance().pulse("search-progress", "search", "still searching: "
                + search.visited() + " nodes taken, " + search.waiting() + " waiting, "
                + search.stored() + " stored, " + pending.millis() + "ms so far"
                + whereAmI());
    }

    private void collect() {
        SearchJob job = pending;
        if (job == null || !job.done()) {
            return;
        }
        pending = null;
        boolean announce = pendingAnnouncement;
        pendingAnnouncement = false;
        Objective target = objective;
        if (target == null) {
            return;
        }
        NavigatorAgent agent = NavigatorAgent.instance();
        Journey.Result result = Journey.collect(job.search(), agent.navigator().blocks(),
                agent.navigator().walk());
        Trace.instance().event("goto", "search for " + target.label() + " finished after "
                + job.millis() + "ms, outcome " + result.outcome() + ", "
                + result.visited() + " nodes seen, " + result.route().length() + " steps usable"
                + whereAmI());
        if (result.arrived()) {
            Trace.instance().event("goto", "already standing on the goal " + target.label());
            halt();
            ClientNotice.warn("Already at " + target.label() + ".");
            return;
        }
        if (!result.usable()) {
            outOfReach = true;
            Trace.instance().event("goto", "nothing usable came back, giving up on "
                    + target.label());
            ClientNotice.warn("No path to " + target.label() + ".");
            return;
        }
        RouteTrace.describe(result.route());
        travel(result.route());
        Trace.instance().event("walk", "walking " + result.route().length() + " steps to "
                + target.label() + whereAmI());
        if (announce) {
            ClientNotice.warn((result.reached() ? "Path found: " : "Partial path: ")
                    + result.route().length() + " steps.");
        }
    }

    private void finish(WalkOutcome outcome) {
        halt();
        Objective target = objective;
        Trace.instance().event("walk", "route " + (outcome == WalkOutcome.ABANDONED
                ? "abandoned" : "finished") + " after all its steps, wanted "
                + (target == null ? "nothing" : target.label()) + whereAmI());
        if (target == null) {
            Trace.instance().event("walk", "no goal recorded, so nothing more is searched");
            return;
        }
        if (satisfiedBy(target, Minecraft.getInstance().player)) {
            Trace.instance().event("walk", "arrived at " + target.label());
            ClientNotice.warn("Arrived at " + target.label() + ".");
            return;
        }
        Trace.instance().event("walk", "still short of " + target.label()
                + ", searching again from where the player actually is");
        replan(target);
    }

    private String whereAmI() {
        var player = Minecraft.getInstance().player;
        return player == null ? ""
                : ", player at " + dev.helm.diag.PlayerReport.everything(player);
    }

    private void replan(Objective target) {
        NavigatorAgent agent = NavigatorAgent.instance();
        if (!agent.navigator().ready()) {
            Trace.instance().event("walk", "navigator is not ready, cannot search again");
            return;
        }
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        var feet = player.blockPosition();
        Trace.instance().event("goto", "searching again for " + target.label()
                + " from " + feet.getX() + " " + feet.getY() + " " + feet.getZ());
        SearchJob job = agent.navigator()
                .searchFor(target.goal(), feet.getX(), feet.getY(), feet.getZ());
        if (job == null) {
            Trace.instance().event("goto", "the search could not be started");
            return;
        }
        await(job, target);
        pendingAnnouncement = false;
    }

    private static boolean satisfiedBy(Objective what, LocalPlayer player) {
        if (player == null) {
            return false;
        }
        var feet = player.blockPosition();
        return what.satisfiedBy(feet.getX(), feet.getY(), feet.getZ());
    }
}
