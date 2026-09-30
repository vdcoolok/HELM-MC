package dev.helm.input;

import com.mojang.blaze3d.platform.InputConstants;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class KeyCodes {

    private static final Map<String, Integer> BY_NAME = new LinkedHashMap<>();
    private static final Map<String, String> DESCRIPTIONS = new LinkedHashMap<>();
    private static final List<String> DISPLAY_ORDER = new ArrayList<>();

    static {
        described("SPACE", InputConstants.KEY_SPACE, "space bar");
        described("SHIFT", InputConstants.KEY_LSHIFT, "left shift");
        described("LSHIFT", InputConstants.KEY_LSHIFT, "left shift");
        described("RSHIFT", InputConstants.KEY_RSHIFT, "right shift");
        described("CTRL", InputConstants.KEY_LCONTROL, "left ctrl");
        described("LCTRL", InputConstants.KEY_LCONTROL, "left ctrl");
        described("RCTRL", InputConstants.KEY_RCONTROL, "right ctrl");
        described("CONTROL", InputConstants.KEY_LCONTROL, "left ctrl");
        described("ALT", InputConstants.KEY_LALT, "left alt");
        described("LALT", InputConstants.KEY_LALT, "left alt");
        described("RALT", InputConstants.KEY_RALT, "right alt");
        described("ENTER", InputConstants.KEY_RETURN, "enter");
        described("RETURN", InputConstants.KEY_RETURN, "enter");
        described("TAB", InputConstants.KEY_TAB, "tab");
        described("ESCAPE", InputConstants.KEY_ESCAPE, "esc");
        described("ESC", InputConstants.KEY_ESCAPE, "esc");

        for (int digit = 0; digit <= 9; digit++) {
            described(Integer.toString(digit), code("KEY_" + digit), "number " + digit);
        }
        for (char letter = 'A'; letter <= 'Z'; letter++) {
            described(String.valueOf(letter), code("KEY_" + letter), "letter "
                    + Character.toLowerCase(letter));
        }
        for (int function = 1; function <= 25; function++) {
            described("F" + function, code("KEY_F" + function), "function key " + function);
        }
    }

    private KeyCodes() {
    }

    static Integer find(String name) {
        return BY_NAME.get(name);
    }

    static String canonical(String name) {
        return name;
    }

    static String describe(String name) {
        return DESCRIPTIONS.getOrDefault(name, "key");
    }

    static List<String> listed() {
        return DISPLAY_ORDER;
    }

    private static void described(String name, int code, String description) {
        if (BY_NAME.putIfAbsent(name, code) == null) {
            DISPLAY_ORDER.add(name);
        }
        DESCRIPTIONS.putIfAbsent(name, description);
    }

    private static int code(String constant) {
        try {
            return (Integer) InputConstants.class.getField(constant).get(null);
        } catch (ReflectiveOperationException missing) {
            throw new IllegalStateException("Missing key constant: " + constant);
        }
    }

    static String normalize(String token) {
        return token.trim().toUpperCase(Locale.ROOT);
    }
}
