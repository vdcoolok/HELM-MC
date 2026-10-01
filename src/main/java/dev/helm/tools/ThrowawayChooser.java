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
            Blocks.STONE
    };

    private ThrowawayChooser() {
    }

    public static boolean selectFor(boolean select) {
        PlayerInventory inventory = inventory();
        int slot = bestSlot(inventory);
        if (slot < 0) {
            return false;
        }
        if (select && inventory.selectedSlot() != slot) {
            inventory.selectSlot(slot);
        }
        return true;
    }

    private static PlayerInventory inventory() {
        var client = net.minecraft.client.Minecraft.getInstance();
        if (client.player == null) {
            throw new IllegalStateException("no player");
        }
        return new PlayerInventory(client.player);
    }

    private static int bestSlot(PlayerInventory inventory) {
        int anyBlock = -1;
        for (int slot = 0; slot < 9; slot++) {
            Block block = placeableFrom(inventory.slot(slot));
            if (block == null) {
                continue;
            }
            if (anyBlock < 0) {
                anyBlock = slot;
            }
            for (Block wanted : PREFERRED) {
                if (block == wanted) {
                    return slot;
                }
            }
        }
        return anyBlock;
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
