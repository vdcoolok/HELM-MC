package dev.helm.macro;

public final class MacroCount {

    private MacroCount() {
    }

    public static long parse(String token, int line) {
        try {
            long value = Long.parseLong(token.trim());
            if (value < 0) {
                throw new MacroSyntaxException("Repeat count cannot be negative on line " + line);
            }
            return value;
        } catch (NumberFormatException invalid) {
            throw new MacroSyntaxException("Invalid repeat count: " + token + " on line " + line);
        }
    }
}
