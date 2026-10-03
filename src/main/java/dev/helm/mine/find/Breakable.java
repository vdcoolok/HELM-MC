package dev.helm.mine.find;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import dev.helm.pathfinding.world.BlockView;
import dev.helm.pathfinding.world.block.WorkCosts;
import dev.helm.setting.MiningSettings;

public final class Breakable {

    private Breakable() {
    }

    public static boolean worthMining(BlockView world, WorkCosts work, BlockPos pos) {
        if (work.avoidBreaking(pos.getX(), pos.getY(), pos.getZ(),
                world.stateAt(pos.getX(), pos.getY(), pos.getZ()))) {
            return false;
        }
        if (work.breakTicks(pos.getX(), pos.getY(), pos.getZ(), true)
                >= dev.helm.pathfinding.cost.MoveCosts.IMPOSSIBLE) {
            return false;
        }
        Block above = world.stateAt(pos.getX(), pos.getY() + 1, pos.getZ()).getBlock();
        Block below = world.stateAt(pos.getX(), pos.getY() - 1, pos.getZ()).getBlock();
        return above != Blocks.BEDROCK || below != Blocks.BEDROCK;
    }

    public static boolean touchingOpenSpace(BlockView world, BlockPos pos,
                                            MiningSettings settings) {
        int radius = settings.exposedRadius();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > radius) {
                        continue;
                    }
                    Block block = world.stateAt(pos.getX() + dx, pos.getY() + dy,
                            pos.getZ() + dz).getBlock();
                    if (open(block)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean open(Block block) {
        return block instanceof net.minecraft.world.level.block.AirBlock
                || block == Blocks.LAVA
                || block == Blocks.WATER;
    }
}