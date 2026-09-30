package dev.helm.control;

import java.util.EnumMap;
import java.util.Map;

public final class ControlState {

    private final Map<Control, Boolean> forced = new EnumMap<>(Control.class);

    public boolean isDown(Control control) {
        if (control == null) {
            return false;
        }
        return forced.getOrDefault(control, Boolean.FALSE);
    }

    public void set(Control control, boolean value) {
        forced.put(control, value);
    }

    public void clear() {
        forced.clear();
    }

    public boolean anyMovementRequested() {
        for (Control control : Control.MOVEMENT) {
            if (isDown(control)) {
                return true;
            }
        }
        return false;
    }

    public Map<Control, Boolean> snapshot() {
        return Map.copyOf(forced);
    }
}
