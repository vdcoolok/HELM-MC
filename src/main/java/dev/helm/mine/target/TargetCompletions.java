package dev.helm.mine.target;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.world.level.block.Block;

public final class TargetCompletions {

    private static List<String> identifiers;

    private TargetCompletions() {
    }

    public static List<String> identifiers() {
        if (identifiers == null) {
            identifiers = TargetSelectorReader.allIdentifiers();
        }
        return identifiers;
    }

    public static List<String> forToken(String typed) {
        String token = typed == null ? "" : typed;
        if (token.endsWith("]")) {
            return List.of();
        }
        if (!token.contains("[")) {
            return matchingIdentifiers(token);
        }
        return matchingProperties(token);
    }

    private static List<String> matchingIdentifiers(String typed) {
        List<String> found = new ArrayList<>();
        String wanted = typed.toLowerCase(Locale.ROOT);
        for (String identifier : identifiers()) {
            if (identifier.toLowerCase(Locale.ROOT).startsWith(wanted)) {
                found.add(identifier);
            }
        }
        return found;
    }

    private static List<String> matchingProperties(String typed) {
        int opening = typed.lastIndexOf('[');
        String properties = typed.substring(opening + 1);
        int comma = properties.lastIndexOf(',');
        String leading = comma < 0 ? "" : properties.substring(0, comma + 1);
        String last = comma < 0 ? properties : properties.substring(comma + 1);

        Block block = dev.helm.world.cache.BlockNames.optional(typed.substring(0, opening));
        if (block == null) {
            return List.of();
        }
        if (!last.contains("=")) {
            return withPrefix(leading, unused(block, leading, last));
        }
        int equals = last.indexOf('=');
        String name = last.substring(0, equals);
        return withPrefix(leading + name + "=",
                TargetSelectorReader.propertyValues(block, name), last.substring(equals + 1));
    }

    private static List<String> unused(Block block, String leading, String last) {
        List<String> alreadyUsed = new ArrayList<>();
        String typed = leading + last;
        int start = 0;
        while (start < typed.length()) {
            int equals = typed.indexOf('=', start);
            if (equals < 0) {
                break;
            }
            int comma = typed.indexOf(',', equals);
            if (comma < 0) {
                comma = typed.length();
            }
            alreadyUsed.add(typed.substring(start, equals).trim());
            start = comma + 1;
        }
        List<String> found = new ArrayList<>();
        for (String name : TargetSelectorReader.propertyNames(block)) {
            if (!alreadyUsed.contains(name)) {
                found.add(name);
            }
        }
        return found;
    }

    private static List<String> withPrefix(String prefix, List<String> names) {
        List<String> found = new ArrayList<>(names.size());
        for (String name : names) {
            found.add(prefix + name);
        }
        return found;
    }

    private static List<String> withPrefix(String prefix, List<String> names, String typed) {
        String wanted = typed.toLowerCase(Locale.ROOT);
        List<String> found = new ArrayList<>();
        for (String name : names) {
            if (name.toLowerCase(Locale.ROOT).startsWith(wanted)) {
                found.add(prefix + name);
            }
        }
        return found;
    }
}