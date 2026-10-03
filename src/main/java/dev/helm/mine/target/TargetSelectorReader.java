package dev.helm.mine.target;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

import dev.helm.world.cache.BlockNames;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class TargetSelectorReader {

    private TargetSelectorReader() {
    }

    public static TargetSelector read(String written) {
        String trimmed = written == null ? "" : written.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("A block must be named");
        }
        int opening = trimmed.indexOf('[');
        if (opening < 0) {
            if (trimmed.endsWith("]")) {
                throw new IllegalArgumentException("Not a block name: " + trimmed);
            }
            return TargetSelector.whole(blockOf(trimmed));
        }
        if (!trimmed.endsWith("]")) {
            throw new IllegalArgumentException("Not a block name: " + trimmed);
        }
        Block block = blockOf(trimmed.substring(0, opening));
        String properties = trimmed.substring(opening + 1, trimmed.length() - 1);
        return new TargetSelector(block, properties(block, properties));
    }

    public static Block blockOf(String written) {
        String name = written.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("A block must be named");
        }
        Block block = BlockNames.optional(name);
        if (block == null) {
            throw new IllegalArgumentException("No such block: " + name);
        }
        return block;
    }

    public static List<String> allIdentifiers() {
        List<String> names = new ArrayList<>();
        BuiltInRegistries.BLOCK.entrySet().forEach(entry -> {
            Identifier id = entry.getKey().identifier();
            names.add(id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)
                    ? id.getPath()
                    : id.toString());
        });
        names.sort(String::compareTo);
        return names;
    }

    public static List<TargetProperty> properties(Block block, String written) {
        List<TargetProperty> found = new ArrayList<>();
        if (written.isEmpty()) {
            return found;
        }
        for (String pair : written.split(",")) {
            int equals = pair.indexOf('=');
            if (equals <= 0 || equals == pair.length() - 1) {
                throw new IllegalArgumentException("\"" + pair + "\" is not a property and a value");
            }
            String name = pair.substring(0, equals).trim();
            String value = pair.substring(equals + 1).trim();
            found.add(property(block, name, value));
        }
        return found;
    }

    public static List<String> propertyNames(Block block) {
        List<String> names = new ArrayList<>();
        for (Property<?> candidate : block.getStateDefinition().getProperties()) {
            names.add(candidate.getName());
        }
        names.sort(String::compareTo);
        return names;
    }

    public static List<String> propertyValues(Block block, String name) {
        List<String> values = new ArrayList<>();
        Property property = find(block, name);
        if (property == null) {
            return values;
        }
        for (Object value : property.getPossibleValues()) {
            values.add(propertyValueName(property, value));
        }
        values.sort(String::compareTo);
        return values;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static String propertyValueName(Property property, Object value) {
        return property.getName((Comparable) value);
    }

    private static TargetProperty property(Block block, String name, String value) {
        Property property = find(block, name);
        if (property == null) {
            throw new IllegalArgumentException(block.getName().getString()
                    + " has no property called " + name);
        }
        Comparable parsed = (Comparable) property.getValue(value).orElse(null);
        if (parsed == null) {
            throw new IllegalArgumentException("\"" + value + "\" is not a value of " + name
                    + " on " + block.getName().getString());
        }
        return TargetProperty.of(property, parsed);
    }

    private static Property find(Block block, String name) {
        for (Property<?> candidate : block.getStateDefinition().getProperties()) {
            if (candidate.getName().equals(name)) {
                return candidate;
            }
        }
        return null;
    }
}