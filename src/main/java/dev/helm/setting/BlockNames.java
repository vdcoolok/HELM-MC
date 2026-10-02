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
        if (list == null || list.isBlank()) {
            return List.of();
        }
        List<Block> blocks = new ArrayList<>();
        Set<Block> seen = new LinkedHashSet<>();
        for (String entry : list.split(",")) {
            String name = entry.trim().toLowerCase(Locale.ROOT);
            if (name.isEmpty()) {
                continue;
            }
            if (!name.contains(":")) {
                name = "minecraft:" + name;
            }
            Identifier id = Identifier.tryParse(name);
            if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) {
                continue;
            }
            Block block = BuiltInRegistries.BLOCK.getValue(id);
            if (seen.add(block)) {
                blocks.add(block);
            }
        }
        return List.copyOf(blocks);
    }
}
