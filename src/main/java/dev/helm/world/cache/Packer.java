package dev.helm.world.cache;

import net.minecraft.world.level.chunk.LevelChunk;
import dev.helm.diag.Trace;

public final class Packer implements Runnable {

    private final PackQueue queue;
    private final RegionStore store;

    public Packer(PackQueue queue, RegionStore store) {
        this.queue = queue;
        this.store = store;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                LevelChunk chunk = queue.take();
                if (chunk == null || queue.full()) {
                    continue;
                }
                store.store(ChunkPacker.pack(chunk));
            } catch (InterruptedException stopping) {
                Thread.currentThread().interrupt();
                return;
            } catch (RuntimeException failure) {
                Trace.instance().event("cache", "a chunk could not be packed: " + failure);
            }
        }
    }
}
