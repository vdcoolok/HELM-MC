package dev.helm.world.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

public final class PackQueue {

    private final Map<ChunkPos, LevelChunk> waiting = new ConcurrentHashMap<>();
    private final LinkedBlockingQueue<ChunkPos> order = new LinkedBlockingQueue<>();
    private int limit;

    public PackQueue(int limit) {
        this.limit = limit;
    }

    public void limit(int limit) {
        this.limit = limit;
    }

    public void offer(LevelChunk chunk) {
        if (waiting.putIfAbsent(chunk.getPos(), chunk) == null) {
            order.add(chunk.getPos());
        }
    }

    public LevelChunk take() throws InterruptedException {
        ChunkPos pos = order.take();
        return waiting.remove(pos);
    }

    public int size() {
        return order.size();
    }

    public boolean full() {
        return order.size() > limit;
    }

    public void clear() {
        order.clear();
        waiting.clear();
    }
}
