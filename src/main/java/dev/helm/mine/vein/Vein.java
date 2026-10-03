package dev.helm.mine.vein;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;

import dev.helm.mine.target.TargetFilter;
import dev.helm.pathfinding.world.BlockView;

public final class Vein {

    private Vein() {
    }

    public static List<BlockPos> around(List<BlockPos> known, BlockView world,
                                        TargetFilter filter, BlockPos seed) {
        Set<BlockPos> members = new LinkedHashSet<>();
        List<BlockPos> queue = new ArrayList<>();
        queue.add(seed);
        while (!queue.isEmpty()) {
            BlockPos at = queue.remove(queue.size() - 1);
            if (!members.add(at)) {
                continue;
            }
            for (BlockPos next : touching(at)) {
                if (members.contains(next)) {
                    continue;
                }
                if (!near(known, next)) {
                    continue;
                }
                if (filter.wants(world.stateAt(next.getX(), next.getY(), next.getZ()))) {
                    queue.add(next);
                }
            }
        }
        return List.copyOf(members);
    }

    private static boolean near(List<BlockPos> known, BlockPos pos) {
        for (BlockPos candidate : known) {
            if (candidate.equals(pos)) {
                return true;
            }
        }
        return false;
    }

    private static List<BlockPos> touching(BlockPos pos) {
        return List.of(pos.above(), pos.below(), pos.north(), pos.south(),
                pos.east(), pos.west());
    }
}