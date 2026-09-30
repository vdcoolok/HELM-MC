package dev.helm.command.chat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import dev.helm.command.ArgumentAccess;
import dev.helm.command.ArgumentDefinition;
import dev.helm.command.ArgumentType;
import dev.helm.command.CommandException;
import dev.helm.command.CommandFeedback;

public final class TokenArguments implements ArgumentAccess {

    private final Map<String, Object> values;

    private TokenArguments(Map<String, Object> values) {
        this.values = values;
    }

    public static TokenArguments resolve(List<ArgumentDefinition> definitions, List<String> tokens) {
        Map<String, Object> values = new HashMap<>();
        int cursor = 0;
        int lastRequired = definitions.size();

        for (int index = 0; index < definitions.size(); index++) {
            if (!definitions.get(index).required()) {
                lastRequired = index;
                break;
            }
        }

        for (int index = 0; index < lastRequired; index++) {
            ArgumentDefinition definition = definitions.get(index);
            if (cursor >= tokens.size()) {
                throw new CommandException(CommandFeedback.missingArgument(definition.name()));
            }
            values.put(definition.name(), coerce(definition, tokens.get(cursor)));
            cursor++;
        }

        if (lastRequired < definitions.size()) {
            ArgumentDefinition optional = definitions.get(lastRequired);
            StringBuilder remaining = new StringBuilder();
            while (cursor < tokens.size()) {
                if (remaining.length() > 0) {
                    remaining.append(' ');
                }
                remaining.append(tokens.get(cursor));
                cursor++;
            }
            if (remaining.length() > 0 && optional.type() == ArgumentType.STRING) {
                values.put(optional.name(), remaining.toString());
            }
        }

        return new TokenArguments(values);
    }

    private static Object coerce(ArgumentDefinition definition, String token) {
        return definition.type().coerce(token)
                .orElseThrow(() -> new CommandException(
                        CommandFeedback.invalidArgument(definition.name(), token)));
    }

    @Override
    public boolean has(String name) {
        return values.containsKey(name);
    }

    @Override
    public Optional<String> string(String name) {
        return Optional.ofNullable(values.get(name)).map(String::valueOf);
    }

    @Override
    public Optional<Integer> integer(String name) {
        return cast(name, Integer.class);
    }

    @Override
    public Optional<Double> decimal(String name) {
        return cast(name, Double.class);
    }

    @Override
    public Optional<Boolean> bool(String name) {
        return cast(name, Boolean.class);
    }

    private <T> Optional<T> cast(String name, Class<T> type) {
        Object value = values.get(name);
        return type.isInstance(value) ? Optional.of(type.cast(value)) : Optional.empty();
    }
}
