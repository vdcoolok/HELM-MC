package dev.helm.farm;

import java.util.List;
import java.util.Map;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class CropDrops {

    private static final Map<Block, List<Item>> TABLE = Map.ofEntries(
            Map.entry(Blocks.WHEAT, List.of(
                    net.minecraft.world.item.Items.WHEAT_SEEDS,
                    net.minecraft.world.item.Items.WHEAT)),
            Map.entry(Blocks.CARROTS, List.of(net.minecraft.world.item.Items.CARROT)),
            Map.entry(Blocks.POTATOES, List.of(net.minecraft.world.item.Items.POTATO)),
            Map.entry(Blocks.BEETROOTS, List.of(
                    net.minecraft.world.item.Items.BEETROOT_SEEDS,
                    net.minecraft.world.item.Items.BEETROOT)),
            Map.entry(Blocks.PUMPKIN, List.of(
                    net.minecraft.world.item.Items.PUMPKIN_SEEDS,
                    Blocks.PUMPKIN.asItem())),
            Map.entry(Blocks.MELON, List.of(
                    net.minecraft.world.item.Items.MELON_SEEDS,
                    net.minecraft.world.item.Items.MELON_SLICE,
                    Blocks.MELON.asItem())),
            Map.entry(Blocks.NETHER_WART, List.of(net.minecraft.world.item.Items.NETHER_WART)),
            Map.entry(Blocks.COCOA, List.of(net.minecraft.world.item.Items.COCOA_BEANS)),
            Map.entry(Blocks.SUGAR_CANE, List.of(Blocks.SUGAR_CANE.asItem())),
            Map.entry(Blocks.BAMBOO, List.of(Blocks.BAMBOO.asItem())),
            Map.entry(Blocks.CACTUS, List.of(Blocks.CACTUS.asItem())));

    private CropDrops() {
    }

    public static List<Item> of(Block crop) {
        return TABLE.getOrDefault(crop, List.of());
    }
}