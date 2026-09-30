package dev.helm.macro;

public record MacroDuration(long ticks) {

    public static final long MILLIS_PER_TICK = 50L;
    public static final long TICKS_PER_SECOND = 20L;
    public static final long TICKS_PER_MINUTE = TICKS_PER_SECOND * 60L;

    public MacroDuration {
        if (ticks < 0) {
            throw new MacroSyntaxException("Duration cannot be negative: " + ticks);
        }
    }

    public static MacroDuration parse(String token) {
        String value = token.trim().toLowerCase(java.util.Locale.ROOT);
        if (value.isEmpty()) {
            throw new MacroSyntaxException("Missing duration");
        }

        if (value.endsWith("ms")) {
            return from(value.substring(0, value.length() - 2), token, amount -> amount / MILLIS_PER_TICK);
        }
        if (value.endsWith("s")) {
            return from(value.substring(0, value.length() - 1), token, amount -> amount * TICKS_PER_SECOND);
        }
        if (value.endsWith("m")) {
            return from(value.substring(0, value.length() - 1), token, amount -> amount * TICKS_PER_MINUTE);
        }
        if (value.endsWith("t")) {
            return from(value.substring(0, value.length() - 1), token, amount -> amount);
        }
        return from(value, token, amount -> amount);
    }

    private static MacroDuration from(String value, String token, java.util.function.DoubleUnaryOperator scale) {
        double amount = amount(value.trim(), token);
        long ticks = Math.round(scale.applyAsDouble(amount));
        if (ticks < 0L) {
            throw new MacroSyntaxException("Duration cannot be negative: " + token);
        }
        return new MacroDuration(ticks);
    }

    private static double amount(String value, String token) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException invalid) {
            throw new MacroSyntaxException("Invalid duration: " + token);
        }
    }
}
