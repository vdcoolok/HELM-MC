package dev.helm.farm;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class FarmSurvey {

    private FarmSurvey() {
    }

    public static FarmFindings classify(Level level, List<BlockPos> scanned, FarmArea area) {
        List<BlockPos> harvest = new ArrayList<>();
        List<BlockPos> farmland = new ArrayList<>();
        List<BlockPos> soulSand = new ArrayList<>();
        List<BlockPos> logs = new ArrayList<>();
        List<BlockPos> boneMeal = new ArrayList<>();

        for (BlockPos pos : scanned) {
            if (!area.covers(pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() == Blocks.FARMLAND) {
                if (clearAbove(level, pos)) {
                    farmland.add(pos);
                }
                continue;
            }
            if (state.getBlock() == Blocks.SOUL_SAND) {
                if (clearAbove(level, pos)) {
                    soulSand.add(pos);
                }
                continue;
            }
            if (state.getBlock() == Blocks.JUNGLE_LOG) {
                if (openSide(level, pos)) {
                    logs.add(pos);
                }
                continue;
            }
            if (Crops.ripe(level, pos, state)) {
                harvest.add(pos);
                continue;
            }
            if (wantsBoneMeal(level, pos, state)) {
                boneMeal.add(pos);
            }
        }
        return new FarmFindings(harvest, farmland, soulSand, logs, boneMeal);
    }

    public static boolean clearAbove(Level level, BlockPos pos) {
        return level.getBlockState(pos.above()).getBlock() instanceof AirBlock;
    }

    public static boolean openSide(Level level, BlockPos pos) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(pos.relative(side)).getBlock() instanceof AirBlock) {
                return true;
            }
        }
        return false;
    }

    private static boolean wantsBoneMeal(Level level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof BonemealableBlock plant)) {
            return false;
        }
        return plant.isValidBonemealTarget(level, pos, state)
                && plant.isBonemealSuccess(level, level.getRandom(), pos, state);
    }
}