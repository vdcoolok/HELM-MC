package dev.helm.drops;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class DropScan {

    private static final double REACH = 1.0D;

    private DropScan() {
    }

    public static void learn(ClientLevel level, BreakLedger ledger, DropInterest interest,
                             long tick, long window) {
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity dropped)) {
                continue;
            }
            ItemStack stack = dropped.getItem();
            if (stack.isEmpty()) {
                continue;
            }
            Item item = stack.getItem();
            if (interest.wanted(item, tick, window)) {
                continue;
            }
            if (ledger.holds(dropped.getX(), dropped.getY(), dropped.getZ(), REACH)) {
                interest.learn(item, tick);
            }
        }
    }
}
