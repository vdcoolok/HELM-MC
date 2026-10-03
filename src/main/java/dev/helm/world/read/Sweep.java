package dev.helm.world.read;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import dev.helm.world.read.section.PaletteFilter;

public final class Sweep {

    private final ClientLevel level;
    private final ChunkWalk chunks;
    private final SectionOrder sections;
    private final PaletteFilter filter;
    private final int resultsWanted;
    private final List<BlockPos> found = new ArrayList<>();

    private int chunksRead;

    private Sweep(ClientLevel level, BlockPos from, SweepRequest request) {
        this.level = level;
        this.filter = new PaletteFilter(request.wanted());
        this.resultsWanted = request.resultsWanted();
        this.chunks = new ChunkWalk(from.getX() >> 4, from.getZ() >> 4, request.chunkRadius());
        this.sections = new SectionOrder(level.getSectionsCount(),
                (from.getY() - level.getMinY()) >> 4);
    }

    public static Sweep around(ClientLevel level, BlockPos from, SweepRequest request) {
        return new Sweep(level, from, request);
    }

    public boolean step(long budgetNanos) {
        long deadline = System.nanoTime() + budgetNanos;
        while (chunks.next()) {
            read(chunks.chunkX(), chunks.chunkZ());
            if (found.size() >= resultsWanted) {
                return true;
            }
            if (System.nanoTime() >= deadline) {
                return false;
            }
        }
        return true;
    }

    public List<BlockPos> found() {
        return found;
    }

    public int chunksRead() {
        return chunksRead;
    }

    private void read(int chunkX, int chunkZ) {
        LevelChunk chunk = level.getChunkSource()
                .getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
        if (chunk == null || chunk.isEmpty()) {
            return;
        }
        chunksRead++;
        ColumnScan.into(chunk, chunkX << 4, chunkZ << 4, sections, filter, found);
    }
}
