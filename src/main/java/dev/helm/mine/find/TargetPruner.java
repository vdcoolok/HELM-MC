package dev.helm.mine.find;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

import net.minecraft.core.BlockPos;

import dev.helm.mine.target.TargetFilter;
import dev.helm.pathfinding.world.BlockView;
import dev.helm.pathfinding.world.block.WorkCosts;
import dev.helm.setting.MiningSettings;

public final class TargetPruner {

    private TargetPruner() {
    }

    public static List<BlockPos> prune(List<BlockPos> found, TargetFilter filter,
                                       BlockView world, WorkCosts work, MiningSettings settings,
                                       BlockPos player, List<BlockPos> unreachable,
                                       List<BlockPos> drops, int cap) {
        List<BlockPos> unique = distinct(found);
        List<BlockPos> loose = ShadowedDrops.by(drops, unique, filter, world, work);
        List<BlockPos> kept = new ArrayList<>();
        for (BlockPos pos : unique) {
            if (unreachable.contains(pos)) {
                continue;
            }
            if (pos.getY() > settings.highestLevel()) {
                continue;
            }
            if (pos.getY() < settings.lowestLevel() + world.lowestLevel()) {
                continue;
            }
            if (!stillWanted(world, filter, pos, loose)) {
                continue;
            }
            if (!Breakable.worthMining(world, work, pos)) {
                continue;
            }
            if (settings.onlyExposed() && !Breakable.touchingOpenSpace(world, pos, settings)) {
                continue;
            }
            kept.add(pos);
        }
        kept.sort(Comparator.comparingDouble(pos -> pos.distSqr(player)));
        if (kept.size() <= cap) {
            return kept;
        }
        return List.copyOf(kept.subList(0, cap));
    }

    private static List<BlockPos> distinct(List<BlockPos> found) {
        return new ArrayList<>(new LinkedHashSet<>(found));
    }

    private static boolean stillWanted(BlockView world, TargetFilter filter, BlockPos pos,
                                       List<BlockPos> loose) {
        return !world.loaded(pos.getX(), pos.getZ())
                || filter.wants(world.stateAt(pos.getX(), pos.getY(), pos.getZ()))
                || loose.contains(pos);
    }
}