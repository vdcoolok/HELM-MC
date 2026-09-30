package dev.helm.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class Command {

    private final String name;
    private final List<String> aliases = new ArrayList<>();
    private final List<ArgumentDefinition> arguments = new ArrayList<>();
    private final List<Command> children = new ArrayList<>();

    private String description = "";
    private CommandExecutor executor;
    private Command parent;
    private java.util.function.BooleanSupplier visible = () -> true;

    private Command(String name) {
        this.name = name;
    }

    public static Command leaf(String name, CommandExecutor executor) {
        Command command = new Command(name);
        command.executor = executor;
        return command;
    }

    public static Command group(String name) {
        return new Command(name);
    }

    public Command also(String... labels) {
        aliases.addAll(List.of(labels));
        return this;
    }

    public Command describedAs(String text) {
        description = text;
        return this;
    }

    public Command taking(ArgumentDefinition... definitions) {
        arguments.addAll(List.of(definitions));
        return this;
    }

    public Command shownWhen(java.util.function.BooleanSupplier condition) {
        visible = condition;
        return this;
    }

    public boolean isVisible() {
        return visible.getAsBoolean();
    }

    public Command containing(Command... commands) {
        for (Command command : commands) {
            command.parent = this;
            children.add(command);
        }
        return this;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public List<ArgumentDefinition> arguments() {
        return List.copyOf(arguments);
    }

    public List<Command> children() {
        return List.copyOf(children);
    }

    public Optional<Command> parent() {
        return Optional.ofNullable(parent);
    }

    public CommandExecutor executor() {
        if (executor == null) {
            throw new IllegalStateException(name + " has no executor");
        }
        return executor;
    }

    public boolean isGroup() {
        return executor == null;
    }

    public boolean matches(String token) {
        if (name.equalsIgnoreCase(token)) {
            return true;
        }
        return aliases.stream().anyMatch(alias -> alias.equalsIgnoreCase(token));
    }

    public String path() {
        return parent == null ? name : parent.path() + " " + name;
    }

    public List<String> labels() {
        List<String> all = new ArrayList<>();
        all.add(name);
        all.addAll(aliases);
        return all;
    }

    public Optional<Command> child(String token) {
        return children.stream().filter(command -> command.matches(token)).findFirst();
    }

    public String usage() {
        StringBuilder text = new StringBuilder(path());
        for (ArgumentDefinition argument : arguments) {
            text.append(' ').append(argument.usage());
        }
        return text.toString();
    }

    public String lowerName() {
        return name.toLowerCase(Locale.ROOT);
    }
}
