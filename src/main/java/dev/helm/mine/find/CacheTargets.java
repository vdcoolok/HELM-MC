package dev.helm.mine.find;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import dev.helm.mine.target.TargetFilter;
import dev.helm.world.cache.BlockNames;
import dev.helm.world.cache.CachedRegion;
import dev.helm.world.cache.TrackedBlocks;
import dev.helm.world.cache.WorldCache;

public final class CacheTargets {

    private CacheTargets() {
    }

    public static List<BlockPos> around(TargetFilter filter, BlockPos player, int limit,
                                        int radius) {
        WorldCache cache = WorldCache.get();
        if (cache == null) {
            return List.of();
        }
        List<BlockPos> found = new ArrayList<>();
        int centreX = player.getX() >> 9;
        int centreZ = player.getZ() >> 9;
        for (Block block : filter.blocks()) {
            if (!TrackedBlocks.is(block)) {
                continue;
            }
            String name = BlockNames.of(block);
            for (int ring = 0; ring <= radius; ring++) {
                collectRing(cache, centreX, centreZ, ring, name, found);
                if (found.size() >= limit) {
                    return found;
                }
            }
        }
        return found;
    }

    private static void collectRing(WorldCache cache, int centreX, int centreZ, int ring,
                                    String name, List<BlockPos> found) {
        for (int xOff = -ring; xOff <= ring; xOff++) {
            for (int zOff = -ring; zOff <= ring; zOff++) {
                if (xOff * xOff + zOff * zOff != ring) {
                    continue;
                }
                CachedRegion region = cache.store().region(centreX + xOff, centreZ + zOff);
                if (region == null) {
                    continue;
                }
                for (int[] position : region.trackedPositions(name)) {
                    found.add(new BlockPos(position[0], position[1], position[2]));
                }
            }
        }
    }
}