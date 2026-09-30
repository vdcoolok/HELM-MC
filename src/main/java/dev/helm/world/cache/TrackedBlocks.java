package dev.helm.world.cache;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class TrackedBlocks {

    private static final Set<Block> SET = build();

    private TrackedBlocks() {
    }

    public static boolean is(Block block) {
        return SET.contains(block);
    }

    private static Set<Block> build() {
        Set<Block> blocks = new HashSet<>();
        blocks.add(Blocks.ENDER_CHEST);
        blocks.add(Blocks.FURNACE);
        blocks.add(Blocks.CHEST);
        blocks.add(Blocks.TRAPPED_CHEST);
        blocks.add(Blocks.END_PORTAL);
        blocks.add(Blocks.END_PORTAL_FRAME);
        blocks.add(Blocks.SPAWNER);
        blocks.add(Blocks.BARRIER);
        blocks.add(Blocks.OBSERVER);
        blocks.add(Blocks.NETHER_PORTAL);
        blocks.add(Blocks.HOPPER);
        blocks.add(Blocks.BEACON);
        blocks.add(Blocks.BREWING_STAND);
        blocks.add(Blocks.CREEPER_HEAD);
        blocks.add(Blocks.CREEPER_WALL_HEAD);
        blocks.add(Blocks.DRAGON_HEAD);
        blocks.add(Blocks.DRAGON_WALL_HEAD);
        blocks.add(Blocks.PLAYER_HEAD);
        blocks.add(Blocks.PLAYER_WALL_HEAD);
        blocks.add(Blocks.ZOMBIE_HEAD);
        blocks.add(Blocks.ZOMBIE_WALL_HEAD);
        blocks.add(Blocks.SKELETON_SKULL);
        blocks.add(Blocks.SKELETON_WALL_SKULL);
        blocks.add(Blocks.WITHER_SKELETON_SKULL);
        blocks.add(Blocks.WITHER_SKELETON_WALL_SKULL);
        blocks.add(Blocks.ENCHANTING_TABLE);
        blocks.add(Blocks.ANVIL);
        blocks.add(Blocks.DRAGON_EGG);
        blocks.add(Blocks.JUKEBOX);
        blocks.add(Blocks.END_GATEWAY);
        blocks.add(Blocks.COBWEB);
        blocks.add(Blocks.NETHER_WART);
        blocks.add(Blocks.LADDER);
        blocks.add(Blocks.VINE);
        blocks.addAll(Blocks.DYED_SHULKER_BOX.asList());
        blocks.addAll(Blocks.BED.asList());
        return Set.copyOf(blocks);
    }
}
