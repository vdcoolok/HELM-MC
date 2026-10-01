package dev.helm.command;

import java.util.Optional;

public interface ArgumentAccess {

    boolean has(String name);

    Optional<String> string(String name);

    Optional<Integer> integer(String name);

    Optional<Double> decimal(String name);

    Optional<Boolean> bool(String name);

    default String requireString(String name) {
        return string(name).orElseThrow(() -> new CommandException(CommandFeedback.missingArgument(name)));
    }

    default String optionalString(String name) {
        return string(name).orElse("");
    }

    default int requireInteger(String name) {
        return integer(name).orElseThrow(() -> new CommandException(CommandFeedback.missingArgument(name)));
    }

    default double requireDecimal(String name) {
        return decimal(name).orElseThrow(() -> new CommandException(CommandFeedback.missingArgument(name)));
    }

    default boolean requireBoolean(String name) {
        return bool(name).orElseThrow(() -> new CommandException(CommandFeedback.missingArgument(name)));
    }
}
