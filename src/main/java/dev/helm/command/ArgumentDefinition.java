package dev.helm.command;

import java.util.List;

public record ArgumentDefinition(String name, ArgumentType type, String description, boolean required) {

    public static ArgumentDefinition required(String name, ArgumentType type, String description) {
        return new ArgumentDefinition(name, type, description, true);
    }

    public static ArgumentDefinition optional(String name, ArgumentType type, String description) {
        return new ArgumentDefinition(name, type, description, false);
    }

    public String usage() {
        String rendered = name + " " + type.label();
        return required ? "<" + rendered + ">" : "[" + rendered + "]";
    }

    public String describe() {
        return name + " " + type.label() + (description.isEmpty() ? "" : " - " + description);
    }

    public static String renderList(List<ArgumentDefinition> arguments) {
        if (arguments.isEmpty()) {
            return "";
        }
        StringBuilder rendered = new StringBuilder();
        for (ArgumentDefinition argument : arguments) {
            rendered.append(argument.usage()).append(' ');
        }
        return rendered.toString().trim();
    }
}
