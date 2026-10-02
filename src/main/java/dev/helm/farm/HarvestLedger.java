package dev.helm.farm;

import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class HarvestLedger {

    private final Set<Item> produced = new LinkedHashSet<>();

    public void record(Block crop) {
        produced.addAll(CropDrops.of(crop));
    }

    public boolean wants(ItemStack dropped) {
        return produced.contains(dropped.getItem());
    }

    public boolean empty() {
        return produced.isEmpty();
    }

    public void clear() {
        produced.clear();
    }
}