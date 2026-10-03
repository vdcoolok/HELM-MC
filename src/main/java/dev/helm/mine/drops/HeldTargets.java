package dev.helm.mine.drops;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

import dev.helm.mine.target.TargetFilter;

public final class HeldTargets {

    private HeldTargets() {
    }

    public static int carried(LocalPlayer player, TargetFilter filter) {
        int carried = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (DroppedTargets.wanted(stack, filter)) {
                carried += stack.getCount();
            }
        }
        return carried;
    }
}