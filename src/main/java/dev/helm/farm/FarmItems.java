package dev.helm.farm;

import java.util.List;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public final class FarmItems {

    private static final List<Item> PLANTABLE = List.of(
            Items.BEETROOT_SEEDS,
            Items.MELON_SEEDS,
            Items.WHEAT_SEEDS,
            Items.PUMPKIN_SEEDS,
            Items.POTATO,
            Items.CARROT);

    private static final List<Item> WORTH_COLLECTING = List.of(
            Items.BEETROOT_SEEDS,
            Items.BEETROOT,
            Items.MELON_SEEDS,
            Items.MELON_SLICE,
            Items.WHEAT_SEEDS,
            Items.WHEAT,
            Items.PUMPKIN_SEEDS,
            Items.POTATO,
            Items.CARROT,
            Items.NETHER_WART,
            Items.COCOA_BEANS,
            Blocks.MELON.asItem(),
            Blocks.PUMPKIN.asItem(),
            Blocks.SUGAR_CANE.asItem(),
            Blocks.BAMBOO.asItem(),
            Blocks.CACTUS.asItem());

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

    public static boolean worthCollecting(ItemStack dropped) {
        return WORTH_COLLECTING.contains(dropped.getItem());
    }
}