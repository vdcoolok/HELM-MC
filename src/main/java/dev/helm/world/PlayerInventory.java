package dev.helm.world;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import dev.helm.tools.PlayerInventoryView;

public final class PlayerInventory implements PlayerInventoryView {

    private final Player player;

    public PlayerInventory(Player player) {
        this.player = player;
    }

    @Override
    public Player player() {
        return player;
    }

    @Override
    public ItemStack slot(int index) {
        return player.getInventory().getItem(index);
    }

    @Override
    public int selectedSlot() {
        return player.getInventory().getSelectedSlot();
    }

    @Override
    public void selectSlot(int index) {
        Inventory inventory = player.getInventory();
        if (inventory.getSelectedSlot() != index) {
            inventory.setSelectedSlot(index);
        }
    }
}
