package dev.helm.command;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class CommandRegistry {

    private final Map<String, CommandDefinition> byName = new LinkedHashMap<>();

    public void register(CommandDefinition definition) {
        if (byName.containsKey(definition.name())) {
            throw new IllegalStateException("Duplicate command name: " + definition.name());
        }
        byName.put(definition.name(), definition);
    }

    public Optional<CommandDefinition> find(String label) {
        CommandDefinition exact = byName.get(label.toLowerCase(Locale.ROOT));
        if (exact != null) {
            return Optional.of(exact);
        }
        return byName.values().stream().filter(definition -> definition.matches(label)).findFirst();
    }

    public List<CommandDefinition> all() {
        return List.copyOf(byName.values());
    }

    public Collection<String> labels() {
        return byName.keySet();
    }
}
