package dev.helm.movement;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import dev.helm.control.Control;

public final class MoveInputs {

    private final Map<Control, Boolean> states = new EnumMap<>(Control.class);

    public void press(Control control) {
        states.put(control, Boolean.TRUE);
    }

    public void release(Control control) {
        states.put(control, Boolean.FALSE);
    }

    public void set(Control control, boolean value) {
        states.put(control, value);
    }

    public void toggle(Control control, boolean value) {
        if (value) {
            press(control);
        } else {
            release(control);
        }
    }

    public boolean isSet(Control control) {
        return states.getOrDefault(control, Boolean.FALSE);
    }

    public Map<Control, Boolean> view() {
        return Collections.unmodifiableMap(states);
    }

    public void clear() {
        states.clear();
    }

    public Map<Control, Boolean> drain() {
        Map<Control, Boolean> copy = new EnumMap<>(states);
        states.clear();
        return copy;
    }
}
