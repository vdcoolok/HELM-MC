package dev.helm.farm;

import java.util.function.Predicate;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import dev.helm.inventory.HotbarFetcher;
import dev.helm.inventory.InventorySlots;
import dev.helm.tools.ToolSet;
import dev.helm.world.PlayerInventory;

public final class FarmCarrying {

    private FarmCarrying() {
    }

    public static boolean carried(LocalPlayer player, Predicate<ItemStack> wanted) {
        PlayerInventory inventory = new PlayerInventory(player);
        for (int slot = 0; slot < InventorySlots.HOTBAR; slot++) {
            if (wanted.test(inventory.slot(slot))) {
                return true;
            }
        }
        if (wanted.test(player.getItemBySlot(EquipmentSlot.OFFHAND))) {
            return true;
        }
        return HotbarFetcher.packed(player, wanted) >= 0;
    }

    public static boolean ready(LocalPlayer player, Predicate<ItemStack> wanted) {
        PlayerInventory inventory = new PlayerInventory(player);
        for (int slot = 0; slot < InventorySlots.HOTBAR; slot++) {
            if (wanted.test(inventory.slot(slot))) {
                inventory.selectSlot(slot);
                return true;
            }
        }
        if (!wanted.test(player.getItemBySlot(EquipmentSlot.OFFHAND))) {
            return HotbarFetcher.bringUp(player, wanted) >= 0;
        }
        for (int slot = 0; slot < InventorySlots.HOTBAR; slot++) {
            ItemStack held = inventory.slot(slot);
            if (held.isEmpty() || ToolSet.isTool(held)) {
                inventory.selectSlot(slot);
                return true;
            }
        }
        return false;
    }
}