package dev.helm.tools;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import dev.helm.inventory.HotbarFetcher;
import dev.helm.inventory.InventorySlots;
import dev.helm.setting.Settings;
import dev.helm.world.PlayerInventory;

public final class ThrowawayChooser {

    private ThrowawayChooser() {
    }

    public static boolean isPlaceable(ItemStack stack) {
        return placeableFrom(stack) != null;
    }

    public static boolean isPreferred(ItemStack stack) {
        Block block = placeableFrom(stack);
        return block != null && preferred().contains(block);
    }

    public static int chosen(InventoryView inventory) {
        return best(inventory, 0, InventorySlots.HOTBAR, ThrowawayChooser::isPreferred);
    }

    public static boolean selectFor(boolean select) {
        PlayerInventory inventory = inventory();
        int slot = chosen(inventory);
        if (slot < 0 && select) {
            slot = HotbarFetcher.bringUp(player(), ThrowawayChooser::isPreferred);
        }
        if (slot < 0) {
            slot = best(inventory, 0, InventorySlots.HOTBAR, ThrowawayChooser::isPlaceable);
        }
        if (slot < 0 && select) {
            slot = HotbarFetcher.bringUp(player(), ThrowawayChooser::isPlaceable);
        }
        if (slot < 0) {
            return false;
        }
        if (select && inventory.selectedSlot() != slot) {
            inventory.selectSlot(slot);
        }
        return true;
    }

    private static int best(InventoryView inventory, int from, int to,
                            Predicate<ItemStack> wanted) {
        for (int slot = from; slot < to; slot++) {
            if (wanted.test(inventory.slot(slot))) {
                return slot;
            }
        }
        return -1;
    }

    private static List<Block> preferred() {
        return Settings.holder().movement().placementBlocks();
    }

    private static PlayerInventory inventory() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            throw new IllegalStateException("no player");
        }
        return new PlayerInventory(player);
    }

    private static LocalPlayer player() {
        return Minecraft.getInstance().player;
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