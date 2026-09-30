package dev.helm.setting;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public final class BlockNames {

    private BlockNames() {
    }

    public static Set<Block> parse(String list) {
        if (list == null || list.isBlank()) {
            return Collections.emptySet();
        }
        Set<Block> blocks = new LinkedHashSet<>();
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
            blocks.add(BuiltInRegistries.BLOCK.getValue(id));
        }
        return Set.copyOf(blocks);
    }
}
