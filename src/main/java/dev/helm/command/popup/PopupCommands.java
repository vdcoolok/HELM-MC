package dev.helm.command.popup;

import dev.helm.command.EditorShortcuts;

public final class PopupCommands {

    private PopupCommands() {
    }

    public static String forRow(PopupRow row) {
        if (row == null) {
            return "";
        }
        if (row.column() == PopupRow.SYNTAX_COLUMN) {
            return row.label();
        }
        for (dev.helm.command.EditorShortcut shortcut : EditorShortcuts.all()) {
            if (shortcut.label().equalsIgnoreCase(row.label())) {
                return shortcut.command().trim();
            }
        }
        return row.label();
    }
}