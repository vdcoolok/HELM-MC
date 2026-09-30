package dev.helm.tools;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import dev.helm.world.PlayerInventory;

public final class ThrowawayChooser {

    private static final Block[] PREFERRED = {
            Blocks.DIRT,
            Blocks.COBBLESTONE,
            Blocks.NETHERRACK,
            Blocks.GRAVEL,
            Blocks.OAK_PLANKS,
            Blocks.STONE
    };

    private ThrowawayChooser() {
    }

    public static boolean selectFor(boolean acceptSelected, int x, int y, int z) {
        var client = net.minecraft.client.Minecraft.getInstance();
        if (client.player == null) {
            return false;
        }
        PlayerInventory inventory = new PlayerInventory(client.player);
        if (acceptSelected && !inventory.slot(inventory.selectedSlot()).isEmpty()) {
            return true;
        }
        int slot = bestSlot(inventory);
        if (slot < 0) {
            return false;
        }
        inventory.selectSlot(slot);
        return true;
    }

    private static int bestSlot(PlayerInventory inventory) {
        int fallback = -1;
        for (int slot = 0; slot < 9; slot++) {
            Block block = placeableFrom(inventory.slot(slot));
            if (block == null) {
                continue;
            }
            if (fallback < 0) {
                fallback = slot;
            }
            for (Block wanted : PREFERRED) {
                if (block == wanted) {
                    return slot;
                }
            }
        }
        return fallback;
    }

    public static Block placeableFrom(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        if (stack.getItem() instanceof BlockItem item) {
            return item.getBlock();
        }
        return null;
    }
}
