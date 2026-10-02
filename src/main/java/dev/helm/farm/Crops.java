package dev.helm.farm;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import dev.helm.setting.Settings;

public final class Crops {

    private static List<ReadyCrop> catalogue = List.of();
    private static boolean catalogueReplants;

    private Crops() {
    }

    public static Set<Block> watched() {
        return ripe().stream().map(ReadyCrop::block).collect(Collectors.toUnmodifiableSet());
    }

    public static boolean ripe(Level level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        for (ReadyCrop crop : ripe()) {
            if (crop.block() == block) {
                return crop.ready().ready(level, pos, state);
            }
        }
        return false;
    }

    private static List<ReadyCrop> ripe() {
        boolean replants = Settings.holder().farm().replant();
        if (catalogue.isEmpty() || catalogueReplants != replants) {
            catalogueReplants = replants;
            catalogue = catalogueOf(replants);
        }
        return catalogue;
    }

    private static List<ReadyCrop> catalogueOf(boolean replants) {
        return List.of(
                new ReadyCrop(Blocks.WHEAT, atMaxAge()),
                new ReadyCrop(Blocks.CARROTS, atMaxAge()),
                new ReadyCrop(Blocks.POTATOES, atMaxAge()),
                new ReadyCrop(Blocks.BEETROOTS, atMaxAge()),
                new ReadyCrop(Blocks.PUMPKIN, whenever()),
                new ReadyCrop(Blocks.MELON, whenever()),
                new ReadyCrop(Blocks.NETHER_WART, atLeast(NetherWartBlock.AGE, 3)),
                new ReadyCrop(Blocks.COCOA, atLeast(CocoaBlock.AGE, 2)),
                new ReadyCrop(Blocks.SUGAR_CANE, topOfStack(SugarCaneBlock.class, replants)),
                new ReadyCrop(Blocks.BAMBOO, topOfStack(BambooStalkBlock.class, replants)),
                new ReadyCrop(Blocks.CACTUS, topOfStack(CactusBlock.class, replants)));
    }

    private static Ripe atMaxAge() {
        return (level, pos, state) -> ((CropBlock) state.getBlock()).isMaxAge(state);
    }

    private static Ripe atLeast(IntegerProperty age, int wanted) {
        return (level, pos, state) -> state.getValue(age) >= wanted;
    }

    private static Ripe whenever() {
        return (level, pos, state) -> true;
    }

    private static Ripe topOfStack(Class<?> stem, boolean replants) {
        if (!replants) {
            return whenever();
        }
        return (level, pos, state) -> stem.isInstance(level.getBlockState(pos.below()).getBlock());
    }

    public record ReadyCrop(Block block, Ripe ready) {
    }

    public interface Ripe {

        boolean ready(Level level, BlockPos pos, BlockState state);
    }
}