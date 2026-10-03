package dev.helm.mine.target;

import net.minecraft.world.level.block.Block;

import dev.helm.world.cache.BlockNames;

public final class DeepslateVariants {

    private static final String DEEPSLATE_PREFIX = "deepslate_";
    private static final String ORE_SUFFIX = "_ore";

    private DeepslateVariants() {
    }

    public static Block twinOf(Block block) {
        String name = BlockNames.of(block);
        String twin = twinName(name);
        if (twin == null) {
            return null;
        }
        Block found = BlockNames.optional(twin);
        return found == null || found == block ? null : found;
    }

    private static String twinName(String name) {
        if (name.startsWith(DEEPSLATE_PREFIX)) {
            String plain = name.substring(DEEPSLATE_PREFIX.length());
            return plain.endsWith(ORE_SUFFIX) ? plain : null;
        }
        return name.endsWith(ORE_SUFFIX) ? DEEPSLATE_PREFIX + name : null;
    }
}
