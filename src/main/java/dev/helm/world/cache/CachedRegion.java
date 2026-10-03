package dev.helm.world.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;

public final class CachedRegion {

    private static final int GRID = 32;
    private static final int SPAN = GRID * 16;

    private final int regionX;
    private final int regionZ;
    private final DimensionType dimension;
    private final ResourceKey<Level> dimensionId;
    private final PackedChunk[][] chunks = new PackedChunk[GRID][GRID];
    private boolean unsaved;

    CachedRegion(int regionX, int regionZ, DimensionType dimension,
                 ResourceKey<Level> dimensionId) {
        this.regionX = regionX;
        this.regionZ = regionZ;
        this.dimension = dimension;
        this.dimensionId = dimensionId;
    }

    public int regionX() {
        return regionX;
    }

    public int regionZ() {
        return regionZ;
    }

    public int minX() {
        return regionX * SPAN;
    }

    public int minZ() {
        return regionZ * SPAN;
    }

    public int height() {
        return dimension.height();
    }

    public int floor() {
        return dimension.minY();
    }

    PackedChunk[][] grid() {
        return chunks;
    }

    public boolean unsaved() {
        return unsaved;
    }

    public void put(int chunkX, int chunkZ, PackedChunk chunk) {
        synchronized (this) {
            chunks[chunkX][chunkZ] = chunk;
            unsaved = true;
        }
    }

    public BlockState stateAt(int x, int y, int z) {
        PackedChunk chunk = at(x >> 4, z >> 4);
        if (chunk == null) {
            return null;
        }
        return chunk.stateAt(x & 15, y, z & 15, dimension, dimensionId);
    }

    public boolean holds(int x, int z) {
        return at(x >> 4, z >> 4) != null;
    }

    public List<int[]> trackedPositions(String name) {
        List<int[]> found = new ArrayList<>();
        for (int chunkX = 0; chunkX < GRID; chunkX++) {
            for (int chunkZ = 0; chunkZ < GRID; chunkZ++) {
                PackedChunk chunk = at(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }
                List<int[]> positions = chunk.trackedByName().get(name);
                if (positions == null) {
                    continue;
                }
                int baseX = (regionX * GRID + chunkX) << 4;
                int baseZ = (regionZ * GRID + chunkZ) << 4;
                for (int[] position : positions) {
                    found.add(new int[]{baseX | position[0], position[1], baseZ | position[2]});
                }
            }
        }
        return found;
    }

    public int packedChunks() {
        int count = 0;
        for (PackedChunk[] row : chunks) {
            for (PackedChunk chunk : row) {
                if (chunk != null) {
                    count++;
                }
            }
        }
        return count;
    }

    public void dropExpired(long oldestAcceptable) {
        synchronized (this) {
            for (PackedChunk[] row : chunks) {
                for (int z = 0; z < GRID; z++) {
                    PackedChunk chunk = row[z];
                    if (chunk != null && chunk.packedAt() < oldestAcceptable) {
                        row[z] = null;
                        unsaved = true;
                    }
                }
            }
        }
    }

    void absorb(RegionPayload payload) {
        synchronized (this) {
            for (int x = 0; x < GRID; x++) {
                for (int z = 0; z < GRID; z++) {
                    if (!payload.present(x, z)) {
                        continue;
                    }
                    chunks[x][z] = new PackedChunk(regionX * GRID + x, regionZ * GRID + z,
                            height(), floor(), payload.bitmaps()[x][z],
                            payload.surfaces()[x][z], trackedFor(payload, x, z),
                            payload.packedAt()[x][z]);
                }
            }
            unsaved = false;
        }
    }

    void markSaved() {
        synchronized (this) {
            unsaved = false;
        }
    }

    private PackedChunk at(int chunkX, int chunkZ) {
        int localX = chunkX - regionX * GRID;
        int localZ = chunkZ - regionZ * GRID;
        if (localX < 0 || localX >= GRID || localZ < 0 || localZ >= GRID) {
            return null;
        }
        return chunks[localX][localZ];
    }

    private static Map<String, List<int[]>> trackedFor(RegionPayload payload, int x, int z) {
        Map<String, List<int[]>> byName = payload.tracked()[x][z];
        return byName == null ? new HashMap<>() : byName;
    }
}
