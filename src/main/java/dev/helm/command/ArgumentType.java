package dev.helm.command;

import java.util.Locale;
import java.util.Optional;

public enum ArgumentType {

    STRING,
    INTEGER,
    DOUBLE,
    BOOLEAN;

    public Optional<Object> coerce(String token) {
        String trimmed = token.trim();
        return switch (this) {
            case STRING -> Optional.of(trimmed);
            case INTEGER -> parseInteger(trimmed);
            case DOUBLE -> parseDouble(trimmed);
            case BOOLEAN -> parseBoolean(trimmed);
        };
    }

    private static Optional<Object> parseInteger(String token) {
        try {
            return Optional.of(Integer.valueOf(token));
        } catch (NumberFormatException invalid) {
            return Optional.empty();
        }
    }

    private static Optional<Object> parseDouble(String token) {
        try {
            return Optional.of(Double.valueOf(token));
        } catch (NumberFormatException invalid) {
            return Optional.empty();
        }
    }

    private static Optional<Object> parseBoolean(String token) {
        return switch (token.toLowerCase(Locale.ROOT)) {
            case "true" -> Optional.of(Boolean.TRUE);
            case "false" -> Optional.of(Boolean.FALSE);
            default -> Optional.empty();
        };
    }

    public String label() {
        return switch (this) {
            case STRING -> "<string>";
            case INTEGER -> "<integer>";
            case DOUBLE -> "<number>";
            case BOOLEAN -> "<true|false>";
        };
    }
}
