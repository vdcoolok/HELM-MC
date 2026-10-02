package dev.helm.command.popup;

import java.util.ArrayList;
import java.util.List;

import dev.helm.command.MacroEditing;
import dev.helm.command.chat.DollarPrefix;
import dev.helm.setting.SettingCatalogue;
import dev.helm.setting.SettingKind;

public final class PopupGate {

    private static final String MACRO = "macro";
    private static final String SET = "set";

    private PopupGate() {
    }

    public static boolean owns(String chatValue) {
        if (mentionsSetting(chatValue)) {
            return true;
        }
        if (!MacroEditing.isActive()) {
            return false;
        }
        if (!DollarPrefix.isCommand(chatValue)) {
            return false;
        }
        return !mentionsMacro(chatValue);
    }

    public static boolean mentionsMacro(String chatValue) {
        for (String token : tokens(body(chatValue))) {
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
        if (mentionsSetting(chatValue)) {
            SettingCatalogue.Entry entry = namedSetting(chatValue, cursor);
            if (entry == null) {
                return Mode.SETTINGS;
            }
            if (entry.kind() == SettingKind.BLOCKS) {
                return Mode.BLOCKS;
            }
            return Mode.VALUES;
        }
        if (!owns(chatValue)) {
            return Mode.NONE;
        }
        String before = typedSoFar(chatValue, cursor);
        if (!hasWhitespace(before)) {
            return Mode.NAMES;
        }
        return dev.helm.command.ArgumentHints.wantsInput(before) ? Mode.INPUTS : Mode.NONE;
    }

    public static boolean mentionsSetting(String chatValue) {
        List<String> words = tokens(body(chatValue));
        if (words.isEmpty() || !words.get(0).equalsIgnoreCase(SET)) {
            return false;
        }
        if (words.size() <= 2) {
            return true;
        }
        return picksBlocks(words.get(1));
    }

    private static boolean picksBlocks(String name) {
        SettingCatalogue.Entry entry = SettingCatalogue.find(name);
        return entry != null && entry.kind() == SettingKind.BLOCKS;
    }

    public static List<PopupRow> rows(String chatValue, int cursor) {
        Mode mode = mode(chatValue, cursor);
        if (mode == Mode.SETTINGS) {
            return SettingPicker.rows(partial(chatValue, cursor));
        }
        if (mode == Mode.BLOCKS) {
            SettingCatalogue.Entry entry = namedSetting(chatValue, cursor);
            return BlockPicker.rows(entry, blockFilter(chatValue, cursor, entry));
        }
        if (mode == Mode.VALUES) {
            return SettingPicker.values(namedSetting(chatValue, cursor),
                    partial(chatValue, cursor));
        }
        return List.of();
    }

    private static String blockFilter(String chatValue, int cursor,
                                      SettingCatalogue.Entry entry) {
        String typed = partial(chatValue, cursor);
        if (entry != null && typed.equalsIgnoreCase(entry.key())) {
            return "";
        }
        return typed;
    }

    private static SettingCatalogue.Entry namedSetting(String chatValue, int cursor) {
        List<String> words = tokens(body(chatValue).substring(0, bounded(chatValue, cursor)));
        if (words.size() < 2) {
            return null;
        }
        return SettingCatalogue.find(words.get(1));
    }

    public static String settingName(String chatValue, int cursor) {
        List<String> words = tokens(body(chatValue).substring(0, bounded(chatValue, cursor)));
        return words.size() < 2 ? null : words.get(1);
    }

    public static String hint(String chatValue, int cursor) {
        if (mode(chatValue, cursor) != Mode.NONE) {
            return null;
        }
        return dev.helm.command.ArgumentHints.next(typedSoFar(chatValue, cursor));
    }

    private static int bounded(String chatValue, int cursor) {
        return Math.min(Math.max(cursor - 1, 0), body(chatValue).length());
    }

    private static String typedSoFar(String chatValue, int cursor) {
        String text = body(chatValue);
        return text.substring(0, bounded(chatValue, cursor));
    }

    private static String body(String chatValue) {
        if (!DollarPrefix.isCommand(chatValue)) {
            return "";
        }
        return DollarPrefix.body(chatValue);
    }

    private static List<String> tokens(String text) {
        List<String> found = new ArrayList<>();
        for (String token : dev.helm.command.chat.LineTokenizer.tokenize(text)) {
            found.add(token);
        }
        return found;
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
        String text = body(chatValue);
        int index = Math.min(Math.max(cursor - 1, 0), text.length());
        int start = index;
        while (start > 0 && !Character.isWhitespace(text.charAt(start - 1))) {
            start--;
        }
        return index > start ? text.substring(start, index) : "";
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