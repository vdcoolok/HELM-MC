package dev.helm.macro.runtime;

import dev.helm.macro.MacroStatement;

public final class MacroFailure extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private MacroFailure(String message) {
        super(message);
    }

    public static MacroFailure unavailable(MacroStatement statement) {
        return new MacroFailure("'" + statement.keyword() + "' is not available yet");
    }
}
