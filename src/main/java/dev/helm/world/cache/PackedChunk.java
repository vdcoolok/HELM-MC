package dev.helm.world.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;

public final class PackedChunk {

    private final int chunkX;
    private final int chunkZ;
    private final int height;
    private final int floor;
    private final ChunkBitmap bitmap;
    private final BlockState[] surface;
    private final int[] surfaceLevel;
    private final Map<Integer, String> tracked;
    private final Map<String, List<int[]>> trackedByName;
    private final long packedAt;

    public PackedChunk(int chunkX, int chunkZ, int height, int floor, ChunkBitmap bitmap,
                       BlockState[] surface, Map<String, List<int[]>> trackedByName,
                       long packedAt) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.height = height;
        this.floor = floor;
        this.bitmap = bitmap;
        this.surface = surface;
        this.surfaceLevel = new int[256];
        this.trackedByName = trackedByName;
        this.tracked = indexTracked(trackedByName);
        this.packedAt = packedAt;
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                surfaceLevel[ChunkBitmap.columnIndex(x, z)] = bitmap.highestOccupied(x, z);
            }
        }
    }

    public int chunkX() {
        return chunkX;
    }

    public int chunkZ() {
        return chunkZ;
    }

    public int height() {
        return height;
    }

    public ChunkBitmap bitmap() {
        return bitmap;
    }

    public long packedAt() {
        return packedAt;
    }

    public Map<String, List<int[]>> trackedByName() {
        return trackedByName;
    }

    public BlockState surfaceAt(int column) {
        return surface[column];
    }

    public BlockState stateAt(int x, int y, int z, DimensionType dimension,
                              ResourceKey<Level> dimensionId) {
        int relative = y - floor;
        if (relative < 0 || relative >= height) {
            return Blocks.AIR.defaultBlockState();
        }
        int localX = x & 15;
        int localZ = z & 15;
        TerrainKind kind = bitmap.get(localX, relative, localZ);
        if (surfaceLevel[ChunkBitmap.columnIndex(localX, localZ)] == relative
                && kind != TerrainKind.AVOID) {
            return surface[ChunkBitmap.columnIndex(localX, localZ)];
        }
        String name = tracked.get(ChunkBitmap.positionIndex(localX, relative, localZ));
        if (name != null) {
            return BlockNames.required(name).defaultBlockState();
        }
        if (kind == TerrainKind.SOLID) {
            BlockState floorBlock = unbreakableFloor(relative, dimension, dimensionId);
            if (floorBlock != null) {
                return floorBlock;
            }
        }
        return TerrainSample.representative(kind, dimensionId);
    }

    private BlockState unbreakableFloor(int relative, DimensionType dimension,
                                        ResourceKey<Level> dimensionId) {
        if (relative == dimension.logicalHeight() - 1 && dimension.hasCeiling()) {
            return Blocks.BEDROCK.defaultBlockState();
        }
        if ((Level.OVERWORLD.equals(dimensionId) || Level.NETHER.equals(dimensionId))
                && relative < dimension.minY() + 5 - floor) {
            return Blocks.OBSIDIAN.defaultBlockState();
        }
        return null;
    }

    private static Map<Integer, String> indexTracked(Map<String, List<int[]>> byName) {
        Map<Integer, String> index = new HashMap<>();
        for (Map.Entry<String, List<int[]>> entry : byName.entrySet()) {
            for (int[] position : entry.getValue()) {
                index.put(ChunkBitmap.positionIndex(position[0], position[1], position[2]),
                        entry.getKey());
            }
        }
        return index;
    }
}
