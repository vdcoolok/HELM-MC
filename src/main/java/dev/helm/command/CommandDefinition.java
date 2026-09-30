package dev.helm.command;

import java.util.List;

public record CommandDefinition(String name,
                                List<String> aliases,
                                String description,
                                List<ArgumentDefinition> arguments,
                                CommandExecutor executor) {

    public CommandDefinition {
        aliases = List.copyOf(aliases);
        arguments = List.copyOf(arguments);
    }

    public boolean matches(String candidate) {
        if (name.equalsIgnoreCase(candidate)) {
            return true;
        }
        return aliases.stream().anyMatch(alias -> alias.equalsIgnoreCase(candidate));
    }

    public String usage() {
        String rendered = ArgumentDefinition.renderList(arguments);
        return rendered.isEmpty() ? name : name + " " + rendered;
    }
}
