package dev.helm.command;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class EditorShortcuts {

    private static final String MACRO = "macro";
    private static final String ACTION = MACRO + " action";

    private static final List<EditorShortcut> ALL = List.of(
            new EditorShortcut("exitEditMode", "exitEditMode", "close this macro", List.of()),
            new EditorShortcut("exit", "exitEditMode", "close this macro", List.of()),
            new EditorShortcut("stopEdit", "exitEditMode", "close this macro", List.of()),
            new EditorShortcut("exitEdit", "exitEditMode", "close this macro", List.of()),
            new EditorShortcut("list", ACTION + " list", "show the lines", List.of()),
            new EditorShortcut("remove", ACTION + " remove ", "delete a line", List.of("<line>")),
            new EditorShortcut("rm", ACTION + " remove ", "delete a line", List.of("<line>")),
            new EditorShortcut("delete", ACTION + " remove ", "delete a line", List.of("<line>")),
            new EditorShortcut("move", ACTION + " move ", "move a line",
                    List.of("<from>", "<to>")));

    private EditorShortcuts() {
    }

    public static List<EditorShortcut> all() {
        return ALL;
    }

    public static Optional<EditorShortcut> find(String label) {
        for (EditorShortcut shortcut : ALL) {
            if (shortcut.label().equalsIgnoreCase(label)) {
                return Optional.of(shortcut);
            }
        }
        return Optional.empty();
    }

    public static List<EditorShortcut> shown() {
        return List.of(ALL.get(0), ALL.get(4), ALL.get(5), ALL.get(8));
    }

    public static String expand(String line) {
        String trimmed = line.trim();
        int space = trimmed.indexOf(' ');
        String head = (space < 0 ? trimmed : trimmed.substring(0, space)).toLowerCase(Locale.ROOT);
        String rest = space < 0 ? "" : trimmed.substring(space + 1).trim();

        for (EditorShortcut shortcut : ALL) {
            if (!shortcut.label().equalsIgnoreCase(head)) {
                continue;
            }
            if (shortcut.takesRest()) {
                return shortcut.command().trim() + " " + rest;
            }
            return rest.isEmpty() ? shortcut.command().trim() : null;
        }
        return null;
    }
}