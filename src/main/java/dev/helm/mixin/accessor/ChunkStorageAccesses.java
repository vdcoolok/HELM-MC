package dev.helm.mixin.accessor;

import net.minecraft.client.multiplayer.ClientChunkCache;

public final class ChunkStorageAccesses {

    private ChunkStorageAccesses() {
    }

    public static ChunkStorageAccess storageOf(ClientChunkCache cache) {
        Object storage = ((ChunkCacheAccess) cache).helmStorage();
        return storage instanceof ChunkStorageAccess access ? access : null;
    }
}