package dev.helm.movement;

import dev.helm.control.Control;
import dev.helm.movement.step.MoveStep;

public final class MoveTick {

    private MoveState state;
    private final MoveInputs inputs = new MoveInputs();
    private final MoveIntent intent = new MoveIntent();

    public MoveTick(MoveState initial) {
        this.state = initial;
    }

    public MoveState state() {
        return state;
    }

    public MoveTick state(MoveState next) {
        this.state = next;
        return this;
    }

    public MoveInputs inputs() {
        return inputs;
    }

    public MoveIntent intent() {
        return intent;
    }

    public MoveTick press(Control control) {
        inputs.press(control);
        return this;
    }

    public MoveTick release(Control control) {
        inputs.release(control);
        return this;
    }

    public MoveTick set(Control control, boolean value) {
        inputs.set(control, value);
        return this;
    }

    public void reset() {
        state = MoveState.PREPPING;
        inputs.clear();
        intent.clear();
    }

    public boolean is(MoveStep step) {
        return state == MoveState.RUNNING && step != null;
    }
}
