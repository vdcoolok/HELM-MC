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
    private Route ahead = Route.empty();
    private MoveTick tick = new MoveTick(MoveState.PREPPING);
    private StepContext context;
    private SearchJob pending;
    private Destination destination;
    private boolean searchingAhead;
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
        this.searchingAhead = false;
        this.pendingAnnouncement = true;
    }

    public boolean searching() {
        return pending != null;
    }

    public void halt() {
        this.pending = null;
        this.searchingAhead = false;
        stopWalking();
        this.ahead = Route.empty();
        controls.clear();
        breaker.stop();
        look.clear();
    }

    public void forgetDestination() {
        this.destination = null;
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
            return;
        }
        if (spliceOntoAhead()) {
            return;
        }
        considerSearchingAhead();
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
        boolean forAhead = searchingAhead;
        searchingAhead = false;
        Destination target = destination;
        if (target == null) {
            return;
        }
        NavigatorAgent agent = NavigatorAgent.instance();
        Journey.Result result = Journey.collect(job.search(), agent.navigator().blocks(),
                agent.navigator().walk());
        Trace.instance().event("goto", "search for " + target.describe() + " finished after "
                + job.millis() + "ms, outcome " + result.outcome() + ", "
                + result.visited() + " nodes seen, " + result.route().length() + " steps usable"
                + whereAmI());
        if (forAhead) {
            holdAsAhead(result, target);
            return;
        }
        boolean announce = pendingAnnouncement;
        pendingAnnouncement = false;
        if (result.arrived()) {
            Trace.instance().event("goto", "already standing on the goal " + target.describe());
            halt();
            ClientNotice.warn("Already at " + target.describe() + ".");
            return;
        }
        if (!result.usable()) {
            Trace.instance().event("goto", "nothing usable came back, giving up on "
                    + target.describe());
            ClientNotice.warn("No path to " + target.describe() + ".");
            return;
        }
        RouteTrace.describe(result.route());
        travel(result.route());
        Trace.instance().event("walk", "walking " + result.route().length() + " steps to "
                + target.describe() + whereAmI());
        if (announce) {
            ClientNotice.warn((result.reached() ? "Path found: " : "Partial path: ")
                    + result.route().length() + " steps.");
        }
    }

    private void holdAsAhead(Journey.Result result, Destination target) {
        ahead = Route.empty();
        if (!result.usable()) {
            Trace.instance().event("walk", "nothing usable came back for the segment past "
                    + whereTheSearchStarted() + ", so it is dropped");
            return;
        }
        RouteTrace.describe(result.route());
        ahead = result.route();
        Trace.instance().event("walk", "the segment past " + whereTheSearchStarted() + " is ready, "
                + ahead.length() + " steps toward " + target.describe() + whereAmI());
    }

    private String whereTheSearchStarted() {
        int[] beyond = route.end();
        return beyond == null ? "nowhere" : describe(beyond);
    }

    private boolean spliceOntoAhead() {
        if (ahead.length() == 0 || !walker.cancellable()) {
            return false;
        }
        int[] feet = feet();
        if (feet == null || !Splice.ontoPlannedRoute(ahead, context, feet)) {
            return false;
        }
        Trace.instance().event("walk", "jumping straight onto the segment searched for ahead, "
                + ahead.length() + " steps, without finishing the current one" + whereAmI());
        travel(ahead);
        ahead = Route.empty();
        return true;
    }

    private void considerSearchingAhead() {
        if (destination == null || ahead.length() > 0 || pending != null) {
            return;
        }
        double ticksLeft = walker.ticksRemainingInSegment(false);
        if (!SegmentPlanner.dueFor(ticksLeft)) {
            return;
        }
        int[] beyond = route.end();
        if (beyond == null || reachesGoal(beyond)) {
            return;
        }
        NavigatorAgent agent = NavigatorAgent.instance();
        SearchJob job = SegmentPlanner.beyond(agent.navigator(), destination.goal(),
                beyond[0], beyond[1], beyond[2], SegmentPlanner.favorOf(route));
        if (job == null) {
            return;
        }
        Trace.instance().event("goto", "this segment runs out in " + Math.round(ticksLeft)
                + " ticks, so the search for what comes after it starts from "
                + describe(beyond) + " toward " + destination.describe());
        pending = job;
        searchingAhead = true;
    }

    private void finish(WalkOutcome outcome) {
        Destination target = destination;
        Route walked = route;
        Route planned = ahead;
        stopWalking();
        Trace.instance().event("walk", "segment " + (outcome == WalkOutcome.ABANDONED
                ? "abandoned" : "finished") + " after " + walked.length() + " steps, wanted "
                + (target == null ? "nothing" : target.describe()) + whereAmI());
        if (target == null) {
            Trace.instance().event("walk", "no goal recorded, so nothing more is searched");
            return;
        }
        if (standingOn(target)) {
            Trace.instance().event("walk", "arrived at " + target.describe());
            ClientNotice.warn("Arrived at " + target.describe() + ".");
            return;
        }
        if (planned.length() > 0) {
            int[] end = feet();
            if (end != null && planned.holds(end[0], end[1], end[2])) {
                Trace.instance().event("walk", "carrying straight on to the segment searched for "
                        + "ahead, " + planned.length() + " more steps toward " + target.describe());
                travel(planned);
                return;
            }
            Trace.instance().event("walk", "the segment searched for ahead does not cover where "
                    + "the player ended up, so it is dropped");
        }
        if (pending != null) {
            searchingAhead = true;
            Trace.instance().event("walk", "the search already running will decide what comes "
                    + "next, so nothing extra is started for " + target.describe());
            return;
        }
        Trace.instance().event("walk", "still short of " + target.describe()
                + ", searching again from where the player actually is");
        replan(target);
    }

    private void stopWalking() {
        this.active = false;
        this.route = Route.empty();
        this.tick = new MoveTick(MoveState.PREPPING);
        controls.clear();
        breaker.stop();
        look.clear();
    }

    private void replan(Destination target) {
        NavigatorAgent agent = NavigatorAgent.instance();
        if (!agent.navigator().ready()) {
            Trace.instance().event("walk", "navigator is not ready, cannot search again");
            return;
        }
        int[] from = feet();
        if (from == null) {
            return;
        }
        Trace.instance().event("goto", "searching again for " + target.describe()
                + " from " + describe(from));
        SearchJob job = SegmentPlanner.first(agent.navigator(), target.goal(),
                from[0], from[1], from[2]);
        if (job == null) {
            Trace.instance().event("goto", "the search could not be started");
            return;
        }
        await(job, target);
        pendingAnnouncement = false;
    }

    private int[] feet() {
        var player = Minecraft.getInstance().player;
        if (player == null || context == null) {
            return null;
        }
        return PathStart.whereTheWalkBegins(context);
    }

    private boolean standingOn(Destination target) {
        int[] from = feet();
        return from != null
                && from[0] == target.x() && from[1] == target.y() && from[2] == target.z();
    }

    private boolean reachesGoal(int[] block) {
        return destination != null
                && block[0] == destination.x()
                && block[1] == destination.y()
                && block[2] == destination.z();
    }

    private static String describe(int[] block) {
        return block == null ? "nowhere" : block[0] + " " + block[1] + " " + block[2];
    }

    private String whereAmI() {
        var player = Minecraft.getInstance().player;
        return player == null ? ""
                : ", player at " + dev.helm.diag.PlayerReport.everything(player);
    }
}
