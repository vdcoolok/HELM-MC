package dev.helm.command;

import java.util.List;
import java.util.function.Supplier;

public record ArgumentDefinition(String name,
                                 ArgumentType type,
                                 String description,
                                 boolean required,
                                 boolean variadic,
                                 Supplier<List<String>> suggestions) {

    public ArgumentDefinition {
        description = description == null ? "" : description;
        variadic = variadic && type == ArgumentType.STRING;
    }

    public static ArgumentDefinition required(String name, ArgumentType type, String description) {
        return new ArgumentDefinition(name, type, description, true, false, null);
    }

    public static ArgumentDefinition optional(String name, ArgumentType type, String description) {
        return new ArgumentDefinition(name, type, description, false, false, null);
    }

    public static ArgumentDefinition rest(String name, String description) {
        return new ArgumentDefinition(name, ArgumentType.STRING, description, true, true, null);
    }

    public ArgumentDefinition offering(Supplier<List<String>> values) {
        return new ArgumentDefinition(name, type, description, required, variadic, values);
    }

    public boolean hasSuggestions() {
        return suggestions != null;
    }

    public List<String> suggestedValues() {
        if (suggestions == null) {
            return List.of();
        }
        List<String> values = suggestions.get();
        return values == null ? List.of() : values;
    }

    public String placeholder() {
        return required ? "<" + name + ">" : "[" + name + "]";
    }

    public String usage() {
        if (variadic) {
            return name + " " + type.label() + "...";
        }
        String rendered = placeholder() + " " + type.label();
        return required ? "<" + rendered + ">" : "[" + rendered + "]";
    }

    public String describe() {
        String text = variadic ? name + " " + type.label() + "..."
                : name + " " + type.label();
        return description.isEmpty() ? text : text + " - " + description;
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