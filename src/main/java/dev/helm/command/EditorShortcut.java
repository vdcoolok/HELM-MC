package dev.helm.command;

import java.util.List;

public record EditorShortcut(String label, String command, String meaning, List<String> arguments) {

    public EditorShortcut {
        arguments = arguments == null ? List.of() : List.copyOf(arguments);
    }

    public boolean takesRest() {
        return command.endsWith(" ");
    }
}