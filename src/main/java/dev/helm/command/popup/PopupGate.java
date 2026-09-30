package dev.helm.command.popup;

import dev.helm.command.MacroEditing;
import dev.helm.command.chat.DollarPrefix;

public final class PopupGate {

    private static final String MACRO = "macro";

    private PopupGate() {
    }

    public static boolean owns(String chatValue) {
        if (!MacroEditing.isActive()) {
            return false;
        }
        if (!DollarPrefix.isCommand(chatValue)) {
            return false;
        }
        return !mentionsMacro(chatValue);
    }

    public static boolean mentionsMacro(String chatValue) {
        String body = DollarPrefix.body(chatValue);
        for (String token : dev.helm.command.chat.LineTokenizer.tokenize(body)) {
            if (token.equalsIgnoreCase(MACRO)) {
                return true;
            }
        }
        return false;
    }

    public static boolean picking(String chatValue, int cursor) {
        return mode(chatValue, cursor) == Mode.NAMES;
    }

    public static Mode mode(String chatValue, int cursor) {
        if (!owns(chatValue)) {
            return Mode.NONE;
        }
        String before = typedSoFar(chatValue, cursor);
        if (!hasWhitespace(before)) {
            return Mode.NAMES;
        }
        return dev.helm.command.ArgumentHints.wantsInput(before) ? Mode.INPUTS : Mode.NONE;
    }

    public static String hint(String chatValue, int cursor) {
        if (mode(chatValue, cursor) != Mode.NONE) {
            return null;
        }
        return dev.helm.command.ArgumentHints.next(typedSoFar(chatValue, cursor));
    }

    private static String typedSoFar(String chatValue, int cursor) {
        String body = DollarPrefix.body(chatValue);
        int index = Math.min(Math.max(cursor - 1, 0), body.length());
        return body.substring(0, index);
    }

    private static boolean hasWhitespace(String text) {
        for (int index = 0; index < text.length(); index++) {
            if (Character.isWhitespace(text.charAt(index))) {
                return true;
            }
        }
        return false;
    }

    public static String partial(String chatValue, int cursor) {
        String body = DollarPrefix.body(chatValue);
        int index = Math.min(Math.max(cursor, 0), body.length());
        int start = index;
        while (start > 0 && !Character.isWhitespace(body.charAt(start - 1))) {
            start--;
        }
        return index > start ? body.substring(start, index) : "";
    }

    public static int tokenStart(String chatValue, int cursor) {
        int floor = DollarPrefix.isCommand(chatValue) ? DollarPrefix.PREFIX_LENGTH : 0;
        int index = Math.min(Math.max(cursor, 0), chatValue.length());
        index = Math.max(index, floor);
        while (index > floor && !Character.isWhitespace(chatValue.charAt(index - 1))) {
            index--;
        }
        return index;
    }
}