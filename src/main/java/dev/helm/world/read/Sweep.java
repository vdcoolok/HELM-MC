package dev.helm.world.read;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;

public final class Sweep {

    private final ClientLevel level;
    private final ChunkScanRequest want;
    private final int lowest;
    private final int standingLevel;
    private final int[] sectionOrder;
    private final int originX;
    private final int originZ;
    private final int furthest;
    private final List<BlockPos> found = new ArrayList<>();

    private int ring;
    private List<int[]> offsets = Ring.at(0);
    private int cursor;
    private boolean considered;
    private boolean allUnloaded = true;
    private boolean withinLevel;
    private boolean finished;

    private Sweep(ClientLevel level, BlockPos from, ChunkScanRequest want) {
        this.level = level;
        this.want = want;
        this.lowest = level.getMinY();
        this.standingLevel = from.getY() - this.lowest;
        this.sectionOrder = LevelOrder.nearestFirst(level.getHeight() / 16,
                this.standingLevel >> 4);
        this.originX = from.getX() >> 4;
        this.originZ = from.getZ() >> 4;
        this.furthest = want.chunkRadius() * want.chunkRadius();
        this.finished = want.wanted().isEmpty();
    }

    public static Sweep around(ClientLevel level, BlockPos from, ChunkScanRequest want) {
        return new Sweep(level, from, want);
    }

    public boolean step(long budgetNanos) {
        long deadline = System.nanoTime() + budgetNanos;
        while (cursor < offsets.size()) {
            visit(offsets.get(cursor++));
            if (System.nanoTime() >= deadline) {
                return false;
            }
        }
        if (encompassed() || (found.size() >= want.maxResults()
                && (ring > furthest || (ring > 1 && withinLevel)))) {
            finished = true;
            return true;
        }
        ring++;
        offsets = Ring.at(ring);
        cursor = 0;
        considered = false;
        allUnloaded = true;
        return false;
    }

    public boolean finished() {
        return finished;
    }

    public List<BlockPos> found() {
        return found;
    }

    private boolean encompassed() {
        return considered && allUnloaded;
    }

    private void visit(int[] offset) {
        considered = true;
        int chunkX = originX + offset[0];
        int chunkZ = originZ + offset[1];
        LevelChunk chunk = level.getChunkSource().getChunk(chunkX, chunkZ, null, false);
        if (chunk == null) {
            return;
        }
        allUnloaded = false;
        withinLevel |= ChunkScan.into(chunkX << 4, chunkZ << 4, lowest, chunk,
                sectionOrder, want.wanted(), found, want.maxResults(),
                want.levelWindow(), standingLevel);
    }
}