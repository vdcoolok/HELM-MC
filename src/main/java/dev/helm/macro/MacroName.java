package dev.helm.macro;

import java.util.Locale;

public final class MacroName {

    public static final String SUFFIX = ".macro";

    private MacroName() {
    }

    public static String normalise(String name) {
        if (name == null) {
            return null;
        }
        String trimmed = name.trim();
        if (endsWithSuffix(trimmed)) {
            return trimmed.substring(0, trimmed.length() - SUFFIX.length());
        }
        return trimmed;
    }

    public static boolean isValid(String name) {
        if (name == null || name.isEmpty() || name.length() > 64) {
            return false;
        }
        for (int index = 0; index < name.length(); index++) {
            char character = name.charAt(index);
            boolean allowed = Character.isLetterOrDigit(character) || character == '_' || character == '-';
            if (!allowed) {
                return false;
            }
        }
        return true;
    }

    public static void validate(String name) {
        if (!isValid(name)) {
            throw new MacroStoreException("Invalid macro name: " + name, null);
        }
    }

    private static boolean endsWithSuffix(String name) {
        return name.length() > SUFFIX.length()
                && name.toLowerCase(Locale.ROOT).endsWith(SUFFIX);
    }
}
