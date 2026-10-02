package dev.helm.inventory;

import java.util.OptionalInt;
import java.util.function.Predicate;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

import dev.helm.world.PlayerInventory;

public final class HotbarFetcher {

    private HotbarFetcher() {
    }

    public static int bringUp(LocalPlayer player, Predicate<ItemStack> wanted) {
        int packed = packed(player, wanted);
        if (packed < 0) {
            return -1;
        }
        OptionalInt spare = HotbarSpace.spare(player);
        if (spare.isEmpty()) {
            return -1;
        }
        int destination = spare.getAsInt();
        return SlotSwapper.instance().move(packed, destination) ? destination : -1;
    }

    public static int packed(LocalPlayer player, Predicate<ItemStack> wanted) {
        if (!PlayerContainer.mayRearrange(player)) {
            return -1;
        }
        PlayerInventory inventory = new PlayerInventory(player);
        for (int slot = InventorySlots.FIRST_PACKED; slot < InventorySlots.CARRIED; slot++) {
            if (wanted.test(inventory.slot(slot))) {
                return slot;
            }
        }
        return -1;
    }
}