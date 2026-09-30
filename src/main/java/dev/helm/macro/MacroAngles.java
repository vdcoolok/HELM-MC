package dev.helm.macro;

import dev.helm.rotation.Rotation;

public final class MacroAngles {

    private static final char SEPARATOR = '/';

    private MacroAngles() {
    }

    public static Rotation parse(String arguments, int line) {
        String[] parts = arguments.split("\\" + SEPARATOR);
        if (parts.length != 2) {
            throw new MacroSyntaxException("Expected 'pitch / yaw' on line " + line);
        }
        return Rotation.of(angle(parts[0], "pitch", line), angle(parts[1], "yaw", line));
    }

    private static double angle(String token, String axis, int line) {
        try {
            return Double.parseDouble(token.trim());
        } catch (NumberFormatException invalid) {
            throw new MacroSyntaxException("Invalid " + axis + ": " + token.trim() + " on line " + line);
        }
    }
}
