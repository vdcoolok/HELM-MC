package dev.helm.command;

import java.util.List;
import java.util.function.Supplier;

public record ArgumentDefinition(String name,
                                 ArgumentType type,
                                 String description,
                                 boolean required,
                                 Supplier<List<String>> suggestions) {

    public ArgumentDefinition {
        description = description == null ? "" : description;
    }

    public static ArgumentDefinition required(String name, ArgumentType type, String description) {
        return new ArgumentDefinition(name, type, description, true, null);
    }

    public static ArgumentDefinition optional(String name, ArgumentType type, String description) {
        return new ArgumentDefinition(name, type, description, false, null);
    }

    public ArgumentDefinition offering(Supplier<List<String>> values) {
        return new ArgumentDefinition(name, type, description, required, values);
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
        String rendered = placeholder() + " " + type.label();
        return required ? "<" + rendered + ">" : "[" + rendered + "]";
    }

    public String describe() {
        String text = name + " " + type.label();
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
