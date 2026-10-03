package dev.helm.farm;

import java.util.List;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class FarmItems {

    private static final List<Item> PLANTABLE = List.of(
            Items.BEETROOT_SEEDS,
            Items.MELON_SEEDS,
            Items.WHEAT_SEEDS,
            Items.PUMPKIN_SEEDS,
            Items.POTATO,
            Items.CARROT);

    private FarmItems() {
    }

    public static boolean plantable(ItemStack stack) {
        return PLANTABLE.contains(stack.getItem());
    }

    public static boolean netherWart(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == Items.NETHER_WART;
    }

    public static boolean cocoaBeans(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == Items.COCOA_BEANS;
    }

    public static boolean boneMeal(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == Items.BONE_MEAL;
    }
}