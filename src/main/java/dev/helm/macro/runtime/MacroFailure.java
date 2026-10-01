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

    public static MacroFailure unreachable(int x, int y, int z) {
        return new MacroFailure("No path to " + x + " " + y + " " + z);
    }

    public static MacroFailure unusable(InputBinding input) {
        return new MacroFailure(input + " does nothing while playing, so it cannot be held");
    }
}
