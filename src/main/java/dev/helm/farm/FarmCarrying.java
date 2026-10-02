package dev.helm.farm;

import java.util.function.Predicate;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import dev.helm.world.PlayerInventory;

public final class FarmCarrying {

    private static final int HOTBAR_SLOTS = 9;

    private FarmCarrying() {
    }

    public static boolean carried(LocalPlayer player, Predicate<ItemStack> wanted) {
        PlayerInventory inventory = new PlayerInventory(player);
        for (int slot = 0; slot < HOTBAR_SLOTS; slot++) {
            if (wanted.test(inventory.slot(slot))) {
                return true;
            }
        }
        return wanted.test(player.getItemBySlot(EquipmentSlot.OFFHAND));
    }

    public static boolean ready(LocalPlayer player, Predicate<ItemStack> wanted) {
        PlayerInventory inventory = new PlayerInventory(player);
        for (int slot = 0; slot < HOTBAR_SLOTS; slot++) {
            if (wanted.test(inventory.slot(slot))) {
                inventory.selectSlot(slot);
                return true;
            }
        }
        if (!wanted.test(player.getItemBySlot(EquipmentSlot.OFFHAND))) {
            return false;
        }
        for (int slot = 0; slot < HOTBAR_SLOTS; slot++) {
            ItemStack held = inventory.slot(slot);
            if (held.isEmpty() || FarmItems.tool(held)) {
                inventory.selectSlot(slot);
                return true;
            }
        }
        return false;
    }
}