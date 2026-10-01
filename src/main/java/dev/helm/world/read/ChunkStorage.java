package dev.helm.world.read;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReferenceArray;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.world.level.chunk.LevelChunk;

import dev.helm.diag.Trace;

public final class ChunkStorage {

    private static boolean resolved;
    private static Field storage;
    private static Field chunks;

    private ChunkStorage() {
    }

    @SuppressWarnings("unchecked")
    public static AtomicReferenceArray<LevelChunk> loadedChunks(ClientChunkCache cache) {
        if (!resolve()) {
            return null;
        }
        try {
            Object holder = storage.get(cache);
            if (holder == null) {
                return null;
            }
            return (AtomicReferenceArray<LevelChunk>) chunks.get(holder);
        } catch (IllegalAccessException | RuntimeException unreadable) {
            Trace.instance().event("search",
                    "the loaded chunk array could not be read: " + unreadable);
            return null;
        }
    }

    private static synchronized boolean resolve() {
        if (resolved) {
            return storage != null && chunks != null;
        }
        resolved = true;
        try {
            storage = field(ClientChunkCache.class, "storage");
            if (storage == null) {
                return missing("no storage field on the chunk cache");
            }
            chunks = field(storage.getType(), "chunks");
            if (chunks == null) {
                return missing("no chunk array on the chunk cache's storage");
            }
            return true;
        } catch (RuntimeException | LinkageError broken) {
            storage = null;
            chunks = null;
            return missing("the chunk cache could not be inspected: " + broken);
        }
    }

    private static Field field(Class<?> owner, String name) {
        for (Field candidate : owner.getDeclaredFields()) {
            if (candidate.getName().equals(name)) {
                candidate.setAccessible(true);
                return candidate;
            }
        }
        return null;
    }

    private static boolean missing(String why) {
        Trace.instance().event("search", why + ", so searches fall back to the stored chunk cache");
        storage = null;
        chunks = null;
        return false;
    }
}