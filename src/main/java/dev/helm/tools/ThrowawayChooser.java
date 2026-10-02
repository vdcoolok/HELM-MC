package dev.helm.tools;

import java.util.List;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import dev.helm.world.PlayerInventory;

public final class ThrowawayChooser {

    private ThrowawayChooser() {
    }

    public static boolean selectFor(boolean select) {
        PlayerInventory inventory = inventory();
        int slot = bestSlot(inventory, preferred());
        if (slot < 0) {
            return false;
        }
        if (select && inventory.selectedSlot() != slot) {
            inventory.selectSlot(slot);
        }
        return true;
    }

    private static List<Block> preferred() {
        return dev.helm.setting.Settings.holder().movement().placementBlocks();
    }

    private static PlayerInventory inventory() {
        var client = net.minecraft.client.Minecraft.getInstance();
        if (client.player == null) {
            throw new IllegalStateException("no player");
        }
        return new PlayerInventory(client.player);
    }

    private static int bestSlot(PlayerInventory inventory, List<Block> preferred) {
        int anyBlock = -1;
        for (int slot = 0; slot < 9; slot++) {
            Block block = placeableFrom(inventory.slot(slot));
            if (block == null) {
                continue;
            }
            if (anyBlock < 0) {
                anyBlock = slot;
            }
            for (Block wanted : preferred) {
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
