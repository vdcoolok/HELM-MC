package dev.helm.tools;

import net.minecraft.world.item.ItemStack;

public interface InventoryView {

    ItemStack slot(int index);

    int selectedSlot();

    void selectSlot(int index);
}
