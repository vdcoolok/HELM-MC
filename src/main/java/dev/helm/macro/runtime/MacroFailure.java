package dev.helm.macro.runtime;

import dev.helm.input.InputBinding;

public final class MacroFailure extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private MacroFailure(String message) {
        super(message);
    }

    public static MacroFailure noWorld() {
        return new MacroFailure("Not in a world yet");
    }

    public static MacroFailure unreachable(String label) {
        return new MacroFailure("No path to " + label);
    }

    public static MacroFailure unusable(InputBinding input) {
        return new MacroFailure(input + " does nothing while playing, so it cannot be held");
    }
}
