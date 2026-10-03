package dev.helm.mine.find;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;

import dev.helm.mine.target.TargetFilter;
import dev.helm.pathfinding.world.BlockView;
import dev.helm.pathfinding.world.block.WorkCosts;

public final class ShadowedDrops {

    private static final int SHADOW_RADIUS_SQUARED = 9;

    private ShadowedDrops() {
    }

    public static List<BlockPos> by(List<BlockPos> drops, List<BlockPos> candidates,
                                    TargetFilter filter, BlockView world, WorkCosts work) {
        Set<BlockPos> shadowed = new LinkedHashSet<>();
        for (BlockPos drop : drops) {
            for (BlockPos pos : candidates) {
                if (pos.distSqr(drop) > SHADOW_RADIUS_SQUARED) {
                    continue;
                }
                if (filter.wants(world.stateAt(pos.getX(), pos.getY(), pos.getZ()))
                        && Breakable.worthMining(world, work, pos)) {
                    shadowed.add(drop);
                    break;
                }
            }
        }
        return drops.stream().filter(drop -> !shadowed.contains(drop)).toList();
    }
}