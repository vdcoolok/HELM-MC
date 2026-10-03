package dev.helm.mine;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import dev.helm.world.cache.WorldCache;

public final class ChunkRemembering {

    private ChunkRemembering() {
    }

    public static int around(WorldCache cache, ClientLevel level, LocalPlayer player,
                             int radiusInChunks) {
        int chunkX = player.getBlockX() >> 4;
        int chunkZ = player.getBlockZ() >> 4;
        int queued = 0;
        for (int x = chunkX - radiusInChunks; x <= chunkX + radiusInChunks; x++) {
            for (int z = chunkZ - radiusInChunks; z <= chunkZ + radiusInChunks; z++) {
                queued += remember(cache, level, x, z);
            }
        }
        return queued;
    }

    private static int remember(WorldCache cache, ClientLevel level, int chunkX, int chunkZ) {
        if (level.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false) == null) {
            return 0;
        }
        cache.remember(chunkX, chunkZ, level);
        return 1;
    }
}