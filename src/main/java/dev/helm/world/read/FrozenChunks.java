package dev.helm.world.read;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.world.level.chunk.LevelChunk;

import dev.helm.mixin.accessor.ChunkStorageAccess;
import dev.helm.mixin.accessor.ChunkStorageAccesses;

public final class FrozenChunks implements ChunkSource {

    private final Map<Long, LevelChunk> chunks;
    private final int residentCount;

    private FrozenChunks(Map<Long, LevelChunk> chunks, int residentCount) {
        this.chunks = chunks;
        this.residentCount = residentCount;
    }

    public static FrozenChunks capture(ClientChunkCache cache) {
        ChunkStorageAccess storage = ChunkStorageAccesses.storageOf(cache);
        if (storage == null) {
            return new FrozenChunks(Map.of(), 0);
        }
        var array = storage.helmChunks();
        Map<Long, LevelChunk> found = new HashMap<>(Math.max(16, array.length() * 2));
        int resident = 0;
        for (int slot = 0; slot < array.length(); slot++) {
            LevelChunk chunk = array.get(slot);
            if (chunk == null || chunk.isEmpty()) {
                continue;
            }
            resident++;
            found.put(key(chunk.getPos().x(), chunk.getPos().z()), chunk);
        }
        return new FrozenChunks(found, resident);
    }

    public static FrozenChunks empty() {
        return new FrozenChunks(Map.of(), 0);
    }

    public int residentCount() {
        return residentCount;
    }

    @Override
    public LevelChunk loaded(int chunkX, int chunkZ) {
        return chunks.get(key(chunkX, chunkZ));
    }

    @Override
    public boolean resident(int chunkX, int chunkZ) {
        return chunks.containsKey(key(chunkX, chunkZ));
    }

    private static long key(int chunkX, int chunkZ) {
        return (chunkX & 0xFFFFFFFFL) | ((chunkZ & 0xFFFFFFFFL) << 32);
    }
}