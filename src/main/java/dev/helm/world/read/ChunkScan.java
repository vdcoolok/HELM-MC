package dev.helm.world.read;

import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;

public final class ChunkScan {

    private ChunkScan() {
    }

    public static boolean into(int chunkX, int chunkZ, int lowest, ChunkAccess chunk,
                               int[] sectionOrder, Set<Block> wanted, List<BlockPos> found) {
        LevelChunkSection[] sections = chunk.getSections();
        for (int section : sectionOrder) {
            LevelChunkSection layer = sections[section];
            if (layer == null || layer.hasOnlyAir()) {
                continue;
            }
            int sectionBase = section << 4;
            for (int offset = 0; offset < 16; offset++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        BlockState state = layer.getStates().get(x, offset, z);
                        if (!wanted.contains(state.getBlock())) {
                            continue;
                        }
                        found.add(new BlockPos(chunkX | x, sectionBase + offset + lowest,
                                chunkZ | z));
                    }
                }
            }
        }
        return false;
    }
}