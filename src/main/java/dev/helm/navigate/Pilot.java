package dev.helm.navigate;

import dev.helm.aim.LookController;
import dev.helm.control.Control;
import dev.helm.control.ControlState;
import dev.helm.interaction.BlockBreaker;
import dev.helm.interaction.BlockPlacer;
import dev.helm.movement.MoveState;
import dev.helm.movement.MoveTick;
import dev.helm.movement.Route;
import dev.helm.movement.RouteWalker;
import dev.helm.movement.WalkOutcome;
import dev.helm.movement.step.StepContext;

public final class Pilot {

    private final ControlState controls = new ControlState();
    private final BlockBreaker breaker = new BlockBreaker();
    private final BlockPlacer placer = new BlockPlacer();
    private final RouteWalker walker;
    private final LookController look = LookController.instance();

    private Route route = Route.empty();
    private MoveTick tick = new MoveTick(MoveState.PREPPING);
    private StepContext context;
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

    public void halt() {
        this.active = false;
        this.route = Route.empty();
        this.tick = new MoveTick(MoveState.PREPPING);
        controls.clear();
        breaker.stop();
        look.clear();
    }

    public void tick() {
        if (context == null) {
            return;
        }
        controls.set(Control.ATTACK, false);
        controls.set(Control.USE, false);
        controls.set(Control.SPRINT, false);
        controls.set(Control.MOVE_FORWARD, false);
        controls.set(Control.MOVE_BACK, false);
        controls.set(Control.MOVE_LEFT, false);
        controls.set(Control.MOVE_RIGHT, false);
        controls.set(Control.JUMP, false);
        controls.set(Control.SNEAK, false);

        breaker.tick(controls.isDown(Control.ATTACK));
        placer.tick(controls.isDown(Control.USE));
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
            halt();
        }
    }
}
