package dev.helm.world.cache;

import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import dev.helm.diag.Trace;
import dev.helm.setting.CacheSettings;
import dev.helm.setting.Settings;
import dev.helm.storage.HelmStorage;

public final class WorldCache {

    private static final String ROOT = "cache";
    private static final long FIRST_SAVE_DELAY_MILLIS = 30_000L;
    private static final long SAVE_INTERVAL_MILLIS = 600_000L;

    private static WorldCache current;

    private final RegionStore store;
    private final PackQueue queue;
    private final Thread packer;
    private final Thread saver;
    private volatile boolean running = true;

    private WorldCache(Path directory, ClientLevel level) {
        this.store = new RegionStore(directory, level.dimensionType(), level.dimension());
        CacheSettings settings = Settings.holder().cache();
        this.queue = new PackQueue(settings.queueLimit());
        this.packer = new Thread(new Packer(queue, store), "helm-chunk-packer");
        this.saver = new Thread(this::saveLoop, "helm-chunk-saver");
        this.packer.setDaemon(true);
        this.saver.setDaemon(true);
    }

    public static WorldCache open(ClientLevel level) {
        close();
        Path directory = directoryFor(level);
        if (directory == null) {
            return null;
        }
        WorldCache opened = new WorldCache(directory, level);
        opened.start();
        current = opened;
        Trace.instance().event("cache", "opened at " + directory);
        return opened;
    }

    public static WorldCache get() {
        return current;
    }

    public static void close() {
        WorldCache open = current;
        current = null;
        if (open != null) {
            open.shutdown();
        }
    }

    public RegionStore store() {
        return store;
    }

    public void queue(LevelChunk chunk) {
        if (running && chunk != null && !chunk.isEmpty()) {
            queue.offer(chunk);
        }
    }

    public void remember(int chunkX, int chunkZ, ClientLevel level) {
        queue(level.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false));
    }

    public void tick(int feetX, int feetZ) {
        CacheSettings settings = Settings.holder().cache();
        queue.limit(settings.queueLimit());
        if (settings.expirySeconds() >= 0) {
            store.expire(System.currentTimeMillis() - settings.expirySeconds() * 1000L);
        }
        if (settings.pruneFromMemory()) {
            store.prune(feetX, feetZ);
        }
    }

    private void start() {
        packer.start();
        saver.start();
    }

    private void shutdown() {
        running = false;
        queue.clear();
        packer.interrupt();
        saver.interrupt();
        store.save();
        Trace.instance().event("cache", "closed, " + store.packedChunks() + " chunks held in "
                + store.regionCount() + " regions");
    }

    private void saveLoop() {
        try {
            Thread.sleep(FIRST_SAVE_DELAY_MILLIS);
            while (running) {
                store.save();
                Thread.sleep(SAVE_INTERVAL_MILLIS);
            }
        } catch (InterruptedException stopping) {
            Thread.currentThread().interrupt();
        }
    }

    private static Path directoryFor(ClientLevel level) {
        try {
            ResourceKey<Level> dimension = level.dimension();
            Path root = HelmStorage.root().resolve(ROOT);
            Path directory = root.resolve(dimension.identifier().getNamespace())
                    .resolve(dimension.identifier().getPath() + "_"
                            + level.dimensionType().logicalHeight());
            java.nio.file.Files.createDirectories(directory);
            return directory;
        } catch (IOException unwritable) {
            Trace.instance().event("cache",
                    "no cache directory available, the cache is off: " + unwritable);
            return null;
        }
    }
}
