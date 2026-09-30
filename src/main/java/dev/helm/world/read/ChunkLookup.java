package dev.helm.world.read;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public final class ChunkLookup {

    private final ClientLevel level;
    private LevelChunk memo;
    private int memoX;
    private int memoZ;

    public ChunkLookup(ClientLevel level) {
        this.level = level;
    }

    public LevelChunk loaded(int chunkX, int chunkZ) {
        if (memo != null && memoX == chunkX && memoZ == chunkZ) {
            return memo;
        }
        LevelChunk chunk = level.getChunkSource().getChunk(chunkX, chunkZ,
                ChunkStatus.FULL, false);
        if (chunk == null || chunk.isEmpty()) {
            return null;
        }
        remember(chunk);
        return chunk;
    }

    public boolean resident(int chunkX, int chunkZ) {
        return level.getChunkSource().hasChunk(chunkX, chunkZ);
    }

    private void remember(LevelChunk chunk) {
        this.memo = chunk;
        this.memoX = chunk.getPos().x();
        this.memoZ = chunk.getPos().z();
    }
}
