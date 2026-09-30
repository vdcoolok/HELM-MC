package dev.helm.world.cache;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public final class BlockNames {

    private static final Map<String, Block> RESOLVED = new HashMap<>();

    private BlockNames() {
    }

    public static String of(Block block) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        if (id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
            return id.getPath();
        }
        return id.toString();
    }

    public static Block required(String name) {
        Block block = optional(name);
        if (block == null) {
            throw new IllegalArgumentException("No such block: " + name);
        }
        return block;
    }

    public static Block optional(String name) {
        if (RESOLVED.containsKey(name)) {
            return RESOLVED.get(name);
        }
        Block block = lookup(name);
        RESOLVED.put(name, block);
        return block;
    }

    private static Block lookup(String name) {
        Identifier id = Identifier.tryParse(name.contains(":") ? name
                : Identifier.DEFAULT_NAMESPACE + ":" + name);
        return id == null ? null : BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
    }
}
