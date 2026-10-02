package dev.helm.world.read;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;

public final class BlockScan {

    private BlockScan() {
    }

    public static List<BlockPos> around(ClientLevel level, BlockPos from, ChunkScanRequest want) {
        List<BlockPos> found = new ArrayList<>();
        if (want.wanted().isEmpty()) {
            return found;
        }
        int lowest = level.getMinY();
        int standingLevel = from.getY() - lowest;
        int[] sectionOrder = LevelOrder.nearestFirst(level.getHeight() / 16, standingLevel);
        int originX = from.getX() >> 4;
        int originZ = from.getZ() >> 4;
        int furthest = want.chunkRadius() * want.chunkRadius();
        boolean withinLevel = false;

        for (int ring = 0; ; ring++) {
            boolean considered = false;
            boolean allUnloaded = true;
            for (int[] offset : Ring.at(ring)) {
                considered = true;
                int chunkX = originX + offset[0];
                int chunkZ = originZ + offset[1];
                LevelChunk chunk = level.getChunkSource().getChunk(chunkX, chunkZ, null, false);
                if (chunk == null) {
                    continue;
                }
                allUnloaded = false;
                withinLevel |= ChunkScan.into(chunkX << 4, chunkZ << 4, lowest, chunk,
                        sectionOrder, want.wanted(), found, want.maxResults(),
                        want.levelWindow(), standingLevel);
            }
            if (considered && allUnloaded) {
                return found;
            }
            if (found.size() >= want.maxResults()
                    && (ring > furthest || (ring > 1 && withinLevel))) {
                return found;
            }
        }
    }
}