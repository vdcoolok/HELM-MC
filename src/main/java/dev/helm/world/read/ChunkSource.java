package dev.helm.world.read;

import net.minecraft.world.level.chunk.LevelChunk;

public interface ChunkSource {

    LevelChunk loaded(int chunkX, int chunkZ);

    boolean resident(int chunkX, int chunkZ);
}