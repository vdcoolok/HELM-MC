package dev.helm.tools;

import java.util.Collections;
import java.util.Set;

import net.minecraft.world.level.block.Block;

import dev.helm.setting.MiningSettings;

public final class BlockAvoidList {

    private static volatile Set<Block> current = Collections.emptySet();

    private BlockAvoidList() {
    }

    public static void refresh(MiningSettings settings) {
        Set<Block> blocks = settings.blocksToAvoidBreaking();
        current = blocks.isEmpty() ? Collections.emptySet() : Set.copyOf(blocks);
    }

    public static boolean contains(Block block) {
        return current.contains(block);
    }

    public static Set<Block> all() {
        return current;
    }
}
