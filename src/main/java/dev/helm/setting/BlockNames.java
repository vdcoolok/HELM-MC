package dev.helm.setting;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public final class BlockNames {

    private BlockNames() {
    }

    public static Set<Block> parse(String list) {
        return Set.copyOf(ordered(list));
    }

    public static List<Block> ordered(String list) {
        List<Block> blocks = new ArrayList<>();
        Set<Block> seen = new LinkedHashSet<>();
        for (String entry : tokens(list)) {
            Block block = blockOf(entry);
            if (block != null && seen.add(block)) {
                blocks.add(block);
            }
        }
        return List.copyOf(blocks);
    }

    public static String knownOnly(String list) {
        List<String> good = new ArrayList<>();
        for (String entry : tokens(list)) {
            if (blockOf(entry) != null && !good.contains(entry)) {
                good.add(entry);
            }
        }
        return String.join(", ", good);
    }

    public static List<String> unknown(String list) {
        List<String> bad = new ArrayList<>();
        for (String entry : tokens(list)) {
            if (blockOf(entry) == null && !bad.contains(entry)) {
                bad.add(entry);
            }
        }
        return List.copyOf(bad);
    }

    private static Block blockOf(String name) {
        String full = name.contains(":") ? name : "minecraft:" + name;
        Identifier id = Identifier.tryParse(full);
        if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) {
            return null;
        }
        return BuiltInRegistries.BLOCK.getValue(id);
    }

    private static List<String> tokens(String list) {
        List<String> found = new ArrayList<>();
        if (list == null || list.isBlank()) {
            return found;
        }
        for (String entry : separate(list).split(",")) {
            String name = entry.trim().toLowerCase(Locale.ROOT);
            if (!name.isEmpty()) {
                found.add(name);
            }
        }
        return found;
    }

    public static String separate(String list) {
        if (list == null || list.isBlank()) {
            return "";
        }
        return list.trim().replaceAll("[,\\s]+", ", ");
    }
}
